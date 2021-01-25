package com.huawei.agpengine.impl;

class CoreAnimation extends CoreResource {
    private transient long agpCptr;

    CoreAnimation(long cptr, boolean isCmemoryOwn) {
        super(CoreJni.classUpcastCoreAnimation(cptr), isCmemoryOwn);
        this.agpCptr = cptr;
    }

    static long getCptr(CoreAnimation obj) {
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
    @Override // com.huawei.agpengine.impl.CoreResource
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

    /* access modifiers changed from: package-private */
    public CoreAnimationDesc getDesc() {
        return new CoreAnimationDesc(CoreJni.getDescInCoreAnimation(this.agpCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationTrackDescArrayView getTracks() {
        return new CoreAnimationTrackDescArrayView(CoreJni.getTracksInCoreAnimation(this.agpCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getTimestamps(CoreAnimationTrackDesc animationTrackDesc) {
        return new CoreFloatArrayView(CoreJni.getTimestampsInCoreAnimation(this.agpCptr, this, CoreAnimationTrackDesc.getCptr(animationTrackDesc), animationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getKeyframesFloat(CoreAnimationTrackDesc animationTrackDesc) {
        return new CoreFloatArrayView(CoreJni.getKeyframesFloatInCoreAnimation(this.agpCptr, this, CoreAnimationTrackDesc.getCptr(animationTrackDesc), animationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3ArrayView getKeyframesVec3(CoreAnimationTrackDesc animationTrackDesc) {
        return new CoreVec3ArrayView(CoreJni.getKeyframesVec3InCoreAnimation(this.agpCptr, this, CoreAnimationTrackDesc.getCptr(animationTrackDesc), animationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4ArrayView getKeyframesVec4(CoreAnimationTrackDesc animationTrackDesc) {
        return new CoreVec4ArrayView(CoreJni.getKeyframesVec4InCoreAnimation(this.agpCptr, this, CoreAnimationTrackDesc.getCptr(animationTrackDesc), animationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreBoolArrayView getKeyframesBoolean(CoreAnimationTrackDesc animationTrackDesc) {
        return new CoreBoolArrayView(CoreJni.getKeyframesBooleanInCoreAnimation(this.agpCptr, this, CoreAnimationTrackDesc.getCptr(animationTrackDesc), animationTrackDesc), true);
    }
}
