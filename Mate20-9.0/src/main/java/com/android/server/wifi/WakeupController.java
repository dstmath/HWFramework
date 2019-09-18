package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WakeupConfigStoreData;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WakeupController {
    private static final String TAG = "WakeupController";
    private static final boolean USE_PLATFORM_WIFI_WAKE = true;
    private final ContentObserver mContentObserver;
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mIsActive = false;
    private int mNumScansHandled = 0;
    private final WifiScanner.ScanListener mScanListener = new WifiScanner.ScanListener() {
        public void onPeriodChanged(int periodInMs) {
        }

        public void onResults(WifiScanner.ScanData[] results) {
            if (results.length == 1 && results[0].isAllChannelsScanned()) {
                WakeupController.this.handleScanResults(WakeupController.this.filterDfsScanResults(Arrays.asList(results[0].getResults())));
            }
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onSuccess() {
        }

        public void onFailure(int reason, String description) {
            Log.e(WakeupController.TAG, "ScanListener onFailure: " + reason + ": " + description);
        }
    };
    private boolean mVerboseLoggingEnabled;
    private final WakeupConfigStoreData mWakeupConfigStoreData;
    private final WakeupEvaluator mWakeupEvaluator;
    private final WakeupLock mWakeupLock;
    /* access modifiers changed from: private */
    public final WakeupOnboarding mWakeupOnboarding;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;
    private final WifiWakeMetrics mWifiWakeMetrics;
    private boolean mWifiWakeupEnabled;

    private class IsActiveDataSource implements WakeupConfigStoreData.DataSource<Boolean> {
        private IsActiveDataSource() {
        }

        public Boolean getData() {
            return Boolean.valueOf(WakeupController.this.mIsActive);
        }

        public void setData(Boolean data) {
            boolean unused = WakeupController.this.mIsActive = data.booleanValue();
        }
    }

    public WakeupController(Context context, Looper looper, WakeupLock wakeupLock, WakeupEvaluator wakeupEvaluator, WakeupOnboarding wakeupOnboarding, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore, WifiWakeMetrics wifiWakeMetrics, WifiInjector wifiInjector, FrameworkFacade frameworkFacade) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mWakeupLock = wakeupLock;
        this.mWakeupEvaluator = wakeupEvaluator;
        this.mWakeupOnboarding = wakeupOnboarding;
        this.mWifiConfigManager = wifiConfigManager;
        this.mWifiWakeMetrics = wifiWakeMetrics;
        this.mFrameworkFacade = frameworkFacade;
        this.mWifiInjector = wifiInjector;
        this.mContentObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                WakeupController.this.readWifiWakeupEnabledFromSettings();
                WakeupController.this.mWakeupOnboarding.setOnboarded();
            }
        };
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_wakeup_enabled"), true, this.mContentObserver);
        readWifiWakeupEnabledFromSettings();
        this.mWakeupConfigStoreData = new WakeupConfigStoreData(new IsActiveDataSource(), this.mWakeupOnboarding.getIsOnboadedDataSource(), this.mWakeupOnboarding.getNotificationsDataSource(), this.mWakeupLock.getDataSource());
        wifiConfigStore.registerStoreData(this.mWakeupConfigStoreData);
    }

    /* access modifiers changed from: private */
    public void readWifiWakeupEnabledFromSettings() {
        boolean z = true;
        if (this.mFrameworkFacade.getIntegerSetting(this.mContext, "wifi_wakeup_enabled", 0) != 1) {
            z = false;
        }
        this.mWifiWakeupEnabled = z;
        this.mWifiWakeupEnabled = false;
        StringBuilder sb = new StringBuilder();
        sb.append("WifiWake ");
        sb.append(this.mWifiWakeupEnabled ? "enabled" : "disabled");
        Log.d(TAG, sb.toString());
    }

    private void setActive(boolean isActive) {
        if (this.mIsActive != isActive) {
            Log.d(TAG, "Setting active to " + isActive);
            this.mIsActive = isActive;
            this.mWifiConfigManager.saveToStore(false);
        }
    }

    public void start() {
        Log.d(TAG, "start()");
        this.mWifiInjector.getWifiScanner().registerScanListener(this.mScanListener);
        if (this.mIsActive) {
            this.mWifiWakeMetrics.recordIgnoredStart();
            return;
        }
        setActive(true);
        if (isEnabled()) {
            this.mWakeupOnboarding.maybeShowNotification();
            Set<ScanResultMatchInfo> matchInfos = toMatchInfos(filterDfsScanResults(this.mWifiInjector.getWifiScanner().getSingleScanResults()));
            matchInfos.retainAll(getGoodSavedNetworks());
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Saved networks in most recent scan:" + matchInfos);
            }
            this.mWifiWakeMetrics.recordStartEvent(matchInfos.size());
            this.mWakeupLock.setLock(matchInfos);
        }
    }

    public void stop() {
        Log.d(TAG, "stop()");
        this.mWifiInjector.getWifiScanner().deregisterScanListener(this.mScanListener);
        this.mWakeupOnboarding.onStop();
    }

    public void reset() {
        Log.d(TAG, "reset()");
        this.mWifiWakeMetrics.recordResetEvent(this.mNumScansHandled);
        this.mNumScansHandled = 0;
        setActive(false);
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
        this.mWakeupLock.enableVerboseLogging(this.mVerboseLoggingEnabled);
    }

    /* access modifiers changed from: private */
    public List<ScanResult> filterDfsScanResults(Collection<ScanResult> scanResults) {
        int[] dfsChannels = this.mWifiInjector.getWifiNative().getChannelsForBand(4);
        if (dfsChannels == null) {
            dfsChannels = new int[0];
        }
        return (List) scanResults.stream().filter(new Predicate((Set) Arrays.stream(dfsChannels).boxed().collect(Collectors.toSet())) {
            private final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return WakeupController.lambda$filterDfsScanResults$0(this.f$0, (ScanResult) obj);
            }
        }).collect(Collectors.toList());
    }

    static /* synthetic */ boolean lambda$filterDfsScanResults$0(Set dfsChannelSet, ScanResult scanResult) {
        return !dfsChannelSet.contains(Integer.valueOf(scanResult.frequency));
    }

    private Set<ScanResultMatchInfo> getGoodSavedNetworks() {
        List<WifiConfiguration> savedNetworks = this.mWifiConfigManager.getSavedNetworks();
        Set<ScanResultMatchInfo> goodSavedNetworks = new HashSet<>(savedNetworks.size());
        for (WifiConfiguration config : savedNetworks) {
            if (!isWideAreaNetwork(config) && !config.hasNoInternetAccess() && !config.noInternetAccessExpected && config.getNetworkSelectionStatus().getHasEverConnected()) {
                goodSavedNetworks.add(ScanResultMatchInfo.fromWifiConfiguration(config));
            }
        }
        return goodSavedNetworks;
    }

    private static boolean isWideAreaNetwork(WifiConfiguration config) {
        return false;
    }

    /* access modifiers changed from: private */
    public void handleScanResults(Collection<ScanResult> scanResults) {
        if (!isEnabled()) {
            Log.d(TAG, "Attempted to handleScanResults while not enabled");
            return;
        }
        this.mNumScansHandled++;
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Incoming scan #" + this.mNumScansHandled);
        }
        this.mWakeupOnboarding.maybeShowNotification();
        Set<ScanResultMatchInfo> goodSavedNetworks = getGoodSavedNetworks();
        Set<ScanResultMatchInfo> matchInfos = toMatchInfos(scanResults);
        matchInfos.retainAll(goodSavedNetworks);
        this.mWakeupLock.update(matchInfos);
        if (this.mWakeupLock.isUnlocked()) {
            if (this.mWakeupEvaluator.findViableNetwork(scanResults, goodSavedNetworks) != null) {
                Log.d(TAG, "Enabling wifi for network: " + network.SSID);
                enableWifi();
            }
        }
    }

    private static Set<ScanResultMatchInfo> toMatchInfos(Collection<ScanResult> scanResults) {
        return (Set) scanResults.stream().map($$Lambda$Sgsg9Ml_dxoj_SCBslbH6YHea8.INSTANCE).collect(Collectors.toSet());
    }

    private void enableWifi() {
        if (this.mWifiInjector.getWifiSettingsStore().handleWifiToggled(true)) {
            this.mWifiInjector.getWifiController().sendMessage(155656);
            this.mWifiWakeMetrics.recordWakeupEvent(this.mNumScansHandled);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isEnabled() {
        Log.d(TAG, "WifiWakeupEnabled " + this.mWifiWakeupEnabled);
        return this.mWifiWakeupEnabled && this.mWakeupConfigStoreData.hasBeenRead();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WakeupController");
        pw.println("USE_PLATFORM_WIFI_WAKE: true");
        pw.println("mWifiWakeupEnabled: " + this.mWifiWakeupEnabled);
        pw.println("isOnboarded: " + this.mWakeupOnboarding.isOnboarded());
        pw.println("configStore hasBeenRead: " + this.mWakeupConfigStoreData.hasBeenRead());
        pw.println("mIsActive: " + this.mIsActive);
        pw.println("mNumScansHandled: " + this.mNumScansHandled);
        this.mWakeupLock.dump(fd, pw, args);
    }
}
