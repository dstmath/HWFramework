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

    private int dowait(boolean r11, long r12) throws java.lang.InterruptedException, java.util.concurrent.BrokenBarrierException, java.util.concurrent.TimeoutException {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:java.util.concurrent.CyclicBarrier.dowait(boolean, long):int. bs: [B:17:0x0031, B:33:0x004e, B:35:0x0053]
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:86)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:57)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r10 = this;
        r8 = 0;
        r7 = 0;
        r4 = r10.lock;
        r4.lock();
        r1 = r10.generation;	 Catch:{ all -> 0x0014 }
        r6 = r1.broken;	 Catch:{ all -> 0x0014 }
        if (r6 == 0) goto L_0x0019;	 Catch:{ all -> 0x0014 }
    L_0x000e:
        r6 = new java.util.concurrent.BrokenBarrierException;	 Catch:{ all -> 0x0014 }
        r6.<init>();	 Catch:{ all -> 0x0014 }
        throw r6;	 Catch:{ all -> 0x0014 }
    L_0x0014:
        r6 = move-exception;
        r4.unlock();
        throw r6;
    L_0x0019:
        r6 = java.lang.Thread.interrupted();	 Catch:{ all -> 0x0014 }
        if (r6 == 0) goto L_0x0028;	 Catch:{ all -> 0x0014 }
    L_0x001f:
        r10.breakBarrier();	 Catch:{ all -> 0x0014 }
        r6 = new java.lang.InterruptedException;	 Catch:{ all -> 0x0014 }
        r6.<init>();	 Catch:{ all -> 0x0014 }
        throw r6;	 Catch:{ all -> 0x0014 }
    L_0x0028:
        r6 = r10.count;	 Catch:{ all -> 0x0014 }
        r3 = r6 + -1;	 Catch:{ all -> 0x0014 }
        r10.count = r3;	 Catch:{ all -> 0x0014 }
        if (r3 != 0) goto L_0x004c;
    L_0x0030:
        r5 = 0;
        r0 = r10.barrierCommand;	 Catch:{ all -> 0x0045 }
        if (r0 == 0) goto L_0x0038;	 Catch:{ all -> 0x0045 }
    L_0x0035:
        r0.run();	 Catch:{ all -> 0x0045 }
    L_0x0038:
        r5 = 1;	 Catch:{ all -> 0x0045 }
        r10.nextGeneration();	 Catch:{ all -> 0x0045 }
        if (r5 != 0) goto L_0x0041;
    L_0x003e:
        r10.breakBarrier();	 Catch:{ all -> 0x0014 }
    L_0x0041:
        r4.unlock();
        return r7;
    L_0x0045:
        r6 = move-exception;
        if (r5 != 0) goto L_0x004b;
    L_0x0048:
        r10.breakBarrier();	 Catch:{ all -> 0x0014 }
    L_0x004b:
        throw r6;	 Catch:{ all -> 0x0014 }
    L_0x004c:
        if (r11 != 0) goto L_0x005d;
    L_0x004e:
        r6 = r10.trip;	 Catch:{ InterruptedException -> 0x0068 }
        r6.await();	 Catch:{ InterruptedException -> 0x0068 }
    L_0x0053:
        r6 = r1.broken;	 Catch:{ all -> 0x0014 }
        if (r6 == 0) goto L_0x007d;	 Catch:{ all -> 0x0014 }
    L_0x0057:
        r6 = new java.util.concurrent.BrokenBarrierException;	 Catch:{ all -> 0x0014 }
        r6.<init>();	 Catch:{ all -> 0x0014 }
        throw r6;	 Catch:{ all -> 0x0014 }
    L_0x005d:
        r6 = (r12 > r8 ? 1 : (r12 == r8 ? 0 : -1));
        if (r6 <= 0) goto L_0x0053;
    L_0x0061:
        r6 = r10.trip;	 Catch:{ InterruptedException -> 0x0068 }
        r12 = r6.awaitNanos(r12);	 Catch:{ InterruptedException -> 0x0068 }
        goto L_0x0053;
    L_0x0068:
        r2 = move-exception;
        r6 = r10.generation;	 Catch:{ all -> 0x0014 }
        if (r1 != r6) goto L_0x0071;	 Catch:{ all -> 0x0014 }
    L_0x006d:
        r6 = r1.broken;	 Catch:{ all -> 0x0014 }
        if (r6 == 0) goto L_0x0079;	 Catch:{ all -> 0x0014 }
    L_0x0071:
        r6 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0014 }
        r6.interrupt();	 Catch:{ all -> 0x0014 }
        goto L_0x0053;	 Catch:{ all -> 0x0014 }
    L_0x0079:
        r10.breakBarrier();	 Catch:{ all -> 0x0014 }
        throw r2;	 Catch:{ all -> 0x0014 }
    L_0x007d:
        r6 = r10.generation;	 Catch:{ all -> 0x0014 }
        if (r1 == r6) goto L_0x0085;
    L_0x0081:
        r4.unlock();
        return r3;
    L_0x0085:
        if (r11 == 0) goto L_0x004c;
    L_0x0087:
        r6 = (r12 > r8 ? 1 : (r12 == r8 ? 0 : -1));
        if (r6 > 0) goto L_0x004c;
    L_0x008b:
        r10.breakBarrier();	 Catch:{ all -> 0x0014 }
        r6 = new java.util.concurrent.TimeoutException;	 Catch:{ all -> 0x0014 }
        r6.<init>();	 Catch:{ all -> 0x0014 }
        throw r6;	 Catch:{ all -> 0x0014 }
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.CyclicBarrier.dowait(boolean, long):int");
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
        } catch (TimeoutException toe) {
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
