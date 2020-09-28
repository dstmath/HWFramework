package com.huawei.anim.dynamicanimation;

public abstract class PhysicalModelBase implements IPhysicalModel {
    public static final float DEFAULT_END_POSITION0 = 0.0f;
    public static final float DEFAULT_END_POSITION1 = 1.0f;
    public static final float DEFAULT_INITIAL_VELOCITY = 0.0f;
    public static final float MAXIMUM_END_POSITION0 = 0.0f;
    public static final float MAXIMUM_END_POSITION1 = 99999.0f;
    public static final float MAXIMUM_INITIAL_VELOCITY = 99999.0f;
    public static final float MINIMUM_END_POSITION0 = 0.0f;
    public static final float MINIMUM_END_POSITION1 = -99999.0f;
    public static final float MINIMUM_INITIAL_VELOCITY = -99999.0f;
    protected static final float VELOCITY_THRESHOLD_MULTIPLIER = 62.5f;
    protected float mEndPosition = 0.0f;
    protected float mStartPosition = 0.0f;
    protected long mStartTime = 0;
    protected float mStartVelocity = 0.0f;
    protected float mValueThreshold = Float.MIN_VALUE;
    protected float mVelocityThreshold = Float.MIN_VALUE;

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getDDX();

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getDDX(float f);

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getDX();

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getDX(float f);

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getEstimatedDuration();

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getMaxAbsX();

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getX();

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract float getX(float f);

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract boolean isAtEquilibrium();

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract boolean isAtEquilibrium(float f);

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public abstract boolean isAtEquilibrium(float f, float f2);

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public PhysicalModelBase setValueThreshold(float v) {
        this.mValueThreshold = Math.abs(v);
        this.mVelocityThreshold = this.mValueThreshold * VELOCITY_THRESHOLD_MULTIPLIER;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getEndPosition() {
        return this.mEndPosition;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public PhysicalModelBase setEndPosition(float pos) {
        this.mEndPosition = pos;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getStartTime() {
        return (float) this.mStartTime;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getStartPosition() {
        return this.mStartPosition;
    }

    @Override // com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getStartVelocity() {
        return this.mStartVelocity;
    }
}
