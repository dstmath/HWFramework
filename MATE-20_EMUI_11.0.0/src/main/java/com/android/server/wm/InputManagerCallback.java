package com.android.server.wm;

import android.os.Debug;
import android.os.IBinder;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.KeyEvent;
import com.android.server.input.InputManagerService;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class InputManagerCallback implements InputManagerService.WindowManagerCallbacks {
    private boolean mInputDevicesReady;
    private final Object mInputDevicesReadyMonitor = new Object();
    private boolean mInputDispatchEnabled;
    private boolean mInputDispatchFrozen;
    private String mInputFreezeReason = null;
    private final WindowManagerService mService;

    public InputManagerCallback(WindowManagerService service) {
        this.mService = service;
    }

    public void notifyInputChannelBroken(IBinder token) {
        if (token != null) {
            synchronized (this.mService.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = this.mService.windowForClientLocked((Session) null, token, false);
                    if (windowState != null) {
                        Slog.i("WindowManager", "WINDOW DIED " + windowState);
                        Slog.i("WindowManager", "cancelAnimation");
                        windowState.cancelAnimation();
                        if (windowState.mAppToken != null) {
                            Slog.i("WindowManager", "mAppToken.cancelAnimation");
                            windowState.mAppToken.cancelAnimation();
                        }
                        windowState.removeIfPossible();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public long notifyANR(IBinder token, String reason) {
        AppWindowToken appWindowToken = null;
        WindowState windowState = null;
        boolean aboveSystem = false;
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                boolean z = false;
                if (token != null) {
                    windowState = this.mService.windowForClientLocked((Session) null, token, false);
                    if (windowState != null) {
                        appWindowToken = windowState.mAppToken;
                    }
                } else {
                    DisplayContent focusedDisplay = this.mService.mRoot.getTopFocusedDisplayContent();
                    if (!(focusedDisplay == null || focusedDisplay.mFocusedApp == null || focusedDisplay.mFocusedApp.findMainWindow(false) != null)) {
                        Slog.i("WindowManager", "Input event dispatching timed out sending to focus app when no window");
                        appWindowToken = focusedDisplay.mFocusedApp;
                    }
                }
                if (windowState != null) {
                    Slog.i("WindowManager", "Input event dispatching timed out sending to " + ((Object) windowState.mAttrs.getTitle()) + ".  Reason: " + reason);
                    if (windowState.mBaseLayer > this.mService.mPolicy.getWindowLayerFromTypeLw(2038, windowState.mOwnerCanAddInternalSystemWindow)) {
                        z = true;
                    }
                    aboveSystem = z;
                } else if (appWindowToken != null) {
                    Slog.i("WindowManager", "Input event dispatching timed out sending to application " + appWindowToken.stringName + ".  Reason: " + reason);
                } else {
                    Slog.i("WindowManager", "Input event dispatching timed out .  Reason: " + reason);
                }
                this.mService.saveANRStateLocked(appWindowToken, windowState, reason);
            } catch (Throwable th) {
                WindowManagerService.resetPriorityAfterLockedSection();
                throw th;
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        this.mService.mAtmInternal.saveANRState(reason);
        if (appWindowToken != null && appWindowToken.appToken != null) {
            if (!appWindowToken.keyDispatchingTimedOut(reason, windowState != null ? windowState.mSession.mPid : -1)) {
                return appWindowToken.mInputDispatchingTimeoutNanos;
            }
        } else if (windowState != null) {
            long timeout = this.mService.mAmInternal.inputDispatchingTimedOut(windowState.mSession.mPid, aboveSystem, reason);
            if (timeout >= 0) {
                return 1000000 * timeout;
            }
        }
        return 0;
    }

    public void notifyConfigurationChanged() {
        this.mService.sendNewConfiguration(0);
        synchronized (this.mInputDevicesReadyMonitor) {
            if (!this.mInputDevicesReady) {
                this.mInputDevicesReady = true;
                this.mInputDevicesReadyMonitor.notifyAll();
            }
        }
    }

    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        this.mService.mPolicy.notifyLidSwitchChanged(whenNanos, lidOpen);
    }

    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        this.mService.mPolicy.notifyCameraLensCoverSwitchChanged(whenNanos, lensCovered);
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.interceptKeyBeforeQueueing(event, policyFlags);
    }

    public int interceptMotionBeforeQueueingNonInteractive(int displayId, long whenNanos, int policyFlags) {
        return this.mService.mPolicy.interceptMotionBeforeQueueingNonInteractive(displayId, whenNanos, policyFlags);
    }

    public long interceptKeyBeforeDispatching(IBinder focus, KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.interceptKeyBeforeDispatching(this.mService.windowForClientLocked((Session) null, focus, false), event, policyFlags);
    }

    public KeyEvent dispatchUnhandledKey(IBinder focus, KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.dispatchUnhandledKey(this.mService.windowForClientLocked((Session) null, focus, false), event, policyFlags);
    }

    public int getPointerLayer() {
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 1000;
    }

    public int getPointerDisplayId() {
        if (!HwPCUtils.isPcCastModeInServer() || HwPCUtils.isPcCastModeInServer()) {
            return 0;
        }
        synchronized (this.mService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (!this.mService.mForceDesktopModeOnExternalDisplays) {
                    return 0;
                }
                int firstExternalDisplayId = 0;
                for (int i = this.mService.mRoot.mChildren.size() - 1; i >= 0; i--) {
                    DisplayContent displayContent = (DisplayContent) this.mService.mRoot.mChildren.get(i);
                    if (displayContent.getWindowingMode() == 5) {
                        int displayId = displayContent.getDisplayId();
                        WindowManagerService.resetPriorityAfterLockedSection();
                        return displayId;
                    }
                    if (firstExternalDisplayId == 0 && displayContent.getDisplayId() != 0) {
                        firstExternalDisplayId = displayContent.getDisplayId();
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                return firstExternalDisplayId;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onPointerDownOutsideFocus(IBinder touchedToken) {
        this.mService.mH.obtainMessage(62, touchedToken).sendToTarget();
    }

    public boolean waitForInputDevicesReady(long timeoutMillis) {
        boolean z;
        synchronized (this.mInputDevicesReadyMonitor) {
            if (!this.mInputDevicesReady) {
                try {
                    this.mInputDevicesReadyMonitor.wait(timeoutMillis);
                } catch (InterruptedException e) {
                }
            }
            z = this.mInputDevicesReady;
        }
        return z;
    }

    public void freezeInputDispatchingLw() {
        if (!this.mInputDispatchFrozen) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v("WindowManager", "Freezing input dispatching");
            }
            this.mInputDispatchFrozen = true;
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                this.mInputFreezeReason = Debug.getCallers(6);
            }
            updateInputDispatchModeLw();
        }
    }

    public void thawInputDispatchingLw() {
        if (this.mInputDispatchFrozen) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v("WindowManager", "Thawing input dispatching");
            }
            this.mInputDispatchFrozen = false;
            this.mInputFreezeReason = null;
            updateInputDispatchModeLw();
        }
    }

    public void setEventDispatchingLw(boolean enabled) {
        if (this.mInputDispatchEnabled != enabled) {
            Slog.v("WindowManager", "Setting event dispatching to " + enabled);
            this.mInputDispatchEnabled = enabled;
            updateInputDispatchModeLw();
        }
    }

    private void updateInputDispatchModeLw() {
        this.mService.mInputManager.setInputDispatchMode(this.mInputDispatchEnabled, this.mInputDispatchFrozen);
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        if (this.mInputFreezeReason != null) {
            pw.println(prefix + "mInputFreezeReason=" + this.mInputFreezeReason);
        }
    }
}
