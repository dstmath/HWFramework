package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;

public class CPUBoxBlur {
    private static volatile CPUBoxBlur instance;

    public native int doBlur(Bitmap bitmap, Bitmap bitmap2, int i);

    static {
        instance = null;
        if (instance == null) {
            instance = new CPUBoxBlur();
        }
    }

    private CPUBoxBlur() {
    }

    public static CPUBoxBlur getInstance() {
        return instance;
    }
}
