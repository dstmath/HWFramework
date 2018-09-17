package com.android.internal.view.menu;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import com.android.internal.R;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView.ItemView;
import java.util.ArrayList;

public class ListMenuPresenter implements MenuPresenter, OnItemClickListener {
    private static final String TAG = "ListMenuPresenter";
    public static final String VIEWS_TAG = "android:menu:list";
    MenuAdapter mAdapter;
    private Callback mCallback;
    Context mContext;
    private int mId;
    LayoutInflater mInflater;
    private int mItemIndexOffset;
    int mItemLayoutRes;
    MenuBuilder mMenu;
    ExpandedMenuView mMenuView;
    int mThemeRes;

    private class MenuAdapter extends BaseAdapter {
        private int mExpandedIndex = -1;

        public MenuAdapter() {
            findExpandedIndex();
        }

        public int getCount() {
            int count = ListMenuPresenter.this.mMenu.getNonActionItems().size() - ListMenuPresenter.this.mItemIndexOffset;
            if (this.mExpandedIndex < 0) {
                return count;
            }
            return count - 1;
        }

        public MenuItemImpl getItem(int position) {
            ArrayList<MenuItemImpl> items = ListMenuPresenter.this.mMenu.getNonActionItems();
            position += ListMenuPresenter.this.mItemIndexOffset;
            if (this.mExpandedIndex >= 0 && position >= this.mExpandedIndex) {
                position++;
            }
            return (MenuItemImpl) items.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = ListMenuPresenter.this.mInflater.inflate(ListMenuPresenter.this.mItemLayoutRes, parent, false);
            }
            ((ItemView) convertView).initialize(getItem(position), 0);
            return convertView;
        }

        void findExpandedIndex() {
            MenuItemImpl expandedItem = ListMenuPresenter.this.mMenu.getExpandedItem();
            if (expandedItem != null) {
                ArrayList<MenuItemImpl> items = ListMenuPresenter.this.mMenu.getNonActionItems();
                int count = items.size();
                for (int i = 0; i < count; i++) {
                    if (((MenuItemImpl) items.get(i)) == expandedItem) {
                        this.mExpandedIndex = i;
                        return;
                    }
                }
            }
            this.mExpandedIndex = -1;
        }

        public void notifyDataSetChanged() {
            findExpandedIndex();
            super.notifyDataSetChanged();
        }
    }

    public ListMenuPresenter(Context context, int itemLayoutRes) {
        this(itemLayoutRes, 0);
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
    }

    public ListMenuPresenter(int itemLayoutRes, int themeRes) {
        this.mItemLayoutRes = itemLayoutRes;
        this.mThemeRes = themeRes;
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        if (this.mThemeRes != 0) {
            this.mContext = new ContextThemeWrapper(context, this.mThemeRes);
            this.mInflater = LayoutInflater.from(this.mContext);
        } else if (this.mContext != null) {
            this.mContext = context;
            if (this.mInflater == null) {
                this.mInflater = LayoutInflater.from(this.mContext);
            }
        }
        this.mMenu = menu;
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public MenuView getMenuView(ViewGroup root) {
        if (this.mMenuView == null) {
            this.mMenuView = (ExpandedMenuView) this.mInflater.inflate((int) R.layout.expanded_menu_layout, root, false);
            if (this.mAdapter == null) {
                this.mAdapter = new MenuAdapter();
            }
            this.mMenuView.setAdapter(this.mAdapter);
            this.mMenuView.setOnItemClickListener(this);
        }
        return this.mMenuView;
    }

    public ListAdapter getAdapter() {
        if (this.mAdapter == null) {
            this.mAdapter = new MenuAdapter();
        }
        return this.mAdapter;
    }

    public void updateMenuView(boolean cleared) {
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void setCallback(Callback cb) {
        this.mCallback = cb;
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        new MenuDialogHelper(subMenu).show(null);
        if (this.mCallback != null) {
            this.mCallback.onOpenSubMenu(subMenu);
        }
        return true;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (this.mCallback != null) {
            this.mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    int getItemIndexOffset() {
        return this.mItemIndexOffset;
    }

    public void setItemIndexOffset(int offset) {
        this.mItemIndexOffset = offset;
        if (this.mMenuView != null) {
            updateMenuView(false);
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.mMenu.performItemAction(this.mAdapter.getItem(position), this, 0);
    }

    public boolean flagActionItems() {
        return false;
    }

    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    public void saveHierarchyState(Bundle outState) {
        SparseArray<Parcelable> viewStates = new SparseArray();
        if (this.mMenuView != null) {
            this.mMenuView.saveHierarchyState(viewStates);
        }
        outState.putSparseParcelableArray(VIEWS_TAG, viewStates);
    }

    public void restoreHierarchyState(Bundle inState) {
        SparseArray<Parcelable> viewStates = inState.getSparseParcelableArray(VIEWS_TAG);
        if (viewStates != null) {
            this.mMenuView.restoreHierarchyState(viewStates);
        }
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {
        return this.mId;
    }

    public Parcelable onSaveInstanceState() {
        if (this.mMenuView == null) {
            return null;
        }
        Bundle state = new Bundle();
        saveHierarchyState(state);
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        restoreHierarchyState((Bundle) state);
    }
}
