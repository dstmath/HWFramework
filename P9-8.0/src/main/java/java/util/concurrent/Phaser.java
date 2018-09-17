package java.util.concurrent;

import java.util.concurrent.ForkJoinPool.ManagedBlocker;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import sun.misc.Unsafe;

public class Phaser {
    private static final long COUNTS_MASK = 4294967295L;
    private static final int EMPTY = 1;
    private static final int MAX_PARTIES = 65535;
    private static final int MAX_PHASE = Integer.MAX_VALUE;
    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final int ONE_ARRIVAL = 1;
    private static final int ONE_DEREGISTER = 65537;
    private static final int ONE_PARTY = 65536;
    private static final long PARTIES_MASK = 4294901760L;
    private static final int PARTIES_SHIFT = 16;
    private static final int PHASE_SHIFT = 32;
    static final int SPINS_PER_ARRIVAL;
    private static final long STATE;
    private static final long TERMINATION_BIT = Long.MIN_VALUE;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final int UNARRIVED_MASK = 65535;
    private final AtomicReference<QNode> evenQ;
    private final AtomicReference<QNode> oddQ;
    private final Phaser parent;
    private final Phaser root;
    private volatile long state;

    static final class QNode implements ManagedBlocker {
        final long deadline;
        final boolean interruptible;
        long nanos;
        QNode next;
        final int phase;
        final Phaser phaser;
        volatile Thread thread;
        final boolean timed;
        boolean wasInterrupted;

        QNode(Phaser phaser, int phase, boolean interruptible, boolean timed, long nanos) {
            this.phaser = phaser;
            this.phase = phase;
            this.interruptible = interruptible;
            this.nanos = nanos;
            this.timed = timed;
            this.deadline = timed ? System.nanoTime() + nanos : Phaser.STATE;
            this.thread = Thread.currentThread();
        }

        /* JADX WARNING: Missing block: B:21:0x003e, code:
            if (r0 <= java.util.concurrent.Phaser.STATE) goto L_0x0040;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean isReleasable() {
            if (this.thread == null) {
                return true;
            }
            if (this.phaser.getPhase() != this.phase) {
                this.thread = null;
                return true;
            }
            if (Thread.interrupted()) {
                this.wasInterrupted = true;
            }
            if (this.wasInterrupted && this.interruptible) {
                this.thread = null;
                return true;
            }
            if (this.timed) {
                if (this.nanos > Phaser.STATE) {
                    long nanoTime = this.deadline - System.nanoTime();
                    this.nanos = nanoTime;
                }
                this.thread = null;
                return true;
            }
            return false;
        }

        public boolean block() {
            while (!isReleasable()) {
                if (this.timed) {
                    LockSupport.parkNanos(this, this.nanos);
                } else {
                    LockSupport.park(this);
                }
            }
            return true;
        }
    }

    private static int unarrivedOf(long s) {
        int counts = (int) s;
        return counts == 1 ? 0 : 65535 & counts;
    }

    private static int partiesOf(long s) {
        return ((int) s) >>> 16;
    }

    private static int phaseOf(long s) {
        return (int) (s >>> 32);
    }

    private static int arrivedOf(long s) {
        int counts = (int) s;
        if (counts == 1) {
            return 0;
        }
        return (counts >>> 16) - (65535 & counts);
    }

    private AtomicReference<QNode> queueFor(int phase) {
        return (phase & 1) == 0 ? this.evenQ : this.oddQ;
    }

    private String badArrive(long s) {
        return "Attempted arrival of unregistered party for " + stateToString(s);
    }

    private String badRegister(long s) {
        return "Attempt to register more than 65535 parties for " + stateToString(s);
    }

    private int doArrive(int adjust) {
        long s;
        int phase;
        int unarrived;
        Phaser root = this.root;
        long s2;
        do {
            s2 = root == this ? this.state : reconcileState();
            phase = (int) (s2 >>> 32);
            if (phase < 0) {
                return phase;
            }
            int counts = (int) s2;
            unarrived = counts == 1 ? 0 : counts & 65535;
            if (unarrived <= 0) {
                throw new IllegalStateException(badArrive(s2));
            }
            s = s2 - ((long) adjust);
        } while (!U.compareAndSwapLong(this, STATE, s2, s));
        if (unarrived == 1) {
            long n = s & PARTIES_MASK;
            int nextUnarrived = ((int) n) >>> 16;
            if (root == this) {
                if (onAdvance(phase, nextUnarrived)) {
                    n |= Long.MIN_VALUE;
                } else if (nextUnarrived == 0) {
                    n |= 1;
                } else {
                    n |= (long) nextUnarrived;
                }
                U.compareAndSwapLong(this, STATE, s, n | (((long) ((phase + 1) & Integer.MAX_VALUE)) << 32));
                releaseWaiters(phase);
            } else if (nextUnarrived == 0) {
                phase = this.parent.doArrive(ONE_DEREGISTER);
                U.compareAndSwapLong(this, STATE, s, s | 1);
            } else {
                phase = this.parent.doArrive(1);
            }
        }
        return phase;
    }

    /* JADX WARNING: Missing block: B:27:0x0090, code:
            r23 = r21.doRegister(1);
     */
    /* JADX WARNING: Missing block: B:28:0x0097, code:
            if (r23 >= 0) goto L_0x009b;
     */
    /* JADX WARNING: Missing block: B:32:0x00ae, code:
            if (U.compareAndSwapLong(r25, STATE, r6, (((long) r23) << 32) | r18) != false) goto L_0x0099;
     */
    /* JADX WARNING: Missing block: B:33:0x00b0, code:
            r6 = r25.state;
     */
    /* JADX WARNING: Missing block: B:34:0x00ba, code:
            r23 = (int) (r25.root.state >>> 32);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int doRegister(int registrations) {
        int phase;
        long adjust = (((long) registrations) << 16) | ((long) registrations);
        Phaser parent = this.parent;
        while (true) {
            long s = parent == null ? this.state : reconcileState();
            int counts = (int) s;
            int unarrived = counts & 65535;
            if (registrations <= 65535 - (counts >>> 16)) {
                phase = (int) (s >>> 32);
                if (phase >= 0) {
                    if (counts == 1) {
                        if (parent != null) {
                            synchronized (this) {
                                if (this.state == s) {
                                    break;
                                }
                            }
                        }
                        if (U.compareAndSwapLong(this, STATE, s, (((long) phase) << 32) | adjust)) {
                            break;
                        }
                    } else if (parent == null || reconcileState() == s) {
                        if (unarrived == 0) {
                            this.root.internalAwaitAdvance(phase, null);
                        } else {
                            if (U.compareAndSwapLong(this, STATE, s, s + adjust)) {
                                break;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
            throw new IllegalStateException(badRegister(s));
        }
        return phase;
    }

    private long reconcileState() {
        Phaser root = this.root;
        long s = this.state;
        if (root == this) {
            return s;
        }
        while (true) {
            int phase = (int) (root.state >>> 32);
            if (phase == ((int) (s >>> 32))) {
                return s;
            }
            long j;
            Unsafe unsafe = U;
            long j2 = STATE;
            long j3 = ((long) phase) << 32;
            if (phase < 0) {
                j = COUNTS_MASK & s;
            } else {
                int p = ((int) s) >>> 16;
                if (p == 0) {
                    j = 1;
                } else {
                    j = (PARTIES_MASK & s) | ((long) p);
                }
            }
            long s2 = j3 | j;
            if ((unsafe.compareAndSwapLong(this, j2, s, s2) ^ 1) == 0) {
                return s2;
            }
            s = this.state;
        }
    }

    public Phaser() {
        this(null, 0);
    }

    public Phaser(int parties) {
        this(null, parties);
    }

    public Phaser(Phaser parent) {
        this(parent, 0);
    }

    public Phaser(Phaser parent, int parties) {
        if ((parties >>> 16) != 0) {
            throw new IllegalArgumentException("Illegal number of parties");
        }
        long j;
        int phase = 0;
        this.parent = parent;
        if (parent != null) {
            Phaser root = parent.root;
            this.root = root;
            this.evenQ = root.evenQ;
            this.oddQ = root.oddQ;
            if (parties != 0) {
                phase = parent.doRegister(1);
            }
        } else {
            this.root = this;
            this.evenQ = new AtomicReference();
            this.oddQ = new AtomicReference();
        }
        if (parties == 0) {
            j = 1;
        } else {
            j = ((((long) phase) << 32) | (((long) parties) << 16)) | ((long) parties);
        }
        this.state = j;
    }

    public int register() {
        return doRegister(1);
    }

    public int bulkRegister(int parties) {
        if (parties < 0) {
            throw new IllegalArgumentException();
        } else if (parties == 0) {
            return getPhase();
        } else {
            return doRegister(parties);
        }
    }

    public int arrive() {
        return doArrive(1);
    }

    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }

    public int arriveAndAwaitAdvance() {
        long s;
        int phase;
        int unarrived;
        Phaser root = this.root;
        long s2;
        do {
            s2 = root == this ? this.state : reconcileState();
            phase = (int) (s2 >>> 32);
            if (phase < 0) {
                return phase;
            }
            int counts = (int) s2;
            unarrived = counts == 1 ? 0 : counts & 65535;
            if (unarrived <= 0) {
                throw new IllegalStateException(badArrive(s2));
            }
            s = s2 - 1;
        } while (!U.compareAndSwapLong(this, STATE, s2, s));
        if (unarrived > 1) {
            return root.internalAwaitAdvance(phase, null);
        }
        if (root != this) {
            return this.parent.arriveAndAwaitAdvance();
        }
        long n = s & PARTIES_MASK;
        int nextUnarrived = ((int) n) >>> 16;
        if (onAdvance(phase, nextUnarrived)) {
            n |= Long.MIN_VALUE;
        } else if (nextUnarrived == 0) {
            n |= 1;
        } else {
            n |= (long) nextUnarrived;
        }
        int nextPhase = (phase + 1) & Integer.MAX_VALUE;
        if (!U.compareAndSwapLong(this, STATE, s, n | (((long) nextPhase) << 32))) {
            return (int) (this.state >>> 32);
        }
        releaseWaiters(phase);
        return nextPhase;
    }

    public int awaitAdvance(int phase) {
        Phaser root = this.root;
        int p = (int) ((root == this ? this.state : reconcileState()) >>> 32);
        if (phase < 0) {
            return phase;
        }
        if (p == phase) {
            return root.internalAwaitAdvance(phase, null);
        }
        return p;
    }

    public int awaitAdvanceInterruptibly(int phase) throws InterruptedException {
        Phaser root = this.root;
        int p = (int) ((root == this ? this.state : reconcileState()) >>> 32);
        if (phase < 0) {
            return phase;
        }
        if (p == phase) {
            QNode node = new QNode(this, phase, true, false, STATE);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted) {
                throw new InterruptedException();
            }
        }
        return p;
    }

    public int awaitAdvanceInterruptibly(int phase, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long nanos = unit.toNanos(timeout);
        Phaser root = this.root;
        int p = (int) ((root == this ? this.state : reconcileState()) >>> 32);
        if (phase < 0) {
            return phase;
        }
        if (p == phase) {
            QNode node = new QNode(this, phase, true, true, nanos);
            p = root.internalAwaitAdvance(phase, node);
            if (node.wasInterrupted) {
                throw new InterruptedException();
            } else if (p == phase) {
                throw new TimeoutException();
            }
        }
        return p;
    }

    public void forceTermination() {
        Phaser root = this.root;
        long s;
        do {
            s = root.state;
            if (s < STATE) {
                return;
            }
        } while (!U.compareAndSwapLong(root, STATE, s, Long.MIN_VALUE | s));
        releaseWaiters(0);
        releaseWaiters(1);
    }

    public final int getPhase() {
        return (int) (this.root.state >>> 32);
    }

    public int getRegisteredParties() {
        return partiesOf(this.state);
    }

    public int getArrivedParties() {
        return arrivedOf(reconcileState());
    }

    public int getUnarrivedParties() {
        return unarrivedOf(reconcileState());
    }

    public Phaser getParent() {
        return this.parent;
    }

    public Phaser getRoot() {
        return this.root;
    }

    public boolean isTerminated() {
        return this.root.state < STATE;
    }

    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }

    public String toString() {
        return stateToString(reconcileState());
    }

    private String stateToString(long s) {
        return super.toString() + "[phase = " + phaseOf(s) + " parties = " + partiesOf(s) + " arrived = " + arrivedOf(s) + "]";
    }

    private void releaseWaiters(int phase) {
        AtomicReference<QNode> head = (phase & 1) == 0 ? this.evenQ : this.oddQ;
        while (true) {
            QNode q = (QNode) head.get();
            if (q != null && q.phase != ((int) (this.root.state >>> 32))) {
                if (head.compareAndSet(q, q.next)) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                }
            } else {
                return;
            }
        }
    }

    private int abortWait(int phase) {
        int p;
        AtomicReference<QNode> head = (phase & 1) == 0 ? this.evenQ : this.oddQ;
        while (true) {
            QNode q = (QNode) head.get();
            p = (int) (this.root.state >>> 32);
            if (q == null) {
                break;
            }
            Thread t = q.thread;
            if (t != null && q.phase == p) {
                break;
            } else if (head.compareAndSet(q, q.next) && t != null) {
                q.thread = null;
                LockSupport.unpark(t);
            }
        }
        return p;
    }

    static {
        int i;
        if (NCPU < 2) {
            i = 1;
        } else {
            i = 256;
        }
        SPINS_PER_ARRIVAL = i;
        try {
            STATE = U.objectFieldOffset(Phaser.class.getDeclaredField("state"));
            Class<?> ensureLoaded = LockSupport.class;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private int internalAwaitAdvance(int phase, QNode node) {
        int p;
        releaseWaiters(phase - 1);
        boolean queued = false;
        int lastUnarrived = 0;
        int spins = SPINS_PER_ARRIVAL;
        while (true) {
            long s = this.state;
            p = (int) (s >>> 32);
            if (p != phase) {
                break;
            } else if (node == null) {
                int unarrived = ((int) s) & 65535;
                if (unarrived != lastUnarrived) {
                    lastUnarrived = unarrived;
                    if (unarrived < NCPU) {
                        spins += SPINS_PER_ARRIVAL;
                    }
                }
                boolean interrupted = Thread.interrupted();
                if (!interrupted) {
                    spins--;
                    if (spins >= 0) {
                    }
                }
                QNode qNode = new QNode(this, phase, false, false, STATE);
                qNode.wasInterrupted = interrupted;
            } else if (node.isReleasable()) {
                break;
            } else if (queued) {
                try {
                    ForkJoinPool.managedBlock(node);
                } catch (InterruptedException e) {
                    node.wasInterrupted = true;
                }
            } else {
                AtomicReference<QNode> head = (phase & 1) == 0 ? this.evenQ : this.oddQ;
                QNode q = (QNode) head.get();
                node.next = q;
                if ((q == null || q.phase == phase) && ((int) (this.state >>> 32)) == phase) {
                    queued = head.compareAndSet(q, node);
                }
            }
        }
        if (node != null) {
            if (node.thread != null) {
                node.thread = null;
            }
            if (node.wasInterrupted && (node.interruptible ^ 1) != 0) {
                Thread.currentThread().interrupt();
            }
            if (p == phase) {
                p = (int) (this.state >>> 32);
                if (p == phase) {
                    return abortWait(phase);
                }
            }
        }
        releaseWaiters(phase);
        return p;
    }
}
