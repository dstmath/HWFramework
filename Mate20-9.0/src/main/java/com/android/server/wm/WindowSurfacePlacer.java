package com.android.server.wm;

import android.os.Debug;
import android.os.Trace;
import android.util.ArraySet;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.annotations.VisibleForTesting;
import java.io.PrintWriter;
import java.util.function.Predicate;

class WindowSurfacePlacer {
    static final int SET_FORCE_HIDING_CHANGED = 4;
    static final int SET_ORIENTATION_CHANGE_COMPLETE = 8;
    static final int SET_UPDATE_ROTATION = 1;
    static final int SET_WALLPAPER_ACTION_PENDING = 16;
    static final int SET_WALLPAPER_MAY_CHANGE = 2;
    private static final String TAG = "WindowManager";
    private int mDeferDepth = 0;
    private boolean mInLayout = false;
    private boolean mLastIsTopIsFullscreen = false;
    private int mLayoutRepeatCount;
    private final Runnable mPerformSurfacePlacement;
    private final WindowManagerService mService;
    private final SparseIntArray mTempTransitionReasons = new SparseIntArray();
    private final LayerAndToken mTmpLayerAndToken = new LayerAndToken();
    private boolean mTraversalScheduled;
    private final WallpaperController mWallpaperControllerLocked;

    private static final class LayerAndToken {
        public int layer;
        public AppWindowToken token;

        private LayerAndToken() {
        }
    }

    public WindowSurfacePlacer(WindowManagerService service) {
        this.mService = service;
        this.mWallpaperControllerLocked = this.mService.mRoot.mWallpaperController;
        this.mPerformSurfacePlacement = new Runnable() {
            public final void run() {
                WindowSurfacePlacer.lambda$new$0(WindowSurfacePlacer.this);
            }
        };
    }

    public static /* synthetic */ void lambda$new$0(WindowSurfacePlacer windowSurfacePlacer) {
        synchronized (windowSurfacePlacer.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                windowSurfacePlacer.performSurfacePlacement();
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
    }

    /* access modifiers changed from: package-private */
    public void deferLayout() {
        this.mDeferDepth++;
    }

    /* access modifiers changed from: package-private */
    public void continueLayout() {
        this.mDeferDepth--;
        if (this.mDeferDepth <= 0) {
            performSurfacePlacement();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutDeferred() {
        return this.mDeferDepth > 0;
    }

    /* access modifiers changed from: package-private */
    public final void performSurfacePlacement() {
        performSurfacePlacement(false);
    }

    /* access modifiers changed from: package-private */
    public final void performSurfacePlacement(boolean force) {
        if (this.mDeferDepth <= 0 || force) {
            int loopCount = 6;
            do {
                this.mTraversalScheduled = false;
                performSurfacePlacementLoop();
                this.mService.mAnimationHandler.removeCallbacks(this.mPerformSurfacePlacement);
                loopCount--;
                if (!this.mTraversalScheduled) {
                    break;
                }
            } while (loopCount > 0);
            this.mService.mRoot.mWallpaperActionPending = false;
            boolean isTopIsFullscreen = this.mService.mPolicy.isTopIsFullscreen();
            if (this.mLastIsTopIsFullscreen != isTopIsFullscreen) {
                this.mLastIsTopIsFullscreen = isTopIsFullscreen;
                this.mService.mInputManager.setIsTopFullScreen(this.mLastIsTopIsFullscreen);
            }
        }
    }

    private void performSurfacePlacementLoop() {
        WindowState ws;
        if (this.mInLayout) {
            Slog.w(TAG, "performLayoutAndPlaceSurfacesLocked called while in layout. Callers=" + Debug.getCallers(3));
        } else if (!this.mService.mWaitingForConfig && this.mService.mDisplayReady) {
            Trace.traceBegin(32, "wmLayout");
            this.mInLayout = true;
            boolean recoveringMemory = false;
            if (!this.mService.mForceRemoves.isEmpty()) {
                while (!this.mService.mForceRemoves.isEmpty()) {
                    Slog.i(TAG, "Force removing: " + ws);
                    ws.removeImmediately();
                }
                Slog.w(TAG, "Due to memory failure, waiting a bit for next layout");
                Object tmp = new Object();
                synchronized (tmp) {
                    try {
                        tmp.wait(250);
                    } catch (InterruptedException e) {
                    }
                }
                recoveringMemory = true;
            }
            try {
                this.mService.mRoot.performSurfacePlacement(recoveringMemory);
                this.mInLayout = false;
                if (this.mService.mRoot.isLayoutNeeded()) {
                    int i = this.mLayoutRepeatCount + 1;
                    this.mLayoutRepeatCount = i;
                    if (i < 6) {
                        requestTraversal();
                    } else {
                        Slog.e(TAG, "Performed 6 layouts in a row. Skipping");
                        this.mLayoutRepeatCount = 0;
                    }
                } else {
                    this.mLayoutRepeatCount = 0;
                }
                if (this.mService.mWindowsChanged && !this.mService.mWindowChangeListeners.isEmpty()) {
                    this.mService.mH.removeMessages(19);
                    this.mService.mH.sendEmptyMessage(19);
                }
            } catch (RuntimeException e2) {
                this.mInLayout = false;
                Slog.wtf(TAG, "Unhandled exception while laying out windows", e2);
            }
            Trace.traceEnd(32);
        }
    }

    /* access modifiers changed from: package-private */
    public void debugLayoutRepeats(String msg, int pendingLayoutChanges) {
        if (this.mLayoutRepeatCount >= 4) {
            Slog.v(TAG, "Layouts looping: " + msg + ", mPendingLayoutChanges = 0x" + Integer.toHexString(pendingLayoutChanges));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInLayout() {
        return this.mInLayout;
    }

    /* access modifiers changed from: package-private */
    public int handleAppTransitionReadyLocked() {
        AppWindowToken animLpToken;
        AppWindowToken topClosingApp;
        AppWindowToken topOpeningApp;
        int flags;
        int appsCount = this.mService.mOpeningApps.size();
        if (!transitionGoodToGo(appsCount, this.mTempTransitionReasons)) {
            return 0;
        }
        Trace.traceBegin(32, "AppTransitionReady");
        Flog.i(307, "**** GOOD TO GO");
        int transit = this.mService.mAppTransition.getAppTransition();
        if (this.mService.mSkipAppTransitionAnimation && !AppTransition.isKeyguardGoingAwayTransit(transit)) {
            transit = -1;
        }
        this.mService.mSkipAppTransitionAnimation = false;
        this.mService.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mService.mH.removeMessages(13);
        DisplayContent displayContent = this.mService.getDefaultDisplayContentLocked();
        this.mService.mRoot.mWallpaperMayChange = false;
        for (int i = 0; i < appsCount; i++) {
            this.mService.mOpeningApps.valueAt(i).clearAnimatingFlags();
        }
        this.mWallpaperControllerLocked.adjustWallpaperWindowsForAppTransitionIfNeeded(displayContent, this.mService.mOpeningApps);
        boolean hasWallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget() != null;
        int transit2 = maybeUpdateTransitToWallpaper(maybeUpdateTransitToTranslucentAnim(transit), canBeWallpaperTarget(this.mService.mOpeningApps) && hasWallpaperTarget, canBeWallpaperTarget(this.mService.mClosingApps) && hasWallpaperTarget);
        ArraySet<Integer> activityTypes = collectActivityTypes(this.mService.mOpeningApps, this.mService.mClosingApps);
        if (this.mService.mPolicy.allowAppAnimationsLw()) {
            animLpToken = findAnimLayoutParamsToken(transit2, activityTypes);
        } else {
            animLpToken = null;
        }
        AppWindowToken animLpToken2 = animLpToken;
        WindowManager.LayoutParams animLp = getAnimLp(animLpToken2);
        overrideWithRemoteAnimationIfSet(animLpToken2, transit2, activityTypes);
        boolean voiceInteraction = containsVoiceInteraction(this.mService.mOpeningApps) || containsVoiceInteraction(this.mService.mOpeningApps);
        this.mService.mSurfaceAnimationRunner.deferStartingAnimations();
        try {
            processApplicationsAnimatingInPlace(transit2);
            this.mTmpLayerAndToken.token = null;
            boolean voiceInteraction2 = voiceInteraction;
            try {
                handleClosingApps(transit2, animLp, voiceInteraction2, this.mTmpLayerAndToken);
                topClosingApp = this.mTmpLayerAndToken.token;
                AppWindowToken topOpeningApp2 = handleOpeningApps(transit2, animLp, voiceInteraction2);
                if (!WindowManagerService.HW_SUPPORT_LAUNCHER_EXIT_ANIM || transit2 != 13) {
                    topOpeningApp = topOpeningApp2;
                } else {
                    topOpeningApp = topOpeningApp2;
                    if (topOpeningApp != null) {
                        try {
                            int i2 = appsCount;
                            try {
                                if (topOpeningApp.toString().contains("com.huawei.android.launcher/.unihome.UniHomeLauncher") != 0 || topOpeningApp.toString().contains("com.huawei.android.launcher/.drawer.DrawerLauncher")) {
                                    this.mService.mExitPivotX = -1.0f;
                                    this.mService.mExitPivotY = -1.0f;
                                    this.mService.mExitIconBitmap = null;
                                    this.mService.mExitIconWidth = -1;
                                    this.mService.mExitIconHeight = -1;
                                    this.mService.mExitFlag = -1;
                                    Slog.i(TAG, "exit info has been reset.");
                                }
                            } catch (Throwable th) {
                                th = th;
                                boolean z = voiceInteraction2;
                                AppWindowToken appWindowToken = animLpToken2;
                                ArraySet<Integer> arraySet = activityTypes;
                                int i3 = transit2;
                                this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                                throw th;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            int i4 = appsCount;
                            boolean z2 = voiceInteraction2;
                            AppWindowToken appWindowToken2 = animLpToken2;
                            ArraySet<Integer> arraySet2 = activityTypes;
                            int i5 = transit2;
                            this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                            throw th;
                        }
                    }
                }
                try {
                    this.mService.mAppTransition.setLastAppTransition(transit2, topOpeningApp, topClosingApp);
                    flags = this.mService.mAppTransition.getTransitFlags();
                    boolean z3 = voiceInteraction2;
                } catch (Throwable th3) {
                    th = th3;
                    boolean z4 = voiceInteraction2;
                    AppWindowToken appWindowToken3 = animLpToken2;
                    ArraySet<Integer> arraySet3 = activityTypes;
                    int i6 = transit2;
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                int i7 = appsCount;
                boolean z5 = voiceInteraction2;
                AppWindowToken appWindowToken4 = animLpToken2;
                ArraySet<Integer> arraySet4 = activityTypes;
                int i8 = transit2;
                this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                throw th;
            }
            try {
                ArraySet<Integer> arraySet5 = activityTypes;
                AppWindowToken appWindowToken5 = animLpToken2;
                int transit3 = transit2;
                try {
                    int layoutRedo = this.mService.mAppTransition.goodToGo(transit2, topOpeningApp, topClosingApp, this.mService.mOpeningApps, this.mService.mClosingApps);
                    handleNonAppWindowsInTransition(transit3, flags);
                    this.mService.mAppTransition.postAnimationCallback();
                    this.mService.mAppTransition.clear();
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    this.mService.mTaskSnapshotController.onTransitionStarting();
                    this.mService.mOpeningApps.clear();
                    this.mService.mClosingApps.clear();
                    this.mService.mUnknownAppVisibilityController.clear();
                    displayContent.setLayoutNeeded();
                    DisplayContent dc = this.mService.getDefaultDisplayContentLocked();
                    if (HwPCUtils.isPcCastModeInServer()) {
                        DisplayContent pcDC = this.mService.mRoot.getDisplayContent(this.mService.getFocusedDisplayId());
                        if (pcDC != null) {
                            dc = pcDC;
                        }
                    }
                    dc.computeImeTarget(true);
                    this.mService.updateFocusedWindowLocked(2, true);
                    this.mService.mFocusMayChange = false;
                    this.mService.mH.obtainMessage(47, this.mTempTransitionReasons.clone()).sendToTarget();
                    Trace.traceEnd(32);
                    return layoutRedo | 1 | 2;
                } catch (Throwable th5) {
                    th = th5;
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                AppWindowToken appWindowToken6 = animLpToken2;
                ArraySet<Integer> arraySet6 = activityTypes;
                int i9 = transit2;
                this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                throw th;
            }
        } catch (Throwable th7) {
            th = th7;
            int i10 = appsCount;
            AppWindowToken appWindowToken7 = animLpToken2;
            ArraySet<Integer> arraySet7 = activityTypes;
            int i11 = transit2;
            boolean z6 = voiceInteraction;
            this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
            throw th;
        }
    }

    private static WindowManager.LayoutParams getAnimLp(AppWindowToken wtoken) {
        WindowState mainWindow = wtoken != null ? wtoken.findMainWindow() : null;
        if (mainWindow != null) {
            return mainWindow.mAttrs;
        }
        return null;
    }

    private void overrideWithRemoteAnimationIfSet(AppWindowToken animLpToken, int transit, ArraySet<Integer> activityTypes) {
        if (transit != 26 && animLpToken != null) {
            RemoteAnimationDefinition definition = animLpToken.getRemoteAnimationDefinition();
            if (definition != null) {
                RemoteAnimationAdapter adapter = definition.getAdapter(transit, activityTypes);
                if (adapter != null) {
                    this.mService.mAppTransition.overridePendingAppTransitionRemote(adapter);
                }
            }
        }
    }

    private AppWindowToken findAnimLayoutParamsToken(int transit, ArraySet<Integer> activityTypes) {
        AppWindowToken result = lookForHighestTokenWithFilter(this.mService.mClosingApps, this.mService.mOpeningApps, new Predicate(transit, activityTypes) {
            private final /* synthetic */ int f$0;
            private final /* synthetic */ ArraySet f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return WindowSurfacePlacer.lambda$findAnimLayoutParamsToken$1(this.f$0, this.f$1, (AppWindowToken) obj);
            }
        });
        if (result != null) {
            return result;
        }
        AppWindowToken result2 = lookForHighestTokenWithFilter(this.mService.mClosingApps, this.mService.mOpeningApps, $$Lambda$WindowSurfacePlacer$wCevQN6hMxiB97Eay8ibpi2Xaxo.INSTANCE);
        if (result2 != null) {
            return result2;
        }
        return lookForHighestTokenWithFilter(this.mService.mClosingApps, this.mService.mOpeningApps, $$Lambda$WindowSurfacePlacer$tJcqA51ohv9DQjcvHOarwInr01s.INSTANCE);
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$1(int transit, ArraySet activityTypes, AppWindowToken w) {
        return w.getRemoteAnimationDefinition() != null && w.getRemoteAnimationDefinition().hasTransition(transit, activityTypes);
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$2(AppWindowToken w) {
        return w.fillsParent() && w.findMainWindow() != null;
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$3(AppWindowToken w) {
        return w.findMainWindow() != null;
    }

    private ArraySet<Integer> collectActivityTypes(ArraySet<AppWindowToken> array1, ArraySet<AppWindowToken> array2) {
        ArraySet<Integer> result = new ArraySet<>();
        for (int i = array1.size() - 1; i >= 0; i--) {
            result.add(Integer.valueOf(array1.valueAt(i).getActivityType()));
        }
        for (int i2 = array2.size() - 1; i2 >= 0; i2--) {
            result.add(Integer.valueOf(array2.valueAt(i2).getActivityType()));
        }
        return result;
    }

    private AppWindowToken lookForHighestTokenWithFilter(ArraySet<AppWindowToken> array1, ArraySet<AppWindowToken> array2, Predicate<AppWindowToken> filter) {
        AppWindowToken wtoken;
        int array1count = array1.size();
        int count = array2.size() + array1count;
        int bestPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken bestToken = null;
        for (int i = 0; i < count; i++) {
            if (i < array1count) {
                wtoken = array1.valueAt(i);
            } else {
                wtoken = array2.valueAt(i - array1count);
            }
            int prefixOrderIndex = wtoken.getPrefixOrderIndex();
            if (filter.test(wtoken) && prefixOrderIndex > bestPrefixOrderIndex) {
                bestPrefixOrderIndex = prefixOrderIndex;
                bestToken = wtoken;
            }
        }
        return bestToken;
    }

    private boolean containsVoiceInteraction(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            if (apps.valueAt(i).mVoiceInteraction) {
                return true;
            }
        }
        return false;
    }

    private AppWindowToken handleOpeningApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        String str;
        int appsCount = this.mService.mOpeningApps.size();
        int topOpeningLayer = Integer.MIN_VALUE;
        AppWindowToken topOpeningApp = null;
        int i = 0;
        while (i < appsCount) {
            AppWindowToken wtoken = this.mService.mOpeningApps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now opening app" + wtoken);
            }
            if (!wtoken.setVisibility(animLp, true, transit, false, voiceInteraction)) {
                this.mService.mNoAnimationNotifyOnTransitionFinished.add(wtoken.token);
            }
            wtoken.updateReportedVisibilityLocked();
            wtoken.waitingToShow = false;
            this.mService.openSurfaceTransaction();
            try {
                wtoken.showAllWindowsLocked();
                if (animLp != null) {
                    int layer = wtoken.getHighestAnimLayer();
                    if (topOpeningApp == null || layer > topOpeningLayer) {
                        topOpeningApp = wtoken;
                        topOpeningLayer = layer;
                    }
                }
                if (this.mService.mAppTransition.isNextAppTransitionThumbnailUp()) {
                    wtoken.attachThumbnailAnimation();
                } else if (this.mService.mAppTransition.isNextAppTransitionOpenCrossProfileApps()) {
                    wtoken.attachCrossProfileAppsThumbnailAnimation();
                }
                i++;
            } finally {
                str = "handleAppTransitionReadyLocked";
                this.mService.closeSurfaceTransaction(str);
            }
        }
        return topOpeningApp;
    }

    private AppWindowToken findMaxWindowSizeToken(int appsCount, int transit) {
        int maxSize = -1;
        AppWindowToken maxSizeToken = null;
        if (WindowManagerService.HW_SUPPORT_LAUNCHER_EXIT_ANIM && transit == 13) {
            for (int i = 0; i < appsCount; i++) {
                AppWindowToken wtoken = this.mService.mClosingApps.valueAt(i);
                if (wtoken != null) {
                    WindowState win = wtoken.findMainWindow();
                    if (win != null && win.mFrame != null && win.mFrame.height() * win.mFrame.width() >= maxSize && this.mService.isSupportHwAppExitAnim(wtoken)) {
                        maxSize = win.mFrame.height() * win.mFrame.width();
                        maxSizeToken = wtoken;
                    }
                }
            }
        }
        return maxSizeToken;
    }

    private void handleClosingApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction, LayerAndToken layerAndToken) {
        LayerAndToken layerAndToken2 = layerAndToken;
        int appsCount = this.mService.mClosingApps.size();
        int i = transit;
        AppWindowToken maxWinHeightToken = findMaxWindowSizeToken(appsCount, i);
        Slog.i(TAG, "closing apps count = " + appsCount);
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < appsCount) {
                AppWindowToken wtoken = this.mService.mClosingApps.valueAt(i3);
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Now closing app " + wtoken);
                }
                if (maxWinHeightToken == wtoken) {
                    wtoken.mShouldDrawIcon = true;
                }
                wtoken.setVisibility(animLp, false, i, false, voiceInteraction);
                wtoken.updateReportedVisibilityLocked();
                wtoken.allDrawn = true;
                wtoken.deferClearAllDrawn = false;
                if (!(wtoken.startingWindow == null || wtoken.startingWindow.mAnimatingExit || wtoken.getController() == null)) {
                    wtoken.getController().removeStartingWindow();
                }
                if (animLp != null) {
                    int layer = wtoken.getHighestAnimLayer();
                    if (layerAndToken2.token == null || layer > layerAndToken2.layer) {
                        layerAndToken2.token = wtoken;
                        layerAndToken2.layer = layer;
                    }
                }
                if (this.mService.mAppTransition.isNextAppTransitionThumbnailDown()) {
                    wtoken.attachThumbnailAnimation();
                }
                i2 = i3 + 1;
            } else {
                return;
            }
        }
    }

    private void handleNonAppWindowsInTransition(int transit, int flags) {
        boolean z = false;
        if (transit == 20 && (flags & 4) != 0 && (flags & 2) == 0) {
            Animation anim = this.mService.mPolicy.createKeyguardWallpaperExit((flags & 1) != 0);
            if (anim != null) {
                this.mService.getDefaultDisplayContentLocked().mWallpaperController.startWallpaperAnimation(anim);
            }
        }
        if (transit == 20 || transit == 21) {
            DisplayContent defaultDisplayContentLocked = this.mService.getDefaultDisplayContentLocked();
            boolean z2 = transit == 21;
            if ((flags & 1) != 0) {
                z = true;
            }
            defaultDisplayContentLocked.startKeyguardExitOnNonAppWindows(z2, z);
        }
    }

    private boolean transitionGoodToGo(int appsCount, SparseIntArray outReasons) {
        int i;
        Slog.v(TAG, "Checking " + appsCount + " opening apps (frozen=" + this.mService.mDisplayFrozen + " timeout=" + this.mService.mAppTransition.isTimeout() + "), Track: " + this.mService.mAppTransitTrack);
        this.mService.mAppTransitTrack = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
        outReasons.clear();
        if (this.mService.mAppTransition.isTimeout()) {
            return true;
        }
        if (screenRotationAnimation == null || !screenRotationAnimation.isAnimating() || !this.mService.rotationNeedsUpdateLocked()) {
            for (int i2 = 0; i2 < appsCount; i2++) {
                AppWindowToken wtoken = this.mService.mOpeningApps.valueAt(i2);
                Slog.v(TAG, "Check opening app=" + wtoken + ": allDrawn=" + wtoken.allDrawn + " startingDisplayed=" + wtoken.startingDisplayed + " startingMoved=" + wtoken.startingMoved + " isRelaunching()=" + wtoken.isRelaunching() + " startingWindow=" + wtoken.startingWindow);
                boolean allDrawn = wtoken.allDrawn && !wtoken.isRelaunching();
                if (!allDrawn && !wtoken.startingDisplayed && !wtoken.startingMoved) {
                    return false;
                }
                int windowingMode = wtoken.getWindowingMode();
                if (allDrawn) {
                    outReasons.put(windowingMode, 2);
                } else {
                    if (wtoken.startingData instanceof SplashScreenStartingData) {
                        i = 1;
                    } else {
                        i = 4;
                    }
                    outReasons.put(windowingMode, i);
                }
            }
            if (this.mService.mAppTransition.isFetchingAppTransitionsSpecs()) {
                Flog.i(310, "wait for the specs to be fetched");
                return false;
            } else if (!this.mService.mUnknownAppVisibilityController.allResolved()) {
                Flog.i(310, "unknownApps is not empty: " + this.mService.mUnknownAppVisibilityController.getDebugMessage());
                return false;
            } else {
                if (!this.mWallpaperControllerLocked.isWallpaperVisible() || this.mWallpaperControllerLocked.wallpaperTransitionReady()) {
                    return true;
                }
                Flog.i(310, "wallpaper is not ready for transition");
                return false;
            }
        } else {
            Flog.i(310, "wait for screen rotation animation to finish");
            return false;
        }
    }

    private int maybeUpdateTransitToWallpaper(int transit, boolean openingAppHasWallpaper, boolean closingAppHasWallpaper) {
        if (transit == 0 || transit == 26 || transit == 19) {
            return transit;
        }
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        WindowState oldWallpaper = this.mWallpaperControllerLocked.isWallpaperTargetAnimating() ? null : wallpaperTarget;
        ArraySet<AppWindowToken> openingApps = this.mService.mOpeningApps;
        ArraySet<AppWindowToken> closingApps = this.mService.mClosingApps;
        AppWindowToken topOpeningApp = getTopApp(this.mService.mOpeningApps, false);
        AppWindowToken topClosingApp = getTopApp(this.mService.mClosingApps, true);
        boolean openingCanBeWallpaperTarget = canBeWallpaperTarget(openingApps);
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", openingApps=" + openingApps + ", closingApps=" + closingApps);
        }
        int oldTransit = transit;
        if (openingCanBeWallpaperTarget && transit == 20) {
            transit = 21;
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "New transit: " + AppTransition.appTransitionToString(21));
            }
        } else if (!AppTransition.isKeyguardGoingAwayTransit(transit)) {
            if (closingAppHasWallpaper && openingAppHasWallpaper) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Wallpaper animation!");
                }
                switch (transit) {
                    case 6:
                    case 8:
                    case 10:
                        transit = 14;
                        break;
                    case 7:
                    case 9:
                    case 11:
                        transit = 15;
                        break;
                }
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit: " + AppTransition.appTransitionToString(transit));
                }
            } else if (oldWallpaper != null && !this.mService.mOpeningApps.isEmpty() && !openingApps.contains(oldWallpaper.mAppToken) && closingApps.contains(oldWallpaper.mAppToken) && topClosingApp == oldWallpaper.mAppToken) {
                transit = 12;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit away from wallpaper: " + AppTransition.appTransitionToString(12));
                }
            } else if (wallpaperTarget != null && wallpaperTarget.isVisibleLw() && openingApps.contains(wallpaperTarget.mAppToken) && topOpeningApp == wallpaperTarget.mAppToken && transit != 25) {
                transit = 13;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit into wallpaper: " + AppTransition.appTransitionToString(13));
                }
            }
        }
        if (WindowManagerDebugConfig.HWFLOW) {
            Slog.i(TAG, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", oldTransit " + AppTransition.appTransitionToString(oldTransit) + ", transit " + AppTransition.appTransitionToString(transit));
        }
        return transit;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int maybeUpdateTransitToTranslucentAnim(int transit) {
        boolean taskOrActivity = AppTransition.isTaskTransit(transit) || AppTransition.isActivityTransit(transit);
        boolean allOpeningVisible = true;
        boolean allTranslucentOpeningApps = !this.mService.mOpeningApps.isEmpty();
        for (int i = this.mService.mOpeningApps.size() - 1; i >= 0; i--) {
            AppWindowToken token = this.mService.mOpeningApps.valueAt(i);
            if (!token.isVisible()) {
                allOpeningVisible = false;
                if (token.fillsParent()) {
                    allTranslucentOpeningApps = false;
                }
            }
        }
        boolean allTranslucentClosingApps = !this.mService.mClosingApps.isEmpty();
        int i2 = this.mService.mClosingApps.size() - 1;
        while (true) {
            int i3 = i2;
            if (i3 < 0) {
                break;
            } else if (this.mService.mClosingApps.valueAt(i3).fillsParent()) {
                allTranslucentClosingApps = false;
                break;
            } else {
                i2 = i3 - 1;
            }
        }
        if (taskOrActivity && allTranslucentClosingApps && allOpeningVisible) {
            return 25;
        }
        if (!taskOrActivity || !allTranslucentOpeningApps || !this.mService.mClosingApps.isEmpty()) {
            return transit;
        }
        return 24;
    }

    private boolean canBeWallpaperTarget(ArraySet<AppWindowToken> apps) {
        for (int i = apps.size() - 1; i >= 0; i--) {
            if (apps.valueAt(i).windowsCanBeWallpaperTarget()) {
                return true;
            }
        }
        return false;
    }

    private AppWindowToken getTopApp(ArraySet<AppWindowToken> apps, boolean ignoreHidden) {
        int topPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken topApp = null;
        for (int i = apps.size() - 1; i >= 0; i--) {
            AppWindowToken app = apps.valueAt(i);
            if (!ignoreHidden || !app.isHidden()) {
                int prefixOrderIndex = app.getPrefixOrderIndex();
                if (prefixOrderIndex > topPrefixOrderIndex) {
                    topPrefixOrderIndex = prefixOrderIndex;
                    topApp = app;
                }
            }
        }
        return topApp;
    }

    private void processApplicationsAnimatingInPlace(int transit) {
        if (transit == 17) {
            WindowState win = this.mService.getDefaultDisplayContentLocked().findFocusedWindow();
            if (win != null) {
                AppWindowToken wtoken = win.mAppToken;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Now animating app in place " + wtoken);
                }
                wtoken.cancelAnimation();
                wtoken.applyAnimationLocked(null, transit, false, false);
                wtoken.updateReportedVisibilityLocked();
                wtoken.showAllWindowsLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void requestTraversal() {
        if (!this.mTraversalScheduled) {
            this.mTraversalScheduled = true;
            this.mService.mAnimationHandler.post(this.mPerformSurfacePlacement);
        }
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "mTraversalScheduled=" + this.mTraversalScheduled);
        pw.println(prefix + "mHoldScreenWindow=" + this.mService.mRoot.mHoldScreenWindow);
        pw.println(prefix + "mObscuringWindow=" + this.mService.mRoot.mObscuringWindow);
        pw.println(prefix + "mDeferDepth=" + this.mDeferDepth);
    }
}
