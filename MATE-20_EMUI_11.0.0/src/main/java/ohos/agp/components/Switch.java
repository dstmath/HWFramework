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

    private native void nativeSetSwitchTextOff(long j, String str);

    private native void nativeSetSwitchTextOn(long j, String str);

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
    @Override // ohos.agp.components.AbsButton, ohos.agp.components.CompoundButton, ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new SwitchAttrsConstants();
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

    @Deprecated
    public Element getTrackDrawable() {
        return this.mTrackElement;
    }

    public Element getTrackElement() {
        return this.mTrackElement;
    }

    @Deprecated
    public Element getThumbDrawable() {
        return getButtonElement();
    }

    public Element getThumbElement() {
        return getButtonElement();
    }

    public String getTextOn() {
        return nativeGetSwitchTextOn(this.mNativeViewPtr);
    }

    public String getTextOff() {
        return nativeGetSwitchTextOff(this.mNativeViewPtr);
    }

    public void setTextOn(String str) {
        nativeSetSwitchTextOn(this.mNativeViewPtr, str);
    }

    public void setTextOff(String str) {
        nativeSetSwitchTextOff(this.mNativeViewPtr, str);
    }

    @Deprecated
    public void setTrackDrawable(Element element) {
        setTrackElement(element);
    }

    public void setTrackElement(Element element) {
        this.mTrackElement = element;
        nativeSetTrackElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    @Deprecated
    public void setThumbDrawable(Element element) {
        setButtonElement(element);
    }

    public void setThumbElement(Element element) {
        setButtonElement(element);
    }

    @Override // ohos.agp.components.AbsButton, ohos.agp.components.CompoundButton, ohos.agp.components.Text, ohos.agp.components.Component
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
