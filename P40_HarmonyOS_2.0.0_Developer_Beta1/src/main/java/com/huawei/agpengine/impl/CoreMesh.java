package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMesh extends CoreResource {
    private transient long agpCptr;

    CoreMesh(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreMesh(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMesh obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.agpengine.impl.CoreResource
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    /* access modifiers changed from: package-private */
    public CoreMeshDesc getDesc() {
        return new CoreMeshDesc(CoreJni.getDescInCoreMesh(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDescArrayView getPrimitives() {
        return new CoreMeshPrimitiveDescArrayView(CoreJni.getPrimitivesInCoreMesh(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getJointBounds() {
        return new CoreFloatArrayView(CoreJni.getJointBoundsInCoreMesh(this.agpCptr, this), true);
    }
}
