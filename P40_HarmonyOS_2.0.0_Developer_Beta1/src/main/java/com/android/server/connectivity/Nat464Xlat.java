package com.android.server.connectivity;

import android.net.IDnsResolver;
import android.net.INetd;
import android.net.InetAddresses;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.net.BaseNetworkObserver;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Objects;

public class Nat464Xlat extends BaseNetworkObserver {
    private static final String CLAT_PREFIX = "v4-";
    private static final NetworkInfo.State[] NETWORK_STATES = {NetworkInfo.State.CONNECTED, NetworkInfo.State.SUSPENDED};
    private static final int[] NETWORK_TYPES = {0, 1, 9};
    private static final String TAG = Nat464Xlat.class.getSimpleName();
    private String mBaseIface;
    private final IDnsResolver mDnsResolver;
    private Inet6Address mIPv6Address;
    private String mIface;
    private final INetworkManagementService mNMService;
    private IpPrefix mNat64Prefix;
    private final INetd mNetd;
    private final NetworkAgentInfo mNetwork;
    private State mState = State.IDLE;

    /* access modifiers changed from: private */
    public enum State {
        IDLE,
        DISCOVERING,
        STARTING,
        RUNNING
    }

    public Nat464Xlat(NetworkAgentInfo nai, INetd netd, IDnsResolver dnsResolver, INetworkManagementService nmService) {
        this.mDnsResolver = dnsResolver;
        this.mNetd = netd;
        this.mNMService = nmService;
        this.mNetwork = nai;
    }

    @VisibleForTesting
    protected static boolean requiresClat(NetworkAgentInfo nai) {
        int netType = nai.networkInfo.getType();
        boolean supported = ArrayUtils.contains(NETWORK_TYPES, nai.networkInfo.getType());
        boolean connected = ArrayUtils.contains(NETWORK_STATES, nai.networkInfo.getState());
        LinkProperties lp = nai.linkProperties;
        boolean isIpv6OnlyNetwork = lp != null && lp.hasGlobalIpv6Address() && !lp.hasIpv4Address();
        boolean skip464xlat = nai.netMisc() != null && nai.netMisc().skip464xlat;
        boolean doXlat = SystemProperties.getBoolean("persist.net.doxlat", true);
        if (!doXlat) {
            Slog.i(TAG, "Android Xlat is disabled");
        }
        if (!SystemProperties.getBoolean("gsm.net.doxlat", true)) {
            Slog.i(TAG, "Android XCAP Xlat is disabled");
            doXlat = false;
        }
        if (!supported || !connected || !isIpv6OnlyNetwork || skip464xlat) {
            return false;
        }
        return netType != 0 || doXlat;
    }

    @VisibleForTesting
    protected static boolean shouldStartClat(NetworkAgentInfo nai) {
        LinkProperties lp = nai.linkProperties;
        return (!requiresClat(nai) || lp == null || lp.getNat64Prefix() == null) ? false : true;
    }

    public boolean isPrefixDiscoveryStarted() {
        return this.mState == State.DISCOVERING || isStarted();
    }

    public boolean isStarted() {
        return this.mState == State.STARTING || this.mState == State.RUNNING;
    }

    public boolean isStarting() {
        return this.mState == State.STARTING;
    }

    public boolean isRunning() {
        return this.mState == State.RUNNING;
    }

    private void enterStartingState(String baseIface) {
        try {
            this.mNMService.registerObserver(this);
            String addrStr = null;
            try {
                addrStr = this.mNetd.clatdStart(baseIface, this.mNat64Prefix.toString());
            } catch (RemoteException | ServiceSpecificException e) {
                String str = TAG;
                Slog.e(str, "Error starting clatd on " + baseIface + ": " + e);
            }
            this.mIface = CLAT_PREFIX + baseIface;
            this.mBaseIface = baseIface;
            this.mState = State.STARTING;
            try {
                this.mIPv6Address = (Inet6Address) InetAddresses.parseNumericAddress(addrStr);
            } catch (ClassCastException | IllegalArgumentException | NullPointerException e2) {
                String str2 = TAG;
                Slog.e(str2, "Invalid IPv6 address" + e2.getClass().getSimpleName());
            }
        } catch (RemoteException e3) {
            String str3 = TAG;
            Slog.e(str3, "Can't register interface observer for clat on " + this.mNetwork.name());
        }
    }

    private void enterRunningState() {
        this.mState = State.RUNNING;
    }

    private void leaveStartedState() {
        try {
            this.mNMService.unregisterObserver(this);
        } catch (RemoteException | IllegalStateException e) {
            String str = TAG;
            Slog.e(str, "Error unregistering clatd observer on " + this.mBaseIface + ": " + e);
        }
        this.mIface = null;
        this.mBaseIface = null;
        this.mState = State.IDLE;
        if (requiresClat(this.mNetwork)) {
            this.mState = State.DISCOVERING;
            return;
        }
        stopPrefixDiscovery();
        this.mState = State.IDLE;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void start() {
        if (isStarted()) {
            Slog.e(TAG, "startClat: already started");
        } else if (this.mNetwork.linkProperties == null) {
            Slog.e(TAG, "startClat: Can't start clat with null LinkProperties");
        } else {
            String baseIface = this.mNetwork.linkProperties.getInterfaceName();
            if (baseIface == null) {
                Slog.e(TAG, "startClat: Can't start clat on null interface");
                return;
            }
            String str = TAG;
            Slog.i(str, "Starting clatd on " + baseIface);
            enterStartingState(baseIface);
        }
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.String: [D('e' java.lang.Exception), D('iface' java.lang.String)] */
    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void stop() {
        if (!isStarted()) {
            Slog.e(TAG, "stopClat: already stopped");
            return;
        }
        String str = TAG;
        Slog.i(str, "Stopping clatd on " + this.mBaseIface);
        try {
            this.mNetd.clatdStop(this.mBaseIface);
        } catch (RemoteException | ServiceSpecificException e) {
            String str2 = TAG;
            Slog.e(str2, "Error stopping clatd on " + this.mBaseIface + ": " + e);
        }
        String iface = this.mIface;
        boolean wasRunning = isRunning();
        leaveStartedState();
        if (wasRunning) {
            LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
            lp.removeStackedLink(iface);
            this.mNetwork.connService().handleUpdateLinkProperties(this.mNetwork, lp);
        }
    }

    private void startPrefixDiscovery() {
        try {
            this.mDnsResolver.startPrefix64Discovery(getNetId());
            this.mState = State.DISCOVERING;
        } catch (RemoteException | ServiceSpecificException e) {
            String str = TAG;
            Slog.e(str, "Error starting prefix discovery on netId " + getNetId() + ": " + e);
        }
    }

    private void stopPrefixDiscovery() {
        try {
            this.mDnsResolver.stopPrefix64Discovery(getNetId());
        } catch (RemoteException | ServiceSpecificException e) {
            String str = TAG;
            Slog.e(str, "Error stopping prefix discovery on netId " + getNetId() + ": " + e);
        }
    }

    public void update() {
        if (requiresClat(this.mNetwork)) {
            if (!isPrefixDiscoveryStarted()) {
                startPrefixDiscovery();
            } else if (shouldStartClat(this.mNetwork)) {
                start();
            } else {
                stop();
            }
        } else if (isStarted()) {
            stop();
        } else if (isPrefixDiscoveryStarted()) {
            leaveStartedState();
        }
    }

    public void setNat64Prefix(IpPrefix nat64Prefix) {
        this.mNat64Prefix = nat64Prefix;
    }

    public void fixupLinkProperties(LinkProperties oldLp, LinkProperties lp) {
        if (lp != null) {
            lp.setNat64Prefix(this.mNat64Prefix);
        }
        if (isRunning() && lp != null && !lp.getAllInterfaceNames().contains(this.mIface)) {
            String str = TAG;
            Slog.d(str, "clatd running, updating NAI for " + this.mIface);
            for (LinkProperties stacked : oldLp.getStackedLinks()) {
                if (Objects.equals(this.mIface, stacked.getInterfaceName())) {
                    lp.addStackedLink(stacked);
                    return;
                }
            }
        }
    }

    private LinkProperties makeLinkProperties(LinkAddress clatAddress) {
        LinkProperties stacked = new LinkProperties();
        stacked.setInterfaceName(this.mIface);
        stacked.addRoute(new RouteInfo(new LinkAddress(Inet4Address.ANY, 0), clatAddress.getAddress(), this.mIface));
        stacked.addLinkAddress(clatAddress);
        return stacked;
    }

    private LinkAddress getLinkAddress(String iface) {
        try {
            return this.mNMService.getInterfaceConfig(iface).getLinkAddress();
        } catch (RemoteException | IllegalStateException e) {
            String str = TAG;
            Slog.e(str, "Error getting link properties: " + e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleInterfaceLinkStateChanged */
    public void lambda$interfaceLinkStateChanged$0$Nat464Xlat(String iface, boolean up) {
        if (isStarting() && up && Objects.equals(this.mIface, iface)) {
            LinkAddress clatAddress = getLinkAddress(iface);
            if (clatAddress == null) {
                String str = TAG;
                Slog.e(str, "clatAddress was null for stacked iface " + iface);
                return;
            }
            String str2 = TAG;
            String str3 = this.mIface;
            Slog.i(str2, String.format("interface %s is up, adding stacked link %s on top of %s", str3, str3, this.mBaseIface));
            enterRunningState();
            LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
            lp.addStackedLink(makeLinkProperties(clatAddress));
            this.mNetwork.connService().handleUpdateLinkProperties(this.mNetwork, lp);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: handleInterfaceRemoved */
    public void lambda$interfaceRemoved$1$Nat464Xlat(String iface) {
        if (Objects.equals(this.mIface, iface) && isRunning()) {
            String str = TAG;
            Slog.i(str, "interface " + iface + " removed");
            stop();
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        this.mNetwork.handler().post(new Runnable(iface, up) {
            /* class com.android.server.connectivity.$$Lambda$Nat464Xlat$40jKHQd7R0zgcegyEyc9zPHKXVA */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Nat464Xlat.this.lambda$interfaceLinkStateChanged$0$Nat464Xlat(this.f$1, this.f$2);
            }
        });
    }

    public void interfaceRemoved(String iface) {
        this.mNetwork.handler().post(new Runnable(iface) {
            /* class com.android.server.connectivity.$$Lambda$Nat464Xlat$PACHOP9HoYvr_jzHtIwFDy31Ud4 */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Nat464Xlat.this.lambda$interfaceRemoved$1$Nat464Xlat(this.f$1);
            }
        });
    }

    public String toString() {
        return "mBaseIface: " + this.mBaseIface + ", mIface: " + this.mIface + ", mState: " + this.mState;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getNetId() {
        return this.mNetwork.network.netId;
    }
}
