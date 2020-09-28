package com.huawei.android.icu.util;

import android.icu.util.UResourceBundleIterator;

public class UResourceBundleIteratorEx {
    private UResourceBundleIterator mUResourceBundleIterator;

    public UResourceBundleIteratorEx(UResourceBundleIterator iter) {
        this.mUResourceBundleIterator = iter;
    }

    public boolean hasNext() {
        UResourceBundleIterator uResourceBundleIterator = this.mUResourceBundleIterator;
        if (uResourceBundleIterator == null) {
            return false;
        }
        return uResourceBundleIterator.hasNext();
    }

    public UResourceBundleEx next() {
        UResourceBundleIterator uResourceBundleIterator = this.mUResourceBundleIterator;
        if (uResourceBundleIterator != null) {
            return new UResourceBundleEx(uResourceBundleIterator.next());
        }
        return null;
    }
}
