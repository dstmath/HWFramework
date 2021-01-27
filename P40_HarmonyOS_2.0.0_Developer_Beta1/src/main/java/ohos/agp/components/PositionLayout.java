package ohos.agp.components;

import ohos.app.Context;

public class PositionLayout extends ComponentContainer {
    private native long nativeGetHandle();

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetHandle();
        }
    }

    public PositionLayout(Context context) {
        this(context, null);
    }

    public PositionLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "PositionLayoutDefaultStyle");
    }

    public PositionLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }
}
