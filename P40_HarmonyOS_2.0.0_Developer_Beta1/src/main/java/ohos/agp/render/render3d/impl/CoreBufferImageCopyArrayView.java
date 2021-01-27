package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreBufferImageCopyArrayView {
    private transient long agpCptrCoreBufferImageCopyArrayView;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreBufferImageCopyArrayView(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreBufferImageCopyArrayView = j;
    }

    static long getCptr(CoreBufferImageCopyArrayView coreBufferImageCopyArrayView) {
        if (coreBufferImageCopyArrayView == null) {
            return 0;
        }
        return coreBufferImageCopyArrayView.agpCptrCoreBufferImageCopyArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreBufferImageCopyArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreBufferImageCopyArrayView(this.agpCptrCoreBufferImageCopyArrayView);
                }
                this.agpCptrCoreBufferImageCopyArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreBufferImageCopyArrayView coreBufferImageCopyArrayView, boolean z) {
        if (coreBufferImageCopyArrayView != null) {
            synchronized (coreBufferImageCopyArrayView.lock) {
                coreBufferImageCopyArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreBufferImageCopyArrayView);
    }

    CoreBufferImageCopyArrayView(Buffer buffer) {
        this(CoreJni.newCoreBufferImageCopyArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreBufferImageCopyArrayView(this.agpCptrCoreBufferImageCopyArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreBufferImageCopy get(long j) {
        return new CoreBufferImageCopy(CoreJni.getInCoreBufferImageCopyArrayView(this.agpCptrCoreBufferImageCopyArrayView, this, j), true);
    }
}
