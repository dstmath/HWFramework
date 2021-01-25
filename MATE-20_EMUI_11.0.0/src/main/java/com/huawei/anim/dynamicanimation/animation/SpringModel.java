package com.huawei.anim.dynamicanimation.animation;

import com.huawei.anim.dynamicanimation.SpringModelBase;
import com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation;

public class SpringModel extends SpringModelBase {
    private final OscarDynamicAnimation.MassState mMassState;
    private float mTotalT;

    public SpringModel(float springConstant, float damping) {
        super(springConstant, damping);
        this.mTotalT = 0.0f;
        this.mTotalT = 0.0f;
        this.mMassState = new OscarDynamicAnimation.MassState();
    }

    public OscarDynamicAnimation.MassState updateValues(long deltaT) {
        this.mTotalT += (float) deltaT;
        float dt = this.mTotalT / 1000.0f;
        this.mMassState.mValue = getX(dt);
        this.mMassState.mVelocity = getDX(dt);
        return this.mMassState;
    }
}
