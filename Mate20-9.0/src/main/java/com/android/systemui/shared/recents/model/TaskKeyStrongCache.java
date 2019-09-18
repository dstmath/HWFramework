package com.android.systemui.shared.recents.model;

import android.util.ArrayMap;
import com.android.systemui.shared.recents.model.Task;
import java.io.PrintWriter;

public class TaskKeyStrongCache<V> extends TaskKeyCache<V> {
    private static final String TAG = "TaskKeyCache";
    private final ArrayMap<Integer, V> mCache = new ArrayMap<>();

    /* access modifiers changed from: package-private */
    public final void copyEntries(TaskKeyStrongCache<V> other) {
        synchronized (other.mKeys) {
            for (int i = other.mKeys.size() - 1; i >= 0; i--) {
                Task.TaskKey key = (Task.TaskKey) other.mKeys.valueAt(i);
                if (key != null) {
                    put(key, other.getCacheEntry(key.id));
                }
            }
        }
    }

    public void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.print(TAG);
        writer.print(" numEntries=");
        writer.print(this.mKeys.size());
        writer.println();
        int keyCount = this.mKeys.size();
        for (int i = 0; i < keyCount; i++) {
            writer.print(innerPrefix);
            writer.println(this.mKeys.get(this.mKeys.keyAt(i)));
        }
    }

    /* access modifiers changed from: protected */
    public V getCacheEntry(int id) {
        V v;
        synchronized (this.mCache) {
            v = this.mCache.get(Integer.valueOf(id));
        }
        return v;
    }

    /* access modifiers changed from: protected */
    public void putCacheEntry(int id, V value) {
        synchronized (this.mCache) {
            this.mCache.put(Integer.valueOf(id), value);
        }
    }

    /* access modifiers changed from: protected */
    public void removeCacheEntry(int id) {
        synchronized (this.mCache) {
            this.mCache.remove(Integer.valueOf(id));
        }
    }

    /* access modifiers changed from: protected */
    public void evictAllCache() {
        synchronized (this.mCache) {
            this.mCache.clear();
        }
    }
}
