package com.android.server.webkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;
import android.util.Slog;
import android.webkit.UserPackage;
import android.webkit.WebViewFactory;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewProviderResponse;
import com.android.server.os.HwBootFail;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class WebViewUpdater {
    private static final String TAG = WebViewUpdater.class.getSimpleName();
    private static final int VALIDITY_INCORRECT_SDK_VERSION = 1;
    private static final int VALIDITY_INCORRECT_SIGNATURE = 3;
    private static final int VALIDITY_INCORRECT_VERSION_CODE = 2;
    private static final int VALIDITY_NO_LIBRARY_FLAG = 4;
    private static final int VALIDITY_OK = 0;
    private static final int WAIT_TIMEOUT_MS = 1000;
    private int NUMBER_OF_RELROS_UNKNOWN = HwBootFail.STAGE_BOOT_SUCCESS;
    private boolean mAnyWebViewInstalled = false;
    private Context mContext;
    private PackageInfo mCurrentWebViewPackage = null;
    private Object mLock = new Object();
    private int mMinimumVersionCode = -1;
    private int mNumRelroCreationsFinished = 0;
    private int mNumRelroCreationsStarted = 0;
    private SystemInterface mSystemInterface;
    private boolean mWebViewPackageDirty = false;

    private static class ProviderAndPackageInfo {
        public final PackageInfo packageInfo;
        public final WebViewProviderInfo provider;

        public ProviderAndPackageInfo(WebViewProviderInfo provider, PackageInfo packageInfo) {
            this.provider = provider;
            this.packageInfo = packageInfo;
        }
    }

    private static class WebViewPackageMissingException extends Exception {
        public WebViewPackageMissingException(String message) {
            super(message);
        }

        public WebViewPackageMissingException(Exception e) {
            super(e);
        }
    }

    WebViewUpdater(Context context, SystemInterface systemInterface) {
        this.mContext = context;
        this.mSystemInterface = systemInterface;
    }

    void packageStateChanged(String packageName, int changedState) {
        boolean updateWebView;
        String str;
        WebViewProviderInfo[] webViewPackages = this.mSystemInterface.getWebViewPackages();
        int i = 0;
        int length = webViewPackages.length;
        while (i < length) {
            WebViewProviderInfo provider = webViewPackages[i];
            if (provider.packageName.equals(packageName)) {
                updateWebView = false;
                boolean removedOrChangedOldPackage = false;
                str = null;
                synchronized (this.mLock) {
                    PackageInfo newPackage = findPreferredWebViewPackage();
                    if (this.mCurrentWebViewPackage != null) {
                        str = this.mCurrentWebViewPackage.packageName;
                        if (changedState == 0 && newPackage.packageName.equals(str)) {
                            return;
                        }
                        try {
                            if (newPackage.packageName.equals(str) && newPackage.lastUpdateTime == this.mCurrentWebViewPackage.lastUpdateTime) {
                                return;
                            }
                        } catch (WebViewPackageMissingException e) {
                            this.mCurrentWebViewPackage = null;
                            Slog.e(TAG, "Could not find valid WebView package to create relro with " + e);
                        }
                    }
                    updateWebView = (provider.packageName.equals(newPackage.packageName) || provider.packageName.equals(str)) ? true : this.mCurrentWebViewPackage == null;
                    removedOrChangedOldPackage = provider.packageName.equals(str);
                    if (updateWebView) {
                        onWebViewProviderChanged(newPackage);
                    }
                }
            } else {
                i++;
            }
        }
        return;
        if (!(!updateWebView || (removedOrChangedOldPackage ^ 1) == 0 || str == null)) {
            this.mSystemInterface.killPackageDependents(str);
        }
        return;
    }

    void prepareWebViewInSystemServer() {
        try {
            synchronized (this.mLock) {
                this.mCurrentWebViewPackage = findPreferredWebViewPackage();
                this.mSystemInterface.updateUserSetting(this.mContext, this.mCurrentWebViewPackage.packageName);
                onWebViewProviderChanged(this.mCurrentWebViewPackage);
            }
        } catch (Throwable t) {
            Slog.e(TAG, "error preparing webview provider from system server", t);
        }
    }

    String changeProviderAndSetting(String newProviderName) {
        PackageInfo newPackage = updateCurrentWebViewPackage(newProviderName);
        if (newPackage == null) {
            return "";
        }
        return newPackage.packageName;
    }

    PackageInfo updateCurrentWebViewPackage(String newProviderName) {
        PackageInfo oldPackage;
        PackageInfo newPackage;
        boolean providerChanged;
        synchronized (this.mLock) {
            oldPackage = this.mCurrentWebViewPackage;
            if (newProviderName != null) {
                this.mSystemInterface.updateUserSetting(this.mContext, newProviderName);
            }
            try {
                newPackage = findPreferredWebViewPackage();
                if (oldPackage != null) {
                    providerChanged = newPackage.packageName.equals(oldPackage.packageName) ^ 1;
                } else {
                    providerChanged = true;
                }
                if (providerChanged) {
                    onWebViewProviderChanged(newPackage);
                }
            } catch (WebViewPackageMissingException e) {
                this.mCurrentWebViewPackage = null;
                Slog.e(TAG, "Couldn't find WebView package to use " + e);
                return null;
            }
        }
        if (providerChanged && oldPackage != null) {
            this.mSystemInterface.killPackageDependents(oldPackage.packageName);
        }
        return newPackage;
    }

    private void onWebViewProviderChanged(PackageInfo newPackage) {
        synchronized (this.mLock) {
            this.mAnyWebViewInstalled = true;
            if (this.mNumRelroCreationsStarted == this.mNumRelroCreationsFinished) {
                this.mCurrentWebViewPackage = newPackage;
                this.mNumRelroCreationsStarted = this.NUMBER_OF_RELROS_UNKNOWN;
                this.mNumRelroCreationsFinished = 0;
                this.mNumRelroCreationsStarted = this.mSystemInterface.onWebViewProviderChanged(newPackage);
                checkIfRelrosDoneLocked();
            } else {
                this.mWebViewPackageDirty = true;
            }
        }
    }

    WebViewProviderInfo[] getValidWebViewPackages() {
        ProviderAndPackageInfo[] providersAndPackageInfos = getValidWebViewPackagesAndInfos();
        WebViewProviderInfo[] providers = new WebViewProviderInfo[providersAndPackageInfos.length];
        for (int n = 0; n < providersAndPackageInfos.length; n++) {
            providers[n] = providersAndPackageInfos[n].provider;
        }
        return providers;
    }

    private ProviderAndPackageInfo[] getValidWebViewPackagesAndInfos() {
        WebViewProviderInfo[] allProviders = this.mSystemInterface.getWebViewPackages();
        List<ProviderAndPackageInfo> providers = new ArrayList();
        for (int n = 0; n < allProviders.length; n++) {
            try {
                PackageInfo packageInfo = this.mSystemInterface.getPackageInfoForProvider(allProviders[n]);
                if (isValidProvider(allProviders[n], packageInfo)) {
                    providers.add(new ProviderAndPackageInfo(allProviders[n], packageInfo));
                    if (allProviders[n] != null) {
                        Log.i(TAG, "getValidWebViewPackagesAndInfos = : " + allProviders[n].packageName);
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }
        return (ProviderAndPackageInfo[]) providers.toArray(new ProviderAndPackageInfo[providers.size()]);
    }

    private PackageInfo findPreferredWebViewPackage() throws WebViewPackageMissingException {
        ProviderAndPackageInfo[] providers = getValidWebViewPackagesAndInfos();
        String userChosenProvider = this.mSystemInterface.getUserChosenWebViewProvider(this.mContext);
        for (ProviderAndPackageInfo providerAndPackage : providers) {
            if (providerAndPackage.provider.packageName.equals(userChosenProvider) && isInstalledAndEnabledForAllUsers(this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, providerAndPackage.provider))) {
                return providerAndPackage.packageInfo;
            }
        }
        for (ProviderAndPackageInfo providerAndPackage2 : providers) {
            if (providerAndPackage2.provider.availableByDefault && isInstalledAndEnabledForAllUsers(this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, providerAndPackage2.provider))) {
                return providerAndPackage2.packageInfo;
            }
        }
        this.mAnyWebViewInstalled = false;
        throw new WebViewPackageMissingException("Could not find a loadable WebView package");
    }

    static boolean isInstalledAndEnabledForAllUsers(List<UserPackage> userPackages) {
        for (UserPackage userPackage : userPackages) {
            if (userPackage.isInstalledPackage()) {
                if ((userPackage.isEnabledPackage() ^ 1) != 0) {
                }
            }
            return false;
        }
        return true;
    }

    void notifyRelroCreationCompleted() {
        synchronized (this.mLock) {
            this.mNumRelroCreationsFinished++;
            checkIfRelrosDoneLocked();
        }
    }

    WebViewProviderResponse waitForAndGetProvider() {
        boolean webViewReady;
        PackageInfo webViewPackage;
        long timeoutTimeMs = (System.nanoTime() / 1000000) + 1000;
        int webViewStatus = 0;
        synchronized (this.mLock) {
            webViewReady = webViewIsReadyLocked();
            while (!webViewReady) {
                long timeNowMs = System.nanoTime() / 1000000;
                if (timeNowMs >= timeoutTimeMs) {
                    break;
                }
                try {
                    this.mLock.wait(timeoutTimeMs - timeNowMs);
                } catch (InterruptedException e) {
                }
                webViewReady = webViewIsReadyLocked();
            }
            webViewPackage = this.mCurrentWebViewPackage;
            if (!webViewReady) {
                if (this.mAnyWebViewInstalled) {
                    webViewStatus = 3;
                    Slog.e(TAG, "Timed out waiting for relro creation, relros started " + this.mNumRelroCreationsStarted + " relros finished " + this.mNumRelroCreationsFinished + " package dirty? " + this.mWebViewPackageDirty);
                } else {
                    webViewStatus = 4;
                }
            }
        }
        if (!webViewReady) {
            Slog.w(TAG, "creating relro file timed out");
        }
        return new WebViewProviderResponse(webViewPackage, webViewStatus);
    }

    PackageInfo getCurrentWebViewPackage() {
        PackageInfo packageInfo;
        synchronized (this.mLock) {
            packageInfo = this.mCurrentWebViewPackage;
        }
        return packageInfo;
    }

    private boolean webViewIsReadyLocked() {
        if (this.mWebViewPackageDirty || this.mNumRelroCreationsStarted != this.mNumRelroCreationsFinished) {
            return false;
        }
        return this.mAnyWebViewInstalled;
    }

    private void checkIfRelrosDoneLocked() {
        if (this.mNumRelroCreationsStarted != this.mNumRelroCreationsFinished) {
            return;
        }
        if (this.mWebViewPackageDirty) {
            this.mWebViewPackageDirty = false;
            try {
                onWebViewProviderChanged(findPreferredWebViewPackage());
                return;
            } catch (WebViewPackageMissingException e) {
                this.mCurrentWebViewPackage = null;
                return;
            }
        }
        this.mLock.notifyAll();
    }

    boolean isValidProvider(WebViewProviderInfo configInfo, PackageInfo packageInfo) {
        return validityResult(configInfo, packageInfo) == 0;
    }

    private int validityResult(WebViewProviderInfo configInfo, PackageInfo packageInfo) {
        if (!UserPackage.hasCorrectTargetSdkVersion(packageInfo)) {
            return 1;
        }
        if (!versionCodeGE(packageInfo.versionCode, getMinimumVersionCode()) && (this.mSystemInterface.systemIsDebuggable() ^ 1) != 0) {
            return 2;
        }
        if (!providerHasValidSignature(configInfo, packageInfo, this.mSystemInterface)) {
            return 3;
        }
        if (WebViewFactory.getWebViewLibrary(packageInfo.applicationInfo) == null) {
            return 4;
        }
        return 0;
    }

    private static boolean versionCodeGE(int versionCode1, int versionCode2) {
        return versionCode1 / 100000 >= versionCode2 / 100000;
    }

    private int getMinimumVersionCode() {
        if (this.mMinimumVersionCode > 0) {
            return this.mMinimumVersionCode;
        }
        int minimumVersionCode = -1;
        for (WebViewProviderInfo provider : this.mSystemInterface.getWebViewPackages()) {
            if (provider.availableByDefault && (provider.isFallback ^ 1) != 0) {
                try {
                    int versionCode = this.mSystemInterface.getFactoryPackageVersion(provider.packageName);
                    if (minimumVersionCode < 0 || versionCode < minimumVersionCode) {
                        minimumVersionCode = versionCode;
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        this.mMinimumVersionCode = minimumVersionCode;
        return this.mMinimumVersionCode;
    }

    private static boolean providerHasValidSignature(WebViewProviderInfo provider, PackageInfo packageInfo, SystemInterface systemInterface) {
        if (systemInterface.systemIsDebuggable()) {
            return true;
        }
        if (provider.signatures == null || provider.signatures.length == 0) {
            return packageInfo.applicationInfo.isSystemApp();
        }
        Signature[] packageSignatures = packageInfo.signatures;
        if (packageSignatures.length != 1) {
            return false;
        }
        byte[] packageSignature = packageSignatures[0].toByteArray();
        for (String signature : provider.signatures) {
            if (Arrays.equals(packageSignature, Base64.decode(signature, 0))) {
                return true;
            }
        }
        return false;
    }

    void dumpState(PrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mCurrentWebViewPackage == null) {
                pw.println("  Current WebView package is null");
            } else {
                pw.println(String.format("  Current WebView package (name, version): (%s, %s)", new Object[]{this.mCurrentWebViewPackage.packageName, this.mCurrentWebViewPackage.versionName}));
            }
            pw.println(String.format("  Minimum WebView version code: %d", new Object[]{Integer.valueOf(this.mMinimumVersionCode)}));
            pw.println(String.format("  Number of relros started: %d", new Object[]{Integer.valueOf(this.mNumRelroCreationsStarted)}));
            pw.println(String.format("  Number of relros finished: %d", new Object[]{Integer.valueOf(this.mNumRelroCreationsFinished)}));
            pw.println(String.format("  WebView package dirty: %b", new Object[]{Boolean.valueOf(this.mWebViewPackageDirty)}));
            pw.println(String.format("  Any WebView package installed: %b", new Object[]{Boolean.valueOf(this.mAnyWebViewInstalled)}));
            try {
                PackageInfo preferredWebViewPackage = findPreferredWebViewPackage();
                pw.println(String.format("  Preferred WebView package (name, version): (%s, %s)", new Object[]{preferredWebViewPackage.packageName, preferredWebViewPackage.versionName}));
            } catch (WebViewPackageMissingException e) {
                pw.println(String.format("  Preferred WebView package: none", new Object[0]));
            }
            dumpAllPackageInformationLocked(pw);
        }
        return;
    }

    private void dumpAllPackageInformationLocked(PrintWriter pw) {
        WebViewProviderInfo[] allProviders = this.mSystemInterface.getWebViewPackages();
        pw.println("  WebView packages:");
        for (WebViewProviderInfo provider : allProviders) {
            PackageInfo systemUserPackageInfo = ((UserPackage) this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, provider).get(0)).getPackageInfo();
            if (systemUserPackageInfo != null) {
                int validity = validityResult(provider, systemUserPackageInfo);
                String packageDetails = String.format("versionName: %s, versionCode: %d, targetSdkVersion: %d", new Object[]{systemUserPackageInfo.versionName, Integer.valueOf(systemUserPackageInfo.versionCode), Integer.valueOf(systemUserPackageInfo.applicationInfo.targetSdkVersion)});
                if (validity == 0) {
                    boolean installedForAllUsers = isInstalledAndEnabledForAllUsers(this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, provider));
                    String str = "    Valid package %s (%s) is %s installed/enabled for all users";
                    Object[] objArr = new Object[3];
                    objArr[0] = systemUserPackageInfo.packageName;
                    objArr[1] = packageDetails;
                    objArr[2] = installedForAllUsers ? "" : "NOT";
                    pw.println(String.format(str, objArr));
                } else {
                    pw.println(String.format("    Invalid package %s (%s), reason: %s", new Object[]{systemUserPackageInfo.packageName, packageDetails, getInvalidityReason(validity)}));
                }
            }
        }
    }

    private static String getInvalidityReason(int invalidityReason) {
        switch (invalidityReason) {
            case 1:
                return "SDK version too low";
            case 2:
                return "Version code too low";
            case 3:
                return "Incorrect signature";
            case 4:
                return "No WebView-library manifest flag";
            default:
                return "Unexcepted validity-reason";
        }
    }
}
