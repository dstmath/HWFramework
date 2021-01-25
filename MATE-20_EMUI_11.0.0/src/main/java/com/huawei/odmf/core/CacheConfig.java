package com.huawei.odmf.core;

import com.huawei.odmf.utils.ODMFCache;
import com.huawei.odmf.utils.Singleton;

public final class CacheConfig {
    private static final Singleton<CacheConfig> DEFAULT_CONFIG = new Singleton<CacheConfig>() {
        /* class com.huawei.odmf.core.CacheConfig.AnonymousClass1 */

        @Override // com.huawei.odmf.utils.Singleton
        public CacheConfig create() {
            return new CacheConfig();
        }
    };
    private boolean isOpenObjectCache;
    private final Object lock;
    private int objectCacheNum;

    private CacheConfig() {
        this.lock = new Object();
        this.isOpenObjectCache = false;
        this.objectCacheNum = 100;
    }

    public static CacheConfig getDefault() {
        return DEFAULT_CONFIG.get();
    }

    public boolean isOpenObjectCache() {
        boolean z;
        synchronized (this.lock) {
            z = this.isOpenObjectCache;
        }
        return z;
    }

    public void setOpenObjectCache(boolean z) {
        synchronized (this.lock) {
            boolean z2 = this.isOpenObjectCache;
            this.isOpenObjectCache = z;
            if (!z2 && !this.isOpenObjectCache) {
                return;
            }
            if (!z2 || !this.isOpenObjectCache) {
                ODMFCache<ObjectId, ManagedObject> objectsCache = PersistentStoreCoordinator.getDefault().getObjectsCache();
                if (objectsCache != null) {
                    objectsCache.clear();
                }
                if (this.isOpenObjectCache) {
                    PersistentStoreCoordinator.getDefault().createObjectsCache();
                }
            }
        }
    }

    public void setOpenObjectCache(boolean z, int i) {
        synchronized (this.lock) {
            boolean z2 = this.isOpenObjectCache;
            this.isOpenObjectCache = z;
            if (z2 || this.isOpenObjectCache) {
                ODMFCache<ObjectId, ManagedObject> objectsCache = PersistentStoreCoordinator.getDefault().getObjectsCache();
                if (objectsCache != null) {
                    objectsCache.clear();
                }
                if (this.isOpenObjectCache) {
                    setObjectCacheNum(i);
                    PersistentStoreCoordinator.getDefault().createObjectsCache();
                }
            }
        }
    }

    public int getObjectCacheNum() {
        int i;
        synchronized (this.lock) {
            i = this.objectCacheNum;
        }
        return i;
    }

    private void setObjectCacheNum(int i) {
        synchronized (this.lock) {
            this.objectCacheNum = i;
        }
    }
}
