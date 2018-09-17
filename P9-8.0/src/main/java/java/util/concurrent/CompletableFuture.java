package java.util.concurrent;

import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import sun.misc.Unsafe;

public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {
    static final int ASYNC = 1;
    private static final Executor ASYNC_POOL;
    static final int NESTED = -1;
    private static final long NEXT;
    static final AltResult NIL = new AltResult(null);
    private static final long RESULT;
    static final int SPINS;
    private static final long STACK;
    static final int SYNC = 0;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final boolean USE_COMMON_POOL;
    volatile Object result;
    volatile Completion stack;

    static final class AltResult {
        final Throwable ex;

        AltResult(Throwable x) {
            this.ex = x;
        }
    }

    public interface AsynchronousCompletionTask {
    }

    static final class AsyncRun extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<Void> dep;
        Runnable fn;

        AsyncRun(CompletableFuture<Void> dep, Runnable fn) {
            this.dep = dep;
            this.fn = fn;
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }

        public final boolean exec() {
            run();
            return true;
        }

        public void run() {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                Runnable f = this.fn;
                if (f != null) {
                    this.dep = null;
                    this.fn = null;
                    if (d.result == null) {
                        try {
                            f.run();
                            d.completeNull();
                        } catch (Throwable ex) {
                            d.completeThrowable(ex);
                        }
                    }
                    d.postComplete();
                }
            }
        }
    }

    static final class AsyncSupply<T> extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<T> dep;
        Supplier<? extends T> fn;

        AsyncSupply(CompletableFuture<T> dep, Supplier<? extends T> fn) {
            this.dep = dep;
            this.fn = fn;
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }

        public final boolean exec() {
            run();
            return true;
        }

        public void run() {
            CompletableFuture<T> d = this.dep;
            if (d != null) {
                Supplier<? extends T> f = this.fn;
                if (f != null) {
                    this.dep = null;
                    this.fn = null;
                    if (d.result == null) {
                        try {
                            d.completeValue(f.get());
                        } catch (Throwable ex) {
                            d.completeThrowable(ex);
                        }
                    }
                    d.postComplete();
                }
            }
        }
    }

    static abstract class Completion extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        volatile Completion next;

        abstract boolean isLive();

        abstract CompletableFuture<?> tryFire(int i);

        Completion() {
        }

        public final void run() {
            tryFire(1);
        }

        public final boolean exec() {
            tryFire(1);
            return false;
        }

        public final Void getRawResult() {
            return null;
        }

        public final void setRawResult(Void v) {
        }
    }

    static abstract class UniCompletion<T, V> extends Completion {
        CompletableFuture<V> dep;
        Executor executor;
        CompletableFuture<T> src;

        UniCompletion(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src) {
            this.executor = executor;
            this.dep = dep;
            this.src = src;
        }

        final boolean claim() {
            Executor e = this.executor;
            if (compareAndSetForkJoinTaskTag((short) 0, (short) 1)) {
                if (e == null) {
                    return true;
                }
                this.executor = null;
                e.execute(this);
            }
            return false;
        }

        final boolean isLive() {
            return this.dep != null;
        }
    }

    static abstract class BiCompletion<T, U, V> extends UniCompletion<T, V> {
        CompletableFuture<U> snd;

        BiCompletion(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(executor, dep, src);
            this.snd = snd;
        }
    }

    static final class BiAccept<T, U> extends BiCompletion<T, U, Void> {
        BiConsumer<? super T, ? super U> fn;

        BiAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, BiConsumer<? super T, ? super U> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                BiAccept biAccept;
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                BiConsumer biConsumer = this.fn;
                if (mode > 0) {
                    biAccept = null;
                } else {
                    biAccept = this;
                }
                if ((d.biAccept(a, b, biConsumer, biAccept) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class BiApply<T, U, V> extends BiCompletion<T, U, V> {
        BiFunction<? super T, ? super U, ? extends V> fn;

        BiApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd, BiFunction<? super T, ? super U, ? extends V> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d = this.dep;
            if (d != null) {
                BiApply biApply;
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                BiFunction biFunction = this.fn;
                if (mode > 0) {
                    biApply = null;
                } else {
                    biApply = this;
                }
                if ((d.biApply(a, b, biFunction, biApply) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class BiRelay<T, U> extends BiCompletion<T, U, Void> {
        BiRelay(CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                if ((d.biRelay(a, b) ^ 1) == 0) {
                    this.src = null;
                    this.snd = null;
                    this.dep = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class BiRun<T, U> extends BiCompletion<T, U, Void> {
        Runnable fn;

        BiRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Runnable fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                BiRun biRun;
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                Runnable runnable = this.fn;
                if (mode > 0) {
                    biRun = null;
                } else {
                    biRun = this;
                }
                if ((d.biRun(a, b, runnable, biRun) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class Canceller implements BiConsumer<Object, Throwable> {
        final Future<?> f;

        Canceller(Future<?> f) {
            this.f = f;
        }

        public void accept(Object ignore, Throwable ex) {
            if (ex == null && this.f != null && (this.f.isDone() ^ 1) != 0) {
                this.f.cancel(false);
            }
        }
    }

    static final class CoCompletion extends Completion {
        BiCompletion<?, ?, ?> base;

        CoCompletion(BiCompletion<?, ?, ?> base) {
            this.base = base;
        }

        final CompletableFuture<?> tryFire(int mode) {
            BiCompletion<?, ?, ?> c = this.base;
            if (c != null) {
                CompletableFuture<?> d = c.tryFire(mode);
                if (d != null) {
                    this.base = null;
                    return d;
                }
            }
            return null;
        }

        final boolean isLive() {
            BiCompletion<?, ?, ?> c = this.base;
            return (c == null || c.dep == null) ? false : true;
        }
    }

    static final class DelayedCompleter<U> implements Runnable {
        final CompletableFuture<U> f;
        final U u;

        DelayedCompleter(CompletableFuture<U> f, U u) {
            this.f = f;
            this.u = u;
        }

        public void run() {
            if (this.f != null) {
                this.f.complete(this.u);
            }
        }
    }

    static final class DelayedExecutor implements Executor {
        final long delay;
        final Executor executor;
        final TimeUnit unit;

        DelayedExecutor(long delay, TimeUnit unit, Executor executor) {
            this.delay = delay;
            this.unit = unit;
            this.executor = executor;
        }

        public void execute(Runnable r) {
            Delayer.delay(new TaskSubmitter(this.executor, r), this.delay, this.unit);
        }
    }

    static final class Delayer {
        static final ScheduledThreadPoolExecutor delayer;

        static final class DaemonThreadFactory implements ThreadFactory {
            DaemonThreadFactory() {
            }

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("CompletableFutureDelayScheduler");
                return t;
            }
        }

        Delayer() {
        }

        static ScheduledFuture<?> delay(Runnable command, long delay, TimeUnit unit) {
            return delayer.schedule(command, delay, unit);
        }

        static {
            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new DaemonThreadFactory());
            delayer = scheduledThreadPoolExecutor;
            scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
        }
    }

    static final class MinimalStage<T> extends CompletableFuture<T> {
        MinimalStage() {
        }

        MinimalStage(Object r) {
            super(r);
        }

        public <U> CompletableFuture<U> newIncompleteFuture() {
            return new MinimalStage();
        }

        public T get() {
            throw new UnsupportedOperationException();
        }

        public T get(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        public T getNow(T t) {
            throw new UnsupportedOperationException();
        }

        public T join() {
            throw new UnsupportedOperationException();
        }

        public boolean complete(T t) {
            throw new UnsupportedOperationException();
        }

        public boolean completeExceptionally(Throwable ex) {
            throw new UnsupportedOperationException();
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            throw new UnsupportedOperationException();
        }

        public void obtrudeValue(T t) {
            throw new UnsupportedOperationException();
        }

        public void obtrudeException(Throwable ex) {
            throw new UnsupportedOperationException();
        }

        public boolean isDone() {
            throw new UnsupportedOperationException();
        }

        public boolean isCancelled() {
            throw new UnsupportedOperationException();
        }

        public boolean isCompletedExceptionally() {
            throw new UnsupportedOperationException();
        }

        public int getNumberOfDependents() {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        public CompletableFuture<T> completeOnTimeout(T t, long timeout, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }

    static final class OrAccept<T, U extends T> extends BiCompletion<T, U, Void> {
        Consumer<? super T> fn;

        OrAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Consumer<? super T> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                OrAccept orAccept;
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                Consumer consumer = this.fn;
                if (mode > 0) {
                    orAccept = null;
                } else {
                    orAccept = this;
                }
                if ((d.orAccept(a, b, consumer, orAccept) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class OrApply<T, U extends T, V> extends BiCompletion<T, U, V> {
        Function<? super T, ? extends V> fn;

        OrApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Function<? super T, ? extends V> fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d = this.dep;
            if (d != null) {
                OrApply orApply;
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                Function function = this.fn;
                if (mode > 0) {
                    orApply = null;
                } else {
                    orApply = this;
                }
                if ((d.orApply(a, b, function, orApply) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class OrRelay<T, U> extends BiCompletion<T, U, Object> {
        OrRelay(CompletableFuture<Object> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }

        final CompletableFuture<Object> tryFire(int mode) {
            CompletableFuture<Object> d = this.dep;
            if (d != null) {
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                if ((d.orRelay(a, b) ^ 1) == 0) {
                    this.src = null;
                    this.snd = null;
                    this.dep = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class OrRun<T, U> extends BiCompletion<T, U, Void> {
        Runnable fn;

        OrRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Runnable fn) {
            super(executor, dep, src, snd);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                OrRun orRun;
                CompletableFuture<T> a = this.src;
                CompletableFuture<U> b = this.snd;
                Runnable runnable = this.fn;
                if (mode > 0) {
                    orRun = null;
                } else {
                    orRun = this;
                }
                if ((d.orRun(a, b, runnable, orRun) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(a, b, mode);
                }
            }
            return null;
        }
    }

    static final class Signaller extends Completion implements ManagedBlocker {
        final long deadline;
        boolean interrupted;
        final boolean interruptible;
        long nanos;
        volatile Thread thread = Thread.currentThread();

        Signaller(boolean interruptible, long nanos, long deadline) {
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.deadline = deadline;
        }

        final CompletableFuture<?> tryFire(int ignore) {
            Thread w = this.thread;
            if (w != null) {
                this.thread = null;
                LockSupport.unpark(w);
            }
            return null;
        }

        public boolean isReleasable() {
            if (Thread.interrupted()) {
                this.interrupted = true;
            }
            if (this.interrupted && this.interruptible) {
                return true;
            }
            if (this.deadline != 0) {
                if (this.nanos <= 0) {
                    return true;
                }
                long nanoTime = this.deadline - System.nanoTime();
                this.nanos = nanoTime;
                if (nanoTime <= 0) {
                    return true;
                }
            }
            if (this.thread != null) {
                return false;
            }
            return true;
        }

        public boolean block() {
            while (!isReleasable()) {
                if (this.deadline == 0) {
                    LockSupport.park(this);
                } else {
                    LockSupport.parkNanos(this, this.nanos);
                }
            }
            return true;
        }

        final boolean isLive() {
            return this.thread != null;
        }
    }

    static final class TaskSubmitter implements Runnable {
        final Runnable action;
        final Executor executor;

        TaskSubmitter(Executor executor, Runnable action) {
            this.executor = executor;
            this.action = action;
        }

        public void run() {
            this.executor.execute(this.action);
        }
    }

    static final class ThreadPerTaskExecutor implements Executor {
        ThreadPerTaskExecutor() {
        }

        public void execute(Runnable r) {
            new Thread(r).start();
        }
    }

    static final class Timeout implements Runnable {
        final CompletableFuture<?> f;

        Timeout(CompletableFuture<?> f) {
            this.f = f;
        }

        public void run() {
            if (this.f != null && (this.f.isDone() ^ 1) != 0) {
                this.f.completeExceptionally(new TimeoutException());
            }
        }
    }

    static final class UniAccept<T> extends UniCompletion<T, Void> {
        Consumer<? super T> fn;

        UniAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Consumer<? super T> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                UniAccept uniAccept;
                CompletableFuture<T> a = this.src;
                Consumer consumer = this.fn;
                if (mode > 0) {
                    uniAccept = null;
                } else {
                    uniAccept = this;
                }
                if ((d.uniAccept(a, consumer, uniAccept) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniApply<T, V> extends UniCompletion<T, V> {
        Function<? super T, ? extends V> fn;

        UniApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T, ? extends V> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d = this.dep;
            if (d != null) {
                UniApply uniApply;
                CompletableFuture<T> a = this.src;
                Function function = this.fn;
                if (mode > 0) {
                    uniApply = null;
                } else {
                    uniApply = this;
                }
                if ((d.uniApply(a, function, uniApply) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniCompose<T, V> extends UniCompletion<T, V> {
        Function<? super T, ? extends CompletionStage<V>> fn;

        UniCompose(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T, ? extends CompletionStage<V>> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d = this.dep;
            if (d != null) {
                UniCompose uniCompose;
                CompletableFuture<T> a = this.src;
                Function function = this.fn;
                if (mode > 0) {
                    uniCompose = null;
                } else {
                    uniCompose = this;
                }
                if ((d.uniCompose(a, function, uniCompose) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniExceptionally<T> extends UniCompletion<T, T> {
        Function<? super Throwable, ? extends T> fn;

        UniExceptionally(CompletableFuture<T> dep, CompletableFuture<T> src, Function<? super Throwable, ? extends T> fn) {
            super(null, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d = this.dep;
            if (d != null) {
                CompletableFuture<T> a = this.src;
                if ((d.uniExceptionally(a, this.fn, this) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniHandle<T, V> extends UniCompletion<T, V> {
        BiFunction<? super T, Throwable, ? extends V> fn;

        UniHandle(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, BiFunction<? super T, Throwable, ? extends V> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> d = this.dep;
            if (d != null) {
                UniHandle uniHandle;
                CompletableFuture<T> a = this.src;
                BiFunction biFunction = this.fn;
                if (mode > 0) {
                    uniHandle = null;
                } else {
                    uniHandle = this;
                }
                if ((d.uniHandle(a, biFunction, uniHandle) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniRelay<T> extends UniCompletion<T, T> {
        UniRelay(CompletableFuture<T> dep, CompletableFuture<T> src) {
            super(null, dep, src);
        }

        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d = this.dep;
            if (d != null) {
                CompletableFuture<T> a = this.src;
                if ((d.uniRelay(a) ^ 1) == 0) {
                    this.src = null;
                    this.dep = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniRun<T> extends UniCompletion<T, Void> {
        Runnable fn;

        UniRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Runnable fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> d = this.dep;
            if (d != null) {
                UniRun uniRun;
                CompletableFuture<T> a = this.src;
                Runnable runnable = this.fn;
                if (mode > 0) {
                    uniRun = null;
                } else {
                    uniRun = this;
                }
                if ((d.uniRun(a, runnable, uniRun) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    static final class UniWhenComplete<T> extends UniCompletion<T, T> {
        BiConsumer<? super T, ? super Throwable> fn;

        UniWhenComplete(Executor executor, CompletableFuture<T> dep, CompletableFuture<T> src, BiConsumer<? super T, ? super Throwable> fn) {
            super(executor, dep, src);
            this.fn = fn;
        }

        final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> d = this.dep;
            if (d != null) {
                UniWhenComplete uniWhenComplete;
                CompletableFuture<T> a = this.src;
                BiConsumer biConsumer = this.fn;
                if (mode > 0) {
                    uniWhenComplete = null;
                } else {
                    uniWhenComplete = this;
                }
                if ((d.uniWhenComplete(a, biConsumer, uniWhenComplete) ^ 1) == 0) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(a, mode);
                }
            }
            return null;
        }
    }

    final boolean internalComplete(Object r) {
        return U.compareAndSwapObject(this, RESULT, null, r);
    }

    final boolean casStack(Completion cmp, Completion val) {
        return U.compareAndSwapObject(this, STACK, cmp, val);
    }

    final boolean tryPushStack(Completion c) {
        Completion h = this.stack;
        lazySetNext(c, h);
        return U.compareAndSwapObject(this, STACK, h, c);
    }

    final void pushStack(Completion c) {
        do {
        } while (!tryPushStack(c));
    }

    static {
        boolean z;
        Executor commonPool;
        int i = 0;
        if (ForkJoinPool.getCommonPoolParallelism() > 1) {
            z = true;
        } else {
            z = false;
        }
        USE_COMMON_POOL = z;
        if (USE_COMMON_POOL) {
            commonPool = ForkJoinPool.commonPool();
        } else {
            commonPool = new ThreadPerTaskExecutor();
        }
        ASYNC_POOL = commonPool;
        if (Runtime.getRuntime().availableProcessors() > 1) {
            i = 256;
        }
        SPINS = i;
        try {
            RESULT = U.objectFieldOffset(CompletableFuture.class.getDeclaredField("result"));
            STACK = U.objectFieldOffset(CompletableFuture.class.getDeclaredField("stack"));
            NEXT = U.objectFieldOffset(Completion.class.getDeclaredField("next"));
            Class<?> ensureLoaded = LockSupport.class;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    final boolean completeNull() {
        return U.compareAndSwapObject(this, RESULT, null, NIL);
    }

    final Object encodeValue(T t) {
        return t == null ? NIL : t;
    }

    final boolean completeValue(T t) {
        Object obj;
        Unsafe unsafe = U;
        long j = RESULT;
        if (t == null) {
            obj = NIL;
        } else {
            T obj2 = t;
        }
        return unsafe.compareAndSwapObject(this, j, null, obj2);
    }

    static AltResult encodeThrowable(Throwable x) {
        if (!(x instanceof CompletionException)) {
            x = new CompletionException(x);
        }
        return new AltResult(x);
    }

    final boolean completeThrowable(Throwable x) {
        return U.compareAndSwapObject(this, RESULT, null, encodeThrowable(x));
    }

    static Object encodeThrowable(Throwable x, Object r) {
        if (!(x instanceof CompletionException)) {
            x = new CompletionException(x);
        } else if ((r instanceof AltResult) && x == ((AltResult) r).ex) {
            return r;
        }
        return new AltResult(x);
    }

    final boolean completeThrowable(Throwable x, Object r) {
        return U.compareAndSwapObject(this, RESULT, null, encodeThrowable(x, r));
    }

    Object encodeOutcome(T t, Throwable x) {
        if (x == null) {
            return t == null ? NIL : t;
        } else {
            return encodeThrowable(x);
        }
    }

    static Object encodeRelay(Object r) {
        if (!(r instanceof AltResult)) {
            return r;
        }
        Throwable x = ((AltResult) r).ex;
        if (x == null || ((x instanceof CompletionException) ^ 1) == 0) {
            return r;
        }
        return new AltResult(new CompletionException(x));
    }

    final boolean completeRelay(Object r) {
        return U.compareAndSwapObject(this, RESULT, null, encodeRelay(r));
    }

    private static <T> T reportGet(Object r) throws InterruptedException, ExecutionException {
        if (r == null) {
            throw new InterruptedException();
        } else if (r instanceof AltResult) {
            Throwable x = ((AltResult) r).ex;
            if (x == null) {
                return null;
            }
            if (x instanceof CancellationException) {
                throw ((CancellationException) x);
            }
            if (x instanceof CompletionException) {
                Throwable cause = x.getCause();
                if (cause != null) {
                    x = cause;
                }
            }
            throw new ExecutionException(x);
        } else {
            T t = r;
            return r;
        }
    }

    private static <T> T reportJoin(Object r) {
        if (r instanceof AltResult) {
            Throwable x = ((AltResult) r).ex;
            if (x == null) {
                return null;
            }
            if (x instanceof CancellationException) {
                throw ((CancellationException) x);
            } else if (x instanceof CompletionException) {
                throw ((CompletionException) x);
            } else {
                throw new CompletionException(x);
            }
        }
        T t = r;
        return r;
    }

    static Executor screenExecutor(Executor e) {
        if (!USE_COMMON_POOL && e == ForkJoinPool.commonPool()) {
            return ASYNC_POOL;
        }
        if (e != null) {
            return e;
        }
        throw new NullPointerException();
    }

    static void lazySetNext(Completion c, Completion next) {
        U.putOrderedObject(c, NEXT, next);
    }

    final void postComplete() {
        CompletableFuture<?> f = this;
        while (true) {
            Completion h = f.stack;
            if (h == null) {
                if (f != this) {
                    f = this;
                    h = this.stack;
                    if (h == null) {
                        return;
                    }
                }
                return;
            }
            Completion t = h.next;
            if (f.casStack(h, t)) {
                if (t != null) {
                    if (f != this) {
                        pushStack(h);
                    } else {
                        h.next = null;
                    }
                }
                CompletableFuture<?> d = h.tryFire(-1);
                f = d == null ? this : d;
            }
        }
    }

    final void cleanStack() {
        Completion p = null;
        Completion q = this.stack;
        while (q != null) {
            Completion s = q.next;
            if (q.isLive()) {
                p = q;
                q = s;
            } else if (p == null) {
                casStack(q, s);
                q = this.stack;
            } else {
                p.next = s;
                if (p.isLive()) {
                    q = s;
                } else {
                    p = null;
                    q = this.stack;
                }
            }
        }
    }

    final void push(UniCompletion<?, ?> c) {
        if (c != null) {
            while (this.result == null && (tryPushStack(c) ^ 1) != 0) {
                lazySetNext(c, null);
            }
        }
    }

    final CompletableFuture<T> postFire(CompletableFuture<?> a, int mode) {
        if (!(a == null || a.stack == null)) {
            if (mode < 0 || a.result == null) {
                a.cleanStack();
            } else {
                a.postComplete();
            }
        }
        if (!(this.result == null || this.stack == null)) {
            if (mode < 0) {
                return this;
            }
            postComplete();
        }
        return null;
    }

    final <S> boolean uniApply(CompletableFuture<S> a, Function<? super S, ? extends T> f, UniApply<S, T> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    completeValue(f.apply(r));
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniApplyStage(Executor e, Function<? super T, ? extends V> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<V> d = newIncompleteFuture();
        if (!(e == null && (d.uniApply(this, f, null) ^ 1) == 0)) {
            UniApply<T, V> c = new UniApply(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final <S> boolean uniAccept(CompletableFuture<S> a, Consumer<? super S> f, UniAccept<S> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    f.accept(r);
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Void> uniAcceptStage(Executor e, Consumer<? super T> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = newIncompleteFuture();
        if (!(e == null && (d.uniAccept(this, f, null) ^ 1) == 0)) {
            UniAccept<T> c = new UniAccept(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniRun(CompletableFuture<?> a, Runnable f, UniRun<?> c) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        }
                    }
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    f.run();
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Void> uniRunStage(Executor e, Runnable f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = newIncompleteFuture();
        if (!(e == null && (d.uniRun(this, f, null) ^ 1) == 0)) {
            UniRun<T> c = new UniRun(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniWhenComplete(CompletableFuture<T> a, BiConsumer<? super T, ? super Throwable> f, UniWhenComplete<T> c) {
        Throwable x = null;
        if (a != null) {
            T r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    Object t;
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            if (x == null) {
                                x = ex;
                            } else if (x != ex) {
                                x.addSuppressed(ex);
                            }
                        }
                    }
                    if (r instanceof AltResult) {
                        x = ((AltResult) r).ex;
                        t = null;
                    } else {
                        T tr = r;
                        T t2 = r;
                    }
                    f.accept(t2, x);
                    if (x == null) {
                        internalComplete(r);
                        return true;
                    }
                    completeThrowable(x, r);
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<T> uniWhenCompleteStage(Executor e, BiConsumer<? super T, ? super Throwable> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<T> d = newIncompleteFuture();
        if (!(e == null && (d.uniWhenComplete(this, f, null) ^ 1) == 0)) {
            UniWhenComplete<T> c = new UniWhenComplete(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final <S> boolean uniHandle(CompletableFuture<S> a, BiFunction<? super S, Throwable, ? extends T> f, UniHandle<S, T> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    Throwable x;
                    Object s;
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        x = ((AltResult) r).ex;
                        s = null;
                    } else {
                        x = null;
                        S ss = r;
                        S s2 = r;
                    }
                    completeValue(f.apply(s2, x));
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniHandleStage(Executor e, BiFunction<? super T, Throwable, ? extends V> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<V> d = newIncompleteFuture();
        if (!(e == null && (d.uniHandle(this, f, null) ^ 1) == 0)) {
            UniHandle<T, V> c = new UniHandle(e, d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniExceptionally(CompletableFuture<T> a, Function<? super Throwable, ? extends T> f, UniExceptionally<T> c) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    try {
                        if (r instanceof AltResult) {
                            Throwable x = ((AltResult) r).ex;
                            if (x != null) {
                                if (c != null && (c.claim() ^ 1) != 0) {
                                    return false;
                                }
                                completeValue(f.apply(x));
                            }
                        }
                        internalComplete(r);
                    } catch (Throwable ex) {
                        completeThrowable(ex);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<T> uniExceptionallyStage(Function<Throwable, ? extends T> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<T> d = newIncompleteFuture();
        if (!d.uniExceptionally(this, f, null)) {
            UniExceptionally<T> c = new UniExceptionally(d, this, f);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    final boolean uniRelay(CompletableFuture<T> a) {
        if (a != null) {
            Object r = a.result;
            if (r != null) {
                if (this.result == null) {
                    completeRelay(r);
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<T> uniCopyStage() {
        CompletableFuture<T> d = newIncompleteFuture();
        Object r = this.result;
        if (r != null) {
            d.completeRelay(r);
        } else {
            UniRelay<T> c = new UniRelay(d, this);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    private MinimalStage<T> uniAsMinimalStage() {
        Object r = this.result;
        if (r != null) {
            return new MinimalStage(encodeRelay(r));
        }
        MinimalStage<T> d = new MinimalStage();
        UniRelay<T> c = new UniRelay(d, this);
        push(c);
        c.tryFire(0);
        return d;
    }

    final <S> boolean uniCompose(CompletableFuture<S> a, Function<? super S, ? extends CompletionStage<T>> f, UniCompose<S, T> c) {
        if (a != null) {
            S r = a.result;
            if (!(r == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    CompletableFuture<T> g = ((CompletionStage) f.apply(r)).toCompletableFuture();
                    if (g.result == null || (uniRelay(g) ^ 1) != 0) {
                        UniRelay<T> copy = new UniRelay(this, g);
                        g.push(copy);
                        copy.tryFire(0);
                        if (this.result == null) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniComposeStage(Executor e, Function<? super T, ? extends CompletionStage<V>> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<V> d = newIncompleteFuture();
        if (e == null) {
            T r = this.result;
            if (r != null) {
                if (r instanceof AltResult) {
                    Throwable x = ((AltResult) r).ex;
                    if (x != null) {
                        d.result = encodeThrowable(x, r);
                        return d;
                    }
                    r = null;
                }
                try {
                    CompletableFuture<V> g = ((CompletionStage) f.apply(r)).toCompletableFuture();
                    Object s = g.result;
                    if (s != null) {
                        d.completeRelay(s);
                    } else {
                        UniRelay<V> c = new UniRelay(d, g);
                        g.push(c);
                        c.tryFire(0);
                    }
                    return d;
                } catch (Throwable ex) {
                    d.result = encodeThrowable(ex);
                    return d;
                }
            }
        }
        UniCompose<T, V> c2 = new UniCompose(e, d, this, f);
        push(c2);
        c2.tryFire(0);
        return d;
    }

    final void bipush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (true) {
                Object r = this.result;
                if (r == null && (tryPushStack(c) ^ 1) != 0) {
                    lazySetNext(c, null);
                } else if (b != null && b != this && b.result == null) {
                    Completion q = r != null ? c : new CoCompletion(c);
                    while (b.result == null && (b.tryPushStack(q) ^ 1) != 0) {
                        lazySetNext(q, null);
                    }
                    return;
                } else {
                    return;
                }
            }
            if (b != null) {
            }
        }
    }

    final CompletableFuture<T> postFire(CompletableFuture<?> a, CompletableFuture<?> b, int mode) {
        if (!(b == null || b.stack == null)) {
            if (mode < 0 || b.result == null) {
                b.cleanStack();
            } else {
                b.postComplete();
            }
        }
        return postFire(a, mode);
    }

    final <R, S> boolean biApply(CompletableFuture<R> a, CompletableFuture<S> b, BiFunction<? super R, ? super S, ? extends T> f, BiApply<R, S, T> c) {
        if (a != null) {
            R r = a.result;
            if (!(r == null || b == null)) {
                S s = b.result;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            } else {
                                r = null;
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            } else {
                                s = null;
                            }
                        }
                        if (c != null) {
                            try {
                                if ((c.claim() ^ 1) != 0) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        S ss = s;
                        completeValue(f.apply(r, s));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <U, V> CompletableFuture<V> biApplyStage(Executor e, CompletionStage<U> o, BiFunction<? super T, ? super U, ? extends V> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<V> d = newIncompleteFuture();
                if (!(e == null && (d.biApply(this, b, f, null) ^ 1) == 0)) {
                    BiApply<T, U, V> c = new BiApply(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    final <R, S> boolean biAccept(CompletableFuture<R> a, CompletableFuture<S> b, BiConsumer<? super R, ? super S> f, BiAccept<R, S> c) {
        if (a != null) {
            R r = a.result;
            if (!(r == null || b == null)) {
                S s = b.result;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            } else {
                                r = null;
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            } else {
                                s = null;
                            }
                        }
                        if (c != null) {
                            try {
                                if ((c.claim() ^ 1) != 0) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        S ss = s;
                        f.accept(r, s);
                        completeNull();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <U> CompletableFuture<Void> biAcceptStage(Executor e, CompletionStage<U> o, BiConsumer<? super T, ? super U> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && (d.biAccept(this, b, f, null) ^ 1) == 0)) {
                    BiAccept<T, U> c = new BiAccept(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    final boolean biRun(CompletableFuture<?> a, CompletableFuture<?> b, Runnable f, BiRun<?, ?> c) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || b == null)) {
                Object s = b.result;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            }
                        }
                        if (c != null) {
                            try {
                                if ((c.claim() ^ 1) != 0) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        f.run();
                        completeNull();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private CompletableFuture<Void> biRunStage(Executor e, CompletionStage<?> o, Runnable f) {
        if (f != null) {
            CompletableFuture<?> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && (d.biRun(this, b, f, null) ^ 1) == 0)) {
                    BiRun<T, ?> c = new BiRun(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    boolean biRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        if (a != null) {
            Object r = a.result;
            if (!(r == null || b == null)) {
                Object s = b.result;
                if (s != null) {
                    if (this.result == null) {
                        Throwable x;
                        if (r instanceof AltResult) {
                            x = ((AltResult) r).ex;
                            if (x != null) {
                                completeThrowable(x, r);
                            }
                        }
                        if (s instanceof AltResult) {
                            x = ((AltResult) s).ex;
                            if (x != null) {
                                completeThrowable(x, s);
                            }
                        }
                        completeNull();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    static CompletableFuture<Void> andTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<Void> d = new CompletableFuture();
        if (lo > hi) {
            d.result = NIL;
        } else {
            CompletableFuture<?> a;
            int mid = (lo + hi) >>> 1;
            if (lo == mid) {
                a = cfs[lo];
            } else {
                a = andTree(cfs, lo, mid);
            }
            if (a != null) {
                CompletableFuture<?> b = lo == hi ? a : hi == mid + 1 ? cfs[hi] : andTree(cfs, mid + 1, hi);
                if (b != null) {
                    if (!d.biRelay(a, b)) {
                        BiRelay<?, ?> c = new BiRelay(d, a, b);
                        a.bipush(b, c);
                        c.tryFire(0);
                    }
                }
            }
            throw new NullPointerException();
        }
        return d;
    }

    final void orpush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (true) {
                if ((b != null && b.result != null) || this.result != null) {
                    return;
                }
                if (!tryPushStack(c)) {
                    lazySetNext(c, null);
                } else if (b != null && b != this && b.result == null) {
                    Completion q = new CoCompletion(c);
                    while (this.result == null && b.result == null && (b.tryPushStack(q) ^ 1) != 0) {
                        lazySetNext(q, null);
                    }
                    return;
                } else {
                    return;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r2 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final <R, S extends R> boolean orApply(CompletableFuture<R> a, CompletableFuture<S> b, Function<? super R, ? extends T> f, OrApply<R, S, T> c) {
        if (!(a == null || b == null)) {
            R r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (f != null) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    R rr = r;
                    completeValue(f.apply(r));
                }
                return true;
            }
        }
        return false;
    }

    private <U extends T, V> CompletableFuture<V> orApplyStage(Executor e, CompletionStage<U> o, Function<? super T, ? extends V> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<V> d = newIncompleteFuture();
                if (!(e == null && (d.orApply(this, b, f, null) ^ 1) == 0)) {
                    OrApply<T, U, V> c = new OrApply(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r2 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final <R, S extends R> boolean orAccept(CompletableFuture<R> a, CompletableFuture<S> b, Consumer<? super R> f, OrAccept<R, S> c) {
        if (!(a == null || b == null)) {
            R r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (f != null) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    R rr = r;
                    f.accept(r);
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private <U extends T> CompletableFuture<Void> orAcceptStage(Executor e, CompletionStage<U> o, Consumer<? super T> f) {
        if (f != null) {
            CompletableFuture<U> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && (d.orAccept(this, b, f, null) ^ 1) == 0)) {
                    OrAccept<T, U> c = new OrAccept(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r2 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean orRun(CompletableFuture<?> a, CompletableFuture<?> b, Runnable f, OrRun<?, ?> c) {
        if (!(a == null || b == null)) {
            Object r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (f != null) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if ((c.claim() ^ 1) != 0) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        Throwable x = ((AltResult) r).ex;
                        if (x != null) {
                            completeThrowable(x, r);
                        }
                    }
                    f.run();
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private CompletableFuture<Void> orRunStage(Executor e, CompletionStage<?> o, Runnable f) {
        if (f != null) {
            CompletableFuture<?> b = o.toCompletableFuture();
            if (b != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (!(e == null && (d.orRun(this, b, f, null) ^ 1) == 0)) {
                    OrRun<T, ?> c = new OrRun(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Missing block: B:7:0x000c, code:
            if (r0 != null) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean orRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        if (!(a == null || b == null)) {
            Object r = a.result;
            if (r == null) {
                r = b.result;
            }
            if (this.result == null) {
                completeRelay(r);
            }
            return true;
        }
        return false;
    }

    static CompletableFuture<Object> orTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<Object> d = new CompletableFuture();
        if (lo <= hi) {
            CompletableFuture<?> a;
            int mid = (lo + hi) >>> 1;
            if (lo == mid) {
                a = cfs[lo];
            } else {
                a = orTree(cfs, lo, mid);
            }
            if (a != null) {
                CompletableFuture<?> b = lo == hi ? a : hi == mid + 1 ? cfs[hi] : orTree(cfs, mid + 1, hi);
                if (b != null) {
                    if (!d.orRelay(a, b)) {
                        OrRelay<?, ?> c = new OrRelay(d, a, b);
                        a.orpush(b, c);
                        c.tryFire(0);
                    }
                }
            }
            throw new NullPointerException();
        }
        return d;
    }

    static <U> CompletableFuture<U> asyncSupplyStage(Executor e, Supplier<U> f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<U> d = new CompletableFuture();
        e.execute(new AsyncSupply(d, f));
        return d;
    }

    static CompletableFuture<Void> asyncRunStage(Executor e, Runnable f) {
        if (f == null) {
            throw new NullPointerException();
        }
        CompletableFuture<Void> d = new CompletableFuture();
        e.execute(new AsyncRun(d, f));
        return d;
    }

    private Object waitingGet(boolean interruptible) {
        Object r;
        Signaller q = null;
        boolean queued = false;
        int spins = SPINS;
        while (true) {
            r = this.result;
            if (r == null) {
                if (spins <= 0) {
                    if (q != null) {
                        if (queued) {
                            try {
                                ForkJoinPool.managedBlock(q);
                            } catch (InterruptedException e) {
                                q.interrupted = true;
                            }
                            if (q.interrupted && interruptible) {
                                break;
                            }
                        }
                        queued = tryPushStack(q);
                    } else {
                        q = new Signaller(interruptible, 0, 0);
                    }
                } else if (ThreadLocalRandom.nextSecondarySeed() >= 0) {
                    spins--;
                }
            } else {
                break;
            }
        }
        if (q != null) {
            q.thread = null;
            if (q.interrupted) {
                if (interruptible) {
                    cleanStack();
                } else {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (r != null) {
            postComplete();
        }
        return r;
    }

    private Object timedGet(long nanos) throws TimeoutException {
        if (Thread.interrupted()) {
            return null;
        }
        if (nanos > 0) {
            Object r;
            long d = System.nanoTime() + nanos;
            long deadline = d == 0 ? 1 : d;
            Signaller q = null;
            boolean queued = false;
            while (true) {
                r = this.result;
                if (r != null) {
                    break;
                } else if (q == null) {
                    q = new Signaller(true, nanos, deadline);
                } else if (!queued) {
                    queued = tryPushStack(q);
                } else if (q.nanos <= 0) {
                    break;
                } else {
                    try {
                        ForkJoinPool.managedBlock(q);
                    } catch (InterruptedException e) {
                        q.interrupted = true;
                    }
                    if (q.interrupted) {
                        break;
                    }
                }
            }
            if (q != null) {
                q.thread = null;
            }
            if (r != null) {
                postComplete();
            } else {
                cleanStack();
            }
            if (r != null || (q != null && q.interrupted)) {
                return r;
            }
        }
        throw new TimeoutException();
    }

    CompletableFuture(Object r) {
        this.result = r;
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return asyncSupplyStage(ASYNC_POOL, supplier);
    }

    public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor) {
        return asyncSupplyStage(screenExecutor(executor), supplier);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return asyncRunStage(ASYNC_POOL, runnable);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return asyncRunStage(screenExecutor(executor), runnable);
    }

    public static <U> CompletableFuture<U> completedFuture(U value) {
        if (value == null) {
            value = NIL;
        }
        return new CompletableFuture(value);
    }

    public boolean isDone() {
        return this.result != null;
    }

    public T get() throws InterruptedException, ExecutionException {
        Object r = this.result;
        if (r == null) {
            r = waitingGet(true);
        }
        return reportGet(r);
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        Object r = this.result;
        if (r == null) {
            r = timedGet(nanos);
        }
        return reportGet(r);
    }

    public T join() {
        Object r = this.result;
        if (r == null) {
            r = waitingGet(false);
        }
        return reportJoin(r);
    }

    public T getNow(T valueIfAbsent) {
        Object r = this.result;
        return r == null ? valueIfAbsent : reportJoin(r);
    }

    public boolean complete(T value) {
        boolean triggered = completeValue(value);
        postComplete();
        return triggered;
    }

    public boolean completeExceptionally(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        boolean triggered = internalComplete(new AltResult(ex));
        postComplete();
        return triggered;
    }

    public <U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn) {
        return uniApplyStage(null, fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return uniApplyStage(defaultExecutor(), fn);
    }

    public <U> CompletableFuture<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return uniApplyStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<Void> thenAccept(Consumer<? super T> action) {
        return uniAcceptStage(null, action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action) {
        return uniAcceptStage(defaultExecutor(), action);
    }

    public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return uniAcceptStage(screenExecutor(executor), action);
    }

    public CompletableFuture<Void> thenRun(Runnable action) {
        return uniRunStage(null, action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action) {
        return uniRunStage(defaultExecutor(), action);
    }

    public CompletableFuture<Void> thenRunAsync(Runnable action, Executor executor) {
        return uniRunStage(screenExecutor(executor), action);
    }

    public <U, V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return biApplyStage(null, other, fn);
    }

    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return biApplyStage(defaultExecutor(), other, fn);
    }

    public <U, V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return biApplyStage(screenExecutor(executor), other, fn);
    }

    public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(null, other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return biAcceptStage(defaultExecutor(), other, action);
    }

    public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return biAcceptStage(screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
        return biRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
        return biRunStage(defaultExecutor(), other, action);
    }

    public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return biRunStage(screenExecutor(executor), other, action);
    }

    public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(null, other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn) {
        return orApplyStage(defaultExecutor(), other, fn);
    }

    public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return orApplyStage(screenExecutor(executor), other, fn);
    }

    public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(null, other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action) {
        return orAcceptStage(defaultExecutor(), other, action);
    }

    public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor) {
        return orAcceptStage(screenExecutor(executor), other, action);
    }

    public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
        return orRunStage(null, other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
        return orRunStage(defaultExecutor(), other, action);
    }

    public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
        return orRunStage(screenExecutor(executor), other, action);
    }

    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(null, fn);
    }

    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return uniComposeStage(defaultExecutor(), fn);
    }

    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) {
        return uniComposeStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(null, action);
    }

    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return uniWhenCompleteStage(defaultExecutor(), action);
    }

    public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return uniWhenCompleteStage(screenExecutor(executor), action);
    }

    public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(null, fn);
    }

    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return uniHandleStage(defaultExecutor(), fn);
    }

    public <U> CompletableFuture<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return uniHandleStage(screenExecutor(executor), fn);
    }

    public CompletableFuture<T> toCompletableFuture() {
        return this;
    }

    public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return uniExceptionallyStage(fn);
    }

    public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs) {
        return andTree(cfs, 0, cfs.length - 1);
    }

    public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs) {
        return orTree(cfs, 0, cfs.length - 1);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean cancelled;
        if (this.result == null) {
            cancelled = internalComplete(new AltResult(new CancellationException()));
        } else {
            cancelled = false;
        }
        postComplete();
        return !cancelled ? isCancelled() : true;
    }

    public boolean isCancelled() {
        Object r = this.result;
        if (r instanceof AltResult) {
            return ((AltResult) r).ex instanceof CancellationException;
        }
        return false;
    }

    public boolean isCompletedExceptionally() {
        AltResult r = this.result;
        return (r instanceof AltResult) && r != NIL;
    }

    public void obtrudeValue(T value) {
        if (value == null) {
            value = NIL;
        }
        this.result = value;
        postComplete();
    }

    public void obtrudeException(Throwable ex) {
        if (ex == null) {
            throw new NullPointerException();
        }
        this.result = new AltResult(ex);
        postComplete();
    }

    public int getNumberOfDependents() {
        int count = 0;
        for (Completion p = this.stack; p != null; p = p.next) {
            count++;
        }
        return count;
    }

    public String toString() {
        String str;
        Object r = this.result;
        int count = 0;
        for (Completion p = this.stack; p != null; p = p.next) {
            count++;
        }
        StringBuilder append = new StringBuilder().append(super.toString());
        if (r == null) {
            if (count == 0) {
                str = "[Not completed]";
            } else {
                str = "[Not completed, " + count + " dependents]";
            }
        } else if (!(r instanceof AltResult) || ((AltResult) r).ex == null) {
            str = "[Completed normally]";
        } else {
            str = "[Completed exceptionally]";
        }
        return append.append(str).toString();
    }

    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new CompletableFuture();
    }

    public Executor defaultExecutor() {
        return ASYNC_POOL;
    }

    public CompletableFuture<T> copy() {
        return uniCopyStage();
    }

    public CompletionStage<T> minimalCompletionStage() {
        return uniAsMinimalStage();
    }

    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
        if (supplier == null || executor == null) {
            throw new NullPointerException();
        }
        executor.execute(new AsyncSupply(this, supplier));
        return this;
    }

    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
        return completeAsync(supplier, defaultExecutor());
    }

    public CompletableFuture<T> orTimeout(long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        if (this.result == null) {
            whenComplete(new Canceller(Delayer.delay(new Timeout(this), timeout, unit)));
        }
        return this;
    }

    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        if (unit == null) {
            throw new NullPointerException();
        }
        if (this.result == null) {
            whenComplete(new Canceller(Delayer.delay(new DelayedCompleter(this, value), timeout, unit)));
        }
        return this;
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor) {
        if (unit != null && executor != null) {
            return new DelayedExecutor(delay, unit, executor);
        }
        throw new NullPointerException();
    }

    public static Executor delayedExecutor(long delay, TimeUnit unit) {
        if (unit != null) {
            return new DelayedExecutor(delay, unit, ASYNC_POOL);
        }
        throw new NullPointerException();
    }

    public static <U> CompletionStage<U> completedStage(U value) {
        if (value == null) {
            value = NIL;
        }
        return new MinimalStage(value);
    }

    public static <U> CompletableFuture<U> failedFuture(Throwable ex) {
        if (ex != null) {
            return new CompletableFuture(new AltResult(ex));
        }
        throw new NullPointerException();
    }

    public static <U> CompletionStage<U> failedStage(Throwable ex) {
        if (ex != null) {
            return new MinimalStage(new AltResult(ex));
        }
        throw new NullPointerException();
    }
}
