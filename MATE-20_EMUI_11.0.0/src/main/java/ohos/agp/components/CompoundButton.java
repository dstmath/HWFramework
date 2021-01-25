package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.AbsButtonAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

public abstract class CompoundButton extends Button {
    private Element mButtonDrawable = null;
    protected OnCheckedChangeListener mCheckedListener = null;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CompoundButton compoundButton, boolean z);
    }

    private native void nativeCompoundButtonToggle(long j);

    private native int nativeGetTextColorOff(long j);

    private native int nativeGetTextColorOn(long j);

    private native boolean nativeIsCompoundButtonChecked(long j);

    private native void nativeSetButtonDrawable(long j, long j2);

    private native void nativeSetCompoundButtonCallback(long j, OnCheckedChangeListener onCheckedChangeListener);

    private native void nativeSetCompoundButtonChecked(long j, boolean z);

    private native void nativeSetTextColorOff(long j, int i);

    private native void nativeSetTextColorOn(long j, int i);

    public CompoundButton(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        AttrSet mergeStyle = AttrHelper.mergeStyle(context, attrSet, 0);
        for (int i = 0; i < mergeStyle.getLength(); i++) {
            mergeStyle.getAttr(i).ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$CompoundButton$e7GUYSuoFw3spJfu70fNSTS18 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    CompoundButton.this.lambda$new$0$CompoundButton((Attr) obj);
                }
            });
        }
    }

    public /* synthetic */ void lambda$new$0$CompoundButton(Attr attr) {
        String name = attr.getName();
        if (((name.hashCode() == 742313895 && name.equals(AbsButtonAttrsConstants.CHECKED)) ? (char) 0 : 65535) == 0) {
            setChecked(attr.getBoolValue());
        }
    }

    public void setChecked(boolean z) {
        nativeSetCompoundButtonChecked(this.mNativeViewPtr, z);
    }

    public boolean isChecked() {
        return nativeIsCompoundButtonChecked(this.mNativeViewPtr);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.mCheckedListener = onCheckedChangeListener;
        nativeSetCompoundButtonCallback(this.mNativeViewPtr, this.mCheckedListener);
    }

    public void toggle() {
        nativeCompoundButtonToggle(this.mNativeViewPtr);
    }

    public Color getTextColorOn() {
        return new Color(nativeGetTextColorOn(this.mNativeViewPtr));
    }

    public Color getTextColorOff() {
        return new Color(nativeGetTextColorOff(this.mNativeViewPtr));
    }

    public void setTextColorOn(Color color) {
        nativeSetTextColorOn(this.mNativeViewPtr, color.getValue());
    }

    public void setTextColorOff(Color color) {
        nativeSetTextColorOff(this.mNativeViewPtr, color.getValue());
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new AbsButtonAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
    }
}
