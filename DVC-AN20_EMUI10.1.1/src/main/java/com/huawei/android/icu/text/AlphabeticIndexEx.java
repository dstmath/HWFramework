package com.huawei.android.icu.text;

import android.icu.text.AlphabeticIndex;
import java.util.ArrayList;
import java.util.Locale;

public class AlphabeticIndexEx {
    private AlphabeticIndex.ImmutableIndex mImmutableIndex = null;

    public AlphabeticIndexEx(Locale locale, int maxLabelCount, ArrayList<Locale> locales) {
        if (locales != null) {
            AlphabeticIndex alphabeticIndex = new AlphabeticIndex(locale).setMaxLabelCount(maxLabelCount);
            int size = locales.size();
            for (int i = 0; i < size; i++) {
                alphabeticIndex.addLabels(locales.get(i));
            }
            this.mImmutableIndex = alphabeticIndex.buildImmutableIndex();
        }
    }

    private AlphabeticIndex.ImmutableIndex getImmutableIndex() {
        return this.mImmutableIndex;
    }

    public int getBucketCount() {
        if (getImmutableIndex() == null) {
            return Integer.MIN_VALUE;
        }
        return getImmutableIndex().getBucketCount();
    }

    public int getBucketIndex(CharSequence name) {
        if (getImmutableIndex() == null) {
            return Integer.MIN_VALUE;
        }
        return getImmutableIndex().getBucketIndex(name);
    }

    public String getBucketLabel(int bucketIndex) {
        if (getImmutableIndex().getBucket(bucketIndex) != null) {
            return getImmutableIndex().getBucket(bucketIndex).getLabel();
        }
        return "";
    }
}
