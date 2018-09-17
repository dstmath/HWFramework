package huawei.android.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import huawei.android.widget.Attributes.Mode;
import java.util.List;

public abstract class BaseSwipeAdapter extends BaseAdapter implements SwipeItemMangerInterface, SwipeAdapterInterface {
    protected SwipeItemMangerImpl mItemManger = new SwipeItemMangerImpl(this);

    public abstract void fillValues(int i, View view);

    public abstract View generateView(int i, ViewGroup viewGroup);

    public abstract int getSwipeLayoutResourceId(int i);

    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }

    public final View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (convertView == null) {
            v = generateView(position, parent);
        }
        this.mItemManger.bind(v, position);
        fillValues(position, v);
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
