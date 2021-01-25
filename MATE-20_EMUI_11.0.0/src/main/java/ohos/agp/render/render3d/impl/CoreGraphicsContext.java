package ohos.agp.render.render3d.impl;

class CoreGraphicsContext {
    private transient long agpCptrCoreGraphicsContext;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreGraphicsContext(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGraphicsContext = j;
    }

    static long getCptr(CoreGraphicsContext coreGraphicsContext) {
        if (coreGraphicsContext == null) {
            return 0;
        }
        return coreGraphicsContext.agpCptrCoreGraphicsContext;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGraphicsContext != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreGraphicsContext = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGraphicsContext coreGraphicsContext, boolean z) {
        if (coreGraphicsContext != null) {
            synchronized (coreGraphicsContext.lock) {
                coreGraphicsContext.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGraphicsContext);
    }

    static CoreGraphicsContext create(CoreEngine coreEngine) {
        long createInCoreGraphicsContext0 = CoreJni.createInCoreGraphicsContext0(CoreEngine.getCptr(coreEngine), coreEngine);
        if (createInCoreGraphicsContext0 == 0) {
            return null;
        }
        return new CoreGraphicsContext(createInCoreGraphicsContext0, false);
    }

    static CoreGraphicsContext create(CoreEngineCreateInfo coreEngineCreateInfo, CoreDeviceCreateInfo coreDeviceCreateInfo) {
        long createInCoreGraphicsContext1 = CoreJni.createInCoreGraphicsContext1(CoreEngineCreateInfo.getCptr(coreEngineCreateInfo), coreEngineCreateInfo, CoreDeviceCreateInfo.getCptr(coreDeviceCreateInfo), coreDeviceCreateInfo);
        if (createInCoreGraphicsContext1 == 0) {
            return null;
        }
        return new CoreGraphicsContext(createInCoreGraphicsContext1, false);
    }

    static void destroy(CoreGraphicsContext coreGraphicsContext) {
        CoreJni.destroyInCoreGraphicsContext(getCptr(coreGraphicsContext), coreGraphicsContext);
    }

    static String getVersion() {
        return CoreJni.getVersionInCoreGraphicsContext();
    }

    /* access modifiers changed from: package-private */
    public void init() {
        CoreJni.initInCoreGraphicsContext(this.agpCptrCoreGraphicsContext, this);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphType coreRenderNodeGraphType) {
        CoreJni.renderFrameInCoreGraphicsContext(this.agpCptrCoreGraphicsContext, this, coreRenderNodeGraphType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreEcs getEcs() {
        return new CoreEcs(CoreJni.getEcsInCoreGraphicsContext(this.agpCptrCoreGraphicsContext, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreator getResourceCreator() {
        return new CoreResourceCreator(CoreJni.getResourceCreatorInCoreGraphicsContext(this.agpCptrCoreGraphicsContext, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreEngine getEngine() {
        return new CoreEngine(CoreJni.getEngineInCoreGraphicsContext(this.agpCptrCoreGraphicsContext, this), false);
    }

    enum CoreRenderNodeGraphType {
        UNDEFINED(0),
        LIGHT_WEIGHT_RENDERING_PIPELINE(1),
        LIGHT_WEIGHT_RENDERING_PIPELINE_MSAA(2),
        HIGH_DEFINITION_RENDERING_PIPELINE(3);
        
        private final int swigValue;

        static CoreRenderNodeGraphType swigToEnum(int i) {
            CoreRenderNodeGraphType[] coreRenderNodeGraphTypeArr = (CoreRenderNodeGraphType[]) CoreRenderNodeGraphType.class.getEnumConstants();
            if (i < coreRenderNodeGraphTypeArr.length && i >= 0 && coreRenderNodeGraphTypeArr[i].swigValue == i) {
                return coreRenderNodeGraphTypeArr[i];
            }
            for (CoreRenderNodeGraphType coreRenderNodeGraphType : coreRenderNodeGraphTypeArr) {
                if (coreRenderNodeGraphType.swigValue == i) {
                    return coreRenderNodeGraphType;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreRenderNodeGraphType.class + " with value " + i);
        }

        /* access modifiers changed from: package-private */
        public final int swigValue() {
            return this.swigValue;
        }

        private CoreRenderNodeGraphType() {
            this(SwigNext.next);
        }

        private CoreRenderNodeGraphType(CoreRenderNodeGraphType coreRenderNodeGraphType) {
            this(coreRenderNodeGraphType.swigValue);
        }

        private CoreRenderNodeGraphType(int i) {
            this.swigValue = i;
            int unused = SwigNext.next = i + 1;
        }

        private static class SwigNext {
            private static int next;

            private SwigNext() {
            }
        }
    }
}
