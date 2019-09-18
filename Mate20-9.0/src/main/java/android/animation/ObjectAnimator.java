package android.animation;

import android.animation.AnimationHandler;
import android.graphics.Path;
import android.graphics.PointF;
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
        String propertyName;
        String propertyName2 = null;
        if (this.mPropertyName != null) {
            return this.mPropertyName;
        }
        if (this.mProperty != null) {
            return this.mProperty.getName();
        }
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        for (int i = 0; i < this.mValues.length; i++) {
            if (i == 0) {
                propertyName = "";
            } else {
                propertyName = propertyName2 + ",";
            }
            propertyName2 = propertyName + this.mValues[i].getPropertyName();
        }
        return propertyName2;
    }

    /* access modifiers changed from: package-private */
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
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes(xPropertyName, (Keyframes) keyframes.createXIntKeyframes()), PropertyValuesHolder.ofKeyframes(yPropertyName, (Keyframes) keyframes.createYIntKeyframes()));
    }

    public static <T> ObjectAnimator ofInt(T target, Property<T, Integer> property, int... values) {
        ObjectAnimator anim = new ObjectAnimator(target, property);
        anim.setIntValues(values);
        return anim;
    }

    public static <T> ObjectAnimator ofInt(T target, Property<T, Integer> xProperty, Property<T, Integer> yProperty, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes((Property) xProperty, (Keyframes) keyframes.createXIntKeyframes()), PropertyValuesHolder.ofKeyframes((Property) yProperty, (Keyframes) keyframes.createYIntKeyframes()));
    }

    public static ObjectAnimator ofMultiInt(Object target, String propertyName, int[][] values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiInt(propertyName, values));
    }

    public static ObjectAnimator ofMultiInt(Object target, String propertyName, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiInt(propertyName, path));
    }

    @SafeVarargs
    public static <T> ObjectAnimator ofMultiInt(Object target, String propertyName, TypeConverter<T, int[]> converter, TypeEvaluator<T> evaluator, T... values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiInt(propertyName, converter, evaluator, (V[]) values));
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
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes(xPropertyName, (Keyframes) keyframes.createXFloatKeyframes()), PropertyValuesHolder.ofKeyframes(yPropertyName, (Keyframes) keyframes.createYFloatKeyframes()));
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> property, float... values) {
        ObjectAnimator anim = new ObjectAnimator(target, property);
        anim.setFloatValues(values);
        return anim;
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> xProperty, Property<T, Float> yProperty, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofKeyframes((Property) xProperty, (Keyframes) keyframes.createXFloatKeyframes()), PropertyValuesHolder.ofKeyframes((Property) yProperty, (Keyframes) keyframes.createYFloatKeyframes()));
    }

    public static ObjectAnimator ofMultiFloat(Object target, String propertyName, float[][] values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiFloat(propertyName, values));
    }

    public static ObjectAnimator ofMultiFloat(Object target, String propertyName, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiFloat(propertyName, path));
    }

    @SafeVarargs
    public static <T> ObjectAnimator ofMultiFloat(Object target, String propertyName, TypeConverter<T, float[]> converter, TypeEvaluator<T> evaluator, T... values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofMultiFloat(propertyName, converter, evaluator, (V[]) values));
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

    public void setIntValues(int... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setIntValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofInt((Property<?, Integer>) this.mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofInt(this.mPropertyName, values));
        }
    }

    public void setFloatValues(float... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setFloatValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofFloat((Property<?, Float>) this.mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofFloat(this.mPropertyName, values));
        }
    }

    public void setObjectValues(Object... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setObjectValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofObject(this.mProperty, (TypeEvaluator) null, (V[]) values));
        } else {
            setValues(PropertyValuesHolder.ofObject(this.mPropertyName, (TypeEvaluator) null, values));
        }
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

    public void start() {
        AnimationHandler.getInstance().autoCancelBasedOn(this);
        super.start();
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAutoCancel(AnimationHandler.AnimationFrameCallback anim) {
        if (anim != null && (anim instanceof ObjectAnimator)) {
            ObjectAnimator objAnim = (ObjectAnimator) anim;
            if (objAnim.mAutoCancel && hasSameTargetAndProperties(objAnim)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void initAnimation() {
        if (!this.mInitialized) {
            Object target = getTarget();
            if (target != null) {
                for (PropertyValuesHolder propertyValuesHolder : this.mValues) {
                    propertyValuesHolder.setupSetterAndGetter(target);
                }
            }
            super.initAnimation();
        }
    }

    public ObjectAnimator setDuration(long duration) {
        super.setDuration(duration);
        return this;
    }

    public Object getTarget() {
        if (this.mTarget == null) {
            return null;
        }
        return this.mTarget.get();
    }

    public void setTarget(Object target) {
        if (getTarget() != target) {
            if (isStarted()) {
                cancel();
            }
            this.mTarget = target == null ? null : new WeakReference<>(target);
            this.mInitialized = false;
        }
    }

    public void setupStartValues() {
        initAnimation();
        Object target = getTarget();
        if (target != null) {
            for (PropertyValuesHolder propertyValuesHolder : this.mValues) {
                propertyValuesHolder.setupStartValue(target);
            }
        }
    }

    public void setupEndValues() {
        initAnimation();
        Object target = getTarget();
        if (target != null) {
            for (PropertyValuesHolder propertyValuesHolder : this.mValues) {
                propertyValuesHolder.setupEndValue(target);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void animateValue(float fraction) {
        Object target = getTarget();
        if (this.mTarget == null || target != null) {
            super.animateValue(fraction);
            for (PropertyValuesHolder animatedValue : this.mValues) {
                animatedValue.setAnimatedValue(target);
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
    public boolean isInitialized() {
        return this.mInitialized;
    }

    public ObjectAnimator clone() {
        return (ObjectAnimator) super.clone();
    }

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
