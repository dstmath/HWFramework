package com.android.server.wm;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Slog;
import android.view.InputChannel;
import android.view.InputEventReceiver;
import android.view.InputEventReceiver.Factory;
import android.view.KeyEvent;
import android.view.WindowManagerPolicy.InputConsumer;
import com.android.server.input.InputApplicationHandle;
import com.android.server.input.InputManagerService.WindowManagerCallbacks;
import com.android.server.input.InputWindowHandle;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

final class InputMonitor implements WindowManagerCallbacks {
    private boolean mAddInputConsumerHandle;
    private boolean mAddPipInputConsumerHandle;
    private boolean mAddWallpaperInputConsumerHandle;
    private boolean mDisableWallpaperTouchEvents;
    private InputWindowHandle mFocusedInputWindowHandle;
    private final ArrayMap<String, InputConsumerImpl> mInputConsumers = new ArrayMap();
    private boolean mInputDevicesReady;
    private final Object mInputDevicesReadyMonitor = new Object();
    private boolean mInputDispatchEnabled;
    private boolean mInputDispatchFrozen;
    private WindowState mInputFocus;
    private String mInputFreezeReason = null;
    private int mInputWindowHandleCount;
    private InputWindowHandle[] mInputWindowHandles;
    private final WindowManagerService mService;
    private final Rect mTmpRect = new Rect();
    private final UpdateInputForAllWindowsConsumer mUpdateInputForAllWindowsConsumer = new UpdateInputForAllWindowsConsumer(this, null);
    private boolean mUpdateInputWindowsNeeded = true;

    private static final class EventReceiverInputConsumer extends InputConsumerImpl implements InputConsumer {
        private final InputEventReceiver mInputEventReceiver;
        private InputMonitor mInputMonitor;

        EventReceiverInputConsumer(WindowManagerService service, InputMonitor monitor, Looper looper, String name, Factory inputEventReceiverFactory) {
            super(service, name, null);
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
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    private final class UpdateInputForAllWindowsConsumer implements Consumer<WindowState> {
        boolean inDrag;
        InputConsumerImpl navInputConsumer;
        InputConsumerImpl pipInputConsumer;
        Rect pipTouchableBounds;
        WallpaperController wallpaperController;
        InputConsumerImpl wallpaperInputConsumer;

        /* synthetic */ UpdateInputForAllWindowsConsumer(InputMonitor this$0, UpdateInputForAllWindowsConsumer -this1) {
            this();
        }

        private UpdateInputForAllWindowsConsumer() {
        }

        private void updateInputWindows(boolean inDrag) {
            boolean z;
            Rect -get8;
            this.navInputConsumer = InputMonitor.this.getInputConsumer("nav_input_consumer", 0);
            this.pipInputConsumer = InputMonitor.this.getInputConsumer("pip_input_consumer", 0);
            this.wallpaperInputConsumer = InputMonitor.this.getInputConsumer("wallpaper_input_consumer", 0);
            InputMonitor.this.mAddInputConsumerHandle = this.navInputConsumer != null;
            InputMonitor inputMonitor = InputMonitor.this;
            if (this.pipInputConsumer != null) {
                z = true;
            } else {
                z = false;
            }
            inputMonitor.mAddPipInputConsumerHandle = z;
            inputMonitor = InputMonitor.this;
            if (this.wallpaperInputConsumer != null) {
                z = true;
            } else {
                z = false;
            }
            inputMonitor.mAddWallpaperInputConsumerHandle = z;
            InputMonitor.this.mTmpRect.setEmpty();
            if (InputMonitor.this.mAddPipInputConsumerHandle) {
                -get8 = InputMonitor.this.mTmpRect;
            } else {
                -get8 = null;
            }
            this.pipTouchableBounds = -get8;
            InputMonitor.this.mDisableWallpaperTouchEvents = false;
            this.inDrag = inDrag;
            this.wallpaperController = InputMonitor.this.mService.mRoot.mWallpaperController;
            InputMonitor.this.mService.mRoot.forAllWindows((Consumer) this, true);
            if (InputMonitor.this.mAddWallpaperInputConsumerHandle) {
                InputMonitor.this.addInputWindowHandle(this.wallpaperInputConsumer.mWindowHandle);
            }
            InputMonitor.this.mService.mInputManager.setInputWindows(InputMonitor.this.mInputWindowHandles, InputMonitor.this.mFocusedInputWindowHandle);
            InputMonitor.this.clearInputWindowHandlesLw();
        }

        public void accept(WindowState w) {
            InputChannel inputChannel = w.mInputChannel;
            InputWindowHandle inputWindowHandle = w.mInputWindowHandle;
            if (inputChannel != null && inputWindowHandle != null && !w.mRemoved && !w.canReceiveTouchInput()) {
                boolean hasWallpaper;
                int flags = w.mAttrs.flags;
                int privateFlags = w.mAttrs.privateFlags;
                int type = w.mAttrs.type;
                boolean hasFocus = w == InputMonitor.this.mInputFocus;
                boolean isVisible = w.isVisibleLw();
                if (w.getStackId() == 4) {
                    if (InputMonitor.this.mAddPipInputConsumerHandle && inputWindowHandle.layer <= this.pipInputConsumer.mWindowHandle.layer) {
                        w.getStack().getBounds(this.pipTouchableBounds);
                        this.pipInputConsumer.mWindowHandle.touchableRegion.set(this.pipTouchableBounds);
                        InputMonitor.this.addInputWindowHandle(this.pipInputConsumer.mWindowHandle);
                        InputMonitor.this.mAddPipInputConsumerHandle = false;
                    }
                    if (!hasFocus) {
                        return;
                    }
                }
                if (InputMonitor.this.mAddInputConsumerHandle && inputWindowHandle.layer <= this.navInputConsumer.mWindowHandle.layer) {
                    InputMonitor.this.addInputWindowHandle(this.navInputConsumer.mWindowHandle);
                    InputMonitor.this.mAddInputConsumerHandle = false;
                }
                if (InputMonitor.this.mAddWallpaperInputConsumerHandle && w.mAttrs.type == 2013 && w.isVisibleLw()) {
                    InputMonitor.this.addInputWindowHandle(this.wallpaperInputConsumer.mWindowHandle);
                    InputMonitor.this.mAddWallpaperInputConsumerHandle = false;
                }
                if ((privateFlags & 2048) != 0) {
                    InputMonitor.this.mDisableWallpaperTouchEvents = true;
                }
                if (this.wallpaperController.isWallpaperTarget(w) && (privateFlags & 1024) == 0) {
                    hasWallpaper = InputMonitor.this.mDisableWallpaperTouchEvents ^ 1;
                } else {
                    hasWallpaper = false;
                }
                if (this.inDrag && isVisible && w.getDisplayContent().isDefaultDisplay) {
                    InputMonitor.this.mService.mDragState.sendDragStartedIfNeededLw(w);
                }
                InputMonitor.this.addInputWindowHandle(inputWindowHandle, w, flags, type, isVisible, hasFocus, hasWallpaper);
            }
        }
    }

    public InputMonitor(WindowManagerService service) {
        this.mService = service;
    }

    private void addInputConsumer(String name, InputConsumerImpl consumer) {
        this.mInputConsumers.put(name, consumer);
        updateInputWindowsLw(true);
    }

    boolean destroyInputConsumer(String name) {
        if (!disposeInputConsumer((InputConsumerImpl) this.mInputConsumers.remove(name))) {
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

    InputConsumerImpl getInputConsumer(String name, int displayId) {
        return displayId == 0 ? (InputConsumerImpl) this.mInputConsumers.get(name) : null;
    }

    void layoutInputConsumers(int dw, int dh) {
        for (int i = this.mInputConsumers.size() - 1; i >= 0; i--) {
            ((InputConsumerImpl) this.mInputConsumers.valueAt(i)).layout(dw, dh);
        }
    }

    InputConsumer createInputConsumer(Looper looper, String name, Factory inputEventReceiverFactory) {
        if (this.mInputConsumers.containsKey(name)) {
            throw new IllegalStateException("Existing input consumer found with name: " + name);
        }
        EventReceiverInputConsumer consumer = new EventReceiverInputConsumer(this.mService, this, looper, name, inputEventReceiverFactory);
        addInputConsumer(name, consumer);
        return consumer;
    }

    void createInputConsumer(String name, InputChannel inputChannel) {
        if (this.mInputConsumers.containsKey(name)) {
            throw new IllegalStateException("Existing input consumer found with name: " + name);
        }
        InputConsumerImpl consumer = new InputConsumerImpl(this.mService, name, inputChannel);
        if (name.equals("wallpaper_input_consumer")) {
            consumer.mWindowHandle.hasWallpaper = true;
        } else if (name.equals("pip_input_consumer")) {
            InputWindowHandle inputWindowHandle = consumer.mWindowHandle;
            inputWindowHandle.layoutParamsFlags |= 32;
        }
        addInputConsumer(name, consumer);
    }

    public void notifyInputChannelBroken(InputWindowHandle inputWindowHandle) {
        if (inputWindowHandle != null) {
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    WindowState windowState = inputWindowHandle.windowState;
                    if (windowState != null) {
                        Slog.i("WindowManager", "WINDOW DIED " + windowState);
                        windowState.removeIfPossible();
                    }
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
        }
    }

    public long notifyANR(InputApplicationHandle inputApplicationHandle, InputWindowHandle inputWindowHandle, String reason) {
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
                if (windowState != null) {
                    Slog.i("WindowManager", "Input event dispatching timed out sending to " + windowState.mAttrs.getTitle() + ".  Reason: " + reason);
                    aboveSystem = windowState.mBaseLayer > this.mService.mPolicy.getWindowLayerFromTypeLw(2038, windowState.mOwnerCanAddInternalSystemWindow);
                } else if (appWindowToken != null) {
                    Slog.i("WindowManager", "Input event dispatching timed out sending to application " + appWindowToken.stringName + ".  Reason: " + reason);
                } else {
                    Slog.i("WindowManager", "Input event dispatching timed out .  Reason: " + reason);
                }
                this.mService.saveANRStateLocked(appWindowToken, windowState, reason);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        this.mService.mAmInternal.saveANRState(reason);
        if (appWindowToken != null && appWindowToken.appToken != null) {
            boolean abort;
            AppWindowContainerController controller = appWindowToken.getController();
            if (controller != null) {
                abort = controller.keyDispatchingTimedOut(reason, windowState != null ? windowState.mSession.mPid : -1);
            } else {
                abort = false;
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

    private void addInputWindowHandle(InputWindowHandle windowHandle) {
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

    void addInputWindowHandle(InputWindowHandle inputWindowHandle, WindowState child, int flags, int type, boolean isVisible, boolean hasFocus, boolean hasWallpaper) {
        inputWindowHandle.name = child.toString();
        inputWindowHandle.layoutParamsFlags = child.getTouchableRegion(inputWindowHandle.touchableRegion, flags);
        if (inputWindowHandle.windowState != null && (inputWindowHandle.windowState instanceof WindowState)) {
            boolean isNotchSupport;
            WindowState windowState = inputWindowHandle.windowState;
            if ((windowState.getAttrs().hwFlags & 65536) == 0) {
                isNotchSupport = windowState.getHwNotchSupport();
            } else {
                isNotchSupport = true;
            }
            inputWindowHandle.layoutParamsPrivateFlags = isNotchSupport ? 65536 : 0;
        }
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
        inputWindowHandle.displayId = child.getDisplayId();
        if (child.mGlobalScale != 1.0f) {
            inputWindowHandle.scaleFactor = 1.0f / child.mGlobalScale;
        } else {
            inputWindowHandle.scaleFactor = 1.0f;
        }
        addInputWindowHandle(inputWindowHandle);
        if (hasFocus) {
            this.mFocusedInputWindowHandle = inputWindowHandle;
        }
    }

    private void clearInputWindowHandlesLw() {
        while (this.mInputWindowHandleCount != 0) {
            InputWindowHandle[] inputWindowHandleArr = this.mInputWindowHandles;
            int i = this.mInputWindowHandleCount - 1;
            this.mInputWindowHandleCount = i;
            inputWindowHandleArr[i] = null;
        }
        this.mFocusedInputWindowHandle = null;
    }

    void setUpdateInputWindowsNeededLw() {
        this.mUpdateInputWindowsNeeded = true;
    }

    void updateInputWindowsLw(boolean force) {
        if (force || (this.mUpdateInputWindowsNeeded ^ 1) == 0) {
            InputWindowHandle dragWindowHandle;
            this.mUpdateInputWindowsNeeded = false;
            boolean inDrag = this.mService.mDragState != null;
            if (inDrag) {
                dragWindowHandle = this.mService.mDragState.getInputWindowHandle();
                if (dragWindowHandle != null) {
                    addInputWindowHandle(dragWindowHandle);
                } else {
                    Slog.w("WindowManager", "Drag is in progress but there is no drag window handle.");
                }
            }
            if (this.mService.mTaskPositioner != null) {
                dragWindowHandle = this.mService.mTaskPositioner.mDragWindowHandle;
                if (dragWindowHandle != null) {
                    addInputWindowHandle(dragWindowHandle);
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
        Set<String> inputConsumerKeys = this.mInputConsumers.keySet();
        if (!inputConsumerKeys.isEmpty()) {
            pw.println(prefix + "InputConsumers:");
            for (String key : inputConsumerKeys) {
                pw.println(prefix + "  name=" + key);
            }
        }
    }

    public void notifyANRWarning(int pid) {
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManager.getService().asBinder().transact(501, data, reply, 0);
            reply.readException();
            data.recycle();
            reply.recycle();
        } catch (Exception ex) {
            Slog.e("WindowManager", "notifyANRWarning exception:" + ex);
        }
    }
}
