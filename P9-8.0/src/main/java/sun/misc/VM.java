package sun.misc;

import java.lang.Thread.State;
import java.util.Properties;

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
    private static boolean allowArraySyntax = defaultAllowArraySyntax;
    private static boolean allowGetCallerClass = true;
    private static volatile boolean booted = false;
    private static boolean defaultAllowArraySyntax = false;
    private static long directMemory = 67108864;
    private static volatile int finalRefCount = 0;
    private static boolean pageAlignDirectMemory;
    private static volatile int peakFinalRefCount = 0;
    private static final Properties savedProps = new Properties();
    private static boolean suspended = false;

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
        return 1;
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
        if ("true".equals((String) props.remove("sun.nio.PageAlignDirectMemory"))) {
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
            z = Boolean.parseBoolean(props.getProperty("jdk.logging.allowStackWalkSearch"));
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
        if ((threadStatus & 4) != 0) {
            return State.RUNNABLE;
        }
        if ((threadStatus & 1024) != 0) {
            return State.BLOCKED;
        }
        if ((threadStatus & 16) != 0) {
            return State.WAITING;
        }
        if ((threadStatus & 32) != 0) {
            return State.TIMED_WAITING;
        }
        if ((threadStatus & 2) != 0) {
            return State.TERMINATED;
        }
        if ((threadStatus & 1) == 0) {
            return State.NEW;
        }
        return State.RUNNABLE;
    }
}
