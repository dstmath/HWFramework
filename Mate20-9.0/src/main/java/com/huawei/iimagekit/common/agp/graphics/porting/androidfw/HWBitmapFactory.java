package com.huawei.iimagekit.common.agp.graphics.porting.androidfw;

import android.graphics.Bitmap;

public class HWBitmapFactory {
    public static native void destroy(long j);

    public static native Bitmap getBitmap(long j);

    public static native long init(long j, int i, int i2);

    public static native void render(long j);
}
