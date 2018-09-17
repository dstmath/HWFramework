package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.AppOpsManager;
import android.aps.IApsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.os.Binder;
import android.os.Debug;
import android.os.PowerManager.WakeLock;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.IWindowFocusObserver;
import android.view.IWindowId.Stub;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.WindowInfo;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IHwWindowStateAnimator;
import com.android.server.LocalServices;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.input.InputWindowHandle;
import com.android.server.power.IHwShutdownThread;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.Predicate;

public class WindowState extends WindowContainer<WindowState> implements android.view.WindowManagerPolicy.WindowState {
    private static final boolean DEBUG_DISABLE_SAVING_SURFACES = ActivityManager.ENABLE_TASK_SNAPSHOTS;
    private static final boolean IS_FULL_SCREEN = (!SystemProperties.get("ro.config.hw_screen_aspect", "").equals(""));
    public static final int LOW_RESOLUTION_COMPOSITION_OFF = 1;
    public static final int LOW_RESOLUTION_COMPOSITION_ON = 2;
    public static final int LOW_RESOLUTION_FEATURE_OFF = 0;
    static final int MINIMUM_VISIBLE_HEIGHT_IN_DP = 36;
    static final int MINIMUM_VISIBLE_WIDTH_IN_DP = 42;
    private static final String PROPERTY_START_WINDOWS_AHEAD = "persist.start_windows_ahead";
    static final int RESIZE_HANDLE_WIDTH_IN_DP = 10;
    static final boolean SHOW_TRANSACTIONS = false;
    static final String TAG = "WindowManager";
    private static final Region sEmptyRegion = new Region();
    private static final Comparator<WindowState> sWindowSubLayerComparator = new Comparator<WindowState>() {
        public int compare(WindowState w1, WindowState w2) {
            int layer1 = w1.mSubLayer;
            int layer2 = w2.mSubLayer;
            if (layer1 < layer2 || (layer1 == layer2 && layer2 < 0)) {
                return -1;
            }
            return 1;
        }
    };
    boolean isBinderDiedCalling = false;
    private boolean mAnimateReplacingWindow = false;
    boolean mAnimatingExit;
    private boolean mAnimatingWithSavedSurface;
    boolean mAppDied;
    boolean mAppFreezing;
    final int mAppOp;
    public boolean mAppOpVisibility = true;
    AppWindowToken mAppToken;
    public final LayoutParams mAttrs = new LayoutParams();
    final int mBaseLayer;
    private boolean mCanCarryColors;
    final IWindow mClient;
    private InputChannel mClientChannel;
    final Rect mCompatFrame = new Rect();
    final Rect mContainingFrame = new Rect();
    boolean mContentChanged;
    private final Rect mContentFrame = new Rect();
    final Rect mContentInsets = new Rect();
    private boolean mContentInsetsChanged;
    final Context mContext;
    private DeadWindowEventReceiver mDeadWindowEventReceiver;
    final DeathRecipient mDeathRecipient;
    final Rect mDecorFrame = new Rect();
    boolean mDestroying;
    final Rect mDisplayFrame = new Rect();
    private boolean mDragResizing;
    private boolean mDragResizingChangeReported = true;
    private WakeLock mDrawLock;
    boolean mEnforceSizeCompat;
    private RemoteCallbackList<IWindowFocusObserver> mFocusCallbacks;
    int mForceCompatMode = 0;
    private boolean mForceHideNonSystemOverlayWindow;
    final Rect mFrame = new Rect();
    private boolean mFrameSizeChanged = false;
    final Rect mGivenContentInsets = new Rect();
    boolean mGivenInsetsPending;
    final Region mGivenTouchableRegion = new Region();
    final Rect mGivenVisibleInsets = new Rect();
    float mGlobalScale = 1.0f;
    float mHScale = 1.0f;
    boolean mHasSurface = false;
    boolean mHaveFrame;
    boolean mHidden;
    boolean mInRelayout;
    InputChannel mInputChannel;
    final InputWindowHandle mInputWindowHandle;
    private final Rect mInsetFrame = new Rect();
    float mInvGlobalScale = 1.0f;
    private boolean mIsChildWindow;
    private final boolean mIsFloatingLayer;
    final boolean mIsImWindow;
    final boolean mIsWallpaper;
    final Rect mLastContentInsets = new Rect();
    final Rect mLastFrame = new Rect();
    int mLastFreezeDuration;
    float mLastHScale = 1.0f;
    private final Rect mLastOutsets = new Rect();
    private final Rect mLastOverscanInsets = new Rect();
    final Rect mLastRelayoutContentInsets = new Rect();
    private final MergedConfiguration mLastReportedConfiguration = new MergedConfiguration();
    private int mLastRequestedHeight;
    private int mLastRequestedWidth;
    private final Rect mLastStableInsets = new Rect();
    final Rect mLastSurfaceInsets = new Rect();
    CharSequence mLastTitle;
    float mLastVScale = 1.0f;
    private final Rect mLastVisibleInsets = new Rect();
    int mLastVisibleLayoutRotation = -1;
    int mLayer;
    final boolean mLayoutAttached;
    boolean mLayoutNeeded;
    int mLayoutSeq = -1;
    private boolean mMovedByResize;
    boolean mObscured;
    boolean mOrientationChanging;
    private final Rect mOutsetFrame = new Rect();
    final Rect mOutsets = new Rect();
    private boolean mOutsetsChanged = false;
    private final Rect mOverscanFrame = new Rect();
    final Rect mOverscanInsets = new Rect();
    private boolean mOverscanInsetsChanged;
    final boolean mOwnerCanAddInternalSystemWindow;
    final int mOwnerUid;
    private final Rect mParentFrame = new Rect();
    boolean mPermanentlyHidden;
    final WindowManagerPolicy mPolicy;
    boolean mPolicyVisibility = true;
    boolean mPolicyVisibilityAfterAnim = true;
    boolean mRelayoutCalled;
    boolean mRemoveOnExit;
    boolean mRemoved;
    private WindowState mReplacementWindow = null;
    private boolean mReplacingRemoveRequested = false;
    boolean mReportOrientationChanged;
    int mRequestedHeight;
    int mRequestedWidth;
    private int mResizeMode;
    boolean mResizedWhileGone = false;
    private boolean mResizedWhileNotDragResizing;
    private boolean mResizedWhileNotDragResizingReported;
    boolean mSeamlesslyRotated = false;
    int mSeq;
    final WindowManagerService mService;
    final Session mSession;
    private boolean mShowToOwnerOnly;
    final Point mShownPosition = new Point();
    boolean mSkipEnterAnimationForSeamlessReplacement = false;
    private final Rect mStableFrame = new Rect();
    final Rect mStableInsets = new Rect();
    private boolean mStableInsetsChanged;
    private boolean mStartWindowsAhead = false;
    private String mStringNameCache;
    final int mSubLayer;
    private boolean mSurfaceSaved = false;
    int mSystemUiVisibility;
    final Matrix mTmpMatrix = new Matrix();
    private final Rect mTmpRect = new Rect();
    WindowToken mToken;
    int mTouchableInsets = 0;
    boolean mTurnOnScreen;
    float mVScale = 1.0f;
    int mViewVisibility;
    final Rect mVisibleFrame = new Rect();
    final Rect mVisibleInsets = new Rect();
    private boolean mVisibleInsetsChanged;
    int mWallpaperDisplayOffsetX = Integer.MIN_VALUE;
    int mWallpaperDisplayOffsetY = Integer.MIN_VALUE;
    boolean mWallpaperVisible;
    float mWallpaperX = -1.0f;
    float mWallpaperXStep = -1.0f;
    float mWallpaperY = -1.0f;
    float mWallpaperYStep = -1.0f;
    private boolean mWasExiting;
    private boolean mWasVisibleBeforeClientHidden;
    boolean mWillReplaceWindow = false;
    final WindowStateAnimator mWinAnimator;
    final WindowId mWindowId;
    boolean mWindowRemovalAllowed;
    int mXOffset;
    int mYOffset;

    private final class DeadWindowEventReceiver extends InputEventReceiver {
        DeadWindowEventReceiver(InputChannel inputChannel) {
            super(inputChannel, WindowState.this.mService.mH.getLooper());
        }

        public void onInputEvent(InputEvent event) {
            finishInputEvent(event, true);
        }
    }

    private class DeathRecipient implements android.os.IBinder.DeathRecipient {
        /* synthetic */ DeathRecipient(WindowState this$0, DeathRecipient -this1) {
            this();
        }

        private DeathRecipient() {
        }

        public void binderDied() {
            try {
                synchronized (WindowState.this.mService.mWindowMap) {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState win = WindowState.this.mService.windowForClientLocked(WindowState.this.mSession, WindowState.this.mClient, false);
                    Slog.i(WindowState.TAG, "WIN DEATH: " + win);
                    if (win != null) {
                        DisplayContent dc = WindowState.this.getDisplayContent();
                        if (win.mAppToken != null && win.mAppToken.findMainWindow() == win) {
                            WindowState.this.mService.mTaskSnapshotController.onAppDied(win.mAppToken);
                        }
                        WindowState.this.isBinderDiedCalling = true;
                        win.removeIfPossible(WindowState.this.shouldKeepVisibleDeadAppWindow());
                        if (win.mAttrs.type == 2034) {
                            TaskStack stack = dc.getDockedStackIgnoringVisibility();
                            if (stack != null) {
                                stack.resetDockedStackToMiddle();
                            }
                            WindowState.this.mService.setDockedStackResizing(false);
                        }
                    } else if (WindowState.this.mHasSurface) {
                        Slog.e(WindowState.TAG, "!!! LEAK !!! Window removed but surface still valid.");
                        WindowState.this.removeIfPossible();
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
            } catch (IllegalArgumentException e) {
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    static final class UpdateReportedVisibilityResults {
        boolean nowGone = true;
        int numDrawn;
        int numInteresting;
        int numVisible;

        UpdateReportedVisibilityResults() {
        }

        void reset() {
            this.numInteresting = 0;
            this.numVisible = 0;
            this.numDrawn = 0;
            this.nowGone = true;
        }
    }

    private static final class WindowId extends Stub {
        private final WeakReference<WindowState> mOuter;

        /* synthetic */ WindowId(WindowState outer, WindowId -this1) {
            this(outer);
        }

        private WindowId(WindowState outer) {
            this.mOuter = new WeakReference(outer);
        }

        public void registerFocusObserver(IWindowFocusObserver observer) {
            WindowState outer = (WindowState) this.mOuter.get();
            if (outer != null) {
                outer.registerFocusObserver(observer);
            }
        }

        public void unregisterFocusObserver(IWindowFocusObserver observer) {
            WindowState outer = (WindowState) this.mOuter.get();
            if (outer != null) {
                outer.unregisterFocusObserver(observer);
            }
        }

        public boolean isFocused() {
            WindowState outer = (WindowState) this.mOuter.get();
            return outer != null ? outer.isFocused() : false;
        }
    }

    WindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow, int forceCompatFlag) {
        this.mService = service;
        this.mSession = s;
        this.mClient = c;
        this.mAppOp = appOp;
        this.mToken = token;
        this.mAppToken = this.mToken.asAppWindowToken();
        this.mOwnerUid = ownerId;
        this.mOwnerCanAddInternalSystemWindow = ownerCanAddInternalSystemWindow;
        this.mWindowId = new WindowId(this, null);
        this.mAttrs.copyFrom(a);
        this.mViewVisibility = viewVisibility;
        this.mPolicy = this.mService.mPolicy;
        this.mContext = this.mService.mContext;
        DeathRecipient deathRecipient = new DeathRecipient(this, null);
        this.mSeq = seq;
        this.mEnforceSizeCompat = (this.mAttrs.privateFlags & 128) != 0;
        try {
            c.asBinder().linkToDeath(deathRecipient, 0);
            this.mDeathRecipient = deathRecipient;
            boolean z;
            if (this.mAttrs.type < 1000 || this.mAttrs.type > 1999) {
                this.mBaseLayer = (this.mPolicy.getWindowLayerLw(this) * 10000) + 1000;
                this.mSubLayer = 0;
                this.mIsChildWindow = false;
                this.mLayoutAttached = false;
                z = this.mAttrs.type != 2011 ? this.mAttrs.type == 2012 : true;
                this.mIsImWindow = z;
                this.mIsWallpaper = this.mAttrs.type == 2013;
            } else {
                this.mBaseLayer = (this.mPolicy.getWindowLayerLw(parentWindow) * 10000) + 1000;
                this.mSubLayer = this.mPolicy.getSubWindowLayerFromTypeLw(a.type);
                this.mIsChildWindow = true;
                Slog.v(TAG, "Adding " + this + " to " + parentWindow);
                parentWindow.addChild((WindowContainer) this, sWindowSubLayerComparator);
                this.mLayoutAttached = this.mAttrs.type != 1003;
                z = parentWindow.mAttrs.type != 2011 ? parentWindow.mAttrs.type == 2012 : true;
                this.mIsImWindow = z;
                if (parentWindow.mAttrs.type == 2013) {
                    z = true;
                } else {
                    z = false;
                }
                this.mIsWallpaper = z;
            }
            this.mIsFloatingLayer = !this.mIsImWindow ? this.mIsWallpaper : true;
            if (forceCompatFlag == -3) {
                this.mForceCompatMode = getTopParentWindow().mForceCompatMode;
            } else {
                this.mForceCompatMode = forceCompatFlag;
            }
            if (this.mAppToken != null && this.mAppToken.mShowForAllUsers) {
                LayoutParams layoutParams = this.mAttrs;
                layoutParams.flags |= DumpState.DUMP_FROZEN;
            }
            IHwWindowStateAnimator iwsa = HwServiceFactory.getHuaweiWindowStateAnimator();
            if (iwsa != null) {
                this.mWinAnimator = iwsa.getInstance(this);
            } else {
                this.mWinAnimator = new WindowStateAnimator(this);
            }
            this.mWinAnimator.mAlpha = a.alpha;
            this.mRequestedWidth = 0;
            this.mRequestedHeight = 0;
            this.mLastRequestedWidth = 0;
            this.mLastRequestedHeight = 0;
            this.mXOffset = 0;
            this.mYOffset = 0;
            this.mLayer = 0;
            this.mInputWindowHandle = new InputWindowHandle(this.mAppToken != null ? this.mAppToken.mInputApplicationHandle : null, this, c, getDisplayId());
            this.mStartWindowsAhead = SystemProperties.getBoolean(PROPERTY_START_WINDOWS_AHEAD, false);
        } catch (RemoteException e) {
            this.mDeathRecipient = null;
            this.mIsChildWindow = false;
            this.mLayoutAttached = false;
            this.mIsImWindow = false;
            this.mIsWallpaper = false;
            this.mIsFloatingLayer = false;
            this.mBaseLayer = 0;
            this.mSubLayer = 0;
            this.mInputWindowHandle = null;
            this.mWinAnimator = null;
        }
    }

    void attach() {
        this.mSession.windowAddedLocked(this.mAttrs.packageName);
    }

    public int getOwningUid() {
        return this.mOwnerUid;
    }

    public String getOwningPackage() {
        return this.mAttrs.packageName;
    }

    public boolean canAddInternalSystemWindow() {
        return this.mOwnerCanAddInternalSystemWindow;
    }

    private void subtractInsets(Rect frame, Rect layoutFrame, Rect insetFrame, Rect displayFrame) {
        frame.inset(Math.max(0, insetFrame.left - Math.max(layoutFrame.left, displayFrame.left)), Math.max(0, insetFrame.top - Math.max(layoutFrame.top, displayFrame.top)), Math.max(0, Math.min(layoutFrame.right, displayFrame.right) - insetFrame.right), Math.max(0, Math.min(layoutFrame.bottom, displayFrame.bottom) - insetFrame.bottom));
    }

    public void computeFrameLw(Rect parentFrame, Rect displayFrame, Rect overscanFrame, Rect contentFrame, Rect visibleFrame, Rect decorFrame, Rect stableFrame, Rect outsetFrame) {
        if (!this.mWillReplaceWindow || (!this.mAnimatingExit && (this.mReplacingRemoveRequested ^ 1) == 0)) {
            Rect layoutDisplayFrame;
            Rect layoutContainingFrame;
            int layoutXDiff;
            int layoutYDiff;
            this.mHaveFrame = true;
            Task task = getTask();
            boolean inFullscreenContainer = inFullscreenContainer();
            int windowsAreFloating = task != null ? task.isFloating() : 0;
            DisplayContent dc = getDisplayContent();
            if (inFullscreenContainer) {
                this.mInsetFrame.setEmpty();
            } else if (task != null && isInMultiWindowMode()) {
                task.getTempInsetBounds(this.mInsetFrame);
            }
            if (inFullscreenContainer || layoutInParentFrame()) {
                this.mContainingFrame.set(parentFrame);
                this.mDisplayFrame.set(displayFrame);
                layoutDisplayFrame = displayFrame;
                layoutContainingFrame = parentFrame;
                layoutXDiff = 0;
                layoutYDiff = 0;
            } else {
                getContainerBounds(this.mContainingFrame);
                if (!(this.mAppToken == null || (this.mAppToken.mFrozenBounds.isEmpty() ^ 1) == 0)) {
                    Rect frozen = (Rect) this.mAppToken.mFrozenBounds.peek();
                    this.mContainingFrame.right = this.mContainingFrame.left + frozen.width();
                    this.mContainingFrame.bottom = this.mContainingFrame.top + frozen.height();
                }
                WindowState imeWin = this.mService.mInputMethodWindow;
                this.mService.mHwWMSEx.adjustWindowPosForPadPC(this.mContainingFrame, contentFrame, imeWin, this.mService.mInputMethodTarget, this);
                if (imeWin != null && imeWin.isVisibleNow() && this.mService.mInputMethodTarget == this) {
                    int stackId = getStackId();
                    if (stackId == 2 && this.mContainingFrame.bottom > contentFrame.bottom) {
                        Rect rect = this.mContainingFrame;
                        rect.top -= this.mContainingFrame.bottom - contentFrame.bottom;
                    } else if (stackId != 4 && this.mContainingFrame.bottom > parentFrame.bottom) {
                        this.mContainingFrame.bottom = parentFrame.bottom;
                    }
                }
                if (windowsAreFloating != 0 && this.mContainingFrame.isEmpty()) {
                    this.mContainingFrame.set(contentFrame);
                }
                this.mDisplayFrame.set(this.mContainingFrame);
                layoutXDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.left - this.mContainingFrame.left : 0;
                layoutYDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.top - this.mContainingFrame.top : 0;
                layoutContainingFrame = !this.mInsetFrame.isEmpty() ? this.mInsetFrame : this.mContainingFrame;
                this.mTmpRect.set(0, 0, dc.getDisplayInfo().logicalWidth, dc.getDisplayInfo().logicalHeight);
                subtractInsets(this.mDisplayFrame, layoutContainingFrame, displayFrame, this.mTmpRect);
                if (!layoutInParentFrame()) {
                    subtractInsets(this.mContainingFrame, layoutContainingFrame, parentFrame, this.mTmpRect);
                    subtractInsets(this.mInsetFrame, layoutContainingFrame, parentFrame, this.mTmpRect);
                }
                layoutDisplayFrame = displayFrame;
                displayFrame.intersect(layoutContainingFrame);
            }
            int pw = this.mContainingFrame.width();
            int ph = this.mContainingFrame.height();
            if (!this.mParentFrame.equals(parentFrame)) {
                this.mParentFrame.set(parentFrame);
                this.mContentChanged = true;
            }
            if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
                this.mLastRequestedWidth = this.mRequestedWidth;
                this.mLastRequestedHeight = this.mRequestedHeight;
                this.mContentChanged = true;
            }
            this.mOverscanFrame.set(overscanFrame);
            this.mContentFrame.set(contentFrame);
            this.mVisibleFrame.set(visibleFrame);
            this.mDecorFrame.set(decorFrame);
            this.mStableFrame.set(stableFrame);
            boolean hasOutsets = outsetFrame != null;
            if (hasOutsets) {
                this.mOutsetFrame.set(outsetFrame);
            }
            int fw = this.mFrame.width();
            int fh = this.mFrame.height();
            applyGravityAndUpdateFrame(layoutContainingFrame, layoutDisplayFrame);
            if (hasOutsets) {
                this.mOutsets.set(Math.max(this.mContentFrame.left - this.mOutsetFrame.left, 0), Math.max(this.mContentFrame.top - this.mOutsetFrame.top, 0), Math.max(this.mOutsetFrame.right - this.mContentFrame.right, 0), Math.max(this.mOutsetFrame.bottom - this.mContentFrame.bottom, 0));
            } else {
                this.mOutsets.set(0, 0, 0, 0);
            }
            if (windowsAreFloating != 0 && (this.mFrame.isEmpty() ^ 1) != 0) {
                Rect limitFrame = task.inPinnedWorkspace() ? this.mFrame : this.mContentFrame;
                int height = Math.min(this.mFrame.height(), limitFrame.height());
                int width = Math.min(limitFrame.width(), this.mFrame.width());
                DisplayMetrics displayMetrics = getDisplayContent().getDisplayMetrics();
                int minVisibleHeight = Math.min(height, WindowManagerService.dipToPixel(36, displayMetrics));
                int minVisibleWidth = Math.min(width, WindowManagerService.dipToPixel(42, displayMetrics));
                int top = Math.max(limitFrame.top, Math.min(this.mFrame.top, limitFrame.bottom - minVisibleHeight));
                int left = Math.max((limitFrame.left + minVisibleWidth) - width, Math.min(this.mFrame.left, limitFrame.right - minVisibleWidth));
                this.mFrame.set(left, top, left + width, top + height);
                this.mContentFrame.set(this.mFrame);
                this.mVisibleFrame.set(this.mContentFrame);
                this.mStableFrame.set(this.mContentFrame);
            } else if (this.mAttrs.type == 2034) {
                dc.getDockedDividerController().positionDockedStackedDivider(this.mFrame);
                this.mContentFrame.set(this.mFrame);
                if (!this.mFrame.equals(this.mLastFrame)) {
                    this.mMovedByResize = true;
                }
            } else {
                this.mContentFrame.set(Math.max(this.mContentFrame.left, this.mFrame.left), Math.max(this.mContentFrame.top, this.mFrame.top), Math.min(this.mContentFrame.right, this.mFrame.right), Math.min(this.mContentFrame.bottom, this.mFrame.bottom));
                this.mVisibleFrame.set(Math.max(this.mVisibleFrame.left, this.mFrame.left), Math.max(this.mVisibleFrame.top, this.mFrame.top), Math.min(this.mVisibleFrame.right, this.mFrame.right), Math.min(this.mVisibleFrame.bottom, this.mFrame.bottom));
                this.mStableFrame.set(Math.max(this.mStableFrame.left, this.mFrame.left), Math.max(this.mStableFrame.top, this.mFrame.top), Math.min(this.mStableFrame.right, this.mFrame.right), Math.min(this.mStableFrame.bottom, this.mFrame.bottom));
            }
            if (inFullscreenContainer && (windowsAreFloating ^ 1) != 0) {
                this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, 0), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, 0), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, 0), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, 0));
            }
            if (this.mAttrs.type == 2034) {
                this.mStableInsets.set(Math.max(this.mStableFrame.left - this.mDisplayFrame.left, 0), Math.max(this.mStableFrame.top - this.mDisplayFrame.top, 0), Math.max(this.mDisplayFrame.right - this.mStableFrame.right, 0), Math.max(this.mDisplayFrame.bottom - this.mStableFrame.bottom, 0));
                this.mContentInsets.setEmpty();
                this.mVisibleInsets.setEmpty();
            } else {
                int i;
                int i2;
                getDisplayContent().getLogicalDisplayRect(this.mTmpRect);
                boolean overrideRightInset = (windowsAreFloating != 0 || (inFullscreenContainer ^ 1) == 0) ? false : this.mFrame.right > this.mTmpRect.right;
                boolean overrideBottomInset = (windowsAreFloating != 0 || (inFullscreenContainer ^ 1) == 0) ? false : this.mFrame.bottom > this.mTmpRect.bottom;
                Rect rect2 = this.mContentInsets;
                int i3 = this.mContentFrame.left - this.mFrame.left;
                int i4 = this.mContentFrame.top - this.mFrame.top;
                if (overrideRightInset) {
                    i = this.mTmpRect.right - this.mContentFrame.right;
                } else {
                    i = this.mFrame.right - this.mContentFrame.right;
                }
                if (overrideBottomInset) {
                    i2 = this.mTmpRect.bottom - this.mContentFrame.bottom;
                } else {
                    i2 = this.mFrame.bottom - this.mContentFrame.bottom;
                }
                rect2.set(i3, i4, i, i2);
                rect2 = this.mVisibleInsets;
                i3 = this.mVisibleFrame.left - this.mFrame.left;
                i4 = this.mVisibleFrame.top - this.mFrame.top;
                if (overrideRightInset) {
                    i = this.mTmpRect.right - this.mVisibleFrame.right;
                } else {
                    i = this.mFrame.right - this.mVisibleFrame.right;
                }
                if (overrideBottomInset) {
                    i2 = this.mTmpRect.bottom - this.mVisibleFrame.bottom;
                } else {
                    i2 = this.mFrame.bottom - this.mVisibleFrame.bottom;
                }
                rect2.set(i3, i4, i, i2);
                rect2 = this.mStableInsets;
                i3 = Math.max(this.mStableFrame.left - this.mFrame.left, 0);
                i4 = Math.max(this.mStableFrame.top - this.mFrame.top, 0);
                if (overrideRightInset) {
                    i = Math.max(this.mTmpRect.right - this.mStableFrame.right, 0);
                } else {
                    i = Math.max(this.mFrame.right - this.mStableFrame.right, 0);
                }
                if (overrideBottomInset) {
                    i2 = Math.max(this.mTmpRect.bottom - this.mStableFrame.bottom, 0);
                } else {
                    i2 = Math.max(this.mFrame.bottom - this.mStableFrame.bottom, 0);
                }
                rect2.set(i3, i4, i, i2);
                adjustVisibleInsetsInSplitMode();
            }
            this.mFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mCompatFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mContentFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mVisibleFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mStableFrame.offset(-layoutXDiff, -layoutYDiff);
            this.mCompatFrame.set(this.mFrame);
            if (this.mEnforceSizeCompat) {
                this.mOverscanInsets.scale(this.mInvGlobalScale);
                this.mContentInsets.scale(this.mInvGlobalScale);
                this.mVisibleInsets.scale(this.mInvGlobalScale);
                this.mStableInsets.scale(this.mInvGlobalScale);
                this.mOutsets.scale(this.mInvGlobalScale);
                this.mCompatFrame.scale(this.mInvGlobalScale);
                adjustContentsInsetsInCompatMode();
            }
            if (this.mIsWallpaper && !(fw == this.mFrame.width() && fh == this.mFrame.height())) {
                DisplayContent displayContent = getDisplayContent();
                if (displayContent != null) {
                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                    getDisplayContent().mWallpaperController.updateWallpaperOffset(this, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                }
            }
        }
    }

    private void adjustContentsInsetsInCompatMode() {
        WindowState imeWin = this.mService.mInputMethodWindow;
        if (imeWin != null && imeWin.isVisibleNow() && this.mService.mInputMethodTarget == this && imeWin.isDisplayedLw() && this.mContentInsets.bottom != 0 && (this.mAttrs.softInputMode & 240) != 48 && this.mContentInsets.bottom >= this.mVisibleInsets.bottom) {
            Rect rect = this.mContentInsets;
            rect.bottom--;
        }
    }

    private void adjustVisibleInsetsInSplitMode() {
        if (this.mService.isSplitMode() && (isInMultiWindowMode() ^ 1) != 0) {
            this.mVisibleInsets.right = this.mTmpRect.right - this.mVisibleFrame.right;
        }
    }

    public Rect getFrameLw() {
        return this.mFrame;
    }

    public Point getShownPositionLw() {
        return this.mShownPosition;
    }

    public Rect getDisplayFrameLw() {
        return this.mDisplayFrame;
    }

    public Rect getOverscanFrameLw() {
        return this.mOverscanFrame;
    }

    public Rect getContentFrameLw() {
        return this.mContentFrame;
    }

    public Rect getVisibleFrameLw() {
        return this.mVisibleFrame;
    }

    Rect getStableFrameLw() {
        return this.mStableFrame;
    }

    public boolean getGivenInsetsPendingLw() {
        return this.mGivenInsetsPending;
    }

    public Rect getGivenContentInsetsLw() {
        return this.mGivenContentInsets;
    }

    public Rect getGivenVisibleInsetsLw() {
        return this.mGivenVisibleInsets;
    }

    public LayoutParams getAttrs() {
        return this.mAttrs;
    }

    public boolean getNeedsMenuLw(android.view.WindowManagerPolicy.WindowState bottom) {
        return getDisplayContent().getNeedsMenu(this, bottom);
    }

    public int getSystemUiVisibility() {
        return this.mSystemUiVisibility;
    }

    public int getSurfaceLayer() {
        return this.mLayer;
    }

    public int getBaseType() {
        return getTopParentWindow().mAttrs.type;
    }

    public IApplicationToken getAppToken() {
        return this.mAppToken != null ? this.mAppToken.appToken : null;
    }

    public boolean isVoiceInteraction() {
        return this.mAppToken != null ? this.mAppToken.mVoiceInteraction : false;
    }

    boolean setReportResizeHints() {
        this.mOverscanInsetsChanged |= this.mLastOverscanInsets.equals(this.mOverscanInsets) ^ 1;
        this.mContentInsetsChanged |= this.mLastContentInsets.equals(this.mContentInsets) ^ 1;
        this.mVisibleInsetsChanged |= this.mLastVisibleInsets.equals(this.mVisibleInsets) ^ 1;
        this.mStableInsetsChanged |= this.mLastStableInsets.equals(this.mStableInsets) ^ 1;
        this.mOutsetsChanged |= this.mLastOutsets.equals(this.mOutsets) ^ 1;
        boolean z = this.mFrameSizeChanged;
        int i = this.mLastFrame.width() == this.mFrame.width() ? this.mLastFrame.height() != this.mFrame.height() ? 1 : 0 : 1;
        this.mFrameSizeChanged = i | z;
        if (this.mOverscanInsetsChanged || this.mContentInsetsChanged || this.mVisibleInsetsChanged || this.mOutsetsChanged) {
            return true;
        }
        return this.mFrameSizeChanged;
    }

    void updateResizingWindowIfNeeded() {
        WindowStateAnimator winAnimator = this.mWinAnimator;
        if (this.mHasSurface && this.mService.mLayoutSeq == this.mLayoutSeq && !isGoneForLayoutLw()) {
            Task task = getTask();
            if (task == null || !task.mStack.isAnimatingBounds()) {
                setReportResizeHints();
                boolean configChanged = isConfigChanged();
                int dragResizingChanged;
                if (isDragResizeChanged()) {
                    dragResizingChanged = isDragResizingChangeReported() ^ 1;
                } else {
                    dragResizingChanged = 0;
                }
                this.mLastFrame.set(this.mFrame);
                if (this.mContentInsetsChanged || this.mVisibleInsetsChanged || winAnimator.mSurfaceResized || this.mOutsetsChanged || this.mFrameSizeChanged || configChanged || dragResizingChanged != 0 || (isResizedWhileNotDragResizingReported() ^ 1) != 0 || this.mReportOrientationChanged) {
                    if (this.mAppToken == null || !this.mAppDied) {
                        updateLastInsetValues();
                        this.mService.-com_android_server_wm_AppWindowToken-mthref-0(this);
                        if (this.mOrientationChanging || dragResizingChanged != 0 || isResizedWhileNotDragResizing()) {
                            winAnimator.mDrawState = 1;
                            if (this.mAppToken != null) {
                                this.mAppToken.clearAllDrawn();
                            }
                        }
                        if (!this.mService.mResizingWindows.contains(this)) {
                            this.mService.mResizingWindows.add(this);
                        }
                    } else {
                        this.mAppToken.removeDeadWindows();
                    }
                } else if (this.mOrientationChanging && isDrawnLw()) {
                    this.mOrientationChanging = false;
                    this.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
                }
            }
        }
    }

    DisplayContent getDisplayContent() {
        return this.mToken.getDisplayContent();
    }

    DisplayInfo getDisplayInfo() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent != null) {
            return displayContent.getDisplayInfo();
        }
        return null;
    }

    public int getDisplayId() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return -1;
        }
        return displayContent.getDisplayId();
    }

    Task getTask() {
        return this.mAppToken != null ? this.mAppToken.getTask() : null;
    }

    TaskStack getStack() {
        TaskStack taskStack = null;
        Task task = getTask();
        if (task != null && task.mStack != null) {
            return task.mStack;
        }
        DisplayContent dc = getDisplayContent();
        if (this.mAttrs.type >= IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && dc != null) {
            taskStack = dc.getHomeStack();
        }
        return taskStack;
    }

    void getVisibleBounds(Rect bounds) {
        Task task = getTask();
        boolean intersectWithStackBounds = task != null ? task.cropWindowsToStackBounds() : false;
        bounds.setEmpty();
        this.mTmpRect.setEmpty();
        if (intersectWithStackBounds) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
            } else {
                intersectWithStackBounds = false;
            }
        }
        bounds.set(this.mVisibleFrame);
        if (intersectWithStackBounds) {
            bounds.intersect(this.mTmpRect);
        }
        if (bounds.isEmpty()) {
            bounds.set(this.mFrame);
            if (intersectWithStackBounds) {
                bounds.intersect(this.mTmpRect);
            }
        }
    }

    public long getInputDispatchingTimeoutNanos() {
        if (this.mAppToken != null) {
            return this.mAppToken.mInputDispatchingTimeoutNanos;
        }
        return 5000000000L;
    }

    public boolean hasAppShownWindows() {
        if (this.mAppToken != null) {
            return !this.mAppToken.firstWindowDrawn ? this.mAppToken.startingDisplayed : true;
        } else {
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0015, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:9:0x001e, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        if (dsdx < 0.99999f || dsdx > 1.00001f || dtdy < 0.99999f || dtdy > 1.00001f || dtdx < -1.0E-6f || dtdx > 1.0E-6f || dsdy < -1.0E-6f || dsdy > 1.0E-6f) {
            return false;
        }
        return true;
    }

    void prelayout() {
        if (this.mEnforceSizeCompat) {
            String pkgName = null;
            if (this.mAppToken != null) {
                pkgName = this.mAppToken.appPackageName;
            }
            if (this.mAppToken == null || pkgName == null || pkgName.isEmpty()) {
                pkgName = getOwningPackage();
            }
            IApsManager apsManager = (IApsManager) LocalServices.getService(IApsManager.class);
            float resolutionRatio = 0.0f;
            if (apsManager != null) {
                resolutionRatio = apsManager.getResolution(pkgName);
            }
            if (0.0f >= resolutionRatio || resolutionRatio >= 1.0f) {
                this.mGlobalScale = getDisplayContent().mCompatibleScreenScale;
                this.mInvGlobalScale = 1.0f / this.mGlobalScale;
                return;
            }
            this.mInvGlobalScale = resolutionRatio;
            this.mGlobalScale = 1.0f / this.mInvGlobalScale;
            return;
        }
        this.mInvGlobalScale = 1.0f;
        this.mGlobalScale = 1.0f;
    }

    boolean hasContentToDisplay() {
        if (!this.mAppFreezing && isDrawnLw() && (this.mViewVisibility == 0 || isAnimatingWithSavedSurface() || (this.mWinAnimator.isAnimationSet() && (this.mService.mAppTransition.isTransitionSet() ^ 1) != 0))) {
            return true;
        }
        return super.hasContentToDisplay();
    }

    boolean isVisible() {
        return wouldBeVisibleIfPolicyIgnored() ? this.mPolicyVisibility : false;
    }

    boolean wouldBeVisibleIfPolicyIgnored() {
        if (!this.mHasSurface || (isParentWindowHidden() ^ 1) == 0 || (this.mAnimatingExit ^ 1) == 0 || (this.mDestroying ^ 1) == 0) {
            return false;
        }
        return this.mIsWallpaper ? this.mWallpaperVisible : true;
    }

    public boolean isVisibleLw() {
        return isVisible();
    }

    boolean isWinVisibleLw() {
        if (this.mAppToken == null || (this.mAppToken.hiddenRequested ^ 1) != 0 || this.mAppToken.mAppAnimator.animating) {
            return isVisible();
        }
        return false;
    }

    boolean isVisibleNow() {
        if (!this.mToken.hidden || this.mAttrs.type == 3) {
            return isVisible();
        }
        return false;
    }

    boolean isPotentialDragTarget() {
        if (!isVisibleNow() || (this.mRemoved ^ 1) == 0 || this.mInputChannel == null || this.mInputWindowHandle == null) {
            return false;
        }
        return true;
    }

    boolean isVisibleOrAdding() {
        AppWindowToken atoken = this.mAppToken;
        if ((!this.mHasSurface && (this.mRelayoutCalled || this.mViewVisibility != 0)) || !this.mPolicyVisibility || (isParentWindowHidden() ^ 1) == 0) {
            return false;
        }
        if ((atoken == null || (atoken.hiddenRequested ^ 1) != 0) && (this.mAnimatingExit ^ 1) != 0) {
            return this.mDestroying ^ 1;
        }
        return false;
    }

    boolean isOnScreen() {
        boolean z = true;
        if (!this.mHasSurface || this.mDestroying || (this.mPolicyVisibility ^ 1) != 0) {
            return false;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken != null) {
            if ((isParentWindowHidden() || (atoken.hiddenRequested ^ 1) == 0) && this.mWinAnimator.mAnimation == null && atoken.mAppAnimator.animation == null) {
                z = false;
            }
            return z;
        }
        if (isParentWindowHidden() && this.mWinAnimator.mAnimation == null) {
            z = false;
        }
        return z;
    }

    boolean mightAffectAllDrawn(boolean visibleOnly) {
        int isViewVisible;
        if ((this.mAppToken == null || (this.mAppToken.isClientHidden() ^ 1) != 0) && this.mViewVisibility == 0) {
            isViewVisible = this.mWindowRemovalAllowed ^ 1;
        } else {
            isViewVisible = 0;
        }
        if (((!isOnScreen() || (visibleOnly && isViewVisible == 0)) && this.mWinAnimator.mAttrType != 1 && this.mWinAnimator.mAttrType != 4) || (this.mAnimatingExit ^ 1) == 0) {
            return false;
        }
        return this.mDestroying ^ 1;
    }

    boolean isInteresting() {
        if (this.mAppToken == null || (this.mAppDied ^ 1) == 0) {
            return false;
        }
        return this.mAppToken.mAppAnimator.freezingScreen ? this.mAppFreezing ^ 1 : true;
    }

    boolean isReadyForDisplay() {
        boolean z = true;
        if (this.mToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return false;
        }
        if (!this.mHasSurface || !this.mPolicyVisibility || (this.mDestroying ^ 1) == 0) {
            z = false;
        } else if ((isParentWindowHidden() || this.mViewVisibility != 0 || (this.mToken.hidden ^ 1) == 0) && this.mWinAnimator.mAnimation == null && (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null)) {
            z = false;
        }
        return z;
    }

    public boolean canAffectSystemUiFlags() {
        boolean shown = this.mWinAnimator.getShown();
        int exiting = (this.mAnimatingExit || this.mDestroying) ? 1 : this.mAppToken != null ? this.mAppToken.hidden : 0;
        return (!shown || (exiting ^ 1) == 0) ? false : (this.mAttrs.alpha == 0.0f) ^ 1;
    }

    public boolean isDisplayedLw() {
        AppWindowToken atoken = this.mAppToken;
        if (!isDrawnLw() || !this.mPolicyVisibility) {
            return false;
        }
        if ((!isParentWindowHidden() && (atoken == null || (atoken.hiddenRequested ^ 1) != 0)) || this.mWinAnimator.mAnimating) {
            return true;
        }
        if (atoken == null || atoken.mAppAnimator.animation == null) {
            return false;
        }
        return true;
    }

    public boolean isAnimatingLw() {
        if (this.mWinAnimator.mAnimation == null) {
            return (this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null) ? false : true;
        } else {
            return true;
        }
    }

    public boolean isGoneForLayoutLw() {
        AppWindowToken atoken = this.mAppToken;
        if (this.mViewVisibility == 8 || (this.mRelayoutCalled ^ 1) != 0 || ((atoken == null && this.mToken.hidden) || ((atoken != null && atoken.hiddenRequested) || isParentWindowHidden() || (this.mAnimatingExit && (isAnimatingLw() ^ 1) != 0)))) {
            return true;
        }
        return this.mDestroying;
    }

    public boolean isDrawFinishedLw() {
        if (!this.mHasSurface || (this.mDestroying ^ 1) == 0) {
            return false;
        }
        if (this.mWinAnimator.mDrawState == 2 || this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4) {
            return true;
        }
        return false;
    }

    public boolean isDrawnLw() {
        if (!this.mHasSurface || (this.mDestroying ^ 1) == 0) {
            return false;
        }
        return this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4;
    }

    private boolean isOpaqueDrawn() {
        if (((!this.mIsWallpaper && this.mAttrs.format == -1) || (this.mIsWallpaper && this.mWallpaperVisible)) && isDrawnLw() && this.mWinAnimator.mAnimation == null) {
            return this.mAppToken == null || this.mAppToken.mAppAnimator.animation == null;
        } else {
            return false;
        }
    }

    void onMovedByResize() {
        this.mMovedByResize = true;
        super.onMovedByResize();
    }

    boolean onAppVisibilityChanged(boolean visible, boolean runningAppAnimation) {
        boolean changed = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            changed |= ((WindowState) this.mChildren.get(i)).onAppVisibilityChanged(visible, runningAppAnimation);
        }
        if (this.mAttrs.type == 3) {
            if (!visible && isVisibleNow() && this.mAppToken.mAppAnimator.isAnimating()) {
                this.mAnimatingExit = true;
                this.mRemoveOnExit = true;
                this.mWindowRemovalAllowed = true;
            }
            return changed;
        }
        if (!visible && isVisibleNow()) {
            this.mWinAnimator.detachChildren();
        }
        if (visible != isVisibleNow()) {
            if (!runningAppAnimation) {
                AccessibilityController accessibilityController = this.mService.mAccessibilityController;
                int winTransit = visible ? 1 : 2;
                this.mWinAnimator.applyAnimationLocked(winTransit, visible);
                if (accessibilityController != null && getDisplayId() == 0) {
                    accessibilityController.onWindowTransitionLocked(this, winTransit);
                }
            }
            changed = true;
            setDisplayLayoutNeeded();
        }
        return changed;
    }

    boolean onSetAppExiting() {
        DisplayContent displayContent = getDisplayContent();
        boolean changed = false;
        if (isVisibleNow()) {
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (this.mService.mAccessibilityController != null && isDefaultDisplay()) {
                this.mService.mAccessibilityController.onWindowTransitionLocked(this, 2);
            }
            changed = true;
            if (displayContent != null) {
                displayContent.setLayoutNeeded();
            }
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            changed |= ((WindowState) this.mChildren.get(i)).onSetAppExiting();
        }
        return changed;
    }

    void onResize() {
        destroySavedSurface();
        ArrayList<WindowState> resizingWindows = this.mService.mResizingWindows;
        if (this.mHasSurface && (resizingWindows.contains(this) ^ 1) != 0) {
            resizingWindows.add(this);
            if (!(computeDragResizing() || this.mAttrs.type != 1 || (this.mWinAnimator.isForceScaled() ^ 1) == 0 || (isGoneForLayoutLw() ^ 1) == 0 || (getTask().inPinnedWorkspace() ^ 1) == 0)) {
                setResizedWhileNotDragResizing(true);
            }
        }
        if (isGoneForLayoutLw()) {
            this.mResizedWhileGone = true;
        }
        super.onResize();
    }

    void onUnfreezeBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onUnfreezeBounds();
        }
        if (this.mHasSurface) {
            this.mLayoutNeeded = true;
            setDisplayLayoutNeeded();
            if (!this.mService.mResizingWindows.contains(this)) {
                this.mService.mResizingWindows.add(this);
            }
        }
    }

    void handleWindowMovedIfNeeded() {
        if (hasMoved()) {
            int left = this.mFrame.left;
            int top = this.mFrame.top;
            Task task = getTask();
            int adjustedForMinimizedDockOrIme;
            if (task == null) {
                adjustedForMinimizedDockOrIme = 0;
            } else if (task.mStack.isAdjustedForMinimizedDockedStack()) {
                adjustedForMinimizedDockOrIme = 1;
            } else {
                adjustedForMinimizedDockOrIme = task.mStack.isAdjustedForIme();
            }
            if (this.mService.okToDisplay() && (this.mAttrs.privateFlags & 64) == 0 && (isDragResizing() ^ 1) != 0 && (adjustedForMinimizedDockOrIme ^ 1) != 0 && ((task == null || getTask().mStack.hasMovementAnimations()) && (this.mWinAnimator.mLastHidden ^ 1) != 0)) {
                this.mWinAnimator.setMoveAnimation(left, top);
            }
            if (this.mService.mAccessibilityController != null && getDisplayContent().getDisplayId() == 0) {
                this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            try {
                this.mClient.moved(left, top);
            } catch (RemoteException e) {
            }
            this.mMovedByResize = false;
        }
    }

    private boolean hasMoved() {
        if (!this.mHasSurface || ((!this.mContentChanged && !this.mMovedByResize) || (this.mAnimatingExit ^ 1) == 0 || (this.mFrame.top == this.mLastFrame.top && this.mFrame.left == this.mLastFrame.left))) {
            return false;
        }
        return this.mIsChildWindow ? getParentWindow().hasMoved() ^ 1 : true;
    }

    boolean isObscuringDisplay() {
        boolean z = false;
        Task task = getTask();
        if (task != null && task.mStack != null && (task.mStack.fillsParent() ^ 1) != 0) {
            return false;
        }
        if (isOpaqueDrawn()) {
            z = fillsDisplay();
        }
        return z;
    }

    boolean fillsDisplay() {
        DisplayInfo displayInfo = getDisplayInfo();
        if (this.mFrame.left > 0 || this.mFrame.top > 0 || this.mFrame.right < displayInfo.appWidth || this.mFrame.bottom < displayInfo.appHeight) {
            return false;
        }
        return true;
    }

    boolean isConfigChanged() {
        return getLastReportedConfiguration().equals(getConfiguration()) ^ 1;
    }

    void onWindowReplacementTimeout() {
        if (this.mWillReplaceWindow) {
            removeImmediately();
            return;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onWindowReplacementTimeout();
        }
    }

    void forceWindowsScaleableInTransaction(boolean force) {
        if (this.mWinAnimator != null && this.mWinAnimator.hasSurface()) {
            this.mWinAnimator.mSurfaceController.forceScaleableInTransaction(force);
        }
        super.forceWindowsScaleableInTransaction(force);
    }

    void removeImmediately() {
        super.removeImmediately();
        if (this.mRemoved) {
            Slog.v(TAG, "WS.removeImmediately: " + this + " Already removed...");
            return;
        }
        this.mRemoved = true;
        this.mWillReplaceWindow = false;
        if (this.mReplacementWindow != null) {
            this.mReplacementWindow.mSkipEnterAnimationForSeamlessReplacement = false;
        }
        DisplayContent dc = getDisplayContent();
        if (this.mService.mInputMethodTarget == this) {
            dc.computeImeTarget(true);
        }
        if (WindowManagerService.excludeWindowTypeFromTapOutTask(this.mAttrs.type) || WindowManagerService.excludeWindowsFromTapOutTask(this)) {
            dc.mTapExcludedWindows.remove(this);
        }
        this.mPolicy.removeWindowLw(this);
        disposeInputChannel();
        this.mWinAnimator.destroyDeferredSurfaceLocked();
        this.mWinAnimator.destroySurfaceLocked();
        this.mSession.windowRemovedLocked();
        try {
            this.mClient.asBinder().unlinkToDeath(this.mDeathRecipient, 0);
        } catch (RuntimeException e) {
        }
        this.mService.postWindowRemoveCleanupLocked(this);
    }

    void removeIfPossible() {
        super.removeIfPossible();
        removeIfPossible(false);
    }

    private void removeIfPossible(boolean keepVisibleDeadWindow) {
        this.mWindowRemovalAllowed = true;
        Slog.v(TAG, "removeIfPossible: " + this + " callers=" + Debug.getCallers(5));
        boolean startingWindow = this.mAttrs.type == 3;
        long origId = Binder.clearCallingIdentity();
        disposeInputChannel();
        boolean wasVisible = false;
        int displayId = getDisplayId();
        if (this.mHasSurface && this.mService.okToDisplay()) {
            if (this.mWillReplaceWindow) {
                Slog.v(TAG, "Preserving " + this + " until the new one is " + "added");
                this.mAnimatingExit = true;
                this.mReplacingRemoveRequested = true;
                Binder.restoreCallingIdentity(origId);
                return;
            } else if (!isAnimatingWithSavedSurface() || (this.mAppToken.allDrawnExcludingSaved ^ 1) == 0) {
                wasVisible = isWinVisibleLw();
                if (keepVisibleDeadWindow) {
                    Slog.v(TAG, "Not removing " + this + " because app died while it's visible");
                    this.mAppDied = true;
                    setDisplayLayoutNeeded();
                    this.mService.mWindowPlacerLocked.performSurfacePlacement();
                    openInputChannel(null);
                    this.mService.mInputMonitor.updateInputWindowsLw(true);
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
                if (wasVisible) {
                    int transit = !startingWindow ? 2 : 5;
                    if (this.mWinAnimator.applyAnimationLocked(transit, false)) {
                        this.mAnimatingExit = true;
                    }
                    if (this.mService.mAccessibilityController != null && displayId == 0) {
                        this.mService.mAccessibilityController.onWindowTransitionLocked(this, transit);
                    }
                }
                if (this.isBinderDiedCalling && HwPCUtils.enabled() && getDisplayId() != 0) {
                    this.isBinderDiedCalling = false;
                    this.mAnimatingExit = false;
                }
                int isAnimating = this.mWinAnimator.isAnimationSet() ? this.mWinAnimator.isDummyAnimation() ^ 1 : 0;
                boolean lastWindowIsStartingWindow;
                if (!startingWindow || this.mAppToken == null) {
                    lastWindowIsStartingWindow = false;
                } else {
                    lastWindowIsStartingWindow = this.mAppToken.isLastWindow(this);
                }
                if (this.mWinAnimator.getShown() && this.mAnimatingExit && !(lastWindowIsStartingWindow && isAnimating == 0)) {
                    Slog.v(TAG, "Not removing " + this + " due to exit animation ");
                    setupWindowForRemoveOnExit();
                    if (this.mAppToken != null) {
                        this.mAppToken.updateReportedVisibilityLocked();
                    }
                    Binder.restoreCallingIdentity(origId);
                    return;
                }
            } else {
                Slog.d(TAG, "removeWindowLocked: delay removal of " + this + " due to early animation");
                setupWindowForRemoveOnExit();
                Binder.restoreCallingIdentity(origId);
                return;
            }
        }
        removeImmediately();
        if (wasVisible && this.mService.updateOrientationFromAppTokensLocked(false, displayId)) {
            this.mService.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
        }
        if (this.mLastTitle == null || !this.mLastTitle.toString().contains("Emui:ProximityWnd")) {
            this.mService.updateFocusedWindowLocked(0, true);
        } else {
            this.mService.updateFocusedWindowLocked(2, true);
        }
        Binder.restoreCallingIdentity(origId);
    }

    private void setupWindowForRemoveOnExit() {
        this.mRemoveOnExit = true;
        setDisplayLayoutNeeded();
        boolean focusChanged = this.mService.updateFocusedWindowLocked(3, false);
        this.mService.mWindowPlacerLocked.performSurfacePlacement();
        if (focusChanged) {
            this.mService.mInputMonitor.updateInputWindowsLw(false);
        }
    }

    void setHasSurface(boolean hasSurface) {
        this.mHasSurface = hasSurface;
    }

    int getAnimLayerAdjustment() {
        if (this.mIsImWindow && this.mService.mInputMethodTarget != null) {
            AppWindowToken appToken = this.mService.mInputMethodTarget.mAppToken;
            if (appToken != null) {
                return appToken.getAnimLayerAdjustment();
            }
        }
        return this.mToken.getAnimLayerAdjustment();
    }

    int getSpecialWindowAnimLayerAdjustment() {
        int specialAdjustment = 0;
        if (this.mIsImWindow) {
            specialAdjustment = getDisplayContent().mInputMethodAnimLayerAdjustment;
        } else if (this.mIsWallpaper) {
            specialAdjustment = getDisplayContent().mWallpaperController.getAnimLayerAdjustment();
        }
        return this.mLayer + specialAdjustment;
    }

    /* renamed from: canBeImeTarget */
    boolean lambda$-com_android_server_wm_DisplayContent_33773() {
        if (this.mIsImWindow) {
            return false;
        }
        int fl = this.mAttrs.flags & 131080;
        int type = this.mAttrs.type;
        if (fl == 0 || fl == 131080 || type == 3) {
            return isVisibleOrAdding();
        }
        return false;
    }

    void scheduleAnimationIfDimming() {
        DisplayContent dc = getDisplayContent();
        if (dc != null) {
            DimLayerUser dimLayerUser = getDimLayerUser();
            if (dimLayerUser != null && dc.mDimLayerController.isDimming(dimLayerUser, this.mWinAnimator)) {
                this.mService.scheduleAnimationLocked();
            }
        }
    }

    void openInputChannel(InputChannel outInputChannel) {
        if (this.mInputChannel != null) {
            throw new IllegalStateException("Window already has an input channel.");
        }
        InputChannel[] inputChannels = InputChannel.openInputChannelPair(getName());
        this.mInputChannel = inputChannels[0];
        this.mClientChannel = inputChannels[1];
        this.mInputWindowHandle.inputChannel = inputChannels[0];
        if (outInputChannel != null) {
            this.mClientChannel.transferTo(outInputChannel);
            this.mClientChannel.dispose();
            this.mClientChannel = null;
        } else {
            this.mDeadWindowEventReceiver = new DeadWindowEventReceiver(this.mClientChannel);
        }
        this.mService.mInputManager.registerInputChannel(this.mInputChannel, this.mInputWindowHandle);
    }

    void disposeInputChannel() {
        if (this.mDeadWindowEventReceiver != null) {
            this.mDeadWindowEventReceiver.dispose();
            this.mDeadWindowEventReceiver = null;
        }
        if (this.mInputChannel != null) {
            this.mService.mInputManager.unregisterInputChannel(this.mInputChannel);
            this.mInputChannel.dispose();
            this.mInputChannel = null;
        }
        if (this.mClientChannel != null) {
            this.mClientChannel.dispose();
            this.mClientChannel = null;
        }
        this.mInputWindowHandle.inputChannel = null;
    }

    void applyDimLayerIfNeeded() {
        AppWindowToken token = this.mAppToken;
        if (token == null || !token.removed) {
            DisplayContent dc = getDisplayContent();
            if (!this.mAnimatingExit && this.mAppDied) {
                dc.mDimLayerController.applyDimAbove(getDimLayerUser(), this.mWinAnimator);
            } else if (!((this.mAttrs.flags & 2) == 0 || dc == null || (this.mAnimatingExit ^ 1) == 0 || !isVisible())) {
                dc.mDimLayerController.applyDimBehind(getDimLayerUser(), this.mWinAnimator);
            }
        }
    }

    private DimLayerUser getDimLayerUser() {
        Task task = getTask();
        if (task != null) {
            return task;
        }
        return getStack();
    }

    boolean removeReplacedWindowIfNeeded(WindowState replacement) {
        if (this.mWillReplaceWindow && this.mReplacementWindow == replacement && replacement.hasDrawnLw()) {
            replacement.mSkipEnterAnimationForSeamlessReplacement = false;
            removeReplacedWindow();
            return true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).removeReplacedWindowIfNeeded(replacement)) {
                return true;
            }
        }
        return false;
    }

    private void removeReplacedWindow() {
        Slog.d(TAG, "Removing replaced window: " + this);
        if (isDimming()) {
            transferDimToReplacement();
        }
        this.mWillReplaceWindow = false;
        this.mAnimateReplacingWindow = false;
        this.mReplacingRemoveRequested = false;
        this.mReplacementWindow = null;
        if (this.mAnimatingExit || (this.mAnimateReplacingWindow ^ 1) != 0) {
            removeImmediately();
        }
    }

    boolean setReplacementWindowIfNeeded(WindowState replacementCandidate) {
        boolean replacementSet = false;
        if (this.mWillReplaceWindow && this.mReplacementWindow == null && getWindowTag().toString().equals(replacementCandidate.getWindowTag().toString())) {
            this.mReplacementWindow = replacementCandidate;
            replacementCandidate.mSkipEnterAnimationForSeamlessReplacement = this.mAnimateReplacingWindow ^ 1;
            replacementSet = true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            replacementSet |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(replacementCandidate);
        }
        return replacementSet;
    }

    void setDisplayLayoutNeeded() {
        DisplayContent dc = getDisplayContent();
        if (dc != null) {
            dc.setLayoutNeeded();
        }
    }

    boolean inPinnedWorkspace() {
        Task task = getTask();
        return task != null ? task.inPinnedWorkspace() : false;
    }

    void applyAdjustForImeIfNeeded() {
        Task task = getTask();
        if (task != null && task.mStack != null && task.mStack.isAdjustedForIme()) {
            task.mStack.applyAdjustForImeIfNeeded(task);
        }
    }

    void switchUser() {
        super.switchUser();
        if (isHiddenFromUserLocked()) {
            hideLw(false);
        }
    }

    int getTouchableRegion(Region region, int flags) {
        if (!((flags & 40) == 0) || this.mAppToken == null) {
            getTouchableRegion(region);
        } else {
            flags |= 32;
            DimLayerUser dimLayerUser = getDimLayerUser();
            if (dimLayerUser != null) {
                dimLayerUser.getDimBounds(this.mTmpRect);
            } else {
                getVisibleBounds(this.mTmpRect);
            }
            if (inFreeformWorkspace()) {
                int delta = WindowManagerService.dipToPixel(10, getDisplayContent().getDisplayMetrics());
                this.mTmpRect.inset(-delta, -delta);
            }
            region.set(this.mTmpRect);
            cropRegionToStackBoundsIfNeeded(region);
        }
        return flags;
    }

    void checkPolicyVisibilityChange() {
        if (this.mPolicyVisibility != this.mPolicyVisibilityAfterAnim) {
            this.mPolicyVisibility = this.mPolicyVisibilityAfterAnim;
            setDisplayLayoutNeeded();
            if (!this.mPolicyVisibility) {
                if (this.mService.mCurrentFocus == this) {
                    this.mService.mFocusMayChange = true;
                }
                this.mService.enableScreenIfNeededLocked();
            }
        }
    }

    void setRequestedSize(int requestedWidth, int requestedHeight) {
        if (this.mRequestedWidth != requestedWidth || this.mRequestedHeight != requestedHeight) {
            this.mLayoutNeeded = true;
            this.mRequestedWidth = requestedWidth;
            this.mRequestedHeight = requestedHeight;
        }
    }

    void prepareWindowToDisplayDuringRelayout(boolean wasVisible) {
        if ((this.mAttrs.flags & DumpState.DUMP_COMPILER_STATS) != 0) {
            Slog.v(TAG, "Relayout window turning screen on: " + this);
            this.mTurnOnScreen = true;
        }
        if (!wasVisible) {
            if ((this.mAttrs.softInputMode & 240) == 16) {
                this.mLayoutNeeded = true;
            }
            if (isDrawnLw() && this.mService.okToDisplay()) {
                this.mWinAnimator.applyEnterAnimationLocked();
            }
        }
    }

    void getMergedConfiguration(MergedConfiguration outConfiguration) {
        outConfiguration.setConfiguration(this.mService.mRoot.getConfiguration(), getMergedOverrideConfiguration());
        onChangeConfiguration(outConfiguration);
    }

    void setLastReportedMergedConfiguration(MergedConfiguration config) {
        this.mLastReportedConfiguration.setTo(config);
    }

    void getLastReportedMergedConfiguration(MergedConfiguration config) {
        config.setTo(this.mLastReportedConfiguration);
    }

    private Configuration getLastReportedConfiguration() {
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getDisplayId()) && this.mAppToken == null) {
            return getConfiguration();
        }
        return this.mLastReportedConfiguration.getMergedConfiguration();
    }

    void adjustStartingWindowFlags() {
        if (this.mAttrs.type == 1 && this.mAppToken != null && this.mAppToken.startingWindow != null) {
            LayoutParams sa = this.mAppToken.startingWindow.mAttrs;
            sa.flags = (sa.flags & -4718594) | (this.mAttrs.flags & 4718593);
        }
    }

    void setWindowScale(int requestedWidth, int requestedHeight) {
        float f = 1.0f;
        if ((this.mAttrs.flags & 16384) != 0) {
            float f2;
            if (this.mAttrs.width != requestedWidth) {
                f2 = ((float) this.mAttrs.width) / ((float) requestedWidth);
            } else {
                f2 = 1.0f;
            }
            this.mHScale = f2;
            if (this.mAttrs.height != requestedHeight) {
                f = ((float) this.mAttrs.height) / ((float) requestedHeight);
            }
            this.mVScale = f;
            return;
        }
        this.mVScale = 1.0f;
        this.mHScale = 1.0f;
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean shouldKeepVisibleDeadAppWindow() {
        boolean z = false;
        if (!isWinVisibleLw() || this.mAppToken == null || this.mAppToken.isClientHidden() || this.mAttrs.token != this.mClient.asBinder() || this.mAttrs.type == 3) {
            return false;
        }
        TaskStack stack = getStack();
        if (stack != null) {
            z = StackId.keepVisibleDeadAppWindowOnScreen(stack.mStackId);
        }
        return z;
    }

    boolean canReceiveKeys() {
        if (!isVisibleOrAdding() || this.mViewVisibility != 0 || (this.mRemoveOnExit ^ 1) == 0 || (this.mAttrs.flags & 8) != 0) {
            return false;
        }
        if (this.mAppToken == null || this.mAppToken.windowsAreFocusable()) {
            return canReceiveTouchInput() ^ 1;
        }
        return false;
    }

    boolean canReceiveTouchInput() {
        if (this.mAppToken == null || this.mAppToken.getTask() == null) {
            return false;
        }
        return this.mAppToken.getTask().mStack.shouldIgnoreInput();
    }

    public boolean hasDrawnLw() {
        return this.mWinAnimator.mDrawState == 4;
    }

    public boolean showLw(boolean doAnimation) {
        return showLw(doAnimation, true);
    }

    boolean showLw(boolean doAnimation, boolean requestAnim) {
        if (isHiddenFromUserLocked() || !this.mAppOpVisibility || this.mPermanentlyHidden || this.mForceHideNonSystemOverlayWindow) {
            return false;
        }
        if (this.mPolicyVisibility && this.mPolicyVisibilityAfterAnim) {
            return false;
        }
        if (doAnimation) {
            if (!this.mService.okToDisplay()) {
                doAnimation = false;
            } else if (this.mPolicyVisibility && this.mWinAnimator.mAnimation == null) {
                doAnimation = false;
            }
        }
        this.mPolicyVisibility = true;
        this.mPolicyVisibilityAfterAnim = true;
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(1, true);
        }
        if (requestAnim) {
            this.mService.scheduleAnimationLocked();
        }
        if ((this.mAttrs.flags & 8) == 0) {
            this.mService.updateFocusedWindowLocked(0, false);
        }
        return true;
    }

    public boolean hideLw(boolean doAnimation) {
        return hideLw(doAnimation, true);
    }

    boolean hideLw(boolean doAnimation, boolean requestAnim) {
        if (doAnimation && !this.mService.okToDisplay()) {
            doAnimation = false;
        }
        if (!(doAnimation ? this.mPolicyVisibilityAfterAnim : this.mPolicyVisibility)) {
            return false;
        }
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (this.mWinAnimator.mAnimation == null) {
                doAnimation = false;
            }
        }
        this.mPolicyVisibilityAfterAnim = false;
        if (!doAnimation) {
            this.mPolicyVisibility = false;
            this.mService.enableScreenIfNeededLocked();
            if (this.mService.mCurrentFocus == this) {
                this.mService.mFocusMayChange = true;
            }
        }
        if (requestAnim) {
            this.mService.scheduleAnimationLocked();
        }
        if (this.mService.mCurrentFocus == this) {
            this.mService.updateFocusedWindowLocked(0, false);
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:6:0x0017, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* renamed from: setForceHideNonSystemOverlayWindowIfNeeded */
    void lambda$-com_android_server_wm_WindowManagerService_375781(boolean forceHide) {
        if (!this.mOwnerCanAddInternalSystemWindow && ((LayoutParams.isSystemAlertWindowType(this.mAttrs.type) || this.mAttrs.type == 2005) && this.mForceHideNonSystemOverlayWindow != forceHide)) {
            this.mForceHideNonSystemOverlayWindow = forceHide;
            if (forceHide) {
                hideLw(true, true);
            } else {
                showLw(true, true);
            }
        }
    }

    public void setAppOpVisibilityLw(boolean state) {
        if (this.mAppOpVisibility != state) {
            this.mAppOpVisibility = state;
            if (state) {
                showLw(true, true);
            } else {
                hideLw(true, true);
            }
        }
    }

    public void hidePermanentlyLw() {
        if (!this.mPermanentlyHidden) {
            this.mPermanentlyHidden = true;
            hideLw(true, true);
        }
    }

    public void pokeDrawLockLw(long timeout) {
        if (isVisibleOrAdding()) {
            if (this.mDrawLock == null) {
                this.mDrawLock = this.mService.mPowerManager.newWakeLock(128, "Window:" + getWindowTag());
                this.mDrawLock.setReferenceCounted(false);
                this.mDrawLock.setWorkSource(new WorkSource(this.mOwnerUid, this.mAttrs.packageName));
            }
            this.mDrawLock.acquire(timeout);
        }
    }

    public boolean isAlive() {
        return this.mClient.asBinder().isBinderAlive();
    }

    boolean isClosing() {
        return !this.mAnimatingExit ? this.mService.mClosingApps.contains(this.mAppToken) : true;
    }

    boolean isAnimatingWithSavedSurface() {
        return this.mAnimatingWithSavedSurface;
    }

    boolean isAnimating() {
        if (this.mWinAnimator.isAnimationSet() || this.mAnimatingExit) {
            return true;
        }
        return super.isAnimating();
    }

    boolean isAnimatingInvisibleWithSavedSurface() {
        if (this.mAnimatingWithSavedSurface && (this.mViewVisibility != 0 || this.mWindowRemovalAllowed)) {
            return true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).isAnimatingInvisibleWithSavedSurface()) {
                return true;
            }
        }
        return false;
    }

    void stopUsingSavedSurface() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).stopUsingSavedSurface();
        }
        if (isAnimatingInvisibleWithSavedSurface()) {
            clearAnimatingWithSavedSurface();
            this.mDestroying = true;
            this.mWinAnimator.hide("stopUsingSavedSurface");
            getDisplayContent().mWallpaperController.hideWallpapers(this);
        }
    }

    void markSavedSurfaceExiting() {
        if (isAnimatingInvisibleWithSavedSurface()) {
            this.mAnimatingExit = true;
            this.mWinAnimator.mAnimating = true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).markSavedSurfaceExiting();
        }
    }

    void addWinAnimatorToList(ArrayList<WindowStateAnimator> animators) {
        animators.add(this.mWinAnimator);
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).addWinAnimatorToList(animators);
        }
    }

    void sendAppVisibilityToClients() {
        super.sendAppVisibilityToClients();
        boolean clientHidden = this.mAppToken.isClientHidden();
        if (this.mAttrs.type != 3 || !clientHidden) {
            if (clientHidden) {
                for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                    ((WindowState) this.mChildren.get(i)).mWinAnimator.detachChildren();
                }
                this.mWinAnimator.detachChildren();
            }
            try {
                this.mClient.dispatchAppVisibility(clientHidden ^ 1);
            } catch (RemoteException e) {
            }
        }
    }

    public void setVisibleBeforeClientHidden() {
        this.mWasVisibleBeforeClientHidden = (this.mViewVisibility != 0 ? this.mAnimatingWithSavedSurface : 1) | this.mWasVisibleBeforeClientHidden;
        super.setVisibleBeforeClientHidden();
    }

    public void clearWasVisibleBeforeClientHidden() {
        this.mWasVisibleBeforeClientHidden = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWasVisibleBeforeClientHidden();
        }
    }

    public boolean wasVisibleBeforeClientHidden() {
        return this.mWasVisibleBeforeClientHidden;
    }

    void onStartFreezingScreen() {
        this.mAppFreezing = true;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onStartFreezingScreen();
        }
    }

    boolean onStopFreezingScreen() {
        boolean unfrozeWindows = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            unfrozeWindows |= ((WindowState) this.mChildren.get(i)).onStopFreezingScreen();
        }
        if (!this.mAppFreezing) {
            return unfrozeWindows;
        }
        this.mAppFreezing = false;
        if (!(!this.mHasSurface || (this.mOrientationChanging ^ 1) == 0 || this.mService.mWindowsFreezingScreen == 2)) {
            this.mOrientationChanging = true;
            this.mService.mRoot.mOrientationChangeComplete = false;
        }
        this.mLastFreezeDuration = 0;
        setDisplayLayoutNeeded();
        return true;
    }

    private boolean shouldSaveSurface() {
        if (this.mWinAnimator.mSurfaceController == null) {
            return false;
        }
        if ((this.mAttrs.hwFlags & 2) == 2) {
            Slog.v(TAG, "dont Save surface because FLAG_DESTORY_SURFACE");
            return false;
        } else if (!this.mWasVisibleBeforeClientHidden || (this.mAttrs.flags & 8192) != 0 || SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false) || SystemProperties.getBoolean("ro.config.hw_disable_surface", true) || ActivityManager.isLowRamDeviceStatic()) {
            return false;
        } else {
            AppWindowToken taskTop = getTask().getTopVisibleAppToken();
            if ((taskTop != null && taskTop != this.mAppToken) || this.mResizedWhileGone || DEBUG_DISABLE_SAVING_SURFACES) {
                return false;
            }
            return this.mAppToken.shouldSaveSurface();
        }
    }

    boolean destroySurface(boolean cleanupOnResume, boolean appStopped) {
        boolean destroyedSomething = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            destroyedSomething |= ((WindowState) this.mChildren.get(i)).destroySurface(cleanupOnResume, appStopped);
        }
        boolean z = (appStopped || this.mWindowRemovalAllowed) ? true : cleanupOnResume;
        if (!z && ("com.touchtype.swiftkey".equals(getOwningPackage()) ^ 1) != 0) {
            return destroyedSomething;
        }
        if (appStopped || this.mWindowRemovalAllowed) {
            this.mWinAnimator.destroyPreservedSurfaceLocked();
        }
        if (this.mDestroying) {
            Slog.e(TAG, "win=" + this + " destroySurfaces: appStopped=" + appStopped + " win.mWindowRemovalAllowed=" + this.mWindowRemovalAllowed + " win.mRemoveOnExit=" + this.mRemoveOnExit);
            if (!cleanupOnResume || this.mRemoveOnExit) {
                destroyOrSaveSurfaceUnchecked();
            }
            if (this.mRemoveOnExit) {
                removeImmediately();
            }
            if (cleanupOnResume) {
                requestUpdateWallpaperIfNeeded();
            }
            this.mDestroying = false;
            destroyedSomething = true;
        }
        return destroyedSomething;
    }

    void destroyOrSaveSurfaceUnchecked() {
        this.mSurfaceSaved = shouldSaveSurface();
        if (this.mSurfaceSaved) {
            Slog.i(TAG, "EGLdebug Saving surface: " + this);
            this.mSession.setTransparentRegion(this.mClient, sEmptyRegion);
            this.mWinAnimator.hide("saved surface");
            this.mWinAnimator.mDrawState = 0;
            setHasSurface(false);
            if (this.mWinAnimator.mSurfaceController != null) {
                try {
                    this.mClient.updateSurfaceStatus(false);
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception thrown when updateSurfaceStatus" + this + ": " + e);
                }
                this.mWinAnimator.mSurfaceController.disconnectInTransaction();
            }
            this.mAnimatingWithSavedSurface = false;
        } else {
            this.mWinAnimator.destroySurfaceLocked();
        }
        this.mAnimatingExit = false;
    }

    void destroySavedSurface() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).destroySavedSurface();
        }
        if (this.mSurfaceSaved) {
            this.mWinAnimator.destroySurfaceLocked();
            this.mSurfaceSaved = false;
        }
        this.mWasVisibleBeforeClientHidden = false;
    }

    int restoreSavedSurfaceForInterestingWindow() {
        int interestingNotDrawn = -1;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            int childInterestingNotDrawn = ((WindowState) this.mChildren.get(i)).restoreSavedSurfaceForInterestingWindow();
            if (childInterestingNotDrawn != -1) {
                if (interestingNotDrawn == -1) {
                    interestingNotDrawn = childInterestingNotDrawn;
                } else {
                    interestingNotDrawn += childInterestingNotDrawn;
                }
            }
        }
        if (this.mAttrs.type == 3 || this.mAppDied || (wasVisibleBeforeClientHidden() ^ 1) != 0 || (this.mAppToken.mAppAnimator.freezingScreen && this.mAppFreezing)) {
            return interestingNotDrawn;
        }
        restoreSavedSurface();
        if (!isDrawnLw()) {
            if (interestingNotDrawn == -1) {
                interestingNotDrawn = 1;
            } else {
                interestingNotDrawn++;
            }
        }
        return interestingNotDrawn;
    }

    boolean restoreSavedSurface() {
        if (!this.mSurfaceSaved) {
            return false;
        }
        if (this.mLastVisibleLayoutRotation != getDisplayContent().getRotation()) {
            destroySavedSurface();
            return false;
        }
        this.mSurfaceSaved = false;
        if (this.mWinAnimator.mSurfaceController != null) {
            setHasSurface(true);
            this.mWinAnimator.mDrawState = 3;
            this.mAnimatingWithSavedSurface = true;
            requestUpdateWallpaperIfNeeded();
        } else {
            Slog.wtf(TAG, "Failed to restore saved surface: surface gone! " + this);
        }
        return true;
    }

    boolean canRestoreSurface() {
        if (this.mWasVisibleBeforeClientHidden && this.mSurfaceSaved) {
            return true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).canRestoreSurface()) {
                return true;
            }
        }
        return false;
    }

    boolean hasSavedSurface() {
        return this.mSurfaceSaved;
    }

    void clearHasSavedSurface() {
        this.mSurfaceSaved = false;
        this.mAnimatingWithSavedSurface = false;
        if (this.mWasVisibleBeforeClientHidden) {
            this.mAppToken.destroySavedSurfaces();
        }
    }

    boolean clearAnimatingWithSavedSurface() {
        if (!this.mAnimatingWithSavedSurface) {
            return false;
        }
        this.mAnimatingWithSavedSurface = false;
        return true;
    }

    public boolean isDefaultDisplay() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return false;
        }
        return displayContent.isDefaultDisplay;
    }

    public boolean isDimming() {
        DimLayerUser dimLayerUser = getDimLayerUser();
        DisplayContent dc = getDisplayContent();
        if (dimLayerUser == null || dc == null) {
            return false;
        }
        return dc.mDimLayerController.isDimming(dimLayerUser, this.mWinAnimator);
    }

    void setShowToOwnerOnlyLocked(boolean showToOwnerOnly) {
        this.mShowToOwnerOnly = showToOwnerOnly;
    }

    private boolean isHiddenFromUserLocked() {
        boolean z = false;
        WindowState win = getTopParentWindow();
        if (win.mAttrs.type < IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && win.mAppToken != null && win.mAppToken.mShowForAllUsers && win.mFrame.left <= win.mDisplayFrame.left && win.mFrame.top <= win.mDisplayFrame.top && win.mFrame.right >= win.mStableFrame.right && win.mFrame.bottom >= win.mStableFrame.bottom) {
            return false;
        }
        if (win.mShowToOwnerOnly) {
            z = this.mService.isCurrentProfileLocked(UserHandle.getUserId(win.mOwnerUid)) ^ 1;
        }
        return z;
    }

    private static void applyInsets(Region outRegion, Rect frame, Rect inset) {
        outRegion.set(frame.left + inset.left, frame.top + inset.top, frame.right - inset.right, frame.bottom - inset.bottom);
    }

    void getTouchableRegion(Region outRegion) {
        Rect frame = this.mFrame;
        switch (this.mTouchableInsets) {
            case 1:
                applyInsets(outRegion, frame, this.mGivenContentInsets);
                break;
            case 2:
                applyInsets(outRegion, frame, this.mGivenVisibleInsets);
                break;
            case 3:
                outRegion.set(this.mGivenTouchableRegion);
                outRegion.translate(frame.left, frame.top);
                break;
            default:
                outRegion.set(frame);
                break;
        }
        cropRegionToStackBoundsIfNeeded(outRegion);
    }

    private void cropRegionToStackBoundsIfNeeded(Region region) {
        Task task = getTask();
        if (task != null && (task.cropWindowsToStackBounds() ^ 1) == 0) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
                region.op(this.mTmpRect, Op.INTERSECT);
            }
        }
    }

    void reportFocusChangedSerialized(boolean focused, boolean inTouchMode) {
        try {
            this.mClient.windowFocusChanged(focused, inTouchMode);
            String winTitle = this.mAttrs.getTitle().toString();
            if (winTitle != null && winTitle.contains("Toast")) {
                Slog.w(TAG, "EGLdebug  callers=" + Debug.getCallers(8));
            }
        } catch (RemoteException e) {
        }
        if (this.mFocusCallbacks != null) {
            int N = this.mFocusCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IWindowFocusObserver obs = (IWindowFocusObserver) this.mFocusCallbacks.getBroadcastItem(i);
                if (focused) {
                    try {
                        obs.focusGained(this.mWindowId.asBinder());
                    } catch (RemoteException e2) {
                    }
                } else {
                    obs.focusLost(this.mWindowId.asBinder());
                }
            }
            this.mFocusCallbacks.finishBroadcast();
        }
    }

    public Configuration getConfiguration() {
        if (this.mAppToken == null || this.mAppToken.mFrozenMergedConfig.size() <= 0) {
            return super.getConfiguration();
        }
        return (Configuration) this.mAppToken.mFrozenMergedConfig.peek();
    }

    void reportResized() {
        Trace.traceBegin(32, "wm.reportResized_" + getWindowTag());
        try {
            Rect frame;
            final MergedConfiguration mergedConfiguration = new MergedConfiguration(this.mService.mRoot.getConfiguration(), getMergedOverrideConfiguration());
            setLastReportedMergedConfiguration(mergedConfiguration);
            onChangeConfiguration(mergedConfiguration);
            if (this.mEnforceSizeCompat) {
                frame = this.mCompatFrame;
            } else {
                frame = this.mFrame;
            }
            final Rect overscanInsets = this.mLastOverscanInsets;
            final Rect contentInsets = this.mLastContentInsets;
            final Rect visibleInsets = this.mLastVisibleInsets;
            final Rect stableInsets = this.mLastStableInsets;
            final Rect outsets = this.mLastOutsets;
            final boolean reportDraw = this.mWinAnimator.mDrawState == 1;
            final boolean reportOrientation = this.mReportOrientationChanged;
            final int displayId = getDisplayId();
            if (this.mAttrs.type == 3 || !(this.mClient instanceof IWindow.Stub)) {
                dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, reportOrientation, displayId);
            } else {
                this.mService.mH.post(new Runnable() {
                    public void run() {
                        try {
                            WindowState.this.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, reportOrientation, displayId);
                        } catch (RemoteException e) {
                        }
                    }
                });
            }
            if (this.mService.mAccessibilityController != null && getDisplayId() == 0) {
                this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            this.mOverscanInsetsChanged = false;
            this.mContentInsetsChanged = false;
            this.mVisibleInsetsChanged = false;
            this.mStableInsetsChanged = false;
            this.mOutsetsChanged = false;
            this.mFrameSizeChanged = false;
            this.mResizedWhileNotDragResizingReported = true;
            this.mWinAnimator.mSurfaceResized = false;
            this.mReportOrientationChanged = false;
        } catch (RemoteException e) {
            this.mOrientationChanging = false;
            this.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
            this.mOverscanInsetsChanged = false;
            this.mContentInsetsChanged = false;
            this.mVisibleInsetsChanged = false;
            this.mStableInsetsChanged = false;
            this.mWinAnimator.mSurfaceResized = false;
            Slog.w(TAG, "Failed to report 'resized' to the client of " + this + ", removing this window.");
            this.mService.mPendingRemove.add(this);
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
        Trace.traceEnd(32);
    }

    Rect getBackdropFrame(Rect frame) {
        int resizing = !isDragResizing() ? isDragResizeChanged() : 1;
        if (StackId.useWindowFrameForBackdrop(getStackId()) || (resizing ^ 1) != 0) {
            return frame;
        }
        DisplayInfo displayInfo = getDisplayInfo();
        this.mTmpRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        return this.mTmpRect;
    }

    public int getStackId() {
        TaskStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mStackId;
    }

    private void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, boolean reportOrientation, int displayId) throws RemoteException {
        boolean forceRelayout;
        if (isDragResizeChanged() || this.mResizedWhileNotDragResizing) {
            forceRelayout = true;
        } else {
            forceRelayout = reportOrientation;
        }
        this.mClient.resized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, getBackdropFrame(frame), forceRelayout, this.mPolicy.isNavBarForcedShownLw(this), displayId);
        this.mDragResizingChangeReported = true;
    }

    public void registerFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mFocusCallbacks == null) {
                    this.mFocusCallbacks = new RemoteCallbackList();
                }
                this.mFocusCallbacks.register(observer);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void unregisterFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mFocusCallbacks != null) {
                    this.mFocusCallbacks.unregister(observer);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean isFocused() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mService.mCurrentFocus == this;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        return z;
    }

    boolean inFreeformWorkspace() {
        Task task = getTask();
        return task != null ? task.inFreeformWorkspace() : false;
    }

    public boolean isInMultiWindowMode() {
        Task task = getTask();
        return task != null ? task.isFullscreen() ^ 1 : false;
    }

    private boolean inFullscreenContainer() {
        if (((IS_FULL_SCREEN || (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer())) && this.mService != null && (this.mService.mKeyguardGoingAway ^ 1) != 0 && this.mService.isKeyguardLocked() && (this.mAttrs.flags & DumpState.DUMP_FROZEN) != 0) || this.mAppToken == null) {
            return true;
        }
        if (this.mAppToken.hasBounds()) {
            return false;
        }
        return isInMultiWindowMode() ^ 1;
    }

    private void getContainerBounds(Rect outBounds) {
        if (isInMultiWindowMode()) {
            getTask().getBounds(outBounds);
        } else if (this.mAppToken != null) {
            this.mAppToken.getBounds(outBounds);
        } else {
            outBounds.setEmpty();
        }
    }

    boolean isDragResizeChanged() {
        return this.mDragResizing != computeDragResizing();
    }

    void setWaitingForDrawnIfResizingChanged() {
        if (isDragResizeChanged()) {
            this.mService.mWaitingForDrawn.add(this);
        }
        super.setWaitingForDrawnIfResizingChanged();
    }

    private boolean isDragResizingChangeReported() {
        return this.mDragResizingChangeReported;
    }

    /* renamed from: resetDragResizingChangeReported */
    void -com_android_server_wm_DockedStackDividerController-mthref-0() {
        this.mDragResizingChangeReported = false;
        super.resetDragResizingChangeReported();
    }

    private void setResizedWhileNotDragResizing(boolean resizedWhileNotDragResizing) {
        this.mResizedWhileNotDragResizing = resizedWhileNotDragResizing;
        this.mResizedWhileNotDragResizingReported = resizedWhileNotDragResizing ^ 1;
    }

    boolean isResizedWhileNotDragResizing() {
        return this.mResizedWhileNotDragResizing;
    }

    private boolean isResizedWhileNotDragResizingReported() {
        return this.mResizedWhileNotDragResizingReported;
    }

    int getResizeMode() {
        return this.mResizeMode;
    }

    private boolean computeDragResizing() {
        boolean z = false;
        Task task = getTask();
        if (task == null || !StackId.isStackAffectedByDragResizing(getStackId()) || this.mAttrs.width != -1 || this.mAttrs.height != -1) {
            return false;
        }
        if (task.isDragResizing()) {
            return true;
        }
        if ((getDisplayContent().mDividerControllerLocked.isResizing() || !(this.mAppToken == null || (this.mAppToken.mFrozenBounds.isEmpty() ^ 1) == 0)) && (task.inFreeformWorkspace() ^ 1) != 0) {
            z = isGoneForLayoutLw() ^ 1;
        }
        return z;
    }

    void setDragResizing() {
        boolean resizing = computeDragResizing();
        if (resizing != this.mDragResizing) {
            this.mDragResizing = resizing;
            Task task = getTask();
            if (task == null || !task.isDragResizing()) {
                int i;
                if (this.mDragResizing && getDisplayContent().mDividerControllerLocked.isResizing()) {
                    i = 1;
                } else {
                    i = 0;
                }
                this.mResizeMode = i;
            } else {
                this.mResizeMode = task.getDragResizeMode();
            }
        }
    }

    boolean isDragResizing() {
        return this.mDragResizing;
    }

    boolean isDockedResizing() {
        if (this.mDragResizing && getResizeMode() == 1) {
            return true;
        }
        return isChildWindow() ? getParentWindow().isDockedResizing() : false;
    }

    void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        TaskStack stack = getStack();
        pw.print(prefix);
        pw.print("mDisplayId=");
        pw.print(getDisplayId());
        if (stack != null) {
            pw.print(" stackId=");
            pw.print(stack.mStackId);
        }
        pw.print(" mSession=");
        pw.print(this.mSession);
        pw.print(" mClient=");
        pw.println(this.mClient.asBinder());
        pw.print(prefix);
        pw.print("mOwnerUid=");
        pw.print(this.mOwnerUid);
        pw.print(" mShowToOwnerOnly=");
        pw.print(this.mShowToOwnerOnly);
        pw.print(" package=");
        pw.print(this.mAttrs.packageName);
        pw.print(" appop=");
        pw.println(AppOpsManager.opToName(this.mAppOp));
        pw.print(prefix);
        pw.print("mAttrs=");
        pw.println(this.mAttrs);
        pw.print(prefix);
        pw.print("Requested w=");
        pw.print(this.mRequestedWidth);
        pw.print(" h=");
        pw.print(this.mRequestedHeight);
        pw.print(" mLayoutSeq=");
        pw.println(this.mLayoutSeq);
        if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
            pw.print(prefix);
            pw.print("LastRequested w=");
            pw.print(this.mLastRequestedWidth);
            pw.print(" h=");
            pw.println(this.mLastRequestedHeight);
        }
        if (this.mIsChildWindow || this.mLayoutAttached) {
            pw.print(prefix);
            pw.print("mParentWindow=");
            pw.print(getParentWindow());
            pw.print(" mLayoutAttached=");
            pw.println(this.mLayoutAttached);
        }
        if (this.mIsImWindow || this.mIsWallpaper || this.mIsFloatingLayer) {
            pw.print(prefix);
            pw.print("mIsImWindow=");
            pw.print(this.mIsImWindow);
            pw.print(" mIsWallpaper=");
            pw.print(this.mIsWallpaper);
            pw.print(" mIsFloatingLayer=");
            pw.print(this.mIsFloatingLayer);
            pw.print(" mWallpaperVisible=");
            pw.println(this.mWallpaperVisible);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mBaseLayer=");
            pw.print(this.mBaseLayer);
            pw.print(" mSubLayer=");
            pw.print(this.mSubLayer);
            pw.print(" mAnimLayer=");
            pw.print(this.mLayer);
            pw.print("+");
            pw.print(getAnimLayerAdjustment());
            pw.print("=");
            pw.print(this.mWinAnimator.mAnimLayer);
            pw.print(" mLastLayer=");
            pw.println(this.mWinAnimator.mLastLayer);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mToken=");
            pw.println(this.mToken);
            if (this.mAppToken != null) {
                pw.print(prefix);
                pw.print("mAppToken=");
                pw.println(this.mAppToken);
                pw.print(prefix);
                pw.print(" isAnimatingWithSavedSurface()=");
                pw.print(isAnimatingWithSavedSurface());
                pw.print(" mAppDied=");
                pw.println(this.mAppDied);
            }
            pw.print(prefix);
            pw.print("mViewVisibility=0x");
            pw.print(Integer.toHexString(this.mViewVisibility));
            pw.print(" mHaveFrame=");
            pw.print(this.mHaveFrame);
            pw.print(" mObscured=");
            pw.println(this.mObscured);
            pw.print(prefix);
            pw.print("mSeq=");
            pw.print(this.mSeq);
            pw.print(" mSystemUiVisibility=0x");
            pw.println(Integer.toHexString(this.mSystemUiVisibility));
        }
        if (!this.mPolicyVisibility || (this.mPolicyVisibilityAfterAnim ^ 1) != 0 || (this.mAppOpVisibility ^ 1) != 0 || isParentWindowHidden() || this.mPermanentlyHidden || this.mForceHideNonSystemOverlayWindow) {
            pw.print(prefix);
            pw.print("mPolicyVisibility=");
            pw.print(this.mPolicyVisibility);
            pw.print(" mPolicyVisibilityAfterAnim=");
            pw.print(this.mPolicyVisibilityAfterAnim);
            pw.print(" mAppOpVisibility=");
            pw.print(this.mAppOpVisibility);
            pw.print(" parentHidden=");
            pw.print(isParentWindowHidden());
            pw.print(" mForceHideNonSystemOverlayWindow=");
            pw.println(this.mForceHideNonSystemOverlayWindow);
        }
        if (!this.mRelayoutCalled || this.mLayoutNeeded) {
            pw.print(prefix);
            pw.print("mRelayoutCalled=");
            pw.print(this.mRelayoutCalled);
            pw.print(" mLayoutNeeded=");
            pw.println(this.mLayoutNeeded);
        }
        if (!(this.mXOffset == 0 && this.mYOffset == 0)) {
            pw.print(prefix);
            pw.print("Offsets x=");
            pw.print(this.mXOffset);
            pw.print(" y=");
            pw.println(this.mYOffset);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mGivenContentInsets=");
            this.mGivenContentInsets.printShortString(pw);
            pw.print(" mGivenVisibleInsets=");
            this.mGivenVisibleInsets.printShortString(pw);
            pw.println();
            if (this.mTouchableInsets != 0 || this.mGivenInsetsPending) {
                pw.print(prefix);
                pw.print("mTouchableInsets=");
                pw.print(this.mTouchableInsets);
                pw.print(" mGivenInsetsPending=");
                pw.println(this.mGivenInsetsPending);
                Region region = new Region();
                getTouchableRegion(region);
                pw.print(prefix);
                pw.print("touchable region=");
                pw.println(region);
            }
            pw.print(prefix);
            pw.print("mFullConfiguration=");
            pw.println(getConfiguration());
            pw.print(prefix);
            pw.print("mLastReportedConfiguration=");
            pw.println(getLastReportedConfiguration());
        }
        pw.print(prefix);
        pw.print("mHasSurface=");
        pw.print(this.mHasSurface);
        pw.print(" mShownPosition=");
        this.mShownPosition.printShortString(pw);
        pw.print(" isReadyForDisplay()=");
        pw.print(isReadyForDisplay());
        pw.print(" hasSavedSurface()=");
        pw.print(hasSavedSurface());
        pw.print(" mWindowRemovalAllowed=");
        pw.println(this.mWindowRemovalAllowed);
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mFrame=");
            this.mFrame.printShortString(pw);
            pw.print(" last=");
            this.mLastFrame.printShortString(pw);
            pw.println();
        }
        if (this.mEnforceSizeCompat) {
            pw.print(prefix);
            pw.print("mCompatFrame=");
            this.mCompatFrame.printShortString(pw);
            pw.println();
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("Frames: containing=");
            this.mContainingFrame.printShortString(pw);
            pw.print(" parent=");
            this.mParentFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    display=");
            this.mDisplayFrame.printShortString(pw);
            pw.print(" overscan=");
            this.mOverscanFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    content=");
            this.mContentFrame.printShortString(pw);
            pw.print(" visible=");
            this.mVisibleFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    decor=");
            this.mDecorFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("    outset=");
            this.mOutsetFrame.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("Cur insets: overscan=");
            this.mOverscanInsets.printShortString(pw);
            pw.print(" content=");
            this.mContentInsets.printShortString(pw);
            pw.print(" visible=");
            this.mVisibleInsets.printShortString(pw);
            pw.print(" stable=");
            this.mStableInsets.printShortString(pw);
            pw.print(" surface=");
            this.mAttrs.surfaceInsets.printShortString(pw);
            pw.print(" outsets=");
            this.mOutsets.printShortString(pw);
            pw.println();
            pw.print(prefix);
            pw.print("Lst insets: overscan=");
            this.mLastOverscanInsets.printShortString(pw);
            pw.print(" content=");
            this.mLastContentInsets.printShortString(pw);
            pw.print(" visible=");
            this.mLastVisibleInsets.printShortString(pw);
            pw.print(" stable=");
            this.mLastStableInsets.printShortString(pw);
            pw.print(" physical=");
            this.mLastOutsets.printShortString(pw);
            pw.print(" outset=");
            this.mLastOutsets.printShortString(pw);
            pw.println();
        }
        pw.print(prefix);
        pw.print(this.mWinAnimator);
        pw.println(":");
        this.mWinAnimator.dump(pw, prefix + "  ", dumpAll);
        if (this.mAnimatingExit || this.mRemoveOnExit || this.mDestroying || this.mRemoved) {
            pw.print(prefix);
            pw.print("mAnimatingExit=");
            pw.print(this.mAnimatingExit);
            pw.print(" mRemoveOnExit=");
            pw.print(this.mRemoveOnExit);
            pw.print(" mDestroying=");
            pw.print(this.mDestroying);
            pw.print(" mRemoved=");
            pw.println(this.mRemoved);
        }
        if (this.mOrientationChanging || this.mAppFreezing || this.mTurnOnScreen || this.mReportOrientationChanged) {
            pw.print(prefix);
            pw.print("mOrientationChanging=");
            pw.print(this.mOrientationChanging);
            pw.print(" mAppFreezing=");
            pw.print(this.mAppFreezing);
            pw.print(" mTurnOnScreen=");
            pw.print(this.mTurnOnScreen);
            pw.print(" mReportOrientationChanged=");
            pw.println(this.mReportOrientationChanged);
        }
        if (this.mLastFreezeDuration != 0) {
            pw.print(prefix);
            pw.print("mLastFreezeDuration=");
            TimeUtils.formatDuration((long) this.mLastFreezeDuration, pw);
            pw.println();
        }
        if (!(this.mHScale == 1.0f && this.mVScale == 1.0f)) {
            pw.print(prefix);
            pw.print("mHScale=");
            pw.print(this.mHScale);
            pw.print(" mVScale=");
            pw.println(this.mVScale);
        }
        if (!(this.mWallpaperX == -1.0f && this.mWallpaperY == -1.0f)) {
            pw.print(prefix);
            pw.print("mWallpaperX=");
            pw.print(this.mWallpaperX);
            pw.print(" mWallpaperY=");
            pw.println(this.mWallpaperY);
        }
        if (!(this.mWallpaperXStep == -1.0f && this.mWallpaperYStep == -1.0f)) {
            pw.print(prefix);
            pw.print("mWallpaperXStep=");
            pw.print(this.mWallpaperXStep);
            pw.print(" mWallpaperYStep=");
            pw.println(this.mWallpaperYStep);
        }
        if (!(this.mWallpaperDisplayOffsetX == Integer.MIN_VALUE && this.mWallpaperDisplayOffsetY == Integer.MIN_VALUE)) {
            pw.print(prefix);
            pw.print("mWallpaperDisplayOffsetX=");
            pw.print(this.mWallpaperDisplayOffsetX);
            pw.print(" mWallpaperDisplayOffsetY=");
            pw.println(this.mWallpaperDisplayOffsetY);
        }
        if (this.mDrawLock != null) {
            pw.print(prefix);
            pw.println("mDrawLock=" + this.mDrawLock);
        }
        if (isDragResizing()) {
            pw.print(prefix);
            pw.println("isDragResizing=" + isDragResizing());
        }
        if (computeDragResizing()) {
            pw.print(prefix);
            pw.println("computeDragResizing=" + computeDragResizing());
        }
        pw.print(prefix);
        pw.println("canReceiveKeys=" + canReceiveKeys());
    }

    String getName() {
        return Integer.toHexString(System.identityHashCode(this)) + " " + getWindowTag();
    }

    CharSequence getWindowTag() {
        CharSequence tag = this.mAttrs.getTitle();
        if (tag == null || tag.length() <= 0) {
            return this.mAttrs.packageName;
        }
        return tag;
    }

    public String toString() {
        CharSequence title = getWindowTag();
        if (!(this.mStringNameCache != null && this.mLastTitle == title && this.mWasExiting == this.mAnimatingExit)) {
            this.mLastTitle = title;
            this.mWasExiting = this.mAnimatingExit;
            this.mStringNameCache = "Window{" + Integer.toHexString(System.identityHashCode(this)) + " u" + UserHandle.getUserId(this.mOwnerUid) + " " + this.mLastTitle + (this.mAnimatingExit ? " EXITING}" : "}");
        }
        return this.mStringNameCache;
    }

    void transformClipRectFromScreenToSurfaceSpace(Rect clipRect) {
        if (this.mHScale >= 0.0f) {
            clipRect.left = (int) (((float) clipRect.left) / this.mHScale);
            clipRect.right = (int) Math.ceil((double) (((float) clipRect.right) / this.mHScale));
        }
        if (this.mVScale >= 0.0f) {
            clipRect.top = (int) (((float) clipRect.top) / this.mVScale);
            clipRect.bottom = (int) Math.ceil((double) (((float) clipRect.bottom) / this.mVScale));
        }
    }

    void applyGravityAndUpdateFrame(Rect containingFrame, Rect displayFrame) {
        int w;
        int h;
        float x;
        float y;
        int pw = containingFrame.width();
        int ph = containingFrame.height();
        boolean inNonFullscreenContainer = inFullscreenContainer() ^ 1;
        int fitToDisplay = (getTask() == null || (inNonFullscreenContainer ^ 1) != 0) ? 1 : this.mAttrs.type != 1 ? ((this.mAttrs.flags & 512) != 0) ^ 1 : 0;
        if ((this.mAttrs.flags & 16384) != 0) {
            if (this.mAttrs.width < 0) {
                w = pw;
            } else if (this.mEnforceSizeCompat) {
                w = (int) ((((float) this.mAttrs.width) * this.mGlobalScale) + 0.5f);
            } else {
                w = this.mAttrs.width;
            }
            if (this.mAttrs.height < 0) {
                h = ph;
            } else if (this.mEnforceSizeCompat) {
                h = (int) ((((float) this.mAttrs.height) * this.mGlobalScale) + 0.5f);
            } else {
                h = this.mAttrs.height;
            }
        } else {
            if (this.mAttrs.width == -1) {
                w = pw;
            } else if (this.mEnforceSizeCompat) {
                w = (int) ((((float) this.mRequestedWidth) * this.mGlobalScale) + 0.5f);
            } else {
                w = this.mRequestedWidth;
            }
            if (this.mAttrs.height == -1) {
                h = ph;
            } else if (this.mEnforceSizeCompat) {
                h = (int) ((((float) this.mRequestedHeight) * this.mGlobalScale) + 0.5f);
            } else {
                h = this.mRequestedHeight;
            }
        }
        if (!this.mEnforceSizeCompat || (isOutOfDisplayFrameEnable() ^ 1) == 0) {
            x = (float) this.mAttrs.x;
            y = (float) this.mAttrs.y;
        } else {
            x = ((float) this.mAttrs.x) * this.mGlobalScale;
            y = ((float) this.mAttrs.y) * this.mGlobalScale;
        }
        if (inNonFullscreenContainer && (layoutInParentFrame() ^ 1) != 0) {
            w = Math.min(w, pw);
            h = Math.min(h, ph);
        }
        Gravity.apply(this.mAttrs.gravity, w, h, containingFrame, (int) ((this.mAttrs.horizontalMargin * ((float) pw)) + x), (int) ((this.mAttrs.verticalMargin * ((float) ph)) + y), this.mFrame);
        if (fitToDisplay != 0) {
            Gravity.applyDisplay(this.mAttrs.gravity, displayFrame, this.mFrame);
        }
        this.mCompatFrame.set(this.mFrame);
        if (this.mEnforceSizeCompat) {
            this.mCompatFrame.scale(this.mInvGlobalScale);
            String title = getWindowTag() != null ? getWindowTag().toString() : null;
            if (title == null || !title.equals("Toast") || this.mRequestedWidth - this.mCompatFrame.width() != 1) {
                return;
            }
            Rect rect;
            if (this.mFrame.right + 1 <= containingFrame.right) {
                rect = this.mFrame;
                rect.right++;
                rect = this.mCompatFrame;
                rect.right++;
            } else if (this.mFrame.left - 1 >= containingFrame.left) {
                rect = this.mFrame;
                rect.left--;
                rect = this.mCompatFrame;
                rect.left--;
            }
        }
    }

    boolean isOutOfDisplayFrameEnable() {
        return (this.mAttrs.hwFlags & DumpState.DUMP_DEXOPT) != 0;
    }

    boolean isChildWindow() {
        return this.mIsChildWindow;
    }

    boolean layoutInParentFrame() {
        if (!this.mIsChildWindow || (this.mAttrs.privateFlags & 65536) == 0) {
            return false;
        }
        return true;
    }

    boolean hideNonSystemOverlayWindowsWhenVisible() {
        if ((this.mAttrs.privateFlags & DumpState.DUMP_FROZEN) != 0) {
            return this.mSession.mCanHideNonSystemOverlayWindows;
        }
        return false;
    }

    WindowState getParentWindow() {
        return this.mIsChildWindow ? (WindowState) super.getParent() : null;
    }

    WindowState getTopParentWindow() {
        WindowState current = this;
        WindowState topParent = this;
        while (current != null && current.mIsChildWindow) {
            current = current.getParentWindow();
            if (current != null) {
                topParent = current;
            }
        }
        return topParent;
    }

    boolean isParentWindowHidden() {
        WindowState parent = getParentWindow();
        return parent != null ? parent.mHidden : false;
    }

    void setWillReplaceWindow(boolean animate) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceWindow(animate);
        }
        if ((this.mAttrs.privateFlags & 32768) == 0 && this.mAttrs.type != 3) {
            this.mWillReplaceWindow = true;
            this.mReplacementWindow = null;
            this.mAnimateReplacingWindow = animate;
        }
    }

    void clearWillReplaceWindow() {
        this.mWillReplaceWindow = false;
        this.mReplacementWindow = null;
        this.mAnimateReplacingWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWillReplaceWindow();
        }
    }

    boolean waitingForReplacement() {
        if (this.mWillReplaceWindow) {
            return true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).waitingForReplacement()) {
                return true;
            }
        }
        return false;
    }

    void requestUpdateWallpaperIfNeeded() {
        DisplayContent dc = getDisplayContent();
        if (!(dc == null || (this.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0)) {
            dc.pendingLayoutChanges |= 4;
            dc.setLayoutNeeded();
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).requestUpdateWallpaperIfNeeded();
        }
    }

    float translateToWindowX(float x) {
        float winX = x - ((float) this.mFrame.left);
        if (this.mEnforceSizeCompat) {
            return winX * this.mGlobalScale;
        }
        return winX;
    }

    float translateToWindowY(float y) {
        float winY = y - ((float) this.mFrame.top);
        if (this.mEnforceSizeCompat) {
            return winY * this.mGlobalScale;
        }
        return winY;
    }

    private void transferDimToReplacement() {
        boolean z = false;
        DimLayerUser dimLayerUser = getDimLayerUser();
        DisplayContent dc = getDisplayContent();
        if (dimLayerUser != null && dc != null) {
            DimLayerController dimLayerController = dc.mDimLayerController;
            WindowStateAnimator windowStateAnimator = this.mReplacementWindow.mWinAnimator;
            if ((this.mAttrs.flags & 2) != 0) {
                z = true;
            }
            dimLayerController.applyDim(dimLayerUser, windowStateAnimator, z);
        }
    }

    boolean shouldBeReplacedWithChildren() {
        if (this.mIsChildWindow || this.mAttrs.type == 2 || this.mAttrs.type == 4) {
            return true;
        }
        return false;
    }

    void setWillReplaceChildWindows() {
        if (shouldBeReplacedWithChildren()) {
            setWillReplaceWindow(false);
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceChildWindows();
        }
    }

    WindowState getReplacingWindow() {
        if (this.mAnimatingExit && this.mWillReplaceWindow && this.mAnimateReplacingWindow) {
            return this;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowState replacing = ((WindowState) this.mChildren.get(i)).getReplacingWindow();
            if (replacing != null) {
                return replacing;
            }
        }
        return null;
    }

    public int getRotationAnimationHint() {
        if (this.mAppToken != null) {
            return this.mAppToken.mRotationAnimationHint;
        }
        return -1;
    }

    public boolean isInputMethodWindow() {
        return this.mIsImWindow;
    }

    boolean performShowLocked() {
        if (isHiddenFromUserLocked()) {
            hideLw(false);
            return false;
        }
        logPerformShow("performShow on ");
        int drawState = this.mWinAnimator.mDrawState;
        if (!((drawState != 4 && drawState != 3) || this.mAttrs.type == 3 || this.mAppToken == null)) {
            this.mAppToken.onFirstWindowDrawn(this, this.mWinAnimator);
        }
        if (this.mWinAnimator.mDrawState != 3 || (isReadyForDisplay() ^ 1) != 0) {
            return false;
        }
        logPerformShow("Showing ");
        this.mService.enableScreenIfNeededLocked();
        this.mWinAnimator.applyEnterAnimationLocked();
        this.mWinAnimator.mLastAlpha = -1.0f;
        this.mWinAnimator.mDrawState = 4;
        if (this.mStartWindowsAhead && this.mAttrs.type == 3 && this.mWinAnimator.mAppAnimator != null && this.mWinAnimator.mAppAnimator.hasTransformation && this.mWinAnimator.mAppAnimator.animation != null) {
            this.mWinAnimator.mAppAnimator.stepAnimationLocked(SystemClock.uptimeMillis() - 12);
            this.mWinAnimator.prepareSurfaceLocked(true);
            this.mWinAnimator.mAppAnimator.stepAnimationLocked(SystemClock.uptimeMillis() - 4);
            this.mWinAnimator.prepareSurfaceLocked(true);
        }
        this.mService.scheduleAnimationLocked();
        if (this.mHidden) {
            this.mHidden = false;
            DisplayContent displayContent = getDisplayContent();
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                WindowState c = (WindowState) this.mChildren.get(i);
                if (c.mWinAnimator.mSurfaceController != null) {
                    c.performShowLocked();
                    if (displayContent != null) {
                        displayContent.setLayoutNeeded();
                    }
                }
            }
        }
        if (this.mAttrs.type == 2011) {
            getDisplayContent().mDividerControllerLocked.resetImeHideRequested();
        }
        return true;
    }

    private void logPerformShow(String prefix) {
    }

    WindowInfo getWindowInfo() {
        boolean z = false;
        WindowInfo windowInfo = WindowInfo.obtain();
        windowInfo.type = this.mAttrs.type;
        windowInfo.layer = this.mLayer;
        windowInfo.token = this.mClient.asBinder();
        windowInfo.title = this.mAttrs.accessibilityTitle;
        windowInfo.accessibilityIdOfAnchor = this.mAttrs.accessibilityIdOfAnchor;
        windowInfo.focused = isFocused();
        Task task = getTask();
        if (task != null) {
            z = task.inPinnedWorkspace();
        }
        windowInfo.inPictureInPicture = z;
        if (this.mIsChildWindow) {
            windowInfo.parentToken = getParentWindow().mClient.asBinder();
        }
        int childCount = this.mChildren.size();
        if (childCount > 0) {
            if (windowInfo.childTokens == null) {
                windowInfo.childTokens = new ArrayList(childCount);
            }
            for (int j = 0; j < childCount; j++) {
                windowInfo.childTokens.add(((WindowState) this.mChildren.get(j)).mClient.asBinder());
            }
        }
        return windowInfo;
    }

    int getHighestAnimLayer() {
        int highest = this.mWinAnimator.mAnimLayer;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            int childLayer = ((WindowState) this.mChildren.get(i)).getHighestAnimLayer();
            if (childLayer > highest) {
                highest = childLayer;
            }
        }
        return highest;
    }

    boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (this.mChildren.isEmpty()) {
            return applyInOrderWithImeWindows(callback, traverseTopToBottom);
        }
        if (traverseTopToBottom) {
            return forAllWindowTopToBottom(callback);
        }
        return forAllWindowBottomToTop(callback);
    }

    private boolean forAllWindowBottomToTop(ToBooleanFunction<WindowState> callback) {
        int i = 0;
        int count = this.mChildren.size();
        WindowState child = (WindowState) this.mChildren.get(0);
        while (i < count && child.mSubLayer < 0) {
            if (child.applyInOrderWithImeWindows(callback, false)) {
                return true;
            }
            i++;
            if (i >= count) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        if (applyInOrderWithImeWindows(callback, false)) {
            return true;
        }
        while (i < count) {
            if (child.applyInOrderWithImeWindows(callback, false)) {
                return true;
            }
            i++;
            if (i >= count) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        return false;
    }

    private boolean forAllWindowTopToBottom(ToBooleanFunction<WindowState> callback) {
        int i = this.mChildren.size() - 1;
        WindowState child = (WindowState) this.mChildren.get(i);
        while (i >= 0 && child.mSubLayer >= 0) {
            if (child.applyInOrderWithImeWindows(callback, true)) {
                return true;
            }
            i--;
            if (i < 0) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        if (applyInOrderWithImeWindows(callback, true)) {
            return true;
        }
        while (i >= 0) {
            if (child.applyInOrderWithImeWindows(callback, true)) {
                return true;
            }
            i--;
            if (i < 0) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        return false;
    }

    private boolean applyInOrderWithImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            if ((this.mService.mInputMethodTarget == this && getDisplayContent().forAllImeWindows(callback, traverseTopToBottom)) || callback.apply(this)) {
                return true;
            }
        } else if (callback.apply(this)) {
            return true;
        } else {
            if (this.mService.mInputMethodTarget == this && getDisplayContent().forAllImeWindows(callback, traverseTopToBottom)) {
                return true;
            }
        }
        return false;
    }

    WindowState getWindow(Predicate<WindowState> callback) {
        if (this.mChildren.isEmpty()) {
            if (!callback.test(this)) {
                this = null;
            }
            return this;
        }
        int i = this.mChildren.size() - 1;
        WindowState child = (WindowState) this.mChildren.get(i);
        while (i >= 0 && child.mSubLayer >= 0) {
            if (callback.test(child)) {
                return child;
            }
            i--;
            if (i < 0) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        if (callback.test(this)) {
            return this;
        }
        while (i >= 0) {
            if (callback.test(child)) {
                return child;
            }
            i--;
            if (i < 0) {
                break;
            }
            child = (WindowState) this.mChildren.get(i);
        }
        return null;
    }

    boolean isWindowAnimationSet() {
        if (this.mWinAnimator.isWindowAnimationSet()) {
            return true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((WindowState) this.mChildren.get(i)).isWindowAnimationSet()) {
                return true;
            }
        }
        return false;
    }

    void onExitAnimationDone() {
        if (!this.mChildren.isEmpty()) {
            LinkedList<WindowState> childWindows = new LinkedList(this.mChildren);
            for (int i = childWindows.size() - 1; i >= 0; i--) {
                ((WindowState) childWindows.get(i)).onExitAnimationDone();
            }
        }
        if (this.mWinAnimator.mEnteringAnimation) {
            this.mWinAnimator.mEnteringAnimation = false;
            this.mService.requestTraversal();
            if (this.mAppToken == null) {
                try {
                    this.mClient.dispatchWindowShown();
                } catch (RemoteException e) {
                }
            }
        }
        if (!(this.mWinAnimator.isWindowAnimationSet() || this.mService.mAccessibilityController == null || getDisplayId() != 0)) {
            this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
        }
        if (this.mAnimatingExit && !this.mWinAnimator.isWindowAnimationSet()) {
            Slog.v(TAG, "Exit animation finished in " + this + ": remove=" + this.mRemoveOnExit);
            this.mDestroying = true;
            boolean hasSurface = this.mWinAnimator.hasSurface();
            if (hasSurface) {
                this.mWinAnimator.hide("onExitAnimationDone");
            }
            if (this.mAppToken != null) {
                this.mAppToken.destroySurfaces();
            } else {
                if (hasSurface) {
                    this.mService.mDestroySurface.add(this);
                }
                if (this.mRemoveOnExit) {
                    this.mService.mPendingRemove.add(this);
                    this.mRemoveOnExit = false;
                }
            }
            this.mAnimatingExit = false;
            getDisplayContent().mWallpaperController.hideWallpapers(this);
        }
    }

    boolean clearAnimatingFlags() {
        boolean didSomething = false;
        if (!(this.mWillReplaceWindow || (this.mRemoveOnExit ^ 1) == 0)) {
            if (this.mAnimatingExit) {
                this.mAnimatingExit = false;
                didSomething = true;
            }
            if (this.mWinAnimator.mAnimating) {
                this.mWinAnimator.mAnimating = false;
                didSomething = true;
            }
            if (this.mDestroying) {
                this.mDestroying = false;
                this.mService.mDestroySurface.remove(this);
                didSomething = true;
            }
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            didSomething |= ((WindowState) this.mChildren.get(i)).clearAnimatingFlags();
        }
        return didSomething;
    }

    public boolean isRtl() {
        return getConfiguration().getLayoutDirection() == 1;
    }

    void hideWallpaperWindow(boolean wasDeferred, String reason) {
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            ((WindowState) this.mChildren.get(j)).hideWallpaperWindow(wasDeferred, reason);
        }
        if (!this.mWinAnimator.mLastHidden || wasDeferred) {
            this.mWinAnimator.hide(reason);
            dispatchWallpaperVisibility(false);
            DisplayContent displayContent = getDisplayContent();
            if (displayContent != null) {
                displayContent.pendingLayoutChanges |= 4;
            }
        }
    }

    void dispatchWallpaperVisibility(boolean visible) {
        boolean hideAllowed = getDisplayContent().mWallpaperController.mDeferredHideWallpaper == null;
        if (this.mWallpaperVisible == visible) {
            return;
        }
        if (hideAllowed || visible) {
            this.mWallpaperVisible = visible;
            try {
                this.mClient.dispatchAppVisibility(visible);
            } catch (RemoteException e) {
            }
        }
    }

    boolean hasVisibleNotDrawnWallpaper() {
        if (this.mWallpaperVisible && (isDrawnLw() ^ 1) != 0) {
            return true;
        }
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowState) this.mChildren.get(j)).hasVisibleNotDrawnWallpaper()) {
                return true;
            }
        }
        return false;
    }

    void updateReportedVisibility(UpdateReportedVisibilityResults results) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).updateReportedVisibility(results);
        }
        if (!this.mAppFreezing && this.mViewVisibility == 0 && this.mAttrs.type != 3 && !this.mDestroying) {
            results.numInteresting++;
            if (isDrawnLw()) {
                results.numDrawn++;
                if (!this.mWinAnimator.isAnimationSet()) {
                    results.numVisible++;
                }
                results.nowGone = false;
            } else if (this.mWinAnimator.isAnimationSet()) {
                results.nowGone = false;
            }
        }
    }

    void calculatePolicyCrop(Rect policyCrop) {
        DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
        if (!isDefaultDisplay()) {
            policyCrop.set(0, 0, this.mCompatFrame.width(), this.mCompatFrame.height());
            policyCrop.intersect(-this.mCompatFrame.left, -this.mCompatFrame.top, displayInfo.logicalWidth - this.mCompatFrame.left, displayInfo.logicalHeight - this.mCompatFrame.top);
        } else if (this.mLayer >= this.mService.mSystemDecorLayer) {
            policyCrop.set(0, 0, this.mCompatFrame.width(), this.mCompatFrame.height());
        } else if (this.mDecorFrame.isEmpty()) {
            policyCrop.set(0, 0, this.mCompatFrame.width(), this.mCompatFrame.height());
        } else {
            calculateSystemDecorRect(policyCrop);
        }
    }

    private void calculateSystemDecorRect(Rect systemDecorRect) {
        int i = 0;
        Rect decorRect = this.mDecorFrame;
        int width = this.mFrame.width();
        int height = this.mFrame.height();
        int left = this.mXOffset + this.mFrame.left;
        int top = this.mYOffset + this.mFrame.top;
        if (isDockedResizing()) {
            DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
            systemDecorRect.set(0, 0, Math.max(width, displayInfo.logicalWidth), Math.max(height, displayInfo.logicalHeight));
        } else {
            systemDecorRect.set(0, 0, width, height);
        }
        if (inFreeformWorkspace()) {
            i = isAnimatingLw();
        }
        if (i ^ 1) {
            systemDecorRect.intersect(decorRect.left - left, decorRect.top - top, decorRect.right - left, decorRect.bottom - top);
        }
        if (this.mEnforceSizeCompat && this.mInvGlobalScale != 1.0f) {
            float scale = this.mInvGlobalScale;
            systemDecorRect.left = (int) ((((float) systemDecorRect.left) * scale) - 0.5f);
            systemDecorRect.top = (int) ((((float) systemDecorRect.top) * scale) - 0.5f);
            systemDecorRect.right = (int) ((((float) systemDecorRect.right) * scale) + 0.5f);
            systemDecorRect.bottom = (int) ((((float) systemDecorRect.bottom) * scale) + 0.5f);
        }
    }

    void expandForSurfaceInsets(Rect r) {
        r.inset(-this.mAttrs.surfaceInsets.left, -this.mAttrs.surfaceInsets.top, -this.mAttrs.surfaceInsets.right, -this.mAttrs.surfaceInsets.bottom);
    }

    boolean surfaceInsetsChanging() {
        return this.mLastSurfaceInsets.equals(this.mAttrs.surfaceInsets) ^ 1;
    }

    int relayoutVisibleWindow(int result, int attrChanges, int oldVisibility) {
        int i;
        int i2 = 0;
        boolean wasVisible = isVisibleLw();
        if (wasVisible && (isDrawnLw() ^ 1) == 0) {
            i = 0;
        } else {
            i = 2;
        }
        result |= i;
        if (this.mAnimatingExit) {
            Slog.d(TAG, "relayoutVisibleWindow: " + this + " mAnimatingExit=true, mRemoveOnExit=" + this.mRemoveOnExit + ", mDestroying=" + this.mDestroying);
            this.mWinAnimator.cancelExitAnimationForNextAnimationLocked();
            this.mAnimatingExit = false;
        }
        if (this.mDestroying) {
            this.mDestroying = false;
            this.mService.mDestroySurface.remove(this);
        }
        if (oldVisibility == 8) {
            this.mWinAnimator.mEnterAnimationPending = true;
        }
        this.mLastVisibleLayoutRotation = getDisplayContent().getRotation();
        this.mWinAnimator.mEnteringAnimation = true;
        prepareWindowToDisplayDuringRelayout(wasVisible);
        if (wasVisible && !this.mService.mPowerManager.isScreenOn() && (this.mAttrs.flags & DumpState.DUMP_COMPILER_STATS) != 0 && this.mOwnerUid == NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE) {
            this.mService.mPowerManager.wakeUp(SystemClock.uptimeMillis());
        }
        boolean isFormatChanged = false;
        if (this.mAttrs != null && this.mAttrs.format == -3 && oldVisibility == 4) {
            isFormatChanged = true;
            Slog.i(TAG, "relayoutVisibleWindow: set isFormatChanged");
        }
        if ((isFormatChanged || (attrChanges & 8) != 0) && !this.mWinAnimator.tryChangeFormatInPlaceLocked()) {
            this.mWinAnimator.preserveSurfaceLocked();
            result |= 6;
        }
        if (isDragResizeChanged() || isResizedWhileNotDragResizing() || (surfaceInsetsChanging() && (inPinnedWorkspace() ^ 1) != 0)) {
            this.mLastSurfaceInsets.set(this.mAttrs.surfaceInsets);
            setDragResizing();
            setResizedWhileNotDragResizing(false);
            if (this.mHasSurface && (isChildWindow() ^ 1) != 0) {
                this.mWinAnimator.preserveSurfaceLocked();
                result |= 6;
            }
        }
        boolean freeformResizing = isDragResizing() ? getResizeMode() == 0 : false;
        boolean dockedResizing = isDragResizing() ? getResizeMode() == 1 : false;
        if (freeformResizing) {
            i = 16;
        } else {
            i = 0;
        }
        result |= i;
        if (dockedResizing) {
            i2 = 8;
        }
        result |= i2;
        if (isAnimatingWithSavedSurface()) {
            return result | 2;
        }
        return result;
    }

    boolean isLaidOut() {
        return this.mLayoutSeq != -1;
    }

    void updateLastInsetValues() {
        this.mLastOverscanInsets.set(this.mOverscanInsets);
        this.mLastContentInsets.set(this.mContentInsets);
        this.mLastVisibleInsets.set(this.mVisibleInsets);
        this.mLastStableInsets.set(this.mStableInsets);
        this.mLastOutsets.set(this.mOutsets);
    }

    boolean usesRelativeZOrdering() {
        if (isChildWindow() && this.mAttrs.type == 1004) {
            return true;
        }
        return false;
    }

    public boolean canCarryColors() {
        return this.mCanCarryColors;
    }

    public void setCanCarryColors(boolean carry) {
        this.mCanCarryColors = carry;
    }

    private boolean isStatusBarWindow() {
        if (this.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || this.mAttrs.type == 2014 || this.mAttrs.type == 2019 || this.mAttrs.type == 2024) {
            return true;
        }
        return false;
    }

    int getLowResolutionMode() {
        if (!isStatusBarWindow()) {
            IApsManager apsManager = (IApsManager) LocalServices.getService(IApsManager.class);
            if (apsManager == null) {
                return 0;
            }
            float resolutionRatio = apsManager.getResolution(getOwningPackage());
            return (0.0f >= resolutionRatio || resolutionRatio >= 1.0f) ? 0 : 2;
        } else if ((this.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || this.mAttrs.type == 2014) && getFrameLw().equals(getDisplayFrameLw())) {
            return 1;
        } else {
            return 2;
        }
    }

    private void onChangeConfiguration(MergedConfiguration mergedConfiguration) {
        if (HwPCUtils.enabled() && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getDisplayId()) && mergedConfiguration != null && getTask() != null && getTask().isFullscreen()) {
            Configuration cf = mergedConfiguration.getOverrideConfiguration();
            DisplayContent dc = getDisplayContent();
            if (cf != null && dc != null) {
                DisplayInfo displayInfo = getDisplayInfo();
                if (displayInfo != null) {
                    int displayWidth = displayInfo.logicalWidth;
                    int displayHeight = displayInfo.logicalHeight;
                    cf.setAppBounds(0, 0, displayWidth, displayHeight);
                    float scale = ((float) displayInfo.logicalDensityDpi) / 160.0f;
                    cf.screenWidthDp = (int) ((((float) displayWidth) / scale) + 0.5f);
                    cf.screenHeightDp = (int) ((((float) displayHeight) / scale) + 0.5f);
                    mergedConfiguration.setOverrideConfiguration(cf);
                    HwPCUtils.log(TAG, "set pc fullscreen, width:" + displayWidth + " height:" + displayHeight + " scale:" + scale + " cf.screenWidthDp:" + cf.screenWidthDp + " cf.screenHeightDp:" + cf.screenHeightDp);
                }
            }
        }
    }

    public boolean getHwNotchSupport() {
        return this.mAppToken != null ? this.mAppToken.getHwNotchSupport() : false;
    }
}
