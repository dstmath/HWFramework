package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMeshBuilder extends CoreInterface {
    private transient long agpCptr;

    CoreMeshBuilder(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreMeshBuilder(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshBuilder obj) {
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
    @Override // com.huawei.agpengine.impl.CoreInterface
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

    static class CorePrimitive {
        private transient long agpCptr;
        transient boolean isAgpCmemOwn;

        CorePrimitive(long cptr, boolean isCmemoryOwn) {
            this.isAgpCmemOwn = isCmemoryOwn;
            this.agpCptr = cptr;
        }

        static long getCptr(CorePrimitive obj) {
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
                    CoreJni.deleteCoreMeshBuilderCorePrimitive(this.agpCptr);
                }
                this.agpCptr = 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void setVertexCount(long value) {
            CoreJni.setVarvertexCountCoreMeshBuilderCorePrimitive(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getVertexCount() {
            return CoreJni.getVarvertexCountCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setIndexCount(long value) {
            CoreJni.setVarindexCountCoreMeshBuilderCorePrimitive(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getIndexCount() {
            return CoreJni.getVarindexCountCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setInstanceCount(long value) {
            CoreJni.setVarinstanceCountCoreMeshBuilderCorePrimitive(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getInstanceCount() {
            return CoreJni.getVarinstanceCountCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setMorphTargetCount(long value) {
            CoreJni.setVarmorphTargetCountCoreMeshBuilderCorePrimitive(this.agpCptr, this, value);
        }

        /* access modifiers changed from: package-private */
        public long getMorphTargetCount() {
            return CoreJni.getVarmorphTargetCountCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setIndexType(CoreIndexType value) {
            CoreJni.setVarindexTypeCoreMeshBuilderCorePrimitive(this.agpCptr, this, value.swigValue());
        }

        /* access modifiers changed from: package-private */
        public CoreIndexType getIndexType() {
            return CoreIndexType.swigToEnum(CoreJni.getVarindexTypeCoreMeshBuilderCorePrimitive(this.agpCptr, this));
        }

        /* access modifiers changed from: package-private */
        public void setMaterial(CoreResourceHandle value) {
            CoreJni.setVarmaterialCoreMeshBuilderCorePrimitive(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getMaterial() {
            long cptr = CoreJni.getVarmaterialCoreMeshBuilderCorePrimitive(this.agpCptr, this);
            if (cptr == 0) {
                return null;
            }
            return new CoreResourceHandle(cptr, false);
        }

        /* access modifiers changed from: package-private */
        public void setTangents(boolean isEnabled) {
            CoreJni.setVartangentsCoreMeshBuilderCorePrimitive(this.agpCptr, this, isEnabled);
        }

        /* access modifiers changed from: package-private */
        public boolean getTangents() {
            return CoreJni.getVartangentsCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setColors(boolean isEnabled) {
            CoreJni.setVarcolorsCoreMeshBuilderCorePrimitive(this.agpCptr, this, isEnabled);
        }

        /* access modifiers changed from: package-private */
        public boolean getColors() {
            return CoreJni.getVarcolorsCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        /* access modifiers changed from: package-private */
        public void setJoints(boolean isEnabled) {
            CoreJni.setVarjointsCoreMeshBuilderCorePrimitive(this.agpCptr, this, isEnabled);
        }

        /* access modifiers changed from: package-private */
        public boolean getJoints() {
            return CoreJni.getVarjointsCoreMeshBuilderCorePrimitive(this.agpCptr, this);
        }

        CorePrimitive() {
            this(CoreJni.newCorePrimitive(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void addPrimitive(CorePrimitive primitive) {
        CoreJni.addPrimitiveInCoreMeshBuilder(this.agpCptr, this, CorePrimitive.getCptr(primitive), primitive);
    }

    /* access modifiers changed from: package-private */
    public CorePrimitive getPrimitive(long index) {
        return new CorePrimitive(CoreJni.getPrimitiveInCoreMeshBuilder(this.agpCptr, this, index), false);
    }

    /* access modifiers changed from: package-private */
    public void allocate() {
        CoreJni.allocateInCoreMeshBuilder(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexData(long primitiveIndex, CoreVec3ArrayView positions, CoreVec3ArrayView normals, CoreVec2ArrayView texcoords, CoreVec4ArrayView tangents, CoreVec4ArrayView colors) {
        CoreJni.setVertexDataInCoreMeshBuilder0(this.agpCptr, this, primitiveIndex, CoreVec3ArrayView.getCptr(positions), positions, CoreVec3ArrayView.getCptr(normals), normals, CoreVec2ArrayView.getCptr(texcoords), texcoords, CoreVec4ArrayView.getCptr(tangents), tangents, CoreVec4ArrayView.getCptr(colors), colors);
    }

    /* access modifiers changed from: package-private */
    public void setVertexData(long primitiveIndex, CoreFloatArrayView positions, CoreFloatArrayView normals, CoreFloatArrayView texcoords, CoreFloatArrayView tangents, CoreFloatArrayView colors) {
        CoreJni.setVertexDataInCoreMeshBuilder1(this.agpCptr, this, primitiveIndex, CoreFloatArrayView.getCptr(positions), positions, CoreFloatArrayView.getCptr(normals), normals, CoreFloatArrayView.getCptr(texcoords), texcoords, CoreFloatArrayView.getCptr(tangents), tangents, CoreFloatArrayView.getCptr(colors), colors);
    }

    /* access modifiers changed from: package-private */
    public void setAabb(long primitiveIndex, CoreVec3 min, CoreVec3 max) {
        CoreJni.setAabbInCoreMeshBuilder(this.agpCptr, this, primitiveIndex, CoreVec3.getCptr(min), min, CoreVec3.getCptr(max), max);
    }

    /* access modifiers changed from: package-private */
    public void calculateAabb(long primitiveIndex, CoreVec3ArrayView positions) {
        CoreJni.calculateAabbInCoreMeshBuilder(this.agpCptr, this, primitiveIndex, CoreVec3ArrayView.getCptr(positions), positions);
    }

    /* access modifiers changed from: package-private */
    public void setIndexData(long primitiveIndex, CoreByteArrayView indices) {
        CoreJni.setIndexDataInCoreMeshBuilder(this.agpCptr, this, primitiveIndex, CoreByteArrayView.getCptr(indices), indices);
    }

    /* access modifiers changed from: package-private */
    public void setJointData(long primitiveIndex, CoreByteArrayView jointData, CoreVec4ArrayView weightData, CoreVec3ArrayView vertexPositions) {
        CoreJni.setJointDataInCoreMeshBuilder(this.agpCptr, this, primitiveIndex, CoreByteArrayView.getCptr(jointData), jointData, CoreVec4ArrayView.getCptr(weightData), weightData, CoreVec3ArrayView.getCptr(vertexPositions), vertexPositions);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetData(long primitiveIndex, CoreVec3ArrayView basePositions, CoreVec3ArrayView baseNormals, CoreVec4ArrayView baseTangents, CoreVec3ArrayView targetPositions, CoreVec3ArrayView targetNormals, CoreVec3ArrayView targetTangents) {
        CoreJni.setMorphTargetDataInCoreMeshBuilder(this.agpCptr, this, primitiveIndex, CoreVec3ArrayView.getCptr(basePositions), basePositions, CoreVec3ArrayView.getCptr(baseNormals), baseNormals, CoreVec4ArrayView.getCptr(baseTangents), baseTangents, CoreVec3ArrayView.getCptr(targetPositions), targetPositions, CoreVec3ArrayView.getCptr(targetNormals), targetNormals, CoreVec3ArrayView.getCptr(targetTangents), targetTangents);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getVertexData() {
        return new CoreByteArrayView(CoreJni.getVertexDataInCoreMeshBuilder(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getIndexData() {
        return new CoreByteArrayView(CoreJni.getIndexDataInCoreMeshBuilder(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getJointData() {
        return new CoreByteArrayView(CoreJni.getJointDataInCoreMeshBuilder(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getMorphTargetData() {
        return new CoreByteArrayView(CoreJni.getMorphTargetDataInCoreMeshBuilder(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getJointBoundsData() {
        return new CoreFloatArrayView(CoreJni.getJointBoundsDataInCoreMeshBuilder(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDescArrayView getPrimitives() {
        return new CoreMeshPrimitiveDescArrayView(CoreJni.getPrimitivesInCoreMeshBuilder(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public long getVertexCount() {
        return CoreJni.getVertexCountInCoreMeshBuilder(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public long getIndexCount() {
        return CoreJni.getIndexCountInCoreMeshBuilder(this.agpCptr, this);
    }

    static void free(CoreMeshBuilder v) {
        CoreJni.freeInCoreMeshBuilder(getCptr(v), v);
    }
}
