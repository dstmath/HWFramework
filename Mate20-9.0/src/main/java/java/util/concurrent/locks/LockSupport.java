package java.util.concurrent.locks;

import java.util.concurrent.ThreadLocalRandom;
import sun.misc.Unsafe;

public class LockSupport {
    private static final long PARKBLOCKER;
    private static final long SECONDARY;
    private static final Unsafe U = Unsafe.getUnsafe();

    private LockSupport() {
    }

    private static void setBlocker(Thread t, Object arg) {
        U.putObject(t, PARKBLOCKER, arg);
    }

    public static void unpark(Thread thread) {
        if (thread != null) {
            U.unpark(thread);
        }
    }

    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        U.park(false, 0);
        setBlocker(t, null);
    }

    public static void parkNanos(Object blocker, long nanos) {
        if (nanos > 0) {
            Thread t = Thread.currentThread();
            setBlocker(t, blocker);
            U.park(false, nanos);
            setBlocker(t, null);
        }
    }

    public static void parkUntil(Object blocker, long deadline) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        U.park(true, deadline);
        setBlocker(t, null);
    }

    public static Object getBlocker(Thread t) {
        if (t != null) {
            return U.getObjectVolatile(t, PARKBLOCKER);
        }
        throw new NullPointerException();
    }

    public static void park() {
        U.park(false, 0);
    }

    public static void parkNanos(long nanos) {
        if (nanos > 0) {
            U.park(false, nanos);
        }
    }

    public static void parkUntil(long deadline) {
        U.park(true, deadline);
    }

    static final int nextSecondarySeed() {
        int r;
        Thread t = Thread.currentThread();
        int i = U.getInt(t, SECONDARY);
        int r2 = i;
        if (i != 0) {
            int r3 = (r2 << 13) ^ r2;
            int r4 = r3 ^ (r3 >>> 17);
            r = r4 ^ (r4 << 5);
        } else {
            int nextInt = ThreadLocalRandom.current().nextInt();
            int r5 = nextInt;
            if (nextInt == 0) {
                r = 1;
            } else {
                r = r5;
            }
        }
        U.putInt(t, SECONDARY, r);
        return r;
    }

    static {
        try {
            PARKBLOCKER = U.objectFieldOffset(Thread.class.getDeclaredField("parkBlocker"));
            SECONDARY = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocalRandomSecondarySeed"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
