package com.huawei.anim.dynamicanimation.interpolator;

import android.view.View;
import android.view.animation.Interpolator;
import com.huawei.anim.dynamicanimation.PhysicalModelBase;
import com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat;
import com.huawei.anim.dynamicanimation.animation.OscarFloatValueHolder;
import com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase;
import com.huawei.anim.dynamicanimation.util.OscarViewCompat;

public abstract class PhysicalInterpolatorBase<T extends PhysicalInterpolatorBase<T>> implements Interpolator {
    public static final ViewProperty ALPHA = new ViewProperty("alpha") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass12 */

        public void setValue(View view, float value) {
            view.setAlpha(value);
        }

        public float getValue(View view) {
            return view.getAlpha();
        }
    };
    public static final float MIN_VISIBLE_CHANGE_ALPHA = 0.00390625f;
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = 0.1f;
    public static final float MIN_VISIBLE_CHANGE_SCALE = 0.002f;
    public static final float MIN_VISIBLE_CHANGE_STANDARD_INTERPOLATE = 0.001f;
    public static final ViewProperty ROTATION = new ViewProperty("rotation") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass6 */

        public void setValue(View view, float value) {
            view.setRotation(value);
        }

        public float getValue(View view) {
            return view.getRotation();
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass7 */

        public void setValue(View view, float value) {
            view.setRotationX(value);
        }

        public float getValue(View view) {
            return view.getRotationX();
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass8 */

        public void setValue(View view, float value) {
            view.setRotationY(value);
        }

        public float getValue(View view) {
            return view.getRotationY();
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass4 */

        public void setValue(View view, float value) {
            view.setScaleX(value);
        }

        public float getValue(View view) {
            return view.getScaleX();
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass5 */

        public void setValue(View view, float value) {
            view.setScaleY(value);
        }

        public float getValue(View view) {
            return view.getScaleY();
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass13 */

        public void setValue(View view, float value) {
            view.setScrollX((int) value);
        }

        public float getValue(View view) {
            return (float) view.getScrollX();
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass14 */

        public void setValue(View view, float value) {
            view.setScrollY((int) value);
        }

        public float getValue(View view) {
            return (float) view.getScrollY();
        }
    };
    protected static final float THRESHOLD_MULTIPLIER = 0.75f;
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass1 */

        public void setValue(View view, float value) {
            view.setTranslationX(value);
        }

        public float getValue(View view) {
            return view.getTranslationX();
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass2 */

        public void setValue(View view, float value) {
            view.setTranslationY(value);
        }

        public float getValue(View view) {
            return view.getTranslationY();
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass3 */

        public void setValue(View view, float value) {
            OscarViewCompat.setTranslationZ(view, value);
        }

        public float getValue(View view) {
            return OscarViewCompat.getTranslationZ(view);
        }
    };
    private static final float UNSET = Float.MAX_VALUE;
    public static final ViewProperty X = new ViewProperty("x") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass9 */

        public void setValue(View view, float value) {
            view.setX(value);
        }

        public float getValue(View view) {
            return view.getX();
        }
    };
    public static final ViewProperty Y = new ViewProperty("y") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass10 */

        public void setValue(View view, float value) {
            view.setY(value);
        }

        public float getValue(View view) {
            return view.getY();
        }
    };
    public static final ViewProperty Z = new ViewProperty("z") {
        /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass11 */

        public void setValue(View view, float value) {
            OscarViewCompat.setZ(view, value);
        }

        public float getValue(View view) {
            return OscarViewCompat.getZ(view);
        }
    };
    private InterpolatorDataUpdateListener mDataUpdateListener;
    private long mDuration = 300;
    float mMaxValue = UNSET;
    float mMinValue = (-this.mMaxValue);
    private float mMinVisibleChange;
    private PhysicalModelBase mModel;
    final OscarFloatPropertyCompat mProperty;
    protected float mTimeScale;

    public interface InterpolatorDataUpdateListener {
        void onDataUpdate(float f, float f2, float f3, float f4);
    }

    /* access modifiers changed from: package-private */
    public abstract T setValueThreshold(float f);

    public static abstract class ViewProperty extends OscarFloatPropertyCompat<View> {
        private ViewProperty(String name) {
            super(name);
        }
    }

    PhysicalInterpolatorBase(final OscarFloatValueHolder floatValueHolder, PhysicalModelBase model) {
        this.mModel = model;
        PhysicalModelBase physicalModelBase = this.mModel;
        if (physicalModelBase != null) {
            this.mTimeScale = physicalModelBase.getEstimatedDuration() * 1.0f;
        }
        this.mProperty = new OscarFloatPropertyCompat("FloatValueHolder") {
            /* class com.huawei.anim.dynamicanimation.interpolator.PhysicalInterpolatorBase.AnonymousClass15 */

            @Override // com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat
            public float getValue(Object object) {
                return floatValueHolder.getValue();
            }

            @Override // com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat
            public void setValue(Object object, float value) {
                floatValueHolder.setValue(value);
            }
        };
        this.mMinVisibleChange = 0.001f;
    }

    <K> PhysicalInterpolatorBase(OscarFloatPropertyCompat<K> property, PhysicalModelBase model) {
        this.mModel = model;
        PhysicalModelBase physicalModelBase = this.mModel;
        if (physicalModelBase != null) {
            this.mTimeScale = physicalModelBase.getEstimatedDuration() * 1.0f;
        }
        this.mProperty = property;
        OscarFloatPropertyCompat oscarFloatPropertyCompat = this.mProperty;
        if (oscarFloatPropertyCompat == ROTATION || oscarFloatPropertyCompat == ROTATION_X || oscarFloatPropertyCompat == ROTATION_Y) {
            this.mMinVisibleChange = 0.1f;
        } else if (oscarFloatPropertyCompat == ALPHA) {
            this.mMinVisibleChange = 0.00390625f;
        } else if (oscarFloatPropertyCompat == SCALE_X || oscarFloatPropertyCompat == SCALE_Y) {
            this.mMinVisibleChange = 0.002f;
        } else {
            this.mMinVisibleChange = 1.0f;
        }
    }

    public T setModel(PhysicalModelBase model) {
        this.mModel = model;
        PhysicalModelBase physicalModelBase = this.mModel;
        if (physicalModelBase != null) {
            this.mTimeScale = physicalModelBase.getEstimatedDuration() * 1.0f;
        }
        return this;
    }

    public final <T extends PhysicalModelBase> T getModel() {
        return (T) this.mModel;
    }

    public T setMaxValue(float max) {
        this.mMaxValue = max;
        return this;
    }

    public T setMinValue(float min) {
        this.mMinValue = min;
        return this;
    }

    public T setMinimumVisibleChange(float minimumVisibleChange) {
        if (minimumVisibleChange > 0.0f) {
            this.mMinVisibleChange = minimumVisibleChange;
            setValueThreshold(minimumVisibleChange);
            return this;
        }
        throw new IllegalArgumentException("Minimum visible change must be positive.");
    }

    /* access modifiers changed from: package-private */
    public final float getValueThreshold() {
        return this.mMinVisibleChange * 0.75f;
    }

    public T setDuration(long duration) {
        if (duration >= 0) {
            this.mDuration = duration;
            return this;
        }
        throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
    }

    /* JADX WARN: Type inference failed for: r1v1, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r2v3, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r3v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float input) {
        float t = (this.mTimeScale * input) / 1000.0f;
        float x = getModel().getX(t);
        if (this.mDataUpdateListener != null) {
            this.mDataUpdateListener.onDataUpdate(t, x, getModel().getDX(t), getModel().getDDX(t));
        }
        return x / getDeltaX();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r1v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* access modifiers changed from: protected */
    public float getDeltaX() {
        return Math.abs(getModel().getEndPosition() - getModel().getStartPosition());
    }

    /* JADX WARN: Type inference failed for: r6v1, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r2v6, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r3v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    /* JADX WARN: Type inference failed for: r4v1, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    public float getInterpolation2(float t) {
        if (this.mDuration < 0 || t < this.mModel.getStartTime() || t > this.mModel.getStartTime() + ((float) this.mDuration)) {
            return 0.0f;
        }
        float f = this.mTimeScale;
        if (f == 0.0f || f == -1.0f) {
            return 0.0f;
        }
        float t2 = (this.mTimeScale * ((t - this.mModel.getStartTime()) / ((float) this.mDuration))) / 1000.0f;
        float x = getModel().getX(t2);
        this.mDataUpdateListener.onDataUpdate(t2, x, getModel().getDX(t2), getModel().getDDX(t2));
        return x / Math.abs(getModel().getEndPosition());
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    public float getDuration() {
        return getModel().getEstimatedDuration();
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.huawei.anim.dynamicanimation.PhysicalModelBase] */
    public float getEndOffset() {
        return getModel().getEndPosition();
    }

    public T setDataUpdateListener(InterpolatorDataUpdateListener dataUpdateListener) {
        this.mDataUpdateListener = dataUpdateListener;
        return this;
    }
}
