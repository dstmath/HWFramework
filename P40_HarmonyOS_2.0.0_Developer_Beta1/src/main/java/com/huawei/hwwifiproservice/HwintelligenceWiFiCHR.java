package com.huawei.hwwifiproservice;

import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
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
    private static HwintelligenceWiFiCHR sHwintelligenceWiFiCHR = null;
    private Timer mConnectTimer = null;
    private HwIntelligenceStateMachine mMachine;
    private WifiProStatisticsManager mWifiProStatisticsManager;

    private HwintelligenceWiFiCHR(HwIntelligenceStateMachine machine) {
        this.mMachine = machine;
        this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
    }

    public static HwintelligenceWiFiCHR getInstance(HwIntelligenceStateMachine machine) {
        if (sHwintelligenceWiFiCHR == null) {
            sHwintelligenceWiFiCHR = new HwintelligenceWiFiCHR(machine);
        }
        return sHwintelligenceWiFiCHR;
    }

    public void increaseAutoOpenCount() {
        this.mWifiProStatisticsManager.increaseAutoOpenCount();
    }

    public void increaseAutoCloseCount() {
        this.mWifiProStatisticsManager.increaseAutoCloseCount();
    }

    public void uploadPortalApInWhite(String bssid, String ssid) {
        HwHiLog.i(MessageUtil.TAG, false, "uploadPortalApInWhite ssid =  %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(ssid)});
    }

    public void uploadWhiteNum(short num) {
        HwHiLog.d(MessageUtil.TAG, false, "uploadWhiteNum num = %{public}d", new Object[]{Short.valueOf(num)});
    }

    public void uploadAutoCloseFailed(int reason) {
    }

    public void uploadAutoOpenConnectFailed(String bssid, String ssid, int reason) {
    }

    public void startConnectTimer() {
        stopConnectTimer();
        HwHiLog.d(MessageUtil.TAG, false, "startConnectTimer", new Object[0]);
        this.mConnectTimer = new Timer();
        this.mConnectTimer.schedule(new MyTimerTask(), 60000);
    }

    public void stopConnectTimer() {
        HwHiLog.d(MessageUtil.TAG, false, "stopConnectTimer ", new Object[0]);
        Timer timer = this.mConnectTimer;
        if (timer != null) {
            timer.cancel();
            this.mConnectTimer = null;
        }
    }

    public class MyTimerTask extends TimerTask {
        public MyTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            String bssid = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedBssid();
            String ssid = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedSsid();
            int reason = HwintelligenceWiFiCHR.this.mMachine.getConnectFailedReason();
            HwHiLog.d(MessageUtil.TAG, false, "uploadAutoOpenConnectFailed ssid = %{public}s reason = %{public}d", new Object[]{StringUtilEx.safeDisplaySsid(ssid), Integer.valueOf(reason)});
            HwintelligenceWiFiCHR.this.uploadAutoOpenConnectFailed(bssid, ssid, reason);
        }
    }
}
