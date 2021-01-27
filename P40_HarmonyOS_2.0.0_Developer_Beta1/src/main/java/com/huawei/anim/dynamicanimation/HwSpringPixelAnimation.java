package com.huawei.anim.dynamicanimation;

import com.huawei.anim.dynamicanimation.DynamicAnimation;

public class HwSpringPixelAnimation extends HWSpringAnimation {
    private float a = 0.0f;

    public HwSpringPixelAnimation(FloatValueHolder floatValueHolder) {
        super(floatValueHolder);
    }

    public <K> HwSpringPixelAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        super(k, floatPropertyCompat);
    }

    public <K> HwSpringPixelAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, SpringModel springModel) {
        super(k, floatPropertyCompat, springModel);
    }

    public <K> HwSpringPixelAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, float f, float f2, float f3, float f4) {
        super(k, floatPropertyCompat, f, f2, f3, f4);
    }

    public <K> HwSpringPixelAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, float f, float f2, float f3, float f4, float f5) {
        super(k, floatPropertyCompat, f, f2, f3, f4, f5);
    }

    public <K> HwSpringPixelAnimation(FloatValueHolder floatValueHolder, float f, float f2, float f3, float f4) {
        super(floatValueHolder, f, f2, f3, f4);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.HWSpringAnimation, com.huawei.anim.dynamicanimation.DynamicAnimation
    public boolean a(long j) {
        DynamicAnimation.a updateValues = getSpringModel().updateValues(j);
        float startValue = updateValues.a + getStartValue();
        float f = startValue - this.a;
        if (Math.abs(f) >= 1.0f) {
            this.mValue = startValue;
        } else {
            this.mValue += Math.signum(f) * 1.0f;
        }
        this.a = startValue;
        this.mVelocity = updateValues.b;
        if (!b(this.mValue - getStartValue(), this.mVelocity) && !b(startValue - getStartValue(), this.mVelocity)) {
            return false;
        }
        this.mValue = getSpringModel().getEndPosition() + getStartValue();
        this.mVelocity = 0.0f;
        return true;
    }
}
