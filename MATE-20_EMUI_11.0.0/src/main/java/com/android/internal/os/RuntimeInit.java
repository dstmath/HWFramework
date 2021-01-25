package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport;
import android.ddm.DdmRegister;
import android.os.Build;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.AndroidConfig;
import com.android.server.NetworkManagementSocketTagger;
import dalvik.system.RuntimeHooks;
import dalvik.system.VMRuntime;
import java.lang.Thread;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.logging.LogManager;

public class RuntimeInit {
    static final boolean DEBUG = false;
    static final String TAG = "AndroidRuntime";
    @UnsupportedAppUsage
    private static boolean initialized;
    @UnsupportedAppUsage
    private static IBinder mApplicationObject;
    private static volatile boolean mCrashing = false;

    private static final native void nativeFinishInit();

    private static final native void nativeSetExitWithoutCleanup(boolean z);

    /* access modifiers changed from: private */
    public static int Clog_e(String tag, String msg, Throwable tr) {
        return Log.printlns(4, 6, tag, msg, tr);
    }

    /* access modifiers changed from: private */
    public static class LoggingHandler implements Thread.UncaughtExceptionHandler {
        public volatile boolean mTriggered;

        private LoggingHandler() {
            this.mTriggered = false;
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread t, Throwable e) {
            this.mTriggered = true;
            if (!RuntimeInit.mCrashing) {
                if (RuntimeInit.mApplicationObject == null && 1000 == Process.myUid()) {
                    RuntimeInit.Clog_e(RuntimeInit.TAG, "*** FATAL EXCEPTION IN SYSTEM PROCESS: " + t.getName(), e);
                    ExitCatch.disable(Process.myPid());
                    return;
                }
                StringBuilder message = new StringBuilder();
                message.append("FATAL EXCEPTION: ");
                message.append(t.getName());
                message.append("\n");
                String processName = ActivityThread.currentProcessName();
                if (processName != null) {
                    message.append("Process: ");
                    message.append(processName);
                    message.append(", ");
                }
                message.append("PID: ");
                message.append(Process.myPid());
                if (Thread.getDefaultUncaughtExceptionHandler() == null || !(Thread.getDefaultUncaughtExceptionHandler() instanceof KillApplicationHandler)) {
                    try {
                        ApplicationErrorReport.ParcelableCrashInfo crashInfo = new ApplicationErrorReport.ParcelableCrashInfo(e);
                        crashInfo.stackTrace += "-loghandler";
                        ActivityManager.getService().handleApplicationCrash(RuntimeInit.mApplicationObject, crashInfo);
                    } catch (Throwable th) {
                        Slog.e(RuntimeInit.TAG, "catch exception for handle application crash");
                    }
                }
                RuntimeInit.Clog_e(RuntimeInit.TAG, message.toString(), e);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class KillApplicationHandler implements Thread.UncaughtExceptionHandler {
        private final LoggingHandler mLoggingHandler;

        public KillApplicationHandler(LoggingHandler loggingHandler) {
            this.mLoggingHandler = (LoggingHandler) Objects.requireNonNull(loggingHandler);
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread t, Throwable e) {
            try {
                ensureLogging(t, e);
                if (RuntimeInit.mCrashing) {
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                    return;
                }
                boolean unused = RuntimeInit.mCrashing = true;
                if (ActivityThread.currentActivityThread() != null) {
                    ActivityThread.currentActivityThread().stopProfiling();
                }
                ActivityManager.getService().handleApplicationCrash(RuntimeInit.mApplicationObject, new ApplicationErrorReport.ParcelableCrashInfo(e));
                Process.killProcess(Process.myPid());
                System.exit(10);
            } catch (Throwable th) {
            }
        }

        private void ensureLogging(Thread t, Throwable e) {
            if (!this.mLoggingHandler.mTriggered) {
                try {
                    this.mLoggingHandler.uncaughtException(t, e);
                } catch (Throwable th) {
                }
            }
        }
    }

    @UnsupportedAppUsage
    protected static final void commonInit() {
        LoggingHandler loggingHandler = new LoggingHandler();
        RuntimeHooks.setUncaughtExceptionPreHandler(loggingHandler);
        Thread.setDefaultUncaughtExceptionHandler(new KillApplicationHandler(loggingHandler));
        RuntimeHooks.setTimeZoneIdSupplier($$Lambda$RuntimeInit$ep4ioD9YINkHI5Q1wZ0N_7VFAOg.INSTANCE);
        LogManager.getLogManager().reset();
        new AndroidConfig();
        System.setProperty("http.agent", getDefaultUserAgent());
        NetworkManagementSocketTagger.install();
        if (SystemProperties.get("ro.kernel.android.tracing").equals("1")) {
            Slog.i(TAG, "NOTE: emulator trace profiling enabled");
            Debug.enableEmulatorTraceOutput();
        }
        initialized = true;
    }

    /* JADX INFO: Multiple debug info for r2v4 java.lang.String: [D('model' java.lang.String), D('id' java.lang.String)] */
    private static String getDefaultUserAgent() {
        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version"));
        result.append(" (Linux; U; Android ");
        String version = Build.VERSION.RELEASE;
        result.append(version.length() > 0 ? version : "1.0");
        if ("REL".equals(Build.VERSION.CODENAME)) {
            String model = Build.MODEL;
            if (model.length() > 0) {
                result.append("; ");
                result.append(model);
            }
        }
        String id = Build.ID;
        if (id.length() > 0) {
            result.append(" Build/");
            result.append(id);
        }
        result.append(")");
        return result.toString();
    }

    protected static Runnable findStaticMain(String className, String[] argv, ClassLoader classLoader) {
        try {
            try {
                Method m = Class.forName(className, true, classLoader).getMethod("main", String[].class);
                int modifiers = m.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    return new MethodAndArgsCaller(m, argv);
                }
                throw new RuntimeException("Main method is not public and static on " + className);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Missing static main on " + className, ex);
            } catch (SecurityException ex2) {
                throw new RuntimeException("Problem getting static main on " + className, ex2);
            }
        } catch (ClassNotFoundException ex3) {
            throw new RuntimeException("Missing class when invoking static main " + className, ex3);
        }
    }

    @UnsupportedAppUsage
    public static final void main(String[] argv) {
        enableDdms();
        if (argv.length == 2 && argv[1].equals("application")) {
            redirectLogStreams();
        }
        commonInit();
        nativeFinishInit();
    }

    protected static Runnable applicationInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) {
        nativeSetExitWithoutCleanup(true);
        VMRuntime.getRuntime().setTargetHeapUtilization(0.75f);
        VMRuntime.getRuntime().setTargetSdkVersion(targetSdkVersion);
        Arguments args = new Arguments(argv);
        Trace.traceEnd(64);
        return findStaticMain(args.startClass, args.startArgs, classLoader);
    }

    public static void redirectLogStreams() {
        System.out.close();
        System.setOut(new AndroidPrintStream(4, "System.out"));
        System.err.close();
        System.setErr(new AndroidPrintStream(5, "System.err"));
    }

    public static void wtf(String tag, Throwable t, boolean system) {
        try {
            if (ActivityManager.getService().handleApplicationWtf(mApplicationObject, tag, system, new ApplicationErrorReport.ParcelableCrashInfo(t))) {
                Process.killProcess(Process.myPid());
                System.exit(10);
            }
        } catch (Throwable t2) {
            if (!(t2 instanceof DeadObjectException)) {
                Slog.e(TAG, "Error reporting WTF", t2);
                Slog.e(TAG, "Original WTF:", t);
            }
        }
    }

    public static final void setApplicationObject(IBinder app) {
        mApplicationObject = app;
    }

    @UnsupportedAppUsage
    public static final IBinder getApplicationObject() {
        return mApplicationObject;
    }

    static final void enableDdms() {
        DdmRegister.registerHandlers();
    }

    static class Arguments {
        String[] startArgs;
        String startClass;

        Arguments(String[] args) throws IllegalArgumentException {
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int curArg = 0;
            while (true) {
                if (curArg >= args.length) {
                    break;
                }
                String arg = args[curArg];
                if (arg.equals("--")) {
                    curArg++;
                    break;
                } else if (!arg.startsWith("--")) {
                    break;
                } else {
                    curArg++;
                }
            }
            if (curArg != args.length) {
                int curArg2 = curArg + 1;
                this.startClass = args[curArg];
                this.startArgs = new String[(args.length - curArg2)];
                String[] strArr = this.startArgs;
                System.arraycopy(args, curArg2, strArr, 0, strArr.length);
                return;
            }
            throw new IllegalArgumentException("Missing classname argument to RuntimeInit!");
        }
    }

    /* access modifiers changed from: package-private */
    public static class MethodAndArgsCaller implements Runnable {
        private final String[] mArgs;
        private final Method mMethod;

        public MethodAndArgsCaller(Method method, String[] args) {
            this.mMethod = method;
            this.mArgs = args;
        }

        @Override // java.lang.Runnable
        public void run() {
            try {
                this.mMethod.invoke(null, this.mArgs);
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
}
