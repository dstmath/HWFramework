package com.huawei.iimagekit.common.agp.graphics.porting.androidfw;

import android.graphics.Bitmap;

public class HWBitmapUtil {
    public static native void destroyHardwareBitmapTexture(int i);

    public static native Bitmap initHardwareBitmap(int i, int i2);

    public static native int initHardwareBitmapTexture(Bitmap bitmap);
}
