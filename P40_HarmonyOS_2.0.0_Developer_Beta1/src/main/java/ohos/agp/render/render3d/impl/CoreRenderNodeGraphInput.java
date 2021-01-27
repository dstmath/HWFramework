package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderNodeGraphInput {
    private transient long agpCptrRenderNodeGraphInput;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderNodeGraphInput(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRenderNodeGraphInput = j;
    }

    static long getCptr(CoreRenderNodeGraphInput coreRenderNodeGraphInput) {
        if (coreRenderNodeGraphInput == null) {
            return 0;
        }
        return coreRenderNodeGraphInput.agpCptrRenderNodeGraphInput;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRenderNodeGraphInput != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderNodeGraphInput(this.agpCptrRenderNodeGraphInput);
                }
                this.agpCptrRenderNodeGraphInput = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderNodeGraphInput coreRenderNodeGraphInput, boolean z) {
        if (coreRenderNodeGraphInput != null) {
            synchronized (coreRenderNodeGraphInput.delLock) {
                coreRenderNodeGraphInput.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderNodeGraphInput);
    }

    /* access modifiers changed from: package-private */
    public void setRenderNodeGraphHandle(CoreRenderHandle coreRenderHandle) {
        CoreJni.setVarrenderNodeGraphHandleCoreRenderNodeGraphInput(this.agpCptrRenderNodeGraphInput, this, CoreRenderHandle.getCptr(coreRenderHandle), coreRenderHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreRenderHandle getRenderNodeGraphHandle() {
        long varrenderNodeGraphHandleCoreRenderNodeGraphInput = CoreJni.getVarrenderNodeGraphHandleCoreRenderNodeGraphInput(this.agpCptrRenderNodeGraphInput, this);
        if (varrenderNodeGraphHandleCoreRenderNodeGraphInput == 0) {
            return null;
        }
        return new CoreRenderHandle(varrenderNodeGraphHandleCoreRenderNodeGraphInput, false);
    }

    CoreRenderNodeGraphInput() {
        this(CoreJni.newCoreRenderNodeGraphInput(), true);
    }
}
