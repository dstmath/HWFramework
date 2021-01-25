package ohos.agp.render.render3d.impl;

class CoreAnimation extends CoreResource {
    private transient long agpCptragpCoreAnimationCptr;

    CoreAnimation(long j, boolean z) {
        super(CoreJni.classUpcastCoreAnimation(j), z);
        this.agpCptragpCoreAnimationCptr = j;
    }

    static long getCptr(CoreAnimation coreAnimation) {
        if (coreAnimation == null) {
            return 0;
        }
        return coreAnimation.agpCptragpCoreAnimationCptr;
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.agp.render.render3d.impl.CoreResource
    public synchronized void delete() {
        if (this.agpCptragpCoreAnimationCptr != 0) {
            if (!this.isAgpCmemOwn) {
                this.agpCptragpCoreAnimationCptr = 0;
            } else {
                this.isAgpCmemOwn = false;
                throw new UnsupportedOperationException("C++ destructor does not have public access");
            }
        }
        super.delete();
    }

    static long getCptrAndSetMemOwn(CoreAnimation coreAnimation, boolean z) {
        if (coreAnimation != null) {
            coreAnimation.isAgpCmemOwn = z;
        }
        return getCptr(coreAnimation);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationDesc getDesc() {
        return new CoreAnimationDesc(CoreJni.getDescInCoreAnimation(this.agpCptragpCoreAnimationCptr, this), false);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationTrackDescArrayView getTracks() {
        return new CoreAnimationTrackDescArrayView(CoreJni.getTracksInCoreAnimation(this.agpCptragpCoreAnimationCptr, this), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getTimestamps(CoreAnimationTrackDesc coreAnimationTrackDesc) {
        return new CoreFloatArrayView(CoreJni.getTimestampsInCoreAnimation(this.agpCptragpCoreAnimationCptr, this, CoreAnimationTrackDesc.getCptr(coreAnimationTrackDesc), coreAnimationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreFloatArrayView getKeyframesFloat(CoreAnimationTrackDesc coreAnimationTrackDesc) {
        return new CoreFloatArrayView(CoreJni.getKeyframesFloatInCoreAnimation(this.agpCptragpCoreAnimationCptr, this, CoreAnimationTrackDesc.getCptr(coreAnimationTrackDesc), coreAnimationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3ArrayView getKeyframesVec3(CoreAnimationTrackDesc coreAnimationTrackDesc) {
        return new CoreVec3ArrayView(CoreJni.getKeyframesVec3InCoreAnimation(this.agpCptragpCoreAnimationCptr, this, CoreAnimationTrackDesc.getCptr(coreAnimationTrackDesc), coreAnimationTrackDesc), true);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4ArrayView getKeyframesVec4(CoreAnimationTrackDesc coreAnimationTrackDesc) {
        return new CoreVec4ArrayView(CoreJni.getKeyframesVec4InCoreAnimation(this.agpCptragpCoreAnimationCptr, this, CoreAnimationTrackDesc.getCptr(coreAnimationTrackDesc), coreAnimationTrackDesc), true);
    }
}
