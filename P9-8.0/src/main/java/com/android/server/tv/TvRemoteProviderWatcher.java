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
import com.android.server.am.HwBroadcastRadarUtil;
import java.util.ArrayList;
import java.util.Collections;

final class TvRemoteProviderWatcher {
    private static final boolean DEBUG = Log.isLoggable(TAG, 2);
    private static final String TAG = "TvRemoteProvWatcher";
    private final Context mContext;
    private final Handler mHandler;
    private final PackageManager mPackageManager;
    private final ProviderMethods mProvider;
    private final ArrayList<TvRemoteProviderProxy> mProviderProxies = new ArrayList();
    private boolean mRunning;
    private final BroadcastReceiver mScanPackagesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (TvRemoteProviderWatcher.DEBUG) {
                Slog.d(TvRemoteProviderWatcher.TAG, "Received package manager broadcast: " + intent);
            }
            TvRemoteProviderWatcher.this.mHandler.post(TvRemoteProviderWatcher.this.mScanPackagesRunnable);
        }
    };
    private final Runnable mScanPackagesRunnable = new Runnable() {
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
        this.mUnbundledServicePackage = context.getString(17039810);
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
            filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
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
                ((TvRemoteProviderProxy) this.mProviderProxies.get(i)).stop();
            }
        }
    }

    private void scanPackages() {
        if (this.mRunning) {
            TvRemoteProviderProxy providerProxy;
            if (DEBUG) {
                Log.d(TAG, "scanPackages()");
            }
            int targetIndex = 0;
            for (ResolveInfo resolveInfo : this.mPackageManager.queryIntentServicesAsUser(new Intent("com.android.media.tv.remoteprovider.TvRemoteProvider"), 0, this.mUserId)) {
                ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                if (serviceInfo != null && verifyServiceTrusted(serviceInfo)) {
                    int sourceIndex = findProvider(serviceInfo.packageName, serviceInfo.name);
                    int targetIndex2;
                    if (sourceIndex < 0) {
                        providerProxy = new TvRemoteProviderProxy(this.mContext, new ComponentName(serviceInfo.packageName, serviceInfo.name), this.mUserId, serviceInfo.applicationInfo.uid);
                        providerProxy.start();
                        targetIndex2 = targetIndex + 1;
                        this.mProviderProxies.add(targetIndex, providerProxy);
                        this.mProvider.addProvider(providerProxy);
                        targetIndex = targetIndex2;
                    } else if (sourceIndex >= targetIndex) {
                        TvRemoteProviderProxy provider = (TvRemoteProviderProxy) this.mProviderProxies.get(sourceIndex);
                        provider.start();
                        provider.rebindIfDisconnected();
                        targetIndex2 = targetIndex + 1;
                        Collections.swap(this.mProviderProxies, sourceIndex, targetIndex);
                        targetIndex = targetIndex2;
                    }
                }
            }
            if (DEBUG) {
                Log.d(TAG, "scanPackages() targetIndex " + targetIndex);
            }
            if (targetIndex < this.mProviderProxies.size()) {
                for (int i = this.mProviderProxies.size() - 1; i >= targetIndex; i--) {
                    providerProxy = (TvRemoteProviderProxy) this.mProviderProxies.get(i);
                    this.mProvider.removeProvider(providerProxy);
                    this.mProviderProxies.remove(providerProxy);
                    providerProxy.stop();
                }
            }
        }
    }

    private boolean verifyServiceTrusted(ServiceInfo serviceInfo) {
        if (serviceInfo.permission == null || (serviceInfo.permission.equals("android.permission.BIND_TV_REMOTE_SERVICE") ^ 1) != 0) {
            Slog.w(TAG, "Ignoring atv remote provider service because it did not require the BIND_TV_REMOTE_SERVICE permission in its manifest: " + serviceInfo.packageName + "/" + serviceInfo.name);
            return false;
        } else if (!serviceInfo.packageName.equals(this.mUnbundledServicePackage)) {
            Slog.w(TAG, "Ignoring atv remote provider service because the package has not been set and/or whitelisted: " + serviceInfo.packageName + "/" + serviceInfo.name);
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
            if (((TvRemoteProviderProxy) this.mProviderProxies.get(i)).hasComponentName(packageName, className)) {
                return i;
            }
        }
        return -1;
    }
}
