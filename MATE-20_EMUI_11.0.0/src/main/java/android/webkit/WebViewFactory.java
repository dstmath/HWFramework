package android.webkit;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.util.AndroidRuntimeException;
import android.util.ArraySet;
import android.util.Log;
import android.webkit.IWebViewUpdateService;
import java.io.File;
import java.lang.reflect.Method;

@SystemApi
public final class WebViewFactory {
    private static final String CHROMIUM_WEBVIEW_FACTORY = "com.android.webview.chromium.WebViewChromiumFactoryProviderForQ";
    private static final String CHROMIUM_WEBVIEW_FACTORY_METHOD = "create";
    private static final boolean DEBUG = false;
    public static final int LIBLOAD_ADDRESS_SPACE_NOT_RESERVED = 2;
    public static final int LIBLOAD_FAILED_JNI_CALL = 7;
    public static final int LIBLOAD_FAILED_LISTING_WEBVIEW_PACKAGES = 4;
    public static final int LIBLOAD_FAILED_TO_FIND_NAMESPACE = 10;
    public static final int LIBLOAD_FAILED_TO_LOAD_LIBRARY = 6;
    public static final int LIBLOAD_FAILED_TO_OPEN_RELRO_FILE = 5;
    public static final int LIBLOAD_FAILED_WAITING_FOR_RELRO = 3;
    public static final int LIBLOAD_FAILED_WAITING_FOR_WEBVIEW_REASON_UNKNOWN = 8;
    public static final int LIBLOAD_SUCCESS = 0;
    public static final int LIBLOAD_WRONG_PACKAGE_NAME = 1;
    private static final String LOGTAG = "WebViewFactory";
    private static String WEBVIEW_UPDATE_SERVICE_NAME = "webviewupdate";
    private static String sDataDirectorySuffix;
    @UnsupportedAppUsage
    private static PackageInfo sPackageInfo;
    @UnsupportedAppUsage
    private static WebViewFactoryProvider sProviderInstance;
    private static final Object sProviderLock = new Object();
    private static boolean sWebViewDisabled;
    private static Boolean sWebViewSupported;

    private static String getWebViewPreparationErrorReason(int error) {
        if (error == 3) {
            return "Time out waiting for Relro files being created";
        }
        if (error == 4) {
            return "No WebView installed";
        }
        if (error != 8) {
            return "Unknown";
        }
        return "Crashed for unknown reason";
    }

    /* access modifiers changed from: package-private */
    public static class MissingWebViewPackageException extends Exception {
        public MissingWebViewPackageException(String message) {
            super(message);
        }

        public MissingWebViewPackageException(Exception e) {
            super(e);
        }
    }

    private static boolean isWebViewSupported() {
        if (sWebViewSupported == null) {
            sWebViewSupported = Boolean.valueOf(AppGlobals.getInitialApplication().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WEBVIEW));
        }
        return sWebViewSupported.booleanValue();
    }

    static void disableWebView() {
        synchronized (sProviderLock) {
            if (sProviderInstance == null) {
                sWebViewDisabled = true;
            } else {
                throw new IllegalStateException("Can't disable WebView: WebView already initialized");
            }
        }
    }

    static void setDataDirectorySuffix(String suffix) {
        synchronized (sProviderLock) {
            if (sProviderInstance != null) {
                throw new IllegalStateException("Can't set data directory suffix: WebView already initialized");
            } else if (suffix.indexOf(File.separatorChar) < 0) {
                sDataDirectorySuffix = suffix;
            } else {
                throw new IllegalArgumentException("Suffix " + suffix + " contains a path separator");
            }
        }
    }

    static String getDataDirectorySuffix() {
        String str;
        synchronized (sProviderLock) {
            str = sDataDirectorySuffix;
        }
        return str;
    }

    public static String getWebViewLibrary(ApplicationInfo ai) {
        if (ai.metaData != null) {
            return ai.metaData.getString("com.android.webview.WebViewLibrary");
        }
        return null;
    }

    public static PackageInfo getLoadedPackageInfo() {
        PackageInfo packageInfo;
        synchronized (sProviderLock) {
            packageInfo = sPackageInfo;
        }
        return packageInfo;
    }

    /* JADX DEBUG: Type inference failed for r0v1. Raw type applied. Possible types: java.lang.Class<?>, java.lang.Class<android.webkit.WebViewFactoryProvider> */
    public static Class<WebViewFactoryProvider> getWebViewProviderClass(ClassLoader clazzLoader) throws ClassNotFoundException {
        return Class.forName(CHROMIUM_WEBVIEW_FACTORY, true, clazzLoader);
    }

    public static int loadWebViewNativeLibraryFromPackage(String packageName, ClassLoader clazzLoader) {
        if (!isWebViewSupported()) {
            return 1;
        }
        try {
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            if (response.status != 0 && response.status != 3) {
                return response.status;
            }
            if (!response.packageInfo.packageName.equals(packageName)) {
                return 1;
            }
            try {
                int loadNativeRet = WebViewLibraryLoader.loadNativeLibrary(clazzLoader, getWebViewLibrary(AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, 268435584).applicationInfo));
                if (loadNativeRet == 0) {
                    return response.status;
                }
                return loadNativeRet;
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(LOGTAG, "Couldn't find package " + packageName);
                return 1;
            }
        } catch (RemoteException e2) {
            Log.e(LOGTAG, "error waiting for relro creation", e2);
            return 8;
        }
    }

    @UnsupportedAppUsage
    static WebViewFactoryProvider getProvider() {
        synchronized (sProviderLock) {
            if (sProviderInstance != null) {
                return sProviderInstance;
            }
            int uid = Process.myUid();
            if (uid == 0 || uid == 1000 || uid == 1001 || uid == 1027 || uid == 1002) {
                throw new UnsupportedOperationException("For security reasons, WebView is not allowed in privileged processes");
            } else if (!isWebViewSupported()) {
                throw new UnsupportedOperationException();
            } else if (!sWebViewDisabled) {
                Trace.traceBegin(16, "WebViewFactory.getProvider()");
                try {
                    Method staticFactory = null;
                    try {
                        staticFactory = getProviderClass().getMethod(CHROMIUM_WEBVIEW_FACTORY_METHOD, WebViewDelegate.class);
                    } catch (Exception e) {
                    }
                    Trace.traceBegin(16, "WebViewFactoryProvider invocation");
                    try {
                        sProviderInstance = (WebViewFactoryProvider) staticFactory.invoke(null, new WebViewDelegate());
                        WebViewFactoryProvider webViewFactoryProvider = sProviderInstance;
                        Trace.traceEnd(16);
                        return webViewFactoryProvider;
                    } catch (Exception e2) {
                        Log.e(LOGTAG, "error instantiating provider", e2);
                        throw new AndroidRuntimeException(e2);
                    } catch (Throwable th) {
                        Trace.traceEnd(16);
                        throw th;
                    }
                } finally {
                    Trace.traceEnd(16);
                }
            } else {
                throw new IllegalStateException("WebView.disableWebView() was called: WebView is disabled");
            }
        }
    }

    private static boolean signaturesEquals(Signature[] s1, Signature[] s2) {
        if (s1 == null) {
            if (s2 == null) {
                return true;
            }
            return false;
        } else if (s2 == null) {
            return false;
        } else {
            ArraySet<Signature> set1 = new ArraySet<>();
            for (Signature signature : s1) {
                set1.add(signature);
            }
            ArraySet<Signature> set2 = new ArraySet<>();
            for (Signature signature2 : s2) {
                set2.add(signature2);
            }
            return set1.equals(set2);
        }
    }

    private static void verifyPackageInfo(PackageInfo chosen, PackageInfo toUse) throws MissingWebViewPackageException {
        if (!chosen.packageName.equals(toUse.packageName)) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, packageName mismatch, expected: " + chosen.packageName + " actual: " + toUse.packageName);
        } else if (chosen.getLongVersionCode() > toUse.getLongVersionCode()) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, version code is lower than expected: " + chosen.getLongVersionCode() + " actual: " + toUse.getLongVersionCode());
        } else if (getWebViewLibrary(toUse.applicationInfo) == null) {
            throw new MissingWebViewPackageException("Tried to load an invalid WebView provider: " + toUse.packageName);
        } else if (!signaturesEquals(chosen.signatures, toUse.signatures)) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, signature mismatch");
        }
    }

    /* JADX INFO: finally extract failed */
    @UnsupportedAppUsage
    private static Context getWebViewContextAndSetProvider() throws MissingWebViewPackageException {
        Application initialApplication = AppGlobals.getInitialApplication();
        Trace.traceBegin(16, "WebViewUpdateService.waitForAndGetProvider()");
        try {
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            try {
                Trace.traceEnd(16);
                if (response.status != 0) {
                    if (response.status != 3) {
                        throw new MissingWebViewPackageException("Failed to load WebView provider: " + getWebViewPreparationErrorReason(response.status));
                    }
                }
                Trace.traceBegin(16, "ActivityManager.addPackageDependency()");
                try {
                    ActivityManager.getService().addPackageDependency(response.packageInfo.packageName);
                    Trace.traceEnd(16);
                    PackageManager pm = initialApplication.getPackageManager();
                    Trace.traceBegin(16, "PackageManager.getPackageInfo()");
                    try {
                        PackageInfo newPackageInfo = pm.getPackageInfo(response.packageInfo.packageName, 268444864);
                        Trace.traceEnd(16);
                        verifyPackageInfo(response.packageInfo, newPackageInfo);
                        ApplicationInfo ai = newPackageInfo.applicationInfo;
                        Trace.traceBegin(16, "initialApplication.createApplicationContext");
                        try {
                            Context webViewContext = initialApplication.createApplicationContext(ai, 3);
                            sPackageInfo = newPackageInfo;
                            return webViewContext;
                        } finally {
                            Trace.traceEnd(16);
                        }
                    } catch (Throwable th) {
                        Trace.traceEnd(16);
                        throw th;
                    }
                } catch (Throwable th2) {
                    Trace.traceEnd(16);
                    throw th2;
                }
            } catch (PackageManager.NameNotFoundException | RemoteException e) {
                throw new MissingWebViewPackageException("Failed to load WebView provider: " + e);
            }
        } catch (Throwable th3) {
            Trace.traceEnd(16);
            throw th3;
        }
    }

    /* JADX INFO: finally extract failed */
    @UnsupportedAppUsage
    private static Class<WebViewFactoryProvider> getProviderClass() {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewFactory.getWebViewContextAndSetProvider()");
            try {
                Context webViewContext = getWebViewContextAndSetProvider();
                Trace.traceEnd(16);
                Log.i(LOGTAG, "Loading " + sPackageInfo.packageName + " version " + sPackageInfo.versionName + " (code " + sPackageInfo.getLongVersionCode() + ")");
                Trace.traceBegin(16, "WebViewFactory.getChromiumProviderClass()");
                try {
                    for (String newAssetPath : webViewContext.getApplicationInfo().getAllApkPaths()) {
                        initialApplication.getAssets().addAssetPathAsSharedLibrary(newAssetPath);
                    }
                    ClassLoader clazzLoader = webViewContext.getClassLoader();
                    Trace.traceBegin(16, "WebViewFactory.loadNativeLibrary()");
                    WebViewLibraryLoader.loadNativeLibrary(clazzLoader, getWebViewLibrary(sPackageInfo.applicationInfo));
                    Trace.traceEnd(16);
                    Trace.traceBegin(16, "Class.forName()");
                    try {
                        Class<WebViewFactoryProvider> webViewProviderClass = getWebViewProviderClass(clazzLoader);
                        Trace.traceEnd(16);
                        return webViewProviderClass;
                    } finally {
                        Trace.traceEnd(16);
                    }
                } catch (ClassNotFoundException e) {
                    Log.e(LOGTAG, "error loading provider", e);
                    throw new AndroidRuntimeException(e);
                } catch (Throwable th) {
                    Trace.traceEnd(16);
                    throw th;
                }
            } catch (Throwable th2) {
                Trace.traceEnd(16);
                throw th2;
            }
        } catch (MissingWebViewPackageException e2) {
            Log.e(LOGTAG, "Chromium WebView package does not exist", e2);
            throw new AndroidRuntimeException(e2);
        }
    }

    public static void prepareWebViewInZygote() {
        try {
            WebViewLibraryLoader.reserveAddressSpaceInZygote();
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing native loader", t);
        }
    }

    public static int onWebViewProviderChanged(PackageInfo packageInfo) {
        int startedRelroProcesses = 0;
        try {
            startedRelroProcesses = WebViewLibraryLoader.prepareNativeLibraries(packageInfo);
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing webview native library", t);
        }
        WebViewZygote.onWebViewProviderChanged(packageInfo);
        return startedRelroProcesses;
    }

    @UnsupportedAppUsage
    public static IWebViewUpdateService getUpdateService() {
        if (isWebViewSupported()) {
            return getUpdateServiceUnchecked();
        }
        return null;
    }

    static IWebViewUpdateService getUpdateServiceUnchecked() {
        return IWebViewUpdateService.Stub.asInterface(ServiceManager.getService(WEBVIEW_UPDATE_SERVICE_NAME));
    }
}
