package ohos.agp.components;

import ohos.agp.animation.AnimatorProperty;
import ohos.app.Context;

public class PageFlipper extends StackLayout {
    private static final int DEFAULT_PERIOD = 3000;
    private AnimatorProperty mInAnimation;
    private AnimatorProperty mOutAnimation;

    private native int nativeGetCurrentIndex(long j);

    private native int nativeGetFlipPeriod(long j);

    private native long nativeGetHandle();

    private native boolean nativeIsFlipping(long j);

    private native void nativeSetCurrentIndex(long j, int i);

    private native void nativeSetFlipPeriod(long j, int i);

    private native void nativeSetIncomingAnimation(long j, long j2);

    private native void nativeSetOutgoingAnimation(long j, long j2);

    private native void nativeStartFlipping(long j);

    private native void nativeStopFlipping(long j);

    public PageFlipper(Context context) {
        this(context, null);
    }

    public PageFlipper(Context context, AttrSet attrSet) {
        this(context, attrSet, "PageFlipperDefaultStyle");
    }

    public PageFlipper(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        setFlipPeriod(3000);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetHandle();
        }
    }

    @Override // ohos.agp.components.ComponentContainer
    public void addComponent(Component component) {
        super.addComponent(component);
        if (getChildCount() == 1) {
            component.setVisibility(0);
        } else {
            component.setVisibility(2);
        }
    }

    public Component getCurrentComponent() {
        return getComponentAt(getCurrentIndex());
    }

    public int getCurrentIndex() {
        return nativeGetCurrentIndex(this.mNativeViewPtr);
    }

    public void setCurrentIndex(int i) {
        nativeSetCurrentIndex(this.mNativeViewPtr, i);
    }

    public void showNext() {
        setCurrentIndex(getCurrentIndex() + 1);
    }

    public void showPrevious() {
        setCurrentIndex(getCurrentIndex() - 1);
    }

    @Override // ohos.agp.components.ComponentContainer
    public void removeAllComponents() {
        super.removeAllComponents();
        setCurrentIndex(0);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public void removeComponent(Component component) {
        int currentIndex = getCurrentIndex();
        super.removeComponent(component);
        setCurrentIndex(currentIndex);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public void removeComponentAt(int i) {
        Component componentAt;
        if (i >= 0 && (componentAt = getComponentAt(i)) != null) {
            removeComponent(componentAt);
        }
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public void removeComponents(int i, int i2) {
        while (i < i2) {
            removeComponentAt(i);
            i++;
        }
    }

    public AnimatorProperty getIncomingAnimation() {
        return this.mInAnimation;
    }

    public void setIncomingAnimation(AnimatorProperty animatorProperty) {
        this.mInAnimation = animatorProperty;
        nativeSetIncomingAnimation(this.mNativeViewPtr, animatorProperty != null ? animatorProperty.getNativeAnimatorPtr() : 0);
    }

    public AnimatorProperty getOutgoingAnimation() {
        return this.mOutAnimation;
    }

    public void setOutgoingAnimation(AnimatorProperty animatorProperty) {
        this.mOutAnimation = animatorProperty;
        nativeSetOutgoingAnimation(this.mNativeViewPtr, animatorProperty != null ? animatorProperty.getNativeAnimatorPtr() : 0);
    }

    public int getFlipPeriod() {
        return nativeGetFlipPeriod(this.mNativeViewPtr);
    }

    public void setFlipPeriod(int i) {
        if (i < 0) {
            i = 0;
        }
        nativeSetFlipPeriod(this.mNativeViewPtr, i);
    }

    @Deprecated
    public int getFlipInterval() {
        return getFlipPeriod();
    }

    @Deprecated
    public void setFlipInterval(int i) {
        setFlipPeriod(i);
    }

    public void startFlipping() {
        nativeStartFlipping(this.mNativeViewPtr);
    }

    public void stopFlipping() {
        nativeStopFlipping(this.mNativeViewPtr);
    }

    public boolean isFlipping() {
        return nativeIsFlipping(this.mNativeViewPtr);
    }
}
