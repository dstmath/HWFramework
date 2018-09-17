package java.net;

import libcore.util.BasicLruCache;

class AddressCache {
    private static final int MAX_ENTRIES = 16;
    private static final long TTL_NANOS = 2000000000;
    private final BasicLruCache<AddressCacheKey, AddressCacheEntry> cache = new BasicLruCache(16);

    static class AddressCacheEntry {
        final long expiryNanos = (System.nanoTime() + AddressCache.TTL_NANOS);
        final Object value;

        AddressCacheEntry(Object value) {
            this.value = value;
        }
    }

    static class AddressCacheKey {
        private final String mHostname;
        private final int mNetId;

        AddressCacheKey(String hostname, int netId) {
            this.mHostname = hostname;
            this.mNetId = netId;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (!(o instanceof AddressCacheKey)) {
                return false;
            }
            AddressCacheKey lhs = (AddressCacheKey) o;
            if (!(this.mHostname.equals(lhs.mHostname) && this.mNetId == lhs.mNetId)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return ((this.mNetId + 527) * 31) + this.mHostname.hashCode();
        }
    }

    AddressCache() {
    }

    public void clear() {
        this.cache.evictAll();
    }

    public Object get(String hostname, int netId) {
        AddressCacheEntry entry = (AddressCacheEntry) this.cache.get(new AddressCacheKey(hostname, netId));
        if (entry == null || entry.expiryNanos < System.nanoTime()) {
            return null;
        }
        return entry.value;
    }

    public void put(String hostname, int netId, InetAddress[] addresses) {
        this.cache.put(new AddressCacheKey(hostname, netId), new AddressCacheEntry(addresses));
    }

    public void putUnknownHost(String hostname, int netId, String detailMessage) {
        this.cache.put(new AddressCacheKey(hostname, netId), new AddressCacheEntry(detailMessage));
    }
}
