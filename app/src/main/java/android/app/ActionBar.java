package android.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.widget.SpinnerAdapter;
import com.android.internal.R;

public abstract class ActionBar {
    public static final int DISPLAY_HOME_AS_UP = 4;
    public static final int DISPLAY_HW_NO_SPLIT_LINE = 32768;
    public static final int DISPLAY_SHOW_CUSTOM = 16;
    public static final int DISPLAY_SHOW_HOME = 2;
    public static final int DISPLAY_SHOW_TITLE = 8;
    public static final int DISPLAY_TITLE_MULTIPLE_LINES = 32;
    public static final int DISPLAY_USE_LOGO = 1;
    public static final int NAVIGATION_MODE_LIST = 1;
    public static final int NAVIGATION_MODE_STANDARD = 0;
    public static final int NAVIGATION_MODE_TABS = 2;

    private static class FollowOutOfActionBar implements OnFocusChangeListener, Runnable {
        private final ViewGroup mContainer;
        private final ViewGroup mFocusRoot;
        private final ViewGroup mToolbar;

        public FollowOutOfActionBar(ViewGroup focusRoot, ViewGroup container, ViewGroup toolbar) {
            this.mContainer = container;
            this.mToolbar = toolbar;
            this.mFocusRoot = focusRoot;
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                v.setOnFocusChangeListener(null);
                View focused = this.mFocusRoot.findFocus();
                if (focused != null) {
                    focused.setOnFocusChangeListener(this);
                } else {
                    this.mFocusRoot.post(this);
                }
            }
        }

        public void run() {
            if (this.mContainer != null) {
                this.mContainer.setTouchscreenBlocksFocus(true);
            }
            if (this.mToolbar != null) {
                this.mToolbar.setTouchscreenBlocksFocus(true);
            }
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        @ExportedProperty(category = "layout", mapping = {@IntToString(from = -1, to = "NONE"), @IntToString(from = 0, to = "NONE"), @IntToString(from = 48, to = "TOP"), @IntToString(from = 80, to = "BOTTOM"), @IntToString(from = 3, to = "LEFT"), @IntToString(from = 5, to = "RIGHT"), @IntToString(from = 8388611, to = "START"), @IntToString(from = 8388613, to = "END"), @IntToString(from = 16, to = "CENTER_VERTICAL"), @IntToString(from = 112, to = "FILL_VERTICAL"), @IntToString(from = 1, to = "CENTER_HORIZONTAL"), @IntToString(from = 7, to = "FILL_HORIZONTAL"), @IntToString(from = 17, to = "CENTER"), @IntToString(from = 119, to = "FILL")})
        public int gravity;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            this.gravity = ActionBar.NAVIGATION_MODE_STANDARD;
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ActionBar_LayoutParams);
            this.gravity = a.getInt(ActionBar.NAVIGATION_MODE_STANDARD, ActionBar.NAVIGATION_MODE_STANDARD);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.gravity = ActionBar.NAVIGATION_MODE_STANDARD;
            this.gravity = 8388627;
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = ActionBar.NAVIGATION_MODE_STANDARD;
            this.gravity = gravity;
        }

        public LayoutParams(int gravity) {
            this(-2, -1, gravity);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = ActionBar.NAVIGATION_MODE_STANDARD;
            this.gravity = source.gravity;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.gravity = ActionBar.NAVIGATION_MODE_STANDARD;
        }

        protected void encodeProperties(ViewHierarchyEncoder encoder) {
            super.encodeProperties(encoder);
            encoder.addProperty("gravity", this.gravity);
        }
    }

    public interface OnMenuVisibilityListener {
        void onMenuVisibilityChanged(boolean z);
    }

    public interface OnNavigationListener {
        boolean onNavigationItemSelected(int i, long j);
    }

    public static abstract class Tab {
        public static final int INVALID_POSITION = -1;

        public abstract CharSequence getContentDescription();

        public abstract View getCustomView();

        public abstract Drawable getIcon();

        public abstract int getPosition();

        public abstract Object getTag();

        public abstract CharSequence getText();

        public abstract void select();

        public abstract Tab setContentDescription(int i);

        public abstract Tab setContentDescription(CharSequence charSequence);

        public abstract Tab setCustomView(int i);

        public abstract Tab setCustomView(View view);

        public abstract Tab setIcon(int i);

        public abstract Tab setIcon(Drawable drawable);

        public abstract Tab setTabListener(TabListener tabListener);

        public abstract Tab setTag(Object obj);

        public abstract Tab setText(int i);

        public abstract Tab setText(CharSequence charSequence);
    }

    public interface TabListener {
        void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction);

        void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction);

        void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction);
    }

    public abstract void addOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener);

    public abstract void addTab(Tab tab);

    public abstract void addTab(Tab tab, int i);

    public abstract void addTab(Tab tab, int i, boolean z);

    public abstract void addTab(Tab tab, boolean z);

    public abstract View getCustomView();

    public abstract int getDisplayOptions();

    public abstract int getHeight();

    public abstract int getNavigationItemCount();

    public abstract int getNavigationMode();

    public abstract int getSelectedNavigationIndex();

    public abstract Tab getSelectedTab();

    public abstract CharSequence getSubtitle();

    public abstract Tab getTabAt(int i);

    public abstract int getTabCount();

    public abstract CharSequence getTitle();

    public abstract void hide();

    public abstract boolean isShowing();

    public abstract Tab newTab();

    public abstract void removeAllTabs();

    public abstract void removeOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener);

    public abstract void removeTab(Tab tab);

    public abstract void removeTabAt(int i);

    public abstract void selectTab(Tab tab);

    public abstract void setBackgroundDrawable(Drawable drawable);

    public abstract void setCustomView(int i);

    public abstract void setCustomView(View view);

    public abstract void setCustomView(View view, LayoutParams layoutParams);

    public abstract void setDisplayHomeAsUpEnabled(boolean z);

    public abstract void setDisplayOptions(int i);

    public abstract void setDisplayOptions(int i, int i2);

    public abstract void setDisplayShowCustomEnabled(boolean z);

    public abstract void setDisplayShowHomeEnabled(boolean z);

    public abstract void setDisplayShowTitleEnabled(boolean z);

    public abstract void setDisplayUseLogoEnabled(boolean z);

    public abstract void setIcon(int i);

    public abstract void setIcon(Drawable drawable);

    public abstract void setListNavigationCallbacks(SpinnerAdapter spinnerAdapter, OnNavigationListener onNavigationListener);

    public abstract void setLogo(int i);

    public abstract void setLogo(Drawable drawable);

    public abstract void setNavigationMode(int i);

    public abstract void setSelectedNavigationItem(int i);

    public abstract void setSubtitle(int i);

    public abstract void setSubtitle(CharSequence charSequence);

    public abstract void setTitle(int i);

    public abstract void setTitle(CharSequence charSequence);

    public abstract void show();

    public void setStackedBackgroundDrawable(Drawable d) {
    }

    public void setSplitBackgroundDrawable(Drawable d) {
    }

    public void setHomeButtonEnabled(boolean enabled) {
    }

    public Context getThemedContext() {
        return null;
    }

    public boolean isTitleTruncated() {
        return false;
    }

    public void setHomeAsUpIndicator(Drawable indicator) {
    }

    public void setHomeAsUpIndicator(int resId) {
    }

    public void setHomeActionContentDescription(CharSequence description) {
    }

    public void setHomeActionContentDescription(int resId) {
    }

    public void setHideOnContentScrollEnabled(boolean hideOnContentScroll) {
        if (hideOnContentScroll) {
            throw new UnsupportedOperationException("Hide on content scroll is not supported in this action bar configuration.");
        }
    }

    public boolean isHideOnContentScrollEnabled() {
        return false;
    }

    public int getHideOffset() {
        return NAVIGATION_MODE_STANDARD;
    }

    public void setHideOffset(int offset) {
        if (offset != 0) {
            throw new UnsupportedOperationException("Setting an explicit action bar hide offset is not supported in this action bar configuration.");
        }
    }

    public void setElevation(float elevation) {
        if (elevation != 0.0f) {
            throw new UnsupportedOperationException("Setting a non-zero elevation is not supported in this action bar configuration.");
        }
    }

    public float getElevation() {
        return 0.0f;
    }

    public void setDefaultDisplayHomeAsUpEnabled(boolean enabled) {
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
    }

    public void onConfigurationChanged(Configuration config) {
    }

    public void dispatchMenuVisibilityChanged(boolean visible) {
    }

    public ActionMode startActionMode(Callback callback) {
        return null;
    }

    public boolean openOptionsMenu() {
        return false;
    }

    public boolean invalidateOptionsMenu() {
        return false;
    }

    public boolean onMenuKeyEvent(KeyEvent event) {
        return false;
    }

    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean collapseActionView() {
        return false;
    }

    public void setWindowTitle(CharSequence title) {
    }

    public boolean requestFocus() {
        return false;
    }

    public void onDestroy() {
    }

    protected boolean requestFocus(ViewGroup viewGroup) {
        if (viewGroup == null || viewGroup.hasFocus()) {
            return false;
        }
        ViewGroup viewGroup2 = viewGroup.getTouchscreenBlocksFocus() ? viewGroup : null;
        ViewParent parent = viewGroup.getParent();
        ViewGroup container = null;
        while (parent != null && (parent instanceof ViewGroup)) {
            ViewGroup vgParent = (ViewGroup) parent;
            if (vgParent.getTouchscreenBlocksFocus()) {
                container = vgParent;
                break;
            }
            parent = vgParent.getParent();
        }
        if (container != null) {
            container.setTouchscreenBlocksFocus(false);
        }
        if (viewGroup2 != null) {
            viewGroup2.setTouchscreenBlocksFocus(false);
        }
        viewGroup.requestFocus();
        View focused = viewGroup.findFocus();
        if (focused != null) {
            focused.setOnFocusChangeListener(new FollowOutOfActionBar(viewGroup, container, viewGroup2));
        } else {
            if (container != null) {
                container.setTouchscreenBlocksFocus(true);
            }
            if (viewGroup2 != null) {
                viewGroup2.setTouchscreenBlocksFocus(true);
            }
        }
        return true;
    }

    public Drawable getBackgroundDrawable() {
        return null;
    }
}
