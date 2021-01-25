package com.huawei.android.app;

import android.app.AlarmManager;
import com.huawei.annotation.HwSystemApi;

public class AlarmManagerEx {
    @HwSystemApi
    public static final int FLAG_ALLOW_WHILE_IDLE_UNRESTRICTED = 8;
    @HwSystemApi
    public static final int FLAG_STANDALONE = 1;

    public static int getWakeUpNum(AlarmManager obj, String pkg) {
        return 0;
    }

    public static int getWakeUpNum(AlarmManager obj, int uid, String pkg) {
        return 0;
    }
}
