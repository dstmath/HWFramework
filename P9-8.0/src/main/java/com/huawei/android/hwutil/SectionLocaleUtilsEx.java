package com.huawei.android.hwutil;

import huawei.android.hwutil.SectionLocaleUtils;

public class SectionLocaleUtilsEx {
    private SectionLocaleUtils mSingleton = SectionLocaleUtils.getInstance();

    public String getLabel(String displayName) {
        return this.mSingleton == null ? null : this.mSingleton.getLabel(displayName);
    }
}
