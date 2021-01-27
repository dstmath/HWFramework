package com.android.server.wm;

import android.os.SystemClock;
import android.os.Trace;
import android.util.ArraySet;
import android.util.CoordinationModeUtils;
import android.util.Flog;
import android.util.Jlog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.RemoteAnimationAdapter;
import android.view.RemoteAnimationDefinition;
import android.view.WindowManager;
import android.view.animation.Animation;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wm.WindowManagerService;
import java.util.Iterator;
import java.util.function.Predicate;

public class AppTransitionController {
    private static final String TAG = "WindowManager";
    private final DisplayContent mDisplayContent;
    private RemoteAnimationDefinition mRemoteAnimationDefinition = null;
    private final WindowManagerService mService;
    private final SparseIntArray mTempTransitionReasons = new SparseIntArray();
    private final WallpaperController mWallpaperControllerLocked;

    AppTransitionController(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
        this.mWallpaperControllerLocked = this.mDisplayContent.mWallpaperController;
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mRemoteAnimationDefinition = definition;
    }

    /* access modifiers changed from: package-private */
    public void handleAppTransitionReady() {
        int transit;
        AppWindowToken animLpToken;
        AppWindowToken topOpeningApp;
        AppWindowToken topClosingApp;
        AppWindowToken topChangingApp;
        Throwable th;
        this.mTempTransitionReasons.clear();
        if (transitionGoodToGo(this.mDisplayContent.mOpeningApps, this.mTempTransitionReasons) && transitionGoodToGo(this.mDisplayContent.mChangingApps, this.mTempTransitionReasons)) {
            Trace.traceBegin(32, "AppTransitionReady");
            Jlog.printActivitySwitchAnimBegin();
            Flog.i(307, "**** GOOD TO GO");
            AppTransition appTransition = this.mDisplayContent.mAppTransition;
            int transit2 = appTransition.getAppTransition();
            if (this.mDisplayContent.mSkipAppTransitionAnimation && !AppTransition.isKeyguardGoingAwayTransit(transit2)) {
                transit2 = -1;
            }
            DisplayContent displayContent = this.mDisplayContent;
            displayContent.mSkipAppTransitionAnimation = false;
            displayContent.mNoAnimationNotifyOnTransitionFinished.clear();
            appTransition.removeAppTransitionTimeoutCallbacks();
            DisplayContent displayContent2 = this.mDisplayContent;
            displayContent2.mWallpaperMayChange = false;
            int appCount = displayContent2.mOpeningApps.size();
            for (int i = 0; i < appCount; i++) {
                ((AppWindowToken) this.mDisplayContent.mOpeningApps.valueAtUnchecked(i)).clearAnimatingFlags();
            }
            int appCount2 = this.mDisplayContent.mChangingApps.size();
            for (int i2 = 0; i2 < appCount2; i2++) {
                ((AppWindowToken) this.mDisplayContent.mChangingApps.valueAtUnchecked(i2)).clearAnimatingFlags();
            }
            this.mWallpaperControllerLocked.adjustWallpaperWindowsForAppTransitionIfNeeded(this.mDisplayContent.mOpeningApps, this.mDisplayContent.mChangingApps);
            boolean hasWallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget() != null;
            boolean openingAppHasWallpaper = canBeWallpaperTarget(this.mDisplayContent.mOpeningApps) && hasWallpaperTarget;
            boolean closingAppHasWallpaper = canBeWallpaperTarget(this.mDisplayContent.mClosingApps) && hasWallpaperTarget;
            if (this.mService.getRecentsAnimationController() == null || !(transit2 == 6 || transit2 == 7 || transit2 == 9)) {
                transit = maybeUpdateTransitToWallpaper(maybeUpdateTransitToTranslucentAnim(transit2), openingAppHasWallpaper, closingAppHasWallpaper);
            } else {
                transit = -1;
                Slog.d(TAG, "Turn some transits into TRANSIT_UNSET during recents animation, original transit:-1");
            }
            ArraySet<Integer> activityTypes = collectActivityTypes(this.mDisplayContent.mOpeningApps, this.mDisplayContent.mClosingApps, this.mDisplayContent.mChangingApps);
            boolean allowAnimations = this.mDisplayContent.getDisplayPolicy().allowAppAnimationsLw();
            if (allowAnimations) {
                animLpToken = findAnimLayoutParamsToken(transit, activityTypes);
            } else {
                animLpToken = null;
            }
            if (allowAnimations) {
                topOpeningApp = getTopApp(this.mDisplayContent.mOpeningApps, false);
            } else {
                topOpeningApp = null;
            }
            if (allowAnimations) {
                topClosingApp = getTopApp(this.mDisplayContent.mClosingApps, false);
            } else {
                topClosingApp = null;
            }
            if (allowAnimations) {
                topChangingApp = getTopApp(this.mDisplayContent.mChangingApps, false);
            } else {
                topChangingApp = null;
            }
            WindowManager.LayoutParams animLp = getAnimLp(animLpToken);
            overrideWithRemoteAnimationIfSet(animLpToken, transit, activityTypes);
            if (appTransition.getRemoteAnimationController() == null && !this.mService.isInWallpaperEffect()) {
                this.mService.recoverAllActivityVisible();
                Flog.i(307, "recover the scale and let all layers visible");
            }
            if (this.mService.mRtgSchedSwitch) {
                this.mService.mHwWMSEx.appTransitionBoost(this.mDisplayContent, transit);
            }
            boolean voiceInteraction = containsVoiceInteraction(this.mDisplayContent.mOpeningApps) || containsVoiceInteraction(this.mDisplayContent.mOpeningApps) || containsVoiceInteraction(this.mDisplayContent.mChangingApps);
            this.mService.mSurfaceAnimationRunner.deferStartingAnimations();
            try {
                processApplicationsAnimatingInPlace(transit);
                if (transit == 50 || transit == 52) {
                    notifyWindowStateChange();
                }
                handleClosingApps(transit, animLp, voiceInteraction);
                handleOpeningApps(transit, animLp, voiceInteraction);
                handleChangingApps(transit, animLp, voiceInteraction);
                try {
                } catch (Throwable th2) {
                    th = th2;
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    throw th;
                }
                try {
                    this.mService.mAtmService.mHwATMSEx.updateHwFreeformNotificationState(this.mDisplayContent.mDisplayId, "ready");
                    this.mService.mHwWMSEx.resetAppWindowExitInfo(transit, topOpeningApp);
                    appTransition.setLastAppTransition(transit, topOpeningApp, topClosingApp, topChangingApp);
                    int flags = appTransition.getTransitFlags();
                    int layoutRedo = appTransition.goodToGo(transit, topOpeningApp, this.mDisplayContent.mOpeningApps);
                    handleNonAppWindowsInTransition(transit, flags);
                    appTransition.postAnimationCallback();
                    appTransition.clear();
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    this.mService.mTaskSnapshotController.onTransitionStarting(this.mDisplayContent);
                    this.mDisplayContent.mOpeningApps.clear();
                    this.mDisplayContent.mClosingApps.clear();
                    this.mDisplayContent.mChangingApps.clear();
                    this.mDisplayContent.mUnknownAppVisibilityController.clear();
                    this.mDisplayContent.setLayoutNeeded();
                    this.mDisplayContent.computeImeTarget(true);
                    this.mService.mAtmInternal.notifyAppTransitionStarting(this.mTempTransitionReasons.clone(), SystemClock.uptimeMillis());
                    Trace.traceEnd(32);
                    this.mDisplayContent.pendingLayoutChanges |= layoutRedo | 1 | 2;
                } catch (Throwable th3) {
                    th = th3;
                    this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                this.mService.mSurfaceAnimationRunner.continueStartingAnimations();
                throw th;
            }
        }
    }

    private static WindowManager.LayoutParams getAnimLp(AppWindowToken wtoken) {
        WindowState mainWindow = wtoken != null ? wtoken.findMainWindow() : null;
        if (mainWindow != null) {
            return mainWindow.mAttrs;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public RemoteAnimationAdapter getRemoteAnimationOverride(AppWindowToken animLpToken, int transit, ArraySet<Integer> activityTypes) {
        RemoteAnimationAdapter adapter;
        RemoteAnimationDefinition definition = animLpToken.getRemoteAnimationDefinition();
        if (definition != null && (adapter = definition.getAdapter(transit, activityTypes)) != null) {
            return adapter;
        }
        RemoteAnimationAdapter adapter2 = this.mRemoteAnimationDefinition;
        if (adapter2 == null) {
            return null;
        }
        return adapter2.getAdapter(transit, activityTypes);
    }

    private void overrideWithRemoteAnimationIfSet(AppWindowToken animLpToken, int transit, ArraySet<Integer> activityTypes) {
        RemoteAnimationAdapter adapter;
        if (transit != 26 && animLpToken != null && (adapter = getRemoteAnimationOverride(animLpToken, transit, activityTypes)) != null) {
            animLpToken.getDisplayContent().mAppTransition.overridePendingAppTransitionRemote(adapter);
        }
    }

    private AppWindowToken findAnimLayoutParamsToken(int transit, ArraySet<Integer> activityTypes) {
        ArraySet<AppWindowToken> closingApps = this.mDisplayContent.mClosingApps;
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        ArraySet<AppWindowToken> changingApps = this.mDisplayContent.mChangingApps;
        AppWindowToken result = lookForHighestTokenWithFilter(closingApps, openingApps, changingApps, new Predicate(transit, activityTypes) {
            /* class com.android.server.wm.$$Lambda$AppTransitionController$YfQg1m68hbvcHoXbvzomyslzuaU */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ ArraySet f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AppTransitionController.lambda$findAnimLayoutParamsToken$0(this.f$0, this.f$1, (AppWindowToken) obj);
            }
        });
        if (result != null) {
            return result;
        }
        AppWindowToken result2 = lookForHighestTokenWithFilter(closingApps, openingApps, changingApps, $$Lambda$AppTransitionController$ESsBJ2royCDDfelW3z7cgYH5q2I.INSTANCE);
        if (result2 != null) {
            return result2;
        }
        return lookForHighestTokenWithFilter(closingApps, openingApps, changingApps, $$Lambda$AppTransitionController$j4jrKo6PKtYRjRfPVQMMiQB02jg.INSTANCE);
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$0(int transit, ArraySet activityTypes, AppWindowToken w) {
        return w.getRemoteAnimationDefinition() != null && w.getRemoteAnimationDefinition().hasTransition(transit, activityTypes);
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$1(AppWindowToken w) {
        return w.fillsParent() && w.findMainWindow() != null;
    }

    static /* synthetic */ boolean lambda$findAnimLayoutParamsToken$2(AppWindowToken w) {
        return w.findMainWindow() != null;
    }

    private static ArraySet<Integer> collectActivityTypes(ArraySet<AppWindowToken> array1, ArraySet<AppWindowToken> array2, ArraySet<AppWindowToken> array3) {
        ArraySet<Integer> result = new ArraySet<>();
        for (int i = array1.size() - 1; i >= 0; i--) {
            result.add(Integer.valueOf(array1.valueAt(i).getActivityType()));
        }
        for (int i2 = array2.size() - 1; i2 >= 0; i2--) {
            result.add(Integer.valueOf(array2.valueAt(i2).getActivityType()));
        }
        for (int i3 = array3.size() - 1; i3 >= 0; i3--) {
            result.add(Integer.valueOf(array3.valueAt(i3).getActivityType()));
        }
        return result;
    }

    private static AppWindowToken lookForHighestTokenWithFilter(ArraySet<AppWindowToken> array1, ArraySet<AppWindowToken> array2, ArraySet<AppWindowToken> array3, Predicate<AppWindowToken> filter) {
        AppWindowToken wtoken;
        int array2base = array1.size();
        int array3base = array2.size() + array2base;
        int count = array3.size() + array3base;
        int bestPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken bestToken = null;
        for (int i = 0; i < count; i++) {
            if (i < array2base) {
                wtoken = array1.valueAt(i);
            } else if (i < array3base) {
                wtoken = array2.valueAt(i - array2base);
            } else {
                wtoken = array3.valueAt(i - array3base);
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

    /* JADX INFO: finally extract failed */
    private void handleOpeningApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        int appsCount = openingApps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = openingApps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now opening app" + wtoken);
            }
            if (!wtoken.commitVisibility(animLp, true, transit, false, voiceInteraction)) {
                this.mDisplayContent.mNoAnimationNotifyOnTransitionFinished.add(wtoken.token);
            }
            wtoken.updateReportedVisibilityLocked();
            wtoken.waitingToShow = false;
            this.mService.openSurfaceTransaction();
            try {
                wtoken.showAllWindowsLocked();
                this.mService.closeSurfaceTransaction("handleAppTransitionReady");
                if (this.mDisplayContent.mAppTransition.isNextAppTransitionThumbnailUp()) {
                    wtoken.attachThumbnailAnimation();
                } else if (this.mDisplayContent.mAppTransition.isNextAppTransitionOpenCrossProfileApps()) {
                    wtoken.attachCrossProfileAppsThumbnailAnimation();
                }
            } catch (Throwable th) {
                this.mService.closeSurfaceTransaction("handleAppTransitionReady");
                throw th;
            }
        }
    }

    private void handleClosingApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        ArraySet<AppWindowToken> closingApps = this.mDisplayContent.mClosingApps;
        int appsCount = closingApps.size();
        AppWindowToken maxWinHeightToken = this.mService.mHwWMSEx.findExitToLauncherMaxWindowSizeToken(closingApps, appsCount, transit);
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = closingApps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now closing app " + wtoken);
            }
            if (maxWinHeightToken == wtoken) {
                wtoken.mShouldDrawIcon = true;
            }
            if (this.mService.mHwWMSEx.isRightInMagicWindow(wtoken.findMainWindow())) {
                wtoken.mShouldDrawIcon = false;
            }
            wtoken.commitVisibility(animLp, false, transit, false, voiceInteraction);
            wtoken.updateReportedVisibilityLocked();
            wtoken.allDrawn = true;
            wtoken.deferClearAllDrawn = false;
            if (wtoken.startingWindow != null && !wtoken.startingWindow.mAnimatingExit) {
                wtoken.removeStartingWindow();
            }
            if (this.mDisplayContent.mAppTransition.isNextAppTransitionThumbnailDown()) {
                wtoken.attachThumbnailAnimation();
            }
        }
    }

    /* JADX INFO: finally extract failed */
    private void handleChangingApps(int transit, WindowManager.LayoutParams animLp, boolean voiceInteraction) {
        ArraySet<AppWindowToken> apps = this.mDisplayContent.mChangingApps;
        int appsCount = apps.size();
        for (int i = 0; i < appsCount; i++) {
            AppWindowToken wtoken = apps.valueAt(i);
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                Slog.v(TAG, "Now changing app" + wtoken);
            }
            wtoken.cancelAnimationOnly();
            wtoken.applyAnimationLocked(null, transit, true, false);
            wtoken.updateReportedVisibilityLocked();
            this.mService.openSurfaceTransaction();
            try {
                wtoken.showAllWindowsLocked();
                this.mService.closeSurfaceTransaction("handleChangingApps");
                TaskStack taskStack = wtoken.getStack();
                if (taskStack.isTvSplitExitTop()) {
                    taskStack.setTvSplitExitTop(false);
                    Slog.i(TAG, "setTvSplitExitTop false");
                }
            } catch (Throwable th) {
                this.mService.closeSurfaceTransaction("handleChangingApps");
                throw th;
            }
        }
    }

    private void handleNonAppWindowsInTransition(int transit, int flags) {
        boolean z = false;
        if (transit == 20 && (flags & 4) != 0 && (flags & 2) == 0) {
            Animation anim = this.mService.mPolicy.createKeyguardWallpaperExit((flags & 1) != 0);
            if (anim != null) {
                this.mDisplayContent.mWallpaperController.startWallpaperAnimation(anim);
            }
        }
        if (transit == 20 || transit == 21) {
            DisplayContent displayContent = this.mDisplayContent;
            boolean z2 = transit == 21;
            if ((flags & 1) != 0) {
                z = true;
            }
            displayContent.startKeyguardExitOnNonAppWindows(z2, z);
        }
    }

    private boolean transitionGoodToGo(ArraySet<AppWindowToken> apps, SparseIntArray outReasons) {
        Slog.i(TAG, "Checking " + apps.size() + " opening apps (frozen=" + this.mService.mDisplayFrozen + " timeout=" + this.mDisplayContent.mAppTransition.isTimeout() + ")...");
        ScreenRotationAnimation screenRotationAnimation = this.mService.mAnimator.getScreenRotationAnimationLocked(0);
        if (this.mService.getFoldDisplayMode() == 4 && CoordinationModeUtils.getInstance(this.mService.mContext).getCoordinationState() == 3) {
            return false;
        }
        if (this.mDisplayContent.mAppTransition.isTimeout()) {
            return true;
        }
        if (screenRotationAnimation == null || !screenRotationAnimation.isAnimating() || !this.mService.getDefaultDisplayContentLocked().rotationNeedsUpdate()) {
            for (int i = 0; i < apps.size(); i++) {
                AppWindowToken wtoken = apps.valueAt(i);
                Slog.i(TAG, "Check opening app=" + wtoken + ": allDrawn=" + wtoken.allDrawn + " startingDisplayed=" + wtoken.startingDisplayed + " startingMoved=" + wtoken.startingMoved + " isRelaunching()=" + wtoken.isRelaunching() + " startingWindow=" + wtoken.startingWindow);
                boolean allDrawn = wtoken.allDrawn && !wtoken.isRelaunching();
                if (!(allDrawn || wtoken.startingDisplayed || wtoken.startingMoved)) {
                    return false;
                }
                int windowingMode = wtoken.getWindowingMode();
                if (allDrawn) {
                    outReasons.put(windowingMode, 2);
                } else {
                    outReasons.put(windowingMode, wtoken.mStartingData instanceof SplashScreenStartingData ? 1 : 4);
                }
            }
            if (this.mDisplayContent.mAppTransition.isFetchingAppTransitionsSpecs()) {
                Slog.i(TAG, "isFetchingAppTransitionSpecs=true");
                return false;
            } else if (!this.mDisplayContent.mUnknownAppVisibilityController.allResolved()) {
                Slog.i(TAG, "unknownApps is not empty: " + this.mDisplayContent.mUnknownAppVisibilityController.getDebugMessage());
                return false;
            } else {
                if (!this.mWallpaperControllerLocked.isWallpaperVisible() || this.mWallpaperControllerLocked.wallpaperTransitionReady()) {
                    return true;
                }
                return false;
            }
        } else {
            Slog.i(TAG, "Delaying app transition for screen rotation animation to finish");
            return false;
        }
    }

    private int maybeUpdateTransitToWallpaper(int transit, boolean openingAppHasWallpaper, boolean closingAppHasWallpaper) {
        WindowState oldWallpaper;
        int transit2 = transit;
        if (transit2 == 0 || transit2 == 26 || transit2 == 19 || AppTransition.isChangeTransit(transit)) {
            return transit2;
        }
        WindowState wallpaperTarget = this.mWallpaperControllerLocked.getWallpaperTarget();
        boolean showWallpaper = (wallpaperTarget == null || (wallpaperTarget.mAttrs.flags & 1048576) == 0) ? false : true;
        if (this.mWallpaperControllerLocked.isWallpaperTargetAnimating() || !showWallpaper) {
            oldWallpaper = null;
        } else {
            oldWallpaper = wallpaperTarget;
        }
        ArraySet<AppWindowToken> openingApps = this.mDisplayContent.mOpeningApps;
        ArraySet<AppWindowToken> closingApps = this.mDisplayContent.mClosingApps;
        AppWindowToken topOpeningApp = getTopApp(this.mDisplayContent.mOpeningApps, false);
        AppWindowToken topClosingApp = getTopApp(this.mDisplayContent.mClosingApps, true);
        boolean openingCanBeWallpaperTarget = canBeWallpaperTarget(openingApps);
        if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
            Slog.v(TAG, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", openingApps=" + openingApps + ", closingApps=" + closingApps);
        }
        if (openingCanBeWallpaperTarget) {
            if (transit2 == 20) {
                transit2 = 21;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit: " + AppTransition.appTransitionToString(21));
                }
                Flog.i(310, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", openingApps=" + openingApps + ", closingApps=" + closingApps + ", New transit into wallpaper: " + AppTransition.appTransitionToString(transit2));
                return transit2;
            }
        }
        if (!AppTransition.isKeyguardGoingAwayTransit(transit)) {
            if (closingAppHasWallpaper && openingAppHasWallpaper) {
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "Wallpaper animation!");
                }
                switch (transit2) {
                    case 6:
                    case 8:
                    case 10:
                        transit2 = 14;
                        break;
                    case 7:
                    case 9:
                    case WindowManagerService.H.WINDOW_FREEZE_TIMEOUT /* 11 */:
                        transit2 = 15;
                        break;
                }
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit: " + AppTransition.appTransitionToString(transit2));
                }
            } else if (oldWallpaper != null && !this.mDisplayContent.mOpeningApps.isEmpty() && !openingApps.contains(oldWallpaper.mAppToken) && closingApps.contains(oldWallpaper.mAppToken) && topClosingApp == oldWallpaper.mAppToken) {
                transit2 = 12;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit away from wallpaper: " + AppTransition.appTransitionToString(12));
                }
            } else if (wallpaperTarget != null && wallpaperTarget.isVisibleLw() && openingApps.contains(wallpaperTarget.mAppToken) && topOpeningApp == wallpaperTarget.mAppToken && transit2 != 25) {
                transit2 = 13;
                if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS) {
                    Slog.v(TAG, "New transit into wallpaper: " + AppTransition.appTransitionToString(13));
                }
            }
        }
        Flog.i(310, "New wallpaper target=" + wallpaperTarget + ", oldWallpaper=" + oldWallpaper + ", openingApps=" + openingApps + ", closingApps=" + closingApps + ", New transit into wallpaper: " + AppTransition.appTransitionToString(transit2));
        return transit2;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int maybeUpdateTransitToTranslucentAnim(int transit) {
        if (AppTransition.isChangeTransit(transit)) {
            return transit;
        }
        boolean taskOrActivity = AppTransition.isTaskTransit(transit) || AppTransition.isActivityTransit(transit);
        boolean allOpeningVisible = true;
        boolean allTranslucentOpeningApps = !this.mDisplayContent.mOpeningApps.isEmpty();
        for (int i = this.mDisplayContent.mOpeningApps.size() - 1; i >= 0; i--) {
            AppWindowToken token = this.mDisplayContent.mOpeningApps.valueAt(i);
            if (!token.isVisible()) {
                allOpeningVisible = false;
                if (token.fillsParent()) {
                    allTranslucentOpeningApps = false;
                }
            }
        }
        boolean allTranslucentClosingApps = !this.mDisplayContent.mClosingApps.isEmpty();
        int i2 = this.mDisplayContent.mClosingApps.size() - 1;
        while (true) {
            if (i2 < 0) {
                break;
            } else if (this.mDisplayContent.mClosingApps.valueAt(i2).fillsParent()) {
                allTranslucentClosingApps = false;
                break;
            } else {
                i2--;
            }
        }
        Flog.i(310, "maybe update transition to translucent anim: taskOrActivity=" + taskOrActivity + " allTranslucentClosingApps=" + allTranslucentClosingApps + " allOpeningVisible=" + allOpeningVisible + " allTranslucentOpeningApps=" + allTranslucentOpeningApps + " closingAppsIsEmpty=" + this.mDisplayContent.mClosingApps.isEmpty());
        if (taskOrActivity && allTranslucentClosingApps && allOpeningVisible) {
            return 25;
        }
        if (!taskOrActivity || !allTranslucentOpeningApps || !this.mDisplayContent.mClosingApps.isEmpty()) {
            return transit;
        }
        return 24;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isTransitWithinTask(int transit, Task task) {
        if (task == null || !this.mDisplayContent.mChangingApps.isEmpty()) {
            return false;
        }
        if (transit != 6 && transit != 7 && transit != 18) {
            return false;
        }
        Iterator<AppWindowToken> it = this.mDisplayContent.mOpeningApps.iterator();
        while (it.hasNext()) {
            if (it.next().getTask() != task) {
                return false;
            }
        }
        Iterator<AppWindowToken> it2 = this.mDisplayContent.mClosingApps.iterator();
        while (it2.hasNext()) {
            if (it2.next().getTask() != task) {
                return false;
            }
        }
        return true;
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
        int prefixOrderIndex;
        int topPrefixOrderIndex = Integer.MIN_VALUE;
        AppWindowToken topApp = null;
        for (int i = apps.size() - 1; i >= 0; i--) {
            AppWindowToken app = apps.valueAt(i);
            if ((!ignoreHidden || !app.isHidden()) && (prefixOrderIndex = app.getPrefixOrderIndex()) > topPrefixOrderIndex) {
                topPrefixOrderIndex = prefixOrderIndex;
                topApp = app;
            }
        }
        return topApp;
    }

    private void processApplicationsAnimatingInPlace(int transit) {
        WindowState win;
        if (transit == 17 && (win = this.mDisplayContent.findFocusedWindow()) != null) {
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

    private void notifyWindowStateChange() {
        this.mService.mAtmService.mHwATMSEx.notifyTvSplitWindowStateChange();
    }
}
