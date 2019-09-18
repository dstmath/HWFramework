package com.android.server.wifi.wifipro;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.wifi.Clock;
import com.android.server.wifi.HwQoE.HidataWechatTraffic;
import com.android.server.wifi.HwQoE.HwQoEService;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwSelfCureEngine;
import com.android.server.wifi.SavedNetworkEvaluator;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiConnectivityHelper;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.WifiStateMachine;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.HashMap;

public class HwSavedNetworkEvaluator extends SavedNetworkEvaluator {
    private static int BACKUP_UNUSED = 0;
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_OK = 0;
    private static int HAS_INET_SELECTED = 100;
    private static int HIDATA_PREFER_SELECTED = HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO;
    private static final String HOME_PKT_NAME = "com.huawei.android.launcher";
    private static final String HUAWEI_GUEST = "\"Huawei-Guest\"NONE";
    private static final int MIN_3_LEVEL = -75;
    private static int NORMAL_PORTAL_SELECTED = 103;
    private static int NO_INET_SELECTED = 104;
    private static int PORTAL_DISAPPEAR_THRESHOLD = 2;
    private static int RECOVERY_SELECTED = 101;
    private static final String TAG = "HwSavedNetworkEvaluator";
    private static int TRUSTED_PORTAL_SELECTED = 102;
    private int backupTypeSelected = BACKUP_UNUSED;
    private WifiConfiguration hasInetNetworkCandidate = null;
    private ScanResult hasInetScanResultCandidate = null;
    private Context mContext;
    private HwArbitrationManager mHwArbitrationManager = null;
    private int mSelfCureCandidateLostCnt = 0;
    private WifiNative mWifiNative;
    private WifiStateMachine mWifiStateMachine;
    private WifiConfiguration noInetNetworkCandidate = null;
    private ScanResult noInetScanResultCandidate = null;
    private boolean portalDisappeared = true;
    private int portalDisappearedCounter = 0;
    private WifiConfiguration portalNetworkCandidate = null;
    private String portalNotifiedConfigKey = null;
    private boolean portalNotifiedHasInternet = false;
    private int portalNotifiedMaxRssi = WifiHandover.INVALID_RSSI;
    private ScanResult portalScanResultCandidate = null;
    private ScanResult portalScanResultTursted = null;
    private WifiConfiguration preferNetworkCandidate = null;
    private ScanResult preferScanResultCandidate = null;
    private WifiConfiguration recoveryNetworkCandidate = null;
    private ScanResult recoveryScanResultCandidate = null;
    private WifiConfiguration selfCureNetworkCandidate = null;
    private ScanResult selfCureScanResultCandidate = null;

    public HwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, WifiStateMachine wsm, WifiConnectivityHelper connectivityHelper) {
        super(context, scoringParams, configManager, clock, localLog, connectivityHelper);
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
    }

    public synchronized void resetHwSelectedCandidates() {
        this.preferNetworkCandidate = null;
        this.preferScanResultCandidate = null;
        this.hasInetNetworkCandidate = null;
        this.hasInetScanResultCandidate = null;
        this.recoveryNetworkCandidate = null;
        this.recoveryScanResultCandidate = null;
        this.portalScanResultTursted = null;
        this.portalNetworkCandidate = null;
        this.portalScanResultCandidate = null;
        this.noInetNetworkCandidate = null;
        this.noInetScanResultCandidate = null;
        this.selfCureNetworkCandidate = null;
        this.selfCureScanResultCandidate = null;
        this.backupTypeSelected = BACKUP_UNUSED;
        this.portalDisappeared = true;
        this.portalNotifiedMaxRssi = WifiHandover.INVALID_RSSI;
    }

    public boolean isNetworkEnabledExtended(WifiConfiguration config, WifiConfiguration.NetworkSelectionStatus status) {
        if (status.isNetworkEnabled() || HwAutoConnectManager.getInstance() == null || !HwAutoConnectManager.getInstance().allowAutoJoinDisabledNetworkAgain(config)) {
            return status.isNetworkEnabled();
        }
        LOGD("isNetworkEnabledExtended, allowAutoJoinDisabledNetworkAgain = " + config.configKey());
        return true;
    }

    public boolean unselectDueToFailedLastTime(ScanResult scanResult, WifiConfiguration config) {
        if (!(scanResult == null || config == null || (config.lastConnFailedType != 3 && config.lastConnFailedType != 2 && config.lastConnFailedType != 4))) {
            long deltaMs = System.currentTimeMillis() - config.lastConnFailedTimestamp;
            WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
            int count = status.getDisableReasonCounter(config.lastConnFailedType);
            if (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) >= 4) {
                if (config.rssiStatusDisabled != -200 && config.rssiStatusDisabled < -75) {
                    status.setNetworkSelectionStatus(0);
                    status.setDisableTime(-1);
                    status.setNetworkSelectionDisableReason(0);
                    config.rssiStatusDisabled = WifiHandover.INVALID_RSSI;
                }
                return false;
            } else if (isUserOnWlanSettings()) {
                return false;
            } else {
                if (deltaMs > 300000) {
                    config.lastConnFailedType = 0;
                    config.lastConnFailedTimestamp = 0;
                    config.rssiStatusDisabled = WifiHandover.INVALID_RSSI;
                    return false;
                } else if ((count == 1 && deltaMs < 10000) || ((count == 2 && deltaMs < 30000) || ((count == 3 && deltaMs < HidataWechatTraffic.MIN_VALID_TIME) || (count == 4 && deltaMs < 90000)))) {
                    LOGD("unselectDueToFailedLastTime, DELAYED!!! count = " + count + ", deltaMs = " + deltaMs + ", ssid = " + scanResult.SSID + ", level = " + scanResult.level);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean unselectDhcpFailedBssid(ScanResult scanResult, WifiConfiguration config) {
        if (scanResult == null || config == null || HwSelfCureEngine.getInstance() == null || !HwSelfCureEngine.getInstance().isDhcpFailedBssid(scanResult.BSSID)) {
            return false;
        }
        LOGD("unselectDhcpFailedBssid, key = " + config.configKey());
        return true;
    }

    private boolean unselectPoorNetwork(ScanResult scanResult, WifiConfiguration config) {
        if (scanResult == null || config == null || isMobileDataInactive() || WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) >= 3 || (HwSelfCureEngine.getInstance() != null && HwSelfCureEngine.getInstance().isDhcpFailedConfigKey(config.configKey()))) {
            return false;
        }
        long ts = this.mWifiStateMachine.getWifiEnabledTimeStamp();
        if (ts != 0 && System.currentTimeMillis() - ts < 20000) {
            return false;
        }
        HwQoEService mHwQoEService = HwQoEService.getInstance();
        if (mHwQoEService != null && mHwQoEService.isConnectWhenWeChating(scanResult)) {
            return false;
        }
        String pktName = "";
        HwAutoConnectManager autoConnectManager = HwAutoConnectManager.getInstance();
        if (autoConnectManager != null) {
            pktName = autoConnectManager.getCurrentPackageName();
        }
        if (!HOME_PKT_NAME.equals(pktName)) {
            LOGD("unselectPoorNetwork, DELAY!!! level = " + scanResult.level + ", ssid = " + scanResult.SSID);
            return true;
        }
        return false;
    }

    public boolean unselectDiscNonLocally(ScanResult scanResult, WifiConfiguration config) {
        if (unselectDhcpFailedBssid(scanResult, config) || unselectPoorNetwork(scanResult, config)) {
            return true;
        }
        if (scanResult == null || config == null || config.rssiDiscNonLocally == 0 || config.rssiDiscNonLocally == -200 || WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) >= 3 || isMobileDataInactive()) {
            return false;
        }
        if (scanResult.level - config.rssiDiscNonLocally < (WifiProCommonUtils.isWpaOrWpa2(config) ? 5 : 8)) {
            LOGD("unselectDiscNonLocally, DELAYED this bssid !!! current = " + scanResult.level + ", disc = " + config.rssiDiscNonLocally + ", ssid = " + config.configKey());
            return true;
        }
        return false;
    }

    public synchronized WifiConfiguration getLastCandidateByWifiPro(WifiConfiguration config, ScanResult scanResultCandidate) {
        WifiConfiguration lastConfig;
        lastConfig = candidateUpdatedByWifiPro(config);
        scanResultUpdatedByWifiPro(lastConfig, scanResultCandidate);
        handleSelectNetworkCompleted(lastConfig);
        return lastConfig;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005c, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00cc, code lost:
        return true;
     */
    public synchronized boolean selectBestNetworkByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        if (!this.mWifiStateMachine.isWifiSelfCuring() || config == null || config.networkId != this.mWifiStateMachine.getSelfCureNetworkId()) {
            if (isWifiProEnabled() && this.mWifiStateMachine.getCurrentNetwork() == null && config != null && scanResult != null) {
                LOGD("selectBestNetworkByWifiPro, current = " + config.configKey(true) + ", internetHistory = " + config.internetHistory + ", level = " + scanResult.level + ", 5GHz = " + scanResult.is5GHz());
                if (networkIgnoredByWifiPro(config, scanResult) || selectNetworkHidataPrefer(config, scanResult) || selectNetworkHasInternet(config, scanResult) || selectNetworkHasInternetEver(config, scanResult) || selectNetworkPortal(config, scanResult) || selectNetworkNoInternet(config, scanResult)) {
                }
            }
            return false;
        }
        LOGD("selectBestNetworkByWifiPro, wifi self curing, forced connecting network = " + config.configKey());
        if (this.selfCureNetworkCandidate == null || this.selfCureScanResultCandidate == null) {
            this.selfCureNetworkCandidate = config;
            this.selfCureScanResultCandidate = scanResult;
            this.selfCureNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (scanResult != null && scanResult.level > this.selfCureScanResultCandidate.level) {
            this.selfCureNetworkCandidate = config;
            this.selfCureScanResultCandidate = scanResult;
            this.selfCureNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        }
    }

    private boolean networkIgnoredByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        if (config != null && config.isTempCreated) {
            LOGD(config.SSID + ", networkIgnoredByAPScore, skip candidate due to istempcreated");
            return true;
        } else if (scanResult == null || HwAutoConnectManager.getInstance() == null || !HwAutoConnectManager.getInstance().isBssidMatchedBlacklist(scanResult.BSSID)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean selectNetworkHasInternet(WifiConfiguration config, ScanResult scanResult) {
        if (config == null || HUAWEI_GUEST.equals(config.configKey())) {
            return false;
        }
        if (!hasInternet(config) && !maybeHasInternet(config) && (!config.noInternetAccess || config.internetRecoveryStatus != 5)) {
            return false;
        }
        if (this.hasInetNetworkCandidate == null) {
            this.hasInetNetworkCandidate = config;
            this.hasInetScanResultCandidate = scanResult;
            this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (!maybeHasInternet(this.hasInetNetworkCandidate) && maybeHasInternet(config)) {
            return true;
        } else {
            if (maybeHasInternet(this.hasInetNetworkCandidate) && !maybeHasInternet(config)) {
                this.hasInetNetworkCandidate = config;
                this.hasInetScanResultCandidate = scanResult;
                this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                return true;
            } else if ((!ScanResult.is5GHz(this.hasInetScanResultCandidate.frequency) || !ScanResult.is5GHz(scanResult.frequency)) && (!ScanResult.is24GHz(this.hasInetScanResultCandidate.frequency) || !ScanResult.is24GHz(scanResult.frequency))) {
                if (!ScanResult.is5GHz(this.hasInetScanResultCandidate.frequency) || !ScanResult.is24GHz(scanResult.frequency)) {
                    if (ScanResult.is24GHz(this.hasInetScanResultCandidate.frequency) && ScanResult.is5GHz(scanResult.frequency) && (scanResult.level > this.hasInetScanResultCandidate.level || scanResult.level >= -72)) {
                        this.hasInetNetworkCandidate = config;
                        this.hasInetScanResultCandidate = scanResult;
                        this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                    }
                } else if (this.hasInetScanResultCandidate.level < -72 && scanResult.level > this.hasInetScanResultCandidate.level) {
                    this.hasInetNetworkCandidate = config;
                    this.hasInetScanResultCandidate = scanResult;
                    this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                }
            } else if (scanResult.level > this.hasInetScanResultCandidate.level) {
                this.hasInetNetworkCandidate = config;
                this.hasInetScanResultCandidate = scanResult;
                this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            }
        }
        return true;
    }

    private boolean selectNetworkHidataPrefer(WifiConfiguration config, ScanResult scanResult) {
        if (config == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("selectNetworkHidataPrefer: ");
        sb.append(hasInternet(config));
        sb.append(",");
        sb.append(maybeHasInternet(config));
        sb.append(",");
        sb.append(config.noInternetAccess);
        sb.append(",");
        sb.append(config.internetRecoveryStatus == 5);
        LOGD(sb.toString());
        if (hasInternet(config) || maybeHasInternet(config) || (config.noInternetAccess && config.internetRecoveryStatus == 5)) {
            this.mHwArbitrationManager = HwArbitrationManager.getInstance();
            if (this.mHwArbitrationManager != null) {
                HashMap<Integer, String> preferList = this.mHwArbitrationManager.getWifiPreferenceFromHiData();
                LOGD("getWifiPreferenceFromHiData: " + preferList.toString());
                if (preferList.containsKey(0) && config.SSID.equals(preferList.get(0))) {
                    this.preferNetworkCandidate = config;
                    this.preferScanResultCandidate = scanResult;
                    this.preferNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                    LOGD("found avalible user preferred network: " + config.SSID);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean selectNetworkHasInternetEver(WifiConfiguration config, ScanResult scanResult) {
        boolean z = false;
        if (config == null || HUAWEI_GUEST.equals(config.configKey()) || !config.noInternetAccess || config.portalNetwork || !WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("selectNetworkHasInternetEver, recovery matched, candidate = ");
        sb.append(config.configKey(true));
        sb.append(", recoveryNetworkCandidate is null = ");
        if (this.recoveryNetworkCandidate == null) {
            z = true;
        }
        sb.append(z);
        LOGD(sb.toString());
        if (this.recoveryNetworkCandidate == null) {
            this.recoveryNetworkCandidate = config;
            this.recoveryScanResultCandidate = scanResult;
            this.recoveryNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (rssiStronger(scanResult, this.recoveryScanResultCandidate)) {
            LOGD("selectNetworkHasInternetEver, use the stronger network for recovery candidate, new candidate = " + config.configKey(true));
            this.recoveryNetworkCandidate = config;
            this.recoveryScanResultCandidate = scanResult;
            this.recoveryNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        }
        return true;
    }

    private boolean selectNetworkPortal(WifiConfiguration config, ScanResult scanResult) {
        if (config == null || scanResult == null) {
            return false;
        }
        if (!config.portalNetwork && !WifiProCommonUtils.isOpenAndMaybePortal(config) && !WifiProCommonUtils.isInMonitorList(config.configKey(), WifiProCommonUtils.NON_OPEN_PORTALS) && !HUAWEI_GUEST.equals(config.configKey())) {
            return false;
        }
        if (this.portalNotifiedConfigKey != null && this.portalNotifiedConfigKey.equals(config.configKey())) {
            this.portalDisappeared = false;
            this.portalDisappearedCounter = 0;
            if (scanResult.level > this.portalNotifiedMaxRssi) {
                this.portalNotifiedMaxRssi = scanResult.level;
            }
        }
        if (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) <= 1 || (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) <= 2 && (config.lastHasInternetTimestamp == 0 || System.currentTimeMillis() - config.lastHasInternetTimestamp >= 7200000))) {
            return true;
        }
        if (!isUserOnWlanSettings() && this.portalNotifiedConfigKey != null && !this.portalNotifiedConfigKey.equals(config.configKey()) && config.lastHasInternetTimestamp == 0) {
            return true;
        }
        if (!isUserOnWlanSettings() && this.portalNotifiedConfigKey != null && this.portalNotifiedConfigKey.equals(config.configKey()) && (!this.portalNotifiedHasInternet || WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) <= 2)) {
            return true;
        }
        if (!isUserOnWlanSettings() && HwAutoConnectManager.getInstance() != null && !HwAutoConnectManager.getInstance().allowCheckPortalNetwork(config.configKey(), scanResult.BSSID)) {
            return true;
        }
        if (this.portalNetworkCandidate == null) {
            LOGD("selectNetworkPortal, portal status unknown, backup it if no other choice, candidate = " + config.configKey());
            this.portalNetworkCandidate = config;
            this.portalScanResultCandidate = scanResult;
            this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (config.lastHasInternetTimestamp == 0 && this.portalNetworkCandidate.lastHasInternetTimestamp == 0) {
            if (rssiStronger(scanResult, this.portalScanResultCandidate)) {
                LOGD("selectNetworkPortal, use the stronger rssi for portal unauthen candidate, new candidate = " + config.configKey());
                this.portalNetworkCandidate = config;
                this.portalScanResultCandidate = scanResult;
                this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            }
        } else if (config.lastHasInternetTimestamp > this.portalNetworkCandidate.lastHasInternetTimestamp) {
            LOGD("selectNetworkPortal, use the portal network that login recently, new candidate = " + config.configKey());
            this.portalNetworkCandidate = config;
            this.portalScanResultCandidate = scanResult;
            this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        }
        return true;
    }

    private boolean selectNetworkNoInternet(WifiConfiguration config, ScanResult scanResult) {
        if (!config.noInternetAccess || WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        }
        if (this.recoveryNetworkCandidate == null && this.portalNetworkCandidate == null) {
            if (config.internetRecoveryCheckTimestamp > 0 && config.internetRecoveryStatus == 4 && System.currentTimeMillis() - config.internetRecoveryCheckTimestamp > 3600000) {
                LOGD("selectNetworkNoInternet, recovery unmatched, reset tobe unknown after 1h from last checking!");
                config.internetRecoveryStatus = 3;
            }
            if (config.internetRecoveryStatus == 4 && WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
                return true;
            }
            if (this.noInetNetworkCandidate == null) {
                LOGD("selectNetworkNoInternet, no internet network = " + config.configKey(true) + ", backup it if no other better one.");
                this.noInetNetworkCandidate = config;
                this.noInetScanResultCandidate = scanResult;
                this.noInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            } else if (rssiStronger(scanResult, this.noInetScanResultCandidate)) {
                this.noInetNetworkCandidate = config;
                this.noInetScanResultCandidate = scanResult;
                this.noInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            }
            return true;
        }
        LOGD("selectNetworkNoInternet, better network selected, skip due to no internet, candidate = " + config.configKey(true));
        return true;
    }

    private WifiConfiguration candidateUpdatedByWifiPro(WifiConfiguration config) {
        if (this.mWifiStateMachine.isWifiSelfCuring() && this.selfCureNetworkCandidate != null) {
            LOGD("Within Wifi Self Curing, the highest network is " + this.selfCureNetworkCandidate.configKey(true));
            this.mSelfCureCandidateLostCnt = 0;
            return this.selfCureNetworkCandidate;
        } else if (!this.mWifiStateMachine.isWifiSelfCuring() || this.selfCureNetworkCandidate != null) {
            WifiConfiguration newConfig = config;
            boolean isfactorymode = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
            if (isWifiProEnabled()) {
                this.mWifiStateMachine.setWifiBackgroundReason(5);
                if (this.preferNetworkCandidate != null) {
                    LOGD("HwEvaluator, use the preferred candidate from HiData, network = " + this.preferNetworkCandidate.configKey(true));
                    newConfig = this.preferNetworkCandidate;
                    this.backupTypeSelected = HIDATA_PREFER_SELECTED;
                } else if (this.hasInetNetworkCandidate != null) {
                    LOGD("HwEvaluator, use the best candidate has internet always or unknown, network = " + this.hasInetNetworkCandidate.configKey(true));
                    newConfig = this.hasInetNetworkCandidate;
                    this.backupTypeSelected = HAS_INET_SELECTED;
                } else if (this.recoveryNetworkCandidate != null) {
                    LOGD("HwEvaluator, use the recovery matched candidate(has internet ever), network = " + this.recoveryNetworkCandidate.configKey(true));
                    newConfig = this.recoveryNetworkCandidate;
                    this.backupTypeSelected = RECOVERY_SELECTED;
                } else if (this.portalNetworkCandidate != null) {
                    LOGD("HwEvaluator, use the best portal to connect, network = " + this.portalNetworkCandidate.configKey(true));
                    newConfig = this.portalNetworkCandidate;
                    this.backupTypeSelected = NORMAL_PORTAL_SELECTED;
                    if (!isUserOnWlanSettings() && !isfactorymode) {
                        this.mWifiStateMachine.setWifiBackgroundReason(0);
                    }
                } else if (this.noInetNetworkCandidate != null) {
                    if (!WifiProCommonUtils.isWifiProSwitchOn(this.mContext) || isfactorymode) {
                        LOGD("HwEvaluator, WLAN+ off, use no internet network(lowest priority) to connect, network = " + this.noInetNetworkCandidate.configKey(true));
                        newConfig = this.noInetNetworkCandidate;
                        this.backupTypeSelected = NO_INET_SELECTED;
                    } else if (!isUserOnWlanSettings() && this.noInetNetworkCandidate.internetRecoveryStatus != 4 && WifiProCommonUtils.matchedRequestByHistory(this.noInetNetworkCandidate.internetHistory, 104)) {
                        newConfig = this.noInetNetworkCandidate;
                        this.backupTypeSelected = NO_INET_SELECTED;
                        this.mWifiStateMachine.setWifiBackgroundReason(3);
                        LOGD("HwEvaluator, background connection for no internet access, network = " + this.noInetNetworkCandidate.configKey());
                    }
                }
            }
            return newConfig;
        } else {
            LOGD("candidateUpdatedByWifiPro, Within Wifi Self Curing and AP lost at this scan, skip connect others.");
            this.mSelfCureCandidateLostCnt++;
            if (this.mSelfCureCandidateLostCnt == 2) {
                LOGD("candidateUpdatedByWifiPro, stop self cure because AP lost 2 times");
                this.mWifiStateMachine.notifySelfCureNetworkLost();
            }
            return null;
        }
    }

    private ScanResult scanResultUpdatedByWifiPro(WifiConfiguration networkCandidate, ScanResult scanResult) {
        if (this.mWifiStateMachine.isWifiSelfCuring() && this.selfCureScanResultCandidate != null) {
            return this.selfCureScanResultCandidate;
        }
        if (!this.mWifiStateMachine.isWifiSelfCuring() || this.selfCureScanResultCandidate != null) {
            if (isWifiProEnabled() && networkCandidate != null) {
                LOGD("HwEvaluator, update scan result, selected type = " + this.backupTypeSelected);
                if (this.backupTypeSelected == HIDATA_PREFER_SELECTED) {
                    return this.preferScanResultCandidate;
                }
                if (this.backupTypeSelected == HAS_INET_SELECTED) {
                    return this.hasInetScanResultCandidate;
                }
                if (this.backupTypeSelected == RECOVERY_SELECTED) {
                    return this.recoveryScanResultCandidate;
                }
                if (this.backupTypeSelected == TRUSTED_PORTAL_SELECTED) {
                    return this.portalScanResultTursted;
                }
                if (this.backupTypeSelected == NORMAL_PORTAL_SELECTED) {
                    return this.portalScanResultCandidate;
                }
                if (this.backupTypeSelected == NO_INET_SELECTED) {
                    return this.noInetScanResultCandidate;
                }
            }
            return scanResult;
        }
        LOGD("scanResultUpdatedByWifiPro, Within Wifi Self Curing and AP lost at this scan, skip connect others.");
        return null;
    }

    public synchronized void handleSelectNetworkCompleted(WifiConfiguration candidate) {
        if (isWifiProEnabled() && this.mWifiStateMachine.getCurrentNetwork() == null) {
            if ((candidate == null ? -4 : 0) == -4) {
                if (this.portalNotifiedConfigKey != null && this.portalDisappeared) {
                    this.portalDisappearedCounter++;
                    LOGD("handleAutoJoinCompleted, notified portal = " + this.portalNotifiedConfigKey + ", disappear counter = " + this.portalDisappearedCounter);
                    if (this.portalDisappearedCounter >= PORTAL_DISAPPEAR_THRESHOLD) {
                        Intent intent = new Intent();
                        intent.setAction(WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE);
                        intent.setFlags(67108864);
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                } else if (!(this.portalNotifiedConfigKey == null || this.portalDisappeared || HwAutoConnectManager.getInstance() == null)) {
                    HwAutoConnectManager.getInstance().updatePopUpNetworkRssi(this.portalNotifiedConfigKey, this.portalNotifiedMaxRssi);
                }
            }
        }
    }

    public synchronized void resetSelfCureCandidateLostCnt() {
        this.mSelfCureCandidateLostCnt = 0;
    }

    public synchronized void portalNotifyChanged(boolean popUp, String configKey, boolean hasInternetAccess) {
        if (popUp) {
            try {
                this.portalNotifiedConfigKey = configKey;
                this.portalNotifiedHasInternet = hasInternetAccess;
            } catch (Throwable th) {
                throw th;
            }
        } else {
            this.portalNotifiedConfigKey = null;
            this.portalNotifiedHasInternet = false;
        }
        this.portalDisappearedCounter = 0;
    }

    private boolean rssiStronger(ScanResult newObj, ScanResult oldObj) {
        if (newObj == null || oldObj == null || newObj.level <= oldObj.level) {
            return false;
        }
        return true;
    }

    private boolean hasInternet(WifiConfiguration config) {
        return config != null && !config.noInternetAccess && !config.portalNetwork && WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
    }

    private boolean maybeHasInternet(WifiConfiguration config) {
        return !WifiProCommonUtils.isOpenType(config) && !config.noInternetAccess && !config.portalNetwork && WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 103);
    }

    private boolean isUserOnWlanSettings() {
        return WifiProCommonUtils.isQueryActivityMatched(this.mContext, "com.android.settings.Settings$WifiSettingsActivity");
    }

    private boolean isMobileDataInactive() {
        return WifiProCommonUtils.isMobileDataOff(this.mContext) || WifiProCommonUtils.isNoSIMCard(this.mContext) || WifiProCommonUtils.isWifiOnly(this.mContext);
    }

    private boolean isWifiProEnabled() {
        return true;
    }

    public synchronized boolean isWifiProEnabledOrSelfCureGoing() {
        return isWifiProEnabled() || this.mWifiStateMachine.isWifiSelfCuring();
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
