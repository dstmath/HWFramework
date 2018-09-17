package android.view;

import android.content.ComponentName;
import android.content.Intent;

public interface Menu {
    public static final int CATEGORY_ALTERNATIVE = 262144;
    public static final int CATEGORY_CONTAINER = 65536;
    public static final int CATEGORY_MASK = -65536;
    public static final int CATEGORY_SECONDARY = 196608;
    public static final int CATEGORY_SHIFT = 16;
    public static final int CATEGORY_SYSTEM = 131072;
    public static final int FIRST = 1;
    public static final int FLAG_ALWAYS_PERFORM_CLOSE = 2;
    public static final int FLAG_APPEND_TO_GROUP = 1;
    public static final int FLAG_PERFORM_NO_CLOSE = 1;
    public static final int NONE = 0;
    public static final int USER_MASK = 65535;
    public static final int USER_SHIFT = 0;

    MenuItem add(int i);

    MenuItem add(int i, int i2, int i3, int i4);

    MenuItem add(int i, int i2, int i3, CharSequence charSequence);

    MenuItem add(CharSequence charSequence);

    int addIntentOptions(int i, int i2, int i3, ComponentName componentName, Intent[] intentArr, Intent intent, int i4, MenuItem[] menuItemArr);

    SubMenu addSubMenu(int i);

    SubMenu addSubMenu(int i, int i2, int i3, int i4);

    SubMenu addSubMenu(int i, int i2, int i3, CharSequence charSequence);

    SubMenu addSubMenu(CharSequence charSequence);

    void clear();

    void close();

    MenuItem findItem(int i);

    MenuItem getItem(int i);

    boolean hasVisibleItems();

    boolean isShortcutKey(int i, KeyEvent keyEvent);

    boolean performIdentifierAction(int i, int i2);

    boolean performShortcut(int i, KeyEvent keyEvent, int i2);

    void removeGroup(int i);

    void removeItem(int i);

    void setGroupCheckable(int i, boolean z, boolean z2);

    void setGroupEnabled(int i, boolean z);

    void setGroupVisible(int i, boolean z);

    void setQwertyMode(boolean z);

    int size();
}
