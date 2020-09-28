package com.huawei.anim.dynamicanimation.interpolator;

import com.huawei.anim.dynamicanimation.FlingModelBase;
import com.huawei.anim.dynamicanimation.OutputData;
import com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat;
import com.huawei.anim.dynamicanimation.animation.OscarFloatValueHolder;

public class FlingInterpolator extends PhysicalInterpolatorBase<FlingInterpolator> {
    public FlingInterpolator(OscarFloatValueHolder floatValueHolder, FlingModelBase model) {
        super(floatValueHolder, model);
        model.setValueThreshold(getValueThreshold());
    }

    public FlingInterpolator(float initVelocity, float friction) {
        super((OscarFloatPropertyCompat) null, new FlingModelBase(initVelocity, friction));
        ((FlingModelBase) getModel()).setValueThreshold(getValueThreshold());
    }

    public <K> FlingInterpolator(OscarFloatPropertyCompat<K> property, float initVelocity, float friction) {
        super(property, new FlingModelBase(initVelocity, friction));
        ((FlingModelBase) getModel()).setValueThreshold(getValueThreshold());
    }

    /* access modifiers changed from: package-private */
    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public FlingInterpolator setValueThreshold(float threshold) {
        getModel().setValueThreshold(0.75f * threshold);
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public float getDeltaX() {
        return getEndOffset();
    }

    public OutputData getInterpolateData(float input) {
        float t = (this.mTimeScale * input) / 1000.0f;
        return new OutputData(t, getModel().getX(t), getModel().getDX(t), getModel().getDDX(t));
    }
}
