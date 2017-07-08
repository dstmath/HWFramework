package com.android.internal.view.menu;

import android.content.Context;
import android.graphics.Rect;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.MenuPopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public abstract class MenuPopup implements ShowableListMenu, MenuPresenter, OnItemClickListener {
    private Rect mEpicenterBounds;

    public abstract void addMenu(MenuBuilder menuBuilder);

    public abstract void setAnchorView(View view);

    public abstract void setForceShowIcon(boolean z);

    public abstract void setGravity(int i);

    public abstract void setHorizontalOffset(int i);

    public abstract void setOnDismissListener(OnDismissListener onDismissListener);

    public abstract void setShowTitle(boolean z);

    public abstract void setVerticalOffset(int i);

    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds;
    }

    public Rect getEpicenterBounds() {
        return this.mEpicenterBounds;
    }

    public void initForMenu(Context context, MenuBuilder menu) {
    }

    public MenuView getMenuView(ViewGroup root) {
        throw new UnsupportedOperationException("MenuPopups manage their own views");
    }

    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public int getId() {
        return 0;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListAdapter outerAdapter = (ListAdapter) parent.getAdapter();
        toMenuAdapter(outerAdapter).mAdapterMenu.performItemAction((MenuItem) outerAdapter.getItem(position), 0);
    }

    protected static int measureIndividualMenuWidth(ListAdapter adapter, ViewGroup parent, Context context, int maxAllowedWidth) {
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            if (parent == null) {
                parent = new FrameLayout(context);
            }
            itemView = adapter.getView(i, itemView, parent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            int itemWidth = itemView.getMeasuredWidth();
            if (itemWidth >= maxAllowedWidth) {
                return maxAllowedWidth;
            }
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }
        return maxWidth;
    }

    protected static MenuAdapter toMenuAdapter(ListAdapter adapter) {
        if (adapter instanceof HeaderViewListAdapter) {
            return (MenuAdapter) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return (MenuAdapter) adapter;
    }

    protected static boolean shouldPreserveIconSpacing(MenuBuilder menu) {
        int count = menu.size();
        for (int i = 0; i < count; i++) {
            MenuItem childItem = menu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                return true;
            }
        }
        return false;
    }

    public MenuPopupWindow getMenuPopup() {
        return null;
    }
}
