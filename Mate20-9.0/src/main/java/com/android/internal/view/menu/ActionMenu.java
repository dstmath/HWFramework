package com.android.internal.view.menu;

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

    public ActionMenu(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public MenuItem add(CharSequence title) {
        return add(0, 0, 0, title);
    }

    public MenuItem add(int titleRes) {
        return add(0, 0, 0, titleRes);
    }

    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return add(groupId, itemId, order, (CharSequence) this.mContext.getResources().getString(titleRes));
    }

    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        ActionMenuItem item = new ActionMenuItem(getContext(), groupId, itemId, 0, order, title);
        this.mItems.add(order, item);
        return item;
    }

    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        ActionMenu actionMenu = this;
        Intent[] intentArr = specifics;
        PackageManager pm = actionMenu.mContext.getPackageManager();
        int i = 0;
        Intent intent2 = intent;
        List<ResolveInfo> lri = pm.queryIntentActivityOptions(caller, intentArr, intent2, 0);
        int N = lri != null ? lri.size() : 0;
        if ((flags & 1) == 0) {
            removeGroup(groupId);
        }
        while (i < N) {
            ResolveInfo ri = lri.get(i);
            Intent rintent = new Intent(ri.specificIndex < 0 ? intent2 : intentArr[ri.specificIndex]);
            rintent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name));
            MenuItem item = actionMenu.add(groupId, itemId, order, ri.loadLabel(pm)).setIcon(ri.loadIcon(pm)).setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
            i++;
            actionMenu = this;
        }
        int i2 = groupId;
        int i3 = itemId;
        int i4 = order;
        return N;
    }

    public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    public void clear() {
        this.mItems.clear();
    }

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

    public MenuItem findItem(int id) {
        return this.mItems.get(findItemIndex(id));
    }

    public MenuItem getItem(int index) {
        return this.mItems.get(index);
    }

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
            boolean is_modifiers_exact_match = (modifierState & 69647) == (69647 & (qwerty ? item.getAlphabeticModifiers() : item.getNumericModifiers()));
            if (keyCode == shortcut && is_modifiers_exact_match) {
                return item;
            }
        }
        return null;
    }

    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcut(keyCode, event) != null;
    }

    public boolean performIdentifierAction(int id, int flags) {
        int index = findItemIndex(id);
        if (index < 0) {
            return false;
        }
        return this.mItems.get(index).invoke();
    }

    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        ActionMenuItem item = findItemWithShortcut(keyCode, event);
        if (item == null) {
            return false;
        }
        return item.invoke();
    }

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

    public void removeItem(int id) {
        this.mItems.remove(findItemIndex(id));
    }

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

    public void setQwertyMode(boolean isQwerty) {
        this.mIsQwerty = isQwerty;
    }

    public int size() {
        return this.mItems.size();
    }
}
