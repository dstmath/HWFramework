package java.util.concurrent;

import sun.misc.Unsafe;

public class Exchanger<V> {
    private static final int ABASE;
    private static final int ASHIFT = 7;
    private static final long BLOCKER;
    private static final long BOUND;
    static final int FULL = (NCPU >= 510 ? MMASK : NCPU >>> 1);
    private static final long MATCH;
    private static final int MMASK = 255;
    private static final int NCPU = Runtime.getRuntime().availableProcessors();
    private static final Object NULL_ITEM = new Object();
    private static final int SEQ = 256;
    private static final long SLOT;
    private static final int SPINS = 1024;
    private static final Object TIMED_OUT = new Object();
    private static final Unsafe U = Unsafe.getUnsafe();
    private volatile Node[] arena;
    private volatile int bound;
    private final Participant participant = new Participant();
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
        try {
            BOUND = U.objectFieldOffset(Exchanger.class.getDeclaredField("bound"));
            SLOT = U.objectFieldOffset(Exchanger.class.getDeclaredField("slot"));
            MATCH = U.objectFieldOffset(Node.class.getDeclaredField("match"));
            BLOCKER = U.objectFieldOffset(Thread.class.getDeclaredField("parkBlocker"));
            int scale = U.arrayIndexScale(Node[].class);
            if (((scale - 1) & scale) != 0 || scale > 128) {
                throw new Error("Unsupported array scale");
            }
            ABASE = U.arrayBaseOffset(Node[].class) + 128;
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    private final Object arenaExchange(Object item, boolean timed, long ns) {
        long ns2;
        Node q;
        Node[] a;
        Node[] a2;
        Thread t;
        int b;
        Thread thread;
        Thread t2;
        int h;
        Object v;
        int h2;
        int b2;
        int b3;
        Object obj = item;
        Node[] a3 = this.arena;
        Node p = (Node) this.participant.get();
        int i = p.index;
        long ns3 = ns;
        while (true) {
            int i2 = i;
            Unsafe unsafe = U;
            long j = (long) ((i2 << 7) + ABASE);
            long j2 = j;
            Node q2 = (Node) unsafe.getObjectVolatile(a3, j);
            if (q2 != null) {
                ns2 = ns3;
                q = q2;
                if (U.compareAndSwapObject(a3, j2, q2, null)) {
                    Object v2 = q.item;
                    q.match = obj;
                    Thread w = q.parked;
                    if (w != null) {
                        U.unpark(w);
                    }
                    return v2;
                }
            } else {
                ns2 = ns3;
                q = q2;
            }
            int i3 = this.bound;
            int b4 = i3;
            int i4 = i3 & MMASK;
            int m = i4;
            if (i2 > i4 || q != null) {
                int m2 = m;
                a = a3;
                Node node = q;
                int b5 = b4;
                if (p.bound != b5) {
                    p.bound = b5;
                    p.collides = 0;
                    i = (i2 != m2 || m2 == 0) ? m2 : m2 - 1;
                } else {
                    int i5 = p.collides;
                    int c = i5;
                    if (i5 >= m2 && m2 != FULL) {
                        if (U.compareAndSwapInt(this, BOUND, b5, b5 + 256 + 1)) {
                            i = m2 + 1;
                        }
                    }
                    p.collides = c + 1;
                    i = i2 == 0 ? m2 : i2 - 1;
                }
                p.index = i;
            } else {
                p.item = obj;
                int m3 = m;
                Thread thread2 = null;
                if (U.compareAndSwapObject(a3, j2, null, p)) {
                    long end = (!timed || m3 != 0) ? 0 : System.nanoTime() + ns2;
                    Thread t3 = Thread.currentThread();
                    int h3 = p.hash;
                    int spins = 1024;
                    while (true) {
                        int spins2 = spins;
                        Object v3 = p.match;
                        if (v3 != null) {
                            Node node2 = q;
                            int i6 = b4;
                            U.putOrderedObject(p, MATCH, thread2);
                            p.item = thread2;
                            p.hash = h3;
                            return v3;
                        }
                        Node q3 = q;
                        int b6 = b4;
                        if (spins2 > 0) {
                            int h4 = (h3 << 1) ^ h3;
                            int h5 = h4 ^ (h4 >>> 3);
                            int h6 = h5 ^ (h5 << 10);
                            if (h6 == 0) {
                                h6 = 1024 | ((int) t3.getId());
                            } else if (h6 < 0) {
                                int spins3 = spins2 - 1;
                                if ((spins3 & 511) == 0) {
                                    Thread.yield();
                                }
                                h3 = h6;
                                spins = spins3;
                                t2 = t3;
                                a2 = a3;
                            }
                            h3 = h6;
                            t2 = t3;
                            a2 = a3;
                            spins = spins2;
                        } else if (U.getObjectVolatile(a3, j2) != p) {
                            spins = 1024;
                            t2 = t3;
                            a2 = a3;
                        } else {
                            if (t3.isInterrupted() == 0 && m3 == 0) {
                                if (timed) {
                                    long nanoTime = end - System.nanoTime();
                                    ns2 = nanoTime;
                                    if (nanoTime <= 0) {
                                        h = h3;
                                        v = v3;
                                    }
                                }
                                long ns4 = ns2;
                                int h7 = h3;
                                Object obj2 = v3;
                                U.putObject(t3, BLOCKER, this);
                                p.parked = t3;
                                if (U.getObjectVolatile(a3, j2) == p) {
                                    U.park(false, ns4);
                                }
                                p.parked = thread2;
                                U.putObject(t3, BLOCKER, thread2);
                                ns2 = ns4;
                                t2 = t3;
                                a2 = a3;
                                spins = spins2;
                                b = b6;
                                h3 = h7;
                                thread = thread2;
                                thread2 = thread;
                                b4 = b;
                                q = q3;
                                t3 = t;
                                a3 = a2;
                            } else {
                                h = h3;
                                v = v3;
                            }
                            if (U.getObjectVolatile(a3, j2) == p) {
                                Object obj3 = v;
                                int h8 = h;
                                t = t3;
                                a = a3;
                                thread = thread2;
                                if (U.compareAndSwapObject(a3, j2, p, null)) {
                                    if (m3 != 0) {
                                        U.compareAndSwapInt(this, BOUND, b3, (b6 + 256) - 1);
                                    }
                                    p.item = thread;
                                    p.hash = h8;
                                    int i7 = p.index >>> 1;
                                    p.index = i7;
                                    if (Thread.interrupted()) {
                                        return thread;
                                    }
                                    if (timed && m3 == 0 && ns2 <= 0) {
                                        return TIMED_OUT;
                                    }
                                    i = i7;
                                } else {
                                    b2 = b6;
                                    h2 = h8;
                                }
                            } else {
                                t = t3;
                                a = a3;
                                b2 = b6;
                                h2 = h;
                                thread = thread2;
                            }
                            h3 = h2;
                            spins = spins2;
                            thread2 = thread;
                            b4 = b;
                            q = q3;
                            t3 = t;
                            a3 = a2;
                        }
                        b = b6;
                        thread = thread2;
                        thread2 = thread;
                        b4 = b;
                        q = q3;
                        t3 = t;
                        a3 = a2;
                    }
                } else {
                    a = a3;
                    Node node3 = q;
                    int i8 = b4;
                    p.item = null;
                    i = i2;
                }
            }
            ns3 = ns2;
            a3 = a;
            obj = item;
        }
    }

    private final Object slotExchange(Object item, boolean timed, long ns) {
        Object v;
        int h;
        long ns2;
        Object obj = item;
        Node p = (Node) this.participant.get();
        Thread t = Thread.currentThread();
        if (t.isInterrupted()) {
            return null;
        }
        while (true) {
            Node node = this.slot;
            Node q = node;
            int spins = 1;
            if (node != null) {
                if (U.compareAndSwapObject(this, SLOT, q, null)) {
                    Object v2 = q.item;
                    q.match = obj;
                    Thread w = q.parked;
                    if (w != null) {
                        U.unpark(w);
                    }
                    return v2;
                } else if (NCPU > 1 && this.bound == 0) {
                    if (U.compareAndSwapInt(this, BOUND, 0, 256)) {
                        this.arena = new Node[((FULL + 2) << 7)];
                    }
                }
            } else if (this.arena != null) {
                return null;
            } else {
                p.item = obj;
                if (U.compareAndSwapObject(this, SLOT, null, p)) {
                    int h2 = p.hash;
                    long end = timed ? System.nanoTime() + ns : 0;
                    int h3 = 1024;
                    if (NCPU > 1) {
                        spins = 1024;
                    }
                    long ns3 = ns;
                    int h4 = h2;
                    while (true) {
                        Object obj2 = p.match;
                        v = obj2;
                        if (obj2 != null) {
                            h = h4;
                            break;
                        } else if (spins > 0) {
                            int h5 = (h4 << 1) ^ h4;
                            int h6 = h5 ^ (h5 >>> 3);
                            h4 = h6 ^ (h6 << 10);
                            if (h4 == 0) {
                                h4 = h3 | ((int) t.getId());
                            } else if (h4 < 0) {
                                spins--;
                                if ((spins & 511) == 0) {
                                    Thread.yield();
                                }
                            }
                        } else if (this.slot != p) {
                            spins = 1024;
                        } else {
                            if (t.isInterrupted() || this.arena != null) {
                                ns2 = ns3;
                            } else {
                                if (timed) {
                                    long nanoTime = end - System.nanoTime();
                                    long ns4 = nanoTime;
                                    if (nanoTime > 0) {
                                        ns3 = ns4;
                                    } else {
                                        ns2 = ns4;
                                    }
                                }
                                U.putObject(t, BLOCKER, this);
                                p.parked = t;
                                if (this.slot == p) {
                                    U.park(false, ns3);
                                }
                                p.parked = null;
                                U.putObject(t, BLOCKER, null);
                            }
                            h = h4;
                            if (U.compareAndSwapObject(this, SLOT, p, null)) {
                                v = (!timed || ns2 > 0 || t.isInterrupted()) ? null : TIMED_OUT;
                                long j = ns2;
                            } else {
                                h4 = h;
                                ns3 = ns2;
                                h3 = 1024;
                            }
                        }
                    }
                    U.putOrderedObject(p, MATCH, null);
                    p.item = null;
                    p.hash = h;
                    return v;
                }
                p.item = null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001f, code lost:
        if (r1 != null) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
        r1 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        if (r1 != NULL_ITEM) goto L_0x0028;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0012, code lost:
        if (r1 == null) goto L_0x0014;
     */
    public V exchange(V x) throws InterruptedException {
        Object v;
        Object item = x == null ? NULL_ITEM : x;
        if (this.arena == null) {
            Object slotExchange = slotExchange(item, false, 0);
            v = slotExchange;
        }
        if (!Thread.interrupted()) {
            Object arenaExchange = arenaExchange(item, false, 0);
            v = arenaExchange;
        }
        throw new InterruptedException();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0021, code lost:
        if (r3 != null) goto L_0x0023;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0023, code lost:
        r3 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0026, code lost:
        if (r3 == TIMED_OUT) goto L_0x0030;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        if (r3 != NULL_ITEM) goto L_0x002e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0035, code lost:
        throw new java.util.concurrent.TimeoutException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
        if (r3 == null) goto L_0x0016;
     */
    public V exchange(V x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        Object v;
        Object item = x == null ? NULL_ITEM : x;
        long ns = unit.toNanos(timeout);
        if (this.arena == null) {
            Object slotExchange = slotExchange(item, true, ns);
            v = slotExchange;
        }
        if (!Thread.interrupted()) {
            Object arenaExchange = arenaExchange(item, true, ns);
            v = arenaExchange;
        }
        throw new InterruptedException();
    }
}
