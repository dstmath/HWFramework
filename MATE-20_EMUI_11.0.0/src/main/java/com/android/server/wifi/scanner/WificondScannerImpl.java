package com.android.server.wifi.scanner;

import android.app.AlarmManager;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.ClientModeImpl;
import com.android.server.wifi.Clock;
import com.android.server.wifi.HiCoexManager;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.IHwWificondScannerImplEx;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.ScanResultUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.concurrent.GuardedBy;

public class WificondScannerImpl extends WifiScannerImpl implements Handler.Callback {
    private static final boolean DBG = false;
    private static final int MAX_APS_PER_SCAN = 32;
    public static final int MAX_HIDDEN_NETWORK_IDS_PER_SCAN = 16;
    private static final int MAX_SCAN_BUCKETS = 16;
    private static final String RT_DISABLE_WIFISCAN = "12";
    private static final String RT_ENABLE_WIFISCAN = "11";
    private static final int SCAN_BUFFER_CAPACITY = 10;
    private static final int SCAN_RESULT_CACHE_MS = 5000;
    private static final long SCAN_TIMEOUT_MS = 15000;
    private static final String TAG = "WificondScannerImpl";
    public static final String TIMEOUT_ALARM_TAG = "WificondScannerImpl Scan Timeout";
    private final AlarmManager mAlarmManager;
    private final ChannelHelper mChannelHelper;
    private final Clock mClock;
    private final Context mContext;
    private final Handler mEventHandler;
    private HiCoexManager mHiCoexManager = null;
    private final boolean mHwPnoScanSupported;
    private IHwWificondScannerImplEx mHwWificondScannerImplEx = null;
    private final String mIfaceName;
    private LastPnoScanSettings mLastPnoScanSettings = null;
    private LastScanSettings mLastScanSettings = null;
    private WifiScanner.ScanData mLatestSingleScanResult = new WifiScanner.ScanData(0, 0, new ScanResult[0]);
    private LocalLog mLocalLog = null;
    private ArrayList<ScanDetail> mNativePnoScanResults;
    private ArrayList<ScanDetail> mNativeScanResults;
    @GuardedBy("mSettingsLock")
    private AlarmManager.OnAlarmListener mScanTimeoutListener;
    private final Object mSettingsLock = new Object();
    private final WifiMonitor mWifiMonitor;
    private final WifiNative mWifiNative;

    public WificondScannerImpl(Context context, String ifaceName, WifiNative wifiNative, WifiMonitor wifiMonitor, ChannelHelper channelHelper, Looper looper, Clock clock) {
        this.mContext = context;
        this.mIfaceName = ifaceName;
        this.mWifiNative = wifiNative;
        this.mWifiMonitor = wifiMonitor;
        this.mChannelHelper = channelHelper;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mEventHandler = new Handler(looper, this);
        this.mClock = clock;
        this.mHwPnoScanSupported = this.mContext.getResources().getBoolean(17891576);
        wifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.SCAN_FAILED_EVENT, this.mEventHandler);
        wifiMonitor.registerHandler(this.mIfaceName, 147474, this.mEventHandler);
        wifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.SCAN_RESULTS_EVENT, this.mEventHandler);
        this.mHwWificondScannerImplEx = HwWifiServiceFactory.getHwWificondScannerImplEx(context);
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void cleanup() {
        synchronized (this.mSettingsLock) {
            stopHwPnoScan();
            this.mLastScanSettings = null;
            this.mLastPnoScanSettings = null;
            this.mWifiMonitor.deregisterHandler(this.mIfaceName, WifiMonitor.SCAN_FAILED_EVENT, this.mEventHandler);
            this.mWifiMonitor.deregisterHandler(this.mIfaceName, 147474, this.mEventHandler);
            this.mWifiMonitor.deregisterHandler(this.mIfaceName, WifiMonitor.SCAN_RESULTS_EVENT, this.mEventHandler);
        }
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean getScanCapabilities(WifiNative.ScanCapabilities capabilities) {
        capabilities.max_scan_cache_size = ScoringParams.Values.MAX_EXPID;
        capabilities.max_scan_buckets = 16;
        capabilities.max_ap_cache_per_scan = 32;
        capabilities.max_rssi_sample_size = 8;
        capabilities.max_scan_reporting_threshold = 10;
        return true;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean startSingleScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        if (eventHandler == null || settings == null) {
            Log.w(TAG, "Invalid arguments for startSingleScan: settings=" + settings + ",eventHandler=" + eventHandler);
            return false;
        } else if (!"factory".equals(SystemProperties.get("ro.runmode", "normal")) || !SystemProperties.getBoolean("persist.rt.running", false) || !RT_DISABLE_WIFISCAN.equals(SystemProperties.get("sys.abandonwifiscan.value", RT_ENABLE_WIFISCAN))) {
            synchronized (this.mSettingsLock) {
                if (this.mLastScanSettings != null) {
                    Log.w(TAG, "A single scan is already running");
                    return false;
                }
                ChannelHelper.ChannelCollection allFreqs = this.mChannelHelper.createChannelCollection();
                boolean reportFullResults = false;
                for (int i = 0; i < settings.num_buckets; i++) {
                    WifiNative.BucketSettings bucketSettings = settings.buckets[i];
                    if ((bucketSettings.report_events & 2) != 0) {
                        reportFullResults = true;
                    }
                    allFreqs.addChannels(bucketSettings);
                }
                List<String> hiddenNetworkSSIDSet = new ArrayList<>();
                if (settings.hiddenNetworks != null) {
                    int numHiddenNetworks = Math.min(settings.hiddenNetworks.length, 16);
                    for (int i2 = 0; i2 < numHiddenNetworks; i2++) {
                        hiddenNetworkSSIDSet.add(settings.hiddenNetworks[i2].ssid);
                    }
                }
                this.mLastScanSettings = new LastScanSettings(this.mClock.getElapsedSinceBootMillis(), reportFullResults, allFreqs, eventHandler);
                if (settings.isHiddenSingleScan) {
                    Log.d(TAG, "settings isHiddenSingleScan true");
                    this.mLastScanSettings.isHiddenSingleScan = true;
                }
                boolean success = false;
                if (!allFreqs.isEmpty()) {
                    Set<Integer> freqs = allFreqs.getScanFreqs();
                    success = this.mWifiNative.scan(this.mIfaceName, settings.scanType, freqs, hiddenNetworkSSIDSet);
                    if (!success) {
                        Log.e(TAG, "Failed to start scan, freqs=" + freqs);
                    }
                } else {
                    Log.e(TAG, "Failed to start scan because there is no available channel to scan");
                }
                if (success) {
                    this.mScanTimeoutListener = new AlarmManager.OnAlarmListener() {
                        /* class com.android.server.wifi.scanner.WificondScannerImpl.AnonymousClass1 */

                        @Override // android.app.AlarmManager.OnAlarmListener
                        public void onAlarm() {
                            WificondScannerImpl.this.handleScanTimeout();
                        }
                    };
                    this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + SCAN_TIMEOUT_MS, TIMEOUT_ALARM_TAG, this.mScanTimeoutListener, this.mEventHandler);
                } else {
                    this.mEventHandler.post(new Runnable() {
                        /* class com.android.server.wifi.scanner.WificondScannerImpl.AnonymousClass2 */

                        @Override // java.lang.Runnable
                        public void run() {
                            WificondScannerImpl.this.reportScanFailure();
                        }
                    });
                }
                return true;
            }
        } else {
            Log.w(TAG, "Abandon wifi scan in factory Rt running mode");
            return true;
        }
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public WifiScanner.ScanData getLatestSingleScanResults() {
        return this.mLatestSingleScanResult;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean startBatchedScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        Log.w(TAG, "startBatchedScan() is not supported");
        return false;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void stopBatchedScan() {
        Log.w(TAG, "stopBatchedScan() is not supported");
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void pauseBatchedScan() {
        Log.w(TAG, "pauseBatchedScan() is not supported");
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void restartBatchedScan() {
        Log.w(TAG, "restartBatchedScan() is not supported");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanTimeout() {
        synchronized (this.mSettingsLock) {
            Log.e(TAG, "Timed out waiting for scan result from wificond");
            reportScanFailure();
            this.mScanTimeoutListener = null;
        }
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void setWifiScanLogger(LocalLog logger) {
        this.mLocalLog = logger;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void logWifiScan(String message) {
        LocalLog localLog = this.mLocalLog;
        if (localLog != null) {
            localLog.log("<WifiScanLogger> " + message);
        }
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WifiMonitor.SCAN_RESULTS_EVENT /* 147461 */:
                cancelScanTimeout();
                pollLatestScanData();
                return true;
            case WifiMonitor.SCAN_FAILED_EVENT /* 147473 */:
                Log.w(TAG, "Scan failed");
                cancelScanTimeout();
                reportScanFailure();
                return true;
            case 147474:
                pollLatestScanDataForPno();
                return true;
            default:
                return true;
        }
    }

    private void cancelScanTimeout() {
        synchronized (this.mSettingsLock) {
            if (this.mScanTimeoutListener != null) {
                this.mAlarmManager.cancel(this.mScanTimeoutListener);
                this.mScanTimeoutListener = null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportScanFailure() {
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings != null) {
                if (this.mLastScanSettings.singleScanEventHandler != null) {
                    this.mLastScanSettings.singleScanEventHandler.onScanStatus(3);
                }
                this.mLastScanSettings = null;
            }
        }
    }

    private void reportPnoScanFailure() {
        synchronized (this.mSettingsLock) {
            if (this.mLastPnoScanSettings != null) {
                if (this.mLastPnoScanSettings.pnoScanEventHandler != null) {
                    this.mLastPnoScanSettings.pnoScanEventHandler.onPnoScanFailed();
                }
                this.mLastPnoScanSettings = null;
            }
        }
    }

    private void pollLatestScanDataForPno() {
        synchronized (this.mSettingsLock) {
            if (this.mLastPnoScanSettings != null) {
                this.mNativePnoScanResults = this.mWifiNative.getPnoScanResults(this.mIfaceName);
                List<ScanResult> hwPnoScanResults = new ArrayList<>();
                int numFilteredScanResults = 0;
                for (int i = 0; i < this.mNativePnoScanResults.size(); i++) {
                    ScanResult result = this.mNativePnoScanResults.get(i).getScanResult();
                    if (result.timestamp / 1000 > this.mLastPnoScanSettings.startTime) {
                        hwPnoScanResults.add(result);
                    } else {
                        numFilteredScanResults++;
                    }
                }
                if (numFilteredScanResults != 0 || this.mNativePnoScanResults.size() == 0) {
                    Log.i("WifiScanLog", "Pno Filtering out " + numFilteredScanResults + " pno scan results.total size " + this.mNativePnoScanResults.size());
                }
                if (this.mLastPnoScanSettings.pnoScanEventHandler != null) {
                    this.mLastPnoScanSettings.pnoScanEventHandler.onPnoNetworkFound((ScanResult[]) hwPnoScanResults.toArray(new ScanResult[hwPnoScanResults.size()]));
                }
            }
        }
    }

    private static int getBandScanned(ChannelHelper.ChannelCollection channelCollection) {
        if (channelCollection.containsBand(7)) {
            return 7;
        }
        if (channelCollection.containsBand(3)) {
            return 3;
        }
        if (channelCollection.containsBand(6)) {
            return 6;
        }
        if (channelCollection.containsBand(2)) {
            return 2;
        }
        if (channelCollection.containsBand(4)) {
            return 4;
        }
        if (channelCollection.containsBand(1)) {
            return 1;
        }
        return 0;
    }

    private void pollLatestScanData() {
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings != null) {
                if (this.mHiCoexManager == null) {
                    this.mHiCoexManager = HwWifiServiceFactory.getHiCoexManager();
                }
                if (this.mHiCoexManager != null) {
                    this.mHiCoexManager.notifyForegroundScan(false, "");
                }
                this.mNativeScanResults = this.mWifiNative.getScanResults(this.mIfaceName);
                ArrayList<ScanResult> arrayList = new ArrayList();
                List<ScanResult> singleScanResultsForApp = new ArrayList<>();
                int numFilteredScanResults = 0;
                String holdBssid = getConnectedBssid();
                int list_size = this.mNativeScanResults.size();
                for (int i = 0; i < list_size; i++) {
                    ScanResult result = this.mNativeScanResults.get(i).getScanResult();
                    long timestamp_ms = result.timestamp / 1000;
                    if (RttServiceImpl.HAL_RANGING_TIMEOUT_MS + timestamp_ms <= this.mLastScanSettings.startTime) {
                        if (!holdBssid.equals(result.BSSID)) {
                            numFilteredScanResults++;
                            this.mHwWificondScannerImplEx.pollLatestScanData(result, this.mLastScanSettings.startTime, this.mLastScanSettings.singleScanFreqs.containsChannel(result.frequency));
                        }
                    }
                    if (timestamp_ms <= this.mLastScanSettings.startTime) {
                        result.timestamp += 1000;
                    }
                    if (timestamp_ms <= this.mLastScanSettings.startTime && holdBssid.equals(result.BSSID)) {
                        result.timestamp = this.mClock.getElapsedSinceBootMillis() * 1000;
                    }
                    if (this.mLastScanSettings.singleScanFreqs.containsChannel(result.frequency)) {
                        arrayList.add(result);
                    }
                    this.mHwWificondScannerImplEx.pollLatestScanData(result, this.mLastScanSettings.startTime, this.mLastScanSettings.singleScanFreqs.containsChannel(result.frequency));
                }
                singleScanResultsForApp.addAll(arrayList);
                if (numFilteredScanResults != 0 || this.mNativeScanResults.size() == 0) {
                    Log.i("WifiScanLog", "Filtering out " + numFilteredScanResults + " scan results. total size " + this.mNativeScanResults.size() + " , Filtered Results : " + ((Object) this.mHwWificondScannerImplEx.getResultsString()));
                }
                if (this.mLastScanSettings.singleScanEventHandler != null) {
                    if (this.mLastScanSettings.reportSingleScanFullResults) {
                        for (ScanResult scanResult : arrayList) {
                            this.mLastScanSettings.singleScanEventHandler.onFullScanResult(scanResult, 0);
                        }
                    }
                    Collections.sort(arrayList, SCAN_RESULT_SORT_COMPARATOR);
                    Collections.sort(singleScanResultsForApp, SCAN_RESULT_SORT_COMPARATOR);
                    this.mLatestSingleScanResult = new WifiScanner.ScanData(0, 0, 0, getBandScanned(this.mLastScanSettings.singleScanFreqs), (ScanResult[]) singleScanResultsForApp.toArray(new ScanResult[singleScanResultsForApp.size()]), this.mLastScanSettings.isHiddenSingleScan);
                    this.mLastScanSettings.singleScanEventHandler.onScanStatus(0);
                }
                this.mLastScanSettings = null;
            }
        }
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public WifiScanner.ScanData[] getLatestBatchedScanResults(boolean flush) {
        return null;
    }

    private boolean startHwPnoScan(WifiNative.PnoSettings pnoSettings) {
        return this.mWifiNative.startPnoScan(this.mIfaceName, pnoSettings);
    }

    private void stopHwPnoScan() {
        this.mWifiNative.stopPnoScan(this.mIfaceName);
    }

    private boolean isHwPnoScanRequired(boolean isConnectedPno) {
        return !isConnectedPno && this.mHwPnoScanSupported;
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean setHwPnoList(WifiNative.PnoSettings settings, WifiNative.PnoEventHandler eventHandler) {
        synchronized (this.mSettingsLock) {
            if (this.mLastPnoScanSettings != null) {
                Log.w(TAG, "Already running a PNO scan");
                return false;
            } else if (!isHwPnoScanRequired(settings.isConnected)) {
                return false;
            } else {
                if (startHwPnoScan(settings)) {
                    this.mLastPnoScanSettings = new LastPnoScanSettings(this.mClock.getElapsedSinceBootMillis(), settings.networkList, eventHandler);
                } else {
                    Log.e(TAG, "Failed to start PNO scan");
                    reportPnoScanFailure();
                }
                return true;
            }
        }
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean resetHwPnoList() {
        synchronized (this.mSettingsLock) {
            if (this.mLastPnoScanSettings == null) {
                Log.w(TAG, "No PNO scan running");
                return false;
            }
            this.mLastPnoScanSettings = null;
            stopHwPnoScan();
            return true;
        }
    }

    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public boolean isHwPnoSupported(boolean isConnectedPno) {
        return isHwPnoScanRequired(isConnectedPno);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wifi.scanner.WifiScannerImpl
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (this.mSettingsLock) {
            long nowMs = this.mClock.getElapsedSinceBootMillis();
            pw.println("Latest native scan results:");
            if (this.mNativeScanResults != null) {
                ScanResultUtil.dumpScanResults(pw, (List) this.mNativeScanResults.stream().map($$Lambda$WificondScannerImpl$CSjtYSyNiQ_mC6mOyQ4GpkylqY.INSTANCE).collect(Collectors.toList()), nowMs);
            }
            pw.println("Latest native pno scan results:");
            if (this.mNativePnoScanResults != null) {
                ScanResultUtil.dumpScanResults(pw, (List) this.mNativePnoScanResults.stream().map($$Lambda$WificondScannerImpl$VfxaUtYlcuU7Z28abhvk42O2k.INSTANCE).collect(Collectors.toList()), nowMs);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class LastScanSettings {
        public boolean isHiddenSingleScan = false;
        public boolean reportSingleScanFullResults;
        public WifiNative.ScanEventHandler singleScanEventHandler;
        public ChannelHelper.ChannelCollection singleScanFreqs;
        public long startTime;

        LastScanSettings(long startTime2, boolean reportSingleScanFullResults2, ChannelHelper.ChannelCollection singleScanFreqs2, WifiNative.ScanEventHandler singleScanEventHandler2) {
            this.startTime = startTime2;
            this.reportSingleScanFullResults = reportSingleScanFullResults2;
            this.singleScanFreqs = singleScanFreqs2;
            this.singleScanEventHandler = singleScanEventHandler2;
        }
    }

    /* access modifiers changed from: private */
    public static class LastPnoScanSettings {
        public WifiNative.PnoNetwork[] pnoNetworkList;
        public WifiNative.PnoEventHandler pnoScanEventHandler;
        public long startTime;

        LastPnoScanSettings(long startTime2, WifiNative.PnoNetwork[] pnoNetworkList2, WifiNative.PnoEventHandler pnoScanEventHandler2) {
            this.startTime = startTime2;
            this.pnoNetworkList = pnoNetworkList2;
            this.pnoScanEventHandler = pnoScanEventHandler2;
        }
    }

    private String getConnectedBssid() {
        ClientModeImpl clientMode;
        WifiInfo wifiInfo;
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null || (clientMode = wifiInjector.getClientModeImpl()) == null || (wifiInfo = clientMode.getWifiInfo()) == null || wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return "";
        }
        if (wifiInfo.getBSSID() != null) {
            return wifiInfo.getBSSID();
        }
        return "";
    }
}
