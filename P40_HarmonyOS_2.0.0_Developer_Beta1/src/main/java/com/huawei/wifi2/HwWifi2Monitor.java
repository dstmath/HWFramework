package com.huawei.wifi2;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import android.os.Handler;
import android.os.Message;
import android.util.ArraySet;
import android.util.SparseArray;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HwWifi2Monitor {
    public static final int ANT_CORE_ROB_EVNET = 147757;
    public static final int ASSOCIATION_REJECTION_EVENT = 147499;
    public static final int AUTHENTICATION_FAILURE_EVENT = 147463;
    public static final int BASE = 147456;
    public static final int EAP_ERRORCODE_REPORT_EVENT = 147956;
    public static final int NETWORK_CONNECTION_EVENT = 147459;
    public static final int NETWORK_DISCONNECTION_EVENT = 147460;
    public static final int SLAVE_WIFI_SUP_CONNECTION_EVENT = 147457;
    public static final int SLAVE_WIFI_SUP_DISCONNECTION_EVENT = 147458;
    public static final int SUPPLICANT_STATE_CHANGE_EVENT = 147462;
    private static final String TAG = "HwWifi2Monitor";
    public static final int VOWIFI_DETECT_IRQ_STR_EVENT = 147520;
    public static final int WAPI_AUTHENTICATION_FAILURE_EVENT = 147474;
    public static final int WAPI_CERTIFICATION_FAILURE_EVENT = 147475;
    public static final int WPA3_CONNECT_FAIL_EVENT = 147666;
    public static final int WPS_START_OKC_EVENT = 147656;
    private final Map<String, SparseArray<Set<Handler>>> mHandlerMap = new HashMap();
    private boolean mIsConnected = false;
    private final Map<String, Boolean> mMonitoringMap = new HashMap();

    public HwWifi2Monitor() {
        HwHiLog.i(TAG, false, "HwWifi2Monitor constructor", new Object[0]);
    }

    public synchronized void startMonitoring(String iface) {
        HwHiLog.i(TAG, false, "startMonitoring(%{public}s)", new Object[]{iface});
        setMonitoring(iface, true);
        broadcastSupplicantConnectionEvent(iface);
    }

    public synchronized void stopMonitoring(String iface) {
        HwHiLog.i(TAG, false, "stopMonitoring(%{public}s)", new Object[]{iface});
        setMonitoring(iface, true);
        broadcastSupplicantDisconnectionEvent(iface);
        setMonitoring(iface, false);
    }

    public synchronized void stopAllMonitoring() {
        this.mIsConnected = false;
        setMonitoringNone();
    }

    public void broadcastSupplicantConnectionEvent(String iface) {
        sendMessage(iface, Message.obtain((Handler) null, (int) SLAVE_WIFI_SUP_CONNECTION_EVENT));
    }

    public void broadcastSupplicantDisconnectionEvent(String iface) {
        HwHiLog.i(TAG, false, "broadcastSupplicantDisconnectionEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain((Handler) null, (int) SLAVE_WIFI_SUP_DISCONNECTION_EVENT));
    }

    public void broadcastNetworkConnectionEvent(String iface, int networkId, String bssid) {
        HwHiLog.i(TAG, false, "broadcastNetworkConnectionEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, NETWORK_CONNECTION_EVENT, networkId, 0, bssid));
    }

    public void broadcastSupplicantStateChangeEvent(String iface, int networkId, WifiSsid wifiSsid, String bssid, SupplicantState newSupplicantState) {
        HwHiLog.i(TAG, false, "broadcastSupplicantStateChangeEvent: %{public}s", new Object[]{iface});
        if (newSupplicantState == SupplicantState.ASSOCIATING) {
            String pmfInfo = ScanResultRecords.getDefault().getPmf(bssid);
            if (pmfInfo == null) {
                pmfInfo = "";
            }
            HwWifi2ChrManager.uploadWifi2PmfInfo(pmfInfo);
        }
        sendMessage(iface, Message.obtain(null, SUPPLICANT_STATE_CHANGE_EVENT, 0, 0, new StateChangeResult(networkId, wifiSsid, bssid, newSupplicantState)));
    }

    public void broadcastAuthenticationFailureEvent(String iface, int reason, int errorCode) {
        HwHiLog.i(TAG, false, "broadcastAuthenticationFailureEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, AUTHENTICATION_FAILURE_EVENT, reason, errorCode));
        HwWifi2ChrManager.uploadWifi2AuthFailException(iface, reason);
    }

    public void broadcastNetworkDisconnectionEvent(String iface, int local, int reason, String bssid) {
        HwHiLog.i(TAG, false, "broadcastNetworkDisconnectionEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, NETWORK_DISCONNECTION_EVENT, local, reason, bssid));
        WifiCommonUtils.notifyDeviceState("WLAN", "CONNECT_END", "");
        HwWifi2ChrManager.uploadWifi2DisconnectException(reason);
    }

    public void broadcastAssociationRejectionEvent(String iface, int status, boolean isTimedOut, String bssid) {
        HwHiLog.i(TAG, false, "broadcastAssociationRejectionEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, ASSOCIATION_REJECTION_EVENT, isTimedOut ? 1 : 0, status, bssid));
        WifiCommonUtils.notifyDeviceState("WLAN", "CONNECT_END", "");
        HwWifi2ChrManager.uploadWifi2AccosFailException(status);
    }

    public void broadcastTargetBssidEvent(String iface, String bssid) {
        HwHiLog.i(TAG, false, "broadcastTargetBssidEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, HwWifi2ClientModeImplConst.CMD_TARGET_BSSID, 0, 0, bssid));
    }

    public void broadcastAssociatedBssidEvent(String iface, String bssid) {
        HwHiLog.i(TAG, false, "broadcastAssociatedBssidEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, HwWifi2ClientModeImplConst.CMD_ASSOCIATED_BSSID, 0, 0, bssid));
    }

    public void broadcastVoWifiIrqStrEvent(String iface) {
        HwHiLog.i(TAG, false, "broadcastVoWifiIrqStrEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain((Handler) null, (int) VOWIFI_DETECT_IRQ_STR_EVENT));
    }

    public void broadcastHilinkStartWpsEvent(String iface, String bssid) {
        HwHiLog.i(TAG, false, "broadcastHilinkStartWpsEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, WPS_START_OKC_EVENT, bssid));
    }

    public void broadcastWpa3ConnectFailEvent(String iface, String eventInfo) {
        HwHiLog.i(TAG, false, "broadcastWpa3ConnectFailEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, WPA3_CONNECT_FAIL_EVENT, eventInfo));
    }

    public void broadcastAbsAntCoreRobEvent(String iface) {
        HwHiLog.i(TAG, false, "broadcastAbsAntCoreRobEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain((Handler) null, (int) ANT_CORE_ROB_EVNET));
    }

    public void broadcastNetworkEapErrorcodeReportEvent(String iface, int networkId, String ssid, int errorcode) {
        HwHiLog.i(TAG, false, "broadcastNetworkEapErrorcodeReportEvent: %{public}s", new Object[]{iface});
        sendMessage(iface, Message.obtain(null, EAP_ERRORCODE_REPORT_EVENT, networkId, errorcode, ssid));
    }

    public synchronized void registerHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> slaveWifiIfaceHandlers = this.mHandlerMap.get(iface);
        if (slaveWifiIfaceHandlers == null) {
            slaveWifiIfaceHandlers = new SparseArray<>();
            this.mHandlerMap.put(iface, slaveWifiIfaceHandlers);
        }
        Set<Handler> handlers = slaveWifiIfaceHandlers.get(what);
        if (handlers == null) {
            handlers = new ArraySet();
            slaveWifiIfaceHandlers.put(what, handlers);
        }
        handlers.add(handler);
    }

    public synchronized void deregisterHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> slaveWifiIfaceHandlers = this.mHandlerMap.get(iface);
        if (slaveWifiIfaceHandlers != null) {
            Set<Handler> handlers = slaveWifiIfaceHandlers.get(what);
            if (handlers != null) {
                handlers.remove(handler);
            }
        }
    }

    private boolean isMonitoring(String iface) {
        return this.mMonitoringMap.get(iface).booleanValue();
    }

    private void sendMessage(String iface, Message message) {
        SparseArray<Set<Handler>> ifaceHandlers = this.mHandlerMap.get(iface);
        if (iface == null || ifaceHandlers == null) {
            HwHiLog.d(TAG, false, "Sending to all monitors because there's no matching iface", new Object[0]);
            for (Map.Entry<String, SparseArray<Set<Handler>>> entry : this.mHandlerMap.entrySet()) {
                if (isMonitoring(entry.getKey())) {
                    sendMessageToTargets(entry.getValue().get(message.what), Message.obtain(message));
                }
            }
        } else if (isMonitoring(iface)) {
            sendMessageToTargets(ifaceHandlers.get(message.what), Message.obtain(message));
        } else {
            HwHiLog.d(TAG, false, "Dropping event because (%{public}s) is stopped", new Object[]{iface});
        }
        message.recycle();
    }

    private void sendMessage(Handler handler, Message message) {
        HwHiLog.i("Wifi2ClientModeImpl", false, "%{public}s send Monitor Message %{public}s", new Object[]{HwWifi2Injector.getInstance().getClientModeImpl().getCurrentState().getName(), HwWifi2ClientModeImplConst.messageNumToString(message.what)});
        message.setTarget(handler);
        message.sendToTarget();
    }

    private void sendMessageToTargets(Set<Handler> ifaceWhatHandlers, Message message) {
        if (ifaceWhatHandlers != null) {
            for (Handler handler : ifaceWhatHandlers) {
                if (handler != null) {
                    sendMessage(handler, message);
                }
            }
        }
    }

    private void setMonitoring(String iface, boolean isEnabled) {
        this.mMonitoringMap.put(iface, Boolean.valueOf(isEnabled));
    }

    private void setMonitoringNone() {
        for (String iface : this.mMonitoringMap.keySet()) {
            setMonitoring(iface, false);
        }
    }
}
