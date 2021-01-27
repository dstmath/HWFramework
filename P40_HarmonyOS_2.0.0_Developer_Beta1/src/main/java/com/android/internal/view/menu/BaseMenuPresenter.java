package com.android.internal.view.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import java.util.ArrayList;

public abstract class BaseMenuPresenter implements MenuPresenter {
    private MenuPresenter.Callback mCallback;
    protected Context mContext;
    private int mId;
    protected LayoutInflater mInflater;
    private int mItemLayoutRes;
    protected MenuBuilder mMenu;
    private int mMenuLayoutRes;
    protected MenuView mMenuView;
    protected Context mSystemContext;
    protected LayoutInflater mSystemInflater;

    public abstract void bindItemView(MenuItemImpl menuItemImpl, MenuView.ItemView itemView);

    public BaseMenuPresenter(Context context, int menuLayoutRes, int itemLayoutRes) {
        this.mSystemContext = context;
        this.mSystemInflater = LayoutInflater.from(context);
        this.mMenuLayoutRes = menuLayoutRes;
        this.mItemLayoutRes = itemLayoutRes;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void initForMenu(Context context, MenuBuilder menu) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(this.mContext);
        this.mMenu = menu;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public MenuView getMenuView(ViewGroup root) {
        if (this.mMenuView == null) {
            this.mMenuView = (MenuView) this.mSystemInflater.inflate(this.mMenuLayoutRes, root, false);
            this.mMenuView.initialize(this.mMenu);
            updateMenuView(true);
        }
        return this.mMenuView;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void updateMenuView(boolean cleared) {
        ViewGroup parent = (ViewGroup) this.mMenuView;
        if (parent != null) {
            int childIndex = 0;
            MenuBuilder menuBuilder = this.mMenu;
            if (menuBuilder != null) {
                menuBuilder.flagActionItems();
                ArrayList<MenuItemImpl> visibleItems = this.mMenu.getVisibleItems();
                int itemCount = visibleItems.size();
                for (int i = 0; i < itemCount; i++) {
                    MenuItemImpl item = visibleItems.get(i);
                    if (shouldIncludeItem(childIndex, item)) {
                        View convertView = parent.getChildAt(childIndex);
                        MenuItemImpl oldItem = convertView instanceof MenuView.ItemView ? ((MenuView.ItemView) convertView).getItemData() : null;
                        View itemView = getItemView(item, convertView, parent);
                        if (item != oldItem) {
                            itemView.setPressed(false);
                            itemView.jumpDrawablesToCurrentState();
                        }
                        if (itemView != convertView) {
                            addItemView(itemView, childIndex);
                        }
                        childIndex++;
                    }
                }
            }
            while (childIndex < parent.getChildCount()) {
                if (!filterLeftoverView(parent, childIndex)) {
                    childIndex++;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addItemView(View itemView, int childIndex) {
        ViewGroup currentParent = (ViewGroup) itemView.getParent();
        if (currentParent != null) {
            currentParent.removeView(itemView);
        }
        ((ViewGroup) this.mMenuView).addView(itemView, childIndex);
    }

    /* access modifiers changed from: protected */
    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        parent.removeViewAt(childIndex);
        return true;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void setCallback(MenuPresenter.Callback cb) {
        this.mCallback = cb;
    }

    public MenuPresenter.Callback getCallback() {
        return this.mCallback;
    }

    public MenuView.ItemView createItemView(ViewGroup parent) {
        return (MenuView.ItemView) this.mSystemInflater.inflate(this.mItemLayoutRes, parent, false);
    }

    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        MenuView.ItemView itemView;
        if (convertView instanceof MenuView.ItemView) {
            itemView = (MenuView.ItemView) convertView;
        } else {
            itemView = createItemView(parent);
        }
        bindItemView(item, itemView);
        return (View) itemView;
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return true;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        MenuPresenter.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public boolean onSubMenuSelected(SubMenuBuilder menu) {
        MenuPresenter.Callback callback = this.mCallback;
        if (callback != null) {
            return callback.onOpenSubMenu(menu);
        }
        return false;
    }

    @Override // com.android.internal.view.menu.MenuPresenter
    public boolean flagActionItems() {
        return false;
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
        return this.mId;
    }

    public void setId(int id) {
        this.mId = id;
    }
}
