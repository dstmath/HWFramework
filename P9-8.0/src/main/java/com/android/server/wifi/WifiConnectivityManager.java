package com.android.server.wifi;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.PnoSettings;
import android.net.wifi.WifiScanner.PnoSettings.PnoNetwork;
import android.net.wifi.WifiScanner.ScanData;
import android.net.wifi.WifiScanner.ScanListener;
import android.net.wifi.WifiScanner.ScanSettings;
import android.net.wifi.WifiScanner.ScanSettings.HiddenNetwork;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.hotspot2.ANQPData;
import com.android.server.wifi.hotspot2.PasspointNetworkEvaluator;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.StringUtil;
import huawei.android.app.admin.HwDevicePolicyManagerEx;
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

public class WifiConnectivityManager extends AbsWifiConnectivityManager {
    public static final int BSSID_BLACKLIST_EXPIRE_TIME_MS = 300000;
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
    public static final int MAX_PERIODIC_SCAN_INTERVAL_MS = 160000;
    public static final int MAX_PNO_SCAN_RESTART_ALLOWED = 2;
    public static final int MAX_RX_PACKET_FOR_FULL_SCANS = 16;
    public static final int MAX_SCAN_RESTART_ALLOWED = 5;
    public static final int MAX_TX_PACKET_FOR_FULL_SCANS = 8;
    private static final int PASSPOINT_NETWORK_EVALUATOR_PRIORITY = 2;
    public static final int PERIODIC_SCAN_INTERVAL_MS = 20000;
    public static final String PERIODIC_SCAN_TIMER_TAG = "WifiConnectivityManager Schedule Periodic Scan Timer";
    private static final String POLICY_AUTO_CONNECT = "policy-auto-connect";
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
    private static final String TAG = "WifiConnectivityManager";
    private static final int VALID_ROAM_BLACK_LIST_MIN_SSID_NUM = 2;
    private static final String VALUE_DISABLE = "value_disable";
    private static final int WATCHDOG_INTERVAL_MS = 1200000;
    public static final String WATCHDOG_TIMER_TAG = "WifiConnectivityManager Schedule Watchdog Timer";
    public static final int WIFI_STATE_CONNECTED = 1;
    public static final int WIFI_STATE_DISCONNECTED = 2;
    public static final int WIFI_STATE_TRANSITIONING = 3;
    public static final int WIFI_STATE_UNKNOWN = 0;
    private final AlarmManager mAlarmManager;
    private final AllSingleScanListener mAllSingleScanListener = new AllSingleScanListener(this, null);
    private int mBand5GHzBonus;
    private Map<String, BssidBlacklistStatus> mBssidBlacklist = new HashMap();
    private final Clock mClock;
    private final WifiConfigManager mConfigManager;
    private final LinkedList<Long> mConnectionAttemptTimeStamps;
    private final WifiConnectivityHelper mConnectivityHelper;
    private int mCurrentConnectionBonus;
    private boolean mDbg = false;
    private boolean mEnableAutoJoinWhenAssociated;
    private final Handler mEventHandler;
    private final OnAlarmListener mHourPeriodicScanTimerListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.startHourPeriodicSingleScan();
        }
    };
    private boolean mHourPeriodicScanTimerSet = false;
    private int mInitialScoreMax;
    private String mLastConnectionAttemptBssid = null;
    private long mLastHourPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private long mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    private final LocalLog mLocalLog;
    private int mMin24GHzRssi;
    private int mMin5GHzRssi;
    private final WifiNetworkSelector mNetworkSelector;
    private final Set<String> mOldSsidList = new HashSet();
    private final OnAlarmListener mPeriodicScanTimerListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.periodicScanTimerHandler();
        }
    };
    private boolean mPeriodicScanTimerSet = false;
    private int mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
    private final PnoScanListener mPnoScanListener = new PnoScanListener(this, null);
    private int mPnoScanRestartCount = 0;
    private boolean mPnoScanStarted = false;
    private final OnAlarmListener mRestartScanListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.startConnectivityScan(true, true);
        }
    };
    private int mSameNetworkBonus;
    private int mScanRestartCount = 0;
    private final WifiScanner mScanner;
    private boolean mScreenOn = false;
    private int mSecureBonus;
    private int mSingleScanRestartCount = 0;
    private boolean mSingleScanStarted = false;
    private final WifiStateMachine mStateMachine;
    private int mTotalConnectivityAttemptsRateLimited = 0;
    private boolean mUntrustedConnectionAllowed = false;
    private boolean mWaitForFullBandScanResults = false;
    private final OnAlarmListener mWatchdogListener = new OnAlarmListener() {
        public void onAlarm() {
            WifiConnectivityManager.this.watchdogHandler();
        }
    };
    private boolean mWifiConnectivityManagerEnabled = true;
    private boolean mWifiEnabled = false;
    private final WifiInfo mWifiInfo;
    private final WifiLastResortWatchdog mWifiLastResortWatchdog;
    private final WifiMetrics mWifiMetrics;
    private HwWifiStatStore mWifiStatStore;
    protected int mWifiState = 0;

    private class AllSingleScanListener implements ScanListener {
        private boolean mNeedLog;
        private List<ScanDetail> mScanDetails;

        /* synthetic */ AllSingleScanListener(WifiConnectivityManager this$0, AllSingleScanListener -this1) {
            this();
        }

        private AllSingleScanListener() {
            this.mScanDetails = new ArrayList();
            this.mNeedLog = true;
        }

        public void clearScanDetails() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "29", "AllScan clearScanDetails.");
            this.mScanDetails.clear();
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

        public void onResults(ScanData[] results) {
            if (WifiConnectivityManager.this.mWifiEnabled && (WifiConnectivityManager.this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
                if (WifiConnectivityManager.this.mWaitForFullBandScanResults) {
                    if (results[0].isAllChannelsScanned()) {
                        WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
                    } else {
                        WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys, "34", "AllScan waiting for full band scan results.");
                        clearScanDetails();
                        return;
                    }
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
                    }
                }
                if (WifiConnectivityManager.this.mScreenOn && WifiConnectivityManager.this.mWifiState != 1 && (wasConnectAttempted ^ 1) != 0 && WifiConnectivityManager.this.isWifiScanSpecialChannels() && WifiConnectivityManager.this.mSingleScanStarted) {
                    Log.w(WifiConnectivityManager.TAG, "*******wifi scan special channels, but no connect ap ,  force fullband scan ****");
                    WifiConnectivityManager.this.handleScanCountChanged(0);
                    WifiConnectivityManager.this.startSingleScan(true);
                    WifiConnectivityManager.this.mSingleScanStarted = false;
                }
                WifiConnectivityManager.this.mScanRestartCount = 0;
                return;
            }
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys, "33", "AllScan onResults returned mWifiEnabled:%s, mWifiConnectivityManagerEnabled:%s", Boolean.valueOf(WifiConnectivityManager.this.mWifiEnabled), Boolean.valueOf(WifiConnectivityManager.this.mWifiConnectivityManagerEnabled));
            clearScanDetails();
            WifiConnectivityManager.this.mWaitForFullBandScanResults = false;
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (WifiConnectivityManager.this.mWifiEnabled && (WifiConnectivityManager.this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
                this.mNeedLog = true;
                if (WifiConnectivityManager.this.mDbg) {
                    WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.mScanner.mCurrentScanKeys + "AllSingleScanListener onFullResult: " + fullScanResult.SSID + " capabilities " + fullScanResult.capabilities);
                }
                this.mScanDetails.add(ScanResultUtil.toScanDetail(fullScanResult));
                WifiConnectivityManager.this.mScanRestartCount = 0;
                return;
            }
            if (this.mNeedLog) {
                WifiConnectivityManager.this.localLog("Key#00:", "36", "AllScan onFullResult returned mWifiEnabled:%s, mWifiConnectivityManagerEnabled:%s", Boolean.valueOf(WifiConnectivityManager.this.mWifiEnabled), Boolean.valueOf(WifiConnectivityManager.this.mWifiConnectivityManagerEnabled));
            }
            this.mNeedLog = false;
        }
    }

    private static class BssidBlacklistStatus {
        public long blacklistedTimeStamp;
        public int counter;
        public boolean isBlacklisted;

        /* synthetic */ BssidBlacklistStatus(BssidBlacklistStatus -this0) {
            this();
        }

        private BssidBlacklistStatus() {
            this.blacklistedTimeStamp = WifiConnectivityManager.RESET_TIME_STAMP;
        }
    }

    private class OnSavedNetworkUpdateListener implements com.android.server.wifi.WifiConfigManager.OnSavedNetworkUpdateListener {
        /* synthetic */ OnSavedNetworkUpdateListener(WifiConnectivityManager this$0, OnSavedNetworkUpdateListener -this1) {
            this();
        }

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
            updatePnoScan();
        }

        public void onSavedNetworkTemporarilyDisabled(int networkId) {
            WifiConnectivityManager.this.mConnectivityHelper.removeNetworkIfCurrent(networkId);
        }

        public void onSavedNetworkPermanentlyDisabled(int networkId) {
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

    private class PnoScanListener implements android.net.wifi.WifiScanner.PnoScanListener {
        private int mLowRssiNetworkRetryDelay;
        private List<ScanDetail> mScanDetails;

        /* synthetic */ PnoScanListener(WifiConnectivityManager this$0, PnoScanListener -this1) {
            this();
        }

        private PnoScanListener() {
            this.mScanDetails = new ArrayList();
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

        public int getLowRssiNetworkRetryDelay() {
            return this.mLowRssiNetworkRetryDelay;
        }

        public void onSuccess() {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "41", "PnoScanListener onSuccess");
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "42", "PnoScanListener onFailure: reason: %s, description:%s", Integer.valueOf(reason), description);
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get5 = wifiConnectivityManager.mScanRestartCount;
            wifiConnectivityManager.mScanRestartCount = -get5 + 1;
            if (-get5 < 5) {
                WifiConnectivityManager.this.scheduleDelayedConnectivityScan(WifiConnectivityManager.RESTART_SCAN_DELAY_MS);
                return;
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start PNO scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "43", "PnoScanListener onPeriodChanged: actual scan period ms", Integer.valueOf(periodInMs));
        }

        public void onResults(ScanData[] results) {
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPnoNetworkFound(ScanResult[] results) {
            int -get3;
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "44", "PnoScanListener: onPnoNetworkFound: results len = %s", Integer.valueOf(results.length));
            for (ScanResult result : results) {
                this.mScanDetails.add(ScanResultUtil.toScanDetail(result));
            }
            boolean wasConnectAttempted = WifiConnectivityManager.this.handleScanResults(this.mScanDetails, "PnoScanListener", WifiConnectivityManager.this.getScanKey(this));
            clearScanDetails();
            WifiConnectivityManager.this.mScanRestartCount = 0;
            if (wasConnectAttempted) {
                resetLowRssiNetworkRetryDelay();
            } else {
                if (this.mLowRssiNetworkRetryDelay > WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS) {
                    this.mLowRssiNetworkRetryDelay = WifiConnectivityManager.LOW_RSSI_NETWORK_RETRY_MAX_DELAY_MS;
                }
                WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
                -get3 = wifiConnectivityManager.mPnoScanRestartCount;
                wifiConnectivityManager.mPnoScanRestartCount = -get3 + 1;
                if (-get3 < 2) {
                    WifiConnectivityManager.this.scheduleDelayedConnectivityScan(this.mLowRssiNetworkRetryDelay);
                }
                this.mLowRssiNetworkRetryDelay *= 2;
            }
            WifiConnectivityManager.this.mScanRestartCount = 0;
        }
    }

    private class RestartSingleScanListener implements OnAlarmListener {
        private final boolean mIsFullBandScan;

        RestartSingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onAlarm() {
            WifiConnectivityManager.this.startSingleScan(this.mIsFullBandScan);
        }
    }

    private class SingleScanListener implements ScanListener {
        private final boolean mIsFullBandScan;

        SingleScanListener(boolean isFullBandScan) {
            this.mIsFullBandScan = isFullBandScan;
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "37", "SingleScanListener onFailure: reason:%s description:%s SingleScanRestartCount:%s", Integer.valueOf(reason), description, Integer.valueOf(WifiConnectivityManager.this.mSingleScanRestartCount));
            WifiConnectivityManager wifiConnectivityManager = WifiConnectivityManager.this;
            int -get8 = wifiConnectivityManager.mSingleScanRestartCount;
            wifiConnectivityManager.mSingleScanRestartCount = -get8 + 1;
            if (-get8 < 5) {
                WifiConnectivityManager.this.scheduleDelayedSingleScan(this.mIsFullBandScan);
                return;
            }
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
            WifiConnectivityManager.this.localLog("Failed to successfully start single scan for 5 times");
        }

        public void onPeriodChanged(int periodInMs) {
            WifiConnectivityManager.this.localLog(WifiConnectivityManager.this.getScanKey(this), "38", "SingleScanListener onPeriodChanged: actual scan period %s ms", Integer.valueOf(periodInMs));
        }

        public void onResults(ScanData[] results) {
            WifiConnectivityManager.this.mSingleScanStarted = true;
        }

        public void onFullResult(ScanResult fullScanResult) {
            if (WifiConnectivityManager.this.mDbg) {
                WifiConnectivityManager.this.localLog("SingleScanListener onFullResult: " + fullScanResult.SSID + " capabilities " + fullScanResult.capabilities);
            }
            WifiConnectivityManager.this.mSingleScanRestartCount = 0;
        }
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    private boolean handleScanResults(List<ScanDetail> scanDetails, String listenerName, String keys) {
        refreshBssidBlacklist();
        if (this.mStateMachine.isLinkDebouncing() || this.mStateMachine.isSupplicantTransientState()) {
            Log.i("WifiScanLog", keys + listenerName + " onResults: No network selection because linkDebouncing is " + this.mStateMachine.isLinkDebouncing() + " and supplicantTransient is " + this.mStateMachine.isSupplicantTransientState());
            return false;
        }
        localLog(keys, "27", " onResults: start network selection");
        this.mNetworkSelector.mCurrentScanKeys = keys;
        WifiConfiguration candidate = this.mNetworkSelector.selectNetwork(scanDetails, buildBssidBlacklist(), this.mWifiInfo, this.mStateMachine.isConnected(), this.mStateMachine.isDisconnected(), this.mUntrustedConnectionAllowed);
        this.mWifiLastResortWatchdog.updateAvailableNetworks(this.mNetworkSelector.getFilteredScanDetails());
        this.mWifiMetrics.countScanResults(scanDetails);
        if (candidate == null) {
            return false;
        }
        localLog(keys, "28", "WNS selectNetwork candidate-%s", candidate.SSID);
        connectToNetwork(candidate, keys);
        return true;
    }

    WifiConnectivityManager(Context context, WifiStateMachine stateMachine, WifiScanner scanner, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog, boolean enable, FrameworkFacade frameworkFacade, SavedNetworkEvaluator savedNetworkEvaluator, ScoredNetworkEvaluator scoredNetworkEvaluator, PasspointNetworkEvaluator passpointNetworkEvaluator) {
        this.mStateMachine = stateMachine;
        this.mScanner = scanner;
        this.mConfigManager = configManager;
        this.mWifiInfo = wifiInfo;
        this.mNetworkSelector = networkSelector;
        this.mNetworkSelector.mConnectivityHelper = connectivityHelper;
        this.mConnectivityHelper = connectivityHelper;
        this.mLocalLog = localLog;
        this.mWifiLastResortWatchdog = wifiLastResortWatchdog;
        this.mWifiMetrics = wifiMetrics;
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mEventHandler = new Handler(looper);
        this.mClock = clock;
        this.mConnectionAttemptTimeStamps = new LinkedList();
        this.mMin5GHzRssi = context.getResources().getInteger(17694891);
        this.mMin24GHzRssi = context.getResources().getInteger(17694890);
        this.mBand5GHzBonus = context.getResources().getInteger(17694865);
        this.mCurrentConnectionBonus = context.getResources().getInteger(17694882);
        this.mSameNetworkBonus = context.getResources().getInteger(17694872);
        this.mSecureBonus = context.getResources().getInteger(17694873);
        int thresholdSaturatedRssi24 = context.getResources().getInteger(17694894);
        this.mEnableAutoJoinWhenAssociated = context.getResources().getBoolean(17957058);
        this.mInitialScoreMax = (context.getResources().getInteger(17694894) + context.getResources().getInteger(17694870)) * context.getResources().getInteger(17694871);
        localLog("PNO settings: min5GHzRssi " + this.mMin5GHzRssi + " min24GHzRssi " + this.mMin24GHzRssi + " currentConnectionBonus " + this.mCurrentConnectionBonus + " sameNetworkBonus " + this.mSameNetworkBonus + " secureNetworkBonus " + this.mSecureBonus + " initialScoreMax " + this.mInitialScoreMax);
        boolean hs2Enabled = context.getResources().getBoolean(17957059);
        localLog("Passpoint is: " + (hs2Enabled ? "enabled" : "disabled"));
        this.mNetworkSelector.registerNetworkEvaluator(savedNetworkEvaluator, 1);
        if (hs2Enabled) {
            this.mNetworkSelector.registerNetworkEvaluator(passpointNetworkEvaluator, 2);
        }
        this.mNetworkSelector.registerNetworkEvaluator(scoredNetworkEvaluator, 3);
        this.mScanner.registerScanListener(this.mAllSingleScanListener);
        this.mConfigManager.setOnSavedNetworkUpdateListener(new OnSavedNetworkUpdateListener(this, null));
        this.mWifiConnectivityManagerEnabled = enable;
        localLog("ConnectivityScanManager initialized and " + (enable ? "enabled" : "disabled"));
        this.mWifiStatStore = HwWifiServiceFactory.getHwWifiStatStore();
    }

    private boolean shouldSkipConnectionAttempt(Long timeMillis) {
        Iterator<Long> attemptIter = this.mConnectionAttemptTimeStamps.iterator();
        while (attemptIter.hasNext()) {
            if (timeMillis.longValue() - ((Long) attemptIter.next()).longValue() <= 240000) {
                break;
            }
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
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        if (scanResultCandidate == null) {
            Log.i("WifiScanLog", keys + "connectToNetwork: bad candidate - " + candidate + " scanResult: " + scanResultCandidate);
            return;
        }
        String targetBssid = scanResultCandidate.BSSID;
        String targetAssociationId = candidate.SSID + " : " + targetBssid;
        if (targetBssid != null && ((targetBssid.equals(this.mLastConnectionAttemptBssid) || targetBssid.equals(this.mWifiInfo.getBSSID())) && SupplicantState.isConnecting(this.mWifiInfo.getSupplicantState()))) {
            Log.i("WifiScanLog", keys + "connectToNetwork: Either already connected " + "or is connecting to " + targetAssociationId);
        } else if (candidate.BSSID == null || (candidate.BSSID.equals("any") ^ 1) == 0 || (candidate.BSSID.equals(targetBssid) ^ 1) == 0) {
            Long elapsedTimeMillis = Long.valueOf(this.mClock.getElapsedSinceBootMillis());
            if (this.mScreenOn || !shouldSkipConnectionAttempt(elapsedTimeMillis)) {
                String currentAssociationId;
                noteConnectionAttempt(elapsedTimeMillis);
                this.mLastConnectionAttemptBssid = targetBssid;
                WifiConfiguration currentConnectedNetwork = this.mConfigManager.getConfiguredNetwork(this.mWifiInfo.getNetworkId());
                if (currentConnectedNetwork == null) {
                    currentAssociationId = "Disconnected";
                } else {
                    currentAssociationId = this.mWifiInfo.getSSID() + " : " + this.mWifiInfo.getBSSID();
                }
                this.mStateMachine.setCHRConnectingSartTimestamp(elapsedTimeMillis.longValue());
                if (currentConnectedNetwork == null || currentConnectedNetwork.networkId != candidate.networkId) {
                    Bundle bundle = new HwDevicePolicyManagerEx().getPolicy(null, POLICY_AUTO_CONNECT);
                    if (bundle == null || !bundle.getBoolean(VALUE_DISABLE)) {
                        if (this.mConnectivityHelper.isFirmwareRoamingSupported() && (candidate.BSSID == null || candidate.BSSID.equals("any"))) {
                            targetBssid = "any";
                            localLog("connectToNetwork: Connect to " + candidate.SSID + ":" + targetBssid + " from " + currentAssociationId);
                        } else {
                            localLog("connectToNetwork: Connect to " + targetAssociationId + " from " + currentAssociationId);
                        }
                        if (this.mStateMachine.isScanAndManualConnectMode()) {
                            Log.d(TAG, "Only allow Manual Connection, ignore auto connection.");
                            return;
                        }
                        localLog(keys, "46", "WifiStateMachine startConnectToNetwork");
                        if (this.mWifiStatStore != null) {
                            this.mWifiStatStore.setWifiConnectType("AUTO_CONNECT");
                        }
                        this.mStateMachine.startConnectToNetwork(candidate.networkId, unselectDhcpFailedBssid(targetBssid, scanResultCandidate.BSSID, candidate));
                    } else {
                        Log.w(TAG, "connectToNetwork: MDM deny auto connect!");
                        return;
                    }
                } else if (this.mConnectivityHelper.isFirmwareRoamingSupported()) {
                    Log.i("WifiScanLog", keys + "connectToNetwork: Roaming candidate - " + targetAssociationId + "." + " The actual roaming target is up to the firmware.");
                } else if (this.mStateMachine.isWifiRepeaterStarted()) {
                    Log.i("WifiScanLog", keys + "WifiRepeater is started, do not allow auto roam.");
                } else {
                    localLog(keys, "45", "connectToNetwork: Roaming to %s  from %s!", targetAssociationId, currentAssociationId);
                    this.mStateMachine.startRoamToNetwork(candidate.networkId, scanResultCandidate);
                }
                return;
            }
            Log.i("WifiScanLog", keys + "connectToNetwork: Too many connection attempts. Skipping this attempt!");
            this.mTotalConnectivityAttemptsRateLimited++;
        } else {
            Log.i("WifiScanLog", keys + "connecToNetwork: target BSSID " + targetBssid + " does not match the " + "config specified BSSID " + candidate.BSSID + ". Drop it!");
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

    private boolean setScanChannels(ScanSettings settings) {
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
        settings.channels = new ChannelSpec[freqs.size()];
        for (Integer freq : freqs) {
            int index2 = index + 1;
            settings.channels[index] = new ChannelSpec(freq.intValue());
            index = index2;
        }
        return true;
    }

    private void watchdogHandler() {
        localLog("watchdogHandler");
        Log.i("WifiScanLog", "start a single scan from watchdogHandler ? " + (this.mWifiState == 2));
        if (this.mWifiState == 2) {
            localLog("start a single scan from watchdogHandler");
            scheduleWatchdogTimer();
            startSingleScan(true);
        }
    }

    private void startPeriodicSingleScan() {
        long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
        localLog("****start Periodic SingleScan,mPeriodicSingleScanInterval : " + (this.mPeriodicSingleScanInterval / 1000) + " s");
        if (!(this.mLastPeriodicSingleScanTimeStamp == RESET_TIME_STAMP || (handleForceScan() ^ 1) == 0)) {
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
        boolean isFullBandScan = true;
        if (this.mWifiState == 1 && (this.mWifiInfo.txSuccessRate > 8.0d || this.mWifiInfo.rxSuccessRate > 16.0d)) {
            localLog("No full band scan due to ongoing traffic");
            isFullBandScan = false;
        }
        this.mLastPeriodicSingleScanTimeStamp = currentTimeStamp;
        handleScanCountChanged(0);
        if (isScanThisPeriod(this.mStateMachine.isP2pConnected())) {
            startSingleScan(isFullBandScan);
        }
        this.mPeriodicSingleScanInterval = getPeriodicSingleScanInterval();
        schedulePeriodicScanTimer(this.mPeriodicSingleScanInterval);
    }

    private void resetLastPeriodicSingleScanTimeStamp() {
        this.mLastPeriodicSingleScanTimeStamp = RESET_TIME_STAMP;
    }

    private void periodicScanTimerHandler() {
        Log.i(TAG, "periodicScanTimerHandler mScreenOn " + this.mScreenOn);
        localLog("periodicScanTimerHandler");
        if (this.mScreenOn) {
            startPeriodicSingleScan();
        }
    }

    private void startHourPeriodicSingleScan() {
        localLog("startHourPeriodicSingleScan: screenOn=" + this.mScreenOn + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mScreenOn && this.mWifiEnabled && (this.mWifiConnectivityManagerEnabled ^ 1) != 0) {
            long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
            if (this.mLastHourPeriodicSingleScanTimeStamp != RESET_TIME_STAMP) {
                long msSinceLastScan = currentTimeStamp - this.mLastHourPeriodicSingleScanTimeStamp;
                if (msSinceLastScan < ANQPData.DATA_LIFETIME_MILLISECONDS) {
                    localLog("Last hour periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                    scheduleHourPeriodicScanTimer(3600000 - ((int) msSinceLastScan));
                    return;
                }
            }
            startScan(true);
            this.mLastHourPeriodicSingleScanTimeStamp = currentTimeStamp;
            scheduleHourPeriodicScanTimer(3600000);
        }
    }

    private void scheduleHourPeriodicScanTimer(int intervalMs) {
        this.mAlarmManager.setExact(3, this.mClock.getElapsedSinceBootMillis() + ((long) intervalMs), HOUR_PERIODIC_SCAN_TIMER_TAG, this.mHourPeriodicScanTimerListener, this.mEventHandler);
        this.mHourPeriodicScanTimerSet = true;
    }

    private void stopHourPeriodicSingleScan() {
        if (this.mHourPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mHourPeriodicScanTimerListener);
            this.mHourPeriodicScanTimerSet = false;
        }
    }

    private void startSingleScan(boolean isFullBandScan) {
        if (this.mWifiEnabled && (this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
            startScan(isFullBandScan);
        }
    }

    private void startScan(boolean isFullBandScan) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        ScanSettings settings = null;
        if (!isWifiScanSpecialChannels() || this.mWifiState == 1) {
            Log.e(TAG, "isWifiScanSpecialChannels is false,mWifiState : " + this.mWifiState);
        } else {
            settings = getScanGenieSettings();
            localLog("****isWifiScanSpecialChannels *settings =**:" + settings);
        }
        if (settings == null) {
            settings = new ScanSettings();
            if (!(isFullBandScan || setScanChannels(settings))) {
                isFullBandScan = true;
            }
            settings.band = getScanBand(isFullBandScan);
            settings.reportEvents = 3;
            settings.numBssidsPerScan = 0;
        }
        List<HiddenNetwork> hiddenNetworkList = this.mConfigManager.retrieveHiddenNetworkList();
        settings.hiddenNetworks = (HiddenNetwork[]) hiddenNetworkList.toArray(new HiddenNetwork[hiddenNetworkList.size()]);
        SingleScanListener singleScanListener = new SingleScanListener(isFullBandScan);
        if (settings.channels != null) {
            for (int i = 0; i < settings.channels.length; i++) {
                localLog("settings  channels frequency: " + settings.channels[i].frequency + ", dwellTimeMS: " + settings.channels[i].dwellTimeMS + ", passive :" + settings.channels[i].passive);
            }
        }
        this.mScanner.startScan(settings, singleScanListener, WifiStateMachine.WIFI_WORK_SOURCE);
    }

    private void startPeriodicScan(boolean scanImmediately) {
        this.mPnoScanListener.resetLowRssiNetworkRetryDelay();
        if (this.mWifiState != 1 || (this.mEnableAutoJoinWhenAssociated ^ 1) == 0) {
            if (scanImmediately) {
                resetLastPeriodicSingleScanTimeStamp();
            }
            this.mPeriodicSingleScanInterval = PERIODIC_SCAN_INTERVAL_MS;
            resetPeriodicSingleScanInterval();
            startPeriodicSingleScan();
        }
    }

    private void startDisconnectedPnoScan() {
        PnoSettings pnoSettings = new PnoSettings();
        List<PnoNetwork> pnoNetworkList = this.mConfigManager.retrievePnoNetworkList();
        int listSize = pnoNetworkList.size();
        if (listSize == 0) {
            localLog("No saved network for starting disconnected PNO.");
            return;
        }
        pnoSettings.networkList = new PnoNetwork[listSize];
        pnoSettings.networkList = (PnoNetwork[]) pnoNetworkList.toArray(pnoSettings.networkList);
        pnoSettings.min5GHzRssi = this.mMin5GHzRssi;
        pnoSettings.min24GHzRssi = this.mMin24GHzRssi;
        pnoSettings.initialScoreMax = this.mInitialScoreMax;
        pnoSettings.currentConnectionBonus = this.mCurrentConnectionBonus;
        pnoSettings.sameNetworkBonus = this.mSameNetworkBonus;
        pnoSettings.secureBonus = this.mSecureBonus;
        pnoSettings.band5GHzBonus = this.mBand5GHzBonus;
        ScanSettings scanSettings = new ScanSettings();
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
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 1200000, WATCHDOG_TIMER_TAG, this.mWatchdogListener, this.mEventHandler);
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

    private void scheduleDelayedSingleScan(boolean isFullBandScan) {
        localLog("scheduleDelayedSingleScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 2000, RESTART_SINGLE_SCAN_TIMER_TAG, new RestartSingleScanListener(isFullBandScan), this.mEventHandler);
    }

    private void scheduleDelayedConnectivityScan(int msFromNow) {
        localLog("scheduleDelayedConnectivityScan");
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + ((long) msFromNow), RESTART_CONNECTIVITY_SCAN_TIMER_TAG, this.mRestartScanListener, this.mEventHandler);
    }

    protected void startConnectivityScan(boolean scanImmediately, boolean isRestartScan) {
        localLog("startConnectivityScan: screenOn=" + this.mScreenOn + " wifiState=" + stateToString(this.mWifiState) + " scanImmediately=" + scanImmediately + " wifiEnabled=" + this.mWifiEnabled + " wifiConnectivityManagerEnabled=" + this.mWifiConnectivityManagerEnabled);
        if (this.mWifiEnabled && (this.mWifiConnectivityManagerEnabled ^ 1) == 0) {
            stopConnectivityScan(isRestartScan);
            if (this.mWifiState == 1 || this.mWifiState == 2) {
                if (this.mScreenOn) {
                    startPeriodicScan(scanImmediately);
                } else if (this.mWifiState == 2 && (this.mPnoScanStarted ^ 1) != 0) {
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

    public void forceConnectivityScan() {
        localLog("forceConnectivityScan");
        this.mWaitForFullBandScanResults = true;
        startSingleScan(true);
    }

    private boolean updateBssidBlacklist(String bssid, boolean enable, int reasonCode) {
        boolean z = true;
        if (enable) {
            if (this.mBssidBlacklist.remove(bssid) == null) {
                z = false;
            }
            return z;
        } else if (!this.mStateMachine.isConnected() || getScanResultsHasSameSsid(bssid).size() < 2) {
            return false;
        } else {
            BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
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
        localLog("trackBssid: " + (enable ? "enable " : "disable ") + StringUtil.safeDisplayBssid(bssid) + " reason code " + reasonCode);
        if (bssid == null || !updateBssidBlacklist(bssid, enable, reasonCode)) {
            return false;
        }
        updateFirmwareRoamingConfiguration();
        if (!enable) {
            startConnectivityScan(true, false);
        }
        return true;
    }

    public boolean isBssidDisabled(String bssid) {
        BssidBlacklistStatus status = (BssidBlacklistStatus) this.mBssidBlacklist.get(bssid);
        return status == null ? false : status.isBlacklisted;
    }

    private HashSet<String> buildBssidBlacklist() {
        HashSet<String> blacklistedBssids = new HashSet();
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
            ArrayList<String> blacklistedBssids = new ArrayList(buildBssidBlacklist());
            int blacklistSize = blacklistedBssids.size();
            if (blacklistSize > maxBlacklistSize) {
                Log.wtf(TAG, "Attempt to write " + blacklistSize + " blacklisted BSSIDs, max size is " + maxBlacklistSize);
                ArrayList<String> blacklistedBssids2 = new ArrayList(blacklistedBssids.subList(0, maxBlacklistSize));
                localLog("Trim down BSSID blacklist size from " + blacklistSize + " to " + blacklistedBssids2.size());
                blacklistedBssids = blacklistedBssids2;
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
                BssidBlacklistStatus status = (BssidBlacklistStatus) iter.next();
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
        Iterable scanResults = null;
        if (this.mScanner != null) {
            scanResults = this.mScanner.getSingleScanResults();
        }
        if (scanResults.isEmpty()) {
            Log.d(TAG, "getScanResultsHasSameSsid: WifiStateMachine.ScanResultsList is empty.");
            return scanResults;
        }
        Object ssid = null;
        for (ScanResult result : scanResults) {
            if (bssid.equals(result.BSSID)) {
                ssid = result.SSID;
                break;
            }
        }
        if (TextUtils.isEmpty(ssid)) {
            Log.d(TAG, "getScanResultsHasSameSsid: can't find the corresponding ssid with the given bssid.");
            return scanResults;
        }
        List<ScanResult> sameSsidList = new ArrayList();
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
        this.mLastConnectionAttemptBssid = null;
        this.mWaitForFullBandScanResults = false;
    }

    private void updateRunningState() {
        if (this.mWifiEnabled && this.mWifiConnectivityManagerEnabled) {
            localLog("Starting up WifiConnectivityManager");
            start();
            return;
        }
        localLog("Stopping WifiConnectivityManager");
        stop();
    }

    public void setWifiEnabled(boolean enable) {
        localLog("Set WiFi " + (enable ? "enabled" : "disabled"));
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
        localLog("Set WiFiConnectivityManager " + (enable ? "enabled" : "disabled"));
        this.mWifiConnectivityManagerEnabled = enable;
        updateRunningState();
        if (enable) {
            stopHourPeriodicSingleScan();
        } else {
            startHourPeriodicSingleScan();
        }
    }

    int getLowRssiNetworkRetryDelay() {
        return this.mPnoScanListener.getLowRssiNetworkRetryDelay();
    }

    long getLastPeriodicSingleScanTimeStamp() {
        return this.mLastPeriodicSingleScanTimeStamp;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiConnectivityManager");
        pw.println("WifiConnectivityManager - Log Begin ----");
        this.mLocalLog.dump(fd, pw, args);
        pw.println("WifiConnectivityManager - Log End ----");
    }

    private String getScanKey(ScanListener scanListener) {
        return "Key#" + this.mScanner.getListenerKey(this) + ":";
    }

    void localLog(String scanKey, String eventKey, String log) {
        localLog(scanKey, eventKey, log, null);
    }

    void localLog(String scanKey, String eventKey, String log, Object... params) {
        if (!"Key#0:".equals(scanKey)) {
            WifiConnectivityHelper.localLog(this.mLocalLog, scanKey, eventKey, log, params);
        }
    }
}
