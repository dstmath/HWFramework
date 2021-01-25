package com.huawei.anim.dynamicanimation;

import com.huawei.anim.dynamicanimation.util.LogX;
import com.huawei.anim.dynamicanimation.util.Utils;

public class FlingModelBase extends PhysicalModelBase {
    private static final float DEFAULT_VALUE_THRESHOLD = 0.75f;
    private static final float FRICTION_SCALE = -4.2f;
    private static final String TAG = FlingModelBase.class.getSimpleName();
    private float currentT;
    private float estimateTime;
    private float estimateValue;
    private float friction;
    private float initVelocity;
    private boolean isDirty;
    private float signum;

    public FlingModelBase(float initVelocity2, float friction2) {
        this(initVelocity2, friction2, 0.75f);
    }

    public FlingModelBase(float initVelocity2, float friction2, float minThreshold) {
        this.currentT = 0.0f;
        this.isDirty = true;
        super.setValueThreshold(minThreshold);
        setInitVelocity(initVelocity2);
        setFriction(friction2);
    }

    private void reset() {
        if (this.isDirty) {
            sanityCheck();
            this.estimateTime = ((float) (Math.log((double) (this.mVelocityThreshold / this.initVelocity)) / ((double) this.friction))) * 1000.0f;
            this.estimateTime = Math.max(this.estimateTime, 0.0f);
            this.estimateValue = getX(this.estimateTime / 1000.0f);
            this.isDirty = false;
            String str = TAG;
            LogX.i(str, "reset: estimateTime=" + this.estimateTime + ",estimateValue=" + this.estimateValue);
        }
    }

    public void sanityCheck() {
        if (Utils.isFloatZero(this.initVelocity)) {
            throw new UnsupportedOperationException("InitVelocity should be set and can not be 0!!");
        } else if (Utils.isFloatZero(this.friction)) {
            throw new UnsupportedOperationException("Friction should be set and can not be 0!!");
        }
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getX(float t) {
        this.currentT = t;
        float f = this.signum;
        float f2 = this.initVelocity;
        float f3 = this.friction;
        return f * ((float) (((double) (f2 / f3)) * (Math.exp((double) (f3 * t)) - 1.0d)));
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getX() {
        return getX(this.currentT);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getDX(float dt) {
        return this.signum * ((float) (((double) this.initVelocity) * Math.exp((double) (this.friction * dt))));
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getDX() {
        return getDX(this.currentT);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getDDX(float dt) {
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getDDX() {
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public boolean isAtEquilibrium(float dt) {
        return false;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public boolean isAtEquilibrium() {
        return this.initVelocity < this.mVelocityThreshold;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public boolean isAtEquilibrium(float x, float v) {
        return Math.abs(x - getEndPosition()) < this.mValueThreshold && Math.abs(v) < this.mVelocityThreshold;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getEstimatedDuration() {
        reset();
        return this.estimateTime;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getEndPosition() {
        reset();
        return this.estimateValue;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public final PhysicalModelBase setValueThreshold(float v) {
        super.setValueThreshold(v);
        this.isDirty = true;
        return this;
    }

    public final <T extends PhysicalModelBase> T setInitVelocity(float initVelocity2) {
        this.initVelocity = Math.abs(initVelocity2);
        this.signum = Math.signum(initVelocity2);
        this.isDirty = true;
        return this;
    }

    public final <T extends PhysicalModelBase> T setFriction(float friction2) {
        this.friction = FRICTION_SCALE * friction2;
        this.isDirty = true;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.IPhysicalModel
    public float getMaxAbsX() {
        reset();
        return this.estimateValue;
    }
}
