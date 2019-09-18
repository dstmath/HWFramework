package com.android.server.wm;

import android.app.AppOpsManager;
import android.aps.IApsManager;
import android.content.Context;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.IApplicationToken;
import android.view.IWindow;
import android.view.IWindowFocusObserver;
import android.view.IWindowId;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowInfo;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputWindowHandle;
import com.android.server.job.controllers.JobStatus;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.LocalAnimationAdapter;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.utils.WmDisplayCutout;
import huawei.android.hwutil.HwFullScreenDisplay;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

public class WindowState extends WindowContainer<WindowState> implements WindowManagerPolicy.WindowState {
    private static final float DEFAULT_DIM_AMOUNT_DEAD_WINDOW = 0.5f;
    static final int HWFREEFORM_RESIZE_HANDLE_INSIZE_WIDTH_IN_DP = 10;
    static final int HWFREEFORM_RESIZE_HANDLE_WIDTH_IN_DP = 20;
    private static final boolean IS_FULL_SCREEN = HwFullScreenDisplay.isFullScreenDevice();
    private static final boolean IS_NOTCH_PROP = (!SystemProperties.get("ro.config.hw_notch_size", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS).equals(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS));
    public static final int LOW_RESOLUTION_COMPOSITION_OFF = 1;
    public static final int LOW_RESOLUTION_COMPOSITION_ON = 2;
    public static final int LOW_RESOLUTION_FEATURE_OFF = 0;
    static final int MINIMUM_VISIBLE_HEIGHT_IN_DP = 36;
    static final int MINIMUM_VISIBLE_WIDTH_IN_DP = 42;
    static final int RESIZE_HANDLE_WIDTH_IN_DP = 10;
    static final boolean SHOW_TRANSACTIONS = false;
    static final String TAG = "WindowManager";
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
    boolean isBinderDiedCalling;
    private boolean mAnimateReplacingWindow;
    boolean mAnimatingExit;
    boolean mAppDied;
    boolean mAppFreezing;
    final int mAppOp;
    public boolean mAppOpVisibility;
    AppWindowToken mAppToken;
    public final WindowManager.LayoutParams mAttrs;
    final int mBaseLayer;
    private boolean mCanCarryColors;
    final IWindow mClient;
    private InputChannel mClientChannel;
    final Rect mCompatFrame;
    final Rect mContainingFrame;
    boolean mContentChanged;
    private final Rect mContentFrame;
    final Rect mContentInsets;
    private boolean mContentInsetsChanged;
    final Context mContext;
    public int mCurrentFoldDisplayMode;
    private DeadWindowEventReceiver mDeadWindowEventReceiver;
    final DeathRecipient mDeathRecipient;
    final Rect mDecorFrame;
    private int mDecorTopCompensation;
    boolean mDestroying;
    WmDisplayCutout mDisplayCutout;
    private boolean mDisplayCutoutChanged;
    final Rect mDisplayFrame;
    private boolean mDragResizing;
    private boolean mDragResizingChangeReported;
    private PowerManager.WakeLock mDrawLock;
    private boolean mDrawnStateEvaluated;
    boolean mEnforceSizeCompat;
    private RemoteCallbackList<IWindowFocusObserver> mFocusCallbacks;
    int mForceCompatMode;
    private boolean mForceHideNonSystemOverlayWindow;
    final Rect mFrame;
    private long mFrameNumber;
    private boolean mFrameSizeChanged;
    final Rect mGivenContentInsets;
    boolean mGivenInsetsPending;
    final Region mGivenTouchableRegion;
    final Rect mGivenVisibleInsets;
    float mGlobalScale;
    float mHScale;
    boolean mHasSurface;
    boolean mHaveFrame;
    boolean mHidden;
    private boolean mHiddenWhileSuspended;
    IHwWindowStateEx mHwWSEx;
    boolean mInRelayout;
    InputChannel mInputChannel;
    final InputWindowHandle mInputWindowHandle;
    private final Rect mInsetFrame;
    float mInvGlobalScale;
    private boolean mIsChildWindow;
    private boolean mIsDimming;
    private final boolean mIsFloatingLayer;
    final boolean mIsImWindow;
    final boolean mIsWallpaper;
    final Rect mLastContentInsets;
    private WmDisplayCutout mLastDisplayCutout;
    final Rect mLastFrame;
    int mLastFreezeDuration;
    float mLastHScale;
    private final Rect mLastOutsets;
    private final Rect mLastOverscanInsets;
    final Rect mLastRelayoutContentInsets;
    private final MergedConfiguration mLastReportedConfiguration;
    private int mLastRequestedHeight;
    private int mLastRequestedWidth;
    private final Rect mLastStableInsets;
    final Rect mLastSurfaceInsets;
    CharSequence mLastTitle;
    float mLastVScale;
    private final Rect mLastVisibleInsets;
    int mLastVisibleLayoutRotation;
    int mLayer;
    final boolean mLayoutAttached;
    boolean mLayoutNeeded;
    int mLayoutSeq;
    public Point mLazyModeSurfacePosition;
    private boolean mMovedByResize;
    boolean mObscured;
    private boolean mOrientationChangeTimedOut;
    private boolean mOrientationChanging;
    private final Rect mOutsetFrame;
    final Rect mOutsets;
    private boolean mOutsetsChanged;
    private final Rect mOverscanFrame;
    final Rect mOverscanInsets;
    private boolean mOverscanInsetsChanged;
    protected Point mOverscanPosition;
    final boolean mOwnerCanAddInternalSystemWindow;
    final int mOwnerUid;
    final Rect mParentFrame;
    private boolean mParentFrameWasClippedByDisplayCutout;
    boolean mPermanentlyHidden;
    final WindowManagerPolicy mPolicy;
    boolean mPolicyVisibility;
    boolean mPolicyVisibilityAfterAnim;
    private PowerManagerWrapper mPowerManagerWrapper;
    boolean mRelayoutCalled;
    boolean mRemoveOnExit;
    boolean mRemoved;
    private WindowState mReplacementWindow;
    private boolean mReplacingRemoveRequested;
    boolean mReportOrientationChanged;
    int mRequestedHeight;
    int mRequestedWidth;
    private int mResizeMode;
    boolean mResizedWhileGone;
    /* access modifiers changed from: package-private */
    public boolean mSeamlesslyRotated;
    int mSeq;
    final Session mSession;
    private boolean mShowToOwnerOnly;
    boolean mSkipEnterAnimationForSeamlessReplacement;
    private final Rect mStableFrame;
    final Rect mStableInsets;
    private boolean mStableInsetsChanged;
    private String mStringNameCache;
    public Point mSubFoldModeSurfacePosition;
    final int mSubLayer;
    private final Point mSurfacePosition;
    int mSystemUiVisibility;
    private TapExcludeRegionHolder mTapExcludeRegionHolder;
    final Matrix mTmpMatrix;
    private final Rect mTmpRect;
    WindowToken mToken;
    int mTouchableInsets;
    float mVScale;
    int mViewVisibility;
    final Rect mVisibleFrame;
    final Rect mVisibleInsets;
    private boolean mVisibleInsetsChanged;
    int mWallpaperDisplayOffsetX;
    int mWallpaperDisplayOffsetY;
    boolean mWallpaperVisible;
    float mWallpaperX;
    float mWallpaperXStep;
    float mWallpaperY;
    float mWallpaperYStep;
    private boolean mWasExiting;
    private boolean mWasVisibleBeforeClientHidden;
    boolean mWillReplaceWindow;
    final WindowStateAnimator mWinAnimator;
    final WindowId mWindowId;
    boolean mWindowRemovalAllowed;

    private final class DeadWindowEventReceiver extends InputEventReceiver {
        DeadWindowEventReceiver(InputChannel inputChannel) {
            super(inputChannel, WindowState.this.mService.mH.getLooper());
        }

        public void onInputEvent(InputEvent event, int displayId) {
            finishInputEvent(event, true);
        }
    }

    private class DeathRecipient implements IBinder.DeathRecipient {
        private DeathRecipient() {
        }

        public void binderDied() {
            boolean resetSplitScreenResizing = false;
            try {
                synchronized (WindowState.this.mService.mWindowMap) {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState win = WindowState.this.mService.windowForClientLocked(WindowState.this.mSession, WindowState.this.mClient, false);
                    Slog.i(WindowState.TAG, "WIN DEATH: " + win);
                    if (win != null) {
                        if (WindowState.this.mService.mHwWMSEx.getSecureScreenWindow().contains(win)) {
                            WindowState.this.mService.mHwWMSEx.removeSecureScreenWindow(win);
                        }
                        DisplayContent dc = WindowState.this.getDisplayContent();
                        if (win.mAppToken != null && win.mAppToken.findMainWindow() == win) {
                            WindowState.this.mService.mTaskSnapshotController.onAppDied(win.mAppToken);
                        }
                        WindowState.this.isBinderDiedCalling = true;
                        win.removeIfPossible(WindowState.this.shouldKeepVisibleDeadAppWindow());
                        if (win.mAttrs.type == 2034) {
                            TaskStack stack = dc.getSplitScreenPrimaryStackIgnoringVisibility();
                            if (stack != null) {
                                stack.resetDockedStackToMiddle();
                            }
                            resetSplitScreenResizing = true;
                        }
                    } else if (WindowState.this.mHasSurface) {
                        Slog.e(WindowState.TAG, "!!! LEAK !!! Window removed but surface still valid.");
                        WindowState.this.removeIfPossible();
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                if (resetSplitScreenResizing) {
                    WindowState.this.mService.mActivityManager.setSplitScreenResizing(false);
                }
            } catch (RemoteException e) {
                throw e.rethrowAsRuntimeException();
            } catch (IllegalArgumentException e2) {
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
    }

    private final class MoveAnimationSpec implements LocalAnimationAdapter.AnimationSpec {
        private final long mDuration;
        private Point mFrom;
        private Interpolator mInterpolator;
        private Point mTo;

        private MoveAnimationSpec(int fromX, int fromY, int toX, int toY) {
            this.mFrom = new Point();
            this.mTo = new Point();
            Animation anim = AnimationUtils.loadAnimation(WindowState.this.mContext, 17432765);
            this.mDuration = (long) (((float) anim.computeDurationHint()) * WindowState.this.mService.getWindowAnimationScaleLocked());
            this.mInterpolator = anim.getInterpolator();
            this.mFrom.set(fromX, fromY);
            this.mTo.set(toX, toY);
        }

        public long getDuration() {
            return this.mDuration;
        }

        public void apply(SurfaceControl.Transaction t, SurfaceControl leash, long currentPlayTime) {
            float v = this.mInterpolator.getInterpolation(((float) currentPlayTime) / ((float) getDuration()));
            t.setPosition(leash, ((float) this.mFrom.x) + (((float) (this.mTo.x - this.mFrom.x)) * v), ((float) this.mFrom.y) + (((float) (this.mTo.y - this.mFrom.y)) * v));
        }

        public void dump(PrintWriter pw, String prefix) {
            pw.print(prefix);
            pw.print("from=");
            pw.print(this.mFrom);
            pw.print(" to=");
            pw.print(this.mTo);
            pw.print(" duration=");
            pw.println(this.mDuration);
        }

        public void writeToProtoInner(ProtoOutputStream proto) {
            long token = proto.start(1146756268034L);
            this.mFrom.writeToProto(proto, 1146756268033L);
            this.mTo.writeToProto(proto, 1146756268034L);
            proto.write(1112396529667L, this.mDuration);
            proto.end(token);
        }
    }

    interface PowerManagerWrapper {
        boolean isInteractive();

        void wakeUp(long j, String str);
    }

    static final class UpdateReportedVisibilityResults {
        boolean nowGone = true;
        int numDrawn;
        int numInteresting;
        int numVisible;

        UpdateReportedVisibilityResults() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.numInteresting = 0;
            this.numVisible = 0;
            this.numDrawn = 0;
            this.nowGone = true;
        }
    }

    private static final class WindowId extends IWindowId.Stub {
        private final WeakReference<WindowState> mOuter;

        private WindowId(WindowState outer) {
            this.mOuter = new WeakReference<>(outer);
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
            return outer != null && outer.isFocused();
        }
    }

    public /* bridge */ /* synthetic */ void commitPendingTransaction() {
        super.commitPendingTransaction();
    }

    public /* bridge */ /* synthetic */ int compareTo(WindowContainer windowContainer) {
        return super.compareTo(windowContainer);
    }

    public /* bridge */ /* synthetic */ SurfaceControl getAnimationLeashParent() {
        return super.getAnimationLeashParent();
    }

    public /* bridge */ /* synthetic */ SurfaceControl getParentSurfaceControl() {
        return super.getParentSurfaceControl();
    }

    public /* bridge */ /* synthetic */ SurfaceControl.Transaction getPendingTransaction() {
        return super.getPendingTransaction();
    }

    public /* bridge */ /* synthetic */ SurfaceControl getSurfaceControl() {
        return super.getSurfaceControl();
    }

    public /* bridge */ /* synthetic */ int getSurfaceHeight() {
        return super.getSurfaceHeight();
    }

    public /* bridge */ /* synthetic */ int getSurfaceWidth() {
        return super.getSurfaceWidth();
    }

    public /* bridge */ /* synthetic */ SurfaceControl.Builder makeAnimationLeash() {
        return super.makeAnimationLeash();
    }

    public /* bridge */ /* synthetic */ void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public /* bridge */ /* synthetic */ void onOverrideConfigurationChanged(Configuration configuration) {
        super.onOverrideConfigurationChanged(configuration);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    WindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow, int forceCompatFlag) {
        this(r14, s, c, token, parentWindow, appOp, seq, a, viewVisibility, ownerId, ownerCanAddInternalSystemWindow, new PowerManagerWrapper() {
            public void wakeUp(long time, String reason) {
                WindowManagerService.this.mPowerManager.wakeUp(time, reason);
            }

            public boolean isInteractive() {
                return WindowManagerService.this.mPowerManager.isInteractive();
            }
        }, forceCompatFlag);
        final WindowManagerService windowManagerService = service;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    WindowState(WindowManagerService service, Session s, IWindow c, WindowToken token, WindowState parentWindow, int appOp, int seq, WindowManager.LayoutParams a, int viewVisibility, int ownerId, boolean ownerCanAddInternalSystemWindow, PowerManagerWrapper powerManagerWrapper, int forceCompatFlag) {
        super(service);
        InputApplicationHandle inputApplicationHandle;
        IWindow iWindow = c;
        WindowToken windowToken = token;
        WindowState windowState = parentWindow;
        WindowManager.LayoutParams layoutParams = a;
        int i = forceCompatFlag;
        this.mAttrs = new WindowManager.LayoutParams();
        this.mPolicyVisibility = true;
        this.mPolicyVisibilityAfterAnim = true;
        this.mAppOpVisibility = true;
        this.mHidden = true;
        this.mDragResizingChangeReported = true;
        this.mLayoutSeq = -1;
        this.mLastReportedConfiguration = new MergedConfiguration();
        this.mVisibleInsets = new Rect();
        this.mLastVisibleInsets = new Rect();
        this.mContentInsets = new Rect();
        this.mLastContentInsets = new Rect();
        this.mLastRelayoutContentInsets = new Rect();
        this.mOverscanInsets = new Rect();
        this.mLastOverscanInsets = new Rect();
        this.mStableInsets = new Rect();
        this.mLastStableInsets = new Rect();
        this.mOutsets = new Rect();
        this.mLastOutsets = new Rect();
        this.mOutsetsChanged = false;
        this.mDisplayCutout = WmDisplayCutout.NO_CUTOUT;
        this.mLastDisplayCutout = WmDisplayCutout.NO_CUTOUT;
        this.mGivenContentInsets = new Rect();
        this.mGivenVisibleInsets = new Rect();
        this.mGivenTouchableRegion = new Region();
        this.mTouchableInsets = 0;
        this.mGlobalScale = 1.0f;
        this.mInvGlobalScale = 1.0f;
        this.mHScale = 1.0f;
        this.mVScale = 1.0f;
        this.mLastHScale = 1.0f;
        this.mLastVScale = 1.0f;
        this.mTmpMatrix = new Matrix();
        this.mFrame = new Rect();
        this.mLastFrame = new Rect();
        this.mFrameSizeChanged = false;
        this.mCompatFrame = new Rect();
        this.mContainingFrame = new Rect();
        this.mParentFrame = new Rect();
        this.mDisplayFrame = new Rect();
        this.mOverscanFrame = new Rect();
        this.mStableFrame = new Rect();
        this.mDecorFrame = new Rect();
        this.mContentFrame = new Rect();
        this.mVisibleFrame = new Rect();
        this.mOutsetFrame = new Rect();
        this.mInsetFrame = new Rect();
        this.mWallpaperX = -1.0f;
        this.mWallpaperY = -1.0f;
        this.mWallpaperXStep = -1.0f;
        this.mWallpaperYStep = -1.0f;
        this.mWallpaperDisplayOffsetX = Integer.MIN_VALUE;
        this.mWallpaperDisplayOffsetY = Integer.MIN_VALUE;
        this.mLastVisibleLayoutRotation = -1;
        this.mHasSurface = false;
        this.mWillReplaceWindow = false;
        this.mReplacingRemoveRequested = false;
        this.mAnimateReplacingWindow = false;
        this.mReplacementWindow = null;
        this.mSkipEnterAnimationForSeamlessReplacement = false;
        this.mForceCompatMode = 0;
        this.mTmpRect = new Rect();
        this.mResizedWhileGone = false;
        this.mSeamlesslyRotated = false;
        this.mLastSurfaceInsets = new Rect();
        this.mSurfacePosition = new Point();
        this.mFrameNumber = -1;
        this.mOverscanPosition = new Point();
        this.mIsDimming = false;
        this.isBinderDiedCalling = false;
        this.mDecorTopCompensation = 0;
        this.mLazyModeSurfacePosition = new Point();
        this.mHwWSEx = null;
        this.mCurrentFoldDisplayMode = 0;
        this.mSubFoldModeSurfacePosition = new Point();
        this.mSession = s;
        this.mClient = iWindow;
        this.mAppOp = appOp;
        this.mToken = windowToken;
        this.mAppToken = this.mToken.asAppWindowToken();
        this.mOwnerUid = ownerId;
        this.mOwnerCanAddInternalSystemWindow = ownerCanAddInternalSystemWindow;
        this.mWindowId = new WindowId();
        this.mAttrs.copyFrom(layoutParams);
        this.mLastSurfaceInsets.set(this.mAttrs.surfaceInsets);
        this.mViewVisibility = viewVisibility;
        this.mPolicy = this.mService.mPolicy;
        this.mContext = this.mService.mContext;
        DeathRecipient deathRecipient = new DeathRecipient();
        this.mSeq = seq;
        this.mHwWSEx = HwServiceExFactory.getHwWindowStateEx(this.mService, this);
        this.mEnforceSizeCompat = (this.mAttrs.privateFlags & 128) != 0 && !isInHwFreeFormWorkspace();
        this.mPowerManagerWrapper = powerManagerWrapper;
        try {
            c.asBinder().linkToDeath(deathRecipient, 0);
            this.mDeathRecipient = deathRecipient;
            if (this.mAttrs.type < 1000 || this.mAttrs.type > 1999) {
                this.mBaseLayer = (this.mPolicy.getWindowLayerLw(this) * 10000) + 1000;
                this.mSubLayer = 0;
                this.mIsChildWindow = false;
                this.mLayoutAttached = false;
                this.mIsImWindow = this.mAttrs.type == 2011 || this.mAttrs.type == 2012;
                this.mIsWallpaper = this.mAttrs.type == 2013;
            } else {
                this.mBaseLayer = (this.mPolicy.getWindowLayerLw(windowState) * 10000) + 1000;
                this.mSubLayer = this.mPolicy.getSubWindowLayerFromTypeLw(layoutParams.type);
                this.mIsChildWindow = true;
                Slog.v(TAG, "Adding " + this + " to " + windowState);
                windowState.addChild(this, sWindowSubLayerComparator);
                this.mLayoutAttached = this.mAttrs.type != 1003;
                this.mIsImWindow = windowState.mAttrs.type == 2011 || windowState.mAttrs.type == 2012;
                this.mIsWallpaper = windowState.mAttrs.type == 2013;
            }
            this.mIsFloatingLayer = this.mIsImWindow || this.mIsWallpaper;
            if (i == -3) {
                this.mForceCompatMode = getTopParentWindow().mForceCompatMode;
            } else {
                this.mForceCompatMode = i;
            }
            if (this.mAppToken != null && this.mAppToken.mShowForAllUsers) {
                this.mAttrs.flags |= DumpState.DUMP_FROZEN;
            }
            HwServiceFactory.IHwWindowStateAnimator iwsa = HwServiceFactory.getHuaweiWindowStateAnimator();
            if (iwsa != null) {
                this.mWinAnimator = iwsa.getInstance(this);
            } else {
                this.mWinAnimator = new WindowStateAnimator(this);
            }
            this.mWinAnimator.mAlpha = layoutParams.alpha;
            this.mRequestedWidth = 0;
            this.mRequestedHeight = 0;
            this.mLastRequestedWidth = 0;
            this.mLastRequestedHeight = 0;
            this.mLayer = 0;
            if (this.mAppToken != null) {
                inputApplicationHandle = this.mAppToken.mInputApplicationHandle;
            } else {
                inputApplicationHandle = null;
            }
            HwServiceFactory.IHwWindowStateAnimator iHwWindowStateAnimator = iwsa;
            this.mInputWindowHandle = new InputWindowHandle(inputApplicationHandle, this, iWindow, getDisplayId());
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
            StringBuilder sb = new StringBuilder();
            RemoteException remoteException = e;
            sb.append("Window = ");
            sb.append(this);
            sb.append(" token = ");
            sb.append(windowToken);
            Slog.v(TAG, sb.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public void attach() {
        this.mSession.windowAddedLocked(this.mAttrs.packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean getDrawnStateEvaluated() {
        return this.mDrawnStateEvaluated;
    }

    /* access modifiers changed from: package-private */
    public void setDrawnStateEvaluated(boolean evaluated) {
        this.mDrawnStateEvaluated = evaluated;
    }

    /* access modifiers changed from: package-private */
    public void onParentSet() {
        super.onParentSet();
        setDrawnStateEvaluated(false);
        getDisplayContent().reapplyMagnificationSpec();
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

    public boolean canAcquireSleepToken() {
        return this.mSession.mCanAcquireSleepToken;
    }

    private void subtractInsets(Rect frame, Rect layoutFrame, Rect insetFrame, Rect displayFrame) {
        frame.inset(Math.max(0, insetFrame.left - Math.max(layoutFrame.left, displayFrame.left)), Math.max(0, insetFrame.top - Math.max(layoutFrame.top, displayFrame.top)), Math.max(0, Math.min(layoutFrame.right, displayFrame.right) - insetFrame.right), Math.max(0, Math.min(layoutFrame.bottom, displayFrame.bottom) - insetFrame.bottom));
    }

    private void correctionCutoutRegion(DisplayCutout displayCutout, Rect rect) {
        int insertTop = displayCutout.getSafeInsetTop();
        int insetBottom = displayCutout.getSafeInsetBottom();
        int insetLeft = displayCutout.getSafeInsetLeft();
        int insetRight = displayCutout.getSafeInsetRight();
        if (insertTop != 0 || insetBottom != 0) {
            rect.set(rect.left + 1, rect.top, rect.right - 1, rect.bottom);
        } else if (insetLeft != 0 || insetRight != 0) {
            rect.set(rect.left, rect.top + 1, rect.right, rect.bottom - 1);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0681, code lost:
        if (r27 != r6.mFrame.height()) goto L_0x0686;
     */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0306  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0328  */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0430  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x04a0  */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x05e8  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x0671  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x069f  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x06a5  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x06ab  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x01ba  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01fc  */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x01fe  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x0201  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0217  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x025d  */
    public void computeFrameLw(Rect parentFrame, Rect displayFrame, Rect overscanFrame, Rect contentFrame, Rect visibleFrame, Rect decorFrame, Rect stableFrame, Rect outsetFrame, WmDisplayCutout displayCutout, boolean parentFrameWasClippedByDisplayCutout) {
        DisplayContent dc;
        Rect layoutContainingFrame;
        int layoutYDiff;
        int layoutXDiff;
        Rect layoutDisplayFrame;
        boolean hasOutsets;
        int fh;
        int pw;
        int ph;
        int fh2;
        boolean overrideBottomInset;
        WmDisplayCutout displayCutout2;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        Rect rect = parentFrame;
        Rect rect2 = displayFrame;
        Rect rect3 = contentFrame;
        Rect rect4 = outsetFrame;
        if (!this.mWillReplaceWindow || (!this.mAnimatingExit && this.mReplacingRemoveRequested)) {
            this.mHaveFrame = true;
            this.mParentFrameWasClippedByDisplayCutout = parentFrameWasClippedByDisplayCutout;
            Task task = getTask();
            boolean inFullscreenContainer = inFullscreenContainer();
            boolean windowsAreFloating = task != null && task.isFloating();
            DisplayContent dc2 = getDisplayContent();
            if (task == null || !isInMultiWindowMode()) {
                this.mInsetFrame.setEmpty();
            } else {
                task.getTempInsetBounds(this.mInsetFrame);
            }
            if (inFullscreenContainer) {
                dc = dc2;
            } else if (layoutInParentFrame()) {
                dc = dc2;
            } else {
                getBounds(this.mContainingFrame);
                if (this.mAppToken != null && !this.mAppToken.mFrozenBounds.isEmpty()) {
                    Rect frozen = this.mAppToken.mFrozenBounds.peek();
                    this.mContainingFrame.right = this.mContainingFrame.left + frozen.width();
                    this.mContainingFrame.bottom = this.mContainingFrame.top + frozen.height();
                }
                WindowState imeWin = this.mService.mInputMethodWindow;
                WindowState imeWin2 = imeWin;
                dc = dc2;
                this.mService.mHwWMSEx.adjustWindowPosForPadPC(this.mContainingFrame, rect3, imeWin, this.mService.mInputMethodTarget, this);
                if (imeWin2 != null && imeWin2.isVisibleNow() && isInputMethodTarget()) {
                    if (inFreeformWindowingMode() && this.mContainingFrame.bottom > rect3.bottom) {
                        this.mContainingFrame.set(adjustImePosForFreeform(rect3, this.mContainingFrame));
                        this.mContainingFrame.top -= this.mContainingFrame.bottom - rect3.bottom;
                    } else if (!inPinnedWindowingMode() && this.mContainingFrame.bottom > rect.bottom) {
                        this.mContainingFrame.bottom = rect.bottom;
                    }
                }
                if (windowsAreFloating && this.mContainingFrame.isEmpty()) {
                    this.mContainingFrame.set(rect3);
                }
                TaskStack stack = getStack();
                if (inPinnedWindowingMode() && stack != null && stack.lastAnimatingBoundsWasToFullscreen()) {
                    this.mInsetFrame.intersectUnchecked(rect);
                    this.mContainingFrame.intersectUnchecked(rect);
                }
                this.mDisplayFrame.set(this.mContainingFrame);
                int layoutXDiff2 = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.left - this.mContainingFrame.left : 0;
                layoutYDiff = !this.mInsetFrame.isEmpty() ? this.mInsetFrame.top - this.mContainingFrame.top : 0;
                layoutContainingFrame = !this.mInsetFrame.isEmpty() ? this.mInsetFrame : this.mContainingFrame;
                TaskStack taskStack = stack;
                int layoutXDiff3 = layoutXDiff2;
                this.mTmpRect.set(0, 0, dc.getDisplayInfo().logicalWidth, dc.getDisplayInfo().logicalHeight);
                subtractInsets(this.mDisplayFrame, layoutContainingFrame, rect2, this.mTmpRect);
                if (!layoutInParentFrame() && (!IS_NOTCH_PROP || !isInHwFreeFormWorkspace())) {
                    subtractInsets(this.mContainingFrame, layoutContainingFrame, rect, this.mTmpRect);
                    subtractInsets(this.mInsetFrame, layoutContainingFrame, rect, this.mTmpRect);
                }
                layoutDisplayFrame = rect2;
                layoutDisplayFrame.intersect(layoutContainingFrame);
                layoutXDiff = layoutXDiff3;
                int pw2 = this.mContainingFrame.width();
                int ph2 = this.mContainingFrame.height();
                if (!this.mParentFrame.equals(rect)) {
                    this.mParentFrame.set(rect);
                    this.mContentChanged = true;
                }
                if (!(this.mRequestedWidth == this.mLastRequestedWidth && this.mRequestedHeight == this.mLastRequestedHeight)) {
                    this.mLastRequestedWidth = this.mRequestedWidth;
                    this.mLastRequestedHeight = this.mRequestedHeight;
                    this.mContentChanged = true;
                }
                this.mOverscanFrame.set(overscanFrame);
                this.mContentFrame.set(rect3);
                this.mVisibleFrame.set(visibleFrame);
                this.mDecorFrame.set(decorFrame);
                this.mStableFrame.set(stableFrame);
                hasOutsets = rect4 == null;
                if (hasOutsets) {
                    this.mOutsetFrame.set(rect4);
                }
                int fw = this.mFrame.width();
                fh = this.mFrame.height();
                applyGravityAndUpdateFrame(layoutContainingFrame, layoutDisplayFrame);
                if (!hasOutsets) {
                    Rect rect5 = layoutDisplayFrame;
                    boolean z = hasOutsets;
                    ph = ph2;
                    pw = pw2;
                    this.mOutsets.set(Math.max(this.mContentFrame.left - this.mOutsetFrame.left, 0), Math.max(this.mContentFrame.top - this.mOutsetFrame.top, 0), Math.max(this.mOutsetFrame.right - this.mContentFrame.right, 0), Math.max(this.mOutsetFrame.bottom - this.mContentFrame.bottom, 0));
                } else {
                    pw = pw2;
                    ph = ph2;
                    boolean z2 = hasOutsets;
                    this.mOutsets.set(0, 0, 0, 0);
                }
                if (windowsAreFloating || this.mFrame.isEmpty()) {
                    fh2 = fh;
                    Task task2 = task;
                    if (this.mAttrs.type != 2034) {
                        dc.getDockedDividerController().positionDockedStackedDivider(this.mFrame);
                        this.mContentFrame.set(this.mFrame);
                        if (!this.mFrame.equals(this.mLastFrame)) {
                            overrideBottomInset = true;
                            this.mMovedByResize = true;
                            if (inFullscreenContainer && !windowsAreFloating) {
                                this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, 0), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, 0), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, 0), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, 0));
                            }
                            if (this.mAttrs.type == 2034) {
                                WmDisplayCutout c = displayCutout.calculateRelativeTo(this.mDisplayFrame);
                                this.mTmpRect.set(this.mDisplayFrame);
                                this.mTmpRect.inset(c.getDisplayCutout().getSafeInsets());
                                this.mTmpRect.intersectUnchecked(this.mStableFrame);
                                this.mStableInsets.set(Math.max(this.mTmpRect.left - this.mDisplayFrame.left, 0), Math.max(this.mTmpRect.top - this.mDisplayFrame.top, 0), Math.max(this.mDisplayFrame.right - this.mTmpRect.right, 0), Math.max(this.mDisplayFrame.bottom - this.mTmpRect.bottom, 0));
                                this.mContentInsets.setEmpty();
                                this.mVisibleInsets.setEmpty();
                                displayCutout2 = WmDisplayCutout.NO_CUTOUT;
                                Rect rect6 = layoutContainingFrame;
                            } else {
                                displayCutout2 = displayCutout;
                                getDisplayContent().getBounds(this.mTmpRect);
                                boolean overrideRightInset = (windowsAreFloating || inFullscreenContainer || this.mFrame.right <= this.mTmpRect.right) ? false : overrideBottomInset;
                                if (windowsAreFloating || inFullscreenContainer || this.mFrame.bottom <= this.mTmpRect.bottom) {
                                    overrideBottomInset = false;
                                }
                                Rect rect7 = this.mContentInsets;
                                int i7 = this.mContentFrame.left - this.mFrame.left;
                                int i8 = this.mContentFrame.top - this.mFrame.top;
                                if (overrideRightInset) {
                                    i = this.mTmpRect.right - this.mContentFrame.right;
                                } else {
                                    i = this.mFrame.right - this.mContentFrame.right;
                                }
                                if (overrideBottomInset) {
                                    Rect rect8 = layoutContainingFrame;
                                    i2 = this.mTmpRect.bottom - this.mContentFrame.bottom;
                                } else {
                                    i2 = this.mFrame.bottom - this.mContentFrame.bottom;
                                }
                                rect7.set(i7, i8, i, i2);
                                Rect rect9 = this.mVisibleInsets;
                                int i9 = this.mVisibleFrame.left - this.mFrame.left;
                                int i10 = this.mVisibleFrame.top - this.mFrame.top;
                                if (overrideRightInset) {
                                    i3 = this.mTmpRect.right - this.mVisibleFrame.right;
                                } else {
                                    i3 = this.mFrame.right - this.mVisibleFrame.right;
                                }
                                if (overrideBottomInset) {
                                    i4 = this.mTmpRect.bottom - this.mVisibleFrame.bottom;
                                } else {
                                    i4 = this.mFrame.bottom - this.mVisibleFrame.bottom;
                                }
                                rect9.set(i9, i10, i3, i4);
                                Rect rect10 = this.mStableInsets;
                                int max = Math.max(this.mStableFrame.left - this.mFrame.left, 0);
                                int max2 = Math.max(this.mStableFrame.top - this.mFrame.top, 0);
                                if (overrideRightInset) {
                                    i5 = Math.max(this.mTmpRect.right - this.mStableFrame.right, 0);
                                } else {
                                    i5 = Math.max(this.mFrame.right - this.mStableFrame.right, 0);
                                }
                                if (overrideBottomInset) {
                                    i6 = Math.max(this.mTmpRect.bottom - this.mStableFrame.bottom, 0);
                                } else {
                                    i6 = Math.max(this.mFrame.bottom - this.mStableFrame.bottom, 0);
                                }
                                rect10.set(max, max2, i5, i6);
                                adjustVisibleInsetsInSplitMode();
                            }
                            this.mDisplayCutout = displayCutout2.calculateRelativeTo(this.mFrame);
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
                                if (IS_NOTCH_PROP && !this.mPolicy.getLayoutBeyondDisplayCutout() && this.mCompatFrame.width() != this.mCompatFrame.height()) {
                                    Rect cutoutRect = this.mDisplayCutout.getDisplayCutout().getBounds().getBounds();
                                    cutoutRect.scale(this.mInvGlobalScale);
                                    correctionCutoutRegion(this.mDisplayCutout.getDisplayCutout(), cutoutRect);
                                    Region tmpRegion = new Region();
                                    tmpRegion.set(cutoutRect);
                                    this.mDisplayCutout = WmDisplayCutout.computeSafeInsets(DisplayCutout.fromBounds(tmpRegion.getBoundaryPath()), this.mCompatFrame.width(), this.mCompatFrame.height());
                                }
                                adjustContentsInsetsInCompatMode();
                            }
                            if (this.mIsWallpaper) {
                                if (fw != this.mFrame.width()) {
                                }
                                DisplayContent displayContent = getDisplayContent();
                                if (displayContent != null) {
                                    DisplayInfo displayInfo = displayContent.getDisplayInfo();
                                    getDisplayContent().mWallpaperController.updateWallpaperOffset(this, displayInfo.logicalWidth, displayInfo.logicalHeight, false);
                                }
                            }
                            if (!WindowManagerDebugConfig.DEBUG_FOCUS) {
                                int i11 = ph;
                                int i12 = pw;
                            } else {
                                Slog.v(TAG, "Resolving window" + this + "(mRequestedWidth=" + this.mRequestedWidth + ", mRequestedheight=" + this.mRequestedHeight + ") to (pw=" + pw + ", ph=" + ph + "): frame=" + this.mFrame.toShortString() + " ci=" + this.mContentInsets.toShortString() + " vi=" + this.mVisibleInsets.toShortString() + " si=" + this.mStableInsets.toShortString() + " of=" + this.mOutsets.toShortString());
                            }
                            return;
                        }
                    } else {
                        if (this.mAttrs.type == 2039) {
                            dc.getDockedDividerController().positionCoordinationStackDivider(this.mFrame);
                            this.mContentFrame.set(this.mFrame);
                            if (!this.mFrame.equals(this.mLastFrame)) {
                                overrideBottomInset = true;
                                this.mMovedByResize = true;
                            }
                        } else {
                            overrideBottomInset = true;
                            offsetSystemDialog(rect3);
                            this.mContentFrame.set(Math.max(this.mContentFrame.left, this.mFrame.left), Math.max(this.mContentFrame.top, this.mFrame.top), Math.min(this.mContentFrame.right, this.mFrame.right), Math.min(this.mContentFrame.bottom, this.mFrame.bottom));
                            this.mVisibleFrame.set(Math.max(this.mVisibleFrame.left, this.mFrame.left), Math.max(this.mVisibleFrame.top, this.mFrame.top), Math.min(this.mVisibleFrame.right, this.mFrame.right), Math.min(this.mVisibleFrame.bottom, this.mFrame.bottom));
                            this.mStableFrame.set(Math.max(this.mStableFrame.left, this.mFrame.left), Math.max(this.mStableFrame.top, this.mFrame.top), Math.min(this.mStableFrame.right, this.mFrame.right), Math.min(this.mStableFrame.bottom, this.mFrame.bottom));
                        }
                        this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, 0), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, 0), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, 0), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, 0));
                        if (this.mAttrs.type == 2034) {
                        }
                        this.mDisplayCutout = displayCutout2.calculateRelativeTo(this.mFrame);
                        this.mFrame.offset(-layoutXDiff, -layoutYDiff);
                        this.mCompatFrame.offset(-layoutXDiff, -layoutYDiff);
                        this.mContentFrame.offset(-layoutXDiff, -layoutYDiff);
                        this.mVisibleFrame.offset(-layoutXDiff, -layoutYDiff);
                        this.mStableFrame.offset(-layoutXDiff, -layoutYDiff);
                        this.mCompatFrame.set(this.mFrame);
                        if (this.mEnforceSizeCompat) {
                        }
                        if (this.mIsWallpaper) {
                        }
                        if (!WindowManagerDebugConfig.DEBUG_FOCUS) {
                        }
                        return;
                    }
                } else {
                    Rect limitFrame = task.inPinnedWindowingMode() ? this.mFrame : this.mContentFrame;
                    int height = Math.min(this.mFrame.height(), limitFrame.height());
                    int width = Math.min(limitFrame.width(), this.mFrame.width());
                    DisplayMetrics displayMetrics = getDisplayContent().getDisplayMetrics();
                    int minVisibleHeight = Math.min(height, WindowManagerService.dipToPixel(36, displayMetrics));
                    int minVisibleWidth = Math.min(width, WindowManagerService.dipToPixel(42, displayMetrics));
                    DisplayMetrics displayMetrics2 = displayMetrics;
                    int top = adjustTopForFreeform(this.mFrame, limitFrame, minVisibleHeight);
                    int i13 = minVisibleHeight;
                    Task task3 = task;
                    fh2 = fh;
                    int left = Math.max((limitFrame.left + minVisibleWidth) - width, Math.min(this.mFrame.left, limitFrame.right - minVisibleWidth));
                    int i14 = minVisibleWidth;
                    this.mFrame.set(left, top, left + width, top + height);
                    this.mContentFrame.set(this.mFrame);
                    this.mVisibleFrame.set(this.mContentFrame);
                    this.mStableFrame.set(this.mContentFrame);
                }
                overrideBottomInset = true;
                this.mOverscanInsets.set(Math.max(this.mOverscanFrame.left - layoutContainingFrame.left, 0), Math.max(this.mOverscanFrame.top - layoutContainingFrame.top, 0), Math.max(layoutContainingFrame.right - this.mOverscanFrame.right, 0), Math.max(layoutContainingFrame.bottom - this.mOverscanFrame.bottom, 0));
                if (this.mAttrs.type == 2034) {
                }
                this.mDisplayCutout = displayCutout2.calculateRelativeTo(this.mFrame);
                this.mFrame.offset(-layoutXDiff, -layoutYDiff);
                this.mCompatFrame.offset(-layoutXDiff, -layoutYDiff);
                this.mContentFrame.offset(-layoutXDiff, -layoutYDiff);
                this.mVisibleFrame.offset(-layoutXDiff, -layoutYDiff);
                this.mStableFrame.offset(-layoutXDiff, -layoutYDiff);
                this.mCompatFrame.set(this.mFrame);
                if (this.mEnforceSizeCompat) {
                }
                if (this.mIsWallpaper) {
                }
                if (!WindowManagerDebugConfig.DEBUG_FOCUS) {
                }
                return;
            }
            this.mContainingFrame.set(rect);
            this.mDisplayFrame.set(rect2);
            layoutDisplayFrame = rect2;
            layoutContainingFrame = rect;
            layoutXDiff = 0;
            layoutYDiff = 0;
            int pw22 = this.mContainingFrame.width();
            int ph22 = this.mContainingFrame.height();
            if (!this.mParentFrame.equals(rect)) {
            }
            this.mLastRequestedWidth = this.mRequestedWidth;
            this.mLastRequestedHeight = this.mRequestedHeight;
            this.mContentChanged = true;
            this.mOverscanFrame.set(overscanFrame);
            this.mContentFrame.set(rect3);
            this.mVisibleFrame.set(visibleFrame);
            this.mDecorFrame.set(decorFrame);
            this.mStableFrame.set(stableFrame);
            if (rect4 == null) {
            }
            if (hasOutsets) {
            }
            int fw2 = this.mFrame.width();
            fh = this.mFrame.height();
            applyGravityAndUpdateFrame(layoutContainingFrame, layoutDisplayFrame);
            if (!hasOutsets) {
            }
            if (windowsAreFloating) {
            }
            fh2 = fh;
            Task task22 = task;
            if (this.mAttrs.type != 2034) {
            }
        }
    }

    private void adjustContentsInsetsInCompatMode() {
        if (this.mDecorTopCompensation == 1 && this.mContentInsets.top > 0) {
            this.mContentInsets.top -= this.mDecorTopCompensation;
        }
        WindowState imeWin = this.mService.mInputMethodWindow;
        if (imeWin != null && imeWin.isVisibleNow() && this.mService.mInputMethodTarget == this && imeWin.isDisplayedLw() && this.mContentInsets.bottom != 0 && (this.mAttrs.softInputMode & 240) != 48 && this.mContentInsets.bottom >= this.mVisibleInsets.bottom) {
            this.mContentInsets.bottom--;
        }
    }

    private void adjustVisibleInsetsInSplitMode() {
        if (this.mService.isSplitMode() && !isInMultiWindowMode()) {
            this.mVisibleInsets.right = this.mTmpRect.right - this.mVisibleFrame.right;
        }
    }

    public Rect getBounds() {
        if (isInMultiWindowMode()) {
            return getTask().getBounds();
        }
        if (this.mAppToken != null) {
            return this.mAppToken.getBounds();
        }
        return super.getBounds();
    }

    public Rect getFrameLw() {
        return this.mFrame;
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

    /* access modifiers changed from: package-private */
    public Rect getStableFrameLw() {
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

    public WindowManager.LayoutParams getAttrs() {
        return this.mAttrs;
    }

    public boolean getNeedsMenuLw(WindowManagerPolicy.WindowState bottom) {
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
        if (this.mAppToken != null) {
            return this.mAppToken.appToken;
        }
        return null;
    }

    public boolean isVoiceInteraction() {
        return this.mAppToken != null && this.mAppToken.mVoiceInteraction;
    }

    /* access modifiers changed from: package-private */
    public boolean setReportResizeHints() {
        this.mOverscanInsetsChanged |= !this.mLastOverscanInsets.equals(this.mOverscanInsets);
        this.mContentInsetsChanged |= !this.mLastContentInsets.equals(this.mContentInsets);
        this.mVisibleInsetsChanged |= !this.mLastVisibleInsets.equals(this.mVisibleInsets);
        this.mStableInsetsChanged |= !this.mLastStableInsets.equals(this.mStableInsets);
        this.mOutsetsChanged |= !this.mLastOutsets.equals(this.mOutsets);
        this.mFrameSizeChanged |= (this.mLastFrame.width() == this.mFrame.width() && this.mLastFrame.height() == this.mFrame.height()) ? false : true;
        this.mDisplayCutoutChanged |= !this.mLastDisplayCutout.equals(this.mDisplayCutout);
        if (this.mOverscanInsetsChanged || this.mContentInsetsChanged || this.mVisibleInsetsChanged || this.mOutsetsChanged || this.mFrameSizeChanged || this.mDisplayCutoutChanged) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateResizingWindowIfNeeded() {
        WindowStateAnimator winAnimator = this.mWinAnimator;
        if (this.mHasSurface && getDisplayContent().mLayoutSeq == this.mLayoutSeq && !isGoneForLayoutLw()) {
            Task task = getTask();
            if (task == null || !task.mStack.isAnimatingBounds()) {
                setReportResizeHints();
                boolean configChanged = isConfigChanged();
                if (WindowManagerDebugConfig.DEBUG_CONFIGURATION && configChanged) {
                    Slog.v(TAG, "Win " + this + " config changed: " + getConfiguration());
                }
                boolean dragResizingChanged = isDragResizeChanged() && !isDragResizingChangeReported();
                this.mLastFrame.set(this.mFrame);
                if (this.mContentInsetsChanged || this.mVisibleInsetsChanged || this.mStableInsetsChanged || winAnimator.mSurfaceResized || this.mOutsetsChanged || this.mFrameSizeChanged || this.mDisplayCutoutChanged || configChanged || dragResizingChanged || this.mReportOrientationChanged) {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v(TAG, "Resize reasons for w=" + this + ":  contentInsetsChanged=" + this.mContentInsetsChanged + " " + this.mContentInsets.toShortString() + " visibleInsetsChanged=" + this.mVisibleInsetsChanged + " " + this.mVisibleInsets.toShortString() + " stableInsetsChanged=" + this.mStableInsetsChanged + " " + this.mStableInsets.toShortString() + " outsetsChanged=" + this.mOutsetsChanged + " " + this.mOutsets.toShortString() + " surfaceResized=" + winAnimator.mSurfaceResized + " configChanged=" + configChanged + " dragResizingChanged=" + dragResizingChanged + " reportOrientationChanged=" + this.mReportOrientationChanged + " displayCutoutChanged=" + this.mDisplayCutoutChanged);
                    }
                    if (this.mAppToken == null || !this.mAppDied) {
                        updateLastInsetValues();
                        this.mService.makeWindowFreezingScreenIfNeededLocked(this);
                        if (getOrientationChanging() || dragResizingChanged) {
                            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v(TAG, "Orientation or resize start waiting for draw, mDrawState=DRAW_PENDING in " + this + ", surfaceController " + winAnimator.mSurfaceController);
                            }
                            winAnimator.mDrawState = 1;
                            if (this.mAppToken != null) {
                                this.mAppToken.clearAllDrawn();
                            }
                        }
                        if (!this.mService.mResizingWindows.contains(this)) {
                            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                                Slog.v(TAG, "Resizing window " + this);
                            }
                            this.mService.mResizingWindows.add(this);
                        }
                    } else {
                        this.mAppToken.removeDeadWindows();
                    }
                } else if (getOrientationChanging() && isDrawnLw()) {
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v(TAG, "Orientation not waiting for draw in " + this + ", surfaceController " + winAnimator.mSurfaceController);
                    }
                    setOrientationChanging(false);
                    this.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mService.mDisplayFreezeTime);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getOrientationChanging() {
        return (this.mOrientationChanging || (isVisible() && getConfiguration().orientation != getLastReportedConfiguration().orientation)) && !this.mSeamlesslyRotated && !this.mOrientationChangeTimedOut;
    }

    /* access modifiers changed from: package-private */
    public void setOrientationChanging(boolean changing) {
        this.mOrientationChanging = changing;
        this.mOrientationChangeTimedOut = false;
    }

    /* access modifiers changed from: package-private */
    public void orientationChangeTimedOut() {
        this.mOrientationChangeTimedOut = true;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent() {
        return this.mToken.getDisplayContent();
    }

    /* access modifiers changed from: package-private */
    public void onDisplayChanged(DisplayContent dc) {
        super.onDisplayChanged(dc);
        if (dc != null) {
            this.mLayoutSeq = dc.mLayoutSeq - 1;
            this.mInputWindowHandle.displayId = dc.getDisplayId();
        }
    }

    /* access modifiers changed from: package-private */
    public DisplayInfo getDisplayInfo() {
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

    /* access modifiers changed from: package-private */
    public Task getTask() {
        if (this.mAppToken != null) {
            return this.mAppToken.getTask();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack() {
        Task task = getTask();
        if (task != null && task.mStack != null) {
            return task.mStack;
        }
        DisplayContent dc = getDisplayContent();
        return (this.mAttrs.type < 2000 || dc == null) ? null : dc.getHomeStack();
    }

    /* access modifiers changed from: package-private */
    public void getVisibleBounds(Rect bounds) {
        Task task = getTask();
        boolean intersectWithStackBounds = task != null && task.cropWindowsToStackBounds();
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
        return this.mAppToken != null && (this.mAppToken.firstWindowDrawn || this.mAppToken.startingDisplayed);
    }

    /* access modifiers changed from: package-private */
    public boolean isIdentityMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        if (dsdx < 0.99999f || dsdx > 1.00001f || dtdy < 0.99999f || dtdy > 1.00001f || dtdx < -1.0E-6f || dtdx > 1.0E-6f || dsdy < -1.0E-6f || dsdy > 1.0E-6f) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void prelayout() {
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
            if (Float.compare(0.6667f, resolutionRatio) == 0) {
                this.mGlobalScale = 1.5f;
            } else {
                this.mGlobalScale = 1.0f / this.mInvGlobalScale;
            }
        } else {
            this.mInvGlobalScale = 1.0f;
            this.mGlobalScale = 1.0f;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasContentToDisplay() {
        if (this.mAppFreezing || !isDrawnLw() || (this.mViewVisibility != 0 && (!this.mWinAnimator.isAnimationSet() || this.mService.mAppTransition.isTransitionSet()))) {
            return super.hasContentToDisplay();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisible() {
        return wouldBeVisibleIfPolicyIgnored() && this.mPolicyVisibility;
    }

    /* access modifiers changed from: package-private */
    public boolean wouldBeVisibleIfPolicyIgnored() {
        return this.mHasSurface && !isParentWindowHidden() && !this.mAnimatingExit && !this.mDestroying && (!this.mIsWallpaper || this.mWallpaperVisible);
    }

    public boolean isVisibleLw() {
        return isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean isWinVisibleLw() {
        return (this.mAppToken == null || !this.mAppToken.hiddenRequested || this.mAppToken.isSelfAnimating()) && isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean isVisibleNow() {
        return (!this.mToken.isHidden() || this.mAttrs.type == 3) && isVisible();
    }

    /* access modifiers changed from: package-private */
    public boolean isPotentialDragTarget() {
        return isVisibleNow() && !this.mRemoved && this.mInputChannel != null && this.mInputWindowHandle != null;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisibleOrAdding() {
        AppWindowToken atoken = this.mAppToken;
        return (this.mHasSurface || (!this.mRelayoutCalled && this.mViewVisibility == 0)) && this.mPolicyVisibility && !isParentWindowHidden() && (atoken == null || !atoken.hiddenRequested) && !this.mAnimatingExit && !this.mDestroying;
    }

    /* access modifiers changed from: package-private */
    public boolean isOnScreen() {
        boolean z = false;
        if (!this.mHasSurface || this.mDestroying || !this.mPolicyVisibility) {
            return false;
        }
        AppWindowToken atoken = this.mAppToken;
        if (atoken != null) {
            if ((!isParentWindowHidden() && !atoken.hiddenRequested) || this.mWinAnimator.isAnimationSet()) {
                z = true;
            }
            return z;
        }
        if (!isParentWindowHidden() || this.mWinAnimator.isAnimationSet()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean mightAffectAllDrawn() {
        boolean isAppType = this.mWinAnimator.mAttrType == 1 || this.mWinAnimator.mAttrType == 4;
        if ((isOnScreen() || isAppType) && !this.mAnimatingExit && !this.mDestroying) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isInteresting() {
        return this.mAppToken != null && !this.mAppDied && (!this.mAppToken.isFreezingScreen() || !this.mAppFreezing);
    }

    /* access modifiers changed from: package-private */
    public boolean isReadyForDisplay() {
        boolean z = false;
        if (this.mToken.waitingToShow && this.mService.mAppTransition.isTransitionSet()) {
            return false;
        }
        if (this.mHasSurface && this.mPolicyVisibility && !this.mDestroying && ((!isParentWindowHidden() && this.mViewVisibility == 0 && !this.mToken.isHidden()) || this.mWinAnimator.isAnimationSet())) {
            z = true;
        }
        return z;
    }

    public boolean canAffectSystemUiFlags() {
        boolean z = true;
        if (this.mAttrs.alpha == 0.0f) {
            return false;
        }
        if (this.mAppToken == null) {
            boolean shown = this.mWinAnimator.getShown();
            boolean exiting = this.mAnimatingExit || this.mDestroying;
            if (!shown || exiting) {
                z = false;
            }
            return z;
        }
        Task task = getTask();
        if (!(task != null && task.canAffectSystemUiFlags()) || this.mAppToken.isHidden()) {
            z = false;
        }
        return z;
    }

    public boolean isDisplayedLw() {
        AppWindowToken atoken = this.mAppToken;
        return isDrawnLw() && this.mPolicyVisibility && ((!isParentWindowHidden() && (atoken == null || !atoken.hiddenRequested)) || this.mWinAnimator.isAnimationSet());
    }

    public boolean isAnimatingLw() {
        return isAnimating();
    }

    public boolean isGoneForLayoutLw() {
        AppWindowToken atoken = this.mAppToken;
        return this.mViewVisibility == 8 || !this.mRelayoutCalled || (atoken == null && this.mToken.isHidden()) || ((atoken != null && atoken.hiddenRequested) || isParentWindowHidden() || ((this.mAnimatingExit && !isAnimatingLw()) || this.mDestroying));
    }

    public boolean isDrawFinishedLw() {
        return this.mHasSurface && !this.mDestroying && (this.mWinAnimator.mDrawState == 2 || this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4);
    }

    public boolean isDrawnLw() {
        return this.mHasSurface && !this.mDestroying && (this.mWinAnimator.mDrawState == 3 || this.mWinAnimator.mDrawState == 4);
    }

    private boolean isOpaqueDrawn() {
        return ((!this.mIsWallpaper && this.mAttrs.format == -1) || (this.mIsWallpaper && this.mWallpaperVisible)) && isDrawnLw() && !this.mWinAnimator.isAnimationSet();
    }

    /* access modifiers changed from: package-private */
    public void onMovedByResize() {
        this.mMovedByResize = true;
        super.onMovedByResize();
    }

    /* access modifiers changed from: package-private */
    public boolean onAppVisibilityChanged(boolean visible, boolean runningAppAnimation) {
        boolean changed = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            changed |= ((WindowState) this.mChildren.get(i)).onAppVisibilityChanged(visible, runningAppAnimation);
        }
        if (this.mAttrs.type == 3) {
            if (!visible && isVisibleNow() && this.mAppToken.isSelfAnimating()) {
                this.mAnimatingExit = true;
                this.mRemoveOnExit = true;
                this.mWindowRemovalAllowed = true;
            }
            return changed;
        }
        boolean isVisibleNow = isVisibleNow();
        if (visible != isVisibleNow) {
            if (!runningAppAnimation && isVisibleNow) {
                AccessibilityController accessibilityController = this.mService.mAccessibilityController;
                this.mWinAnimator.applyAnimationLocked(2, false);
                if (accessibilityController != null && getDisplayId() == 0) {
                    accessibilityController.onWindowTransitionLocked(this, 2);
                }
            }
            changed = true;
            setDisplayLayoutNeeded();
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean onSetAppExiting() {
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

    /* access modifiers changed from: package-private */
    public void onResize() {
        ArrayList<WindowState> resizingWindows = this.mService.mResizingWindows;
        if (this.mHasSurface && !isGoneForLayoutLw() && !resizingWindows.contains(this)) {
            resizingWindows.add(this);
        }
        if (isGoneForLayoutLw()) {
            this.mResizedWhileGone = true;
        }
        super.onResize();
    }

    /* access modifiers changed from: package-private */
    public void onUnfreezeBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onUnfreezeBounds();
        }
        if (this.mHasSurface != 0) {
            this.mLayoutNeeded = true;
            setDisplayLayoutNeeded();
            if (!this.mService.mResizingWindows.contains(this)) {
                this.mService.mResizingWindows.add(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleWindowMovedIfNeeded() {
        if (hasMoved()) {
            int left = this.mFrame.left;
            int top = this.mFrame.top;
            Task task = getTask();
            boolean adjustedForMinimizedDockOrIme = task != null && (task.mStack.isAdjustedForMinimizedDockedStack() || task.mStack.isAdjustedForIme());
            if (this.mToken.okToAnimate() && (this.mAttrs.privateFlags & 64) == 0 && !isDragResizing() && !adjustedForMinimizedDockOrIme && getWindowConfiguration().hasMovementAnimations() && !this.mWinAnimator.mLastHidden && ((!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(getDisplayId())) && this.mService.getLazyMode() == 0 && ((!HwFoldScreenState.isFoldScreenDevice() || !this.mService.isInSubFoldScaleMode()) && HwFreeFormUtils.isFreeFormEnable() && !inFreeformWindowingMode()))) {
                startMoveAnimation(left, top);
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
        return this.mHasSurface && (this.mContentChanged || this.mMovedByResize) && !this.mAnimatingExit && (!(this.mFrame.top == this.mLastFrame.top && this.mFrame.left == this.mLastFrame.left) && (!this.mIsChildWindow || !getParentWindow().hasMoved()));
    }

    /* access modifiers changed from: package-private */
    public boolean isObscuringDisplay() {
        Task task = getTask();
        boolean z = false;
        if (task != null && task.mStack != null && !task.mStack.fillsParent()) {
            return false;
        }
        if (isOpaqueDrawn() && fillsDisplay()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean fillsDisplay() {
        DisplayInfo displayInfo = getDisplayInfo();
        return this.mFrame.left <= 0 && this.mFrame.top <= 0 && this.mFrame.right >= displayInfo.appWidth && this.mFrame.bottom >= displayInfo.appHeight;
    }

    /* access modifiers changed from: package-private */
    public boolean isConfigChanged() {
        return !getLastReportedConfiguration().equals(getConfiguration());
    }

    /* access modifiers changed from: package-private */
    public void onWindowReplacementTimeout() {
        if (this.mWillReplaceWindow) {
            removeImmediately();
            return;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).onWindowReplacementTimeout();
        }
    }

    /* access modifiers changed from: package-private */
    public void forceWindowsScaleableInTransaction(boolean force) {
        if (this.mWinAnimator != null && this.mWinAnimator.hasSurface()) {
            this.mWinAnimator.mSurfaceController.forceScaleableInTransaction(force);
        }
        super.forceWindowsScaleableInTransaction(force);
    }

    /* access modifiers changed from: package-private */
    public void removeImmediately() {
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
        if (isInputMethodTarget()) {
            dc.computeImeTarget(true);
        }
        if (WindowManagerService.excludeWindowTypeFromTapOutTask(this.mAttrs.type) || WindowManagerService.excludeWindowsFromTapOutTask(this)) {
            dc.mTapExcludedWindows.remove(this);
        }
        if (this.mTapExcludeRegionHolder != null) {
            dc.mTapExcludeProvidingWindows.remove(this);
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

    /* access modifiers changed from: package-private */
    public void removeIfPossible() {
        super.removeIfPossible();
        removeIfPossible(false);
    }

    /* access modifiers changed from: private */
    public void removeIfPossible(boolean keepVisibleDeadWindow) {
        this.mWindowRemovalAllowed = true;
        Slog.v(TAG, "removeIfPossible: " + this);
        boolean startingWindow = this.mAttrs.type == 3;
        if (startingWindow && WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.d(TAG, "Starting window removed " + this);
        }
        int transit = 5;
        if (WindowManagerDebugConfig.DEBUG_FOCUS || (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT && this == this.mService.mCurrentFocus)) {
            Slog.v(TAG, "Remove " + this + " client=" + Integer.toHexString(System.identityHashCode(this.mClient.asBinder())) + ", surfaceController=" + this.mWinAnimator.mSurfaceController + " Callers=" + Debug.getCallers(5));
        }
        long origId = Binder.clearCallingIdentity();
        disposeInputChannel();
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            StringBuilder sb = new StringBuilder();
            sb.append("Remove ");
            sb.append(this);
            sb.append(": mSurfaceController=");
            sb.append(this.mWinAnimator.mSurfaceController);
            sb.append(" mAnimatingExit=");
            sb.append(this.mAnimatingExit);
            sb.append(" mRemoveOnExit=");
            sb.append(this.mRemoveOnExit);
            sb.append(" mHasSurface=");
            sb.append(this.mHasSurface);
            sb.append(" surfaceShowing=");
            sb.append(this.mWinAnimator.getShown());
            sb.append(" isAnimationSet=");
            sb.append(this.mWinAnimator.isAnimationSet());
            sb.append(" app-animation=");
            sb.append(this.mAppToken != null ? Boolean.valueOf(this.mAppToken.isSelfAnimating()) : "false");
            sb.append(" mWillReplaceWindow=");
            sb.append(this.mWillReplaceWindow);
            sb.append(" inPendingTransaction=");
            sb.append(this.mAppToken != null ? this.mAppToken.inPendingTransaction : false);
            sb.append(" mDisplayFrozen=");
            sb.append(this.mService.mDisplayFrozen);
            sb.append(" callers=");
            sb.append(Debug.getCallers(6));
            Slog.v(TAG, sb.toString());
        }
        boolean wasVisible = false;
        int displayId = getDisplayId();
        if (this.mHasSurface && this.mToken.okToAnimate()) {
            if (this.mWillReplaceWindow) {
                Slog.v(TAG, "Preserving " + this + " until the new one is added");
                this.mAnimatingExit = true;
                this.mReplacingRemoveRequested = true;
                Binder.restoreCallingIdentity(origId);
                return;
            }
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
                if (!startingWindow) {
                    transit = 2;
                }
                try {
                    if (this.mWinAnimator.applyAnimationLocked(transit, false)) {
                        this.mAnimatingExit = true;
                        setDisplayLayoutNeeded();
                        this.mService.requestTraversal();
                    }
                    if (this.mService.mAccessibilityController != null && displayId == 0) {
                        this.mService.mAccessibilityController.onWindowTransitionLocked(this, transit);
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            }
            if (!(this.isBinderDiedCalling == 0 || !HwPCUtils.enabled() || getDisplayId() == 0)) {
                this.isBinderDiedCalling = false;
                this.mAnimatingExit = false;
            }
            boolean isAnimating = this.mWinAnimator.isAnimationSet() && (this.mAppToken == null || !this.mAppToken.isWaitingForTransitionStart());
            boolean lastWindowIsStartingWindow = startingWindow && this.mAppToken != null && this.mAppToken.isLastWindow(this);
            if (this.mWinAnimator.getShown() && this.mAnimatingExit && (!lastWindowIsStartingWindow || isAnimating || startingWindow)) {
                Slog.v(TAG, "Not removing " + this + " due to exit animation ");
                setupWindowForRemoveOnExit();
                if (this.mAppToken != null) {
                    this.mAppToken.updateReportedVisibilityLocked();
                }
                Binder.restoreCallingIdentity(origId);
                return;
            }
        }
        removeImmediately();
        if (wasVisible && this.mService.updateOrientationFromAppTokensLocked(displayId)) {
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

    /* access modifiers changed from: package-private */
    public void setHasSurface(boolean hasSurface) {
        this.mHasSurface = hasSurface;
    }

    /* access modifiers changed from: package-private */
    public boolean canBeImeTarget() {
        if (this.mIsImWindow) {
            return false;
        }
        if (!(this.mAppToken == null || this.mAppToken.windowsAreFocusable())) {
            return false;
        }
        int fl = this.mAttrs.flags & 131080;
        int type = this.mAttrs.type;
        if (fl != 0 && fl != 131080 && type != 3) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i(TAG, "isVisibleOrAdding " + this + ": " + isVisibleOrAdding());
            if (!isVisibleOrAdding()) {
                Slog.i(TAG, "  mSurfaceController=" + this.mWinAnimator.mSurfaceController + " relayoutCalled=" + this.mRelayoutCalled + " viewVis=" + this.mViewVisibility + " policyVis=" + this.mPolicyVisibility + " policyVisAfterAnim=" + this.mPolicyVisibilityAfterAnim + " parentHidden=" + isParentWindowHidden() + " exiting=" + this.mAnimatingExit + " destroying=" + this.mDestroying);
                if (this.mAppToken != null) {
                    Slog.i(TAG, "  mAppToken.hiddenRequested=" + this.mAppToken.hiddenRequested);
                }
            }
        }
        return isVisibleOrAdding();
    }

    /* access modifiers changed from: package-private */
    public void openInputChannel(InputChannel outInputChannel) {
        if (this.mInputChannel == null) {
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
            return;
        }
        throw new IllegalStateException("Window already has an input channel.");
    }

    /* access modifiers changed from: package-private */
    public void disposeInputChannel() {
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

    /* access modifiers changed from: package-private */
    public boolean removeReplacedWindowIfNeeded(WindowState replacement) {
        if (!this.mWillReplaceWindow || this.mReplacementWindow != replacement || !replacement.hasDrawnLw()) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                if (((WindowState) this.mChildren.get(i)).removeReplacedWindowIfNeeded(replacement)) {
                    return true;
                }
            }
            return false;
        }
        replacement.mSkipEnterAnimationForSeamlessReplacement = false;
        removeReplacedWindow();
        return true;
    }

    private void removeReplacedWindow() {
        Slog.d(TAG, "Removing replaced window: " + this);
        this.mWillReplaceWindow = false;
        this.mAnimateReplacingWindow = false;
        this.mReplacingRemoveRequested = false;
        this.mReplacementWindow = null;
        if (this.mAnimatingExit || !this.mAnimateReplacingWindow) {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setReplacementWindowIfNeeded(WindowState replacementCandidate) {
        boolean replacementSet = false;
        if (this.mWillReplaceWindow && this.mReplacementWindow == null && getWindowTag().toString().equals(replacementCandidate.getWindowTag().toString())) {
            this.mReplacementWindow = replacementCandidate;
            replacementCandidate.mSkipEnterAnimationForSeamlessReplacement = !this.mAnimateReplacingWindow;
            replacementSet = true;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            replacementSet |= ((WindowState) this.mChildren.get(i)).setReplacementWindowIfNeeded(replacementCandidate);
        }
        return replacementSet;
    }

    /* access modifiers changed from: package-private */
    public void setDisplayLayoutNeeded() {
        DisplayContent dc = getDisplayContent();
        if (dc != null) {
            dc.setLayoutNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    public void applyAdjustForImeIfNeeded() {
        Task task = getTask();
        if (task != null && task.mStack != null && task.mStack.isAdjustedForIme()) {
            task.mStack.applyAdjustForImeIfNeeded(task);
        }
    }

    /* access modifiers changed from: package-private */
    public void switchUser() {
        super.switchUser();
        if (isHiddenFromUserLocked()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.w(TAG, "user changing, hiding " + this + ", attrs=" + this.mAttrs.type + ", belonging to " + this.mOwnerUid);
            }
            hideLw(false);
        }
    }

    /* access modifiers changed from: package-private */
    public int getTouchableRegion(Region region, int flags) {
        int i;
        if (!((flags & 40) == 0) || this.mAppToken == null) {
            getTouchableRegion(region);
        } else {
            flags |= 32;
            Task task = getTask();
            if (task != null) {
                task.getDimBounds(this.mTmpRect);
            } else {
                getStack().getDimBounds(this.mTmpRect);
            }
            if (inFreeformWindowingMode() || inHwPCFreeformWindowingMode()) {
                DisplayMetrics displayMetrics = getDisplayContent().getDisplayMetrics();
                if (HwFreeFormUtils.isFreeFormEnable()) {
                    i = 20;
                } else {
                    i = 10;
                }
                int delta = WindowManagerService.dipToPixel(i, displayMetrics);
                this.mTmpRect.inset(-delta, -delta);
            }
            region.set(this.mTmpRect);
            cropRegionToStackBoundsIfNeeded(region);
        }
        return flags;
    }

    /* access modifiers changed from: package-private */
    public void checkPolicyVisibilityChange() {
        if (this.mPolicyVisibility != this.mPolicyVisibilityAfterAnim) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Policy visibility changing after anim in " + this.mWinAnimator + ": " + this.mPolicyVisibilityAfterAnim);
            }
            this.mPolicyVisibility = this.mPolicyVisibilityAfterAnim;
            if (!this.mPolicyVisibility) {
                this.mWinAnimator.hide("checkPolicyVisibilityChange");
                if (this.mService.mCurrentFocus == this) {
                    if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                        Slog.i(TAG, "setAnimationLocked: setting mFocusMayChange true");
                    }
                    this.mService.mFocusMayChange = true;
                }
                setDisplayLayoutNeeded();
                this.mService.enableScreenIfNeededLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setRequestedSize(int requestedWidth, int requestedHeight) {
        if (this.mRequestedWidth != requestedWidth || this.mRequestedHeight != requestedHeight) {
            this.mLayoutNeeded = true;
            this.mRequestedWidth = requestedWidth;
            this.mRequestedHeight = requestedHeight;
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareWindowToDisplayDuringRelayout(boolean wasVisible) {
        boolean hasTurnScreenOnFlag = (this.mAttrs.flags & DumpState.DUMP_COMPILER_STATS) != 0;
        boolean allowTheaterMode = this.mService.mAllowTheaterModeWakeFromLayout || Settings.Global.getInt(this.mService.mContext.getContentResolver(), "theater_mode_on", 0) == 0;
        boolean canTurnScreenOn = this.mAppToken == null || this.mAppToken.canTurnScreenOn();
        if (hasTurnScreenOnFlag) {
            if (allowTheaterMode && canTurnScreenOn && !this.mPowerManagerWrapper.isInteractive()) {
                Slog.v(TAG, "Relayout window turning screen on: " + this);
                this.mPowerManagerWrapper.wakeUp(SystemClock.uptimeMillis(), "android.server.wm:TURN_ON");
            }
            if (this.mAppToken != null) {
                this.mAppToken.setCanTurnScreenOn(false);
            }
        }
        if (wasVisible) {
            Flog.i(307, "Already visible and does not turn on screen, skip preparing: " + this);
            if (!(this.mAppToken == null || this.mAppToken.getController() == null || !this.mAppToken.allDrawn)) {
                Flog.i(307, "wasVisible so remove StartingWindow " + this.mAppToken);
                this.mAppToken.getController().removeStartingWindow();
            }
            return;
        }
        if ((this.mAttrs.softInputMode & 240) == 16) {
            this.mLayoutNeeded = true;
        }
        if (isDrawnLw() && this.mToken.okToAnimate()) {
            this.mWinAnimator.applyEnterAnimationLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public void getMergedConfiguration(MergedConfiguration outConfiguration) {
        outConfiguration.setConfiguration(this.mService.mRoot.getConfiguration(), getMergedOverrideConfiguration());
        if (this.mService.mHwWMSEx != null) {
            this.mService.mHwWMSEx.onChangeConfiguration(outConfiguration, this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setLastReportedMergedConfiguration(MergedConfiguration config) {
        this.mLastReportedConfiguration.setTo(config);
    }

    /* access modifiers changed from: package-private */
    public void getLastReportedMergedConfiguration(MergedConfiguration config) {
        config.setTo(this.mLastReportedConfiguration);
    }

    private Configuration getLastReportedConfiguration() {
        if (!HwPCUtils.enabledInPad() || !HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(getDisplayId()) || this.mAppToken != null) {
            return this.mLastReportedConfiguration.getMergedConfiguration();
        }
        return getConfiguration();
    }

    /* access modifiers changed from: package-private */
    public void adjustStartingWindowFlags() {
        if (this.mAttrs.type == 1 && this.mAppToken != null && this.mAppToken.startingWindow != null) {
            WindowManager.LayoutParams sa = this.mAppToken.startingWindow.mAttrs;
            sa.flags = (sa.flags & -4718594) | (this.mAttrs.flags & 4718593);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowScale(int requestedWidth, int requestedHeight) {
        float f = 1.0f;
        if ((this.mAttrs.flags & 16384) != 0) {
            this.mHScale = this.mAttrs.width != requestedWidth ? ((float) this.mAttrs.width) / ((float) requestedWidth) : 1.0f;
            if (this.mAttrs.height != requestedHeight) {
                f = ((float) this.mAttrs.height) / ((float) requestedHeight);
            }
            this.mVScale = f;
            return;
        }
        this.mVScale = 1.0f;
        this.mHScale = 1.0f;
    }

    /* access modifiers changed from: private */
    public boolean shouldKeepVisibleDeadAppWindow() {
        if (!isWinVisibleLw() || this.mAppToken == null || this.mAppToken.isClientHidden() || this.mAttrs.token != this.mClient.asBinder() || this.mAttrs.type == 3) {
            return false;
        }
        return getWindowConfiguration().keepVisibleDeadAppWindowOnScreen();
    }

    /* access modifiers changed from: package-private */
    public boolean canReceiveKeys() {
        return isVisibleOrAdding() && this.mViewVisibility == 0 && !this.mRemoveOnExit && (this.mAttrs.flags & 8) == 0 && (this.mAppToken == null || this.mAppToken.windowsAreFocusable()) && !canReceiveTouchInput();
    }

    /* access modifiers changed from: package-private */
    public boolean canReceiveTouchInput() {
        return (this.mAppToken == null || this.mAppToken.getTask() == null || !this.mAppToken.getTask().mStack.shouldIgnoreInput()) ? false : true;
    }

    public boolean hasDrawnLw() {
        return this.mWinAnimator.mDrawState == 4;
    }

    public boolean showLw(boolean doAnimation) {
        return showLw(doAnimation, true);
    }

    /* access modifiers changed from: package-private */
    public boolean showLw(boolean doAnimation, boolean requestAnim) {
        if (isHiddenFromUserLocked() || !this.mAppOpVisibility || this.mPermanentlyHidden || this.mHiddenWhileSuspended || this.mForceHideNonSystemOverlayWindow) {
            return false;
        }
        if (this.mPolicyVisibility && this.mPolicyVisibilityAfterAnim) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v(TAG, "Policy visibility true: " + this);
        }
        if (doAnimation) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "doAnimation: mPolicyVisibility=" + this.mPolicyVisibility + " isAnimationSet=" + this.mWinAnimator.isAnimationSet());
            }
            if (!this.mToken.okToAnimate()) {
                doAnimation = false;
            } else if (this.mPolicyVisibility && !this.mWinAnimator.isAnimationSet()) {
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

    /* access modifiers changed from: package-private */
    public boolean hideLw(boolean doAnimation, boolean requestAnim) {
        if (doAnimation && !this.mToken.okToAnimate()) {
            doAnimation = false;
        }
        if (!(doAnimation ? this.mPolicyVisibilityAfterAnim : this.mPolicyVisibility)) {
            return false;
        }
        if (doAnimation) {
            this.mWinAnimator.applyAnimationLocked(2, false);
            if (!this.mWinAnimator.isAnimationSet()) {
                doAnimation = false;
            }
        }
        this.mPolicyVisibilityAfterAnim = false;
        if (!doAnimation) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Policy visibility false: " + this);
            }
            this.mPolicyVisibility = false;
            this.mService.enableScreenIfNeededLocked();
            if (this.mService.mCurrentFocus == this) {
                if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                    Slog.i(TAG, "WindowState.hideLw: setting mFocusMayChange true");
                }
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

    /* access modifiers changed from: package-private */
    public void setForceHideNonSystemOverlayWindowIfNeeded(boolean forceHide) {
        if (!this.mOwnerCanAddInternalSystemWindow && ((WindowManager.LayoutParams.isSystemAlertWindowType(this.mAttrs.type) || this.mAttrs.type == 2005) && this.mForceHideNonSystemOverlayWindow != forceHide)) {
            this.mForceHideNonSystemOverlayWindow = forceHide;
            if (forceHide) {
                hideLw(true, true);
            } else {
                showLw(true, true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setHiddenWhileSuspended(boolean hide) {
        if (!this.mOwnerCanAddInternalSystemWindow && ((WindowManager.LayoutParams.isSystemAlertWindowType(this.mAttrs.type) || this.mAttrs.type == 2005) && this.mHiddenWhileSuspended != hide)) {
            this.mHiddenWhileSuspended = hide;
            if (hide) {
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

    /* access modifiers changed from: package-private */
    public void initAppOpsState() {
        if (this.mAppOp != -1 && this.mAppOpVisibility) {
            int mode = this.mService.mAppOps.startOpNoThrow(this.mAppOp, getOwningUid(), getOwningPackage(), true);
            if (!(mode == 0 || mode == 3)) {
                this.mService.mHwWMSEx.setAppOpHideHook(this, false);
            }
            this.mService.mHwWMSEx.addWindowReport(this, mode);
            this.mService.mHwWMSEx.setVisibleFromParent(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void resetAppOpsState() {
        if (this.mAppOp != -1 && this.mAppOpVisibility) {
            this.mService.mAppOps.finishOp(this.mAppOp, getOwningUid(), getOwningPackage());
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAppOpsState() {
        if (this.mAppOp != -1) {
            int uid = getOwningUid();
            String packageName = getOwningPackage();
            if (this.mAppOpVisibility) {
                int mode = this.mService.mAppOps.checkOpNoThrow(this.mAppOp, uid, packageName);
                if (!(mode == 0 || mode == 3)) {
                    this.mService.mAppOps.finishOp(this.mAppOp, uid, packageName);
                    setAppOpVisibilityLw(false);
                }
            } else {
                int mode2 = this.mService.mAppOps.startOpNoThrow(this.mAppOp, uid, packageName, true);
                if (mode2 == 0 || mode2 == 3) {
                    setAppOpVisibilityLw(true);
                }
            }
        }
    }

    public void hidePermanentlyLw() {
        if (!this.mPermanentlyHidden) {
            this.mPermanentlyHidden = true;
            hideLw(true, true);
            HwServiceFactory.reportToastHiddenToIAware(this.mSession.mPid, System.identityHashCode(this));
        }
    }

    public void pokeDrawLockLw(long timeout) {
        if (isVisibleOrAdding()) {
            if (this.mDrawLock == null) {
                CharSequence tag = getWindowTag();
                PowerManager powerManager = this.mService.mPowerManager;
                this.mDrawLock = powerManager.newWakeLock(128, "Window:" + tag);
                this.mDrawLock.setReferenceCounted(false);
                this.mDrawLock.setWorkSource(new WorkSource(this.mOwnerUid, this.mAttrs.packageName));
            }
            this.mDrawLock.acquire(timeout);
        }
    }

    public boolean isAlive() {
        return this.mClient.asBinder().isBinderAlive();
    }

    /* access modifiers changed from: package-private */
    public boolean isClosing() {
        return this.mAnimatingExit || (this.mAppToken != null && this.mAppToken.isClosingOrEnteringPip());
    }

    /* access modifiers changed from: package-private */
    public void addWinAnimatorToList(ArrayList<WindowStateAnimator> animators) {
        animators.add(this.mWinAnimator);
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).addWinAnimatorToList(animators);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendAppVisibilityToClients() {
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
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    Slog.v(TAG, "Setting visibility of " + this + ": " + (!clientHidden));
                }
                this.mClient.dispatchAppVisibility(!clientHidden);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onStartFreezingScreen() {
        this.mAppFreezing = true;
        int i = this.mChildren.size() - 1;
        while (true) {
            int i2 = i;
            if (i2 >= 0) {
                ((WindowState) this.mChildren.get(i2)).onStartFreezingScreen();
                i = i2 - 1;
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean onStopFreezingScreen() {
        boolean unfrozeWindows = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            unfrozeWindows |= ((WindowState) this.mChildren.get(i)).onStopFreezingScreen();
        }
        if (this.mAppFreezing == 0) {
            return unfrozeWindows;
        }
        this.mAppFreezing = false;
        if (this.mHasSurface && !getOrientationChanging() && this.mService.mWindowsFreezingScreen != 2) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "set mOrientationChanging of " + this);
            }
            setOrientationChanging(true);
            this.mService.mRoot.mOrientationChangeComplete = false;
        }
        this.mLastFreezeDuration = 0;
        setDisplayLayoutNeeded();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean destroySurface(boolean cleanupOnResume, boolean appStopped) {
        boolean destroyedSomething = false;
        ArrayList<WindowState> childWindows = new ArrayList<>(this.mChildren);
        for (int i = childWindows.size() - 1; i >= 0; i--) {
            destroyedSomething |= childWindows.get(i).destroySurface(cleanupOnResume, appStopped);
        }
        if (!appStopped && !this.mWindowRemovalAllowed && !cleanupOnResume && !"com.touchtype.swiftkey".equals(getOwningPackage())) {
            return destroyedSomething;
        }
        if (appStopped || this.mWindowRemovalAllowed) {
            this.mWinAnimator.destroyPreservedSurfaceLocked();
        }
        if (this.mDestroying) {
            Slog.e(TAG, "win=" + this + " destroySurfaces: appStopped=" + appStopped + " win.mWindowRemovalAllowed=" + this.mWindowRemovalAllowed + " win.mRemoveOnExit=" + this.mRemoveOnExit);
            if (!cleanupOnResume || this.mRemoveOnExit) {
                destroySurfaceUnchecked();
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

    /* access modifiers changed from: package-private */
    public void destroySurfaceUnchecked() {
        this.mWinAnimator.destroySurfaceLocked();
        this.mAnimatingExit = false;
    }

    public boolean isDefaultDisplay() {
        DisplayContent displayContent = getDisplayContent();
        if (displayContent == null) {
            return false;
        }
        return displayContent.isDefaultDisplay;
    }

    /* access modifiers changed from: package-private */
    public void setShowToOwnerOnlyLocked(boolean showToOwnerOnly) {
        this.mShowToOwnerOnly = showToOwnerOnly;
    }

    private boolean isHiddenFromUserLocked() {
        WindowState win = getTopParentWindow();
        boolean z = false;
        if (win.mAttrs.type < 2000 && win.mAppToken != null && win.mAppToken.mShowForAllUsers && win.mFrame.left <= win.mDisplayFrame.left && win.mFrame.top <= win.mDisplayFrame.top && win.mFrame.right >= win.mStableFrame.right && win.mFrame.bottom >= win.mStableFrame.bottom) {
            return false;
        }
        if (win.mShowToOwnerOnly && !this.mService.isCurrentProfileLocked(UserHandle.getUserId(win.mOwnerUid))) {
            z = true;
        }
        return z;
    }

    private static void applyInsets(Region outRegion, Rect frame, Rect inset) {
        outRegion.set(frame.left + inset.left, frame.top + inset.top, frame.right - inset.right, frame.bottom - inset.bottom);
    }

    /* access modifiers changed from: package-private */
    public void getTouchableRegion(Region outRegion) {
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
                WindowManagerService windowManagerService = this.mService;
                if (WindowManagerService.mSupporInputMethodFilletAdaptation && isImeWithHwFlag() && this.mService.mPolicy.isInputMethodMovedUp()) {
                    Rect tmpRect = outRegion.getBounds();
                    tmpRect.bottom = frame.bottom;
                    outRegion.set(tmpRect);
                    break;
                }
            default:
                outRegion.set(frame);
                break;
        }
        cropRegionToStackBoundsIfNeeded(outRegion);
    }

    private void cropRegionToStackBoundsIfNeeded(Region region) {
        Task task = getTask();
        if (task != null && task.cropWindowsToStackBounds()) {
            TaskStack stack = task.mStack;
            if (stack != null) {
                stack.getDimBounds(this.mTmpRect);
                region.op(this.mTmpRect, Region.Op.INTERSECT);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportFocusChangedSerialized(boolean focused, boolean inTouchMode) {
        try {
            this.mClient.windowFocusChanged(focused, inTouchMode);
        } catch (RemoteException e) {
        }
        if (this.mFocusCallbacks != null) {
            int N = this.mFocusCallbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {
                IWindowFocusObserver obs = this.mFocusCallbacks.getBroadcastItem(i);
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
        return this.mAppToken.mFrozenMergedConfig.peek();
    }

    /* access modifiers changed from: package-private */
    public void reportResized() {
        WindowState windowState;
        boolean z;
        final Rect frame;
        Trace.traceBegin(32, "wm.reportResized_" + getWindowTag());
        try {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Reporting new frame to " + this + ": " + this.mCompatFrame);
            }
            MergedConfiguration mergedConfiguration = new MergedConfiguration(this.mService.mRoot.getConfiguration(), getMergedOverrideConfiguration());
            if (this.mService.mHwWMSEx != null) {
                this.mService.mHwWMSEx.onChangeConfiguration(mergedConfiguration, this);
            }
            setLastReportedMergedConfiguration(mergedConfiguration);
            final boolean reportDraw = true;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION && this.mWinAnimator.mDrawState == 1) {
                Slog.i(TAG, "Resizing " + this + " WITH DRAW PENDING");
            }
            if (!this.mEnforceSizeCompat || isInHwFreeFormWorkspace()) {
                frame = this.mFrame;
            } else {
                frame = this.mCompatFrame;
            }
            final Rect overscanInsets = this.mLastOverscanInsets;
            final Rect contentInsets = this.mLastContentInsets;
            final Rect visibleInsets = this.mLastVisibleInsets;
            final Rect stableInsets = this.mLastStableInsets;
            final Rect outsets = this.mLastOutsets;
            if (this.mWinAnimator.mDrawState != 1) {
                reportDraw = false;
            }
            final boolean reportOrientation = this.mReportOrientationChanged;
            final int displayId = getDisplayId();
            final DisplayCutout displayCutout = this.mDisplayCutout.getDisplayCutout();
            if (this.mAttrs.type == 3 || !(this.mClient instanceof IWindow.Stub)) {
                z = false;
                windowState = this;
                try {
                    dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, reportOrientation, displayId, displayCutout);
                } catch (RemoteException e) {
                    windowState.setOrientationChanging(z);
                    windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mService.mDisplayFreezeTime);
                    windowState.mOverscanInsetsChanged = z;
                    windowState.mContentInsetsChanged = z;
                    windowState.mVisibleInsetsChanged = z;
                    windowState.mStableInsetsChanged = z;
                    windowState.mWinAnimator.mSurfaceResized = z;
                    Slog.w(TAG, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
                    windowState.mService.mPendingRemove.add(windowState);
                    windowState.mService.mWindowPlacerLocked.requestTraversal();
                    Trace.traceEnd(32);
                }
            } else {
                r1 = r1;
                AnonymousClass3 r14 = r1;
                WindowManagerService.H h = this.mService.mH;
                final MergedConfiguration mergedConfiguration2 = mergedConfiguration;
                try {
                    AnonymousClass3 r1 = new Runnable() {
                        public void run() {
                            try {
                                WindowState.this.dispatchResized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration2, reportOrientation, displayId, displayCutout);
                            } catch (RemoteException e) {
                            }
                        }
                    };
                    h.post(r14);
                    z = false;
                    windowState = this;
                } catch (RemoteException e2) {
                    z = false;
                    windowState = this;
                    windowState.setOrientationChanging(z);
                    windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mService.mDisplayFreezeTime);
                    windowState.mOverscanInsetsChanged = z;
                    windowState.mContentInsetsChanged = z;
                    windowState.mVisibleInsetsChanged = z;
                    windowState.mStableInsetsChanged = z;
                    windowState.mWinAnimator.mSurfaceResized = z;
                    Slog.w(TAG, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
                    windowState.mService.mPendingRemove.add(windowState);
                    windowState.mService.mWindowPlacerLocked.requestTraversal();
                    Trace.traceEnd(32);
                }
            }
            if (windowState.mService.mAccessibilityController != null && getDisplayId() == 0) {
                windowState.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            windowState.mOverscanInsetsChanged = z;
            windowState.mContentInsetsChanged = z;
            windowState.mVisibleInsetsChanged = z;
            windowState.mStableInsetsChanged = z;
            windowState.mOutsetsChanged = z;
            windowState.mFrameSizeChanged = z;
            windowState.mDisplayCutoutChanged = z;
            windowState.mWinAnimator.mSurfaceResized = z;
            windowState.mReportOrientationChanged = z;
        } catch (RemoteException e3) {
            windowState = this;
            z = false;
            windowState.setOrientationChanging(z);
            windowState.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - windowState.mService.mDisplayFreezeTime);
            windowState.mOverscanInsetsChanged = z;
            windowState.mContentInsetsChanged = z;
            windowState.mVisibleInsetsChanged = z;
            windowState.mStableInsetsChanged = z;
            windowState.mWinAnimator.mSurfaceResized = z;
            Slog.w(TAG, "Failed to report 'resized' to the client of " + windowState + ", removing this window.");
            windowState.mService.mPendingRemove.add(windowState);
            windowState.mService.mWindowPlacerLocked.requestTraversal();
            Trace.traceEnd(32);
        }
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public Rect getBackdropFrame(Rect frame) {
        boolean resizing = isDragResizing() || isDragResizeChanged();
        if (getWindowConfiguration().useWindowFrameForBackdrop() || !resizing) {
            return frame;
        }
        DisplayInfo displayInfo = getDisplayInfo();
        this.mTmpRect.set(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        return this.mTmpRect;
    }

    /* access modifiers changed from: protected */
    public int getStackId() {
        TaskStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mStackId;
    }

    /* access modifiers changed from: private */
    public void dispatchResized(Rect frame, Rect overscanInsets, Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw, MergedConfiguration mergedConfiguration, boolean reportOrientation, int displayId, DisplayCutout displayCutout) throws RemoteException {
        if (reportDraw && toString().contains("StatusBar")) {
            Slog.d(TAG, "dispatchResized called to report draw");
        }
        this.mClient.resized(frame, overscanInsets, contentInsets, visibleInsets, stableInsets, outsets, reportDraw, mergedConfiguration, getBackdropFrame(frame), isDragResizeChanged() || reportOrientation, this.mPolicy.isNavBarForcedShownLw(this), displayId, new DisplayCutout.ParcelableWrapper(displayCutout));
        this.mDragResizingChangeReported = true;
    }

    public void registerFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mFocusCallbacks == null) {
                    this.mFocusCallbacks = new RemoteCallbackList<>();
                }
                this.mFocusCallbacks.register(observer);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public void unregisterFocusObserver(IWindowFocusObserver observer) {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (this.mFocusCallbacks != null) {
                    this.mFocusCallbacks.unregister(observer);
                }
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    public boolean isFocused() {
        boolean z;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                z = this.mService.mCurrentFocus == this;
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        return z;
    }

    public boolean isInMultiWindowMode() {
        Task task = getTask();
        return task != null && !task.isFullscreen();
    }

    private boolean inFullscreenContainer() {
        boolean z = true;
        if (((IS_FULL_SCREEN || (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer())) && this.mService != null && !this.mService.mKeyguardGoingAway && this.mService.isKeyguardLocked() && (this.mAttrs.flags & DumpState.DUMP_FROZEN) != 0) || isInHideCaptionList()) {
            return true;
        }
        if (this.mAppToken != null && (!this.mAppToken.matchParentBounds() || isInMultiWindowMode())) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isLetterboxedAppWindow() {
        return (!isInMultiWindowMode() && this.mAppToken != null && !this.mAppToken.matchParentBounds()) || isLetterboxedForDisplayCutoutLw();
    }

    public boolean isLetterboxedForDisplayCutoutLw() {
        if (this.mAppToken != null && this.mParentFrameWasClippedByDisplayCutout && this.mAttrs.layoutInDisplayCutoutMode != 1 && this.mAttrs.isFullscreen()) {
            return !frameCoversEntireAppTokenBounds();
        }
        return false;
    }

    private boolean frameCoversEntireAppTokenBounds() {
        this.mTmpRect.set(this.mAppToken.getBounds());
        this.mTmpRect.intersectUnchecked(this.mFrame);
        return this.mAppToken.getBounds().equals(this.mTmpRect);
    }

    public boolean isLetterboxedOverlappingWith(Rect rect) {
        return this.mAppToken != null && this.mAppToken.isLetterboxOverlappingWith(rect);
    }

    /* access modifiers changed from: package-private */
    public boolean isDragResizeChanged() {
        return this.mDragResizing != computeDragResizing();
    }

    /* access modifiers changed from: package-private */
    public void setWaitingForDrawnIfResizingChanged() {
        if (isDragResizeChanged()) {
            this.mService.mWaitingForDrawn.add(this);
        }
        super.setWaitingForDrawnIfResizingChanged();
    }

    private boolean isDragResizingChangeReported() {
        return this.mDragResizingChangeReported;
    }

    /* access modifiers changed from: package-private */
    public void resetDragResizingChangeReported() {
        this.mDragResizingChangeReported = false;
        super.resetDragResizingChangeReported();
    }

    /* access modifiers changed from: package-private */
    public int getResizeMode() {
        return this.mResizeMode;
    }

    private boolean computeDragResizing() {
        Task task = getTask();
        boolean z = false;
        if (task == null) {
            return false;
        }
        if ((!inSplitScreenWindowingMode() && !inFreeformWindowingMode()) || this.mAttrs.width != -1 || this.mAttrs.height != -1) {
            return false;
        }
        if (task.isDragResizing()) {
            return true;
        }
        if ((getDisplayContent().mDividerControllerLocked.isResizing() || (this.mAppToken != null && !this.mAppToken.mFrozenBounds.isEmpty())) && !task.inFreeformWindowingMode() && !task.inHwPCFreeformWindowingMode() && !isGoneForLayoutLw()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setDragResizing() {
        int i;
        boolean resizing = computeDragResizing();
        if (resizing != this.mDragResizing) {
            this.mDragResizing = resizing;
            Task task = getTask();
            if (task == null || !task.isDragResizing()) {
                if (!this.mDragResizing || !getDisplayContent().mDividerControllerLocked.isResizing()) {
                    i = 0;
                } else {
                    i = 1;
                }
                this.mResizeMode = i;
            } else {
                this.mResizeMode = task.getDragResizeMode();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDragResizing() {
        return this.mDragResizing;
    }

    /* access modifiers changed from: package-private */
    public boolean isDockedResizing() {
        if (this.mDragResizing && getResizeMode() == 1) {
            return true;
        }
        if (!isChildWindow() || !getParentWindow().isDockedResizing()) {
            return false;
        }
        return true;
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        writeIdentifierToProto(proto, 1146756268034L);
        proto.write(1120986464259L, getDisplayId());
        proto.write(1120986464260L, getStackId());
        this.mAttrs.writeToProto(proto, 1146756268037L);
        this.mGivenContentInsets.writeToProto(proto, 1146756268038L);
        this.mFrame.writeToProto(proto, 1146756268039L);
        this.mContainingFrame.writeToProto(proto, 1146756268040L);
        this.mParentFrame.writeToProto(proto, 1146756268041L);
        this.mContentFrame.writeToProto(proto, 1146756268042L);
        this.mContentInsets.writeToProto(proto, 1146756268043L);
        this.mAttrs.surfaceInsets.writeToProto(proto, 1146756268044L);
        this.mSurfacePosition.writeToProto(proto, 1146756268048L);
        this.mWinAnimator.writeToProto(proto, 1146756268045L);
        proto.write(1133871366158L, this.mAnimatingExit);
        for (int i = 0; i < this.mChildren.size(); i++) {
            ((WindowState) this.mChildren.get(i)).writeToProto(proto, 2246267895823L, trim);
        }
        proto.write(1120986464274L, this.mRequestedWidth);
        proto.write(1120986464275L, this.mRequestedHeight);
        proto.write(1120986464276L, this.mViewVisibility);
        proto.write(1120986464277L, this.mSystemUiVisibility);
        proto.write(1133871366166L, this.mHasSurface);
        proto.write(1133871366167L, isReadyForDisplay());
        this.mDisplayFrame.writeToProto(proto, 1146756268056L);
        this.mOverscanFrame.writeToProto(proto, 1146756268057L);
        this.mVisibleFrame.writeToProto(proto, 1146756268058L);
        this.mDecorFrame.writeToProto(proto, 1146756268059L);
        this.mOutsetFrame.writeToProto(proto, 1146756268060L);
        this.mOverscanInsets.writeToProto(proto, 1146756268061L);
        this.mVisibleInsets.writeToProto(proto, 1146756268062L);
        this.mStableInsets.writeToProto(proto, 1146756268063L);
        this.mOutsets.writeToProto(proto, 1146756268064L);
        this.mDisplayCutout.getDisplayCutout().writeToProto(proto, 1146756268065L);
        proto.write(1133871366178L, this.mRemoveOnExit);
        proto.write(1133871366179L, this.mDestroying);
        proto.write(1133871366180L, this.mRemoved);
        proto.write(1133871366181L, isOnScreen());
        proto.write(1133871366182L, isVisible());
        proto.end(token);
    }

    public void writeIdentifierToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, System.identityHashCode(this));
        proto.write(1120986464258L, UserHandle.getUserId(this.mOwnerUid));
        CharSequence title = getWindowTag();
        if (title != null) {
            proto.write(1138166333443L, title.toString());
        }
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        boolean z;
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
        pw.println(this.mAttrs.toString(prefix));
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
                pw.print(" mAppDied=");
                pw.print(this.mAppDied);
                pw.print(prefix);
                pw.print("drawnStateEvaluated=");
                pw.print(getDrawnStateEvaluated());
                pw.print(prefix);
                pw.print("mightAffectAllDrawn=");
                pw.println(mightAffectAllDrawn());
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
        if (!this.mPolicyVisibility || !this.mPolicyVisibilityAfterAnim || !this.mAppOpVisibility || isParentWindowHidden() || this.mPermanentlyHidden || this.mForceHideNonSystemOverlayWindow || this.mHiddenWhileSuspended) {
            pw.print(prefix);
            pw.print("mPolicyVisibility=");
            pw.print(this.mPolicyVisibility);
            pw.print(" mPolicyVisibilityAfterAnim=");
            pw.print(this.mPolicyVisibilityAfterAnim);
            pw.print(" mAppOpVisibility=");
            pw.print(this.mAppOpVisibility);
            pw.print(" parentHidden=");
            pw.print(isParentWindowHidden());
            pw.print(" mPermanentlyHidden=");
            pw.print(this.mPermanentlyHidden);
            pw.print(" mHiddenWhileSuspended=");
            pw.print(this.mHiddenWhileSuspended);
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
        pw.print(" isReadyForDisplay()=");
        pw.print(isReadyForDisplay());
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
            pw.print(" cutout=" + this.mDisplayCutout.getDisplayCutout());
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
            pw.print(" cutout=" + this.mLastDisplayCutout);
            pw.println();
        }
        super.dump(pw, prefix, dumpAll);
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
        boolean z2 = true;
        if (getOrientationChanging() || this.mAppFreezing || this.mReportOrientationChanged) {
            pw.print(prefix);
            pw.print("mOrientationChanging=");
            pw.print(this.mOrientationChanging);
            pw.print(" configOrientationChanging=");
            if (getLastReportedConfiguration().orientation != getConfiguration().orientation) {
                z = true;
            } else {
                z = false;
            }
            pw.print(z);
            pw.print(" mAppFreezing=");
            pw.print(this.mAppFreezing);
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
        pw.println("isOnScreen=" + isOnScreen());
        pw.print(prefix);
        pw.println("isVisible=" + isVisible());
        pw.print(prefix);
        pw.println("canReceiveKeys=" + canReceiveKeys());
        pw.print(prefix);
        StringBuilder sb = new StringBuilder();
        sb.append("hwNotchSupport=");
        if ((this.mAttrs.hwFlags & 65536) == 0) {
            z2 = getHwNotchSupport();
        }
        sb.append(z2);
        pw.println(sb.toString());
        pw.print(prefix);
        pw.print("mEnforceSizeCompat=");
        pw.print(this.mEnforceSizeCompat);
        pw.print(" compat=");
        this.mCompatFrame.printShortString(pw);
        pw.print(" mInvGlobalScale=");
        pw.print(this.mInvGlobalScale);
        pw.print(" mGlobalScale=");
        pw.println(this.mGlobalScale);
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return Integer.toHexString(System.identityHashCode(this)) + " " + getWindowTag();
    }

    /* access modifiers changed from: package-private */
    public CharSequence getWindowTag() {
        CharSequence tag = this.mAttrs.getTitle();
        if (tag == null || tag.length() <= 0) {
            return this.mAttrs.packageName;
        }
        return tag;
    }

    public String toString() {
        boolean containPrivacyInfo;
        CharSequence title = getWindowTag();
        if (!(this.mStringNameCache != null && this.mLastTitle == title && this.mWasExiting == this.mAnimatingExit)) {
            this.mLastTitle = title;
            this.mWasExiting = this.mAnimatingExit;
            if (this.mLastTitle instanceof Spanned) {
                Spanned text = (Spanned) this.mLastTitle;
                containPrivacyInfo = false;
                for (Object tmpSpan : text.getSpans(0, text.length(), Object.class)) {
                    if (tmpSpan instanceof SuggestionSpan) {
                        String[] suggestions = ((SuggestionSpan) tmpSpan).getSuggestions();
                        if (suggestions.length > 0) {
                            boolean equals = suggestions[0].equals("privacy title");
                            containPrivacyInfo = equals;
                            if (equals) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } else {
                containPrivacyInfo = false;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Window{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" u");
            sb.append(UserHandle.getUserId(this.mOwnerUid));
            sb.append(" ");
            sb.append(containPrivacyInfo ? "xxxxxx" : this.mLastTitle);
            sb.append(this.mAnimatingExit ? " EXITING}" : "}");
            this.mStringNameCache = sb.toString();
        }
        return this.mStringNameCache;
    }

    /* access modifiers changed from: package-private */
    public void transformClipRectFromScreenToSurfaceSpace(Rect clipRect) {
        if (this.mHScale >= 0.0f) {
            clipRect.left = (int) (((float) clipRect.left) / this.mHScale);
            clipRect.right = (int) Math.ceil((double) (((float) clipRect.right) / this.mHScale));
        }
        if (this.mVScale >= 0.0f) {
            clipRect.top = (int) (((float) clipRect.top) / this.mVScale);
            clipRect.bottom = (int) Math.ceil((double) (((float) clipRect.bottom) / this.mVScale));
        }
    }

    /* access modifiers changed from: package-private */
    public void applyGravityAndUpdateFrame(Rect containingFrame, Rect displayFrame) {
        int h;
        int w;
        float x;
        float y;
        int w2;
        Rect rect = containingFrame;
        int pw = containingFrame.width();
        int ph = containingFrame.height();
        Task task = getTask();
        boolean inNonFullscreenContainer = !inFullscreenContainer();
        boolean z = false;
        boolean noLimits = (this.mAttrs.flags & 512) != 0;
        if (task == null || !inNonFullscreenContainer || (this.mAttrs.type != 1 && !noLimits)) {
            z = true;
        }
        boolean fitToDisplay = z;
        if ((this.mAttrs.flags & 16384) != 0) {
            if (this.mAttrs.width < 0) {
                w = pw;
            } else if (this.mEnforceSizeCompat != 0) {
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
                w2 = pw;
            } else if (this.mEnforceSizeCompat != 0) {
                w2 = (int) ((((float) this.mRequestedWidth) * this.mGlobalScale) + 0.5f);
            } else {
                w2 = this.mRequestedWidth;
            }
            if (this.mAttrs.height == -1) {
                h = ph;
            } else if (this.mEnforceSizeCompat) {
                h = (int) ((((float) this.mRequestedHeight) * this.mGlobalScale) + 0.5f);
            } else {
                h = this.mRequestedHeight;
            }
        }
        if (this.mEnforceSizeCompat) {
            x = ((float) this.mAttrs.x) * this.mGlobalScale;
            y = ((float) this.mAttrs.y) * this.mGlobalScale;
        } else {
            x = (float) this.mAttrs.x;
            y = (float) this.mAttrs.y;
        }
        float x2 = x;
        float y2 = y;
        if (inNonFullscreenContainer && !layoutInParentFrame()) {
            w = Math.min(w, pw);
            h = Math.min(h, ph);
        }
        Gravity.apply(this.mAttrs.gravity, w, h, rect, (int) (x2 + (this.mAttrs.horizontalMargin * ((float) pw))), (int) (y2 + (this.mAttrs.verticalMargin * ((float) ph))), this.mFrame);
        if (fitToDisplay) {
            Gravity.applyDisplay(this.mAttrs.gravity, displayFrame, this.mFrame);
        } else {
            Rect rect2 = displayFrame;
        }
        this.mCompatFrame.set(this.mFrame);
        if (this.mEnforceSizeCompat) {
            this.mCompatFrame.scale(this.mInvGlobalScale);
            String title = getWindowTag() != null ? getWindowTag().toString() : null;
            if (title != null && title.equals("Toast") && this.mRequestedWidth - this.mCompatFrame.width() == 1) {
                if (this.mFrame.right + 1 <= rect.right) {
                    this.mFrame.right++;
                    this.mCompatFrame.right++;
                } else if (this.mFrame.left - 1 >= rect.left) {
                    this.mFrame.left--;
                    this.mCompatFrame.left--;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isChildWindow() {
        return this.mIsChildWindow;
    }

    /* access modifiers changed from: package-private */
    public boolean layoutInParentFrame() {
        return this.mIsChildWindow && (this.mAttrs.privateFlags & 65536) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hideNonSystemOverlayWindowsWhenVisible() {
        return (this.mAttrs.privateFlags & DumpState.DUMP_FROZEN) != 0 && this.mSession.mCanHideNonSystemOverlayWindows;
    }

    /* access modifiers changed from: package-private */
    public WindowState getParentWindow() {
        if (this.mIsChildWindow) {
            return (WindowState) super.getParent();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState getTopParentWindow() {
        WindowState topParent = this;
        WindowState current = topParent;
        while (current != null && current.mIsChildWindow) {
            current = current.getParentWindow();
            if (current != null) {
                topParent = current;
            }
        }
        return topParent;
    }

    /* access modifiers changed from: package-private */
    public boolean isParentWindowHidden() {
        WindowState parent = getParentWindow();
        return parent != null && parent.mHidden;
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceWindow(boolean animate) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceWindow(animate);
        }
        if ((this.mAttrs.privateFlags & 32768) == 0 && this.mAttrs.type != 3) {
            this.mWillReplaceWindow = true;
            this.mReplacementWindow = null;
            this.mAnimateReplacingWindow = animate;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearWillReplaceWindow() {
        this.mWillReplaceWindow = false;
        this.mReplacementWindow = null;
        this.mAnimateReplacingWindow = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).clearWillReplaceWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean waitingForReplacement() {
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

    /* access modifiers changed from: package-private */
    public void requestUpdateWallpaperIfNeeded() {
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

    /* access modifiers changed from: package-private */
    public float translateToWindowX(float x) {
        float winX = x - ((float) this.mFrame.left);
        if (this.mEnforceSizeCompat) {
            return winX * this.mGlobalScale;
        }
        return winX;
    }

    /* access modifiers changed from: package-private */
    public float translateToWindowY(float y) {
        float winY = y - ((float) this.mFrame.top);
        if (this.mEnforceSizeCompat) {
            return winY * this.mGlobalScale;
        }
        return winY;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeReplacedWithChildren() {
        return this.mIsChildWindow || this.mAttrs.type == 2 || this.mAttrs.type == 4;
    }

    /* access modifiers changed from: package-private */
    public void setWillReplaceChildWindows() {
        if (shouldBeReplacedWithChildren()) {
            setWillReplaceWindow(false);
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).setWillReplaceChildWindows();
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState getReplacingWindow() {
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

    /* access modifiers changed from: package-private */
    public boolean performShowLocked() {
        if (isHiddenFromUserLocked()) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.w(TAG, "hiding " + this + ", belonging to " + this.mOwnerUid);
            }
            hideLw(false);
            return false;
        }
        logPerformShow("performShow on ");
        int drawState = this.mWinAnimator.mDrawState;
        if (!((drawState != 4 && drawState != 3) || this.mAttrs.type == 3 || this.mAppToken == null)) {
            this.mAppToken.onFirstWindowDrawn(this, this.mWinAnimator);
        }
        if (this.mWinAnimator.mDrawState != 3 || !isReadyForDisplay()) {
            return false;
        }
        logPerformShow("Showing ");
        this.mService.enableScreenIfNeededLocked();
        this.mWinAnimator.applyEnterAnimationLocked();
        this.mWinAnimator.mLastAlpha = -1.0f;
        this.mWinAnimator.mDrawState = 4;
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
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY || (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && this.mAttrs.type == 3)) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            sb.append(this);
            sb.append(": mDrawState=");
            sb.append(this.mWinAnimator.drawStateToString());
            sb.append(" readyForDisplay=");
            sb.append(isReadyForDisplay());
            sb.append(" starting=");
            boolean z = false;
            sb.append(this.mAttrs.type == 3);
            sb.append(" during animation: policyVis=");
            sb.append(this.mPolicyVisibility);
            sb.append(" parentHidden=");
            sb.append(isParentWindowHidden());
            sb.append(" tok.hiddenRequested=");
            sb.append(this.mAppToken != null && this.mAppToken.hiddenRequested);
            sb.append(" tok.hidden=");
            sb.append(this.mAppToken != null && this.mAppToken.isHidden());
            sb.append(" animationSet=");
            sb.append(this.mWinAnimator.isAnimationSet());
            sb.append(" tok animating=");
            if (this.mAppToken != null && this.mAppToken.isSelfAnimating()) {
                z = true;
            }
            sb.append(z);
            sb.append(" Callers=");
            sb.append(Debug.getCallers(4));
            Slog.v(TAG, sb.toString());
        }
    }

    /* access modifiers changed from: package-private */
    public WindowInfo getWindowInfo() {
        WindowInfo windowInfo = WindowInfo.obtain();
        windowInfo.type = this.mAttrs.type;
        windowInfo.layer = this.mLayer;
        windowInfo.token = this.mClient.asBinder();
        if (this.mAppToken != null) {
            windowInfo.activityToken = this.mAppToken.appToken.asBinder();
        }
        windowInfo.title = this.mAttrs.accessibilityTitle;
        boolean z = true;
        boolean isPanelWindow = this.mAttrs.type >= 1000 && this.mAttrs.type <= 1999;
        boolean isAccessibilityOverlay = windowInfo.type == 2032;
        if (TextUtils.isEmpty(windowInfo.title) && (isPanelWindow || isAccessibilityOverlay)) {
            windowInfo.title = this.mAttrs.getTitle();
        }
        windowInfo.accessibilityIdOfAnchor = this.mAttrs.accessibilityIdOfAnchor;
        windowInfo.focused = isFocused();
        Task task = getTask();
        if (task == null || !task.inPinnedWindowingMode()) {
            z = false;
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

    /* access modifiers changed from: package-private */
    public int getHighestAnimLayer() {
        int highest = this.mWinAnimator.mAnimLayer;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            int childLayer = ((WindowState) this.mChildren.get(i)).getHighestAnimLayer();
            if (childLayer > highest) {
                highest = childLayer;
            }
        }
        return highest;
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (this.mChildren.isEmpty()) {
            return applyInOrderWithImeWindows(callback, traverseTopToBottom);
        }
        if (traverseTopToBottom) {
            return forAllWindowTopToBottom(callback);
        }
        return forAllWindowBottomToTop(callback);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: com.android.server.wm.WindowState} */
    /* JADX WARNING: Multi-variable type inference failed */
    private boolean forAllWindowBottomToTop(ToBooleanFunction<WindowState> callback) {
        WindowState child;
        int i = 0;
        int count = this.mChildren.size();
        Object obj = this.mChildren.get(0);
        while (true) {
            child = (WindowState) obj;
            if (i >= count || child.mSubLayer >= 0) {
                break;
            } else if (child.applyInOrderWithImeWindows(callback, false)) {
                return true;
            } else {
                i++;
                if (i >= count) {
                    break;
                }
                obj = this.mChildren.get(i);
            }
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
            child = this.mChildren.get(i);
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: com.android.server.wm.WindowState} */
    /* JADX WARNING: Multi-variable type inference failed */
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
            child = this.mChildren.get(i);
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
            child = this.mChildren.get(i);
        }
        return false;
    }

    private boolean applyInOrderWithImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            if ((isInputMethodTarget() && getDisplayContent().forAllImeWindows(callback, traverseTopToBottom)) || callback.apply(this)) {
                return true;
            }
        } else if (callback.apply(this)) {
            return true;
        } else {
            if (isInputMethodTarget() && getDisplayContent().forAllImeWindows(callback, traverseTopToBottom)) {
                return true;
            }
        }
        return false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: com.android.server.wm.WindowState} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v7, resolved type: com.android.server.wm.WindowState} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public WindowState getWindow(Predicate<WindowState> callback) {
        WindowState windowState = null;
        if (this.mChildren.isEmpty()) {
            if (callback.test(this)) {
                windowState = this;
            }
            return windowState;
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
            child = this.mChildren.get(i);
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
            child = this.mChildren.get(i);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isSelfOrAncestorWindowAnimatingExit() {
        WindowState window = this;
        while (!window.mAnimatingExit) {
            window = window.getParentWindow();
            if (window == null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onExitAnimationDone() {
        if (!this.mChildren.isEmpty()) {
            ArrayList<WindowState> childWindows = new ArrayList<>(this.mChildren);
            for (int i = childWindows.size() - 1; i >= 0; i--) {
                childWindows.get(i).onExitAnimationDone();
            }
        }
        if (this.mWinAnimator == null) {
            Slog.d(TAG, "onExitAnimationDone: " + this);
            return;
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
        if (!isSelfAnimating()) {
            if (this.mService.mAccessibilityController != null && getDisplayId() == 0) {
                this.mService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
            if (isSelfOrAncestorWindowAnimatingExit()) {
                Slog.v(TAG, "Exit animation finished in " + this + ": remove=" + this.mRemoveOnExit);
                this.mDestroying = true;
                boolean hasSurface = this.mWinAnimator.hasSurface();
                if (WindowManagerService.HW_SUPPORT_LAUNCHER_EXIT_ANIM && this.mWinAnimator.mSurfaceController != null) {
                    this.mWinAnimator.mSurfaceController.setWindowClipFlag(0);
                }
                this.mWinAnimator.hide(getPendingTransaction(), "onExitAnimationDone");
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
    }

    /* access modifiers changed from: package-private */
    public boolean clearAnimatingFlags() {
        boolean didSomething = false;
        if (!this.mWillReplaceWindow && !this.mRemoveOnExit) {
            if (this.mAnimatingExit) {
                this.mAnimatingExit = false;
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

    /* access modifiers changed from: package-private */
    public void hideWallpaperWindow(boolean wasDeferred, String reason) {
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

    /* access modifiers changed from: package-private */
    public void dispatchWallpaperVisibility(boolean visible) {
        boolean hideAllowed = getDisplayContent().mWallpaperController.mDeferredHideWallpaper == null;
        if (this.mWallpaperVisible == visible) {
            return;
        }
        if (hideAllowed || visible) {
            this.mWallpaperVisible = visible;
            try {
                Slog.i(TAG, "test-wallpaper Updating vis of wallpaper " + this + ": " + visible + " from:\n" + Debug.getCallers(4));
                this.mClient.dispatchAppVisibility(visible);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasVisibleNotDrawnWallpaper() {
        if (this.mWallpaperVisible && !isDrawnLw()) {
            return true;
        }
        for (int j = this.mChildren.size() - 1; j >= 0; j--) {
            if (((WindowState) this.mChildren.get(j)).hasVisibleNotDrawnWallpaper()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateReportedVisibility(UpdateReportedVisibilityResults results) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((WindowState) this.mChildren.get(i)).updateReportedVisibility(results);
        }
        if (this.mAppFreezing == 0 && this.mViewVisibility == 0 && this.mAttrs.type != 3 && !this.mDestroying) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Win " + this + ": isDrawn=" + isDrawnLw() + ", isAnimationSet=" + this.mWinAnimator.isAnimationSet());
                if (!isDrawnLw()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Not displayed: s=");
                    sb.append(this.mWinAnimator.mSurfaceController);
                    sb.append(" pv=");
                    sb.append(this.mPolicyVisibility);
                    sb.append(" mDrawState=");
                    sb.append(this.mWinAnimator.mDrawState);
                    sb.append(" ph=");
                    sb.append(isParentWindowHidden());
                    sb.append(" th=");
                    sb.append(this.mAppToken != null ? this.mAppToken.hiddenRequested : false);
                    sb.append(" a=");
                    sb.append(this.mWinAnimator.isAnimationSet());
                    Slog.v(TAG, sb.toString());
                }
            }
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

    private boolean skipDecorCrop() {
        if (this.mDecorFrame.isEmpty()) {
            return true;
        }
        if (this.mAppToken != null) {
            return false;
        }
        return this.mToken.canLayerAboveSystemBars();
    }

    /* access modifiers changed from: package-private */
    public void calculatePolicyCrop(Rect policyCrop) {
        DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
        if (!isDefaultDisplay()) {
            policyCrop.set(0, 0, this.mCompatFrame.width(), this.mCompatFrame.height());
            policyCrop.intersect(-this.mCompatFrame.left, -this.mCompatFrame.top, displayInfo.logicalWidth - this.mCompatFrame.left, displayInfo.logicalHeight - this.mCompatFrame.top);
        } else if (skipDecorCrop()) {
            policyCrop.set(0, 0, this.mCompatFrame.width(), this.mCompatFrame.height());
        } else {
            calculateSystemDecorRect(policyCrop);
        }
    }

    private void computeDecorTopCompensation(int systemDeocrRectTop, float scale) {
        this.mDecorTopCompensation = ((int) ((((float) systemDeocrRectTop) * scale) + 0.5f)) - ((int) ((((float) systemDeocrRectTop) * scale) - 0.5f));
    }

    private void calculateSystemDecorRect(Rect systemDecorRect) {
        Rect decorRect = this.mDecorFrame;
        int width = this.mFrame.width();
        int height = this.mFrame.height();
        int left = this.mFrame.left;
        int top = this.mFrame.top;
        boolean cropToDecor = false;
        if (isDockedResizing() || getDisplayContent().mDividerControllerLocked.isResizing()) {
            DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
            systemDecorRect.set(0, 0, Math.max(width, displayInfo.logicalWidth), Math.max(height, displayInfo.logicalHeight));
        } else {
            systemDecorRect.set(0, 0, width, height);
        }
        if (((!inFreeformWindowingMode() && !inHwPCFreeformWindowingMode()) || !isAnimatingLw()) && !isDockedResizing()) {
            cropToDecor = true;
        }
        if (cropToDecor) {
            systemDecorRect.intersect(decorRect.left - left, decorRect.top - top, decorRect.right - left, decorRect.bottom - top);
        }
        if (this.mEnforceSizeCompat && this.mInvGlobalScale != 1.0f) {
            float scale = this.mInvGlobalScale;
            computeDecorTopCompensation(systemDecorRect.top, scale);
            systemDecorRect.left = (int) ((((float) systemDecorRect.left) * scale) - 0.5f);
            systemDecorRect.top = (int) ((((float) systemDecorRect.top) * scale) - 0.5f);
            systemDecorRect.right = (int) ((((float) systemDecorRect.right) * scale) + 0.5f);
            systemDecorRect.bottom = (int) ((((float) systemDecorRect.bottom) * scale) + 0.5f);
        }
    }

    /* access modifiers changed from: package-private */
    public void expandForSurfaceInsets(Rect r) {
        r.inset(-this.mAttrs.surfaceInsets.left, -this.mAttrs.surfaceInsets.top, -this.mAttrs.surfaceInsets.right, -this.mAttrs.surfaceInsets.bottom);
    }

    /* access modifiers changed from: package-private */
    public boolean surfaceInsetsChanging() {
        return !this.mLastSurfaceInsets.equals(this.mAttrs.surfaceInsets);
    }

    /* access modifiers changed from: package-private */
    public int relayoutVisibleWindow(int result, int attrChanges, int oldVisibility) {
        boolean wasVisible = isVisibleLw();
        int i = 0;
        int result2 = result | ((!wasVisible || !isDrawnLw()) ? 2 : 0);
        if (this.mAnimatingExit) {
            Slog.d(TAG, "relayoutVisibleWindow: " + this + " mAnimatingExit=true, mRemoveOnExit=" + this.mRemoveOnExit + ", mDestroying=" + this.mDestroying);
            this.mWinAnimator.cancelExitAnimationForNextAnimationLocked();
            this.mAnimatingExit = false;
        }
        if (this.mDestroying) {
            this.mDestroying = false;
            this.mService.mDestroySurface.remove(this);
        }
        boolean dockedResizing = true;
        if (oldVisibility == 8) {
            this.mWinAnimator.mEnterAnimationPending = true;
        }
        this.mLastVisibleLayoutRotation = getDisplayContent().getRotation();
        this.mWinAnimator.mEnteringAnimation = true;
        prepareWindowToDisplayDuringRelayout(wasVisible);
        if (wasVisible && !this.mService.mPowerManager.isScreenOn() && (this.mAttrs.flags & DumpState.DUMP_COMPILER_STATS) != 0 && this.mOwnerUid == 1001) {
            if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v(TAG, "Turning screen on as FLAG_TURN_SCREEN_ON!");
            }
            this.mService.mPowerManager.wakeUp(SystemClock.uptimeMillis());
        }
        boolean isFormatChanged = false;
        if (this.mAttrs != null && this.mAttrs.format == -3 && oldVisibility == 4) {
            isFormatChanged = true;
            Slog.i(TAG, "relayoutVisibleWindow: set isFormatChanged");
        }
        if ((isFormatChanged || (attrChanges & 8) != 0) && !this.mWinAnimator.tryChangeFormatInPlaceLocked()) {
            this.mWinAnimator.preserveSurfaceLocked();
            result2 |= 6;
        }
        if (isDragResizeChanged()) {
            setDragResizing();
            if (this.mHasSurface && !isChildWindow()) {
                this.mWinAnimator.preserveSurfaceLocked();
                result2 |= 6;
            }
        }
        boolean freeformResizing = isDragResizing() && getResizeMode() == 0;
        if (!isDragResizing() || getResizeMode() != 1) {
            dockedResizing = false;
        }
        int result3 = result2 | (freeformResizing ? 16 : 0);
        if (dockedResizing) {
            i = 8;
        }
        return result3 | i;
    }

    /* access modifiers changed from: package-private */
    public boolean isLaidOut() {
        return this.mLayoutSeq != -1;
    }

    /* access modifiers changed from: package-private */
    public void updateLastInsetValues() {
        this.mLastOverscanInsets.set(this.mOverscanInsets);
        this.mLastContentInsets.set(this.mContentInsets);
        this.mLastVisibleInsets.set(this.mVisibleInsets);
        this.mLastStableInsets.set(this.mStableInsets);
        this.mLastOutsets.set(this.mOutsets);
        this.mLastDisplayCutout = this.mDisplayCutout;
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(Animation anim) {
        AnimationAdapter adapter;
        DisplayInfo displayInfo = getDisplayContent().getDisplayInfo();
        anim.initialize(this.mFrame.width(), this.mFrame.height(), displayInfo.appWidth, displayInfo.appHeight);
        anim.restrictDuration(JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY);
        if (("com.android.contacts".equals(this.mAttrs.packageName) || "com.android.mms".equals(this.mAttrs.packageName)) && this.mAttrs.type == 3) {
            Slog.v(TAG, "skip contacts or mms animation when application starting ");
            anim.scaleCurrentDuration(0.0f);
        } else {
            anim.scaleCurrentDuration(this.mService.getWindowAnimationScaleLocked());
        }
        int lazyMode = this.mService.getLazyMode();
        if (!(getDisplayId() == -1 || getDisplayId() == 0)) {
            lazyMode = 0;
        }
        if (lazyMode == 0) {
            adapter = new LocalAnimationAdapter(new WindowAnimationSpec(anim, this.mSurfacePosition, false), this.mService.mSurfaceAnimationRunner);
            if (HwFoldScreenState.isFoldScreenDevice() && this.mService.isInSubFoldScaleMode()) {
                adapter = new LocalAnimationAdapter(new WindowAnimationSpec(anim, this.mSubFoldModeSurfacePosition, false), this.mService.mSurfaceAnimationRunner);
            }
        } else {
            adapter = new LocalAnimationAdapter(new WindowAnimationSpec(anim, this.mLazyModeSurfacePosition, false), this.mService.mSurfaceAnimationRunner);
        }
        startAnimation(this.mPendingTransaction, adapter);
        commitPendingTransaction();
    }

    private void startMoveAnimation(int left, int top) {
        Point oldPosition = new Point();
        Point newPosition = new Point();
        transformFrameToSurfacePosition(this.mLastFrame.left, this.mLastFrame.top, oldPosition);
        transformFrameToSurfacePosition(left, top, newPosition);
        MoveAnimationSpec moveAnimationSpec = new MoveAnimationSpec(oldPosition.x, oldPosition.y, newPosition.x, newPosition.y);
        startAnimation(getPendingTransaction(), new LocalAnimationAdapter(moveAnimationSpec, this.mService.mSurfaceAnimationRunner));
    }

    private void startAnimation(SurfaceControl.Transaction t, AnimationAdapter adapter) {
        startAnimation(t, adapter, this.mWinAnimator.mLastHidden);
    }

    /* access modifiers changed from: protected */
    public void onAnimationFinished() {
        this.mWinAnimator.onAnimationFinished();
    }

    /* access modifiers changed from: package-private */
    public void getTransformationMatrix(float[] float9, Matrix outMatrix) {
        float9[0] = this.mWinAnimator.mDsDx;
        float9[3] = this.mWinAnimator.mDtDx;
        float9[1] = this.mWinAnimator.mDtDy;
        float9[4] = this.mWinAnimator.mDsDy;
        int x = this.mSurfacePosition.x;
        int y = this.mSurfacePosition.y;
        WindowContainer parent = getParent();
        if (isChildWindow()) {
            WindowState parentWindow = getParentWindow();
            x += parentWindow.mFrame.left - parentWindow.mAttrs.surfaceInsets.left;
            y += parentWindow.mFrame.top - parentWindow.mAttrs.surfaceInsets.top;
        } else if (parent != null) {
            Rect parentBounds = parent.getBounds();
            x += parentBounds.left;
            y += parentBounds.top;
        }
        float9[2] = (float) x;
        float9[5] = (float) y;
        float9[6] = 0.0f;
        float9[7] = 0.0f;
        float9[8] = 1.0f;
        outMatrix.setValues(float9);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldMagnify() {
        if (this.mAttrs.type == 2011 || this.mAttrs.type == 2012 || this.mAttrs.type == 2027 || this.mAttrs.type == 2019 || this.mAttrs.type == 2024) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public SurfaceSession getSession() {
        if (this.mSession.mSurfaceSession != null) {
            return this.mSession.mSurfaceSession;
        }
        return getParent().getSession();
    }

    /* access modifiers changed from: package-private */
    public boolean needsZBoost() {
        if (this.mIsImWindow && this.mService.mInputMethodTarget != null) {
            AppWindowToken appToken = this.mService.mInputMethodTarget.mAppToken;
            if (appToken != null) {
                return appToken.needsZBoost();
            }
        }
        return this.mWillReplaceWindow;
    }

    private void applyDims(Dimmer dimmer) {
        if (!this.mAnimatingExit && this.mAppDied) {
            this.mIsDimming = true;
            dimmer.dimAbove(getPendingTransaction(), this, 0.5f);
        } else if ((this.mAttrs.flags & 2) != 0 && isVisibleNow() && !this.mHidden && !inFreeformWindowingMode()) {
            this.mIsDimming = true;
            dimmer.dimBelow(getPendingTransaction(), this, this.mAttrs.dimAmount);
        }
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaces() {
        Dimmer dimmer = getDimmer();
        this.mIsDimming = false;
        if (dimmer != null) {
            applyDims(dimmer);
        }
        updateSurfacePosition();
        this.mWinAnimator.prepareSurfaceLocked(true);
        if (!((!this.mWinAnimator.mLazyIsExiting && !this.mWinAnimator.mLazyIsEntering) || this.mService.getDefaultDisplayContentLocked() == null || this.mService.getDefaultDisplayContentLocked().mTmpWindowAnimator == null)) {
            this.mService.getDefaultDisplayContentLocked().mTmpWindowAnimator.setIsLazying(true);
        }
        super.prepareSurfaces();
    }

    public void onAnimationLeashCreated(SurfaceControl.Transaction t, SurfaceControl leash) {
        super.onAnimationLeashCreated(t, leash);
        t.setPosition(this.mSurfaceControl, 0.0f, 0.0f);
        this.mLastSurfacePosition.set(0, 0);
        this.mLazyModeSurfacePosition.set(0, 0);
        if (HwFoldScreenState.isFoldScreenDevice()) {
            this.mSubFoldModeSurfacePosition.set(0, 0);
        }
    }

    public void onAnimationLeashDestroyed(SurfaceControl.Transaction t) {
        super.onAnimationLeashDestroyed(t);
        updateSurfacePosition(t);
    }

    /* access modifiers changed from: package-private */
    public void updateSurfacePosition() {
        updateSurfacePosition(getPendingTransaction());
    }

    private void updateSurfacePosition(SurfaceControl.Transaction t) {
        if (this.mSurfaceControl != null) {
            transformFrameToSurfacePosition(this.mFrame.left, this.mFrame.top, this.mSurfacePosition);
            boolean isFoldDisplayModeChanged = false;
            if (HwFoldScreenState.isFoldScreenDevice()) {
                int currentFoldDisplayMode = this.mService.getFoldDisplayMode();
                if (this.mCurrentFoldDisplayMode != currentFoldDisplayMode) {
                    isFoldDisplayModeChanged = true;
                    this.mCurrentFoldDisplayMode = currentFoldDisplayMode;
                }
            }
            if ((!this.mSurfaceAnimator.hasLeash() && !this.mLastSurfacePosition.equals(this.mSurfacePosition)) || isFoldDisplayModeChanged) {
                t.setPosition(this.mSurfaceControl, (float) this.mSurfacePosition.x, (float) this.mSurfacePosition.y);
                this.mLastSurfacePosition.set(this.mSurfacePosition.x, this.mSurfacePosition.y);
                this.mLazyModeSurfacePosition.set(this.mSurfacePosition.x, this.mSurfacePosition.y);
                if (HwFoldScreenState.isFoldScreenDevice()) {
                    this.mSubFoldModeSurfacePosition.set(this.mSurfacePosition.x, this.mSurfacePosition.y);
                }
                if (surfaceInsetsChanging() && this.mWinAnimator.hasSurface()) {
                    this.mLastSurfaceInsets.set(this.mAttrs.surfaceInsets);
                    t.deferTransactionUntil(this.mSurfaceControl, this.mWinAnimator.mSurfaceController.mSurfaceControl.getHandle(), getFrameNumber());
                }
            }
        }
    }

    public void updateSurfacePosition(int x, int y) {
        int y2;
        int x2;
        int lazyMode = this.mService.getLazyMode();
        if (!(getDisplayId() == -1 || getDisplayId() == 0)) {
            lazyMode = 0;
        }
        if (lazyMode != 0 || this.mWinAnimator.mLazyIsExiting) {
            WindowState parent = getParentWindow();
            if (parent != null) {
                x -= parent.mWinAnimator.mShownPosition.x;
                y -= parent.mWinAnimator.mShownPosition.y;
            }
            if (inMultiWindowMode() && getParentWindow() == null && getStack() != null && getTask() != null) {
                x -= getStack().mLastSurfacePosition.x + getTask().mLastSurfacePosition.x;
                y -= getStack().mLastSurfacePosition.y + getTask().mLastSurfacePosition.y;
            }
            x2 = (int) (((float) x) - (((float) this.mAttrs.surfaceInsets.left) * this.mLazyScale));
            y2 = (int) (((float) y) - (((float) this.mAttrs.surfaceInsets.top) * this.mLazyScale));
        } else {
            x2 = this.mSurfacePosition.x;
            y2 = this.mSurfacePosition.y;
        }
        if (!(this.mLazyModeSurfacePosition.x == x2 && this.mLazyModeSurfacePosition.y == y2) && !this.mSurfaceAnimator.hasLeash()) {
            getPendingTransaction().setPosition(this.mSurfaceControl, (float) x2, (float) y2);
            this.mLazyModeSurfacePosition.set(x2, y2);
        }
    }

    private void transformFrameToSurfacePosition(int left, int top, Point outPoint) {
        outPoint.set(left, top);
        WindowContainer parentWindowContainer = getParent();
        if (isChildWindow()) {
            WindowState parent = getParentWindow();
            outPoint.offset((-parent.mFrame.left) + parent.mAttrs.surfaceInsets.left, (-parent.mFrame.top) + parent.mAttrs.surfaceInsets.top);
        } else if (parentWindowContainer != null) {
            Rect parentBounds = parentWindowContainer.getBounds();
            outPoint.offset(-parentBounds.left, -parentBounds.top);
        }
        TaskStack stack = getStack();
        if (stack != null) {
            int outset = stack.getStackOutset();
            outPoint.offset(outset, outset);
        }
        outPoint.offset(-this.mAttrs.surfaceInsets.left, -this.mAttrs.surfaceInsets.top);
        if (this.mService != null && this.mService.mHwWMSEx != null) {
            this.mService.mHwWMSEx.updateSurfacePositionForPCMode(this, outPoint);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean needsRelativeLayeringToIme() {
        boolean inTokenWithAndAboveImeTarget = false;
        if (!inSplitScreenWindowingMode()) {
            return false;
        }
        if (isChildWindow()) {
            if (getParentWindow().isInputMethodTarget()) {
                return true;
            }
        } else if (this.mAppToken != null) {
            WindowState imeTarget = this.mService.mInputMethodTarget;
            if (imeTarget != null && imeTarget != this && imeTarget.mToken == this.mToken && imeTarget.compareTo(this) <= 0) {
                inTokenWithAndAboveImeTarget = true;
            }
            return inTokenWithAndAboveImeTarget;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void assignLayer(SurfaceControl.Transaction t, int layer) {
        if (needsRelativeLayeringToIme()) {
            getDisplayContent().assignRelativeLayerForImeTargetChild(t, this);
        } else {
            super.assignLayer(t, layer);
        }
    }

    public boolean isDimming() {
        return this.mIsDimming;
    }

    public void assignChildLayers(SurfaceControl.Transaction t) {
        DisplayContent dc = getDisplayContent();
        if (dc != null && toString().contains(dc.mObserveWinTitle)) {
            dc.mObserveWin = this;
        }
        int layer = 1;
        for (int i = 0; i < this.mChildren.size(); i++) {
            WindowState w = (WindowState) this.mChildren.get(i);
            if (w.mAttrs.type == 1001) {
                w.assignLayer(t, -2);
            } else if (w.mAttrs.type == 1004) {
                w.assignLayer(t, -1);
            } else {
                w.assignLayer(t, layer);
            }
            w.assignChildLayers(t);
            layer++;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateTapExcludeRegion(int regionId, int left, int top, int width, int height) {
        Task task;
        DisplayContent currentDisplay = getDisplayContent();
        if (currentDisplay != null) {
            if (this.mTapExcludeRegionHolder == null) {
                this.mTapExcludeRegionHolder = new TapExcludeRegionHolder();
                currentDisplay.mTapExcludeProvidingWindows.add(this);
            }
            this.mTapExcludeRegionHolder.updateRegion(regionId, left, top, width, height);
            if (this.mService.mFocusedApp != null && this.mService.mFocusedApp.getDisplayContent() == currentDisplay) {
                task = this.mService.mFocusedApp.getTask();
            } else {
                task = null;
            }
            currentDisplay.setTouchExcludeRegion(task);
            return;
        }
        throw new IllegalStateException("Trying to update window not attached to any display.");
    }

    /* access modifiers changed from: package-private */
    public void amendTapExcludeRegion(Region region) {
        this.mTapExcludeRegionHolder.amendRegion(region, getBounds());
    }

    public boolean isInputMethodTarget() {
        return this.mService.mInputMethodTarget == this;
    }

    /* access modifiers changed from: package-private */
    public long getFrameNumber() {
        return this.mFrameNumber;
    }

    /* access modifiers changed from: package-private */
    public void setFrameNumber(long frameNumber) {
        this.mFrameNumber = frameNumber;
    }

    public boolean canCarryColors() {
        return this.mCanCarryColors;
    }

    public void setCanCarryColors(boolean carry) {
        this.mCanCarryColors = carry;
    }

    private boolean isStatusBarWindow() {
        return this.mAttrs.type == 2000 || this.mAttrs.type == 2014 || this.mAttrs.type == 2019 || this.mAttrs.type == 2024;
    }

    /* access modifiers changed from: package-private */
    public int getLowResolutionMode() {
        String pkgName = getOwningPackage();
        if ((pkgName != null && (pkgName.contains("com.huawei.hwid") || pkgName.contains("com.huawei.gameassistant"))) || this.mAttrs.type == 2020) {
            return 2;
        }
        boolean z = false;
        if (isStatusBarWindow()) {
            if ((this.mAttrs.hwFlags & 4) != 0) {
                z = true;
            }
            boolean statusBarExpanded = z;
            if ((this.mAttrs.type == 2000 || this.mAttrs.type == 2014) && statusBarExpanded) {
                return 1;
            }
            return 2;
        }
        IApsManager apsManager = (IApsManager) LocalServices.getService(IApsManager.class);
        if (apsManager == null) {
            return 0;
        }
        float resolutionRatio = apsManager.getResolution(getOwningPackage());
        if (0.0f >= resolutionRatio || resolutionRatio >= 1.0f) {
            return 0;
        }
        return 2;
    }

    private void offsetSystemDialog(Rect contentFrame) {
        int i;
        int i2;
        if (this.mAttrs.type == 2008 && HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getDisplayId()) && this.mFrame.bottom > contentFrame.bottom) {
            if (contentFrame.bottom - this.mFrame.bottom > contentFrame.top - this.mFrame.top) {
                i = contentFrame.bottom;
                i2 = this.mFrame.bottom;
            } else {
                i = contentFrame.top;
                i2 = this.mFrame.top;
            }
            this.mFrame.offset(0, i - i2);
        }
    }

    public boolean getHwNotchSupport() {
        if (this.mAppToken != null) {
            return this.mAppToken.getHwNotchSupport();
        }
        return false;
    }

    public boolean isWindowUsingNotch() {
        boolean using;
        boolean isNotchSupport = false;
        if (this.mPolicy.isNotchDisplayDisabled()) {
            return false;
        }
        switch (this.mService.mHwWMSEx.getAppUseNotchMode(getOwningPackage())) {
            case 1:
                using = true;
                break;
            case 2:
                using = false;
                break;
            default:
                if ((getAttrs().hwFlags & 65536) != 0 || getAttrs().layoutInDisplayCutoutMode == 1 || getHwNotchSupport() || this.mService.mHwWMSEx.isInNotchAppWhitelist(this)) {
                    isNotchSupport = true;
                }
                using = isNotchSupport;
                break;
        }
        return using;
    }

    public int getHwGestureNavOptions() {
        return (this.mAppToken != null ? this.mAppToken.getHwGestureNavOptions() : 0) | getAttrs().hwFlags;
    }

    public int getLayer() {
        return this.mToken.getLayer();
    }

    public boolean isInAboveAppWindows() {
        return (this.mToken.asAppWindowToken() != null || this.mToken.windowType == 2013 || this.mToken.windowType == 2103 || this.mToken.windowType == 2011 || this.mToken.windowType == 2012) ? false : true;
    }

    private boolean isInHideCaptionList() {
        return this.mHwWSEx.isInHideCaptionList();
    }

    /* access modifiers changed from: protected */
    public boolean isInHwFreeFormWorkspace() {
        return this.mHwWSEx.isInHwFreeFormWorkspace();
    }

    private Rect adjustImePosForFreeform(Rect contentFrame, Rect containingFrame) {
        return this.mHwWSEx.adjustImePosForFreeform(contentFrame, containingFrame);
    }

    private int adjustTopForFreeform(Rect frame, Rect limitframe, int minVisibleHeight) {
        return this.mHwWSEx.adjustTopForFreeform(frame, limitframe, minVisibleHeight);
    }

    public boolean isImeWithHwFlag() {
        String packageName = getAttrs().packageName;
        if (this.mAttrs.type != 2011 || packageName == null || !packageName.contains("com.baidu.input_huawei")) {
            return false;
        }
        return true;
    }

    public void showInsetSurfaceOverlayImmediately() {
        this.mService.openSurfaceTransaction();
        try {
            synchronized (this.mWinAnimator.mInsetSurfaceLock) {
                if (this.mWinAnimator.mInsetSurfaceOverlay != null) {
                    this.mWinAnimator.mInsetSurfaceOverlay.show();
                }
            }
            this.mService.closeSurfaceTransaction("showInsetSurfaceOverlayImmediately");
        } catch (Throwable th) {
            this.mService.closeSurfaceTransaction("showInsetSurfaceOverlayImmediately");
            throw th;
        }
    }

    public void hideInsetSurfaceOverlayImmediately() {
        this.mService.openSurfaceTransaction();
        try {
            synchronized (this.mWinAnimator.mInsetSurfaceLock) {
                if (this.mWinAnimator.mInsetSurfaceOverlay != null) {
                    this.mWinAnimator.mInsetSurfaceOverlay.hide();
                }
            }
            this.mService.closeSurfaceTransaction("destroyInsetSurfaceOverlayImmediately");
        } catch (Throwable th) {
            this.mService.closeSurfaceTransaction("destroyInsetSurfaceOverlayImmediately");
            throw th;
        }
    }

    public void updateSurfacePositionBySubFoldMode(int x, int y) {
        if (HwFoldScreenState.isFoldScreenDevice()) {
            WindowState parent = getParentWindow();
            if (parent != null) {
                x -= parent.mWinAnimator.mShownPosition.x;
                y -= parent.mWinAnimator.mShownPosition.y;
            } else if (!(getStack() == null || getTask() == null || this.mAppToken == null)) {
                x -= (getStack().mLastSurfacePosition.x + getTask().mLastSurfacePosition.x) + this.mAppToken.mLastSurfacePosition.x;
                y -= (getStack().mLastSurfacePosition.y + getTask().mLastSurfacePosition.y) + this.mAppToken.mLastSurfacePosition.y;
            }
            int x2 = (int) (((float) x) - (((float) this.mAttrs.surfaceInsets.left) * this.mService.mSubFoldModeScale));
            int y2 = (int) (((float) y) - (((float) this.mAttrs.surfaceInsets.top) * this.mService.mSubFoldModeScale));
            if (!(this.mSubFoldModeSurfacePosition.x == x2 && this.mSubFoldModeSurfacePosition.y == y2) && !this.mSurfaceAnimator.hasLeash()) {
                getPendingTransaction().setPosition(this.mSurfaceControl, (float) x2, (float) y2);
                this.mSubFoldModeSurfacePosition.set(x2, y2);
            }
        }
    }
}
