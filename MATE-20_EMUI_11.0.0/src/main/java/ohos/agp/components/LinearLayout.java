package ohos.agp.components;

import ohos.agp.components.ComponentContainer;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants;
import ohos.app.Context;

public class LinearLayout extends ComponentContainer {
    private native int nativeGetLinearLayoutGravity(long j);

    private native long nativeGetLinearLayoutHandle();

    private native boolean nativeGetLinearLayoutMeasureWithLargestChildEnabled(long j);

    private native int nativeGetLinearLayoutOrientation(long j);

    private native float nativeGetLinearLayoutWeightSum(long j);

    private native void nativeSetLinearLayoutGravity(long j, int i);

    private native void nativeSetLinearLayoutMeasureWithLargestChildEnabled(long j, boolean z);

    private native void nativeSetLinearLayoutOrientation(long j, int i);

    private native void nativeSetLinearLayoutWeightSum(long j, float f);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetLinearLayoutHandle();
        }
    }

    public LinearLayout(Context context) {
        this(context, null);
    }

    public LinearLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "LinearLayoutDefaultStyle");
    }

    public LinearLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new DirectionalLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    public void setGravity(int i) {
        nativeSetLinearLayoutGravity(this.mNativeViewPtr, i);
    }

    public void setOrientation(int i) {
        if (i == 0) {
            nativeSetLinearLayoutOrientation(this.mNativeViewPtr, 0);
        } else {
            nativeSetLinearLayoutOrientation(this.mNativeViewPtr, 1);
        }
    }

    public int getGravity() {
        return nativeGetLinearLayoutGravity(this.mNativeViewPtr);
    }

    public int getOrientation() {
        return nativeGetLinearLayoutOrientation(this.mNativeViewPtr);
    }

    public void setWeightSum(float f) {
        nativeSetLinearLayoutWeightSum(this.mNativeViewPtr, f);
    }

    public float getWeightSum() {
        return nativeGetLinearLayoutWeightSum(this.mNativeViewPtr);
    }

    public void setMeasureWithLargestChildEnabled(boolean z) {
        nativeSetLinearLayoutMeasureWithLargestChildEnabled(this.mNativeViewPtr, z);
    }

    public boolean isMeasureWithLargestChildEnabled() {
        return nativeGetLinearLayoutMeasureWithLargestChildEnabled(this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        return super.verifyLayoutConfig(layoutConfig);
    }

    @Override // ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutParams(context, attrSet);
    }

    public static class LayoutParams extends ComponentContainer.LayoutConfig {
        private native void nativeSetLinearLayoutLayoutParams(long j, int[] iArr, float f);

        public LayoutParams() {
        }

        public LayoutParams(Context context, AttrSet attrSet) {
            super(context, attrSet);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(int i, int i2, int i3, float f) {
            super(i, i2);
        }

        public LayoutParams(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
        }

        public void applyToComponent(long j, int[] iArr, float f) {
            nativeSetLinearLayoutLayoutParams(j, iArr, f);
        }
    }
}
