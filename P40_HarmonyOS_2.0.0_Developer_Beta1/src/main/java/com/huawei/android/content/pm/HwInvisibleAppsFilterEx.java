package com.huawei.android.content.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.HwInvisibleAppsFilter;
import java.util.List;

public class HwInvisibleAppsFilterEx {
    private HwInvisibleAppsFilter invisibleAppsFilter;

    public HwInvisibleAppsFilterEx(Context context) {
        this.invisibleAppsFilter = new HwInvisibleAppsFilter(context);
    }

    public HwInvisibleAppsFilter getInvisibleAppsFilter() {
        return this.invisibleAppsFilter;
    }

    public void setInvisibleAppsFilter(HwInvisibleAppsFilter invisibleAppsFilter2) {
        this.invisibleAppsFilter = invisibleAppsFilter2;
    }

    public List<ApplicationInfo> filterHideApp(List<ApplicationInfo> allApps) {
        return this.invisibleAppsFilter.filterHideApp(allApps);
    }
}
