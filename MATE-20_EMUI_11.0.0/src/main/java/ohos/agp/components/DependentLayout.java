package ohos.agp.components;

import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.RelativeLayout;
import ohos.agp.styles.Style;
import ohos.app.Context;

public class DependentLayout extends RelativeLayout {
    public static final int ABOVE = 2;
    public static final int ALIGN_BASELINE = 4;
    public static final int ALIGN_BOTTOM = 8;
    public static final int ALIGN_END = 19;
    public static final int ALIGN_LEFT = 5;
    public static final int ALIGN_PARENT_BOTTOM = 12;
    public static final int ALIGN_PARENT_END = 21;
    public static final int ALIGN_PARENT_LEFT = 9;
    public static final int ALIGN_PARENT_RIGHT = 11;
    public static final int ALIGN_PARENT_START = 20;
    public static final int ALIGN_PARENT_TOP = 10;
    public static final int ALIGN_RIGHT = 7;
    public static final int ALIGN_START = 18;
    public static final int ALIGN_TOP = 6;
    public static final int BELOW = 3;
    public static final int CENTER_HORIZONTAL = 14;
    public static final int CENTER_IN_PARENT = 13;
    public static final int CENTER_VERTICAL = 15;
    public static final int END_OF = 17;
    public static final int LEFT_OF = 0;
    public static final int RIGHT_OF = 1;
    public static final int START_OF = 16;
    public static final int TRUE = -1;
    private static final int VERB_COUNT = 22;

    public DependentLayout(Context context) {
        super(context);
    }

    public DependentLayout(Context context, AttrSet attrSet) {
        super(context, attrSet);
    }

    public DependentLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    @Override // ohos.agp.components.RelativeLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (layoutConfig instanceof LayoutConfig) {
            return layoutConfig;
        }
        return new LayoutConfig(layoutConfig);
    }

    @Override // ohos.agp.components.RelativeLayout, ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutConfig(context, attrSet);
    }

    public static class LayoutConfig extends RelativeLayout.LayoutParams {
        public LayoutConfig() {
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            super(context, attrSet);
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super((RelativeLayout.LayoutParams) layoutConfig);
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        @Override // ohos.agp.components.RelativeLayout.LayoutParams, ohos.agp.components.ComponentContainer.LayoutConfig, java.lang.Object
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        @Override // ohos.agp.components.RelativeLayout.LayoutParams
        public void addRule(int i, int i2) {
            super.addRule(i, i2);
        }

        @Override // ohos.agp.components.RelativeLayout.LayoutParams
        public void addRule(int i) {
            super.addRule(i);
        }

        @Override // ohos.agp.components.RelativeLayout.LayoutParams
        public void removeRule(int i) {
            super.removeRule(i);
        }

        @Override // ohos.agp.components.RelativeLayout.LayoutParams
        public int getRule(int i) {
            return super.getRule(i);
        }

        @Override // ohos.agp.components.RelativeLayout.LayoutParams
        public int[] getRules() {
            return super.getRules();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.components.RelativeLayout.LayoutParams, ohos.agp.components.ComponentContainer.LayoutConfig
        public void resolveLayoutDirection(Component.LayoutDirection layoutDirection) {
            super.resolveLayoutDirection(layoutDirection);
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.RelativeLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.RelativeLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        super.createNativePtr();
    }

    @Override // ohos.agp.components.RelativeLayout
    public int getIgnoreGravity() {
        return super.getIgnoreGravity();
    }

    @Override // ohos.agp.components.RelativeLayout
    public void setIgnoreGravity(int i) {
        super.setIgnoreGravity(i);
    }

    @Override // ohos.agp.components.RelativeLayout
    public int getGravity() {
        return super.getGravity();
    }

    @Override // ohos.agp.components.RelativeLayout
    public void setGravity(int i) {
        super.setGravity(i);
    }
}
