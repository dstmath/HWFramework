package huawei.android.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import huawei.android.widget.Attributes.Mode;
import java.util.List;

public abstract class ArraySwipeAdapter<T> extends ArrayAdapter implements SwipeItemMangerInterface, SwipeAdapterInterface {
    private SwipeItemMangerImpl mItemManger = new SwipeItemMangerImpl(this);

    public ArraySwipeAdapter(Context context, int resource) {
        super(context, resource);
    }

    public ArraySwipeAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public ArraySwipeAdapter(Context context, int resource, T[] objects) {
        super(context, resource, objects);
    }

    public ArraySwipeAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public ArraySwipeAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    public ArraySwipeAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        this.mItemManger.bind(v, position);
        return v;
    }

    public void openItem(int position) {
        this.mItemManger.openItem(position);
    }

    public void closeItem(int position) {
        this.mItemManger.closeItem(position);
    }

    public void closeAllExcept(SwipeLayout layout) {
        this.mItemManger.closeAllExcept(layout);
    }

    public void closeAllItems() {
        this.mItemManger.closeAllItems();
    }

    public List<Integer> getOpenItems() {
        return this.mItemManger.getOpenItems();
    }

    public List<SwipeLayout> getOpenLayouts() {
        return this.mItemManger.getOpenLayouts();
    }

    public void removeShownLayouts(SwipeLayout layout) {
        this.mItemManger.removeShownLayouts(layout);
    }

    public boolean isOpen(int position) {
        return this.mItemManger.isOpen(position);
    }

    public Mode getMode() {
        return this.mItemManger.getMode();
    }

    public void setMode(Mode mode) {
        this.mItemManger.setMode(mode);
    }

    public void setDismissCallback(SwipeDismissCallback dismissCallback) {
        this.mItemManger.setDismissCallback(dismissCallback);
    }

    public void deleteItem(View childView, int position) {
        this.mItemManger.deleteItem(childView, position);
    }
}
