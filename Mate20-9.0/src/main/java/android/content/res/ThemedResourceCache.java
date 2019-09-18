package android.content.res;

import android.content.res.Resources;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import java.lang.ref.WeakReference;

abstract class ThemedResourceCache<T> {
    private LongSparseArray<WeakReference<T>> mNullThemedEntries;
    private ArrayMap<Resources.ThemeKey, LongSparseArray<WeakReference<T>>> mThemedEntries;
    private LongSparseArray<WeakReference<T>> mUnthemedEntries;

    /* access modifiers changed from: protected */
    public abstract boolean shouldInvalidateEntry(T t, int i);

    ThemedResourceCache() {
    }

    public void put(long key, Resources.Theme theme, T entry) {
        put(key, theme, entry, true);
    }

    public void put(long key, Resources.Theme theme, T entry, boolean usesTheme) {
        LongSparseArray<WeakReference<T>> entries;
        if (entry != null) {
            synchronized (this) {
                if (!usesTheme) {
                    try {
                        entries = getUnthemedLocked(true);
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    entries = getThemedLocked(theme, true);
                }
                if (entries != null) {
                    entries.put(key, new WeakReference(entry));
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002c, code lost:
        return null;
     */
    public T get(long key, Resources.Theme theme) {
        synchronized (this) {
            LongSparseArray<WeakReference<T>> themedEntries = getThemedLocked(theme, false);
            if (themedEntries != null) {
                WeakReference<T> themedEntry = themedEntries.get(key);
                if (themedEntry != null) {
                    T t = themedEntry.get();
                    return t;
                }
            }
            LongSparseArray<WeakReference<T>> unthemedEntries = getUnthemedLocked(false);
            if (unthemedEntries != null) {
                WeakReference<T> unthemedEntry = unthemedEntries.get(key);
                if (unthemedEntry != null) {
                    T t2 = unthemedEntry.get();
                    return t2;
                }
            }
        }
    }

    public void onConfigurationChange(int configChanges) {
        prune(configChanges);
    }

    private LongSparseArray<WeakReference<T>> getThemedLocked(Resources.Theme t, boolean create) {
        if (t == null) {
            if (this.mNullThemedEntries == null && create) {
                this.mNullThemedEntries = new LongSparseArray<>(1);
            }
            return this.mNullThemedEntries;
        }
        if (this.mThemedEntries == null) {
            if (!create) {
                return null;
            }
            this.mThemedEntries = new ArrayMap<>(1);
        }
        Resources.ThemeKey key = t.getKey();
        LongSparseArray<WeakReference<T>> cache = this.mThemedEntries.get(key);
        if (cache == null && create) {
            cache = new LongSparseArray<>(1);
            this.mThemedEntries.put(key.clone(), cache);
        }
        return cache;
    }

    private LongSparseArray<WeakReference<T>> getUnthemedLocked(boolean create) {
        if (this.mUnthemedEntries == null && create) {
            this.mUnthemedEntries = new LongSparseArray<>(1);
        }
        return this.mUnthemedEntries;
    }

    private boolean prune(int configChanges) {
        boolean z;
        synchronized (this) {
            z = true;
            if (this.mThemedEntries != null) {
                for (int i = this.mThemedEntries.size() - 1; i >= 0; i--) {
                    if (pruneEntriesLocked(this.mThemedEntries.valueAt(i), configChanges)) {
                        this.mThemedEntries.removeAt(i);
                    }
                }
            }
            pruneEntriesLocked(this.mNullThemedEntries, configChanges);
            pruneEntriesLocked(this.mUnthemedEntries, configChanges);
            if (this.mThemedEntries != null || this.mNullThemedEntries != null || this.mUnthemedEntries != null) {
                z = false;
            }
        }
        return z;
    }

    private boolean pruneEntriesLocked(LongSparseArray<WeakReference<T>> entries, int configChanges) {
        boolean z = true;
        if (entries == null) {
            return true;
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            WeakReference<T> ref = entries.valueAt(i);
            if (ref == null || pruneEntryLocked(ref.get(), configChanges)) {
                entries.removeAt(i);
            }
        }
        if (entries.size() != 0) {
            z = false;
        }
        return z;
    }

    private boolean pruneEntryLocked(T entry, int configChanges) {
        return entry == null || (configChanges != 0 && shouldInvalidateEntry(entry, configChanges));
    }
}
