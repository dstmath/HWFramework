package java.util.concurrent;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Executors {

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        DefaultThreadFactory() {
            ThreadGroup threadGroup;
            SecurityManager s = System.getSecurityManager();
            if (s != null) {
                threadGroup = s.getThreadGroup();
            } else {
                threadGroup = Thread.currentThread().getThreadGroup();
            }
            this.group = threadGroup;
            this.namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != 5) {
                t.setPriority(5);
            }
            return t;
        }
    }

    private static class DelegatedExecutorService extends AbstractExecutorService {
        private final ExecutorService e;

        DelegatedExecutorService(ExecutorService executor) {
            this.e = executor;
        }

        public void execute(Runnable command) {
            this.e.execute(command);
        }

        public void shutdown() {
            this.e.shutdown();
        }

        public List<Runnable> shutdownNow() {
            return this.e.shutdownNow();
        }

        public boolean isShutdown() {
            return this.e.isShutdown();
        }

        public boolean isTerminated() {
            return this.e.isTerminated();
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return this.e.awaitTermination(timeout, unit);
        }

        public Future<?> submit(Runnable task) {
            return this.e.submit(task);
        }

        public <T> Future<T> submit(Callable<T> task) {
            return this.e.submit((Callable) task);
        }

        public <T> Future<T> submit(Runnable task, T result) {
            return this.e.submit(task, result);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return this.e.invokeAll(tasks);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return this.e.invokeAll(tasks, timeout, unit);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            return this.e.invokeAny(tasks);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.e.invokeAny(tasks, timeout, unit);
        }
    }

    private static class DelegatedScheduledExecutorService extends DelegatedExecutorService implements ScheduledExecutorService {
        private final ScheduledExecutorService e;

        DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
            super(executor);
            this.e = executor;
        }

        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return this.e.schedule(command, delay, unit);
        }

        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return this.e.schedule((Callable) callable, delay, unit);
        }

        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return this.e.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return this.e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }

    private static class FinalizableDelegatedExecutorService extends DelegatedExecutorService {
        FinalizableDelegatedExecutorService(ExecutorService executor) {
            super(executor);
        }

        protected void finalize() {
            super.shutdown();
        }
    }

    private static final class PrivilegedCallable<T> implements Callable<T> {
        final AccessControlContext acc = AccessController.getContext();
        final Callable<T> task;

        PrivilegedCallable(Callable<T> task) {
            this.task = task;
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<T>() {
                    public T run() throws Exception {
                        return PrivilegedCallable.this.task.call();
                    }
                }, this.acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    private static final class PrivilegedCallableUsingCurrentClassLoader<T> implements Callable<T> {
        final AccessControlContext acc = AccessController.getContext();
        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        final Callable<T> task;

        PrivilegedCallableUsingCurrentClassLoader(Callable<T> task) {
            this.task = task;
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<T>() {
                    public T run() throws Exception {
                        Thread t = Thread.currentThread();
                        ClassLoader cl = t.getContextClassLoader();
                        if (PrivilegedCallableUsingCurrentClassLoader.this.ccl == cl) {
                            return PrivilegedCallableUsingCurrentClassLoader.this.task.call();
                        }
                        t.setContextClassLoader(PrivilegedCallableUsingCurrentClassLoader.this.ccl);
                        try {
                            T call = PrivilegedCallableUsingCurrentClassLoader.this.task.call();
                            return call;
                        } finally {
                            t.setContextClassLoader(cl);
                        }
                    }
                }, this.acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    private static class PrivilegedThreadFactory extends DefaultThreadFactory {
        final AccessControlContext acc = AccessController.getContext();
        final ClassLoader ccl = Thread.currentThread().getContextClassLoader();

        PrivilegedThreadFactory() {
        }

        public Thread newThread(final Runnable r) {
            return super.newThread(new Runnable() {
                public void run() {
                    final Runnable runnable = r;
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            Thread.currentThread().setContextClassLoader(PrivilegedThreadFactory.this.ccl);
                            runnable.run();
                            return null;
                        }
                    }, PrivilegedThreadFactory.this.acc);
                }
            });
        }
    }

    private static final class RunnableAdapter<T> implements Callable<T> {
        private final T result;
        private final Runnable task;

        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }

        public T call() {
            this.task.run();
            return this.result;
        }
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    }

    public static ExecutorService newWorkStealingPool(int parallelism) {
        return new ForkJoinPool(parallelism, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    }

    public static ExecutorService newWorkStealingPool() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors(), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(nThreads, nThreads, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), threadFactory);
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue()));
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), threadFactory));
    }

    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1, threadFactory));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }

    public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
        if (executor != null) {
            return new DelegatedExecutorService(executor);
        }
        throw new NullPointerException();
    }

    public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
        if (executor != null) {
            return new DelegatedScheduledExecutorService(executor);
        }
        throw new NullPointerException();
    }

    public static ThreadFactory defaultThreadFactory() {
        return new DefaultThreadFactory();
    }

    public static ThreadFactory privilegedThreadFactory() {
        return new PrivilegedThreadFactory();
    }

    public static <T> Callable<T> callable(Runnable task, T result) {
        if (task != null) {
            return new RunnableAdapter(task, result);
        }
        throw new NullPointerException();
    }

    public static Callable<Object> callable(Runnable task) {
        if (task != null) {
            return new RunnableAdapter(task, null);
        }
        throw new NullPointerException();
    }

    public static Callable<Object> callable(final PrivilegedAction<?> action) {
        if (action != null) {
            return new Callable<Object>() {
                public Object call() {
                    return action.run();
                }
            };
        }
        throw new NullPointerException();
    }

    public static Callable<Object> callable(final PrivilegedExceptionAction<?> action) {
        if (action != null) {
            return new Callable<Object>() {
                public Object call() throws Exception {
                    return action.run();
                }
            };
        }
        throw new NullPointerException();
    }

    public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
        if (callable != null) {
            return new PrivilegedCallable(callable);
        }
        throw new NullPointerException();
    }

    public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
        if (callable != null) {
            return new PrivilegedCallableUsingCurrentClassLoader(callable);
        }
        throw new NullPointerException();
    }

    private Executors() {
    }
}
