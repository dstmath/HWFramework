package com.android.server.wm;

import android.content.res.Configuration;
import android.iawareperf.UniPerf;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.SurfaceControl;
import android.view.WindowManager;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.HwServiceFactory;
import com.android.server.zrhung.IZRHungService;
import com.huawei.pgmng.log.LogPower;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/* access modifiers changed from: package-private */
public class RootWindowContainer extends WindowContainer<DisplayContent> implements ConfigurationContainerListener {
    private static final int SET_SCREEN_BRIGHTNESS_OVERRIDE = 1;
    private static final int SET_USER_ACTIVITY_TIMEOUT = 2;
    private static final String TAG = "WindowManager";
    private static final Consumer<WindowState> sRemoveReplacedWindowsConsumer = $$Lambda$RootWindowContainer$Vvv8jzH2oSE9eakZwTuKd5NpsU.INSTANCE;
    private float mAppBrightnessLast = -1.0f;
    private String mAppBrightnessPackageName;
    private String mAppBrightnessPackageNameLast = "";
    private final Consumer<WindowState> mCloseSystemDialogsConsumer = new Consumer() {
        /* class com.android.server.wm.$$Lambda$RootWindowContainer$qT2ficAmvrvFcBdiJIGNKxJ8Z9Q */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            RootWindowContainer.this.lambda$new$0$RootWindowContainer((WindowState) obj);
        }
    };
    private String mCloseSystemDialogsReason;
    private HwServiceFactory.IDisplayEffectMonitor mDisplayEffectMonitor;
    private final SurfaceControl.Transaction mDisplayTransaction = new SurfaceControl.Transaction();
    private final Handler mHandler;
    private Session mHoldScreen = null;
    WindowState mHoldScreenWindow = null;
    private Object mLastWindowFreezeSource = null;
    private boolean mObscureApplicationContentOnSecondaryDisplays = false;
    WindowState mObscuringWindow = null;
    boolean mOrientationChangeComplete = true;
    private RootActivityContainer mRootActivityContainer;
    private float mScreenBrightness = -1.0f;
    private boolean mSustainedPerformanceModeCurrent = false;
    private boolean mSustainedPerformanceModeEnabled = false;
    final HashMap<Integer, AppWindowToken> mTopFocusedAppByProcess = new HashMap<>();
    int mTopFocusedDisplayId = -1;
    private boolean mUpdateRotation = false;
    private long mUserActivityTimeout = -1;
    boolean mWallpaperActionPending = false;

    public /* synthetic */ void lambda$new$0$RootWindowContainer(WindowState w) {
        if (w.mHasSurface) {
            try {
                w.mClient.closeSystemDialogs(this.mCloseSystemDialogsReason);
            } catch (RemoteException e) {
            }
        }
    }

    static /* synthetic */ void lambda$static$1(WindowState w) {
        AppWindowToken aToken = w.mAppToken;
        if (aToken != null) {
            aToken.removeReplacedWindowIfNeeded(w);
        }
    }

    RootWindowContainer(WindowManagerService service) {
        super(service);
        this.mHandler = new MyHandler(service.mH.getLooper());
        this.mDisplayEffectMonitor = HwServiceFactory.getDisplayEffectMonitor(this.mWmService.mContext);
    }

    /* access modifiers changed from: package-private */
    public void setRootActivityContainer(RootActivityContainer container) {
        this.mRootActivityContainer = container;
        if (container != null) {
            container.registerConfigurationChangeListener(this);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows) {
        this.mTopFocusedAppByProcess.clear();
        boolean changed = false;
        int topFocusedDisplayId = -1;
        int i = this.mChildren.size();
        while (true) {
            i--;
            if (i < 0) {
                break;
            }
            DisplayContent dc = (DisplayContent) this.mChildren.get(i);
            changed |= dc.updateFocusedWindowLocked(mode, updateInputWindows, topFocusedDisplayId);
            WindowState newFocus = dc.mCurrentFocus;
            if (newFocus != null) {
                int pidOfNewFocus = newFocus.mSession.mPid;
                if (this.mTopFocusedAppByProcess.get(Integer.valueOf(pidOfNewFocus)) == null) {
                    this.mTopFocusedAppByProcess.put(Integer.valueOf(pidOfNewFocus), newFocus.mAppToken);
                }
                if (topFocusedDisplayId == -1) {
                    topFocusedDisplayId = dc.getDisplayId();
                }
            } else if (topFocusedDisplayId == -1 && dc.mFocusedApp != null) {
                topFocusedDisplayId = dc.getDisplayId();
            }
        }
        if (topFocusedDisplayId == -1) {
            topFocusedDisplayId = 0;
        }
        if (this.mTopFocusedDisplayId != topFocusedDisplayId) {
            Slog.i(TAG, "updateFocusedWindowLocked: the focused display is changed from " + this.mTopFocusedDisplayId + " to " + topFocusedDisplayId);
            this.mTopFocusedDisplayId = topFocusedDisplayId;
            this.mWmService.mInputManager.setFocusedDisplay(topFocusedDisplayId);
            this.mWmService.mPolicy.setTopFocusedDisplay(topFocusedDisplayId);
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v(TAG, "New topFocusedDisplayId=" + topFocusedDisplayId);
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getTopFocusedDisplayContent() {
        DisplayContent dc = getDisplayContent(this.mTopFocusedDisplayId);
        return dc != null ? dc : getDisplayContent(0);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onChildPositionChanged() {
        this.mWmService.updateFocusedWindowLocked(0, !this.mWmService.mPerDisplayFocusEnabled);
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getDisplayContent(int displayId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent current = (DisplayContent) this.mChildren.get(i);
            if (current.getDisplayId() == displayId) {
                return current;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent createDisplayContent(Display display, ActivityDisplay activityDisplay) {
        int displayId = display.getDisplayId();
        DisplayContent existing = getDisplayContent(displayId);
        if (existing != null) {
            existing.mAcitvityDisplay = activityDisplay;
            existing.initializeDisplayOverrideConfiguration();
            return existing;
        }
        DisplayContent dc = HwServiceFactory.createDisplayContent(display, this.mWmService, activityDisplay);
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v(TAG, "Adding display=" + display);
        }
        this.mWmService.mDisplayWindowSettings.applySettingsToDisplayLocked(dc);
        dc.initializeDisplayOverrideConfiguration();
        if (this.mWmService.mDisplayManagerInternal != null) {
            this.mWmService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, dc.getDisplayInfo());
            dc.configureDisplayPolicy();
        }
        this.mWmService.reconfigureDisplayLocked(dc);
        return dc;
    }

    /* access modifiers changed from: package-private */
    public void onSettingsRetrieved() {
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx);
            if (this.mWmService.mDisplayWindowSettings.updateSettingsForDisplay(displayContent)) {
                displayContent.initializeDisplayOverrideConfiguration();
                this.mWmService.reconfigureDisplayLocked(displayContent);
                if (displayContent.isDefaultDisplay) {
                    this.mWmService.mAtmService.updateConfigurationLocked(this.mWmService.computeNewConfiguration(displayContent.getDisplayId()), null, false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutNeeded() {
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            if (((DisplayContent) this.mChildren.get(displayNdx)).isLayoutNeeded()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void getWindowsByName(ArrayList<WindowState> output, String name) {
        int objectId = 0;
        try {
            objectId = Integer.parseInt(name, 16);
            name = null;
        } catch (RuntimeException e) {
        }
        getWindowsByName(output, name, objectId);
    }

    private void getWindowsByName(ArrayList<WindowState> output, String name, int objectId) {
        forAllWindows((Consumer<WindowState>) new Consumer(name, output, objectId) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$O6gArs92KbWUhitra1og4WTg69c */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ ArrayList f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$getWindowsByName$2(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$getWindowsByName$2(String name, ArrayList output, int objectId, WindowState w) {
        if (name != null) {
            if (w.mAttrs.getTitle().toString().contains(name)) {
                output.add(w);
            }
        } else if (System.identityHashCode(w) == objectId) {
            output.add(w);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAnyNonToastWindowVisibleForUid(int callingUid) {
        return forAllWindows((ToBooleanFunction<WindowState>) new ToBooleanFunction(callingUid) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$IlD1lD49ui7gQmU2NkxgnXIhlOo */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final boolean apply(Object obj) {
                return RootWindowContainer.lambda$isAnyNonToastWindowVisibleForUid$3(this.f$0, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ boolean lambda$isAnyNonToastWindowVisibleForUid$3(int callingUid, WindowState w) {
        return w.getOwningUid() == callingUid && w.mAttrs.type != 2005 && w.mAttrs.type != 3 && w.isVisibleNow();
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppWindowToken(IBinder binder) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken atoken = ((DisplayContent) this.mChildren.get(i)).getAppWindowToken(binder);
            if (atoken != null) {
                return atoken;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowToken getWindowToken(IBinder binder) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            WindowToken wtoken = ((DisplayContent) this.mChildren.get(i)).getWindowToken(binder);
            if (wtoken != null) {
                return wtoken;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public DisplayContent getWindowTokenDisplay(WindowToken token) {
        if (token == null) {
            return null;
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent dc = (DisplayContent) this.mChildren.get(i);
            if (dc.getWindowToken(token.token) == token) {
                return dc;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setDisplayOverrideConfigurationIfNeeded(Configuration newConfiguration, DisplayContent displayContent) {
        if (displayContent.getRequestedOverrideConfiguration().diff(newConfiguration) != 0) {
            displayContent.onRequestedOverrideConfigurationChanged(newConfiguration);
            if (displayContent.getDisplayId() == 0) {
                setGlobalConfigurationIfNeeded(newConfiguration);
            }
        }
    }

    private void setGlobalConfigurationIfNeeded(Configuration newConfiguration) {
        if (getConfiguration().diff(newConfiguration) != 0) {
            onConfigurationChanged(newConfiguration);
        }
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        prepareFreezingTaskBounds();
        super.onConfigurationChanged(newParentConfig);
    }

    private void prepareFreezingTaskBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack(int windowingMode, int activityType) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            TaskStack stack = ((DisplayContent) this.mChildren.get(i)).getStack(windowingMode, activityType);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setSecureSurfaceState(int userId, boolean disabled) {
        forAllWindows((Consumer<WindowState>) new Consumer(userId, disabled) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$vMW2dyMvZQ0PDhptvNKN5WXpK_w */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$setSecureSurfaceState$4(this.f$0, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$setSecureSurfaceState$4(int userId, boolean disabled, WindowState w) {
        if (w.mHasSurface && userId == UserHandle.getUserId(w.mOwnerUid)) {
            w.mWinAnimator.setSecureLocked(disabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateHiddenWhileSuspendedState(ArraySet<String> packages, boolean suspended) {
        forAllWindows((Consumer<WindowState>) new Consumer(packages, suspended) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$jHLZ5ssJOPMd9KJ4tf6FHZ8ZLXI */
            private final /* synthetic */ ArraySet f$0;
            private final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$updateHiddenWhileSuspendedState$5(this.f$0, this.f$1, (WindowState) obj);
            }
        }, false);
    }

    static /* synthetic */ void lambda$updateHiddenWhileSuspendedState$5(ArraySet packages, boolean suspended, WindowState w) {
        if (packages.contains(w.getOwningPackage())) {
            w.setHiddenWhileSuspended(suspended);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateAppOpsState() {
        forAllWindows((Consumer<WindowState>) $$Lambda$RootWindowContainer$auMc5HUrsvttHP3CYY9dttuuvi8.INSTANCE, false);
    }

    static /* synthetic */ boolean lambda$canShowStrictModeViolation$7(int pid, WindowState w) {
        return w.mSession.mPid == pid && w.isVisibleLw();
    }

    /* access modifiers changed from: package-private */
    public boolean canShowStrictModeViolation(int pid) {
        return getWindow(new Predicate(pid) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$3EhML4qLwBt5KlZ9KF4rJB08Ac */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return RootWindowContainer.lambda$canShowStrictModeViolation$7(this.f$0, (WindowState) obj);
            }
        }) != null;
    }

    /* access modifiers changed from: package-private */
    public void closeSystemDialogs(String reason) {
        this.mCloseSystemDialogsReason = reason;
        forAllWindows(this.mCloseSystemDialogsConsumer, false);
    }

    /* access modifiers changed from: package-private */
    public void removeReplacedWindows() {
        this.mWmService.openSurfaceTransaction();
        try {
            forAllWindows(sRemoveReplacedWindowsConsumer, true);
        } finally {
            this.mWmService.closeSurfaceTransaction("removeReplacedWindows");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasPendingLayoutChanges(WindowAnimator animator) {
        boolean hasChanges = false;
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            int pendingChanges = animator.getPendingLayoutChanges(((DisplayContent) this.mChildren.get(i)).getDisplayId());
            if ((pendingChanges & 4) != 0) {
                animator.mBulkUpdateParams |= 8;
            }
            if (pendingChanges != 0) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v29 */
    /* JADX WARN: Type inference failed for: r0v30, types: [int] */
    /* access modifiers changed from: package-private */
    public boolean reclaimSomeSurfaceMemory(WindowStateAnimator winAnimator, String operation, boolean secure) {
        Throwable th;
        WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
        boolean leakedSurface = false;
        boolean killedApps = false;
        boolean z = false;
        EventLog.writeEvent(31000, winAnimator.mWin.toString(), Integer.valueOf(winAnimator.mSession.mPid), operation);
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            Slog.i(TAG, "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mChildren.size();
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                leakedSurface |= ((DisplayContent) this.mChildren.get(displayNdx)).destroyLeakedSurfaces();
            }
            if (!leakedSurface) {
                Slog.w(TAG, "No leaked surfaces; killing applications!");
                SparseIntArray pidCandidates = new SparseIntArray();
                boolean killedApps2 = false;
                int displayNdx2 = 0;
                while (displayNdx2 < numDisplays) {
                    try {
                        ((DisplayContent) this.mChildren.get(displayNdx2)).forAllWindows(new Consumer(pidCandidates) {
                            /* class com.android.server.wm.$$Lambda$RootWindowContainer$yUm1jRpi8w1ifmlGbgy1F1iTxu4 */
                            private final /* synthetic */ SparseIntArray f$1;

                            {
                                this.f$1 = r2;
                            }

                            @Override // java.util.function.Consumer
                            public final void accept(Object obj) {
                                RootWindowContainer.this.lambda$reclaimSomeSurfaceMemory$8$RootWindowContainer(this.f$1, (WindowState) obj);
                            }
                        }, z);
                        if (pidCandidates.size() > 0) {
                            int[] pids = new int[pidCandidates.size()];
                            int i = z;
                            while (i < pids.length) {
                                pids[i == true ? 1 : 0] = pidCandidates.keyAt(i == true ? 1 : 0);
                                i++;
                            }
                            try {
                                try {
                                    if (this.mWmService.mActivityManager.killPids(pids, "Free memory", secure)) {
                                        killedApps2 = true;
                                    }
                                } catch (RemoteException e) {
                                } catch (Throwable th2) {
                                    th = th2;
                                    Binder.restoreCallingIdentity(callingIdentity);
                                    throw th;
                                }
                            } catch (RemoteException e2) {
                            }
                        }
                        displayNdx2++;
                        z = false;
                    } catch (Throwable th3) {
                        th = th3;
                        Binder.restoreCallingIdentity(callingIdentity);
                        throw th;
                    }
                }
                killedApps = killedApps2;
            }
            if (leakedSurface || killedApps) {
                try {
                    Slog.w(TAG, "Looks like we have reclaimed some memory, clearing surface for retry.");
                    if (surfaceController != null) {
                        winAnimator.destroySurface();
                        if (winAnimator.mWin.mAppToken != null) {
                            winAnimator.mWin.mAppToken.removeStartingWindow();
                        }
                    }
                    try {
                        winAnimator.mWin.mClient.dispatchGetNewSurface();
                    } catch (RemoteException e3) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    Binder.restoreCallingIdentity(callingIdentity);
                    throw th;
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            if (leakedSurface || killedApps) {
                return true;
            }
            return false;
        } catch (Throwable th5) {
            th = th5;
            Binder.restoreCallingIdentity(callingIdentity);
            throw th;
        }
    }

    public /* synthetic */ void lambda$reclaimSomeSurfaceMemory$8$RootWindowContainer(SparseIntArray pidCandidates, WindowState w) {
        if (!this.mWmService.mForceRemoves.contains(w)) {
            WindowStateAnimator wsa = w.mWinAnimator;
            if (wsa.mSurfaceController != null) {
                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void performSurfacePlacement(boolean recoveringMemory) {
        Trace.traceBegin(32, "performSurfacePlacement");
        try {
            performSurfacePlacementNoTrace(recoveringMemory);
        } finally {
            Trace.traceEnd(32);
        }
    }

    /* access modifiers changed from: package-private */
    public void performSurfacePlacementNoTrace(boolean recoveringMemory) {
        if (this.mWmService.mFocusMayChange) {
            this.mWmService.mFocusMayChange = false;
            this.mWmService.updateFocusedWindowLocked(3, false);
        }
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ((DisplayContent) this.mChildren.get(displayNdx)).setExitingTokensHasVisible(false);
        }
        this.mHoldScreen = null;
        this.mScreenBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mObscureApplicationContentOnSecondaryDisplays = false;
        this.mSustainedPerformanceModeCurrent = false;
        this.mWmService.mTransactionSequence++;
        DisplayContent defaultDisplay = this.mWmService.getDefaultDisplayContentLocked();
        WindowSurfacePlacer surfacePlacer = this.mWmService.mWindowPlacerLocked;
        Trace.traceBegin(32, "applySurfaceChanges");
        this.mWmService.openSurfaceTransaction();
        try {
            applySurfaceChangesTransaction(recoveringMemory);
        } catch (RuntimeException e) {
            Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
        } catch (Throwable th) {
            this.mWmService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
            Trace.traceEnd(32);
            throw th;
        }
        this.mWmService.closeSurfaceTransaction("performLayoutAndPlaceSurfaces");
        Trace.traceEnd(32);
        this.mWmService.mAnimator.executeAfterPrepareSurfacesRunnables();
        checkAppTransitionReady(surfacePlacer);
        RecentsAnimationController recentsAnimationController = this.mWmService.getRecentsAnimationController();
        if (recentsAnimationController != null) {
            recentsAnimationController.checkAnimationReady(defaultDisplay.mWallpaperController);
        }
        for (int displayNdx2 = 0; displayNdx2 < numDisplays; displayNdx2++) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx2);
            if (displayContent.mWallpaperMayChange) {
                if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                    Slog.v(TAG, "Wallpaper may change!  Adjusting");
                }
                displayContent.pendingLayoutChanges |= 4;
            }
        }
        if (this.mWmService.mFocusMayChange) {
            this.mWmService.mFocusMayChange = false;
            this.mWmService.updateFocusedWindowLocked(2, false);
        }
        if (isLayoutNeeded()) {
            defaultDisplay.pendingLayoutChanges |= 1;
        }
        handleResizingWindows();
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION && this.mWmService.mDisplayFrozen) {
            Slog.v(TAG, "With display frozen, orientationChangeComplete=" + this.mOrientationChangeComplete);
        }
        int brightness = -1;
        if (this.mOrientationChangeComplete) {
            if (this.mWmService.mIsPerfBoost) {
                this.mWmService.mIsPerfBoost = false;
                UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{-1});
            }
            LogPower.push(130, Integer.toString(this.mWmService.getDefaultDisplayRotation()));
            if (this.mWmService.mWindowsFreezingScreen != 0) {
                this.mWmService.mWindowsFreezingScreen = 0;
                this.mWmService.mLastFinishedFreezeSource = this.mLastWindowFreezeSource;
                this.mWmService.mH.removeMessages(11);
            }
            if (this.mWmService.mDisplayFrozen) {
                Slog.i(TAG, "orientation change is complete, call stopFreezingDisplayLocked");
            }
            this.mWmService.stopFreezingDisplayLocked();
            if (!this.mWmService.mDisplayFrozen && !HwPCUtils.isPcCastModeInServer()) {
                reLayoutIfNeed();
            }
        }
        int i = this.mWmService.mDestroySurface.size();
        if (i > 0) {
            do {
                i--;
                WindowState win = this.mWmService.mDestroySurface.get(i);
                win.mDestroying = false;
                DisplayContent displayContent2 = win.getDisplayContent();
                if (displayContent2.mInputMethodWindow == win) {
                    displayContent2.setInputMethodWindowLocked(null);
                }
                if (displayContent2.mWallpaperController.isWallpaperTarget(win)) {
                    displayContent2.pendingLayoutChanges |= 4;
                }
                win.destroySurfaceUnchecked();
                win.mWinAnimator.destroyPreservedSurfaceLocked();
            } while (i > 0);
            this.mWmService.mDestroySurface.clear();
        }
        for (int displayNdx3 = 0; displayNdx3 < numDisplays; displayNdx3++) {
            ((DisplayContent) this.mChildren.get(displayNdx3)).removeExistingTokensIfPossible();
        }
        for (int displayNdx4 = 0; displayNdx4 < numDisplays; displayNdx4++) {
            DisplayContent displayContent3 = (DisplayContent) this.mChildren.get(displayNdx4);
            if (displayContent3.pendingLayoutChanges != 0) {
                displayContent3.setLayoutNeeded();
            }
        }
        this.mWmService.setHoldScreenLocked(this.mHoldScreen);
        if (!this.mWmService.mDisplayFrozen) {
            float f = this.mScreenBrightness;
            if (f >= 0.0f) {
                brightness = toBrightnessOverride(f);
            }
            float f2 = this.mScreenBrightness;
            if (f2 < 0.0f) {
                this.mAppBrightnessPackageName = "android";
                sendBrightnessToMonitor(-1.0f, this.mAppBrightnessPackageName);
            } else {
                sendBrightnessToMonitor(f2, this.mAppBrightnessPackageName);
            }
            this.mHandler.obtainMessage(1, brightness, 0).sendToTarget();
            this.mHandler.obtainMessage(2, Long.valueOf(this.mUserActivityTimeout)).sendToTarget();
        }
        boolean z = this.mSustainedPerformanceModeCurrent;
        if (z != this.mSustainedPerformanceModeEnabled) {
            this.mSustainedPerformanceModeEnabled = z;
            this.mWmService.mPowerManagerInternal.powerHint(6, this.mSustainedPerformanceModeEnabled ? 1 : 0);
        }
        if (this.mUpdateRotation) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.d(TAG, "Performing post-rotate rotation");
            }
            this.mUpdateRotation = updateRotationUnchecked();
        }
        if (this.mWmService.mWaitingForDrawnCallback != null || (this.mOrientationChangeComplete && !isLayoutNeeded() && !this.mUpdateRotation)) {
            this.mWmService.checkDrawnWindowsLocked();
        }
        int N = this.mWmService.mPendingRemove.size();
        if (N > 0) {
            if (this.mWmService.mPendingRemoveTmp.length < N) {
                this.mWmService.mPendingRemoveTmp = new WindowState[(N + 10)];
            }
            this.mWmService.mPendingRemove.toArray(this.mWmService.mPendingRemoveTmp);
            this.mWmService.mPendingRemove.clear();
            ArrayList<DisplayContent> displayList = new ArrayList<>();
            for (int i2 = 0; i2 < N; i2++) {
                WindowState w = this.mWmService.mPendingRemoveTmp[i2];
                if (w.getParentWindow() == null || !w.getParentWindow().mIsRemovingIfPossible) {
                    w.removeImmediately();
                    DisplayContent displayContent4 = w.getDisplayContent();
                    if (displayContent4 != null && !displayList.contains(displayContent4)) {
                        displayList.add(displayContent4);
                    }
                }
            }
            for (int j = displayList.size() - 1; j >= 0; j--) {
                displayList.get(j).assignWindowLayers(true);
            }
        }
        for (int displayNdx5 = this.mChildren.size() - 1; displayNdx5 >= 0; displayNdx5--) {
            ((DisplayContent) this.mChildren.get(displayNdx5)).checkCompleteDeferredRemoval();
        }
        forAllDisplays($$Lambda$RootWindowContainer$7XcqfZjQLAbjpIyed3iDnVtZro4.INSTANCE);
        this.mWmService.enableScreenIfNeededLocked();
        this.mWmService.scheduleAnimationLocked();
    }

    static /* synthetic */ void lambda$performSurfacePlacementNoTrace$9(DisplayContent dc) {
        dc.getInputMonitor().updateInputWindowsLw(true);
        dc.updateSystemGestureExclusion();
        dc.updateTouchExcludeRegion();
    }

    private void checkAppTransitionReady(WindowSurfacePlacer surfacePlacer) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent curDisplay = (DisplayContent) this.mChildren.get(i);
            if (curDisplay.mAppTransition.isReady()) {
                curDisplay.mAppTransitionController.handleAppTransitionReady();
            }
            if (curDisplay.mAppTransition.isRunning() && !curDisplay.isAppAnimating()) {
                curDisplay.handleAnimatingStoppedAndTransition();
            }
        }
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory) {
        this.mHoldScreenWindow = null;
        this.mObscuringWindow = null;
        DisplayInfo defaultInfo = this.mWmService.getDefaultDisplayContentLocked().getDisplayInfo();
        int defaultDw = defaultInfo.logicalWidth;
        int defaultDh = defaultInfo.logicalHeight;
        if (this.mWmService.mWatermark != null) {
            this.mWmService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        if (this.mWmService.mStrictModeFlash != null) {
            this.mWmService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (this.mWmService.mCircularDisplayMask != null) {
            this.mWmService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, this.mWmService.getDefaultDisplayRotation());
        }
        if (this.mWmService.mEmulatorDisplayOverlay != null) {
            this.mWmService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh, this.mWmService.getDefaultDisplayRotation());
        }
        int count = this.mChildren.size();
        for (int j = 0; j < count; j++) {
            ((DisplayContent) this.mChildren.get(j)).applySurfaceChangesTransaction(recoveringMemory);
        }
        this.mWmService.mDisplayManagerInternal.performTraversal(this.mDisplayTransaction);
        SurfaceControl.mergeToGlobalTransaction(this.mDisplayTransaction);
    }

    private void handleResizingWindows() {
        for (int i = this.mWmService.mResizingWindows.size() - 1; i >= 0; i--) {
            WindowState win = this.mWmService.mResizingWindows.get(i);
            boolean isAppFreezing = win.mAppFreezing;
            boolean isWaitingForConfig = win.getDisplayContent().mWaitingForConfig;
            if (needStopFreezingScreen(win)) {
                win.mAppToken.stopFreezingScreen(false, true);
            }
            if (isAppFreezing || isWaitingForConfig) {
                Slog.i(TAG, "handleResizingWindows : win = " + win + " freezing = " + isAppFreezing + " waitForConfig = " + isWaitingForConfig);
            } else {
                win.reportResized();
                this.mWmService.mResizingWindows.remove(i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleNotObscuredLocked(WindowState w, boolean obscured, boolean syswin) {
        WindowManager.LayoutParams attrs = w.mAttrs;
        int attrFlags = attrs.flags;
        boolean onScreen = w.isOnScreen();
        boolean canBeSeen = w.isDisplayedLw() || w.isVirtualDisplayedLw();
        int privateflags = attrs.privateFlags;
        boolean displayHasContent = false;
        if (w.mHasSurface && onScreen && !syswin && w.mAttrs.userActivityTimeout >= 0 && this.mUserActivityTimeout < 0) {
            if ((w.mAttrs.privateFlags & 1024) == 0 || !this.mWmService.mDestroySurface.contains(w)) {
                this.mUserActivityTimeout = w.mAttrs.userActivityTimeout;
            } else {
                Slog.e(TAG, "do not set userActivityTimeout this time");
            }
        }
        if (w.mHasSurface && canBeSeen) {
            if ((attrFlags & 128) != 0) {
                if (!this.mWmService.mPolicy.isKeyguardShowingOrOccluded() || !((w.mAppToken == null || w.mAppToken.mActivityRecord == null || !w.mAppToken.mActivityRecord.mShowWhenLocked) && (attrs.flags & 524288) == 0)) {
                    this.mHoldScreen = w.mSession;
                    this.mHoldScreenWindow = w;
                } else {
                    Slog.d("DebugKeepScreenOn", "handleNotObscuredLocked: keyguard isShowing app can not be seen but ShowWhenLocked " + w);
                }
            }
            if (!syswin && w.mAttrs.screenBrightness >= 0.0f && this.mScreenBrightness < 0.0f && w.isVisibleLw()) {
                this.mScreenBrightness = w.mAttrs.screenBrightness;
                this.mAppBrightnessPackageName = w.mAttrs.packageName;
            }
            int type = attrs.type;
            DisplayContent displayContent = w.getDisplayContent();
            if (displayContent != null && displayContent.isDefaultDisplay) {
                if (type == 2023 || (attrs.privateFlags & 1024) != 0) {
                    this.mObscureApplicationContentOnSecondaryDisplays = true;
                }
                displayHasContent = true;
            } else if (displayContent != null && (!this.mObscureApplicationContentOnSecondaryDisplays || (obscured && type == 2009))) {
                displayHasContent = true;
            }
            if ((262144 & privateflags) != 0) {
                this.mSustainedPerformanceModeCurrent = true;
            }
        }
        return displayHasContent;
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked() {
        boolean changed = false;
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            if (((DisplayContent) this.mChildren.get(i)).updateRotationAndSendNewConfigIfNeeded()) {
                changed = true;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean copyAnimToLayoutParams() {
        boolean doRequest = false;
        int bulkUpdateParams = this.mWmService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & 1) != 0) {
            this.mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 4) == 0) {
            if (this.mWmService.mDisplayFrozen) {
                Flog.i(308, "Orientation change is not complete");
            }
            this.mOrientationChangeComplete = false;
        } else {
            if (this.mWmService.mDisplayFrozen) {
                Flog.i(308, "Orientation change is complete");
            }
            this.mOrientationChangeComplete = true;
            this.mLastWindowFreezeSource = this.mWmService.mAnimator.mLastWindowFreezeSource;
            if (this.mWmService.mWindowsFreezingScreen != 0) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & 8) != 0) {
            this.mWallpaperActionPending = true;
        }
        return doRequest;
    }

    private static int toBrightnessOverride(float value) {
        return (int) (255.0f * value);
    }

    private final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                RootWindowContainer.this.mWmService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(msg.arg1);
            } else if (i == 2) {
                RootWindowContainer.this.mWmService.mPowerManagerInternal.setUserActivityTimeoutOverrideFromWindowManager(((Long) msg.obj).longValue());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpDisplayContents(PrintWriter pw) {
        pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
        if (this.mWmService.mDisplayReady) {
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((DisplayContent) this.mChildren.get(i)).dump(pw, "  ", true);
            }
            return;
        }
        pw.println("  NO DISPLAY");
    }

    /* access modifiers changed from: package-private */
    public void dumpTopFocusedDisplayId(PrintWriter pw) {
        pw.print("  mTopFocusedDisplayId=");
        pw.println(this.mTopFocusedDisplayId);
    }

    /* access modifiers changed from: package-private */
    public void dumpLayoutNeededDisplayIds(PrintWriter pw) {
        if (isLayoutNeeded()) {
            pw.print("  mLayoutNeeded on displays=");
            int count = this.mChildren.size();
            for (int displayNdx = 0; displayNdx < count; displayNdx++) {
                DisplayContent displayContent = (DisplayContent) this.mChildren.get(displayNdx);
                if (displayContent.isLayoutNeeded()) {
                    pw.print(displayContent.getDisplayId());
                }
            }
            pw.println();
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpWindowsNoHeader(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        forAllWindows((Consumer<WindowState>) new Consumer(windows, pw, new int[1], dumpAll) {
            /* class com.android.server.wm.$$Lambda$RootWindowContainer$nRMSe8o9Vhp4MBHMJJoyb6ObTQ0 */
            private final /* synthetic */ ArrayList f$0;
            private final /* synthetic */ PrintWriter f$1;
            private final /* synthetic */ int[] f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                RootWindowContainer.lambda$dumpWindowsNoHeader$10(this.f$0, this.f$1, this.f$2, this.f$3, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$dumpWindowsNoHeader$10(ArrayList windows, PrintWriter pw, int[] index, boolean dumpAll, WindowState w) {
        if (windows == null || windows.contains(w)) {
            pw.println("  Window #" + index[0] + " " + w + ":");
            w.dump(pw, "    ", dumpAll || windows != null);
            index[0] = index[0] + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpTokens(PrintWriter pw, boolean dumpAll) {
        pw.println("  All tokens:");
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).dumpTokens(pw, dumpAll);
        }
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            if (this.mWmService.mDisplayReady) {
                int count = this.mChildren.size();
                for (int i = 0; i < count; i++) {
                    ((DisplayContent) this.mChildren.get(i)).writeToProto(proto, 2246267895810L, logLevel);
                }
            }
            if (logLevel == 0) {
                forAllWindows((Consumer<WindowState>) new Consumer(proto) {
                    /* class com.android.server.wm.$$Lambda$RootWindowContainer$RlRzlI_mvPut3IP0uoIxY4DU_c0 */
                    private final /* synthetic */ ProtoOutputStream f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((WindowState) obj).writeIdentifierToProto(this.f$0, 2246267895811L);
                    }
                }, true);
            }
            proto.end(token);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return "ROOT";
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayContent child, boolean includingParents) {
        super.positionChildAt(position, (int) child, includingParents);
        RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
        if (rootActivityContainer != null) {
            rootActivityContainer.onChildPositionChanged(child.mAcitvityDisplay, position);
        }
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayContent child) {
        super.positionChildAt(position, (int) child, false);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void scheduleAnimation() {
        this.mWmService.scheduleAnimationLocked();
    }

    /* access modifiers changed from: protected */
    public void removeChild(DisplayContent dc) {
        super.removeChild((RootWindowContainer) dc);
        if (this.mTopFocusedDisplayId == dc.getDisplayId()) {
            this.mWmService.updateFocusedWindowLocked(0, true);
        }
    }

    /* access modifiers changed from: package-private */
    public void forAllDisplays(Consumer<DisplayContent> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            callback.accept((DisplayContent) this.mChildren.get(i));
        }
    }

    /* access modifiers changed from: package-private */
    public void forAllDisplayPolicies(Consumer<DisplayPolicy> callback) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            callback.accept(((DisplayContent) this.mChildren.get(i)).getDisplayPolicy());
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState getCurrentInputMethodWindow() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(i);
            if (displayContent.mInputMethodWindow != null) {
                return displayContent.mInputMethodWindow;
            }
        }
        return null;
    }

    private void reLayoutIfNeed() {
    }

    /* access modifiers changed from: package-private */
    public void performDisplayTraversal() {
        Slog.i(TAG, "performDisplayTraversal");
        this.mWmService.openSurfaceTransaction();
        try {
            this.mWmService.mDisplayManagerInternal.performTraversal(this.mDisplayTransaction, true);
            SurfaceControl.mergeToGlobalTransaction(this.mDisplayTransaction);
        } catch (RuntimeException e) {
            Slog.wtf(TAG, "Unhandled exception in Window Manager");
        } catch (Throwable th) {
            this.mWmService.closeSurfaceTransaction("performDisplayTraversal");
            throw th;
        }
        this.mWmService.closeSurfaceTransaction("performDisplayTraversal");
    }

    private void sendBrightnessToMonitor(float brightness, String packageName) {
        if (this.mDisplayEffectMonitor != null && packageName != null) {
            if (((double) Math.abs(brightness - this.mAppBrightnessLast)) > 1.0E-7d || !this.mAppBrightnessPackageNameLast.equals(packageName)) {
                ArrayMap<String, Object> params = new ArrayMap<>();
                params.put("paramType", "windowManagerBrightness");
                params.put("brightness", Integer.valueOf(toBrightnessOverride(brightness)));
                params.put(IZRHungService.PARA_PACKAGENAME, packageName);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
                this.mAppBrightnessLast = brightness;
                this.mAppBrightnessPackageNameLast = packageName;
            }
        }
    }

    private boolean needStopFreezingScreen(WindowState win) {
        if (win == null || win.mAppToken == null || win.mAppToken.isRelaunching() || !win.mAppFreezing || !win.inMultiWindowMode()) {
            return false;
        }
        return true;
    }
}
