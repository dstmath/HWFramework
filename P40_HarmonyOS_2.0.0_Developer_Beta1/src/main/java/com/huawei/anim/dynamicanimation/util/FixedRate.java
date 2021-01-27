package com.huawei.anim.dynamicanimation.util;

public class FixedRate implements FollowHandRate {
    private float a;

    public FixedRate(float f) {
        this.a = f;
    }

    @Override // com.huawei.anim.dynamicanimation.util.FollowHandRate
    public float getRate(float f) {
        return this.a;
    }

    public FixedRate setK(float f) {
        this.a = f;
        return this;
    }
}
