package com.android.server.webkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.util.Slog;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewProviderResponse;
import java.io.PrintWriter;

public class WebViewUpdateServiceImpl {
    private static final int MULTIPROCESS_SETTING_OFF_VALUE = Integer.MIN_VALUE;
    private static final int MULTIPROCESS_SETTING_ON_VALUE = Integer.MAX_VALUE;
    private static final String TAG = WebViewUpdateServiceImpl.class.getSimpleName();
    private final Context mContext;
    private SystemInterface mSystemInterface;
    private WebViewUpdater mWebViewUpdater = new WebViewUpdater(this.mContext, this.mSystemInterface);

    public WebViewUpdateServiceImpl(Context context, SystemInterface systemInterface) {
        this.mContext = context;
        this.mSystemInterface = systemInterface;
    }

    /* access modifiers changed from: package-private */
    public void packageStateChanged(String packageName, int changedState, int userId) {
        this.mWebViewUpdater.packageStateChanged(packageName, changedState);
    }

    /* access modifiers changed from: package-private */
    public void prepareWebViewInSystemServer() {
        migrateFallbackStateOnBoot();
        this.mWebViewUpdater.prepareWebViewInSystemServer();
        if (getCurrentWebViewPackage() == null) {
            WebViewProviderInfo fallbackProvider = getFallbackProvider(this.mSystemInterface.getWebViewPackages());
            if (fallbackProvider != null) {
                String str = TAG;
                Slog.w(str, "No valid provider, trying to enable " + fallbackProvider.packageName);
                this.mSystemInterface.enablePackageForAllUsers(this.mContext, fallbackProvider.packageName, true);
            } else {
                Slog.e(TAG, "No valid provider and no fallback available.");
            }
        }
        boolean multiProcessEnabled = isMultiProcessEnabled();
        this.mSystemInterface.notifyZygote(multiProcessEnabled);
        if (multiProcessEnabled) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.server.webkit.$$Lambda$lAUGMGZZth095wGxrAtUYbmlIJY */

                @Override // java.lang.Runnable
                public final void run() {
                    WebViewUpdateServiceImpl.this.startZygoteWhenReady();
                }
            });
        }
    }

    /* access modifiers changed from: package-private */
    public void startZygoteWhenReady() {
        waitForAndGetProvider();
        this.mSystemInterface.ensureZygoteStarted();
    }

    /* access modifiers changed from: package-private */
    public void handleNewUser(int userId) {
        if (userId != 0) {
            handleUserChange();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleUserRemoved(int userId) {
        handleUserChange();
    }

    private void handleUserChange() {
        this.mWebViewUpdater.updateCurrentWebViewPackage(null);
    }

    /* access modifiers changed from: package-private */
    public void notifyRelroCreationCompleted() {
        this.mWebViewUpdater.notifyRelroCreationCompleted();
    }

    /* access modifiers changed from: package-private */
    public WebViewProviderResponse waitForAndGetProvider() {
        return this.mWebViewUpdater.waitForAndGetProvider();
    }

    /* access modifiers changed from: package-private */
    public String changeProviderAndSetting(String newProvider) {
        return this.mWebViewUpdater.changeProviderAndSetting(newProvider);
    }

    /* access modifiers changed from: package-private */
    public WebViewProviderInfo[] getValidWebViewPackages() {
        return this.mWebViewUpdater.getValidWebViewPackages();
    }

    /* access modifiers changed from: package-private */
    public WebViewProviderInfo[] getWebViewPackages() {
        return this.mSystemInterface.getWebViewPackages();
    }

    /* access modifiers changed from: package-private */
    public PackageInfo getCurrentWebViewPackage() {
        return this.mWebViewUpdater.getCurrentWebViewPackage();
    }

    private void migrateFallbackStateOnBoot() {
        if (this.mSystemInterface.isFallbackLogicEnabled()) {
            WebViewProviderInfo fallbackProvider = getFallbackProvider(this.mSystemInterface.getWebViewPackages());
            if (fallbackProvider != null) {
                String str = TAG;
                Slog.i(str, "One-time migration: enabling " + fallbackProvider.packageName);
                this.mSystemInterface.enablePackageForAllUsers(this.mContext, fallbackProvider.packageName, true);
            } else {
                Slog.i(TAG, "Skipping one-time migration: no fallback provider");
            }
            this.mSystemInterface.enableFallbackLogic(false);
        }
    }

    private static WebViewProviderInfo getFallbackProvider(WebViewProviderInfo[] webviewPackages) {
        for (WebViewProviderInfo provider : webviewPackages) {
            if (provider.isFallback) {
                return provider;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean isMultiProcessEnabled() {
        int settingValue = this.mSystemInterface.getMultiProcessSetting(this.mContext);
        return this.mSystemInterface.isMultiProcessDefaultEnabled() ? settingValue > Integer.MIN_VALUE : settingValue >= MULTIPROCESS_SETTING_ON_VALUE;
    }

    /* access modifiers changed from: package-private */
    public void enableMultiProcess(boolean enable) {
        PackageInfo current = getCurrentWebViewPackage();
        this.mSystemInterface.setMultiProcessSetting(this.mContext, enable ? MULTIPROCESS_SETTING_ON_VALUE : Integer.MIN_VALUE);
        this.mSystemInterface.notifyZygote(enable);
        if (current != null) {
            this.mSystemInterface.killPackageDependents(current.packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpState(PrintWriter pw) {
        pw.println("Current WebView Update Service state");
        pw.println(String.format("  Fallback logic enabled: %b", Boolean.valueOf(this.mSystemInterface.isFallbackLogicEnabled())));
        pw.println(String.format("  Multiprocess enabled: %b", Boolean.valueOf(isMultiProcessEnabled())));
        this.mWebViewUpdater.dumpState(pw);
    }
}
