package com.huawei.systemmanager.appcontrol.iaware;

import android.app.mtm.iaware.HwAppStartupSettingFilter;

public class HwAppStartupSettingFilterEx {
    private HwAppStartupSettingFilter mInnerFilter = new HwAppStartupSettingFilter();

    public HwAppStartupSettingFilter getHwAppStartupSettingFilter() {
        return this.mInnerFilter;
    }

    public int[] getPolicy() {
        if (this.mInnerFilter == null) {
            return null;
        }
        return this.mInnerFilter.getPolicy();
    }

    public HwAppStartupSettingFilterEx setPolicy(int[] policy) {
        if (this.mInnerFilter != null) {
            this.mInnerFilter.setPolicy(policy);
        }
        return this;
    }

    public HwAppStartupSettingFilterEx setShow(int[] show) {
        if (this.mInnerFilter != null) {
            this.mInnerFilter.setShow(show);
        }
        return this;
    }
}
