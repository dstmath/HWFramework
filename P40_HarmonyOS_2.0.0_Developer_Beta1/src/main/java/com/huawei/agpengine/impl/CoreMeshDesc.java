package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMeshDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMeshDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshDesc obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreMeshDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setVertexBuffer(long value) {
        CoreJni.setVarvertexBufferCoreMeshDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexBuffer() {
        return CoreJni.getVarvertexBufferCoreMeshDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexBuffer(long value) {
        CoreJni.setVarindexBufferCoreMeshDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getIndexBuffer() {
        return CoreJni.getVarindexBufferCoreMeshDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setJointAttributeBuffer(long value) {
        CoreJni.setVarjointAttributeBufferCoreMeshDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getJointAttributeBuffer() {
        return CoreJni.getVarjointAttributeBufferCoreMeshDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetBuffer(long value) {
        CoreJni.setVarmorphTargetBufferCoreMeshDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetBuffer() {
        return CoreJni.getVarmorphTargetBufferCoreMeshDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setPrimitives(CoreResourceDataHandle value) {
        CoreJni.setVarprimitivesCoreMeshDesc(this.agpCptr, this, CoreResourceDataHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getPrimitives() {
        long cptr = CoreJni.getVarprimitivesCoreMeshDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceDataHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setPrimitiveCount(long value) {
        CoreJni.setVarprimitiveCountCoreMeshDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getPrimitiveCount() {
        return CoreJni.getVarprimitiveCountCoreMeshDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setJointBounds(CoreResourceDataHandle value) {
        CoreJni.setVarjointBoundsCoreMeshDesc(this.agpCptr, this, CoreResourceDataHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getJointBounds() {
        long cptr = CoreJni.getVarjointBoundsCoreMeshDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceDataHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointBoundsCount(long value) {
        CoreJni.setVarjointBoundsCountCoreMeshDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getJointBoundsCount() {
        return CoreJni.getVarjointBoundsCountCoreMeshDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMin(CoreVec3 value) {
        CoreJni.setVaraabbMinCoreMeshDesc(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMin() {
        long cptr = CoreJni.getVaraabbMinCoreMeshDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMax(CoreVec3 value) {
        CoreJni.setVaraabbMaxCoreMeshDesc(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMax() {
        long cptr = CoreJni.getVaraabbMaxCoreMeshDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    CoreMeshDesc() {
        this(CoreJni.newCoreMeshDesc(), true);
    }
}
