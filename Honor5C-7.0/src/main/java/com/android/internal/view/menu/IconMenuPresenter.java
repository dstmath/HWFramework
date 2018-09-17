package com.android.internal.view.menu;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.internal.R;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView.ItemView;
import java.util.ArrayList;

public class IconMenuPresenter extends BaseMenuPresenter {
    private static final String OPEN_SUBMENU_KEY = "android:menu:icon:submenu";
    private static final String VIEWS_TAG = "android:menu:icon";
    private int mMaxItems;
    private IconMenuItemView mMoreView;
    MenuDialogHelper mOpenSubMenu;
    int mOpenSubMenuId;
    SubMenuPresenterCallback mSubMenuPresenterCallback;

    class SubMenuPresenterCallback implements Callback {
        SubMenuPresenterCallback() {
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            IconMenuPresenter.this.mOpenSubMenuId = 0;
            if (IconMenuPresenter.this.mOpenSubMenu != null) {
                IconMenuPresenter.this.mOpenSubMenu.dismiss();
                IconMenuPresenter.this.mOpenSubMenu = null;
            }
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu != null) {
                IconMenuPresenter.this.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            }
            return false;
        }
    }

    public IconMenuPresenter(Context context) {
        super(new ContextThemeWrapper(context, (int) R.style.Theme_IconMenu), R.layout.icon_menu_layout, R.layout.icon_menu_item_layout);
        this.mMaxItems = -1;
        this.mSubMenuPresenterCallback = new SubMenuPresenterCallback();
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        super.initForMenu(context, menu);
        this.mMaxItems = -1;
    }

    public void bindItemView(MenuItemImpl item, ItemView itemView) {
        IconMenuItemView view = (IconMenuItemView) itemView;
        view.setItemData(item);
        view.initialize(item.getTitleForItemView(view), item.getIcon());
        view.setVisibility(item.isVisible() ? 0 : 8);
        view.setEnabled(view.isEnabled());
        view.setLayoutParams(view.getTextAppropriateLayoutParams());
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        boolean fits = (this.mMenu.getNonActionItems().size() != this.mMaxItems || childIndex >= this.mMaxItems) ? childIndex < this.mMaxItems + -1 : true;
        if (!fits || item.isActionButton()) {
            return false;
        }
        return true;
    }

    protected void addItemView(View itemView, int childIndex) {
        IconMenuItemView v = (IconMenuItemView) itemView;
        IconMenuView parent = this.mMenuView;
        v.setIconMenuView(parent);
        v.setItemInvoker(parent);
        v.setBackgroundDrawable(parent.getItemBackgroundDrawable());
        super.addItemView(itemView, childIndex);
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        MenuDialogHelper helper = new MenuDialogHelper(subMenu);
        helper.setPresenterCallback(this.mSubMenuPresenterCallback);
        helper.show(null);
        this.mOpenSubMenu = helper;
        this.mOpenSubMenuId = subMenu.getItem().getItemId();
        super.onSubMenuSelected(subMenu);
        return true;
    }

    public void updateMenuView(boolean cleared) {
        int i;
        ViewParent menuView = this.mMenuView;
        if (this.mMaxItems < 0) {
            this.mMaxItems = menuView.getMaxItems();
        }
        ArrayList<MenuItemImpl> itemsToShow = this.mMenu.getNonActionItems();
        boolean needsMore = itemsToShow.size() > this.mMaxItems;
        super.updateMenuView(cleared);
        if (needsMore && (this.mMoreView == null || this.mMoreView.getParent() != menuView)) {
            if (this.mMoreView == null) {
                this.mMoreView = menuView.createMoreItemView();
                this.mMoreView.setBackgroundDrawable(menuView.getItemBackgroundDrawable());
            }
            menuView.addView(this.mMoreView);
        } else if (!(needsMore || this.mMoreView == null)) {
            menuView.removeView(this.mMoreView);
        }
        if (needsMore) {
            i = this.mMaxItems - 1;
        } else {
            i = itemsToShow.size();
        }
        menuView.setNumActualItemsShown(i);
    }

    protected boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        if (parent.getChildAt(childIndex) != this.mMoreView) {
            return super.filterLeftoverView(parent, childIndex);
        }
        return false;
    }

    public int getNumActualItemsShown() {
        return ((IconMenuView) this.mMenuView).getNumActualItemsShown();
    }

    public void saveHierarchyState(Bundle outState) {
        SparseArray<Parcelable> viewStates = new SparseArray();
        if (this.mMenuView != null) {
            ((View) this.mMenuView).saveHierarchyState(viewStates);
        }
        outState.putSparseParcelableArray(VIEWS_TAG, viewStates);
    }

    public void restoreHierarchyState(Bundle inState) {
        SparseArray<Parcelable> viewStates = inState.getSparseParcelableArray(VIEWS_TAG);
        if (viewStates != null) {
            ((View) this.mMenuView).restoreHierarchyState(viewStates);
        }
        int subMenuId = inState.getInt(OPEN_SUBMENU_KEY, 0);
        if (subMenuId > 0 && this.mMenu != null) {
            MenuItem item = this.mMenu.findItem(subMenuId);
            if (item != null) {
                onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
            }
        }
    }

    public Parcelable onSaveInstanceState() {
        if (this.mMenuView == null) {
            return null;
        }
        Bundle state = new Bundle();
        saveHierarchyState(state);
        if (this.mOpenSubMenuId > 0) {
            state.putInt(OPEN_SUBMENU_KEY, this.mOpenSubMenuId);
        }
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        restoreHierarchyState((Bundle) state);
    }
}
