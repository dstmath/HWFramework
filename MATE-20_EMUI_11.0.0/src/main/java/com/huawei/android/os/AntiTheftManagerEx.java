package com.huawei.android.os;

import android.util.Log;
import huawei.android.os.HwAntiTheftManager;

public class AntiTheftManagerEx {
    public static byte[] read() {
        return HwAntiTheftManager.getInstance().readAntiTheftData();
    }

    public static int wipe() {
        return HwAntiTheftManager.getInstance().wipeAntiTheftData();
    }

    public static int write(byte[] writeToNative) {
        return HwAntiTheftManager.getInstance().writeAntiTheftData(writeToNative);
    }

    public static int getDataBlockSize() {
        return HwAntiTheftManager.getInstance().getAntiTheftDataBlockSize();
    }

    public static int setEnable(boolean enable) {
        return HwAntiTheftManager.getInstance().setAntiTheftEnabled(enable);
    }

    public static boolean getEnable() {
        return HwAntiTheftManager.getInstance().getAntiTheftEnabled();
    }

    @Deprecated
    public static boolean checkRootState() {
        Log.e("AntiTheftManagerEx", "checkRootState @Removed");
        return false;
    }

    public static boolean isAntiTheftSupported() {
        return HwAntiTheftManager.getInstance().isAntiTheftSupported();
    }
}
