package ohos.agp.components;

public abstract class RecycleItemProvider extends BaseItemProvider {
    private static final int DEFAULT_CACHE_SIZE = Integer.MAX_VALUE;
    private int mCacheSize = Integer.MAX_VALUE;

    public void onItemMoved(int i, int i2) {
    }

    public void setCacheSize(int i) {
        this.mCacheSize = i;
    }

    public int getCacheSize() {
        return this.mCacheSize;
    }
}
