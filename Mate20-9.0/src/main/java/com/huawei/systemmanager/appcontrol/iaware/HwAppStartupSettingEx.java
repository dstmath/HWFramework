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
        if (this.mInnerSetting == null) {
            return "";
        }
        return this.mInnerSetting.getPackageName();
    }

    public int getPolicy(int type) {
        if (this.mInnerSetting == null) {
            return 0;
        }
        return this.mInnerSetting.getPolicy(type);
    }

    public int getModifier(int type) {
        if (this.mInnerSetting == null) {
            return 0;
        }
        return this.mInnerSetting.getModifier(type);
    }

    public int getShow(int type) {
        if (this.mInnerSetting == null) {
            return 0;
        }
        return this.mInnerSetting.getShow(type);
    }

    public HwAppStartupSetting getHwAppStartupSetting() {
        return this.mInnerSetting;
    }
}
