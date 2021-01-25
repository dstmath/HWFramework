package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.AdaptiveBoxLayout;
import ohos.agp.components.ComponentContainer;
import ohos.app.Context;

public class AdaptiveBoxLayout extends ComponentContainer {
    private native void nativeAddAdaptiveRule(long j, int i, int i2, int i3);

    private native void nativeClearAdaptiveRules(long j);

    private native long nativeGetAdaptiveBoxLayoutHandle();

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetAdaptiveBoxLayoutHandle();
        }
    }

    public AdaptiveBoxLayout(Context context) {
        this(context, null);
    }

    public AdaptiveBoxLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "AdaptiveBoxLayoutDefaultStyle");
    }

    public AdaptiveBoxLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    public void addAdaptiveRule(int i, int i2, int i3) {
        nativeAddAdaptiveRule(this.mNativeViewPtr, i, i2, i3);
    }

    public void clearAdaptiveRules() {
        nativeClearAdaptiveRules(this.mNativeViewPtr);
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
        public int gravity = 17;

        private native void nativeSetAdaptiveBoxLayoutParams(long j, int[] iArr);

        public LayoutConfig() {
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3) {
            super(i, i2);
            this.gravity = i3;
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            super(context, attrSet);
            attrSet.getAttr("layout_alignment").ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$AdaptiveBoxLayout$LayoutConfig$w0CSVTVp9zouMdU6rpPJ4vGGxmA */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AdaptiveBoxLayout.LayoutConfig.this.lambda$new$0$AdaptiveBoxLayout$LayoutConfig((Attr) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$AdaptiveBoxLayout$LayoutConfig(Attr attr) {
            this.gravity = attr.getIntegerValue();
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.gravity = layoutConfig.gravity;
        }

        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            nativeSetAdaptiveBoxLayoutParams(component.getNativeViewPtr(), new int[]{this.width, this.height, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, this.gravity});
            super.applyToComponent(component);
        }
    }
}
