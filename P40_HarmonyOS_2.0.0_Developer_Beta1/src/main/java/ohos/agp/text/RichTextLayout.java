package ohos.agp.text;

import ohos.agp.render.Paint;
import ohos.agp.utils.Rect;

public class RichTextLayout extends Layout {
    private native float nativeCalculateTextWidth(long j, long j2);

    private native long nativeRichTextLayout(long j, long j2, int[] iArr, int i, boolean z);

    public RichTextLayout(RichText richText, Paint paint, Rect rect, int i) {
        this(richText, paint, rect, i, false);
    }

    public RichTextLayout(RichText richText, Paint paint, Rect rect, int i, boolean z) throws NullPointerException {
        if (paint == null || rect == null) {
            throw new NullPointerException("The paint and the rect are not null! ");
        }
        this.mNativeLayoutHandle = nativeRichTextLayout(richText.getNativeRichText(), paint.getNativeHandle(), new int[]{rect.left, rect.top, rect.right, rect.bottom}, i, z);
        initLayout(this.mNativeLayoutHandle);
    }

    public float calculateTextWidth(RichText richText) {
        return nativeCalculateTextWidth(this.mNativeLayoutHandle, richText.getNativeRichText());
    }
}
