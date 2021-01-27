package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants;
import ohos.app.Context;

public class DirectionalLayout extends ComponentContainer {
    private native int nativeGetAlignment(long j);

    private native long nativeGetHandle();

    private native boolean nativeGetMeasureWithLargestChildEnabled(long j);

    private native int nativeGetOrientation(long j);

    private native float nativeGetTotalWeight(long j);

    private native void nativeSetAlignment(long j, int i);

    private native void nativeSetMeasureWithLargestChildEnabled(long j, boolean z);

    private native void nativeSetOrientation(long j, int i);

    private native void nativeSetTotalWeight(long j, float f);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetHandle();
        }
    }

    public DirectionalLayout(Context context) {
        this(context, null);
    }

    public DirectionalLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "DirectionalLayoutDefaultStyle");
    }

    public DirectionalLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getDirectionalLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    public void setAlignment(int i) {
        nativeSetAlignment(this.mNativeViewPtr, i);
    }

    public void setOrientation(int i) {
        if (i == 0) {
            nativeSetOrientation(this.mNativeViewPtr, 0);
        } else {
            nativeSetOrientation(this.mNativeViewPtr, 1);
        }
    }

    public int getAlignment() {
        return nativeGetAlignment(this.mNativeViewPtr);
    }

    public int getOrientation() {
        return nativeGetOrientation(this.mNativeViewPtr);
    }

    public void setTotalWeight(float f) {
        nativeSetTotalWeight(this.mNativeViewPtr, f);
    }

    public float getTotalWeight() {
        return nativeGetTotalWeight(this.mNativeViewPtr);
    }

    public void setMeasureWithLargestChildEnabled(boolean z) {
        nativeSetMeasureWithLargestChildEnabled(this.mNativeViewPtr, z);
    }

    public boolean isMeasureWithLargestChildEnabled() {
        return nativeGetMeasureWithLargestChildEnabled(this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (layoutConfig instanceof LayoutConfig) {
            return layoutConfig;
        }
        return new LayoutConfig(layoutConfig);
    }

    @Override // ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutConfig(context, attrSet);
    }

    public static class LayoutConfig extends ComponentContainer.LayoutConfig {
        public static final int UNSPECIFIED_ALIGNMENT = -1;
        public static final float UNSPECIFIED_WEIGHT = 0.0f;
        public int alignment = -1;
        public float weight = 0.0f;

        private native void nativeSetLayoutConfig(long j, int[] iArr, boolean z, float f);

        public LayoutConfig() {
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            super(context, attrSet);
            attrSet.getAttr("layout_alignment").ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$DirectionalLayout$LayoutConfig$YRmKQ3jwdqjNSDYmR1Qb5JI1Mk0 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DirectionalLayout.LayoutConfig.this.lambda$new$0$DirectionalLayout$LayoutConfig((Attr) obj);
                }
            });
            attrSet.getAttr(DirectionalLayoutAttrsConstants.LayoutParamsAttrsConstants.LAYOUT_WEIGHT).ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$DirectionalLayout$LayoutConfig$_JOYKLczbt_SXDAibzifhrLHQps */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DirectionalLayout.LayoutConfig.this.lambda$new$1$DirectionalLayout$LayoutConfig((Attr) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$DirectionalLayout$LayoutConfig(Attr attr) {
            this.alignment = attr.getIntegerValue();
        }

        public /* synthetic */ void lambda$new$1$DirectionalLayout$LayoutConfig(Attr attr) {
            this.weight = (float) attr.getIntegerValue();
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3, float f) {
            super(i, i2);
            this.alignment = i3;
            this.weight = f;
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.alignment = layoutConfig.alignment;
            this.weight = layoutConfig.weight;
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001d: APUT  (r4v0 int[]), (2 ??[int, float, short, byte, char]), (r0v4 int) */
        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            int[] iArr = new int[7];
            iArr[0] = this.width;
            iArr[1] = this.height;
            iArr[2] = isMarginsRelative() ? getHorizontalStartMargin() : getMarginLeft();
            iArr[3] = getMarginTop();
            iArr[4] = isMarginsRelative() ? getHorizontalEndMargin() : getMarginRight();
            iArr[5] = getMarginBottom();
            iArr[6] = this.alignment;
            nativeSetLayoutConfig(component.getNativeViewPtr(), iArr, isMarginsRelative(), this.weight);
        }
    }
}
