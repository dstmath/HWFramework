package com.huawei.systemmanager.appcontrol.iaware;

import android.app.mtm.iaware.HwAppStartupSetting;

public class HwAppStartupSettingEx {
    private HwAppStartupSetting mInnerSetting;

    public HwAppStartupSettingEx(String packageName, int[] policy, int[] modifier, int[] show) {
        this.mInnerSetting = new HwAppStartupSetting(packageName, policy, modifier, show);
    }

    public HwAppStartupSettingEx(HwAppStartupSetting hwAppStartupSetting) {
        this.mInnerSetting = hwAppStartupSetting;
    }

    public String getPackageName() {
        HwAppStartupSetting hwAppStartupSetting = this.mInnerSetting;
        if (hwAppStartupSetting == null) {
            return "";
        }
        return hwAppStartupSetting.getPackageName();
    }

    public int getPolicy(int type) {
        HwAppStartupSetting hwAppStartupSetting = this.mInnerSetting;
        if (hwAppStartupSetting == null) {
            return 0;
        }
        return hwAppStartupSetting.getPolicy(type);
    }

    public int getModifier(int type) {
        HwAppStartupSetting hwAppStartupSetting = this.mInnerSetting;
        if (hwAppStartupSetting == null) {
            return 0;
        }
        return hwAppStartupSetting.getModifier(type);
    }

    public int getShow(int type) {
        HwAppStartupSetting hwAppStartupSetting = this.mInnerSetting;
        if (hwAppStartupSetting == null) {
            return 0;
        }
        return hwAppStartupSetting.getShow(type);
    }

    public HwAppStartupSetting getHwAppStartupSetting() {
        return this.mInnerSetting;
    }
}
