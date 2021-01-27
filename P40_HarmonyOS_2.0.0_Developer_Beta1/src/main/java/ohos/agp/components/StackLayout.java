package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.StackLayout;
import ohos.agp.styles.Style;
import ohos.app.Context;

public class StackLayout extends ComponentContainer {
    private native long nativeGetFrameLayoutHandle();

    private native boolean nativeGetFrameLayoutMeasureAllChildren(long j);

    private native void nativeSetFrameLayoutMeasureAllChildren(long j, boolean z);

    public StackLayout(Context context) {
        this(context, null);
    }

    public StackLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "StackLayoutDefaultStyle");
    }

    public StackLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getStackLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetFrameLayoutHandle();
        }
    }

    public void setMeasureAllChildren(boolean z) {
        nativeSetFrameLayoutMeasureAllChildren(this.mNativeViewPtr, z);
    }

    public boolean getMeasureAllChildren() {
        return nativeGetFrameLayoutMeasureAllChildren(this.mNativeViewPtr);
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
        public int alignment = -1;

        private native void nativeSetFrameLayoutLayoutParams(long j, int[] iArr, boolean z);

        public LayoutConfig() {
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            super(context, attrSet);
            attrSet.getAttr("layout_alignment").ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$StackLayout$LayoutConfig$IKOhfrjpQZ9aflDTu3xPup9ZQ */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    StackLayout.LayoutConfig.this.lambda$new$0$StackLayout$LayoutConfig((Attr) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$StackLayout$LayoutConfig(Attr attr) {
            this.alignment = attr.getIntegerValue();
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3) {
            super(i, i2);
            this.alignment = i3;
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.alignment = layoutConfig.alignment;
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001d: APUT  (r0v1 int[]), (2 ??[int, float, short, byte, char]), (r1v3 int) */
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
            nativeSetFrameLayoutLayoutParams(component.getNativeViewPtr(), iArr, isMarginsRelative());
        }
    }
}
