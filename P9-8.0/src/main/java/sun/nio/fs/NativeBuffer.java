package sun.nio.fs;

import sun.misc.Cleaner;
import sun.misc.Unsafe;

class NativeBuffer {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long address;
    private final Cleaner cleaner = Cleaner.create(this, new Deallocator(this.address));
    private Object owner;
    private final int size;

    private static class Deallocator implements Runnable {
        private final long address;

        Deallocator(long address) {
            this.address = address;
        }

        public void run() {
            NativeBuffer.unsafe.freeMemory(this.address);
        }
    }

    NativeBuffer(int size) {
        this.address = unsafe.allocateMemory((long) size);
        this.size = size;
    }

    void release() {
        NativeBuffers.releaseNativeBuffer(this);
    }

    long address() {
        return this.address;
    }

    int size() {
        return this.size;
    }

    Cleaner cleaner() {
        return this.cleaner;
    }

    void setOwner(Object owner) {
        this.owner = owner;
    }

    Object owner() {
        return this.owner;
    }
}
