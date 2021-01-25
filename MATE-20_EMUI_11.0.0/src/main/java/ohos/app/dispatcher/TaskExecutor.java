package ohos.app.dispatcher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import ohos.app.dispatcher.task.Task;
import ohos.app.dispatcher.task.TaskPriorityComparator;
import ohos.app.dispatcher.threading.WorkerPool;
import ohos.app.dispatcher.threading.WorkerPoolConfig;
import ohos.app.dispatcher.threading.WorkerThread;
import ohos.appexecfwk.utils.AppLog;

/* access modifiers changed from: package-private */
public class TaskExecutor extends WorkerPool implements DelayExecuteService, WorkerThread.Delegate {
    private static final String CONSUMER_THREAD_NAME = "TaskConsumer";
    private static final int INITIAL_CAPACITY = 64;
    private static AtomicLong sequence = new AtomicLong(0);
    private volatile Thread consumer;
    private final Object consumerLock = new Object();
    private final DelayQueue<DelayTaskWrapper> delayTasks = new DelayQueue<>();
    private final BlockingQueue<Task> pendingTasks = new PriorityBlockingQueue(64, new TaskPriorityComparator());
    private AtomicLong taskCounter = new AtomicLong(0);
    private final AtomicBoolean terminated = new AtomicBoolean(false);

    /* access modifiers changed from: private */
    public static class DelayTaskWrapper implements Delayed {
        private final Runnable task;
        private final Long triggerTime;

        DelayTaskWrapper(Runnable runnable, long j) {
            this.task = runnable;
            this.triggerTime = Long.valueOf(calcTriggerTime(j));
        }

        /* access modifiers changed from: package-private */
        public long calcTriggerTime(long j) {
            long currentTimeMillis = System.currentTimeMillis();
            if (Long.MAX_VALUE - currentTimeMillis <= j) {
                return Long.MAX_VALUE;
            }
            return j + currentTimeMillis;
        }

        @Override // java.util.concurrent.Delayed
        public long getDelay(TimeUnit timeUnit) {
            return timeUnit.convert(this.triggerTime.longValue() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        public int compareTo(Delayed delayed) {
            if (delayed == null) {
                throw new NullPointerException("Other comparable is null");
            } else if (delayed == this) {
                return 0;
            } else {
                if (delayed instanceof DelayTaskWrapper) {
                    return this.triggerTime.compareTo(((DelayTaskWrapper) delayed).triggerTime);
                }
                throw new ClassCastException("Other comparable is not expected instance");
            }
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            if ((obj instanceof DelayTaskWrapper) && compareTo((Delayed) ((DelayTaskWrapper) obj)) == 0) {
                return true;
            }
            return false;
        }

        @Override // java.lang.Object
        public int hashCode() {
            return System.identityHashCode(this);
        }

        /* access modifiers changed from: package-private */
        public void run() {
            this.task.run();
        }
    }

    public TaskExecutor(WorkerPoolConfig workerPoolConfig) {
        super(workerPoolConfig);
    }

    public void execute(Task task) {
        if (task != null) {
            task.setSequence(sequence.getAndIncrement());
            if (!addWorker(this, task) && !this.pendingTasks.offer(task)) {
                AppLog.w("TaskExecutor.execute rejected a task", new Object[0]);
                return;
            }
            return;
        }
        throw new NullPointerException("Runnable to be execute cannot be null");
    }

    /* JADX INFO: finally extract failed */
    @Override // ohos.app.dispatcher.threading.WorkerThread.Delegate
    public void doWorks(WorkerThread workerThread) {
        Runnable pollFirstTask = workerThread.pollFirstTask();
        while (true) {
            if (pollFirstTask == null) {
                try {
                    pollFirstTask = getTask(workerThread);
                    if (pollFirstTask == null) {
                        return;
                    }
                } finally {
                    onWorkerExit(workerThread, true);
                }
            }
            try {
                beforeRun(pollFirstTask);
                try {
                    pollFirstTask.run();
                    afterRun(pollFirstTask);
                    workerThread.incTaskCount();
                    this.taskCounter.incrementAndGet();
                    pollFirstTask = null;
                } catch (Throwable th) {
                    afterRun(pollFirstTask);
                    throw th;
                }
            } catch (Throwable th2) {
                workerThread.incTaskCount();
                this.taskCounter.incrementAndGet();
                throw th2;
            }
        }
    }

    private Runnable getTask(WorkerThread workerThread) {
        Task task;
        while (true) {
            boolean z = false;
            while (true) {
                if (!this.terminated.get() || !this.pendingTasks.isEmpty()) {
                    int workCount = getWorkCount();
                    boolean z2 = workCount > getCoreThreadCount();
                    if (!z || !z2 || !this.pendingTasks.isEmpty()) {
                        if (z2) {
                            try {
                                task = this.pendingTasks.poll(getKeepAliveTime(), TimeUnit.MILLISECONDS);
                            } catch (InterruptedException unused) {
                                AppLog.w("TaskExecutor.getTask on %{public}s is interrupted", workerThread.getThreadName());
                            }
                        } else {
                            task = this.pendingTasks.take();
                        }
                        if (task != null) {
                            return task;
                        }
                        z = true;
                    } else if (compareAndDecNum(workCount)) {
                        AppLog.d("TaskExecutor::getTask on  %{public}s is timeout", workerThread.getThreadName());
                        return null;
                    }
                } else {
                    AppLog.d("TaskExecutor::getTask on  %{public}s is terminated", workerThread.getThreadName());
                    decrementThread();
                    return null;
                }
            }
        }
    }

    public void terminate(boolean z) {
        terminateConsumer();
        closePool(z);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.app.dispatcher.threading.WorkerPool
    public void afterRun(Runnable runnable) {
        if (runnable instanceof Task) {
            ((Task) runnable).afterTaskExecute();
        } else {
            AppLog.w("TaskExecutor.afterRun instance of task is not expected", new Object[0]);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.app.dispatcher.threading.WorkerPool
    public void beforeRun(Runnable runnable) {
        if (runnable instanceof Task) {
            ((Task) runnable).beforeTaskExecute();
        } else {
            AppLog.w("TaskExecutor.beforeRun instance of task is not expected", new Object[0]);
        }
    }

    @Override // ohos.app.dispatcher.DelayExecuteService
    public boolean delayExecute(Runnable runnable, long j) {
        if (j <= 0) {
            runnable.run();
            return true;
        } else if (this.terminated.get()) {
            return false;
        } else {
            this.delayTasks.offer((DelayQueue<DelayTaskWrapper>) new DelayTaskWrapper(runnable, j));
            ensureConsumeStarted();
            return true;
        }
    }

    private void terminateConsumer() {
        this.terminated.set(true);
        synchronized (this.consumerLock) {
            if (this.consumer != null && !this.consumer.isInterrupted()) {
                try {
                    this.consumer.interrupt();
                } catch (SecurityException unused) {
                    AppLog.w("TaskExecutor.terminateConsumer has SecurityException", new Object[0]);
                }
            }
        }
    }

    private void ensureConsumeStarted() {
        if (this.consumer == null) {
            synchronized (this.consumerLock) {
                if (this.consumer == null) {
                    Thread thread = new Thread(new Runnable() {
                        /* class ohos.app.dispatcher.TaskExecutor.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            TaskExecutor.this.consume();
                        }
                    }, CONSUMER_THREAD_NAME);
                    thread.start();
                    this.consumer = thread;
                    AppLog.d("TaskExecutor start a delay task consumer", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void consume() {
        while (true) {
            if (!this.terminated.get() || !this.delayTasks.isEmpty()) {
                try {
                    DelayTaskWrapper take = this.delayTasks.take();
                    if (take != null) {
                        take.run();
                    }
                } catch (InterruptedException unused) {
                    AppLog.w("TaskExecutor Consumer thread is interrupted", new Object[0]);
                }
            } else {
                return;
            }
        }
    }

    public int getPendingTasksSize() {
        return this.pendingTasks.size();
    }

    public long getTaskCounter() {
        return this.taskCounter.get();
    }
}
