package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreMeshBuilder {
    private transient long agpCptrMeshBuilder;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreMeshBuilder(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrMeshBuilder = j;
    }

    static long getCptr(CoreMeshBuilder coreMeshBuilder) {
        if (coreMeshBuilder == null) {
            return 0;
        }
        return coreMeshBuilder.agpCptrMeshBuilder;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrMeshBuilder != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrMeshBuilder = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMeshBuilder coreMeshBuilder, boolean z) {
        if (coreMeshBuilder != null) {
            synchronized (coreMeshBuilder.delLock) {
                coreMeshBuilder.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMeshBuilder);
    }

    static class CorePrimitive {
        private transient long agpCptrMeshBuilderPrimitive;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CorePrimitive(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrMeshBuilderPrimitive = j;
        }

        static long getCptr(CorePrimitive corePrimitive) {
            if (corePrimitive == null) {
                return 0;
            }
            return corePrimitive.agpCptrMeshBuilderPrimitive;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrMeshBuilderPrimitive != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive);
                    }
                    this.agpCptrMeshBuilderPrimitive = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CorePrimitive corePrimitive, boolean z) {
            if (corePrimitive != null) {
                synchronized (corePrimitive.delLock) {
                    corePrimitive.isAgpCmemOwn = z;
                }
            }
            return getCptr(corePrimitive);
        }

        /* access modifiers changed from: package-private */
        public void setVertexCount(long j) {
            CoreJni.setVarvertexCountCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, j);
        }

        /* access modifiers changed from: package-private */
        public long getVertexCount() {
            return CoreJni.getVarvertexCountCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
        }

        /* access modifiers changed from: package-private */
        public void setIndexCount(long j) {
            CoreJni.setVarindexCountCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, j);
        }

        /* access modifiers changed from: package-private */
        public long getIndexCount() {
            return CoreJni.getVarindexCountCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
        }

        /* access modifiers changed from: package-private */
        public void setMorphTargetCount(long j) {
            CoreJni.setVarmorphTargetCountCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, j);
        }

        /* access modifiers changed from: package-private */
        public long getMorphTargetCount() {
            return CoreJni.getVarmorphTargetCountCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
        }

        /* access modifiers changed from: package-private */
        public void setIndexType(CoreIndexType coreIndexType) {
            CoreJni.setVarindexTypeCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, coreIndexType.swigValue());
        }

        /* access modifiers changed from: package-private */
        public CoreIndexType getIndexType() {
            return CoreIndexType.swigToEnum(CoreJni.getVarindexTypeCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this));
        }

        /* access modifiers changed from: package-private */
        public void setMaterial(CoreResourceHandle coreResourceHandle) {
            CoreJni.setVarmaterialCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
        }

        /* access modifiers changed from: package-private */
        public CoreResourceHandle getMaterial() {
            long varmaterialCoreMeshBuilderCorePrimitive = CoreJni.getVarmaterialCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
            if (varmaterialCoreMeshBuilderCorePrimitive == 0) {
                return null;
            }
            return new CoreResourceHandle(varmaterialCoreMeshBuilderCorePrimitive, false);
        }

        /* access modifiers changed from: package-private */
        public void setTangents(boolean z) {
            CoreJni.setVartangentsCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, z);
        }

        /* access modifiers changed from: package-private */
        public boolean getTangents() {
            return CoreJni.getVartangentsCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
        }

        /* access modifiers changed from: package-private */
        public void setColors(boolean z) {
            CoreJni.setVarcolorsCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, z);
        }

        /* access modifiers changed from: package-private */
        public boolean getColors() {
            return CoreJni.getVarcolorsCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
        }

        /* access modifiers changed from: package-private */
        public void setJoints(boolean z) {
            CoreJni.setVarjointsCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this, z);
        }

        /* access modifiers changed from: package-private */
        public boolean getJoints() {
            return CoreJni.getVarjointsCoreMeshBuilderCorePrimitive(this.agpCptrMeshBuilderPrimitive, this);
        }

        CorePrimitive() {
            this(CoreJni.newCorePrimitive(), true);
        }
    }

    /* access modifiers changed from: package-private */
    public void addPrimitive(CorePrimitive corePrimitive) {
        CoreJni.addPrimitiveInCoreMeshBuilder(this.agpCptrMeshBuilder, this, CorePrimitive.getCptr(corePrimitive), corePrimitive);
    }

    /* access modifiers changed from: package-private */
    public CorePrimitive getPrimitive(long j) {
        return new CorePrimitive(CoreJni.getPrimitiveInCoreMeshBuilder(this.agpCptrMeshBuilder, this, j), false);
    }

    /* access modifiers changed from: package-private */
    public void allocate() {
        CoreJni.allocateInCoreMeshBuilder(this.agpCptrMeshBuilder, this);
    }

    /* access modifiers changed from: package-private */
    public void setVertexData(long j, CoreVec3ArrayView coreVec3ArrayView, CoreVec3ArrayView coreVec3ArrayView2, CoreVec2ArrayView coreVec2ArrayView, CoreVec4ArrayView coreVec4ArrayView, CoreVec4ArrayView coreVec4ArrayView2) {
        CoreJni.setVertexDataInCoreMeshBuilder0(this.agpCptrMeshBuilder, this, j, CoreVec3ArrayView.getCptr(coreVec3ArrayView), coreVec3ArrayView, CoreVec3ArrayView.getCptr(coreVec3ArrayView2), coreVec3ArrayView2, CoreVec2ArrayView.getCptr(coreVec2ArrayView), coreVec2ArrayView, CoreVec4ArrayView.getCptr(coreVec4ArrayView), coreVec4ArrayView, CoreVec4ArrayView.getCptr(coreVec4ArrayView2), coreVec4ArrayView2);
    }

    /* access modifiers changed from: package-private */
    public void setVertexData(long j, CoreFloatArrayView coreFloatArrayView, CoreFloatArrayView coreFloatArrayView2, CoreFloatArrayView coreFloatArrayView3, CoreFloatArrayView coreFloatArrayView4, CoreFloatArrayView coreFloatArrayView5) {
        CoreJni.setVertexDataInCoreMeshBuilder1(this.agpCptrMeshBuilder, this, j, CoreFloatArrayView.getCptr(coreFloatArrayView), coreFloatArrayView, CoreFloatArrayView.getCptr(coreFloatArrayView2), coreFloatArrayView2, CoreFloatArrayView.getCptr(coreFloatArrayView3), coreFloatArrayView3, CoreFloatArrayView.getCptr(coreFloatArrayView4), coreFloatArrayView4, CoreFloatArrayView.getCptr(coreFloatArrayView5), coreFloatArrayView5);
    }

    /* access modifiers changed from: package-private */
    public void setAabb(long j, CoreVec3 coreVec3, CoreVec3 coreVec32) {
        CoreJni.setAabbInCoreMeshBuilder(this.agpCptrMeshBuilder, this, j, CoreVec3.getCptr(coreVec3), coreVec3, CoreVec3.getCptr(coreVec32), coreVec32);
    }

    /* access modifiers changed from: package-private */
    public void calculateAabb(long j, CoreVec3ArrayView coreVec3ArrayView) {
        CoreJni.calculateAabbInCoreMeshBuilder(this.agpCptrMeshBuilder, this, j, CoreVec3ArrayView.getCptr(coreVec3ArrayView), coreVec3ArrayView);
    }

    /* access modifiers changed from: package-private */
    public void setIndexData(long j, CoreByteArrayView coreByteArrayView) {
        CoreJni.setIndexDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this, j, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView);
    }

    /* access modifiers changed from: package-private */
    public void setJointData(long j, CoreByteArrayView coreByteArrayView, CoreVec4ArrayView coreVec4ArrayView, CoreVec3ArrayView coreVec3ArrayView) {
        CoreJni.setJointDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this, j, CoreByteArrayView.getCptr(coreByteArrayView), coreByteArrayView, CoreVec4ArrayView.getCptr(coreVec4ArrayView), coreVec4ArrayView, CoreVec3ArrayView.getCptr(coreVec3ArrayView), coreVec3ArrayView);
    }

    /* access modifiers changed from: package-private */
    public void setMorphTargetData(long j, CoreVec3ArrayView coreVec3ArrayView, CoreVec3ArrayView coreVec3ArrayView2, CoreVec4ArrayView coreVec4ArrayView, CoreVec3ArrayView coreVec3ArrayView3, CoreVec3ArrayView coreVec3ArrayView4, CoreVec4ArrayView coreVec4ArrayView2) {
        CoreJni.setMorphTargetDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this, j, CoreVec3ArrayView.getCptr(coreVec3ArrayView), coreVec3ArrayView, CoreVec3ArrayView.getCptr(coreVec3ArrayView2), coreVec3ArrayView2, CoreVec4ArrayView.getCptr(coreVec4ArrayView), coreVec4ArrayView, CoreVec3ArrayView.getCptr(coreVec3ArrayView3), coreVec3ArrayView3, CoreVec3ArrayView.getCptr(coreVec3ArrayView4), coreVec3ArrayView4, CoreVec4ArrayView.getCptr(coreVec4ArrayView2), coreVec4ArrayView2);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getVertexData() {
        return new CoreByteArrayView(CoreJni.getVertexDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getIndexData() {
        return new CoreByteArrayView(CoreJni.getIndexDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getJointData() {
        return new CoreByteArrayView(CoreJni.getJointDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreByteArrayView getMorphTargetData() {
        return new CoreByteArrayView(CoreJni.getMorphTargetDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getJointBoundsData() {
        return new CoreFloatArrayView(CoreJni.getJointBoundsDataInCoreMeshBuilder(this.agpCptrMeshBuilder, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDescArrayView getPrimitives() {
        return new CoreMeshPrimitiveDescArrayView(CoreJni.getPrimitivesInCoreMeshBuilder(this.agpCptrMeshBuilder, this), true);
    }

    /* access modifiers changed from: package-private */
    public long getVertexCount() {
        return CoreJni.getVertexCountInCoreMeshBuilder(this.agpCptrMeshBuilder, this);
    }

    /* access modifiers changed from: package-private */
    public long getIndexCount() {
        return CoreJni.getIndexCountInCoreMeshBuilder(this.agpCptrMeshBuilder, this);
    }

    static class CoreDeleter {
        private transient long agpCptrMeshBuilderDeleter;
        private final Object delLock;
        transient boolean isAgpCmemOwn;

        CoreDeleter(long j, boolean z) {
            this.delLock = new Object();
            this.isAgpCmemOwn = z;
            this.agpCptrMeshBuilderDeleter = j;
        }

        static long getCptr(CoreDeleter coreDeleter) {
            if (coreDeleter == null) {
                return 0;
            }
            return coreDeleter.agpCptrMeshBuilderDeleter;
        }

        /* access modifiers changed from: protected */
        public void finalize() {
            delete();
        }

        /* access modifiers changed from: package-private */
        public void delete() {
            synchronized (this.delLock) {
                if (this.agpCptrMeshBuilderDeleter != 0) {
                    if (this.isAgpCmemOwn) {
                        this.isAgpCmemOwn = false;
                        CoreJni.deleteCoreMeshBuilderCoreDeleter(this.agpCptrMeshBuilderDeleter);
                    }
                    this.agpCptrMeshBuilderDeleter = 0;
                }
            }
        }

        static long getCptrAndSetMemOwn(CoreDeleter coreDeleter, boolean z) {
            if (coreDeleter != null) {
                synchronized (coreDeleter.delLock) {
                    coreDeleter.isAgpCmemOwn = z;
                }
            }
            return getCptr(coreDeleter);
        }

        CoreDeleter() {
            this(CoreJni.newCoreMeshBuilderCoreDeleter(), true);
        }
    }
}
