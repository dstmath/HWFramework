package java.io;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class ExpiringCache {
    private int MAX_ENTRIES;
    private Map<String, Entry> map;
    private long millisUntilExpiration;
    private int queryCount;
    private int queryOverflow;

    static class Entry {
        private long timestamp;
        private String val;

        Entry(long timestamp, String val) {
            this.timestamp = timestamp;
            this.val = val;
        }

        long timestamp() {
            return this.timestamp;
        }

        void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        String val() {
            return this.val;
        }

        void setVal(String val) {
            this.val = val;
        }
    }

    ExpiringCache() {
        this(30000);
    }

    ExpiringCache(long millisUntilExpiration) {
        this.queryOverflow = 300;
        this.MAX_ENTRIES = HttpURLConnection.HTTP_OK;
        this.millisUntilExpiration = millisUntilExpiration;
        this.map = new LinkedHashMap<String, Entry>() {
            protected boolean removeEldestEntry(java.util.Map.Entry eldest) {
                return size() > ExpiringCache.this.MAX_ENTRIES;
            }
        };
    }

    synchronized String get(String key) {
        int i = this.queryCount + 1;
        this.queryCount = i;
        if (i >= this.queryOverflow) {
            cleanup();
        }
        Entry entry = entryFor(key);
        if (entry == null) {
            return null;
        }
        return entry.val();
    }

    synchronized void put(String key, String val) {
        int i = this.queryCount + 1;
        this.queryCount = i;
        if (i >= this.queryOverflow) {
            cleanup();
        }
        Entry entry = entryFor(key);
        if (entry != null) {
            entry.setTimestamp(System.currentTimeMillis());
            entry.setVal(val);
        } else {
            this.map.put(key, new Entry(System.currentTimeMillis(), val));
        }
    }

    synchronized void clear() {
        this.map.clear();
    }

    private Entry entryFor(String key) {
        Entry entry = (Entry) this.map.get(key);
        if (entry == null) {
            return entry;
        }
        long delta = System.currentTimeMillis() - entry.timestamp();
        if (delta >= 0 && delta < this.millisUntilExpiration) {
            return entry;
        }
        this.map.remove(key);
        return null;
    }

    private void cleanup() {
        Set<String> keySet = this.map.keySet();
        String[] keys = new String[keySet.size()];
        int i = 0;
        for (String key : keySet) {
            int i2 = i + 1;
            keys[i] = key;
            i = i2;
        }
        for (String entryFor : keys) {
            entryFor(entryFor);
        }
        this.queryCount = 0;
    }
}
