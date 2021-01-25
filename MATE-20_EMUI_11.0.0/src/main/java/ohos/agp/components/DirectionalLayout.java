package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.LinearLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.DirectionalLayoutAttrsConstants;
import ohos.app.Context;

public class DirectionalLayout extends LinearLayout {
    public DirectionalLayout(Context context) {
        super(context, null);
    }

    public DirectionalLayout(Context context, AttrSet attrSet) {
        super(context, attrSet, "DirectionalLayoutDefaultStyle");
    }

    public DirectionalLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.LinearLayout
    public void setGravity(int i) {
        super.setGravity(i);
    }

    @Override // ohos.agp.components.LinearLayout
    public void setOrientation(int i) {
        super.setOrientation(i);
    }

    @Override // ohos.agp.components.LinearLayout
    public int getGravity() {
        return super.getGravity();
    }

    @Override // ohos.agp.components.LinearLayout
    public int getOrientation() {
        return super.getOrientation();
    }

    @Override // ohos.agp.components.LinearLayout
    public void setWeightSum(float f) {
        super.setWeightSum(f);
    }

    @Override // ohos.agp.components.LinearLayout
    public float getWeightSum() {
        return super.getWeightSum();
    }

    @Override // ohos.agp.components.LinearLayout
    public void setMeasureWithLargestChildEnabled(boolean z) {
        super.setMeasureWithLargestChildEnabled(z);
    }

    @Override // ohos.agp.components.LinearLayout
    public boolean isMeasureWithLargestChildEnabled() {
        return super.isMeasureWithLargestChildEnabled();
    }

    public static class LayoutConfig extends LinearLayout.LayoutParams {
        public static final int UNSPECIFIED_GRAVITY = -1;
        public static final float UNSPECIFIED_WEIGHT = 0.0f;
        public int gravity = -1;
        public float weight = 0.0f;

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
            this.gravity = attr.getIntegerValue();
        }

        public /* synthetic */ void lambda$new$1$DirectionalLayout$LayoutConfig(Attr attr) {
            this.weight = (float) attr.getIntegerValue();
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3, float f) {
            super(i, i2, i3, f);
            this.gravity = i3;
            this.weight = f;
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super((LinearLayout.LayoutParams) layoutConfig);
            this.gravity = layoutConfig.gravity;
            this.weight = layoutConfig.weight;
        }

        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            super.applyToComponent(component.getNativeViewPtr(), new int[]{this.width, this.height, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, this.gravity}, this.weight);
        }
    }

    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (layoutConfig instanceof LayoutConfig) {
            return layoutConfig;
        }
        return new LayoutConfig(layoutConfig);
    }

    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutConfig(context, attrSet);
    }
}
