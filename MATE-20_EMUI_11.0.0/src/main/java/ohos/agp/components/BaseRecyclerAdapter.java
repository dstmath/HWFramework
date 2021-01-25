package ohos.agp.components;

@Deprecated
public abstract class BaseRecyclerAdapter extends BaseItemProvider {
    private static final int DEFAULT_CACHE_SIZE = 30;
    private int mCacheSize = 30;

    @Deprecated
    public void onItemClicked(int i, Component component, ComponentContainer componentContainer) {
    }

    @Deprecated
    public void onItemLongClicked(int i, Component component, ComponentContainer componentContainer) {
    }

    public void onItemMoved(int i, int i2) {
    }

    @Deprecated
    public void releaseView(int i, Component component) {
    }

    public void setCacheSize(int i) {
        this.mCacheSize = i;
    }

    public int getCacheSize() {
        return this.mCacheSize;
    }
}
