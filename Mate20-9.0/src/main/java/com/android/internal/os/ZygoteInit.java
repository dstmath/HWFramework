package com.android.internal.os;

import android.common.HwFrameworkFactory;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.impl.CacheValue;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.ULocale;
import android.opengl.EGL14;
import android.os.Build;
import android.os.Environment;
import android.os.IInstalld;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.ServiceSpecificException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.ZygoteProcess;
import android.os.storage.StorageManager;
import android.security.keystore.AndroidKeyStoreProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructCapUserData;
import android.system.StructCapUserHeader;
import android.text.Hyphenator;
import android.util.EventLog;
import android.util.Jlog;
import android.util.Log;
import android.util.TimingsTraceLog;
import android.webkit.WebViewFactory;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RuntimeInit;
import com.android.internal.os.ZygoteConnection;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.util.Preconditions;
import com.huawei.featurelayer.HwFeatureLoader;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import dalvik.system.ZygoteHooks;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Provider;
import java.security.Security;
import libcore.io.IoUtils;

public class ZygoteInit {
    private static final String ABI_LIST_ARG = "--abi-list=";
    private static final int BOOT_PRELOAD_CLASS_RAM = 307200;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_END = 3030;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_START = 3020;
    private static final String PRELOADED_CLASSES = "/system/etc/preloaded-classes";
    private static final int PRELOAD_GC_THRESHOLD = 67108864;
    public static final boolean PRELOAD_RESOURCES = true;
    private static final String PROPERTY_DISABLE_OPENGL_PRELOADING = "ro.zygote.disable_gl_preload";
    private static final String PROPERTY_GFX_DRIVER = "ro.gfx.driver.0";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String SOCKET_NAME_ARG = "--socket-name=";
    private static final String TAG = "Zygote";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;
    private static boolean isPrimaryCpuAbi = false;
    private static Resources mResources;
    private static boolean sIsMygote;
    private static boolean sPreloadComplete;

    private static native void nativePreloadAppProcessHALs();

    private static final native void nativeZygoteInit();

    static {
        boolean z = false;
        if (System.getenv("MAPLE_RUNTIME") != null) {
            z = true;
        }
        sIsMygote = z;
    }

    static void preload(TimingsTraceLog bootTimingsTraceLog) {
        Log.d(TAG, "begin preload");
        bootTimingsTraceLog.traceBegin("BeginIcuCachePinning");
        beginIcuCachePinning();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("PreloadClasses");
        preloadClasses();
        HwFeatureLoader.SystemFeature.loadFeatureFramework(null);
        HwFeatureLoader.SystemFeature.preloadClasses();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("PreloadResources");
        preloadResources();
        bootTimingsTraceLog.traceEnd();
        Trace.traceBegin(16384, "PreloadAppProcessHALs");
        nativePreloadAppProcessHALs();
        Trace.traceEnd(16384);
        Trace.traceBegin(16384, "PreloadOpenGL");
        preloadOpenGL();
        Trace.traceEnd(16384);
        preloadSharedLibraries();
        preloadTextResources();
        preloadHwThemeZipsAndSomeIcons(0);
        WebViewFactory.prepareWebViewInZygote();
        endIcuCachePinning();
        warmUpJcaProviders();
        Log.d(TAG, "end preload");
        sPreloadComplete = true;
    }

    public static void lazyPreload() {
        Preconditions.checkState(!sPreloadComplete);
        Log.i(TAG, "Lazily preloading resources.");
        preload(new TimingsTraceLog("ZygoteInitTiming_lazy", 16384));
    }

    private static void beginIcuCachePinning() {
        Log.i(TAG, "Installing ICU cache reference pinning...");
        CacheValue.setStrength(CacheValue.Strength.STRONG);
        Log.i(TAG, "Preloading ICU data...");
        for (ULocale uLocale : new ULocale[]{ULocale.ROOT, ULocale.US, ULocale.getDefault()}) {
            new DecimalFormatSymbols(uLocale);
        }
    }

    private static void endIcuCachePinning() {
        CacheValue.setStrength(CacheValue.Strength.SOFT);
        Log.i(TAG, "Uninstalled ICU cache reference pinning...");
    }

    private static void preloadSharedLibraries() {
        Log.i(TAG, "Preloading shared libraries...");
        System.loadLibrary("android");
        System.loadLibrary("compiler_rt");
        System.loadLibrary("jnigraphics");
    }

    private static void preloadOpenGL() {
        String driverPackageName = SystemProperties.get(PROPERTY_GFX_DRIVER);
        if (SystemProperties.getBoolean(PROPERTY_DISABLE_OPENGL_PRELOADING, false)) {
            return;
        }
        if (driverPackageName == null || driverPackageName.isEmpty()) {
            EGL14.eglGetDisplay(0);
        }
    }

    private static void preloadTextResources() {
        Hyphenator.init();
        TextView.preloadFontCache();
    }

    private static void warmUpJcaProviders() {
        long startTime = SystemClock.uptimeMillis();
        Trace.traceBegin(16384, "Starting installation of AndroidKeyStoreProvider");
        AndroidKeyStoreProvider.install();
        Log.i(TAG, "Installed AndroidKeyStoreProvider in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        Trace.traceEnd(16384);
        long startTime2 = SystemClock.uptimeMillis();
        Trace.traceBegin(16384, "Starting warm up of JCA providers");
        for (Provider p : Security.getProviders()) {
            p.warmUpServiceProvision();
        }
        Log.i(TAG, "Warmed up JCA providers in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
        Trace.traceEnd(16384);
    }

    private static void preloadClasses() {
        String str;
        String str2;
        int count;
        String line;
        VMRuntime runtime = VMRuntime.getRuntime();
        try {
            InputStream is = new FileInputStream(PRELOADED_CLASSES);
            Log.i(TAG, "Preloading classes...");
            long startTime = SystemClock.uptimeMillis();
            int reuid = Os.getuid();
            int regid = Os.getgid();
            boolean droppedPriviliges = false;
            if (reuid == 0 && regid == 0) {
                try {
                    Os.setregid(0, 9999);
                    Os.setreuid(0, 9999);
                    droppedPriviliges = true;
                } catch (ErrnoException ex) {
                    throw new RuntimeException("Failed to drop root", ex);
                }
            }
            float defaultUtilization = runtime.getTargetHeapUtilization();
            runtime.setTargetHeapUtilization(0.8f);
            long j = 16384;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is), BOOT_PRELOAD_CLASS_RAM);
                int count2 = 0;
                while (true) {
                    count = count2;
                    String readLine = br.readLine();
                    String line2 = readLine;
                    if (readLine == null) {
                        break;
                    }
                    line = line2.trim();
                    if (!line.startsWith("#")) {
                        if (!line.equals("")) {
                            Trace.traceBegin(j, line);
                            Class.forName(line, true, null);
                            count++;
                            count2 = count;
                            Trace.traceEnd(16384);
                            j = 16384;
                        }
                    }
                    count2 = count;
                    j = 16384;
                }
                Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization);
                Trace.traceBegin(16384, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(16384);
                if (droppedPriviliges) {
                    try {
                        Os.setreuid(0, 0);
                        Os.setregid(0, 0);
                    } catch (ErrnoException ex2) {
                        throw new RuntimeException("Failed to restore root", ex2);
                    }
                }
            } catch (ClassNotFoundException e) {
                ClassNotFoundException classNotFoundException = e;
                Log.w(TAG, "Class not found for preloading: " + line);
            } catch (UnsatisfiedLinkError e2) {
                UnsatisfiedLinkError unsatisfiedLinkError = e2;
                Log.w(TAG, "Problem preloading " + line + ": " + e2);
            } catch (IOException e3) {
                try {
                    Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e3);
                    if (droppedPriviliges) {
                        try {
                        } catch (ErrnoException ex3) {
                            throw new RuntimeException(str2, ex3);
                        }
                    }
                } finally {
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    str = "PreloadDexCaches";
                    long j2 = 16384;
                    Trace.traceBegin(j2, str);
                    runtime.preloadDexCaches();
                    Trace.traceEnd(j2);
                    if (droppedPriviliges) {
                        int i = 0;
                        try {
                            Os.setreuid(i, i);
                            Os.setregid(i, i);
                        } catch (ErrnoException ex32) {
                            str2 = "Failed to restore root";
                            throw new RuntimeException(str2, ex32);
                        }
                    }
                }
            } catch (Throwable t) {
                Throwable th = t;
                Log.e(TAG, "Error preloading " + line + ".", t);
                if (t instanceof Error) {
                    throw ((Error) t);
                } else if (t instanceof RuntimeException) {
                    throw ((RuntimeException) t);
                } else {
                    throw new RuntimeException(t);
                }
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "Couldn't find /system/etc/preloaded-classes.");
        }
    }

    private static void preloadResources() {
        VMRuntime runtime = VMRuntime.getRuntime();
        try {
            mResources = Resources.getSystem(true);
            mResources.startPreloading();
            Log.i(TAG, "Preloading resources...");
            long startTime = SystemClock.uptimeMillis();
            TypedArray ar = mResources.obtainTypedArray(17236068);
            int N = preloadDrawables(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            long startTime2 = SystemClock.uptimeMillis();
            TypedArray ar2 = mResources.obtainTypedArray(17236067);
            ar2.recycle();
            Log.i(TAG, "...preloaded " + preloadColorStateLists(ar2) + " resources in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
            if (mResources.getBoolean(17956977)) {
                long startTime3 = SystemClock.uptimeMillis();
                TypedArray ar3 = mResources.obtainTypedArray(17236069);
                ar3.recycle();
                Log.i(TAG, "...preloaded " + preloadDrawables(ar3) + " resource in " + (SystemClock.uptimeMillis() - startTime3) + "ms.");
            }
            if (isPrimaryCpuAbi) {
                long startTime4 = SystemClock.uptimeMillis();
                TypedArray ar4 = mResources.obtainTypedArray(33816576);
                ar4.recycle();
                Log.w(TAG, "...preloaded " + preloadDrawables(ar4) + " hwextdrawable resources in " + (SystemClock.uptimeMillis() - startTime4) + "ms.");
            }
            mResources.finishPreloading();
        } catch (RuntimeException e) {
            Log.w(TAG, "Failure preloading resources", e);
        }
    }

    private static int preloadColorStateLists(TypedArray ar) {
        int N = ar.length();
        int i = 0;
        while (i < N) {
            int id = ar.getResourceId(i, 0);
            if (id == 0 || mResources.getColorStateList(id, null) != null) {
                i++;
            } else {
                throw new IllegalArgumentException("Unable to find preloaded color resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    private static int preloadDrawables(TypedArray ar) {
        int N = ar.length();
        int i = 0;
        while (i < N) {
            int id = ar.getResourceId(i, 0);
            if (id == 0 || mResources.getDrawable(id, null) != null) {
                i++;
            } else {
                throw new IllegalArgumentException("Unable to find preloaded drawable resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    public static void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
        if (mResources == null) {
            mResources = Resources.getSystem(true);
        }
        Log.i(TAG, "preloadHwThemeZipsAndSomeIcons");
        mResources.getImpl().getHwResourcesImpl().preloadHwThemeZipsAndSomeIcons(currentUserId);
    }

    public static void clearHwThemeZipsAndSomeIcons() {
        if (mResources == null) {
            mResources = Resources.getSystem(true);
        }
        Log.i(TAG, "clearHwThemeZipsAndSomeIcons");
        mResources.getImpl().getHwResourcesImpl().clearHwThemeZipsAndSomeIcons();
    }

    static void gcAndFinalize() {
        VMRuntime runtime = VMRuntime.getRuntime();
        System.gc();
        runtime.runFinalizationSync();
        System.gc();
    }

    private static Runnable handleSystemServerProcess(ZygoteConnection.Arguments parsedArgs) {
        ExitCatch.enable(Process.myPid(), 7);
        Os.umask(OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        if (parsedArgs.niceName != null) {
            Process.setArgV0(parsedArgs.niceName);
        }
        String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
        if (systemServerClasspath != null) {
            performSystemServerDexOpt(systemServerClasspath);
            if (SystemProperties.getBoolean("dalvik.vm.profilesystemserver", false) && (Build.IS_USERDEBUG || Build.IS_ENG)) {
                try {
                    prepareSystemServerProfile(systemServerClasspath);
                } catch (Exception e) {
                    Log.wtf(TAG, "Failed to set up system server profile", e);
                }
            }
        }
        if (parsedArgs.invokeWith != null) {
            String[] args = parsedArgs.remainingArgs;
            if (systemServerClasspath != null) {
                String[] amendedArgs = new String[(args.length + 2)];
                amendedArgs[0] = "-cp";
                amendedArgs[1] = systemServerClasspath;
                System.arraycopy(args, 0, amendedArgs, 2, args.length);
                args = amendedArgs;
            }
            WrapperInit.execApplication(parsedArgs.invokeWith, parsedArgs.niceName, parsedArgs.targetSdkVersion, VMRuntime.getCurrentInstructionSet(), null, args);
            throw new IllegalStateException("Unexpected return from WrapperInit.execApplication");
        }
        ClassLoader cl = null;
        if (systemServerClasspath != null) {
            if (sIsMygote) {
                VMRuntime.getRuntime();
                VMRuntime.preSystemServerClLoad();
            }
            cl = createPathClassLoader(systemServerClasspath, parsedArgs.targetSdkVersion);
            Thread.currentThread().setContextClassLoader(cl);
            if (sIsMygote) {
                VMRuntime.getRuntime();
                VMRuntime.notifyClassLoaderConstructed(cl);
            }
        }
        return zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, cl);
    }

    private static void prepareSystemServerProfile(String systemServerClasspath) throws RemoteException {
        if (!systemServerClasspath.isEmpty()) {
            String[] codePaths = systemServerClasspath.split(":");
            IInstalld.Stub.asInterface(ServiceManager.getService("installd")).prepareAppProfile("android", 0, UserHandle.getAppId(1000), "primary.prof", codePaths[0], null);
            VMRuntime.registerAppInfo(new File(Environment.getDataProfilesDePackageDirectory(0, "android"), "primary.prof").getAbsolutePath(), codePaths);
        }
    }

    public static void setApiBlacklistExemptions(String[] exemptions) {
        VMRuntime.getRuntime().setHiddenApiExemptions(exemptions);
    }

    public static void setHiddenApiAccessLogSampleRate(int percent) {
        VMRuntime.getRuntime().setHiddenApiAccessLogSamplingRate(percent);
    }

    static ClassLoader createPathClassLoader(String classPath, int targetSdkVersion) {
        String libraryPath = System.getProperty("java.library.path");
        return ClassLoaderFactory.createClassLoader(classPath, libraryPath, libraryPath, ClassLoader.getSystemClassLoader(), targetSdkVersion, true, null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x002b, code lost:
        if (r4 == null) goto L_0x002d;
     */
    private static void performSystemServerDexOpt(String classPath) {
        IInstalld installd;
        String instructionSet;
        HwZygoteInit hwZygoteInit;
        int i;
        int i2;
        int index;
        String classPathElement;
        int index2;
        String classPathForElement;
        String classPathElement2;
        String[] classPathElements = classPath.split(":");
        IInstalld installd2 = IInstalld.Stub.asInterface(ServiceManager.getService("installd"));
        String instructionSet2 = VMRuntime.getRuntime().vmInstructionSet();
        int[] retDexOpt = null;
        HwZygoteInit hwZygoteInit2 = HwFrameworkFactory.getHwZygoteInit();
        if (sIsMygote) {
            if (hwZygoteInit2 != null) {
                int[] dexOptNeededForMapleSystemServer = hwZygoteInit2.getDexOptNeededForMapleSystemServer(installd2, classPathElements, instructionSet2);
                retDexOpt = dexOptNeededForMapleSystemServer;
            }
            return;
        }
        int[] retDexOpt2 = retDexOpt;
        int length = classPathElements.length;
        int index3 = 0;
        String classPathForElement2 = "";
        int i3 = 0;
        while (i3 < length) {
            String classPathElement3 = classPathElements[i3];
            String systemServerFilter = SystemProperties.get("dalvik.vm.systemservercompilerfilter", "speed");
            if (sIsMygote) {
                index = index3 + 1;
                index2 = retDexOpt2[index3];
                i2 = i3;
                classPathElement = classPathElement3;
            } else {
                i2 = i3;
                classPathElement = classPathElement3;
                try {
                    index2 = DexFile.getDexOptNeeded(classPathElement3, instructionSet2, systemServerFilter, null, false, false);
                    index = index3;
                } catch (FileNotFoundException e) {
                    i = length;
                    hwZygoteInit = hwZygoteInit2;
                    instructionSet = instructionSet2;
                    installd = installd2;
                    FileNotFoundException fileNotFoundException = e;
                    Log.w(TAG, "Missing classpath element for system server: " + classPathElement);
                    classPathForElement2 = classPathForElement2;
                } catch (IOException e2) {
                    IOException iOException = e2;
                    Log.w(TAG, "Error checking classpath element for system server: " + classPathElement, e2);
                    index = index3;
                    index2 = 0;
                }
            }
            int dexoptNeeded = index2;
            if (dexoptNeeded != 0) {
                classPathForElement = classPathForElement2;
                String classPathElement4 = classPathElement;
                i = length;
                hwZygoteInit = hwZygoteInit2;
                instructionSet = instructionSet2;
                installd = installd2;
                try {
                    installd2.dexopt(classPathElement, 1000, PhoneConstants.APN_TYPE_ALL, instructionSet2, dexoptNeeded, null, 0, systemServerFilter, StorageManager.UUID_PRIVATE_INTERNAL, getSystemServerClassLoaderContext(classPathForElement2), null, false, 0, null, null, "server-dexopt");
                    classPathElement2 = classPathElement4;
                } catch (RemoteException | ServiceSpecificException e3) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Failed compiling classpath element for system server: ");
                    classPathElement2 = classPathElement4;
                    sb.append(classPathElement2);
                    Log.w(TAG, sb.toString(), e3);
                }
            } else {
                classPathForElement = classPathForElement2;
                classPathElement2 = classPathElement;
                i = length;
                hwZygoteInit = hwZygoteInit2;
                instructionSet = instructionSet2;
                installd = installd2;
            }
            classPathForElement2 = encodeSystemServerClassPath(classPathForElement, classPathElement2);
            index3 = index;
            i3 = i2 + 1;
            length = i;
            hwZygoteInit2 = hwZygoteInit;
            instructionSet2 = instructionSet;
            installd2 = installd;
        }
        String str = classPathForElement2;
        HwZygoteInit hwZygoteInit3 = hwZygoteInit2;
        String str2 = instructionSet2;
        IInstalld iInstalld = installd2;
    }

    private static String getSystemServerClassLoaderContext(String classPath) {
        if (classPath == null) {
            return "PCL[]";
        }
        return "PCL[" + classPath + "]";
    }

    private static String encodeSystemServerClassPath(String classPath, String newElement) {
        if (classPath == null || classPath.isEmpty()) {
            return newElement;
        }
        return classPath + ":" + newElement;
    }

    private static Runnable forkSystemServer(String abiList, String socketName, ZygoteServer zygoteServer) {
        long capabilities = posixCapabilitiesAsBits(OsConstants.CAP_IPC_LOCK, OsConstants.CAP_KILL, OsConstants.CAP_NET_ADMIN, OsConstants.CAP_NET_BIND_SERVICE, OsConstants.CAP_NET_BROADCAST, OsConstants.CAP_NET_RAW, OsConstants.CAP_SYS_MODULE, OsConstants.CAP_SYS_NICE, OsConstants.CAP_SYS_PTRACE, OsConstants.CAP_SYS_TIME, OsConstants.CAP_SYS_TTY_CONFIG, OsConstants.CAP_WAKE_ALARM, OsConstants.CAP_BLOCK_SUSPEND);
        StructCapUserHeader header = new StructCapUserHeader(OsConstants._LINUX_CAPABILITY_VERSION_3, 0);
        try {
            StructCapUserData[] data = Os.capget(header);
            long capabilities2 = ((((long) data[1].effective) << 32) | ((long) data[0].effective)) & capabilities;
            try {
                ZygoteConnection.Arguments parsedArgs = new ZygoteConnection.Arguments(new String[]{"--setuid=1000", "--setgid=1000", "--setgroups=1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1018,1021,1023,1024,1032,1065,3001,3002,3003,3006,3007,3009,3010,3011", "--capabilities=" + capabilities2 + "," + capabilities2, "--nice-name=system_server", "--runtime-args", "--target-sdk-version=10000", "com.android.server.SystemServer"});
                ZygoteConnection.applyDebuggerSystemProperty(parsedArgs);
                ZygoteConnection.applyInvokeWithSystemProperty(parsedArgs);
                if (SystemProperties.getBoolean("dalvik.vm.profilesystemserver", false)) {
                    try {
                        parsedArgs.runtimeFlags |= 16384;
                    } catch (IllegalArgumentException e) {
                        ex = e;
                        StructCapUserHeader structCapUserHeader = header;
                    }
                }
                StructCapUserHeader structCapUserHeader2 = header;
                try {
                    if (Zygote.forkSystemServer(parsedArgs.uid, parsedArgs.gid, parsedArgs.gids, parsedArgs.runtimeFlags, null, parsedArgs.permittedCapabilities, parsedArgs.effectiveCapabilities) != 0) {
                        return null;
                    }
                    if (hasSecondZygote(abiList)) {
                        waitForSecondaryZygote(socketName);
                    }
                    zygoteServer.closeServerSocket();
                    return handleSystemServerProcess(parsedArgs);
                } catch (IllegalArgumentException e2) {
                    ex = e2;
                    throw new RuntimeException(ex);
                }
            } catch (IllegalArgumentException e3) {
                ex = e3;
                StructCapUserHeader structCapUserHeader3 = header;
                throw new RuntimeException(ex);
            }
        } catch (ErrnoException ex) {
            StructCapUserHeader structCapUserHeader4 = header;
            ErrnoException errnoException = ex;
            throw new RuntimeException("Failed to capget()", ex);
        }
    }

    private static long posixCapabilitiesAsBits(int... capabilities) {
        long result = 0;
        for (int capability : capabilities) {
            if (capability < 0 || capability > OsConstants.CAP_LAST_CAP) {
                throw new IllegalArgumentException(String.valueOf(capability));
            }
            result |= 1 << capability;
        }
        return result;
    }

    public static void main(String[] argv) {
        ZygoteServer zygoteServer = new ZygoteServer();
        ZygoteHooks.startZygoteNoThreadCreation();
        try {
            Os.setpgid(0, 0);
            HwFrameworkFactory.getLogException().initLogBlackList();
            try {
                if (!"1".equals(SystemProperties.get("sys.boot_completed"))) {
                    MetricsLogger.histogram(null, "boot_zygote_init", (int) SystemClock.elapsedRealtime());
                }
                TimingsTraceLog bootTimingsTraceLog = new TimingsTraceLog(Process.is64Bit() ? "Zygote64Timing" : "Zygote32Timing", 16384);
                bootTimingsTraceLog.traceBegin("ZygoteInit");
                int myPriority = Process.getThreadPriority(Process.myPid());
                Process.setThreadPriority(-19);
                RuntimeInit.enableDdms();
                String socketName = "zygote";
                String abiList = null;
                boolean enableLazyPreload = false;
                boolean startSystemServer = false;
                for (int i = 1; i < argv.length; i++) {
                    if ("start-system-server".equals(argv[i])) {
                        startSystemServer = true;
                        isPrimaryCpuAbi = true;
                    } else if ("--enable-lazy-preload".equals(argv[i])) {
                        enableLazyPreload = true;
                    } else if (argv[i].startsWith(ABI_LIST_ARG)) {
                        abiList = argv[i].substring(ABI_LIST_ARG.length());
                    } else if (argv[i].startsWith(SOCKET_NAME_ARG)) {
                        socketName = argv[i].substring(SOCKET_NAME_ARG.length());
                    } else {
                        throw new RuntimeException("Unknown command line argument: " + argv[i]);
                    }
                }
                if (abiList != null) {
                    zygoteServer.registerServerSocketFromEnv(socketName);
                    if (!enableLazyPreload) {
                        bootTimingsTraceLog.traceBegin("ZygotePreload");
                        EventLog.writeEvent(3020, SystemClock.uptimeMillis());
                        Jlog.d(28, "JL_BOOT_PROGRESS_PRELOAD_START:" + argv[0]);
                        Log.initHWLog();
                        preload(bootTimingsTraceLog);
                        EventLog.writeEvent(LOG_BOOT_PROGRESS_PRELOAD_END, SystemClock.uptimeMillis());
                        bootTimingsTraceLog.traceEnd();
                        Jlog.d(29, "JL_BOOT_PROGRESS_PRELOAD_END");
                    } else {
                        Zygote.resetNicePriority();
                    }
                    bootTimingsTraceLog.traceBegin("PostZygoteInitGC");
                    gcAndFinalize();
                    bootTimingsTraceLog.traceEnd();
                    bootTimingsTraceLog.traceEnd();
                    Trace.setTracingEnabled(false, 0);
                    Zygote.nativeSecurityInit();
                    Process.setThreadPriority(myPriority);
                    Zygote.nativeUnmountStorageOnInit();
                    ZygoteHooks.stopZygoteNoThreadCreation();
                    if (startSystemServer) {
                        Runnable r = forkSystemServer(abiList, socketName, zygoteServer);
                        if (r != null) {
                            r.run();
                            zygoteServer.closeServerSocket();
                            return;
                        }
                    }
                    Log.i(TAG, "Accepting command socket connections");
                    Runnable caller = zygoteServer.runSelectLoop(abiList);
                    zygoteServer.closeServerSocket();
                    if (caller != null) {
                        caller.run();
                    }
                    return;
                }
                throw new RuntimeException("No ABI list supplied.");
            } catch (Throwable th) {
                zygoteServer.closeServerSocket();
                throw th;
            }
        } catch (ErrnoException ex) {
            throw new RuntimeException("Failed to setpgid(0,0)", ex);
        }
    }

    private static boolean hasSecondZygote(String abiList) {
        return !SystemProperties.get("ro.product.cpu.abilist").equals(abiList);
    }

    private static void waitForSecondaryZygote(String socketName) {
        ZygoteProcess.waitForConnectionToZygote("zygote".equals(socketName) ? "zygote_secondary" : "zygote");
    }

    static boolean isPreloadComplete() {
        return sPreloadComplete;
    }

    private ZygoteInit() {
    }

    public static final Runnable zygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) {
        Trace.traceBegin(64, "ZygoteInit");
        RuntimeInit.redirectLogStreams();
        RuntimeInit.commonInit();
        nativeZygoteInit();
        return RuntimeInit.applicationInit(targetSdkVersion, argv, classLoader);
    }

    static final Runnable childZygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) {
        RuntimeInit.Arguments args = new RuntimeInit.Arguments(argv);
        return RuntimeInit.findStaticMain(args.startClass, args.startArgs, classLoader);
    }
}
