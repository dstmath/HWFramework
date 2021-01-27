package com.huawei.agpengine.impl;

class CoreGltf2ImporterPtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGltf2ImporterPtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGltf2ImporterPtr obj) {
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
                CoreJni.deleteCoreGltf2ImporterPtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreGltf2ImporterPtr(CoreGltf2Importer Ptr) {
        this(CoreJni.newCoreGltf2ImporterPtr0(CoreGltf2Importer.getCptr(Ptr), Ptr), true);
    }

    CoreGltf2ImporterPtr(CoreGltf2ImporterPtr Right) {
        this(CoreJni.newCoreGltf2ImporterPtr1(getCptr(Right), Right), true);
    }

    /* access modifiers changed from: package-private */
    public CoreGltf2Importer get() {
        long cptr = CoreJni.getInCoreGltf2ImporterPtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreGltf2Importer(cptr, false);
    }
}
