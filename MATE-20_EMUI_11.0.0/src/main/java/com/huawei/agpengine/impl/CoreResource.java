package com.huawei.agpengine.impl;

import java.math.BigInteger;

class CoreResource {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResource(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResource obj) {
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
    public CorePropertyHandle getProperties() {
        long cptr = CoreJni.getPropertiesInCoreResource(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CorePropertyHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setProperties(CorePropertyHandle handle) {
        CoreJni.setPropertiesInCoreResource(this.agpCptr, this, CorePropertyHandle.getCptr(handle), handle);
    }

    /* access modifiers changed from: package-private */
    public BigInteger getType() {
        return CoreJni.getTypeInCoreResource(this.agpCptr, this);
    }

    static void destroy(CoreResource r) {
        CoreJni.destroyInCoreResource(getCptr(r), r);
    }
}
