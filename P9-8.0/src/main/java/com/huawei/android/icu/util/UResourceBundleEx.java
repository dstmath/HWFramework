package com.huawei.android.icu.util;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import com.huawei.android.icu.impl.ICUResourceBundleEx;

public class UResourceBundleEx {
    public static final int STRING = 0;
    private UResourceBundle mUResourceBundle;

    public UResourceBundleEx(UResourceBundle bundle) {
        this.mUResourceBundle = bundle;
    }

    public static ICUResourceBundleEx getBundleInstance(String baseName, ULocale locale) {
        return new ICUResourceBundleEx((ICUResourceBundle) UResourceBundle.getBundleInstance(baseName, locale));
    }

    public int getType() {
        return this.mUResourceBundle.getType();
    }

    public String getKey() {
        return this.mUResourceBundle.getKey();
    }

    public String getString() {
        return this.mUResourceBundle.getString();
    }
}
