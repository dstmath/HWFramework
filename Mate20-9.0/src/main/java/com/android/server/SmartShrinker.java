package com.android.server;

import android.os.SystemProperties;
import android.util.Log;
import com.android.server.am.ProcessList;

public final class SmartShrinker {
    private static final boolean DEBUG = false;
    private static volatile boolean ENABLE = SystemProperties.getBoolean("ro.config.hw_smart_shrink", false);
    public static final int RECLAIM_ALL_MODE = 2;
    public static final int RECLAIM_ANON_MODE = 1;
    public static final int RECLAIM_INACTIVE_MODE = 4;
    public static final int RECLAIM_SOFT_MODE = 3;
    private static final String TAG = "RMS.SmartShrinker";

    public static final void init_once(boolean enable) {
        ENABLE = enable;
    }

    public static final void reclaim(int pid, int mode) {
        if (ENABLE) {
            ProcessList.callProcReclaim(pid, mode);
            Log.w(TAG, "SmartShrinker is runing in pid =" + pid + " reclaim mode = " + mode);
        }
    }
}
