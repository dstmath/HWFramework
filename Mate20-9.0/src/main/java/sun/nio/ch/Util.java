package sun.nio.ch;

import java.nio.ByteBuffer;
import java.security.AccessController;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.security.action.GetPropertyAction;

public class Util {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    /* access modifiers changed from: private */
    public static final int TEMP_BUF_POOL_SIZE = IOUtil.IOV_MAX;
    private static ThreadLocal<BufferCache> bufferCache = new ThreadLocal<BufferCache>() {
        /* access modifiers changed from: protected */
        public BufferCache initialValue() {
            return new BufferCache();
        }
    };
    private static volatile String bugLevel = null;
    private static int pageSize = -1;
    private static Unsafe unsafe = Unsafe.getUnsafe();

    private static class BufferCache {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private ByteBuffer[] buffers = new ByteBuffer[Util.TEMP_BUF_POOL_SIZE];
        private int count;
        private int start;

        static {
            Class<Util> cls = Util.class;
        }

        private int next(int i) {
            return (i + 1) % Util.TEMP_BUF_POOL_SIZE;
        }

        BufferCache() {
        }

        /* access modifiers changed from: package-private */
        public ByteBuffer get(int size) {
            if (this.count == 0) {
                return null;
            }
            ByteBuffer[] buffers2 = this.buffers;
            ByteBuffer buf = buffers2[this.start];
            if (buf.capacity() < size) {
                buf = null;
                int i = this.start;
                while (true) {
                    int next = next(i);
                    i = next;
                    if (next == this.start) {
                        break;
                    }
                    ByteBuffer bb = buffers2[i];
                    if (bb != null) {
                        if (bb.capacity() >= size) {
                            buf = bb;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (buf == null) {
                    return null;
                }
                buffers2[i] = buffers2[this.start];
            }
            buffers2[this.start] = null;
            this.start = next(this.start);
            this.count--;
            buf.rewind();
            buf.limit(size);
            return buf;
        }

        /* access modifiers changed from: package-private */
        public boolean offerFirst(ByteBuffer buf) {
            if (this.count >= Util.TEMP_BUF_POOL_SIZE) {
                return false;
            }
            this.start = ((this.start + Util.TEMP_BUF_POOL_SIZE) - 1) % Util.TEMP_BUF_POOL_SIZE;
            this.buffers[this.start] = buf;
            this.count++;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean offerLast(ByteBuffer buf) {
            if (this.count >= Util.TEMP_BUF_POOL_SIZE) {
                return false;
            }
            this.buffers[(this.start + this.count) % Util.TEMP_BUF_POOL_SIZE] = buf;
            this.count++;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean isEmpty() {
            return this.count == 0;
        }

        /* access modifiers changed from: package-private */
        public ByteBuffer removeFirst() {
            ByteBuffer buf = this.buffers[this.start];
            this.buffers[this.start] = null;
            this.start = next(this.start);
            this.count--;
            return buf;
        }
    }

    public static ByteBuffer getTemporaryDirectBuffer(int size) {
        BufferCache cache = bufferCache.get();
        ByteBuffer buf = cache.get(size);
        if (buf != null) {
            return buf;
        }
        if (!cache.isEmpty()) {
            free(cache.removeFirst());
        }
        return ByteBuffer.allocateDirect(size);
    }

    public static void releaseTemporaryDirectBuffer(ByteBuffer buf) {
        offerFirstTemporaryDirectBuffer(buf);
    }

    static void offerFirstTemporaryDirectBuffer(ByteBuffer buf) {
        if (!bufferCache.get().offerFirst(buf)) {
            free(buf);
        }
    }

    static void offerLastTemporaryDirectBuffer(ByteBuffer buf) {
        if (!bufferCache.get().offerLast(buf)) {
            free(buf);
        }
    }

    private static void free(ByteBuffer buf) {
        Cleaner cleaner = ((DirectBuffer) buf).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    static ByteBuffer[] subsequence(ByteBuffer[] bs, int offset, int length) {
        if (offset == 0 && length == bs.length) {
            return bs;
        }
        int n = length;
        ByteBuffer[] bs2 = new ByteBuffer[n];
        for (int i = 0; i < n; i++) {
            bs2[i] = bs[offset + i];
        }
        return bs2;
    }

    static <E> Set<E> ungrowableSet(final Set<E> s) {
        return new Set<E>() {
            public int size() {
                return Set.this.size();
            }

            public boolean isEmpty() {
                return Set.this.isEmpty();
            }

            public boolean contains(Object o) {
                return Set.this.contains(o);
            }

            public Object[] toArray() {
                return Set.this.toArray();
            }

            public <T> T[] toArray(T[] a) {
                return Set.this.toArray(a);
            }

            public String toString() {
                return Set.this.toString();
            }

            public Iterator<E> iterator() {
                return Set.this.iterator();
            }

            public boolean equals(Object o) {
                return Set.this.equals(o);
            }

            public int hashCode() {
                return Set.this.hashCode();
            }

            public void clear() {
                Set.this.clear();
            }

            public boolean remove(Object o) {
                return Set.this.remove(o);
            }

            public boolean containsAll(Collection<?> coll) {
                return Set.this.containsAll(coll);
            }

            public boolean removeAll(Collection<?> coll) {
                return Set.this.removeAll(coll);
            }

            public boolean retainAll(Collection<?> coll) {
                return Set.this.retainAll(coll);
            }

            public boolean add(E e) {
                throw new UnsupportedOperationException();
            }

            public boolean addAll(Collection<? extends E> collection) {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static byte _get(long a) {
        return unsafe.getByte(a);
    }

    private static void _put(long a, byte b) {
        unsafe.putByte(a, b);
    }

    static void erase(ByteBuffer bb) {
        unsafe.setMemory(((DirectBuffer) bb).address(), (long) bb.capacity(), (byte) 0);
    }

    static Unsafe unsafe() {
        return unsafe;
    }

    static int pageSize() {
        if (pageSize == -1) {
            pageSize = unsafe().pageSize();
        }
        return pageSize;
    }

    static boolean atBugLevel(String bl) {
        if (bugLevel == null) {
            if (!VM.isBooted()) {
                return false;
            }
            String value = (String) AccessController.doPrivileged(new GetPropertyAction("sun.nio.ch.bugLevel"));
            bugLevel = value != null ? value : "";
        }
        return bugLevel.equals(bl);
    }
}
