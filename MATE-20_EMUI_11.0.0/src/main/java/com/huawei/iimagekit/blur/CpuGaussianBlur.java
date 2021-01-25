package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;

public class CpuGaussianBlur {
    private static volatile CpuGaussianBlur instance = null;

    public native int doBlur(Bitmap bitmap, Bitmap bitmap2, int i);

    private CpuGaussianBlur() {
    }

    public static synchronized CpuGaussianBlur getInstance() {
        CpuGaussianBlur cpuGaussianBlur;
        synchronized (CpuGaussianBlur.class) {
            if (instance == null) {
                instance = new CpuGaussianBlur();
            }
            cpuGaussianBlur = instance;
        }
        return cpuGaussianBlur;
    }
}
