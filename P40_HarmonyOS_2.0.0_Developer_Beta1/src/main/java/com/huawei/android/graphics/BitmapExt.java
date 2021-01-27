package com.huawei.android.graphics;

import android.graphics.Bitmap;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BitmapExt {
    public static void incReference(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.incReference();
        }
    }
}
