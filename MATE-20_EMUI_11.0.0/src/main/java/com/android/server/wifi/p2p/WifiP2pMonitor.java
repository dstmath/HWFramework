package com.android.server.wifi.p2p;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pProvDiscEvent;
import android.net.wifi.p2p.nsd.WifiP2pServiceResponse;
import android.os.Handler;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WifiP2pMonitor implements IHwWifiP2pMonitorInner {
    public static final int AP_STA_CONNECTED_EVENT = 147498;
    public static final int AP_STA_DISCONNECTED_EVENT = 147497;
    private static final int BASE = 147456;
    public static final int HW_P2P_DEVICE_FOUND_EVENT = 147577;
    public static final int HW_P2P_DISCONNECT_ERRCODE_EVENT = 147578;
    public static final int HW_P2P_RECEVIE_INVITATION_EVENT = 147579;
    public static final int P2P_DEVICE_FOUND_EVENT = 147477;
    public static final int P2P_DEVICE_LOST_EVENT = 147478;
    public static final int P2P_FIND_STOPPED_EVENT = 147493;
    public static final int P2P_GC_INTERFACE_CREATED_EVENT = 147557;
    public static final int P2P_GO_INTERFACE_CREATED_EVENT = 147556;
    public static final int P2P_GO_NEGOTIATION_FAILURE_EVENT = 147482;
    public static final int P2P_GO_NEGOTIATION_REQUEST_EVENT = 147479;
    public static final int P2P_GO_NEGOTIATION_SUCCESS_EVENT = 147481;
    public static final int P2P_GROUP_FORMATION_FAILURE_EVENT = 147484;
    public static final int P2P_GROUP_FORMATION_SUCCESS_EVENT = 147483;
    public static final int P2P_GROUP_REMOVED_EVENT = 147486;
    public static final int P2P_GROUP_STARTED_EVENT = 147485;
    public static final int P2P_INTERFACE_ADDRESS_EVENT = 147499;
    public static final int P2P_INVITATION_RECEIVED_EVENT = 147487;
    public static final int P2P_INVITATION_RESULT_EVENT = 147488;
    public static final int P2P_PERSISTENT_NETWORK_REMOVED_UNEXPECTEDLY_EVENT = 147558;
    public static final int P2P_PROV_DISC_ENTER_PIN_EVENT = 147491;
    public static final int P2P_PROV_DISC_FAILURE_EVENT = 147495;
    public static final int P2P_PROV_DISC_PBC_REQ_EVENT = 147489;
    public static final int P2P_PROV_DISC_PBC_RSP_EVENT = 147490;
    public static final int P2P_PROV_DISC_SHOW_PIN_EVENT = 147492;
    public static final int P2P_REMOVE_AND_REFORM_GROUP_EVENT = 147496;
    public static final int P2P_SERV_DISC_RESP_EVENT = 147494;
    public static final int SUP_CONNECTION_EVENT = 147457;
    public static final int SUP_DISCONNECTION_EVENT = 147458;
    private static final String TAG = "WifiP2pMonitor";
    private boolean mConnected = false;
    private final Map<String, SparseArray<Set<Handler>>> mHandlerMap = new HashMap();
    private final IHwWifiP2pMonitorExt mHwWifiP2pMonitorExt;
    private final Map<String, Boolean> mMonitoringMap = new HashMap();
    private boolean mVerboseLoggingEnabled = false;
    private final WifiInjector mWifiInjector;

    public WifiP2pMonitor(WifiInjector wifiInjector) {
        this.mHwWifiP2pMonitorExt = HwWifiServiceFactory.getHwWifiP2pMonitorExt(this, wifiInjector);
        this.mWifiInjector = wifiInjector;
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    public synchronized void registerHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> ifaceHandlers = this.mHandlerMap.get(iface);
        if (ifaceHandlers == null) {
            ifaceHandlers = new SparseArray<>();
            this.mHandlerMap.put(iface, ifaceHandlers);
        }
        Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(what);
        if (ifaceWhatHandlers == null) {
            ifaceWhatHandlers = new ArraySet();
            ifaceHandlers.put(what, ifaceWhatHandlers);
        }
        ifaceWhatHandlers.add(handler);
    }

    private boolean isMonitoring(String iface) {
        Boolean val = this.mMonitoringMap.get(iface);
        if (val == null) {
            return false;
        }
        return val.booleanValue();
    }

    @VisibleForTesting
    public void setMonitoring(String iface, boolean enabled) {
        this.mMonitoringMap.put(iface, Boolean.valueOf(enabled));
    }

    private void setMonitoringNone() {
        for (String iface : this.mMonitoringMap.keySet()) {
            setMonitoring(iface, false);
        }
    }

    public synchronized void startMonitoring(String iface) {
        setMonitoring(iface, true);
        broadcastSupplicantConnectionEvent(iface);
    }

    public synchronized void stopMonitoring(String iface) {
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "stopMonitoring(" + iface + ")");
        }
        setMonitoring(iface, true);
        broadcastSupplicantDisconnectionEvent(iface);
        setMonitoring(iface, false);
    }

    public synchronized void stopAllMonitoring() {
        this.mConnected = false;
        setMonitoringNone();
    }

    private void sendMessage(String iface, int what) {
        sendMessage(iface, Message.obtain((Handler) null, what));
    }

    private void sendMessage(String iface, int what, Object obj) {
        sendMessage(iface, Message.obtain(null, what, obj));
    }

    private void sendMessage(String iface, int what, int arg1) {
        sendMessage(iface, Message.obtain(null, what, arg1, 0));
    }

    private void sendMessage(String iface, int what, int arg1, int arg2) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2));
    }

    private void sendMessage(String iface, int what, int arg1, int arg2, Object obj) {
        sendMessage(iface, Message.obtain(null, what, arg1, arg2, obj));
    }

    private void sendMessage(String iface, Message message) {
        SparseArray<Set<Handler>> ifaceHandlers = this.mHandlerMap.get(iface);
        if (iface == null || ifaceHandlers == null) {
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Sending to all monitors because there's no matching iface");
            }
            for (Map.Entry<String, SparseArray<Set<Handler>>> entry : this.mHandlerMap.entrySet()) {
                if (isMonitoring(entry.getKey())) {
                    for (Handler handler : entry.getValue().get(message.what)) {
                        if (handler != null) {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            }
        } else if (isMonitoring(iface)) {
            Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(message.what);
            if (ifaceWhatHandlers != null) {
                for (Handler handler2 : ifaceWhatHandlers) {
                    if (handler2 != null) {
                        sendMessage(handler2, Message.obtain(message));
                    }
                }
            }
        } else if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Dropping event because (" + iface + ") is stopped");
        }
        message.recycle();
    }

    private void sendMessage(Handler handler, Message message) {
        message.setTarget(handler);
        message.sendToTarget();
    }

    public void broadcastSupplicantConnectionEvent(String iface) {
        sendMessage(iface, 147457);
    }

    public void broadcastSupplicantDisconnectionEvent(String iface) {
        sendMessage(iface, 147458);
    }

    public void broadcastP2pDeviceFound(String iface, WifiP2pDevice device) {
        if (device != null) {
            sendMessage(iface, P2P_DEVICE_FOUND_EVENT, device);
        }
    }

    public void broadcastP2pDeviceLost(String iface, WifiP2pDevice device) {
        if (device != null) {
            sendMessage(iface, P2P_DEVICE_LOST_EVENT, device);
        }
    }

    public void broadcastP2pFindStopped(String iface) {
        sendMessage(iface, P2P_FIND_STOPPED_EVENT);
    }

    public void broadcastP2pGoNegotiationRequest(String iface, WifiP2pConfig config) {
        if (config != null) {
            sendMessage(iface, P2P_GO_NEGOTIATION_REQUEST_EVENT, config);
        }
    }

    public void broadcastP2pGoNegotiationSuccess(String iface) {
        sendMessage(iface, P2P_GO_NEGOTIATION_SUCCESS_EVENT);
    }

    public void broadcastP2pGoNegotiationFailure(String iface, WifiP2pServiceImpl.P2pStatus reason) {
        sendMessage(iface, P2P_GO_NEGOTIATION_FAILURE_EVENT, reason);
    }

    public void broadcastP2pGroupFormationSuccess(String iface) {
        sendMessage(iface, P2P_GROUP_FORMATION_SUCCESS_EVENT);
    }

    public void broadcastP2pGroupFormationFailure(String iface, String reason) {
        WifiP2pServiceImpl.P2pStatus err = WifiP2pServiceImpl.P2pStatus.UNKNOWN;
        if (reason.equals("FREQ_CONFLICT")) {
            err = WifiP2pServiceImpl.P2pStatus.NO_COMMON_CHANNEL;
        }
        sendMessage(iface, P2P_GROUP_FORMATION_FAILURE_EVENT, err);
    }

    public void broadcastP2pGroupStarted(String iface, WifiP2pGroup group) {
        if (group != null) {
            sendMessage(iface, P2P_GROUP_STARTED_EVENT, group);
        }
    }

    public void broadcastP2pGroupRemoved(String iface, WifiP2pGroup group) {
        if (group != null) {
            sendMessage(iface, P2P_GROUP_REMOVED_EVENT, group);
        }
    }

    public void broadcastP2pInvitationReceived(String iface, WifiP2pGroup group) {
        if (group != null) {
            sendMessage(iface, P2P_INVITATION_RECEIVED_EVENT, group);
        }
    }

    public void broadcastP2pInvitationResult(String iface, WifiP2pServiceImpl.P2pStatus result) {
        sendMessage(iface, P2P_INVITATION_RESULT_EVENT, result);
    }

    public void broadcastP2pProvisionDiscoveryPbcRequest(String iface, WifiP2pProvDiscEvent event) {
        if (event != null) {
            sendMessage(iface, P2P_PROV_DISC_PBC_REQ_EVENT, event);
        }
    }

    public void broadcastP2pProvisionDiscoveryPbcResponse(String iface, WifiP2pProvDiscEvent event) {
        if (event != null) {
            sendMessage(iface, P2P_PROV_DISC_PBC_RSP_EVENT, event);
        }
    }

    public void broadcastP2pProvisionDiscoveryEnterPin(String iface, WifiP2pProvDiscEvent event) {
        if (event != null) {
            sendMessage(iface, P2P_PROV_DISC_ENTER_PIN_EVENT, event);
        }
    }

    public void broadcastP2pProvisionDiscoveryShowPin(String iface, WifiP2pProvDiscEvent event) {
        if (event != null) {
            sendMessage(iface, P2P_PROV_DISC_SHOW_PIN_EVENT, event);
        }
    }

    public void broadcastP2pProvisionDiscoveryFailure(String iface) {
        sendMessage(iface, P2P_PROV_DISC_FAILURE_EVENT);
    }

    public void broadcastP2pServiceDiscoveryResponse(String iface, List<WifiP2pServiceResponse> services) {
        sendMessage(iface, P2P_SERV_DISC_RESP_EVENT, services);
    }

    public void broadcastP2pApStaConnected(String iface, WifiP2pDevice device) {
        sendMessage(iface, AP_STA_CONNECTED_EVENT, device);
    }

    public void broadcastP2pApStaDisconnected(String iface, WifiP2pDevice device) {
        sendMessage(iface, AP_STA_DISCONNECTED_EVENT, device);
    }

    public IHwWifiP2pMonitorExt getIHwWifiP2pMonitorExt() {
        return this.mHwWifiP2pMonitorExt;
    }

    public void broadcastP2pGroupRemoveAndReform(String iface) {
        sendMessage(iface, P2P_REMOVE_AND_REFORM_GROUP_EVENT);
    }

    public void broadcastP2pGoInterfaceCreated(String iface, String token) {
        sendMessage(iface, 147556, token);
    }

    public void broadcastP2pGcInterfaceCreated(String iface, String token) {
        sendMessage(iface, 147557, token);
    }

    public void broadcastP2pPersistentNetworkRemovedUnexpectedly(String iface, int networkId) {
        sendMessage(iface, P2P_PERSISTENT_NETWORK_REMOVED_UNEXPECTEDLY_EVENT, networkId);
    }

    @Override // com.android.server.wifi.p2p.IHwWifiP2pMonitorInner
    public void sendMessageEx(String iface, int what, Object obj) {
        sendMessage(iface, what, obj);
    }

    public void broadcastP2pInterfaceAddr(String iface, WifiP2pDevice device) {
        sendMessage(iface, 147499, device);
    }

    public void broadcastP2pDisconnectErrCode(String iface, String errCode) {
        sendMessage(iface, HW_P2P_DISCONNECT_ERRCODE_EVENT, errCode);
    }

    public void broadcastP2pRecvInvitation(String iface, String address) {
        sendMessage(iface, HW_P2P_RECEVIE_INVITATION_EVENT, address);
    }
}
