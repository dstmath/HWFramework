package com.android.server.wifi;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.net.wifi.wifipro.WifiProStatusUtils;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Log;
import com.android.server.wifi.wifipro.HwDualBandMessageUtil;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwWifiQualifiedNetworkSelector extends WifiQualifiedNetworkSelector {
    private static int BACKUP_UNUSED = 0;
    private static int CONSECUTIVE_GOOD_RSSI_COUNTER = 0;
    public static final int HANDOVER_STATUS_DISALLOWED = -4;
    public static final int HANDOVER_STATUS_OK = 0;
    private static int HAS_INET_SELECTED = 0;
    private static final int MIN_3_LEVEL = -75;
    private static int MIN_MACHINE_BOOT_MS = 0;
    private static int NORMAL_PORTAL_SELECTED = 0;
    private static int NO_INET_SELECTED = 0;
    private static int PORTAL_DISAPPEAR_THRESHOLD = 0;
    private static int RECOVERY_SELECTED = 0;
    private static final String TAG = "HwWifiQualifiedNetworkSelector";
    private static int TRUSTED_PORTAL_SELECTED;
    private int backupTypeSelected;
    private WifiConfiguration hasInetNetworkCandidate;
    private ScanResult hasInetScanResultCandidate;
    private Context mContext;
    private int mSelfCureCandidateLostCnt;
    private WifiConfigManager mWifiConfigManager;
    private WifiNative mWifiNative;
    private WifiStateMachine mWifiStateMachine;
    private WifiConfiguration noInetNetworkCandidate;
    private ScanResult noInetScanResultCandidate;
    private boolean portalDisappeared;
    private int portalDisappearedCounter;
    private WifiConfiguration portalNetworkCandidate;
    private WifiConfiguration portalNetworkTursted;
    private AtomicBoolean portalNotificationShown;
    private String portalNotifiedSsid;
    private ScanResult portalScanResultCandidate;
    private ScanResult portalScanResultTursted;
    private WifiConfiguration recoveryNetworkCandidate;
    private ScanResult recoveryScanResultCandidate;
    private WifiConfiguration selfCureNetworkCandidate;
    private ScanResult selfCureScanResultCandidate;

    static {
        BACKUP_UNUSED = HANDOVER_STATUS_OK;
        HAS_INET_SELECTED = 100;
        RECOVERY_SELECTED = MessageUtil.CMD_ON_STOP;
        TRUSTED_PORTAL_SELECTED = MessageUtil.CMD_START_SCAN;
        NORMAL_PORTAL_SELECTED = HwDualBandMessageUtil.CMD_STOP_MONITOR;
        NO_INET_SELECTED = HwDualBandMessageUtil.CMD_UPDATE_AP_INFO;
        MIN_MACHINE_BOOT_MS = 45000;
        PORTAL_DISAPPEAR_THRESHOLD = 2;
        CONSECUTIVE_GOOD_RSSI_COUNTER = 3;
    }

    public HwWifiQualifiedNetworkSelector(WifiConfigManager configureStore, Context context, WifiInfo wifiInfo, Clock clock, WifiStateMachine wsm, WifiNative wifiNative) {
        super(configureStore, context, wifiInfo, clock);
        this.portalNotificationShown = new AtomicBoolean(false);
        this.hasInetNetworkCandidate = null;
        this.hasInetScanResultCandidate = null;
        this.recoveryNetworkCandidate = null;
        this.recoveryScanResultCandidate = null;
        this.portalNetworkTursted = null;
        this.portalScanResultTursted = null;
        this.portalNetworkCandidate = null;
        this.portalScanResultCandidate = null;
        this.noInetNetworkCandidate = null;
        this.noInetScanResultCandidate = null;
        this.backupTypeSelected = BACKUP_UNUSED;
        this.selfCureNetworkCandidate = null;
        this.selfCureScanResultCandidate = null;
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiNative = wifiNative;
        this.mWifiConfigManager = configureStore;
        this.portalNotificationShown.set(false);
        this.portalNotifiedSsid = null;
        this.portalDisappeared = true;
        this.mSelfCureCandidateLostCnt = HANDOVER_STATUS_OK;
    }

    private boolean underThresholdByWifiPro(WifiConfiguration config) {
        return false;
    }

    public synchronized boolean isWifiProEnabled() {
        return WifiHandover.isWifiProEnabled();
    }

    public synchronized void resetConnectConfig() {
        this.hasInetNetworkCandidate = null;
        this.hasInetScanResultCandidate = null;
        this.recoveryNetworkCandidate = null;
        this.recoveryScanResultCandidate = null;
        this.portalNetworkTursted = null;
        this.portalScanResultTursted = null;
        this.portalNetworkCandidate = null;
        this.portalScanResultCandidate = null;
        this.noInetNetworkCandidate = null;
        this.noInetScanResultCandidate = null;
        this.selfCureNetworkCandidate = null;
        this.selfCureScanResultCandidate = null;
        this.backupTypeSelected = BACKUP_UNUSED;
        this.portalDisappeared = true;
    }

    public synchronized void portalNotifyChanged(boolean on, String ssid) {
        this.portalNotificationShown.set(on);
        this.portalNotifiedSsid = ssid;
        this.portalDisappearedCounter = HANDOVER_STATUS_OK;
    }

    private boolean rssiStronger(ScanResult newObj, ScanResult oldObj) {
        if (newObj == null || oldObj == null || newObj.level <= oldObj.level) {
            return false;
        }
        return true;
    }

    public synchronized boolean selectBestNetworkByWifiPro(WifiConfiguration config, ScanResult scanResult) {
        if (this.mWifiStateMachine.isWifiSelfCuring() && config.networkId == this.mWifiStateMachine.getSelfCureNetworkId()) {
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
            return true;
        }
        if (isWifiProEnabled() && this.mWifiStateMachine.getCurrentNetwork() == null && config != null && scanResult != null) {
            LOGD("selectBestNetworkByWifiPro, current = " + config.configKey(true) + ", internetHistory = " + config.internetHistory + ", level = " + scanResult.level + ", 5GHz = " + scanResult.is5GHz());
            if (networkIgnoredByWifiPro(config) || networkIgnoredByStatus(config) || selectNetworkHasInternet(config, scanResult) || selectNetworkHasInternetEver(config, scanResult) || selectNetworkPortal(config, scanResult) || selectNetworkNoInternet(config, scanResult)) {
                return true;
            }
        }
        return false;
    }

    public boolean networkIgnoredByWifiPro(WifiConfiguration config) {
        if (config == null || !config.isTempCreated) {
            return false;
        }
        LOGD(config.SSID + ", networkIgnoredByAPScore, skip candidate due to istempcreated");
        return true;
    }

    public boolean networkIgnoredByStatus(WifiConfiguration config) {
        if (underThresholdByWifiPro(config)) {
            config.poorRssiDectected = true;
            config.consecutiveGoodRssiCounter = HANDOVER_STATUS_OK;
            LOGD("networkIgnoredByStatus, AutoJoin Connection, poorRssiDectected, ignore candidate = " + config.configKey(true));
            return true;
        } else if (config.poorRssiDectected) {
            config.consecutiveGoodRssiCounter++;
            if (config.consecutiveGoodRssiCounter < CONSECUTIVE_GOOD_RSSI_COUNTER && !WifiProCommonUtils.isQueryActivityMatched(this.mContext, "com.android.settings.Settings$WifiSettingsActivity")) {
                return true;
            }
            config.poorRssiDectected = false;
            config.consecutiveGoodRssiCounter = HANDOVER_STATUS_OK;
            return false;
        } else {
            config.poorRssiDectected = false;
            config.consecutiveGoodRssiCounter = HANDOVER_STATUS_OK;
            return false;
        }
    }

    private boolean selectNetworkHasInternet(WifiConfiguration config, ScanResult scanResult) {
        if (!hasInternet(config) && !maybeHasInternet(config) && ((!config.portalNetwork || config.portalCheckStatus != 1) && (!config.noInternetAccess || config.internetRecoveryStatus != 5))) {
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
            } else if ((ScanResult.is5GHz(this.hasInetScanResultCandidate.frequency) && ScanResult.is5GHz(scanResult.frequency)) || (ScanResult.is24GHz(this.hasInetScanResultCandidate.frequency) && ScanResult.is24GHz(scanResult.frequency))) {
                if (scanResult.level > this.hasInetScanResultCandidate.level) {
                    this.hasInetNetworkCandidate = config;
                    this.hasInetScanResultCandidate = scanResult;
                    this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                }
            } else if (ScanResult.is5GHz(this.hasInetScanResultCandidate.frequency) && ScanResult.is24GHz(scanResult.frequency)) {
                if (this.hasInetScanResultCandidate.level < MIN_3_LEVEL && scanResult.level > this.hasInetScanResultCandidate.level) {
                    this.hasInetNetworkCandidate = config;
                    this.hasInetScanResultCandidate = scanResult;
                    this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                }
            } else if (ScanResult.is24GHz(this.hasInetScanResultCandidate.frequency) && ScanResult.is5GHz(scanResult.frequency) && (scanResult.level > this.hasInetScanResultCandidate.level || scanResult.level >= MIN_3_LEVEL)) {
                this.hasInetNetworkCandidate = config;
                this.hasInetScanResultCandidate = scanResult;
                this.hasInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
            }
        }
        return true;
    }

    private boolean selectNetworkHasInternetEver(WifiConfiguration config, ScanResult scanResult) {
        boolean z = false;
        if (!config.noInternetAccess || !NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        }
        StringBuilder append = new StringBuilder().append("selectNetworkHasInternetEver, recovery matched, candidate = ").append(config.configKey(true)).append(", recoveryNetworkCandidate is null = ");
        if (this.recoveryNetworkCandidate == null) {
            z = true;
        }
        LOGD(append.append(z).toString());
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
        if (!WifiProCommonUtils.isOpenAndPortal(config) && !WifiProCommonUtils.isOpenAndMaybePortal(config)) {
            return false;
        }
        if (this.portalNotificationShown.get() && this.portalNotifiedSsid != null && this.portalNotifiedSsid.equals(config.SSID)) {
            this.portalDisappeared = false;
            this.portalDisappearedCounter = HANDOVER_STATUS_OK;
        }
        if (WifiProCommonUtils.isInMonitorList(config.SSID, WifiProCommonUtils.TRUSTED_PORTAL_LIST)) {
            LOGD("selectNetworkPortal, portal is in trusted list, backup it, candidate = " + config.configKey(true));
            this.portalNetworkTursted = config;
            this.portalScanResultTursted = scanResult;
            this.portalNetworkTursted.getNetworkSelectionStatus().setCandidate(scanResult);
            return true;
        } else if (this.portalNetworkTursted != null) {
            LOGD("selectNetworkPortal, skip the normal portal since the trusted portal selected, candidate = " + config.configKey(true));
            return true;
        } else if (scanResult == null || scanResult.level >= -82) {
            if (config.internetRecoveryCheckTimestamp > 0 && config.portalCheckStatus == 2 && !this.portalNotificationShown.get() && System.currentTimeMillis() - config.internetRecoveryCheckTimestamp > 28800000) {
                LOGD("selectNetworkPortal, reset portal status tobe unknown after 8h from last checking!");
                config.portalCheckStatus = HANDOVER_STATUS_OK;
            }
            if (config.portalCheckStatus == 0 || isUserOnWlanSettings() || ((isMobileDataInactive() && isMachineBootedReady()) || getWifiProAutoConnectFlag())) {
                if (this.portalNetworkCandidate == null) {
                    LOGD("selectNetworkPortal, portal status unknown, backup it if no other choice, candidate = " + config.configKey(true));
                    this.portalNetworkCandidate = config;
                    this.portalScanResultCandidate = scanResult;
                    this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                } else if (config.lastHasInternetTimestamp == 0 && this.portalNetworkCandidate.lastHasInternetTimestamp == 0) {
                    if (rssiStronger(scanResult, this.portalScanResultCandidate)) {
                        LOGD("selectNetworkPortal, use the stronger rssi for portal unauthen candidate, new candidate = " + config.configKey(true));
                        this.portalNetworkCandidate = config;
                        this.portalScanResultCandidate = scanResult;
                        this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                    }
                } else if (config.lastHasInternetTimestamp > this.portalNetworkCandidate.lastHasInternetTimestamp) {
                    LOGD("selectNetworkPortal, use the portal network that login recently, new candidate = " + config.configKey(true));
                    this.portalNetworkCandidate = config;
                    this.portalScanResultCandidate = scanResult;
                    this.portalNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                }
            } else if (config.portalCheckStatus == 2) {
                LOGD("selectNetworkPortal, ignore to select this portal because of unthen detected last time for background.");
            }
            this.mWifiNative.disableNetwork(config.networkId);
            return true;
        } else {
            LOGD("selectNetworkPortal, don't select it this time due to poor rssi, candidate = " + config.configKey(true) + ", level = " + scanResult.level);
            this.mWifiNative.disableNetwork(config.networkId);
            return true;
        }
    }

    private boolean selectNetworkNoInternet(WifiConfiguration config, ScanResult scanResult) {
        if (!WifiProStatusUtils.isWifiProEnabledViaXml(this.mContext)) {
            LOGD("selectNetworkNoInternet, return false");
            return false;
        } else if (!config.noInternetAccess || NetworkHistoryUtils.allowWifiConfigRecovery(config.internetHistory)) {
            return false;
        } else {
            if (this.recoveryNetworkCandidate == null && this.portalNetworkTursted == null && this.portalNetworkCandidate == null) {
                if (config.internetRecoveryCheckTimestamp > 0 && config.internetRecoveryStatus == 4 && System.currentTimeMillis() - config.internetRecoveryCheckTimestamp > 3600000) {
                    LOGD("selectNetworkNoInternet, recovery unmatched, reset tobe unknown after 1h from last checking!");
                    config.internetRecoveryStatus = 3;
                }
                if (!WifiProCommonUtils.allowRecheckForNoInternet(config, scanResult, this.mContext)) {
                    LOGD("selectNetworkNoInternet, unallow to select candidate that has no internet, status = " + config.internetRecoveryStatus);
                } else if (this.noInetNetworkCandidate == null) {
                    LOGD("selectNetworkNoInternet, no internet network = " + config.configKey(true) + ", backup it if no other better one.");
                    this.noInetNetworkCandidate = config;
                    this.noInetScanResultCandidate = scanResult;
                    this.noInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                } else if (rssiStronger(scanResult, this.noInetScanResultCandidate)) {
                    LOGD("selectNetworkNoInternet, use the stronger rssi for no internet candidate, new candidate = " + config.configKey(true));
                    this.noInetNetworkCandidate = config;
                    this.noInetScanResultCandidate = scanResult;
                    this.noInetNetworkCandidate.getNetworkSelectionStatus().setCandidate(scanResult);
                }
                this.mWifiNative.disableNetwork(config.networkId);
                return true;
            }
            LOGD("selectNetworkNoInternet, better network selected, skip due to no internet, candidate = " + config.configKey(true));
            this.mWifiNative.disableNetwork(config.networkId);
            return true;
        }
    }

    public synchronized WifiConfiguration candidateUpdatedByWifiPro(WifiConfiguration config) {
        if (this.mWifiStateMachine.isWifiSelfCuring() && this.selfCureNetworkCandidate != null) {
            LOGD("Within Wifi Self Curing, the highest network is " + this.selfCureNetworkCandidate.configKey(true));
            this.mSelfCureCandidateLostCnt = HANDOVER_STATUS_OK;
            return this.selfCureNetworkCandidate;
        } else if (this.mWifiStateMachine.isWifiSelfCuring() && this.selfCureNetworkCandidate == null) {
            LOGD("candidateUpdatedByWifiPro, Within Wifi Self Curing and AP lost at this scan, skip connect others.");
            this.mSelfCureCandidateLostCnt++;
            if (this.mSelfCureCandidateLostCnt == 2) {
                LOGD("candidateUpdatedByWifiPro, stop self cure because AP lost 2 times");
                this.mWifiStateMachine.notifySelfCureNetworkLost();
            }
            return null;
        } else {
            WifiConfiguration newConfig = config;
            if (isWifiProEnabled()) {
                this.mWifiStateMachine.setWifiBackgroundReason(5);
                if (this.hasInetNetworkCandidate != null) {
                    LOGD("WLAN+ Enabled, use the best candidate has internet always or unknown, network = " + this.hasInetNetworkCandidate.configKey(true));
                    newConfig = this.hasInetNetworkCandidate;
                    this.backupTypeSelected = HAS_INET_SELECTED;
                } else if (this.recoveryNetworkCandidate != null) {
                    LOGD("WLAN+ Enabled, use the recovery matched candidate(has internet ever), network = " + this.recoveryNetworkCandidate.configKey(true));
                    newConfig = this.recoveryNetworkCandidate;
                    this.backupTypeSelected = RECOVERY_SELECTED;
                } else if (this.portalNetworkTursted != null) {
                    LOGD("WLAN+ Enabled, use the whitelist portal because of no network has internet found, network = " + this.portalNetworkTursted.configKey(true));
                    newConfig = this.portalNetworkTursted;
                    this.backupTypeSelected = TRUSTED_PORTAL_SELECTED;
                } else if (this.portalNetworkCandidate != null) {
                    LOGD("WLAN+ Enabled, prepare to use portal, isUserOnWlanSettings = " + isUserOnWlanSettings());
                    LOGD("WLAN+ Enabled, prepare to use portal, isMobileDataInactive = " + isMobileDataInactive());
                    LOGD("WLAN+ Enabled, prepare to use portal, getWifiProAutoConnectFlag = " + getWifiProAutoConnectFlag());
                    if (isUserOnWlanSettings() || isMobileDataInactive() || getWifiProAutoConnectFlag()) {
                        LOGD("WLAN+ Enabled, use the best portal to connect foreground, network = " + this.portalNetworkCandidate.configKey(true));
                        newConfig = this.portalNetworkCandidate;
                        this.backupTypeSelected = NORMAL_PORTAL_SELECTED;
                    } else if (!this.portalNotificationShown.get() && isMachineBootedReady()) {
                        LOGD("WLAN+ Enabled, use the best portal to connect background, network = " + this.portalNetworkCandidate.configKey(true));
                        newConfig = this.portalNetworkCandidate;
                        this.backupTypeSelected = NORMAL_PORTAL_SELECTED;
                        this.mWifiStateMachine.setWifiBackgroundReason(newConfig.portalCheckStatus);
                    }
                } else if (this.noInetNetworkCandidate != null) {
                    if (!WifiProStatusUtils.isWifiProEnabledViaXml(this.mContext)) {
                        LOGD("WLAN+ disable, select no internet AP");
                        newConfig = this.noInetNetworkCandidate;
                        this.backupTypeSelected = NO_INET_SELECTED;
                    } else if (!(isUserOnWlanSettings() || this.mWifiStateMachine.isHiLinkActive())) {
                        LOGD("WLAN+ Enabled, use no internet to recheck internet capacity at background, network = " + this.noInetNetworkCandidate.configKey(true));
                        newConfig = this.noInetNetworkCandidate;
                        this.backupTypeSelected = NO_INET_SELECTED;
                        this.mWifiStateMachine.setWifiBackgroundReason(newConfig.internetRecoveryStatus);
                    }
                }
            }
            return newConfig;
        }
    }

    public synchronized ScanResult scanResultUpdatedByWifiPro(WifiConfiguration networkCandidate, ScanResult scanResult) {
        if (this.mWifiStateMachine.isWifiSelfCuring() && this.selfCureScanResultCandidate != null) {
            return this.selfCureScanResultCandidate;
        } else if (this.mWifiStateMachine.isWifiSelfCuring() && this.selfCureScanResultCandidate == null) {
            LOGD("scanResultUpdatedByWifiPro, Within Wifi Self Curing and AP lost at this scan, skip connect others.");
            return null;
        } else {
            if (isWifiProEnabled() && networkCandidate != null) {
                LOGD("WLAN+ Enabled, update scan result, selected type = " + this.backupTypeSelected);
                if (this.backupTypeSelected == HAS_INET_SELECTED) {
                    return this.hasInetScanResultCandidate;
                } else if (this.backupTypeSelected == RECOVERY_SELECTED) {
                    return this.recoveryScanResultCandidate;
                } else if (this.backupTypeSelected == TRUSTED_PORTAL_SELECTED) {
                    return this.portalScanResultTursted;
                } else if (this.backupTypeSelected == NORMAL_PORTAL_SELECTED) {
                    return this.portalScanResultCandidate;
                } else if (this.backupTypeSelected == NO_INET_SELECTED) {
                    return this.noInetScanResultCandidate;
                }
            }
            return scanResult;
        }
    }

    public synchronized void handleAutoJoinCompleted(WifiConfiguration candidate) {
        if (isWifiProEnabled() && this.mWifiStateMachine.getCurrentNetwork() == null) {
            if ((candidate == null ? HANDOVER_STATUS_DISALLOWED : HANDOVER_STATUS_OK) == HANDOVER_STATUS_DISALLOWED && (getWifiProAutoConnectFlag() || (this.portalNotificationShown.get() && this.portalDisappeared))) {
                this.portalDisappearedCounter++;
                LOGD("handleAutoJoinCompleted, notified portal = " + this.portalNotifiedSsid + ", disappear counter = " + this.portalDisappearedCounter);
                if (this.portalDisappearedCounter >= PORTAL_DISAPPEAR_THRESHOLD) {
                    this.portalNotificationShown.set(false);
                    this.portalDisappearedCounter = HANDOVER_STATUS_OK;
                    Intent intent = new Intent();
                    intent.setAction(WifiproUtils.ACTION_NOTIFY_PORTAL_OUT_OF_RANGE);
                    intent.setFlags(67108864);
                    this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
            }
        }
    }

    public synchronized void resetSelfCureCandidateLostCnt() {
        this.mSelfCureCandidateLostCnt = HANDOVER_STATUS_OK;
    }

    private boolean hasInternet(WifiConfiguration config) {
        if (config.noInternetAccess || WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, MessageUtil.CMD_START_SCAN)) {
            return false;
        }
        return WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, 100);
    }

    private boolean maybeHasInternet(WifiConfiguration config) {
        if (WifiProCommonUtils.isOpenType(config) || config.noInternetAccess || config.portalNetwork) {
            return false;
        }
        return WifiProCommonUtils.matchedRequestByHistory(config.internetHistory, HwDualBandMessageUtil.CMD_STOP_MONITOR);
    }

    private boolean isUserOnWlanSettings() {
        return WifiProCommonUtils.isQueryActivityMatched(this.mContext, "com.android.settings.Settings$WifiSettingsActivity");
    }

    private boolean isMobileDataInactive() {
        return !WifiProCommonUtils.isMobileDataOff(this.mContext) ? WifiProCommonUtils.isNoSIMCard(this.mContext) : true;
    }

    private boolean isMachineBootedReady() {
        return SystemClock.elapsedRealtime() >= ((long) MIN_MACHINE_BOOT_MS);
    }

    private boolean getWifiProAutoConnectFlag() {
        return this.mContext != null && Global.getInt(this.mContext.getContentResolver(), "wifi_pro_auto_connect_portal_flag", HANDOVER_STATUS_OK) == 1;
    }

    private void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
