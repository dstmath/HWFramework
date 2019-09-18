package com.android.internal.textservice;

import android.util.SparseIntArray;
import com.android.internal.annotations.VisibleForTesting;
import java.util.function.IntUnaryOperator;

@VisibleForTesting
public final class LazyIntToIntMap {
    private final SparseIntArray mMap = new SparseIntArray();
    private final IntUnaryOperator mMappingFunction;

    public LazyIntToIntMap(IntUnaryOperator mappingFunction) {
        this.mMappingFunction = mappingFunction;
    }

    public void delete(int key) {
        this.mMap.delete(key);
    }

    public int get(int key) {
        int index = this.mMap.indexOfKey(key);
        if (index >= 0) {
            return this.mMap.valueAt(index);
        }
        int value = this.mMappingFunction.applyAsInt(key);
        this.mMap.append(key, value);
        return value;
    }
}
