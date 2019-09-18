package com.android.server.wifi;

import android.app.AlarmManager;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.StringUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class WifiConnectivityManager extends AbsWifiConnectivityManager {
    @VisibleForTesting
    public static final int BSSID_BLACKLIST_EXPIRE_TIME_MS = 300000;
    @VisibleForTesting
    public static final int BSSID_BLACKLIST_THRESHOLD = 3;
    private static final int CHANNEL_LIST_AGE_MS = 3600000;
    private static final int CONNECTED_PNO_SCAN_INTERVAL_MS = 160000;
    private static final int DISCONNECTED_PNO_SCAN_INTERVAL_MS = 20000;
    public static final String HOUR_PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Hour Periodic Scan Timer";
    private static final int HOUR_PERIOD_SCAN_INTERVAL_MS = 3600000;
    public static final int HW_MIN_PERIODIC_SCAN_INTERVAL_MS = 10000;
    private static final int LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS = 80000;
    private static final int LOW_RSSI_NETWORK_RETRY_START_DELAY_MS = 20000;
    public static final int MAX_CONNECTION_ATTEMPTS_RATE = 6;
    public static final int MAX_CONNECTION_ATTEMPTS_TIME_INTERVAL_MS = 240000;
    @VisibleForTesting
    public static final int MAX_PERIODIC_SCAN_INTERVAL_MS = 160000;
    public static final int MAX_PNO_SCAN_RESTART_ALLOWED = 2;
    @VisibleForTesting
    public static final int MAX_SCAN_RESTART_ALLOWED = 5;
    private static final int PASSPOINT_NETWORK_EVALUATOR_PRIORITY = 2;
    @VisibleForTesting
    public static final int PERIODIC_SCAN_INTERVAL_MS = 20000;
    public static final String PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Periodic Scan Timer";
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
    protected static final int PRECONFIGURED_NETWORK_EVALUATOR_PRIORITY = 4;
    private static final boolean R1 = (SystemProperties.get("ro.config.hw_opta", "0").equals("389") && SystemProperties.get("ro.config.hw_optb", "0").equals("840"));
    @VisibleForTesting
    public static final int REASON_CODE_AP_UNABLE_TO_HANDLE_NEW_STA = 17;
    private static final long RESET_TIME_STAMP = Long.MIN_VALUE;
    public static final String RESTART_CONNECTIVITY_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Scan";
    private static final int RESTART_SCAN_DELAY_MS = 2000;
    public static final String RESTART_SINGLE_SCAN_TIMER_TAG = "WifiConnectivityManager Restart Single Scan";
    private static final int SAVED_NETWORK_EVALUATOR_PRIORITY = 1;
    public static final int SCAN_COUNT_CHANGE_REASON_ADD = 0;
    public static final int SCAN_COUNT_CHANGE_REASON_MINUS = 1;
    public static final int SCAN_COUNT_CHANGE_REASON_RESET = 2;
    private static final boolean SCAN_IMMEDIATELY = true;
    private static final boolean SCAN_ON_SCHEDULE = false;
    private static final int SCORED_NETWORK_EVALUATOR_PRIORITY = 3;
    private static final int SWITCH_TO_WIFI_AUTO = 0;
    private static final String TAG = "WifiConnectivityManager";
    private static final int VALID_ROAM_BLACK_LIST_MIN_SSID_NUM = 2;
    private static final String VALUE_DISABLE = "value_disable";
    private static final int WATCHDOG_INTERVAL_MS = 1680000;
    public static final String WATCHDOG_TIMER_TAG = "WifiConnectivityManager Schedule Watchdog Timer";
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    public static final int WIFI_STATE_TRANSITIONING = 3;
    public static final int WIFI_STATE_UNKNOWN = 0;
    /* access modifiers changed from: private */
    public final AlarmManager mAlarmManager;
    private final AllSingleScanListener mAllSingleScanListener = new AllSingleScanListener();
    private int mBand5GHzBonus;
    private Map<String, BssidBlacklistStatus> mBssidBlacklist = new HashMap();
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final CarrierNetworkNotifier mCarrierNetworkNotifier;
    /* access modifiers changed from: private */
    public final Clock mClock;
    private final WifiConfigManager mConfigManager;
    private final LinkedList<Long> mConnectionAttemptTimeStamps;
    /* access modifiers changed from: private */
    public final WifiConnectivityHelper mConnectivityHelper;
    private Context mContext;
    private int mCurrentConnectionBonus;
    /* access modifiers changed from: private */
    public boolean mDbg = false;
    private boolean mEnableAutoJoinWhenAssociated;
    /* access modifiers changed from: private */
    public final Handler mEventHandler;
    private int mFullScanMaxRxRate;
    private int mFullScanMaxTxRate;
    private final AlarmManager.OnAlarmListener mHourPeriodicScanTimerListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.startHourPeriodicSingleScan();
        }
    };
    private boolean mHourPeriodicScanTimerSet = false;
    private HwWifiCHRService mHwWifiCHRService;
    private int mInitialScoreMax;
    private String mLastConnectionAttemptBssid = null;
    private long mLastHourPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private long mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private final LocalLog mLocalLog;
    private int mMin24GHzRssi;
    private int mMin5GHzRssi;
    protected final WifiNetworkSelector mNetworkSelector;
    /* access modifiers changed from: private */
    public final Set<String> mOldSsidList = new HashSet();
    private final OpenNetworkNotifier mOpenNetworkNotifier;
    private final AlarmManager.OnAlarmListener mPeriodicScanTimerListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.periodicScanTimerHandler();
        }
    };
    private boolean mPeriodicScanTimerSet = false;
    private int mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
    private final PnoScanListener mPnoScanListener = new PnoScanListener();
    /* access modifiers changed from: private */
    public int mPnoScanRestartCount = 0;
    /* access modifiers changed from: private */
    public boolean mPnoScanStarted = false;
    /* access modifiers changed from: private */
    public final AlarmManager.OnAlarmListener mRestartScanListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.startConnectivityScan(true, true);
        }
    };
    private int mSameNetworkBonus;
    /* access modifiers changed from: private */
    public int mScanRestartCount = 0;
    /* access modifiers changed from: private */
    public final WifiScanner mScanner;
    private final ScoringParams mScoringParams;
    /* access modifiers changed from: private */
    public boolean mScreenOn = false;
    private int mSecureBonus;
    /* access modifiers changed from: private */
    public int mSingleScanRestartCount = 0;
    /* access modifiers changed from: private */
    public boolean mSingleScanStarted = false;
    /* access modifiers changed from: private */
    public final WifiStateMachine mStateMachine;
    private int mTotalConnectivityAttemptsRateLimited = 0;
    private boolean mUntrustedConnectionAllowed = false;
    /* access modifiers changed from: private */
    public boolean mUseSingleRadioChainScanResults = false;
    /* access modifiers changed from: private */
    public boolean mWaitForFullBandScanResults = false;
    private final AlarmManager.OnAlarmListener mWatchdogListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.watchdogHandler();
        }
    };
    /* access modifiers changed from: private */
    public boolean mWifiConnectivityManagerEnabled = true;
    /* access modifiers changed from: private */
    public boolean mWifiEnabled = false;
    private final WifiInfo mWifiInfo;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    /* access modifiers changed from: private */
    public final WifiMetrics mWifiMetrics;
    private WifiNetworkNotifier mWifiNetworkNotifier = null;
    protected int mWifiState = 0;

    private class AllSingleScanListener implements WifiScanner.ScanListener {
        private boolean mNeedLog;
        private int mNumScanResultsIgnoredDueToSingleRadioChain;
        private List<ScanDetail> mScanDetails;

        private AllSingleScanListener() {
            this.mScanDetails = new ArrayList();
            this.mNeedLog = true;
            this.mNumScanResultsIgnoredDueToSingleRadioChain = 0;
        }

        public void clearScanDetails() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "29", "AllScan clearScanDetails.");
            this.mScanDetails.clear();
            this.mNumScanResultsIgnoredDueToSingleRadioChain = 0;
        }

        public void onSuccess() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "30", "AllScan registerScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "31", "AllScan registerScanListener onFailure: reason:%s description:%s", Integer.valueOf(reason), description);
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "32", "AllScan onPeriodChanged periodInMs:%s", Integer.valueOf(periodInMs));
        }

        public void onResults(WifiScanner.ScanData[] results) {
            if (results.length > 0 && results[0].isHiddenScanResult()) {
                Log.d(WifiConnectivityManager.TAG, "HiddenScanResult allSingleScanlister retrun");
            } else if (!WifiConnectivityManager.this.mWifiEnabled || !WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys, "33", "AllScan onResults returned mWifiEnabled:%s, mWifiConnectivityManagerEnabled:%s", Boolean.valueOf(WifiConnectivityManager.this.mWifiEnabled), Boolean.valueOf(WifiConnectivityManager.this.mWifiConnectivityManagerEnabled));
                clearScanDetails();
                boolean unused = WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
            } else {
                if (WifiConnectivityManager.this.mWaitForFullBandScanResults) {
                    if (!results[0].isAllChannelsScanned()) {
                        WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys, "34", "AllScan waiting for full band scan results.");
                        clearScanDetails();
                        return;
                    }
                    boolean unused2 = WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
                }
                if (results.length > 0) {
                    WifiConnectivityManager.this.mWifiMetrics.incrementAvailableNetworksHistograms(this.mScanDetails, results[0].isAllChannelsScanned());
                }
                if (this.mNumScanResultsIgnoredDueToSingleRadioChain > 0) {
                    Log.i(WifiConnectivityManager.TAG, "Number of scan results ignored due to single radio chain scan: " + this.mNumScanResultsIgnoredDueToSingleRadioChain);
                }
                boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "AllSingleScanListener", WifiConnectivityManager.this.mScanner.mCurrentScanKeys);
                String result = ScanResultUtil.getScanResultLogs(WifiConnectivityManager.this.mOldSsidList, this.mScanDetails);
                WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys, "35", "AllScan wasConnectAttempted:%s, results:%s", Boolean.valueOf(wasConnectAttempted), result);
                clearScanDetails();
                if (WifiConnectivityManager.this.mPnoScanStarted) {
                    if (wasConnectAttempted) {
                        WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoBad();
                    } else {
                        WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoGood();
                        WifiConnectivityManager.this.startConnectivityScan(true, true);
                    }
                }
                if (WifiConnectivityManager.this.mScreenOn && WifiConnectivityManager.this.mWifiState != 1 && !wasConnectAttempted && WifiConnectivityManager.this.isWifiScanSpecialChannels() && WifiConnectivityManager.this.mSingleScanStarted) {
                    Log.w(WifiConnectivityManager.TAG, "*******wifi scan special channels, but no connect ap ,  force fullband scan ****");
                    WifiConnectivityManager.this.handleScanCountChanged(0);
                    WifiConnectivityManager.this.startSingleScan(true, WifiStateMachine.WIFI_WORK_SOURCE);
                    boolean unused3 = WifiConnectivityManager.this.mSingleScanStarted = false;
                }
                int unused4 = WifiConnectivityManager.this.mScanRestartCount = 0;
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (!WifiConnectivityManager.this.mWifiEnabled || !WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                if (this.mNeedLog) {
                    WifiConnectivityManager.this.localLog("Key#00:", "36", "AllScan onFullResult returned mWifiEnabled:%s, mWifiConnectivityManagerEnabled:%s", Boolean.valueOf(WifiConnectivityManager.this.mWifiEnabled), Boolean.valueOf(WifiConnectivityManager.this.mWifiConnectivityManagerEnabled));
                }
                this.mNeedLog = false;
                return;
            }
            this.mNeedLog = true;
            if (WifiConnectivityManager.this.mDbg) {
                WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys + "AllSingleScanListener onFullResult: " + fullScanResult.SSID + " capabilities " + fullScanResult.capabilities);
            }
            if (WifiConnectivityManager.this.mUseSingleRadioChainScanResults || fullScanResult.radioChainInfos == null || fullScanResult.radioChainInfos.length != 1) {
                this.mScanDetails.add(ScanResultUtil.toScanDetail(fullScanResult));
                int unused = WifiConnectivityManager.this.mScanRestartCount = 0;
                return;
            }
            this.mNumScanResultsIgnoredDueToSingleRadioChain++;
        }
    }

    private static class BssidBlacklistStatus {
        public long blacklistedTimeStamp;
        public int counter;
        public boolean isBlacklisted;

        private BssidBlacklistStatus() {
            this.blacklistedTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
        }
    }

    private class OnSavedNetworkUpdateListener implements WifiConfigManager.OnSavedNetworkUpdateListener {
        private OnSavedNetworkUpdateListener() {
        }

        public void onSavedNetworkAdded(int networkId) {
            updatePnoScan();
        }

        public void onSavedNetworkEnabled(int networkId) {
            updatePnoScan();
        }

        public void onSavedNetworkRemoved(int networkId) {
            updatePnoScan();
        }

        public void onSavedNetworkUpdated(int networkId) {
            WifiConnectivityManager.this.mStateMachine.updateCapabilities();
            updatePnoScan();
        }

        public void onSavedNetworkTemporarilyDisabled(int networkId, int disableReason) {
            if (disableReason != 6) {
                WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
            }
        }

        public void onSavedNetworkPermanentlyDisabled(int networkId, int disableReason) {
            WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
            updatePnoScan();
        }

        private void updatePnoScan() {
            if (!WifiConnectivityManager.this.mScreenOn) {
                WifiConnectivityManager.this.localLog("Saved networks updated");
                WifiConnectivityManager.this.startConnectivityScan(false, false);
            }
        }
    }

    private class PnoScanListener implements WifiScanner.PnoScanListener {
        private int mLowRssiNetworkRetryDelay;
        private List<ScanDetail> mScanDetails;

        private PnoScanListener() {
            this.mScanDetails = new CopyOnWriteArrayList();
            this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.PERIODIC_SCAN_INTERVAL_MS;
        }

        public void clearScanDetails() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "39", "PnoScan clearScanDetails.");
            this.mScanDetails.clear();
        }

        public void resetLowRssiNetworkRetryDelay() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "40", "PnoScan resetLowRssiNetworkRetryDelay.");
            this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.PERIODIC_SCAN_INTERVAL_MS;
        }

        @VisibleForTesting
        public int getLowRssiNetworkRetryDelay() {
            return this.mLowRssiNetworkRetryDelay;
        }

        public void onSuccess() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "41", "PnoScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "42", "PnoScanListener onFailure: reason: %s, description:%s", Integer.valueOf(reason), description);
            if (WifiConnectivityManager.this.mScanRestartCount = WifiConnectivityManager.this.mScanRestartCount + 1 < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(WifiConnectivityManager.RESTART_SCAN_DELAY_MS);
                return;
            }
            int unused = WifiConnectivityManager.this.mScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start PNO scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "43", "PnoScanListener onPeriodChanged: actual scan period ms", Integer.valueOf(periodInMs));
        }

        public void onResults(WifiScanner.ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "44", "PnoScanListener: onPnoNetworkFound: results len = %s", Integer.valueOf(results.length));
            for (ScanResult result : results) {
                if (result.informationElements == null) {
                    WifiConnectivityManager.this.localLog("Skipping scan result with null information elements");
                } else {
                    this.mScanDetails.add(ScanResultUtil.toScanDetail(result));
                }
            }
            boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "PnoScanListener", WifiConnectivityManager.this.getScanKey(this));
            clearScanDetails();
            int unused = WifiConnectivityManager.this.mScanRestartCount = 0;
            if (!wasConnectAttempted) {
                if (this.mLowRssiNetworkRetryDelay > WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS) {
                    this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS;
                }
                if (WifiConnectivityManager.this.mPnoScanRestartCount = WifiConnectivityManager.this.mPnoScanRestartCount + 1 < 2) {
                    WifiConnectivityManager.this.mAlarmManager.set(3, ((long) this.mLowRssiNetworkRetryDelay) + WifiConnectivityManager.this.mClock.getElapsedSinceBootMillis(), WifiConnectivityManager.RESTART_CONNECTIVITY_SCAN_TIMER_TAG, WifiConnectivityManager.this.mRestartScanListener, WifiConnectivityManager.this.mEventHandler);
                }
                this.mLowRssiNetworkRetryDelay *= 2;
            } else {
                resetLowRssiNetworkRetryDelay();
            }
            int unused2 = WifiConnectivityManager.this.mScanRestartCount = 0;
        }
    }

    private class RestartSingleScanListener implements AlarmManager.OnAlarmListener {
        private final boolean mIsFullBandScan;

        RestartSingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onAlarm() {
            WifiConnectivityManager.this.startSingleScan(this.mIsFullBandScan, WifiStateMachine.WIFI_WORK_SOURCE);
        }
    }

    private class SingleScanListener implements WifiScanner.ScanListener {
        private final boolean mIsFullBandScan;

        SingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "37", "SingleScanListener onFailure: reason:%s description:%s SingleScanRestartCount:%s", Integer.valueOf(reason), description, Integer.valueOf(WifiConnectivityManager.this.mSingleScanRestartCount));
            if (WifiConnectivityManager.this.mSingleScanRestartCount = WifiConnectivityManager.this.mSingleScanRestartCount + 1 < 5) {
                WifiConnectivityManager.this.scheduleDelayedSingleScan(this.mIsFullBandScan);
                return;
            }
            int unused = WifiConnectivityManager.this.mSingleScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start single scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "38", "SingleScanListener onPeriodChanged: actual scan period %s ms", Integer.valueOf(periodInMs));
        }

        public void onResults(WifiScanner.ScanData[] results) {
            boolean unused = WifiConnectivityManager.this.mSingleScanStarted = true;
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (WifiConnectivityManager.this.mDbg) {
                WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
                wifiConnectivityManager.localLog("SingleScanListener onFullResult: " + fullScanResult.SSID + " capabilities " + fullScanResult.capabilities);
            }
            int unused = WifiConnectivityManager.this.mSingleScanRestartCount = 0;
        }
    }

    /* access modifiers changed from: private */
    public void localLog(String log) {
        this.mLocalLog.log(log);
    }

    /* access modifiers changed from: private */
    public boolean handleScanResults(List<ScanDetail> scanDetails, String listenerName, String keys) {
        refreshBssidBlacklist();
        if (Settings.System.getInt(this.mContext.getContentResolver(), "wifi_connect_type", 0) != 0) {
            return false;
        }
        if (this.mStateMachine.isSupplicantTransientState()) {
            Log.i("WifiScanLog", keys + listenerName + " onResults: No network selection because supplicantTransient is " + this.mStateMachine.isSupplicantTransientState());
            return false;
        }
        localLog(keys, "27", " onResults: start network selection");
        this.mNetworkSelector.mCurrentScanKeys = keys;
        WifiConfiguration candidate = this.mNetworkSelector.selectNetwork(scanDetails, buildBssidBlacklist(), this.mWifiInfo, this.mStateMachine.isConnected(), this.mStateMachine.isDisconnected(), this.mUntrustedConnectionAllowed);
        this.mWifiLastResortWatchdog.updateAvailableNetworks(this.mNetworkSelector.getConnectableScanDetails());
        this.mWifiMetrics.countScanResults(scanDetails);
        if (candidate != null) {
            ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
            if (!isWifiScanSpecialChannels() || scanResultCandidate == null || HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(scanResultCandidate.frequency, scanResultCandidate.level) > 2) {
                localLog(keys, "28", "WNS selectNetwork candidate-%s", candidate.SSID);
                connectToNetwork(candidate, keys);
                return true;
            }
            Log.d(TAG, "candidate = " + candidate.configKey() + ", don't connect to poor network because use specified-channels-scan, rssi = " + scanResultCandidate.level);
            return false;
        }
        if (this.mWifiState == 2) {
            if (R1) {
                this.mWifiNetworkNotifier.handleScanResults(this.mNetworkSelector.getFilteredScanDetailsForOpenUnsavedNetworks());
            } else {
                Log.d(TAG, "DO NOT notification for Open unsaved networks.");
            }
            if (this.mCarrierNetworkConfig.isCarrierEncryptionInfoAvailable()) {
                this.mCarrierNetworkNotifier.handleScanResults(this.mNetworkSelector.getFilteredScanDetailsForCarrierUnsavedNetworks(this.mCarrierNetworkConfig));
            }
        }
        return false;
    }

    WifiConnectivityManager(Context context, ScoringParams scoringParams, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean enable, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator) {
        Context context2 = context;
        WifiConnectivityHelper wifiConnectivityHelper = connectivityHelper;
        Looper looper2 = looper;
        boolean z = enable;
        this.mContext = context2;
        this.mStateMachine = stateMachine;
        this.mScanner = scanner;
        this.mConfigManager = configManager;
        this.mWifiInfo = wifiInfo;
        this.mNetworkSelector = networkSelector;
        this.mNetworkSelector.mConnectivityHelper = wifiConnectivityHelper;
        this.mConnectivityHelper = wifiConnectivityHelper;
        this.mLocalLog = localLog;
        this.mWifiLastResortWatchdog = wifiLastResortWatchdog;
        this.mOpenNetworkNotifier = openNetworkNotifier;
        this.mCarrierNetworkNotifier = carrierNetworkNotifier;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
        if (R1) {
            this.mWifiNetworkNotifier = new WifiNetworkNotifier(context2, looper2, frameworkFacade);
        } else {
            FrameworkFacade frameworkFacade2 = frameworkFacade;
        }
        this.mWifiMetrics = wifiMetrics;
        this.mAlarmManager = (AlarmManager) context2.getSystemService("alarm");
        this.mEventHandler = new Handler(looper2);
        this.mClock = clock;
        this.mScoringParams = scoringParams;
        this.mConnectionAttemptTimeStamps = new LinkedList<>();
        this.mMin5GHzRssi = this.mScoringParams.getEntryRssi(ScoringParams.BAND5);
        this.mMin24GHzRssi = this.mScoringParams.getEntryRssi(ScoringParams.BAND2);
        this.mBand5GHzBonus = context.getResources().getInteger(17694884);
        this.mCurrentConnectionBonus = context.getResources().getInteger(17694901);
        this.mSameNetworkBonus = context.getResources().getInteger(17694891);
        this.mSecureBonus = context.getResources().getInteger(17694892);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957081);
        this.mUseSingleRadioChainScanResults = context.getResources().getBoolean(17957083);
        this.mInitialScoreMax = (Math.max(this.mScoringParams.getGoodRssi(ScoringParams.BAND2), this.mScoringParams.getGoodRssi(ScoringParams.BAND5)) + context.getResources().getInteger(17694889)) * context.getResources().getInteger(17694890);
        this.mFullScanMaxTxRate = context.getResources().getInteger(17694904);
        this.mFullScanMaxRxRate = context.getResources().getInteger(17694903);
        localLog("PNO settings: min5GHzRssi " + this.mMin5GHzRssi + " min24GHzRssi " + this.mMin24GHzRssi + " currentConnectionBonus " + this.mCurrentConnectionBonus + " sameNetworkBonus " + this.mSameNetworkBonus + " secureNetworkBonus " + this.mSecureBonus + " initialScoreMax " + this.mInitialScoreMax);
        boolean hs2Enabled = context.getPackageManager().hasSystemFeature("android.hardware.wifi.passpoint");
        StringBuilder sb = new StringBuilder();
        sb.append("Passpoint is: ");
        sb.append(hs2Enabled ? "enabled" : "disabled");
        localLog(sb.toString());
        this.mNetworkSelector.registerNetworkEvaluator(savedNetworkEvaluator, 1);
        if (hs2Enabled) {
            boolean z2 = hs2Enabled;
            this.mNetworkSelector.registerNetworkEvaluator(passpointNetworkEvaluator, 2);
        } else {
            boolean z3 = hs2Enabled;
            PasspointNetworkEvaluator passpointNetworkEvaluator2 = passpointNetworkEvaluator;
        }
        this.mNetworkSelector.registerNetworkEvaluator(scoredNetworkEvaluator, 3);
        this.mScanner.registerScanListener(this.mAllSingleScanListener);
        this.mConfigManager.setOnSavedNetworkUpdateListener(new OnSavedNetworkUpdateListener());
        this.mWifiConnectivityManagerEnabled = z;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("ConnectivityScanManager initialized and ");
        sb2.append(z ? "enabled" : "disabled");
        localLog(sb2.toString());
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    private boolean shouldSkipConnectionAttempt(Long timeMillis) {
        Iterator<Long> attemptIter = this.mConnectionAttemptTimeStamps.iterator();
        while (attemptIter.hasNext() && timeMillis.longValue() - attemptIter.next().longValue() > 240000) {
            attemptIter.remove();
        }
        return this.mConnectionAttemptTimeStamps.size() >= 6;
    }

    private void noteConnectionAttempt(Long timeMillis) {
        this.mConnectionAttemptTimeStamps.addLast(timeMillis);
    }

    private void clearConnectionAttemptTimeStamps() {
        this.mConnectionAttemptTimeStamps.clear();
    }

    private void connectToNetwork(WifiConfiguration candidate, String keys) {
        String currentAssociationId;
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        if (scanResultCandidate == null) {
            Log.i("WifiScanLog", keys + "connectToNetwork: bad candidate - " + candidate + " scanResult: " + scanResultCandidate);
            return;
        }
        String targetBssid = scanResultCandidate.BSSID;
        String targetAssociationId = candidate.SSID + " : " + StringUtil.safeDisplayBssid(targetBssid);
        if (targetBssid != null && ((targetBssid.equals(this.mLastConnectionAttemptBssid) || targetBssid.equals(this.mWifiInfo.getBSSID())) && SupplicantState.isConnecting(this.mWifiInfo.getSupplicantState()))) {
            Log.i("WifiScanLog", keys + "connectToNetwork: Either already connected or is connecting to " + targetAssociationId);
        } else if (candidate.BSSID == null || candidate.BSSID.equals("any") || candidate.BSSID.equals(targetBssid)) {
            long elapsedTimeMillis = this.mClock.getElapsedSinceBootMillis();
            if (this.mScreenOn || !shouldSkipConnectionAttempt(Long.valueOf(elapsedTimeMillis))) {
                noteConnectionAttempt(Long.valueOf(elapsedTimeMillis));
                this.mLastConnectionAttemptBssid = targetBssid;
                WifiConfiguration currentConnectedNetwork = this.mConfigManager.getConfiguredNetwork(this.mWifiInfo.getNetworkId());
                if (currentConnectedNetwork == null) {
                    currentAssociationId = "Disconnected";
                } else {
                    currentAssociationId = this.mWifiInfo.getSSID() + " : " + StringUtil.safeDisplayBssid(this.mWifiInfo.getBSSID());
                }
                this.mStateMachine.setCHRConnectingSartTimestamp(elapsedTimeMillis);
                if (currentConnectedNetwork == null || currentConnectedNetwork.networkId != candidate.networkId) {
                    Bundle bundle = HwWifiServiceFactory.getHwDevicePolicyManager().getPolicy(null, POLICY_AUTO_CONNECT);
                    if (bundle == null || !bundle.getBoolean(VALUE_DISABLE)) {
                        if (candidate.BSSID == null || candidate.BSSID.equals("any")) {
                            targetBssid = "any";
                            localLog("connectToNetwork: Connect to " + StringUtil.safeDisplayBssid(candidate.SSID) + ":" + StringUtil.safeDisplayBssid(targetBssid) + " from " + currentAssociationId);
                        } else {
                            localLog("connectToNetwork: Connect to " + targetAssociationId + " from " + currentAssociationId);
                        }
                        if (this.mStateMachine.isScanAndManualConnectMode()) {
                            Log.d(TAG, "Only allow Manual Connection, ignore auto connection.");
                            return;
                        }
                        localLog(keys, "46", "WifiStateMachine startConnectToNetwork");
                        if (this.mHwWifiCHRService != null) {
                            this.mHwWifiCHRService.updateConnectType("AUTO_CONNECT");
                        }
                        this.mStateMachine.startConnectToNetwork(candidate.networkId, 1010, unselectDhcpFailedBssid(targetBssid, scanResultCandidate.BSSID, candidate));
                    } else {
                        Log.w(TAG, "connectToNetwork: MDM deny auto connect!");
                        return;
                    }
                } else if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                    Log.i("WifiScanLog", keys + "connectToNetwork: Roaming candidate - " + targetAssociationId + ". The actual roaming target is up to the firmware.");
                } else if (this.mStateMachine.isWifiRepeaterStarted()) {
                    Log.i("WifiScanLog", keys + "WifiRepeater is started, do not allow auto roam.");
                } else {
                    localLog(keys, "45", "connectToNetwork: Roaming to %s  from %s!", targetAssociationId, currentAssociationId);
                    this.mStateMachine.startRoamToNetwork(candidate.networkId, scanResultCandidate);
                }
                return;
            }
            Log.i("WifiScanLog", keys + "connectToNetwork: Too many connection attempts. Skipping this attempt!");
            this.mTotalConnectivityAttemptsRateLimited = this.mTotalConnectivityAttemptsRateLimited + 1;
        } else {
            Log.i("WifiScanLog", keys + "connecToNetwork: target BSSID " + StringUtil.safeDisplayBssid(targetBssid) + " does not match the config specified BSSID " + StringUtil.safeDisplayBssid(candidate.BSSID) + ". Drop it!");
        }
    }

    private int getScanBand() {
        return getScanBand(true);
    }

    private int getScanBand(boolean isFullBandScan) {
        if (isFullBandScan) {
            return 7;
        }
        return 0;
    }

    private boolean setScanChannels(WifiScanner.ScanSettings settings) {
        WifiConfiguration config = this.mStateMachine.getCurrentWifiConfiguration();
        if (config == null) {
            return false;
        }
        Set<Integer> freqs = this.mConfigManager.fetchChannelSetForNetworkForPartialScan(config.networkId, ANQPData.DATA_LIFETIME_MILLISECONDS, this.mWifiInfo.getFrequency());
        if (freqs == null || freqs.size() == 0) {
            localLog("No scan channels for " + config.configKey() + ". Perform full band scan");
            return false;
        }
        int index = 0;
        settings.channels = new WifiScanner.ChannelSpec[freqs.size()];
        for (Integer freq : freqs) {
            settings.channels[index] = new WifiScanner.ChannelSpec(freq.intValue());
            index++;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void watchdogHandler() {
        localLog("watchdogHandler");
        StringBuilder sb = new StringBuilder();
        sb.append("start a single scan from watchdogHandler ? ");
        sb.append(this.mWifiState == 2);
        Log.i("WifiScanLog", sb.toString());
        if (this.mWifiState == 2) {
            localLog("start a single scan from watchdogHandler");
            scheduleWatchdogTimer();
            startSingleScan(true, WifiStateMachine.WIFI_WORK_SOURCE);
        }
    }

    private void startPeriodicSingleScan() {
        long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
        localLog("****start Periodic SingleScan,mPeriodicSingleScanInterval : " + (this.mPeriodicSingleScanInterval / 1000) + " s");
        if (this.mLastPeriodicSingleScanTimeStamp != RESET_TIME_STAMP && !handleForceScan()) {
            long msSinceLastScan = currentTimeStamp - this.mLastPeriodicSingleScanTimeStamp;
            int mPeriodicScanInterval = PERIODIC_SCAN_INTERVAL_MS;
            if (isSupportWifiScanGenie()) {
                mPeriodicScanInterval = 10000;
            }
            if (msSinceLastScan < ((long) mPeriodicScanInterval)) {
                localLog("Last periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                schedulePeriodicScanTimer(mPeriodicScanInterval - ((int) msSinceLastScan));
                return;
            }
        }
        boolean isScanNeeded = true;
        boolean isFullBandScan = true;
        boolean isTrafficOverThreshold = this.mWifiInfo.txSuccessRate > ((double) this.mFullScanMaxTxRate) || this.mWifiInfo.rxSuccessRate > ((double) this.mFullScanMaxRxRate);
        if (this.mWifiState == 1 && isTrafficOverThreshold) {
            if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                localLog("No partial scan because firmware roaming is supported.");
                isScanNeeded = false;
            } else {
                localLog("No full band scan due to ongoing traffic");
                isFullBandScan = false;
            }
        }
        if (isScanNeeded && isScanThisPeriod(this.mStateMachine.isP2pConnected())) {
            this.mLastPeriodicSingleScanTimeStamp = currentTimeStamp;
            handleScanCountChanged(0);
            startSingleScan(isFullBandScan, WifiStateMachine.WIFI_WORK_SOURCE);
        }
        this.mPeriodicSingleScanInterval = getPeriodicSingleScanInterval();
        schedulePeriodicScanTimer(this.mPeriodicSingleScanInterval);
    }

    private void resetLastPeriodicSingleScanTimeStamp() {
        this.mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    }

    /* access modifiers changed from: private */
    public void periodicScanTimerHandler() {
        Log.i(TAG, "periodicScanTimerHandler mScreenOn " + this.mScreenOn);
        localLog("periodicScanTimerHandler");
        if (this.mScreenOn) {
            startPeriodicSingleScan();
        }
    }

    /* access modifiers changed from: private */
    public void startHourPeriodicSingleScan() {
        localLog("startHourPeriodicSingleScan: screenOn=" + this.mScreenOn + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mScreenOn && this.mWifiEnabled && !this.mWifiConnectivityManagerEnabled) {
            long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
            if (this.mLastHourPeriodicSingleScanTimeStamp != RESET_TIME_STAMP) {
                long msSinceLastScan = currentTimeStamp - this.mLastHourPeriodicSingleScanTimeStamp;
                if (msSinceLastScan < ANQPData.DATA_LIFETIME_MILLISECONDS) {
                    localLog("Last hour periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                    scheduleHourPeriodicScanTimer(3600000 - ((int) msSinceLastScan));
                    return;
                }
            }
            startScan(true, WifiStateMachine.WIFI_WORK_SOURCE);
            this.mLastHourPeriodicSingleScanTimeStamp = currentTimeStamp;
            scheduleHourPeriodicScanTimer(3600000);
        }
    }

    private void scheduleHourPeriodicScanTimer(int intervalMs) {
        AlarmManager.OnAlarmListener onAlarmListener = this.mHourPeriodicScanTimerListener;
        Handler handler = this.mEventHandler;
        this.mAlarmManager.setExact(3, ((long) intervalMs) + this.mClock.getElapsedSinceBootMillis(), HOUR_PERIODIC_SCAN_TIMER_TAG, onAlarmListener, handler);
        this.mHourPeriodicScanTimerSet = true;
    }

    private void stopHourPeriodicSingleScan() {
        if (this.mHourPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mHourPeriodicScanTimerListener);
            this.mHourPeriodicScanTimerSet = false;
        }
    }

    /* access modifiers changed from: private */
    public void startSingleScan(boolean isFullBandScan, WorkSource workSource) {
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            startScan(isFullBandScan, workSource);
        }
    }

    private void startScan(boolean isFullBandScan, WorkSource workSource) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        WifiScanner.ScanSettings settings = null;
        if (!isWifiScanSpecialChannels() || this.mWifiState == 1) {
            Log.e(TAG, "isWifiScanSpecialChannels is false,mWifiState : " + this.mWifiState);
        } else {
            settings = getScanGenieSettings();
            if (settings != null) {
                isFullBandScan = false;
            }
            localLog("****isWifiScanSpecialChannels *settings =**:" + settings);
        }
        if (settings == null) {
            settings = new WifiScanner.ScanSettings();
            if (!isFullBandScan && !setScanChannels(settings)) {
                isFullBandScan = true;
            }
        }
        settings.type = 2;
        settings.band = getScanBand(isFullBandScan);
        settings.reportEvents = 3;
        settings.numBssidsPerScan = 0;
        List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = this.mConfigManager.retrieveHiddenNetworkList();
        settings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
        SingleScanListener singleScanListener = new SingleScanListener(isFullBandScan);
        if (settings.channels != null) {
            for (int i = 0; i < settings.channels.length; i++) {
                localLog("settings  channels frequency: " + settings.channels[i].frequency + ", dwellTimeMS: " + settings.channels[i].dwellTimeMS + ", passive :" + settings.channels[i].passive);
            }
        }
        this.mScanner.startScan(settings, singleScanListener, workSource);
        this.mWifiMetrics.incrementConnectivityOneshotScanCount();
    }

    private void startPeriodicScan(boolean scanImmediately) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (this.mWifiState != 1 || this.mEnableAutoJoinWhenAssociated) {
            if (scanImmediately) {
                resetLastPeriodicSingleScanTimeStamp();
            }
            this.mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
            resetPeriodicSingleScanInterval();
            startPeriodicSingleScan();
        }
    }

    private void startDisconnectedPnoScan() {
        WifiScanner.PnoSettings pnoSettings = new WifiScanner.PnoSettings();
        List<WifiScanner.PnoSettings.PnoNetwork> pnoNetworkList = this.mConfigManager.retrievePnoNetworkList();
        int listSize = pnoNetworkList.size();
        if (listSize == 0) {
            localLog("No saved network for starting disconnected PNO.");
            return;
        }
        pnoSettings.networkList = new WifiScanner.PnoSettings.PnoNetwork[listSize];
        pnoSettings.networkList = (WifiScanner.PnoSettings.PnoNetwork[]) pnoNetworkList.toArray(pnoSettings.networkList);
        pnoSettings.min5GHzRssi = this.mMin5GHzRssi;
        pnoSettings.min24GHzRssi = this.mMin24GHzRssi;
        pnoSettings.initialScoreMax = this.mInitialScoreMax;
        pnoSettings.currentConnectionBonus = this.mCurrentConnectionBonus;
        pnoSettings.sameNetworkBonus = this.mSameNetworkBonus;
        pnoSettings.secureBonus = this.mSecureBonus;
        pnoSettings.band5GHzBonus = this.mBand5GHzBonus;
        WifiScanner.ScanSettings scanSettings = new WifiScanner.ScanSettings();
        scanSettings.band = getScanBand();
        scanSettings.reportEvents = 4;
        scanSettings.numBssidsPerScan = 0;
        scanSettings.periodInMs = PERIODIC_SCAN_INTERVAL_MS;
        this.mPnoScanListener.clearScanDetails();
        this.mScanner.startDisconnectedPnoScan(scanSettings, pnoSettings, this.mPnoScanListener);
        this.mPnoScanStarted = true;
    }

    private void stopPnoScan() {
        if (this.mPnoScanStarted) {
            this.mScanner.stopPnoScan(this.mPnoScanListener);
        }
        this.mPnoScanStarted = false;
    }

    private void scheduleWatchdogTimer() {
        localLog("scheduleWatchdogTimer");
        this.mAlarmManager.set(2, 1680000 + this.mClock.getElapsedSinceBootMillis(), WATCHDOG_TIMER_TAG, this.mWatchdogListener, this.mEventHandler);
    }

    private void schedulePeriodicScanTimer(int intervalMs) {
        AlarmManager.OnAlarmListener onAlarmListener = this.mPeriodicScanTimerListener;
        Handler handler = this.mEventHandler;
        this.mAlarmManager.setExact(2, ((long) intervalMs) + this.mClock.getElapsedSinceBootMillis(), PERIODIC_SCAN_TIMER_TAG, onAlarmListener, handler);
        this.mPeriodicScanTimerSet = true;
    }

    private void cancelPeriodicScanTimer() {
        if (this.mPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mPeriodicScanTimerListener);
            this.mPeriodicScanTimerSet = false;
        }
    }

    /* access modifiers changed from: private */
    public void scheduleDelayedSingleScan(boolean isFullBandScan) {
        localLog("scheduleDelayedSingleScan");
        RestartSingleScanListener restartSingleScanListener = new RestartSingleScanListener(isFullBandScan);
        this.mAlarmManager.set(2, 2000 + this.mClock.getElapsedSinceBootMillis(), RESTART_SINGLE_SCAN_TIMER_TAG, restartSingleScanListener, this.mEventHandler);
    }

    /* access modifiers changed from: private */
    public void scheduleDelayedConnectivityScan(int msFromNow) {
        localLog("scheduleDelayedConnectivityScan");
        AlarmManager.OnAlarmListener onAlarmListener = this.mRestartScanListener;
        Handler handler = this.mEventHandler;
        this.mAlarmManager.set(2, ((long) msFromNow) + this.mClock.getElapsedSinceBootMillis(), RESTART_CONNECTIVITY_SCAN_TIMER_TAG, onAlarmListener, handler);
    }

    /* access modifiers changed from: protected */
    public void startConnectivityScan(boolean scanImmediately, boolean isRestartScan) {
        localLog("startConnectivityScan: screenOn=" + this.mScreenOn + " wifiState=" + stateToString(this.mWifiState) + " scanImmediately=" + scanImmediately + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            stopConnectivityScan(isRestartScan);
            if (this.mWifiState == 1 || this.mWifiState == 2) {
                if (this.mScreenOn) {
                    startPeriodicScan(scanImmediately);
                } else if (this.mWifiState == 2 && !this.mPnoScanStarted) {
                    startDisconnectedPnoScan();
                }
            }
        }
    }

    private void stopConnectivityScan(boolean isRestartScan) {
        cancelPeriodicScanTimer();
        stopPnoScan();
        if (!isRestartScan) {
            this.mScanRestartCount = 0;
        }
    }

    public void handleScreenStateChanged(boolean screenOn) {
        localLog("handleScreenStateChanged: screenOn=" + screenOn);
        this.mScreenOn = screenOn;
        if (R1) {
            this.mWifiNetworkNotifier.handleScreenStateChanged(screenOn);
        } else {
            this.mOpenNetworkNotifier.handleScreenStateChanged(screenOn);
        }
        this.mCarrierNetworkNotifier.handleScreenStateChanged(screenOn);
        startConnectivityScan(false, false);
        if (screenOn) {
            startHourPeriodicSingleScan();
        } else {
            stopHourPeriodicSingleScan();
        }
    }

    private static String stateToString(int state) {
        switch (state) {
            case 1:
                return "connected";
            case 2:
                return "disconnected";
            case 3:
                return "transitioning";
            default:
                return "unknown";
        }
    }

    public void handleConnectionStateChanged(int state) {
        localLog("handleConnectionStateChanged: state=" + stateToString(state));
        this.mWifiState = state;
        if (this.mWifiState == 1) {
            if (R1) {
                this.mWifiNetworkNotifier.clearPendingNotification();
            } else {
                this.mOpenNetworkNotifier.handleWifiConnected();
            }
            this.mCarrierNetworkNotifier.handleWifiConnected();
        }
        handleScanCountChanged(2);
        if (this.mWifiState == 2) {
            this.mLastConnectionAttemptBssid = null;
            scheduleWatchdogTimer();
            startConnectivityScan(true, false);
            return;
        }
        if (this.mWifiState == 1) {
            this.mPnoScanRestartCount = 0;
        }
        startConnectivityScan(false, false);
    }

    public void handleConnectionAttemptEnded(int failureCode) {
        if (failureCode != 1) {
            this.mOpenNetworkNotifier.handleConnectionFailure();
            this.mCarrierNetworkNotifier.handleConnectionFailure();
        }
    }

    public void setUntrustedConnectionAllowed(boolean allowed) {
        localLog("setUntrustedConnectionAllowed: allowed=" + allowed);
        if (this.mUntrustedConnectionAllowed != allowed) {
            this.mUntrustedConnectionAllowed = allowed;
            startConnectivityScan(true, false);
        }
    }

    public void setUserConnectChoice(int netId) {
        localLog("setUserConnectChoice: netId=" + netId);
        this.mNetworkSelector.setUserConnectChoice(netId);
    }

    public void prepareForForcedConnection(int netId) {
        localLog("prepareForForcedConnection: netId=" + netId);
        clearConnectionAttemptTimeStamps();
        clearBssidBlacklist();
    }

    public void forceConnectivityScan(WorkSource workSource) {
        localLog("forceConnectivityScan in request of " + workSource);
        this.mWaitForFullBandScanResults = true;
        startSingleScan(true, workSource);
    }

    private boolean updateBssidBlacklist(String bssid, boolean enable, int reasonCode) {
        boolean z = false;
        if (enable) {
            if (this.mBssidBlacklist.remove(bssid) != null) {
                z = true;
            }
            return z;
        } else if (!this.mStateMachine.isConnected() || getScanResultsHasSameSsid(bssid).size() < 2) {
            return false;
        } else {
            BssidBlacklistStatus status = this.mBssidBlacklist.get(bssid);
            if (status == null) {
                status = new BssidBlacklistStatus();
                this.mBssidBlacklist.put(bssid, status);
            }
            status.blacklistedTimeStamp = this.mClock.getElapsedSinceBootMillis();
            status.counter++;
            if (status.isBlacklisted || (status.counter < 3 && reasonCode != 17)) {
                return false;
            }
            status.isBlacklisted = true;
            return true;
        }
    }

    public boolean trackBssid(String bssid, boolean enable, int reasonCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("trackBssid: ");
        sb.append(enable ? "enable " : "disable ");
        sb.append(StringUtil.safeDisplayBssid(bssid));
        sb.append(" reason code ");
        sb.append(reasonCode);
        localLog(sb.toString());
        if (bssid == null || !updateBssidBlacklist(bssid, enable, reasonCode)) {
            return false;
        }
        updateFirmwareRoamingConfiguration();
        if (!enable) {
            startConnectivityScan(true, false);
        }
        return true;
    }

    @VisibleForTesting
    public boolean isBssidDisabled(String bssid) {
        BssidBlacklistStatus status = this.mBssidBlacklist.get(bssid);
        if (status == null) {
            return false;
        }
        return status.isBlacklisted;
    }

    private HashSet<String> buildBssidBlacklist() {
        HashSet<String> blacklistedBssids = new HashSet<>();
        for (String bssid : this.mBssidBlacklist.keySet()) {
            if (isBssidDisabled(bssid)) {
                blacklistedBssids.add(bssid);
            }
        }
        return blacklistedBssids;
    }

    private void updateFirmwareRoamingConfiguration() {
        if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
            int maxBlacklistSize = this.mConnectivityHelper.getMaxNumBlacklistBssid();
            if (maxBlacklistSize <= 0) {
                Log.wtf(TAG, "Invalid max BSSID blacklist size:  " + maxBlacklistSize);
                return;
            }
            ArrayList<String> blacklistedBssids = new ArrayList<>(buildBssidBlacklist());
            int blacklistSize = blacklistedBssids.size();
            if (blacklistSize > maxBlacklistSize) {
                Log.wtf(TAG, "Attempt to write " + blacklistSize + " blacklisted BSSIDs, max size is " + maxBlacklistSize);
                blacklistedBssids = new ArrayList<>(blacklistedBssids.subList(0, maxBlacklistSize));
                localLog("Trim down BSSID blacklist size from " + blacklistSize + " to " + blacklistedBssids.size());
            }
            if (!this.mConnectivityHelper.setFirmwareRoamingConfiguration(blacklistedBssids, new ArrayList())) {
                localLog("Failed to set firmware roaming configuration.");
            }
        }
    }

    private void refreshBssidBlacklist() {
        if (!this.mBssidBlacklist.isEmpty()) {
            boolean updated = false;
            Iterator<BssidBlacklistStatus> iter = this.mBssidBlacklist.values().iterator();
            Long currentTimeStamp = Long.valueOf(this.mClock.getElapsedSinceBootMillis());
            while (iter.hasNext()) {
                BssidBlacklistStatus status = iter.next();
                if (status.isBlacklisted && currentTimeStamp.longValue() - status.blacklistedTimeStamp >= 300000) {
                    iter.remove();
                    updated = true;
                }
            }
            if (updated) {
                updateFirmwareRoamingConfiguration();
            }
        }
    }

    private void clearBssidBlacklist() {
        this.mBssidBlacklist.clear();
        updateFirmwareRoamingConfiguration();
    }

    private List<ScanResult> getScanResultsHasSameSsid(String bssid) {
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "getScanResultsHasSameSsid: bssid is empty.");
            return new ArrayList();
        }
        List<ScanResult> scanResults = null;
        if (this.mScanner != null) {
            scanResults = this.mScanner.getSingleScanResults();
        }
        if (scanResults.isEmpty()) {
            Log.d(TAG, "getScanResultsHasSameSsid: WifiStateMachine.ScanResultsList is empty.");
            return scanResults;
        }
        String ssid = null;
        Iterator<ScanResult> it = scanResults.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ScanResult result = it.next();
            if (bssid.equals(result.BSSID)) {
                ssid = result.SSID;
                break;
            }
        }
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "getScanResultsHasSameSsid: can't find the corresponding ssid with the given bssid.");
            return scanResults;
        }
        List<ScanResult> sameSsidList = new ArrayList<>();
        for (ScanResult result2 : scanResults) {
            if (ssid.equals(result2.SSID)) {
                sameSsidList.add(result2);
            }
        }
        return sameSsidList;
    }

    private void start() {
        this.mConnectivityHelper.getFirmwareRoamingInfo();
        clearBssidBlacklist();
        startConnectivityScan(true, false);
    }

    private void stop() {
        stopConnectivityScan(false);
        clearBssidBlacklist();
        resetLastPeriodicSingleScanTimeStamp();
        if (R1) {
            this.mWifiNetworkNotifier.clearPendingNotification();
        } else {
            this.mOpenNetworkNotifier.clearPendingNotification(true);
        }
        this.mCarrierNetworkNotifier.clearPendingNotification(true);
        this.mLastConnectionAttemptBssid = null;
        this.mWaitForFullBandScanResults = false;
    }

    private void updateRunningState() {
        if (!this.mWifiEnabled || !this.mWifiConnectivityManagerEnabled) {
            localLog("Stopping WifiConnectivityManager");
            stop();
            return;
        }
        localLog("Starting up WifiConnectivityManager");
        start();
    }

    public void setWifiEnabled(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Set WiFi ");
        sb.append(enable ? "enabled" : "disabled");
        localLog(sb.toString());
        this.mWifiEnabled = enable;
        if (!this.mWifiEnabled) {
            this.mOldSsidList.clear();
        }
        updateRunningState();
    }

    public boolean isWifiConnectivityManagerEnabled() {
        return this.mWifiConnectivityManagerEnabled;
    }

    public void enable(boolean enable) {
        StringBuilder sb = new StringBuilder();
        sb.append("Set WiFiConnectivityManager ");
        sb.append(enable ? "enabled" : "disabled");
        localLog(sb.toString());
        this.mWifiConnectivityManagerEnabled = enable;
        updateRunningState();
        if (enable) {
            stopHourPeriodicSingleScan();
        } else {
            startHourPeriodicSingleScan();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getLowRssiNetworkRetryDelay() {
        return this.mPnoScanListener.getLowRssiNetworkRetryDelay();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public long getLastPeriodicSingleScanTimeStamp() {
        return this.mLastPeriodicSingleScanTimeStamp;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConnectivityManager");
        pw.println("WifiConnectivityManager - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConnectivityManager - Log End ----");
        this.mOpenNetworkNotifier.dump(fd, pw, args);
        this.mCarrierNetworkNotifier.dump(fd, pw, args);
    }

    /* access modifiers changed from: private */
    public String getScanKey(WifiScanner.ScanListener scanListener) {
        int key = this.mScanner.getScanKey(this);
        return "Key#" + key + ":";
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    /* access modifiers changed from: package-private */
    public void localLog(String scanKey, String eventKey, String log, Object... params) {
        if (!"Key#0:".equals(scanKey)) {
            WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
        }
    }
}
