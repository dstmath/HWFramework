package com.android.server.wm;

import android.app.ActivityManagerNative;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;
import android.view.InputChannel;
import android.view.KeyEvent;
import com.android.server.am.ProcessList;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputManagerService.WindowManagerCallbacks;
import com.android.server.input.InputWindowHandle;
import com.android.server.power.AbsPowerManagerService;
import java.io.PrintWriter;
import java.util.Arrays;

final class InputMonitor implements WindowManagerCallbacks {
    private boolean mInputDevicesReady;
    private final Object mInputDevicesReadyMonitor;
    private boolean mInputDispatchEnabled;
    private boolean mInputDispatchFrozen;
    private WindowState mInputFocus;
    private String mInputFreezeReason;
    private int mInputWindowHandleCount;
    private InputWindowHandle[] mInputWindowHandles;
    private final WindowManagerService mService;
    private boolean mUpdateInputWindowsNeeded;

    public InputMonitor(WindowManagerService service) {
        this.mInputFreezeReason = null;
        this.mUpdateInputWindowsNeeded = true;
        this.mInputDevicesReadyMonitor = new Object();
        this.mService = service;
    }

    public void notifyInputChannelBroken(InputWindowHandle inputWindowHandle) {
        if (inputWindowHandle != null) {
            synchronized (this.mService.mWindowMap) {
                WindowState windowState = inputWindowHandle.windowState;
                if (windowState != null) {
                    Slog.i("WindowManager", "WINDOW DIED " + windowState);
                    this.mService.removeWindowLocked(windowState);
                }
            }
        }
    }

    public long notifyANR(InputApplicationHandle inputApplicationHandle, InputWindowHandle inputWindowHandle, String reason) {
        AppWindowToken appWindowToken = null;
        WindowState windowState = null;
        boolean aboveSystem = false;
        synchronized (this.mService.mWindowMap) {
            if (inputWindowHandle != null) {
                windowState = (WindowState) inputWindowHandle.windowState;
                if (windowState != null) {
                    appWindowToken = windowState.mAppToken;
                }
            }
            if (appWindowToken == null && inputApplicationHandle != null) {
                appWindowToken = (AppWindowToken) inputApplicationHandle.appWindowToken;
            }
            if (windowState != null) {
                Slog.i("WindowManager", "Input event dispatching timed out sending to " + windowState.mAttrs.getTitle() + ".  Reason: " + reason);
                aboveSystem = windowState.mBaseLayer > this.mService.mPolicy.windowTypeToLayerLw(2003);
            } else if (appWindowToken != null) {
                Slog.i("WindowManager", "Input event dispatching timed out sending to application " + appWindowToken.stringName + ".  Reason: " + reason);
            } else {
                Slog.i("WindowManager", "Input event dispatching timed out .  Reason: " + reason);
            }
            this.mService.saveANRStateLocked(appWindowToken, windowState, reason);
        }
        if (appWindowToken != null && appWindowToken.appToken != null) {
            try {
                if (!appWindowToken.appToken.keyDispatchingTimedOut(reason)) {
                    return appWindowToken.inputDispatchingTimeoutNanos;
                }
            } catch (RemoteException e) {
            }
        } else if (windowState != null) {
            try {
                long timeout = ActivityManagerNative.getDefault().inputDispatchingTimedOut(windowState.mSession.mPid, aboveSystem, reason);
                if (timeout >= 0) {
                    return 1000000 * timeout;
                }
            } catch (RemoteException e2) {
            }
        }
        return 0;
    }

    private void addInputWindowHandleLw(InputWindowHandle windowHandle) {
        if (this.mInputWindowHandles == null) {
            this.mInputWindowHandles = new InputWindowHandle[16];
        }
        if (this.mInputWindowHandleCount >= this.mInputWindowHandles.length) {
            this.mInputWindowHandles = (InputWindowHandle[]) Arrays.copyOf(this.mInputWindowHandles, this.mInputWindowHandleCount * 2);
        }
        InputWindowHandle[] inputWindowHandleArr = this.mInputWindowHandles;
        int i = this.mInputWindowHandleCount;
        this.mInputWindowHandleCount = i + 1;
        inputWindowHandleArr[i] = windowHandle;
    }

    private void addInputWindowHandleLw(InputWindowHandle inputWindowHandle, WindowState child, int flags, int type, boolean isVisible, boolean hasFocus, boolean hasWallpaper) {
        inputWindowHandle.name = child.toString();
        inputWindowHandle.layoutParamsFlags = child.getTouchableRegion(inputWindowHandle.touchableRegion, flags);
        inputWindowHandle.layoutParamsType = type;
        inputWindowHandle.dispatchingTimeoutNanos = child.getInputDispatchingTimeoutNanos();
        inputWindowHandle.visible = isVisible;
        inputWindowHandle.canReceiveKeys = child.canReceiveKeys();
        inputWindowHandle.hasFocus = hasFocus;
        inputWindowHandle.hasWallpaper = hasWallpaper;
        inputWindowHandle.paused = child.mAppToken != null ? child.mAppToken.paused : false;
        inputWindowHandle.layer = child.mLayer;
        inputWindowHandle.ownerPid = child.mSession.mPid;
        inputWindowHandle.ownerUid = child.mSession.mUid;
        inputWindowHandle.inputFeatures = child.mAttrs.inputFeatures;
        Rect frame = child.mFrame;
        inputWindowHandle.frameLeft = frame.left;
        inputWindowHandle.frameTop = frame.top;
        inputWindowHandle.frameRight = frame.right;
        inputWindowHandle.frameBottom = frame.bottom;
        if (child.isDockedInEffect()) {
            inputWindowHandle.frameLeft += child.mXOffset;
            inputWindowHandle.frameTop += child.mYOffset;
            inputWindowHandle.frameRight += child.mXOffset;
            inputWindowHandle.frameBottom += child.mYOffset;
        }
        if (child.mGlobalScale != 1.0f) {
            inputWindowHandle.scaleFactor = 1.0f / child.mGlobalScale;
        } else {
            inputWindowHandle.scaleFactor = 1.0f;
        }
        addInputWindowHandleLw(inputWindowHandle);
    }

    private void clearInputWindowHandlesLw() {
        while (this.mInputWindowHandleCount != 0) {
            InputWindowHandle[] inputWindowHandleArr = this.mInputWindowHandles;
            int i = this.mInputWindowHandleCount - 1;
            this.mInputWindowHandleCount = i;
            inputWindowHandleArr[i] = null;
        }
    }

    public void setUpdateInputWindowsNeededLw() {
        this.mUpdateInputWindowsNeeded = true;
    }

    public void updateInputWindowsLw(boolean force) {
        if (force || this.mUpdateInputWindowsNeeded) {
            InputWindowHandle dragWindowHandle;
            this.mUpdateInputWindowsNeeded = false;
            boolean disableWallpaperTouchEvents = false;
            boolean inDrag = this.mService.mDragState != null;
            if (inDrag) {
                dragWindowHandle = this.mService.mDragState.mDragWindowHandle;
                if (dragWindowHandle != null) {
                    addInputWindowHandleLw(dragWindowHandle);
                } else {
                    Slog.w("WindowManager", "Drag is in progress but there is no drag window handle.");
                }
            }
            if (this.mService.mTaskPositioner != null) {
                dragWindowHandle = this.mService.mTaskPositioner.mDragWindowHandle;
                if (dragWindowHandle != null) {
                    addInputWindowHandleLw(dragWindowHandle);
                } else {
                    Slog.e("WindowManager", "Repositioning is in progress but there is no drag window handle.");
                }
            }
            boolean addInputConsumerHandle = this.mService.mInputConsumer != null;
            boolean addWallpaperInputConsumerHandle = this.mService.mWallpaperInputConsumer != null;
            int numDisplays = this.mService.mDisplayContents.size();
            WallpaperController wallpaperController = this.mService.mWallpaperControllerLocked;
            for (int displayNdx = 0; displayNdx < numDisplays; displayNdx++) {
                WindowList windows = ((DisplayContent) this.mService.mDisplayContents.valueAt(displayNdx)).getWindowList();
                for (int winNdx = windows.size() - 1; winNdx >= 0; winNdx--) {
                    WindowState child = (WindowState) windows.get(winNdx);
                    InputChannel inputChannel = child.mInputChannel;
                    InputWindowHandle inputWindowHandle = child.mInputWindowHandle;
                    if (!(inputChannel == null || inputWindowHandle == null || child.mRemoved || child.isAdjustedForMinimizedDock())) {
                        if (addInputConsumerHandle) {
                            int i = inputWindowHandle.layer;
                            int i2 = this.mService.mInputConsumer.mWindowHandle.layer;
                            if (i <= r0) {
                                addInputWindowHandleLw(this.mService.mInputConsumer.mWindowHandle);
                                addInputConsumerHandle = false;
                            }
                        }
                        if (addWallpaperInputConsumerHandle && child.mAttrs.type == 2013) {
                            addInputWindowHandleLw(this.mService.mWallpaperInputConsumer.mWindowHandle);
                            addWallpaperInputConsumerHandle = false;
                        }
                        int flags = child.mAttrs.flags;
                        int privateFlags = child.mAttrs.privateFlags;
                        int type = child.mAttrs.type;
                        boolean hasFocus = child == this.mInputFocus;
                        boolean isVisible = child.isVisibleLw();
                        if ((privateFlags & DumpState.DUMP_VERIFIERS) != 0) {
                            disableWallpaperTouchEvents = true;
                        }
                        boolean hasWallpaper = (wallpaperController.isWallpaperTarget(child) && (privateFlags & DumpState.DUMP_PROVIDERS) == 0) ? !disableWallpaperTouchEvents : false;
                        boolean onDefaultDisplay = child.getDisplayId() == 0;
                        if (inDrag && isVisible && onDefaultDisplay) {
                            this.mService.mDragState.sendDragStartedIfNeededLw(child);
                        }
                        addInputWindowHandleLw(inputWindowHandle, child, flags, type, isVisible, hasFocus, hasWallpaper);
                    }
                }
            }
            if (addWallpaperInputConsumerHandle) {
                addInputWindowHandleLw(this.mService.mWallpaperInputConsumer.mWindowHandle);
            }
            this.mService.mInputManager.setInputWindows(this.mInputWindowHandles);
            clearInputWindowHandlesLw();
        }
    }

    public void notifyConfigurationChanged() {
        this.mService.sendNewConfiguration();
        synchronized (this.mInputDevicesReadyMonitor) {
            if (!this.mInputDevicesReady) {
                this.mInputDevicesReady = true;
                this.mInputDevicesReadyMonitor.notifyAll();
            }
        }
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

    public void notifyLidSwitchChanged(long whenNanos, boolean lidOpen) {
        this.mService.mPolicy.notifyLidSwitchChanged(whenNanos, lidOpen);
    }

    public void notifyCameraLensCoverSwitchChanged(long whenNanos, boolean lensCovered) {
        this.mService.mPolicy.notifyCameraLensCoverSwitchChanged(whenNanos, lensCovered);
    }

    public int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.interceptKeyBeforeQueueing(event, policyFlags);
    }

    public int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        return this.mService.mPolicy.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
    }

    public long interceptKeyBeforeDispatching(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.interceptKeyBeforeDispatching(focus != null ? (WindowState) focus.windowState : null, event, policyFlags);
    }

    public KeyEvent dispatchUnhandledKey(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        return this.mService.mPolicy.dispatchUnhandledKey(focus != null ? (WindowState) focus.windowState : null, event, policyFlags);
    }

    public int getPointerLayer() {
        return (this.mService.mPolicy.windowTypeToLayerLw(2018) * AbsPowerManagerService.MIN_COVER_SCREEN_OFF_TIMEOUT) + ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE;
    }

    public void setInputFocusLw(WindowState newWindow, boolean updateInputWindows) {
        if (newWindow != this.mInputFocus) {
            if (newWindow != null && newWindow.canReceiveKeys()) {
                newWindow.mToken.paused = false;
            }
            this.mInputFocus = newWindow;
            setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                updateInputWindowsLw(false);
            }
        }
    }

    public void setFocusedAppLw(AppWindowToken newApp) {
        if (newApp == null) {
            this.mService.mInputManager.setFocusedApplication(null);
            return;
        }
        InputApplicationHandle handle = newApp.mInputApplicationHandle;
        handle.name = newApp.toString();
        handle.dispatchingTimeoutNanos = newApp.inputDispatchingTimeoutNanos;
        this.mService.mInputManager.setFocusedApplication(handle);
    }

    public void pauseDispatchingLw(WindowToken window) {
        if (!window.paused) {
            window.paused = true;
            updateInputWindowsLw(true);
        }
    }

    public void resumeDispatchingLw(WindowToken window) {
        if (window.paused) {
            window.paused = false;
            updateInputWindowsLw(true);
        }
    }

    public void freezeInputDispatchingLw() {
        if (!this.mInputDispatchFrozen) {
            this.mInputDispatchFrozen = true;
            this.mInputFreezeReason = Debug.getCallers(6);
            updateInputDispatchModeLw();
        }
    }

    public void thawInputDispatchingLw() {
        if (this.mInputDispatchFrozen) {
            this.mInputDispatchFrozen = false;
            this.mInputFreezeReason = null;
            updateInputDispatchModeLw();
        }
    }

    public void setEventDispatchingLw(boolean enabled) {
        if (this.mInputDispatchEnabled != enabled) {
            this.mInputDispatchEnabled = enabled;
            updateInputDispatchModeLw();
        }
    }

    private void updateInputDispatchModeLw() {
        this.mService.mInputManager.setInputDispatchMode(this.mInputDispatchEnabled, this.mInputDispatchFrozen);
    }

    void dump(PrintWriter pw, String prefix) {
        if (this.mInputFreezeReason != null) {
            pw.println(prefix + "mInputFreezeReason=" + this.mInputFreezeReason);
        }
    }

    public void notifyANRWarning(int pid) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManagerNative.getDefault().asBinder().transact(501, data, reply, 0);
            reply.readException();
            data.recycle();
            reply.recycle();
        } catch (Exception ex) {
            Slog.e("WindowManager", "notifyANRWarning exception:" + ex);
        }
    }
}
