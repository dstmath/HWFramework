package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;

public class CPUGaussianBlur {
    private static volatile CPUGaussianBlur instance;

    public native int doBlur(Bitmap bitmap, Bitmap bitmap2, int i);

    static {
        instance = null;
        if (instance == null) {
            instance = new CPUGaussianBlur();
        }
    }

    private CPUGaussianBlur() {
    }

    public static CPUGaussianBlur getInstance() {
        return instance;
    }
}
