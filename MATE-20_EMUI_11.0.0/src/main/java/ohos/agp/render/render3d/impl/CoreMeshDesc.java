package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMeshDesc {
    private transient long agpCptrCoreMeshDesc;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMeshDesc(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreMeshDesc = j;
    }

    static long getCptr(CoreMeshDesc coreMeshDesc) {
        if (coreMeshDesc == null) {
            return 0;
        }
        return coreMeshDesc.agpCptrCoreMeshDesc;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreMeshDesc != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMeshDesc(this.agpCptrCoreMeshDesc);
                }
                this.agpCptrCoreMeshDesc = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMeshDesc coreMeshDesc, boolean z) {
        if (coreMeshDesc != null) {
            synchronized (coreMeshDesc.delLock) {
                coreMeshDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMeshDesc);
    }

    /* access modifiers changed from: package-private */
    public void setVertexBuffer(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarvertexBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getVertexBuffer() {
        long varvertexBufferCoreMeshDesc = CoreJni.getVarvertexBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varvertexBufferCoreMeshDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varvertexBufferCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setIndexBuffer(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarindexBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getIndexBuffer() {
        long varindexBufferCoreMeshDesc = CoreJni.getVarindexBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varindexBufferCoreMeshDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varindexBufferCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointAttributeBuffer(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarjointAttributeBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getJointAttributeBuffer() {
        long varjointAttributeBufferCoreMeshDesc = CoreJni.getVarjointAttributeBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varjointAttributeBufferCoreMeshDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varjointAttributeBufferCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetBuffer(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.setVarmorphTargetBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreGpuResourceHandle getMorphTargetBuffer() {
        long varmorphTargetBufferCoreMeshDesc = CoreJni.getVarmorphTargetBufferCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varmorphTargetBufferCoreMeshDesc == 0) {
            return null;
        }
        return new CoreGpuResourceHandle(varmorphTargetBufferCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setPrimitives(CoreResourceDataHandle coreResourceDataHandle) {
        CoreJni.setVarprimitivesCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreResourceDataHandle.getCptr(coreResourceDataHandle), coreResourceDataHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getPrimitives() {
        long varprimitivesCoreMeshDesc = CoreJni.getVarprimitivesCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varprimitivesCoreMeshDesc == 0) {
            return null;
        }
        return new CoreResourceDataHandle(varprimitivesCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setPrimitiveCount(long j) {
        CoreJni.setVarprimitiveCountCoreMeshDesc(this.agpCptrCoreMeshDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getPrimitiveCount() {
        return CoreJni.getVarprimitiveCountCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setJointBounds(CoreResourceDataHandle coreResourceDataHandle) {
        CoreJni.setVarjointBoundsCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreResourceDataHandle.getCptr(coreResourceDataHandle), coreResourceDataHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getJointBounds() {
        long varjointBoundsCoreMeshDesc = CoreJni.getVarjointBoundsCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varjointBoundsCoreMeshDesc == 0) {
            return null;
        }
        return new CoreResourceDataHandle(varjointBoundsCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointBoundsCount(long j) {
        CoreJni.setVarjointBoundsCountCoreMeshDesc(this.agpCptrCoreMeshDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getJointBoundsCount() {
        return CoreJni.getVarjointBoundsCountCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMin(CoreVec3 coreVec3) {
        CoreJni.setVaraabbMinCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMin() {
        long varaabbMinCoreMeshDesc = CoreJni.getVaraabbMinCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varaabbMinCoreMeshDesc == 0) {
            return null;
        }
        return new CoreVec3(varaabbMinCoreMeshDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMax(CoreVec3 coreVec3) {
        CoreJni.setVaraabbMaxCoreMeshDesc(this.agpCptrCoreMeshDesc, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMax() {
        long varaabbMaxCoreMeshDesc = CoreJni.getVaraabbMaxCoreMeshDesc(this.agpCptrCoreMeshDesc, this);
        if (varaabbMaxCoreMeshDesc == 0) {
            return null;
        }
        return new CoreVec3(varaabbMaxCoreMeshDesc, false);
    }

    CoreMeshDesc() {
        this(CoreJni.newCoreMeshDesc(), true);
    }
}
