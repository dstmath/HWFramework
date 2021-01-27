package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreLocalMatrixComponent {
    private transient long agpCptrCoreLocalMatrixComponent;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreLocalMatrixComponent(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreLocalMatrixComponent = j;
    }

    static long getCptr(CoreLocalMatrixComponent coreLocalMatrixComponent) {
        if (coreLocalMatrixComponent == null) {
            return 0;
        }
        return coreLocalMatrixComponent.agpCptrCoreLocalMatrixComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreLocalMatrixComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreLocalMatrixComponent(this.agpCptrCoreLocalMatrixComponent);
                }
                this.agpCptrCoreLocalMatrixComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreLocalMatrixComponent coreLocalMatrixComponent, boolean z) {
        if (coreLocalMatrixComponent != null) {
            synchronized (coreLocalMatrixComponent.lock) {
                coreLocalMatrixComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreLocalMatrixComponent);
    }

    /* access modifiers changed from: package-private */
    public void setMatrix(CoreMat4X4 coreMat4X4) {
        CoreJni.setVarmatrixCoreLocalMatrixComponent(this.agpCptrCoreLocalMatrixComponent, this, CoreMat4X4.getCptr(coreMat4X4), coreMat4X4);
    }

    /* access modifiers changed from: package-private */
    public CoreMat4X4 getMatrix() {
        long varmatrixCoreLocalMatrixComponent = CoreJni.getVarmatrixCoreLocalMatrixComponent(this.agpCptrCoreLocalMatrixComponent, this);
        if (varmatrixCoreLocalMatrixComponent == 0) {
            return null;
        }
        return new CoreMat4X4(varmatrixCoreLocalMatrixComponent, false);
    }

    CoreLocalMatrixComponent() {
        this(CoreJni.newCoreLocalMatrixComponent(), true);
    }
}
