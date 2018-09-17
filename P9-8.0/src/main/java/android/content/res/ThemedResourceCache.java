package android.content.res;

import android.content.res.Resources.Theme;
import android.util.ArrayMap;
import android.util.LongSparseArray;
import java.lang.ref.WeakReference;

abstract class ThemedResourceCache<T> {
    private LongSparseArray<WeakReference<T>> mNullThemedEntries;
    private ArrayMap<ThemeKey, LongSparseArray<WeakReference<T>>> mThemedEntries;
    private LongSparseArray<WeakReference<T>> mUnthemedEntries;

    protected abstract boolean shouldInvalidateEntry(T t, int i);

    ThemedResourceCache() {
    }

    public void put(long key, Theme theme, T entry) {
        put(key, theme, entry, true);
    }

    public void put(long key, Theme theme, T entry, boolean usesTheme) {
        if (entry != null) {
            synchronized (this) {
                LongSparseArray<WeakReference<T>> entries;
                if (usesTheme) {
                    entries = getThemedLocked(theme, true);
                } else {
                    entries = getUnthemedLocked(true);
                }
                if (entries != null) {
                    entries.put(key, new WeakReference(entry));
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x002d, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public T get(long key, Theme theme) {
        synchronized (this) {
            T t;
            LongSparseArray<WeakReference<T>> themedEntries = getThemedLocked(theme, false);
            if (themedEntries != null) {
                WeakReference<T> themedEntry = (WeakReference) themedEntries.get(key);
                if (themedEntry != null) {
                    t = themedEntry.get();
                    return t;
                }
            }
            LongSparseArray<WeakReference<T>> unthemedEntries = getUnthemedLocked(false);
            if (unthemedEntries != null) {
                WeakReference<T> unthemedEntry = (WeakReference) unthemedEntries.get(key);
                if (unthemedEntry != null) {
                    t = unthemedEntry.get();
                    return t;
                }
            }
        }
    }

    public void onConfigurationChange(int configChanges) {
        prune(configChanges);
    }

    private LongSparseArray<WeakReference<T>> getThemedLocked(Theme t, boolean create) {
        if (t == null) {
            if (this.mNullThemedEntries == null && create) {
                this.mNullThemedEntries = new LongSparseArray(1);
            }
            return this.mNullThemedEntries;
        }
        if (this.mThemedEntries == null) {
            if (!create) {
                return null;
            }
            this.mThemedEntries = new ArrayMap(1);
        }
        ThemeKey key = t.getKey();
        LongSparseArray<WeakReference<T>> cache = (LongSparseArray) this.mThemedEntries.get(key);
        if (cache == null && create) {
            cache = new LongSparseArray(1);
            this.mThemedEntries.put(key.clone(), cache);
        }
        return cache;
    }

    private LongSparseArray<WeakReference<T>> getUnthemedLocked(boolean create) {
        if (this.mUnthemedEntries == null && create) {
            this.mUnthemedEntries = new LongSparseArray(1);
        }
        return this.mUnthemedEntries;
    }

    private boolean prune(int configChanges) {
        boolean z;
        synchronized (this) {
            if (this.mThemedEntries != null) {
                for (int i = this.mThemedEntries.size() - 1; i >= 0; i--) {
                    if (pruneEntriesLocked((LongSparseArray) this.mThemedEntries.valueAt(i), configChanges)) {
                        this.mThemedEntries.removeAt(i);
                    }
                }
            }
            pruneEntriesLocked(this.mNullThemedEntries, configChanges);
            pruneEntriesLocked(this.mUnthemedEntries, configChanges);
            z = (this.mThemedEntries == null && this.mNullThemedEntries == null) ? this.mUnthemedEntries == null : false;
        }
        return z;
    }

    private boolean pruneEntriesLocked(LongSparseArray<WeakReference<T>> entries, int configChanges) {
        boolean z = true;
        if (entries == null) {
            return true;
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            WeakReference<T> ref = (WeakReference) entries.valueAt(i);
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
        if (entry == null) {
            return true;
        }
        if (configChanges != 0) {
            return shouldInvalidateEntry(entry, configChanges);
        }
        return false;
    }
}
