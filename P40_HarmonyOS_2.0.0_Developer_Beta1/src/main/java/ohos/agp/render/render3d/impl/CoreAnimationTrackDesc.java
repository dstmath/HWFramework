package ohos.agp.render.render3d.impl;

class CoreAnimationTrackDesc {
    private transient long agpCptrCoreAnimationTrackDesc;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreAnimationTrackDesc(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreAnimationTrackDesc = j;
    }

    static long getCptr(CoreAnimationTrackDesc coreAnimationTrackDesc) {
        if (coreAnimationTrackDesc == null) {
            return 0;
        }
        return coreAnimationTrackDesc.agpCptrCoreAnimationTrackDesc;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreAnimationTrackDesc != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc);
                }
                this.agpCptrCoreAnimationTrackDesc = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreAnimationTrackDesc coreAnimationTrackDesc, boolean z) {
        if (coreAnimationTrackDesc != null) {
            synchronized (coreAnimationTrackDesc.lock) {
                coreAnimationTrackDesc.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreAnimationTrackDesc);
    }

    /* access modifiers changed from: package-private */
    public void setPath(String str) {
        CoreJni.setVarpathCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, str);
    }

    /* access modifiers changed from: package-private */
    public String getPath() {
        return CoreJni.getVarpathCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setInterpolationMode(CoreAnimationInterpolation coreAnimationInterpolation) {
        CoreJni.setVarinterpolationModeCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, coreAnimationInterpolation.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationInterpolation getInterpolationMode() {
        return CoreAnimationInterpolation.swigToEnum(CoreJni.getVarinterpolationModeCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this));
    }

    /* access modifiers changed from: package-private */
    public void setType(CoreAnimationType coreAnimationType) {
        CoreJni.setVartypeCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, coreAnimationType.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationType getType() {
        return CoreAnimationType.swigToEnum(CoreJni.getVartypeCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this));
    }

    /* access modifiers changed from: package-private */
    public void setFrameCount(long j) {
        CoreJni.setVarframeCountCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getFrameCount() {
        return CoreJni.getVarframeCountCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setDataElementCount(long j) {
        CoreJni.setVardataElementCountCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getDataElementCount() {
        return CoreJni.getVardataElementCountCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this);
    }

    /* access modifiers changed from: package-private */
    public void setTimestamps(CoreResourceDataHandle coreResourceDataHandle) {
        CoreJni.setVartimestampsCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, CoreResourceDataHandle.getCptr(coreResourceDataHandle), coreResourceDataHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getTimestamps() {
        long vartimestampsCoreAnimationTrackDesc = CoreJni.getVartimestampsCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this);
        if (vartimestampsCoreAnimationTrackDesc == 0) {
            return null;
        }
        return new CoreResourceDataHandle(vartimestampsCoreAnimationTrackDesc, false);
    }

    /* access modifiers changed from: package-private */
    public void setData(CoreResourceDataHandle coreResourceDataHandle) {
        CoreJni.setVardataCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this, CoreResourceDataHandle.getCptr(coreResourceDataHandle), coreResourceDataHandle);
    }

    /* access modifiers changed from: package-private */
    public CoreResourceDataHandle getData() {
        long vardataCoreAnimationTrackDesc = CoreJni.getVardataCoreAnimationTrackDesc(this.agpCptrCoreAnimationTrackDesc, this);
        if (vardataCoreAnimationTrackDesc == 0) {
            return null;
        }
        return new CoreResourceDataHandle(vardataCoreAnimationTrackDesc, false);
    }

    CoreAnimationTrackDesc() {
        this(CoreJni.newCoreAnimationTrackDesc(), true);
    }
}
