package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.iawareperf.UniPerf;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.ArrayMap;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Slog;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.WindowManager.LayoutParams;
import com.android.internal.util.ArrayUtils;
import com.android.server.EventLogTags;
import com.android.server.HwServiceFactory;
import com.android.server.HwServiceFactory.IDisplayEffectMonitor;
import com.android.server.power.IHwShutdownThread;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass1;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass2;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass3;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass4;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass5;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass6;
import com.android.server.wm.-$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0.AnonymousClass7;
import com.huawei.pgmng.log.LogPower;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

class RootWindowContainer extends WindowContainer<DisplayContent> {
    private static final int SET_SCREEN_BRIGHTNESS_OVERRIDE = 1;
    private static final int SET_USER_ACTIVITY_TIMEOUT = 2;
    private static final String TAG = "WindowManager";
    private static final Consumer<WindowState> sRemoveReplacedWindowsConsumer = new -$Lambda$cHAc_wCK_9-nlRTF5Ggz5ZbNDr0();
    private float mAppBrightnessLast = -1.0f;
    String mAppBrightnessPackageName;
    private String mAppBrightnessPackageNameLast = "";
    private final ArrayList<Integer> mChangedStackList = new ArrayList();
    private final Consumer<WindowState> mCloseSystemDialogsConsumer = new AnonymousClass1(this);
    private String mCloseSystemDialogsReason;
    private IDisplayEffectMonitor mDisplayEffectMonitor;
    private final Handler mHandler;
    private Session mHoldScreen = null;
    WindowState mHoldScreenWindow = null;
    private Object mLastWindowFreezeSource = null;
    private final WindowLayersController mLayersController;
    private boolean mObscureApplicationContentOnSecondaryDisplays = false;
    WindowState mObscuringWindow = null;
    boolean mOrientationChangeComplete = true;
    RemoteEventTrace mRemoteEventTrace;
    private float mScreenBrightness = -1.0f;
    WindowManagerService mService;
    boolean mSurfaceTraceEnabled;
    ParcelFileDescriptor mSurfaceTraceFd;
    private boolean mSustainedPerformanceModeCurrent = false;
    private boolean mSustainedPerformanceModeEnabled = false;
    private boolean mUpdateRotation = false;
    private long mUserActivityTimeout = -1;
    boolean mWallpaperActionPending = false;
    final WallpaperController mWallpaperController;
    private boolean mWallpaperForceHidingChanged = false;
    boolean mWallpaperMayChange = false;

    private final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    RootWindowContainer.this.mService.mPowerManagerInternal.setScreenBrightnessOverrideFromWindowManager(msg.arg1);
                    return;
                case 2:
                    RootWindowContainer.this.mService.mPowerManagerInternal.setUserActivityTimeoutOverrideFromWindowManager(((Long) msg.obj).longValue());
                    return;
                default:
                    return;
            }
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_8205(WindowState w) {
        if (w.mHasSurface) {
            try {
                w.mClient.closeSystemDialogs(this.mCloseSystemDialogsReason);
            } catch (RemoteException e) {
            }
        }
    }

    static /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_8486(WindowState w) {
        AppWindowToken aToken = w.mAppToken;
        if (aToken != null) {
            aToken.removeReplacedWindowIfNeeded(w);
        }
    }

    private void sendBrightnessToMonitor(float brightness, String packageName) {
        if (this.mDisplayEffectMonitor != null && packageName != null) {
            if (((double) Math.abs(brightness - this.mAppBrightnessLast)) > 1.0E-7d || (this.mAppBrightnessPackageNameLast.equals(packageName) ^ 1) != 0) {
                ArrayMap<String, Object> params = new ArrayMap();
                params.put("paramType", "windowManagerBrightness");
                params.put("brightness", Integer.valueOf(toBrightnessOverride(brightness)));
                params.put("packageName", packageName);
                this.mDisplayEffectMonitor.sendMonitorParam(params);
                this.mAppBrightnessLast = brightness;
                this.mAppBrightnessPackageNameLast = packageName;
            }
        }
    }

    RootWindowContainer(WindowManagerService service) {
        this.mService = service;
        this.mHandler = new MyHandler(service.mH.getLooper());
        this.mLayersController = new WindowLayersController(this.mService);
        this.mWallpaperController = new WallpaperController(this.mService);
        this.mDisplayEffectMonitor = HwServiceFactory.getDisplayEffectMonitor(this.mService.mContext);
        if (this.mDisplayEffectMonitor == null) {
            Slog.e(TAG, "HwServiceFactory getDisplayEffectMonitor failed!");
        }
    }

    WindowState computeFocusedWindow() {
        WindowState win;
        if (HwPCUtils.isPcCastModeInServer()) {
            DisplayContent dc = getDisplayContent(this.mService.getFocusedDisplayId());
            if (dc != null) {
                win = dc.findFocusedWindow();
                if (win != null) {
                    return win;
                }
            }
        }
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            win = ((DisplayContent) this.mChildren.get(i)).findFocusedWindow();
            if (win != null) {
                return win;
            }
        }
        return null;
    }

    void getDisplaysInFocusOrder(SparseIntArray displaysInFocusOrder) {
        displaysInFocusOrder.clear();
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            DisplayContent displayContent = (DisplayContent) this.mChildren.get(i);
            if (!displayContent.isRemovalDeferred()) {
                displaysInFocusOrder.put(i, displayContent.getDisplayId());
            }
        }
    }

    DisplayContent getDisplayContentOrCreate(int displayId) {
        DisplayContent dc = getDisplayContent(displayId);
        if (dc == null) {
            Display display = this.mService.mDisplayManager.getDisplay(displayId);
            if (display != null) {
                long callingIdentity = Binder.clearCallingIdentity();
                try {
                    dc = createDisplayContent(display);
                } finally {
                    Binder.restoreCallingIdentity(callingIdentity);
                }
            }
        }
        return dc;
    }

    DisplayContent getDisplayContent(int displayId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            DisplayContent current = (DisplayContent) this.mChildren.get(i);
            if (current.getDisplayId() == displayId) {
                return current;
            }
        }
        return null;
    }

    private DisplayContent createDisplayContent(Display display) {
        DisplayContent dc = HwServiceFactory.createDisplayContent(display, this.mService, this.mLayersController, this.mWallpaperController);
        int displayId = display.getDisplayId();
        DisplayInfo displayInfo = dc.getDisplayInfo();
        Rect rect = new Rect();
        this.mService.mDisplaySettings.getOverscanLocked(displayInfo.name, displayInfo.uniqueId, rect);
        displayInfo.overscanLeft = rect.left;
        displayInfo.overscanTop = rect.top;
        displayInfo.overscanRight = rect.right;
        displayInfo.overscanBottom = rect.bottom;
        if (this.mService.mDisplayManagerInternal != null) {
            this.mService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(displayId, displayInfo);
            this.mService.configureDisplayPolicyLocked(dc);
            if (displayId == 0 && this.mService.canDispatchPointerEvents()) {
                dc.mTapDetector = new TaskTapPointerEventListener(this.mService, dc);
                this.mService.registerPointerEventListener(dc.mTapDetector);
                this.mService.registerPointerEventListener(this.mService.mMousePositionTracker);
            }
            if (HwPCUtils.enabled() && displayId != -1 && displayId != 0 && displayInfo.type == 2 && this.mService.canDispatchExternalPointerEvents()) {
                dc.mTapDetector = new TaskTapPointerEventListener(this.mService, dc);
                this.mService.registerExternalPointerEventListener(dc.mTapDetector);
                try {
                    this.mService.registerExternalPointerEventListener(this.mService.mMousePositionTracker);
                } catch (Exception e) {
                    Slog.w(TAG, "register external pointer event listener", e);
                }
            }
        }
        return dc;
    }

    boolean isLayoutNeeded() {
        int numDisplays = this.mChildren.size();
        for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            if (((DisplayContent) this.mChildren.get(displayNdx)).isLayoutNeeded()) {
                return true;
            }
        }
        return false;
    }

    void getWindowsByName(ArrayList<WindowState> output, String name) {
        int objectId = 0;
        try {
            objectId = Integer.parseInt(name, 16);
            name = null;
        } catch (RuntimeException e) {
        }
        getWindowsByName(output, name, objectId);
    }

    private void getWindowsByName(ArrayList<WindowState> output, String name, int objectId) {
        forAllWindows((Consumer) new AnonymousClass5(objectId, name, output), true);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_16716(String name, ArrayList output, int objectId, WindowState w) {
        if (name != null) {
            if (w.mAttrs.getTitle().toString().contains(name)) {
                output.add(w);
            }
        } else if (System.identityHashCode(w) == objectId) {
            output.add(w);
        }
    }

    AppWindowToken getAppWindowToken(IBinder binder) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            AppWindowToken atoken = ((DisplayContent) this.mChildren.get(i)).getAppWindowToken(binder);
            if (atoken != null) {
                return atoken;
            }
        }
        return null;
    }

    DisplayContent getWindowTokenDisplay(WindowToken token) {
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

    int[] setDisplayOverrideConfigurationIfNeeded(Configuration newConfiguration, int displayId) {
        DisplayContent displayContent = getDisplayContent(displayId);
        if (displayContent == null) {
            throw new IllegalArgumentException("Display not found for id: " + displayId);
        }
        if (!(displayContent.getOverrideConfiguration().diff(newConfiguration) != 0)) {
            return null;
        }
        displayContent.onOverrideConfigurationChanged(newConfiguration);
        if (displayId == 0) {
            return setGlobalConfigurationIfNeeded(newConfiguration);
        }
        return updateStackBoundsAfterConfigChange(displayId);
    }

    private int[] setGlobalConfigurationIfNeeded(Configuration newConfiguration) {
        if (!(getConfiguration().diff(newConfiguration) != 0)) {
            return null;
        }
        onConfigurationChanged(newConfiguration);
        return updateStackBoundsAfterConfigChange();
    }

    void onConfigurationChanged(Configuration newParentConfig) {
        prepareFreezingTaskBounds();
        super.onConfigurationChanged(newParentConfig);
        this.mService.mPolicy.onConfigurationChanged();
    }

    private int[] updateStackBoundsAfterConfigChange() {
        this.mChangedStackList.clear();
        int numDisplays = this.mChildren.size();
        for (int i = 0; i < numDisplays; i++) {
            ((DisplayContent) this.mChildren.get(i)).updateStackBoundsAfterConfigChange(this.mChangedStackList);
        }
        return this.mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(this.mChangedStackList);
    }

    private int[] updateStackBoundsAfterConfigChange(int displayId) {
        this.mChangedStackList.clear();
        getDisplayContent(displayId).updateStackBoundsAfterConfigChange(this.mChangedStackList);
        return this.mChangedStackList.isEmpty() ? null : ArrayUtils.convertToIntArray(this.mChangedStackList);
    }

    private void prepareFreezingTaskBounds() {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).prepareFreezingTaskBounds();
        }
    }

    TaskStack getStackById(int stackId) {
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            TaskStack stack = ((DisplayContent) this.mChildren.get(i)).getStackById(stackId);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }

    void setSecureSurfaceState(int userId, boolean disabled) {
        forAllWindows((Consumer) new AnonymousClass7(disabled, userId), true);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_21585(int userId, boolean disabled, WindowState w) {
        if (w.mHasSurface && userId == UserHandle.getUserId(w.mOwnerUid)) {
            w.mWinAnimator.setSecureLocked(disabled);
        }
    }

    void updateAppOpsState() {
        forAllWindows((Consumer) new AnonymousClass2(this), false);
    }

    /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_21850(WindowState w) {
        if (w.mAppOp != -1) {
            this.mService.setAppOpVisibilityLwHook(w, this.mService.mAppOps.checkOpNoThrow(w.mAppOp, w.getOwningUid(), w.getOwningPackage()));
        }
    }

    static /* synthetic */ boolean lambda$-com_android_server_wm_RootWindowContainer_22491(int pid, WindowState w) {
        return w.mSession.mPid == pid ? w.isVisibleLw() : false;
    }

    boolean canShowStrictModeViolation(int pid) {
        return getWindow(new AnonymousClass4(pid)) != null;
    }

    void closeSystemDialogs(String reason) {
        this.mCloseSystemDialogsReason = reason;
        forAllWindows(this.mCloseSystemDialogsConsumer, false);
    }

    void removeReplacedWindows() {
        this.mService.openSurfaceTransaction();
        try {
            forAllWindows(sRemoveReplacedWindowsConsumer, true);
        } finally {
            this.mService.closeSurfaceTransaction();
        }
    }

    boolean hasPendingLayoutChanges(WindowAnimator animator) {
        boolean hasChanges = false;
        int count = this.mChildren.size();
        for (int i = 0; i < count; i++) {
            int pendingChanges = animator.getPendingLayoutChanges(((DisplayContent) this.mChildren.get(i)).getDisplayId());
            if ((pendingChanges & 4) != 0) {
                animator.mBulkUpdateParams |= 32;
            }
            if (pendingChanges != 0) {
                hasChanges = true;
            }
        }
        return hasChanges;
    }

    boolean reclaimSomeSurfaceMemory(WindowStateAnimator winAnimator, String operation, boolean secure) {
        WindowSurfaceController surfaceController = winAnimator.mSurfaceController;
        int leakedSurface = 0;
        boolean killedApps = false;
        EventLog.writeEvent(EventLogTags.WM_NO_SURFACE_MEMORY, new Object[]{winAnimator.mWin.toString(), Integer.valueOf(winAnimator.mSession.mPid), operation});
        long callingIdentity = Binder.clearCallingIdentity();
        try {
            int displayNdx;
            Slog.i(TAG, "Out of memory for surface!  Looking for leaks...");
            int numDisplays = this.mChildren.size();
            for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                leakedSurface |= ((DisplayContent) this.mChildren.get(displayNdx)).destroyLeakedSurfaces();
            }
            if (leakedSurface == 0) {
                Slog.w(TAG, "No leaked surfaces; killing applications!");
                SparseIntArray pidCandidates = new SparseIntArray();
                for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                    ((DisplayContent) this.mChildren.get(displayNdx)).forAllWindows((Consumer) new AnonymousClass3(this, pidCandidates), false);
                    if (pidCandidates.size() > 0) {
                        int[] pids = new int[pidCandidates.size()];
                        for (int i = 0; i < pids.length; i++) {
                            pids[i] = pidCandidates.keyAt(i);
                        }
                        try {
                            if (this.mService.mActivityManager.killPids(pids, "Free memory", secure)) {
                                killedApps = true;
                            }
                        } catch (RemoteException e) {
                        }
                    }
                }
            }
            if (leakedSurface != 0 || killedApps) {
                Slog.w(TAG, "Looks like we have reclaimed some memory, clearing surface for retry.");
                if (surfaceController != null) {
                    winAnimator.destroySurface();
                    if (!(winAnimator.mWin.mAppToken == null || winAnimator.mWin.mAppToken.getController() == null)) {
                        winAnimator.mWin.mAppToken.getController().removeStartingWindow();
                    }
                }
                try {
                    winAnimator.mWin.mClient.dispatchGetNewSurface();
                } catch (RemoteException e2) {
                }
            }
            Binder.restoreCallingIdentity(callingIdentity);
            return leakedSurface == 0 ? killedApps : true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(callingIdentity);
        }
    }

    /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_25154(SparseIntArray pidCandidates, WindowState w) {
        if (!this.mService.mForceRemoves.contains(w)) {
            WindowStateAnimator wsa = w.mWinAnimator;
            if (wsa.mSurfaceController != null) {
                pidCandidates.append(wsa.mSession.mPid, wsa.mSession.mPid);
            }
        }
    }

    void performSurfacePlacement(boolean recoveringMemory) {
        int displayNdx;
        int i;
        WindowState win;
        DisplayContent displayContent;
        boolean updateInputWindowsNeeded = false;
        if (this.mService.mFocusMayChange) {
            this.mService.mFocusMayChange = false;
            updateInputWindowsNeeded = this.mService.updateFocusedWindowLocked(3, false);
        }
        int numDisplays = this.mChildren.size();
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ((DisplayContent) this.mChildren.get(displayNdx)).setExitingTokensHasVisible(false);
        }
        this.mHoldScreen = null;
        this.mScreenBrightness = -1.0f;
        this.mUserActivityTimeout = -1;
        this.mObscureApplicationContentOnSecondaryDisplays = false;
        this.mSustainedPerformanceModeCurrent = false;
        WindowManagerService windowManagerService = this.mService;
        windowManagerService.mTransactionSequence++;
        DisplayContent defaultDisplay = this.mService.getDefaultDisplayContentLocked();
        DisplayInfo defaultInfo = defaultDisplay.getDisplayInfo();
        int defaultDw = defaultInfo.logicalWidth;
        int defaultDh = defaultInfo.logicalHeight;
        this.mService.openSurfaceTransaction();
        try {
            applySurfaceChangesTransaction(recoveringMemory, defaultDw, defaultDh);
        } catch (RuntimeException e) {
            Slog.wtf(TAG, "Unhandled exception in Window Manager", e);
        } finally {
            this.mService.closeSurfaceTransaction();
        }
        WindowSurfacePlacer surfacePlacer = this.mService.mWindowPlacerLocked;
        if (this.mService.mAppTransition.isReady()) {
            defaultDisplay.pendingLayoutChanges |= surfacePlacer.handleAppTransitionReadyLocked();
        }
        if (!this.mService.mAnimator.mAppWindowAnimating && this.mService.mAppTransition.isRunning()) {
            defaultDisplay.pendingLayoutChanges |= this.mService.handleAnimatingStoppedAndTransitionLocked();
        }
        if (this.mWallpaperForceHidingChanged && defaultDisplay.pendingLayoutChanges == 0 && (this.mService.mAppTransition.isReady() ^ 1) != 0) {
            defaultDisplay.pendingLayoutChanges |= 1;
        }
        this.mWallpaperForceHidingChanged = false;
        if (this.mWallpaperMayChange) {
            defaultDisplay.pendingLayoutChanges |= 4;
        }
        if (this.mService.mFocusMayChange) {
            this.mService.mFocusMayChange = false;
            if (this.mService.updateFocusedWindowLocked(2, false)) {
                updateInputWindowsNeeded = true;
                defaultDisplay.pendingLayoutChanges |= 8;
            }
        }
        if (isLayoutNeeded()) {
            defaultDisplay.pendingLayoutChanges |= 1;
        }
        for (i = this.mService.mResizingWindows.size() - 1; i >= 0; i--) {
            win = (WindowState) this.mService.mResizingWindows.get(i);
            if (!win.mAppFreezing) {
                if (win.mAppToken != null) {
                    win.mAppToken.destroySavedSurfaces();
                }
                win.reportResized();
                this.mService.mResizingWindows.remove(i);
            }
        }
        if (this.mOrientationChangeComplete) {
            if (this.mService.mDisplayFrozen) {
                if (this.mLastWindowFreezeSource != null) {
                    Jlog.d(59, Jlog.extractAppName(this.mLastWindowFreezeSource.toString()), "");
                } else {
                    Jlog.d(59, "");
                }
                if (this.mService.mIsPerfBoost) {
                    this.mService.mIsPerfBoost = false;
                    UniPerf.getInstance().uniPerfEvent(4105, "", new int[]{-1});
                }
                LogPower.push(130, Integer.toString(this.mService.getDefaultDisplayRotation()));
            }
            if (this.mService.mWindowsFreezingScreen != 0) {
                this.mService.mWindowsFreezingScreen = 0;
                this.mService.mLastFinishedFreezeSource = this.mLastWindowFreezeSource;
                this.mService.mH.removeMessages(11);
            }
            if (this.mService.mDisplayFrozen) {
                Slog.i(TAG, "orientation change is complete, call stopFreezingDisplayLocked");
            }
            this.mService.stopFreezingDisplayLocked();
        }
        boolean wallpaperDestroyed = false;
        i = this.mService.mDestroySurface.size();
        if (i > 0) {
            while (true) {
                i--;
                win = (WindowState) this.mService.mDestroySurface.get(i);
                win.mDestroying = false;
                if (this.mService.mInputMethodWindow == win) {
                    this.mService.setInputMethodWindowLocked(null);
                }
                if (win.getDisplayContent().mWallpaperController.isWallpaperTarget(win)) {
                    wallpaperDestroyed = true;
                }
                win.destroyOrSaveSurfaceUnchecked();
                if (i <= 0) {
                    break;
                }
            }
            this.mService.mDestroySurface.clear();
        }
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            ((DisplayContent) this.mChildren.get(displayNdx)).removeExistingTokensIfPossible();
        }
        if (wallpaperDestroyed) {
            defaultDisplay.pendingLayoutChanges |= 4;
            defaultDisplay.setLayoutNeeded();
        }
        for (displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
            displayContent = (DisplayContent) this.mChildren.get(displayNdx);
            if (displayContent.pendingLayoutChanges != 0) {
                displayContent.setLayoutNeeded();
            }
        }
        this.mService.mInputMonitor.updateInputWindowsLw(true);
        this.mService.setHoldScreenLocked(this.mHoldScreen);
        if (!this.mService.mDisplayFrozen) {
            if (this.mScreenBrightness < 0.0f) {
                this.mAppBrightnessPackageName = "android";
                sendBrightnessToMonitor(-1.0f, this.mAppBrightnessPackageName);
            } else {
                sendBrightnessToMonitor(this.mScreenBrightness, this.mAppBrightnessPackageName);
            }
            this.mHandler.obtainMessage(1, this.mScreenBrightness < 0.0f ? -1 : toBrightnessOverride(this.mScreenBrightness), 0).sendToTarget();
            this.mHandler.obtainMessage(2, Long.valueOf(this.mUserActivityTimeout)).sendToTarget();
        }
        if (this.mSustainedPerformanceModeCurrent != this.mSustainedPerformanceModeEnabled) {
            this.mSustainedPerformanceModeEnabled = this.mSustainedPerformanceModeCurrent;
            this.mService.mPowerManagerInternal.powerHint(6, this.mSustainedPerformanceModeEnabled ? 1 : 0);
        }
        if (this.mService.mTurnOnScreen) {
            if (this.mService.mAllowTheaterModeWakeFromLayout || Global.getInt(this.mService.mContext.getContentResolver(), "theater_mode_on", 0) == 0) {
                Slog.v(TAG, "Turning screen on after layout!");
                this.mService.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "android.server.wm:TURN_ON");
            }
            this.mService.mTurnOnScreen = false;
        }
        if (this.mUpdateRotation) {
            int displayId = defaultDisplay.getDisplayId();
            if (defaultDisplay.updateRotationUnchecked(false)) {
                this.mService.mH.obtainMessage(18, Integer.valueOf(displayId)).sendToTarget();
            } else {
                this.mUpdateRotation = false;
            }
        }
        if (!(this.mService.mWaitingForDrawnCallback == null && (!this.mOrientationChangeComplete || (defaultDisplay.isLayoutNeeded() ^ 1) == 0 || (this.mUpdateRotation ^ 1) == 0))) {
            this.mService.checkDrawnWindowsLocked();
        }
        if (this.mService.mKeyguardDismissDoneCallback != null) {
            this.mService.checkKeyguardDismissDoneLocked();
        }
        int N = this.mService.mPendingRemove.size();
        if (N > 0) {
            if (this.mService.mPendingRemoveTmp.length < N) {
                this.mService.mPendingRemoveTmp = new WindowState[(N + 10)];
            }
            this.mService.mPendingRemove.toArray(this.mService.mPendingRemoveTmp);
            this.mService.mPendingRemove.clear();
            ArrayList<DisplayContent> displayList = new ArrayList();
            for (i = 0; i < N; i++) {
                WindowState w = this.mService.mPendingRemoveTmp[i];
                w.removeImmediately();
                displayContent = w.getDisplayContent();
                if (!(displayContent == null || (displayList.contains(displayContent) ^ 1) == 0)) {
                    displayList.add(displayContent);
                }
            }
            for (int j = displayList.size() - 1; j >= 0; j--) {
                ((DisplayContent) displayList.get(j)).assignWindowLayers(true);
            }
        }
        for (displayNdx = this.mChildren.size() - 1; displayNdx >= 0; displayNdx--) {
            ((DisplayContent) this.mChildren.get(displayNdx)).checkCompleteDeferredRemoval();
        }
        if (updateInputWindowsNeeded) {
            this.mService.mInputMonitor.updateInputWindowsLw(false);
        }
        this.mService.setFocusTaskRegionLocked(null);
        this.mService.enableScreenIfNeededLocked();
        this.mService.scheduleAnimationLocked();
        this.mService.mWindowPlacerLocked.destroyPendingSurfaces();
    }

    private void applySurfaceChangesTransaction(boolean recoveringMemory, int defaultDw, int defaultDh) {
        this.mHoldScreenWindow = null;
        this.mObscuringWindow = null;
        if (this.mService.mWatermark != null) {
            this.mService.mWatermark.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mStrictModeFlash != null) {
            this.mService.mStrictModeFlash.positionSurface(defaultDw, defaultDh);
        }
        if (this.mService.mCircularDisplayMask != null) {
            this.mService.mCircularDisplayMask.positionSurface(defaultDw, defaultDh, this.mService.getDefaultDisplayRotation());
        }
        if (this.mService.mEmulatorDisplayOverlay != null) {
            this.mService.mEmulatorDisplayOverlay.positionSurface(defaultDw, defaultDh, this.mService.getDefaultDisplayRotation());
        }
        boolean focusDisplayed = false;
        for (int j = 0; j < this.mChildren.size(); j++) {
            focusDisplayed |= ((DisplayContent) this.mChildren.get(j)).applySurfaceChangesTransaction(recoveringMemory);
        }
        if (focusDisplayed) {
            this.mService.mH.sendEmptyMessage(3);
        }
        this.mService.mDisplayManagerInternal.performTraversalInTransactionFromWindowManager();
    }

    boolean handleNotObscuredLocked(WindowState w, boolean obscured, boolean syswin) {
        LayoutParams attrs = w.mAttrs;
        int attrFlags = attrs.flags;
        boolean canBeSeen = w.isDisplayedLw();
        int privateflags = attrs.privateFlags;
        boolean displayHasContent = false;
        if (w.mAttrs.type == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME && (canBeSeen ^ 1) != 0) {
            canBeSeen = w.mHasSurface ? this.mService.mPolicy.isKeyguardShowingOrOccluded() : false;
            if (canBeSeen) {
                Slog.w(TAG, "reset canBeSeen for statusbar when keyguard on");
            }
        }
        if (w.mHasSurface && canBeSeen) {
            if ((attrFlags & 128) != 0 && isWindowVisibleInKeyguard(attrs) && (!HwPCUtils.isPcCastModeInServer() || w.isDefaultDisplay() || HwPCUtils.enabledInPad())) {
                this.mHoldScreen = w.mSession;
                this.mHoldScreenWindow = w;
            }
            if (!syswin && w.mAttrs.screenBrightness >= 0.0f && this.mScreenBrightness < 0.0f && w.isVisibleLw()) {
                this.mScreenBrightness = w.mAttrs.screenBrightness;
                this.mAppBrightnessPackageName = w.mAttrs.packageName;
            }
            if (!syswin && w.mAttrs.userActivityTimeout >= 0 && this.mUserActivityTimeout < 0) {
                if ((w.mAttrs.privateFlags & 1024) == 0 || !this.mService.mDestroySurface.contains(w)) {
                    this.mUserActivityTimeout = w.mAttrs.userActivityTimeout;
                } else {
                    Slog.e(TAG, "do not set userActivityTimeout this time");
                }
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
            if ((DumpState.DUMP_DOMAIN_PREFERRED & privateflags) != 0) {
                this.mSustainedPerformanceModeCurrent = true;
            }
        }
        return displayHasContent;
    }

    boolean copyAnimToLayoutParams() {
        boolean doRequest = false;
        int bulkUpdateParams = this.mService.mAnimator.mBulkUpdateParams;
        if ((bulkUpdateParams & 1) != 0) {
            this.mUpdateRotation = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 2) != 0) {
            this.mWallpaperMayChange = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 4) != 0) {
            this.mWallpaperForceHidingChanged = true;
            doRequest = true;
        }
        if ((bulkUpdateParams & 8) == 0) {
            this.mOrientationChangeComplete = false;
        } else {
            this.mOrientationChangeComplete = true;
            this.mLastWindowFreezeSource = this.mService.mAnimator.mLastWindowFreezeSource;
            if (this.mService.mWindowsFreezingScreen != 0) {
                doRequest = true;
            }
        }
        if ((bulkUpdateParams & 16) != 0) {
            this.mService.mTurnOnScreen = true;
        }
        if ((bulkUpdateParams & 32) != 0) {
            this.mWallpaperActionPending = true;
        }
        return doRequest;
    }

    private static int toBrightnessOverride(float value) {
        return (int) (255.0f * value);
    }

    void enableSurfaceTrace(ParcelFileDescriptor pfd) {
        FileDescriptor fd = pfd.getFileDescriptor();
        if (this.mSurfaceTraceEnabled) {
            disableSurfaceTrace();
        }
        this.mSurfaceTraceEnabled = true;
        this.mRemoteEventTrace = new RemoteEventTrace(this.mService, fd);
        this.mSurfaceTraceFd = pfd;
        for (int displayNdx = this.mChildren.size() - 1; displayNdx >= 0; displayNdx--) {
            ((DisplayContent) this.mChildren.get(displayNdx)).enableSurfaceTrace(fd);
        }
    }

    void disableSurfaceTrace() {
        this.mSurfaceTraceEnabled = false;
        this.mRemoteEventTrace = null;
        this.mSurfaceTraceFd = null;
        for (int displayNdx = this.mChildren.size() - 1; displayNdx >= 0; displayNdx--) {
            ((DisplayContent) this.mChildren.get(displayNdx)).disableSurfaceTrace();
        }
    }

    void dumpDisplayContents(PrintWriter pw) {
        pw.println("WINDOW MANAGER DISPLAY CONTENTS (dumpsys window displays)");
        if (this.mService.mDisplayReady) {
            int count = this.mChildren.size();
            for (int i = 0; i < count; i++) {
                ((DisplayContent) this.mChildren.get(i)).dump("  ", pw);
            }
            return;
        }
        pw.println("  NO DISPLAY");
    }

    void dumpLayoutNeededDisplayIds(PrintWriter pw) {
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

    void dumpWindowsNoHeader(PrintWriter pw, boolean dumpAll, ArrayList<WindowState> windows) {
        forAllWindows((Consumer) new AnonymousClass6(dumpAll, windows, pw, new int[1]), true);
    }

    static /* synthetic */ void lambda$-com_android_server_wm_RootWindowContainer_52695(ArrayList windows, PrintWriter pw, int[] index, boolean dumpAll, WindowState w) {
        boolean z = true;
        if (windows == null || windows.contains(w)) {
            pw.println("  Window #" + index[0] + " " + w + ":");
            String str = "    ";
            if (!dumpAll && windows == null) {
                z = false;
            }
            w.dump(pw, str, z);
            index[0] = index[0] + 1;
        }
    }

    void dumpTokens(PrintWriter pw, boolean dumpAll) {
        pw.println("  All tokens:");
        for (int i = this.mChildren.size() - 1; i >= 0; i--) {
            ((DisplayContent) this.mChildren.get(i)).dumpTokens(pw, dumpAll);
        }
    }

    String getName() {
        return "ROOT";
    }

    private boolean isWindowVisibleInKeyguard(LayoutParams attrs) {
        boolean z = true;
        if (!this.mService.mPolicy.isKeyguardShowingOrOccluded() || (attrs.flags & DumpState.DUMP_FROZEN) != 0) {
            return true;
        }
        if (attrs.type == 1) {
            z = false;
        }
        return z;
    }
}
