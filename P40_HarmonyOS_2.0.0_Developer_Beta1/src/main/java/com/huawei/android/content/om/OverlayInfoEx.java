package com.huawei.android.content.om;

import android.content.om.OverlayInfo;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class OverlayInfoEx {
    private OverlayInfo mOverlayInfo;

    public OverlayInfoEx(Object info) {
        this.mOverlayInfo = (OverlayInfo) info;
    }

    @HwSystemApi
    public String getPackageName() {
        return this.mOverlayInfo.packageName;
    }
}
