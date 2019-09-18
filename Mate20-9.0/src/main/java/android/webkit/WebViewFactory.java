package android.webkit;

import android.annotation.SystemApi;
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
    private static final String CHROMIUM_WEBVIEW_FACTORY = "com.android.webview.chromium.WebViewChromiumFactoryProviderForP";
    private static final String CHROMIUM_WEBVIEW_FACTORY_METHOD = "create";
    public static final String CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY = "persist.sys.webview.vmsize";
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
    private static PackageInfo sPackageInfo;
    private static WebViewFactoryProvider sProviderInstance;
    private static final Object sProviderLock = new Object();
    private static boolean sWebViewDisabled;
    private static Boolean sWebViewSupported;

    static class MissingWebViewPackageException extends Exception {
        public MissingWebViewPackageException(String message) {
            super(message);
        }

        public MissingWebViewPackageException(Exception e) {
            super(e);
        }
    }

    private static String getWebViewPreparationErrorReason(int error) {
        if (error == 8) {
            return "Crashed for unknown reason";
        }
        switch (error) {
            case 3:
                return "Time out waiting for Relro files being created";
            case 4:
                return "No WebView installed";
            default:
                return "Unknown";
        }
    }

    private static boolean isWebViewSupported() {
        if (sWebViewSupported == null) {
            sWebViewSupported = Boolean.valueOf(AppGlobals.getInitialApplication().getPackageManager().hasSystemFeature("android.software.webview"));
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

    static WebViewFactoryProvider getProvider() {
        synchronized (sProviderLock) {
            if (sProviderInstance != null) {
                WebViewFactoryProvider webViewFactoryProvider = sProviderInstance;
                return webViewFactoryProvider;
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
                        staticFactory = getProviderClass().getMethod(CHROMIUM_WEBVIEW_FACTORY_METHOD, new Class[]{WebViewDelegate.class});
                    } catch (NoSuchMethodException e) {
                    }
                    Trace.traceBegin(16, "WebViewFactoryProvider invocation");
                    sProviderInstance = (WebViewFactoryProvider) staticFactory.invoke(null, new Object[]{new WebViewDelegate()});
                    WebViewFactoryProvider webViewFactoryProvider2 = sProviderInstance;
                    Trace.traceEnd(16);
                    Trace.traceEnd(16);
                    return webViewFactoryProvider2;
                } catch (Exception e2) {
                    Log.e(LOGTAG, "error instantiating provider", e2);
                    throw new AndroidRuntimeException(e2);
                } catch (Throwable th) {
                    Trace.traceEnd(16);
                    throw th;
                }
            } else {
                throw new IllegalStateException("WebView.disableWebView() was called: WebView is disabled");
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: boolean} */
    /* JADX WARNING: type inference failed for: r0v0 */
    /* JADX WARNING: type inference failed for: r0v1, types: [int] */
    /* JADX WARNING: type inference failed for: r0v5 */
    /* JADX WARNING: type inference failed for: r0v6 */
    /* JADX WARNING: Multi-variable type inference failed */
    private static boolean signaturesEquals(Signature[] s1, Signature[] s2) {
        ? r0 = 0;
        if (s1 == null) {
            if (s2 == null) {
                r0 = 1;
            }
            return r0;
        } else if (s2 == null) {
            return false;
        } else {
            ArraySet<Signature> set1 = new ArraySet<>();
            for (Signature signature : s1) {
                set1.add(signature);
            }
            ArraySet<Signature> set2 = new ArraySet<>();
            int length = s2.length;
            while (r0 < length) {
                set2.add(s2[r0]);
                r0++;
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

    private static void fixupStubApplicationInfo(ApplicationInfo ai, PackageManager pm) throws MissingWebViewPackageException {
        String donorPackageName = null;
        if (ai.metaData != null) {
            donorPackageName = ai.metaData.getString("com.android.webview.WebViewDonorPackage");
        }
        if (donorPackageName != null) {
            try {
                ApplicationInfo donorInfo = pm.getPackageInfo(donorPackageName, 270541824).applicationInfo;
                ai.sourceDir = donorInfo.sourceDir;
                ai.splitSourceDirs = donorInfo.splitSourceDirs;
                ai.nativeLibraryDir = donorInfo.nativeLibraryDir;
                ai.secondaryNativeLibraryDir = donorInfo.secondaryNativeLibraryDir;
                ai.primaryCpuAbi = donorInfo.primaryCpuAbi;
                ai.secondaryCpuAbi = donorInfo.secondaryCpuAbi;
            } catch (PackageManager.NameNotFoundException e) {
                throw new MissingWebViewPackageException("Failed to find donor package: " + donorPackageName);
            }
        }
    }

    private static Context getWebViewContextAndSetProvider() throws MissingWebViewPackageException {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewUpdateService.waitForAndGetProvider()");
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            Trace.traceEnd(16);
            if (response.status != 0) {
                if (response.status != 3) {
                    throw new MissingWebViewPackageException("Failed to load WebView provider: " + getWebViewPreparationErrorReason(response.status));
                }
            }
            Trace.traceBegin(16, "ActivityManager.addPackageDependency()");
            ActivityManager.getService().addPackageDependency(response.packageInfo.packageName);
            Trace.traceEnd(16);
            PackageManager pm = initialApplication.getPackageManager();
            Trace.traceBegin(16, "PackageManager.getPackageInfo()");
            PackageInfo newPackageInfo = pm.getPackageInfo(response.packageInfo.packageName, 268444864);
            Trace.traceEnd(16);
            verifyPackageInfo(response.packageInfo, newPackageInfo);
            ApplicationInfo ai = newPackageInfo.applicationInfo;
            fixupStubApplicationInfo(ai, pm);
            Trace.traceBegin(16, "initialApplication.createApplicationContext");
            Context webViewContext = initialApplication.createApplicationContext(ai, 3);
            sPackageInfo = newPackageInfo;
            Trace.traceEnd(16);
            return webViewContext;
        } catch (PackageManager.NameNotFoundException | RemoteException e) {
            throw new MissingWebViewPackageException("Failed to load WebView provider: " + e);
        } catch (Throwable th) {
            Trace.traceEnd(16);
            throw th;
        }
    }

    private static Class<WebViewFactoryProvider> getProviderClass() {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewFactory.getWebViewContextAndSetProvider()");
            Context webViewContext = getWebViewContextAndSetProvider();
            Trace.traceEnd(16);
            Log.i(LOGTAG, "Loading " + sPackageInfo.packageName + " version " + sPackageInfo.versionName + " (code " + sPackageInfo.getLongVersionCode() + ")");
            Trace.traceBegin(16, "WebViewFactory.getChromiumProviderClass()");
            try {
                initialApplication.getAssets().addAssetPathAsSharedLibrary(webViewContext.getApplicationInfo().sourceDir);
                ClassLoader clazzLoader = webViewContext.getClassLoader();
                Trace.traceBegin(16, "WebViewFactory.loadNativeLibrary()");
                WebViewLibraryLoader.loadNativeLibrary(clazzLoader, getWebViewLibrary(sPackageInfo.applicationInfo));
                Trace.traceEnd(16);
                Trace.traceBegin(16, "Class.forName()");
                Class<WebViewFactoryProvider> webViewProviderClass = getWebViewProviderClass(clazzLoader);
                Trace.traceEnd(16);
                Trace.traceEnd(16);
                return webViewProviderClass;
            } catch (ClassNotFoundException e) {
                Log.e(LOGTAG, "error loading provider", e);
                throw new AndroidRuntimeException((Exception) e);
            } catch (Throwable th) {
                Trace.traceEnd(16);
                throw th;
            }
        } catch (MissingWebViewPackageException e2) {
            Log.e(LOGTAG, "Chromium WebView package does not exist", e2);
            throw new AndroidRuntimeException((Exception) e2);
        } catch (Throwable th2) {
            Trace.traceEnd(16);
            throw th2;
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
        ApplicationInfo originalAppInfo = new ApplicationInfo(packageInfo.applicationInfo);
        try {
            fixupStubApplicationInfo(packageInfo.applicationInfo, AppGlobals.getInitialApplication().getPackageManager());
            startedRelroProcesses = WebViewLibraryLoader.prepareNativeLibraries(packageInfo);
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing webview native library", t);
        }
        WebViewZygote.onWebViewProviderChanged(packageInfo, originalAppInfo);
        return startedRelroProcesses;
    }

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
