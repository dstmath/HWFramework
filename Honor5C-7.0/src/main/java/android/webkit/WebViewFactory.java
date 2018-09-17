package android.webkit;

import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
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
import android.webkit.IWebViewUpdateService.Stub;
import com.android.internal.telephony.RILConstants;
import com.android.server.LocalServices;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class WebViewFactory {
    private static final long CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES = 104857600;
    private static final String CHROMIUM_WEBVIEW_FACTORY = "com.android.webview.chromium.WebViewChromiumFactoryProvider";
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
    private static String WEBVIEW_UPDATE_SERVICE_NAME;
    private static boolean sAddressSpaceReserved;
    private static PackageInfo sPackageInfo;
    private static WebViewFactoryProvider sProviderInstance;
    private static final Object sProviderLock = null;

    /* renamed from: android.webkit.WebViewFactory.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ String val$abi;

        AnonymousClass1(String val$abi) {
            this.val$abi = val$abi;
        }

        public void run() {
            try {
                Log.e(WebViewFactory.LOGTAG, "relro file creator for " + this.val$abi + " crashed. Proceeding without");
                WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
            } catch (RemoteException e) {
                Log.e(WebViewFactory.LOGTAG, "Cannot reach WebViewUpdateService. " + e.getMessage());
            }
        }
    }

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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public static void main(String[] args) {
            boolean result = WebViewFactory.DEBUG;
            boolean is64Bit = VMRuntime.getRuntime().is64Bit();
            try {
                if (args.length == WebViewFactory.LIBLOAD_ADDRESS_SPACE_NOT_RESERVED && args[WebViewFactory.LIBLOAD_SUCCESS] != null) {
                    if (args[WebViewFactory.LIBLOAD_WRONG_PACKAGE_NAME] != null) {
                        Log.v(WebViewFactory.LOGTAG, "RelroFileCreator (64bit = " + is64Bit + "), " + " 32-bit lib: " + args[WebViewFactory.LIBLOAD_SUCCESS] + ", 64-bit lib: " + args[WebViewFactory.LIBLOAD_WRONG_PACKAGE_NAME]);
                        if (WebViewFactory.sAddressSpaceReserved) {
                            result = WebViewFactory.nativeCreateRelroFile(args[WebViewFactory.LIBLOAD_SUCCESS], args[WebViewFactory.LIBLOAD_WRONG_PACKAGE_NAME], WebViewFactory.CHROMIUM_WEBVIEW_NATIVE_RELRO_32, WebViewFactory.CHROMIUM_WEBVIEW_NATIVE_RELRO_64);
                            if (result) {
                                try {
                                } catch (RemoteException e) {
                                    Log.e(WebViewFactory.LOGTAG, "error notifying update service", e);
                                }
                            }
                            WebViewFactory.getUpdateService().notifyRelroCreationCompleted();
                            if (!result) {
                                Log.e(WebViewFactory.LOGTAG, "failed to create relro file");
                            }
                            System.exit(WebViewFactory.LIBLOAD_SUCCESS);
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
                        System.exit(WebViewFactory.LIBLOAD_SUCCESS);
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
                System.exit(WebViewFactory.LIBLOAD_SUCCESS);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.webkit.WebViewFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.webkit.WebViewFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.WebViewFactory.<clinit>():void");
    }

    private static native boolean nativeCreateRelroFile(String str, String str2, String str3, String str4);

    private static native int nativeLoadWithRelroFile(String str, String str2, String str3, String str4, ClassLoader classLoader);

    private static native boolean nativeReserveAddressSpace(long j);

    private static String getWebViewPreparationErrorReason(int error) {
        switch (error) {
            case LIBLOAD_FAILED_WAITING_FOR_RELRO /*3*/:
                return "Time out waiting for Relro files being created";
            case LIBLOAD_FAILED_LISTING_WEBVIEW_PACKAGES /*4*/:
                return "No WebView installed";
            case LIBLOAD_FAILED_WAITING_FOR_WEBVIEW_REASON_UNKNOWN /*8*/:
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
        return sPackageInfo;
    }

    public static int loadWebViewNativeLibraryFromPackage(String packageName, ClassLoader clazzLoader) {
        try {
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            if (response.status != 0 && response.status != LIBLOAD_FAILED_WAITING_FOR_RELRO) {
                return response.status;
            }
            if (!response.packageInfo.packageName.equals(packageName)) {
                return LIBLOAD_WRONG_PACKAGE_NAME;
            }
            try {
                sPackageInfo = AppGlobals.getInitialApplication().getPackageManager().getPackageInfo(packageName, 268435584);
                int loadNativeRet = loadNativeLibrary(clazzLoader);
                if (loadNativeRet == 0) {
                    return response.status;
                }
                return loadNativeRet;
            } catch (NameNotFoundException e) {
                Log.e(LOGTAG, "Couldn't find package " + packageName);
                return LIBLOAD_WRONG_PACKAGE_NAME;
            }
        } catch (RemoteException e2) {
            Log.e(LOGTAG, "error waiting for relro creation", e2);
            return LIBLOAD_FAILED_WAITING_FOR_WEBVIEW_REASON_UNKNOWN;
        }
    }

    static WebViewFactoryProvider getProvider() {
        synchronized (sProviderLock) {
            if (sProviderInstance != null) {
                WebViewFactoryProvider webViewFactoryProvider = sProviderInstance;
                return webViewFactoryProvider;
            }
            int uid = Process.myUid();
            if (uid == 0 || uid == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) {
                throw new UnsupportedOperationException("For security reasons, WebView is not allowed in privileged processes");
            }
            ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
            Trace.traceBegin(16, "WebViewFactory.getProvider()");
            try {
                Class<WebViewFactoryProvider> providerClass = getProviderClass();
                int i = "providerClass.newInstance()";
                Trace.traceBegin(16, i);
                try {
                    Class[] clsArr = new Class[i];
                    clsArr[LIBLOAD_SUCCESS] = WebViewDelegate.class;
                    Constructor constructor = providerClass.getConstructor(clsArr);
                    Object[] objArr = new Object[LIBLOAD_WRONG_PACKAGE_NAME];
                    objArr[LIBLOAD_SUCCESS] = new WebViewDelegate();
                    sProviderInstance = (WebViewFactoryProvider) constructor.newInstance(objArr);
                    webViewFactoryProvider = sProviderInstance;
                    Trace.traceEnd(16);
                    Trace.traceEnd(16);
                    StrictMode.setThreadPolicy(oldPolicy);
                    return webViewFactoryProvider;
                } catch (Exception e) {
                    Log.e(LOGTAG, "error instantiating provider", e);
                    throw new AndroidRuntimeException(e);
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
        int i = LIBLOAD_SUCCESS;
        if (s1 == null) {
            boolean z;
            if (s2 == null) {
                z = true;
            }
            return z;
        } else if (s2 == null) {
            return DEBUG;
        } else {
            int i2;
            ArraySet<Signature> set1 = new ArraySet();
            int length = s1.length;
            for (i2 = LIBLOAD_SUCCESS; i2 < length; i2 += LIBLOAD_WRONG_PACKAGE_NAME) {
                set1.add(s1[i2]);
            }
            ArraySet<Signature> set2 = new ArraySet();
            i2 = s2.length;
            while (i < i2) {
                set2.add(s2[i]);
                i += LIBLOAD_WRONG_PACKAGE_NAME;
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

    private static Context getWebViewContextAndSetProvider() {
        Application initialApplication = AppGlobals.getInitialApplication();
        try {
            Trace.traceBegin(16, "WebViewUpdateService.waitForAndGetProvider()");
            WebViewProviderResponse response = getUpdateService().waitForAndGetProvider();
            Trace.traceEnd(16);
            if (response.status == 0 || response.status == LIBLOAD_FAILED_WAITING_FOR_RELRO) {
                Trace.traceBegin(16, "ActivityManager.addPackageDependency()");
                ActivityManagerNative.getDefault().addPackageDependency(response.packageInfo.packageName);
                Trace.traceEnd(16);
                Trace.traceBegin(16, "PackageManager.getPackageInfo()");
                PackageInfo newPackageInfo = initialApplication.getPackageManager().getPackageInfo(response.packageInfo.packageName, 268444864);
                Trace.traceEnd(16);
                verifyPackageInfo(response.packageInfo, newPackageInfo);
                Trace.traceBegin(16, "initialApplication.createApplicationContext");
                Context webViewContext = initialApplication.createApplicationContext(newPackageInfo.applicationInfo, LIBLOAD_FAILED_WAITING_FOR_RELRO);
                sPackageInfo = newPackageInfo;
                Trace.traceEnd(16);
                return webViewContext;
            }
            throw new MissingWebViewPackageException("Failed to load WebView provider: " + getWebViewPreparationErrorReason(response.status));
        } catch (AndroidException e) {
            throw new MissingWebViewPackageException("Failed to load WebView provider: " + e);
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
                loadNativeLibrary(clazzLoader);
                Trace.traceEnd(16);
                Trace.traceBegin(16, "Class.forName()");
                Class<WebViewFactoryProvider> cls = Class.forName(CHROMIUM_WEBVIEW_FACTORY, true, clazzLoader);
                Trace.traceEnd(16);
                Trace.traceEnd(16);
                return cls;
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
        int numRelros = LIBLOAD_SUCCESS;
        if (Build.SUPPORTED_32_BIT_ABIS.length > 0) {
            createRelroFile(DEBUG, nativeLibraryPaths);
            numRelros = LIBLOAD_WRONG_PACKAGE_NAME;
        }
        if (Build.SUPPORTED_64_BIT_ABIS.length <= 0) {
            return numRelros;
        }
        createRelroFile(true, nativeLibraryPaths);
        return numRelros + LIBLOAD_WRONG_PACKAGE_NAME;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int onWebViewProviderChanged(PackageInfo packageInfo) {
        IOException e;
        Throwable th;
        String[] strArr = null;
        try {
            strArr = getWebViewNativeLibraryPaths(packageInfo);
            if (strArr != null) {
                long newVmSize = 0;
                int length = strArr.length;
                for (int i = LIBLOAD_SUCCESS; i < length; i += LIBLOAD_WRONG_PACKAGE_NAME) {
                    String path = strArr[i];
                    if (!(path == null || TextUtils.isEmpty(path))) {
                        File f = new File(path);
                        if (f.exists()) {
                            newVmSize = Math.max(newVmSize, f.length());
                        } else {
                            if (path.contains("!/")) {
                                String[] split = TextUtils.split(path, "!/");
                                if (split.length == LIBLOAD_ADDRESS_SPACE_NOT_RESERVED) {
                                    Throwable th2 = null;
                                    ZipFile zipFile = null;
                                    try {
                                        ZipFile z = new ZipFile(split[LIBLOAD_SUCCESS]);
                                        try {
                                            ZipEntry e2 = z.getEntry(split[LIBLOAD_WRONG_PACKAGE_NAME]);
                                            if (e2 == null || e2.getMethod() != 0) {
                                                if (z != null) {
                                                    z.close();
                                                }
                                                if (th2 != null) {
                                                    throw th2;
                                                }
                                            } else {
                                                newVmSize = Math.max(newVmSize, e2.getSize());
                                                if (z != null) {
                                                    try {
                                                        z.close();
                                                    } catch (IOException e3) {
                                                        e = e3;
                                                        zipFile = z;
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
                                            zipFile = z;
                                            if (zipFile != null) {
                                                try {
                                                    zipFile.close();
                                                } catch (Throwable th5) {
                                                    if (th2 == null) {
                                                        th2 = th5;
                                                    } else if (th2 != th5) {
                                                        th2.addSuppressed(th5);
                                                    }
                                                }
                                            }
                                            if (th2 == null) {
                                                throw th;
                                            }
                                            try {
                                                throw th2;
                                            } catch (IOException e4) {
                                                e = e4;
                                            }
                                        }
                                    } catch (Throwable th6) {
                                        th = th6;
                                        if (zipFile != null) {
                                            zipFile.close();
                                        }
                                        if (th2 == null) {
                                            throw th2;
                                        }
                                        throw th;
                                    }
                                }
                            }
                            Log.e(LOGTAG, "error sizing load for " + path);
                        }
                    }
                }
                newVmSize = Math.max(2 * newVmSize, CHROMIUM_WEBVIEW_DEFAULT_VMSIZE_BYTES);
                Log.d(LOGTAG, "Setting new address space to " + newVmSize);
                SystemProperties.set(CHROMIUM_WEBVIEW_VMSIZE_SIZE_PROPERTY, Long.toString(newVmSize));
            }
        } catch (Throwable t) {
            Log.e(LOGTAG, "error preparing webview native library", t);
        }
        return prepareWebViewInSystemServer(strArr);
    }

    private static String getLoadFromApkPath(String apkPath, String[] abiList, String nativeLibFileName) {
        Exception e;
        Throwable th;
        int i = LIBLOAD_SUCCESS;
        Throwable th2 = null;
        ZipFile zipFile = null;
        try {
            ZipFile z = new ZipFile(apkPath);
            try {
                int length = abiList.length;
                while (i < length) {
                    String entry = "lib/" + abiList[i] + "/" + nativeLibFileName;
                    ZipEntry e2 = z.getEntry(entry);
                    if (e2 == null || e2.getMethod() != 0) {
                        i += LIBLOAD_WRONG_PACKAGE_NAME;
                    } else {
                        String str = apkPath + "!/" + entry;
                        if (z != null) {
                            try {
                                z.close();
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
                            zipFile = z;
                        }
                    }
                }
                if (z != null) {
                    try {
                        z.close();
                    } catch (Throwable th4) {
                        th2 = th4;
                    }
                }
                if (th2 == null) {
                    return "";
                }
                throw th2;
            } catch (Throwable th5) {
                th = th5;
                zipFile = z;
                if (zipFile != null) {
                    try {
                        zipFile.close();
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
            if (zipFile != null) {
                zipFile.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
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
                path32 = "";
            } else {
                path32 = ai.nativeLibraryDir;
                path64 = "";
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
        String[] strArr = new String[LIBLOAD_ADDRESS_SPACE_NOT_RESERVED];
        strArr[LIBLOAD_SUCCESS] = path32;
        strArr[LIBLOAD_WRONG_PACKAGE_NAME] = path64;
        return strArr;
    }

    private static void createRelroFile(boolean is64Bit, String[] nativeLibraryPaths) {
        String abi = is64Bit ? Build.SUPPORTED_64_BIT_ABIS[LIBLOAD_SUCCESS] : Build.SUPPORTED_32_BIT_ABIS[LIBLOAD_SUCCESS];
        Runnable crashHandler = new AnonymousClass1(abi);
        if (nativeLibraryPaths != null) {
            try {
                if (nativeLibraryPaths[LIBLOAD_SUCCESS] != null) {
                    if (nativeLibraryPaths[LIBLOAD_WRONG_PACKAGE_NAME] != null) {
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

    private static int loadNativeLibrary(ClassLoader clazzLoader) {
        if (sAddressSpaceReserved) {
            String[] args = getWebViewNativeLibraryPaths(sPackageInfo);
            int result = nativeLoadWithRelroFile(args[LIBLOAD_SUCCESS], args[LIBLOAD_WRONG_PACKAGE_NAME], CHROMIUM_WEBVIEW_NATIVE_RELRO_32, CHROMIUM_WEBVIEW_NATIVE_RELRO_64, clazzLoader);
            if (result != 0) {
                Log.w(LOGTAG, "failed to load with relro file, proceeding without");
            }
            return result;
        }
        Log.e(LOGTAG, "can't load with relro file; address space not reserved");
        return LIBLOAD_ADDRESS_SPACE_NOT_RESERVED;
    }

    public static IWebViewUpdateService getUpdateService() {
        return Stub.asInterface(ServiceManager.getService(WEBVIEW_UPDATE_SERVICE_NAME));
    }
}
