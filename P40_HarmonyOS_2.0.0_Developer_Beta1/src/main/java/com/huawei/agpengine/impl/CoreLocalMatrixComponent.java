package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreLocalMatrixComponent {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreLocalMatrixComponent(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreLocalMatrixComponent obj) {
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
                CoreJni.deleteCoreLocalMatrixComponent(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setMatrix(CoreMat4X4 value) {
        CoreJni.setVarmatrixCoreLocalMatrixComponent(this.agpCptr, this, CoreMat4X4.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreMat4X4 getMatrix() {
        long cptr = CoreJni.getVarmatrixCoreLocalMatrixComponent(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreMat4X4(cptr, false);
    }

    CoreLocalMatrixComponent() {
        this(CoreJni.newCoreLocalMatrixComponent(), true);
    }
}
