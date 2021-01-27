package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.AbsButtonAttrsConstants;
import ohos.agp.utils.Color;
import ohos.agp.utils.ErrorHandler;
import ohos.app.Context;

public abstract class AbsButton extends Button {
    private Element mButtonElement;
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

    public AbsButton(Context context) {
        this(context, null);
    }

    public AbsButton(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public AbsButton(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mButtonElement = null;
        AttrSet mergeStyle = AttrHelper.mergeStyle(context, attrSet, 0);
        for (int i = 0; i < mergeStyle.getLength(); i++) {
            mergeStyle.getAttr(i).ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$AbsButton$25ehkQogUkSZE2W6NEie_orbh0 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AbsButton.this.lambda$new$0$AbsButton((Attr) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$new$0$AbsButton(Attr attr) {
        String name = attr.getName();
        if (((name.hashCode() == -1081306068 && name.equals(AbsButtonAttrsConstants.MARKED)) ? (char) 0 : 65535) == 0) {
            setChecked(attr.getBoolValue());
        }
    }

    public void setChecked(boolean z) {
        nativeSetChecked(this.mNativeViewPtr, z);
    }

    public boolean isChecked() {
        return nativeIsChecked(this.mNativeViewPtr);
    }

    public void setCheckedStateChangedListener(CheckedStateChangedListener checkedStateChangedListener) {
        this.mListener = checkedStateChangedListener;
        nativeSetCheckedListener(this.mNativeViewPtr, this.mListener);
    }

    public void toggle() {
        nativeToggle(this.mNativeViewPtr);
    }

    public Color getTextColorOn() {
        return new Color(nativeGetTextColorOn(this.mNativeViewPtr));
    }

    public Color getTextColorOff() {
        return new Color(nativeGetTextColorOff(this.mNativeViewPtr));
    }

    public void setTextColorOn(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetTextColorOn(this.mNativeViewPtr, color.getValue());
    }

    public void setTextColorOff(Color color) {
        ErrorHandler.validateParamNotNull(color);
        nativeSetTextColorOff(this.mNativeViewPtr, color.getValue());
    }

    public void setButtonElement(Element element) {
        this.mButtonElement = element;
        nativeSetStateElement(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getButtonElement() {
        return this.mButtonElement;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getAbsButtonAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(AbsButtonAttrsConstants.CHECK_ELEMENT)) {
            setButtonElement(style.getPropertyValue(AbsButtonAttrsConstants.CHECK_ELEMENT).asElement());
        }
    }
}
