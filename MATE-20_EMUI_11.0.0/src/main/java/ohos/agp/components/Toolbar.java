package ohos.agp.components;

import ohos.agp.components.ComponentContainer;
import ohos.agp.components.StackLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ToolbarAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

@Deprecated
public class Toolbar extends ComponentContainer {
    private native String nativeGetLogoDescription(long j);

    private native int nativeGetSubtitleTextColor(long j);

    private native int nativeGetTitleTextColor(long j);

    private native long nativeGetToolbarHandle();

    private native String nativeGetToolbarLogo(long j);

    private native String nativeGetToolbarSubtitle(long j);

    private native String nativeGetToolbarTitle(long j);

    private native int nativeGetToolbarTitleMarginBottom(long j);

    private native int nativeGetToolbarTitleMarginEnd(long j);

    private native int nativeGetToolbarTitleMarginStart(long j);

    private native int nativeGetToolbarTitleMarginTop(long j);

    private native void nativeSetLogoDescription(long j, String str);

    private native void nativeSetSubtitleTextColor(long j, int i);

    private native void nativeSetTitleTextColor(long j, int i);

    private native void nativeSetToolbarLogo(long j, String str);

    private native void nativeSetToolbarSubtitle(long j, String str);

    private native void nativeSetToolbarTitle(long j, String str);

    private native void nativeSetToolbarTitleMargin(long j, int i, int i2, int i3, int i4);

    private native void nativeSetToolbarTitleMarginBottom(long j, int i);

    private native void nativeSetToolbarTitleMarginEnd(long j, int i);

    private native void nativeSetToolbarTitleMarginStart(long j, int i);

    private native void nativeSetToolbarTitleMarginTop(long j, int i);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetToolbarHandle();
        }
    }

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public Toolbar(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ToolbarAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    public void setTitle(String str) {
        nativeSetToolbarTitle(this.mNativeViewPtr, str);
    }

    public String getTitle() {
        return nativeGetToolbarTitle(this.mNativeViewPtr);
    }

    public void setSubtitle(String str) {
        nativeSetToolbarSubtitle(this.mNativeViewPtr, str);
    }

    public String getSubtitle() {
        return nativeGetToolbarSubtitle(this.mNativeViewPtr);
    }

    public void setLogo(String str) {
        nativeSetToolbarLogo(this.mNativeViewPtr, str);
    }

    public String getLogo() {
        return nativeGetToolbarLogo(this.mNativeViewPtr);
    }

    public void setLogoDescription(String str) {
        nativeSetLogoDescription(this.mNativeViewPtr, str);
    }

    public String getLogoDescription() {
        return nativeGetLogoDescription(this.mNativeViewPtr);
    }

    public void setTitleMargin(int i, int i2, int i3, int i4) {
        nativeSetToolbarTitleMargin(this.mNativeViewPtr, i, i2, i3, i4);
    }

    public void setTitleMarginStart(int i) {
        nativeSetToolbarTitleMarginStart(this.mNativeViewPtr, i);
    }

    public int getTitleMarginStart() {
        return nativeGetToolbarTitleMarginStart(this.mNativeViewPtr);
    }

    public void setTitleMarginTop(int i) {
        nativeSetToolbarTitleMarginTop(this.mNativeViewPtr, i);
    }

    public int getTitleMarginTop() {
        return nativeGetToolbarTitleMarginTop(this.mNativeViewPtr);
    }

    public void setTitleMarginEnd(int i) {
        nativeSetToolbarTitleMarginEnd(this.mNativeViewPtr, i);
    }

    public int getTitleMarginEnd() {
        return nativeGetToolbarTitleMarginEnd(this.mNativeViewPtr);
    }

    public void setTitleMarginBottom(int i) {
        nativeSetToolbarTitleMarginBottom(this.mNativeViewPtr, i);
    }

    public int getTitleMarginBottom() {
        return nativeGetToolbarTitleMarginBottom(this.mNativeViewPtr);
    }

    public void setTitleTextColor(Color color) {
        nativeSetTitleTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getTitleTextColor() {
        return new Color(nativeGetTitleTextColor(this.mNativeViewPtr));
    }

    public void setSubtitleTextColor(Color color) {
        nativeSetSubtitleTextColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getSubtitleTextColor() {
        return new Color(nativeGetSubtitleTextColor(this.mNativeViewPtr));
    }

    public static class LayoutConfig extends StackLayout.LayoutConfig {
        static final int CUSTOM = 0;
        static final int EXPANDED = 2;
        static final int SYSTEM = 1;
        public int viewType = 0;

        private native void nativeSetToolbarLayoutParams(long j, int[] iArr);

        public LayoutConfig() {
        }

        public LayoutConfig(int i) {
            super(-2, -1, i);
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3) {
            super(i, i2, i3);
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super((StackLayout.LayoutConfig) layoutConfig);
            this.viewType = layoutConfig.viewType;
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        @Override // ohos.agp.components.StackLayout.LayoutConfig, ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            nativeSetToolbarLayoutParams(component.getNativeViewPtr(), new int[]{this.width, this.height, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin, this.viewType, this.gravity});
        }
    }
}
