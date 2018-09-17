package java.util.concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import sun.misc.Unsafe;

public class ForkJoinWorkerThread extends Thread {
    private static final long INHERITABLETHREADLOCALS;
    private static final long INHERITEDACCESSCONTROLCONTEXT;
    private static final long THREADLOCALS;
    private static final Unsafe U = Unsafe.getUnsafe();
    final ForkJoinPool pool;
    final WorkQueue workQueue;

    static final class InnocuousForkJoinWorkerThread extends ForkJoinWorkerThread {
        private static final AccessControlContext INNOCUOUS_ACC = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, null)});
        private static final ThreadGroup innocuousThreadGroup = createThreadGroup();

        InnocuousForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool, innocuousThreadGroup, INNOCUOUS_ACC);
        }

        void afterTopLevelExec() {
            eraseThreadLocals();
        }

        public ClassLoader getContextClassLoader() {
            return ClassLoader.getSystemClassLoader();
        }

        public void setUncaughtExceptionHandler(UncaughtExceptionHandler x) {
        }

        public void setContextClassLoader(ClassLoader cl) {
            throw new SecurityException("setContextClassLoader");
        }

        private static ThreadGroup createThreadGroup() {
            try {
                Unsafe u = Unsafe.getUnsafe();
                long tg = u.objectFieldOffset(Thread.class.getDeclaredField("group"));
                long gp = u.objectFieldOffset(ThreadGroup.class.getDeclaredField("parent"));
                ThreadGroup group = (ThreadGroup) u.getObject(Thread.currentThread(), tg);
                while (group != null) {
                    ThreadGroup parent = (ThreadGroup) u.getObject(group, gp);
                    if (parent == null) {
                        return new ThreadGroup(group, "InnocuousForkJoinWorkerThreadGroup");
                    }
                    group = parent;
                }
                throw new Error("Cannot create ThreadGroup");
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    protected ForkJoinWorkerThread(ForkJoinPool pool) {
        super("aForkJoinWorkerThread");
        this.pool = pool;
        this.workQueue = pool.registerWorker(this);
    }

    ForkJoinWorkerThread(ForkJoinPool pool, ThreadGroup threadGroup, AccessControlContext acc) {
        super(threadGroup, null, "aForkJoinWorkerThread");
        U.putOrderedObject(this, INHERITEDACCESSCONTROLCONTEXT, acc);
        eraseThreadLocals();
        this.pool = pool;
        this.workQueue = pool.registerWorker(this);
    }

    public ForkJoinPool getPool() {
        return this.pool;
    }

    public int getPoolIndex() {
        return this.workQueue.getPoolIndex();
    }

    protected void onStart() {
    }

    protected void onTermination(Throwable exception) {
    }

    public void run() {
        if (this.workQueue.array == null) {
            try {
                onStart();
                this.pool.runWorker(this.workQueue);
                try {
                    onTermination(null);
                    this.pool.deregisterWorker(this, null);
                } catch (Throwable th) {
                    this.pool.deregisterWorker(this, null);
                    throw th;
                }
            } catch (Throwable th2) {
                try {
                    onTermination(null);
                    this.pool.deregisterWorker(this, null);
                } catch (Throwable th22) {
                    this.pool.deregisterWorker(this, null);
                    throw th22;
                }
                throw th22;
            }
        }
    }

    final void eraseThreadLocals() {
        U.putObject(this, THREADLOCALS, null);
        U.putObject(this, INHERITABLETHREADLOCALS, null);
    }

    void afterTopLevelExec() {
    }

    static {
        try {
            THREADLOCALS = U.objectFieldOffset(Thread.class.getDeclaredField("threadLocals"));
            INHERITABLETHREADLOCALS = U.objectFieldOffset(Thread.class.getDeclaredField("inheritableThreadLocals"));
            INHERITEDACCESSCONTROLCONTEXT = U.objectFieldOffset(Thread.class.getDeclaredField("inheritedAccessControlContext"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
