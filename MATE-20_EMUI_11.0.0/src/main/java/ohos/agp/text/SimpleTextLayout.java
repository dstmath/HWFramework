package ohos.agp.text;

import ohos.agp.render.Paint;
import ohos.agp.utils.Rect;

public class SimpleTextLayout extends Layout {
    private native float nativeCalculateTextWidth(long j, String str, long j2);

    private native long nativeSimpleTextLayout(String str, long j, int[] iArr, int i, boolean z);

    public SimpleTextLayout(String str, Paint paint, Rect rect, int i) {
        this(str, paint, rect, i, false);
    }

    public SimpleTextLayout(String str, Paint paint, Rect rect, int i, boolean z) throws NullPointerException {
        if (paint == null || rect == null) {
            throw new NullPointerException("The paint and the rect are not null! ");
        }
        this.mNativeLayoutHandle = nativeSimpleTextLayout(str, paint.getNativeHandle(), new int[]{rect.left, rect.top, rect.right, rect.bottom}, i, z);
        initLayout(this.mNativeLayoutHandle);
    }

    public float calculateTextWidth(String str, Paint paint) {
        return nativeCalculateTextWidth(this.mNativeLayoutHandle, str, paint.getNativeHandle());
    }
}
