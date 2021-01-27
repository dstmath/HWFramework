package ohos.agp.components;

import java.util.HashSet;
import java.util.Set;
import ohos.agp.database.DataSetPublisher;
import ohos.agp.database.DataSetSubscriber;

public abstract class BaseItemProvider {
    private static final int DEFAULT_COMPONENT_COUNT_NUM = 1;
    private static final int DEFAULT_COMPONENT_TYPE = 0;
    private final Set<Component> mChildrenPool = new HashSet();
    private TextFilter mFilter = null;
    private final DataSetPublisher mPublisher = new DataSetPublisher();

    @Deprecated
    private void removeJavaComponent(int i) {
    }

    public abstract Component getComponent(int i, Component component, ComponentContainer componentContainer);

    public int getComponentTypeCount() {
        return 1;
    }

    public abstract int getCount();

    public abstract Object getItem(int i);

    public int getItemComponentType(int i) {
        return 0;
    }

    public abstract long getItemId(int i);

    public void notifyDataChanged() {
        this.mPublisher.notifyChanged();
    }

    public void notifyDataInvalidated() {
        this.mPublisher.informInvalidated();
    }

    public void notifyDataSetItemChanged(int i) {
        this.mPublisher.notifyItemChanged(i);
    }

    public void notifyDataSetItemInserted(int i) {
        this.mPublisher.notifyItemInserted(i);
    }

    public void notifyDataSetItemRemoved(int i) {
        this.mPublisher.notifyItemRemoved(i);
    }

    public void notifyDataSetItemRangeChanged(int i, int i2) {
        this.mPublisher.notifyItemRangeChanged(i, i2);
    }

    public void notifyDataSetItemRangeInserted(int i, int i2) {
        this.mPublisher.notifyItemRangeInserted(i, i2);
    }

    public void notifyDataSetItemRangeRemoved(int i, int i2) {
        this.mPublisher.notifyItemRangeRemoved(i, i2);
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

    public void setFilter(TextFilter textFilter) {
        this.mFilter = textFilter;
    }

    public TextFilter getFilter() {
        return this.mFilter;
    }

    public void filter(CharSequence charSequence) {
        TextFilter textFilter = this.mFilter;
        if (textFilter != null) {
            textFilter.filter(charSequence);
        }
    }

    private Component getJavaComponent(int i, Component component, ComponentContainer componentContainer) {
        Component component2 = getComponent(i, component, componentContainer);
        if (component2 != null) {
            this.mChildrenPool.add(component2);
        }
        return component2;
    }

    private void removeJavaComponent(Component component) {
        this.mChildrenPool.remove(component);
    }
}
