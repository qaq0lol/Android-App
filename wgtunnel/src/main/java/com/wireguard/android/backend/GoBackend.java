/*
 * Copyright © 2017-2023 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;

import com.windscribe.common.DNSDetails;
import com.windscribe.common.DnsType;
import com.windscribe.common.VPNTunnelWrapper;
import com.wireguard.android.backend.BackendException.Reason;
import com.wireguard.android.backend.Tunnel.State;
import com.wireguard.android.util.SharedLibraryLoader;
import com.wireguard.config.Config;
import com.wireguard.config.InetEndpoint;
import com.wireguard.config.InetNetwork;
import com.wireguard.config.Peer;
import com.wireguard.crypto.Key;
import com.wireguard.crypto.KeyFormatException;
import com.wireguard.util.NonNullForAll;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import androidx.annotation.Nullable;
import androidx.collection.ArraySet;

/**
 * Implementation of {@link Backend} that uses the wireguard-go userspace implementation to provide
 * WireGuard tunnels.
 */
@NonNullForAll
public final class GoBackend implements Backend {
    private static final int DNS_RESOLUTION_RETRIES = 10;
    private static final String TAG = "WireGuard/GoBackend";
    @Nullable private static AlwaysOnCallback alwaysOnCallback;
    private static GhettoCompletableFuture<VpnService> vpnService = new GhettoCompletableFuture<>();
    private final Context context;
    @Nullable private Config currentConfig;
    @Nullable private Tunnel currentTunnel;
    private int currentTunnelHandle = -1;
    private final Object tunnelStateLock = new Object();

    private VPNTunnelWrapper tunnelWrapper;

    /**
     * Public constructor for GoBackend.
     *
     * @param context An Android {@link Context}
     */
    public GoBackend(final Context context) {
        SharedLibraryLoader.loadSharedLibrary(context, "wg-go");
        this.context = context;
    }

    /**
     * Set a {@link AlwaysOnCallback} to be invoked when {@link VpnService} is started by the
     * system's Always-On VPN mode.
     *
     * @param cb Callback to be invoked
     */
    public static void setAlwaysOnCallback(final AlwaysOnCallback cb) {
        alwaysOnCallback = cb;
    }

    @Nullable private static native String wgGetConfig(int handle);

    private static native int wgGetSocketV4(int handle);

    private static native int wgGetSocketV6(int handle);

    private static native void wgTurnOff(int handle);

    private static native int wgTurnOn(String ifName, int tunFd, String settings, boolean customTun);

    private static native int wgTurnOnDoubleHop(String ifName, int tunFd, String outerSettings, String outerAddresses, boolean customTun);

    private static native int wgStartDoubleHop(int handle, String innerSettings);

    private static native String wgVersion();

    /**
     * Method to get the names of running tunnels.
     *
     * @return A set of string values denoting names of running tunnels.
     */
    @Override
    public Set<String> getRunningTunnelNames() {
        if (currentTunnel != null) {
            final Set<String> runningTunnels = new ArraySet<>();
            runningTunnels.add(currentTunnel.getName());
            return runningTunnels;
        }
        return Collections.emptySet();
    }

    /**
     * Get the associated {@link State} for a given {@link Tunnel}.
     *
     * @param tunnel The tunnel to examine the state of.
     * @return {@link State} associated with the given tunnel.
     */
    @Override
    public State getState(final Tunnel tunnel) {
        return currentTunnel == tunnel ? State.UP : State.DOWN;
    }

    /**
     * Get the associated {@link Statistics} for a given {@link Tunnel}.
     *
     * @param tunnel The tunnel to retrieve statistics for.
     * @return {@link Statistics} associated with the given tunnel.
     */
    @Override
    public Statistics getStatistics(final Tunnel tunnel) {
        final Statistics stats = new Statistics();
        if (tunnel != currentTunnel || currentTunnelHandle == -1)
            return stats;
        final String config = wgGetConfig(currentTunnelHandle);
        if (config == null)
            return stats;
        Key key = null;
        long rx = 0;
        long tx = 0;
        long latestHandshakeMSec = 0;
        for (final String line : config.split("\\n")) {
            if (line.startsWith("public_key=")) {
                if (key != null)
                    stats.add(key, rx, tx, latestHandshakeMSec);
                rx = 0;
                tx = 0;
                latestHandshakeMSec = 0;
                try {
                    key = Key.fromHex(line.substring(11));
                } catch (final KeyFormatException ignored) {
                    key = null;
                }
            } else if (line.startsWith("rx_bytes=")) {
                if (key == null)
                    continue;
                try {
                    rx = Long.parseLong(line.substring(9));
                } catch (final NumberFormatException ignored) {
                    rx = 0;
                }
            } else if (line.startsWith("tx_bytes=")) {
                if (key == null)
                    continue;
                try {
                    tx = Long.parseLong(line.substring(9));
                } catch (final NumberFormatException ignored) {
                    tx = 0;
                }
            } else if (line.startsWith("last_handshake_time_sec=")) {
                if (key == null)
                    continue;
                try {
                    latestHandshakeMSec += Long.parseLong(line.substring(24)) * 1000;
                } catch (final NumberFormatException ignored) {
                    latestHandshakeMSec = 0;
                }
            } else if (line.startsWith("last_handshake_time_nsec=")) {
                if (key == null)
                    continue;
                try {
                    latestHandshakeMSec += Long.parseLong(line.substring(25)) / 1000000;
                } catch (final NumberFormatException ignored) {
                    latestHandshakeMSec = 0;
                }
            }
        }
        if (key != null)
            stats.add(key, rx, tx, latestHandshakeMSec);
        return stats;
    }

    /**
     * Get the version of the underlying wireguard-go library.
     *
     * @return {@link String} value of the version of the wireguard-go library.
     */
    @Override
    public String getVersion() {
        return wgVersion();
    }

    /**
     * Change the state of a given {@link Tunnel}, optionally applying a given {@link Config}.
     *
     * @param tunnel The tunnel to control the state of.
     * @param state  The new state for this tunnel. Must be {@code UP}, {@code DOWN}, or
     *               {@code TOGGLE}.
     * @param config The configuration for this tunnel, may be null if state is {@code DOWN}.
     * @return {@link State} of the tunnel after state changes are applied.
     * @throws Exception Exception raised while changing tunnel state.
     */
    @Override
    public State setState(final Tunnel tunnel, State state, @Nullable final Config config) throws Exception {
        return setStateWithOptionalFirstHop(tunnel, state, config, null);
    }

    /**
     * Establishes Windscribe's WireGuard tunnel as the inner hop, using the supplied WireGuard
     * profile as its encrypted underlay. Only the inner tunnel owns Android's VpnService TUN.
     */
    public State setStateDoubleHop(
            final Tunnel tunnel,
            State state,
            @Nullable final Config innerConfig,
            final Config firstHopConfig) throws Exception {
        if (state != State.UP) {
            return setStateWithOptionalFirstHop(tunnel, state, innerConfig, null);
        }
        if (firstHopConfig == null) {
            throw new IllegalArgumentException("A first-hop WireGuard profile is required");
        }
        return setStateWithOptionalFirstHop(tunnel, state, innerConfig, firstHopConfig);
    }

    private State setStateWithOptionalFirstHop(
            final Tunnel tunnel,
            State state,
            @Nullable final Config config,
            @Nullable final Config firstHopConfig) throws Exception {
        final State originalState = getState(tunnel);

        if (state == State.TOGGLE)
            state = originalState == State.UP ? State.DOWN : State.UP;
        if (state == originalState && tunnel == currentTunnel && config == currentConfig)
            return originalState;
        if (state == State.UP) {
            final Config originalConfig = currentConfig;
            final Tunnel originalTunnel = currentTunnel;
            if (currentTunnel != null)
                setStateInternal(currentTunnel, null, State.DOWN, null);
            try {
                setStateInternal(tunnel, config, state, firstHopConfig);
            } catch (final Exception e) {
                // Do not silently restore a previously active single-hop tunnel when the
                // user explicitly requested double-hop. A failed chain must remain disconnected.
                if (originalTunnel != null && firstHopConfig == null)
                    setStateInternal(originalTunnel, originalConfig, State.UP, null);
                throw e;
            }
        } else if (state == State.DOWN && tunnel == currentTunnel) {
            setStateInternal(tunnel, null, State.DOWN, null);
        }
        return getState(tunnel);
    }



    /**
     * Resolve peer endpoint hostnames before the Android VPN TUN is established.
     *
     * When using an imported underlay, DNS lookups made after Builder.establish() can
     * otherwise be routed into the not-yet-started inner VPN and stall the connection.
     */
    private String createResolvedUserspaceConfig(final Config config) throws Exception {
        // Peer.toWgUserspaceString() already serializes the resolved endpoint. Validate that
        // every configured peer has a cached numeric endpoint before passing settings to Go.
        ensureEndpointsResolved(config);
        final String settings = config.toWgUserspaceString();
        for (final Peer peer : config.getPeers()) {
            final InetEndpoint endpoint = peer.getEndpoint().orElse(null);
            if (endpoint == null) {
                continue;
            }
            final InetEndpoint resolved = endpoint.getResolved().orElse(null);
            if (resolved == null) {
                throw new BackendException(Reason.DNS_RESOLUTION_FAILURE, endpoint.getHost());
            }
            final String expected = "endpoint=" + resolved;
            if (!settings.contains(expected)) {
                throw new IllegalStateException(
                        "The resolved WireGuard endpoint was not included in native settings: "
                                + endpoint.getHost());
            }
        }
        return settings;
    }

    private static String getInterfaceAddresses(final Config config) {
        final StringBuilder addresses = new StringBuilder();
        for (final InetNetwork address : config.getInterface().getAddresses()) {
            if (addresses.length() > 0) {
                addresses.append(',');
            }
            addresses.append(address.toString());
        }
        return addresses.toString();
    }

    private void protectWireGuardSockets(
            final VpnService service,
            final int tunnelHandle,
            @Nullable final Config firstHopConfig) throws Exception {
        final int socketV4 = wgGetSocketV4(tunnelHandle);
        final int socketV6 = wgGetSocketV6(tunnelHandle);
        if (firstHopConfig == null) {
            if (socketV4 >= 0) {
                service.protect(socketV4);
            }
            if (socketV6 >= 0) {
                service.protect(socketV6);
            }
            return;
        }

        boolean needsV4 = false;
        boolean needsV6 = false;
        for (final Peer peer : firstHopConfig.getPeers()) {
            final InetEndpoint endpoint = peer.getEndpoint().orElse(null);
            if (endpoint == null) {
                continue;
            }
            final InetEndpoint resolved = endpoint.getResolved().orElse(null);
            if (resolved == null) {
                throw new BackendException(Reason.DNS_RESOLUTION_FAILURE, endpoint.getHost());
            }
            final InetAddress address = InetAddress.getByName(resolved.getHost());
            if (address instanceof java.net.Inet6Address) {
                needsV6 = true;
            } else {
                needsV4 = true;
            }
        }

        final boolean protectedV4 = socketV4 >= 0 && service.protect(socketV4);
        final boolean protectedV6 = socketV6 >= 0 && service.protect(socketV6);
        if ((needsV4 && !protectedV4) || (needsV6 && !protectedV6) || (!protectedV4 && !protectedV6)) {
            throw new BackendException(Reason.UNABLE_TO_START_VPN);
        }
    }

    private void setStateInternal(
            final Tunnel tunnel,
            @Nullable final Config config,
            final State state,
            @Nullable final Config firstHopConfig) throws Exception {
        // Resolve both peers before entering the tunnel-state lock; DNS can block for seconds.
        if (state == State.UP && config != null) {
            ensureEndpointsResolved(config);
            if (firstHopConfig != null) {
                ensureEndpointsResolved(firstHopConfig);
                validateFirstHopRoutes(config, firstHopConfig);
            }
        }

        synchronized (tunnelStateLock) {
            setStateInternalLocked(tunnel, config, state, firstHopConfig);
        }
    }


    private void validateFirstHopRoutes(final Config innerConfig, final Config firstHopConfig) throws Exception {
        for (final Peer innerPeer : innerConfig.getPeers()) {
            final InetEndpoint endpoint = innerPeer.getEndpoint().orElse(null);
            if (endpoint == null) {
                continue;
            }
            final InetEndpoint resolved = endpoint.getResolved().orElse(null);
            if (resolved == null) {
                throw new BackendException(Reason.DNS_RESOLUTION_FAILURE, endpoint.getHost());
            }
            final InetAddress destination = InetAddress.getByName(resolved.getHost());
            boolean routedByFirstHop = false;
            for (final Peer firstHopPeer : firstHopConfig.getPeers()) {
                for (final InetNetwork allowed : firstHopPeer.getAllowedIps()) {
                    if (networkContains(allowed, destination)) {
                        routedByFirstHop = true;
                        break;
                    }
                }
                if (routedByFirstHop) {
                    break;
                }
            }
            if (!routedByFirstHop) {
                throw new IllegalArgumentException(
                        "The first-hop WireGuard AllowedIPs does not include the selected Windscribe server endpoint ("
                                + destination.getHostAddress()
                                + "). Add the endpoint's IP range to the first-hop peer AllowedIPs.");
            }
        }
    }

    private static boolean networkContains(final InetNetwork network, final InetAddress address) {
        final byte[] networkBytes = network.getAddress().getAddress();
        final byte[] addressBytes = address.getAddress();
        if (networkBytes.length != addressBytes.length) {
            return false;
        }
        int bits = network.getMask();
        for (int i = 0; i < networkBytes.length && bits > 0; i++) {
            final int compareBits = Math.min(8, bits);
            final int mask = 0xff << (8 - compareBits) & 0xff;
            if ((networkBytes[i] & mask) != (addressBytes[i] & mask)) {
                return false;
            }
            bits -= compareBits;
        }
        return true;
    }

    private void ensureEndpointsResolved(final Config config) throws Exception {
        dnsRetry: for (int i = 0; i < DNS_RESOLUTION_RETRIES; ++i) {
            for (final Peer peer : config.getPeers()) {
                final InetEndpoint endpoint = peer.getEndpoint().orElse(null);
                if (endpoint == null) {
                    continue;
                }
                if (endpoint.getResolved().orElse(null) == null) {
                    if (i < DNS_RESOLUTION_RETRIES - 1) {
                        Log.w(TAG, "DNS host \"" + endpoint.getHost() + "\" failed to resolve; trying again");
                        Thread.sleep(1000);
                        continue dnsRetry;
                    }
                    throw new BackendException(Reason.DNS_RESOLUTION_FAILURE, endpoint.getHost());
                }
            }
            return;
        }
    }

    private void setStateInternalLocked(
            final Tunnel tunnel,
            @Nullable final Config config,
            final State state,
            @Nullable final Config firstHopConfig) throws Exception {
        Log.i(TAG, "Bringing tunnel " + tunnel.getName() + ' ' + state);

        if (state == State.UP) {
            if (config == null)
                throw new BackendException(Reason.TUNNEL_MISSING_CONFIG);

            if (VpnService.prepare(context) != null)
                throw new BackendException(Reason.VPN_NOT_AUTHORIZED);

            final VpnService service;
            if (!vpnService.isDone()) {
                Log.d(TAG, "Requesting to start VpnService");
                context.startService(new Intent(context, VpnService.class));
            }

            try {
                service = vpnService.get(2, TimeUnit.SECONDS);
            } catch (final TimeoutException e) {
                final Exception be = new BackendException(Reason.UNABLE_TO_START_VPN);
                be.initCause(e);
                throw be;
            }
            service.setOwner(this);

            if (currentTunnelHandle != -1) {
                Log.w(TAG, "Tunnel already up");
                return;
            }

            // DNS resolution already completed in setStateInternal() before acquiring lock

            // Build config
            final String goConfig = firstHopConfig == null ? config.toWgUserspaceString() : createResolvedUserspaceConfig(config);
            // Resolve the imported endpoint before establish() installs the system VPN route.
            final String firstHopSettings = firstHopConfig == null ? null : createResolvedUserspaceConfig(firstHopConfig);
            final String firstHopAddresses = firstHopConfig == null ? null : getInterfaceAddresses(firstHopConfig);

            // Create the vpn tunnel with android API
            final VpnService.Builder builder = service.getBuilder();
            builder.setSession(tunnel.getName());

            for (final String excludedApplication : config.getInterface().getExcludedApplications())
                builder.addDisallowedApplication(excludedApplication);

            for (final String includedApplication : config.getInterface().getIncludedApplications())
                builder.addAllowedApplication(includedApplication);

            // Check if IPv6 is configured by examining interface addresses
            boolean hasIPv6Address = false;
            for (final InetNetwork addr : config.getInterface().getAddresses()) {
                builder.addAddress(addr.getAddress(), addr.getMask());
                if (addr.getAddress() instanceof java.net.Inet6Address) {
                    hasIPv6Address = true;
                }
            }

            for (final InetAddress addr : config.getInterface().getDnsServers())
                builder.addDnsServer(addr.getHostAddress());

            for (final String dnsSearchDomain : config.getInterface().getDnsSearchDomains())
                builder.addSearchDomain(dnsSearchDomain);

            boolean sawDefaultRoute = false;
            for (final Peer peer : config.getPeers()) {
                for (final InetNetwork addr : peer.getAllowedIps()) {
                    if (addr.getMask() == 0)
                        sawDefaultRoute = true;
                    // Skip ::/0 route only if IPv6 is not configured
                    if (!addr.toString().equals("::/0") || hasIPv6Address){
                        builder.addRoute(addr.getAddress(), addr.getMask());
                    }
                }
            }

            // "Kill-switch" semantics
            if (!(sawDefaultRoute && config.getPeers().size() == 1)) {
                builder.allowFamily(OsConstants.AF_INET);
                builder.allowFamily(OsConstants.AF_INET6);
            }

            // The inner WireGuard packet is itself carried inside the outer WireGuard TUN.
            // Cap the app-facing MTU in double-hop mode to leave room for both sets of headers.
            final int configuredMtu = config.getInterface().getMtu().orElse(1280);
            builder.setMtu(firstHopConfig == null ? configuredMtu : Math.min(configuredMtu, 1280));

            service.applyExcludedRoutes(builder);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                builder.setMetered(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                service.setUnderlyingNetworks(null);

            builder.setBlocking(true);

            final DNSDetails dnsDetails = service.getDnsDetails();
            final boolean customTun = dnsDetails != null && dnsDetails.getType() == DnsType.Proxy;

            if (customTun) {
                ParcelFileDescriptor tun = builder.establish();
                if (tun == null)
                    throw new BackendException(Reason.TUN_CREATION_ERROR);
                // Live port, not dnsDetails.getControlDPort(): that value is snapshotted during
                // profile creation, before ctrld picks the port it actually listens on.
                tunnelWrapper = new VPNTunnelWrapper(tun, service, service.getControlDPort(), service.shouldEnablePacketLogging());
                tunnelWrapper.start();
                ParcelFileDescriptor wrappedTun = tunnelWrapper.getParcelDescriptor();
                Log.d(TAG, "Go backend " + wgVersion());
                if (firstHopConfig != null) {
                    currentTunnelHandle = wgTurnOnDoubleHop(
                            tunnel.getName(),
                            wrappedTun.detachFd(),
                            firstHopSettings,
                            firstHopAddresses,
                            true);
                } else {
                    currentTunnelHandle = wgTurnOn(tunnel.getName(), wrappedTun.detachFd(), goConfig, true);
                }
            } else {
                try (final ParcelFileDescriptor tun = builder.establish()) {
                    if (tun == null)
                        throw new BackendException(Reason.TUN_CREATION_ERROR);
                    Log.d(TAG, "Go backend " + wgVersion());
                    if (firstHopConfig != null) {
                        currentTunnelHandle = wgTurnOnDoubleHop(
                                tunnel.getName(),
                                tun.detachFd(),
                                firstHopSettings,
                                firstHopAddresses,
                                false);
                    } else {
                        currentTunnelHandle = wgTurnOn(tunnel.getName(), tun.detachFd(), goConfig, false);
                    }
                }
            }

            if (currentTunnelHandle < 0) {
                if (tunnelWrapper != null) {
                    tunnelWrapper.stop();
                    tunnelWrapper = null;
                }
                throw new BackendException(Reason.GO_ACTIVATION_ERROR_CODE, currentTunnelHandle);
            }

            try {
                protectWireGuardSockets(service, currentTunnelHandle, firstHopConfig);
                if (firstHopConfig != null) {
                    final int startResult = wgStartDoubleHop(currentTunnelHandle, goConfig);
                    if (startResult != 0) {
                        throw new BackendException(Reason.GO_ACTIVATION_ERROR_CODE, startResult);
                    }
                }
            } catch (final Exception e) {
                final int failedHandle = currentTunnelHandle;
                currentTunnelHandle = -1;
                wgTurnOff(failedHandle);
                if (tunnelWrapper != null) {
                    tunnelWrapper.stop();
                    tunnelWrapper = null;
                }
                throw e;
            }

            currentTunnel = tunnel;
            currentConfig = config;
        } else {
            if (currentTunnelHandle == -1) {
                Log.w(TAG, "Tunnel already down");
                return;
            }
            int handleToClose = currentTunnelHandle;
            currentTunnel = null;
            currentTunnelHandle = -1;
            currentConfig = null;
            wgTurnOff(handleToClose);
            try {
                vpnService.get(0, TimeUnit.NANOSECONDS).stopSelf();
            } catch (final TimeoutException ignored) { }
            if (tunnelWrapper!= null){
                tunnelWrapper.stop();
            }
        }

        tunnel.onStateChange(state);
    }

    /**
     * Callback for {@link GoBackend} that is invoked when {@link VpnService} is started by the
     * system's Always-On VPN mode.
     */
    public interface AlwaysOnCallback {
        void alwaysOnTriggered();
    }

    // TODO: When we finally drop API 21 and move to API 24, delete this and replace with the ordinary CompletableFuture.
    private static final class GhettoCompletableFuture<V> {
        private final LinkedBlockingQueue<V> completion = new LinkedBlockingQueue<>(1);
        private final FutureTask<V> result = new FutureTask<>(completion::peek);

        public boolean complete(final V value) {
            final boolean offered = completion.offer(value);
            if (offered)
                result.run();
            return offered;
        }

        public V get() throws ExecutionException, InterruptedException {
            return result.get();
        }

        public V get(final long timeout, final TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            return result.get(timeout, unit);
        }

        public boolean isDone() {
            return !completion.isEmpty();
        }

        public GhettoCompletableFuture<V> newIncompleteFuture() {
            return new GhettoCompletableFuture<>();
        }
    }

    /**
     * {@link android.net.VpnService} implementation for {@link GoBackend}
     */
    public static class VpnService extends android.net.VpnService {
        @Nullable private GoBackend owner;

        @Nullable
        public DNSDetails getDnsDetails() {
            return null;
        }

        /**
         * Port ctrld is listening on. Subclasses MUST override: the port is chosen at random per
         * connection, so this placeholder is never the live value.
         */
        public int getControlDPort() {
            return 5355;
        }

        public boolean shouldEnablePacketLogging() {
            return false;
        }

        public Builder getBuilder() {
            return new Builder();
        }

        protected void applyExcludedRoutes(Builder builder) {
        }

        @Override
        public void onCreate() {
            vpnService.complete(this);
            super.onCreate();
        }

        @Override
        public void onDestroy() {
            if (owner != null) {
                synchronized (owner.tunnelStateLock) {
                    final Tunnel tunnel = owner.currentTunnel;
                    if (tunnel != null) {
                        if (owner.currentTunnelHandle != -1)
                            wgTurnOff(owner.currentTunnelHandle);
                        owner.currentTunnel = null;
                        owner.currentTunnelHandle = -1;
                        owner.currentConfig = null;
                        tunnel.onStateChange(State.DOWN);
                    }
                }
            }
            vpnService = vpnService.newIncompleteFuture();
            super.onDestroy();
        }

        @Override
        public int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
            vpnService.complete(this);
            if (intent == null || intent.getComponent() == null || !intent.getComponent().getPackageName().equals(getPackageName())) {
                Log.d(TAG, "Service started by Always-on VPN feature");
                if (alwaysOnCallback != null)
                    alwaysOnCallback.alwaysOnTriggered();
            }
            return super.onStartCommand(intent, flags, startId);
        }

        public void setOwner(final GoBackend owner) {
            this.owner = owner;
        }
    }
}
