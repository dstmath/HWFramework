package com.android.internal.view.menu;

import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import java.util.ArrayList;
import java.util.List;

public class ActionMenu implements Menu {
    private Context mContext;
    private boolean mIsQwerty;
    private ArrayList<ActionMenuItem> mItems = new ArrayList<>();

    @UnsupportedAppUsage
    public ActionMenu(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    @Override // android.view.Menu
    public MenuItem add(CharSequence title) {
        return add(0, 0, 0, title);
    }

    @Override // android.view.Menu
    public MenuItem add(int titleRes) {
        return add(0, 0, 0, titleRes);
    }

    @Override // android.view.Menu
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return add(groupId, itemId, order, this.mContext.getResources().getString(titleRes));
    }

    @Override // android.view.Menu
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        ActionMenuItem item = new ActionMenuItem(getContext(), groupId, itemId, 0, order, title);
        this.mItems.add(order, item);
        return item;
    }

    @Override // android.view.Menu
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        PackageManager pm = this.mContext.getPackageManager();
        int N = 0;
        List<ResolveInfo> lri = pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        if (lri != null) {
            N = lri.size();
        }
        if ((flags & 1) == 0) {
            removeGroup(groupId);
        }
        for (int i = 0; i < N; i++) {
            ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(ri.specificIndex < 0 ? intent : specifics[ri.specificIndex]);
            rintent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name));
            MenuItem item = add(groupId, itemId, order, ri.loadLabel(pm)).setIcon(ri.loadIcon(pm)).setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }
        return N;
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    @Override // android.view.Menu
    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    @Override // android.view.Menu
    public void clear() {
        this.mItems.clear();
    }

    @Override // android.view.Menu
    public void close() {
    }

    private int findItemIndex(int id) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            if (items.get(i).getItemId() == id) {
                return i;
            }
        }
        return -1;
    }

    @Override // android.view.Menu
    public MenuItem findItem(int id) {
        return this.mItems.get(findItemIndex(id));
    }

    @Override // android.view.Menu
    public MenuItem getItem(int index) {
        return this.mItems.get(index);
    }

    @Override // android.view.Menu
    public boolean hasVisibleItems() {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            if (items.get(i).isVisible()) {
                return true;
            }
        }
        return false;
    }

    private ActionMenuItem findItemWithShortcut(int keyCode, KeyEvent event) {
        char shortcut;
        boolean qwerty = this.mIsQwerty;
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        int modifierState = event.getModifiers();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (qwerty) {
                shortcut = item.getAlphabeticShortcut();
            } else {
                shortcut = item.getNumericShortcut();
            }
            boolean is_modifiers_exact_match = (modifierState & Menu.SUPPORTED_MODIFIERS_MASK) == (69647 & (qwerty ? item.getAlphabeticModifiers() : item.getNumericModifiers()));
            if (keyCode == shortcut && is_modifiers_exact_match) {
                return item;
            }
        }
        return null;
    }

    @Override // android.view.Menu
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcut(keyCode, event) != null;
    }

    @Override // android.view.Menu
    public boolean performIdentifierAction(int id, int flags) {
        int index = findItemIndex(id);
        if (index < 0) {
            return false;
        }
        return this.mItems.get(index).invoke();
    }

    @Override // android.view.Menu
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        ActionMenuItem item = findItemWithShortcut(keyCode, event);
        if (item == null) {
            return false;
        }
        return item.invoke();
    }

    @Override // android.view.Menu
    public void removeGroup(int groupId) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        int i = 0;
        while (i < itemCount) {
            if (items.get(i).getGroupId() == groupId) {
                items.remove(i);
                itemCount--;
            } else {
                i++;
            }
        }
    }

    @Override // android.view.Menu
    public void removeItem(int id) {
        this.mItems.remove(findItemIndex(id));
    }

    @Override // android.view.Menu
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (item.getGroupId() == group) {
                item.setCheckable(checkable);
                item.setExclusiveCheckable(exclusive);
            }
        }
    }

    @Override // android.view.Menu
    public void setGroupEnabled(int group, boolean enabled) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }

    @Override // android.view.Menu
    public void setGroupVisible(int group, boolean visible) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = items.get(i);
            if (item.getGroupId() == group) {
                item.setVisible(visible);
            }
        }
    }

    @Override // android.view.Menu
    public void setQwertyMode(boolean isQwerty) {
        this.mIsQwerty = isQwerty;
    }

    @Override // android.view.Menu
    public int size() {
        return this.mItems.size();
    }
}
