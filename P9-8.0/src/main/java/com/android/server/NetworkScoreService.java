package com.android.server;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.net.INetworkRecommendationProvider;
import android.net.INetworkScoreCache;
import android.net.INetworkScoreService.Stub;
import android.net.NetworkKey;
import android.net.NetworkScorerAppData;
import android.net.ScoredNetwork;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiScanner;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.TransferPipe;
import com.android.internal.util.DumpUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NetworkScoreService extends Stub {
    private static final boolean DBG;
    private static final String TAG = "NetworkScoreService";
    private static final boolean VERBOSE;
    private final DispatchingContentObserver mContentObserver;
    private final Context mContext;
    private final Handler mHandler;
    private final NetworkScorerAppManager mNetworkScorerAppManager;
    @GuardedBy("mPackageMonitorLock")
    private NetworkScorerPackageMonitor mPackageMonitor;
    private final Object mPackageMonitorLock;
    @GuardedBy("mScoreCaches")
    private final Map<Integer, RemoteCallbackList<INetworkScoreCache>> mScoreCaches;
    private final Function<NetworkScorerAppData, ScoringServiceConnection> mServiceConnProducer;
    @GuardedBy("mServiceConnectionLock")
    private ScoringServiceConnection mServiceConnection;
    private final Object mServiceConnectionLock;
    private BroadcastReceiver mUserIntentReceiver;

    static class CurrentNetworkScoreCacheFilter implements UnaryOperator<List<ScoredNetwork>> {
        private final NetworkKey mCurrentNetwork;

        CurrentNetworkScoreCacheFilter(Supplier<WifiInfo> wifiInfoSupplier) {
            this.mCurrentNetwork = NetworkKey.createFromWifiInfo((WifiInfo) wifiInfoSupplier.get());
        }

        public List<ScoredNetwork> apply(List<ScoredNetwork> scoredNetworks) {
            if (this.mCurrentNetwork == null || scoredNetworks.isEmpty()) {
                return Collections.emptyList();
            }
            for (int i = 0; i < scoredNetworks.size(); i++) {
                ScoredNetwork scoredNetwork = (ScoredNetwork) scoredNetworks.get(i);
                if (scoredNetwork.networkKey.equals(this.mCurrentNetwork)) {
                    return Collections.singletonList(scoredNetwork);
                }
            }
            return Collections.emptyList();
        }
    }

    public static class DispatchingContentObserver extends ContentObserver {
        private final Context mContext;
        private final Handler mHandler;
        private final Map<Uri, Integer> mUriEventMap = new ArrayMap();

        public DispatchingContentObserver(Context context, Handler handler) {
            super(handler);
            this.mContext = context;
            this.mHandler = handler;
        }

        void observe(Uri uri, int what) {
            this.mUriEventMap.put(uri, Integer.valueOf(what));
            this.mContext.getContentResolver().registerContentObserver(uri, false, this);
        }

        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (NetworkScoreService.DBG) {
                Log.d(NetworkScoreService.TAG, String.format("onChange(%s, %s)", new Object[]{Boolean.valueOf(selfChange), uri}));
            }
            Integer what = (Integer) this.mUriEventMap.get(uri);
            if (what != null) {
                this.mHandler.obtainMessage(what.intValue()).sendToTarget();
            } else {
                Log.w(NetworkScoreService.TAG, "No matching event to send for URI = " + uri);
            }
        }
    }

    static class FilteringCacheUpdatingConsumer implements BiConsumer<INetworkScoreCache, Object> {
        private final Context mContext;
        private UnaryOperator<List<ScoredNetwork>> mCurrentNetworkFilter;
        private final int mNetworkType;
        private UnaryOperator<List<ScoredNetwork>> mScanResultsFilter;
        private final List<ScoredNetwork> mScoredNetworkList;

        static FilteringCacheUpdatingConsumer create(Context context, List<ScoredNetwork> scoredNetworkList, int networkType) {
            return new FilteringCacheUpdatingConsumer(context, scoredNetworkList, networkType, null, null);
        }

        FilteringCacheUpdatingConsumer(Context context, List<ScoredNetwork> scoredNetworkList, int networkType, UnaryOperator<List<ScoredNetwork>> currentNetworkFilter, UnaryOperator<List<ScoredNetwork>> scanResultsFilter) {
            this.mContext = context;
            this.mScoredNetworkList = scoredNetworkList;
            this.mNetworkType = networkType;
            this.mCurrentNetworkFilter = currentNetworkFilter;
            this.mScanResultsFilter = scanResultsFilter;
        }

        public void accept(INetworkScoreCache networkScoreCache, Object cookie) {
            int filterType = 0;
            if (cookie instanceof Integer) {
                filterType = ((Integer) cookie).intValue();
            }
            try {
                List<ScoredNetwork> filteredNetworkList = filterScores(this.mScoredNetworkList, filterType);
                if (!filteredNetworkList.isEmpty()) {
                    networkScoreCache.updateScores(filteredNetworkList);
                }
            } catch (RemoteException e) {
                if (NetworkScoreService.VERBOSE) {
                    Log.v(NetworkScoreService.TAG, "Unable to update scores of type " + this.mNetworkType, e);
                }
            }
        }

        private List<ScoredNetwork> filterScores(List<ScoredNetwork> scoredNetworkList, int filterType) {
            switch (filterType) {
                case 0:
                    return scoredNetworkList;
                case 1:
                    if (this.mCurrentNetworkFilter == null) {
                        this.mCurrentNetworkFilter = new CurrentNetworkScoreCacheFilter(new WifiInfoSupplier(this.mContext));
                    }
                    return (List) this.mCurrentNetworkFilter.apply(scoredNetworkList);
                case 2:
                    if (this.mScanResultsFilter == null) {
                        this.mScanResultsFilter = new ScanResultsScoreCacheFilter(new ScanResultsSupplier(this.mContext));
                    }
                    return (List) this.mScanResultsFilter.apply(scoredNetworkList);
                default:
                    Log.w(NetworkScoreService.TAG, "Unknown filter type: " + filterType);
                    return scoredNetworkList;
            }
        }
    }

    private class NetworkScorerPackageMonitor extends PackageMonitor {
        final String mPackageToWatch;

        /* synthetic */ NetworkScorerPackageMonitor(NetworkScoreService this$0, String packageToWatch, NetworkScorerPackageMonitor -this2) {
            this(packageToWatch);
        }

        private NetworkScorerPackageMonitor(String packageToWatch) {
            this.mPackageToWatch = packageToWatch;
        }

        public void onPackageAdded(String packageName, int uid) {
            evaluateBinding(packageName, true);
        }

        public void onPackageRemoved(String packageName, int uid) {
            evaluateBinding(packageName, true);
        }

        public void onPackageModified(String packageName) {
            evaluateBinding(packageName, false);
        }

        public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
            if (doit) {
                for (String packageName : packages) {
                    evaluateBinding(packageName, true);
                }
            }
            return super.onHandleForceStop(intent, packages, uid, doit);
        }

        public void onPackageUpdateFinished(String packageName, int uid) {
            evaluateBinding(packageName, true);
        }

        private void evaluateBinding(String changedPackageName, boolean forceUnbind) {
            if (this.mPackageToWatch.equals(changedPackageName)) {
                if (NetworkScoreService.DBG) {
                    Log.d(NetworkScoreService.TAG, "Evaluating binding for: " + changedPackageName + ", forceUnbind=" + forceUnbind);
                }
                NetworkScorerAppData activeScorer = NetworkScoreService.this.mNetworkScorerAppManager.getActiveScorer();
                if (activeScorer == null) {
                    if (NetworkScoreService.DBG) {
                        Log.d(NetworkScoreService.TAG, "No active scorers available.");
                    }
                    NetworkScoreService.this.refreshBinding();
                } else {
                    if (forceUnbind) {
                        NetworkScoreService.this.unbindFromScoringServiceIfNeeded();
                    }
                    if (NetworkScoreService.DBG) {
                        Log.d(NetworkScoreService.TAG, "Binding to " + activeScorer.getRecommendationServiceComponent() + " if needed.");
                    }
                    NetworkScoreService.this.bindToScoringServiceIfNeeded(activeScorer);
                }
            }
        }
    }

    static class ScanResultsScoreCacheFilter implements UnaryOperator<List<ScoredNetwork>> {
        private final Set<NetworkKey> mScanResultKeys;

        ScanResultsScoreCacheFilter(Supplier<List<ScanResult>> resultsSupplier) {
            List<ScanResult> scanResults = (List) resultsSupplier.get();
            int size = scanResults.size();
            this.mScanResultKeys = new ArraySet(size);
            for (int i = 0; i < size; i++) {
                NetworkKey key = NetworkKey.createFromScanResult((ScanResult) scanResults.get(i));
                if (key != null) {
                    this.mScanResultKeys.add(key);
                }
            }
        }

        public List<ScoredNetwork> apply(List<ScoredNetwork> scoredNetworks) {
            if (this.mScanResultKeys.isEmpty() || scoredNetworks.isEmpty()) {
                return Collections.emptyList();
            }
            List<ScoredNetwork> filteredScores = new ArrayList();
            for (int i = 0; i < scoredNetworks.size(); i++) {
                ScoredNetwork scoredNetwork = (ScoredNetwork) scoredNetworks.get(i);
                if (this.mScanResultKeys.contains(scoredNetwork.networkKey)) {
                    filteredScores.add(scoredNetwork);
                }
            }
            return filteredScores;
        }
    }

    private static class ScanResultsSupplier implements Supplier<List<ScanResult>> {
        private final Context mContext;

        ScanResultsSupplier(Context context) {
            this.mContext = context;
        }

        public List<ScanResult> get() {
            WifiScanner wifiScanner = (WifiScanner) this.mContext.getSystemService(WifiScanner.class);
            if (wifiScanner != null) {
                return wifiScanner.getSingleScanResults();
            }
            Log.w(NetworkScoreService.TAG, "WifiScanner is null, failed to return scan results.");
            return Collections.emptyList();
        }
    }

    public static class ScoringServiceConnection implements ServiceConnection {
        private final NetworkScorerAppData mAppData;
        private volatile boolean mBound = false;
        private volatile boolean mConnected = false;
        private volatile INetworkRecommendationProvider mRecommendationProvider;

        ScoringServiceConnection(NetworkScorerAppData appData) {
            this.mAppData = appData;
        }

        public void bind(Context context) {
            if (!this.mBound) {
                Intent service = new Intent("android.net.action.RECOMMEND_NETWORKS");
                service.setComponent(this.mAppData.getRecommendationServiceComponent());
                this.mBound = context.bindServiceAsUser(service, this, 67108865, UserHandle.SYSTEM);
                if (!this.mBound) {
                    Log.w(NetworkScoreService.TAG, "Bind call failed for " + service);
                    context.unbindService(this);
                } else if (NetworkScoreService.DBG) {
                    Log.d(NetworkScoreService.TAG, "ScoringServiceConnection bound.");
                }
            }
        }

        public void unbind(Context context) {
            try {
                if (this.mBound) {
                    this.mBound = false;
                    context.unbindService(this);
                    if (NetworkScoreService.DBG) {
                        Log.d(NetworkScoreService.TAG, "ScoringServiceConnection unbound.");
                    }
                }
            } catch (RuntimeException e) {
                Log.e(NetworkScoreService.TAG, "Unbind failed.", e);
            }
            this.mConnected = false;
            this.mRecommendationProvider = null;
        }

        public NetworkScorerAppData getAppData() {
            return this.mAppData;
        }

        public INetworkRecommendationProvider getRecommendationProvider() {
            return this.mRecommendationProvider;
        }

        public String getPackageName() {
            return this.mAppData.getRecommendationServiceComponent().getPackageName();
        }

        public boolean isAlive() {
            return this.mBound ? this.mConnected : false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            if (NetworkScoreService.DBG) {
                Log.d(NetworkScoreService.TAG, "ScoringServiceConnection: " + name.flattenToString());
            }
            this.mConnected = true;
            this.mRecommendationProvider = INetworkRecommendationProvider.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            if (NetworkScoreService.DBG) {
                Log.d(NetworkScoreService.TAG, "ScoringServiceConnection, disconnected: " + name.flattenToString());
            }
            this.mConnected = false;
            this.mRecommendationProvider = null;
        }

        public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.println("ScoringServiceConnection: " + this.mAppData.getRecommendationServiceComponent() + ", bound: " + this.mBound + ", connected: " + this.mConnected);
        }
    }

    public final class ServiceHandler extends Handler {
        public static final int MSG_RECOMMENDATIONS_PACKAGE_CHANGED = 1;
        public static final int MSG_RECOMMENDATION_ENABLED_SETTING_CHANGED = 2;

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 1:
                case 2:
                    NetworkScoreService.this.refreshBinding();
                    return;
                default:
                    Log.w(NetworkScoreService.TAG, "Unknown message: " + what);
                    return;
            }
        }
    }

    private static class WifiInfoSupplier implements Supplier<WifiInfo> {
        private final Context mContext;

        WifiInfoSupplier(Context context) {
            this.mContext = context;
        }

        public WifiInfo get() {
            WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(WifiManager.class);
            if (wifiManager != null) {
                return wifiManager.getConnectionInfo();
            }
            Log.w(NetworkScoreService.TAG, "WifiManager is null, failed to return the WifiInfo.");
            return null;
        }
    }

    static {
        boolean isLoggable;
        boolean z = false;
        if (Build.IS_DEBUGGABLE) {
            isLoggable = Log.isLoggable(TAG, 3);
        } else {
            isLoggable = false;
        }
        DBG = isLoggable;
        if (Build.IS_DEBUGGABLE) {
            z = Log.isLoggable(TAG, 2);
        }
        VERBOSE = z;
    }

    public NetworkScoreService(Context context) {
        this(context, new NetworkScorerAppManager(context), new -$Lambda$Ash-36Gr90yYPZEIENlguvJE7uk(), Looper.myLooper());
    }

    NetworkScoreService(Context context, NetworkScorerAppManager networkScoreAppManager, Function<NetworkScorerAppData, ScoringServiceConnection> serviceConnProducer, Looper looper) {
        this.mPackageMonitorLock = new Object();
        this.mServiceConnectionLock = new Object();
        this.mUserIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int userId = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if (NetworkScoreService.DBG) {
                    Log.d(NetworkScoreService.TAG, "Received " + action + " for userId " + userId);
                }
                if (userId != -10000 && "android.intent.action.USER_UNLOCKED".equals(action)) {
                    NetworkScoreService.this.onUserUnlocked(userId);
                }
            }
        };
        this.mContext = context;
        this.mNetworkScorerAppManager = networkScoreAppManager;
        this.mScoreCaches = new ArrayMap();
        this.mContext.registerReceiverAsUser(this.mUserIntentReceiver, UserHandle.SYSTEM, new IntentFilter("android.intent.action.USER_UNLOCKED"), null, null);
        this.mHandler = new ServiceHandler(looper);
        this.mContentObserver = new DispatchingContentObserver(context, this.mHandler);
        this.mServiceConnProducer = serviceConnProducer;
    }

    void systemReady() {
        if (DBG) {
            Log.d(TAG, "systemReady");
        }
        registerRecommendationSettingsObserver();
    }

    void systemRunning() {
        if (DBG) {
            Log.d(TAG, "systemRunning");
        }
    }

    void onUserUnlocked(int userId) {
        if (DBG) {
            Log.d(TAG, "onUserUnlocked(" + userId + ")");
        }
        refreshBinding();
    }

    private void refreshBinding() {
        if (DBG) {
            Log.d(TAG, "refreshBinding()");
        }
        this.mNetworkScorerAppManager.updateState();
        this.mNetworkScorerAppManager.migrateNetworkScorerAppSettingIfNeeded();
        registerPackageMonitorIfNeeded();
        bindToScoringServiceIfNeeded();
    }

    private void registerRecommendationSettingsObserver() {
        this.mContentObserver.observe(Global.getUriFor("network_recommendations_package"), 1);
        this.mContentObserver.observe(Global.getUriFor("network_recommendations_enabled"), 2);
    }

    private void registerPackageMonitorIfNeeded() {
        if (DBG) {
            Log.d(TAG, "registerPackageMonitorIfNeeded()");
        }
        NetworkScorerAppData appData = this.mNetworkScorerAppManager.getActiveScorer();
        synchronized (this.mPackageMonitorLock) {
            if (this.mPackageMonitor != null && (appData == null || (appData.getRecommendationServicePackageName().equals(this.mPackageMonitor.mPackageToWatch) ^ 1) != 0)) {
                if (DBG) {
                    Log.d(TAG, "Unregistering package monitor for " + this.mPackageMonitor.mPackageToWatch);
                }
                this.mPackageMonitor.unregister();
                this.mPackageMonitor = null;
            }
            if (appData != null && this.mPackageMonitor == null) {
                this.mPackageMonitor = new NetworkScorerPackageMonitor(this, appData.getRecommendationServicePackageName(), null);
                this.mPackageMonitor.register(this.mContext, null, UserHandle.SYSTEM, false);
                if (DBG) {
                    Log.d(TAG, "Registered package monitor for " + this.mPackageMonitor.mPackageToWatch);
                }
            }
        }
    }

    private void bindToScoringServiceIfNeeded() {
        if (DBG) {
            Log.d(TAG, "bindToScoringServiceIfNeeded");
        }
        bindToScoringServiceIfNeeded(this.mNetworkScorerAppManager.getActiveScorer());
    }

    private void bindToScoringServiceIfNeeded(NetworkScorerAppData appData) {
        if (DBG) {
            Log.d(TAG, "bindToScoringServiceIfNeeded(" + appData + ")");
        }
        if (appData != null) {
            synchronized (this.mServiceConnectionLock) {
                if (!(this.mServiceConnection == null || (this.mServiceConnection.getAppData().equals(appData) ^ 1) == 0)) {
                    unbindFromScoringServiceIfNeeded();
                }
                if (this.mServiceConnection == null) {
                    this.mServiceConnection = (ScoringServiceConnection) this.mServiceConnProducer.apply(appData);
                }
                this.mServiceConnection.bind(this.mContext);
            }
            return;
        }
        unbindFromScoringServiceIfNeeded();
    }

    private void unbindFromScoringServiceIfNeeded() {
        if (DBG) {
            Log.d(TAG, "unbindFromScoringServiceIfNeeded");
        }
        synchronized (this.mServiceConnectionLock) {
            if (this.mServiceConnection != null) {
                this.mServiceConnection.unbind(this.mContext);
                if (DBG) {
                    Log.d(TAG, "Disconnected from: " + this.mServiceConnection.getAppData().getRecommendationServiceComponent());
                }
            }
            this.mServiceConnection = null;
        }
        clearInternal();
    }

    public boolean updateScores(ScoredNetwork[] networks) {
        if (isCallerActiveScorer(getCallingUid())) {
            long token = Binder.clearCallingIdentity();
            try {
                Map<Integer, List<ScoredNetwork>> networksByType = new ArrayMap();
                for (ScoredNetwork network : networks) {
                    List<ScoredNetwork> networkList = (List) networksByType.get(Integer.valueOf(network.networkKey.type));
                    if (networkList == null) {
                        networkList = new ArrayList();
                        networksByType.put(Integer.valueOf(network.networkKey.type), networkList);
                    }
                    networkList.add(network);
                }
                for (Entry<Integer, List<ScoredNetwork>> entry : networksByType.entrySet()) {
                    RemoteCallbackList<INetworkScoreCache> callbackList;
                    boolean isEmpty;
                    synchronized (this.mScoreCaches) {
                        callbackList = (RemoteCallbackList) this.mScoreCaches.get(entry.getKey());
                        isEmpty = callbackList != null ? callbackList.getRegisteredCallbackCount() == 0 : true;
                    }
                    if (!isEmpty) {
                        sendCacheUpdateCallback(FilteringCacheUpdatingConsumer.create(this.mContext, (List) entry.getValue(), ((Integer) entry.getKey()).intValue()), Collections.singleton(callbackList));
                    } else if (Log.isLoggable(TAG, 2)) {
                        Log.v(TAG, "No scorer registered for type " + entry.getKey() + ", discarding");
                    }
                }
                Binder.restoreCallingIdentity(token);
                return true;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Caller with UID " + getCallingUid() + " is not the active scorer.");
        }
    }

    private boolean callerCanRequestScores() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.REQUEST_NETWORK_SCORES") == 0;
    }

    private boolean callerCanScoreNetworks() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.SCORE_NETWORKS") == 0;
    }

    public boolean clearScores() {
        if (isCallerActiveScorer(getCallingUid()) || callerCanRequestScores()) {
            long token = Binder.clearCallingIdentity();
            try {
                clearInternal();
                return true;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        } else {
            throw new SecurityException("Caller is neither the active scorer nor the scorer manager.");
        }
    }

    public boolean setActiveScorer(String packageName) {
        if (isCallerSystemProcess(getCallingUid()) || (callerCanScoreNetworks() ^ 1) == 0) {
            return this.mNetworkScorerAppManager.setActiveScorer(packageName);
        }
        throw new SecurityException("Caller is neither the system process or a network scorer.");
    }

    public boolean isCallerActiveScorer(int callingUid) {
        boolean z = false;
        synchronized (this.mServiceConnectionLock) {
            if (this.mServiceConnection != null && this.mServiceConnection.getAppData().packageUid == callingUid) {
                z = true;
            }
        }
        return z;
    }

    private boolean isCallerSystemProcess(int callingUid) {
        return callingUid == 1000;
    }

    public String getActiveScorerPackage() {
        synchronized (this.mServiceConnectionLock) {
            if (this.mServiceConnection != null) {
                String packageName = this.mServiceConnection.getPackageName();
                return packageName;
            }
            return null;
        }
    }

    public NetworkScorerAppData getActiveScorer() {
        if (isCallerSystemProcess(getCallingUid()) || callerCanRequestScores()) {
            synchronized (this.mServiceConnectionLock) {
                if (this.mServiceConnection != null) {
                    NetworkScorerAppData appData = this.mServiceConnection.getAppData();
                    return appData;
                }
                return null;
            }
        }
        throw new SecurityException("Caller is neither the system process nor a score requester.");
    }

    public List<NetworkScorerAppData> getAllValidScorers() {
        if (isCallerSystemProcess(getCallingUid()) || (callerCanRequestScores() ^ 1) == 0) {
            return this.mNetworkScorerAppManager.getAllValidScorers();
        }
        throw new SecurityException("Caller is neither the system process nor a score requester.");
    }

    public void disableScoring() {
        if (!isCallerActiveScorer(getCallingUid()) && (callerCanRequestScores() ^ 1) != 0) {
            throw new SecurityException("Caller is neither the active scorer nor the scorer manager.");
        }
    }

    private void clearInternal() {
        sendCacheUpdateCallback(new BiConsumer<INetworkScoreCache, Object>() {
            public void accept(INetworkScoreCache networkScoreCache, Object cookie) {
                try {
                    networkScoreCache.clearScores();
                } catch (RemoteException e) {
                    if (Log.isLoggable(NetworkScoreService.TAG, 2)) {
                        Log.v(NetworkScoreService.TAG, "Unable to clear scores", e);
                    }
                }
            }
        }, getScoreCacheLists());
    }

    public void registerNetworkScoreCache(int networkType, INetworkScoreCache scoreCache, int filterType) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REQUEST_NETWORK_SCORES", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mScoreCaches) {
                RemoteCallbackList<INetworkScoreCache> callbackList = (RemoteCallbackList) this.mScoreCaches.get(Integer.valueOf(networkType));
                if (callbackList == null) {
                    callbackList = new RemoteCallbackList();
                    this.mScoreCaches.put(Integer.valueOf(networkType), callbackList);
                }
                if (!callbackList.register(scoreCache, Integer.valueOf(filterType))) {
                    if (callbackList.getRegisteredCallbackCount() == 0) {
                        this.mScoreCaches.remove(Integer.valueOf(networkType));
                    }
                    if (Log.isLoggable(TAG, 2)) {
                        Log.v(TAG, "Unable to register NetworkScoreCache for type " + networkType);
                    }
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public void unregisterNetworkScoreCache(int networkType, INetworkScoreCache scoreCache) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REQUEST_NETWORK_SCORES", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            synchronized (this.mScoreCaches) {
                RemoteCallbackList<INetworkScoreCache> callbackList = (RemoteCallbackList) this.mScoreCaches.get(Integer.valueOf(networkType));
                if (callbackList == null || (callbackList.unregister(scoreCache) ^ 1) != 0) {
                    if (Log.isLoggable(TAG, 2)) {
                        Log.v(TAG, "Unable to unregister NetworkScoreCache for type " + networkType);
                    }
                } else if (callbackList.getRegisteredCallbackCount() == 0) {
                    this.mScoreCaches.remove(Integer.valueOf(networkType));
                }
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public boolean requestScores(NetworkKey[] networks) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.REQUEST_NETWORK_SCORES", TAG);
        long token = Binder.clearCallingIdentity();
        try {
            INetworkRecommendationProvider provider = getRecommendationProvider();
            if (provider != null) {
                provider.requestScores(networks);
                Binder.restoreCallingIdentity(token);
                return true;
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to request scores.", e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
        Binder.restoreCallingIdentity(token);
        return false;
    }

    protected void dump(final FileDescriptor fd, final PrintWriter writer, final String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            long token = Binder.clearCallingIdentity();
            try {
                NetworkScorerAppData currentScorer = this.mNetworkScorerAppManager.getActiveScorer();
                if (currentScorer == null) {
                    writer.println("Scoring is disabled.");
                    return;
                }
                writer.println("Current scorer: " + currentScorer);
                sendCacheUpdateCallback(new BiConsumer<INetworkScoreCache, Object>() {
                    /* JADX WARNING: Removed duplicated region for block: B:2:0x000c A:{Splitter: B:0:0x0000, ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception)} */
                    /* JADX WARNING: Missing block: B:2:0x000c, code:
            r0 = move-exception;
     */
                    /* JADX WARNING: Missing block: B:3:0x000d, code:
            r7.println("Failed to dump score cache: " + r0);
     */
                    /* JADX WARNING: Missing block: B:5:?, code:
            return;
     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void accept(INetworkScoreCache networkScoreCache, Object cookie) {
                        try {
                            TransferPipe.dumpAsync(networkScoreCache.asBinder(), fd, args);
                        } catch (Exception e) {
                        }
                    }
                }, getScoreCacheLists());
                synchronized (this.mServiceConnectionLock) {
                    if (this.mServiceConnection != null) {
                        this.mServiceConnection.dump(fd, writer, args);
                    } else {
                        writer.println("ScoringServiceConnection: null");
                    }
                }
                writer.flush();
                Binder.restoreCallingIdentity(token);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private Collection<RemoteCallbackList<INetworkScoreCache>> getScoreCacheLists() {
        Collection arrayList;
        synchronized (this.mScoreCaches) {
            arrayList = new ArrayList(this.mScoreCaches.values());
        }
        return arrayList;
    }

    private void sendCacheUpdateCallback(BiConsumer<INetworkScoreCache, Object> consumer, Collection<RemoteCallbackList<INetworkScoreCache>> remoteCallbackLists) {
        for (RemoteCallbackList<INetworkScoreCache> callbackList : remoteCallbackLists) {
            synchronized (callbackList) {
                int count = callbackList.beginBroadcast();
                int i = 0;
                while (i < count) {
                    try {
                        consumer.accept((INetworkScoreCache) callbackList.getBroadcastItem(i), callbackList.getBroadcastCookie(i));
                        i++;
                    } catch (Throwable th) {
                        callbackList.finishBroadcast();
                    }
                }
                callbackList.finishBroadcast();
            }
        }
    }

    private INetworkRecommendationProvider getRecommendationProvider() {
        synchronized (this.mServiceConnectionLock) {
            if (this.mServiceConnection != null) {
                INetworkRecommendationProvider recommendationProvider = this.mServiceConnection.getRecommendationProvider();
                return recommendationProvider;
            }
            return null;
        }
    }
}
