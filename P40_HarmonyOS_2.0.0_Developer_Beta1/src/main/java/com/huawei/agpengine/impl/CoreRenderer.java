package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderer {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderer(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderer obj) {
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
                CoreJni.deleteCoreRenderer(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInput renderNodeGraphInput) {
        CoreJni.renderFrameInCoreRenderer0(this.agpCptr, this, CoreRenderNodeGraphInput.getCptr(renderNodeGraphInput), renderNodeGraphInput);
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphType renderNodeGraph) {
        CoreJni.renderFrameInCoreRenderer1(this.agpCptr, this, renderNodeGraph.swigValue());
    }

    /* access modifiers changed from: package-private */
    public void renderFrame(CoreRenderNodeGraphInputArrayView renderNodeGraphInputs) {
        CoreJni.renderFrameInCoreRenderer2(this.agpCptr, this, CoreRenderNodeGraphInputArrayView.getCptr(renderNodeGraphInputs), renderNodeGraphInputs);
    }
}
