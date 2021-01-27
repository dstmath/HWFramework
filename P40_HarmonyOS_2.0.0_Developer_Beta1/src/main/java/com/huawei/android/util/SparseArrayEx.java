package com.huawei.android.util;

import android.util.SparseArray;

public class SparseArrayEx<E> {
    private SparseArray<E> mArray;

    public SparseArrayEx(SparseArray<E> array) {
        this.mArray = array;
    }

    public E removeReturnOld(int key) {
        return (E) this.mArray.removeReturnOld(key);
    }
}
