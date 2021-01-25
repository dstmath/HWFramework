package com.android.server.wifi.wifipro;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.SubscriptionManager;
import android.util.LocalLog;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.arbitration.HwArbitrationManager;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.Clock;
import com.android.server.wifi.HwQoE.HwQoEUtils;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.SavedNetworkEvaluator;
import com.android.server.wifi.ScanRequestProxy;
import com.android.server.wifi.ScanResultMatchInfo;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.WifiConnectivityHelper;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.HashMap;
import java.util.List;

public class HwSavedNetworkEvaluator extends SavedNetworkEvaluator {
    private static final String AP_TYPE_COMMON = "CommonAp";
    private static final int AP_TYPE_HAS_INTERNET = 1;
    private static final int AP_TYPE_NO_INTERNET = 0;
    private static final String AP_TYPE_PORTAL = "PortalAp";
    private static final int AP_TYPE_PORTAL_INTERNET = 2;
    private static int BACKUP_UNUSED = 0;
    private static int BLACK_LIST_SELECTED = HwQoEUtils.QOE_MSG_EVALUATE_OTA_INFO;
    private static final String FACTORY_MODE = "factory";
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_OK = 0;
    private static int HAS_INET_SELECTED = 100;
    private static int HIDATA_PREFER_SELECTED = HwQoEUtils.QOE_MSG_MONITOR_GET_QUALITY_INFO;
    private static final String HOME_PKT_NAME = "com.huawei.android.launcher";
    private static final String HUAWEI_GUEST = "\"Huawei-Guest\"NONE";
    private static final int MIN_3_LEVEL = -75;
    private static final int MSG_NOT_AUTO_CONNECT = 29;
    private static final int MSG_NOT_CONNECT = 1001;
    private static final int MSG_WEAK_SIGNAL = 1000;
    private static int NORMAL_PORTAL_SELECTED = 103;
    private static final int NOT_CONNECT_BLACKLIST = 1;
    private static final int NOT_CONNECT_DHCP_FAIL = 2;
    private static final int NOT_CONNECT_WEAK_SIGAL = 0;
    private static int NO_INET_SELECTED = HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET;
    private static int PORTAL_DISAPPEAR_THRESHOLD = 2;
    private static int RECOVERY_SELECTED = 101;
    private static final String TAG = "HwSavedNetworkEvaluator";
    private static int TRUSTED_PORTAL_SELECTED = 102;
    private int backupTypeSelected = BACKUP_UNUSED;
    private WifiConfiguration hasInetNetworkCandidate = null;
    private ScanResult hasInetScanResultCandidate = null;
    private WifiConfiguration mBlackListNetworkCandidate = null;
    private ScanResult mBlackListScanResultCandidate = null;
    private Context mContext;
    private HwArbitrationManager mHwArbitrationManager = null;
    private HwWifiCHRService mHwWifiChrService;
    private HwWifiProServiceManager mHwWifiProServiceManager;
    private PowerManager mPowerManager;
    private int mSelfCureCandidateLostCnt = 0;
    private final WifiConfigManager mWifiConfigManager;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private ClientModeImpl mWifiStateMachine;
    private WifiConfiguration noInetNetworkCandidate = null;
    private ScanResult noInetScanResultCandidate = null;
    private boolean portalDisappeared = true;
    private int portalDisappearedCounter = 0;
    private WifiConfiguration portalNetworkCandidate = null;
    private String portalNotifiedConfigKey = null;
    private boolean portalNotifiedHasInternet = false;
    private int portalNotifiedMaxRssi = -200;
    private ScanResult portalScanResultCandidate = null;
    private ScanResult portalScanResultTursted = null;
    private WifiConfiguration preferNetworkCandidate = null;
    private ScanResult preferScanResultCandidate = null;
    private WifiConfiguration recoveryNetworkCandidate = null;
    private ScanResult recoveryScanResultCandidate = null;
    private WifiConfiguration selfCureNetworkCandidate = null;
    private ScanResult selfCureScanResultCandidate = null;

    public HwSavedNetworkEvaluator(Context context, ScoringParams scoringParams, WifiConfigManager configManager, Clock clock, LocalLog localLog, ClientModeImpl wsm, WifiConnectivityHelper connectivityHelper, SubscriptionManager subscriptionManager) {
        super(context, scoringParams, configManager, clock, localLog, connectivityHelper, subscriptionManager);
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiNative = WifiInjector.getInstance().getWifiNative();
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        this.mHwWifiChrService = HwWifiServiceFactory.getHwWifiCHRService();
        this.mWifiConfigManager = configManager;
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
        this.mBlackListNetworkCandidate = null;
        this.mBlackListScanResultCandidate = null;
        this.backupTypeSelected = BACKUP_UNUSED;
        this.portalDisappeared = true;
        this.portalNotifiedMaxRssi = -200;
    }

    public boolean isNetworkEnabledExtended(WifiConfiguration config, WifiConfiguration.NetworkSelectionStatus status) {
        if ((status.isNetworkEnabled() || !this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted() || !this.mHwWifiProServiceManager.allowAutoJoinDisabledNetworkAgain(config)) && !(102 == WifiProCommonUtils.getSelfCuringState() && config.networkId == this.mWifiStateMachine.getSelfCureNetworkId())) {
            boolean isEnableExtend = status.isNetworkEnabled();
            if (!isEnableExtend) {
                uploadNotAutoConnectChrEvent(config, 1);
            }
            return isEnableExtend;
        }
        HwHiLog.d(TAG, false, "isNetworkEnabledExtended, allowAutoJoinDisabledNetworkAgain = %{private}s", new Object[]{config.configKey()});
        return true;
    }

    public boolean unselectDueToFailedLastTime(ScanResult scanResult, WifiConfiguration config) {
        if (!(scanResult == null || config == null || (config.lastConnFailedType != 3 && config.lastConnFailedType != 2 && config.lastConnFailedType != 4))) {
            long deltaMs = System.currentTimeMillis() - config.lastConnFailedTimestamp;
            WifiConfiguration.NetworkSelectionStatus status = config.getNetworkSelectionStatus();
            int count = status.getDisableReasonCounter(config.lastConnFailedType);
            if (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) >= 4) {
                if (config.rssiStatusDisabled != -200 && config.rssiStatusDisabled < MIN_3_LEVEL) {
                    status.setNetworkSelectionStatus(0);
                    status.setDisableTime(-1);
                    status.setNetworkSelectionDisableReason(0);
                    config.rssiStatusDisabled = -200;
                }
                return false;
            } else if (isUserOnWlanSettings()) {
                return false;
            } else {
                if (deltaMs > 300000) {
                    config.lastConnFailedType = 0;
                    config.lastConnFailedTimestamp = 0;
                    config.rssiStatusDisabled = -200;
                    return false;
                } else if ((count == 1 && deltaMs < 10000) || ((count == 2 && deltaMs < 30000) || ((count == 3 && deltaMs < 60000) || (count == 4 && deltaMs < 90000)))) {
                    HwHiLog.d(TAG, false, "unselectDueToFailedLastTime, DELAYED!!! count = %{public}d, deltaMs = %{public}s, ssid = %{public}s, level = %{public}d", new Object[]{Integer.valueOf(count), String.valueOf(deltaMs), scanResult.SSID, Integer.valueOf(scanResult.level)});
                    uploadNotAutoConnectChrEvent(scanResult, 2);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean unselectDhcpFailedBssid(ScanResult scanResult, WifiConfiguration config) {
        if (scanResult == null || config == null || !this.mHwWifiProServiceManager.isHwSelfCureEngineStarted() || !this.mHwWifiProServiceManager.isDhcpFailedBssid(scanResult.BSSID)) {
            return false;
        }
        HwHiLog.d(TAG, false, "unselectDhcpFailedBssid, key = %{private}s", new Object[]{config.configKey()});
        uploadNotAutoConnectChrEvent(scanResult, 1);
        return true;
    }

    private boolean unselectPoorNetwork(ScanResult scanResult, WifiConfiguration config) {
        if (scanResult == null || config == null || isMobileDataInactive() || WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) >= 3 || (this.mHwWifiProServiceManager.isHwSelfCureEngineStarted() && this.mHwWifiProServiceManager.isDhcpFailedConfigKey(config.configKey()))) {
            return false;
        }
        long ts = this.mWifiStateMachine.getWifiEnabledTimeStamp();
        if (ts != 0 && System.currentTimeMillis() - ts < 20000) {
            return false;
        }
        String pktName = "";
        if (this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted()) {
            pktName = this.mHwWifiProServiceManager.getCurrentPackageNameFromWifiPro();
        }
        if (!"com.huawei.android.launcher".equals(pktName)) {
            HwHiLog.d(TAG, false, "unselectPoorNetwork, DELAY!!! level = %{public}d, ssid = %{public}s", new Object[]{Integer.valueOf(scanResult.level), scanResult.SSID});
            if (config.SSID != null) {
                this.mHwWifiProServiceManager.notifyChrEvent(1000, config.portalNetwork ? AP_TYPE_PORTAL : AP_TYPE_COMMON, config.SSID, scanResult.frequency);
            }
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
            HwHiLog.d(TAG, false, "unselectDiscNonLocally, DELAYED this bssid !!! current = %{public}d, disc = %{public}d, ssid = %{public}s", new Object[]{Integer.valueOf(scanResult.level), Integer.valueOf(config.rssiDiscNonLocally), StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
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

    public synchronized boolean selectBestNetworkByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        if (!this.mWifiStateMachine.isWifiSelfCuring() || config == null || config.networkId != this.mWifiStateMachine.getSelfCureNetworkId()) {
            if (isWifiProEnabled() && this.mWifiStateMachine.getCurrentNetwork() == null && config != null && scanResult != null) {
                HwHiLog.d(TAG, false, "selectBestNetworkByWifiPro, current = %{public}s, internetHistory = %{public}s, level = %{public}d, 5GHz = %{public}s, portalNetwork = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid()), config.internetHistory, Integer.valueOf(scanResult.level), String.valueOf(scanResult.is5GHz()), String.valueOf(config.portalNetwork)});
                if (networkIgnoredByWifiPro(config, scanResult) || selectNetworkHidataPrefer(config, scanResult) || selectNetworkHasInternet(config, scanResult) || selectNetworkHasInternetEver(config, scanResult) || selectNetworkPortal(config, scanResult) || selectNetworkNoInternet(config, scanResult)) {
                    return true;
                }
            }
            return false;
        }
        HwHiLog.d(TAG, false, "selectBestNetworkByWifiPro, wifi self curing, forced connecting network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
        if (this.selfCureNetworkCandidate == null || this.selfCureScanResultCandidate == null) {
            this.selfCureNetworkCandidate = config;
            this.selfCureScanResultCandidate = scanResult;
            this.selfCureNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (scanResult != null && scanResult.level > this.selfCureScanResultCandidate.level) {
            this.selfCureNetworkCandidate = config;
            this.selfCureScanResultCandidate = scanResult;
            this.selfCureNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        }
        return true;
    }

    private boolean networkIgnoredByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        if (config != null && config.isTempCreated) {
            HwHiLog.d(TAG, false, "%{public}s, networkIgnoredByAPScore, skip candidate due to istempcreated", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
            return true;
        } else if (scanResult == null || !this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted() || !this.mHwWifiProServiceManager.isBssidMatchedBlacklist(scanResult.BSSID)) {
            return false;
        } else {
            HwHiLog.d(TAG, false, "%{public}s is in black list", new Object[]{StringUtilEx.safeDisplayBssid(scanResult.BSSID)});
            selectNetworkFromBlackList(config, scanResult);
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
        WifiConfiguration wifiConfiguration = this.hasInetNetworkCandidate;
        if (wifiConfiguration == null) {
            this.hasInetNetworkCandidate = config;
            this.hasInetScanResultCandidate = scanResult;
            this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (!maybeHasInternet(wifiConfiguration) && maybeHasInternet(config)) {
            return true;
        } else {
            if (!maybeHasInternet(this.hasInetNetworkCandidate) || maybeHasInternet(config)) {
                int newScore = WifiProCommonUtils.calculateScore(scanResult);
                int currentScore = WifiProCommonUtils.calculateScore(this.hasInetScanResultCandidate);
                dumpCandidateScore(newScore, currentScore, scanResult);
                if (newScore > currentScore || (newScore == currentScore && scanResult.level > this.hasInetScanResultCandidate.level)) {
                    this.hasInetNetworkCandidate = config;
                    this.hasInetScanResultCandidate = scanResult;
                    this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                    return true;
                }
            } else {
                this.hasInetNetworkCandidate = config;
                this.hasInetScanResultCandidate = scanResult;
                this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                return true;
            }
        }
        return true;
    }

    private void dumpCandidateScore(int newScore, int currentScore, ScanResult scanResult) {
        if (this.mWifiManager == null) {
            this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        }
        if (this.mWifiManager.getVerboseLoggingLevel() > 0 && scanResult != null) {
            HwHiLog.i(TAG, false, "BSSID = %{public}s, is5g = %{public}s, supportedWifiCategory = %{public}d, rssi = %{public}d, newScore = %{public}d, currentScore= %{public}d", new Object[]{StringUtilEx.safeDisplayBssid(scanResult.BSSID), String.valueOf(ScanResult.is5GHz(scanResult.frequency)), Integer.valueOf(scanResult.supportedWifiCategory), Integer.valueOf(scanResult.level), Integer.valueOf(newScore), Integer.valueOf(currentScore)});
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0036: APUT  
      (r2v1 java.lang.Object[])
      (3 ??[int, float, short, byte, char])
      (wrap: java.lang.String : 0x0032: INVOKE  (r5v4 java.lang.String) = (r5v3 boolean) type: STATIC call: java.lang.String.valueOf(boolean):java.lang.String)
     */
    private boolean selectNetworkHidataPrefer(WifiConfiguration config, ScanResult scanResult) {
        if (config == null) {
            return false;
        }
        Object[] objArr = new Object[4];
        objArr[0] = String.valueOf(hasInternet(config));
        objArr[1] = String.valueOf(maybeHasInternet(config));
        objArr[2] = String.valueOf(config.noInternetAccess);
        objArr[3] = String.valueOf(config.internetRecoveryStatus == 5);
        HwHiLog.d(TAG, false, "selectNetworkHidataPrefer: %{public}s,%{public}s,%{public}s,%{public}s", objArr);
        if (hasInternet(config) || maybeHasInternet(config) || (config.noInternetAccess && config.internetRecoveryStatus == 5)) {
            this.mHwArbitrationManager = HwArbitrationManager.getInstance();
            HwArbitrationManager hwArbitrationManager = this.mHwArbitrationManager;
            if (hwArbitrationManager != null) {
                HashMap<Integer, String> preferList = hwArbitrationManager.getWifiPreferenceFromHiData();
                HwHiLog.d(TAG, false, "getWifiPreferenceFromHiData: %{public}s", new Object[]{String.valueOf(preferList.size())});
                if (preferList.containsKey(0) && config.SSID.equals(preferList.get(0))) {
                    this.preferNetworkCandidate = config;
                    this.preferScanResultCandidate = scanResult;
                    this.preferNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                    HwHiLog.d(TAG, false, "found avalible user preferred network: %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.SSID)});
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0039: APUT  
      (r1v7 java.lang.Object[])
      (1 ??[boolean, int, float, short, byte, char])
      (wrap: java.lang.String : 0x0035: INVOKE  (r2v5 java.lang.String) = (r2v4 boolean) type: STATIC call: java.lang.String.valueOf(boolean):java.lang.String)
     */
    private boolean selectNetworkHasInternetEver(WifiConfiguration config, ScanResult scanResult) {
        if (config == null || HUAWEI_GUEST.equals(config.configKey()) || !config.noInternetAccess || config.portalNetwork || !WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        }
        Object[] objArr = new Object[2];
        objArr[0] = StringUtilEx.safeDisplaySsid(config.getPrintableSsid());
        objArr[1] = String.valueOf(this.recoveryNetworkCandidate == null);
        HwHiLog.d(TAG, false, "selectNetworkHasInternetEver, recovery matched, candidate = %{public}s, recoveryNetworkCandidate is null = %{public}s", objArr);
        if (this.recoveryNetworkCandidate == null) {
            this.recoveryNetworkCandidate = config;
            this.recoveryScanResultCandidate = scanResult;
            this.recoveryNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        } else if (rssiStronger(scanResult, this.recoveryScanResultCandidate)) {
            HwHiLog.d(TAG, false, "selectNetworkHasInternetEver, use the stronger network for recovery candidate, new candidate = %{private}s", new Object[]{config.configKey(true)});
            this.recoveryNetworkCandidate = config;
            this.recoveryScanResultCandidate = scanResult;
            this.recoveryNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        }
        return true;
    }

    private boolean selectNetworkPortal(WifiConfiguration config, ScanResult scanResult) {
        String str;
        String str2;
        if (config == null || scanResult == null) {
            return false;
        }
        if (!config.portalNetwork && ((ScanResultUtil.isScanResultForOweNetwork(scanResult) || !WifiProCommonUtils.isOpenAndMaybePortal(config)) && !WifiProCommonUtils.isInMonitorList(config.configKey(), WifiProCommonUtils.NON_OPEN_PORTALS) && !HUAWEI_GUEST.equals(config.configKey()))) {
            return false;
        }
        String str3 = this.portalNotifiedConfigKey;
        if (str3 != null && str3.equals(config.configKey())) {
            this.portalDisappeared = false;
            this.portalDisappearedCounter = 0;
            if (scanResult.level > this.portalNotifiedMaxRssi) {
                this.portalNotifiedMaxRssi = scanResult.level;
            }
        }
        if (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) <= 1 || (WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) <= 2 && (config.lastHasInternetTimestamp == 0 || System.currentTimeMillis() - config.lastHasInternetTimestamp >= 7200000))) {
            if (!(this.mHwWifiProServiceManager == null || config.SSID == null)) {
                this.mHwWifiProServiceManager.notifyChrEvent(1001, AP_TYPE_PORTAL, config.SSID, scanResult.frequency);
            }
            return true;
        } else if (!isUserOnWlanSettings() && (str2 = this.portalNotifiedConfigKey) != null && !str2.equals(config.configKey()) && config.lastHasInternetTimestamp == 0) {
            return true;
        } else {
            if (!isUserOnWlanSettings() && (str = this.portalNotifiedConfigKey) != null && str.equals(config.configKey()) && (!this.portalNotifiedHasInternet || WifiProCommonUtils.getSignalLevel(scanResult.frequency, scanResult.level) <= 2)) {
                return true;
            }
            if (!isUserOnWlanSettings() && this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted() && !this.mHwWifiProServiceManager.allowCheckPortalNetwork(config.configKey(), scanResult.BSSID)) {
                return true;
            }
            if (this.portalNetworkCandidate == null) {
                HwHiLog.d(TAG, false, "selectNetworkPortal, portal status unknown, backup it if no other choice, candidate = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                this.portalNetworkCandidate = config;
                this.portalScanResultCandidate = scanResult;
                this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            } else if (config.lastHasInternetTimestamp == 0 && this.portalNetworkCandidate.lastHasInternetTimestamp == 0) {
                if (rssiStronger(scanResult, this.portalScanResultCandidate)) {
                    HwHiLog.d(TAG, false, "selectNetworkPortal, use the stronger rssi for portal unauthen candidate, new candidate = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                    this.portalNetworkCandidate = config;
                    this.portalScanResultCandidate = scanResult;
                    this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                }
            } else if (config.lastHasInternetTimestamp > this.portalNetworkCandidate.lastHasInternetTimestamp) {
                HwHiLog.d(TAG, false, "selectNetworkPortal, use the portal network that login recently, new candidate = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
                this.portalNetworkCandidate = config;
                this.portalScanResultCandidate = scanResult;
                this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            }
            return true;
        }
    }

    private boolean selectNetworkNoInternet(WifiConfiguration config, ScanResult scanResult) {
        if (!config.noInternetAccess || WifiProCommonUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        }
        if (this.recoveryNetworkCandidate == null && this.portalNetworkCandidate == null) {
            if (config.internetRecoveryCheckTimestamp > 0 && config.internetRecoveryStatus == 4 && System.currentTimeMillis() - config.internetRecoveryCheckTimestamp > 3600000) {
                HwHiLog.d(TAG, false, "selectNetworkNoInternet, recovery unmatched, reset tobe unknown after 1h from last checking!", new Object[0]);
                config.internetRecoveryStatus = 3;
            }
            if (config.internetRecoveryStatus != 4 || !WifiProCommonUtils.isWifiProSwitchOn(this.mContext)) {
                if (this.noInetNetworkCandidate == null) {
                    HwHiLog.d(TAG, false, "selectNetworkNoInternet, no internet network = %{public}s, backup it if no other better one.", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
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
            if (!(this.mHwWifiProServiceManager == null || config.SSID == null)) {
                this.mHwWifiProServiceManager.notifyChrEvent(1001, config.portalNetwork ? AP_TYPE_PORTAL : AP_TYPE_COMMON, config.SSID, scanResult.frequency);
            }
            return true;
        }
        HwHiLog.d(TAG, false, "selectNetworkNoInternet, better network selected, skip due to no internet, candidate = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(config.getPrintableSsid())});
        return true;
    }

    private boolean selectNetworkFromBlackList(WifiConfiguration config, ScanResult scanResult) {
        if (config == null || scanResult == null) {
            HwHiLog.e(TAG, false, "selectNetworkFromBlackList, parameter is invalid", new Object[0]);
            return false;
        }
        this.mBlackListNetworkCandidate = config;
        this.mBlackListScanResultCandidate = scanResult;
        this.mBlackListNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
        return true;
    }

    private WifiConfiguration candidateUpdatedByWifiPro(WifiConfiguration config) {
        WifiConfiguration wifiConfiguration;
        this.backupTypeSelected = BACKUP_UNUSED;
        if (this.mWifiStateMachine.isWifiSelfCuring() && (wifiConfiguration = this.selfCureNetworkCandidate) != null) {
            HwHiLog.d(TAG, false, "Within Wifi Self Curing, the highest network is %{public}s", new Object[]{wifiConfiguration.configKey(true)});
            this.mSelfCureCandidateLostCnt = 0;
            return this.selfCureNetworkCandidate;
        } else if (!this.mWifiStateMachine.isWifiSelfCuring() || this.selfCureNetworkCandidate != null) {
            WifiConfiguration newConfig = config;
            boolean isfactorymode = FACTORY_MODE.equals(SystemProperties.get("ro.runmode", "normal"));
            if (!isWifiProEnabled()) {
                return newConfig;
            }
            this.mWifiStateMachine.setWifiBackgroundReason(5);
            WifiConfiguration wifiConfiguration2 = this.preferNetworkCandidate;
            if (wifiConfiguration2 != null) {
                HwHiLog.d(TAG, false, "HwEvaluator, use the preferred candidate from HiData, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(wifiConfiguration2.getPrintableSsid())});
                newConfig = this.preferNetworkCandidate;
                this.backupTypeSelected = HIDATA_PREFER_SELECTED;
            } else {
                WifiConfiguration wifiConfiguration3 = this.hasInetNetworkCandidate;
                if (wifiConfiguration3 != null) {
                    HwHiLog.d(TAG, false, "HwEvaluator, use the best candidate has internet always or unknown, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(wifiConfiguration3.getPrintableSsid())});
                    newConfig = this.hasInetNetworkCandidate;
                    this.backupTypeSelected = HAS_INET_SELECTED;
                } else {
                    WifiConfiguration wifiConfiguration4 = this.recoveryNetworkCandidate;
                    if (wifiConfiguration4 != null) {
                        HwHiLog.d(TAG, false, "HwEvaluator, use the recovery matched candidate(has internet ever), network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(wifiConfiguration4.getPrintableSsid())});
                        newConfig = this.recoveryNetworkCandidate;
                        this.backupTypeSelected = RECOVERY_SELECTED;
                    } else {
                        WifiConfiguration wifiConfiguration5 = this.portalNetworkCandidate;
                        if (wifiConfiguration5 != null) {
                            HwHiLog.d(TAG, false, "HwEvaluator, use the best portal to connect, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(wifiConfiguration5.getPrintableSsid())});
                            newConfig = this.portalNetworkCandidate;
                            this.backupTypeSelected = NORMAL_PORTAL_SELECTED;
                            if (!isUserOnWlanSettings() && !isfactorymode && !WifiCommonUtils.IS_TV) {
                                HwHiLog.d(TAG, false, "HwEvaluator, to set WifiBackgroundReason WIFI_BACKGROUND_PORTAL_CHECKING", new Object[0]);
                                this.mWifiStateMachine.setWifiBackgroundReason(0);
                            }
                        } else if (this.noInetNetworkCandidate == null) {
                            HwHiLog.d(TAG, false, "HwEvaluator, ready to select blacklist candidate", new Object[0]);
                        } else if (!WifiProCommonUtils.isWifiProSwitchOn(this.mContext) || isfactorymode) {
                            HwHiLog.d(TAG, false, "HwEvaluator, WLAN+ off, use no internet network(lowest priority) to connect, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.noInetNetworkCandidate.getPrintableSsid())});
                            newConfig = this.noInetNetworkCandidate;
                            this.backupTypeSelected = NO_INET_SELECTED;
                        } else if (!isUserOnWlanSettings() && this.noInetNetworkCandidate.internetRecoveryStatus != 4 && WifiProCommonUtils.matchedRequestByHistory(this.noInetNetworkCandidate.internetHistory, (int) HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET)) {
                            newConfig = this.noInetNetworkCandidate;
                            this.backupTypeSelected = NO_INET_SELECTED;
                            this.mWifiStateMachine.setWifiBackgroundReason(3);
                            HwHiLog.d(TAG, false, "HwEvaluator, background connection for no internet access, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.noInetNetworkCandidate.getPrintableSsid())});
                        }
                    }
                }
            }
            if (this.mBlackListNetworkCandidate == null || this.backupTypeSelected != BACKUP_UNUSED) {
                return newConfig;
            }
            return updateBlackListCandidateByWifiPro(newConfig);
        } else {
            HwHiLog.d(TAG, false, "candidateUpdatedByWifiPro, Within Wifi Self Curing and AP lost at this scan, skip connect others.", new Object[0]);
            this.mSelfCureCandidateLostCnt++;
            if (this.mSelfCureCandidateLostCnt != 2) {
                return null;
            }
            HwHiLog.d(TAG, false, "candidateUpdatedByWifiPro, stop self cure because AP lost 2 times", new Object[0]);
            this.mWifiStateMachine.notifySelfCureNetworkLost();
            return null;
        }
    }

    private WifiConfiguration updateBlackListCandidateByWifiPro(WifiConfiguration config) {
        ScanResult scanResult;
        boolean isFactoryMode = FACTORY_MODE.equals(SystemProperties.get("ro.runmode", "normal"));
        if (this.mBlackListNetworkCandidate.portalNetwork || (WifiProCommonUtils.isOpenAndMaybePortal(this.mBlackListNetworkCandidate) && (scanResult = this.mBlackListScanResultCandidate) != null && !ScanResultUtil.isScanResultForOweNetwork(scanResult))) {
            WifiConfiguration newConfig = this.mBlackListNetworkCandidate;
            this.backupTypeSelected = BLACK_LIST_SELECTED;
            if (isUserOnWlanSettings() || isFactoryMode) {
                return newConfig;
            }
            HwHiLog.d(TAG, false, "HwEvaluator, use the portal network in black list to connect", new Object[0]);
            this.mWifiStateMachine.setWifiBackgroundReason(0);
            return newConfig;
        } else if (!this.mBlackListNetworkCandidate.noInternetAccess) {
            HwHiLog.d(TAG, false, "HwEvaluator, use the black list network to connect, ssid = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.mBlackListNetworkCandidate.SSID)});
            WifiConfiguration newConfig2 = this.mBlackListNetworkCandidate;
            this.backupTypeSelected = BLACK_LIST_SELECTED;
            return newConfig2;
        } else if (!WifiProCommonUtils.isWifiProSwitchOn(this.mContext) || isFactoryMode) {
            HwHiLog.d(TAG, false, "HwEvaluator, WLAN+ off, use no internet network in blacklist to connect, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.mBlackListNetworkCandidate.getPrintableSsid())});
            WifiConfiguration newConfig3 = this.mBlackListNetworkCandidate;
            this.backupTypeSelected = BLACK_LIST_SELECTED;
            return newConfig3;
        } else if (isUserOnWlanSettings() || this.mBlackListNetworkCandidate.internetRecoveryStatus == 4 || !WifiProCommonUtils.matchedRequestByHistory(this.mBlackListNetworkCandidate.internetHistory, (int) HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET)) {
            return config;
        } else {
            WifiConfiguration newConfig4 = this.mBlackListNetworkCandidate;
            this.backupTypeSelected = BLACK_LIST_SELECTED;
            this.mWifiStateMachine.setWifiBackgroundReason(3);
            HwHiLog.d(TAG, false, "background connection for blacklist no internet access, network = %{public}s", new Object[]{StringUtilEx.safeDisplaySsid(this.mBlackListNetworkCandidate.getPrintableSsid())});
            return newConfig4;
        }
    }

    private ScanResult scanResultUpdatedByWifiPro(WifiConfiguration networkCandidate, ScanResult scanResult) {
        ScanResult scanResult2;
        if (this.mWifiStateMachine.isWifiSelfCuring() && (scanResult2 = this.selfCureScanResultCandidate) != null) {
            return scanResult2;
        }
        if (!this.mWifiStateMachine.isWifiSelfCuring() || this.selfCureScanResultCandidate != null) {
            if (isWifiProEnabled() && networkCandidate != null) {
                HwHiLog.d(TAG, false, "HwEvaluator, update scan result, selected type = %{public}d", new Object[]{Integer.valueOf(this.backupTypeSelected)});
                int i = this.backupTypeSelected;
                if (i == HIDATA_PREFER_SELECTED) {
                    return this.preferScanResultCandidate;
                }
                if (i == HAS_INET_SELECTED) {
                    return this.hasInetScanResultCandidate;
                }
                if (i == RECOVERY_SELECTED) {
                    return this.recoveryScanResultCandidate;
                }
                if (i == TRUSTED_PORTAL_SELECTED) {
                    return this.portalScanResultTursted;
                }
                if (i == NORMAL_PORTAL_SELECTED) {
                    return this.portalScanResultCandidate;
                }
                if (i == NO_INET_SELECTED) {
                    return this.noInetScanResultCandidate;
                }
                if (i == BLACK_LIST_SELECTED) {
                    return this.mBlackListScanResultCandidate;
                }
                HwHiLog.d(TAG, false, "HwEvaluator, unsupported selected type", new Object[0]);
            }
            return scanResult;
        }
        HwHiLog.d(TAG, false, "scanResultUpdatedByWifiPro, Within Wifi Self Curing and AP lost at this scan, skip connect others.", new Object[0]);
        return null;
    }

    public synchronized void handleSelectNetworkCompleted(WifiConfiguration candidate) {
        if (isWifiProEnabled() && this.mWifiStateMachine.getCurrentNetwork() == null) {
            if ((candidate == null ? -4 : 0) == -4) {
                if (this.portalNotifiedConfigKey != null && this.portalDisappeared) {
                    this.portalDisappearedCounter++;
                    HwHiLog.d(TAG, false, "handleAutoJoinCompleted, notified portal = %{public}s, disappear counter = %{public}d", new Object[]{this.portalNotifiedConfigKey, Integer.valueOf(this.portalDisappearedCounter)});
                    if (this.portalDisappearedCounter >= PORTAL_DISAPPEAR_THRESHOLD) {
                        Intent intent = new Intent();
                        intent.setAction(HwWifiProServiceManager.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE);
                        intent.setFlags(67108864);
                        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                    }
                } else if (this.portalNotifiedConfigKey != null && !this.portalDisappeared && this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted()) {
                    this.mHwWifiProServiceManager.updatePopUpNetworkRssi(this.portalNotifiedConfigKey, this.portalNotifiedMaxRssi);
                }
            }
        }
    }

    public synchronized void resetSelfCureCandidateLostCnt() {
        this.mSelfCureCandidateLostCnt = 0;
    }

    public synchronized void portalNotifyChanged(boolean popUp, String configKey, boolean hasInternetAccess) {
        if (popUp) {
            this.portalNotifiedConfigKey = configKey;
            this.portalNotifiedHasInternet = hasInternetAccess;
        } else {
            this.portalNotifiedConfigKey = null;
            this.portalNotifiedHasInternet = false;
        }
        this.portalDisappearedCounter = 0;
    }

    private boolean rssiStronger(ScanResult newObj, ScanResult oldObj) {
        if (newObj == null || oldObj == null) {
            return false;
        }
        int currentScore = WifiProCommonUtils.calculateScore(oldObj);
        int newScore = WifiProCommonUtils.calculateScore(newObj);
        if (newScore > currentScore) {
            return true;
        }
        if (newScore != currentScore || newObj.level <= oldObj.level) {
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
        return WifiProCommonUtils.isQueryActivityMatched(this.mContext, WifiProCommonUtils.HUAWEI_SETTINGS_WLAN);
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

    private void uploadNotAutoConnectChrEvent(ScanResult scanResult, int rejectReason) {
        if (scanResult == null) {
            HwHiLog.e(TAG, false, "uploadNotAutoConnectChrEvent scanResult is null", new Object[0]);
            return;
        }
        WifiConfiguration config = getConfigFromScanResult(scanResult);
        if (config == null) {
            HwHiLog.e(TAG, false, "config is null, no valid config in scan result", new Object[0]);
        } else {
            uploadChrEvent(scanResult, rejectReason, config);
        }
    }

    private void uploadNotAutoConnectChrEvent(WifiConfiguration config, int rejectReason) {
        if (config == null) {
            HwHiLog.e(TAG, false, "uploadNotAutoConnectChrEvent config is null", new Object[0]);
            return;
        }
        ScanResult scanResult = getScanResultFromConfig(config);
        if (scanResult == null) {
            HwHiLog.e(TAG, false, "scanResult is null, no scanResult in config", new Object[0]);
        } else {
            uploadChrEvent(scanResult, rejectReason, config);
        }
    }

    private void uploadChrEvent(ScanResult scanResult, int rejectReason, WifiConfiguration config) {
        Context context;
        int apType;
        if (this.mHwWifiChrService == null || (context = this.mContext) == null || scanResult == null || config == null) {
            HwHiLog.e(TAG, false, "uploadChrEvent is null", new Object[0]);
            return;
        }
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) context.getSystemService("power");
        }
        boolean isScreenOn = this.mPowerManager.isScreenOn();
        if (config.portalNetwork) {
            apType = 2;
        } else {
            apType = config.noInternetAccess ? 0 : 1;
        }
        Bundle data = new Bundle();
        data.putInt("isScreenOn", isScreenOn ? 1 : 0);
        data.putInt("RejectModule", 0);
        data.putInt("RejectReason", rejectReason);
        data.putString("SSID", config.SSID);
        data.putString("BSS", scanResult.BSSID);
        data.putInt("keyMgmt", ScanResultMatchInfo.getNetworkType(scanResult));
        data.putInt("eap", config.enterpriseConfig == null ? -1 : config.enterpriseConfig.getEapMethod());
        data.putInt("frequency", scanResult.frequency);
        data.putInt("RSSI", scanResult.level);
        data.putInt("ApType", apType);
        this.mHwWifiChrService.uploadDFTEvent(29, data);
    }

    private WifiConfiguration getConfigFromScanResult(ScanResult scanResult) {
        WifiConfigManager wifiConfigManager;
        if (scanResult == null || (wifiConfigManager = this.mWifiConfigManager) == null) {
            HwHiLog.w(TAG, false, "getConfigFromScanResult scanResult are invalid", new Object[0]);
            return null;
        }
        List<WifiConfiguration> configs = wifiConfigManager.getConfiguredNetworks();
        if (configs == null || configs.size() == 0) {
            HwHiLog.w(TAG, false, "getConfigFromScanResult configs are invalid", new Object[0]);
            return null;
        }
        String scanSsid = "\"" + scanResult.SSID + "\"";
        for (WifiConfiguration config : configs) {
            if (scanSsid.equals(config.SSID) && ScanResultMatchInfo.getNetworkType(scanResult) == ScanResultMatchInfo.getNetworkType(config)) {
                return config;
            }
        }
        return null;
    }

    private ScanResult getScanResultFromConfig(WifiConfiguration config) {
        if (config == null) {
            HwHiLog.w(TAG, false, "getScanResultFromConfig config is invalid", new Object[0]);
            return null;
        }
        ScanRequestProxy scanProxy = WifiInjector.getInstance().getScanRequestProxy();
        if (scanProxy == null) {
            return null;
        }
        synchronized (scanProxy) {
            List<ScanResult> scanResults = scanProxy.getScanResults();
            if (scanResults == null) {
                return null;
            }
            for (ScanResult scanResult : scanResults) {
                if (("\"" + scanResult.SSID + "\"").equals(config.SSID) && ScanResultMatchInfo.getNetworkType(scanResult) == ScanResultMatchInfo.getNetworkType(config)) {
                    return scanResult;
                }
            }
            return null;
        }
    }
}
