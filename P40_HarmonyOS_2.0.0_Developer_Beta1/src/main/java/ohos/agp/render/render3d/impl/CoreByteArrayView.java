package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreByteArrayView {
    private transient long agpCptrCoreByteArrayView;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreByteArrayView(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreByteArrayView = j;
    }

    static long getCptr(CoreByteArrayView coreByteArrayView) {
        if (coreByteArrayView == null) {
            return 0;
        }
        return coreByteArrayView.agpCptrCoreByteArrayView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreByteArrayView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreByteArrayView(this.agpCptrCoreByteArrayView);
                }
                this.agpCptrCoreByteArrayView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreByteArrayView coreByteArrayView, boolean z) {
        if (coreByteArrayView != null) {
            synchronized (coreByteArrayView.lock) {
                coreByteArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreByteArrayView);
    }

    CoreByteArrayView(Buffer buffer) {
        this(CoreJni.newCoreByteArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreByteArrayView(this.agpCptrCoreByteArrayView, this);
    }

    /* access modifiers changed from: package-private */
    public short get(long j) {
        return CoreJni.getInCoreByteArrayView(this.agpCptrCoreByteArrayView, this, j);
    }
}
