package com.android.server.display;

import android.os.SystemProperties;
import android.util.Slog;
import android.view.Surface;

public class HwCustDisplayManagerServiceImpl extends HwCustDisplayManagerService {
    public static final int DISPLAY_LOW_POWER_LEVEL_FHD = 0;
    public static final int DISPLAY_LOW_POWER_LEVEL_HD = 1;
    public static final int MAX_LOW_POWER_DISPLAY_LEVEL = 1;
    private static final String TAG = "HwCustDisplayManagerServiceImpl";
    private static final boolean isRogSupported = SystemProperties.getBoolean("ro.config.ROG", false);
    private int mLowPowerDisplayLevel = SystemProperties.getInt("persist.sys.res.level", 0);

    public void setLowPowerDisplayLevel(int level) {
        if (isRogSupported) {
            Integer lowResLevel = Integer.valueOf(level);
            Slog.i(TAG, "setLowPowerDisplayLevel level = " + level);
            if (level != this.mLowPowerDisplayLevel) {
                if (level < 0 || level > 1) {
                    Slog.e(TAG, "set lowpower display level failed, invalid value:" + level);
                    return;
                }
                SystemProperties.set("persist.sys.res.level", lowResLevel.toString());
                if (level == 1) {
                    SystemProperties.set("persist.sys.res.icon_size", "112");
                } else {
                    SystemProperties.set("persist.sys.res.icon_size", "-1");
                }
                this.mLowPowerDisplayLevel = level;
                Surface.setLowPowerDisplayLevel(this.mLowPowerDisplayLevel);
            }
        }
    }

    public int getLowPowerDisplayLevel() {
        Slog.i(TAG, "getLowPowerDisplayLevel level = " + this.mLowPowerDisplayLevel);
        return this.mLowPowerDisplayLevel;
    }
}
