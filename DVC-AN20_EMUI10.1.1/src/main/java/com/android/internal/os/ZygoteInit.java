package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.app.ApplicationLoaders;
import android.common.HwFrameworkFactory;
import android.content.pm.SharedLibraryInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.wifi.WifiEnterpriseConfig;
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
import android.provider.SettingsStringUtil;
import android.security.keystore.AndroidKeyStoreProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructCapUserData;
import android.system.StructCapUserHeader;
import android.telephony.SmsManager;
import android.text.Hyphenator;
import android.util.EventLog;
import android.util.Log;
import android.util.TimingsTraceLog;
import android.webkit.WebViewFactory;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.RuntimeInit;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import libcore.io.IoUtils;

public class ZygoteInit {
    private static final String ABI_LIST_ARG = "--abi-list=";
    private static final int LOG_BOOT_PROGRESS_PRELOAD_END = 3030;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_START = 3020;
    private static final String NON_BOOTCLASSPATH_JAR_HIDL_BASE = "android.hidl.base-V1.0-java";
    private static final String NON_BOOTCLASSPATH_JAR_HIDL_MANAGER = "android.hidl.manager-V1.0-java";
    private static final String PRELOADED_CLASSES = "/system/etc/preloaded-classes";
    private static final int PRELOAD_GC_THRESHOLD = 50000;
    public static final boolean PRELOAD_RESOURCES = true;
    private static final String PROPERTY_DISABLE_GRAPHICS_DRIVER_PRELOADING = "ro.zygote.disable_gl_preload";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String SOCKET_NAME_ARG = "--socket-name=";
    private static final String TAG = "Zygote";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;
    private static boolean ignoreNativeCallLoading = true;
    @UnsupportedAppUsage
    private static Resources mResources;
    private static ArrayList<String> nonBootClasspathList = new ArrayList<>();
    private static ClassLoader sCachedSystemServerClassLoader = null;
    public static final boolean sIsMygote = (System.getenv("MAPLE_RUNTIME") != null);
    private static boolean sPreloadComplete;

    private static native void nativePreloadAppProcessHALs();

    static native void nativePreloadGraphicsDriver();

    private static final native void nativeZygoteInit();

    static void preload(TimingsTraceLog bootTimingsTraceLog) {
        Log.d(TAG, "begin preload");
        bootTimingsTraceLog.traceBegin("BeginPreload");
        beginPreload();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("PreloadClasses");
        preloadClasses();
        HwFeatureLoader.SystemFeature.loadFeatureFramework(null);
        HwFeatureLoader.SystemFeature.preloadClasses();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("CacheNonBootClasspathClassLoaders");
        cacheNonBootClasspathClassLoaders();
        bootTimingsTraceLog.traceEnd();
        bootTimingsTraceLog.traceBegin("PreloadResources");
        preloadResources();
        bootTimingsTraceLog.traceEnd();
        Trace.traceBegin(16384, "PreloadAppProcessHALs");
        nativePreloadAppProcessHALs();
        Trace.traceEnd(16384);
        Trace.traceBegin(16384, "PreloadGraphicsDriver");
        maybePreloadGraphicsDriver();
        Trace.traceEnd(16384);
        preloadSharedLibraries();
        preloadTextResources();
        preloadHwThemeZipsAndSomeIcons(0);
        WebViewFactory.prepareWebViewInZygote();
        endPreload();
        warmUpJcaProviders();
        Log.d(TAG, "end preload");
        sPreloadComplete = true;
    }

    public static void lazyPreload() {
        Preconditions.checkState(!sPreloadComplete);
        Log.i(TAG, "Lazily preloading resources.");
        preload(new TimingsTraceLog("ZygoteInitTiming_lazy", 16384));
    }

    private static void beginPreload() {
        Log.i(TAG, "Calling ZygoteHooks.beginPreload()");
        ZygoteHooks.onBeginPreload();
    }

    private static void endPreload() {
        ZygoteHooks.onEndPreload();
        Log.i(TAG, "Called ZygoteHooks.endPreload()");
    }

    private static void preloadSharedLibraries() {
        Log.i(TAG, "Preloading shared libraries...");
        System.loadLibrary("android");
        System.loadLibrary("compiler_rt");
        System.loadLibrary("jnigraphics");
    }

    private static void maybePreloadGraphicsDriver() {
        if (!SystemProperties.getBoolean(PROPERTY_DISABLE_GRAPHICS_DRIVER_PRELOADING, false)) {
            nativePreloadGraphicsDriver();
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

    /* JADX WARNING: Removed duplicated region for block: B:104:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01ab  */
    private static void preloadClasses() {
        float defaultUtilization;
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
                    Os.setregid(0, Process.NOBODY_UID);
                    Os.setreuid(0, Process.NOBODY_UID);
                    droppedPriviliges = true;
                } catch (ErrnoException ex) {
                    throw new RuntimeException("Failed to drop root", ex);
                }
            }
            float defaultUtilization2 = runtime.getTargetHeapUtilization();
            runtime.setTargetHeapUtilization(0.8f);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is), 256);
                int count = 0;
                while (true) {
                    String line = br.readLine();
                    if (line == null) {
                        break;
                    }
                    try {
                        String line2 = line.trim();
                        if (line2.startsWith("#") || line2.equals("")) {
                            defaultUtilization2 = defaultUtilization2;
                        } else {
                            try {
                                Trace.traceBegin(16384, line2);
                                try {
                                    Class.forName(line2, true, null);
                                    count++;
                                } catch (ClassNotFoundException e) {
                                    Log.w(TAG, "Class not found for preloading: " + line2);
                                } catch (UnsatisfiedLinkError e2) {
                                    Log.w(TAG, "Problem preloading " + line2 + ": " + e2);
                                } catch (Throwable t) {
                                    Log.e(TAG, "Error preloading " + line2 + ".", t);
                                    if (t instanceof Error) {
                                        throw ((Error) t);
                                    } else if (t instanceof RuntimeException) {
                                        throw ((RuntimeException) t);
                                    } else {
                                        throw new RuntimeException(t);
                                    }
                                }
                                Trace.traceEnd(16384);
                                defaultUtilization2 = defaultUtilization2;
                            } catch (IOException e3) {
                                e = e3;
                                defaultUtilization = defaultUtilization2;
                                try {
                                    Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                                    IoUtils.closeQuietly(is);
                                    runtime.setTargetHeapUtilization(defaultUtilization);
                                    Trace.traceBegin(16384, "PreloadDexCaches");
                                    runtime.preloadDexCaches();
                                    Trace.traceEnd(16384);
                                    if (!droppedPriviliges) {
                                        try {
                                            Os.setreuid(0, 0);
                                            Os.setregid(0, 0);
                                            return;
                                        } catch (ErrnoException ex2) {
                                            throw new RuntimeException("Failed to restore root", ex2);
                                        }
                                    } else {
                                        return;
                                    }
                                } catch (Throwable th) {
                                    ex = th;
                                    IoUtils.closeQuietly(is);
                                    runtime.setTargetHeapUtilization(defaultUtilization);
                                    Trace.traceBegin(16384, "PreloadDexCaches");
                                    runtime.preloadDexCaches();
                                    Trace.traceEnd(16384);
                                    if (droppedPriviliges) {
                                        try {
                                            Os.setreuid(0, 0);
                                            Os.setregid(0, 0);
                                        } catch (ErrnoException ex3) {
                                            throw new RuntimeException("Failed to restore root", ex3);
                                        }
                                    }
                                    throw ex;
                                }
                            } catch (Throwable th2) {
                                ex = th2;
                                defaultUtilization = defaultUtilization2;
                                IoUtils.closeQuietly(is);
                                runtime.setTargetHeapUtilization(defaultUtilization);
                                Trace.traceBegin(16384, "PreloadDexCaches");
                                runtime.preloadDexCaches();
                                Trace.traceEnd(16384);
                                if (droppedPriviliges) {
                                }
                                throw ex;
                            }
                        }
                    } catch (IOException e4) {
                        e = e4;
                        defaultUtilization = defaultUtilization2;
                        Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                        IoUtils.closeQuietly(is);
                        runtime.setTargetHeapUtilization(defaultUtilization);
                        Trace.traceBegin(16384, "PreloadDexCaches");
                        runtime.preloadDexCaches();
                        Trace.traceEnd(16384);
                        if (!droppedPriviliges) {
                        }
                    } catch (Throwable th3) {
                        ex = th3;
                        defaultUtilization = defaultUtilization2;
                        IoUtils.closeQuietly(is);
                        runtime.setTargetHeapUtilization(defaultUtilization);
                        Trace.traceBegin(16384, "PreloadDexCaches");
                        runtime.preloadDexCaches();
                        Trace.traceEnd(16384);
                        if (droppedPriviliges) {
                        }
                        throw ex;
                    }
                }
                try {
                    Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization2);
                    Trace.traceBegin(16384, "PreloadDexCaches");
                    runtime.preloadDexCaches();
                    Trace.traceEnd(16384);
                    if (droppedPriviliges) {
                        try {
                            Os.setreuid(0, 0);
                            Os.setregid(0, 0);
                        } catch (ErrnoException ex4) {
                            throw new RuntimeException("Failed to restore root", ex4);
                        }
                    }
                } catch (IOException e5) {
                    e = e5;
                    defaultUtilization = defaultUtilization2;
                    Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    Trace.traceBegin(16384, "PreloadDexCaches");
                    runtime.preloadDexCaches();
                    Trace.traceEnd(16384);
                    if (!droppedPriviliges) {
                    }
                } catch (Throwable th4) {
                    ex = th4;
                    defaultUtilization = defaultUtilization2;
                    IoUtils.closeQuietly(is);
                    runtime.setTargetHeapUtilization(defaultUtilization);
                    Trace.traceBegin(16384, "PreloadDexCaches");
                    runtime.preloadDexCaches();
                    Trace.traceEnd(16384);
                    if (droppedPriviliges) {
                    }
                    throw ex;
                }
            } catch (IOException e6) {
                e = e6;
                defaultUtilization = defaultUtilization2;
                Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e);
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization);
                Trace.traceBegin(16384, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(16384);
                if (!droppedPriviliges) {
                }
            } catch (Throwable th5) {
                ex = th5;
                defaultUtilization = defaultUtilization2;
                IoUtils.closeQuietly(is);
                runtime.setTargetHeapUtilization(defaultUtilization);
                Trace.traceBegin(16384, "PreloadDexCaches");
                runtime.preloadDexCaches();
                Trace.traceEnd(16384);
                if (droppedPriviliges) {
                }
                throw ex;
            }
        } catch (FileNotFoundException e7) {
            Log.e(TAG, "Couldn't find /system/etc/preloaded-classes.");
        }
    }

    public static List<String> getNonBootClasspathList() {
        List<String> ret = new ArrayList<>(Arrays.asList(new String[nonBootClasspathList.size()]));
        Collections.copy(ret, nonBootClasspathList);
        return ret;
    }

    private static void cacheNonBootClasspathClassLoaders() {
        SharedLibraryInfo hidlBase = new SharedLibraryInfo("/system/framework/android.hidl.base-V1.0-java.jar", null, null, null, 0, 0, null, null, null);
        SharedLibraryInfo hidlManager = new SharedLibraryInfo("/system/framework/android.hidl.manager-V1.0-java.jar", null, null, null, 0, 0, null, null, null);
        hidlManager.addDependency(hidlBase);
        ApplicationLoaders.getDefault().createAndCacheNonBootclasspathSystemClassLoaders(new SharedLibraryInfo[]{hidlBase, hidlManager});
        if (sIsMygote) {
            if (!nonBootClasspathList.contains(NON_BOOTCLASSPATH_JAR_HIDL_BASE)) {
                nonBootClasspathList.add(NON_BOOTCLASSPATH_JAR_HIDL_BASE);
            }
            if (!nonBootClasspathList.contains(NON_BOOTCLASSPATH_JAR_HIDL_MANAGER)) {
                nonBootClasspathList.add(NON_BOOTCLASSPATH_JAR_HIDL_MANAGER);
            }
        }
    }

    private static void preloadResources() {
        VMRuntime.getRuntime();
        try {
            mResources = Resources.getSystem();
            mResources.startPreloading();
            Log.i(TAG, "Preloading resources...");
            long startTime = SystemClock.uptimeMillis();
            TypedArray ar = mResources.obtainTypedArray(R.array.preloaded_drawables);
            int N = preloadDrawables(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            long startTime2 = SystemClock.uptimeMillis();
            TypedArray ar2 = mResources.obtainTypedArray(R.array.preloaded_color_state_lists);
            int N2 = preloadColorStateLists(ar2);
            ar2.recycle();
            Log.i(TAG, "...preloaded " + N2 + " resources in " + (SystemClock.uptimeMillis() - startTime2) + "ms.");
            if (mResources.getBoolean(R.bool.config_freeformWindowManagement)) {
                long startTime3 = SystemClock.uptimeMillis();
                TypedArray ar3 = mResources.obtainTypedArray(R.array.preloaded_freeform_multi_window_drawables);
                int N3 = preloadDrawables(ar3);
                ar3.recycle();
                Log.i(TAG, "...preloaded " + N3 + " resource in " + (SystemClock.uptimeMillis() - startTime3) + "ms.");
            }
            mResources.finishPreloading();
        } catch (RuntimeException e) {
            Log.w(TAG, "Failure preloading resources", e);
        }
    }

    private static int preloadColorStateLists(TypedArray ar) {
        int N = ar.length();
        for (int i = 0; i < N; i++) {
            int id = ar.getResourceId(i, 0);
            if (id != 0 && mResources.getColorStateList(id, null) == null) {
                throw new IllegalArgumentException("Unable to find preloaded color resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    private static int preloadDrawables(TypedArray ar) {
        int N = ar.length();
        for (int i = 0; i < N; i++) {
            int id = ar.getResourceId(i, 0);
            if (id != 0 && mResources.getDrawable(id, null) == null) {
                throw new IllegalArgumentException("Unable to find preloaded drawable resource #0x" + Integer.toHexString(id) + " (" + ar.getString(i) + ")");
            }
        }
        return N;
    }

    public static void preloadHwThemeZipsAndSomeIcons(int currentUserId) {
        if (mResources == null) {
            mResources = Resources.getSystem();
        }
        Log.i(TAG, "preloadHwThemeZipsAndSomeIcons");
        mResources.getImpl().getHwResourcesImpl().preloadHwThemeZipsAndSomeIcons(currentUserId);
    }

    public static void clearHwThemeZipsAndSomeIcons() {
        if (mResources == null) {
            mResources = Resources.getSystem();
        }
        Log.i(TAG, "clearHwThemeZipsAndSomeIcons");
        mResources.getImpl().getHwResourcesImpl().clearHwThemeZipsAndSomeIcons();
    }

    private static void gcAndFinalize() {
        ZygoteHooks.gcAndFinalize();
    }

    private static Runnable handleSystemServerProcess(ZygoteArguments parsedArgs) {
        ExitCatch.enable(Process.myPid(), 7);
        Os.umask(OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        if (parsedArgs.mNiceName != null) {
            Process.setArgV0(parsedArgs.mNiceName);
        }
        String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
        if (systemServerClasspath != null) {
            if (performSystemServerDexOpt(systemServerClasspath)) {
                sCachedSystemServerClassLoader = null;
            }
            if (SystemProperties.getBoolean("dalvik.vm.profilesystemserver", false) && (Build.IS_USERDEBUG || Build.IS_ENG)) {
                try {
                    prepareSystemServerProfile(systemServerClasspath);
                } catch (Exception e) {
                    Log.wtf(TAG, "Failed to set up system server profile", e);
                }
            }
        }
        if (parsedArgs.mInvokeWith != null) {
            String[] args = parsedArgs.mRemainingArgs;
            if (systemServerClasspath != null) {
                String[] amendedArgs = new String[(args.length + 2)];
                amendedArgs[0] = "-cp";
                amendedArgs[1] = systemServerClasspath;
                System.arraycopy(args, 0, amendedArgs, 2, args.length);
                args = amendedArgs;
            }
            WrapperInit.execApplication(parsedArgs.mInvokeWith, parsedArgs.mNiceName, parsedArgs.mTargetSdkVersion, VMRuntime.getCurrentInstructionSet(), null, args);
            throw new IllegalStateException("Unexpected return from WrapperInit.execApplication");
        }
        createSystemServerClassLoader();
        ClassLoader cl = sCachedSystemServerClassLoader;
        if (cl != null) {
            Thread.currentThread().setContextClassLoader(cl);
            if (sIsMygote) {
                VMRuntime.getRuntime();
                VMRuntime.notifyClassLoaderConstructed(cl);
            }
        }
        return zygoteInit(parsedArgs.mTargetSdkVersion, parsedArgs.mRemainingArgs, cl);
    }

    private static void createSystemServerClassLoader() {
        String systemServerClasspath;
        if (sIsMygote && ignoreNativeCallLoading) {
            ignoreNativeCallLoading = false;
        } else if (sCachedSystemServerClassLoader == null && (systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH")) != null) {
            if (sIsMygote) {
                VMRuntime.getRuntime();
                VMRuntime.preSystemServerClLoad();
            }
            sCachedSystemServerClassLoader = createPathClassLoader(systemServerClasspath, 10000);
        }
    }

    private static void prepareSystemServerProfile(String systemServerClasspath) throws RemoteException {
        if (!systemServerClasspath.isEmpty()) {
            String[] codePaths = systemServerClasspath.split(SettingsStringUtil.DELIMITER);
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

    public static void setHiddenApiUsageLogger(VMRuntime.HiddenApiUsageLogger logger) {
        VMRuntime.getRuntime();
        VMRuntime.setHiddenApiUsageLogger(logger);
    }

    static ClassLoader createPathClassLoader(String classPath, int targetSdkVersion) {
        String libraryPath = System.getProperty("java.library.path");
        return ClassLoaderFactory.createClassLoader(classPath, libraryPath, libraryPath, ClassLoader.getSystemClassLoader().getParent(), targetSdkVersion, true, null);
    }

    private static boolean performSystemServerDexOpt(String classPath) {
        int[] retDexOpt;
        IInstalld installd;
        String instructionSet;
        HwZygoteInit hwZygoteInit;
        int i;
        int i2;
        int index;
        int dexoptNeeded;
        int i3;
        String str;
        String classPathElement;
        String classPathForElement;
        int[] retDexOpt2;
        String[] classPathElements = classPath.split(SettingsStringUtil.DELIMITER);
        IInstalld installd2 = IInstalld.Stub.asInterface(ServiceManager.getService("installd"));
        String instructionSet2 = VMRuntime.getRuntime().vmInstructionSet();
        HwZygoteInit hwZygoteInit2 = HwFrameworkFactory.getHwZygoteInit();
        if (!sIsMygote) {
            retDexOpt = null;
        } else if (hwZygoteInit2 == null || (retDexOpt2 = hwZygoteInit2.getDexOptNeededForMapleSystemServer(installd2, classPathElements, instructionSet2)) == null) {
            return false;
        } else {
            retDexOpt = retDexOpt2;
        }
        int length = classPathElements.length;
        int index2 = 0;
        String classPathForElement2 = "";
        boolean compiledSomething = false;
        int i4 = 0;
        while (i4 < length) {
            String classPathElement2 = classPathElements[i4];
            String systemServerFilter = SystemProperties.get("dalvik.vm.systemservercompilerfilter", "speed");
            if (sIsMygote) {
                index = index2 + 1;
                dexoptNeeded = retDexOpt[index2];
                classPathElement = classPathElement2;
                i3 = i4;
                str = TAG;
            } else {
                i3 = i4;
                str = TAG;
                classPathElement = classPathElement2;
                try {
                    dexoptNeeded = DexFile.getDexOptNeeded(classPathElement2, instructionSet2, systemServerFilter, null, false, false);
                    index = index2;
                } catch (FileNotFoundException e) {
                    i = length;
                    hwZygoteInit = hwZygoteInit2;
                    instructionSet = instructionSet2;
                    installd = installd2;
                    i2 = i3;
                    Log.w(str, "Missing classpath element for system server: " + classPathElement);
                    classPathForElement2 = classPathForElement2;
                } catch (IOException e2) {
                    Log.w(str, "Error checking classpath element for system server: " + classPathElement, e2);
                    dexoptNeeded = 0;
                    index = index2;
                }
            }
            if (dexoptNeeded != 0) {
                classPathForElement = classPathForElement2;
                i2 = i3;
                i = length;
                hwZygoteInit = hwZygoteInit2;
                instructionSet = instructionSet2;
                installd = installd2;
                try {
                    installd2.dexopt(classPathElement, 1000, "*", instructionSet2, dexoptNeeded, null, 0, systemServerFilter, StorageManager.UUID_PRIVATE_INTERNAL, getSystemServerClassLoaderContext(classPathForElement2), null, false, 0, null, null, "server-dexopt");
                    compiledSomething = true;
                } catch (RemoteException | ServiceSpecificException e3) {
                    Log.w(str, "Failed compiling classpath element for system server: " + classPathElement, e3);
                }
            } else {
                classPathForElement = classPathForElement2;
                i = length;
                hwZygoteInit = hwZygoteInit2;
                instructionSet = instructionSet2;
                installd = installd2;
                i2 = i3;
            }
            classPathForElement2 = encodeSystemServerClassPath(classPathForElement, classPathElement);
            index2 = index;
            i4 = i2 + 1;
            length = i;
            hwZygoteInit2 = hwZygoteInit;
            instructionSet2 = instructionSet;
            installd2 = installd;
        }
        return compiledSomething;
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
        return classPath + SettingsStringUtil.DELIMITER + newElement;
    }

    private static Runnable forkSystemServer(String abiList, String socketName, ZygoteServer zygoteServer) {
        long capabilities = posixCapabilitiesAsBits(OsConstants.CAP_IPC_LOCK, OsConstants.CAP_KILL, OsConstants.CAP_NET_ADMIN, OsConstants.CAP_NET_BIND_SERVICE, OsConstants.CAP_NET_BROADCAST, OsConstants.CAP_NET_RAW, OsConstants.CAP_SYS_MODULE, OsConstants.CAP_SYS_NICE, OsConstants.CAP_SYS_PTRACE, OsConstants.CAP_SYS_TIME, OsConstants.CAP_SYS_TTY_CONFIG, OsConstants.CAP_WAKE_ALARM, OsConstants.CAP_BLOCK_SUSPEND);
        try {
            StructCapUserData[] data = Os.capget(new StructCapUserHeader(OsConstants._LINUX_CAPABILITY_VERSION_3, 0));
            long capabilities2 = ((((long) data[1].effective) << 32) | ((long) data[0].effective)) & capabilities;
            try {
                ZygoteArguments parsedArgs = new ZygoteArguments(new String[]{"--setuid=1000", "--setgid=1000", "--setgroups=1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1018,1021,1023,1024,1032,1065,3001,3002,3003,3006,3007,3009,3010,3011", "--capabilities=" + capabilities2 + SmsManager.REGEX_PREFIX_DELIMITER + capabilities2, "--nice-name=system_server", "--runtime-args", "--target-sdk-version=10000", "com.android.server.SystemServer"});
                Zygote.applyDebuggerSystemProperty(parsedArgs);
                Zygote.applyInvokeWithSystemProperty(parsedArgs);
                if (SystemProperties.getBoolean("dalvik.vm.profilesystemserver", false)) {
                    parsedArgs.mRuntimeFlags |= 16384;
                }
                if (Zygote.forkSystemServer(parsedArgs.mUid, parsedArgs.mGid, parsedArgs.mGids, parsedArgs.mRuntimeFlags, null, parsedArgs.mPermittedCapabilities, parsedArgs.mEffectiveCapabilities) != 0) {
                    return null;
                }
                if (hasSecondZygote(abiList)) {
                    waitForSecondaryZygote(socketName);
                }
                zygoteServer.closeServerSocket();
                return handleSystemServerProcess(parsedArgs);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            }
        } catch (ErrnoException ex2) {
            throw new RuntimeException("Failed to capget()", ex2);
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

    @UnsupportedAppUsage
    public static void main(String[] argv) {
        ZygoteServer zygoteServer;
        boolean isPrimaryZygote;
        Runnable r;
        ZygoteServer zygoteServer2 = null;
        ZygoteHooks.startZygoteNoThreadCreation();
        try {
            Os.setpgid(0, 0);
            try {
                if (!"1".equals(SystemProperties.get("sys.boot_completed"))) {
                    try {
                        MetricsLogger.histogram(null, "boot_zygote_init", (int) SystemClock.elapsedRealtime());
                    } catch (Throwable th) {
                        ex = th;
                        zygoteServer = null;
                    }
                }
                TimingsTraceLog bootTimingsTraceLog = new TimingsTraceLog(Process.is64Bit() ? "Zygote64Timing" : "Zygote32Timing", 16384);
                bootTimingsTraceLog.traceBegin("ZygoteInit");
                RuntimeInit.enableDdms();
                String zygoteSocketName = Zygote.PRIMARY_SOCKET_NAME;
                boolean enableLazyPreload = false;
                String abiList = null;
                boolean startSystemServer = false;
                int i = 1;
                while (i < argv.length) {
                    zygoteServer = zygoteServer2;
                    try {
                        if ("start-system-server".equals(argv[i])) {
                            startSystemServer = true;
                        } else if ("--enable-lazy-preload".equals(argv[i])) {
                            enableLazyPreload = true;
                        } else if (argv[i].startsWith("--abi-list=")) {
                            abiList = argv[i].substring("--abi-list=".length());
                        } else if (argv[i].startsWith(SOCKET_NAME_ARG)) {
                            zygoteSocketName = argv[i].substring(SOCKET_NAME_ARG.length());
                        } else {
                            throw new RuntimeException("Unknown command line argument: " + argv[i]);
                        }
                        i++;
                        zygoteServer2 = zygoteServer;
                    } catch (Throwable th2) {
                        ex = th2;
                        try {
                            Log.e(TAG, "System zygote died with exception", ex);
                            throw ex;
                        } catch (Throwable ex) {
                            if (zygoteServer != null) {
                                zygoteServer.closeServerSocket();
                            }
                            throw ex;
                        }
                    }
                }
                if (sIsMygote) {
                    isPrimaryZygote = zygoteSocketName.equals(Zygote.PRIMARY_MYGOTE_SOCKET_NAME);
                } else {
                    isPrimaryZygote = zygoteSocketName.equals(Zygote.PRIMARY_SOCKET_NAME);
                }
                if ((SystemProperties.get("ro.maple.enable", WifiEnterpriseConfig.ENGINE_DISABLE).equals("1") && !SystemProperties.get("persist.mygote.disable", WifiEnterpriseConfig.ENGINE_DISABLE).equals("1")) && isPrimaryZygote && !startSystemServer) {
                    enableLazyPreload = true;
                }
                if (abiList != null) {
                    Log.initHWLog();
                    if (!enableLazyPreload) {
                        bootTimingsTraceLog.traceBegin("ZygotePreload");
                        EventLog.writeEvent(3020, SystemClock.uptimeMillis());
                        preload(bootTimingsTraceLog);
                        EventLog.writeEvent(3030, SystemClock.uptimeMillis());
                        bootTimingsTraceLog.traceEnd();
                    } else {
                        Zygote.resetNicePriority();
                    }
                    bootTimingsTraceLog.traceBegin("PostZygoteInitGC");
                    gcAndFinalize();
                    bootTimingsTraceLog.traceEnd();
                    bootTimingsTraceLog.traceEnd();
                    Trace.setTracingEnabled(false, 0);
                    Zygote.initNativeState(isPrimaryZygote);
                    ZygoteHooks.stopZygoteNoThreadCreation();
                    ZygoteServer zygoteServer3 = new ZygoteServer(isPrimaryZygote);
                    if (!startSystemServer || (r = forkSystemServer(abiList, zygoteSocketName, zygoteServer3)) == null) {
                        Log.i(TAG, "Accepting command socket connections");
                        Runnable caller = zygoteServer3.runSelectLoop(abiList);
                        zygoteServer3.closeServerSocket();
                        if (caller != null) {
                            caller.run();
                            return;
                        }
                        return;
                    }
                    r.run();
                    zygoteServer3.closeServerSocket();
                    return;
                }
                throw new RuntimeException("No ABI list supplied.");
            } catch (Throwable th3) {
                ex = th3;
                zygoteServer = null;
                Log.e(TAG, "System zygote died with exception", ex);
                throw ex;
            }
        } catch (ErrnoException ex2) {
            throw new RuntimeException("Failed to setpgid(0,0)", ex2);
        }
    }

    private static boolean hasSecondZygote(String abiList) {
        return !SystemProperties.get("ro.product.cpu.abilist").equals(abiList);
    }

    private static void waitForSecondaryZygote(String socketName) {
        boolean equals = Zygote.PRIMARY_MYGOTE_SOCKET_NAME.equals(socketName);
        String otherZygoteName = Zygote.SECONDARY_SOCKET_NAME;
        if (equals) {
            ZygoteProcess.waitForConnectionToZygote(Zygote.PRIMARY_SOCKET_NAME);
            ZygoteProcess.waitForConnectionToZygote(otherZygoteName);
            return;
        }
        if (!Zygote.PRIMARY_SOCKET_NAME.equals(socketName)) {
            otherZygoteName = Zygote.PRIMARY_SOCKET_NAME;
        }
        ZygoteProcess.waitForConnectionToZygote(otherZygoteName);
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
