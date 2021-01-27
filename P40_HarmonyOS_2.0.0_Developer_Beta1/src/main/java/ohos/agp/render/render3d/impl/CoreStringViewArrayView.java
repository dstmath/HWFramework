package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreStringViewArrayView {
    private transient long agpCptr;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreStringViewArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreStringViewArrayView coreStringViewArrayView) {
        if (coreStringViewArrayView == null) {
            return 0;
        }
        return coreStringViewArrayView.agpCptr;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptr != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreStringViewArrayView(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreStringViewArrayView coreStringViewArrayView, boolean z) {
        if (coreStringViewArrayView != null) {
            synchronized (coreStringViewArrayView.delLock) {
                coreStringViewArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreStringViewArrayView);
    }

    CoreStringViewArrayView(Buffer buffer) {
        this(CoreJni.newCoreStringViewArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreStringViewArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public String get(long j) {
        return CoreJni.getInCoreStringViewArrayView(this.agpCptr, this, j);
    }
}
