package com.huawei.agpengine.impl;

import java.math.BigInteger;

/* access modifiers changed from: package-private */
public class CoreSystem {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSystem(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSystem obj) {
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

    static long getCptrAndSetMemOwn(CoreSystem obj, boolean isMemOwn) {
        long cptr;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            obj.isAgpCmemOwn = isMemOwn;
            cptr = getCptr(obj);
        }
        return cptr;
    }

    /* access modifiers changed from: package-private */
    public String name() {
        return CoreJni.nameInCoreSystem(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CorePropertyHandle getProps() {
        long cptr = CoreJni.getPropsInCoreSystem(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CorePropertyHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setProps(CorePropertyHandle arg0) {
        CoreJni.setPropsInCoreSystem(this.agpCptr, this, CorePropertyHandle.getCptr(arg0), arg0);
    }

    /* access modifiers changed from: package-private */
    public boolean isActive() {
        return CoreJni.isActiveInCoreSystem(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setActive(boolean isActive) {
        CoreJni.setActiveInCoreSystem(this.agpCptr, this, isActive);
    }

    /* access modifiers changed from: package-private */
    public void initialize() {
        CoreJni.initializeInCoreSystem(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean update(boolean isFrameRenderingQueued, BigInteger time, BigInteger delta) {
        return CoreJni.updateInCoreSystem(this.agpCptr, this, isFrameRenderingQueued, time, delta);
    }

    /* access modifiers changed from: package-private */
    public void uninitialize() {
        CoreJni.uninitializeInCoreSystem(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreEcs getEcs() {
        return new CoreEcs(CoreJni.getEcsInCoreSystem(this.agpCptr, this), false);
    }
}
