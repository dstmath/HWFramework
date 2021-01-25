package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class NoneBlur {
    public static void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        new Canvas(blurredBitmap).drawBitmap(bitmapForBlur, new Matrix(), new Paint());
    }
}
