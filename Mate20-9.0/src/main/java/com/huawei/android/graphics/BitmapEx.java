package com.huawei.android.graphics;

import android.graphics.Bitmap;

public class BitmapEx {
    public static void setNinePatchChunk(Bitmap bitmap, byte[] chunk) {
        bitmap.setNinePatchChunk(chunk);
    }

    public static Bitmap createAshmemBitmap(Bitmap bitmap) {
        return bitmap.createAshmemBitmap();
    }
}
