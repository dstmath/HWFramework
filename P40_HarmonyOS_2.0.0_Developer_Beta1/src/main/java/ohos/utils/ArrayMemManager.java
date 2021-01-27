package ohos.utils;

public class ArrayMemManager {
    public int CACHE_ARRAY_CAPACITY;
    public int CACHE_BASE_SIZE = 4;
    private int CACHE_CAPACITY_THRESH;
    public int CACHE_LEVEL = 2;
    public int CACHE_SHRINK_DIVISOR;
    public int MAX_CACHE_LEVEL;
    private int[] cacheSize;
    private Object[][] caches;

    public ArrayMemManager() {
        int i = this.CACHE_BASE_SIZE;
        int i2 = this.CACHE_LEVEL;
        this.CACHE_CAPACITY_THRESH = i * (i2 - 1) * 2;
        this.MAX_CACHE_LEVEL = 7;
        this.CACHE_ARRAY_CAPACITY = 10;
        this.CACHE_SHRINK_DIVISOR = 3;
        this.caches = new Object[i2][];
        this.cacheSize = new int[i2];
    }

    private int getCacheSizeLevel(int i) {
        int i2 = this.CACHE_BASE_SIZE;
        int i3 = 0;
        if (i <= i2) {
            return 0;
        }
        do {
            i3++;
            i2 <<= 1;
            if (i <= i2) {
                break;
            }
        } while (i3 < this.MAX_CACHE_LEVEL);
        return i3 >= this.CACHE_LEVEL ? ~i3 : i3;
    }

    public int getExpandCapacity(int i, int i2) {
        if (i2 < i) {
            return -1;
        }
        int cacheSizeLevel = getCacheSizeLevel(i);
        if (cacheSizeLevel < 0) {
            cacheSizeLevel = ~cacheSizeLevel;
        }
        if (cacheSizeLevel >= this.CACHE_LEVEL) {
            return i + (i >> 1);
        }
        return i == 0 ? this.CACHE_BASE_SIZE : i << 1;
    }

    public int getShrinkCapacity(int i, int i2) {
        int i3 = this.CACHE_CAPACITY_THRESH;
        if (i <= i3 || i2 >= i / this.CACHE_SHRINK_DIVISOR) {
            return -1;
        }
        int i4 = (i * 2) / 3;
        return i4 < i3 ? i3 : i4;
    }

    public synchronized Object[] allocFromCache(int i) {
        int cacheSizeLevel = getCacheSizeLevel(i);
        if (cacheSizeLevel >= 0 && this.cacheSize[cacheSizeLevel] > 0) {
            if (this.caches[cacheSizeLevel] != null) {
                Object[] objArr = this.caches[cacheSizeLevel];
                this.caches[cacheSizeLevel] = (Object[]) objArr[0];
                int[] iArr = this.cacheSize;
                iArr[cacheSizeLevel] = iArr[cacheSizeLevel] - 1;
                return objArr;
            }
        }
        return null;
    }

    public synchronized void freeToCache(Object[] objArr, int i) {
        if (i != 0) {
            if (i <= this.CACHE_CAPACITY_THRESH) {
                int cacheSizeLevel = getCacheSizeLevel(i);
                if (cacheSizeLevel >= 0) {
                    if (cacheSizeLevel <= this.CACHE_LEVEL && this.cacheSize[cacheSizeLevel] < this.CACHE_ARRAY_CAPACITY) {
                        objArr[0] = this.caches[cacheSizeLevel];
                        this.caches[cacheSizeLevel] = objArr;
                        int[] iArr = this.cacheSize;
                        iArr[cacheSizeLevel] = iArr[cacheSizeLevel] + 1;
                    }
                }
            }
        }
    }
}
