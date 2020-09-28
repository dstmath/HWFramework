package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;

public class CpuFastBlur {
    private static volatile CpuFastBlur instance = null;

    public native int doBlur(Bitmap bitmap, Bitmap bitmap2, int i);

    private CpuFastBlur() {
    }

    public static synchronized CpuFastBlur getInstance() {
        CpuFastBlur cpuFastBlur;
        synchronized (CpuFastBlur.class) {
            if (instance == null) {
                instance = new CpuFastBlur();
            }
            cpuFastBlur = instance;
        }
        return cpuFastBlur;
    }
}
