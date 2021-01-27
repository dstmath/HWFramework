package ohos.agp.render.render3d.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreMeshPrimitiveDescArrayView {
    private transient long agpCptrMeshPrimitiveDescArrView;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMeshPrimitiveDescArrayView(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrMeshPrimitiveDescArrView = j;
    }

    static long getCptr(CoreMeshPrimitiveDescArrayView coreMeshPrimitiveDescArrayView) {
        if (coreMeshPrimitiveDescArrayView == null) {
            return 0;
        }
        return coreMeshPrimitiveDescArrayView.agpCptrMeshPrimitiveDescArrView;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrMeshPrimitiveDescArrView != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMeshPrimitiveDescArrayView(this.agpCptrMeshPrimitiveDescArrView);
                }
                this.agpCptrMeshPrimitiveDescArrView = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMeshPrimitiveDescArrayView coreMeshPrimitiveDescArrayView, boolean z) {
        if (coreMeshPrimitiveDescArrayView != null) {
            synchronized (coreMeshPrimitiveDescArrayView.delLock) {
                coreMeshPrimitiveDescArrayView.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMeshPrimitiveDescArrayView);
    }

    CoreMeshPrimitiveDescArrayView(Buffer buffer) {
        this(CoreJni.newCoreMeshPrimitiveDescArrayView(buffer), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreMeshPrimitiveDescArrayView(this.agpCptrMeshPrimitiveDescArrView, this);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDesc get(long j) {
        return new CoreMeshPrimitiveDesc(CoreJni.getInCoreMeshPrimitiveDescArrayView(this.agpCptrMeshPrimitiveDescArrView, this, j), true);
    }
}
