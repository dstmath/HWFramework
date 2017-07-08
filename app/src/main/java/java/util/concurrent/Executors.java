package java.util.concurrent;

import android.icu.util.AnnualTimeZoneRule;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Executors {

    /* renamed from: java.util.concurrent.Executors.1 */
    static class AnonymousClass1 implements Callable<Object> {
        final /* synthetic */ PrivilegedAction val$action;

        AnonymousClass1(PrivilegedAction val$action) {
            this.val$action = val$action;
        }

        public Object call() {
            return this.val$action.run();
        }
    }

    /* renamed from: java.util.concurrent.Executors.2 */
    static class AnonymousClass2 implements Callable<Object> {
        final /* synthetic */ PrivilegedExceptionAction val$action;

        AnonymousClass2(PrivilegedExceptionAction val$action) {
            this.val$action = val$action;
        }

        public Object call() throws Exception {
            return this.val$action.run();
        }
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = null;
        private final ThreadGroup group;
        private final String namePrefix;
        private final AtomicInteger threadNumber;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.Executors.DefaultThreadFactory.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.Executors.DefaultThreadFactory.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.Executors.DefaultThreadFactory.<clinit>():void");
        }

        DefaultThreadFactory() {
            ThreadGroup threadGroup;
            this.threadNumber = new AtomicInteger(1);
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
        final AccessControlContext acc;
        final Callable<T> task;

        /* renamed from: java.util.concurrent.Executors.PrivilegedCallable.1 */
        class AnonymousClass1 implements PrivilegedExceptionAction<T> {
            final /* synthetic */ PrivilegedCallable this$1;

            AnonymousClass1(PrivilegedCallable this$1) {
                this.this$1 = this$1;
            }

            public T run() throws Exception {
                return this.this$1.task.call();
            }
        }

        PrivilegedCallable(Callable<T> task) {
            this.task = task;
            this.acc = AccessController.getContext();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(new AnonymousClass1(this), this.acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    private static final class PrivilegedCallableUsingCurrentClassLoader<T> implements Callable<T> {
        final AccessControlContext acc;
        final ClassLoader ccl;
        final Callable<T> task;

        /* renamed from: java.util.concurrent.Executors.PrivilegedCallableUsingCurrentClassLoader.1 */
        class AnonymousClass1 implements PrivilegedExceptionAction<T> {
            final /* synthetic */ PrivilegedCallableUsingCurrentClassLoader this$1;

            AnonymousClass1(PrivilegedCallableUsingCurrentClassLoader this$1) {
                this.this$1 = this$1;
            }

            public T run() throws Exception {
                Thread t = Thread.currentThread();
                ClassLoader cl = t.getContextClassLoader();
                if (this.this$1.ccl == cl) {
                    return this.this$1.task.call();
                }
                t.setContextClassLoader(this.this$1.ccl);
                try {
                    T call = this.this$1.task.call();
                    return call;
                } finally {
                    t.setContextClassLoader(cl);
                }
            }
        }

        PrivilegedCallableUsingCurrentClassLoader(Callable<T> task) {
            this.task = task;
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public T call() throws Exception {
            try {
                return AccessController.doPrivileged(new AnonymousClass1(this), this.acc);
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        }
    }

    private static class PrivilegedThreadFactory extends DefaultThreadFactory {
        final AccessControlContext acc;
        final ClassLoader ccl;

        /* renamed from: java.util.concurrent.Executors.PrivilegedThreadFactory.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ PrivilegedThreadFactory this$1;
            final /* synthetic */ Runnable val$r;

            /* renamed from: java.util.concurrent.Executors.PrivilegedThreadFactory.1.1 */
            class AnonymousClass1 implements PrivilegedAction<Void> {
                final /* synthetic */ AnonymousClass1 this$2;
                final /* synthetic */ Runnable val$r;

                AnonymousClass1(AnonymousClass1 this$2, Runnable val$r) {
                    this.this$2 = this$2;
                    this.val$r = val$r;
                }

                public /* bridge */ /* synthetic */ Object run() {
                    return run();
                }

                public Void m92run() {
                    Thread.currentThread().setContextClassLoader(this.this$2.this$1.ccl);
                    this.val$r.run();
                    return null;
                }
            }

            AnonymousClass1(PrivilegedThreadFactory this$1, Runnable val$r) {
                this.this$1 = this$1;
                this.val$r = val$r;
            }

            public void run() {
                AccessController.doPrivileged(new AnonymousClass1(this, this.val$r), this.this$1.acc);
            }
        }

        PrivilegedThreadFactory() {
            this.acc = AccessController.getContext();
            this.ccl = Thread.currentThread().getContextClassLoader();
        }

        public Thread newThread(Runnable r) {
            return super.newThread(new AnonymousClass1(this, r));
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
        return new ThreadPoolExecutor(0, AnnualTimeZoneRule.MAX_YEAR, 60, TimeUnit.SECONDS, new SynchronousQueue());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(0, (int) AnnualTimeZoneRule.MAX_YEAR, 60, TimeUnit.SECONDS, new SynchronousQueue(), threadFactory);
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

    public static Callable<Object> callable(PrivilegedAction<?> action) {
        if (action != null) {
            return new AnonymousClass1(action);
        }
        throw new NullPointerException();
    }

    public static Callable<Object> callable(PrivilegedExceptionAction<?> action) {
        if (action != null) {
            return new AnonymousClass2(action);
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
