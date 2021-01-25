package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMesh extends CoreResource {
    private transient long agpCptrCoreMesh;
    private final Object delLock = new Object();

    CoreMesh(long j, boolean z) {
        super(CoreJni.classUpcastCoreMesh(j), z);
        this.agpCptrCoreMesh = j;
    }

    static long getCptr(CoreMesh coreMesh) {
        if (coreMesh == null) {
            return 0;
        }
        return coreMesh.agpCptrCoreMesh;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreResource
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMesh != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreMesh = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
            super.delete();
        }
    }

    static long getCptrAndSetMemOwn(CoreMesh coreMesh, boolean z) {
        if (coreMesh != null) {
            coreMesh.isAgpCmemOwn = z;
        }
        return getCptr(coreMesh);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshDesc getDesc() {
        return new CoreMeshDesc(CoreJni.getDescInCoreMesh(this.agpCptrCoreMesh, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDescArrayView getPrimitives() {
        return new CoreMeshPrimitiveDescArrayView(CoreJni.getPrimitivesInCoreMesh(this.agpCptrCoreMesh, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getJointBounds() {
        return new CoreFloatArrayView(CoreJni.getJointBoundsInCoreMesh(this.agpCptrCoreMesh, this), true);
    }
}
