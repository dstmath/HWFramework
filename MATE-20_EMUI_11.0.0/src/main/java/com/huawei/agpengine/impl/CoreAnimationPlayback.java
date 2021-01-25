package com.huawei.agpengine.impl;

class CoreAnimationPlayback {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreAnimationPlayback(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreAnimationPlayback obj) {
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
    public String getName() {
        return CoreJni.getNameInCoreAnimationPlayback(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setPlaybackState(CoreAnimationPlaybackState state) {
        CoreJni.setPlaybackStateInCoreAnimationPlayback(this.agpCptr, this, state.swigValue());
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationPlaybackState getPlaybackState() {
        return CoreAnimationPlaybackState.swigToEnum(CoreJni.getPlaybackStateInCoreAnimationPlayback(this.agpCptr, this));
    }

    /* access modifiers changed from: package-private */
    public void setRepeatCount(long repeatCount) {
        CoreJni.setRepeatCountInCoreAnimationPlayback(this.agpCptr, this, repeatCount);
    }

    /* access modifiers changed from: package-private */
    public long getRepeatCount() {
        return CoreJni.getRepeatCountInCoreAnimationPlayback(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setWeight(float weight) {
        CoreJni.setWeightInCoreAnimationPlayback(this.agpCptr, this, weight);
    }

    /* access modifiers changed from: package-private */
    public float getWeight() {
        return CoreJni.getWeightInCoreAnimationPlayback(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setTimePosition(float timePosition) {
        CoreJni.setTimePositionInCoreAnimationPlayback(this.agpCptr, this, timePosition);
    }

    /* access modifiers changed from: package-private */
    public float getTimePosition() {
        return CoreJni.getTimePositionInCoreAnimationPlayback(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public float getDuration() {
        return CoreJni.getDurationInCoreAnimationPlayback(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public boolean isCompleted() {
        return CoreJni.isCompletedInCoreAnimationPlayback(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void setSpeed(float arg0) {
        CoreJni.setSpeedInCoreAnimationPlayback(this.agpCptr, this, arg0);
    }

    /* access modifiers changed from: package-private */
    public float getSpeed() {
        return CoreJni.getSpeedInCoreAnimationPlayback(this.agpCptr, this);
    }
}
