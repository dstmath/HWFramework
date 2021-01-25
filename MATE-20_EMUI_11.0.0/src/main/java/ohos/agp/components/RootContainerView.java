package ohos.agp.components;

import ohos.app.Context;

public class RootContainerView extends ComponentContainer {
    public static final int SLIDE_DISABLE = 0;
    public static final int SLIDE_HORIZONTAL = 2;
    public static final int SLIDE_HORZVERT = 1;
    public static final int SLIDE_VERTICAL = 3;
    protected OnSlideListener mOnSlideListener;

    public interface OnSlideListener {
        void onSlideStart(RootContainerView rootContainerView);
    }

    private native long nativeGetRootContainerViewHandle();

    private native int nativeGetSlideRecognizerMode(long j);

    private native void nativeSetOnSlideCallback(long j, OnSlideListener onSlideListener);

    private native void nativeSetSlideRecognizerMode(long j, int i);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetRootContainerViewHandle();
        }
    }

    public RootContainerView(Context context) {
        this(context, null);
    }

    public RootContainerView(Context context, AttrSet attrSet) {
        this(context, attrSet, "RootContainerViewDefaultStyle");
    }

    public RootContainerView(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mOnSlideListener = null;
    }

    public void setOnSlideListener(OnSlideListener onSlideListener) {
        this.mOnSlideListener = onSlideListener;
        nativeSetOnSlideCallback(this.mNativeViewPtr, onSlideListener);
    }

    public void setSlideRecognizerMode(int i) {
        nativeSetSlideRecognizerMode(this.mNativeViewPtr, i);
    }

    public int getSlideRecognizerMode() {
        return nativeGetSlideRecognizerMode(this.mNativeViewPtr);
    }
}
