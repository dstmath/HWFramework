package com.huawei.anim.dynamicanimation.animation;

import com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation;

public class HWSpringAnimation extends OscarDynamicAnimation<HWSpringAnimation> {
    private float mEndValue = 0.0f;
    private SpringModel mSpringModel;
    private float mStartValue = 0.0f;

    public HWSpringAnimation(OscarFloatValueHolder startValue) {
        super(startValue);
    }

    public <K> HWSpringAnimation(K object, OscarFloatPropertyCompat<K> property) {
        super(object, property);
    }

    public <K> HWSpringAnimation(K object, OscarFloatPropertyCompat<K> property, SpringModel springModel) {
        super(object, property);
        this.mSpringModel = springModel;
    }

    public <K> HWSpringAnimation(K object, OscarFloatPropertyCompat<K> property, float stiffness, float damping, float endValue, float velocity) {
        super(object, property);
        setStartVelocity(velocity);
        this.mEndValue = endValue;
        this.mStartValue = property.getValue(object);
        this.mSpringModel = new SpringModel(stiffness, damping);
        this.mSpringModel.setValueThreshold(getValueThreshold()).snap(0.0f).setEndPosition(endValue - this.mStartValue, velocity, -1);
    }

    public <K> HWSpringAnimation(K object, OscarFloatPropertyCompat<K> property, float stiffness, float damping, float startValue, float endValue, float velocity) {
        super(object, property);
        super.setStartValue(startValue);
        setStartVelocity(velocity);
        this.mStartValue = startValue;
        this.mEndValue = endValue;
        this.mSpringModel = new SpringModel(stiffness, damping);
        this.mSpringModel.setValueThreshold(getValueThreshold()).snap(0.0f).setEndPosition(endValue - this.mStartValue, velocity, -1);
    }

    public <K> HWSpringAnimation(OscarFloatValueHolder startValue, float stiffness, float damping, float endValue, float velocity) {
        super(startValue);
        setStartVelocity(velocity);
        this.mEndValue = endValue;
        this.mStartValue = startValue.getValue();
        this.mSpringModel = new SpringModel(stiffness, damping);
        this.mSpringModel.setValueThreshold(Math.abs(endValue - startValue.getValue()) * 0.001f);
        this.mSpringModel.snap(0.0f);
        this.mSpringModel.setEndPosition(endValue - this.mStartValue, velocity, -1);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public boolean updateValueAndVelocity(long deltaT) {
        OscarDynamicAnimation.MassState massState = this.mSpringModel.updateValues(deltaT);
        this.mValue = massState.mValue + this.mStartValue;
        this.mVelocity = massState.mVelocity;
        if (!isAtEquilibrium(this.mValue - this.mStartValue, this.mVelocity)) {
            return false;
        }
        this.mValue = this.mSpringModel.getEndPosition() + this.mStartValue;
        this.mVelocity = 0.0f;
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public float getAcceleration(float v, float v1) {
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public void setValueThreshold(float v) {
        this.mSpringModel.setValueThreshold(v);
    }

    public void setSpringModel(SpringModel springModel) {
        this.mSpringModel = springModel;
    }

    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public void start() {
        super.start();
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public boolean isAtEquilibrium(float value, float velocity) {
        return this.mSpringModel.isAtEquilibrium(value, velocity);
    }

    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public HWSpringAnimation setStartValue(float startValue) {
        super.setStartValue(startValue);
        this.mStartValue = startValue;
        float startVelocity = this.mSpringModel.getStartVelocity();
        this.mSpringModel.snap(0.0f);
        this.mSpringModel.setEndPosition(this.mEndValue - this.mStartValue, startVelocity, -1);
        return this;
    }

    public SpringModel getSpringModel() {
        return this.mSpringModel;
    }

    public float getStartValue() {
        return this.mStartValue;
    }
}
