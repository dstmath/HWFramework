package com.android.internal.view.menu;

import android.content.Context;
import android.graphics.Rect;
import android.hwcontrol.HwWidgetFactory;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.HwWidgetColumn;
import android.widget.ListAdapter;
import android.widget.MenuPopupWindow;
import android.widget.PopupWindow;

public abstract class MenuPopup implements ShowableListMenu, MenuPresenter, AdapterView.OnItemClickListener {
    private Rect mEpicenterBounds;

    public abstract void addMenu(MenuBuilder menuBuilder);

    public abstract void setAnchorView(View view);

    public abstract void setForceShowIcon(boolean z);

    public abstract void setGravity(int i);

    public abstract void setHorizontalOffset(int i);

    public abstract void setOnDismissListener(PopupWindow.OnDismissListener onDismissListener);

    public abstract void setShowTitle(boolean z);

    public abstract void setVerticalOffset(int i);

    public void setEpicenterBounds(Rect bounds) {
        this.mEpicenterBounds = bounds;
    }

    public Rect getEpicenterBounds() {
        return this.mEpicenterBounds;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void initForMenu(Context context, MenuBuilder menu) {
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public MenuView getMenuView(ViewGroup root) {
        throw new UnsupportedOperationException("MenuPopups manage their own views");
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public int getId() {
        return 0;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListAdapter outerAdapter = (ListAdapter) parent.getAdapter();
        toMenuAdapter(outerAdapter).mAdapterMenu.performItemAction((MenuItem) outerAdapter.getItem(position), 0);
    }

    protected static int measureIndividualMenuWidth(ListAdapter adapter, ViewGroup parent, Context context, int maxAllowedWidth) {
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;
        if (HwWidgetFactory.isHwTheme(context)) {
            maxAllowedWidth = HwWidgetFactory.getHwWidgetColumn(context).getMaxColumnWidth(0);
        }
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        int count = adapter.getCount();
        int i = 0;
        while (true) {
            if (i >= count) {
                break;
            }
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
                maxWidth = maxAllowedWidth;
                break;
            }
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
            i++;
        }
        if (!HwWidgetFactory.isHwTheme(context)) {
            return maxWidth;
        }
        HwWidgetColumn hwWidgetColumn = HwWidgetFactory.getHwWidgetColumn(context);
        int menuMaxColumnWidth = hwWidgetColumn.getMaxColumnWidth(0);
        int menuMinColumnWidth = hwWidgetColumn.getMinColumnWidth(0);
        int maxWidth2 = menuMaxColumnWidth < maxWidth ? menuMaxColumnWidth : maxWidth;
        return menuMinColumnWidth > maxWidth2 ? menuMinColumnWidth : maxWidth2;
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
