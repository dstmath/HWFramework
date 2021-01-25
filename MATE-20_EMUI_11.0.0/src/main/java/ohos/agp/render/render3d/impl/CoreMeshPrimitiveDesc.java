package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMeshPrimitiveDesc {
    static final long INVALID_BUFFER_BYTE_SIZE = 2147483647L;
    static final long INVALID_BUFFER_OFFSET = 2147483647L;
    private transient long agpCptrMeshPrimitiveDesc;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMeshPrimitiveDesc(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrMeshPrimitiveDesc = j;
    }

    static long getCptr(CoreMeshPrimitiveDesc coreMeshPrimitiveDesc) {
        if (coreMeshPrimitiveDesc == null) {
            return 0;
        }
        return coreMeshPrimitiveDesc.agpCptrMeshPrimitiveDesc;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrMeshPrimitiveDesc != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc);
                }
                this.agpCptrMeshPrimitiveDesc = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMeshPrimitiveDesc coreMeshPrimitiveDesc, boolean z) {
        if (coreMeshPrimitiveDesc != null) {
            synchronized (coreMeshPrimitiveDesc.delLock) {
                coreMeshPrimitiveDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMeshPrimitiveDesc);
    }

    /* access modifiers changed from: package-private */
    public void setIndexType(CoreIndexType coreIndexType) {
        CoreJni.setVarindexTypeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, coreIndexType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreIndexType getIndexType() {
        return CoreIndexType.swigToEnum(CoreJni.getVarindexTypeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this));
    }

    /* access modifiers changed from: package-private */
    public void setIndexOffset(long j) {
        CoreJni.setVarindexOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getIndexOffset() {
        return CoreJni.getVarindexOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexCount(long j) {
        CoreJni.setVarindexCountCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getIndexCount() {
        return CoreJni.getVarindexCountCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setIndexByteSize(long j) {
        CoreJni.setVarindexByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getIndexByteSize() {
        return CoreJni.getVarindexByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexPositionOffset(long j) {
        CoreJni.setVarvertexPositionOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexPositionOffset() {
        return CoreJni.getVarvertexPositionOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexPositionByteSize(long j) {
        CoreJni.setVarvertexPositionByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexPositionByteSize() {
        return CoreJni.getVarvertexPositionByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexNormalOffset(long j) {
        CoreJni.setVarvertexNormalOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexNormalOffset() {
        return CoreJni.getVarvertexNormalOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexNormalByteSize(long j) {
        CoreJni.setVarvertexNormalByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexNormalByteSize() {
        return CoreJni.getVarvertexNormalByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexUvOffset(long j) {
        CoreJni.setVarvertexUvOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexUvOffset() {
        return CoreJni.getVarvertexUvOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexUvByteSize(long j) {
        CoreJni.setVarvertexUvByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexUvByteSize() {
        return CoreJni.getVarvertexUvByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexTangentOffset(long j) {
        CoreJni.setVarvertexTangentOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexTangentOffset() {
        return CoreJni.getVarvertexTangentOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexTangentByteSize(long j) {
        CoreJni.setVarvertexTangentByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexTangentByteSize() {
        return CoreJni.getVarvertexTangentByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexColorOffset(long j) {
        CoreJni.setVarvertexColorOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexColorOffset() {
        return CoreJni.getVarvertexColorOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexColorByteSize(long j) {
        CoreJni.setVarvertexColorByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexColorByteSize() {
        return CoreJni.getVarvertexColorByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointIndexOffset(long j) {
        CoreJni.setVarvertexJointIndexOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointIndexOffset() {
        return CoreJni.getVarvertexJointIndexOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointIndexByteSize(long j) {
        CoreJni.setVarvertexJointIndexByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointIndexByteSize() {
        return CoreJni.getVarvertexJointIndexByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointWeightOffset(long j) {
        CoreJni.setVarvertexJointWeightOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointWeightOffset() {
        return CoreJni.getVarvertexJointWeightOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexJointWeightByteSize(long j) {
        CoreJni.setVarvertexJointWeightByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexJointWeightByteSize() {
        return CoreJni.getVarvertexJointWeightByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexCount(long j) {
        CoreJni.setVarvertexCountCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getVertexCount() {
        return CoreJni.getVarvertexCountCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetOffset(long j) {
        CoreJni.setVarmorphTargetOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetOffset() {
        return CoreJni.getVarmorphTargetOffsetCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetByteSize(long j) {
        CoreJni.setVarmorphTargetByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetByteSize() {
        return CoreJni.getVarmorphTargetByteSizeCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetCount(long j) {
        CoreJni.setVarmorphTargetCountCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getMorphTargetCount() {
        return CoreJni.getVarmorphTargetCountCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMin(CoreVec3 coreVec3) {
        CoreJni.setVaraabbMinCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMin() {
        long varaabbMinCoreMeshPrimitiveDesc = CoreJni.getVaraabbMinCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
        if (varaabbMinCoreMeshPrimitiveDesc == 0) {
            return null;
        }
        return new CoreVec3(varaabbMinCoreMeshPrimitiveDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setAabbMax(CoreVec3 coreVec3) {
        CoreJni.setVaraabbMaxCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 getAabbMax() {
        long varaabbMaxCoreMeshPrimitiveDesc = CoreJni.getVaraabbMaxCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
        if (varaabbMaxCoreMeshPrimitiveDesc == 0) {
            return null;
        }
        return new CoreVec3(varaabbMaxCoreMeshPrimitiveDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterial(CoreResourceHandle coreResourceHandle) {
        CoreJni.setVarmaterialCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMaterial() {
        long varmaterialCoreMeshPrimitiveDesc = CoreJni.getVarmaterialCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
        if (varmaterialCoreMeshPrimitiveDesc == 0) {
            return null;
        }
        return new CoreResourceHandle(varmaterialCoreMeshPrimitiveDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setFlags(long j) {
        CoreJni.setVarflagsCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getFlags() {
        return CoreJni.getVarflagsCoreMeshPrimitiveDesc(this.agpCptrMeshPrimitiveDesc, this);
    }

    CoreMeshPrimitiveDesc() {
        this(CoreJni.newCoreMeshPrimitiveDesc(), true);
    }
}
