package com.huawei.anim.dynamicanimation;

import android.util.Log;
import com.huawei.anim.dynamicanimation.util.Utils;

public class FlingModelBase extends PhysicalModelBase {
    private static final String a = "FlingModelBase";
    private static final float b = -4.2f;
    private static final float c = 0.75f;
    private static final int d = 1000;
    private float e;
    private float f;
    private float g;
    private float h;
    private float i;
    private float j;
    private boolean k;

    public FlingModelBase(float f2, float f3) {
        this(f2, f3, 0.75f);
    }

    public FlingModelBase(float f2, float f3, float f4) {
        this.i = 0.0f;
        this.k = true;
        super.setValueThreshold(f4);
        setInitVelocity(f2);
        setFriction(f3);
    }

    private void a() {
        if (this.k) {
            sanityCheck();
            this.g = ((float) (Math.log((double) (this.mVelocityThreshold / this.e)) / ((double) this.f))) * 1000.0f;
            this.g = Math.max(this.g, 0.0f);
            this.h = getPosition(this.g / 1000.0f);
            this.k = false;
            Log.i(a, "reset: estimateTime=" + this.g + ",estimateValue=" + this.h);
        }
    }

    public void sanityCheck() {
        if (Utils.isFloatZero(this.e)) {
            throw new UnsupportedOperationException("InitVelocity should be set and can not be 0!!");
        } else if (Utils.isFloatZero(this.f)) {
            throw new UnsupportedOperationException("Friction should be set and can not be 0!!");
        }
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getPosition(float f2) {
        this.i = f2;
        float f3 = this.j;
        float f4 = this.e;
        float f5 = this.f;
        return f3 * ((float) (((double) (f4 / f5)) * (Math.exp((double) (f5 * f2)) - 1.0d)));
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getPosition() {
        return getPosition(this.i);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getVelocity(float f2) {
        return this.j * ((float) (((double) this.e) * Math.exp((double) (this.f * f2))));
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getVelocity() {
        return getVelocity(this.i);
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getAcceleration(float f2) {
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getAcceleration() {
        return 0.0f;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public boolean isAtEquilibrium(float f2) {
        return false;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public boolean isAtEquilibrium() {
        return this.e < this.mVelocityThreshold;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public boolean isAtEquilibrium(float f2, float f3) {
        return Math.abs(f2 - getEndPosition()) < this.mValueThreshold && Math.abs(f3) < this.mVelocityThreshold;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getEstimatedDuration() {
        a();
        return this.g;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getEndPosition() {
        a();
        return this.h;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public final PhysicalModelBase setValueThreshold(float f2) {
        super.setValueThreshold(f2);
        this.k = true;
        return this;
    }

    public final <T extends PhysicalModelBase> T setInitVelocity(float f2) {
        this.e = Math.abs(f2);
        this.j = Math.signum(f2);
        this.k = true;
        return this;
    }

    public final <T extends PhysicalModelBase> T setFriction(float f2) {
        this.f = f2 * b;
        this.k = true;
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.PhysicalModelBase, com.huawei.anim.dynamicanimation.PhysicalModel
    public float getMaxAbsX() {
        a();
        return this.h;
    }
}
