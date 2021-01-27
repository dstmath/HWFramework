package com.android.internal.view.menu;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
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
    private MenuItem.OnMenuItemClickListener mClickListener;
    private CharSequence mContentDescription;
    private Context mContext;
    private int mFlags = 16;
    private final int mGroup;
    private boolean mHasIconTint = false;
    private boolean mHasIconTintMode = false;
    private Drawable mIconDrawable;
    private int mIconResId = 0;
    private ColorStateList mIconTintList = null;
    private PorterDuff.Mode mIconTintMode = null;
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

    @UnsupportedAppUsage
    public ActionMenuItem(Context context, int group, int id, int categoryOrder, int ordering, CharSequence title) {
        this.mContext = context;
        this.mId = id;
        this.mGroup = group;
        this.mCategoryOrder = categoryOrder;
        this.mOrdering = ordering;
        this.mTitle = title;
    }

    @Override // android.view.MenuItem
    public char getAlphabeticShortcut() {
        return this.mShortcutAlphabeticChar;
    }

    @Override // android.view.MenuItem
    public int getAlphabeticModifiers() {
        return this.mShortcutAlphabeticModifiers;
    }

    @Override // android.view.MenuItem
    public int getGroupId() {
        return this.mGroup;
    }

    @Override // android.view.MenuItem
    public Drawable getIcon() {
        return this.mIconDrawable;
    }

    @Override // android.view.MenuItem
    public Intent getIntent() {
        return this.mIntent;
    }

    @Override // android.view.MenuItem
    public int getItemId() {
        return this.mId;
    }

    @Override // android.view.MenuItem
    public ContextMenu.ContextMenuInfo getMenuInfo() {
        return null;
    }

    @Override // android.view.MenuItem
    public char getNumericShortcut() {
        return this.mShortcutNumericChar;
    }

    @Override // android.view.MenuItem
    public int getNumericModifiers() {
        return this.mShortcutNumericModifiers;
    }

    @Override // android.view.MenuItem
    public int getOrder() {
        return this.mOrdering;
    }

    @Override // android.view.MenuItem
    public SubMenu getSubMenu() {
        return null;
    }

    @Override // android.view.MenuItem
    public CharSequence getTitle() {
        return this.mTitle;
    }

    @Override // android.view.MenuItem
    public CharSequence getTitleCondensed() {
        CharSequence charSequence = this.mTitleCondensed;
        return charSequence != null ? charSequence : this.mTitle;
    }

    @Override // android.view.MenuItem
    public boolean hasSubMenu() {
        return false;
    }

    @Override // android.view.MenuItem
    public boolean isCheckable() {
        return (this.mFlags & 1) != 0;
    }

    @Override // android.view.MenuItem
    public boolean isChecked() {
        return (this.mFlags & 2) != 0;
    }

    @Override // android.view.MenuItem
    public boolean isEnabled() {
        return (this.mFlags & 16) != 0;
    }

    @Override // android.view.MenuItem
    public boolean isVisible() {
        return (this.mFlags & 8) == 0;
    }

    @Override // android.view.MenuItem
    public MenuItem setAlphabeticShortcut(char alphaChar) {
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setAlphabeticShortcut(char alphachar, int alphaModifiers) {
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphachar);
        this.mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setCheckable(boolean checkable) {
        this.mFlags = (this.mFlags & -2) | (checkable ? 1 : 0);
        return this;
    }

    public ActionMenuItem setExclusiveCheckable(boolean exclusive) {
        this.mFlags = (this.mFlags & -5) | (exclusive ? 4 : 0);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setChecked(boolean checked) {
        this.mFlags = (this.mFlags & -3) | (checked ? 2 : 0);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setEnabled(boolean enabled) {
        this.mFlags = (this.mFlags & -17) | (enabled ? 16 : 0);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setIcon(Drawable icon) {
        this.mIconDrawable = icon;
        this.mIconResId = 0;
        applyIconTint();
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setIcon(int iconRes) {
        this.mIconResId = iconRes;
        this.mIconDrawable = this.mContext.getDrawable(iconRes);
        applyIconTint();
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setIconTintList(ColorStateList iconTintList) {
        this.mIconTintList = iconTintList;
        this.mHasIconTint = true;
        applyIconTint();
        return this;
    }

    @Override // android.view.MenuItem
    public ColorStateList getIconTintList() {
        return this.mIconTintList;
    }

    @Override // android.view.MenuItem
    public MenuItem setIconTintMode(PorterDuff.Mode iconTintMode) {
        this.mIconTintMode = iconTintMode;
        this.mHasIconTintMode = true;
        applyIconTint();
        return this;
    }

    @Override // android.view.MenuItem
    public PorterDuff.Mode getIconTintMode() {
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

    @Override // android.view.MenuItem
    public MenuItem setIntent(Intent intent) {
        this.mIntent = intent;
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setNumericShortcut(char numericChar) {
        this.mShortcutNumericChar = numericChar;
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setNumericShortcut(char numericChar, int numericModifiers) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener menuItemClickListener) {
        this.mClickListener = menuItemClickListener;
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setShortcut(char numericChar, char alphaChar) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setShortcut(char numericChar, char alphaChar, int numericModifiers, int alphaModifiers) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutNumericModifiers = KeyEvent.normalizeMetaState(numericModifiers);
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        this.mShortcutAlphabeticModifiers = KeyEvent.normalizeMetaState(alphaModifiers);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setTitle(CharSequence title) {
        this.mTitle = title;
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setTitle(int title) {
        this.mTitle = this.mContext.getResources().getString(title);
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setTitleCondensed(CharSequence title) {
        this.mTitleCondensed = title;
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setVisible(boolean visible) {
        int i = 8;
        int i2 = this.mFlags & 8;
        if (visible) {
            i = 0;
        }
        this.mFlags = i2 | i;
        return this;
    }

    public boolean invoke() {
        MenuItem.OnMenuItemClickListener onMenuItemClickListener = this.mClickListener;
        if (onMenuItemClickListener != null && onMenuItemClickListener.onMenuItemClick(this)) {
            return true;
        }
        Intent intent = this.mIntent;
        if (intent == null) {
            return false;
        }
        this.mContext.startActivity(intent);
        return true;
    }

    @Override // android.view.MenuItem
    public void setShowAsAction(int show) {
    }

    @Override // android.view.MenuItem
    public MenuItem setActionView(View actionView) {
        throw new UnsupportedOperationException();
    }

    @Override // android.view.MenuItem
    public View getActionView() {
        return null;
    }

    @Override // android.view.MenuItem
    public MenuItem setActionView(int resId) {
        throw new UnsupportedOperationException();
    }

    @Override // android.view.MenuItem
    public ActionProvider getActionProvider() {
        return null;
    }

    @Override // android.view.MenuItem
    public MenuItem setActionProvider(ActionProvider actionProvider) {
        throw new UnsupportedOperationException();
    }

    @Override // android.view.MenuItem
    public MenuItem setShowAsActionFlags(int actionEnum) {
        setShowAsAction(actionEnum);
        return this;
    }

    @Override // android.view.MenuItem
    public boolean expandActionView() {
        return false;
    }

    @Override // android.view.MenuItem
    public boolean collapseActionView() {
        return false;
    }

    @Override // android.view.MenuItem
    public boolean isActionViewExpanded() {
        return false;
    }

    @Override // android.view.MenuItem
    public MenuItem setOnActionExpandListener(MenuItem.OnActionExpandListener listener) {
        return this;
    }

    @Override // android.view.MenuItem
    public MenuItem setContentDescription(CharSequence contentDescription) {
        this.mContentDescription = contentDescription;
        return this;
    }

    @Override // android.view.MenuItem
    public CharSequence getContentDescription() {
        return this.mContentDescription;
    }

    @Override // android.view.MenuItem
    public MenuItem setTooltipText(CharSequence tooltipText) {
        this.mTooltipText = tooltipText;
        return this;
    }

    @Override // android.view.MenuItem
    public CharSequence getTooltipText() {
        return this.mTooltipText;
    }
}
