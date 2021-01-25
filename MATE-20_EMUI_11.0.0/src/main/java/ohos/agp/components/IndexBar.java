package ohos.agp.components;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.Paint;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class IndexBar extends Component {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_INDEXBAR");
    private OnSelectedListener mListener;

    public interface OnSelectedListener {
        void onSelected(int i);
    }

    private native long nativeGetIndexBarHandle();

    private native int nativeGetSelectedCount(long j);

    private native void nativeInitIndexBar(long j);

    private native void nativeSetDiameter(long j, float f, float f2);

    private native void nativeSetIndexPaint(long j, long j2);

    private native void nativeSetIndexRadius(long j, float f);

    private native void nativeSetIndexString(long j, String[] strArr, long j2);

    private native void nativeSetSelectedCallback(long j, OnSelectedListener onSelectedListener);

    private native void nativeSetSelectedCount(long j, int i);

    private native void nativeSetSelectedPaint(long j, long j2);

    private native void nativeSetStringSelectedPaint(long j, long j2);

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, AttrSet attrSet) {
        this(context, attrSet, "IndexBarDefaultStyle");
    }

    public IndexBar(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mListener = null;
        nativeInitIndexBar(this.mNativeViewPtr);
    }

    @Deprecated
    public void setDiameter(float f, float f2) {
        nativeSetDiameter(this.mNativeViewPtr, f, f2);
    }

    public void setIndexRadius(float f) {
        nativeSetIndexRadius(this.mNativeViewPtr, f);
    }

    public void setIndexPaint(Paint paint) {
        nativeSetIndexPaint(this.mNativeViewPtr, paint.getNativeHandle());
    }

    public void setIndexString(String[] strArr, Paint paint) {
        nativeSetIndexString(this.mNativeViewPtr, strArr, paint.getNativeHandle());
    }

    public void setSelectedCallback(OnSelectedListener onSelectedListener) {
        this.mListener = onSelectedListener;
        nativeSetSelectedCallback(this.mNativeViewPtr, this.mListener);
    }

    public void setSelectedCount(int i) {
        nativeSetSelectedCount(this.mNativeViewPtr, i);
    }

    public void setSelectedPaint(Paint paint) {
        nativeSetSelectedPaint(this.mNativeViewPtr, paint.getNativeHandle());
    }

    public void setStringSelectedPaint(Paint paint) {
        nativeSetStringSelectedPaint(this.mNativeViewPtr, paint.getNativeHandle());
    }

    public int getSelectedCount() {
        return nativeGetSelectedCount(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetIndexBarHandle();
        }
    }
}
