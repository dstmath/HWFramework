package com.android.internal.view.menu;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ActionProvider.VisibilityListener;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewDebug.CapturedViewProperty;
import android.widget.LinearLayout;
import com.android.internal.R;
import com.android.internal.view.menu.MenuView.ItemView;
import com.huawei.pgmng.plug.PGSdk;

public final class MenuItemImpl implements MenuItem {
    private static final int CHECKABLE = 1;
    private static final int CHECKED = 2;
    private static final int DOWNLOADING = 1;
    private static final int DOWNLOAD_PAUSE = 2;
    private static final int DOWNLOAD_READY = 0;
    private static final int ENABLED = 16;
    private static final int EXCLUSIVE = 4;
    private static final int HIDDEN = 8;
    private static final int IS_ACTION = 32;
    static final int NO_ICON = 0;
    private static final int SHOW_AS_ACTION_MASK = 3;
    private static final String TAG = "MenuItemImpl";
    private static String sDeleteShortcutLabel;
    private static String sEnterShortcutLabel;
    private static String sLanguage;
    private static String sPrependShortcutLabel;
    private static String sSpaceShortcutLabel;
    private ActionProvider mActionProvider;
    private View mActionView;
    private final int mCategoryOrder;
    private OnMenuItemClickListener mClickListener;
    private int mFlags;
    private final int mGroup;
    private Drawable mIconDrawable;
    private int mIconResId;
    private final int mId;
    private Intent mIntent;
    private boolean mIsActionViewExpanded;
    private Runnable mItemCallback;
    private MenuBuilder mMenu;
    private ContextMenuInfo mMenuInfo;
    private ProgressDrawable mMenuProgressDrawable;
    private OnActionExpandListener mOnActionExpandListener;
    private final int mOrdering;
    private LayerDrawable mProgressDrawable;
    private char mShortcutAlphabeticChar;
    private char mShortcutNumericChar;
    private int mShowAsAction;
    private SubMenuBuilder mSubMenu;
    private CharSequence mTitle;
    private CharSequence mTitleCondensed;

    MenuItemImpl(MenuBuilder menu, int group, int id, int categoryOrder, int ordering, CharSequence title, int showAsAction) {
        this.mIconResId = NO_ICON;
        this.mFlags = ENABLED;
        this.mShowAsAction = NO_ICON;
        this.mIsActionViewExpanded = false;
        String lang = menu.getContext().getResources().getConfiguration().locale.toString();
        if (sPrependShortcutLabel == null || !lang.equals(sLanguage)) {
            sLanguage = lang;
            sPrependShortcutLabel = menu.getContext().getResources().getString(R.string.prepend_shortcut_label);
            sEnterShortcutLabel = menu.getContext().getResources().getString(R.string.menu_enter_shortcut_label);
            sDeleteShortcutLabel = menu.getContext().getResources().getString(R.string.menu_delete_shortcut_label);
            sSpaceShortcutLabel = menu.getContext().getResources().getString(R.string.menu_space_shortcut_label);
        }
        this.mMenu = menu;
        this.mId = id;
        this.mGroup = group;
        this.mCategoryOrder = categoryOrder;
        this.mOrdering = ordering;
        this.mTitle = title;
        this.mShowAsAction = showAsAction;
    }

    public boolean invoke() {
        if ((this.mClickListener != null && this.mClickListener.onMenuItemClick(this)) || this.mMenu.dispatchMenuItemSelected(this.mMenu, this)) {
            return true;
        }
        if (this.mItemCallback != null) {
            this.mItemCallback.run();
            return true;
        }
        if (this.mIntent != null) {
            try {
                this.mMenu.getContext().startActivity(this.mIntent);
                return true;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Can't find activity to handle intent; ignoring", e);
            }
        }
        if (this.mActionProvider == null || !this.mActionProvider.onPerformDefaultAction()) {
            return false;
        }
        return true;
    }

    public boolean isEnabled() {
        return (this.mFlags & ENABLED) != 0;
    }

    public MenuItem setEnabled(boolean enabled) {
        if (enabled) {
            this.mFlags |= ENABLED;
        } else {
            this.mFlags &= -17;
        }
        this.mMenu.onItemsChanged(false);
        return this;
    }

    public int getGroupId() {
        return this.mGroup;
    }

    @CapturedViewProperty
    public int getItemId() {
        return this.mId;
    }

    public int getOrder() {
        return this.mCategoryOrder;
    }

    public int getOrdering() {
        return this.mOrdering;
    }

    public Intent getIntent() {
        return this.mIntent;
    }

    public MenuItem setIntent(Intent intent) {
        this.mIntent = intent;
        return this;
    }

    Runnable getCallback() {
        return this.mItemCallback;
    }

    public MenuItem setCallback(Runnable callback) {
        this.mItemCallback = callback;
        return this;
    }

    public char getAlphabeticShortcut() {
        return this.mShortcutAlphabeticChar;
    }

    public MenuItem setAlphabeticShortcut(char alphaChar) {
        if (this.mShortcutAlphabeticChar == alphaChar) {
            return this;
        }
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        this.mMenu.onItemsChanged(false);
        return this;
    }

    public char getNumericShortcut() {
        return this.mShortcutNumericChar;
    }

    public MenuItem setNumericShortcut(char numericChar) {
        if (this.mShortcutNumericChar == numericChar) {
            return this;
        }
        this.mShortcutNumericChar = numericChar;
        this.mMenu.onItemsChanged(false);
        return this;
    }

    public MenuItem setShortcut(char numericChar, char alphaChar) {
        this.mShortcutNumericChar = numericChar;
        this.mShortcutAlphabeticChar = Character.toLowerCase(alphaChar);
        this.mMenu.onItemsChanged(false);
        return this;
    }

    char getShortcut() {
        return this.mMenu.isQwertyMode() ? this.mShortcutAlphabeticChar : this.mShortcutNumericChar;
    }

    String getShortcutLabel() {
        char shortcut = getShortcut();
        if (shortcut == '\u0000') {
            return "";
        }
        StringBuilder sb = new StringBuilder(sPrependShortcutLabel);
        switch (shortcut) {
            case HIDDEN /*8*/:
                sb.append(sDeleteShortcutLabel);
                break;
            case PGSdk.TYPE_CLOCK /*10*/:
                sb.append(sEnterShortcutLabel);
                break;
            case IS_ACTION /*32*/:
                sb.append(sSpaceShortcutLabel);
                break;
            default:
                sb.append(shortcut);
                break;
        }
        return sb.toString();
    }

    boolean shouldShowShortcut() {
        return this.mMenu.isShortcutsVisible() && getShortcut() != '\u0000';
    }

    public SubMenu getSubMenu() {
        return this.mSubMenu;
    }

    public boolean hasSubMenu() {
        return this.mSubMenu != null;
    }

    void setSubMenu(SubMenuBuilder subMenu) {
        this.mSubMenu = subMenu;
        subMenu.setHeaderTitle(getTitle());
    }

    @CapturedViewProperty
    public CharSequence getTitle() {
        return this.mTitle;
    }

    CharSequence getTitleForItemView(ItemView itemView) {
        if (itemView == null || !itemView.prefersCondensedTitle()) {
            return getTitle();
        }
        return getTitleCondensed();
    }

    public MenuItem setTitle(CharSequence title) {
        this.mTitle = title;
        this.mMenu.onItemsChanged(false);
        if (this.mSubMenu != null) {
            this.mSubMenu.setHeaderTitle(title);
        }
        return this;
    }

    public MenuItem setTitle(int title) {
        return setTitle(this.mMenu.getContext().getString(title));
    }

    public CharSequence getTitleCondensed() {
        return this.mTitleCondensed != null ? this.mTitleCondensed : this.mTitle;
    }

    public MenuItem setTitleCondensed(CharSequence title) {
        this.mTitleCondensed = title;
        if (title == null) {
            title = this.mTitle;
        }
        this.mMenu.onItemsChanged(false);
        return this;
    }

    public Drawable getIcon() {
        if (this.mIconDrawable != null) {
            return this.mIconDrawable;
        }
        if (this.mIconResId == 0) {
            return null;
        }
        Drawable icon = this.mMenu.getContext().getDrawable(this.mIconResId);
        this.mIconResId = NO_ICON;
        this.mIconDrawable = icon;
        return icon;
    }

    public void setProgressStatus(int status, int progress) {
        if (status == 0) {
            setIcon((int) androidhwext.R.drawable.ic_menu_download);
        } else if (status == DOWNLOADING) {
            this.mMenuProgressDrawable = new ProgressDrawable(this.mMenu.getContext().getResources(), null);
            this.mMenuProgressDrawable.setProgress(progress);
            outDrawables = new Drawable[DOWNLOAD_PAUSE];
            outDrawables[NO_ICON] = this.mMenu.getContext().getDrawable(androidhwext.R.drawable.menu_download);
            outDrawables[DOWNLOADING] = this.mMenuProgressDrawable;
            this.mProgressDrawable = new LayerDrawable(outDrawables);
            setIcon(this.mProgressDrawable);
        } else if (status == DOWNLOAD_PAUSE) {
            this.mMenuProgressDrawable = new ProgressDrawable(this.mMenu.getContext().getResources(), null);
            this.mMenuProgressDrawable.setProgress(progress);
            outDrawables = new Drawable[DOWNLOAD_PAUSE];
            outDrawables[NO_ICON] = this.mMenu.getContext().getDrawable(androidhwext.R.drawable.menu_download_continue);
            outDrawables[DOWNLOADING] = this.mMenuProgressDrawable;
            this.mProgressDrawable = new LayerDrawable(outDrawables);
            setIcon(this.mProgressDrawable);
        }
    }

    public void setProgressStatus(Drawable drawable, int progress, int mProgressRadius) {
        this.mMenuProgressDrawable = new ProgressDrawable(this.mMenu.getContext().getResources(), null);
        this.mMenuProgressDrawable.setProgressRadius(mProgressRadius);
        this.mMenuProgressDrawable.setProgress(progress);
        Drawable[] outDrawables = new Drawable[DOWNLOAD_PAUSE];
        outDrawables[NO_ICON] = drawable;
        outDrawables[DOWNLOADING] = this.mMenuProgressDrawable;
        this.mProgressDrawable = new LayerDrawable(outDrawables);
        setIcon(this.mProgressDrawable);
    }

    public MenuItem setIcon(Drawable icon) {
        this.mIconResId = NO_ICON;
        this.mIconDrawable = icon;
        this.mMenu.onItemsChanged(false);
        return this;
    }

    public MenuItem setIcon(int iconResId) {
        this.mIconDrawable = null;
        this.mIconResId = iconResId;
        this.mMenu.onItemsChanged(false);
        return this;
    }

    public boolean isCheckable() {
        return (this.mFlags & DOWNLOADING) == DOWNLOADING;
    }

    public MenuItem setCheckable(boolean checkable) {
        int oldFlags = this.mFlags;
        this.mFlags = (checkable ? DOWNLOADING : NO_ICON) | (this.mFlags & -2);
        if (oldFlags != this.mFlags) {
            this.mMenu.onItemsChanged(false);
        }
        return this;
    }

    public void setExclusiveCheckable(boolean exclusive) {
        this.mFlags = (exclusive ? EXCLUSIVE : NO_ICON) | (this.mFlags & -5);
    }

    public boolean isExclusiveCheckable() {
        return (this.mFlags & EXCLUSIVE) != 0;
    }

    public boolean isChecked() {
        return (this.mFlags & DOWNLOAD_PAUSE) == DOWNLOAD_PAUSE;
    }

    public MenuItem setChecked(boolean checked) {
        if ((this.mFlags & EXCLUSIVE) != 0) {
            this.mMenu.setExclusiveItemChecked(this);
        } else {
            setCheckedInt(checked);
        }
        return this;
    }

    void setCheckedInt(boolean checked) {
        int i;
        int oldFlags = this.mFlags;
        int i2 = this.mFlags & -3;
        if (checked) {
            i = DOWNLOAD_PAUSE;
        } else {
            i = NO_ICON;
        }
        this.mFlags = i | i2;
        if (oldFlags != this.mFlags) {
            this.mMenu.onItemsChanged(false);
        }
    }

    public boolean isVisible() {
        boolean z = false;
        if (this.mActionProvider == null || !this.mActionProvider.overridesItemVisibility()) {
            if ((this.mFlags & HIDDEN) == 0) {
                z = true;
            }
            return z;
        }
        if ((this.mFlags & HIDDEN) == 0) {
            z = this.mActionProvider.isVisible();
        }
        return z;
    }

    boolean setVisibleInt(boolean shown) {
        int oldFlags = this.mFlags;
        this.mFlags = (shown ? NO_ICON : HIDDEN) | (this.mFlags & -9);
        if (oldFlags != this.mFlags) {
            return true;
        }
        return false;
    }

    public MenuItem setVisible(boolean shown) {
        if (setVisibleInt(shown)) {
            this.mMenu.onItemVisibleChanged(this);
        }
        return this;
    }

    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener clickListener) {
        this.mClickListener = clickListener;
        return this;
    }

    public String toString() {
        return this.mTitle != null ? this.mTitle.toString() : null;
    }

    void setMenuInfo(ContextMenuInfo menuInfo) {
        this.mMenuInfo = menuInfo;
    }

    public ContextMenuInfo getMenuInfo() {
        return this.mMenuInfo;
    }

    public void actionFormatChanged() {
        this.mMenu.onItemActionRequestChanged(this);
    }

    public boolean shouldShowIcon() {
        return this.mMenu.getOptionalIconsVisible();
    }

    public boolean isActionButton() {
        return (this.mFlags & IS_ACTION) == IS_ACTION;
    }

    public boolean requestsActionButton() {
        return (this.mShowAsAction & DOWNLOADING) == DOWNLOADING;
    }

    public boolean requiresActionButton() {
        return (this.mShowAsAction & DOWNLOAD_PAUSE) == DOWNLOAD_PAUSE;
    }

    public void setIsActionButton(boolean isActionButton) {
        if (isActionButton) {
            this.mFlags |= IS_ACTION;
        } else {
            this.mFlags &= -33;
        }
    }

    public boolean showsTextAsAction() {
        return (this.mShowAsAction & EXCLUSIVE) == EXCLUSIVE;
    }

    public void setShowAsAction(int actionEnum) {
        switch (actionEnum & SHOW_AS_ACTION_MASK) {
            case NO_ICON /*0*/:
            case DOWNLOADING /*1*/:
            case DOWNLOAD_PAUSE /*2*/:
                this.mShowAsAction = actionEnum;
                this.mMenu.onItemActionRequestChanged(this);
            default:
                throw new IllegalArgumentException("SHOW_AS_ACTION_ALWAYS, SHOW_AS_ACTION_IF_ROOM, and SHOW_AS_ACTION_NEVER are mutually exclusive.");
        }
    }

    public MenuItem setActionView(View view) {
        this.mActionView = view;
        this.mActionProvider = null;
        if (view != null && view.getId() == -1 && this.mId > 0) {
            view.setId(this.mId);
        }
        this.mMenu.onItemActionRequestChanged(this);
        return this;
    }

    public MenuItem setActionView(int resId) {
        Context context = this.mMenu.getContext();
        setActionView(LayoutInflater.from(context).inflate(resId, new LinearLayout(context), false));
        return this;
    }

    public View getActionView() {
        if (this.mActionView != null) {
            return this.mActionView;
        }
        if (this.mActionProvider == null) {
            return null;
        }
        this.mActionView = this.mActionProvider.onCreateActionView(this);
        return this.mActionView;
    }

    public ActionProvider getActionProvider() {
        return this.mActionProvider;
    }

    public MenuItem setActionProvider(ActionProvider actionProvider) {
        if (this.mActionProvider != null) {
            this.mActionProvider.reset();
        }
        this.mActionView = null;
        this.mActionProvider = actionProvider;
        this.mMenu.onItemsChanged(true);
        if (this.mActionProvider != null) {
            this.mActionProvider.setVisibilityListener(new VisibilityListener() {
                public void onActionProviderVisibilityChanged(boolean isVisible) {
                    MenuItemImpl.this.mMenu.onItemVisibleChanged(MenuItemImpl.this);
                }
            });
        }
        return this;
    }

    public MenuItem setShowAsActionFlags(int actionEnum) {
        setShowAsAction(actionEnum);
        return this;
    }

    public boolean expandActionView() {
        if (!hasCollapsibleActionView()) {
            return false;
        }
        if (this.mOnActionExpandListener == null || this.mOnActionExpandListener.onMenuItemActionExpand(this)) {
            return this.mMenu.expandItemActionView(this);
        }
        return false;
    }

    public boolean collapseActionView() {
        if ((this.mShowAsAction & HIDDEN) == 0) {
            return false;
        }
        if (this.mActionView == null) {
            return true;
        }
        if (this.mOnActionExpandListener == null || this.mOnActionExpandListener.onMenuItemActionCollapse(this)) {
            return this.mMenu.collapseItemActionView(this);
        }
        return false;
    }

    public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
        this.mOnActionExpandListener = listener;
        return this;
    }

    public boolean hasCollapsibleActionView() {
        boolean z = false;
        if ((this.mShowAsAction & HIDDEN) == 0) {
            return false;
        }
        if (this.mActionView == null && this.mActionProvider != null) {
            this.mActionView = this.mActionProvider.onCreateActionView(this);
        }
        if (this.mActionView != null) {
            z = true;
        }
        return z;
    }

    public void setActionViewExpanded(boolean isExpanded) {
        this.mIsActionViewExpanded = isExpanded;
        this.mMenu.onItemsChanged(false);
    }

    public boolean isActionViewExpanded() {
        return this.mIsActionViewExpanded;
    }
}
