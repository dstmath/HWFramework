package com.huawei.anim.dynamicanimation.interpolator;

import com.huawei.anim.dynamicanimation.OutputData;
import com.huawei.anim.dynamicanimation.PhysicalModelBase;
import com.huawei.anim.dynamicanimation.SpringModelBase;
import com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat;
import com.huawei.anim.dynamicanimation.animation.OscarFloatValueHolder;
import com.huawei.anim.dynamicanimation.util.LogX;
import com.huawei.anim.dynamicanimation.util.Utils;
import com.huawei.uikit.effect.BuildConfig;

public class SpringInterpolator extends PhysicalInterpolatorBase<SpringInterpolator> {
    private static final String TAG = SpringInterpolator.class.getSimpleName();

    public SpringInterpolator(OscarFloatValueHolder floatValueHolder) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(800.0f, 15.0f, getValueThreshold());
        model.setValueThreshold(Math.abs(1.0f) * 0.001f);
        model.snap(0.0f);
        model.setEndPosition(1.0f, 0.0f, -1);
        setModel(model);
    }

    public SpringInterpolator() {
        this(new OscarFloatValueHolder(0.0f));
    }

    public SpringInterpolator(OscarFloatValueHolder floatValueHolder, float stiffness, float damping) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, getValueThreshold());
        model.setValueThreshold(Math.abs(1.0f) * 0.001f);
        model.snap(0.0f);
        model.setEndPosition(1.0f, 0.0f, -1);
        setModel(model);
    }

    public SpringInterpolator(float stiffness, float damping) {
        this(new OscarFloatValueHolder(0.0f), stiffness, damping);
    }

    public SpringInterpolator(OscarFloatValueHolder floatValueHolder, float stiffness, float damping, float endPos) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, getValueThreshold());
        model.setValueThreshold(Math.abs(endPos - 0.0f) * 0.001f);
        model.snap(0.0f);
        model.setEndPosition(endPos, 0.0f, -1);
        setModel(model);
    }

    public SpringInterpolator(float stiffness, float damping, float endPos) {
        this(new OscarFloatValueHolder(0.0f), stiffness, damping, endPos);
    }

    public SpringInterpolator(OscarFloatValueHolder floatValueHolder, float stiffness, float damping, float endPos, float velocity) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, getValueThreshold());
        model.setValueThreshold(Math.abs(endPos - 0.0f) * 0.001f);
        model.snap(0.0f);
        model.setEndPosition(endPos, velocity, -1);
        setModel(model);
    }

    public SpringInterpolator(float stiffness, float damping, float endPos, float velocity) {
        this(new OscarFloatValueHolder(0.0f), stiffness, damping, endPos, velocity);
    }

    public SpringInterpolator(OscarFloatValueHolder floatValueHolder, float stiffness, float damping, float endPos, float velocity, float valueThreshold) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, 0.75f * valueThreshold);
        model.snap(0.0f);
        model.setEndPosition(endPos, velocity, -1);
        setModel(model);
    }

    public SpringInterpolator(float stiffness, float damping, float endPos, float velocity, float valueThreshold) {
        this(new OscarFloatValueHolder(0.0f), stiffness, damping, endPos, velocity, valueThreshold);
    }

    public <K> SpringInterpolator(OscarFloatValueHolder floatValueHolder, SpringModelBase model) {
        super(floatValueHolder, model);
        setModel(model);
    }

    public <K> SpringInterpolator(OscarFloatPropertyCompat<K> property) {
        super(property, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(800.0f, 15.0f, getValueThreshold());
        model.snap(0.0f);
        model.setEndPosition(1.0f, 0.0f, -1);
        setModel(model);
    }

    public <K> SpringInterpolator(OscarFloatPropertyCompat<K> property, float stiffness, float damping) {
        super(property, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, getValueThreshold());
        model.snap(0.0f);
        model.setEndPosition(1.0f, 0.0f, -1);
        setModel(model);
    }

    public <K> SpringInterpolator(OscarFloatPropertyCompat<K> property, float stiffness, float damping, float endPos) {
        super(property, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, getValueThreshold());
        model.snap(0.0f);
        model.setEndPosition(endPos, 0.0f, -1);
        setModel(model);
    }

    public <K> SpringInterpolator(OscarFloatPropertyCompat<K> property, float stiffness, float damping, float endPos, float velocity) {
        super(property, (PhysicalModelBase) null);
        SpringModelBase model = new SpringModelBase(stiffness, damping, getValueThreshold());
        model.snap(0.0f);
        model.setEndPosition(endPos, velocity, -1);
        setModel(model);
    }

    public <K> SpringInterpolator(OscarFloatPropertyCompat<K> property, SpringModelBase model) {
        super(property, model);
        setModel(model);
    }

    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public SpringInterpolator setValueThreshold(float v) {
        PhysicalModelBase model = getModel();
        if (model == null) {
            return this;
        }
        model.setValueThreshold(0.75f * v);
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public float getEndOffset() {
        return getModel().getEndPosition() - getModel().getStartPosition();
    }

    public OutputData getInterpolateData(float input) {
        float t = (this.mTimeScale * input) / 1000.0f;
        return new OutputData(t, getModel().getX(t), getModel().getDX(t), getModel().getDDX(t));
    }

    /* JADX INFO: Multiple debug info for r0v5 float: [D('iv' float), D('t' float)] */
    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public float getInterpolation(float input) {
        super.getInterpolation(input);
        if (Float.compare(input, 1.0f) == 0) {
            return 1.0f;
        }
        float t = (this.mTimeScale * input) / 1000.0f;
        float x = getModel().getX(t);
        if (getModel().isAtEquilibrium(t)) {
            String str = TAG;
            LogX.d(str, "done at" + t + BuildConfig.FLAVOR);
        }
        float firstExtremeX = Math.abs(((SpringModelBase) getModel()).getFirstExtremumX());
        float displacment0 = getModel().getEndPosition() - getModel().getStartPosition();
        float delta = firstExtremeX + displacment0;
        if (Utils.isFloatZero(displacment0)) {
            return (x + delta) / delta;
        }
        return x / displacment0;
    }
}
