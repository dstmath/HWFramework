package com.android.internal.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.UnsupportedAppUsage;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.rms.AppAssociate;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.Toolbar;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;
import com.android.internal.widget.ActionBarContainer;
import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ScrollingTabContainerView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WindowDecorActionBar extends ActionBar implements ActionBarOverlayLayout.ActionBarVisibilityCallback {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final long FADE_IN_DURATION_MS = 200;
    private static final float FADE_IN_START_ALPHA = 0.1f;
    private static final long FADE_OUT_DURATION_MS = 100;
    private static final String GALLERY3DNAME_NEW = "com.huawei.photos.app.Gallery";
    private static final String GALLERY3DNAME_OLD = "com.android.gallery3d.app.Gallery";
    private static final String HWCAMERANAME = "com.android.hwcamera.Camera";
    private static final int INVALID_POSITION = -1;
    private static final String TAG = "WindowDecorActionBar";
    private static final int TRANSITION_DURATION = 120;
    private static final PathInterpolator TRANSITION_INTERPOLATOER = new PathInterpolator(0.27f, 0.11f, 0.08f, 0.97f);
    ActionMode mActionMode;
    private Activity mActivity;
    private ActionBarContainer mContainerView;
    private ImageView mContainerViewCopy;
    private boolean mContentAnimations = true;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    @UnsupportedAppUsage
    private ActionBarContextView mContextView;
    private int mCurWindowVisibility = 0;
    private Animator mCurrentShowAnim;
    private DecorToolbar mDecorToolbar;
    private View mDecorView;
    ActionMode mDeferredDestroyActionMode;
    ActionMode.Callback mDeferredModeDestroyCallback;
    private Dialog mDialog;
    private boolean mDisplayHomeAsUpSet;
    private boolean mHasEmbeddedTabs;
    private boolean mHiddenByApp;
    private boolean mHiddenBySystem;
    final Animator.AnimatorListener mHideListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass1 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            if (WindowDecorActionBar.this.mContentAnimations && WindowDecorActionBar.this.mContentView != null) {
                WindowDecorActionBar.this.mContentView.setTranslationY(0.0f);
                WindowDecorActionBar.this.mContainerView.setTranslationY(0.0f);
            }
            if (WindowDecorActionBar.this.mSplitView != null && WindowDecorActionBar.this.mContextDisplayMode == 1) {
                WindowDecorActionBar.this.mSplitView.setVisibility(8);
            }
            WindowDecorActionBar.this.mContainerView.setVisibility(8);
            WindowDecorActionBar.this.mContainerView.setTransitioning(false);
            WindowDecorActionBar.this.mCurrentShowAnim = null;
            WindowDecorActionBar.this.completeDeferredDestroyActionMode();
            if (WindowDecorActionBar.this.mOverlayLayout != null) {
                WindowDecorActionBar.this.mOverlayLayout.requestApplyInsets();
            }
        }
    };
    boolean mHideOnContentScroll;
    private boolean mIsGalleryActiviy = true;
    private boolean mLastMenuVisibility;
    private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList<>();
    private boolean mNowShowing = true;
    private ActionBarOverlayLayout mOverlayLayout;
    private int mSavedTabPosition = -1;
    private TabImpl mSelectedTab;
    private boolean mShouldTransition = false;
    private boolean mShowHideAnimationEnabled;
    final Animator.AnimatorListener mShowListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass2 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            WindowDecorActionBar.this.mCurrentShowAnim = null;
            WindowDecorActionBar.this.mContainerView.requestLayout();
        }
    };
    private boolean mShowingForMode;
    private ActionBarContainer mSplitView;
    @UnsupportedAppUsage
    private ScrollingTabContainerView mTabScrollView;
    private ArrayList<TabImpl> mTabs = new ArrayList<>();
    private Context mThemedContext;
    private AnimatorSet mTransitionAnim;
    final Animator.AnimatorListener mTransitionListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass4 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationStart(Animator animation) {
            if (WindowDecorActionBar.this.mContainerView != null) {
                WindowDecorActionBar.this.mContainerView.setAlpha(0.0f);
            }
        }
    };
    final ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        /* class com.android.internal.app.WindowDecorActionBar.AnonymousClass3 */

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator animation) {
            ((View) WindowDecorActionBar.this.mContainerView.getParent()).invalidate();
        }
    };

    public WindowDecorActionBar(Activity activity) {
        this.mActivity = activity;
        Window window = activity.getWindow();
        this.mIsGalleryActiviy = checkGalleryActivity(activity);
        View decor = window.getDecorView();
        boolean overlayMode = this.mActivity.getWindow().hasFeature(9);
        init(decor);
        if (!overlayMode) {
            this.mContentView = decor.findViewById(16908290);
        }
    }

    public WindowDecorActionBar(Dialog dialog) {
        this.mDialog = dialog;
        init(dialog.getWindow().getDecorView());
    }

    public WindowDecorActionBar(View layout) {
        init(layout);
    }

    private void init(View decor) {
        this.mDecorToolbar = getDecorToolbar(decor.findViewById(R.id.action_bar));
        this.mContext = this.mDecorToolbar.getContext();
        initCustomPanel(decor);
        initActionBarOverlayLayout(decor);
        ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
        if (actionBarOverlayLayout != null) {
            actionBarOverlayLayout.setActionBarVisibilityCallback(this);
        }
        initContextView(decor);
        initContainerView(decor);
        this.mSplitView = (ActionBarContainer) decor.findViewById(R.id.split_action_bar);
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (decorToolbar == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with a compatible window decor layout");
        }
        this.mContextDisplayMode = decorToolbar.isSplit() ? 1 : 0;
        boolean homeAsUp = (this.mDecorToolbar.getDisplayOptions() & 4) != 0;
        if (homeAsUp) {
            this.mDisplayHomeAsUpSet = true;
        }
        ActionBarPolicy abp = ActionBarPolicy.get(this.mContext);
        setHomeButtonEnabled(abp.enableHomeButtonByDefault() || homeAsUp);
        setHasEmbeddedTabs(abp.hasEmbeddedTabs());
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.ActionBar, 16843470, 0);
        if (a.getBoolean(21, false)) {
            setHideOnContentScrollEnabled(true);
        }
        int elevation = a.getDimensionPixelSize(20, 0);
        if (elevation != 0) {
            setElevation((float) elevation);
        }
        a.recycle();
        this.mDecorView = decor;
        this.mTransitionAnim = new AnimatorSet();
    }

    public void setShoudTransition(boolean shouldTransition) {
        this.mShouldTransition = shouldTransition;
    }

    private void initActionBarTransition() {
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(this.mContainerViewCopy, AppAssociate.ASSOC_WINDOW_ALPHA, 1.0f, 0.0f);
        fadeOutAnim.setDuration(120L);
        fadeOutAnim.setInterpolator(TRANSITION_INTERPOLATOER);
        fadeOutAnim.addListener(this.mTransitionListener);
        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(this.mContainerView, AppAssociate.ASSOC_WINDOW_ALPHA, FADE_IN_START_ALPHA, 1.0f);
        fadeInAnim.setDuration(120L);
        fadeInAnim.setInterpolator(TRANSITION_INTERPOLATOER);
        this.mTransitionAnim.playSequentially(fadeOutAnim, fadeInAnim);
    }

    private void startActionBarTransition() {
        AnimatorSet animatorSet;
        ActionBarContainer actionBarContainer;
        if (!this.mShouldTransition || (animatorSet = this.mTransitionAnim) == null || animatorSet.isRunning() || (actionBarContainer = this.mContainerView) == null || !actionBarContainer.isLaidOut()) {
            Log.w(TAG, "should not do the transition or the transition anim is null or it is running or the mContainer view is null or mContainer view hasn't been drawn to screen ");
            return;
        }
        if (this.mContainerViewCopy == null) {
            initContainerViewCopy();
            initActionBarTransition();
        }
        if (this.mContainerViewCopy != null) {
            this.mContainerView.setDrawingCacheEnabled(true);
            Bitmap bitmapCache = this.mContainerView.getDrawingCache();
            Bitmap bitmapForCopy = null;
            if (bitmapCache != null) {
                bitmapForCopy = Bitmap.createBitmap(bitmapCache);
            }
            this.mContainerView.setDrawingCacheEnabled(false);
            if (bitmapForCopy != null) {
                this.mContainerViewCopy.setImageBitmap(bitmapForCopy);
                this.mTransitionAnim.start();
            }
        }
    }

    private DecorToolbar getDecorToolbar(View view) {
        if (view instanceof DecorToolbar) {
            return (DecorToolbar) view;
        }
        if (view instanceof Toolbar) {
            return ((Toolbar) view).getWrapper();
        }
        String sn = view == null ? "NULL Object" : view.getClass().getSimpleName();
        throw new IllegalStateException("Can't make a decor toolbar out of " + sn);
    }

    @Override // android.app.ActionBar
    public void setElevation(float elevation) {
        this.mContainerView.setElevation(elevation);
        ActionBarContainer actionBarContainer = this.mSplitView;
        if (actionBarContainer != null) {
            actionBarContainer.setElevation(elevation);
        }
    }

    @Override // android.app.ActionBar
    public float getElevation() {
        return this.mContainerView.getElevation();
    }

    @Override // android.app.ActionBar
    public void onConfigurationChanged(Configuration newConfig) {
        setHasEmbeddedTabs(ActionBarPolicy.get(this.mContext).hasEmbeddedTabs());
    }

    private void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        this.mHasEmbeddedTabs = hasEmbeddedTabs;
        if (!this.mHasEmbeddedTabs) {
            this.mDecorToolbar.setEmbeddedTabView(null);
            this.mContainerView.setTabContainer(this.mTabScrollView);
        } else {
            this.mContainerView.setTabContainer(null);
            this.mDecorToolbar.setEmbeddedTabView(this.mTabScrollView);
        }
        boolean z = true;
        boolean isInTabMode = getNavigationMode() == 2;
        ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
        if (scrollingTabContainerView != null) {
            if (isInTabMode) {
                scrollingTabContainerView.setVisibility(0);
                ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
                if (actionBarOverlayLayout != null) {
                    actionBarOverlayLayout.requestApplyInsets();
                }
            } else {
                scrollingTabContainerView.setVisibility(8);
            }
        }
        this.mDecorToolbar.setCollapsible(!this.mHasEmbeddedTabs && isInTabMode);
        ActionBarOverlayLayout actionBarOverlayLayout2 = this.mOverlayLayout;
        if (this.mHasEmbeddedTabs || !isInTabMode) {
            z = false;
        }
        actionBarOverlayLayout2.setHasNonEmbeddedTabs(z);
    }

    private void ensureTabsExist() {
        if (this.mTabScrollView == null) {
            ScrollingTabContainerView tabScroller = new ScrollingTabContainerView(this.mContext);
            if (this.mHasEmbeddedTabs) {
                tabScroller.setVisibility(0);
                this.mDecorToolbar.setEmbeddedTabView(tabScroller);
            } else {
                if (getNavigationMode() == 2) {
                    tabScroller.setVisibility(0);
                    ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
                    if (actionBarOverlayLayout != null) {
                        actionBarOverlayLayout.requestApplyInsets();
                    }
                } else {
                    tabScroller.setVisibility(8);
                }
                this.mContainerView.setTabContainer(tabScroller);
            }
            this.mTabScrollView = tabScroller;
        }
    }

    /* access modifiers changed from: package-private */
    public void completeDeferredDestroyActionMode() {
        ActionMode.Callback callback = this.mDeferredModeDestroyCallback;
        if (callback != null) {
            callback.onDestroyActionMode(this.mDeferredDestroyActionMode);
            this.mDeferredDestroyActionMode = null;
            this.mDeferredModeDestroyCallback = null;
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void onWindowVisibilityChanged(int visibility) {
        this.mCurWindowVisibility = visibility;
    }

    @Override // android.app.ActionBar
    @UnsupportedAppUsage
    public void setShowHideAnimationEnabled(boolean enabled) {
        Animator animator;
        this.mShowHideAnimationEnabled = enabled;
        if (!enabled && (animator = this.mCurrentShowAnim) != null) {
            animator.end();
        }
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

    @Override // android.app.ActionBar
    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(getThemedContext()).inflate(resId, this.mDecorToolbar.getViewGroup(), false));
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
    public void setHomeButtonEnabled(boolean enable) {
        this.mDecorToolbar.setHomeButtonEnabled(enable);
    }

    @Override // android.app.ActionBar
    public void setTitle(int resId) {
        setTitle(this.mContext.getString(resId));
    }

    @Override // android.app.ActionBar
    public void setSubtitle(int resId) {
        setSubtitle(this.mContext.getString(resId));
    }

    @Override // android.app.ActionBar
    public void setSelectedNavigationItem(int position) {
        int navigationMode = this.mDecorToolbar.getNavigationMode();
        if (navigationMode == 1) {
            this.mDecorToolbar.setDropdownSelectedPosition(position);
        } else if (navigationMode == 2) {
            selectTab(this.mTabs.get(position));
        } else {
            throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    @Override // android.app.ActionBar
    public void removeAllTabs() {
        cleanupTabs();
    }

    private void cleanupTabs() {
        if (this.mSelectedTab != null) {
            selectTab(null);
        }
        this.mTabs.clear();
        ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
        if (scrollingTabContainerView != null) {
            scrollingTabContainerView.removeAllTabs();
        }
        this.mSavedTabPosition = -1;
    }

    @Override // android.app.ActionBar
    public void setTitle(CharSequence title) {
        this.mDecorToolbar.setTitle(title);
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
    public void setDisplayOptions(int options) {
        startActionBarTransition();
        if ((options & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mDecorToolbar.setDisplayOptions(options);
    }

    @Override // android.app.ActionBar
    public void setDisplayOptions(int options, int mask) {
        startActionBarTransition();
        int current = this.mDecorToolbar.getDisplayOptions();
        if ((mask & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mDecorToolbar.setDisplayOptions((options & mask) | ((~mask) & current));
    }

    @Override // android.app.ActionBar
    public void setBackgroundDrawable(Drawable d) {
        this.mContainerView.setPrimaryBackground(d);
    }

    @Override // android.app.ActionBar
    public void setStackedBackgroundDrawable(Drawable d) {
        this.mContainerView.setStackedBackground(d);
    }

    @Override // android.app.ActionBar
    public void setSplitBackgroundDrawable(Drawable d) {
        ActionBarContainer actionBarContainer = this.mSplitView;
        if (actionBarContainer != null) {
            actionBarContainer.setSplitBackground(d);
        }
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
        return this.mDecorToolbar.getNavigationMode();
    }

    @Override // android.app.ActionBar
    public int getDisplayOptions() {
        return this.mDecorToolbar.getDisplayOptions();
    }

    @Override // android.app.ActionBar
    public ActionMode startActionMode(ActionMode.Callback callback) {
        ActionMode actionMode = this.mActionMode;
        if (actionMode != null) {
            actionMode.finish();
        }
        this.mOverlayLayout.setHideOnContentScrollEnabled(false);
        this.mContextView.killMode();
        ActionModeImpl mode = new ActionModeImpl(this.mContextView.getContext(), callback);
        if (!mode.dispatchOnCreate()) {
            return null;
        }
        this.mActionMode = mode;
        mode.invalidate();
        this.mContextView.initForMode(mode);
        animateToMode(true);
        ActionBarContainer actionBarContainer = this.mSplitView;
        if (!(actionBarContainer == null || this.mContextDisplayMode != 1 || actionBarContainer.getVisibility() == 0)) {
            this.mSplitView.setVisibility(0);
            ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
            if (actionBarOverlayLayout != null) {
                actionBarOverlayLayout.requestApplyInsets();
            }
        }
        this.mContextView.sendAccessibilityEvent(32);
        return mode;
    }

    private void configureTab(ActionBar.Tab tab, int position) {
        TabImpl tabi = (TabImpl) tab;
        if (tabi.getCallback() != null) {
            tabi.setPosition(position);
            this.mTabs.add(position, tabi);
            int count = this.mTabs.size();
            for (int i = position + 1; i < count; i++) {
                this.mTabs.get(i).setPosition(i);
            }
            return;
        }
        throw new IllegalStateException("Action Bar Tab must have a Callback");
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, setSelected);
        configureTab(tab, this.mTabs.size());
        if (setSelected) {
            selectTab(tab);
        }
    }

    @Override // android.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            selectTab(tab);
        }
    }

    @Override // android.app.ActionBar
    public ActionBar.Tab newTab() {
        return new TabImpl();
    }

    @Override // android.app.ActionBar
    public void removeTab(ActionBar.Tab tab) {
        removeTabAt(tab.getPosition());
    }

    @Override // android.app.ActionBar
    public void removeTabAt(int position) {
        if (this.mTabScrollView != null) {
            TabImpl tabImpl = this.mSelectedTab;
            int selectedTabPosition = tabImpl != null ? tabImpl.getPosition() : this.mSavedTabPosition;
            this.mTabScrollView.removeTabAt(position);
            TabImpl removedTab = this.mTabs.remove(position);
            if (removedTab != null) {
                removedTab.setPosition(-1);
            }
            int newTabCount = this.mTabs.size();
            for (int i = position; i < newTabCount; i++) {
                this.mTabs.get(i).setPosition(i);
            }
            if (selectedTabPosition == position) {
                selectTab(this.mTabs.isEmpty() ? null : this.mTabs.get(Math.max(0, position - 1)));
            }
        }
    }

    @Override // android.app.ActionBar
    public void selectTab(ActionBar.Tab tab) {
        FragmentTransaction trans;
        int i = -1;
        if (getNavigationMode() != 2) {
            if (tab != null) {
                i = tab.getPosition();
            }
            this.mSavedTabPosition = i;
            return;
        }
        if (this.mDecorToolbar.getViewGroup().isInEditMode()) {
            trans = null;
        } else {
            trans = this.mActivity.getFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        TabImpl tabImpl = this.mSelectedTab;
        if (tabImpl != tab) {
            ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
            if (tab != null) {
                i = tab.getPosition();
            }
            scrollingTabContainerView.setTabSelected(i);
            TabImpl tabImpl2 = this.mSelectedTab;
            if (tabImpl2 != null) {
                tabImpl2.getCallback().onTabUnselected(this.mSelectedTab, trans);
            }
            this.mSelectedTab = (TabImpl) tab;
            TabImpl tabImpl3 = this.mSelectedTab;
            if (tabImpl3 != null) {
                tabImpl3.getCallback().onTabSelected(this.mSelectedTab, trans);
            }
        } else if (tabImpl != null) {
            tabImpl.getCallback().onTabReselected(this.mSelectedTab, trans);
            this.mTabScrollView.animateToTab(tab.getPosition());
        }
        if (trans != null && !trans.isEmpty()) {
            trans.commit();
        }
    }

    @Override // android.app.ActionBar
    public ActionBar.Tab getSelectedTab() {
        return this.mSelectedTab;
    }

    @Override // android.app.ActionBar
    public int getHeight() {
        return this.mContainerView.getHeight();
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void enableContentAnimations(boolean enabled) {
        this.mContentAnimations = enabled;
    }

    @Override // android.app.ActionBar
    public void show() {
        if (this.mHiddenByApp) {
            this.mHiddenByApp = false;
            updateVisibility(false);
        }
    }

    private void showForActionMode() {
        if (!this.mShowingForMode) {
            this.mShowingForMode = true;
            ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
            if (actionBarOverlayLayout != null) {
                actionBarOverlayLayout.setShowingForActionMode(true);
            }
            updateVisibility(false);
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void showForSystem() {
        if (this.mHiddenBySystem) {
            this.mHiddenBySystem = false;
            updateVisibility(true);
        }
    }

    @Override // android.app.ActionBar
    public void hide() {
        if (!this.mHiddenByApp) {
            this.mHiddenByApp = true;
            updateVisibility(false);
        }
    }

    private void hideForActionMode() {
        if (this.mShowingForMode) {
            this.mShowingForMode = false;
            ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
            if (actionBarOverlayLayout != null) {
                actionBarOverlayLayout.setShowingForActionMode(false);
            }
            updateVisibility(false);
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void hideForSystem() {
        if (!this.mHiddenBySystem) {
            this.mHiddenBySystem = true;
            updateVisibility(true);
        }
    }

    @Override // android.app.ActionBar
    public void setHideOnContentScrollEnabled(boolean hideOnContentScroll) {
        if (!hideOnContentScroll || this.mOverlayLayout.isInOverlayMode()) {
            this.mHideOnContentScroll = hideOnContentScroll;
            this.mOverlayLayout.setHideOnContentScrollEnabled(hideOnContentScroll);
            return;
        }
        throw new IllegalStateException("Action bar must be in overlay mode (Window.FEATURE_OVERLAY_ACTION_BAR) to enable hide on content scroll");
    }

    @Override // android.app.ActionBar
    public boolean isHideOnContentScrollEnabled() {
        return this.mOverlayLayout.isHideOnContentScrollEnabled();
    }

    @Override // android.app.ActionBar
    public int getHideOffset() {
        return this.mOverlayLayout.getActionBarHideOffset();
    }

    @Override // android.app.ActionBar
    public void setHideOffset(int offset) {
        if (offset == 0 || this.mOverlayLayout.isInOverlayMode()) {
            this.mOverlayLayout.setActionBarHideOffset(offset);
            return;
        }
        throw new IllegalStateException("Action bar must be in overlay mode (Window.FEATURE_OVERLAY_ACTION_BAR) to set a non-zero hide offset");
    }

    /* access modifiers changed from: private */
    public static boolean checkShowingFlags(boolean hiddenByApp, boolean hiddenBySystem, boolean showingForMode) {
        if (showingForMode) {
            return true;
        }
        if (hiddenByApp || hiddenBySystem) {
            return false;
        }
        return true;
    }

    private void updateVisibility(boolean fromSystem) {
        if (checkShowingFlags(this.mHiddenByApp, this.mHiddenBySystem, this.mShowingForMode)) {
            if (!this.mNowShowing) {
                this.mNowShowing = true;
                doShow(fromSystem);
            }
        } else if (this.mNowShowing) {
            this.mNowShowing = false;
            doHide(fromSystem);
        }
    }

    public void doShow(boolean fromSystem) {
        View view;
        View view2;
        Animator animator = this.mCurrentShowAnim;
        if (animator != null) {
            animator.end();
        }
        this.mContainerView.setVisibility(0);
        if (this.mCurWindowVisibility != 0 || (!this.mShowHideAnimationEnabled && !fromSystem)) {
            this.mContainerView.setAlpha(1.0f);
            this.mContainerView.setTranslationY(0.0f);
            if (this.mContentAnimations && (view = this.mContentView) != null) {
                view.setTranslationY(0.0f);
            }
            ActionBarContainer actionBarContainer = this.mSplitView;
            if (actionBarContainer != null && this.mContextDisplayMode == 1) {
                actionBarContainer.setAlpha(1.0f);
                this.mSplitView.setTranslationY(0.0f);
                this.mSplitView.setVisibility(0);
            }
            this.mShowListener.onAnimationEnd(null);
        } else {
            this.mContainerView.setTranslationY(0.0f);
            float startingY = (float) (-this.mContainerView.getHeight());
            if (fromSystem) {
                int[] topLeft = {0, 0};
                this.mContainerView.getLocationInWindow(topLeft);
                startingY -= (float) topLeft[1];
            }
            if (this.mIsGalleryActiviy) {
                startingY = 0.0f;
            }
            this.mContainerView.setTranslationY(startingY);
            AnimatorSet anim = new AnimatorSet();
            ObjectAnimator a = ObjectAnimator.ofFloat(this.mContainerView, View.TRANSLATION_Y, 0.0f);
            a.addUpdateListener(this.mUpdateListener);
            AnimatorSet.Builder b = anim.play(a);
            if (this.mContentAnimations && (view2 = this.mContentView) != null) {
                b.with(ObjectAnimator.ofFloat(view2, View.TRANSLATION_Y, startingY, 0.0f));
            }
            ActionBarContainer actionBarContainer2 = this.mSplitView;
            if (actionBarContainer2 != null && this.mContextDisplayMode == 1) {
                actionBarContainer2.setTranslationY((float) actionBarContainer2.getHeight());
                this.mSplitView.setVisibility(0);
                b.with(ObjectAnimator.ofFloat(this.mSplitView, View.TRANSLATION_Y, 0.0f));
            }
            anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563651));
            anim.setDuration(250L);
            anim.addListener(this.mShowListener);
            this.mCurrentShowAnim = anim;
            anim.start();
        }
        ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
        if (actionBarOverlayLayout != null) {
            actionBarOverlayLayout.requestApplyInsets();
        }
    }

    public void doHide(boolean fromSystem) {
        View view;
        Animator animator = this.mCurrentShowAnim;
        if (animator != null) {
            animator.end();
        }
        if (this.mCurWindowVisibility != 0 || (!this.mShowHideAnimationEnabled && !fromSystem)) {
            this.mHideListener.onAnimationEnd(null);
            return;
        }
        this.mContainerView.setAlpha(1.0f);
        this.mContainerView.setTransitioning(true);
        AnimatorSet anim = new AnimatorSet();
        float endingY = (float) (-this.mContainerView.getHeight());
        if (fromSystem) {
            int[] topLeft = {0, 0};
            this.mContainerView.getLocationInWindow(topLeft);
            endingY -= (float) topLeft[1];
        }
        ObjectAnimator a = ObjectAnimator.ofFloat(this.mContainerView, View.TRANSLATION_Y, endingY);
        a.addUpdateListener(this.mUpdateListener);
        AnimatorSet.Builder b = anim.play(a);
        if (this.mContentAnimations && (view = this.mContentView) != null) {
            b.with(ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0.0f, endingY));
        }
        ActionBarContainer actionBarContainer = this.mSplitView;
        if (actionBarContainer != null && actionBarContainer.getVisibility() == 0) {
            this.mSplitView.setAlpha(1.0f);
            b.with(ObjectAnimator.ofFloat(this.mSplitView, View.TRANSLATION_Y, (float) this.mSplitView.getHeight()));
        }
        anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563650));
        anim.setDuration(250L);
        anim.addListener(this.mHideListener);
        this.mCurrentShowAnim = anim;
        anim.start();
    }

    @Override // android.app.ActionBar
    public boolean isShowing() {
        int height = getHeight();
        return this.mNowShowing && (height == 0 || getHideOffset() < height);
    }

    /* access modifiers changed from: protected */
    public void animateToMode(boolean toActionMode) {
        Animator fadeIn;
        Animator fadeIn2;
        if (toActionMode) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        if (shouldAnimateContextView()) {
            if (toActionMode) {
                fadeIn2 = this.mDecorToolbar.setupAnimatorToVisibility(8, FADE_OUT_DURATION_MS);
                fadeIn = this.mContextView.setupAnimatorToVisibility(0, FADE_IN_DURATION_MS);
            } else {
                fadeIn = this.mDecorToolbar.setupAnimatorToVisibility(0, FADE_IN_DURATION_MS);
                fadeIn2 = this.mContextView.setupAnimatorToVisibility(8, FADE_OUT_DURATION_MS);
            }
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(fadeIn2, fadeIn);
            set.start();
        } else if (toActionMode) {
            this.mDecorToolbar.setVisibility(8);
            this.mContextView.setVisibility(0);
        } else {
            this.mDecorToolbar.setVisibility(0);
            this.mContextView.setVisibility(8);
        }
    }

    private boolean shouldAnimateContextView() {
        return this.mContainerView.isLaidOut();
    }

    @Override // android.app.ActionBar
    public Context getThemedContext() {
        if (this.mThemedContext == null) {
            TypedValue outValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(16843671, outValue, true);
            int targetThemeRes = outValue.resourceId;
            if (targetThemeRes == 0 || this.mContext.getThemeResId() == targetThemeRes) {
                this.mThemedContext = this.mContext;
            } else {
                this.mThemedContext = new ContextThemeWrapper(this.mContext, targetThemeRes);
            }
        }
        return this.mThemedContext;
    }

    @Override // android.app.ActionBar
    public boolean isTitleTruncated() {
        DecorToolbar decorToolbar = this.mDecorToolbar;
        return decorToolbar != null && decorToolbar.isTitleTruncated();
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
    public void setHomeActionContentDescription(int resId) {
        this.mDecorToolbar.setNavigationContentDescription(resId);
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void onContentScrollStarted() {
        Animator animator = this.mCurrentShowAnim;
        if (animator != null) {
            animator.cancel();
            this.mCurrentShowAnim = null;
        }
    }

    @Override // com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback
    public void onContentScrollStopped() {
    }

    @Override // android.app.ActionBar
    public boolean collapseActionView() {
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (decorToolbar == null || !decorToolbar.hasExpandedActionView()) {
            return false;
        }
        this.mDecorToolbar.collapseActionView();
        return true;
    }

    public class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
        private final Context mActionModeContext;
        private ActionMode.Callback mCallback;
        private WeakReference<View> mCustomView;
        private final MenuBuilder mMenu;

        public ActionModeImpl(Context context, ActionMode.Callback callback) {
            this.mActionModeContext = context;
            this.mCallback = callback;
            this.mMenu = new MenuBuilder(context).setDefaultShowAsAction(1);
            this.mMenu.setCallback(this);
        }

        @Override // android.view.ActionMode
        public MenuInflater getMenuInflater() {
            return new MenuInflater(this.mActionModeContext);
        }

        @Override // android.view.ActionMode
        public Menu getMenu() {
            return this.mMenu;
        }

        @Override // android.view.ActionMode
        public void finish() {
            if (WindowDecorActionBar.this.mActionMode == this) {
                if (!WindowDecorActionBar.checkShowingFlags(WindowDecorActionBar.this.mHiddenByApp, WindowDecorActionBar.this.mHiddenBySystem, false)) {
                    WindowDecorActionBar windowDecorActionBar = WindowDecorActionBar.this;
                    windowDecorActionBar.mDeferredDestroyActionMode = this;
                    windowDecorActionBar.mDeferredModeDestroyCallback = this.mCallback;
                } else {
                    this.mCallback.onDestroyActionMode(this);
                }
                this.mCallback = null;
                WindowDecorActionBar.this.animateToMode(false);
                WindowDecorActionBar.this.mContextView.closeMode();
                WindowDecorActionBar.this.mDecorToolbar.getViewGroup().sendAccessibilityEvent(32);
                WindowDecorActionBar.this.mOverlayLayout.setHideOnContentScrollEnabled(WindowDecorActionBar.this.mHideOnContentScroll);
                WindowDecorActionBar.this.mActionMode = null;
            }
        }

        @Override // android.view.ActionMode
        public void invalidate() {
            if (WindowDecorActionBar.this.mActionMode == this) {
                this.mMenu.stopDispatchingItemsChanged();
                try {
                    this.mCallback.onPrepareActionMode(this, this.mMenu);
                } finally {
                    this.mMenu.startDispatchingItemsChanged();
                }
            }
        }

        public boolean dispatchOnCreate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                return this.mCallback.onCreateActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        @Override // android.view.ActionMode
        public void setCustomView(View view) {
            WindowDecorActionBar.this.mContextView.setCustomView(view);
            this.mCustomView = new WeakReference<>(view);
        }

        @Override // android.view.ActionMode
        public void setSubtitle(CharSequence subtitle) {
            WindowDecorActionBar.this.mContextView.setSubtitle(subtitle);
        }

        @Override // android.view.ActionMode
        public void setTitle(CharSequence title) {
            WindowDecorActionBar.this.mContextView.setTitle(title);
        }

        @Override // android.view.ActionMode
        public void setTitle(int resId) {
            setTitle(WindowDecorActionBar.this.mContext.getResources().getString(resId));
        }

        @Override // android.view.ActionMode
        public void setSubtitle(int resId) {
            setSubtitle(WindowDecorActionBar.this.mContext.getResources().getString(resId));
        }

        @Override // android.view.ActionMode
        public CharSequence getTitle() {
            return WindowDecorActionBar.this.mContextView.getTitle();
        }

        @Override // android.view.ActionMode
        public CharSequence getSubtitle() {
            return WindowDecorActionBar.this.mContextView.getSubtitle();
        }

        @Override // android.view.ActionMode
        public void setTitleOptionalHint(boolean titleOptional) {
            super.setTitleOptionalHint(titleOptional);
            WindowDecorActionBar.this.mContextView.setTitleOptional(titleOptional);
        }

        @Override // android.view.ActionMode
        public boolean isTitleOptional() {
            return WindowDecorActionBar.this.mContextView.isTitleOptional();
        }

        @Override // android.view.ActionMode
        public View getCustomView() {
            WeakReference<View> weakReference = this.mCustomView;
            if (weakReference != null) {
                return weakReference.get();
            }
            return null;
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            ActionMode.Callback callback = this.mCallback;
            if (callback != null) {
                return callback.onActionItemClicked(this, item);
            }
            return false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            if (this.mCallback == null) {
                return false;
            }
            if (!subMenu.hasVisibleItems()) {
                return true;
            }
            new MenuPopupHelper(WindowDecorActionBar.this.getThemedContext(), subMenu).show();
            return true;
        }

        public void onCloseSubMenu(SubMenuBuilder menu) {
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
            if (this.mCallback != null) {
                invalidate();
                WindowDecorActionBar.this.mContextView.showOverflowMenu();
            }
        }
    }

    public class TabImpl extends ActionBar.Tab {
        @UnsupportedAppUsage
        private ActionBar.TabListener mCallback;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private int mPosition = -1;
        private Object mTag;
        private CharSequence mText;

        public TabImpl() {
        }

        @Override // android.app.ActionBar.Tab
        public Object getTag() {
            return this.mTag;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public ActionBar.TabListener getCallback() {
            return this.mCallback;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setTabListener(ActionBar.TabListener callback) {
            this.mCallback = callback;
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public View getCustomView() {
            return this.mCustomView;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(int layoutResId) {
            return setCustomView(LayoutInflater.from(WindowDecorActionBar.this.getThemedContext()).inflate(layoutResId, (ViewGroup) null));
        }

        @Override // android.app.ActionBar.Tab
        public Drawable getIcon() {
            return this.mIcon;
        }

        @Override // android.app.ActionBar.Tab
        public int getPosition() {
            return this.mPosition;
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override // android.app.ActionBar.Tab
        public CharSequence getText() {
            return this.mText;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setIcon(int resId) {
            return setIcon(WindowDecorActionBar.this.mContext.getDrawable(resId));
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setText(int resId) {
            return setText(WindowDecorActionBar.this.mContext.getResources().getText(resId));
        }

        @Override // android.app.ActionBar.Tab
        public void select() {
            WindowDecorActionBar.this.selectTab(this);
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(int resId) {
            return setContentDescription(WindowDecorActionBar.this.mContext.getResources().getText(resId));
        }

        @Override // android.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.app.ActionBar.Tab
        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    @Override // android.app.ActionBar
    public void setCustomView(View view) {
        startActionBarTransition();
        this.mDecorToolbar.setCustomView(view);
    }

    @Override // android.app.ActionBar
    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        startActionBarTransition();
        view.setLayoutParams(layoutParams);
        this.mDecorToolbar.setCustomView(view);
    }

    @Override // android.app.ActionBar
    public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        this.mDecorToolbar.setDropdownParams(adapter, new NavItemSelectedListener(callback));
    }

    @Override // android.app.ActionBar
    public int getSelectedNavigationIndex() {
        TabImpl tabImpl;
        int navigationMode = this.mDecorToolbar.getNavigationMode();
        if (navigationMode == 1) {
            return this.mDecorToolbar.getDropdownSelectedPosition();
        }
        if (navigationMode == 2 && (tabImpl = this.mSelectedTab) != null) {
            return tabImpl.getPosition();
        }
        return -1;
    }

    @Override // android.app.ActionBar
    public int getNavigationItemCount() {
        int navigationMode = this.mDecorToolbar.getNavigationMode();
        if (navigationMode == 1) {
            return this.mDecorToolbar.getDropdownItemCount();
        }
        if (navigationMode != 2) {
            return 0;
        }
        return this.mTabs.size();
    }

    @Override // android.app.ActionBar
    public int getTabCount() {
        return this.mTabs.size();
    }

    @Override // android.app.ActionBar
    public void setNavigationMode(int mode) {
        ActionBarOverlayLayout actionBarOverlayLayout;
        startActionBarTransition();
        int oldMode = this.mDecorToolbar.getNavigationMode();
        if (oldMode == 2) {
            this.mSavedTabPosition = getSelectedNavigationIndex();
            selectTab(null);
            this.mTabScrollView.setVisibility(8);
        }
        if (!(oldMode == mode || this.mHasEmbeddedTabs || (actionBarOverlayLayout = this.mOverlayLayout) == null)) {
            actionBarOverlayLayout.requestFitSystemWindows();
        }
        this.mDecorToolbar.setNavigationMode(mode);
        boolean z = false;
        if (mode == 2) {
            ensureTabsExist();
            this.mTabScrollView.setVisibility(0);
            int i = this.mSavedTabPosition;
            if (i != -1) {
                setSelectedNavigationItem(i);
                this.mSavedTabPosition = -1;
            }
        }
        this.mDecorToolbar.setCollapsible(mode == 2 && !this.mHasEmbeddedTabs);
        ActionBarOverlayLayout actionBarOverlayLayout2 = this.mOverlayLayout;
        if (mode == 2 && !this.mHasEmbeddedTabs) {
            z = true;
        }
        actionBarOverlayLayout2.setHasNonEmbeddedTabs(z);
    }

    @Override // android.app.ActionBar
    public ActionBar.Tab getTabAt(int index) {
        return this.mTabs.get(index);
    }

    @Override // android.app.ActionBar
    public void setIcon(int resId) {
        this.mDecorToolbar.setIcon(resId);
    }

    @Override // android.app.ActionBar
    public void setIcon(Drawable icon) {
        this.mDecorToolbar.setIcon(icon);
    }

    public boolean hasIcon() {
        return this.mDecorToolbar.hasIcon();
    }

    @Override // android.app.ActionBar
    public void setLogo(int resId) {
        this.mDecorToolbar.setLogo(resId);
    }

    @Override // android.app.ActionBar
    public void setLogo(Drawable logo) {
        this.mDecorToolbar.setLogo(logo);
    }

    public boolean hasLogo() {
        return this.mDecorToolbar.hasLogo();
    }

    @Override // android.app.ActionBar
    public void setDefaultDisplayHomeAsUpEnabled(boolean enable) {
        if (!this.mDisplayHomeAsUpSet) {
            setDisplayHomeAsUpEnabled(enable);
        }
    }

    private boolean checkGalleryActivity(Activity activity) {
        String hostName = activity.toString();
        if (hostName == null) {
            return false;
        }
        if (hostName.indexOf(HWCAMERANAME) >= 0 || hostName.indexOf(GALLERY3DNAME_NEW) >= 0 || hostName.indexOf(GALLERY3DNAME_OLD) >= 0) {
            return true;
        }
        return false;
    }

    private void initContainerViewCopy() {
        if (this.mDecorView != null) {
            this.mContainerViewCopy = new ImageView(this.mContext);
            if (this.mDecorView instanceof ViewGroup) {
                this.mContainerViewCopy.setLayoutParams(new ActionBar.LayoutParams(-2, -2));
                ((ViewGroup) this.mDecorView).addView(this.mContainerViewCopy);
            }
            int[] location = new int[2];
            this.mContainerView.getLocationOnScreen(location);
            this.mContainerViewCopy.setTranslationY((float) location[1]);
        }
    }

    /* access modifiers changed from: protected */
    public void initContainerView(View decor) {
        View view = decor.findViewById(R.id.action_bar_container);
        if (view instanceof ActionBarContainer) {
            this.mContainerView = (ActionBarContainer) view;
        }
    }

    /* access modifiers changed from: protected */
    public void initContextView(View decor) {
        View view = decor.findViewById(R.id.action_context_bar);
        if (view instanceof ActionBarContextView) {
            this.mContextView = (ActionBarContextView) view;
        }
    }

    /* access modifiers changed from: protected */
    public ScrollingTabContainerView initScrollingTabContainerView() {
        return new ScrollingTabContainerView(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void setContextView(ActionBarContextView view) {
        this.mContextView = view;
    }

    /* access modifiers changed from: protected */
    public ActionBarContextView getContextView() {
        return this.mContextView;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: protected */
    public ScrollingTabContainerView getTabScrollView() {
        return this.mTabScrollView;
    }

    /* access modifiers changed from: protected */
    public DecorToolbar getDecorToolbar() {
        return this.mDecorToolbar;
    }

    /* access modifiers changed from: protected */
    public void setContainerView(ActionBarContainer view) {
        this.mContainerView = view;
    }

    /* access modifiers changed from: protected */
    public ActionBarContainer getContainerView() {
        return this.mContainerView;
    }

    /* access modifiers changed from: protected */
    public ActionBarContainer getSplitView() {
        return this.mSplitView;
    }

    /* access modifiers changed from: protected */
    public ActionBarOverlayLayout getOverlayLayout() {
        return this.mOverlayLayout;
    }

    /* access modifiers changed from: protected */
    public int getContextDisplayMode() {
        return this.mContextDisplayMode;
    }

    /* access modifiers changed from: protected */
    public void initCustomPanel(View decor) {
    }

    /* access modifiers changed from: protected */
    public void initActionBarOverlayLayout(View decor) {
        this.mOverlayLayout = (ActionBarOverlayLayout) decor.findViewById(R.id.decor_content_parent);
    }

    /* access modifiers changed from: protected */
    public void setOverlayLayout(ActionBarOverlayLayout overlayLayout) {
        this.mOverlayLayout = overlayLayout;
    }

    /* access modifiers changed from: protected */
    public void setContextDisplayMode(int mode) {
        this.mContextDisplayMode = mode;
    }

    /* access modifiers changed from: protected */
    public Activity getActivity() {
        return this.mActivity;
    }

    @Deprecated
    public void setAnimationEnable(boolean animationEnabled) {
    }

    @Deprecated
    public void setScrollTabAnimEnable(boolean shouldAnim) {
    }
}
