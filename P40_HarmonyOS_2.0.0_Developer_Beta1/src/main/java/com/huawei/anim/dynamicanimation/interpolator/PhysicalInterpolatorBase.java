package com.huawei.anim.dynamicanimation.interpolator;

import android.view.animation.Interpolator;
import com.huawei.anim.dynamicanimation.DynamicAnimation;
import com.huawei.anim.dynamicanimation.FloatPropertyCompat;
import com.huawei.anim.dynamicanimation.FloatValueHolder;
import com.huawei.anim.dynamicanimation.PhysicalModelBase;
import com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase;
import java.math.BigDecimal;

public abstract class PhysicalInterpolatorBase<T extends PhysicalInterpolatorBase<T>> implements Interpolator {
    public static final float MIN_VISIBLE_CHANGE_ALPHA = new BigDecimal(1.0d).divide(new BigDecimal("256")).floatValue();
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = new BigDecimal(1.0d).divide(new BigDecimal("10")).floatValue();
    public static final float MIN_VISIBLE_CHANGE_SCALE = new BigDecimal(1.0d).divide(new BigDecimal("500")).floatValue();
    public static final float MIN_VISIBLE_CHANGE_STANDARD_INTERPOLATE = new BigDecimal(1.0d).divide(new BigDecimal("1000")).floatValue();
    protected static final float THRESHOLD_MULTIPLIER = 0.75f;
    private static final float d = Float.MAX_VALUE;
    private static final long e = 300;
    private static final float f = 1000.0f;
    private static final float g = -1.0f;
    final FloatPropertyCompat a;
    float b = d;
    float c = (-this.b);
    private float h;
    private long i = e;
    private PhysicalModelBase j;
    private InterpolatorDataUpdateListener k;
    protected float mTimeScale;

    public interface InterpolatorDataUpdateListener {
        void onDataUpdate(float f, float f2, float f3, float f4);
    }

    /* access modifiers changed from: package-private */
    public abstract T setValueThreshold(float f2);

    PhysicalInterpolatorBase(final FloatValueHolder floatValueHolder, PhysicalModelBase physicalModelBase) {
        this.j = physicalModelBase;
        this.a = new FloatPropertyCompat("FloatValueHolder") {
            /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass1 */

            @Override // com.huawei.anim.dynamicanimation.FloatPropertyCompat
            public float getValue(Object obj) {
                return floatValueHolder.getValue();
            }

            @Override // com.huawei.anim.dynamicanimation.FloatPropertyCompat
            public void setValue(Object obj, float f) {
                floatValueHolder.setValue(f);
            }
        };
        this.h = MIN_VISIBLE_CHANGE_STANDARD_INTERPOLATE;
    }

    <K> PhysicalInterpolatorBase(FloatPropertyCompat<K> floatPropertyCompat, PhysicalModelBase physicalModelBase) {
        this.j = physicalModelBase;
        this.a = floatPropertyCompat;
        if (this.a == DynamicAnimation.ROTATION || this.a == DynamicAnimation.ROTATION_X || this.a == DynamicAnimation.ROTATION_Y) {
            this.h = MIN_VISIBLE_CHANGE_ROTATION_DEGREES;
        } else if (this.a == DynamicAnimation.ALPHA) {
            this.h = MIN_VISIBLE_CHANGE_ALPHA;
        } else if (this.a == DynamicAnimation.SCALE_X || this.a == DynamicAnimation.SCALE_Y) {
            this.h = MIN_VISIBLE_CHANGE_SCALE;
        } else {
            this.h = 1.0f;
        }
    }

    public T setModel(PhysicalModelBase physicalModelBase) {
        this.j = physicalModelBase;
        return this;
    }

    public final <T extends PhysicalModelBase> T getModel() {
        return (T) this.j;
    }

    public T setMaxValue(float f2) {
        this.b = f2;
        return this;
    }

    public T setMinValue(float f2) {
        this.c = f2;
        return this;
    }

    public T setMinimumVisibleChange(float f2) {
        if (f2 > 0.0f) {
            this.h = f2;
            setValueThreshold(f2);
            return this;
        }
        throw new IllegalArgumentException("Minimum visible change must be positive.");
    }

    /* access modifiers changed from: package-private */
    public final float a() {
        return this.h * 0.75f;
    }

    public T setDuration(long j2) {
        if (j2 >= 0) {
            this.i = j2;
            return this;
        }
        throw new IllegalArgumentException("Animators cannot have negative duration: " + j2);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r1v1, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r2v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float f2) {
        float duration = (f2 * getDuration()) / f;
        float position = getModel().getPosition(duration);
        if (this.k != null) {
            this.k.onDataUpdate(duration, position, getModel().getVelocity(duration), getModel().getAcceleration(duration));
        }
        return position / getDeltaX();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r1v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* access modifiers changed from: protected */
    public float getDeltaX() {
        return Math.abs(getModel().getEndPosition() - getModel().getStartPosition());
    }

    /* JADX WARN: Type inference failed for: r0v19, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r1v1, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r2v4, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r5v5, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    public float getInterpolation2(float f2) {
        if (this.i < 0 || f2 < this.j.getStartTime() || f2 > this.j.getStartTime() + ((float) this.i) || getDuration() == 0.0f || getDuration() == -1.0f) {
            return 0.0f;
        }
        float startTime = (((f2 - this.j.getStartTime()) / ((float) this.i)) * getDuration()) / f;
        float position = getModel().getPosition(startTime);
        this.k.onDataUpdate(startTime, position, getModel().getVelocity(startTime), getModel().getAcceleration(startTime));
        return position / Math.abs(getModel().getEndPosition());
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    public float getDuration() {
        return getModel().getEstimatedDuration();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    public float getEndOffset() {
        return getModel().getEndPosition();
    }

    public T setDataUpdateListener(InterpolatorDataUpdateListener interpolatorDataUpdateListener) {
        this.k = interpolatorDataUpdateListener;
        return this;
    }
}
