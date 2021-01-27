package ohos.agp.animation;

import java.io.IOException;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.animation.Animator;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.global.resource.NotExistException;
import ohos.global.resource.ResourceManager;
import ohos.global.resource.WrongTypeException;
import ohos.global.resource.solidxml.Node;
import ohos.global.resource.solidxml.TypedAttribute;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class AnimatorValue extends Animator {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_AnimatorValue");
    protected ValueUpdateListener mUpdateListener = null;

    public interface ValueUpdateListener {
        void onUpdate(AnimatorValue animatorValue, float f);
    }

    private native long nativeGetValueAnimatorHandle();

    private native void nativeSetUpdateListener(long j, ValueUpdateListener valueUpdateListener);

    public AnimatorValue() {
        this.mNativeAnimatorPtr = nativeGetValueAnimatorHandle();
        initAnimator(this.mNativeAnimatorPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.animation.Animator
    public void parse(Node node, ResourceManager resourceManager) {
        super.parse(node, resourceManager);
        for (TypedAttribute typedAttribute : node.getTypedAttributes(resourceManager)) {
            if (typedAttribute == null) {
                HiLog.error(TAG, "typedAttribute is null", new Object[0]);
            } else {
                HiLog.debug(TAG, "read viewAnimator attr: %{public}s", new Object[]{typedAttribute.getName()});
                try {
                    String name = typedAttribute.getName();
                    char c = 65535;
                    switch (name.hashCode()) {
                        case -2032139925:
                            if (name.equals("repeat_count")) {
                                c = 2;
                                break;
                            }
                            break;
                        case -1992012396:
                            if (name.equals(SchemaSymbols.ATTVAL_DURATION)) {
                                c = 1;
                                break;
                            }
                            break;
                        case 95467907:
                            if (name.equals("delay")) {
                                c = 0;
                                break;
                            }
                            break;
                        case 2096253127:
                            if (name.equals("interpolator")) {
                                c = 3;
                                break;
                            }
                            break;
                    }
                    if (c == 0) {
                        setDelay((long) typedAttribute.getIntegerValue());
                    } else if (c == 1) {
                        setDuration((long) typedAttribute.getIntegerValue());
                    } else if (c == 2) {
                        setLoopedCount(typedAttribute.getIntegerValue());
                    } else if (c != 3) {
                        HiLog.debug(TAG, "do not support this tag: %{public}s", new Object[]{typedAttribute.getName()});
                    } else {
                        setCurveType(typedAttribute.getIntegerValue());
                    }
                } catch (IOException | NotExistException | WrongTypeException e) {
                    throw new AnimatorScatterException("set " + typedAttribute.getName() + " failed", e);
                }
            }
        }
    }

    public void setDuration(long j) {
        setDurationInternal(j);
    }

    public void setLoopedCount(int i) {
        setLoopedCountInternal(i);
    }

    public void setDelay(long j) {
        setDelayInternal(j);
    }

    public void setCurveType(int i) {
        setCurveTypeInternal(i);
    }

    public void setStateChangedListener(Animator.StateChangedListener stateChangedListener) {
        setPauseListenerInternal(stateChangedListener);
        setStartListenerInternal(stateChangedListener);
    }

    public void setLoopedListener(Animator.LoopedListener loopedListener) {
        setLoopedListenerInternal(loopedListener);
    }

    public void setValueUpdateListener(ValueUpdateListener valueUpdateListener) {
        this.mUpdateListener = valueUpdateListener;
        nativeSetUpdateListener(this.mNativeAnimatorPtr, this.mUpdateListener);
    }
}
