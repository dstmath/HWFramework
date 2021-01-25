package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CorePropertyApi {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CorePropertyApi(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CorePropertyApi obj) {
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
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long propertyCount() {
        return CoreJni.propertyCountInCorePropertyApi(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreProperty metaData(long index) {
        long cptr = CoreJni.metaDataInCorePropertyApi(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CoreProperty(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public boolean clone(CorePropertyHandle aDst, CorePropertyHandle arg1) {
        return CoreJni.cloneInCorePropertyApi(this.agpCptr, this, CorePropertyHandle.getCptr(aDst), aDst, CorePropertyHandle.getCptr(arg1), arg1);
    }

    /* access modifiers changed from: package-private */
    public void destroy(CorePropertyHandle aDst) {
        CoreJni.destroyInCorePropertyApi(this.agpCptr, this, CorePropertyHandle.getCptr(aDst), aDst);
    }
}
