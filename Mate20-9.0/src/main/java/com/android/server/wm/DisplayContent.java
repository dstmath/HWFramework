package com.android.server.wm;

import android.common.HwFrameworkFactory;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormUtils;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.display.DisplayManagerInternal;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.DisplayMetrics;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.InputDevice;
import android.view.MagnificationSpec;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.view.IInputMethodClient;
import com.android.server.BatteryService;
import com.android.server.HwServiceFactory;
import com.android.server.job.controllers.JobStatus;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.WindowContainer;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.utils.CoordinateTransforms;
import com.android.server.wm.utils.RotationCache;
import com.android.server.wm.utils.WmDisplayCutout;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DisplayContent extends AbsDisplayContent {
    private static final boolean IS_DEBUG_VERSION = (SystemProperties.getInt("ro.logsystem.usertype", 1) == 3);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    private static final int PAD_DISPLAY_ID = 100000;
    private static final String TAG = "WindowManager";
    private static final String TAG_VISIBILITY = "WindowManager_visibility";
    private static final String TAG_VR = "VRService";
    private static final String UNIQUE_ID_PREFIX_LOCAL = "local:";
    private static final boolean mIsFoldable;
    final int[] index = new int[1];
    boolean isDefaultDisplay;
    protected final AboveAppWindowContainers mAboveAppWindowsContainers = new AboveAppWindowContainers("mAboveAppWindowsContainers", this.mService);
    private boolean mAltOrientation = false;
    private final Consumer<WindowState> mApplyPostLayoutPolicy = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.this.mService.mPolicy.applyPostLayoutPolicyLw((WindowState) obj, ((WindowState) obj).mAttrs, ((WindowState) obj).getParentWindow(), DisplayContent.this.mService.mInputMethodTarget);
        }
    };
    private final Consumer<WindowState> mApplySurfaceChangesTransaction = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.lambda$new$8(DisplayContent.this, (WindowState) obj);
        }
    };
    int mBaseDisplayDensity = 0;
    int mBaseDisplayHeight = 0;
    private Rect mBaseDisplayRect = new Rect();
    int mBaseDisplayWidth = 0;
    private final NonAppWindowContainers mBelowAppWindowsContainers = new NonAppWindowContainers("mBelowAppWindowsContainers", this.mService);
    private final DisplayMetrics mCompatDisplayMetrics = new DisplayMetrics();
    float mCompatibleScreenScale;
    private final Predicate<WindowState> mComputeImeTargetPredicate = new Predicate() {
        public final boolean test(Object obj) {
            return DisplayContent.lambda$new$6(DisplayContent.this, (WindowState) obj);
        }
    };
    private int mDeferUpdateImeTargetCount;
    private boolean mDeferredRemoval;
    protected final Display mDisplay;
    private final RotationCache<DisplayCutout, WmDisplayCutout> mDisplayCutoutCache = new RotationCache<>(new RotationCache.RotationDependentComputation() {
        public final Object compute(Object obj, int i) {
            return DisplayContent.this.calculateDisplayCutoutForRotationUncached((DisplayCutout) obj, i);
        }
    });
    DisplayFrames mDisplayFrames;
    protected int mDisplayId = -1;
    protected final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private boolean mDisplayReady = false;
    boolean mDisplayScalingDisabled;
    final DockedStackDividerController mDividerControllerLocked;
    final ArrayList<WindowToken> mExitingTokens = new ArrayList<>();
    private final ToBooleanFunction<WindowState> mFindFocusedWindow = new ToBooleanFunction() {
        public final boolean apply(Object obj) {
            return DisplayContent.lambda$new$3(DisplayContent.this, (WindowState) obj);
        }
    };
    float mForceCompatibleScreenScale;
    private boolean mHaveApp = false;
    private boolean mHaveBootMsg = false;
    private boolean mHaveKeyguard = true;
    private boolean mHaveWallpaper = false;
    private final NonMagnifiableWindowContainers mImeWindowsContainers = new NonMagnifiableWindowContainers("mImeWindowsContainers", this.mService);
    DisplayCutout mInitialDisplayCutout;
    int mInitialDisplayDensity = 0;
    int mInitialDisplayHeight = 0;
    int mInitialDisplayWidth = 0;
    private boolean mLastHasContent;
    /* access modifiers changed from: private */
    public int mLastKeyguardForcedOrientation = -1;
    /* access modifiers changed from: private */
    public int mLastOrientation = -1;
    private boolean mLastWallpaperVisible = false;
    /* access modifiers changed from: private */
    public int mLastWindowForcedOrientation = -1;
    private boolean mLayoutNeeded;
    int mLayoutSeq = 0;
    private MagnificationSpec mMagnificationSpec;
    private int mMaxUiWidth;
    WindowToken mObserveToken = null;
    WindowState mObserveWin = null;
    String mObserveWinTitle = "FingerprintDialogView";
    private SurfaceControl mOverlayLayer;
    private final Region mPcTouchExcludeRegion = new Region();
    private final Consumer<WindowState> mPerformLayout = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.lambda$new$4(DisplayContent.this, (WindowState) obj);
        }
    };
    private final Consumer<WindowState> mPerformLayoutAttached = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.lambda$new$5(DisplayContent.this, (WindowState) obj);
        }
    };
    final PinnedStackController mPinnedStackControllerLocked;
    private boolean mQuickBoot = true;
    final DisplayMetrics mRealDisplayMetrics = new DisplayMetrics();
    private boolean mRemovingDisplay = false;
    private int mRotation = (SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90);
    private final Consumer<WindowState> mScheduleToastTimeout = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.lambda$new$2(DisplayContent.this, (WindowState) obj);
        }
    };
    private final SurfaceSession mSession = new SurfaceSession();
    boolean mShouldOverrideDisplayConfiguration = true;
    private int mSurfaceSize;
    TaskTapPointerEventListener mTapDetector;
    final ArraySet<WindowState> mTapExcludeProvidingWindows = new ArraySet<>();
    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList<>();
    protected final TaskStackContainers mTaskStackContainers = new TaskStackContainers(this.mService);
    private final ApplySurfaceChangesTransactionState mTmpApplySurfaceChangesTransactionState = new ApplySurfaceChangesTransactionState();
    private final Rect mTmpBounds = new Rect();
    private final DisplayMetrics mTmpDisplayMetrics = new DisplayMetrics();
    private final float[] mTmpFloats = new float[9];
    private boolean mTmpInitial;
    private final Matrix mTmpMatrix = new Matrix();
    private boolean mTmpRecoveringMemory;
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final RectF mTmpRectF = new RectF();
    private final Region mTmpRegion = new Region();
    private final TaskForResizePointSearchResult mTmpTaskForResizePointSearchResult = new TaskForResizePointSearchResult();
    private final LinkedList<AppWindowToken> mTmpUpdateAllDrawn = new LinkedList<>();
    private WindowState mTmpWindow;
    private WindowState mTmpWindow2;
    WindowAnimator mTmpWindowAnimator;
    private final HashMap<IBinder, WindowToken> mTokenMap = new HashMap<>();
    WindowToken mTopAboveAppToken = null;
    private Region mTouchExcludeRegion = new Region();
    private boolean mUpdateImeTarget;
    private final Consumer<WindowState> mUpdateWallpaperForAnimator = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.lambda$new$1(DisplayContent.this, (WindowState) obj);
        }
    };
    private final Consumer<WindowState> mUpdateWindowsForAnimator = new Consumer() {
        public final void accept(Object obj) {
            DisplayContent.lambda$new$0(DisplayContent.this, (WindowState) obj);
        }
    };
    private IVRSystemServiceManager mVrMananger;
    WallpaperController mWallpaperController;
    boolean mWinEverCovered = false;
    private SurfaceControl mWindowingLayer;
    int pendingLayoutChanges;

    protected final class AboveAppWindowContainers extends NonAppWindowContainers {
        AboveAppWindowContainers(String name, WindowManagerService service) {
            super(name, service);
        }

        /* access modifiers changed from: package-private */
        public void assignChildLayers(SurfaceControl.Transaction t) {
            assignChildLayers(t, null);
        }

        /* access modifiers changed from: package-private */
        public void assignChildLayers(SurfaceControl.Transaction t, WindowContainer imeContainer) {
            boolean needAssignIme = (imeContainer == null || imeContainer.getSurfaceControl() == null) ? false : true;
            for (int j = 0; j < this.mChildren.size(); j++) {
                WindowToken wt = (WindowToken) this.mChildren.get(j);
                if (wt.windowType == 2034) {
                    wt.assignRelativeLayer(t, DisplayContent.this.mTaskStackContainers.getSplitScreenDividerAnchor(), 1);
                } else {
                    wt.assignLayer(t, j);
                    wt.assignChildLayers(t);
                    int layer = this.mService.mPolicy.getWindowLayerFromTypeLw(wt.windowType, wt.mOwnerCanManageAppTokens);
                    if (needAssignIme && layer >= this.mService.mPolicy.getWindowLayerFromTypeLw(2012, true)) {
                        imeContainer.assignRelativeLayer(t, wt.getSurfaceControl(), -1);
                        needAssignIme = false;
                    }
                }
            }
            if (needAssignIme) {
                imeContainer.assignRelativeLayer(t, getSurfaceControl(), HwBootFail.STAGE_BOOT_SUCCESS);
            }
            DisplayContent.this.checkNeedNotifyFingerWinCovered();
            DisplayContent.this.mObserveToken = null;
            DisplayContent.this.mTopAboveAppToken = null;
        }
    }

    private static final class ApplySurfaceChangesTransactionState {
        boolean displayHasContent;
        boolean focusDisplayed;
        boolean obscured;
        int preferredModeId;
        float preferredRefreshRate;
        boolean syswin;

        private ApplySurfaceChangesTransactionState() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.displayHasContent = false;
            this.obscured = false;
            this.syswin = false;
            this.focusDisplayed = false;
            this.preferredRefreshRate = 0.0f;
            this.preferredModeId = 0;
        }
    }

    static class DisplayChildWindowContainer<E extends WindowContainer> extends WindowContainer<E> {
        DisplayChildWindowContainer(WindowManagerService service) {
            super(service);
        }

        /* access modifiers changed from: package-private */
        public boolean fillsParent() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean isVisible() {
            return true;
        }
    }

    protected class NonAppWindowContainers extends DisplayChildWindowContainer<WindowToken> {
        private final Dimmer mDimmer = new Dimmer(this);
        private final Predicate<WindowState> mGetOrientingWindow = $$Lambda$DisplayContent$NonAppWindowContainers$FI_O7m2qEDfIRZef3D32AxGrcs.INSTANCE;
        private final String mName;
        private final Rect mTmpDimBoundsRect = new Rect();
        private final Comparator<WindowToken> mWindowComparator = new Comparator() {
            public final int compare(Object obj, Object obj2) {
                return DisplayContent.NonAppWindowContainers.lambda$new$0(DisplayContent.NonAppWindowContainers.this, (WindowToken) obj, (WindowToken) obj2);
            }
        };

        public static /* synthetic */ int lambda$new$0(NonAppWindowContainers nonAppWindowContainers, WindowToken token1, WindowToken token2) {
            return nonAppWindowContainers.mService.mPolicy.getWindowLayerFromTypeLw(token1.windowType, token1.mOwnerCanManageAppTokens) < nonAppWindowContainers.mService.mPolicy.getWindowLayerFromTypeLw(token2.windowType, token2.mOwnerCanManageAppTokens) ? -1 : 1;
        }

        static /* synthetic */ boolean lambda$new$1(WindowState w) {
            if (!w.isVisibleLw() || !w.mPolicyVisibilityAfterAnim) {
                return false;
            }
            int req = w.mAttrs.screenOrientation;
            if (req == -1 || req == 3 || req == -2) {
                return false;
            }
            return true;
        }

        NonAppWindowContainers(String name, WindowManagerService service) {
            super(service);
            this.mName = name;
        }

        /* access modifiers changed from: package-private */
        public void addChild(WindowToken token) {
            addChild(token, this.mWindowComparator);
        }

        /* access modifiers changed from: package-private */
        public int getOrientation() {
            WindowManagerPolicy policy = this.mService.mPolicy;
            WindowState win = getWindow(this.mGetOrientingWindow);
            if (win != null) {
                int req = win.mAttrs.screenOrientation;
                if (policy.isKeyguardHostWindow(win.mAttrs)) {
                    int unused = DisplayContent.this.mLastKeyguardForcedOrientation = req;
                    if (this.mService.mKeyguardGoingAway) {
                        int unused2 = DisplayContent.this.mLastWindowForcedOrientation = -1;
                        return -2;
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, win + " forcing orientation to " + req + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return DisplayContent.this.mLastWindowForcedOrientation = req;
            }
            int unused3 = DisplayContent.this.mLastWindowForcedOrientation = -1;
            boolean isUnoccluding = this.mService.mAppTransition.getAppTransition() == 23 && this.mService.mUnknownAppVisibilityController.allResolved();
            if (policy.isKeyguardShowingAndNotOccluded() || isUnoccluding) {
                return DisplayContent.this.mLastKeyguardForcedOrientation;
            }
            return -2;
        }

        /* access modifiers changed from: package-private */
        public String getName() {
            return this.mName;
        }

        /* access modifiers changed from: package-private */
        public Dimmer getDimmer() {
            return this.mDimmer;
        }

        /* access modifiers changed from: package-private */
        public void prepareSurfaces() {
            this.mDimmer.resetDimStates();
            super.prepareSurfaces();
            getBounds(this.mTmpDimBoundsRect);
            if (this.mDimmer.updateDims(getPendingTransaction(), this.mTmpDimBoundsRect)) {
                scheduleAnimation();
            }
        }
    }

    private class NonMagnifiableWindowContainers extends NonAppWindowContainers {
        NonMagnifiableWindowContainers(String name, WindowManagerService service) {
            super(name, service);
        }

        /* access modifiers changed from: package-private */
        public void applyMagnificationSpec(SurfaceControl.Transaction t, MagnificationSpec spec) {
        }
    }

    static final class ScreenshotApplicationState {
        WindowState appWin;
        int maxLayer;
        int minLayer;
        boolean screenshotReady;

        ScreenshotApplicationState() {
        }

        /* access modifiers changed from: package-private */
        public void reset(boolean screenshotReady2) {
            this.appWin = null;
            int i = 0;
            this.maxLayer = 0;
            this.minLayer = 0;
            this.screenshotReady = screenshotReady2;
            if (!screenshotReady2) {
                i = HwBootFail.STAGE_BOOT_SUCCESS;
            }
            this.minLayer = i;
        }
    }

    static final class TaskForResizePointSearchResult {
        boolean searchDone;
        Task taskForResize;

        TaskForResizePointSearchResult() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.searchDone = false;
            this.taskForResize = null;
        }
    }

    protected final class TaskStackContainers extends DisplayChildWindowContainer<TaskStack> {
        SurfaceControl mAppAnimationLayer = null;
        SurfaceControl mBoostedAppAnimationLayer = null;
        private TaskStack mCoordinationPrimaryStack = null;
        SurfaceControl mHomeAppAnimationLayer = null;
        private TaskStack mHomeStack = null;
        private TaskStack mPinnedStack = null;
        SurfaceControl mSplitScreenDividerAnchor = null;
        private TaskStack mSplitScreenPrimaryStack = null;

        TaskStackContainers(WindowManagerService service) {
            super(service);
        }

        /* access modifiers changed from: package-private */
        public TaskStack getStack(int windowingMode, int activityType) {
            if (activityType == 2) {
                return this.mHomeStack;
            }
            if (windowingMode == 2) {
                return this.mPinnedStack;
            }
            if (windowingMode == 3) {
                return this.mSplitScreenPrimaryStack;
            }
            if (windowingMode == 11) {
                return this.mCoordinationPrimaryStack;
            }
            for (int i = DisplayContent.this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(i);
                if (activityType == 0 && stack != null && windowingMode == stack.getWindowingMode()) {
                    return stack;
                }
                if (stack != null && stack.isCompatible(windowingMode, activityType)) {
                    return stack;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public TaskStack getTopStack() {
            if (DisplayContent.this.mTaskStackContainers.getChildCount() > 0) {
                return (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(DisplayContent.this.mTaskStackContainers.getChildCount() - 1);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getHomeStack() {
            if (this.mHomeStack == null && DisplayContent.this.mDisplayId == 0) {
                Slog.e(DisplayContent.TAG, "getHomeStack: Returning null from this=" + this);
            }
            return this.mHomeStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getPinnedStack() {
            return this.mPinnedStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getSplitScreenPrimaryStack() {
            return this.mSplitScreenPrimaryStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getCoordinationPrimaryStack() {
            return this.mCoordinationPrimaryStack;
        }

        /* access modifiers changed from: package-private */
        public ArrayList<Task> getVisibleTasks() {
            ArrayList<Task> visibleTasks = new ArrayList<>();
            forAllTasks(new Consumer(visibleTasks) {
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    DisplayContent.TaskStackContainers.lambda$getVisibleTasks$0(this.f$0, (Task) obj);
                }
            });
            return visibleTasks;
        }

        static /* synthetic */ void lambda$getVisibleTasks$0(ArrayList visibleTasks, Task task) {
            if (task.isVisible()) {
                visibleTasks.add(task);
            }
        }

        /* access modifiers changed from: package-private */
        public void addStackToDisplay(TaskStack stack, boolean onTop) {
            addStackReferenceIfNeeded(stack);
            addChild(stack, onTop);
            stack.onDisplayChanged(DisplayContent.this);
        }

        /* access modifiers changed from: package-private */
        public void onStackWindowingModeChanged(TaskStack stack) {
            removeStackReferenceIfNeeded(stack);
            addStackReferenceIfNeeded(stack);
            if (stack == this.mPinnedStack && getTopStack() != stack) {
                positionChildAt((int) HwBootFail.STAGE_BOOT_SUCCESS, stack, false);
            }
        }

        private void addStackReferenceIfNeeded(TaskStack stack) {
            if (stack.isActivityTypeHome()) {
                if (this.mHomeStack == null) {
                    this.mHomeStack = stack;
                } else {
                    throw new IllegalArgumentException("addStackReferenceIfNeeded: home stack=" + this.mHomeStack + " already exist on display=" + this + " stack=" + stack);
                }
            }
            int windowingMode = stack.getWindowingMode();
            if (windowingMode == 2) {
                if (this.mPinnedStack == null) {
                    this.mPinnedStack = stack;
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: pinned stack=" + this.mPinnedStack + " already exist on display=" + this + " stack=" + stack);
            } else if (windowingMode == 3) {
                if (this.mSplitScreenPrimaryStack == null) {
                    this.mSplitScreenPrimaryStack = stack;
                    DisplayContent.this.mDividerControllerLocked.notifyDockedStackExistsChanged(true);
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: split-screen-primary stack=" + this.mSplitScreenPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            } else if (windowingMode != 11) {
            } else {
                if (this.mCoordinationPrimaryStack == null) {
                    this.mCoordinationPrimaryStack = stack;
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: coordination_primary stack=" + this.mCoordinationPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            }
        }

        private void removeStackReferenceIfNeeded(TaskStack stack) {
            if (stack == this.mHomeStack) {
                this.mHomeStack = null;
            } else if (stack == this.mPinnedStack) {
                this.mPinnedStack = null;
            } else if (stack == this.mSplitScreenPrimaryStack) {
                this.mSplitScreenPrimaryStack = null;
                this.mService.setDockedStackCreateStateLocked(0, null);
                DisplayContent.this.mDividerControllerLocked.notifyDockedStackExistsChanged(false);
            } else if (stack == this.mCoordinationPrimaryStack) {
                this.mCoordinationPrimaryStack = null;
                CoordinationModeUtils.getInstance(this.mService.mContext).setCoordinationCreateMode(0);
            }
        }

        private void addChild(TaskStack stack, boolean toTop) {
            addChild(stack, findPositionForStack(toTop ? this.mChildren.size() : 0, stack, true));
            DisplayContent.this.setLayoutNeeded();
        }

        /* access modifiers changed from: protected */
        public void removeChild(TaskStack stack) {
            super.removeChild(stack);
            removeStackReferenceIfNeeded(stack);
        }

        /* access modifiers changed from: package-private */
        public boolean isOnTop() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public void positionChildAt(int position, TaskStack child, boolean includingParents) {
            if (!child.getWindowConfiguration().isAlwaysOnTop() || position == Integer.MAX_VALUE) {
                super.positionChildAt(findPositionForStack(position, child, false), child, includingParents);
                DisplayContent.this.setLayoutNeeded();
                return;
            }
            Slog.w(DisplayContent.TAG, "Ignoring move of always-on-top stack=" + this + " to bottom");
            super.positionChildAt(this.mChildren.indexOf(child), child, false);
        }

        private int findPositionForStack(int requestedPosition, TaskStack stack, boolean adding) {
            boolean z = true;
            int topChildPosition = this.mChildren.size() - 1;
            boolean toTop = requestedPosition == Integer.MAX_VALUE;
            if (!adding ? requestedPosition < topChildPosition : requestedPosition < topChildPosition + 1) {
                z = false;
            }
            int targetPosition = requestedPosition;
            if ((!z && !toTop) || stack.getWindowingMode() == 2 || !DisplayContent.this.hasPinnedStack()) {
                return targetPosition;
            }
            if (((TaskStack) this.mChildren.get(topChildPosition)).getWindowingMode() == 2) {
                return adding ? topChildPosition : topChildPosition - 1;
            }
            throw new IllegalStateException("Pinned stack isn't top stack??? " + this.mChildren);
        }

        /* access modifiers changed from: package-private */
        public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                if (super.forAllWindows(callback, traverseTopToBottom) || forAllExitingAppTokenWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            } else if (forAllExitingAppTokenWindows(callback, traverseTopToBottom) || super.forAllWindows(callback, traverseTopToBottom)) {
                return true;
            }
            return false;
        }

        private boolean forAllExitingAppTokenWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                    AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                    for (int j = appTokens.size() - 1; j >= 0; j--) {
                        if (((AppWindowToken) appTokens.get(j)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                            return true;
                        }
                    }
                }
            } else {
                int count = this.mChildren.size();
                for (int i2 = 0; i2 < count; i2++) {
                    AppTokenList appTokens2 = ((TaskStack) this.mChildren.get(i2)).mExitingAppTokens;
                    int appTokensCount = appTokens2.size();
                    for (int j2 = 0; j2 < appTokensCount; j2++) {
                        if (((AppWindowToken) appTokens2.get(j2)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setExitingTokensHasVisible(boolean hasVisible) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    ((AppWindowToken) appTokens.get(j)).hasVisible = hasVisible;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeExistingAppTokensIfPossible() {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    AppWindowToken token = (AppWindowToken) appTokens.get(j);
                    if (!token.hasVisible && !this.mService.mClosingApps.contains(token) && (!token.mIsExiting || token.isEmpty())) {
                        cancelAnimation();
                        Slog.v(DisplayContent.TAG, "performLayout: App token exiting now removed" + token);
                        token.removeIfPossible();
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        public int getOrientation() {
            if (DisplayContent.this.isStackVisible(3) || (!HwFreeFormUtils.isFreeFormEnable() && DisplayContent.this.isStackVisible(5))) {
                if (this.mHomeStack != null && this.mHomeStack.isVisible() && DisplayContent.this.mDividerControllerLocked.isMinimizedDock() && (!DisplayContent.this.mDividerControllerLocked.isHomeStackResizable() || !this.mHomeStack.matchParentBounds())) {
                    int orientation = this.mHomeStack.getOrientation();
                    if (orientation != -2) {
                        return orientation;
                    }
                }
                return -1;
            }
            int orientation2 = super.getOrientation();
            if (this.mService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive")) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, "Forcing UNSPECIFIED orientation in car for display id=" + DisplayContent.this.mDisplayId + ". Ignoring " + orientation2);
                }
                return -1;
            } else if (orientation2 == -2 || orientation2 == 3) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, "No app is requesting an orientation, return " + DisplayContent.this.mLastOrientation + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return DisplayContent.this.mLastOrientation;
            } else {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v(DisplayContent.TAG, "App is requesting an orientation, return " + orientation2 + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return orientation2;
            }
        }

        /* access modifiers changed from: package-private */
        public void assignChildLayers(SurfaceControl.Transaction t) {
            assignStackOrdering(t);
            for (int i = 0; i < this.mChildren.size(); i++) {
                ((TaskStack) this.mChildren.get(i)).assignChildLayers(t);
            }
        }

        /* access modifiers changed from: package-private */
        public void assignStackOrdering(SurfaceControl.Transaction t) {
            int layerForAnimationLayer = 0;
            int layerForAnimationLayer2 = 0;
            int layerForHomeAnimationLayer = 0;
            int layer = 0;
            int state = 0;
            while (state <= 2) {
                int layerForBoostedAnimationLayer = layerForAnimationLayer2;
                int layerForAnimationLayer3 = layerForAnimationLayer;
                for (int i = 0; i < this.mChildren.size(); i++) {
                    TaskStack s = (TaskStack) this.mChildren.get(i);
                    if ((state != 0 || s.isActivityTypeHome()) && ((state != 1 || (!s.isActivityTypeHome() && (!s.isAlwaysOnTop() || !HwFreeFormUtils.isFreeFormEnable() || s.inFreeformWindowingMode()))) && (state != 2 || s.isAlwaysOnTop()))) {
                        int layer2 = layer + 1;
                        s.assignLayer(t, layer);
                        if (!(s.inSplitScreenWindowingMode() == 0 || this.mSplitScreenDividerAnchor == null)) {
                            t.setLayer(this.mSplitScreenDividerAnchor, layer2);
                            layer2++;
                        }
                        if ((s.isTaskAnimating() || s.isAppAnimating()) && state != 2) {
                            layerForAnimationLayer3 = layer2;
                            layer2++;
                        }
                        if (state != 2) {
                            layer = layer2 + 1;
                            layerForBoostedAnimationLayer = layer2;
                        } else {
                            layer = layer2;
                        }
                    }
                }
                if (state == 0) {
                    layerForHomeAnimationLayer = layer;
                    layer++;
                }
                state++;
                layerForAnimationLayer = layerForAnimationLayer3;
                layerForAnimationLayer2 = layerForBoostedAnimationLayer;
            }
            if (this.mAppAnimationLayer != null) {
                t.setLayer(this.mAppAnimationLayer, layerForAnimationLayer);
            }
            if (this.mBoostedAppAnimationLayer != null) {
                t.setLayer(this.mBoostedAppAnimationLayer, layerForAnimationLayer2);
            }
            if (this.mHomeAppAnimationLayer != null) {
                t.setLayer(this.mHomeAppAnimationLayer, layerForHomeAnimationLayer);
            }
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl getAppAnimationLayer(@WindowContainer.AnimationLayer int animationLayer) {
            switch (animationLayer) {
                case 1:
                    return this.mBoostedAppAnimationLayer;
                case 2:
                    return this.mHomeAppAnimationLayer;
                default:
                    return this.mAppAnimationLayer;
            }
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl getSplitScreenDividerAnchor() {
            return this.mSplitScreenDividerAnchor;
        }

        /* access modifiers changed from: package-private */
        public void onParentSet() {
            super.onParentSet();
            if (getParent() != null) {
                this.mAppAnimationLayer = makeChildSurface(null).setName("animationLayer").build();
                this.mBoostedAppAnimationLayer = makeChildSurface(null).setName("boostedAnimationLayer").build();
                this.mHomeAppAnimationLayer = makeChildSurface(null).setName("homeAnimationLayer").build();
                this.mSplitScreenDividerAnchor = makeChildSurface(null).setName("splitScreenDividerAnchor").build();
                getPendingTransaction().show(this.mAppAnimationLayer).show(this.mBoostedAppAnimationLayer).show(this.mHomeAppAnimationLayer).show(this.mSplitScreenDividerAnchor);
                scheduleAnimation();
                return;
            }
            this.mAppAnimationLayer.destroy();
            this.mAppAnimationLayer = null;
            this.mBoostedAppAnimationLayer.destroy();
            this.mBoostedAppAnimationLayer = null;
            this.mHomeAppAnimationLayer.destroy();
            this.mHomeAppAnimationLayer = null;
            this.mSplitScreenDividerAnchor.destroy();
            this.mSplitScreenDividerAnchor = null;
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.get("ro.config.hw_fold_disp").isEmpty() && SystemProperties.get("persist.sys.fold.disp.size").isEmpty()) {
            z = false;
        }
        mIsFoldable = z;
    }

    /* access modifiers changed from: package-private */
    public boolean isCoverOpen() {
        return this.mService.mHwWMSEx.isCoverOpen();
    }

    public static /* synthetic */ void lambda$new$0(DisplayContent displayContent, WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        AppWindowToken atoken = w.mAppToken;
        if (winAnimator.mDrawState != 3) {
            return;
        }
        if ((atoken == null || atoken.allDrawn) && w.performShowLocked()) {
            displayContent.pendingLayoutChanges |= 8;
        }
    }

    public static /* synthetic */ void lambda$new$1(DisplayContent displayContent, WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (winAnimator.mSurfaceController != null && winAnimator.hasSurface()) {
            int flags = w.mAttrs.flags;
            if (winAnimator.isAnimationSet()) {
                AnimationAdapter anim = w.getAnimation();
                if (anim != null) {
                    if ((flags & DumpState.DUMP_DEXOPT) != 0 && anim.getDetachWallpaper()) {
                        displayContent.mTmpWindow = w;
                    }
                    int color = anim.getBackgroundColor();
                    if (color != 0) {
                        TaskStack stack = w.getStack();
                        if (stack != null) {
                            stack.setAnimationBackground(winAnimator, color);
                        }
                    }
                }
            }
            AppWindowToken atoken = winAnimator.mWin.mAppToken;
            AnimationAdapter animation = atoken != null ? atoken.getAnimation() : null;
            if (animation != null) {
                if ((1048576 & flags) != 0 && animation.getDetachWallpaper()) {
                    displayContent.mTmpWindow = w;
                }
                int color2 = animation.getBackgroundColor();
                if (color2 != 0) {
                    TaskStack stack2 = w.getStack();
                    if (stack2 != null) {
                        stack2.setAnimationBackground(winAnimator, color2);
                    }
                }
            }
        }
    }

    public static /* synthetic */ void lambda$new$2(DisplayContent displayContent, WindowState w) {
        int lostFocusUid = displayContent.mTmpWindow.mOwnerUid;
        Handler handler = displayContent.mService.mH;
        if (w.mAttrs.type == 2005 && w.mOwnerUid == lostFocusUid && !handler.hasMessages(52, w)) {
            handler.sendMessageDelayed(handler.obtainMessage(52, w), w.mAttrs.hideTimeoutMilliseconds);
        }
    }

    public static /* synthetic */ boolean lambda$new$3(DisplayContent displayContent, WindowState w) {
        AppWindowToken focusedApp = displayContent.mService.mFocusedApp;
        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v(TAG, "Looking for focus: " + w + ", flags=" + w.mAttrs.flags + ", canReceive=" + w.canReceiveKeys());
        }
        if (!w.canReceiveKeys()) {
            return false;
        }
        if (HwPCUtils.isPcCastModeInServer() && !displayContent.isDefaultDisplay) {
            if (displayContent.mService.getPCLauncherFocused() && w.mAttrs != null && w.mAttrs.type != 2103 && w.mAppToken != null) {
                return false;
            }
            if (HwPCUtils.enabledInPad() && !displayContent.mService.mPolicy.isKeyguardLocked() && w.mAttrs != null && w.mAttrs.type == 2000) {
                HwPCUtils.log(TAG, "Skipping " + w + ", flags=" + w.mAttrs.flags + ", canReceive=" + w.canReceiveKeys());
                return false;
            }
        }
        AppWindowToken wtoken = w.mAppToken;
        if (wtoken != null && (wtoken.removed || wtoken.sendingToBottom)) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                StringBuilder sb = new StringBuilder();
                sb.append("Skipping ");
                sb.append(wtoken);
                sb.append(" because ");
                sb.append(wtoken.removed ? "removed" : "sendingToBottom");
                Slog.v(TAG, sb.toString());
            }
            return false;
        } else if (focusedApp == null) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: focusedApp=null using new focus @ " + w);
            }
            displayContent.mTmpWindow = w;
            return true;
        } else if (!focusedApp.windowsAreFocusable()) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: focusedApp windows not focusable using new focus @ " + w);
            }
            displayContent.mTmpWindow = w;
            return true;
        } else if (wtoken == null || w.mAttrs.type == 3 || focusedApp.compareTo((WindowContainer) wtoken) <= 0 || (HwPCUtils.isPcCastModeInServer() && focusedApp.getDisplayContent() != displayContent)) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: Found new focus @ " + w);
            }
            displayContent.mTmpWindow = w;
            return true;
        } else {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "findFocusedWindow: " + wtoken + " below Reached focused app=" + focusedApp);
            }
            displayContent.mTmpWindow = null;
            return true;
        }
    }

    public static /* synthetic */ void lambda$new$4(DisplayContent displayContent, WindowState w) {
        boolean isCoverOpen;
        if (IS_TABLET) {
            isCoverOpen = displayContent.isCoverOpen() || displayContent.mService.mPowerManager.isScreenOn();
        } else {
            isCoverOpen = displayContent.isCoverOpen();
        }
        if (displayContent.mTmpInitial && IS_DEBUG_VERSION) {
            ArrayMap<String, Object> params = new ArrayMap<>();
            params.put("checkType", "HighWindowLayerScene");
            params.put("number", Integer.valueOf(displayContent.index[0]));
            params.put("windowState", w);
            if (HwServiceFactory.getWinFreezeScreenMonitor() != null) {
                HwServiceFactory.getWinFreezeScreenMonitor().checkFreezeScreen(params);
            }
            displayContent.index[0] = displayContent.index[0] + 1;
        }
        boolean ggGone = (displayContent.mTmpWindow != null && displayContent.mService.mPolicy.canBeHiddenByKeyguardLw(w)) || w.isGoneForLayoutLw();
        boolean gone = ((!ggGone && isCoverOpen) || w.mAttrs.type == 2000 || w.mAttrs.type == 2101 || w.mAttrs.type == 2100 || (w.mIsWallpaper && w.mReportOrientationChanged)) ? false : true;
        if (WindowManagerDebugConfig.DEBUG_LAYOUT && !w.mLayoutAttached) {
            Slog.v(TAG, "1ST PASS " + w + ": gone=" + gone + "<-" + ggGone + " mHaveFrame=" + w.mHaveFrame + " mLayoutAttached=" + w.mLayoutAttached + " screen changed=" + w.isConfigChanged());
            AppWindowToken atoken = w.mAppToken;
            if (gone) {
                StringBuilder sb = new StringBuilder();
                sb.append("  GONE: mViewVisibility=");
                sb.append(w.mViewVisibility);
                sb.append(" mRelayoutCalled=");
                sb.append(w.mRelayoutCalled);
                sb.append(" hidden=");
                sb.append(w.mToken.isHidden());
                sb.append(" hiddenRequested=");
                sb.append(atoken != null && atoken.hiddenRequested);
                sb.append(" parentHidden=");
                sb.append(w.isParentWindowHidden());
                Slog.v(TAG, sb.toString());
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  VIS: mViewVisibility=");
                sb2.append(w.mViewVisibility);
                sb2.append(" mRelayoutCalled=");
                sb2.append(w.mRelayoutCalled);
                sb2.append(" hidden=");
                sb2.append(w.mToken.isHidden());
                sb2.append(" hiddenRequested=");
                sb2.append(atoken != null && atoken.hiddenRequested);
                sb2.append(" parentHidden=");
                sb2.append(w.isParentWindowHidden());
                Slog.v(TAG, sb2.toString());
            }
        }
        if (gone && w.mHaveFrame && !w.mLayoutNeeded) {
            if ((!w.isConfigChanged() && !w.setReportResizeHints()) || w.isGoneForLayoutLw()) {
                return;
            }
            if ((w.mAttrs.privateFlags & 1024) == 0 && (!w.mHasSurface || w.mAppToken == null || !w.mAppToken.layoutConfigChanges)) {
                return;
            }
        }
        if (!w.mLayoutAttached) {
            if (displayContent.mTmpInitial) {
                w.mContentChanged = false;
            }
            if (w.mAttrs.type == 2023) {
                displayContent.mTmpWindow = w;
            }
            w.mLayoutNeeded = false;
            w.prelayout();
            boolean firstLayout = true ^ w.isLaidOut();
            displayContent.mService.mPolicy.layoutWindowLw(w, null, displayContent.mDisplayFrames);
            w.mLayoutSeq = displayContent.mLayoutSeq;
            if (firstLayout) {
                w.updateLastInsetValues();
            }
            if (w.mAppToken != null) {
                w.mAppToken.layoutLetterbox(w);
            }
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "  LAYOUT: mFrame=" + w.mFrame + " mContainingFrame=" + w.mContainingFrame + " mDisplayFrame=" + w.mDisplayFrame);
            }
        }
    }

    public static /* synthetic */ void lambda$new$5(DisplayContent displayContent, WindowState w) {
        if (w.mLayoutAttached) {
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "2ND PASS " + w + " mHaveFrame=" + w.mHaveFrame + " mViewVisibility=" + w.mViewVisibility + " mRelayoutCalled=" + w.mRelayoutCalled);
            }
            if (displayContent.mTmpWindow != null && displayContent.mService.mPolicy.canBeHiddenByKeyguardLw(w)) {
                return;
            }
            if ((w.mViewVisibility != 8 && w.mRelayoutCalled) || !w.mHaveFrame || w.mLayoutNeeded) {
                if (displayContent.mTmpInitial) {
                    w.mContentChanged = false;
                }
                w.mLayoutNeeded = false;
                w.prelayout();
                displayContent.mService.mPolicy.layoutWindowLw(w, w.getParentWindow(), displayContent.mDisplayFrames);
                w.mLayoutSeq = displayContent.mLayoutSeq;
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v(TAG, " LAYOUT: mFrame=" + w.mFrame + " mContainingFrame=" + w.mContainingFrame + " mDisplayFrame=" + w.mDisplayFrame);
                }
            }
        } else if (w.mAttrs.type == 2023) {
            displayContent.mTmpWindow = displayContent.mTmpWindow2;
        }
    }

    public static /* synthetic */ boolean lambda$new$6(DisplayContent displayContent, WindowState w) {
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD && displayContent.mUpdateImeTarget) {
            Slog.i(TAG, "Checking window @" + w + " fl=0x" + Integer.toHexString(w.mAttrs.flags));
        }
        return w.canBeImeTarget();
    }

    public static /* synthetic */ void lambda$new$8(DisplayContent displayContent, WindowState w) {
        WindowSurfacePlacer windowSurfacePlacer = displayContent.mService.mWindowPlacerLocked;
        boolean obscuredChanged = w.mObscured != displayContent.mTmpApplySurfaceChangesTransactionState.obscured;
        RootWindowContainer root = displayContent.mService.mRoot;
        boolean someoneLosingFocus = !displayContent.mService.mLosingFocus.isEmpty();
        w.mObscured = displayContent.mTmpApplySurfaceChangesTransactionState.obscured;
        if (!displayContent.mTmpApplySurfaceChangesTransactionState.obscured) {
            boolean isDisplayed = w.isDisplayedLw();
            if (isDisplayed && w.isObscuringDisplay()) {
                root.mObscuringWindow = w;
                displayContent.mTmpApplySurfaceChangesTransactionState.obscured = true;
            }
            displayContent.mTmpApplySurfaceChangesTransactionState.displayHasContent |= root.handleNotObscuredLocked(w, displayContent.mTmpApplySurfaceChangesTransactionState.obscured, displayContent.mTmpApplySurfaceChangesTransactionState.syswin);
            if (w.mHasSurface && isDisplayed) {
                int type = w.mAttrs.type;
                if (type == 2008 || type == 2010 || (w.mAttrs.privateFlags & 1024) != 0) {
                    displayContent.mTmpApplySurfaceChangesTransactionState.syswin = true;
                }
                if (displayContent.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate == 0.0f && w.mAttrs.preferredRefreshRate != 0.0f) {
                    displayContent.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate = w.mAttrs.preferredRefreshRate;
                }
                if (displayContent.mTmpApplySurfaceChangesTransactionState.preferredModeId == 0 && w.mAttrs.preferredDisplayModeId != 0) {
                    displayContent.mTmpApplySurfaceChangesTransactionState.preferredModeId = w.mAttrs.preferredDisplayModeId;
                }
            }
        }
        if (displayContent.isDefaultDisplay && obscuredChanged && w.isVisibleLw() && displayContent.mWallpaperController.isWallpaperTarget(w)) {
            displayContent.mWallpaperController.updateWallpaperVisibility();
        }
        w.handleWindowMovedIfNeeded();
        WindowStateAnimator winAnimator = w.mWinAnimator;
        w.mContentChanged = false;
        if (w.mHasSurface) {
            boolean committed = winAnimator.commitFinishDrawingLocked();
            if (displayContent.isDefaultDisplay && committed) {
                if (w.mAttrs.type == 2023) {
                    displayContent.pendingLayoutChanges |= 1;
                }
                if ((w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                        Slog.v(TAG, "First draw done in potential wallpaper target " + w);
                    }
                    root.mWallpaperMayChange = true;
                    displayContent.pendingLayoutChanges |= 4;
                }
            }
        }
        AppWindowToken atoken = w.mAppToken;
        if (atoken != null) {
            atoken.updateLetterboxSurface(w);
            if (atoken.updateDrawnWindowStates(w) && !displayContent.mTmpUpdateAllDrawn.contains(atoken)) {
                Slog.v(TAG_VISIBILITY, "updateAllDrawn Add " + atoken);
                displayContent.mTmpUpdateAllDrawn.add(atoken);
            }
        }
        if (displayContent.isDefaultDisplay && someoneLosingFocus && w == displayContent.mService.mCurrentFocus && w.isDisplayedLw()) {
            displayContent.mTmpApplySurfaceChangesTransactionState.focusDisplayed = true;
        }
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayContent.mDisplayId) && someoneLosingFocus && w == displayContent.mService.mCurrentFocus && w.isDisplayedLw()) {
            displayContent.mTmpApplySurfaceChangesTransactionState.focusDisplayed = true;
        }
        w.updateResizingWindowIfNeeded();
    }

    public DisplayContent(Display display, WindowManagerService service, WallpaperController wallpaperController, DisplayWindowController controller) {
        super(service);
        setController(controller);
        if (service.mRoot.getDisplayContent(display.getDisplayId()) == null) {
            this.mDisplay = display;
            this.mDisplayId = display.getDisplayId();
            this.mWallpaperController = wallpaperController;
            display.getDisplayInfo(this.mDisplayInfo);
            display.getMetrics(this.mDisplayMetrics);
            this.isDefaultDisplay = this.mDisplayId == 0;
            this.mDisplayFrames = new DisplayFrames(this.mDisplayId, this.mDisplayInfo, calculateDisplayCutoutForRotation(this.mDisplayInfo.rotation));
            if (!this.isDefaultDisplay && !"local:100000".equals(this.mDisplayInfo.uniqueId) && SystemProperties.getInt("ro.panel.hw_orientation", 0) / 90 == 1) {
                this.mRotation = 0;
            }
            this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
            initializeDisplayBaseInfo();
            this.mDividerControllerLocked = new DockedStackDividerController(service, this);
            this.mPinnedStackControllerLocked = new PinnedStackController(service, this);
            this.mSurfaceSize = Math.max(this.mBaseDisplayHeight, this.mBaseDisplayWidth) * 2;
            SurfaceControl.Builder b = this.mService.makeSurfaceBuilder(this.mSession).setSize(this.mSurfaceSize, this.mSurfaceSize).setOpaque(true);
            this.mWindowingLayer = b.setName("Display Root").build();
            this.mOverlayLayer = b.setName("Display Overlays").build();
            getPendingTransaction().setLayer(this.mWindowingLayer, 0).setLayerStack(this.mWindowingLayer, this.mDisplayId).show(this.mWindowingLayer).setLayer(this.mOverlayLayer, 1).setLayerStack(this.mOverlayLayer, this.mDisplayId).show(this.mOverlayLayer);
            getPendingTransaction().apply();
            super.addChild(this.mBelowAppWindowsContainers, (Comparator) null);
            super.addChild(this.mTaskStackContainers, (Comparator) null);
            super.addChild(this.mAboveAppWindowsContainers, (Comparator) null);
            super.addChild(this.mImeWindowsContainers, (Comparator) null);
            this.mService.mRoot.addChild(this, (Comparator) null);
            this.mDisplayReady = true;
            return;
        }
        throw new IllegalArgumentException("Display with ID=" + display.getDisplayId() + " already exists=" + service.mRoot.getDisplayContent(display.getDisplayId()) + " new=" + display);
    }

    /* access modifiers changed from: package-private */
    public boolean isReady() {
        return this.mService.mDisplayReady && this.mDisplayReady;
    }

    /* access modifiers changed from: package-private */
    public int getDisplayId() {
        return this.mDisplayId;
    }

    /* access modifiers changed from: package-private */
    public WindowToken getWindowToken(IBinder binder) {
        return this.mTokenMap.get(binder);
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppWindowToken(IBinder binder) {
        WindowToken token = getWindowToken(binder);
        if (token == null) {
            return null;
        }
        return token.asAppWindowToken();
    }

    private void addWindowToken(IBinder binder, WindowToken token) {
        DisplayContent dc = this.mService.mRoot.getWindowTokenDisplay(token);
        if (dc != null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " already mapped to display=" + dc + " tokens=" + dc.mTokenMap);
        } else if (binder == null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " binder is null");
        } else if (token != null) {
            if (this.mVrMananger.isVRDeviceConnected() && IS_DEBUG_VERSION) {
                Log.i(TAG_VR, "displaycontent addWindowToken binder = " + binder + "token = " + token + "displayid = " + this.mDisplayId);
            }
            this.mTokenMap.put(binder, token);
            if (token.asAppWindowToken() == null) {
                int i = token.windowType;
                if (i != 2103) {
                    switch (i) {
                        case 2011:
                        case 2012:
                            this.mImeWindowsContainers.addChild(token);
                            return;
                        case 2013:
                            break;
                        default:
                            this.mAboveAppWindowsContainers.addChild(token);
                            return;
                    }
                }
                this.mBelowAppWindowsContainers.addChild(token);
            }
        } else {
            throw new IllegalArgumentException("Can't map null token to display=" + getName() + " binder=" + binder);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowToken removeWindowToken(IBinder binder) {
        WindowToken token = this.mTokenMap.remove(binder);
        if (token != null && token.asAppWindowToken() == null) {
            token.setExiting();
            if (token.isEmpty()) {
                token.removeImmediately();
            }
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public void reParentWindowToken(WindowToken token) {
        DisplayContent prevDc = token.getDisplayContent();
        if (prevDc != this) {
            if (!(prevDc == null || prevDc.mTokenMap.remove(token.token) == null || token.asAppWindowToken() != null)) {
                token.getParent().removeChild(token);
            }
            addWindowToken(token.token, token);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAppToken(IBinder binder) {
        WindowToken token = removeWindowToken(binder);
        if (token == null) {
            Slog.w(TAG, "removeAppToken: Attempted to remove non-existing token: " + binder);
            return;
        }
        AppWindowToken appToken = token.asAppWindowToken();
        if (appToken == null) {
            Slog.w(TAG, "Attempted to remove non-App token: " + binder + " token=" + token);
            return;
        }
        appToken.onRemovedFromDisplay();
    }

    /* access modifiers changed from: package-private */
    public Display getDisplay() {
        return this.mDisplay;
    }

    public DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    public int getRotation() {
        return this.mRotation;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRotation(int newRotation) {
        this.mRotation = newRotation;
    }

    /* access modifiers changed from: package-private */
    public int getLastOrientation() {
        return this.mLastOrientation;
    }

    /* access modifiers changed from: package-private */
    public void setLastOrientation(int orientation) {
        this.mLastOrientation = orientation;
    }

    /* access modifiers changed from: package-private */
    public boolean getAltOrientation() {
        return this.mAltOrientation;
    }

    /* access modifiers changed from: package-private */
    public void setAltOrientation(boolean altOrientation) {
        this.mAltOrientation = altOrientation;
    }

    /* access modifiers changed from: package-private */
    public int getLastWindowForcedOrientation() {
        return this.mLastWindowForcedOrientation;
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked() {
        return updateRotationUnchecked(false);
    }

    /* access modifiers changed from: protected */
    public boolean updateRotationUnchecked(boolean forceUpdate) {
        int rotation;
        ScreenRotationAnimation screenRotationAnimation;
        boolean rotateSeamlessly;
        boolean z;
        if (!forceUpdate) {
            if (this.mService.mDeferredRotationPauseCount > 0) {
                Flog.i(308, "Deferring rotation, rotation is paused.");
                return false;
            }
            ScreenRotationAnimation screenRotationAnimation2 = this.mService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            if (screenRotationAnimation2 != null && screenRotationAnimation2.isAnimating()) {
                Flog.i(308, "Deferring rotation, animation in progress.");
                return false;
            } else if (this.mService.mDisplayFrozen) {
                Flog.i(308, "Deferring rotation, still finishing previous rotation");
                return false;
            }
        }
        if (!this.mService.mDisplayEnabled) {
            Flog.i(308, "Deferring rotation, display is not enabled.");
            return false;
        }
        int oldRotation = this.mRotation;
        int lastOrientation = this.mLastOrientation;
        boolean oldAltOrientation = this.mAltOrientation;
        if (!HwPCUtils.enabledInPad() || this.isDefaultDisplay || !"local:100000".equals(this.mDisplayInfo.uniqueId)) {
            rotation = this.mService.mPolicy.rotationForOrientationLw(lastOrientation, oldRotation, this.isDefaultDisplay);
        } else {
            rotation = 1;
        }
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.v(TAG, "Computed rotation=" + rotation + " for display id=" + this.mDisplayId + " based on lastOrientation=" + lastOrientation + " and oldRotation=" + oldRotation);
        }
        boolean mayRotateSeamlessly = this.mService.mPolicy.shouldRotateSeamlessly(oldRotation, rotation);
        if (mayRotateSeamlessly) {
            if (getWindow($$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ.INSTANCE) != null && !forceUpdate) {
                return false;
            }
            if (hasPinnedStack()) {
                mayRotateSeamlessly = false;
            }
            int i = 0;
            while (true) {
                if (i >= this.mService.mSessions.size()) {
                    break;
                } else if (this.mService.mSessions.valueAt(i).hasAlertWindowSurfaces()) {
                    mayRotateSeamlessly = false;
                    break;
                } else {
                    i++;
                }
            }
        }
        boolean rotateSeamlessly2 = mayRotateSeamlessly;
        boolean altOrientation = !this.mService.mPolicy.rotationHasCompatibleMetricsLw(lastOrientation, rotation);
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            StringBuilder sb = new StringBuilder();
            sb.append("Display id=");
            sb.append(this.mDisplayId);
            sb.append(" selected orientation ");
            sb.append(lastOrientation);
            sb.append(", got rotation ");
            sb.append(rotation);
            sb.append(" which has ");
            sb.append(altOrientation ? "incompatible" : "compatible");
            sb.append(" metrics");
            Slog.v(TAG, sb.toString());
        }
        if (oldRotation == rotation && oldAltOrientation == altOrientation) {
            Flog.i(308, "No changes, Selected orientation " + lastOrientation + ", got rotation " + rotation + " altOrientation " + altOrientation);
            return false;
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Display id=");
        sb2.append(this.mDisplayId);
        sb2.append(" rotation changed to ");
        sb2.append(rotation);
        sb2.append(altOrientation ? " (alt)" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        sb2.append(" from ");
        sb2.append(oldRotation);
        sb2.append(oldAltOrientation ? " (alt)" : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        sb2.append(", lastOrientation=");
        sb2.append(lastOrientation);
        sb2.append(", rotateSeamlessly=");
        sb2.append(rotateSeamlessly2);
        Flog.i(308, sb2.toString());
        if (deltaRotation(rotation, oldRotation) != 2) {
            this.mService.mWaitingForConfig = true;
        }
        if (!(this.mService == null || -1 == this.mService.getDockedStackSide())) {
            uploadOrientation(rotation);
        }
        this.mRotation = rotation;
        setDisplayRotationFR(this.mRotation);
        if (mIsFoldable) {
            this.mService.mH.removeMessages(105);
            this.mService.mH.sendMessage(this.mService.mH.obtainMessage(105, this.mRotation, 0));
            Slog.d(TAG, "Fsm_comm send SET_INPUT_ROTATION_TASK message. mRotation:" + this.mRotation);
        }
        this.mAltOrientation = altOrientation;
        if (this.isDefaultDisplay) {
            this.mService.mPolicy.setRotationLw(rotation);
        }
        this.mService.mWindowsFreezingScreen = 1;
        this.mService.mH.removeMessages(11);
        this.mService.mH.sendEmptyMessageDelayed(11, (long) WindowManagerService.WINDOW_FREEZE_TIMEOUT_DURATION);
        setLayoutNeeded();
        int[] anim = new int[2];
        this.mService.mPolicy.selectRotationAnimationLw(anim);
        if (!rotateSeamlessly2) {
            this.mService.startFreezingDisplayLocked(anim[0], anim[1], this);
            screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        } else {
            screenRotationAnimation = null;
            this.mService.startSeamlessRotation();
        }
        ScreenRotationAnimation screenRotationAnimation3 = screenRotationAnimation;
        updateDisplayAndOrientation(getConfiguration().uiMode);
        if (screenRotationAnimation3 == null || !screenRotationAnimation3.hasScreenshot()) {
            boolean z2 = altOrientation;
            rotateSeamlessly = rotateSeamlessly2;
        } else {
            int[] iArr = anim;
            boolean z3 = altOrientation;
            rotateSeamlessly = rotateSeamlessly2;
            if (screenRotationAnimation3.setRotation(getPendingTransaction(), rotation, JobStatus.DEFAULT_TRIGGER_UPDATE_DELAY, this.mService.getTransitionAnimationScaleLocked(), this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight)) {
                this.mService.scheduleAnimationLocked();
            }
        }
        if (rotateSeamlessly) {
            z = true;
            forAllWindows((Consumer<WindowState>) new Consumer(oldRotation, rotation) {
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void accept(Object obj) {
                    ((WindowState) obj).mWinAnimator.seamlesslyRotateWindow(DisplayContent.this.getPendingTransaction(), this.f$1, this.f$2);
                }
            }, true);
        } else {
            z = true;
        }
        this.mService.mDisplayManagerInternal.performTraversal(getPendingTransaction());
        scheduleAnimation();
        forAllWindows((Consumer<WindowState>) new Consumer(rotateSeamlessly) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                DisplayContent.lambda$updateRotationUnchecked$11(DisplayContent.this, this.f$1, (WindowState) obj);
            }
        }, z);
        if (rotateSeamlessly) {
            this.mService.mH.removeMessages(54);
            this.mService.mH.sendEmptyMessageDelayed(54, 2000);
        }
        int i2 = this.mService.mRotationWatchers.size() - (z ? 1 : 0);
        while (true) {
            int i3 = i2;
            if (i3 < 0) {
                break;
            }
            WindowManagerService.RotationWatcher rotationWatcher = this.mService.mRotationWatchers.get(i3);
            if (rotationWatcher.mDisplayId == this.mDisplayId) {
                try {
                    rotationWatcher.mWatcher.onRotationChanged(rotation);
                } catch (RemoteException e) {
                }
            }
            i2 = i3 - 1;
        }
        if (screenRotationAnimation3 == null && this.mService.mAccessibilityController != null && this.isDefaultDisplay) {
            this.mService.mAccessibilityController.onRotationChangedLocked(this);
        }
        this.mService.mPolicy.notifyRotationChange(this.mRotation);
        WindowManagerService windowManagerService = this.mService;
        if (WindowManagerService.mSupporInputMethodFilletAdaptation && rotation == 0 && !this.mService.mPolicy.isNavBarVisible() && this.mService.mInputMethodWindow != null && this.mService.mInputMethodWindow.isImeWithHwFlag() && (this.mService.mInputMethodWindow.getAttrs().hwFlags & DumpState.DUMP_DEXOPT) == 0 && this.mService.mInputMethodWindow.isVisible()) {
            this.mService.mH.post(new Runnable() {
                public void run() {
                    synchronized (DisplayContent.this.mService.mWindowMap) {
                        try {
                            WindowManagerService.boostPriorityForLockedSection();
                            if (DisplayContent.this.mService.mInputMethodWindow != null) {
                                DisplayContent.this.mService.mInputMethodWindow.showInsetSurfaceOverlayImmediately();
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
            });
        }
        return z;
    }

    public static /* synthetic */ void lambda$updateRotationUnchecked$11(DisplayContent displayContent, boolean rotateSeamlessly, WindowState w) {
        if (w.mHasSurface && !rotateSeamlessly) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Set mOrientationChanging of " + w);
            }
            w.setOrientationChanging(true);
            displayContent.mService.mRoot.mOrientationChangeComplete = false;
            w.mLastFreezeDuration = 0;
        }
        w.mReportOrientationChanged = true;
    }

    /* access modifiers changed from: package-private */
    public void configureDisplayPolicy() {
        this.mService.mPolicy.setInitialDisplaySize(getDisplay(), this.mBaseDisplayWidth, this.mBaseDisplayHeight, this.mBaseDisplayDensity);
        this.mDisplayFrames.onDisplayInfoUpdated(this.mDisplayInfo, calculateDisplayCutoutForRotation(this.mDisplayInfo.rotation));
    }

    private DisplayInfo updateDisplayAndOrientation(int uiMode) {
        boolean z = true;
        if (!(this.mRotation == 1 || this.mRotation == 3)) {
            z = false;
        }
        boolean rotated = z;
        int realdw = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int realdh = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int dw = realdw;
        int dh = realdh;
        if (this.mAltOrientation) {
            if (realdw > realdh) {
                int maxw = (int) (((float) realdh) / 1.3f);
                if (maxw < realdw) {
                    dw = maxw;
                }
            } else {
                int maxh = (int) (((float) realdw) / 1.3f);
                if (maxh < realdh) {
                    dh = maxh;
                }
            }
        }
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(this.mRotation).getDisplayCutout();
        int i = dw;
        int i2 = dh;
        int i3 = uiMode;
        int appHeight = this.mService.mPolicy.getNonDecorDisplayHeight(i, i2, this.mRotation, i3, this.mDisplayId, displayCutout);
        this.mDisplayInfo.rotation = this.mRotation;
        this.mDisplayInfo.logicalWidth = dw;
        this.mDisplayInfo.logicalHeight = dh;
        this.mDisplayInfo.logicalDensityDpi = this.mBaseDisplayDensity;
        this.mDisplayInfo.appWidth = this.mService.mPolicy.getNonDecorDisplayWidth(i, i2, this.mRotation, i3, this.mDisplayId, displayCutout);
        this.mDisplayInfo.appHeight = appHeight;
        DisplayInfo overrideDisplayInfo = null;
        if (this.isDefaultDisplay) {
            this.mDisplayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, null);
        }
        this.mDisplayInfo.displayCutout = displayCutout.isEmpty() ? null : displayCutout;
        this.mDisplayInfo.getAppMetrics(this.mDisplayMetrics);
        if (this.mDisplayScalingDisabled) {
            this.mDisplayInfo.flags |= 1073741824;
        } else {
            this.mDisplayInfo.flags &= -1073741825;
        }
        if (this.mShouldOverrideDisplayConfiguration) {
            overrideDisplayInfo = this.mDisplayInfo;
        }
        this.mService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(this.mDisplayId, overrideDisplayInfo);
        this.mBaseDisplayRect.set(0, 0, dw, dh);
        if (this.isDefaultDisplay) {
            DisplayMetrics displayMetrics = this.mDisplayMetrics;
            this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
        }
        updateBounds();
        return this.mDisplayInfo;
    }

    /* access modifiers changed from: package-private */
    public WmDisplayCutout calculateDisplayCutoutForRotation(int rotation) {
        return this.mDisplayCutoutCache.getOrCompute(this.mInitialDisplayCutout, rotation);
    }

    /* access modifiers changed from: private */
    public WmDisplayCutout calculateDisplayCutoutForRotationUncached(DisplayCutout cutout, int rotation) {
        if (cutout == null || cutout == DisplayCutout.NO_CUTOUT) {
            return WmDisplayCutout.NO_CUTOUT;
        }
        if (rotation == 0) {
            return WmDisplayCutout.computeSafeInsets(cutout, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
        }
        boolean rotated = true;
        if (!(rotation == 1 || rotation == 3)) {
            rotated = false;
        }
        Path bounds = cutout.getBounds().getBoundaryPath();
        CoordinateTransforms.transformPhysicalToLogicalCoordinates(rotation, this.mBaseDisplayWidth, this.mBaseDisplayHeight, this.mTmpMatrix);
        bounds.transform(this.mTmpMatrix);
        return WmDisplayCutout.computeSafeInsets(DisplayCutout.fromBounds(bounds), rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth, rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0153  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0158 A[SYNTHETIC] */
    public void computeScreenConfiguration(Configuration config) {
        int i;
        int i2;
        int i3;
        Configuration configuration = config;
        DisplayInfo displayInfo = updateDisplayAndOrientation(configuration.uiMode);
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        configuration.orientation = dw <= dh ? 1 : 2;
        configuration.windowConfiguration.setWindowingMode(1);
        float density = this.mDisplayMetrics.density;
        int i4 = dw;
        int i5 = dh;
        configuration.screenWidthDp = (int) (((float) this.mService.mPolicy.getConfigDisplayWidth(i4, i5, displayInfo.rotation, configuration.uiMode, this.mDisplayId, displayInfo.displayCutout)) / density);
        configuration.screenHeightDp = (int) (((float) this.mService.mPolicy.getConfigDisplayHeight(i4, i5, displayInfo.rotation, configuration.uiMode, this.mDisplayId, displayInfo.displayCutout)) / density);
        this.mService.mPolicy.getNonDecorInsetsLw(displayInfo.rotation, dw, dh, displayInfo.displayCutout, this.mTmpRect);
        int leftInset = this.mTmpRect.left;
        int topInset = this.mTmpRect.top;
        configuration.windowConfiguration.setAppBounds(leftInset, topInset, displayInfo.appWidth + leftInset, displayInfo.appHeight + topInset);
        boolean z = false;
        boolean rotated = displayInfo.rotation == 1 || displayInfo.rotation == 3;
        char c = 3;
        int i6 = topInset;
        int i7 = leftInset;
        float f = density;
        computeSizeRangesAndScreenLayout(displayInfo, this.mDisplayId, rotated, configuration.uiMode, dw, dh, density, configuration);
        int i8 = configuration.screenLayout & -769;
        if ((displayInfo.flags & 16) != 0) {
            i = 512;
        } else {
            i = 256;
        }
        configuration.screenLayout = i8 | i;
        configuration.compatScreenWidthDp = (int) (((float) configuration.screenWidthDp) / this.mCompatibleScreenScale);
        configuration.compatScreenHeightDp = (int) (((float) configuration.screenHeightDp) / this.mCompatibleScreenScale);
        int i9 = 1;
        configuration.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated, configuration.uiMode, dw, dh, this.mDisplayId);
        configuration.densityDpi = displayInfo.logicalDensityDpi;
        if (displayInfo.isHdr()) {
            i2 = 8;
        } else {
            i2 = 4;
        }
        configuration.colorMode = i2 | ((!displayInfo.isWideColorGamut() || !this.mService.hasWideColorGamutSupport()) ? 1 : 2);
        configuration.touchscreen = 1;
        configuration.keyboard = 1;
        configuration.navigation = 1;
        int navigationPresence = 0;
        InputDevice[] devices = this.mService.mInputManager.getInputDevices();
        int len = devices != null ? devices.length : 0;
        int keyboardPresence = 0;
        int i10 = 0;
        while (i10 < len) {
            InputDevice device = devices[i10];
            if (!device.isVirtual()) {
                int sources = device.getSources();
                int presenceFlag = device.isExternal() ? 2 : i9;
                if (!this.mService.mIsTouchDevice) {
                    char c2 = c;
                    configuration.touchscreen = 1;
                } else if ((sources & UsbACInterface.FORMAT_II_AC3) == 4098) {
                    configuration.touchscreen = 3;
                }
                if ((sources & 65540) == 65540) {
                    configuration.navigation = 3;
                    navigationPresence |= presenceFlag;
                } else if ((sources & UsbTerminalTypes.TERMINAL_IN_MIC) == 513 && configuration.navigation == 1) {
                    i3 = 2;
                    configuration.navigation = 2;
                    navigationPresence |= presenceFlag;
                    if (device.getKeyboardType() != i3) {
                        configuration.keyboard = i3;
                        keyboardPresence |= presenceFlag;
                    }
                }
                i3 = 2;
                if (device.getKeyboardType() != i3) {
                }
            }
            i10++;
            i9 = 1;
            c = 3;
        }
        if (configuration.navigation == 1 && this.mService.mHasPermanentDpad) {
            configuration.navigation = 2;
            navigationPresence |= 1;
        }
        if (configuration.keyboard != 1) {
            z = true;
        }
        boolean hardKeyboardAvailable = z;
        if (hardKeyboardAvailable != this.mService.mHardKeyboardAvailable) {
            this.mService.mHardKeyboardAvailable = hardKeyboardAvailable;
            this.mService.mH.removeMessages(22);
            this.mService.mH.sendEmptyMessage(22);
        }
        configuration.keyboardHidden = 1;
        configuration.hardKeyboardHidden = 1;
        configuration.navigationHidden = 1;
        this.mService.mPolicy.adjustConfigurationLw(configuration, keyboardPresence, navigationPresence);
    }

    private int computeCompatSmallestWidth(boolean rotated, int uiMode, int dw, int dh, int displayId) {
        int unrotDw;
        int unrotDh;
        this.mTmpDisplayMetrics.setTo(this.mDisplayMetrics);
        DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDh = dh;
            unrotDw = dw;
        }
        int sw = reduceCompatConfigWidthSize(0, 0, uiMode, tmpDm, unrotDw, unrotDh, displayId);
        int i = uiMode;
        DisplayMetrics tmpDm2 = tmpDm;
        int i2 = displayId;
        DisplayMetrics displayMetrics = tmpDm2;
        return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(sw, 1, i, tmpDm, unrotDh, unrotDw, i2), 2, i, displayMetrics, unrotDw, unrotDh, i2), 3, i, displayMetrics, unrotDh, unrotDw, i2);
    }

    private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh, int displayId) {
        int i = dw;
        int i2 = dh;
        int i3 = rotation;
        int i4 = uiMode;
        int i5 = displayId;
        dm.noncompatWidthPixels = this.mService.mPolicy.getNonDecorDisplayWidth(i, i2, i3, i4, i5, this.mDisplayInfo.displayCutout);
        dm.noncompatHeightPixels = this.mService.mPolicy.getNonDecorDisplayHeight(i, i2, i3, i4, i5, this.mDisplayInfo.displayCutout);
        int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, null)) / dm.density) + 0.5f);
        if (curSize == 0 || size < curSize) {
            return size;
        }
        return curSize;
    }

    private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, int displayId, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
        int unrotDw;
        int unrotDh;
        DisplayInfo displayInfo2 = displayInfo;
        Configuration configuration = outConfig;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDh = dh;
            unrotDw = dw;
        }
        displayInfo2.smallestNominalAppWidth = 1073741824;
        displayInfo2.smallestNominalAppHeight = 1073741824;
        displayInfo2.largestNominalAppWidth = 0;
        displayInfo2.largestNominalAppHeight = 0;
        adjustDisplaySizeRanges(displayInfo2, displayId, 0, uiMode, unrotDw, unrotDh);
        DisplayInfo displayInfo3 = displayInfo2;
        int i = displayId;
        int i2 = uiMode;
        adjustDisplaySizeRanges(displayInfo3, i, 1, i2, unrotDh, unrotDw);
        adjustDisplaySizeRanges(displayInfo3, i, 2, i2, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo3, i, 3, i2, unrotDh, unrotDw);
        float f = density;
        int i3 = uiMode;
        int i4 = displayId;
        int sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(Configuration.resetScreenLayout(configuration.screenLayout), 0, f, unrotDw, unrotDh, i3, i4), 1, f, unrotDh, unrotDw, i3, i4), 2, f, unrotDw, unrotDh, i3, i4), 3, f, unrotDh, unrotDw, i3, i4);
        configuration.smallestScreenWidthDp = (int) (((float) displayInfo2.smallestNominalAppWidth) / density);
        configuration.screenLayout = sl;
    }

    private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode, int displayId) {
        int w = this.mService.mPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayId, this.mDisplayInfo.displayCutout);
        int longSize = w;
        int shortSize = this.mService.mPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayId, this.mDisplayInfo.displayCutout);
        if (longSize < shortSize) {
            int tmp = longSize;
            longSize = shortSize;
            shortSize = tmp;
        }
        return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
    }

    private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int displayId, int rotation, int uiMode, int dw, int dh) {
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
        int width = this.mService.mPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode, displayId, displayCutout);
        if (width < displayInfo.smallestNominalAppWidth) {
            displayInfo.smallestNominalAppWidth = width;
        }
        if (width > displayInfo.largestNominalAppWidth) {
            displayInfo.largestNominalAppWidth = width;
        }
        int height = this.mService.mPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode, displayId, displayCutout);
        if (height < displayInfo.smallestNominalAppHeight) {
            displayInfo.smallestNominalAppHeight = height;
        }
        if (height > displayInfo.largestNominalAppHeight) {
            displayInfo.largestNominalAppHeight = height;
        }
    }

    /* access modifiers changed from: package-private */
    public DockedStackDividerController getDockedDividerController() {
        return this.mDividerControllerLocked;
    }

    /* access modifiers changed from: package-private */
    public PinnedStackController getPinnedStackController() {
        return this.mPinnedStackControllerLocked;
    }

    /* access modifiers changed from: package-private */
    public boolean hasAccess(int uid) {
        return this.mDisplay.hasAccess(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getHomeStack() {
        return this.mTaskStackContainers.getHomeStack();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getSplitScreenPrimaryStack() {
        TaskStack stack = this.mTaskStackContainers.getSplitScreenPrimaryStack();
        if (stack == null || !stack.isVisible()) {
            return null;
        }
        return stack;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getSplitScreenPrimaryStackIgnoringVisibility() {
        return this.mTaskStackContainers.getSplitScreenPrimaryStack();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getPinnedStack() {
        return this.mTaskStackContainers.getPinnedStack();
    }

    /* access modifiers changed from: private */
    public boolean hasPinnedStack() {
        return this.mTaskStackContainers.getPinnedStack() != null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getTopStackInWindowingMode(int windowingMode) {
        return getStack(windowingMode, 0);
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack(int windowingMode, int activityType) {
        return this.mTaskStackContainers.getStack(windowingMode, activityType);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public TaskStack getTopStack() {
        return this.mTaskStackContainers.getTopStack();
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Task> getVisibleTasks() {
        return this.mTaskStackContainers.getVisibleTasks();
    }

    /* access modifiers changed from: package-private */
    public void onStackWindowingModeChanged(TaskStack stack) {
        this.mTaskStackContainers.onStackWindowingModeChanged(stack);
    }

    public void onConfigurationChanged(Configuration newParentConfig) {
        if (!HwPCUtils.enabledInPad() || HwPCUtils.isPcCastModeInServer() || this.isDefaultDisplay || !"local:100000".equals(this.mDisplayInfo.uniqueId) || this.mDisplayInfo.rotation != 1) {
            super.onConfigurationChanged(newParentConfig);
            this.mService.reconfigureDisplayLocked(this);
            if (getDockedDividerController() != null) {
                getDockedDividerController().onConfigurationChanged();
            }
            if (getPinnedStackController() != null) {
                getPinnedStackController().onConfigurationChanged();
            }
            return;
        }
        Slog.v(TAG, "onConfigurationChanged() not handle");
    }

    /* access modifiers changed from: package-private */
    public void updateStackBoundsAfterConfigChange(List<TaskStack> changedStackList) {
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack.updateBoundsAfterConfigChange()) {
                changedStackList.add(stack);
            }
        }
        if (hasPinnedStack() == 0) {
            this.mPinnedStackControllerLocked.onDisplayInfoChanged();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean fillsParent() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isVisible() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onAppTransitionDone() {
        super.onAppTransitionDone();
        this.mService.mWindowsChanged = true;
    }

    /* access modifiers changed from: package-private */
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                DisplayChildWindowContainer child = (DisplayChildWindowContainer) this.mChildren.get(i);
                if ((child != this.mImeWindowsContainers || this.mService.mInputMethodTarget == null) && child.forAllWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            }
        } else {
            int count = this.mChildren.size();
            for (int i2 = 0; i2 < count; i2++) {
                DisplayChildWindowContainer child2 = (DisplayChildWindowContainer) this.mChildren.get(i2);
                if ((child2 != this.mImeWindowsContainers || this.mService.mInputMethodTarget == null) && child2.forAllWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean forAllImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return this.mImeWindowsContainers.forAllWindows(callback, traverseTopToBottom);
    }

    /* access modifiers changed from: package-private */
    public int getOrientation() {
        WindowManagerPolicy policy = this.mService.mPolicy;
        if (!this.mService.mDisplayFrozen) {
            int orientation = this.mAboveAppWindowsContainers.getOrientation();
            if (orientation != -2) {
                return orientation;
            }
        } else if (this.mLastWindowForcedOrientation != -1) {
            Slog.v(TAG, "Display id=" + this.mDisplayId + " is frozen, return " + this.mLastWindowForcedOrientation);
            return this.mLastWindowForcedOrientation;
        } else if (policy.isKeyguardLocked()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v(TAG, "Display id=" + this.mDisplayId + " is frozen while keyguard locked, return " + this.mLastOrientation);
            }
            return this.mLastOrientation;
        }
        return this.mTaskStackContainers.getOrientation();
    }

    /* access modifiers changed from: package-private */
    public void updateDisplayInfo() {
        updateBaseDisplayMetricsIfNeeded();
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(i)).updateDisplayInfo(null);
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeDisplayBaseInfo() {
        DisplayManagerInternal displayManagerInternal = this.mService.mDisplayManagerInternal;
        if (displayManagerInternal != null) {
            DisplayInfo newDisplayInfo = displayManagerInternal.getDisplayInfo(this.mDisplayId);
            if (newDisplayInfo != null) {
                this.mDisplayInfo.copyFrom(newDisplayInfo);
            }
        }
        updateBaseDisplayMetrics(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mDisplayInfo.logicalDensityDpi);
        this.mInitialDisplayWidth = this.mDisplayInfo.logicalWidth;
        this.mInitialDisplayHeight = this.mDisplayInfo.logicalHeight;
        this.mInitialDisplayDensity = this.mDisplayInfo.logicalDensityDpi;
        this.mInitialDisplayCutout = this.mDisplayInfo.displayCutout;
    }

    private void updateBaseDisplayMetricsIfNeeded() {
        int i;
        int i2;
        int i3;
        DisplayInfo newDisplayInfo = new DisplayInfo();
        this.mService.mDisplayManagerInternal.getNonOverrideDisplayInfo(this.mDisplayId, newDisplayInfo);
        int orientation = newDisplayInfo.rotation;
        boolean isDisplayDensityForced = false;
        boolean rotated = orientation == 1 || orientation == 3;
        int newWidth = rotated ? newDisplayInfo.logicalHeight : newDisplayInfo.logicalWidth;
        int newHeight = rotated ? newDisplayInfo.logicalWidth : newDisplayInfo.logicalHeight;
        int newDensity = newDisplayInfo.logicalDensityDpi;
        DisplayCutout newCutout = newDisplayInfo.displayCutout;
        if ((this.mInitialDisplayWidth == newWidth && this.mInitialDisplayHeight == newHeight && this.mInitialDisplayDensity == newDensity && Objects.equals(this.mInitialDisplayCutout, newCutout)) ? false : true) {
            Slog.v(TAG, "the display metrics changed.  mInitialDisplayWidth = " + this.mInitialDisplayWidth + "; mInitialDisplayHeight = " + this.mInitialDisplayHeight + "; mInitialDisplayDensity = " + this.mInitialDisplayDensity + "; newWidth = " + newWidth + "; newHeight = " + newHeight + "; newDensity = " + newDensity + "; mBaseDisplayWidth = " + this.mBaseDisplayWidth + "; mBaseDisplayHeight = " + this.mBaseDisplayHeight + "; mBaseDisplayDensity = " + this.mBaseDisplayDensity + "; rotated = " + rotated + "; mDisplayId = " + this.mDisplayId);
            boolean isDisplaySizeForced = (this.mBaseDisplayWidth == this.mInitialDisplayWidth && this.mBaseDisplayHeight == this.mInitialDisplayHeight) ? false : true;
            if (this.mBaseDisplayDensity != this.mInitialDisplayDensity) {
                isDisplayDensityForced = true;
            }
            if (isDisplaySizeForced) {
                i = this.mBaseDisplayWidth;
            } else {
                i = newWidth;
            }
            if (isDisplaySizeForced) {
                i2 = this.mBaseDisplayHeight;
            } else {
                i2 = newHeight;
            }
            if (isDisplayDensityForced) {
                i3 = this.mBaseDisplayDensity;
            } else {
                i3 = newDensity;
            }
            updateBaseDisplayMetrics(i, i2, i3);
            this.mInitialDisplayWidth = newWidth;
            this.mInitialDisplayHeight = newHeight;
            this.mInitialDisplayDensity = newDensity;
            this.mInitialDisplayCutout = newCutout;
            this.mService.reconfigureDisplayLocked(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setMaxUiWidth(int width) {
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v(TAG, "Setting max ui width:" + width + " on display:" + getDisplayId());
        }
        this.mMaxUiWidth = width;
        updateBaseDisplayMetrics(this.mBaseDisplayWidth, this.mBaseDisplayHeight, this.mBaseDisplayDensity);
    }

    /* access modifiers changed from: package-private */
    public void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        this.mBaseDisplayWidth = baseWidth;
        this.mBaseDisplayHeight = baseHeight;
        this.mBaseDisplayDensity = baseDensity;
        if (this.mMaxUiWidth > 0 && this.mBaseDisplayWidth > this.mMaxUiWidth) {
            this.mBaseDisplayHeight = (this.mMaxUiWidth * this.mBaseDisplayHeight) / this.mBaseDisplayWidth;
            this.mBaseDisplayDensity = (this.mMaxUiWidth * this.mBaseDisplayDensity) / this.mBaseDisplayWidth;
            this.mBaseDisplayWidth = this.mMaxUiWidth;
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v(TAG, "Applying config restraints:" + this.mBaseDisplayWidth + "x" + this.mBaseDisplayHeight + " at density:" + this.mBaseDisplayDensity + " on display:" + getDisplayId());
            }
        }
        this.mBaseDisplayRect.set(0, 0, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
        updateBounds();
        this.mDisplayCutoutCache.clearCacheTable();
    }

    /* access modifiers changed from: package-private */
    public void getStableRect(Rect out) {
        out.set(this.mDisplayFrames.mStable);
    }

    /* access modifiers changed from: package-private */
    public TaskStack createStack(int stackId, boolean onTop, StackWindowController controller) {
        TaskStack stack;
        if (HwPCUtils.isExtDynamicStack(stackId)) {
            stack = HwServiceFactory.createTaskStack(this.mService, stackId, controller);
        } else {
            stack = new TaskStack(this.mService, stackId, controller);
        }
        this.mTaskStackContainers.addStackToDisplay(stack, onTop);
        return stack;
    }

    /* access modifiers changed from: package-private */
    public void moveStackToDisplay(TaskStack stack, boolean onTop) {
        DisplayContent prevDc = stack.getDisplayContent();
        if (prevDc == null) {
            throw new IllegalStateException("Trying to move stackId=" + stack.mStackId + " which is not currently attached to any display");
        } else if (prevDc.getDisplayId() != this.mDisplayId) {
            prevDc.mTaskStackContainers.removeChild(stack);
            this.mTaskStackContainers.addStackToDisplay(stack, onTop);
        } else {
            throw new IllegalArgumentException("Trying to move stackId=" + stack.mStackId + " to its current displayId=" + this.mDisplayId);
        }
    }

    /* access modifiers changed from: protected */
    public void addChild(DisplayChildWindowContainer child, Comparator<DisplayChildWindowContainer> comparator) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: protected */
    public void addChild(DisplayChildWindowContainer child, int index2) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: protected */
    public void removeChild(DisplayChildWindowContainer child) {
        if (this.mRemovingDisplay) {
            super.removeChild(child);
            return;
        }
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayChildWindowContainer child, boolean includingParents) {
        getParent().positionChildAt(position, this, includingParents);
    }

    /* access modifiers changed from: package-private */
    public void positionStackAt(int position, TaskStack child) {
        this.mTaskStackContainers.positionChildAt(position, child, false);
        layoutAndAssignWindowLayersIfNeeded();
    }

    /* access modifiers changed from: package-private */
    public int taskIdFromPoint(int x, int y) {
        int stackNdx = this.mTaskStackContainers.getChildCount();
        while (true) {
            stackNdx--;
            if (stackNdx < 0) {
                return -1;
            }
            int taskId = ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).taskIdFromPoint(x, y);
            if (taskId != -1) {
                return taskId;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Task findTaskForResizePoint(int x, int y) {
        int delta = WindowManagerService.dipToPixel(HwFreeFormUtils.isFreeFormEnable() ? 20 : 10, this.mDisplayMetrics);
        this.mTmpTaskForResizePointSearchResult.reset();
        int stackNdx = this.mTaskStackContainers.getChildCount();
        while (true) {
            stackNdx--;
            if (stackNdx < 0) {
                return null;
            }
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(stackNdx);
            if (stack != null) {
                if (stack.getWindowConfiguration().canResizeTask() || (HwFreeFormUtils.isFreeFormEnable() && isStackVisible(5))) {
                    stack.findTaskForResizePoint(x, y, delta, this.mTmpTaskForResizePointSearchResult);
                    if (this.mTmpTaskForResizePointSearchResult.searchDone) {
                        return this.mTmpTaskForResizePointSearchResult.taskForResize;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setTouchExcludeRegion(Task focusedTask) {
        if (focusedTask == null) {
            this.mTouchExcludeRegion.setEmpty();
        } else {
            this.mTouchExcludeRegion.set(this.mBaseDisplayRect);
            int delta = WindowManagerService.dipToPixel(HwFreeFormUtils.isFreeFormEnable() ? 20 : 10, this.mDisplayMetrics);
            this.mTmpRect2.setEmpty();
            for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).setTouchExcludeRegion(focusedTask, delta, this.mTouchExcludeRegion, this.mDisplayFrames.mContent, this.mTmpRect2);
            }
            if (!this.mTmpRect2.isEmpty()) {
                this.mTouchExcludeRegion.op(this.mTmpRect2, Region.Op.UNION);
            }
        }
        if (HwPCUtils.isPcCastModeInServer() != 0) {
            if (!this.isDefaultDisplay) {
                this.mTouchExcludeRegion.setEmpty();
            }
            this.mPcTouchExcludeRegion.setEmpty();
        }
        WindowState inputMethod = this.mService.mInputMethodWindow;
        if (inputMethod != null && inputMethod.isVisibleLw()) {
            inputMethod.getTouchableRegion(this.mTmpRegion);
            if (HwPCUtils.isPcCastModeInServer()) {
                if (inputMethod.getDisplayId() == this.mDisplayId) {
                    this.mPcTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
                }
            } else if (inputMethod.getDisplayId() == this.mDisplayId) {
                this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
            } else {
                inputMethod.getDisplayContent().setTouchExcludeRegion(null);
            }
        }
        if (HwPCUtils.isPcCastModeInServer() && this.mDisplayId == 0) {
            TaskStack taskStack = (TaskStack) this.mTaskStackContainers.getTopChild();
            if (taskStack != null) {
                Task task = (Task) taskStack.getTopChild();
                if (task != null) {
                    AppWindowToken appWindowToken = (AppWindowToken) task.getTopChild();
                    if (!(appWindowToken == null || appWindowToken.appComponentName == null || !"com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(appWindowToken.appComponentName))) {
                        Rect touchpadBounds = new Rect();
                        task.getDimBounds(touchpadBounds);
                        this.mPcTouchExcludeRegion.op(touchpadBounds, Region.Op.UNION);
                        WindowState touchpadState = (WindowState) appWindowToken.getTopChild();
                        if (touchpadState != null) {
                            WindowState popupWindowState = (WindowState) touchpadState.getTopChild();
                            if (popupWindowState != null && popupWindowState.mAttrs.type == 1000) {
                                Region popupRegion = new Region();
                                popupWindowState.getTouchableRegion(popupRegion);
                                this.mPcTouchExcludeRegion.op(popupRegion, Region.Op.DIFFERENCE);
                            }
                        }
                    }
                }
            }
        }
        for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
            WindowState win = this.mTapExcludedWindows.get(i);
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayId) || win.isVisible()) {
                win.getTouchableRegion(this.mTmpRegion);
                this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
                if (HwPCUtils.isPcCastModeInServer() && this.mDisplayId == 0 && win.isVisible() && !this.mPcTouchExcludeRegion.isEmpty()) {
                    this.mPcTouchExcludeRegion.op(this.mTmpRegion, Region.Op.DIFFERENCE);
                }
            }
        }
        if (this.mTapDetector != null) {
            this.mTapDetector.setHwPCTouchExcludeRegion(this.mPcTouchExcludeRegion);
        }
        for (int i2 = this.mTapExcludeProvidingWindows.size() - 1; i2 >= 0; i2--) {
            this.mTapExcludeProvidingWindows.valueAt(i2).amendTapExcludeRegion(this.mTouchExcludeRegion);
        }
        if (this.mDisplayId == 0 && getSplitScreenPrimaryStack() != null) {
            this.mDividerControllerLocked.getTouchRegion(this.mTmpRect);
            this.mTmpRegion.set(this.mTmpRect);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
        }
        if (this.mTapDetector != null) {
            this.mTapDetector.setTouchExcludeRegion(this.mTouchExcludeRegion);
        }
    }

    /* access modifiers changed from: package-private */
    public void switchUser() {
        super.switchUser();
        this.mService.mWindowsChanged = true;
    }

    private void resetAnimationBackgroundAnimator() {
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).resetAnimationBackgroundAnimator();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeIfPossible() {
        if (isAnimating()) {
            this.mDeferredRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeImmediately() {
        boolean isVRDisplay = true;
        this.mRemovingDisplay = true;
        try {
            super.removeImmediately();
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v(TAG, "Removing display=" + this);
            }
            boolean isPCDisplay = HwPCUtils.enabled() && this.mDisplayId != -1 && this.mDisplayId != 0 && (this.mDisplayInfo.type == 2 || this.mDisplayInfo.type == 3 || (((this.mDisplayInfo.type == 5 || this.mDisplayInfo.type == 4) && SystemProperties.getBoolean("hw_pc_support_overlay", false)) || (this.mDisplayInfo.type == 5 && ("com.hpplay.happycast".equals(this.mDisplayInfo.ownerPackageName) || "com.huawei.works".equals(this.mDisplayInfo.ownerPackageName)))));
            if (!this.mVrMananger.isVRDeviceConnected() || !this.mVrMananger.isValidVRDisplayId(this.mDisplayId)) {
                isVRDisplay = false;
            }
            if (this.mService.canDispatchPointerEvents()) {
                if (this.mTapDetector != null && !isPCDisplay && !isVRDisplay) {
                    this.mService.unregisterPointerEventListener(this.mTapDetector);
                }
                if (this.mDisplayId == 0 && this.mService.mMousePositionTracker != null) {
                    this.mService.unregisterPointerEventListener(this.mService.mMousePositionTracker);
                }
            }
            this.mService.mAnimator.removeDisplayLocked(this.mDisplayId);
            if (isPCDisplay && this.mDisplayId != 0 && this.mService.canDispatchExternalPointerEvents() && this.mTapDetector != null) {
                this.mService.unregisterExternalPointerEventListener(this.mTapDetector);
                try {
                    this.mService.unregisterExternalPointerEventListener(this.mService.mMousePositionTracker);
                } catch (Exception e) {
                }
            }
        } catch (IllegalStateException e2) {
            Slog.w(TAG, "TaskTapPointerEventListener  not registered");
        } catch (Throwable th) {
            this.mRemovingDisplay = false;
            throw th;
        }
        this.mRemovingDisplay = false;
        this.mService.onDisplayRemoved(this.mDisplayId);
    }

    /* access modifiers changed from: package-private */
    public boolean checkCompleteDeferredRemoval() {
        if (super.checkCompleteDeferredRemoval() || !this.mDeferredRemoval) {
            return true;
        }
        removeImmediately();
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRemovalDeferred() {
        return this.mDeferredRemoval;
    }

    /* access modifiers changed from: package-private */
    public boolean animateForIme(float interpolatedValue, float animationTarget, float dividerAnimationTarget) {
        boolean updated = false;
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack != null && stack.isAdjustedForIme()) {
                if (interpolatedValue >= 1.0f && animationTarget == 0.0f && dividerAnimationTarget == 0.0f) {
                    stack.resetAdjustedForIme(true);
                    updated = true;
                } else {
                    this.mDividerControllerLocked.mLastAnimationProgress = this.mDividerControllerLocked.getInterpolatedAnimationValue(interpolatedValue);
                    this.mDividerControllerLocked.mLastDividerProgress = this.mDividerControllerLocked.getInterpolatedDividerValue(interpolatedValue);
                    updated |= stack.updateAdjustForIme(this.mDividerControllerLocked.mLastAnimationProgress, this.mDividerControllerLocked.mLastDividerProgress, false);
                }
                if (interpolatedValue >= 1.0f) {
                    stack.endImeAdjustAnimation();
                }
            }
        }
        return updated;
    }

    /* access modifiers changed from: package-private */
    public boolean clearImeAdjustAnimation() {
        boolean changed = false;
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack != null && stack.isAdjustedForIme()) {
                stack.resetAdjustedForIme(true);
                changed = true;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public void beginImeAdjustAnimation() {
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack.isVisible() && stack.isAdjustedForIme()) {
                stack.beginImeAdjustAnimation();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void adjustForImeIfNeeded() {
        WindowState imeWin = this.mService.mInputMethodWindow;
        boolean imeVisible = imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw() && !this.mDividerControllerLocked.isImeHideRequested();
        boolean dockVisible = isStackVisible(3);
        TaskStack imeTargetStack = this.mService.getImeFocusStackLocked();
        int imeDockSide = (!dockVisible || imeTargetStack == null) ? -1 : imeTargetStack.getDockSide();
        boolean imeOnTop = imeDockSide == 2;
        int i = 4;
        boolean imeOnBottom = imeDockSide == 4;
        boolean dockMinimized = this.mDividerControllerLocked.isMinimizedDock();
        int imeHeight = this.mDisplayFrames.getInputMethodWindowVisibleHeight();
        boolean imeHeightChanged = imeVisible && imeHeight != this.mDividerControllerLocked.getImeHeightAdjustedFor();
        boolean rotated = this.mRotation == 1 || this.mRotation == 3;
        if (!imeVisible || !dockVisible || ((!imeOnTop && !imeOnBottom) || dockMinimized)) {
            for (int i2 = this.mTaskStackContainers.getChildCount() - 1; i2 >= 0; i2--) {
                TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i2);
                stack.resetAdjustedForIme(!dockVisible || !stack.inSplitScreenWindowingMode());
            }
            this.mDividerControllerLocked.setAdjustedForIme(false, imeVisible && dockVisible && rotated, dockVisible, imeWin, imeHeight);
        } else {
            int i3 = this.mTaskStackContainers.getChildCount() - 1;
            while (i3 >= 0) {
                TaskStack stack2 = (TaskStack) this.mTaskStackContainers.getChildAt(i3);
                boolean isDockedOnBottom = stack2.getDockSide() == i;
                if (!stack2.isVisible() || ((!imeOnBottom && !isDockedOnBottom) || !stack2.inSplitScreenWindowingMode())) {
                    stack2.resetAdjustedForIme(false);
                } else {
                    stack2.setAdjustedForIme(imeWin, imeOnBottom && imeHeightChanged);
                }
                i3--;
                i = 4;
            }
            this.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, true, true, imeWin, imeHeight);
        }
        this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
    }

    /* access modifiers changed from: package-private */
    public int getLayerForAnimationBackground(WindowStateAnimator winAnimator) {
        WindowState visibleWallpaper = this.mBelowAppWindowsContainers.getWindow($$Lambda$DisplayContent$Po0ivnfO2TfRfOth5ZIOFcmugs4.INSTANCE);
        if (visibleWallpaper != null) {
            return visibleWallpaper.mWinAnimator.mAnimLayer;
        }
        return winAnimator.mAnimLayer;
    }

    static /* synthetic */ boolean lambda$getLayerForAnimationBackground$12(WindowState w) {
        return w.mIsWallpaper && w.isVisibleNow();
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        getBounds(this.mTmpRect, newRotation);
        createRotationMatrix(deltaRotation(newRotation, oldRotation), (float) this.mTmpRect.width(), (float) this.mTmpRect.height(), this.mTmpMatrix);
        this.mTmpRectF.set(bounds);
        this.mTmpMatrix.mapRect(this.mTmpRectF);
        this.mTmpRectF.round(bounds);
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            return delta + 4;
        }
        return delta;
    }

    private static void createRotationMatrix(int rotation, float displayWidth, float displayHeight, Matrix outMatrix) {
        createRotationMatrix(rotation, 0.0f, 0.0f, displayWidth, displayHeight, outMatrix);
    }

    static void createRotationMatrix(int rotation, float rectLeft, float rectTop, float displayWidth, float displayHeight, Matrix outMatrix) {
        switch (rotation) {
            case 0:
                outMatrix.reset();
                return;
            case 1:
                outMatrix.setRotate(90.0f, 0.0f, 0.0f);
                outMatrix.postTranslate(displayWidth, 0.0f);
                outMatrix.postTranslate(-rectTop, rectLeft);
                return;
            case 2:
                outMatrix.reset();
                return;
            case 3:
                outMatrix.setRotate(270.0f, 0.0f, 0.0f);
                outMatrix.postTranslate(0.0f, displayHeight);
                outMatrix.postTranslate(rectTop, 0.0f);
                return;
            default:
                return;
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId, boolean trim) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, trim);
        proto.write(1120986464258L, this.mDisplayId);
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).writeToProto(proto, 2246267895811L, trim);
        }
        this.mDividerControllerLocked.writeToProto(proto, 1146756268036L);
        this.mPinnedStackControllerLocked.writeToProto(proto, 1146756268037L);
        for (int i = this.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
            ((WindowToken) this.mAboveAppWindowsContainers.getChildAt(i)).writeToProto(proto, 2246267895814L, trim);
        }
        for (int i2 = this.mBelowAppWindowsContainers.getChildCount() - 1; i2 >= 0; i2--) {
            ((WindowToken) this.mBelowAppWindowsContainers.getChildAt(i2)).writeToProto(proto, 2246267895815L, trim);
        }
        for (int i3 = this.mImeWindowsContainers.getChildCount() - 1; i3 >= 0; i3--) {
            ((WindowToken) this.mImeWindowsContainers.getChildAt(i3)).writeToProto(proto, 2246267895816L, trim);
        }
        proto.write(1120986464265L, this.mBaseDisplayDensity);
        this.mDisplayInfo.writeToProto(proto, 1146756268042L);
        proto.write(1120986464267L, this.mRotation);
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        if (screenRotationAnimation != null) {
            screenRotationAnimation.writeToProto(proto, 1146756268044L);
        }
        this.mDisplayFrames.writeToProto(proto, 1146756268045L);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        super.dump(pw, prefix, dumpAll);
        pw.print(prefix);
        pw.print("Display: mDisplayId=");
        pw.println(this.mDisplayId);
        pw.print("  " + prefix);
        pw.print("init=");
        pw.print(this.mInitialDisplayWidth);
        pw.print("x");
        pw.print(this.mInitialDisplayHeight);
        pw.print(" ");
        pw.print(this.mInitialDisplayDensity);
        pw.print("dpi");
        if (!(this.mInitialDisplayWidth == this.mBaseDisplayWidth && this.mInitialDisplayHeight == this.mBaseDisplayHeight && this.mInitialDisplayDensity == this.mBaseDisplayDensity)) {
            pw.print(" base=");
            pw.print(this.mBaseDisplayWidth);
            pw.print("x");
            pw.print(this.mBaseDisplayHeight);
            pw.print(" ");
            pw.print(this.mBaseDisplayDensity);
            pw.print("dpi");
        }
        if (this.mDisplayScalingDisabled) {
            pw.println(" noscale");
        }
        pw.print(" cur=");
        pw.print(this.mDisplayInfo.logicalWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.logicalHeight);
        pw.print(" app=");
        pw.print(this.mDisplayInfo.appWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.appHeight);
        pw.print(" rng=");
        pw.print(this.mDisplayInfo.smallestNominalAppWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.smallestNominalAppHeight);
        pw.print("-");
        pw.print(this.mDisplayInfo.largestNominalAppWidth);
        pw.print("x");
        pw.println(this.mDisplayInfo.largestNominalAppHeight);
        pw.print(subPrefix + "deferred=" + this.mDeferredRemoval + " mLayoutNeeded=" + this.mLayoutNeeded);
        StringBuilder sb = new StringBuilder();
        sb.append(" mTouchExcludeRegion=");
        sb.append(this.mTouchExcludeRegion);
        pw.println(sb.toString());
        pw.println();
        pw.print(prefix);
        pw.print("mLayoutSeq=");
        pw.println(this.mLayoutSeq);
        pw.println();
        pw.print(prefix);
        pw.print("mDeferUpdateImeTargetCount=");
        pw.println(this.mDeferUpdateImeTargetCount);
        pw.println();
        pw.println(prefix + "Application tokens in top down Z order:");
        for (int stackNdx = this.mTaskStackContainers.getChildCount() + -1; stackNdx >= 0; stackNdx += -1) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).dump(pw, prefix + "  ", dumpAll);
        }
        pw.println();
        if (!this.mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
                WindowToken token = this.mExitingTokens.get(i);
                pw.print("  Exiting #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                pw.println(':');
                token.dump(pw, "    ", dumpAll);
            }
        }
        pw.println();
        if (getHomeStack() != null) {
            pw.println(prefix + "homeStack=" + homeStack.getName());
        }
        if (getPinnedStack() != null) {
            pw.println(prefix + "pinnedStack=" + pinnedStack.getName());
        }
        if (getSplitScreenPrimaryStack() != null) {
            pw.println(prefix + "splitScreenPrimaryStack=" + splitScreenPrimaryStack.getName());
        }
        pw.println();
        this.mDividerControllerLocked.dump(prefix, pw);
        pw.println();
        this.mPinnedStackControllerLocked.dump(prefix, pw);
        pw.println();
        this.mDisplayFrames.dump(prefix, pw);
    }

    public String toString() {
        return "Display " + this.mDisplayId + " info=" + this.mDisplayInfo + " stacks=" + this.mChildren;
    }

    /* access modifiers changed from: package-private */
    public String getName() {
        return "Display " + this.mDisplayId + " name=\"" + this.mDisplayInfo.name + "\"";
    }

    /* access modifiers changed from: package-private */
    public boolean isStackVisible(int windowingMode) {
        TaskStack stack = getTopStackInWindowingMode(windowingMode);
        return stack != null && stack.isVisible();
    }

    /* access modifiers changed from: package-private */
    public WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        return getWindow(new Predicate((int) xf, (int) yf) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final boolean test(Object obj) {
                return DisplayContent.lambda$getTouchableWinAtPointLocked$13(DisplayContent.this, this.f$1, this.f$2, (WindowState) obj);
            }
        });
    }

    public static /* synthetic */ boolean lambda$getTouchableWinAtPointLocked$13(DisplayContent displayContent, int x, int y, WindowState w) {
        int flags = w.mAttrs.flags;
        boolean z = false;
        if (!w.isVisibleLw() || (flags & 16) != 0) {
            return false;
        }
        w.getVisibleBounds(displayContent.mTmpRect);
        if (!displayContent.mTmpRect.contains(x, y)) {
            return false;
        }
        w.getTouchableRegion(displayContent.mTmpRegion);
        int touchFlags = flags & 40;
        if (displayContent.mTmpRegion.contains(x, y) || touchFlags == 0) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean canAddToastWindowForUid(int uid) {
        boolean z = true;
        if (getWindow(new Predicate(uid) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return DisplayContent.lambda$canAddToastWindowForUid$14(this.f$0, (WindowState) obj);
            }
        }) != null) {
            return true;
        }
        if (getWindow(new Predicate(uid) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return DisplayContent.lambda$canAddToastWindowForUid$15(this.f$0, (WindowState) obj);
            }
        }) != null) {
            z = false;
        }
        return z;
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$14(int uid, WindowState w) {
        return w.mOwnerUid == uid && w.isFocused();
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$15(int uid, WindowState w) {
        return w.mAttrs.type == 2005 && w.mOwnerUid == uid && !w.mPermanentlyHidden && !w.mWindowRemovalAllowed;
    }

    /* access modifiers changed from: package-private */
    public void scheduleToastWindowsTimeoutIfNeededLocked(WindowState oldFocus, WindowState newFocus) {
        if (oldFocus != null && (newFocus == null || newFocus.mOwnerUid != oldFocus.mOwnerUid)) {
            this.mTmpWindow = oldFocus;
            forAllWindows(this.mScheduleToastTimeout, false);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState findFocusedWindow() {
        this.mTmpWindow = null;
        forAllWindows(this.mFindFocusedWindow, true);
        if (this.mTmpWindow != null) {
            return this.mTmpWindow;
        }
        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
            Slog.v(TAG, "findFocusedWindow: No focusable windows.");
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void checkNeedNotifyFingerWinCovered() {
        boolean needNotify;
        boolean fingerWinCovered;
        if (this.mObserveWin != null && this.mObserveWin.isVisibleOrAdding()) {
            for (int i = this.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
                WindowToken windowToken = (WindowToken) this.mAboveAppWindowsContainers.getChildAt(i);
                if (windowToken.mChildren.contains(this.mObserveWin)) {
                    this.mObserveToken = windowToken;
                } else if (!(windowToken.getTopChild() == null || !((WindowState) windowToken.getTopChild()).isVisibleOrAdding() || ((WindowState) windowToken.getTopChild()).getAttrs().type == 2000 || ((WindowState) windowToken.getTopChild()).getAttrs().type == 2019 || (this.mTopAboveAppToken != null && this.mTopAboveAppToken.getLayer() >= windowToken.getLayer()))) {
                    this.mTopAboveAppToken = windowToken;
                }
            }
            if (this.mObserveToken == null || this.mTopAboveAppToken == null || this.mObserveToken.getLayer() >= this.mTopAboveAppToken.getLayer()) {
                fingerWinCovered = false;
                needNotify = this.mWinEverCovered;
                this.mWinEverCovered = false;
            } else {
                fingerWinCovered = true;
                this.mWinEverCovered = true;
                needNotify = true;
            }
            if (needNotify) {
                Rect winFrame = new Rect();
                if (this.mTopAboveAppToken != null) {
                    WindowState topAboveAppWindow = (WindowState) this.mTopAboveAppToken.getTopChild();
                    if (topAboveAppWindow != null) {
                        winFrame = topAboveAppWindow.getVisibleFrameLw();
                    }
                }
                this.mService.notifyFingerWinCovered(fingerWinCovered, winFrame);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void assignWindowLayers(boolean setLayoutNeeded) {
        Trace.traceBegin(32, "assignWindowLayers");
        assignChildLayers(getPendingTransaction());
        if (setLayoutNeeded) {
            setLayoutNeeded();
        }
        scheduleAnimation();
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public void layoutAndAssignWindowLayersIfNeeded() {
        this.mService.mWindowsChanged = true;
        setLayoutNeeded();
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayId)) {
            this.mService.updateFocusedWindowLocked(3, false);
            assignWindowLayers(false);
        } else if (!this.mService.updateFocusedWindowLocked(3, false)) {
            assignWindowLayers(false);
        }
        this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
        this.mService.mWindowPlacerLocked.performSurfacePlacement();
        this.mService.mInputMonitor.updateInputWindowsLw(false);
    }

    /* access modifiers changed from: package-private */
    public boolean destroyLeakedSurfaces() {
        this.mTmpWindow = null;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            public final void accept(Object obj) {
                DisplayContent.lambda$destroyLeakedSurfaces$16(DisplayContent.this, (WindowState) obj);
            }
        }, false);
        return this.mTmpWindow != null;
    }

    public static /* synthetic */ void lambda$destroyLeakedSurfaces$16(DisplayContent displayContent, WindowState w) {
        WindowStateAnimator wsa = w.mWinAnimator;
        if (wsa.mSurfaceController != null) {
            if (!displayContent.mService.mSessions.contains(wsa.mSession)) {
                Slog.w(TAG, "LEAKED SURFACE (session doesn't exist): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mToken + " pid=" + w.mSession.mPid + " uid=" + w.mSession.mUid);
                wsa.destroySurface();
                displayContent.mService.mForceRemoves.add(w);
                displayContent.mTmpWindow = w;
            } else if (w.mAppToken != null && w.mAppToken.isClientHidden()) {
                Slog.w(TAG, "LEAKED SURFACE (app token hidden): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mAppToken);
                wsa.destroySurface();
                displayContent.mTmpWindow = w;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState computeImeTarget(boolean updateImeTarget) {
        AppWindowToken token = null;
        if (this.mService.mInputMethodWindow == null) {
            if (updateImeTarget) {
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.w(TAG, "Moving IM target from " + this.mService.mInputMethodTarget + " to null since mInputMethodWindow is null");
                }
                setInputMethodTarget(null, this.mService.mInputMethodTargetWaitingAnim);
            }
            return null;
        }
        WindowState curTarget = this.mService.mInputMethodTarget;
        if (!canUpdateImeTarget()) {
            Slog.w(TAG, "Defer updating IME target:" + curTarget);
            return curTarget;
        }
        this.mUpdateImeTarget = updateImeTarget;
        WindowState target = getWindow(this.mComputeImeTargetPredicate);
        if (target != null && target.mAttrs.type == 3) {
            AppWindowToken token2 = target.mAppToken;
            if (token2 != null) {
                WindowState betterTarget = token2.getImeTargetBelowWindow(target);
                if (betterTarget != null) {
                    target = betterTarget;
                }
            }
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v(TAG, "Proposed new IME target: " + target + " curTarget:" + curTarget);
        }
        boolean isClosing = curTarget != null && ((curTarget.mAnimatingExit && curTarget.mWinAnimator.getShown()) || curTarget.mService.mClosingApps.contains(curTarget.mAppToken));
        if (curTarget == null || !curTarget.isDisplayedLw() || !isClosing || (target != null && !target.isActivityTypeHome())) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v(TAG, "Desired input method target=" + target + " updateImeTarget=" + updateImeTarget);
            }
            if (target == null) {
                if (updateImeTarget) {
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        Slog.w(TAG, "Moving IM target from " + curTarget + " to null." + BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                    }
                    setInputMethodTarget(null, this.mService.mInputMethodTargetWaitingAnim);
                }
                return null;
            }
            if (updateImeTarget) {
                if (curTarget != null) {
                    token = curTarget.mAppToken;
                }
                if (token != null) {
                    WindowState highestTarget = null;
                    if (token.isSelfAnimating()) {
                        highestTarget = token.getHighestAnimLayerWindow(curTarget);
                    }
                    if (highestTarget != null) {
                        AppTransition appTransition = this.mService.mAppTransition;
                        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                            Slog.v(TAG, appTransition + " " + highestTarget + " animating=" + highestTarget.mWinAnimator.isAnimationSet() + " layer=" + highestTarget.mWinAnimator.mAnimLayer + " new layer=" + target.mWinAnimator.mAnimLayer);
                        }
                        if (appTransition.isTransitionSet()) {
                            setInputMethodTarget(highestTarget, true);
                            return highestTarget;
                        } else if (highestTarget.mWinAnimator.isAnimationSet() && highestTarget.mWinAnimator.mAnimLayer > target.mWinAnimator.mAnimLayer) {
                            setInputMethodTarget(highestTarget, true);
                            return highestTarget;
                        }
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.w(TAG, "Moving IM target from " + curTarget + " to " + target + BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
                setInputMethodTarget(target, false);
            }
            return target;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v(TAG, "New target is home while current target is closing, not changing:" + curTarget);
        }
        return curTarget;
    }

    private void setInputMethodTarget(WindowState target, boolean targetWaitingAnim) {
        if (target != this.mService.mInputMethodTarget || this.mService.mInputMethodTargetWaitingAnim != targetWaitingAnim) {
            if (HwPCUtils.isPcCastModeInServer() && this.mService.mHardKeyboardAvailable && this.mService.getFocusedDisplayId() != 0 && this.mDisplayId == 0) {
                Slog.i(TAG, "setInputMethodTarget: target = " + target + ", mDisplayId = " + this.mDisplayId + ", mInputMethodTarget =" + this.mService.mInputMethodTarget);
                if (target != null) {
                    Slog.i(TAG, "setInputMethodTarget " + Debug.getCallers(8));
                }
            }
            if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(this.mDisplayId) || this.mService.mInputMethodTarget == null || this.mService.mInputMethodTarget.getDisplayId() == this.mDisplayId) {
                this.mService.mInputMethodTarget = target;
                WindowManagerPolicy policy = this.mService.mPolicy;
                if (policy instanceof PhoneWindowManager) {
                    ((PhoneWindowManager) policy).setInputMethodTargetWindow(target);
                }
                this.mService.mInputMethodTargetWaitingAnim = targetWaitingAnim;
                assignWindowLayers(false);
                return;
            }
            Slog.i(TAG, "setInputMethodTarget inputmethod on default display return, mDisplayId = " + this.mDisplayId + ", mHardKeyboardAvailable =" + this.mService.mHardKeyboardAvailable + ", mService.mInputMethodTarget =" + this.mService.mInputMethodTarget + ", target =" + target);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean getNeedsMenu(WindowState top, WindowManagerPolicy.WindowState bottom) {
        boolean z = false;
        if (top.mAttrs.needsMenuKey != 0) {
            if (top.mAttrs.needsMenuKey == 1) {
                z = true;
            }
            return z;
        }
        this.mTmpWindow = null;
        WindowState candidate = getWindow(new Predicate(top, bottom) {
            private final /* synthetic */ WindowState f$1;
            private final /* synthetic */ WindowManagerPolicy.WindowState f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final boolean test(Object obj) {
                return DisplayContent.lambda$getNeedsMenu$17(DisplayContent.this, this.f$1, this.f$2, (WindowState) obj);
            }
        });
        if (candidate != null && candidate.mAttrs.needsMenuKey == 1) {
            z = true;
        }
        return z;
    }

    public static /* synthetic */ boolean lambda$getNeedsMenu$17(DisplayContent displayContent, WindowState top, WindowManagerPolicy.WindowState bottom, WindowState w) {
        if (w == top) {
            displayContent.mTmpWindow = w;
        }
        if (displayContent.mTmpWindow == null) {
            return false;
        }
        if (w.mAttrs.needsMenuKey == 0 && w != bottom) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setLayoutNeeded() {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.w(TAG, "setLayoutNeeded: callers=" + Debug.getCallers(3));
        }
        this.mLayoutNeeded = true;
    }

    private void clearLayoutNeeded() {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.w(TAG, "clearLayoutNeeded: callers=" + Debug.getCallers(3));
        }
        this.mLayoutNeeded = false;
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutNeeded() {
        return this.mLayoutNeeded;
    }

    /* access modifiers changed from: package-private */
    public void dumpTokens(PrintWriter pw, boolean dumpAll) {
        if (!this.mTokenMap.isEmpty()) {
            pw.println("  Display #" + this.mDisplayId);
            for (WindowToken token : this.mTokenMap.values()) {
                pw.print("  ");
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ", dumpAll);
                } else {
                    pw.println();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpWindowAnimators(PrintWriter pw, String subPrefix) {
        forAllWindows((Consumer<WindowState>) new Consumer(pw, subPrefix, new int[1]) {
            private final /* synthetic */ PrintWriter f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int[] f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                DisplayContent.lambda$dumpWindowAnimators$18(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, false);
    }

    static /* synthetic */ void lambda$dumpWindowAnimators$18(PrintWriter pw, String subPrefix, int[] index2, WindowState w) {
        if (!w.toString().contains("hwSingleMode_window")) {
            WindowStateAnimator wAnim = w.mWinAnimator;
            pw.println(subPrefix + "Window #" + index2[0] + ": " + wAnim);
            index2[0] = index2[0] + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void startKeyguardExitOnNonAppWindows(boolean onWallpaper, boolean goingToShade) {
        forAllWindows((Consumer<WindowState>) new Consumer(onWallpaper, goingToShade) {
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                DisplayContent.lambda$startKeyguardExitOnNonAppWindows$19(WindowManagerPolicy.this, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$startKeyguardExitOnNonAppWindows$19(WindowManagerPolicy policy, boolean onWallpaper, boolean goingToShade, WindowState w) {
        if (w.mAppToken == null && policy.canBeHiddenByKeyguardLw(w) && w.wouldBeVisibleIfPolicyIgnored() && !w.isVisible()) {
            w.startAnimation(policy.createHiddenByKeyguardExit(onWallpaper, goingToShade));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkWaitingForWindows() {
        this.mHaveBootMsg = false;
        this.mHaveApp = false;
        this.mHaveWallpaper = false;
        this.mHaveKeyguard = this.mService.mBootAnimationStopped;
        this.mQuickBoot = false;
        WindowState visibleWindow = getWindow(new Predicate() {
            public final boolean test(Object obj) {
                return DisplayContent.lambda$checkWaitingForWindows$20(DisplayContent.this, (WindowState) obj);
            }
        });
        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i(TAG, "******** booted=" + this.mService.mSystemBooted + " msg=" + this.mService.mShowingBootMessages + " haveBoot=" + this.mHaveBootMsg + " haveApp=" + this.mHaveApp + " haveWall=" + this.mHaveWallpaper + " haveKeyguard=" + this.mHaveKeyguard + " mQuickBoot=" + this.mQuickBoot);
        }
        if (visibleWindow != null) {
            return true;
        }
        boolean wallpaperEnabled = this.mService.mContext.getResources().getBoolean(17956968) && this.mService.mContext.getResources().getBoolean(17956916) && !this.mService.mOnlyCore;
        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i(TAG, " wallEnabled=" + wallpaperEnabled);
        }
        if (!this.mService.mSystemBooted && !this.mHaveBootMsg) {
            return true;
        }
        if (!this.mService.mSystemBooted || ((this.mHaveApp || this.mHaveKeyguard) && (!wallpaperEnabled || this.mHaveWallpaper))) {
            return false;
        }
        return true;
    }

    public static /* synthetic */ boolean lambda$checkWaitingForWindows$20(DisplayContent displayContent, WindowState w) {
        if (w.mAttrs.type != 2000 && w.mAttrs.type != 2013 && w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
            return true;
        }
        if (w.isDrawnLw()) {
            if (w.mAttrs.type == 2021) {
                displayContent.mHaveBootMsg = true;
            } else if (w.mAttrs.type == 2 || w.mAttrs.type == 4) {
                displayContent.mHaveApp = true;
            } else if (w.mAttrs.type == 2013) {
                displayContent.mHaveWallpaper = true;
            } else if (w.mAttrs.type == 2000) {
                displayContent.mHaveKeyguard = displayContent.mService.mPolicy.isKeyguardDrawnLw();
            }
        } else if (w.mAttrs.type == 2000) {
            displayContent.mHaveKeyguard = displayContent.mService.mPolicy.isKeyguardDrawnLw();
            displayContent.mQuickBoot = true;
        } else if (w.mAttrs.type == 2013) {
            displayContent.mHaveWallpaper = true;
            displayContent.mQuickBoot = true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateWindowsForAnimator(WindowAnimator animator) {
        this.mTmpWindowAnimator = animator;
        forAllWindows(this.mUpdateWindowsForAnimator, true);
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperForAnimator(WindowAnimator animator) {
        resetAnimationBackgroundAnimator();
        this.mTmpWindow = null;
        this.mTmpWindowAnimator = animator;
        forAllWindows(this.mUpdateWallpaperForAnimator, true);
        if (animator.mWindowDetachedWallpaper != this.mTmpWindow) {
            if (WindowManagerDebugConfig.DEBUG_WALLPAPER) {
                Slog.v(TAG, "Detached wallpaper changed from " + animator.mWindowDetachedWallpaper + " to " + this.mTmpWindow);
            }
            animator.mWindowDetachedWallpaper = this.mTmpWindow;
            animator.mBulkUpdateParams |= 2;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inputMethodClientHasFocus(IInputMethodClient client) {
        boolean z = false;
        WindowState imFocus = computeImeTarget(false);
        if (imFocus == null) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i(TAG, "Desired input method target: " + imFocus);
            Slog.i(TAG, "Current focus: " + this.mService.mCurrentFocus);
            Slog.i(TAG, "Last focus: " + this.mService.mLastFocus);
        }
        IInputMethodClient imeClient = imFocus.mSession.mClient;
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i(TAG, "IM target client: " + imeClient);
            if (imeClient != null) {
                Slog.i(TAG, "IM target client binder: " + imeClient.asBinder());
                Slog.i(TAG, "Requesting client binder: " + client.asBinder());
            }
        }
        if (imeClient != null && imeClient.asBinder() == client.asBinder()) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSecureWindowOnScreen() {
        return getWindow($$Lambda$DisplayContent$5D_ifLpk7QwGe9ZLZynNnDca9g.INSTANCE) != null;
    }

    static /* synthetic */ boolean lambda$hasSecureWindowOnScreen$21(WindowState w) {
        return w.isOnScreen() && (w.mAttrs.flags & 8192) != 0;
    }

    /* access modifiers changed from: package-private */
    public void updateSystemUiVisibility(int visibility, int globalDiff) {
        forAllWindows((Consumer<WindowState>) new Consumer(visibility, globalDiff) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                DisplayContent.lambda$updateSystemUiVisibility$22(this.f$0, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$updateSystemUiVisibility$22(int visibility, int globalDiff, WindowState w) {
        try {
            int curValue = w.mSystemUiVisibility;
            int diff = (curValue ^ visibility) & globalDiff;
            int newValue = ((~diff) & curValue) | (visibility & diff);
            if (newValue != curValue) {
                w.mSeq++;
                w.mSystemUiVisibility = newValue;
            }
            if (newValue != curValue || w.mAttrs.hasSystemUiListeners) {
                w.mClient.dispatchSystemUiVisibilityChanged(w.mSeq, visibility, newValue, diff);
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void onWindowFreezeTimeout() {
        Slog.w(TAG, "Window freeze timeout expired.");
        this.mService.mWindowsFreezingScreen = 2;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            public final void accept(Object obj) {
                DisplayContent.lambda$onWindowFreezeTimeout$23(DisplayContent.this, (WindowState) obj);
            }
        }, true);
        this.mService.mWindowPlacerLocked.performSurfacePlacement();
    }

    public static /* synthetic */ void lambda$onWindowFreezeTimeout$23(DisplayContent displayContent, WindowState w) {
        if (w.getOrientationChanging()) {
            w.orientationChangeTimedOut();
            w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - displayContent.mService.mDisplayFreezeTime);
            Slog.w(TAG, "Force clearing orientation change: " + w);
        }
    }

    /* access modifiers changed from: package-private */
    public void waitForAllWindowsDrawn() {
        WindowManagerPolicy policy = this.mService.mPolicy;
        if (!this.isDefaultDisplay || isCoverOpen()) {
            forAllWindows((Consumer<WindowState>) new Consumer(policy) {
                private final /* synthetic */ WindowManagerPolicy f$1;

                {
                    this.f$1 = r2;
                }

                public final void accept(Object obj) {
                    DisplayContent.lambda$waitForAllWindowsDrawn$25(DisplayContent.this, this.f$1, (WindowState) obj);
                }
            }, true);
            return;
        }
        Slog.w(TAG, "waitForAllWindowsDrawn cover is closed on default Display");
        forAllWindows((Consumer<WindowState>) new Consumer(policy) {
            private final /* synthetic */ WindowManagerPolicy f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                DisplayContent.lambda$waitForAllWindowsDrawn$24(DisplayContent.this, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    public static /* synthetic */ void lambda$waitForAllWindowsDrawn$24(DisplayContent displayContent, WindowManagerPolicy policy, WindowState w) {
        boolean keyguard = policy.isKeyguardHostWindow(w.mAttrs);
        if ((w.isVisibleLw() && (w.mAppToken != null || keyguard)) || w.mAttrs.type == 2100 || w.mAttrs.type == 2101) {
            w.mWinAnimator.mDrawState = 1;
            w.mLastContentInsets.set(-1, -1, -1, -1);
            displayContent.mService.mWaitingForDrawn.add(w);
        }
    }

    public static /* synthetic */ void lambda$waitForAllWindowsDrawn$25(DisplayContent displayContent, WindowManagerPolicy policy, WindowState w) {
        boolean keyguard = policy.isKeyguardHostWindow(w.mAttrs);
        if (!w.isVisibleLw()) {
            return;
        }
        if (w.mAppToken != null || keyguard) {
            w.mWinAnimator.mDrawState = 1;
            w.mLastContentInsets.set(-1, -1, -1, -1);
            displayContent.mService.mWaitingForDrawn.add(w);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean applySurfaceChangesTransaction(boolean recoveringMemory) {
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        WindowSurfacePlacer windowSurfacePlacer = this.mService.mWindowPlacerLocked;
        this.mTmpUpdateAllDrawn.clear();
        int repeats = 0;
        while (true) {
            repeats++;
            if (repeats <= 6) {
                if (this.isDefaultDisplay && (this.pendingLayoutChanges & 4) != 0) {
                    this.mWallpaperController.adjustWallpaperWindows(this);
                }
                if (this.isDefaultDisplay && (this.pendingLayoutChanges & 2) != 0) {
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                        Slog.v(TAG, "Computing new config from layout");
                    }
                    if (this.mService.updateOrientationFromAppTokensLocked(this.mDisplayId)) {
                        setLayoutNeeded();
                        this.mService.mH.obtainMessage(18, Integer.valueOf(this.mDisplayId)).sendToTarget();
                    }
                }
                if ((this.pendingLayoutChanges & 1) != 0) {
                    setLayoutNeeded();
                }
                if (repeats < 4) {
                    performLayout(repeats == 1, false);
                } else {
                    Slog.w(TAG, "Layout repeat skipped after too many iterations");
                }
                this.pendingLayoutChanges = 0;
                if (this.isDefaultDisplay) {
                    this.mService.mPolicy.beginPostLayoutPolicyLw(dw, dh);
                    forAllWindows(this.mApplyPostLayoutPolicy, true);
                    this.pendingLayoutChanges |= this.mService.mPolicy.finishPostLayoutPolicyLw();
                }
                if (this.pendingLayoutChanges == 0) {
                    break;
                }
            } else {
                Slog.w(TAG, "Animation repeat aborted after too many iterations");
                clearLayoutNeeded();
                break;
            }
        }
        this.mTmpApplySurfaceChangesTransactionState.reset();
        this.mTmpRecoveringMemory = recoveringMemory;
        forAllWindows(this.mApplySurfaceChangesTransaction, true);
        prepareSurfaces();
        this.mLastHasContent = this.mTmpApplySurfaceChangesTransactionState.displayHasContent;
        this.mService.mDisplayManagerInternal.setDisplayProperties(this.mDisplayId, this.mLastHasContent, this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate, this.mTmpApplySurfaceChangesTransactionState.preferredModeId, true);
        boolean wallpaperVisible = this.mWallpaperController.isWallpaperVisible();
        if (wallpaperVisible != this.mLastWallpaperVisible) {
            this.mLastWallpaperVisible = wallpaperVisible;
            this.mService.mWallpaperVisibilityListeners.notifyWallpaperVisibilityChanged(this);
        }
        while (!this.mTmpUpdateAllDrawn.isEmpty()) {
            this.mTmpUpdateAllDrawn.removeLast().updateAllDrawn();
        }
        return this.mTmpApplySurfaceChangesTransactionState.focusDisplayed;
    }

    private void updateBounds() {
        calculateBounds(this.mTmpBounds);
        setBounds(this.mTmpBounds);
    }

    private void calculateBounds(Rect out) {
        int orientation = this.mDisplayInfo.rotation;
        boolean rotated = true;
        if (!(orientation == 1 || orientation == 3)) {
            rotated = false;
        }
        int physWidth = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int physHeight = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int width = this.mDisplayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = this.mDisplayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    public void getBounds(Rect out) {
        calculateBounds(out);
    }

    private void getBounds(Rect out, int orientation) {
        getBounds(out);
        int rotationDelta = deltaRotation(this.mDisplayInfo.rotation, orientation);
        if (rotationDelta == 1 || rotationDelta == 3) {
            createRotationMatrix(rotationDelta, (float) this.mBaseDisplayWidth, (float) this.mBaseDisplayHeight, this.mTmpMatrix);
            this.mTmpRectF.set(out);
            this.mTmpMatrix.mapRect(this.mTmpRectF);
            this.mTmpRectF.round(out);
        }
    }

    /* access modifiers changed from: package-private */
    public void performLayout(boolean initial, boolean updateInputWindows) {
        if (isLayoutNeeded()) {
            clearLayoutNeeded();
            int dw = this.mDisplayInfo.logicalWidth;
            int dh = this.mDisplayInfo.logicalHeight;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v(TAG, "-------------------------------------");
                Slog.v(TAG, "performLayout: needed=" + isLayoutNeeded() + " dw=" + dw + " dh=" + dh);
            }
            this.mDisplayFrames.onDisplayInfoUpdated(this.mDisplayInfo, calculateDisplayCutoutForRotation(this.mDisplayInfo.rotation));
            this.mDisplayFrames.mRotation = this.mRotation;
            this.mService.mHwWMSEx.setNaviBarFlag();
            this.mService.mPolicy.beginLayoutLw(this.mDisplayFrames, getConfiguration().uiMode);
            if (this.isDefaultDisplay) {
                this.mService.mSystemDecorLayer = this.mService.mPolicy.getSystemDecorLayerLw();
                this.mService.mScreenRect.set(0, 0, dw, dh);
            }
            int seq = this.mLayoutSeq + 1;
            if (seq < 0) {
                seq = 0;
            }
            this.mLayoutSeq = seq;
            this.mTmpWindow = null;
            this.mTmpInitial = initial;
            forAllWindows(this.mPerformLayout, true);
            this.mTmpWindow2 = this.mTmpWindow;
            this.mTmpWindow = null;
            forAllWindows(this.mPerformLayoutAttached, true);
            this.mService.mInputMonitor.layoutInputConsumers(dw, dh);
            this.mService.mInputMonitor.setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                this.mService.mInputMonitor.updateInputWindowsLw(false);
            }
            this.mService.mH.sendEmptyMessage(41);
        }
    }

    /* access modifiers changed from: package-private */
    public Bitmap screenshotDisplayLocked(Bitmap.Config config) {
        if (!this.mService.mPolicy.isScreenOn()) {
            return null;
        }
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        if (dw <= 0 || dh <= 0) {
            return null;
        }
        Rect frame = new Rect(0, 0, dw, dh);
        int rot = this.mDisplay.getRotation();
        int i = 3;
        if (rot == 1 || rot == 3) {
            if (rot != 1) {
                i = 1;
            }
            rot = i;
        }
        int rot2 = rot;
        convertCropForSurfaceFlinger(frame, rot2, dw, dh);
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
        Bitmap bitmap = SurfaceControl.screenshot(frame, dw, dh, 0, 1, screenRotationAnimation != null && screenRotationAnimation.isAnimating(), rot2);
        if (bitmap == null) {
            Slog.w(TAG, "Failed to take screenshot");
            return null;
        }
        Bitmap ret = bitmap.createAshmemBitmap(config);
        bitmap.recycle();
        return ret;
    }

    protected static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        if (rot == 1) {
            int tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            int tmp2 = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp2;
            int tmp3 = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp3;
        } else if (rot == 3) {
            int tmp4 = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp4;
        }
    }

    /* access modifiers changed from: package-private */
    public void onSeamlessRotationTimeout() {
        this.mTmpWindow = null;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            public final void accept(Object obj) {
                DisplayContent.lambda$onSeamlessRotationTimeout$26(DisplayContent.this, (WindowState) obj);
            }
        }, true);
        if (this.mTmpWindow != null) {
            this.mService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    public static /* synthetic */ void lambda$onSeamlessRotationTimeout$26(DisplayContent displayContent, WindowState w) {
        if (w.mSeamlesslyRotated) {
            displayContent.mTmpWindow = w;
            w.setDisplayLayoutNeeded();
            displayContent.mService.markForSeamlessRotation(w, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setExitingTokensHasVisible(boolean hasVisible) {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            this.mExitingTokens.get(i).hasVisible = hasVisible;
        }
        this.mTaskStackContainers.setExitingTokensHasVisible(hasVisible);
    }

    /* access modifiers changed from: package-private */
    public void removeExistingTokensIfPossible() {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            if (!this.mExitingTokens.get(i).hasVisible) {
                this.mExitingTokens.remove(i);
            }
        }
        this.mTaskStackContainers.removeExistingAppTokensIfPossible();
    }

    /* access modifiers changed from: package-private */
    public void onDescendantOverrideConfigurationChanged() {
        setLayoutNeeded();
        this.mService.requestTraversal();
    }

    /* access modifiers changed from: package-private */
    public boolean okToDisplay() {
        boolean z = false;
        if (this.mDisplayId == 0) {
            if (!this.mService.mDisplayFrozen && this.mService.mDisplayEnabled && this.mService.mPolicy.isScreenOn()) {
                z = true;
            }
            return z;
        }
        if (this.mDisplayInfo.state == 2) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean okToAnimate() {
        return okToDisplay() && (this.mDisplayId != 0 || this.mService.mPolicy.okToAnimate());
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeSurface(SurfaceSession s) {
        return this.mService.makeSurfaceBuilder(s).setParent(this.mWindowingLayer);
    }

    /* access modifiers changed from: package-private */
    public SurfaceSession getSession() {
        return this.mSession;
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeChildSurface(WindowContainer child) {
        SurfaceControl.Builder b = this.mService.makeSurfaceBuilder(child != null ? child.getSession() : getSession());
        b.setSize(this.mSurfaceSize, this.mSurfaceSize);
        if (child == null) {
            return b;
        }
        return b.setName(child.getName()).setParent(this.mWindowingLayer);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeOverlay() {
        return this.mService.makeSurfaceBuilder(this.mSession).setParent(this.mOverlayLayer);
    }

    /* access modifiers changed from: package-private */
    public void reparentToOverlay(SurfaceControl.Transaction transaction, SurfaceControl surface) {
        transaction.reparent(surface, this.mOverlayLayer.getHandle());
    }

    /* access modifiers changed from: package-private */
    public void applyMagnificationSpec(MagnificationSpec spec) {
        if (((double) spec.scale) != 1.0d) {
            this.mMagnificationSpec = spec;
        } else {
            this.mMagnificationSpec = null;
        }
        applyMagnificationSpec(getPendingTransaction(), spec);
        getPendingTransaction().apply();
    }

    /* access modifiers changed from: package-private */
    public void reapplyMagnificationSpec() {
        if (this.mMagnificationSpec != null) {
            applyMagnificationSpec(getPendingTransaction(), this.mMagnificationSpec);
        }
    }

    /* access modifiers changed from: package-private */
    public void onParentSet() {
    }

    /* access modifiers changed from: package-private */
    public void assignChildLayers(SurfaceControl.Transaction t) {
        this.mBelowAppWindowsContainers.assignLayer(t, 0);
        this.mTaskStackContainers.assignLayer(t, 1);
        this.mAboveAppWindowsContainers.assignLayer(t, 2);
        WindowState imeTarget = this.mService.mInputMethodTarget;
        boolean needAssignIme = true;
        if (imeTarget != null && !imeTarget.inSplitScreenWindowingMode() && !imeTarget.mToken.isAppAnimating() && imeTarget.getSurfaceControl() != null) {
            this.mImeWindowsContainers.assignRelativeLayer(t, imeTarget.getSurfaceControl(), 1);
            needAssignIme = false;
        }
        this.mBelowAppWindowsContainers.assignChildLayers(t);
        this.mTaskStackContainers.assignChildLayers(t);
        this.mAboveAppWindowsContainers.assignChildLayers(t, needAssignIme ? this.mImeWindowsContainers : null);
        this.mImeWindowsContainers.assignChildLayers(t);
    }

    /* access modifiers changed from: package-private */
    public void assignRelativeLayerForImeTargetChild(SurfaceControl.Transaction t, WindowContainer child) {
        child.assignRelativeLayer(t, this.mImeWindowsContainers.getSurfaceControl(), 1);
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaces() {
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
            screenRotationAnimation.getEnterTransformation().getMatrix().getValues(this.mTmpFloats);
            this.mPendingTransaction.setMatrix(this.mWindowingLayer, this.mTmpFloats[0], this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4]);
            this.mPendingTransaction.setPosition(this.mWindowingLayer, this.mTmpFloats[2], this.mTmpFloats[5]);
            this.mPendingTransaction.setAlpha(this.mWindowingLayer, screenRotationAnimation.getEnterTransformation().getAlpha());
        }
        int i = this.mTaskStackContainers.getChildCount() - 1;
        while (true) {
            int i2 = i;
            if (i2 >= 0) {
                TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i2);
                stack.updateSurfacePosition();
                stack.updateSurfaceSize(stack.getPendingTransaction());
                i = i2 - 1;
            } else {
                super.prepareSurfaces();
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void assignStackOrdering() {
        this.mTaskStackContainers.assignStackOrdering(getPendingTransaction());
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateImeTarget() {
        this.mDeferUpdateImeTargetCount++;
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateImeTarget() {
        if (this.mDeferUpdateImeTargetCount != 0) {
            this.mDeferUpdateImeTargetCount--;
            if (this.mDeferUpdateImeTargetCount == 0) {
                computeImeTarget(true);
            }
        }
    }

    private boolean canUpdateImeTarget() {
        return this.mDeferUpdateImeTargetCount == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean getLastHasContent() {
        return this.mLastHasContent;
    }

    public TaskStack getCoordinationPrimaryStackIgnoringVisibility() {
        return this.mTaskStackContainers.getCoordinationPrimaryStack();
    }
}
