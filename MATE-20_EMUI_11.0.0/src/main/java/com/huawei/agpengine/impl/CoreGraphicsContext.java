package com.huawei.agpengine.impl;

class CoreGraphicsContext extends CoreInterface {
    private transient long agpCptr;

    CoreGraphicsContext(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreGraphicsContext(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGraphicsContext obj) {
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

    /* access modifiers changed from: package-private */
    public void init() {
        CoreJni.initInCoreGraphicsContext(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreEcs getEcs() {
        return new CoreEcs(CoreJni.getEcsInCoreGraphicsContext(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreator getResourceCreator() {
        return new CoreResourceCreator(CoreJni.getResourceCreatorInCoreGraphicsContext(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreEngine getEngine() {
        return new CoreEngine(CoreJni.getEngineInCoreGraphicsContext(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public long getRenderNodeGraph(CoreRenderNodeGraphType renderNodeGraphType) {
        return CoreJni.getRenderNodeGraphInCoreGraphicsContext(this.agpCptr, this, renderNodeGraphType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreSceneUtil getSceneUtil() {
        return new CoreSceneUtil(CoreJni.getSceneUtilInCoreGraphicsContext(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshUtil getMeshUtil() {
        return new CoreMeshUtil(CoreJni.getMeshUtilInCoreGraphicsContext(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreGltf2 getGltf() {
        return new CoreGltf2(CoreJni.getGltfInCoreGraphicsContext(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public void destroy() {
        CoreJni.destroyInCoreGraphicsContext(this.agpCptr, this);
    }

    enum CoreRenderNodeGraphType {
        UNDEFINED(0),
        LIGHT_WEIGHT_RENDERING_PIPELINE(1),
        LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA(2),
        HIGH_DEFINITION_RENDERING_PIPELINE(3);
        
        private final int swigValue;

        /* access modifiers changed from: package-private */
        public final int swigValue() {
            return this.swigValue;
        }

        static CoreRenderNodeGraphType swigToEnum(int swigValue2) {
            CoreRenderNodeGraphType[] swigValues = (CoreRenderNodeGraphType[]) CoreRenderNodeGraphType.class.getEnumConstants();
            if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
                return swigValues[swigValue2];
            }
            for (CoreRenderNodeGraphType swigEnum : swigValues) {
                if (swigEnum.swigValue == swigValue2) {
                    return swigEnum;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreRenderNodeGraphType.class + " with value " + swigValue2);
        }

        private CoreRenderNodeGraphType() {
            this.swigValue = SwigNext.next;
            SwigNext.access$008();
        }

        private CoreRenderNodeGraphType(int swigValue2) {
            this.swigValue = swigValue2;
            int unused = SwigNext.next = swigValue2 + 1;
        }

        private CoreRenderNodeGraphType(CoreRenderNodeGraphType swigEnum) {
            this.swigValue = swigEnum.swigValue;
            int unused = SwigNext.next = this.swigValue + 1;
        }

        private static class SwigNext {
            private static int next = 0;

            private SwigNext() {
            }

            static /* synthetic */ int access$008() {
                int i = next;
                next = i + 1;
                return i;
            }
        }
    }
}
