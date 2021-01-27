package ohos.agp.animation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.animation.Animator;
import ohos.agp.components.Component;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AnimatorProperty extends Animator implements Component.AvailabilityObserver {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_ViewPropertyAnimator");
    private final String floatAttr;
    private final HashSet<String> floatSet;
    private final String intAttr;
    private final HashSet<String> intSet;
    protected Component mComponent;

    private native void nativeAlpha(long j, float f);

    private native void nativeAlphaBy(long j, float f);

    private native void nativeAlphaFrom(long j, float f);

    private native long nativeGetPropertyAnimatorHandle();

    private native void nativeMoveByX(long j, float f);

    private native void nativeMoveByY(long j, float f);

    private native void nativeMoveFromX(long j, float f);

    private native void nativeMoveFromY(long j, float f);

    private native void nativeMoveToX(long j, float f);

    private native void nativeMoveToY(long j, float f);

    private native void nativeResetAnimator(long j);

    private native void nativeRotation(long j, float f);

    private native void nativeRotationBy(long j, float f);

    private native void nativeScaleX(long j, float f);

    private native void nativeScaleXBy(long j, float f);

    private native void nativeScaleXFrom(long j, float f);

    private native void nativeScaleY(long j, float f);

    private native void nativeScaleYBy(long j, float f);

    private native void nativeScaleYFrom(long j, float f);

    private native void nativeSetTarget(long j, long j2);

    public AnimatorProperty() {
        this(null);
    }

    public AnimatorProperty(Component component) {
        this.mComponent = null;
        this.intAttr = "duration,delay,interpolator,repeat_count";
        this.intSet = new HashSet<>(Arrays.asList("duration,delay,interpolator,repeat_count".split(",")));
        this.floatAttr = "alpha,alphaBy,moveFromX,moveToX,moveFromY,moveToY,moveByX,moveByY,scaleX,scaleXBy,scaleY,scaleYBy,rotation,rotationBy,rotationX,rotationXBy,rotationY,rotationYBy";
        this.floatSet = new HashSet<>(Arrays.asList("alpha,alphaBy,moveFromX,moveToX,moveFromY,moveToY,moveByX,moveByY,scaleX,scaleXBy,scaleY,scaleYBy,rotation,rotationBy,rotationX,rotationXBy,rotationY,rotationYBy".split(",")));
        this.mNativeAnimatorPtr = nativeGetPropertyAnimatorHandle();
        initAnimator(this.mNativeAnimatorPtr);
        setTarget(component);
    }

    @Override // ohos.agp.components.Component.AvailabilityObserver
    public void onComponentRemoved(Component component) {
        if (Objects.equals(this.mComponent, component)) {
            stop();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.animation.Animator
    public void parse(Node node, ResourceManager resourceManager) {
        super.parse(node, resourceManager);
        for (TypedAttribute typedAttribute : node.getTypedAttributes(resourceManager)) {
            if (typedAttribute == null) {
                HiLog.error(TAG, "typedAttribute is null", new Object[0]);
            } else {
                String name = typedAttribute.getName();
                if (name == null) {
                    HiLog.error(TAG, "attribute name is null", new Object[0]);
                } else {
                    HiLog.debug(TAG, "read viewPropertyAnimator attr: %{public}s", new Object[]{name});
                    setPropertyValue(name, typedAttribute);
                }
            }
        }
    }

    private void setPropertyValue(String str, TypedAttribute typedAttribute) {
        try {
            if (this.intSet.contains(str)) {
                HiLog.debug(TAG, "apply viewPropertyAnimator attr: %{public}s , value: %{public}d", new Object[]{str, Integer.valueOf(typedAttribute.getIntegerValue())});
                char c = 65535;
                switch (str.hashCode()) {
                    case -2032139925:
                        if (str.equals("repeat_count")) {
                            c = 4;
                            break;
                        }
                        break;
                    case -1992012396:
                        if (str.equals(SchemaSymbols.ATTVAL_DURATION)) {
                            c = 0;
                            break;
                        }
                        break;
                    case 95027439:
                        if (str.equals("curve")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 95467907:
                        if (str.equals("delay")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 801632180:
                        if (str.equals("loop_count")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 2096253127:
                        if (str.equals("interpolator")) {
                            c = 2;
                            break;
                        }
                        break;
                }
                if (c == 0) {
                    setDuration((long) typedAttribute.getIntegerValue());
                } else if (c == 1) {
                    setDelay((long) typedAttribute.getIntegerValue());
                } else if (c == 2 || c == 3) {
                    setCurveType(typedAttribute.getIntegerValue());
                } else if (c == 4 || c == 5) {
                    setLoopedCount(typedAttribute.getIntegerValue());
                } else {
                    HiLog.debug(TAG, "continue setPropertyValue.", new Object[0]);
                }
            } else if (this.floatSet.contains(str)) {
                try {
                    Method declaredMethod = AnimatorProperty.class.getDeclaredMethod(str, Float.TYPE);
                    HiLog.debug(TAG, "method is %{public}s", new Object[]{declaredMethod});
                    declaredMethod.setAccessible(true);
                    HiLog.debug(TAG, "apply viewPropertyAnimator attr: %{public}s, value: %{public}f", new Object[]{str, Float.valueOf(convertFloat(typedAttribute.getStringValue()))});
                    declaredMethod.invoke(this, Float.valueOf(convertFloat(typedAttribute.getStringValue())));
                } catch (NoSuchMethodException e) {
                    throw new AnimatorScatterException("setPropertyValue : NoSuchMethodException", e);
                }
            } else {
                HiLog.error(TAG, "do not support this tag: %{public}s", new Object[]{str});
            }
        } catch (IOException unused) {
            throw new AnimatorScatterException("setPropertyValue %{public}s: IOException " + str);
        } catch (WrongTypeException e2) {
            throw new AnimatorScatterException("setPropertyValue %{public}s: WrongTypeException" + str, e2);
        } catch (InvocationTargetException e3) {
            throw new AnimatorScatterException("setPropertyValue %{public}s: InvocationTargetException" + str, e3);
        } catch (IllegalAccessException e4) {
            throw new AnimatorScatterException("setPropertyValue %{public}s: IllegalAccessException" + str, e4);
        } catch (NotExistException e5) {
            throw new AnimatorScatterException("setPropertyValue %{public}s: NotExistException" + str, e5);
        }
    }

    private float convertFloat(String str) {
        if (str == null || str.length() == 0) {
            throw new AnimatorScatterException("get invalid property value.");
        }
        try {
            if (str.substring(str.length() - 1).equalsIgnoreCase("f")) {
                return Float.parseFloat(str.substring(0, str.length() - 1));
            }
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            throw new AnimatorScatterException(str + " format is wrong.", e);
        }
    }

    public AnimatorProperty setTarget(Component component) {
        reset();
        if (component == null) {
            Component component2 = this.mComponent;
            if (component2 != null) {
                component2.removeAvailabilityObserver(this);
                this.mComponent = null;
                nativeSetTarget(this.mNativeAnimatorPtr, 0);
            }
        } else if (!Objects.equals(this.mComponent, component)) {
            Component component3 = this.mComponent;
            if (component3 != null) {
                component3.removeAvailabilityObserver(this);
            }
            this.mComponent = component;
            nativeSetTarget(this.mNativeAnimatorPtr, this.mComponent.getNativeViewPtr());
            this.mComponent.addAvailabilityObserver(this);
        }
        return this;
    }

    public Component getTarget() {
        return this.mComponent;
    }

    public AnimatorProperty setDuration(long j) {
        setDurationInternal(j);
        return this;
    }

    public AnimatorProperty setDelay(long j) {
        setDelayInternal(j);
        return this;
    }

    public AnimatorProperty setCurveType(int i) {
        setCurveTypeInternal(i);
        return this;
    }

    public void reset() {
        nativeResetAnimator(this.mNativeAnimatorPtr);
    }

    public AnimatorProperty setLoopedCount(int i) {
        setLoopedCountInternal(i);
        return this;
    }

    public AnimatorProperty setLoopedListener(Animator.LoopedListener loopedListener) {
        setLoopedListenerInternal(loopedListener);
        return this;
    }

    public AnimatorProperty setStateChangedListener(Animator.StateChangedListener stateChangedListener) {
        setPauseListenerInternal(stateChangedListener);
        setStartListenerInternal(stateChangedListener);
        return this;
    }

    public AnimatorProperty moveFromX(float f) {
        nativeMoveFromX(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty moveToX(float f) {
        nativeMoveToX(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty moveByX(float f) {
        nativeMoveByX(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty moveFromY(float f) {
        nativeMoveFromY(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty moveToY(float f) {
        nativeMoveToY(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty moveByY(float f) {
        nativeMoveByY(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty alpha(float f) {
        nativeAlpha(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty alphaBy(float f) {
        nativeAlphaBy(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty scaleX(float f) {
        nativeScaleX(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty scaleXBy(float f) {
        nativeScaleXBy(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty scaleY(float f) {
        nativeScaleY(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty scaleYBy(float f) {
        nativeScaleYBy(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty rotate(float f) {
        nativeRotation(this.mNativeAnimatorPtr, f);
        return this;
    }

    public AnimatorProperty rotationBy(float f) {
        nativeRotationBy(this.mNativeAnimatorPtr, f);
        return this;
    }
}
