package ohos.agp.components;

import java.util.HashMap;
import java.util.Map;
import ohos.agp.database.DataSetPublisher;
import ohos.agp.database.DataSetSubscriber;

public abstract class PageSliderProvider {
    public static final int POSITION_INVALID = -2;
    public static final int POSITION_REMAIN = -1;
    private final Map<Long, Object> mNativeCache = new HashMap();
    private long mNativeCounter = 1;
    private final DataSetPublisher mPublisher = new DataSetPublisher();

    public abstract Object createPageInContainer(ComponentContainer componentContainer, int i);

    public abstract void destroyPageFromContainer(ComponentContainer componentContainer, int i, Object obj);

    public abstract int getCount();

    public int getPageIndex(Object obj) {
        return -1;
    }

    public String getPageTitle(int i) {
        return null;
    }

    public abstract boolean isPageMatchToObject(Component component, Object obj);

    public void onUpdateFinished(ComponentContainer componentContainer) {
    }

    public void startUpdate(ComponentContainer componentContainer) {
    }

    public void notifyDataChanged() {
        this.mPublisher.notifyChanged();
    }

    public final void addDataSubscriber(DataSetSubscriber dataSetSubscriber) {
        this.mPublisher.registerSubscriber(dataSetSubscriber);
    }

    public final void removeDataSubscriber(DataSetSubscriber dataSetSubscriber) {
        this.mPublisher.unregisterSubscriber((DataSetPublisher) dataSetSubscriber);
    }

    private void removeDataSubscriber(long j) {
        this.mPublisher.unregisterSubscriber(j);
    }

    private long instantiateItemFromNative(ComponentContainer componentContainer, int i) {
        Object createPageInContainer = createPageInContainer(componentContainer, i);
        long j = this.mNativeCounter;
        this.mNativeCounter = 1 + j;
        this.mNativeCache.put(Long.valueOf(j), createPageInContainer);
        return j;
    }

    private void destroyItemFromNative(ComponentContainer componentContainer, int i, long j) {
        destroyPageFromContainer(componentContainer, i, this.mNativeCache.remove(Long.valueOf(j)));
    }

    private boolean isComponentFromObjectFromNative(Component component, long j) {
        return isPageMatchToObject(component, this.mNativeCache.get(Long.valueOf(j)));
    }
}
