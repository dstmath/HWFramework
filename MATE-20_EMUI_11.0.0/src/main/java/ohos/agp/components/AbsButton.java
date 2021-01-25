package ohos.agp.components;

import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.AbsButtonAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

public abstract class AbsButton extends CompoundButton {
    private Element mButtonElement = null;
    private CheckedStateChangedListener mListener;

    public interface CheckedStateChangedListener {
        void onCheckedChanged(AbsButton absButton, boolean z);
    }

    private native int nativeGetTextColorOff(long j);

    private native int nativeGetTextColorOn(long j);

    private native boolean nativeIsChecked(long j);

    private native void nativeSetChecked(long j, boolean z);

    private native void nativeSetCheckedListener(long j, CheckedStateChangedListener checkedStateChangedListener);

    private native void nativeSetStateElement(long j, long j2);

    private native void nativeSetTextColorOff(long j, int i);

    private native void nativeSetTextColorOn(long j, int i);

    private native void nativeToggle(long j);

    public AbsButton(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    @Override // ohos.agp.components.CompoundButton
    public void setChecked(boolean z) {
        nativeSetChecked(this.mNativeViewPtr, z);
    }

    @Override // ohos.agp.components.CompoundButton
    public boolean isChecked() {
        return nativeIsChecked(this.mNativeViewPtr);
    }

    public void setCheckedStateChangedListener(CheckedStateChangedListener checkedStateChangedListener) {
        this.mListener = checkedStateChangedListener;
        nativeSetCheckedListener(this.mNativeViewPtr, this.mListener);
    }

    @Override // ohos.agp.components.CompoundButton
    public void toggle() {
        nativeToggle(this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.CompoundButton
    public Color getTextColorOn() {
        return new Color(nativeGetTextColorOn(this.mNativeViewPtr));
    }

    @Override // ohos.agp.components.CompoundButton
    public Color getTextColorOff() {
        return new Color(nativeGetTextColorOff(this.mNativeViewPtr));
    }

    @Override // ohos.agp.components.CompoundButton
    public void setTextColorOn(Color color) {
        if (color != null) {
            nativeSetTextColorOn(this.mNativeViewPtr, color.getValue());
        }
    }

    @Override // ohos.agp.components.CompoundButton
    public void setTextColorOff(Color color) {
        if (color != null) {
            nativeSetTextColorOff(this.mNativeViewPtr, color.getValue());
        }
    }

    public void setButtonElement(Element element) {
        this.mButtonElement = element;
        nativeSetStateElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getButtonElement() {
        return this.mButtonElement;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.CompoundButton, ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new AbsButtonAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.CompoundButton, ohos.agp.components.Text, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(AbsButtonAttrsConstants.CHECK_ELEMENT)) {
            setButtonElement(style.getPropertyValue(AbsButtonAttrsConstants.CHECK_ELEMENT).asElement());
        }
    }
}
