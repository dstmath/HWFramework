package com.huawei.android.util;

import android.util.Jlog;
import android.util.Log;

public class JlogEx {
    private static final int EXIST = 1;
    private static final String JLOG_CLASS = "android.util.Jlog";
    private static final String TAG = "JlogEx";
    private static final int UNEXIST = 0;
    private static final int UNKNOWN = -1;
    private static int sJlogExist = -1;

    public static int jLogEvent(int tag, String msg) {
        if (checkJlogExist()) {
            return Jlog.d(tag, msg);
        }
        return -1;
    }

    public static int jLogEvent(int tag, String arg1, String msg) {
        if (checkJlogExist()) {
            return Jlog.d(tag, arg1, msg);
        }
        return -1;
    }

    public static int jLogEvent(int tag, int arg2, String msg) {
        if (checkJlogExist()) {
            return Jlog.d(tag, arg2, msg);
        }
        return -1;
    }

    private static boolean checkJlogExist() {
        int i = sJlogExist;
        int i2 = 1;
        if (i == -1) {
            try {
                if (Class.forName(JLOG_CLASS) == null) {
                    i2 = 0;
                }
                sJlogExist = i2;
            } catch (ClassNotFoundException e) {
                sJlogExist = 0;
            }
            return checkJlogExist();
        } else if (i == 0) {
            return false;
        } else {
            if (i == 1) {
                return true;
            }
            Log.e(TAG, "get unknown sJlogExist status.");
            return false;
        }
    }
}
