package com.huawei.anim.dynamicanimation;

import android.os.Build;
import android.os.Looper;
import android.util.AndroidRuntimeException;
import android.view.View;
import com.huawei.anim.dynamicanimation.DynamicAnimation;
import com.huawei.anim.dynamicanimation.a;
import java.math.BigDecimal;
import java.util.ArrayList;

public abstract class DynamicAnimation<T extends DynamicAnimation<T>> implements a.b {
    public static final ViewProperty ALPHA = new ViewProperty("alpha") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass4 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setAlpha(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getAlpha();
        }
    };
    public static final int ANDROID_LOLLIPOP = 21;
    public static final ViewProperty AXIS_X = new ViewProperty("x") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass15 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setX(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getX();
        }
    };
    public static final ViewProperty AXIS_Y = new ViewProperty("y") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass2 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setY(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getY();
        }
    };
    public static final ViewProperty AXIS_Z = new ViewProperty("z") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass3 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            if (Build.VERSION.SDK_INT >= 21) {
                view.setZ(f);
            }
        }

        /* renamed from: a */
        public float getValue(View view) {
            if (Build.VERSION.SDK_INT >= 21) {
                return view.getZ();
            }
            return 0.0f;
        }
    };
    public static final float MIN_VISIBLE_CHANGE_ALPHA = new BigDecimal(1.0d).divide(new BigDecimal("256")).floatValue();
    public static final float MIN_VISIBLE_CHANGE_PIXELS = 1.0f;
    public static final float MIN_VISIBLE_CHANGE_ROTATION_DEGREES = new BigDecimal(1.0d).divide(new BigDecimal("10")).floatValue();
    public static final float MIN_VISIBLE_CHANGE_SCALE = new BigDecimal(1.0d).divide(new BigDecimal("500")).floatValue();
    public static final ViewProperty ROTATION = new ViewProperty("rotation") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass12 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setRotation(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getRotation();
        }
    };
    public static final ViewProperty ROTATION_X = new ViewProperty("rotationX") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass13 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setRotationX(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getRotationX();
        }
    };
    public static final ViewProperty ROTATION_Y = new ViewProperty("rotationY") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass14 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setRotationY(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getRotationY();
        }
    };
    public static final ViewProperty SCALE_X = new ViewProperty("scaleX") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass10 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setScaleX(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getScaleX();
        }
    };
    public static final ViewProperty SCALE_Y = new ViewProperty("scaleY") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass11 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setScaleY(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getScaleY();
        }
    };
    public static final ViewProperty SCROLL_X = new ViewProperty("scrollX") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass5 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setScrollX((int) f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return (float) view.getScrollX();
        }
    };
    public static final ViewProperty SCROLL_Y = new ViewProperty("scrollY") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass6 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setScrollY((int) f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return (float) view.getScrollY();
        }
    };
    public static final ViewProperty TRANSLATION_X = new ViewProperty("translationX") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass1 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setTranslationX(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getTranslationX();
        }
    };
    public static final ViewProperty TRANSLATION_Y = new ViewProperty("translationY") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass8 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            view.setTranslationY(f);
        }

        /* renamed from: a */
        public float getValue(View view) {
            return view.getTranslationY();
        }
    };
    public static final ViewProperty TRANSLATION_Z = new ViewProperty("translationZ") {
        /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass9 */

        /* renamed from: a */
        public void setValue(View view, float f) {
            if (Build.VERSION.SDK_INT >= 21) {
                view.setTranslationZ(f);
            }
        }

        /* renamed from: a */
        public float getValue(View view) {
            if (Build.VERSION.SDK_INT >= 21) {
                return view.getTranslationZ();
            }
            return 0.0f;
        }
    };
    private static final float a = Float.MAX_VALUE;
    private static final float b = 0.75f;
    private static final int c = 16;
    private boolean d;
    private boolean e;
    private long f;
    private float g;
    private final ArrayList<OnAnimationStartListener> h;
    private final ArrayList<OnAnimationEndListener> i;
    private final ArrayList<OnAnimationUpdateListener> j;
    protected boolean mIsStartValueIsSet;
    protected float mMaxValue;
    protected float mMinValue;
    protected FloatPropertyCompat mProperty;
    protected Object mTarget;
    protected float mValue;
    protected float mVelocity;

    public interface OnAnimationEndListener {
        void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2);
    }

    public interface OnAnimationStartListener {
        void onAnimationStart(DynamicAnimation dynamicAnimation, float f, float f2);
    }

    public interface OnAnimationUpdateListener {
        void onAnimationUpdate(DynamicAnimation dynamicAnimation, float f, float f2);
    }

    /* access modifiers changed from: package-private */
    public abstract float a(float f2, float f3);

    /* access modifiers changed from: package-private */
    public abstract void a(float f2);

    /* access modifiers changed from: package-private */
    public abstract boolean a(long j2);

    /* access modifiers changed from: package-private */
    public abstract boolean b(float f2, float f3);

    static class a {
        float a;
        float b;

        a() {
        }
    }

    DynamicAnimation(final FloatValueHolder floatValueHolder) {
        this.mVelocity = 0.0f;
        this.mValue = a;
        this.mIsStartValueIsSet = false;
        this.mMaxValue = a;
        this.mMinValue = -this.mMaxValue;
        this.d = false;
        this.e = false;
        this.f = 0;
        this.h = new ArrayList<>();
        this.i = new ArrayList<>();
        this.j = new ArrayList<>();
        this.mTarget = null;
        this.mProperty = new FloatPropertyCompat("FloatValueHolder") {
            /* class com.huawei.anim.dynamicanimation.DynamicAnimation.AnonymousClass7 */

            @Override // com.huawei.anim.dynamicanimation.FloatPropertyCompat
            public float getValue(Object obj) {
                return floatValueHolder.getValue();
            }

            @Override // com.huawei.anim.dynamicanimation.FloatPropertyCompat
            public void setValue(Object obj, float f) {
                floatValueHolder.setValue(f);
            }
        };
        this.g = 1.0f;
    }

    <K> DynamicAnimation(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        this.mVelocity = 0.0f;
        this.mValue = a;
        this.mIsStartValueIsSet = false;
        this.mMaxValue = a;
        this.mMinValue = -this.mMaxValue;
        this.d = false;
        this.e = false;
        this.f = 0;
        this.h = new ArrayList<>();
        this.i = new ArrayList<>();
        this.j = new ArrayList<>();
        a((DynamicAnimation<T>) k, floatPropertyCompat);
    }

    public <K> T setObj(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        a((DynamicAnimation<T>) k, floatPropertyCompat);
        return this;
    }

    public T setStartValue(float f2) {
        this.mValue = f2;
        this.mIsStartValueIsSet = true;
        return this;
    }

    public T setStartVelocity(float f2) {
        this.mVelocity = f2;
        return this;
    }

    public T setMaxValue(float f2) {
        this.mMaxValue = f2;
        return this;
    }

    public T setMinValue(float f2) {
        this.mMinValue = f2;
        return this;
    }

    public T addStartListener(OnAnimationStartListener onAnimationStartListener) {
        if (!this.h.contains(onAnimationStartListener)) {
            this.h.add(onAnimationStartListener);
        }
        return this;
    }

    public T addEndListener(OnAnimationEndListener onAnimationEndListener) {
        if (!this.i.contains(onAnimationEndListener)) {
            this.i.add(onAnimationEndListener);
        }
        return this;
    }

    public void removeStartListener(OnAnimationStartListener onAnimationStartListener) {
        a(this.h, onAnimationStartListener);
    }

    public void removeEndListener(OnAnimationEndListener onAnimationEndListener) {
        a(this.i, onAnimationEndListener);
    }

    public T addUpdateListener(OnAnimationUpdateListener onAnimationUpdateListener) {
        if (!isRunning()) {
            if (!this.j.contains(onAnimationUpdateListener)) {
                this.j.add(onAnimationUpdateListener);
            }
            return this;
        }
        throw new UnsupportedOperationException("Error: Update listeners must be added beforethe animation.");
    }

    public void removeUpdateListener(OnAnimationUpdateListener onAnimationUpdateListener) {
        a(this.j, onAnimationUpdateListener);
    }

    public T clearListeners() {
        this.h.clear();
        this.j.clear();
        this.i.clear();
        return this;
    }

    public T setMinimumVisibleChange(float f2) {
        if (f2 > 0.0f) {
            this.g = f2;
            a(f2 * 0.75f);
            return this;
        }
        throw new IllegalArgumentException("Minimum visible change must be positive.");
    }

    public float getMinimumVisibleChange() {
        return this.g;
    }

    private static void a(ArrayList arrayList) {
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            if (arrayList.get(size) == null) {
                arrayList.remove(size);
            }
        }
    }

    private static <T> void a(ArrayList<T> arrayList, T t) {
        int indexOf = arrayList.indexOf(t);
        if (indexOf >= 0) {
            arrayList.set(indexOf, null);
        }
    }

    public void start() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        } else if (!this.e) {
            this.d = false;
            a();
        }
    }

    public void cancel() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be canceled on the main thread");
        } else if (this.e) {
            a(true);
        }
    }

    public boolean isRunning() {
        return this.e;
    }

    private void a() {
        if (!this.e) {
            this.e = true;
            if (!this.mIsStartValueIsSet) {
                this.mValue = c();
            }
            float f2 = this.mValue;
            if (f2 > this.mMaxValue || f2 < this.mMinValue) {
                throw new IllegalArgumentException("Starting value need to be in between min value and max value");
            }
            a.a().a(this, 0);
            for (int i2 = 0; i2 < this.h.size(); i2++) {
                if (this.h.get(i2) != null) {
                    this.h.get(i2).onAnimationStart(this, this.mValue, this.mVelocity);
                }
            }
            a(this.h);
        }
    }

    public void startImmediately() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Animations may only be started on the main thread");
        } else if (!this.e) {
            this.d = true;
            a();
        }
    }

    @Override // com.huawei.anim.dynamicanimation.a.b
    public boolean doAnimationFrame(long j2) {
        long j3 = this.f;
        if (j3 == 0) {
            this.f = j2;
            if (this.d) {
                j3 = j2 - 16;
            } else {
                setPropertyValue(this.mValue);
                return false;
            }
        }
        this.f = j2;
        this.mValue = Math.min(this.mValue, this.mMaxValue);
        this.mValue = Math.max(this.mValue, this.mMinValue);
        setPropertyValue(this.mValue);
        boolean a2 = a(j2 - j3);
        if (a2) {
            a(false);
        }
        return a2;
    }

    private void a(boolean z) {
        this.e = false;
        a.a().a(this);
        this.f = 0;
        this.mIsStartValueIsSet = false;
        for (int i2 = 0; i2 < this.i.size(); i2++) {
            if (this.i.get(i2) != null) {
                this.i.get(i2).onAnimationEnd(this, z, this.mValue, this.mVelocity);
            }
        }
        a(this.i);
    }

    public void setPropertyValue(float f2) {
        this.mProperty.setValue(this.mTarget, f2);
        for (int i2 = 0; i2 < this.j.size(); i2++) {
            if (this.j.get(i2) != null) {
                this.j.get(i2).onAnimationUpdate(this, f2, this.mVelocity);
            }
        }
        a(this.j);
    }

    /* access modifiers changed from: package-private */
    public float b() {
        return this.g * 0.75f;
    }

    private float c() {
        return this.mProperty.getValue(this.mTarget);
    }

    private <K> void a(K k, FloatPropertyCompat<K> floatPropertyCompat) {
        this.mTarget = k;
        this.mProperty = floatPropertyCompat;
        FloatPropertyCompat floatPropertyCompat2 = this.mProperty;
        if (floatPropertyCompat2 == ROTATION || floatPropertyCompat2 == ROTATION_X || floatPropertyCompat2 == ROTATION_Y) {
            this.g = MIN_VISIBLE_CHANGE_ROTATION_DEGREES;
        } else if (floatPropertyCompat2 == ALPHA) {
            this.g = MIN_VISIBLE_CHANGE_ALPHA;
        } else if (floatPropertyCompat2 == SCALE_X || floatPropertyCompat2 == SCALE_Y) {
            this.g = MIN_VISIBLE_CHANGE_ALPHA;
        } else {
            this.g = 1.0f;
        }
    }

    public static abstract class ViewProperty extends FloatPropertyCompat<View> {
        private ViewProperty(String str) {
            super(str);
        }
    }
}
