package ohos.agp.components;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.render.Paint;
import ohos.app.Context;
import ohos.hiviewdfx.HiLogLabel;

public class IndexBar extends Component {
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_INDEXBAR");
    private Paint mIndexPaint;
    private String[] mIndexString;
    private OnSelectedListener mListener;
    private Paint mSelectedPaint;
    private Paint mStringPaint;
    private Paint mStringSelectedPaint;

    public interface OnSelectedListener {
        void onSelected(int i);
    }

    private native long nativeGetIndexBarHandle();

    private native int nativeGetIndexBarStatus(long j);

    private native float nativeGetIndexRadius(long j);

    private native int nativeGetMaxCount(long j);

    private native int nativeGetMinCount(long j);

    private native int nativeGetSelectedCount(long j);

    private native String nativeGetSelectedString(long j);

    private native void nativeInitIndexBar(long j);

    private native void nativeLanguageSwitch(long j, String[] strArr);

    private native void nativeSetIndexBarExpandedStatus(long j, boolean z);

    private native void nativeSetIndexPaint(long j, long j2);

    private native void nativeSetIndexRadius(long j, float f);

    private native void nativeSetIndexString(long j, String[] strArr, long j2);

    private native void nativeSetMaxCount(long j, int i);

    private native void nativeSetMinCount(long j, int i);

    private native void nativeSetMinCountAndMaxCount(long j, int i, int i2);

    private native void nativeSetSelectedCallback(long j, OnSelectedListener onSelectedListener);

    private native void nativeSetSelectedCount(long j, int i);

    private native void nativeSetSelectedPaint(long j, long j2);

    private native void nativeSetSelectedString(long j, String str);

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

    public void setIndexRadius(float f) {
        nativeSetIndexRadius(this.mNativeViewPtr, f);
    }

    public float getIndexRadius() {
        return nativeGetIndexRadius(this.mNativeViewPtr);
    }

    public void setIndexPaint(Paint paint) {
        this.mIndexPaint = paint;
        nativeSetIndexPaint(this.mNativeViewPtr, paint.getNativeHandle());
    }

    public void setIndexString(String[] strArr, Paint paint) {
        this.mStringPaint = paint;
        String[] strArr2 = new String[strArr.length];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        this.mIndexString = strArr2;
        nativeSetIndexString(this.mNativeViewPtr, strArr2, paint.getNativeHandle());
    }

    public void languageSwitch(String[] strArr) {
        String[] strArr2 = new String[strArr.length];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        this.mIndexString = strArr2;
        nativeLanguageSwitch(this.mNativeViewPtr, strArr2);
    }

    public String[] getIndexString() {
        String[] strArr = this.mIndexString;
        String[] strArr2 = new String[strArr.length];
        System.arraycopy(strArr, 0, strArr2, 0, strArr.length);
        return strArr2;
    }

    public void setSelectedCallback(OnSelectedListener onSelectedListener) {
        this.mListener = onSelectedListener;
        nativeSetSelectedCallback(this.mNativeViewPtr, this.mListener);
    }

    public OnSelectedListener getSelectedCallback() {
        return this.mListener;
    }

    public void setSelectedCount(int i) {
        nativeSetSelectedCount(this.mNativeViewPtr, i);
    }

    public void setSelectedPaint(Paint paint) {
        this.mSelectedPaint = paint;
        nativeSetSelectedPaint(this.mNativeViewPtr, paint.getNativeHandle());
    }

    public void setStringSelectedPaint(Paint paint) {
        this.mStringSelectedPaint = paint;
        nativeSetStringSelectedPaint(this.mNativeViewPtr, paint.getNativeHandle());
    }

    public void setIndexBarExpandedStatus(boolean z) {
        nativeSetIndexBarExpandedStatus(this.mNativeViewPtr, z);
    }

    public int getIndexBarStatus() {
        return nativeGetIndexBarStatus(this.mNativeViewPtr);
    }

    public int getSelectedCount() {
        return nativeGetSelectedCount(this.mNativeViewPtr);
    }

    public void setSelectedString(String str) {
        nativeSetSelectedString(this.mNativeViewPtr, str);
    }

    public String getSelectedString() {
        return nativeGetSelectedString(this.mNativeViewPtr);
    }

    public void setMaxCount(int i) {
        nativeSetMaxCount(this.mNativeViewPtr, i);
    }

    public int getMaxCount() {
        return nativeGetMaxCount(this.mNativeViewPtr);
    }

    public void setMinCount(int i) {
        nativeSetMinCount(this.mNativeViewPtr, i);
    }

    public int getMinCount() {
        return nativeGetMinCount(this.mNativeViewPtr);
    }

    public void setMinCountAndMaxCount(int i, int i2) {
        nativeSetMinCountAndMaxCount(this.mNativeViewPtr, i, i2);
    }

    public Paint getIndexPaint() {
        return this.mIndexPaint;
    }

    public Paint getIndexStringPaint() {
        return this.mStringPaint;
    }

    public Paint getSelectedPaint() {
        return this.mSelectedPaint;
    }

    public Paint getStringSelectedPaint() {
        return this.mStringSelectedPaint;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetIndexBarHandle();
        }
    }
}
