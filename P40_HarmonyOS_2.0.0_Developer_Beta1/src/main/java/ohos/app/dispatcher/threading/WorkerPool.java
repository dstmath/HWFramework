package ohos.app.dispatcher.threading;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import ohos.app.dispatcher.threading.WorkerThread;
import ohos.appexecfwk.utils.AppLog;

public abstract class WorkerPool {
    private static final int CAPACITY = 536870911;
    private static final int CLEANED = 1073741824;
    private static final int CLOSED = 1610612736;
    private static final int CLOSING = 0;
    private static final int CORE_THREAD_LOWER_LIMIT = 0;
    private static final int COUNT_BITS = 29;
    private static final int INTERRUPT = 536870912;
    private static final int MAX_THREAD_LOWER_LIMIT = 1;
    private static final int RUNNING = -536870912;
    private static final int THREAD_UPPER_LIMIT = 256;
    private long aliveTimeLimit;
    private final AtomicInteger control;
    private int coreThreadLimit = 0;
    private final ThreadFactory factory = new DefaultThreadFactory();
    private final HashSet<WorkerThread> pool = new HashSet<>();
    private final ReentrantLock poolLock = new ReentrantLock();
    private int threadLimit = 0;

    private boolean checkCoreThreadCount(int i) {
        return i <= 256 && i >= 0;
    }

    private boolean checkMaxThreadCount(int i) {
        return i <= 256 && i >= 1;
    }

    private boolean checkThreadCount(int i, int i2) {
        return i >= i2;
    }

    private static int combineToControl(int i, int i2) {
        return i | i2;
    }

    private static int getStateFromControl(int i) {
        return i & RUNNING;
    }

    private static int getWorkingThreadNum(int i) {
        return i & CAPACITY;
    }

    private static boolean isRunning(int i) {
        return i < 0;
    }

    /* access modifiers changed from: protected */
    public void afterRun(Runnable runnable) {
    }

    /* access modifiers changed from: protected */
    public void beforeRun(Runnable runnable) {
    }

    public WorkerPool(WorkerPoolConfig workerPoolConfig) {
        long j = 0;
        this.aliveTimeLimit = 0;
        this.control = new AtomicInteger(combineToControl(RUNNING, 0));
        if (workerPoolConfig == null) {
            throw new NullPointerException("WorkerPool::checkConfigParams WorkerPoolConfig cannot be null");
        } else if (checkConfigParams(workerPoolConfig).booleanValue()) {
            this.threadLimit = workerPoolConfig.getMaxThreadCount();
            this.coreThreadLimit = workerPoolConfig.getCoreThreadCount();
            long keepAliveTime = workerPoolConfig.getKeepAliveTime();
            this.aliveTimeLimit = keepAliveTime > 0 ? keepAliveTime : j;
        } else {
            throw new IllegalArgumentException("WorkerPool::checkConfigParams parameters are illegal");
        }
    }

    private Boolean checkConfigParams(WorkerPoolConfig workerPoolConfig) {
        int maxThreadCount = workerPoolConfig.getMaxThreadCount();
        int coreThreadCount = workerPoolConfig.getCoreThreadCount();
        if (!checkThreadCount(maxThreadCount, coreThreadCount)) {
            AppLog.e("WorkerPool::checkConfigParams parameters are illegal, maxThreadCount %{public}d is less than coreThreadCount %{public}d", Integer.valueOf(maxThreadCount), Integer.valueOf(coreThreadCount));
            return false;
        } else if (!checkMaxThreadCount(maxThreadCount)) {
            AppLog.e("WorkerPool::checkConfigParams maxThreadCount %{public}d is illegal", Integer.valueOf(maxThreadCount));
            return false;
        } else if (checkCoreThreadCount(coreThreadCount)) {
            return true;
        } else {
            AppLog.e("WorkerPool::checkConfigParams coreThreadCount %{public}d is illegal", Integer.valueOf(coreThreadCount));
            return false;
        }
    }

    public long getKeepAliveTime() {
        return this.aliveTimeLimit;
    }

    public int getCoreThreadCount() {
        return this.coreThreadLimit;
    }

    public int getMaxThreadCount() {
        return this.threadLimit;
    }

    public int getWorkCount() {
        return getWorkingThreadNum(this.control.get());
    }

    public Map<String, Long> getWorkerThreadsInfo() {
        HashMap hashMap = new HashMap();
        ReentrantLock reentrantLock = this.poolLock;
        reentrantLock.lock();
        try {
            Iterator<WorkerThread> it = this.pool.iterator();
            while (it.hasNext()) {
                WorkerThread next = it.next();
                hashMap.put(next.getThreadName(), Long.valueOf(next.getTaskCounter()));
            }
            return hashMap;
        } finally {
            reentrantLock.unlock();
        }
    }

    /* access modifiers changed from: protected */
    public final void closePool(boolean z) {
        ReentrantLock reentrantLock = this.poolLock;
        reentrantLock.lock();
        try {
            AppLog.i("WorkerPool::closePool start", new Object[0]);
            advanceStateTo(0);
            interruptWorkers();
            AppLog.i("WorkerPool::closePool end", new Object[0]);
        } finally {
            reentrantLock.unlock();
        }
    }

    private void interruptWorkers() {
        Object[] objArr;
        ReentrantLock reentrantLock = this.poolLock;
        reentrantLock.lock();
        try {
            Iterator<WorkerThread> it = this.pool.iterator();
            while (it.hasNext()) {
                Thread thread = it.next().thread;
                if (thread != null && !thread.isInterrupted()) {
                    try {
                        thread.interrupt();
                        objArr = new Object[]{thread.getName()};
                    } catch (SecurityException unused) {
                        AppLog.w("WorkerPool::interruptWorkers has SecurityException", new Object[0]);
                        objArr = new Object[]{thread.getName()};
                    } catch (Throwable th) {
                        AppLog.d("WorkerPool::interruptWorkers interrupt a thread: %{public}s", thread.getName());
                        throw th;
                    }
                    AppLog.d("WorkerPool::interruptWorkers interrupt a thread: %{public}s", objArr);
                }
            }
        } finally {
            reentrantLock.unlock();
        }
    }

    /* JADX INFO: finally extract failed */
    public boolean addWorker(WorkerThread.Delegate delegate, Runnable runnable) {
        WorkerThread workerThread;
        boolean z = false;
        AppLog.i("WorkerPool::addWorker", new Object[0]);
        ReentrantLock reentrantLock = this.poolLock;
        while (true) {
            int workingThreadNum = getWorkingThreadNum(this.control.get());
            if (workingThreadNum < this.threadLimit) {
                if (!isRunning(this.control.get())) {
                    workerThread = null;
                    break;
                } else if (compareAndIncThreadNum(workingThreadNum)) {
                    WorkerThread workerThread2 = new WorkerThread(delegate, runnable, this.factory);
                    reentrantLock.lock();
                    try {
                        this.pool.add(workerThread2);
                        reentrantLock.unlock();
                        z = true;
                        workerThread = workerThread2;
                        break;
                    } catch (Throwable th) {
                        reentrantLock.unlock();
                        throw th;
                    }
                } else {
                    AppLog.d("WorkerPool::addWorker retry addWorker", new Object[0]);
                }
            } else {
                break;
            }
        }
        if (z) {
            workerThread.thread.start();
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void onWorkerExit(WorkerThread workerThread, boolean z) {
        ReentrantLock reentrantLock = this.poolLock;
        reentrantLock.lock();
        if (z) {
            try {
                decrementThread();
            } catch (Throwable th) {
                reentrantLock.unlock();
                throw th;
            }
        }
        AppLog.d("WorkerThread::onWorkerExit Thread %{public}s finished with %{public}d tasks finished", workerThread.thread.getName(), workerThread.taskCounter);
        this.pool.remove(workerThread);
        reentrantLock.unlock();
    }

    private void advanceStateTo(int i) {
        int i2;
        do {
            i2 = this.control.get();
            if (i2 >= i) {
                return;
            }
        } while (!this.control.compareAndSet(i2, combineToControl(i, getWorkingThreadNum(i2))));
    }

    private boolean compareAndIncThreadNum(int i) {
        int i2 = this.control.get();
        return this.control.compareAndSet(i2, combineToControl(getStateFromControl(i2), i + 1));
    }

    /* access modifiers changed from: protected */
    public void decrementThread() {
        do {
        } while (!compareAndDecThreadNum(this.control.get()));
    }

    private boolean compareAndDecThreadNum(int i) {
        return this.control.compareAndSet(i, i - 1);
    }

    /* access modifiers changed from: protected */
    public boolean compareAndDecNum(int i) {
        return compareAndDecThreadNum(combineToControl(getStateFromControl(this.control.get()), i));
    }
}
