package java.util.concurrent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolExecutor extends AbstractExecutorService {
    private static final int CAPACITY = 536870911;
    private static final int COUNT_BITS = 29;
    private static final boolean ONLY_ONE = true;
    private static final int RUNNING = -536870912;
    private static final int SHUTDOWN = 0;
    private static final int STOP = 536870912;
    private static final int TERMINATED = 1610612736;
    private static final int TIDYING = 1073741824;
    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();
    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");
    private volatile boolean allowCoreThreadTimeOut;
    private long completedTaskCount;
    private volatile int corePoolSize;
    private final AtomicInteger ctl;
    private volatile RejectedExecutionHandler handler;
    private volatile long keepAliveTime;
    private int largestPoolSize;
    private final ReentrantLock mainLock;
    private volatile int maximumPoolSize;
    private final Condition termination;
    private volatile ThreadFactory threadFactory;
    private final BlockingQueue<Runnable> workQueue;
    private final HashSet<Worker> workers;

    public static class AbortPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + e.toString());
        }
    }

    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }

    public static class DiscardPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
        private static final long serialVersionUID = 6138294804551838833L;
        volatile long completedTasks;
        Runnable firstTask;
        final Thread thread;

        Worker(Runnable firstTask2) {
            setState(-1);
            this.firstTask = firstTask2;
            this.thread = ThreadPoolExecutor.this.getThreadFactory().newThread(this);
        }

        public void run() {
            ThreadPoolExecutor.this.runWorker(this);
        }

        /* access modifiers changed from: protected */
        public boolean isHeldExclusively() {
            if (getState() != 0) {
                return ThreadPoolExecutor.ONLY_ONE;
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean tryAcquire(int unused) {
            if (!compareAndSetState(0, 1)) {
                return false;
            }
            setExclusiveOwnerThread(Thread.currentThread());
            return ThreadPoolExecutor.ONLY_ONE;
        }

        /* access modifiers changed from: protected */
        public boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return ThreadPoolExecutor.ONLY_ONE;
        }

        public void lock() {
            acquire(1);
        }

        public boolean tryLock() {
            return tryAcquire(1);
        }

        public void unlock() {
            release(1);
        }

        public boolean isLocked() {
            return isHeldExclusively();
        }

        /* access modifiers changed from: package-private */
        public void interruptIfStarted() {
            if (getState() >= 0) {
                Thread thread2 = this.thread;
                Thread t = thread2;
                if (thread2 != null && !t.isInterrupted()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException e) {
                    }
                }
            }
        }
    }

    private static int runStateOf(int c) {
        return RUNNING & c;
    }

    private static int workerCountOf(int c) {
        return CAPACITY & c;
    }

    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    private static boolean runStateLessThan(int c, int s) {
        if (c < s) {
            return ONLY_ONE;
        }
        return false;
    }

    private static boolean runStateAtLeast(int c, int s) {
        if (c >= s) {
            return ONLY_ONE;
        }
        return false;
    }

    private static boolean isRunning(int c) {
        if (c < 0) {
            return ONLY_ONE;
        }
        return false;
    }

    private boolean compareAndIncrementWorkerCount(int expect) {
        return this.ctl.compareAndSet(expect, expect + 1);
    }

    private boolean compareAndDecrementWorkerCount(int expect) {
        return this.ctl.compareAndSet(expect, expect - 1);
    }

    private void decrementWorkerCount() {
        do {
        } while (!compareAndDecrementWorkerCount(this.ctl.get()));
    }

    private void advanceRunState(int targetState) {
        int c;
        do {
            c = this.ctl.get();
            if (runStateAtLeast(c, targetState)) {
                return;
            }
        } while (!this.ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))));
    }

    /* access modifiers changed from: package-private */
    public final void tryTerminate() {
        while (true) {
            int c = this.ctl.get();
            if (!isRunning(c) && !runStateAtLeast(c, TIDYING) && (runStateOf(c) != 0 || this.workQueue.isEmpty())) {
                if (workerCountOf(c) != 0) {
                    interruptIdleWorkers(ONLY_ONE);
                    return;
                }
                ReentrantLock mainLock2 = this.mainLock;
                mainLock2.lock();
                try {
                    if (this.ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                        terminated();
                        this.ctl.set(ctlOf(TERMINATED, 0));
                        this.termination.signalAll();
                        mainLock2.unlock();
                        return;
                    }
                    mainLock2.unlock();
                } catch (Throwable th) {
                    mainLock2.unlock();
                    throw th;
                }
            }
        }
    }

    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            ReentrantLock mainLock2 = this.mainLock;
            mainLock2.lock();
            try {
                Iterator<Worker> it = this.workers.iterator();
                while (it.hasNext()) {
                    security.checkAccess(it.next().thread);
                }
            } finally {
                mainLock2.unlock();
            }
        }
    }

    private void interruptWorkers() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            Iterator<Worker> it = this.workers.iterator();
            while (it.hasNext()) {
                it.next().interruptIfStarted();
            }
        } finally {
            mainLock2.unlock();
        }
    }

    private void interruptIdleWorkers(boolean onlyOne) {
        Worker w;
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            Iterator<Worker> it = this.workers.iterator();
            while (it.hasNext()) {
                w = it.next();
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    t.interrupt();
                    w.unlock();
                    continue;
                }
                if (onlyOne) {
                    break;
                }
            }
        } catch (SecurityException e) {
            w.unlock();
        } catch (Throwable th) {
            mainLock2.unlock();
            throw th;
        }
        mainLock2.unlock();
    }

    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    /* access modifiers changed from: package-private */
    public final void reject(Runnable command) {
        this.handler.rejectedExecution(command, this);
    }

    /* access modifiers changed from: package-private */
    public void onShutdown() {
    }

    /* access modifiers changed from: package-private */
    public final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(this.ctl.get());
        if (rs == RUNNING || (rs == 0 && shutdownOK)) {
            return ONLY_ONE;
        }
        return false;
    }

    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = this.workQueue;
        ArrayList<Runnable> taskList = new ArrayList<>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : (Runnable[]) q.toArray(new Runnable[0])) {
                if (q.remove(r)) {
                    taskList.add(r);
                }
            }
        }
        return taskList;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00a2, code lost:
        return false;
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        ReentrantLock mainLock2;
        loop0:
        while (true) {
            int c = this.ctl.get();
            int rs = runStateOf(c);
            if (rs < 0 || (rs == 0 && firstTask == null && !this.workQueue.isEmpty())) {
                while (true) {
                    int wc = workerCountOf(c);
                    if (wc >= CAPACITY) {
                        break loop0;
                    }
                    if (wc >= (core ? this.corePoolSize : this.maximumPoolSize)) {
                        break loop0;
                    } else if (compareAndIncrementWorkerCount(c)) {
                        boolean workerStarted = false;
                        boolean workerAdded = false;
                        Worker w = null;
                        try {
                            w = new Worker(firstTask);
                            Thread t = w.thread;
                            if (t != null) {
                                mainLock2 = this.mainLock;
                                mainLock2.lock();
                                int rs2 = runStateOf(this.ctl.get());
                                if (rs2 < 0 || (rs2 == 0 && firstTask == null)) {
                                    if (!t.isAlive()) {
                                        this.workers.add(w);
                                        int s = this.workers.size();
                                        if (s > this.largestPoolSize) {
                                            this.largestPoolSize = s;
                                        }
                                        workerAdded = ONLY_ONE;
                                    } else {
                                        throw new IllegalThreadStateException();
                                    }
                                }
                                mainLock2.unlock();
                                if (workerAdded) {
                                    t.start();
                                    workerStarted = ONLY_ONE;
                                }
                            }
                            if (!workerStarted) {
                                addWorkerFailed(w);
                            }
                            return workerStarted;
                        } catch (Throwable th) {
                            if (0 == 0) {
                                addWorkerFailed(w);
                            }
                            throw th;
                        }
                    } else {
                        c = this.ctl.get();
                        if (runStateOf(c) != rs) {
                        }
                    }
                }
            }
        }
        return false;
    }

    private void addWorkerFailed(Worker w) {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        if (w != null) {
            try {
                this.workers.remove(w);
            } catch (Throwable th) {
                mainLock2.unlock();
                throw th;
            }
        }
        decrementWorkerCount();
        tryTerminate();
        mainLock2.unlock();
    }

    /* JADX INFO: finally extract failed */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) {
            decrementWorkerCount();
        }
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            this.completedTaskCount += w.completedTasks;
            this.workers.remove(w);
            mainLock2.unlock();
            tryTerminate();
            int c = this.ctl.get();
            if (runStateLessThan(c, STOP)) {
                if (!completedAbruptly) {
                    int min = this.allowCoreThreadTimeOut ? 0 : this.corePoolSize;
                    if (min == 0 && !this.workQueue.isEmpty()) {
                        min = 1;
                    }
                    if (workerCountOf(c) >= min) {
                        return;
                    }
                }
                addWorker(null, false);
            }
        } catch (Throwable th) {
            mainLock2.unlock();
            throw th;
        }
    }

    private Runnable getTask() {
        Runnable r;
        boolean timedOut = false;
        while (true) {
            int c = this.ctl.get();
            int rs = runStateOf(c);
            if (rs < 0 || (rs < STOP && !this.workQueue.isEmpty())) {
                int wc = workerCountOf(c);
                boolean timed = this.allowCoreThreadTimeOut || wc > this.corePoolSize;
                if ((wc <= this.maximumPoolSize && (!timed || !timedOut)) || (wc <= 1 && !this.workQueue.isEmpty())) {
                    if (timed) {
                        try {
                            r = this.workQueue.poll(this.keepAliveTime, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            timedOut = false;
                        }
                    } else {
                        r = this.workQueue.take();
                    }
                    if (r != null) {
                        return r;
                    }
                    timedOut = ONLY_ONE;
                } else if (compareAndDecrementWorkerCount(c)) {
                    return null;
                }
            }
        }
        decrementWorkerCount();
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        boolean isFirstTask = ONLY_ONE;
        boolean completedAbruptly = ONLY_ONE;
        while (processTask(wt, w, isFirstTask)) {
            try {
                isFirstTask = false;
            } finally {
                processWorkerExit(w, completedAbruptly);
            }
        }
        completedAbruptly = false;
    }

    private boolean processTask(Thread wt, Worker w, boolean isFirstTask) {
        Runnable task = null;
        Throwable thrown = null;
        if (isFirstTask) {
            task = w.firstTask;
            w.firstTask = null;
            w.unlock();
        }
        if (task == null) {
            Runnable task2 = getTask();
            task = task2;
            if (task2 == null) {
                return false;
            }
        }
        w.lock();
        if ((runStateAtLeast(this.ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(this.ctl.get(), STOP))) && !wt.isInterrupted()) {
            wt.interrupt();
        }
        try {
            beforeExecute(wt, task);
            task.run();
            afterExecute(task, null);
            w.completedTasks++;
            w.unlock();
            return ONLY_ONE;
        } catch (RuntimeException x) {
            Throwable thrown2 = x;
            throw x;
        } catch (Error x2) {
            Throwable thrown3 = x2;
            throw x2;
        } catch (Throwable thrown4) {
            w.completedTasks++;
            w.unlock();
            throw thrown4;
        }
    }

    public ThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime2, TimeUnit unit, BlockingQueue<Runnable> workQueue2) {
        this(corePoolSize2, maximumPoolSize2, keepAliveTime2, unit, workQueue2, Executors.defaultThreadFactory(), defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime2, TimeUnit unit, BlockingQueue<Runnable> workQueue2, ThreadFactory threadFactory2) {
        this(corePoolSize2, maximumPoolSize2, keepAliveTime2, unit, workQueue2, threadFactory2, defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime2, TimeUnit unit, BlockingQueue<Runnable> workQueue2, RejectedExecutionHandler handler2) {
        this(corePoolSize2, maximumPoolSize2, keepAliveTime2, unit, workQueue2, Executors.defaultThreadFactory(), handler2);
    }

    public ThreadPoolExecutor(int corePoolSize2, int maximumPoolSize2, long keepAliveTime2, TimeUnit unit, BlockingQueue<Runnable> workQueue2, ThreadFactory threadFactory2, RejectedExecutionHandler handler2) {
        this.ctl = new AtomicInteger(ctlOf(RUNNING, 0));
        this.mainLock = new ReentrantLock();
        this.workers = new HashSet<>();
        this.termination = this.mainLock.newCondition();
        if (corePoolSize2 < 0 || maximumPoolSize2 <= 0 || maximumPoolSize2 < corePoolSize2 || keepAliveTime2 < 0) {
            throw new IllegalArgumentException();
        } else if (workQueue2 == null || threadFactory2 == null || handler2 == null) {
            throw new NullPointerException();
        } else {
            this.corePoolSize = corePoolSize2;
            this.maximumPoolSize = maximumPoolSize2;
            this.workQueue = workQueue2;
            this.keepAliveTime = unit.toNanos(keepAliveTime2);
            this.threadFactory = threadFactory2;
            this.handler = handler2;
        }
    }

    public void execute(Runnable command) {
        if (command != null) {
            int c = this.ctl.get();
            if (workerCountOf(c) < this.corePoolSize) {
                if (!addWorker(command, ONLY_ONE)) {
                    c = this.ctl.get();
                } else {
                    return;
                }
            }
            if (isRunning(c) && this.workQueue.offer(command)) {
                int recheck = this.ctl.get();
                if (!isRunning(recheck) && remove(command)) {
                    reject(command);
                } else if (workerCountOf(recheck) == 0) {
                    addWorker(null, false);
                }
            } else if (!addWorker(command, false)) {
                reject(command);
            }
            return;
        }
        throw new NullPointerException();
    }

    /* JADX INFO: finally extract failed */
    public void shutdown() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            checkShutdownAccess();
            advanceRunState(0);
            interruptIdleWorkers();
            onShutdown();
            mainLock2.unlock();
            tryTerminate();
        } catch (Throwable th) {
            mainLock2.unlock();
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    public List<Runnable> shutdownNow() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            List<Runnable> tasks = drainQueue();
            mainLock2.unlock();
            tryTerminate();
            return tasks;
        } catch (Throwable th) {
            mainLock2.unlock();
            throw th;
        }
    }

    public boolean isShutdown() {
        return isRunning(this.ctl.get()) ^ ONLY_ONE;
    }

    public boolean isTerminating() {
        int c = this.ctl.get();
        if (isRunning(c) || !runStateLessThan(c, TERMINATED)) {
            return false;
        }
        return ONLY_ONE;
    }

    public boolean isTerminated() {
        return runStateAtLeast(this.ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        while (!runStateAtLeast(this.ctl.get(), TERMINATED)) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.termination.awaitNanos(nanos);
            } finally {
                mainLock2.unlock();
            }
        }
        mainLock2.unlock();
        return ONLY_ONE;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        shutdown();
    }

    public void setThreadFactory(ThreadFactory threadFactory2) {
        if (threadFactory2 != null) {
            this.threadFactory = threadFactory2;
            return;
        }
        throw new NullPointerException();
    }

    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler2) {
        if (handler2 != null) {
            this.handler = handler2;
            return;
        }
        throw new NullPointerException();
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return this.handler;
    }

    public void setCorePoolSize(int corePoolSize2) {
        if (corePoolSize2 >= 0) {
            int delta = corePoolSize2 - this.corePoolSize;
            this.corePoolSize = corePoolSize2;
            if (workerCountOf(this.ctl.get()) > corePoolSize2) {
                interruptIdleWorkers();
            } else if (delta > 0) {
                int k = Math.min(delta, this.workQueue.size());
                while (true) {
                    int k2 = k - 1;
                    if (k > 0 && addWorker(null, ONLY_ONE) && !this.workQueue.isEmpty()) {
                        k = k2;
                    } else {
                        return;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    public boolean prestartCoreThread() {
        if (workerCountOf(this.ctl.get()) >= this.corePoolSize || !addWorker(null, ONLY_ONE)) {
            return false;
        }
        return ONLY_ONE;
    }

    /* access modifiers changed from: package-private */
    public void ensurePrestart() {
        int wc = workerCountOf(this.ctl.get());
        if (wc < this.corePoolSize) {
            addWorker(null, ONLY_ONE);
        } else if (wc == 0) {
            addWorker(null, false);
        }
    }

    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, ONLY_ONE)) {
            n++;
        }
        return n;
    }

    public boolean allowsCoreThreadTimeOut() {
        return this.allowCoreThreadTimeOut;
    }

    public void allowCoreThreadTimeOut(boolean value) {
        if (value && this.keepAliveTime <= 0) {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        } else if (value != this.allowCoreThreadTimeOut) {
            this.allowCoreThreadTimeOut = value;
            if (value) {
                interruptIdleWorkers();
            }
        }
    }

    public void setMaximumPoolSize(int maximumPoolSize2) {
        if (maximumPoolSize2 <= 0 || maximumPoolSize2 < this.corePoolSize) {
            throw new IllegalArgumentException();
        }
        this.maximumPoolSize = maximumPoolSize2;
        if (workerCountOf(this.ctl.get()) > maximumPoolSize2) {
            interruptIdleWorkers();
        }
    }

    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException();
        } else if (time != 0 || !allowsCoreThreadTimeOut()) {
            long keepAliveTime2 = unit.toNanos(time);
            this.keepAliveTime = keepAliveTime2;
            if (keepAliveTime2 - this.keepAliveTime < 0) {
                interruptIdleWorkers();
            }
        } else {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        }
    }

    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(this.keepAliveTime, TimeUnit.NANOSECONDS);
    }

    public BlockingQueue<Runnable> getQueue() {
        return this.workQueue;
    }

    public boolean remove(Runnable task) {
        boolean removed = this.workQueue.remove(task);
        tryTerminate();
        return removed;
    }

    public void purge() {
        BlockingQueue<Runnable> q = this.workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if ((r instanceof Future) && ((Future) r).isCancelled()) {
                    it.remove();
                }
            }
        } catch (ConcurrentModificationException e) {
            for (Object r2 : q.toArray()) {
                if ((r2 instanceof Future) && ((Future) r2).isCancelled()) {
                    q.remove(r2);
                }
            }
        }
        tryTerminate();
    }

    public int getPoolSize() {
        int i;
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            if (runStateAtLeast(this.ctl.get(), TIDYING)) {
                i = 0;
            } else {
                i = this.workers.size();
            }
            return i;
        } finally {
            mainLock2.unlock();
        }
    }

    public int getActiveCount() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        int n = 0;
        try {
            Iterator<Worker> it = this.workers.iterator();
            while (it.hasNext()) {
                if (it.next().isLocked()) {
                    n++;
                }
            }
            return n;
        } finally {
            mainLock2.unlock();
        }
    }

    public int getLargestPoolSize() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            return this.largestPoolSize;
        } finally {
            mainLock2.unlock();
        }
    }

    public long getTaskCount() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            long n = this.completedTaskCount;
            Iterator<Worker> it = this.workers.iterator();
            while (it.hasNext()) {
                Worker w = it.next();
                n += w.completedTasks;
                if (w.isLocked()) {
                    n++;
                }
            }
            return ((long) this.workQueue.size()) + n;
        } finally {
            mainLock2.unlock();
        }
    }

    public long getCompletedTaskCount() {
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            long n = this.completedTaskCount;
            Iterator<Worker> it = this.workers.iterator();
            while (it.hasNext()) {
                n += it.next().completedTasks;
            }
            return n;
        } finally {
            mainLock2.unlock();
        }
    }

    /* JADX INFO: finally extract failed */
    public String toString() {
        String runState;
        ReentrantLock mainLock2 = this.mainLock;
        mainLock2.lock();
        try {
            long ncompleted = this.completedTaskCount;
            int nactive = 0;
            int nworkers = this.workers.size();
            Iterator<Worker> it = this.workers.iterator();
            while (it.hasNext()) {
                Worker w = it.next();
                ncompleted += w.completedTasks;
                if (w.isLocked()) {
                    nactive++;
                }
            }
            mainLock2.unlock();
            int c = this.ctl.get();
            if (runStateLessThan(c, 0)) {
                runState = "Running";
            } else if (runStateAtLeast(c, TERMINATED)) {
                runState = "Terminated";
            } else {
                runState = "Shutting down";
            }
            return super.toString() + "[" + runState + ", pool size = " + nworkers + ", active threads = " + nactive + ", queued tasks = " + this.workQueue.size() + ", completed tasks = " + ncompleted + "]";
        } catch (Throwable th) {
            mainLock2.unlock();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void beforeExecute(Thread t, Runnable r) {
    }

    /* access modifiers changed from: protected */
    public void afterExecute(Runnable r, Throwable t) {
    }

    /* access modifiers changed from: protected */
    public void terminated() {
    }
}
