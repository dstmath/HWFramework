package com.android.internal.view.menu;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;

public class ActionMenuItem implements MenuItem {
    private static final int CHECKABLE = 1;
    private static final int CHECKED = 2;
    private static final int ENABLED = 16;
    private static final int EXCLUSIVE = 4;
    private static final int HIDDEN = 8;
    private static final int NO_ICON = 0;
    private final int mCategoryOrder;
    private OnMenuItemClickListener mClickListener;
    private CharSequence mContentDescription;
    private Context mContext;
    private int mFlags = 16;
    private final int mGroup;
    private boolean mHasIconTint = false;
    private boolean mHasIconTintMode = false;
    private Drawable mIconDrawable;
    private int mIconResId = 0;
    private ColorStateList mIconTintList = null;
    private Mode mIconTintMode = null;
    private final int mId;
    private Intent mIntent;
    private final int mOrdering;
    private char mShortcutAlphabeticChar;
    private int mShortcutAlphabeticModifiers = 4096;
    private char mShortcutNumericChar;
    private int mShortcutNumericModifiers = 4096;
    private CharSequence mTitle;
    private CharSequence mTitleCondensed;
    private CharSequence mTooltipText;

    public ActionMenuItem(Context context, int group, int id, int categoryOrder, int ordering, CharSequence title) {
        this.mContext = context;
        this.mId = id;
        this.mGroup = group;
        this.mCategoryOrder = categoryOrder;
        this.mOrdering = ordering;
        this.mTitle = title;
    }

    public char getAlphabeticShortcut() {
        return this.mShortcutAlphabeticChar;
    }

    public int getAlphabeticModifiers() {
        return this.mShortcutAlphabeticModifiers;
    }

    public int getGroupId() {
        return this.mGroup;
    }

    public Drawable getIcon() {
        return this.mIconDrawable;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public int getItemId() {
        return this.mId;
    }

    public ContextMenuInfo getMenuInfo() {
        return null;
    }

    public char getNumericShortcut() {
        return this.mShortcutNumericChar;
    }

    public int getNumericModifiers() {
        return this.mShortcutNumericModifiers;
    }

    public int getOrder() {
        return this.mOrdering;
    }

    public SubMenu getSubMenu() {
        return null;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getTitleCondensed() {
        return this.mTitleCondensed != null ? this.mTitleCondensed : this.mTitle;
    }

    public boolean hasSubMenu() {
        return false;
    }

    public boolean isCheckable() {
        return (this.mFlags & 1) != 0;
    }

    public boolean isChecked() {
        return (this.mFlags & 2) != 0;
    }

    public boolean isEnabled() {
        return (this.mFlags & 16) != 0;
    }

    public boolean isVisible() {
        return (this.mFlags & 8) == 0;
    }

    public MenuItem setAlphabeticShortcut(char alphaChar) {
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        return this;
    }

    public MenuItem setAlphabeticShortcut(char alphachar, int alphaModifiers) {
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphachar);
        this.mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers);
        return this;
    }

    public MenuItem setCheckable(boolean checkable) {
        this.mFlags = (checkable ? 1 : 0) | (this.mFlags & -2);
        return this;
    }

    public ActionMenuItem setExclusiveCheckable(boolean exclusive) {
        this.mFlags = (exclusive ? 4 : 0) | (this.mFlags & -5);
        return this;
    }

    public MenuItem setChecked(boolean checked) {
        this.mFlags = (checked ? 2 : 0) | (this.mFlags & -3);
        return this;
    }

    public MenuItem setEnabled(boolean enabled) {
        this.mFlags = (enabled ? 16 : 0) | (this.mFlags & -17);
        return this;
    }

    public MenuItem setIcon(Drawable icon) {
        this.mIconDrawable = icon;
        this.mIconResId = 0;
        applyIconTint();
        return this;
    }

    public MenuItem setIcon(int iconRes) {
        this.mIconResId = iconRes;
        this.mIconDrawable = this.mContext.getDrawable(iconRes);
        applyIconTint();
        return this;
    }

    public MenuItem setIconTintList(ColorStateList iconTintList) {
        this.mIconTintList = iconTintList;
        this.mHasIconTint = true;
        applyIconTint();
        return this;
    }

    public ColorStateList getIconTintList() {
        return this.mIconTintList;
    }

    public MenuItem setIconTintMode(Mode iconTintMode) {
        this.mIconTintMode = iconTintMode;
        this.mHasIconTintMode = true;
        applyIconTint();
        return this;
    }

    public Mode getIconTintMode() {
        return this.mIconTintMode;
    }

    private void applyIconTint() {
        if (this.mIconDrawable == null) {
            return;
        }
        if (this.mHasIconTint || this.mHasIconTintMode) {
            this.mIconDrawable = this.mIconDrawable.mutate();
            if (this.mHasIconTint) {
                this.mIconDrawable.setTintList(this.mIconTintList);
            }
            if (this.mHasIconTintMode) {
                this.mIconDrawable.setTintMode(this.mIconTintMode);
            }
        }
    }

    public MenuItem setIntent(Intent intent) {
        this.mIntent = intent;
        return this;
    }

    public MenuItem setNumericShortcut(char numericChar) {
        this.mShortcutNumericChar = numericChar;
        return this;
    }

    public MenuItem setNumericShortcut(char numericChar, int numericModifiers) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers);
        return this;
    }

    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        this.mClickListener = menuItemClickListener;
        return this;
    }

    public MenuItem setShortcut(char numericChar, char alphaChar) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        return this;
    }

    public MenuItem setShortcut(char numericChar, char alphaChar, int numericModifiers, int alphaModifiers) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers);
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        this.mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers);
        return this;
    }

    public MenuItem setTitle(CharSequence title) {
        this.mTitle = title;
        return this;
    }

    public MenuItem setTitle(int title) {
        this.mTitle = this.mContext.getResources().getString(title);
        return this;
    }

    public MenuItem setTitleCondensed(CharSequence title) {
        this.mTitleCondensed = title;
        return this;
    }

    public MenuItem setVisible(boolean visible) {
        this.mFlags = (visible ? 0 : 8) | (this.mFlags & 8);
        return this;
    }

    public boolean invoke() {
        if (this.mClickListener != null && this.mClickListener.onMenuItemClick(this)) {
            return true;
        }
        if (this.mIntent == null) {
            return false;
        }
        this.mContext.startActivity(this.mIntent);
        return true;
    }

    public void setShowAsAction(int show) {
    }

    public MenuItem setActionView(View actionView) {
        throw new UnsupportedOperationException();
    }

    public View getActionView() {
        return null;
    }

    public MenuItem setActionView(int resId) {
        throw new UnsupportedOperationException();
    }

    public ActionProvider getActionProvider() {
        return null;
    }

    public MenuItem setActionProvider(ActionProvider actionProvider) {
        throw new UnsupportedOperationException();
    }

    public MenuItem setShowAsActionFlags(int actionEnum) {
        setShowAsAction(actionEnum);
        return this;
    }

    public boolean expandActionView() {
        return false;
    }

    public boolean collapseActionView() {
        return false;
    }

    public boolean isActionViewExpanded() {
        return false;
    }

    public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
        return this;
    }

    public MenuItem setContentDescription(CharSequence contentDescription) {
        this.mContentDescription = contentDescription;
        return this;
    }

    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    public MenuItem setTooltipText(CharSequence tooltipText) {
        this.mTooltipText = tooltipText;
        return this;
    }

    public CharSequence getTooltipText() {
        return this.mTooltipText;
    }
}
