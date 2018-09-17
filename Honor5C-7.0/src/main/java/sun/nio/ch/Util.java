package sun.nio.ch;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.security.AccessController;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;
import sun.security.action.GetPropertyAction;

class Util {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int TEMP_BUF_POOL_SIZE = 0;
    private static ThreadLocal<BufferCache> bufferCache;
    private static volatile String bugLevel;
    private static ThreadLocal<SoftReference<SelectorWrapper>> localSelector;
    private static ThreadLocal<SelectorWrapper> localSelectorWrapper;
    private static int pageSize;
    private static Unsafe unsafe;

    /* renamed from: sun.nio.ch.Util.2 */
    static class AnonymousClass2 implements Set<E> {
        final /* synthetic */ Set val$s;

        AnonymousClass2(Set val$s) {
            this.val$s = val$s;
        }

        public int size() {
            return this.val$s.size();
        }

        public boolean isEmpty() {
            return this.val$s.isEmpty();
        }

        public boolean contains(Object o) {
            return this.val$s.contains(o);
        }

        public Object[] toArray() {
            return this.val$s.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return this.val$s.toArray(a);
        }

        public String toString() {
            return this.val$s.toString();
        }

        public Iterator<E> iterator() {
            return this.val$s.iterator();
        }

        public boolean equals(Object o) {
            return this.val$s.equals(o);
        }

        public int hashCode() {
            return this.val$s.hashCode();
        }

        public void clear() {
            this.val$s.clear();
        }

        public boolean remove(Object o) {
            return this.val$s.remove(o);
        }

        public boolean containsAll(Collection<?> coll) {
            return this.val$s.containsAll(coll);
        }

        public boolean removeAll(Collection<?> coll) {
            return this.val$s.removeAll(coll);
        }

        public boolean retainAll(Collection<?> coll) {
            return this.val$s.retainAll(coll);
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }
    }

    private static class BufferCache {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private ByteBuffer[] buffers;
        private int count;
        private int start;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.Util.BufferCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.Util.BufferCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Util.BufferCache.<clinit>():void");
        }

        private int next(int i) {
            return (i + 1) % Util.TEMP_BUF_POOL_SIZE;
        }

        BufferCache() {
            this.buffers = new ByteBuffer[Util.TEMP_BUF_POOL_SIZE];
        }

        ByteBuffer get(int size) {
            if (this.count == 0) {
                return null;
            }
            ByteBuffer[] buffers = this.buffers;
            ByteBuffer byteBuffer = buffers[this.start];
            if (byteBuffer.capacity() < size) {
                ByteBuffer bb;
                byteBuffer = null;
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
                byteBuffer = bb;
                if (byteBuffer == null) {
                    return null;
                }
                buffers[i] = buffers[this.start];
            }
            buffers[this.start] = null;
            this.start = next(this.start);
            this.count--;
            byteBuffer.rewind();
            byteBuffer.limit(size);
            return byteBuffer;
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
            Object obj = null;
            if (!-assertionsDisabled) {
                if (this.count > 0) {
                    obj = 1;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            ByteBuffer buf = this.buffers[this.start];
            this.buffers[this.start] = null;
            this.start = next(this.start);
            this.count--;
            return buf;
        }
    }

    private static class SelectorWrapper {
        private Selector sel;

        private static class Closer implements Runnable {
            private Selector sel;

            /* synthetic */ Closer(Selector sel, Closer closer) {
                this(sel);
            }

            private Closer(Selector sel) {
                this.sel = sel;
            }

            public void run() {
                try {
                    this.sel.close();
                } catch (Throwable th) {
                    Error error = new Error(th);
                }
            }
        }

        /* synthetic */ SelectorWrapper(Selector sel, SelectorWrapper selectorWrapper) {
            this(sel);
        }

        private SelectorWrapper(Selector sel) {
            this.sel = sel;
            Cleaner.create(this, new Closer(sel, null));
        }

        public Selector get() {
            return this.sel;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.Util.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.Util.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.Util.<clinit>():void");
    }

    Util() {
    }

    static ByteBuffer getTemporaryDirectBuffer(int size) {
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

    static void releaseTemporaryDirectBuffer(ByteBuffer buf) {
        offerFirstTemporaryDirectBuffer(buf);
    }

    static void offerFirstTemporaryDirectBuffer(ByteBuffer buf) {
        if (!-assertionsDisabled) {
            if ((buf != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!((BufferCache) bufferCache.get()).offerFirst(buf)) {
            free(buf);
        }
    }

    static void offerLastTemporaryDirectBuffer(ByteBuffer buf) {
        if (!-assertionsDisabled) {
            if ((buf != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!((BufferCache) bufferCache.get()).offerLast(buf)) {
            free(buf);
        }
    }

    private static void free(ByteBuffer buf) {
        Cleaner cleaner = ((DirectBuffer) buf).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    static Selector getTemporarySelector(SelectableChannel sc) throws IOException {
        SelectorWrapper selWrapper;
        Selector sel;
        SoftReference<SelectorWrapper> ref = (SoftReference) localSelector.get();
        if (ref != null) {
            selWrapper = (SelectorWrapper) ref.get();
            if (selWrapper != null) {
                sel = selWrapper.get();
                if (sel != null) {
                    if (sel.provider() != sc.provider()) {
                    }
                    localSelectorWrapper.set(selWrapper);
                    return sel;
                }
            }
        }
        sel = sc.provider().openSelector();
        selWrapper = new SelectorWrapper(sel, null);
        localSelector.set(new SoftReference(selWrapper));
        localSelectorWrapper.set(selWrapper);
        return sel;
    }

    static void releaseTemporarySelector(Selector sel) throws IOException {
        sel.selectNow();
        if (-assertionsDisabled || sel.keys().isEmpty()) {
            localSelectorWrapper.set(null);
            return;
        }
        throw new AssertionError((Object) "Temporary selector not empty");
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

    static <E> Set<E> ungrowableSet(Set<E> s) {
        return new AnonymousClass2(s);
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
