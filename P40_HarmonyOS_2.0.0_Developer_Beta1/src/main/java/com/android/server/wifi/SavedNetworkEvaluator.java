package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.telephony.SubscriptionManager;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.HwServiceFactory;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.hwUtil.ScanResultUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.TelephonyUtil;
import huawei.cust.HwCustUtils;
import java.util.Iterator;
import java.util.List;

public class SavedNetworkEvaluator extends AbsSavedNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
    @VisibleForTesting
    public static final int LAST_SELECTION_AWARD_DECAY_MSEC = 60000;
    private static final String NAME = "SavedNetworkEvaluator";
    private final int mBand5GHzAward;
    private final Clock mClock;
    private final WifiConnectivityHelper mConnectivityHelper;
    private Context mContext;
    HwCustWifiAutoJoinController mCust = ((HwCustWifiAutoJoinController) HwCustUtils.createObj(HwCustWifiAutoJoinController.class, new Object[0]));
    private final int mLastSelectionAward;
    private final LocalLog mLocalLog;
    private final int mRssiScoreOffset;
    private final int mRssiScoreSlope;
    private final int mSameBssidAward;
    private final int mSameNetworkAward;
    private final ScoringParams mScoringParams;
    private final int mSecurityAward;
    private final SubscriptionManager mSubscriptionManager;
    private final WifiConfigManager mWifiConfigManager;

    public SavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiConnectivityHelper connectivityHelper, SubscriptionManager subscriptionManager) {
        this.mScoringParams = scoringParams;
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mConnectivityHelper = connectivityHelper;
        this.mSubscriptionManager = subscriptionManager;
        this.mContext = context;
        this.mRssiScoreSlope = context.getResources().getInteger(17694919);
        this.mRssiScoreOffset = context.getResources().getInteger(17694918);
        this.mSameBssidAward = context.getResources().getInteger(17694920);
        this.mSameNetworkAward = context.getResources().getInteger(17694930);
        this.mLastSelectionAward = context.getResources().getInteger(17694916);
        this.mSecurityAward = context.getResources().getInteger(17694921);
        this.mBand5GHzAward = context.getResources().getInteger(17694913);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public int getId() {
        return 0;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public String getName() {
        return NAME;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public void update(List<ScanDetail> list) {
    }

    private int calculateBssidScore(ScanResult scanResult, WifiConfiguration network, WifiConfiguration currentNetwork, String currentBssid, StringBuffer sbuf) {
        boolean is5GHz = scanResult.is5GHz();
        sbuf.append("[ ");
        sbuf.append(StringUtilEx.safeDisplaySsid(scanResult.SSID));
        sbuf.append(" ");
        sbuf.append(ScanResultUtilEx.getConfusedBssid(scanResult.BSSID));
        sbuf.append(" RSSI:");
        sbuf.append(scanResult.level);
        sbuf.append(" ] ");
        int score = 0 + ((this.mRssiScoreOffset + Math.min(scanResult.level, this.mScoringParams.getGoodRssi(scanResult.frequency))) * this.mRssiScoreSlope);
        sbuf.append(" RSSI score: ");
        sbuf.append(score);
        sbuf.append(",");
        if (is5GHz) {
            score += this.mBand5GHzAward;
            sbuf.append(" 5GHz bonus: ");
            sbuf.append(this.mBand5GHzAward);
            sbuf.append(",");
        }
        int lastUserSelectedNetworkId = this.mWifiConfigManager.getLastSelectedNetwork();
        if (lastUserSelectedNetworkId != -1 && lastUserSelectedNetworkId == network.networkId) {
            long timeDifference = this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = Math.max(this.mLastSelectionAward - ((int) (timeDifference / 60000)), 0);
                score += bonus;
                sbuf.append(" User selection ");
                sbuf.append(timeDifference);
                sbuf.append(" ms ago, bonus: ");
                sbuf.append(bonus);
                sbuf.append(",");
            }
        }
        if (currentNetwork != null && network.networkId == currentNetwork.networkId) {
            score += this.mSameNetworkAward;
            sbuf.append(" Same network bonus: ");
            sbuf.append(this.mSameNetworkAward);
            sbuf.append(",");
            if (this.mConnectivityHelper.isFirmwareRoamingSupported() && currentBssid != null && !currentBssid.equals(scanResult.BSSID)) {
                score += this.mSameBssidAward;
                sbuf.append(" Equivalent BSSID bonus: ");
                sbuf.append(this.mSameBssidAward);
                sbuf.append(",");
            }
        }
        if (currentBssid != null && currentBssid.equals(scanResult.BSSID)) {
            score += this.mSameBssidAward;
            sbuf.append(" Same BSSID bonus: ");
            sbuf.append(this.mSameBssidAward);
            sbuf.append(",");
        }
        if (!WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            score += this.mSecurityAward;
            sbuf.append(" Secure network bonus: ");
            sbuf.append(this.mSecurityAward);
            sbuf.append(",");
        }
        sbuf.append(" ## Total score: ");
        sbuf.append(score);
        sbuf.append("\n");
        return score;
    }

    @Override // com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator
    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, WifiNetworkSelector.NetworkEvaluator.OnConnectableListener onConnectableListener) {
        ScanResult scanResultCandidate;
        WifiConfiguration candidate;
        Iterator<ScanDetail> it;
        WifiConfiguration network;
        WifiConfiguration candidate2;
        ScanResult scanResultCandidate2;
        WifiConfiguration network2;
        char c;
        int i;
        String keys = this.mConnectivityHelper.mCurrentScanKeys;
        StringBuffer savedScan = new StringBuffer();
        StringBuffer scoreHistory = new StringBuffer();
        resetHwSelectedCandidates();
        Iterator<ScanDetail> it2 = scanDetails.iterator();
        int highestScore = Integer.MIN_VALUE;
        ScanResult scanResultCandidate3 = null;
        WifiConfiguration candidate3 = null;
        while (it2.hasNext()) {
            ScanDetail scanDetail = it2.next();
            ScanResult scanResult = scanDetail.getScanResult();
            WifiConfiguration network3 = this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail);
            if (network3 == null) {
                it = it2;
            } else {
                savedScan.append(StringUtilEx.safeDisplaySsid(scanResult.SSID) + " / ");
                HwCustWifiAutoJoinController hwCustWifiAutoJoinController = this.mCust;
                if (hwCustWifiAutoJoinController == null || !hwCustWifiAutoJoinController.isWifiAutoJoinPriority(this.mContext)) {
                    if (network3.isPasspoint()) {
                        it = it2;
                        i = 1;
                        c = 0;
                        network2 = network3;
                    } else if (network3.isEphemeral()) {
                        it = it2;
                        i = 1;
                        c = 0;
                        network2 = network3;
                    }
                    Object[] objArr = new Object[i];
                    objArr[c] = network2;
                    localLog(keys, "6", "network.isPasspoint %s", objArr);
                } else {
                    localLog(keys, "5", "isWifiAutoJoinPriority is true, ignore passpoint");
                }
                WifiConfiguration.NetworkSelectionStatus status = network3.getNetworkSelectionStatus();
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(status, network3.SSID, this.mContext)) {
                    Log.w(NAME, "evaluateNetworks: doesNotWifiConnectRejectByCust!");
                    it = it2;
                } else {
                    status.setSeenInLastQualifiedNetworkSelection(true);
                    if (unselectDueToFailedLastTime(scanResult, network3) || unselectDiscNonLocally(scanResult, network3)) {
                        it = it2;
                        network = network3;
                    } else if (!isNetworkEnabledExtended(network3, status)) {
                        it = it2;
                        network = network3;
                    } else if (network3.BSSID != null && !network3.BSSID.equals("any") && !network3.BSSID.equals(scanResult.BSSID)) {
                        localLog(keys, "Network %s has specified BSSID %, Skip %s", WifiNetworkSelector.toNetworkString(network3), StringUtilEx.safeDisplayBssid(network3.BSSID), StringUtilEx.safeDisplayBssid(scanResult.BSSID));
                        it = it2;
                    } else if (TelephonyUtil.isSimConfig(network3) && !TelephonyUtil.isSimPresent(this.mSubscriptionManager)) {
                        it = it2;
                    } else if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(network3, false)) {
                        Log.w(NAME, "evaluateNetworks: MDM deny connect to restricted network!");
                        it = it2;
                    } else if (WifiConfigStoreUtils.isSkipAutoConnect(this.mContext, network3)) {
                        it = it2;
                    } else {
                        it = it2;
                        int score = calculateBssidScore(scanResult, network3, currentNetwork, currentBssid, scoreHistory);
                        if (score > status.getCandidateScore() || (score == status.getCandidateScore() && status.getCandidate() != null && scanResult.level > status.getCandidate().level)) {
                            this.mWifiConfigManager.setNetworkCandidateScanResult(network3.networkId, scanResult, score);
                        }
                        HwCustWifiAutoJoinController hwCustWifiAutoJoinController2 = this.mCust;
                        if (hwCustWifiAutoJoinController2 != null && hwCustWifiAutoJoinController2.isWifiAutoJoinPriority(this.mContext)) {
                            if (candidate3 == null) {
                                localLog(keys, "8", "isWifiAutoJoinPriority ");
                                candidate3 = network3;
                            }
                            candidate3 = this.mCust.attemptAutoJoinCust(candidate3, network3);
                            scanResultCandidate3 = scanResult;
                            status.setCandidate(scanResult);
                            onConnectableListener.onConnectable(scanDetail, this.mWifiConfigManager.getConfiguredNetwork(network3.networkId), score);
                            it2 = it;
                        } else if (network3.useExternalScores) {
                            localLog(keys, "9", "Network %S has external score." + WifiNetworkSelector.toNetworkString(network3));
                        } else if ((!HuaweiTelephonyConfigs.isChinaMobile() || HwServiceFactory.getWifiProCommonUtilsEx().hwIsWifiProSwitchOn(this.mContext)) && selectBestNetworkByWifiPro(network3, scanResult)) {
                            localLog(keys, "10", "selectBestNetworkByWifiPro");
                        } else {
                            onConnectableListener.onConnectable(scanDetail, this.mWifiConfigManager.getConfiguredNetwork(network3.networkId), score);
                            if (HuaweiTelephonyConfigs.isChinaMobile()) {
                                WifiConfiguration configurationCandidateForThisScan = this.mWifiConfigManager.getConfiguredNetwork(network3.networkId);
                                if (configurationCandidateForThisScan != null) {
                                    if (candidate3 == null) {
                                        scanResultCandidate2 = scanResult;
                                        this.mWifiConfigManager.setNetworkCandidateScanResult(network3.networkId, scanResultCandidate2, highestScore);
                                        candidate2 = this.mWifiConfigManager.getConfiguredNetwork(network3.networkId);
                                        this.mWifiConfigManager.clearNetworkConnectChoice(network3.networkId);
                                        localLog("CMCC candidate set to " + candidate2);
                                    } else if (configurationCandidateForThisScan.priority > candidate3.priority) {
                                        localLog("CMCC find more higher priority,candidate set to new : " + configurationCandidateForThisScan.SSID);
                                        scanResultCandidate2 = scanResult;
                                        this.mWifiConfigManager.setNetworkCandidateScanResult(network3.networkId, scanResultCandidate2, highestScore);
                                        candidate2 = this.mWifiConfigManager.getConfiguredNetwork(network3.networkId);
                                        this.mWifiConfigManager.clearNetworkConnectChoice(network3.networkId);
                                    }
                                    scanResultCandidate3 = scanResultCandidate2;
                                    candidate3 = candidate2;
                                }
                            } else {
                                HwCustWifiAutoJoinController hwCustWifiAutoJoinController3 = this.mCust;
                                if (hwCustWifiAutoJoinController3 != null && hwCustWifiAutoJoinController3.isWifiAutoJoinPriority(this.mContext)) {
                                    localLog("isWifiAutoJoinPriority candidate : " + candidate3);
                                } else if (score > highestScore || (score == highestScore && scanResultCandidate3 != null && scanResult.level > scanResultCandidate3.level)) {
                                    this.mWifiConfigManager.setNetworkCandidateScanResult(network3.networkId, scanResult, score);
                                    highestScore = score;
                                    scanResultCandidate3 = scanResult;
                                    candidate3 = this.mWifiConfigManager.getConfiguredNetwork(network3.networkId);
                                }
                            }
                            it2 = it;
                        }
                    }
                    localLog(keys, "7", "status.isNetworkEnabled is false, %s", network);
                }
            }
            it2 = it;
        }
        if (savedScan.length() > 0) {
            localLog(keys, "11", "savedScan %s", savedScan.toString());
        }
        if (scoreHistory.length() > 0) {
            localLog(keys, "12", "scoreHistory %s", scoreHistory.toString());
        }
        if (HuaweiTelephonyConfigs.isChinaMobile() && !HwServiceFactory.getWifiProCommonUtilsEx().hwIsWifiProSwitchOn(this.mContext)) {
            candidate = candidate3;
            scanResultCandidate = scanResultCandidate3;
        } else if (isWifiProEnabledOrSelfCureGoing()) {
            localLog(keys, "13", "isWifiProEnabledOrSelfCureGoing true ");
            WifiConfiguration lastCandidate = getLastCandidateByWifiPro(candidate3, scanResultCandidate3);
            if (lastCandidate == null) {
                Log.w(NAME, "getLastCandidateByWifiPro is null");
                return lastCandidate;
            }
            ScanResult lastScanResultCandidate = lastCandidate.getNetworkSelectionStatus().getCandidate();
            ScanDetail scanDetail2 = findScanDetailByLastScanResult(scanDetails, lastScanResultCandidate);
            if (lastScanResultCandidate != null && scanDetail2 != null) {
                int tempScore = calculateBssidScore(lastScanResultCandidate, lastCandidate, currentNetwork, currentBssid, scoreHistory);
                StringBuilder sb = new StringBuilder();
                sb.append(StringUtilEx.safeDisplaySsid(lastScanResultCandidate.SSID));
                sb.append(":");
                sb.append(StringUtilEx.safeDisplayBssid(lastScanResultCandidate.BSSID));
                sb.append("(");
                sb.append(lastScanResultCandidate.is24GHz() ? "2.4GHz" : "5GHz");
                sb.append(") ");
                sb.append(String.valueOf(lastScanResultCandidate.level));
                Log.i(NAME, "saved available networks ssid = " + sb.toString() + ", tempScore = " + tempScore);
                onConnectableListener.onConnectable(scanDetail2, lastCandidate, tempScore);
            }
            return lastCandidate;
        } else {
            candidate = candidate3;
            scanResultCandidate = scanResultCandidate3;
        }
        if (scanResultCandidate == null) {
            localLog(keys, "14", "did not see any good candidates.");
        }
        return candidate;
    }

    private ScanDetail findScanDetailByLastScanResult(List<ScanDetail> scanDetails, ScanResult lastScanResultCandidate) {
        if (lastScanResultCandidate == null || scanDetails == null) {
            Log.w(NAME, "findScanDetailByLastScanResult invalid parameter : " + lastScanResultCandidate);
            return null;
        }
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            if (!(scanResult == null || scanResult.BSSID == null || !scanResult.BSSID.equals(lastScanResultCandidate.BSSID))) {
                Log.i(NAME, "findScanDetailByLastScanResult ssid = " + StringUtilEx.safeDisplaySsid(scanResult.SSID) + ", bssid = " + StringUtilEx.safeDisplayBssid(scanResult.BSSID));
                return scanDetail;
            }
        }
        return null;
    }

    private void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    private void localLog(String scanKey, String eventKey, String log, Object... params) {
        HwScanLocalLog.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
