package com.android.server.wifi.scanner;

import android.app.AlarmManager;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.wifi.Clock;
import com.android.server.wifi.ScanDetail;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.WifiMonitor;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.scanner.ChannelHelper;
import com.android.server.wifi.util.ScanResultUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.concurrent.GuardedBy;

public class WificondScannerImpl extends WifiScannerImpl implements Handler.Callback {
    private static final boolean DBG = false;
    private static final int MAX_APS_PER_SCAN = 32;
    public static final int MAX_HIDDEN_NETWORK_IDS_PER_SCAN = 16;
    private static final int MAX_SCAN_BUCKETS = 16;
    private static final int SCAN_BUFFER_CAPACITY = 10;
    private static final long SCAN_TIMEOUT_MS = 15000;
    private static final String TAG = "WificondScannerImpl";
    public static final String TIMEOUT_ALARM_TAG = "WificondScannerImpl Scan Timeout";
    private final AlarmManager mAlarmManager;
    private final ChannelHelper mChannelHelper;
    private final Clock mClock;
    private final Context mContext;
    private final Handler mEventHandler;
    private final boolean mHwPnoScanSupported;
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

    private static class LastPnoScanSettings {
        public WifiNative.PnoNetwork[] pnoNetworkList;
        public WifiNative.PnoEventHandler pnoScanEventHandler;
        public long startTime;

        LastPnoScanSettings(long startTime2, WifiNative.PnoNetwork[] pnoNetworkList2, WifiNative.PnoEventHandler pnoScanEventHandler2) {
            this.startTime = startTime2;
            this.pnoNetworkList = pnoNetworkList2;
            this.pnoScanEventHandler = pnoScanEventHandler2;
        }
    }

    private static class LastScanSettings {
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

    public WificondScannerImpl(Context context, String ifaceName, WifiNative wifiNative, WifiMonitor wifiMonitor, ChannelHelper channelHelper, Looper looper, Clock clock) {
        this.mContext = context;
        this.mIfaceName = ifaceName;
        this.mWifiNative = wifiNative;
        this.mWifiMonitor = wifiMonitor;
        this.mChannelHelper = channelHelper;
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mEventHandler = new Handler(looper, this);
        this.mClock = clock;
        this.mHwPnoScanSupported = this.mContext.getResources().getBoolean(17957071);
        wifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.SCAN_FAILED_EVENT, this.mEventHandler);
        wifiMonitor.registerHandler(this.mIfaceName, 147474, this.mEventHandler);
        wifiMonitor.registerHandler(this.mIfaceName, WifiMonitor.SCAN_RESULTS_EVENT, this.mEventHandler);
    }

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

    public boolean getScanCapabilities(WifiNative.ScanCapabilities capabilities) {
        capabilities.max_scan_cache_size = ScoringParams.Values.MAX_EXPID;
        capabilities.max_scan_buckets = 16;
        capabilities.max_ap_cache_per_scan = 32;
        capabilities.max_rssi_sample_size = 8;
        capabilities.max_scan_reporting_threshold = 10;
        return true;
    }

    public ChannelHelper getChannelHelper() {
        return this.mChannelHelper;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00e4, code lost:
        return true;
     */
    public boolean startSingleScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        WifiNative.ScanSettings scanSettings = settings;
        WifiNative.ScanEventHandler scanEventHandler = eventHandler;
        if (scanEventHandler == null || scanSettings == null) {
            Log.w(TAG, "Invalid arguments for startSingleScan: settings=" + scanSettings + ",eventHandler=" + scanEventHandler);
            return false;
        }
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings != null) {
                Log.w(TAG, "A single scan is already running");
                return false;
            }
            ChannelHelper.ChannelCollection allFreqs = this.mChannelHelper.createChannelCollection();
            boolean reportFullResults = false;
            for (int i = 0; i < scanSettings.num_buckets; i++) {
                WifiNative.BucketSettings bucketSettings = scanSettings.buckets[i];
                if ((bucketSettings.report_events & 2) != 0) {
                    reportFullResults = true;
                }
                allFreqs.addChannels(bucketSettings);
            }
            List<String> hiddenNetworkSSIDSet = new ArrayList<>();
            if (scanSettings.hiddenNetworks != null) {
                int numHiddenNetworks = Math.min(scanSettings.hiddenNetworks.length, 16);
                for (int i2 = 0; i2 < numHiddenNetworks; i2++) {
                    hiddenNetworkSSIDSet.add(scanSettings.hiddenNetworks[i2].ssid);
                }
            }
            LastScanSettings lastScanSettings = new LastScanSettings(this.mClock.getElapsedSinceBootMillis(), reportFullResults, allFreqs, scanEventHandler);
            this.mLastScanSettings = lastScanSettings;
            if (scanSettings.isHiddenSingleScan) {
                Log.d(TAG, "settings isHiddenSingleScan true");
                this.mLastScanSettings.isHiddenSingleScan = true;
            }
            boolean success = false;
            if (!allFreqs.isEmpty()) {
                success = this.mWifiNative.scan(this.mIfaceName, scanSettings.scanType, allFreqs.getScanFreqs(), hiddenNetworkSSIDSet);
                if (!success) {
                    Log.e(TAG, "Failed to start scan, freqs=" + freqs);
                }
            } else {
                Log.e(TAG, "Failed to start scan because there is no available channel to scan");
            }
            if (success) {
                this.mScanTimeoutListener = new AlarmManager.OnAlarmListener() {
                    public void onAlarm() {
                        WificondScannerImpl.this.handleScanTimeout();
                    }
                };
                this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + SCAN_TIMEOUT_MS, TIMEOUT_ALARM_TAG, this.mScanTimeoutListener, this.mEventHandler);
            } else {
                this.mEventHandler.post(new Runnable() {
                    public void run() {
                        WificondScannerImpl.this.reportScanFailure();
                    }
                });
            }
        }
    }

    public WifiScanner.ScanData getLatestSingleScanResults() {
        return this.mLatestSingleScanResult;
    }

    public boolean startBatchedScan(WifiNative.ScanSettings settings, WifiNative.ScanEventHandler eventHandler) {
        Log.w(TAG, "startBatchedScan() is not supported");
        return false;
    }

    public void stopBatchedScan() {
        Log.w(TAG, "stopBatchedScan() is not supported");
    }

    public void pauseBatchedScan() {
        Log.w(TAG, "pauseBatchedScan() is not supported");
    }

    public void restartBatchedScan() {
        Log.w(TAG, "restartBatchedScan() is not supported");
    }

    /* access modifiers changed from: private */
    public void handleScanTimeout() {
        synchronized (this.mSettingsLock) {
            Log.e(TAG, "Timed out waiting for scan result from wificond");
            reportScanFailure();
            this.mScanTimeoutListener = null;
        }
    }

    public void setWifiScanLogger(LocalLog logger) {
        this.mLocalLog = logger;
    }

    public void logWifiScan(String message) {
        if (this.mLocalLog != null) {
            LocalLog localLog = this.mLocalLog;
            localLog.log("<WifiScanLogger> " + message);
        }
    }

    public boolean handleMessage(Message msg) {
        int i = msg.what;
        if (i != 147461) {
            switch (i) {
                case WifiMonitor.SCAN_FAILED_EVENT:
                    Log.w(TAG, "Scan failed");
                    cancelScanTimeout();
                    reportScanFailure();
                    break;
                case 147474:
                    pollLatestScanDataForPno();
                    break;
            }
        } else {
            cancelScanTimeout();
            pollLatestScanData();
        }
        return true;
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
    public void reportScanFailure() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008d, code lost:
        return;
     */
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
                    Log.d("WifiScanLog", "Pno Filtering out " + numFilteredScanResults + " pno scan results.total size " + this.mNativePnoScanResults.size());
                }
                if (this.mLastPnoScanSettings.pnoScanEventHandler != null) {
                    this.mLastPnoScanSettings.pnoScanEventHandler.onPnoNetworkFound((ScanResult[]) hwPnoScanResults.toArray(new ScanResult[hwPnoScanResults.size()]));
                }
            }
        }
    }

    private static boolean isAllChannelsScanned(ChannelHelper.ChannelCollection channelCollection) {
        if (!channelCollection.containsBand(1) || !channelCollection.containsBand(2)) {
            return false;
        }
        return true;
    }

    private ArrayMap<String, StringBuffer> getSavedNetworkScanResults() {
        long nowMs = System.currentTimeMillis();
        ArrayMap<String, StringBuffer> filterResults = new ArrayMap<>();
        WifiManager wifiMgr = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiMgr == null) {
            Log.d(TAG, "WifiManager is null,error!");
            return filterResults;
        }
        List<WifiConfiguration> savedNetworks = wifiMgr.getConfiguredNetworks();
        int savedNetworksListSize = savedNetworks.size();
        for (int i = 0; i < savedNetworksListSize; i++) {
            WifiConfiguration network = savedNetworks.get(i);
            long diffMs = nowMs - (0 == network.lastHasInternetTimestamp ? network.lastConnected : network.lastHasInternetTimestamp);
            if (0 < diffMs && diffMs < 604800000) {
                filterResults.put(network.SSID, null);
            }
        }
        return filterResults;
    }

    private void pollLatestScanData() {
        boolean isFrequencyFiltered;
        boolean isTimeFiltered;
        boolean isFrequencyFiltered2;
        StringBuffer filterSsidValue;
        synchronized (this.mSettingsLock) {
            if (this.mLastScanSettings != null) {
                this.mNativeScanResults = this.mWifiNative.getScanResults(this.mIfaceName);
                List<ScanResult> singleScanResults = new ArrayList<>();
                List<ScanResult> singleScanResultsForApp = new ArrayList<>();
                StringBuffer filteredResultsString = new StringBuffer();
                ArrayMap<String, StringBuffer> filterResults = getSavedNetworkScanResults();
                int numFilteredScanResults = 0;
                int i = 0;
                int list_size = this.mNativeScanResults.size();
                while (i < list_size) {
                    ScanResult result = this.mNativeScanResults.get(i).getScanResult();
                    long timestamp_ms = result.timestamp / 1000;
                    if (timestamp_ms > this.mLastScanSettings.startTime) {
                        isTimeFiltered = false;
                        if (this.mLastScanSettings.singleScanFreqs.containsChannel(result.frequency)) {
                            singleScanResults.add(result);
                            isFrequencyFiltered = false;
                        } else {
                            isFrequencyFiltered = true;
                        }
                    } else {
                        isTimeFiltered = true;
                        isFrequencyFiltered = false;
                        if (this.mLastScanSettings.singleScanFreqs.containsChannel(result.frequency)) {
                            singleScanResultsForApp.add(result);
                        }
                    }
                    boolean isFrequencyFiltered3 = isFrequencyFiltered;
                    boolean isTimeFiltered2 = isTimeFiltered;
                    if (!isTimeFiltered2) {
                        if (!isFrequencyFiltered3) {
                            isFrequencyFiltered2 = isFrequencyFiltered3;
                            i++;
                            boolean z = isTimeFiltered2;
                            boolean isTimeFiltered3 = isFrequencyFiltered2;
                        }
                    }
                    if (numFilteredScanResults == 0) {
                        long j = timestamp_ms;
                        filteredResultsString.append(this.mLastScanSettings.startTime);
                        filteredResultsString.append("/");
                    }
                    if (isFrequencyFiltered3) {
                        filteredResultsString.append(result.SSID);
                        filteredResultsString.append("|");
                        filteredResultsString.append(result.frequency);
                        filteredResultsString.append("|");
                        filteredResultsString.append(ScanResultUtil.getConfusedBssid(result.BSSID));
                        filteredResultsString.append("|");
                    }
                    if (isTimeFiltered2) {
                        StringBuffer stringBuffer = new StringBuffer("\"");
                        stringBuffer.append(result.SSID);
                        stringBuffer.append("\"");
                        String filterSsid = stringBuffer.toString();
                        if (filterResults.containsKey(filterSsid)) {
                            StringBuffer filterSsidValue2 = filterResults.get(filterSsid);
                            if (filterSsidValue2 == null) {
                                filterSsidValue = new StringBuffer();
                            } else {
                                filterSsidValue = filterSsidValue2;
                            }
                            try {
                                filterSsidValue.append(result.BSSID.substring(result.BSSID.length() - 5));
                                filterSsidValue.append("|");
                                isFrequencyFiltered2 = isFrequencyFiltered3;
                                try {
                                    filterSsidValue.append(result.timestamp / 1000);
                                    filterSsidValue.append("|");
                                    filterResults.put(filterSsid, filterSsidValue);
                                } catch (StringIndexOutOfBoundsException e) {
                                }
                            } catch (StringIndexOutOfBoundsException e2) {
                                isFrequencyFiltered2 = isFrequencyFiltered3;
                                Log.d(TAG, "substring: StringIndexOutOfBoundsException");
                                numFilteredScanResults++;
                                i++;
                                boolean z2 = isTimeFiltered2;
                                boolean isTimeFiltered32 = isFrequencyFiltered2;
                            }
                            numFilteredScanResults++;
                            i++;
                            boolean z22 = isTimeFiltered2;
                            boolean isTimeFiltered322 = isFrequencyFiltered2;
                        }
                    }
                    isFrequencyFiltered2 = isFrequencyFiltered3;
                    numFilteredScanResults++;
                    i++;
                    boolean z222 = isTimeFiltered2;
                    boolean isTimeFiltered3222 = isFrequencyFiltered2;
                }
                for (Map.Entry<String, StringBuffer> entry : filterResults.entrySet()) {
                    StringBuffer arrayMap = entry.getValue();
                    if (arrayMap != null) {
                        filteredResultsString.append(entry.getKey());
                        filteredResultsString.append("|");
                        filteredResultsString.append(arrayMap);
                    }
                }
                singleScanResultsForApp.addAll(singleScanResults);
                if (numFilteredScanResults != 0 || this.mNativeScanResults.size() == 0) {
                    Log.d("WifiScanLog", "Filtering out " + numFilteredScanResults + " scan results. total size " + this.mNativeScanResults.size() + " , Filtered Results : " + filteredResultsString);
                }
                if (this.mLastScanSettings.singleScanEventHandler != null) {
                    if (this.mLastScanSettings.reportSingleScanFullResults) {
                        for (ScanResult scanResult : singleScanResults) {
                            this.mLastScanSettings.singleScanEventHandler.onFullScanResult(scanResult, 0);
                        }
                    }
                    Collections.sort(singleScanResults, SCAN_RESULT_SORT_COMPARATOR);
                    Collections.sort(singleScanResultsForApp, SCAN_RESULT_SORT_COMPARATOR);
                    WifiScanner.ScanData scanData = new WifiScanner.ScanData(0, 0, 0, isAllChannelsScanned(this.mLastScanSettings.singleScanFreqs), (ScanResult[]) singleScanResultsForApp.toArray(new ScanResult[singleScanResultsForApp.size()]), this.mLastScanSettings.isHiddenSingleScan);
                    this.mLatestSingleScanResult = scanData;
                    this.mLastScanSettings.singleScanEventHandler.onScanStatus(0);
                }
                this.mLastScanSettings = null;
            }
        }
    }

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

    public boolean isHwPnoSupported(boolean isConnectedPno) {
        return isHwPnoScanRequired(isConnectedPno);
    }

    /* access modifiers changed from: protected */
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
}
