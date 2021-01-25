package com.huawei.iimagekit.blur;

import android.graphics.Bitmap;

public class CpuBoxBlur {
    private static volatile CpuBoxBlur instance = null;

    public native int doBlur(Bitmap bitmap, Bitmap bitmap2, int i);

    private CpuBoxBlur() {
    }

    public static synchronized CpuBoxBlur getInstance() {
        CpuBoxBlur cpuBoxBlur;
        synchronized (CpuBoxBlur.class) {
            if (instance == null) {
                instance = new CpuBoxBlur();
            }
            cpuBoxBlur = instance;
        }
        return cpuBoxBlur;
    }
}
