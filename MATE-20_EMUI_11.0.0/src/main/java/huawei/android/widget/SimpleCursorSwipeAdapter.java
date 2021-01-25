package huawei.android.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import huawei.android.widget.Attributes;
import java.util.List;

public abstract class SimpleCursorSwipeAdapter extends SimpleCursorAdapter implements SwipeItemMangerInterface, SwipeAdapterInterface {
    private SwipeItemMangerImpl mItemManger = new SwipeItemMangerImpl(this);

    @Override // huawei.android.widget.SwipeAdapterInterface
    public abstract int getSwipeLayoutResourceId(int i);

    protected SimpleCursorSwipeAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags) {
        super(context, layout, cursor, from, to, flags);
    }

    protected SimpleCursorSwipeAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
        super(context, layout, cursor, from, to);
    }

    @Override // android.widget.Adapter, android.widget.CursorAdapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        this.mItemManger.bind(view, position);
        return view;
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void openItem(int position) {
        this.mItemManger.openItem(position);
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void closeItem(int position) {
        this.mItemManger.closeItem(position);
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void closeAllExcept(SwipeLayout layout) {
        this.mItemManger.closeAllExcept(layout);
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void closeAllItems() {
        this.mItemManger.closeAllItems();
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public List<Integer> getOpenItems() {
        return this.mItemManger.getOpenItems();
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public List<SwipeLayout> getOpenLayouts() {
        return this.mItemManger.getOpenLayouts();
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void removeShownLayouts(SwipeLayout layout) {
        this.mItemManger.removeShownLayouts(layout);
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public boolean isOpen(int position) {
        return this.mItemManger.isOpen(position);
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public Attributes.Mode getMode() {
        return this.mItemManger.getMode();
    }

    @Override // huawei.android.widget.SwipeItemMangerInterface
    public void setMode(Attributes.Mode mode) {
        this.mItemManger.setMode(mode);
    }

    public void setDismissCallback(SwipeDismissCallback dismissCallback) {
        this.mItemManger.setDismissCallback(dismissCallback);
    }

    public void deleteItem(View childView, int position) {
        this.mItemManger.deleteItem(childView, position);
    }
}
