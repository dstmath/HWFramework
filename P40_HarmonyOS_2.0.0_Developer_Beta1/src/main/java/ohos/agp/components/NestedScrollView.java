package ohos.agp.components;

import ohos.app.Context;

public class NestedScrollView extends ScrollView {
    private native long nativeGetNestedScrollViewHandle();

    public NestedScrollView(Context context) {
        this(context, null);
    }

    public NestedScrollView(Context context, AttrSet attrSet) {
        this(context, attrSet, "NestedScrollViewDefaultStyle");
    }

    public NestedScrollView(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ScrollView, ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetNestedScrollViewHandle();
        }
    }
}
