package com.huawei.android.icu.impl;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.ULocale;
import com.huawei.android.icu.util.UResourceBundleIteratorEx;

public class ICUResourceBundleEx {
    public static final String ICU_LANG_BASE_NAME = "android/icu/impl/data/icudt63b/lang";
    private ICUResourceBundle mICUResourceBundle;

    public ICUResourceBundleEx(ICUResourceBundle bundle) {
        this.mICUResourceBundle = bundle;
    }

    public ULocale getULocale() {
        ICUResourceBundle iCUResourceBundle = this.mICUResourceBundle;
        if (iCUResourceBundle != null) {
            return iCUResourceBundle.getULocale();
        }
        return null;
    }

    public ICUResourceBundleEx getWithFallback(String path) {
        ICUResourceBundle iCUResourceBundle = this.mICUResourceBundle;
        if (iCUResourceBundle != null) {
            return new ICUResourceBundleEx(iCUResourceBundle.getWithFallback(path));
        }
        return null;
    }

    public UResourceBundleIteratorEx getIterator() {
        ICUResourceBundle iCUResourceBundle = this.mICUResourceBundle;
        if (iCUResourceBundle != null) {
            return new UResourceBundleIteratorEx(iCUResourceBundle.getIterator());
        }
        return null;
    }
}
