package java.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
    private final Runnable barrierCommand;
    private int count;
    private Generation generation;
    private final ReentrantLock lock;
    private final int parties;
    private final Condition trip;

    private static class Generation {
        boolean broken;

        private Generation() {
        }
    }

    private void nextGeneration() {
        this.trip.signalAll();
        this.count = this.parties;
        this.generation = new Generation();
    }

    private void breakBarrier() {
        this.generation.broken = true;
        this.count = this.parties;
        this.trip.signalAll();
    }

    private int dowait(boolean timed, long nanos) throws InterruptedException, BrokenBarrierException, TimeoutException {
        Generation g;
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            g = this.generation;
            if (g.broken) {
                throw new BrokenBarrierException();
            } else if (!Thread.interrupted()) {
                int index = this.count - 1;
                this.count = index;
                if (index == 0) {
                    Runnable command = this.barrierCommand;
                    if (command != null) {
                        command.run();
                    }
                    nextGeneration();
                    if (1 == 0) {
                        breakBarrier();
                    }
                    lock2.unlock();
                    return 0;
                }
                while (true) {
                    if (!timed) {
                        this.trip.await();
                    } else if (nanos > 0) {
                        nanos = this.trip.awaitNanos(nanos);
                    }
                    if (g.broken) {
                        throw new BrokenBarrierException();
                    } else if (g != this.generation) {
                        lock2.unlock();
                        return index;
                    } else if (timed && nanos <= 0) {
                        breakBarrier();
                        throw new TimeoutException();
                    }
                }
            } else {
                breakBarrier();
                throw new InterruptedException();
            }
        } catch (InterruptedException ie) {
            if (g == this.generation) {
                if (!g.broken) {
                    breakBarrier();
                    throw ie;
                }
            }
            Thread.currentThread().interrupt();
        } catch (Throwable th) {
            lock2.unlock();
            throw th;
        }
    }

    public CyclicBarrier(int parties2, Runnable barrierAction) {
        this.lock = new ReentrantLock();
        this.trip = this.lock.newCondition();
        this.generation = new Generation();
        if (parties2 > 0) {
            this.parties = parties2;
            this.count = parties2;
            this.barrierCommand = barrierAction;
            return;
        }
        throw new IllegalArgumentException();
    }

    public CyclicBarrier(int parties2) {
        this(parties2, null);
    }

    public int getParties() {
        return this.parties;
    }

    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0);
        } catch (TimeoutException toe) {
            throw new Error((Throwable) toe);
        }
    }

    public int await(long timeout, TimeUnit unit) throws InterruptedException, BrokenBarrierException, TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    public boolean isBroken() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.generation.broken;
        } finally {
            lock2.unlock();
        }
    }

    public void reset() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            breakBarrier();
            nextGeneration();
        } finally {
            lock2.unlock();
        }
    }

    public int getNumberWaiting() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.parties - this.count;
        } finally {
            lock2.unlock();
        }
    }
}
