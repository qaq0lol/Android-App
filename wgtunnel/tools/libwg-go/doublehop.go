package main

import (
	"encoding/binary"
	"errors"
	"fmt"
	"net"
	"net/netip"
	"os"
	"sync"
	"sync/atomic"
	"syscall"

	"github.com/amnezia-vpn/amneziawg-go/conn"
	"github.com/amnezia-vpn/amneziawg-go/tun"
)

// chainTun is an in-process packet interface for the outer (first-hop) WireGuard device.
// Packets written by chainBind are read by the outer WireGuard device. Decrypted packets
// from the first-hop peer are delivered back to chainBind.
type chainTun struct {
	outbound chan []byte
	inbound  chan []byte
	done     chan struct{}
	events   chan tun.Event
	once     sync.Once
}

func newChainTun() *chainTun {
	t := &chainTun{
		outbound: make(chan []byte, 512),
		inbound:  make(chan []byte, 512),
		done:     make(chan struct{}),
		events:   make(chan tun.Event, 1),
	}
	t.events <- tun.EventUp
	return t
}

func (t *chainTun) File() *os.File { return nil }

func (t *chainTun) Read(bufs [][]byte, sizes []int, offset int) (int, error) {
	if len(bufs) == 0 || len(sizes) == 0 || offset < 0 || offset >= len(bufs[0]) {
		return 0, syscall.EINVAL
	}
	for {
		select {
		case <-t.done:
			return 0, os.ErrClosed
		case packet := <-t.outbound:
			if len(packet) > len(bufs[0])-offset {
				return 0, syscall.EMSGSIZE
			}
			sizes[0] = copy(bufs[0][offset:], packet)
			return 1, nil
		}
	}
}

func (t *chainTun) Write(bufs [][]byte, offset int) (int, error) {
	if offset < 0 {
		return 0, syscall.EINVAL
	}
	for i, buf := range bufs {
		if offset > len(buf) {
			return i, syscall.EINVAL
		}
		packet := append([]byte(nil), buf[offset:]...)
		select {
		case <-t.done:
			return i, os.ErrClosed
		case t.inbound <- packet:
		}
	}
	return len(bufs), nil
}

func (t *chainTun) MTU() (int, error) { return 1420, nil }
func (t *chainTun) Name() (string, error) { return "windscribe-first-hop", nil }
func (t *chainTun) Events() <-chan tun.Event { return t.events }
func (t *chainTun) BatchSize() int { return 1 }

func (t *chainTun) Close() error {
	t.once.Do(func() {
		close(t.done)
		close(t.events)
	})
	return nil
}

func (t *chainTun) send(packet []byte) error {
	data := append([]byte(nil), packet...)
	select {
	case <-t.done:
		return os.ErrClosed
	case t.outbound <- data:
		return nil
	}
}

func (t *chainTun) receive() ([]byte, error) {
	select {
	case <-t.done:
		return nil, os.ErrClosed
	case packet := <-t.inbound:
		return packet, nil
	}
}

type chainEndpoint struct {
	addr    netip.AddrPort
	srcAddr netip.Addr
}

func (e *chainEndpoint) ClearSrc() { e.srcAddr = netip.Addr{} }
func (e *chainEndpoint) SrcToString() string {
	if !e.srcAddr.IsValid() {
		return ""
	}
	return netip.AddrPortFrom(e.srcAddr, 0).String()
}
func (e *chainEndpoint) DstToString() string { return e.addr.String() }
func (e *chainEndpoint) DstToBytes() []byte {
	data, _ := e.addr.MarshalBinary()
	return data
}
func (e *chainEndpoint) DstIP() netip.Addr { return e.addr.Addr() }
func (e *chainEndpoint) SrcIP() netip.Addr { return e.srcAddr }

var nextChainPort atomic.Uint32

// chainBind implements WireGuard's UDP Bind interface by encoding the datagrams
// as IP/UDP packets and feeding them into the first-hop WireGuard device's TUN.
type chainBind struct {
	tun       *chainTun
	sourceV4  netip.Addr
	sourceV6  netip.Addr
	mu        sync.RWMutex
	port      uint16
	opened    bool
	closed    chan struct{}
	closeOnce sync.Once
	allowed   map[netip.AddrPort]struct{}
}

func newChainBind(t *chainTun, addresses string) (*chainBind, error) {
	b := &chainBind{
		tun:     t,
		closed:  make(chan struct{}),
		allowed: make(map[netip.AddrPort]struct{}),
	}
	for _, raw := range splitAddressList(addresses) {
		ip, err := netip.ParseAddr(raw)
		if err != nil {
			return nil, fmt.Errorf("invalid first-hop interface address %q: %w", raw, err)
		}
		ip = ip.Unmap()
		if ip.Is4() && !b.sourceV4.IsValid() {
			b.sourceV4 = ip
		} else if ip.Is6() && !b.sourceV6.IsValid() {
			b.sourceV6 = ip
		}
	}
	if !b.sourceV4.IsValid() && !b.sourceV6.IsValid() {
		return nil, errors.New("first-hop WireGuard configuration must contain an IPv4 or IPv6 Interface Address")
	}
	return b, nil
}

func splitAddressList(value string) []string {
	var result []string
	for _, item := range stringsSplit(value, ",") {
		item = stringsTrimSpace(item)
		if item == "" {
			continue
		}
		if slash := stringsIndexByte(item, '/'); slash >= 0 {
			item = item[:slash]
		}
		result = append(result, item)
	}
	return result
}

func (b *chainBind) Open(port uint16) ([]conn.ReceiveFunc, uint16, error) {
	b.mu.Lock()
	defer b.mu.Unlock()
	if b.opened {
		return nil, 0, conn.ErrBindAlreadyOpen
	}
	if port == 0 {
		next := nextChainPort.Add(1)
		port = uint16(49152 + (next % 15000))
		if port == 0 {
			port = 49152
		}
	}
	b.port = port
	b.opened = true
	return []conn.ReceiveFunc{b.receive}, port, nil
}

func (b *chainBind) Close() error {
	b.closeOnce.Do(func() {
		close(b.closed)
	})
	b.mu.Lock()
	b.opened = false
	b.mu.Unlock()
	return nil
}

func (b *chainBind) SetMark(uint32) error { return nil }
func (b *chainBind) BatchSize() int { return 1 }

func (b *chainBind) ParseEndpoint(value string) (conn.Endpoint, error) {
	host, portText, err := net.SplitHostPort(value)
	if err != nil {
		return nil, err
	}
	address, err := net.ResolveUDPAddr("udp", net.JoinHostPort(host, portText))
	if err != nil {
		return nil, err
	}
	ip, ok := netip.AddrFromSlice(address.IP)
	if !ok {
		return nil, fmt.Errorf("could not resolve WireGuard endpoint %q", value)
	}
	addr := netip.AddrPortFrom(ip.Unmap(), uint16(address.Port))
	if !addr.IsValid() || addr.Port() == 0 {
		return nil, fmt.Errorf("invalid WireGuard endpoint %q", value)
	}
	b.mu.Lock()
	b.allowed[addr] = struct{}{}
	b.mu.Unlock()
	return &chainEndpoint{addr: addr}, nil
}

func (b *chainBind) Send(buffers [][]byte, endpoint conn.Endpoint) error {
	ep, ok := endpoint.(*chainEndpoint)
	if !ok {
		return conn.ErrWrongEndpointType
	}
	b.mu.RLock()
	port := b.port
	opened := b.opened
	b.mu.RUnlock()
	if !opened {
		return os.ErrClosed
	}

	source := b.sourceV4
	if ep.addr.Addr().Is6() {
		source = b.sourceV6
	}
	if !source.IsValid() {
		return fmt.Errorf("first-hop WireGuard configuration has no address for endpoint family %s", ep.addr.Addr())
	}

	for _, payload := range buffers {
		packet, err := buildUDPIPPacket(source, ep.addr, port, payload)
		if err != nil {
			return err
		}
		if err := b.tun.send(packet); err != nil {
			return err
		}
	}
	return nil
}

func (b *chainBind) receive(packets [][]byte, sizes []int, endpoints []conn.Endpoint) (int, error) {
	if len(packets) == 0 || len(sizes) == 0 || len(endpoints) == 0 {
		return 0, syscall.EINVAL
	}
	for {
		select {
		case <-b.closed:
			return 0, net.ErrClosed
		case <-b.tun.done:
			return 0, net.ErrClosed
		default:
		}

		packet, err := b.tun.receive()
		if err != nil {
			return 0, net.ErrClosed
		}
		srcIP, dstIP, srcPort, dstPort, payload, ok := parseUDPIPPacket(packet)
		if !ok {
			continue
		}
		b.mu.RLock()
		listenPort := b.port
		_, knownEndpoint := b.allowed[netip.AddrPortFrom(srcIP, srcPort)]
		b.mu.RUnlock()
		if dstPort != listenPort || !knownEndpoint {
			continue
		}
		if len(payload) > len(packets[0]) {
			continue
		}
		sizes[0] = copy(packets[0], payload)
		endpoints[0] = &chainEndpoint{
			addr:    netip.AddrPortFrom(srcIP, srcPort),
			srcAddr: dstIP,
		}
		return 1, nil
	}
}

func buildUDPIPPacket(source netip.Addr, destination netip.AddrPort, sourcePort uint16, payload []byte) ([]byte, error) {
	destinationIP := destination.Addr().Unmap()
	source = source.Unmap()
	if source.Is4() != destinationIP.Is4() {
		return nil, errors.New("first-hop source and destination address families do not match")
	}
	udpLength := 8 + len(payload)
	if udpLength > 65535 {
		return nil, errors.New("WireGuard UDP packet exceeds IP payload limit")
	}
	udpPacket := make([]byte, udpLength)
	binary.BigEndian.PutUint16(udpPacket[0:2], sourcePort)
	binary.BigEndian.PutUint16(udpPacket[2:4], destination.Port())
	binary.BigEndian.PutUint16(udpPacket[4:6], uint16(udpLength))
	copy(udpPacket[8:], payload)

	if source.Is4() {
		ipPacket := make([]byte, 20+udpLength)
		ipPacket[0] = 0x45
		binary.BigEndian.PutUint16(ipPacket[2:4], uint16(len(ipPacket)))
		ipPacket[8] = 64
		ipPacket[9] = 17
		src := source.As4()
		dst := destinationIP.As4()
		copy(ipPacket[12:16], src[:])
		copy(ipPacket[16:20], dst[:])
		binary.BigEndian.PutUint16(ipPacket[10:12], internetChecksum(ipPacket[:20]))
		copy(ipPacket[20:], udpPacket)
		return ipPacket, nil
	}

	ipPacket := make([]byte, 40+udpLength)
	ipPacket[0] = 0x60
	binary.BigEndian.PutUint16(ipPacket[4:6], uint16(udpLength))
	ipPacket[6] = 17
	ipPacket[7] = 64
	src := source.As16()
	dst := destinationIP.As16()
	copy(ipPacket[8:24], src[:])
	copy(ipPacket[24:40], dst[:])
	pseudo := make([]byte, 40+len(udpPacket))
	copy(pseudo[0:16], src[:])
	copy(pseudo[16:32], dst[:])
	binary.BigEndian.PutUint32(pseudo[32:36], uint32(len(udpPacket)))
	pseudo[39] = 17
	copy(pseudo[40:], udpPacket)
	checksum := internetChecksum(pseudo)
	if checksum == 0 {
		checksum = 0xffff
	}
	binary.BigEndian.PutUint16(udpPacket[6:8], checksum)
	copy(ipPacket[40:], udpPacket)
	return ipPacket, nil
}

func parseUDPIPPacket(packet []byte) (source, destination netip.Addr, sourcePort, destinationPort uint16, payload []byte, ok bool) {
	if len(packet) < 1 {
		return
	}
	switch packet[0] >> 4 {
	case 4:
		if len(packet) < 28 {
			return
		}
		headerLength := int(packet[0]&0x0f) * 4
		totalLength := int(binary.BigEndian.Uint16(packet[2:4]))
		if headerLength < 20 || totalLength < headerLength+8 || totalLength > len(packet) || packet[9] != 17 {
			return
		}
		fragment := binary.BigEndian.Uint16(packet[6:8])
		if fragment&0x1fff != 0 {
			return
		}
		var src, dst [4]byte
		copy(src[:], packet[12:16])
		copy(dst[:], packet[16:20])
		source = netip.AddrFrom4(src)
		destination = netip.AddrFrom4(dst)
		udp := packet[headerLength:totalLength]
		udpLength := int(binary.BigEndian.Uint16(udp[4:6]))
		if udpLength < 8 || udpLength > len(udp) {
			return
		}
		sourcePort = binary.BigEndian.Uint16(udp[0:2])
		destinationPort = binary.BigEndian.Uint16(udp[2:4])
		payload = udp[8:udpLength]
		return source, destination, sourcePort, destinationPort, payload, true
	case 6:
		if len(packet) < 48 || packet[6] != 17 {
			return
		}
		totalLength := 40 + int(binary.BigEndian.Uint16(packet[4:6]))
		if totalLength < 48 || totalLength > len(packet) {
			return
		}
		var src, dst [16]byte
		copy(src[:], packet[8:24])
		copy(dst[:], packet[24:40])
		source = netip.AddrFrom16(src)
		destination = netip.AddrFrom16(dst)
		udp := packet[40:totalLength]
		udpLength := int(binary.BigEndian.Uint16(udp[4:6]))
		if udpLength < 8 || udpLength > len(udp) {
			return
		}
		sourcePort = binary.BigEndian.Uint16(udp[0:2])
		destinationPort = binary.BigEndian.Uint16(udp[2:4])
		payload = udp[8:udpLength]
		return source, destination, sourcePort, destinationPort, payload, true
	default:
		return
	}
}

func internetChecksum(data []byte) uint16 {
	var sum uint32
	for len(data) >= 2 {
		sum += uint32(binary.BigEndian.Uint16(data[:2]))
		data = data[2:]
	}
	if len(data) == 1 {
		sum += uint32(data[0]) << 8
	}
	for sum>>16 != 0 {
		sum = (sum & 0xffff) + (sum >> 16)
	}
	return ^uint16(sum)
}
