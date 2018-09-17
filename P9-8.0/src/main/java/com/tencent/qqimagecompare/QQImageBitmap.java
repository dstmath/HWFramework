package com.tencent.qqimagecompare;

import android.graphics.Bitmap;

public class QQImageBitmap {
    long mThisC;

    public enum eColorFormat {
        QQIMAGE_CLR_UNKNOWN,
        QQIMAGE_CLR_RGBA8888,
        QQIMAGE_CLR_GRAY
    }

    QQImageBitmap() {
        this.mThisC = createNativeObject0();
    }

    public QQImageBitmap(eColorFormat ecolorformat, int i, int i2, int i3) {
        int i4 = 0;
        switch (ecolorformat) {
            case QQIMAGE_CLR_RGBA8888:
                i4 = 1;
                break;
            case QQIMAGE_CLR_GRAY:
                i4 = 2;
                break;
        }
        this.mThisC = createNativeObject4i(i4, i, i2, i3);
    }

    QQImageBitmap(boolean z) {
        if (!z) {
            this.mThisC = createNativeObject0();
        }
    }

    private native int ClipBitmapC(long j, Bitmap bitmap, int i, int i2);

    private native int ClipC(long j, long j2, int i, int i2);

    private native int GetHeightC(long j);

    private native int GetWidthC(long j);

    public static long createNativeObject() {
        return createNativeObject0();
    }

    private static native long createNativeObject0();

    private native long createNativeObject4i(int i, int i2, int i3, int i4);

    private native void destroyNativeObject(long j);

    public int clip(Bitmap bitmap, int i, int i2) {
        return ClipBitmapC(this.mThisC, bitmap, i, i2);
    }

    public int clip(QQImageBitmap qQImageBitmap, int i, int i2) {
        return ClipC(this.mThisC, qQImageBitmap.mThisC, i, i2);
    }

    public void delete() {
        if (this.mThisC != 0) {
            destroyNativeObject(this.mThisC);
            this.mThisC = 0;
        }
    }

    public int getHeight() {
        return GetHeightC(this.mThisC);
    }

    public int getWidth() {
        return GetWidthC(this.mThisC);
    }
}
