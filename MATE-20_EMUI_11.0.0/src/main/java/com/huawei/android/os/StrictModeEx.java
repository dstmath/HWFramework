package com.huawei.android.os;

import android.content.pm.ApplicationInfo;
import android.os.StrictMode;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class StrictModeEx {
    public static void initThreadDefaults(ApplicationInfo ai) {
        StrictMode.initThreadDefaults(ai);
    }
}
