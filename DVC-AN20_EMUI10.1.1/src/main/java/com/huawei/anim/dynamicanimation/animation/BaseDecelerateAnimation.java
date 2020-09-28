package com.huawei.anim.dynamicanimation.animation;

import com.huawei.anim.dynamicanimation.PhysicalModelBase;

public abstract class BaseDecelerateAnimation<T extends PhysicalModelBase> extends OscarDynamicAnimation<BaseDecelerateAnimation<T>> {
    private long frameTime;
    private float lastValue = 0.0f;
    T model;

    /* access modifiers changed from: package-private */
    public abstract void sanityCheck();

    <K> BaseDecelerateAnimation(K object, OscarFloatPropertyCompat<K> property, T model2) {
        super(object, property);
        this.model = model2;
        this.model.setValueThreshold(getValueThreshold());
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public boolean updateValueAndVelocity(long deltaT) {
        boolean isFinished = true;
        this.frameTime += deltaT;
        float currentValue = this.model.getX(((float) this.frameTime) / 1000.0f);
        this.mValue += currentValue - this.lastValue;
        this.lastValue = currentValue;
        this.mVelocity = this.model.getDX(((float) this.frameTime) / 1000.0f);
        if (this.mValue < this.mMinValue) {
            this.mValue = this.mMinValue;
        } else if (this.mValue > this.mMaxValue) {
            this.mValue = this.mMaxValue;
        } else {
            isFinished = isAtEquilibrium(this.mValue, this.mVelocity);
        }
        if (isFinished) {
            reset();
        }
        return isFinished;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public float getAcceleration(float v, float v1) {
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public boolean isAtEquilibrium(float x, float v) {
        return this.model.isAtEquilibrium(x, v);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public void setValueThreshold(float v) {
        this.model.setValueThreshold(v);
    }

    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public void cancel() {
        super.cancel();
        reset();
    }

    private void reset() {
        this.frameTime = 0;
        this.lastValue = 0.0f;
    }

    public T getModel() {
        return this.model;
    }

    @Override // com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation
    public void start() {
        if (this.model != null) {
            sanityCheck();
            this.model.setValueThreshold(getValueThreshold());
            super.start();
            return;
        }
        throw new UnsupportedOperationException("Incomplete Animation: Physical Model should be set!");
    }
}
