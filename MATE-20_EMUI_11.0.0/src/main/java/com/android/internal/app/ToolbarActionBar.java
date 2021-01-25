package com.android.internal.app;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowCallbackWrapper;
import android.widget.SpinnerAdapter;
import android.widget.Toolbar;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ToolbarWidgetWrapper;
import java.util.ArrayList;

public class ToolbarActionBar extends ActionBar {
    private DecorToolbar mDecorToolbar;
    private boolean mLastMenuVisibility;
    private boolean mMenuCallbackSet;
    private final Toolbar.OnMenuItemClickListener mMenuClicker = new Toolbar.OnMenuItemClickListener() {
        /* class com.android.internal.app.ToolbarActionBar.AnonymousClass2 */

        @Override // android.widget.Toolbar.OnMenuItemClickListener
        public boolean onMenuItemClick(MenuItem item) {
            return ToolbarActionBar.this.mWindowCallback.onMenuItemSelected(0, item);
        }
    };
    private final Runnable mMenuInvalidator = new Runnable() {
        /* class com.android.internal.app.ToolbarActionBar.AnonymousClass1 */

        @Override // java.lang.Runnable
        public void run() {
            ToolbarActionBar.this.populateOptionsMenu();
        }
    };
    private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList<>();
    private boolean mToolbarMenuPrepared;
    private Window.Callback mWindowCallback;

    public ToolbarActionBar(Toolbar toolbar, CharSequence title, Window.Callback windowCallback) {
        this.mDecorToolbar = new ToolbarWidgetWrapper(toolbar, false);
        this.mWindowCallback = new ToolbarCallbackWrapper(windowCallback);
        this.mDecorToolbar.setWindowCallback(this.mWindowCallback);
        toolbar.setOnMenuItemClickListener(this.mMenuClicker);
        this.mDecorToolbar.setWindowTitle(title);
    }

    public Window.Callback getWrappedWindowCallback() {
        return this.mWindowCallback;
    }

    @Override // android.app.ActionBar
    public void setCustomView(View view) {
        setCustomView(view, new ActionBar.LayoutParams(-2, -2));
    }

    @Override // android.app.ActionBar
    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        if (view != null) {
            view.setLayoutParams(layoutParams);
        }
        this.mDecorToolbar.setCustomView(view);
    }

    @Override // android.app.ActionBar
    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(this.mDecorToolbar.getContext()).inflate(resId, this.mDecorToolbar.getViewGroup(), false));
    }

    @Override // android.app.ActionBar
    public void setIcon(int resId) {
        this.mDecorToolbar.setIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setIcon(Drawable icon) {
        this.mDecorToolbar.setIcon(icon);
    }

    @Override // android.app.ActionBar
    public void setLogo(int resId) {
        this.mDecorToolbar.setLogo(resId);
    }

    @Override // android.app.ActionBar
    public void setLogo(Drawable logo) {
        this.mDecorToolbar.setLogo(logo);
    }

    @Override // android.app.ActionBar
    public void setStackedBackgroundDrawable(Drawable d) {
    }

    @Override // android.app.ActionBar
    public void setSplitBackgroundDrawable(Drawable d) {
    }

    @Override // android.app.ActionBar
    public void setHomeButtonEnabled(boolean enabled) {
    }

    @Override // android.app.ActionBar
    public void setElevation(float elevation) {
        this.mDecorToolbar.getViewGroup().setElevation(elevation);
    }

    @Override // android.app.ActionBar
    public float getElevation() {
        return this.mDecorToolbar.getViewGroup().getElevation();
    }

    @Override // android.app.ActionBar
    public Context getThemedContext() {
        return this.mDecorToolbar.getContext();
    }

    @Override // android.app.ActionBar
    public boolean isTitleTruncated() {
        return super.isTitleTruncated();
    }

    @Override // android.app.ActionBar
    public void setHomeAsUpIndicator(Drawable indicator) {
        this.mDecorToolbar.setNavigationIcon(indicator);
    }

    @Override // android.app.ActionBar
    public void setHomeAsUpIndicator(int resId) {
        this.mDecorToolbar.setNavigationIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setHomeActionContentDescription(CharSequence description) {
        this.mDecorToolbar.setNavigationContentDescription(description);
    }

    @Override // android.app.ActionBar
    public void setDefaultDisplayHomeAsUpEnabled(boolean enabled) {
    }

    @Override // android.app.ActionBar
    public void setHomeActionContentDescription(int resId) {
        this.mDecorToolbar.setNavigationContentDescription(resId);
    }

    @Override // android.app.ActionBar
    public void setShowHideAnimationEnabled(boolean enabled) {
    }

    @Override // android.app.ActionBar
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    @Override // android.app.ActionBar
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override // android.app.ActionBar
    public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        this.mDecorToolbar.setDropdownParams(adapter, new NavItemSelectedListener(callback));
    }

    @Override // android.app.ActionBar
    public void setSelectedNavigationItem(int position) {
        if (this.mDecorToolbar.getNavigationMode() == 1) {
            this.mDecorToolbar.setDropdownSelectedPosition(position);
            return;
        }
        throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
    }

    @Override // android.app.ActionBar
    public int getSelectedNavigationIndex() {
        return -1;
    }

    @Override // android.app.ActionBar
    public int getNavigationItemCount() {
        return 0;
    }

    @Override // android.app.ActionBar
    public void setTitle(CharSequence title) {
        this.mDecorToolbar.setTitle(title);
    }

    @Override // android.app.ActionBar
    public void setTitle(int resId) {
        DecorToolbar decorToolbar = this.mDecorToolbar;
        decorToolbar.setTitle(resId != 0 ? decorToolbar.getContext().getText(resId) : null);
    }

    @Override // android.app.ActionBar
    public void setWindowTitle(CharSequence title) {
        this.mDecorToolbar.setWindowTitle(title);
    }

    @Override // android.app.ActionBar
    public void setSubtitle(CharSequence subtitle) {
        this.mDecorToolbar.setSubtitle(subtitle);
    }

    @Override // android.app.ActionBar
    public void setSubtitle(int resId) {
        DecorToolbar decorToolbar = this.mDecorToolbar;
        decorToolbar.setSubtitle(resId != 0 ? decorToolbar.getContext().getText(resId) : null);
    }

    @Override // android.app.ActionBar
    public void setDisplayOptions(int options) {
        setDisplayOptions(options, -1);
    }

    @Override // android.app.ActionBar
    public void setDisplayOptions(int options, int mask) {
        this.mDecorToolbar.setDisplayOptions((options & mask) | ((~mask) & this.mDecorToolbar.getDisplayOptions()));
    }

    @Override // android.app.ActionBar
    public void setDisplayUseLogoEnabled(boolean useLogo) {
        setDisplayOptions(useLogo ? 1 : 0, 1);
    }

    @Override // android.app.ActionBar
    public void setDisplayShowHomeEnabled(boolean showHome) {
        setDisplayOptions(showHome ? 2 : 0, 2);
    }

    @Override // android.app.ActionBar
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        setDisplayOptions(showHomeAsUp ? 4 : 0, 4);
    }

    @Override // android.app.ActionBar
    public void setDisplayShowTitleEnabled(boolean showTitle) {
        setDisplayOptions(showTitle ? 8 : 0, 8);
    }

    @Override // android.app.ActionBar
    public void setDisplayShowCustomEnabled(boolean showCustom) {
        setDisplayOptions(showCustom ? 16 : 0, 16);
    }

    @Override // android.app.ActionBar
    public void setBackgroundDrawable(Drawable d) {
        this.mDecorToolbar.setBackgroundDrawable(d);
    }

    @Override // android.app.ActionBar
    public View getCustomView() {
        return this.mDecorToolbar.getCustomView();
    }

    @Override // android.app.ActionBar
    public CharSequence getTitle() {
        return this.mDecorToolbar.getTitle();
    }

    @Override // android.app.ActionBar
    public CharSequence getSubtitle() {
        return this.mDecorToolbar.getSubtitle();
    }

    @Override // android.app.ActionBar
    public int getNavigationMode() {
        return 0;
    }

    @Override // android.app.ActionBar
    public void setNavigationMode(int mode) {
        if (mode != 2) {
            this.mDecorToolbar.setNavigationMode(mode);
            return;
        }
        throw new IllegalArgumentException("Tabs not supported in this configuration");
    }

    @Override // android.app.ActionBar
    public int getDisplayOptions() {
        return this.mDecorToolbar.getDisplayOptions();
    }

    @Override // android.app.ActionBar
    public ActionBar.Tab newTab() {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void removeTab(ActionBar.Tab tab) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void removeTabAt(int position) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void removeAllTabs() {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public void selectTab(ActionBar.Tab tab) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public ActionBar.Tab getSelectedTab() {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public ActionBar.Tab getTabAt(int index) {
        throw new UnsupportedOperationException("Tabs are not supported in toolbar action bars");
    }

    @Override // android.app.ActionBar
    public int getTabCount() {
        return 0;
    }

    @Override // android.app.ActionBar
    public int getHeight() {
        return this.mDecorToolbar.getHeight();
    }

    @Override // android.app.ActionBar
    public void show() {
        this.mDecorToolbar.setVisibility(0);
    }

    @Override // android.app.ActionBar
    public void hide() {
        this.mDecorToolbar.setVisibility(8);
    }

    @Override // android.app.ActionBar
    public boolean isShowing() {
        return this.mDecorToolbar.getVisibility() == 0;
    }

    @Override // android.app.ActionBar
    public boolean openOptionsMenu() {
        return this.mDecorToolbar.showOverflowMenu();
    }

    @Override // android.app.ActionBar
    public boolean closeOptionsMenu() {
        return this.mDecorToolbar.hideOverflowMenu();
    }

    @Override // android.app.ActionBar
    public boolean invalidateOptionsMenu() {
        this.mDecorToolbar.getViewGroup().removeCallbacks(this.mMenuInvalidator);
        this.mDecorToolbar.getViewGroup().postOnAnimation(this.mMenuInvalidator);
        return true;
    }

    @Override // android.app.ActionBar
    public boolean collapseActionView() {
        if (!this.mDecorToolbar.hasExpandedActionView()) {
            return false;
        }
        this.mDecorToolbar.collapseActionView();
        return true;
    }

    /* access modifiers changed from: package-private */
    public void populateOptionsMenu() {
        if (!this.mMenuCallbackSet) {
            this.mDecorToolbar.setMenuCallbacks(new ActionMenuPresenterCallback(), new MenuBuilderCallback());
            this.mMenuCallbackSet = true;
        }
        Menu menu = this.mDecorToolbar.getMenu();
        MenuBuilder mb = menu instanceof MenuBuilder ? (MenuBuilder) menu : null;
        if (mb != null) {
            mb.stopDispatchingItemsChanged();
        }
        try {
            menu.clear();
            if (!this.mWindowCallback.onCreatePanelMenu(0, menu) || !this.mWindowCallback.onPreparePanel(0, null, menu)) {
                menu.clear();
            }
        } finally {
            if (mb != null) {
                mb.startDispatchingItemsChanged();
            }
        }
    }

    @Override // android.app.ActionBar
    public boolean onMenuKeyEvent(KeyEvent event) {
        if (event.getAction() == 1) {
            openOptionsMenu();
        }
        return true;
    }

    @Override // android.app.ActionBar
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        Menu menu = this.mDecorToolbar.getMenu();
        if (menu == null) {
            return false;
        }
        boolean z = true;
        if (KeyCharacterMap.load(event != null ? event.getDeviceId() : -1).getKeyboardType() == 1) {
            z = false;
        }
        menu.setQwertyMode(z);
        return menu.performShortcut(keyCode, event, 0);
    }

    @Override // android.app.ActionBar
    public void onDestroy() {
        this.mDecorToolbar.getViewGroup().removeCallbacks(this.mMenuInvalidator);
    }

    @Override // android.app.ActionBar
    public void addOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.add(listener);
    }

    @Override // android.app.ActionBar
    public void removeOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.remove(listener);
    }

    @Override // android.app.ActionBar
    public void dispatchMenuVisibilityChanged(boolean isVisible) {
        if (isVisible != this.mLastMenuVisibility) {
            this.mLastMenuVisibility = isVisible;
            int count = this.mMenuVisibilityListeners.size();
            for (int i = 0; i < count; i++) {
                this.mMenuVisibilityListeners.get(i).onMenuVisibilityChanged(isVisible);
            }
        }
    }

    private class ToolbarCallbackWrapper extends WindowCallbackWrapper {
        public ToolbarCallbackWrapper(Window.Callback wrapped) {
            super(wrapped);
        }

        @Override // android.view.WindowCallbackWrapper, android.view.Window.Callback
        public boolean onPreparePanel(int featureId, View view, Menu menu) {
            boolean result = super.onPreparePanel(featureId, view, menu);
            if (result && !ToolbarActionBar.this.mToolbarMenuPrepared) {
                ToolbarActionBar.this.mDecorToolbar.setMenuPrepared();
                ToolbarActionBar.this.mToolbarMenuPrepared = true;
            }
            return result;
        }

        @Override // android.view.WindowCallbackWrapper, android.view.Window.Callback
        public View onCreatePanelView(int featureId) {
            if (featureId == 0) {
                return new View(ToolbarActionBar.this.mDecorToolbar.getContext());
            }
            return super.onCreatePanelView(featureId);
        }
    }

    /* access modifiers changed from: private */
    public final class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private boolean mClosingActionMenu;

        private ActionMenuPresenterCallback() {
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (ToolbarActionBar.this.mWindowCallback == null) {
                return false;
            }
            ToolbarActionBar.this.mWindowCallback.onMenuOpened(8, subMenu);
            return true;
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (!this.mClosingActionMenu) {
                this.mClosingActionMenu = true;
                ToolbarActionBar.this.mDecorToolbar.dismissPopupMenus();
                if (ToolbarActionBar.this.mWindowCallback != null) {
                    ToolbarActionBar.this.mWindowCallback.onPanelClosed(8, menu);
                }
                this.mClosingActionMenu = false;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class MenuBuilderCallback implements MenuBuilder.Callback {
        private MenuBuilderCallback() {
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            return false;
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
            if (ToolbarActionBar.this.mWindowCallback == null) {
                return;
            }
            if (ToolbarActionBar.this.mDecorToolbar.isOverflowMenuShowing()) {
                ToolbarActionBar.this.mWindowCallback.onPanelClosed(8, menu);
            } else if (ToolbarActionBar.this.mWindowCallback.onPreparePanel(0, null, menu)) {
                ToolbarActionBar.this.mWindowCallback.onMenuOpened(8, menu);
            }
        }
    }
}
