package sun.nio.ch;

import java.nio.ByteBuffer;
import sun.misc.Cleaner;

class IOVecWrapper {
    private static final int BASE_OFFSET = 0;
    private static final int LEN_OFFSET = addressSize;
    private static final int SIZE_IOVEC = ((short) (addressSize * 2));
    static int addressSize = Util.unsafe().addressSize();
    private static final ThreadLocal<IOVecWrapper> cached = new ThreadLocal();
    final long address = this.vecArray.address();
    private final ByteBuffer[] buf;
    private final int[] position;
    private final int[] remaining;
    private final ByteBuffer[] shadow;
    private final int size;
    private final AllocatedNativeObject vecArray;

    private static class Deallocator implements Runnable {
        private final AllocatedNativeObject obj;

        Deallocator(AllocatedNativeObject obj) {
            this.obj = obj;
        }

        public void run() {
            this.obj.free();
        }
    }

    private IOVecWrapper(int size) {
        this.size = size;
        this.buf = new ByteBuffer[size];
        this.position = new int[size];
        this.remaining = new int[size];
        this.shadow = new ByteBuffer[size];
        this.vecArray = new AllocatedNativeObject(SIZE_IOVEC * size, false);
    }

    static IOVecWrapper get(int size) {
        IOVecWrapper wrapper = (IOVecWrapper) cached.get();
        if (wrapper != null && wrapper.size < size) {
            wrapper.vecArray.free();
            wrapper = null;
        }
        if (wrapper != null) {
            return wrapper;
        }
        wrapper = new IOVecWrapper(size);
        Cleaner.create(wrapper, new Deallocator(wrapper.vecArray));
        cached.set(wrapper);
        return wrapper;
    }

    void setBuffer(int i, ByteBuffer buf, int pos, int rem) {
        this.buf[i] = buf;
        this.position[i] = pos;
        this.remaining[i] = rem;
    }

    void setShadow(int i, ByteBuffer buf) {
        this.shadow[i] = buf;
    }

    ByteBuffer getBuffer(int i) {
        return this.buf[i];
    }

    int getPosition(int i) {
        return this.position[i];
    }

    int getRemaining(int i) {
        return this.remaining[i];
    }

    ByteBuffer getShadow(int i) {
        return this.shadow[i];
    }

    void clearRefs(int i) {
        this.buf[i] = null;
        this.shadow[i] = null;
    }

    void putBase(int i, long base) {
        int offset = (SIZE_IOVEC * i) + 0;
        if (addressSize == 4) {
            this.vecArray.putInt(offset, (int) base);
        } else {
            this.vecArray.putLong(offset, base);
        }
    }

    void putLen(int i, long len) {
        int offset = (SIZE_IOVEC * i) + LEN_OFFSET;
        if (addressSize == 4) {
            this.vecArray.putInt(offset, (int) len);
        } else {
            this.vecArray.putLong(offset, len);
        }
    }
}
