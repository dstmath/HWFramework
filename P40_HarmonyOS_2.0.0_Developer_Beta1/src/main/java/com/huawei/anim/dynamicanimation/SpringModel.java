package com.huawei.anim.dynamicanimation;

import com.huawei.anim.dynamicanimation.DynamicAnimation;

public class SpringModel extends SpringModelBase {
    private static final float a = 1000.0f;
    private final DynamicAnimation.a b;
    private float c;

    public SpringModel(float f, float f2) {
        super(f, f2, DEFAULT_VALUE_THRESHOLD);
        this.c = 0.0f;
        this.c = 0.0f;
        this.b = new DynamicAnimation.a();
    }

    public SpringModel reset() {
        this.c = 0.0f;
        DynamicAnimation.a aVar = this.b;
        aVar.a = 0.0f;
        aVar.b = 0.0f;
        return this;
    }

    public DynamicAnimation.a updateValues(long j) {
        this.c += (float) j;
        float f = this.c / a;
        this.b.a = getPosition(f);
        this.b.b = getVelocity(f);
        return this.b;
    }
}
