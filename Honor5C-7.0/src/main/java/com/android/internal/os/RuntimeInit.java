package com.android.internal.os;

import android.app.ActivityManagerNative;
import android.app.ActivityThread;
import android.app.ApplicationErrorReport.CrashInfo;
import android.ddm.DdmRegister;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.DeadObjectException;
import android.os.Debug;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.util.Slog;
import com.android.internal.logging.AndroidConfig;
import com.android.internal.os.ZygoteInit.MethodAndArgsCaller;
import com.android.server.NetworkManagementSocketTagger;
import dalvik.system.VMRuntime;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.TimeZone;
import java.util.logging.LogManager;
import org.apache.harmony.luni.internal.util.TimezoneGetter;

public class RuntimeInit {
    private static final boolean DEBUG = false;
    private static final String TAG = "AndroidRuntime";
    private static boolean initialized;
    private static IBinder mApplicationObject;
    private static volatile boolean mCrashing;

    static class Arguments {
        String[] startArgs;
        String startClass;

        Arguments(String[] args) throws IllegalArgumentException {
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int curArg = 0;
            while (curArg < args.length) {
                String arg = args[curArg];
                if (!arg.equals("--")) {
                    if (!arg.startsWith("--")) {
                        break;
                    }
                    curArg++;
                } else {
                    curArg++;
                    break;
                }
            }
            if (curArg == args.length) {
                throw new IllegalArgumentException("Missing classname argument to RuntimeInit!");
            }
            int curArg2 = curArg + 1;
            this.startClass = args[curArg];
            this.startArgs = new String[(args.length - curArg2)];
            System.arraycopy(args, curArg2, this.startArgs, 0, this.startArgs.length);
        }
    }

    private static class UncaughtHandler implements UncaughtExceptionHandler {
        private UncaughtHandler() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void uncaughtException(Thread t, Throwable e) {
            try {
                if (RuntimeInit.mCrashing) {
                    Process.killProcess(Process.myPid());
                    System.exit(10);
                    return;
                }
                RuntimeInit.mCrashing = true;
                if (RuntimeInit.mApplicationObject == null) {
                    RuntimeInit.Clog_e(RuntimeInit.TAG, "*** FATAL EXCEPTION IN SYSTEM PROCESS: " + t.getName(), e);
                } else {
                    StringBuilder message = new StringBuilder();
                    message.append("FATAL EXCEPTION: ").append(t.getName()).append("\n");
                    String processName = ActivityThread.currentProcessName();
                    if (processName != null) {
                        message.append("Process: ").append(processName).append(", ");
                    }
                    message.append("PID: ").append(Process.myPid());
                    RuntimeInit.Clog_e(RuntimeInit.TAG, message.toString(), e);
                }
                if (ActivityThread.currentActivityThread() != null) {
                    ActivityThread.currentActivityThread().stopProfiling();
                }
                ActivityManagerNative.getDefault().handleApplicationCrash(RuntimeInit.mApplicationObject, new CrashInfo(e));
                Process.killProcess(Process.myPid());
                System.exit(10);
            } catch (Throwable th) {
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.os.RuntimeInit.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.os.RuntimeInit.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.RuntimeInit.<clinit>():void");
    }

    private static final native void nativeFinishInit();

    private static final native void nativeSetExitWithoutCleanup(boolean z);

    private static final native void nativeZygoteInit();

    private static int Clog_e(String tag, String msg, Throwable tr) {
        return Log.printlns(4, 6, tag, msg, tr);
    }

    private static final void commonInit() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtHandler());
        TimezoneGetter.setInstance(new TimezoneGetter() {
            public String getId() {
                return SystemProperties.get("persist.sys.timezone");
            }
        });
        TimeZone.setDefault(null);
        LogManager.getLogManager().reset();
        AndroidConfig androidConfig = new AndroidConfig();
        System.setProperty("http.agent", getDefaultUserAgent());
        NetworkManagementSocketTagger.install();
        if (SystemProperties.get("ro.kernel.android.tracing").equals("1")) {
            Slog.i(TAG, "NOTE: emulator trace profiling enabled");
            Debug.enableEmulatorTraceOutput();
        }
        initialized = true;
    }

    private static String getDefaultUserAgent() {
        StringBuilder result = new StringBuilder(64);
        result.append("Dalvik/");
        result.append(System.getProperty("java.vm.version"));
        result.append(" (Linux; U; Android ");
        String version = VERSION.RELEASE;
        if (version.length() <= 0) {
            version = "1.0";
        }
        result.append(version);
        if ("REL".equals(VERSION.CODENAME)) {
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

    private static void invokeStaticMain(String className, String[] argv, ClassLoader classLoader) throws MethodAndArgsCaller {
        boolean z = DEBUG;
        try {
            try {
                Method m = Class.forName(className, true, classLoader).getMethod("main", new Class[]{String[].class});
                int modifiers = m.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    z = Modifier.isPublic(modifiers);
                }
                if (z) {
                    throw new MethodAndArgsCaller(m, argv);
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

    public static final void main(String[] argv) {
        enableDdms();
        if (argv.length == 2 && argv[1].equals("application")) {
            redirectLogStreams();
        }
        commonInit();
        nativeFinishInit();
    }

    public static final void zygoteInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) throws MethodAndArgsCaller {
        Trace.traceBegin(64, "RuntimeInit");
        redirectLogStreams();
        commonInit();
        nativeZygoteInit();
        applicationInit(targetSdkVersion, argv, classLoader);
    }

    public static void wrapperInit(int targetSdkVersion, String[] argv) throws MethodAndArgsCaller {
        applicationInit(targetSdkVersion, argv, null);
    }

    private static void applicationInit(int targetSdkVersion, String[] argv, ClassLoader classLoader) throws MethodAndArgsCaller {
        nativeSetExitWithoutCleanup(true);
        VMRuntime.getRuntime().setTargetHeapUtilization(0.75f);
        VMRuntime.getRuntime().setTargetSdkVersion(targetSdkVersion);
        try {
            Arguments args = new Arguments(argv);
            Trace.traceEnd(64);
            invokeStaticMain(args.startClass, args.startArgs, classLoader);
        } catch (IllegalArgumentException ex) {
            Slog.e(TAG, ex.getMessage());
        }
    }

    public static void redirectLogStreams() {
        System.out.close();
        System.setOut(new AndroidPrintStream(4, "System.out"));
        System.err.close();
        System.setErr(new AndroidPrintStream(5, "System.err"));
    }

    public static void wtf(String tag, Throwable t, boolean system) {
        try {
            if (ActivityManagerNative.getDefault().handleApplicationWtf(mApplicationObject, tag, system, new CrashInfo(t))) {
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

    public static final IBinder getApplicationObject() {
        return mApplicationObject;
    }

    static final void enableDdms() {
        DdmRegister.registerHandlers();
    }
}
