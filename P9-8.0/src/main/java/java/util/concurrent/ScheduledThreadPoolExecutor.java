package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {
    private static final long DEFAULT_KEEPALIVE_MILLIS = 10;
    private static final AtomicLong sequencer = new AtomicLong();
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;
    private volatile boolean executeExistingDelayedTasksAfterShutdown = true;
    volatile boolean removeOnCancel;

    static class DelayedWorkQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {
        private static final int INITIAL_CAPACITY = 16;
        private final Condition available = this.lock.newCondition();
        private Thread leader;
        private final ReentrantLock lock = new ReentrantLock();
        private RunnableScheduledFuture<?>[] queue = new RunnableScheduledFuture[16];
        private int size;

        private class Itr implements Iterator<Runnable> {
            final RunnableScheduledFuture<?>[] array;
            int cursor;
            int lastRet = -1;

            Itr(RunnableScheduledFuture<?>[] array) {
                this.array = array;
            }

            public boolean hasNext() {
                return this.cursor < this.array.length;
            }

            public Runnable next() {
                if (this.cursor >= this.array.length) {
                    throw new NoSuchElementException();
                }
                this.lastRet = this.cursor;
                RunnableScheduledFuture[] runnableScheduledFutureArr = this.array;
                int i = this.cursor;
                this.cursor = i + 1;
                return runnableScheduledFutureArr[i];
            }

            public void remove() {
                if (this.lastRet < 0) {
                    throw new IllegalStateException();
                }
                DelayedWorkQueue.this.remove(this.array[this.lastRet]);
                this.lastRet = -1;
            }
        }

        DelayedWorkQueue() {
        }

        private void setIndex(RunnableScheduledFuture<?> f, int idx) {
            if (f instanceof ScheduledFutureTask) {
                ((ScheduledFutureTask) f).heapIndex = idx;
            }
        }

        private void siftUp(int k, RunnableScheduledFuture<?> key) {
            while (k > 0) {
                int parent = (k - 1) >>> 1;
                RunnableScheduledFuture<?> e = this.queue[parent];
                if (key.compareTo(e) >= 0) {
                    break;
                }
                this.queue[k] = e;
                setIndex(e, k);
                k = parent;
            }
            this.queue[k] = key;
            setIndex(key, k);
        }

        private void siftDown(int k, RunnableScheduledFuture<?> key) {
            int half = this.size >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                RunnableScheduledFuture<?> c = this.queue[child];
                int right = child + 1;
                if (right < this.size && c.compareTo(this.queue[right]) > 0) {
                    child = right;
                    c = this.queue[right];
                }
                if (key.compareTo(c) <= 0) {
                    break;
                }
                this.queue[k] = c;
                setIndex(c, k);
                k = child;
            }
            this.queue[k] = key;
            setIndex(key, k);
        }

        private void grow() {
            int oldCapacity = this.queue.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity < 0) {
                newCapacity = Integer.MAX_VALUE;
            }
            this.queue = (RunnableScheduledFuture[]) Arrays.copyOf(this.queue, newCapacity);
        }

        private int indexOf(Object x) {
            if (x != null) {
                int i;
                if (x instanceof ScheduledFutureTask) {
                    i = ((ScheduledFutureTask) x).heapIndex;
                    if (i >= 0 && i < this.size && this.queue[i] == x) {
                        return i;
                    }
                }
                for (i = 0; i < this.size; i++) {
                    if (x.lambda$-java_util_function_Predicate_4628(this.queue[i])) {
                        return i;
                    }
                }
            }
            return -1;
        }

        public boolean contains(Object x) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                boolean z = indexOf(x) != -1;
                lock.unlock();
                return z;
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        public boolean remove(Object x) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = indexOf(x);
                if (i < 0) {
                    return false;
                }
                setIndex(this.queue[i], -1);
                int s = this.size - 1;
                this.size = s;
                RunnableScheduledFuture<?> replacement = this.queue[s];
                this.queue[s] = null;
                if (s != i) {
                    siftDown(i, replacement);
                    if (this.queue[i] == replacement) {
                        siftUp(i, replacement);
                    }
                }
                lock.unlock();
                return true;
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = this.size;
                return i;
            } finally {
                lock.unlock();
            }
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        public int remainingCapacity() {
            return Integer.MAX_VALUE;
        }

        public RunnableScheduledFuture<?> peek() {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> runnableScheduledFuture = this.queue[0];
                return runnableScheduledFuture;
            } finally {
                lock.unlock();
            }
        }

        public boolean offer(Runnable x) {
            if (x == null) {
                throw new NullPointerException();
            }
            RunnableScheduledFuture<?> e = (RunnableScheduledFuture) x;
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int i = this.size;
                if (i >= this.queue.length) {
                    grow();
                }
                this.size = i + 1;
                if (i == 0) {
                    this.queue[0] = e;
                    setIndex(e, 0);
                } else {
                    siftUp(i, e);
                }
                if (this.queue[0] == e) {
                    this.leader = null;
                    this.available.signal();
                }
                lock.unlock();
                return true;
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        public void put(Runnable e) {
            offer(e);
        }

        public boolean add(Runnable e) {
            return offer(e);
        }

        public boolean offer(Runnable e, long timeout, TimeUnit unit) {
            return offer(e);
        }

        private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
            int s = this.size - 1;
            this.size = s;
            RunnableScheduledFuture<?> x = this.queue[s];
            this.queue[s] = null;
            if (s != 0) {
                siftDown(0, x);
            }
            setIndex(f, -1);
            return f;
        }

        public RunnableScheduledFuture<?> poll() {
            RunnableScheduledFuture<?> runnableScheduledFuture = null;
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                RunnableScheduledFuture<?> first = this.queue[0];
                if (first != null && first.getDelay(TimeUnit.NANOSECONDS) <= 0) {
                    runnableScheduledFuture = finishPoll(first);
                }
                lock.unlock();
                return runnableScheduledFuture;
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        public RunnableScheduledFuture<?> take() throws InterruptedException {
            ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            while (true) {
                Thread thisThread;
                try {
                    RunnableScheduledFuture<?> first = this.queue[0];
                    if (first == null) {
                        this.available.await();
                    } else {
                        long delay = first.getDelay(TimeUnit.NANOSECONDS);
                        if (delay <= 0) {
                            RunnableScheduledFuture<?> finishPoll = finishPoll(first);
                            if (this.leader == null && this.queue[0] != null) {
                                this.available.signal();
                            }
                            lock.unlock();
                            return finishPoll;
                        } else if (this.leader != null) {
                            this.available.await();
                        } else {
                            thisThread = Thread.currentThread();
                            this.leader = thisThread;
                            this.available.awaitNanos(delay);
                            if (this.leader == thisThread) {
                                this.leader = null;
                            }
                        }
                    }
                } catch (Throwable th) {
                    if (this.leader == null && this.queue[0] != null) {
                        this.available.signal();
                    }
                    lock.unlock();
                }
            }
        }

        public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            while (true) {
                Thread thisThread;
                try {
                    RunnableScheduledFuture<?> first = this.queue[0];
                    if (first != null) {
                        long delay = first.getDelay(TimeUnit.NANOSECONDS);
                        if (delay <= 0) {
                            RunnableScheduledFuture<?> finishPoll = finishPoll(first);
                            if (this.leader == null && this.queue[0] != null) {
                                this.available.signal();
                            }
                            lock.unlock();
                            return finishPoll;
                        } else if (nanos <= 0) {
                            if (this.leader == null && this.queue[0] != null) {
                                this.available.signal();
                            }
                            lock.unlock();
                            return null;
                        } else {
                            if (nanos >= delay) {
                                if (this.leader == null) {
                                    thisThread = Thread.currentThread();
                                    this.leader = thisThread;
                                    nanos -= delay - this.available.awaitNanos(delay);
                                    if (this.leader == thisThread) {
                                        this.leader = null;
                                    }
                                }
                            }
                            nanos = this.available.awaitNanos(nanos);
                        }
                    } else if (nanos <= 0) {
                        if (this.leader == null && this.queue[0] != null) {
                            this.available.signal();
                        }
                        lock.unlock();
                        return null;
                    } else {
                        nanos = this.available.awaitNanos(nanos);
                    }
                } catch (Throwable th) {
                    if (this.leader == null && this.queue[0] != null) {
                        this.available.signal();
                    }
                    lock.unlock();
                }
            }
        }

        public void clear() {
            ReentrantLock lock = this.lock;
            lock.lock();
            int i = 0;
            while (i < this.size) {
                try {
                    RunnableScheduledFuture<?> t = this.queue[i];
                    if (t != null) {
                        this.queue[i] = null;
                        setIndex(t, -1);
                    }
                    i++;
                } finally {
                    lock.unlock();
                }
            }
            this.size = 0;
        }

        private RunnableScheduledFuture<?> peekExpired() {
            RunnableScheduledFuture<?> first = this.queue[0];
            return (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) ? null : first;
        }

        public int drainTo(Collection<? super Runnable> c) {
            if (c == null) {
                throw new NullPointerException();
            } else if (c == this) {
                throw new IllegalArgumentException();
            } else {
                ReentrantLock lock = this.lock;
                lock.lock();
                int n = 0;
                while (true) {
                    try {
                        RunnableScheduledFuture<?> first = peekExpired();
                        if (first == null) {
                            break;
                        }
                        c.add(first);
                        finishPoll(first);
                        n++;
                    } finally {
                        lock.unlock();
                    }
                }
                return n;
            }
        }

        public int drainTo(Collection<? super Runnable> c, int maxElements) {
            if (c == null) {
                throw new NullPointerException();
            } else if (c == this) {
                throw new IllegalArgumentException();
            } else if (maxElements <= 0) {
                return 0;
            } else {
                ReentrantLock lock = this.lock;
                lock.lock();
                int n = 0;
                while (n < maxElements) {
                    try {
                        RunnableScheduledFuture<?> first = peekExpired();
                        if (first == null) {
                            break;
                        }
                        c.add(first);
                        finishPoll(first);
                        n++;
                    } catch (Throwable th) {
                        lock.unlock();
                    }
                }
                lock.unlock();
                return n;
            }
        }

        public Object[] toArray() {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                Object[] copyOf = Arrays.copyOf(this.queue, this.size, Object[].class);
                return copyOf;
            } finally {
                lock.unlock();
            }
        }

        public <T> T[] toArray(T[] a) {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (a.length < this.size) {
                    T[] copyOf = Arrays.copyOf(this.queue, this.size, a.getClass());
                    return copyOf;
                }
                System.arraycopy(this.queue, 0, (Object) a, 0, this.size);
                if (a.length > this.size) {
                    a[this.size] = null;
                }
                lock.unlock();
                return a;
            } finally {
                lock.unlock();
            }
        }

        public Iterator<Runnable> iterator() {
            return new Itr((RunnableScheduledFuture[]) Arrays.copyOf(this.queue, this.size));
        }
    }

    private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {
        int heapIndex;
        RunnableScheduledFuture<V> outerTask = this;
        private final long period;
        private final long sequenceNumber;
        private volatile long time;

        ScheduledFutureTask(Runnable r, V result, long triggerTime, long sequenceNumber) {
            super(r, result);
            this.time = triggerTime;
            this.period = 0;
            this.sequenceNumber = sequenceNumber;
        }

        ScheduledFutureTask(Runnable r, V result, long triggerTime, long period, long sequenceNumber) {
            super(r, result);
            this.time = triggerTime;
            this.period = period;
            this.sequenceNumber = sequenceNumber;
        }

        ScheduledFutureTask(Callable<V> callable, long triggerTime, long sequenceNumber) {
            super(callable);
            this.time = triggerTime;
            this.period = 0;
            this.sequenceNumber = sequenceNumber;
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(this.time - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        public int compareTo(Delayed other) {
            int i = -1;
            if (other == this) {
                return 0;
            }
            long diff;
            if (other instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> x = (ScheduledFutureTask) other;
                diff = this.time - x.time;
                if (diff < 0) {
                    return -1;
                }
                return (diff <= 0 && this.sequenceNumber < x.sequenceNumber) ? -1 : 1;
            } else {
                diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
                if (diff >= 0) {
                    i = diff > 0 ? 1 : 0;
                }
                return i;
            }
        }

        public boolean isPeriodic() {
            return this.period != 0;
        }

        private void setNextRunTime() {
            long p = this.period;
            if (p > 0) {
                this.time += p;
            } else {
                this.time = ScheduledThreadPoolExecutor.this.triggerTime(-p);
            }
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            if (cancelled && ScheduledThreadPoolExecutor.this.removeOnCancel && this.heapIndex >= 0) {
                ScheduledThreadPoolExecutor.this.remove(this);
            }
            return cancelled;
        }

        public void run() {
            boolean periodic = isPeriodic();
            if (!ScheduledThreadPoolExecutor.this.canRunInCurrentRunState(periodic)) {
                cancel(false);
            } else if (!periodic) {
                super.run();
            } else if (super.runAndReset()) {
                setNextRunTime();
                ScheduledThreadPoolExecutor.this.reExecutePeriodic(this.outerTask);
            }
        }
    }

    boolean canRunInCurrentRunState(boolean periodic) {
        boolean z;
        if (periodic) {
            z = this.continueExistingPeriodicTasksAfterShutdown;
        } else {
            z = this.executeExistingDelayedTasksAfterShutdown;
        }
        return isRunningOrShutdown(z);
    }

    private void delayedExecute(RunnableScheduledFuture<?> task) {
        if (isShutdown()) {
            reject(task);
            return;
        }
        super.getQueue().add(task);
        if (isShutdown() && (canRunInCurrentRunState(task.isPeriodic()) ^ 1) != 0 && remove(task)) {
            task.cancel(false);
        } else {
            ensurePrestart();
        }
    }

    void reExecutePeriodic(RunnableScheduledFuture<?> task) {
        if (canRunInCurrentRunState(true)) {
            super.getQueue().add(task);
            if (canRunInCurrentRunState(true) || !remove(task)) {
                ensurePrestart();
            } else {
                task.cancel(false);
            }
        }
    }

    void onShutdown() {
        BlockingQueue<Runnable> q = super.getQueue();
        boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy();
        boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy();
        if (keepDelayed || (keepPeriodic ^ 1) == 0) {
            for (RunnableScheduledFuture<?> e : q.toArray()) {
                if (e instanceof RunnableScheduledFuture) {
                    RunnableScheduledFuture<?> t = e;
                    if (((t.isPeriodic() ? keepPeriodic ^ 1 : keepDelayed ^ 1) != 0 || t.isCancelled()) && q.remove(t)) {
                        t.cancel(false);
                    }
                }
            }
        } else {
            for (Object e2 : q.toArray()) {
                if (e2 instanceof RunnableScheduledFuture) {
                    ((RunnableScheduledFuture) e2).cancel(false);
                }
            }
            q.clear();
        }
        tryTerminate();
    }

    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
        return task;
    }

    protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
        return task;
    }

    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue());
    }

    public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, Integer.MAX_VALUE, (long) DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue(), threadFactory);
    }

    public ScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, (long) DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue(), handler);
    }

    public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEPALIVE_MILLIS, TimeUnit.MILLISECONDS, new DelayedWorkQueue(), threadFactory, handler);
    }

    private long triggerTime(long delay, TimeUnit unit) {
        if (delay < 0) {
            delay = 0;
        }
        return triggerTime(unit.toNanos(delay));
    }

    long triggerTime(long delay) {
        long nanoTime = System.nanoTime();
        if (delay >= 4611686018427387903L) {
            delay = overflowFree(delay);
        }
        return nanoTime + delay;
    }

    private long overflowFree(long delay) {
        Delayed head = (Delayed) super.getQueue().peek();
        if (head == null) {
            return delay;
        }
        long headDelay = head.getDelay(TimeUnit.NANOSECONDS);
        if (headDelay >= 0 || delay - headDelay >= 0) {
            return delay;
        }
        return Long.MAX_VALUE + headDelay;
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        }
        RunnableScheduledFuture<Void> t = decorateTask(command, new ScheduledFutureTask(command, null, triggerTime(delay, unit), sequencer.getAndIncrement()));
        delayedExecute(t);
        return t;
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        if (callable == null || unit == null) {
            throw new NullPointerException();
        }
        RunnableScheduledFuture<V> t = decorateTask((Callable) callable, new ScheduledFutureTask(callable, triggerTime(delay, unit), sequencer.getAndIncrement()));
        delayedExecute(t);
        return t;
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        } else if (period <= 0) {
            throw new IllegalArgumentException();
        } else {
            RunnableScheduledFuture sft = new ScheduledFutureTask(command, null, triggerTime(initialDelay, unit), unit.toNanos(period), sequencer.getAndIncrement());
            RunnableScheduledFuture<Void> t = decorateTask(command, sft);
            sft.outerTask = t;
            delayedExecute(t);
            return t;
        }
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        if (command == null || unit == null) {
            throw new NullPointerException();
        } else if (delay <= 0) {
            throw new IllegalArgumentException();
        } else {
            RunnableScheduledFuture sft = new ScheduledFutureTask(command, null, triggerTime(initialDelay, unit), -unit.toNanos(delay), sequencer.getAndIncrement());
            RunnableScheduledFuture<Void> t = decorateTask(command, sft);
            sft.outerTask = t;
            delayedExecute(t);
            return t;
        }
    }

    public void execute(Runnable command) {
        schedule(command, 0, TimeUnit.NANOSECONDS);
    }

    public Future<?> submit(Runnable task) {
        return schedule(task, 0, TimeUnit.NANOSECONDS);
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return schedule(Executors.callable(task, result), 0, TimeUnit.NANOSECONDS);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return schedule((Callable) task, 0, TimeUnit.NANOSECONDS);
    }

    public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
        this.continueExistingPeriodicTasksAfterShutdown = value;
        if (!value && isShutdown()) {
            onShutdown();
        }
    }

    public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
        return this.continueExistingPeriodicTasksAfterShutdown;
    }

    public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
        this.executeExistingDelayedTasksAfterShutdown = value;
        if (!value && isShutdown()) {
            onShutdown();
        }
    }

    public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
        return this.executeExistingDelayedTasksAfterShutdown;
    }

    public void setRemoveOnCancelPolicy(boolean value) {
        this.removeOnCancel = value;
    }

    public boolean getRemoveOnCancelPolicy() {
        return this.removeOnCancel;
    }

    public void shutdown() {
        super.shutdown();
    }

    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    public BlockingQueue<Runnable> getQueue() {
        return super.getQueue();
    }
}
