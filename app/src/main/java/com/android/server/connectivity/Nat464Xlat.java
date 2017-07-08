package com.android.server.connectivity;

import android.content.Context;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.ArrayUtils;
import com.android.server.net.BaseNetworkObserver;
import java.net.Inet4Address;

public class Nat464Xlat extends BaseNetworkObserver {
    private static final String CLAT_PREFIX = "v4-";
    private static final int[] NETWORK_TYPES = null;
    private static final String TAG = "Nat464Xlat";
    private String mBaseIface;
    private final Handler mHandler;
    private String mIface;
    private boolean mIsRunning;
    private final INetworkManagementService mNMService;
    private final NetworkAgentInfo mNetwork;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.connectivity.Nat464Xlat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.connectivity.Nat464Xlat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.connectivity.Nat464Xlat.<clinit>():void");
    }

    public Nat464Xlat(Context context, INetworkManagementService nmService, Handler handler, NetworkAgentInfo nai) {
        this.mNMService = nmService;
        this.mHandler = handler;
        this.mNetwork = nai;
    }

    public static boolean requiresClat(NetworkAgentInfo nai) {
        int netType = nai.networkInfo.getType();
        boolean connected = nai.networkInfo.isConnected();
        boolean hasIPv4Address = nai.linkProperties != null ? nai.linkProperties.hasIPv4Address() : false;
        boolean doXlat = SystemProperties.getBoolean("persist.net.doxlat", true);
        if (!doXlat) {
            Slog.i(TAG, "Android Xlat is disabled");
        }
        if (!connected || hasIPv4Address || !ArrayUtils.contains(NETWORK_TYPES, netType)) {
            return false;
        }
        if (netType != 0) {
            return true;
        }
        return doXlat;
    }

    public boolean isStarted() {
        return this.mIface != null;
    }

    private void clear() {
        this.mIface = null;
        this.mBaseIface = null;
        this.mIsRunning = false;
    }

    public void start() {
        if (isStarted()) {
            Slog.e(TAG, "startClat: already started");
        } else if (this.mNetwork.linkProperties == null) {
            Slog.e(TAG, "startClat: Can't start clat with null LinkProperties");
        } else {
            try {
                this.mNMService.registerObserver(this);
                this.mBaseIface = this.mNetwork.linkProperties.getInterfaceName();
                if (this.mBaseIface == null) {
                    Slog.e(TAG, "startClat: Can't start clat on null interface");
                    return;
                }
                this.mIface = CLAT_PREFIX + this.mBaseIface;
                Slog.i(TAG, "Starting clatd on " + this.mBaseIface);
                try {
                    this.mNMService.startClatd(this.mBaseIface);
                } catch (Exception e) {
                    Slog.e(TAG, "Error starting clatd: " + e);
                }
            } catch (RemoteException e2) {
                Slog.e(TAG, "startClat: Can't register interface observer for clat on " + this.mNetwork);
            }
        }
    }

    public void stop() {
        if (isStarted()) {
            Slog.i(TAG, "Stopping clatd");
            try {
                this.mNMService.stopClatd(this.mBaseIface);
                return;
            } catch (Exception e) {
                Slog.e(TAG, "Error stopping clatd: " + e);
                return;
            }
        }
        Slog.e(TAG, "clatd: already stopped");
    }

    private void updateConnectivityService(LinkProperties lp) {
        Message msg = this.mHandler.obtainMessage(528387, lp);
        msg.replyTo = this.mNetwork.messenger;
        Slog.i(TAG, "sending message to ConnectivityService: " + msg);
        msg.sendToTarget();
    }

    public void fixupLinkProperties(LinkProperties oldLp) {
        if (this.mNetwork.clatd != null && this.mIsRunning && this.mNetwork.linkProperties != null && !this.mNetwork.linkProperties.getAllInterfaceNames().contains(this.mIface)) {
            Slog.d(TAG, "clatd running, updating NAI for " + this.mIface);
            for (LinkProperties stacked : oldLp.getStackedLinks()) {
                if (this.mIface.equals(stacked.getInterfaceName())) {
                    this.mNetwork.linkProperties.addStackedLink(stacked);
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
        } catch (Exception e) {
            Slog.e(TAG, "Error getting link properties: " + e);
            return null;
        }
    }

    private void maybeSetIpv6NdOffload(String iface, boolean on) {
        if (this.mNetwork.networkInfo.getType() == 1) {
            try {
                Slog.d(TAG, (on ? "En" : "Dis") + "abling ND offload on " + iface);
                this.mNMService.setInterfaceIpv6NdOffload(iface, on);
            } catch (Exception e) {
                Slog.w(TAG, "Changing IPv6 ND offload on " + iface + "failed: " + e);
            }
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean up) {
        if (isStarted() && up && this.mIface.equals(iface)) {
            Slog.i(TAG, "interface " + iface + " is up, mIsRunning " + this.mIsRunning + "->true");
            if (!this.mIsRunning) {
                LinkAddress clatAddress = getLinkAddress(iface);
                if (clatAddress != null) {
                    this.mIsRunning = true;
                    maybeSetIpv6NdOffload(this.mBaseIface, false);
                    LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
                    lp.addStackedLink(makeLinkProperties(clatAddress));
                    Slog.i(TAG, "Adding stacked link " + this.mIface + " on top of " + this.mBaseIface);
                    updateConnectivityService(lp);
                }
            }
        }
    }

    public void interfaceRemoved(String iface) {
        if (isStarted() && this.mIface.equals(iface)) {
            Slog.i(TAG, "interface " + iface + " removed, mIsRunning " + this.mIsRunning + "->false");
            if (this.mIsRunning) {
                try {
                    this.mNMService.unregisterObserver(this);
                    this.mNMService.stopClatd(this.mBaseIface);
                } catch (RemoteException e) {
                }
                maybeSetIpv6NdOffload(this.mBaseIface, true);
                LinkProperties lp = new LinkProperties(this.mNetwork.linkProperties);
                lp.removeStackedLink(this.mIface);
                clear();
                updateConnectivityService(lp);
            }
        }
    }
}
