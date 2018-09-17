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
    static final /* synthetic */ boolean -assertionsDisabled = (Util.class.desiredAssertionStatus() ^ 1);
    private static final int TEMP_BUF_POOL_SIZE = IOUtil.IOV_MAX;
    private static ThreadLocal<BufferCache> bufferCache = new ThreadLocal<BufferCache>() {
        protected BufferCache initialValue() {
            return new BufferCache();
        }
    };
    private static volatile String bugLevel = null;
    private static int pageSize = -1;
    private static Unsafe unsafe = Unsafe.getUnsafe();

    private static class BufferCache {
        static final /* synthetic */ boolean -assertionsDisabled = (BufferCache.class.desiredAssertionStatus() ^ 1);
        private ByteBuffer[] buffers = new ByteBuffer[Util.TEMP_BUF_POOL_SIZE];
        private int count;
        private int start;

        private int next(int i) {
            return (i + 1) % Util.TEMP_BUF_POOL_SIZE;
        }

        BufferCache() {
        }

        ByteBuffer get(int size) {
            if (this.count == 0) {
                return null;
            }
            ByteBuffer[] buffers = this.buffers;
            ByteBuffer buf = buffers[this.start];
            if (buf.capacity() < size) {
                ByteBuffer bb;
                buf = null;
                int i = this.start;
                do {
                    i = next(i);
                    if (i == this.start) {
                        break;
                    }
                    bb = buffers[i];
                    if (bb == null) {
                        break;
                    }
                } while (bb.capacity() < size);
                buf = bb;
                if (buf == null) {
                    return null;
                }
                buffers[i] = buffers[this.start];
            }
            buffers[this.start] = null;
            this.start = next(this.start);
            this.count--;
            buf.rewind();
            buf.limit(size);
            return buf;
        }

        boolean offerFirst(ByteBuffer buf) {
            if (this.count >= Util.TEMP_BUF_POOL_SIZE) {
                return false;
            }
            this.start = ((this.start + Util.TEMP_BUF_POOL_SIZE) - 1) % Util.TEMP_BUF_POOL_SIZE;
            this.buffers[this.start] = buf;
            this.count++;
            return true;
        }

        boolean offerLast(ByteBuffer buf) {
            if (this.count >= Util.TEMP_BUF_POOL_SIZE) {
                return false;
            }
            this.buffers[(this.start + this.count) % Util.TEMP_BUF_POOL_SIZE] = buf;
            this.count++;
            return true;
        }

        boolean isEmpty() {
            return this.count == 0;
        }

        ByteBuffer removeFirst() {
            if (-assertionsDisabled || this.count > 0) {
                ByteBuffer buf = this.buffers[this.start];
                this.buffers[this.start] = null;
                this.start = next(this.start);
                this.count--;
                return buf;
            }
            throw new AssertionError();
        }
    }

    public static ByteBuffer getTemporaryDirectBuffer(int size) {
        BufferCache cache = (BufferCache) bufferCache.get();
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
        if (!-assertionsDisabled && buf == null) {
            throw new AssertionError();
        } else if (!((BufferCache) bufferCache.get()).offerFirst(buf)) {
            free(buf);
        }
    }

    static void offerLastTemporaryDirectBuffer(ByteBuffer buf) {
        if (!-assertionsDisabled && buf == null) {
            throw new AssertionError();
        } else if (!((BufferCache) bufferCache.get()).offerLast(buf)) {
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
        ByteBuffer[] bs2 = new ByteBuffer[length];
        for (int i = 0; i < length; i++) {
            bs2[i] = bs[offset + i];
        }
        return bs2;
    }

    static <E> Set<E> ungrowableSet(final Set<E> s) {
        return new Set<E>() {
            public int size() {
                return s.size();
            }

            public boolean isEmpty() {
                return s.isEmpty();
            }

            public boolean contains(Object o) {
                return s.contains(o);
            }

            public Object[] toArray() {
                return s.toArray();
            }

            public <T> T[] toArray(T[] a) {
                return s.toArray(a);
            }

            public String toString() {
                return s.toString();
            }

            public Iterator<E> iterator() {
                return s.iterator();
            }

            public boolean equals(Object o) {
                return s.equals(o);
            }

            public int hashCode() {
                return s.hashCode();
            }

            public void clear() {
                s.clear();
            }

            public boolean remove(Object o) {
                return s.remove(o);
            }

            public boolean containsAll(Collection<?> coll) {
                return s.containsAll(coll);
            }

            public boolean removeAll(Collection<?> coll) {
                return s.removeAll(coll);
            }

            public boolean retainAll(Collection<?> coll) {
                return s.retainAll(coll);
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
            if (value == null) {
                value = "";
            }
            bugLevel = value;
        }
        return bugLevel.equals(bl);
    }
}
