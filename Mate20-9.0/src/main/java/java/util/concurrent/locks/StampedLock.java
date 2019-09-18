package java.util.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import sun.misc.Unsafe;

public class StampedLock implements Serializable {
    private static final long ABITS = 255;
    private static final int CANCELLED = 1;
    private static final int HEAD_SPINS = (NCPU > 1 ? 1024 : 0);
    private static final long INTERRUPTED = 1;
    private static final int LG_READERS = 7;
    private static final int MAX_HEAD_SPINS;
    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final long ORIGIN = 256;
    private static final int OVERFLOW_YIELD_RATE = 7;
    private static final long PARKBLOCKER;
    private static final long RBITS = 127;
    private static final long RFULL = 126;
    private static final int RMODE = 0;
    private static final long RUNIT = 1;
    private static final long SBITS = -128;
    private static final int SPINS = (NCPU > 1 ? 64 : 0);
    private static final long STATE;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final int WAITING = -1;
    private static final long WBIT = 128;
    private static final long WCOWAIT;
    private static final long WHEAD;
    private static final int WMODE = 1;
    private static final long WNEXT;
    private static final long WSTATUS;
    private static final long WTAIL;
    private static final long serialVersionUID = -6001602636862214147L;
    transient ReadLockView readLockView;
    transient ReadWriteLockView readWriteLockView;
    private transient int readerOverflow;
    private volatile transient long state = ORIGIN;
    private volatile transient WNode whead;
    transient WriteLockView writeLockView;
    private volatile transient WNode wtail;

    final class ReadLockView implements Lock {
        ReadLockView() {
        }

        public void lock() {
            StampedLock.this.readLock();
        }

        public void lockInterruptibly() throws InterruptedException {
            StampedLock.this.readLockInterruptibly();
        }

        public boolean tryLock() {
            return StampedLock.this.tryReadLock() != 0;
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return StampedLock.this.tryReadLock(time, unit) != 0;
        }

        public void unlock() {
            StampedLock.this.unstampedUnlockRead();
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    final class ReadWriteLockView implements ReadWriteLock {
        ReadWriteLockView() {
        }

        public Lock readLock() {
            return StampedLock.this.asReadLock();
        }

        public Lock writeLock() {
            return StampedLock.this.asWriteLock();
        }
    }

    static final class WNode {
        volatile WNode cowait;
        final int mode;
        volatile WNode next;
        volatile WNode prev;
        volatile int status;
        volatile Thread thread;

        WNode(int m, WNode p) {
            this.mode = m;
            this.prev = p;
        }
    }

    final class WriteLockView implements Lock {
        WriteLockView() {
        }

        public void lock() {
            StampedLock.this.writeLock();
        }

        public void lockInterruptibly() throws InterruptedException {
            StampedLock.this.writeLockInterruptibly();
        }

        public boolean tryLock() {
            return StampedLock.this.tryWriteLock() != 0;
        }

        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return StampedLock.this.tryWriteLock(time, unit) != 0;
        }

        public void unlock() {
            StampedLock.this.unstampedUnlockWrite();
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }

    static {
        int i = 0;
        if (NCPU > 1) {
            i = 65536;
        }
        MAX_HEAD_SPINS = i;
        try {
            STATE = U.objectFieldOffset(StampedLock.class.getDeclaredField("state"));
            WHEAD = U.objectFieldOffset(StampedLock.class.getDeclaredField("whead"));
            WTAIL = U.objectFieldOffset(StampedLock.class.getDeclaredField("wtail"));
            WSTATUS = U.objectFieldOffset(WNode.class.getDeclaredField("status"));
            WNEXT = U.objectFieldOffset(WNode.class.getDeclaredField("next"));
            WCOWAIT = U.objectFieldOffset(WNode.class.getDeclaredField("cowait"));
            PARKBLOCKER = U.objectFieldOffset(Thread.class.getDeclaredField("parkBlocker"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    public long writeLock() {
        long j = this.state;
        long s = j;
        if ((j & ABITS) == 0) {
            Unsafe unsafe = U;
            long j2 = STATE;
            long j3 = s + WBIT;
            long j4 = j3;
            if (unsafe.compareAndSwapLong(this, j2, s, j3)) {
                return j4;
            }
        }
        return acquireWrite(false, 0);
    }

    public long tryWriteLock() {
        long j = this.state;
        long s = j;
        if ((j & ABITS) == 0) {
            Unsafe unsafe = U;
            long j2 = STATE;
            long j3 = s + WBIT;
            long j4 = j3;
            if (unsafe.compareAndSwapLong(this, j2, s, j3)) {
                return j4;
            }
        }
        return 0;
    }

    public long tryWriteLock(long time, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long tryWriteLock = tryWriteLock();
            long next = tryWriteLock;
            if (tryWriteLock != 0) {
                return next;
            }
            if (nanos <= 0) {
                return 0;
            }
            long nanoTime = System.nanoTime() + nanos;
            long deadline = nanoTime;
            if (nanoTime == 0) {
                deadline = 1;
            }
            long acquireWrite = acquireWrite(true, deadline);
            long next2 = acquireWrite;
            if (acquireWrite != 1) {
                return next2;
            }
        }
        throw new InterruptedException();
    }

    public long writeLockInterruptibly() throws InterruptedException {
        if (!Thread.interrupted()) {
            long acquireWrite = acquireWrite(true, 0);
            long next = acquireWrite;
            if (acquireWrite != 1) {
                return next;
            }
        }
        throw new InterruptedException();
    }

    public long readLock() {
        long s = this.state;
        if (this.whead == this.wtail && (ABITS & s) < RFULL) {
            long j = s + 1;
            long j2 = j;
            if (U.compareAndSwapLong(this, STATE, s, j)) {
                return j2;
            }
        }
        return acquireRead(false, 0);
    }

    public long tryReadLock() {
        while (true) {
            long j = this.state;
            long s = j;
            long j2 = j & ABITS;
            long m = j2;
            if (j2 == WBIT) {
                return 0;
            }
            if (m < RFULL) {
                long j3 = s + 1;
                long next = j3;
                if (U.compareAndSwapLong(this, STATE, s, j3)) {
                    return next;
                }
            } else {
                long tryIncReaderOverflow = tryIncReaderOverflow(s);
                long next2 = tryIncReaderOverflow;
                if (tryIncReaderOverflow != 0) {
                    return next2;
                }
            }
        }
    }

    public long tryReadLock(long time, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(time);
        if (!Thread.interrupted()) {
            long j = this.state;
            long s = j;
            long j2 = j & ABITS;
            long m = j2;
            if (j2 != WBIT) {
                if (m < RFULL) {
                    long j3 = s + 1;
                    long next = j3;
                    if (U.compareAndSwapLong(this, STATE, s, j3)) {
                        return next;
                    }
                } else {
                    long tryIncReaderOverflow = tryIncReaderOverflow(s);
                    long next2 = tryIncReaderOverflow;
                    if (tryIncReaderOverflow != 0) {
                        return next2;
                    }
                }
            }
            if (nanos <= 0) {
                return 0;
            }
            long nanoTime = System.nanoTime() + nanos;
            long deadline = nanoTime;
            if (nanoTime == 0) {
                deadline = 1;
            }
            long acquireRead = acquireRead(true, deadline);
            long next3 = acquireRead;
            if (acquireRead != 1) {
                return next3;
            }
        }
        throw new InterruptedException();
    }

    public long readLockInterruptibly() throws InterruptedException {
        if (!Thread.interrupted()) {
            long acquireRead = acquireRead(true, 0);
            long next = acquireRead;
            if (acquireRead != 1) {
                return next;
            }
        }
        throw new InterruptedException();
    }

    public long tryOptimisticRead() {
        long j = this.state;
        long s = j;
        if ((j & WBIT) == 0) {
            return s & SBITS;
        }
        return 0;
    }

    public boolean validate(long stamp) {
        U.loadFence();
        return (stamp & SBITS) == (SBITS & this.state);
    }

    public void unlockWrite(long stamp) {
        if (this.state != stamp || (stamp & WBIT) == 0) {
            throw new IllegalMonitorStateException();
        }
        Unsafe unsafe = U;
        long j = STATE;
        long stamp2 = WBIT + stamp;
        unsafe.putLongVolatile(this, j, stamp2 == 0 ? 256 : stamp2);
        WNode wNode = this.whead;
        WNode h = wNode;
        if (wNode != null && h.status != 0) {
            release(h);
        }
    }

    public void unlockRead(long stamp) {
        while (true) {
            long j = this.state;
            long s = j;
            if ((j & SBITS) != (stamp & SBITS) || (stamp & ABITS) == 0) {
                break;
            }
            long j2 = ABITS & s;
            long m = j2;
            if (j2 == 0 || m == WBIT) {
                break;
            } else if (m < RFULL) {
                if (U.compareAndSwapLong(this, STATE, s, s - 1)) {
                    if (m == 1) {
                        WNode wNode = this.whead;
                        WNode h = wNode;
                        if (wNode != null && h.status != 0) {
                            release(h);
                            return;
                        }
                        return;
                    }
                    return;
                }
            } else if (tryDecReaderOverflow(s) != 0) {
                return;
            }
        }
        throw new IllegalMonitorStateException();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0091, code lost:
        r9 = r6;
     */
    public void unlock(long stamp) {
        long j = ABITS;
        long a = stamp & ABITS;
        while (true) {
            long a2 = a;
            long a3 = this.state;
            long s = a3;
            if ((a3 & SBITS) != (stamp & SBITS)) {
                break;
            }
            long j2 = s & j;
            long m = j2;
            if (j2 != 0) {
                if (m != WBIT) {
                    if (a2 != 0) {
                        if (a2 >= WBIT) {
                            long j3 = s;
                            break;
                        }
                        if (m < RFULL) {
                            long j4 = s;
                            if (U.compareAndSwapLong(this, STATE, s, s - 1)) {
                                if (m == 1) {
                                    WNode wNode = this.whead;
                                    WNode h = wNode;
                                    if (!(wNode == null || h.status == 0)) {
                                        release(h);
                                    }
                                }
                                return;
                            }
                        } else if (tryDecReaderOverflow(s) != 0) {
                            return;
                        }
                        a = a2;
                        j = ABITS;
                    } else {
                        break;
                    }
                } else if (a2 == m) {
                    Unsafe unsafe = U;
                    long j5 = STATE;
                    long s2 = WBIT + s;
                    unsafe.putLongVolatile(this, j5, s2 == 0 ? 256 : s2);
                    WNode wNode2 = this.whead;
                    WNode h2 = wNode2;
                    if (!(wNode2 == null || h2.status == 0)) {
                        release(h2);
                    }
                    return;
                } else {
                    long j6 = s;
                }
            } else {
                break;
            }
        }
        throw new IllegalMonitorStateException();
    }

    public long tryConvertToWriteLock(long stamp) {
        long a = stamp & ABITS;
        while (true) {
            long j = this.state;
            long s = j;
            if ((j & SBITS) != (stamp & SBITS)) {
                break;
            }
            long j2 = s & ABITS;
            long m = j2;
            if (j2 != 0) {
                if (m != WBIT) {
                    if (m != 1 || a == 0) {
                        break;
                    }
                    Unsafe unsafe = U;
                    long j3 = STATE;
                    long j4 = (s - 1) + WBIT;
                    long next = j4;
                    if (unsafe.compareAndSwapLong(this, j3, s, j4)) {
                        return next;
                    }
                } else if (a != m) {
                    return 0;
                } else {
                    return stamp;
                }
            } else if (a != 0) {
                break;
            } else {
                Unsafe unsafe2 = U;
                long j5 = STATE;
                long j6 = s + WBIT;
                long next2 = j6;
                if (unsafe2.compareAndSwapLong(this, j5, s, j6)) {
                    return next2;
                }
            }
        }
        return 0;
    }

    public long tryConvertToReadLock(long stamp) {
        long j = ABITS;
        long a = stamp & ABITS;
        while (true) {
            long a2 = a;
            long a3 = this.state;
            long s = a3;
            if ((a3 & SBITS) != (stamp & SBITS)) {
                break;
            }
            long j2 = s & j;
            long m = j2;
            if (j2 != 0) {
                long s2 = s;
                if (m == WBIT) {
                    if (a2 == m) {
                        long j3 = 129 + s2;
                        long next = j3;
                        U.putLongVolatile(this, STATE, j3);
                        WNode wNode = this.whead;
                        WNode h = wNode;
                        if (!(wNode == null || h.status == 0)) {
                            release(h);
                        }
                        return next;
                    }
                } else if (a2 == 0 || a2 >= WBIT) {
                    return 0;
                } else {
                    return stamp;
                }
            } else if (a2 != 0) {
                long j4 = s;
                break;
            } else {
                if (m < RFULL) {
                    long s3 = s + 1;
                    long next2 = s3;
                    long j5 = s;
                    if (U.compareAndSwapLong(this, STATE, s, s3)) {
                        return next2;
                    }
                } else {
                    long tryIncReaderOverflow = tryIncReaderOverflow(s);
                    long next3 = tryIncReaderOverflow;
                    if (tryIncReaderOverflow != 0) {
                        return next3;
                    }
                }
                a = a2;
                j = ABITS;
            }
        }
        return 0;
    }

    public long tryConvertToOptimisticRead(long stamp) {
        long s;
        long s2 = ABITS;
        long a = stamp & ABITS;
        U.loadFence();
        while (true) {
            long j = this.state;
            s = j;
            if ((j & SBITS) == (stamp & SBITS)) {
                long j2 = s & s2;
                long m = j2;
                if (j2 != 0) {
                    if (m != WBIT) {
                        if (a == 0) {
                            break;
                        } else if (a >= WBIT) {
                            break;
                        } else {
                            if (m < RFULL) {
                                long s3 = s - 1;
                                long next = s3;
                                long j3 = s;
                                if (U.compareAndSwapLong(this, STATE, s, s3)) {
                                    if (m == 1) {
                                        WNode wNode = this.whead;
                                        WNode h = wNode;
                                        if (!(wNode == null || h.status == 0)) {
                                            release(h);
                                        }
                                    }
                                    return next & SBITS;
                                }
                            } else {
                                long tryDecReaderOverflow = tryDecReaderOverflow(s);
                                long next2 = tryDecReaderOverflow;
                                if (tryDecReaderOverflow != 0) {
                                    return next2 & SBITS;
                                }
                            }
                            s2 = ABITS;
                        }
                    } else if (a == m) {
                        Unsafe unsafe = U;
                        long j4 = STATE;
                        long s4 = WBIT + s;
                        long j5 = s4 == 0 ? 256 : s4;
                        long next3 = j5;
                        unsafe.putLongVolatile(this, j4, j5);
                        WNode wNode2 = this.whead;
                        WNode h2 = wNode2;
                        if (!(wNode2 == null || h2.status == 0)) {
                            release(h2);
                        }
                        return next3;
                    }
                } else if (a == 0) {
                    return s;
                }
            } else {
                break;
            }
        }
        return 0;
    }

    public boolean tryUnlockWrite() {
        long j = this.state;
        long s = j;
        if ((j & WBIT) == 0) {
            return false;
        }
        Unsafe unsafe = U;
        long j2 = STATE;
        long s2 = WBIT + s;
        unsafe.putLongVolatile(this, j2, s2 == 0 ? 256 : s2);
        WNode wNode = this.whead;
        WNode h = wNode;
        if (!(wNode == null || h.status == 0)) {
            release(h);
        }
        return true;
    }

    public boolean tryUnlockRead() {
        while (true) {
            long j = this.state;
            long s = j;
            long j2 = j & ABITS;
            long m = j2;
            if (j2 != 0 && m < WBIT) {
                if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, s - 1)) {
                        if (m == 1) {
                            WNode wNode = this.whead;
                            WNode h = wNode;
                            if (!(wNode == null || h.status == 0)) {
                                release(h);
                            }
                        }
                        return true;
                    }
                } else if (tryDecReaderOverflow(s) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getReadLockCount(long s) {
        long j = RBITS & s;
        long readers = j;
        if (j >= RFULL) {
            readers = RFULL + ((long) this.readerOverflow);
        }
        return (int) readers;
    }

    public boolean isWriteLocked() {
        return (this.state & WBIT) != 0;
    }

    public boolean isReadLocked() {
        return (this.state & RBITS) != 0;
    }

    public int getReadLockCount() {
        return getReadLockCount(this.state);
    }

    public String toString() {
        String str;
        long s = this.state;
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if ((ABITS & s) == 0) {
            str = "[Unlocked]";
        } else if ((WBIT & s) != 0) {
            str = "[Write-locked]";
        } else {
            str = "[Read-locks:" + getReadLockCount(s) + "]";
        }
        sb.append(str);
        return sb.toString();
    }

    public Lock asReadLock() {
        ReadLockView readLockView2 = this.readLockView;
        ReadLockView v = readLockView2;
        if (readLockView2 != null) {
            return v;
        }
        ReadLockView readLockView3 = new ReadLockView();
        this.readLockView = readLockView3;
        return readLockView3;
    }

    public Lock asWriteLock() {
        WriteLockView writeLockView2 = this.writeLockView;
        WriteLockView v = writeLockView2;
        if (writeLockView2 != null) {
            return v;
        }
        WriteLockView writeLockView3 = new WriteLockView();
        this.writeLockView = writeLockView3;
        return writeLockView3;
    }

    public ReadWriteLock asReadWriteLock() {
        ReadWriteLockView readWriteLockView2 = this.readWriteLockView;
        ReadWriteLockView v = readWriteLockView2;
        if (readWriteLockView2 != null) {
            return v;
        }
        ReadWriteLockView readWriteLockView3 = new ReadWriteLockView();
        this.readWriteLockView = readWriteLockView3;
        return readWriteLockView3;
    }

    /* access modifiers changed from: package-private */
    public final void unstampedUnlockWrite() {
        long j = this.state;
        long s = j;
        if ((j & WBIT) != 0) {
            Unsafe unsafe = U;
            long j2 = STATE;
            long s2 = WBIT + s;
            unsafe.putLongVolatile(this, j2, s2 == 0 ? 256 : s2);
            WNode wNode = this.whead;
            WNode h = wNode;
            if (wNode != null && h.status != 0) {
                release(h);
                return;
            }
            return;
        }
        throw new IllegalMonitorStateException();
    }

    /* access modifiers changed from: package-private */
    public final void unstampedUnlockRead() {
        while (true) {
            long j = this.state;
            long s = j;
            long j2 = j & ABITS;
            long m = j2;
            if (j2 != 0 && m < WBIT) {
                if (m < RFULL) {
                    if (U.compareAndSwapLong(this, STATE, s, s - 1)) {
                        if (m == 1) {
                            WNode wNode = this.whead;
                            WNode h = wNode;
                            if (wNode != null && h.status != 0) {
                                release(h);
                                return;
                            }
                            return;
                        }
                        return;
                    }
                } else if (tryDecReaderOverflow(s) != 0) {
                    return;
                }
            }
        }
        throw new IllegalMonitorStateException();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        U.putLongVolatile(this, STATE, ORIGIN);
    }

    private long tryIncReaderOverflow(long s) {
        if ((ABITS & s) == RFULL) {
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                this.readerOverflow++;
                U.putLongVolatile(this, STATE, s);
                return s;
            }
        } else if ((LockSupport.nextSecondarySeed() & 7) == 0) {
            Thread.yield();
        }
        return 0;
    }

    private long tryDecReaderOverflow(long s) {
        long next;
        if ((ABITS & s) == RFULL) {
            if (U.compareAndSwapLong(this, STATE, s, s | RBITS)) {
                int i = this.readerOverflow;
                int r = i;
                if (i > 0) {
                    this.readerOverflow = r - 1;
                    next = s;
                } else {
                    next = s - 1;
                }
                U.putLongVolatile(this, STATE, next);
                return next;
            }
        } else if ((LockSupport.nextSecondarySeed() & 7) == 0) {
            Thread.yield();
        }
        return 0;
    }

    private void release(WNode h) {
        if (h != null) {
            U.compareAndSwapInt(h, WSTATUS, -1, 0);
            WNode wNode = h.next;
            WNode q = wNode;
            if (wNode == null || q.status == 1) {
                WNode t = this.wtail;
                while (t != null && t != h) {
                    if (t.status <= 0) {
                        q = t;
                    }
                    t = t.prev;
                }
            }
            if (q != null) {
                Thread thread = q.thread;
                Thread w = thread;
                if (thread != null) {
                    U.unpark(w);
                }
            }
        }
    }

    private long acquireWrite(boolean interruptible, long deadline) {
        WNode wNode;
        WNode node;
        boolean z;
        long time;
        WNode node2 = null;
        int spins = -1;
        while (true) {
            int spins2 = spins;
            long j = this.state;
            long s = j;
            long j2 = j & ABITS;
            long m = j2;
            if (j2 == 0) {
                Unsafe unsafe = U;
                long j3 = STATE;
                long j4 = s + WBIT;
                long ns = j4;
                if (unsafe.compareAndSwapLong(this, j3, s, j4)) {
                    return ns;
                }
            } else {
                int i = 0;
                if (spins2 < 0) {
                    if (m == WBIT && this.wtail == this.whead) {
                        i = SPINS;
                    }
                    spins = i;
                } else {
                    if (spins2 <= 0) {
                        WNode wNode2 = this.wtail;
                        WNode p = wNode2;
                        if (wNode2 == null) {
                            WNode hd = new WNode(1, null);
                            if (U.compareAndSwapObject(this, WHEAD, null, hd)) {
                                this.wtail = hd;
                            }
                        } else if (node2 == null) {
                            node2 = new WNode(1, p);
                        } else if (node2.prev != p) {
                            node2.prev = p;
                        } else {
                            WNode wNode3 = null;
                            if (U.compareAndSwapObject(this, WTAIL, p, node2)) {
                                p.next = node2;
                                boolean wasInterrupted = false;
                                WNode p2 = p;
                                int spins3 = -1;
                                while (true) {
                                    int spins4 = spins3;
                                    WNode wNode4 = this.whead;
                                    WNode h = wNode4;
                                    if (wNode4 == p2) {
                                        if (spins4 < 0) {
                                            spins4 = HEAD_SPINS;
                                        } else if (spins4 < MAX_HEAD_SPINS) {
                                            spins4 <<= 1;
                                        }
                                        int spins5 = spins4;
                                        int k = spins5;
                                        while (true) {
                                            int k2 = k;
                                            long j5 = this.state;
                                            long s2 = j5;
                                            if ((j5 & ABITS) == 0) {
                                                Unsafe unsafe2 = U;
                                                long j6 = STATE;
                                                long j7 = s2 + WBIT;
                                                long ns2 = j7;
                                                if (unsafe2.compareAndSwapLong(this, j6, s2, j7)) {
                                                    this.whead = node2;
                                                    node2.prev = wNode3;
                                                    if (wasInterrupted) {
                                                        Thread.currentThread().interrupt();
                                                    }
                                                    return ns2;
                                                }
                                            } else if (LockSupport.nextSecondarySeed() >= 0) {
                                                k2--;
                                                if (k2 <= 0) {
                                                    spins3 = spins5;
                                                    break;
                                                }
                                            } else {
                                                continue;
                                            }
                                            k = k2;
                                        }
                                    } else {
                                        if (h != null) {
                                            while (true) {
                                                WNode wNode5 = h.cowait;
                                                WNode c = wNode5;
                                                if (wNode5 == null) {
                                                    break;
                                                }
                                                if (U.compareAndSwapObject(h, WCOWAIT, c, c.cowait)) {
                                                    Thread thread = c.thread;
                                                    Thread w = thread;
                                                    if (thread != null) {
                                                        U.unpark(w);
                                                    }
                                                }
                                            }
                                        }
                                        spins3 = spins4;
                                    }
                                    if (this.whead == h) {
                                        WNode wNode6 = node2.prev;
                                        WNode np = wNode6;
                                        if (wNode6 == p2) {
                                            int i2 = p2.status;
                                            int ps = i2;
                                            if (i2 == 0) {
                                                U.compareAndSwapInt(p2, WSTATUS, 0, -1);
                                            } else if (ps == 1) {
                                                WNode wNode7 = p2.prev;
                                                WNode pp = wNode7;
                                                if (wNode7 != null) {
                                                    node2.prev = pp;
                                                    pp.next = node2;
                                                }
                                            } else {
                                                if (deadline == 0) {
                                                    time = 0;
                                                } else {
                                                    long nanoTime = deadline - System.nanoTime();
                                                    time = nanoTime;
                                                    if (nanoTime <= 0) {
                                                        return cancelWaiter(node2, node2, false);
                                                    }
                                                }
                                                z = false;
                                                Thread wt = Thread.currentThread();
                                                U.putObject(wt, PARKBLOCKER, this);
                                                node = node2;
                                                node.thread = wt;
                                                if (p2.status < 0 && (!(p2 == h && (this.state & ABITS) == 0) && this.whead == h && node.prev == p2)) {
                                                    U.park(false, time);
                                                }
                                                wNode = null;
                                                node.thread = null;
                                                long j8 = time;
                                                U.putObject(wt, PARKBLOCKER, null);
                                                if (Thread.interrupted()) {
                                                    if (interruptible) {
                                                        return cancelWaiter(node, node, true);
                                                    }
                                                    wasInterrupted = true;
                                                }
                                            }
                                        } else if (np != null) {
                                            np.next = node2;
                                            p2 = np;
                                        }
                                        wNode = wNode3;
                                        node = node2;
                                        z = false;
                                    } else {
                                        wNode = wNode3;
                                        node = node2;
                                        z = false;
                                    }
                                    boolean z2 = z;
                                    node2 = node;
                                    wNode3 = wNode;
                                }
                            }
                        }
                    } else if (LockSupport.nextSecondarySeed() >= 0) {
                        spins2--;
                    }
                    spins = spins2;
                }
            }
            node2 = node2;
            spins = spins2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0148, code lost:
        if (r14 == false) goto L_0x0151;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x014a, code lost:
        java.lang.Thread.currentThread().interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0151, code lost:
        return r26;
     */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x01de A[LOOP:3: B:67:0x010c->B:121:0x01de, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0067  */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x0158 A[EDGE_INSN: B:245:0x0158->B:83:0x0158 ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:253:0x029a A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:256:0x029e A[SYNTHETIC] */
    private long acquireRead(boolean interruptible, long deadline) {
        int spins;
        WNode h;
        WNode p;
        boolean wasInterrupted;
        WNode wNode;
        WNode wNode2;
        boolean z;
        boolean z2;
        long time;
        long ns;
        long s;
        Thread thread;
        long ns2;
        Thread thread2;
        boolean z3;
        boolean z4;
        long time2;
        long ns3;
        boolean h2 = false;
        WNode node = null;
        int spins2 = -1;
        loop0:
        while (true) {
            WNode node2 = this.whead;
            WNode h3 = node2;
            WNode wNode3 = this.wtail;
            WNode p2 = wNode3;
            long j = 1;
            if (node2 == wNode3) {
                spins = spins2;
                WNode h4 = h3;
                WNode p3 = p2;
                while (true) {
                    long j2 = this.state;
                    long s2 = j2;
                    long j3 = j2 & ABITS;
                    long m = j3;
                    if (j3 < RFULL) {
                        long j4 = s2 + j;
                        ns3 = j4;
                        long s3 = s2;
                        p = p3;
                        h = h4;
                        if (U.compareAndSwapLong(this, STATE, s2, j4)) {
                            long j5 = s3;
                            break loop0;
                        }
                        long j6 = s3;
                        if (m >= WBIT) {
                            if (spins <= 0) {
                                if (spins == 0) {
                                    WNode nh = this.whead;
                                    WNode np = this.wtail;
                                    if (nh == h && np == p) {
                                        break;
                                    }
                                    WNode h5 = nh;
                                    WNode p4 = np;
                                    if (nh != np) {
                                        h = h5;
                                        p = p4;
                                        break;
                                    }
                                    h4 = h5;
                                    p3 = p4;
                                } else {
                                    p3 = p;
                                    h4 = h;
                                }
                                spins = SPINS;
                                j = 1;
                            } else if (LockSupport.nextSecondarySeed() >= 0) {
                                spins--;
                            }
                        }
                        p3 = p;
                        h4 = h;
                        j = 1;
                    } else {
                        long s4 = s2;
                        p = p3;
                        h = h4;
                        if (m < WBIT) {
                            long tryIncReaderOverflow = tryIncReaderOverflow(s4);
                            ns3 = tryIncReaderOverflow;
                            if (tryIncReaderOverflow != 0) {
                                break loop0;
                            }
                        }
                        if (m >= WBIT) {
                        }
                        p3 = p;
                        h4 = h;
                        j = 1;
                    }
                }
            } else {
                spins = spins2;
                h = h3;
                p = p2;
            }
            Thread thread3 = null;
            if (p == null) {
                WNode hd = new WNode(1, null);
                if (U.compareAndSwapObject(this, WHEAD, null, hd)) {
                    this.wtail = hd;
                }
            } else {
                if (node == null) {
                    node = new WNode(0, p);
                } else if (h == p || p.mode != 0) {
                    WNode wNode4 = null;
                    if (node.prev != p) {
                        node.prev = p;
                    } else {
                        wasInterrupted = h2;
                        if (U.compareAndSwapObject(this, WTAIL, p, node)) {
                            p.next = node;
                            int spins3 = -1;
                            loop4:
                            while (true) {
                                int spins4 = spins3;
                                WNode wNode5 = this.whead;
                                WNode h6 = wNode5;
                                if (wNode5 == p) {
                                    if (spins4 < 0) {
                                        spins4 = HEAD_SPINS;
                                    } else if (spins4 < MAX_HEAD_SPINS) {
                                        spins4 <<= 1;
                                    }
                                    int spins5 = spins4;
                                    int k = spins5;
                                    while (true) {
                                        int k2 = k;
                                        long j7 = this.state;
                                        long s5 = j7;
                                        long j8 = j7 & ABITS;
                                        long m2 = j8;
                                        if (j8 < RFULL) {
                                            long j9 = s5 + 1;
                                            ns = j9;
                                            long s6 = s5;
                                            wNode = wNode4;
                                            if (U.compareAndSwapLong(this, STATE, s5, j9)) {
                                                s = s6;
                                                break loop4;
                                            }
                                            long j10 = s6;
                                            if (m2 >= WBIT && LockSupport.nextSecondarySeed() >= 0) {
                                                k2--;
                                                if (k2 > 0) {
                                                    spins3 = spins5;
                                                    break;
                                                }
                                            }
                                            k = k2;
                                            wNode4 = wNode;
                                        } else {
                                            long s7 = s5;
                                            wNode = wNode4;
                                            if (m2 < WBIT) {
                                                s = s7;
                                                long tryIncReaderOverflow2 = tryIncReaderOverflow(s);
                                                ns = tryIncReaderOverflow2;
                                                if (tryIncReaderOverflow2 != 0) {
                                                    break loop4;
                                                }
                                            }
                                            k2--;
                                            if (k2 > 0) {
                                            }
                                        }
                                    }
                                } else {
                                    wNode = wNode4;
                                    if (h6 != null) {
                                        while (true) {
                                            WNode wNode6 = h6.cowait;
                                            WNode c = wNode6;
                                            if (wNode6 == null) {
                                                break;
                                            }
                                            if (U.compareAndSwapObject(h6, WCOWAIT, c, c.cowait)) {
                                                Thread thread4 = c.thread;
                                                Thread w = thread4;
                                                if (thread4 != null) {
                                                    U.unpark(w);
                                                }
                                            }
                                        }
                                    }
                                    spins3 = spins4;
                                }
                                if (this.whead == h6) {
                                    WNode wNode7 = node.prev;
                                    WNode np2 = wNode7;
                                    if (wNode7 == p) {
                                        int i = p.status;
                                        int ps = i;
                                        if (i == 0) {
                                            U.compareAndSwapInt(p, WSTATUS, 0, -1);
                                        } else if (ps == 1) {
                                            WNode wNode8 = p.prev;
                                            WNode pp = wNode8;
                                            if (wNode8 != null) {
                                                node.prev = pp;
                                                pp.next = node;
                                            }
                                        } else {
                                            if (deadline == 0) {
                                                time = 0;
                                            } else {
                                                long nanoTime = deadline - System.nanoTime();
                                                time = nanoTime;
                                                if (nanoTime <= 0) {
                                                    return cancelWaiter(node, node, false);
                                                }
                                            }
                                            z2 = false;
                                            Thread wt = Thread.currentThread();
                                            WNode h7 = h6;
                                            U.putObject(wt, PARKBLOCKER, this);
                                            node.thread = wt;
                                            if (p.status < 0) {
                                                WNode h8 = h7;
                                                if ((p != h8 || (this.state & ABITS) == WBIT) && this.whead == h8 && node.prev == p) {
                                                    U.park(false, time);
                                                }
                                            }
                                            wNode2 = null;
                                            node.thread = null;
                                            long j11 = time;
                                            U.putObject(wt, PARKBLOCKER, null);
                                            if (Thread.interrupted()) {
                                                if (interruptible) {
                                                    return cancelWaiter(node, node, true);
                                                }
                                                z = true;
                                                wasInterrupted = true;
                                                boolean z5 = z2;
                                                boolean z6 = z;
                                                wNode4 = wNode2;
                                            }
                                            z = true;
                                            boolean z52 = z2;
                                            boolean z62 = z;
                                            wNode4 = wNode2;
                                        }
                                    } else if (np2 != null) {
                                        np2.next = node;
                                        p = np2;
                                    }
                                }
                                wNode2 = wNode;
                                z2 = false;
                                z = true;
                                boolean z522 = z2;
                                boolean z622 = z;
                                wNode4 = wNode2;
                            }
                            this.whead = node;
                            node.prev = wNode;
                            while (true) {
                                WNode wNode9 = node.cowait;
                                WNode c2 = wNode9;
                                if (wNode9 == null) {
                                    break;
                                }
                                long s8 = s;
                                if (U.compareAndSwapObject(node, WCOWAIT, c2, c2.cowait)) {
                                    Thread thread5 = c2.thread;
                                    Thread w2 = thread5;
                                    if (thread5 != null) {
                                        U.unpark(w2);
                                    }
                                }
                                s = s8;
                            }
                            long j12 = s;
                            if (wasInterrupted) {
                                Thread.currentThread().interrupt();
                            }
                            return ns;
                        }
                        h2 = wasInterrupted;
                    }
                } else {
                    Unsafe unsafe = U;
                    long j13 = WCOWAIT;
                    WNode wNode10 = p.cowait;
                    node.cowait = wNode10;
                    if (!unsafe.compareAndSwapObject(p, j13, wNode10, node)) {
                        node.cowait = null;
                    } else {
                        boolean wasInterrupted2 = h2;
                        while (true) {
                            WNode wNode11 = this.whead;
                            WNode h9 = wNode11;
                            if (wNode11 != null) {
                                WNode wNode12 = h9.cowait;
                                WNode c3 = wNode12;
                                if (wNode12 != null) {
                                    if (U.compareAndSwapObject(h9, WCOWAIT, c3, c3.cowait)) {
                                        Thread thread6 = c3.thread;
                                        Thread w3 = thread6;
                                        if (thread6 != null) {
                                            U.unpark(w3);
                                        }
                                    }
                                }
                            }
                            WNode wNode13 = p.prev;
                            WNode pp2 = wNode13;
                            if (h9 == wNode13 || h9 == p || pp2 == null) {
                                while (true) {
                                    long j14 = this.state;
                                    long s9 = j14;
                                    long j15 = j14 & ABITS;
                                    long m3 = j15;
                                    if (j15 < RFULL) {
                                        long j16 = s9 + 1;
                                        ns2 = j16;
                                        long s10 = s9;
                                        thread = thread3;
                                        if (U.compareAndSwapLong(this, STATE, s9, j16)) {
                                            long j17 = s10;
                                            break loop0;
                                        }
                                        long j18 = s10;
                                        if (m3 < WBIT) {
                                            break;
                                        }
                                        thread3 = thread;
                                    } else {
                                        thread = thread3;
                                        long s11 = s9;
                                        if (m3 < WBIT) {
                                            long tryIncReaderOverflow3 = tryIncReaderOverflow(s11);
                                            ns2 = tryIncReaderOverflow3;
                                            if (tryIncReaderOverflow3 != 0) {
                                                break loop0;
                                            }
                                        }
                                        if (m3 < WBIT) {
                                        }
                                    }
                                }
                            } else {
                                thread = thread3;
                            }
                            if (this.whead != h9 || p.prev != pp2) {
                                thread2 = thread;
                                z3 = true;
                            } else if (pp2 == null || h9 == p || p.status > 0) {
                                node = null;
                                h2 = wasInterrupted2;
                            } else {
                                if (deadline == 0) {
                                    time2 = 0;
                                    z4 = false;
                                } else {
                                    long nanoTime2 = deadline - System.nanoTime();
                                    long time3 = nanoTime2;
                                    if (nanoTime2 <= 0) {
                                        if (wasInterrupted2) {
                                            Thread.currentThread().interrupt();
                                        }
                                        return cancelWaiter(node, p, false);
                                    }
                                    z4 = false;
                                    time2 = time3;
                                }
                                Thread wt2 = Thread.currentThread();
                                U.putObject(wt2, PARKBLOCKER, this);
                                node.thread = wt2;
                                if ((h9 != pp2 || (this.state & ABITS) == WBIT) && this.whead == h9 && p.prev == pp2) {
                                    U.park(z4, time2);
                                }
                                node.thread = thread;
                                U.putObject(wt2, PARKBLOCKER, thread);
                                if (Thread.interrupted()) {
                                    thread2 = thread;
                                    if (interruptible) {
                                        return cancelWaiter(node, p, true);
                                    }
                                    z3 = true;
                                    wasInterrupted2 = true;
                                } else {
                                    thread2 = thread;
                                    z3 = true;
                                }
                            }
                            thread3 = thread2;
                            WNode wNode14 = h9;
                            boolean z7 = z3;
                        }
                        node = null;
                        h2 = wasInterrupted2;
                    }
                }
                spins2 = spins;
            }
            wasInterrupted = h2;
            h2 = wasInterrupted;
            spins2 = spins;
        }
        if (h2) {
            Thread.currentThread().interrupt();
        }
        return ns3;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0067, code lost:
        if (r8 != null) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x006b, code lost:
        if (r11 != r10.wtail) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x006d, code lost:
        U.compareAndSwapObject(r10, WTAIL, r11, r1);
     */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0066 A[SYNTHETIC] */
    private long cancelWaiter(WNode node, WNode group, boolean interrupted) {
        WNode succ;
        if (node != null && group != null) {
            node.status = 1;
            WNode p = group;
            while (true) {
                WNode wNode = p.cowait;
                WNode q = wNode;
                if (wNode == null) {
                    break;
                } else if (q.status == 1) {
                    U.compareAndSwapObject(p, WCOWAIT, q, q.cowait);
                    p = group;
                } else {
                    p = q;
                }
            }
            if (group == node) {
                for (WNode r = group.cowait; r != null; r = r.cowait) {
                    Thread thread = r.thread;
                    Thread w = thread;
                    if (thread != null) {
                        U.unpark(w);
                    }
                }
                WNode pred = node.prev;
                while (pred != null) {
                    while (true) {
                        WNode wNode2 = node.next;
                        WNode succ2 = wNode2;
                        if (wNode2 != null && succ2.status != 1) {
                            succ = succ2;
                            break;
                        }
                        WNode t = this.wtail;
                        WNode q2 = null;
                        while (true) {
                            WNode q3 = t;
                            if (q3 == null || q3 == node) {
                                if (succ2 != q2) {
                                    succ = succ2;
                                    break;
                                }
                                WNode succ3 = q2;
                                if (U.compareAndSwapObject(node, WNEXT, succ2, q2)) {
                                    succ = succ3;
                                    break;
                                }
                            } else {
                                if (q3.status != 1) {
                                    q2 = q3;
                                }
                                t = q3.prev;
                            }
                        }
                        if (succ2 != q2) {
                        }
                    }
                    if (pred.next == node) {
                        U.compareAndSwapObject(pred, WNEXT, node, succ);
                    }
                    if (succ != null) {
                        Thread thread2 = succ.thread;
                        Thread w2 = thread2;
                        if (thread2 != null) {
                            succ.thread = null;
                            U.unpark(w2);
                        }
                    }
                    if (pred.status != 1) {
                        break;
                    }
                    WNode wNode3 = pred.prev;
                    WNode pp = wNode3;
                    if (wNode3 == null) {
                        break;
                    }
                    node.prev = pp;
                    U.compareAndSwapObject(pp, WNEXT, pred, succ);
                    pred = pp;
                }
            }
        }
        while (true) {
            WNode pred2 = this.whead;
            WNode h = pred2;
            if (pred2 == null) {
                break;
            }
            WNode wNode4 = h.next;
            WNode q4 = wNode4;
            if (wNode4 == null || q4.status == 1) {
                WNode t2 = this.wtail;
                while (t2 != null && t2 != h) {
                    if (t2.status <= 0) {
                        q4 = t2;
                    }
                    t2 = t2.prev;
                }
            }
            if (h == this.whead) {
                if (q4 != null && h.status == 0) {
                    long j = this.state;
                    long s = j;
                    if ((j & ABITS) != WBIT && (s == 0 || q4.mode == 0)) {
                        release(h);
                    }
                }
            }
        }
        if (interrupted || Thread.interrupted()) {
            return 1;
        }
        return 0;
    }
}
