package com.android.systemui.shared.recents.model;

import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.shared.recents.model.Task;

public abstract class TaskKeyCache<V> {
    protected static final String TAG = "TaskKeyCache";
    protected final SparseArray<Task.TaskKey> mKeys = new SparseArray<>();

    /* access modifiers changed from: protected */
    public abstract void evictAllCache();

    /* access modifiers changed from: protected */
    public abstract V getCacheEntry(int i);

    /* access modifiers changed from: protected */
    public abstract void putCacheEntry(int i, V v);

    /* access modifiers changed from: protected */
    public abstract void removeCacheEntry(int i);

    /* access modifiers changed from: package-private */
    public final V get(Task.TaskKey key) {
        return getCacheEntry(key.id);
    }

    /* access modifiers changed from: package-private */
    public final V getAndInvalidateIfModified(Task.TaskKey key) {
        Task.TaskKey lastKey;
        synchronized (this.mKeys) {
            lastKey = this.mKeys.get(key.id);
        }
        if (lastKey == null || (lastKey.windowingMode == key.windowingMode && lastKey.lastActiveTime == key.lastActiveTime)) {
            return getCacheEntry(key.id);
        }
        remove(key);
        return null;
    }

    /* access modifiers changed from: package-private */
    public final void put(Task.TaskKey key, V value) {
        if (key == null) {
            Log.e(TAG, "Unexpected key == null");
            return;
        }
        synchronized (this.mKeys) {
            this.mKeys.put(key.id, key);
        }
        putCacheEntry(key.id, value);
    }

    /* access modifiers changed from: package-private */
    public final void remove(Task.TaskKey key) {
        removeCacheEntry(key.id);
        synchronized (this.mKeys) {
            this.mKeys.remove(key.id);
        }
    }

    /* access modifiers changed from: package-private */
    public final void evictAll() {
        evictAllCache();
        synchronized (this.mKeys) {
            this.mKeys.clear();
        }
    }
}
