package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.Channel;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import sun.security.action.GetIntegerAction;

abstract class AsynchronousChannelGroupImpl extends AsynchronousChannelGroup implements Executor {
    private static final int internalThreadCount = ((Integer) AccessController.doPrivileged(new GetIntegerAction("sun.nio.ch.internalThreadPoolSize", 1))).lambda$-java_util_stream_IntPipeline_14709();
    private final ThreadPool pool;
    private final AtomicBoolean shutdown = new AtomicBoolean();
    private final Object shutdownNowLock = new Object();
    private final Queue<Runnable> taskQueue;
    private volatile boolean terminateInitiated;
    private final AtomicInteger threadCount = new AtomicInteger();
    private ScheduledThreadPoolExecutor timeoutExecutor;

    abstract Object attachForeignChannel(Channel channel, FileDescriptor fileDescriptor) throws IOException;

    abstract void closeAllChannels() throws IOException;

    abstract void detachForeignChannel(Object obj);

    abstract void executeOnHandlerTask(Runnable runnable);

    abstract boolean isEmpty();

    abstract void shutdownHandlerTasks();

    AsynchronousChannelGroupImpl(AsynchronousChannelProvider provider, ThreadPool pool) {
        super(provider);
        this.pool = pool;
        if (pool.isFixedThreadPool()) {
            this.taskQueue = new ConcurrentLinkedQueue();
        } else {
            this.taskQueue = null;
        }
        this.timeoutExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, ThreadPool.defaultThreadFactory());
        this.timeoutExecutor.setRemoveOnCancelPolicy(true);
    }

    final ExecutorService executor() {
        return this.pool.executor();
    }

    final boolean isFixedThreadPool() {
        return this.pool.isFixedThreadPool();
    }

    final int fixedThreadCount() {
        if (isFixedThreadPool()) {
            return this.pool.poolSize();
        }
        return this.pool.poolSize() + internalThreadCount;
    }

    private Runnable bindToGroup(final Runnable task) {
        return new Runnable() {
            public void run() {
                Invoker.bindToGroup(this);
                task.run();
            }
        };
    }

    private void startInternalThread(final Runnable task) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                ThreadPool.defaultThreadFactory().newThread(task).start();
                return null;
            }
        });
    }

    protected final void startThreads(Runnable task) {
        int i;
        if (!isFixedThreadPool()) {
            for (i = 0; i < internalThreadCount; i++) {
                startInternalThread(task);
                this.threadCount.incrementAndGet();
            }
        }
        if (this.pool.poolSize() > 0) {
            task = bindToGroup(task);
            i = 0;
            while (i < this.pool.poolSize()) {
                try {
                    this.pool.executor().execute(task);
                    this.threadCount.incrementAndGet();
                    i++;
                } catch (RejectedExecutionException e) {
                    return;
                }
            }
        }
    }

    final int threadCount() {
        return this.threadCount.get();
    }

    final int threadExit(Runnable task, boolean replaceMe) {
        if (replaceMe) {
            try {
                if (Invoker.isBoundToAnyGroup()) {
                    this.pool.executor().execute(bindToGroup(task));
                } else {
                    startInternalThread(task);
                }
                return this.threadCount.get();
            } catch (RejectedExecutionException e) {
            }
        }
        return this.threadCount.decrementAndGet();
    }

    final void executeOnPooledThread(Runnable task) {
        if (isFixedThreadPool()) {
            executeOnHandlerTask(task);
        } else {
            this.pool.executor().execute(bindToGroup(task));
        }
    }

    final void offerTask(Runnable task) {
        this.taskQueue.offer(task);
    }

    final Runnable pollTask() {
        return this.taskQueue == null ? null : (Runnable) this.taskQueue.poll();
    }

    final Future<?> schedule(Runnable task, long timeout, TimeUnit unit) {
        try {
            return this.timeoutExecutor.schedule(task, timeout, unit);
        } catch (Object rej) {
            if (this.terminateInitiated) {
                return null;
            }
            throw new AssertionError(rej);
        }
    }

    public final boolean isShutdown() {
        return this.shutdown.get();
    }

    public final boolean isTerminated() {
        return this.pool.executor().isTerminated();
    }

    private void shutdownExecutors() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                AsynchronousChannelGroupImpl.this.pool.executor().shutdown();
                AsynchronousChannelGroupImpl.this.timeoutExecutor.shutdown();
                return null;
            }
        });
    }

    public final void shutdown() {
        if (!this.shutdown.getAndSet(true) && isEmpty()) {
            synchronized (this.shutdownNowLock) {
                if (!this.terminateInitiated) {
                    this.terminateInitiated = true;
                    shutdownHandlerTasks();
                    shutdownExecutors();
                }
            }
        }
    }

    public final void shutdownNow() throws IOException {
        this.shutdown.set(true);
        synchronized (this.shutdownNowLock) {
            if (!this.terminateInitiated) {
                this.terminateInitiated = true;
                closeAllChannels();
                shutdownHandlerTasks();
                shutdownExecutors();
            }
        }
    }

    final void detachFromThreadPool() {
        if (this.shutdown.getAndSet(true)) {
            throw new AssertionError((Object) "Already shutdown");
        } else if (isEmpty()) {
            shutdownHandlerTasks();
        } else {
            throw new AssertionError((Object) "Group not empty");
        }
    }

    public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.pool.executor().awaitTermination(timeout, unit);
    }

    public final void execute(final Runnable task) {
        if (System.getSecurityManager() != null) {
            final AccessControlContext acc = AccessController.getContext();
            Runnable delegate = task;
            task = new Runnable() {
                public void run() {
                    final Runnable runnable = task;
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            runnable.run();
                            return null;
                        }
                    }, acc);
                }
            };
        }
        executeOnPooledThread(task);
    }
}
