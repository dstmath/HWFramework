package ohos.agp.components;

import ohos.app.Context;

public class NestedScrollCoordinator extends DirectionalLayout {
    private native long nativeGetHandle();

    public NestedScrollCoordinator(Context context) {
        this(context, null);
    }

    public NestedScrollCoordinator(Context context, AttrSet attrSet) {
        this(context, attrSet, "NestedScrollCoordinatorDefaultStyle");
    }

    public NestedScrollCoordinator(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetHandle();
        }
    }
}
