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

        /* synthetic */ Generation(Generation -this0) {
            this();
        }

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
        ReentrantLock lock = this.lock;
        lock.lock();
        Generation g;
        try {
            g = this.generation;
            if (g.broken) {
                throw new BrokenBarrierException();
            } else if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            } else {
                int index = this.count - 1;
                this.count = index;
                if (index == 0) {
                    Runnable command = this.barrierCommand;
                    if (command != null) {
                        command.run();
                    }
                    nextGeneration();
                    if (!true) {
                        breakBarrier();
                    }
                    lock.unlock();
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
                        lock.unlock();
                        return index;
                    } else if (timed && nanos <= 0) {
                        breakBarrier();
                        throw new TimeoutException();
                    }
                }
            }
        } catch (InterruptedException ie) {
            if (g != this.generation || (g.broken ^ 1) == 0) {
                Thread.currentThread().interrupt();
            } else {
                breakBarrier();
                throw ie;
            }
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public CyclicBarrier(int parties, Runnable barrierAction) {
        this.lock = new ReentrantLock();
        this.trip = this.lock.newCondition();
        this.generation = new Generation();
        if (parties <= 0) {
            throw new IllegalArgumentException();
        }
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    public CyclicBarrier(int parties) {
        this(parties, null);
    }

    public int getParties() {
        return this.parties;
    }

    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0);
        } catch (Throwable toe) {
            throw new Error(toe);
        }
    }

    public int await(long timeout, TimeUnit unit) throws InterruptedException, BrokenBarrierException, TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    public boolean isBroken() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean z = this.generation.broken;
            return z;
        } finally {
            lock.unlock();
        }
    }

    public void reset() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();
            nextGeneration();
        } finally {
            lock.unlock();
        }
    }

    public int getNumberWaiting() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.parties - this.count;
            return i;
        } finally {
            lock.unlock();
        }
    }
}
