package com.huawei.anim.dynamicanimation.animation;

import com.huawei.anim.dynamicanimation.FlingModelBase;

public class HWFlingAnimation extends BaseDecelerateAnimation<FlingModelBase> {
    public <K> HWFlingAnimation(K object, OscarFloatPropertyCompat<K> property, float initVelocity, float friction) {
        super(object, property, new FlingModelBase(initVelocity, friction));
        ((FlingModelBase) getModel()).setValueThreshold(getValueThreshold());
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.BaseDecelerateAnimation
    public void sanityCheck() {
        ((FlingModelBase) this.model).sanityCheck();
    }

    public HWFlingAnimation setInitVelocity(float initVelocity) {
        ((FlingModelBase) this.model).setInitVelocity(initVelocity);
        return this;
    }

    public HWFlingAnimation setFriction(float friction) {
        ((FlingModelBase) this.model).setFriction(friction);
        return this;
    }
}
