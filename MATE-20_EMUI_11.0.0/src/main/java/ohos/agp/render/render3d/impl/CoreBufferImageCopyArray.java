package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreBufferImageCopyArray {
    private transient long agpCptrCoreBufferImageCopyArray;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreBufferImageCopyArray(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreBufferImageCopyArray = j;
    }

    static long getCptr(CoreBufferImageCopyArray coreBufferImageCopyArray) {
        if (coreBufferImageCopyArray == null) {
            return 0;
        }
        return coreBufferImageCopyArray.agpCptrCoreBufferImageCopyArray;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreBufferImageCopyArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreBufferImageCopyArray(this.agpCptrCoreBufferImageCopyArray);
                }
                this.agpCptrCoreBufferImageCopyArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreBufferImageCopyArray coreBufferImageCopyArray, boolean z) {
        if (coreBufferImageCopyArray != null) {
            synchronized (coreBufferImageCopyArray.lock) {
                coreBufferImageCopyArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreBufferImageCopyArray);
    }

    CoreBufferImageCopyArray(Buffer buffer) {
        this(CoreJni.newCoreBufferImageCopyArray(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreBufferImageCopyArray(this.agpCptrCoreBufferImageCopyArray, this);
    }

    /* access modifiers changed from: package-private */
    public CoreBufferImageCopy get(long j) {
        return new CoreBufferImageCopy(CoreJni.getInCoreBufferImageCopyArray(this.agpCptrCoreBufferImageCopyArray, this, j), true);
    }
}
