package com.android.server.wm;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Slog;
import android.view.DisplayInfo;
import com.android.server.am.ProcessList;
import com.android.server.power.AbsPowerManagerService;
import com.android.server.usb.UsbAudioDevice;
import java.io.PrintWriter;
import java.util.ArrayList;

class WallpaperController {
    private static final String TAG = null;
    private static final int WALLPAPER_DRAW_NORMAL = 0;
    private static final int WALLPAPER_DRAW_PENDING = 1;
    private static final long WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION = 500;
    private static final int WALLPAPER_DRAW_TIMEOUT = 2;
    private static final long WALLPAPER_TIMEOUT = 150;
    private static final long WALLPAPER_TIMEOUT_RECOVERY = 10000;
    private static boolean mUsingHwNavibar;
    private WindowState mDeferredHideWallpaper;
    private final FindWallpaperTargetResult mFindResults;
    private int mLastWallpaperDisplayOffsetX;
    private int mLastWallpaperDisplayOffsetY;
    private long mLastWallpaperTimeoutTime;
    private float mLastWallpaperX;
    private float mLastWallpaperXStep;
    private float mLastWallpaperY;
    private float mLastWallpaperYStep;
    private WindowState mLowerWallpaperTarget;
    private final WindowManagerService mService;
    private WindowState mUpperWallpaperTarget;
    WindowState mWaitingOnWallpaper;
    private int mWallpaperAnimLayerAdjustment;
    private int mWallpaperDrawState;
    private WindowState mWallpaperTarget;
    private final ArrayList<WindowToken> mWallpaperTokens;

    private static final class FindWallpaperTargetResult {
        WindowState topWallpaper;
        int topWallpaperIndex;
        WindowState wallpaperTarget;
        int wallpaperTargetIndex;

        private FindWallpaperTargetResult() {
            this.topWallpaperIndex = WallpaperController.WALLPAPER_DRAW_NORMAL;
            this.topWallpaper = null;
            this.wallpaperTargetIndex = WallpaperController.WALLPAPER_DRAW_NORMAL;
            this.wallpaperTarget = null;
        }

        void setTopWallpaper(WindowState win, int index) {
            this.topWallpaper = win;
            this.topWallpaperIndex = index;
        }

        void setWallpaperTarget(WindowState win, int index) {
            this.wallpaperTarget = win;
            this.wallpaperTargetIndex = index;
        }

        void reset() {
            this.topWallpaperIndex = WallpaperController.WALLPAPER_DRAW_NORMAL;
            this.topWallpaper = null;
            this.wallpaperTargetIndex = WallpaperController.WALLPAPER_DRAW_NORMAL;
            this.wallpaperTarget = null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wm.WallpaperController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wm.WallpaperController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wm.WallpaperController.<clinit>():void");
    }

    public WallpaperController(WindowManagerService service) {
        this.mWallpaperTokens = new ArrayList();
        this.mWallpaperTarget = null;
        this.mLowerWallpaperTarget = null;
        this.mUpperWallpaperTarget = null;
        this.mLastWallpaperX = -1.0f;
        this.mLastWallpaperY = -1.0f;
        this.mLastWallpaperXStep = -1.0f;
        this.mLastWallpaperYStep = -1.0f;
        this.mLastWallpaperDisplayOffsetX = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        this.mLastWallpaperDisplayOffsetY = UsbAudioDevice.kAudioDeviceMeta_Alsa;
        this.mDeferredHideWallpaper = null;
        this.mWallpaperDrawState = WALLPAPER_DRAW_NORMAL;
        this.mFindResults = new FindWallpaperTargetResult();
        this.mService = service;
    }

    WindowState getWallpaperTarget() {
        return this.mWallpaperTarget;
    }

    WindowState getLowerWallpaperTarget() {
        return this.mLowerWallpaperTarget;
    }

    WindowState getUpperWallpaperTarget() {
        return this.mUpperWallpaperTarget;
    }

    boolean isWallpaperTarget(WindowState win) {
        return win == this.mWallpaperTarget;
    }

    boolean isBelowWallpaperTarget(WindowState win) {
        return this.mWallpaperTarget != null && this.mWallpaperTarget.mLayer >= win.mBaseLayer;
    }

    boolean isWallpaperVisible() {
        return isWallpaperVisible(this.mWallpaperTarget);
    }

    private boolean isWallpaperVisible(WindowState wallpaperTarget) {
        if (wallpaperTarget != null) {
            if (!wallpaperTarget.mObscured) {
                return true;
            }
            if (!(wallpaperTarget.mAppToken == null || wallpaperTarget.mAppToken.mAppAnimator.animation == null)) {
                return true;
            }
        }
        if (this.mUpperWallpaperTarget == null && this.mLowerWallpaperTarget == null) {
            return false;
        }
        return true;
    }

    boolean isWallpaperTargetAnimating() {
        if (this.mWallpaperTarget == null || !this.mWallpaperTarget.mWinAnimator.isAnimationSet() || this.mWallpaperTarget.mWinAnimator.isDummyAnimation()) {
            return false;
        }
        return true;
    }

    void updateWallpaperVisibility() {
        DisplayContent displayContent = this.mWallpaperTarget.getDisplayContent();
        if (displayContent != null) {
            boolean visible = isWallpaperVisible(this.mWallpaperTarget);
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenNdx);
                if (token.hidden == visible) {
                    boolean z;
                    if (visible) {
                        z = false;
                    } else {
                        z = true;
                    }
                    token.hidden = z;
                    displayContent.layoutNeeded = true;
                }
                WindowList windows = token.windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    WindowState wallpaper = (WindowState) windows.get(wallpaperNdx);
                    if (visible) {
                        updateWallpaperOffset(wallpaper, dw, dh, false);
                    }
                    dispatchWallpaperVisibility(wallpaper, visible);
                }
            }
        }
    }

    void hideDeferredWallpapersIfNeeded() {
        if (this.mDeferredHideWallpaper != null) {
            hideWallpapers(this.mDeferredHideWallpaper);
            this.mDeferredHideWallpaper = null;
        }
    }

    void hideWallpapers(WindowState winGoingAway) {
        if (this.mWallpaperTarget != null && (this.mWallpaperTarget != winGoingAway || this.mLowerWallpaperTarget != null)) {
            return;
        }
        if (this.mService.mAppTransition.isRunning()) {
            this.mDeferredHideWallpaper = winGoingAway;
            return;
        }
        boolean wasDeferred = this.mDeferredHideWallpaper == winGoingAway;
        for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(i);
            for (int j = token.windows.size() - 1; j >= 0; j--) {
                WindowState wallpaper = (WindowState) token.windows.get(j);
                WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                if (!winAnimator.mLastHidden || wasDeferred) {
                    winAnimator.hide("hideWallpapers");
                    dispatchWallpaperVisibility(wallpaper, false);
                    DisplayContent displayContent = wallpaper.getDisplayContent();
                    if (displayContent != null) {
                        displayContent.pendingLayoutChanges |= 4;
                    }
                }
            }
            token.hidden = true;
        }
    }

    void dispatchWallpaperVisibility(WindowState wallpaper, boolean visible) {
        if (wallpaper.mWallpaperVisible == visible) {
            return;
        }
        if (this.mDeferredHideWallpaper == null || visible) {
            wallpaper.mWallpaperVisible = visible;
            try {
                wallpaper.mClient.dispatchAppVisibility(visible);
            } catch (RemoteException e) {
            }
        }
    }

    boolean updateWallpaperOffset(WindowState wallpaperWin, int dw, int dh, boolean sync) {
        boolean rawChanged = false;
        float wpx = this.mLastWallpaperX >= 0.0f ? this.mLastWallpaperX : 0.0f;
        float wpxs = this.mLastWallpaperXStep >= 0.0f ? this.mLastWallpaperXStep : -1.0f;
        int availw = (wallpaperWin.mFrame.right - wallpaperWin.mFrame.left) - dw;
        int offset = availw > 0 ? -((int) ((((float) availw) * wpx) + TaskPositioner.RESIZING_HINT_ALPHA)) : WALLPAPER_DRAW_NORMAL;
        if (this.mLastWallpaperDisplayOffsetX != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
            offset += this.mLastWallpaperDisplayOffsetX;
        }
        boolean z = wallpaperWin.mXOffset != offset;
        if (z) {
            wallpaperWin.mXOffset = offset;
        }
        if (!(wallpaperWin.mWallpaperX == wpx && wallpaperWin.mWallpaperXStep == wpxs)) {
            wallpaperWin.mWallpaperX = wpx;
            wallpaperWin.mWallpaperXStep = wpxs;
            rawChanged = true;
        }
        float wpy = this.mLastWallpaperY >= 0.0f ? this.mLastWallpaperY : TaskPositioner.RESIZING_HINT_ALPHA;
        float wpys = this.mLastWallpaperYStep >= 0.0f ? this.mLastWallpaperYStep : -1.0f;
        int availh = (wallpaperWin.mFrame.bottom - wallpaperWin.mFrame.top) - dh;
        offset = availh > 0 ? -((int) ((((float) availh) * wpy) + TaskPositioner.RESIZING_HINT_ALPHA)) : WALLPAPER_DRAW_NORMAL;
        if (mUsingHwNavibar) {
            offset = WALLPAPER_DRAW_NORMAL;
        }
        if (this.mLastWallpaperDisplayOffsetY != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
            offset += this.mLastWallpaperDisplayOffsetY;
        }
        if (wallpaperWin.mYOffset != offset) {
            z = true;
            wallpaperWin.mYOffset = offset;
        }
        if (!(wallpaperWin.mWallpaperY == wpy && wallpaperWin.mWallpaperYStep == wpys)) {
            wallpaperWin.mWallpaperY = wpy;
            wallpaperWin.mWallpaperYStep = wpys;
            rawChanged = true;
        }
        if (rawChanged && (wallpaperWin.mAttrs.privateFlags & 4) != 0) {
            if (sync) {
                try {
                    this.mWaitingOnWallpaper = wallpaperWin;
                } catch (RemoteException e) {
                }
            }
            wallpaperWin.mClient.dispatchWallpaperOffsets(wallpaperWin.mWallpaperX, wallpaperWin.mWallpaperY, wallpaperWin.mWallpaperXStep, wallpaperWin.mWallpaperYStep, sync);
            if (sync && this.mWaitingOnWallpaper != null) {
                long start = SystemClock.uptimeMillis();
                if (this.mLastWallpaperTimeoutTime + WALLPAPER_TIMEOUT_RECOVERY < start) {
                    try {
                        this.mService.mWindowMap.wait(WALLPAPER_TIMEOUT);
                    } catch (InterruptedException e2) {
                    }
                    if (WALLPAPER_TIMEOUT + start < SystemClock.uptimeMillis()) {
                        Slog.i(TAG, "Timeout waiting for wallpaper to offset: " + wallpaperWin);
                        this.mLastWallpaperTimeoutTime = start;
                    }
                }
                this.mWaitingOnWallpaper = null;
            }
        }
        return z;
    }

    void setWindowWallpaperPosition(WindowState window, float x, float y, float xStep, float yStep) {
        if (window.mWallpaperX != x || window.mWallpaperY != y) {
            window.mWallpaperX = x;
            window.mWallpaperY = y;
            window.mWallpaperXStep = xStep;
            window.mWallpaperYStep = yStep;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    void setWindowWallpaperDisplayOffset(WindowState window, int x, int y) {
        if (window.mWallpaperDisplayOffsetX != x || window.mWallpaperDisplayOffsetY != y) {
            window.mWallpaperDisplayOffsetX = x;
            window.mWallpaperDisplayOffsetY = y;
            updateWallpaperOffsetLocked(window, true);
        }
    }

    Bundle sendWindowWallpaperCommand(WindowState window, String action, int x, int y, int z, Bundle extras, boolean sync) {
        if (window == this.mWallpaperTarget || window == this.mLowerWallpaperTarget || window == this.mUpperWallpaperTarget) {
            boolean doWait = sync;
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(curTokenNdx)).windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    try {
                        ((WindowState) windows.get(wallpaperNdx)).mClient.dispatchWallpaperCommand(action, x, y, z, extras, sync);
                        sync = false;
                    } catch (RemoteException e) {
                    }
                }
            }
            if (doWait) {
            }
        }
        return null;
    }

    void updateWallpaperOffsetLocked(WindowState changingTarget, boolean sync) {
        DisplayContent displayContent = changingTarget.getDisplayContent();
        if (displayContent != null) {
            DisplayInfo displayInfo = displayContent.getDisplayInfo();
            int dw = displayInfo.logicalWidth;
            int dh = displayInfo.logicalHeight;
            WindowState target = this.mWallpaperTarget;
            if (target != null) {
                if (target.mWallpaperX >= 0.0f) {
                    this.mLastWallpaperX = target.mWallpaperX;
                } else if (changingTarget.mWallpaperX >= 0.0f) {
                    this.mLastWallpaperX = changingTarget.mWallpaperX;
                }
                if (target.mWallpaperY >= 0.0f) {
                    this.mLastWallpaperY = target.mWallpaperY;
                } else if (changingTarget.mWallpaperY >= 0.0f) {
                    this.mLastWallpaperY = changingTarget.mWallpaperY;
                }
                if (target.mWallpaperDisplayOffsetX != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                    this.mLastWallpaperDisplayOffsetX = target.mWallpaperDisplayOffsetX;
                } else if (changingTarget.mWallpaperDisplayOffsetX != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                    this.mLastWallpaperDisplayOffsetX = changingTarget.mWallpaperDisplayOffsetX;
                }
                if (target.mWallpaperDisplayOffsetY != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                    this.mLastWallpaperDisplayOffsetY = target.mWallpaperDisplayOffsetY;
                } else if (changingTarget.mWallpaperDisplayOffsetY != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                    this.mLastWallpaperDisplayOffsetY = changingTarget.mWallpaperDisplayOffsetY;
                }
                if (target.mWallpaperXStep >= 0.0f) {
                    this.mLastWallpaperXStep = target.mWallpaperXStep;
                } else if (changingTarget.mWallpaperXStep >= 0.0f) {
                    this.mLastWallpaperXStep = changingTarget.mWallpaperXStep;
                }
                if (target.mWallpaperYStep >= 0.0f) {
                    this.mLastWallpaperYStep = target.mWallpaperYStep;
                } else if (changingTarget.mWallpaperYStep >= 0.0f) {
                    this.mLastWallpaperYStep = changingTarget.mWallpaperYStep;
                }
            }
            for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(curTokenNdx)).windows;
                for (int wallpaperNdx = windows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                    WindowState wallpaper = (WindowState) windows.get(wallpaperNdx);
                    if (updateWallpaperOffset(wallpaper, dw, dh, sync)) {
                        WindowStateAnimator winAnimator = wallpaper.mWinAnimator;
                        winAnimator.computeShownFrameLocked();
                        winAnimator.setWallpaperOffset(wallpaper.mShownPosition);
                        sync = false;
                    }
                }
            }
        }
    }

    void clearLastWallpaperTimeoutTime() {
        this.mLastWallpaperTimeoutTime = 0;
    }

    void wallpaperCommandComplete(IBinder window) {
        if (this.mWaitingOnWallpaper != null && this.mWaitingOnWallpaper.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mWindowMap.notifyAll();
        }
    }

    void wallpaperOffsetsComplete(IBinder window) {
        if (this.mWaitingOnWallpaper != null && this.mWaitingOnWallpaper.mClient.asBinder() == window) {
            this.mWaitingOnWallpaper = null;
            this.mService.mWindowMap.notifyAll();
        }
    }

    int getAnimLayerAdjustment() {
        return this.mWallpaperAnimLayerAdjustment;
    }

    void setAnimLayerAdjustment(WindowState win, int adj) {
        if (win == this.mWallpaperTarget && this.mLowerWallpaperTarget == null) {
            this.mWallpaperAnimLayerAdjustment = adj;
            for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowList windows = ((WindowToken) this.mWallpaperTokens.get(i)).windows;
                for (int j = windows.size() - 1; j >= 0; j--) {
                    WindowState wallpaper = (WindowState) windows.get(j);
                    wallpaper.mWinAnimator.mAnimLayer = wallpaper.mLayer + adj;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void findWallpaperTarget(WindowList windows, FindWallpaperTargetResult result) {
        WindowAnimator winAnimator = this.mService.mAnimator;
        result.reset();
        WindowState w = null;
        int windowDetachedI = -1;
        boolean resetTopWallpaper = false;
        boolean inFreeformSpace = false;
        boolean replacing = false;
        for (int i = windows.size() - 1; i >= 0; i--) {
            w = (WindowState) windows.get(i);
            if (w.mAttrs.type != 2013) {
                resetTopWallpaper = true;
                if (w == winAnimator.mWindowDetachedWallpaper || w.mAppToken == null || !w.mAppToken.hidden || w.mAppToken.mAppAnimator.animation != null) {
                    if (!inFreeformSpace) {
                        TaskStack stack = w.getStack();
                        inFreeformSpace = stack != null && stack.mStackId == WALLPAPER_DRAW_TIMEOUT;
                    }
                    replacing = !replacing ? w.mWillReplaceWindow : true;
                    boolean hasWallpaper = (w.mAttrs.flags & DumpState.DUMP_DEXOPT) == 0 ? w.mAppToken != null ? w.mWinAnimator.mKeyguardGoingAwayWithWallpaper : false : true;
                    if (hasWallpaper && w.isOnScreen() && (this.mWallpaperTarget == w || w.isDrawFinishedLw())) {
                        result.setWallpaperTarget(w, i);
                        if (w == this.mWallpaperTarget && w.mWinAnimator.isAnimationSet()) {
                        }
                    } else if (w == winAnimator.mWindowDetachedWallpaper) {
                        windowDetachedI = i;
                    }
                }
            } else if (result.topWallpaper == null || resetTopWallpaper) {
                result.setTopWallpaper(w, i);
                resetTopWallpaper = false;
            }
        }
        if (result.wallpaperTarget == null && windowDetachedI >= 0) {
            result.setWallpaperTarget(w, windowDetachedI);
        }
        if (result.wallpaperTarget != null) {
            return;
        }
        if (inFreeformSpace || (r3 && this.mWallpaperTarget != null)) {
            result.setWallpaperTarget(result.topWallpaper, result.topWallpaperIndex);
        }
    }

    private boolean updateWallpaperWindowsTarget(WindowList windows, FindWallpaperTargetResult result) {
        boolean targetChanged = false;
        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        if (this.mWallpaperTarget != wallpaperTarget && (this.mLowerWallpaperTarget == null || this.mLowerWallpaperTarget != wallpaperTarget)) {
            this.mLowerWallpaperTarget = null;
            this.mUpperWallpaperTarget = null;
            WindowState oldW = this.mWallpaperTarget;
            this.mWallpaperTarget = wallpaperTarget;
            targetChanged = true;
            if (!(wallpaperTarget == null || oldW == null)) {
                boolean oldAnim = oldW.isAnimatingLw();
                if (wallpaperTarget.isAnimatingLw() && oldAnim) {
                    int oldI = windows.indexOf(oldW);
                    if (oldI >= 0) {
                        if (wallpaperTarget.mAppToken != null && wallpaperTarget.mAppToken.hiddenRequested) {
                            this.mWallpaperTarget = oldW;
                            wallpaperTarget = oldW;
                            wallpaperTargetIndex = oldI;
                        } else if (wallpaperTargetIndex > oldI) {
                            this.mUpperWallpaperTarget = wallpaperTarget;
                            this.mLowerWallpaperTarget = oldW;
                            wallpaperTarget = oldW;
                            wallpaperTargetIndex = oldI;
                        } else {
                            this.mUpperWallpaperTarget = oldW;
                            this.mLowerWallpaperTarget = wallpaperTarget;
                        }
                    }
                }
            }
        } else if (!(this.mLowerWallpaperTarget == null || (this.mLowerWallpaperTarget.isAnimatingLw() && this.mUpperWallpaperTarget.isAnimatingLw()))) {
            this.mLowerWallpaperTarget = null;
            this.mUpperWallpaperTarget = null;
            this.mWallpaperTarget = wallpaperTarget;
            targetChanged = true;
        }
        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return targetChanged;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean updateWallpaperWindowsTargetByLayer(WindowList windows, FindWallpaperTargetResult result) {
        boolean visible;
        int i = WALLPAPER_DRAW_NORMAL;
        WindowState wallpaperTarget = result.wallpaperTarget;
        int wallpaperTargetIndex = result.wallpaperTargetIndex;
        if (wallpaperTarget != null) {
            visible = true;
        } else {
            visible = false;
        }
        if (visible) {
            visible = isWallpaperVisible(wallpaperTarget);
            if (this.mLowerWallpaperTarget == null && wallpaperTarget.mAppToken != null) {
                i = wallpaperTarget.mAppToken.mAppAnimator.animLayerAdjustment;
            }
            this.mWallpaperAnimLayerAdjustment = i;
            int maxLayer = (this.mService.mPolicy.getMaxWallpaperLayer() * AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) + ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
            while (wallpaperTargetIndex > 0) {
                WindowState wb = (WindowState) windows.get(wallpaperTargetIndex - 1);
                if (wb.mBaseLayer >= maxLayer || wb.mAttachedWindow == wallpaperTarget || ((wallpaperTarget.mAttachedWindow != null && wb.mAttachedWindow == wallpaperTarget.mAttachedWindow) || (wb.mAttrs.type == 3 && wallpaperTarget.mToken != null && wb.mToken == wallpaperTarget.mToken))) {
                    wallpaperTarget = wb;
                    wallpaperTargetIndex--;
                }
            }
        }
        result.setWallpaperTarget(wallpaperTarget, wallpaperTargetIndex);
        return visible;
    }

    boolean updateWallpaperWindowsPlacement(WindowList windows, WindowState wallpaperTarget, int wallpaperTargetIndex, boolean visible) {
        DisplayInfo displayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        boolean changed = false;
        for (int curTokenNdx = this.mWallpaperTokens.size() - 1; curTokenNdx >= 0; curTokenNdx--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenNdx);
            if (token.hidden == visible) {
                token.hidden = !visible;
                this.mService.getDefaultDisplayContentLocked().layoutNeeded = true;
            }
            WindowList tokenWindows = token.windows;
            for (int wallpaperNdx = tokenWindows.size() - 1; wallpaperNdx >= 0; wallpaperNdx--) {
                WindowState wallpaper = (WindowState) tokenWindows.get(wallpaperNdx);
                if (visible) {
                    updateWallpaperOffset(wallpaper, dw, dh, false);
                }
                dispatchWallpaperVisibility(wallpaper, visible);
                wallpaper.mWinAnimator.mAnimLayer = wallpaper.mLayer + this.mWallpaperAnimLayerAdjustment;
                if (wallpaper == wallpaperTarget) {
                    wallpaperTargetIndex--;
                    if (wallpaperTargetIndex > 0) {
                        wallpaperTarget = (WindowState) windows.get(wallpaperTargetIndex - 1);
                    } else {
                        wallpaperTarget = null;
                    }
                } else {
                    int oldIndex = windows.indexOf(wallpaper);
                    if (oldIndex >= 0) {
                        windows.remove(oldIndex);
                        this.mService.mWindowsChanged = true;
                        if (oldIndex < wallpaperTargetIndex) {
                            wallpaperTargetIndex--;
                        }
                    }
                    int insertionIndex = WALLPAPER_DRAW_NORMAL;
                    if (visible && wallpaperTarget != null) {
                        int type = wallpaperTarget.mAttrs.type;
                        if ((wallpaperTarget.mAttrs.privateFlags & DumpState.DUMP_PROVIDERS) != 0 || type == 2029) {
                            insertionIndex = windows.indexOf(wallpaperTarget);
                        }
                    }
                    windows.add(insertionIndex, wallpaper);
                    this.mService.mWindowsChanged = true;
                    changed = true;
                }
            }
        }
        return changed;
    }

    boolean adjustWallpaperWindows() {
        this.mService.mWindowPlacerLocked.mWallpaperMayChange = false;
        WindowList windows = this.mService.getDefaultWindowListLocked();
        findWallpaperTarget(windows, this.mFindResults);
        boolean targetChanged = updateWallpaperWindowsTarget(windows, this.mFindResults);
        boolean visible = updateWallpaperWindowsTargetByLayer(windows, this.mFindResults);
        WindowState wallpaperTarget = this.mFindResults.wallpaperTarget;
        int wallpaperTargetIndex = this.mFindResults.wallpaperTargetIndex;
        if (wallpaperTarget != null || this.mFindResults.topWallpaper == null) {
            wallpaperTarget = wallpaperTargetIndex > 0 ? (WindowState) windows.get(wallpaperTargetIndex - 1) : null;
        } else {
            wallpaperTarget = this.mFindResults.topWallpaper;
            wallpaperTargetIndex = this.mFindResults.topWallpaperIndex + WALLPAPER_DRAW_PENDING;
        }
        if (visible) {
            if (this.mWallpaperTarget.mWallpaperX >= 0.0f) {
                this.mLastWallpaperX = this.mWallpaperTarget.mWallpaperX;
                this.mLastWallpaperXStep = this.mWallpaperTarget.mWallpaperXStep;
            }
            if (this.mWallpaperTarget.mWallpaperY >= 0.0f) {
                this.mLastWallpaperY = this.mWallpaperTarget.mWallpaperY;
                this.mLastWallpaperYStep = this.mWallpaperTarget.mWallpaperYStep;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetX != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                this.mLastWallpaperDisplayOffsetX = this.mWallpaperTarget.mWallpaperDisplayOffsetX;
            }
            if (this.mWallpaperTarget.mWallpaperDisplayOffsetY != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
                this.mLastWallpaperDisplayOffsetY = this.mWallpaperTarget.mWallpaperDisplayOffsetY;
            }
        }
        return targetChanged ? updateWallpaperWindowsPlacement(windows, wallpaperTarget, wallpaperTargetIndex, visible) : updateWallpaperWindowsPlacement(windows, wallpaperTarget, wallpaperTargetIndex, visible);
    }

    boolean processWallpaperDrawPendingTimeout() {
        if (this.mWallpaperDrawState != WALLPAPER_DRAW_PENDING) {
            return false;
        }
        this.mWallpaperDrawState = WALLPAPER_DRAW_TIMEOUT;
        return true;
    }

    boolean wallpaperTransitionReady() {
        boolean transitionReady = true;
        boolean wallpaperReady = true;
        for (int curTokenIndex = this.mWallpaperTokens.size() - 1; curTokenIndex >= 0 && wallpaperReady; curTokenIndex--) {
            WindowToken token = (WindowToken) this.mWallpaperTokens.get(curTokenIndex);
            int curWallpaperIndex = token.windows.size() - 1;
            while (curWallpaperIndex >= 0) {
                WindowState wallpaper = (WindowState) token.windows.get(curWallpaperIndex);
                if (!wallpaper.mWallpaperVisible || wallpaper.isDrawnLw()) {
                    curWallpaperIndex--;
                } else {
                    wallpaperReady = false;
                    if (this.mWallpaperDrawState != WALLPAPER_DRAW_TIMEOUT) {
                        transitionReady = false;
                    }
                    if (this.mWallpaperDrawState == 0) {
                        this.mWallpaperDrawState = WALLPAPER_DRAW_PENDING;
                        this.mService.mH.removeMessages(39);
                        this.mService.mH.sendEmptyMessageDelayed(39, WALLPAPER_DRAW_PENDING_TIMEOUT_DURATION);
                    }
                }
            }
        }
        if (wallpaperReady) {
            this.mWallpaperDrawState = WALLPAPER_DRAW_NORMAL;
            this.mService.mH.removeMessages(39);
        }
        return transitionReady;
    }

    void addWallpaperToken(WindowToken token) {
        this.mWallpaperTokens.add(token);
    }

    void removeWallpaperToken(WindowToken token) {
        this.mWallpaperTokens.remove(token);
    }

    void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mWallpaperTarget=");
        pw.println(this.mWallpaperTarget);
        if (!(this.mLowerWallpaperTarget == null && this.mUpperWallpaperTarget == null)) {
            pw.print(prefix);
            pw.print("mLowerWallpaperTarget=");
            pw.println(this.mLowerWallpaperTarget);
            pw.print(prefix);
            pw.print("mUpperWallpaperTarget=");
            pw.println(this.mUpperWallpaperTarget);
        }
        pw.print(prefix);
        pw.print("mLastWallpaperX=");
        pw.print(this.mLastWallpaperX);
        pw.print(" mLastWallpaperY=");
        pw.println(this.mLastWallpaperY);
        if (this.mLastWallpaperDisplayOffsetX != UsbAudioDevice.kAudioDeviceMeta_Alsa || this.mLastWallpaperDisplayOffsetY != UsbAudioDevice.kAudioDeviceMeta_Alsa) {
            pw.print(prefix);
            pw.print("mLastWallpaperDisplayOffsetX=");
            pw.print(this.mLastWallpaperDisplayOffsetX);
            pw.print(" mLastWallpaperDisplayOffsetY=");
            pw.println(this.mLastWallpaperDisplayOffsetY);
        }
    }

    void dumpTokens(PrintWriter pw, String prefix, boolean dumpAll) {
        if (!this.mWallpaperTokens.isEmpty()) {
            pw.println();
            pw.print(prefix);
            pw.println("Wallpaper tokens:");
            for (int i = this.mWallpaperTokens.size() - 1; i >= 0; i--) {
                WindowToken token = (WindowToken) this.mWallpaperTokens.get(i);
                pw.print(prefix);
                pw.print("Wallpaper #");
                pw.print(i);
                pw.print(' ');
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ");
                } else {
                    pw.println();
                }
            }
        }
    }
}
