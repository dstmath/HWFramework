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
import java.lang.annotation.RCUnownedThisRef;

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
    private AdapterView.OnItemSelectedListener mNavItemSelectedListener;
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
    private final View.OnClickListener mUpClickListener = new View.OnClickListener() {
        /* class com.android.internal.widget.ActionBarView.AnonymousClass1 */

        @Override // android.view.View.OnClickListener
        @RCUnownedThisRef
        public void onClick(View v) {
            if (ActionBarView.this.mMenuPrepared) {
                ActionBarView.this.mWindowCallback.onMenuItemSelected(0, ActionBarView.this.mLogoNavItem);
            }
        }
    };
    private ViewGroup mUpGoerFive;
    private boolean mUserTitle;
    private boolean mWasHomeEnabled;
    Window.Callback mWindowCallback;

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
        this.mHomeResId = a.getResourceId(16, R.layout.action_bar_home);
        this.mUpGoerFive = (ViewGroup) inflater.inflate(R.layout.action_bar_up_container, (ViewGroup) this, false);
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
            setDisplayOptions(16 | this.mDisplayOptions);
        }
        this.mContentHeight = a.getLayoutDimension(4, 0);
        a.recycle();
        this.mLogoNavItem = new ActionMenuItem(context, 0, 16908332, 0, 0, this.mTitle);
        this.mUpGoerFive.setOnClickListener(this.mUpClickListener);
        this.mUpGoerFive.setClickable(true);
        this.mUpGoerFive.setFocusable(true);
        if (getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.internal.widget.AbsActionBarView, android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        ViewGroup viewGroup;
        super.onConfigurationChanged(newConfig);
        this.mTitleView = null;
        this.mSubtitleView = null;
        LinearLayout linearLayout = this.mTitleLayout;
        if (linearLayout != null && linearLayout.getParent() == (viewGroup = this.mUpGoerFive)) {
            viewGroup.removeView(this.mTitleLayout);
        }
        this.mTitleLayout = null;
        if ((this.mDisplayOptions & 8) != 0) {
            initTitle();
        }
        int i = this.mHomeDescriptionRes;
        if (i != 0) {
            setNavigationContentDescription(i);
        }
        ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
        if (scrollingTabContainerView != null && this.mIncludeTabs) {
            ViewGroup.LayoutParams lp = scrollingTabContainerView.getLayoutParams();
            if (lp != null) {
                lp.width = -2;
                lp.height = -1;
            }
            this.mTabScrollView.setAllowCollapse(true);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setWindowCallback(Window.Callback cb) {
        this.mWindowCallback = cb;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mTabSelector);
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void initProgress() {
        this.mProgressView = new ProgressBar(this.mContext, null, 0, this.mProgressStyle);
        this.mProgressView.setId(R.id.progress_horizontal);
        this.mProgressView.setMax(10000);
        this.mProgressView.setVisibility(8);
        addView(this.mProgressView);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void initIndeterminateProgress() {
        this.mIndeterminateProgressView = new ProgressBar(this.mContext, null, 0, this.mIndeterminateProgressStyle);
        this.mIndeterminateProgressView.setId(R.id.progress_circular);
        this.mIndeterminateProgressView.setVisibility(8);
        addView(this.mIndeterminateProgressView);
    }

    @Override // com.android.internal.widget.AbsActionBarView
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
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(R.bool.action_bar_expanded_action_views_exclusive));
                } else {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                    this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                    this.mActionMenuPresenter.setItemLimit(Integer.MAX_VALUE);
                }
            }
            super.setSplitToolbar(splitActionBar);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean isSplit() {
        return this.mSplitActionBar;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean canSplit() {
        return true;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasEmbeddedTabs() {
        return this.mIncludeTabs;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setEmbeddedTabView(ScrollingTabContainerView tabs) {
        ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
        if (scrollingTabContainerView != null) {
            removeView(scrollingTabContainerView);
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

    @Override // com.android.internal.widget.DecorToolbar
    public void setSplitViewLocation(int start, int end) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void updateSplitLocation() {
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setMenuPrepared() {
        this.mMenuPrepared = true;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        ActionMenuView menuView;
        ViewGroup oldParent;
        MenuBuilder menuBuilder = this.mOptionsMenu;
        if (menu != menuBuilder) {
            if (menuBuilder != null) {
                menuBuilder.removeMenuPresenter(this.mActionMenuPresenter);
                this.mOptionsMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
            }
            MenuBuilder builder = (MenuBuilder) menu;
            this.mOptionsMenu = builder;
            if (!(this.mMenuView == null || (oldParent = (ViewGroup) this.mMenuView.getParent()) == null)) {
                oldParent.removeView(this.mMenuView);
            }
            if (this.mActionMenuPresenter == null) {
                this.mActionMenuPresenter = new ActionMenuPresenter(this.mContext);
                this.mActionMenuPresenter.setCallback(cb);
                this.mActionMenuPresenter.setId(R.id.action_menu_presenter);
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
            if (!this.mSplitActionBar) {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(R.bool.action_bar_expanded_action_views_exclusive));
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

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasExpandedActionView() {
        ExpandedActionViewMenuPresenter expandedActionViewMenuPresenter = this.mExpandedMenuPresenter;
        return (expandedActionViewMenuPresenter == null || expandedActionViewMenuPresenter.mCurrentExpandedItem == null) ? false : true;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void collapseActionView() {
        MenuItemImpl item;
        ExpandedActionViewMenuPresenter expandedActionViewMenuPresenter = this.mExpandedMenuPresenter;
        if (expandedActionViewMenuPresenter == null) {
            item = null;
        } else {
            item = expandedActionViewMenuPresenter.mCurrentExpandedItem;
        }
        if (item != null) {
            item.collapseActionView();
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setCustomView(View view) {
        boolean showCustom = (this.mDisplayOptions & 16) != 0;
        View view2 = this.mCustomNavView;
        if (view2 != null && showCustom) {
            removeView(view2);
        }
        this.mCustomNavView = view;
        View view3 = this.mCustomNavView;
        if (view3 != null && showCustom) {
            addView(view3);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public CharSequence getTitle() {
        return this.mTitle;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setTitle(CharSequence title) {
        this.mUserTitle = true;
        setTitleImpl(title);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setWindowTitle(CharSequence title) {
        if (!this.mUserTitle) {
            setTitleImpl(title);
        }
    }

    private void setTitleImpl(CharSequence title) {
        this.mTitle = title;
        TextView textView = this.mTitleView;
        if (textView != null) {
            textView.setText(title);
            int i = 8;
            boolean visible = this.mExpandedActionView == null && (this.mDisplayOptions & 8) != 0 && (!TextUtils.isEmpty(this.mTitle) || !TextUtils.isEmpty(this.mSubtitle));
            LinearLayout linearLayout = this.mTitleLayout;
            if (visible) {
                i = 0;
            }
            linearLayout.setVisibility(i);
        }
        ActionMenuItem actionMenuItem = this.mLogoNavItem;
        if (actionMenuItem != null) {
            actionMenuItem.setTitle(title);
        }
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    @Override // com.android.internal.widget.DecorToolbar
    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSubtitle(CharSequence subtitle) {
        this.mSubtitle = subtitle;
        TextView textView = this.mSubtitleView;
        if (textView != null) {
            textView.setText(subtitle);
            int i = 0;
            this.mSubtitleView.setVisibility(subtitle != null ? 0 : 8);
            boolean visible = this.mExpandedActionView == null && (this.mDisplayOptions & 8) != 0 && (!TextUtils.isEmpty(this.mTitle) || !TextUtils.isEmpty(this.mSubtitle));
            LinearLayout linearLayout = this.mTitleLayout;
            if (!visible) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setHomeButtonEnabled(boolean enable) {
        setHomeButtonEnabled(enable, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        if (!TextUtils.isEmpty(subtitle)) {
            return getResources().getString(R.string.action_bar_home_subtitle_description_format, title, subtitle, homeDesc);
        }
        return getResources().getString(R.string.action_bar_home_description_format, title, homeDesc);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDisplayOptions(int options) {
        View view;
        int i = this.mDisplayOptions;
        int flagsChanged = -1;
        if (i != -1) {
            flagsChanged = options ^ i;
        }
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
                this.mHomeLayout.setIcon(this.mLogo != null && (options & 1) != 0 ? this.mLogo : this.mIcon);
            }
            if ((flagsChanged & 8) != 0) {
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
            if (!((flagsChanged & 16) == 0 || (view = this.mCustomNavView) == null)) {
                if ((options & 16) != 0) {
                    addView(view);
                } else {
                    removeView(view);
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

    @Override // com.android.internal.widget.DecorToolbar
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

    @Override // com.android.internal.widget.DecorToolbar
    public void setIcon(int resId) {
        setIcon(resId != 0 ? this.mContext.getDrawable(resId) : null);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasIcon() {
        return this.mIcon != null;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setLogo(Drawable logo) {
        this.mLogo = logo;
        if (logo != null && (this.mDisplayOptions & 1) != 0) {
            this.mHomeLayout.setIcon(logo);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setLogo(int resId) {
        setLogo(resId != 0 ? this.mContext.getDrawable(resId) : null);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasLogo() {
        return this.mLogo != null;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationMode(int mode) {
        ScrollingTabContainerView scrollingTabContainerView;
        ScrollingTabContainerView scrollingTabContainerView2;
        int oldMode = this.mNavigationMode;
        if (mode != oldMode) {
            if (oldMode == 1) {
                LinearLayout linearLayout = this.mListNavLayout;
                if (linearLayout != null) {
                    removeView(linearLayout);
                }
            } else if (oldMode == 2 && (scrollingTabContainerView2 = this.mTabScrollView) != null && this.mIncludeTabs) {
                removeView(scrollingTabContainerView2);
            }
            if (mode == 1) {
                if (this.mSpinner == null) {
                    this.mSpinner = new Spinner(this.mContext, null, 16843479);
                    this.mSpinner.setId(R.id.action_bar_spinner);
                    this.mListNavLayout = new LinearLayout(this.mContext, null, 16843508);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
                    params.gravity = 17;
                    this.mListNavLayout.addView(this.mSpinner, params);
                }
                SpinnerAdapter adapter = this.mSpinner.getAdapter();
                SpinnerAdapter spinnerAdapter = this.mSpinnerAdapter;
                if (adapter != spinnerAdapter) {
                    this.mSpinner.setAdapter(spinnerAdapter);
                }
                this.mSpinner.setOnItemSelectedListener(this.mNavItemSelectedListener);
                addView(this.mListNavLayout);
            } else if (mode == 2 && (scrollingTabContainerView = this.mTabScrollView) != null && this.mIncludeTabs) {
                addView(scrollingTabContainerView);
            }
            this.mNavigationMode = mode;
            requestLayout();
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDropdownParams(SpinnerAdapter adapter, AdapterView.OnItemSelectedListener l) {
        this.mSpinnerAdapter = adapter;
        this.mNavItemSelectedListener = l;
        Spinner spinner = this.mSpinner;
        if (spinner != null) {
            spinner.setAdapter(adapter);
            this.mSpinner.setOnItemSelectedListener(l);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getDropdownItemCount() {
        SpinnerAdapter spinnerAdapter = this.mSpinnerAdapter;
        if (spinnerAdapter != null) {
            return spinnerAdapter.getCount();
        }
        return 0;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDropdownSelectedPosition(int position) {
        this.mSpinner.setSelection(position);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getDropdownSelectedPosition() {
        return this.mSpinner.getSelectedItemPosition();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public View getCustomView() {
        return this.mCustomNavView;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getNavigationMode() {
        return this.mNavigationMode;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getDisplayOptions() {
        return this.mDisplayOptions;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public ViewGroup getViewGroup() {
        return this;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ActionBar.LayoutParams((int) DEFAULT_CUSTOM_GRAVITY);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        ViewParent parent;
        super.onFinishInflate();
        this.mUpGoerFive.addView(this.mHomeLayout, 0);
        addView(this.mUpGoerFive);
        View view = this.mCustomNavView;
        if (view != null && (this.mDisplayOptions & 16) != 0 && (parent = view.getParent()) != this) {
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mCustomNavView);
            }
            addView(this.mCustomNavView);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initTitle() {
        if (this.mTitleLayout == null) {
            this.mTitleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.action_bar_title_item, (ViewGroup) this, false);
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_title);
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_subtitle);
            int i = this.mTitleStyleRes;
            if (i != 0) {
                this.mTitleView.setTextAppearance(i);
            }
            CharSequence charSequence = this.mTitle;
            if (charSequence != null) {
                this.mTitleView.setText(charSequence);
            }
            int i2 = this.mSubtitleStyleRes;
            if (i2 != 0) {
                this.mSubtitleView.setTextAppearance(i2);
            }
            CharSequence charSequence2 = this.mSubtitle;
            if (charSequence2 != null) {
                this.mSubtitleView.setText(charSequence2);
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

    @Override // com.android.internal.widget.DecorToolbar
    public void setCollapsible(boolean collapsible) {
        this.mIsCollapsible = collapsible;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean isTitleTruncated() {
        Layout titleLayout;
        TextView textView = this.mTitleView;
        if (textView == null || (titleLayout = textView.getLayout()) == null) {
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
    /* JADX WARNING: Removed duplicated region for block: B:133:0x02b3  */
    /* JADX WARNING: Removed duplicated region for block: B:136:0x02d0  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x02de  */
    /* JADX WARNING: Removed duplicated region for block: B:146:0x02f5  */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x02fc  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0307  */
    /* JADX WARNING: Removed duplicated region for block: B:171:? A[ADDED_TO_REGION, RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01f9  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01fc  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x020a  */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int homeWidthSpec;
        int availableWidth;
        int rightOfCenter;
        int tabWidth;
        View customView;
        LinearLayout linearLayout;
        ActionBarContextView actionBarContextView;
        ProgressBar progressBar;
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
            for (int i2 = 0; i2 < upChildCount; i2++) {
                if (this.mUpGoerFive.getChildAt(i2).getVisibility() != 8) {
                    visibleChildren++;
                }
            }
            if (visibleChildren == 0) {
                setMeasuredDimension(0, 0);
                return;
            }
        }
        if (View.MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with android:layout_width=\"match_parent\" (or fill_parent)");
        } else if (View.MeasureSpec.getMode(heightMeasureSpec) == Integer.MIN_VALUE) {
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
            int rightOfCenter2 = leftOfCenter;
            LinearLayout linearLayout2 = this.mTitleLayout;
            boolean showTitle = (linearLayout2 == null || linearLayout2.getVisibility() == 8 || (this.mDisplayOptions & 8) == 0) ? false : true;
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            ViewGroup.LayoutParams homeLp = homeLayout.getLayoutParams();
            if (homeLp.width < 0) {
                homeWidthSpec = View.MeasureSpec.makeMeasureSpec(availableWidth2, Integer.MIN_VALUE);
            } else {
                homeWidthSpec = View.MeasureSpec.makeMeasureSpec(homeLp.width, 1073741824);
            }
            homeLayout.measure(homeWidthSpec, exactHeightSpec);
            int homeWidth = 0;
            if ((homeLayout.getVisibility() == 8 || homeLayout.getParent() != this.mUpGoerFive) && !showTitle) {
                availableWidth = availableWidth2;
            } else {
                int homeWidth2 = homeLayout.getMeasuredWidth();
                int homeOffsetWidth = homeLayout.getStartOffset() + homeWidth2;
                homeWidth = homeWidth2;
                availableWidth = Math.max(0, availableWidth2 - homeOffsetWidth);
                leftOfCenter = Math.max(0, availableWidth - homeOffsetWidth);
            }
            if (this.mMenuView != null && this.mMenuView.getParent() == this) {
                availableWidth = measureChildView(this.mMenuView, availableWidth, exactHeightSpec, 0);
                rightOfCenter2 = Math.max(0, rightOfCenter2 - this.mMenuView.getMeasuredWidth());
            }
            ProgressBar progressBar2 = this.mIndeterminateProgressView;
            if (progressBar2 == null || progressBar2.getVisibility() == 8) {
                rightOfCenter = rightOfCenter2;
            } else {
                availableWidth = measureChildView(this.mIndeterminateProgressView, availableWidth, childSpecHeight, 0);
                rightOfCenter = Math.max(0, rightOfCenter2 - this.mIndeterminateProgressView.getMeasuredWidth());
            }
            if (this.mExpandedActionView == null) {
                int i3 = this.mNavigationMode;
                if (i3 != 1) {
                    if (i3 == 2) {
                        if (this.mTabScrollView != null) {
                            int itemPaddingSize = this.mItemPadding;
                            if (showTitle) {
                                itemPaddingSize *= 2;
                            }
                            int availableWidth3 = Math.max(0, availableWidth - itemPaddingSize);
                            int leftOfCenter2 = Math.max(0, leftOfCenter - itemPaddingSize);
                            this.mTabScrollView.measure(View.MeasureSpec.makeMeasureSpec(availableWidth3, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
                            int tabWidth2 = this.mTabScrollView.getMeasuredWidth();
                            availableWidth = Math.max(0, availableWidth3 - tabWidth2);
                            tabWidth = Math.max(0, leftOfCenter2 - tabWidth2);
                            customView = null;
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
                                    horizontalMargin = ablp.rightMargin + ablp.leftMargin;
                                    verticalMargin = ablp.topMargin + ablp.bottomMargin;
                                } else {
                                    horizontalMargin = 0;
                                }
                                if (this.mContentHeight <= 0) {
                                    customNavHeightMode = Integer.MIN_VALUE;
                                } else {
                                    customNavHeightMode = lp.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                                }
                                int customNavHeight = Math.max(0, (lp.height >= 0 ? Math.min(lp.height, height) : height) - verticalMargin);
                                int customNavWidthMode = lp.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                                int customNavWidth = Math.max(0, (lp.width >= 0 ? Math.min(lp.width, availableWidth) : availableWidth) - horizontalMargin);
                                if (((ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY) & 7) == 1) {
                                    if (lp.width == -1) {
                                        customNavWidth = Math.min(tabWidth, rightOfCenter) * 2;
                                    }
                                }
                                customView.measure(View.MeasureSpec.makeMeasureSpec(customNavWidth, customNavWidthMode), View.MeasureSpec.makeMeasureSpec(customNavHeight, customNavHeightMode));
                                availableWidth -= horizontalMargin + customView.getMeasuredWidth();
                            }
                            measureChildView(this.mUpGoerFive, availableWidth + homeWidth, View.MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824), 0);
                            linearLayout = this.mTitleLayout;
                            if (linearLayout != null) {
                                Math.max(0, tabWidth - linearLayout.getMeasuredWidth());
                            }
                            if (this.mContentHeight <= 0) {
                                int measuredHeight = 0;
                                for (int i4 = 0; i4 < childCount; i4++) {
                                    int paddedViewHeight = getChildAt(i4).getMeasuredHeight() + verticalPadding;
                                    if (paddedViewHeight > measuredHeight) {
                                        measuredHeight = paddedViewHeight;
                                    }
                                }
                                setMeasuredDimension(contentWidth, measuredHeight);
                            } else {
                                setMeasuredDimension(contentWidth, maxHeight);
                            }
                            actionBarContextView = this.mContextView;
                            if (actionBarContextView != null) {
                                actionBarContextView.setContentHeight(getMeasuredHeight());
                            }
                            progressBar = this.mProgressView;
                            if (progressBar == null && progressBar.getVisibility() != 8) {
                                this.mProgressView.measure(View.MeasureSpec.makeMeasureSpec(contentWidth - (this.mProgressBarPadding * 2), 1073741824), View.MeasureSpec.makeMeasureSpec(getMeasuredHeight(), Integer.MIN_VALUE));
                                return;
                            }
                            return;
                        }
                    }
                } else if (this.mListNavLayout != null) {
                    int itemPaddingSize2 = this.mItemPadding;
                    if (showTitle) {
                        itemPaddingSize2 *= 2;
                    }
                    int availableWidth4 = Math.max(0, availableWidth - itemPaddingSize2);
                    int leftOfCenter3 = Math.max(0, leftOfCenter - itemPaddingSize2);
                    this.mListNavLayout.measure(View.MeasureSpec.makeMeasureSpec(availableWidth4, Integer.MIN_VALUE), View.MeasureSpec.makeMeasureSpec(height, 1073741824));
                    int listNavWidth = this.mListNavLayout.getMeasuredWidth();
                    availableWidth = Math.max(0, availableWidth4 - listNavWidth);
                    tabWidth = Math.max(0, leftOfCenter3 - listNavWidth);
                    customView = null;
                    if (this.mExpandedActionView != null) {
                    }
                    if (customView != null) {
                    }
                    measureChildView(this.mUpGoerFive, availableWidth + homeWidth, View.MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824), 0);
                    linearLayout = this.mTitleLayout;
                    if (linearLayout != null) {
                    }
                    if (this.mContentHeight <= 0) {
                    }
                    actionBarContextView = this.mContextView;
                    if (actionBarContextView != null) {
                    }
                    progressBar = this.mProgressView;
                    if (progressBar == null) {
                        return;
                    }
                    return;
                }
            }
            tabWidth = leftOfCenter;
            customView = null;
            if (this.mExpandedActionView != null) {
            }
            if (customView != null) {
            }
            measureChildView(this.mUpGoerFive, availableWidth + homeWidth, View.MeasureSpec.makeMeasureSpec(this.mContentHeight, 1073741824), 0);
            linearLayout = this.mTitleLayout;
            if (linearLayout != null) {
            }
            if (this.mContentHeight <= 0) {
            }
            actionBarContextView = this.mContextView;
            if (actionBarContextView != null) {
            }
            progressBar = this.mProgressView;
            if (progressBar == null) {
            }
        } else {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with android:layout_height=\"wrap_content\"");
        }
    }

    /* JADX INFO: Multiple debug info for r3v8 int: [D('lp' android.view.ViewGroup$LayoutParams), D('centeredEnd' int)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x026e  */
    /* JADX WARNING: Removed duplicated region for block: B:137:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x009d A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c5  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00f2  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x013a  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x014a  */
    @Override // android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int startOffset;
        int x;
        int x2;
        View customView;
        ProgressBar progressBar;
        int hgravity;
        int i;
        int x3;
        int contentHeight = ((b - t) - getPaddingTop()) - getPaddingBottom();
        if (contentHeight > 0) {
            boolean isLayoutRtl = isLayoutRtl();
            int direction = isLayoutRtl ? 1 : -1;
            int menuStart = isLayoutRtl ? getPaddingLeft() : (r - l) - getPaddingRight();
            int x4 = isLayoutRtl ? (r - l) - getPaddingRight() : getPaddingLeft();
            int y = getPaddingTop();
            HomeView homeLayout = this.mExpandedActionView != null ? this.mExpandedHomeLayout : this.mHomeLayout;
            LinearLayout linearLayout = this.mTitleLayout;
            boolean showTitle = (linearLayout == null || linearLayout.getVisibility() == 8 || (this.mDisplayOptions & 8) == 0) ? false : true;
            if (homeLayout.getParent() == this.mUpGoerFive) {
                if (homeLayout.getVisibility() != 8) {
                    startOffset = homeLayout.getStartOffset();
                } else if (showTitle) {
                    startOffset = homeLayout.getUpWidth();
                }
                int x5 = next(x4 + positionChild(this.mUpGoerFive, next(x4, startOffset, isLayoutRtl), y, contentHeight, isLayoutRtl), startOffset, isLayoutRtl);
                if (this.mExpandedActionView == null && (i = this.mNavigationMode) != 0) {
                    if (i == 1) {
                        if (i == 2 && this.mTabScrollView != null) {
                            if (showTitle) {
                                x5 = next(x5, this.mItemPadding, isLayoutRtl);
                            }
                            x = next(x5 + positionChild(this.mTabScrollView, x5, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                            if (this.mMenuView == null && this.mMenuView.getParent() == this) {
                                x2 = x;
                                positionChild(this.mMenuView, menuStart, y, contentHeight, !isLayoutRtl);
                                menuStart += this.mMenuView.getMeasuredWidth() * direction;
                            } else {
                                x2 = x;
                            }
                            ProgressBar progressBar2 = this.mIndeterminateProgressView;
                            if (!(progressBar2 == null || progressBar2.getVisibility() == 8)) {
                                positionChild(this.mIndeterminateProgressView, menuStart, y, contentHeight, !isLayoutRtl);
                                menuStart += this.mIndeterminateProgressView.getMeasuredWidth() * direction;
                            }
                            customView = null;
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
                                    int x6 = next(x2, ablp.getMarginStart(), isLayoutRtl);
                                    menuStart += ablp.getMarginEnd() * direction;
                                    int topMargin2 = ablp.topMargin;
                                    bottomMargin = ablp.bottomMargin;
                                    topMargin = topMargin2;
                                    x2 = x6;
                                }
                                int hgravity2 = 8388615 & gravity;
                                if (hgravity2 == 1) {
                                    int hgravity3 = hgravity2;
                                    int centeredLeft = ((this.mRight - this.mLeft) - navWidth) / 2;
                                    if (isLayoutRtl) {
                                        if (centeredLeft + navWidth > x2) {
                                            hgravity3 = 5;
                                        } else if (centeredLeft < menuStart) {
                                            hgravity3 = 3;
                                        }
                                        hgravity = hgravity3;
                                    } else {
                                        int centeredEnd = centeredLeft + navWidth;
                                        if (centeredLeft < x2) {
                                            hgravity = 3;
                                        } else if (centeredEnd > menuStart) {
                                            hgravity = 5;
                                        } else {
                                            hgravity = hgravity3;
                                        }
                                    }
                                } else if (gravity == 0) {
                                    hgravity = Gravity.START;
                                } else {
                                    hgravity = hgravity2;
                                }
                                int xpos = 0;
                                int absoluteGravity = Gravity.getAbsoluteGravity(hgravity, layoutDirection);
                                if (absoluteGravity == 1) {
                                    xpos = ((this.mRight - this.mLeft) - navWidth) / 2;
                                } else if (absoluteGravity == 3) {
                                    xpos = isLayoutRtl ? menuStart : x2;
                                } else if (absoluteGravity == 5) {
                                    xpos = isLayoutRtl ? x2 - navWidth : menuStart - navWidth;
                                }
                                int vgravity = gravity & 112;
                                if (gravity == 0) {
                                    vgravity = 16;
                                }
                                int ypos = 0;
                                if (vgravity == 16) {
                                    ypos = ((((this.mBottom - this.mTop) - getPaddingBottom()) - getPaddingTop()) - customView.getMeasuredHeight()) / 2;
                                } else if (vgravity == 48) {
                                    ypos = getPaddingTop() + topMargin;
                                } else if (vgravity == 80) {
                                    ypos = ((getHeight() - getPaddingBottom()) - customView.getMeasuredHeight()) - bottomMargin;
                                }
                                int customWidth = customView.getMeasuredWidth();
                                customView.layout(xpos, ypos, xpos + customWidth, customView.getMeasuredHeight() + ypos);
                                next(x2, customWidth, isLayoutRtl);
                            }
                            progressBar = this.mProgressView;
                            if (progressBar != null) {
                                progressBar.bringToFront();
                                int halfProgressHeight = this.mProgressView.getMeasuredHeight() / 2;
                                ProgressBar progressBar3 = this.mProgressView;
                                int i2 = this.mProgressBarPadding;
                                progressBar3.layout(i2, -halfProgressHeight, progressBar3.getMeasuredWidth() + i2, halfProgressHeight);
                                return;
                            }
                            return;
                        }
                    } else if (this.mListNavLayout != null) {
                        if (showTitle) {
                            x3 = next(x5, this.mItemPadding, isLayoutRtl);
                        } else {
                            x3 = x5;
                        }
                        x = next(x3 + positionChild(this.mListNavLayout, x3, y, contentHeight, isLayoutRtl), this.mItemPadding, isLayoutRtl);
                        if (this.mMenuView == null) {
                        }
                        x2 = x;
                        ProgressBar progressBar22 = this.mIndeterminateProgressView;
                        positionChild(this.mIndeterminateProgressView, menuStart, y, contentHeight, !isLayoutRtl);
                        menuStart += this.mIndeterminateProgressView.getMeasuredWidth() * direction;
                        customView = null;
                        if (this.mExpandedActionView != null) {
                        }
                        if (customView != null) {
                        }
                        progressBar = this.mProgressView;
                        if (progressBar != null) {
                        }
                    }
                }
                x = x5;
                if (this.mMenuView == null) {
                }
                x2 = x;
                ProgressBar progressBar222 = this.mIndeterminateProgressView;
                positionChild(this.mIndeterminateProgressView, menuStart, y, contentHeight, !isLayoutRtl);
                menuStart += this.mIndeterminateProgressView.getMeasuredWidth() * direction;
                customView = null;
                if (this.mExpandedActionView != null) {
                }
                if (customView != null) {
                }
                progressBar = this.mProgressView;
                if (progressBar != null) {
                }
            }
            startOffset = 0;
            int x52 = next(x4 + positionChild(this.mUpGoerFive, next(x4, startOffset, isLayoutRtl), y, contentHeight, isLayoutRtl), startOffset, isLayoutRtl);
            if (i == 1) {
            }
        }
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ActionBar.LayoutParams(getContext(), attrs);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp == null) {
            return generateDefaultLayoutParams();
        }
        return lp;
    }

    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        ExpandedActionViewMenuPresenter expandedActionViewMenuPresenter = this.mExpandedMenuPresenter;
        if (!(expandedActionViewMenuPresenter == null || expandedActionViewMenuPresenter.mCurrentExpandedItem == null)) {
            state.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        state.isOverflowOpen = isOverflowMenuShowing();
        return state;
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable p) {
        MenuBuilder menuBuilder;
        MenuItem item;
        SavedState state = (SavedState) p;
        super.onRestoreInstanceState(state.getSuperState());
        if (!(state.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || (menuBuilder = this.mOptionsMenu) == null || (item = menuBuilder.findItem(state.expandedMenuItemId)) == null)) {
            item.expandActionView();
        }
        if (state.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationIcon(Drawable indicator) {
        this.mHomeLayout.setUpIndicator(indicator);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDefaultNavigationIcon(Drawable icon) {
        this.mHomeLayout.setDefaultUpIndicator(icon);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationIcon(int resId) {
        this.mHomeLayout.setUpIndicator(resId);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationContentDescription(CharSequence description) {
        this.mHomeDescription = description;
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationContentDescription(int resId) {
        this.mHomeDescriptionRes = resId;
        this.mHomeDescription = resId != 0 ? getResources().getText(resId) : null;
        updateHomeAccessibility(this.mUpGoerFive.isEnabled());
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDefaultNavigationContentDescription(int defaultNavigationContentDescription) {
        if (this.mDefaultUpDescription != defaultNavigationContentDescription) {
            this.mDefaultUpDescription = defaultNavigationContentDescription;
            updateHomeAccessibility(this.mUpGoerFive.isEnabled());
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setMenuCallbacks(MenuPresenter.Callback presenterCallback, MenuBuilder.Callback menuBuilderCallback) {
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.setCallback(presenterCallback);
        }
        MenuBuilder menuBuilder = this.mOptionsMenu;
        if (menuBuilder != null) {
            menuBuilder.setCallback(menuBuilderCallback);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public Menu getMenu() {
        return this.mOptionsMenu;
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class com.android.internal.widget.ActionBarView.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.expandedMenuItemId);
            out.writeInt(this.isOverflowOpen ? 1 : 0);
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
            Drawable drawable = this.mUpIndicator;
            if (drawable != null) {
                this.mUpView.setImageDrawable(drawable);
            } else if (this.mUpIndicatorRes != 0) {
                this.mUpView.setImageDrawable(getContext().getDrawable(this.mUpIndicatorRes));
            } else {
                this.mUpView.setImageDrawable(this.mDefaultUpIndicator);
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            if (this.mUpIndicatorRes != 0) {
                updateUpIndicator();
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
            onPopulateAccessibilityEvent(event);
            return true;
        }

        @Override // android.view.View
        public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
            super.onPopulateAccessibilityEventInternal(event);
            CharSequence cdesc = getContentDescription();
            if (!TextUtils.isEmpty(cdesc)) {
                event.getText().add(cdesc);
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        public boolean dispatchHoverEvent(MotionEvent event) {
            return onHoverEvent(event);
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onFinishInflate() {
            this.mUpView = (ImageView) findViewById(R.id.up);
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
        @Override // android.widget.FrameLayout, android.view.View
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
        @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
        public void onLayout(boolean changed, int l, int t, int r, int b) {
            int r2;
            int l2;
            int iconRight;
            int vCenter;
            int upLeft;
            int upRight;
            int vCenter2 = (b - t) / 2;
            boolean isLayoutRtl = isLayoutRtl();
            int width = getWidth();
            int upOffset = 0;
            if (this.mUpView.getVisibility() != 8) {
                FrameLayout.LayoutParams upLp = (FrameLayout.LayoutParams) this.mUpView.getLayoutParams();
                int upHeight = this.mUpView.getMeasuredHeight();
                int upWidth = this.mUpView.getMeasuredWidth();
                upOffset = upLp.leftMargin + upWidth + upLp.rightMargin;
                int upTop = vCenter2 - (upHeight / 2);
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
            int iconTop = Math.max(iconLp.topMargin, vCenter2 - (iconHeight / 2));
            int iconBottom = iconTop + iconHeight;
            int delta = Math.max(iconLp.getMarginStart(), ((r2 - l2) / 2) - (iconWidth / 2));
            if (isLayoutRtl) {
                int iconRight2 = (width - upOffset) - delta;
                vCenter = iconRight2;
                iconRight = iconRight2 - iconWidth;
            } else {
                int iconLeft = upOffset + delta;
                vCenter = iconLeft + iconWidth;
                iconRight = iconLeft;
            }
            this.mIconView.layout(iconRight, iconTop, vCenter, iconBottom);
        }

        /* access modifiers changed from: protected */
        @Deprecated
        public void layoutUpView(View view, int upLeft, int upTop, int upRight, int upBottom, int leftMargin, int upOffset) {
        }
    }

    /* access modifiers changed from: private */
    public class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public void initForMenu(Context context, MenuBuilder menu) {
            MenuItemImpl menuItemImpl;
            MenuBuilder menuBuilder = this.mMenu;
            if (!(menuBuilder == null || (menuItemImpl = this.mCurrentExpandedItem) == null)) {
                menuBuilder.collapseItemActionView(menuItemImpl);
            }
            this.mMenu = menu;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public MenuView getMenuView(ViewGroup root) {
            return null;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public void updateMenuView(boolean cleared) {
            if (this.mCurrentExpandedItem != null) {
                boolean found = false;
                MenuBuilder menuBuilder = this.mMenu;
                if (menuBuilder != null) {
                    int count = menuBuilder.size();
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

        @Override // com.android.internal.view.menu.MenuPresenter
        public void setCallback(MenuPresenter.Callback cb) {
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            return false;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public boolean flagActionItems() {
            return false;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (ActionBarView.this.mExpandedHomeLayout == null) {
                ActionBarView.this.initExpandedHomeLayout();
            }
            ActionBarView.this.mExpandedActionView = item.getActionView();
            ActionBarView.this.mExpandedHomeLayout.setIcon(ActionBarView.this.mIcon.getConstantState().newDrawable(ActionBarView.this.getResources()));
            this.mCurrentExpandedItem = item;
            ViewParent parent = ActionBarView.this.mExpandedActionView.getParent();
            ActionBarView actionBarView = ActionBarView.this;
            if (parent != actionBarView) {
                actionBarView.addView(actionBarView.mExpandedActionView);
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

        @Override // com.android.internal.view.menu.MenuPresenter
        public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
            if (ActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) ActionBarView.this.mExpandedActionView).onActionViewCollapsed();
            }
            ActionBarView actionBarView = ActionBarView.this;
            actionBarView.removeView(actionBarView.mExpandedActionView);
            if (ActionBarView.this.mExpandedHomeLayout != null && ActionBarView.this.mExpandedHomeLayout.getParent() == ActionBarView.this.mUpGoerFive) {
                ActionBarView.this.mUpGoerFive.removeView(ActionBarView.this.mExpandedHomeLayout);
            }
            ActionBarView actionBarView2 = ActionBarView.this;
            actionBarView2.mExpandedActionView = null;
            if ((actionBarView2.mDisplayOptions & 2) != 0) {
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
            ActionBarView actionBarView3 = ActionBarView.this;
            actionBarView3.setHomeButtonEnabled(actionBarView3.mWasHomeEnabled);
            ActionBarView.this.requestLayout();
            item.setActionViewExpanded(false);
            return true;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public int getId() {
            return 0;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public Parcelable onSaveInstanceState() {
            return null;
        }

        @Override // com.android.internal.view.menu.MenuPresenter
        public void onRestoreInstanceState(Parcelable state) {
        }
    }

    @Deprecated
    public void setCustomTitle(View view) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public LinearLayout initTitleLayout() {
        return (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.action_bar_title_item, (ViewGroup) this, false);
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
            /* class com.android.internal.widget.ActionBarView.AnonymousClass2 */

            @Override // android.view.View.OnClickListener
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
