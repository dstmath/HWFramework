package com.huawei.iimagekit.blur;

import android.content.Context;

public class GPUGaussianBlur extends GPUBlurBase {
    private static volatile GPUGaussianBlur instance = null;

    public native void doBlur(int i, int i2, int i3);

    public static GPUGaussianBlur getInstance(Context context) {
        if (instance == null) {
            instance = new GPUGaussianBlur(context);
        }
        return instance;
    }

    GPUGaussianBlur(Context context) {
        initContext(context);
    }
}
