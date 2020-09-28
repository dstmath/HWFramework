package com.huawei.systemmanager.notificationmanager.widget;

import huawei.android.hwutil.SectionLocaleUtils;

public class SectionLocaleUtilsEx {
    public int getBucketIndex(String str) {
        return SectionLocaleUtils.getInstance().getBucketIndex(str);
    }
}
