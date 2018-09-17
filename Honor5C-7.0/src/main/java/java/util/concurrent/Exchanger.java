package java.util.concurrent;

import dalvik.bytecode.Opcodes;
import sun.misc.Unsafe;

public class Exchanger<V> {
    private static final int ABASE = 0;
    private static final int ASHIFT = 7;
    private static final long BLOCKER = 0;
    private static final long BOUND = 0;
    static final int FULL = 0;
    private static final long MATCH = 0;
    private static final int MMASK = 255;
    private static final int NCPU = 0;
    private static final Object NULL_ITEM = null;
    private static final int SEQ = 256;
    private static final long SLOT = 0;
    private static final int SPINS = 1024;
    private static final Object TIMED_OUT = null;
    private static final Unsafe U = null;
    private volatile Node[] arena;
    private volatile int bound;
    private final Participant participant;
    private volatile Node slot;

    static final class Node {
        int bound;
        int collides;
        int hash;
        int index;
        Object item;
        volatile Object match;
        volatile Thread parked;

        Node() {
        }
    }

    static final class Participant extends ThreadLocal<Node> {
        Participant() {
        }

        public Node initialValue() {
            return new Node();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.Exchanger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.Exchanger.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.Exchanger.<clinit>():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final Object arenaExchange(Object item, boolean timed, long ns) {
        Node q;
        Object v;
        Object a = this.arena;
        Node p = (Node) this.participant.get();
        int i = p.index;
        while (true) {
            int i2 = (i << ASHIFT) + ABASE;
            long j = (long) i2;
            q = (Node) U.getObjectVolatile(a, (long) i2);
            if (q != null && U.compareAndSwapObject(a, j, q, null)) {
                break;
            }
            int b = this.bound;
            int m = b & MMASK;
            if (i > m || q != null) {
                if (p.bound != b) {
                    p.bound = b;
                    p.collides = NCPU;
                    i = (i != m || m == 0) ? m : m - 1;
                } else {
                    int c = p.collides;
                    if (c < m || m == FULL || !U.compareAndSwapInt(this, BOUND, b, (b + SEQ) + 1)) {
                        p.collides = c + 1;
                        i = i == 0 ? m : i - 1;
                    } else {
                        i = m + 1;
                    }
                }
                p.index = i;
            } else {
                p.item = item;
                if (U.compareAndSwapObject(a, j, null, p)) {
                    long end = (timed && m == 0) ? System.nanoTime() + ns : SLOT;
                    Thread t = Thread.currentThread();
                    int h = p.hash;
                    int spins = SPINS;
                    while (true) {
                        v = p.match;
                        if (v == null) {
                            if (spins <= 0) {
                                if (U.getObjectVolatile(a, j) == p) {
                                    if (!t.isInterrupted() && m == 0) {
                                        if (timed) {
                                            ns = end - System.nanoTime();
                                        }
                                        U.putObject(t, BLOCKER, this);
                                        p.parked = t;
                                        if (U.getObjectVolatile(a, j) == p) {
                                            U.park(false, ns);
                                        }
                                        p.parked = null;
                                        U.putObject(t, BLOCKER, null);
                                    }
                                    if (U.getObjectVolatile(a, j) == p && U.compareAndSwapObject(a, j, p, null)) {
                                        break;
                                    }
                                }
                                spins = SPINS;
                            } else {
                                h ^= h << 1;
                                h ^= h >>> 3;
                                h ^= h << 10;
                                if (h == 0) {
                                    h = ((int) t.getId()) | SPINS;
                                } else if (h < 0) {
                                    spins--;
                                    if ((spins & Opcodes.OP_CHECK_CAST_JUMBO) == 0) {
                                        Thread.yield();
                                    }
                                }
                            }
                        } else {
                            U.putOrderedObject(p, MATCH, null);
                            p.item = null;
                            p.hash = h;
                            return v;
                        }
                    }
                    if (m != 0) {
                        U.compareAndSwapInt(this, BOUND, b, (b + SEQ) - 1);
                    }
                    p.item = null;
                    p.hash = h;
                    i = p.index >>> 1;
                    p.index = i;
                    if (Thread.interrupted()) {
                        return null;
                    }
                    if (timed && m == 0 && ns <= SLOT) {
                        return TIMED_OUT;
                    }
                }
                p.item = null;
            }
        }
        v = q.item;
        q.match = item;
        Thread w = q.parked;
        if (w != null) {
            U.unpark(w);
        }
        return v;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final Object slotExchange(Object item, boolean timed, long ns) {
        Node p = (Node) this.participant.get();
        Thread t = Thread.currentThread();
        if (t.isInterrupted()) {
            return null;
        }
        Node q;
        while (true) {
            q = this.slot;
            if (q != null) {
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    break;
                } else if (NCPU > 1 && this.bound == 0 && U.compareAndSwapInt(this, BOUND, NCPU, SEQ)) {
                    this.arena = new Node[((FULL + 2) << ASHIFT)];
                }
            } else if (this.arena != null) {
                return null;
            } else {
                p.item = item;
                if (U.compareAndSwapObject(this, SLOT, null, p)) {
                    break;
                }
                p.item = null;
            }
        }
        Object v = q.item;
        q.match = item;
        Thread w = q.parked;
        if (w != null) {
            U.unpark(w);
        }
        return v;
    }

    public Exchanger() {
        this.participant = new Participant();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V exchange(V x) throws InterruptedException {
        if (x == null) {
            Object item = NULL_ITEM;
        } else {
            V item2 = x;
        }
        if (this.arena == null) {
            Object v = slotExchange(item, false, SLOT);
        }
        if (!Thread.interrupted()) {
            v = arenaExchange(item, false, SLOT);
        }
        throw new InterruptedException();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V exchange(V x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (x == null) {
            Object item = NULL_ITEM;
        } else {
            V item2 = x;
        }
        long ns = unit.toNanos(timeout);
        if (this.arena == null) {
            Object v = slotExchange(item, true, ns);
        }
        if (!Thread.interrupted()) {
            v = arenaExchange(item, true, ns);
        }
        throw new InterruptedException();
    }
}
