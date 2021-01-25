package com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Property;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ActionMenuPresenter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toolbar;
import com.android.internal.R;
import com.android.internal.view.menu.ActionMenuItem;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPresenter;
import java.lang.annotation.RCUnownedThisRef;

public class ToolbarWidgetWrapper implements DecorToolbar {
    private static final int AFFECTS_LOGO_MASK = 3;
    private static final long DEFAULT_FADE_DURATION_MS = 200;
    private static final String TAG = "ToolbarWidgetWrapper";
    private ActionMenuPresenter mActionMenuPresenter;
    private View mCustomView;
    private int mDefaultNavigationContentDescription;
    private Drawable mDefaultNavigationIcon;
    private int mDisplayOpts;
    private CharSequence mHomeDescription;
    private Drawable mIcon;
    private Drawable mLogo;
    private boolean mMenuPrepared;
    private Drawable mNavIcon;
    private int mNavigationMode;
    private Spinner mSpinner;
    private CharSequence mSubtitle;
    private View mTabView;
    private CharSequence mTitle;
    private boolean mTitleSet;
    private Toolbar mToolbar;
    private Window.Callback mWindowCallback;

    public ToolbarWidgetWrapper(Toolbar toolbar, boolean style) {
        this(toolbar, style, R.string.action_bar_up_description);
    }

    public ToolbarWidgetWrapper(Toolbar toolbar, boolean style, int defaultNavigationContentDescription) {
        Drawable drawable;
        this.mNavigationMode = 0;
        this.mDefaultNavigationContentDescription = 0;
        this.mToolbar = toolbar;
        this.mTitle = toolbar.getTitle();
        this.mSubtitle = toolbar.getSubtitle();
        this.mTitleSet = this.mTitle != null;
        this.mNavIcon = this.mToolbar.getNavigationIcon();
        TypedArray a = toolbar.getContext().obtainStyledAttributes(null, R.styleable.ActionBar, 16843470, 0);
        this.mDefaultNavigationIcon = a.getDrawable(13);
        if (style) {
            CharSequence title = a.getText(5);
            if (!TextUtils.isEmpty(title)) {
                setTitle(title);
            }
            CharSequence subtitle = a.getText(9);
            if (!TextUtils.isEmpty(subtitle)) {
                setSubtitle(subtitle);
            }
            Drawable logo = a.getDrawable(6);
            if (logo != null) {
                setLogo(logo);
            }
            Drawable icon = a.getDrawable(0);
            if (icon != null) {
                setIcon(icon);
            }
            if (this.mNavIcon == null && (drawable = this.mDefaultNavigationIcon) != null) {
                setNavigationIcon(drawable);
            }
            setDisplayOptions(a.getInt(8, 0));
            int customNavId = a.getResourceId(10, 0);
            if (customNavId != 0) {
                setCustomView(LayoutInflater.from(this.mToolbar.getContext()).inflate(customNavId, (ViewGroup) this.mToolbar, false));
                setDisplayOptions(this.mDisplayOpts | 16);
            }
            int height = a.getLayoutDimension(4, 0);
            if (height > 0) {
                ViewGroup.LayoutParams lp = this.mToolbar.getLayoutParams();
                lp.height = height;
                this.mToolbar.setLayoutParams(lp);
            }
            int contentInsetStart = a.getDimensionPixelOffset(22, -1);
            int contentInsetEnd = a.getDimensionPixelOffset(23, -1);
            if (contentInsetStart >= 0 || contentInsetEnd >= 0) {
                this.mToolbar.setContentInsetsRelative(Math.max(contentInsetStart, 0), Math.max(contentInsetEnd, 0));
            }
            int titleTextStyle = a.getResourceId(11, 0);
            if (titleTextStyle != 0) {
                Toolbar toolbar2 = this.mToolbar;
                toolbar2.setTitleTextAppearance(toolbar2.getContext(), titleTextStyle);
            }
            int subtitleTextStyle = a.getResourceId(12, 0);
            if (subtitleTextStyle != 0) {
                Toolbar toolbar3 = this.mToolbar;
                toolbar3.setSubtitleTextAppearance(toolbar3.getContext(), subtitleTextStyle);
            }
            int popupTheme = a.getResourceId(26, 0);
            if (popupTheme != 0) {
                this.mToolbar.setPopupTheme(popupTheme);
            }
        } else {
            this.mDisplayOpts = detectDisplayOptions();
        }
        a.recycle();
        setDefaultNavigationContentDescription(defaultNavigationContentDescription);
        this.mHomeDescription = this.mToolbar.getNavigationContentDescription();
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            /* class com.android.internal.widget.ToolbarWidgetWrapper.AnonymousClass1 */
            final ActionMenuItem mNavItem = new ActionMenuItem(ToolbarWidgetWrapper.this.mToolbar.getContext(), 0, 16908332, 0, 0, ToolbarWidgetWrapper.this.mTitle);

            @Override // android.view.View.OnClickListener
            @RCUnownedThisRef
            public void onClick(View v) {
                if (ToolbarWidgetWrapper.this.mWindowCallback != null && ToolbarWidgetWrapper.this.mMenuPrepared) {
                    ToolbarWidgetWrapper.this.mWindowCallback.onMenuItemSelected(0, this.mNavItem);
                }
            }
        });
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDefaultNavigationContentDescription(int defaultNavigationContentDescription) {
        if (defaultNavigationContentDescription != this.mDefaultNavigationContentDescription) {
            this.mDefaultNavigationContentDescription = defaultNavigationContentDescription;
            if (TextUtils.isEmpty(this.mToolbar.getNavigationContentDescription())) {
                setNavigationContentDescription(this.mDefaultNavigationContentDescription);
            }
        }
    }

    private int detectDisplayOptions() {
        if (this.mToolbar.getNavigationIcon() == null) {
            return 11;
        }
        int opts = 11 | 4;
        this.mDefaultNavigationIcon = this.mToolbar.getNavigationIcon();
        return opts;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public ViewGroup getViewGroup() {
        return this.mToolbar;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public Context getContext() {
        return this.mToolbar.getContext();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean isSplit() {
        return false;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasExpandedActionView() {
        return this.mToolbar.hasExpandedActionView();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void collapseActionView() {
        this.mToolbar.collapseActionView();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setWindowCallback(Window.Callback cb) {
        this.mWindowCallback = cb;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setWindowTitle(CharSequence title) {
        if (!this.mTitleSet) {
            setTitleInt(title);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public CharSequence getTitle() {
        return this.mToolbar.getTitle();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setTitle(CharSequence title) {
        this.mTitleSet = true;
        setTitleInt(title);
    }

    private void setTitleInt(CharSequence title) {
        this.mTitle = title;
        if ((this.mDisplayOpts & 8) != 0) {
            this.mToolbar.setTitle(title);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public CharSequence getSubtitle() {
        return this.mToolbar.getSubtitle();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSubtitle(CharSequence subtitle) {
        this.mSubtitle = subtitle;
        if ((this.mDisplayOpts & 8) != 0) {
            this.mToolbar.setSubtitle(subtitle);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void initProgress() {
        Log.i(TAG, "Progress display unsupported");
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void initIndeterminateProgress() {
        Log.i(TAG, "Progress display unsupported");
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean canSplit() {
        return false;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSplitView(ViewGroup splitView) {
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSplitActionBarAlways(boolean bAlwaysSplit) {
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSplitToolbar(boolean split) {
        if (split) {
            throw new UnsupportedOperationException("Cannot split an android.widget.Toolbar");
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSplitWhenNarrow(boolean splitWhenNarrow) {
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasIcon() {
        return this.mIcon != null;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasLogo() {
        return this.mLogo != null;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setIcon(int resId) {
        setIcon(resId != 0 ? getContext().getDrawable(resId) : null);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setIcon(Drawable d) {
        this.mIcon = d;
        updateToolbarLogo();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setLogo(int resId) {
        setLogo(resId != 0 ? getContext().getDrawable(resId) : null);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setLogo(Drawable d) {
        this.mLogo = d;
        updateToolbarLogo();
    }

    private void updateToolbarLogo() {
        Drawable logo = null;
        int i = this.mDisplayOpts;
        if ((i & 2) != 0) {
            if ((i & 1) != 0) {
                Drawable drawable = this.mLogo;
                if (drawable == null) {
                    drawable = this.mIcon;
                }
                logo = drawable;
            } else {
                logo = this.mIcon;
            }
        }
        this.mToolbar.setLogo(logo);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean canShowOverflowMenu() {
        return this.mToolbar.canShowOverflowMenu();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean isOverflowMenuShowing() {
        return this.mToolbar.isOverflowMenuShowing();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean isOverflowMenuShowPending() {
        return this.mToolbar.isOverflowMenuShowPending();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean showOverflowMenu() {
        return this.mToolbar.showOverflowMenu();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hideOverflowMenu() {
        return this.mToolbar.hideOverflowMenu();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setMenuPrepared() {
        this.mMenuPrepared = true;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        if (this.mActionMenuPresenter == null) {
            this.mActionMenuPresenter = new ActionMenuPresenter(this.mToolbar.getContext());
            this.mActionMenuPresenter.setId(R.id.action_menu_presenter);
        }
        this.mActionMenuPresenter.setCallback(cb);
        this.mToolbar.setMenu((MenuBuilder) menu, this.mActionMenuPresenter);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void dismissPopupMenus() {
        this.mToolbar.dismissPopupMenus();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getDisplayOptions() {
        return this.mDisplayOpts;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDisplayOptions(int newOpts) {
        View view;
        int changed = this.mDisplayOpts ^ newOpts;
        this.mDisplayOpts = newOpts;
        if (changed != 0) {
            if ((changed & 4) != 0) {
                if ((newOpts & 4) != 0) {
                    updateHomeAccessibility();
                }
                updateNavigationIcon();
            }
            if ((changed & 3) != 0) {
                updateToolbarLogo();
            }
            if ((changed & 8) != 0) {
                if ((newOpts & 8) != 0) {
                    this.mToolbar.setTitle(this.mTitle);
                    this.mToolbar.setSubtitle(this.mSubtitle);
                } else {
                    this.mToolbar.setTitle((CharSequence) null);
                    this.mToolbar.setSubtitle((CharSequence) null);
                }
            }
            if ((changed & 16) != 0 && (view = this.mCustomView) != null) {
                if ((newOpts & 16) != 0) {
                    this.mToolbar.addView(view);
                } else {
                    this.mToolbar.removeView(view);
                }
            }
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setEmbeddedTabView(ScrollingTabContainerView tabView) {
        Toolbar toolbar;
        View view = this.mTabView;
        if (view != null && view.getParent() == (toolbar = this.mToolbar)) {
            toolbar.removeView(this.mTabView);
        }
        this.mTabView = tabView;
        if (tabView != null && this.mNavigationMode == 2) {
            this.mToolbar.addView(this.mTabView, 0);
            Toolbar.LayoutParams lp = (Toolbar.LayoutParams) this.mTabView.getLayoutParams();
            lp.width = -2;
            lp.height = -2;
            lp.gravity = 8388691;
            tabView.setAllowCollapse(true);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean hasEmbeddedTabs() {
        return this.mTabView != null;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public boolean isTitleTruncated() {
        return this.mToolbar.isTitleTruncated();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setCollapsible(boolean collapsible) {
        this.mToolbar.setCollapsible(collapsible);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setHomeButtonEnabled(boolean enable) {
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getNavigationMode() {
        return this.mNavigationMode;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationMode(int mode) {
        Toolbar toolbar;
        View view;
        Toolbar toolbar2;
        int oldMode = this.mNavigationMode;
        if (mode != oldMode) {
            if (oldMode == 1) {
                Spinner spinner = this.mSpinner;
                if (spinner != null && spinner.getParent() == (toolbar = this.mToolbar)) {
                    toolbar.removeView(this.mSpinner);
                }
            } else if (oldMode == 2 && (view = this.mTabView) != null && view.getParent() == (toolbar2 = this.mToolbar)) {
                toolbar2.removeView(this.mTabView);
            }
            this.mNavigationMode = mode;
            if (mode == 0) {
                return;
            }
            if (mode == 1) {
                ensureSpinner();
                this.mToolbar.addView(this.mSpinner, 0);
            } else if (mode == 2) {
                View view2 = this.mTabView;
                if (view2 != null) {
                    this.mToolbar.addView(view2, 0);
                    Toolbar.LayoutParams lp = (Toolbar.LayoutParams) this.mTabView.getLayoutParams();
                    lp.width = -2;
                    lp.height = -2;
                    lp.gravity = 8388691;
                }
            } else {
                throw new IllegalArgumentException("Invalid navigation mode " + mode);
            }
        }
    }

    private void ensureSpinner() {
        if (this.mSpinner == null) {
            this.mSpinner = new Spinner(getContext(), null, 16843479);
            this.mSpinner.setLayoutParams(new Toolbar.LayoutParams(-2, -2, 8388627));
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDropdownParams(SpinnerAdapter adapter, AdapterView.OnItemSelectedListener listener) {
        ensureSpinner();
        this.mSpinner.setAdapter(adapter);
        this.mSpinner.setOnItemSelectedListener(listener);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDropdownSelectedPosition(int position) {
        Spinner spinner = this.mSpinner;
        if (spinner != null) {
            spinner.setSelection(position);
            return;
        }
        throw new IllegalStateException("Can't set dropdown selected position without an adapter");
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getDropdownSelectedPosition() {
        Spinner spinner = this.mSpinner;
        if (spinner != null) {
            return spinner.getSelectedItemPosition();
        }
        return 0;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getDropdownItemCount() {
        Spinner spinner = this.mSpinner;
        if (spinner != null) {
            return spinner.getCount();
        }
        return 0;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setCustomView(View view) {
        View view2 = this.mCustomView;
        if (!(view2 == null || (this.mDisplayOpts & 16) == 0)) {
            this.mToolbar.removeView(view2);
        }
        this.mCustomView = view;
        if (view != null && (this.mDisplayOpts & 16) != 0) {
            this.mToolbar.addView(this.mCustomView);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public View getCustomView() {
        return this.mCustomView;
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void animateToVisibility(int visibility) {
        Animator anim = setupAnimatorToVisibility(visibility, DEFAULT_FADE_DURATION_MS);
        if (anim != null) {
            anim.start();
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public Animator setupAnimatorToVisibility(int visibility, long duration) {
        if (visibility == 8) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(this.mToolbar, (Property<Toolbar, Float>) View.ALPHA, 1.0f, 0.0f);
            anim.setDuration(duration);
            anim.addListener(new AnimatorListenerAdapter() {
                /* class com.android.internal.widget.ToolbarWidgetWrapper.AnonymousClass2 */
                private boolean mCanceled = false;

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    if (!this.mCanceled) {
                        ToolbarWidgetWrapper.this.mToolbar.setVisibility(8);
                    }
                }

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animation) {
                    this.mCanceled = true;
                }
            });
            return anim;
        } else if (visibility != 0) {
            return null;
        } else {
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(this.mToolbar, (Property<Toolbar, Float>) View.ALPHA, 0.0f, 1.0f);
            anim2.setDuration(duration);
            anim2.addListener(new AnimatorListenerAdapter() {
                /* class com.android.internal.widget.ToolbarWidgetWrapper.AnonymousClass3 */

                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animation) {
                    ToolbarWidgetWrapper.this.mToolbar.setVisibility(0);
                }
            });
            return anim2;
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationIcon(Drawable icon) {
        this.mNavIcon = icon;
        updateNavigationIcon();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationIcon(int resId) {
        setNavigationIcon(resId != 0 ? this.mToolbar.getContext().getDrawable(resId) : null);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setDefaultNavigationIcon(Drawable defaultNavigationIcon) {
        if (this.mDefaultNavigationIcon != defaultNavigationIcon) {
            this.mDefaultNavigationIcon = defaultNavigationIcon;
            updateNavigationIcon();
        }
    }

    private void updateNavigationIcon() {
        if ((this.mDisplayOpts & 4) != 0) {
            Toolbar toolbar = this.mToolbar;
            Drawable drawable = this.mNavIcon;
            if (drawable == null) {
                drawable = this.mDefaultNavigationIcon;
            }
            toolbar.setNavigationIcon(drawable);
            return;
        }
        this.mToolbar.setNavigationIcon((Drawable) null);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationContentDescription(CharSequence description) {
        this.mHomeDescription = description;
        updateHomeAccessibility();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setNavigationContentDescription(int resId) {
        setNavigationContentDescription(resId == 0 ? null : getContext().getString(resId));
    }

    private void updateHomeAccessibility() {
        if ((this.mDisplayOpts & 4) == 0) {
            return;
        }
        if (TextUtils.isEmpty(this.mHomeDescription)) {
            this.mToolbar.setNavigationContentDescription(this.mDefaultNavigationContentDescription);
        } else {
            this.mToolbar.setNavigationContentDescription(this.mHomeDescription);
        }
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void saveHierarchyState(SparseArray<Parcelable> toolbarStates) {
        this.mToolbar.saveHierarchyState(toolbarStates);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void restoreHierarchyState(SparseArray<Parcelable> toolbarStates) {
        this.mToolbar.restoreHierarchyState(toolbarStates);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setBackgroundDrawable(Drawable d) {
        this.mToolbar.setBackgroundDrawable(d);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getHeight() {
        return this.mToolbar.getHeight();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setVisibility(int visible) {
        this.mToolbar.setVisibility(visible);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public int getVisibility() {
        return this.mToolbar.getVisibility();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setMenuCallbacks(MenuPresenter.Callback presenterCallback, MenuBuilder.Callback menuBuilderCallback) {
        this.mToolbar.setMenuCallbacks(presenterCallback, menuBuilderCallback);
    }

    @Override // com.android.internal.widget.DecorToolbar
    public Menu getMenu() {
        return this.mToolbar.getMenu();
    }

    @Override // com.android.internal.widget.DecorToolbar
    public void setSplitViewLocation(int start, int end) {
    }
}
