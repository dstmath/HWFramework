package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphDesc {
    private transient long agpCptrRenderNodeGraphDesc;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphDesc(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRenderNodeGraphDesc = j;
    }

    static long getCptr(CoreRenderNodeGraphDesc coreRenderNodeGraphDesc) {
        if (coreRenderNodeGraphDesc == null) {
            return 0;
        }
        return coreRenderNodeGraphDesc.agpCptrRenderNodeGraphDesc;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRenderNodeGraphDesc != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderNodeGraphDesc(this.agpCptrRenderNodeGraphDesc);
                }
                this.agpCptrRenderNodeGraphDesc = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeGraphDesc coreRenderNodeGraphDesc, boolean z) {
        if (coreRenderNodeGraphDesc != null) {
            synchronized (coreRenderNodeGraphDesc.delLock) {
                coreRenderNodeGraphDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeGraphDesc);
    }

    /* access modifiers changed from: package-private */
    public void setRenderNodeGraphName(String str) {
        CoreJni.setVarrenderNodeGraphNameCoreRenderNodeGraphDesc(this.agpCptrRenderNodeGraphDesc, this, str);
    }

    /* access modifiers changed from: package-private */
    public String getRenderNodeGraphName() {
        return CoreJni.getVarrenderNodeGraphNameCoreRenderNodeGraphDesc(this.agpCptrRenderNodeGraphDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setNodes(CoreRenderNodeDescArrayView coreRenderNodeDescArrayView) {
        CoreJni.setVarnodesCoreRenderNodeGraphDesc(this.agpCptrRenderNodeGraphDesc, this, CoreRenderNodeDescArrayView.getCptr(coreRenderNodeDescArrayView), coreRenderNodeDescArrayView);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderNodeDescArrayView getNodes() {
        long varnodesCoreRenderNodeGraphDesc = CoreJni.getVarnodesCoreRenderNodeGraphDesc(this.agpCptrRenderNodeGraphDesc, this);
        if (varnodesCoreRenderNodeGraphDesc == 0) {
            return null;
        }
        return new CoreRenderNodeDescArrayView(varnodesCoreRenderNodeGraphDesc, false);
    }

    CoreRenderNodeGraphDesc() {
        this(CoreJni.newCoreRenderNodeGraphDesc(), true);
    }
}
