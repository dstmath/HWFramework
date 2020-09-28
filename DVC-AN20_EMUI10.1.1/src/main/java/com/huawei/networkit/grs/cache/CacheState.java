package com.huawei.networkit.grs.cache;

public class CacheState {
    public static final int CACHE_EXPIRED = 2;
    public static final int CACHE_UNAVAILABLE = 3;
    public static final int CACHE_UNEXPIRED = 1;
    private int cacheState = 3;

    public void setCacheState(int cacheState2) {
        this.cacheState = cacheState2;
    }

    public boolean isUnexpired() {
        return this.cacheState == 1;
    }

    public int getCacheState() {
        return this.cacheState;
    }
}
