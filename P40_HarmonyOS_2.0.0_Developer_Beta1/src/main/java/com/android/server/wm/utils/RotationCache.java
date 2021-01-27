package com.android.server.wm.utils;

import android.util.SparseArray;

public class RotationCache<T, R> {
    private final SparseArray<R> mCache = new SparseArray<>(4);
    private T mCachedFor;
    private final RotationDependentComputation<T, R> mComputation;

    @FunctionalInterface
    public interface RotationDependentComputation<T, R> {
        R compute(T t, int i);
    }

    public RotationCache(RotationDependentComputation<T, R> computation) {
        this.mComputation = computation;
    }

    public void clearCacheTable() {
        this.mCache.clear();
    }

    public R getOrCompute(T t, int rotation) {
        if (t != this.mCachedFor) {
            this.mCache.clear();
            this.mCachedFor = t;
        }
        int idx = this.mCache.indexOfKey(rotation);
        if (idx >= 0) {
            return this.mCache.valueAt(idx);
        }
        R result = this.mComputation.compute(t, rotation);
        this.mCache.put(rotation, result);
        return result;
    }
}
