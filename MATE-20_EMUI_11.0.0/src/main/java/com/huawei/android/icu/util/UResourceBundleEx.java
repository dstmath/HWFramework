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
        ICUResourceBundle fBundle = null;
        if (UResourceBundle.getBundleInstance(baseName, locale) instanceof ICUResourceBundle) {
            fBundle = UResourceBundle.getBundleInstance(baseName, locale);
        }
        return new ICUResourceBundleEx(fBundle);
    }

    public int getType() {
        UResourceBundle uResourceBundle = this.mUResourceBundle;
        if (uResourceBundle == null) {
            return 0;
        }
        return uResourceBundle.getType();
    }

    public String getKey() {
        UResourceBundle uResourceBundle = this.mUResourceBundle;
        if (uResourceBundle != null) {
            return uResourceBundle.getKey();
        }
        return null;
    }

    public String getString() {
        UResourceBundle uResourceBundle = this.mUResourceBundle;
        if (uResourceBundle != null) {
            return uResourceBundle.getString();
        }
        return null;
    }
}
