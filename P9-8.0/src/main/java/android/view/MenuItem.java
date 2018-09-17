package android.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu.ContextMenuInfo;

public interface MenuItem {
    public static final int SHOW_AS_ACTION_ALWAYS = 2;
    public static final int SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW = 8;
    public static final int SHOW_AS_ACTION_IF_ROOM = 1;
    public static final int SHOW_AS_ACTION_NEVER = 0;
    public static final int SHOW_AS_ACTION_WITH_TEXT = 4;
    public static final int SHOW_AS_OVERFLOW_ALWAYS = Integer.MIN_VALUE;

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

    MenuItem setIconTintList(ColorStateList tint) {
        return this;
    }

    ColorStateList getIconTintList() {
        return null;
    }

    MenuItem setIconTintMode(Mode tintMode) {
        return this;
    }

    Mode getIconTintMode() {
        return null;
    }

    MenuItem setShortcut(char numericChar, char alphaChar, int numericModifiers, int alphaModifiers) {
        if ((alphaModifiers & Menu.SUPPORTED_MODIFIERS_MASK) == 4096 && (numericModifiers & Menu.SUPPORTED_MODIFIERS_MASK) == 4096) {
            return setShortcut(numericChar, alphaChar);
        }
        return this;
    }

    MenuItem setNumericShortcut(char numericChar, int numericModifiers) {
        if ((Menu.SUPPORTED_MODIFIERS_MASK & numericModifiers) == 4096) {
            return setNumericShortcut(numericChar);
        }
        return this;
    }

    int getNumericModifiers() {
        return 4096;
    }

    MenuItem setAlphabeticShortcut(char alphaChar, int alphaModifiers) {
        if ((Menu.SUPPORTED_MODIFIERS_MASK & alphaModifiers) == 4096) {
            return setAlphabeticShortcut(alphaChar);
        }
        return this;
    }

    int getAlphabeticModifiers() {
        return 4096;
    }

    MenuItem setContentDescription(CharSequence contentDescription) {
        return this;
    }

    CharSequence getContentDescription() {
        return null;
    }

    MenuItem setTooltipText(CharSequence tooltipText) {
        return this;
    }

    CharSequence getTooltipText() {
        return null;
    }

    boolean requiresOverflow() {
        return false;
    }
}
