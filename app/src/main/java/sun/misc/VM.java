package sun.misc;

import java.lang.Thread.State;
import java.util.Properties;
import java.util.jar.Pack200.Unpacker;

public class VM {
    private static final int JVMTI_THREAD_STATE_ALIVE = 1;
    private static final int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 1024;
    private static final int JVMTI_THREAD_STATE_RUNNABLE = 4;
    private static final int JVMTI_THREAD_STATE_TERMINATED = 2;
    private static final int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 16;
    private static final int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 32;
    @Deprecated
    public static final int STATE_GREEN = 1;
    @Deprecated
    public static final int STATE_RED = 3;
    @Deprecated
    public static final int STATE_YELLOW = 2;
    private static boolean allowArraySyntax;
    private static boolean allowGetCallerClass;
    private static volatile boolean booted;
    private static boolean defaultAllowArraySyntax;
    private static long directMemory;
    private static volatile int finalRefCount;
    private static boolean pageAlignDirectMemory;
    private static volatile int peakFinalRefCount;
    private static final Properties savedProps = null;
    private static boolean suspended;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.misc.VM.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.misc.VM.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.misc.VM.<clinit>():void");
    }

    @Deprecated
    public static boolean threadsSuspended() {
        return suspended;
    }

    public static boolean allowThreadSuspension(ThreadGroup g, boolean b) {
        return g.allowThreadSuspension(b);
    }

    @Deprecated
    public static boolean suspendThreads() {
        suspended = true;
        return true;
    }

    @Deprecated
    public static void unsuspendThreads() {
        suspended = false;
    }

    @Deprecated
    public static void unsuspendSomeThreads() {
    }

    @Deprecated
    public static final int getState() {
        return STATE_GREEN;
    }

    @Deprecated
    public static void asChange(int as_old, int as_new) {
    }

    @Deprecated
    public static void asChange_otherthread(int as_old, int as_new) {
    }

    public static void booted() {
        booted = true;
    }

    public static boolean isBooted() {
        return booted;
    }

    public static long maxDirectMemory() {
        return directMemory;
    }

    public static boolean isDirectMemoryPageAligned() {
        return pageAlignDirectMemory;
    }

    public static boolean allowArraySyntax() {
        return allowArraySyntax;
    }

    public static boolean allowGetCallerClass() {
        return allowGetCallerClass;
    }

    public static String getSavedProperty(String key) {
        return savedProps.getProperty(key);
    }

    public static void saveAndRemoveProperties(Properties props) {
        if (booted) {
            throw new IllegalStateException("System initialization has completed");
        }
        boolean z;
        savedProps.putAll(props);
        String s = (String) props.remove("sun.nio.MaxDirectMemorySize");
        if (s != null) {
            if (s.equals("-1")) {
                directMemory = Runtime.getRuntime().maxMemory();
            } else {
                long l = Long.parseLong(s);
                if (l > -1) {
                    directMemory = l;
                }
            }
        }
        if (Unpacker.TRUE.equals((String) props.remove("sun.nio.PageAlignDirectMemory"))) {
            pageAlignDirectMemory = true;
        }
        s = props.getProperty("sun.lang.ClassLoader.allowArraySyntax");
        if (s == null) {
            z = defaultAllowArraySyntax;
        } else {
            z = Boolean.parseBoolean(s);
        }
        allowArraySyntax = z;
        s = props.getProperty("jdk.reflect.allowGetCallerClass");
        if (s == null || s.isEmpty() || Boolean.parseBoolean(s)) {
            z = true;
        } else {
            z = Boolean.valueOf(props.getProperty("jdk.logging.allowStackWalkSearch")).booleanValue();
        }
        allowGetCallerClass = z;
        props.remove("java.lang.Integer.IntegerCache.high");
        props.remove("sun.zip.disableMemoryMapping");
        props.remove("sun.java.launcher.diag");
    }

    public static void initializeOSEnvironment() {
    }

    public static int getFinalRefCount() {
        return finalRefCount;
    }

    public static int getPeakFinalRefCount() {
        return peakFinalRefCount;
    }

    public static void addFinalRefCount(int n) {
        finalRefCount += n;
        if (finalRefCount > peakFinalRefCount) {
            peakFinalRefCount = finalRefCount;
        }
    }

    public static State toThreadState(int threadStatus) {
        if ((threadStatus & JVMTI_THREAD_STATE_RUNNABLE) != 0) {
            return State.RUNNABLE;
        }
        if ((threadStatus & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) != 0) {
            return State.BLOCKED;
        }
        if ((threadStatus & JVMTI_THREAD_STATE_WAITING_INDEFINITELY) != 0) {
            return State.WAITING;
        }
        if ((threadStatus & JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT) != 0) {
            return State.TIMED_WAITING;
        }
        if ((threadStatus & STATE_YELLOW) != 0) {
            return State.TERMINATED;
        }
        if ((threadStatus & STATE_GREEN) == 0) {
            return State.NEW;
        }
        return State.RUNNABLE;
    }
}
