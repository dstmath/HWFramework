package ohos.agp.components;

import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ScrollViewAttrsConstants;
import ohos.app.Context;

public class ScrollView extends StackLayout {
    private native boolean nativeGetReboundEffect(long j);

    private native long nativeGetScrollViewHandle();

    private native void nativeScrollViewFling(long j, int i, int i2);

    private native boolean nativeScrollViewGetFillViewport(long j);

    private native void nativeScrollViewSetFillViewport(long j, boolean z);

    private native void nativeScrollViewSmoothScrollBy(long j, int i, int i2);

    private native void nativeScrollViewSmoothScrollTo(long j, int i, int i2);

    private native void nativeSetReboundEffect(long j, boolean z);

    public ScrollView(Context context) {
        this(context, null);
    }

    public ScrollView(Context context, AttrSet attrSet) {
        this(context, attrSet, "ScrollViewDefaultStyle");
    }

    public ScrollView(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ScrollViewAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetScrollViewHandle();
        }
    }

    public void fling(int i, int i2) {
        nativeScrollViewFling(this.mNativeViewPtr, i, i2);
    }

    public void smoothScrollBy(int i, int i2) {
        nativeScrollViewSmoothScrollBy(this.mNativeViewPtr, i, i2);
    }

    public void smoothScrollTo(int i, int i2) {
        nativeScrollViewSmoothScrollTo(this.mNativeViewPtr, i, i2);
    }

    public void setFillViewport(boolean z) {
        nativeScrollViewSetFillViewport(this.mNativeViewPtr, z);
    }

    public boolean isFillViewport() {
        return nativeScrollViewGetFillViewport(this.mNativeViewPtr);
    }

    public void setReboundEffect(boolean z) {
        nativeSetReboundEffect(this.mNativeViewPtr, z);
    }

    public boolean getReboundEffect() {
        return nativeGetReboundEffect(this.mNativeViewPtr);
    }
}
