package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreRenderMeshComponent {
    private transient long agpCptrRenderMeshComponent;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRenderMeshComponent(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRenderMeshComponent = j;
    }

    static long getCptr(CoreRenderMeshComponent coreRenderMeshComponent) {
        if (coreRenderMeshComponent == null) {
            return 0;
        }
        return coreRenderMeshComponent.agpCptrRenderMeshComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRenderMeshComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRenderMeshComponent(this.agpCptrRenderMeshComponent);
                }
                this.agpCptrRenderMeshComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRenderMeshComponent coreRenderMeshComponent, boolean z) {
        if (coreRenderMeshComponent != null) {
            synchronized (coreRenderMeshComponent.delLock) {
                coreRenderMeshComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRenderMeshComponent);
    }

    /* access modifiers changed from: package-private */
    public void setMesh(CoreResourceHandle coreResourceHandle) {
        CoreJni.setVarmeshCoreRenderMeshComponent(this.agpCptrRenderMeshComponent, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMesh() {
        long varmeshCoreRenderMeshComponent = CoreJni.getVarmeshCoreRenderMeshComponent(this.agpCptrRenderMeshComponent, this);
        if (varmeshCoreRenderMeshComponent == 0) {
            return null;
        }
        return new CoreResourceHandle(varmeshCoreRenderMeshComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setMaterial(CoreResourceHandle coreResourceHandle) {
        CoreJni.setVarmaterialCoreRenderMeshComponent(this.agpCptrRenderMeshComponent, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle getMaterial() {
        long varmaterialCoreRenderMeshComponent = CoreJni.getVarmaterialCoreRenderMeshComponent(this.agpCptrRenderMeshComponent, this);
        if (varmaterialCoreRenderMeshComponent == 0) {
            return null;
        }
        return new CoreResourceHandle(varmaterialCoreRenderMeshComponent, false);
    }

    /* access modifiers changed from: package-private */
    public void setCastShadows(boolean z) {
        CoreJni.setVarcastShadowsCoreRenderMeshComponent(this.agpCptrRenderMeshComponent, this, z);
    }

    /* access modifiers changed from: package-private */
    public boolean getCastShadows() {
        return CoreJni.getVarcastShadowsCoreRenderMeshComponent(this.agpCptrRenderMeshComponent, this);
    }

    CoreRenderMeshComponent() {
        this(CoreJni.newCoreRenderMeshComponent(), true);
    }
}
