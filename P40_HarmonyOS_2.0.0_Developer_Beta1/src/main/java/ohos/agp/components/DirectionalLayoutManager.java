package ohos.agp.components;

public class DirectionalLayoutManager extends LayoutManager {
    private native long nativeGetHandle();

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LayoutManager
    public void createNativePtr() {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetHandle();
        }
    }
}
