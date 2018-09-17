package com.huawei.android.icu.text;

import android.icu.text.AlphabeticIndex;
import android.icu.text.AlphabeticIndex.ImmutableIndex;
import java.util.ArrayList;
import java.util.Locale;

public class AlphabeticIndexEx {
    private ImmutableIndex mImmutableIndex = null;

    public AlphabeticIndexEx(Locale locale, int maxLabelCount, ArrayList<Locale> locales) {
        AlphabeticIndex alphabeticIndex = new AlphabeticIndex(locale).setMaxLabelCount(maxLabelCount);
        int size = locales.size();
        for (int i = 0; i < size; i++) {
            alphabeticIndex.addLabels(new Locale[]{(Locale) locales.get(i)});
        }
        this.mImmutableIndex = alphabeticIndex.buildImmutableIndex();
    }

    private ImmutableIndex getImmutableIndex() {
        return this.mImmutableIndex;
    }

    public int getBucketCount() {
        return getImmutableIndex().getBucketCount();
    }

    public int getBucketIndex(CharSequence name) {
        return getImmutableIndex().getBucketIndex(name);
    }

    public String getBucketLabel(int bucketIndex) {
        if (getImmutableIndex().getBucket(bucketIndex) != null) {
            return getImmutableIndex().getBucket(bucketIndex).getLabel();
        }
        return "";
    }
}
