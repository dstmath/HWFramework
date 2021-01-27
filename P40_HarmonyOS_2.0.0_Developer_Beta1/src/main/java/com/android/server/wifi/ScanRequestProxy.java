package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiScanner;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class ScanRequestProxy implements IHwScanRequestProxyInner {
    private static final String ENCRYPTION_TYPE_OWE = "OWE";
    private static final String ENCRYPTION_TYPE_SAE = "SAE";
    private static final String LBS_PACKAGE_NAME = "com.huawei.lbs";
    private static final String SCAN_DURATION = "ScanDura";
    private static final String SCAN_ERROR_CODE = "ErrCode";
    private static final String SCAN_OWE_AP_COUNT = "OweApCnt";
    @VisibleForTesting
    public static final int SCAN_REQUEST_THROTTLE_INTERVAL_BG_APPS_MS = 1800000;
    @VisibleForTesting
    public static final int SCAN_REQUEST_THROTTLE_MAX_IN_TIME_WINDOW_FG_APPS = 4;
    @VisibleForTesting
    public static final int SCAN_REQUEST_THROTTLE_TIME_WINDOW_FG_APPS_MS = 120000;
    private static final int SCAN_RESULTES_CACHE_TIME_IN_MILLIS = 30000;
    private static final String SCAN_SAE_AP_COUNT = "SaeApCnt";
    private static final String[] SCAN_WHITE_PACKAGE_NAME_LIST = {"com.huawei.smarthome", "com.huawei.smartspeaker", "com.hicloud.android.clone", "com.huaweioverseas.smarthome", "com.sankuai.meituan.dispatch.homebrew", "com.huawei.hereto", "com.sankuai.meituan.dispatch.crowdsource", "com.huawei.indoorkit", "com.huawei.heretomapgrabber", "com.sankuai.meituan"};
    private static final String SCAN_WPA2_AP_COUNT = "Wpa2ApCnt";
    private static final String TAG = "WifiScanRequestProxy";
    private final ActivityManager mActivityManager;
    private final AppOpsManager mAppOps;
    private final Clock mClock;
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final List<ScanResult> mHistoryScanResults = new ArrayList();
    public final IHwScanRequestProxyEx mHwScanRequestProxyEx;
    private HwWifiCHRService mHwWifiCHRService = HwWifiServiceFactory.getHwWifiCHRService();
    private final List<ScanResult> mLastScanResults = new ArrayList();
    private long mLastScanTimestampForBgApps = 0;
    private final ArrayMap<Pair<Integer, String>, LinkedList<Long>> mLastScanTimestampsForFgApps = new ArrayMap<>();
    private long mScanDuration = 0;
    private boolean mScanningEnabled = false;
    private boolean mScanningForHiddenNetworksEnabled = false;
    private final List<ScanResult> mSpecifiedChannelScanResults = new ArrayList();
    private long mStartTime = 0;
    private final ThrottleEnabledSettingObserver mThrottleEnabledSettingObserver;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiScanner mWifiScanner;

    /* access modifiers changed from: private */
    public class GlobalScanListener implements WifiScanner.ScanListener {
        private GlobalScanListener() {
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.d(ScanRequestProxy.TAG, "Scan results received");
            }
            if (scanDatas.length != 1) {
                Log.wtf(ScanRequestProxy.TAG, "Found more than 1 batch of scan results, Failing...");
                ScanRequestProxy.this.sendScanResultBroadcast(false);
                return;
            }
            WifiScanner.ScanData scanData = scanDatas[0];
            ScanResult[] scanResults = scanData.getResults();
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.d(ScanRequestProxy.TAG, "Received " + scanResults.length + " scan results");
            }
            ScanRequestProxy.this.mHwScanRequestProxyEx.sendWifiCategoryChangeBroadcast();
            if (scanData.getBandScanned() == 7 || scanData.getBandScanned() == 0) {
                synchronized (ScanRequestProxy.this) {
                    if (scanData.getBandScanned() == 7) {
                        ScanRequestProxy.this.mHistoryScanResults.clear();
                        ScanRequestProxy.this.mHistoryScanResults.addAll(ScanRequestProxy.this.mLastScanResults);
                        ScanRequestProxy.this.mSpecifiedChannelScanResults.clear();
                        ScanRequestProxy.this.mLastScanResults.clear();
                        ScanRequestProxy.this.mLastScanResults.addAll(Arrays.asList(scanResults));
                    } else if (ScanRequestProxy.this.mLastScanResults.size() == 0) {
                        ScanRequestProxy.this.mSpecifiedChannelScanResults.clear();
                        ScanRequestProxy.this.mSpecifiedChannelScanResults.addAll(Arrays.asList(scanResults));
                    } else {
                        ScanRequestProxy.this.mergeScanResults(scanResults);
                    }
                }
                ScanRequestProxy.this.sendScanResultBroadcast(true);
                if (ScanRequestProxy.this.mHwScanRequestProxyEx.getAllowHiLinkScanResultsBroadcast()) {
                    ScanRequestProxy.this.mHwScanRequestProxyEx.sendHilinkscanResultBroadcast();
                }
            }
            ScanRequestProxy.this.mScanDuration = SystemClock.elapsedRealtime() - ScanRequestProxy.this.mStartTime;
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void mergeScanResults(ScanResult[] scanResults) {
        int size = this.mLastScanResults.size();
        for (ScanResult scanResult : scanResults) {
            if (!TextUtils.isEmpty(scanResult.BSSID)) {
                boolean isMatched = false;
                int i = 0;
                while (true) {
                    if (i >= size) {
                        break;
                    } else if (scanResult.BSSID.equals(this.mLastScanResults.get(i).BSSID)) {
                        this.mLastScanResults.set(i, scanResult);
                        isMatched = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!isMatched) {
                    this.mLastScanResults.add(scanResult);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class ScanRequestProxyScanListener implements WifiScanner.ScanListener {
        private boolean mIsQuickTtffScan;

        private ScanRequestProxyScanListener() {
            this.mIsQuickTtffScan = false;
        }

        public void onSuccess() {
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.d(ScanRequestProxy.TAG, "Scan request succeeded");
            }
        }

        public void onFailure(int reason, String description) {
            Log.e(ScanRequestProxy.TAG, "Scan failure received. reason: " + reason + ",description: " + description);
            ScanRequestProxy.this.sendScanResultBroadcast(false);
            ScanRequestProxy.this.mScanDuration = SystemClock.elapsedRealtime() - ScanRequestProxy.this.mStartTime;
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
            if (scanDatas != null && scanDatas.length == 1 && this.mIsQuickTtffScan) {
                Log.i(ScanRequestProxy.TAG, "Process QuickttffScan results.");
                ScanRequestProxy.this.update24GhzScanResults(scanDatas[0].getResults());
                Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
                intent.addFlags(67108864);
                intent.putExtra("resultsUpdated", true);
                ScanRequestProxy.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.ACCESS_COARSE_LOCATION");
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }

        public void setQuickTtffScan() {
            this.mIsQuickTtffScan = true;
        }
    }

    /* access modifiers changed from: private */
    public class ThrottleEnabledSettingObserver extends ContentObserver {
        private boolean mThrottleEnabled = true;

        ThrottleEnabledSettingObserver(Handler handler) {
            super(handler);
        }

        public void initialize() {
            ScanRequestProxy.this.mFrameworkFacade.registerContentObserver(ScanRequestProxy.this.mContext, Settings.Global.getUriFor("wifi_scan_throttle_enabled"), true, this);
            this.mThrottleEnabled = getValue();
            if (ScanRequestProxy.this.mVerboseLoggingEnabled) {
                Log.v(ScanRequestProxy.TAG, "Scan throttle enabled " + this.mThrottleEnabled);
            }
        }

        public boolean isEnabled() {
            return this.mThrottleEnabled;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            this.mThrottleEnabled = getValue();
            Log.i(ScanRequestProxy.TAG, "Scan throttle enabled " + this.mThrottleEnabled);
        }

        private boolean getValue() {
            return ScanRequestProxy.this.mFrameworkFacade.getIntegerSetting(ScanRequestProxy.this.mContext, "wifi_scan_throttle_enabled", 1) == 1;
        }
    }

    ScanRequestProxy(Context context, AppOpsManager appOpsManager, ActivityManager activityManager, WifiInjector wifiInjector, WifiConfigManager configManager, WifiPermissionsUtil wifiPermissionUtil, WifiMetrics wifiMetrics, Clock clock, FrameworkFacade frameworkFacade, Handler handler) {
        this.mContext = context;
        this.mAppOps = appOpsManager;
        this.mActivityManager = activityManager;
        this.mWifiInjector = wifiInjector;
        this.mWifiConfigManager = configManager;
        this.mWifiPermissionsUtil = wifiPermissionUtil;
        this.mWifiMetrics = wifiMetrics;
        this.mClock = clock;
        this.mFrameworkFacade = frameworkFacade;
        this.mThrottleEnabledSettingObserver = new ThrottleEnabledSettingObserver(handler);
        this.mHwScanRequestProxyEx = HwWifiServiceFactory.getHwScanRequestProxyEx(this, this.mContext, this.mWifiInjector);
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    private boolean retrieveWifiScannerIfNecessary() {
        if (this.mWifiScanner == null) {
            this.mWifiScanner = this.mWifiInjector.getWifiScanner();
            this.mThrottleEnabledSettingObserver.initialize();
            WifiScanner wifiScanner = this.mWifiScanner;
            if (wifiScanner != null) {
                wifiScanner.registerScanListener(new GlobalScanListener());
            }
        }
        return this.mWifiScanner != null;
    }

    private void sendScanAvailableBroadcast(Context context, boolean available) {
        Log.i(TAG, "Sending scan available broadcast: " + available);
        Intent intent = new Intent("wifi_scan_available");
        intent.addFlags(67108864);
        if (available) {
            intent.putExtra("scan_enabled", 3);
        } else {
            intent.putExtra("scan_enabled", 1);
        }
        context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void enableScanningInternal(boolean enable) {
        if (!retrieveWifiScannerIfNecessary()) {
            Log.e(TAG, "Failed to retrieve wifiscanner");
            return;
        }
        this.mWifiScanner.setScanningEnabled(enable);
        sendScanAvailableBroadcast(this.mContext, enable);
        clearScanResults();
        StringBuilder sb = new StringBuilder();
        sb.append("Scanning is ");
        sb.append(enable ? "enabled" : "disabled");
        Log.i(TAG, sb.toString());
    }

    public void enableScanning(boolean enable, boolean enableScanningForHiddenNetworks) {
        if (enable) {
            enableScanningInternal(true);
            this.mScanningForHiddenNetworksEnabled = enableScanningForHiddenNetworks;
            StringBuilder sb = new StringBuilder();
            sb.append("Scanning for hidden networks is ");
            sb.append(enableScanningForHiddenNetworks ? "enabled" : "disabled");
            Log.i(TAG, sb.toString());
        } else {
            enableScanningInternal(false);
        }
        this.mScanningEnabled = enable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendScanResultBroadcast(boolean scanSucceeded) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", scanSucceeded);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void sendScanResultFailureBroadcastToPackage(String packageName) {
        Intent intent = new Intent("android.net.wifi.SCAN_RESULTS");
        intent.addFlags(67108864);
        intent.putExtra("resultsUpdated", false);
        intent.setPackage(packageName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void trimPastScanRequestTimesForForegroundApp(List<Long> scanRequestTimestamps, long currentTimeMillis) {
        Iterator<Long> timestampsIter = scanRequestTimestamps.iterator();
        while (timestampsIter.hasNext() && currentTimeMillis - timestampsIter.next().longValue() > 120000) {
            timestampsIter.remove();
        }
    }

    private LinkedList<Long> getOrCreateScanRequestTimestampsForForegroundApp(int callingUid, String packageName) {
        Pair<Integer, String> uidAndPackageNamePair = Pair.create(Integer.valueOf(callingUid), packageName);
        LinkedList<Long> scanRequestTimestamps = this.mLastScanTimestampsForFgApps.get(uidAndPackageNamePair);
        if (scanRequestTimestamps != null) {
            return scanRequestTimestamps;
        }
        LinkedList<Long> scanRequestTimestamps2 = new LinkedList<>();
        this.mLastScanTimestampsForFgApps.put(uidAndPackageNamePair, scanRequestTimestamps2);
        return scanRequestTimestamps2;
    }

    private boolean shouldScanRequestBeThrottledForForegroundApp(int callingUid, String packageName) {
        LinkedList<Long> scanRequestTimestamps = getOrCreateScanRequestTimestampsForForegroundApp(callingUid, packageName);
        long currentTimeMillis = this.mClock.getElapsedSinceBootMillis();
        trimPastScanRequestTimesForForegroundApp(scanRequestTimestamps, currentTimeMillis);
        if (scanRequestTimestamps.size() >= 4) {
            return true;
        }
        scanRequestTimestamps.addLast(Long.valueOf(currentTimeMillis));
        return false;
    }

    private boolean shouldScanRequestBeThrottledForBackgroundApp() {
        long lastScanMs = this.mLastScanTimestampForBgApps;
        long elapsedRealtime = this.mClock.getElapsedSinceBootMillis();
        if (lastScanMs != 0 && elapsedRealtime - lastScanMs < 1800000) {
            return true;
        }
        this.mLastScanTimestampForBgApps = elapsedRealtime;
        return false;
    }

    private boolean isRequestFromBackground(int callingUid, String packageName) {
        this.mAppOps.checkPackage(callingUid, packageName);
        try {
            return this.mActivityManager.getPackageImportance(packageName) > 125;
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to check the app state", e);
            return true;
        }
    }

    private boolean shouldScanRequestBeThrottledForApp(int callingUid, String packageName) {
        boolean isThrottled = false;
        if (packageName == null || packageName.length() == 0 || !isRequestFromBackground(callingUid, packageName)) {
            boolean isWhiteListPackage = false;
            String[] strArr = SCAN_WHITE_PACKAGE_NAME_LIST;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(packageName)) {
                    Log.i(TAG, packageName + "is white list package");
                    isWhiteListPackage = true;
                    isThrottled = false;
                    break;
                } else {
                    i++;
                }
            }
            if (!isWhiteListPackage) {
                isThrottled = shouldScanRequestBeThrottledForForegroundApp(callingUid, packageName);
            }
            if (isThrottled) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Foreground scan app request [" + callingUid + ", " + packageName + "]");
                }
                this.mWifiMetrics.incrementExternalForegroundAppOneshotScanRequestsThrottledCount();
            }
        } else {
            isThrottled = shouldScanRequestBeThrottledForBackgroundApp();
            if (isThrottled) {
                if (this.mVerboseLoggingEnabled) {
                    Log.v(TAG, "Background scan app request [" + callingUid + ", " + packageName + "]");
                }
                this.mWifiMetrics.incrementExternalBackgroundAppOneshotScanRequestsThrottledCount();
            }
        }
        this.mWifiMetrics.incrementExternalAppOneshotScanRequestsCount();
        return isThrottled;
    }

    public boolean startScan(int callingUid, String packageName) {
        this.mStartTime = SystemClock.elapsedRealtime();
        return startScanForSpecBand(callingUid, packageName, 7);
    }

    public boolean startScanForSpecBand(int callingUid, String packageName, int band) {
        if (!retrieveWifiScannerIfNecessary()) {
            Log.e(TAG, "Failed to retrieve wifiscanner");
            sendScanResultFailureBroadcastToPackage(packageName);
            return false;
        }
        boolean fromSettingsOrSetupWizard = this.mWifiPermissionsUtil.checkNetworkSettingsPermission(callingUid) || this.mWifiPermissionsUtil.checkNetworkSetupWizardPermission(callingUid);
        if (fromSettingsOrSetupWizard || !this.mThrottleEnabledSettingObserver.isEnabled() || !shouldScanRequestBeThrottledForApp(callingUid, packageName)) {
            WorkSource workSource = new WorkSource(callingUid, packageName);
            WifiScanner.ScanSettings settings = new WifiScanner.ScanSettings();
            if (fromSettingsOrSetupWizard) {
                settings.type = 2;
            }
            settings.band = 7;
            if (band >= 0 && band <= 7) {
                settings.band = band;
            }
            settings.reportEvents = 3;
            if (this.mScanningForHiddenNetworksEnabled) {
                List<WifiScanner.ScanSettings.HiddenNetwork> hiddenNetworkList = this.mWifiConfigManager.retrieveHiddenNetworkList();
                hiddenNetworkList.addAll(this.mWifiInjector.getWifiNetworkSuggestionsManager().retrieveHiddenNetworkList());
                settings.hiddenNetworks = (WifiScanner.ScanSettings.HiddenNetwork[]) hiddenNetworkList.toArray(new WifiScanner.ScanSettings.HiddenNetwork[hiddenNetworkList.size()]);
            }
            ScanRequestProxyScanListener scanRequestProxyScanListener = new ScanRequestProxyScanListener();
            if ("com.huawei.lbs".equals(packageName) && band == 1) {
                scanRequestProxyScanListener.setQuickTtffScan();
            }
            this.mWifiScanner.startScan(settings, scanRequestProxyScanListener, workSource);
            return true;
        }
        Log.i(TAG, "Scan request from " + packageName + " throttled");
        sendScanResultFailureBroadcastToPackage(packageName);
        return false;
    }

    public List<ScanResult> getScanResults() {
        synchronized (this) {
            if (this.mLastScanResults.isEmpty()) {
                List<ScanResult> scanResults = this.mHistoryScanResults.isEmpty() ? this.mSpecifiedChannelScanResults : this.mHistoryScanResults;
                StringBuilder sb = new StringBuilder();
                sb.append("getScanResults, mLastScanResults is empty return ");
                sb.append(this.mHistoryScanResults.isEmpty() ? "mSpecifiedChannelScanResults" : "mHistoryScanResults");
                Log.i(TAG, sb.toString());
                List<ScanResult> filterScanResult = (List) scanResults.stream().filter(new Predicate(this.mClock.getElapsedSinceBootMillis()) {
                    /* class com.android.server.wifi.$$Lambda$ScanRequestProxy$W_2_wLzw4IJ7ql1p_TDitBXMO1A */
                    private final /* synthetic */ long f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return ScanRequestProxy.lambda$getScanResults$0(this.f$0, (ScanResult) obj);
                    }
                }).collect(Collectors.toList());
                return filterScanResult.isEmpty() ? scanResults : filterScanResult;
            }
            return this.mLastScanResults;
        }
    }

    static /* synthetic */ boolean lambda$getScanResults$0(long currentTimeInMillis, ScanResult ScanResult) {
        return currentTimeInMillis - (ScanResult.timestamp / 1000) < 30000;
    }

    public List<ScanResult> getHistoryScanResults() {
        List<ScanResult> list;
        synchronized (this) {
            list = this.mHistoryScanResults;
        }
        return list;
    }

    public List<ScanResult> getSpecifiedChannelScanResults() {
        List<ScanResult> list;
        synchronized (this) {
            list = this.mSpecifiedChannelScanResults;
        }
        return list;
    }

    public void clearScanResults() {
        synchronized (this) {
            if (!this.mLastScanResults.isEmpty()) {
                this.mHistoryScanResults.clear();
                this.mHistoryScanResults.addAll(this.mLastScanResults);
            }
            this.mLastScanResults.clear();
        }
        this.mLastScanTimestampForBgApps = 0;
        this.mLastScanTimestampsForFgApps.clear();
    }

    public void update24GhzScanResults(ScanResult[] scanResults) {
        if (!(this.mLastScanResults == null || scanResults == null)) {
            synchronized (this) {
                Iterator<ScanResult> iterator = this.mLastScanResults.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().is24GHz()) {
                        iterator.remove();
                    }
                }
                for (ScanResult scanResult : scanResults) {
                    if (scanResult.is24GHz()) {
                        this.mLastScanResults.add(scanResult);
                    }
                }
            }
        }
    }

    public void clearScanRequestTimestampsForApp(String packageName, int uid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Clearing scan request timestamps for uid=" + uid + ", packageName=" + packageName);
        }
        this.mLastScanTimestampsForFgApps.remove(Pair.create(Integer.valueOf(uid), packageName));
    }

    @Override // com.android.server.wifi.IHwScanRequestProxyInner
    public boolean hwRetrieveWifiScannerIfNecessary() {
        return retrieveWifiScannerIfNecessary();
    }

    @Override // com.android.server.wifi.IHwScanRequestProxyInner
    public Context getContext() {
        return this.mContext;
    }

    @Override // com.android.server.wifi.IHwScanRequestProxyInner
    public WifiScanner getWifiScanner() {
        return this.mWifiScanner;
    }

    @Override // com.android.server.wifi.IHwScanRequestProxyInner
    public List<ScanResult> getScanResult() {
        List<ScanResult> list;
        synchronized (this) {
            list = this.mLastScanResults;
        }
        return list;
    }

    public void updateEvaluateScanResult() {
        synchronized (this) {
            if (this.mHwScanRequestProxyEx != null) {
                this.mHwScanRequestProxyEx.updateScanResultByWifiPro(this.mLastScanResults);
            }
        }
    }

    private void notifyScanFailedInfo(int reason) {
        int wpa2ApCnt = 0;
        int saeApCnt = 0;
        int oweApCnt = 0;
        int duration = (int) this.mScanDuration;
        List<ScanResult> list = this.mLastScanResults;
        if (!(list == null || this.mHwWifiCHRService == null)) {
            for (ScanResult scanResult : list) {
                String capabilities = scanResult.capabilities;
                if (capabilities != null) {
                    if (capabilities.contains(ENCRYPTION_TYPE_SAE)) {
                        saeApCnt++;
                    } else if (capabilities.contains(ENCRYPTION_TYPE_OWE)) {
                        oweApCnt++;
                    } else {
                        wpa2ApCnt++;
                    }
                }
            }
            Bundle data = new Bundle();
            data.putInt(SCAN_ERROR_CODE, reason);
            data.putInt(SCAN_DURATION, duration);
            data.putInt(SCAN_WPA2_AP_COUNT, wpa2ApCnt);
            data.putInt(SCAN_SAE_AP_COUNT, saeApCnt);
            data.putInt(SCAN_OWE_AP_COUNT, oweApCnt);
            Log.d(TAG, "ready to upload scan failed info");
            this.mHwWifiCHRService.uploadDFTEvent(22, data);
        }
    }
}
