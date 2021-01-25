package ohos.agp.components;

import ohos.app.Context;

public class AbsoluteLayout extends ComponentContainer {
    private native long nativeGetAbsoluteLayoutHandle();

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetAbsoluteLayoutHandle();
        }
    }

    public AbsoluteLayout(Context context) {
        this(context, null);
    }

    public AbsoluteLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "AbsoluteLayoutDefaultStyle");
    }

    public AbsoluteLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }
}
