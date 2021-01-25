package com.huawei.android.hardware.fmradio.common;

import android.os.DeadObjectException;
import android.os.DeadSystemException;
import android.os.RemoteException;
import com.huawei.android.os.SystemPropertiesEx;

public class FmUtils {
    private static final String PLATFORM = SystemPropertiesEx.get("ro.board.platform", "kirin");

    private FmUtils() {
    }

    public static boolean isHisiPlatform() {
        return PLATFORM.startsWith("hi") || PLATFORM.startsWith("kirin");
    }

    public static boolean isQcomPlatform() {
        return PLATFORM.startsWith("msm");
    }

    public static boolean isMtkPlatform() {
        return PLATFORM.startsWith("mt");
    }

    public static RuntimeException rethrowFromSystemServer(RemoteException e) {
        if (e instanceof DeadObjectException) {
            return new RuntimeException(new DeadSystemException());
        }
        return new RuntimeException(e);
    }
}
