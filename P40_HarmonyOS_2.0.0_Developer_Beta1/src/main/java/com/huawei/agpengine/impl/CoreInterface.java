package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreInterface {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreInterface(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreInterface obj) {
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
    public String name() {
        return CoreJni.nameInCoreInterface(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CorePluginRegister getPluginRegister() {
        return new CorePluginRegister(CoreJni.getPluginRegisterInCoreInterface(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreInterface getInterface(String name) {
        long cptr = CoreJni.getInterfaceInCoreInterface(this.agpCptr, this, name);
        if (cptr == 0) {
            return null;
        }
        return new CoreInterface(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void ref() {
        CoreJni.refInCoreInterface(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void unref() {
        CoreJni.unrefInCoreInterface(this.agpCptr, this);
    }
}
