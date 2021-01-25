package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMeshCreateInfo {
    private transient long agpCptrCoreMeshCreateInfo;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMeshCreateInfo(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreMeshCreateInfo = j;
    }

    static long getCptr(CoreMeshCreateInfo coreMeshCreateInfo) {
        if (coreMeshCreateInfo == null) {
            return 0;
        }
        return coreMeshCreateInfo.agpCptrCoreMeshCreateInfo;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMeshCreateInfo != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo);
                }
                this.agpCptrCoreMeshCreateInfo = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMeshCreateInfo coreMeshCreateInfo, boolean z) {
        if (coreMeshCreateInfo != null) {
            synchronized (coreMeshCreateInfo.delLock) {
                coreMeshCreateInfo.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMeshCreateInfo);
    }

    /* access modifiers changed from: package-private */
    public void setVertexCount(long j) {
        CoreJni.setVarvertexCountCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexCount() {
        return CoreJni.getVarvertexCountCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexData(CoreByteArrayView coreByteArrayView) {
        CoreJni.setVarvertexDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getVertexData() {
        long varvertexDataCoreMeshCreateInfo = CoreJni.getVarvertexDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
        if (varvertexDataCoreMeshCreateInfo == 0) {
            return null;
        }
        return new CoreByteArrayView(varvertexDataCoreMeshCreateInfo, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointData(CoreByteArrayView coreByteArrayView) {
        CoreJni.setVarjointDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getJointData() {
        long varjointDataCoreMeshCreateInfo = CoreJni.getVarjointDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
        if (varjointDataCoreMeshCreateInfo == 0) {
            return null;
        }
        return new CoreByteArrayView(varjointDataCoreMeshCreateInfo, false);
    }

    /* access modifiers changed from: package-private */
    public void setTargetData(CoreByteArrayView coreByteArrayView) {
        CoreJni.setVartargetDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getTargetData() {
        long vartargetDataCoreMeshCreateInfo = CoreJni.getVartargetDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
        if (vartargetDataCoreMeshCreateInfo == 0) {
            return null;
        }
        return new CoreByteArrayView(vartargetDataCoreMeshCreateInfo, false);
    }

    /* access modifiers changed from: package-private */
    public void setIndexCount(long j) {
        CoreJni.setVarindexCountCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getIndexCount() {
        return CoreJni.getVarindexCountCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexData(CoreByteArrayView coreByteArrayView) {
        CoreJni.setVarindexDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getIndexData() {
        long varindexDataCoreMeshCreateInfo = CoreJni.getVarindexDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
        if (varindexDataCoreMeshCreateInfo == 0) {
            return null;
        }
        return new CoreByteArrayView(varindexDataCoreMeshCreateInfo, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointBoundsData(CoreFloatArrayView coreFloatArrayView) {
        CoreJni.setVarjointBoundsDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, CoreFloatArrayView.getCptr(coreFloatArrayView), coreFloatArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getJointBoundsData() {
        long varjointBoundsDataCoreMeshCreateInfo = CoreJni.getVarjointBoundsDataCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
        if (varjointBoundsDataCoreMeshCreateInfo == 0) {
            return null;
        }
        return new CoreFloatArrayView(varjointBoundsDataCoreMeshCreateInfo, false);
    }

    /* access modifiers changed from: package-private */
    public void setPrimitives(CoreMeshPrimitiveDescArrayView coreMeshPrimitiveDescArrayView) {
        CoreJni.setVarprimitivesCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this, CoreMeshPrimitiveDescArrayView.getCptr(coreMeshPrimitiveDescArrayView), coreMeshPrimitiveDescArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDescArrayView getPrimitives() {
        long varprimitivesCoreMeshCreateInfo = CoreJni.getVarprimitivesCoreMeshCreateInfo(this.agpCptrCoreMeshCreateInfo, this);
        if (varprimitivesCoreMeshCreateInfo == 0) {
            return null;
        }
        return new CoreMeshPrimitiveDescArrayView(varprimitivesCoreMeshCreateInfo, false);
    }

    CoreMeshCreateInfo() {
        this(CoreJni.newCoreMeshCreateInfo(), true);
    }
}
