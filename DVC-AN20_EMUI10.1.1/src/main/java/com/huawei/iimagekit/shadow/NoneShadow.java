package com.huawei.iimagekit.shadow;

import android.graphics.Bitmap;

public class NoneShadow {
    public static void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        blurredBitmap.copy(Bitmap.createBitmap(bitmapForBlur.getWidth(), bitmapForBlur.getHeight(), Bitmap.Config.ARGB_8888).getConfig(), true);
    }
}
