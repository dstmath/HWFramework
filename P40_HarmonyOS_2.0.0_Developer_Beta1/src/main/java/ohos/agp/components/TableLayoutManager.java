package ohos.agp.components;

public class TableLayoutManager extends LayoutManager {
    private native int nativeGetColumnCount(long j);

    private native long nativeGetHandle();

    private native int nativeGetRowCount(long j);

    private native void nativeSetColumnCount(long j, int i);

    private native void nativeSetRowCount(long j, int i);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LayoutManager
    public void createNativePtr() {
        if (this.mNativePtr == 0) {
            this.mNativePtr = nativeGetHandle();
        }
    }

    public void setColumnCount(int i) {
        nativeSetColumnCount(this.mNativePtr, i);
    }

    public int getColumnCount() {
        return nativeGetColumnCount(this.mNativePtr);
    }

    public void setRowCount(int i) {
        nativeSetRowCount(this.mNativePtr, i);
    }

    public int getRowCount() {
        return nativeGetRowCount(this.mNativePtr);
    }
}
