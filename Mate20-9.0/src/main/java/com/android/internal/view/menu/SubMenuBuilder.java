package com.android.internal.view.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import com.android.internal.view.menu.MenuBuilder;

public class SubMenuBuilder extends MenuBuilder implements SubMenu {
    private MenuItemImpl mItem;
    private MenuBuilder mParentMenu;

    public SubMenuBuilder(Context context, MenuBuilder parentMenu, MenuItemImpl item) {
        super(context);
        this.mParentMenu = parentMenu;
        this.mItem = item;
    }

    public void setQwertyMode(boolean isQwerty) {
        this.mParentMenu.setQwertyMode(isQwerty);
    }

    public boolean isQwertyMode() {
        return this.mParentMenu.isQwertyMode();
    }

    public void setShortcutsVisible(boolean shortcutsVisible) {
        this.mParentMenu.setShortcutsVisible(shortcutsVisible);
    }

    public boolean isShortcutsVisible() {
        return this.mParentMenu.isShortcutsVisible();
    }

    public Menu getParentMenu() {
        return this.mParentMenu;
    }

    public MenuItem getItem() {
        return this.mItem;
    }

    public void setCallback(MenuBuilder.Callback callback) {
        this.mParentMenu.setCallback(callback);
    }

    public MenuBuilder getRootMenu() {
        return this.mParentMenu.getRootMenu();
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchMenuItemSelected(MenuBuilder menu, MenuItem item) {
        return super.dispatchMenuItemSelected(menu, item) || this.mParentMenu.dispatchMenuItemSelected(menu, item);
    }

    public SubMenu setIcon(Drawable icon) {
        this.mItem.setIcon(icon);
        return this;
    }

    public SubMenu setIcon(int iconRes) {
        this.mItem.setIcon(iconRes);
        return this;
    }

    public SubMenu setHeaderIcon(Drawable icon) {
        return (SubMenu) super.setHeaderIconInt(icon);
    }

    public SubMenu setHeaderIcon(int iconRes) {
        return (SubMenu) super.setHeaderIconInt(iconRes);
    }

    public SubMenu setHeaderTitle(CharSequence title) {
        return (SubMenu) super.setHeaderTitleInt(title);
    }

    public SubMenu setHeaderTitle(int titleRes) {
        return (SubMenu) super.setHeaderTitleInt(titleRes);
    }

    public SubMenu setHeaderView(View view) {
        return (SubMenu) super.setHeaderViewInt(view);
    }

    public boolean expandItemActionView(MenuItemImpl item) {
        return this.mParentMenu.expandItemActionView(item);
    }

    public boolean collapseItemActionView(MenuItemImpl item) {
        return this.mParentMenu.collapseItemActionView(item);
    }

    public String getActionViewStatesKey() {
        int itemId = this.mItem != null ? this.mItem.getItemId() : 0;
        if (itemId == 0) {
            return null;
        }
        return super.getActionViewStatesKey() + ":" + itemId;
    }

    public void setGroupDividerEnabled(boolean groupDividerEnabled) {
        this.mParentMenu.setGroupDividerEnabled(groupDividerEnabled);
    }

    public boolean isGroupDividerEnabled() {
        return this.mParentMenu.isGroupDividerEnabled();
    }
}
