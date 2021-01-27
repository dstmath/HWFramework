package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreRenderMeshComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRenderMeshComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRenderMeshComponent obj) {
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
                CoreJni.deleteCoreRenderMeshComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setMesh(CoreResourceHandle value) {
        CoreJni.setVarmeshCoreRenderMeshComponent(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMesh() {
        long cptr = CoreJni.getVarmeshCoreRenderMeshComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterial(CoreResourceHandle value) {
        CoreJni.setVarmaterialCoreRenderMeshComponent(this.agpCptr, this, CoreResourceHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMaterial() {
        long cptr = CoreJni.getVarmaterialCoreRenderMeshComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setCastShadows(boolean isEnabled) {
        CoreJni.setVarcastShadowsCoreRenderMeshComponent(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getCastShadows() {
        return CoreJni.getVarcastShadowsCoreRenderMeshComponent(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setReceiveShadows(boolean isEnabled) {
        CoreJni.setVarreceiveShadowsCoreRenderMeshComponent(this.agpCptr, this, isEnabled);
    }

    /* access modifiers changed from: package-private */
    public boolean getReceiveShadows() {
        return CoreJni.getVarreceiveShadowsCoreRenderMeshComponent(this.agpCptr, this);
    }

    CoreRenderMeshComponent() {
        this(CoreJni.newCoreRenderMeshComponent(), true);
    }
}
