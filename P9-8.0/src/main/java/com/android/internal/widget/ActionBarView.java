package com.android.internal.widget;

import android.animation.LayoutTransition;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
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
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window.Callback;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.SubMenuBuilder;

public class ActionBarView extends AbsActionBarView implements DecorToolbar {
    private static final int DEFAULT_CUSTOM_GRAVITY = 8388627;
    public static final int DISPLAY_DEFAULT = 0;
    private static final int DISPLAY_RELAYOUT_MASK = 63;
    private static final String TAG = "ActionBarView";
    private ActionBarContextView mContextView;
    protected View mCustTitle;
    private View mCustomNavView;
    private int mDefaultUpDescription = R.string.action_bar_up_description;
    private int mDisplayOptions = -1;
    public View mExpandedActionView;
    private final OnClickListener mExpandedActionViewUpListener = new OnClickListener() {
        public void onClick(View v) {
            MenuItemImpl item = ActionBarView.this.mExpandedMenuPresenter.mCurrentExpandedItem;
            if (item != null) {
                item.collapseActionView();
            }
        }
    };
    private HomeView mExpandedHomeLayout;
    public ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private CharSequence mHomeDescription;
    private int mHomeDescriptionRes;
    private HomeView mHomeLayout;
    private int mHomeResId;
    private Drawable mIcon;
    private boolean mIncludeTabs;
    private final int mIndeterminateProgressStyle;
    private ProgressBar mIndeterminateProgressView;
    private boolean mIsCollapsible;
    private int mItemPadding;
    private LinearLayout mListNavLayout;
    private Drawable mLogo;
    private ActionMenuItem mLogoNavItem;
    private boolean mMenuPrepared;
    private OnItemSelectedListener mNavItemSelectedListener;
    private int mNavigationMode;
    private MenuBuilder mOptionsMenu;
    private int mProgressBarPadding;
    private final int mProgressStyle;
    private ProgressBar mProgressView;
    private Spinner mSpinner;
    private SpinnerAdapter mSpinnerAdapter;
    private CharSequence mSubtitle;
    private final int mSubtitleStyleRes;
    private TextView mSubtitleView;
    private ScrollingTabContainerView mTabScrollView;
    private Runnable mTabSelector;
    private CharSequence mTitle;
    protected LinearLayout mTitleLayout;
    private final int mTitleStyleRes;
    private TextView mTitleView;
    private final OnClickListener mUpClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (ActionBarView.this.mMenuPrepared) {
                ActionBarView.this.mWindowCallback.onMenuItemSelected(0, ActionBarView.this.mLogoNavItem);
            }
        }
    };
    private ViewGroup mUpGoerFive;
    private boolean mUserTitle;
    private boolean mWasHomeEnabled;
    Callback mWindowCallback;

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        /* synthetic */ ExpandedActionViewMenuPresenter(ActionBarView this$0, ExpandedActionViewMenuPresenter -this1) {
            this();
        }

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
                    for (int i = 0; i < count; i++) {
                        if (this.mMenu.getItem(i) == this.mCurrentExpandedItem) {
                            found = true;
                            break;
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
                ActionBarView.this.deleteExpandedHomeIfNeed();
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

        protected void onConfigurationChanged(Configuration newConfig) {
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
            return -wrap9(event);
        }

        protected void onFinishInflate() {
            this.mUpView = (ImageView) findViewById(R.id.up);
            this.mIconView = (ImageView) findViewById(R.id.home);
            this.mDefaultUpIndicator = this.mUpView.getDrawable();
        }

        public int getStartOffset() {
            return this.mUpView.getVisibility() == 8 ? this.mStartOffset : 0;
        }

        public int getUpWidth() {
            return this.mUpWidth;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            measureChildWithMargins(this.mUpView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            LayoutParams upLp = (LayoutParams) this.mUpView.getLayoutParams();
            int upMargins = upLp.leftMargin + upLp.rightMargin;
            this.mUpWidth = this.mUpView.getMeasuredWidth();
            this.mStartOffset = this.mUpWidth + upMargins;
            int width = this.mUpView.getVisibility() == 8 ? 0 : this.mStartOffset;
            int height = (upLp.topMargin + this.mUpView.getMeasuredHeight()) + upLp.bottomMargin;
            if (this.mIconView.getVisibility() != 8) {
                measureChildWithMargins(this.mIconView, widthMeasureSpec, width, heightMeasureSpec, 0);
                LayoutParams iconLp = (LayoutParams) this.mIconView.getLayoutParams();
                width += (iconLp.leftMargin + this.mIconView.getMeasuredWidth()) + iconLp.rightMargin;
                height = Math.max(height, (iconLp.topMargin + this.mIconView.getMeasuredHeight()) + iconLp.bottomMargin);
            } else if (upMargins < 0) {
                width -= upMargins;
            }
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            switch (widthMode) {
                case Integer.MIN_VALUE:
                    width = Math.min(width, widthSize);
                    break;
                case 1073741824:
                    width = widthSize;
                    break;
            }
            switch (heightMode) {
                case Integer.MIN_VALUE:
                    height = Math.min(height, heightSize);
                    break;
                case 1073741824:
                    height = heightSize;
                    break;
            }
            -wrap6(width, height);
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int iconRight;
            int iconLeft;
            int vCenter = (b - t) / 2;
            boolean isLayoutRtl = isRtlLocale();
            int width = getWidth();
            int upOffset = 0;
            if (this.mUpView.getVisibility() != 8) {
                int upRight;
                int upLeft;
                int i;
                LayoutParams upLp = (LayoutParams) this.mUpView.getLayoutParams();
                int upHeight = this.mUpView.getMeasuredHeight();
                int upWidth = this.mUpView.getMeasuredWidth();
                upOffset = (upLp.leftMargin + upWidth) + upLp.rightMargin;
                int upTop = vCenter - (upHeight / 2);
                int upBottom = upTop + upHeight;
                if (isLayoutRtl) {
                    upRight = width;
                    upLeft = width - upWidth;
                    r -= upOffset;
                } else {
                    upRight = upWidth;
                    upLeft = 0;
                    l += upOffset;
                }
                View view = this.mUpView;
                if (isLayoutRtl) {
                    i = upLp.rightMargin;
                } else {
                    i = upLp.leftMargin;
                }
                layoutUpView(view, upLeft, upTop, upRight, upBottom, i, upOffset);
            }
            LayoutParams iconLp = (LayoutParams) this.mIconView.getLayoutParams();
            int iconHeight = this.mIconView.getMeasuredHeight();
            int iconWidth = this.mIconView.getMeasuredWidth();
            int hCenter = (r - l) / 2;
            int iconTop = Math.max(iconLp.topMargin, vCenter - (iconHeight / 2));
            int iconBottom = iconTop + iconHeight;
            int delta = Math.max(iconLp.getMarginStart(), hCenter - (iconWidth / 2));
            if (isLayoutRtl) {
                iconRight = (width - upOffset) - delta;
                iconLeft = iconRight - iconWidth;
            } else {
                iconLeft = upOffset + delta;
                iconRight = iconLeft + iconWidth;
            }
            this.mIconView.layout(iconLeft, iconTop, iconRight, iconBottom);
        }

        protected void layoutUpView(View view, int upLeft, int upTop, int upRight, int upBottom, int leftMargin, int upOffset) {
            view.layout(upLeft, upTop, upRight, upBottom);
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int expandedMenuItemId;
        boolean isOverflowOpen;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = false;
            super(in);
            this.expandedMenuItemId = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            }
            this.isOverflowOpen = z;
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
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        this.mNavigationMode = a.getInt(7, 0);
        this.mTitle = a.getText(5);
        this.mSubtitle = a.getText(9);
        this.mLogo = a.getDrawable(6);
        this.mIcon = a.getDrawable(0);
        LayoutInflater inflater = LayoutInflater.from(context);
        this.mHomeResId = a.getResourceId(16, R.layout.action_bar_home);
        this.mUpGoerFive = (ViewGroup) inflater.inflate((int) R.layout.action_bar_up_container, (ViewGroup) this, false);
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
            this.mCustomNavView = inflater.inflate(customNavId, (ViewGroup) this, false);
            this.mNavigationMode = 0;
            setDisplayOptions(this.mDisplayOptions | 16);
        }
        this.mContentHeight = a.getLayoutDimension(4, 0);
        a.recycle();
        this.mLogoNavItem = new ActionMenuItem(context, 0, R.id.home, 0, 0, this.mTitle);
        this.mUpGoerFive.setOnClickListener(this.mUpClickListener);
        this.mUpGoerFive.setClickable(true);
        this.mUpGoerFive.setFocusable(true);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
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

    public void setWindowCallback(Callback cb) {
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
        this.mProgressView.setId(R.id.progress_horizontal);
        this.mProgressView.setMax(10000);
        this.mProgressView.setVisibility(8);
        addView(this.mProgressView);
    }

    public void initIndeterminateProgress() {
        this.mIndeterminateProgressView = new ProgressBar(this.mContext, null, 0, this.mIndeterminateProgressStyle);
        this.mIndeterminateProgressView.setId(R.id.progress_circular);
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
                this.mMenuView.setVisibility(getAnimatedVisibility());
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
                if (splitActionBar) {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                    this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                    this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                } else {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(R.bool.action_bar_expanded_action_views_exclusive));
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

    protected void updateSplitLocation() {
    }

    public void setMenuPrepared() {
        this.mMenuPrepared = true;
    }

    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        if (menu != this.mOptionsMenu) {
            ViewGroup oldParent;
            ActionMenuView menuView;
            if (this.mOptionsMenu != null) {
                this.mOptionsMenu.removeMenuPresenter(this.mActionMenuPresenter);
                this.mOptionsMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
            }
            MenuBuilder builder = (MenuBuilder) menu;
            this.mOptionsMenu = builder;
            if (this.mMenuView != null) {
                oldParent = (ViewGroup) this.mMenuView.getParent();
                if (oldParent != null) {
                    oldParent.removeView(this.mMenuView);
                }
            }
            if (this.mActionMenuPresenter == null) {
                this.mActionMenuPresenter = initActionMenuPresenter(this.mContext);
                this.mActionMenuPresenter.setCallback(cb);
                this.mActionMenuPresenter.setId(R.id.action_menu_presenter);
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter(this, null);
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
            if (this.mSplitActionBar) {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                layoutParams.width = -1;
                layoutParams.height = -2;
                configPresenters(builder);
                View menuView2 = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                if (this.mSplitView != null) {
                    oldParent = (ViewGroup) menuView2.getParent();
                    if (!(oldParent == null || oldParent == this.mSplitView)) {
                        oldParent.removeView(menuView2);
                    }
                    menuView2.setVisibility(getAnimatedVisibility());
                    this.mSplitView.addView(menuView2, layoutParams);
                } else {
                    menuView2.-wrap18(layoutParams);
                }
            } else {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(R.bool.action_bar_expanded_action_views_exclusive));
                configPresenters(builder);
                menuView2 = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                oldParent = (ViewGroup) menuView2.getParent();
                if (!(oldParent == null || oldParent == this)) {
                    oldParent.removeView(menuView2);
                }
                addView((View) menuView2, layoutParams);
            }
            this.mMenuView = menuView2;
            updateSplitLocation();
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
        if (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) {
            return false;
        }
        return true;
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
        int i = 0;
        this.mTitle = title;
        if (this.mTitleView != null) {
            this.mTitleView.setText(title);
            int visible = (this.mExpandedActionView != null || (this.mDisplayOptions & 8) == 0) ? 0 : TextUtils.isEmpty(this.mTitle) ? TextUtils.isEmpty(this.mSubtitle) ^ 1 : 1;
            LinearLayout linearLayout = this.mTitleLayout;
            if (visible == 0) {
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
        int i = 0;
        this.mSubtitle = subtitle;
        if (this.mSubtitleView != null) {
            this.mSubtitleView.setText(subtitle);
            this.mSubtitleView.setVisibility(subtitle != null ? 0 : 8);
            int visible = (this.mExpandedActionView != null || (this.mDisplayOptions & 8) == 0) ? 0 : TextUtils.isEmpty(this.mTitle) ? TextUtils.isEmpty(this.mSubtitle) ^ 1 : 1;
            LinearLayout linearLayout = this.mTitleLayout;
            if (visible == 0) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    public void setHomeButtonEnabled(boolean enable) {
        setHomeButtonEnabled(enable, true);
    }

    private void setHomeButtonEnabled(boolean enable, boolean recordState) {
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
        if (homeEnabled) {
            this.mUpGoerFive.setImportantForAccessibility(0);
            this.mUpGoerFive.setContentDescription(buildHomeContentDescription());
            return;
        }
        this.mUpGoerFive.setContentDescription(null);
        this.mUpGoerFive.setImportantForAccessibility(2);
    }

    private CharSequence buildHomeContentDescription() {
        CharSequence homeDesc;
        if (this.mHomeDescription != null) {
            homeDesc = this.mHomeDescription;
        } else if ((this.mDisplayOptions & 4) != 0) {
            homeDesc = this.mContext.getResources().getText(this.mDefaultUpDescription);
        } else {
            homeDesc = this.mContext.getResources().getText(R.string.action_bar_home_description);
        }
        CharSequence title = getTitle();
        CharSequence subtitle = getSubtitle();
        if (TextUtils.isEmpty(title)) {
            return homeDesc;
        }
        String result;
        if (TextUtils.isEmpty(subtitle)) {
            result = getResources().getString(R.string.action_bar_home_description_format, new Object[]{title, homeDesc});
        } else {
            result = getResources().getString(R.string.action_bar_home_subtitle_description_format, new Object[]{title, subtitle, homeDesc});
        }
        return result;
    }

    public void setDisplayOptions(int options) {
        int flagsChanged = this.mDisplayOptions == -1 ? -1 : options ^ this.mDisplayOptions;
        this.mDisplayOptions = options;
        if ((flagsChanged & 63) != 0) {
            if ((flagsChanged & 4) != 0) {
                boolean setUp = (options & 4) != 0;
                this.mHomeLayout.setShowUp(setUp);
                if (setUp) {
                    setHomeButtonEnabled(true);
                }
            }
            if ((flagsChanged & 1) != 0) {
                boolean logoVis = (this.mLogo == null || (options & 1) == 0) ? false : true;
                this.mHomeLayout.setIcon(logoVis ? this.mLogo : this.mIcon);
            }
            if ((flagsChanged & 8) != 0) {
                if ((options & 8) != 0) {
                    initTitle();
                } else {
                    this.mUpGoerFive.removeView(this.mTitleLayout);
                }
            }
            boolean showHome = (options & 2) != 0;
            boolean titleUp = !showHome ? (this.mDisplayOptions & 4) != 0 : false;
            this.mHomeLayout.setShowIcon(showHome);
            int homeVis = ((showHome || titleUp) && this.mExpandedActionView == null) ? 0 : 8;
            this.mHomeLayout.setVisibility(homeVis);
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
                        this.mSpinner = new Spinner(this.mContext, null, R.attr.actionDropDownStyle);
                        this.mSpinner.setId(R.id.action_bar_spinner);
                        this.mListNavLayout = new LinearLayout(this.mContext, null, R.attr.actionBarTabBarStyle);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
                        params.gravity = 17;
                        this.mListNavLayout.addView(this.mSpinner, (ViewGroup.LayoutParams) params);
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
                        this.mTabScrollView.setContentHeight(ActionBarPolicy.get(getContext()).getTabContainerHeight());
                        this.mTabScrollView.updateTabViewContainerWidth(getContext());
                        break;
                    }
            }
            this.mNavigationMode = mode;
            requestLayout();
        }
    }

    public void setDropdownParams(SpinnerAdapter adapter, OnItemSelectedListener l) {
        this.mSpinnerAdapter = adapter;
        this.mNavItemSelectedListener = l;
        if (this.mSpinner != null) {
            this.mSpinner.setAdapter(adapter);
            this.mSpinner.setOnItemSelectedListener(l);
        }
    }

    public int getDropdownItemCount() {
        return this.mSpinnerAdapter != null ? this.mSpinnerAdapter.getCount() : 0;
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

    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ActionBar.LayoutParams(DEFAULT_CUSTOM_GRAVITY);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mUpGoerFive.addView(this.mHomeLayout, 0);
        addView(this.mUpGoerFive);
        if (this.mCustomNavView != null && (this.mDisplayOptions & 16) != 0) {
            ActionBarView parent = this.mCustomNavView.getParent();
            if (parent != this) {
                if (parent instanceof ViewGroup) {
                    parent.removeView(this.mCustomNavView);
                }
                addView(this.mCustomNavView);
            }
        }
    }

    private void initTitle() {
        if (this.mTitleLayout == null) {
            this.mTitleLayout = initTitleLayout();
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_title);
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_subtitle);
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
            initTitleAppearance();
        }
        this.mUpGoerFive.addView(this.mTitleLayout);
        if (this.mExpandedActionView != null || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) {
            this.mTitleLayout.setVisibility(8);
        } else {
            this.mTitleLayout.setVisibility(0);
        }
        setCustomTitle(this.mCustTitle);
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

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int childCount = getChildCount();
        if (this.mIsCollapsible) {
            int visibleChildren = 0;
            for (i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (!(child.getVisibility() == 8 || ((child == this.mMenuView && this.mMenuView.getChildCount() == 0) || child == this.mUpGoerFive))) {
                    visibleChildren++;
                }
            }
            int upChildCount = this.mUpGoerFive.getChildCount();
            for (i = 0; i < upChildCount; i++) {
                if (this.mUpGoerFive.getChildAt(i).getVisibility() != 8) {
                    visibleChildren++;
                }
            }
            if (visibleChildren == 0) {
                -wrap6(0, 0);
                return;
            }
        }
        if (MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_width=\"match_parent\" (or fill_parent)");
        } else if (MeasureSpec.getMode(heightMeasureSpec) != Integer.MIN_VALUE) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with android:layout_height=\"wrap_content\"");
        } else {
            int homeWidthSpec;
            int contentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int maxHeight = this.mContentHeight >= 0 ? this.mContentHeight : MeasureSpec.getSize(heightMeasureSpec);
            int verticalPadding = getPaddingTop() + getPaddingBottom();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int height = maxHeight - verticalPadding;
            int childSpecHeight = MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE);
            int exactHeightSpec = MeasureSpec.makeMeasureSpec(height, 1073741824);
            int availableWidth = (contentWidth - paddingLeft) - paddingRight;
            int leftOfCenter = availableWidth / 2;
            int rightOfCenter = leftOfCenter;
            boolean showTitle = (this.mTitleLayout == null || this.mTitleLayout.getVisibility() == 8) ? false : (this.mDisplayOptions & 8) != 0;
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            ViewGroup.LayoutParams homeLp = homeLayout.getLayoutParams();
            if (homeLp.width < 0) {
                homeWidthSpec = MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE);
            } else {
                homeWidthSpec = MeasureSpec.makeMeasureSpec(homeLp.width, 1073741824);
            }
            homeLayout.measure(homeWidthSpec, exactHeightSpec);
            int homeWidth = 0;
            if ((homeLayout.getVisibility() != 8 && homeLayout.getParent() == this.mUpGoerFive) || showTitle) {
                homeWidth = homeLayout.getMeasuredWidth();
                int homeOffsetWidth = homeWidth + homeLayout.getStartOffset();
                availableWidth = Math.max(0, availableWidth - homeOffsetWidth);
                leftOfCenter = Math.max(0, availableWidth - homeOffsetWidth);
            }
            if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                availableWidth = measureChildView(this.mMenuView, availableWidth, exactHeightSpec, 0);
                rightOfCenter = Math.max(0, rightOfCenter - this.mMenuView.getMeasuredWidth());
            }
            if (!(this.mIndeterminateProgressView == null || this.mIndeterminateProgressView.getVisibility() == 8)) {
                availableWidth = measureChildView(this.mIndeterminateProgressView, availableWidth, childSpecHeight, 0);
                rightOfCenter = Math.max(0, rightOfCenter - this.mIndeterminateProgressView.getMeasuredWidth());
            }
            if (this.mExpandedActionView == null) {
                int itemPaddingSize;
                switch (this.mNavigationMode) {
                    case 1:
                        if (this.mListNavLayout != null) {
                            itemPaddingSize = showTitle ? this.mItemPadding * 2 : this.mItemPadding;
                            availableWidth = Math.max(0, availableWidth - itemPaddingSize);
                            leftOfCenter = Math.max(0, leftOfCenter - itemPaddingSize);
                            this.mListNavLayout.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, 1073741824));
                            int listNavWidth = this.mListNavLayout.getMeasuredWidth();
                            availableWidth = Math.max(0, availableWidth - listNavWidth);
                            leftOfCenter = Math.max(0, leftOfCenter - listNavWidth);
                            break;
                        }
                        break;
                    case 2:
                        if (this.mTabScrollView != null) {
                            itemPaddingSize = this.mTabScrollView.adjustPadding(availableWidth, showTitle ? this.mItemPadding * 2 : this.mItemPadding);
                            availableWidth = Math.max(0, availableWidth - itemPaddingSize);
                            leftOfCenter = Math.max(0, leftOfCenter - itemPaddingSize);
                            this.mTabScrollView.measure(MeasureSpec.makeMeasureSpec(availableWidth, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, 1073741824));
                            int tabWidth = this.mTabScrollView.getMeasuredWidth();
                            availableWidth = Math.max(0, availableWidth - tabWidth);
                            leftOfCenter = Math.max(0, leftOfCenter - tabWidth);
                            break;
                        }
                        break;
                }
            }
            View customView = null;
            if (this.mExpandedActionView != null) {
                customView = this.mExpandedActionView;
            } else if (!((this.mDisplayOptions & 16) == 0 || this.mCustomNavView == null)) {
                customView = this.mCustomNavView;
            }
            if (customView != null) {
                int min;
                ViewGroup.LayoutParams lp = generateLayoutParams(customView.getLayoutParams());
                ActionBar.LayoutParams ablp = lp instanceof ActionBar.LayoutParams ? (ActionBar.LayoutParams) lp : null;
                int horizontalMargin = 0;
                int verticalMargin = 0;
                if (ablp != null) {
                    horizontalMargin = ablp.leftMargin + ablp.rightMargin;
                    verticalMargin = ablp.topMargin + ablp.bottomMargin;
                }
                int customNavHeightMode = this.mContentHeight <= 0 ? Integer.MIN_VALUE : lp.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp.height >= 0) {
                    height = Math.min(lp.height, height);
                }
                int customNavHeight = Math.max(0, height - verticalMargin);
                int customNavWidthMode = lp.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp.width >= 0) {
                    min = Math.min(lp.width, availableWidth);
                } else {
                    min = availableWidth;
                }
                int customNavWidth = Math.max(0, min - horizontalMargin);
                if (((ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY) & 7) == 1 && lp.width == -1) {
                    customNavWidth = Math.min(leftOfCenter, rightOfCenter) * 2;
                }
                customView.measure(MeasureSpec.makeMeasureSpec(customNavWidth, customNavWidthMode), MeasureSpec.makeMeasureSpec(customNavHeight, customNavHeightMode));
                availableWidth -= customView.getMeasuredWidth() + horizontalMargin;
            }
            availableWidth = measureChildView(this.mUpGoerFive, availableWidth + homeWidth, MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824), 0);
            if (this.mTitleLayout != null) {
                leftOfCenter = Math.max(0, leftOfCenter - this.mTitleLayout.getMeasuredWidth());
            }
            if (this.mContentHeight <= 0) {
                int measuredHeight = 0;
                for (i = 0; i < childCount; i++) {
                    int paddedViewHeight = getChildAt(i).getMeasuredHeight() + verticalPadding;
                    if (paddedViewHeight > measuredHeight) {
                        measuredHeight = paddedViewHeight;
                    }
                }
                -wrap6(contentWidth, measuredHeight);
            } else {
                -wrap6(contentWidth, maxHeight);
            }
            if (this.mContextView != null) {
                this.mContextView.setContentHeight(getMeasuredHeight());
            }
            if (!(this.mProgressView == null || this.mProgressView.getVisibility() == 8)) {
                this.mProgressView.measure(MeasureSpec.makeMeasureSpec(contentWidth - (this.mProgressBarPadding * 2), 1073741824), MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Integer.MIN_VALUE));
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int contentHeight = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (contentHeight > 0) {
            boolean isLayoutRtl = isLayoutRtl();
            int direction = isLayoutRtl ? 1 : -1;
            int menuStart = isLayoutRtl ? getPaddingLeft() : (r - l) - getPaddingRight();
            int x = isLayoutRtl ? (r - l) - getPaddingRight() : getPaddingLeft();
            int y = getPaddingTop();
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            boolean showTitle = (this.mTitleLayout == null || this.mTitleLayout.getVisibility() == 8) ? false : (this.mDisplayOptions & 8) != 0;
            int startOffset = 0;
            if (homeLayout.getParent() == this.mUpGoerFive) {
                if (homeLayout.getVisibility() != 8) {
                    startOffset = homeLayout.getStartOffset();
                } else if (showTitle) {
                    startOffset = homeLayout.getUpWidth();
                }
            }
            int x2 = AbsActionBarView.next(x + positionChild(this.mUpGoerFive, AbsActionBarView.next(x, startOffset, isLayoutRtl), y, contentHeight, isLayoutRtl), startOffset, isLayoutRtl);
            if (this.mExpandedActionView == null) {
                switch (this.mNavigationMode) {
                    case 1:
                        if (this.mListNavLayout != null) {
                            if (showTitle) {
                                x2 = AbsActionBarView.next(x2, this.mItemPadding, isLayoutRtl);
                            }
                            x2 = AbsActionBarView.next(x2 + positionChild(this.mListNavLayout, x2, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            break;
                        }
                        break;
                    case 2:
                        if (this.mTabScrollView != null) {
                            if (showTitle) {
                                x2 = AbsActionBarView.next(x2, this.mItemPadding, isLayoutRtl);
                            }
                            x2 = AbsActionBarView.next(x2 + positionChild(this.mTabScrollView, x2, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            break;
                        }
                        break;
                }
            }
            if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                positionChild(this.mMenuView, menuStart, y, contentHeight, isLayoutRtl ^ 1);
                menuStart += this.mMenuView.getMeasuredWidth() * direction;
            }
            if (!(this.mIndeterminateProgressView == null || this.mIndeterminateProgressView.getVisibility() == 8)) {
                positionChild(this.mIndeterminateProgressView, menuStart, y, contentHeight, isLayoutRtl ^ 1);
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
                int topMargin = 0;
                int bottomMargin = 0;
                if (ablp != null) {
                    x2 = AbsActionBarView.next(x2, ablp.getMarginStart(), isLayoutRtl);
                    menuStart += ablp.getMarginEnd() * direction;
                    topMargin = ablp.topMargin;
                    bottomMargin = ablp.bottomMargin;
                }
                int hgravity = gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
                if (hgravity == 1) {
                    int centeredLeft = ((this.mRight - this.mLeft) - navWidth) / 2;
                    int centeredEnd;
                    if (isLayoutRtl) {
                        centeredEnd = centeredLeft;
                        if (centeredLeft + navWidth > x2) {
                            hgravity = 5;
                        } else if (centeredLeft < menuStart) {
                            hgravity = 3;
                        }
                    } else {
                        int centeredStart = centeredLeft;
                        centeredEnd = centeredLeft + navWidth;
                        if (centeredLeft < x2) {
                            hgravity = 3;
                        } else if (centeredEnd > menuStart) {
                            hgravity = 5;
                        }
                    }
                } else if (gravity == 0) {
                    hgravity = Gravity.START;
                }
                int xpos = 0;
                switch (Gravity.getAbsoluteGravity(hgravity, layoutDirection)) {
                    case 1:
                        xpos = ((this.mRight - this.mLeft) - navWidth) / 2;
                        break;
                    case 3:
                        if (!isLayoutRtl) {
                            xpos = x2;
                            break;
                        } else {
                            xpos = menuStart;
                            break;
                        }
                    case 5:
                        if (!isLayoutRtl) {
                            xpos = menuStart - navWidth;
                            break;
                        } else {
                            xpos = x2 - navWidth;
                            break;
                        }
                }
                int vgravity = gravity & 112;
                if (gravity == 0) {
                    vgravity = 16;
                }
                int ypos = 0;
                switch (vgravity) {
                    case 16:
                        ypos = ((((this.mBottom - this.mTop) - getPaddingBottom()) - getPaddingTop()) - customView.getMeasuredHeight()) / 2;
                        break;
                    case 48:
                        ypos = getPaddingTop() + topMargin;
                        break;
                    case 80:
                        ypos = ((getHeight() - getPaddingBottom()) - customView.getMeasuredHeight()) - bottomMargin;
                        break;
                }
                int customWidth = customView.getMeasuredWidth();
                customView.layout(xpos, ypos, xpos + customWidth, customView.getMeasuredHeight() + ypos);
                x2 = AbsActionBarView.next(x2, customWidth, isLayoutRtl);
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
        SavedState state = new SavedState(super.-wrap0());
        if (!(this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null)) {
            state.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        state.isOverflowOpen = isOverflowMenuShowing();
        return state;
    }

    public void onRestoreInstanceState(Parcelable p) {
        SavedState state = (SavedState) p;
        super.-wrap2(state.getSuperState());
        if (!(state.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || this.mOptionsMenu == null)) {
            MenuItem item = this.mOptionsMenu.findItem(state.expandedMenuItemId);
            if (item != null) {
                item.expandActionView();
            }
        }
        if (state.isOverflowOpen) {
            postShowOverflowMenuPending();
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

    public void setCustomTitle(View view) {
    }

    protected LinearLayout initTitleLayout() {
        return (LinearLayout) LayoutInflater.from(getContext()).inflate((int) R.layout.action_bar_title_item, (ViewGroup) this, false);
    }

    protected TextView getTitleView() {
        return this.mTitleView;
    }

    protected TextView getSubTitleView() {
        return this.mSubtitleView;
    }

    protected void deleteExpandedHomeIfNeed() {
    }

    protected void initExpandedHomeLayout() {
        this.mExpandedHomeLayout = (HomeView) LayoutInflater.from(this.mContext).inflate(this.mHomeResId, this.mUpGoerFive, false);
        this.mExpandedHomeLayout.setShowUp(true);
        this.mExpandedHomeLayout.setOnClickListener(this.mExpandedActionViewUpListener);
        this.mExpandedHomeLayout.setContentDescription(getResources().getText(this.mDefaultUpDescription));
        Drawable upBackground = this.mUpGoerFive.getBackground();
        if (upBackground != null) {
            this.mExpandedHomeLayout.setBackground(upBackground.getConstantState().newDrawable());
        }
        this.mExpandedHomeLayout.setEnabled(true);
        this.mExpandedHomeLayout.setFocusable(true);
    }

    protected ViewGroup getUpGoerFive() {
        return this.mUpGoerFive;
    }

    protected HomeView getHomeLayout() {
        return this.mHomeLayout;
    }

    protected HomeView getExpandedHomeLayout() {
        return this.mExpandedHomeLayout;
    }

    protected ActionMenuPresenter initActionMenuPresenter(Context context) {
        return new ActionMenuPresenter(context);
    }

    protected void initTitleAppearance() {
    }

    protected OnClickListener getUpClickListener() {
        return this.mUpClickListener;
    }
}
