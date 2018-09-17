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

        Worker(Runnable firstTask) {
            setState(-1);
            this.firstTask = firstTask;
            this.thread = ThreadPoolExecutor.this.getThreadFactory().newThread(this);
        }

        public void run() {
            ThreadPoolExecutor.this.runWorker(this);
        }

        protected boolean isHeldExclusively() {
            return getState() != 0 ? ThreadPoolExecutor.ONLY_ONE : false;
        }

        protected boolean tryAcquire(int unused) {
            if (!compareAndSetState(0, 1)) {
                return false;
            }
            setExclusiveOwnerThread(Thread.currentThread());
            return ThreadPoolExecutor.ONLY_ONE;
        }

        protected boolean tryRelease(int unused) {
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

        void interruptIfStarted() {
            if (getState() >= 0) {
                Thread t = this.thread;
                if (t != null && (t.isInterrupted() ^ 1) != 0) {
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
        return c < s ? ONLY_ONE : false;
    }

    private static boolean runStateAtLeast(int c, int s) {
        return c >= s ? ONLY_ONE : false;
    }

    private static boolean isRunning(int c) {
        return c < 0 ? ONLY_ONE : false;
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

    final void tryTerminate() {
        while (true) {
            int c = this.ctl.get();
            if (!isRunning(c) && !runStateAtLeast(c, TIDYING) && (runStateOf(c) != 0 || (this.workQueue.isEmpty() ^ 1) == 0)) {
                if (workerCountOf(c) != 0) {
                    interruptIdleWorkers(ONLY_ONE);
                    return;
                }
                ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    if (this.ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
                        terminated();
                        this.ctl.set(ctlOf(TERMINATED, 0));
                        this.termination.signalAll();
                        mainLock.unlock();
                        return;
                    }
                    mainLock.unlock();
                } catch (Throwable th) {
                    mainLock.unlock();
                }
            }
        }
    }

    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : this.workers) {
                    security.checkAccess(w.thread);
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    private void interruptWorkers() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : this.workers) {
                w.interruptIfStarted();
            }
        } finally {
            mainLock.unlock();
        }
    }

    private void interruptIdleWorkers(boolean onlyOne) {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : this.workers) {
                Thread t = w.thread;
                if (!t.isInterrupted() && w.tryLock()) {
                    t.interrupt();
                    w.unlock();
                }
                if (onlyOne) {
                    break;
                }
            }
        } catch (SecurityException e) {
            w.unlock();
        } catch (Throwable th) {
            mainLock.unlock();
        }
        mainLock.unlock();
    }

    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    final void reject(Runnable command) {
        this.handler.rejectedExecution(command, this);
    }

    void onShutdown() {
    }

    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(this.ctl.get());
        if (rs != RUNNING) {
            return rs == 0 ? shutdownOK : false;
        } else {
            return ONLY_ONE;
        }
    }

    private List<Runnable> drainQueue() {
        int i = 0;
        BlockingQueue<Runnable> q = this.workQueue;
        ArrayList<Runnable> taskList = new ArrayList();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            Runnable[] runnableArr = (Runnable[]) q.toArray(new Runnable[0]);
            int length = runnableArr.length;
            while (i < length) {
                Runnable r = runnableArr[i];
                if (q.remove(r)) {
                    taskList.add(r);
                }
                i++;
            }
        }
        return taskList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x0069  */
    /* JADX WARNING: Missing block: B:13:0x0029, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean addWorker(Runnable firstTask, boolean core) {
        Throwable th;
        loop0:
        while (true) {
            int c = this.ctl.get();
            int rs = runStateOf(c);
            if (rs < 0 || (rs == 0 && firstTask == null && !this.workQueue.isEmpty())) {
                do {
                    int wc = workerCountOf(c);
                    if (wc >= CAPACITY) {
                        break loop0;
                    }
                    if (wc >= (core ? this.corePoolSize : this.maximumPoolSize)) {
                        break loop0;
                    } else if (compareAndIncrementWorkerCount(c)) {
                        boolean workerStarted = false;
                        boolean workerAdded = false;
                        Worker worker = null;
                        try {
                            Worker w = new Worker(firstTask);
                            ReentrantLock mainLock;
                            try {
                                Thread t = w.thread;
                                if (t != null) {
                                    mainLock = this.mainLock;
                                    mainLock.lock();
                                    rs = runStateOf(this.ctl.get());
                                    if (rs < 0 || (rs == 0 && firstTask == null)) {
                                        if (t.isAlive()) {
                                            throw new IllegalThreadStateException();
                                        }
                                        this.workers.add(w);
                                        int s = this.workers.size();
                                        if (s > this.largestPoolSize) {
                                            this.largestPoolSize = s;
                                        }
                                        workerAdded = ONLY_ONE;
                                    }
                                    mainLock.unlock();
                                    if (workerAdded) {
                                        t.start();
                                        workerStarted = ONLY_ONE;
                                    }
                                }
                                if (!workerStarted) {
                                    addWorkerFailed(w);
                                }
                                return workerStarted;
                            } catch (Throwable th2) {
                                th = th2;
                                worker = w;
                                if (null == null) {
                                    addWorkerFailed(worker);
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (null == null) {
                            }
                            throw th;
                        }
                    } else {
                        c = this.ctl.get();
                    }
                } while (runStateOf(c) == rs);
            }
        }
        return false;
    }

    private void addWorkerFailed(Worker w) {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        if (w != null) {
            try {
                this.workers.remove(w);
            } catch (Throwable th) {
                mainLock.unlock();
            }
        }
        decrementWorkerCount();
        tryTerminate();
        mainLock.unlock();
    }

    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        if (completedAbruptly) {
            decrementWorkerCount();
        }
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            this.completedTaskCount += w.completedTasks;
            this.workers.remove(w);
            tryTerminate();
            int c = this.ctl.get();
            if (runStateLessThan(c, STOP)) {
                if (!completedAbruptly) {
                    int min = this.allowCoreThreadTimeOut ? 0 : this.corePoolSize;
                    if (min == 0 && (this.workQueue.isEmpty() ^ 1) != 0) {
                        min = 1;
                    }
                    if (workerCountOf(c) >= min) {
                        return;
                    }
                }
                addWorker(null, false);
            }
        } finally {
            mainLock.unlock();
        }
    }

    private Runnable getTask() {
        boolean timedOut = false;
        while (true) {
            int c = this.ctl.get();
            int rs = runStateOf(c);
            if (rs < 0 || (rs < STOP && !this.workQueue.isEmpty())) {
                int wc = workerCountOf(c);
                boolean timed = (this.allowCoreThreadTimeOut || wc > this.corePoolSize) ? ONLY_ONE : false;
                if ((wc <= this.maximumPoolSize && (!timed || !timedOut)) || (wc <= 1 && !this.workQueue.isEmpty())) {
                    Runnable r;
                    if (timed) {
                        try {
                            r = (Runnable) this.workQueue.poll(this.keepAliveTime, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            timedOut = false;
                        }
                    } else {
                        r = (Runnable) this.workQueue.take();
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

    /* JADX WARNING: Missing block: B:47:0x007e, code:
            r0 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock();
        boolean completedAbruptly = ONLY_ONE;
        loop0:
        while (true) {
            if (task == null) {
                try {
                    task = getTask();
                    if (task == null) {
                        break loop0;
                    }
                } finally {
                    processWorkerExit(w, completedAbruptly);
                }
            }
            w.lock();
            if ((runStateAtLeast(this.ctl.get(), STOP) || (Thread.interrupted() && runStateAtLeast(this.ctl.get(), STOP))) && (wt.isInterrupted() ^ 1) != 0) {
                wt.interrupt();
            }
            try {
                beforeExecute(wt, task);
                Throwable thrown;
                try {
                    task.run();
                    afterExecute(task, thrown);
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                } catch (Throwable x) {
                    thrown = x;
                    throw x;
                } catch (Throwable x2) {
                    thrown = x2;
                    throw x2;
                } catch (Throwable th) {
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                w.completedTasks++;
                w.unlock();
            }
        }
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        this.ctl = new AtomicInteger(ctlOf(RUNNING, 0));
        this.mainLock = new ReentrantLock();
        this.workers = new HashSet();
        this.termination = this.mainLock.newCondition();
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
            throw new IllegalArgumentException();
        } else if (workQueue == null || threadFactory == null || handler == null) {
            throw new NullPointerException();
        } else {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.workQueue = workQueue;
            this.keepAliveTime = unit.toNanos(keepAliveTime);
            this.threadFactory = threadFactory;
            this.handler = handler;
        }
    }

    public void execute(Runnable command) {
        if (command == null) {
            throw new NullPointerException();
        }
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
    }

    public void shutdown() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(0);
            interruptIdleWorkers();
            onShutdown();
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
    }

    public List<Runnable> shutdownNow() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            List<Runnable> tasks = drainQueue();
            tryTerminate();
            return tasks;
        } finally {
            mainLock.unlock();
        }
    }

    public boolean isShutdown() {
        return isRunning(this.ctl.get()) ^ 1;
    }

    public boolean isTerminating() {
        int c = this.ctl.get();
        return !isRunning(c) ? runStateLessThan(c, TERMINATED) : false;
    }

    public boolean isTerminated() {
        return runStateAtLeast(this.ctl.get(), TERMINATED);
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        while (!runStateAtLeast(this.ctl.get(), TERMINATED)) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.termination.awaitNanos(nanos);
            } finally {
                mainLock.unlock();
            }
        }
        mainLock.unlock();
        return ONLY_ONE;
    }

    protected void finalize() {
        shutdown();
    }

    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException();
        }
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return this.handler;
    }

    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException();
        }
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(this.ctl.get()) > corePoolSize) {
            interruptIdleWorkers();
        } else if (delta > 0) {
            int k = Math.min(delta, this.workQueue.size());
            while (true) {
                int k2 = k;
                k = k2 - 1;
                if (k2 <= 0 || !addWorker(null, ONLY_ONE) || this.workQueue.isEmpty()) {
                    return;
                }
            }
        }
    }

    public int getCorePoolSize() {
        return this.corePoolSize;
    }

    public boolean prestartCoreThread() {
        if (workerCountOf(this.ctl.get()) < this.corePoolSize) {
            return addWorker(null, ONLY_ONE);
        }
        return false;
    }

    void ensurePrestart() {
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

    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < this.corePoolSize) {
            throw new IllegalArgumentException();
        }
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(this.ctl.get()) > maximumPoolSize) {
            interruptIdleWorkers();
        }
    }

    public int getMaximumPoolSize() {
        return this.maximumPoolSize;
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException();
        } else if (time == 0 && allowsCoreThreadTimeOut()) {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        } else {
            long keepAliveTime = unit.toNanos(time);
            long delta = keepAliveTime - this.keepAliveTime;
            this.keepAliveTime = keepAliveTime;
            if (delta < 0) {
                interruptIdleWorkers();
            }
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
                Runnable r = (Runnable) it.next();
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
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int i;
            if (runStateAtLeast(this.ctl.get(), TIDYING)) {
                i = 0;
            } else {
                i = this.workers.size();
            }
            mainLock.unlock();
            return i;
        } catch (Throwable th) {
            mainLock.unlock();
        }
    }

    public int getActiveCount() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        int n = 0;
        try {
            for (Worker w : this.workers) {
                if (w.isLocked()) {
                    n++;
                }
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public int getLargestPoolSize() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int i = this.largestPoolSize;
            return i;
        } finally {
            mainLock.unlock();
        }
    }

    public long getTaskCount() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = this.completedTaskCount;
            for (Worker w : this.workers) {
                n += w.completedTasks;
                if (w.isLocked()) {
                    n++;
                }
            }
            long size = ((long) this.workQueue.size()) + n;
            return size;
        } finally {
            mainLock.unlock();
        }
    }

    public long getCompletedTaskCount() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = this.completedTaskCount;
            for (Worker w : this.workers) {
                n += w.completedTasks;
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    public String toString() {
        ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            String runState;
            long ncompleted = this.completedTaskCount;
            int nactive = 0;
            int nworkers = this.workers.size();
            for (Worker w : this.workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked()) {
                    nactive++;
                }
            }
            int c = this.ctl.get();
            if (runStateLessThan(c, 0)) {
                runState = "Running";
            } else if (runStateAtLeast(c, TERMINATED)) {
                runState = "Terminated";
            } else {
                runState = "Shutting down";
            }
            return super.toString() + "[" + runState + ", pool size = " + nworkers + ", active threads = " + nactive + ", queued tasks = " + this.workQueue.size() + ", completed tasks = " + ncompleted + "]";
        } finally {
            mainLock.unlock();
        }
    }

    protected void beforeExecute(Thread t, Runnable r) {
    }

    protected void afterExecute(Runnable r, Throwable t) {
    }

    protected void terminated() {
    }
}
