package com.android.server.rog;

import android.rog.AppRogInfo;

public interface IRogPolicy {
    void calRogAppScale();

    AppRogInfo getAppOwnInfo(HwRogInfosCollector hwRogInfosCollector, String str);

    float getAppRogScale();
}
