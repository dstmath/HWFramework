package com.android.server.webkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Slog;
import android.webkit.WebViewFactory;
import android.webkit.WebViewFactory.MissingWebViewPackageException;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewProviderResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebViewUpdateServiceImpl {
    private static final String TAG = null;
    private Context mContext;
    private SystemInterface mSystemInterface;
    private WebViewUpdater mWebViewUpdater;

    private static class WebViewUpdater {
        private static final int WAIT_TIMEOUT_MS = 1000;
        private int NUMBER_OF_RELROS_UNKNOWN;
        private boolean mAnyWebViewInstalled;
        private Context mContext;
        private PackageInfo mCurrentWebViewPackage;
        private Object mLock;
        private int mMinimumVersionCode;
        private int mNumRelroCreationsFinished;
        private int mNumRelroCreationsStarted;
        private SystemInterface mSystemInterface;
        private boolean mWebViewPackageDirty;

        private static class ProviderAndPackageInfo {
            public final PackageInfo packageInfo;
            public final WebViewProviderInfo provider;

            public ProviderAndPackageInfo(WebViewProviderInfo provider, PackageInfo packageInfo) {
                this.provider = provider;
                this.packageInfo = packageInfo;
            }
        }

        public WebViewUpdater(Context context, SystemInterface systemInterface) {
            this.mMinimumVersionCode = -1;
            this.mNumRelroCreationsStarted = 0;
            this.mNumRelroCreationsFinished = 0;
            this.mWebViewPackageDirty = false;
            this.mAnyWebViewInstalled = false;
            this.NUMBER_OF_RELROS_UNKNOWN = Integer.MAX_VALUE;
            this.mCurrentWebViewPackage = null;
            this.mLock = new Object();
            this.mContext = context;
            this.mSystemInterface = systemInterface;
        }

        public void packageStateChanged(String packageName, int changedState) {
            for (WebViewProviderInfo provider : this.mSystemInterface.getWebViewPackages()) {
                if (provider.packageName.equals(packageName)) {
                    boolean updateWebView = false;
                    boolean removedOrChangedOldPackage = false;
                    String str = null;
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
                            } catch (MissingWebViewPackageException e) {
                                Slog.e(WebViewUpdateServiceImpl.TAG, "Could not find valid WebView package to create relro with " + e);
                            }
                        }
                        updateWebView = (provider.packageName.equals(newPackage.packageName) || provider.packageName.equals(str)) ? true : this.mCurrentWebViewPackage == null;
                        removedOrChangedOldPackage = provider.packageName.equals(str);
                        if (updateWebView) {
                            onWebViewProviderChanged(newPackage);
                        }
                        if (!(!updateWebView || removedOrChangedOldPackage || str == null)) {
                            this.mSystemInterface.killPackageDependents(str);
                        }
                        return;
                    }
                }
            }
        }

        public void prepareWebViewInSystemServer() {
            try {
                synchronized (this.mLock) {
                    this.mCurrentWebViewPackage = findPreferredWebViewPackage();
                    this.mSystemInterface.updateUserSetting(this.mContext, this.mCurrentWebViewPackage.packageName);
                    onWebViewProviderChanged(this.mCurrentWebViewPackage);
                }
            } catch (Throwable t) {
                Slog.e(WebViewUpdateServiceImpl.TAG, "error preparing webview provider from system server", t);
            }
        }

        public String changeProviderAndSetting(String newProviderName) {
            PackageInfo newPackage;
            synchronized (this.mLock) {
                boolean providerChanged;
                PackageInfo oldPackage = this.mCurrentWebViewPackage;
                this.mSystemInterface.updateUserSetting(this.mContext, newProviderName);
                try {
                    newPackage = findPreferredWebViewPackage();
                    providerChanged = oldPackage != null ? !newPackage.packageName.equals(oldPackage.packageName) : true;
                    if (providerChanged) {
                        onWebViewProviderChanged(newPackage);
                    }
                } catch (MissingWebViewPackageException e) {
                    Slog.e(WebViewUpdateServiceImpl.TAG, "Tried to change WebView provider but failed to fetch WebView package " + e);
                    return "";
                }
            }
            if (providerChanged && oldPackage != null) {
                this.mSystemInterface.killPackageDependents(oldPackage.packageName);
            }
            return newPackage.packageName;
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

        private ProviderAndPackageInfo[] getValidWebViewPackagesAndInfos(boolean onlyInstalled) {
            WebViewProviderInfo[] allProviders = this.mSystemInterface.getWebViewPackages();
            List<ProviderAndPackageInfo> providers = new ArrayList();
            int n = 0;
            while (n < allProviders.length) {
                try {
                    PackageInfo packageInfo = this.mSystemInterface.getPackageInfoForProvider(allProviders[n]);
                    if ((!onlyInstalled || WebViewUpdateServiceImpl.isInstalledPackage(packageInfo)) && isValidProvider(allProviders[n], packageInfo)) {
                        providers.add(new ProviderAndPackageInfo(allProviders[n], packageInfo));
                    }
                } catch (NameNotFoundException e) {
                }
                n++;
            }
            return (ProviderAndPackageInfo[]) providers.toArray(new ProviderAndPackageInfo[providers.size()]);
        }

        public WebViewProviderInfo[] getValidAndInstalledWebViewPackages() {
            ProviderAndPackageInfo[] providersAndPackageInfos = getValidWebViewPackagesAndInfos(true);
            WebViewProviderInfo[] providers = new WebViewProviderInfo[providersAndPackageInfos.length];
            for (int n = 0; n < providersAndPackageInfos.length; n++) {
                providers[n] = providersAndPackageInfos[n].provider;
            }
            return providers;
        }

        private PackageInfo findPreferredWebViewPackage() {
            ProviderAndPackageInfo[] providers = getValidWebViewPackagesAndInfos(false);
            String userChosenProvider = this.mSystemInterface.getUserChosenWebViewProvider(this.mContext);
            for (ProviderAndPackageInfo providerAndPackage : providers) {
                if (providerAndPackage.provider.packageName.equals(userChosenProvider) && WebViewUpdateServiceImpl.isInstalledPackage(providerAndPackage.packageInfo) && WebViewUpdateServiceImpl.isEnabledPackage(providerAndPackage.packageInfo)) {
                    return providerAndPackage.packageInfo;
                }
            }
            for (ProviderAndPackageInfo providerAndPackage2 : providers) {
                if (providerAndPackage2.provider.availableByDefault && WebViewUpdateServiceImpl.isInstalledPackage(providerAndPackage2.packageInfo) && WebViewUpdateServiceImpl.isEnabledPackage(providerAndPackage2.packageInfo)) {
                    return providerAndPackage2.packageInfo;
                }
            }
            for (ProviderAndPackageInfo providerAndPackage22 : providers) {
                if (providerAndPackage22.provider.availableByDefault) {
                    return providerAndPackage22.packageInfo;
                }
            }
            this.mAnyWebViewInstalled = false;
            throw new MissingWebViewPackageException("Could not find a loadable WebView package");
        }

        public void notifyRelroCreationCompleted() {
            synchronized (this.mLock) {
                this.mNumRelroCreationsFinished++;
                checkIfRelrosDoneLocked();
            }
        }

        public WebViewProviderResponse waitForAndGetProvider() {
            PackageInfo webViewPackage;
            long timeoutTimeMs = (System.nanoTime() / 1000000) + 1000;
            int webViewStatus = 0;
            synchronized (this.mLock) {
                boolean webViewReady = webViewIsReadyLocked();
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
                        Slog.e(WebViewUpdateServiceImpl.TAG, "Timed out waiting for relro creation, relros started " + this.mNumRelroCreationsStarted + " relros finished " + this.mNumRelroCreationsFinished + " package dirty? " + this.mWebViewPackageDirty);
                    } else {
                        webViewStatus = 4;
                    }
                }
            }
            if (!webViewReady) {
                Slog.w(WebViewUpdateServiceImpl.TAG, "creating relro file timed out");
            }
            return new WebViewProviderResponse(webViewPackage, webViewStatus);
        }

        public String getCurrentWebViewPackageName() {
            synchronized (this.mLock) {
                if (this.mCurrentWebViewPackage == null) {
                    return null;
                }
                String str = this.mCurrentWebViewPackage.packageName;
                return str;
            }
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
                } catch (MissingWebViewPackageException e) {
                    return;
                }
            }
            this.mLock.notifyAll();
        }

        private static boolean versionCodeGE(int versionCode1, int versionCode2) {
            return versionCode1 / 100000 >= versionCode2 / 100000;
        }

        public boolean isValidProvider(WebViewProviderInfo configInfo, PackageInfo packageInfo) {
            if ((versionCodeGE(packageInfo.versionCode, getMinimumVersionCode()) || this.mSystemInterface.systemIsDebuggable()) && WebViewUpdateServiceImpl.providerHasValidSignature(configInfo, packageInfo, this.mSystemInterface) && WebViewFactory.getWebViewLibrary(packageInfo.applicationInfo) != null) {
                return true;
            }
            return false;
        }

        private int getMinimumVersionCode() {
            if (this.mMinimumVersionCode > 0) {
                return this.mMinimumVersionCode;
            }
            for (WebViewProviderInfo provider : this.mSystemInterface.getWebViewPackages()) {
                if (provider.availableByDefault && !provider.isFallback) {
                    try {
                        int versionCode = this.mSystemInterface.getFactoryPackageVersion(provider.packageName);
                        if (this.mMinimumVersionCode < 0 || versionCode < this.mMinimumVersionCode) {
                            this.mMinimumVersionCode = versionCode;
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
            }
            return this.mMinimumVersionCode;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.webkit.WebViewUpdateServiceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.webkit.WebViewUpdateServiceImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.webkit.WebViewUpdateServiceImpl.<clinit>():void");
    }

    public WebViewUpdateServiceImpl(Context context, SystemInterface systemInterface) {
        this.mContext = context;
        this.mSystemInterface = systemInterface;
        this.mWebViewUpdater = new WebViewUpdater(this.mContext, this.mSystemInterface);
    }

    void packageStateChanged(String packageName, int changedState, int userId) {
        updateFallbackStateOnPackageChange(packageName, changedState);
        this.mWebViewUpdater.packageStateChanged(packageName, changedState);
    }

    void prepareWebViewInSystemServer() {
        updateFallbackStateOnBoot();
        this.mWebViewUpdater.prepareWebViewInSystemServer();
    }

    private boolean existsValidNonFallbackProvider(WebViewProviderInfo[] providers) {
        for (WebViewProviderInfo provider : providers) {
            if (provider.availableByDefault && !provider.isFallback) {
                try {
                    PackageInfo packageInfo = this.mSystemInterface.getPackageInfoForProvider(provider);
                    if (isInstalledPackage(packageInfo) && isEnabledPackage(packageInfo) && this.mWebViewUpdater.isValidProvider(provider, packageInfo)) {
                        return true;
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        return false;
    }

    void handleNewUser(int userId) {
        if (this.mSystemInterface.isFallbackLogicEnabled()) {
            WebViewProviderInfo[] webviewProviders = this.mSystemInterface.getWebViewPackages();
            WebViewProviderInfo fallbackProvider = getFallbackProvider(webviewProviders);
            if (fallbackProvider != null) {
                this.mSystemInterface.enablePackageForUser(fallbackProvider.packageName, !existsValidNonFallbackProvider(webviewProviders), userId);
            }
        }
    }

    void notifyRelroCreationCompleted() {
        this.mWebViewUpdater.notifyRelroCreationCompleted();
    }

    WebViewProviderResponse waitForAndGetProvider() {
        return this.mWebViewUpdater.waitForAndGetProvider();
    }

    String changeProviderAndSetting(String newProvider) {
        return this.mWebViewUpdater.changeProviderAndSetting(newProvider);
    }

    WebViewProviderInfo[] getValidWebViewPackages() {
        return this.mWebViewUpdater.getValidAndInstalledWebViewPackages();
    }

    WebViewProviderInfo[] getWebViewPackages() {
        return this.mSystemInterface.getWebViewPackages();
    }

    String getCurrentWebViewPackageName() {
        return this.mWebViewUpdater.getCurrentWebViewPackageName();
    }

    void enableFallbackLogic(boolean enable) {
        this.mSystemInterface.enableFallbackLogic(enable);
    }

    private void updateFallbackStateOnBoot() {
        if (this.mSystemInterface.isFallbackLogicEnabled()) {
            updateFallbackState(this.mSystemInterface.getWebViewPackages(), true);
        }
    }

    private void updateFallbackStateOnPackageChange(String changedPackage, int changedState) {
        if (this.mSystemInterface.isFallbackLogicEnabled()) {
            WebViewProviderInfo[] webviewProviders = this.mSystemInterface.getWebViewPackages();
            boolean changedPackageAvailableByDefault = false;
            for (WebViewProviderInfo provider : webviewProviders) {
                if (provider.packageName.equals(changedPackage)) {
                    if (provider.availableByDefault) {
                        changedPackageAvailableByDefault = true;
                    }
                    if (!changedPackageAvailableByDefault) {
                        updateFallbackState(webviewProviders, false);
                    }
                }
            }
            if (!changedPackageAvailableByDefault) {
                updateFallbackState(webviewProviders, false);
            }
        }
    }

    private void updateFallbackState(WebViewProviderInfo[] webviewProviders, boolean isBoot) {
        WebViewProviderInfo fallbackProvider = getFallbackProvider(webviewProviders);
        if (fallbackProvider != null) {
            boolean existsValidNonFallbackProvider = existsValidNonFallbackProvider(webviewProviders);
            try {
                boolean isFallbackEnabled = isEnabledPackage(this.mSystemInterface.getPackageInfoForProvider(fallbackProvider));
                if (existsValidNonFallbackProvider && (isFallbackEnabled || isBoot)) {
                    this.mSystemInterface.uninstallAndDisablePackageForAllUsers(this.mContext, fallbackProvider.packageName);
                } else if (!existsValidNonFallbackProvider && (!isFallbackEnabled || isBoot)) {
                    this.mSystemInterface.enablePackageForAllUsers(this.mContext, fallbackProvider.packageName, true);
                }
            } catch (NameNotFoundException e) {
            }
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

    boolean isFallbackPackage(String packageName) {
        boolean z = false;
        if (packageName == null || !this.mSystemInterface.isFallbackLogicEnabled()) {
            return false;
        }
        WebViewProviderInfo fallbackProvider = getFallbackProvider(this.mSystemInterface.getWebViewPackages());
        if (fallbackProvider != null) {
            z = packageName.equals(fallbackProvider.packageName);
        }
        return z;
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

    private static boolean isEnabledPackage(PackageInfo packageInfo) {
        return packageInfo.applicationInfo.enabled;
    }

    private static boolean isInstalledPackage(PackageInfo packageInfo) {
        if ((packageInfo.applicationInfo.flags & 8388608) == 0 || (packageInfo.applicationInfo.privateFlags & 1) != 0) {
            return false;
        }
        return true;
    }
}
