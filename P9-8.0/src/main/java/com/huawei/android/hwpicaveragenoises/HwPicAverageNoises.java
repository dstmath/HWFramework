package com.huawei.android.hwpicaveragenoises;

import android.graphics.Bitmap;

public class HwPicAverageNoises {
    public static boolean isAverageNoiseSupported() {
        return huawei.android.hwpicaveragenoises.HwPicAverageNoises.isAverageNoiseSupported();
    }

    public static Bitmap jniNoiseBitmap(Bitmap bitmapOld) {
        return new huawei.android.hwpicaveragenoises.HwPicAverageNoises().jniNoiseBitmap(bitmapOld);
    }
}
