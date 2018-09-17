package com.android.internal.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.ActionBar.OnNavigationListener;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.Property;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
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
import com.android.internal.widget.ActionBarOverlayLayout.ActionBarVisibilityCallback;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ScrollingTabContainerView;
import huawei.cust.HwCfgFilePolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class WindowDecorActionBar extends ActionBar implements ActionBarVisibilityCallback {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final long FADE_IN_DURATION_MS = 200;
    private static final float FADE_IN_START_ALPHA = 0.1f;
    private static final long FADE_OUT_DURATION_MS = 100;
    private static final String GALLERY3DNAME = "com.android.gallery3d.app.Gallery";
    private static final String HWCAMERANAME = "com.android.hwcamera.Camera";
    private static final int INVALID_POSITION = -1;
    private static final String TAG = "WindowDecorActionBar";
    private static final int TRANSITION_DURATION = 120;
    private static final PathInterpolator TRANSITION_INTERPOLATOER = null;
    ActionMode mActionMode;
    private Activity mActivity;
    private ActionBarContainer mContainerView;
    private ImageView mContainerViewCopy;
    private boolean mContentAnimations;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    private ActionBarContextView mContextView;
    private int mCurWindowVisibility;
    private Animator mCurrentShowAnim;
    private DecorToolbar mDecorToolbar;
    private View mDecorView;
    ActionMode mDeferredDestroyActionMode;
    Callback mDeferredModeDestroyCallback;
    private Dialog mDialog;
    private boolean mDisplayHomeAsUpSet;
    private boolean mHasEmbeddedTabs;
    private boolean mHiddenByApp;
    private boolean mHiddenBySystem;
    final AnimatorListener mHideListener;
    boolean mHideOnContentScroll;
    private boolean mIsGalleryActiviy;
    private boolean mLastMenuVisibility;
    private ArrayList<OnMenuVisibilityListener> mMenuVisibilityListeners;
    private boolean mNowShowing;
    private ActionBarOverlayLayout mOverlayLayout;
    private int mSavedTabPosition;
    private TabImpl mSelectedTab;
    private boolean mShouldTransition;
    private boolean mShowHideAnimationEnabled;
    final AnimatorListener mShowListener;
    private boolean mShowingForMode;
    private ActionBarContainer mSplitView;
    private ScrollingTabContainerView mTabScrollView;
    private ArrayList<TabImpl> mTabs;
    private Context mThemedContext;
    private AnimatorSet mTransitionAnim;
    final AnimatorListener mTransitionListener;
    final AnimatorUpdateListener mUpdateListener;

    public class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
        private final Context mActionModeContext;
        private Callback mCallback;
        private WeakReference<View> mCustomView;
        private final MenuBuilder mMenu;

        public ActionModeImpl(Context context, Callback callback) {
            this.mActionModeContext = context;
            this.mCallback = callback;
            this.mMenu = new MenuBuilder(context).setDefaultShowAsAction(WindowDecorActionBar.CONTEXT_DISPLAY_SPLIT);
            this.mMenu.setCallback(this);
        }

        public MenuInflater getMenuInflater() {
            return new MenuInflater(this.mActionModeContext);
        }

        public Menu getMenu() {
            return this.mMenu;
        }

        public void finish() {
            if (WindowDecorActionBar.this.mActionMode == this) {
                if (WindowDecorActionBar.checkShowingFlags(WindowDecorActionBar.this.mHiddenByApp, WindowDecorActionBar.this.mHiddenBySystem, WindowDecorActionBar.-assertionsDisabled)) {
                    this.mCallback.onDestroyActionMode(this);
                } else {
                    WindowDecorActionBar.this.mDeferredDestroyActionMode = this;
                    WindowDecorActionBar.this.mDeferredModeDestroyCallback = this.mCallback;
                }
                this.mCallback = null;
                WindowDecorActionBar.this.animateToMode(WindowDecorActionBar.-assertionsDisabled);
                WindowDecorActionBar.this.mContextView.closeMode();
                WindowDecorActionBar.this.mDecorToolbar.getViewGroup().sendAccessibilityEvent(32);
                WindowDecorActionBar.this.mOverlayLayout.setHideOnContentScrollEnabled(WindowDecorActionBar.this.mHideOnContentScroll);
                WindowDecorActionBar.this.mActionMode = null;
            }
        }

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
                boolean onCreateActionMode = this.mCallback.onCreateActionMode(this, this.mMenu);
                return onCreateActionMode;
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public void setCustomView(View view) {
            WindowDecorActionBar.this.mContextView.setCustomView(view);
            this.mCustomView = new WeakReference(view);
        }

        public void setSubtitle(CharSequence subtitle) {
            WindowDecorActionBar.this.mContextView.setSubtitle(subtitle);
        }

        public void setTitle(CharSequence title) {
            WindowDecorActionBar.this.mContextView.setTitle(title);
        }

        public void setTitle(int resId) {
            setTitle(WindowDecorActionBar.this.mContext.getResources().getString(resId));
        }

        public void setSubtitle(int resId) {
            setSubtitle(WindowDecorActionBar.this.mContext.getResources().getString(resId));
        }

        public CharSequence getTitle() {
            return WindowDecorActionBar.this.mContextView.getTitle();
        }

        public CharSequence getSubtitle() {
            return WindowDecorActionBar.this.mContextView.getSubtitle();
        }

        public void setTitleOptionalHint(boolean titleOptional) {
            super.setTitleOptionalHint(titleOptional);
            WindowDecorActionBar.this.mContextView.setTitleOptional(titleOptional);
        }

        public boolean isTitleOptional() {
            return WindowDecorActionBar.this.mContextView.isTitleOptional();
        }

        public View getCustomView() {
            return this.mCustomView != null ? (View) this.mCustomView.get() : null;
        }

        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (this.mCallback != null) {
                return this.mCallback.onActionItemClicked(this, item);
            }
            return WindowDecorActionBar.-assertionsDisabled;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            if (this.mCallback == null) {
                return WindowDecorActionBar.-assertionsDisabled;
            }
            if (!subMenu.hasVisibleItems()) {
                return true;
            }
            new MenuPopupHelper(WindowDecorActionBar.this.getThemedContext(), subMenu).show();
            return true;
        }

        public void onCloseSubMenu(SubMenuBuilder menu) {
        }

        public void onMenuModeChange(MenuBuilder menu) {
            if (this.mCallback != null) {
                invalidate();
                WindowDecorActionBar.this.mContextView.showOverflowMenu();
            }
        }
    }

    public class TabImpl extends Tab {
        private TabListener mCallback;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private int mPosition;
        private Object mTag;
        private CharSequence mText;

        public TabImpl() {
            this.mPosition = WindowDecorActionBar.INVALID_POSITION;
        }

        public Object getTag() {
            return this.mTag;
        }

        public Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public TabListener getCallback() {
            return this.mCallback;
        }

        public Tab setTabListener(TabListener callback) {
            this.mCallback = callback;
            return this;
        }

        public View getCustomView() {
            return this.mCustomView;
        }

        public Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setCustomView(int layoutResId) {
            return setCustomView(LayoutInflater.from(WindowDecorActionBar.this.getThemedContext()).inflate(layoutResId, null));
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setIcon(int resId) {
            return setIcon(WindowDecorActionBar.this.mContext.getDrawable(resId));
        }

        public Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setText(int resId) {
            return setText(WindowDecorActionBar.this.mContext.getResources().getText(resId));
        }

        public void select() {
            WindowDecorActionBar.this.selectTab(this);
        }

        public Tab setContentDescription(int resId) {
            return setContentDescription(WindowDecorActionBar.this.mContext.getResources().getText(resId));
        }

        public Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                WindowDecorActionBar.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.WindowDecorActionBar.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.app.WindowDecorActionBar.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.WindowDecorActionBar.<clinit>():void");
    }

    public void setDisplayOptions(int r1, int r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.app.WindowDecorActionBar.setDisplayOptions(int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.app.WindowDecorActionBar.setDisplayOptions(int, int):void");
    }

    public WindowDecorActionBar(Activity activity) {
        this.mTabs = new ArrayList();
        this.mSavedTabPosition = INVALID_POSITION;
        this.mMenuVisibilityListeners = new ArrayList();
        this.mCurWindowVisibility = CONTEXT_DISPLAY_NORMAL;
        this.mContentAnimations = true;
        this.mNowShowing = true;
        this.mHideListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (WindowDecorActionBar.this.mContentAnimations && WindowDecorActionBar.this.mContentView != null) {
                    WindowDecorActionBar.this.mContentView.setTranslationY(0.0f);
                    WindowDecorActionBar.this.mContainerView.setTranslationY(0.0f);
                }
                if (WindowDecorActionBar.this.mSplitView != null && WindowDecorActionBar.this.mContextDisplayMode == WindowDecorActionBar.CONTEXT_DISPLAY_SPLIT) {
                    WindowDecorActionBar.this.mSplitView.setVisibility(8);
                }
                WindowDecorActionBar.this.mContainerView.setVisibility(8);
                WindowDecorActionBar.this.mContainerView.setTransitioning(WindowDecorActionBar.-assertionsDisabled);
                WindowDecorActionBar.this.mCurrentShowAnim = null;
                WindowDecorActionBar.this.completeDeferredDestroyActionMode();
                if (WindowDecorActionBar.this.mOverlayLayout != null) {
                    WindowDecorActionBar.this.mOverlayLayout.requestApplyInsets();
                }
            }
        };
        this.mShowListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                WindowDecorActionBar.this.mCurrentShowAnim = null;
                WindowDecorActionBar.this.mContainerView.requestLayout();
            }
        };
        this.mUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ((View) WindowDecorActionBar.this.mContainerView.getParent()).invalidate();
            }
        };
        this.mShouldTransition = -assertionsDisabled;
        this.mTransitionListener = new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (WindowDecorActionBar.this.mContainerView != null) {
                    WindowDecorActionBar.this.mContainerView.setAlpha(0.0f);
                }
            }
        };
        this.mIsGalleryActiviy = true;
        this.mActivity = activity;
        Window window = activity.getWindow();
        this.mIsGalleryActiviy = checkGalleryActivity(activity);
        View decor = window.getDecorView();
        boolean overlayMode = this.mActivity.getWindow().hasFeature(9);
        init(decor);
        if (!overlayMode) {
            this.mContentView = decor.findViewById(R.id.content);
        }
    }

    public WindowDecorActionBar(Dialog dialog) {
        this.mTabs = new ArrayList();
        this.mSavedTabPosition = INVALID_POSITION;
        this.mMenuVisibilityListeners = new ArrayList();
        this.mCurWindowVisibility = CONTEXT_DISPLAY_NORMAL;
        this.mContentAnimations = true;
        this.mNowShowing = true;
        this.mHideListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (WindowDecorActionBar.this.mContentAnimations && WindowDecorActionBar.this.mContentView != null) {
                    WindowDecorActionBar.this.mContentView.setTranslationY(0.0f);
                    WindowDecorActionBar.this.mContainerView.setTranslationY(0.0f);
                }
                if (WindowDecorActionBar.this.mSplitView != null && WindowDecorActionBar.this.mContextDisplayMode == WindowDecorActionBar.CONTEXT_DISPLAY_SPLIT) {
                    WindowDecorActionBar.this.mSplitView.setVisibility(8);
                }
                WindowDecorActionBar.this.mContainerView.setVisibility(8);
                WindowDecorActionBar.this.mContainerView.setTransitioning(WindowDecorActionBar.-assertionsDisabled);
                WindowDecorActionBar.this.mCurrentShowAnim = null;
                WindowDecorActionBar.this.completeDeferredDestroyActionMode();
                if (WindowDecorActionBar.this.mOverlayLayout != null) {
                    WindowDecorActionBar.this.mOverlayLayout.requestApplyInsets();
                }
            }
        };
        this.mShowListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                WindowDecorActionBar.this.mCurrentShowAnim = null;
                WindowDecorActionBar.this.mContainerView.requestLayout();
            }
        };
        this.mUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ((View) WindowDecorActionBar.this.mContainerView.getParent()).invalidate();
            }
        };
        this.mShouldTransition = -assertionsDisabled;
        this.mTransitionListener = new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (WindowDecorActionBar.this.mContainerView != null) {
                    WindowDecorActionBar.this.mContainerView.setAlpha(0.0f);
                }
            }
        };
        this.mIsGalleryActiviy = true;
        this.mDialog = dialog;
        init(dialog.getWindow().getDecorView());
    }

    public WindowDecorActionBar(View layout) {
        this.mTabs = new ArrayList();
        this.mSavedTabPosition = INVALID_POSITION;
        this.mMenuVisibilityListeners = new ArrayList();
        this.mCurWindowVisibility = CONTEXT_DISPLAY_NORMAL;
        this.mContentAnimations = true;
        this.mNowShowing = true;
        this.mHideListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (WindowDecorActionBar.this.mContentAnimations && WindowDecorActionBar.this.mContentView != null) {
                    WindowDecorActionBar.this.mContentView.setTranslationY(0.0f);
                    WindowDecorActionBar.this.mContainerView.setTranslationY(0.0f);
                }
                if (WindowDecorActionBar.this.mSplitView != null && WindowDecorActionBar.this.mContextDisplayMode == WindowDecorActionBar.CONTEXT_DISPLAY_SPLIT) {
                    WindowDecorActionBar.this.mSplitView.setVisibility(8);
                }
                WindowDecorActionBar.this.mContainerView.setVisibility(8);
                WindowDecorActionBar.this.mContainerView.setTransitioning(WindowDecorActionBar.-assertionsDisabled);
                WindowDecorActionBar.this.mCurrentShowAnim = null;
                WindowDecorActionBar.this.completeDeferredDestroyActionMode();
                if (WindowDecorActionBar.this.mOverlayLayout != null) {
                    WindowDecorActionBar.this.mOverlayLayout.requestApplyInsets();
                }
            }
        };
        this.mShowListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                WindowDecorActionBar.this.mCurrentShowAnim = null;
                WindowDecorActionBar.this.mContainerView.requestLayout();
            }
        };
        this.mUpdateListener = new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ((View) WindowDecorActionBar.this.mContainerView.getParent()).invalidate();
            }
        };
        this.mShouldTransition = -assertionsDisabled;
        this.mTransitionListener = new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (WindowDecorActionBar.this.mContainerView != null) {
                    WindowDecorActionBar.this.mContainerView.setAlpha(0.0f);
                }
            }
        };
        this.mIsGalleryActiviy = true;
        if (-assertionsDisabled || layout.isInEditMode()) {
            init(layout);
            return;
        }
        throw new AssertionError();
    }

    private void init(View decor) {
        this.mDecorToolbar = getDecorToolbar(decor.findViewById(R.id.action_bar));
        this.mContext = this.mDecorToolbar.getContext();
        initCustomPanel(decor);
        initActionBarOverlayLayout(decor);
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setActionBarVisibilityCallback(this);
        }
        initContextView(decor);
        initContainerView(decor);
        this.mSplitView = (ActionBarContainer) decor.findViewById(R.id.split_action_bar);
        if (this.mDecorToolbar == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with a compatible window decor layout");
        }
        int i;
        boolean homeAsUp;
        if (this.mDecorToolbar.isSplit()) {
            i = CONTEXT_DISPLAY_SPLIT;
        } else {
            i = CONTEXT_DISPLAY_NORMAL;
        }
        this.mContextDisplayMode = i;
        if ((this.mDecorToolbar.getDisplayOptions() & 4) != 0) {
            homeAsUp = true;
        } else {
            homeAsUp = -assertionsDisabled;
        }
        if (homeAsUp) {
            this.mDisplayHomeAsUpSet = true;
        }
        ActionBarPolicy abp = ActionBarPolicy.get(this.mContext);
        if (abp.enableHomeButtonByDefault()) {
            homeAsUp = true;
        }
        setHomeButtonEnabled(homeAsUp);
        setHasEmbeddedTabs(abp.hasEmbeddedTabs());
        TypedArray a = this.mContext.obtainStyledAttributes(null, R.styleable.ActionBar, R.attr.actionBarStyle, CONTEXT_DISPLAY_NORMAL);
        if (a.getBoolean(21, -assertionsDisabled)) {
            setHideOnContentScrollEnabled(true);
        }
        int elevation = a.getDimensionPixelSize(20, CONTEXT_DISPLAY_NORMAL);
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
        ObjectAnimator fadeOutAnim = ObjectAnimator.ofFloat(this.mContainerViewCopy, "alpha", new float[]{LayoutParams.BRIGHTNESS_OVERRIDE_FULL, 0.0f});
        fadeOutAnim.setDuration(120);
        fadeOutAnim.setInterpolator(TRANSITION_INTERPOLATOER);
        fadeOutAnim.addListener(this.mTransitionListener);
        ObjectAnimator fadeInAnim = ObjectAnimator.ofFloat(this.mContainerView, "alpha", new float[]{FADE_IN_START_ALPHA, LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
        fadeInAnim.setDuration(120);
        fadeInAnim.setInterpolator(TRANSITION_INTERPOLATOER);
        this.mTransitionAnim.playSequentially(new Animator[]{fadeOutAnim, fadeInAnim});
    }

    private void startActionBarTransition() {
        if (!this.mShouldTransition || this.mTransitionAnim == null || this.mTransitionAnim.isRunning() || this.mContainerView == null || !this.mContainerView.isLaidOut()) {
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
            this.mContainerView.setDrawingCacheEnabled(-assertionsDisabled);
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
        throw new IllegalStateException("Can't make a decor toolbar out of " + (view == null ? "NULL Object" : view.getClass().getSimpleName()));
    }

    public void setElevation(float elevation) {
        this.mContainerView.setElevation(elevation);
        if (this.mSplitView != null) {
            this.mSplitView.setElevation(elevation);
        }
    }

    public float getElevation() {
        return this.mContainerView.getElevation();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setHasEmbeddedTabs(ActionBarPolicy.get(this.mContext).hasEmbeddedTabs());
    }

    private void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        boolean z;
        this.mHasEmbeddedTabs = hasEmbeddedTabs;
        if (this.mHasEmbeddedTabs) {
            this.mContainerView.setTabContainer(null);
            this.mDecorToolbar.setEmbeddedTabView(this.mTabScrollView);
        } else {
            this.mDecorToolbar.setEmbeddedTabView(null);
            this.mContainerView.setTabContainer(this.mTabScrollView);
        }
        boolean isInTabMode = getNavigationMode() == 2 ? true : -assertionsDisabled;
        if (this.mTabScrollView != null) {
            if (isInTabMode) {
                this.mTabScrollView.setVisibility(CONTEXT_DISPLAY_NORMAL);
                if (this.mOverlayLayout != null) {
                    this.mOverlayLayout.requestApplyInsets();
                }
            } else {
                this.mTabScrollView.setVisibility(8);
            }
        }
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (this.mHasEmbeddedTabs) {
            z = -assertionsDisabled;
        } else {
            z = isInTabMode;
        }
        decorToolbar.setCollapsible(z);
        ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
        if (this.mHasEmbeddedTabs) {
            isInTabMode = -assertionsDisabled;
        }
        actionBarOverlayLayout.setHasNonEmbeddedTabs(isInTabMode);
    }

    private void ensureTabsExist() {
        if (this.mTabScrollView == null) {
            ScrollingTabContainerView tabScroller = initScrollingTabContainerView();
            if (this.mHasEmbeddedTabs) {
                tabScroller.setVisibility(CONTEXT_DISPLAY_NORMAL);
                this.mDecorToolbar.setEmbeddedTabView(tabScroller);
            } else {
                if (getNavigationMode() == 2) {
                    tabScroller.setVisibility(CONTEXT_DISPLAY_NORMAL);
                    if (this.mOverlayLayout != null) {
                        this.mOverlayLayout.requestApplyInsets();
                    }
                } else {
                    tabScroller.setVisibility(8);
                }
                this.mContainerView.setTabContainer(tabScroller);
            }
            this.mTabScrollView = tabScroller;
        }
    }

    void completeDeferredDestroyActionMode() {
        if (this.mDeferredModeDestroyCallback != null) {
            this.mDeferredModeDestroyCallback.onDestroyActionMode(this.mDeferredDestroyActionMode);
            this.mDeferredDestroyActionMode = null;
            this.mDeferredModeDestroyCallback = null;
        }
    }

    public void onWindowVisibilityChanged(int visibility) {
        this.mCurWindowVisibility = visibility;
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
        this.mShowHideAnimationEnabled = enabled;
        if (!enabled && this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.add(listener);
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.remove(listener);
    }

    public void dispatchMenuVisibilityChanged(boolean isVisible) {
        if (isVisible != this.mLastMenuVisibility) {
            this.mLastMenuVisibility = isVisible;
            int count = this.mMenuVisibilityListeners.size();
            for (int i = CONTEXT_DISPLAY_NORMAL; i < count; i += CONTEXT_DISPLAY_SPLIT) {
                ((OnMenuVisibilityListener) this.mMenuVisibilityListeners.get(i)).onMenuVisibilityChanged(isVisible);
            }
        }
    }

    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(getThemedContext()).inflate(resId, this.mDecorToolbar.getViewGroup(), (boolean) -assertionsDisabled));
    }

    public void setDisplayUseLogoEnabled(boolean useLogo) {
        setDisplayOptions(useLogo ? CONTEXT_DISPLAY_SPLIT : CONTEXT_DISPLAY_NORMAL, CONTEXT_DISPLAY_SPLIT);
    }

    public void setDisplayShowHomeEnabled(boolean showHome) {
        setDisplayOptions(showHome ? 2 : CONTEXT_DISPLAY_NORMAL, 2);
    }

    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        setDisplayOptions(showHomeAsUp ? 4 : CONTEXT_DISPLAY_NORMAL, 4);
    }

    public void setDisplayShowTitleEnabled(boolean showTitle) {
        setDisplayOptions(showTitle ? 8 : CONTEXT_DISPLAY_NORMAL, 8);
    }

    public void setDisplayShowCustomEnabled(boolean showCustom) {
        setDisplayOptions(showCustom ? 16 : CONTEXT_DISPLAY_NORMAL, 16);
    }

    public void setHomeButtonEnabled(boolean enable) {
        this.mDecorToolbar.setHomeButtonEnabled(enable);
    }

    public void setTitle(int resId) {
        setTitle(this.mContext.getString(resId));
    }

    public void setSubtitle(int resId) {
        setSubtitle(this.mContext.getString(resId));
    }

    public void setSelectedNavigationItem(int position) {
        switch (this.mDecorToolbar.getNavigationMode()) {
            case CONTEXT_DISPLAY_SPLIT /*1*/:
                this.mDecorToolbar.setDropdownSelectedPosition(position);
            case HwCfgFilePolicy.PC /*2*/:
                selectTab((Tab) this.mTabs.get(position));
            default:
                throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    public void removeAllTabs() {
        cleanupTabs();
    }

    private void cleanupTabs() {
        if (this.mSelectedTab != null) {
            selectTab(null);
        }
        this.mTabs.clear();
        if (this.mTabScrollView != null) {
            this.mTabScrollView.removeAllTabs();
        }
        this.mSavedTabPosition = INVALID_POSITION;
    }

    public void setTitle(CharSequence title) {
        this.mDecorToolbar.setTitle(title);
    }

    public void setWindowTitle(CharSequence title) {
        this.mDecorToolbar.setWindowTitle(title);
    }

    public void setSubtitle(CharSequence subtitle) {
        this.mDecorToolbar.setSubtitle(subtitle);
    }

    public void setDisplayOptions(int options) {
        startActionBarTransition();
        if ((options & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mDecorToolbar.setDisplayOptions(options);
    }

    public void setBackgroundDrawable(Drawable d) {
        this.mContainerView.setPrimaryBackground(d);
    }

    public void setStackedBackgroundDrawable(Drawable d) {
        this.mContainerView.setStackedBackground(d);
    }

    public void setSplitBackgroundDrawable(Drawable d) {
        if (this.mSplitView != null) {
            this.mSplitView.setSplitBackground(d);
        }
    }

    public View getCustomView() {
        return this.mDecorToolbar.getCustomView();
    }

    public CharSequence getTitle() {
        return this.mDecorToolbar.getTitle();
    }

    public CharSequence getSubtitle() {
        return this.mDecorToolbar.getSubtitle();
    }

    public int getNavigationMode() {
        return this.mDecorToolbar.getNavigationMode();
    }

    public int getDisplayOptions() {
        return this.mDecorToolbar.getDisplayOptions();
    }

    public ActionMode startActionMode(Callback callback) {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mOverlayLayout.setHideOnContentScrollEnabled(-assertionsDisabled);
        this.mContextView.killMode();
        ActionModeImpl mode = new ActionModeImpl(this.mContextView.getContext(), callback);
        if (!mode.dispatchOnCreate()) {
            return null;
        }
        this.mActionMode = mode;
        mode.invalidate();
        this.mContextView.initForMode(mode);
        animateToMode(true);
        if (!(this.mSplitView == null || this.mContextDisplayMode != CONTEXT_DISPLAY_SPLIT || this.mSplitView.getVisibility() == 0)) {
            this.mSplitView.setVisibility(CONTEXT_DISPLAY_NORMAL);
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.requestApplyInsets();
            }
        }
        this.mContextView.sendAccessibilityEvent(32);
        return mode;
    }

    private void configureTab(Tab tab, int position) {
        TabImpl tabi = (TabImpl) tab;
        if (tabi.getCallback() == null) {
            throw new IllegalStateException("Action Bar Tab must have a Callback");
        }
        tabi.setPosition(position);
        this.mTabs.add(position, tabi);
        int count = this.mTabs.size();
        for (int i = position + CONTEXT_DISPLAY_SPLIT; i < count; i += CONTEXT_DISPLAY_SPLIT) {
            ((TabImpl) this.mTabs.get(i)).setPosition(i);
        }
    }

    public void addTab(Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, setSelected);
        configureTab(tab, this.mTabs.size());
        if (setSelected) {
            selectTab(tab);
        }
    }

    public void addTab(Tab tab, int position, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            selectTab(tab);
        }
    }

    public Tab newTab() {
        return new TabImpl();
    }

    public void removeTab(Tab tab) {
        removeTabAt(tab.getPosition());
    }

    public void removeTabAt(int position) {
        if (this.mTabScrollView != null) {
            int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : this.mSavedTabPosition;
            this.mTabScrollView.removeTabAt(position);
            TabImpl removedTab = (TabImpl) this.mTabs.remove(position);
            if (removedTab != null) {
                removedTab.setPosition(INVALID_POSITION);
            }
            int newTabCount = this.mTabs.size();
            for (int i = position; i < newTabCount; i += CONTEXT_DISPLAY_SPLIT) {
                ((TabImpl) this.mTabs.get(i)).setPosition(i);
            }
            if (selectedTabPosition == position) {
                Tab tab;
                if (this.mTabs.isEmpty()) {
                    tab = null;
                } else {
                    tab = (Tab) this.mTabs.get(Math.max(CONTEXT_DISPLAY_NORMAL, position + INVALID_POSITION));
                }
                selectTab(tab);
            }
        }
    }

    public void selectTab(Tab tab) {
        int i = INVALID_POSITION;
        if (getNavigationMode() != 2) {
            if (tab != null) {
                i = tab.getPosition();
            }
            this.mSavedTabPosition = i;
            return;
        }
        FragmentTransaction fragmentTransaction;
        if (this.mDecorToolbar.getViewGroup().isInEditMode()) {
            fragmentTransaction = null;
        } else {
            fragmentTransaction = this.mActivity.getFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        if (this.mSelectedTab != tab) {
            ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
            if (tab != null) {
                i = tab.getPosition();
            }
            scrollingTabContainerView.setTabSelected(i);
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabUnselected(this.mSelectedTab, fragmentTransaction);
            }
            this.mSelectedTab = (TabImpl) tab;
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabSelected(this.mSelectedTab, fragmentTransaction);
            }
        } else if (this.mSelectedTab != null) {
            this.mSelectedTab.getCallback().onTabReselected(this.mSelectedTab, fragmentTransaction);
            this.mTabScrollView.animateToTab(tab.getPosition());
        }
        if (!(fragmentTransaction == null || fragmentTransaction.isEmpty())) {
            fragmentTransaction.commit();
        }
    }

    public Tab getSelectedTab() {
        return this.mSelectedTab;
    }

    public int getHeight() {
        return this.mContainerView.getHeight();
    }

    public void enableContentAnimations(boolean enabled) {
        this.mContentAnimations = enabled;
    }

    public void show() {
        if (this.mHiddenByApp) {
            this.mHiddenByApp = -assertionsDisabled;
            updateVisibility(-assertionsDisabled);
        }
    }

    private void showForActionMode() {
        if (!this.mShowingForMode) {
            this.mShowingForMode = true;
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.setShowingForActionMode(true);
            }
            updateVisibility(-assertionsDisabled);
        }
    }

    public void showForSystem() {
        if (this.mHiddenBySystem) {
            this.mHiddenBySystem = -assertionsDisabled;
            updateVisibility(true);
        }
    }

    public void hide() {
        if (!this.mHiddenByApp) {
            this.mHiddenByApp = true;
            updateVisibility(-assertionsDisabled);
        }
    }

    private void hideForActionMode() {
        if (this.mShowingForMode) {
            this.mShowingForMode = -assertionsDisabled;
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.setShowingForActionMode(-assertionsDisabled);
            }
            updateVisibility(-assertionsDisabled);
        }
    }

    public void hideForSystem() {
        if (!this.mHiddenBySystem) {
            this.mHiddenBySystem = true;
            updateVisibility(true);
        }
    }

    public void setHideOnContentScrollEnabled(boolean hideOnContentScroll) {
        if (!hideOnContentScroll || this.mOverlayLayout.isInOverlayMode()) {
            this.mHideOnContentScroll = hideOnContentScroll;
            this.mOverlayLayout.setHideOnContentScrollEnabled(hideOnContentScroll);
            return;
        }
        throw new IllegalStateException("Action bar must be in overlay mode (Window.FEATURE_OVERLAY_ACTION_BAR) to enable hide on content scroll");
    }

    public boolean isHideOnContentScrollEnabled() {
        return this.mOverlayLayout.isHideOnContentScrollEnabled();
    }

    public int getHideOffset() {
        return this.mOverlayLayout.getActionBarHideOffset();
    }

    public void setHideOffset(int offset) {
        if (offset == 0 || this.mOverlayLayout.isInOverlayMode()) {
            this.mOverlayLayout.setActionBarHideOffset(offset);
            return;
        }
        throw new IllegalStateException("Action bar must be in overlay mode (Window.FEATURE_OVERLAY_ACTION_BAR) to set a non-zero hide offset");
    }

    private static boolean checkShowingFlags(boolean hiddenByApp, boolean hiddenBySystem, boolean showingForMode) {
        if (showingForMode) {
            return true;
        }
        if (hiddenByApp || hiddenBySystem) {
            return -assertionsDisabled;
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
            this.mNowShowing = -assertionsDisabled;
            doHide(fromSystem);
        }
    }

    public void doShow(boolean fromSystem) {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
        this.mContainerView.setVisibility(CONTEXT_DISPLAY_NORMAL);
        if (this.mCurWindowVisibility == 0 && (this.mShowHideAnimationEnabled || fromSystem)) {
            this.mContainerView.setTranslationY(0.0f);
            float startingY = (float) (-this.mContainerView.getHeight());
            if (fromSystem) {
                int[] topLeft = new int[]{CONTEXT_DISPLAY_NORMAL, CONTEXT_DISPLAY_NORMAL};
                this.mContainerView.getLocationInWindow(topLeft);
                startingY -= (float) topLeft[CONTEXT_DISPLAY_SPLIT];
            }
            if (this.mIsGalleryActiviy) {
                startingY = 0.0f;
            }
            this.mContainerView.setTranslationY(startingY);
            AnimatorSet anim = new AnimatorSet();
            ActionBarContainer actionBarContainer = this.mContainerView;
            Property property = View.TRANSLATION_Y;
            float[] fArr = new float[CONTEXT_DISPLAY_SPLIT];
            fArr[CONTEXT_DISPLAY_NORMAL] = 0.0f;
            ObjectAnimator a = ObjectAnimator.ofFloat(actionBarContainer, property, fArr);
            a.addUpdateListener(this.mUpdateListener);
            Builder b = anim.play(a);
            if (this.mContentAnimations && this.mContentView != null) {
                b.with(ObjectAnimator.ofFloat(this.mContentView, View.TRANSLATION_Y, new float[]{startingY, 0.0f}));
            }
            if (this.mSplitView != null && this.mContextDisplayMode == CONTEXT_DISPLAY_SPLIT) {
                this.mSplitView.setTranslationY((float) this.mSplitView.getHeight());
                this.mSplitView.setVisibility(CONTEXT_DISPLAY_NORMAL);
                actionBarContainer = this.mSplitView;
                property = View.TRANSLATION_Y;
                fArr = new float[CONTEXT_DISPLAY_SPLIT];
                fArr[CONTEXT_DISPLAY_NORMAL] = 0.0f;
                b.with(ObjectAnimator.ofFloat(actionBarContainer, property, fArr));
            }
            anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.decelerate_cubic));
            anim.setDuration(250);
            anim.addListener(this.mShowListener);
            this.mCurrentShowAnim = anim;
            anim.start();
        } else {
            this.mContainerView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            this.mContainerView.setTranslationY(0.0f);
            if (this.mContentAnimations && this.mContentView != null) {
                this.mContentView.setTranslationY(0.0f);
            }
            if (this.mSplitView != null && this.mContextDisplayMode == CONTEXT_DISPLAY_SPLIT) {
                this.mSplitView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                this.mSplitView.setTranslationY(0.0f);
                this.mSplitView.setVisibility(CONTEXT_DISPLAY_NORMAL);
            }
            this.mShowListener.onAnimationEnd(null);
        }
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.requestApplyInsets();
        }
    }

    public void doHide(boolean fromSystem) {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
        if (this.mCurWindowVisibility == 0 && (this.mShowHideAnimationEnabled || fromSystem)) {
            this.mContainerView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            this.mContainerView.setTransitioning(true);
            AnimatorSet anim = new AnimatorSet();
            float endingY = (float) (-this.mContainerView.getHeight());
            if (fromSystem) {
                int[] topLeft = new int[]{CONTEXT_DISPLAY_NORMAL, CONTEXT_DISPLAY_NORMAL};
                this.mContainerView.getLocationInWindow(topLeft);
                endingY -= (float) topLeft[CONTEXT_DISPLAY_SPLIT];
            }
            ActionBarContainer actionBarContainer = this.mContainerView;
            Property property = View.TRANSLATION_Y;
            float[] fArr = new float[CONTEXT_DISPLAY_SPLIT];
            fArr[CONTEXT_DISPLAY_NORMAL] = endingY;
            ObjectAnimator a = ObjectAnimator.ofFloat(actionBarContainer, property, fArr);
            a.addUpdateListener(this.mUpdateListener);
            Builder b = anim.play(a);
            if (this.mContentAnimations && this.mContentView != null) {
                b.with(ObjectAnimator.ofFloat(this.mContentView, View.TRANSLATION_Y, new float[]{0.0f, endingY}));
            }
            if (this.mSplitView != null && this.mSplitView.getVisibility() == 0) {
                this.mSplitView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                actionBarContainer = this.mSplitView;
                property = View.TRANSLATION_Y;
                fArr = new float[CONTEXT_DISPLAY_SPLIT];
                fArr[CONTEXT_DISPLAY_NORMAL] = (float) this.mSplitView.getHeight();
                b.with(ObjectAnimator.ofFloat(actionBarContainer, property, fArr));
            }
            anim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, R.interpolator.accelerate_cubic));
            anim.setDuration(250);
            anim.addListener(this.mHideListener);
            this.mCurrentShowAnim = anim;
            anim.start();
            return;
        }
        this.mHideListener.onAnimationEnd(null);
    }

    public boolean isShowing() {
        int height = getHeight();
        if (!this.mNowShowing || (height != 0 && getHideOffset() >= height)) {
            return -assertionsDisabled;
        }
        return true;
    }

    protected void animateToMode(boolean toActionMode) {
        if (toActionMode) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        if (shouldAnimateContextView()) {
            Animator fadeOut;
            Animator fadeIn;
            if (toActionMode) {
                fadeOut = this.mDecorToolbar.setupAnimatorToVisibility(8, FADE_OUT_DURATION_MS);
                fadeIn = this.mContextView.setupAnimatorToVisibility(CONTEXT_DISPLAY_NORMAL, FADE_IN_DURATION_MS);
            } else {
                fadeIn = this.mDecorToolbar.setupAnimatorToVisibility(CONTEXT_DISPLAY_NORMAL, FADE_IN_DURATION_MS);
                fadeOut = this.mContextView.setupAnimatorToVisibility(8, FADE_OUT_DURATION_MS);
            }
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(new Animator[]{fadeOut, fadeIn});
            set.start();
        } else if (toActionMode) {
            this.mDecorToolbar.setVisibility(8);
            this.mContextView.setVisibility(CONTEXT_DISPLAY_NORMAL);
        } else {
            this.mDecorToolbar.setVisibility(CONTEXT_DISPLAY_NORMAL);
            this.mContextView.setVisibility(8);
        }
    }

    private boolean shouldAnimateContextView() {
        return this.mContainerView.isLaidOut();
    }

    public Context getThemedContext() {
        if (this.mThemedContext == null) {
            TypedValue outValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(R.attr.actionBarWidgetTheme, outValue, true);
            int targetThemeRes = outValue.resourceId;
            if (targetThemeRes == 0 || this.mContext.getThemeResId() == targetThemeRes) {
                this.mThemedContext = this.mContext;
            } else {
                this.mThemedContext = new ContextThemeWrapper(this.mContext, targetThemeRes);
            }
        }
        return this.mThemedContext;
    }

    public boolean isTitleTruncated() {
        return this.mDecorToolbar != null ? this.mDecorToolbar.isTitleTruncated() : -assertionsDisabled;
    }

    public void setHomeAsUpIndicator(Drawable indicator) {
        this.mDecorToolbar.setNavigationIcon(indicator);
    }

    public void setHomeAsUpIndicator(int resId) {
        this.mDecorToolbar.setNavigationIcon(resId);
    }

    public void setHomeActionContentDescription(CharSequence description) {
        this.mDecorToolbar.setNavigationContentDescription(description);
    }

    public void setHomeActionContentDescription(int resId) {
        this.mDecorToolbar.setNavigationContentDescription(resId);
    }

    public void onContentScrollStarted() {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.cancel();
            this.mCurrentShowAnim = null;
        }
    }

    public void onContentScrollStopped() {
    }

    public boolean collapseActionView() {
        if (this.mDecorToolbar == null || !this.mDecorToolbar.hasExpandedActionView()) {
            return -assertionsDisabled;
        }
        this.mDecorToolbar.collapseActionView();
        return true;
    }

    public boolean requestFocus() {
        return requestFocus(this.mDecorToolbar.getViewGroup());
    }

    public void setCustomView(View view) {
        startActionBarTransition();
        this.mDecorToolbar.setCustomView(view);
    }

    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        startActionBarTransition();
        view.setLayoutParams(layoutParams);
        this.mDecorToolbar.setCustomView(view);
    }

    public void setListNavigationCallbacks(SpinnerAdapter adapter, OnNavigationListener callback) {
        this.mDecorToolbar.setDropdownParams(adapter, new NavItemSelectedListener(callback));
    }

    public int getSelectedNavigationIndex() {
        int i = INVALID_POSITION;
        switch (this.mDecorToolbar.getNavigationMode()) {
            case CONTEXT_DISPLAY_SPLIT /*1*/:
                return this.mDecorToolbar.getDropdownSelectedPosition();
            case HwCfgFilePolicy.PC /*2*/:
                if (this.mSelectedTab != null) {
                    i = this.mSelectedTab.getPosition();
                }
                return i;
            default:
                return INVALID_POSITION;
        }
    }

    public int getNavigationItemCount() {
        switch (this.mDecorToolbar.getNavigationMode()) {
            case CONTEXT_DISPLAY_SPLIT /*1*/:
                return this.mDecorToolbar.getDropdownItemCount();
            case HwCfgFilePolicy.PC /*2*/:
                return this.mTabs.size();
            default:
                return CONTEXT_DISPLAY_NORMAL;
        }
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public void setNavigationMode(int mode) {
        boolean z;
        boolean z2 = -assertionsDisabled;
        startActionBarTransition();
        int oldMode = this.mDecorToolbar.getNavigationMode();
        switch (oldMode) {
            case HwCfgFilePolicy.PC /*2*/:
                this.mSavedTabPosition = getSelectedNavigationIndex();
                selectTab(null);
                this.mTabScrollView.setVisibility(8);
                break;
        }
        if (!(oldMode == mode || this.mHasEmbeddedTabs || this.mOverlayLayout == null)) {
            this.mOverlayLayout.requestFitSystemWindows();
        }
        this.mDecorToolbar.setNavigationMode(mode);
        switch (mode) {
            case HwCfgFilePolicy.PC /*2*/:
                ensureTabsExist();
                this.mTabScrollView.setVisibility(CONTEXT_DISPLAY_NORMAL);
                if (this.mSavedTabPosition != INVALID_POSITION) {
                    setSelectedNavigationItem(this.mSavedTabPosition);
                    this.mSavedTabPosition = INVALID_POSITION;
                    break;
                }
                break;
        }
        DecorToolbar decorToolbar = this.mDecorToolbar;
        if (mode != 2 || this.mHasEmbeddedTabs) {
            z = -assertionsDisabled;
        } else {
            z = true;
        }
        decorToolbar.setCollapsible(z);
        ActionBarOverlayLayout actionBarOverlayLayout = this.mOverlayLayout;
        if (mode == 2 && !this.mHasEmbeddedTabs) {
            z2 = true;
        }
        actionBarOverlayLayout.setHasNonEmbeddedTabs(z2);
    }

    public Tab getTabAt(int index) {
        return (Tab) this.mTabs.get(index);
    }

    public void setIcon(int resId) {
        this.mDecorToolbar.setIcon(resId);
    }

    public void setIcon(Drawable icon) {
        this.mDecorToolbar.setIcon(icon);
    }

    public boolean hasIcon() {
        return this.mDecorToolbar.hasIcon();
    }

    public void setLogo(int resId) {
        this.mDecorToolbar.setLogo(resId);
    }

    public void setLogo(Drawable logo) {
        this.mDecorToolbar.setLogo(logo);
    }

    public boolean hasLogo() {
        return this.mDecorToolbar.hasLogo();
    }

    public void setDefaultDisplayHomeAsUpEnabled(boolean enable) {
        if (!this.mDisplayHomeAsUpSet) {
            setDisplayHomeAsUpEnabled(enable);
        }
    }

    private boolean checkGalleryActivity(Activity activity) {
        String hostName = activity.toString();
        if (hostName == null || (hostName.indexOf(HWCAMERANAME) < 0 && hostName.indexOf(GALLERY3DNAME) < 0)) {
            return -assertionsDisabled;
        }
        return true;
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
            this.mContainerViewCopy.setTranslationY((float) location[CONTEXT_DISPLAY_SPLIT]);
        }
    }

    protected void initContainerView(View decor) {
        View view = decor.findViewById(R.id.action_bar_container);
        if (view instanceof ActionBarContainer) {
            this.mContainerView = (ActionBarContainer) view;
        }
    }

    protected void initContextView(View decor) {
        View view = decor.findViewById(R.id.action_context_bar);
        if (view instanceof ActionBarContextView) {
            this.mContextView = (ActionBarContextView) view;
        }
    }

    protected ScrollingTabContainerView initScrollingTabContainerView() {
        return new ScrollingTabContainerView(this.mContext);
    }

    protected void setContextView(ActionBarContextView view) {
        this.mContextView = view;
    }

    protected ActionBarContextView getContextView() {
        return this.mContextView;
    }

    protected Context getContext() {
        return this.mContext;
    }

    protected ScrollingTabContainerView getTabScrollView() {
        return this.mTabScrollView;
    }

    protected DecorToolbar getDecorToolbar() {
        return this.mDecorToolbar;
    }

    protected void setContainerView(ActionBarContainer view) {
        this.mContainerView = view;
    }

    protected ActionBarContainer getContainerView() {
        return this.mContainerView;
    }

    protected ActionBarContainer getSplitView() {
        return this.mSplitView;
    }

    protected ActionBarOverlayLayout getOverlayLayout() {
        return this.mOverlayLayout;
    }

    protected int getContextDisplayMode() {
        return this.mContextDisplayMode;
    }

    protected void initCustomPanel(View decor) {
    }

    protected void initActionBarOverlayLayout(View decor) {
        this.mOverlayLayout = (ActionBarOverlayLayout) decor.findViewById(R.id.decor_content_parent);
    }

    protected void setOverlayLayout(ActionBarOverlayLayout overlayLayout) {
        this.mOverlayLayout = overlayLayout;
    }

    protected void setContextDisplayMode(int mode) {
        this.mContextDisplayMode = mode;
    }

    protected Activity getActivity() {
        return this.mActivity;
    }

    public void setAnimationEnable(boolean animationEnabled) {
        if (this.mSplitView != null) {
            this.mSplitView.setAnimationEnable(animationEnabled);
        }
    }

    public void setScrollTabAnimEnable(boolean shouldAnim) {
    }
}
