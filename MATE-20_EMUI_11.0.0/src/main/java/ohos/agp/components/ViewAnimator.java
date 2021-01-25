package ohos.agp.components;

import ohos.agp.animation.AnimatorProperty;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class ViewAnimator extends StackLayout {
    static final int MAX_VIEW_COUNT = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "AGP_ViewAnimator");
    private AnimatorProperty mInAnimation;
    private AnimatorProperty mOutAnimation;

    private native int nativeGetDisplayedChild(long j);

    private native long nativeGetViewAnimatorHandle();

    private native void nativeSetViewAnimatorDisplayedChild(long j, int i);

    private native void nativeSetViewAnimatorInAnimation(long j, long j2);

    private native void nativeSetViewAnimatorOutAnimation(long j, long j2);

    public ViewAnimator(Context context) {
        this(context, null);
    }

    public ViewAnimator(Context context, AttrSet attrSet) {
        this(context, attrSet, "ViewAnimatorDefaultStyle");
    }

    public ViewAnimator(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetViewAnimatorHandle();
        }
    }

    @Override // ohos.agp.components.ComponentContainer
    public void addComponent(Component component) {
        super.addComponent(component);
        if (getChildCount() == 1) {
            component.setVisibility(0);
        } else {
            component.setVisibility(8);
        }
    }

    public Component getCurrentView() {
        return getComponentAt(getDisplayedChild());
    }

    public int getDisplayedChild() {
        return nativeGetDisplayedChild(this.mNativeViewPtr);
    }

    public void setDisplayedChild(int i) {
        nativeSetViewAnimatorDisplayedChild(this.mNativeViewPtr, i);
    }

    public void showNext() {
        setDisplayedChild(getDisplayedChild() + 1);
    }

    public void showPrevious() {
        setDisplayedChild(getDisplayedChild() - 1);
    }

    @Override // ohos.agp.components.ComponentContainer
    public void removeAllComponents() {
        super.removeAllComponents();
        setDisplayedChild(0);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public void removeComponent(Component component) {
        int displayedChild = getDisplayedChild();
        super.removeComponent(component);
        setDisplayedChild(displayedChild);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public void removeComponentAt(int i) {
        Component componentAt;
        if (i >= 0 && (componentAt = getComponentAt(i)) != null) {
            removeComponent(componentAt);
        }
    }

    public void removeViews(int i, int i2) {
        while (i < i2) {
            removeComponentAt(i);
            i++;
        }
    }

    public AnimatorProperty getInAnimation() {
        return this.mInAnimation;
    }

    public void setInAnimation(AnimatorProperty animatorProperty) {
        this.mInAnimation = animatorProperty;
        nativeSetViewAnimatorInAnimation(this.mNativeViewPtr, animatorProperty != null ? animatorProperty.getNativeAnimatorPtr() : 0);
    }

    public AnimatorProperty getOutAnimation() {
        return this.mOutAnimation;
    }

    public void setOutAnimation(AnimatorProperty animatorProperty) {
        this.mOutAnimation = animatorProperty;
        nativeSetViewAnimatorOutAnimation(this.mNativeViewPtr, animatorProperty != null ? animatorProperty.getNativeAnimatorPtr() : 0);
    }
}
