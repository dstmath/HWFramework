package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.HwServiceFactory;
import com.android.server.wifi.WifiNetworkSelector;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.WifiCommonUtils;
import huawei.cust.HwCustUtils;
import java.util.Iterator;
import java.util.List;

public class SavedNetworkEvaluator extends AbsSavedNetworkEvaluator implements WifiNetworkSelector.NetworkEvaluator {
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
    private final WifiConfigManager mWifiConfigManager;

    public SavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiConnectivityHelper connectivityHelper) {
        this.mScoringParams = scoringParams;
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mConnectivityHelper = connectivityHelper;
        this.mContext = context;
        this.mRssiScoreSlope = context.getResources().getInteger(17694890);
        this.mRssiScoreOffset = context.getResources().getInteger(17694889);
        this.mSameBssidAward = context.getResources().getInteger(17694891);
        this.mSameNetworkAward = context.getResources().getInteger(17694901);
        this.mLastSelectionAward = context.getResources().getInteger(17694887);
        this.mSecurityAward = context.getResources().getInteger(17694892);
        this.mBand5GHzAward = context.getResources().getInteger(17694884);
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    public String getName() {
        return NAME;
    }

    private void updateSavedNetworkSelectionStatus() {
        List<WifiConfiguration> savedNetworks = this.mWifiConfigManager.getSavedNetworks();
        if (savedNetworks.size() == 0) {
            localLog("No saved networks.");
            return;
        }
        StringBuffer sbuf = new StringBuffer();
        for (WifiConfiguration network : savedNetworks) {
            if (!network.isPasspoint()) {
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(network.getNetworkSelectionStatus(), network.SSID, this.mContext)) {
                    Log.w(NAME, "updateSavedNetworkSelectionStatus: doesNotWifiConnectRejectByCust!");
                } else {
                    this.mWifiConfigManager.tryEnableNetwork(network.networkId);
                    this.mWifiConfigManager.clearNetworkCandidateScanResult(network.networkId);
                    WifiConfiguration.NetworkSelectionStatus status = network.getNetworkSelectionStatus();
                    if (!status.isNetworkEnabled()) {
                        sbuf.append("  ");
                        sbuf.append(WifiNetworkSelector.toNetworkString(network));
                        sbuf.append(" ");
                        for (int index = 1; index < 17; index++) {
                            int count = status.getDisableReasonCounter(index);
                            if (count > 0) {
                                sbuf.append("reason=");
                                sbuf.append(WifiConfiguration.NetworkSelectionStatus.getNetworkDisableReasonString(index));
                                sbuf.append(", count=");
                                sbuf.append(count);
                                sbuf.append("; ");
                            }
                        }
                        sbuf.append("\n");
                    }
                }
            }
        }
        if (sbuf.length() > 0) {
            localLog("Disabled saved networks:");
            localLog(sbuf.toString());
        }
    }

    public void update(List<ScanDetail> list) {
        updateSavedNetworkSelectionStatus();
    }

    private int calculateBssidScore(ScanResult scanResult, WifiConfiguration network, WifiConfiguration currentNetwork, String currentBssid, StringBuffer sbuf) {
        int rssi;
        ScanResult scanResult2 = scanResult;
        WifiConfiguration wifiConfiguration = network;
        WifiConfiguration wifiConfiguration2 = currentNetwork;
        String str = currentBssid;
        StringBuffer stringBuffer = sbuf;
        boolean is5GHz = scanResult.is5GHz();
        stringBuffer.append("[ ");
        stringBuffer.append(scanResult2.SSID);
        stringBuffer.append(" ");
        stringBuffer.append(ScanResultUtil.getConfusedBssid(scanResult2.BSSID));
        stringBuffer.append(" RSSI:");
        stringBuffer.append(scanResult2.level);
        stringBuffer.append(" ] ");
        int rssiSaturationThreshold = this.mScoringParams.getGoodRssi(scanResult2.frequency);
        if (scanResult2.level < rssiSaturationThreshold) {
            rssi = scanResult2.level;
        } else {
            rssi = rssiSaturationThreshold;
        }
        int score = 0 + ((this.mRssiScoreOffset + rssi) * this.mRssiScoreSlope);
        stringBuffer.append(" RSSI score: ");
        stringBuffer.append(score);
        stringBuffer.append(",");
        if (is5GHz) {
            score += this.mBand5GHzAward;
            stringBuffer.append(" 5GHz bonus: ");
            stringBuffer.append(this.mBand5GHzAward);
            stringBuffer.append(",");
        }
        int lastUserSelectedNetworkId = this.mWifiConfigManager.getLastSelectedNetwork();
        if (lastUserSelectedNetworkId != -1 && lastUserSelectedNetworkId == wifiConfiguration.networkId) {
            long timeDifference = this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = this.mLastSelectionAward - ((int) ((timeDifference / 1000) / 60));
                score += bonus > 0 ? bonus : 0;
                stringBuffer.append(" User selection ");
                stringBuffer.append((timeDifference / 1000) / 60);
                stringBuffer.append(" minutes ago, bonus: ");
                stringBuffer.append(bonus);
                stringBuffer.append(",");
            }
        }
        if (wifiConfiguration2 != null && wifiConfiguration.networkId == wifiConfiguration2.networkId) {
            score += this.mSameNetworkAward;
            stringBuffer.append(" Same network bonus: ");
            stringBuffer.append(this.mSameNetworkAward);
            stringBuffer.append(",");
            if (this.mConnectivityHelper.isFirmwareRoamingSupported() && str != null && !str.equals(scanResult2.BSSID)) {
                score += this.mSameBssidAward;
                stringBuffer.append(" Equivalent BSSID bonus: ");
                stringBuffer.append(this.mSameBssidAward);
                stringBuffer.append(",");
            }
        }
        if (str != null && str.equals(scanResult2.BSSID)) {
            score += this.mSameBssidAward;
            stringBuffer.append(" Same BSSID bonus: ");
            stringBuffer.append(this.mSameBssidAward);
            stringBuffer.append(",");
        }
        if (!WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            score += this.mSecurityAward;
            stringBuffer.append(" Secure network bonus: ");
            stringBuffer.append(this.mSecurityAward);
            stringBuffer.append(",");
        }
        stringBuffer.append(" ## Total score: ");
        stringBuffer.append(score);
        stringBuffer.append("\n");
        return score;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:92:0x0279, code lost:
        if (r12.level > r2.level) goto L_0x0282;
     */
    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, List<Pair<ScanDetail, WifiConfiguration>> connectableNetworks) {
        StringBuffer savedScan;
        Iterator<ScanDetail> it;
        ScanResult scanResultCandidate;
        WifiConfiguration network;
        StringBuffer savedScan2;
        Iterator<ScanDetail> it2;
        ScanResult scanResultCandidate2;
        ScanResult scanResultCandidate3;
        WifiConfiguration candidate;
        ScanResult scanResultCandidate4;
        StringBuffer savedScan3;
        WifiConfiguration network2;
        char c;
        int i;
        List<Pair<ScanDetail, WifiConfiguration>> list = connectableNetworks;
        String keys = this.mConnectivityHelper.mCurrentScanKeys;
        StringBuffer savedScan4 = new StringBuffer();
        StringBuffer scoreHistory = new StringBuffer();
        resetHwSelectedCandidates();
        Iterator<ScanDetail> it3 = scanDetails.iterator();
        int highestScore = Integer.MIN_VALUE;
        ScanResult scanResultCandidate5 = null;
        WifiConfiguration candidate2 = null;
        while (it3.hasNext() != 0) {
            ScanDetail scanDetail = it3.next();
            ScanResult scanResult = scanDetail.getScanResult();
            WifiConfiguration network3 = this.mWifiConfigManager.getConfiguredNetworkForScanDetailAndCache(scanDetail);
            if (network3 != null) {
                savedScan4.append(scanResult.SSID + " / ");
                if (this.mCust == null || !this.mCust.isWifiAutoJoinPriority(this.mContext)) {
                    if (network3.isPasspoint()) {
                        i = 1;
                        savedScan = savedScan4;
                        it = it3;
                        scanResultCandidate = scanResultCandidate5;
                        network2 = network3;
                        ScanResult scanResultCandidate6 = scanResult;
                        c = 0;
                    } else if (network3.isEphemeral()) {
                        i = 1;
                        savedScan = savedScan4;
                        it = it3;
                        scanResultCandidate = scanResultCandidate5;
                        network2 = network3;
                        ScanResult scanResultCandidate7 = scanResult;
                        c = 0;
                    }
                    Object[] objArr = new Object[i];
                    objArr[c] = network2;
                    localLog(keys, "6", "network.isPasspoint %s", objArr);
                    scanResultCandidate5 = scanResultCandidate;
                    it3 = it;
                    savedScan4 = savedScan;
                } else {
                    localLog(keys, "5", "isWifiAutoJoinPriority is true, ignore passpoint");
                }
                WifiConfiguration.NetworkSelectionStatus status = network3.getNetworkSelectionStatus();
                if (WifiCommonUtils.doesNotWifiConnectRejectByCust(status, network3.SSID, this.mContext)) {
                    Log.w(NAME, "evaluateNetworks: doesNotWifiConnectRejectByCust!");
                } else {
                    status.setSeenInLastQualifiedNetworkSelection(true);
                    if (unselectDueToFailedLastTime(scanResult, network3) || unselectDiscNonLocally(scanResult, network3)) {
                        savedScan2 = savedScan4;
                        it2 = it3;
                        scanResultCandidate2 = scanResultCandidate5;
                        WifiConfiguration.NetworkSelectionStatus networkSelectionStatus = status;
                        network = network3;
                        ScanResult scanResultCandidate8 = scanResult;
                    } else if (!isNetworkEnabledExtended(network3, status)) {
                        savedScan2 = savedScan4;
                        it2 = it3;
                        scanResultCandidate2 = scanResultCandidate5;
                        WifiConfiguration.NetworkSelectionStatus networkSelectionStatus2 = status;
                        network = network3;
                        ScanResult scanResultCandidate9 = scanResult;
                    } else {
                        if (network3.BSSID == null || network3.BSSID.equals("any") || network3.BSSID.equals(scanResult.BSSID)) {
                            it = it3;
                            if (!TelephonyUtil.isSimConfig(network3) || this.mWifiConfigManager.isSimPresent()) {
                                if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(network3, false)) {
                                    Log.w(NAME, "evaluateNetworks: MDM deny connect to restricted network!");
                                } else if (this.mCust != null && this.mCust.isWifiAutoJoinPriority(this.mContext)) {
                                    if (candidate2 == null) {
                                        localLog(keys, "8", "isWifiAutoJoinPriority ");
                                        candidate2 = network3;
                                    }
                                    candidate2 = this.mCust.attemptAutoJoinCust(candidate2, network3);
                                    scanResultCandidate5 = scanResult;
                                    status.setCandidate(scanResult);
                                    it3 = it;
                                } else if (!WifiConfigStoreUtils.isSkipAutoConnect(this.mContext, network3)) {
                                    WifiConfiguration.NetworkSelectionStatus status2 = status;
                                    savedScan = savedScan4;
                                    WifiConfiguration network4 = network3;
                                    ScanResult scanResultCandidate10 = scanResultCandidate5;
                                    scanResultCandidate5 = scanResult;
                                    int score = calculateBssidScore(scanResult, network3, currentNetwork, currentBssid, scoreHistory);
                                    if (score > status2.getCandidateScore() || (score == status2.getCandidateScore() && status2.getCandidate() != null && scanResultCandidate5.level > status2.getCandidate().level)) {
                                        this.mWifiConfigManager.setNetworkCandidateScanResult(network4.networkId, scanResultCandidate5, score);
                                    }
                                    if (network4.useExternalScores) {
                                        localLog(keys, "9", "Network %S has external score." + WifiNetworkSelector.toNetworkString(network4));
                                    } else if ((!HuaweiTelephonyConfigs.isChinaMobile() || HwServiceFactory.getWifiProCommonUtilsEx().hwIsWifiProSwitchOn(this.mContext)) && selectBestNetworkByWifiPro(network4, scanResultCandidate5)) {
                                        localLog(keys, "10", "selectBestNetworkByWifiPro");
                                    } else {
                                        if (list != null) {
                                            list.add(Pair.create(scanDetail, this.mWifiConfigManager.getConfiguredNetwork(network4.networkId)));
                                        }
                                        if (HuaweiTelephonyConfigs.isChinaMobile()) {
                                            WifiConfiguration configurationCandidateForThisScan = this.mWifiConfigManager.getConfiguredNetwork(network4.networkId);
                                            if (configurationCandidateForThisScan != null) {
                                                if (candidate2 == null) {
                                                    scanResultCandidate4 = scanResultCandidate5;
                                                    this.mWifiConfigManager.setNetworkCandidateScanResult(network4.networkId, scanResultCandidate4, highestScore);
                                                    candidate = this.mWifiConfigManager.getConfiguredNetwork(network4.networkId);
                                                    this.mWifiConfigManager.clearNetworkConnectChoice(network4.networkId);
                                                    localLog("CMCC candidate set to " + candidate);
                                                } else if (configurationCandidateForThisScan.priority > candidate2.priority) {
                                                    localLog("CMCC find more higher priority,candidate set to new : " + configurationCandidateForThisScan.SSID);
                                                    scanResultCandidate4 = scanResultCandidate5;
                                                    this.mWifiConfigManager.setNetworkCandidateScanResult(network4.networkId, scanResultCandidate4, highestScore);
                                                    candidate = this.mWifiConfigManager.getConfiguredNetwork(network4.networkId);
                                                    this.mWifiConfigManager.clearNetworkConnectChoice(network4.networkId);
                                                }
                                                scanResultCandidate5 = scanResultCandidate4;
                                                candidate2 = candidate;
                                            }
                                        } else {
                                            if (this.mCust == null || !this.mCust.isWifiAutoJoinPriority(this.mContext)) {
                                                if (score <= highestScore) {
                                                    scanResultCandidate3 = (score != highestScore || scanResultCandidate10 == null) ? scanResultCandidate10 : scanResultCandidate10;
                                                }
                                                int highestScore2 = score;
                                                this.mWifiConfigManager.setNetworkCandidateScanResult(network4.networkId, scanResultCandidate5, highestScore2);
                                                candidate2 = this.mWifiConfigManager.getConfiguredNetwork(network4.networkId);
                                                highestScore = highestScore2;
                                            } else {
                                                localLog("isWifiAutoJoinPriority candidate : " + candidate2);
                                                scanResultCandidate3 = scanResultCandidate10;
                                            }
                                            scanResultCandidate5 = scanResultCandidate3;
                                        }
                                        it3 = it;
                                        savedScan4 = savedScan;
                                    }
                                    scanResultCandidate = scanResultCandidate10;
                                    scanResultCandidate5 = scanResultCandidate;
                                    it3 = it;
                                    savedScan4 = savedScan;
                                }
                            }
                        } else {
                            it = it3;
                            localLog(keys, "Network %s has specified BSSID %, Skip ", WifiNetworkSelector.toNetworkString(network3), network3.BSSID, scanResult.BSSID);
                        }
                        savedScan3 = savedScan4;
                        scanResultCandidate = scanResultCandidate5;
                        scanResultCandidate5 = scanResultCandidate;
                        it3 = it;
                        savedScan4 = savedScan;
                    }
                    localLog(keys, "7", "status.isNetworkEnabled is false, %s", network);
                    scanResultCandidate5 = scanResultCandidate;
                    it3 = it;
                    savedScan4 = savedScan;
                }
            }
            savedScan3 = savedScan4;
            it = it3;
            scanResultCandidate = scanResultCandidate5;
            scanResultCandidate5 = scanResultCandidate;
            it3 = it;
            savedScan4 = savedScan;
        }
        ScanResult scanResultCandidate11 = scanResultCandidate5;
        StringBuffer savedScan5 = savedScan4;
        if (savedScan5.length() > 0) {
            localLog(keys, "11", "savedScan %s", savedScan5.toString());
        }
        if (scoreHistory.length() > 0) {
            localLog(keys, "12", "scoreHistory %s", scoreHistory.toString());
        }
        if ((!HuaweiTelephonyConfigs.isChinaMobile() || HwServiceFactory.getWifiProCommonUtilsEx().hwIsWifiProSwitchOn(this.mContext)) && isWifiProEnabledOrSelfCureGoing()) {
            localLog(keys, "13", "isWifiProEnabledOrSelfCureGoing true ");
            return getLastCandidateByWifiPro(candidate2, scanResultCandidate11);
        }
        if (scanResultCandidate11 == null) {
            localLog(keys, "14", "did not see any good candidates.");
        }
        return candidate2;
    }

    public boolean isNetworkEnabledExtended(WifiConfiguration config, WifiConfiguration.NetworkSelectionStatus status) {
        return true;
    }

    private void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    private void localLog(String scanKey, String eventKey, String log, Object... params) {
        WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
