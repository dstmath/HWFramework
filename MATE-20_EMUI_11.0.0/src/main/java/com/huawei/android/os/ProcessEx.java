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
    public static final int PROC_OUT_STRING = 4096;
    @HwSystemApi
    public static final int PROC_SPACE_TERM = 32;
    @HwSystemApi
    public static final int SYSTEM_UID = 1000;

    @HwSystemApi
    public static void setCanSelfBackground(boolean isBackgroundOk) {
        Process.setCanSelfBackground(isBackgroundOk);
    }

    @HwSystemApi
    public static final int[] getPidsForCommands(String[] cmds) {
        return Process.getPidsForCommands(cmds);
    }

    @HwSystemApi
    public static boolean readProcFile(String file, int[] format, String[] outStrings, long[] outLongs, float[] outFloats) {
        return Process.readProcFile(file, format, outStrings, outLongs, outFloats);
    }
}
