package com.huawei.android.app;

import android.app.AlarmManager;

public class AlarmManagerEx {
    public static int getWakeUpNum(AlarmManager obj, String pkg) {
        return 0;
    }

    public static int getWakeUpNum(AlarmManager obj, int uid, String pkg) {
        return obj.getWakeUpNum(uid, pkg);
    }
}
