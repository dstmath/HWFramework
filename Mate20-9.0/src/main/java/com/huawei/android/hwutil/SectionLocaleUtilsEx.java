package com.huawei.android.hwutil;

import huawei.android.hwutil.SectionLocaleUtils;

public class SectionLocaleUtilsEx {
    private SectionLocaleUtils mSingleton = SectionLocaleUtils.getInstance();

    public String getLabel(String displayName) {
        if (this.mSingleton == null) {
            return null;
        }
        return this.mSingleton.getLabel(displayName);
    }
}
