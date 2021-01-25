package ohos.agp.render.render3d.impl;

/* access modifiers changed from: package-private */
public class CoreWorldMatrixComponent {
    private transient long agpCptrWorldMatrixComponent;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreWorldMatrixComponent(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrWorldMatrixComponent = j;
    }

    static long getCptr(CoreWorldMatrixComponent coreWorldMatrixComponent) {
        if (coreWorldMatrixComponent == null) {
            return 0;
        }
        return coreWorldMatrixComponent.agpCptrWorldMatrixComponent;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrWorldMatrixComponent != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreWorldMatrixComponent(this.agpCptrWorldMatrixComponent);
                }
                this.agpCptrWorldMatrixComponent = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreWorldMatrixComponent coreWorldMatrixComponent, boolean z) {
        if (coreWorldMatrixComponent != null) {
            synchronized (coreWorldMatrixComponent.delLock) {
                coreWorldMatrixComponent.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreWorldMatrixComponent);
    }

    /* access modifiers changed from: package-private */
    public void setMatrix(CoreMat4X4 coreMat4X4) {
        CoreJni.setVarmatrixCoreWorldMatrixComponent(this.agpCptrWorldMatrixComponent, this, CoreMat4X4.getCptr(coreMat4X4), coreMat4X4);
    }

    /* access modifiers changed from: package-private */
    public CoreMat4X4 getMatrix() {
        long varmatrixCoreWorldMatrixComponent = CoreJni.getVarmatrixCoreWorldMatrixComponent(this.agpCptrWorldMatrixComponent, this);
        if (varmatrixCoreWorldMatrixComponent == 0) {
            return null;
        }
        return new CoreMat4X4(varmatrixCoreWorldMatrixComponent, false);
    }

    CoreWorldMatrixComponent() {
        this(CoreJni.newCoreWorldMatrixComponent(), true);
    }
}
