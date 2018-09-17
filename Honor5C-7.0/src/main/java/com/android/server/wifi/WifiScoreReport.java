package com.android.server.wifi;

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
    private static final int MAX_BAD_LINKSPEED_COUNT = 6;
    private static final int MAX_BAD_RSSI_COUNT = 7;
    private static final int MAX_LOW_RSSI_COUNT = 1;
    private static final int MAX_STUCK_LINK_COUNT = 5;
    private static final int MAX_SUCCESS_COUNT_OF_STUCK_LINK = 3;
    private static final int MIN_NUM_TICKS_AT_STATE = 1000;
    private static final int MIN_SUCCESS_COUNT = 5;
    private static final int MIN_SUSTAINED_LINK_STUCK_COUNT = 1;
    private static final double MIN_TX_RATE_FOR_WORKING_LINK = 0.3d;
    private static final int SCAN_CACHE_COUNT_PENALTY = 2;
    private static final int SCAN_CACHE_VISIBILITY_MS = 12000;
    private static final int STARTING_SCORE = 56;
    private static final String TAG = "WifiStateMachine";
    private static final int USER_DISCONNECT_PENALTY = 5;
    private int mBadLinkspeedcount;
    private String mReport;

    WifiScoreReport(String report, int badLinkspeedcount) {
        this.mReport = report;
        this.mBadLinkspeedcount = badLinkspeedcount;
    }

    public String getReport() {
        return this.mReport;
    }

    public int getBadLinkspeedcount() {
        return this.mBadLinkspeedcount;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static WifiScoreReport calculateScore(WifiInfo wifiInfo, WifiConfiguration currentConfiguration, WifiConfigManager wifiConfigManager, NetworkAgent networkAgent, WifiScoreReport lastReport, int aggressiveHandover) {
        boolean isBadRSSI;
        boolean isLowRSSI;
        boolean isHighRSSI;
        int penalizedDueToUserTriggeredDisconnect;
        int i;
        String rssiStatus;
        String str;
        StringBuilder append;
        Object[] objArr;
        WifiStateMachine globalHwWsm;
        boolean debugLogging = false;
        if (wifiConfigManager.mEnableVerboseLogging.get() > 0) {
            debugLogging = true;
        }
        StringBuilder sb = new StringBuilder();
        int score = STARTING_SCORE;
        boolean isBadLinkspeed = (!wifiInfo.is24GHz() || wifiInfo.getLinkSpeed() >= wifiConfigManager.mBadLinkSpeed24) ? wifiInfo.is5GHz() && wifiInfo.getLinkSpeed() < wifiConfigManager.mBadLinkSpeed5 : true;
        boolean isGoodLinkspeed = (!wifiInfo.is24GHz() || wifiInfo.getLinkSpeed() < wifiConfigManager.mGoodLinkSpeed24) ? wifiInfo.is5GHz() && wifiInfo.getLinkSpeed() >= wifiConfigManager.mGoodLinkSpeed5 : true;
        int badLinkspeedcount = 0;
        if (lastReport != null) {
            badLinkspeedcount = lastReport.getBadLinkspeedcount();
        }
        if (isBadLinkspeed) {
            if (badLinkspeedcount < MAX_BAD_LINKSPEED_COUNT) {
                badLinkspeedcount += MIN_SUSTAINED_LINK_STUCK_COUNT;
            }
        } else if (badLinkspeedcount > 0) {
            badLinkspeedcount--;
        }
        if (isBadLinkspeed) {
            sb.append(" bl(").append(badLinkspeedcount).append(")");
        }
        if (isGoodLinkspeed) {
            sb.append(" gl");
        }
        boolean use24Thresholds = false;
        boolean homeNetworkBoost = false;
        ScanDetailCache scanDetailCache = wifiConfigManager.getScanDetailCache(currentConfiguration);
        if (!(currentConfiguration == null || scanDetailCache == null)) {
            currentConfiguration.setVisibility(scanDetailCache.getVisibility(12000));
            if (!(currentConfiguration.visibility == null || currentConfiguration.visibility.rssi24 == WifiConfiguration.INVALID_RSSI)) {
                if (currentConfiguration.visibility.rssi24 >= currentConfiguration.visibility.rssi5 - 2) {
                    use24Thresholds = true;
                }
            }
            if (scanDetailCache.size() <= MAX_BAD_LINKSPEED_COUNT) {
                if (currentConfiguration.allowedKeyManagement.cardinality() == MIN_SUSTAINED_LINK_STUCK_COUNT) {
                    if (currentConfiguration.allowedKeyManagement.get(MIN_SUSTAINED_LINK_STUCK_COUNT)) {
                        homeNetworkBoost = true;
                    }
                }
            }
        }
        if (homeNetworkBoost) {
            sb.append(" hn");
        }
        if (use24Thresholds) {
            sb.append(" u24");
        }
        int rssi = (wifiInfo.getRssi() - (aggressiveHandover * MAX_BAD_LINKSPEED_COUNT)) + (homeNetworkBoost ? USER_DISCONNECT_PENALTY : 0);
        Object[] objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
        objArr2[0] = Integer.valueOf(rssi);
        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(aggressiveHandover);
        sb.append(String.format(" rssi=%d ag=%d", objArr2));
        boolean is24GHz = !use24Thresholds ? wifiInfo.is24GHz() : true;
        if (is24GHz) {
            if (rssi < wifiConfigManager.mThresholdMinimumRssi24.get()) {
                isBadRSSI = true;
                if (is24GHz) {
                    if (rssi < wifiConfigManager.mThresholdQualifiedRssi24.get()) {
                        isLowRSSI = true;
                        if (is24GHz) {
                            if (rssi >= wifiConfigManager.mThresholdSaturatedRssi24.get()) {
                                isHighRSSI = true;
                                if (isBadRSSI) {
                                    sb.append(" br");
                                }
                                if (isLowRSSI) {
                                    sb.append(" lr");
                                }
                                if (isHighRSSI) {
                                    sb.append(" hr");
                                }
                                penalizedDueToUserTriggeredDisconnect = 0;
                                if (currentConfiguration != null) {
                                    if (wifiInfo.txSuccessRate <= 5.0d) {
                                    }
                                    if (!isBadRSSI) {
                                        currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        i = currentConfiguration.numTicksAtBadRSSI;
                                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                            }
                                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                            }
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                            }
                                            currentConfiguration.numTicksAtBadRSSI = 0;
                                        }
                                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI <= 0 && currentConfiguration.numUserTriggeredWifiDisableLowRSSI <= 0) {
                                                if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                }
                                            }
                                            score = 51;
                                            penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                            sb.append(" p1");
                                        }
                                    } else if (!isLowRSSI) {
                                        currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        i = currentConfiguration.numTicksAtLowRSSI;
                                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                            }
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                            }
                                            currentConfiguration.numTicksAtLowRSSI = 0;
                                        }
                                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment && (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0 || currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0)) {
                                            score = 51;
                                            penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                            sb.append(" p2");
                                        }
                                    } else if (!isHighRSSI) {
                                        currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        i = currentConfiguration.numTicksAtNotHighRSSI;
                                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                            }
                                            currentConfiguration.numTicksAtNotHighRSSI = 0;
                                        }
                                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment && currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                            score = 51;
                                            penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                            sb.append(" p3");
                                        }
                                    }
                                    objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                                    objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                                    objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                                    sb.append(String.format(" ticks %d,%d,%d", objArr2));
                                }
                                if (debugLogging) {
                                    rssiStatus = "";
                                    if (!isBadRSSI) {
                                        rssiStatus = rssiStatus + " badRSSI ";
                                    } else if (!isHighRSSI) {
                                        rssiStatus = rssiStatus + " highRSSI ";
                                    } else if (isLowRSSI) {
                                        rssiStatus = rssiStatus + " lowRSSI ";
                                    }
                                    if (isBadLinkspeed) {
                                        rssiStatus = rssiStatus + " lowSpeed ";
                                    }
                                    str = TAG;
                                    append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                                    append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                                    append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                                    append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                                    Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                                }
                                if (wifiInfo.txBadRate >= 1.0d) {
                                    if (wifiInfo.txSuccessRate < 3.0d && (isBadRSSI || isLowRSSI)) {
                                        i = wifiInfo.linkStuckCount;
                                        if (r0 < USER_DISCONNECT_PENALTY) {
                                            wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        }
                                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                        objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                                        sb.append(String.format(" ls+=%d", objArr2));
                                        if (debugLogging) {
                                            Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                                        }
                                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                        objArr2[0] = Integer.valueOf(score);
                                        sb.append(String.format(" [%d", objArr2));
                                        i = wifiInfo.linkStuckCount;
                                        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                                        }
                                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                        objArr2[0] = Integer.valueOf(score);
                                        sb.append(String.format(",%d", objArr2));
                                        if (!isBadLinkspeed) {
                                            score -= 4;
                                            if (debugLogging) {
                                                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                            }
                                        } else if (isGoodLinkspeed) {
                                            if (wifiInfo.txSuccessRate > 5.0d) {
                                                score += GOOD_LINKSPEED_BONUS;
                                            }
                                        }
                                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                        objArr2[0] = Integer.valueOf(score);
                                        sb.append(String.format(",%d", objArr2));
                                        if (!isBadRSSI) {
                                            i = wifiInfo.badRssiCount;
                                            if (r0 < MAX_BAD_RSSI_COUNT) {
                                                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                            }
                                        } else if (isLowRSSI) {
                                            wifiInfo.badRssiCount = 0;
                                            wifiInfo.lowRssiCount = 0;
                                        } else {
                                            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                            if (wifiInfo.badRssiCount > 0) {
                                                wifiInfo.badRssiCount--;
                                            }
                                        }
                                        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                        objArr2[0] = Integer.valueOf(score);
                                        sb.append(String.format(",%d", objArr2));
                                        if (debugLogging) {
                                            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                                        }
                                        if (isHighRSSI) {
                                            score += USER_DISCONNECT_PENALTY;
                                            if (debugLogging) {
                                                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                            }
                                        }
                                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                        objArr2[0] = Integer.valueOf(score);
                                        sb.append(String.format(",%d]", objArr2));
                                        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                                        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                                        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                                        sb.append(String.format(" brc=%d lrc=%d", objArr2));
                                        if (score > 60) {
                                            score = 60;
                                        }
                                        if (score < 0) {
                                        }
                                        score = 100;
                                        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                                        if (globalHwWsm != null) {
                                            score = globalHwWsm.resetScoreByInetAccess(100);
                                        }
                                        if (score != wifiInfo.score) {
                                            if (debugLogging) {
                                                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                            }
                                            wifiInfo.score = score;
                                            if (networkAgent != null) {
                                                networkAgent.sendNetworkScore(score);
                                            }
                                        }
                                        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                                    }
                                }
                                if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                                    if (wifiInfo.linkStuckCount > 0) {
                                        wifiInfo.linkStuckCount--;
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                                    sb.append(String.format(" ls-=%d", objArr2));
                                    if (debugLogging) {
                                        Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                                    }
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(" [%d", objArr2));
                                i = wifiInfo.linkStuckCount;
                                if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                    score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d", objArr2));
                                if (!isBadLinkspeed) {
                                    score -= 4;
                                    if (debugLogging) {
                                        Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                    }
                                } else if (isGoodLinkspeed) {
                                    if (wifiInfo.txSuccessRate > 5.0d) {
                                        score += GOOD_LINKSPEED_BONUS;
                                    }
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d", objArr2));
                                if (!isBadRSSI) {
                                    i = wifiInfo.badRssiCount;
                                    if (r0 < MAX_BAD_RSSI_COUNT) {
                                        wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                    }
                                } else if (isLowRSSI) {
                                    wifiInfo.badRssiCount = 0;
                                    wifiInfo.lowRssiCount = 0;
                                } else {
                                    wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                    if (wifiInfo.badRssiCount > 0) {
                                        wifiInfo.badRssiCount--;
                                    }
                                }
                                score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d", objArr2));
                                if (debugLogging) {
                                    Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                                }
                                if (isHighRSSI) {
                                    score += USER_DISCONNECT_PENALTY;
                                    if (debugLogging) {
                                        Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                    }
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d]", objArr2));
                                objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                                objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                                objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                                sb.append(String.format(" brc=%d lrc=%d", objArr2));
                                if (score > 60) {
                                    score = 60;
                                }
                                if (score < 0) {
                                }
                                score = 100;
                                globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                                if (globalHwWsm != null) {
                                    score = globalHwWsm.resetScoreByInetAccess(100);
                                }
                                if (score != wifiInfo.score) {
                                    if (debugLogging) {
                                        Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                    }
                                    wifiInfo.score = score;
                                    if (networkAgent != null) {
                                        networkAgent.sendNetworkScore(score);
                                    }
                                }
                                return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                            }
                        }
                        if (is24GHz) {
                            isHighRSSI = wifiInfo.getRssi() < wifiConfigManager.mThresholdSaturatedRssi5.get();
                        } else {
                            isHighRSSI = false;
                        }
                        if (isBadRSSI) {
                            sb.append(" br");
                        }
                        if (isLowRSSI) {
                            sb.append(" lr");
                        }
                        if (isHighRSSI) {
                            sb.append(" hr");
                        }
                        penalizedDueToUserTriggeredDisconnect = 0;
                        if (currentConfiguration != null) {
                            if (wifiInfo.txSuccessRate <= 5.0d) {
                            }
                            if (!isBadRSSI) {
                                currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtBadRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtBadRSSI = 0;
                                }
                                if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        sb.append(" p1");
                                    }
                                }
                            } else if (!isLowRSSI) {
                                currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtLowRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtLowRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                sb.append(" p2");
                            } else if (isHighRSSI) {
                                currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtNotHighRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtNotHighRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                sb.append(" p3");
                            }
                            objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                            objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                            objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                            sb.append(String.format(" ticks %d,%d,%d", objArr2));
                        }
                        if (debugLogging) {
                            rssiStatus = "";
                            if (!isBadRSSI) {
                                rssiStatus = rssiStatus + " badRSSI ";
                            } else if (!isHighRSSI) {
                                rssiStatus = rssiStatus + " highRSSI ";
                            } else if (isLowRSSI) {
                                rssiStatus = rssiStatus + " lowRSSI ";
                            }
                            if (isBadLinkspeed) {
                                rssiStatus = rssiStatus + " lowSpeed ";
                            }
                            str = TAG;
                            append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                            append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                            Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                        }
                        if (wifiInfo.txBadRate >= 1.0d) {
                            i = wifiInfo.linkStuckCount;
                            if (r0 < USER_DISCONNECT_PENALTY) {
                                wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls+=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(" [%d", objArr2));
                            i = wifiInfo.linkStuckCount;
                            if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadLinkspeed) {
                                score -= 4;
                                if (debugLogging) {
                                    Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                }
                            } else if (isGoodLinkspeed) {
                                if (wifiInfo.txSuccessRate > 5.0d) {
                                    score += GOOD_LINKSPEED_BONUS;
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadRSSI) {
                                i = wifiInfo.badRssiCount;
                                if (r0 < MAX_BAD_RSSI_COUNT) {
                                    wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                }
                            } else if (isLowRSSI) {
                                wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                if (wifiInfo.badRssiCount > 0) {
                                    wifiInfo.badRssiCount--;
                                }
                            } else {
                                wifiInfo.badRssiCount = 0;
                                wifiInfo.lowRssiCount = 0;
                            }
                            score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                            }
                            if (isHighRSSI) {
                                score += USER_DISCONNECT_PENALTY;
                                if (debugLogging) {
                                    Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d]", objArr2));
                            objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                            objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                            sb.append(String.format(" brc=%d lrc=%d", objArr2));
                            if (score > 60) {
                                score = 60;
                            }
                            if (score < 0) {
                            }
                            score = 100;
                            globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                            if (globalHwWsm != null) {
                                score = globalHwWsm.resetScoreByInetAccess(100);
                            }
                            if (score != wifiInfo.score) {
                                if (debugLogging) {
                                    Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                }
                                wifiInfo.score = score;
                                if (networkAgent != null) {
                                    networkAgent.sendNetworkScore(score);
                                }
                            }
                            return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                        }
                        if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                            if (wifiInfo.linkStuckCount > 0) {
                                wifiInfo.linkStuckCount--;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls-=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(" [%d", objArr2));
                        i = wifiInfo.linkStuckCount;
                        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadLinkspeed) {
                            score -= 4;
                            if (debugLogging) {
                                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                            }
                        } else if (isGoodLinkspeed) {
                            if (wifiInfo.txSuccessRate > 5.0d) {
                                score += GOOD_LINKSPEED_BONUS;
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadRSSI) {
                            i = wifiInfo.badRssiCount;
                            if (r0 < MAX_BAD_RSSI_COUNT) {
                                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                        } else if (isLowRSSI) {
                            wifiInfo.badRssiCount = 0;
                            wifiInfo.lowRssiCount = 0;
                        } else {
                            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                            if (wifiInfo.badRssiCount > 0) {
                                wifiInfo.badRssiCount--;
                            }
                        }
                        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (debugLogging) {
                            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                        }
                        if (isHighRSSI) {
                            score += USER_DISCONNECT_PENALTY;
                            if (debugLogging) {
                                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d]", objArr2));
                        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                        sb.append(String.format(" brc=%d lrc=%d", objArr2));
                        if (score > 60) {
                            score = 60;
                        }
                        if (score < 0) {
                        }
                        score = 100;
                        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                        if (globalHwWsm != null) {
                            score = globalHwWsm.resetScoreByInetAccess(100);
                        }
                        if (score != wifiInfo.score) {
                            if (debugLogging) {
                                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                            }
                            wifiInfo.score = score;
                            if (networkAgent != null) {
                                networkAgent.sendNetworkScore(score);
                            }
                        }
                        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                    }
                }
                if (is24GHz) {
                    isLowRSSI = false;
                } else {
                    isLowRSSI = wifiInfo.getRssi() < wifiConfigManager.mThresholdMinimumRssi5.get();
                }
                if (is24GHz) {
                    if (rssi >= wifiConfigManager.mThresholdSaturatedRssi24.get()) {
                        isHighRSSI = true;
                        if (isBadRSSI) {
                            sb.append(" br");
                        }
                        if (isLowRSSI) {
                            sb.append(" lr");
                        }
                        if (isHighRSSI) {
                            sb.append(" hr");
                        }
                        penalizedDueToUserTriggeredDisconnect = 0;
                        if (currentConfiguration != null) {
                            if (wifiInfo.txSuccessRate <= 5.0d) {
                            }
                            if (!isBadRSSI) {
                                currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtBadRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtBadRSSI = 0;
                                }
                                if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        sb.append(" p1");
                                    }
                                }
                            } else if (!isLowRSSI) {
                                currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtLowRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtLowRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                sb.append(" p2");
                            } else if (isHighRSSI) {
                                currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtNotHighRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtNotHighRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                sb.append(" p3");
                            }
                            objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                            objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                            objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                            sb.append(String.format(" ticks %d,%d,%d", objArr2));
                        }
                        if (debugLogging) {
                            rssiStatus = "";
                            if (!isBadRSSI) {
                                rssiStatus = rssiStatus + " badRSSI ";
                            } else if (!isHighRSSI) {
                                rssiStatus = rssiStatus + " highRSSI ";
                            } else if (isLowRSSI) {
                                rssiStatus = rssiStatus + " lowRSSI ";
                            }
                            if (isBadLinkspeed) {
                                rssiStatus = rssiStatus + " lowSpeed ";
                            }
                            str = TAG;
                            append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                            append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                            Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                        }
                        if (wifiInfo.txBadRate >= 1.0d) {
                            i = wifiInfo.linkStuckCount;
                            if (r0 < USER_DISCONNECT_PENALTY) {
                                wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls+=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(" [%d", objArr2));
                            i = wifiInfo.linkStuckCount;
                            if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadLinkspeed) {
                                score -= 4;
                                if (debugLogging) {
                                    Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                }
                            } else if (isGoodLinkspeed) {
                                if (wifiInfo.txSuccessRate > 5.0d) {
                                    score += GOOD_LINKSPEED_BONUS;
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadRSSI) {
                                i = wifiInfo.badRssiCount;
                                if (r0 < MAX_BAD_RSSI_COUNT) {
                                    wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                }
                            } else if (isLowRSSI) {
                                wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                if (wifiInfo.badRssiCount > 0) {
                                    wifiInfo.badRssiCount--;
                                }
                            } else {
                                wifiInfo.badRssiCount = 0;
                                wifiInfo.lowRssiCount = 0;
                            }
                            score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                            }
                            if (isHighRSSI) {
                                score += USER_DISCONNECT_PENALTY;
                                if (debugLogging) {
                                    Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d]", objArr2));
                            objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                            objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                            sb.append(String.format(" brc=%d lrc=%d", objArr2));
                            if (score > 60) {
                                score = 60;
                            }
                            if (score < 0) {
                            }
                            score = 100;
                            globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                            if (globalHwWsm != null) {
                                score = globalHwWsm.resetScoreByInetAccess(100);
                            }
                            if (score != wifiInfo.score) {
                                if (debugLogging) {
                                    Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                }
                                wifiInfo.score = score;
                                if (networkAgent != null) {
                                    networkAgent.sendNetworkScore(score);
                                }
                            }
                            return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                        }
                        if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                            if (wifiInfo.linkStuckCount > 0) {
                                wifiInfo.linkStuckCount--;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls-=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(" [%d", objArr2));
                        i = wifiInfo.linkStuckCount;
                        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadLinkspeed) {
                            score -= 4;
                            if (debugLogging) {
                                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                            }
                        } else if (isGoodLinkspeed) {
                            if (wifiInfo.txSuccessRate > 5.0d) {
                                score += GOOD_LINKSPEED_BONUS;
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadRSSI) {
                            i = wifiInfo.badRssiCount;
                            if (r0 < MAX_BAD_RSSI_COUNT) {
                                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                        } else if (isLowRSSI) {
                            wifiInfo.badRssiCount = 0;
                            wifiInfo.lowRssiCount = 0;
                        } else {
                            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                            if (wifiInfo.badRssiCount > 0) {
                                wifiInfo.badRssiCount--;
                            }
                        }
                        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (debugLogging) {
                            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                        }
                        if (isHighRSSI) {
                            score += USER_DISCONNECT_PENALTY;
                            if (debugLogging) {
                                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d]", objArr2));
                        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                        sb.append(String.format(" brc=%d lrc=%d", objArr2));
                        if (score > 60) {
                            score = 60;
                        }
                        if (score < 0) {
                        }
                        score = 100;
                        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                        if (globalHwWsm != null) {
                            score = globalHwWsm.resetScoreByInetAccess(100);
                        }
                        if (score != wifiInfo.score) {
                            if (debugLogging) {
                                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                            }
                            wifiInfo.score = score;
                            if (networkAgent != null) {
                                networkAgent.sendNetworkScore(score);
                            }
                        }
                        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                    }
                }
                if (is24GHz) {
                    isHighRSSI = false;
                } else {
                    if (wifiInfo.getRssi() < wifiConfigManager.mThresholdSaturatedRssi5.get()) {
                    }
                }
                if (isBadRSSI) {
                    sb.append(" br");
                }
                if (isLowRSSI) {
                    sb.append(" lr");
                }
                if (isHighRSSI) {
                    sb.append(" hr");
                }
                penalizedDueToUserTriggeredDisconnect = 0;
                if (currentConfiguration != null) {
                    if (wifiInfo.txSuccessRate <= 5.0d) {
                    }
                    if (!isBadRSSI) {
                        currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtBadRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtBadRSSI = 0;
                        }
                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                sb.append(" p1");
                            }
                        }
                    } else if (!isLowRSSI) {
                        currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtLowRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtLowRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                        sb.append(" p2");
                    } else if (isHighRSSI) {
                        currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtNotHighRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtNotHighRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                        sb.append(" p3");
                    }
                    objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                    objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                    objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                    sb.append(String.format(" ticks %d,%d,%d", objArr2));
                }
                if (debugLogging) {
                    rssiStatus = "";
                    if (!isBadRSSI) {
                        rssiStatus = rssiStatus + " badRSSI ";
                    } else if (!isHighRSSI) {
                        rssiStatus = rssiStatus + " highRSSI ";
                    } else if (isLowRSSI) {
                        rssiStatus = rssiStatus + " lowRSSI ";
                    }
                    if (isBadLinkspeed) {
                        rssiStatus = rssiStatus + " lowSpeed ";
                    }
                    str = TAG;
                    append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                    append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                    Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                }
                if (wifiInfo.txBadRate >= 1.0d) {
                    i = wifiInfo.linkStuckCount;
                    if (r0 < USER_DISCONNECT_PENALTY) {
                        wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls+=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(" [%d", objArr2));
                    i = wifiInfo.linkStuckCount;
                    if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                        score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadLinkspeed) {
                        score -= 4;
                        if (debugLogging) {
                            Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                        }
                    } else if (isGoodLinkspeed) {
                        if (wifiInfo.txSuccessRate > 5.0d) {
                            score += GOOD_LINKSPEED_BONUS;
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadRSSI) {
                        i = wifiInfo.badRssiCount;
                        if (r0 < MAX_BAD_RSSI_COUNT) {
                            wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        }
                    } else if (isLowRSSI) {
                        wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                        if (wifiInfo.badRssiCount > 0) {
                            wifiInfo.badRssiCount--;
                        }
                    } else {
                        wifiInfo.badRssiCount = 0;
                        wifiInfo.lowRssiCount = 0;
                    }
                    score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                    }
                    if (isHighRSSI) {
                        score += USER_DISCONNECT_PENALTY;
                        if (debugLogging) {
                            Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d]", objArr2));
                    objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                    objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                    sb.append(String.format(" brc=%d lrc=%d", objArr2));
                    if (score > 60) {
                        score = 60;
                    }
                    if (score < 0) {
                    }
                    score = 100;
                    globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                    if (globalHwWsm != null) {
                        score = globalHwWsm.resetScoreByInetAccess(100);
                    }
                    if (score != wifiInfo.score) {
                        if (debugLogging) {
                            Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                        }
                        wifiInfo.score = score;
                        if (networkAgent != null) {
                            networkAgent.sendNetworkScore(score);
                        }
                    }
                    return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                }
                if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                    if (wifiInfo.linkStuckCount > 0) {
                        wifiInfo.linkStuckCount--;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls-=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(" [%d", objArr2));
                i = wifiInfo.linkStuckCount;
                if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                    score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadLinkspeed) {
                    score -= 4;
                    if (debugLogging) {
                        Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                    }
                } else if (isGoodLinkspeed) {
                    if (wifiInfo.txSuccessRate > 5.0d) {
                        score += GOOD_LINKSPEED_BONUS;
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadRSSI) {
                    i = wifiInfo.badRssiCount;
                    if (r0 < MAX_BAD_RSSI_COUNT) {
                        wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                } else if (isLowRSSI) {
                    wifiInfo.badRssiCount = 0;
                    wifiInfo.lowRssiCount = 0;
                } else {
                    wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                    if (wifiInfo.badRssiCount > 0) {
                        wifiInfo.badRssiCount--;
                    }
                }
                score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (debugLogging) {
                    Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                }
                if (isHighRSSI) {
                    score += USER_DISCONNECT_PENALTY;
                    if (debugLogging) {
                        Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d]", objArr2));
                objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                sb.append(String.format(" brc=%d lrc=%d", objArr2));
                if (score > 60) {
                    score = 60;
                }
                if (score < 0) {
                }
                score = 100;
                globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                if (globalHwWsm != null) {
                    score = globalHwWsm.resetScoreByInetAccess(100);
                }
                if (score != wifiInfo.score) {
                    if (debugLogging) {
                        Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                    }
                    wifiInfo.score = score;
                    if (networkAgent != null) {
                        networkAgent.sendNetworkScore(score);
                    }
                }
                return new WifiScoreReport(sb.toString(), badLinkspeedcount);
            }
        }
        if (!is24GHz) {
            if (rssi < wifiConfigManager.mThresholdMinimumRssi5.get()) {
                isBadRSSI = true;
                if (is24GHz) {
                    if (rssi < wifiConfigManager.mThresholdQualifiedRssi24.get()) {
                        isLowRSSI = true;
                        if (is24GHz) {
                            if (rssi >= wifiConfigManager.mThresholdSaturatedRssi24.get()) {
                                isHighRSSI = true;
                                if (isBadRSSI) {
                                    sb.append(" br");
                                }
                                if (isLowRSSI) {
                                    sb.append(" lr");
                                }
                                if (isHighRSSI) {
                                    sb.append(" hr");
                                }
                                penalizedDueToUserTriggeredDisconnect = 0;
                                if (currentConfiguration != null) {
                                    if (wifiInfo.txSuccessRate <= 5.0d) {
                                    }
                                    if (!isBadRSSI) {
                                        currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        i = currentConfiguration.numTicksAtBadRSSI;
                                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                            }
                                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                            }
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                            }
                                            currentConfiguration.numTicksAtBadRSSI = 0;
                                        }
                                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                score = 51;
                                                penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                                sb.append(" p1");
                                            }
                                        }
                                    } else if (!isLowRSSI) {
                                        currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        i = currentConfiguration.numTicksAtLowRSSI;
                                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                            }
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                            }
                                            currentConfiguration.numTicksAtLowRSSI = 0;
                                        }
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                        sb.append(" p2");
                                    } else if (isHighRSSI) {
                                        currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        i = currentConfiguration.numTicksAtNotHighRSSI;
                                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                            }
                                            currentConfiguration.numTicksAtNotHighRSSI = 0;
                                        }
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                        sb.append(" p3");
                                    }
                                    objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                                    objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                                    objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                                    sb.append(String.format(" ticks %d,%d,%d", objArr2));
                                }
                                if (debugLogging) {
                                    rssiStatus = "";
                                    if (!isBadRSSI) {
                                        rssiStatus = rssiStatus + " badRSSI ";
                                    } else if (!isHighRSSI) {
                                        rssiStatus = rssiStatus + " highRSSI ";
                                    } else if (isLowRSSI) {
                                        rssiStatus = rssiStatus + " lowRSSI ";
                                    }
                                    if (isBadLinkspeed) {
                                        rssiStatus = rssiStatus + " lowSpeed ";
                                    }
                                    str = TAG;
                                    append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                                    append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                                    append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                                    append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                                    Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                                }
                                if (wifiInfo.txBadRate >= 1.0d) {
                                    i = wifiInfo.linkStuckCount;
                                    if (r0 < USER_DISCONNECT_PENALTY) {
                                        wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                                    sb.append(String.format(" ls+=%d", objArr2));
                                    if (debugLogging) {
                                        Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(score);
                                    sb.append(String.format(" [%d", objArr2));
                                    i = wifiInfo.linkStuckCount;
                                    if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                        score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(score);
                                    sb.append(String.format(",%d", objArr2));
                                    if (!isBadLinkspeed) {
                                        score -= 4;
                                        if (debugLogging) {
                                            Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                        }
                                    } else if (isGoodLinkspeed) {
                                        if (wifiInfo.txSuccessRate > 5.0d) {
                                            score += GOOD_LINKSPEED_BONUS;
                                        }
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(score);
                                    sb.append(String.format(",%d", objArr2));
                                    if (!isBadRSSI) {
                                        i = wifiInfo.badRssiCount;
                                        if (r0 < MAX_BAD_RSSI_COUNT) {
                                            wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        }
                                    } else if (isLowRSSI) {
                                        wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        if (wifiInfo.badRssiCount > 0) {
                                            wifiInfo.badRssiCount--;
                                        }
                                    } else {
                                        wifiInfo.badRssiCount = 0;
                                        wifiInfo.lowRssiCount = 0;
                                    }
                                    score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(score);
                                    sb.append(String.format(",%d", objArr2));
                                    if (debugLogging) {
                                        Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                                    }
                                    if (isHighRSSI) {
                                        score += USER_DISCONNECT_PENALTY;
                                        if (debugLogging) {
                                            Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                        }
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(score);
                                    sb.append(String.format(",%d]", objArr2));
                                    objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                                    objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                                    sb.append(String.format(" brc=%d lrc=%d", objArr2));
                                    if (score > 60) {
                                        score = 60;
                                    }
                                    if (score < 0) {
                                    }
                                    score = 100;
                                    globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                                    if (globalHwWsm != null) {
                                        score = globalHwWsm.resetScoreByInetAccess(100);
                                    }
                                    if (score != wifiInfo.score) {
                                        if (debugLogging) {
                                            Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                        }
                                        wifiInfo.score = score;
                                        if (networkAgent != null) {
                                            networkAgent.sendNetworkScore(score);
                                        }
                                    }
                                    return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                                }
                                if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                                    if (wifiInfo.linkStuckCount > 0) {
                                        wifiInfo.linkStuckCount--;
                                    }
                                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                                    sb.append(String.format(" ls-=%d", objArr2));
                                    if (debugLogging) {
                                        Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                                    }
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(" [%d", objArr2));
                                i = wifiInfo.linkStuckCount;
                                if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                    score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d", objArr2));
                                if (!isBadLinkspeed) {
                                    score -= 4;
                                    if (debugLogging) {
                                        Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                    }
                                } else if (isGoodLinkspeed) {
                                    if (wifiInfo.txSuccessRate > 5.0d) {
                                        score += GOOD_LINKSPEED_BONUS;
                                    }
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d", objArr2));
                                if (!isBadRSSI) {
                                    i = wifiInfo.badRssiCount;
                                    if (r0 < MAX_BAD_RSSI_COUNT) {
                                        wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                    }
                                } else if (isLowRSSI) {
                                    wifiInfo.badRssiCount = 0;
                                    wifiInfo.lowRssiCount = 0;
                                } else {
                                    wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                    if (wifiInfo.badRssiCount > 0) {
                                        wifiInfo.badRssiCount--;
                                    }
                                }
                                score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d", objArr2));
                                if (debugLogging) {
                                    Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                                }
                                if (isHighRSSI) {
                                    score += USER_DISCONNECT_PENALTY;
                                    if (debugLogging) {
                                        Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                    }
                                }
                                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                                objArr2[0] = Integer.valueOf(score);
                                sb.append(String.format(",%d]", objArr2));
                                objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                                objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                                objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                                sb.append(String.format(" brc=%d lrc=%d", objArr2));
                                if (score > 60) {
                                    score = 60;
                                }
                                if (score < 0) {
                                }
                                score = 100;
                                globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                                if (globalHwWsm != null) {
                                    score = globalHwWsm.resetScoreByInetAccess(100);
                                }
                                if (score != wifiInfo.score) {
                                    if (debugLogging) {
                                        Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                    }
                                    wifiInfo.score = score;
                                    if (networkAgent != null) {
                                        networkAgent.sendNetworkScore(score);
                                    }
                                }
                                return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                            }
                        }
                        if (is24GHz) {
                            if (wifiInfo.getRssi() < wifiConfigManager.mThresholdSaturatedRssi5.get()) {
                            }
                        } else {
                            isHighRSSI = false;
                        }
                        if (isBadRSSI) {
                            sb.append(" br");
                        }
                        if (isLowRSSI) {
                            sb.append(" lr");
                        }
                        if (isHighRSSI) {
                            sb.append(" hr");
                        }
                        penalizedDueToUserTriggeredDisconnect = 0;
                        if (currentConfiguration != null) {
                            if (wifiInfo.txSuccessRate <= 5.0d) {
                            }
                            if (!isBadRSSI) {
                                currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtBadRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtBadRSSI = 0;
                                }
                                if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        sb.append(" p1");
                                    }
                                }
                            } else if (!isLowRSSI) {
                                currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtLowRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtLowRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                sb.append(" p2");
                            } else if (isHighRSSI) {
                                currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtNotHighRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtNotHighRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                sb.append(" p3");
                            }
                            objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                            objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                            objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                            sb.append(String.format(" ticks %d,%d,%d", objArr2));
                        }
                        if (debugLogging) {
                            rssiStatus = "";
                            if (!isBadRSSI) {
                                rssiStatus = rssiStatus + " badRSSI ";
                            } else if (!isHighRSSI) {
                                rssiStatus = rssiStatus + " highRSSI ";
                            } else if (isLowRSSI) {
                                rssiStatus = rssiStatus + " lowRSSI ";
                            }
                            if (isBadLinkspeed) {
                                rssiStatus = rssiStatus + " lowSpeed ";
                            }
                            str = TAG;
                            append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                            append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                            Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                        }
                        if (wifiInfo.txBadRate >= 1.0d) {
                            i = wifiInfo.linkStuckCount;
                            if (r0 < USER_DISCONNECT_PENALTY) {
                                wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls+=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(" [%d", objArr2));
                            i = wifiInfo.linkStuckCount;
                            if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadLinkspeed) {
                                score -= 4;
                                if (debugLogging) {
                                    Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                }
                            } else if (isGoodLinkspeed) {
                                if (wifiInfo.txSuccessRate > 5.0d) {
                                    score += GOOD_LINKSPEED_BONUS;
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadRSSI) {
                                i = wifiInfo.badRssiCount;
                                if (r0 < MAX_BAD_RSSI_COUNT) {
                                    wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                }
                            } else if (isLowRSSI) {
                                wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                if (wifiInfo.badRssiCount > 0) {
                                    wifiInfo.badRssiCount--;
                                }
                            } else {
                                wifiInfo.badRssiCount = 0;
                                wifiInfo.lowRssiCount = 0;
                            }
                            score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                            }
                            if (isHighRSSI) {
                                score += USER_DISCONNECT_PENALTY;
                                if (debugLogging) {
                                    Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d]", objArr2));
                            objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                            objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                            sb.append(String.format(" brc=%d lrc=%d", objArr2));
                            if (score > 60) {
                                score = 60;
                            }
                            if (score < 0) {
                            }
                            score = 100;
                            globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                            if (globalHwWsm != null) {
                                score = globalHwWsm.resetScoreByInetAccess(100);
                            }
                            if (score != wifiInfo.score) {
                                if (debugLogging) {
                                    Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                }
                                wifiInfo.score = score;
                                if (networkAgent != null) {
                                    networkAgent.sendNetworkScore(score);
                                }
                            }
                            return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                        }
                        if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                            if (wifiInfo.linkStuckCount > 0) {
                                wifiInfo.linkStuckCount--;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls-=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(" [%d", objArr2));
                        i = wifiInfo.linkStuckCount;
                        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadLinkspeed) {
                            score -= 4;
                            if (debugLogging) {
                                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                            }
                        } else if (isGoodLinkspeed) {
                            if (wifiInfo.txSuccessRate > 5.0d) {
                                score += GOOD_LINKSPEED_BONUS;
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadRSSI) {
                            i = wifiInfo.badRssiCount;
                            if (r0 < MAX_BAD_RSSI_COUNT) {
                                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                        } else if (isLowRSSI) {
                            wifiInfo.badRssiCount = 0;
                            wifiInfo.lowRssiCount = 0;
                        } else {
                            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                            if (wifiInfo.badRssiCount > 0) {
                                wifiInfo.badRssiCount--;
                            }
                        }
                        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (debugLogging) {
                            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                        }
                        if (isHighRSSI) {
                            score += USER_DISCONNECT_PENALTY;
                            if (debugLogging) {
                                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d]", objArr2));
                        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                        sb.append(String.format(" brc=%d lrc=%d", objArr2));
                        if (score > 60) {
                            score = 60;
                        }
                        if (score < 0) {
                        }
                        score = 100;
                        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                        if (globalHwWsm != null) {
                            score = globalHwWsm.resetScoreByInetAccess(100);
                        }
                        if (score != wifiInfo.score) {
                            if (debugLogging) {
                                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                            }
                            wifiInfo.score = score;
                            if (networkAgent != null) {
                                networkAgent.sendNetworkScore(score);
                            }
                        }
                        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                    }
                }
                if (is24GHz) {
                    isLowRSSI = false;
                } else {
                    if (wifiInfo.getRssi() < wifiConfigManager.mThresholdMinimumRssi5.get()) {
                    }
                }
                if (is24GHz) {
                    if (rssi >= wifiConfigManager.mThresholdSaturatedRssi24.get()) {
                        isHighRSSI = true;
                        if (isBadRSSI) {
                            sb.append(" br");
                        }
                        if (isLowRSSI) {
                            sb.append(" lr");
                        }
                        if (isHighRSSI) {
                            sb.append(" hr");
                        }
                        penalizedDueToUserTriggeredDisconnect = 0;
                        if (currentConfiguration != null) {
                            if (wifiInfo.txSuccessRate <= 5.0d) {
                            }
                            if (!isBadRSSI) {
                                currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtBadRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtBadRSSI = 0;
                                }
                                if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        sb.append(" p1");
                                    }
                                }
                            } else if (!isLowRSSI) {
                                currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtLowRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtLowRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                sb.append(" p2");
                            } else if (isHighRSSI) {
                                currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtNotHighRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtNotHighRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                sb.append(" p3");
                            }
                            objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                            objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                            objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                            sb.append(String.format(" ticks %d,%d,%d", objArr2));
                        }
                        if (debugLogging) {
                            rssiStatus = "";
                            if (!isBadRSSI) {
                                rssiStatus = rssiStatus + " badRSSI ";
                            } else if (!isHighRSSI) {
                                rssiStatus = rssiStatus + " highRSSI ";
                            } else if (isLowRSSI) {
                                rssiStatus = rssiStatus + " lowRSSI ";
                            }
                            if (isBadLinkspeed) {
                                rssiStatus = rssiStatus + " lowSpeed ";
                            }
                            str = TAG;
                            append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                            append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                            Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                        }
                        if (wifiInfo.txBadRate >= 1.0d) {
                            i = wifiInfo.linkStuckCount;
                            if (r0 < USER_DISCONNECT_PENALTY) {
                                wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls+=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(" [%d", objArr2));
                            i = wifiInfo.linkStuckCount;
                            if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadLinkspeed) {
                                score -= 4;
                                if (debugLogging) {
                                    Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                }
                            } else if (isGoodLinkspeed) {
                                if (wifiInfo.txSuccessRate > 5.0d) {
                                    score += GOOD_LINKSPEED_BONUS;
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadRSSI) {
                                i = wifiInfo.badRssiCount;
                                if (r0 < MAX_BAD_RSSI_COUNT) {
                                    wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                }
                            } else if (isLowRSSI) {
                                wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                if (wifiInfo.badRssiCount > 0) {
                                    wifiInfo.badRssiCount--;
                                }
                            } else {
                                wifiInfo.badRssiCount = 0;
                                wifiInfo.lowRssiCount = 0;
                            }
                            score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                            }
                            if (isHighRSSI) {
                                score += USER_DISCONNECT_PENALTY;
                                if (debugLogging) {
                                    Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d]", objArr2));
                            objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                            objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                            sb.append(String.format(" brc=%d lrc=%d", objArr2));
                            if (score > 60) {
                                score = 60;
                            }
                            if (score < 0) {
                            }
                            score = 100;
                            globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                            if (globalHwWsm != null) {
                                score = globalHwWsm.resetScoreByInetAccess(100);
                            }
                            if (score != wifiInfo.score) {
                                if (debugLogging) {
                                    Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                }
                                wifiInfo.score = score;
                                if (networkAgent != null) {
                                    networkAgent.sendNetworkScore(score);
                                }
                            }
                            return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                        }
                        if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                            if (wifiInfo.linkStuckCount > 0) {
                                wifiInfo.linkStuckCount--;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls-=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(" [%d", objArr2));
                        i = wifiInfo.linkStuckCount;
                        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadLinkspeed) {
                            score -= 4;
                            if (debugLogging) {
                                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                            }
                        } else if (isGoodLinkspeed) {
                            if (wifiInfo.txSuccessRate > 5.0d) {
                                score += GOOD_LINKSPEED_BONUS;
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadRSSI) {
                            i = wifiInfo.badRssiCount;
                            if (r0 < MAX_BAD_RSSI_COUNT) {
                                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                        } else if (isLowRSSI) {
                            wifiInfo.badRssiCount = 0;
                            wifiInfo.lowRssiCount = 0;
                        } else {
                            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                            if (wifiInfo.badRssiCount > 0) {
                                wifiInfo.badRssiCount--;
                            }
                        }
                        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (debugLogging) {
                            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                        }
                        if (isHighRSSI) {
                            score += USER_DISCONNECT_PENALTY;
                            if (debugLogging) {
                                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d]", objArr2));
                        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                        sb.append(String.format(" brc=%d lrc=%d", objArr2));
                        if (score > 60) {
                            score = 60;
                        }
                        if (score < 0) {
                        }
                        score = 100;
                        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                        if (globalHwWsm != null) {
                            score = globalHwWsm.resetScoreByInetAccess(100);
                        }
                        if (score != wifiInfo.score) {
                            if (debugLogging) {
                                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                            }
                            wifiInfo.score = score;
                            if (networkAgent != null) {
                                networkAgent.sendNetworkScore(score);
                            }
                        }
                        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                    }
                }
                if (is24GHz) {
                    isHighRSSI = false;
                } else {
                    if (wifiInfo.getRssi() < wifiConfigManager.mThresholdSaturatedRssi5.get()) {
                    }
                }
                if (isBadRSSI) {
                    sb.append(" br");
                }
                if (isLowRSSI) {
                    sb.append(" lr");
                }
                if (isHighRSSI) {
                    sb.append(" hr");
                }
                penalizedDueToUserTriggeredDisconnect = 0;
                if (currentConfiguration != null) {
                    if (wifiInfo.txSuccessRate <= 5.0d) {
                    }
                    if (!isBadRSSI) {
                        currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtBadRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtBadRSSI = 0;
                        }
                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                sb.append(" p1");
                            }
                        }
                    } else if (!isLowRSSI) {
                        currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtLowRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtLowRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                        sb.append(" p2");
                    } else if (isHighRSSI) {
                        currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtNotHighRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtNotHighRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                        sb.append(" p3");
                    }
                    objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                    objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                    objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                    sb.append(String.format(" ticks %d,%d,%d", objArr2));
                }
                if (debugLogging) {
                    rssiStatus = "";
                    if (!isBadRSSI) {
                        rssiStatus = rssiStatus + " badRSSI ";
                    } else if (!isHighRSSI) {
                        rssiStatus = rssiStatus + " highRSSI ";
                    } else if (isLowRSSI) {
                        rssiStatus = rssiStatus + " lowRSSI ";
                    }
                    if (isBadLinkspeed) {
                        rssiStatus = rssiStatus + " lowSpeed ";
                    }
                    str = TAG;
                    append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                    append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                    Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                }
                if (wifiInfo.txBadRate >= 1.0d) {
                    i = wifiInfo.linkStuckCount;
                    if (r0 < USER_DISCONNECT_PENALTY) {
                        wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls+=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(" [%d", objArr2));
                    i = wifiInfo.linkStuckCount;
                    if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                        score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadLinkspeed) {
                        score -= 4;
                        if (debugLogging) {
                            Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                        }
                    } else if (isGoodLinkspeed) {
                        if (wifiInfo.txSuccessRate > 5.0d) {
                            score += GOOD_LINKSPEED_BONUS;
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadRSSI) {
                        i = wifiInfo.badRssiCount;
                        if (r0 < MAX_BAD_RSSI_COUNT) {
                            wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        }
                    } else if (isLowRSSI) {
                        wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                        if (wifiInfo.badRssiCount > 0) {
                            wifiInfo.badRssiCount--;
                        }
                    } else {
                        wifiInfo.badRssiCount = 0;
                        wifiInfo.lowRssiCount = 0;
                    }
                    score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                    }
                    if (isHighRSSI) {
                        score += USER_DISCONNECT_PENALTY;
                        if (debugLogging) {
                            Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d]", objArr2));
                    objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                    objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                    sb.append(String.format(" brc=%d lrc=%d", objArr2));
                    if (score > 60) {
                        score = 60;
                    }
                    if (score < 0) {
                    }
                    score = 100;
                    globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                    if (globalHwWsm != null) {
                        score = globalHwWsm.resetScoreByInetAccess(100);
                    }
                    if (score != wifiInfo.score) {
                        if (debugLogging) {
                            Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                        }
                        wifiInfo.score = score;
                        if (networkAgent != null) {
                            networkAgent.sendNetworkScore(score);
                        }
                    }
                    return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                }
                if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                    if (wifiInfo.linkStuckCount > 0) {
                        wifiInfo.linkStuckCount--;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls-=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(" [%d", objArr2));
                i = wifiInfo.linkStuckCount;
                if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                    score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadLinkspeed) {
                    score -= 4;
                    if (debugLogging) {
                        Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                    }
                } else if (isGoodLinkspeed) {
                    if (wifiInfo.txSuccessRate > 5.0d) {
                        score += GOOD_LINKSPEED_BONUS;
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadRSSI) {
                    i = wifiInfo.badRssiCount;
                    if (r0 < MAX_BAD_RSSI_COUNT) {
                        wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                } else if (isLowRSSI) {
                    wifiInfo.badRssiCount = 0;
                    wifiInfo.lowRssiCount = 0;
                } else {
                    wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                    if (wifiInfo.badRssiCount > 0) {
                        wifiInfo.badRssiCount--;
                    }
                }
                score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (debugLogging) {
                    Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                }
                if (isHighRSSI) {
                    score += USER_DISCONNECT_PENALTY;
                    if (debugLogging) {
                        Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d]", objArr2));
                objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                sb.append(String.format(" brc=%d lrc=%d", objArr2));
                if (score > 60) {
                    score = 60;
                }
                if (score < 0) {
                }
                score = 100;
                globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                if (globalHwWsm != null) {
                    score = globalHwWsm.resetScoreByInetAccess(100);
                }
                if (score != wifiInfo.score) {
                    if (debugLogging) {
                        Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                    }
                    wifiInfo.score = score;
                    if (networkAgent != null) {
                        networkAgent.sendNetworkScore(score);
                    }
                }
                return new WifiScoreReport(sb.toString(), badLinkspeedcount);
            }
        }
        isBadRSSI = false;
        if (is24GHz) {
            if (rssi < wifiConfigManager.mThresholdQualifiedRssi24.get()) {
                isLowRSSI = true;
                if (is24GHz) {
                    if (rssi >= wifiConfigManager.mThresholdSaturatedRssi24.get()) {
                        isHighRSSI = true;
                        if (isBadRSSI) {
                            sb.append(" br");
                        }
                        if (isLowRSSI) {
                            sb.append(" lr");
                        }
                        if (isHighRSSI) {
                            sb.append(" hr");
                        }
                        penalizedDueToUserTriggeredDisconnect = 0;
                        if (currentConfiguration != null) {
                            if (wifiInfo.txSuccessRate <= 5.0d) {
                            }
                            if (!isBadRSSI) {
                                currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtBadRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtBadRSSI = 0;
                                }
                                if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        score = 51;
                                        penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                        sb.append(" p1");
                                    }
                                }
                            } else if (!isLowRSSI) {
                                currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtLowRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                                    }
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtLowRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                                sb.append(" p2");
                            } else if (isHighRSSI) {
                                currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                i = currentConfiguration.numTicksAtNotHighRSSI;
                                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                                    }
                                    currentConfiguration.numTicksAtNotHighRSSI = 0;
                                }
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                                sb.append(" p3");
                            }
                            objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                            objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                            objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                            sb.append(String.format(" ticks %d,%d,%d", objArr2));
                        }
                        if (debugLogging) {
                            rssiStatus = "";
                            if (!isBadRSSI) {
                                rssiStatus = rssiStatus + " badRSSI ";
                            } else if (!isHighRSSI) {
                                rssiStatus = rssiStatus + " highRSSI ";
                            } else if (isLowRSSI) {
                                rssiStatus = rssiStatus + " lowRSSI ";
                            }
                            if (isBadLinkspeed) {
                                rssiStatus = rssiStatus + " lowSpeed ";
                            }
                            str = TAG;
                            append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                            append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                            append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                            Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                        }
                        if (wifiInfo.txBadRate >= 1.0d) {
                            i = wifiInfo.linkStuckCount;
                            if (r0 < USER_DISCONNECT_PENALTY) {
                                wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls+=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(" [%d", objArr2));
                            i = wifiInfo.linkStuckCount;
                            if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                                score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadLinkspeed) {
                                score -= 4;
                                if (debugLogging) {
                                    Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                                }
                            } else if (isGoodLinkspeed) {
                                if (wifiInfo.txSuccessRate > 5.0d) {
                                    score += GOOD_LINKSPEED_BONUS;
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (!isBadRSSI) {
                                i = wifiInfo.badRssiCount;
                                if (r0 < MAX_BAD_RSSI_COUNT) {
                                    wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                                }
                            } else if (isLowRSSI) {
                                wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                if (wifiInfo.badRssiCount > 0) {
                                    wifiInfo.badRssiCount--;
                                }
                            } else {
                                wifiInfo.badRssiCount = 0;
                                wifiInfo.lowRssiCount = 0;
                            }
                            score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                            }
                            if (isHighRSSI) {
                                score += USER_DISCONNECT_PENALTY;
                                if (debugLogging) {
                                    Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                                }
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(score);
                            sb.append(String.format(",%d]", objArr2));
                            objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                            objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                            sb.append(String.format(" brc=%d lrc=%d", objArr2));
                            if (score > 60) {
                                score = 60;
                            }
                            if (score < 0) {
                            }
                            score = 100;
                            globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                            if (globalHwWsm != null) {
                                score = globalHwWsm.resetScoreByInetAccess(100);
                            }
                            if (score != wifiInfo.score) {
                                if (debugLogging) {
                                    Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                                }
                                wifiInfo.score = score;
                                if (networkAgent != null) {
                                    networkAgent.sendNetworkScore(score);
                                }
                            }
                            return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                        }
                        if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                            if (wifiInfo.linkStuckCount > 0) {
                                wifiInfo.linkStuckCount--;
                            }
                            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                            sb.append(String.format(" ls-=%d", objArr2));
                            if (debugLogging) {
                                Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(" [%d", objArr2));
                        i = wifiInfo.linkStuckCount;
                        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadLinkspeed) {
                            score -= 4;
                            if (debugLogging) {
                                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                            }
                        } else if (isGoodLinkspeed) {
                            if (wifiInfo.txSuccessRate > 5.0d) {
                                score += GOOD_LINKSPEED_BONUS;
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (!isBadRSSI) {
                            i = wifiInfo.badRssiCount;
                            if (r0 < MAX_BAD_RSSI_COUNT) {
                                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                            }
                        } else if (isLowRSSI) {
                            wifiInfo.badRssiCount = 0;
                            wifiInfo.lowRssiCount = 0;
                        } else {
                            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                            if (wifiInfo.badRssiCount > 0) {
                                wifiInfo.badRssiCount--;
                            }
                        }
                        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d", objArr2));
                        if (debugLogging) {
                            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                        }
                        if (isHighRSSI) {
                            score += USER_DISCONNECT_PENALTY;
                            if (debugLogging) {
                                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                            }
                        }
                        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                        objArr2[0] = Integer.valueOf(score);
                        sb.append(String.format(",%d]", objArr2));
                        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                        sb.append(String.format(" brc=%d lrc=%d", objArr2));
                        if (score > 60) {
                            score = 60;
                        }
                        if (score < 0) {
                        }
                        score = 100;
                        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                        if (globalHwWsm != null) {
                            score = globalHwWsm.resetScoreByInetAccess(100);
                        }
                        if (score != wifiInfo.score) {
                            if (debugLogging) {
                                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                            }
                            wifiInfo.score = score;
                            if (networkAgent != null) {
                                networkAgent.sendNetworkScore(score);
                            }
                        }
                        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                    }
                }
                if (is24GHz) {
                    if (wifiInfo.getRssi() < wifiConfigManager.mThresholdSaturatedRssi5.get()) {
                    }
                } else {
                    isHighRSSI = false;
                }
                if (isBadRSSI) {
                    sb.append(" br");
                }
                if (isLowRSSI) {
                    sb.append(" lr");
                }
                if (isHighRSSI) {
                    sb.append(" hr");
                }
                penalizedDueToUserTriggeredDisconnect = 0;
                if (currentConfiguration != null) {
                    if (wifiInfo.txSuccessRate <= 5.0d) {
                    }
                    if (!isBadRSSI) {
                        currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtBadRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtBadRSSI = 0;
                        }
                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                sb.append(" p1");
                            }
                        }
                    } else if (!isLowRSSI) {
                        currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtLowRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtLowRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                        sb.append(" p2");
                    } else if (isHighRSSI) {
                        currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtNotHighRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtNotHighRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                        sb.append(" p3");
                    }
                    objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                    objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                    objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                    sb.append(String.format(" ticks %d,%d,%d", objArr2));
                }
                if (debugLogging) {
                    rssiStatus = "";
                    if (!isBadRSSI) {
                        rssiStatus = rssiStatus + " badRSSI ";
                    } else if (!isHighRSSI) {
                        rssiStatus = rssiStatus + " highRSSI ";
                    } else if (isLowRSSI) {
                        rssiStatus = rssiStatus + " lowRSSI ";
                    }
                    if (isBadLinkspeed) {
                        rssiStatus = rssiStatus + " lowSpeed ";
                    }
                    str = TAG;
                    append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                    append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                    Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                }
                if (wifiInfo.txBadRate >= 1.0d) {
                    i = wifiInfo.linkStuckCount;
                    if (r0 < USER_DISCONNECT_PENALTY) {
                        wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls+=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(" [%d", objArr2));
                    i = wifiInfo.linkStuckCount;
                    if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                        score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadLinkspeed) {
                        score -= 4;
                        if (debugLogging) {
                            Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                        }
                    } else if (isGoodLinkspeed) {
                        if (wifiInfo.txSuccessRate > 5.0d) {
                            score += GOOD_LINKSPEED_BONUS;
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadRSSI) {
                        i = wifiInfo.badRssiCount;
                        if (r0 < MAX_BAD_RSSI_COUNT) {
                            wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        }
                    } else if (isLowRSSI) {
                        wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                        if (wifiInfo.badRssiCount > 0) {
                            wifiInfo.badRssiCount--;
                        }
                    } else {
                        wifiInfo.badRssiCount = 0;
                        wifiInfo.lowRssiCount = 0;
                    }
                    score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                    }
                    if (isHighRSSI) {
                        score += USER_DISCONNECT_PENALTY;
                        if (debugLogging) {
                            Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d]", objArr2));
                    objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                    objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                    sb.append(String.format(" brc=%d lrc=%d", objArr2));
                    if (score > 60) {
                        score = 60;
                    }
                    if (score < 0) {
                    }
                    score = 100;
                    globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                    if (globalHwWsm != null) {
                        score = globalHwWsm.resetScoreByInetAccess(100);
                    }
                    if (score != wifiInfo.score) {
                        if (debugLogging) {
                            Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                        }
                        wifiInfo.score = score;
                        if (networkAgent != null) {
                            networkAgent.sendNetworkScore(score);
                        }
                    }
                    return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                }
                if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                    if (wifiInfo.linkStuckCount > 0) {
                        wifiInfo.linkStuckCount--;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls-=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(" [%d", objArr2));
                i = wifiInfo.linkStuckCount;
                if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                    score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadLinkspeed) {
                    score -= 4;
                    if (debugLogging) {
                        Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                    }
                } else if (isGoodLinkspeed) {
                    if (wifiInfo.txSuccessRate > 5.0d) {
                        score += GOOD_LINKSPEED_BONUS;
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadRSSI) {
                    i = wifiInfo.badRssiCount;
                    if (r0 < MAX_BAD_RSSI_COUNT) {
                        wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                } else if (isLowRSSI) {
                    wifiInfo.badRssiCount = 0;
                    wifiInfo.lowRssiCount = 0;
                } else {
                    wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                    if (wifiInfo.badRssiCount > 0) {
                        wifiInfo.badRssiCount--;
                    }
                }
                score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (debugLogging) {
                    Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                }
                if (isHighRSSI) {
                    score += USER_DISCONNECT_PENALTY;
                    if (debugLogging) {
                        Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d]", objArr2));
                objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                sb.append(String.format(" brc=%d lrc=%d", objArr2));
                if (score > 60) {
                    score = 60;
                }
                if (score < 0) {
                }
                score = 100;
                globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                if (globalHwWsm != null) {
                    score = globalHwWsm.resetScoreByInetAccess(100);
                }
                if (score != wifiInfo.score) {
                    if (debugLogging) {
                        Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                    }
                    wifiInfo.score = score;
                    if (networkAgent != null) {
                        networkAgent.sendNetworkScore(score);
                    }
                }
                return new WifiScoreReport(sb.toString(), badLinkspeedcount);
            }
        }
        if (is24GHz) {
            if (wifiInfo.getRssi() < wifiConfigManager.mThresholdMinimumRssi5.get()) {
            }
        } else {
            isLowRSSI = false;
        }
        if (is24GHz) {
            if (rssi >= wifiConfigManager.mThresholdSaturatedRssi24.get()) {
                isHighRSSI = true;
                if (isBadRSSI) {
                    sb.append(" br");
                }
                if (isLowRSSI) {
                    sb.append(" lr");
                }
                if (isHighRSSI) {
                    sb.append(" hr");
                }
                penalizedDueToUserTriggeredDisconnect = 0;
                if (currentConfiguration != null) {
                    if (wifiInfo.txSuccessRate <= 5.0d) {
                    }
                    if (!isBadRSSI) {
                        currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtBadRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtBadRSSI = 0;
                        }
                        if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                score = 51;
                                penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                                sb.append(" p1");
                            }
                        }
                    } else if (!isLowRSSI) {
                        currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtLowRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                            }
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtLowRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                        sb.append(" p2");
                    } else if (isHighRSSI) {
                        currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        i = currentConfiguration.numTicksAtNotHighRSSI;
                        if (r0 > MIN_NUM_TICKS_AT_STATE) {
                            if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                                currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                            }
                            currentConfiguration.numTicksAtNotHighRSSI = 0;
                        }
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                        sb.append(" p3");
                    }
                    objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
                    objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
                    objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
                    sb.append(String.format(" ticks %d,%d,%d", objArr2));
                }
                if (debugLogging) {
                    rssiStatus = "";
                    if (!isBadRSSI) {
                        rssiStatus = rssiStatus + " badRSSI ";
                    } else if (!isHighRSSI) {
                        rssiStatus = rssiStatus + " highRSSI ";
                    } else if (isLowRSSI) {
                        rssiStatus = rssiStatus + " lowRSSI ";
                    }
                    if (isBadLinkspeed) {
                        rssiStatus = rssiStatus + " lowSpeed ";
                    }
                    str = TAG;
                    append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txBadRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
                    append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
                    append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
                    objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
                    Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
                }
                if (wifiInfo.txBadRate >= 1.0d) {
                    i = wifiInfo.linkStuckCount;
                    if (r0 < USER_DISCONNECT_PENALTY) {
                        wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls+=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(" [%d", objArr2));
                    i = wifiInfo.linkStuckCount;
                    if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                        score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadLinkspeed) {
                        score -= 4;
                        if (debugLogging) {
                            Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                        }
                    } else if (isGoodLinkspeed) {
                        if (wifiInfo.txSuccessRate > 5.0d) {
                            score += GOOD_LINKSPEED_BONUS;
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (!isBadRSSI) {
                        i = wifiInfo.badRssiCount;
                        if (r0 < MAX_BAD_RSSI_COUNT) {
                            wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                        }
                    } else if (isLowRSSI) {
                        wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                        if (wifiInfo.badRssiCount > 0) {
                            wifiInfo.badRssiCount--;
                        }
                    } else {
                        wifiInfo.badRssiCount = 0;
                        wifiInfo.lowRssiCount = 0;
                    }
                    score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                    }
                    if (isHighRSSI) {
                        score += USER_DISCONNECT_PENALTY;
                        if (debugLogging) {
                            Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                        }
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(score);
                    sb.append(String.format(",%d]", objArr2));
                    objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                    objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                    objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                    sb.append(String.format(" brc=%d lrc=%d", objArr2));
                    if (score > 60) {
                        score = 60;
                    }
                    if (score < 0) {
                    }
                    score = 100;
                    globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                    if (globalHwWsm != null) {
                        score = globalHwWsm.resetScoreByInetAccess(100);
                    }
                    if (score != wifiInfo.score) {
                        if (debugLogging) {
                            Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                        }
                        wifiInfo.score = score;
                        if (networkAgent != null) {
                            networkAgent.sendNetworkScore(score);
                        }
                    }
                    return new WifiScoreReport(sb.toString(), badLinkspeedcount);
                }
                if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
                    if (wifiInfo.linkStuckCount > 0) {
                        wifiInfo.linkStuckCount--;
                    }
                    objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                    objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
                    sb.append(String.format(" ls-=%d", objArr2));
                    if (debugLogging) {
                        Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(" [%d", objArr2));
                i = wifiInfo.linkStuckCount;
                if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                    score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadLinkspeed) {
                    score -= 4;
                    if (debugLogging) {
                        Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                    }
                } else if (isGoodLinkspeed) {
                    if (wifiInfo.txSuccessRate > 5.0d) {
                        score += GOOD_LINKSPEED_BONUS;
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (!isBadRSSI) {
                    i = wifiInfo.badRssiCount;
                    if (r0 < MAX_BAD_RSSI_COUNT) {
                        wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                    }
                } else if (isLowRSSI) {
                    wifiInfo.badRssiCount = 0;
                    wifiInfo.lowRssiCount = 0;
                } else {
                    wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                    if (wifiInfo.badRssiCount > 0) {
                        wifiInfo.badRssiCount--;
                    }
                }
                score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d", objArr2));
                if (debugLogging) {
                    Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
                }
                if (isHighRSSI) {
                    score += USER_DISCONNECT_PENALTY;
                    if (debugLogging) {
                        Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                    }
                }
                objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
                objArr2[0] = Integer.valueOf(score);
                sb.append(String.format(",%d]", objArr2));
                objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
                objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
                objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
                sb.append(String.format(" brc=%d lrc=%d", objArr2));
                if (score > 60) {
                    score = 60;
                }
                if (score < 0) {
                }
                score = 100;
                globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
                if (globalHwWsm != null) {
                    score = globalHwWsm.resetScoreByInetAccess(100);
                }
                if (score != wifiInfo.score) {
                    if (debugLogging) {
                        Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                    }
                    wifiInfo.score = score;
                    if (networkAgent != null) {
                        networkAgent.sendNetworkScore(score);
                    }
                }
                return new WifiScoreReport(sb.toString(), badLinkspeedcount);
            }
        }
        if (is24GHz) {
            isHighRSSI = false;
        } else {
            if (wifiInfo.getRssi() < wifiConfigManager.mThresholdSaturatedRssi5.get()) {
            }
        }
        if (isBadRSSI) {
            sb.append(" br");
        }
        if (isLowRSSI) {
            sb.append(" lr");
        }
        if (isHighRSSI) {
            sb.append(" hr");
        }
        penalizedDueToUserTriggeredDisconnect = 0;
        if (currentConfiguration != null) {
            if (wifiInfo.txSuccessRate <= 5.0d) {
            }
            if (!isBadRSSI) {
                currentConfiguration.numTicksAtBadRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                i = currentConfiguration.numTicksAtBadRSSI;
                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                    if (currentConfiguration.numUserTriggeredWifiDisableBadRSSI > 0) {
                        currentConfiguration.numUserTriggeredWifiDisableBadRSSI--;
                    }
                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                    }
                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                    }
                    currentConfiguration.numTicksAtBadRSSI = 0;
                }
                if (wifiConfigManager.mEnableWifiCellularHandoverUserTriggeredAdjustment) {
                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                        score = 51;
                        penalizedDueToUserTriggeredDisconnect = MIN_SUSTAINED_LINK_STUCK_COUNT;
                        sb.append(" p1");
                    }
                }
            } else if (!isLowRSSI) {
                currentConfiguration.numTicksAtLowRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                i = currentConfiguration.numTicksAtLowRSSI;
                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                    if (currentConfiguration.numUserTriggeredWifiDisableLowRSSI > 0) {
                        currentConfiguration.numUserTriggeredWifiDisableLowRSSI--;
                    }
                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                    }
                    currentConfiguration.numTicksAtLowRSSI = 0;
                }
                score = 51;
                penalizedDueToUserTriggeredDisconnect = SCAN_CACHE_COUNT_PENALTY;
                sb.append(" p2");
            } else if (isHighRSSI) {
                currentConfiguration.numTicksAtNotHighRSSI += MIN_SUSTAINED_LINK_STUCK_COUNT;
                i = currentConfiguration.numTicksAtNotHighRSSI;
                if (r0 > MIN_NUM_TICKS_AT_STATE) {
                    if (currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI > 0) {
                        currentConfiguration.numUserTriggeredWifiDisableNotHighRSSI--;
                    }
                    currentConfiguration.numTicksAtNotHighRSSI = 0;
                }
                score = 51;
                penalizedDueToUserTriggeredDisconnect = MAX_SUCCESS_COUNT_OF_STUCK_LINK;
                sb.append(" p3");
            }
            objArr2 = new Object[MAX_SUCCESS_COUNT_OF_STUCK_LINK];
            objArr2[0] = Integer.valueOf(currentConfiguration.numTicksAtBadRSSI);
            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(currentConfiguration.numTicksAtLowRSSI);
            objArr2[SCAN_CACHE_COUNT_PENALTY] = Integer.valueOf(currentConfiguration.numTicksAtNotHighRSSI);
            sb.append(String.format(" ticks %d,%d,%d", objArr2));
        }
        if (debugLogging) {
            rssiStatus = "";
            if (!isBadRSSI) {
                rssiStatus = rssiStatus + " badRSSI ";
            } else if (!isHighRSSI) {
                rssiStatus = rssiStatus + " highRSSI ";
            } else if (isLowRSSI) {
                rssiStatus = rssiStatus + " lowRSSI ";
            }
            if (isBadLinkspeed) {
                rssiStatus = rssiStatus + " lowSpeed ";
            }
            str = TAG;
            append = new StringBuilder().append("calculateWifiScore freq=").append(Integer.toString(wifiInfo.getFrequency())).append(" speed=").append(Integer.toString(wifiInfo.getLinkSpeed())).append(" score=").append(Integer.toString(wifiInfo.score)).append(rssiStatus).append(" -> txbadrate=");
            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr[0] = Double.valueOf(wifiInfo.txBadRate);
            append = append.append(String.format("%.2f", objArr)).append(" txgoodrate=");
            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr[0] = Double.valueOf(wifiInfo.txSuccessRate);
            append = append.append(String.format("%.2f", objArr)).append(" txretriesrate=");
            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr[0] = Double.valueOf(wifiInfo.txRetriesRate);
            append = append.append(String.format("%.2f", objArr)).append(" rxrate=");
            objArr = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr[0] = Double.valueOf(wifiInfo.rxSuccessRate);
            Log.d(str, append.append(String.format("%.2f", objArr)).append(" userTriggerdPenalty").append(penalizedDueToUserTriggeredDisconnect).toString());
        }
        if (wifiInfo.txBadRate >= 1.0d) {
            i = wifiInfo.linkStuckCount;
            if (r0 < USER_DISCONNECT_PENALTY) {
                wifiInfo.linkStuckCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
            }
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
            sb.append(String.format(" ls+=%d", objArr2));
            if (debugLogging) {
                Log.d(TAG, " bad link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
            }
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(score);
            sb.append(String.format(" [%d", objArr2));
            i = wifiInfo.linkStuckCount;
            if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
                score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
            }
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(score);
            sb.append(String.format(",%d", objArr2));
            if (!isBadLinkspeed) {
                score -= 4;
                if (debugLogging) {
                    Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
                }
            } else if (isGoodLinkspeed) {
                if (wifiInfo.txSuccessRate > 5.0d) {
                    score += GOOD_LINKSPEED_BONUS;
                }
            }
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(score);
            sb.append(String.format(",%d", objArr2));
            if (!isBadRSSI) {
                i = wifiInfo.badRssiCount;
                if (r0 < MAX_BAD_RSSI_COUNT) {
                    wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
                }
            } else if (isLowRSSI) {
                wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
                if (wifiInfo.badRssiCount > 0) {
                    wifiInfo.badRssiCount--;
                }
            } else {
                wifiInfo.badRssiCount = 0;
                wifiInfo.lowRssiCount = 0;
            }
            score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(score);
            sb.append(String.format(",%d", objArr2));
            if (debugLogging) {
                Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
            }
            if (isHighRSSI) {
                score += USER_DISCONNECT_PENALTY;
                if (debugLogging) {
                    Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
                }
            }
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(score);
            sb.append(String.format(",%d]", objArr2));
            objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
            objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
            objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
            sb.append(String.format(" brc=%d lrc=%d", objArr2));
            if (score > 60) {
                score = 60;
            }
            if (score < 0) {
            }
            score = 100;
            globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
            if (globalHwWsm != null) {
                score = globalHwWsm.resetScoreByInetAccess(100);
            }
            if (score != wifiInfo.score) {
                if (debugLogging) {
                    Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
                }
                wifiInfo.score = score;
                if (networkAgent != null) {
                    networkAgent.sendNetworkScore(score);
                }
            }
            return new WifiScoreReport(sb.toString(), badLinkspeedcount);
        }
        if (wifiInfo.txBadRate < MIN_TX_RATE_FOR_WORKING_LINK) {
            if (wifiInfo.linkStuckCount > 0) {
                wifiInfo.linkStuckCount--;
            }
            objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
            objArr2[0] = Integer.valueOf(wifiInfo.linkStuckCount);
            sb.append(String.format(" ls-=%d", objArr2));
            if (debugLogging) {
                Log.d(TAG, " good link -> stuck count =" + Integer.toString(wifiInfo.linkStuckCount));
            }
        }
        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
        objArr2[0] = Integer.valueOf(score);
        sb.append(String.format(" [%d", objArr2));
        i = wifiInfo.linkStuckCount;
        if (r0 > MIN_SUSTAINED_LINK_STUCK_COUNT) {
            score -= (wifiInfo.linkStuckCount - 1) * SCAN_CACHE_COUNT_PENALTY;
        }
        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
        objArr2[0] = Integer.valueOf(score);
        sb.append(String.format(",%d", objArr2));
        if (!isBadLinkspeed) {
            score -= 4;
            if (debugLogging) {
                Log.d(TAG, " isBadLinkspeed   ---> count=" + badLinkspeedcount + " score=" + Integer.toString(score));
            }
        } else if (isGoodLinkspeed) {
            if (wifiInfo.txSuccessRate > 5.0d) {
                score += GOOD_LINKSPEED_BONUS;
            }
        }
        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
        objArr2[0] = Integer.valueOf(score);
        sb.append(String.format(",%d", objArr2));
        if (!isBadRSSI) {
            i = wifiInfo.badRssiCount;
            if (r0 < MAX_BAD_RSSI_COUNT) {
                wifiInfo.badRssiCount += MIN_SUSTAINED_LINK_STUCK_COUNT;
            }
        } else if (isLowRSSI) {
            wifiInfo.badRssiCount = 0;
            wifiInfo.lowRssiCount = 0;
        } else {
            wifiInfo.lowRssiCount = MIN_SUSTAINED_LINK_STUCK_COUNT;
            if (wifiInfo.badRssiCount > 0) {
                wifiInfo.badRssiCount--;
            }
        }
        score -= (wifiInfo.badRssiCount * SCAN_CACHE_COUNT_PENALTY) + wifiInfo.lowRssiCount;
        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
        objArr2[0] = Integer.valueOf(score);
        sb.append(String.format(",%d", objArr2));
        if (debugLogging) {
            Log.d(TAG, " badRSSI count" + Integer.toString(wifiInfo.badRssiCount) + " lowRSSI count" + Integer.toString(wifiInfo.lowRssiCount) + " --> score " + Integer.toString(score));
        }
        if (isHighRSSI) {
            score += USER_DISCONNECT_PENALTY;
            if (debugLogging) {
                Log.d(TAG, " isHighRSSI       ---> score=" + Integer.toString(score));
            }
        }
        objArr2 = new Object[MIN_SUSTAINED_LINK_STUCK_COUNT];
        objArr2[0] = Integer.valueOf(score);
        sb.append(String.format(",%d]", objArr2));
        objArr2 = new Object[SCAN_CACHE_COUNT_PENALTY];
        objArr2[0] = Integer.valueOf(wifiInfo.badRssiCount);
        objArr2[MIN_SUSTAINED_LINK_STUCK_COUNT] = Integer.valueOf(wifiInfo.lowRssiCount);
        sb.append(String.format(" brc=%d lrc=%d", objArr2));
        if (score > 60) {
            score = 60;
        }
        if (score < 0) {
        }
        score = 100;
        globalHwWsm = HwWifiServiceFactory.getHwWifiServiceManager().getGlobalHwWifiStateMachine();
        if (globalHwWsm != null) {
            score = globalHwWsm.resetScoreByInetAccess(100);
        }
        if (score != wifiInfo.score) {
            if (debugLogging) {
                Log.d(TAG, "calculateWifiScore() report new score " + Integer.toString(score));
            }
            wifiInfo.score = score;
            if (networkAgent != null) {
                networkAgent.sendNetworkScore(score);
            }
        }
        return new WifiScoreReport(sb.toString(), badLinkspeedcount);
    }
}
