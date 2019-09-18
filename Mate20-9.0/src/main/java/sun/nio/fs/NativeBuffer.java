package sun.nio.fs;

import sun.misc.Cleaner;
import sun.misc.Unsafe;

class NativeBuffer {
    /* access modifiers changed from: private */
    public static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long address;
    private final Cleaner cleaner = Cleaner.create(this, new Deallocator(this.address));
    private Object owner;
    private final int size;

    private static class Deallocator implements Runnable {
        private final long address;

        Deallocator(long address2) {
            this.address = address2;
        }

        public void run() {
            NativeBuffer.unsafe.freeMemory(this.address);
        }
    }

    NativeBuffer(int size2) {
        this.address = unsafe.allocateMemory((long) size2);
        this.size = size2;
    }

    /* access modifiers changed from: package-private */
    public void release() {
        NativeBuffers.releaseNativeBuffer(this);
    }

    /* access modifiers changed from: package-private */
    public long address() {
        return this.address;
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.size;
    }

    /* access modifiers changed from: package-private */
    public Cleaner cleaner() {
        return this.cleaner;
    }

    /* access modifiers changed from: package-private */
    public void setOwner(Object owner2) {
        this.owner = owner2;
    }

    /* access modifiers changed from: package-private */
    public Object owner() {
        return this.owner;
    }
}
