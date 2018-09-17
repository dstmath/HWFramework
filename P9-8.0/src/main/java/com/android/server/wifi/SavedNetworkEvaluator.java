package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.NetworkSelectionStatus;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.wifi.WifiNetworkSelector.NetworkEvaluator;
import com.android.server.wifi.util.ScanResultUtil;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SavedNetworkEvaluator extends AbsSavedNetworkEvaluator implements NetworkEvaluator {
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
    private final int mSecurityAward;
    private final int mThresholdSaturatedRssi24;
    private final int mThresholdSaturatedRssi5;
    private final WifiConfigManager mWifiConfigManager;

    public SavedNetworkEvaluator(Context context, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiConnectivityHelper connectivityHelper) {
        this.mWifiConfigManager = configManager;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mConnectivityHelper = connectivityHelper;
        this.mContext = context;
        this.mRssiScoreSlope = context.getResources().getInteger(17694871);
        this.mRssiScoreOffset = context.getResources().getInteger(17694870);
        this.mSameBssidAward = context.getResources().getInteger(17694872);
        this.mSameNetworkAward = context.getResources().getInteger(17694882);
        this.mLastSelectionAward = context.getResources().getInteger(17694868);
        this.mSecurityAward = context.getResources().getInteger(17694873);
        this.mBand5GHzAward = context.getResources().getInteger(17694865);
        this.mThresholdSaturatedRssi24 = context.getResources().getInteger(17694894);
        this.mThresholdSaturatedRssi5 = context.getResources().getInteger(17694895);
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
                this.mWifiConfigManager.tryEnableNetwork(network.networkId);
                this.mWifiConfigManager.clearNetworkCandidateScanResult(network.networkId);
                NetworkSelectionStatus status = network.getNetworkSelectionStatus();
                if (!status.isNetworkEnabled()) {
                    sbuf.append("  ").append(WifiNetworkSelector.toNetworkString(network)).append(" ");
                    for (int index = 1; index < 16; index++) {
                        int count = status.getDisableReasonCounter(index);
                        if (count > 0) {
                            sbuf.append("reason=").append(NetworkSelectionStatus.getNetworkDisableReasonString(index)).append(", count=").append(count).append("; ");
                        }
                    }
                    sbuf.append("\n");
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
        boolean is5GHz = scanResult.is5GHz();
        sbuf.append("[ ").append(scanResult.SSID).append(" ").append(ScanResultUtil.getConfusedBssid(scanResult.BSSID)).append(" RSSI:").append(scanResult.level).append(" ] ");
        int rssiSaturationThreshold = is5GHz ? this.mThresholdSaturatedRssi5 : this.mThresholdSaturatedRssi24;
        if (scanResult.level < rssiSaturationThreshold) {
            rssi = scanResult.level;
        } else {
            rssi = rssiSaturationThreshold;
        }
        int score = ((this.mRssiScoreOffset + rssi) * this.mRssiScoreSlope) + 0;
        sbuf.append(" RSSI score: ").append(score).append(",");
        if (is5GHz) {
            score += this.mBand5GHzAward;
            sbuf.append(" 5GHz bonus: ").append(this.mBand5GHzAward).append(",");
        }
        int lastUserSelectedNetworkId = this.mWifiConfigManager.getLastSelectedNetwork();
        if (lastUserSelectedNetworkId != -1 && lastUserSelectedNetworkId == network.networkId) {
            long timeDifference = this.mClock.getElapsedSinceBootMillis() - this.mWifiConfigManager.getLastSelectedTimeStamp();
            if (timeDifference > 0) {
                int bonus = this.mLastSelectionAward - ((int) ((timeDifference / 1000) / 60));
                score += bonus > 0 ? bonus : 0;
                sbuf.append(" User selection ").append((timeDifference / 1000) / 60).append(" minutes ago, bonus: ").append(bonus).append(",");
            }
        }
        if (currentNetwork != null && network.networkId == currentNetwork.networkId) {
            score += this.mSameNetworkAward;
            sbuf.append(" Same network bonus: ").append(this.mSameNetworkAward).append(",");
            if (this.mConnectivityHelper.isFirmwareRoamingSupported() && currentBssid != null) {
                if ((currentBssid.equals(scanResult.BSSID) ^ 1) != 0) {
                    score += this.mSameBssidAward;
                    sbuf.append(" Equivalent BSSID bonus: ").append(this.mSameBssidAward).append(",");
                }
            }
        }
        if (currentBssid != null) {
            if (currentBssid.equals(scanResult.BSSID)) {
                score += this.mSameBssidAward;
                sbuf.append(" Same BSSID bonus: ").append(this.mSameBssidAward).append(",");
            }
        }
        if (!WifiConfigurationUtil.isConfigForOpenNetwork(network)) {
            score += this.mSecurityAward;
            sbuf.append(" Secure network bonus: ").append(this.mSecurityAward).append(",");
        }
        sbuf.append(" ## Total score: ").append(score).append("\n");
        return score;
    }

    public WifiConfiguration evaluateNetworks(List<ScanDetail> scanDetails, WifiConfiguration currentNetwork, String currentBssid, boolean connected, boolean untrustedNetworkAllowed, List<Pair<ScanDetail, WifiConfiguration>> connectableNetworks) {
        String keys = this.mConnectivityHelper.mCurrentScanKeys;
        StringBuffer savedScan = new StringBuffer();
        int highestScore = Integer.MIN_VALUE;
        ScanResult scanResultCandidate = null;
        WifiConfiguration candidate = null;
        StringBuffer scoreHistory = new StringBuffer();
        resetHwSelectedCandidates();
        for (ScanDetail scanDetail : scanDetails) {
            ScanResult scanResult = scanDetail.getScanResult();
            int highestScoreOfScanResult = Integer.MIN_VALUE;
            int candidateIdOfScanResult = -1;
            if (this.mWifiConfigManager.getSavedNetworkForScanDetailAndCache(scanDetail) != null) {
                WifiConfiguration configurationCandidateForThisScan;
                List<WifiConfiguration> associatedConfigurations = new ArrayList(Arrays.asList(new WifiConfiguration[]{this.mWifiConfigManager.getSavedNetworkForScanDetailAndCache(scanDetail)}));
                savedScan.append(scanResult.SSID + " / ");
                for (WifiConfiguration network : associatedConfigurations) {
                    if (this.mCust != null && this.mCust.isWifiAutoJoinPriority()) {
                        localLog(keys, "5", "isWifiAutoJoinPriority is true, ignore passpoint");
                    } else if (network.isPasspoint() || network.isEphemeral()) {
                        localLog(keys, "6", "network.isPasspoint %s", network);
                    }
                    NetworkSelectionStatus status = network.getNetworkSelectionStatus();
                    status.setSeenInLastQualifiedNetworkSelection(true);
                    if (unselectDueToFailedLastTime(scanResult, network) || unselectDiscNonLocally(scanResult, network) || (status.isNetworkEnabled() ^ 1) != 0) {
                        localLog(keys, "7", "status.isNetworkEnabled is false, %s", network);
                    } else if (network.BSSID != null && (network.BSSID.equals("any") ^ 1) != 0 && (network.BSSID.equals(scanResult.BSSID) ^ 1) != 0) {
                        localLog(keys, "Network %s has specified BSSID %, Skip ", WifiNetworkSelector.toNetworkString(network), network.BSSID, scanResult.BSSID);
                    } else if (HwWifiServiceFactory.getHwWifiDevicePolicy().isWifiRestricted(network, false)) {
                        Log.w(NAME, "evaluateNetworks: MDM deny connect to restricted network!");
                    } else if (this.mCust != null && this.mCust.isWifiAutoJoinPriority()) {
                        if (candidate == null) {
                            localLog(keys, "8", "isWifiAutoJoinPriority ");
                            candidate = network;
                        }
                        candidate = this.mCust.attemptAutoJoinCust(candidate, network);
                        scanResultCandidate = scanResult;
                        status.setCandidate(scanResult);
                    } else if (!WifiConfigStoreUtils.isSkipAutoConnect(this.mContext, network)) {
                        int score = calculateBssidScore(scanResult, network, currentNetwork, currentBssid, scoreHistory);
                        if (score > status.getCandidateScore() || (score == status.getCandidateScore() && status.getCandidate() != null && scanResult.level > status.getCandidate().level)) {
                            this.mWifiConfigManager.setNetworkCandidateScanResult(network.networkId, scanResult, score);
                        }
                        if (network.useExternalScores) {
                            localLog(keys, "9", "Network %S has external score." + WifiNetworkSelector.toNetworkString(network));
                        } else if (selectBestNetworkByWifiPro(network, scanResult)) {
                            localLog(keys, "10", "selectBestNetworkByWifiPro");
                        } else if (HuaweiTelephonyConfigs.isChinaMobile()) {
                            configurationCandidateForThisScan = this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult);
                            if (configurationCandidateForThisScan == null) {
                                candidateIdOfScanResult = network.networkId;
                            } else if (network.priority > configurationCandidateForThisScan.priority) {
                                candidateIdOfScanResult = network.networkId;
                            }
                        } else if (score > highestScoreOfScanResult) {
                            highestScoreOfScanResult = score;
                            candidateIdOfScanResult = network.networkId;
                        }
                    }
                }
                if (connectableNetworks != null) {
                    connectableNetworks.add(Pair.create(scanDetail, this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult)));
                }
                if (HuaweiTelephonyConfigs.isChinaMobile()) {
                    configurationCandidateForThisScan = this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult);
                    if (configurationCandidateForThisScan != null) {
                        if (candidate == null) {
                            scanResultCandidate = scanResult;
                            this.mWifiConfigManager.setNetworkCandidateScanResult(candidateIdOfScanResult, scanResult, highestScore);
                            candidate = this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult);
                            this.mWifiConfigManager.clearNetworkConnectChoice(candidateIdOfScanResult);
                            localLog("CMCC candidate set to " + candidate);
                        } else if (configurationCandidateForThisScan.priority > candidate.priority) {
                            localLog("CMCC find more higher priority,candidate set to new : " + configurationCandidateForThisScan.SSID);
                            scanResultCandidate = scanResult;
                            this.mWifiConfigManager.setNetworkCandidateScanResult(candidateIdOfScanResult, scanResult, highestScore);
                            candidate = this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult);
                            this.mWifiConfigManager.clearNetworkConnectChoice(candidateIdOfScanResult);
                        }
                    }
                } else if (this.mCust != null && this.mCust.isWifiAutoJoinPriority()) {
                    localLog("isWifiAutoJoinPriority candidate : " + candidate);
                } else if (highestScoreOfScanResult > highestScore || (highestScoreOfScanResult == highestScore && scanResultCandidate != null && scanResult.level > scanResultCandidate.level)) {
                    highestScore = highestScoreOfScanResult;
                    scanResultCandidate = scanResult;
                    this.mWifiConfigManager.setNetworkCandidateScanResult(candidateIdOfScanResult, scanResult, highestScore);
                    candidate = this.mWifiConfigManager.getConfiguredNetwork(candidateIdOfScanResult);
                }
            }
        }
        if (savedScan.length() > 0) {
            localLog(keys, "11", "savedScan %s", savedScan.toString());
        }
        if (scoreHistory.length() > 0) {
            localLog(keys, "12", "scoreHistory %s", scoreHistory.toString());
        }
        if (isWifiProEnabledOrSelfCureGoing()) {
            localLog(keys, "13", "isWifiProEnabledOrSelfCureGoing true ");
            return getLastCandidateByWifiPro(candidate, scanResultCandidate);
        }
        if (scanResultCandidate == null) {
            localLog(keys, "14", "did not see any good candidates.");
        }
        return candidate;
    }

    private void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    private void localLog(String scanKey, String eventKey, String log, Object... params) {
        WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
    }
}
