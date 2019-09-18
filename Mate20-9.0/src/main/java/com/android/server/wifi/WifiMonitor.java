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
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.scanner.ScanResultRecords;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiCommonUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WifiMonitor {
    public static final int ANQP_DONE_EVENT = 147500;
    public static final String ANT_CORE_ROB = "ANT_CORE_ROB";
    public static final int ASSOCIATION_REJECTION_EVENT = 147499;
    public static final int AUTHENTICATION_FAILURE_EVENT = 147463;
    public static final int AUTHENTICATION_TIMEOUT_EVENT = 147501;
    private static final int BASE = 147456;
    static final long CHECK_INTERVAL = 30000;
    private static final int CONFIG_AUTH_FAILURE = 18;
    private static final int CONFIG_MULTIPLE_PBC_DETECTED = 12;
    private static boolean DBG = HWFLOW;
    public static final int EAP_ERRORCODE_REPORT_EVENT = 147956;
    public static final int EVENT_ANT_CORE_ROB = 147757;
    public static final int GAS_QUERY_DONE_EVENT = 147508;
    public static final int GAS_QUERY_START_EVENT = 147507;
    public static final int HEART_BEAT_ACK_EVENT = 147506;
    public static final int HS20_REMEDIATION_EVENT = 147517;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static boolean HWLOGW_E = true;
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
    private static final int STATE_EVENT_PROCESSING = 2;
    private static final int STATE_EVENT_WAIT = 1;
    private static final int STATE_THREAD_OVER = 0;
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
    /* access modifiers changed from: private */
    public static volatile int mEventToken;
    /* access modifiers changed from: private */
    public static final Object mEventTokenLock = new Object();
    /* access modifiers changed from: private */
    public static volatile int mMonitorThreadState;
    private boolean mConnected = false;
    private final Map<String, SparseArray<Set<Handler>>> mHandlerMap = new HashMap();
    private HwWifiCHRService mHwWifiCHRService;
    private final Map<String, Boolean> mMonitoringMap = new HashMap();
    private boolean mVerboseLoggingEnabled = false;
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

    public WifiMonitor(WifiInjector wifiInjector) {
        this.mWifiInjector = wifiInjector;
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    /* access modifiers changed from: package-private */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mVerboseLoggingEnabled = true;
        } else {
            this.mVerboseLoggingEnabled = false;
        }
    }

    public synchronized void registerHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> ifaceHandlers = this.mHandlerMap.get(iface);
        if (ifaceHandlers == null) {
            ifaceHandlers = new SparseArray<>();
            this.mHandlerMap.put(iface, ifaceHandlers);
        }
        Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(what);
        if (ifaceWhatHandlers == null) {
            ifaceWhatHandlers = new ArraySet<>();
            ifaceHandlers.put(what, ifaceWhatHandlers);
        }
        ifaceWhatHandlers.add(handler);
    }

    public synchronized void deregisterHandler(String iface, int what, Handler handler) {
        SparseArray<Set<Handler>> ifaceHandlers = this.mHandlerMap.get(iface);
        if (ifaceHandlers != null) {
            Set<Handler> ifaceWhatHandlers = ifaceHandlers.get(what);
            if (ifaceWhatHandlers != null) {
                ifaceWhatHandlers.remove(handler);
            }
        }
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
        Log.d(TAG, "startMonitoring(" + iface + ")");
        setMonitoring(iface, true);
        broadcastSupplicantConnectionEvent(iface);
    }

    public synchronized void stopMonitoring(String iface) {
        Log.d(TAG, "stopMonitoring(" + iface + ")");
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
        SparseArray<Set<Handler>> ifaceHandlers = this.mHandlerMap.get(iface);
        if (iface == null || ifaceHandlers == null) {
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Sending to all monitors because there's no matching iface");
            }
            for (Map.Entry<String, SparseArray<Set<Handler>>> entry : this.mHandlerMap.entrySet()) {
                if (isMonitoring(entry.getKey())) {
                    for (Handler handler : (Set) entry.getValue().get(message.what)) {
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
                if (cfgError == 12) {
                    sendMessage(iface, (int) WPS_FAIL_EVENT, 3);
                    return;
                } else if (cfgError != 18) {
                    if (reason == 0) {
                        reason = cfgError;
                    }
                    sendMessage(iface, WPS_FAIL_EVENT, 0, reason);
                    return;
                } else {
                    sendMessage(iface, (int) WPS_FAIL_EVENT, 6);
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
        sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, (Object) new TelephonyUtil.SimAuthRequestData(networkId, 4, ssid, data));
    }

    public void broadcastNetworkUmtsAuthRequestEvent(String iface, int networkId, String ssid, String[] data) {
        sendMessage(iface, (int) SUP_REQUEST_SIM_AUTH, (Object) new TelephonyUtil.SimAuthRequestData(networkId, 5, ssid, data));
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

    public void broadcastAuthenticationFailureEvent(String iface, int reason, int errorCode) {
        sendMessage(iface, AUTHENTICATION_FAILURE_EVENT, reason, errorCode);
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.updateWifiAuthFailEvent(iface, reason);
        }
    }

    public void broadcastAssociationRejectionEvent(String iface, int status, boolean timedOut, String bssid) {
        sendMessage(iface, ASSOCIATION_REJECTION_EVENT, timedOut ? 1 : 0, status, bssid);
        WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_END, "");
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.uploadAssocRejectException(status);
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
        WifiCommonUtils.notifyDeviceState(WifiCommonUtils.DEVICE_WLAN, WifiCommonUtils.STATE_CONNECT_END, "");
        if (this.mHwWifiCHRService != null) {
            this.mHwWifiCHRService.uploadDisconnectException(reason);
        }
    }

    public void broadcastSupplicantStateChangeEvent(String iface, int networkId, WifiSsid wifiSsid, String bssid, SupplicantState newSupplicantState) {
        if (newSupplicantState == SupplicantState.ASSOCIATING) {
            String pmfInfo = ScanResultRecords.getDefault().getPmf(bssid);
            if (pmfInfo == null) {
                pmfInfo = "";
            }
            if (this.mHwWifiCHRService != null) {
                Bundle data = new Bundle();
                data.putString("pmfInfo", pmfInfo);
                this.mHwWifiCHRService.uploadDFTEvent(1, data);
            }
        }
        sendMessage(iface, SUPPLICANT_STATE_CHANGE_EVENT, 0, 0, new StateChangeResult(networkId, wifiSsid, bssid, newSupplicantState));
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

    public void broadcastNetworkEAPErrorcodeReportEvent(String iface, int networkId, String ssid, int errorcode) {
        sendMessage(iface, EAP_ERRORCODE_REPORT_EVENT, networkId, errorcode, ssid);
    }
}
