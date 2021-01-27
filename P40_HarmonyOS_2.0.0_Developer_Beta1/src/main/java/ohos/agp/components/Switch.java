package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.SwitchAttrsConstants;
import ohos.app.Context;

public class Switch extends AbsButton {
    private Element mTrackElement;

    private native long nativeGetSwitchHandle();

    private native String nativeGetSwitchTextOff(long j);

    private native String nativeGetSwitchTextOn(long j);

    private native int nativeGetThumbHeight(long j);

    private native int nativeGetThumbHorizontalPadding(long j);

    private native int nativeGetThumbVerticalPadding(long j);

    private native int nativeGetThumbWidth(long j);

    private native void nativeSetSwitchTextOff(long j, String str);

    private native void nativeSetSwitchTextOn(long j, String str);

    private native void nativeSetThumbPadding(long j, int i, int i2);

    private native void nativeSetThumbSize(long j, int i, int i2);

    private native void nativeSetTrackElement(long j, long j2);

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, AttrSet attrSet) {
        this(context, attrSet, "SwitchDefaultStyle");
    }

    public Switch(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mTrackElement = null;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.AbsButton, ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getSwitchAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Button, ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetSwitchHandle();
        }
    }

    public Element getTrackElement() {
        return this.mTrackElement;
    }

    public Element getThumbElement() {
        return getButtonElement();
    }

    public String getStateOnText() {
        return nativeGetSwitchTextOn(this.mNativeViewPtr);
    }

    public String getStateOffText() {
        return nativeGetSwitchTextOff(this.mNativeViewPtr);
    }

    public void setStateOnText(String str) {
        nativeSetSwitchTextOn(this.mNativeViewPtr, str);
    }

    public void setStateOffText(String str) {
        nativeSetSwitchTextOff(this.mNativeViewPtr, str);
    }

    public void setThumbPadding(int i, int i2) {
        nativeSetThumbPadding(this.mNativeViewPtr, i, i2);
    }

    public int[] getThumbPadding() {
        return new int[]{nativeGetThumbHorizontalPadding(this.mNativeViewPtr), nativeGetThumbVerticalPadding(this.mNativeViewPtr)};
    }

    public int getThumbHorizontalPadding() {
        return nativeGetThumbHorizontalPadding(this.mNativeViewPtr);
    }

    public void setThumbHorizontalPadding(int i) {
        setThumbPadding(i, nativeGetThumbVerticalPadding(this.mNativeViewPtr));
    }

    public int getThumbVerticalPadding() {
        return nativeGetThumbVerticalPadding(this.mNativeViewPtr);
    }

    public void setThumbVerticalPadding(int i) {
        setThumbPadding(nativeGetThumbHorizontalPadding(this.mNativeViewPtr), i);
    }

    public void setThumbSize(int i, int i2) {
        nativeSetThumbSize(this.mNativeViewPtr, i2, i);
    }

    public int[] getThumbSize() {
        return new int[]{nativeGetThumbWidth(this.mNativeViewPtr), nativeGetThumbHeight(this.mNativeViewPtr)};
    }

    public int getThumbWidth() {
        return nativeGetThumbWidth(this.mNativeViewPtr);
    }

    public void setThumbWidth(int i) {
        setThumbSize(i, nativeGetThumbHeight(this.mNativeViewPtr));
    }

    public int getThumbHeight() {
        return nativeGetThumbHeight(this.mNativeViewPtr);
    }

    public void setThumbHeight(int i) {
        setThumbSize(nativeGetThumbWidth(this.mNativeViewPtr), i);
    }

    public void setTrackElement(Element element) {
        this.mTrackElement = element;
        nativeSetTrackElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public void setThumbElement(Element element) {
        setButtonElement(element);
    }

    @Override // ohos.agp.components.AbsButton, ohos.agp.components.Text, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(SwitchAttrsConstants.TRACK_ELEMENT)) {
            setTrackElement(style.getPropertyValue(SwitchAttrsConstants.TRACK_ELEMENT).asElement());
        }
        if (style.hasProperty("thumb_element")) {
            setThumbElement(style.getPropertyValue("thumb_element").asElement());
        }
    }
}
