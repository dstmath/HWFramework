package com.huawei.anim.dynamicanimation.animation;

import com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation;

public class HwSpringPixelAnimation extends HWSpringAnimation {
    private float lastValue = 0.0f;

    public HwSpringPixelAnimation(OscarFloatValueHolder startValue) {
        super(startValue);
    }

    public <K> HwSpringPixelAnimation(K object, OscarFloatPropertyCompat<K> property) {
        super(object, property);
    }

    public <K> HwSpringPixelAnimation(K object, OscarFloatPropertyCompat<K> property, SpringModel springModel) {
        super(object, property, springModel);
    }

    public <K> HwSpringPixelAnimation(K object, OscarFloatPropertyCompat<K> property, float stiffness, float damping, float endValue, float velocity) {
        super(object, property, stiffness, damping, endValue, velocity);
    }

    public <K> HwSpringPixelAnimation(K object, OscarFloatPropertyCompat<K> property, float stiffness, float damping, float startValue, float endValue, float velocity) {
        super(object, property, stiffness, damping, startValue, endValue, velocity);
    }

    public <K> HwSpringPixelAnimation(OscarFloatValueHolder startValue, float stiffness, float damping, float endValue, float velocity) {
        super(startValue, stiffness, damping, endValue, velocity);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.HWSpringAnimation, com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public boolean updateValueAndVelocity(long deltaT) {
        OscarDynamicAnimation.MassState massState = getSpringModel().updateValues(deltaT);
        float newValue = massState.mValue + getStartValue();
        float delta = newValue - this.lastValue;
        if (Math.abs(delta) >= 1.0f) {
            this.mValue = newValue;
        } else {
            this.mValue += Math.signum(delta) * 1.0f;
        }
        this.lastValue = newValue;
        this.mVelocity = massState.mVelocity;
        if (!isAtEquilibrium(this.mValue - getStartValue(), this.mVelocity) && !isAtEquilibrium(newValue - getStartValue(), this.mVelocity)) {
            return false;
        }
        this.mValue = getSpringModel().getEndPosition() + getStartValue();
        this.mVelocity = 0.0f;
        return true;
    }
}
