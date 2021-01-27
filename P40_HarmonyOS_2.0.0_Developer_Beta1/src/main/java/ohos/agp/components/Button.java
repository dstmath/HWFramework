package ohos.agp.components;

import ohos.app.Context;

public class Button extends Text {
    private native long nativeGetButtonHandle();

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Text, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetButtonHandle();
        }
    }

    public Button(Context context) {
        this(context, null);
    }

    public Button(Context context, AttrSet attrSet) {
        this(context, attrSet, "ButtonDefaultStyle");
    }

    public Button(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }
}
