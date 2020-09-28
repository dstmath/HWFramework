package com.android.internal.policy;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.SearchManager;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.AndroidRuntimeException;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.IRotationWatcher;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputQueue;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.menu.ContextMenuBuilder;
import com.android.internal.view.menu.IconMenuPresenter;
import com.android.internal.view.menu.ListMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuDialogHelper;
import com.android.internal.view.menu.MenuHelper;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.widget.DecorContentParent;
import com.android.internal.widget.FloatingToolbar;
import com.android.internal.widget.SwipeDismissLayout;
import com.huawei.android.fsm.HwFoldScreenManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PhoneWindow extends AbsWindow implements MenuBuilder.Callback {
    private static final String ACTION_BAR_TAG = "android:ActionBar";
    private static final int CUSTOM_TITLE_COMPATIBLE_FEATURES = 13505;
    private static final boolean DEBUG = false;
    private static final int DEFAULT_BACKGROUND_FADE_DURATION_MS = 300;
    static final int FLAG_RESOURCE_SET_ICON = 1;
    static final int FLAG_RESOURCE_SET_ICON_FALLBACK = 4;
    static final int FLAG_RESOURCE_SET_LOGO = 2;
    private static final String FOCUSED_ID_TAG = "android:focusedViewId";
    public static final boolean IS_SIDE_PROP = (!SystemProperties.get("ro.config.hw_curved_side_disp", "").equals(""));
    private static final int NAVIGATION_BAR_DEFAULT_COLOR = -16974597;
    private static final boolean NAVIGATION_BAR_IS_BLACK_PROP = SystemProperties.getBoolean("ro.config.hw_navbar_is_black", false);
    private static final String PACKAGE_NAME_INSTALLER = "com.android.packageinstaller";
    private static final String PANELS_TAG = "android:Panels";
    private static final String TAG = "PhoneWindow";
    private static final Transition USE_DEFAULT_TRANSITION = new TransitionSet();
    private static final String VIEWS_TAG = "android:views";
    static final RotationWatcher sRotationWatcher = new RotationWatcher();
    private ActionMenuPresenterCallback mActionMenuPresenterCallback;
    private ViewRootImpl.ActivityConfigCallback mActivityConfigCallback;
    private Boolean mAllowEnterTransitionOverlap;
    private Boolean mAllowReturnTransitionOverlap;
    private boolean mAlwaysReadCloseOnTouchAttr;
    private boolean mAlwaysSplit;
    private AudioManager mAudioManager;
    Drawable mBackgroundDrawable;
    private long mBackgroundFadeDurationMillis;
    Drawable mBackgroundFallbackDrawable;
    private ProgressBar mCircularProgressBar;
    private boolean mClipToOutline;
    private boolean mClosingActionMenu;
    ViewGroup mContentParent;
    private boolean mContentParentExplicitlySet;
    private Scene mContentScene;
    ContextMenuBuilder mContextMenu;
    final PhoneWindowMenuCallback mContextMenuCallback;
    MenuHelper mContextMenuHelper;
    protected DecorView mDecor;
    private int mDecorCaptionShade;
    DecorContentParent mDecorContentParent;
    private DrawableFeatureState[] mDrawables;
    private float mElevation;
    boolean mEnsureNavigationBarContrastWhenTransparent;
    boolean mEnsureStatusBarContrastWhenTransparent;
    private Transition mEnterTransition;
    private Transition mExitTransition;
    TypedValue mFixedHeightMajor;
    TypedValue mFixedHeightMinor;
    TypedValue mFixedWidthMajor;
    TypedValue mFixedWidthMinor;
    private boolean mForceDecorInstall;
    private boolean mForcedNavigationBarColor;
    private boolean mForcedStatusBarColor;
    private int mFrameResource;
    private ProgressBar mHorizontalProgressBar;
    int mIconRes;
    private int mInvalidatePanelMenuFeatures;
    private boolean mInvalidatePanelMenuPosted;
    private final Runnable mInvalidatePanelMenuRunnable;
    boolean mIsFloating;
    private boolean mIsStartingWindow;
    private boolean mIsTranslucent;
    private boolean mIsTransparent;
    private KeyguardManager mKeyguardManager;
    private LayoutInflater mLayoutInflater;
    private ImageView mLeftIconView;
    private boolean mLoadElevation;
    int mLogoRes;
    private MediaController mMediaController;
    private MediaSessionManager mMediaSessionManager;
    final TypedValue mMinWidthMajor;
    final TypedValue mMinWidthMinor;
    int mNavigationBarColor;
    int mNavigationBarDividerColor;
    private String mPackageName;
    int mPanelChordingKey;
    private PanelMenuPresenterCallback mPanelMenuPresenterCallback;
    private PanelFeatureState[] mPanels;
    PanelFeatureState mPreparedPanel;
    private Transition mReenterTransition;
    int mResourcesSetFlags;
    private Transition mReturnTransition;
    private ImageView mRightIconView;
    private Transition mSharedElementEnterTransition;
    private Transition mSharedElementExitTransition;
    private Transition mSharedElementReenterTransition;
    private Transition mSharedElementReturnTransition;
    private Boolean mSharedElementsUseOverlay;
    protected boolean mSpecialSet;
    int mStatusBarColor;
    private boolean mSupportsPictureInPicture;
    InputQueue.Callback mTakeInputQueueCallback;
    SurfaceHolder.Callback2 mTakeSurfaceCallback;
    private int mTextColor;
    private int mTheme;
    @UnsupportedAppUsage
    private CharSequence mTitle;
    private int mTitleColor;
    private TextView mTitleView;
    private TransitionManager mTransitionManager;
    private int mUiOptions;
    protected boolean mUseDecorContext;
    private int mVolumeControlStreamType;

    /* access modifiers changed from: package-private */
    public static class WindowManagerHolder {
        static final IWindowManager sWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

        WindowManagerHolder() {
        }
    }

    @UnsupportedAppUsage
    public PhoneWindow(Context context) {
        super(context);
        this.mPackageName = "";
        this.mContextMenuCallback = new PhoneWindowMenuCallback(this);
        this.mMinWidthMajor = new TypedValue();
        this.mMinWidthMinor = new TypedValue();
        this.mForceDecorInstall = false;
        this.mContentParentExplicitlySet = false;
        this.mBackgroundDrawable = null;
        this.mBackgroundFallbackDrawable = null;
        this.mLoadElevation = true;
        this.mFrameResource = 0;
        this.mTextColor = 0;
        this.mStatusBarColor = 0;
        this.mNavigationBarColor = 0;
        this.mNavigationBarDividerColor = 0;
        this.mForcedStatusBarColor = false;
        this.mForcedNavigationBarColor = false;
        this.mTitle = null;
        this.mTitleColor = 0;
        this.mAlwaysReadCloseOnTouchAttr = false;
        this.mVolumeControlStreamType = Integer.MIN_VALUE;
        this.mUiOptions = 0;
        this.mInvalidatePanelMenuRunnable = new Runnable() {
            /* class com.android.internal.policy.PhoneWindow.AnonymousClass1 */

            public void run() {
                for (int i = 0; i <= 13; i++) {
                    if ((PhoneWindow.this.mInvalidatePanelMenuFeatures & (1 << i)) != 0) {
                        PhoneWindow.this.doInvalidatePanelMenu(i);
                    }
                }
                PhoneWindow.this.mInvalidatePanelMenuPosted = false;
                PhoneWindow.this.mInvalidatePanelMenuFeatures = 0;
            }
        };
        this.mEnterTransition = null;
        Transition transition = USE_DEFAULT_TRANSITION;
        this.mReturnTransition = transition;
        this.mExitTransition = null;
        this.mReenterTransition = transition;
        this.mSharedElementEnterTransition = null;
        this.mSharedElementReturnTransition = transition;
        this.mSharedElementExitTransition = null;
        this.mSharedElementReenterTransition = transition;
        this.mBackgroundFadeDurationMillis = -1;
        this.mTheme = -1;
        this.mDecorCaptionShade = 0;
        this.mUseDecorContext = false;
        this.mSpecialSet = false;
        this.mAlwaysSplit = false;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override // android.view.Window
    public void onFreeFormOutLineChanged(int touchingState) {
        DecorView decorView = this.mDecor;
        if (decorView == null) {
            HwFreeFormUtils.log(TAG, "FreeFormOutLine change to color: " + HwFreeFormUtils.intToColor(touchingState) + ",but decorview is null.");
        } else if (touchingState == 1) {
            decorView.refreshFreeFormOutLineState(false, false, true);
        } else if (touchingState == 2) {
            decorView.refreshFreeFormOutLineState(false, false, false);
        } else if (touchingState == 3) {
            decorView.refreshFreeFormOutLineState(false, true, false);
        } else if (touchingState == 4) {
            decorView.refreshFreeFormOutLineState(true, false, false);
        }
    }

    @Override // android.view.Window
    public void restoreFreeFormConfig() {
        DecorView decorView = this.mDecor;
        if (decorView == null) {
            HwFreeFormUtils.log(TAG, "restoreFreeFormConfig,but decorview is null.");
        } else {
            decorView.setWindowFrameForced(null);
        }
    }

    private void isNeedHideForeground(DecorView dv) {
        if (HwFreeFormUtils.isFreeFormEnable() && PACKAGE_NAME_INSTALLER.equals(getContext().getPackageName())) {
            dv.setHideFreeFormForeground();
        }
    }

    public PhoneWindow(Context context, Window preservedWindow, ViewRootImpl.ActivityConfigCallback activityConfigCallback) {
        this(context);
        boolean z = true;
        this.mUseDecorContext = true;
        if (preservedWindow != null) {
            this.mDecor = (DecorView) preservedWindow.getDecorView();
            this.mElevation = preservedWindow.getElevation();
            this.mLoadElevation = false;
            this.mForceDecorInstall = true;
            getAttributes().token = preservedWindow.getAttributes().token;
        }
        if (!(Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_FORCE_RESIZABLE_ACTIVITIES, 0) != 0) && !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            z = false;
        }
        this.mSupportsPictureInPicture = z;
        this.mActivityConfigCallback = activityConfigCallback;
    }

    @Override // android.view.Window
    public final void setContainer(Window container) {
        super.setContainer(container);
    }

    @Override // android.view.Window
    public boolean requestFeature(int featureId) {
        if (!this.mContentParentExplicitlySet) {
            int features = getFeatures();
            int newFeatures = (1 << featureId) | features;
            if ((newFeatures & 128) != 0 && (newFeatures & -13506) != 0) {
                throw new AndroidRuntimeException("You cannot combine custom titles with other title features");
            } else if ((features & 2) != 0 && featureId == 8) {
                return false;
            } else {
                if ((features & 256) != 0 && featureId == 1) {
                    removeFeature(8);
                }
                if ((features & 256) != 0 && featureId == 11) {
                    throw new AndroidRuntimeException("You cannot combine swipe dismissal and the action bar.");
                } else if ((features & 2048) != 0 && featureId == 8) {
                    throw new AndroidRuntimeException("You cannot combine swipe dismissal and the action bar.");
                } else if (featureId != 5 || !getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH)) {
                    return super.requestFeature(featureId);
                } else {
                    throw new AndroidRuntimeException("You cannot use indeterminate progress on a watch.");
                }
            }
        } else {
            throw new AndroidRuntimeException("requestFeature() must be called before adding content");
        }
    }

    @Override // android.view.Window
    public void setUiOptions(int uiOptions) {
        this.mUiOptions = uiOptions;
    }

    @Override // android.view.Window
    public void setUiOptions(int uiOptions, int mask) {
        this.mUiOptions = (this.mUiOptions & (~mask)) | (uiOptions & mask);
    }

    @Override // android.view.Window
    public TransitionManager getTransitionManager() {
        return this.mTransitionManager;
    }

    @Override // android.view.Window
    public void setTransitionManager(TransitionManager tm) {
        this.mTransitionManager = tm;
    }

    @Override // android.view.Window
    public Scene getContentScene() {
        return this.mContentScene;
    }

    @Override // android.view.Window
    public void setContentView(int layoutResID) {
        if (this.mContentParent == null) {
            installDecor();
        } else if (!hasFeature(12)) {
            this.mContentParent.removeAllViews();
        }
        if (hasFeature(12)) {
            transitionTo(Scene.getSceneForLayout(this.mContentParent, layoutResID, getContext()));
        } else {
            this.mLayoutInflater.inflate(layoutResID, this.mContentParent);
        }
        this.mContentParent.requestApplyInsets();
        Window.Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        this.mContentParentExplicitlySet = true;
    }

    @Override // android.view.Window
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(-1, -1));
    }

    @Override // android.view.Window
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (this.mContentParent == null) {
            installDecor();
        } else if (!hasFeature(12)) {
            this.mContentParent.removeAllViews();
        }
        if (hasFeature(12)) {
            view.setLayoutParams(params);
            transitionTo(new Scene(this.mContentParent, view));
        } else {
            this.mContentParent.addView(view, params);
        }
        this.mContentParent.requestApplyInsets();
        Window.Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        this.mContentParentExplicitlySet = true;
    }

    @Override // android.view.Window
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        if (this.mContentParent == null) {
            installDecor();
        }
        if (hasFeature(12)) {
            Log.v(TAG, "addContentView does not support content transitions");
        }
        this.mContentParent.addView(view, params);
        this.mContentParent.requestApplyInsets();
        Window.Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
    }

    @Override // android.view.Window
    public void clearContentView() {
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.clearContentView();
        }
    }

    private void transitionTo(Scene scene) {
        if (this.mContentScene == null) {
            scene.enter();
        } else {
            this.mTransitionManager.transitionTo(scene);
        }
        this.mContentScene = scene;
    }

    @Override // android.view.Window
    public View getCurrentFocus() {
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            return decorView.findFocus();
        }
        return null;
    }

    @Override // android.view.Window
    public void takeSurface(SurfaceHolder.Callback2 callback) {
        this.mTakeSurfaceCallback = callback;
    }

    @Override // android.view.Window
    public void takeInputQueue(InputQueue.Callback callback) {
        this.mTakeInputQueueCallback = callback;
    }

    @Override // android.view.Window
    public boolean isFloating() {
        return this.mIsFloating;
    }

    public boolean isTranslucent() {
        return this.mIsTranslucent;
    }

    /* access modifiers changed from: package-private */
    public boolean isShowingWallpaper() {
        return (getAttributes().flags & 1048576) != 0;
    }

    @Override // android.view.Window
    public LayoutInflater getLayoutInflater() {
        return this.mLayoutInflater;
    }

    @Override // android.view.Window
    public void setTitle(CharSequence title) {
        setTitle(title, true);
    }

    public void setTitle(CharSequence title, boolean updateAccessibilityTitle) {
        ViewRootImpl vr;
        TextView textView = this.mTitleView;
        if (textView != null) {
            textView.setText(title);
        } else {
            DecorContentParent decorContentParent = this.mDecorContentParent;
            if (decorContentParent != null) {
                decorContentParent.setWindowTitle(title);
            }
        }
        this.mTitle = title;
        if (updateAccessibilityTitle) {
            WindowManager.LayoutParams params = getAttributes();
            if (!TextUtils.equals(title, params.accessibilityTitle)) {
                params.accessibilityTitle = TextUtils.stringOrSpannedString(title);
                DecorView decorView = this.mDecor;
                if (!(decorView == null || (vr = decorView.getViewRootImpl()) == null)) {
                    vr.onWindowTitleChanged();
                }
                dispatchWindowAttributesChanged(getAttributes());
            }
        }
    }

    @Override // android.view.Window
    @Deprecated
    public void setTitleColor(int textColor) {
        TextView textView = this.mTitleView;
        if (textView != null) {
            textView.setTextColor(textColor);
        }
        this.mTitleColor = textColor;
    }

    public final boolean preparePanel(PanelFeatureState st, KeyEvent event) {
        DecorContentParent decorContentParent;
        DecorContentParent decorContentParent2;
        DecorContentParent decorContentParent3;
        if (isDestroyed()) {
            return false;
        }
        if (st.isPrepared) {
            return true;
        }
        PanelFeatureState panelFeatureState = this.mPreparedPanel;
        if (!(panelFeatureState == null || panelFeatureState == st)) {
            closePanel(panelFeatureState, false);
        }
        Window.Callback cb = getCallback();
        if (cb != null) {
            st.createdPanelView = cb.onCreatePanelView(st.featureId);
        }
        boolean isActionBarMenu = st.featureId == 0 || st.featureId == 8;
        if (isActionBarMenu && (decorContentParent3 = this.mDecorContentParent) != null) {
            decorContentParent3.setMenuPrepared();
        }
        if (st.createdPanelView == null) {
            if (st.menu == null || st.refreshMenuContent) {
                if (st.menu == null && (!initializePanelMenu(st) || st.menu == null)) {
                    return false;
                }
                if (isActionBarMenu && this.mDecorContentParent != null) {
                    if (this.mActionMenuPresenterCallback == null) {
                        this.mActionMenuPresenterCallback = new ActionMenuPresenterCallback();
                    }
                    this.mDecorContentParent.setMenu(st.menu, this.mActionMenuPresenterCallback);
                }
                st.menu.stopDispatchingItemsChanged();
                if (cb == null || !cb.onCreatePanelMenu(st.featureId, st.menu)) {
                    st.setMenu(null);
                    if (isActionBarMenu && (decorContentParent2 = this.mDecorContentParent) != null) {
                        decorContentParent2.setMenu(null, this.mActionMenuPresenterCallback);
                    }
                    return false;
                }
                st.refreshMenuContent = false;
            }
            st.menu.stopDispatchingItemsChanged();
            if (st.frozenActionViewState != null) {
                st.menu.restoreActionViewStates(st.frozenActionViewState);
                st.frozenActionViewState = null;
            }
            if (!cb.onPreparePanel(st.featureId, st.createdPanelView, st.menu)) {
                if (isActionBarMenu && (decorContentParent = this.mDecorContentParent) != null) {
                    decorContentParent.setMenu(null, this.mActionMenuPresenterCallback);
                }
                st.menu.startDispatchingItemsChanged();
                return false;
            }
            st.qwertyMode = KeyCharacterMap.load(event != null ? event.getDeviceId() : -1).getKeyboardType() != 1;
            st.menu.setQwertyMode(st.qwertyMode);
            st.menu.startDispatchingItemsChanged();
        }
        st.isPrepared = true;
        st.isHandled = false;
        this.mPreparedPanel = st;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateParamsOnConfigChanged() {
        if (!HwFoldScreenManager.isFoldable()) {
            Log.d(TAG, "updateStyledWidth: CoordinationModeUtils is not foldable.");
        } else if (this.mActivityConfigCallback != null) {
            Log.d(TAG, "updateStyledWidth: mActivityConfigCallback is " + this.mActivityConfigCallback);
        } else {
            if (this.mFixedWidthMajor != null) {
                TypedValue fixedWidthMajor = new TypedValue();
                try {
                    if (getContext().getTheme().resolveAttribute(R.attr.windowFixedWidthMajor, fixedWidthMajor, true)) {
                        this.mFixedWidthMajor = fixedWidthMajor;
                    }
                } catch (Resources.NotFoundException e) {
                }
            }
            if (this.mFixedWidthMinor != null) {
                TypedValue fixedWidthMinor = new TypedValue();
                try {
                    if (getContext().getTheme().resolveAttribute(R.attr.windowFixedWidthMinor, fixedWidthMinor, true)) {
                        this.mFixedWidthMinor = fixedWidthMinor;
                    }
                } catch (Resources.NotFoundException e2) {
                }
            }
        }
    }

    @Override // android.view.Window
    public void onConfigurationChanged(Configuration newConfig) {
        PanelFeatureState st;
        if (this.mDecorContentParent == null && (st = getPanelState(0, false)) != null && st.menu != null) {
            if (st.isOpen) {
                Bundle state = new Bundle();
                if (st.iconMenuPresenter != null) {
                    st.iconMenuPresenter.saveHierarchyState(state);
                }
                if (st.listMenuPresenter != null) {
                    st.listMenuPresenter.saveHierarchyState(state);
                }
                clearMenuViews(st);
                reopenMenu(false);
                if (st.iconMenuPresenter != null) {
                    st.iconMenuPresenter.restoreHierarchyState(state);
                }
                if (st.listMenuPresenter != null) {
                    st.listMenuPresenter.restoreHierarchyState(state);
                    return;
                }
                return;
            }
            clearMenuViews(st);
        }
    }

    @Override // android.view.Window
    public void onMultiWindowModeChanged() {
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.onConfigurationChanged(getContext().getResources().getConfiguration());
        }
    }

    @Override // android.view.Window
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updatePictureInPictureOutlineProvider(isInPictureInPictureMode);
        }
    }

    @Override // android.view.Window
    public void reportActivityRelaunched() {
        DecorView decorView = this.mDecor;
        if (decorView != null && decorView.getViewRootImpl() != null) {
            this.mDecor.getViewRootImpl().reportActivityRelaunched();
        }
    }

    private static void clearMenuViews(PanelFeatureState st) {
        st.createdPanelView = null;
        st.refreshDecorView = true;
        st.clearMenuPresenters();
    }

    @Override // android.view.Window
    public final void openPanel(int featureId, KeyEvent event) {
        DecorContentParent decorContentParent;
        if (featureId != 0 || (decorContentParent = this.mDecorContentParent) == null || !decorContentParent.canShowOverflowMenu() || ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
            openPanel(getPanelState(featureId, true), event);
        } else {
            this.mDecorContentParent.showOverflowMenu();
        }
    }

    private void openPanel(PanelFeatureState st, KeyEvent event) {
        int backgroundResId;
        ViewGroup.LayoutParams lp;
        if (!st.isOpen && !isDestroyed()) {
            if (st.featureId == 0) {
                Context context = getContext();
                boolean isXLarge = (context.getResources().getConfiguration().screenLayout & 15) == 4;
                boolean isHoneycombApp = context.getApplicationInfo().targetSdkVersion >= 11;
                if (isXLarge && isHoneycombApp) {
                    return;
                }
            }
            Window.Callback cb = getCallback();
            if (cb == null || cb.onMenuOpened(st.featureId, st.menu)) {
                WindowManager wm = getWindowManager();
                if (wm != null && preparePanel(st, event)) {
                    int width = -2;
                    if (st.decorView == null || st.refreshDecorView) {
                        if (st.decorView == null) {
                            if (!initializePanelDecor(st) || st.decorView == null) {
                                return;
                            }
                        } else if (st.refreshDecorView && st.decorView.getChildCount() > 0) {
                            st.decorView.removeAllViews();
                        }
                        if (initializePanelContent(st) && st.hasPanelItems()) {
                            ViewGroup.LayoutParams lp2 = st.shownPanelView.getLayoutParams();
                            if (lp2 == null) {
                                lp2 = new ViewGroup.LayoutParams(-2, -2);
                            }
                            if (lp2.width == -1) {
                                backgroundResId = st.fullBackground;
                                width = -1;
                            } else {
                                backgroundResId = st.background;
                            }
                            st.decorView.setWindowBackground(getContext().getDrawable(backgroundResId));
                            ViewParent shownPanelParent = st.shownPanelView.getParent();
                            if (shownPanelParent != null && (shownPanelParent instanceof ViewGroup)) {
                                ((ViewGroup) shownPanelParent).removeView(st.shownPanelView);
                            }
                            st.decorView.addView(st.shownPanelView, lp2);
                            if (!st.shownPanelView.hasFocus()) {
                                st.shownPanelView.requestFocus();
                            }
                        } else {
                            return;
                        }
                    } else if (!st.isInListMode()) {
                        width = -1;
                    } else if (!(st.createdPanelView == null || (lp = st.createdPanelView.getLayoutParams()) == null || lp.width != -1)) {
                        width = -1;
                    }
                    st.isHandled = false;
                    WindowManager.LayoutParams lp3 = new WindowManager.LayoutParams(width, -2, st.x, st.y, 1003, 8519680, st.decorView.mDefaultOpacity);
                    if (st.isCompact) {
                        lp3.gravity = getOptionsPanelGravity();
                        sRotationWatcher.addWindow(this);
                    } else {
                        lp3.gravity = st.gravity;
                    }
                    lp3.windowAnimations = st.windowAnimations;
                    wm.addView(st.decorView, lp3);
                    st.isOpen = true;
                    return;
                }
                return;
            }
            closePanel(st, true);
        }
    }

    @Override // android.view.Window
    public final void closePanel(int featureId) {
        DecorContentParent decorContentParent;
        if (featureId == 0 && (decorContentParent = this.mDecorContentParent) != null && decorContentParent.canShowOverflowMenu() && !ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
            this.mDecorContentParent.hideOverflowMenu();
        } else if (featureId == 6) {
            closeContextMenu();
        } else {
            closePanel(getPanelState(featureId, true), true);
        }
    }

    public final void closePanel(PanelFeatureState st, boolean doCallback) {
        DecorContentParent decorContentParent;
        if (!doCallback || st.featureId != 0 || (decorContentParent = this.mDecorContentParent) == null || !decorContentParent.isOverflowMenuShowing()) {
            ViewManager wm = getWindowManager();
            if (wm != null && st.isOpen) {
                if (st.decorView != null) {
                    wm.removeView(st.decorView);
                    if (st.isCompact) {
                        sRotationWatcher.removeWindow(this);
                    }
                }
                if (doCallback) {
                    callOnPanelClosed(st.featureId, st, null);
                }
            }
            st.isPrepared = false;
            st.isHandled = false;
            st.isOpen = false;
            st.shownPanelView = null;
            if (st.isInExpandedMode) {
                st.refreshDecorView = true;
                st.isInExpandedMode = false;
            }
            if (this.mPreparedPanel == st) {
                this.mPreparedPanel = null;
                this.mPanelChordingKey = 0;
                return;
            }
            return;
        }
        checkCloseActionMenu(st.menu);
    }

    /* access modifiers changed from: package-private */
    public void checkCloseActionMenu(Menu menu) {
        if (!this.mClosingActionMenu) {
            this.mClosingActionMenu = true;
            this.mDecorContentParent.dismissPopups();
            Window.Callback cb = getCallback();
            if (cb != null && !isDestroyed()) {
                cb.onPanelClosed(8, menu);
            }
            this.mClosingActionMenu = false;
        }
    }

    @Override // android.view.Window
    public final void togglePanel(int featureId, KeyEvent event) {
        PanelFeatureState st = getPanelState(featureId, true);
        if (st.isOpen) {
            closePanel(st, true);
        } else {
            openPanel(st, event);
        }
    }

    @Override // android.view.Window
    public void invalidatePanelMenu(int featureId) {
        DecorView decorView;
        this.mInvalidatePanelMenuFeatures |= 1 << featureId;
        if (!this.mInvalidatePanelMenuPosted && (decorView = this.mDecor) != null) {
            decorView.postOnAnimation(this.mInvalidatePanelMenuRunnable);
            this.mInvalidatePanelMenuPosted = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void doPendingInvalidatePanelMenu() {
        if (this.mInvalidatePanelMenuPosted) {
            this.mDecor.removeCallbacks(this.mInvalidatePanelMenuRunnable);
            this.mInvalidatePanelMenuRunnable.run();
        }
    }

    /* access modifiers changed from: package-private */
    public void doInvalidatePanelMenu(int featureId) {
        PanelFeatureState st;
        PanelFeatureState st2 = getPanelState(featureId, false);
        if (st2 != null) {
            if (st2.menu != null) {
                Bundle savedActionViewStates = new Bundle();
                st2.menu.saveActionViewStates(savedActionViewStates);
                if (savedActionViewStates.size() > 0) {
                    st2.frozenActionViewState = savedActionViewStates;
                }
                st2.menu.stopDispatchingItemsChanged();
                st2.menu.clear();
            }
            st2.refreshMenuContent = true;
            st2.refreshDecorView = true;
            if ((featureId == 8 || featureId == 0) && this.mDecorContentParent != null && (st = getPanelState(0, false)) != null) {
                st.isPrepared = false;
                preparePanel(st, null);
            }
        }
    }

    public final boolean onKeyDownPanel(int featureId, KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0) {
            this.mPanelChordingKey = keyCode;
            PanelFeatureState st = getPanelState(featureId, false);
            if (st != null && !st.isOpen) {
                return preparePanel(st, event);
            }
        }
        return false;
    }

    public final void onKeyUpPanel(int featureId, KeyEvent event) {
        DecorContentParent decorContentParent;
        if (this.mPanelChordingKey != 0) {
            this.mPanelChordingKey = 0;
            PanelFeatureState st = getPanelState(featureId, false);
            if (!event.isCanceled()) {
                DecorView decorView = this.mDecor;
                if ((decorView == null || decorView.mPrimaryActionMode == null) && st != null) {
                    boolean playSoundEffect = false;
                    if (featureId != 0 || (decorContentParent = this.mDecorContentParent) == null || !decorContentParent.canShowOverflowMenu() || ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
                        if (st.isOpen || st.isHandled) {
                            playSoundEffect = st.isOpen;
                            closePanel(st, true);
                        } else if (st.isPrepared) {
                            boolean show = true;
                            if (st.refreshMenuContent) {
                                st.isPrepared = false;
                                show = preparePanel(st, event);
                            }
                            if (show) {
                                EventLog.writeEvent(50001, 0);
                                playSoundEffect = true;
                            }
                        }
                    } else if (this.mDecorContentParent.isOverflowMenuShowing()) {
                        playSoundEffect = this.mDecorContentParent.hideOverflowMenu();
                    } else if (!isDestroyed() && preparePanel(st, event)) {
                        playSoundEffect = this.mDecorContentParent.showOverflowMenu();
                    }
                    if (playSoundEffect) {
                        AudioManager audioManager = (AudioManager) getContext().getSystemService("audio");
                        if (audioManager != null) {
                            audioManager.playSoundEffect(0);
                        } else {
                            Log.w(TAG, "Couldn't get audio manager");
                        }
                    }
                }
            }
        }
    }

    @Override // android.view.Window
    public final void closeAllPanels() {
        if (getWindowManager() != null) {
            PanelFeatureState[] panels = this.mPanels;
            int N = panels != null ? panels.length : 0;
            for (int i = 0; i < N; i++) {
                PanelFeatureState panel = panels[i];
                if (panel != null) {
                    closePanel(panel, true);
                }
            }
            closeContextMenu();
        }
    }

    private synchronized void closeContextMenu() {
        if (this.mContextMenu != null) {
            this.mContextMenu.close();
            dismissContextMenu();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void dismissContextMenu() {
        this.mContextMenu = null;
        if (this.mContextMenuHelper != null) {
            this.mContextMenuHelper.dismiss();
            this.mContextMenuHelper = null;
        }
    }

    @Override // android.view.Window
    public boolean performPanelShortcut(int featureId, int keyCode, KeyEvent event, int flags) {
        return performPanelShortcut(getPanelState(featureId, false), keyCode, event, flags);
    }

    /* access modifiers changed from: package-private */
    public boolean performPanelShortcut(PanelFeatureState st, int keyCode, KeyEvent event, int flags) {
        if (event.isSystem() || st == null) {
            return false;
        }
        boolean handled = false;
        if ((st.isPrepared || preparePanel(st, event)) && st.menu != null) {
            handled = st.menu.performShortcut(keyCode, event, flags);
        }
        if (handled) {
            st.isHandled = true;
            if ((flags & 1) == 0 && this.mDecorContentParent == null) {
                closePanel(st, true);
            }
        }
        return handled;
    }

    @Override // android.view.Window
    public boolean performPanelIdentifierAction(int featureId, int id, int flags) {
        PanelFeatureState st = getPanelState(featureId, true);
        if (!preparePanel(st, new KeyEvent(0, 82)) || st.menu == null) {
            return false;
        }
        boolean res = st.menu.performIdentifierAction(id, flags);
        if (this.mDecorContentParent == null) {
            closePanel(st, true);
        }
        return res;
    }

    public PanelFeatureState findMenuPanel(Menu menu) {
        PanelFeatureState[] panels = this.mPanels;
        int N = panels != null ? panels.length : 0;
        for (int i = 0; i < N; i++) {
            PanelFeatureState panel = panels[i];
            if (panel != null && panel.menu == menu) {
                return panel;
            }
        }
        return null;
    }

    @Override // com.android.internal.view.menu.MenuBuilder.Callback
    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        PanelFeatureState panel;
        Window.Callback cb = getCallback();
        if (cb == null || isDestroyed() || (panel = findMenuPanel(menu.getRootMenu())) == null) {
            return false;
        }
        return cb.onMenuItemSelected(panel.featureId, item);
    }

    @Override // com.android.internal.view.menu.MenuBuilder.Callback
    public void onMenuModeChange(MenuBuilder menu) {
        reopenMenu(true);
    }

    private void reopenMenu(boolean toggleMenuMode) {
        DecorContentParent decorContentParent = this.mDecorContentParent;
        if (decorContentParent == null || !decorContentParent.canShowOverflowMenu() || (ViewConfiguration.get(getContext()).hasPermanentMenuKey() && !this.mDecorContentParent.isOverflowMenuShowPending())) {
            PanelFeatureState st = getPanelState(0, false);
            if (st != null) {
                boolean newExpandedMode = st.isInExpandedMode;
                if (toggleMenuMode) {
                    newExpandedMode = !newExpandedMode;
                }
                st.refreshDecorView = true;
                closePanel(st, false);
                st.isInExpandedMode = newExpandedMode;
                openPanel(st, (KeyEvent) null);
                return;
            }
            return;
        }
        Window.Callback cb = getCallback();
        if (this.mDecorContentParent.isOverflowMenuShowing() && toggleMenuMode) {
            this.mDecorContentParent.hideOverflowMenu();
            PanelFeatureState st2 = getPanelState(0, false);
            if (st2 != null && cb != null && !isDestroyed()) {
                cb.onPanelClosed(8, st2.menu);
            }
        } else if (cb != null && !isDestroyed()) {
            if (this.mInvalidatePanelMenuPosted && (1 & this.mInvalidatePanelMenuFeatures) != 0) {
                this.mDecor.removeCallbacks(this.mInvalidatePanelMenuRunnable);
                this.mInvalidatePanelMenuRunnable.run();
            }
            PanelFeatureState st3 = getPanelState(0, false);
            if (st3 != null && st3.menu != null && !st3.refreshMenuContent && cb.onPreparePanel(0, st3.createdPanelView, st3.menu)) {
                cb.onMenuOpened(8, st3.menu);
                this.mDecorContentParent.showOverflowMenu();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean initializePanelMenu(PanelFeatureState st) {
        Context context = getContext();
        if ((st.featureId == 0 || st.featureId == 8) && this.mDecorContentParent != null) {
            TypedValue outValue = new TypedValue();
            Resources.Theme baseTheme = context.getTheme();
            baseTheme.resolveAttribute(16843825, outValue, true);
            Resources.Theme widgetTheme = null;
            if (outValue.resourceId != 0) {
                widgetTheme = context.getResources().newTheme();
                widgetTheme.setTo(baseTheme);
                widgetTheme.applyStyle(outValue.resourceId, true);
                widgetTheme.resolveAttribute(16843671, outValue, true);
            } else {
                baseTheme.resolveAttribute(16843671, outValue, true);
            }
            if (outValue.resourceId != 0) {
                if (widgetTheme == null) {
                    widgetTheme = context.getResources().newTheme();
                    widgetTheme.setTo(baseTheme);
                }
                widgetTheme.applyStyle(outValue.resourceId, true);
            }
            if (widgetTheme != null) {
                context = new ContextThemeWrapper(context, 0);
                context.getTheme().setTo(widgetTheme);
            }
        }
        MenuBuilder menu = new MenuBuilder(context);
        menu.setCallback(this);
        st.setMenu(menu);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean initializePanelDecor(PanelFeatureState st) {
        st.decorView = generateDecor(st.featureId);
        st.gravity = 81;
        st.setStyle(getContext());
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.Window, 0, st.listPresenterTheme);
        float elevation = a.getDimension(38, 0.0f);
        if (elevation != 0.0f) {
            st.decorView.setElevation(elevation);
        }
        a.recycle();
        return true;
    }

    private int getOptionsPanelGravity() {
        try {
            return WindowManagerHolder.sWindowManager.getPreferredOptionsPanelGravity(getContext().getDisplayId());
        } catch (RemoteException ex) {
            Log.e(TAG, "Couldn't getOptionsPanelGravity; using default", ex);
            return 81;
        }
    }

    /* access modifiers changed from: package-private */
    public void onOptionsPanelRotationChanged() {
        PanelFeatureState st = getPanelState(0, false);
        if (st != null) {
            WindowManager.LayoutParams lp = st.decorView != null ? (WindowManager.LayoutParams) st.decorView.getLayoutParams() : null;
            if (lp != null) {
                lp.gravity = getOptionsPanelGravity();
                ViewManager wm = getWindowManager();
                if (wm != null) {
                    wm.updateViewLayout(st.decorView, lp);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean initializePanelContent(PanelFeatureState st) {
        MenuView menuView;
        if (st.createdPanelView != null) {
            st.shownPanelView = st.createdPanelView;
            return true;
        } else if (st.menu == null) {
            return false;
        } else {
            if (this.mPanelMenuPresenterCallback == null) {
                this.mPanelMenuPresenterCallback = new PanelMenuPresenterCallback();
            }
            if (st.isInListMode()) {
                menuView = st.getListMenuView(getContext(), this.mPanelMenuPresenterCallback);
            } else {
                menuView = st.getIconMenuView(getContext(), this.mPanelMenuPresenterCallback);
            }
            st.shownPanelView = (View) menuView;
            if (st.shownPanelView == null) {
                return false;
            }
            int defaultAnimations = menuView.getWindowAnimations();
            if (defaultAnimations != 0) {
                st.windowAnimations = defaultAnimations;
            }
            return true;
        }
    }

    @Override // android.view.Window
    public boolean performContextMenuIdentifierAction(int id, int flags) {
        ContextMenuBuilder contextMenuBuilder = this.mContextMenu;
        if (contextMenuBuilder != null) {
            return contextMenuBuilder.performIdentifierAction(id, flags);
        }
        return false;
    }

    @Override // android.view.Window
    public final void setElevation(float elevation) {
        this.mElevation = elevation;
        WindowManager.LayoutParams attrs = getAttributes();
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.setElevation(elevation);
            attrs.setSurfaceInsets(this.mDecor, true, false);
        }
        dispatchWindowAttributesChanged(attrs);
    }

    @Override // android.view.Window
    public float getElevation() {
        return this.mElevation;
    }

    @Override // android.view.Window
    public final void setClipToOutline(boolean clipToOutline) {
        this.mClipToOutline = clipToOutline;
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.setClipToOutline(clipToOutline);
        }
    }

    @Override // android.view.Window
    public final void setBackgroundDrawable(Drawable drawable) {
        if (drawable != this.mBackgroundDrawable) {
            this.mBackgroundDrawable = drawable;
            DecorView decorView = this.mDecor;
            if (decorView != null) {
                decorView.setWindowBackground(drawable);
                Drawable drawable2 = this.mBackgroundFallbackDrawable;
                if (drawable2 != null) {
                    DecorView decorView2 = this.mDecor;
                    if (drawable != null) {
                        drawable2 = null;
                    }
                    decorView2.setBackgroundFallback(drawable2);
                }
            }
        }
    }

    @Override // android.view.Window
    public final void setFeatureDrawableResource(int featureId, int resId) {
        if (resId != 0) {
            DrawableFeatureState st = getDrawableState(featureId, true);
            if (st.resid != resId) {
                st.resid = resId;
                st.uri = null;
                st.local = getContext().getDrawable(resId);
                updateDrawable(featureId, st, false);
                return;
            }
            return;
        }
        setFeatureDrawable(featureId, null);
    }

    @Override // android.view.Window
    public final void setFeatureDrawableUri(int featureId, Uri uri) {
        if (uri != null) {
            DrawableFeatureState st = getDrawableState(featureId, true);
            if (st.uri == null || !st.uri.equals(uri)) {
                st.resid = 0;
                st.uri = uri;
                st.local = loadImageURI(uri);
                updateDrawable(featureId, st, false);
                return;
            }
            return;
        }
        setFeatureDrawable(featureId, null);
    }

    @Override // android.view.Window
    public final void setFeatureDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        st.resid = 0;
        st.uri = null;
        if (st.local != drawable) {
            st.local = drawable;
            updateDrawable(featureId, st, false);
        }
    }

    @Override // android.view.Window
    public void setFeatureDrawableAlpha(int featureId, int alpha) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        if (st.alpha != alpha) {
            st.alpha = alpha;
            updateDrawable(featureId, st, false);
        }
    }

    /* access modifiers changed from: protected */
    public final void setFeatureDefaultDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        if (st.def != drawable) {
            st.def = drawable;
            updateDrawable(featureId, st, false);
        }
    }

    @Override // android.view.Window
    public final void setFeatureInt(int featureId, int value) {
        updateInt(featureId, value, false);
    }

    /* access modifiers changed from: protected */
    public final void updateDrawable(int featureId, boolean fromActive) {
        DrawableFeatureState st = getDrawableState(featureId, false);
        if (st != null) {
            updateDrawable(featureId, st, fromActive);
        }
    }

    /* access modifiers changed from: protected */
    public void onDrawableChanged(int featureId, Drawable drawable, int alpha) {
        ImageView view;
        if (featureId == 3) {
            view = getLeftIconView();
        } else if (featureId == 4) {
            view = getRightIconView();
        } else {
            return;
        }
        if (drawable != null) {
            drawable.setAlpha(alpha);
            view.setImageDrawable(drawable);
            view.setVisibility(0);
            return;
        }
        view.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onIntChanged(int featureId, int value) {
        FrameLayout titleContainer;
        if (featureId == 2 || featureId == 5) {
            updateProgressBars(value);
        } else if (featureId == 7 && (titleContainer = (FrameLayout) findViewById(R.id.title_container)) != null) {
            this.mLayoutInflater.inflate(value, titleContainer);
        }
    }

    private void updateProgressBars(int value) {
        ProgressBar circularProgressBar = getCircularProgressBar(true);
        ProgressBar horizontalProgressBar = getHorizontalProgressBar(true);
        int features = getLocalFeatures();
        if (value == -1) {
            if ((features & 4) != 0) {
                if (horizontalProgressBar != null) {
                    horizontalProgressBar.setVisibility((horizontalProgressBar.isIndeterminate() || horizontalProgressBar.getProgress() < 10000) ? 0 : 4);
                } else {
                    Log.e(TAG, "Horizontal progress bar not located in current window decor");
                }
            }
            if ((features & 32) == 0) {
                return;
            }
            if (circularProgressBar != null) {
                circularProgressBar.setVisibility(0);
            } else {
                Log.e(TAG, "Circular progress bar not located in current window decor");
            }
        } else if (value == -2) {
            if ((features & 4) != 0) {
                if (horizontalProgressBar != null) {
                    horizontalProgressBar.setVisibility(8);
                } else {
                    Log.e(TAG, "Horizontal progress bar not located in current window decor");
                }
            }
            if ((features & 32) == 0) {
                return;
            }
            if (circularProgressBar != null) {
                circularProgressBar.setVisibility(8);
            } else {
                Log.e(TAG, "Circular progress bar not located in current window decor");
            }
        } else if (value == -3) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setIndeterminate(true);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
        } else if (value == -4) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setIndeterminate(false);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
        } else if (value >= 0 && value <= 10000) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setProgress(value + 0);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
            if (value < 10000) {
                showProgressBars(horizontalProgressBar, circularProgressBar);
            } else {
                hideProgressBars(horizontalProgressBar, circularProgressBar);
            }
        } else if (20000 <= value && value <= 30000) {
            if (horizontalProgressBar != null) {
                horizontalProgressBar.setSecondaryProgress(value - 20000);
            } else {
                Log.e(TAG, "Horizontal progress bar not located in current window decor");
            }
            showProgressBars(horizontalProgressBar, circularProgressBar);
        }
    }

    private void showProgressBars(ProgressBar horizontalProgressBar, ProgressBar spinnyProgressBar) {
        int features = getLocalFeatures();
        if (!((features & 32) == 0 || spinnyProgressBar == null || spinnyProgressBar.getVisibility() != 4)) {
            spinnyProgressBar.setVisibility(0);
        }
        if ((features & 4) != 0 && horizontalProgressBar != null && horizontalProgressBar.getProgress() < 10000) {
            horizontalProgressBar.setVisibility(0);
        }
    }

    private void hideProgressBars(ProgressBar horizontalProgressBar, ProgressBar spinnyProgressBar) {
        int features = getLocalFeatures();
        Animation anim = AnimationUtils.loadAnimation(getContext(), 17432577);
        anim.setDuration(1000);
        if (!((features & 32) == 0 || spinnyProgressBar == null || spinnyProgressBar.getVisibility() != 0)) {
            spinnyProgressBar.startAnimation(anim);
            spinnyProgressBar.setVisibility(4);
        }
        if ((features & 4) != 0 && horizontalProgressBar != null && horizontalProgressBar.getVisibility() == 0) {
            horizontalProgressBar.startAnimation(anim);
            horizontalProgressBar.setVisibility(4);
        }
    }

    @Override // android.view.Window
    public void setIcon(int resId) {
        this.mIconRes = resId;
        this.mResourcesSetFlags |= 1;
        this.mResourcesSetFlags &= -5;
        DecorContentParent decorContentParent = this.mDecorContentParent;
        if (decorContentParent != null) {
            decorContentParent.setIcon(resId);
        }
    }

    @Override // android.view.Window
    public void setDefaultIcon(int resId) {
        if ((this.mResourcesSetFlags & 1) == 0) {
            this.mIconRes = resId;
            DecorContentParent decorContentParent = this.mDecorContentParent;
            if (decorContentParent == null) {
                return;
            }
            if (decorContentParent.hasIcon() && (this.mResourcesSetFlags & 4) == 0) {
                return;
            }
            if (resId != 0) {
                this.mDecorContentParent.setIcon(resId);
                this.mResourcesSetFlags &= -5;
                return;
            }
            this.mDecorContentParent.setIcon(getContext().getPackageManager().getDefaultActivityIcon());
            this.mResourcesSetFlags |= 4;
        }
    }

    @Override // android.view.Window
    public void setLogo(int resId) {
        this.mLogoRes = resId;
        this.mResourcesSetFlags |= 2;
        DecorContentParent decorContentParent = this.mDecorContentParent;
        if (decorContentParent != null) {
            decorContentParent.setLogo(resId);
        }
    }

    @Override // android.view.Window
    public void setDefaultLogo(int resId) {
        if ((this.mResourcesSetFlags & 2) == 0) {
            this.mLogoRes = resId;
            DecorContentParent decorContentParent = this.mDecorContentParent;
            if (decorContentParent != null && !decorContentParent.hasLogo()) {
                this.mDecorContentParent.setLogo(resId);
            }
        }
    }

    @Override // android.view.Window
    public void setLocalFocus(boolean hasFocus, boolean inTouchMode) {
        getViewRootImpl().windowFocusChanged(hasFocus, inTouchMode);
    }

    @Override // android.view.Window
    public void injectInputEvent(InputEvent event) {
        getViewRootImpl().dispatchInputEvent(event);
    }

    private ViewRootImpl getViewRootImpl() {
        ViewRootImpl viewRootImpl;
        DecorView decorView = this.mDecor;
        if (decorView != null && (viewRootImpl = decorView.getViewRootImpl()) != null) {
            return viewRootImpl;
        }
        throw new IllegalStateException("view not added");
    }

    @Override // android.view.Window
    public void takeKeyEvents(boolean get) {
        this.mDecor.setFocusable(get);
    }

    @Override // android.view.Window
    public boolean superDispatchKeyEvent(KeyEvent event) {
        return this.mDecor.superDispatchKeyEvent(event);
    }

    @Override // android.view.Window
    public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
        return this.mDecor.superDispatchKeyShortcutEvent(event);
    }

    @Override // android.view.Window
    public boolean superDispatchTouchEvent(MotionEvent event) {
        return this.mDecor.superDispatchTouchEvent(event);
    }

    @Override // android.view.Window
    public boolean superDispatchTrackballEvent(MotionEvent event) {
        return this.mDecor.superDispatchTrackballEvent(event);
    }

    @Override // android.view.Window
    public boolean superDispatchGenericMotionEvent(MotionEvent event) {
        return this.mDecor.superDispatchGenericMotionEvent(event);
    }

    /* access modifiers changed from: protected */
    public boolean onKeyDown(int featureId, int keyCode, KeyEvent event) {
        DecorView decorView = this.mDecor;
        KeyEvent.DispatcherState dispatcher = decorView != null ? decorView.getKeyDispatcherState() : null;
        int i = 0;
        if (keyCode != 4) {
            if (keyCode != 79) {
                if (keyCode == 82) {
                    if (featureId >= 0) {
                        i = featureId;
                    }
                    onKeyDownPanel(i, event);
                    return true;
                } else if (keyCode != 130) {
                    if (keyCode == 164 || keyCode == 24 || keyCode == 25) {
                        if (this.mMediaController != null) {
                            getMediaSessionManager().dispatchVolumeKeyEventAsSystemService(this.mMediaController.getSessionToken(), event);
                        } else {
                            getMediaSessionManager().dispatchVolumeKeyEventAsSystemService(event, this.mVolumeControlStreamType);
                        }
                        if (IS_SIDE_PROP) {
                            sendEvent();
                        }
                        return true;
                    } else if (!(keyCode == 126 || keyCode == 127)) {
                        switch (keyCode) {
                        }
                    }
                }
            }
            return this.mMediaController != null && getMediaSessionManager().dispatchMediaKeyEventAsSystemService(this.mMediaController.getSessionToken(), event);
        } else if (event.getRepeatCount() <= 0 && featureId >= 0) {
            if (dispatcher != null) {
                dispatcher.startTracking(event, this);
            }
            return true;
        }
        return false;
    }

    private void sendEvent() {
        String packageName = getContext().getPackageName();
        if (!this.mPackageName.equals(packageName)) {
            Bundle data = new Bundle();
            data.putString("package", packageName);
            data.putString(HwFrameworkMonitor.KEY_RECEIVE_TIME, Long.toString(System.currentTimeMillis()));
            HwFrameworkMonitor monitor = HwFrameworkFactory.getHwFrameworkMonitor();
            if (monitor != null) {
                monitor.monitor(907400028, data);
                this.mPackageName = packageName;
                Log.i(TAG, " this pacakage: " + packageName + " time : " + System.currentTimeMillis());
            }
        }
    }

    private KeyguardManager getKeyguardManager() {
        if (this.mKeyguardManager == null) {
            this.mKeyguardManager = (KeyguardManager) getContext().getSystemService(Context.KEYGUARD_SERVICE);
        }
        return this.mKeyguardManager;
    }

    /* access modifiers changed from: package-private */
    public AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
        }
        return this.mAudioManager;
    }

    private MediaSessionManager getMediaSessionManager() {
        if (this.mMediaSessionManager == null) {
            this.mMediaSessionManager = (MediaSessionManager) getContext().getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        return this.mMediaSessionManager;
    }

    /* access modifiers changed from: protected */
    public boolean onKeyUp(int featureId, int keyCode, KeyEvent event) {
        PanelFeatureState st;
        DecorView decorView = this.mDecor;
        KeyEvent.DispatcherState dispatcher = decorView != null ? decorView.getKeyDispatcherState() : null;
        if (dispatcher != null) {
            dispatcher.handleUpEvent(event);
        }
        int i = 0;
        if (keyCode != 4) {
            if (keyCode != 79) {
                if (keyCode == 82) {
                    if (featureId >= 0) {
                        i = featureId;
                    }
                    onKeyUpPanel(i, event);
                    return true;
                } else if (keyCode != 130) {
                    if (keyCode == 164) {
                        getMediaSessionManager().dispatchVolumeKeyEventAsSystemService(event, Integer.MIN_VALUE);
                        return true;
                    } else if (keyCode == 171) {
                        if (this.mSupportsPictureInPicture && !event.isCanceled()) {
                            getWindowControllerCallback().enterPictureInPictureModeIfPossible();
                        }
                        return true;
                    } else if (keyCode == 24 || keyCode == 25) {
                        if (this.mMediaController != null) {
                            getMediaSessionManager().dispatchVolumeKeyEventAsSystemService(this.mMediaController.getSessionToken(), event);
                        } else {
                            getMediaSessionManager().dispatchVolumeKeyEventAsSystemService(event, this.mVolumeControlStreamType);
                        }
                        return true;
                    } else if (!(keyCode == 126 || keyCode == 127)) {
                        switch (keyCode) {
                            case 84:
                                if (!isNotInstantAppAndKeyguardRestricted() && (getContext().getResources().getConfiguration().uiMode & 15) != 6) {
                                    if (event.isTracking() && !event.isCanceled()) {
                                        launchDefaultSearch(event);
                                    }
                                    return true;
                                }
                        }
                    }
                }
            }
            return this.mMediaController != null && getMediaSessionManager().dispatchMediaKeyEventAsSystemService(this.mMediaController.getSessionToken(), event);
        } else if (featureId >= 0 && event.isTracking() && !event.isCanceled()) {
            if (featureId != 0 || (st = getPanelState(featureId, false)) == null || !st.isInExpandedMode) {
                closePanel(featureId);
                return true;
            }
            reopenMenu(true);
            return true;
        }
        return false;
    }

    private boolean isNotInstantAppAndKeyguardRestricted() {
        return !getContext().getPackageManager().isInstantApp() && getKeyguardManager().inKeyguardRestrictedInputMode();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.Window
    public void onActive() {
    }

    @Override // android.view.Window
    public final View getDecorView() {
        if (this.mDecor == null || this.mForceDecorInstall) {
            installDecor();
        }
        return this.mDecor;
    }

    @Override // android.view.Window
    public final View peekDecorView() {
        return this.mDecor;
    }

    /* access modifiers changed from: package-private */
    public void onViewRootImplSet(ViewRootImpl viewRoot) {
        viewRoot.setActivityConfigCallback(this.mActivityConfigCallback);
    }

    @Override // android.view.Window
    public Bundle saveHierarchyState() {
        Bundle outState = new Bundle();
        if (this.mContentParent == null) {
            return outState;
        }
        SparseArray<Parcelable> states = new SparseArray<>();
        this.mContentParent.saveHierarchyState(states);
        outState.putSparseParcelableArray(VIEWS_TAG, states);
        View focusedView = this.mContentParent.findFocus();
        if (!(focusedView == null || focusedView.getId() == -1)) {
            outState.putInt(FOCUSED_ID_TAG, focusedView.getId());
        }
        SparseArray<Parcelable> panelStates = new SparseArray<>();
        savePanelState(panelStates);
        if (panelStates.size() > 0) {
            outState.putSparseParcelableArray(PANELS_TAG, panelStates);
        }
        if (this.mDecorContentParent != null) {
            SparseArray<Parcelable> actionBarStates = new SparseArray<>();
            this.mDecorContentParent.saveToolbarHierarchyState(actionBarStates);
            outState.putSparseParcelableArray(ACTION_BAR_TAG, actionBarStates);
        }
        return outState;
    }

    @Override // android.view.Window
    public void restoreHierarchyState(Bundle savedInstanceState) {
        if (this.mContentParent != null) {
            try {
                SparseArray<Parcelable> savedStates = savedInstanceState.getSparseParcelableArray(VIEWS_TAG);
                if (savedStates != null) {
                    this.mContentParent.restoreHierarchyState(savedStates);
                }
                int focusedViewId = savedInstanceState.getInt(FOCUSED_ID_TAG, -1);
                if (focusedViewId != -1) {
                    View needsFocus = this.mContentParent.findViewById(focusedViewId);
                    if (needsFocus != null) {
                        needsFocus.requestFocus();
                    } else {
                        Log.w(TAG, "Previously focused view reported id " + focusedViewId + " during save, but can't be found during restore.");
                    }
                }
                SparseArray<Parcelable> panelStates = savedInstanceState.getSparseParcelableArray(PANELS_TAG);
                if (panelStates != null) {
                    restorePanelState(panelStates);
                }
                if (this.mDecorContentParent != null) {
                    SparseArray<Parcelable> actionBarStates = savedInstanceState.getSparseParcelableArray(ACTION_BAR_TAG);
                    if (actionBarStates != null) {
                        doPendingInvalidatePanelMenu();
                        this.mDecorContentParent.restoreToolbarHierarchyState(actionBarStates);
                        return;
                    }
                    Log.w(TAG, "Missing saved instance states for action bar views! State will not be restored.");
                }
            } catch (BadParcelableException e) {
                Log.e(TAG, "restoreHierarchyState getSparseParcelableArray catch BadParcelableException" + e.toString());
            }
        }
    }

    private void savePanelState(SparseArray<Parcelable> icicles) {
        PanelFeatureState[] panels = this.mPanels;
        if (panels != null) {
            for (int curFeatureId = panels.length - 1; curFeatureId >= 0; curFeatureId--) {
                if (panels[curFeatureId] != null) {
                    icicles.put(curFeatureId, panels[curFeatureId].onSaveInstanceState());
                }
            }
        }
    }

    private void restorePanelState(SparseArray<Parcelable> icicles) {
        for (int i = icicles.size() - 1; i >= 0; i--) {
            int curFeatureId = icicles.keyAt(i);
            PanelFeatureState st = getPanelState(curFeatureId, false);
            if (st != null) {
                st.onRestoreInstanceState(icicles.get(curFeatureId));
                invalidatePanelMenu(curFeatureId);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void openPanelsAfterRestore() {
        PanelFeatureState[] panels = this.mPanels;
        if (panels != null) {
            for (int i = panels.length - 1; i >= 0; i--) {
                PanelFeatureState st = panels[i];
                if (st != null) {
                    st.applyFrozenState();
                    if (!st.isOpen && st.wasLastOpen) {
                        st.isInExpandedMode = st.wasLastExpanded;
                        openPanel(st, (KeyEvent) null);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class PanelMenuPresenterCallback implements MenuPresenter.Callback {
        private PanelMenuPresenterCallback() {
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            Menu parentMenu = menu.getRootMenu();
            boolean isSubMenu = parentMenu != menu;
            PanelFeatureState panel = PhoneWindow.this.findMenuPanel(isSubMenu ? parentMenu : menu);
            if (panel == null) {
                return;
            }
            if (isSubMenu) {
                PhoneWindow.this.callOnPanelClosed(panel.featureId, panel, parentMenu);
                PhoneWindow.this.closePanel(panel, true);
                return;
            }
            PhoneWindow.this.closePanel(panel, allMenusAreClosing);
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            Window.Callback cb;
            if (subMenu != null || !PhoneWindow.this.hasFeature(8) || (cb = PhoneWindow.this.getCallback()) == null || PhoneWindow.this.isDestroyed()) {
                return true;
            }
            cb.onMenuOpened(8, subMenu);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public final class ActionMenuPresenterCallback implements MenuPresenter.Callback {
        private ActionMenuPresenterCallback() {
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            Window.Callback cb = PhoneWindow.this.getCallback();
            if (cb == null) {
                return false;
            }
            cb.onMenuOpened(8, subMenu);
            return true;
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            PhoneWindow.this.checkCloseActionMenu(menu);
        }
    }

    /* access modifiers changed from: protected */
    public DecorView generateDecor(int featureId) {
        Context context;
        if (this.mUseDecorContext) {
            Context applicationContext = getContext().getApplicationContext();
            if (applicationContext == null) {
                context = getContext();
            } else {
                context = new DecorContext(applicationContext, getContext());
                int i = this.mTheme;
                if (i != -1) {
                    context.setTheme(i);
                }
            }
        } else {
            context = getContext();
        }
        return new DecorView(context, featureId, this, getAttributes());
    }

    /* JADX INFO: Multiple debug info for r3v21 int: [D('res' android.util.TypedValue), D('layoutResource' int)] */
    /* access modifiers changed from: protected */
    public ViewGroup generateLayout(DecorView decor) {
        int i;
        int layoutResource;
        Drawable frame;
        ProgressBar progress;
        int layoutResource2;
        TypedArray a = getWindowStyle();
        initSplitMode();
        this.mIsTransparent = a.getBoolean(5, false);
        initTranslucentImmersion();
        this.mIsFloating = a.getBoolean(4, false);
        int flagsToUpdate = (~getForcedWindowFlags()) & 65792;
        if (this.mIsFloating) {
            setLayout(-2, -2);
            setFlags(0, flagsToUpdate);
        } else {
            setFlags(65792, flagsToUpdate);
        }
        if (a.getBoolean(3, false)) {
            requestFeature(1);
        } else if (a.getBoolean(15, false)) {
            requestFeature(8);
        }
        if (a.getBoolean(17, false)) {
            requestFeature(9);
        }
        if (a.getBoolean(16, false)) {
            requestFeature(10);
        }
        if (a.getBoolean(25, false)) {
            requestFeature(11);
        }
        if (a.getBoolean(9, false)) {
            setFlags(1024, (~getForcedWindowFlags()) & 1024);
        }
        if (a.getBoolean(23, false)) {
            setFlags(67108864, (~getForcedWindowFlags()) & 67108864);
        }
        if (a.getBoolean(24, false)) {
            setFlags(134217728, (~getForcedWindowFlags()) & 134217728);
        }
        if (a.getBoolean(22, false)) {
            setFlags(33554432, 33554432 & (~getForcedWindowFlags()));
        }
        if (a.getBoolean(14, false)) {
            setFlags(1048576, 1048576 & (~getForcedWindowFlags()));
        }
        if (a.getBoolean(18, getContext().getApplicationInfo().targetSdkVersion >= 11)) {
            setFlags(8388608, 8388608 & (~getForcedWindowFlags()));
        }
        a.getValue(19, this.mMinWidthMajor);
        a.getValue(20, this.mMinWidthMinor);
        if (a.hasValue(57)) {
            if (this.mFixedWidthMajor == null) {
                this.mFixedWidthMajor = new TypedValue();
            }
            a.getValue(57, this.mFixedWidthMajor);
        }
        if (a.hasValue(58)) {
            if (this.mFixedWidthMinor == null) {
                this.mFixedWidthMinor = new TypedValue();
            }
            a.getValue(58, this.mFixedWidthMinor);
        }
        if (a.hasValue(55)) {
            if (this.mFixedHeightMajor == null) {
                this.mFixedHeightMajor = new TypedValue();
            }
            a.getValue(55, this.mFixedHeightMajor);
        }
        if (a.hasValue(56)) {
            if (this.mFixedHeightMinor == null) {
                this.mFixedHeightMinor = new TypedValue();
            }
            a.getValue(56, this.mFixedHeightMinor);
        }
        if (a.getBoolean(26, false)) {
            requestFeature(12);
        }
        if (a.getBoolean(45, false)) {
            requestFeature(13);
        }
        this.mIsTranslucent = a.getBoolean(5, false);
        Context context = getContext();
        int targetSdk = context.getApplicationInfo().targetSdkVersion;
        boolean targetPreHoneycomb = targetSdk < 11;
        boolean targetPreIcs = targetSdk < 14;
        boolean targetPreL = targetSdk < 21;
        boolean targetPreQ = targetSdk < 29;
        boolean targetHcNeedsOptions = context.getResources().getBoolean(R.bool.target_honeycomb_needs_options_menu);
        boolean noActionBar = !hasFeature(8) || hasFeature(1);
        if (targetPreHoneycomb || (targetPreIcs && targetHcNeedsOptions && noActionBar)) {
            setNeedsMenuKey(1);
        } else {
            setNeedsMenuKey(2);
        }
        if (!this.mForcedStatusBarColor) {
            this.mStatusBarColor = a.getColor(35, -16777216);
        }
        if (!this.mForcedNavigationBarColor) {
            calculateNavigationBarColor(a);
            this.mNavigationBarDividerColor = a.getColor(50, 0);
        }
        if (!targetPreQ) {
            this.mEnsureStatusBarContrastWhenTransparent = a.getBoolean(52, false);
            this.mEnsureNavigationBarContrastWhenTransparent = a.getBoolean(53, true);
        }
        WindowManager.LayoutParams params = getAttributes();
        if (!this.mIsFloating) {
            if (!targetPreL && a.getBoolean(34, false)) {
                setFlags(Integer.MIN_VALUE, (~getForcedWindowFlags()) & Integer.MIN_VALUE);
            }
            if (this.mDecor.mForceWindowDrawsBarBackgrounds) {
                params.privateFlags |= 131072;
            }
        }
        if (a.getBoolean(46, false)) {
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | 8192);
        }
        if (a.getBoolean(49, false)) {
            decor.setSystemUiVisibility(decor.getSystemUiVisibility() | 16);
        }
        if (a.hasValue(51)) {
            int mode = a.getInt(51, -1);
            if (mode < 0 || mode > 2) {
                throw new UnsupportedOperationException("Unknown windowLayoutInDisplayCutoutMode: " + a.getString(51));
            }
            params.layoutInDisplayCutoutMode = mode;
        }
        if ((this.mAlwaysReadCloseOnTouchAttr || getContext().getApplicationInfo().targetSdkVersion >= 11) && a.getBoolean(21, false)) {
            setCloseOnTouchOutsideIfNotSet(true);
        }
        if (!hasSoftInputMode()) {
            params.softInputMode = a.getInt(13, params.softInputMode);
        }
        updateLayoutParamsColor();
        if (a.getBoolean(11, this.mIsFloating)) {
            if ((getForcedWindowFlags() & 2) == 0) {
                params.flags |= 2;
            }
            if (haveDimAmount()) {
                i = 0;
            } else if (!isSplitMode() || this.mIsFloating) {
                i = 0;
                params.dimAmount = a.getFloat(0, 0.5f);
            } else {
                params.dimAmount = 0.0f;
                i = 0;
            }
        } else {
            i = 0;
        }
        if (params.windowAnimations == 0) {
            params.windowAnimations = a.getResourceId(8, i);
        }
        if (getContainer() == null) {
            if (this.mBackgroundDrawable == null) {
                if (this.mFrameResource == 0) {
                    this.mFrameResource = a.getResourceId(2, 0);
                }
                if (a.hasValue(1)) {
                    this.mBackgroundDrawable = a.getDrawable(1);
                }
            }
            if (a.hasValue(47)) {
                this.mBackgroundFallbackDrawable = a.getDrawable(47);
            }
            if (this.mLoadElevation) {
                this.mElevation = a.getDimension(38, 0.0f);
            }
            this.mClipToOutline = a.getBoolean(39, false);
            this.mTextColor = a.getColor(7, 0);
        }
        int features = getLocalFeatures();
        if ((features & 2048) != 0) {
            layoutResource = R.layout.screen_swipe_dismiss;
            setCloseOnSwipeEnabled(true);
        } else if ((features & 24) != 0) {
            if (this.mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.dialogTitleIconsDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = 17367278;
            }
            removeFeature(8);
        } else if ((features & 36) != 0 && (features & 256) == 0) {
            layoutResource = R.layout.screen_progress;
        } else if ((features & 128) != 0) {
            if (this.mIsFloating) {
                TypedValue res2 = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.dialogCustomTitleDecorLayout, res2, true);
                layoutResource2 = res2.resourceId;
            } else {
                layoutResource2 = 17367272;
            }
            removeFeature(8);
        } else if ((features & 2) == 0) {
            if (this.mIsFloating) {
                TypedValue res3 = new TypedValue();
                getContext().getTheme().resolveAttribute(R.attr.dialogTitleDecorLayout, res3, true);
                layoutResource = res3.resourceId;
            } else if ((features & 256) != 0) {
                layoutResource = a.getResourceId(54, R.layout.screen_action_bar);
            } else {
                layoutResource = R.layout.screen_title;
            }
        } else if ((features & 1024) != 0) {
            layoutResource = R.layout.screen_simple_overlay_action_mode;
        } else {
            layoutResource = R.layout.screen_simple;
        }
        this.mDecor.startChanging();
        this.mDecor.onResourcesLoaded(this.mLayoutInflater, layoutResource);
        isNeedHideForeground(this.mDecor);
        ViewGroup contentParent = (ViewGroup) findViewById(16908290);
        if (contentParent != null) {
            if (!((features & 32) == 0 || (progress = getCircularProgressBar(false)) == null)) {
                progress.setIndeterminate(true);
            }
            if ((features & 2048) != 0) {
                registerSwipeCallbacks(contentParent);
            }
            if (getContainer() == null) {
                this.mDecor.setWindowBackground(this.mBackgroundDrawable);
                if (this.mFrameResource != 0) {
                    frame = getContext().getDrawable(this.mFrameResource);
                } else {
                    frame = null;
                }
                this.mDecor.setWindowFrame(frame);
                this.mDecor.setElevation(this.mElevation);
                this.mDecor.setClipToOutline(this.mClipToOutline);
                CharSequence charSequence = this.mTitle;
                if (charSequence != null) {
                    setTitle(charSequence);
                }
                if (this.mTitleColor == 0) {
                    this.mTitleColor = this.mTextColor;
                }
                setTitleColor(this.mTitleColor);
            }
            this.mDecor.finishChanging();
            return contentParent;
        }
        throw new RuntimeException("Window couldn't find content container view");
    }

    private void calculateNavigationBarColor(TypedArray a) {
        this.mNavigationBarColor = a.getColor(36, HwWidgetFactory.DEFAULT_PRIMARY_COLOR);
        if (this.mNavigationBarColor != NAVIGATION_BAR_DEFAULT_COLOR) {
            return;
        }
        if (!NAVIGATION_BAR_IS_BLACK_PROP) {
            this.mNavigationBarColor = HwWidgetFactory.DEFAULT_PRIMARY_COLOR;
            this.mSpecialSet = true;
            return;
        }
        this.mNavigationBarColor = -16777216;
    }

    /* access modifiers changed from: protected */
    public boolean isForcedNavigationBarColor() {
        return this.mForcedNavigationBarColor;
    }

    @Override // android.view.Window
    public void alwaysReadCloseOnTouchAttr() {
        this.mAlwaysReadCloseOnTouchAttr = true;
    }

    @Override // android.view.Window
    public void setSplitActionBarAlways(boolean bAlwaysSplit) {
        this.mAlwaysSplit = bAlwaysSplit;
    }

    private void installDecor() {
        Drawable drawable;
        this.mForceDecorInstall = false;
        DecorView decorView = this.mDecor;
        if (decorView == null) {
            this.mDecor = generateDecor(-1);
            this.mDecor.setDescendantFocusability(262144);
            this.mDecor.setIsRootNamespace(true);
            if (!this.mInvalidatePanelMenuPosted && this.mInvalidatePanelMenuFeatures != 0) {
                this.mDecor.postOnAnimation(this.mInvalidatePanelMenuRunnable);
            }
        } else {
            decorView.setWindow(this);
        }
        if (this.mContentParent == null) {
            this.mContentParent = generateLayout(this.mDecor);
            this.mDecor.makeOptionalFitsSystemWindows();
            DecorContentParent decorContentParent = (DecorContentParent) this.mDecor.findViewById(R.id.decor_content_parent);
            if (decorContentParent != null) {
                this.mDecorContentParent = decorContentParent;
                this.mDecorContentParent.setWindowCallback(getCallback());
                if (this.mDecorContentParent.getTitle() == null) {
                    this.mDecorContentParent.setWindowTitle(this.mTitle);
                }
                int localFeatures = getLocalFeatures();
                for (int i = 0; i < 13; i++) {
                    if (((1 << i) & localFeatures) != 0) {
                        this.mDecorContentParent.initFeature(i);
                    }
                }
                if (this.mAlwaysSplit) {
                    this.mDecorContentParent.setSplitActionBarAlways(true);
                }
                this.mDecorContentParent.setUiOptions(this.mUiOptions);
                if ((this.mResourcesSetFlags & 1) != 0 || (this.mIconRes != 0 && !this.mDecorContentParent.hasIcon())) {
                    this.mDecorContentParent.setIcon(this.mIconRes);
                } else if ((this.mResourcesSetFlags & 1) == 0 && this.mIconRes == 0 && !this.mDecorContentParent.hasIcon()) {
                    this.mDecorContentParent.setIcon(getContext().getPackageManager().getDefaultActivityIcon());
                    this.mResourcesSetFlags |= 4;
                }
                if ((this.mResourcesSetFlags & 2) != 0 || (this.mLogoRes != 0 && !this.mDecorContentParent.hasLogo())) {
                    this.mDecorContentParent.setLogo(this.mLogoRes);
                }
                PanelFeatureState st = getPanelState(0, false);
                if (!isDestroyed() && ((st == null || st.menu == null) && !this.mIsStartingWindow)) {
                    invalidatePanelMenu(8);
                }
            } else {
                this.mTitleView = (TextView) findViewById(16908310);
                if (this.mTitleView != null) {
                    if ((getLocalFeatures() & 2) != 0) {
                        View titleContainer = findViewById(R.id.title_container);
                        if (titleContainer != null) {
                            titleContainer.setVisibility(8);
                        } else {
                            this.mTitleView.setVisibility(8);
                        }
                        this.mContentParent.setForeground(null);
                    } else {
                        this.mTitleView.setText(this.mTitle);
                    }
                }
            }
            if (this.mDecor.getBackground() == null && (drawable = this.mBackgroundFallbackDrawable) != null) {
                this.mDecor.setBackgroundFallback(drawable);
            }
            if (hasFeature(13)) {
                if (this.mTransitionManager == null) {
                    int transitionRes = getWindowStyle().getResourceId(27, 0);
                    if (transitionRes != 0) {
                        this.mTransitionManager = TransitionInflater.from(getContext()).inflateTransitionManager(transitionRes, this.mContentParent);
                    } else {
                        this.mTransitionManager = new TransitionManager();
                    }
                }
                this.mEnterTransition = getTransition(this.mEnterTransition, null, 28);
                this.mReturnTransition = getTransition(this.mReturnTransition, USE_DEFAULT_TRANSITION, 40);
                this.mExitTransition = getTransition(this.mExitTransition, null, 29);
                this.mReenterTransition = getTransition(this.mReenterTransition, USE_DEFAULT_TRANSITION, 41);
                this.mSharedElementEnterTransition = getTransition(this.mSharedElementEnterTransition, null, 30);
                this.mSharedElementReturnTransition = getTransition(this.mSharedElementReturnTransition, USE_DEFAULT_TRANSITION, 42);
                this.mSharedElementExitTransition = getTransition(this.mSharedElementExitTransition, null, 31);
                this.mSharedElementReenterTransition = getTransition(this.mSharedElementReenterTransition, USE_DEFAULT_TRANSITION, 43);
                if (this.mAllowEnterTransitionOverlap == null) {
                    this.mAllowEnterTransitionOverlap = Boolean.valueOf(getWindowStyle().getBoolean(33, true));
                }
                if (this.mAllowReturnTransitionOverlap == null) {
                    this.mAllowReturnTransitionOverlap = Boolean.valueOf(getWindowStyle().getBoolean(32, true));
                }
                if (this.mBackgroundFadeDurationMillis < 0) {
                    this.mBackgroundFadeDurationMillis = (long) getWindowStyle().getInteger(37, 300);
                }
                if (this.mSharedElementsUseOverlay == null) {
                    this.mSharedElementsUseOverlay = Boolean.valueOf(getWindowStyle().getBoolean(44, true));
                }
            }
        }
    }

    private Transition getTransition(Transition currentValue, Transition defaultValue, int id) {
        if (currentValue != defaultValue) {
            return currentValue;
        }
        int transitionId = getWindowStyle().getResourceId(id, -1);
        if (transitionId == -1 || transitionId == 17760256) {
            return defaultValue;
        }
        Transition transition = TransitionInflater.from(getContext()).inflateTransition(transitionId);
        if (!(transition instanceof TransitionSet) || ((TransitionSet) transition).getTransitionCount() != 0) {
            return transition;
        }
        return null;
    }

    private Drawable loadImageURI(Uri uri) {
        try {
            return Drawable.createFromStream(getContext().getContentResolver().openInputStream(uri), null);
        } catch (Exception e) {
            Log.w(TAG, "Unable to open content: " + uri);
            return null;
        }
    }

    /* JADX INFO: Multiple debug info for r0v3 com.android.internal.policy.PhoneWindow$DrawableFeatureState: [D('nar' com.android.internal.policy.PhoneWindow$DrawableFeatureState[]), D('st' com.android.internal.policy.PhoneWindow$DrawableFeatureState)] */
    private DrawableFeatureState getDrawableState(int featureId, boolean required) {
        if ((getFeatures() & (1 << featureId)) != 0) {
            DrawableFeatureState[] drawableFeatureStateArr = this.mDrawables;
            DrawableFeatureState[] ar = drawableFeatureStateArr;
            if (drawableFeatureStateArr == null || ar.length <= featureId) {
                DrawableFeatureState[] nar = new DrawableFeatureState[(featureId + 1)];
                if (ar != null) {
                    System.arraycopy(ar, 0, nar, 0, ar.length);
                }
                ar = nar;
                this.mDrawables = nar;
            }
            DrawableFeatureState st = ar[featureId];
            if (st != null) {
                return st;
            }
            DrawableFeatureState st2 = new DrawableFeatureState(featureId);
            ar[featureId] = st2;
            return st2;
        } else if (!required) {
            return null;
        } else {
            throw new RuntimeException("The feature has not been requested");
        }
    }

    /* access modifiers changed from: package-private */
    public PanelFeatureState getPanelState(int featureId, boolean required) {
        return getPanelState(featureId, required, null);
    }

    /* JADX INFO: Multiple debug info for r0v3 com.android.internal.policy.PhoneWindow$PanelFeatureState: [D('nar' com.android.internal.policy.PhoneWindow$PanelFeatureState[]), D('st' com.android.internal.policy.PhoneWindow$PanelFeatureState)] */
    private PanelFeatureState getPanelState(int featureId, boolean required, PanelFeatureState convertPanelState) {
        PanelFeatureState st;
        if ((getFeatures() & (1 << featureId)) != 0) {
            PanelFeatureState[] panelFeatureStateArr = this.mPanels;
            PanelFeatureState[] ar = panelFeatureStateArr;
            if (panelFeatureStateArr == null || ar.length <= featureId) {
                PanelFeatureState[] nar = new PanelFeatureState[(featureId + 1)];
                if (ar != null) {
                    System.arraycopy(ar, 0, nar, 0, ar.length);
                }
                ar = nar;
                this.mPanels = nar;
            }
            PanelFeatureState st2 = ar[featureId];
            if (st2 != null) {
                return st2;
            }
            if (convertPanelState != null) {
                st = convertPanelState;
            } else {
                st = new PanelFeatureState(featureId);
            }
            ar[featureId] = st;
            return st;
        } else if (!required) {
            return null;
        } else {
            throw new RuntimeException("The feature has not been requested");
        }
    }

    @Override // android.view.Window
    public final void setChildDrawable(int featureId, Drawable drawable) {
        DrawableFeatureState st = getDrawableState(featureId, true);
        st.child = drawable;
        updateDrawable(featureId, st, false);
    }

    @Override // android.view.Window
    public final void setChildInt(int featureId, int value) {
        updateInt(featureId, value, false);
    }

    @Override // android.view.Window
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        PanelFeatureState st = getPanelState(0, false);
        if (st == null || st.menu == null || !st.menu.isShortcutKey(keyCode, event)) {
            return false;
        }
        return true;
    }

    private void updateDrawable(int featureId, DrawableFeatureState st, boolean fromResume) {
        if (this.mContentParent != null) {
            int featureMask = 1 << featureId;
            if ((getFeatures() & featureMask) != 0 || fromResume) {
                Drawable drawable = null;
                if (st != null) {
                    drawable = st.child;
                    if (drawable == null) {
                        drawable = st.local;
                    }
                    if (drawable == null) {
                        drawable = st.def;
                    }
                }
                if ((getLocalFeatures() & featureMask) == 0) {
                    if (getContainer() == null) {
                        return;
                    }
                    if (isActive() || fromResume) {
                        getContainer().setChildDrawable(featureId, drawable);
                    }
                } else if (st == null) {
                } else {
                    if (st.cur != drawable || st.curAlpha != st.alpha) {
                        st.cur = drawable;
                        st.curAlpha = st.alpha;
                        onDrawableChanged(featureId, drawable, st.alpha);
                    }
                }
            }
        }
    }

    private void updateInt(int featureId, int value, boolean fromResume) {
        if (this.mContentParent != null) {
            int featureMask = 1 << featureId;
            if ((getFeatures() & featureMask) == 0 && !fromResume) {
                return;
            }
            if ((getLocalFeatures() & featureMask) != 0) {
                onIntChanged(featureId, value);
            } else if (getContainer() != null) {
                getContainer().setChildInt(featureId, value);
            }
        }
    }

    private ImageView getLeftIconView() {
        ImageView imageView = this.mLeftIconView;
        if (imageView != null) {
            return imageView;
        }
        if (this.mContentParent == null) {
            installDecor();
        }
        ImageView imageView2 = (ImageView) findViewById(R.id.left_icon);
        this.mLeftIconView = imageView2;
        return imageView2;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.Window
    public void dispatchWindowAttributesChanged(WindowManager.LayoutParams attrs) {
        super.dispatchWindowAttributesChanged(attrs);
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateColorViews(null, true);
        }
    }

    private ProgressBar getCircularProgressBar(boolean shouldInstallDecor) {
        ProgressBar progressBar = this.mCircularProgressBar;
        if (progressBar != null) {
            return progressBar;
        }
        if (this.mContentParent == null && shouldInstallDecor) {
            installDecor();
        }
        this.mCircularProgressBar = (ProgressBar) findViewById(R.id.progress_circular);
        ProgressBar progressBar2 = this.mCircularProgressBar;
        if (progressBar2 != null) {
            progressBar2.setVisibility(4);
        }
        return this.mCircularProgressBar;
    }

    private ProgressBar getHorizontalProgressBar(boolean shouldInstallDecor) {
        ProgressBar progressBar = this.mHorizontalProgressBar;
        if (progressBar != null) {
            return progressBar;
        }
        if (this.mContentParent == null && shouldInstallDecor) {
            installDecor();
        }
        this.mHorizontalProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);
        ProgressBar progressBar2 = this.mHorizontalProgressBar;
        if (progressBar2 != null) {
            progressBar2.setVisibility(4);
        }
        return this.mHorizontalProgressBar;
    }

    private ImageView getRightIconView() {
        ImageView imageView = this.mRightIconView;
        if (imageView != null) {
            return imageView;
        }
        if (this.mContentParent == null) {
            installDecor();
        }
        ImageView imageView2 = (ImageView) findViewById(R.id.right_icon);
        this.mRightIconView = imageView2;
        return imageView2;
    }

    private void registerSwipeCallbacks(ViewGroup contentParent) {
        if (!(contentParent instanceof SwipeDismissLayout)) {
            Log.w(TAG, "contentParent is not a SwipeDismissLayout: " + contentParent);
            return;
        }
        SwipeDismissLayout swipeDismiss = (SwipeDismissLayout) contentParent;
        swipeDismiss.setOnDismissedListener(new SwipeDismissLayout.OnDismissedListener() {
            /* class com.android.internal.policy.PhoneWindow.AnonymousClass2 */

            @Override // com.android.internal.widget.SwipeDismissLayout.OnDismissedListener
            public void onDismissed(SwipeDismissLayout layout) {
                PhoneWindow.this.dispatchOnWindowSwipeDismissed();
                PhoneWindow.this.dispatchOnWindowDismissed(false, true);
            }
        });
        swipeDismiss.setOnSwipeProgressChangedListener(new SwipeDismissLayout.OnSwipeProgressChangedListener() {
            /* class com.android.internal.policy.PhoneWindow.AnonymousClass3 */

            @Override // com.android.internal.widget.SwipeDismissLayout.OnSwipeProgressChangedListener
            public void onSwipeProgressChanged(SwipeDismissLayout layout, float alpha, float translate) {
                int flags;
                WindowManager.LayoutParams newParams = PhoneWindow.this.getAttributes();
                newParams.x = (int) translate;
                newParams.alpha = alpha;
                PhoneWindow.this.setAttributes(newParams);
                if (newParams.x == 0) {
                    flags = 1024;
                } else {
                    flags = 512;
                }
                PhoneWindow.this.setFlags(flags, 1536);
            }

            @Override // com.android.internal.widget.SwipeDismissLayout.OnSwipeProgressChangedListener
            public void onSwipeCancelled(SwipeDismissLayout layout) {
                WindowManager.LayoutParams newParams = PhoneWindow.this.getAttributes();
                if (newParams.x != 0 || newParams.alpha != 1.0f) {
                    newParams.x = 0;
                    newParams.alpha = 1.0f;
                    PhoneWindow.this.setAttributes(newParams);
                    PhoneWindow.this.setFlags(1024, 1536);
                }
            }
        });
    }

    @Override // android.view.Window
    public void setCloseOnSwipeEnabled(boolean closeOnSwipeEnabled) {
        if (hasFeature(11)) {
            ViewGroup viewGroup = this.mContentParent;
            if (viewGroup instanceof SwipeDismissLayout) {
                ((SwipeDismissLayout) viewGroup).setDismissable(closeOnSwipeEnabled);
            }
        }
        super.setCloseOnSwipeEnabled(closeOnSwipeEnabled);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void callOnPanelClosed(int featureId, PanelFeatureState panel, Menu menu) {
        Window.Callback cb = getCallback();
        if (cb != null) {
            if (menu == null) {
                if (panel == null && featureId >= 0) {
                    PanelFeatureState[] panelFeatureStateArr = this.mPanels;
                    if (featureId < panelFeatureStateArr.length) {
                        panel = panelFeatureStateArr[featureId];
                    }
                }
                if (panel != null) {
                    menu = panel.menu;
                }
            }
            if ((panel == null || panel.isOpen) && !isDestroyed()) {
                cb.onPanelClosed(featureId, menu);
            }
        }
    }

    private boolean isTvUserSetupComplete() {
        boolean z = false;
        boolean isTvSetupComplete = Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
        if (Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.TV_USER_SETUP_COMPLETE, 0) != 0) {
            z = true;
        }
        return isTvSetupComplete & z;
    }

    private boolean launchDefaultSearch(KeyEvent event) {
        boolean result;
        if (getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK) && !isTvUserSetupComplete()) {
            return false;
        }
        Window.Callback cb = getCallback();
        if (cb == null || isDestroyed()) {
            result = false;
        } else {
            sendCloseSystemWindows("search");
            int deviceId = event.getDeviceId();
            SearchEvent searchEvent = null;
            if (deviceId != 0) {
                searchEvent = new SearchEvent(InputDevice.getDevice(deviceId));
            }
            try {
                result = cb.onSearchRequested(searchEvent);
            } catch (AbstractMethodError e) {
                Log.e(TAG, "WindowCallback " + cb.getClass().getName() + " does not implement method onSearchRequested(SearchEvent); fa", e);
                result = cb.onSearchRequested();
            }
        }
        if (result || (getContext().getResources().getConfiguration().uiMode & 15) != 4) {
            return result;
        }
        Bundle args = new Bundle();
        args.putInt(Intent.EXTRA_ASSIST_INPUT_DEVICE_ID, event.getDeviceId());
        return ((SearchManager) getContext().getSystemService("search")).launchLegacyAssist(null, getContext().getUserId(), args);
    }

    @Override // android.view.Window
    public void setVolumeControlStream(int streamType) {
        this.mVolumeControlStreamType = streamType;
    }

    @Override // android.view.Window
    public int getVolumeControlStream() {
        return this.mVolumeControlStreamType;
    }

    @Override // android.view.Window
    public void setMediaController(MediaController controller) {
        this.mMediaController = controller;
    }

    @Override // android.view.Window
    public MediaController getMediaController() {
        return this.mMediaController;
    }

    @Override // android.view.Window
    public void setEnterTransition(Transition enterTransition) {
        this.mEnterTransition = enterTransition;
    }

    @Override // android.view.Window
    public void setReturnTransition(Transition transition) {
        this.mReturnTransition = transition;
    }

    @Override // android.view.Window
    public void setExitTransition(Transition exitTransition) {
        this.mExitTransition = exitTransition;
    }

    @Override // android.view.Window
    public void setReenterTransition(Transition transition) {
        this.mReenterTransition = transition;
    }

    @Override // android.view.Window
    public void setSharedElementEnterTransition(Transition sharedElementEnterTransition) {
        this.mSharedElementEnterTransition = sharedElementEnterTransition;
    }

    @Override // android.view.Window
    public void setSharedElementReturnTransition(Transition transition) {
        this.mSharedElementReturnTransition = transition;
    }

    @Override // android.view.Window
    public void setSharedElementExitTransition(Transition sharedElementExitTransition) {
        this.mSharedElementExitTransition = sharedElementExitTransition;
    }

    @Override // android.view.Window
    public void setSharedElementReenterTransition(Transition transition) {
        this.mSharedElementReenterTransition = transition;
    }

    @Override // android.view.Window
    public Transition getEnterTransition() {
        return this.mEnterTransition;
    }

    @Override // android.view.Window
    public Transition getReturnTransition() {
        Transition transition = this.mReturnTransition;
        return transition == USE_DEFAULT_TRANSITION ? getEnterTransition() : transition;
    }

    @Override // android.view.Window
    public Transition getExitTransition() {
        return this.mExitTransition;
    }

    @Override // android.view.Window
    public Transition getReenterTransition() {
        Transition transition = this.mReenterTransition;
        return transition == USE_DEFAULT_TRANSITION ? getExitTransition() : transition;
    }

    @Override // android.view.Window
    public Transition getSharedElementEnterTransition() {
        return this.mSharedElementEnterTransition;
    }

    @Override // android.view.Window
    public Transition getSharedElementReturnTransition() {
        Transition transition = this.mSharedElementReturnTransition;
        if (transition == USE_DEFAULT_TRANSITION) {
            return getSharedElementEnterTransition();
        }
        return transition;
    }

    @Override // android.view.Window
    public Transition getSharedElementExitTransition() {
        return this.mSharedElementExitTransition;
    }

    @Override // android.view.Window
    public Transition getSharedElementReenterTransition() {
        Transition transition = this.mSharedElementReenterTransition;
        if (transition == USE_DEFAULT_TRANSITION) {
            return getSharedElementExitTransition();
        }
        return transition;
    }

    @Override // android.view.Window
    public void setAllowEnterTransitionOverlap(boolean allow) {
        this.mAllowEnterTransitionOverlap = Boolean.valueOf(allow);
    }

    @Override // android.view.Window
    public boolean getAllowEnterTransitionOverlap() {
        Boolean bool = this.mAllowEnterTransitionOverlap;
        if (bool == null) {
            return true;
        }
        return bool.booleanValue();
    }

    @Override // android.view.Window
    public void setAllowReturnTransitionOverlap(boolean allowExitTransitionOverlap) {
        this.mAllowReturnTransitionOverlap = Boolean.valueOf(allowExitTransitionOverlap);
    }

    @Override // android.view.Window
    public boolean getAllowReturnTransitionOverlap() {
        Boolean bool = this.mAllowReturnTransitionOverlap;
        if (bool == null) {
            return true;
        }
        return bool.booleanValue();
    }

    @Override // android.view.Window
    public long getTransitionBackgroundFadeDuration() {
        long j = this.mBackgroundFadeDurationMillis;
        if (j < 0) {
            return 300;
        }
        return j;
    }

    @Override // android.view.Window
    public void setTransitionBackgroundFadeDuration(long fadeDurationMillis) {
        if (fadeDurationMillis >= 0) {
            this.mBackgroundFadeDurationMillis = fadeDurationMillis;
            return;
        }
        throw new IllegalArgumentException("negative durations are not allowed");
    }

    @Override // android.view.Window
    public void setSharedElementsUseOverlay(boolean sharedElementsUseOverlay) {
        this.mSharedElementsUseOverlay = Boolean.valueOf(sharedElementsUseOverlay);
    }

    @Override // android.view.Window
    public boolean getSharedElementsUseOverlay() {
        Boolean bool = this.mSharedElementsUseOverlay;
        if (bool == null) {
            return true;
        }
        return bool.booleanValue();
    }

    /* access modifiers changed from: private */
    public static final class DrawableFeatureState {
        int alpha = 255;
        Drawable child;
        Drawable cur;
        int curAlpha = 255;
        Drawable def;
        final int featureId;
        Drawable local;
        int resid;
        Uri uri;

        DrawableFeatureState(int _featureId) {
            this.featureId = _featureId;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class PanelFeatureState {
        int background;
        View createdPanelView;
        DecorView decorView;
        int featureId;
        Bundle frozenActionViewState;
        Bundle frozenMenuState;
        int fullBackground;
        int gravity;
        IconMenuPresenter iconMenuPresenter;
        boolean isCompact;
        boolean isHandled;
        boolean isInExpandedMode;
        boolean isOpen;
        boolean isPrepared;
        ListMenuPresenter listMenuPresenter;
        int listPresenterTheme;
        MenuBuilder menu;
        public boolean qwertyMode;
        boolean refreshDecorView = false;
        boolean refreshMenuContent;
        View shownPanelView;
        boolean wasLastExpanded;
        boolean wasLastOpen;
        int windowAnimations;
        int x;
        int y;

        PanelFeatureState(int featureId2) {
            this.featureId = featureId2;
        }

        public boolean isInListMode() {
            return this.isInExpandedMode || this.isCompact;
        }

        public boolean hasPanelItems() {
            View view = this.shownPanelView;
            if (view == null) {
                return false;
            }
            if (this.createdPanelView != null) {
                return true;
            }
            if (this.isCompact || this.isInExpandedMode) {
                if (this.listMenuPresenter.getAdapter().getCount() > 0) {
                    return true;
                }
                return false;
            } else if (((ViewGroup) view).getChildCount() > 0) {
                return true;
            } else {
                return false;
            }
        }

        public void clearMenuPresenters() {
            MenuBuilder menuBuilder = this.menu;
            if (menuBuilder != null) {
                menuBuilder.removeMenuPresenter(this.iconMenuPresenter);
                this.menu.removeMenuPresenter(this.listMenuPresenter);
            }
            this.iconMenuPresenter = null;
            this.listMenuPresenter = null;
        }

        /* access modifiers changed from: package-private */
        public void setStyle(Context context) {
            TypedArray a = context.obtainStyledAttributes(R.styleable.Theme);
            this.background = a.getResourceId(46, 0);
            this.fullBackground = a.getResourceId(47, 0);
            this.windowAnimations = a.getResourceId(93, 0);
            this.isCompact = a.getBoolean(314, false);
            this.listPresenterTheme = a.getResourceId(315, R.style.Theme_ExpandedMenu);
            a.recycle();
        }

        /* access modifiers changed from: package-private */
        public void setMenu(MenuBuilder menu2) {
            MenuBuilder menuBuilder = this.menu;
            if (menu2 != menuBuilder) {
                if (menuBuilder != null) {
                    menuBuilder.removeMenuPresenter(this.iconMenuPresenter);
                    this.menu.removeMenuPresenter(this.listMenuPresenter);
                }
                this.menu = menu2;
                if (menu2 != null) {
                    IconMenuPresenter iconMenuPresenter2 = this.iconMenuPresenter;
                    if (iconMenuPresenter2 != null) {
                        menu2.addMenuPresenter(iconMenuPresenter2);
                    }
                    ListMenuPresenter listMenuPresenter2 = this.listMenuPresenter;
                    if (listMenuPresenter2 != null) {
                        menu2.addMenuPresenter(listMenuPresenter2);
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public MenuView getListMenuView(Context context, MenuPresenter.Callback cb) {
            if (this.menu == null) {
                return null;
            }
            if (!this.isCompact) {
                getIconMenuView(context, cb);
            }
            if (this.listMenuPresenter == null) {
                this.listMenuPresenter = new ListMenuPresenter((int) R.layout.list_menu_item_layout, this.listPresenterTheme);
                this.listMenuPresenter.setCallback(cb);
                this.listMenuPresenter.setId(R.id.list_menu_presenter);
                this.menu.addMenuPresenter(this.listMenuPresenter);
            }
            IconMenuPresenter iconMenuPresenter2 = this.iconMenuPresenter;
            if (iconMenuPresenter2 != null) {
                this.listMenuPresenter.setItemIndexOffset(iconMenuPresenter2.getNumActualItemsShown());
            }
            return this.listMenuPresenter.getMenuView(this.decorView);
        }

        /* access modifiers changed from: package-private */
        public MenuView getIconMenuView(Context context, MenuPresenter.Callback cb) {
            if (this.menu == null) {
                return null;
            }
            if (this.iconMenuPresenter == null) {
                this.iconMenuPresenter = new IconMenuPresenter(context);
                this.iconMenuPresenter.setCallback(cb);
                this.iconMenuPresenter.setId(R.id.icon_menu_presenter);
                this.menu.addMenuPresenter(this.iconMenuPresenter);
            }
            return this.iconMenuPresenter.getMenuView(this.decorView);
        }

        /* access modifiers changed from: package-private */
        public Parcelable onSaveInstanceState() {
            SavedState savedState = new SavedState();
            savedState.featureId = this.featureId;
            savedState.isOpen = this.isOpen;
            savedState.isInExpandedMode = this.isInExpandedMode;
            if (this.menu != null) {
                savedState.menuState = new Bundle();
                this.menu.savePresenterStates(savedState.menuState);
            }
            return savedState;
        }

        /* access modifiers changed from: package-private */
        public void onRestoreInstanceState(Parcelable state) {
            SavedState savedState = (SavedState) state;
            this.featureId = savedState.featureId;
            this.wasLastOpen = savedState.isOpen;
            this.wasLastExpanded = savedState.isInExpandedMode;
            this.frozenMenuState = savedState.menuState;
            this.createdPanelView = null;
            this.shownPanelView = null;
            this.decorView = null;
        }

        /* access modifiers changed from: package-private */
        public void applyFrozenState() {
            Bundle bundle;
            MenuBuilder menuBuilder = this.menu;
            if (menuBuilder != null && (bundle = this.frozenMenuState) != null) {
                menuBuilder.restorePresenterStates(bundle);
                this.frozenMenuState = null;
            }
        }

        /* access modifiers changed from: private */
        public static class SavedState implements Parcelable {
            public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
                /* class com.android.internal.policy.PhoneWindow.PanelFeatureState.SavedState.AnonymousClass1 */

                @Override // android.os.Parcelable.Creator
                public SavedState createFromParcel(Parcel in) {
                    return SavedState.readFromParcel(in);
                }

                @Override // android.os.Parcelable.Creator
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
            int featureId;
            boolean isInExpandedMode;
            boolean isOpen;
            Bundle menuState;

            private SavedState() {
            }

            @Override // android.os.Parcelable
            public int describeContents() {
                return 0;
            }

            @Override // android.os.Parcelable
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(this.featureId);
                dest.writeInt(this.isOpen ? 1 : 0);
                dest.writeInt(this.isInExpandedMode ? 1 : 0);
                if (this.isOpen) {
                    dest.writeBundle(this.menuState);
                }
            }

            /* access modifiers changed from: private */
            public static SavedState readFromParcel(Parcel source) {
                SavedState savedState = new SavedState();
                savedState.featureId = source.readInt();
                boolean z = false;
                savedState.isOpen = source.readInt() == 1;
                if (source.readInt() == 1) {
                    z = true;
                }
                savedState.isInExpandedMode = z;
                if (savedState.isOpen) {
                    savedState.menuState = source.readBundle();
                }
                return savedState;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class RotationWatcher extends IRotationWatcher.Stub {
        private Handler mHandler;
        private boolean mIsWatching;
        private final Runnable mRotationChanged = new Runnable() {
            /* class com.android.internal.policy.PhoneWindow.RotationWatcher.AnonymousClass1 */

            public void run() {
                RotationWatcher.this.dispatchRotationChanged();
            }
        };
        private final ArrayList<WeakReference<PhoneWindow>> mWindows = new ArrayList<>();

        RotationWatcher() {
        }

        @Override // android.view.IRotationWatcher
        public void onRotationChanged(int rotation) throws RemoteException {
            this.mHandler.post(this.mRotationChanged);
        }

        public void addWindow(PhoneWindow phoneWindow) {
            synchronized (this.mWindows) {
                if (!this.mIsWatching) {
                    try {
                        WindowManagerHolder.sWindowManager.watchRotation(this, phoneWindow.getContext().getDisplayId());
                        this.mHandler = new Handler();
                        this.mIsWatching = true;
                    } catch (RemoteException ex) {
                        Log.e(PhoneWindow.TAG, "Couldn't start watching for device rotation", ex);
                    }
                }
                this.mWindows.add(new WeakReference<>(phoneWindow));
            }
        }

        public void removeWindow(PhoneWindow phoneWindow) {
            synchronized (this.mWindows) {
                int i = 0;
                while (i < this.mWindows.size()) {
                    PhoneWindow win = this.mWindows.get(i).get();
                    if (win != null) {
                        if (win != phoneWindow) {
                            i++;
                        }
                    }
                    this.mWindows.remove(i);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void dispatchRotationChanged() {
            synchronized (this.mWindows) {
                int i = 0;
                while (i < this.mWindows.size()) {
                    PhoneWindow win = this.mWindows.get(i).get();
                    if (win != null) {
                        win.onOptionsPanelRotationChanged();
                        i++;
                    } else {
                        this.mWindows.remove(i);
                    }
                }
            }
        }
    }

    public static final class PhoneWindowMenuCallback implements MenuBuilder.Callback, MenuPresenter.Callback {
        private static final int FEATURE_ID = 6;
        private boolean mShowDialogForSubmenu;
        private MenuDialogHelper mSubMenuHelper;
        private final PhoneWindow mWindow;

        public PhoneWindowMenuCallback(PhoneWindow window) {
            this.mWindow = window;
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu.getRootMenu() != menu) {
                onCloseSubMenu(menu);
            }
            if (allMenusAreClosing) {
                Window.Callback callback = this.mWindow.getCallback();
                if (callback != null && !this.mWindow.isDestroyed()) {
                    callback.onPanelClosed(6, menu);
                }
                if (menu == this.mWindow.mContextMenu) {
                    this.mWindow.dismissContextMenu();
                }
                MenuDialogHelper menuDialogHelper = this.mSubMenuHelper;
                if (menuDialogHelper != null) {
                    menuDialogHelper.dismiss();
                    this.mSubMenuHelper = null;
                }
            }
        }

        private void onCloseSubMenu(MenuBuilder menu) {
            Window.Callback callback = this.mWindow.getCallback();
            if (callback != null && !this.mWindow.isDestroyed()) {
                callback.onPanelClosed(6, menu.getRootMenu());
            }
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            Window.Callback callback = this.mWindow.getCallback();
            return callback != null && !this.mWindow.isDestroyed() && callback.onMenuItemSelected(6, item);
        }

        @Override // com.android.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
        }

        @Override // com.android.internal.view.menu.MenuPresenter.Callback
        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu == null) {
                return false;
            }
            subMenu.setCallback(this);
            if (!this.mShowDialogForSubmenu) {
                return false;
            }
            this.mSubMenuHelper = new MenuDialogHelper(subMenu);
            this.mSubMenuHelper.show(null);
            return true;
        }

        public void setShowDialogForSubmenu(boolean enabled) {
            this.mShowDialogForSubmenu = enabled;
        }
    }

    /* access modifiers changed from: package-private */
    public int getLocalFeaturesPrivate() {
        return super.getLocalFeatures();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.Window
    public void setDefaultWindowFormat(int format) {
        super.setDefaultWindowFormat(format);
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows() {
        sendCloseSystemWindows(getContext(), null);
    }

    /* access modifiers changed from: package-private */
    public void sendCloseSystemWindows(String reason) {
        sendCloseSystemWindows(getContext(), reason);
    }

    public static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManager.isSystemReady()) {
            try {
                ActivityManager.getService().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // android.view.Window
    public int getStatusBarColor() {
        return this.mStatusBarColor;
    }

    @Override // android.view.Window
    public void setStatusBarColor(int color) {
        this.mStatusBarColor = color;
        this.mForcedStatusBarColor = true;
        updateLayoutParamsColor();
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateColorViews(null, false);
        }
    }

    public boolean getIsForcedStatusBarColor() {
        return this.mForcedStatusBarColor;
    }

    public int getForcedStatusBarColor() {
        if (this.mForcedStatusBarColor) {
            return this.mStatusBarColor;
        }
        return 0;
    }

    @Override // android.view.Window
    public int getNavigationBarColor() {
        if (isNavigationBarSetWhite()) {
            this.mNavigationBarColor = HwWidgetFactory.DEFAULT_PRIMARY_COLOR;
            Log.i(TAG, "The current is in startup guide, so let set navigation bar white");
            this.mSpecialSet = true;
        }
        return this.mNavigationBarColor;
    }

    @Override // android.view.Window
    public void setNavigationBarColor(int color) {
        this.mNavigationBarColor = color;
        this.mForcedNavigationBarColor = true;
        updateLayoutParamsColor();
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateColorViews(null, false);
        }
    }

    @Override // android.view.Window
    public void setNavigationBarDividerColor(int navigationBarDividerColor) {
        this.mNavigationBarDividerColor = navigationBarDividerColor;
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateColorViews(null, false);
        }
    }

    @Override // android.view.Window
    public int getNavigationBarDividerColor() {
        return this.mNavigationBarDividerColor;
    }

    @Override // android.view.Window
    public void setStatusBarContrastEnforced(boolean ensureContrast) {
        this.mEnsureStatusBarContrastWhenTransparent = ensureContrast;
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateColorViews(null, false);
        }
    }

    @Override // android.view.Window
    public boolean isStatusBarContrastEnforced() {
        return this.mEnsureStatusBarContrastWhenTransparent;
    }

    @Override // android.view.Window
    public void setNavigationBarContrastEnforced(boolean enforceContrast) {
        this.mEnsureNavigationBarContrastWhenTransparent = enforceContrast;
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateColorViews(null, false);
        }
    }

    @Override // android.view.Window
    public boolean isNavigationBarContrastEnforced() {
        return this.mEnsureNavigationBarContrastWhenTransparent;
    }

    public void setIsStartingWindow(boolean isStartingWindow) {
        this.mIsStartingWindow = isStartingWindow;
    }

    @Override // android.view.Window
    public void setTheme(int resid) {
        this.mTheme = resid;
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            Context context = decorView.getContext();
            if (context instanceof DecorContext) {
                context.setTheme(resid);
            }
        }
    }

    @Override // android.view.Window
    public void setResizingCaptionDrawable(Drawable drawable) {
        this.mDecor.setUserCaptionBackgroundDrawable(drawable);
    }

    @Override // android.view.Window
    public void setDecorCaptionShade(int decorCaptionShade) {
        this.mDecorCaptionShade = decorCaptionShade;
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateDecorCaptionShade();
        }
    }

    /* access modifiers changed from: package-private */
    public int getDecorCaptionShade() {
        return this.mDecorCaptionShade;
    }

    @Override // android.view.Window
    public void setAttributes(WindowManager.LayoutParams params) {
        super.setAttributes(params);
        DecorView decorView = this.mDecor;
        if (decorView != null) {
            decorView.updateLogTag(params);
        }
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public int getEmuiActionBarLayout(int layoutResource) {
        return layoutResource;
    }

    /* access modifiers changed from: protected */
    public void setEmuiActionModeBar(ViewStub viewStub) {
    }

    /* access modifiers changed from: protected */
    public int getHeightMeasureSpec(int fixh, int heightSize, int defaultHeightMeasureSpec) {
        return defaultHeightMeasureSpec;
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamsColor() {
    }

    public boolean isNavigationBarSetWhite() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean CheckPermanentMenuKey() {
        return !ViewConfiguration.get(getContext()).hasPermanentMenuKey();
    }

    /* access modifiers changed from: protected */
    public FloatingToolbar getFloatingToolbar(Window window) {
        return new FloatingToolbar(window);
    }

    /* access modifiers changed from: protected */
    public boolean windowIsTranslucent() {
        return this.mIsTransparent;
    }

    /* access modifiers changed from: protected */
    public void initTranslucentImmersion() {
    }

    /* access modifiers changed from: protected */
    public boolean isTranslucentImmersion() {
        return false;
    }

    @Override // android.view.Window
    public WindowInsetsController getInsetsController() {
        return this.mDecor.getWindowInsetsController();
    }

    /* access modifiers changed from: protected */
    public void initSplitMode() {
    }

    /* access modifiers changed from: protected */
    public boolean isSplitMode() {
        return false;
    }

    @Override // android.view.Window
    public void setSystemGestureExclusionRects(List<Rect> rects) {
        getViewRootImpl().setRootSystemGestureExclusionRects(rects);
    }

    @Override // android.view.Window
    public List<Rect> getSystemGestureExclusionRects() {
        return getViewRootImpl().getRootSystemGestureExclusionRects();
    }
}
