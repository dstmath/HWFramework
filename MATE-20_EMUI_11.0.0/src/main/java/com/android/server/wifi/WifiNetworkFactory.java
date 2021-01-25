package com.android.server.wifi;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.MacAddress;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.INetworkRequestUserSelectionCallback;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiScanner;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.wifi.NetworkRequestStoreData;
import com.android.server.wifi.WifiNetworkFactory;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.ExternalCallbackTracker;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class WifiNetworkFactory extends NetworkFactory {
    private static final int LTE_HAS_INTERNET_SCORE = 50;
    private static final int LTE_NO_INTERNET_SCORE = 10;
    @VisibleForTesting
    public static final int NETWORK_CONNECTION_TIMEOUT_MS = 30000;
    @VisibleForTesting
    public static final int PERIODIC_SCAN_INTERVAL_MS = 10000;
    @VisibleForTesting
    private static final int SCORE_FILTER = 60;
    private static final String TAG = "WifiNetworkFactory";
    @VisibleForTesting
    public static final String UI_START_INTENT_ACTION = "com.android.settings.wifi.action.NETWORK_REQUEST";
    @VisibleForTesting
    public static final String UI_START_INTENT_CATEGORY = "android.intent.category.DEFAULT";
    @VisibleForTesting
    public static final String UI_START_INTENT_EXTRA_APP_NAME = "com.android.settings.wifi.extra.APP_NAME";
    @VisibleForTesting
    public static final String UI_START_INTENT_EXTRA_REQUEST_IS_FOR_SINGLE_NETWORK = "com.android.settings.wifi.extra.REQUEST_IS_FOR_SINGLE_NETWORK";
    @VisibleForTesting
    public static final int USER_SELECTED_NETWORK_CONNECT_RETRY_MAX = 3;
    private List<ScanResult> mActiveMatchedScanResults;
    private NetworkRequest mActiveSpecificNetworkRequest;
    private WifiNetworkSpecifier mActiveSpecificNetworkRequestSpecifier;
    private final ActivityManager mActivityManager;
    private final AlarmManager mAlarmManager;
    private final AppOpsManager mAppOpsManager;
    private final Clock mClock;
    private NetworkRequest mConnectedSpecificNetworkRequest;
    private WifiNetworkSpecifier mConnectedSpecificNetworkRequestSpecifier;
    private final ConnectionTimeoutAlarmListener mConnectionTimeoutAlarmListener;
    private boolean mConnectionTimeoutSet = false;
    private final Context mContext;
    private int mGenericConnectionReqCount = 0;
    private final Handler mHandler;
    private boolean mHasNewDataToSerialize = false;
    private boolean mIsPeriodicScanPaused = false;
    private int mLastScore = 0;
    private final Handler.Callback mNetworkConnectionTriggerCallback = new Handler.Callback() {
        /* class com.android.server.wifi.$$Lambda$WifiNetworkFactory$FWScVL5YZolV22WpOQkmaAHmpU */

        @Override // android.os.Handler.Callback
        public final boolean handleMessage(Message message) {
            return WifiNetworkFactory.this.lambda$new$0$WifiNetworkFactory(message);
        }
    };
    private boolean mPendingConnectionSuccess = false;
    private final PeriodicScanAlarmListener mPeriodicScanTimerListener;
    private boolean mPeriodicScanTimerSet = false;
    private final ExternalCallbackTracker<INetworkRequestMatchCallback> mRegisteredCallbacks;
    private final NetworkFactoryScanListener mScanListener;
    private final WifiScanner.ScanSettings mScanSettings;
    private final Messenger mSrcMessenger;
    private final Map<String, Set<AccessPoint>> mUserApprovedAccessPointMap = new HashMap();
    private WifiConfiguration mUserSelectedNetwork;
    private int mUserSelectedNetworkConnectRetryCount;
    private boolean mVerboseLoggingEnabled = false;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiConfigStore mWifiConfigStore;
    private final WifiConnectivityManager mWifiConnectivityManager;
    private boolean mWifiEnabled = false;
    private final WifiInjector mWifiInjector;
    private final WifiMetrics mWifiMetrics;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiScanner mWifiScanner;

    public static class AccessPoint {
        public final MacAddress bssid;
        public final int networkType;
        public final String ssid;

        AccessPoint(String ssid2, MacAddress bssid2, int networkType2) {
            this.ssid = ssid2;
            this.bssid = bssid2;
            this.networkType = networkType2;
        }

        public int hashCode() {
            return Objects.hash(this.ssid, this.bssid, Integer.valueOf(this.networkType));
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AccessPoint)) {
                return false;
            }
            AccessPoint other = (AccessPoint) obj;
            if (!TextUtils.equals(this.ssid, other.ssid) || !Objects.equals(this.bssid, other.bssid) || this.networkType != other.networkType) {
                return false;
            }
            return true;
        }

        public String toString() {
            return "AccessPoint: " + this.ssid + ", " + this.bssid + ", " + this.networkType;
        }
    }

    /* access modifiers changed from: private */
    public class NetworkFactoryScanListener implements WifiScanner.ScanListener {
        private NetworkFactoryScanListener() {
        }

        public void onSuccess() {
            if (WifiNetworkFactory.this.mVerboseLoggingEnabled) {
                Log.i(WifiNetworkFactory.TAG, "Scan request succeeded");
            }
        }

        public void onFailure(int reason, String description) {
            Log.e(WifiNetworkFactory.TAG, "Scan failure received. reason: " + reason + ", description: " + description);
            WifiNetworkFactory.this.scheduleNextPeriodicScan();
        }

        public void onResults(WifiScanner.ScanData[] scanDatas) {
            if (WifiNetworkFactory.this.mVerboseLoggingEnabled) {
                Log.i(WifiNetworkFactory.TAG, "Scan results received");
            }
            if (scanDatas.length != 1) {
                Log.wtf(WifiNetworkFactory.TAG, "Found more than 1 batch of scan results, Ignoring...");
                return;
            }
            ScanResult[] scanResults = scanDatas[0].getResults();
            if (WifiNetworkFactory.this.mVerboseLoggingEnabled) {
                Log.i(WifiNetworkFactory.TAG, "Received " + scanResults.length + " scan results");
            }
            List<ScanResult> matchedScanResults = WifiNetworkFactory.this.getNetworksMatchingActiveNetworkRequest(scanResults);
            if (WifiNetworkFactory.this.mActiveMatchedScanResults == null) {
                WifiNetworkFactory.this.mWifiMetrics.incrementNetworkRequestApiMatchSizeHistogram(matchedScanResults.size());
            }
            WifiNetworkFactory.this.mActiveMatchedScanResults = matchedScanResults;
            ScanResult approvedScanResult = null;
            if (WifiNetworkFactory.this.isActiveRequestForSingleAccessPoint()) {
                approvedScanResult = WifiNetworkFactory.this.findUserApprovedAccessPointForActiveRequestFromActiveMatchedScanResults();
            }
            if (approvedScanResult == null || WifiNetworkFactory.this.mWifiConfigManager.wasEphemeralNetworkDeleted(ScanResultUtil.createQuotedSSID(approvedScanResult.SSID))) {
                if (WifiNetworkFactory.this.mVerboseLoggingEnabled) {
                    Log.i(WifiNetworkFactory.TAG, "No approved access points found in matching scan results. Sending match callback");
                }
                WifiNetworkFactory.this.sendNetworkRequestMatchCallbacksForActiveRequest(matchedScanResults);
                WifiNetworkFactory.this.scheduleNextPeriodicScan();
                return;
            }
            Log.i(WifiNetworkFactory.TAG, "Approved access point found in matching scan results. Triggering connect " + approvedScanResult);
            WifiNetworkFactory.this.handleConnectToNetworkUserSelectionInternal(ScanResultUtil.createNetworkFromScanResult(approvedScanResult));
            WifiNetworkFactory.this.mWifiMetrics.incrementNetworkRequestApiNumUserApprovalBypass();
        }

        public void onFullResult(ScanResult fullScanResult) {
        }

        public void onPeriodChanged(int periodInMs) {
        }
    }

    /* access modifiers changed from: private */
    public class PeriodicScanAlarmListener implements AlarmManager.OnAlarmListener {
        private PeriodicScanAlarmListener() {
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            WifiNetworkFactory.this.startScan();
        }
    }

    /* access modifiers changed from: private */
    public class ConnectionTimeoutAlarmListener implements AlarmManager.OnAlarmListener {
        private ConnectionTimeoutAlarmListener() {
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            Log.e(WifiNetworkFactory.TAG, "Timed-out connecting to network");
            WifiNetworkFactory wifiNetworkFactory = WifiNetworkFactory.this;
            wifiNetworkFactory.handleNetworkConnectionFailure(wifiNetworkFactory.mUserSelectedNetwork);
        }
    }

    /* access modifiers changed from: private */
    public class NetworkFactoryUserSelectionCallback extends INetworkRequestUserSelectionCallback.Stub {
        private final NetworkRequest mNetworkRequest;

        NetworkFactoryUserSelectionCallback(NetworkRequest networkRequest) {
            this.mNetworkRequest = networkRequest;
        }

        public void select(WifiConfiguration wifiConfiguration) {
            WifiNetworkFactory.this.mHandler.post(new Runnable(wifiConfiguration) {
                /* class com.android.server.wifi.$$Lambda$WifiNetworkFactory$NetworkFactoryUserSelectionCallback$dR_8SWPminVWhl4XzfYZZwM_I0w */
                private final /* synthetic */ WifiConfiguration f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    WifiNetworkFactory.NetworkFactoryUserSelectionCallback.this.lambda$select$0$WifiNetworkFactory$NetworkFactoryUserSelectionCallback(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$select$0$WifiNetworkFactory$NetworkFactoryUserSelectionCallback(WifiConfiguration wifiConfiguration) {
            if (WifiNetworkFactory.this.mActiveSpecificNetworkRequest != this.mNetworkRequest) {
                Log.e(WifiNetworkFactory.TAG, "Stale callback select received");
            } else {
                WifiNetworkFactory.this.handleConnectToNetworkUserSelection(wifiConfiguration);
            }
        }

        public void reject() {
            WifiNetworkFactory.this.mHandler.post(new Runnable() {
                /* class com.android.server.wifi.$$Lambda$WifiNetworkFactory$NetworkFactoryUserSelectionCallback$ghzGnrxKK2lkDhd7NYfPGo8Eqtk */

                @Override // java.lang.Runnable
                public final void run() {
                    WifiNetworkFactory.NetworkFactoryUserSelectionCallback.this.lambda$reject$1$WifiNetworkFactory$NetworkFactoryUserSelectionCallback();
                }
            });
        }

        public /* synthetic */ void lambda$reject$1$WifiNetworkFactory$NetworkFactoryUserSelectionCallback() {
            if (WifiNetworkFactory.this.mActiveSpecificNetworkRequest != this.mNetworkRequest) {
                Log.e(WifiNetworkFactory.TAG, "Stale callback reject received");
            } else {
                WifiNetworkFactory.this.handleRejectUserSelection();
            }
        }
    }

    public /* synthetic */ boolean lambda$new$0$WifiNetworkFactory(Message msg) {
        switch (msg.what) {
            case 151554:
                Log.e(TAG, "Failed to trigger network connection");
                handleNetworkConnectionFailure(this.mUserSelectedNetwork);
                return true;
            case 151555:
                if (!this.mVerboseLoggingEnabled) {
                    return true;
                }
                Log.i(TAG, "Triggered network connection");
                return true;
            default:
                Log.e(TAG, "Unknown message " + msg.what);
                return true;
        }
    }

    private class NetworkRequestDataSource implements NetworkRequestStoreData.DataSource {
        private NetworkRequestDataSource() {
        }

        @Override // com.android.server.wifi.NetworkRequestStoreData.DataSource
        public Map<String, Set<AccessPoint>> toSerialize() {
            WifiNetworkFactory.this.mHasNewDataToSerialize = false;
            return WifiNetworkFactory.this.mUserApprovedAccessPointMap;
        }

        @Override // com.android.server.wifi.NetworkRequestStoreData.DataSource
        public void fromDeserialized(Map<String, Set<AccessPoint>> approvedAccessPointMap) {
            WifiNetworkFactory.this.mUserApprovedAccessPointMap.putAll(approvedAccessPointMap);
        }

        @Override // com.android.server.wifi.NetworkRequestStoreData.DataSource
        public void reset() {
            WifiNetworkFactory.this.mUserApprovedAccessPointMap.clear();
        }

        @Override // com.android.server.wifi.NetworkRequestStoreData.DataSource
        public boolean hasNewDataToSerialize() {
            return WifiNetworkFactory.this.mHasNewDataToSerialize;
        }
    }

    public WifiNetworkFactory(Looper looper, Context context, NetworkCapabilities nc, ActivityManager activityManager, AlarmManager alarmManager, AppOpsManager appOpsManager, Clock clock, WifiInjector wifiInjector, WifiConnectivityManager connectivityManager, WifiConfigManager configManager, WifiConfigStore configStore, WifiPermissionsUtil wifiPermissionsUtil, WifiMetrics wifiMetrics) {
        super(looper, context, TAG, nc);
        this.mContext = context;
        this.mActivityManager = activityManager;
        this.mAlarmManager = alarmManager;
        this.mAppOpsManager = appOpsManager;
        this.mClock = clock;
        this.mHandler = new Handler(looper);
        this.mWifiInjector = wifiInjector;
        this.mWifiConnectivityManager = connectivityManager;
        this.mWifiConfigManager = configManager;
        this.mWifiConfigStore = configStore;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mWifiMetrics = wifiMetrics;
        this.mScanSettings = new WifiScanner.ScanSettings();
        WifiScanner.ScanSettings scanSettings = this.mScanSettings;
        scanSettings.type = 2;
        scanSettings.band = 7;
        scanSettings.reportEvents = 1;
        this.mScanListener = new NetworkFactoryScanListener();
        this.mPeriodicScanTimerListener = new PeriodicScanAlarmListener();
        this.mConnectionTimeoutAlarmListener = new ConnectionTimeoutAlarmListener();
        this.mRegisteredCallbacks = new ExternalCallbackTracker<>(this.mHandler);
        this.mSrcMessenger = new Messenger(new Handler(looper, this.mNetworkConnectionTriggerCallback));
        configStore.registerStoreData(wifiInjector.makeNetworkRequestStoreData(new NetworkRequestDataSource()));
        setScoreFilter(60);
    }

    private void saveToStore() {
        this.mHasNewDataToSerialize = true;
        if (!this.mWifiConfigManager.saveToStore(true)) {
            Log.w(TAG, "Failed to save to store");
        }
    }

    public void enableVerboseLogging(int verbose) {
        this.mVerboseLoggingEnabled = verbose > 0;
    }

    public void addCallback(IBinder binder, INetworkRequestMatchCallback callback, int callbackIdentifier) {
        if (this.mActiveSpecificNetworkRequest == null) {
            Log.wtf(TAG, "No valid network request. Ignoring callback registration");
            try {
                callback.onAbort();
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to invoke network request abort callback " + callback, e);
            }
        } else if (!this.mRegisteredCallbacks.add(binder, callback, callbackIdentifier)) {
            Log.e(TAG, "Failed to add callback");
        } else {
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Adding callback. Num callbacks: " + this.mRegisteredCallbacks.getNumCallbacks());
            }
            try {
                callback.onUserSelectionCallbackRegistration(new NetworkFactoryUserSelectionCallback(this.mActiveSpecificNetworkRequest));
            } catch (RemoteException e2) {
                Log.e(TAG, "Unable to invoke user selection registration callback " + callback, e2);
            }
        }
    }

    public void removeCallback(int callbackIdentifier) {
        this.mRegisteredCallbacks.remove(callbackIdentifier);
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Removing callback. Num callbacks: " + this.mRegisteredCallbacks.getNumCallbacks());
        }
    }

    private boolean canNewRequestOverrideExistingRequest(WifiNetworkSpecifier newRequest, WifiNetworkSpecifier existingRequest) {
        if (existingRequest == null || this.mWifiPermissionsUtil.checkNetworkSettingsPermission(newRequest.requestorUid) || isRequestFromForegroundApp(newRequest.requestorPackageName) || !isRequestFromForegroundApp(existingRequest.requestorPackageName)) {
            return true;
        }
        Log.e(TAG, "Already processing request from a foreground app " + existingRequest.requestorPackageName + ". Rejecting request from " + newRequest.requestorPackageName);
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRequestWithNetworkSpecifierValid(NetworkRequest networkRequest) {
        if (!(networkRequest.networkCapabilities.getNetworkSpecifier() instanceof WifiNetworkSpecifier)) {
            Log.e(TAG, "Invalid network specifier mentioned. Rejecting");
            return false;
        } else if (!networkRequest.hasCapability(12)) {
            return true;
        } else {
            Log.e(TAG, "Request with wifi network specifier cannot contain NET_CAPABILITY_INTERNET. Rejecting");
            return false;
        }
    }

    public boolean acceptRequest(NetworkRequest networkRequest, int score) {
        NetworkSpecifier ns = networkRequest.networkCapabilities.getNetworkSpecifier();
        if (ns != null) {
            if (!isRequestWithNetworkSpecifierValid(networkRequest)) {
                releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
                return false;
            } else if (!this.mWifiEnabled) {
                Log.e(TAG, "Wifi off. Rejecting");
                return false;
            } else {
                WifiNetworkSpecifier wns = (WifiNetworkSpecifier) ns;
                if (!WifiConfigurationUtil.validateNetworkSpecifier(wns)) {
                    Log.e(TAG, "Invalid network specifier. Rejecting request from " + wns.requestorPackageName);
                    releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
                    return false;
                }
                try {
                    this.mAppOpsManager.checkPackage(wns.requestorUid, wns.requestorPackageName);
                    if (!this.mWifiPermissionsUtil.checkNetworkSettingsPermission(wns.requestorUid) && !isRequestFromForegroundAppOrService(wns.requestorPackageName)) {
                        Log.e(TAG, "Request not from foreground app or service. Rejecting request from " + wns.requestorPackageName);
                        releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
                        return false;
                    } else if (!canNewRequestOverrideExistingRequest(wns, this.mActiveSpecificNetworkRequestSpecifier)) {
                        Log.e(TAG, "Request cannot override active request. Rejecting request from " + wns.requestorPackageName);
                        releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
                        return false;
                    } else if (!canNewRequestOverrideExistingRequest(wns, this.mConnectedSpecificNetworkRequestSpecifier)) {
                        Log.e(TAG, "Request cannot override connected request. Rejecting request from " + wns.requestorPackageName);
                        releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
                        return false;
                    } else if (this.mVerboseLoggingEnabled) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Accepted network request with specifier from fg ");
                        sb.append(isRequestFromForegroundApp(wns.requestorPackageName) ? "app" : "service");
                        Log.i(TAG, sb.toString());
                    }
                } catch (SecurityException e) {
                    Log.e(TAG, "Invalid uid/package name " + wns.requestorPackageName + ", " + wns.requestorPackageName, e);
                    releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
                    return false;
                }
            }
        }
        if (!this.mVerboseLoggingEnabled) {
            return true;
        }
        Log.i(TAG, "Accepted network request " + networkRequest);
        return true;
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkScore(NetworkRequest networkRequest, int score) {
        boolean isNeedEnableAutoConnect;
        boolean isWifiConnected = false;
        if (this.mLastScore != score) {
            ClientModeImpl clientModeImpl = this.mWifiInjector.getClientModeImpl();
            if (clientModeImpl == null) {
                Log.e(TAG, "clientModeImpl is null");
                return;
            }
            NetworkInfo networkInfo = clientModeImpl.getNetworkInfo();
            if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                isWifiConnected = true;
            }
            if (!isWifiConnected || !(score == 0 || score == 50 || score == 10)) {
                if (!isWifiConnected && score == 101) {
                    isNeedEnableAutoConnect = true;
                } else if (score >= 60) {
                    isNeedEnableAutoConnect = false;
                } else {
                    isNeedEnableAutoConnect = true;
                }
                Log.i(TAG, "update score, old: " + this.mLastScore + ", new: " + score + ", need enable auto connect: " + isNeedEnableAutoConnect);
                this.mLastScore = score;
                if (isNeedEnableAutoConnect) {
                    this.mWifiConnectivityManager.setTrustedConnectionAllowed(true);
                } else {
                    this.mWifiConnectivityManager.setTrustedConnectionAllowed(false);
                }
            } else {
                Log.i(TAG, "wifi is connected, do not deal with LTE score, score: " + score);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void needNetworkFor(NetworkRequest networkRequest, int score) {
        NetworkSpecifier ns = networkRequest.networkCapabilities.getNetworkSpecifier();
        if (ns == null) {
            int i = this.mGenericConnectionReqCount + 1;
            this.mGenericConnectionReqCount = i;
            if (i == 1) {
                this.mWifiConnectivityManager.setTrustedConnectionAllowed(true);
            }
        } else if (!isRequestWithNetworkSpecifierValid(networkRequest)) {
            releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
        } else if (!this.mWifiEnabled) {
            Log.e(TAG, "Wifi off. Rejecting");
        } else {
            retrieveWifiScanner();
            setupForActiveRequest();
            this.mActiveSpecificNetworkRequest = new NetworkRequest(networkRequest);
            WifiNetworkSpecifier wns = (WifiNetworkSpecifier) ns;
            this.mActiveSpecificNetworkRequestSpecifier = new WifiNetworkSpecifier(wns.ssidPatternMatcher, wns.bssidPatternMatcher, wns.wifiConfiguration, wns.requestorUid, wns.requestorPackageName);
            this.mWifiMetrics.incrementNetworkRequestApiNumRequest();
            startUi();
            startPeriodicScans();
        }
    }

    /* access modifiers changed from: protected */
    public void releaseNetworkFor(NetworkRequest networkRequest) {
        NetworkSpecifier ns = networkRequest.networkCapabilities.getNetworkSpecifier();
        if (ns == null) {
            int i = this.mGenericConnectionReqCount;
            if (i == 0) {
                Log.e(TAG, "No valid network request to release");
                return;
            }
            int i2 = i - 1;
            this.mGenericConnectionReqCount = i2;
            if (i2 == 0) {
                this.mWifiConnectivityManager.setTrustedConnectionAllowed(false);
            }
        } else if (!(ns instanceof WifiNetworkSpecifier)) {
            Log.e(TAG, "Invalid network specifier mentioned. Ignoring");
        } else if (!this.mWifiEnabled) {
            Log.e(TAG, "Wifi off. Ignoring");
        } else if (this.mActiveSpecificNetworkRequest == null && this.mConnectedSpecificNetworkRequest == null) {
            Log.e(TAG, "Network release received with no active/connected request. Ignoring");
        } else if (Objects.equals(this.mActiveSpecificNetworkRequest, networkRequest)) {
            Log.i(TAG, "App released request, cancelling " + this.mActiveSpecificNetworkRequest);
            teardownForActiveRequest();
        } else if (Objects.equals(this.mConnectedSpecificNetworkRequest, networkRequest)) {
            Log.i(TAG, "App released request, cancelling " + this.mConnectedSpecificNetworkRequest);
            teardownForConnectedNetwork();
        } else {
            Log.e(TAG, "Network specifier does not match the active/connected request. Ignoring");
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        WifiNetworkFactory.super.dump(fd, pw, args);
        pw.println("WifiNetworkFactory: mGenericConnectionReqCount " + this.mGenericConnectionReqCount);
        pw.println("WifiNetworkFactory: mActiveSpecificNetworkRequest " + this.mActiveSpecificNetworkRequest);
        pw.println("WifiNetworkFactory: mUserApprovedAccessPointMap " + this.mUserApprovedAccessPointMap);
    }

    public boolean hasConnectionRequests() {
        return (this.mGenericConnectionReqCount <= 0 && this.mActiveSpecificNetworkRequest == null && this.mConnectedSpecificNetworkRequest == null) ? false : true;
    }

    public Pair<Integer, String> getSpecificNetworkRequestUidAndPackageName(WifiConfiguration connectedNetwork) {
        if (this.mUserSelectedNetwork == null || connectedNetwork == null) {
            return Pair.create(-1, "");
        }
        if (!isUserSelectedNetwork(connectedNetwork)) {
            Log.w(TAG, "Connected to unknown network " + connectedNetwork + ". Ignoring...");
            return Pair.create(-1, "");
        }
        WifiNetworkSpecifier wifiNetworkSpecifier = this.mConnectedSpecificNetworkRequestSpecifier;
        if (wifiNetworkSpecifier != null) {
            return Pair.create(Integer.valueOf(wifiNetworkSpecifier.requestorUid), this.mConnectedSpecificNetworkRequestSpecifier.requestorPackageName);
        }
        WifiNetworkSpecifier wifiNetworkSpecifier2 = this.mActiveSpecificNetworkRequestSpecifier;
        if (wifiNetworkSpecifier2 != null) {
            return Pair.create(Integer.valueOf(wifiNetworkSpecifier2.requestorUid), this.mActiveSpecificNetworkRequestSpecifier.requestorPackageName);
        }
        return Pair.create(-1, "");
    }

    private int addNetworkToWifiConfigManager(WifiConfiguration network) {
        WifiConfiguration existingSavedNetwork = this.mWifiConfigManager.getConfiguredNetwork(network.configKey());
        if (existingSavedNetwork != null) {
            return existingSavedNetwork.networkId;
        }
        NetworkUpdateResult networkUpdateResult = this.mWifiConfigManager.addOrUpdateNetwork(network, this.mActiveSpecificNetworkRequestSpecifier.requestorUid, this.mActiveSpecificNetworkRequestSpecifier.requestorPackageName);
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Added network to config manager " + networkUpdateResult.netId);
        }
        return networkUpdateResult.netId;
    }

    private void connectToNetwork(WifiConfiguration network) {
        cancelConnectionTimeout();
        int networkId = addNetworkToWifiConfigManager(network);
        this.mWifiMetrics.setNominatorForNetwork(networkId, 7);
        Message msg = Message.obtain();
        msg.what = 151553;
        msg.arg1 = networkId;
        msg.replyTo = this.mSrcMessenger;
        this.mWifiInjector.getClientModeImpl().sendMessage(msg);
        scheduleConnectionTimeout();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectToNetworkUserSelectionInternal(WifiConfiguration network) {
        this.mWifiConnectivityManager.setSpecificNetworkRequestInProgress(true);
        WifiConfiguration networkToConnect = new WifiConfiguration(this.mActiveSpecificNetworkRequestSpecifier.wifiConfiguration);
        networkToConnect.SSID = network.SSID;
        networkToConnect.BSSID = findBestBssidFromActiveMatchedScanResultsForNetwork(network);
        networkToConnect.ephemeral = true;
        networkToConnect.fromWifiNetworkSpecifier = true;
        this.mUserSelectedNetwork = networkToConnect;
        this.mWifiInjector.getClientModeImpl().disconnectCommand();
        connectToNetwork(networkToConnect);
        this.mPendingConnectionSuccess = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleConnectToNetworkUserSelection(WifiConfiguration network) {
        Log.i(TAG, "User initiated connect to network: " + StringUtilEx.safeDisplaySsid(network.SSID));
        cancelPeriodicScans();
        handleConnectToNetworkUserSelectionInternal(network);
        addNetworkToUserApprovedAccessPointMap(this.mUserSelectedNetwork);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRejectUserSelection() {
        Log.w(TAG, "User dismissed notification, cancelling " + this.mActiveSpecificNetworkRequest);
        teardownForActiveRequest();
        this.mWifiMetrics.incrementNetworkRequestApiNumUserReject();
    }

    private boolean isUserSelectedNetwork(WifiConfiguration config) {
        if (TextUtils.equals(this.mUserSelectedNetwork.SSID, config.SSID) && Objects.equals(this.mUserSelectedNetwork.allowedKeyManagement, config.allowedKeyManagement)) {
            return true;
        }
        return false;
    }

    public void handleConnectionAttemptEnded(int failureCode, WifiConfiguration network) {
        if (failureCode == 1) {
            handleNetworkConnectionSuccess(network);
        } else {
            handleNetworkConnectionFailure(network);
        }
    }

    private void handleNetworkConnectionSuccess(WifiConfiguration connectedNetwork) {
        if (!(this.mUserSelectedNetwork == null || connectedNetwork == null || !this.mPendingConnectionSuccess)) {
            if (!isUserSelectedNetwork(connectedNetwork)) {
                Log.w(TAG, "Connected to unknown network " + connectedNetwork + ". Ignoring...");
                return;
            }
            Log.i(TAG, "Connected to network " + this.mUserSelectedNetwork);
            for (INetworkRequestMatchCallback callback : this.mRegisteredCallbacks.getCallbacks()) {
                try {
                    callback.onUserSelectionConnectSuccess(this.mUserSelectedNetwork);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to invoke network request connect failure callback " + callback, e);
                }
            }
            setupForConnectedRequest();
            this.mWifiMetrics.incrementNetworkRequestApiNumConnectSuccess();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNetworkConnectionFailure(WifiConfiguration failedNetwork) {
        if (!(this.mUserSelectedNetwork == null || failedNetwork == null || !this.mPendingConnectionSuccess)) {
            if (!isUserSelectedNetwork(failedNetwork)) {
                Log.w(TAG, "Connection failed to unknown network " + failedNetwork + ". Ignoring...");
                return;
            }
            Log.w(TAG, "Failed to connect to network " + this.mUserSelectedNetwork);
            int i = this.mUserSelectedNetworkConnectRetryCount;
            this.mUserSelectedNetworkConnectRetryCount = i + 1;
            if (i < 3) {
                Log.i(TAG, "Retrying connection attempt, attempt# " + this.mUserSelectedNetworkConnectRetryCount);
                connectToNetwork(this.mUserSelectedNetwork);
                return;
            }
            Log.e(TAG, "Connection failures, cancelling " + this.mUserSelectedNetwork);
            for (INetworkRequestMatchCallback callback : this.mRegisteredCallbacks.getCallbacks()) {
                try {
                    callback.onUserSelectionConnectFailure(this.mUserSelectedNetwork);
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to invoke network request connect failure callback " + callback, e);
                }
            }
            teardownForActiveRequest();
        }
    }

    public void handleScreenStateChanged(boolean screenOn) {
        if (this.mActiveSpecificNetworkRequest != null && this.mUserSelectedNetwork == null) {
            if (screenOn) {
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "Resuming scans on screen on");
                }
                startScan();
                this.mIsPeriodicScanPaused = false;
                return;
            }
            if (this.mVerboseLoggingEnabled) {
                Log.i(TAG, "Pausing scans on screen off");
            }
            cancelPeriodicScans();
            this.mIsPeriodicScanPaused = true;
        }
    }

    public void setWifiState(boolean enabled) {
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "setWifiState " + enabled);
        }
        if (enabled) {
            reevaluateAllRequests();
        } else {
            if (this.mActiveSpecificNetworkRequest != null) {
                Log.w(TAG, "Wifi off, cancelling " + this.mActiveSpecificNetworkRequest);
                teardownForActiveRequest();
            }
            if (this.mConnectedSpecificNetworkRequest != null) {
                Log.w(TAG, "Wifi off, cancelling " + this.mConnectedSpecificNetworkRequest);
                teardownForConnectedNetwork();
            }
        }
        this.mWifiEnabled = enabled;
    }

    private void cleanupActiveRequest() {
        for (INetworkRequestMatchCallback callback : this.mRegisteredCallbacks.getCallbacks()) {
            try {
                callback.onAbort();
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to invoke network request abort callback " + callback, e);
            }
        }
        NetworkRequest networkRequest = this.mActiveSpecificNetworkRequest;
        if (networkRequest != null) {
            releaseRequestAsUnfulfillableByAnyFactory(networkRequest);
        }
        this.mActiveSpecificNetworkRequest = null;
        this.mActiveSpecificNetworkRequestSpecifier = null;
        this.mUserSelectedNetwork = null;
        this.mUserSelectedNetworkConnectRetryCount = 0;
        this.mIsPeriodicScanPaused = false;
        this.mActiveMatchedScanResults = null;
        this.mPendingConnectionSuccess = false;
        cancelPeriodicScans();
        cancelConnectionTimeout();
        this.mRegisteredCallbacks.clear();
    }

    private void setupForActiveRequest() {
        if (this.mActiveSpecificNetworkRequest != null) {
            cleanupActiveRequest();
        }
    }

    private void teardownForActiveRequest() {
        cleanupActiveRequest();
        if (this.mConnectedSpecificNetworkRequest == null) {
            this.mWifiConnectivityManager.setSpecificNetworkRequestInProgress(false);
        }
    }

    private void setupForConnectedRequest() {
        this.mConnectedSpecificNetworkRequest = this.mActiveSpecificNetworkRequest;
        this.mConnectedSpecificNetworkRequestSpecifier = this.mActiveSpecificNetworkRequestSpecifier;
        this.mActiveSpecificNetworkRequest = null;
        this.mActiveSpecificNetworkRequestSpecifier = null;
        this.mPendingConnectionSuccess = false;
        cancelConnectionTimeout();
    }

    private void teardownForConnectedNetwork() {
        Log.i(TAG, "Disconnecting from network on reset");
        this.mWifiInjector.getClientModeImpl().disconnectCommand();
        this.mConnectedSpecificNetworkRequest = null;
        this.mConnectedSpecificNetworkRequestSpecifier = null;
        if (this.mActiveSpecificNetworkRequest == null) {
            this.mWifiConnectivityManager.setSpecificNetworkRequestInProgress(false);
        }
    }

    private boolean isRequestFromForegroundAppOrService(String requestorPackageName) {
        try {
            return this.mActivityManager.getPackageImportance(requestorPackageName) <= 125;
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to check the app state", e);
            return false;
        }
    }

    private boolean isRequestFromForegroundApp(String requestorPackageName) {
        try {
            return this.mActivityManager.getPackageImportance(requestorPackageName) <= 100;
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to check the app state", e);
            return false;
        }
    }

    private void retrieveWifiScanner() {
        if (this.mWifiScanner == null) {
            this.mWifiScanner = this.mWifiInjector.getWifiScanner();
            Preconditions.checkNotNull(this.mWifiScanner);
        }
    }

    private void startPeriodicScans() {
        if (this.mActiveSpecificNetworkRequestSpecifier == null) {
            Log.e(TAG, "Periodic scan triggered when there is no active network request. Ignoring...");
            return;
        }
        WifiNetworkSpecifier wns = this.mActiveSpecificNetworkRequestSpecifier;
        if (wns.wifiConfiguration.hiddenSSID) {
            WifiScanner.ScanSettings scanSettings = this.mScanSettings;
            scanSettings.hiddenNetworks = new WifiScanner.ScanSettings.HiddenNetwork[1];
            scanSettings.hiddenNetworks[0] = new WifiScanner.ScanSettings.HiddenNetwork(NativeUtil.addEnclosingQuotes(wns.ssidPatternMatcher.getPath()));
        }
        startScan();
    }

    private void cancelPeriodicScans() {
        if (this.mPeriodicScanTimerSet) {
            this.mAlarmManager.cancel(this.mPeriodicScanTimerListener);
            this.mPeriodicScanTimerSet = false;
        }
        this.mScanSettings.hiddenNetworks = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleNextPeriodicScan() {
        if (this.mIsPeriodicScanPaused) {
            Log.e(TAG, "Scan triggered when periodic scanning paused. Ignoring...");
            return;
        }
        this.mAlarmManager.set(2, RttServiceImpl.HAL_AWARE_RANGING_TIMEOUT_MS + this.mClock.getElapsedSinceBootMillis(), TAG, this.mPeriodicScanTimerListener, this.mHandler);
        this.mPeriodicScanTimerSet = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startScan() {
        if (this.mActiveSpecificNetworkRequestSpecifier == null) {
            Log.e(TAG, "Scan triggered when there is no active network request. Ignoring...");
            return;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Starting the next scan for " + this.mActiveSpecificNetworkRequestSpecifier);
        }
        this.mWifiScanner.startScan(this.mScanSettings, this.mScanListener, new WorkSource(this.mActiveSpecificNetworkRequestSpecifier.requestorUid));
    }

    private boolean doesScanResultMatchWifiNetworkSpecifier(WifiNetworkSpecifier wns, ScanResult scanResult) {
        if (wns.ssidPatternMatcher.match(scanResult.SSID) && MacAddress.fromString(scanResult.BSSID).matches((MacAddress) wns.bssidPatternMatcher.first, (MacAddress) wns.bssidPatternMatcher.second) && ScanResultMatchInfo.getNetworkType(wns.wifiConfiguration) == ScanResultMatchInfo.getNetworkType(scanResult)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<ScanResult> getNetworksMatchingActiveNetworkRequest(ScanResult[] scanResults) {
        if (this.mActiveSpecificNetworkRequestSpecifier == null) {
            Log.e(TAG, "Scan results received with no active network request. Ignoring...");
            return new ArrayList();
        }
        List<ScanResult> matchedScanResults = new ArrayList<>();
        WifiNetworkSpecifier wns = this.mActiveSpecificNetworkRequestSpecifier;
        for (ScanResult scanResult : scanResults) {
            if (doesScanResultMatchWifiNetworkSpecifier(wns, scanResult)) {
                matchedScanResults.add(scanResult);
            }
        }
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "List of scan results matching the active request " + matchedScanResults);
        }
        return matchedScanResults;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNetworkRequestMatchCallbacksForActiveRequest(List<ScanResult> matchedScanResults) {
        if (this.mRegisteredCallbacks.getNumCallbacks() == 0) {
            Log.e(TAG, "No callback registered for sending network request matches. Ignoring...");
            return;
        }
        for (INetworkRequestMatchCallback callback : this.mRegisteredCallbacks.getCallbacks()) {
            try {
                callback.onMatch(matchedScanResults);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to invoke network request match callback " + callback, e);
            }
        }
    }

    private void cancelConnectionTimeout() {
        if (this.mConnectionTimeoutSet) {
            this.mAlarmManager.cancel(this.mConnectionTimeoutAlarmListener);
            this.mConnectionTimeoutSet = false;
        }
    }

    private void scheduleConnectionTimeout() {
        this.mAlarmManager.set(2, this.mClock.getElapsedSinceBootMillis() + 30000, TAG, this.mConnectionTimeoutAlarmListener, this.mHandler);
        this.mConnectionTimeoutSet = true;
    }

    private CharSequence getAppName(String packageName) {
        try {
            CharSequence appName = this.mContext.getPackageManager().getApplicationLabel(this.mContext.getPackageManager().getApplicationInfo(packageName, 0));
            if (appName != null) {
                return appName;
            }
            return "";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to find app name for " + packageName);
            return "";
        }
    }

    private void startUi() {
        Intent intent = new Intent();
        intent.setAction(UI_START_INTENT_ACTION);
        intent.addCategory(UI_START_INTENT_CATEGORY);
        intent.setFlags(272629760);
        intent.putExtra(UI_START_INTENT_EXTRA_APP_NAME, getAppName(this.mActiveSpecificNetworkRequestSpecifier.requestorPackageName));
        intent.putExtra(UI_START_INTENT_EXTRA_REQUEST_IS_FOR_SINGLE_NETWORK, isActiveRequestForSingleNetwork());
        this.mContext.startActivityAsUser(intent, UserHandle.getUserHandleForUid(this.mActiveSpecificNetworkRequestSpecifier.requestorUid));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isActiveRequestForSingleAccessPoint() {
        WifiNetworkSpecifier wifiNetworkSpecifier = this.mActiveSpecificNetworkRequestSpecifier;
        if (wifiNetworkSpecifier != null && wifiNetworkSpecifier.ssidPatternMatcher.getType() == 0 && Objects.equals(this.mActiveSpecificNetworkRequestSpecifier.bssidPatternMatcher.second, MacAddress.BROADCAST_ADDRESS)) {
            return true;
        }
        return false;
    }

    private boolean isActiveRequestForSingleNetwork() {
        WifiNetworkSpecifier wifiNetworkSpecifier = this.mActiveSpecificNetworkRequestSpecifier;
        if (wifiNetworkSpecifier == null) {
            return false;
        }
        if (wifiNetworkSpecifier.ssidPatternMatcher.getType() != 0 && !Objects.equals(this.mActiveSpecificNetworkRequestSpecifier.bssidPatternMatcher.second, MacAddress.BROADCAST_ADDRESS)) {
            return false;
        }
        return true;
    }

    private String findBestBssidFromActiveMatchedScanResultsForNetwork(WifiConfiguration network) {
        List<ScanResult> list;
        if (this.mActiveSpecificNetworkRequestSpecifier == null || (list = this.mActiveMatchedScanResults) == null) {
            return null;
        }
        ScanResult selectedScanResult = list.stream().filter(new Predicate(network) {
            /* class com.android.server.wifi.$$Lambda$WifiNetworkFactory$DUFbxIb2MVWW7kG59l4yotYQhxY */
            private final /* synthetic */ WifiConfiguration f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return Objects.equals(ScanResultMatchInfo.fromScanResult((ScanResult) obj), ScanResultMatchInfo.fromWifiConfiguration(this.f$0));
            }
        }).max(Comparator.comparing($$Lambda$WifiNetworkFactory$CeLllDmgSLUEXADpBNicCUsuAQ.INSTANCE)).orElse(null);
        if (selectedScanResult == null) {
            Log.wtf(TAG, "Expected to find at least one matching scan result");
            return null;
        }
        if (this.mVerboseLoggingEnabled) {
            Log.i(TAG, "Best bssid selected for the request ssid:" + StringUtilEx.safeDisplaySsid(selectedScanResult.SSID) + " bssid:" + StringUtilEx.safeDisplayBssid(selectedScanResult.BSSID));
        }
        return selectedScanResult.BSSID;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ScanResult findUserApprovedAccessPointForActiveRequestFromActiveMatchedScanResults() {
        String requestorPackageName;
        Set<AccessPoint> approvedAccessPoints;
        WifiNetworkSpecifier wifiNetworkSpecifier = this.mActiveSpecificNetworkRequestSpecifier;
        if (wifiNetworkSpecifier == null || this.mActiveMatchedScanResults == null || (approvedAccessPoints = this.mUserApprovedAccessPointMap.get((requestorPackageName = wifiNetworkSpecifier.requestorPackageName))) == null) {
            return null;
        }
        for (ScanResult scanResult : this.mActiveMatchedScanResults) {
            AccessPoint accessPoint = new AccessPoint(scanResult.SSID, MacAddress.fromString(scanResult.BSSID), ScanResultMatchInfo.fromScanResult(scanResult).networkType);
            if (approvedAccessPoints.contains(accessPoint)) {
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "Found " + accessPoint + " in user approved access point for " + requestorPackageName);
                }
                return scanResult;
            }
        }
        return null;
    }

    private void addNetworkToUserApprovedAccessPointMap(WifiConfiguration network) {
        if (!(this.mActiveSpecificNetworkRequestSpecifier == null || this.mActiveMatchedScanResults == null)) {
            Set<AccessPoint> newUserApprovedAccessPoints = new HashSet<>();
            for (ScanResult scanResult : this.mActiveMatchedScanResults) {
                ScanResultMatchInfo fromScanResult = ScanResultMatchInfo.fromScanResult(scanResult);
                if (fromScanResult.equals(ScanResultMatchInfo.fromWifiConfiguration(network))) {
                    newUserApprovedAccessPoints.add(new AccessPoint(scanResult.SSID, MacAddress.fromString(scanResult.BSSID), fromScanResult.networkType));
                }
            }
            if (!newUserApprovedAccessPoints.isEmpty()) {
                String requestorPackageName = this.mActiveSpecificNetworkRequestSpecifier.requestorPackageName;
                Set<AccessPoint> approvedAccessPoints = this.mUserApprovedAccessPointMap.get(requestorPackageName);
                if (approvedAccessPoints == null) {
                    approvedAccessPoints = new HashSet();
                    this.mUserApprovedAccessPointMap.put(requestorPackageName, approvedAccessPoints);
                    this.mWifiMetrics.incrementNetworkRequestApiNumApps();
                }
                if (this.mVerboseLoggingEnabled) {
                    Log.i(TAG, "Adding " + newUserApprovedAccessPoints + " to user approved access point for " + requestorPackageName);
                }
                approvedAccessPoints.addAll(newUserApprovedAccessPoints);
                saveToStore();
            }
        }
    }

    public void removeUserApprovedAccessPointsForApp(String packageName) {
        Iterator<Map.Entry<String, Set<AccessPoint>>> iter = this.mUserApprovedAccessPointMap.entrySet().iterator();
        while (iter.hasNext()) {
            if (packageName.equals(iter.next().getKey())) {
                Log.i(TAG, "Removing all approved access points for " + packageName);
                iter.remove();
            }
        }
        saveToStore();
    }

    public void clear() {
        this.mUserApprovedAccessPointMap.clear();
        Log.i(TAG, "Cleared all internal state");
        saveToStore();
    }
}
