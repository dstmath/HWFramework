package com.huawei.android.os;

import android.os.Process;
import com.huawei.annotation.HwSystemApi;

public class ProcessEx {
    @HwSystemApi
    public static final int INCIDENTD_UID = 1067;
    @HwSystemApi
    public static final int INVALID_UID = -1;
    public static final int MEDIA_RW_GID = 1023;

    @HwSystemApi
    public static void setCanSelfBackground(boolean isBackgroundOk) {
        Process.setCanSelfBackground(isBackgroundOk);
    }
}
