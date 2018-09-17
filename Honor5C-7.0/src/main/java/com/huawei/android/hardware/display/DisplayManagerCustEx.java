package com.huawei.android.hardware.display;

import com.huawei.android.util.NoExtAPIException;

public final class DisplayManagerCustEx {
    public static final int DISPLAY_LOW_POWER_LEVEL_FHD = 0;
    public static final int DISPLAY_LOW_POWER_LEVEL_HD = 1;
    public static final int MAX_LOW_POWER_DISPLAY_LEVEL = 1;

    public int getLowPowerDisplayLevel() {
        throw new NoExtAPIException("method not supported.");
    }

    public void setLowPowerDisplayLevel(int level) {
        throw new NoExtAPIException("method not supported.");
    }
}
