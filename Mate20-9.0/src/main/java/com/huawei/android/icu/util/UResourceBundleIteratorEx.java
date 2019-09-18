package com.huawei.android.icu.util;

import android.icu.util.UResourceBundleIterator;

public class UResourceBundleIteratorEx {
    private UResourceBundleIterator mUResourceBundleIterator;

    public UResourceBundleIteratorEx(UResourceBundleIterator iter) {
        this.mUResourceBundleIterator = iter;
    }

    public boolean hasNext() {
        return this.mUResourceBundleIterator.hasNext();
    }

    public UResourceBundleEx next() {
        return new UResourceBundleEx(this.mUResourceBundleIterator.next());
    }
}
