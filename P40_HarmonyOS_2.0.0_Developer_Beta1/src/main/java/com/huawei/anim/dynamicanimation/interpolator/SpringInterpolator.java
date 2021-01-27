package com.huawei.anim.dynamicanimation.interpolator;

import android.util.Log;
import com.huawei.anim.dynamicanimation.FloatPropertyCompat;
import com.huawei.anim.dynamicanimation.FloatValueHolder;
import com.huawei.anim.dynamicanimation.OutputData;
import com.huawei.anim.dynamicanimation.PhysicalModelBase;
import com.huawei.anim.dynamicanimation.SpringModelBase;
import com.huawei.anim.dynamicanimation.util.Utils;

public class SpringInterpolator extends PhysicalInterpolatorBase<SpringInterpolator> {
    private static final String d = "SpringInterpolator";
    private static final int e = -1;
    private static final float f = 1000.0f;

    public SpringInterpolator(FloatValueHolder floatValueHolder) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(800.0f, 15.0f, a());
        springModelBase.setValueThreshold(Math.abs(1.0f) * SpringModelBase.DEFAULT_VALUE_THRESHOLD);
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(1.0f, 0.0f, -1);
        setModel(springModelBase);
    }

    public SpringInterpolator() {
        this(new FloatValueHolder(0.0f));
    }

    public SpringInterpolator(FloatValueHolder floatValueHolder, float f2, float f3) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, a());
        springModelBase.setValueThreshold(Math.abs(1.0f) * SpringModelBase.DEFAULT_VALUE_THRESHOLD);
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(1.0f, 0.0f, -1);
        setModel(springModelBase);
    }

    public SpringInterpolator(float f2, float f3) {
        this(new FloatValueHolder(0.0f), f2, f3);
    }

    public SpringInterpolator(FloatValueHolder floatValueHolder, float f2, float f3, float f4) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, a());
        springModelBase.setValueThreshold(Math.abs(f4 - 0.0f) * SpringModelBase.DEFAULT_VALUE_THRESHOLD);
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(f4, 0.0f, -1);
        setModel(springModelBase);
    }

    public SpringInterpolator(float f2, float f3, float f4) {
        this(new FloatValueHolder(0.0f), f2, f3, f4);
    }

    public SpringInterpolator(FloatValueHolder floatValueHolder, float f2, float f3, float f4, float f5) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, a());
        springModelBase.setValueThreshold(Math.abs(f4 - 0.0f) * SpringModelBase.DEFAULT_VALUE_THRESHOLD);
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(f4, f5, -1);
        setModel(springModelBase);
    }

    public SpringInterpolator(float f2, float f3, float f4, float f5) {
        this(new FloatValueHolder(0.0f), f2, f3, f4, f5);
    }

    public SpringInterpolator(FloatValueHolder floatValueHolder, float f2, float f3, float f4, float f5, float f6) {
        super(floatValueHolder, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, f6 * 0.75f);
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(f4, f5, -1);
        setModel(springModelBase);
    }

    public SpringInterpolator(float f2, float f3, float f4, float f5, float f6) {
        this(new FloatValueHolder(0.0f), f2, f3, f4, f5, f6);
    }

    public <K> SpringInterpolator(FloatValueHolder floatValueHolder, SpringModelBase springModelBase) {
        super(floatValueHolder, springModelBase);
        setModel(springModelBase);
    }

    public <K> SpringInterpolator(FloatPropertyCompat<K> floatPropertyCompat) {
        super(floatPropertyCompat, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(800.0f, 15.0f, a());
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(1.0f, 0.0f, -1);
        setModel(springModelBase);
    }

    public <K> SpringInterpolator(FloatPropertyCompat<K> floatPropertyCompat, float f2, float f3) {
        super(floatPropertyCompat, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, a());
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(1.0f, 0.0f, -1);
        setModel(springModelBase);
    }

    public <K> SpringInterpolator(FloatPropertyCompat<K> floatPropertyCompat, float f2, float f3, float f4) {
        super(floatPropertyCompat, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, a());
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(f4, 0.0f, -1);
        setModel(springModelBase);
    }

    public <K> SpringInterpolator(FloatPropertyCompat<K> floatPropertyCompat, float f2, float f3, float f4, float f5) {
        super(floatPropertyCompat, (PhysicalModelBase) null);
        SpringModelBase springModelBase = new SpringModelBase(f2, f3, a());
        springModelBase.snap(0.0f);
        springModelBase.setEndPosition(f4, f5, -1);
        setModel(springModelBase);
    }

    public <K> SpringInterpolator(FloatPropertyCompat<K> floatPropertyCompat, SpringModelBase springModelBase) {
        super(floatPropertyCompat, springModelBase);
        setModel(springModelBase);
    }

    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public SpringInterpolator setValueThreshold(float f2) {
        PhysicalModelBase model = getModel();
        if (model == null) {
            return this;
        }
        model.setValueThreshold(f2 * 0.75f);
        return this;
    }

    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase
    public float getEndOffset() {
        return getModel().getEndPosition() - getModel().getStartPosition();
    }

    public OutputData getInterpolateData(float f2) {
        float duration = (f2 * getDuration()) / f;
        return new OutputData(duration, getModel().getPosition(duration), getModel().getVelocity(duration), getModel().getAcceleration(duration));
    }

    @Override // com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase, android.animation.TimeInterpolator
    public float getInterpolation(float f2) {
        super.getInterpolation(f2);
        if (Float.compare(f2, 1.0f) == 0) {
            return 1.0f;
        }
        float duration = (f2 * getDuration()) / f;
        float position = getModel().getPosition(duration);
        if (getModel().isAtEquilibrium(duration)) {
            Log.i(d, "done at" + duration + "");
        }
        float endPosition = getModel().getEndPosition() - getModel().getStartPosition();
        float f3 = 0.0f;
        if (getModel() instanceof SpringModelBase) {
            f3 = Math.abs(((SpringModelBase) getModel()).getFirstExtremumX());
        }
        float f4 = f3 + endPosition;
        if (Utils.isFloatZero(endPosition)) {
            return (position + f4) / f4;
        }
        return position / endPosition;
    }
}
