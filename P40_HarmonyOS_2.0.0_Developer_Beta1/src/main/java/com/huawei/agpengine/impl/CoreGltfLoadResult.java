package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreGltfLoadResult {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltfLoadResult(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltfLoadResult obj) {
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
                CoreJni.deleteCoreGltfLoadResult(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getSuccess() {
        return CoreJni.getVarsuccessCoreGltfLoadResult(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public String getError() {
        return CoreJni.getVarerrorCoreGltfLoadResult(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfData getData() {
        long cptr = CoreJni.getDataInCoreGltfLoadResult(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGltfData(cptr, false);
    }
}
