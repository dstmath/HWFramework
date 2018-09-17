package com.android.server.wifi;

import android.content.Context;
import android.net.NetworkAgent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.util.Log;

public class WifiScoreReport {
    private static final int AGGRESSIVE_HANDOVER_PENALTY = 6;
    private static final int BAD_LINKSPEED_PENALTY = 4;
    private static final int BAD_RSSI_COUNT_PENALTY = 2;
    private static final int GOOD_LINKSPEED_BONUS = 4;
    private static final int HOME_VISIBLE_NETWORK_MAX_COUNT = 6;
    private static final int LINK_STUCK_PENALTY = 2;
    private static final int LOW_SCORE_COUNT_MAX = 10;
    private static final int MAX_BAD_RSSI_COUNT = 7;
    private static final int MAX_LOW_RSSI_COUNT = 1;
    private static final int MAX_STUCK_LINK_COUNT = 5;
    private static final int MAX_SUCCESS_RATE_OF_STUCK_LINK = 3;
    private static final int MIN_SUSTAINED_LINK_STUCK_COUNT = 1;
    private static final double MIN_TX_FAILURE_RATE_FOR_WORKING_LINK = 0.3d;
    private static final int SCAN_CACHE_COUNT_PENALTY = 2;
    private static final int SCAN_CACHE_VISIBILITY_MS = 12000;
    private static final int STARTING_SCORE = 56;
    private static final String TAG = "WifiScoreReport";
    private static final int WIFI_SCORE_BAD = 98;
    private static final int WIFI_SCORE_GOOD = 100;
    private int lowScoreCount = 0;
    private final int mBadLinkSpeed24;
    private final int mBadLinkSpeed5;
    private final int mGoodLinkSpeed24;
    private final int mGoodLinkSpeed5;
    private boolean mIsHomeNetwork;
    private boolean mMultiBandScanResults;
    private String mReport;
    private boolean mReportValid = false;
    private final int mThresholdMinimumRssi24;
    private final int mThresholdMinimumRssi5;
    private final int mThresholdQualifiedRssi24;
    private final int mThresholdQualifiedRssi5;
    private final int mThresholdSaturatedRssi24;
    private final int mThresholdSaturatedRssi5;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;

    public void setLowScoreCount(int lowScoreCount) {
        this.lowScoreCount = lowScoreCount;
    }

    WifiScoreReport(Context context, WifiConfigManager wifiConfigManager) {
        this.mThresholdMinimumRssi5 = context.getResources().getInteger(17694891);
        this.mThresholdQualifiedRssi5 = context.getResources().getInteger(17694897);
        this.mThresholdSaturatedRssi5 = context.getResources().getInteger(17694895);
        this.mThresholdMinimumRssi24 = context.getResources().getInteger(17694890);
        this.mThresholdQualifiedRssi24 = context.getResources().getInteger(17694896);
        this.mThresholdSaturatedRssi24 = context.getResources().getInteger(17694894);
        this.mBadLinkSpeed24 = context.getResources().getInteger(17694888);
        this.mBadLinkSpeed5 = context.getResources().getInteger(17694889);
        this.mGoodLinkSpeed24 = context.getResources().getInteger(17694892);
        this.mGoodLinkSpeed5 = context.getResources().getInteger(17694893);
        this.mWifiConfigManager = wifiConfigManager;
    }

    public String getLastReport() {
        return this.mReport;
    }

    public void reset() {
        this.mReport = "";
        this.mReportValid = false;
    }

    public boolean isLastReportValid() {
        return this.mReportValid;
    }

    public void enableVerboseLogging(boolean enable) {
        this.mVerboseLoggingEnabled = enable;
    }

    public void calculateAndReportScore(WifiInfo wifiInfo, NetworkAgent networkAgent, int aggressiveHandover, WifiMetrics wifiMetrics) {
        updateScoringState(wifiInfo, aggressiveHandover);
        int score = calculateScore(wifiInfo, aggressiveHandover);
        int rawScore = score;
        if (score > 60) {
            score = 60;
        }
        if (score < 0) {
            score = 0;
        }
        score = WifiInjector.getInstance().getWifiStateMachine().resetScoreByInetAccess(score == 60 ? 100 : 98);
        boolean wifiConnectivityManagerEnabled = WifiInjector.getInstance().getWifiStateMachine().isWifiConnectivityManagerEnabled();
        Log.d(TAG, "Score = " + score + ", wifiConnectivityManagerEnabled = " + wifiConnectivityManagerEnabled + ", lowScoreCount = " + this.lowScoreCount);
        if (score == 100) {
            if (!wifiConnectivityManagerEnabled) {
                this.lowScoreCount = 0;
                return;
            }
        } else if (score == 98) {
            if (!wifiConnectivityManagerEnabled) {
                int i = this.lowScoreCount;
                this.lowScoreCount = i + 1;
                if (i < 10) {
                    return;
                }
            }
            return;
        }
        this.lowScoreCount = 0;
        if (score != wifiInfo.score) {
            Log.d(TAG, " rawScore = " + rawScore + ", score = " + score);
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, " report new wifi score " + score);
            }
            wifiInfo.score = score;
            if (networkAgent != null) {
                networkAgent.sendNetworkScore(score);
            }
        }
        this.mReport = String.format(" score=%d", new Object[]{Integer.valueOf(score)});
        this.mReportValid = true;
        wifiMetrics.incrementWifiScoreCount(score);
    }

    private void updateScoringState(WifiInfo wifiInfo, int aggressiveHandover) {
        this.mMultiBandScanResults = multiBandScanResults(wifiInfo);
        this.mIsHomeNetwork = isHomeNetwork(wifiInfo);
        int rssiThreshBad = this.mThresholdMinimumRssi24;
        int rssiThreshLow = this.mThresholdQualifiedRssi24;
        if (wifiInfo.is5GHz() && (this.mMultiBandScanResults ^ 1) != 0) {
            rssiThreshBad = this.mThresholdMinimumRssi5;
            rssiThreshLow = this.mThresholdQualifiedRssi5;
        }
        int rssi = wifiInfo.getRssi();
        if (aggressiveHandover != 0) {
            rssi -= aggressiveHandover * 6;
        }
        if (this.mIsHomeNetwork) {
            rssi += 5;
        }
        if (wifiInfo.txBadRate < 1.0d || wifiInfo.txSuccessRate >= 3.0d || rssi >= rssiThreshLow) {
            if (wifiInfo.txBadRate < MIN_TX_FAILURE_RATE_FOR_WORKING_LINK && wifiInfo.linkStuckCount > 0) {
                wifiInfo.linkStuckCount--;
            }
        } else if (wifiInfo.linkStuckCount < 5) {
            wifiInfo.linkStuckCount++;
        }
        if (rssi < rssiThreshBad) {
            if (wifiInfo.badRssiCount < 7) {
                wifiInfo.badRssiCount++;
            }
        } else if (rssi < rssiThreshLow) {
            wifiInfo.lowRssiCount = 1;
            if (wifiInfo.badRssiCount > 0) {
                wifiInfo.badRssiCount--;
            }
        } else {
            wifiInfo.badRssiCount = 0;
            wifiInfo.lowRssiCount = 0;
        }
    }

    private int calculateScore(WifiInfo wifiInfo, int aggressiveHandover) {
        int score = 56;
        int rssiThreshSaturated = this.mThresholdSaturatedRssi24;
        int linkspeedThreshBad = this.mBadLinkSpeed24;
        int linkspeedThreshGood = this.mGoodLinkSpeed24;
        if (wifiInfo.is5GHz()) {
            if (!this.mMultiBandScanResults) {
                rssiThreshSaturated = this.mThresholdSaturatedRssi5;
            }
            linkspeedThreshBad = this.mBadLinkSpeed5;
            linkspeedThreshGood = this.mGoodLinkSpeed5;
        }
        int rssi = wifiInfo.getRssi();
        if (aggressiveHandover != 0) {
            rssi -= aggressiveHandover * 6;
        }
        if (this.mIsHomeNetwork) {
            rssi += 5;
        }
        int linkSpeed = wifiInfo.getLinkSpeed();
        if (wifiInfo.linkStuckCount > 1) {
            score = 56 - ((wifiInfo.linkStuckCount - 1) * 2);
        }
        if (linkSpeed < linkspeedThreshBad) {
            score -= 4;
        } else if (linkSpeed >= linkspeedThreshGood && wifiInfo.txSuccessRate > 5.0d) {
            score += 4;
        }
        score -= (wifiInfo.badRssiCount * 2) + wifiInfo.lowRssiCount;
        if (rssi >= rssiThreshSaturated) {
            return score + 5;
        }
        return score;
    }

    private boolean multiBandScanResults(WifiInfo wifiInfo) {
        WifiConfiguration currentConfiguration = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        if (currentConfiguration == null) {
            return false;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(wifiInfo.getNetworkId());
        if (scanDetailCache == null) {
            return false;
        }
        currentConfiguration.setVisibility(scanDetailCache.getVisibility(12000));
        if (currentConfiguration.visibility == null || currentConfiguration.visibility.rssi24 == WifiConfiguration.INVALID_RSSI || currentConfiguration.visibility.rssi5 == WifiConfiguration.INVALID_RSSI || currentConfiguration.visibility.rssi24 < currentConfiguration.visibility.rssi5 - 2) {
            return false;
        }
        return true;
    }

    private boolean isHomeNetwork(WifiInfo wifiInfo) {
        WifiConfiguration currentConfiguration = this.mWifiConfigManager.getConfiguredNetwork(wifiInfo.getNetworkId());
        if (currentConfiguration == null || currentConfiguration.allowedKeyManagement.cardinality() != 1 || !currentConfiguration.allowedKeyManagement.get(1)) {
            return false;
        }
        ScanDetailCache scanDetailCache = this.mWifiConfigManager.getScanDetailCacheForNetwork(wifiInfo.getNetworkId());
        return scanDetailCache != null && scanDetailCache.size() <= 6;
    }
}
