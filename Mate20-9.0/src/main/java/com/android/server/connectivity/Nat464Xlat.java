package com.android.server.connectivity;

import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.RouteInfo;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.net.BaseNetworkObserver;
import java.net.Inet4Address;
import java.util.Objects;

public class Nat464Xlat extends BaseNetworkObserver {
    private static final String CLAT_PREFIX = "v4-";
    private static final NetworkInfo.State[] NETWORK_STATES = {NetworkInfo.State.CONNECTED, NetworkInfo.State.SUSPENDED};
    private static final int[] NETWORK_TYPES = {0, 1, 9};
    private static final String TAG = Nat464Xlat.class.getSimpleName();
    private String mBaseIface;
    private String mIface;
    private final INetworkManagementService mNMService;
    private final NetworkAgentInfo mNetwork;
    private State mState = State.IDLE;

    private enum State {
        IDLE,
        STARTING,
        RUNNING,
        STOPPING
    }

    public Nat464Xlat(INetworkManagementService nmService, NetworkAgentInfo nai) {
        this.mNMService = nmService;
        this.mNetwork = nai;
    }

    public static boolean requiresClat(NetworkAgentInfo nai) {
        int netType = nai.networkInfo.getType();
        boolean supported = ArrayUtils.contains(NETWORK_TYPES, nai.networkInfo.getType());
        boolean connected = nai.networkInfo.isConnected();
        boolean hasIPv4Address = nai.linkProperties != null && nai.linkProperties.hasIPv4Address();
        boolean doXlat = SystemProperties.getBoolean("persist.net.doxlat", true);
        if (!doXlat) {
            Slog.i(TAG, "Android Xlat is disabled");
        }
        if (!SystemProperties.getBoolean("gsm.net.doxlat", true)) {
            Slog.i(TAG, "Android XCAP Xlat is disabled");
            doXlat = false;
        }
        if (!supported || !connected || hasIPv4Address) {
            return false;
        }
        if (netType != 0 || doXlat) {
            return true;
        }
        return false;
    }

    public boolean isStarted() {
        return this.mState != State.IDLE;
    }

    public boolean isStarting() {
        return this.mState == State.STARTING;
    }

    public boolean isRunning() {
        return this.mState == State.RUNNING;
    }

    public boolean isStopping() {
        return this.mState == State.STOPPING;
    }

    private void enterStartingState(String baseIface) {
        try {
            this.mNMService.registerObserver(this);
            try {
                this.mNMService.startClatd(baseIface);
            } catch (RemoteException | IllegalStateException e) {
                String str = TAG;
                Slog.e(str, "Error starting clatd on " + baseIface, e);
            }
            this.mIface = CLAT_PREFIX + baseIface;
            this.mBaseIface = baseIface;
            this.mState = State.STARTING;
        } catch (RemoteException e2) {
            String str2 = TAG;
            Slog.e(str2, "startClat: Can't register interface observer for clat on " + this.mNetwork.name());
        }
    }

    private void enterRunningState() {
        this.mState = State.RUNNING;
    }

    private void enterStoppingState() {
        try {
            this.mNMService.stopClatd(this.mBaseIface);
        } catch (RemoteException | IllegalStateException e) {
            String str = TAG;
            Slog.e(str, "Error stopping clatd on " + this.mBaseIface, e);
        }
        this.mState = State.STOPPING;
    }

    private void enterIdleState() {
        try {
            this.mNMService.unregisterObserver(this);
        } catch (RemoteException | IllegalStateException e) {
            String str = TAG;
            Slog.e(str, "Error unregistering clatd observer on " + this.mBaseIface, e);
        }
        this.mIface = null;
        this.mBaseIface = null;
        this.mState = State.IDLE;
    }

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

    public void stop() {
        if (isStarted()) {
            String str = TAG;
            Slog.i(str, "Stopping clatd on " + this.mBaseIface);
            boolean wasStarting = isStarting();
            enterStoppingState();
            if (wasStarting) {
                enterIdleState();
            }
        }
    }

    public void fixupLinkProperties(LinkProperties oldLp, LinkProperties lp) {
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
    public void handleInterfaceLinkStateChanged(String iface, boolean up) {
        if (isStarting() && up && Objects.equals(this.mIface, iface)) {
            LinkAddress clatAddress = getLinkAddress(iface);
            if (clatAddress == null) {
                String str = TAG;
                Slog.e(str, "clatAddress was null for stacked iface " + iface);
                return;
            }
            Slog.i(TAG, String.format("interface %s is up, adding stacked link %s on top of %s", new Object[]{this.mIface, this.mIface, this.mBaseIface}));
            enterRunningState();
            LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
            lp.addStackedLink(makeLinkProperties(clatAddress));
            this.mNetwork.connService().handleUpdateLinkProperties(this.mNetwork, lp);
        }
    }

    /* access modifiers changed from: private */
    public void handleInterfaceRemoved(String iface) {
        if (Objects.equals(this.mIface, iface)) {
            if (isRunning() || isStopping()) {
                String str = TAG;
                Slog.i(str, "interface " + iface + " removed");
                if (!isStopping()) {
                    enterStoppingState();
                }
                enterIdleState();
                LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
                lp.removeStackedLink(iface);
                this.mNetwork.connService().handleUpdateLinkProperties(this.mNetwork, lp);
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        this.mNetwork.handler().post(new Runnable(iface, up) {
            private final /* synthetic */ String f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                Nat464Xlat.this.handleInterfaceLinkStateChanged(this.f$1, this.f$2);
            }
        });
    }

    public void interfaceRemoved(String iface) {
        this.mNetwork.handler().post(new Runnable(iface) {
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                Nat464Xlat.this.handleInterfaceRemoved(this.f$1);
            }
        });
    }

    public String toString() {
        return "mBaseIface: " + this.mBaseIface + ", mIface: " + this.mIface + ", mState: " + this.mState;
    }
}
