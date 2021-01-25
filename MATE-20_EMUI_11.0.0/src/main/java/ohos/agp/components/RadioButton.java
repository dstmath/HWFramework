package ohos.agp.components;

import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.RadioButtonAttrsConstants;
import ohos.app.Context;

public class RadioButton extends AbsButton {
    private native long nativeGetRadioButtonHandle();

    public RadioButton(Context context) {
        this(context, null);
    }

    public RadioButton(Context context, AttrSet attrSet) {
        this(context, attrSet, "RadioButtonDefaultStyle");
    }

    public RadioButton(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.AbsButton, ohos.agp.components.CompoundButton, ohos.agp.components.Text, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new RadioButtonAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Button, ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetRadioButtonHandle();
        }
    }
}
