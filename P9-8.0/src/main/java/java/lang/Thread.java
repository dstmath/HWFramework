package java.lang;

import dalvik.system.VMStack;
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
    ThreadLocalMap inheritableThreadLocals = null;
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
    ThreadLocalMap threadLocals = null;
    private Thread threadQ;
    private volatile int threadStatus = 0;
    private long tid;
    private volatile UncaughtExceptionHandler uncaughtExceptionHandler;

    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Boolean> subclassAudits = new ConcurrentHashMap();
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = new ReferenceQueue();

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
            if (referent == null) {
                z = false;
            } else if (referent != ((WeakClassKey) obj).get()) {
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
        if (millis < 0) {
            throw new IllegalArgumentException("millis < 0: " + millis);
        } else if (nanos < 0) {
            throw new IllegalArgumentException("nanos < 0: " + nanos);
        } else if (nanos > 999999) {
            throw new IllegalArgumentException("nanos > 999999: " + nanos);
        } else if (millis != 0 || nanos != 0) {
            long start = System.nanoTime();
            long duration = (1000000 * millis) + ((long) nanos);
            Object lock = currentThread().lock;
            synchronized (lock) {
                while (true) {
                    sleep(lock, millis, nanos);
                    long now = System.nanoTime();
                    long elapsed = now - start;
                    if (elapsed >= duration) {
                    } else {
                        duration -= elapsed;
                        start = now;
                        millis = duration / 1000000;
                        nanos = (int) (duration % 1000000);
                    }
                }
            }
        } else if (interrupted()) {
            throw new InterruptedException();
        }
    }

    private void init(ThreadGroup g, Runnable target, String name, long stackSize) {
        Thread parent = currentThread();
        if (g == null) {
            g = parent.getThreadGroup();
        }
        g.addUnstarted();
        this.group = g;
        this.target = target;
        this.priority = parent.getPriority();
        this.daemon = parent.isDaemon();
        setName(name);
        if (System.DEBUG && this.group.activeCount() > 300) {
            System.out.println("Group name=" + g.toString() + ", Thread Total count=" + g.activeCount() + ", This thread name=" + this.name);
            dumpStack();
        }
        init2(parent);
        this.stackSize = stackSize;
        this.tid = nextThreadID();
    }

    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public Thread() {
        init(null, null, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(Runnable target) {
        init(null, target, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(ThreadGroup group, Runnable target) {
        init(group, target, "Thread-" + nextThreadNum(), 0);
    }

    public Thread(String name) {
        init(null, null, name, 0);
    }

    public Thread(ThreadGroup group, String name) {
        init(group, null, name, 0);
    }

    Thread(ThreadGroup group, String name, int priority, boolean daemon) {
        this.group = group;
        this.group.addUnstarted();
        if (name == null) {
            name = "Thread-" + nextThreadNum();
        }
        this.name = name;
        this.priority = priority;
        this.daemon = daemon;
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

    public Thread(Runnable target, String name) {
        init(null, target, name, 0);
    }

    public Thread(ThreadGroup group, Runnable target, String name) {
        init(group, target, name, 0);
    }

    public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
        init(group, target, name, stackSize);
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
            throw new IllegalArgumentException();
        }
        ThreadGroup g = getThreadGroup();
        if (g != null) {
            if (newPriority > g.getMaxPriority()) {
                newPriority = g.getMaxPriority();
            }
            synchronized (this) {
                this.priority = newPriority;
                if (isAlive()) {
                    nativeSetPriority(newPriority);
                }
            }
        }
    }

    public final int getPriority() {
        return this.priority;
    }

    public final void setName(String name) {
        checkAccess();
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        synchronized (this) {
            this.name = name;
            if (isAlive()) {
                nativeSetName(name);
            }
        }
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
            if (millis < 0) {
                throw new IllegalArgumentException("timeout value is negative");
            }
            if (millis == 0) {
                while (isAlive()) {
                    this.lock.wait(0);
                }
            } else {
                while (isAlive()) {
                    long delay = millis - now;
                    if (delay <= 0) {
                        break;
                    }
                    this.lock.wait(delay);
                    now = System.currentTimeMillis() - base;
                }
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
        if (isAlive()) {
            throw new IllegalThreadStateException();
        }
        this.daemon = on;
    }

    public final boolean isDaemon() {
        return this.daemon;
    }

    public final void checkAccess() {
    }

    public String toString() {
        ThreadGroup group = getThreadGroup();
        if (group != null) {
            return "Thread[" + getName() + "," + getPriority() + "," + group.getName() + "]";
        }
        return "Thread[" + getName() + "," + getPriority() + "," + "" + "]";
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
        Map<Thread, StackTraceElement[]> map = new HashMap();
        int count = ThreadGroup.systemThreadGroup.activeCount();
        Thread[] threads = new Thread[((count / 2) + count)];
        count = ThreadGroup.systemThreadGroup.enumerate(threads);
        for (int i = 0; i < count; i++) {
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
        Boolean result = (Boolean) Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }
        return result.booleanValue();
    }

    private static boolean auditSubclass(final Class<?> subcl) {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                Class<?> cl = subcl;
                while (cl != Thread.class) {
                    try {
                        cl.getDeclaredMethod("getContextClassLoader", new Class[0]);
                        return Boolean.TRUE;
                    } catch (NoSuchMethodException e) {
                        try {
                            cl.getDeclaredMethod("setContextClassLoader", ClassLoader.class);
                            return Boolean.TRUE;
                        } catch (NoSuchMethodException e2) {
                            cl = cl.getSuperclass();
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

    /* JADX WARNING: Removed duplicated region for block: B:7:0x002c A:{Splitter: B:2:0x0006, ExcHandler: java.lang.RuntimeException (e java.lang.RuntimeException)} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void dispatchUncaughtException(Throwable e) {
        UncaughtExceptionHandler initialUeh = getUncaughtExceptionPreHandler();
        if (initialUeh != null) {
            try {
                initialUeh.uncaughtException(this, e);
            } catch (RuntimeException e2) {
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
            Reference<? extends Class<?>> ref = queue.poll();
            if (ref != null) {
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

    public final void parkFor$(long nanos) {
        synchronized (this.lock) {
            switch (this.parkState) {
                case 1:
                    long millis = nanos / 1000000;
                    nanos %= 1000000;
                    this.parkState = 3;
                    try {
                        this.lock.wait(millis, (int) nanos);
                        if (this.parkState == 3) {
                            this.parkState = 1;
                        }
                    } catch (InterruptedException e) {
                        interrupt();
                        if (this.parkState == 3) {
                            this.parkState = 1;
                        }
                    } catch (Throwable th) {
                        if (this.parkState == 3) {
                            this.parkState = 1;
                        }
                    }
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
