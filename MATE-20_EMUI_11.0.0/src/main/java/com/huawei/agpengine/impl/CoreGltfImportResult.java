package com.huawei.agpengine.impl;

class CoreGltfImportResult {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltfImportResult(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltfImportResult obj) {
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
                CoreJni.deleteCoreGltfImportResult(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getSuccess() {
        return CoreJni.getVarsuccessCoreGltfImportResult(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public String getError() {
        return CoreJni.getVarerrorCoreGltfImportResult(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreGltfResourceData getData() {
        long cptr = CoreJni.getVardataCoreGltfImportResult(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGltfResourceData(cptr, false);
    }
}
