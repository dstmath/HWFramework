package ohos.agp.components;

import ohos.agp.styles.Style;
import ohos.app.Context;

public class ScrollView extends StackLayout {
    private native boolean nativeGetReboundEffect(long j);

    private native void nativeGetReboundEffectParams(long j, ReboundEffectParams reboundEffectParams);

    private native long nativeGetScrollViewHandle();

    private native void nativeScrollViewFling(long j, int i, int i2);

    private native boolean nativeScrollViewGetFillViewport(long j);

    private native void nativeScrollViewSetFillViewport(long j, boolean z);

    private native void nativeScrollViewSmoothScrollBy(long j, int i, int i2);

    private native void nativeScrollViewSmoothScrollByX(long j, int i);

    private native void nativeScrollViewSmoothScrollByY(long j, int i);

    private native void nativeScrollViewSmoothScrollTo(long j, int i, int i2);

    private native void nativeScrollViewSmoothScrollXTo(long j, int i);

    private native void nativeScrollViewSmoothScrollYTo(long j, int i);

    private native void nativeSetReboundEffect(long j, boolean z);

    private native void nativeSetReboundEffectParams(long j, int i, float f, int i2);

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
            this.mAttrsConstants = AttrHelper.getScrollViewAttrsConstants();
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

    public void doFling(int i, int i2) {
        nativeScrollViewFling(this.mNativeViewPtr, i, i2);
    }

    public void doFlingX(int i) {
        nativeScrollViewFling(this.mNativeViewPtr, i, 0);
    }

    public void doFlingY(int i) {
        nativeScrollViewFling(this.mNativeViewPtr, 0, i);
    }

    public void fluentScrollBy(int i, int i2) {
        nativeScrollViewSmoothScrollBy(this.mNativeViewPtr, i, i2);
    }

    public void fluentScrollByX(int i) {
        nativeScrollViewSmoothScrollByX(this.mNativeViewPtr, i);
    }

    public void fluentScrollByY(int i) {
        nativeScrollViewSmoothScrollByY(this.mNativeViewPtr, i);
    }

    public void fluentScrollTo(int i, int i2) {
        nativeScrollViewSmoothScrollTo(this.mNativeViewPtr, i, i2);
    }

    public void fluentScrollXTo(int i) {
        nativeScrollViewSmoothScrollXTo(this.mNativeViewPtr, i);
    }

    public void fluentScrollYTo(int i) {
        nativeScrollViewSmoothScrollYTo(this.mNativeViewPtr, i);
    }

    public void setMatchViewportEnabled(boolean z) {
        nativeScrollViewSetFillViewport(this.mNativeViewPtr, z);
    }

    public boolean isMatchViewport() {
        return nativeScrollViewGetFillViewport(this.mNativeViewPtr);
    }

    public void setReboundEffect(boolean z) {
        nativeSetReboundEffect(this.mNativeViewPtr, z);
    }

    public boolean getReboundEffect() {
        return nativeGetReboundEffect(this.mNativeViewPtr);
    }

    public void setReboundEffectParams(int i, float f, int i2) {
        nativeSetReboundEffectParams(this.mNativeViewPtr, i, f, i2);
    }

    public void setReboundEffectParams(ReboundEffectParams reboundEffectParams) {
        nativeSetReboundEffectParams(this.mNativeViewPtr, reboundEffectParams.overscrollPercent, reboundEffectParams.overscrollRate, reboundEffectParams.remainVisiblePercent);
    }

    public ReboundEffectParams getReboundEffectParams() {
        ReboundEffectParams reboundEffectParams = new ReboundEffectParams(0, 0.0f, 0);
        nativeGetReboundEffectParams(this.mNativeViewPtr, reboundEffectParams);
        return reboundEffectParams;
    }

    public static class ReboundEffectParams {
        private int overscrollPercent;
        private float overscrollRate;
        private int remainVisiblePercent;

        public ReboundEffectParams(int i, float f, int i2) {
            this.overscrollPercent = i;
            this.overscrollRate = f;
            this.remainVisiblePercent = i2;
        }

        public int getOverscrollPercent() {
            return this.overscrollPercent;
        }

        public float getOverscrollRate() {
            return this.overscrollRate;
        }

        public int getRemainVisiblePercent() {
            return this.remainVisiblePercent;
        }

        public void setOverscrollPercent(int i) {
            this.overscrollPercent = i;
        }

        public void setOverscrollRate(float f) {
            this.overscrollRate = f;
        }

        public void setRemainVisiblePercent(int i) {
            this.remainVisiblePercent = i;
        }
    }
}
