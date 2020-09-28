package com.huawei.android.os;

import android.os.IBinder;
import huawei.android.os.HwMountManager;

public class HwMountManagerEx {
    public static String mountCifs(String source, String option, IBinder binder) {
        return HwMountManager.getInstance().mountCifs(source, option, binder);
    }

    public static void unmountCifs(String mountPoint) {
        HwMountManager.getInstance().unmountCifs(mountPoint);
    }

    public static int isSupportedCifs() {
        return HwMountManager.getInstance().isSupportedCifs();
    }
}
