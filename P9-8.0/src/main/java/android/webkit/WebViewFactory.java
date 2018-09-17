package android.webkit;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.AndroidRuntimeException;
import android.util.ArraySet;
import android.util.Log;
import android.util.LogException;
import android.webkit.IWebViewUpdateService.Stub;
import com.android.internal.telephony.RILConstants;
import com.android.server.LocalServices;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class WebViewFactory {
    private static final long CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES = 104857600;
    private static final String CHROMIUM_WEBVIEW_FACTORY = "com.android.webview.chromium.WebViewChromiumFactoryProviderForO";
    private static final String CHROMIUM_WEBVIEW_FACTORY_METHOD = "create";
    private static final String CHROMIUM_WEBVIEW_NATIVE_RELRO_32 = "/data/misc/shared_relro/libwebviewchromium32.relro";
    private static final String CHROMIUM_WEBVIEW_NATIVE_RELRO_64 = "/data/misc/shared_relro/libwebviewchromium64.relro";
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
    private static final String NULL_WEBVIEW_FACTORY = "com.android.webview.nullwebview.NullWebViewFactoryProvider";
    private static String WEBVIEW_UPDATE_SERVICE_NAME = "webviewupdate";
    private static boolean sAddressSpaceReserved = false;
    private static PackageInfo sPackageInfo;
    private static WebViewFactoryProvider sProviderInstance;
    private static final Object sProviderLock = new Object();

    public static class MissingWebViewPackageException extends AndroidRuntimeException {
        public MissingWebViewPackageException(String message) {
            super(message);
        }

        public MissingWebViewPackageException(Exception e) {
            super(e);
        }
    }

    private static class RelroFileCreator {
        private RelroFileCreator() {
        }

        /* JADX WARNING: Missing block: B:22:0x00ae, code:
            r0 = move-exception;
     */
        /* JADX WARNING: Missing block: B:23:0x00af, code:
            android.util.Log.e(android.webkit.WebViewFactory.LOGTAG, "error notifying update service", r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static void main(String[] args) {
            boolean result = false;
            boolean is64Bit = VMRuntime.getRuntime().is64Bit();
            try {
                if (args.length == 2 && args[0] != null) {
                    if (args[1] != null) {
                        Log.v(WebViewFactory.LOGTAG, "RelroFileCreator (64bit = " + is64Bit + "), " + " 32-bit lib: " + args[0] + ", 64-bit lib: " + args[1]);
                        if (WebViewFactory.sAddressSpaceReserved) {
                            result = WebViewFactory.nativeCreateRelroFile(args[0], args[1], WebViewFactory.CHROMIUM_WEBVIEW_NATIVE_RELRO_32, WebViewFactory.CHROMIUM_WEBVIEW_NATIVE_RELRO_64);
                            try {
                                WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                            } catch (RemoteException e) {
                                Log.e(WebViewFactory.LOGTAG, "error notifying update service", e);
                            }
                            if (!result) {
                                Log.e(WebViewFactory.LOGTAG, "failed to create relro file");
                            }
                            System.exit(0);
                            return;
                        }
                        Log.e(WebViewFactory.LOGTAG, "can't create relro file; address space not reserved");
                        try {
                            WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                        } catch (RemoteException e2) {
                            Log.e(WebViewFactory.LOGTAG, "error notifying update service", e2);
                        }
                        if (!result) {
                            Log.e(WebViewFactory.LOGTAG, "failed to create relro file");
                        }
                        System.exit(0);
                        return;
                    }
                }
                Log.e(WebViewFactory.LOGTAG, "Invalid RelroFileCreator args: " + Arrays.toString(args));
            } finally {
                try {
                    WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                } catch (RemoteException e22) {
                    Log.e(WebViewFactory.LOGTAG, "error notifying update service", e22);
                }
                if (!result) {
                    Log.e(WebViewFactory.LOGTAG, "failed to create relro file");
                }
                System.exit(0);
            }
        }
    }

    private static native boolean nativeCreateRelroFile(String str, String str2, String str3, String str4);

    private static native int nativeLoadWithRelroFile(String str, String str2, String str3, String str4, ClassLoader classLoader);

    private static native boolean nativeReserveAddressSpace(long j);

    private static String getWebViewPreparationErrorReason(int error) {
        switch (error) {
            case 3:
                return "Time out waiting for Relro files being created";
            case 4:
                return "No WebView installed";
            case 8:
                return "Crashed for unknown reason";
            default:
                return "Unknown";
        }
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
        try {
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            if (response.status != 0 && response.status != 3) {
                return response.status;
            }
            if (!response.packageInfo.packageName.equals(packageName)) {
                return 1;
            }
            try {
                int loadNativeRet = loadNativeLibrary(clazzLoader, AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, 268435584));
                if (loadNativeRet == 0) {
                    return response.status;
                }
                return loadNativeRet;
            } catch (NameNotFoundException e) {
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
            WebViewFactoryProvider webViewFactoryProvider;
            if (sProviderInstance != null) {
                webViewFactoryProvider = sProviderInstance;
                return webViewFactoryProvider;
            }
            int uid = Process.myUid();
            if (uid == 0 || uid == 1000 || uid == 1001 || uid == RILConstants.RIL_UNSOL_CDMA_INFO_REC || uid == 1002) {
                throw new UnsupportedOperationException("For security reasons, WebView is not allowed in privileged processes");
            }
            ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
            Trace.traceBegin(16, "WebViewFactory.getProvider()");
            try {
                Method staticFactory;
                Class providerClass = getProviderClass();
                try {
                    staticFactory = providerClass.getMethod(CHROMIUM_WEBVIEW_FACTORY_METHOD, new Class[]{WebViewDelegate.class});
                } catch (NoSuchMethodException e) {
                }
                Trace.traceBegin(16, "WebViewFactoryProvider invocation");
                try {
                    sProviderInstance = (WebViewFactoryProvider) staticFactory.invoke(null, new Object[]{new WebViewDelegate()});
                    webViewFactoryProvider = sProviderInstance;
                    Trace.traceEnd(16);
                    Trace.traceEnd(16);
                    StrictMode.setThreadPolicy(oldPolicy);
                    return webViewFactoryProvider;
                } catch (Exception e2) {
                    Log.e(LOGTAG, "error instantiating provider", e2);
                    throw new AndroidRuntimeException(e2);
                } catch (Throwable th) {
                    Trace.traceEnd(16);
                }
            } finally {
                Trace.traceEnd(16);
                StrictMode.setThreadPolicy(oldPolicy);
            }
        }
    }

    private static boolean signaturesEquals(Signature[] s1, Signature[] s2) {
        int i = 0;
        if (s1 == null) {
            boolean z;
            if (s2 == null) {
                z = true;
            }
            return z;
        } else if (s2 == null) {
            return false;
        } else {
            ArraySet<Signature> set1 = new ArraySet();
            for (Signature signature : s1) {
                set1.add(signature);
            }
            ArraySet<Signature> set2 = new ArraySet();
            int length = s2.length;
            while (i < length) {
                set2.add(s2[i]);
                i++;
            }
            return set1.equals(set2);
        }
    }

    private static void verifyPackageInfo(PackageInfo chosen, PackageInfo toUse) {
        if (!chosen.packageName.equals(toUse.packageName)) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, packageName mismatch, expected: " + chosen.packageName + " actual: " + toUse.packageName);
        } else if (chosen.versionCode > toUse.versionCode) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, version code is lower than expected: " + chosen.versionCode + " actual: " + toUse.versionCode);
        } else if (getWebViewLibrary(toUse.applicationInfo) == null) {
            throw new MissingWebViewPackageException("Tried to load an invalid WebView provider: " + toUse.packageName);
        } else if (!signaturesEquals(chosen.signatures, toUse.signatures)) {
            throw new MissingWebViewPackageException("Failed to verify WebView provider, signature mismatch");
        }
    }

    private static void fixupStubApplicationInfo(ApplicationInfo ai, PackageManager pm) {
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
            } catch (NameNotFoundException e) {
                throw new MissingWebViewPackageException("Failed to find donor package: " + donorPackageName);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0043 A:{Splitter: B:1:0x0006, ExcHandler: android.os.RemoteException (r1_0 'e' android.util.AndroidException)} */
    /* JADX WARNING: Missing block: B:13:0x0043, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x005d, code:
            throw new android.webkit.WebViewFactory.MissingWebViewPackageException("Failed to load WebView provider: " + r1);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Context getWebViewContextAndSetProvider() {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewUpdateService.waitForAndGetProvider()");
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            Trace.traceEnd(16);
            if (response.status == 0 || response.status == 3) {
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
            }
            throw new MissingWebViewPackageException("Failed to load WebView provider: " + getWebViewPreparationErrorReason(response.status));
        } catch (AndroidException e) {
        } catch (Throwable th) {
            Trace.traceEnd(16);
        }
    }

    private static Class<WebViewFactoryProvider> getProviderClass() {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewFactory.getWebViewContextAndSetProvider()");
            Context webViewContext = getWebViewContextAndSetProvider();
            Trace.traceEnd(16);
            Log.i(LOGTAG, "Loading " + sPackageInfo.packageName + " version " + sPackageInfo.versionName + " (code " + sPackageInfo.versionCode + ")");
            Trace.traceBegin(16, "WebViewFactory.getChromiumProviderClass()");
            try {
                initialApplication.getAssets().addAssetPathAsSharedLibrary(webViewContext.getApplicationInfo().sourceDir);
                ClassLoader clazzLoader = webViewContext.getClassLoader();
                Trace.traceBegin(16, "WebViewFactory.loadNativeLibrary()");
                loadNativeLibrary(clazzLoader, sPackageInfo);
                Trace.traceEnd(16);
                Trace.traceBegin(16, "Class.forName()");
                Class<WebViewFactoryProvider> webViewProviderClass = getWebViewProviderClass(clazzLoader);
                Trace.traceEnd(16);
                Trace.traceEnd(16);
                return webViewProviderClass;
            } catch (Exception e) {
                try {
                    Log.e(LOGTAG, "error loading provider", e);
                    throw new AndroidRuntimeException(e);
                } catch (Throwable th) {
                    Trace.traceEnd(16);
                }
            } catch (Throwable th2) {
                Trace.traceEnd(16);
            }
        } catch (Exception e2) {
            try {
                return Class.forName(NULL_WEBVIEW_FACTORY);
            } catch (ClassNotFoundException e3) {
                Log.e(LOGTAG, "Chromium WebView package does not exist", e2);
                throw new AndroidRuntimeException(e2);
            }
        } catch (Throwable th3) {
            Trace.traceEnd(16);
        }
    }

    public static void prepareWebViewInZygote() {
        try {
            System.loadLibrary("webviewchromium_loader");
            long addressSpaceToReserve = SystemProperties.getLong(CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY, CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES);
            sAddressSpaceReserved = nativeReserveAddressSpace(addressSpaceToReserve);
            if (!sAddressSpaceReserved) {
                Log.e(LOGTAG, "reserving " + addressSpaceToReserve + " bytes of address space failed");
            }
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing native loader", t);
        }
    }

    private static int prepareWebViewInSystemServer(String[] nativeLibraryPaths) {
        int numRelros = 0;
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            createRelroFile(false, nativeLibraryPaths);
            numRelros = 1;
        }
        if (Build.SUPPORTED_64_BIT_ABIS.length <= 0) {
            return numRelros;
        }
        createRelroFile(true, nativeLibraryPaths);
        return numRelros + 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x008c A:{Splitter: B:31:0x0086, ExcHandler: java.io.IOException (e java.io.IOException)} */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x00f9 A:{SYNTHETIC, Splitter: B:59:0x00f9} */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x010f A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00fe A:{SYNTHETIC, Splitter: B:62:0x00fe} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:36:0x008c, code:
            r2 = e;
     */
    /* JADX WARNING: Missing block: B:37:0x008d, code:
            r12 = r13;
     */
    /* JADX WARNING: Missing block: B:45:0x00e3, code:
            r15 = th;
     */
    /* JADX WARNING: Missing block: B:74:?, code:
            r6 = java.lang.Math.max(2 * r6, CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES);
            android.util.Log.d(LOGTAG, "Setting new address space to " + r6);
            android.os.SystemProperties.set(CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY, java.lang.Long.toString(r6));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int onWebViewProviderChanged(PackageInfo packageInfo) {
        Throwable th;
        String[] strArr = null;
        String originalSourceDir = packageInfo.applicationInfo.sourceDir;
        try {
            fixupStubApplicationInfo(packageInfo.applicationInfo, AppGlobals.getInitialApplication().getPackageManager());
            strArr = getWebViewNativeLibraryPaths(packageInfo);
            if (strArr != null) {
                long newVmSize = 0;
                int i = 0;
                int length = strArr.length;
                while (true) {
                    int i2 = i;
                    if (i2 >= length) {
                        break;
                    }
                    String path = strArr[i2];
                    if (!(path == null || TextUtils.isEmpty(path))) {
                        File f = new File(path);
                        if (f.exists()) {
                            newVmSize = Math.max(newVmSize, f.length());
                        } else {
                            if (path.contains("!/")) {
                                String[] split = TextUtils.split(path, "!/");
                                if (split.length == 2) {
                                    Throwable th2 = null;
                                    ZipFile z = null;
                                    try {
                                        ZipFile z2 = new ZipFile(split[0]);
                                        try {
                                            ZipEntry e = z2.getEntry(split[1]);
                                            if (e == null || e.getMethod() != 0) {
                                                if (z2 != null) {
                                                    z2.close();
                                                }
                                                if (th2 != null) {
                                                    throw th2;
                                                }
                                            } else {
                                                newVmSize = Math.max(newVmSize, e.getSize());
                                                if (z2 != null) {
                                                    try {
                                                        z2.close();
                                                    } catch (IOException e2) {
                                                    } catch (Throwable th3) {
                                                        th2 = th3;
                                                    }
                                                }
                                                if (th2 != null) {
                                                    throw th2;
                                                }
                                            }
                                        } catch (Throwable th4) {
                                            th = th4;
                                            z = z2;
                                            if (z != null) {
                                                try {
                                                    z.close();
                                                } catch (Throwable th5) {
                                                    if (th2 == null) {
                                                        th2 = th5;
                                                    } else if (th2 != th5) {
                                                        th2.addSuppressed(th5);
                                                    }
                                                }
                                            }
                                            if (th2 == null) {
                                                try {
                                                    throw th2;
                                                } catch (IOException e3) {
                                                    IOException e4 = e3;
                                                    Log.e(LOGTAG, "error reading APK file " + split[0] + ", ", e4);
                                                    Log.e(LOGTAG, "error sizing load for " + path);
                                                    i = i2 + 1;
                                                }
                                            } else {
                                                throw th;
                                            }
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        if (z != null) {
                                        }
                                        if (th2 == null) {
                                        }
                                    }
                                }
                            }
                            Log.e(LOGTAG, "error sizing load for " + path);
                        }
                    }
                    i = i2 + 1;
                }
            }
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing webview native library", t);
        }
        WebViewZygote.onWebViewProviderChanged(packageInfo, originalSourceDir);
        return prepareWebViewInSystemServer(strArr);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x0078 A:{SYNTHETIC, Splitter: B:37:0x0078} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x008b A:{Catch:{ IOException -> 0x007e }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x007d A:{SYNTHETIC, Splitter: B:40:0x007d} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String getLoadFromApkPath(String apkPath, String[] abiList, String nativeLibFileName) {
        Exception e;
        Throwable th;
        int i = 0;
        Throwable th2 = null;
        ZipFile z = null;
        try {
            ZipFile z2 = new ZipFile(apkPath);
            try {
                int length = abiList.length;
                while (i < length) {
                    String entry = "lib/" + abiList[i] + "/" + nativeLibFileName;
                    ZipEntry e2 = z2.getEntry(entry);
                    if (e2 == null || e2.getMethod() != 0) {
                        i++;
                    } else {
                        String str = apkPath + "!/" + entry;
                        if (z2 != null) {
                            try {
                                z2.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        if (th2 == null) {
                            return str;
                        }
                        try {
                            throw th2;
                        } catch (IOException e3) {
                            e = e3;
                            z = z2;
                        }
                    }
                }
                if (z2 != null) {
                    try {
                        z2.close();
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                if (th2 == null) {
                    return LogException.NO_VALUE;
                }
                throw th2;
            } catch (Throwable th5) {
                th = th5;
                z = z2;
                if (z != null) {
                    try {
                        z.close();
                    } catch (Throwable th6) {
                        if (th2 == null) {
                            th2 = th6;
                        } else if (th2 != th6) {
                            th2.addSuppressed(th6);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e4) {
                        e = e4;
                        throw new MissingWebViewPackageException(e);
                    }
                }
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            if (z != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private static String[] getWebViewNativeLibraryPaths(PackageInfo packageInfo) {
        String path64;
        String path32;
        ApplicationInfo ai = packageInfo.applicationInfo;
        String NATIVE_LIB_FILE_NAME = getWebViewLibrary(ai);
        boolean primaryArchIs64bit = VMRuntime.is64BitAbi(ai.primaryCpuAbi);
        if (TextUtils.isEmpty(ai.secondaryCpuAbi)) {
            if (primaryArchIs64bit) {
                path64 = ai.nativeLibraryDir;
                path32 = LogException.NO_VALUE;
            } else {
                path32 = ai.nativeLibraryDir;
                path64 = LogException.NO_VALUE;
            }
        } else if (primaryArchIs64bit) {
            path64 = ai.nativeLibraryDir;
            path32 = ai.secondaryNativeLibraryDir;
        } else {
            path64 = ai.secondaryNativeLibraryDir;
            path32 = ai.nativeLibraryDir;
        }
        if (!TextUtils.isEmpty(path32)) {
            path32 = path32 + "/" + NATIVE_LIB_FILE_NAME;
            if (!new File(path32).exists()) {
                path32 = getLoadFromApkPath(ai.sourceDir, Build.SUPPORTED_32_BIT_ABIS, NATIVE_LIB_FILE_NAME);
            }
        }
        if (!TextUtils.isEmpty(path64)) {
            path64 = path64 + "/" + NATIVE_LIB_FILE_NAME;
            if (!new File(path64).exists()) {
                path64 = getLoadFromApkPath(ai.sourceDir, Build.SUPPORTED_64_BIT_ABIS, NATIVE_LIB_FILE_NAME);
            }
        }
        return new String[]{path32, path64};
    }

    private static void createRelroFile(boolean is64Bit, String[] nativeLibraryPaths) {
        final String abi = is64Bit ? Build.SUPPORTED_64_BIT_ABIS[0] : Build.SUPPORTED_32_BIT_ABIS[0];
        Runnable crashHandler = new Runnable() {
            public void run() {
                try {
                    Log.e(WebViewFactory.LOGTAG, "relro file creator for " + abi + " crashed. Proceeding without");
                    WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                } catch (RemoteException e) {
                    Log.e(WebViewFactory.LOGTAG, "Cannot reach WebViewUpdateService. " + e.getMessage());
                }
            }
        };
        if (nativeLibraryPaths != null) {
            try {
                if (nativeLibraryPaths[0] != null) {
                    if (nativeLibraryPaths[1] != null) {
                        if (((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).startIsolatedProcess(RelroFileCreator.class.getName(), nativeLibraryPaths, "WebViewLoader-" + abi, abi, RILConstants.RIL_UNSOL_RESPONSE_IMS_NETWORK_STATE_CHANGED, crashHandler) <= 0) {
                            throw new Exception("Failed to start the relro file creator process");
                        }
                        return;
                    }
                }
            } catch (Throwable t) {
                Log.e(LOGTAG, "error starting relro file creator for abi " + abi, t);
                crashHandler.run();
                return;
            }
        }
        throw new IllegalArgumentException("Native library paths to the WebView RelRo process must not be null!");
    }

    private static int loadNativeLibrary(ClassLoader clazzLoader, PackageInfo packageInfo) {
        if (sAddressSpaceReserved) {
            String[] args = getWebViewNativeLibraryPaths(packageInfo);
            int result = nativeLoadWithRelroFile(args[0], args[1], CHROMIUM_WEBVIEW_NATIVE_RELRO_32, CHROMIUM_WEBVIEW_NATIVE_RELRO_64, clazzLoader);
            if (result != 0) {
                Log.w(LOGTAG, "failed to load with relro file, proceeding without");
            }
            return result;
        }
        Log.e(LOGTAG, "can't load with relro file; address space not reserved");
        return 2;
    }

    public static IWebViewUpdateService getUpdateService() {
        return Stub.asInterface(ServiceManager.getService(WEBVIEW_UPDATE_SERVICE_NAME));
    }
}
