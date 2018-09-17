package sun.nio.ch;

class AllocatedNativeObject extends NativeObject {
    AllocatedNativeObject(int size, boolean pageAligned) {
        super(size, pageAligned);
    }

    synchronized void free() {
        if (this.allocationAddress != 0) {
            unsafe.freeMemory(this.allocationAddress);
            this.allocationAddress = 0;
        }
    }
}
