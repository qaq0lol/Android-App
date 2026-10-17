/* SPDX-License-Identifier: Apache-2.0
 *
 * Copyright © 2017-2022 Jason A. Donenfeld <Jason@zx2c4.com>. All Rights Reserved.
 */

package main

// #cgo LDFLAGS: -llog
// #include <android/log.h>
import "C"

import (
	"fmt"
	"math"
	"net"
	"os"
	"os/signal"
	"runtime"
	"runtime/debug"
	"strings"
	"sync"
	"unsafe"

	"golang.org/x/sys/unix"
	"github.com/amnezia-vpn/amneziawg-go/conn"
	"github.com/amnezia-vpn/amneziawg-go/device"
	"github.com/amnezia-vpn/amneziawg-go/ipc"
	"github.com/amnezia-vpn/amneziawg-go/tun"
)

type AndroidLogger struct {
	level C.int
	tag   *C.char
}

func cstring(s string) *C.char {
	b, err := unix.BytePtrFromString(s)
	if err != nil {
		b := [1]C.char{}
		return &b[0]
	}
	return (*C.char)(unsafe.Pointer(b))
}

func (l AndroidLogger) Printf(format string, args ...interface{}) {
	C.__android_log_write(l.level, l.tag, cstring(fmt.Sprintf(format, args...)))
}

type TunnelHandle struct {
	// device is the device-facing (second-hop) tunnel in a chain, or the only tunnel normally.
	device *device.Device
	// outerDevice is the imported first-hop WireGuard tunnel when doubleHop is true.
	outerDevice *device.Device
	uapi net.Listener
	outerTun *chainTun
	innerTun tun.Device
	chainBind *chainBind
	outerSettings string
	outerAddresses string
	doubleHop bool
}

var tunnelHandles map[int32]TunnelHandle
var tunnelHandlesMutex sync.RWMutex
var signalHandlerOnce sync.Once

func init() {
	tunnelHandles = make(map[int32]TunnelHandle)
}

func ensureSignalHandlerStarted() {
	signalHandlerOnce.Do(func() {
		signals := make(chan os.Signal, 1)
		signal.Notify(signals, unix.SIGUSR2)
		go func() {
			buf := make([]byte, os.Getpagesize())
			for {
				select {
				case <-signals:
					n := runtime.Stack(buf, true)
					if n == len(buf) {
						n--
					}
					buf[n] = 0
					C.__android_log_write(C.ANDROID_LOG_ERROR, cstring("WireGuard/GoBackend/Stacktrace"), (*C.char)(unsafe.Pointer(&buf[0])))
				}
			}
		}()
	})
}

//export wgTurnOn
func wgTurnOn(interfaceName string, tunFd int32, settings string, customTun bool) int32 {
	// Lazily start signal handler on first use to avoid crashes during library load
	ensureSignalHandlerStarted()

	tag := cstring("WireGuard/GoBackend/" + interfaceName)
	logger := &device.Logger{
		Verbosef: AndroidLogger{level: C.ANDROID_LOG_DEBUG, tag: tag}.Printf,
		Errorf:   AndroidLogger{level: C.ANDROID_LOG_ERROR, tag: tag}.Printf,
	}

	tun, name, err := tun.CreateUnmonitoredTUNFromFD(int(tunFd), customTun)
	if err != nil {
		unix.Close(int(tunFd))
		logger.Errorf("CreateUnmonitoredTUNFromFD: %v", err)
		return -1
	}

	logger.Verbosef("Attaching to interface %v", name)
	device := device.NewDevice(tun, conn.NewStdNetBind(), logger)

	err = device.IpcSet(settings)
	if err != nil {
		unix.Close(int(tunFd))
		logger.Errorf("IpcSet: %v", err)
		return -1
	}
	device.DisableSomeRoamingForBrokenMobileSemantics()

	var uapi net.Listener

	uapiFile, err := ipc.UAPIOpen(name)
	if err != nil {
		logger.Errorf("UAPIOpen: %v", err)
	} else {
		uapi, err = ipc.UAPIListen(name, uapiFile)
		if err != nil {
			uapiFile.Close()
			logger.Errorf("UAPIListen: %v", err)
		} else {
			go func() {
				for {
					conn, err := uapi.Accept()
					if err != nil {
						return
					}
					go device.IpcHandle(conn)
				}
			}()
		}
	}

	err = device.Up()
	if err != nil {
		logger.Errorf("Unable to bring up device: %v", err)
		uapiFile.Close()
		device.Close()
		return -1
	}
	logger.Verbosef("Device started")

	var i int32
	tunnelHandlesMutex.Lock()
	for i = 0; i < math.MaxInt32; i++ {
		if _, exists := tunnelHandles[i]; !exists {
			break
		}
	}
	if i == math.MaxInt32 {
		tunnelHandlesMutex.Unlock()
		logger.Errorf("Unable to find empty handle")
		uapiFile.Close()
		device.Close()
		return -1
	}
	tunnelHandles[i] = TunnelHandle{device: device, uapi: uapi}
	tunnelHandlesMutex.Unlock()
	return i
}


//export wgTurnOnDoubleHop
func wgTurnOnDoubleHop(interfaceName string, tunFd int32, outerSettings string, outerAddresses string, customTun bool) int32 {
	ensureSignalHandlerStarted()
	tag := cstring("WireGuard/GoBackend/DoubleHop/" + interfaceName)
	logger := &device.Logger{
		Verbosef: AndroidLogger{level: C.ANDROID_LOG_DEBUG, tag: tag}.Printf,
		Errorf:   AndroidLogger{level: C.ANDROID_LOG_ERROR, tag: tag}.Printf,
	}

	// The Android VPN TUN belongs to the Windscribe second hop. The imported first hop
	// runs over an internal packet TUN and therefore does not request a second VpnService.
	innerTun, _, err := tun.CreateUnmonitoredTUNFromFD(int(tunFd), customTun)
	if err != nil {
		unix.Close(int(tunFd))
		logger.Errorf("CreateUnmonitoredTUNFromFD for double-hop: %v", err)
		return -1
	}

	outerTun := newChainTun()
	outerDevice := device.NewDevice(outerTun, conn.NewStdNetBind(), logger)
	// First bring up the outer sockets without sending packets: Java must call protect() on
	// those socket FDs before either hop can begin transmitting.
	if err = outerDevice.IpcSet(withoutPersistentKeepalive(outerSettings)); err != nil {
		logger.Errorf("Could not configure first-hop WireGuard device: %v", err)
		outerDevice.Close()
		innerTun.Close()
		return -1
	}
	outerDevice.DisableSomeRoamingForBrokenMobileSemantics()
	if err = outerDevice.Up(); err != nil {
		logger.Errorf("Could not open first-hop WireGuard sockets: %v", err)
		outerDevice.Close()
		innerTun.Close()
		return -1
	}

	var handleID int32
	tunnelHandlesMutex.Lock()
	for handleID = 0; handleID < math.MaxInt32; handleID++ {
		if _, exists := tunnelHandles[handleID]; !exists {
			break
		}
	}
	if handleID == math.MaxInt32 {
		tunnelHandlesMutex.Unlock()
		logger.Errorf("Unable to allocate a double-hop tunnel handle")
		outerDevice.Close()
		innerTun.Close()
		return -1
	}
	tunnelHandles[handleID] = TunnelHandle{
		outerDevice: outerDevice,
		outerTun: outerTun,
		innerTun: innerTun,
		outerSettings: outerSettings,
		outerAddresses: outerAddresses,
		doubleHop: true,
	}
	tunnelHandlesMutex.Unlock()
	logger.Verbosef("Outer WireGuard tunnel prepared; waiting for Android socket protection")
	return handleID
}

//export wgStartDoubleHop
func wgStartDoubleHop(handleID int32, innerSettings string) int32 {
	tunnelHandlesMutex.RLock()
	handle, ok := tunnelHandles[handleID]
	tunnelHandlesMutex.RUnlock()
	if !ok || !handle.doubleHop || handle.outerDevice == nil || handle.outerTun == nil || handle.innerTun == nil {
		return -1
	}

	logger := &device.Logger{
		Verbosef: AndroidLogger{level: C.ANDROID_LOG_DEBUG, tag: cstring("WireGuard/GoBackend/DoubleHop/Start")}.Printf,
		Errorf:   AndroidLogger{level: C.ANDROID_LOG_ERROR, tag: cstring("WireGuard/GoBackend/DoubleHop/Start")}.Printf,
	}

	// Restore the imported profile's keepalive only after Java has protected the outer sockets.
	if err := handle.outerDevice.IpcSet(handle.outerSettings); err != nil {
		logger.Errorf("Could not activate first-hop WireGuard profile: %v", err)
		return -1
	}

	bind, err := newChainBind(handle.outerTun, handle.outerAddresses)
	if err != nil {
		logger.Errorf("Could not create second-hop transport over first hop: %v", err)
		return -1
	}

	// Transfer ownership of the Android TUN to the new inner device before constructing it.
	// If construction or startup fails, innerDevice.Close() owns cleanup and wgTurnOff will
	// only tear down the outer device. This prevents closing the same TUN descriptor twice.
	tunnelHandlesMutex.Lock()
	current, stillExists := tunnelHandles[handleID]
	if !stillExists || !current.doubleHop || current.innerTun == nil {
		tunnelHandlesMutex.Unlock()
		bind.Close()
		return -1
	}
	innerTun := current.innerTun
	current.innerTun = nil
	tunnelHandles[handleID] = current
	tunnelHandlesMutex.Unlock()

	innerDevice := device.NewDevice(innerTun, bind, logger)
	if err := innerDevice.IpcSet(innerSettings); err != nil {
		logger.Errorf("Could not configure Windscribe second-hop WireGuard device: %v", err)
		innerDevice.Close()
		return -1
	}
	innerDevice.DisableSomeRoamingForBrokenMobileSemantics()
	if err := innerDevice.Up(); err != nil {
		logger.Errorf("Could not activate Windscribe second-hop over first hop: %v", err)
		innerDevice.Close()
		return -1
	}

	tunnelHandlesMutex.Lock()
	current, stillExists = tunnelHandles[handleID]
	if !stillExists || !current.doubleHop || current.outerDevice != handle.outerDevice {
		tunnelHandlesMutex.Unlock()
		innerDevice.Close()
		return -1
	}
	current.device = innerDevice
	current.chainBind = bind
	tunnelHandles[handleID] = current
	tunnelHandlesMutex.Unlock()
	logger.Verbosef("Both WireGuard hops are running")
	return 0
}

//export wgTurnOff
func wgTurnOff(tunnelHandle int32) {
	tunnelHandlesMutex.Lock()
	handle, ok := tunnelHandles[tunnelHandle]
	if !ok {
		tunnelHandlesMutex.Unlock()
		return
	}
	delete(tunnelHandles, tunnelHandle)
	tunnelHandlesMutex.Unlock()

	if handle.uapi != nil {
		handle.uapi.Close()
	}
	// Close the inner/device-facing tunnel first so its transport stops writing packets
	// before the outer TUN and sockets are torn down.
	if handle.device != nil {
		handle.device.Close()
	}
	if handle.outerDevice != nil {
		handle.outerDevice.Close()
	}
	if handle.innerTun != nil {
		handle.innerTun.Close()
	}
}

//export wgGetSocketV4
func wgGetSocketV4(tunnelHandle int32) int32 {
	tunnelHandlesMutex.RLock()
	handle, ok := tunnelHandles[tunnelHandle]
	tunnelHandlesMutex.RUnlock()
	if !ok {
		return -1
	}
	bindDevice := handle.device
	if handle.outerDevice != nil {
		bindDevice = handle.outerDevice
	}
	if bindDevice == nil {
		return -1
	}
	bind, _ := bindDevice.Bind().(conn.PeekLookAtSocketFd)
	if bind == nil {
		return -1
	}
	fd, err := bind.PeekLookAtSocketFd4()
	if err != nil {
		return -1
	}
	return int32(fd)
}

//export wgGetSocketV6
func wgGetSocketV6(tunnelHandle int32) int32 {
	tunnelHandlesMutex.RLock()
	handle, ok := tunnelHandles[tunnelHandle]
	tunnelHandlesMutex.RUnlock()
	if !ok {
		return -1
	}
	bindDevice := handle.device
	if handle.outerDevice != nil {
		bindDevice = handle.outerDevice
	}
	if bindDevice == nil {
		return -1
	}
	bind, _ := bindDevice.Bind().(conn.PeekLookAtSocketFd)
	if bind == nil {
		return -1
	}
	fd, err := bind.PeekLookAtSocketFd6()
	if err != nil {
		return -1
	}
	return int32(fd)
}

//export wgGetConfig
func wgGetConfig(tunnelHandle int32) *C.char {
	tunnelHandlesMutex.RLock()
	handle, ok := tunnelHandles[tunnelHandle]
	tunnelHandlesMutex.RUnlock()
	if !ok {
		return nil
	}
	settings, err := handle.device.IpcGet()
	if err != nil {
		return nil
	}
	return C.CString(settings)
}

//export wgVersion
func wgVersion() *C.char {
	info, ok := debug.ReadBuildInfo()
	if !ok {
		return C.CString("unknown")
	}
	for _, dep := range info.Deps {
		if dep.Path == "github.com/amnezia-vpn/amneziawg-go" {
			parts := strings.Split(dep.Version, "-")
			if len(parts) == 3 && len(parts[2]) == 12 {
				return C.CString(parts[2][:7])
			}
			return C.CString(dep.Version)
		}
	}
	return C.CString("unknown")
}

func main() {}
