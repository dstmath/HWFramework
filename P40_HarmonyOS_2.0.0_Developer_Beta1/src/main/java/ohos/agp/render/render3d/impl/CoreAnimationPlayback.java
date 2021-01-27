package ohos.agp.render.render3d.impl;

class CoreAnimationPlayback {
    private transient long agpCptrCoreAnimationPlayback;
    transient boolean isAgpCmemOwn;
    private final Object lock = new Object();

    CoreAnimationPlayback(long j, boolean z) {
        this.isAgpCmemOwn = z;
        this.agpCptrCoreAnimationPlayback = j;
    }

    static long getCptr(CoreAnimationPlayback coreAnimationPlayback) {
        if (coreAnimationPlayback == null) {
            return 0;
        }
        return coreAnimationPlayback.agpCptrCoreAnimationPlayback;
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreAnimationPlayback != 0) {
                if (!this.isAgpCmemOwn) {
                    this.agpCptrCoreAnimationPlayback = 0;
                } else {
                    this.isAgpCmemOwn = false;
                    throw new UnsupportedOperationException("C++ destructor does not have public access");
                }
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreAnimationPlayback coreAnimationPlayback, boolean z) {
        if (coreAnimationPlayback != null) {
            synchronized (coreAnimationPlayback.lock) {
                coreAnimationPlayback.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreAnimationPlayback);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return CoreJni.getNameInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }

    /* access modifiers changed from: package-private */
    public void setPlaybackState(CoreAnimationPlaybackState coreAnimationPlaybackState) {
        CoreJni.setPlaybackStateInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this, coreAnimationPlaybackState.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationPlaybackState getPlaybackState() {
        return CoreAnimationPlaybackState.swigToEnum(CoreJni.getPlaybackStateInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this));
    }

    /* access modifiers changed from: package-private */
    public void setRepeatCount(long j) {
        CoreJni.setRepeatCountInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this, j);
    }

    /* access modifiers changed from: package-private */
    public long getRepeatCount() {
        return CoreJni.getRepeatCountInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }

    /* access modifiers changed from: package-private */
    public void setWeight(float f) {
        CoreJni.setWeightInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getWeight() {
        return CoreJni.getWeightInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }

    /* access modifiers changed from: package-private */
    public void setTimePosition(float f) {
        CoreJni.setTimePositionInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getTimePosition() {
        return CoreJni.getTimePositionInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }

    /* access modifiers changed from: package-private */
    public float getDuration() {
        return CoreJni.getDurationInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }

    /* access modifiers changed from: package-private */
    public boolean isCompleted() {
        return CoreJni.isCompletedInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpeed(float f) {
        CoreJni.setSpeedInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this, f);
    }

    /* access modifiers changed from: package-private */
    public float getSpeed() {
        return CoreJni.getSpeedInCoreAnimationPlayback(this.agpCptrCoreAnimationPlayback, this);
    }
}
