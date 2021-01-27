package com.huawei.agpengine.impl;

class CoreSystemGraphLoaderFactory extends CoreInterface {
    private transient long agpCptr;

    CoreSystemGraphLoaderFactory(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreSystemGraphLoaderFactory(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSystemGraphLoaderFactory obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.agpengine.impl.CoreInterface
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }
}
