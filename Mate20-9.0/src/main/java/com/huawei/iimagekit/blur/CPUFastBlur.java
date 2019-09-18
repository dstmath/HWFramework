package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;

public class CPUFastBlur {
    private static volatile CPUFastBlur instance;

    public native int doBlur(Bitmap bitmap, Bitmap bitmap2, int i);

    static {
        instance = null;
        if (instance == null) {
            instance = new CPUFastBlur();
        }
    }

    private CPUFastBlur() {
    }

    public static CPUFastBlur getInstance() {
        return instance;
    }
}
