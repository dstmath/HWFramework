package ohos.agp.components;

import ohos.app.Context;

public class ViewFlipper extends ViewAnimator {
    private static final int DEFAULT_INTERVAL = 3000;

    private native int nativeGetFlipInterval(long j);

    private native long nativeGetViewFlipperHandle();

    private native boolean nativeIsFlipping(long j);

    private native void nativeSetFlipInterval(long j, int i);

    private native void nativeStartFlipping(long j);

    private native void nativeStopFlipping(long j);

    public ViewFlipper(Context context) {
        this(context, null);
    }

    public ViewFlipper(Context context, AttrSet attrSet) {
        this(context, attrSet, "ViewFlipperDefaultStyle");
    }

    public ViewFlipper(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        setFlipInterval(3000);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ViewAnimator, ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetViewFlipperHandle();
        }
    }

    public int getFlipInterval() {
        return nativeGetFlipInterval(this.mNativeViewPtr);
    }

    public void setFlipInterval(int i) {
        if (i < 0) {
            i = 0;
        }
        nativeSetFlipInterval(this.mNativeViewPtr, i);
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
