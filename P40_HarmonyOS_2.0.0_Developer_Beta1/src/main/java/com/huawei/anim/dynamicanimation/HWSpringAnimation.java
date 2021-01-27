package com.huawei.anim.dynamicanimation;

import com.huawei.anim.dynamicanimation.DynamicAnimation;

public class HWSpringAnimation extends DynamicAnimation<HWSpringAnimation> {
    private static final int a = -1;
    private static final float b = Float.MAX_VALUE;
    private SpringModel c;
    private float d = 0.0f;
    private float e = 0.0f;
    private float f = b;

    public HWSpringAnimation(FloatValueHolder floatValueHolder) {
        super(floatValueHolder);
    }

    public <K> HWSpringAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        super(k, floatPropertyCompat);
    }

    public <K> HWSpringAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, SpringModel springModel) {
        super(k, floatPropertyCompat);
        this.c = springModel;
        this.d = floatPropertyCompat.getValue(k);
        this.c.setValueThreshold(b()).snap(0.0f);
    }

    public <K> HWSpringAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, float f2, float f3, float f4, float f5) {
        super(k, floatPropertyCompat);
        setStartVelocity(f5);
        this.e = f4;
        this.d = floatPropertyCompat.getValue(k);
        this.c = new SpringModel(f2, f3);
        this.c.setValueThreshold(b()).snap(0.0f).setEndPosition(f4 - this.d, f5, -1);
    }

    public <K> HWSpringAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, float f2, float f3, float f4, float f5, float f6) {
        super(k, floatPropertyCompat);
        super.setStartValue(f4);
        setStartVelocity(f6);
        this.d = f4;
        this.e = f5;
        this.c = new SpringModel(f2, f3);
        this.c.setValueThreshold(b()).snap(0.0f).setEndPosition(f5 - this.d, f6, -1);
    }

    public <K> HWSpringAnimation(FloatValueHolder floatValueHolder, float f2, float f3, float f4, float f5) {
        super(floatValueHolder);
        setStartVelocity(f5);
        this.e = f4;
        this.d = floatValueHolder.getValue();
        this.c = new SpringModel(f2, f3);
        this.c.setValueThreshold(Math.abs(f4 - floatValueHolder.getValue()) * SpringModelBase.DEFAULT_VALUE_THRESHOLD);
        this.c.snap(0.0f);
        this.c.setEndPosition(f4 - this.d, f5, -1);
    }

    public HWSpringAnimation reset() {
        this.mTarget = null;
        this.mProperty = null;
        setStartVelocity(0.0f);
        this.e = 0.0f;
        this.d = 0.0f;
        this.c.reset().snap(0.0f).setEndPosition(1.0f, 0.0f, -1);
        a.a().a(this);
        return (HWSpringAnimation) super.clearListeners();
    }

    public <K> HWSpringAnimation setObj(K k, FloatPropertyCompat<K> floatPropertyCompat, float f2, float f3, float f4, float f5) {
        super.setObj(k, floatPropertyCompat);
        setStartVelocity(f5);
        this.e = f4;
        if (this.mTarget == null) {
            if (this.mProperty == null) {
                final FloatValueHolder floatValueHolder = new FloatValueHolder(0.0f);
                this.mProperty = new FloatPropertyCompat("FloatValueHolder") {
                    /* class com.huawei.anim.dynamicanimation.HWSpringAnimation.AnonymousClass1 */

                    @Override // com.huawei.anim.dynamicanimation.FloatPropertyCompat
                    public float getValue(Object obj) {
                        return floatValueHolder.getValue();
                    }

                    @Override // com.huawei.anim.dynamicanimation.FloatPropertyCompat
                    public void setValue(Object obj, float f) {
                        floatValueHolder.setValue(f);
                    }
                };
            } else {
                this.mProperty.setValue(this.mTarget, 0.0f);
            }
            this.d = 0.0f;
        } else {
            this.d = this.mProperty.getValue(this.mTarget);
        }
        this.c.reset().setStiffness(f2).setDamping(f3).snap(0.0f).setEndPosition(f4 - this.d, f5, -1);
        return this;
    }

    public HWSpringAnimation endToPosition(float f2, float f3) {
        if (isRunning()) {
            this.f = f2;
        } else {
            this.f = b;
            if (!this.mIsStartValueIsSet) {
                this.d = this.mProperty.getValue(this.mTarget);
            }
            setStartVelocity(f3);
            this.e = f2;
            getSpringModel().reset().snap(0.0f).setEndPosition(this.e - this.d, f3, -1);
            start();
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public boolean a(long j) {
        float f2 = this.f;
        if (f2 != b) {
            this.e = f2;
            this.f = b;
            setStartVelocity(this.mVelocity);
            this.d = this.mProperty.getValue(this.mTarget);
            this.c.setEndValue(this.e - this.d, this.mVelocity);
            DynamicAnimation.a updateValues = this.c.updateValues(j / 2);
            this.mValue = updateValues.a + this.d;
            this.mVelocity = updateValues.b;
            return false;
        }
        DynamicAnimation.a updateValues2 = this.c.updateValues(j);
        this.mValue = updateValues2.a + this.d;
        this.mVelocity = updateValues2.b;
        if (!b(this.mValue - this.d, this.mVelocity)) {
            return false;
        }
        this.mValue = this.c.getEndPosition() + this.d;
        this.mVelocity = 0.0f;
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public float a(float f2, float f3) {
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public void a(float f2) {
        this.c.setValueThreshold(f2);
    }

    public void setSpringModel(SpringModel springModel) {
        this.c = springModel;
    }

    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public void start() {
        super.start();
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public boolean b(float f2, float f3) {
        return this.c.isAtEquilibrium(f2, f3);
    }

    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public HWSpringAnimation setStartValue(float f2) {
        super.setStartValue(f2);
        this.d = f2;
        float startVelocity = this.c.getStartVelocity();
        this.c.snap(0.0f);
        this.c.setEndPosition(this.e - this.d, startVelocity, -1);
        return this;
    }

    public SpringModel getSpringModel() {
        return this.c;
    }

    public float getStartValue() {
        return this.d;
    }
}
