package com.android.server.tv;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.server.slice.SliceClientPermissions;
import java.util.ArrayList;
import java.util.Collections;

final class TvRemoteProviderWatcher {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 2);
    private static final String TAG = "TvRemoteProvWatcher";
    private final Context mContext;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final PackageManager mPackageManager;
    private final ProviderMethods mProvider;
    private final ArrayList<TvRemoteProviderProxy> mProviderProxies = new ArrayList<>();
    private boolean mRunning;
    private final BroadcastReceiver mScanPackagesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (TvRemoteProviderWatcher.DEBUG) {
                Slog.d(TvRemoteProviderWatcher.TAG, "Received package manager broadcast: " + intent);
            }
            TvRemoteProviderWatcher.this.mHandler.post(TvRemoteProviderWatcher.this.mScanPackagesRunnable);
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mScanPackagesRunnable = new Runnable() {
        public void run() {
            TvRemoteProviderWatcher.this.scanPackages();
        }
    };
    private final String mUnbundledServicePackage;
    private final int mUserId;

    public interface ProviderMethods {
        void addProvider(TvRemoteProviderProxy tvRemoteProviderProxy);

        void removeProvider(TvRemoteProviderProxy tvRemoteProviderProxy);
    }

    public TvRemoteProviderWatcher(Context context, ProviderMethods provider, Handler handler) {
        this.mContext = context;
        this.mProvider = provider;
        this.mHandler = handler;
        this.mUserId = UserHandle.myUserId();
        this.mPackageManager = context.getPackageManager();
        this.mUnbundledServicePackage = context.getString(17039843);
    }

    public void start() {
        if (DEBUG) {
            Slog.d(TAG, "start()");
        }
        if (!this.mRunning) {
            this.mRunning = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            filter.addAction("android.intent.action.PACKAGE_RESTARTED");
            filter.addDataScheme("package");
            this.mContext.registerReceiverAsUser(this.mScanPackagesReceiver, new UserHandle(this.mUserId), filter, null, this.mHandler);
            this.mHandler.post(this.mScanPackagesRunnable);
        }
    }

    public void stop() {
        if (this.mRunning) {
            this.mRunning = false;
            this.mContext.unregisterReceiver(this.mScanPackagesReceiver);
            this.mHandler.removeCallbacks(this.mScanPackagesRunnable);
            for (int i = this.mProviderProxies.size() - 1; i >= 0; i--) {
                this.mProviderProxies.get(i).stop();
            }
        }
    }

    /* access modifiers changed from: private */
    public void scanPackages() {
        int targetIndex;
        if (this.mRunning) {
            if (DEBUG) {
                Log.d(TAG, "scanPackages()");
            }
            int targetIndex2 = 0;
            for (ResolveInfo resolveInfo : this.mPackageManager.queryIntentServicesAsUser(new Intent("com.android.media.tv.remoteprovider.TvRemoteProvider"), 0, this.mUserId)) {
                ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                if (serviceInfo != null && verifyServiceTrusted(serviceInfo)) {
                    int sourceIndex = findProvider(serviceInfo.packageName, serviceInfo.name);
                    if (sourceIndex < 0) {
                        TvRemoteProviderProxy providerProxy = new TvRemoteProviderProxy(this.mContext, new ComponentName(serviceInfo.packageName, serviceInfo.name), this.mUserId, serviceInfo.applicationInfo.uid);
                        providerProxy.start();
                        targetIndex = targetIndex2 + 1;
                        this.mProviderProxies.add(targetIndex2, providerProxy);
                        this.mProvider.addProvider(providerProxy);
                    } else if (sourceIndex >= targetIndex2) {
                        TvRemoteProviderProxy provider = this.mProviderProxies.get(sourceIndex);
                        provider.start();
                        provider.rebindIfDisconnected();
                        targetIndex = targetIndex2 + 1;
                        Collections.swap(this.mProviderProxies, sourceIndex, targetIndex2);
                    }
                    targetIndex2 = targetIndex;
                }
            }
            if (DEBUG) {
                Log.d(TAG, "scanPackages() targetIndex " + targetIndex2);
            }
            if (targetIndex2 < this.mProviderProxies.size()) {
                for (int i = this.mProviderProxies.size() - 1; i >= targetIndex2; i--) {
                    TvRemoteProviderProxy providerProxy2 = this.mProviderProxies.get(i);
                    this.mProvider.removeProvider(providerProxy2);
                    this.mProviderProxies.remove(providerProxy2);
                    providerProxy2.stop();
                }
            }
        }
    }

    private boolean verifyServiceTrusted(ServiceInfo serviceInfo) {
        if (serviceInfo.permission == null || !serviceInfo.permission.equals("android.permission.BIND_TV_REMOTE_SERVICE")) {
            Slog.w(TAG, "Ignoring atv remote provider service because it did not require the BIND_TV_REMOTE_SERVICE permission in its manifest: " + serviceInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + serviceInfo.name);
            return false;
        } else if (!serviceInfo.packageName.equals(this.mUnbundledServicePackage)) {
            Slog.w(TAG, "Ignoring atv remote provider service because the package has not been set and/or whitelisted: " + serviceInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + serviceInfo.name);
            return false;
        } else if (hasNecessaryPermissions(serviceInfo.packageName)) {
            return true;
        } else {
            Slog.w(TAG, "Ignoring atv remote provider service because its package does not have TV_VIRTUAL_REMOTE_CONTROLLER permission: " + serviceInfo.packageName);
            return false;
        }
    }

    private boolean hasNecessaryPermissions(String packageName) {
        if (this.mPackageManager.checkPermission("android.permission.TV_VIRTUAL_REMOTE_CONTROLLER", packageName) == 0) {
            return true;
        }
        return false;
    }

    private int findProvider(String packageName, String className) {
        int count = this.mProviderProxies.size();
        for (int i = 0; i < count; i++) {
            if (this.mProviderProxies.get(i).hasComponentName(packageName, className)) {
                return i;
            }
        }
        return -1;
    }
}
