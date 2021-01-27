package com.huawei.agpengine.impl;

class CoreAnimationTrackDesc {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreAnimationTrackDesc(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreAnimationTrackDesc obj) {
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
                CoreJni.deleteCoreAnimationTrackDesc(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPath(String value) {
        CoreJni.setVarpathCoreAnimationTrackDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public String getPath() {
        return CoreJni.getVarpathCoreAnimationTrackDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setInterpolationMode(CoreAnimationInterpolation value) {
        CoreJni.setVarinterpolationModeCoreAnimationTrackDesc(this.agpCptr, this, value.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationInterpolation getInterpolationMode() {
        return CoreAnimationInterpolation.swigToEnum(CoreJni.getVarinterpolationModeCoreAnimationTrackDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setType(CoreAnimationType value) {
        CoreJni.setVartypeCoreAnimationTrackDesc(this.agpCptr, this, value.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationType getType() {
        return CoreAnimationType.swigToEnum(CoreJni.getVartypeCoreAnimationTrackDesc(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setFrameCount(long value) {
        CoreJni.setVarframeCountCoreAnimationTrackDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getFrameCount() {
        return CoreJni.getVarframeCountCoreAnimationTrackDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setDataElementCount(long value) {
        CoreJni.setVardataElementCountCoreAnimationTrackDesc(this.agpCptr, this, value);
    }

    /* access modifiers changed from: package-private */
    public long getDataElementCount() {
        return CoreJni.getVardataElementCountCoreAnimationTrackDesc(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setTimestamps(CoreResourceDataHandle value) {
        CoreJni.setVartimestampsCoreAnimationTrackDesc(this.agpCptr, this, CoreResourceDataHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getTimestamps() {
        long cptr = CoreJni.getVartimestampsCoreAnimationTrackDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceDataHandle(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void setData(CoreResourceDataHandle value) {
        CoreJni.setVardataCoreAnimationTrackDesc(this.agpCptr, this, CoreResourceDataHandle.getCptr(value), value);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getData() {
        long cptr = CoreJni.getVardataCoreAnimationTrackDesc(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreResourceDataHandle(cptr, false);
    }

    CoreAnimationTrackDesc() {
        this(CoreJni.newCoreAnimationTrackDesc(), true);
    }
}
