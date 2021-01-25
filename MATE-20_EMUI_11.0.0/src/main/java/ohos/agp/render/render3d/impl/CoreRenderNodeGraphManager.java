package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphManager {
    private transient long agpCptr;
    private final Object delLock = new Object();
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphManager(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptr = j;
    }

    static long getCptr(CoreRenderNodeGraphManager coreRenderNodeGraphManager) {
        if (coreRenderNodeGraphManager == null) {
            return 0;
        }
        return coreRenderNodeGraphManager.agpCptr;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptr != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptr = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeGraphManager coreRenderNodeGraphManager, boolean z) {
        if (coreRenderNodeGraphManager != null) {
            synchronized (coreRenderNodeGraphManager.delLock) {
                coreRenderNodeGraphManager.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeGraphManager);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderHandle create(CoreRenderNodeGraphUsageType coreRenderNodeGraphUsageType, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc, String str) {
        return new CoreRenderHandle(CoreJni.createInCoreRenderNodeGraphManager0(this.agpCptr, this, coreRenderNodeGraphUsageType.swigValue(), CoreRenderNodeGraphDesc.getCptr(coreRenderNodeGraphDesc), coreRenderNodeGraphDesc, str), true);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderHandle create(CoreRenderNodeGraphUsageType coreRenderNodeGraphUsageType, CoreRenderNodeGraphDesc coreRenderNodeGraphDesc) {
        return new CoreRenderHandle(CoreJni.createInCoreRenderNodeGraphManager1(this.agpCptr, this, coreRenderNodeGraphUsageType.swigValue(), CoreRenderNodeGraphDesc.getCptr(coreRenderNodeGraphDesc), coreRenderNodeGraphDesc), true);
    }

    /* access modifiers changed from: package-private */
    public void destroy(CoreRenderHandle coreRenderHandle) {
        CoreJni.destroyInCoreRenderNodeGraphManager(this.agpCptr, this, CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreStringArray getRenderNodeNames(CoreRenderHandle coreRenderHandle) {
        return new CoreStringArray(CoreJni.getRenderNodeNamesInCoreRenderNodeGraphManager(this.agpCptr, this, CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle), true);
    }

    /* access modifiers changed from: package-private */
    public long getRenderNodeCount(CoreRenderHandle coreRenderHandle) {
        return CoreJni.getRenderNodeCountInCoreRenderNodeGraphManager(this.agpCptr, this, CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle);
    }

    /* access modifiers changed from: package-private */
    public void insertRenderNode(CoreRenderHandle coreRenderHandle, CoreRenderNodeDesc coreRenderNodeDesc, long j) {
        CoreJni.insertRenderNodeInCoreRenderNodeGraphManager(this.agpCptr, this, CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle, CoreRenderNodeDesc.getCptr(coreRenderNodeDesc), coreRenderNodeDesc, j);
    }

    /* access modifiers changed from: package-private */
    public void eraseRenderNode(CoreRenderHandle coreRenderHandle, long j) {
        CoreJni.eraseRenderNodeInCoreRenderNodeGraphManager(this.agpCptr, this, CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle, j);
    }

    /* access modifiers changed from: package-private */
    public enum CoreRenderNodeGraphUsageType {
        RENDER_NODE_GRAPH_STATIC(0),
        RENDER_NODE_GRAPH_DYNAMIC(1);
        
        private final int swigValue;

        /* access modifiers changed from: package-private */
        public final int swigValue() {
            return this.swigValue;
        }

        static CoreRenderNodeGraphUsageType swigToEnum(int i) {
            CoreRenderNodeGraphUsageType[] coreRenderNodeGraphUsageTypeArr = (CoreRenderNodeGraphUsageType[]) CoreRenderNodeGraphUsageType.class.getEnumConstants();
            if (i < coreRenderNodeGraphUsageTypeArr.length && i >= 0 && coreRenderNodeGraphUsageTypeArr[i].swigValue == i) {
                return coreRenderNodeGraphUsageTypeArr[i];
            }
            for (CoreRenderNodeGraphUsageType coreRenderNodeGraphUsageType : coreRenderNodeGraphUsageTypeArr) {
                if (coreRenderNodeGraphUsageType.swigValue == i) {
                    return coreRenderNodeGraphUsageType;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreRenderNodeGraphUsageType.class + " with value " + i);
        }

        private CoreRenderNodeGraphUsageType() {
            this(SwigNext.next);
        }

        private CoreRenderNodeGraphUsageType(int i) {
            this.swigValue = i;
            int unused = SwigNext.next = i + 1;
        }

        private CoreRenderNodeGraphUsageType(CoreRenderNodeGraphUsageType coreRenderNodeGraphUsageType) {
            this(coreRenderNodeGraphUsageType.swigValue);
        }

        private static class SwigNext {
            private static int next;

            private SwigNext() {
            }
        }
    }
}
