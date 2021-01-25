package com.huawei.systemmanager.appcontrol.iaware;

import android.app.mtm.iaware.HwAppStartupSettingFilter;

public class HwAppStartupSettingFilterEx {
    private HwAppStartupSettingFilter mInnerFilter = new HwAppStartupSettingFilter();

    public HwAppStartupSettingFilter getHwAppStartupSettingFilter() {
        return this.mInnerFilter;
    }

    public int[] getPolicy() {
        HwAppStartupSettingFilter hwAppStartupSettingFilter = this.mInnerFilter;
        if (hwAppStartupSettingFilter == null) {
            return null;
        }
        return hwAppStartupSettingFilter.getPolicy();
    }

    public HwAppStartupSettingFilterEx setPolicy(int[] policy) {
        HwAppStartupSettingFilter hwAppStartupSettingFilter = this.mInnerFilter;
        if (hwAppStartupSettingFilter != null) {
            hwAppStartupSettingFilter.setPolicy(policy);
        }
        return this;
    }

    public HwAppStartupSettingFilterEx setShow(int[] show) {
        HwAppStartupSettingFilter hwAppStartupSettingFilter = this.mInnerFilter;
        if (hwAppStartupSettingFilter != null) {
            hwAppStartupSettingFilter.setShow(show);
        }
        return this;
    }
}
