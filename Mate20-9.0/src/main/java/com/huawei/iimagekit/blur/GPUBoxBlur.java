package com.huawei.iimagekit.blur;

import android.content.Context;

public class GPUBoxBlur extends GPUBlurBase {
    private static volatile GPUBoxBlur instance = null;

    public native void doBlur(int i, int i2, int i3);

    public static GPUBoxBlur getInstance(Context context) {
        if (instance == null) {
            instance = new GPUBoxBlur(context);
        }
        return instance;
    }

    GPUBoxBlur(Context context) {
        initContext(context);
    }
}
