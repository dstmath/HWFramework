package com.huawei.agpengine.impl;

class CoreAnimationDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreAnimationDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreAnimationDesc obj) {
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
                CoreJni.deleteCoreAnimationDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setName(String value) {
        CoreJni.setVarnameCoreAnimationDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getVarnameCoreAnimationDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setTracks(CoreResourceDataHandle value) {
        CoreJni.setVartracksCoreAnimationDesc(this.agpCptr, this, CoreResourceDataHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getTracks() {
        long cptr = CoreJni.getVartracksCoreAnimationDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceDataHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setTrackCount(long value) {
        CoreJni.setVartrackCountCoreAnimationDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getTrackCount() {
        return CoreJni.getVartrackCountCoreAnimationDesc(this.agpCptr, this);
    }

    CoreAnimationDesc() {
        this(CoreJni.newCoreAnimationDesc(), true);
    }
}
