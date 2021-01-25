package com.android.server.wifi;

import android.app.AlarmManager;
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
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.HwUtil.IHwDevicePolicyManagerEx;
import com.android.server.wifi.WifiConfigManager;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hwUtil.ScanResultUtilEx;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.hwUtil.WifiNetworkNotifier;
import com.android.server.wifi.util.ScanResultUtil;
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
    public static final String HOUR_PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Hour Periodic Scan Timer";
    protected static final int HOUR_PERIOD_SCAN_INTERVAL_MS = 3600000;
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
    @VisibleForTesting
    static final int MOVING_PNO_SCAN_INTERVAL_MS = 20000;
    private static final int MSG_SCAN_GENIE_FAIL_EVENT = 1002;
    private static final int MSG_SCAN_GENIE_OCCUR = 1000;
    private static final int MSG_SCAN_GENIE_SUCC_EVENT = 1001;
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
    private static final String SCAN_GENIE_FULL_BAND = "FullBand";
    private static final String SCAN_GENIE_SPECIFIED_BAND = "SpecifiedBand";
    private static final boolean SCAN_IMMEDIATELY = true;
    private static final boolean SCAN_ON_SCHEDULE = false;
    private static final int SCORED_NETWORK_EVALUATOR_PRIORITY = 3;
    @VisibleForTesting
    static final int STATIONARY_PNO_SCAN_INTERVAL_MS = 60000;
    private static final int SWITCH_TO_WIFI_AUTO = 0;
    private static final String TAG = "WifiConnectivityManager";
    protected static final int VALID_ROAM_BLACK_LIST_MIN_SSID_NUM = 2;
    private static final String VALUE_DISABLE = "value_disable";
    private static final int WATCHDOG_INTERVAL_MS = 1680000;
    public static final String WATCHDOG_TIMER_TAG = "WifiConnectivityManager Schedule Watchdog Timer";
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    public static final int WIFI_STATE_TRANSITIONING = 3;
    public static final int WIFI_STATE_UNKNOWN = 0;
    private final AlarmManager mAlarmManager;
    private final AllSingleScanListener mAllSingleScanListener = new AllSingleScanListener();
    private int mBand5GHzBonus;
    private Map<String, BssidBlacklistStatus> mBssidBlacklist = new HashMap();
    private final CarrierNetworkConfig mCarrierNetworkConfig;
    private final CarrierNetworkNotifier mCarrierNetworkNotifier;
    private final Clock mClock;
    private final WifiConfigManager mConfigManager;
    private final LinkedList<Long> mConnectionAttemptTimeStamps;
    private final WifiConnectivityHelper mConnectivityHelper;
    private Context mContext;
    private int mCurrentConnectionBonus;
    private boolean mDbg = false;
    private boolean mEnableAutoJoinWhenAssociated;
    private final Handler mEventHandler;
    protected boolean mExtendWifiScanPeriodForClone = false;
    private int mFullScanMaxRxRate;
    private int mFullScanMaxTxRate;
    protected boolean mHourPeriodicScanTimerSet = false;
    private HwWifiCHRService mHwWifiCHRService;
    private String mLastConnectionAttemptBssid = null;
    protected long mLastHourPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private long mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private final LocalLog mLocalLog;
    protected final WifiNetworkSelector mNetworkSelector;
    private final Set<String> mOldSsidList = new HashSet();
    private final OpenNetworkNotifier mOpenNetworkNotifier;
    private final AlarmManager.OnAlarmListener mPeriodicScanTimerListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass3 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            WifiConnectivityManager.this.periodicScanTimerHandler();
        }
    };
    private boolean mPeriodicScanTimerSet = false;
    private int mPeriodicSingleScanInterval = 20000;
    private int mPnoScanIntervalMs;
    private final PnoScanListener mPnoScanListener = new PnoScanListener();
    private int mPnoScanRestartCount = 0;
    protected boolean mPnoScanStarted = false;
    private final AlarmManager.OnAlarmListener mRestartScanListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass1 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            WifiConnectivityManager.this.startConnectivityScan(true, true);
        }
    };
    private int mRssiScoreOffset;
    private int mRssiScoreSlope;
    private boolean mRunning = false;
    private int mSameNetworkBonus;
    private int mScanRestartCount = 0;
    private String mScanType = "";
    private WifiScanner mScanner;
    protected final ScoringParams mScoringParams;
    private boolean mScreenOn = false;
    private int mSecureBonus;
    private int mSingleScanRestartCount = 0;
    private boolean mSingleScanStarted = false;
    private boolean mSpecificNetworkRequestInProgress = false;
    private final ClientModeImpl mStateMachine;
    private int mTotalConnectivityAttemptsRateLimited = 0;
    private boolean mTrustedConnectionAllowed = false;
    private boolean mUntrustedConnectionAllowed = false;
    private boolean mUseSingleRadioChainScanResults = false;
    private boolean mWaitForFullBandScanResults = false;
    private final AlarmManager.OnAlarmListener mWatchdogListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.WifiConnectivityManager.AnonymousClass2 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            WifiConnectivityManager.this.watchdogHandler();
        }
    };
    private boolean mWifiConnectivityManagerEnabled = true;
    private boolean mWifiEnabled = false;
    private final WifiInfo mWifiInfo;
    private final WifiInjector mWifiInjector;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;
    private WifiNetworkNotifier mWifiNetworkNotifier = null;
    protected int mWifiState = 0;

    static /* synthetic */ int access$1308(WifiConnectivityManager x0) {
        int i = x0.mScanRestartCount;
        x0.mScanRestartCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$1908(WifiConnectivityManager x0) {
        int i = x0.mSingleScanRestartCount;
        x0.mSingleScanRestartCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$2308(WifiConnectivityManager x0) {
        int i = x0.mPnoScanRestartCount;
        x0.mPnoScanRestartCount = i + 1;
        return i;
    }

    /* access modifiers changed from: private */
    public static class BssidBlacklistStatus {
        public long blacklistedTimeStamp;
        public int counter;
        public boolean isBlacklisted;

        private BssidBlacklistStatus() {
            this.blacklistedTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    /* access modifiers changed from: private */
    public class RestartSingleScanListener implements AlarmManager.OnAlarmListener {
        private final boolean mIsFullBandScan;

        RestartSingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            WifiConnectivityManager.this.startSingleScan(this.mIsFullBandScan, ClientModeImpl.WIFI_WORK_SOURCE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleScanResults(List<ScanDetail> scanDetails, String listenerName, String keys) {
        refreshBssidBlacklist();
        if (Settings.System.getInt(this.mContext.getContentResolver(), "wifi_connect_type", 0) != 0) {
            return false;
        }
        if (this.mStateMachine.isSupplicantTransientState()) {
            Log.i("WifiScanLog", keys + listenerName + " onResults: No network selection because supplicantTransient is " + this.mStateMachine.isSupplicantTransientState());
            return false;
        }
        localLog(keys, "27", " onResults: start network selection");
        WifiNetworkSelector wifiNetworkSelector = this.mNetworkSelector;
        wifiNetworkSelector.mCurrentScanKeys = keys;
        WifiConfiguration candidate = wifiNetworkSelector.selectNetwork(scanDetails, buildBssidBlacklist(), this.mWifiInfo, this.mStateMachine.isConnected(), this.mStateMachine.isDisconnected(), this.mUntrustedConnectionAllowed);
        this.mWifiLastResortWatchdog.updateAvailableNetworks(this.mNetworkSelector.getConnectableScanDetails());
        this.mWifiMetrics.countScanResults(scanDetails);
        if (candidate == null) {
            if (this.mWifiState == 2) {
                if (R1) {
                    this.mWifiNetworkNotifier.handleScanResults(this.mNetworkSelector.getFilteredScanDetailsForOpenUnsavedNetworks());
                }
                if (this.mCarrierNetworkConfig.isCarrierEncryptionInfoAvailable()) {
                    this.mCarrierNetworkNotifier.handleScanResults(this.mNetworkSelector.getFilteredScanDetailsForCarrierUnsavedNetworks(this.mCarrierNetworkConfig));
                }
            }
            return false;
        } else if (ignorePoorNetwork(candidate)) {
            return false;
        } else {
            localLog(keys, "28", "WNS selectNetwork candidate-%s", StringUtilEx.safeDisplaySsid(candidate.SSID));
            connectToNetwork(candidate, keys);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public class AllSingleScanListener implements WifiScanner.ScanListener {
        private boolean mNeedLog;
        private int mNumScanResultsIgnoredDueToSingleRadioChain;
        private List<ScanDetail> mScanDetails;

        private AllSingleScanListener() {
            this.mScanDetails = new ArrayList();
            this.mNeedLog = true;
            this.mNumScanResultsIgnoredDueToSingleRadioChain = 0;
        }

        public void clearScanDetails() {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "29", "AllScan clearScanDetails.");
            this.mScanDetails.clear();
            this.mNumScanResultsIgnoredDueToSingleRadioChain = 0;
        }

        public void onSuccess() {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "30", "AllScan registerScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "31", "AllScan registerScanListener onFailure: reason:%s description:%s", Integer.valueOf(reason), description);
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "32", "AllScan onPeriodChanged periodInMs:%s", Integer.valueOf(periodInMs));
        }

        public void onResults(WifiScanner.ScanData[] results) {
            if (results.length > 0 && results[0].isHiddenScanResult()) {
                Log.i(WifiConnectivityManager.TAG, "HiddenScanResult allSingleScanlister retrun");
            } else if (!WifiConnectivityManager.this.mWifiEnabled || !WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
                wifiConnectivityManager.localLog(wifiConnectivityManager.mScanner.mCurrentScanKeys, "33", "AllScan onResults returned mWifiEnabled:%s, mWifiConnectivityManagerEnabled:%s", Boolean.valueOf(WifiConnectivityManager.this.mWifiEnabled), Boolean.valueOf(WifiConnectivityManager.this.mWifiConnectivityManagerEnabled));
                clearScanDetails();
                WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
            } else {
                boolean isFullBandScanResults = results[0].getBandScanned() == 7 || results[0].getBandScanned() == 3;
                if (WifiConnectivityManager.this.mWaitForFullBandScanResults) {
                    if (!isFullBandScanResults) {
                        WifiConnectivityManager wifiConnectivityManager2 = WifiConnectivityManager.this;
                        wifiConnectivityManager2.localLog(wifiConnectivityManager2.mScanner.mCurrentScanKeys, "34", "AllScan waiting for full band scan results.");
                        clearScanDetails();
                        return;
                    }
                    WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
                }
                if (results.length > 0) {
                    WifiConnectivityManager.this.mWifiMetrics.incrementAvailableNetworksHistograms(this.mScanDetails, isFullBandScanResults);
                }
                if (this.mNumScanResultsIgnoredDueToSingleRadioChain > 0) {
                    Log.i(WifiConnectivityManager.TAG, "Number of scan results ignored due to single radio chain scan: " + this.mNumScanResultsIgnoredDueToSingleRadioChain);
                }
                WifiConnectivityManager wifiConnectivityManager3 = WifiConnectivityManager.this;
                boolean wasConnectAttempted = wifiConnectivityManager3.handleScanResults(this.mScanDetails, "AllSingleScanListener", wifiConnectivityManager3.mScanner.mCurrentScanKeys);
                String result = ScanResultUtilEx.getScanResultLogs(WifiConnectivityManager.this.mOldSsidList, this.mScanDetails);
                WifiConnectivityManager wifiConnectivityManager4 = WifiConnectivityManager.this;
                wifiConnectivityManager4.localLog(wifiConnectivityManager4.mScanner.mCurrentScanKeys, "35", "AllScan wasConnectAttempted:%s, results:%s", Boolean.valueOf(wasConnectAttempted), result);
                clearScanDetails();
                if (WifiConnectivityManager.this.mPnoScanStarted) {
                    if (wasConnectAttempted) {
                        WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoBad();
                    } else {
                        WifiConnectivityManager.this.mWifiMetrics.incrementNumConnectivityWatchdogPnoGood();
                        WifiConnectivityManager.this.startConnectivityScan(true, true);
                    }
                }
                WifiConnectivityManager wifiConnectivityManager5 = WifiConnectivityManager.this;
                if (wifiConnectivityManager5.forceFullbandScanAgain(wifiConnectivityManager5.mScreenOn, WifiConnectivityManager.this.mWifiState, wasConnectAttempted, WifiConnectivityManager.this.mSingleScanStarted)) {
                    WifiConnectivityManager.this.mSingleScanStarted = false;
                    WifiConnectivityManager.this.startSingleScan(true, ClientModeImpl.WIFI_WORK_SOURCE);
                }
                WifiConnectivityManager.this.mScanRestartCount = 0;
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (!WifiConnectivityManager.this.mWifiEnabled || !WifiConnectivityManager.this.mWifiConnectivityManagerEnabled) {
                if (this.mNeedLog) {
                    WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
                    wifiConnectivityManager.localLog("Key#00:", "36", "AllScan onFullResult returned mWifiEnabled:%s, mWifiConnectivityManagerEnabled:%s", Boolean.valueOf(wifiConnectivityManager.mWifiEnabled), Boolean.valueOf(WifiConnectivityManager.this.mWifiConnectivityManagerEnabled));
                }
                this.mNeedLog = false;
                return;
            }
            this.mNeedLog = true;
            if (WifiConnectivityManager.this.mDbg) {
                WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys + "AllSingleScanListener onFullResult: " + StringUtilEx.safeDisplaySsid(fullScanResult.SSID) + " capabilities " + fullScanResult.capabilities);
            }
            if (WifiConnectivityManager.this.mUseSingleRadioChainScanResults || fullScanResult.radioChainInfos == null || fullScanResult.radioChainInfos.length != 1) {
                ScanDetail scanDetail = ScanResultUtil.toScanDetail(fullScanResult);
                if (scanDetail != null) {
                    this.mScanDetails.add(scanDetail);
                }
                WifiConnectivityManager.this.mScanRestartCount = 0;
                return;
            }
            this.mNumScanResultsIgnoredDueToSingleRadioChain++;
        }
    }

    /* access modifiers changed from: private */
    public class SingleScanListener implements WifiScanner.ScanListener {
        private final boolean mIsFullBandScan;

        SingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onSuccess() {
            if (WifiConnectivityManager.SCAN_GENIE_SPECIFIED_BAND.equals(WifiConnectivityManager.this.mScanType)) {
                WifiConnectivityManager.this.mScanType = "";
                WifiConnectivityManager.this.notifyScanGenieEvent(WifiConnectivityManager.MSG_SCAN_GENIE_SUCC_EVENT);
            }
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "37", "SingleScanListener onFailure: reason:%s description:%s SingleScanRestartCount:%s", Integer.valueOf(reason), description, Integer.valueOf(WifiConnectivityManager.this.mSingleScanRestartCount));
            if (WifiConnectivityManager.SCAN_GENIE_FULL_BAND.equals(WifiConnectivityManager.this.mScanType)) {
                WifiConnectivityManager.this.notifyScanGenieEvent(WifiConnectivityManager.MSG_SCAN_GENIE_FAIL_EVENT);
            }
            if (WifiConnectivityManager.access$1908(WifiConnectivityManager.this) >= 5 || !WifiConnectivityManager.this.mStateMachine.attemptAutoConnect()) {
                WifiConnectivityManager.this.mSingleScanRestartCount = 0;
                WifiConnectivityManager.this.localLog("Failed to successfully start single scan for 5 times");
                return;
            }
            WifiConnectivityManager.this.scheduleDelayedSingleScan(this.mIsFullBandScan);
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "38", "SingleScanListener onPeriodChanged: actual scan period %s ms", Integer.valueOf(periodInMs));
        }

        public void onResults(WifiScanner.ScanData[] results) {
            WifiConnectivityManager.this.mSingleScanStarted = true;
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (WifiConnectivityManager.this.mDbg) {
                WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
                wifiConnectivityManager.localLog("SingleScanListener onFullResult: " + StringUtilEx.safeDisplaySsid(fullScanResult.SSID) + " capabilities " + fullScanResult.capabilities);
            }
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
        }
    }

    /* access modifiers changed from: private */
    public class PnoScanListener implements WifiScanner.PnoScanListener {
        private int mLowRssiNetworkRetryDelay;
        private List<ScanDetail> mScanDetails;

        private PnoScanListener() {
            this.mScanDetails = new CopyOnWriteArrayList();
            this.mLowRssiNetworkRetryDelay = 20000;
        }

        public void clearScanDetails() {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "39", "PnoScan clearScanDetails.");
            this.mScanDetails.clear();
        }

        public void resetLowRssiNetworkRetryDelay() {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "40", "PnoScan resetLowRssiNetworkRetryDelay.");
            this.mLowRssiNetworkRetryDelay = 20000;
        }

        @VisibleForTesting
        public int getLowRssiNetworkRetryDelay() {
            return this.mLowRssiNetworkRetryDelay;
        }

        public void onSuccess() {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "41", "PnoScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "42", "PnoScanListener onFailure: reason: %s, description:%s", Integer.valueOf(reason), description);
            if (WifiConnectivityManager.access$1308(WifiConnectivityManager.this) < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(WifiConnectivityManager.RESTART_SCAN_DELAY_MS);
                return;
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start PNO scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "43", "PnoScanListener onPeriodChanged: actual scan period ms", Integer.valueOf(periodInMs));
        }

        public void onResults(WifiScanner.ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            wifiConnectivityManager.localLog(wifiConnectivityManager.getScanKey(this), "44", "PnoScanListener: onPnoNetworkFound: results len = %s", Integer.valueOf(results.length));
            for (ScanResult result : results) {
                if (result.informationElements == null) {
                    WifiConnectivityManager.this.localLog("Skipping scan result with null information elements");
                } else {
                    ScanDetail scanDetail = ScanResultUtil.toScanDetail(result);
                    if (scanDetail != null) {
                        this.mScanDetails.add(scanDetail);
                    }
                }
            }
            WifiConnectivityManager wifiConnectivityManager2 = WifiConnectivityManager.this;
            boolean wasConnectAttempted = wifiConnectivityManager2.handleScanResults(this.mScanDetails, "PnoScanListener", wifiConnectivityManager2.getScanKey(this));
            clearScanDetails();
            WifiConnectivityManager.this.mScanRestartCount = 0;
            if (!wasConnectAttempted) {
                if (this.mLowRssiNetworkRetryDelay > WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS) {
                    this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS;
                }
                if (WifiConnectivityManager.access$2308(WifiConnectivityManager.this) < 2) {
                    WifiConnectivityManager.this.mAlarmManager.set(3, WifiConnectivityManager.this.mClock.getElapsedSinceBootMillis() + ((long) this.mLowRssiNetworkRetryDelay), WifiConnectivityManager.RESTART_CONNECTIVITY_SCAN_TIMER_TAG, WifiConnectivityManager.this.mRestartScanListener, WifiConnectivityManager.this.mEventHandler);
                }
                this.mLowRssiNetworkRetryDelay *= 2;
            } else {
                resetLowRssiNetworkRetryDelay();
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
        }
    }

    private class OnSavedNetworkUpdateListener implements WifiConfigManager.OnSavedNetworkUpdateListener {
        private OnSavedNetworkUpdateListener() {
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkAdded(int networkId) {
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkEnabled(int networkId) {
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkRemoved(int networkId) {
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkUpdated(int networkId) {
            WifiConnectivityManager.this.mStateMachine.updateCapabilities();
            updatePnoScan();
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkTemporarilyDisabled(int networkId, int disableReason) {
            if (disableReason != 6) {
                WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
            }
        }

        @Override // com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener
        public void onSavedNetworkPermanentlyDisabled(int networkId, int disableReason) {
            if (disableReason != 10) {
                WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
                updatePnoScan();
            }
        }

        private void updatePnoScan() {
            if (!WifiConnectivityManager.this.mScreenOn) {
                WifiConnectivityManager.this.localLog("Saved networks updated");
                WifiConnectivityManager.this.startConnectivityScan(false, false);
            }
        }
    }

    WifiConnectivityManager(Context context, ScoringParams scoringParams, ClientModeImpl stateMachine, WifiInjector injector, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog) {
        this.mContext = context;
        this.mStateMachine = stateMachine;
        this.mWifiInjector = injector;
        this.mConfigManager = configManager;
        this.mWifiInfo = wifiInfo;
        this.mNetworkSelector = networkSelector;
        this.mNetworkSelector.mConnectivityHelper = connectivityHelper;
        this.mConnectivityHelper = connectivityHelper;
        this.mLocalLog = localLog;
        this.mWifiLastResortWatchdog = wifiLastResortWatchdog;
        this.mOpenNetworkNotifier = openNetworkNotifier;
        this.mCarrierNetworkNotifier = carrierNetworkNotifier;
        this.mCarrierNetworkConfig = carrierNetworkConfig;
        if (R1) {
            this.mWifiNetworkNotifier = new WifiNetworkNotifier(context, looper, this.mWifiInjector.getFrameworkFacade());
        }
        this.mWifiMetrics = wifiMetrics;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
        this.mScoringParams = scoringParams;
        this.mConnectionAttemptTimeStamps = new LinkedList<>();
        this.mBand5GHzBonus = context.getResources().getInteger(17694913);
        this.mCurrentConnectionBonus = context.getResources().getInteger(17694930);
        this.mSameNetworkBonus = context.getResources().getInteger(17694920);
        this.mSecureBonus = context.getResources().getInteger(17694921);
        this.mRssiScoreOffset = context.getResources().getInteger(17694918);
        this.mRssiScoreSlope = context.getResources().getInteger(17694919);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17891586);
        this.mUseSingleRadioChainScanResults = context.getResources().getBoolean(17891590);
        this.mFullScanMaxTxRate = context.getResources().getInteger(17694933);
        this.mFullScanMaxRxRate = context.getResources().getInteger(17694932);
        this.mPnoScanIntervalMs = 20000;
        localLog("PNO settings: min5GHzRssi " + this.mScoringParams.getEntryRssi(5000) + " min24GHzRssi " + this.mScoringParams.getEntryRssi(ScoringParams.BAND2) + " currentConnectionBonus " + this.mCurrentConnectionBonus + " sameNetworkBonus " + this.mSameNetworkBonus + " secureNetworkBonus " + this.mSecureBonus + " initialScoreMax " + initialScoreMax());
        this.mConfigManager.setOnSavedNetworkUpdateListener(new OnSavedNetworkUpdateListener());
        this.mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    }

    private int initialScoreMax() {
        return this.mRssiScoreSlope * (Math.max(this.mScoringParams.getGoodRssi(ScoringParams.BAND2), this.mScoringParams.getGoodRssi(5000)) + this.mRssiScoreOffset);
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
        String targetAssociationId = candidate.SSID + " : " + StringUtilEx.safeDisplayBssid(targetBssid);
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
                    currentAssociationId = this.mWifiInfo.getSSID() + " : " + StringUtilEx.safeDisplayBssid(this.mWifiInfo.getBSSID());
                }
                this.mStateMachine.setCHRConnectingSartTimestamp(elapsedTimeMillis);
                if (currentConnectedNetwork == null || currentConnectedNetwork.networkId != candidate.networkId) {
                    IHwDevicePolicyManagerEx hwDevicePolicyManagerEx = HwWifiServiceFactory.getHwDevicePolicyManager();
                    if (hwDevicePolicyManagerEx == null) {
                        Log.w(TAG, "hwDevicePolicyManagerEx is null.");
                        return;
                    }
                    Bundle bundle = hwDevicePolicyManagerEx.getPolicy(null, POLICY_AUTO_CONNECT);
                    if (bundle == null || !bundle.getBoolean(VALUE_DISABLE)) {
                        if (candidate.BSSID == null || candidate.BSSID.equals("any")) {
                            targetBssid = "any";
                            localLog("connectToNetwork: Connect to " + StringUtilEx.safeDisplaySsid(candidate.SSID) + ":" + StringUtilEx.safeDisplayBssid(targetBssid) + " from " + currentAssociationId);
                        } else {
                            localLog("connectToNetwork: Connect to " + targetAssociationId + " from " + currentAssociationId);
                        }
                        if (this.mStateMachine.isScanAndManualConnectMode()) {
                            Log.i(TAG, "Only allow Manual Connection, ignore auto connection.");
                            return;
                        }
                        localLog(keys, "46", "WifiStateMachine startConnectToNetwork");
                        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
                        if (hwWifiCHRService != null) {
                            hwWifiCHRService.updateConnectType("AUTO_CONNECT");
                        }
                        this.mStateMachine.startConnectToNetwork(candidate.networkId, 1010, unselectDhcpFailedBssid(targetBssid, scanResultCandidate.BSSID, candidate));
                        return;
                    }
                    Log.w(TAG, "connectToNetwork: MDM deny auto connect!");
                } else if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                    Log.i("WifiScanLog", keys + "connectToNetwork: Roaming candidate - " + targetAssociationId + ". The actual roaming target is up to the firmware.");
                } else if (this.mStateMachine.isWifiRepeaterStarted()) {
                    Log.i("WifiScanLog", keys + "WifiRepeater is started, do not allow auto roam.");
                } else {
                    localLog(keys, "45", "connectToNetwork: Roaming to %s  from %s!", targetAssociationId, currentAssociationId);
                    this.mStateMachine.startRoamToNetwork(candidate.networkId, scanResultCandidate);
                }
            } else {
                Log.i("WifiScanLog", keys + "connectToNetwork: Too many connection attempts. Skipping this attempt!");
                this.mTotalConnectivityAttemptsRateLimited = this.mTotalConnectivityAttemptsRateLimited + 1;
            }
        } else {
            Log.i("WifiScanLog", keys + "connecToNetwork: target BSSID " + StringUtilEx.safeDisplayBssid(targetBssid) + " does not match the config specified BSSID " + StringUtilEx.safeDisplayBssid(candidate.BSSID) + ". Drop it!");
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
            localLog("No scan channels for " + StringUtilEx.safeDisplaySsid(config.getPrintableSsid()) + ". Perform full band scan");
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
    /* access modifiers changed from: public */
    private void watchdogHandler() {
        localLog("watchdogHandler");
        StringBuilder sb = new StringBuilder();
        sb.append("start a single scan from watchdogHandler ? ");
        sb.append(this.mWifiState == 2);
        Log.i("WifiScanLog", sb.toString());
        if (this.mWifiState == 2) {
            localLog("start a single scan from watchdogHandler");
            scheduleWatchdogTimer();
            startSingleScan(true, ClientModeImpl.WIFI_WORK_SOURCE);
        }
    }

    private void startPeriodicSingleScan() {
        long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
        localLog("****start Periodic SingleScan,mPeriodicSingleScanInterval : " + (this.mPeriodicSingleScanInterval / 1000) + " s");
        if (this.mLastPeriodicSingleScanTimeStamp != RESET_TIME_STAMP && !handleForceScan()) {
            long msSinceLastScan = currentTimeStamp - this.mLastPeriodicSingleScanTimeStamp;
            int mPeriodicScanInterval = getWifiScanGenieMinInterval(20000);
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
        if (this.mWifiInjector.getClientModeImpl().isNeedIgnoreScan()) {
            Log.i(TAG, "Ignore this periodic single scan because miracast is working");
            isScanNeeded = false;
        }
        if (isScanNeeded && isScanThisPeriod(this.mStateMachine.isP2pConnected())) {
            if (!WifiCommonUtils.IS_TV || this.mWifiState != 1) {
                this.mLastPeriodicSingleScanTimeStamp = currentTimeStamp;
                handleScanCountChanged(0);
                startSingleScan(isFullBandScan, ClientModeImpl.WIFI_WORK_SOURCE);
            } else {
                Log.d(TAG, "Turn off periodic scan when WiFi is connected for TV");
            }
        }
        this.mPeriodicSingleScanInterval = getPeriodicSingleScanInterval();
        schedulePeriodicScanTimer(this.mPeriodicSingleScanInterval);
    }

    private void resetLastPeriodicSingleScanTimeStamp() {
        this.mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void periodicScanTimerHandler() {
        Log.i(TAG, "periodicScanTimerHandler mScreenOn " + this.mScreenOn);
        localLog("periodicScanTimerHandler");
        if (this.mScreenOn) {
            startPeriodicSingleScan();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startSingleScan(boolean isFullBandScan, WorkSource workSource) {
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            startScan(isFullBandScan, workSource);
        }
    }

    /* access modifiers changed from: protected */
    public void startScan(boolean isFullBandScan, WorkSource workSource) {
        if (this.mScanner == null) {
            Log.e(TAG, "ignore scan and wifistate is " + this.mWifiEnabled + " ConnectivityManager is " + this.mWifiConnectivityManagerEnabled);
            return;
        }
        int wifiMode = this.mStateMachine.getWifiMode();
        boolean isNeedIgnoreScan = this.mWifiInjector.getClientModeImpl().isNeedIgnoreScan();
        if ((wifiMode & 4) != 0 || isNeedIgnoreScan || (this.mStateMachine.isConnected() && this.mStateMachine.isInGameAppMode())) {
            Log.i(TAG, "Ignore scan because wifi mode " + wifiMode + ", isNeedIgnoreScan " + isNeedIgnoreScan);
            return;
        }
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        WifiScanner.ScanSettings settings = null;
        if (!isWifiScanSpecialChannels() || this.mWifiState == 1) {
            Log.e(TAG, "isWifiScanSpecialChannels is false,mWifiState : " + this.mWifiState);
        } else {
            settings = getScanGenieSettings();
            if (!(settings == null || settings.channels == null || settings.channels.length == 0)) {
                isFullBandScan = false;
            }
            localLog("****isWifiScanSpecialChannels *settings =**:" + settings);
        }
        if (settings == null || settings.channels == null || settings.channels.length == 0) {
            settings = new WifiScanner.ScanSettings();
            if (!isFullBandScan && !setScanChannels(settings)) {
                isFullBandScan = true;
            }
        }
        this.mScanType = isFullBandScan ? SCAN_GENIE_FULL_BAND : SCAN_GENIE_SPECIFIED_BAND;
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
        if (!isFullBandScan) {
            notifyScanGenieEvent(1000);
        }
        this.mWifiMetrics.incrementConnectivityOneshotScanCount();
    }

    private void startPeriodicScan(boolean scanImmediately) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (this.mWifiState != 1 || this.mEnableAutoJoinWhenAssociated) {
            if (scanImmediately) {
                resetLastPeriodicSingleScanTimeStamp();
            }
            this.mPeriodicSingleScanInterval = 20000;
            resetPeriodicSingleScanInterval();
            startPeriodicSingleScan();
        }
    }

    private static int deviceMobilityStateToPnoScanIntervalMs(int state) {
        if (state == 0 || state == 1 || state == 2) {
            return 20000;
        }
        if (state != 3) {
            return -1;
        }
        return 60000;
    }

    public void setDeviceMobilityState(int newState) {
        int newPnoScanIntervalMs = deviceMobilityStateToPnoScanIntervalMs(newState);
        if (newPnoScanIntervalMs < 0) {
            Log.e(TAG, "Invalid device mobility state: " + newState);
        } else if (newPnoScanIntervalMs != this.mPnoScanIntervalMs) {
            this.mPnoScanIntervalMs = newPnoScanIntervalMs;
            Log.i(TAG, "PNO Scan Interval changed to " + this.mPnoScanIntervalMs + " ms.");
            if (this.mPnoScanStarted) {
                Log.i(TAG, "Restarting PNO Scan with new scan interval");
                stopPnoScan();
                this.mWifiMetrics.enterDeviceMobilityState(newState);
                startDisconnectedPnoScan();
                return;
            }
            this.mWifiMetrics.enterDeviceMobilityState(newState);
        } else if (this.mPnoScanStarted) {
            this.mWifiMetrics.logPnoScanStop();
            this.mWifiMetrics.enterDeviceMobilityState(newState);
            this.mWifiMetrics.logPnoScanStart();
        } else {
            this.mWifiMetrics.enterDeviceMobilityState(newState);
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
        pnoSettings.min5GHzRssi = this.mScoringParams.getEntryRssi(5000);
        pnoSettings.min24GHzRssi = this.mScoringParams.getEntryRssi(ScoringParams.BAND2);
        pnoSettings.initialScoreMax = initialScoreMax();
        pnoSettings.currentConnectionBonus = this.mCurrentConnectionBonus;
        pnoSettings.sameNetworkBonus = this.mSameNetworkBonus;
        pnoSettings.secureBonus = this.mSecureBonus;
        pnoSettings.band5GHzBonus = this.mBand5GHzBonus;
        WifiScanner.ScanSettings scanSettings = new WifiScanner.ScanSettings();
        scanSettings.band = getScanBand();
        scanSettings.reportEvents = 4;
        scanSettings.numBssidsPerScan = 0;
        scanSettings.periodInMs = this.mPnoScanIntervalMs;
        this.mPnoScanListener.clearScanDetails();
        this.mScanner.startDisconnectedPnoScan(scanSettings, pnoSettings, this.mPnoScanListener);
        this.mPnoScanStarted = true;
        this.mWifiMetrics.logPnoScanStart();
    }

    private void stopPnoScan() {
        if (this.mPnoScanStarted) {
            this.mScanner.stopPnoScan(this.mPnoScanListener);
            this.mPnoScanStarted = false;
            this.mWifiMetrics.logPnoScanStop();
        }
    }

    private void scheduleWatchdogTimer() {
        localLog("scheduleWatchdogTimer");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 1680000, WATCHDOG_TIMER_TAG, this.mWatchdogListener, this.mEventHandler);
    }

    private void schedulePeriodicScanTimer(int intervalMs) {
        this.mAlarmManager.setExact(2, this.mClock.getElapsedSinceBootMillis() + ((long) intervalMs), PERIODIC_SCAN_TIMER_TAG, this.mPeriodicScanTimerListener, this.mEventHandler);
        this.mPeriodicScanTimerSet = true;
    }

    private void cancelPeriodicScanTimer() {
        if (this.mPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mPeriodicScanTimerListener);
            this.mPeriodicScanTimerSet = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleDelayedSingleScan(boolean isFullBandScan) {
        localLog("scheduleDelayedSingleScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 2000, RESTART_SINGLE_SCAN_TIMER_TAG, new RestartSingleScanListener(isFullBandScan), this.mEventHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleDelayedConnectivityScan(int msFromNow) {
        localLog("scheduleDelayedConnectivityScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + ((long) msFromNow), RESTART_CONNECTIVITY_SCAN_TIMER_TAG, this.mRestartScanListener, this.mEventHandler);
    }

    /* access modifiers changed from: protected */
    public void startConnectivityScan(boolean scanImmediately, boolean isRestartScan) {
        localLog("startConnectivityScan: screenOn=" + this.mScreenOn + " wifiState=" + stateToString(this.mWifiState) + " scanImmediately=" + scanImmediately + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            stopConnectivityScan(isRestartScan);
            int i = this.mWifiState;
            if (i != 1 && i != 2) {
                return;
            }
            if (this.mScreenOn) {
                startPeriodicScan(scanImmediately);
            } else if (this.mWifiState == 2 && !this.mPnoScanStarted) {
                startDisconnectedPnoScan();
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
        this.mConfigManager.clearPnoConfigKeyBlacklist();
        startConnectivityScan(false, false);
        if (screenOn) {
            startHourPeriodicSingleScan();
        } else {
            stopHourPeriodicSingleScan();
        }
    }

    private static String stateToString(int state) {
        if (state == 1) {
            return "connected";
        }
        if (state == 2) {
            return "disconnected";
        }
        if (state != 3) {
            return "unknown";
        }
        return "transitioning";
    }

    public void handleConnectionStateChanged(int state) {
        localLog("handleConnectionStateChanged: state=" + stateToString(state));
        this.mWifiState = state;
        if (this.mWifiState == 1) {
            if (R1) {
                this.mWifiNetworkNotifier.clearPendingNotification();
            } else {
                this.mOpenNetworkNotifier.handleWifiConnected(this.mWifiInfo.getSSID());
            }
        }
        handleScanCountChanged(2);
        int i = this.mWifiState;
        if (i == 2) {
            this.mExtendWifiScanPeriodForClone = false;
            this.mLastConnectionAttemptBssid = null;
            scheduleWatchdogTimer();
            startConnectivityScan(true, false);
            return;
        }
        if (i == 1) {
            this.mPnoScanRestartCount = 0;
        }
        startConnectivityScan(false, false);
    }

    public void handleConnectionAttemptEnded(int failureCode) {
        String ssid;
        if (failureCode == 1) {
            if (this.mWifiInfo.getWifiSsid() == null) {
                ssid = null;
            } else {
                ssid = this.mWifiInfo.getWifiSsid().toString();
            }
            this.mOpenNetworkNotifier.handleWifiConnected(ssid);
            this.mCarrierNetworkNotifier.handleWifiConnected(ssid);
            return;
        }
        this.mOpenNetworkNotifier.handleConnectionFailure();
        this.mCarrierNetworkNotifier.handleConnectionFailure();
        if (!this.mScreenOn) {
            this.mAlarmManager.set(3, this.mClock.getElapsedSinceBootMillis() + 20000, RESTART_CONNECTIVITY_SCAN_TIMER_TAG, this.mRestartScanListener, this.mEventHandler);
        }
    }

    private void checkStateAndEnable() {
        enable(!this.mSpecificNetworkRequestInProgress && (this.mUntrustedConnectionAllowed || this.mTrustedConnectionAllowed));
        startConnectivityScan(true, false);
    }

    public void setTrustedConnectionAllowed(boolean allowed) {
        localLog("setTrustedConnectionAllowed: allowed=" + allowed);
        if (this.mTrustedConnectionAllowed != allowed) {
            this.mTrustedConnectionAllowed = allowed;
            checkStateAndEnable();
        }
    }

    public void setUntrustedConnectionAllowed(boolean allowed) {
        localLog("setUntrustedConnectionAllowed: allowed=" + allowed);
        if (this.mUntrustedConnectionAllowed != allowed) {
            this.mUntrustedConnectionAllowed = allowed;
            startConnectivityScan(true, false);
        }
    }

    public void setSpecificNetworkRequestInProgress(boolean inProgress) {
        localLog("setsetSpecificNetworkRequestInProgress : inProgress=" + inProgress);
        if (this.mSpecificNetworkRequestInProgress != inProgress) {
            this.mSpecificNetworkRequestInProgress = inProgress;
            checkStateAndEnable();
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
        if (enable) {
            return this.mBssidBlacklist.remove(bssid) != null;
        }
        if (!this.mStateMachine.isConnected() || getScanResultsHasSameSsid(this.mScanner, bssid).size() < 2) {
            return false;
        }
        if (this.mWifiLastResortWatchdog.shouldIgnoreBssidUpdate(bssid)) {
            localLog("Ignore update Bssid Blacklist since Watchdog trigger is activated");
            return false;
        }
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

    public boolean trackBssid(String bssid, boolean enable, int reasonCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("trackBssid: ");
        sb.append(enable ? "enable " : "disable ");
        sb.append(StringUtilEx.safeDisplayBssid(bssid));
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
            if (!this.mConnectivityHelper.setFirmwareRoamingConfiguration(blacklistedBssids, new ArrayList<>())) {
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

    private void retrieveWifiScanner() {
        if (this.mScanner == null) {
            this.mScanner = this.mWifiInjector.getWifiScanner();
            Preconditions.checkNotNull(this.mScanner);
            this.mScanner.registerScanListener(this.mAllSingleScanListener);
        }
    }

    private void clearBssidBlacklist() {
        this.mBssidBlacklist.clear();
        updateFirmwareRoamingConfiguration();
    }

    private void start() {
        if (!this.mRunning) {
            retrieveWifiScanner();
            this.mConnectivityHelper.getFirmwareRoamingInfo();
            clearBssidBlacklist();
            this.mRunning = true;
        }
    }

    private void stop() {
        if (this.mRunning) {
            this.mRunning = false;
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
        Log.i(TAG, "enable wifi auto connect: " + enable);
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
    /* access modifiers changed from: public */
    private String getScanKey(WifiScanner.ScanListener scanListener) {
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
            HwScanLocalLog.localLog(this.mLocalLog, scanKey, eventKey, log, params);
        }
    }

    /* access modifiers changed from: protected */
    public AlarmManager getAlarmManager() {
        return this.mAlarmManager;
    }

    /* access modifiers changed from: protected */
    public Handler getEventHandler() {
        return this.mEventHandler;
    }

    /* access modifiers changed from: protected */
    public boolean isScreenOn() {
        return this.mScreenOn;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiEnabled() {
        return this.mWifiEnabled;
    }

    /* access modifiers changed from: protected */
    public void notifyScanGenieEvent(int eventId) {
    }
}
