package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderNodeGraphDesc obj) {
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
                CoreJni.deleteCoreRenderNodeGraphDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setRenderNodeGraphName(String value) {
        CoreJni.setVarrenderNodeGraphNameCoreRenderNodeGraphDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public String getRenderNodeGraphName() {
        return CoreJni.getVarrenderNodeGraphNameCoreRenderNodeGraphDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setNodes(CoreRenderNodeDescArrayView value) {
        CoreJni.setVarnodesCoreRenderNodeGraphDesc(this.agpCptr, this, CoreRenderNodeDescArrayView.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeDescArrayView getNodes() {
        long cptr = CoreJni.getVarnodesCoreRenderNodeGraphDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreRenderNodeDescArrayView(cptr, false);
    }

    CoreRenderNodeGraphDesc() {
        this(CoreJni.newCoreRenderNodeGraphDesc(), true);
    }
}
