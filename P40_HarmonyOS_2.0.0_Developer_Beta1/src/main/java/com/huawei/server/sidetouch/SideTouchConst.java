package com.huawei.server.sidetouch;

import com.huawei.android.os.SystemPropertiesEx;

public class SideTouchConst {
    public static final boolean DEBUG;

    static {
        boolean z = true;
        if (!(HwSideStatusManager.AUDIO_STATE_MUSIC.equals(SystemPropertiesEx.get("ro.debuggable", HwSideStatusManager.AUDIO_STATE_NONE)) || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 3 || SystemPropertiesEx.getInt("ro.logsystem.usertype", 1) == 5)) {
            z = false;
        }
        DEBUG = z;
    }
}
