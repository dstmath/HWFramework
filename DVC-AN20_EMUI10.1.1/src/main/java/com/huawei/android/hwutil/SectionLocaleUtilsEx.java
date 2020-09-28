package com.huawei.android.hwutil;

import huawei.android.hwutil.SectionLocaleUtils;

public class SectionLocaleUtilsEx {
    private SectionLocaleUtils mSingleton = SectionLocaleUtils.getInstance();

    public String getLabel(String displayName) {
        SectionLocaleUtils sectionLocaleUtils = this.mSingleton;
        if (sectionLocaleUtils == null) {
            return null;
        }
        return sectionLocaleUtils.getLabel(displayName);
    }
}
