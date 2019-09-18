package com.android.server.backup.utils;

import android.util.SparseArray;
import java.util.HashSet;

public final class SparseArrayUtils {
    private SparseArrayUtils() {
    }

    public static <V> HashSet<V> union(SparseArray<HashSet<V>> sets) {
        HashSet<V> unionSet = new HashSet<>();
        int n = sets.size();
        for (int i = 0; i < n; i++) {
            HashSet<V> ithSet = sets.valueAt(i);
            if (ithSet != null) {
                unionSet.addAll(ithSet);
            }
        }
        return unionSet;
    }
}
