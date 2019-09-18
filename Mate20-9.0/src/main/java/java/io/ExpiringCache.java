package java.io;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class ExpiringCache {
    /* access modifiers changed from: private */
    public int MAX_ENTRIES;
    private Map<String, Entry> map;
    private long millisUntilExpiration;
    private int queryCount;
    private int queryOverflow;

    static class Entry {
        private long timestamp;
        private String val;

        Entry(long timestamp2, String val2) {
            this.timestamp = timestamp2;
            this.val = val2;
        }

        /* access modifiers changed from: package-private */
        public long timestamp() {
            return this.timestamp;
        }

        /* access modifiers changed from: package-private */
        public void setTimestamp(long timestamp2) {
            this.timestamp = timestamp2;
        }

        /* access modifiers changed from: package-private */
        public String val() {
            return this.val;
        }

        /* access modifiers changed from: package-private */
        public void setVal(String val2) {
            this.val = val2;
        }
    }

    ExpiringCache() {
        this(30000);
    }

    ExpiringCache(long millisUntilExpiration2) {
        this.queryOverflow = HttpURLConnection.HTTP_MULT_CHOICE;
        this.MAX_ENTRIES = HttpURLConnection.HTTP_OK;
        this.millisUntilExpiration = millisUntilExpiration2;
        this.map = new LinkedHashMap<String, Entry>() {
            /* access modifiers changed from: protected */
            public boolean removeEldestEntry(Map.Entry<String, Entry> entry) {
                return size() > ExpiringCache.this.MAX_ENTRIES;
            }
        };
    }

    /* access modifiers changed from: package-private */
    public synchronized String get(String key) {
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

    /* access modifiers changed from: package-private */
    public synchronized void put(String key, String val) {
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

    /* access modifiers changed from: package-private */
    public synchronized void clear() {
        this.map.clear();
    }

    private Entry entryFor(String key) {
        Entry entry = this.map.get(key);
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
            keys[i] = key;
            i++;
        }
        for (String entryFor : keys) {
            entryFor(entryFor);
        }
        this.queryCount = 0;
    }
}
