package com.huawei.android.os;

import huawei.android.os.HwBootanimManager;

public class BootanimEx {
    public static boolean isBootOrShutdownSoundCapable() {
        return HwBootanimManager.getInstance().isBootOrShutdownSoundCapable();
    }

    public static void switchBootOrShutSound(String openOrClose) {
        HwBootanimManager.getInstance().switchBootOrShutSound(openOrClose);
    }

    public static int getBootAnimSoundSwitch() {
        return HwBootanimManager.getInstance().getBootAnimSoundSwitch();
    }
}
