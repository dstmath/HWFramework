package com.android.internal.policy;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.hwcontrol.HwWidgetFactory;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.HwSlog;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
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
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewStub;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.Window.WindowControllerCallback;
import android.view.WindowCallbacks;
import android.view.WindowInsets;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.policy.PhoneWindow.PhoneWindowMenuCallback;
import com.android.internal.telephony.AbstractRILConstants;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.Protocol;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.StandaloneActionMode;
import com.android.internal.view.menu.ContextMenuBuilder;
import com.android.internal.view.menu.MenuHelper;
import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.BackgroundFallback;
import com.android.internal.widget.DecorCaptionView;
import com.android.internal.widget.FloatingToolbar;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;

public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks {
    static final boolean DEBUG_IMMERSION = false;
    private static final boolean DEBUG_MEASURE = false;
    private static final int DECOR_SHADOW_FOCUSED_HEIGHT_IN_DIP = 20;
    private static final int DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP = 5;
    private static final String PERMISSION_USE_SMARTKEY = "huawei.permission.USE_SMARTKEY";
    private static final boolean SWEEP_OPEN_MENU = false;
    private static final String TAG = "DecorView";
    private boolean mAllowUpdateElevation;
    private boolean mApplyFloatingHorizontalInsets;
    private boolean mApplyFloatingVerticalInsets;
    private float mAvailableWidth;
    private BackdropFrameRenderer mBackdropFrameRenderer;
    private final BackgroundFallback mBackgroundFallback;
    private final Rect mBackgroundPadding;
    private final int mBarEnterExitDuration;
    private Drawable mCaptionBackgroundDrawable;
    private boolean mChanging;
    ViewGroup mContentRoot;
    DecorCaptionView mDecorCaptionView;
    int mDefaultOpacity;
    private int mDownY;
    private final Rect mDrawingBounds;
    private boolean mElevationAdjustedForStack;
    private ObjectAnimator mFadeAnim;
    private final int mFeatureId;
    private ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private final Rect mFloatingInsets;
    private FloatingToolbar mFloatingToolbar;
    private OnPreDrawListener mFloatingToolbarPreDrawListener;
    final boolean mForceWindowDrawsStatusBarBackground;
    private boolean mForcedDrawSysBarBackground;
    private final Rect mFrameOffsets;
    private final Rect mFramePadding;
    private boolean mHasCaption;
    private final Interpolator mHideInterpolator;
    private final Paint mHorizontalResizeShadowPaint;
    private Callback mLastBackgroundDrawableCb;
    private int mLastBottomInset;
    private boolean mLastHasBottomStableInset;
    private boolean mLastHasRightStableInset;
    private boolean mLastHasTopStableInset;
    private int mLastRightInset;
    private boolean mLastShouldAlwaysConsumeNavBar;
    private int mLastTopInset;
    private int mLastWindowFlags;
    String mLogTag;
    private Drawable mMenuBackground;
    private final ColorViewState mNavigationColorViewState;
    private View mNavigationGuard;
    private Rect mOutsets;
    ActionMode mPrimaryActionMode;
    private PopupWindow mPrimaryActionModePopup;
    private ActionBarContextView mPrimaryActionModeView;
    private int mResizeMode;
    private final int mResizeShadowSize;
    private Drawable mResizingBackgroundDrawable;
    private int mRootScrollY;
    private final int mSemiTransparentStatusBarColor;
    private final Interpolator mShowInterpolator;
    private Runnable mShowPrimaryActionModePopup;
    int mStackId;
    private final ColorViewState mStatusColorViewState;
    private View mStatusGuard;
    private Rect mTempRect;
    private Drawable mUserCaptionBackgroundDrawable;
    private final Paint mVerticalResizeShadowPaint;
    private boolean mWatchingForMenu;
    private PhoneWindow mWindow;
    private boolean mWindowResizeCallbacksAdded;

    /* renamed from: com.android.internal.policy.DecorView.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ColorViewState val$state;

        AnonymousClass1(ColorViewState val$state) {
            this.val$state = val$state;
        }

        public void run() {
            this.val$state.view.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            this.val$state.view.setVisibility(4);
        }
    }

    /* renamed from: com.android.internal.policy.DecorView.4 */
    class AnonymousClass4 implements OnPreDrawListener {
        final /* synthetic */ FloatingActionMode val$mode;

        AnonymousClass4(FloatingActionMode val$mode) {
            this.val$mode = val$mode;
        }

        public boolean onPreDraw() {
            this.val$mode.updateViewLocationInWindow();
            return true;
        }
    }

    private class ActionModeCallback2Wrapper extends Callback2 {
        private final ActionMode.Callback mWrapped;

        /* renamed from: com.android.internal.policy.DecorView.ActionModeCallback2Wrapper.1 */
        class AnonymousClass1 implements AnimatorListener {
            final /* synthetic */ ActionBarContextView val$lastActionModeView;

            AnonymousClass1(ActionBarContextView val$lastActionModeView) {
                this.val$lastActionModeView = val$lastActionModeView;
            }

            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (this.val$lastActionModeView == DecorView.this.mPrimaryActionModeView) {
                    this.val$lastActionModeView.setVisibility(8);
                    if (DecorView.this.mPrimaryActionModePopup != null) {
                        DecorView.this.mPrimaryActionModePopup.dismiss();
                    }
                    this.val$lastActionModeView.killMode();
                    DecorView.this.mFadeAnim = null;
                }
            }

            public void onAnimationCancel(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }
        }

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
            boolean isFloating;
            boolean isMncApp = DecorView.SWEEP_OPEN_MENU;
            this.mWrapped.onDestroyActionMode(mode);
            if (DecorView.this.mContext.getApplicationInfo().targetSdkVersion >= 23) {
                isMncApp = true;
            }
            if (isMncApp) {
                isPrimary = mode == DecorView.this.mPrimaryActionMode ? true : DecorView.SWEEP_OPEN_MENU;
                isFloating = mode == DecorView.this.mFloatingActionMode ? true : DecorView.SWEEP_OPEN_MENU;
                if (!isPrimary && mode.getType() == 0) {
                    Log.e(DecorView.this.mLogTag, "Destroying unexpected ActionMode instance of TYPE_PRIMARY; " + mode + " was not the current primary action mode! Expected " + DecorView.this.mPrimaryActionMode);
                }
                if (!isFloating && mode.getType() == 1) {
                    Log.e(DecorView.this.mLogTag, "Destroying unexpected ActionMode instance of TYPE_FLOATING; " + mode + " was not the current floating action mode! Expected " + DecorView.this.mFloatingActionMode);
                }
            } else {
                isPrimary = mode.getType() == 0 ? true : DecorView.SWEEP_OPEN_MENU;
                isFloating = mode.getType() == 1 ? true : DecorView.SWEEP_OPEN_MENU;
            }
            if (isPrimary) {
                if (DecorView.this.mPrimaryActionModePopup != null) {
                    DecorView.this.removeCallbacks(DecorView.this.mShowPrimaryActionModePopup);
                }
                if (DecorView.this.mPrimaryActionModeView != null) {
                    DecorView.this.endOnGoingFadeAnimation();
                    ActionBarContextView lastActionModeView = DecorView.this.mPrimaryActionModeView;
                    DecorView.this.mFadeAnim = ObjectAnimator.ofFloat(DecorView.this.mPrimaryActionModeView, View.ALPHA, new float[]{LayoutParams.BRIGHTNESS_OVERRIDE_FULL, 0.0f});
                    DecorView.this.mFadeAnim.addListener(new AnonymousClass1(lastActionModeView));
                    DecorView.this.mFadeAnim.start();
                }
                DecorView.this.mPrimaryActionMode = null;
            } else if (isFloating) {
                DecorView.this.cleanupFloatingActionModeViews();
                DecorView.this.mFloatingActionMode = null;
            }
            if (!(DecorView.this.mWindow.getCallback() == null || DecorView.this.mWindow.isDestroyed())) {
                try {
                    DecorView.this.mWindow.getCallback().onActionModeFinished(mode);
                } catch (AbstractMethodError e) {
                }
            }
            DecorView.this.requestFitSystemWindows();
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (this.mWrapped instanceof Callback2) {
                ((Callback2) this.mWrapped).onGetContentRect(mode, view, outRect);
            } else {
                super.onGetContentRect(mode, view, outRect);
            }
        }
    }

    private static class ColorViewState {
        int color;
        final int hideWindowFlag;
        final int horizontalGravity;
        final int id;
        boolean present;
        final int systemUiHideFlag;
        int targetVisibility;
        final String transitionName;
        final int translucentFlag;
        final int verticalGravity;
        View view;
        boolean visible;

        ColorViewState(int systemUiHideFlag, int translucentFlag, int verticalGravity, int horizontalGravity, String transitionName, int id, int hideWindowFlag) {
            this.view = null;
            this.targetVisibility = 4;
            this.present = DecorView.SWEEP_OPEN_MENU;
            this.id = id;
            this.systemUiHideFlag = systemUiHideFlag;
            this.translucentFlag = translucentFlag;
            this.verticalGravity = verticalGravity;
            this.horizontalGravity = horizontalGravity;
            this.transitionName = transitionName;
            this.hideWindowFlag = hideWindowFlag;
        }
    }

    static class WindowManagerHolder {
        static final IWindowManager sWindowManager = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.policy.DecorView.WindowManagerHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.policy.DecorView.WindowManagerHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.policy.DecorView.WindowManagerHolder.<clinit>():void");
        }

        WindowManagerHolder() {
        }
    }

    DecorView(Context context, int featureId, PhoneWindow window, LayoutParams params) {
        super(context);
        this.mAllowUpdateElevation = SWEEP_OPEN_MENU;
        this.mElevationAdjustedForStack = SWEEP_OPEN_MENU;
        this.mDefaultOpacity = -1;
        this.mDrawingBounds = new Rect();
        this.mBackgroundPadding = new Rect();
        this.mFramePadding = new Rect();
        this.mFrameOffsets = new Rect();
        this.mHasCaption = SWEEP_OPEN_MENU;
        this.mStatusColorViewState = new ColorViewState(4, EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS, 48, 3, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, R.id.statusBarBackground, GL10.GL_STENCIL_BUFFER_BIT);
        this.mNavigationColorViewState = new ColorViewState(2, EditorInfo.IME_FLAG_NAVIGATE_NEXT, 80, DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME, R.id.navigationBarBackground, 0);
        this.mBackgroundFallback = new BackgroundFallback();
        this.mLastTopInset = 0;
        this.mLastBottomInset = 0;
        this.mLastRightInset = 0;
        this.mLastHasTopStableInset = SWEEP_OPEN_MENU;
        this.mLastHasBottomStableInset = SWEEP_OPEN_MENU;
        this.mLastHasRightStableInset = SWEEP_OPEN_MENU;
        this.mLastWindowFlags = 0;
        this.mLastShouldAlwaysConsumeNavBar = SWEEP_OPEN_MENU;
        this.mRootScrollY = 0;
        this.mOutsets = new Rect();
        this.mWindowResizeCallbacksAdded = SWEEP_OPEN_MENU;
        this.mLastBackgroundDrawableCb = null;
        this.mBackdropFrameRenderer = null;
        this.mLogTag = TAG;
        this.mFloatingInsets = new Rect();
        this.mApplyFloatingVerticalInsets = SWEEP_OPEN_MENU;
        this.mApplyFloatingHorizontalInsets = SWEEP_OPEN_MENU;
        this.mResizeMode = -1;
        this.mVerticalResizeShadowPaint = new Paint();
        this.mHorizontalResizeShadowPaint = new Paint();
        this.mForcedDrawSysBarBackground = SWEEP_OPEN_MENU;
        this.mFeatureId = featureId;
        this.mShowInterpolator = AnimationUtils.loadInterpolator(context, R.interpolator.linear_out_slow_in);
        this.mHideInterpolator = AnimationUtils.loadInterpolator(context, R.interpolator.fast_out_linear_in);
        this.mBarEnterExitDuration = context.getResources().getInteger(R.integer.dock_enter_exit_duration);
        boolean z = (!context.getResources().getBoolean(R.bool.config_forceWindowDrawsStatusBarBackground) || context.getApplicationInfo().targetSdkVersion < 24) ? SWEEP_OPEN_MENU : HwWidgetFactory.isHwTheme(context) ? SWEEP_OPEN_MENU : true;
        this.mForceWindowDrawsStatusBarBackground = z;
        this.mSemiTransparentStatusBarColor = context.getResources().getColor(R.color.system_bar_background_semi_transparent, null);
        updateAvailableWidth();
        setWindow(window);
        updateLogTag(params);
        this.mResizeShadowSize = context.getResources().getDimensionPixelSize(R.dimen.resize_shadow_size);
        initResizingPaints();
    }

    void setBackgroundFallback(int resId) {
        Drawable drawable = null;
        BackgroundFallback backgroundFallback = this.mBackgroundFallback;
        if (resId != 0) {
            drawable = getContext().getDrawable(resId);
        }
        backgroundFallback.setDrawable(drawable);
        boolean z = (getBackground() != null || this.mBackgroundFallback.hasFallback()) ? SWEEP_OPEN_MENU : true;
        setWillNotDraw(z);
    }

    public boolean gatherTransparentRegion(Region region) {
        return (gatherTransparentRegion(this.mStatusColorViewState, region) || gatherTransparentRegion(this.mNavigationColorViewState, region)) ? true : super.gatherTransparentRegion(region);
    }

    boolean gatherTransparentRegion(ColorViewState colorViewState, Region region) {
        if (colorViewState.view != null && colorViewState.visible && isResizing()) {
            return colorViewState.view.gatherTransparentRegion(region);
        }
        return SWEEP_OPEN_MENU;
    }

    public void onDraw(Canvas c) {
        super.onDraw(c);
        this.mBackgroundFallback.draw(this.mContentRoot, c, this.mWindow.mContentParent);
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean onKeyDown;
        int keyCode = event.getKeyCode();
        boolean isDown = event.getAction() == 0 ? true : SWEEP_OPEN_MENU;
        if (HwSlog.HW_DEBUG && keyCode == RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) {
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
            boolean handled;
            if (keyCode == MetricsEvent.TUNER_NIGHT_MODE) {
                try {
                    if (!(this.mContext.getPackageManager().checkPermission(PERMISSION_USE_SMARTKEY, this.mContext.getPackageName()) == 0 ? true : SWEEP_OPEN_MENU)) {
                        return SWEEP_OPEN_MENU;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "checkPermission error" + e);
                    return SWEEP_OPEN_MENU;
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
            onKeyDown = this.mWindow.onKeyDown(this.mFeatureId, event.getKeyCode(), event);
        } else {
            onKeyDown = this.mWindow.onKeyUp(this.mFeatureId, event.getKeyCode(), event);
        }
        return onKeyDown;
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent ev) {
        if (this.mWindow.mPreparedPanel == null || !this.mWindow.performPanelShortcut(this.mWindow.mPreparedPanel, ev.getKeyCode(), ev, 1)) {
            Window.Callback cb = this.mWindow.getCallback();
            boolean handled = (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchKeyShortcutEvent(ev) : cb.dispatchKeyShortcutEvent(ev);
            if (handled) {
                return true;
            }
            PanelFeatureState st = this.mWindow.getPanelState(0, SWEEP_OPEN_MENU);
            if (st != null && this.mWindow.mPreparedPanel == null) {
                this.mWindow.preparePanel(st, ev);
                handled = this.mWindow.performPanelShortcut(st, ev.getKeyCode(), ev, 1);
                st.isPrepared = SWEEP_OPEN_MENU;
                if (handled) {
                    return true;
                }
            }
            return SWEEP_OPEN_MENU;
        }
        if (this.mWindow.mPreparedPanel != null) {
            this.mWindow.mPreparedPanel.isHandled = true;
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        Window.Callback cb = this.mWindow.getCallback();
        return (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchTouchEvent(ev) : cb.dispatchTouchEvent(ev);
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
        if (event.getKeyCode() == 4) {
            int action = event.getAction();
            if (this.mPrimaryActionMode != null) {
                if (action == 1) {
                    this.mPrimaryActionMode.finish();
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
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
        return (x < 0 || y < 0 || x > getWidth() || y > getHeight()) ? true : SWEEP_OPEN_MENU;
    }

    private boolean isOutOfBounds(int x, int y) {
        if (x < -5 || y < -5 || x > getWidth() + DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP || y > getHeight() + DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP) {
            return true;
        }
        return SWEEP_OPEN_MENU;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (this.mHasCaption && isShowingCaption() && action == 0 && isOutOfInnerBounds((int) event.getX(), (int) event.getY())) {
            return true;
        }
        if (this.mFeatureId < 0 || action != 0 || !isOutOfBounds((int) event.getX(), (int) event.getY())) {
            return SWEEP_OPEN_MENU;
        }
        this.mWindow.closePanel(this.mFeatureId);
        return true;
    }

    public void sendAccessibilityEvent(int eventType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            if ((this.mFeatureId == 0 || this.mFeatureId == 6 || this.mFeatureId == 2 || this.mFeatureId == DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP) && getChildCount() == 1) {
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

    protected boolean setFrame(int l, int t, int r, int b) {
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        int i = getResources().getConfiguration().orientation;
        boolean isPortrait = r0 == 1 ? true : SWEEP_OPEN_MENU;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        boolean fixedWidth = SWEEP_OPEN_MENU;
        this.mApplyFloatingHorizontalInsets = SWEEP_OPEN_MENU;
        if (widthMode == Integer.MIN_VALUE) {
            TypedValue tvw = isPortrait ? this.mWindow.mFixedWidthMinor : this.mWindow.mFixedWidthMajor;
            if (!(tvw == null || tvw.type == 0)) {
                int w;
                i = tvw.type;
                if (r0 == DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP) {
                    w = (int) tvw.getDimension(metrics);
                } else {
                    i = tvw.type;
                    if (r0 == 6) {
                        w = (int) tvw.getFraction((float) metrics.widthPixels, (float) metrics.widthPixels);
                    } else {
                        w = 0;
                    }
                }
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                if (w > 0) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(w, widthSize), EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    fixedWidth = true;
                } else {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec((widthSize - this.mFloatingInsets.left) - this.mFloatingInsets.right, RtlSpacingHelper.UNDEFINED);
                    this.mApplyFloatingHorizontalInsets = true;
                }
            }
        }
        this.mApplyFloatingVerticalInsets = SWEEP_OPEN_MENU;
        if (heightMode == Integer.MIN_VALUE) {
            TypedValue tvh;
            if (isPortrait) {
                tvh = this.mWindow.mFixedHeightMajor;
            } else {
                tvh = this.mWindow.mFixedHeightMinor;
            }
            if (!(tvh == null || tvh.type == 0)) {
                int h;
                i = tvh.type;
                if (r0 == DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP) {
                    h = (int) tvh.getDimension(metrics);
                } else {
                    i = tvh.type;
                    if (r0 == 6) {
                        h = (int) tvh.getFraction((float) metrics.heightPixels, (float) metrics.heightPixels);
                    } else {
                        h = 0;
                    }
                }
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                if (h > 0) {
                    heightMeasureSpec = this.mWindow.getHeightMeasureSpec(h, heightSize, MeasureSpec.makeMeasureSpec(Math.min(h, heightSize), EditorInfo.IME_FLAG_NO_ENTER_ACTION));
                } else {
                    if ((this.mWindow.getAttributes().flags & GL10.GL_DEPTH_BUFFER_BIT) == 0) {
                        heightMeasureSpec = MeasureSpec.makeMeasureSpec((heightSize - this.mFloatingInsets.top) - this.mFloatingInsets.bottom, RtlSpacingHelper.UNDEFINED);
                        this.mApplyFloatingVerticalInsets = true;
                    }
                }
            }
        }
        getOutsets(this.mOutsets);
        if (this.mOutsets.top <= 0) {
        }
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec((this.mOutsets.top + MeasureSpec.getSize(heightMeasureSpec)) + this.mOutsets.bottom, mode);
        }
        if (this.mOutsets.left <= 0) {
        }
        mode = MeasureSpec.getMode(widthMeasureSpec);
        if (mode != 0) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((this.mOutsets.left + MeasureSpec.getSize(widthMeasureSpec)) + this.mOutsets.right, mode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        boolean measure = SWEEP_OPEN_MENU;
        i = this.mWindow.getAttributes().type;
        if (r0 == 2011 && !isPortrait) {
            int sw = this.mWindow.getScreenWidth();
            if (sw > 0) {
                width = Math.min(sw, width);
                measure = true;
            }
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        if (!fixedWidth && widthMode == Integer.MIN_VALUE) {
            TypedValue tv = isPortrait ? this.mWindow.mMinWidthMinor : this.mWindow.mMinWidthMajor;
            if (tv.type != 0) {
                int min;
                i = tv.type;
                if (r0 == DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP) {
                    min = (int) tv.getDimension(metrics);
                } else {
                    i = tv.type;
                    if (r0 == 6) {
                        min = (int) tv.getFraction(this.mAvailableWidth, this.mAvailableWidth);
                    } else {
                        min = 0;
                    }
                }
                if (width < min) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(min, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                    measure = true;
                }
            }
        }
        if (measure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
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
        boolean isPopup;
        MenuHelper helper;
        if (this.mWindow.mContextMenuHelper != null) {
            this.mWindow.mContextMenuHelper.dismiss();
            this.mWindow.mContextMenuHelper = null;
        }
        PhoneWindowMenuCallback callback = this.mWindow.mContextMenuCallback;
        if (this.mWindow.mContextMenu == null) {
            this.mWindow.mContextMenu = new ContextMenuBuilder(getContext());
            this.mWindow.mContextMenu.setCallback(callback);
        } else {
            this.mWindow.mContextMenu.clearAll();
        }
        if (Float.isNaN(x) || Float.isNaN(y)) {
            isPopup = SWEEP_OPEN_MENU;
        } else {
            isPopup = true;
        }
        if (isPopup) {
            helper = this.mWindow.mContextMenu.showPopup(getContext(), originalView, x, y);
        } else {
            helper = this.mWindow.mContextMenu.showDialog(originalView, originalView.getWindowToken());
        }
        if (helper != null) {
            boolean z;
            if (isPopup) {
                z = SWEEP_OPEN_MENU;
            } else {
                z = true;
            }
            callback.setShowDialogForSubmenu(z);
            helper.setPresenterCallback(callback);
        }
        this.mWindow.mContextMenuHelper = helper;
        if (helper != null) {
            return true;
        }
        return SWEEP_OPEN_MENU;
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
        Callback2 wrappedCallback = new ActionModeCallback2Wrapper(callback);
        ActionMode mode = null;
        if (!(this.mWindow.getCallback() == null || this.mWindow.isDestroyed())) {
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

    private void cleanupFloatingActionModeViews() {
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

    void startChanging() {
        this.mChanging = true;
    }

    void finishChanging() {
        this.mChanging = SWEEP_OPEN_MENU;
        drawableChanged();
    }

    public void setWindowBackground(Drawable drawable) {
        boolean z = true;
        if (getBackground() != drawable) {
            setBackgroundDrawable(drawable);
            if (drawable != null) {
                if (!this.mWindow.isTranslucent()) {
                    z = this.mWindow.isShowingWallpaper();
                }
                this.mResizingBackgroundDrawable = enforceNonTranslucentBackground(drawable, z);
            } else {
                Context context = getContext();
                int i = this.mWindow.mBackgroundFallbackResource;
                if (!this.mWindow.isTranslucent()) {
                    z = this.mWindow.isShowingWallpaper();
                }
                this.mResizingBackgroundDrawable = getResizingBackgroundDrawable(context, 0, i, z);
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
        if (getForeground() != drawable) {
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
        LayoutParams attrs = this.mWindow.getAttributes();
        this.mFloatingInsets.setEmpty();
        if ((attrs.flags & GL10.GL_DEPTH_BUFFER_BIT) == 0) {
            if (attrs.height == -2) {
                this.mFloatingInsets.top = insets.getSystemWindowInsetTop();
                this.mFloatingInsets.bottom = insets.getSystemWindowInsetBottom();
                insets = insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), 0);
            }
            if (this.mWindow.getAttributes().width == -2) {
                this.mFloatingInsets.left = insets.getSystemWindowInsetTop();
                this.mFloatingInsets.right = insets.getSystemWindowInsetBottom();
                insets = insets.replaceSystemWindowInsets(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            }
        }
        this.mFrameOffsets.set(insets.getSystemWindowInsets());
        insets = updateStatusGuard(updateColorViews(insets, true));
        updateNavigationGuard(insets);
        if (getForeground() != null) {
            drawableChanged();
        }
        return insets;
    }

    public boolean isTransitionGroup() {
        return SWEEP_OPEN_MENU;
    }

    static int getColorViewTopInset(int stableTop, int systemTop) {
        return Math.min(stableTop, systemTop);
    }

    static int getColorViewBottomInset(int stableBottom, int systemBottom) {
        return Math.min(stableBottom, systemBottom);
    }

    static int getColorViewRightInset(int stableRight, int systemRight) {
        return Math.min(stableRight, systemRight);
    }

    static boolean isNavBarToRightEdge(int bottomInset, int rightInset) {
        return (bottomInset != 0 || rightInset <= 0) ? SWEEP_OPEN_MENU : true;
    }

    static int getNavBarSize(int bottomInset, int rightInset) {
        return isNavBarToRightEdge(bottomInset, rightInset) ? rightInset : bottomInset;
    }

    WindowInsets updateColorViews(WindowInsets insets, boolean animate) {
        boolean z;
        LayoutParams attrs = this.mWindow.getAttributes();
        int sysUiVisibility = attrs.systemUiVisibility | getWindowSystemUiVisibility();
        if (!this.mWindow.mIsFloating && ActivityManager.isHighEndGfx()) {
            boolean statusBarNeedsRightInset;
            boolean disallowAnimate = (isLaidOut() ? SWEEP_OPEN_MENU : true) | (((this.mLastWindowFlags ^ attrs.flags) & RtlSpacingHelper.UNDEFINED) != 0 ? 1 : 0);
            this.mLastWindowFlags = attrs.flags;
            if (insets != null) {
                this.mLastTopInset = getColorViewTopInset(insets.getStableInsetTop(), insets.getSystemWindowInsetTop());
                this.mLastBottomInset = getColorViewBottomInset(insets.getStableInsetBottom(), insets.getSystemWindowInsetBottom());
                this.mLastRightInset = getColorViewRightInset(insets.getStableInsetRight(), insets.getSystemWindowInsetRight());
                boolean hasTopStableInset = insets.getStableInsetTop() != 0 ? true : SWEEP_OPEN_MENU;
                disallowAnimate |= hasTopStableInset != this.mLastHasTopStableInset ? 1 : 0;
                this.mLastHasTopStableInset = hasTopStableInset;
                boolean hasBottomStableInset = insets.getStableInsetBottom() != 0 ? true : SWEEP_OPEN_MENU;
                disallowAnimate |= hasBottomStableInset != this.mLastHasBottomStableInset ? 1 : 0;
                this.mLastHasBottomStableInset = hasBottomStableInset;
                boolean hasRightStableInset = insets.getStableInsetRight() != 0 ? true : SWEEP_OPEN_MENU;
                disallowAnimate |= hasRightStableInset != this.mLastHasRightStableInset ? 1 : 0;
                this.mLastHasRightStableInset = hasRightStableInset;
                this.mLastShouldAlwaysConsumeNavBar = insets.shouldAlwaysConsumeNavBar();
            }
            boolean navBarToRightEdge = isNavBarToRightEdge(this.mLastBottomInset, this.mLastRightInset);
            int navBarSize = getNavBarSize(this.mLastBottomInset, this.mLastRightInset);
            ColorViewState colorViewState = this.mNavigationColorViewState;
            int i = this.mWindow.mNavigationBarColor;
            boolean z2 = (!animate || disallowAnimate) ? SWEEP_OPEN_MENU : true;
            updateColorViewInt(colorViewState, sysUiVisibility, i, navBarSize, navBarToRightEdge, 0, z2, SWEEP_OPEN_MENU);
            if (navBarToRightEdge) {
                statusBarNeedsRightInset = this.mNavigationColorViewState.present;
            } else {
                statusBarNeedsRightInset = SWEEP_OPEN_MENU;
            }
            int statusBarRightInset = statusBarNeedsRightInset ? this.mLastRightInset : 0;
            ColorViewState colorViewState2 = this.mStatusColorViewState;
            int calculateStatusBarColor = calculateStatusBarColor();
            int i2 = this.mLastTopInset;
            boolean z3 = (!animate || disallowAnimate) ? SWEEP_OPEN_MENU : true;
            updateColorViewInt(colorViewState2, sysUiVisibility, calculateStatusBarColor, i2, SWEEP_OPEN_MENU, statusBarRightInset, z3, this.mForceWindowDrawsStatusBarBackground);
        }
        if ((attrs.flags & RtlSpacingHelper.UNDEFINED) != 0 && (sysUiVisibility & GL10.GL_NEVER) == 0 && (sysUiVisibility & 2) == 0) {
            z = true;
        } else {
            z = this.mLastShouldAlwaysConsumeNavBar;
        }
        boolean consumingStatusBar = ((sysUiVisibility & GL10.GL_STENCIL_BUFFER_BIT) == 0 && (RtlSpacingHelper.UNDEFINED & sysUiVisibility) == 0 && (attrs.flags & GL10.GL_DEPTH_BUFFER_BIT) == 0 && (attrs.flags & Protocol.BASE_SYSTEM_RESERVED) == 0 && this.mForceWindowDrawsStatusBarBackground) ? this.mLastTopInset != 0 ? true : SWEEP_OPEN_MENU : SWEEP_OPEN_MENU;
        int consumedTop = consumingStatusBar ? this.mLastTopInset : 0;
        int consumedRight = z ? this.mLastRightInset : 0;
        int consumedBottom = z ? this.mLastBottomInset : 0;
        if (this.mContentRoot != null && (this.mContentRoot.getLayoutParams() instanceof MarginLayoutParams)) {
            ViewGroup.LayoutParams lp = (MarginLayoutParams) this.mContentRoot.getLayoutParams();
            if (lp.topMargin == consumedTop && lp.rightMargin == consumedRight) {
                if (lp.bottomMargin != consumedBottom) {
                }
                if (insets != null) {
                    insets = insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop() - consumedTop, insets.getSystemWindowInsetRight() - consumedRight, insets.getSystemWindowInsetBottom() - consumedBottom);
                }
            }
            lp.topMargin = consumedTop;
            lp.rightMargin = consumedRight;
            lp.bottomMargin = consumedBottom;
            this.mContentRoot.setLayoutParams(lp);
            if (insets == null) {
                requestApplyInsets();
            }
            if (insets != null) {
                insets = insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop() - consumedTop, insets.getSystemWindowInsetRight() - consumedRight, insets.getSystemWindowInsetBottom() - consumedBottom);
            }
        }
        if (insets != null) {
            return insets.consumeStableInsets();
        }
        return insets;
    }

    private int calculateStatusBarColor() {
        int flags = this.mWindow.getAttributes().flags;
        if ((EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS & flags) != 0) {
            return this.mSemiTransparentStatusBarColor;
        }
        if ((RtlSpacingHelper.UNDEFINED & flags) != 0) {
            return this.mWindow.getStatusBarColor();
        }
        return View.MEASURED_STATE_MASK;
    }

    private int getCurrentColor(ColorViewState state) {
        if (state.visible) {
            return state.color;
        }
        return 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateColorViewInt(ColorViewState state, int sysUiVis, int color, int size, boolean verticalBar, int rightMargin, boolean animate, boolean force) {
        boolean z;
        boolean show;
        int i;
        boolean visibilityChanged;
        View view;
        int resolvedHeight;
        int resolvedDynStatusBarWidth;
        int resolvedWidth;
        int resolvedGravity;
        FrameLayout.LayoutParams lp;
        if ((state.systemUiHideFlag & sysUiVis) == 0) {
            z = (this.mWindow.getAttributes().flags & state.hideWindowFlag) == 0 ? true : SWEEP_OPEN_MENU;
        } else {
            z = SWEEP_OPEN_MENU;
        }
        state.present = z;
        int i2 = (this.mWindow.getAttributes().flags & RtlSpacingHelper.UNDEFINED) == 0 ? force : 1;
        boolean isEmui = HwWidgetFactory.isHwTheme(getContext());
        if (!isEmui) {
            state.present &= i2;
        }
        if (!state.present || (View.MEASURED_STATE_MASK & color) == 0) {
            show = SWEEP_OPEN_MENU;
        } else {
            if ((this.mWindow.getAttributes().flags & state.translucentFlag) == 0) {
                force = true;
            }
            show = force;
        }
        boolean showView = (!show || isResizing() || size <= 0) ? SWEEP_OPEN_MENU : true;
        if (!this.mWindow.getHwFloating()) {
            if (isEmui) {
                if (this.mWindow.windowIsTranslucent()) {
                }
            }
            i = 1;
            showView = (showView & i) | this.mWindow.isSplitMode();
            visibilityChanged = SWEEP_OPEN_MENU;
            view = state.view;
            resolvedHeight = verticalBar ? -1 : size;
            resolvedDynStatusBarWidth = -1;
            i = getContext().getResources().getConfiguration().orientation;
            boolean isLand = r0 != 2 ? true : SWEEP_OPEN_MENU;
            if (this.mForcedDrawSysBarBackground && !verticalBar && isLand) {
                resolvedDynStatusBarWidth = this.mWindow.getFullScreenWidth();
            }
            resolvedWidth = verticalBar ? size : resolvedDynStatusBarWidth;
            resolvedGravity = verticalBar ? state.horizontalGravity : state.verticalGravity;
            if (view == null) {
                int vis = showView ? 0 : 4;
                i = state.targetVisibility;
                visibilityChanged = r0 == vis ? true : SWEEP_OPEN_MENU;
                state.targetVisibility = vis;
                lp = (FrameLayout.LayoutParams) view.getLayoutParams();
                i = lp.height;
                if (r0 == resolvedHeight) {
                    i = lp.width;
                    if (r0 == resolvedWidth) {
                        i = lp.gravity;
                        if (r0 == resolvedGravity) {
                            i = lp.rightMargin;
                            if (r0 != rightMargin) {
                            }
                            if (showView) {
                                view.setBackgroundColor(color);
                            }
                        }
                    }
                }
                lp.height = resolvedHeight;
                lp.width = resolvedWidth;
                lp.gravity = resolvedGravity;
                lp.rightMargin = rightMargin;
                view.setLayoutParams(lp);
                if (showView) {
                    view.setBackgroundColor(color);
                }
            } else if (showView) {
                if (HwWidgetFactory.isHwLightTheme(getContext()) && state.transitionName != null) {
                    if (state.transitionName.equals(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)) {
                        view = HwPolicyFactory.getHwNavigationBarColorView(this.mContext);
                        state.view = view;
                        view.setBackgroundColor(color);
                        view.setTransitionName(state.transitionName);
                        view.setId(state.id);
                        visibilityChanged = true;
                        view.setVisibility(4);
                        state.targetVisibility = 0;
                        lp = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
                        lp.rightMargin = rightMargin;
                        addView(view, (ViewGroup.LayoutParams) lp);
                        updateColorViewTranslations();
                    }
                }
                View view2 = new View(this.mContext);
                state.view = view2;
                view.setBackgroundColor(color);
                view.setTransitionName(state.transitionName);
                view.setId(state.id);
                visibilityChanged = true;
                view.setVisibility(4);
                state.targetVisibility = 0;
                lp = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
                lp.rightMargin = rightMargin;
                addView(view, (ViewGroup.LayoutParams) lp);
                updateColorViewTranslations();
            }
            if (visibilityChanged) {
                view.animate().cancel();
                boolean forceCloseAnimation = this.mWindow.getTryForcedCloseAnimation(WindowManagerHolder.sWindowManager, animate, getTag());
                if (animate && !forceCloseAnimation) {
                    i = this.mWindow.getAttributes().isEmuiStyle;
                    if (!(r0 == -1 || isResizing())) {
                        if (showView) {
                            view.animate().alpha(0.0f).setInterpolator(this.mHideInterpolator).setDuration((long) this.mBarEnterExitDuration).withEndAction(new AnonymousClass1(state));
                        } else {
                            if (view.getVisibility() != 0) {
                                view.setVisibility(0);
                                view.setAlpha(0.0f);
                            }
                            view.animate().alpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL).setInterpolator(this.mShowInterpolator).setDuration((long) this.mBarEnterExitDuration);
                        }
                    }
                }
                view.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                view.setVisibility(showView ? 0 : 4);
            }
            state.visible = show;
            state.color = color;
        }
        i = 0;
        showView = (showView & i) | this.mWindow.isSplitMode();
        visibilityChanged = SWEEP_OPEN_MENU;
        view = state.view;
        if (verticalBar) {
        }
        resolvedDynStatusBarWidth = -1;
        i = getContext().getResources().getConfiguration().orientation;
        if (r0 != 2) {
        }
        resolvedDynStatusBarWidth = this.mWindow.getFullScreenWidth();
        if (verticalBar) {
        }
        if (verticalBar) {
        }
        if (view == null) {
            if (showView) {
            }
            i = state.targetVisibility;
            if (r0 == vis) {
            }
            state.targetVisibility = vis;
            lp = (FrameLayout.LayoutParams) view.getLayoutParams();
            i = lp.height;
            if (r0 == resolvedHeight) {
                i = lp.width;
                if (r0 == resolvedWidth) {
                    i = lp.gravity;
                    if (r0 == resolvedGravity) {
                        i = lp.rightMargin;
                        if (r0 != rightMargin) {
                        }
                        if (showView) {
                            view.setBackgroundColor(color);
                        }
                    }
                }
            }
            lp.height = resolvedHeight;
            lp.width = resolvedWidth;
            lp.gravity = resolvedGravity;
            lp.rightMargin = rightMargin;
            view.setLayoutParams(lp);
            if (showView) {
                view.setBackgroundColor(color);
            }
        } else if (showView) {
            if (state.transitionName.equals(Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME)) {
                view = HwPolicyFactory.getHwNavigationBarColorView(this.mContext);
                state.view = view;
                view.setBackgroundColor(color);
                view.setTransitionName(state.transitionName);
                view.setId(state.id);
                visibilityChanged = true;
                view.setVisibility(4);
                state.targetVisibility = 0;
                lp = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
                lp.rightMargin = rightMargin;
                addView(view, (ViewGroup.LayoutParams) lp);
                updateColorViewTranslations();
            }
            View view22 = new View(this.mContext);
            state.view = view22;
            view.setBackgroundColor(color);
            view.setTransitionName(state.transitionName);
            view.setId(state.id);
            visibilityChanged = true;
            view.setVisibility(4);
            state.targetVisibility = 0;
            lp = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
            lp.rightMargin = rightMargin;
            addView(view, (ViewGroup.LayoutParams) lp);
            updateColorViewTranslations();
        }
        if (visibilityChanged) {
            view.animate().cancel();
            boolean forceCloseAnimation2 = this.mWindow.getTryForcedCloseAnimation(WindowManagerHolder.sWindowManager, animate, getTag());
            i = this.mWindow.getAttributes().isEmuiStyle;
            if (showView) {
                view.animate().alpha(0.0f).setInterpolator(this.mHideInterpolator).setDuration((long) this.mBarEnterExitDuration).withEndAction(new AnonymousClass1(state));
            } else {
                if (view.getVisibility() != 0) {
                    view.setVisibility(0);
                    view.setAlpha(0.0f);
                }
                view.animate().alpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL).setInterpolator(this.mShowInterpolator).setDuration((long) this.mBarEnterExitDuration);
            }
        }
        state.visible = show;
        state.color = color;
    }

    private void updateColorViewTranslations() {
        int rootScrollY = this.mRootScrollY;
        if (this.mStatusColorViewState.view != null) {
            int i;
            View view = this.mStatusColorViewState.view;
            if (rootScrollY > 0) {
                i = rootScrollY;
            } else {
                i = 0;
            }
            view.setTranslationY((float) i);
        }
        if (this.mNavigationColorViewState.view != null) {
            View view2 = this.mNavigationColorViewState.view;
            if (rootScrollY >= 0) {
                rootScrollY = 0;
            }
            view2.setTranslationY((float) rootScrollY);
        }
    }

    private WindowInsets updateStatusGuard(WindowInsets insets) {
        int i = 0;
        boolean showStatusGuard = SWEEP_OPEN_MENU;
        if (this.mPrimaryActionModeView != null && (this.mPrimaryActionModeView.getLayoutParams() instanceof MarginLayoutParams)) {
            MarginLayoutParams mlp = (MarginLayoutParams) this.mPrimaryActionModeView.getLayoutParams();
            boolean mlpChanged = SWEEP_OPEN_MENU;
            if (this.mPrimaryActionModeView.isShown()) {
                boolean nonOverlay;
                boolean z;
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
                        this.mStatusGuard.setBackgroundColor(this.mContext.getColor(R.color.input_method_navigation_guard));
                        addView(this.mStatusGuard, indexOfChild(this.mStatusColorViewState.view), new FrameLayout.LayoutParams(-1, mlp.topMargin, 8388659));
                    } else {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mStatusGuard.getLayoutParams();
                        if (lp.height != mlp.topMargin) {
                            lp.height = mlp.topMargin;
                            this.mStatusGuard.setLayoutParams(lp);
                        }
                    }
                }
                showStatusGuard = this.mStatusGuard != null ? true : SWEEP_OPEN_MENU;
                if ((this.mWindow.getLocalFeaturesPrivate() & GL10.GL_STENCIL_BUFFER_BIT) == 0) {
                    nonOverlay = true;
                } else {
                    nonOverlay = SWEEP_OPEN_MENU;
                }
                if (nonOverlay) {
                    z = showStatusGuard;
                } else {
                    z = SWEEP_OPEN_MENU;
                }
                insets = insets.consumeSystemWindowInsets(SWEEP_OPEN_MENU, z, SWEEP_OPEN_MENU, SWEEP_OPEN_MENU);
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

    private void updateNavigationGuard(WindowInsets insets) {
        if (this.mWindow.getAttributes().type == AbstractRILConstants.RIL_REQUEST_HW_DATA_CONNECTION_DETACH) {
            if (this.mWindow.mContentParent != null && (this.mWindow.mContentParent.getLayoutParams() instanceof MarginLayoutParams)) {
                MarginLayoutParams mlp = (MarginLayoutParams) this.mWindow.mContentParent.getLayoutParams();
                mlp.bottomMargin = insets.getSystemWindowInsetBottom();
                this.mWindow.mContentParent.setLayoutParams(mlp);
            }
            if (this.mNavigationGuard == null) {
                this.mNavigationGuard = new View(this.mContext);
                this.mNavigationGuard.setBackgroundColor(this.mContext.getColor(R.color.input_method_navigation_guard));
                addView(this.mNavigationGuard, indexOfChild(this.mNavigationColorViewState.view), new FrameLayout.LayoutParams(-1, insets.getSystemWindowInsetBottom(), 8388691));
            } else {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mNavigationGuard.getLayoutParams();
                lp.height = insets.getSystemWindowInsetBottom();
                this.mNavigationGuard.setLayoutParams(lp);
            }
            updateNavigationGuardColor();
        }
    }

    void updateNavigationGuardColor() {
        int i = 0;
        if (this.mNavigationGuard != null) {
            View view = this.mNavigationGuard;
            if (this.mWindow.getNavigationBarColor() == 0) {
                i = 4;
            }
            view.setVisibility(i);
        }
    }

    private void drawableChanged() {
        if (!this.mChanging) {
            setPadding(this.mFramePadding.left + this.mBackgroundPadding.left, this.mFramePadding.top + this.mBackgroundPadding.top, this.mFramePadding.right + this.mBackgroundPadding.right, this.mFramePadding.bottom + this.mBackgroundPadding.bottom);
            requestLayout();
            invalidate();
            int opacity = -1;
            if (StackId.hasWindowShadow(this.mStackId)) {
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
        if (!(!this.mWindow.hasFeature(0) || hasWindowFocus || this.mWindow.mPanelChordingKey == 0)) {
            this.mWindow.closePanel(0);
        }
        Window.Callback cb = this.mWindow.getCallback();
        if (!(cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0)) {
            cb.onWindowFocusChanged(hasWindowFocus);
        }
        if (this.mPrimaryActionMode != null) {
            this.mPrimaryActionMode.onWindowFocusChanged(hasWindowFocus);
        }
        if (this.mFloatingActionMode != null) {
            this.mFloatingActionMode.onWindowFocusChanged(hasWindowFocus);
        }
        updateElevation();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window.Callback cb = this.mWindow.getCallback();
        if (!(cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0)) {
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
    }

    protected void onDetachedFromWindow() {
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
        PanelFeatureState st = this.mWindow.getPanelState(0, SWEEP_OPEN_MENU);
        if (!(st == null || st.menu == null || this.mFeatureId >= 0)) {
            st.menu.close();
        }
        releaseThreadedRenderer();
        if (this.mWindowResizeCallbacksAdded) {
            getViewRootImpl().removeWindowCallbacks(this);
            this.mWindowResizeCallbacksAdded = SWEEP_OPEN_MENU;
        }
    }

    public void onCloseSystemDialogs(String reason) {
        if (this.mFeatureId >= 0) {
            this.mWindow.closeAllPanels();
        }
    }

    public SurfaceHolder.Callback2 willYouTakeTheSurface() {
        return this.mFeatureId < 0 ? this.mWindow.mTakeSurfaceCallback : null;
    }

    public InputQueue.Callback willYouTakeTheInputQueue() {
        return this.mFeatureId < 0 ? this.mWindow.mTakeInputQueueCallback : null;
    }

    public void setSurfaceType(int type) {
        this.mWindow.setType(type);
    }

    public void setSurfaceFormat(int format) {
        this.mWindow.setFormat(format);
    }

    public void setSurfaceKeepScreenOn(boolean keepOn) {
        if (keepOn) {
            this.mWindow.addFlags(LogPower.START_CHG_ROTATION);
        } else {
            this.mWindow.clearFlags(LogPower.START_CHG_ROTATION);
        }
    }

    public void onRootViewScrollYChanged(int rootScrollY) {
        this.mRootScrollY = rootScrollY;
        updateColorViewTranslations();
    }

    private ActionMode createActionMode(int type, Callback2 callback, View originatingView) {
        switch (type) {
            case HwCfgFilePolicy.EMUI /*1*/:
                return createFloatingActionMode(originatingView, callback);
            default:
                return createStandaloneActionMode(callback);
        }
    }

    private void setHandledActionMode(ActionMode mode) {
        if (mode.getType() == 0) {
            setHandledPrimaryActionMode(mode);
        } else if (mode.getType() == 1) {
            setHandledFloatingActionMode(mode);
        }
    }

    private ActionMode createStandaloneActionMode(ActionMode.Callback callback) {
        endOnGoingFadeAnimation();
        cleanupPrimaryActionMode();
        if (this.mPrimaryActionModeView == null || !this.mPrimaryActionModeView.isAttachedToWindow()) {
            if (this.mWindow.isFloating()) {
                Context actionBarContext;
                TypedValue outValue = new TypedValue();
                Theme baseTheme = this.mContext.getTheme();
                baseTheme.resolveAttribute(R.attr.actionBarTheme, outValue, true);
                if (outValue.resourceId != 0) {
                    Theme actionBarTheme = this.mContext.getResources().newTheme();
                    actionBarTheme.setTo(baseTheme);
                    actionBarTheme.applyStyle(outValue.resourceId, true);
                    actionBarContext = new ContextThemeWrapper(this.mContext, 0);
                    actionBarContext.getTheme().setTo(actionBarTheme);
                } else {
                    actionBarContext = this.mContext;
                }
                this.mPrimaryActionModeView = new ActionBarContextView(actionBarContext);
                this.mPrimaryActionModePopup = new PopupWindow(actionBarContext, null, (int) R.attr.actionModePopupWindowStyle);
                this.mPrimaryActionModePopup.setWindowLayoutType(2);
                this.mPrimaryActionModePopup.setContentView(this.mPrimaryActionModeView);
                this.mPrimaryActionModePopup.setWidth(-1);
                actionBarContext.getTheme().resolveAttribute(R.attr.actionBarSize, outValue, true);
                this.mPrimaryActionModeView.setContentHeight(TypedValue.complexToDimensionPixelSize(outValue.data, actionBarContext.getResources().getDisplayMetrics()));
                this.mPrimaryActionModePopup.setHeight(-2);
                this.mShowPrimaryActionModePopup = new Runnable() {
                    public void run() {
                        DecorView.this.mPrimaryActionModePopup.showAtLocation(DecorView.this.mPrimaryActionModeView.getApplicationWindowToken(), 55, 0, 0);
                        DecorView.this.endOnGoingFadeAnimation();
                        if (DecorView.this.shouldAnimatePrimaryActionModeView()) {
                            DecorView.this.mFadeAnim = ObjectAnimator.ofFloat(DecorView.this.mPrimaryActionModeView, View.ALPHA, new float[]{0.0f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
                            DecorView.this.mFadeAnim.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationStart(Animator animation) {
                                    DecorView.this.mPrimaryActionModeView.setVisibility(0);
                                }

                                public void onAnimationEnd(Animator animation) {
                                    DecorView.this.mPrimaryActionModeView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                                    DecorView.this.mFadeAnim = null;
                                }
                            });
                            DecorView.this.mFadeAnim.start();
                            return;
                        }
                        DecorView.this.mPrimaryActionModeView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                        DecorView.this.mPrimaryActionModeView.setVisibility(0);
                    }
                };
            } else {
                ViewStub stub = (ViewStub) findViewById(R.id.action_mode_bar_stub);
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
        boolean z;
        this.mPrimaryActionModeView.killMode();
        Context context = this.mPrimaryActionModeView.getContext();
        ActionBarContextView actionBarContextView = this.mPrimaryActionModeView;
        if (this.mPrimaryActionModePopup == null) {
            z = true;
        } else {
            z = SWEEP_OPEN_MENU;
        }
        return new StandaloneActionMode(context, actionBarContextView, callback, z);
    }

    private void endOnGoingFadeAnimation() {
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
            this.mFadeAnim = ObjectAnimator.ofFloat(this.mPrimaryActionModeView, View.ALPHA, new float[]{0.0f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
            this.mFadeAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    DecorView.this.mPrimaryActionModeView.setVisibility(0);
                }

                public void onAnimationEnd(Animator animation) {
                    DecorView.this.mPrimaryActionModeView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
                    DecorView.this.mFadeAnim = null;
                }
            });
            this.mFadeAnim.start();
        } else {
            this.mPrimaryActionModeView.setAlpha(LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
            this.mPrimaryActionModeView.setVisibility(0);
        }
        this.mPrimaryActionModeView.sendAccessibilityEvent(32);
    }

    boolean shouldAnimatePrimaryActionModeView() {
        return isLaidOut();
    }

    private ActionMode createFloatingActionMode(View originatingView, Callback2 callback) {
        if (this.mFloatingActionMode != null) {
            this.mFloatingActionMode.finish();
        }
        cleanupFloatingActionModeViews();
        FloatingActionMode mode = new FloatingActionMode(this.mContext, callback, originatingView);
        this.mFloatingActionModeOriginatingView = originatingView;
        this.mFloatingToolbarPreDrawListener = new AnonymousClass4(mode);
        return mode;
    }

    private void setHandledFloatingActionMode(ActionMode mode) {
        this.mFloatingActionMode = mode;
        this.mFloatingToolbar = this.mWindow.getFloatingToolbar(this.mContext, this.mWindow);
        ((FloatingActionMode) this.mFloatingActionMode).setFloatingToolbar(this.mFloatingToolbar);
        this.mFloatingActionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    void enableCaption(boolean attachedAndVisible) {
        if (this.mHasCaption != attachedAndVisible) {
            this.mHasCaption = attachedAndVisible;
            if (getForeground() != null) {
                drawableChanged();
            }
        }
    }

    void setWindow(PhoneWindow phoneWindow) {
        this.mWindow = phoneWindow;
        Context context = getContext();
        if (context instanceof DecorContext) {
            ((DecorContext) context).setPhoneWindow(this.mWindow);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int workspaceId = getStackId();
        if (this.mStackId != workspaceId) {
            this.mStackId = workspaceId;
            if (this.mDecorCaptionView == null && StackId.hasWindowDecor(this.mStackId)) {
                this.mDecorCaptionView = createDecorCaptionView(this.mWindow.getLayoutInflater());
                if (this.mDecorCaptionView != null) {
                    if (this.mDecorCaptionView.getParent() == null) {
                        addView(this.mDecorCaptionView, 0, new ViewGroup.LayoutParams(-1, -1));
                    }
                    removeView(this.mContentRoot);
                    this.mDecorCaptionView.addView(this.mContentRoot, new MarginLayoutParams(-1, -1));
                }
            } else if (this.mDecorCaptionView != null) {
                this.mDecorCaptionView.onConfigurationChanged(StackId.hasWindowDecor(this.mStackId));
                enableCaption(StackId.hasWindowDecor(workspaceId));
            }
        }
        updateAvailableWidth();
        initializeElevation();
    }

    void onResourcesLoaded(LayoutInflater inflater, int layoutResource) {
        this.mStackId = getStackId();
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
            this.mDecorCaptionView.addView(root, new MarginLayoutParams(-1, -1));
        } else {
            addView(root, 0, new ViewGroup.LayoutParams(-1, -1));
        }
        this.mContentRoot = (ViewGroup) root;
        initializeElevation();
    }

    private void loadBackgroundDrawablesIfNeeded() {
        if (this.mResizingBackgroundDrawable == null) {
            this.mResizingBackgroundDrawable = getResizingBackgroundDrawable(getContext(), this.mWindow.mBackgroundResource, this.mWindow.mBackgroundFallbackResource, !this.mWindow.isTranslucent() ? this.mWindow.isShowingWallpaper() : true);
            if (this.mResizingBackgroundDrawable == null) {
                Log.w(this.mLogTag, "Failed to find background drawable for PhoneWindow=" + this.mWindow);
            }
        }
        if (this.mCaptionBackgroundDrawable == null) {
            this.mCaptionBackgroundDrawable = getContext().getDrawable(R.drawable.decor_caption_title_focused);
        }
        if (this.mResizingBackgroundDrawable != null) {
            this.mLastBackgroundDrawableCb = this.mResizingBackgroundDrawable.getCallback();
            this.mResizingBackgroundDrawable.setCallback(null);
        }
    }

    private DecorCaptionView createDecorCaptionView(LayoutInflater inflater) {
        boolean z = true;
        DecorCaptionView decorCaptionView = null;
        for (int i = getChildCount() - 1; i >= 0 && decorCaptionView == null; i--) {
            View view = getChildAt(i);
            if (view instanceof DecorCaptionView) {
                decorCaptionView = (DecorCaptionView) view;
                removeViewAt(i);
            }
        }
        LayoutParams attrs = this.mWindow.getAttributes();
        boolean isApplication = attrs.type != 1 ? attrs.type == 2 ? true : SWEEP_OPEN_MENU : true;
        if (!this.mWindow.isFloating() && isApplication && StackId.hasWindowDecor(this.mStackId)) {
            if (decorCaptionView == null) {
                decorCaptionView = inflateDecorCaptionView(inflater);
            }
            decorCaptionView.setPhoneWindow(this.mWindow, true);
        } else {
            decorCaptionView = null;
        }
        if (decorCaptionView == null) {
            z = SWEEP_OPEN_MENU;
        }
        enableCaption(z);
        return decorCaptionView;
    }

    private DecorCaptionView inflateDecorCaptionView(LayoutInflater inflater) {
        Context context = getContext();
        DecorCaptionView view = (DecorCaptionView) LayoutInflater.from(context).inflate((int) R.layout.decor_caption, null);
        setDecorCaptionShade(context, view);
        return view;
    }

    private void setDecorCaptionShade(Context context, DecorCaptionView view) {
        switch (this.mWindow.getDecorCaptionShade()) {
            case HwCfgFilePolicy.EMUI /*1*/:
                setLightDecorCaptionShade(view);
            case HwCfgFilePolicy.PC /*2*/:
                setDarkDecorCaptionShade(view);
            default:
                TypedValue value = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.colorPrimary, value, true);
                if (((double) Color.luminance(value.data)) < 0.5d) {
                    setLightDecorCaptionShade(view);
                } else {
                    setDarkDecorCaptionShade(view);
                }
        }
    }

    void updateDecorCaptionShade() {
        if (this.mDecorCaptionView != null) {
            setDecorCaptionShade(getContext(), this.mDecorCaptionView);
        }
    }

    private void setLightDecorCaptionShade(DecorCaptionView view) {
        view.findViewById(R.id.maximize_window).setBackgroundResource(R.drawable.decor_maximize_button_light);
        view.findViewById(R.id.close_window).setBackgroundResource(R.drawable.decor_close_button_light);
    }

    private void setDarkDecorCaptionShade(DecorCaptionView view) {
        view.findViewById(R.id.maximize_window).setBackgroundResource(R.drawable.decor_maximize_button_dark);
        view.findViewById(R.id.close_window).setBackgroundResource(R.drawable.decor_close_button_dark);
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
        return new ColorDrawable(View.MEASURED_STATE_MASK);
    }

    private static Drawable enforceNonTranslucentBackground(Drawable drawable, boolean windowTranslucent) {
        if (!windowTranslucent && (drawable instanceof ColorDrawable)) {
            ColorDrawable colorDrawable = (ColorDrawable) drawable;
            int color = colorDrawable.getColor();
            if (Color.alpha(color) != MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
                ColorDrawable copy = (ColorDrawable) colorDrawable.getConstantState().newDrawable().mutate();
                copy.setColor(Color.argb(MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE, Color.red(color), Color.green(color), Color.blue(color)));
                return copy;
            }
        }
        return drawable;
    }

    private int getStackId() {
        int workspaceId = -1;
        WindowControllerCallback callback = this.mWindow.getWindowControllerCallback();
        if (callback != null) {
            try {
                workspaceId = callback.getWindowStackId();
            } catch (RemoteException e) {
                Log.e(this.mLogTag, "Failed to get the workspace ID of a PhoneWindow.");
            }
        }
        if (workspaceId == -1) {
            return 1;
        }
        return workspaceId;
    }

    void clearContentView() {
        if (this.mDecorCaptionView != null) {
            this.mDecorCaptionView.removeContentView();
            return;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (!(v == this.mStatusColorViewState.view || v == this.mNavigationColorViewState.view || v == this.mStatusGuard || v == this.mNavigationGuard)) {
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
            ThreadedRenderer renderer = getHardwareRenderer();
            if (renderer != null) {
                loadBackgroundDrawablesIfNeeded();
                this.mBackdropFrameRenderer = new BackdropFrameRenderer(this, renderer, initialBounds, this.mResizingBackgroundDrawable, this.mCaptionBackgroundDrawable, this.mUserCaptionBackgroundDrawable, getCurrentColor(this.mStatusColorViewState), getCurrentColor(this.mNavigationColorViewState), fullscreen, systemInsets, stableInsets, resizeMode);
                updateElevation();
                updateColorViews(null, SWEEP_OPEN_MENU);
            }
            this.mResizeMode = resizeMode;
            getViewRootImpl().requestInvalidateRootRenderNode();
        }
    }

    public void onWindowDragResizeEnd() {
        releaseThreadedRenderer();
        updateColorViews(null, SWEEP_OPEN_MENU);
        this.mResizeMode = -1;
        getViewRootImpl().requestInvalidateRootRenderNode();
    }

    public boolean onContentDrawn(int offsetX, int offsetY, int sizeX, int sizeY) {
        if (this.mBackdropFrameRenderer == null) {
            return SWEEP_OPEN_MENU;
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
        int middleColor = (this.mContext.getResources().getColor(R.color.resize_shadow_start_color, null) + this.mContext.getResources().getColor(R.color.resize_shadow_end_color, null)) / 2;
        this.mHorizontalResizeShadowPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mResizeShadowSize, new int[]{startColor, middleColor, endColor}, new float[]{0.0f, 0.3f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL}, TileMode.CLAMP));
        this.mVerticalResizeShadowPaint.setShader(new LinearGradient(0.0f, 0.0f, (float) this.mResizeShadowSize, 0.0f, new int[]{startColor, middleColor, endColor}, new float[]{0.0f, 0.3f, LayoutParams.BRIGHTNESS_OVERRIDE_FULL}, TileMode.CLAMP));
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
        return this.mBackdropFrameRenderer != null ? true : SWEEP_OPEN_MENU;
    }

    private void initializeElevation() {
        this.mAllowUpdateElevation = SWEEP_OPEN_MENU;
        updateElevation();
    }

    private void updateElevation() {
        float elevation = 0.0f;
        boolean wasAdjustedForStack = this.mElevationAdjustedForStack;
        if (!StackId.hasWindowShadow(this.mStackId) || isResizing()) {
            this.mElevationAdjustedForStack = SWEEP_OPEN_MENU;
        } else {
            elevation = (float) (hasWindowFocus() ? DECOR_SHADOW_FOCUSED_HEIGHT_IN_DIP : DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP);
            if (!(this.mAllowUpdateElevation || this.mStackId == 4)) {
                elevation = 20.0f;
            }
            elevation = dipToPx(elevation);
            this.mElevationAdjustedForStack = true;
        }
        if ((wasAdjustedForStack || this.mElevationAdjustedForStack) && getElevation() != elevation) {
            this.mWindow.setElevation(elevation);
        }
    }

    boolean isShowingCaption() {
        return this.mDecorCaptionView != null ? this.mDecorCaptionView.isCaptionShowing() : SWEEP_OPEN_MENU;
    }

    int getCaptionHeight() {
        return isShowingCaption() ? this.mDecorCaptionView.getCaptionHeight() : 0;
    }

    private float dipToPx(float dip) {
        return TypedValue.applyDimension(1, dip, getResources().getDisplayMetrics());
    }

    void setUserCaptionBackgroundDrawable(Drawable drawable) {
        this.mUserCaptionBackgroundDrawable = drawable;
        if (this.mBackdropFrameRenderer != null) {
            this.mBackdropFrameRenderer.setUserCaptionBackgroundDrawable(drawable);
        }
    }

    private static String getTitleSuffix(LayoutParams params) {
        if (params == null) {
            return "";
        }
        String[] split = params.getTitle().toString().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return "";
    }

    void updateLogTag(LayoutParams params) {
        this.mLogTag = "DecorView[" + getTitleSuffix(params) + "]";
    }

    public void updateAvailableWidth() {
        Resources res = getResources();
        this.mAvailableWidth = TypedValue.applyDimension(1, (float) res.getConfiguration().screenWidthDp, res.getDisplayMetrics());
    }

    public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
        PanelFeatureState st = this.mWindow.getPanelState(0, SWEEP_OPEN_MENU);
        if (!this.mWindow.isDestroyed() && st != null && this.mWindow.getCallback() != null) {
            this.mWindow.getCallback().onProvideKeyboardShortcuts(list, st.menu, deviceId);
        }
    }

    public String toString() {
        return "DecorView@" + Integer.toHexString(hashCode()) + "[" + getTitleSuffix(this.mWindow.getAttributes()) + "]";
    }
}
