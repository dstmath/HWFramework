package com.android.i18n.phonenumbers.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegexCache {
    private LRUCache<String, Pattern> cache;

    public RegexCache(int size) {
        this.cache = new LRUCache<>(size);
    }

    public Pattern getPatternForRegex(String regex) {
        Pattern pattern = this.cache.get(regex);
        if (pattern != null) {
            return pattern;
        }
        Pattern pattern2 = Pattern.compile(regex);
        this.cache.put(regex, pattern2);
        return pattern2;
    }

    /* access modifiers changed from: package-private */
    public boolean containsRegex(String regex) {
        return this.cache.containsKey(regex);
    }

    /* access modifiers changed from: private */
    public static class LRUCache<K, V> {
        private LinkedHashMap<K, V> map;
        private int size;

        public LRUCache(int size2) {
            this.size = size2;
            this.map = new LinkedHashMap<K, V>(((size2 * 4) / 3) + 1, 0.75f, true) {
                /* class com.android.i18n.phonenumbers.internal.RegexCache.LRUCache.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // java.util.LinkedHashMap
                public boolean removeEldestEntry(Map.Entry<K, V> entry) {
                    return size() > LRUCache.this.size;
                }
            };
        }

        public synchronized V get(K key) {
            return this.map.get(key);
        }

        public synchronized void put(K key, V value) {
            this.map.put(key, value);
        }

        public synchronized boolean containsKey(K key) {
            return this.map.containsKey(key);
        }
    }
}
