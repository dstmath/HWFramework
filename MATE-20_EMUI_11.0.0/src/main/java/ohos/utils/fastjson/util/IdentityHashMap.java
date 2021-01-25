package ohos.utils.fastjson.util;

import java.lang.reflect.Type;

public class IdentityHashMap<V> {
    private final Entry<V>[] buckets;
    private final int indexMask;

    public IdentityHashMap(int i) {
        this.indexMask = i - 1;
        this.buckets = new Entry[i];
    }

    public final V get(Type type) {
        for (Entry<V> entry = this.buckets[System.identityHashCode(type) & this.indexMask]; entry != null; entry = entry.next) {
            if (type == entry.key) {
                return entry.value;
            }
        }
        return null;
    }

    public boolean put(Type type, V v) {
        int identityHashCode = System.identityHashCode(type);
        int i = this.indexMask & identityHashCode;
        for (Entry<V> entry = this.buckets[i]; entry != null; entry = entry.next) {
            if (type == entry.key) {
                entry.value = v;
                return true;
            }
        }
        this.buckets[i] = new Entry<>(type, v, identityHashCode, this.buckets[i]);
        return false;
    }

    public Class findClass(String str) {
        int i = 0;
        while (true) {
            Entry<V>[] entryArr = this.buckets;
            if (i >= entryArr.length) {
                return null;
            }
            Entry<V> entry = entryArr[i];
            if (entry != null) {
                for (Entry<V> entry2 = entry; entry2 != null; entry2 = entry2.next) {
                    Type type = entry.key;
                    if (type instanceof Class) {
                        Class cls = (Class) type;
                        if (cls.getName().equals(str)) {
                            return cls;
                        }
                    }
                }
                continue;
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public static final class Entry<V> {
        public final int hashCode;
        public final Type key;
        public final Entry<V> next;
        public V value;

        public Entry(Type type, V v, int i, Entry<V> entry) {
            this.key = type;
            this.value = v;
            this.next = entry;
            this.hashCode = i;
        }
    }
}
