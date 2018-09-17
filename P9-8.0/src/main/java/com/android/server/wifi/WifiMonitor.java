package com.android.server.wifi;

import android.net.wifi.SupplicantState;
import android.net.wifi.WifiSsid;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.util.TelephonyUtil.SimAuthRequestData;
import com.android.server.wifi.util.WifiCommonUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class WifiMonitor {
    public static final int ANQP_DONE_EVENT = 147500;
    public static final String ANT_CORE_ROB = "ANT_CORE_ROB";
    public static final int AP_LINKED_EVENT = 147856;
    public static final String AP_LINKED_EVENT_KEY = "event_key";
    public static final String AP_LINKED_MAC_KEY = "mac_key";
    public static final int ASSOCIATION_REJECTION_EVENT = 147499;
    public static final int AUTHENTICATION_FAILURE_EVENT = 147463;
    public static final int AUTHENTICATION_TIMEOUT_EVENT = 147501;
    private static final int BASE = 147456;
    static final long CHECK_INTERVAL = 30000;
    private static final int CONFIG_AUTH_FAILURE = 18;
    private static final int CONFIG_MULTIPLE_PBC_DETECTED = 12;
    private static boolean DBG = HWFLOW;
    public static final int EVENT_ANT_CORE_ROB = 147757;
    public static final int GAS_QUERY_DONE_EVENT = 147508;
    public static final int GAS_QUERY_START_EVENT = 147507;
    public static final int HEART_BEAT_ACK_EVENT = 147506;
    public static final int HS20_REMEDIATION_EVENT = 147517;
    protected static final boolean HWFLOW;
    private static boolean HWLOGW_E = true;
    private static final int MAX_SLEEP_RETRY_TIMES = 70;
    public static final int NETWORK_CONNECTION_EVENT = 147459;
    public static final int NETWORK_DISCONNECTION_EVENT = 147460;
    public static final int P2P_GC_INTERFACE_CREATED_EVENT = 147557;
    public static final int P2P_GO_INTERFACE_CREATED_EVENT = 147556;
    private static final String P2P_INTERFACE_CREATED_STR = "P2P-INTERFACE-CREATED";
    public static final int PNO_SCAN_RESULTS_EVENT = 147474;
    private static final int REASON_TKIP_ONLY_PROHIBITED = 1;
    private static final int REASON_WEP_PROHIBITED = 2;
    public static final int RX_HS20_ANQP_ICON_EVENT = 147509;
    public static final int SCAN_FAILED_EVENT = 147473;
    public static final int SCAN_RESULTS_EVENT = 147461;
    private static final int SLEEP_TIME_RETRY = 50;
    private static final int STATE_EVENT_PROCESSING = 2;
    private static final int STATE_EVENT_WAIT = 1;
    private static final int STATE_THREAD_OVER = 0;
    public static final String STA_JOIN_EVENT = "STA_JOIN";
    public static final String STA_LEAVE_EVENT = "STA_LEAVE";
    public static final int SUPPLICANT_STATE_CHANGE_EVENT = 147462;
    public static final int SUP_CONNECTION_EVENT = 147457;
    public static final int SUP_DISCONNECTION_EVENT = 147458;
    public static final int SUP_REQUEST_IDENTITY = 147471;
    public static final int SUP_REQUEST_SIM_AUTH = 147472;
    private static final String TAG = "WifiMonitor";
    private static final boolean VDBG = HWFLOW;
    public static final int VOWIFI_DETECT_IRQ_STR_EVENT = 147520;
    public static final int WAPI_AUTHENTICATION_FAILURE_EVENT = 147474;
    public static final int WAPI_CERTIFICATION_FAILURE_EVENT = 147475;
    public static final int WPS_FAIL_EVENT = 147465;
    public static final int WPS_OVERLAP_EVENT = 147466;
    public static final int WPS_START_OKC_EVENT = 147656;
    public static final int WPS_SUCCESS_EVENT = 147464;
    public static final int WPS_TIMEOUT_EVENT = 147467;
    private static volatile int mEventToken;
    private static final Object mEventTokenLock = new Object();
    private static volatile int mMonitorThreadState;
    private boolean mConnected = false;
    private final Map<String, SparseArray<Set<Handler>>> mHandlerMap = new HashMap();
    private HwWifiCHRService mHwWifiCHRService;
    private final Map<String, Boolean> mMonitoringMap = new HashMap();
    private boolean mVerboseLoggingEnabled = false;
    private HwWifiCHRStateManager mWiFiCHRManager;
    private final WifiInjector mWifiInjector;

    private static class WatchdogThread extends Thread {
        private int preEventToken;

        public WatchdogThread() {
            super("WifiMonitorWatchdog");
        }

        private void waitMonent(long timeout) {
            synchronized (this) {
                long waitTime = timeout;
                long start = SystemClock.uptimeMillis();
                while (timeout > 0) {
                    try {
                        wait(timeout);
                    } catch (InterruptedException e) {
                        Log.wtf(WifiMonitor.TAG, e);
                    }
                    timeout = waitTime - (SystemClock.uptimeMillis() - start);
                }
            }
        }

        public void run() {
            int wait_state_counter = 0;
            int pretoken = 0;
            while (WifiMonitor.mMonitorThreadState != 0) {
                if (WifiMonitor.mMonitorThreadState == 1) {
                    waitMonent(15000);
                    wait_state_counter++;
                    synchronized (WifiMonitor.mEventTokenLock) {
                        if (pretoken != WifiMonitor.mEventToken) {
                            pretoken = WifiMonitor.mEventToken;
                            wait_state_counter = 0;
                        }
                    }
                    if (wait_state_counter > 6) {
                        wait_state_counter = 0;
                        Log.d(WifiMonitor.TAG, "WatchdogThread wait state.");
                    }
                } else {
                    this.preEventToken = WifiMonitor.mEventToken;
                    waitMonent(WifiMonitor.CHECK_INTERVAL);
                    if (WifiMonitor.mMonitorThreadState == 2 && this.preEventToken == WifiMonitor.mEventToken) {
                        Log.d(WifiMonitor.TAG, "WatchdogThread mMonitorThread statckTrace:");
                    }
                }
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public WifiMonitor(WifiInjector wifiInjector) {
        this.mWifiInjector = wifiInjector;
        this.mWiFiCHRManager = HwWifiServiceFactory.getHwWifiCHRStateManager();
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    public synchronized void registerHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> ifaceHandlers = (SparseArray) this.mHandlerMap.get(iface);
        if (ifaceHandlers == null) {
            ifaceHandlers = new SparseArray();
            this.mHandlerMap.put(iface, ifaceHandlers);
        }
        Set<Handler> ifaceWhatHandlers = (Set) ifaceHandlers.get(what);
        if (ifaceWhatHandlers == null) {
            ifaceWhatHandlers = new ArraySet();
            ifaceHandlers.put(what, ifaceWhatHandlers);
        }
        ifaceWhatHandlers.add(handler);
    }

    private boolean isMonitoring(String iface) {
        Boolean val = (Boolean) this.mMonitoringMap.get(iface);
        if (val == null) {
            return false;
        }
        return val.booleanValue();
    }

    public void setMonitoring(String iface, boolean enabled) {
        this.mMonitoringMap.put(iface, Boolean.valueOf(enabled));
    }

    private void setMonitoringNone() {
        for (String iface : this.mMonitoringMap.keySet()) {
            setMonitoring(iface, false);
        }
    }

    private boolean ensureConnectedLocked() {
        if (this.mConnected) {
            return true;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "connecting to supplicant");
        }
        int connectTries = 0;
        while (true) {
            this.mConnected = this.mWifiInjector.getWifiNative().connectToSupplicant();
            if (this.mConnected) {
                return true;
            }
            if (connectTries >= MAX_SLEEP_RETRY_TIMES) {
                return false;
            }
            try {
                Log.e(TAG, "startMonitoring connectTries sleep:50");
                Thread.sleep(50);
                connectTries++;
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized boolean startMonitoring(String iface, boolean isStaIface) {
        Log.d(TAG, "startMonitoring(" + iface + ") with mConnected = " + this.mConnected);
        if (ensureConnectedLocked()) {
            setMonitoring(iface, true);
            broadcastSupplicantConnectionEvent(iface);
            return true;
        }
        boolean originalMonitoring = isMonitoring(iface);
        setMonitoring(iface, true);
        broadcastSupplicantDisconnectionEvent(iface);
        setMonitoring(iface, originalMonitoring);
        Log.e(TAG, "startMonitoring(" + iface + ") failed!");
        return false;
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
        sendMessage(iface, Message.obtain(null, what));
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
        SparseArray<Set<Handler>> ifaceHandlers = (SparseArray) this.mHandlerMap.get(iface);
        if (iface == null || ifaceHandlers == null) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Sending to all monitors because there's no matching iface");
            }
            for (Entry<String, SparseArray<Set<Handler>>> entry : this.mHandlerMap.entrySet()) {
                if (isMonitoring((String) entry.getKey())) {
                    for (Handler handler : (Set) ((SparseArray) entry.getValue()).get(message.what)) {
                        if (handler != null) {
                            sendMessage(handler, Message.obtain(message));
                        }
                    }
                }
            }
        } else if (isMonitoring(iface) || shouldMessageSend(message.what)) {
            Set<Handler> ifaceWhatHandlers = (Set) ifaceHandlers.get(message.what);
            if (ifaceWhatHandlers != null) {
                for (Handler handler2 : ifaceWhatHandlers) {
                    if (handler2 != null) {
                        sendMessage(handler2, Message.obtain(message));
                    }
                }
            }
        } else if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Dropping event because (" + iface + ") is stopped");
        }
        message.recycle();
    }

    private void sendMessage(Handler handler, Message message) {
        message.setTarget(handler);
        message.sendToTarget();
    }

    public void broadcastWpsFailEvent(String iface, int cfgError, int vendorErrorCode) {
        switch (vendorErrorCode) {
            case 1:
                sendMessage(iface, (int) WPS_FAIL_EVENT, 5);
                return;
            case 2:
                sendMessage(iface, (int) WPS_FAIL_EVENT, 4);
                return;
            default:
                int reason = vendorErrorCode;
                switch (cfgError) {
                    case 12:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 3);
                        return;
                    case 18:
                        sendMessage(iface, (int) WPS_FAIL_EVENT, 6);
                        return;
                    default:
                        if (vendorErrorCode == 0) {
                            reason = cfgError;
                        }
                        sendMessage(iface, WPS_FAIL_EVENT, 0, reason);
                        return;
                }
        }
    }

    public void broadcastWpsSuccessEvent(String iface) {
        sendMessage(iface, (int) WPS_SUCCESS_EVENT);
    }

    private void logDbg(String debug) {
        Log.e(TAG, debug);
    }

    public void broadcastWpsOverlapEvent(String iface) {
        sendMessage(iface, (int) WPS_OVERLAP_EVENT);
    }

    public void broadcastWpsTimeoutEvent(String iface) {
        sendMessage(iface, (int) WPS_TIMEOUT_EVENT);
    }

    public void broadcastAnqpDoneEvent(String iface, AnqpEvent anqpEvent) {
        sendMessage(iface, (int) ANQP_DONE_EVENT, (Object) anqpEvent);
    }

    public void broadcastIconDoneEvent(String iface, IconEvent iconEvent) {
        sendMessage(iface, (int) RX_HS20_ANQP_ICON_EVENT, (Object) iconEvent);
    }

    public void broadcastWnmEvent(String iface, WnmData wnmData) {
        sendMessage(iface, (int) HS20_REMEDIATION_EVENT, (Object) wnmData);
    }

    public void broadcastNetworkIdentityRequestEvent(String iface, int networkId, String ssid) {
        sendMessage(iface, SUP_REQUEST_IDENTITY, 0, networkId, ssid);
    }

    public void broadcastNetworkGsmAuthRequestEvent(String iface, int networkId, String ssid, String[] data) {
        sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, new SimAuthRequestData(networkId, 4, ssid, data));
    }

    public void broadcastNetworkUmtsAuthRequestEvent(String iface, int networkId, String ssid, String[] data) {
        sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, new SimAuthRequestData(networkId, 5, ssid, data));
    }

    public void broadcastScanResultEvent(String iface) {
        sendMessage(iface, (int) SCAN_RESULTS_EVENT);
    }

    public void broadcastPnoScanResultEvent(String iface) {
        sendMessage(iface, 147474);
    }

    public void broadcastScanFailedEvent(String iface) {
        sendMessage(iface, (int) SCAN_FAILED_EVENT);
    }

    public void broadcastAuthenticationFailureEvent(String iface, int reason) {
        sendMessage(iface, AUTHENTICATION_FAILURE_EVENT, 0, reason);
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.updateWifiAuthFailEvent(iface, reason);
        }
    }

    public void broadcastAssociationRejectionEvent(String iface, int status, boolean timedOut, String bssid) {
        sendMessage(iface, ASSOCIATION_REJECTION_EVENT, timedOut ? 1 : 0, status, bssid);
        if (this.mWifiInjector.getWifiNative().getInterfaceName().equals(iface)) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_END, "");
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.uploadAssocRejectException(status, bssid);
        }
    }

    public void broadcastAssociatedBssidEvent(String iface, String bssid) {
        sendMessage(iface, 131219, 0, 0, bssid);
    }

    public void broadcastTargetBssidEvent(String iface, String bssid) {
        sendMessage(iface, 131213, 0, 0, bssid);
    }

    public void broadcastNetworkConnectionEvent(String iface, int networkId, String bssid) {
        sendMessage(iface, NETWORK_CONNECTION_EVENT, networkId, 0, bssid);
    }

    public void broadcastNetworkDisconnectionEvent(String iface, int local, int reason, String bssid) {
        sendMessage(iface, NETWORK_DISCONNECTION_EVENT, local, reason, bssid);
        if (this.mWifiInjector.getWifiNative().getInterfaceName().equals(iface)) {
            WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_END, "");
        }
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.uploadDisconnectException(reason);
        }
    }

    public void broadcastSupplicantStateChangeEvent(String iface, int networkId, WifiSsid wifiSsid, String bssid, SupplicantState newSupplicantState) {
        sendMessage(iface, SUPPLICANT_STATE_CHANGE_EVENT, 0, 0, new StateChangeResult(networkId, wifiSsid, bssid, newSupplicantState));
        if (this.mWiFiCHRManager != null) {
            this.mWiFiCHRManager.updateAP(iface, networkId, newSupplicantState, bssid);
        }
    }

    public void broadcastSupplicantConnectionEvent(String iface) {
        sendMessage(iface, 147457);
    }

    public void broadcastSupplicantDisconnectionEvent(String iface) {
        sendMessage(iface, 147458);
    }

    public void broadcastWapiCertInitFailEvent(String iface) {
        sendMessage(iface, (int) WAPI_CERTIFICATION_FAILURE_EVENT);
    }

    public void broadcastWapiAuthFailEvent(String iface) {
        sendMessage(iface, 147474);
    }

    public void broadcastVoWifiIrqStrEvent(String iface) {
        sendMessage(iface, (int) VOWIFI_DETECT_IRQ_STR_EVENT);
    }

    public void broadcastHilinkStartWpsEvent(String iface, String bssid) {
        sendMessage(iface, (int) WPS_START_OKC_EVENT, (Object) bssid);
    }

    public void broadcastAbsAntCoreRobEvent(String iface) {
        sendMessage(iface, (int) EVENT_ANT_CORE_ROB);
    }

    private boolean shouldMessageSend(int what) {
        if (AP_LINKED_EVENT != what) {
            return false;
        }
        Log.d(TAG, "message " + what + " will be sent");
        return true;
    }

    public void broadcastApLinkedStaChangedEvent(String iface, String event, String macAddress) {
        if (STA_JOIN_EVENT.equals(event) || STA_LEAVE_EVENT.equals(event)) {
            Log.d(TAG, "broadcastApLinkedStaChangedEvent(): event=" + event + " iface=" + iface);
            Object bundle = new Bundle();
            bundle.putString(AP_LINKED_EVENT_KEY, event);
            bundle.putString(AP_LINKED_MAC_KEY, macAddress);
            sendMessage(iface, (int) AP_LINKED_EVENT, bundle);
            return;
        }
        Log.w(TAG, "ApLinkedStaListChange: Invalid event from daemon " + iface);
    }
}
