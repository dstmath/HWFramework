package sun.nio.ch;

import java.nio.ByteBuffer;
import sun.misc.Cleaner;

class IOVecWrapper {
    private static final int BASE_OFFSET = 0;
    private static final int LEN_OFFSET = addressSize;
    private static final int SIZE_IOVEC = ((short) (addressSize * 2));
    static int addressSize = Util.unsafe().addressSize();
    private static final ThreadLocal<IOVecWrapper> cached = new ThreadLocal<>();
    final long address = this.vecArray.address();
    private final ByteBuffer[] buf;
    private final int[] position;
    private final int[] remaining;
    private final ByteBuffer[] shadow;
    private final int size;
    private final AllocatedNativeObject vecArray;

    private static class Deallocator implements Runnable {
        private final AllocatedNativeObject obj;

        Deallocator(AllocatedNativeObject obj2) {
            this.obj = obj2;
        }

        public void run() {
            this.obj.free();
        }
    }

    private IOVecWrapper(int size2) {
        this.size = size2;
        this.buf = new ByteBuffer[size2];
        this.position = new int[size2];
        this.remaining = new int[size2];
        this.shadow = new ByteBuffer[size2];
        this.vecArray = new AllocatedNativeObject(SIZE_IOVEC * size2, false);
    }

    static IOVecWrapper get(int size2) {
        IOVecWrapper wrapper = cached.get();
        if (wrapper != null && wrapper.size < size2) {
            wrapper.vecArray.free();
            wrapper = null;
        }
        if (wrapper != null) {
            return wrapper;
        }
        IOVecWrapper wrapper2 = new IOVecWrapper(size2);
        Cleaner.create(wrapper2, new Deallocator(wrapper2.vecArray));
        cached.set(wrapper2);
        return wrapper2;
    }

    /* access modifiers changed from: package-private */
    public void setBuffer(int i, ByteBuffer buf2, int pos, int rem) {
        this.buf[i] = buf2;
        this.position[i] = pos;
        this.remaining[i] = rem;
    }

    /* access modifiers changed from: package-private */
    public void setShadow(int i, ByteBuffer buf2) {
        this.shadow[i] = buf2;
    }

    /* access modifiers changed from: package-private */
    public ByteBuffer getBuffer(int i) {
        return this.buf[i];
    }

    /* access modifiers changed from: package-private */
    public int getPosition(int i) {
        return this.position[i];
    }

    /* access modifiers changed from: package-private */
    public int getRemaining(int i) {
        return this.remaining[i];
    }

    /* access modifiers changed from: package-private */
    public ByteBuffer getShadow(int i) {
        return this.shadow[i];
    }

    /* access modifiers changed from: package-private */
    public void clearRefs(int i) {
        this.buf[i] = null;
        this.shadow[i] = null;
    }

    /* access modifiers changed from: package-private */
    public void putBase(int i, long base) {
        int offset = (SIZE_IOVEC * i) + 0;
        if (addressSize == 4) {
            this.vecArray.putInt(offset, (int) base);
        } else {
            this.vecArray.putLong(offset, base);
        }
    }

    /* access modifiers changed from: package-private */
    public void putLen(int i, long len) {
        int offset = (SIZE_IOVEC * i) + LEN_OFFSET;
        if (addressSize == 4) {
            this.vecArray.putInt(offset, (int) len);
        } else {
            this.vecArray.putLong(offset, len);
        }
    }
}
