package com.android.server.wifi.wifipro.hwintelligencewifi;

import android.util.Log;
import com.android.server.wifi.HwWifiCHRConstImpl;
import com.android.server.wifi.wifipro.WifiProStatisticsManager;
import com.android.server.wifipro.WifiProCHRManager;
import java.util.Timer;
import java.util.TimerTask;

public class HwintelligenceWiFiCHR {
    public static final int CLOSE_REASON_CLOSE_BY_USER = 2;
    public static final int CLOSE_REASON_CONNECT_TO_NEW_AP = 1;
    public static final int CLOSE_REASON_ENTER_NEW_CELL = 3;
    public static final int OPEN_REASON_ASSOCIATION_REJECT = 4;
    public static final int OPEN_REASON_DHCP_FAILURE = 2;
    public static final int OPEN_REASON_DNS_FAILURE = 5;
    public static final int OPEN_REASON_PASSWORD_FAILURE = 1;
    public static final int OPEN_REASON_SERVER_FULL = 3;
    public static final int OPEN_REASON_UNKOWN = 0;
    private static HwintelligenceWiFiCHR mHwintelligenceWiFiCHR = null;
    private Timer mConnectTimer = null;
    private HwIntelligenceStateMachine mMachine;
    private WifiProCHRManager mWifiCHRStateManager;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    public class MyTimerTask extends TimerTask {
        public void run() {
            String bssid = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedBssid();
            String ssid = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedSsid();
            int reason = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedReason();
            Log.d(MessageUtil.TAG, "uploadAutoOpenConnectFailed ssid = " + ssid + " reason = " + reason);
            HwintelligenceWiFiCHR.this.uploadAutoOpenConnectFailed(bssid, ssid, reason);
        }
    }

    private HwintelligenceWiFiCHR(HwIntelligenceStateMachine machine) {
        this.mMachine = machine;
        this.mWifiCHRStateManager = WifiProCHRManager.getInstance();
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
    }

    public static HwintelligenceWiFiCHR getInstance(HwIntelligenceStateMachine machine) {
        if (mHwintelligenceWiFiCHR == null) {
            mHwintelligenceWiFiCHR = new HwintelligenceWiFiCHR(machine);
        }
        return mHwintelligenceWiFiCHR;
    }

    public void increaseAutoOpenCount() {
        this.mWifiProStatisticsManager.increaseAutoOpenCount();
    }

    public void increaseAutoCloseCount() {
        this.mWifiProStatisticsManager.increaseAutoCloseCount();
    }

    public void uploadPortalApInWhite(String bssid, String ssid) {
        Log.e(MessageUtil.TAG, "uploadPortalApInWhite ssid =  " + ssid);
        this.mWifiCHRStateManager.updateBSSID(bssid);
        this.mWifiCHRStateManager.updateSSID(ssid);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "PORTALAP_IN_WHITE");
    }

    public void uploadWhiteNum(short num) {
        Log.d(MessageUtil.TAG, "uploadWhiteNum num = " + num);
        this.mWifiCHRStateManager.updateAutoOpenWhiteNum(num);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "WHITE_MORETHAN_500");
    }

    public void uploadAutoCloseFailed(int reason) {
        this.mWifiCHRStateManager.updateAutoCloseRootCause((short) reason);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "AUTO_CLOSE_TERMINATION");
    }

    public void uploadAutoOpenConnectFailed(String bssid, String ssid, int reason) {
        this.mWifiCHRStateManager.updateBSSID(bssid);
        this.mWifiCHRStateManager.updateSSID(ssid);
        this.mWifiCHRStateManager.updateAutoOpenRootCause((short) reason);
        this.mWifiCHRStateManager.updateWifiException(HwWifiCHRConstImpl.WIFI_WIFIPRO_EXCEPTION_EVENT, "CANT_CONNECT_FOR_LONG");
    }

    public void startConnectTimer() {
        stopConnectTimer();
        Log.d(MessageUtil.TAG, "startConnectTimer");
        this.mConnectTimer = new Timer();
        this.mConnectTimer.schedule(new MyTimerTask(), 60000);
    }

    public void stopConnectTimer() {
        Log.d(MessageUtil.TAG, "stopConnectTimer ");
        if (this.mConnectTimer != null) {
            this.mConnectTimer.cancel();
            this.mConnectTimer = null;
        }
    }
}
