package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.styles.Style;
import ohos.app.Context;

public class ToggleButton extends AbsButton {
    private native long nativeGetToggleButtonHandle();

    private native String nativeGetToggleButtonTextOff(long j);

    private native String nativeGetToggleButtonTextOn(long j);

    private native void nativeSetToggleButtonTextOff(long j, String str);

    private native void nativeSetToggleButtonTextOn(long j, String str);

    public ToggleButton(Context context) {
        this(context, null);
    }

    public ToggleButton(Context context, AttrSet attrSet) {
        this(context, attrSet, "ToggleButtonDefaultStyle");
    }

    public ToggleButton(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        AttrSet mergeStyle = AttrHelper.mergeStyle(context, attrSet, 0);
        for (int i = 0; i < mergeStyle.getLength(); i++) {
            mergeStyle.getAttr(i).ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$ToggleButton$i8evW0r_fQOEgz9Bi7xB6LUbIIM */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ToggleButton.this.lambda$new$0$ToggleButton((Attr) obj);
                }
            });
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0038  */
    public /* synthetic */ void lambda$new$0$ToggleButton(Attr attr) {
        char c;
        String name = attr.getName();
        int hashCode = name.hashCode();
        if (hashCode != -1341563857) {
            if (hashCode == -320370913 && name.equals("text_state_on")) {
                c = 0;
                if (c == 0) {
                    setStateOnText(attr.getStringValue());
                    return;
                } else if (c == 1) {
                    setStateOffText(attr.getStringValue());
                    return;
                } else {
                    return;
                }
            }
        } else if (name.equals("text_state_off")) {
            c = 1;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Button, ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetToggleButtonHandle();
        }
    }

    public String getStateOnText() {
        return nativeGetToggleButtonTextOn(this.mNativeViewPtr);
    }

    public String getStateOffText() {
        return nativeGetToggleButtonTextOff(this.mNativeViewPtr);
    }

    public void setStateOnText(String str) {
        nativeSetToggleButtonTextOn(this.mNativeViewPtr, str);
    }

    public void setStateOffText(String str) {
        nativeSetToggleButtonTextOff(this.mNativeViewPtr, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.AbsButton, ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getToggleButtonAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.AbsButton, ohos.agp.components.Text, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
    }
}
