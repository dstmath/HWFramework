package android.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu.ContextMenuInfo;

public interface MenuItem {
    public static final int SHOW_AS_ACTION_ALWAYS = 2;
    public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;
    public static final int SHOW_AS_ACTION_IF_ROOM = 1;
    public static final int SHOW_AS_ACTION_NEVER = 0;
    public static final int SHOW_AS_ACTION_WITH_TEXT = 4;

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem menuItem);
    }

    public interface OnActionExpandListener {
        boolean onMenuItemActionCollapse(MenuItem menuItem);

        boolean onMenuItemActionExpand(MenuItem menuItem);
    }

    boolean collapseActionView();

    boolean expandActionView();

    ActionProvider getActionProvider();

    View getActionView();

    char getAlphabeticShortcut();

    int getGroupId();

    Drawable getIcon();

    Intent getIntent();

    int getItemId();

    ContextMenuInfo getMenuInfo();

    char getNumericShortcut();

    int getOrder();

    SubMenu getSubMenu();

    CharSequence getTitle();

    CharSequence getTitleCondensed();

    boolean hasSubMenu();

    boolean isActionViewExpanded();

    boolean isCheckable();

    boolean isChecked();

    boolean isEnabled();

    boolean isVisible();

    MenuItem setActionProvider(ActionProvider actionProvider);

    MenuItem setActionView(int i);

    MenuItem setActionView(View view);

    MenuItem setAlphabeticShortcut(char c);

    MenuItem setCheckable(boolean z);

    MenuItem setChecked(boolean z);

    MenuItem setEnabled(boolean z);

    MenuItem setIcon(int i);

    MenuItem setIcon(Drawable drawable);

    MenuItem setIntent(Intent intent);

    MenuItem setNumericShortcut(char c);

    MenuItem setOnActionExpandListener(OnActionExpandListener onActionExpandListener);

    MenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener);

    MenuItem setShortcut(char c, char c2);

    void setShowAsAction(int i);

    MenuItem setShowAsActionFlags(int i);

    MenuItem setTitle(int i);

    MenuItem setTitle(CharSequence charSequence);

    MenuItem setTitleCondensed(CharSequence charSequence);

    MenuItem setVisible(boolean z);
}
