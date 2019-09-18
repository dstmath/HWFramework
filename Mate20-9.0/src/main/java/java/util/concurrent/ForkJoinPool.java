package java.util.concurrent;

import java.lang.Thread;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import sun.misc.Unsafe;

public class ForkJoinPool extends AbstractExecutorService {
    private static final int ABASE;
    private static final long AC_MASK = -281474976710656L;
    private static final int AC_SHIFT = 48;
    private static final long AC_UNIT = 281474976710656L;
    private static final long ADD_WORKER = 140737488355328L;
    private static final int ASHIFT;
    private static final int COMMON_MAX_SPARES;
    static final int COMMON_PARALLELISM = Math.max(common.config & SMASK, 1);
    private static final long CTL;
    private static final int DEFAULT_COMMON_MAX_SPARES = 256;
    static final int EVENMASK = 65534;
    static final int FIFO_QUEUE = Integer.MIN_VALUE;
    private static final long IDLE_TIMEOUT_MS = 2000;
    static final int IS_OWNED = 1;
    static final int LIFO_QUEUE = 0;
    static final int MAX_CAP = 32767;
    static final int MODE_MASK = -65536;
    static final int POLL_LIMIT = 1023;
    private static final long RUNSTATE;
    private static final int SEED_INCREMENT = -1640531527;
    private static final int SHUTDOWN = Integer.MIN_VALUE;
    static final int SMASK = 65535;
    static final int SPARE_WORKER = 131072;
    private static final long SP_MASK = 4294967295L;
    static final int SQMASK = 126;
    static final int SS_SEQ = 65536;
    private static final int STARTED = 1;
    private static final int STOP = 2;
    private static final long TC_MASK = 281470681743360L;
    private static final int TC_SHIFT = 32;
    private static final long TC_UNIT = 4294967296L;
    private static final int TERMINATED = 4;
    private static final long TIMEOUT_SLOP_MS = 20;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long UC_MASK = -4294967296L;
    static final int UNREGISTERED = 262144;
    static final int UNSIGNALLED = Integer.MIN_VALUE;
    static final ForkJoinPool common = ((ForkJoinPool) AccessController.doPrivileged(new PrivilegedAction<ForkJoinPool>() {
        public ForkJoinPool run() {
            return ForkJoinPool.makeCommonPool();
        }
    }));
    public static final ForkJoinWorkerThreadFactory defaultForkJoinWorkerThreadFactory = new DefaultForkJoinWorkerThreadFactory();
    static final RuntimePermission modifyThreadPermission = new RuntimePermission("modifyThread");
    private static int poolNumberSequence;
    AuxState auxState;
    final int config;
    volatile long ctl;
    final ForkJoinWorkerThreadFactory factory;
    volatile int runState;
    final Thread.UncaughtExceptionHandler ueh;
    volatile WorkQueue[] workQueues;
    final String workerNamePrefix;

    private static final class AuxState extends ReentrantLock {
        private static final long serialVersionUID = -6001602636862214147L;
        long indexSeed;
        volatile long stealCount;

        AuxState() {
        }
    }

    private static final class DefaultForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
        private DefaultForkJoinWorkerThreadFactory() {
        }

        public final ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            return new ForkJoinWorkerThread(pool);
        }
    }

    private static final class EmptyTask extends ForkJoinTask<Void> {
        private static final long serialVersionUID = -7721805057305804111L;

        EmptyTask() {
            this.status = -268435456;
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void x) {
        }

        public final boolean exec() {
            return true;
        }
    }

    public interface ForkJoinWorkerThreadFactory {
        ForkJoinWorkerThread newThread(
/*
Method generation error in method: java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory.newThread(java.util.concurrent.ForkJoinPool):java.util.concurrent.ForkJoinWorkerThread, dex: boot_classes.dex
        jadx.core.utils.exceptions.JadxRuntimeException: Code variable not set in r1v0 ?
        	at jadx.core.dex.instructions.args.SSAVar.getCodeVar(SSAVar.java:189)
        	at jadx.core.codegen.MethodGen.addMethodArguments(MethodGen.java:157)
        	at jadx.core.codegen.MethodGen.addDefinition(MethodGen.java:129)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:297)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:49)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        
*/
    }

    private static final class InnocuousForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {
        private static final AccessControlContext innocuousAcc;

        private InnocuousForkJoinWorkerThreadFactory() {
        }

        static {
            Permissions innocuousPerms = new Permissions();
            innocuousPerms.add(ForkJoinPool.modifyThreadPermission);
            innocuousPerms.add(new RuntimePermission("enableContextClassLoaderOverride"));
            innocuousPerms.add(new RuntimePermission("modifyThreadGroup"));
            innocuousAcc = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, innocuousPerms)});
        }

        public final ForkJoinWorkerThread newThread(final ForkJoinPool pool) {
            return (ForkJoinWorkerThread) AccessController.doPrivileged(new PrivilegedAction<ForkJoinWorkerThread>() {
                public ForkJoinWorkerThread run() {
                    return new ForkJoinWorkerThread.InnocuousForkJoinWorkerThread(pool);
                }
            }, innocuousAcc);
        }
    }

    public interface ManagedBlocker {
        boolean block() throws InterruptedException;

        boolean isReleasable();
    }

    static final class WorkQueue {
        private static final int ABASE;
        private static final int ASHIFT;
        static final int INITIAL_QUEUE_CAPACITY = 8192;
        static final int MAXIMUM_QUEUE_CAPACITY = 67108864;
        private static final long QLOCK;
        private static final Unsafe U = Unsafe.getUnsafe();
        ForkJoinTask<?>[] array;
        volatile int base = 4096;
        int config;
        volatile ForkJoinTask<?> currentJoin;
        volatile ForkJoinTask<?> currentSteal;
        int hint;
        int nsteals;
        final ForkJoinWorkerThread owner;
        volatile Thread parker;
        final ForkJoinPool pool;
        volatile int qlock;
        volatile int scanState;
        int stackPred;
        int top = 4096;

        WorkQueue(ForkJoinPool pool2, ForkJoinWorkerThread owner2) {
            this.pool = pool2;
            this.owner = owner2;
        }

        /* access modifiers changed from: package-private */
        public final int getPoolIndex() {
            return (this.config & ForkJoinPool.SMASK) >>> 1;
        }

        /* access modifiers changed from: package-private */
        public final int queueSize() {
            int n = this.base - this.top;
            if (n >= 0) {
                return 0;
            }
            return -n;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
            if (r3[(r4 - 1) & (r2 - 1)] != null) goto L_0x001f;
         */
        public final boolean isEmpty() {
            int i = this.base;
            int i2 = this.top;
            int s = i2;
            int i3 = i - i2;
            int n = i3;
            if (i3 < 0) {
                if (n == -1) {
                    ForkJoinTask<?>[] forkJoinTaskArr = this.array;
                    ForkJoinTask<?>[] a = forkJoinTaskArr;
                    if (forkJoinTaskArr != null) {
                        int length = a.length;
                        int al = length;
                        if (length != 0) {
                        }
                    }
                }
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public final void push(ForkJoinTask<?> task) {
            U.storeFence();
            int s = this.top;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (forkJoinTaskArr != null) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    a[(al - 1) & s] = task;
                    this.top = s + 1;
                    ForkJoinPool p = this.pool;
                    int i = this.base - s;
                    int d = i;
                    if (i == 0 && p != null) {
                        U.fullFence();
                        p.signalWork();
                    } else if (al + d == 1) {
                        growArray();
                    }
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v13, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v0, resolved type: java.util.concurrent.ForkJoinTask<?>} */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public final ForkJoinTask<?>[] growArray() {
            ForkJoinTask<?>[] oldA = this.array;
            int size = oldA != null ? oldA.length << 1 : 8192;
            if (size < 8192 || size > MAXIMUM_QUEUE_CAPACITY) {
                throw new RejectedExecutionException("Queue capacity exceeded");
            }
            ForkJoinTask<?>[] forkJoinTaskArr = new ForkJoinTask[size];
            this.array = forkJoinTaskArr;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (oldA != null) {
                int length = oldA.length - 1;
                int oldMask = length;
                if (length > 0) {
                    int i = this.top;
                    int t = i;
                    int i2 = this.base;
                    int b = i2;
                    if (i - i2 > 0) {
                        int mask = size - 1;
                        int b2 = b;
                        while (true) {
                            int mask2 = mask;
                            long offset = (((long) (b2 & oldMask)) << ASHIFT) + ((long) ABASE);
                            ForkJoinTask<?> x = U.getObjectVolatile(oldA, offset);
                            if (x != null) {
                                long j = offset;
                                if (U.compareAndSwapObject(oldA, offset, x, null)) {
                                    a[b2 & mask2] = x;
                                }
                            }
                            b2++;
                            if (b2 == t) {
                                break;
                            }
                            mask = mask2;
                        }
                        U.storeFence();
                    }
                }
            }
            return a;
        }

        /* access modifiers changed from: package-private */
        public final ForkJoinTask<?> pop() {
            int b = this.base;
            int s = this.top;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (!(forkJoinTaskArr == null || b == s)) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    int s2 = s - 1;
                    long offset = (((long) ((al - 1) & s2)) << ASHIFT) + ((long) ABASE);
                    ForkJoinTask<?> t = (ForkJoinTask) U.getObject(a, offset);
                    if (t != null && U.compareAndSwapObject(a, offset, t, null)) {
                        this.top = s2;
                        return t;
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final ForkJoinTask<?> pollAt(int b) {
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (forkJoinTaskArr != null) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    long offset = (((long) ((al - 1) & b)) << ASHIFT) + ((long) ABASE);
                    ForkJoinTask<?> t = (ForkJoinTask) U.getObjectVolatile(a, offset);
                    if (t != null) {
                        int b2 = b + 1;
                        if (b == this.base && U.compareAndSwapObject(a, offset, t, null)) {
                            this.base = b2;
                            return t;
                        }
                        return null;
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final ForkJoinTask<?> poll() {
            while (true) {
                int b = this.base;
                int s = this.top;
                ForkJoinTask<?>[] forkJoinTaskArr = this.array;
                ForkJoinTask<?>[] a = forkJoinTaskArr;
                if (forkJoinTaskArr == null) {
                    break;
                }
                int i = b - s;
                int d = i;
                if (i >= 0) {
                    break;
                }
                int length = a.length;
                int al = length;
                if (length <= 0) {
                    break;
                }
                long offset = (((long) ((al - 1) & b)) << ASHIFT) + ((long) ABASE);
                ForkJoinTask<?> t = (ForkJoinTask) U.getObjectVolatile(a, offset);
                int b2 = b + 1;
                if (b == this.base) {
                    if (t != null) {
                        int i2 = s;
                        int s2 = b2;
                        if (U.compareAndSwapObject(a, offset, t, null)) {
                            this.base = s2;
                            return t;
                        }
                    } else {
                        int s3 = b2;
                        if (d == -1) {
                            break;
                        }
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final ForkJoinTask<?> nextLocalTask() {
            return this.config < 0 ? poll() : pop();
        }

        /* access modifiers changed from: package-private */
        public final ForkJoinTask<?> peek() {
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (forkJoinTaskArr != null) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    return a[(al - 1) & (this.config < 0 ? this.base : this.top - 1)];
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final boolean tryUnpush(ForkJoinTask<?> task) {
            int b = this.base;
            int s = this.top;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (!(forkJoinTaskArr == null || b == s)) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    int s2 = s - 1;
                    if (U.compareAndSwapObject(a, (((long) ((al - 1) & s2)) << ASHIFT) + ((long) ABASE), task, null)) {
                        this.top = s2;
                        return true;
                    }
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public final int sharedPush(ForkJoinTask<?> task) {
            if (!U.compareAndSwapInt(this, QLOCK, 0, 1)) {
                return 1;
            }
            int b = this.base;
            int s = this.top;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            int stat = 0;
            if (forkJoinTaskArr != null) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    int i = b - s;
                    int d = i;
                    if ((al - 1) + i > 0) {
                        a[(al - 1) & s] = task;
                        this.top = s + 1;
                        this.qlock = 0;
                        if (d < 0 && b == this.base) {
                            stat = d;
                        }
                        return stat;
                    }
                }
            }
            growAndSharedPush(task);
            return stat;
        }

        private void growAndSharedPush(ForkJoinTask<?> task) {
            try {
                growArray();
                int s = this.top;
                ForkJoinTask<?>[] forkJoinTaskArr = this.array;
                ForkJoinTask<?>[] a = forkJoinTaskArr;
                if (forkJoinTaskArr != null) {
                    int length = a.length;
                    int al = length;
                    if (length > 0) {
                        a[(al - 1) & s] = task;
                        this.top = s + 1;
                    }
                }
            } finally {
                this.qlock = 0;
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v0, resolved type: java.util.concurrent.ForkJoinTask<?>} */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public final boolean trySharedUnpush(ForkJoinTask<?> task) {
            boolean popped = false;
            int s = this.top - 1;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (forkJoinTaskArr != null) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    long offset = (((long) ((al - 1) & s)) << ASHIFT) + ((long) ABASE);
                    if (U.getObject(a, offset) == task) {
                        if (U.compareAndSwapInt(this, QLOCK, 0, 1)) {
                            if (this.top == s + 1 && this.array == a && U.compareAndSwapObject(a, offset, task, null)) {
                                popped = true;
                                this.top = s;
                            }
                            U.putOrderedInt(this, QLOCK, 0);
                        }
                    }
                }
            }
            return popped;
        }

        /* access modifiers changed from: package-private */
        public final void cancelAll() {
            ForkJoinTask<?> forkJoinTask = this.currentJoin;
            ForkJoinTask<?> t = forkJoinTask;
            if (forkJoinTask != null) {
                this.currentJoin = null;
                ForkJoinTask.cancelIgnoringExceptions(t);
            }
            ForkJoinTask<?> forkJoinTask2 = this.currentSteal;
            ForkJoinTask<?> t2 = forkJoinTask2;
            if (forkJoinTask2 != null) {
                this.currentSteal = null;
                ForkJoinTask.cancelIgnoringExceptions(t2);
            }
            while (true) {
                ForkJoinTask<?> poll = poll();
                ForkJoinTask<?> t3 = poll;
                if (poll != null) {
                    ForkJoinTask.cancelIgnoringExceptions(t3);
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public final void localPopAndExec() {
            int nexec = 0;
            do {
                int b = this.base;
                int s = this.top;
                ForkJoinTask<?>[] forkJoinTaskArr = this.array;
                ForkJoinTask<?>[] a = forkJoinTaskArr;
                if (forkJoinTaskArr != null && b != s) {
                    int length = a.length;
                    int al = length;
                    if (length > 0) {
                        int s2 = s - 1;
                        ForkJoinTask<?> t = (ForkJoinTask) U.getAndSetObject(a, (((long) ((al - 1) & s2)) << ASHIFT) + ((long) ABASE), null);
                        if (t != null) {
                            this.top = s2;
                            this.currentSteal = t;
                            t.doExec();
                            nexec++;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } while (nexec <= 1023);
        }

        /* access modifiers changed from: package-private */
        public final void localPollAndExec() {
            int nexec = 0;
            while (true) {
                int b = this.base;
                int s = this.top;
                ForkJoinTask<?>[] forkJoinTaskArr = this.array;
                ForkJoinTask<?>[] a = forkJoinTaskArr;
                if (forkJoinTaskArr != null && b != s) {
                    int length = a.length;
                    int al = length;
                    if (length > 0) {
                        int b2 = b + 1;
                        ForkJoinTask<?> t = (ForkJoinTask) U.getAndSetObject(a, (((long) (b & (al - 1))) << ASHIFT) + ((long) ABASE), null);
                        if (t != null) {
                            this.base = b2;
                            t.doExec();
                            nexec++;
                            if (nexec > 1023) {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public final void runTask(ForkJoinTask<?> task) {
            if (task != null) {
                task.doExec();
                if (this.config < 0) {
                    localPollAndExec();
                } else {
                    localPopAndExec();
                }
                int ns = this.nsteals + 1;
                this.nsteals = ns;
                ForkJoinWorkerThread thread = this.owner;
                this.currentSteal = null;
                if (ns < 0) {
                    transferStealCount(this.pool);
                }
                if (thread != null) {
                    thread.afterTopLevelExec();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public final void transferStealCount(ForkJoinPool p) {
            if (p != null) {
                AuxState auxState = p.auxState;
                AuxState aux = auxState;
                if (auxState != null) {
                    long s = (long) this.nsteals;
                    this.nsteals = 0;
                    if (s < 0) {
                        s = 2147483647L;
                    }
                    aux.lock();
                    try {
                        aux.stealCount += s;
                    } finally {
                        aux.unlock();
                    }
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r6v4, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v0, resolved type: java.util.concurrent.ForkJoinTask<?>} */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public final boolean tryRemoveAndExec(ForkJoinTask<?> task) {
            ForkJoinTask<?> forkJoinTask = task;
            if (forkJoinTask != null && forkJoinTask.status >= 0) {
                do {
                    int i = this.base;
                    int b = i;
                    int i2 = this.top;
                    int s = i2;
                    int i3 = i - i2;
                    int d = i3;
                    if (i3 < 0) {
                        ForkJoinTask<?>[] forkJoinTaskArr = this.array;
                        ForkJoinTask<?>[] a = forkJoinTaskArr;
                        if (forkJoinTaskArr != null) {
                            int length = a.length;
                            int al = length;
                            if (length > 0) {
                                while (true) {
                                    s--;
                                    long offset = (long) ((((al - 1) & s) << ASHIFT) + ABASE);
                                    ForkJoinTask<?> t = U.getObjectVolatile(a, offset);
                                    if (t == null) {
                                        break;
                                    } else if (t == forkJoinTask) {
                                        boolean removed = false;
                                        if (s + 1 == this.top) {
                                            ForkJoinTask<?> forkJoinTask2 = t;
                                            if (U.compareAndSwapObject(a, offset, t, null)) {
                                                this.top = s;
                                                removed = true;
                                            }
                                        } else {
                                            ForkJoinTask<?> t2 = t;
                                            if (this.base == b) {
                                                removed = U.compareAndSwapObject(a, offset, t2, new EmptyTask());
                                            }
                                        }
                                        if (removed) {
                                            ForkJoinTask<?> ps = this.currentSteal;
                                            this.currentSteal = forkJoinTask;
                                            task.doExec();
                                            this.currentSteal = ps;
                                        }
                                    } else {
                                        if (t.status >= 0 || s + 1 != this.top) {
                                            d++;
                                            if (d == 0) {
                                                if (this.base == b) {
                                                    return false;
                                                }
                                            }
                                        } else {
                                            ForkJoinTask<?> forkJoinTask3 = t;
                                            if (U.compareAndSwapObject(a, offset, t, null)) {
                                                this.top = s;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } while (forkJoinTask.status >= 0);
                return false;
            }
            return true;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v1, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r17v0, resolved type: java.util.concurrent.CountedCompleter<?>} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r13v2, resolved type: java.lang.Object} */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public final CountedCompleter<?> popCC(CountedCompleter<?> task, int mode) {
            int b = this.base;
            int s = this.top;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (forkJoinTaskArr != null && b != s) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    int index = (al - 1) & (s - 1);
                    long offset = (((long) index) << ASHIFT) + ((long) ABASE);
                    ForkJoinTask<?> o = (ForkJoinTask) U.getObjectVolatile(a, offset);
                    if (o instanceof CountedCompleter) {
                        CountedCompleter<?> t = (CountedCompleter) o;
                        CountedCompleter<?> r = t;
                        while (true) {
                            CountedCompleter<?> r2 = r;
                            if (r2 != task) {
                                long offset2 = offset;
                                ForkJoinTask<?> o2 = o;
                                int index2 = index;
                                CountedCompleter<?> countedCompleter = r2.completer;
                                r = countedCompleter;
                                if (countedCompleter == null) {
                                    break;
                                }
                                offset = offset2;
                                o = o2;
                                index = index2;
                            } else if ((mode & 1) == 0) {
                                boolean popped = false;
                                if (U.compareAndSwapInt(this, QLOCK, 0, 1)) {
                                    if (this.top == s && this.array == a) {
                                        CountedCompleter<?> countedCompleter2 = r2;
                                        long j = offset;
                                        ForkJoinTask<?> forkJoinTask = o;
                                        int i = index;
                                        if (U.compareAndSwapObject(a, offset, t, null)) {
                                            popped = true;
                                            this.top = s - 1;
                                        }
                                    } else {
                                        long j2 = offset;
                                        ForkJoinTask<?> forkJoinTask2 = o;
                                        int i2 = index;
                                    }
                                    U.putOrderedInt(this, QLOCK, 0);
                                    if (popped) {
                                        return t;
                                    }
                                } else {
                                    long j3 = offset;
                                    ForkJoinTask<?> forkJoinTask3 = o;
                                    int i3 = index;
                                }
                            } else {
                                long j4 = offset;
                                ForkJoinTask<?> forkJoinTask4 = o;
                                int i4 = index;
                                if (U.compareAndSwapObject(a, offset, t, null)) {
                                    this.top = s - 1;
                                    return t;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final int pollAndExecCC(CountedCompleter<?> task) {
            int h;
            int b = this.base;
            int s = this.top;
            ForkJoinTask<?>[] forkJoinTaskArr = this.array;
            ForkJoinTask<?>[] a = forkJoinTaskArr;
            if (!(forkJoinTaskArr == null || b == s)) {
                int length = a.length;
                int al = length;
                if (length > 0) {
                    long offset = (((long) ((al - 1) & b)) << ASHIFT) + ((long) ABASE);
                    ForkJoinTask<?> o = (ForkJoinTask) U.getObjectVolatile(a, offset);
                    if (o == null) {
                        h = 2;
                    } else if ((o instanceof CountedCompleter) == 0) {
                        h = -1;
                    } else {
                        CountedCompleter<?> t = (CountedCompleter) o;
                        CountedCompleter<?> r = t;
                        while (true) {
                            CountedCompleter<?> r2 = r;
                            if (r2 == task) {
                                int b2 = b + 1;
                                if (b == this.base) {
                                    int b3 = b2;
                                    int i = s;
                                    CountedCompleter<?> countedCompleter = r2;
                                    if (U.compareAndSwapObject(a, offset, t, null)) {
                                        this.base = b3;
                                        t.doExec();
                                        h = 1;
                                    }
                                } else {
                                    int i2 = b2;
                                    CountedCompleter<?> countedCompleter2 = r2;
                                }
                                h = 2;
                            } else {
                                int s2 = s;
                                CountedCompleter<?> countedCompleter3 = r2.completer;
                                CountedCompleter<?> r3 = countedCompleter3;
                                if (countedCompleter3 == null) {
                                    h = -1;
                                    break;
                                }
                                r = r3;
                                s = s2;
                            }
                        }
                        return h;
                    }
                    return h;
                }
            }
            h = b | Integer.MIN_VALUE;
            return h;
        }

        /* access modifiers changed from: package-private */
        public final boolean isApparentlyUnblocked() {
            if (this.scanState >= 0) {
                Thread thread = this.owner;
                Thread wt = thread;
                if (thread != null) {
                    Thread.State state = wt.getState();
                    Thread.State s = state;
                    if (!(state == Thread.State.BLOCKED || s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING)) {
                        return true;
                    }
                }
            }
            return false;
        }

        static {
            try {
                QLOCK = U.objectFieldOffset(WorkQueue.class.getDeclaredField("qlock"));
                ABASE = U.arrayBaseOffset(ForkJoinTask[].class);
                int scale = U.arrayIndexScale(ForkJoinTask[].class);
                if (((scale - 1) & scale) == 0) {
                    ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
                    return;
                }
                throw new Error("array index scale not a power of two");
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    private static void checkPermission() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(modifyThreadPermission);
        }
    }

    private static final synchronized int nextPoolId() {
        int i;
        synchronized (ForkJoinPool.class) {
            i = poolNumberSequence + 1;
            poolNumberSequence = i;
        }
        return i;
    }

    private void tryInitialize(boolean checkTermination) {
        if (this.runState == 0) {
            int p = this.config & SMASK;
            int n = p > 1 ? p - 1 : 1;
            int n2 = n | (n >>> 1);
            int n3 = n2 | (n2 >>> 2);
            int n4 = n3 | (n3 >>> 4);
            int n5 = n4 | (n4 >>> 8);
            int n6 = SMASK & (((n5 | (n5 >>> 16)) + 1) << 1);
            AuxState aux = new AuxState();
            WorkQueue[] ws = new WorkQueue[n6];
            synchronized (modifyThreadPermission) {
                if (this.runState == 0) {
                    this.workQueues = ws;
                    this.auxState = aux;
                    this.runState = 1;
                }
            }
        }
        if (checkTermination && this.runState < 0) {
            tryTerminate(false, false);
            throw new RejectedExecutionException();
        }
    }

    private boolean createWorker(boolean isSpare) {
        ForkJoinWorkerThreadFactory fac = this.factory;
        Throwable ex = null;
        ForkJoinWorkerThread wt = null;
        if (fac != null) {
            try {
                ForkJoinWorkerThread newThread = fac.newThread(this);
                wt = newThread;
                if (newThread != null) {
                    if (isSpare) {
                        WorkQueue workQueue = wt.workQueue;
                        WorkQueue q = workQueue;
                        if (workQueue != null) {
                            q.config |= 131072;
                        }
                    }
                    wt.start();
                    return true;
                }
            } catch (Throwable rex) {
                ex = rex;
            }
        }
        deregisterWorker(wt, ex);
        return false;
    }

    private void tryAddWorker(long c) {
        do {
            long nc = (AC_MASK & (AC_UNIT + c)) | (TC_MASK & (TC_UNIT + c));
            if (this.ctl == c) {
                if (U.compareAndSwapLong(this, CTL, c, nc)) {
                    createWorker(false);
                    return;
                }
            }
            long j = this.ctl;
            c = j;
            if ((j & ADD_WORKER) == 0) {
                return;
            }
        } while (((int) c) == 0);
    }

    /* access modifiers changed from: package-private */
    public final WorkQueue registerWorker(ForkJoinWorkerThread wt) {
        wt.setDaemon(true);
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = this.ueh;
        Thread.UncaughtExceptionHandler handler = uncaughtExceptionHandler;
        if (uncaughtExceptionHandler != null) {
            wt.setUncaughtExceptionHandler(handler);
        }
        WorkQueue w = new WorkQueue(this, wt);
        int i = 0;
        int mode = this.config & MODE_MASK;
        AuxState auxState2 = this.auxState;
        AuxState aux = auxState2;
        if (auxState2 != null) {
            aux.lock();
            try {
                long j = aux.indexSeed - 1640531527;
                aux.indexSeed = j;
                int s = (int) j;
                WorkQueue[] ws = this.workQueues;
                if (ws != null) {
                    int length = ws.length;
                    int n = length;
                    if (length > 0) {
                        int i2 = n - 1;
                        int m = i2;
                        i = i2 & (1 | (s << 1));
                        if (ws[i] != null) {
                            int probes = 0;
                            int step = 2;
                            if (n > 4) {
                                step = 2 + ((n >>> 1) & EVENMASK);
                            }
                            while (true) {
                                int i3 = (i + step) & m;
                                i = i3;
                                if (ws[i3] == null) {
                                    break;
                                }
                                probes++;
                                if (probes >= n) {
                                    int i4 = n << 1;
                                    n = i4;
                                    WorkQueue[] workQueueArr = (WorkQueue[]) Arrays.copyOf((T[]) ws, i4);
                                    ws = workQueueArr;
                                    this.workQueues = workQueueArr;
                                    m = n - 1;
                                    probes = 0;
                                }
                            }
                        }
                        w.hint = s;
                        w.config = i | mode;
                        w.scanState = (2147418112 & s) | i;
                        ws[i] = w;
                    }
                }
            } finally {
                aux.unlock();
            }
        }
        wt.setName(this.workerNamePrefix.concat(Integer.toString(i >>> 1)));
        return w;
    }

    /* access modifiers changed from: package-private */
    /*  JADX ERROR: IF instruction can be used only in fallback mode
        jadx.core.utils.exceptions.CodegenException: IF instruction can be used only in fallback mode
        	at jadx.core.codegen.InsnGen.fallbackOnlyInsn(InsnGen.java:568)
        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:474)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeLoop(RegionGen.java:205)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:66)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
        	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:156)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
        	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
        */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00bc  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ba A[EDGE_INSN: B:53:0x00ba->B:45:0x00ba ?: BREAK  , SYNTHETIC] */
    public final void deregisterWorker(java.util.concurrent.ForkJoinWorkerThread r19, java.lang.Throwable r20) {
        /*
            r18 = this;
            r9 = r18
            r10 = r19
            r0 = 0
            r11 = 0
            if (r10 == 0) goto L_0x003b
            java.util.concurrent.ForkJoinPool$WorkQueue r1 = r10.workQueue
            r2 = r1
            if (r1 == 0) goto L_0x003a
            int r0 = r2.config
            r1 = 65535(0xffff, float:9.1834E-41)
            r1 = r1 & r0
            int r3 = r2.nsteals
            java.util.concurrent.ForkJoinPool$AuxState r0 = r9.auxState
            r4 = r0
            if (r0 == 0) goto L_0x003a
            r4.lock()
            java.util.concurrent.ForkJoinPool$WorkQueue[] r0 = r9.workQueues     // Catch:{ all -> 0x0035 }
            r5 = r0
            if (r0 == 0) goto L_0x002b
            int r0 = r5.length     // Catch:{ all -> 0x0035 }
            if (r0 <= r1) goto L_0x002b
            r0 = r5[r1]     // Catch:{ all -> 0x0035 }
            if (r0 != r2) goto L_0x002b
            r5[r1] = r11     // Catch:{ all -> 0x0035 }
        L_0x002b:
            long r6 = r4.stealCount     // Catch:{ all -> 0x0035 }
            long r12 = (long) r3     // Catch:{ all -> 0x0035 }
            long r6 = r6 + r12
            r4.stealCount = r6     // Catch:{ all -> 0x0035 }
            r4.unlock()
            goto L_0x003a
        L_0x0035:
            r0 = move-exception
            r4.unlock()
            throw r0
        L_0x003a:
            r0 = r2
        L_0x003b:
            if (r0 == 0) goto L_0x0044
            int r1 = r0.config
            r2 = 262144(0x40000, float:3.67342E-40)
            r1 = r1 & r2
            if (r1 != 0) goto L_0x006f
        L_0x0044:
            sun.misc.Unsafe r1 = U
            long r3 = CTL
            long r5 = r9.ctl
            r12 = r5
            r7 = -281474976710656(0xffff000000000000, double:NaN)
            r14 = 281474976710656(0x1000000000000, double:1.390671161567E-309)
            long r14 = r12 - r14
            long r7 = r7 & r14
            r14 = 281470681743360(0xffff00000000, double:1.39064994160909E-309)
            r16 = 4294967296(0x100000000, double:2.121995791E-314)
            long r16 = r12 - r16
            long r14 = r14 & r16
            long r7 = r7 | r14
            r14 = 4294967295(0xffffffff, double:2.1219957905E-314)
            long r14 = r14 & r12
            long r7 = r7 | r14
            r2 = r9
            boolean r1 = r1.compareAndSwapLong(r2, r3, r5, r7)
            if (r1 == 0) goto L_0x0044
        L_0x006f:
            if (r0 == 0) goto L_0x0079
            r0.currentSteal = r11
            r1 = -1
            r0.qlock = r1
            r0.cancelAll()
        L_0x0079:
            r1 = 0
            int r1 = r9.tryTerminate(r1, r1)
            if (r1 < 0) goto L_0x00ba
            if (r0 == 0) goto L_0x00ba
            java.util.concurrent.ForkJoinTask<?>[] r1 = r0.array
            if (r1 == 0) goto L_0x00ba
            java.util.concurrent.ForkJoinPool$WorkQueue[] r1 = r9.workQueues
            r7 = r1
            if (r1 == 0) goto L_0x00ba
            int r1 = r7.length
            r8 = r1
            if (r1 > 0) goto L_0x0090
            goto L_0x00ba
        L_0x0090:
            long r1 = r9.ctl
            r11 = r1
            int r1 = (int) r1
            r13 = r1
            if (r1 == 0) goto L_0x00a8
            int r1 = r8 + -1
            r1 = r1 & r13
            r4 = r7[r1]
            r5 = 281474976710656(0x1000000000000, double:1.390671161567E-309)
            r1 = r9
            r2 = r11
            boolean r1 = r1.tryRelease(r2, r4, r5)
            if (r1 == 0) goto L_0x00a7
            goto L_0x00ba
        L_0x00a7:
            goto L_0x0079
        L_0x00a8:
            if (r20 == 0) goto L_0x00ba
            r1 = 140737488355328(0x800000000000, double:6.953355807835E-310)
            long r1 = r1 & r11
            r3 = 0
            int r1 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r1 == 0) goto L_0x00ba
            r9.tryAddWorker(r11)
        L_0x00ba:
            if (r20 != 0) goto L_0x00c0
            java.util.concurrent.ForkJoinTask.helpExpungeStaleExceptions()
            goto L_0x00c3
        L_0x00c0:
            java.util.concurrent.ForkJoinTask.rethrow(r20)
        L_0x00c3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ForkJoinPool.deregisterWorker(java.util.concurrent.ForkJoinWorkerThread, java.lang.Throwable):void");
    }

    /* access modifiers changed from: package-private */
    public final void signalWork() {
        while (true) {
            long j = this.ctl;
            long c = j;
            if (j < 0) {
                int i = (int) c;
                int sp = i;
                if (i != 0) {
                    WorkQueue[] workQueueArr = this.workQueues;
                    WorkQueue[] ws = workQueueArr;
                    if (workQueueArr != null) {
                        int length = ws.length;
                        int i2 = SMASK & sp;
                        int i3 = i2;
                        if (length > i2) {
                            WorkQueue workQueue = ws[i3];
                            WorkQueue v = workQueue;
                            if (workQueue != null) {
                                int ns = sp & Integer.MAX_VALUE;
                                int vs = v.scanState;
                                long nc = (((long) v.stackPred) & SP_MASK) | (UC_MASK & (AC_UNIT + c));
                                if (sp == vs) {
                                    int i4 = vs;
                                    if (U.compareAndSwapLong(this, CTL, c, nc)) {
                                        v.scanState = ns;
                                        LockSupport.unpark(v.parker);
                                        return;
                                    }
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else if ((ADD_WORKER & c) != 0) {
                    tryAddWorker(c);
                    return;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private boolean tryRelease(long c, WorkQueue v, long inc) {
        long j = c;
        WorkQueue workQueue = v;
        int sp = (int) j;
        int ns = sp & Integer.MAX_VALUE;
        if (workQueue != null) {
            int vs = workQueue.scanState;
            long nc = (((long) workQueue.stackPred) & SP_MASK) | (UC_MASK & (j + inc));
            if (sp == vs) {
                if (U.compareAndSwapLong(this, CTL, j, nc)) {
                    workQueue.scanState = ns;
                    LockSupport.unpark(workQueue.parker);
                    return true;
                }
            }
        }
        return false;
    }

    private void tryReactivate(WorkQueue w, WorkQueue[] ws, int r) {
        WorkQueue workQueue = w;
        WorkQueue[] workQueueArr = ws;
        long j = this.ctl;
        long c = j;
        int i = (int) j;
        int sp = i;
        if (i != 0 && workQueue != null && workQueueArr != null) {
            int length = workQueueArr.length;
            int wl = length;
            if (length > 0 && ((sp ^ r) & 65536) == 0) {
                WorkQueue workQueue2 = workQueueArr[(wl - 1) & sp];
                WorkQueue v = workQueue2;
                if (workQueue2 != null) {
                    long nc = (((long) v.stackPred) & SP_MASK) | (UC_MASK & (AC_UNIT + c));
                    int ns = sp & Integer.MAX_VALUE;
                    if (workQueue.scanState < 0 && v.scanState == sp) {
                        WorkQueue v2 = v;
                        int ns2 = ns;
                        if (U.compareAndSwapLong(this, CTL, c, nc)) {
                            v2.scanState = ns2;
                            LockSupport.unpark(v2.parker);
                        }
                    }
                }
            }
        }
    }

    private void inactivate(WorkQueue w, int ss) {
        long j;
        long c;
        WorkQueue workQueue = w;
        int ns = (ss + 65536) | Integer.MIN_VALUE;
        long lc = ((long) ns) & SP_MASK;
        if (workQueue != null) {
            workQueue.scanState = ns;
            do {
                j = this.ctl;
                c = j;
                workQueue.stackPred = (int) c;
                long j2 = c;
            } while (!U.compareAndSwapLong(this, CTL, c, (UC_MASK & (j - AC_UNIT)) | lc));
            return;
        }
    }

    private int awaitWork(WorkQueue w) {
        if (w == null || w.scanState >= 0) {
            return 0;
        }
        long c = this.ctl;
        if (((int) (c >> 48)) + (this.config & SMASK) <= 0) {
            return timedAwaitWork(w, c);
        }
        if ((this.runState & 2) != 0) {
            w.qlock = -1;
            return -1;
        } else if (w.scanState >= 0) {
            return 0;
        } else {
            w.parker = Thread.currentThread();
            if (w.scanState < 0) {
                LockSupport.park(this);
            }
            w.parker = null;
            if ((this.runState & 2) != 0) {
                w.qlock = -1;
                return -1;
            } else if (w.scanState >= 0) {
                return 0;
            } else {
                Thread.interrupted();
                return 0;
            }
        }
    }

    private int timedAwaitWork(WorkQueue w, long c) {
        AuxState aux;
        int stat;
        WorkQueue workQueue = w;
        long j = c;
        int stat2 = 0;
        int i = 1;
        int scale = 1 - ((short) ((int) (j >>> 32)));
        if (scale > 0) {
            i = scale;
        }
        long deadline = (((long) i) * IDLE_TIMEOUT_MS) + System.currentTimeMillis();
        if (this.runState < 0) {
            int tryTerminate = tryTerminate(false, false);
            stat2 = tryTerminate;
            if (tryTerminate <= 0) {
                return stat2;
            }
        }
        int stat3 = stat2;
        if (workQueue != null && workQueue.scanState < 0) {
            workQueue.parker = Thread.currentThread();
            if (workQueue.scanState < 0) {
                LockSupport.parkUntil(this, deadline);
            }
            workQueue.parker = null;
            if ((this.runState & 2) != 0) {
                workQueue.qlock = -1;
                return -1;
            }
            int i2 = workQueue.scanState;
            int ss = i2;
            if (i2 < 0 && !Thread.interrupted() && ((int) j) == ss) {
                AuxState auxState2 = this.auxState;
                AuxState aux2 = auxState2;
                if (auxState2 != null && this.ctl == j && deadline - System.currentTimeMillis() <= TIMEOUT_SLOP_MS) {
                    aux2.lock();
                    try {
                        int cfg = workQueue.config;
                        int idx = cfg & SMASK;
                        int ss2 = ss;
                        long nc = (UC_MASK & (j - TC_UNIT)) | (SP_MASK & ((long) workQueue.stackPred));
                        try {
                            if ((this.runState & 2) == 0) {
                                WorkQueue[] workQueueArr = this.workQueues;
                                WorkQueue[] ws = workQueueArr;
                                if (workQueueArr != null && idx < ws.length && idx >= 0 && ws[idx] == workQueue) {
                                    int idx2 = idx;
                                    aux = aux2;
                                    int i3 = ss2;
                                    WorkQueue[] ws2 = ws;
                                    try {
                                        if (U.compareAndSwapLong(this, CTL, j, nc)) {
                                            ws2[idx2] = null;
                                            workQueue.config = cfg | 262144;
                                            stat = -1;
                                            workQueue.qlock = -1;
                                            aux.unlock();
                                            return stat;
                                        }
                                        stat = stat3;
                                        aux.unlock();
                                        return stat;
                                    } catch (Throwable th) {
                                        th = th;
                                        aux.unlock();
                                        throw th;
                                    }
                                }
                            }
                            aux = aux2;
                            int i4 = ss2;
                            stat = stat3;
                            aux.unlock();
                            return stat;
                        } catch (Throwable th2) {
                            th = th2;
                            aux = aux2;
                            int i5 = ss2;
                            aux.unlock();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        aux = aux2;
                        int i6 = ss;
                        aux.unlock();
                        throw th;
                    }
                }
            }
        }
        return stat3;
    }

    private boolean tryDropSpare(WorkQueue w) {
        WorkQueue[] ws;
        boolean dropped;
        boolean canDrop;
        long nc;
        WorkQueue workQueue = w;
        if (workQueue != null && w.isEmpty()) {
            do {
                long j = this.ctl;
                long c = j;
                if (((short) ((int) (j >> 32))) > 0) {
                    int i = (int) c;
                    int sp = i;
                    if (i != 0 || ((int) (c >> 48)) > 0) {
                        WorkQueue[] workQueueArr = this.workQueues;
                        ws = workQueueArr;
                        if (workQueueArr != null) {
                            int length = ws.length;
                            int wl = length;
                            if (length > 0) {
                                if (sp == 0) {
                                    dropped = U.compareAndSwapLong(this, CTL, c, ((c - TC_UNIT) & TC_MASK) | (AC_MASK & (c - AC_UNIT)) | (SP_MASK & c));
                                    continue;
                                } else {
                                    WorkQueue workQueue2 = ws[(wl - 1) & sp];
                                    WorkQueue v = workQueue2;
                                    if (workQueue2 != null) {
                                        WorkQueue v2 = v;
                                        if (v2.scanState == sp) {
                                            long nc2 = SP_MASK & ((long) v2.stackPred);
                                            if (workQueue == v2 || workQueue.scanState >= 0) {
                                                canDrop = true;
                                                nc = nc2 | (AC_MASK & c) | (TC_MASK & (c - TC_UNIT));
                                            } else {
                                                canDrop = false;
                                                nc = nc2 | (AC_MASK & (AC_UNIT + c)) | (TC_MASK & c);
                                            }
                                            boolean canDrop2 = canDrop;
                                            if (U.compareAndSwapLong(this, CTL, c, nc)) {
                                                v2.scanState = Integer.MAX_VALUE & sp;
                                                LockSupport.unpark(v2.parker);
                                                dropped = canDrop2;
                                                continue;
                                            } else {
                                                dropped = false;
                                                continue;
                                            }
                                        }
                                    } else {
                                        WorkQueue workQueue3 = v;
                                    }
                                    dropped = false;
                                    continue;
                                }
                            }
                        }
                    }
                }
            } while (!dropped);
            int cfg = workQueue.config;
            int idx = SMASK & cfg;
            if (idx >= 0 && idx < ws.length && ws[idx] == workQueue) {
                ws[idx] = null;
            }
            workQueue.config = 262144 | cfg;
            workQueue.qlock = -1;
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final void runWorker(WorkQueue w) {
        w.growArray();
        int bound = (w.config & 131072) != 0 ? 0 : 1023;
        long seed = ((long) w.hint) * -2685821657736338717L;
        if ((this.runState & 2) == 0) {
            long r = seed == 0 ? 1 : seed;
            while (true) {
                if (bound != 0 || !tryDropSpare(w)) {
                    long r2 = r ^ (r >>> 12);
                    long r3 = r2 ^ (r2 << 25);
                    r = r3 ^ (r3 >>> 27);
                    if (scan(w, bound, ((int) (r >>> 48)) | 1, (int) r) < 0 && awaitWork(w) < 0) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private int scan(WorkQueue w, int bound, int step, int r) {
        int m;
        int stat;
        int b;
        int idx;
        WorkQueue workQueue = w;
        int stat2 = 0;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (!(workQueueArr == null || workQueue == null)) {
            int length = ws.length;
            int wl = length;
            if (length > 0) {
                int m2 = wl - 1;
                int idx2 = m2 & r;
                int origin = idx2;
                int npolls = 0;
                int ss = workQueue.scanState;
                int r2 = r;
                while (true) {
                    WorkQueue workQueue2 = ws[origin];
                    WorkQueue q = workQueue2;
                    if (workQueue2 != null) {
                        int i = q.base;
                        int b2 = i;
                        if (i - q.top < 0) {
                            ForkJoinTask<?>[] forkJoinTaskArr = q.array;
                            ForkJoinTask<?>[] a = forkJoinTaskArr;
                            if (forkJoinTaskArr != null) {
                                int length2 = a.length;
                                int al = length2;
                                if (length2 > 0) {
                                    stat = stat2;
                                    m = m2;
                                    int origin2 = idx2;
                                    int idx3 = origin;
                                    long offset = (((long) ((al - 1) & b2)) << ASHIFT) + ((long) ABASE);
                                    ForkJoinTask<?> t = (ForkJoinTask) U.getObjectVolatile(a, offset);
                                    if (t != null) {
                                        int b3 = b2 + 1;
                                        if (b2 != q.base) {
                                            break;
                                        } else if (ss < 0) {
                                            tryReactivate(workQueue, ws, r2);
                                            break;
                                        } else {
                                            ForkJoinTask<?>[] forkJoinTaskArr2 = a;
                                            if (!U.compareAndSwapObject(a, offset, t, null)) {
                                                break;
                                            }
                                            q.base = b3;
                                            workQueue.currentSteal = t;
                                            if (b3 != q.top) {
                                                signalWork();
                                            }
                                            workQueue.runTask(t);
                                            npolls++;
                                            if (npolls > bound) {
                                                return stat;
                                            }
                                            b = origin2;
                                            idx = idx3;
                                            stat2 = stat;
                                            m2 = m;
                                            int i2 = b;
                                            origin = idx;
                                            idx2 = i2;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    int i3 = bound;
                    stat = stat2;
                    m = m2;
                    int origin3 = idx2;
                    int idx4 = origin;
                    if (npolls != 0) {
                        return stat;
                    }
                    int i4 = (idx4 + step) & m;
                    idx = i4;
                    b = origin3;
                    if (i4 != b) {
                        continue;
                    } else if (ss < 0) {
                        return ss;
                    } else {
                        if (r2 >= 0) {
                            inactivate(workQueue, ss);
                            return stat;
                        }
                        r2 <<= 1;
                    }
                    stat2 = stat;
                    m2 = m;
                    int i22 = b;
                    origin = idx;
                    idx2 = i22;
                }
                int i5 = bound;
                return stat;
            }
        }
        int i6 = bound;
        int i7 = r;
        return 0;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0076  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009d  */
    public final int helpComplete(WorkQueue w, CountedCompleter<?> task, int maxTasks) {
        WorkQueue workQueue = w;
        CountedCompleter<?> countedCompleter = task;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            int length = ws.length;
            int wl = length;
            int i = 1;
            if (!(length <= 1 || countedCompleter == null || workQueue == null)) {
                int m = wl - 1;
                int mode = workQueue.config;
                int r = ~mode;
                int origin = r & m;
                int maxTasks2 = maxTasks;
                int oldSum = 0;
                int h = 1;
                int step = 3;
                int k = origin;
                int r2 = r;
                int checkSum = 0;
                while (true) {
                    int i2 = countedCompleter.status;
                    int s = i2;
                    if (i2 < 0) {
                        return s;
                    }
                    if (h == i) {
                        CountedCompleter<?> popCC = workQueue.popCC(countedCompleter, mode);
                        CountedCompleter<?> p = popCC;
                        if (popCC != null) {
                            p.doExec();
                            if (maxTasks2 != 0) {
                                maxTasks2--;
                                if (maxTasks2 == 0) {
                                    return s;
                                }
                            }
                            origin = k;
                            checkSum = 0;
                            oldSum = 0;
                            workQueue = w;
                            i = 1;
                        }
                    }
                    int i3 = k | 1;
                    int i4 = i3;
                    if (i3 >= 0) {
                        int i5 = i4;
                        if (i5 <= m) {
                            WorkQueue workQueue2 = ws[i5];
                            WorkQueue q = workQueue2;
                            if (workQueue2 != null) {
                                int pollAndExecCC = q.pollAndExecCC(countedCompleter);
                                h = pollAndExecCC;
                                if (pollAndExecCC < 0) {
                                    checkSum += h;
                                }
                                if (h <= 0) {
                                    if (h == 1 && maxTasks2 != 0) {
                                        maxTasks2--;
                                        if (maxTasks2 == 0) {
                                            return s;
                                        }
                                    }
                                    step = (r2 >>> 16) | 3;
                                    int r3 = r2 ^ (r2 << 13);
                                    int r4 = r3 ^ (r3 >>> 17);
                                    r2 = r4 ^ (r4 << 5);
                                    int i6 = r2 & m;
                                    origin = i6;
                                    k = i6;
                                    checkSum = 0;
                                    oldSum = 0;
                                } else {
                                    int i7 = (k + step) & m;
                                    k = i7;
                                    if (i7 == origin) {
                                        int oldSum2 = checkSum;
                                        if (oldSum == checkSum) {
                                            return s;
                                        }
                                        checkSum = 0;
                                        oldSum = oldSum2;
                                    } else {
                                        int checkSum2 = oldSum;
                                    }
                                }
                                workQueue = w;
                                i = 1;
                            }
                        }
                    }
                    h = 0;
                    if (h <= 0) {
                    }
                    workQueue = w;
                    i = 1;
                }
            }
        }
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:105:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0041, code lost:
        r10.hint = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0046, code lost:
        if (r9.status >= 0) goto L_0x004f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0048, code lost:
        r24 = r7;
        r25 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004f, code lost:
        r3 = r14.base;
        r11 = r3;
        r13 = r13 + r3;
        r3 = r14.currentJoin;
        r12 = null;
        r15 = r14.array;
        r23 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005a, code lost:
        if (r15 == null) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x005c, code lost:
        r15 = r23;
        r5 = r15.length;
        r16 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0061, code lost:
        if (r5 <= 0) goto L_0x00cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0063, code lost:
        r5 = (r16 - 1) & r11;
        r24 = r7;
        r25 = r8;
        r26 = r5;
        r27 = r6;
        r5 = ((long) ABASE) + (((long) r5) << ASHIFT);
        r7 = (java.util.concurrent.ForkJoinTask) U.getObjectVolatile(r15, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007f, code lost:
        if (r7 == null) goto L_0x00cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0081, code lost:
        r8 = r11 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0085, code lost:
        if (r11 != r14.base) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0089, code lost:
        if (r10.currentJoin != r9) goto L_0x000b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x008d, code lost:
        if (r14.currentSteal != r9) goto L_0x000b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0091, code lost:
        if (r9.status >= 0) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a3, code lost:
        if (U.compareAndSwapObject(r15, r5, r7, null) == false) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a5, code lost:
        r14.base = r8;
        r0.currentSteal = r7;
        r11 = r0.top;
        r12 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ac, code lost:
        r12.doExec();
        r0.currentSteal = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b3, code lost:
        if (r1.status >= 0) goto L_0x00b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b9, code lost:
        if (r0.top != r11) goto L_0x00bc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00bc, code lost:
        r7 = r29.pop();
        r12 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c1, code lost:
        if (r7 != null) goto L_0x00c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00c5, code lost:
        r0.currentSteal = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00c8, code lost:
        r12 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c9, code lost:
        r11 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00cb, code lost:
        r12 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00cd, code lost:
        r27 = r6;
        r24 = r7;
        r25 = r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d4, code lost:
        r27 = r6;
        r24 = r7;
        r25 = r8;
        r15 = r23;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00dc, code lost:
        if (r12 != null) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00e0, code lost:
        if (r11 != r14.base) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e6, code lost:
        if ((r11 - r14.top) < 0) goto L_0x0102;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e8, code lost:
        r9 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00e9, code lost:
        if (r3 != null) goto L_0x00f5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00ed, code lost:
        if (r3 != r14.currentJoin) goto L_0x000b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00ef, code lost:
        r5 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f0, code lost:
        if (r4 != r13) goto L_0x00f3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00f3, code lost:
        r4 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0102, code lost:
        r7 = r24;
        r8 = r25;
        r6 = r27;
        r5 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x000b, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x000b, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x000b, code lost:
        continue;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x000b, code lost:
        continue;
     */
    private void helpStealer(WorkQueue w, ForkJoinTask<?> task) {
        int checkSum;
        int m;
        int wl;
        WorkQueue[] ws;
        WorkQueue v;
        int m2;
        int wl2;
        WorkQueue[] ws2;
        WorkQueue workQueue = w;
        ForkJoinTask<?> forkJoinTask = task;
        if (forkJoinTask != null && workQueue != null) {
            ForkJoinTask<?> ps = workQueue.currentSteal;
            int oldSum = 0;
            while (w.tryRemoveAndExec(task) && forkJoinTask.status >= 0) {
                WorkQueue[] workQueueArr = this.workQueues;
                WorkQueue[] ws3 = workQueueArr;
                if (workQueueArr != null) {
                    int length = ws3.length;
                    int wl3 = length;
                    if (length > 0) {
                        int m3 = wl3 - 1;
                        WorkQueue j = workQueue;
                        int b = 0;
                        ForkJoinTask<?> subtask = forkJoinTask;
                        while (true) {
                            if (subtask.status < 0) {
                                int i = wl3;
                                break;
                            }
                            int h = j.hint | 1;
                            checkSum = b;
                            int k = 0;
                            while (true) {
                                int i2 = ((k << 1) + h) & m3;
                                int i3 = i2;
                                WorkQueue workQueue2 = ws3[i2];
                                WorkQueue v2 = workQueue2;
                                if (workQueue2 != null) {
                                    v = v2;
                                    if (v.currentSteal == subtask) {
                                        break;
                                    }
                                    m = m3;
                                    ws = ws3;
                                    wl = wl3;
                                    checkSum += v.base;
                                } else {
                                    m = m3;
                                    ws = ws3;
                                    wl = wl3;
                                    WorkQueue workQueue3 = v2;
                                }
                                k++;
                                int m4 = m;
                                if (k <= m4) {
                                    m3 = m4;
                                    ws3 = ws;
                                    wl3 = wl;
                                } else {
                                    return;
                                }
                            }
                            j = v;
                            b = checkSum;
                            ws3 = ws2;
                            wl3 = wl2;
                            m3 = m2;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private boolean tryCompensate(WorkQueue w) {
        WorkQueue workQueue = w;
        long c = this.ctl;
        WorkQueue[] ws = this.workQueues;
        int pc = this.config & SMASK;
        int ac = pc + ((int) (c >> 48));
        int tc = pc + ((short) ((int) (c >> 32)));
        if (!(workQueue == null || workQueue.qlock < 0 || pc == 0 || ws == null)) {
            int length = ws.length;
            int wl = length;
            if (length > 0) {
                int m = wl - 1;
                boolean busy = true;
                int i = 0;
                while (true) {
                    if (i > m) {
                        break;
                    }
                    int i2 = (i << 1) | 1;
                    int k = i2;
                    if (i2 <= m && k >= 0) {
                        WorkQueue workQueue2 = ws[k];
                        WorkQueue v = workQueue2;
                        if (workQueue2 != null && v.scanState >= 0 && v.currentSteal == null) {
                            busy = false;
                            break;
                        }
                    }
                    i++;
                }
                if (!busy) {
                } else if (this.ctl != c) {
                    int i3 = m;
                } else {
                    int i4 = (int) c;
                    int sp = i4;
                    if (i4 != 0) {
                        return tryRelease(c, ws[m & sp], 0);
                    } else if (tc < pc || ac <= 1 || !w.isEmpty()) {
                        if (tc >= MAX_CAP || (this == common && tc >= COMMON_MAX_SPARES + pc)) {
                            throw new RejectedExecutionException("Thread limit exceeded replacing blocked worker");
                        }
                        return U.compareAndSwapLong(this, CTL, c, (AC_MASK & c) | (TC_MASK & (TC_UNIT + c))) && createWorker(tc >= pc);
                    } else {
                        int i5 = m;
                        return U.compareAndSwapLong(this, CTL, c, (AC_MASK & (c - AC_UNIT)) | (281474976710655L & c));
                    }
                }
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x005a  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x006a  */
    public final int awaitJoin(WorkQueue w, ForkJoinTask<?> task, long deadline) {
        int s;
        ForkJoinPool forkJoinPool;
        long ms;
        int i;
        long j;
        WorkQueue workQueue = w;
        ForkJoinTask<?> forkJoinTask = task;
        int s2 = 0;
        if (workQueue != null) {
            ForkJoinTask<?> prevJoin = workQueue.currentJoin;
            if (forkJoinTask != null) {
                int i2 = forkJoinTask.status;
                s2 = i2;
                if (i2 >= 0) {
                    workQueue.currentJoin = forkJoinTask;
                    CountedCompleter<?> cc = forkJoinTask instanceof CountedCompleter ? (CountedCompleter) forkJoinTask : null;
                    do {
                        if (cc != null) {
                            forkJoinPool = this;
                            forkJoinPool.helpComplete(workQueue, cc, 0);
                        } else {
                            forkJoinPool = this;
                            helpStealer(w, task);
                        }
                        int i3 = forkJoinTask.status;
                        s = i3;
                        if (i3 < 0) {
                            break;
                        }
                        if (deadline == 0) {
                            j = 0;
                        } else {
                            long nanoTime = deadline - System.nanoTime();
                            long ns = nanoTime;
                            if (nanoTime <= 0) {
                                break;
                            }
                            long millis = TimeUnit.NANOSECONDS.toMillis(ns);
                            long ms2 = millis;
                            if (millis <= 0) {
                                j = 1;
                            } else {
                                ms = ms2;
                                if (!tryCompensate(w)) {
                                    forkJoinTask.internalWait(ms);
                                    long j2 = ms;
                                    U.getAndAddLong(forkJoinPool, CTL, AC_UNIT);
                                }
                                i = forkJoinTask.status;
                                s = i;
                            }
                        }
                        ms = j;
                        if (!tryCompensate(w)) {
                        }
                        i = forkJoinTask.status;
                        s = i;
                    } while (i >= 0);
                    workQueue.currentJoin = prevJoin;
                    return s;
                }
            }
        }
        return s;
    }

    private WorkQueue findNonEmptyStealQueue() {
        int r = ThreadLocalRandom.nextSecondarySeed();
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            int length = ws.length;
            int wl = length;
            if (length > 0) {
                int m = wl - 1;
                int origin = r & m;
                int k = origin;
                int checkSum = 0;
                int checkSum2 = 0;
                while (true) {
                    WorkQueue workQueue = ws[k];
                    WorkQueue q = workQueue;
                    if (workQueue != null) {
                        int i = q.base;
                        int b = i;
                        if (i - q.top < 0) {
                            return q;
                        }
                        checkSum2 += b;
                    }
                    int i2 = (k + 1) & m;
                    k = i2;
                    if (i2 == origin) {
                        int oldSum = checkSum2;
                        if (checkSum == checkSum2) {
                            break;
                        }
                        checkSum2 = 0;
                        checkSum = oldSum;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void helpQuiescePool(WorkQueue w) {
        WorkQueue workQueue = w;
        ForkJoinTask<?> ps = workQueue.currentSteal;
        int wc = workQueue.config;
        boolean active = true;
        while (true) {
            boolean active2 = active;
            if (wc >= 0) {
                ForkJoinTask<?> pop = w.pop();
                ForkJoinTask<?> t = pop;
                if (pop != null) {
                    workQueue.currentSteal = t;
                    t.doExec();
                    workQueue.currentSteal = ps;
                    active = active2;
                }
            }
            WorkQueue findNonEmptyStealQueue = findNonEmptyStealQueue();
            WorkQueue q = findNonEmptyStealQueue;
            if (findNonEmptyStealQueue != null) {
                if (!active2) {
                    active2 = true;
                    U.getAndAddLong(this, CTL, AC_UNIT);
                }
                ForkJoinTask<?> pollAt = q.pollAt(q.base);
                ForkJoinTask<?> t2 = pollAt;
                if (pollAt != null) {
                    workQueue.currentSteal = t2;
                    t2.doExec();
                    workQueue.currentSteal = ps;
                    int i = workQueue.nsteals + 1;
                    workQueue.nsteals = i;
                    if (i < 0) {
                        workQueue.transferStealCount(this);
                    }
                }
            } else if (active2) {
                long j = this.ctl;
                long c = j;
                if (U.compareAndSwapLong(this, CTL, c, (AC_MASK & (j - AC_UNIT)) | (281474976710655L & c))) {
                    active2 = false;
                }
            } else {
                long j2 = this.ctl;
                long c2 = j2;
                if (((int) (j2 >> 48)) + (this.config & SMASK) <= 0) {
                    if (U.compareAndSwapLong(this, CTL, c2, c2 + AC_UNIT)) {
                        return;
                    }
                } else {
                    continue;
                }
            }
            active = active2;
        }
    }

    /* access modifiers changed from: package-private */
    public final ForkJoinTask<?> nextTaskFor(WorkQueue w) {
        ForkJoinTask<?> pollAt;
        ForkJoinTask<?> t;
        do {
            ForkJoinTask<?> nextLocalTask = w.nextLocalTask();
            ForkJoinTask<?> t2 = nextLocalTask;
            if (nextLocalTask != null) {
                return t2;
            }
            WorkQueue findNonEmptyStealQueue = findNonEmptyStealQueue();
            WorkQueue q = findNonEmptyStealQueue;
            if (findNonEmptyStealQueue == null) {
                return null;
            }
            pollAt = q.pollAt(q.base);
            t = pollAt;
        } while (pollAt == null);
        return t;
    }

    static int getSurplusQueuedTaskCount() {
        Thread currentThread = Thread.currentThread();
        Thread t = currentThread;
        int i = 0;
        if (!(currentThread instanceof ForkJoinWorkerThread)) {
            return 0;
        }
        ForkJoinWorkerThread forkJoinWorkerThread = (ForkJoinWorkerThread) t;
        ForkJoinWorkerThread wt = forkJoinWorkerThread;
        ForkJoinPool forkJoinPool = forkJoinWorkerThread.pool;
        ForkJoinPool pool = forkJoinPool;
        int p = forkJoinPool.config & SMASK;
        WorkQueue q = wt.workQueue;
        int n = q.top - q.base;
        int a = ((int) (pool.ctl >> 48)) + p;
        int i2 = p >>> 1;
        int p2 = i2;
        if (a <= i2) {
            int i3 = p2 >>> 1;
            int p3 = i3;
            if (a > i3) {
                i = 1;
            } else {
                int i4 = p3 >>> 1;
                int p4 = i4;
                if (a > i4) {
                    i = 2;
                } else {
                    int i5 = p4 >>> 1;
                    int p5 = i5;
                    i = a > i5 ? 4 : 8;
                }
            }
        }
        return n - i;
    }

    private int tryTerminate(boolean now, boolean enable) {
        Unsafe unsafe;
        long j;
        int rs;
        while (true) {
            int i = this.runState;
            int rs2 = i;
            int i2 = 0;
            if (i < 0) {
                if ((rs2 & 2) != 0) {
                    long oldSum = 0;
                } else {
                    if (!now) {
                        long oldSum2 = 0;
                        loop1:
                        while (true) {
                            long checkSum = this.ctl;
                            if (((int) (checkSum >> 48)) + (this.config & SMASK) > 0) {
                                return 0;
                            }
                            WorkQueue[] workQueueArr = this.workQueues;
                            WorkQueue[] ws = workQueueArr;
                            if (workQueueArr != null) {
                                long checkSum2 = checkSum;
                                for (WorkQueue workQueue : ws) {
                                    WorkQueue w = workQueue;
                                    if (workQueue != null) {
                                        int i3 = w.base;
                                        int b = i3;
                                        checkSum2 += (long) i3;
                                        if (w.currentSteal != null) {
                                            break loop1;
                                        } else if (b != w.top) {
                                            break loop1;
                                        }
                                    }
                                }
                                checkSum = checkSum2;
                            }
                            long oldSum3 = checkSum;
                            if (oldSum2 == checkSum) {
                                break;
                            }
                            oldSum2 = oldSum3;
                        }
                        return 0;
                    }
                    do {
                        unsafe = U;
                        j = RUNSTATE;
                        rs = this.runState;
                    } while (!unsafe.compareAndSwapInt(this, j, rs, rs | 2));
                }
                long oldSum4 = 0;
                while (true) {
                    long oldSum5 = oldSum4;
                    long checkSum3 = this.ctl;
                    WorkQueue[] workQueueArr2 = this.workQueues;
                    WorkQueue[] ws2 = workQueueArr2;
                    if (workQueueArr2 != null) {
                        int i4 = i2;
                        while (true) {
                            int i5 = i4;
                            if (i5 >= ws2.length) {
                                break;
                            }
                            WorkQueue workQueue2 = ws2[i5];
                            WorkQueue w2 = workQueue2;
                            if (workQueue2 != null) {
                                w2.cancelAll();
                                checkSum3 += (long) w2.base;
                                if (w2.qlock >= 0) {
                                    w2.qlock = -1;
                                    ForkJoinWorkerThread forkJoinWorkerThread = w2.owner;
                                    ForkJoinWorkerThread wt = forkJoinWorkerThread;
                                    if (forkJoinWorkerThread != null) {
                                        try {
                                            wt.interrupt();
                                        } catch (Throwable th) {
                                        }
                                    }
                                }
                            }
                            i4 = i5 + 1;
                        }
                    }
                    oldSum4 = checkSum3;
                    if (oldSum5 == checkSum3) {
                        break;
                    }
                    i2 = 0;
                }
                if (((short) ((int) (this.ctl >>> 32))) + (this.config & SMASK) <= 0) {
                    this.runState = -2147483641;
                    synchronized (this) {
                        notifyAll();
                    }
                }
                return -1;
            } else if (enable && this != common) {
                if (rs2 == 0) {
                    tryInitialize(false);
                } else {
                    U.compareAndSwapInt(this, RUNSTATE, rs2, rs2 | Integer.MIN_VALUE);
                }
            }
        }
        return 1;
    }

    private void tryCreateExternalQueue(int index) {
        AuxState auxState2 = this.auxState;
        AuxState aux = auxState2;
        if (auxState2 != null && index >= 0) {
            WorkQueue q = new WorkQueue(this, null);
            q.config = index;
            q.scanState = Integer.MAX_VALUE;
            q.qlock = 1;
            boolean installed = false;
            aux.lock();
            try {
                WorkQueue[] workQueueArr = this.workQueues;
                WorkQueue[] ws = workQueueArr;
                if (workQueueArr != null && index < ws.length && ws[index] == null) {
                    ws[index] = q;
                    installed = true;
                }
                if (installed) {
                    try {
                        q.growArray();
                    } finally {
                        q.qlock = 0;
                    }
                }
            } finally {
                aux.unlock();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void externalPush(ForkJoinTask<?> task) {
        int probe = ThreadLocalRandom.getProbe();
        int r = probe;
        if (probe == 0) {
            ThreadLocalRandom.localInit();
            r = ThreadLocalRandom.getProbe();
        }
        while (true) {
            int rs = this.runState;
            WorkQueue[] ws = this.workQueues;
            if (rs > 0 && ws != null) {
                int length = ws.length;
                int wl = length;
                if (length > 0) {
                    int i = (wl - 1) & r & SQMASK;
                    int k = i;
                    WorkQueue workQueue = ws[i];
                    WorkQueue q = workQueue;
                    if (workQueue == null) {
                        tryCreateExternalQueue(k);
                    } else {
                        int sharedPush = q.sharedPush(task);
                        int stat = sharedPush;
                        if (sharedPush >= 0) {
                            if (stat == 0) {
                                signalWork();
                                return;
                            }
                            r = ThreadLocalRandom.advanceProbe(r);
                        } else {
                            return;
                        }
                    }
                }
            }
            tryInitialize(true);
        }
    }

    private <T> ForkJoinTask<T> externalSubmit(ForkJoinTask<T> task) {
        if (task != null) {
            Thread currentThread = Thread.currentThread();
            Thread t = currentThread;
            if (currentThread instanceof ForkJoinWorkerThread) {
                ForkJoinWorkerThread forkJoinWorkerThread = (ForkJoinWorkerThread) t;
                ForkJoinWorkerThread w = forkJoinWorkerThread;
                if (forkJoinWorkerThread.pool == this) {
                    WorkQueue workQueue = w.workQueue;
                    WorkQueue q = workQueue;
                    if (workQueue != null) {
                        q.push(task);
                        return task;
                    }
                }
            }
            externalPush(task);
            return task;
        }
        throw new NullPointerException();
    }

    static WorkQueue commonSubmitterQueue() {
        ForkJoinPool p = common;
        int r = ThreadLocalRandom.getProbe();
        if (p != null) {
            WorkQueue[] workQueueArr = p.workQueues;
            WorkQueue[] ws = workQueueArr;
            if (workQueueArr != null) {
                int length = ws.length;
                int wl = length;
                if (length > 0) {
                    return ws[(wl - 1) & r & SQMASK];
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final boolean tryExternalUnpush(ForkJoinTask<?> task) {
        int r = ThreadLocalRandom.getProbe();
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            int length = ws.length;
            int wl = length;
            if (length > 0) {
                WorkQueue workQueue = ws[(wl - 1) & r & SQMASK];
                WorkQueue w = workQueue;
                if (workQueue != null && w.trySharedUnpush(task)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final int externalHelpComplete(CountedCompleter<?> task, int maxTasks) {
        int r = ThreadLocalRandom.getProbe();
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            int length = ws.length;
            int wl = length;
            if (length > 0) {
                return helpComplete(ws[(wl - 1) & r & SQMASK], task, maxTasks);
            }
        }
        return 0;
    }

    public ForkJoinPool() {
        this(Math.min((int) MAX_CAP, Runtime.getRuntime().availableProcessors()), defaultForkJoinWorkerThreadFactory, null, false);
    }

    public ForkJoinPool(int parallelism) {
        this(parallelism, defaultForkJoinWorkerThreadFactory, null, false);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory2, Thread.UncaughtExceptionHandler handler, boolean asyncMode) {
        this(checkParallelism(parallelism), checkFactory(factory2), handler, asyncMode ? Integer.MIN_VALUE : 0, "ForkJoinPool-" + nextPoolId() + "-worker-");
        checkPermission();
    }

    private static int checkParallelism(int parallelism) {
        if (parallelism > 0 && parallelism <= MAX_CAP) {
            return parallelism;
        }
        throw new IllegalArgumentException();
    }

    private static ForkJoinWorkerThreadFactory checkFactory(ForkJoinWorkerThreadFactory factory2) {
        if (factory2 != null) {
            return factory2;
        }
        throw new NullPointerException();
    }

    private ForkJoinPool(int parallelism, ForkJoinWorkerThreadFactory factory2, Thread.UncaughtExceptionHandler handler, int mode, String workerNamePrefix2) {
        this.workerNamePrefix = workerNamePrefix2;
        this.factory = factory2;
        this.ueh = handler;
        this.config = (SMASK & parallelism) | mode;
        long np = (long) (-parallelism);
        this.ctl = ((np << 48) & AC_MASK) | ((np << 32) & TC_MASK);
    }

    public static ForkJoinPool commonPool() {
        return common;
    }

    public <T> T invoke(ForkJoinTask<T> task) {
        if (task != null) {
            externalSubmit(task);
            return task.join();
        }
        throw new NullPointerException();
    }

    public void execute(ForkJoinTask<?> task) {
        externalSubmit(task);
    }

    public void execute(Runnable task) {
        ForkJoinTask<?> job;
        if (task != null) {
            if (task instanceof ForkJoinTask) {
                job = (ForkJoinTask) task;
            } else {
                job = new ForkJoinTask.RunnableExecuteAction(task);
            }
            externalSubmit(job);
            return;
        }
        throw new NullPointerException();
    }

    public <T> ForkJoinTask<T> submit(ForkJoinTask<T> task) {
        return externalSubmit(task);
    }

    public <T> ForkJoinTask<T> submit(Callable<T> task) {
        return externalSubmit(new ForkJoinTask.AdaptedCallable(task));
    }

    public <T> ForkJoinTask<T> submit(Runnable task, T result) {
        return externalSubmit(new ForkJoinTask.AdaptedRunnable(task, result));
    }

    public ForkJoinTask<?> submit(Runnable task) {
        ForkJoinTask<?> job;
        if (task != null) {
            if (task instanceof ForkJoinTask) {
                job = (ForkJoinTask) task;
            } else {
                job = new ForkJoinTask.AdaptedRunnableAction(task);
            }
            return externalSubmit(job);
        }
        throw new NullPointerException();
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        ArrayList<Future<T>> futures = new ArrayList<>(tasks.size());
        try {
            for (Callable<T> t : tasks) {
                ForkJoinTask<T> f = new ForkJoinTask.AdaptedCallable<>(t);
                futures.add(f);
                externalSubmit(f);
            }
            int size = futures.size();
            for (int i = 0; i < size; i++) {
                ((ForkJoinTask) futures.get(i)).quietlyJoin();
            }
            return futures;
        } catch (Throwable t2) {
            int size2 = futures.size();
            for (int i2 = 0; i2 < size2; i2++) {
                futures.get(i2).cancel(false);
            }
            throw t2;
        }
    }

    public ForkJoinWorkerThreadFactory getFactory() {
        return this.factory;
    }

    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return this.ueh;
    }

    public int getParallelism() {
        int i = this.config & SMASK;
        int par = i;
        if (i > 0) {
            return par;
        }
        return 1;
    }

    public static int getCommonPoolParallelism() {
        return COMMON_PARALLELISM;
    }

    public int getPoolSize() {
        return (this.config & SMASK) + ((short) ((int) (this.ctl >>> 32)));
    }

    public boolean getAsyncMode() {
        return (this.config & Integer.MIN_VALUE) != 0;
    }

    public int getRunningThreadCount() {
        int rc = 0;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            for (int i = 1; i < ws.length; i += 2) {
                WorkQueue workQueue = ws[i];
                WorkQueue w = workQueue;
                if (workQueue != null && w.isApparentlyUnblocked()) {
                    rc++;
                }
            }
        }
        return rc;
    }

    public int getActiveThreadCount() {
        int r = (this.config & SMASK) + ((int) (this.ctl >> 48));
        if (r <= 0) {
            return 0;
        }
        return r;
    }

    public boolean isQuiescent() {
        return (this.config & SMASK) + ((int) (this.ctl >> 48)) <= 0;
    }

    public long getStealCount() {
        AuxState sc = this.auxState;
        long count = sc == null ? 0 : sc.stealCount;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            for (int i = 1; i < ws.length; i += 2) {
                WorkQueue workQueue = ws[i];
                WorkQueue w = workQueue;
                if (workQueue != null) {
                    count += (long) w.nsteals;
                }
            }
        }
        return count;
    }

    public long getQueuedTaskCount() {
        long count = 0;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            for (int i = 1; i < ws.length; i += 2) {
                WorkQueue workQueue = ws[i];
                WorkQueue w = workQueue;
                if (workQueue != null) {
                    count += (long) w.queueSize();
                }
            }
        }
        return count;
    }

    public int getQueuedSubmissionCount() {
        int count = 0;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            for (int i = 0; i < ws.length; i += 2) {
                WorkQueue workQueue = ws[i];
                WorkQueue w = workQueue;
                if (workQueue != null) {
                    count += w.queueSize();
                }
            }
        }
        return count;
    }

    public boolean hasQueuedSubmissions() {
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            for (int i = 0; i < ws.length; i += 2) {
                WorkQueue workQueue = ws[i];
                WorkQueue w = workQueue;
                if (workQueue != null && !w.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public ForkJoinTask<?> pollSubmission() {
        int nextSecondarySeed = ThreadLocalRandom.nextSecondarySeed();
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            int length = ws.length;
            int wl = length;
            if (length > 0) {
                int m = wl - 1;
                for (int i = 0; i < wl; i++) {
                    WorkQueue workQueue = ws[(i << 1) & m];
                    WorkQueue w = workQueue;
                    if (workQueue != null) {
                        ForkJoinTask<?> poll = w.poll();
                        ForkJoinTask<?> t = poll;
                        if (poll != null) {
                            return t;
                        }
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int drainTasksTo(Collection<? super ForkJoinTask<?>> c) {
        int count = 0;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            for (WorkQueue workQueue : ws) {
                WorkQueue w = workQueue;
                if (workQueue != null) {
                    while (true) {
                        ForkJoinTask<?> poll = w.poll();
                        ForkJoinTask<?> t = poll;
                        if (poll == null) {
                            break;
                        }
                        c.add(t);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public String toString() {
        String level;
        long c;
        long qt = 0;
        long qs = 0;
        int rc = 0;
        AuxState sc = this.auxState;
        long st = sc == null ? 0 : sc.stealCount;
        long c2 = this.ctl;
        WorkQueue[] workQueueArr = this.workQueues;
        WorkQueue[] ws = workQueueArr;
        if (workQueueArr != null) {
            int i = 0;
            while (i < ws.length) {
                WorkQueue workQueue = ws[i];
                WorkQueue w = workQueue;
                if (workQueue != null) {
                    int size = w.queueSize();
                    if ((i & 1) == 0) {
                        c = c2;
                        qs += (long) size;
                    } else {
                        c = c2;
                        qt += (long) size;
                        st += (long) w.nsteals;
                        if (w.isApparentlyUnblocked()) {
                            rc++;
                        }
                    }
                } else {
                    c = c2;
                }
                i++;
                c2 = c;
            }
        }
        long c3 = c2;
        int pc = this.config & SMASK;
        int tc = ((short) ((int) (c3 >>> 32))) + pc;
        int ac = ((int) (c3 >> 48)) + pc;
        if (ac < 0) {
            ac = 0;
        }
        int rs = this.runState;
        if ((rs & 4) != 0) {
            level = "Terminated";
        } else if ((rs & 2) != 0) {
            level = "Terminating";
        } else if ((Integer.MIN_VALUE & rs) != 0) {
            level = "Shutting down";
        } else {
            level = "Running";
        }
        StringBuilder sb = new StringBuilder();
        AuxState auxState2 = sc;
        sb.append(super.toString());
        sb.append("[");
        sb.append(level);
        sb.append(", parallelism = ");
        sb.append(pc);
        sb.append(", size = ");
        sb.append(tc);
        sb.append(", active = ");
        sb.append(ac);
        sb.append(", running = ");
        sb.append(rc);
        sb.append(", steals = ");
        sb.append(st);
        sb.append(", tasks = ");
        sb.append(qt);
        sb.append(", submissions = ");
        sb.append(qs);
        sb.append("]");
        return sb.toString();
    }

    public void shutdown() {
        checkPermission();
        tryTerminate(false, true);
    }

    public List<Runnable> shutdownNow() {
        checkPermission();
        tryTerminate(true, true);
        return Collections.emptyList();
    }

    public boolean isTerminated() {
        return (this.runState & 4) != 0;
    }

    public boolean isTerminating() {
        int rs = this.runState;
        return (rs & 2) != 0 && (rs & 4) == 0;
    }

    public boolean isShutdown() {
        return (this.runState & Integer.MIN_VALUE) != 0;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        if (!Thread.interrupted()) {
            boolean z = false;
            if (this == common) {
                awaitQuiescence(timeout, unit);
                return false;
            }
            long nanos = unit.toNanos(timeout);
            if (isTerminated()) {
                return true;
            }
            if (nanos <= 0) {
                return false;
            }
            long deadline = System.nanoTime() + nanos;
            synchronized (this) {
                while (!isTerminated()) {
                    if (nanos <= 0) {
                        return z;
                    }
                    long millis = TimeUnit.NANOSECONDS.toMillis(nanos);
                    wait(millis > 0 ? millis : 1);
                    nanos = deadline - System.nanoTime();
                    z = false;
                    long j = timeout;
                }
                return true;
            }
        }
        TimeUnit timeUnit = unit;
        throw new InterruptedException();
    }

    public boolean awaitQuiescence(long timeout, TimeUnit unit) {
        ForkJoinPool forkJoinPool = this;
        long nanos = unit.toNanos(timeout);
        Thread thread = Thread.currentThread();
        if (thread instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread forkJoinWorkerThread = (ForkJoinWorkerThread) thread;
            ForkJoinWorkerThread wt = forkJoinWorkerThread;
            if (forkJoinWorkerThread.pool == forkJoinPool) {
                forkJoinPool.helpQuiescePool(wt.workQueue);
                return true;
            }
        }
        long startTime = System.nanoTime();
        int r = 0;
        int b = 1;
        while (!isQuiescent()) {
            WorkQueue[] workQueueArr = forkJoinPool.workQueues;
            WorkQueue[] ws = workQueueArr;
            if (workQueueArr == null) {
                break;
            }
            int length = ws.length;
            int wl = length;
            if (length <= 0) {
                break;
            }
            if (b == 0) {
                if (System.nanoTime() - startTime > nanos) {
                    return false;
                }
                Thread.yield();
            }
            b = 0;
            int m = wl - 1;
            int j = (m + 1) << 2;
            while (true) {
                if (j < 0) {
                    break;
                }
                int r2 = r + 1;
                int r3 = r & m;
                int k = r3;
                if (r3 <= m && k >= 0) {
                    WorkQueue workQueue = ws[k];
                    WorkQueue q = workQueue;
                    if (workQueue != null) {
                        WorkQueue q2 = q;
                        int i = q2.base;
                        int b2 = i;
                        if (i - q2.top < 0) {
                            ForkJoinTask<?> pollAt = q2.pollAt(b2);
                            ForkJoinTask<?> t = pollAt;
                            if (pollAt != null) {
                                t.doExec();
                            }
                            b = 1;
                            r = r2;
                        }
                    } else {
                        continue;
                    }
                }
                j--;
                r = r2;
            }
            forkJoinPool = this;
        }
        return true;
    }

    static void quiesceCommonPool() {
        common.awaitQuiescence(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public static void managedBlock(ManagedBlocker blocker) throws InterruptedException {
        Thread t = Thread.currentThread();
        if (t instanceof ForkJoinWorkerThread) {
            ForkJoinWorkerThread forkJoinWorkerThread = (ForkJoinWorkerThread) t;
            ForkJoinWorkerThread wt = forkJoinWorkerThread;
            ForkJoinPool forkJoinPool = forkJoinWorkerThread.pool;
            ForkJoinPool p = forkJoinPool;
            if (forkJoinPool != null) {
                WorkQueue w = wt.workQueue;
                while (!blocker.isReleasable()) {
                    if (p.tryCompensate(w)) {
                        do {
                            try {
                                if (blocker.isReleasable()) {
                                    break;
                                }
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                U.getAndAddLong(p, CTL, AC_UNIT);
                                throw th2;
                            }
                        } while (!blocker.block());
                        U.getAndAddLong(p, CTL, AC_UNIT);
                        return;
                    }
                }
                return;
            }
        }
        while (!blocker.isReleasable()) {
            if (blocker.block()) {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ForkJoinTask.AdaptedRunnable(runnable, value);
    }

    /* access modifiers changed from: protected */
    public <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ForkJoinTask.AdaptedCallable(callable);
    }

    static {
        try {
            CTL = U.objectFieldOffset(ForkJoinPool.class.getDeclaredField("ctl"));
            RUNSTATE = U.objectFieldOffset(ForkJoinPool.class.getDeclaredField("runState"));
            ABASE = U.arrayBaseOffset(ForkJoinTask[].class);
            int scale = U.arrayIndexScale(ForkJoinTask[].class);
            if (((scale - 1) & scale) == 0) {
                ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
                Class<LockSupport> cls = LockSupport.class;
                int commonMaxSpares = 256;
                try {
                    String p = System.getProperty("java.util.concurrent.ForkJoinPool.common.maximumSpares");
                    if (p != null) {
                        commonMaxSpares = Integer.parseInt(p);
                    }
                } catch (Exception e) {
                }
                COMMON_MAX_SPARES = commonMaxSpares;
                return;
            }
            throw new Error("array index scale not a power of two");
        } catch (ReflectiveOperationException e2) {
            throw new Error((Throwable) e2);
        }
    }

    static ForkJoinPool makeCommonPool() {
        int parallelism = -1;
        ForkJoinWorkerThreadFactory factory2 = null;
        Thread.UncaughtExceptionHandler handler = null;
        try {
            String pp = System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism");
            String fp = System.getProperty("java.util.concurrent.ForkJoinPool.common.threadFactory");
            String hp = System.getProperty("java.util.concurrent.ForkJoinPool.common.exceptionHandler");
            if (pp != null) {
                parallelism = Integer.parseInt(pp);
            }
            if (fp != null) {
                factory2 = (ForkJoinWorkerThreadFactory) ClassLoader.getSystemClassLoader().loadClass(fp).newInstance();
            }
            if (hp != null) {
                handler = (Thread.UncaughtExceptionHandler) ClassLoader.getSystemClassLoader().loadClass(hp).newInstance();
            }
        } catch (Exception e) {
        }
        Thread.UncaughtExceptionHandler handler2 = handler;
        if (factory2 == null) {
            if (System.getSecurityManager() == null) {
                factory2 = defaultForkJoinWorkerThreadFactory;
            } else {
                factory2 = new InnocuousForkJoinWorkerThreadFactory();
            }
        }
        if (parallelism < 0) {
            int availableProcessors = Runtime.getRuntime().availableProcessors() - 1;
            parallelism = availableProcessors;
            if (availableProcessors <= 0) {
                parallelism = 1;
            }
        }
        if (parallelism > MAX_CAP) {
            parallelism = MAX_CAP;
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism, factory2, handler2, 0, "ForkJoinPool.commonPool-worker-");
        return forkJoinPool;
    }
}
