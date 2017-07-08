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
    private static final int NCPU = 0;
    private static final int ONE_ARRIVAL = 1;
    private static final int ONE_DEREGISTER = 65537;
    private static final int ONE_PARTY = 65536;
    private static final long PARTIES_MASK = 4294901760L;
    private static final int PARTIES_SHIFT = 16;
    private static final int PHASE_SHIFT = 32;
    static final int SPINS_PER_ARRIVAL = 0;
    private static final long STATE = 0;
    private static final long TERMINATION_BIT = Long.MIN_VALUE;
    private static final Unsafe U = null;
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

        /* JADX WARNING: inconsistent code. */
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.Phaser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.Phaser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.Phaser.<clinit>():void");
    }

    private static int unarrivedOf(long s) {
        int counts = (int) s;
        return counts == ONE_ARRIVAL ? SPINS_PER_ARRIVAL : UNARRIVED_MASK & counts;
    }

    private static int partiesOf(long s) {
        return ((int) s) >>> PARTIES_SHIFT;
    }

    private static int phaseOf(long s) {
        return (int) (s >>> PHASE_SHIFT);
    }

    private static int arrivedOf(long s) {
        int counts = (int) s;
        if (counts == ONE_ARRIVAL) {
            return SPINS_PER_ARRIVAL;
        }
        return (counts >>> PARTIES_SHIFT) - (UNARRIVED_MASK & counts);
    }

    private AtomicReference<QNode> queueFor(int phase) {
        return (phase & ONE_ARRIVAL) == 0 ? this.evenQ : this.oddQ;
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
        Phaser root = this.root;
        long reconcileState;
        do {
            reconcileState = root == this ? this.state : reconcileState();
            phase = (int) (reconcileState >>> PHASE_SHIFT);
            if (phase < 0) {
                return phase;
            }
            int counts = (int) reconcileState;
            int unarrived = counts == ONE_ARRIVAL ? SPINS_PER_ARRIVAL : counts & UNARRIVED_MASK;
            if (unarrived <= 0) {
                throw new IllegalStateException(badArrive(reconcileState));
            }
            s = reconcileState - ((long) adjust);
        } while (!U.compareAndSwapLong(this, STATE, reconcileState, s));
        if (unarrived == ONE_ARRIVAL) {
            long n = s & PARTIES_MASK;
            int nextUnarrived = ((int) n) >>> PARTIES_SHIFT;
            if (root == this) {
                if (onAdvance(phase, nextUnarrived)) {
                    n |= TERMINATION_BIT;
                } else if (nextUnarrived == 0) {
                    n |= 1;
                } else {
                    n |= (long) nextUnarrived;
                }
                U.compareAndSwapLong(this, STATE, s, n | (((long) ((phase + ONE_ARRIVAL) & MAX_PHASE)) << PHASE_SHIFT));
                releaseWaiters(phase);
            } else if (nextUnarrived == 0) {
                phase = this.parent.doArrive(ONE_DEREGISTER);
                U.compareAndSwapLong(this, STATE, s, s | 1);
            } else {
                phase = this.parent.doArrive(ONE_ARRIVAL);
            }
        }
        return phase;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int doRegister(int registrations) {
        int phase;
        long adjust = (((long) registrations) << PARTIES_SHIFT) | ((long) registrations);
        Phaser parent = this.parent;
        while (true) {
            long s = parent == null ? this.state : reconcileState();
            int counts = (int) s;
            int unarrived = counts & UNARRIVED_MASK;
            if (registrations <= UNARRIVED_MASK - (counts >>> PARTIES_SHIFT)) {
                phase = (int) (s >>> PHASE_SHIFT);
                if (phase < 0) {
                    break;
                } else if (counts != ONE_ARRIVAL) {
                    if (parent == null || reconcileState() == s) {
                        if (unarrived == 0) {
                            this.root.internalAwaitAdvance(phase, null);
                        } else if (U.compareAndSwapLong(this, STATE, s, s + adjust)) {
                            break;
                        }
                    }
                } else if (parent == null) {
                    if (U.compareAndSwapLong(this, STATE, s, (((long) phase) << PHASE_SHIFT) | adjust)) {
                        break;
                    }
                } else {
                    synchronized (this) {
                        if (this.state == s) {
                            break;
                        }
                    }
                }
            } else {
                break;
            }
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
            int phase = (int) (root.state >>> PHASE_SHIFT);
            if (phase == ((int) (s >>> PHASE_SHIFT))) {
                return s;
            }
            long j;
            Unsafe unsafe = U;
            long j2 = STATE;
            long j3 = ((long) phase) << PHASE_SHIFT;
            if (phase < 0) {
                j = COUNTS_MASK & s;
            } else {
                int p = ((int) s) >>> PARTIES_SHIFT;
                if (p == 0) {
                    j = 1;
                } else {
                    j = (PARTIES_MASK & s) | ((long) p);
                }
            }
            long s2 = j3 | j;
            if (unsafe.compareAndSwapLong(this, j2, s, s2)) {
                return s2;
            }
            s = this.state;
        }
    }

    public Phaser() {
        this(null, SPINS_PER_ARRIVAL);
    }

    public Phaser(int parties) {
        this(null, parties);
    }

    public Phaser(Phaser parent) {
        this(parent, SPINS_PER_ARRIVAL);
    }

    public Phaser(Phaser parent, int parties) {
        if ((parties >>> PARTIES_SHIFT) != 0) {
            throw new IllegalArgumentException("Illegal number of parties");
        }
        long j;
        int phase = SPINS_PER_ARRIVAL;
        this.parent = parent;
        if (parent != null) {
            Phaser root = parent.root;
            this.root = root;
            this.evenQ = root.evenQ;
            this.oddQ = root.oddQ;
            if (parties != 0) {
                phase = parent.doRegister(ONE_ARRIVAL);
            }
        } else {
            this.root = this;
            this.evenQ = new AtomicReference();
            this.oddQ = new AtomicReference();
        }
        if (parties == 0) {
            j = 1;
        } else {
            j = ((((long) phase) << PHASE_SHIFT) | (((long) parties) << PARTIES_SHIFT)) | ((long) parties);
        }
        this.state = j;
    }

    public int register() {
        return doRegister(ONE_ARRIVAL);
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
        return doArrive(ONE_ARRIVAL);
    }

    public int arriveAndDeregister() {
        return doArrive(ONE_DEREGISTER);
    }

    public int arriveAndAwaitAdvance() {
        Phaser root = this.root;
        long reconcileState;
        long s;
        do {
            reconcileState = root == this ? this.state : reconcileState();
            int phase = (int) (reconcileState >>> PHASE_SHIFT);
            if (phase < 0) {
                return phase;
            }
            int counts = (int) reconcileState;
            int unarrived = counts == ONE_ARRIVAL ? SPINS_PER_ARRIVAL : counts & UNARRIVED_MASK;
            if (unarrived <= 0) {
                throw new IllegalStateException(badArrive(reconcileState));
            }
            s = reconcileState - 1;
        } while (!U.compareAndSwapLong(this, STATE, reconcileState, s));
        if (unarrived > ONE_ARRIVAL) {
            return root.internalAwaitAdvance(phase, null);
        }
        if (root != this) {
            return this.parent.arriveAndAwaitAdvance();
        }
        long n = s & PARTIES_MASK;
        int nextUnarrived = ((int) n) >>> PARTIES_SHIFT;
        if (onAdvance(phase, nextUnarrived)) {
            n |= TERMINATION_BIT;
        } else if (nextUnarrived == 0) {
            n |= 1;
        } else {
            n |= (long) nextUnarrived;
        }
        int nextPhase = (phase + ONE_ARRIVAL) & MAX_PHASE;
        if (!U.compareAndSwapLong(this, STATE, s, n | (((long) nextPhase) << PHASE_SHIFT))) {
            return (int) (this.state >>> PHASE_SHIFT);
        }
        releaseWaiters(phase);
        return nextPhase;
    }

    public int awaitAdvance(int phase) {
        Phaser root = this.root;
        int p = (int) ((root == this ? this.state : reconcileState()) >>> PHASE_SHIFT);
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
        int p = (int) ((root == this ? this.state : reconcileState()) >>> PHASE_SHIFT);
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
        int p = (int) ((root == this ? this.state : reconcileState()) >>> PHASE_SHIFT);
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
        } while (!U.compareAndSwapLong(root, STATE, s, TERMINATION_BIT | s));
        releaseWaiters(SPINS_PER_ARRIVAL);
        releaseWaiters(ONE_ARRIVAL);
    }

    public final int getPhase() {
        return (int) (this.root.state >>> PHASE_SHIFT);
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
        AtomicReference<QNode> head = (phase & ONE_ARRIVAL) == 0 ? this.evenQ : this.oddQ;
        while (true) {
            QNode q = (QNode) head.get();
            if (q != null && q.phase != ((int) (this.root.state >>> PHASE_SHIFT))) {
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
        AtomicReference<QNode> head = (phase & ONE_ARRIVAL) == 0 ? this.evenQ : this.oddQ;
        while (true) {
            QNode q = (QNode) head.get();
            p = (int) (this.root.state >>> PHASE_SHIFT);
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

    private int internalAwaitAdvance(int phase, QNode node) {
        int p;
        releaseWaiters(phase - 1);
        boolean queued = false;
        int lastUnarrived = SPINS_PER_ARRIVAL;
        int spins = SPINS_PER_ARRIVAL;
        while (true) {
            long s = this.state;
            p = (int) (s >>> PHASE_SHIFT);
            if (p != phase) {
                break;
            } else if (node == null) {
                int unarrived = ((int) s) & UNARRIVED_MASK;
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
                AtomicReference<QNode> head = (phase & ONE_ARRIVAL) == 0 ? this.evenQ : this.oddQ;
                QNode q = (QNode) head.get();
                node.next = q;
                if ((q == null || q.phase == phase) && ((int) (this.state >>> PHASE_SHIFT)) == phase) {
                    queued = head.compareAndSet(q, node);
                }
            }
        }
        if (node != null) {
            if (node.thread != null) {
                node.thread = null;
            }
            if (node.wasInterrupted && !node.interruptible) {
                Thread.currentThread().interrupt();
            }
            if (p == phase) {
                p = (int) (this.state >>> PHASE_SHIFT);
                if (p == phase) {
                    return abortWait(phase);
                }
            }
        }
        releaseWaiters(phase);
        return p;
    }
}
