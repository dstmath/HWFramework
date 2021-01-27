package com.huawei.anim.dynamicanimation.interpolator;

import com.huawei.anim.dynamicanimation.DynamicAnimation;
import com.huawei.anim.dynamicanimation.FlingModelBase;
import com.huawei.anim.dynamicanimation.FloatPropertyCompat;
import com.huawei.anim.dynamicanimation.FloatValueHolder;
import com.huawei.anim.dynamicanimation.OutputData;

public class FlingInterpolator extends PhysicalInterpolatorBase<FlingInterpolator> {
    private static final float d = 1000.0f;

    public FlingInterpolator(FloatValueHolder floatValueHolder, FlingModelBase flingModelBase) {
        super(floatValueHolder, flingModelBase);
        flingModelBase.setValueThreshold(a());
    }

    public FlingInterpolator(float f, float f2) {
        super(DynamicAnimation.AXIS_X, new FlingModelBase(f, f2));
        ((FlingModelBase) getModel()).setValueThreshold(a());
    }

    public <K> FlingInterpolator(FloatPropertyCompat<K> floatPropertyCompat, float f, float f2) {
        super(floatPropertyCompat, new FlingModelBase(f, f2));
        ((FlingModelBase) getModel()).setValueThreshold(a());
    }

    /* access modifiers changed from: package-private */
    /* renamed from: a */
    public FlingInterpolator setValueThreshold(float f) {
        getModel().setValueThreshold(f * 0.75f);
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public float getDeltaX() {
        return getEndOffset();
    }

    public OutputData getInterpolateData(float f) {
        float duration = (f * getDuration()) / d;
        return new OutputData(duration, getModel().getPosition(duration), getModel().getVelocity(duration), getModel().getAcceleration(duration));
    }
}
