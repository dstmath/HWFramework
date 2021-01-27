package com.huawei.anim.dynamicanimation;

import com.huawei.anim.dynamicanimation.PhysicalModelBase;

public abstract class BaseDecelerateAnimation<T extends PhysicalModelBase> extends DynamicAnimation<BaseDecelerateAnimation<T>> {
    private static final int a = 1000;
    private long b;
    private float c = 0.0f;
    protected T mModel;

    /* access modifiers changed from: package-private */
    public abstract void a();

    <K> BaseDecelerateAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat, T t) {
        super(k, floatPropertyCompat);
        this.mModel = t;
        this.mModel.setValueThreshold(b());
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x004e  */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public boolean a(long j) {
        boolean z;
        this.b += j;
        float position = this.mModel.getPosition(((float) this.b) / 1000.0f);
        this.mValue += position - this.c;
        this.c = position;
        this.mVelocity = this.mModel.getVelocity(((float) this.b) / 1000.0f);
        if (this.mValue < this.mMinValue) {
            this.mValue = this.mMinValue;
        } else if (this.mValue > this.mMaxValue) {
            this.mValue = this.mMaxValue;
        } else {
            z = b(this.mValue, this.mVelocity);
            if (z) {
                reset();
            }
            return z;
        }
        z = true;
        if (z) {
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public float a(float f, float f2) {
        return 0.0f;
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public boolean b(float f, float f2) {
        return this.mModel.isAtEquilibrium(f, f2);
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public void a(float f) {
        this.mModel.setValueThreshold(f);
    }

    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public void cancel() {
        super.cancel();
        reset();
    }

    public void reset() {
        this.b = 0;
        this.c = 0.0f;
    }

    public T getmModel() {
        return this.mModel;
    }

    @Override // com.huawei.anim.dynamicanimation.DynamicAnimation
    public void start() {
        if (this.mModel != null) {
            a();
            this.mModel.setValueThreshold(b());
            super.start();
            return;
        }
        throw new UnsupportedOperationException("Incomplete Animation: Physical Model should be set!");
    }
}
