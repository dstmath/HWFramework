package com.android.server.wm;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.os.Debug;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.KeyEvent;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputManagerService;
import com.android.server.input.InputWindowHandle;
import com.android.server.policy.WindowManagerPolicy;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

final class InputMonitor implements InputManagerService.WindowManagerCallbacks {
    /* access modifiers changed from: private */
    public boolean mAddInputConsumerHandle;
    /* access modifiers changed from: private */
    public boolean mAddPipInputConsumerHandle;
    /* access modifiers changed from: private */
    public boolean mAddRecentsAnimationInputConsumerHandle;
    /* access modifiers changed from: private */
    public boolean mAddWallpaperInputConsumerHandle;
    /* access modifiers changed from: private */
    public boolean mDisableWallpaperTouchEvents;
    /* access modifiers changed from: private */
    public InputWindowHandle mFocusedInputWindowHandle;
    /* access modifiers changed from: private */
    public boolean mHasLighterViewInPCCastMode;
    /* access modifiers changed from: private */
    public boolean mHasLighterViewInPCCastModeTemp;
    private final ArrayMap<String, InputConsumerImpl> mInputConsumers = new ArrayMap<>();
    private boolean mInputDevicesReady;
    private final Object mInputDevicesReadyMonitor = new Object();
    private boolean mInputDispatchEnabled;
    private boolean mInputDispatchFrozen;
    /* access modifiers changed from: private */
    public WindowState mInputFocus;
    private String mInputFreezeReason = null;
    private int mInputWindowHandleCount;
    private InputWindowHandle mInputWindowHandleInExtDisplay;
    /* access modifiers changed from: private */
    public InputWindowHandle[] mInputWindowHandles;
    /* access modifiers changed from: private */
    public final WindowManagerService mService;
    /* access modifiers changed from: private */
    public final Rect mTmpRect = new Rect();
    private final UpdateInputForAllWindowsConsumer mUpdateInputForAllWindowsConsumer = new UpdateInputForAllWindowsConsumer();
    private boolean mUpdateInputWindowsNeeded = true;

    private static final class EventReceiverInputConsumer extends InputConsumerImpl implements WindowManagerPolicy.InputConsumer {
        private final InputEventReceiver mInputEventReceiver;
        private InputMonitor mInputMonitor;

        EventReceiverInputConsumer(WindowManagerService service, InputMonitor monitor, Looper looper, String name, InputEventReceiver.Factory inputEventReceiverFactory, int clientPid, UserHandle clientUser) {
            super(service, null, name, null, clientPid, clientUser);
            this.mInputMonitor = monitor;
            this.mInputEventReceiver = inputEventReceiverFactory.createInputEventReceiver(this.mClientChannel, looper);
        }

        public void dismiss() {
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (this.mInputMonitor.destroyInputConsumer(this.mWindowHandle.name)) {
                        this.mInputEventReceiver.dispose();
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
    }

    private final class UpdateInputForAllWindowsConsumer implements Consumer<WindowState> {
        boolean inDrag;
        InputConsumerImpl navInputConsumer;
        InputConsumerImpl pipInputConsumer;
        InputConsumerImpl recentsAnimationInputConsumer;
        WallpaperController wallpaperController;
        InputConsumerImpl wallpaperInputConsumer;

        private UpdateInputForAllWindowsConsumer() {
        }

        /* access modifiers changed from: private */
        public void updateInputWindows(boolean inDrag2) {
            this.navInputConsumer = InputMonitor.this.getInputConsumer("nav_input_consumer", 0);
            this.pipInputConsumer = InputMonitor.this.getInputConsumer("pip_input_consumer", 0);
            this.wallpaperInputConsumer = InputMonitor.this.getInputConsumer("wallpaper_input_consumer", 0);
            this.recentsAnimationInputConsumer = InputMonitor.this.getInputConsumer("recents_animation_input_consumer", 0);
            boolean unused = InputMonitor.this.mAddInputConsumerHandle = this.navInputConsumer != null;
            boolean unused2 = InputMonitor.this.mAddPipInputConsumerHandle = this.pipInputConsumer != null;
            boolean unused3 = InputMonitor.this.mAddWallpaperInputConsumerHandle = this.wallpaperInputConsumer != null;
            boolean unused4 = InputMonitor.this.mAddRecentsAnimationInputConsumerHandle = this.recentsAnimationInputConsumer != null;
            InputMonitor.this.mTmpRect.setEmpty();
            boolean unused5 = InputMonitor.this.mDisableWallpaperTouchEvents = false;
            this.inDrag = inDrag2;
            this.wallpaperController = InputMonitor.this.mService.mRoot.mWallpaperController;
            InputMonitor.this.mService.mRoot.forAllWindows((Consumer<WindowState>) this, true);
            if (InputMonitor.this.mAddWallpaperInputConsumerHandle) {
                InputMonitor.this.addInputWindowHandle(this.wallpaperInputConsumer.mWindowHandle);
            }
            InputMonitor.this.mService.mInputManager.setInputWindows(InputMonitor.this.mInputWindowHandles, InputMonitor.this.mFocusedInputWindowHandle);
            boolean unused6 = InputMonitor.this.mHasLighterViewInPCCastMode = InputMonitor.this.mHasLighterViewInPCCastModeTemp;
            boolean unused7 = InputMonitor.this.mHasLighterViewInPCCastModeTemp = false;
            InputMonitor.this.setFousedWinExtDisplayInPCCastMode();
            InputMonitor.this.clearInputWindowHandlesLw();
        }

        public void accept(WindowState w) {
            WindowState windowState = w;
            InputChannel inputChannel = windowState.mInputChannel;
            InputWindowHandle inputWindowHandle = windowState.mInputWindowHandle;
            if (inputChannel != null && inputWindowHandle != null && !windowState.mRemoved && !w.canReceiveTouchInput()) {
                int flags = windowState.mAttrs.flags;
                int privateFlags = windowState.mAttrs.privateFlags;
                int type = windowState.mAttrs.type;
                boolean hasFocus = windowState == InputMonitor.this.mInputFocus;
                boolean isVisible = w.isVisibleLw();
                if (InputMonitor.this.mAddRecentsAnimationInputConsumerHandle) {
                    RecentsAnimationController recentsAnimationController = InputMonitor.this.mService.getRecentsAnimationController();
                    if (recentsAnimationController != null && recentsAnimationController.hasInputConsumerForApp(windowState.mAppToken)) {
                        if (recentsAnimationController.updateInputConsumerForApp(this.recentsAnimationInputConsumer, hasFocus)) {
                            InputMonitor.this.addInputWindowHandle(this.recentsAnimationInputConsumer.mWindowHandle);
                            boolean unused = InputMonitor.this.mAddRecentsAnimationInputConsumerHandle = false;
                        }
                        return;
                    }
                }
                if (w.inPinnedWindowingMode()) {
                    if (InputMonitor.this.mAddPipInputConsumerHandle && inputWindowHandle.layer <= this.pipInputConsumer.mWindowHandle.layer) {
                        windowState.getBounds(InputMonitor.this.mTmpRect);
                        this.pipInputConsumer.mWindowHandle.touchableRegion.set(InputMonitor.this.mTmpRect);
                        InputMonitor.this.addInputWindowHandle(this.pipInputConsumer.mWindowHandle);
                        boolean unused2 = InputMonitor.this.mAddPipInputConsumerHandle = false;
                    }
                    if (!hasFocus) {
                        return;
                    }
                }
                if (InputMonitor.this.mAddInputConsumerHandle && inputWindowHandle.layer <= this.navInputConsumer.mWindowHandle.layer) {
                    InputMonitor.this.addInputWindowHandle(this.navInputConsumer.mWindowHandle);
                    boolean unused3 = InputMonitor.this.mAddInputConsumerHandle = false;
                }
                if (InputMonitor.this.mAddWallpaperInputConsumerHandle && windowState.mAttrs.type == 2013 && w.isVisibleLw()) {
                    InputMonitor.this.addInputWindowHandle(this.wallpaperInputConsumer.mWindowHandle);
                    boolean unused4 = InputMonitor.this.mAddWallpaperInputConsumerHandle = false;
                }
                if ((privateFlags & 2048) != 0) {
                    boolean unused5 = InputMonitor.this.mDisableWallpaperTouchEvents = true;
                }
                boolean hasWallpaper = this.wallpaperController.isWallpaperTarget(windowState) && (privateFlags & 1024) == 0 && !InputMonitor.this.mDisableWallpaperTouchEvents;
                if (this.inDrag && isVisible && w.getDisplayContent().isDefaultDisplay) {
                    InputMonitor.this.mService.mDragDropController.sendDragStartedIfNeededLocked(windowState);
                }
                InputMonitor.this.addInputWindowHandle(inputWindowHandle, windowState, flags, type, isVisible, hasFocus, hasWallpaper);
            }
        }
    }

    public InputMonitor(WindowManagerService service) {
        this.mService = service;
    }

    private void addInputConsumer(String name, InputConsumerImpl consumer) {
        this.mInputConsumers.put(name, consumer);
        consumer.linkToDeathRecipient();
        updateInputWindowsLw(true);
    }

    /* access modifiers changed from: package-private */
    public boolean destroyInputConsumer(String name) {
        if (!disposeInputConsumer(this.mInputConsumers.remove(name))) {
            return false;
        }
        updateInputWindowsLw(true);
        return true;
    }

    private boolean disposeInputConsumer(InputConsumerImpl consumer) {
        if (consumer == null) {
            return false;
        }
        consumer.disposeChannelsLw();
        return true;
    }

    /* access modifiers changed from: package-private */
    public InputConsumerImpl getInputConsumer(String name, int displayId) {
        if (displayId == 0) {
            return this.mInputConsumers.get(name);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void layoutInputConsumers(int dw, int dh) {
        for (int i = this.mInputConsumers.size() - 1; i >= 0; i--) {
            this.mInputConsumers.valueAt(i).layout(dw, dh);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowManagerPolicy.InputConsumer createInputConsumer(Looper looper, String name, InputEventReceiver.Factory inputEventReceiverFactory) {
        if (!this.mInputConsumers.containsKey(name)) {
            EventReceiverInputConsumer eventReceiverInputConsumer = new EventReceiverInputConsumer(this.mService, this, looper, name, inputEventReceiverFactory, Process.myPid(), UserHandle.SYSTEM);
            addInputConsumer(name, eventReceiverInputConsumer);
            return eventReceiverInputConsumer;
        }
        throw new IllegalStateException("Existing input consumer found with name: " + name);
    }

    /* access modifiers changed from: package-private */
    public void createInputConsumer(IBinder token, String name, InputChannel inputChannel, int clientPid, UserHandle clientUser) {
        if (!this.mInputConsumers.containsKey(name)) {
            InputConsumerImpl inputConsumerImpl = new InputConsumerImpl(this.mService, token, name, inputChannel, clientPid, clientUser);
            char c = 65535;
            int hashCode = name.hashCode();
            if (hashCode != 1024719987) {
                if (hashCode == 1415830696 && name.equals("wallpaper_input_consumer")) {
                    c = 0;
                }
            } else if (name.equals("pip_input_consumer")) {
                c = 1;
            }
            switch (c) {
                case 0:
                    inputConsumerImpl.mWindowHandle.hasWallpaper = true;
                    break;
                case 1:
                    inputConsumerImpl.mWindowHandle.layoutParamsFlags |= 32;
                    break;
            }
            addInputConsumer(name, inputConsumerImpl);
            return;
        }
        throw new IllegalStateException("Existing input consumer found with name: " + name);
    }

    public void notifyInputChannelBroken(InputWindowHandle inputWindowHandle) {
        if (inputWindowHandle != null) {
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = (WindowState) inputWindowHandle.windowState;
                    if (windowState != null) {
                        Slog.i("WindowManager", "WINDOW DIED " + windowState);
                        windowState.removeIfPossible();
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
    }

    public long notifyANR(InputApplicationHandle inputApplicationHandle, InputWindowHandle inputWindowHandle, String reason) {
        boolean abort;
        AppWindowToken appWindowToken = null;
        WindowState windowState = null;
        boolean aboveSystem = false;
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (inputWindowHandle != null) {
                    windowState = (WindowState) inputWindowHandle.windowState;
                    if (windowState != null) {
                        appWindowToken = windowState.mAppToken;
                    }
                }
                if (appWindowToken == null && inputApplicationHandle != null) {
                    appWindowToken = (AppWindowToken) inputApplicationHandle.appWindowToken;
                }
                abort = false;
                if (windowState != null) {
                    Slog.i("WindowManager", "Input event dispatching timed out sending to " + windowState.mAttrs.getTitle() + ".  Reason: " + reason);
                    aboveSystem = windowState.mBaseLayer > this.mService.mPolicy.getWindowLayerFromTypeLw(2038, windowState.mOwnerCanAddInternalSystemWindow);
                } else if (appWindowToken != null) {
                    Slog.i("WindowManager", "Input event dispatching timed out sending to application " + appWindowToken.stringName + ".  Reason: " + reason);
                } else {
                    Slog.i("WindowManager", "Input event dispatching timed out .  Reason: " + reason);
                }
                this.mService.saveANRStateLocked(appWindowToken, windowState, reason);
            } catch (Throwable th) {
                while (true) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        WindowManagerService.resetPriorityAfterLockedSection();
        this.mService.mAmInternal.saveANRState(reason);
        if (appWindowToken != null && appWindowToken.appToken != null) {
            AppWindowContainerController controller = appWindowToken.getController();
            if (controller != null) {
                if (controller.keyDispatchingTimedOut(reason, windowState != null ? windowState.mSession.mPid : -1)) {
                    abort = true;
                }
            }
            if (!abort) {
                return appWindowToken.mInputDispatchingTimeoutNanos;
            }
        } else if (windowState != null) {
            try {
                long timeout = ActivityManager.getService().inputDispatchingTimedOut(windowState.mSession.mPid, aboveSystem, reason);
                if (timeout >= 0) {
                    return 1000000 * timeout;
                }
            } catch (RemoteException e) {
            }
        }
        return 0;
    }

    /* access modifiers changed from: private */
    public void addInputWindowHandle(InputWindowHandle windowHandle) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(windowHandle.displayId)) {
            WindowState ws = (WindowState) windowHandle.windowState;
            if (!(ws == null || ws.getAttrs().getTitle() == null || !"com.huawei.systemui.mk.lighterdrawer.LighterDrawView".equals(ws.getAttrs().getTitle().toString()))) {
                this.mHasLighterViewInPCCastModeTemp = true;
            }
        }
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

    /* access modifiers changed from: package-private */
    public void addInputWindowHandle(InputWindowHandle inputWindowHandle, WindowState child, int flags, int type, boolean isVisible, boolean hasFocus, boolean hasWallpaper) {
        inputWindowHandle.name = child.toString();
        inputWindowHandle.layoutParamsFlags = child.getTouchableRegion(inputWindowHandle.touchableRegion, flags);
        boolean z = false;
        if (inputWindowHandle.windowState != null && (inputWindowHandle.windowState instanceof WindowState)) {
            WindowState windowState = (WindowState) inputWindowHandle.windowState;
            inputWindowHandle.layoutParamsPrivateFlags = windowState.isWindowUsingNotch() ? 65536 : 0;
            int i = 131072;
            boolean isExcludeTransferEventWindow = (windowState.getAttrs().hwFlags & 131072) != 0;
            int i2 = inputWindowHandle.layoutParamsPrivateFlags;
            if (!isExcludeTransferEventWindow) {
                i = 0;
            }
            inputWindowHandle.layoutParamsPrivateFlags = i | i2;
        }
        inputWindowHandle.layoutParamsType = type;
        inputWindowHandle.dispatchingTimeoutNanos = child.getInputDispatchingTimeoutNanos();
        inputWindowHandle.visible = isVisible;
        inputWindowHandle.canReceiveKeys = child.canReceiveKeys();
        inputWindowHandle.hasFocus = hasFocus;
        inputWindowHandle.hasWallpaper = hasWallpaper;
        if (child.mAppToken != null) {
            z = child.mAppToken.paused;
        }
        inputWindowHandle.paused = z;
        inputWindowHandle.layer = child.mLayer;
        inputWindowHandle.ownerPid = child.mSession.mPid;
        inputWindowHandle.ownerUid = child.mSession.mUid;
        inputWindowHandle.inputFeatures = child.mAttrs.inputFeatures;
        inputWindowHandle.isFreeform = child.inFreeformWindowingMode();
        Rect frame = child.mFrame;
        inputWindowHandle.frameLeft = frame.left;
        inputWindowHandle.frameTop = frame.top;
        inputWindowHandle.frameRight = frame.right;
        inputWindowHandle.frameBottom = frame.bottom;
        inputWindowHandle.displayId = child.getDisplayId();
        if (child.mGlobalScale != 1.0f) {
            inputWindowHandle.scaleFactor = 1.0f / child.mGlobalScale;
        } else {
            inputWindowHandle.scaleFactor = 1.0f;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT) {
            Slog.d("WindowManager", "addInputWindowHandle: " + child + ", " + inputWindowHandle);
        }
        addInputWindowHandle(inputWindowHandle);
        if (hasFocus) {
            this.mFocusedInputWindowHandle = inputWindowHandle;
        }
    }

    /* access modifiers changed from: private */
    public void clearInputWindowHandlesLw() {
        while (this.mInputWindowHandleCount != 0) {
            InputWindowHandle[] inputWindowHandleArr = this.mInputWindowHandles;
            int i = this.mInputWindowHandleCount - 1;
            this.mInputWindowHandleCount = i;
            inputWindowHandleArr[i] = null;
        }
        this.mFocusedInputWindowHandle = null;
    }

    /* access modifiers changed from: package-private */
    public void setUpdateInputWindowsNeededLw() {
        this.mUpdateInputWindowsNeeded = true;
    }

    /* access modifiers changed from: package-private */
    public void updateInputWindowsLw(boolean force) {
        if (force || this.mUpdateInputWindowsNeeded) {
            this.mUpdateInputWindowsNeeded = false;
            boolean inDrag = this.mService.mDragDropController.dragDropActiveLocked();
            if (inDrag) {
                InputWindowHandle dragWindowHandle = this.mService.mDragDropController.getInputWindowHandleLocked();
                if (dragWindowHandle != null) {
                    addInputWindowHandle(dragWindowHandle);
                } else {
                    Slog.w("WindowManager", "Drag is in progress but there is no drag window handle.");
                }
            }
            if (this.mService.mTaskPositioningController.isPositioningLocked()) {
                InputWindowHandle dragWindowHandle2 = this.mService.mTaskPositioningController.getDragWindowHandleLocked();
                if (dragWindowHandle2 != null) {
                    addInputWindowHandle(dragWindowHandle2);
                } else {
                    Slog.e("WindowManager", "Repositioning is in progress but there is no drag window handle.");
                }
            }
            this.mUpdateInputForAllWindowsConsumer.updateInputWindows(inDrag);
        }
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
        return (this.mService.mPolicy.getWindowLayerFromTypeLw(2018) * 10000) + 1000;
    }

    public void setInputFocusLw(WindowState newWindow, boolean updateInputWindows) {
        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT || WindowManagerDebugConfig.DEBUG_INPUT) {
            Slog.d("WindowManager", "Input focus has changed to " + newWindow);
        }
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
        handle.dispatchingTimeoutNanos = newApp.mInputDispatchingTimeoutNanos;
        this.mService.mInputManager.setFocusedApplication(handle);
    }

    public void pauseDispatchingLw(WindowToken window) {
        if (!window.paused) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v("WindowManager", "Pausing WindowToken " + window);
            }
            window.paused = true;
            updateInputWindowsLw(true);
        }
    }

    public void resumeDispatchingLw(WindowToken window) {
        if (window.paused) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v("WindowManager", "Resuming WindowToken " + window);
            }
            window.paused = false;
            updateInputWindowsLw(true);
        }
    }

    public void freezeInputDispatchingLw() {
        if (!this.mInputDispatchFrozen) {
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v("WindowManager", "Freezing input dispatching");
            }
            this.mInputDispatchFrozen = true;
            boolean z = WindowManagerDebugConfig.DEBUG_INPUT;
            this.mInputFreezeReason = Debug.getCallers(6);
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
            if (WindowManagerDebugConfig.DEBUG_INPUT) {
                Slog.v("WindowManager", "Setting event dispatching to " + enabled);
            }
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
        Set<String> inputConsumerKeys = this.mInputConsumers.keySet();
        if (!inputConsumerKeys.isEmpty()) {
            pw.println(prefix + "InputConsumers:");
            for (String key : inputConsumerKeys) {
                this.mInputConsumers.get(key).dump(pw, key, prefix);
            }
        }
    }

    public void notifyANRWarning(int pid) {
    }

    public boolean hasLighterViewInPCCastMode() {
        return HwPCUtils.isPcCastModeInServer() && this.mHasLighterViewInPCCastMode;
    }

    /* access modifiers changed from: private */
    public void setFousedWinExtDisplayInPCCastMode() {
        if (HwPCUtils.isPcCastModeInServer()) {
            int i = 0;
            while (i < this.mInputWindowHandles.length) {
                InputWindowHandle inputWindowHandle = this.mInputWindowHandles[i];
                if (inputWindowHandle == null || !HwPCUtils.isValidExtDisplayId(inputWindowHandle.displayId) || !inputWindowHandle.canReceiveKeys) {
                    i++;
                } else {
                    this.mInputWindowHandleInExtDisplay = inputWindowHandle;
                    return;
                }
            }
            this.mInputWindowHandleInExtDisplay = null;
        }
    }

    public InputWindowHandle getFousedWinExtDisplayInPCCastMode() {
        return this.mInputWindowHandleInExtDisplay;
    }
}
