package com.android.internal.os;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.icu.impl.CacheValue;
import android.icu.impl.CacheValue.Strength;
import android.icu.text.DecimalFormatSymbols;
import android.icu.util.ULocale;
import android.net.LocalServerSocket;
import android.opengl.EGL14;
import android.os.Process;
import android.os.Process.ZygoteState;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.security.keystore.AndroidKeyStoreProvider;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.text.Hyphenator;
import android.util.EventLog;
import android.util.Jlog;
import android.util.Log;
import android.util.PtmLog;
import android.webkit.WebViewFactory;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.telephony.RILConstants;
import com.huawei.pgmng.log.LogPower;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;
import dalvik.system.VMRuntime;
import dalvik.system.ZygoteHooks;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;
import libcore.io.IoUtils;

public class ZygoteInit {
    private static final String ABI_LIST_ARG = "--abi-list=";
    private static final String ANDROID_SOCKET_PREFIX = "ANDROID_SOCKET_";
    private static final int LOG_BOOT_PROGRESS_PRELOAD_END = 3030;
    private static final int LOG_BOOT_PROGRESS_PRELOAD_START = 3020;
    private static final String PRELOADED_CLASSES = "/system/etc/preloaded-classes";
    private static final int PRELOAD_GC_THRESHOLD = 67108864;
    public static final boolean PRELOAD_RESOURCES = true;
    private static final String PROPERTY_DISABLE_OPENGL_PRELOADING = "ro.zygote.disable_gl_preload";
    private static final String PROPERTY_RUNNING_IN_CONTAINER = "ro.boot.container";
    private static final int ROOT_GID = 0;
    private static final int ROOT_UID = 0;
    private static final String SOCKET_NAME_ARG = "--socket-name=";
    private static final String TAG = "Zygote";
    private static final int UNPRIVILEGED_GID = 9999;
    private static final int UNPRIVILEGED_UID = 9999;
    private static boolean isPrimaryCpuAbi;
    private static Resources mResources;
    private static LocalServerSocket sServerSocket;

    public static class MethodAndArgsCaller extends Exception implements Runnable {
        private final String[] mArgs;
        private final Method mMethod;

        public MethodAndArgsCaller(Method method, String[] args) {
            this.mMethod = method;
            this.mArgs = args;
        }

        public void run() {
            try {
                this.mMethod.invoke(null, new Object[]{this.mArgs});
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex2) {
                Throwable cause = ex2.getCause();
                if (cause instanceof RuntimeException) {
                    throw ((RuntimeException) cause);
                } else if (cause instanceof Error) {
                    throw ((Error) cause);
                } else {
                    throw new RuntimeException(ex2);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.ZygoteInit.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.ZygoteInit.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ZygoteInit.<clinit>():void");
    }

    private static void registerZygoteSocket(String socketName) {
        if (sServerSocket == null) {
            String fullSocketName = ANDROID_SOCKET_PREFIX + socketName;
            try {
                int fileDesc = Integer.parseInt(System.getenv(fullSocketName));
                try {
                    FileDescriptor fd = new FileDescriptor();
                    fd.setInt$(fileDesc);
                    sServerSocket = new LocalServerSocket(fd);
                } catch (IOException ex) {
                    throw new RuntimeException("Error binding to local socket '" + fileDesc + "'", ex);
                }
            } catch (RuntimeException ex2) {
                throw new RuntimeException(fullSocketName + " unset or invalid", ex2);
            }
        }
    }

    private static ZygoteConnection acceptCommandPeer(String abiList) {
        try {
            return new ZygoteConnection(sServerSocket.accept(), abiList);
        } catch (IOException ex) {
            throw new RuntimeException("IOException during accept()", ex);
        }
    }

    static void closeServerSocket() {
        try {
            if (sServerSocket != null) {
                FileDescriptor fd = sServerSocket.getFileDescriptor();
                sServerSocket.close();
                if (fd != null) {
                    Os.close(fd);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Zygote:  error closing sockets", ex);
        } catch (ErrnoException ex2) {
            Log.e(TAG, "Zygote:  error closing descriptor", ex2);
        }
        sServerSocket = null;
    }

    static FileDescriptor getServerSocketFileDescriptor() {
        return sServerSocket.getFileDescriptor();
    }

    static void preload() {
        Log.d(TAG, "begin preload");
        Trace.traceBegin(16384, "BeginIcuCachePinning");
        beginIcuCachePinning();
        Trace.traceEnd(16384);
        Trace.traceBegin(16384, "PreloadClasses");
        preloadClasses();
        Trace.traceEnd(16384);
        Trace.traceBegin(16384, "PreloadResources");
        preloadResources();
        Trace.traceEnd(16384);
        Trace.traceBegin(16384, "PreloadOpenGL");
        preloadOpenGL();
        Trace.traceEnd(16384);
        preloadSharedLibraries();
        preloadTextResources();
        if (VMRuntime.getRuntime().is64Bit()) {
            preloadHwThemeZipsAndSomeIcons(ROOT_UID);
        }
        WebViewFactory.prepareWebViewInZygote();
        endIcuCachePinning();
        warmUpJcaProviders();
        Log.d(TAG, "end preload");
    }

    private static void beginIcuCachePinning() {
        int i = ROOT_UID;
        Log.i(TAG, "Installing ICU cache reference pinning...");
        CacheValue.setStrength(Strength.STRONG);
        Log.i(TAG, "Preloading ICU data...");
        ULocale[] localesToPin = new ULocale[]{ULocale.ROOT, ULocale.US, ULocale.getDefault()};
        int length = localesToPin.length;
        while (i < length) {
            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(localesToPin[i]);
            i++;
        }
    }

    private static void endIcuCachePinning() {
        CacheValue.setStrength(Strength.SOFT);
        Log.i(TAG, "Uninstalled ICU cache reference pinning...");
    }

    private static void preloadSharedLibraries() {
        Log.i(TAG, "Preloading shared libraries...");
        System.loadLibrary("android");
        System.loadLibrary("compiler_rt");
        System.loadLibrary("jnigraphics");
    }

    private static void preloadOpenGL() {
        if (!SystemProperties.getBoolean(PROPERTY_DISABLE_OPENGL_PRELOADING, false)) {
            EGL14.eglGetDisplay(ROOT_UID);
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
        startTime = SystemClock.uptimeMillis();
        Trace.traceBegin(16384, "Starting warm up of JCA providers");
        Provider[] providers = Security.getProviders();
        int length = providers.length;
        for (int i = ROOT_UID; i < length; i++) {
            providers[i].warmUpServiceProvision();
        }
        Log.i(TAG, "Warmed up JCA providers in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
        Trace.traceEnd(16384);
    }

    private static void preloadClasses() {
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
                    Os.setregid(ROOT_UID, UNPRIVILEGED_UID);
                    Os.setreuid(ROOT_UID, UNPRIVILEGED_UID);
                    droppedPriviliges = PRELOAD_RESOURCES;
                } catch (ErrnoException ex) {
                    throw new RuntimeException("Failed to drop root", ex);
                }
            }
            float defaultUtilization = runtime.getTargetHeapUtilization();
            runtime.setTargetHeapUtilization(0.8f);
            BufferedReader br = new BufferedReader(new InputStreamReader(is), GL10.GL_DEPTH_BUFFER_BIT);
            int count = ROOT_UID;
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (!line.startsWith("#")) {
                    if (line.equals("")) {
                        continue;
                    } else {
                        Trace.traceBegin(16384, "PreloadClass " + line);
                        try {
                            Class.forName(line, PRELOAD_RESOURCES, null);
                            count++;
                        } catch (ClassNotFoundException e) {
                            Log.w(TAG, "Class not found for preloading: " + line);
                        } catch (UnsatisfiedLinkError e2) {
                            Log.w(TAG, "Problem preloading " + line + ": " + e2);
                        } catch (IOException e3) {
                            Log.e(TAG, "Error reading /system/etc/preloaded-classes.", e3);
                            IoUtils.closeQuietly(is);
                            runtime.setTargetHeapUtilization(defaultUtilization);
                            Trace.traceBegin(16384, "PreloadDexCaches");
                            runtime.preloadDexCaches();
                            Trace.traceEnd(16384);
                            if (droppedPriviliges) {
                                try {
                                    Os.setreuid(ROOT_UID, ROOT_UID);
                                    Os.setregid(ROOT_UID, ROOT_UID);
                                } catch (ErrnoException ex2) {
                                    throw new RuntimeException("Failed to restore root", ex2);
                                }
                            }
                        } catch (Throwable th) {
                            IoUtils.closeQuietly(is);
                            runtime.setTargetHeapUtilization(defaultUtilization);
                            Trace.traceBegin(16384, "PreloadDexCaches");
                            runtime.preloadDexCaches();
                            Trace.traceEnd(16384);
                            if (droppedPriviliges) {
                                try {
                                    Os.setreuid(ROOT_UID, ROOT_UID);
                                    Os.setregid(ROOT_UID, ROOT_UID);
                                } catch (ErrnoException ex22) {
                                    throw new RuntimeException("Failed to restore root", ex22);
                                }
                            }
                        }
                        Trace.traceEnd(16384);
                    }
                }
            }
            Log.i(TAG, "...preloaded " + count + " classes in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            IoUtils.closeQuietly(is);
            runtime.setTargetHeapUtilization(defaultUtilization);
            Trace.traceBegin(16384, "PreloadDexCaches");
            runtime.preloadDexCaches();
            Trace.traceEnd(16384);
            if (droppedPriviliges) {
                try {
                    Os.setreuid(ROOT_UID, ROOT_UID);
                    Os.setregid(ROOT_UID, ROOT_UID);
                } catch (ErrnoException ex222) {
                    throw new RuntimeException("Failed to restore root", ex222);
                }
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "Couldn't find /system/etc/preloaded-classes.");
        }
    }

    private static void preloadResources() {
        VMRuntime runtime = VMRuntime.getRuntime();
        try {
            mResources = Resources.getSystem(PRELOAD_RESOURCES);
            mResources.startPreloading();
            Log.i(TAG, "Preloading resources...");
            long startTime = SystemClock.uptimeMillis();
            TypedArray ar = mResources.obtainTypedArray(R.array.preloaded_drawables);
            int N = preloadDrawables(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            startTime = SystemClock.uptimeMillis();
            ar = mResources.obtainTypedArray(R.array.preloaded_color_state_lists);
            N = preloadColorStateLists(ar);
            ar.recycle();
            Log.i(TAG, "...preloaded " + N + " resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            if (mResources.getBoolean(R.bool.config_freeformWindowManagement)) {
                startTime = SystemClock.uptimeMillis();
                ar = mResources.obtainTypedArray(R.array.preloaded_freeform_multi_window_drawables);
                N = preloadDrawables(ar);
                ar.recycle();
                Log.i(TAG, "...preloaded " + N + " resource in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            }
            if (isPrimaryCpuAbi) {
                startTime = SystemClock.uptimeMillis();
                ar = mResources.obtainTypedArray(androidhwext.R.array.preloaded_drawables);
                N = preloadDrawables(ar);
                ar.recycle();
                Log.w(TAG, "...preloaded " + N + " hwextdrawable resources in " + (SystemClock.uptimeMillis() - startTime) + "ms.");
            }
            mResources.finishPreloading();
        } catch (RuntimeException e) {
            Log.w(TAG, "Failure preloading resources", e);
        }
    }

    private static int preloadColorStateLists(TypedArray ar) {
        int N = ar.length();
        int i = ROOT_UID;
        while (i < N) {
            int id = ar.getResourceId(i, ROOT_UID);
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
        int i = ROOT_UID;
        while (i < N) {
            int id = ar.getResourceId(i, ROOT_UID);
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
            mResources = Resources.getSystem(PRELOAD_RESOURCES);
        }
        Log.i(TAG, "preloadHwThemeZipsAndSomeIcons");
        mResources.getImpl().getHwResourcesImpl().preloadHwThemeZipsAndSomeIcons(currentUserId);
    }

    public static void clearHwThemeZipsAndSomeIcons() {
        if (mResources == null) {
            mResources = Resources.getSystem(PRELOAD_RESOURCES);
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

    private static void handleSystemServerProcess(Arguments parsedArgs) throws MethodAndArgsCaller {
        closeServerSocket();
        Os.umask(OsConstants.S_IRWXG | OsConstants.S_IRWXO);
        if (parsedArgs.niceName != null) {
            Process.setArgV0(parsedArgs.niceName);
        }
        String systemServerClasspath = Os.getenv("SYSTEMSERVERCLASSPATH");
        if (systemServerClasspath != null) {
            performSystemServerDexOpt(systemServerClasspath);
        }
        if (parsedArgs.invokeWith != null) {
            String[] args = parsedArgs.remainingArgs;
            if (systemServerClasspath != null) {
                String[] amendedArgs = new String[(args.length + 2)];
                amendedArgs[ROOT_UID] = "-cp";
                amendedArgs[1] = systemServerClasspath;
                System.arraycopy(parsedArgs.remainingArgs, ROOT_UID, amendedArgs, 2, parsedArgs.remainingArgs.length);
            }
            WrapperInit.execApplication(parsedArgs.invokeWith, parsedArgs.niceName, parsedArgs.targetSdkVersion, VMRuntime.getCurrentInstructionSet(), null, args);
            return;
        }
        ClassLoader classLoader = null;
        if (systemServerClasspath != null) {
            classLoader = createSystemServerClassLoader(systemServerClasspath, parsedArgs.targetSdkVersion);
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        RuntimeInit.zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, classLoader);
    }

    private static PathClassLoader createSystemServerClassLoader(String systemServerClasspath, int targetSdkVersion) {
        return PathClassLoaderFactory.createClassLoader(systemServerClasspath, System.getProperty("java.library.path"), null, ClassLoader.getSystemClassLoader(), targetSdkVersion, PRELOAD_RESOURCES);
    }

    private static void performSystemServerDexOpt(String classPath) {
        String[] classPathElements = classPath.split(":");
        InstallerConnection installer = new InstallerConnection();
        HwBootCheck.bootSceneStart(LogPower.WEBVIEW_PAUSED, 120000);
        installer.waitForConnection();
        HwBootCheck.bootSceneEnd(LogPower.WEBVIEW_PAUSED);
        String instructionSet = VMRuntime.getRuntime().vmInstructionSet();
        try {
            String sharedLibraries = "";
            int length = classPathElements.length;
            for (int i = ROOT_UID; i < length; i++) {
                String classPathElement = classPathElements[i];
                int dexoptNeeded = DexFile.getDexOptNeeded(classPathElement, instructionSet, "speed", false);
                if (dexoptNeeded != 0) {
                    installer.dexopt(classPathElement, RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED, instructionSet, dexoptNeeded, ROOT_UID, "speed", null, sharedLibraries);
                }
                if (!sharedLibraries.isEmpty()) {
                    sharedLibraries = sharedLibraries + ":";
                }
                sharedLibraries = sharedLibraries + classPathElement;
            }
            installer.disconnect();
        } catch (Exception e) {
            throw new RuntimeException("Error starting system_server", e);
        } catch (Throwable th) {
            installer.disconnect();
        }
    }

    private static boolean startSystemServer(String abiList, String socketName) throws MethodAndArgsCaller, RuntimeException {
        IllegalArgumentException ex;
        long capabilities = posixCapabilitiesAsBits(OsConstants.CAP_IPC_LOCK, OsConstants.CAP_KILL, OsConstants.CAP_NET_ADMIN, OsConstants.CAP_NET_BIND_SERVICE, OsConstants.CAP_NET_BROADCAST, OsConstants.CAP_NET_RAW, OsConstants.CAP_SYS_MODULE, OsConstants.CAP_SYS_NICE, OsConstants.CAP_SYS_RESOURCE, OsConstants.CAP_SYS_TIME, OsConstants.CAP_SYS_TTY_CONFIG);
        if (!SystemProperties.getBoolean(PROPERTY_RUNNING_IN_CONTAINER, false)) {
            capabilities |= posixCapabilitiesAsBits(OsConstants.CAP_BLOCK_SUSPEND);
        }
        try {
            Arguments parsedArgs = new Arguments(new String[]{"--setuid=1000", "--setgid=1000", "--setgroups=1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1018,1021,1023,1032,3001,3002,3003,3006,3007,3009,3010", "--capabilities=" + capabilities + PtmLog.PAIRE_DELIMETER + capabilities, "--nice-name=system_server", "--runtime-args", "com.android.server.SystemServer"});
            try {
                ZygoteConnection.applyDebuggerSystemProperty(parsedArgs);
                ZygoteConnection.applyInvokeWithSystemProperty(parsedArgs);
                if (Zygote.forkSystemServer(parsedArgs.uid, parsedArgs.gid, parsedArgs.gids, parsedArgs.debugFlags, null, parsedArgs.permittedCapabilities, parsedArgs.effectiveCapabilities) == 0) {
                    if (hasSecondZygote(abiList)) {
                        waitForSecondaryZygote(socketName);
                    }
                    handleSystemServerProcess(parsedArgs);
                }
                return PRELOAD_RESOURCES;
            } catch (IllegalArgumentException e) {
                ex = e;
                throw new RuntimeException(ex);
            }
        } catch (IllegalArgumentException e2) {
            ex = e2;
            throw new RuntimeException(ex);
        }
    }

    private static long posixCapabilitiesAsBits(int... capabilities) {
        long result = 0;
        int length = capabilities.length;
        for (int i = ROOT_UID; i < length; i++) {
            int capability = capabilities[i];
            if (capability < 0 || capability > OsConstants.CAP_LAST_CAP) {
                throw new IllegalArgumentException(String.valueOf(capability));
            }
            result |= 1 << capability;
        }
        return result;
    }

    public static void main(String[] argv) {
        ZygoteHooks.startZygoteNoThreadCreation();
        try {
            Trace.traceBegin(16384, "ZygoteInit");
            int myPriority = Process.getThreadPriority(Process.myPid());
            Process.setThreadPriority(-19);
            RuntimeInit.enableDdms();
            SamplingProfilerIntegration.start();
            boolean startSystemServer = false;
            String socketName = "zygote";
            String abiList = null;
            for (int i = 1; i < argv.length; i++) {
                if ("start-system-server".equals(argv[i])) {
                    startSystemServer = PRELOAD_RESOURCES;
                    isPrimaryCpuAbi = PRELOAD_RESOURCES;
                } else if (argv[i].startsWith(ABI_LIST_ARG)) {
                    abiList = argv[i].substring(ABI_LIST_ARG.length());
                } else if (argv[i].startsWith(SOCKET_NAME_ARG)) {
                    socketName = argv[i].substring(SOCKET_NAME_ARG.length());
                } else {
                    throw new RuntimeException("Unknown command line argument: " + argv[i]);
                }
            }
            if (abiList == null) {
                throw new RuntimeException("No ABI list supplied.");
            }
            registerZygoteSocket(socketName);
            Trace.traceBegin(16384, "ZygotePreload");
            EventLog.writeEvent((int) LOG_BOOT_PROGRESS_PRELOAD_START, SystemClock.uptimeMillis());
            Jlog.d(28, "JL_BOOT_PROGRESS_PRELOAD_START:" + argv[ROOT_UID]);
            Log.initHWLog();
            preload();
            EventLog.writeEvent((int) LOG_BOOT_PROGRESS_PRELOAD_END, SystemClock.uptimeMillis());
            Trace.traceEnd(16384);
            Jlog.d(29, "JL_BOOT_PROGRESS_PRELOAD_END");
            SamplingProfilerIntegration.writeZygoteSnapshot();
            Trace.traceBegin(16384, "PostZygoteInitGC");
            gcAndFinalize();
            Trace.traceEnd(16384);
            Trace.traceEnd(16384);
            Trace.setTracingEnabled(false);
            Process.setThreadPriority(myPriority);
            Zygote.nativeUnmountStorageOnInit();
            ZygoteHooks.stopZygoteNoThreadCreation();
            if (startSystemServer) {
                startSystemServer(abiList, socketName);
            }
            Log.i(TAG, "Accepting command socket connections");
            runSelectLoop(abiList);
            closeServerSocket();
        } catch (MethodAndArgsCaller caller) {
            caller.run();
        } catch (RuntimeException ex) {
            Log.e(TAG, "Zygote died with exception", ex);
            closeServerSocket();
            throw ex;
        }
    }

    private static boolean hasSecondZygote(String abiList) {
        return SystemProperties.get("ro.product.cpu.abilist").equals(abiList) ? false : PRELOAD_RESOURCES;
    }

    private static void waitForSecondaryZygote(String socketName) {
        String otherZygoteName = "zygote".equals(socketName) ? "zygote_secondary" : "zygote";
        while (true) {
            try {
                ZygoteState.connect(otherZygoteName).close();
                break;
            } catch (IOException ioe) {
                Log.w(TAG, "Got error connecting to zygote, retrying. msg= " + ioe.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private static void runSelectLoop(String abiList) throws MethodAndArgsCaller {
        ArrayList<FileDescriptor> fds = new ArrayList();
        ArrayList<ZygoteConnection> peers = new ArrayList();
        fds.add(sServerSocket.getFileDescriptor());
        peers.add(null);
        while (true) {
            int i;
            StructPollfd[] pollFds = new StructPollfd[fds.size()];
            for (i = ROOT_UID; i < pollFds.length; i++) {
                pollFds[i] = new StructPollfd();
                pollFds[i].fd = (FileDescriptor) fds.get(i);
                pollFds[i].events = (short) OsConstants.POLLIN;
            }
            try {
                Os.poll(pollFds, -1);
                for (i = pollFds.length - 1; i >= 0; i--) {
                    if ((pollFds[i].revents & OsConstants.POLLIN) != 0) {
                        if (i == 0) {
                            ZygoteConnection newPeer = acceptCommandPeer(abiList);
                            peers.add(newPeer);
                            fds.add(newPeer.getFileDesciptor());
                        } else if (((ZygoteConnection) peers.get(i)).runOnce()) {
                            peers.remove(i);
                            fds.remove(i);
                        }
                    }
                }
            } catch (ErrnoException ex) {
                throw new RuntimeException("poll failed", ex);
            }
        }
    }

    private ZygoteInit() {
    }
}
