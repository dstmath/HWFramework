package com.android.server.am;

import android.os.SystemProperties;

public class HwCustCompatModePackagesImpl extends HwCustCompatModePackages {
    private static final boolean isRogSupported = SystemProperties.getBoolean("ro.config.ROG", false);
    private boolean mLowPowerDisplayMode;

    public HwCustCompatModePackagesImpl() {
        this.mLowPowerDisplayMode = SystemProperties.getInt("persist.sys.res.level", 0) > 0;
    }

    public boolean isLowPowerDisplayMode() {
        if (!isRogSupported || !this.mLowPowerDisplayMode) {
            return HwCustCompatModePackagesImpl.super.isLowPowerDisplayMode();
        }
        return this.mLowPowerDisplayMode;
    }
}
