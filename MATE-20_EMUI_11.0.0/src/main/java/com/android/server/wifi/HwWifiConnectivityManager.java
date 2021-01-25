package com.android.server.wifi;

import android.app.AlarmManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.ABS.HwABSUtils;
import com.android.server.wifi.MSS.HwMSSUtils;
import com.android.server.wifi.cast.CastOptManager;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class HwWifiConnectivityManager extends WifiConnectivityManager {
    private static final int DEFAULT_SCAN_PERIOD_SKIP_COUNTER = 4;
    private static final int DEFAULT_SCAN_PERIOD_SKIP_COUNTER_FOR_CLONE = 50;
    private static final String FULL_BAND_SCAN = "FullBand";
    private static final int HW_MAX_PERIODIC_SCAN_INTERVAL_MS = 60000;
    private static final int HW_MAX_STATIONARY_PERIODIC_SCAN_INTERVAL_MS = 300000;
    private static final int HW_MID_PERIODIC_SCAN_INTERVAL_MS = 30000;
    private static final int HW_MIX_PERIODIC_SCAN_INTERVAL_MS = 10000;
    private static final int HW_NAVIGATION_PERIODIC_SCAN_INTERVAL_MS = 120000;
    private static final String PG_AR_STATE_ACTION = "com.huawei.intent.action.PG_AR_STATE_ACTION";
    private static final String PG_RECEIVER_PERMISSION = "com.huawei.powergenie.receiverPermission";
    private static final int SCAN_COUNT_CHANGE_REASON_ADD = 0;
    private static final int SCAN_COUNT_CHANGE_REASON_MINUS = 1;
    private static final int SCAN_COUNT_CHANGE_REASON_RESET = 2;
    private static final String SPECIFIED_BAND_SCAN = "SpecifiedBand";
    private static final int STATE_GPS = 3;
    private static final String TAG = "HwWifiConnectivityManager";
    private static WifiStateMachineUtils wifiStateMachineUtils = EasyInvokeFactory.getInvokeUtils(WifiStateMachineUtils.class);
    private boolean bExtendWifiScanPeriodForP2p = false;
    private int iScanPeriodSkipTimes = 4;
    private Clock mClock = null;
    private Context mContext;
    private int mExponentialPeriodicSingleScanInterval;
    protected final AlarmManager.OnAlarmListener mHourPeriodicScanTimerListener = new AlarmManager.OnAlarmListener() {
        /* class com.android.server.wifi.HwWifiConnectivityManager.AnonymousClass2 */

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            HwWifiConnectivityManager.this.startHourPeriodicSingleScan();
        }
    };
    private int mHwSingleScanCounter = 0;
    private HwWifiProServiceManager mHwWifiProServiceManager;
    private boolean mIsStationary = false;
    private LocalLog mLocalLog = null;
    private int mPeriodicSingleScanInterval;
    private int mWifiScanPeriodCounter = 0;
    private ClientModeImpl mWifiStateMachine;

    public HwWifiConnectivityManager(Context context, ScoringParams scoringParams, ClientModeImpl stateMachine, WifiInjector injector, WifiConfigManager configManager, WifiInfo wifiInfo, WifiNetworkSelector networkSelector, WifiConnectivityHelper connectivityHelper, WifiLastResortWatchdog wifiLastResortWatchdog, OpenNetworkNotifier openNetworkNotifier, CarrierNetworkNotifier carrierNetworkNotifier, CarrierNetworkConfig carrierNetworkConfig, WifiMetrics wifiMetrics, Looper looper, Clock clock, LocalLog localLog) {
        super(context, scoringParams, stateMachine, injector, configManager, wifiInfo, networkSelector, connectivityHelper, wifiLastResortWatchdog, openNetworkNotifier, carrierNetworkNotifier, carrierNetworkConfig, wifiMetrics, looper, clock, localLog);
        this.mContext = context;
        this.mClock = clock;
        this.mLocalLog = localLog;
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        this.mWifiStateMachine = stateMachine;
        if (PreconfiguredNetworkManager.IS_R1) {
            this.mNetworkSelector.registerNetworkEvaluator(new PreconfiguredNetworkEvaluator(context, configManager));
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.HwWifiConnectivityManager.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    boolean stationary = intent.getBooleanExtra("stationary", false);
                    HwHiLog.d(HwWifiConnectivityManager.TAG, false, "Current stationary=%{public}s, new stationary=%{public}s", new Object[]{String.valueOf(HwWifiConnectivityManager.this.mIsStationary), String.valueOf(stationary)});
                    if (stationary != HwWifiConnectivityManager.this.mIsStationary) {
                        HwWifiConnectivityManager.this.mIsStationary = stationary;
                        if (HwWifiConnectivityManager.this.mWifiStateMachine == null || HwWifiConnectivityManager.wifiStateMachineUtils.getScreenOn(HwWifiConnectivityManager.this.mWifiStateMachine)) {
                            HwWifiConnectivityManager.this.startConnectivityScan(false, false);
                        } else {
                            HwHiLog.d(HwWifiConnectivityManager.TAG, false, "PG_AR_STATE_ACTION ScreenOff do nothing return !", new Object[0]);
                        }
                    }
                }
            }
        }, new IntentFilter(PG_AR_STATE_ACTION), PG_RECEIVER_PERMISSION, null);
        log(false, "HwWifiConnectivityManager init!", new Object[0]);
    }

    public String unselectDhcpFailedBssid(String targetBssid, String scanResultBssid, WifiConfiguration candidate) {
        if (!this.mHwWifiProServiceManager.isHwSelfCureEngineStarted() || candidate == null) {
            return targetBssid;
        }
        if (scanResultBssid != null && this.mHwWifiProServiceManager.isDhcpFailedConfigKey(candidate.configKey())) {
            return scanResultBssid;
        }
        if (scanResultBssid != null && this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted() && this.mHwWifiProServiceManager.isPortalNotifyOn()) {
            return scanResultBssid;
        }
        if (scanResultBssid == null || !this.mHwWifiProServiceManager.isHwAutoConnectManagerStarted() || !this.mHwWifiProServiceManager.isAutoJoinAllowedSetTargetBssid(candidate, scanResultBssid)) {
            return targetBssid;
        }
        return scanResultBssid;
    }

    private boolean isWifiEnabledState() {
        WifiManager wifiManager;
        Context context = this.mContext;
        if (context == null || (wifiManager = (WifiManager) context.getSystemService("wifi")) == null || wifiManager.getWifiState() != 3) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void extendWifiScanPeriodForP2p(boolean bExtend, int iTimes) {
        if (!bExtend || isWifiEnabledState()) {
            this.mWifiScanPeriodCounter = 0;
        } else {
            this.mWifiScanPeriodCounter = -1;
        }
        this.bExtendWifiScanPeriodForP2p = bExtend;
        if (!bExtend || iTimes <= 0) {
            this.iScanPeriodSkipTimes = 4;
            this.mExtendWifiScanPeriodForClone = false;
        } else {
            this.iScanPeriodSkipTimes = iTimes;
        }
        if (iTimes == 50) {
            this.mExtendWifiScanPeriodForClone = true;
        }
        log(false, "extendWifiScanPeriodForP2p: %{public}s, Times = %{public}d", String.valueOf(this.bExtendWifiScanPeriodForP2p), Integer.valueOf(this.iScanPeriodSkipTimes));
    }

    /* access modifiers changed from: protected */
    public boolean isScanThisPeriod(boolean isP2pConn) {
        if (!isP2pConn && !this.mExtendWifiScanPeriodForClone) {
            if (this.mWifiScanPeriodCounter > 0) {
                this.bExtendWifiScanPeriodForP2p = false;
                this.mWifiScanPeriodCounter = 0;
                this.iScanPeriodSkipTimes = 4;
            }
            return true;
        } else if (!this.bExtendWifiScanPeriodForP2p) {
            return true;
        } else {
            if (!isWifiEnabledState()) {
                log(false, "isScanThisPeriod: false for wifi is not enabled", new Object[0]);
                return false;
            }
            this.mWifiScanPeriodCounter++;
            if (this.mExtendWifiScanPeriodForClone) {
                int i = this.mWifiScanPeriodCounter;
                if (i < this.iScanPeriodSkipTimes) {
                    log(false, "isScanThisPeriod: false for clone mWifiScanPeriodCounter is: %{public}d", Integer.valueOf(i));
                    return false;
                }
                this.bExtendWifiScanPeriodForP2p = false;
                this.mWifiScanPeriodCounter = 0;
                this.iScanPeriodSkipTimes = 4;
                this.mExtendWifiScanPeriodForClone = false;
                return true;
            } else if (this.mWifiScanPeriodCounter % this.iScanPeriodSkipTimes == 0) {
                return true;
            } else {
                log(false, "isScanThisPeriod: false", new Object[0]);
                return false;
            }
        }
    }

    private int getScanGeniePeriodicSingleScanInterval() {
        int i = this.mHwSingleScanCounter;
        if (i < 4) {
            this.mPeriodicSingleScanInterval = 10000;
        } else if (i < 7) {
            this.mPeriodicSingleScanInterval = 30000;
        } else {
            if (!checkProduct() || !checkNavigationMode()) {
                this.mPeriodicSingleScanInterval = HW_MAX_PERIODIC_SCAN_INTERVAL_MS;
            } else {
                this.mPeriodicSingleScanInterval = HW_NAVIGATION_PERIODIC_SCAN_INTERVAL_MS;
            }
            if (this.mIsStationary && shouldDisconnectScanControl()) {
                this.mPeriodicSingleScanInterval = 300000;
            }
        }
        log(false, "HwSingleScanCounter: %{public}d, mPeriodicSingleScanInterval : %{public}d ms", Integer.valueOf(this.mHwSingleScanCounter), Integer.valueOf(this.mPeriodicSingleScanInterval));
        return this.mPeriodicSingleScanInterval;
    }

    private boolean checkProduct() {
        ArrayList<String> productList = new ArrayList<>(Arrays.asList("VTR", "VKY", "MHA", "LON", "EML", "CLT", "NEO", "RVL"));
        for (int k = 0; k < productList.size(); k++) {
            if (SystemProperties.get("ro.product.name", "").contains(productList.get(k))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNavigationMode() {
        ArrayList<String> navigationPackages = new ArrayList<>(Arrays.asList("com.autonavi.minimap", "com.baidu.BaiduMap", "com.autonavi.xmgd.navigator", "com.tencent.map", "com.ovital.ovitalMap", "com.google.android.apps.maps", "com.baidu.navi", "cld.navi.mainframe", "com.sogou.map.android.maps", "com.uu.uunavi", "com.sunboxsoft.oilforgdandroid", "com.pdager", "com.itotem.traffic.broadcasts", "com.mapbar.android.mapbarmap", "com.autonavi.xmgd.navigator.toc", "cn.com.tiros.android.navidog", "com.autonavi.minimap.custom", "com.autonavi.cmccmap", "com.baidu.BaiduMap.pad", "com.baidu.carlife", "com.tigerknows", "com.erlinyou.worldlist", "com.uu.uueeye", "com.mapbar.android.trybuynavi", "com.zhituo.gpslocation", "com.waze", "ru.yandex.yandexnavi"));
        PowerKit mPowerKit = PowerKit.getInstance();
        if (mPowerKit == null) {
            return false;
        }
        boolean state = false;
        for (int k = 0; k < navigationPackages.size(); k++) {
            try {
                state = mPowerKit.checkStateByPkg(this.mContext, navigationPackages.get(k), 3);
            } catch (RemoteException e) {
                log(false, "checkStateByPkg occur exception.", new Object[0]);
            }
            if (state) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int getPeriodicSingleScanInterval() {
        if (!isSupportWifiScanGenie() || this.mWifiState == 1) {
            log(false, "****isSupportWifiScanGenie :  fasle: ", new Object[0]);
            this.mExponentialPeriodicSingleScanInterval *= 2;
            int maxInterval = 160000;
            if (this.mIsStationary) {
                maxInterval = 300000;
            }
            if (this.mExponentialPeriodicSingleScanInterval > maxInterval) {
                this.mExponentialPeriodicSingleScanInterval = maxInterval;
            }
            return this.mExponentialPeriodicSingleScanInterval;
        }
        log(false, "****isSupportWifiScanGenie :  true: ", new Object[0]);
        return getScanGeniePeriodicSingleScanInterval();
    }

    /* access modifiers changed from: protected */
    public void resetPeriodicSingleScanInterval() {
        this.mExponentialPeriodicSingleScanInterval = HwABSUtils.AUTO_HANDOVER_TIMER;
        handleScanCountChanged(2);
    }

    /* access modifiers changed from: protected */
    public void handleSingleScanFailure(int reason) {
        log(false, "handleSingleScanFailure reason %{public}d", Integer.valueOf(reason));
        handleScanCountChanged(1);
    }

    /* access modifiers changed from: protected */
    public void handleSingleScanSuccess() {
    }

    /* access modifiers changed from: protected */
    public void handleScanCountChanged(int reason) {
        if (reason == 0) {
            this.mHwSingleScanCounter++;
            this.mHwWifiProServiceManager.notifyUseFullChannels();
        } else if (1 == reason) {
            int i = this.mHwSingleScanCounter;
            if (i > 0) {
                this.mHwSingleScanCounter = i - 1;
            }
        } else if (2 == reason) {
            this.mHwSingleScanCounter = 0;
        }
        log(false, "handleScanCounterChanged,reason: %{public}d, mHwSingleScanCounter: %{public}d", Integer.valueOf(reason), Integer.valueOf(this.mHwSingleScanCounter));
    }

    /* access modifiers changed from: protected */
    public boolean isSupportWifiScanGenie() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiScanSpecialChannels() {
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager != null && castOptManager.isCastOptWorking()) {
            return true;
        }
        if (!isSupportWifiScanGenie()) {
            return false;
        }
        if (this.mHwSingleScanCounter <= 1) {
            return true;
        }
        this.mHwWifiProServiceManager.notifyUseFullChannels();
        return false;
    }

    /* access modifiers changed from: protected */
    public WifiScanner.ScanSettings getScanGenieSettings() {
        return getHwScanSettings();
    }

    /* access modifiers changed from: protected */
    public boolean handleForceScan() {
        return false;
    }

    private WifiScanner.ScanSettings getHwScanSettings() {
        WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
        settings.band = 0;
        settings.reportEvents = 3;
        settings.numBssidsPerScan = 0;
        CastOptManager castOptManager = CastOptManager.getInstance();
        if (castOptManager == null || !castOptManager.isCastOptWorking()) {
            List<Integer> fusefrequencyList = this.mHwWifiProServiceManager.getScanfrequencys();
            if (fusefrequencyList == null || fusefrequencyList.isEmpty()) {
                settings.channels = new WifiScanner.ChannelSpec[0];
                log(false, "getHwScanSettings,fusefrequencyList is null:", new Object[0]);
                return settings;
            }
            WifiScanner.ChannelSpec[] channels = new WifiScanner.ChannelSpec[fusefrequencyList.size()];
            for (int i = 0; i < fusefrequencyList.size(); i++) {
                channels[i] = new WifiScanner.ChannelSpec(fusefrequencyList.get(i).intValue());
            }
            settings.channels = channels;
        } else {
            settings.channels = castOptManager.getScanfrequencys();
        }
        return settings;
    }

    private void log(boolean isFmtStrPrivate, String msg, Object... args) {
        HwHiLog.d(TAG, isFmtStrPrivate, msg, args);
    }

    private boolean shouldDisconnectScanControl() {
        ClientModeImpl clientModeImpl = this.mWifiStateMachine;
        if (clientModeImpl == null || !clientModeImpl.isPortalConnectLast() || this.mHwSingleScanCounter >= 14) {
            return true;
        }
        log(false, "last disconnected network is portal, delay scan control", new Object[0]);
        return false;
    }

    private void localLog(String log) {
        this.mLocalLog.log(log);
    }

    /* access modifiers changed from: protected */
    public List<ScanResult> getScanResultsHasSameSsid(WifiScanner scanner, String bssid) {
        if (scanner == null || TextUtils.isEmpty(bssid)) {
            HwHiLog.d(TAG, false, "getScanResultsHasSameSsid: bssid is empty.", new Object[0]);
            return new ArrayList();
        }
        List<ScanResult> scanResults = scanner.getSingleScanResults();
        if (scanResults == null || !scanResults.isEmpty()) {
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
                HwHiLog.d(TAG, false, "getScanResultsHasSameSsid: can't find the corresponding ssid with the given bssid.", new Object[0]);
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
        HwHiLog.d(TAG, false, "getScanResultsHasSameSsid: WifiStateMachine.ScanResultsList is empty.", new Object[0]);
        return scanResults;
    }

    /* access modifiers changed from: protected */
    public void startHourPeriodicSingleScan() {
        boolean screenOn = isScreenOn();
        boolean wifiEnabled = isWifiEnabled();
        boolean wifiConnectivityManagerEnabled = isWifiConnectivityManagerEnabled();
        localLog("startHourPeriodicSingleScan: screenOn=" + screenOn + " wifiEnabled=" + wifiEnabled + " wifiConnectivityManagerEnabled=" + wifiConnectivityManagerEnabled + " WifiProCommonUtils.IS_TV=" + WifiProCommonUtils.IS_TV);
        if (screenOn && wifiEnabled && !wifiConnectivityManagerEnabled && !WifiProCommonUtils.IS_TV) {
            long currentTimeStamp = this.mClock.getElapsedSinceBootMillis();
            if (this.mLastHourPeriodicSingleScanTimeStamp != Long.MIN_VALUE) {
                long msSinceLastScan = currentTimeStamp - this.mLastHourPeriodicSingleScanTimeStamp;
                if (msSinceLastScan < 3600000) {
                    localLog("Last hour periodic single scan started " + msSinceLastScan + "ms ago, defer this new scan request.");
                    scheduleHourPeriodicScanTimer(3600000 - ((int) msSinceLastScan));
                    return;
                }
            }
            startScan(true, ClientModeImpl.WIFI_WORK_SOURCE);
            this.mLastHourPeriodicSingleScanTimeStamp = currentTimeStamp;
            scheduleHourPeriodicScanTimer(3600000);
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleHourPeriodicScanTimer(int intervalMs) {
        getAlarmManager().setExact(3, ((long) intervalMs) + this.mClock.getElapsedSinceBootMillis(), "WifiConnectivityManager Schedule Hour Periodic Scan Timer", this.mHourPeriodicScanTimerListener, getEventHandler());
        this.mHourPeriodicScanTimerSet = true;
    }

    /* access modifiers changed from: protected */
    public void stopHourPeriodicSingleScan() {
        AlarmManager mAlarmManager = getAlarmManager();
        if (this.mHourPeriodicScanTimerSet) {
            mAlarmManager.cancel(this.mHourPeriodicScanTimerListener);
            this.mHourPeriodicScanTimerSet = false;
        }
    }

    public boolean ignorePoorNetwork(WifiConfiguration candidate) {
        int i;
        ScoringParams scoringParams;
        ScanResult scanResultCandidate = candidate.getNetworkSelectionStatus().getCandidate();
        if (scanResultCandidate == null) {
            return false;
        }
        if (this.mPnoScanStarted) {
            if (scanResultCandidate.is24GHz()) {
                scoringParams = this.mScoringParams;
                i = 2400;
            } else {
                scoringParams = this.mScoringParams;
                i = HwMSSUtils.MSS_SYNC_AFT_CONNECTED;
            }
            int threshold = scoringParams.getEntryRssi(i);
            HwHiLog.i(TAG, false, "Pno scan rssi is %{public}d, threshold is %{public}d", new Object[]{Integer.valueOf(scanResultCandidate.level), Integer.valueOf(threshold)});
            if (scanResultCandidate.level < threshold) {
                return true;
            }
            return false;
        } else if (!isWifiScanSpecialChannels() || HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(scanResultCandidate.frequency, scanResultCandidate.level) > 2) {
            return false;
        } else {
            HwHiLog.i(TAG, false, "candidate = %{private}s, don't connect to poor network because use specified-channels-scan, rssi = %{public}d", new Object[]{candidate.configKey(), Integer.valueOf(scanResultCandidate.level)});
            return true;
        }
    }

    public boolean forceFullbandScanAgain(boolean mScreenOn, int mWifiState, boolean wasConnectAttempted, boolean isSingleScanStarted) {
        CastOptManager castOptManager = CastOptManager.getInstance();
        boolean isCastOptCondition = castOptManager != null && castOptManager.isCastOptWorking();
        if (!mScreenOn || mWifiState != 2 || wasConnectAttempted || !isWifiScanSpecialChannels() || !isSingleScanStarted || isCastOptCondition) {
            return false;
        }
        HwHiLog.w(TAG, false, "*******wifi scan special channels, but no connect ap ,  force fullband scan ****", new Object[0]);
        handleScanCountChanged(0);
        return true;
    }

    public int getWifiScanGenieMinInterval(int mPeriodicScanInterval) {
        if (isSupportWifiScanGenie()) {
            return 10000;
        }
        return mPeriodicScanInterval;
    }

    /* access modifiers changed from: protected */
    public void notifyScanGenieEvent(int eventId) {
        this.mHwWifiProServiceManager.notifyScanGenieEvent(eventId);
    }
}
