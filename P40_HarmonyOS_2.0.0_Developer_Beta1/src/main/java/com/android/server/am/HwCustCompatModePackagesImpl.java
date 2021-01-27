package com.android.server.am;

import android.os.SystemProperties;

public class HwCustCompatModePackagesImpl extends HwCustCompatModePackages {
    private static final boolean IS_ROG_SUPPORTED = SystemProperties.getBoolean("ro.config.ROG", false);
    private boolean mIsLowPowerDisplayMode;

    public HwCustCompatModePackagesImpl() {
        this.mIsLowPowerDisplayMode = SystemProperties.getInt("persist.sys.res.level", 0) > 0;
    }

    public boolean isLowPowerDisplayMode() {
        boolean z;
        if (!IS_ROG_SUPPORTED || !(z = this.mIsLowPowerDisplayMode)) {
            return HwCustCompatModePackagesImpl.super.isLowPowerDisplayMode();
        }
        return z;
    }
}
