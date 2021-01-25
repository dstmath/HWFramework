package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMeshPrimitiveDesc {
    static final long INVALID_BUFFER_BYTE_SIZE = 2147483647L;
    static final long INVALID_BUFFER_OFFSET = 2147483647L;
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMeshPrimitiveDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshPrimitiveDesc obj) {
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
                CoreJni.deleteCoreMeshPrimitiveDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setIndexType(CoreIndexType value) {
        CoreJni.setVarindexTypeCoreMeshPrimitiveDesc(this.agpCptr, this, value.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreIndexType getIndexType() {
        return CoreIndexType.swigToEnum(CoreJni.getVarindexTypeCoreMeshPrimitiveDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setIndexOffset(long value) {
        CoreJni.setVarindexOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getIndexOffset() {
        return CoreJni.getVarindexOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexCount(long value) {
        CoreJni.setVarindexCountCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getIndexCount() {
        return CoreJni.getVarindexCountCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexByteSize(long value) {
        CoreJni.setVarindexByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getIndexByteSize() {
        return CoreJni.getVarindexByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setInstanceCount(long value) {
        CoreJni.setVarinstanceCountCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getInstanceCount() {
        return CoreJni.getVarinstanceCountCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexPositionOffset(long value) {
        CoreJni.setVarvertexPositionOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexPositionOffset() {
        return CoreJni.getVarvertexPositionOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexPositionByteSize(long value) {
        CoreJni.setVarvertexPositionByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexPositionByteSize() {
        return CoreJni.getVarvertexPositionByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexNormalOffset(long value) {
        CoreJni.setVarvertexNormalOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexNormalOffset() {
        return CoreJni.getVarvertexNormalOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexNormalByteSize(long value) {
        CoreJni.setVarvertexNormalByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexNormalByteSize() {
        return CoreJni.getVarvertexNormalByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexUvOffset(long value) {
        CoreJni.setVarvertexUvOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexUvOffset() {
        return CoreJni.getVarvertexUvOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexUvByteSize(long value) {
        CoreJni.setVarvertexUvByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexUvByteSize() {
        return CoreJni.getVarvertexUvByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexTangentOffset(long value) {
        CoreJni.setVarvertexTangentOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexTangentOffset() {
        return CoreJni.getVarvertexTangentOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexTangentByteSize(long value) {
        CoreJni.setVarvertexTangentByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexTangentByteSize() {
        return CoreJni.getVarvertexTangentByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexColorOffset(long value) {
        CoreJni.setVarvertexColorOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexColorOffset() {
        return CoreJni.getVarvertexColorOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexColorByteSize(long value) {
        CoreJni.setVarvertexColorByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexColorByteSize() {
        return CoreJni.getVarvertexColorByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointIndexOffset(long value) {
        CoreJni.setVarvertexJointIndexOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointIndexOffset() {
        return CoreJni.getVarvertexJointIndexOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointIndexByteSize(long value) {
        CoreJni.setVarvertexJointIndexByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointIndexByteSize() {
        return CoreJni.getVarvertexJointIndexByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointWeightOffset(long value) {
        CoreJni.setVarvertexJointWeightOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointWeightOffset() {
        return CoreJni.getVarvertexJointWeightOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointWeightByteSize(long value) {
        CoreJni.setVarvertexJointWeightByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointWeightByteSize() {
        return CoreJni.getVarvertexJointWeightByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexCount(long value) {
        CoreJni.setVarvertexCountCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getVertexCount() {
        return CoreJni.getVarvertexCountCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetOffset(long value) {
        CoreJni.setVarmorphTargetOffsetCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetOffset() {
        return CoreJni.getVarmorphTargetOffsetCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetByteSize(long value) {
        CoreJni.setVarmorphTargetByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetByteSize() {
        return CoreJni.getVarmorphTargetByteSizeCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetCount(long value) {
        CoreJni.setVarmorphTargetCountCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetCount() {
        return CoreJni.getVarmorphTargetCountCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMin(CoreVec3 value) {
        CoreJni.setVaraabbMinCoreMeshPrimitiveDesc(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMin() {
        long cptr = CoreJni.getVaraabbMinCoreMeshPrimitiveDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMax(CoreVec3 value) {
        CoreJni.setVaraabbMaxCoreMeshPrimitiveDesc(this.agpCptr, this, CoreVec3.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMax() {
        long cptr = CoreJni.getVaraabbMaxCoreMeshPrimitiveDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreVec3(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterial(CoreResourceHandle value) {
        CoreJni.setVarmaterialCoreMeshPrimitiveDesc(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMaterial() {
        long cptr = CoreJni.getVarmaterialCoreMeshPrimitiveDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setFlags(long value) {
        CoreJni.setVarflagsCoreMeshPrimitiveDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getFlags() {
        return CoreJni.getVarflagsCoreMeshPrimitiveDesc(this.agpCptr, this);
    }

    CoreMeshPrimitiveDesc() {
        this(CoreJni.newCoreMeshPrimitiveDesc(), true);
    }
}
