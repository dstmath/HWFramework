package android.animation;

import android.graphics.Path;
import android.graphics.PointF;
import android.net.ProxyInfo;
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
        if (this.mProperty != null) {
            return this.mProperty.getName();
        }
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        for (int i = 0; i < this.mValues.length; i++) {
            if (i == 0) {
                propertyName = ProxyInfo.LOCAL_EXCL_LIST;
            } else {
                propertyName = propertyName + ",";
            }
            propertyName = propertyName + this.mValues[i].getPropertyName();
        }
        return propertyName;
    }

    String getNameForTrace() {
        return "animator:" + getPropertyName();
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
        PropertyValuesHolder x = PropertyValuesHolder.ofKeyframes(xPropertyName, keyframes.createXIntKeyframes());
        PropertyValuesHolder y = PropertyValuesHolder.ofKeyframes(yPropertyName, keyframes.createYIntKeyframes());
        return ofPropertyValuesHolder(target, x, y);
    }

    public static <T> ObjectAnimator ofInt(T target, Property<T, Integer> property, int... values) {
        ObjectAnimator anim = new ObjectAnimator((Object) target, (Property) property);
        anim.setIntValues(values);
        return anim;
    }

    public static <T> ObjectAnimator ofInt(T target, Property<T, Integer> xProperty, Property<T, Integer> yProperty, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        PropertyValuesHolder x = PropertyValuesHolder.ofKeyframes((Property) xProperty, keyframes.createXIntKeyframes());
        PropertyValuesHolder y = PropertyValuesHolder.ofKeyframes((Property) yProperty, keyframes.createYIntKeyframes());
        return ofPropertyValuesHolder(target, x, y);
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
        ObjectAnimator animator = ofInt((Object) target, (Property) property, values);
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
        PropertyValuesHolder x = PropertyValuesHolder.ofKeyframes(xPropertyName, keyframes.createXFloatKeyframes());
        PropertyValuesHolder y = PropertyValuesHolder.ofKeyframes(yPropertyName, keyframes.createYFloatKeyframes());
        return ofPropertyValuesHolder(target, x, y);
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> property, float... values) {
        ObjectAnimator anim = new ObjectAnimator((Object) target, (Property) property);
        anim.setFloatValues(values);
        return anim;
    }

    public static <T> ObjectAnimator ofFloat(T target, Property<T, Float> xProperty, Property<T, Float> yProperty, Path path) {
        PathKeyframes keyframes = KeyframeSet.ofPath(path);
        PropertyValuesHolder x = PropertyValuesHolder.ofKeyframes((Property) xProperty, keyframes.createXFloatKeyframes());
        PropertyValuesHolder y = PropertyValuesHolder.ofKeyframes((Property) yProperty, keyframes.createYFloatKeyframes());
        return ofPropertyValuesHolder(target, x, y);
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
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofObject(propertyName, (TypeConverter) converter, path));
    }

    @SafeVarargs
    public static <T, V> ObjectAnimator ofObject(T target, Property<T, V> property, TypeEvaluator<V> evaluator, V... values) {
        ObjectAnimator anim = new ObjectAnimator((Object) target, (Property) property);
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    @SafeVarargs
    public static <T, V, P> ObjectAnimator ofObject(T target, Property<T, P> property, TypeConverter<V, P> converter, TypeEvaluator<V> evaluator, V... values) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofObject(property, converter, evaluator, values));
    }

    public static <T, V> ObjectAnimator ofObject(T target, Property<T, V> property, TypeConverter<PointF, V> converter, Path path) {
        return ofPropertyValuesHolder(target, PropertyValuesHolder.ofObject((Property) property, (TypeConverter) converter, path));
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
            setValues(PropertyValuesHolder.ofInt(this.mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofInt(this.mPropertyName, values));
        }
    }

    public void setFloatValues(float... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setFloatValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofFloat(this.mProperty, values));
        } else {
            setValues(PropertyValuesHolder.ofFloat(this.mPropertyName, values));
        }
    }

    public void setObjectValues(Object... values) {
        if (this.mValues != null && this.mValues.length != 0) {
            super.setObjectValues(values);
        } else if (this.mProperty != null) {
            setValues(PropertyValuesHolder.ofObject(this.mProperty, (TypeEvaluator) null, values));
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
                    if (pvhMine.getPropertyName() == null || (pvhMine.getPropertyName().equals(pvhTheirs.getPropertyName()) ^ 1) != 0) {
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

    boolean shouldAutoCancel(AnimationFrameCallback anim) {
        if (anim != null && (anim instanceof ObjectAnimator)) {
            ObjectAnimator objAnim = (ObjectAnimator) anim;
            if (objAnim.mAutoCancel && hasSameTargetAndProperties(objAnim)) {
                return true;
            }
        }
        return false;
    }

    void initAnimation() {
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
        return this.mTarget == null ? null : this.mTarget.get();
    }

    public void setTarget(Object target) {
        WeakReference weakReference = null;
        if (getTarget() != target) {
            if (isStarted()) {
                cancel();
            }
            if (target != null) {
                weakReference = new WeakReference(target);
            }
            this.mTarget = weakReference;
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

    void animateValue(float fraction) {
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

    boolean isInitialized() {
        return this.mInitialized;
    }

    public ObjectAnimator clone() {
        return (ObjectAnimator) super.clone();
    }

    public String toString() {
        String returnVal = "ObjectAnimator@" + Integer.toHexString(hashCode()) + ", target " + getTarget();
        if (this.mValues != null) {
            for (PropertyValuesHolder propertyValuesHolder : this.mValues) {
                returnVal = returnVal + "\n    " + propertyValuesHolder.toString();
            }
        }
        return returnVal;
    }
}
