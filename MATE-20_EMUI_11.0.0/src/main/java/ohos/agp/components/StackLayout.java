package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.StackLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.StackLayoutAttrsConstants;
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
            this.mAttrsConstants = new StackLayoutAttrsConstants();
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
        public static final int UNSPECIFIED_GRAVITY = -1;
        public int gravity = -1;

        private native void nativeSetFrameLayoutLayoutParams(long j, int[] iArr);

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
            this.gravity = attr.getIntegerValue();
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3) {
            super(i, i2);
            this.gravity = i3;
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.gravity = layoutConfig.gravity;
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            nativeSetFrameLayoutLayoutParams(component.getNativeViewPtr(), new int[]{this.width, this.height, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, this.gravity});
        }
    }
}
