package com.android.internal.widget;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;
import com.huawei.pgmng.PGAction;
import java.lang.annotation.RCUnownedThisRef;

public class ActionBarView extends AbsActionBarView implements DecorToolbar {
    private static final int DEFAULT_CUSTOM_GRAVITY = 8388627;
    public static final int DISPLAY_DEFAULT = 0;
    private static final int DISPLAY_RELAYOUT_MASK = 63;
    private static final String TAG = "ActionBarView";
    private ActionBarContextView mContextView;
    protected View mCustTitle;
    /* access modifiers changed from: private */
    public View mCustomNavView;
    private int mDefaultUpDescription = 17039538;
    /* access modifiers changed from: private */
    public int mDisplayOptions = -1;
    public View mExpandedActionView;
    /* access modifiers changed from: private */
    public HomeView mExpandedHomeLayout;
    public ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private CharSequence mHomeDescription;
    private int mHomeDescriptionRes;
    /* access modifiers changed from: private */
    public HomeView mHomeLayout;
    private int mHomeResId;
    /* access modifiers changed from: private */
    public Drawable mIcon;
    private boolean mIncludeTabs;
    private final int mIndeterminateProgressStyle;
    private ProgressBar mIndeterminateProgressView;
    private boolean mIsCollapsible;
    private int mItemPadding;
    private LinearLayout mListNavLayout;
    private Drawable mLogo;
    /* access modifiers changed from: private */
    public ActionMenuItem mLogoNavItem;
    /* access modifiers changed from: private */
    public boolean mMenuPrepared;
    private AdapterView.OnItemSelectedListener mNavItemSelectedListener;
    private int mNavigationMode;
    private MenuBuilder mOptionsMenu;
    private int mProgressBarPadding;
    private final int mProgressStyle;
    private ProgressBar mProgressView;
    /* access modifiers changed from: private */
    public Spinner mSpinner;
    private SpinnerAdapter mSpinnerAdapter;
    private CharSequence mSubtitle;
    private final int mSubtitleStyleRes;
    private TextView mSubtitleView;
    /* access modifiers changed from: private */
    public ScrollingTabContainerView mTabScrollView;
    private Runnable mTabSelector;
    private CharSequence mTitle;
    protected LinearLayout mTitleLayout;
    private final int mTitleStyleRes;
    private TextView mTitleView;
    private final View.OnClickListener mUpClickListener = new View.OnClickListener() {
        @RCUnownedThisRef
        public void onClick(View v) {
            if (ActionBarView.this.mMenuPrepared) {
                ActionBarView.this.mWindowCallback.onMenuItemSelected(0, ActionBarView.this.mLogoNavItem);
            }
        }
    };
    /* access modifiers changed from: private */
    public ViewGroup mUpGoerFive;
    private boolean mUserTitle;
    /* access modifiers changed from: private */
    public boolean mWasHomeEnabled;
    Window.Callback mWindowCallback;

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        public void initForMenu(Context context, MenuBuilder menu) {
            if (!(this.mMenu == null || this.mCurrentExpandedItem == null)) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menu;
        }

        public MenuView getMenuView(ViewGroup root) {
            return null;
        }

        public void updateMenuView(boolean cleared) {
            if (this.mCurrentExpandedItem != null) {
                boolean found = false;
                if (this.mMenu != null) {
                    int count = this.mMenu.size();
                    int i = 0;
                    while (true) {
                        if (i >= count) {
                            break;
                        } else if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            found = true;
                            break;
                        } else {
                            i++;
                        }
                    }
                }
                if (!found) {
                    collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
                }
            }
        }

        public void setCallback(MenuPresenter.Callback cb) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean flagActionItems() {
            return false;
        }

        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (ActionBarView.this.mExpandedHomeLayout == null) {
                ActionBarView.this.initExpandedHomeLayout();
            }
            ActionBarView.this.mExpandedActionView = item.getActionView();
            ActionBarView.this.mExpandedHomeLayout.setIcon(ActionBarView.this.mIcon.getConstantState().newDrawable(ActionBarView.this.getResources()));
            this.mCurrentExpandedItem = item;
            if (ActionBarView.this.mExpandedActionView.getParent() != ActionBarView.this) {
                ActionBarView.this.addView(ActionBarView.this.mExpandedActionView);
            }
            if (ActionBarView.this.mExpandedHomeLayout.getParent() != ActionBarView.this.mUpGoerFive) {
                ActionBarView.this.mUpGoerFive.addView(ActionBarView.this.mExpandedHomeLayout);
            }
            ActionBarView.this.mHomeLayout.setVisibility(8);
            if (ActionBarView.this.mTitleLayout != null) {
                ActionBarView.this.mTitleLayout.setVisibility(8);
            }
            if (ActionBarView.this.mTabScrollView != null) {
                ActionBarView.this.mTabScrollView.setVisibility(8);
            }
            if (ActionBarView.this.mSpinner != null) {
                ActionBarView.this.mSpinner.setVisibility(8);
            }
            if (ActionBarView.this.mCustomNavView != null) {
                ActionBarView.this.mCustomNavView.setVisibility(8);
            }
            ActionBarView.this.setHomeButtonEnabled(false, false);
            ActionBarView.this.requestLayout();
            item.setActionViewExpanded(true);
            if (ActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) ActionBarView.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (ActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) ActionBarView.this.mExpandedActionView).onActionViewCollapsed();
            }
            ActionBarView.this.removeView(ActionBarView.this.mExpandedActionView);
            if (ActionBarView.this.mExpandedHomeLayout != null && ActionBarView.this.mExpandedHomeLayout.getParent() == ActionBarView.this.mUpGoerFive) {
                ActionBarView.this.mUpGoerFive.removeView(ActionBarView.this.mExpandedHomeLayout);
            }
            ActionBarView.this.mExpandedActionView = null;
            if ((ActionBarView.this.mDisplayOptions & 2) != 0) {
                ActionBarView.this.mHomeLayout.setVisibility(0);
            }
            if ((ActionBarView.this.mDisplayOptions & 8) != 0) {
                if (ActionBarView.this.mTitleLayout == null) {
                    ActionBarView.this.initTitle();
                } else {
                    ActionBarView.this.mTitleLayout.setVisibility(0);
                }
            }
            if (ActionBarView.this.mTabScrollView != null) {
                ActionBarView.this.mTabScrollView.setVisibility(0);
            }
            if (ActionBarView.this.mSpinner != null) {
                ActionBarView.this.mSpinner.setVisibility(0);
            }
            if (ActionBarView.this.mCustomNavView != null) {
                ActionBarView.this.mCustomNavView.setVisibility(0);
            }
            if (ActionBarView.this.mExpandedHomeLayout != null) {
                ActionBarView.this.mExpandedHomeLayout.setIcon(null);
            }
            this.mCurrentExpandedItem = null;
            ActionBarView.this.setHomeButtonEnabled(ActionBarView.this.mWasHomeEnabled);
            ActionBarView.this.requestLayout();
            item.setActionViewExpanded(false);
            return true;
        }

        public int getId() {
            return 0;
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onRestoreInstanceState(Parcelable state) {
        }
    }

    public static class HomeView extends FrameLayout {
        private static final long DEFAULT_TRANSITION_DURATION = 150;
        private Drawable mDefaultUpIndicator;
        public ImageView mIconView;
        private int mStartOffset;
        private Drawable mUpIndicator;
        private int mUpIndicatorRes;
        public ImageView mUpView;
        public int mUpWidth;

        public HomeView(Context context) {
            this(context, null);
        }

        public HomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutTransition t = getLayoutTransition();
            if (t != null) {
                t.setDuration(DEFAULT_TRANSITION_DURATION);
            }
        }

        public void setShowUp(boolean isUp) {
            this.mUpView.setVisibility(isUp ? 0 : 8);
        }

        public void setShowIcon(boolean showIcon) {
            this.mIconView.setVisibility(showIcon ? 0 : 8);
        }

        public void setIcon(Drawable icon) {
            this.mIconView.setImageDrawable(icon);
        }

        public void setUpIndicator(Drawable d) {
            this.mUpIndicator = d;
            this.mUpIndicatorRes = 0;
            updateUpIndicator();
        }

        public void setDefaultUpIndicator(Drawable d) {
            this.mDefaultUpIndicator = d;
            updateUpIndicator();
        }

        public void setUpIndicator(int resId) {
            this.mUpIndicatorRes = resId;
            this.mUpIndicator = null;
            updateUpIndicator();
        }

        private void updateUpIndicator() {
            if (this.mUpIndicator != null) {
                this.mUpView.setImageDrawable(this.mUpIndicator);
            } else if (this.mUpIndicatorRes != 0) {
                this.mUpView.setImageDrawable(getContext().getDrawable(this.mUpIndicatorRes));
            } else {
                this.mUpView.setImageDrawable(this.mDefaultUpIndicator);
            }
        }

        /* access modifiers changed from: protected */
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if (this.mUpIndicatorRes != 0) {
                updateUpIndicator();
            }
        }

        public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
            onPopulateAccessibilityEvent(event);
            return true;
        }

        public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
            super.onPopulateAccessibilityEventInternal(event);
            CharSequence cdesc = getContentDescription();
            if (!TextUtils.isEmpty(cdesc)) {
                event.getText().add(cdesc);
            }
        }

        public boolean dispatchHoverEvent(MotionEvent event) {
            return onHoverEvent(event);
        }

        /* access modifiers changed from: protected */
        public void onFinishInflate() {
            this.mUpView = (ImageView) findViewById(16909503);
            this.mIconView = (ImageView) findViewById(16908332);
            this.mDefaultUpIndicator = this.mUpView.getDrawable();
        }

        public int getStartOffset() {
            if (this.mUpView.getVisibility() == 8) {
                return this.mStartOffset;
            }
            return 0;
        }

        public int getUpWidth() {
            return this.mUpWidth;
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            measureChildWithMargins(this.mUpView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            FrameLayout.LayoutParams upLp = (FrameLayout.LayoutParams) this.mUpView.getLayoutParams();
            int upMargins = upLp.leftMargin + upLp.rightMargin;
            this.mUpWidth = this.mUpView.getMeasuredWidth();
            this.mStartOffset = this.mUpWidth + upMargins;
            int width = this.mUpView.getVisibility() == 8 ? 0 : this.mStartOffset;
            int height = upLp.topMargin + this.mUpView.getMeasuredHeight() + upLp.bottomMargin;
            if (this.mIconView.getVisibility() != 8) {
                measureChildWithMargins(this.mIconView, widthMeasureSpec, width, heightMeasureSpec, 0);
                FrameLayout.LayoutParams iconLp = (FrameLayout.LayoutParams) this.mIconView.getLayoutParams();
                width += iconLp.leftMargin + this.mIconView.getMeasuredWidth() + iconLp.rightMargin;
                height = Math.max(height, iconLp.topMargin + this.mIconView.getMeasuredHeight() + iconLp.bottomMargin);
            } else if (upMargins < 0) {
                width -= upMargins;
            }
            int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
            if (widthMode == Integer.MIN_VALUE) {
                width = Math.min(width, widthSize);
            } else if (widthMode == 1073741824) {
                width = widthSize;
            }
            if (heightMode == Integer.MIN_VALUE) {
                height = Math.min(height, heightSize);
            } else if (heightMode == 1073741824) {
                height = heightSize;
            }
            setMeasuredDimension(width, height);
        }

        /* access modifiers changed from: protected */
        public void onLayout(boolean changed, int l, int t, int r, int b) {
            int l2;
            int r2;
            int iconLeft;
            int iconRight;
            int upLeft;
            int upRight;
            int vCenter = (b - t) / 2;
            boolean isLayoutRtl = isLayoutRtl();
            int width = getWidth();
            int upOffset = 0;
            if (this.mUpView.getVisibility() != 8) {
                FrameLayout.LayoutParams upLp = (FrameLayout.LayoutParams) this.mUpView.getLayoutParams();
                int upHeight = this.mUpView.getMeasuredHeight();
                int upWidth = this.mUpView.getMeasuredWidth();
                upOffset = upLp.leftMargin + upWidth + upLp.rightMargin;
                int upTop = vCenter - (upHeight / 2);
                int upBottom = upTop + upHeight;
                if (isLayoutRtl) {
                    upRight = width;
                    upLeft = upRight - upWidth;
                    r2 = r - upOffset;
                    l2 = l;
                } else {
                    upRight = upWidth;
                    upLeft = 0;
                    l2 = l + upOffset;
                    r2 = r;
                }
                this.mUpView.layout(upLeft, upTop, upRight, upBottom);
            } else {
                l2 = l;
                r2 = r;
            }
            FrameLayout.LayoutParams iconLp = (FrameLayout.LayoutParams) this.mIconView.getLayoutParams();
            int iconHeight = this.mIconView.getMeasuredHeight();
            int iconWidth = this.mIconView.getMeasuredWidth();
            int iconTop = Math.max(iconLp.topMargin, vCenter - (iconHeight / 2));
            int iconBottom = iconTop + iconHeight;
            int delta = Math.max(iconLp.getMarginStart(), ((r2 - l2) / 2) - (iconWidth / 2));
            if (isLayoutRtl) {
                iconRight = (width - upOffset) - delta;
                iconLeft = iconRight - iconWidth;
            } else {
                iconLeft = upOffset + delta;
                iconRight = iconLeft + iconWidth;
            }
            this.mIconView.layout(iconLeft, iconTop, iconRight, iconBottom);
        }

        /* access modifiers changed from: protected */
        @Deprecated
        public void layoutUpView(View view, int upLeft, int upTop, int upRight, int upBottom, int leftMargin, int upOffset) {
        }
    }

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int expandedMenuItemId;
        boolean isOverflowOpen;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.expandedMenuItemId = in.readInt();
            this.isOverflowOpen = in.readInt() != 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.expandedMenuItemId);
            out.writeInt(this.isOverflowOpen ? 1 : 0);
        }
    }

    public ActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar, 16843470, 0);
        this.mNavigationMode = a.getInt(7, 0);
        this.mTitle = a.getText(5);
        this.mSubtitle = a.getText(9);
        this.mLogo = a.getDrawable(6);
        this.mIcon = a.getDrawable(0);
        LayoutInflater inflater = LayoutInflater.from(context);
        this.mHomeResId = a.getResourceId(16, 17367066);
        this.mUpGoerFive = (ViewGroup) inflater.inflate(17367069, this, false);
        this.mHomeLayout = (HomeView) inflater.inflate(this.mHomeResId, this.mUpGoerFive, false);
        this.mTitleStyleRes = a.getResourceId(11, 0);
        this.mSubtitleStyleRes = a.getResourceId(12, 0);
        this.mProgressStyle = a.getResourceId(1, 0);
        this.mIndeterminateProgressStyle = a.getResourceId(14, 0);
        this.mProgressBarPadding = a.getDimensionPixelOffset(15, 0);
        this.mItemPadding = a.getDimensionPixelOffset(17, 0);
        setDisplayOptions(a.getInt(8, 0));
        int customNavId = a.getResourceId(10, 0);
        if (customNavId != 0) {
            this.mCustomNavView = inflater.inflate(customNavId, this, false);
            this.mNavigationMode = 0;
            setDisplayOptions(16 | this.mDisplayOptions);
        }
        this.mContentHeight = a.getLayoutDimension(4, 0);
        a.recycle();
        ActionMenuItem actionMenuItem = new ActionMenuItem(context, 0, 16908332, 0, 0, this.mTitle);
        this.mLogoNavItem = actionMenuItem;
        this.mUpGoerFive.setOnClickListener(this.mUpClickListener);
        this.mUpGoerFive.setClickable(true);
        this.mUpGoerFive.setFocusable(true);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mTitleView = null;
        this.mSubtitleView = null;
        if (this.mTitleLayout != null && this.mTitleLayout.getParent() == this.mUpGoerFive) {
            this.mUpGoerFive.removeView(this.mTitleLayout);
        }
        this.mTitleLayout = null;
        if ((this.mDisplayOptions & 8) != 0) {
            initTitle();
        }
        if (this.mHomeDescriptionRes != 0) {
            setNavigationContentDescription(this.mHomeDescriptionRes);
        }
        if (this.mTabScrollView != null && this.mIncludeTabs) {
            ViewGroup.LayoutParams lp = this.mTabScrollView.getLayoutParams();
            if (lp != null) {
                lp.width = -2;
                lp.height = -1;
            }
            this.mTabScrollView.setAllowCollapse(true);
        }
    }

    public void setWindowCallback(Window.Callback cb) {
        this.mWindowCallback = cb;
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mTabSelector);
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void initProgress() {
        this.mProgressView = new ProgressBar(this.mContext, null, 0, this.mProgressStyle);
        this.mProgressView.setId(16909229);
        this.mProgressView.setMax(PGAction.PG_ID_DEFAULT_FRONT);
        this.mProgressView.setVisibility(8);
        addView(this.mProgressView);
    }

    public void initIndeterminateProgress() {
        this.mIndeterminateProgressView = new ProgressBar(this.mContext, null, 0, this.mIndeterminateProgressStyle);
        this.mIndeterminateProgressView.setId(16909228);
        this.mIndeterminateProgressView.setVisibility(8);
        addView(this.mIndeterminateProgressView);
    }

    public void setSplitToolbar(boolean splitActionBar) {
        if (this.mSplitActionBar != splitActionBar) {
            if (this.mMenuView != null) {
                ViewGroup oldParent = (ViewGroup) this.mMenuView.getParent();
                if (oldParent != null) {
                    oldParent.removeView(this.mMenuView);
                }
                if (splitActionBar) {
                    if (this.mSplitView != null) {
                        this.mSplitView.addView(this.mMenuView);
                    }
                    this.mMenuView.getLayoutParams().width = -1;
                } else {
                    addView(this.mMenuView);
                    this.mMenuView.getLayoutParams().width = -2;
                }
                this.mMenuView.requestLayout();
            }
            if (this.mSplitView != null) {
                this.mSplitView.setVisibility(splitActionBar ? 0 : 8);
            }
            if (this.mActionMenuPresenter != null) {
                if (!splitActionBar) {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(17956866));
                } else {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                    this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                    this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                }
            }
            super.setSplitToolbar(splitActionBar);
        }
    }

    public boolean isSplit() {
        return this.mSplitActionBar;
    }

    public boolean canSplit() {
        return true;
    }

    public boolean hasEmbeddedTabs() {
        return this.mIncludeTabs;
    }

    public void setEmbeddedTabView(ScrollingTabContainerView tabs) {
        if (this.mTabScrollView != null) {
            removeView(this.mTabScrollView);
        }
        this.mTabScrollView = tabs;
        this.mIncludeTabs = tabs != null;
        if (this.mIncludeTabs && this.mNavigationMode == 2) {
            addView(this.mTabScrollView);
            ViewGroup.LayoutParams lp = this.mTabScrollView.getLayoutParams();
            lp.width = -2;
            lp.height = -1;
            tabs.setAllowCollapse(true);
        }
    }

    public void setSplitViewLocation(int start, int end) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void updateSplitLocation() {
    }

    public void setMenuPrepared() {
        this.mMenuPrepared = true;
    }

    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        ActionMenuView menuView;
        if (menu != this.mOptionsMenu) {
            if (this.mOptionsMenu != null) {
                this.mOptionsMenu.removeMenuPresenter(this.mActionMenuPresenter);
                this.mOptionsMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
            }
            MenuBuilder builder = (MenuBuilder) menu;
            this.mOptionsMenu = builder;
            if (this.mMenuView != null) {
                ViewGroup oldParent = (ViewGroup) this.mMenuView.getParent();
                if (oldParent != null) {
                    oldParent.removeView(this.mMenuView);
                }
            }
            if (this.mActionMenuPresenter == null) {
                this.mActionMenuPresenter = new ActionMenuPresenter(this.mContext);
                this.mActionMenuPresenter.setCallback(cb);
                this.mActionMenuPresenter.setId(16908700);
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
            if (!this.mSplitActionBar) {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(17956866));
                configPresenters(builder);
                menuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                ViewGroup oldParent2 = (ViewGroup) menuView.getParent();
                if (!(oldParent2 == null || oldParent2 == this)) {
                    oldParent2.removeView(menuView);
                }
                addView(menuView, layoutParams);
            } else {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                layoutParams.width = -1;
                layoutParams.height = -2;
                configPresenters(builder);
                menuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                if (this.mSplitView != null) {
                    ViewGroup oldParent3 = (ViewGroup) menuView.getParent();
                    if (!(oldParent3 == null || oldParent3 == this.mSplitView)) {
                        oldParent3.removeView(menuView);
                    }
                    menuView.setVisibility(getAnimatedVisibility());
                    this.mSplitView.addView(menuView, layoutParams);
                } else {
                    menuView.setLayoutParams(layoutParams);
                }
            }
            this.mMenuView = menuView;
        }
    }

    private void configPresenters(MenuBuilder builder) {
        if (builder != null) {
            builder.addMenuPresenter(this.mActionMenuPresenter, this.mPopupContext);
            builder.addMenuPresenter(this.mExpandedMenuPresenter, this.mPopupContext);
            return;
        }
        this.mActionMenuPresenter.initForMenu(this.mPopupContext, null);
        this.mExpandedMenuPresenter.initForMenu(this.mPopupContext, null);
        this.mActionMenuPresenter.updateMenuView(true);
        this.mExpandedMenuPresenter.updateMenuView(true);
    }

    public boolean hasExpandedActionView() {
        return (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) ? false : true;
    }

    public void collapseActionView() {
        MenuItemImpl item;
        if (this.mExpandedMenuPresenter == null) {
            item = null;
        } else {
            item = this.mExpandedMenuPresenter.mCurrentExpandedItem;
        }
        if (item != null) {
            item.collapseActionView();
        }
    }

    public void setCustomView(View view) {
        boolean showCustom = (this.mDisplayOptions & 16) != 0;
        if (this.mCustomNavView != null && showCustom) {
            removeView(this.mCustomNavView);
        }
        this.mCustomNavView = view;
        if (this.mCustomNavView != null && showCustom) {
            addView(this.mCustomNavView);
        }
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public void setTitle(CharSequence title) {
        this.mUserTitle = true;
        setTitleImpl(title);
    }

    public void setWindowTitle(CharSequence title) {
        if (!this.mUserTitle) {
            setTitleImpl(title);
        }
    }

    private void setTitleImpl(CharSequence title) {
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
            int i = 0;
            boolean visible = this.mExpandedActionView == null && (this.mDisplayOptions & 8) != 0 && (!TextUtils.isEmpty(this.mTitle) || !TextUtils.isEmpty(this.mSubtitle));
            LinearLayout linearLayout = this.mTitleLayout;
            if (!visible) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
        if (this.mLogoNavItem != null) {
            this.mLogoNavItem.setTitle(title);
        }
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mSubtitle = subtitle;
        if (this.mSubtitleView != null) {
            this.mSubtitleView.setText(subtitle);
            int i = 8;
            this.mSubtitleView.setVisibility(subtitle != null ? 0 : 8);
            boolean visible = this.mExpandedActionView == null && (this.mDisplayOptions & 8) != 0 && (!TextUtils.isEmpty(this.mTitle) || !TextUtils.isEmpty(this.mSubtitle));
            LinearLayout linearLayout = this.mTitleLayout;
            if (visible) {
                i = 0;
            }
            linearLayout.setVisibility(i);
        }
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    public void setHomeButtonEnabled(boolean enable) {
        setHomeButtonEnabled(enable, true);
    }

    /* access modifiers changed from: private */
    public void setHomeButtonEnabled(boolean enable, boolean recordState) {
        if (recordState) {
            this.mWasHomeEnabled = enable;
        }
        if (this.mExpandedActionView == null) {
            this.mUpGoerFive.setEnabled(enable);
            this.mUpGoerFive.setFocusable(enable);
            updateHomeAccessibility(enable);
        }
    }

    private void updateHomeAccessibility(boolean homeEnabled) {
        if (!homeEnabled) {
            this.mUpGoerFive.setContentDescription(null);
            this.mUpGoerFive.setImportantForAccessibility(2);
            return;
        }
        this.mUpGoerFive.setImportantForAccessibility(0);
        this.mUpGoerFive.setContentDescription(buildHomeContentDescription());
    }

    private CharSequence buildHomeContentDescription() {
        CharSequence homeDesc;
        String result;
        if (this.mHomeDescription != null) {
            homeDesc = this.mHomeDescription;
        } else if ((this.mDisplayOptions & 4) != 0) {
            homeDesc = this.mContext.getResources().getText(this.mDefaultUpDescription);
        } else {
            homeDesc = this.mContext.getResources().getText(17039535);
        }
        CharSequence title = getTitle();
        CharSequence subtitle = getSubtitle();
        if (TextUtils.isEmpty(title)) {
            return homeDesc;
        }
        if (!TextUtils.isEmpty(subtitle)) {
            result = getResources().getString(17039537, new Object[]{title, subtitle, homeDesc});
        } else {
            result = getResources().getString(17039536, new Object[]{title, homeDesc});
        }
        return result;
    }

    public void setDisplayOptions(int options) {
        int i = -1;
        if (this.mDisplayOptions != -1) {
            i = options ^ this.mDisplayOptions;
        }
        int flagsChanged = i;
        this.mDisplayOptions = options;
        if ((flagsChanged & 63) != 0) {
            if ((flagsChanged & 4) != 0) {
                boolean setUp = (options & 4) != 0;
                this.mHomeLayout.setShowUp(setUp);
                if (setUp) {
                    setHomeButtonEnabled(true);
                }
            }
            if (flagsChanged != false && true) {
                this.mHomeLayout.setIcon(this.mLogo != null && (options & 1) != 0 ? this.mLogo : this.mIcon);
            }
            if (flagsChanged != false && true) {
                if ((options & 8) != 0) {
                    initTitle();
                } else {
                    this.mUpGoerFive.removeView(this.mTitleLayout);
                }
            }
            boolean showHome = (options & 2) != 0;
            boolean titleUp = !showHome && ((this.mDisplayOptions & 4) != 0);
            this.mHomeLayout.setShowIcon(showHome);
            this.mHomeLayout.setVisibility(((showHome || titleUp) && this.mExpandedActionView == null) ? 0 : 8);
            if (!((flagsChanged & 16) == 0 || this.mCustomNavView == null)) {
                if ((options & 16) != 0) {
                    addView(this.mCustomNavView);
                } else {
                    removeView(this.mCustomNavView);
                }
            }
            if (!(this.mTitleLayout == null || (flagsChanged & 32) == 0)) {
                if ((options & 32) != 0) {
                    this.mTitleView.setSingleLine(false);
                    this.mTitleView.setMaxLines(2);
                } else {
                    this.mTitleView.setMaxLines(1);
                    this.mTitleView.setSingleLine(true);
                }
            }
            requestLayout();
        } else {
            invalidate();
        }
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        if (icon != null && ((this.mDisplayOptions & 1) == 0 || this.mLogo == null)) {
            this.mHomeLayout.setIcon(icon);
        }
        if (this.mExpandedActionView != null) {
            if (this.mExpandedHomeLayout == null) {
                initExpandedHomeLayout();
            }
            this.mExpandedHomeLayout.setIcon(this.mIcon.getConstantState().newDrawable(getResources()));
        }
    }

    public void setIcon(int resId) {
        setIcon(resId != 0 ? this.mContext.getDrawable(resId) : null);
    }

    public boolean hasIcon() {
        return this.mIcon != null;
    }

    public void setLogo(Drawable logo) {
        this.mLogo = logo;
        if (logo != null && (this.mDisplayOptions & 1) != 0) {
            this.mHomeLayout.setIcon(logo);
        }
    }

    public void setLogo(int resId) {
        setLogo(resId != 0 ? this.mContext.getDrawable(resId) : null);
    }

    public boolean hasLogo() {
        return this.mLogo != null;
    }

    public void setNavigationMode(int mode) {
        int oldMode = this.mNavigationMode;
        if (mode != oldMode) {
            switch (oldMode) {
                case 1:
                    if (this.mListNavLayout != null) {
                        removeView(this.mListNavLayout);
                        break;
                    }
                    break;
                case 2:
                    if (this.mTabScrollView != null && this.mIncludeTabs) {
                        removeView(this.mTabScrollView);
                        break;
                    }
            }
            switch (mode) {
                case 1:
                    if (this.mSpinner == null) {
                        this.mSpinner = new Spinner(this.mContext, null, 16843479);
                        this.mSpinner.setId(16908694);
                        this.mListNavLayout = new LinearLayout(this.mContext, null, 16843508);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
                        params.gravity = 17;
                        this.mListNavLayout.addView(this.mSpinner, params);
                    }
                    if (this.mSpinner.getAdapter() != this.mSpinnerAdapter) {
                        this.mSpinner.setAdapter(this.mSpinnerAdapter);
                    }
                    this.mSpinner.setOnItemSelectedListener(this.mNavItemSelectedListener);
                    addView(this.mListNavLayout);
                    break;
                case 2:
                    if (this.mTabScrollView != null && this.mIncludeTabs) {
                        addView(this.mTabScrollView);
                        break;
                    }
            }
            this.mNavigationMode = mode;
            requestLayout();
        }
    }

    public void setDropdownParams(SpinnerAdapter adapter, AdapterView.OnItemSelectedListener l) {
        this.mSpinnerAdapter = adapter;
        this.mNavItemSelectedListener = l;
        if (this.mSpinner != null) {
            this.mSpinner.setAdapter(adapter);
            this.mSpinner.setOnItemSelectedListener(l);
        }
    }

    public int getDropdownItemCount() {
        if (this.mSpinnerAdapter != null) {
            return this.mSpinnerAdapter.getCount();
        }
        return 0;
    }

    public void setDropdownSelectedPosition(int position) {
        this.mSpinner.setSelection(position);
    }

    public int getDropdownSelectedPosition() {
        return this.mSpinner.getSelectedItemPosition();
    }

    public View getCustomView() {
        return this.mCustomNavView;
    }

    public int getNavigationMode() {
        return this.mNavigationMode;
    }

    public int getDisplayOptions() {
        return this.mDisplayOptions;
    }

    public ViewGroup getViewGroup() {
        return this;
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ActionBar.LayoutParams(DEFAULT_CUSTOM_GRAVITY);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mUpGoerFive.addView(this.mHomeLayout, 0);
        addView(this.mUpGoerFive);
        if (this.mCustomNavView != null && (this.mDisplayOptions & 16) != 0) {
            ViewParent parent = this.mCustomNavView.getParent();
            if (parent != this) {
                if (parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(this.mCustomNavView);
                }
                addView(this.mCustomNavView);
            }
        }
    }

    /* access modifiers changed from: private */
    public void initTitle() {
        if (this.mTitleLayout == null) {
            this.mTitleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(17367068, this, false);
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(16908696);
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(16908695);
            if (this.mTitleStyleRes != 0) {
                this.mTitleView.setTextAppearance(this.mTitleStyleRes);
            }
            if (this.mTitle != null) {
                this.mTitleView.setText(this.mTitle);
            }
            if (this.mSubtitleStyleRes != 0) {
                this.mSubtitleView.setTextAppearance(this.mSubtitleStyleRes);
            }
            if (this.mSubtitle != null) {
                this.mSubtitleView.setText(this.mSubtitle);
                this.mSubtitleView.setVisibility(0);
            }
        }
        this.mUpGoerFive.addView(this.mTitleLayout);
        if (this.mExpandedActionView != null || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) {
            this.mTitleLayout.setVisibility(8);
        } else {
            this.mTitleLayout.setVisibility(0);
        }
    }

    public void setContextView(ActionBarContextView view) {
        this.mContextView = view;
    }

    public void setCollapsible(boolean collapsible) {
        this.mIsCollapsible = collapsible;
    }

    public boolean isTitleTruncated() {
        if (this.mTitleView == null) {
            return false;
        }
        Layout titleLayout = this.mTitleView.getLayout();
        if (titleLayout == null) {
            return false;
        }
        int lineCount = titleLayout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            if (titleLayout.getEllipsisCount(i) > 0) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int homeWidthSpec;
        int availableWidth;
        int horizontalMargin;
        int customNavHeightMode;
        int childCount = getChildCount();
        if (this.mIsCollapsible) {
            int visibleChildren = 0;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (!(child.getVisibility() == 8 || ((child == this.mMenuView && this.mMenuView.getChildCount() == 0) || child == this.mUpGoerFive))) {
                    visibleChildren++;
                }
            }
            int upChildCount = this.mUpGoerFive.getChildCount();
            int visibleChildren2 = visibleChildren;
            for (int i2 = 0; i2 < upChildCount; i2++) {
                if (this.mUpGoerFive.getChildAt(i2).getVisibility() != 8) {
                    visibleChildren2++;
                }
            }
            if (visibleChildren2 == 0) {
                setMeasuredDimension(0, 0);
                return;
            }
        }
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == 1073741824) {
            int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
            if (heightMode == Integer.MIN_VALUE) {
                int contentWidth = View.MeasureSpec.getSize(widthMeasureSpec);
                int maxHeight = this.mContentHeight >= 0 ? this.mContentHeight : View.MeasureSpec.getSize(heightMeasureSpec);
                int verticalPadding = getPaddingTop() + getPaddingBottom();
                int paddingLeft = getPaddingLeft();
                int paddingRight = getPaddingRight();
                int height = maxHeight - verticalPadding;
                int childSpecHeight = View.MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
                int exactHeightSpec = View.MeasureSpec.makeMeasureSpec(height, 1073741824);
                int availableWidth2 = (contentWidth - paddingLeft) - paddingRight;
                int leftOfCenter = availableWidth2 / 2;
                int rightOfCenter = leftOfCenter;
                boolean showTitle = (this.mTitleLayout == null || this.mTitleLayout.getVisibility() == 8 || (this.mDisplayOptions & 8) == 0) ? false : true;
                HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
                ViewGroup.LayoutParams homeLp = homeLayout.getLayoutParams();
                int i3 = widthMode;
                if (homeLp.width < 0) {
                    homeWidthSpec = View.MeasureSpec.makeMeasureSpec(availableWidth2, Integer.MIN_VALUE);
                    ViewGroup.LayoutParams layoutParams = homeLp;
                } else {
                    ViewGroup.LayoutParams layoutParams2 = homeLp;
                    homeWidthSpec = View.MeasureSpec.makeMeasureSpec(homeLp.width, 1073741824);
                }
                int homeWidthSpec2 = homeWidthSpec;
                homeLayout.measure(homeWidthSpec2, exactHeightSpec);
                int i4 = homeWidthSpec2;
                int homeWidth = 0;
                if ((homeLayout.getVisibility() == 8 || homeLayout.getParent() != this.mUpGoerFive) && !showTitle) {
                    int i5 = heightMode;
                    availableWidth = availableWidth2;
                } else {
                    int homeWidth2 = homeLayout.getMeasuredWidth();
                    int homeOffsetWidth = homeLayout.getStartOffset() + homeWidth2;
                    int homeWidth3 = homeWidth2;
                    int i6 = availableWidth2;
                    availableWidth = Math.max(0, availableWidth2 - homeOffsetWidth);
                    int i7 = heightMode;
                    leftOfCenter = Math.max(0, availableWidth - homeOffsetWidth);
                    homeWidth = homeWidth3;
                }
                if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                    availableWidth = measureChildView(this.mMenuView, availableWidth, exactHeightSpec, 0);
                    rightOfCenter = Math.max(0, rightOfCenter - this.mMenuView.getMeasuredWidth());
                }
                if (!(this.mIndeterminateProgressView == null || this.mIndeterminateProgressView.getVisibility() == 8)) {
                    availableWidth = measureChildView(this.mIndeterminateProgressView, availableWidth, childSpecHeight, 0);
                    rightOfCenter = Math.max(0, rightOfCenter - this.mIndeterminateProgressView.getMeasuredWidth());
                }
                int rightOfCenter2 = rightOfCenter;
                if (this.mExpandedActionView == null) {
                    switch (this.mNavigationMode) {
                        case 1:
                            int i8 = paddingLeft;
                            int i9 = paddingRight;
                            if (this.mListNavLayout != null) {
                                int itemPaddingSize = showTitle ? this.mItemPadding * 2 : this.mItemPadding;
                                int availableWidth3 = Math.max(0, availableWidth - itemPaddingSize);
                                int leftOfCenter2 = Math.max(0, leftOfCenter - itemPaddingSize);
                                int i10 = itemPaddingSize;
                                this.mListNavLayout.measure(View.MeasureSpec.makeMeasureSpec(availableWidth3, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
                                int listNavWidth = this.mListNavLayout.getMeasuredWidth();
                                availableWidth = Math.max(0, availableWidth3 - listNavWidth);
                                leftOfCenter = Math.max(0, leftOfCenter2 - listNavWidth);
                                break;
                            }
                            break;
                        case 2:
                            if (this.mTabScrollView != null) {
                                int itemPaddingSize2 = showTitle ? this.mItemPadding * 2 : this.mItemPadding;
                                HomeView homeView = homeLayout;
                                int availableWidth4 = Math.max(0, availableWidth - itemPaddingSize2);
                                int leftOfCenter3 = Math.max(0, leftOfCenter - itemPaddingSize2);
                                int i11 = itemPaddingSize2;
                                int i12 = paddingLeft;
                                int i13 = paddingRight;
                                this.mTabScrollView.measure(View.MeasureSpec.makeMeasureSpec(availableWidth4, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
                                int tabWidth = this.mTabScrollView.getMeasuredWidth();
                                availableWidth = Math.max(0, availableWidth4 - tabWidth);
                                leftOfCenter = Math.max(0, leftOfCenter3 - tabWidth);
                                break;
                            }
                    }
                }
                int i14 = paddingLeft;
                int i15 = paddingRight;
                int leftOfCenter4 = leftOfCenter;
                View customView = null;
                if (this.mExpandedActionView != null) {
                    customView = this.mExpandedActionView;
                } else if (!((this.mDisplayOptions & 16) == 0 || this.mCustomNavView == null)) {
                    customView = this.mCustomNavView;
                }
                if (customView != null) {
                    ViewGroup.LayoutParams lp = generateLayoutParams(customView.getLayoutParams());
                    ActionBar.LayoutParams ablp = lp instanceof ActionBar.LayoutParams ? (ActionBar.LayoutParams) lp : null;
                    int verticalMargin = 0;
                    if (ablp != null) {
                        boolean z = showTitle;
                        verticalMargin = ablp.topMargin + ablp.bottomMargin;
                        horizontalMargin = ablp.rightMargin + ablp.leftMargin;
                    } else {
                        horizontalMargin = 0;
                    }
                    if (this.mContentHeight <= 0) {
                        customNavHeightMode = Integer.MIN_VALUE;
                    } else {
                        customNavHeightMode = lp.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                    }
                    int i16 = height;
                    int customNavHeight = Math.max(0, (lp.height >= 0 ? Math.min(lp.height, height) : height) - verticalMargin);
                    int i17 = childSpecHeight;
                    int customNavWidthMode = lp.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                    int i18 = exactHeightSpec;
                    int customNavWidth = Math.max(0, (lp.width >= 0 ? Math.min(lp.width, availableWidth) : availableWidth) - horizontalMargin);
                    ActionBar.LayoutParams layoutParams3 = ablp;
                    if (((ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY) & 7) == 1) {
                        ViewGroup.LayoutParams layoutParams4 = lp;
                        if (lp.width == -1) {
                            customNavWidth = Math.min(leftOfCenter4, rightOfCenter2) * 2;
                        }
                    }
                    customView.measure(View.MeasureSpec.makeMeasureSpec(customNavWidth, customNavWidthMode), View.MeasureSpec.makeMeasureSpec(customNavHeight, customNavHeightMode));
                    availableWidth -= horizontalMargin + customView.getMeasuredWidth();
                } else {
                    int i19 = height;
                    int i20 = childSpecHeight;
                    int i21 = exactHeightSpec;
                }
                int paddedViewHeight = 0;
                int availableWidth5 = measureChildView(this.mUpGoerFive, availableWidth + homeWidth, View.MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824), 0);
                if (this.mTitleLayout != null) {
                    int leftOfCenter5 = Math.max(0, leftOfCenter4 - this.mTitleLayout.getMeasuredWidth());
                }
                if (this.mContentHeight <= 0) {
                    int measuredHeight = 0;
                    while (true) {
                        int i22 = paddedViewHeight;
                        if (i22 < childCount) {
                            int paddedViewHeight2 = getChildAt(i22).getMeasuredHeight() + verticalPadding;
                            if (paddedViewHeight2 > measuredHeight) {
                                measuredHeight = paddedViewHeight2;
                            }
                            paddedViewHeight = i22 + 1;
                        } else {
                            setMeasuredDimension(contentWidth, measuredHeight);
                        }
                    }
                } else {
                    setMeasuredDimension(contentWidth, maxHeight);
                }
                if (this.mContextView != null) {
                    this.mContextView.setContentHeight(getMeasuredHeight());
                }
                if (!(this.mProgressView == null || this.mProgressView.getVisibility() == 8)) {
                    this.mProgressView.measure(View.MeasureSpec.makeMeasureSpec(contentWidth - (this.mProgressBarPadding * 2), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Integer.MIN_VALUE));
                }
                return;
            }
            int i23 = heightMode;
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with android:layout_height=\"wrap_content\"");
        }
        throw new IllegalStateException(getClass().getSimpleName() + " can only be used with android:layout_width=\"match_parent\" (or fill_parent)");
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int x;
        int topMargin;
        int x2;
        int centeredStart;
        int contentHeight = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (contentHeight > 0) {
            boolean isLayoutRtl = isLayoutRtl();
            int direction = isLayoutRtl ? 1 : -1;
            int menuStart = isLayoutRtl ? getPaddingLeft() : (r - l) - getPaddingRight();
            int x3 = isLayoutRtl ? (r - l) - getPaddingRight() : getPaddingLeft();
            int y = getPaddingTop();
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            boolean showTitle = (this.mTitleLayout == null || this.mTitleLayout.getVisibility() == 8 || (this.mDisplayOptions & 8) == 0) ? false : true;
            int startOffset = 0;
            if (homeLayout.getParent() == this.mUpGoerFive) {
                if (homeLayout.getVisibility() != 8) {
                    startOffset = homeLayout.getStartOffset();
                } else if (showTitle) {
                    startOffset = homeLayout.getUpWidth();
                }
            }
            int startOffset2 = startOffset;
            HomeView homeView = homeLayout;
            int x4 = next(x3 + positionChild(this.mUpGoerFive, next(x3, startOffset2, isLayoutRtl), y, contentHeight, isLayoutRtl), startOffset2, isLayoutRtl);
            if (this.mExpandedActionView == null) {
                switch (this.mNavigationMode) {
                    case 1:
                        if (this.mListNavLayout != null) {
                            if (showTitle) {
                                x4 = next(x4, this.mItemPadding, isLayoutRtl);
                            }
                            int x5 = x4;
                            x4 = next(x5 + positionChild(this.mListNavLayout, x5, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            break;
                        }
                        break;
                    case 2:
                        if (this.mTabScrollView != null) {
                            if (showTitle) {
                                x4 = next(x4, this.mItemPadding, isLayoutRtl);
                            }
                            int x6 = x4;
                            x4 = next(x6 + positionChild(this.mTabScrollView, x6, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            break;
                        }
                        break;
                }
            }
            int x7 = x4;
            if (this.mMenuView == null || this.mMenuView.getParent() != this) {
                x = x7;
            } else {
                x = x7;
                positionChild(this.mMenuView, menuStart, y, contentHeight, !isLayoutRtl);
                menuStart += this.mMenuView.getMeasuredWidth() * direction;
            }
            if (!(this.mIndeterminateProgressView == null || this.mIndeterminateProgressView.getVisibility() == 8)) {
                positionChild(this.mIndeterminateProgressView, menuStart, y, contentHeight, !isLayoutRtl);
                menuStart += this.mIndeterminateProgressView.getMeasuredWidth() * direction;
            }
            View customView = null;
            if (this.mExpandedActionView != null) {
                customView = this.mExpandedActionView;
            } else if (!((this.mDisplayOptions & 16) == 0 || this.mCustomNavView == null)) {
                customView = this.mCustomNavView;
            }
            if (customView != null) {
                int layoutDirection = getLayoutDirection();
                ViewGroup.LayoutParams lp = customView.getLayoutParams();
                ActionBar.LayoutParams ablp = lp instanceof ActionBar.LayoutParams ? (ActionBar.LayoutParams) lp : null;
                int gravity = ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY;
                int navWidth = customView.getMeasuredWidth();
                int topMargin2 = 0;
                if (ablp != null) {
                    ViewGroup.LayoutParams layoutParams = lp;
                    int x8 = next(x, ablp.getMarginStart(), isLayoutRtl);
                    menuStart += ablp.getMarginEnd() * direction;
                    int topMargin3 = ablp.topMargin;
                    int x9 = x8;
                    x2 = ablp.bottomMargin;
                    topMargin2 = topMargin3;
                    topMargin = x9;
                } else {
                    topMargin = x;
                    x2 = 0;
                }
                ActionBar.LayoutParams layoutParams2 = ablp;
                int hgravity = gravity & 8388615;
                if (hgravity == 1) {
                    int hgravity2 = hgravity;
                    int centeredLeft = ((this.mRight - this.mLeft) - navWidth) / 2;
                    if (isLayoutRtl) {
                        int centeredStart2 = centeredLeft + navWidth;
                        int centeredEnd = centeredLeft;
                        if (centeredStart2 > topMargin) {
                            hgravity2 = 5;
                        } else {
                            int i = centeredStart2;
                            if (centeredEnd < menuStart) {
                                hgravity2 = 3;
                            }
                        }
                    } else {
                        int centeredEnd2 = centeredLeft + navWidth;
                        if (centeredLeft < topMargin) {
                            centeredStart = 3;
                        } else if (centeredEnd2 > menuStart) {
                            centeredStart = 5;
                        }
                    }
                    centeredStart = hgravity2;
                } else {
                    centeredStart = gravity == 0 ? 8388611 : hgravity;
                }
                int xpos = 0;
                int absoluteGravity = Gravity.getAbsoluteGravity(centeredStart, layoutDirection);
                int i2 = layoutDirection;
                if (absoluteGravity == 1) {
                    xpos = ((this.mRight - this.mLeft) - navWidth) / 2;
                } else if (absoluteGravity == 3) {
                    xpos = isLayoutRtl ? menuStart : topMargin;
                } else if (absoluteGravity == 5) {
                    xpos = isLayoutRtl ? topMargin - navWidth : menuStart - navWidth;
                }
                int vgravity = gravity & 112;
                if (gravity == 0) {
                    vgravity = 16;
                }
                int ypos = 0;
                int i3 = centeredStart;
                if (vgravity != 16) {
                    if (vgravity == 48) {
                        ypos = getPaddingTop() + topMargin2;
                    } else if (vgravity == 80) {
                        ypos = ((getHeight() - getPaddingBottom()) - customView.getMeasuredHeight()) - x2;
                    }
                    int bottomMargin = x2;
                } else {
                    int i4 = vgravity;
                    int i5 = x2;
                    ypos = ((((this.mBottom - this.mTop) - getPaddingBottom()) - getPaddingTop()) - customView.getMeasuredHeight()) / 2;
                }
                int customWidth = customView.getMeasuredWidth();
                customView.layout(xpos, ypos, xpos + customWidth, customView.getMeasuredHeight() + ypos);
                int x10 = next(topMargin, customWidth, isLayoutRtl);
            } else {
                int i6 = x;
            }
            if (this.mProgressView != null) {
                this.mProgressView.bringToFront();
                int halfProgressHeight = this.mProgressView.getMeasuredHeight() / 2;
                this.mProgressView.layout(this.mProgressBarPadding, -halfProgressHeight, this.mProgressBarPadding + this.mProgressView.getMeasuredWidth(), halfProgressHeight);
            }
        }
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ActionBar.LayoutParams(getContext(), attrs);
    }

    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp == null) {
            return generateDefaultLayoutParams();
        }
        return lp;
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        if (!(this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null)) {
            state.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        state.isOverflowOpen = isOverflowMenuShowing();
        return state;
    }

    public void onRestoreInstanceState(Parcelable p) {
        SavedState state = (SavedState) p;
        super.onRestoreInstanceState(state.getSuperState());
        if (!(state.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || this.mOptionsMenu == null)) {
            MenuItem item = this.mOptionsMenu.findItem(state.expandedMenuItemId);
            if (item != null) {
                item.expandActionView();
            }
        }
        if (state.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    public void setNavigationIcon(Drawable indicator) {
        this.mHomeLayout.setUpIndicator(indicator);
    }

    public void setDefaultNavigationIcon(Drawable icon) {
        this.mHomeLayout.setDefaultUpIndicator(icon);
    }

    public void setNavigationIcon(int resId) {
        this.mHomeLayout.setUpIndicator(resId);
    }

    public void setNavigationContentDescription(CharSequence description) {
        this.mHomeDescription = description;
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    public void setNavigationContentDescription(int resId) {
        this.mHomeDescriptionRes = resId;
        this.mHomeDescription = resId != 0 ? getResources().getText(resId) : null;
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    public void setDefaultNavigationContentDescription(int defaultNavigationContentDescription) {
        if (this.mDefaultUpDescription != defaultNavigationContentDescription) {
            this.mDefaultUpDescription = defaultNavigationContentDescription;
            updateHomeAccessibility(this.mUpGoerFive.isEnabled());
        }
    }

    public void setMenuCallbacks(MenuPresenter.Callback presenterCallback, MenuBuilder.Callback menuBuilderCallback) {
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.setCallback(presenterCallback);
        }
        if (this.mOptionsMenu != null) {
            this.mOptionsMenu.setCallback(menuBuilderCallback);
        }
    }

    public Menu getMenu() {
        return this.mOptionsMenu;
    }

    @Deprecated
    public void setCustomTitle(View view) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public LinearLayout initTitleLayout() {
        return (LinearLayout) LayoutInflater.from(getContext()).inflate(17367068, this, false);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public TextView getTitleView() {
        return this.mTitleView;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public TextView getSubTitleView() {
        return this.mSubtitleView;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void deleteExpandedHomeIfNeed() {
    }

    /* access modifiers changed from: protected */
    public void initExpandedHomeLayout() {
        this.mExpandedHomeLayout = (HomeView) LayoutInflater.from(this.mContext).inflate(this.mHomeResId, this.mUpGoerFive, false);
        this.mExpandedHomeLayout.setShowUp(true);
        this.mExpandedHomeLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MenuItemImpl item = ActionBarView.this.mExpandedMenuPresenter.mCurrentExpandedItem;
                if (item != null) {
                    item.collapseActionView();
                }
            }
        });
        this.mExpandedHomeLayout.setContentDescription(getResources().getText(this.mDefaultUpDescription));
        Drawable upBackground = this.mUpGoerFive.getBackground();
        if (upBackground != null) {
            this.mExpandedHomeLayout.setBackground(upBackground.getConstantState().newDrawable());
        }
        this.mExpandedHomeLayout.setEnabled(true);
        this.mExpandedHomeLayout.setFocusable(true);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public ViewGroup getUpGoerFive() {
        return this.mUpGoerFive;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public HomeView getHomeLayout() {
        return this.mHomeLayout;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public HomeView getExpandedHomeLayout() {
        return this.mExpandedHomeLayout;
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public ActionMenuPresenter initActionMenuPresenter(Context context) {
        return new ActionMenuPresenter(context);
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void initTitleAppearance() {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public View.OnClickListener getUpClickListener() {
        return this.mUpClickListener;
    }
}
