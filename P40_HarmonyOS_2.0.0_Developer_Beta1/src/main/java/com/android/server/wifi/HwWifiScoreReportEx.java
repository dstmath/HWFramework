package com.android.server.wifi;

import android.net.wifi.WifiInfo;
import android.util.wifi.HwHiLog;

public class HwWifiScoreReportEx implements IHwWifiScoreReportEx {
    private static final int LOW_SCORE_COUNT_MAX = 10;
    private static final String TAG = "HwWifiScoreReportEx";
    private static final int WIFI_SCORE_BAD = 58;
    private static final int WIFI_SCORE_GOOD = 60;
    private static HwWifiScoreReportEx mHwWifiScoreReportEx;
    private int lowScoreCount = 0;
    private int mScore = 0;

    public void setLowScoreCount(int lowScoreCount2) {
        this.lowScoreCount = lowScoreCount2;
    }

    public static HwWifiScoreReportEx getDefault() {
        if (mHwWifiScoreReportEx == null) {
            mHwWifiScoreReportEx = new HwWifiScoreReportEx();
        }
        return mHwWifiScoreReportEx;
    }

    public boolean isScoreCalculated(WifiInfo wifiInfo, int score) {
        int rawScore = WifiInjector.getInstance().getClientModeImpl().resetScoreByInetAccess(score == WIFI_SCORE_GOOD ? WIFI_SCORE_GOOD : WIFI_SCORE_BAD);
        boolean wifiConnectivityManagerEnabled = WifiInjector.getInstance().getClientModeImpl().isWifiConnectivityManagerEnabled();
        HwHiLog.i(TAG, false, "Score = %{public}d, wifiConnectivityManagerEnabled = %{public}s, lowScoreCount = %{public}d", new Object[]{Integer.valueOf(rawScore), String.valueOf(wifiConnectivityManagerEnabled), Integer.valueOf(this.lowScoreCount)});
        if (WifiInjector.getInstance().getClientModeImpl().isWifiInObtainingIpState() && (rawScore == WIFI_SCORE_GOOD || rawScore == WIFI_SCORE_BAD)) {
            HwHiLog.i(TAG, false, "do not modify the WiFi Score during selfcure when the state is OBTAINING_IPADDR", new Object[0]);
            return false;
        } else if (canReportWifiScoreDelayed()) {
            HwHiLog.i(TAG, false, "do not modify the WiFi Score when the first detect result has not return", new Object[0]);
            return false;
        } else {
            if (rawScore == WIFI_SCORE_GOOD) {
                if (!wifiConnectivityManagerEnabled) {
                    this.lowScoreCount = 0;
                    if (rawScore == wifiInfo.score) {
                        return false;
                    }
                }
            } else if (rawScore != WIFI_SCORE_BAD) {
                HwHiLog.i(TAG, false, "current wifi is verifying poor link", new Object[0]);
            } else if (wifiConnectivityManagerEnabled && rawScore == wifiInfo.score) {
                return false;
            } else {
                int i = this.lowScoreCount;
                this.lowScoreCount = i + 1;
                if (i < 10) {
                    return false;
                }
            }
            HwHiLog.d(TAG, false, "rawScore = %{public}d, mScore = %{public}d", new Object[]{Integer.valueOf(rawScore), Integer.valueOf(this.mScore)});
            this.lowScoreCount = 0;
            this.mScore = rawScore;
            return true;
        }
    }

    public int getScore() {
        return this.mScore;
    }

    private boolean canReportWifiScoreDelayed() {
        ClientModeImpl clientModeImpl = WifiInjector.getInstance().getClientModeImpl();
        if (!(clientModeImpl instanceof HwWifiStateMachine) || !((HwWifiStateMachine) clientModeImpl).canReportWifiScoreDelayed()) {
            return false;
        }
        return true;
    }
}
