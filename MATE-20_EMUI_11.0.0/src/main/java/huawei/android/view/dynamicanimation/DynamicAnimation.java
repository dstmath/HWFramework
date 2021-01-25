package huawei.android.view.dynamicanimation;

import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.View;
import huawei.android.view.dynamicanimation.AnimationHandler;
import huawei.android.view.dynamicanimation.DynamicAnimation;
import java.util.ArrayList;
import java.util.List;

public abstract class DynamicAnimation<T extends DynamicAnimation<T>> implements AnimationHandler.AnimationFrameCallback {
    public static final ViewProperty ALPHA = new ViewProperty("alpha") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass12 */

        public void setValue(View view, float value) {
            view.setAlpha(value);
        }

        public float getValue(View view) {
            return view.getAlpha();
        }
    };
    private static final int DEFAULT_CAPACITY = 10;
    public static final float MIN_VISIBLE_CHANGE_ALPHA = 0.00390625f;
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = 0.1f;
    public static final float MIN_VISIBLE_CHANGE_SCALE = 0.002f;
    public static final ViewProperty ROTATION = new ViewProperty("rotation") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass6 */

        public void setValue(View view, float value) {
            view.setRotation(value);
        }

        public float getValue(View view) {
            return view.getRotation();
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass7 */

        public void setValue(View view, float value) {
            view.setRotationX(value);
        }

        public float getValue(View view) {
            return view.getRotationX();
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass8 */

        public void setValue(View view, float value) {
            view.setRotationY(value);
        }

        public float getValue(View view) {
            return view.getRotationY();
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass4 */

        public void setValue(View view, float value) {
            view.setScaleX(value);
        }

        public float getValue(View view) {
            return view.getScaleX();
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass5 */

        public void setValue(View view, float value) {
            view.setScaleY(value);
        }

        public float getValue(View view) {
            return view.getScaleY();
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass13 */

        public void setValue(View view, float value) {
            view.setScrollX((int) value);
        }

        public float getValue(View view) {
            return (float) view.getScrollX();
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass14 */

        public void setValue(View view, float value) {
            view.setScrollY((int) value);
        }

        public float getValue(View view) {
            return (float) view.getScrollY();
        }
    };
    private static final float THRESHOLD_MULTIPLIER = 0.75f;
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass1 */

        public void setValue(View view, float value) {
            view.setTranslationX(value);
        }

        public float getValue(View view) {
            return view.getTranslationX();
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass2 */

        public void setValue(View view, float value) {
            view.setTranslationY(value);
        }

        public float getValue(View view) {
            return view.getTranslationY();
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass3 */

        public void setValue(View view, float value) {
            view.setTranslationZ(value);
        }

        public float getValue(View view) {
            return view.getTranslationZ();
        }
    };
    private static final float UNSET = Float.MAX_VALUE;
    public static final ViewProperty X = new ViewProperty("x") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass9 */

        public void setValue(View view, float value) {
            view.setX(value);
        }

        public float getValue(View view) {
            return view.getX();
        }
    };
    public static final ViewProperty Y = new ViewProperty("y") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass10 */

        public void setValue(View view, float value) {
            view.setY(value);
        }

        public float getValue(View view) {
            return view.getY();
        }
    };
    public static final ViewProperty Z = new ViewProperty("z") {
        /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass11 */

        public void setValue(View view, float value) {
            view.setZ(value);
        }

        public float getValue(View view) {
            return view.getZ();
        }
    };
    private final List<OnAnimationEndListener> mEndListeners;
    boolean mIsRunning;
    boolean mIsStartValueIsSet;
    private long mLastFrameTime;
    float mMaxValue;
    float mMinValue;
    private float mMinVisibleChange;
    final FloatPropertyCompat mProperty;
    final Object mTarget;
    private final List<OnAnimationUpdateListener> mUpdateListeners;
    float mValue;
    float mVelocity;

    public interface OnAnimationEndListener {
        void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2);
    }

    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2);
    }

    /* access modifiers changed from: package-private */
    public abstract float getAcceleration(float f, float f2);

    /* access modifiers changed from: package-private */
    public abstract boolean isAtEquilibrium(float f, float f2);

    /* access modifiers changed from: package-private */
    public abstract void setValueThreshold(float f);

    /* access modifiers changed from: package-private */
    public abstract boolean updateValueAndVelocity(long j);

    public static abstract class ViewProperty extends FloatPropertyCompat<View> {
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

    DynamicAnimation(final FloatValueHolder floatValueHolder) {
        this.mVelocity = 0.0f;
        this.mValue = UNSET;
        this.mIsStartValueIsSet = false;
        this.mIsRunning = false;
        this.mMaxValue = UNSET;
        this.mMinValue = -this.mMaxValue;
        this.mLastFrameTime = 0;
        this.mEndListeners = new ArrayList((int) DEFAULT_CAPACITY);
        this.mUpdateListeners = new ArrayList((int) DEFAULT_CAPACITY);
        this.mTarget = null;
        this.mProperty = new FloatPropertyCompat("FloatValueHolder") {
            /* class huawei.android.view.dynamicanimation.DynamicAnimation.AnonymousClass15 */

            @Override // huawei.android.view.dynamicanimation.FloatPropertyCompat
            public float getValue(Object object) {
                return floatValueHolder.getValue();
            }

            @Override // huawei.android.view.dynamicanimation.FloatPropertyCompat
            public void setValue(Object object, float value) {
                floatValueHolder.setValue(value);
            }
        };
        this.mMinVisibleChange = 1.0f;
    }

    <K> DynamicAnimation(K object, FloatPropertyCompat<K> property) {
        this.mVelocity = 0.0f;
        this.mValue = UNSET;
        this.mIsStartValueIsSet = false;
        this.mIsRunning = false;
        this.mMaxValue = UNSET;
        this.mMinValue = -this.mMaxValue;
        this.mLastFrameTime = 0;
        this.mEndListeners = new ArrayList((int) DEFAULT_CAPACITY);
        this.mUpdateListeners = new ArrayList((int) DEFAULT_CAPACITY);
        this.mTarget = object;
        this.mProperty = property;
        FloatPropertyCompat floatPropertyCompat = this.mProperty;
        if (floatPropertyCompat == ROTATION || floatPropertyCompat == ROTATION_X || floatPropertyCompat == ROTATION_Y) {
            this.mMinVisibleChange = 0.1f;
        } else if (floatPropertyCompat == ALPHA) {
            this.mMinVisibleChange = 0.00390625f;
        } else if (floatPropertyCompat == SCALE_X || floatPropertyCompat == SCALE_Y) {
            this.mMinVisibleChange = 0.00390625f;
        } else {
            this.mMinVisibleChange = 1.0f;
        }
    }

    public T setStartValue(float startValue) {
        this.mValue = startValue;
        this.mIsStartValueIsSet = true;
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
            setValueThreshold(0.75f * minimumVisibleChange);
            return this;
        }
        throw new IllegalArgumentException("Minimum visible change must be positive.");
    }

    public float getMinimumVisibleChange() {
        return this.mMinVisibleChange;
    }

    private static <T> void removeNullEntries(List<T> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i) == null) {
                list.remove(i);
            }
        }
    }

    private static <T> void removeEntry(List<T> list, T entry) {
        int index = list.indexOf(entry);
        if (index >= 0) {
            list.set(index, null);
        }
    }

    public void start() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        } else if (!this.mIsRunning) {
            startAnimationInternal();
        }
    }

    public void cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        } else if (this.mIsRunning) {
            endAnimationInternal(true);
        }
    }

    public boolean isRunning() {
        return this.mIsRunning;
    }

    private void startAnimationInternal() {
        if (!this.mIsRunning) {
            this.mIsRunning = true;
            if (!this.mIsStartValueIsSet) {
                this.mValue = getPropertyValue();
            }
            float f = this.mValue;
            if (f > this.mMaxValue || f < this.mMinValue) {
                throw new IllegalArgumentException("Starting value need to be in between min value and max value");
            }
            AnimationHandler.getInstance().addAnimationFrameCallback(this, 0);
        }
    }

    @Override // huawei.android.view.dynamicanimation.AnimationHandler.AnimationFrameCallback
    public boolean doAnimationFrame(long frameTime) {
        long j = this.mLastFrameTime;
        if (j == 0) {
            this.mLastFrameTime = frameTime;
            setPropertyValue(this.mValue);
            return false;
        }
        this.mLastFrameTime = frameTime;
        boolean isFinished = updateValueAndVelocity(frameTime - j);
        float f = this.mValue;
        float f2 = this.mMaxValue;
        if (f >= f2) {
            f = f2;
        }
        this.mValue = f;
        float f3 = this.mValue;
        float f4 = this.mMinValue;
        if (f3 <= f4) {
            f3 = f4;
        }
        this.mValue = f3;
        setPropertyValue(this.mValue);
        if (isFinished) {
            endAnimationInternal(false);
        }
        return isFinished;
    }

    private void endAnimationInternal(boolean isCanceled) {
        this.mIsRunning = false;
        AnimationHandler.getInstance().removeCallback(this);
        this.mLastFrameTime = 0;
        this.mIsStartValueIsSet = false;
        int size = this.mEndListeners.size();
        for (int i = 0; i < size; i++) {
            if (this.mEndListeners.get(i) != null) {
                this.mEndListeners.get(i).onAnimationEnd(this, isCanceled, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mEndListeners);
    }

    /* access modifiers changed from: package-private */
    public void setPropertyValue(float value) {
        this.mProperty.setValue(this.mTarget, value);
        int size = this.mUpdateListeners.size();
        for (int i = 0; i < size; i++) {
            if (this.mUpdateListeners.get(i) != null) {
                this.mUpdateListeners.get(i).onAnimationUpdate(this, this.mValue, this.mVelocity);
            }
        }
        removeNullEntries(this.mUpdateListeners);
    }

    /* access modifiers changed from: package-private */
    public float getValueThreshold() {
        return this.mMinVisibleChange * 0.75f;
    }

    private float getPropertyValue() {
        return this.mProperty.getValue(this.mTarget);
    }
}
