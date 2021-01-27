package com.huawei.anim.dynamicanimation;

import java.math.BigDecimal;

public abstract class PhysicalModelBase implements PhysicalModel {
    public static final float DEFAULT_END_POSITION0 = 0.0f;
    public static final float DEFAULT_END_POSITION1 = 1.0f;
    public static final float DEFAULT_INITIAL_VELOCITY = 0.0f;
    public static final float MAXIMUM_END_POSITION0 = 0.0f;
    public static final float MAXIMUM_END_POSITION1 = 99999.0f;
    public static final float MAXIMUM_INITIAL_VELOCITY = 99999.0f;
    public static final float MINIMUM_END_POSITION0 = 0.0f;
    public static final float MINIMUM_END_POSITION1 = -99999.0f;
    public static final float MINIMUM_INITIAL_VELOCITY = -99999.0f;
    protected static final float VELOCITY_THRESHOLD_MULTIPLIER = new BigDecimal("1000").divide(new BigDecimal("16")).floatValue();
    protected float mEndPosition = 0.0f;
    protected float mStartPosition = 0.0f;
    protected long mStartTime = 0;
    protected float mStartVelocity = 0.0f;
    protected float mValueThreshold = Float.MIN_VALUE;
    protected float mVelocityThreshold = Float.MIN_VALUE;

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getAcceleration();

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getAcceleration(float f);

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getEstimatedDuration();

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getMaxAbsX();

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getPosition();

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getPosition(float f);

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getVelocity();

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract float getVelocity(float f);

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract boolean isAtEquilibrium();

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract boolean isAtEquilibrium(float f);

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public abstract boolean isAtEquilibrium(float f, float f2);

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public PhysicalModelBase setValueThreshold(float f) {
        this.mValueThreshold = Math.abs(f);
        this.mVelocityThreshold = this.mValueThreshold * VELOCITY_THRESHOLD_MULTIPLIER;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public float getEndPosition() {
        return this.mEndPosition;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public PhysicalModelBase setEndPosition(float f) {
        this.mEndPosition = f;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public float getStartTime() {
        return (float) this.mStartTime;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public float getStartPosition() {
        return this.mStartPosition;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModel
    public float getStartVelocity() {
        return this.mStartVelocity;
    }
}
