package com.android.internal.app;

import android.util.ArrayMap;
import android.util.SparseArray;

public class ProcessMap<E> {
    final ArrayMap<String, SparseArray<E>> mMap = new ArrayMap();

    public E get(String name, int uid) {
        SparseArray<E> uids = (SparseArray) this.mMap.get(name);
        if (uids == null) {
            return null;
        }
        return uids.get(uid);
    }

    public E put(String name, int uid, E value) {
        SparseArray<E> uids = (SparseArray) this.mMap.get(name);
        if (uids == null) {
            uids = new SparseArray(2);
            this.mMap.put(name, uids);
        }
        uids.put(uid, value);
        return value;
    }

    public E remove(String name, int uid) {
        SparseArray<E> uids = (SparseArray) this.mMap.get(name);
        if (uids == null) {
            return null;
        }
        E old = uids.removeReturnOld(uid);
        if (uids.size() == 0) {
            this.mMap.remove(name);
        }
        return old;
    }

    public ArrayMap<String, SparseArray<E>> getMap() {
        return this.mMap;
    }

    public int size() {
        return this.mMap.size();
    }
}
