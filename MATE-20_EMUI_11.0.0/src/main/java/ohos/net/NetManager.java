package ohos.net;

import android.net.NetworkUtils;
import android.net.Proxy;
import android.net.ProxyInfo;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.NetSpecifier;
import ohos.rpc.RemoteException;

public final class NetManager {
    public static final int BACKGROUND_POLICY_ALLOWLISTED = 2;
    public static final int BACKGROUND_POLICY_DISABLE = 1;
    public static final int BACKGROUND_POLICY_ENABLED = 3;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109360, "NETMGRKIT");
    private static final int NETID_UNSET = 0;
    private static final HashMap<NetSpecifier, NetStatusCallback> NET_CALLBACKS = new HashMap<>();
    private static final NetSpecifier NET_UNREGISTERED = new NetSpecifier.Builder().clearCapabilities().build();
    private static CallbackHandler sCallbackHandler;
    private final NetManagerProxy mNetManagerProxy = NetManagerProxy.getInstance();

    public int bindRouterToNetworkSlice(NetHandle netHandle) {
        return 0;
    }

    public List<RouteSelectionDescriptor> getActiveNetworkSliceInfoList(TrafficDescriptor trafficDescriptor) {
        return null;
    }

    public int getBoosterPara(int i) {
        return 0;
    }

    public void registerBoosterCallBack(BoosterCallback boosterCallback) {
    }

    public void releaseNetworkSlice(NetStatusCallback netStatusCallback) {
    }

    public void reportBoosterPara(int i) {
    }

    public int requestNetworkSlice(TrafficDescriptor trafficDescriptor, NetStatusCallback netStatusCallback) {
        return 0;
    }

    public void unRegisterCallBack(BoosterCallback boosterCallback) {
    }

    public int unbindRouterToNetworkSlice(NetHandle netHandle) {
        return 0;
    }

    /* access modifiers changed from: private */
    public static class CallbackHandler extends EventHandler {
        public static final int BASE = 524288;
        public static final int CALLBACK_AVAILABLE = 524290;
        public static final int CALLBACK_BLK_CHANGED = 524299;
        public static final int CALLBACK_CAP_CHANGED = 524294;
        public static final int CALLBACK_IP_CHANGED = 524295;
        public static final int CALLBACK_LOSING = 524291;
        public static final int CALLBACK_LOST = 524292;
        public static final int CALLBACK_PRECHECK = 524289;
        public static final int CALLBACK_UNAVAIL = 524293;

        public CallbackHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            NetStatusCallback netStatusCallback;
            if (innerEvent.object instanceof EventObject) {
                EventObject eventObject = (EventObject) innerEvent.object;
                NetSpecifier netSpecifier = eventObject.netSpecifier;
                NetHandle netHandle = eventObject.netHandle;
                boolean z = false;
                HiLog.info(NetManager.LABEL, "processEvent: %{public}d, %{public}d", Integer.valueOf(innerEvent.eventId), Integer.valueOf(netSpecifier.requestId));
                synchronized (NetManager.NET_CALLBACKS) {
                    netStatusCallback = (NetStatusCallback) NetManager.NET_CALLBACKS.get(netSpecifier);
                }
                if (netStatusCallback != null) {
                    HiLog.warn(NetManager.LABEL, "processEvent: %{public}d", Integer.valueOf(innerEvent.eventId));
                    int i = innerEvent.eventId;
                    if (i != 524299) {
                        switch (i) {
                            case CALLBACK_AVAILABLE /* 524290 */:
                                netStatusCallback.onAvailable(netHandle, eventObject.netCapabilities, eventObject.connectionProperties);
                                return;
                            case CALLBACK_LOSING /* 524291 */:
                                netStatusCallback.onLosing(netHandle, innerEvent.param);
                                return;
                            case CALLBACK_LOST /* 524292 */:
                                netStatusCallback.onLost(netHandle);
                                return;
                            case CALLBACK_UNAVAIL /* 524293 */:
                                netStatusCallback.onUnavailable();
                                return;
                            case CALLBACK_CAP_CHANGED /* 524294 */:
                                netStatusCallback.onCapabilitiesChanged(netHandle, eventObject.netCapabilities);
                                return;
                            default:
                                return;
                        }
                    } else {
                        if (innerEvent.param != 0) {
                            z = true;
                        }
                        netStatusCallback.onBlockedStatusChanged(netHandle, z);
                    }
                }
            }
        }
    }

    private NetManager(Context context) {
    }

    public static NetManager getInstance(Context context) {
        return new NetManager(context);
    }

    public boolean enableAirplaneMode(boolean z) {
        try {
            return this.mNetManagerProxy.enableAirplaneMode(z);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to enableAirplaneMode: ", new Object[0]);
            return false;
        }
    }

    public boolean setupSpecificNet(NetSpecifier netSpecifier, NetStatusCallback netStatusCallback) {
        if (netStatusCallback == null) {
            return false;
        }
        try {
            NetRemoteEvent netRemoteEvent = new NetRemoteEvent("android.os.IMessenger");
            netRemoteEvent.setEventHandler(getDefaultHandler());
            synchronized (NET_CALLBACKS) {
                netStatusCallback.networkRequest = this.mNetManagerProxy.setupSpecificNet(netSpecifier, netRemoteEvent);
                if (netStatusCallback.networkRequest == null) {
                    return false;
                }
                HiLog.warn(LABEL, "requestId: %{public}d", Integer.valueOf(netStatusCallback.networkRequest.requestId));
                NET_CALLBACKS.put(netStatusCallback.networkRequest, netStatusCallback);
                return true;
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to setupSpecificNet: ", new Object[0]);
            return false;
        }
    }

    public boolean addNetStatusCallback(NetSpecifier netSpecifier, NetStatusCallback netStatusCallback) {
        if (netStatusCallback == null) {
            return false;
        }
        try {
            NetRemoteEvent netRemoteEvent = new NetRemoteEvent("android.os.IMessenger");
            netRemoteEvent.setEventHandler(getDefaultHandler());
            netStatusCallback.networkRequest = this.mNetManagerProxy.addNetStatusCallback(netSpecifier, netRemoteEvent);
            if (netStatusCallback.networkRequest != null) {
                HiLog.warn(LABEL, "requestId: %{public}d", Integer.valueOf(netStatusCallback.networkRequest.requestId));
                NET_CALLBACKS.put(netStatusCallback.networkRequest, netStatusCallback);
            }
            if (netStatusCallback.networkRequest == null) {
                return false;
            }
            return true;
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to addNetStatusCallback: ", new Object[0]);
            return false;
        }
    }

    public boolean removeNetStatusCallback(NetStatusCallback netStatusCallback) {
        if (netStatusCallback == null) {
            return true;
        }
        ArrayList<NetSpecifier> arrayList = new ArrayList();
        synchronized (NET_CALLBACKS) {
            if (netStatusCallback.networkRequest == null) {
                netStatusCallback.networkRequest = NET_UNREGISTERED;
                return true;
            } else if (netStatusCallback.networkRequest == NET_UNREGISTERED) {
                return true;
            } else {
                for (Map.Entry<NetSpecifier, NetStatusCallback> entry : NET_CALLBACKS.entrySet()) {
                    if (entry.getValue() == netStatusCallback) {
                        arrayList.add(entry.getKey());
                    }
                }
                for (NetSpecifier netSpecifier : arrayList) {
                    try {
                        this.mNetManagerProxy.releaseNetworkRequest(netSpecifier);
                    } catch (RemoteException unused) {
                        HiLog.warn(LABEL, "Failed to removeNetStatusCallback: ", new Object[0]);
                    }
                    NET_CALLBACKS.remove(netSpecifier);
                }
                netStatusCallback.networkRequest = NET_UNREGISTERED;
                return true;
            }
        }
    }

    public boolean addDefaultNetStatusCallback(NetStatusCallback netStatusCallback) {
        if (netStatusCallback == null) {
            return false;
        }
        try {
            NetRemoteEvent netRemoteEvent = new NetRemoteEvent("android.os.IMessenger");
            netRemoteEvent.setEventHandler(getDefaultHandler());
            NetSpecifier addDefaultNetStatusCallback = this.mNetManagerProxy.addDefaultNetStatusCallback(netRemoteEvent);
            if (addDefaultNetStatusCallback != null) {
                HiLog.warn(LABEL, "requestId: %{public}d", Integer.valueOf(addDefaultNetStatusCallback.requestId));
                NET_CALLBACKS.put(addDefaultNetStatusCallback, netStatusCallback);
            }
            if (addDefaultNetStatusCallback == null) {
                return false;
            }
            return true;
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to addDefaultNetStatusCallback: ", new Object[0]);
            return false;
        }
    }

    public NetHandle getDefaultNet() {
        try {
            return this.mNetManagerProxy.getDefaultNet();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getDefaultNet()", new Object[0]);
            return null;
        }
    }

    public NetHandle[] getAllNets() {
        try {
            return this.mNetManagerProxy.getAllNets();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getAllNets()", new Object[0]);
            return null;
        }
    }

    public boolean hasDefaultNet() {
        try {
            return this.mNetManagerProxy.hasDefaultNet();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to hasDefaultNet()", new Object[0]);
            return false;
        }
    }

    public NetCapabilities getNetCapabilities(NetHandle netHandle) {
        try {
            return this.mNetManagerProxy.getNetCapabilities(netHandle);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getNetCapabilities()", new Object[0]);
            return null;
        }
    }

    public HttpProxy getDefaultHttpProxy() {
        return getHttpProxyForNet(getAppNet());
    }

    public HttpProxy getHttpProxyForNet(NetHandle netHandle) {
        try {
            return this.mNetManagerProxy.getHttpProxyForNet(netHandle);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getHttpProxyForNet: ", new Object[0]);
            return null;
        }
    }

    public boolean setAppNet(NetHandle netHandle) {
        int i;
        HiLog.warn(LABEL, "setAppNet begin", new Object[0]);
        if (netHandle == null) {
            i = 0;
        } else {
            i = netHandle.netId;
        }
        if (i == NetworkUtils.getBoundNetworkForProcess()) {
            HiLog.warn(LABEL, "setAppNet netId: %{public}d", Integer.valueOf(i));
            return true;
        }
        try {
            HttpProxy httpProxyForNet = this.mNetManagerProxy.getHttpProxyForNet(new NetHandle(i));
            if (httpProxyForNet == null) {
                return false;
            }
            ProxyInfo proxyInfo = new ProxyInfo(httpProxyForNet.host, httpProxyForNet.port, httpProxyForNet.exclusionList);
            if (!NetworkUtils.bindProcessToNetwork(i)) {
                return false;
            }
            Proxy.setHttpProxySystemProperty(proxyInfo);
            InetAddress.clearDnsCache();
            return true;
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getHttpProxyForNet: RemoteException", new Object[0]);
            return false;
        } catch (SecurityException unused2) {
            HiLog.warn(LABEL, "Failed to getHttpProxyForNet: SecurityException", new Object[0]);
            return false;
        }
    }

    public NetHandle getAppNet() {
        int boundNetworkForProcess = NetworkUtils.getBoundNetworkForProcess();
        if (boundNetworkForProcess == 0) {
            HiLog.warn(LABEL, "getAppNet = 0 netId: %{public}d", Integer.valueOf(boundNetworkForProcess));
            return null;
        }
        HiLog.warn(LABEL, "getAppNet netId: %{public}d", Integer.valueOf(boundNetworkForProcess));
        return new NetHandle(boundNetworkForProcess);
    }

    public boolean startLegacyVpn(VpnProfile vpnProfile) {
        try {
            return this.mNetManagerProxy.startLegacyVpn(vpnProfile);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to startLegacyVpn: ", new Object[0]);
            return false;
        }
    }

    public boolean isDefaultNetMetered() {
        try {
            return this.mNetManagerProxy.isDefaultNetMetered();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to isDefaultNetMetered.", new Object[0]);
            return false;
        }
    }

    public ConnectionProperties getConnectionProperties(NetHandle netHandle) {
        try {
            return this.mNetManagerProxy.getConnectionProperties(netHandle);
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getConnectionProperties.", new Object[0]);
            return null;
        }
    }

    public int getBackgroundPolicy() {
        try {
            return this.mNetManagerProxy.getBackgroundPolicy();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "Failed to getBackgroundPolicy.", new Object[0]);
            return 0;
        }
    }

    private static CallbackHandler getDefaultHandler() {
        if (sCallbackHandler == null) {
            sCallbackHandler = new CallbackHandler(EventRunner.create(true));
        }
        return sCallbackHandler;
    }
}
