package java.lang;

import dalvik.system.VMStack;
import java.io.PrintStream;
import java.lang.ThreadLocal;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import libcore.io.Libcore;
import libcore.util.EmptyArray;
import sun.nio.ch.Interruptible;
import sun.reflect.CallerSensitive;

public class Thread implements Runnable {
    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];
    public static final int MAX_PRIORITY = 10;
    public static final int MIN_PRIORITY = 1;
    private static final int NANOS_PER_MILLI = 1000000;
    public static final int NORM_PRIORITY = 5;
    private static final RuntimePermission SUBCLASS_IMPLEMENTATION_PERMISSION = new RuntimePermission("enableContextClassLoaderOverride");
    private static final int THREAD_LIMIT = 300;
    private static volatile UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    private static int threadInitNumber;
    private static long threadSeqNumber;
    private static volatile UncaughtExceptionHandler uncaughtExceptionPreHandler;
    private volatile Interruptible blocker;
    private final Object blockerLock = new Object();
    private ClassLoader contextClassLoader;
    private boolean daemon = false;
    private long eetop;
    private ThreadGroup group;
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;
    private AccessControlContext inheritedAccessControlContext;
    private final Object lock = new Object();
    private volatile String name;
    private long nativeParkEventPointer;
    private volatile long nativePeer;
    volatile Object parkBlocker;
    private int parkState = 1;
    private int priority;
    private boolean single_step;
    private long stackSize;
    boolean started = false;
    private boolean stillborn = false;
    private Runnable target;
    int threadLocalRandomProbe;
    int threadLocalRandomSecondarySeed;
    long threadLocalRandomSeed;
    ThreadLocal.ThreadLocalMap threadLocals = null;
    private Thread threadQ;
    private volatile int threadStatus = 0;
    private long tid;
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Boolean> subclassAudits = new ConcurrentHashMap();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = new ReferenceQueue<>();

        private Caches() {
        }
    }

    private static class ParkState {
        private static final int PARKED = 3;
        private static final int PREEMPTIVELY_UNPARKED = 2;
        private static final int UNPARKED = 1;

        private ParkState() {
        }
    }

    public enum State {
        NEW,
        RUNNABLE,
        BLOCKED,
        WAITING,
        TIMED_WAITING,
        TERMINATED
    }

    @FunctionalInterface
    public interface UncaughtExceptionHandler {
        void uncaughtException(Thread thread, Throwable th);
    }

    static class WeakClassKey extends WeakReference<Class<?>> {
        private final int hash;

        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            this.hash = System.identityHashCode(cl);
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof WeakClassKey)) {
                return false;
            }
            Object referent = get();
            if (referent == null || referent != ((WeakClassKey) obj).get()) {
                z = false;
            }
            return z;
        }
    }

    public static native Thread currentThread();

    public static native int getLockOwnerThreadId(Object obj);

    public static native boolean interrupted();

    private static native void nativeCreate(Thread thread, long j, boolean z);

    private native int nativeGetStatus(boolean z);

    private native boolean nativeHoldsLock(Object obj);

    private native void nativeInterrupt();

    private native void nativeSetName(String str);

    private native void nativeSetPriority(int i);

    private static native void sleep(Object obj, long j, int i) throws InterruptedException;

    public static native void yield();

    public native boolean isInterrupted();

    private static synchronized int nextThreadNum() {
        int i;
        synchronized (Thread.class) {
            i = threadInitNumber;
            threadInitNumber = i + 1;
        }
        return i;
    }

    private static synchronized long nextThreadID() {
        long j;
        synchronized (Thread.class) {
            j = threadSeqNumber + 1;
            threadSeqNumber = j;
        }
        return j;
    }

    public void blockedOn(Interruptible b) {
        synchronized (this.blockerLock) {
            this.blocker = b;
        }
    }

    public static void sleep(long millis) throws InterruptedException {
        sleep(millis, 0);
    }

    public static void sleep(long millis, int nanos) throws InterruptedException {
        long j = millis;
        int i = nanos;
        if (j < 0) {
            throw new IllegalArgumentException("millis < 0: " + j);
        } else if (i < 0) {
            throw new IllegalArgumentException("nanos < 0: " + i);
        } else if (i > 999999) {
            throw new IllegalArgumentException("nanos > 999999: " + i);
        } else if (j != 0 || i != 0) {
            long start = System.nanoTime();
            long duration = (j * 1000000) + ((long) i);
            Object lock2 = currentThread().lock;
            synchronized (lock2) {
                long duration2 = duration;
                long start2 = start;
                int nanos2 = i;
                long millis2 = j;
                while (true) {
                    try {
                        sleep(lock2, millis2, nanos2);
                        long now = System.nanoTime();
                        long elapsed = now - start2;
                        if (elapsed < duration2) {
                            duration2 -= elapsed;
                            start2 = now;
                            long millis3 = duration2 / 1000000;
                            try {
                                nanos2 = (int) (duration2 % 1000000);
                                millis2 = millis3;
                            } catch (Throwable th) {
                                th = th;
                                long j2 = millis3;
                                throw th;
                            }
                        } else {
                            return;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            }
        } else if (interrupted()) {
            throw new InterruptedException();
        }
    }

    private void init(ThreadGroup g, Runnable target2, String name2, long stackSize2) {
        Thread parent = currentThread();
        if (g == null) {
            g = parent.getThreadGroup();
        }
        g.addUnstarted();
        this.group = g;
        this.target = target2;
        this.priority = parent.getPriority();
        this.daemon = parent.isDaemon();
        setName(name2);
        if (System.DEBUG && this.group.activeCount() > 300) {
            PrintStream printStream = System.out;
            printStream.println("Group name=" + g.toString() + ", Thread Total count=" + g.activeCount() + ", This thread name=" + this.name);
            dumpStack();
        }
        init2(parent);
        this.stackSize = stackSize2;
        this.tid = nextThreadID();
    }

    /* access modifiers changed from: protected */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(Runnable target2) {
        init(null, target2, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(ThreadGroup group2, Runnable target2) {
        init(group2, target2, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(String name2) {
        init(null, null, name2, 0);
    }

    public Thread(ThreadGroup group2, String name2) {
        init(group2, null, name2, 0);
    }

    Thread(ThreadGroup group2, String name2, int priority2, boolean daemon2) {
        this.group = group2;
        this.group.addUnstarted();
        if (name2 == null) {
            name2 = "Thread-" + nextThreadNum();
        }
        this.name = name2;
        this.priority = priority2;
        this.daemon = daemon2;
        init2(currentThread());
        this.tid = nextThreadID();
    }

    private void init2(Thread parent) {
        this.contextClassLoader = parent.getContextClassLoader();
        this.inheritedAccessControlContext = AccessController.getContext();
        if (parent.inheritableThreadLocals != null) {
            this.inheritableThreadLocals = ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
        }
    }

    public Thread(Runnable target2, String name2) {
        init(null, target2, name2, 0);
    }

    public Thread(ThreadGroup group2, Runnable target2, String name2) {
        init(group2, target2, name2, 0);
    }

    public Thread(ThreadGroup group2, Runnable target2, String name2, long stackSize2) {
        init(group2, target2, name2, stackSize2);
    }

    public synchronized void start() {
        if (this.threadStatus != 0 || this.started) {
            throw new IllegalThreadStateException();
        }
        this.group.add(this);
        this.started = false;
        try {
            nativeCreate(this, this.stackSize, this.daemon);
            this.started = true;
            try {
                if (!this.started) {
                    this.group.threadStartFailed(this);
                }
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
        }
        return;
        throw th;
    }

    public void run() {
        if (this.target != null) {
            this.target.run();
        }
    }

    private void exit() {
        if (this.group != null) {
            this.group.threadTerminated(this);
            this.group = null;
        }
        this.target = null;
        this.threadLocals = null;
        this.inheritableThreadLocals = null;
        this.inheritedAccessControlContext = null;
        this.blocker = null;
        this.uncaughtExceptionHandler = null;
    }

    @Deprecated
    public final void stop() {
        stop(new ThreadDeath());
    }

    @Deprecated
    public final void stop(Throwable obj) {
        throw new UnsupportedOperationException();
    }

    public void interrupt() {
        if (this != currentThread()) {
            checkAccess();
        }
        synchronized (this.blockerLock) {
            Interruptible b = this.blocker;
            if (b != null) {
                nativeInterrupt();
                b.interrupt(this);
                return;
            }
            nativeInterrupt();
        }
    }

    @Deprecated
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    public final boolean isAlive() {
        return this.nativePeer != 0;
    }

    @Deprecated
    public final void suspend() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public final void resume() {
        throw new UnsupportedOperationException();
    }

    public final void setPriority(int newPriority) {
        checkAccess();
        if (newPriority > 10 || newPriority < 1) {
            throw new IllegalArgumentException("Priority out of range: " + newPriority);
        }
        ThreadGroup threadGroup = getThreadGroup();
        ThreadGroup g = threadGroup;
        if (threadGroup != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }
            int newPriority2 = newPriority;
            synchronized (this) {
                this.priority = newPriority2;
                if (isAlive()) {
                    nativeSetPriority(newPriority2);
                }
            }
            int i = newPriority2;
        }
    }

    public final int getPriority() {
        return this.priority;
    }

    public final void setName(String name2) {
        checkAccess();
        if (name2 != null) {
            synchronized (this) {
                this.name = name2;
                if (isAlive()) {
                    nativeSetName(name2);
                }
            }
            return;
        }
        throw new NullPointerException("name == null");
    }

    public final String getName() {
        return this.name;
    }

    public final ThreadGroup getThreadGroup() {
        if (getState() == State.TERMINATED) {
            return null;
        }
        return this.group;
    }

    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    public static int enumerate(Thread[] tarray) {
        return currentThread().getThreadGroup().enumerate(tarray);
    }

    @Deprecated
    public int countStackFrames() {
        return getStackTrace().length;
    }

    public final void join(long millis) throws InterruptedException {
        synchronized (this.lock) {
            long base = System.currentTimeMillis();
            long now = 0;
            if (millis >= 0) {
                if (millis != 0) {
                    while (true) {
                        if (!isAlive()) {
                            break;
                        }
                        long delay = millis - now;
                        if (delay <= 0) {
                            break;
                        }
                        this.lock.wait(delay);
                        now = System.currentTimeMillis() - base;
                    }
                } else {
                    while (isAlive()) {
                        this.lock.wait(0);
                    }
                }
            } else {
                throw new IllegalArgumentException("timeout value is negative");
            }
        }
    }

    public final void join(long millis, int nanos) throws InterruptedException {
        synchronized (this.lock) {
            if (millis < 0) {
                throw new IllegalArgumentException("timeout value is negative");
            } else if (nanos < 0 || nanos > 999999) {
                throw new IllegalArgumentException("nanosecond timeout value out of range");
            } else {
                if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
                    millis++;
                }
                join(millis);
            }
        }
    }

    public final void join() throws InterruptedException {
        join(0);
    }

    public static void dumpStack() {
        new Exception("Stack trace").printStackTrace();
    }

    public final void setDaemon(boolean on) {
        checkAccess();
        if (!isAlive()) {
            this.daemon = on;
            return;
        }
        throw new IllegalThreadStateException();
    }

    public final boolean isDaemon() {
        return this.daemon;
    }

    public final void checkAccess() {
    }

    public String toString() {
        ThreadGroup group2 = getThreadGroup();
        if (group2 != null) {
            return "Thread[" + getName() + "," + getPriority() + "," + group2.getName() + "]";
        }
        return "Thread[" + getName() + "," + getPriority() + ",]";
    }

    @CallerSensitive
    public ClassLoader getContextClassLoader() {
        return this.contextClassLoader;
    }

    public void setContextClassLoader(ClassLoader cl) {
        this.contextClassLoader = cl;
    }

    public static boolean holdsLock(Object obj) {
        return currentThread().nativeHoldsLock(obj);
    }

    public StackTraceElement[] getStackTrace() {
        StackTraceElement[] ste = VMStack.getThreadStackTrace(this);
        return ste != null ? ste : EmptyArray.STACK_TRACE_ELEMENT;
    }

    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        Map<Thread, StackTraceElement[]> map = new HashMap<>();
        int count = ThreadGroup.systemThreadGroup.activeCount();
        Thread[] threads = new Thread[((count / 2) + count)];
        int count2 = ThreadGroup.systemThreadGroup.enumerate(threads);
        for (int i = 0; i < count2; i++) {
            map.put(threads[i], threads[i].getStackTrace());
        }
        return map;
    }

    private static boolean isCCLOverridden(Class<?> cl) {
        if (cl == Thread.class) {
            return false;
        }
        processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
        Boolean result = Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }
        return result.booleanValue();
    }

    private static boolean auditSubclass(final Class<?> subcl) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                Class<? super Thread> cls = Class.this;
                while (cls != Thread.class) {
                    try {
                        cls.getDeclaredMethod("getContextClassLoader", new Class[0]);
                        return Boolean.TRUE;
                    } catch (NoSuchMethodException e) {
                        try {
                            cls.getDeclaredMethod("setContextClassLoader", ClassLoader.class);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException e2) {
                            cls = cls.getSuperclass();
                        }
                    }
                }
                return Boolean.FALSE;
            }
        })).booleanValue();
    }

    public long getId() {
        return this.tid;
    }

    public State getState() {
        return State.values()[nativeGetStatus(this.started)];
    }

    public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        defaultUncaughtExceptionHandler = eh;
    }

    public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
        return defaultUncaughtExceptionHandler;
    }

    public static void setUncaughtExceptionPreHandler(UncaughtExceptionHandler eh) {
        uncaughtExceptionPreHandler = eh;
    }

    public static UncaughtExceptionHandler getUncaughtExceptionPreHandler() {
        return uncaughtExceptionPreHandler;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.uncaughtExceptionHandler != null ? this.uncaughtExceptionHandler : this.group;
    }

    public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        checkAccess();
        this.uncaughtExceptionHandler = eh;
    }

    public final void dispatchUncaughtException(Throwable e) {
        UncaughtExceptionHandler initialUeh = getUncaughtExceptionPreHandler();
        if (initialUeh != null) {
            try {
                initialUeh.uncaughtException(this, e);
            } catch (Error | RuntimeException e2) {
            }
        }
        getUncaughtExceptionHandler().uncaughtException(this, e);
        if (Libcore.os.getpid() == Libcore.os.gettid()) {
            System.err.println("Exit due to uncaughtException in main thread:");
            System.exit(10);
        }
    }

    static void processQueue(ReferenceQueue<Class<?>> queue, ConcurrentMap<? extends WeakReference<Class<?>>, ?> map) {
        while (true) {
            Reference<? extends Class<?>> poll = queue.poll();
            Reference<? extends Class<?>> ref = poll;
            if (poll != null) {
                map.remove(ref);
            } else {
                return;
            }
        }
    }

    public final void unpark$() {
        synchronized (this.lock) {
            switch (this.parkState) {
                case 1:
                    this.parkState = 2;
                    break;
                case 2:
                    break;
                default:
                    this.parkState = 1;
                    this.lock.notifyAll();
                    break;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0022, code lost:
        if (r7.parkState == 3) goto L_0x0024;
     */
    public final void parkFor$(long nanos) {
        synchronized (this.lock) {
            switch (this.parkState) {
                case 1:
                    long millis = nanos / 1000000;
                    long nanos2 = nanos % 1000000;
                    this.parkState = 3;
                    try {
                        this.lock.wait(millis, (int) nanos2);
                        break;
                    } catch (InterruptedException e) {
                        try {
                            interrupt();
                            if (this.parkState == 3) {
                                this.parkState = 1;
                                break;
                            }
                        } catch (Throwable th) {
                            if (this.parkState == 3) {
                                this.parkState = 1;
                            }
                            throw th;
                        }
                    }
                    break;
                case 2:
                    this.parkState = 1;
                    break;
                default:
                    throw new AssertionError((Object) "Attempt to repark");
            }
        }
    }

    public final void parkUntil$(long time) {
        synchronized (this.lock) {
            long currentTime = System.currentTimeMillis();
            if (time <= currentTime) {
                this.parkState = 1;
            } else {
                long delayMillis = time - currentTime;
                if (delayMillis > 9223372036854L) {
                    delayMillis = 9223372036854L;
                }
                parkFor$(1000000 * delayMillis);
            }
        }
    }
}
