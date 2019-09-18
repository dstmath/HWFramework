package com.android.internal.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.WindowConfiguration;
import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.freeform.HwFreeFormUtils;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.HwSlog;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.DisplayListCanvas;
import android.view.IWindowManager;
import android.view.InputQueue;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowCallbacks;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import com.android.internal.colorextraction.types.Tonal;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.util.Protocol;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.StandaloneActionMode;
import com.android.internal.view.menu.ContextMenuBuilder;
import com.android.internal.view.menu.MenuHelper;
import com.android.internal.widget.AbsHwDecorCaptionView;
import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.BackgroundFallback;
import com.android.internal.widget.DecorCaptionView;
import com.android.internal.widget.FloatingToolbar;
import com.huawei.android.app.HwActivityManager;
import java.util.List;

public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks {
    static final boolean DEBUG_IMMERSION = false;
    private static final boolean DEBUG_MEASURE = false;
    private static final int DECOR_SHADOW_FOCUSED_HEIGHT_IN_DIP = 20;
    private static final int DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP = 5;
    private static final int[] FREEFORM_CHOOSE_STATE_SET = {16842912};
    private static final int[] FREEFORM_FOCUS_STATE_SET = {16842908};
    private static final int[] FREEFORM_RESIZE_STATE_SET = {16843518};
    private static final boolean IS_HONOR_PRODUCT = "HONOR".equals(SystemProperties.get("ro.product.brand"));
    public static final ColorViewAttributes NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES;
    private static final String PERMISSION_USE_SMARTKEY = "huawei.permission.USE_SMARTKEY";
    private static final ViewOutlineProvider PIP_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
            outline.setAlpha(1.0f);
        }
    };
    public static final ColorViewAttributes STATUS_BAR_COLOR_VIEW_ATTRIBUTES;
    private static final boolean SWEEP_OPEN_MENU = false;
    private static final String TAG = "DecorView";
    private static ColorDrawable mFreeFormBackgroundDrawable = new ColorDrawable(0);
    private boolean mAllowUpdateElevation = false;
    private boolean mApplyFloatingHorizontalInsets = false;
    private boolean mApplyFloatingVerticalInsets = false;
    private float mAvailableWidth;
    private BackdropFrameRenderer mBackdropFrameRenderer = null;
    private final BackgroundFallback mBackgroundFallback = new BackgroundFallback();
    private final Rect mBackgroundPadding = new Rect();
    private final int mBarEnterExitDuration;
    private Drawable mCaptionBackgroundDrawable;
    private boolean mChanging;
    ViewGroup mContentRoot;
    DecorCaptionView mDecorCaptionView;
    int mDefaultOpacity = -1;
    private int mDownY;
    private final Rect mDrawingBounds = new Rect();
    private boolean mElevationAdjustedForStack = false;
    /* access modifiers changed from: private */
    public ObjectAnimator mFadeAnim;
    private final int mFeatureId;
    /* access modifiers changed from: private */
    public ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private final Rect mFloatingInsets = new Rect();
    private FloatingToolbar mFloatingToolbar;
    private ViewTreeObserver.OnPreDrawListener mFloatingToolbarPreDrawListener;
    final boolean mForceWindowDrawsStatusBarBackground;
    private boolean mForcedDrawSysBarBackground = false;
    private final Rect mFrameOffsets = new Rect();
    private final Rect mFramePadding = new Rect();
    private Drawable mFreeformForegroundDrawable;
    private boolean mHasCaption = false;
    private boolean mHideFreeFormForeground;
    private final Interpolator mHideInterpolator;
    private final Paint mHorizontalResizeShadowPaint = new Paint();
    public IHwDecorViewEx mHwDecorViewEx = null;
    private boolean mIsFocuse;
    private boolean mIsHandlingTouchEvent = false;
    private boolean mIsImproperSize;
    private boolean mIsInPictureInPictureMode;
    private boolean mIsProperSize;
    private Drawable.Callback mLastBackgroundDrawableCb = null;
    private int mLastBottomInset = 0;
    private boolean mLastHasBottomStableInset = false;
    private boolean mLastHasLeftStableInset = false;
    private boolean mLastHasRightStableInset = false;
    private boolean mLastHasTopStableInset = false;
    private int mLastLeftInset = 0;
    private ViewOutlineProvider mLastOutlineProvider;
    private int mLastRightInset = 0;
    private boolean mLastShouldAlwaysConsumeNavBar = false;
    private int mLastTopInset = 0;
    private int mLastWindowFlags = 0;
    String mLogTag = TAG;
    private Drawable mMenuBackground;
    private final ColorViewState mNavigationColorViewState = new ColorViewState(NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES);
    private Rect mOutsets = new Rect();
    private final IPressGestureDetector mPressGestureDetector;
    ActionMode mPrimaryActionMode;
    /* access modifiers changed from: private */
    public PopupWindow mPrimaryActionModePopup;
    /* access modifiers changed from: private */
    public ActionBarContextView mPrimaryActionModeView;
    private int mResizeMode = -1;
    private final int mResizeShadowSize;
    private Drawable mResizingBackgroundDrawable;
    private int mRootScrollY = 0;
    private final int mSemiTransparentStatusBarColor;
    private final Interpolator mShowInterpolator;
    /* access modifiers changed from: private */
    public Runnable mShowPrimaryActionModePopup;
    private final ColorViewState mStatusColorViewState = new ColorViewState(STATUS_BAR_COLOR_VIEW_ATTRIBUTES);
    private View mStatusGuard;
    private Rect mTempRect;
    private Drawable mUserCaptionBackgroundDrawable;
    private final Paint mVerticalResizeShadowPaint = new Paint();
    private boolean mWatchingForMenu;
    /* access modifiers changed from: private */
    public PhoneWindow mWindow;
    int mWindowMode = -1;
    private boolean mWindowResizeCallbacksAdded = false;

    private class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;

        public ActionModeCallback2Wrapper(ActionMode.Callback wrapped) {
            this.mWrapped = wrapped;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onCreateActionMode(mode, menu);
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            DecorView.this.requestFitSystemWindows();
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        public void onDestroyActionMode(ActionMode mode) {
            boolean isPrimary;
            this.mWrapped.onDestroyActionMode(mode);
            boolean isFloating = false;
            if (DecorView.this.mContext.getApplicationInfo().targetSdkVersion >= 23) {
                isPrimary = mode == DecorView.this.mPrimaryActionMode;
                if (mode == DecorView.this.mFloatingActionMode) {
                    isFloating = true;
                }
                if (!isPrimary && mode.getType() == 0) {
                    Log.e(DecorView.this.mLogTag, "Destroying unexpected ActionMode instance of TYPE_PRIMARY; " + mode + " was not the current primary action mode! Expected " + DecorView.this.mPrimaryActionMode);
                }
                if (!isFloating && mode.getType() == 1) {
                    Log.e(DecorView.this.mLogTag, "Destroying unexpected ActionMode instance of TYPE_FLOATING; " + mode + " was not the current floating action mode! Expected " + DecorView.this.mFloatingActionMode);
                }
            } else {
                isPrimary = mode.getType() == 0;
                if (mode.getType() == 1) {
                    isFloating = true;
                }
            }
            if (isPrimary) {
                if (DecorView.this.mPrimaryActionModePopup != null) {
                    DecorView.this.removeCallbacks(DecorView.this.mShowPrimaryActionModePopup);
                }
                if (DecorView.this.mPrimaryActionModeView != null) {
                    DecorView.this.endOnGoingFadeAnimation();
                    final ActionBarContextView lastActionModeView = DecorView.this.mPrimaryActionModeView;
                    ObjectAnimator unused = DecorView.this.mFadeAnim = ObjectAnimator.ofFloat(DecorView.this.mPrimaryActionModeView, View.ALPHA, new float[]{1.0f, 0.0f});
                    DecorView.this.mFadeAnim.addListener(new Animator.AnimatorListener() {
                        public void onAnimationStart(Animator animation) {
                        }

                        public void onAnimationEnd(Animator animation) {
                            if (lastActionModeView == DecorView.this.mPrimaryActionModeView) {
                                lastActionModeView.setVisibility(8);
                                if (DecorView.this.mPrimaryActionModePopup != null) {
                                    DecorView.this.mPrimaryActionModePopup.dismiss();
                                }
                                lastActionModeView.killMode();
                                ObjectAnimator unused = DecorView.this.mFadeAnim = null;
                            }
                        }

                        public void onAnimationCancel(Animator animation) {
                        }

                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    DecorView.this.mFadeAnim.start();
                }
                DecorView.this.mPrimaryActionMode = null;
            } else if (isFloating) {
                DecorView.this.cleanupFloatingActionModeViews();
                ActionMode unused2 = DecorView.this.mFloatingActionMode = null;
            }
            if (DecorView.this.mWindow.getCallback() != null && !DecorView.this.mWindow.isDestroyed()) {
                try {
                    DecorView.this.mWindow.getCallback().onActionModeFinished(mode);
                } catch (AbstractMethodError e) {
                }
            }
            DecorView.this.requestFitSystemWindows();
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (this.mWrapped instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) this.mWrapped).onGetContentRect(mode, view, outRect);
            } else {
                super.onGetContentRect(mode, view, outRect);
            }
        }
    }

    public static class ColorViewAttributes {
        final int hideWindowFlag;
        final int horizontalGravity;
        final int id;
        final int seascapeGravity;
        final int systemUiHideFlag;
        final String transitionName;
        final int translucentFlag;
        final int verticalGravity;

        private ColorViewAttributes(int systemUiHideFlag2, int translucentFlag2, int verticalGravity2, int horizontalGravity2, int seascapeGravity2, String transitionName2, int id2, int hideWindowFlag2) {
            this.id = id2;
            this.systemUiHideFlag = systemUiHideFlag2;
            this.translucentFlag = translucentFlag2;
            this.verticalGravity = verticalGravity2;
            this.horizontalGravity = horizontalGravity2;
            this.seascapeGravity = seascapeGravity2;
            this.transitionName = transitionName2;
            this.hideWindowFlag = hideWindowFlag2;
        }

        public boolean isPresentHW(int sysUiVis, int windowFlags, boolean force, boolean isEmui) {
            if (!isEmui) {
                return isPresent(sysUiVis, windowFlags, force);
            }
            return (this.systemUiHideFlag & sysUiVis) == 0 && (this.hideWindowFlag & windowFlags) == 0;
        }

        public boolean isPresent(int sysUiVis, int windowFlags, boolean force) {
            return (this.systemUiHideFlag & sysUiVis) == 0 && (this.hideWindowFlag & windowFlags) == 0 && ((Integer.MIN_VALUE & windowFlags) != 0 || force);
        }

        public boolean isVisible(boolean present, int color, int windowFlags, boolean force) {
            return present && (-16777216 & color) != 0 && ((this.translucentFlag & windowFlags) == 0 || force);
        }

        public boolean isVisible(int sysUiVis, int color, int windowFlags, boolean force) {
            return isVisible(isPresent(sysUiVis, windowFlags, force), color, windowFlags, force);
        }
    }

    private static class ColorViewState {
        final ColorViewAttributes attributes;
        int color;
        boolean present = false;
        int targetVisibility = 4;
        View view = null;
        boolean visible;

        ColorViewState(ColorViewAttributes attributes2) {
            this.attributes = attributes2;
        }
    }

    static class WindowManagerHolder {
        static final IWindowManager sWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));

        WindowManagerHolder() {
        }
    }

    static {
        ColorViewAttributes colorViewAttributes = new ColorViewAttributes(4, 67108864, 48, 3, 5, "android:status:background", 16908335, 1024);
        STATUS_BAR_COLOR_VIEW_ATTRIBUTES = colorViewAttributes;
        ColorViewAttributes colorViewAttributes2 = new ColorViewAttributes(2, 134217728, 80, 5, 3, "android:navigation:background", 16908336, 0);
        NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES = colorViewAttributes2;
    }

    DecorView(Context context, int featureId, PhoneWindow window, WindowManager.LayoutParams params) {
        super(context);
        Drawable drawable;
        boolean z = false;
        if (IS_HONOR_PRODUCT) {
            drawable = getContext().getResources().getDrawable(33751979);
        } else {
            drawable = getContext().getResources().getDrawable(33751978);
        }
        this.mFreeformForegroundDrawable = drawable;
        this.mHideFreeFormForeground = false;
        this.mFeatureId = featureId;
        this.mShowInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mHideInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mBarEnterExitDuration = context.getResources().getInteger(17694941);
        if (context.getResources().getBoolean(17956976) && context.getApplicationInfo().targetSdkVersion >= 24 && !HwWidgetFactory.isHwTheme(context)) {
            z = true;
        }
        this.mForceWindowDrawsStatusBarBackground = z;
        this.mSemiTransparentStatusBarColor = context.getResources().getColor(17170783, null);
        updateAvailableWidth();
        setWindow(window);
        updateLogTag(params);
        this.mResizeShadowSize = context.getResources().getDimensionPixelSize(17105296);
        initResizingPaints();
        this.mPressGestureDetector = HwFrameworkFactory.getPressGestureDetector(context, this, this.mWindow.getContext());
        this.mHwDecorViewEx = HwPolicyFactory.getHwDecorViewEx();
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundFallback(int resId) {
        this.mBackgroundFallback.setDrawable(resId != 0 ? getContext().getDrawable(resId) : null);
        setWillNotDraw(getBackground() == null && !this.mBackgroundFallback.hasFallback());
    }

    public boolean gatherTransparentRegion(Region region) {
        return gatherTransparentRegion(this.mStatusColorViewState, region) || gatherTransparentRegion(this.mNavigationColorViewState, region) || super.gatherTransparentRegion(region);
    }

    /* access modifiers changed from: package-private */
    public boolean gatherTransparentRegion(ColorViewState colorViewState, Region region) {
        if (colorViewState.view == null || !colorViewState.visible || !isResizing()) {
            return false;
        }
        return colorViewState.view.gatherTransparentRegion(region);
    }

    public void onDraw(Canvas c) {
        super.onDraw(c);
        if (this.mWindowMode != 5) {
            this.mBackgroundFallback.draw(this, this.mContentRoot, c, this.mWindow.mContentParent, this.mStatusColorViewState.view, this.mNavigationColorViewState.view);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean z;
        boolean handled;
        if (this.mDecorCaptionView != null && (this.mDecorCaptionView instanceof AbsHwDecorCaptionView) && ((AbsHwDecorCaptionView) this.mDecorCaptionView).processKeyEvent(event)) {
            return true;
        }
        int keyCode = event.getKeyCode();
        boolean isDown = event.getAction() == 0;
        if (HwSlog.HW_DEBUG && keyCode == 1000) {
            Log.i(TAG, "View hierachy: ");
            debug();
        }
        if (isDown && event.getRepeatCount() == 0) {
            if (this.mWindow.mPanelChordingKey > 0 && this.mWindow.mPanelChordingKey != keyCode && dispatchKeyShortcutEvent(event)) {
                return true;
            }
            if (this.mWindow.mPreparedPanel != null && this.mWindow.mPreparedPanel.isOpen && this.mWindow.performPanelShortcut(this.mWindow.mPreparedPanel, keyCode, event, 0)) {
                return true;
            }
        }
        if (!this.mWindow.isDestroyed()) {
            if (keyCode == 308) {
                try {
                    if (!(this.mContext.getPackageManager().checkPermission(PERMISSION_USE_SMARTKEY, this.mContext.getPackageName()) == 0)) {
                        return false;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "checkPermission error" + e);
                    return false;
                }
            }
            Window.Callback cb = this.mWindow.getCallback();
            if (cb == null || this.mFeatureId >= 0) {
                handled = super.dispatchKeyEvent(event);
            } else {
                handled = cb.dispatchKeyEvent(event);
            }
            if (handled) {
                return true;
            }
        }
        if (isDown) {
            z = this.mWindow.onKeyDown(this.mFeatureId, event.getKeyCode(), event);
        } else {
            z = this.mWindow.onKeyUp(this.mFeatureId, event.getKeyCode(), event);
        }
        return z;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent ev) {
        if (this.mWindow.mPreparedPanel == null || !this.mWindow.performPanelShortcut(this.mWindow.mPreparedPanel, ev.getKeyCode(), ev, 1)) {
            Window.Callback cb = this.mWindow.getCallback();
            if ((cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchKeyShortcutEvent(ev) : cb.dispatchKeyShortcutEvent(ev)) {
                return true;
            }
            PhoneWindow.PanelFeatureState st = this.mWindow.getPanelState(0, false);
            if (st != null && this.mWindow.mPreparedPanel == null) {
                this.mWindow.preparePanel(st, ev);
                boolean handled = this.mWindow.performPanelShortcut(st, ev.getKeyCode(), ev, 1);
                st.isPrepared = false;
                if (handled) {
                    return true;
                }
            }
            return false;
        }
        if (this.mWindow.mPreparedPanel != null) {
            this.mWindow.mPreparedPanel.isHandled = true;
        }
        return true;
    }

    private void setHandlingTouchEvent(boolean b) {
        this.mIsHandlingTouchEvent = b;
    }

    /* access modifiers changed from: protected */
    public boolean isHandlingTouchEvent() {
        return this.mIsHandlingTouchEvent;
    }

    public boolean isLongPressSwipe() {
        return this.mPressGestureDetector.isLongPressSwipe();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        int oldAction = ev.getAction();
        if (isHandlingTouchEvent()) {
            Log.i("PressGestureDetector", "DecorView.dispatchTouchEvent, isHandlingTouchEvent() is TRUE.");
        }
        if (this.mPressGestureDetector.dispatchTouchEvent(ev, isHandlingTouchEvent())) {
            ev.setAction(3);
        }
        setHandlingTouchEvent(true);
        Window.Callback cb = this.mWindow.getCallback();
        boolean ret = (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchTouchEvent(ev) : cb.dispatchTouchEvent(ev);
        setHandlingTouchEvent(false);
        ev.setAction(oldAction);
        return ret;
    }

    public boolean dispatchTrackballEvent(MotionEvent ev) {
        Window.Callback cb = this.mWindow.getCallback();
        return (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchTrackballEvent(ev) : cb.dispatchTrackballEvent(ev);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        Window.Callback cb = this.mWindow.getCallback();
        return (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchGenericMotionEvent(ev) : cb.dispatchGenericMotionEvent(ev);
    }

    public boolean superDispatchKeyEvent(KeyEvent event) {
        boolean z = true;
        if (event.getKeyCode() == 4) {
            int action = event.getAction();
            if (this.mPrimaryActionMode != null) {
                if (action == 1) {
                    this.mPrimaryActionMode.finish();
                }
                return true;
            } else if (action == 1) {
                this.mPressGestureDetector.handleBackKey();
            }
        }
        if (super.dispatchKeyEvent(event) != 0) {
            return true;
        }
        if (getViewRootImpl() == null || !getViewRootImpl().dispatchUnhandledKeyEvent(event)) {
            z = false;
        }
        return z;
    }

    public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
        return super.dispatchKeyShortcutEvent(event);
    }

    public boolean superDispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public boolean superDispatchTrackballEvent(MotionEvent event) {
        return super.dispatchTrackballEvent(event);
    }

    public boolean superDispatchGenericMotionEvent(MotionEvent event) {
        return super.dispatchGenericMotionEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return onInterceptTouchEvent(event);
    }

    private boolean isOutOfInnerBounds(int x, int y) {
        return x < 0 || y < 0 || x > getWidth() || y > getHeight();
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < -5 || y < -5 || x > getWidth() + 5 || y > getHeight() + 5;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (this.mHasCaption && isShowingCaption() && action == 0 && isOutOfInnerBounds((int) event.getX(), (int) event.getY())) {
            return true;
        }
        if (this.mFeatureId < 0 || action != 0 || !isOutOfBounds((int) event.getX(), (int) event.getY())) {
            return false;
        }
        this.mWindow.closePanel(this.mFeatureId);
        return true;
    }

    public void sendAccessibilityEvent(int eventType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if ((this.mFeatureId == 0 || this.mFeatureId == 6 || this.mFeatureId == 2 || this.mFeatureId == 5) && getChildCount() == 1) {
                getChildAt(0).sendAccessibilityEvent(eventType);
            } else {
                super.sendAccessibilityEvent(eventType);
            }
        }
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        Window.Callback cb = this.mWindow.getCallback();
        if (cb == null || this.mWindow.isDestroyed() || !cb.dispatchPopulateAccessibilityEvent(event)) {
            return super.dispatchPopulateAccessibilityEventInternal(event);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            Rect drawingBounds = this.mDrawingBounds;
            getDrawingRect(drawingBounds);
            Drawable fg = getForeground();
            if (fg != null) {
                Rect frameOffsets = this.mFrameOffsets;
                drawingBounds.left += frameOffsets.left;
                drawingBounds.top += frameOffsets.top;
                drawingBounds.right -= frameOffsets.right;
                drawingBounds.bottom -= frameOffsets.bottom;
                fg.setBounds(drawingBounds);
                Rect framePadding = this.mFramePadding;
                drawingBounds.left += framePadding.left - frameOffsets.left;
                drawingBounds.top += framePadding.top - frameOffsets.top;
                drawingBounds.right -= framePadding.right - frameOffsets.right;
                drawingBounds.bottom -= framePadding.bottom - frameOffsets.bottom;
            }
            Drawable bg = getBackground();
            if (bg != null) {
                bg.setBounds(drawingBounds);
            }
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x01f4  */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:123:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0104  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x018f  */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasureSpec2;
        int heightMeasureSpec2;
        boolean measure;
        int childCount;
        int i;
        boolean isPortrait;
        DisplayMetrics metrics;
        boolean z;
        TypedValue tv;
        int min;
        int sw;
        int mode;
        int mode2;
        TypedValue tvh;
        int h;
        int w;
        DisplayMetrics metrics2 = getContext().getResources().getDisplayMetrics();
        boolean z2 = true;
        boolean isPortrait2 = getResources().getConfiguration().orientation == 1;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        boolean fixedWidth = false;
        this.mApplyFloatingHorizontalInsets = false;
        if (widthMode == Integer.MIN_VALUE) {
            TypedValue tvw = isPortrait2 ? this.mWindow.mFixedWidthMinor : this.mWindow.mFixedWidthMajor;
            if (!(tvw == null || tvw.type == 0)) {
                if (tvw.type == 5) {
                    w = (int) tvw.getDimension(metrics2);
                } else if (tvw.type == 6) {
                    w = (int) tvw.getFraction((float) metrics2.widthPixels, (float) metrics2.widthPixels);
                } else {
                    w = 0;
                }
                int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
                if (w > 0) {
                    widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(Math.min(w, widthSize), 1073741824);
                    fixedWidth = true;
                } else {
                    int widthMeasureSpec3 = View.MeasureSpec.makeMeasureSpec((widthSize - this.mFloatingInsets.left) - this.mFloatingInsets.right, Integer.MIN_VALUE);
                    this.mApplyFloatingHorizontalInsets = true;
                    widthMeasureSpec2 = widthMeasureSpec3;
                }
                this.mApplyFloatingVerticalInsets = false;
                if (heightMode == Integer.MIN_VALUE) {
                    if (isPortrait2) {
                        tvh = this.mWindow.mFixedHeightMajor;
                    } else {
                        tvh = this.mWindow.mFixedHeightMinor;
                    }
                    if (!(tvh == null || tvh.type == 0)) {
                        if (tvh.type == 5) {
                            h = (int) tvh.getDimension(metrics2);
                        } else if (tvh.type == 6) {
                            h = (int) tvh.getFraction((float) metrics2.heightPixels, (float) metrics2.heightPixels);
                        } else {
                            h = 0;
                        }
                        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
                        if (h > 0) {
                            heightMeasureSpec2 = this.mWindow.getHeightMeasureSpec(h, heightSize, View.MeasureSpec.makeMeasureSpec(Math.min(h, heightSize), 1073741824));
                        } else if ((this.mWindow.getAttributes().flags & 256) == 0) {
                            heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec((heightSize - this.mFloatingInsets.top) - this.mFloatingInsets.bottom, Integer.MIN_VALUE);
                            this.mApplyFloatingVerticalInsets = true;
                        }
                        getOutsets(this.mOutsets);
                        if (this.mOutsets.top > 0 || this.mOutsets.bottom > 0) {
                            mode2 = View.MeasureSpec.getMode(heightMeasureSpec2);
                            if (mode2 != 0) {
                                heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.top + View.MeasureSpec.getSize(heightMeasureSpec2) + this.mOutsets.bottom, mode2);
                            }
                        }
                        if (this.mOutsets.left > 0 || this.mOutsets.right > 0) {
                            mode = View.MeasureSpec.getMode(widthMeasureSpec2);
                            if (mode != 0) {
                                widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.left + View.MeasureSpec.getSize(widthMeasureSpec2) + this.mOutsets.right, mode);
                            }
                        }
                        super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
                        int width = getMeasuredWidth();
                        measure = false;
                        if (!HwPCUtils.isValidExtDisplayId(this.mWindow.getContext()) && ((!HwPCUtils.enabledInPad() || !(this.mDecorCaptionView instanceof AbsHwDecorCaptionView)) && this.mWindow.getAttributes().type == 2011 && !isPortrait2)) {
                            sw = this.mWindow.getScreenWidth();
                            if (sw > 0) {
                                width = Math.min(sw, width);
                                measure = true;
                            }
                        }
                        int widthMeasureSpec4 = View.MeasureSpec.makeMeasureSpec(width, 1073741824);
                        if (!fixedWidth && widthMode == Integer.MIN_VALUE) {
                            tv = isPortrait2 ? this.mWindow.mMinWidthMinor : this.mWindow.mMinWidthMajor;
                            if (tv.type != 0) {
                                if (tv.type == 5) {
                                    min = (int) tv.getDimension(metrics2);
                                } else if (tv.type == 6) {
                                    updateAvailableWidth();
                                    min = (int) tv.getFraction(this.mAvailableWidth, this.mAvailableWidth);
                                } else {
                                    min = 0;
                                }
                                if (width < min) {
                                    widthMeasureSpec4 = View.MeasureSpec.makeMeasureSpec(min, 1073741824);
                                    measure = true;
                                }
                            }
                        }
                        if (!(HwFreeFormUtils.isFreeFormEnable() == 0 || getContext().getPackageName() == null || !"com.tencent.mm".equals(getContext().getPackageName()))) {
                            HwFreeFormUtils.setHideAnimator(false);
                            if (this.mWindowMode == 5 && this.mDecorCaptionView != null && this.mDecorCaptionView.isCaptionShowing()) {
                                int height = getMeasuredHeight();
                                childCount = getChildCount();
                                int height2 = height;
                                i = 0;
                                while (i < childCount) {
                                    if (getChildAt(i) instanceof ViewGroup) {
                                        ViewGroup cView = (ViewGroup) getChildAt(i);
                                        if (cView.getChildCount() != 3 || !(cView.getChildAt(0) instanceof DecorCaptionView) || !(cView.getChildAt(1) instanceof ImageView)) {
                                            metrics = metrics2;
                                            isPortrait = isPortrait2;
                                            z = true;
                                        } else {
                                            View cViewChild = cView.getChildAt(2);
                                            height2 -= this.mDecorCaptionView.getCaptionHeight();
                                            metrics = metrics2;
                                            isPortrait = isPortrait2;
                                            cViewChild.measure(widthMeasureSpec4, View.MeasureSpec.makeMeasureSpec(height2, 1073741824));
                                            z = true;
                                            HwFreeFormUtils.setHideAnimator(true);
                                        }
                                    } else {
                                        metrics = metrics2;
                                        isPortrait = isPortrait2;
                                        z = z2;
                                    }
                                    i++;
                                    z2 = z;
                                    metrics2 = metrics;
                                    isPortrait2 = isPortrait;
                                }
                            }
                        }
                        boolean z3 = isPortrait2;
                        if (measure) {
                            super.onMeasure(widthMeasureSpec4, heightMeasureSpec2);
                            return;
                        }
                        return;
                    }
                }
                heightMeasureSpec2 = heightMeasureSpec;
                getOutsets(this.mOutsets);
                mode2 = View.MeasureSpec.getMode(heightMeasureSpec2);
                if (mode2 != 0) {
                }
                mode = View.MeasureSpec.getMode(widthMeasureSpec2);
                if (mode != 0) {
                }
                super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
                int width2 = getMeasuredWidth();
                measure = false;
                sw = this.mWindow.getScreenWidth();
                if (sw > 0) {
                }
                int widthMeasureSpec42 = View.MeasureSpec.makeMeasureSpec(width2, 1073741824);
                if (isPortrait2) {
                }
                if (tv.type != 0) {
                }
                HwFreeFormUtils.setHideAnimator(false);
                int height3 = getMeasuredHeight();
                childCount = getChildCount();
                int height22 = height3;
                i = 0;
                while (i < childCount) {
                }
                boolean z32 = isPortrait2;
                if (measure) {
                }
            }
        }
        widthMeasureSpec2 = widthMeasureSpec;
        this.mApplyFloatingVerticalInsets = false;
        if (heightMode == Integer.MIN_VALUE) {
        }
        heightMeasureSpec2 = heightMeasureSpec;
        getOutsets(this.mOutsets);
        mode2 = View.MeasureSpec.getMode(heightMeasureSpec2);
        if (mode2 != 0) {
        }
        mode = View.MeasureSpec.getMode(widthMeasureSpec2);
        if (mode != 0) {
        }
        super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
        int width22 = getMeasuredWidth();
        measure = false;
        sw = this.mWindow.getScreenWidth();
        if (sw > 0) {
        }
        int widthMeasureSpec422 = View.MeasureSpec.makeMeasureSpec(width22, 1073741824);
        if (isPortrait2) {
        }
        if (tv.type != 0) {
        }
        HwFreeFormUtils.setHideAnimator(false);
        int height32 = getMeasuredHeight();
        childCount = getChildCount();
        int height222 = height32;
        i = 0;
        while (i < childCount) {
        }
        boolean z322 = isPortrait2;
        if (measure) {
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getOutsets(this.mOutsets);
        if (this.mOutsets.left > 0) {
            offsetLeftAndRight(-this.mOutsets.left);
        }
        if (this.mOutsets.top > 0) {
            offsetTopAndBottom(-this.mOutsets.top);
        }
        if (this.mApplyFloatingVerticalInsets) {
            offsetTopAndBottom(this.mFloatingInsets.top);
        }
        if (this.mApplyFloatingHorizontalInsets) {
            offsetLeftAndRight(this.mFloatingInsets.left);
        }
        if (HwFreeFormUtils.isFreeFormEnable() && this.mWindowMode == 5 && this.mDecorCaptionView != null && getContext().getPackageName() != null && "com.tencent.mm".equals(getContext().getPackageName()) && this.mDecorCaptionView.isCaptionShowing()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i) instanceof ViewGroup) {
                    ViewGroup cView = (ViewGroup) getChildAt(i);
                    if (cView.getChildCount() == 3 && (cView.getChildAt(0) instanceof DecorCaptionView) && (cView.getChildAt(1) instanceof ImageView)) {
                        cView.getChildAt(2).offsetTopAndBottom(this.mDecorCaptionView.getCaptionHeight());
                    }
                }
            }
        }
        updateElevation();
        this.mAllowUpdateElevation = true;
        if (changed && this.mResizeMode == 1) {
            getViewRootImpl().requestInvalidateRootRenderNode();
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mMenuBackground != null) {
            this.mMenuBackground.draw(canvas);
        }
    }

    public boolean showContextMenuForChild(View originalView) {
        return showContextMenuForChildInternal(originalView, Float.NaN, Float.NaN);
    }

    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return showContextMenuForChildInternal(originalView, x, y);
    }

    private boolean showContextMenuForChildInternal(View originalView, float x, float y) {
        MenuHelper helper;
        if (this.mWindow.mContextMenuHelper != null) {
            this.mWindow.mContextMenuHelper.dismiss();
            this.mWindow.mContextMenuHelper = null;
        }
        PhoneWindow.PhoneWindowMenuCallback callback = this.mWindow.mContextMenuCallback;
        if (this.mWindow.mContextMenu == null) {
            this.mWindow.mContextMenu = new ContextMenuBuilder(getContext());
            this.mWindow.mContextMenu.setCallback(callback);
        } else {
            this.mWindow.mContextMenu.clearAll();
        }
        boolean isPopup = !Float.isNaN(x) && !Float.isNaN(y);
        if (isPopup) {
            helper = this.mWindow.mContextMenu.showPopup(getContext(), originalView, x, y);
        } else {
            helper = this.mWindow.mContextMenu.showDialog(originalView, originalView.getWindowToken());
        }
        if (helper != null) {
            callback.setShowDialogForSubmenu(!isPopup);
            helper.setPresenterCallback(callback);
        }
        this.mWindow.mContextMenuHelper = helper;
        if (helper != null) {
            return true;
        }
        return false;
    }

    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return startActionModeForChild(originalView, callback, 0);
    }

    public ActionMode startActionModeForChild(View child, ActionMode.Callback callback, int type) {
        return startActionMode(child, callback, type);
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return startActionMode(callback, 0);
    }

    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return startActionMode(this, callback, type);
    }

    private ActionMode startActionMode(View originatingView, ActionMode.Callback callback, int type) {
        ActionMode.Callback2 wrappedCallback = new ActionModeCallback2Wrapper(callback);
        ActionMode mode = null;
        if (this.mWindow.getCallback() != null && !this.mWindow.isDestroyed()) {
            try {
                mode = this.mWindow.getCallback().onWindowStartingActionMode(wrappedCallback, type);
            } catch (AbstractMethodError e) {
                if (type == 0) {
                    try {
                        mode = this.mWindow.getCallback().onWindowStartingActionMode(wrappedCallback);
                    } catch (AbstractMethodError e2) {
                    }
                }
            }
        }
        if (mode == null) {
            mode = createActionMode(type, wrappedCallback, originatingView);
            if (mode == null || !wrappedCallback.onCreateActionMode(mode, mode.getMenu())) {
                mode = null;
            } else {
                setHandledActionMode(mode);
            }
        } else if (mode.getType() == 0) {
            cleanupPrimaryActionMode();
            this.mPrimaryActionMode = mode;
        } else if (mode.getType() == 1) {
            if (this.mFloatingActionMode != null) {
                this.mFloatingActionMode.finish();
            }
            this.mFloatingActionMode = mode;
        }
        if (!(mode == null || this.mWindow.getCallback() == null || this.mWindow.isDestroyed())) {
            try {
                this.mWindow.getCallback().onActionModeStarted(mode);
            } catch (AbstractMethodError e3) {
            }
        }
        return mode;
    }

    private void cleanupPrimaryActionMode() {
        if (this.mPrimaryActionMode != null) {
            this.mPrimaryActionMode.finish();
            this.mPrimaryActionMode = null;
        }
        if (this.mPrimaryActionModeView != null) {
            this.mPrimaryActionModeView.killMode();
        }
    }

    /* access modifiers changed from: private */
    public void cleanupFloatingActionModeViews() {
        if (this.mFloatingToolbar != null) {
            this.mFloatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        if (this.mFloatingActionModeOriginatingView != null) {
            if (this.mFloatingToolbarPreDrawListener != null) {
                this.mFloatingActionModeOriginatingView.getViewTreeObserver().removeOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
                this.mFloatingToolbarPreDrawListener = null;
            }
            this.mFloatingActionModeOriginatingView = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void startChanging() {
        this.mChanging = true;
    }

    /* access modifiers changed from: package-private */
    public void finishChanging() {
        this.mChanging = false;
        drawableChanged();
    }

    public void setWindowBackground(Drawable drawable) {
        if (getBackground() != drawable) {
            if (this.mWindowMode != 5 || !"com.tencent.mm".equals(this.mContext.getPackageName())) {
                setBackgroundDrawable(drawable);
            } else {
                setBackgroundDrawable(null);
            }
            boolean z = true;
            if (drawable != null) {
                if (!this.mWindow.isTranslucent() && !this.mWindow.isShowingWallpaper()) {
                    z = false;
                }
                this.mResizingBackgroundDrawable = enforceNonTranslucentBackground(drawable, z);
            } else {
                Context context = getContext();
                int i = this.mWindow.mBackgroundFallbackResource;
                if (!this.mWindow.isTranslucent() && !this.mWindow.isShowingWallpaper()) {
                    z = false;
                }
                this.mResizingBackgroundDrawable = getResizingBackgroundDrawable(context, 0, i, z);
            }
            if (this.mWindowMode == 5) {
                this.mResizingBackgroundDrawable = mFreeFormBackgroundDrawable;
            }
            if (this.mResizingBackgroundDrawable != null) {
                this.mResizingBackgroundDrawable.getPadding(this.mBackgroundPadding);
            } else {
                this.mBackgroundPadding.setEmpty();
            }
            drawableChanged();
        }
    }

    public void setWindowFrame(Drawable drawable) {
        if (getForeground() != drawable || this.mWindowMode == 5) {
            if (this.mWindowMode == 5 && !this.mHideFreeFormForeground) {
                drawable = this.mFreeformForegroundDrawable;
            }
            this.mHideFreeFormForeground = false;
            setForeground(drawable);
            if (drawable != null) {
                drawable.getPadding(this.mFramePadding);
            } else {
                this.mFramePadding.setEmpty();
            }
            drawableChanged();
        }
    }

    public void onWindowSystemUiVisibilityChanged(int visible) {
        updateColorViews(null, true);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        WindowManager.LayoutParams attrs = this.mWindow.getAttributes();
        this.mFloatingInsets.setEmpty();
        if ((attrs.flags & 256) == 0) {
            if (attrs.height == -2) {
                this.mFloatingInsets.top = insets.getSystemWindowInsetTop();
                this.mFloatingInsets.bottom = insets.getSystemWindowInsetBottom();
                insets = insets.inset(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            }
            if (this.mWindow.getAttributes().width == -2) {
                this.mFloatingInsets.left = insets.getSystemWindowInsetTop();
                this.mFloatingInsets.right = insets.getSystemWindowInsetBottom();
                insets = insets.inset(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), 0);
            }
        }
        this.mFrameOffsets.set(insets.getSystemWindowInsets());
        WindowInsets insets2 = updateStatusGuard(updateColorViews(insets, true));
        if (getForeground() != null) {
            drawableChanged();
        }
        return insets2;
    }

    public boolean isTransitionGroup() {
        return false;
    }

    public static int getColorViewTopInset(int stableTop, int systemTop) {
        return Math.min(stableTop, systemTop);
    }

    public static int getColorViewBottomInset(int stableBottom, int systemBottom) {
        return Math.min(stableBottom, systemBottom);
    }

    public static int getColorViewRightInset(int stableRight, int systemRight) {
        return Math.min(stableRight, systemRight);
    }

    public static int getColorViewLeftInset(int stableLeft, int systemLeft) {
        return Math.min(stableLeft, systemLeft);
    }

    public static boolean isNavBarToRightEdge(int bottomInset, int rightInset) {
        return bottomInset == 0 && rightInset > 0;
    }

    public static boolean isNavBarToLeftEdge(int bottomInset, int leftInset) {
        return bottomInset == 0 && leftInset > 0;
    }

    public static int getNavBarSize(int bottomInset, int rightInset, int leftInset) {
        if (isNavBarToRightEdge(bottomInset, rightInset)) {
            return rightInset;
        }
        return isNavBarToLeftEdge(bottomInset, leftInset) ? leftInset : bottomInset;
    }

    public static void getNavigationBarRect(int canvasWidth, int canvasHeight, Rect stableInsets, Rect contentInsets, Rect outRect) {
        int bottomInset = getColorViewBottomInset(stableInsets.bottom, contentInsets.bottom);
        int leftInset = getColorViewLeftInset(stableInsets.left, contentInsets.left);
        int rightInset = getColorViewLeftInset(stableInsets.right, contentInsets.right);
        int size = getNavBarSize(bottomInset, rightInset, leftInset);
        if (isNavBarToRightEdge(bottomInset, rightInset)) {
            outRect.set(canvasWidth - size, 0, canvasWidth, canvasHeight);
        } else if (isNavBarToLeftEdge(bottomInset, leftInset)) {
            outRect.set(0, 0, size, canvasHeight);
        } else {
            outRect.set(0, canvasHeight - size, canvasWidth, canvasHeight);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowInsets updateColorViews(WindowInsets insets, boolean animate) {
        int statusBarSideInset;
        int i;
        WindowInsets insets2 = insets;
        WindowManager.LayoutParams attrs = this.mWindow.getAttributes();
        int sysUiVisibility = attrs.systemUiVisibility | getWindowSystemUiVisibility();
        boolean consumingStatusBar = true;
        int i2 = 0;
        boolean isImeWindow = this.mWindow.getAttributes().type == 2011;
        if (!this.mWindow.mIsFloating || isImeWindow) {
            boolean disallowAnimate = (!isLaidOut()) | (((this.mLastWindowFlags ^ attrs.flags) & Integer.MIN_VALUE) != 0);
            this.mLastWindowFlags = attrs.flags;
            if (insets2 != null) {
                this.mLastTopInset = getColorViewTopInset(insets.getStableInsetTop(), insets.getSystemWindowInsetTop());
                this.mLastBottomInset = getColorViewBottomInset(insets.getStableInsetBottom(), insets.getSystemWindowInsetBottom());
                this.mLastRightInset = getColorViewRightInset(insets.getStableInsetRight(), insets.getSystemWindowInsetRight());
                this.mLastLeftInset = getColorViewRightInset(insets.getStableInsetLeft(), insets.getSystemWindowInsetLeft());
                boolean hasTopStableInset = insets.getStableInsetTop() != 0;
                boolean disallowAnimate2 = disallowAnimate | (hasTopStableInset != this.mLastHasTopStableInset);
                this.mLastHasTopStableInset = hasTopStableInset;
                boolean hasBottomStableInset = insets.getStableInsetBottom() != 0;
                boolean disallowAnimate3 = disallowAnimate2 | (hasBottomStableInset != this.mLastHasBottomStableInset);
                this.mLastHasBottomStableInset = hasBottomStableInset;
                boolean hasRightStableInset = insets.getStableInsetRight() != 0;
                boolean disallowAnimate4 = disallowAnimate3 | (hasRightStableInset != this.mLastHasRightStableInset);
                this.mLastHasRightStableInset = hasRightStableInset;
                boolean hasLeftStableInset = insets.getStableInsetLeft() != 0;
                disallowAnimate = disallowAnimate4 | (hasLeftStableInset != this.mLastHasLeftStableInset);
                this.mLastHasLeftStableInset = hasLeftStableInset;
                this.mLastShouldAlwaysConsumeNavBar = insets.shouldAlwaysConsumeNavBar();
            }
            boolean disallowAnimate5 = disallowAnimate;
            boolean navBarToRightEdge = isNavBarToRightEdge(this.mLastBottomInset, this.mLastRightInset);
            boolean navBarToLeftEdge = isNavBarToLeftEdge(this.mLastBottomInset, this.mLastLeftInset);
            updateColorViewInt(this.mNavigationColorViewState, sysUiVisibility, this.mWindow.mNavigationBarColor, this.mWindow.mNavigationBarDividerColor, getNavBarSize(this.mLastBottomInset, this.mLastRightInset, this.mLastLeftInset), navBarToRightEdge || navBarToLeftEdge, navBarToLeftEdge, 0, animate && !disallowAnimate5, false);
            boolean statusBarNeedsRightInset = navBarToRightEdge && this.mNavigationColorViewState.present;
            boolean statusBarNeedsLeftInset = navBarToLeftEdge && this.mNavigationColorViewState.present;
            if (statusBarNeedsRightInset) {
                i = this.mLastRightInset;
            } else if (statusBarNeedsLeftInset) {
                i = this.mLastLeftInset;
            } else {
                statusBarSideInset = 0;
                updateColorViewInt(this.mStatusColorViewState, sysUiVisibility, calculateStatusBarColor(), 0, this.mLastTopInset, false, statusBarNeedsLeftInset, statusBarSideInset, !animate && !disallowAnimate5, this.mForceWindowDrawsStatusBarBackground);
            }
            statusBarSideInset = i;
            updateColorViewInt(this.mStatusColorViewState, sysUiVisibility, calculateStatusBarColor(), 0, this.mLastTopInset, false, statusBarNeedsLeftInset, statusBarSideInset, !animate && !disallowAnimate5, this.mForceWindowDrawsStatusBarBackground);
        }
        boolean consumingNavBar = ((attrs.flags & Integer.MIN_VALUE) != 0 && (sysUiVisibility & 512) == 0 && (sysUiVisibility & 2) == 0) || this.mLastShouldAlwaysConsumeNavBar;
        if (!((sysUiVisibility & 1024) == 0 && (sysUiVisibility & Integer.MIN_VALUE) == 0 && (attrs.flags & 256) == 0 && (attrs.flags & Protocol.BASE_SYSTEM_RESERVED) == 0 && this.mForceWindowDrawsStatusBarBackground && this.mLastTopInset != 0)) {
            consumingStatusBar = false;
        }
        int consumedTop = consumingStatusBar ? this.mLastTopInset : 0;
        int consumedRight = consumingNavBar ? this.mLastRightInset : 0;
        int consumedBottom = consumingNavBar ? this.mLastBottomInset : 0;
        if (consumingNavBar) {
            i2 = this.mLastLeftInset;
        }
        int consumedLeft = i2;
        if (this.mContentRoot != null && (this.mContentRoot.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.mContentRoot.getLayoutParams();
            if (!(lp.topMargin == consumedTop && lp.rightMargin == consumedRight && lp.bottomMargin == consumedBottom && lp.leftMargin == consumedLeft)) {
                lp.topMargin = consumedTop;
                lp.rightMargin = consumedRight;
                lp.bottomMargin = consumedBottom;
                lp.leftMargin = consumedLeft;
                this.mContentRoot.setLayoutParams(lp);
                if (insets2 == null) {
                    requestApplyInsets();
                }
            }
            if (insets2 != null) {
                insets2 = insets2.inset(consumedLeft, consumedTop, consumedRight, consumedBottom);
            }
        }
        if (insets2 != null) {
            return insets2.consumeStableInsets();
        }
        return insets2;
    }

    private int calculateStatusBarColor() {
        return calculateStatusBarColor(this.mWindow.getAttributes().flags, this.mSemiTransparentStatusBarColor, this.mWindow.getStatusBarColor());
    }

    public static int calculateStatusBarColor(int flags, int semiTransparentStatusBarColor, int statusBarColor) {
        if ((67108864 & flags) != 0) {
            return semiTransparentStatusBarColor;
        }
        if ((Integer.MIN_VALUE & flags) != 0) {
            return statusBarColor;
        }
        return Tonal.MAIN_COLOR_DARK;
    }

    private int getCurrentColor(ColorViewState state) {
        if (state.visible) {
            return state.color;
        }
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:93:0x01bb, code lost:
        if (r6.leftMargin != r10) goto L_0x01c9;
     */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x015c  */
    private void updateColorViewInt(ColorViewState state, int sysUiVis, int color, int dividerColor, int size, boolean verticalBar, boolean seascape, int sideMargin, boolean animate, boolean force) {
        int resolvedGravity;
        boolean show;
        int rightMargin;
        int leftMargin;
        View view;
        final ColorViewState colorViewState = state;
        int i = color;
        int i2 = dividerColor;
        boolean z = verticalBar;
        boolean z2 = seascape;
        int i3 = sideMargin;
        boolean z3 = animate;
        boolean z4 = force;
        boolean drawBackSet = (this.mWindow.getAttributes().flags & Integer.MIN_VALUE) != 0 || z4;
        boolean isEmui = HwWidgetFactory.isHwTheme(getContext());
        colorViewState.present = colorViewState.attributes.isPresent(sysUiVis, this.mWindow.getAttributes().flags, z4);
        boolean show2 = colorViewState.attributes.isVisible(colorViewState.present, i, this.mWindow.getAttributes().flags, z4);
        boolean showView = show2 && !isResizing() && size > 0;
        boolean isInputMethodWindow = this.mWindow.getAttributes().type == 2011;
        boolean showView2 = ((!this.mWindow.getHwFloating() && (!isEmui || !this.mWindow.windowIsTranslucent() || this.mWindow.isTranslucentImmersion() || isInputMethodWindow)) & showView) | this.mWindow.isSplitMode();
        boolean z5 = isInputMethodWindow;
        if ("android:navigation:background".equals(colorViewState.attributes.transitionName)) {
            this.mWindow.setNavBarShowStatus(showView2);
        }
        View view2 = colorViewState.view;
        boolean visibilityChanged = false;
        int resolvedHeight = z ? -1 : size;
        int resolvedDynStatusBarWidth = -1;
        boolean z6 = drawBackSet;
        boolean isLand = getContext().getResources().getConfiguration().orientation == 2;
        if (this.mForcedDrawSysBarBackground && !z && isLand) {
            resolvedDynStatusBarWidth = this.mWindow.getFullScreenWidth();
        }
        int resolvedWidth = z ? size : resolvedDynStatusBarWidth;
        if (!z) {
            resolvedGravity = colorViewState.attributes.verticalGravity;
        } else if (z2) {
            boolean z7 = isLand;
            resolvedGravity = colorViewState.attributes.seascapeGravity;
        } else {
            resolvedGravity = colorViewState.attributes.horizontalGravity;
        }
        boolean z8 = isEmui;
        if (view2 != null) {
            int resolvedHeight2 = resolvedHeight;
            int vis = showView2 ? 0 : 4;
            boolean visibilityChanged2 = colorViewState.targetVisibility != vis;
            colorViewState.targetVisibility = vis;
            boolean visibilityChanged3 = visibilityChanged2;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view2.getLayoutParams();
            int rightMargin2 = z2 ? 0 : sideMargin;
            int leftMargin2 = z2 ? sideMargin : 0;
            int i4 = vis;
            if (lp.height == resolvedHeight2 && lp.width == resolvedWidth && lp.gravity == resolvedGravity) {
                rightMargin = rightMargin2;
                if (lp.rightMargin == rightMargin) {
                    show = show2;
                    leftMargin = leftMargin2;
                } else {
                    show = show2;
                    leftMargin = leftMargin2;
                }
            } else {
                show = show2;
                rightMargin = rightMargin2;
                leftMargin = leftMargin2;
            }
            lp.height = resolvedHeight2;
            lp.width = resolvedWidth;
            lp.gravity = resolvedGravity;
            lp.rightMargin = rightMargin;
            lp.leftMargin = leftMargin;
            view2.setLayoutParams(lp);
            if (showView2) {
                setColor(view2, i, i2, z, z2);
            }
            visibilityChanged = visibilityChanged3;
        } else if (showView2) {
            boolean isHwLightTheme = HwWidgetFactory.isHwLightTheme(getContext());
            if (isHwLightTheme) {
                boolean z9 = isHwLightTheme;
                if (colorViewState.attributes.transitionName != null && colorViewState.attributes.transitionName.equals("android:navigation:background")) {
                    View hwNavigationBarColorView = HwPolicyFactory.getHwNavigationBarColorView(this.mContext);
                    view = hwNavigationBarColorView;
                    colorViewState.view = hwNavigationBarColorView;
                    view2 = view;
                    setColor(view2, i, i2, z, z2);
                    view2.setTransitionName(colorViewState.attributes.transitionName);
                    view2.setId(colorViewState.attributes.id);
                    view2.setVisibility(4);
                    colorViewState.targetVisibility = 0;
                    FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
                    if (!z2) {
                        lp2.leftMargin = i3;
                    } else {
                        lp2.rightMargin = i3;
                    }
                    addView(view2, lp2);
                    updateColorViewTranslations();
                    show = show2;
                    visibilityChanged = true;
                }
            }
            View view3 = new View(this.mContext);
            view = view3;
            colorViewState.view = view3;
            view2 = view;
            setColor(view2, i, i2, z, z2);
            view2.setTransitionName(colorViewState.attributes.transitionName);
            view2.setId(colorViewState.attributes.id);
            view2.setVisibility(4);
            colorViewState.targetVisibility = 0;
            FrameLayout.LayoutParams lp22 = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
            if (!z2) {
            }
            addView(view2, lp22);
            updateColorViewTranslations();
            show = show2;
            visibilityChanged = true;
        } else {
            show = show2;
        }
        if (visibilityChanged) {
            view2.animate().cancel();
            boolean forceCloseAnimation = this.mWindow.getTryForcedCloseAnimation(WindowManagerHolder.sWindowManager, z3, getTag());
            if (!z3 || forceCloseAnimation || this.mWindow.getAttributes().isEmuiStyle == -1 || isResizing()) {
                int i5 = 0;
                view2.setAlpha(1.0f);
                if (!showView2) {
                    i5 = 4;
                }
                view2.setVisibility(i5);
            } else if (showView2) {
                if (view2.getVisibility() != 0) {
                    view2.setVisibility(0);
                    view2.setAlpha(0.0f);
                }
                view2.animate().alpha(1.0f).setInterpolator(this.mShowInterpolator).setDuration((long) this.mBarEnterExitDuration);
            } else {
                view2.animate().alpha(0.0f).setInterpolator(this.mHideInterpolator).setDuration(0).withEndAction(new Runnable() {
                    public void run() {
                        colorViewState.view.setAlpha(1.0f);
                        colorViewState.view.setVisibility(4);
                    }
                });
            }
        }
        colorViewState.visible = show;
        colorViewState.color = i;
    }

    private static void setColor(View v, int color, int dividerColor, boolean verticalBar, boolean seascape) {
        if (dividerColor != 0) {
            Pair<Boolean, Boolean> dir = (Pair) v.getTag();
            if (dir != null && ((Boolean) dir.first).booleanValue() == verticalBar && ((Boolean) dir.second).booleanValue() == seascape) {
                LayerDrawable d = (LayerDrawable) v.getBackground();
                ((ColorDrawable) ((InsetDrawable) d.getDrawable(1)).getDrawable()).setColor(color);
                ((ColorDrawable) d.getDrawable(0)).setColor(dividerColor);
                return;
            }
            int size = Math.round(TypedValue.applyDimension(1, 1.0f, v.getContext().getResources().getDisplayMetrics()));
            InsetDrawable d2 = new InsetDrawable(new ColorDrawable(color), (!verticalBar || seascape) ? 0 : size, !verticalBar ? size : 0, (!verticalBar || !seascape) ? 0 : size, 0);
            v.setBackground(new LayerDrawable(new Drawable[]{new ColorDrawable(dividerColor), d2}));
            v.setTag(new Pair(Boolean.valueOf(verticalBar), Boolean.valueOf(seascape)));
            return;
        }
        v.setTag(null);
        v.setBackgroundColor(color);
    }

    private void updateColorViewTranslations() {
        int rootScrollY = this.mRootScrollY;
        float f = 0.0f;
        if (this.mStatusColorViewState.view != null) {
            this.mStatusColorViewState.view.setTranslationY(rootScrollY > 0 ? (float) rootScrollY : 0.0f);
        }
        if (this.mNavigationColorViewState.view != null) {
            View view = this.mNavigationColorViewState.view;
            if (rootScrollY < 0) {
                f = (float) rootScrollY;
            }
            view.setTranslationY(f);
        }
    }

    private WindowInsets updateStatusGuard(WindowInsets insets) {
        boolean showStatusGuard = false;
        int i = 0;
        if (this.mPrimaryActionModeView != null && (this.mPrimaryActionModeView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) this.mPrimaryActionModeView.getLayoutParams();
            boolean mlpChanged = false;
            if (this.mPrimaryActionModeView.isShown()) {
                if (this.mTempRect == null) {
                    this.mTempRect = new Rect();
                }
                Rect rect = this.mTempRect;
                this.mWindow.mContentParent.computeSystemWindowInsets(insets, rect);
                if (mlp.topMargin != (rect.top == 0 ? insets.getSystemWindowInsetTop() : 0)) {
                    mlpChanged = true;
                    mlp.topMargin = insets.getSystemWindowInsetTop();
                    if (this.mStatusGuard == null) {
                        this.mStatusGuard = new View(this.mContext);
                        this.mStatusGuard.setBackgroundColor(this.mContext.getColor(17170556));
                        addView(this.mStatusGuard, indexOfChild(this.mStatusColorViewState.view), new FrameLayout.LayoutParams(-1, mlp.topMargin, 8388659));
                    } else {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mStatusGuard.getLayoutParams();
                        if (lp.height != mlp.topMargin) {
                            lp.height = mlp.topMargin;
                            this.mStatusGuard.setLayoutParams(lp);
                        }
                    }
                }
                boolean nonOverlay = true;
                showStatusGuard = this.mStatusGuard != null;
                if ((this.mWindow.getLocalFeaturesPrivate() & 1024) != 0) {
                    nonOverlay = false;
                }
                if (nonOverlay && showStatusGuard) {
                    insets = insets.inset(0, insets.getSystemWindowInsetTop(), 0, 0);
                }
            } else if (mlp.topMargin != 0) {
                mlpChanged = true;
                mlp.topMargin = 0;
            }
            if (mlpChanged) {
                this.mPrimaryActionModeView.setLayoutParams(mlp);
            }
        }
        if (this.mStatusGuard != null) {
            View view = this.mStatusGuard;
            if (!showStatusGuard) {
                i = 8;
            }
            view.setVisibility(i);
        }
        return insets;
    }

    public void updatePictureInPictureOutlineProvider(boolean isInPictureInPictureMode) {
        if (this.mIsInPictureInPictureMode != isInPictureInPictureMode) {
            if (isInPictureInPictureMode) {
                Window.WindowControllerCallback callback = this.mWindow.getWindowControllerCallback();
                if (callback != null && callback.isTaskRoot()) {
                    super.setOutlineProvider(PIP_OUTLINE_PROVIDER);
                }
            } else if (getOutlineProvider() != this.mLastOutlineProvider) {
                setOutlineProvider(this.mLastOutlineProvider);
            }
            this.mIsInPictureInPictureMode = isInPictureInPictureMode;
        }
    }

    public void setOutlineProvider(ViewOutlineProvider provider) {
        super.setOutlineProvider(provider);
        this.mLastOutlineProvider = provider;
    }

    private void drawableChanged() {
        if (!this.mChanging) {
            setPadding(this.mFramePadding.left + this.mBackgroundPadding.left, this.mFramePadding.top + this.mBackgroundPadding.top, this.mFramePadding.right + this.mBackgroundPadding.right, this.mFramePadding.bottom + this.mBackgroundPadding.bottom);
            requestLayout();
            invalidate();
            int opacity = -1;
            if (getResources().getConfiguration().windowConfiguration.hasWindowShadow() || this.mWindowMode == 5) {
                opacity = -3;
            } else {
                Drawable bg = getBackground();
                Drawable fg = getForeground();
                if (bg != null) {
                    if (fg == null) {
                        opacity = bg.getOpacity();
                    } else if (this.mFramePadding.left > 0 || this.mFramePadding.top > 0 || this.mFramePadding.right > 0 || this.mFramePadding.bottom > 0) {
                        opacity = -3;
                    } else {
                        int fop = fg.getOpacity();
                        int bop = bg.getOpacity();
                        if (fop == -1 || bop == -1) {
                            opacity = -1;
                        } else if (fop == 0) {
                            opacity = bop;
                        } else if (bop == 0) {
                            opacity = fop;
                        } else {
                            opacity = Drawable.resolveOpacity(fop, bop);
                        }
                    }
                }
            }
            this.mDefaultOpacity = opacity;
            if (this.mFeatureId < 0) {
                this.mWindow.setDefaultWindowFormat(opacity);
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (this.mWindow.hasFeature(0) && !hasWindowFocus && this.mWindow.mPanelChordingKey != 0) {
            this.mWindow.closePanel(0);
        }
        Window.Callback cb = this.mWindow.getCallback();
        if (cb != null && !this.mWindow.isDestroyed() && this.mFeatureId < 0) {
            cb.onWindowFocusChanged(hasWindowFocus);
            this.mHwDecorViewEx.handleWindowFocusChanged(hasWindowFocus, this.mWindow.getContext());
        }
        if (this.mPrimaryActionMode != null) {
            this.mPrimaryActionMode.onWindowFocusChanged(hasWindowFocus);
        }
        if (this.mFloatingActionMode != null) {
            this.mFloatingActionMode.onWindowFocusChanged(hasWindowFocus);
        }
        updateElevation();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window.Callback cb = this.mWindow.getCallback();
        if (cb != null && !this.mWindow.isDestroyed() && this.mFeatureId < 0) {
            cb.onAttachedToWindow();
        }
        if (this.mFeatureId == -1) {
            this.mWindow.openPanelsAfterRestore();
        }
        if (!this.mWindowResizeCallbacksAdded) {
            getViewRootImpl().addWindowCallbacks(this);
            this.mWindowResizeCallbacksAdded = true;
        } else if (this.mBackdropFrameRenderer != null) {
            this.mBackdropFrameRenderer.onConfigurationChange();
        }
        this.mWindow.onViewRootImplSet(getViewRootImpl());
        if (this.mPressGestureDetector != null) {
            this.mPressGestureDetector.onAttached(this.mWindow.getAttributes().type);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Window.Callback cb = this.mWindow.getCallback();
        if (cb != null && this.mFeatureId < 0) {
            cb.onDetachedFromWindow();
        }
        if (this.mWindow.mDecorContentParent != null) {
            this.mWindow.mDecorContentParent.dismissPopups();
        }
        if (this.mPrimaryActionModePopup != null) {
            removeCallbacks(this.mShowPrimaryActionModePopup);
            if (this.mPrimaryActionModePopup.isShowing()) {
                this.mPrimaryActionModePopup.dismiss();
            }
            this.mPrimaryActionModePopup = null;
        }
        if (this.mFloatingToolbar != null) {
            this.mFloatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        this.mPressGestureDetector.onDetached();
        PhoneWindow.PanelFeatureState st = this.mWindow.getPanelState(0, false);
        if (!(st == null || st.menu == null || this.mFeatureId >= 0)) {
            st.menu.close();
        }
        releaseThreadedRenderer();
        if (this.mWindowResizeCallbacksAdded) {
            getViewRootImpl().removeWindowCallbacks(this);
            this.mWindowResizeCallbacksAdded = false;
        }
    }

    public void onCloseSystemDialogs(String reason) {
        if (this.mFeatureId >= 0) {
            this.mWindow.closeAllPanels();
        }
    }

    public SurfaceHolder.Callback2 willYouTakeTheSurface() {
        if (this.mFeatureId < 0) {
            return this.mWindow.mTakeSurfaceCallback;
        }
        return null;
    }

    public InputQueue.Callback willYouTakeTheInputQueue() {
        if (this.mFeatureId < 0) {
            return this.mWindow.mTakeInputQueueCallback;
        }
        return null;
    }

    public void setSurfaceType(int type) {
        this.mWindow.setType(type);
    }

    public void setSurfaceFormat(int format) {
        this.mWindow.setFormat(format);
    }

    public void setSurfaceKeepScreenOn(boolean keepOn) {
        if (keepOn) {
            this.mWindow.addFlags(128);
        } else {
            this.mWindow.clearFlags(128);
        }
    }

    public void onRootViewScrollYChanged(int rootScrollY) {
        this.mRootScrollY = rootScrollY;
        updateColorViewTranslations();
    }

    private ActionMode createActionMode(int type, ActionMode.Callback2 callback, View originatingView) {
        if (type != 1) {
            return createStandaloneActionMode(callback);
        }
        return createFloatingActionMode(originatingView, callback);
    }

    private void setHandledActionMode(ActionMode mode) {
        if (mode.getType() == 0) {
            setHandledPrimaryActionMode(mode);
        } else if (mode.getType() == 1) {
            setHandledFloatingActionMode(mode);
        }
    }

    private ActionMode createStandaloneActionMode(ActionMode.Callback callback) {
        Context actionBarContext;
        endOnGoingFadeAnimation();
        cleanupPrimaryActionMode();
        boolean z = false;
        if (this.mPrimaryActionModeView == null || !this.mPrimaryActionModeView.isAttachedToWindow()) {
            if (this.mWindow.isFloating()) {
                TypedValue outValue = new TypedValue();
                Resources.Theme baseTheme = this.mContext.getTheme();
                baseTheme.resolveAttribute(16843825, outValue, true);
                if (outValue.resourceId != 0) {
                    Resources.Theme actionBarTheme = this.mContext.getResources().newTheme();
                    actionBarTheme.setTo(baseTheme);
                    actionBarTheme.applyStyle(outValue.resourceId, true);
                    actionBarContext = new ContextThemeWrapper(this.mContext, 0);
                    actionBarContext.getTheme().setTo(actionBarTheme);
                } else {
                    actionBarContext = this.mContext;
                }
                Context actionBarContext2 = actionBarContext;
                this.mPrimaryActionModeView = new ActionBarContextView(actionBarContext2);
                this.mPrimaryActionModePopup = new PopupWindow(actionBarContext2, null, 17891333);
                this.mPrimaryActionModePopup.setWindowLayoutType(2);
                this.mPrimaryActionModePopup.setContentView(this.mPrimaryActionModeView);
                this.mPrimaryActionModePopup.setWidth(-1);
                actionBarContext2.getTheme().resolveAttribute(16843499, outValue, true);
                this.mPrimaryActionModeView.setContentHeight(TypedValue.complexToDimensionPixelSize(outValue.data, actionBarContext2.getResources().getDisplayMetrics()));
                this.mPrimaryActionModePopup.setHeight(-2);
                this.mShowPrimaryActionModePopup = new Runnable() {
                    public void run() {
                        DecorView.this.mPrimaryActionModePopup.showAtLocation(DecorView.this.mPrimaryActionModeView.getApplicationWindowToken(), 55, 0, 0);
                        DecorView.this.endOnGoingFadeAnimation();
                        if (DecorView.this.shouldAnimatePrimaryActionModeView()) {
                            ObjectAnimator unused = DecorView.this.mFadeAnim = ObjectAnimator.ofFloat(DecorView.this.mPrimaryActionModeView, View.ALPHA, new float[]{0.0f, 1.0f});
                            DecorView.this.mFadeAnim.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationStart(Animator animation) {
                                    DecorView.this.mPrimaryActionModeView.setVisibility(0);
                                }

                                public void onAnimationEnd(Animator animation) {
                                    DecorView.this.mPrimaryActionModeView.setAlpha(1.0f);
                                    ObjectAnimator unused = DecorView.this.mFadeAnim = null;
                                }
                            });
                            DecorView.this.mFadeAnim.start();
                            return;
                        }
                        DecorView.this.mPrimaryActionModeView.setAlpha(1.0f);
                        DecorView.this.mPrimaryActionModeView.setVisibility(0);
                    }
                };
            } else {
                ViewStub stub = (ViewStub) findViewById(16908702);
                this.mWindow.setEmuiActionModeBar(stub);
                if (stub != null) {
                    this.mPrimaryActionModeView = (ActionBarContextView) stub.inflate();
                    this.mPrimaryActionModePopup = null;
                }
            }
        }
        if (this.mPrimaryActionModeView == null) {
            return null;
        }
        this.mPrimaryActionModeView.killMode();
        Context context = this.mPrimaryActionModeView.getContext();
        ActionBarContextView actionBarContextView = this.mPrimaryActionModeView;
        if (this.mPrimaryActionModePopup == null) {
            z = true;
        }
        return new StandaloneActionMode(context, actionBarContextView, callback, z);
    }

    /* access modifiers changed from: private */
    public void endOnGoingFadeAnimation() {
        if (this.mFadeAnim != null) {
            this.mFadeAnim.end();
        }
    }

    private void setHandledPrimaryActionMode(ActionMode mode) {
        endOnGoingFadeAnimation();
        this.mPrimaryActionMode = mode;
        this.mPrimaryActionMode.invalidate();
        this.mPrimaryActionModeView.initForMode(this.mPrimaryActionMode);
        if (this.mPrimaryActionModePopup != null) {
            post(this.mShowPrimaryActionModePopup);
        } else if (shouldAnimatePrimaryActionModeView()) {
            this.mFadeAnim = ObjectAnimator.ofFloat(this.mPrimaryActionModeView, View.ALPHA, new float[]{0.0f, 1.0f});
            this.mFadeAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    DecorView.this.mPrimaryActionModeView.setVisibility(0);
                }

                public void onAnimationEnd(Animator animation) {
                    DecorView.this.mPrimaryActionModeView.setAlpha(1.0f);
                    ObjectAnimator unused = DecorView.this.mFadeAnim = null;
                }
            });
            this.mFadeAnim.start();
        } else {
            this.mPrimaryActionModeView.setAlpha(1.0f);
            this.mPrimaryActionModeView.setVisibility(0);
        }
        this.mPrimaryActionModeView.sendAccessibilityEvent(32);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAnimatePrimaryActionModeView() {
        return isLaidOut();
    }

    private ActionMode createFloatingActionMode(View originatingView, ActionMode.Callback2 callback) {
        if (this.mFloatingActionMode != null) {
            this.mFloatingActionMode.finish();
        }
        cleanupFloatingActionModeViews();
        this.mFloatingToolbar = this.mWindow.getFloatingToolbar(this.mWindow);
        final FloatingActionMode mode = new FloatingActionMode(this.mContext, callback, originatingView, this.mFloatingToolbar);
        this.mFloatingActionModeOriginatingView = originatingView;
        this.mFloatingToolbarPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mode.updateViewLocationInWindow();
                return true;
            }
        };
        return mode;
    }

    private void setHandledFloatingActionMode(ActionMode mode) {
        this.mFloatingActionMode = mode;
        this.mFloatingActionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    /* access modifiers changed from: package-private */
    public void enableCaption(boolean attachedAndVisible) {
        if (this.mHasCaption != attachedAndVisible) {
            this.mHasCaption = attachedAndVisible;
            if (getForeground() != null) {
                drawableChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindow(PhoneWindow phoneWindow) {
        this.mWindow = phoneWindow;
        Context context = getContext();
        if (context instanceof DecorContext) {
            ((DecorContext) context).setPhoneWindow(this.mWindow);
        }
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mWindowMode = getWindowMode();
        boolean displayWindowDecor = newConfig.windowConfiguration.hasWindowDecorCaption() || this.mWindowMode == 5;
        if (this.mDecorCaptionView == null && displayWindowDecor) {
            this.mDecorCaptionView = createDecorCaptionView(this.mWindow.getLayoutInflater());
            if (this.mDecorCaptionView != null) {
                if (this.mDecorCaptionView.getParent() == null) {
                    addView(this.mDecorCaptionView, 0, new ViewGroup.LayoutParams(-1, -1));
                }
                removeView(this.mContentRoot);
                this.mDecorCaptionView.addView(this.mContentRoot, new ViewGroup.MarginLayoutParams(-1, -1));
            }
        } else if (this.mDecorCaptionView != null) {
            this.mDecorCaptionView.onConfigurationChanged(displayWindowDecor);
            enableCaption(displayWindowDecor);
        }
        updateAvailableWidth();
        initializeElevation();
        this.mPressGestureDetector.handleConfigurationChanged(newConfig);
    }

    /* access modifiers changed from: package-private */
    public void onResourcesLoaded(LayoutInflater inflater, int layoutResource) {
        this.mWindowMode = getWindowMode();
        if (this.mBackdropFrameRenderer != null) {
            loadBackgroundDrawablesIfNeeded();
            this.mBackdropFrameRenderer.onResourcesLoaded(this, this.mResizingBackgroundDrawable, this.mCaptionBackgroundDrawable, this.mUserCaptionBackgroundDrawable, getCurrentColor(this.mStatusColorViewState), getCurrentColor(this.mNavigationColorViewState));
        }
        this.mDecorCaptionView = createDecorCaptionView(inflater);
        View root = inflater.inflate(layoutResource, null);
        if (this.mDecorCaptionView != null) {
            if (this.mDecorCaptionView.getParent() == null) {
                addView(this.mDecorCaptionView, new ViewGroup.LayoutParams(-1, -1));
            }
            this.mDecorCaptionView.addView(root, new ViewGroup.MarginLayoutParams(-1, -1));
        } else {
            addView(root, 0, new ViewGroup.LayoutParams(-1, -1));
        }
        this.mContentRoot = (ViewGroup) root;
        initializeElevation();
    }

    private void loadBackgroundDrawablesIfNeeded() {
        if (this.mResizingBackgroundDrawable == null) {
            this.mResizingBackgroundDrawable = getResizingBackgroundDrawable(getContext(), this.mWindow.mBackgroundResource, this.mWindow.mBackgroundFallbackResource, this.mWindow.isTranslucent() || this.mWindow.isShowingWallpaper());
            if (this.mWindowMode == 5) {
                this.mResizingBackgroundDrawable = mFreeFormBackgroundDrawable;
            }
            if (this.mResizingBackgroundDrawable == null) {
                String str = this.mLogTag;
                Log.w(str, "Failed to find background drawable for PhoneWindow=" + this.mWindow);
            }
        }
        if (this.mCaptionBackgroundDrawable == null) {
            if (HwFreeFormUtils.isFreeFormEnable()) {
                this.mCaptionBackgroundDrawable = getContext().getDrawable(33751970);
            } else {
                this.mCaptionBackgroundDrawable = getContext().getDrawable(17302109);
            }
        }
        if (this.mWindowMode == 5) {
            this.mCaptionBackgroundDrawable = mFreeFormBackgroundDrawable;
        }
        if (this.mResizingBackgroundDrawable != null) {
            this.mLastBackgroundDrawableCb = this.mResizingBackgroundDrawable.getCallback();
            this.mResizingBackgroundDrawable.setCallback(null);
        }
    }

    /* JADX WARNING: type inference failed for: r3v6, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private DecorCaptionView createDecorCaptionView(LayoutInflater inflater) {
        DecorCaptionView decorCaptionView = null;
        boolean z = true;
        for (int i = getChildCount() - 1; i >= 0 && decorCaptionView == null; i--) {
            ? decorCaptionView2 = getChildAt(i);
            if (decorCaptionView2 instanceof DecorCaptionView) {
                decorCaptionView = decorCaptionView2;
                removeViewAt(i);
            }
        }
        WindowManager.LayoutParams attrs = this.mWindow.getAttributes();
        boolean isApplication = attrs.type == 1 || attrs.type == 2 || attrs.type == 4;
        WindowConfiguration winConfig = getResources().getConfiguration().windowConfiguration;
        if (this.mWindow.isFloating() || !isApplication || (!winConfig.hasWindowDecorCaption() && this.mWindowMode != 5)) {
            decorCaptionView = null;
        } else {
            if (decorCaptionView == null) {
                decorCaptionView = inflateDecorCaptionView(inflater);
            }
            decorCaptionView.setPhoneWindow(this.mWindow, true);
        }
        if (decorCaptionView == null) {
            z = false;
        }
        enableCaption(z);
        return decorCaptionView;
    }

    private DecorCaptionView inflateDecorCaptionView(LayoutInflater inflater) {
        Context context = getContext();
        DecorCaptionView view = getHwDecorCaptionView(LayoutInflater.from(context), context);
        setDecorCaptionShade(context, view);
        return view;
    }

    private DecorCaptionView getHwDecorCaptionView(LayoutInflater inflater, Context context) {
        if (getResources().getConfiguration().windowConfiguration.getWindowingMode() == 10 || (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastMode())) {
            DecorCaptionView view = HwWidgetFactory.getHwDecorCaptionView(inflater);
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(16843827, value, true);
            if (Color.alpha(value.data) != 0) {
                return view;
            }
            view.setBackgroundResource(33751727);
            this.mWindow.setDecorCaptionShade(2);
            return view;
        } else if (HwFreeFormUtils.isFreeFormEnable()) {
            return (DecorCaptionView) inflater.inflate(34013304, null);
        } else {
            return (DecorCaptionView) inflater.inflate(17367127, null);
        }
    }

    private void setDecorCaptionShade(Context context, DecorCaptionView view) {
        switch (this.mWindow.getDecorCaptionShade()) {
            case 1:
                setLightDecorCaptionShade(view);
                return;
            case 2:
                setDarkDecorCaptionShade(view);
                return;
            default:
                TypedValue value = new TypedValue();
                context.getTheme().resolveAttribute(16843827, value, true);
                if (((double) Color.luminance(value.data)) < 0.5d) {
                    setLightDecorCaptionShade(view);
                    return;
                } else {
                    setDarkDecorCaptionShade(view);
                    return;
                }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDecorCaptionShade() {
        if (this.mDecorCaptionView != null) {
            setDecorCaptionShade(getContext(), this.mDecorCaptionView);
        }
    }

    private void setLightDecorCaptionShade(DecorCaptionView view) {
        if (view instanceof AbsHwDecorCaptionView) {
            ((AbsHwDecorCaptionView) view).updateShade(true);
        } else if (HwFreeFormUtils.isFreeFormEnable()) {
            view.findViewById(34603185).setBackgroundResource(33751977);
            view.findViewById(34603187).setBackgroundResource(33751974);
        } else {
            view.findViewById(16909070).setBackgroundResource(17302114);
            view.findViewById(16908823).setBackgroundResource(17302112);
        }
    }

    private void setDarkDecorCaptionShade(DecorCaptionView view) {
        if (view instanceof AbsHwDecorCaptionView) {
            ((AbsHwDecorCaptionView) view).updateShade(false);
        } else if (HwFreeFormUtils.isFreeFormEnable()) {
            view.findViewById(34603185).setBackgroundResource(33751975);
            view.findViewById(34603187).setBackgroundResource(33751972);
        } else {
            view.findViewById(16909070).setBackgroundResource(17302113);
            view.findViewById(16908823).setBackgroundResource(17302111);
        }
    }

    public static Drawable getResizingBackgroundDrawable(Context context, int backgroundRes, int backgroundFallbackRes, boolean windowTranslucent) {
        if (backgroundRes != 0) {
            Drawable drawable = context.getDrawable(backgroundRes);
            if (drawable != null) {
                return enforceNonTranslucentBackground(drawable, windowTranslucent);
            }
        }
        if (backgroundFallbackRes != 0) {
            Drawable fallbackDrawable = context.getDrawable(backgroundFallbackRes);
            if (fallbackDrawable != null) {
                return enforceNonTranslucentBackground(fallbackDrawable, windowTranslucent);
            }
        }
        return new ColorDrawable(Tonal.MAIN_COLOR_DARK);
    }

    private static Drawable enforceNonTranslucentBackground(Drawable drawable, boolean windowTranslucent) {
        if (!windowTranslucent && (drawable instanceof ColorDrawable)) {
            ColorDrawable colorDrawable = (ColorDrawable) drawable;
            int color = colorDrawable.getColor();
            if (Color.alpha(color) != 255) {
                ColorDrawable copy = (ColorDrawable) colorDrawable.getConstantState().newDrawable().mutate();
                copy.setColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                return copy;
            }
        }
        return drawable;
    }

    /* access modifiers changed from: package-private */
    public void clearContentView() {
        if (this.mDecorCaptionView != null) {
            this.mDecorCaptionView.removeContentView();
            return;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (!(v == this.mStatusColorViewState.view || v == this.mNavigationColorViewState.view || v == this.mStatusGuard)) {
                removeViewAt(i);
            }
        }
    }

    public void onWindowSizeIsChanging(Rect newBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets) {
        if (this.mBackdropFrameRenderer != null) {
            this.mBackdropFrameRenderer.setTargetRect(newBounds, fullscreen, systemInsets, stableInsets);
        }
    }

    public void onWindowDragResizeStart(Rect initialBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets, int resizeMode) {
        if (this.mWindow.isDestroyed()) {
            releaseThreadedRenderer();
        } else if (this.mBackdropFrameRenderer == null) {
            ThreadedRenderer renderer = getThreadedRenderer();
            if (renderer != null) {
                loadBackgroundDrawablesIfNeeded();
                BackdropFrameRenderer backdropFrameRenderer = new BackdropFrameRenderer(this, renderer, initialBounds, this.mResizingBackgroundDrawable, this.mCaptionBackgroundDrawable, this.mUserCaptionBackgroundDrawable, getCurrentColor(this.mStatusColorViewState), getCurrentColor(this.mNavigationColorViewState), fullscreen, systemInsets, stableInsets, resizeMode);
                this.mBackdropFrameRenderer = backdropFrameRenderer;
                updateElevation();
                updateColorViews(null, false);
            }
            this.mResizeMode = resizeMode;
            getViewRootImpl().requestInvalidateRootRenderNode();
        }
    }

    public void onWindowDragResizeEnd() {
        releaseThreadedRenderer();
        updateColorViews(null, false);
        this.mResizeMode = -1;
        getViewRootImpl().requestInvalidateRootRenderNode();
    }

    public boolean onContentDrawn(int offsetX, int offsetY, int sizeX, int sizeY) {
        if (this.mBackdropFrameRenderer == null) {
            return false;
        }
        return this.mBackdropFrameRenderer.onContentDrawn(offsetX, offsetY, sizeX, sizeY);
    }

    public void onRequestDraw(boolean reportNextDraw) {
        if (this.mBackdropFrameRenderer != null) {
            this.mBackdropFrameRenderer.onRequestDraw(reportNextDraw);
        } else if (reportNextDraw && isAttachedToWindow()) {
            getViewRootImpl().reportDrawFinish();
        }
    }

    public void onPostDraw(DisplayListCanvas canvas) {
        drawResizingShadowIfNeeded(canvas);
    }

    private void initResizingPaints() {
        int startColor = this.mContext.getResources().getColor(17170741, null);
        int endColor = this.mContext.getResources().getColor(17170740, null);
        int middleColor = (startColor + endColor) / 2;
        Paint paint = this.mHorizontalResizeShadowPaint;
        LinearGradient linearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mResizeShadowSize, new int[]{startColor, middleColor, endColor}, new float[]{0.0f, 0.3f, 1.0f}, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        Paint paint2 = this.mVerticalResizeShadowPaint;
        LinearGradient linearGradient2 = new LinearGradient(0.0f, 0.0f, (float) this.mResizeShadowSize, 0.0f, new int[]{startColor, middleColor, endColor}, new float[]{0.0f, 0.3f, 1.0f}, Shader.TileMode.CLAMP);
        paint2.setShader(linearGradient2);
    }

    private void drawResizingShadowIfNeeded(DisplayListCanvas canvas) {
        if (this.mResizeMode == 1 && !this.mWindow.mIsFloating && !this.mWindow.isTranslucent() && !this.mWindow.isShowingWallpaper()) {
            canvas.save();
            canvas.translate(0.0f, (float) (getHeight() - this.mFrameOffsets.bottom));
            canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) this.mResizeShadowSize, this.mHorizontalResizeShadowPaint);
            canvas.restore();
            canvas.save();
            canvas.translate((float) (getWidth() - this.mFrameOffsets.right), 0.0f);
            canvas.drawRect(0.0f, 0.0f, (float) this.mResizeShadowSize, (float) getHeight(), this.mVerticalResizeShadowPaint);
            canvas.restore();
        }
    }

    private void releaseThreadedRenderer() {
        if (!(this.mResizingBackgroundDrawable == null || this.mLastBackgroundDrawableCb == null)) {
            this.mResizingBackgroundDrawable.setCallback(this.mLastBackgroundDrawableCb);
            this.mLastBackgroundDrawableCb = null;
        }
        if (this.mBackdropFrameRenderer != null) {
            this.mBackdropFrameRenderer.releaseRenderer();
            this.mBackdropFrameRenderer = null;
            updateElevation();
        }
    }

    private boolean isResizing() {
        return this.mBackdropFrameRenderer != null;
    }

    private void initializeElevation() {
        this.mAllowUpdateElevation = false;
        updateElevation();
    }

    private void updateElevation() {
        float elevation = 0.0f;
        boolean wasAdjustedForStack = this.mElevationAdjustedForStack;
        int windowingMode = getResources().getConfiguration().windowConfiguration.getWindowingMode();
        float f = 5.0f;
        if ((windowingMode == 5 || this.mWindowMode == 5 || windowingMode == 10) && !isResizing()) {
            if (hasWindowFocus()) {
                f = 20.0f;
            }
            float elevation2 = f;
            if (!this.mAllowUpdateElevation) {
                elevation2 = 20.0f;
            }
            if (HwFreeFormUtils.isFreeFormEnable()) {
                elevation = dipToPx(0.0f);
            } else {
                elevation = dipToPx(elevation2);
            }
            this.mElevationAdjustedForStack = true;
        } else if (windowingMode == 2) {
            elevation = dipToPx(5.0f);
            this.mElevationAdjustedForStack = true;
        } else {
            this.mElevationAdjustedForStack = false;
        }
        if ((wasAdjustedForStack || this.mElevationAdjustedForStack) && getElevation() != elevation) {
            this.mWindow.setElevation(elevation);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isShowingCaption() {
        return this.mDecorCaptionView != null && this.mDecorCaptionView.isCaptionShowing();
    }

    /* access modifiers changed from: package-private */
    public int getCaptionHeight() {
        if (isShowingCaption()) {
            return this.mDecorCaptionView.getCaptionHeight();
        }
        return 0;
    }

    private float dipToPx(float dip) {
        return TypedValue.applyDimension(1, dip, getResources().getDisplayMetrics());
    }

    /* access modifiers changed from: package-private */
    public void setUserCaptionBackgroundDrawable(Drawable drawable) {
        this.mUserCaptionBackgroundDrawable = drawable;
        if (this.mBackdropFrameRenderer != null) {
            this.mBackdropFrameRenderer.setUserCaptionBackgroundDrawable(drawable);
        }
    }

    private static String getTitleSuffix(WindowManager.LayoutParams params) {
        if (params == null) {
            return "";
        }
        String[] split = params.getTitle().toString().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return "";
    }

    /* access modifiers changed from: package-private */
    public void updateLogTag(WindowManager.LayoutParams params) {
        this.mLogTag = "DecorView[" + getTitleSuffix(params) + "]";
    }

    public void updateAvailableWidth() {
        Resources res = getResources();
        this.mAvailableWidth = TypedValue.applyDimension(1, (float) res.getConfiguration().screenWidthDp, res.getDisplayMetrics());
    }

    public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
        PhoneWindow.PanelFeatureState st = this.mWindow.getPanelState(0, false);
        Menu menu = st != null ? st.menu : null;
        if (!this.mWindow.isDestroyed() && this.mWindow.getCallback() != null) {
            this.mWindow.getCallback().onProvideKeyboardShortcuts(list, menu, deviceId);
        }
    }

    public void dispatchPointerCaptureChanged(boolean hasCapture) {
        super.dispatchPointerCaptureChanged(hasCapture);
        if (!this.mWindow.isDestroyed() && this.mWindow.getCallback() != null) {
            this.mWindow.getCallback().onPointerCaptureChanged(hasCapture);
        }
    }

    public int getAccessibilityViewId() {
        return 2147483646;
    }

    public String toString() {
        return "DecorView@" + Integer.toHexString(hashCode()) + "[" + getTitleSuffix(this.mWindow.getAttributes()) + "]";
    }

    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (this.mIsProperSize) {
            mergeDrawableStates(drawableState, FREEFORM_CHOOSE_STATE_SET);
        } else if (this.mIsImproperSize) {
            mergeDrawableStates(drawableState, FREEFORM_RESIZE_STATE_SET);
        } else if (this.mIsFocuse) {
            mergeDrawableStates(drawableState, FREEFORM_FOCUS_STATE_SET);
        }
        return drawableState;
    }

    public void refreshFreeFormOutLineState(boolean isProperSize, boolean isImproperSize, boolean isFocuse) {
        boolean needsRefresh = false;
        if (this.mIsProperSize != isProperSize) {
            this.mIsProperSize = isProperSize;
            needsRefresh = true;
        }
        if (this.mIsImproperSize != isImproperSize) {
            this.mIsImproperSize = isImproperSize;
            needsRefresh = true;
        }
        if (this.mIsFocuse != isFocuse) {
            this.mIsFocuse = isFocuse;
            needsRefresh = true;
        }
        if (needsRefresh) {
            refreshDrawableState();
        }
    }

    public void setWindowFrameForced(Drawable drawable) {
        if (getForeground() != drawable) {
            setForeground(drawable);
            if (drawable != null) {
                drawable.getPadding(this.mFramePadding);
            } else {
                this.mFramePadding.setEmpty();
            }
            drawableChanged();
        }
        if (getOutlineProvider() == ViewOutlineProvider.HW_FREEFORM_OUTLINE_PROVIDER) {
            setOutlineProvider(null);
            setClipToOutline(true);
        }
    }

    public void setHideFreeFormForeground() {
        this.mHideFreeFormForeground = true;
    }

    private int getWindowMode() {
        int windowmode = 0;
        Window.WindowControllerCallback callback = this.mWindow.getWindowControllerCallback();
        if (callback != null && (callback instanceof Activity)) {
            windowmode = HwActivityManager.getActivityWindowMode(((Activity) callback).getActivityToken());
        }
        if (windowmode == 0) {
            return 1;
        }
        return windowmode;
    }
}
