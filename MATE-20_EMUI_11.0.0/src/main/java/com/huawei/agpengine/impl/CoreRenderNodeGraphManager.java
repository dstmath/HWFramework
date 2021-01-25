package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphManager {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphManager(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeGraphManager obj) {
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
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long create(CoreRenderNodeGraphUsageType usage, CoreRenderNodeGraphDesc desc, String renderNodeGraphName) {
        return CoreJni.createInCoreRenderNodeGraphManager0(this.agpCptr, this, usage.swigValue(), CoreRenderNodeGraphDesc.getCptr(desc), desc, renderNodeGraphName);
    }

    /* access modifiers changed from: package-private */
    public long create(CoreRenderNodeGraphUsageType usage, CoreRenderNodeGraphDesc desc) {
        return CoreJni.createInCoreRenderNodeGraphManager1(this.agpCptr, this, usage.swigValue(), CoreRenderNodeGraphDesc.getCptr(desc), desc);
    }

    /* access modifiers changed from: package-private */
    public void destroy(long handle) {
        CoreJni.destroyInCoreRenderNodeGraphManager(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public CoreStringArray getRenderNodeNames(long handle) {
        return new CoreStringArray(CoreJni.getRenderNodeNamesInCoreRenderNodeGraphManager(this.agpCptr, this, handle), true);
    }

    /* access modifiers changed from: package-private */
    public long getRenderNodeCount(long handle) {
        return CoreJni.getRenderNodeCountInCoreRenderNodeGraphManager(this.agpCptr, this, handle);
    }

    /* access modifiers changed from: package-private */
    public void insertRenderNode(long handle, CoreRenderNodeDesc renderNodeDesc, long insertIndex) {
        CoreJni.insertRenderNodeInCoreRenderNodeGraphManager(this.agpCptr, this, handle, CoreRenderNodeDesc.getCptr(renderNodeDesc), renderNodeDesc, insertIndex);
    }

    /* access modifiers changed from: package-private */
    public void eraseRenderNode(long handle, long eraseIndex) {
        CoreJni.eraseRenderNodeInCoreRenderNodeGraphManager(this.agpCptr, this, handle, eraseIndex);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeGraphLoader getRenderNodeGraphLoader() {
        return new CoreRenderNodeGraphLoader(CoreJni.getRenderNodeGraphLoaderInCoreRenderNodeGraphManager(this.agpCptr, this), false);
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

        static CoreRenderNodeGraphUsageType swigToEnum(int swigValue2) {
            CoreRenderNodeGraphUsageType[] swigValues = (CoreRenderNodeGraphUsageType[]) CoreRenderNodeGraphUsageType.class.getEnumConstants();
            if (swigValue2 < swigValues.length && swigValue2 >= 0 && swigValues[swigValue2].swigValue == swigValue2) {
                return swigValues[swigValue2];
            }
            for (CoreRenderNodeGraphUsageType swigEnum : swigValues) {
                if (swigEnum.swigValue == swigValue2) {
                    return swigEnum;
                }
            }
            throw new IllegalArgumentException("No enum " + CoreRenderNodeGraphUsageType.class + " with value " + swigValue2);
        }

        private CoreRenderNodeGraphUsageType() {
            this.swigValue = SwigNext.next;
            SwigNext.access$008();
        }

        private CoreRenderNodeGraphUsageType(int swigValue2) {
            this.swigValue = swigValue2;
            int unused = SwigNext.next = swigValue2 + 1;
        }

        private CoreRenderNodeGraphUsageType(CoreRenderNodeGraphUsageType swigEnum) {
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
