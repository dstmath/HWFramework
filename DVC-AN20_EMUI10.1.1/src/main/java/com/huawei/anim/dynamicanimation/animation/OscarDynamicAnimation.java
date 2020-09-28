package com.huawei.anim.dynamicanimation.animation;

import android.os.Build;
import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.View;
import com.huawei.anim.dynamicanimation.animation.OscarAnimationHandler;
import com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation;
import huawei.android.provider.HwSettings;
import java.util.ArrayList;

public abstract class OscarDynamicAnimation<T extends OscarDynamicAnimation<T>> implements OscarAnimationHandler.AnimationFrameCallback {
    public static final ViewProperty ALPHA = new ViewProperty("alpha") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass12 */

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
    public static final ViewProperty ROTATION = new ViewProperty("rotation") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass6 */

        public void setValue(View view, float value) {
            view.setRotation(value);
        }

        public float getValue(View view) {
            return view.getRotation();
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass7 */

        public void setValue(View view, float value) {
            view.setRotationX(value);
        }

        public float getValue(View view) {
            return view.getRotationX();
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass8 */

        public void setValue(View view, float value) {
            view.setRotationY(value);
        }

        public float getValue(View view) {
            return view.getRotationY();
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass4 */

        public void setValue(View view, float value) {
            view.setScaleX(value);
        }

        public float getValue(View view) {
            return view.getScaleX();
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass5 */

        public void setValue(View view, float value) {
            view.setScaleY(value);
        }

        public float getValue(View view) {
            return view.getScaleY();
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass13 */

        public void setValue(View view, float value) {
            view.setScrollX((int) value);
        }

        public float getValue(View view) {
            return (float) view.getScrollX();
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass14 */

        public void setValue(View view, float value) {
            view.setScrollY((int) value);
        }

        public float getValue(View view) {
            return (float) view.getScrollY();
        }
    };
    private static final float THRESHOLD_MULTIPLIER = 0.75f;
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass1 */

        public void setValue(View view, float value) {
            view.setTranslationX(value);
        }

        public float getValue(View view) {
            return view.getTranslationX();
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass2 */

        public void setValue(View view, float value) {
            view.setTranslationY(value);
        }

        public float getValue(View view) {
            return view.getTranslationY();
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass3 */

        public void setValue(View view, float value) {
            if (Build.VERSION.SDK_INT >= 21) {
                view.setTranslationZ(value);
            }
        }

        public float getValue(View view) {
            if (Build.VERSION.SDK_INT >= 21) {
                return view.getTranslationZ();
            }
            return 0.0f;
        }
    };
    private static final float UNSET = Float.MAX_VALUE;
    public static final ViewProperty X = new ViewProperty("x") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass9 */

        public void setValue(View view, float value) {
            view.setX(value);
        }

        public float getValue(View view) {
            return view.getX();
        }
    };
    public static final ViewProperty Y = new ViewProperty("y") {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass10 */

        public void setValue(View view, float value) {
            view.setY(value);
        }

        public float getValue(View view) {
            return view.getY();
        }
    };
    public static final ViewProperty Z = new ViewProperty(HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_Z_SUFFIX) {
        /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass11 */

        public void setValue(View view, float value) {
            if (Build.VERSION.SDK_INT >= 21) {
                view.setZ(value);
            }
        }

        public float getValue(View view) {
            if (Build.VERSION.SDK_INT >= 21) {
                return view.getZ();
            }
            return 0.0f;
        }
    };
    private final ArrayList<OnAnimationEndListener> mEndListeners;
    private long mLastFrameTime;
    float mMaxValue;
    float mMinValue;
    private float mMinVisibleChange;
    final OscarFloatPropertyCompat mProperty;
    boolean mRunning;
    boolean mStartValueIsSet;
    final Object mTarget;
    private final ArrayList<OnAnimationUpdateListener> mUpdateListeners;
    float mValue;
    float mVelocity;

    public interface OnAnimationEndListener {
        void onAnimationEnd(OscarDynamicAnimation oscarDynamicAnimation, boolean z, float f, float f2);
    }

    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(OscarDynamicAnimation oscarDynamicAnimation, float f, float f2);
    }

    /* access modifiers changed from: package-private */
    public abstract float getAcceleration(float f, float f2);

    /* access modifiers changed from: package-private */
    public abstract boolean isAtEquilibrium(float f, float f2);

    /* access modifiers changed from: package-private */
    public abstract void setValueThreshold(float f);

    /* access modifiers changed from: package-private */
    public abstract boolean updateValueAndVelocity(long j);

    public static abstract class ViewProperty extends OscarFloatPropertyCompat<View> {
        private ViewProperty(String name) {
            super(name);
        }
    }

    static class MassState {
        float mValue;
        float mVelocity;

        MassState() {
        }
    }

    OscarDynamicAnimation(final OscarFloatValueHolder floatValueHolder) {
        this.mVelocity = 0.0f;
        this.mValue = Float.MAX_VALUE;
        this.mStartValueIsSet = false;
        this.mRunning = false;
        this.mMaxValue = Float.MAX_VALUE;
        this.mMinValue = -this.mMaxValue;
        this.mLastFrameTime = 0;
        this.mEndListeners = new ArrayList<>();
        this.mUpdateListeners = new ArrayList<>();
        this.mTarget = null;
        this.mProperty = new OscarFloatPropertyCompat("FloatValueHolder") {
            /* class com.huawei.anim.dynamicanimation.animation.OscarDynamicAnimation.AnonymousClass15 */

            @Override // com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat
            public float getValue(Object object) {
                return floatValueHolder.getValue();
            }

            @Override // com.huawei.anim.dynamicanimation.animation.OscarFloatPropertyCompat
            public void setValue(Object object, float value) {
                floatValueHolder.setValue(value);
            }
        };
        this.mMinVisibleChange = 1.0f;
    }

    <K> OscarDynamicAnimation(K object, OscarFloatPropertyCompat<K> property) {
        this.mVelocity = 0.0f;
        this.mValue = Float.MAX_VALUE;
        this.mStartValueIsSet = false;
        this.mRunning = false;
        this.mMaxValue = Float.MAX_VALUE;
        this.mMinValue = -this.mMaxValue;
        this.mLastFrameTime = 0;
        this.mEndListeners = new ArrayList<>();
        this.mUpdateListeners = new ArrayList<>();
        this.mTarget = object;
        this.mProperty = property;
        OscarFloatPropertyCompat oscarFloatPropertyCompat = this.mProperty;
        if (oscarFloatPropertyCompat == ROTATION || oscarFloatPropertyCompat == ROTATION_X || oscarFloatPropertyCompat == ROTATION_Y) {
            this.mMinVisibleChange = 0.1f;
        } else if (oscarFloatPropertyCompat == ALPHA) {
            this.mMinVisibleChange = 0.00390625f;
        } else if (oscarFloatPropertyCompat == SCALE_X || oscarFloatPropertyCompat == SCALE_Y) {
            this.mMinVisibleChange = 0.00390625f;
        } else {
            this.mMinVisibleChange = 1.0f;
        }
    }

    public T setStartValue(float startValue) {
        this.mValue = startValue;
        this.mStartValueIsSet = true;
        return this;
    }

    public T setStartVelocity(float startVelocity) {
        this.mVelocity = startVelocity;
        return this;
    }

    public T setMaxValue(float max) {
        this.mMaxValue = max;
        return this;
    }

    public T setMinValue(float min) {
        this.mMinValue = min;
        return this;
    }

    public T addEndListener(OnAnimationEndListener listener) {
        if (!this.mEndListeners.contains(listener)) {
            this.mEndListeners.add(listener);
        }
        return this;
    }

    public void removeEndListener(OnAnimationEndListener listener) {
        removeEntry(this.mEndListeners, listener);
    }

    public T addUpdateListener(OnAnimationUpdateListener listener) {
        if (!isRunning()) {
            if (!this.mUpdateListeners.contains(listener)) {
                this.mUpdateListeners.add(listener);
            }
            return this;
        }
        throw new UnsupportedOperationException("Error: Update listeners must be added beforethe animation.");
    }

    public void removeUpdateListener(OnAnimationUpdateListener listener) {
        removeEntry(this.mUpdateListeners, listener);
    }

    public T setMinimumVisibleChange(float minimumVisibleChange) {
        if (minimumVisibleChange > 0.0f) {
            this.mMinVisibleChange = minimumVisibleChange;
            setValueThreshold(THRESHOLD_MULTIPLIER * minimumVisibleChange);
            return this;
        }
        throw new IllegalArgumentException("Minimum visible change must be positive.");
    }

    public float getMinimumVisibleChange() {
        return this.mMinVisibleChange;
    }

    private static <T> void removeNullEntries(ArrayList<T> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == null) {
                list.remove(i);
            }
        }
    }

    private static <T> void removeEntry(ArrayList<T> list, T entry) {
        int id = list.indexOf(entry);
        if (id >= 0) {
            list.set(id, null);
        }
    }

    public void start() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        } else if (!this.mRunning) {
            startAnimationInternal();
        }
    }

    public void cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        } else if (this.mRunning) {
            endAnimationInternal(true);
        }
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    private void startAnimationInternal() {
        if (!this.mRunning) {
            this.mRunning = true;
            if (!this.mStartValueIsSet) {
                this.mValue = getPropertyValue();
            }
            float f = this.mValue;
            if (f > this.mMaxValue || f < this.mMinValue) {
                throw new IllegalArgumentException("Starting value need to be in between min value and max value");
            }
            OscarAnimationHandler.getInstance().addAnimationFrameCallback(this, 0);
        }
    }

    @Override // com.huawei.anim.dynamicanimation.animation.OscarAnimationHandler.AnimationFrameCallback
    public boolean doAnimationFrame(long frameTime) {
        long j = this.mLastFrameTime;
        if (j == 0) {
            this.mLastFrameTime = frameTime;
            setPropertyValue(this.mValue);
            return false;
        }
        this.mLastFrameTime = frameTime;
        boolean finished = updateValueAndVelocity(frameTime - j);
        this.mValue = Math.min(this.mValue, this.mMaxValue);
        this.mValue = Math.max(this.mValue, this.mMinValue);
        setPropertyValue(this.mValue);
        if (finished) {
            endAnimationInternal(false);
        }
        return finished;
    }

    private void endAnimationInternal(boolean canceled) {
        this.mRunning = false;
        OscarAnimationHandler.getInstance().removeCallback(this);
        this.mLastFrameTime = 0;
        this.mStartValueIsSet = false;
        for (int i = 0; i < this.mEndListeners.size(); i++) {
            if (this.mEndListeners.get(i) != null) {
                this.mEndListeners.get(i).onAnimationEnd(this, canceled, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mEndListeners);
    }

    /* access modifiers changed from: package-private */
    public void setPropertyValue(float value) {
        this.mProperty.setValue(this.mTarget, value);
        for (int i = 0; i < this.mUpdateListeners.size(); i++) {
            if (this.mUpdateListeners.get(i) != null) {
                this.mUpdateListeners.get(i).onAnimationUpdate(this, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mUpdateListeners);
    }

    /* access modifiers changed from: package-private */
    public float getValueThreshold() {
        return this.mMinVisibleChange * THRESHOLD_MULTIPLIER;
    }

    private float getPropertyValue() {
        return this.mProperty.getValue(this.mTarget);
    }
}
