package java.util.concurrent;

import java.util.concurrent.ForkJoinPool;
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
    private static final boolean USE_COMMON_POOL = (ForkJoinPool.getCommonPoolParallelism() > 1);
    volatile Object result;
    volatile Completion stack;

    static final class AltResult {
        final Throwable ex;

        AltResult(Throwable x) {
            this.ex = x;
        }
    }

    static final class AsyncRun extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        CompletableFuture<Void> dep;
        Runnable fn;

        AsyncRun(CompletableFuture<Void> dep2, Runnable fn2) {
            this.dep = dep2;
            this.fn = fn2;
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
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                Runnable runnable = this.fn;
                Runnable f = runnable;
                if (runnable != null) {
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

        AsyncSupply(CompletableFuture<T> dep2, Supplier<? extends T> fn2) {
            this.dep = dep2;
            this.fn = fn2;
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
            CompletableFuture<T> completableFuture = this.dep;
            CompletableFuture<T> d = completableFuture;
            if (completableFuture != null) {
                Supplier<? extends T> supplier = this.fn;
                Supplier<? extends T> f = supplier;
                if (supplier != null) {
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

    public interface AsynchronousCompletionTask {
    }

    static final class BiAccept<T, U> extends BiCompletion<T, U, Void> {
        BiConsumer<? super T, ? super U> fn;

        BiAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, BiConsumer<? super T, ? super U> fn2) {
            super(executor, dep, src, snd);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.biAccept(completableFuture2, completableFuture4, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class BiApply<T, U, V> extends BiCompletion<T, U, V> {
        BiFunction<? super T, ? super U, ? extends V> fn;

        BiApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd, BiFunction<? super T, ? super U, ? extends V> fn2) {
            super(executor, dep, src, snd);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> completableFuture = this.dep;
            CompletableFuture<V> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.biApply(completableFuture2, completableFuture4, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static abstract class BiCompletion<T, U, V> extends UniCompletion<T, V> {
        CompletableFuture<U> snd;

        BiCompletion(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd2) {
            super(executor, dep, src);
            this.snd = snd2;
        }
    }

    static final class BiRelay<T, U> extends BiCompletion<T, U, Void> {
        BiRelay(CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.biRelay(completableFuture2, completableFuture4)) {
                    this.src = null;
                    this.snd = null;
                    this.dep = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class BiRun<T, U> extends BiCompletion<T, U, Void> {
        Runnable fn;

        BiRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Runnable fn2) {
            super(executor, dep, src, snd);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.biRun(completableFuture2, completableFuture4, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class Canceller implements BiConsumer<Object, Throwable> {
        final Future<?> f;

        Canceller(Future<?> f2) {
            this.f = f2;
        }

        public void accept(Object ignore, Throwable ex) {
            if (ex == null && this.f != null && !this.f.isDone()) {
                this.f.cancel(false);
            }
        }
    }

    static final class CoCompletion extends Completion {
        BiCompletion<?, ?, ?> base;

        CoCompletion(BiCompletion<?, ?, ?> base2) {
            this.base = base2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<?> tryFire(int mode) {
            BiCompletion<?, ?, ?> biCompletion = this.base;
            BiCompletion<?, ?, ?> c = biCompletion;
            if (biCompletion != null) {
                CompletableFuture<?> tryFire = c.tryFire(mode);
                CompletableFuture<?> d = tryFire;
                if (tryFire != null) {
                    this.base = null;
                    return d;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public final boolean isLive() {
            BiCompletion<?, ?, ?> c = this.base;
            return (c == null || c.dep == null) ? false : true;
        }
    }

    static abstract class Completion extends ForkJoinTask<Void> implements Runnable, AsynchronousCompletionTask {
        volatile Completion next;

        /* access modifiers changed from: package-private */
        public abstract boolean isLive();

        /* access modifiers changed from: package-private */
        public abstract CompletableFuture<?> tryFire(int i);

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

    static final class DelayedCompleter<U> implements Runnable {
        final CompletableFuture<U> f;
        final U u;

        DelayedCompleter(CompletableFuture<U> f2, U u2) {
            this.f = f2;
            this.u = u2;
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

        DelayedExecutor(long delay2, TimeUnit unit2, Executor executor2) {
            this.delay = delay2;
            this.unit = unit2;
            this.executor = executor2;
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
            ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, (ThreadFactory) new DaemonThreadFactory());
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

        OrAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Consumer<? super T> fn2) {
            super(executor, dep, src, snd);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.orAccept(completableFuture2, completableFuture4, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class OrApply<T, U extends T, V> extends BiCompletion<T, U, V> {
        Function<? super T, ? extends V> fn;

        OrApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Function<? super T, ? extends V> fn2) {
            super(executor, dep, src, snd);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> completableFuture = this.dep;
            CompletableFuture<V> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.orApply(completableFuture2, completableFuture4, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class OrRelay<T, U> extends BiCompletion<T, U, Object> {
        OrRelay(CompletableFuture<Object> dep, CompletableFuture<T> src, CompletableFuture<U> snd) {
            super(null, dep, src, snd);
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Object> tryFire(int mode) {
            CompletableFuture<Object> completableFuture = this.dep;
            CompletableFuture<Object> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.orRelay(completableFuture2, completableFuture4)) {
                    this.src = null;
                    this.snd = null;
                    this.dep = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class OrRun<T, U> extends BiCompletion<T, U, Void> {
        Runnable fn;

        OrRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, CompletableFuture<U> snd, Runnable fn2) {
            super(executor, dep, src, snd);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                CompletableFuture completableFuture4 = this.snd;
                CompletableFuture completableFuture5 = completableFuture4;
                if (d.orRun(completableFuture2, completableFuture4, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.snd = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, completableFuture5, mode);
                }
            }
            return null;
        }
    }

    static final class Signaller extends Completion implements ForkJoinPool.ManagedBlocker {
        final long deadline;
        boolean interrupted;
        final boolean interruptible;
        long nanos;
        volatile Thread thread = Thread.currentThread();

        Signaller(boolean interruptible2, long nanos2, long deadline2) {
            this.interruptible = interruptible2;
            this.nanos = nanos2;
            this.deadline = deadline2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<?> tryFire(int ignore) {
            Thread thread2 = this.thread;
            Thread w = thread2;
            if (thread2 != null) {
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
            if (this.thread == null) {
                return true;
            }
            return false;
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

        /* access modifiers changed from: package-private */
        public final boolean isLive() {
            return this.thread != null;
        }
    }

    static final class TaskSubmitter implements Runnable {
        final Runnable action;
        final Executor executor;

        TaskSubmitter(Executor executor2, Runnable action2) {
            this.executor = executor2;
            this.action = action2;
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

        Timeout(CompletableFuture<?> f2) {
            this.f = f2;
        }

        public void run() {
            if (this.f != null && !this.f.isDone()) {
                this.f.completeExceptionally(new TimeoutException());
            }
        }
    }

    static final class UniAccept<T> extends UniCompletion<T, Void> {
        Consumer<? super T> fn;

        UniAccept(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Consumer<? super T> fn2) {
            super(executor, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniAccept(completableFuture2, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static final class UniApply<T, V> extends UniCompletion<T, V> {
        Function<? super T, ? extends V> fn;

        UniApply(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T, ? extends V> fn2) {
            super(executor, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> completableFuture = this.dep;
            CompletableFuture<V> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniApply(completableFuture2, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static abstract class UniCompletion<T, V> extends Completion {
        CompletableFuture<V> dep;
        Executor executor;
        CompletableFuture<T> src;

        UniCompletion(Executor executor2, CompletableFuture<V> dep2, CompletableFuture<T> src2) {
            this.executor = executor2;
            this.dep = dep2;
            this.src = src2;
        }

        /* access modifiers changed from: package-private */
        public final boolean claim() {
            Executor e = this.executor;
            if (compareAndSetForkJoinTaskTag(0, 1)) {
                if (e == null) {
                    return true;
                }
                this.executor = null;
                e.execute(this);
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public final boolean isLive() {
            return this.dep != null;
        }
    }

    static final class UniCompose<T, V> extends UniCompletion<T, V> {
        Function<? super T, ? extends CompletionStage<V>> fn;

        UniCompose(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, Function<? super T, ? extends CompletionStage<V>> fn2) {
            super(executor, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> completableFuture = this.dep;
            CompletableFuture<V> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniCompose(completableFuture2, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static final class UniExceptionally<T> extends UniCompletion<T, T> {
        Function<? super Throwable, ? extends T> fn;

        UniExceptionally(CompletableFuture<T> dep, CompletableFuture<T> src, Function<? super Throwable, ? extends T> fn2) {
            super(null, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> completableFuture = this.dep;
            CompletableFuture<T> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniExceptionally(completableFuture2, this.fn, this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static final class UniHandle<T, V> extends UniCompletion<T, V> {
        BiFunction<? super T, Throwable, ? extends V> fn;

        UniHandle(Executor executor, CompletableFuture<V> dep, CompletableFuture<T> src, BiFunction<? super T, Throwable, ? extends V> fn2) {
            super(executor, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<V> tryFire(int mode) {
            CompletableFuture<V> completableFuture = this.dep;
            CompletableFuture<V> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniHandle(completableFuture2, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static final class UniRelay<T> extends UniCompletion<T, T> {
        UniRelay(CompletableFuture<T> dep, CompletableFuture<T> src) {
            super(null, dep, src);
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> completableFuture = this.dep;
            CompletableFuture<T> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniRelay(completableFuture2)) {
                    this.src = null;
                    this.dep = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static final class UniRun<T> extends UniCompletion<T, Void> {
        Runnable fn;

        UniRun(Executor executor, CompletableFuture<Void> dep, CompletableFuture<T> src, Runnable fn2) {
            super(executor, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<Void> tryFire(int mode) {
            CompletableFuture<Void> completableFuture = this.dep;
            CompletableFuture<Void> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniRun(completableFuture2, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    static final class UniWhenComplete<T> extends UniCompletion<T, T> {
        BiConsumer<? super T, ? super Throwable> fn;

        UniWhenComplete(Executor executor, CompletableFuture<T> dep, CompletableFuture<T> src, BiConsumer<? super T, ? super Throwable> fn2) {
            super(executor, dep, src);
            this.fn = fn2;
        }

        /* access modifiers changed from: package-private */
        public final CompletableFuture<T> tryFire(int mode) {
            CompletableFuture<T> completableFuture = this.dep;
            CompletableFuture<T> d = completableFuture;
            if (completableFuture != null) {
                CompletableFuture completableFuture2 = this.src;
                CompletableFuture completableFuture3 = completableFuture2;
                if (d.uniWhenComplete(completableFuture2, this.fn, mode > 0 ? null : this)) {
                    this.dep = null;
                    this.src = null;
                    this.fn = null;
                    return d.postFire(completableFuture3, mode);
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean internalComplete(Object r) {
        return U.compareAndSwapObject(this, RESULT, null, r);
    }

    /* access modifiers changed from: package-private */
    public final boolean casStack(Completion cmp, Completion val) {
        return U.compareAndSwapObject(this, STACK, cmp, val);
    }

    /* access modifiers changed from: package-private */
    public final boolean tryPushStack(Completion c) {
        Completion h = this.stack;
        lazySetNext(c, h);
        return U.compareAndSwapObject(this, STACK, h, c);
    }

    /* access modifiers changed from: package-private */
    public final void pushStack(Completion c) {
        do {
        } while (!tryPushStack(c));
    }

    static {
        Executor executor;
        int i = 0;
        if (USE_COMMON_POOL) {
            executor = ForkJoinPool.commonPool();
        } else {
            executor = new ThreadPerTaskExecutor();
        }
        ASYNC_POOL = executor;
        if (Runtime.getRuntime().availableProcessors() > 1) {
            i = 256;
        }
        SPINS = i;
        try {
            RESULT = U.objectFieldOffset(CompletableFuture.class.getDeclaredField("result"));
            STACK = U.objectFieldOffset(CompletableFuture.class.getDeclaredField("stack"));
            NEXT = U.objectFieldOffset(Completion.class.getDeclaredField("next"));
            Class<LockSupport> cls = LockSupport.class;
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    /* access modifiers changed from: package-private */
    public final boolean completeNull() {
        return U.compareAndSwapObject(this, RESULT, null, NIL);
    }

    /* access modifiers changed from: package-private */
    public final Object encodeValue(T t) {
        return t == null ? NIL : t;
    }

    /* access modifiers changed from: package-private */
    public final boolean completeValue(T t) {
        T t2;
        Unsafe unsafe = U;
        long j = RESULT;
        if (t == null) {
            t2 = NIL;
        } else {
            t2 = t;
        }
        return unsafe.compareAndSwapObject(this, j, null, t2);
    }

    static AltResult encodeThrowable(Throwable x) {
        return new AltResult(x instanceof CompletionException ? x : new CompletionException(x));
    }

    /* access modifiers changed from: package-private */
    public final boolean completeThrowable(Throwable x) {
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

    /* access modifiers changed from: package-private */
    public final boolean completeThrowable(Throwable x, Object r) {
        return U.compareAndSwapObject(this, RESULT, null, encodeThrowable(x, r));
    }

    /* access modifiers changed from: package-private */
    public Object encodeOutcome(T t, Throwable x) {
        if (x == null) {
            return t == null ? NIL : t;
        }
        return encodeThrowable(x);
    }

    static Object encodeRelay(Object r) {
        if (r instanceof AltResult) {
            Throwable th = ((AltResult) r).ex;
            Throwable x = th;
            if (th != null && !(x instanceof CompletionException)) {
                return new AltResult(new CompletionException(x));
            }
        }
        return r;
    }

    /* access modifiers changed from: package-private */
    public final boolean completeRelay(Object r) {
        return U.compareAndSwapObject(this, RESULT, null, encodeRelay(r));
    }

    private static <T> T reportGet(Object r) throws InterruptedException, ExecutionException {
        if (r == null) {
            throw new InterruptedException();
        } else if (!(r instanceof AltResult)) {
            return r;
        } else {
            Throwable th = ((AltResult) r).ex;
            Throwable x = th;
            if (th == null) {
                return null;
            }
            if (!(x instanceof CancellationException)) {
                if (x instanceof CompletionException) {
                    Throwable cause = x.getCause();
                    Throwable cause2 = cause;
                    if (cause != null) {
                        x = cause2;
                    }
                }
                throw new ExecutionException(x);
            }
            throw ((CancellationException) x);
        }
    }

    private static <T> T reportJoin(Object r) {
        if (!(r instanceof AltResult)) {
            return r;
        }
        Throwable th = ((AltResult) r).ex;
        Throwable x = th;
        if (th == null) {
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

    /* access modifiers changed from: package-private */
    public final void postComplete() {
        CompletableFuture completableFuture = this;
        while (true) {
            Completion completion = completableFuture.stack;
            Completion h = completion;
            if (completion == null) {
                if (completableFuture != this) {
                    completableFuture = this;
                    Completion completion2 = this.stack;
                    h = completion2;
                    if (completion2 == null) {
                        return;
                    }
                } else {
                    return;
                }
            }
            Completion completion3 = h.next;
            Completion t = completion3;
            if (completableFuture.casStack(h, completion3)) {
                if (t != null) {
                    if (completableFuture != this) {
                        pushStack(h);
                    } else {
                        h.next = null;
                    }
                }
                CompletableFuture<?> d = h.tryFire(-1);
                completableFuture = d == null ? this : d;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void cleanStack() {
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

    /* access modifiers changed from: package-private */
    public final void push(UniCompletion<?, ?> c) {
        if (c != null) {
            while (this.result == null && !tryPushStack(c)) {
                lazySetNext(c, null);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final CompletableFuture<T> postFire(CompletableFuture<?> a, int mode) {
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

    /* access modifiers changed from: package-private */
    public final <S> boolean uniApply(CompletableFuture<S> a, Function<? super S, ? extends T> f, UniApply<S, T> c) {
        if (a != null) {
            S s = a.result;
            S r = s;
            if (!(s == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable th = ((AltResult) r).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
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
        if (f != null) {
            CompletableFuture<V> d = newIncompleteFuture();
            if (e != null || !d.uniApply(this, f, null)) {
                UniApply<T, V> c = new UniApply<>(e, d, this, f);
                push(c);
                c.tryFire(0);
            }
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final <S> boolean uniAccept(CompletableFuture<S> a, Consumer<? super S> f, UniAccept<S> c) {
        if (a != null) {
            S s = a.result;
            S r = s;
            if (!(s == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable th = ((AltResult) r).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
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
        if (f != null) {
            CompletableFuture<Void> d = newIncompleteFuture();
            if (e != null || !d.uniAccept(this, f, null)) {
                UniAccept<T> c = new UniAccept<>(e, d, this, f);
                push(c);
                c.tryFire(0);
            }
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final boolean uniRun(CompletableFuture<?> a, Runnable f, UniRun<?> c) {
        if (a != null) {
            Object obj = a.result;
            Object r = obj;
            if (!(obj == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable th = ((AltResult) r).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r);
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
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
        if (f != null) {
            CompletableFuture<Void> d = newIncompleteFuture();
            if (e != null || !d.uniRun(this, f, null)) {
                UniRun<T> c = new UniRun<>(e, d, this, f);
                push(c);
                c.tryFire(0);
            }
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final boolean uniWhenComplete(CompletableFuture<T> a, BiConsumer<? super T, ? super Throwable> f, UniWhenComplete<T> c) {
        T tr;
        Throwable x = null;
        if (a != null) {
            T t = a.result;
            T r = t;
            if (!(t == null || f == null)) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
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
                        tr = null;
                    } else {
                        tr = r;
                    }
                    f.accept(tr, x);
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
        if (f != null) {
            CompletableFuture<T> d = newIncompleteFuture();
            if (e != null || !d.uniWhenComplete(this, f, null)) {
                UniWhenComplete<T> c = new UniWhenComplete<>(e, d, this, f);
                push(c);
                c.tryFire(0);
            }
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final <S> boolean uniHandle(CompletableFuture<S> a, BiFunction<? super S, Throwable, ? extends T> f, UniHandle<S, T> c) {
        S ss;
        Throwable x;
        if (a != null) {
            S s = a.result;
            S r = s;
            if (!(s == null || f == null)) {
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r instanceof AltResult) {
                        x = ((AltResult) r).ex;
                        ss = null;
                    } else {
                        x = null;
                        ss = r;
                    }
                    completeValue(f.apply(ss, x));
                }
                return true;
            }
        }
        return false;
    }

    private <V> CompletableFuture<V> uniHandleStage(Executor e, BiFunction<? super T, Throwable, ? extends V> f) {
        if (f != null) {
            CompletableFuture<V> d = newIncompleteFuture();
            if (e != null || !d.uniHandle(this, f, null)) {
                UniHandle<T, V> c = new UniHandle<>(e, d, this, f);
                push(c);
                c.tryFire(0);
            }
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final boolean uniExceptionally(CompletableFuture<T> a, Function<? super Throwable, ? extends T> f, UniExceptionally<T> c) {
        if (a != null) {
            Object obj = a.result;
            Object r = obj;
            if (!(obj == null || f == null)) {
                if (this.result == null) {
                    try {
                        if (r instanceof AltResult) {
                            Throwable th = ((AltResult) r).ex;
                            Throwable x = th;
                            if (th != null) {
                                if (c != null && !c.claim()) {
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
        if (f != null) {
            CompletableFuture<T> d = newIncompleteFuture();
            if (!d.uniExceptionally(this, f, null)) {
                UniExceptionally<T> c = new UniExceptionally<>(d, this, f);
                push(c);
                c.tryFire(0);
            }
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final boolean uniRelay(CompletableFuture<T> a) {
        if (a != null) {
            Object obj = a.result;
            Object r = obj;
            if (obj != null) {
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
        Object obj = this.result;
        Object r = obj;
        if (obj != null) {
            d.completeRelay(r);
        } else {
            UniRelay<T> c = new UniRelay<>(d, this);
            push(c);
            c.tryFire(0);
        }
        return d;
    }

    private MinimalStage<T> uniAsMinimalStage() {
        Object obj = this.result;
        Object r = obj;
        if (obj != null) {
            return new MinimalStage<>(encodeRelay(r));
        }
        MinimalStage<T> d = new MinimalStage<>();
        UniRelay<T> c = new UniRelay<>(d, this);
        push(c);
        c.tryFire(0);
        return d;
    }

    /* access modifiers changed from: package-private */
    public final <S> boolean uniCompose(CompletableFuture<S> a, Function<? super S, ? extends CompletionStage<T>> f, UniCompose<S, T> c) {
        if (a != null) {
            S s = a.result;
            S r = s;
            if (!(s == null || f == null)) {
                if (this.result == null) {
                    if (r instanceof AltResult) {
                        Throwable th = ((AltResult) r).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r);
                        } else {
                            r = null;
                        }
                    }
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    CompletableFuture<T> g = ((CompletionStage) f.apply(r)).toCompletableFuture();
                    if (g.result == null || !uniRelay(g)) {
                        UniRelay<T> copy = new UniRelay<>(this, g);
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
        if (f != null) {
            CompletableFuture<V> d = newIncompleteFuture();
            if (e == null) {
                T t = this.result;
                T r = t;
                if (t != null) {
                    if (r instanceof AltResult) {
                        Throwable th = ((AltResult) r).ex;
                        Throwable x = th;
                        if (th != null) {
                            d.result = encodeThrowable(x, r);
                            return d;
                        }
                        r = null;
                    }
                    try {
                        CompletableFuture<V> g = ((CompletionStage) f.apply(r)).toCompletableFuture();
                        Object obj = g.result;
                        Object s = obj;
                        if (obj != null) {
                            d.completeRelay(s);
                        } else {
                            UniRelay<V> c = new UniRelay<>(d, g);
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
            UniCompose<T, V> c2 = new UniCompose<>(e, d, this, f);
            push(c2);
            c2.tryFire(0);
            return d;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final void bipush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (true) {
                Object obj = this.result;
                Object r = obj;
                if (obj == null && !tryPushStack(c)) {
                    lazySetNext(c, null);
                } else if (b != null && b != this && b.result == null) {
                    Completion q = r != null ? c : new CoCompletion(c);
                    while (b.result == null && !b.tryPushStack(q)) {
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

    /* access modifiers changed from: package-private */
    public final CompletableFuture<T> postFire(CompletableFuture<?> a, CompletableFuture<?> b, int mode) {
        if (!(b == null || b.stack == null)) {
            if (mode < 0 || b.result == null) {
                b.cleanStack();
            } else {
                b.postComplete();
            }
        }
        return postFire(a, mode);
    }

    /* access modifiers changed from: package-private */
    public final <R, S> boolean biApply(CompletableFuture<R> a, CompletableFuture<S> b, BiFunction<? super R, ? super S, ? extends T> f, BiApply<R, S, T> c) {
        if (a != null) {
            R r = a.result;
            R r2 = r;
            if (!(r == null || b == null)) {
                S s = b.result;
                S s2 = s;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        if (r2 instanceof AltResult) {
                            Throwable th = ((AltResult) r2).ex;
                            Throwable x = th;
                            if (th != null) {
                                completeThrowable(x, r2);
                            } else {
                                r2 = null;
                            }
                        }
                        if (s2 instanceof AltResult) {
                            Throwable th2 = ((AltResult) s2).ex;
                            Throwable x2 = th2;
                            if (th2 != null) {
                                completeThrowable(x2, s2);
                            } else {
                                s2 = null;
                            }
                        }
                        if (c != null) {
                            try {
                                if (!c.claim()) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        completeValue(f.apply(r2, s2));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private <U, V> CompletableFuture<V> biApplyStage(Executor e, CompletionStage<U> o, BiFunction<? super T, ? super U, ? extends V> f) {
        if (f != null) {
            CompletableFuture<U> completableFuture = o.toCompletableFuture();
            CompletableFuture<U> b = completableFuture;
            if (completableFuture != null) {
                CompletableFuture<V> d = newIncompleteFuture();
                if (e != null || !d.biApply(this, b, f, null)) {
                    BiApply<T, U, V> c = new BiApply<>(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final <R, S> boolean biAccept(CompletableFuture<R> a, CompletableFuture<S> b, BiConsumer<? super R, ? super S> f, BiAccept<R, S> c) {
        if (a != null) {
            R r = a.result;
            R r2 = r;
            if (!(r == null || b == null)) {
                S s = b.result;
                S s2 = s;
                if (!(s == null || f == null)) {
                    if (this.result == null) {
                        if (r2 instanceof AltResult) {
                            Throwable th = ((AltResult) r2).ex;
                            Throwable x = th;
                            if (th != null) {
                                completeThrowable(x, r2);
                            } else {
                                r2 = null;
                            }
                        }
                        if (s2 instanceof AltResult) {
                            Throwable th2 = ((AltResult) s2).ex;
                            Throwable x2 = th2;
                            if (th2 != null) {
                                completeThrowable(x2, s2);
                            } else {
                                s2 = null;
                            }
                        }
                        if (c != null) {
                            try {
                                if (!c.claim()) {
                                    return false;
                                }
                            } catch (Throwable ex) {
                                completeThrowable(ex);
                            }
                        }
                        f.accept(r2, s2);
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
            CompletableFuture<U> completableFuture = o.toCompletableFuture();
            CompletableFuture<U> b = completableFuture;
            if (completableFuture != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (e != null || !d.biAccept(this, b, f, null)) {
                    BiAccept<T, U> c = new BiAccept<>(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final boolean biRun(CompletableFuture<?> a, CompletableFuture<?> b, Runnable f, BiRun<?, ?> c) {
        if (a != null) {
            Object obj = a.result;
            Object r = obj;
            if (!(obj == null || b == null)) {
                Object obj2 = b.result;
                Object s = obj2;
                if (!(obj2 == null || f == null)) {
                    if (this.result == null) {
                        if (r instanceof AltResult) {
                            Throwable th = ((AltResult) r).ex;
                            Throwable x = th;
                            if (th != null) {
                                completeThrowable(x, r);
                            }
                        }
                        if (s instanceof AltResult) {
                            Throwable th2 = ((AltResult) s).ex;
                            Throwable x2 = th2;
                            if (th2 != null) {
                                completeThrowable(x2, s);
                            }
                        }
                        if (c != null) {
                            try {
                                if (!c.claim()) {
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
            CompletableFuture<?> completableFuture = o.toCompletableFuture();
            CompletableFuture<?> b = completableFuture;
            if (completableFuture != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (e != null || !d.biRun(this, b, f, null)) {
                    BiRun<T, ?> c = new BiRun<>(e, d, this, b, f);
                    bipush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public boolean biRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        if (a != null) {
            Object obj = a.result;
            Object r = obj;
            if (!(obj == null || b == null)) {
                Object obj2 = b.result;
                Object s = obj2;
                if (obj2 != null) {
                    if (this.result == null) {
                        if (r instanceof AltResult) {
                            Throwable th = ((AltResult) r).ex;
                            Throwable x = th;
                            if (th != null) {
                                completeThrowable(x, r);
                            }
                        }
                        if (s instanceof AltResult) {
                            Throwable th2 = ((AltResult) s).ex;
                            Throwable x2 = th2;
                            if (th2 != null) {
                                completeThrowable(x2, s);
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
        CompletableFuture<Void> completableFuture;
        CompletableFuture<Void> completableFuture2;
        CompletableFuture<Void> d = new CompletableFuture<>();
        if (lo > hi) {
            d.result = NIL;
        } else {
            int mid = (lo + hi) >>> 1;
            if (lo == mid) {
                completableFuture = cfs[lo];
            } else {
                completableFuture = andTree(cfs, lo, mid);
            }
            CompletableFuture<Void> completableFuture3 = completableFuture;
            if (completableFuture != null) {
                if (lo == hi) {
                    completableFuture2 = completableFuture3;
                } else {
                    completableFuture2 = hi == mid + 1 ? cfs[hi] : andTree(cfs, mid + 1, hi);
                }
                CompletableFuture<Void> completableFuture4 = completableFuture2;
                if (completableFuture2 != null) {
                    if (!d.biRelay(completableFuture3, completableFuture4)) {
                        BiRelay<?, ?> c = new BiRelay<>(d, completableFuture3, completableFuture4);
                        completableFuture3.bipush(completableFuture4, c);
                        c.tryFire(0);
                    }
                }
            }
            throw new NullPointerException();
        }
        return d;
    }

    /* access modifiers changed from: package-private */
    public final void orpush(CompletableFuture<?> b, BiCompletion<?, ?, ?> c) {
        if (c != null) {
            while (true) {
                if ((b != null && b.result != null) || this.result != null) {
                    return;
                }
                if (!tryPushStack(c)) {
                    lazySetNext(c, null);
                } else if (b != null && b != this && b.result == null) {
                    Completion q = new CoCompletion(c);
                    while (this.result == null && b.result == null && !b.tryPushStack(q)) {
                        lazySetNext(q, null);
                    }
                    return;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000d, code lost:
        if (r1 != null) goto L_0x000f;
     */
    public final <R, S extends R> boolean orApply(CompletableFuture<R> a, CompletableFuture<S> b, Function<? super R, ? extends T> f, OrApply<R, S, T> c) {
        if (!(a == null || b == null)) {
            Object obj = a.result;
            Object r = obj;
            if (obj == null) {
                Object obj2 = b.result;
                r = obj2;
            }
            if (f != null) {
                Object r2 = r;
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r2 instanceof AltResult) {
                        Throwable th = ((AltResult) r2).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r2);
                        } else {
                            r2 = null;
                        }
                    }
                    completeValue(f.apply(r2));
                }
                return true;
            }
        }
        return false;
    }

    private <U extends T, V> CompletableFuture<V> orApplyStage(Executor e, CompletionStage<U> o, Function<? super T, ? extends V> f) {
        if (f != null) {
            CompletableFuture<U> completableFuture = o.toCompletableFuture();
            CompletableFuture<U> b = completableFuture;
            if (completableFuture != null) {
                CompletableFuture<V> d = newIncompleteFuture();
                if (e != null || !d.orApply(this, b, f, null)) {
                    OrApply<T, U, V> c = new OrApply<>(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000d, code lost:
        if (r1 != null) goto L_0x000f;
     */
    public final <R, S extends R> boolean orAccept(CompletableFuture<R> a, CompletableFuture<S> b, Consumer<? super R> f, OrAccept<R, S> c) {
        if (!(a == null || b == null)) {
            Object obj = a.result;
            Object r = obj;
            if (obj == null) {
                Object obj2 = b.result;
                r = obj2;
            }
            if (f != null) {
                Object r2 = r;
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r2 instanceof AltResult) {
                        Throwable th = ((AltResult) r2).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r2);
                        } else {
                            r2 = null;
                        }
                    }
                    f.accept(r2);
                    completeNull();
                }
                return true;
            }
        }
        return false;
    }

    private <U extends T> CompletableFuture<Void> orAcceptStage(Executor e, CompletionStage<U> o, Consumer<? super T> f) {
        if (f != null) {
            CompletableFuture<U> completableFuture = o.toCompletableFuture();
            CompletableFuture<U> b = completableFuture;
            if (completableFuture != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (e != null || !d.orAccept(this, b, f, null)) {
                    OrAccept<T, U> c = new OrAccept<>(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000d, code lost:
        if (r1 != null) goto L_0x000f;
     */
    public final boolean orRun(CompletableFuture<?> a, CompletableFuture<?> b, Runnable f, OrRun<?, ?> c) {
        if (!(a == null || b == null)) {
            Object obj = a.result;
            Object r = obj;
            if (obj == null) {
                Object obj2 = b.result;
                r = obj2;
            }
            if (f != null) {
                Object r2 = r;
                if (this.result == null) {
                    if (c != null) {
                        try {
                            if (!c.claim()) {
                                return false;
                            }
                        } catch (Throwable ex) {
                            completeThrowable(ex);
                        }
                    }
                    if (r2 instanceof AltResult) {
                        Throwable th = ((AltResult) r2).ex;
                        Throwable x = th;
                        if (th != null) {
                            completeThrowable(x, r2);
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
            CompletableFuture<?> completableFuture = o.toCompletableFuture();
            CompletableFuture<?> b = completableFuture;
            if (completableFuture != null) {
                CompletableFuture<Void> d = newIncompleteFuture();
                if (e != null || !d.orRun(this, b, f, null)) {
                    OrRun<T, ?> c = new OrRun<>(e, d, this, b, f);
                    orpush(b, c);
                    c.tryFire(0);
                }
                return d;
            }
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x000c, code lost:
        if (r0 == null) goto L_0x0019;
     */
    public final boolean orRelay(CompletableFuture<?> a, CompletableFuture<?> b) {
        if (!(a == null || b == null)) {
            Object obj = a.result;
            Object r = obj;
            if (obj == null) {
                Object obj2 = b.result;
                r = obj2;
            }
            Object r2 = r;
            if (this.result == null) {
                completeRelay(r2);
            }
            return true;
        }
        return false;
    }

    static CompletableFuture<Object> orTree(CompletableFuture<?>[] cfs, int lo, int hi) {
        CompletableFuture<Object> completableFuture;
        CompletableFuture<Object> completableFuture2;
        CompletableFuture<Object> d = new CompletableFuture<>();
        if (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (lo == mid) {
                completableFuture = cfs[lo];
            } else {
                completableFuture = orTree(cfs, lo, mid);
            }
            CompletableFuture<Object> completableFuture3 = completableFuture;
            if (completableFuture != null) {
                if (lo == hi) {
                    completableFuture2 = completableFuture3;
                } else {
                    completableFuture2 = hi == mid + 1 ? cfs[hi] : orTree(cfs, mid + 1, hi);
                }
                CompletableFuture<Object> completableFuture4 = completableFuture2;
                if (completableFuture2 != null) {
                    if (!d.orRelay(completableFuture3, completableFuture4)) {
                        OrRelay<?, ?> c = new OrRelay<>(d, completableFuture3, completableFuture4);
                        completableFuture3.orpush(completableFuture4, c);
                        c.tryFire(0);
                    }
                }
            }
            throw new NullPointerException();
        }
        return d;
    }

    static <U> CompletableFuture<U> asyncSupplyStage(Executor e, Supplier<U> f) {
        if (f != null) {
            CompletableFuture<U> d = new CompletableFuture<>();
            e.execute(new AsyncSupply(d, f));
            return d;
        }
        throw new NullPointerException();
    }

    static CompletableFuture<Void> asyncRunStage(Executor e, Runnable f) {
        if (f != null) {
            CompletableFuture<Void> d = new CompletableFuture<>();
            e.execute(new AsyncRun(d, f));
            return d;
        }
        throw new NullPointerException();
    }

    private Object waitingGet(boolean interruptible) {
        Object r;
        Signaller q = null;
        boolean queued = false;
        int spins = SPINS;
        while (true) {
            Object obj = this.result;
            r = obj;
            if (obj == null) {
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
                        } else {
                            queued = tryPushStack(q);
                        }
                    } else {
                        Signaller signaller = new Signaller(interruptible, 0, 0);
                        q = signaller;
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
        Object r;
        if (Thread.interrupted()) {
            return null;
        }
        if (nanos > 0) {
            long d = System.nanoTime() + nanos;
            long deadline = d == 0 ? 1 : d;
            boolean queued = false;
            Signaller q = null;
            while (true) {
                boolean queued2 = queued;
                Object obj = this.result;
                r = obj;
                if (obj != null) {
                    break;
                }
                if (q != null) {
                    if (queued2) {
                        if (q.nanos > 0) {
                            try {
                                ForkJoinPool.managedBlock(q);
                            } catch (InterruptedException e) {
                                InterruptedException interruptedException = e;
                                q.interrupted = true;
                            }
                            if (q.interrupted) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        queued = tryPushStack(q);
                    }
                } else {
                    Signaller signaller = new Signaller(true, nanos, deadline);
                    q = signaller;
                }
                queued = queued2;
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

    public CompletableFuture() {
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
        return new CompletableFuture<>(value == null ? NIL : value);
    }

    public boolean isDone() {
        return this.result != null;
    }

    public T get() throws InterruptedException, ExecutionException {
        Object r = this.result;
        return reportGet(r == null ? waitingGet(true) : r);
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        Object r = this.result;
        return reportGet(r == null ? timedGet(nanos) : r);
    }

    public T join() {
        Object r = this.result;
        return reportJoin(r == null ? waitingGet(false) : r);
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
        if (ex != null) {
            boolean triggered = internalComplete(new AltResult(ex));
            postComplete();
            return triggered;
        }
        throw new NullPointerException();
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
        boolean cancelled = this.result == null && internalComplete(new AltResult(new CancellationException()));
        postComplete();
        if (cancelled || isCancelled()) {
            return true;
        }
        return false;
    }

    public boolean isCancelled() {
        Object r = this.result;
        return (r instanceof AltResult) && (((AltResult) r).ex instanceof CancellationException);
    }

    public boolean isCompletedExceptionally() {
        Object r = this.result;
        return (r instanceof AltResult) && r != NIL;
    }

    public void obtrudeValue(T value) {
        this.result = value == null ? NIL : value;
        postComplete();
    }

    public void obtrudeException(Throwable ex) {
        if (ex != null) {
            this.result = new AltResult(ex);
            postComplete();
            return;
        }
        throw new NullPointerException();
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
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
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
        sb.append(str);
        return sb.toString();
    }

    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new CompletableFuture<>();
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
        if (unit != null) {
            if (this.result == null) {
                whenComplete(new Canceller(Delayer.delay(new Timeout(this), timeout, unit)));
            }
            return this;
        }
        throw new NullPointerException();
    }

    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        if (unit != null) {
            if (this.result == null) {
                whenComplete(new Canceller(Delayer.delay(new DelayedCompleter(this, value), timeout, unit)));
            }
            return this;
        }
        throw new NullPointerException();
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
        return new MinimalStage(value == null ? NIL : value);
    }

    public static <U> CompletableFuture<U> failedFuture(Throwable ex) {
        if (ex != null) {
            return new CompletableFuture<>(new AltResult(ex));
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
