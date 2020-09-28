package com.huawei.android.util;

import android.graphics.Bitmap;

public class HwSecureWaterMark {
    public static boolean isWatermarkEnable() {
        return android.util.HwSecureWaterMark.isWatermarkEnable();
    }

    public static Bitmap addWatermark(Bitmap srcBitmap) {
        return android.util.HwSecureWaterMark.addWatermark(srcBitmap);
    }
}
