package com.android.server.wifi;

import android.content.Context;
import android.database.ContentObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WakeupConfigStoreData;
import com.android.server.wifi.hwUtil.StringUtilEx;
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
    @VisibleForTesting
    static final long LAST_DISCONNECT_TIMEOUT_MILLIS = 5000;
    private static final String TAG = "WakeupController";
    private static final boolean USE_PLATFORM_WIFI_WAKE = true;
    private final Clock mClock;
    private final ContentObserver mContentObserver;
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final Handler mHandler;
    private boolean mIsActive = false;
    private ScanResultMatchInfo mLastDisconnectInfo;
    private long mLastDisconnectTimestampMillis;
    private int mNumScansHandled = 0;
    private final WifiScanner.ScanListener mScanListener = new WifiScanner.ScanListener() {
        /* class com.android.server.wifi.WakeupController.AnonymousClass1 */

        public void onPeriodChanged(int periodInMs) {
        }

        public void onResults(WifiScanner.ScanData[] results) {
            boolean isFullBandScanResults = results[0].getBandScanned() == 7 || results[0].getBandScanned() == 3;
            if (results.length == 1 && isFullBandScanResults) {
                WakeupController wakeupController = WakeupController.this;
                wakeupController.handleScanResults(wakeupController.filterDfsScanResults(Arrays.asList(results[0].getResults())));
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
    private final WakeupOnboarding mWakeupOnboarding;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;
    private final WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private final WifiWakeMetrics mWifiWakeMetrics;
    private boolean mWifiWakeupEnabled;

    public WakeupController(Context context, Looper looper, WakeupLock wakeupLock, WakeupEvaluator wakeupEvaluator, WakeupOnboarding wakeupOnboarding, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore, WifiNetworkSuggestionsManager wifiNetworkSuggestionsManager, WifiWakeMetrics wifiWakeMetrics, WifiInjector wifiInjector, FrameworkFacade frameworkFacade, Clock clock) {
        this.mContext = context;
        this.mHandler = new Handler(looper);
        this.mWakeupLock = wakeupLock;
        this.mWakeupEvaluator = wakeupEvaluator;
        this.mWakeupOnboarding = wakeupOnboarding;
        this.mWifiConfigManager = wifiConfigManager;
        this.mWifiNetworkSuggestionsManager = wifiNetworkSuggestionsManager;
        this.mWifiWakeMetrics = wifiWakeMetrics;
        this.mFrameworkFacade = frameworkFacade;
        this.mWifiInjector = wifiInjector;
        this.mContentObserver = new ContentObserver(this.mHandler) {
            /* class com.android.server.wifi.WakeupController.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                WakeupController.this.readWifiWakeupEnabledFromSettings();
                WakeupController.this.mWakeupOnboarding.setOnboarded();
            }
        };
        this.mFrameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_wakeup_enabled"), true, this.mContentObserver);
        readWifiWakeupEnabledFromSettings();
        this.mWakeupConfigStoreData = new WakeupConfigStoreData(new IsActiveDataSource(), this.mWakeupOnboarding.getIsOnboadedDataSource(), this.mWakeupOnboarding.getNotificationsDataSource(), this.mWakeupLock.getDataSource());
        wifiConfigStore.registerStoreData(this.mWakeupConfigStoreData);
        this.mClock = clock;
        this.mLastDisconnectTimestampMillis = 0;
        this.mLastDisconnectInfo = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void readWifiWakeupEnabledFromSettings() {
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

    public void setLastDisconnectInfo(ScanResultMatchInfo scanResultMatchInfo) {
        if (this.mIsActive) {
            Log.e(TAG, "Unexpected setLastDisconnectInfo when WakeupController is active!");
        } else if (scanResultMatchInfo == null) {
            Log.e(TAG, "Unexpected setLastDisconnectInfo(null)");
        } else {
            this.mLastDisconnectTimestampMillis = this.mClock.getElapsedSinceBootMillis();
            this.mLastDisconnectInfo = scanResultMatchInfo;
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "mLastDisconnectInfo set to " + scanResultMatchInfo);
            }
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
            matchInfos.retainAll(getGoodSavedNetworksAndSuggestions());
            long now = this.mClock.getElapsedSinceBootMillis();
            ScanResultMatchInfo scanResultMatchInfo = this.mLastDisconnectInfo;
            if (scanResultMatchInfo != null && now - this.mLastDisconnectTimestampMillis <= 5000) {
                matchInfos.add(scanResultMatchInfo);
                if (this.mVerboseLoggingEnabled) {
                    Log.d(TAG, "Added last connected network to lock: " + this.mLastDisconnectInfo);
                }
            }
            if (this.mVerboseLoggingEnabled) {
                Log.d(TAG, "Saved networks in most recent scan:" + matchInfos);
            }
            this.mWifiWakeMetrics.recordStartEvent(matchInfos.size());
            this.mWakeupLock.setLock(matchInfos);
        }
    }

    public void stop() {
        Log.d(TAG, "stop()");
        this.mLastDisconnectTimestampMillis = 0;
        this.mLastDisconnectInfo = null;
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
    /* access modifiers changed from: public */
    private List<ScanResult> filterDfsScanResults(Collection<ScanResult> scanResults) {
        int[] dfsChannels = this.mWifiInjector.getWifiNative().getChannelsForBand(4);
        if (dfsChannels == null) {
            dfsChannels = new int[0];
        }
        return (List) scanResults.stream().filter(new Predicate((Set) Arrays.stream(dfsChannels).boxed().collect(Collectors.toSet())) {
            /* class com.android.server.wifi.$$Lambda$WakeupController$sB8N4NPbyfefFu6fc4L75U1Md4E */
            private final /* synthetic */ Set f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return WakeupController.lambda$filterDfsScanResults$0(this.f$0, (ScanResult) obj);
            }
        }).collect(Collectors.toList());
    }

    static /* synthetic */ boolean lambda$filterDfsScanResults$0(Set dfsChannelSet, ScanResult scanResult) {
        return !dfsChannelSet.contains(Integer.valueOf(scanResult.frequency));
    }

    private Set<ScanResultMatchInfo> getGoodSavedNetworksAndSuggestions() {
        List<WifiConfiguration> savedNetworks = this.mWifiConfigManager.getSavedNetworks(1010);
        Set<ScanResultMatchInfo> goodNetworks = new HashSet<>(savedNetworks.size());
        for (WifiConfiguration config : savedNetworks) {
            if (!isWideAreaNetwork(config) && !config.hasNoInternetAccess() && !config.noInternetAccessExpected && config.getNetworkSelectionStatus().getHasEverConnected()) {
                goodNetworks.add(ScanResultMatchInfo.fromWifiConfiguration(config));
            }
        }
        for (WifiNetworkSuggestion suggestion : this.mWifiNetworkSuggestionsManager.getAllNetworkSuggestions()) {
            goodNetworks.add(ScanResultMatchInfo.fromWifiConfiguration(suggestion.wifiConfiguration));
        }
        return goodNetworks;
    }

    private static boolean isWideAreaNetwork(WifiConfiguration config) {
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanResults(Collection<ScanResult> scanResults) {
        ScanResult network;
        if (!isEnabled()) {
            Log.d(TAG, "Attempted to handleScanResults while not enabled");
            return;
        }
        this.mNumScansHandled++;
        if (this.mVerboseLoggingEnabled) {
            Log.d(TAG, "Incoming scan #" + this.mNumScansHandled);
        }
        this.mWakeupOnboarding.maybeShowNotification();
        Set<ScanResultMatchInfo> goodNetworks = getGoodSavedNetworksAndSuggestions();
        Set<ScanResultMatchInfo> matchInfos = toMatchInfos(scanResults);
        matchInfos.retainAll(goodNetworks);
        this.mWakeupLock.update(matchInfos);
        if (this.mWakeupLock.isUnlocked() && (network = this.mWakeupEvaluator.findViableNetwork(scanResults, goodNetworks)) != null) {
            Log.d(TAG, "Enabling wifi for network: " + StringUtilEx.safeDisplaySsid(network.SSID));
            enableWifi();
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

    private class IsActiveDataSource implements WakeupConfigStoreData.DataSource<Boolean> {
        private IsActiveDataSource() {
        }

        @Override // com.android.server.wifi.WakeupConfigStoreData.DataSource
        public Boolean getData() {
            return Boolean.valueOf(WakeupController.this.mIsActive);
        }

        public void setData(Boolean data) {
            WakeupController.this.mIsActive = data.booleanValue();
        }
    }
}
