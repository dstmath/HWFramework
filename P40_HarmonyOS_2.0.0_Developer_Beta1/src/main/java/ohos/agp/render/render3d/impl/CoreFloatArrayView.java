package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

class CoreFloatArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreFloatArrayView(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreFloatArrayView coreFloatArrayView) {
        if (coreFloatArrayView == null) {
            return 0;
        }
        return coreFloatArrayView.agpCptr;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreFloatArrayView(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreFloatArrayView coreFloatArrayView, boolean z) {
        if (coreFloatArrayView != null) {
            synchronized (coreFloatArrayView.lock) {
                coreFloatArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreFloatArrayView);
    }

    CoreFloatArrayView(Buffer buffer) {
        this(CoreJni.newCoreFloatArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreFloatArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public float get(long j) {
        return CoreJni.getInCoreFloatArrayView(this.agpCptr, this, j);
    }
}
