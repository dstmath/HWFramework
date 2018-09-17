package com.huawei.android.icu.impl;

import android.icu.impl.ICUResourceBundle;
import android.icu.util.ULocale;
import com.huawei.android.icu.util.UResourceBundleIteratorEx;

public class ICUResourceBundleEx {
    public static final String ICU_LANG_BASE_NAME = "android/icu/impl/data/icudt58b/lang";
    private ICUResourceBundle mICUResourceBundle;

    public ICUResourceBundleEx(ICUResourceBundle fBundle) {
        this.mICUResourceBundle = fBundle;
    }

    public ULocale getULocale() {
        if (this.mICUResourceBundle == null) {
            return null;
        }
        return this.mICUResourceBundle.getULocale();
    }

    public ICUResourceBundleEx getWithFallback(String path) {
        return new ICUResourceBundleEx(this.mICUResourceBundle.getWithFallback(path));
    }

    public UResourceBundleIteratorEx getIterator() {
        return new UResourceBundleIteratorEx(this.mICUResourceBundle.getIterator());
    }
}
