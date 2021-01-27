package com.huawei.anim.dynamicanimation;

public class HWFlingAnimation extends BaseDecelerateAnimation<FlingModelBase> {
    public <K> HWFlingAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, float f, float f2) {
        super(k, floatPropertyCompat, new FlingModelBase(f, f2));
        ((FlingModelBase) getmModel()).setValueThreshold(b());
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.BaseDecelerateAnimation
    public void a() {
        ((FlingModelBase) this.mModel).sanityCheck();
    }

    public HWFlingAnimation setInitVelocity(float f) {
        ((FlingModelBase) this.mModel).setInitVelocity(f);
        return this;
    }

    public HWFlingAnimation setFriction(float f) {
        ((FlingModelBase) this.mModel).setFriction(f);
        return this;
    }
}
