package ohos.agp.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import ohos.agp.database.DataSetPublisher;
import ohos.agp.database.DataSetSubscriber;

public abstract class BaseItemProvider {
    private static final int DEFAULT_COMPONENT_COUNT_NUM = 1;
    private static final int DEFAULT_COMPONENT_TYPE = 0;
    private final Map<Integer, Component> mChildren = new HashMap();
    private final Set<Component> mChildrenToRemove = new HashSet();
    private TextFilter mFilter = null;
    private final DataSetPublisher mPublisher = new DataSetPublisher();

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

    public void notifyDataSetChanged() {
        this.mPublisher.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        this.mPublisher.notifyInvalidated();
    }

    public final void registerDataSetSubscriber(DataSetSubscriber dataSetSubscriber) {
        this.mPublisher.registerSubscriber(dataSetSubscriber);
    }

    public final void unregisterDataSetSubscriber(DataSetSubscriber dataSetSubscriber) {
        this.mPublisher.unregisterSubscriber((DataSetPublisher) dataSetSubscriber);
    }

    private void unregisterDataSetSubscriber(long j) {
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
            this.mChildren.put(Integer.valueOf(i), component2);
        }
        return component2;
    }

    private void removeJavaComponent(int i) {
        Component remove = this.mChildren.remove(Integer.valueOf(i));
        if (remove != null && !this.mChildrenToRemove.contains(remove)) {
            this.mChildrenToRemove.add(remove);
        }
    }

    private void removeJavaComponent(Component component) {
        this.mChildrenToRemove.remove(component);
    }
}
