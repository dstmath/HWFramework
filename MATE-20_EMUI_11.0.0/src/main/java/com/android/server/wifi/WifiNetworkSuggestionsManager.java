package com.android.server.wifi;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.net.MacAddress;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.server.wifi.NetworkSuggestionStoreData;
import com.android.server.wifi.WifiNetworkSuggestionsManager;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class WifiNetworkSuggestionsManager {
    @VisibleForTesting
    public static final String EXTRA_PACKAGE_NAME = "com.android.server.wifi.extra.NetworkSuggestion.PACKAGE_NAME";
    @VisibleForTesting
    public static final String EXTRA_UID = "com.android.server.wifi.extra.NetworkSuggestion.UID";
    @VisibleForTesting
    public static final String NOTIFICATION_USER_ALLOWED_APP_INTENT_ACTION = "com.android.server.wifi.action.NetworkSuggestion.USER_ALLOWED_APP";
    @VisibleForTesting
    public static final String NOTIFICATION_USER_DISALLOWED_APP_INTENT_ACTION = "com.android.server.wifi.action.NetworkSuggestion.USER_DISALLOWED_APP";
    @VisibleForTesting
    public static final String NOTIFICATION_USER_DISMISSED_INTENT_ACTION = "com.android.server.wifi.action.NetworkSuggestion.USER_DISMISSED";
    private static final String TAG = "WifiNetworkSuggestionsManager";
    private Set<ExtendedWifiNetworkSuggestion> mActiveNetworkSuggestionsMatchingConnection;
    private final Map<String, PerAppInfo> mActiveNetworkSuggestionsPerApp = new HashMap();
    private final Map<Pair<ScanResultMatchInfo, MacAddress>, Set<ExtendedWifiNetworkSuggestion>> mActiveScanResultMatchInfoWithBssid = new HashMap();
    private final Map<ScanResultMatchInfo, Set<ExtendedWifiNetworkSuggestion>> mActiveScanResultMatchInfoWithNoBssid = new HashMap();
    private final AppOpsManager mAppOps;
    private final Map<String, AppOpsChangedListener> mAppOpsChangedListenerPerApp = new HashMap();
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.WifiNetworkSuggestionsManager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String packageName = intent.getStringExtra(WifiNetworkSuggestionsManager.EXTRA_PACKAGE_NAME);
                if (packageName == null) {
                    Log.e(WifiNetworkSuggestionsManager.TAG, "No package name found in intent");
                    return;
                }
                char c = 65535;
                int uid = intent.getIntExtra(WifiNetworkSuggestionsManager.EXTRA_UID, -1);
                if (uid == -1) {
                    Log.e(WifiNetworkSuggestionsManager.TAG, "No uid found in intent");
                    return;
                }
                String action = intent.getAction();
                int hashCode = action.hashCode();
                if (hashCode != -536575146) {
                    if (hashCode != -73571697) {
                        if (hashCode == 902585168 && action.equals(WifiNetworkSuggestionsManager.NOTIFICATION_USER_ALLOWED_APP_INTENT_ACTION)) {
                            c = 0;
                        }
                    } else if (action.equals(WifiNetworkSuggestionsManager.NOTIFICATION_USER_DISMISSED_INTENT_ACTION)) {
                        c = 2;
                    }
                } else if (action.equals(WifiNetworkSuggestionsManager.NOTIFICATION_USER_DISALLOWED_APP_INTENT_ACTION)) {
                    c = 1;
                }
                if (c == 0) {
                    Log.i(WifiNetworkSuggestionsManager.TAG, "User clicked to allow app");
                    WifiNetworkSuggestionsManager.this.setHasUserApprovedForApp(true, packageName);
                } else if (c == 1) {
                    Log.i(WifiNetworkSuggestionsManager.TAG, "User clicked to disallow app");
                    WifiNetworkSuggestionsManager.this.setHasUserApprovedForApp(false, packageName);
                    WifiNetworkSuggestionsManager.this.mAppOps.setMode(71, uid, packageName, 1);
                } else if (c != 2) {
                    Log.e(WifiNetworkSuggestionsManager.TAG, "Unknown action " + intent.getAction());
                    return;
                } else {
                    Log.i(WifiNetworkSuggestionsManager.TAG, "User dismissed the notification");
                    WifiNetworkSuggestionsManager.this.mUserApprovalNotificationActive = false;
                    return;
                }
                WifiNetworkSuggestionsManager.this.mUserApprovalNotificationActive = false;
                WifiNetworkSuggestionsManager.this.mNotificationManager.cancel(51);
            }
        }
    };
    private final Context mContext;
    private final FrameworkFacade mFrameworkFacade;
    private final Handler mHandler;
    private boolean mHasNewDataToSerialize = false;
    private final IntentFilter mIntentFilter;
    private final NotificationManager mNotificationManager;
    private final PackageManager mPackageManager;
    private final Resources mResources;
    private boolean mUserApprovalNotificationActive = false;
    private String mUserApprovalNotificationPackageName;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiInjector mWifiInjector;
    private final WifiKeyStore mWifiKeyStore;
    private final WifiMetrics mWifiMetrics;
    private final WifiPermissionsUtil mWifiPermissionsUtil;

    public static class PerAppInfo {
        public final Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = new HashSet();
        public boolean hasUserApproved = false;
        public int maxSize = 0;
        public final String packageName;

        public PerAppInfo(String packageName2) {
            this.packageName = packageName2;
        }

        public boolean equals(Object other) {
            if (other == null || !(other instanceof PerAppInfo)) {
                return false;
            }
            PerAppInfo otherPerAppInfo = (PerAppInfo) other;
            if (!TextUtils.equals(this.packageName, otherPerAppInfo.packageName) || !Objects.equals(this.extNetworkSuggestions, otherPerAppInfo.extNetworkSuggestions) || this.hasUserApproved != otherPerAppInfo.hasUserApproved) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.packageName, this.extNetworkSuggestions, Boolean.valueOf(this.hasUserApproved));
        }
    }

    public static class ExtendedWifiNetworkSuggestion {
        public final PerAppInfo perAppInfo;
        public final WifiNetworkSuggestion wns;

        public ExtendedWifiNetworkSuggestion(WifiNetworkSuggestion wns2, PerAppInfo perAppInfo2) {
            this.wns = wns2;
            this.perAppInfo = perAppInfo2;
            this.wns.wifiConfiguration.fromWifiNetworkSuggestion = true;
            this.wns.wifiConfiguration.ephemeral = true;
            this.wns.wifiConfiguration.creatorName = perAppInfo2.packageName;
            this.wns.wifiConfiguration.creatorUid = wns2.suggestorUid;
        }

        public int hashCode() {
            return Objects.hash(this.wns);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ExtendedWifiNetworkSuggestion)) {
                return false;
            }
            return this.wns.equals(((ExtendedWifiNetworkSuggestion) obj).wns);
        }

        public String toString() {
            return "Extended" + this.wns.toString();
        }

        public static ExtendedWifiNetworkSuggestion fromWns(WifiNetworkSuggestion wns2, PerAppInfo perAppInfo2) {
            return new ExtendedWifiNetworkSuggestion(wns2, perAppInfo2);
        }
    }

    /* access modifiers changed from: private */
    public final class AppOpsChangedListener implements AppOpsManager.OnOpChangedListener {
        private final String mPackageName;
        private final int mUid;

        AppOpsChangedListener(String packageName, int uid) {
            this.mPackageName = packageName;
            this.mUid = uid;
        }

        @Override // android.app.AppOpsManager.OnOpChangedListener
        public void onOpChanged(String op, String packageName) {
            WifiNetworkSuggestionsManager.this.mHandler.post(new Runnable(packageName, op) {
                /* class com.android.server.wifi.$$Lambda$WifiNetworkSuggestionsManager$AppOpsChangedListener$mLsxU8gQBSdf1SUD58OpJgPte0 */
                private final /* synthetic */ String f$1;
                private final /* synthetic */ String f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiNetworkSuggestionsManager.AppOpsChangedListener.this.lambda$onOpChanged$0$WifiNetworkSuggestionsManager$AppOpsChangedListener(this.f$1, this.f$2);
                }
            });
        }

        public /* synthetic */ void lambda$onOpChanged$0$WifiNetworkSuggestionsManager$AppOpsChangedListener(String packageName, String op) {
            if (this.mPackageName.equals(packageName) && "android:change_wifi_state".equals(op)) {
                try {
                    WifiNetworkSuggestionsManager.this.mAppOps.checkPackage(this.mUid, this.mPackageName);
                    if (WifiNetworkSuggestionsManager.this.mAppOps.unsafeCheckOpNoThrow("android:change_wifi_state", this.mUid, this.mPackageName) == 1) {
                        Log.i(WifiNetworkSuggestionsManager.TAG, "User disallowed change wifi state for " + packageName);
                        WifiNetworkSuggestionsManager.this.removeApp(this.mPackageName);
                    }
                } catch (SecurityException e) {
                    Log.wtf(WifiNetworkSuggestionsManager.TAG, "Invalid uid/package" + packageName);
                }
            }
        }
    }

    private class NetworkSuggestionDataSource implements NetworkSuggestionStoreData.DataSource {
        private NetworkSuggestionDataSource() {
        }

        @Override // com.android.server.wifi.NetworkSuggestionStoreData.DataSource
        public Map<String, PerAppInfo> toSerialize() {
            WifiNetworkSuggestionsManager.this.mHasNewDataToSerialize = false;
            return WifiNetworkSuggestionsManager.this.mActiveNetworkSuggestionsPerApp;
        }

        @Override // com.android.server.wifi.NetworkSuggestionStoreData.DataSource
        public void fromDeserialized(Map<String, PerAppInfo> networkSuggestionsMap) {
            WifiNetworkSuggestionsManager.this.mActiveNetworkSuggestionsPerApp.putAll(networkSuggestionsMap);
            for (Map.Entry<String, PerAppInfo> entry : networkSuggestionsMap.entrySet()) {
                String packageName = entry.getKey();
                Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = entry.getValue().extNetworkSuggestions;
                if (!extNetworkSuggestions.isEmpty()) {
                    WifiNetworkSuggestionsManager.this.startTrackingAppOpsChange(packageName, extNetworkSuggestions.iterator().next().wns.suggestorUid);
                }
                WifiNetworkSuggestionsManager.this.addToScanResultMatchInfoMap(extNetworkSuggestions);
            }
        }

        @Override // com.android.server.wifi.NetworkSuggestionStoreData.DataSource
        public void reset() {
            WifiNetworkSuggestionsManager.this.mActiveNetworkSuggestionsPerApp.clear();
            WifiNetworkSuggestionsManager.this.mActiveScanResultMatchInfoWithBssid.clear();
            WifiNetworkSuggestionsManager.this.mActiveScanResultMatchInfoWithNoBssid.clear();
        }

        @Override // com.android.server.wifi.NetworkSuggestionStoreData.DataSource
        public boolean hasNewDataToSerialize() {
            return WifiNetworkSuggestionsManager.this.mHasNewDataToSerialize;
        }
    }

    public WifiNetworkSuggestionsManager(Context context, Handler handler, WifiInjector wifiInjector, WifiPermissionsUtil wifiPermissionsUtil, WifiConfigManager wifiConfigManager, WifiConfigStore wifiConfigStore, WifiMetrics wifiMetrics, WifiKeyStore keyStore) {
        this.mContext = context;
        this.mResources = context.getResources();
        this.mHandler = handler;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mPackageManager = context.getPackageManager();
        this.mWifiInjector = wifiInjector;
        this.mFrameworkFacade = this.mWifiInjector.getFrameworkFacade();
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mWifiConfigManager = wifiConfigManager;
        this.mWifiMetrics = wifiMetrics;
        this.mWifiKeyStore = keyStore;
        wifiConfigStore.registerStoreData(wifiInjector.makeNetworkSuggestionStoreData(new NetworkSuggestionDataSource()));
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction(NOTIFICATION_USER_ALLOWED_APP_INTENT_ACTION);
        this.mIntentFilter.addAction(NOTIFICATION_USER_DISALLOWED_APP_INTENT_ACTION);
        this.mIntentFilter.addAction(NOTIFICATION_USER_DISMISSED_INTENT_ACTION);
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    private void saveToStore() {
        this.mHasNewDataToSerialize = true;
        if (!this.mWifiConfigManager.saveToStore(true)) {
            Log.w(TAG, "Failed to save to store");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addToScanResultMatchInfoMap(Collection<ExtendedWifiNetworkSuggestion> extNetworkSuggestions) {
        Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestionsForScanResultMatchInfo;
        for (ExtendedWifiNetworkSuggestion extNetworkSuggestion : extNetworkSuggestions) {
            ScanResultMatchInfo scanResultMatchInfo = ScanResultMatchInfo.fromWifiConfiguration(extNetworkSuggestion.wns.wifiConfiguration);
            if (!TextUtils.isEmpty(extNetworkSuggestion.wns.wifiConfiguration.BSSID)) {
                Pair<ScanResultMatchInfo, MacAddress> lookupPair = Pair.create(scanResultMatchInfo, MacAddress.fromString(extNetworkSuggestion.wns.wifiConfiguration.BSSID));
                extNetworkSuggestionsForScanResultMatchInfo = this.mActiveScanResultMatchInfoWithBssid.get(lookupPair);
                if (extNetworkSuggestionsForScanResultMatchInfo == null) {
                    extNetworkSuggestionsForScanResultMatchInfo = new HashSet();
                    this.mActiveScanResultMatchInfoWithBssid.put(lookupPair, extNetworkSuggestionsForScanResultMatchInfo);
                }
            } else {
                extNetworkSuggestionsForScanResultMatchInfo = this.mActiveScanResultMatchInfoWithNoBssid.get(scanResultMatchInfo);
                if (extNetworkSuggestionsForScanResultMatchInfo == null) {
                    extNetworkSuggestionsForScanResultMatchInfo = new HashSet();
                    this.mActiveScanResultMatchInfoWithNoBssid.put(scanResultMatchInfo, extNetworkSuggestionsForScanResultMatchInfo);
                }
            }
            extNetworkSuggestionsForScanResultMatchInfo.add(extNetworkSuggestion);
        }
    }

    private void removeFromScanResultMatchInfoMap(Collection<ExtendedWifiNetworkSuggestion> extNetworkSuggestions) {
        for (ExtendedWifiNetworkSuggestion extNetworkSuggestion : extNetworkSuggestions) {
            ScanResultMatchInfo scanResultMatchInfo = ScanResultMatchInfo.fromWifiConfiguration(extNetworkSuggestion.wns.wifiConfiguration);
            if (!TextUtils.isEmpty(extNetworkSuggestion.wns.wifiConfiguration.BSSID)) {
                Pair<ScanResultMatchInfo, MacAddress> lookupPair = Pair.create(scanResultMatchInfo, MacAddress.fromString(extNetworkSuggestion.wns.wifiConfiguration.BSSID));
                Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestionsForScanResultMatchInfo = this.mActiveScanResultMatchInfoWithBssid.get(lookupPair);
                if (extNetworkSuggestionsForScanResultMatchInfo == null) {
                    Log.wtf(TAG, "No scan result match info found.");
                }
                extNetworkSuggestionsForScanResultMatchInfo.remove(extNetworkSuggestion);
                if (extNetworkSuggestionsForScanResultMatchInfo.isEmpty()) {
                    this.mActiveScanResultMatchInfoWithBssid.remove(lookupPair);
                }
            } else {
                Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestionsForScanResultMatchInfo2 = this.mActiveScanResultMatchInfoWithNoBssid.get(scanResultMatchInfo);
                if (extNetworkSuggestionsForScanResultMatchInfo2 == null) {
                    Log.wtf(TAG, "No scan result match info found.");
                }
                extNetworkSuggestionsForScanResultMatchInfo2.remove(extNetworkSuggestion);
                if (extNetworkSuggestionsForScanResultMatchInfo2.isEmpty()) {
                    this.mActiveScanResultMatchInfoWithNoBssid.remove(scanResultMatchInfo);
                }
            }
        }
    }

    private void triggerDisconnectIfServingNetworkSuggestionRemoved(Collection<ExtendedWifiNetworkSuggestion> extNetworkSuggestionsRemoved) {
        Set<ExtendedWifiNetworkSuggestion> set = this.mActiveNetworkSuggestionsMatchingConnection;
        if (set != null && !set.isEmpty() && this.mActiveNetworkSuggestionsMatchingConnection.removeAll(extNetworkSuggestionsRemoved) && this.mActiveNetworkSuggestionsMatchingConnection.isEmpty()) {
            Log.i(TAG, "Only network suggestion matching the connected network removed. Disconnecting...");
            this.mWifiInjector.getClientModeImpl().disconnectCommand();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startTrackingAppOpsChange(String packageName, int uid) {
        AppOpsChangedListener appOpsChangedListener = new AppOpsChangedListener(packageName, uid);
        this.mAppOps.startWatchingMode("android:change_wifi_state", packageName, appOpsChangedListener);
        this.mAppOpsChangedListenerPerApp.put(packageName, appOpsChangedListener);
    }

    private Set<ExtendedWifiNetworkSuggestion> convertToExtendedWnsSet(Collection<WifiNetworkSuggestion> networkSuggestions, PerAppInfo perAppInfo) {
        return (Set) networkSuggestions.stream().collect(Collectors.mapping(new Function() {
            /* class com.android.server.wifi.$$Lambda$WifiNetworkSuggestionsManager$RNZY2kB1hLpsPZOEPwmRjLR4Y */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return WifiNetworkSuggestionsManager.ExtendedWifiNetworkSuggestion.fromWns((WifiNetworkSuggestion) obj, WifiNetworkSuggestionsManager.PerAppInfo.this);
            }
        }, Collectors.toSet()));
    }

    private Set<WifiNetworkSuggestion> convertToWnsSet(Collection<ExtendedWifiNetworkSuggestion> extNetworkSuggestions) {
        return (Set) extNetworkSuggestions.stream().collect(Collectors.mapping($$Lambda$WifiNetworkSuggestionsManager$q6nTN6x3SZ6JCW81VsNsJktzhC8.INSTANCE, Collectors.toSet()));
    }

    public int add(List<WifiNetworkSuggestion> networkSuggestions, int uid, String packageName) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Adding " + networkSuggestions.size() + " networks from " + packageName);
        }
        if (networkSuggestions.isEmpty()) {
            Log.w(TAG, "Empty list of network suggestions for " + packageName + ". Ignoring");
            return 0;
        }
        PerAppInfo perAppInfo = this.mActiveNetworkSuggestionsPerApp.get(packageName);
        if (perAppInfo == null) {
            perAppInfo = new PerAppInfo(packageName);
            this.mActiveNetworkSuggestionsPerApp.put(packageName, perAppInfo);
            if (this.mWifiPermissionsUtil.checkNetworkCarrierProvisioningPermission(uid)) {
                Log.i(TAG, "Setting the carrier provisioning app approved");
                perAppInfo.hasUserApproved = true;
            }
        }
        Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = convertToExtendedWnsSet(networkSuggestions, perAppInfo);
        if (!Collections.disjoint(perAppInfo.extNetworkSuggestions, extNetworkSuggestions)) {
            Log.e(TAG, "Failed to add network suggestions for " + packageName + ". Modification of active network suggestions disallowed");
            return 3;
        } else if (perAppInfo.extNetworkSuggestions.size() + extNetworkSuggestions.size() > WifiManager.NETWORK_SUGGESTIONS_MAX_PER_APP) {
            Log.e(TAG, "Failed to add network suggestions for " + packageName + ". Exceeds max per app, current list size: " + perAppInfo.extNetworkSuggestions.size() + ", new list size: " + extNetworkSuggestions.size());
            return 4;
        } else {
            if (perAppInfo.extNetworkSuggestions.isEmpty()) {
                startTrackingAppOpsChange(packageName, uid);
            }
            Iterator<ExtendedWifiNetworkSuggestion> iterator = extNetworkSuggestions.iterator();
            while (iterator.hasNext()) {
                WifiConfiguration config = iterator.next().wns.wifiConfiguration;
                if (config.isEnterprise() && !this.mWifiKeyStore.updateNetworkKeys(config, null)) {
                    Log.e(TAG, "Enterprise network install failure for SSID: " + StringUtilEx.safeDisplaySsid(config.SSID));
                    iterator.remove();
                }
            }
            perAppInfo.extNetworkSuggestions.addAll(extNetworkSuggestions);
            perAppInfo.maxSize = Math.max(perAppInfo.extNetworkSuggestions.size(), perAppInfo.maxSize);
            addToScanResultMatchInfoMap(extNetworkSuggestions);
            saveToStore();
            this.mWifiMetrics.incrementNetworkSuggestionApiNumModification();
            this.mWifiMetrics.noteNetworkSuggestionApiListSizeHistogram(getAllMaxSizes());
            return 0;
        }
    }

    private void stopTrackingAppOpsChange(String packageName) {
        AppOpsChangedListener appOpsChangedListener = this.mAppOpsChangedListenerPerApp.remove(packageName);
        if (appOpsChangedListener == null) {
            Log.wtf(TAG, "No app ops listener found for " + packageName);
            return;
        }
        this.mAppOps.stopWatchingMode(appOpsChangedListener);
    }

    private void removeInternal(Collection<ExtendedWifiNetworkSuggestion> extNetworkSuggestions, String packageName, PerAppInfo perAppInfo) {
        Set<ExtendedWifiNetworkSuggestion> removingSuggestions = new HashSet<>(perAppInfo.extNetworkSuggestions);
        if (!extNetworkSuggestions.isEmpty()) {
            removingSuggestions.retainAll(extNetworkSuggestions);
            perAppInfo.extNetworkSuggestions.removeAll(extNetworkSuggestions);
        } else {
            new HashSet(perAppInfo.extNetworkSuggestions);
            perAppInfo.extNetworkSuggestions.clear();
        }
        if (perAppInfo.extNetworkSuggestions.isEmpty()) {
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "No active suggestions for " + packageName);
            }
            stopTrackingAppOpsChange(packageName);
        }
        for (ExtendedWifiNetworkSuggestion ewns : removingSuggestions) {
            WifiConfiguration config = ewns.wns.wifiConfiguration;
            if (config.isEnterprise()) {
                this.mWifiKeyStore.removeKeys(config.enterpriseConfig);
            }
        }
        removeFromScanResultMatchInfoMap(removingSuggestions);
    }

    public int remove(List<WifiNetworkSuggestion> networkSuggestions, String packageName) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Removing " + networkSuggestions.size() + " networks from " + packageName);
        }
        PerAppInfo perAppInfo = this.mActiveNetworkSuggestionsPerApp.get(packageName);
        if (perAppInfo == null) {
            Log.e(TAG, "Failed to remove network suggestions for " + packageName + ". No network suggestions found");
            return 5;
        }
        Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = convertToExtendedWnsSet(networkSuggestions, perAppInfo);
        if (extNetworkSuggestions.isEmpty() || perAppInfo.extNetworkSuggestions.containsAll(extNetworkSuggestions)) {
            removeInternal(extNetworkSuggestions, packageName, perAppInfo);
            saveToStore();
            this.mWifiMetrics.incrementNetworkSuggestionApiNumModification();
            this.mWifiMetrics.noteNetworkSuggestionApiListSizeHistogram(getAllMaxSizes());
            return 0;
        }
        Log.e(TAG, "Failed to remove network suggestions for " + packageName + ". Network suggestions not found in active network suggestions");
        return 5;
    }

    public void removeApp(String packageName) {
        PerAppInfo perAppInfo = this.mActiveNetworkSuggestionsPerApp.get(packageName);
        if (perAppInfo != null) {
            triggerDisconnectIfServingNetworkSuggestionRemoved(perAppInfo.extNetworkSuggestions);
            removeInternal(Collections.EMPTY_LIST, packageName, perAppInfo);
            this.mActiveNetworkSuggestionsPerApp.remove(packageName);
            saveToStore();
            Log.i(TAG, "Removed " + packageName);
        }
    }

    public void clear() {
        Iterator<Map.Entry<String, PerAppInfo>> iter = this.mActiveNetworkSuggestionsPerApp.entrySet().iterator();
        triggerDisconnectIfServingNetworkSuggestionRemoved(this.mActiveNetworkSuggestionsMatchingConnection);
        while (iter.hasNext()) {
            Map.Entry<String, PerAppInfo> entry = iter.next();
            removeInternal(Collections.EMPTY_LIST, entry.getKey(), entry.getValue());
            iter.remove();
        }
        saveToStore();
        Log.i(TAG, "Cleared all internal state");
    }

    public boolean hasUserApprovedForApp(String packageName) {
        PerAppInfo perAppInfo = this.mActiveNetworkSuggestionsPerApp.get(packageName);
        if (perAppInfo == null) {
            return false;
        }
        return perAppInfo.hasUserApproved;
    }

    public void setHasUserApprovedForApp(boolean approved, String packageName) {
        PerAppInfo perAppInfo = this.mActiveNetworkSuggestionsPerApp.get(packageName);
        if (perAppInfo != null) {
            if (this.mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("Setting the app ");
                sb.append(approved ? "approved" : "not approved");
                Log.v(TAG, sb.toString());
            }
            perAppInfo.hasUserApproved = approved;
            saveToStore();
        }
    }

    @VisibleForTesting
    public Set<WifiNetworkSuggestion> getAllNetworkSuggestions() {
        return (Set) this.mActiveNetworkSuggestionsPerApp.values().stream().flatMap(new Function() {
            /* class com.android.server.wifi.$$Lambda$WifiNetworkSuggestionsManager$zRzhL8Qzz0iRhKGcHw_pVpLTTCg */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return WifiNetworkSuggestionsManager.this.lambda$getAllNetworkSuggestions$2$WifiNetworkSuggestionsManager((WifiNetworkSuggestionsManager.PerAppInfo) obj);
            }
        }).collect(Collectors.toSet());
    }

    public /* synthetic */ Stream lambda$getAllNetworkSuggestions$2$WifiNetworkSuggestionsManager(PerAppInfo e) {
        return convertToWnsSet(e.extNetworkSuggestions).stream();
    }

    private List<Integer> getAllMaxSizes() {
        return (List) this.mActiveNetworkSuggestionsPerApp.values().stream().map($$Lambda$WifiNetworkSuggestionsManager$uARHVaEQ04Ye8cbmrdW2VQF5c.INSTANCE).collect(Collectors.toList());
    }

    private PendingIntent getPrivateBroadcast(String action, String packageName, int uid) {
        return this.mFrameworkFacade.getBroadcast(this.mContext, 0, new Intent(action).setPackage(WifiCommonUtils.PACKAGE_NAME_FRAMEWORK).putExtra(EXTRA_PACKAGE_NAME, packageName).putExtra(EXTRA_UID, uid), 134217728);
    }

    private CharSequence getAppName(String packageName) {
        try {
            CharSequence appName = this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(packageName, 0));
            if (appName != null) {
                return appName;
            }
            return "";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to find app name for " + packageName);
            return "";
        }
    }

    private void sendUserApprovalNotification(String packageName, int uid) {
        Notification.Action userAllowAppNotificationAction = new Notification.Action.Builder((Icon) null, this.mResources.getText(17041567), getPrivateBroadcast(NOTIFICATION_USER_ALLOWED_APP_INTENT_ACTION, packageName, uid)).build();
        this.mNotificationManager.notify(51, new Notification.Builder(this.mContext, SystemNotificationChannels.NETWORK_STATUS).setSmallIcon(17303543).setTicker(this.mResources.getString(17041570)).setContentTitle(this.mResources.getString(17041570)).setContentText(this.mResources.getString(17041569, getAppName(packageName))).setDeleteIntent(getPrivateBroadcast(NOTIFICATION_USER_DISMISSED_INTENT_ACTION, packageName, uid)).setShowWhen(false).setLocalOnly(true).setColor(this.mResources.getColor(17170460, this.mContext.getTheme())).addAction(userAllowAppNotificationAction).addAction(new Notification.Action.Builder((Icon) null, this.mResources.getText(17041568), getPrivateBroadcast(NOTIFICATION_USER_DISALLOWED_APP_INTENT_ACTION, packageName, uid)).build()).build());
        this.mUserApprovalNotificationActive = true;
        this.mUserApprovalNotificationPackageName = packageName;
    }

    private boolean sendUserApprovalNotificationIfNotApproved(PerAppInfo perAppInfo, WifiNetworkSuggestion matchingSuggestion) {
        if (perAppInfo.hasUserApproved) {
            return false;
        }
        Log.i(TAG, "Sending user approval notification for " + perAppInfo.packageName);
        sendUserApprovalNotification(perAppInfo.packageName, matchingSuggestion.suggestorUid);
        return true;
    }

    private Set<ExtendedWifiNetworkSuggestion> getNetworkSuggestionsForScanResultMatchInfo(ScanResultMatchInfo scanResultMatchInfo, MacAddress bssid) {
        Set<ExtendedWifiNetworkSuggestion> matchingExtNetworkSuggestionsWithBssid;
        Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = new HashSet<>();
        if (!(bssid == null || (matchingExtNetworkSuggestionsWithBssid = this.mActiveScanResultMatchInfoWithBssid.get(Pair.create(scanResultMatchInfo, bssid))) == null)) {
            extNetworkSuggestions.addAll(matchingExtNetworkSuggestionsWithBssid);
        }
        Set<ExtendedWifiNetworkSuggestion> matchingNetworkSuggestionsWithNoBssid = this.mActiveScanResultMatchInfoWithNoBssid.get(scanResultMatchInfo);
        if (matchingNetworkSuggestionsWithNoBssid != null) {
            extNetworkSuggestions.addAll(matchingNetworkSuggestionsWithNoBssid);
        }
        if (extNetworkSuggestions.isEmpty()) {
            return null;
        }
        return extNetworkSuggestions;
    }

    public Set<WifiNetworkSuggestion> getNetworkSuggestionsForScanDetail(ScanDetail scanDetail) {
        ScanResult scanResult = scanDetail.getScanResult();
        if (scanResult == null) {
            Log.e(TAG, "No scan result found in scan detail");
            return null;
        }
        Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = null;
        try {
            extNetworkSuggestions = getNetworkSuggestionsForScanResultMatchInfo(ScanResultMatchInfo.fromScanResult(scanResult), MacAddress.fromString(scanResult.BSSID));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to lookup network from scan result match info map", e);
        }
        if (extNetworkSuggestions == null) {
            return null;
        }
        Set<ExtendedWifiNetworkSuggestion> approvedExtNetworkSuggestions = (Set) extNetworkSuggestions.stream().filter($$Lambda$WifiNetworkSuggestionsManager$VZi4a9MMz0x_1KiQWZ0XwDSoj4.INSTANCE).collect(Collectors.toSet());
        if (!this.mUserApprovalNotificationActive && approvedExtNetworkSuggestions.size() != extNetworkSuggestions.size()) {
            for (ExtendedWifiNetworkSuggestion extNetworkSuggestion : extNetworkSuggestions) {
                if (sendUserApprovalNotificationIfNotApproved(extNetworkSuggestion.perAppInfo, extNetworkSuggestion.wns)) {
                    break;
                }
            }
        }
        if (approvedExtNetworkSuggestions.isEmpty()) {
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "getNetworkSuggestionsForScanDetail Found " + approvedExtNetworkSuggestions + " for " + StringUtilEx.safeDisplaySsid(scanResult.SSID) + "[" + scanResult.capabilities + "]");
        }
        return convertToWnsSet(approvedExtNetworkSuggestions);
    }

    private Set<ExtendedWifiNetworkSuggestion> getNetworkSuggestionsForWifiConfiguration(WifiConfiguration wifiConfiguration, String bssid) {
        Set<ExtendedWifiNetworkSuggestion> extNetworkSuggestions = null;
        try {
            extNetworkSuggestions = getNetworkSuggestionsForScanResultMatchInfo(ScanResultMatchInfo.fromWifiConfiguration(wifiConfiguration), bssid == null ? null : MacAddress.fromString(bssid));
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to lookup network from scan result match info map", e);
        }
        if (extNetworkSuggestions == null) {
            return null;
        }
        Set<ExtendedWifiNetworkSuggestion> approvedExtNetworkSuggestions = (Set) extNetworkSuggestions.stream().filter($$Lambda$WifiNetworkSuggestionsManager$NCSgMx5AU5TMrknU5s9bzw5LWc.INSTANCE).collect(Collectors.toSet());
        if (approvedExtNetworkSuggestions.isEmpty()) {
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "getNetworkSuggestionsFoWifiConfiguration Found " + approvedExtNetworkSuggestions + " for " + wifiConfiguration.SSID + "[" + wifiConfiguration.allowedKeyManagement + "]");
        }
        return approvedExtNetworkSuggestions;
    }

    private void sendPostConnectionBroadcast(String packageName, WifiNetworkSuggestion networkSuggestion) {
        Intent intent = new Intent("android.net.wifi.action.WIFI_NETWORK_SUGGESTION_POST_CONNECTION");
        intent.putExtra("android.net.wifi.extra.NETWORK_SUGGESTION", networkSuggestion);
        intent.setPackage(packageName);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.getUserHandleForUid(networkSuggestion.suggestorUid));
    }

    private void sendPostConnectionBroadcastIfAllowed(String packageName, WifiNetworkSuggestion matchingSuggestion) {
        try {
            this.mWifiPermissionsUtil.enforceCanAccessScanResults(packageName, matchingSuggestion.suggestorUid);
            if (this.mVerboseLoggingEnabled) {
                Log.v(TAG, "Sending post connection broadcast to " + packageName);
            }
            sendPostConnectionBroadcast(packageName, matchingSuggestion);
        } catch (SecurityException e) {
        }
    }

    private void handleConnectionSuccess(WifiConfiguration connectedNetwork, String connectedBssid) {
        Set<ExtendedWifiNetworkSuggestion> matchingExtNetworkSuggestions = getNetworkSuggestionsForWifiConfiguration(connectedNetwork, connectedBssid);
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Network suggestions matching the connection " + matchingExtNetworkSuggestions);
        }
        if (!(matchingExtNetworkSuggestions == null || matchingExtNetworkSuggestions.isEmpty())) {
            this.mWifiMetrics.incrementNetworkSuggestionApiNumConnectSuccess();
            this.mActiveNetworkSuggestionsMatchingConnection = new HashSet(matchingExtNetworkSuggestions);
            Set<ExtendedWifiNetworkSuggestion> matchingExtNetworkSuggestionsWithReqAppInteraction = (Set) matchingExtNetworkSuggestions.stream().filter($$Lambda$WifiNetworkSuggestionsManager$aCg0WttFDDZf8QB522s95VRR5qs.INSTANCE).collect(Collectors.toSet());
            if (matchingExtNetworkSuggestionsWithReqAppInteraction.size() != 0) {
                for (ExtendedWifiNetworkSuggestion matchingExtNetworkSuggestion : matchingExtNetworkSuggestionsWithReqAppInteraction) {
                    sendPostConnectionBroadcastIfAllowed(matchingExtNetworkSuggestion.perAppInfo.packageName, matchingExtNetworkSuggestion.wns);
                }
            }
        }
    }

    private void handleConnectionFailure(WifiConfiguration network, String bssid) {
        Set<ExtendedWifiNetworkSuggestion> matchingExtNetworkSuggestions = getNetworkSuggestionsForWifiConfiguration(network, bssid);
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "Network suggestions matching the connection failure " + matchingExtNetworkSuggestions);
        }
        if (matchingExtNetworkSuggestions != null && !matchingExtNetworkSuggestions.isEmpty()) {
            this.mWifiMetrics.incrementNetworkSuggestionApiNumConnectFailure();
        }
    }

    private void resetConnectionState() {
        this.mActiveNetworkSuggestionsMatchingConnection = null;
    }

    public void handleConnectionAttemptEnded(int failureCode, WifiConfiguration network, String bssid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "handleConnectionAttemptEnded " + failureCode + ", " + network);
        }
        resetConnectionState();
        if (failureCode == 1) {
            handleConnectionSuccess(network, bssid);
        } else {
            handleConnectionFailure(network, bssid);
        }
    }

    public void handleDisconnect(WifiConfiguration network, String bssid) {
        if (this.mVerboseLoggingEnabled) {
            Log.v(TAG, "handleDisconnect " + network);
        }
        resetConnectionState();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Dump of WifiNetworkSuggestionsManager");
        pw.println("WifiNetworkSuggestionsManager - Networks Begin ----");
        for (Map.Entry<String, PerAppInfo> networkSuggestionsEntry : this.mActiveNetworkSuggestionsPerApp.entrySet()) {
            pw.println("Package Name: " + networkSuggestionsEntry.getKey());
            PerAppInfo appInfo = networkSuggestionsEntry.getValue();
            pw.println("Has user approved: " + appInfo.hasUserApproved);
            Iterator<ExtendedWifiNetworkSuggestion> it = appInfo.extNetworkSuggestions.iterator();
            while (it.hasNext()) {
                pw.println("Network: " + it.next());
            }
        }
        pw.println("WifiNetworkSuggestionsManager - Networks End ----");
        pw.println("WifiNetworkSuggestionsManager - Network Suggestions matching connection: " + this.mActiveNetworkSuggestionsMatchingConnection);
    }
}
