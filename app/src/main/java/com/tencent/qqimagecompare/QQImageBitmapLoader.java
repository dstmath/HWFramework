package com.tencent.qqimagecompare;

/* compiled from: Unknown */
public class QQImageBitmapLoader {
    private static native long DecodeJpegFileSubImage1s6iC(String str, int i, int i2, int i3, int i4, int i5, int i6);

    public static QQImageBitmap loadBitmapSubImage(String str, int i, int i2, int i3, int i4, QQImageLoaderHeadInfo qQImageLoaderHeadInfo) {
        if (!qQImageLoaderHeadInfo.bJpeg) {
            return null;
        }
        QQImageBitmap qQImageBitmap = new QQImageBitmap(true);
        qQImageBitmap.mThisC = DecodeJpegFileSubImage1s6iC(str, i, i2, i3, i4, qQImageLoaderHeadInfo.mMCUWidth, qQImageLoaderHeadInfo.mMCUHeight);
        return qQImageBitmap;
    }
}
