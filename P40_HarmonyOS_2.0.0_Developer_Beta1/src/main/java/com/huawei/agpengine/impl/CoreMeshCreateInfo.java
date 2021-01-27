package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMeshCreateInfo {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMeshCreateInfo(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshCreateInfo obj) {
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
                CoreJni.deleteCoreMeshCreateInfo(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setVertexCount(long value) {
        CoreJni.setVarvertexCountCoreMeshCreateInfo(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexCount() {
        return CoreJni.getVarvertexCountCoreMeshCreateInfo(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexData(CoreByteArrayView value) {
        CoreJni.setVarvertexDataCoreMeshCreateInfo(this.agpCptr, this, CoreByteArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getVertexData() {
        long cptr = CoreJni.getVarvertexDataCoreMeshCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreByteArrayView(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointData(CoreByteArrayView value) {
        CoreJni.setVarjointDataCoreMeshCreateInfo(this.agpCptr, this, CoreByteArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getJointData() {
        long cptr = CoreJni.getVarjointDataCoreMeshCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreByteArrayView(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setTargetData(CoreByteArrayView value) {
        CoreJni.setVartargetDataCoreMeshCreateInfo(this.agpCptr, this, CoreByteArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getTargetData() {
        long cptr = CoreJni.getVartargetDataCoreMeshCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreByteArrayView(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setIndexCount(long value) {
        CoreJni.setVarindexCountCoreMeshCreateInfo(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getIndexCount() {
        return CoreJni.getVarindexCountCoreMeshCreateInfo(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexData(CoreByteArrayView value) {
        CoreJni.setVarindexDataCoreMeshCreateInfo(this.agpCptr, this, CoreByteArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getIndexData() {
        long cptr = CoreJni.getVarindexDataCoreMeshCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreByteArrayView(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setJointBoundsData(CoreFloatArrayView value) {
        CoreJni.setVarjointBoundsDataCoreMeshCreateInfo(this.agpCptr, this, CoreFloatArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getJointBoundsData() {
        long cptr = CoreJni.getVarjointBoundsDataCoreMeshCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreFloatArrayView(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setPrimitives(CoreMeshPrimitiveDescArrayView value) {
        CoreJni.setVarprimitivesCoreMeshCreateInfo(this.agpCptr, this, CoreMeshPrimitiveDescArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDescArrayView getPrimitives() {
        long cptr = CoreJni.getVarprimitivesCoreMeshCreateInfo(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreMeshPrimitiveDescArrayView(cptr, false);
    }

    CoreMeshCreateInfo() {
        this(CoreJni.newCoreMeshCreateInfo(), true);
    }
}
