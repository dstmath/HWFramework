package android.animation;

import android.animation.AnimationHandler;
import android.graphics.Path;
import android.graphics.PointF;
import android.telephony.SmsManager;
import android.util.Property;
import java.lang.ref.WeakReference;

public final class ObjectAnimator extends ValueAnimator {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "ObjectAnimator";
    private boolean mAutoCancel = false;
    private Property mProperty;
    private String mPropertyName;
    private WeakReference<Object> mTarget;

    public void setPropertyName(String propertyName) {
        if (this.mValues != null) {
            PropertyValuesHolder valuesHolder = this.mValues[0];
            String oldName = valuesHolder.getPropertyName();
            valuesHolder.setPropertyName(propertyName);
            this.mValuesMap.remove(oldName);
            this.mValuesMap.put(propertyName, valuesHolder);
        }
        this.mPropertyName = propertyName;
        this.mInitialized = false;
    }

    public void setProperty(Property property) {
        if (this.mValues != null) {
            PropertyValuesHolder valuesHolder = this.mValues[0];
            String oldName = valuesHolder.getPropertyName();
            valuesHolder.setProperty(property);
            this.mValuesMap.remove(oldName);
            this.mValuesMap.put(this.mPropertyName, valuesHolder);
        }
        if (this.mProperty != null) {
            this.mPropertyName = property.getName();
        }
        this.mProperty = property;
        this.mInitialized = false;
    }

    public String getPropertyName() {
        String propertyName = null;
        if (this.mPropertyName != null) {
            return this.mPropertyName;
        }
        Property property = this.mProperty;
        if (property != null) {
            return property.getName();
        }
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        for (int i = 0; i < this.mValues.length; i++) {
            propertyName = (i == 0 ? "" : propertyName + SmsManager.REGEX_PREFIX_DELIMITER) + this.mValues[i].getPropertyName();
        }
        return propertyName;
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public String getNameForTrace() {
        return "animator:" + getPropertyName();
    }

    public ObjectAnimator() {
    }

    private ObjectAnimator(Object target, String propertyName) {
        setTarget(target);
        setPropertyName(propertyName);
    }

    private <T> ObjectAnimator(T target, Property<T, ?> property) {
        setTarget(target);
        setProperty(property);
    }

    public static ObjectAnimator ofInt(Object target, String propertyName, int... values) {
        ObjectAnimator anim = new ObjectAnimator(target, propertyName);
        anim.setIntValues(values);
        return anim;
    }

    public static ObjectAnimator ofInt(Object target, String xPropertyName, String yPropertyName, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes(xPropertyName, keyframes.createXIntKeyframes()), PropertyValuesHolder.ofKeyframes(yPropertyName, keyframes.createYIntKeyframes()));
    }

    public static <T> ObjectAnimator ofInt(T target, Property<T, Integer> property, int... values) {
        ObjectAnimator anim = new ObjectAnimator(target, property);
        anim.setIntValues(values);
        return anim;
    }

    public static <T> ObjectAnimator ofInt(T target, Property<T, Integer> xProperty, Property<T, Integer> yProperty, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes(xProperty, keyframes.createXIntKeyframes()), PropertyValuesHolder.ofKeyframes(yProperty, keyframes.createYIntKeyframes()));
    }

    public static ObjectAnimator ofMultiInt(Object target, String propertyName, int[][] values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiInt(propertyName, values));
    }

    public static ObjectAnimator ofMultiInt(Object target, String propertyName, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiInt(propertyName, path));
    }

    @SafeVarargs
    public static <T> ObjectAnimator ofMultiInt(Object target, String propertyName, TypeConverter<T, int[]> converter, TypeEvaluator<T> evaluator, T... values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiInt(propertyName, (TypeConverter) converter, (TypeEvaluator) evaluator, (Object[]) values));
    }

    public static ObjectAnimator ofArgb(Object target, String propertyName, int... values) {
        ObjectAnimator animator = ofInt(target, propertyName, values);
        animator.setEvaluator(ArgbEvaluator.getInstance());
        return animator;
    }

    public static <T> ObjectAnimator ofArgb(T target, Property<T, Integer> property, int... values) {
        ObjectAnimator animator = ofInt(target, property, values);
        animator.setEvaluator(ArgbEvaluator.getInstance());
        return animator;
    }

    public static ObjectAnimator ofFloat(Object target, String propertyName, float... values) {
        ObjectAnimator anim = new ObjectAnimator(target, propertyName);
        anim.setFloatValues(values);
        return anim;
    }

    public static ObjectAnimator ofFloat(Object target, String xPropertyName, String yPropertyName, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes(xPropertyName, keyframes.createXFloatKeyframes()), PropertyValuesHolder.ofKeyframes(yPropertyName, keyframes.createYFloatKeyframes()));
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> property, float... values) {
        ObjectAnimator anim = new ObjectAnimator(target, property);
        anim.setFloatValues(values);
        return anim;
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> xProperty, Property<T, Float> yProperty, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes(xProperty, keyframes.createXFloatKeyframes()), PropertyValuesHolder.ofKeyframes(yProperty, keyframes.createYFloatKeyframes()));
    }

    public static ObjectAnimator ofMultiFloat(Object target, String propertyName, float[][] values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiFloat(propertyName, values));
    }

    public static ObjectAnimator ofMultiFloat(Object target, String propertyName, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiFloat(propertyName, path));
    }

    @SafeVarargs
    public static <T> ObjectAnimator ofMultiFloat(Object target, String propertyName, TypeConverter<T, float[]> converter, TypeEvaluator<T> evaluator, T... values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiFloat(propertyName, (TypeConverter) converter, (TypeEvaluator) evaluator, (Object[]) values));
    }

    public static ObjectAnimator ofObject(Object target, String propertyName, TypeEvaluator evaluator, Object... values) {
        ObjectAnimator anim = new ObjectAnimator(target, propertyName);
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public static ObjectAnimator ofObject(Object target, String propertyName, TypeConverter<PointF, ?> converter, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofObject(propertyName, converter, path));
    }

    @SafeVarargs
    public static <T, V> ObjectAnimator ofObject(T target, Property<T, V> property, TypeEvaluator<V> evaluator, V... values) {
        ObjectAnimator anim = new ObjectAnimator(target, property);
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    @SafeVarargs
    public static <T, V, P> ObjectAnimator ofObject(T target, Property<T, P> property, TypeConverter<V, P> converter, TypeEvaluator<V> evaluator, V... values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofObject(property, converter, evaluator, values));
    }

    public static <T, V> ObjectAnimator ofObject(T target, Property<T, V> property, TypeConverter<PointF, V> converter, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofObject(property, converter, path));
    }

    public static ObjectAnimator ofPropertyValuesHolder(Object target, PropertyValuesHolder... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setValues(values);
        return anim;
    }

    @Override // android.animation.ValueAnimator
    public void setIntValues(int... values) {
        if (this.mValues == null || this.mValues.length == 0) {
            Property property = this.mProperty;
            if (property != null) {
                setValues(PropertyValuesHolder.ofInt(property, values));
                return;
            }
            setValues(PropertyValuesHolder.ofInt(this.mPropertyName, values));
            return;
        }
        super.setIntValues(values);
    }

    @Override // android.animation.ValueAnimator
    public void setFloatValues(float... values) {
        if (this.mValues == null || this.mValues.length == 0) {
            Property property = this.mProperty;
            if (property != null) {
                setValues(PropertyValuesHolder.ofFloat(property, values));
                return;
            }
            setValues(PropertyValuesHolder.ofFloat(this.mPropertyName, values));
            return;
        }
        super.setFloatValues(values);
    }

    @Override // android.animation.ValueAnimator
    public void setObjectValues(Object... values) {
        if (this.mValues == null || this.mValues.length == 0) {
            Property property = this.mProperty;
            if (property != null) {
                setValues(PropertyValuesHolder.ofObject(property, (TypeEvaluator) null, values));
                return;
            }
            setValues(PropertyValuesHolder.ofObject(this.mPropertyName, (TypeEvaluator) null, values));
            return;
        }
        super.setObjectValues(values);
    }

    public void setAutoCancel(boolean cancel) {
        this.mAutoCancel = cancel;
    }

    private boolean hasSameTargetAndProperties(Animator anim) {
        if (anim instanceof ObjectAnimator) {
            PropertyValuesHolder[] theirValues = ((ObjectAnimator) anim).getValues();
            if (((ObjectAnimator) anim).getTarget() == getTarget() && this.mValues.length == theirValues.length) {
                for (int i = 0; i < this.mValues.length; i++) {
                    PropertyValuesHolder pvhMine = this.mValues[i];
                    PropertyValuesHolder pvhTheirs = theirValues[i];
                    if (pvhMine.getPropertyName() == null || !pvhMine.getPropertyName().equals(pvhTheirs.getPropertyName())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override // android.animation.Animator, android.animation.ValueAnimator
    public void start() {
        AnimationHandler.getInstance().autoCancelBasedOn(this);
        super.start();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAutoCancel(AnimationHandler.AnimationFrameCallback anim) {
        if (anim != null && (anim instanceof ObjectAnimator)) {
            ObjectAnimator objAnim = (ObjectAnimator) anim;
            if (!objAnim.mAutoCancel || !hasSameTargetAndProperties(objAnim)) {
                return false;
            }
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public void initAnimation() {
        if (!this.mInitialized) {
            Object target = getTarget();
            if (target != null) {
                int numValues = this.mValues.length;
                for (int i = 0; i < numValues; i++) {
                    this.mValues[i].setupSetterAndGetter(target);
                }
            }
            super.initAnimation();
        }
    }

    @Override // android.animation.Animator, android.animation.ValueAnimator, android.animation.ValueAnimator
    public ObjectAnimator setDuration(long duration) {
        super.setDuration(duration);
        return this;
    }

    public Object getTarget() {
        WeakReference<Object> weakReference = this.mTarget;
        if (weakReference == null) {
            return null;
        }
        return weakReference.get();
    }

    @Override // android.animation.Animator
    public void setTarget(Object target) {
        if (getTarget() != target) {
            if (isStarted()) {
                cancel();
            }
            this.mTarget = target == null ? null : new WeakReference<>(target);
            this.mInitialized = false;
        }
    }

    @Override // android.animation.Animator
    public void setupStartValues() {
        initAnimation();
        Object target = getTarget();
        if (target != null) {
            int numValues = this.mValues.length;
            for (int i = 0; i < numValues; i++) {
                this.mValues[i].setupStartValue(target);
            }
        }
    }

    @Override // android.animation.Animator
    public void setupEndValues() {
        initAnimation();
        Object target = getTarget();
        if (target != null) {
            int numValues = this.mValues.length;
            for (int i = 0; i < numValues; i++) {
                this.mValues[i].setupEndValue(target);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.ValueAnimator
    public void animateValue(float fraction) {
        Object target = getTarget();
        if (this.mTarget == null || target != null) {
            super.animateValue(fraction);
            int numValues = this.mValues.length;
            for (int i = 0; i < numValues; i++) {
                this.mValues[i].setAnimatedValue(target);
            }
            return;
        }
        cancel();
    }

    public void setPathAnimFraction(float fraction) {
        initAnimation();
        animateValue(fraction);
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator, android.animation.ValueAnimator
    public boolean isInitialized() {
        return this.mInitialized;
    }

    @Override // java.lang.Object, android.animation.Animator, android.animation.Animator, android.animation.ValueAnimator, android.animation.ValueAnimator, android.animation.ValueAnimator
    public ObjectAnimator clone() {
        return (ObjectAnimator) super.clone();
    }

    @Override // android.animation.ValueAnimator
    public String toString() {
        String returnVal = "ObjectAnimator@" + Integer.toHexString(hashCode()) + ", target " + getTarget();
        if (this.mValues != null) {
            for (int i = 0; i < this.mValues.length; i++) {
                returnVal = returnVal + "\n    " + this.mValues[i].toString();
            }
        }
        return returnVal;
    }
}
