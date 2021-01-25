package com.android.server.input;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayViewport;
import android.hardware.input.IInputDevicesChangedListener;
import android.hardware.input.ITabletModeChangedListener;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManagerInternal;
import android.hardware.input.KeyboardLayout;
import android.hardware.input.TouchCalibration;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IInputFilter;
import android.view.IInputFilterHost;
import android.view.IInputMonitorHost;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputMonitor;
import android.view.InputWindowHandle;
import android.view.KeyEvent;
import android.view.PointerIcon;
import android.view.ViewConfiguration;
import com.android.internal.R;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.BatteryService;
import com.android.server.DisplayThread;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.Watchdog;
import com.android.server.pm.DumpState;
import com.android.server.slice.SliceClientPermissions;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.hardware.input.IHwInputManager;
import com.huawei.android.hardware.input.IHwTHPEventListener;
import com.huawei.android.view.HwWindowManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import libcore.io.IoUtils;
import libcore.io.Streams;

public class InputManagerService extends AbsInputManagerService implements Watchdog.Monitor, IHwInputManagerInner {
    private static final String BASIC_MODE_KEYMOUSE = "com.huawei.permission.BASIC_MODE_KEYMOUSE";
    public static final int BTN_MOUSE = 272;
    static final boolean DEBUG = false;
    protected static final boolean DEBUG_HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    static final String DEFAULT_EN_LAYOUT_UK = "com.android.inputdevices/com.android.inputdevices.InputDeviceReceiver/keyboard_layout_english_uk";
    static final String DEFAULT_EN_LAYOUT_US = "com.android.inputdevices/com.android.inputdevices.InputDeviceReceiver/keyboard_layout_english_us";
    private static final String EXCLUDED_DEVICES_PATH = "etc/excluded-input-devices.xml";
    private static final int INJECTION_TIMEOUT_MILLIS = 30000;
    private static final int INPUT_EVENT_INJECTION_FAILED = 2;
    private static final int INPUT_EVENT_INJECTION_PERMISSION_DENIED = 1;
    private static final int INPUT_EVENT_INJECTION_SUCCEEDED = 0;
    private static final int INPUT_EVENT_INJECTION_TIMED_OUT = 3;
    private static final boolean IS_SUPPORT_AUTO_KEYBOARD_LAYOUT = "tablet".equals(SystemProperties.get("ro.build.characteristics", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR));
    public static final int KEY_STATE_DOWN = 1;
    public static final int KEY_STATE_UNKNOWN = -1;
    public static final int KEY_STATE_UP = 0;
    public static final int KEY_STATE_VIRTUAL = 2;
    private static final int MSG_DELIVER_INPUT_DEVICES_CHANGED = 1;
    private static final int MSG_DELIVER_TABLET_MODE_CHANGED = 6;
    private static final int MSG_RELOAD_DEVICE_ALIASES = 5;
    private static final int MSG_RELOAD_KEYBOARD_LAYOUTS = 3;
    private static final int MSG_SWITCH_KEYBOARD_LAYOUT = 2;
    private static final int MSG_UPDATE_KEYBOARD_LAYOUTS = 4;
    private static final String PERMISSION_HW_INJECT_EVENTS = "com.huawei.permission.HW_INJECT_EVENTS";
    private static final String PORT_ASSOCIATIONS_PATH = "etc/input-port-associations.xml";
    public static final int SW_CAMERA_LENS_COVER = 9;
    public static final int SW_CAMERA_LENS_COVER_BIT = 512;
    public static final int SW_HEADPHONE_INSERT = 2;
    public static final int SW_HEADPHONE_INSERT_BIT = 4;
    public static final int SW_JACK_BITS = 212;
    public static final int SW_JACK_PHYSICAL_INSERT = 7;
    public static final int SW_JACK_PHYSICAL_INSERT_BIT = 128;
    public static final int SW_KEYPAD_SLIDE = 10;
    public static final int SW_KEYPAD_SLIDE_BIT = 1024;
    public static final int SW_LID = 0;
    public static final int SW_LID_BIT = 1;
    public static final int SW_LINEOUT_INSERT = 6;
    public static final int SW_LINEOUT_INSERT_BIT = 64;
    public static final int SW_MICROPHONE_INSERT = 4;
    public static final int SW_MICROPHONE_INSERT_BIT = 16;
    public static final int SW_TABLET_MODE = 1;
    public static final int SW_TABLET_MODE_BIT = 2;
    static final String TAG = "InputManager";
    final Context mContext;
    private final String[] mCountryUS = {"BE", "CA"};
    private final PersistentDataStore mDataStore = new PersistentDataStore();
    private Context mDisplayContext;
    private final File mDoubleTouchGestureEnableFile;
    private IWindow mFocusedWindow;
    private boolean mFocusedWindowHasCapture;
    private final InputManagerHandler mHandler;
    IHwInputManagerServiceEx mHwIMSEx = null;
    HwInnerInputManagerService mHwInnerService = new HwInnerInputManagerService(this);
    private InputDevice[] mInputDevices = new InputDevice[0];
    private final SparseArray<InputDevicesChangedListenerRecord> mInputDevicesChangedListeners = new SparseArray<>();
    private boolean mInputDevicesChangedPending;
    private Object mInputDevicesLock = new Object();
    IInputFilter mInputFilter;
    InputFilterHost mInputFilterHost;
    final Object mInputFilterLock = new Object();
    private PendingIntent mKeyboardLayoutIntent;
    private boolean mKeyboardLayoutNotificationShown;
    private final String[] mLanguageUS = {"ar", "az", "bg", "bn", "bo", "es", "et", "fa", "he", "hi", "hr", "hu", "iw", "ka", "ko", "lv", "mk", "ru", "sk", "sr", "sv", "th", "tr", "uk", "ur", "zh"};
    private int mNextVibratorTokenValue;
    private NotificationManager mNotificationManager;
    public final long mPtr;
    private boolean mSystemReady;
    private final SparseArray<TabletModeChangedListenerRecord> mTabletModeChangedListeners = new SparseArray<>();
    private final Object mTabletModeLock = new Object();
    private final ArrayList<InputDevice> mTempFullKeyboards = new ArrayList<>();
    private final ArrayList<InputDevicesChangedListenerRecord> mTempInputDevicesChangedListenersToNotify = new ArrayList<>();
    private final List<TabletModeChangedListenerRecord> mTempTabletModeChangedListenersToNotify = new ArrayList();
    final boolean mUseDevInputEventForAudioJack;
    private Object mVibratorLock = new Object();
    private HashMap<IBinder, VibratorToken> mVibratorTokens = new HashMap<>();
    WindowManagerCallbacks mWindowManagerCallbacks;
    private WiredAccessoryCallbacks mWiredAccessoryCallbacks;

    /* access modifiers changed from: private */
    public interface KeyboardLayoutVisitor {
        void visitKeyboardLayout(Resources resources, int i, KeyboardLayout keyboardLayout);
    }

    public interface WindowManagerCallbacks {
        KeyEvent dispatchUnhandledKey(IBinder iBinder, KeyEvent keyEvent, int i);

        int getPointerDisplayId();

        int getPointerLayer();

        long interceptKeyBeforeDispatching(IBinder iBinder, KeyEvent keyEvent, int i);

        int interceptKeyBeforeQueueing(KeyEvent keyEvent, int i);

        int interceptMotionBeforeQueueingNonInteractive(int i, long j, int i2);

        long notifyANR(IBinder iBinder, String str);

        void notifyCameraLensCoverSwitchChanged(long j, boolean z);

        void notifyConfigurationChanged();

        void notifyInputChannelBroken(IBinder iBinder);

        void notifyLidSwitchChanged(long j, boolean z);

        void onPointerDownOutsideFocus(IBinder iBinder);
    }

    public interface WiredAccessoryCallbacks {
        void notifyWiredAccessoryChanged(long j, int i, int i2);

        void systemReady();
    }

    private static native boolean nativeCanDispatchToDisplay(long j, int i, int i2);

    private static native void nativeCancelVibrate(long j, int i, int i2);

    private static native void nativeDisableInputDevice(long j, int i);

    private static native String nativeDump(long j);

    private static native void nativeEnableInputDevice(long j, int i);

    /* access modifiers changed from: private */
    public static native void nativeFadeMousePointer(long j);

    private static native int nativeGetKeyCodeState(long j, int i, int i2, int i3);

    private static native int nativeGetScanCodeState(long j, int i, int i2, int i3);

    private static native int nativeGetSwitchState(long j, int i, int i2, int i3);

    private static native boolean nativeHasKeys(long j, int i, int i2, int[] iArr, boolean[] zArr);

    private static native long nativeInit(InputManagerService inputManagerService, Context context, MessageQueue messageQueue);

    /* access modifiers changed from: private */
    public static native int nativeInjectInputEvent(long j, InputEvent inputEvent, int i, int i2, int i3, int i4, int i5);

    private static native boolean nativeIsInputDeviceEnabled(long j, int i);

    private static native void nativeMonitor(long j);

    /* access modifiers changed from: private */
    public static native void nativePilferPointers(long j, IBinder iBinder);

    private static native void nativeRegisterInputChannel(long j, InputChannel inputChannel, int i);

    private static native void nativeRegisterInputMonitor(long j, InputChannel inputChannel, int i, boolean z);

    private static native void nativeReloadCalibration(long j);

    private static native void nativeReloadDeviceAliases(long j);

    private static native void nativeReloadKeyboardLayouts(long j);

    public static native void nativeReloadPointerIcons(long j, Context context);

    protected static native void nativeResponseTouchEvent(long j, boolean z);

    private static native void nativeSetCustomPointerIcon(long j, PointerIcon pointerIcon);

    protected static native void nativeSetDispatchDisplayInfo(long j, int i, int i2, int i3, int i4);

    /* access modifiers changed from: private */
    public static native void nativeSetDisplayMode(long j, int i, int i2, int i3, int i4);

    private static native void nativeSetDisplayViewports(long j, DisplayViewport[] displayViewportArr);

    private static native void nativeSetFocusedApplication(long j, int i, InputApplicationHandle inputApplicationHandle);

    private static native void nativeSetFocusedDisplay(long j, int i);

    protected static native void nativeSetIawareGameMode(long j, int i);

    protected static native void nativeSetIawareGameModeAccurate(long j, int i);

    private static native void nativeSetInputDispatchMode(long j, boolean z, boolean z2);

    protected static native void nativeSetInputFilterEnabled(long j, boolean z);

    protected static native void nativeSetInputScaleConfig(long j, float f, float f2, int i, int i2);

    private static native void nativeSetInputWindows(long j, InputWindowHandle[] inputWindowHandleArr, int i);

    /* access modifiers changed from: private */
    public static native void nativeSetInteractive(long j, boolean z);

    protected static native void nativeSetKeyguardState(long j, boolean z);

    protected static native void nativeSetMirrorLinkInputStatus(long j, boolean z);

    /* access modifiers changed from: private */
    public static native void nativeSetMousePosition(long j, float f, float f2);

    private static native void nativeSetPointerCapture(long j, boolean z);

    private static native void nativeSetPointerIconType(long j, int i);

    private static native void nativeSetPointerSpeed(long j, int i);

    private static native void nativeSetShowTouches(long j, boolean z);

    private static native void nativeSetSystemUiVisibility(long j, int i);

    private static native void nativeStart(long j);

    /* access modifiers changed from: private */
    public static native void nativeToggleCapsLock(long j, int i);

    /* access modifiers changed from: private */
    public static native void nativeUnregisterInputChannel(long j, InputChannel inputChannel);

    private static native void nativeVibrate(long j, int i, long[] jArr, int i2, int i3);

    public InputManagerService(Context context) {
        File file;
        this.mContext = context;
        this.mHandler = new InputManagerHandler(DisplayThread.get().getLooper());
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17891560);
        Slog.i(TAG, "Initializing input manager, mUseDevInputEventForAudioJack=" + this.mUseDevInputEventForAudioJack);
        this.mPtr = nativeInit(this, this.mContext, this.mHandler.getLooper().getQueue());
        String doubleTouchGestureEnablePath = context.getResources().getString(17039841);
        if (TextUtils.isEmpty(doubleTouchGestureEnablePath)) {
            file = null;
        } else {
            file = new File(doubleTouchGestureEnablePath);
        }
        this.mDoubleTouchGestureEnableFile = file;
        LocalServices.addService(InputManagerInternal.class, new LocalService());
        try {
            this.mHwIMSEx = HwServiceExFactory.getHwInputManagerServiceEx(this, context);
        } catch (Exception e) {
            Slog.e(TAG, "New HwInputManagerService Exception");
        }
    }

    public void setWindowManagerCallbacks(WindowManagerCallbacks callbacks) {
        this.mWindowManagerCallbacks = callbacks;
    }

    public void setWiredAccessoryCallbacks(WiredAccessoryCallbacks callbacks) {
        this.mWiredAccessoryCallbacks = callbacks;
    }

    public void start() {
        Slog.i(TAG, "Starting input manager");
        nativeStart(this.mPtr);
        Watchdog.getInstance().addMonitor(this);
        registerPointerSpeedSettingObserver();
        registerShowTouchesSettingObserver();
        registerAccessibilityLargePointerSettingObserver();
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.input.InputManagerService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                InputManagerService.this.updatePointerSpeedFromSettings();
                InputManagerService.this.updateShowTouchesFromSettings();
                InputManagerService.this.updateAccessibilityLargePointerFromSettings();
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mHandler);
        if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT) {
            this.mContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.input.InputManagerService.AnonymousClass2 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    InputManagerService.this.rsetHasAddedEnLayout();
                    InputManagerService inputManagerService = InputManagerService.this;
                    inputManagerService.notifyInputDevicesChanged(inputManagerService.getInputDevices());
                }
            }, new IntentFilter("android.intent.action.LOCALE_CHANGED"), null, this.mHandler);
        }
        updatePointerSpeedFromSettings();
        updateShowTouchesFromSettings();
        updateAccessibilityLargePointerFromSettings();
    }

    public void systemRunning() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mSystemReady = true;
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.input.InputManagerService.AnonymousClass3 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                InputManagerService.this.updateKeyboardLayouts();
            }
        }, filter, null, this.mHandler);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.input.InputManagerService.AnonymousClass4 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                InputManagerService.this.reloadDeviceAliases();
            }
        }, new IntentFilter("android.bluetooth.device.action.ALIAS_CHANGED"), null, this.mHandler);
        this.mHandler.sendEmptyMessage(5);
        this.mHandler.sendEmptyMessage(4);
        WiredAccessoryCallbacks wiredAccessoryCallbacks = this.mWiredAccessoryCallbacks;
        if (wiredAccessoryCallbacks != null) {
            wiredAccessoryCallbacks.systemReady();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reloadKeyboardLayouts() {
        nativeReloadKeyboardLayouts(this.mPtr);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reloadDeviceAliases() {
        nativeReloadDeviceAliases(this.mPtr);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDisplayViewportsInternal(List<DisplayViewport> viewports) {
        List<DisplayViewport> adjustedViewports = new ArrayList<>();
        viewports.forEach(new Consumer(adjustedViewports) {
            /* class com.android.server.input.$$Lambda$InputManagerService$j90XknDEqCip5hz6aB0Bt4fCqI4 */
            private final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.add(((DisplayViewport) obj).makeCopy());
            }
        });
        if (HwActivityTaskManager.isPCMultiCastMode()) {
            adjustedViewports.forEach($$Lambda$InputManagerService$_1GE8FUE9_QE_Dp1xPvQsX6Otyw.INSTANCE);
        }
        nativeSetDisplayViewports(this.mPtr, (DisplayViewport[]) adjustedViewports.toArray(new DisplayViewport[0]));
    }

    /* JADX INFO: Multiple debug info for r3v1 int: [D('e' android.os.RemoteException), D('logicalWidth' int)] */
    static /* synthetic */ void lambda$setDisplayViewportsInternal$1(DisplayViewport viewport) {
        if (HwActivityTaskManager.getCurPCWindowAreaNum() != 0) {
            int width = viewport.deviceWidth;
            int height = viewport.deviceHeight;
            Point displaySize = new Point();
            try {
                HwWindowManager.getDisplaySize(1, 0, displaySize);
            } catch (RemoteException e) {
                Slog.e(TAG, "getDisplaySize exception");
            }
            int logicalWidth = displaySize.x;
            int logicalHeight = displaySize.y;
            if (width > height) {
                logicalHeight = logicalWidth;
                logicalWidth = logicalHeight;
            }
            viewport.logicalFrame.right = viewport.logicalFrame.left + logicalWidth;
            viewport.logicalFrame.bottom = viewport.logicalFrame.top + logicalHeight;
            viewport.physicalFrame.right = viewport.physicalFrame.left + width;
            viewport.physicalFrame.bottom = viewport.physicalFrame.top + height;
        }
    }

    public int getKeyCodeState(int deviceId, int sourceMask, int keyCode) {
        return nativeGetKeyCodeState(this.mPtr, deviceId, sourceMask, keyCode);
    }

    public int getScanCodeState(int deviceId, int sourceMask, int scanCode) {
        return nativeGetScanCodeState(this.mPtr, deviceId, sourceMask, scanCode);
    }

    public int getSwitchState(int deviceId, int sourceMask, int switchCode) {
        return nativeGetSwitchState(this.mPtr, deviceId, sourceMask, switchCode);
    }

    public boolean hasKeys(int deviceId, int sourceMask, int[] keyCodes, boolean[] keyExists) {
        if (keyCodes == null) {
            throw new IllegalArgumentException("keyCodes must not be null.");
        } else if (keyExists != null && keyExists.length >= keyCodes.length) {
            return nativeHasKeys(this.mPtr, deviceId, sourceMask, keyCodes, keyExists);
        } else {
            throw new IllegalArgumentException("keyExists must not be null and must be at least as large as keyCodes.");
        }
    }

    public InputChannel monitorInput(String inputChannelName, int displayId) {
        if (inputChannelName == null) {
            throw new IllegalArgumentException("inputChannelName must not be null.");
        } else if (displayId >= 0) {
            InputChannel[] inputChannels = InputChannel.openInputChannelPair(inputChannelName);
            inputChannels[0].setToken(new Binder());
            nativeRegisterInputMonitor(this.mPtr, inputChannels[0], displayId, false);
            inputChannels[0].dispose();
            return inputChannels[1];
        } else {
            throw new IllegalArgumentException("displayId must >= 0.");
        }
    }

    public InputMonitor monitorGestureInput(String inputChannelName, int displayId) {
        if (checkCallingPermission("android.permission.MONITOR_INPUT", "monitorInputRegion()")) {
            Objects.requireNonNull(inputChannelName, "inputChannelName must not be null.");
            if (displayId >= 0) {
                Log.i(TAG, "monitorGestureInput inputChannelName:" + inputChannelName + ",displayId:" + displayId);
                long ident = Binder.clearCallingIdentity();
                try {
                    InputChannel[] inputChannels = InputChannel.openInputChannelPair(inputChannelName);
                    InputMonitorHost host = new InputMonitorHost(inputChannels[0]);
                    inputChannels[0].setToken(host.asBinder());
                    nativeRegisterInputMonitor(this.mPtr, inputChannels[0], displayId, true);
                    return new InputMonitor(inputChannelName, inputChannels[1], host);
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            } else {
                throw new IllegalArgumentException("displayId must >= 0.");
            }
        } else {
            throw new SecurityException("Requires MONITOR_INPUT permission");
        }
    }

    public void registerInputChannel(InputChannel inputChannel, IBinder token) {
        if (inputChannel != null) {
            if (token == null) {
                token = new Binder();
            }
            inputChannel.setToken(token);
            nativeRegisterInputChannel(this.mPtr, inputChannel, -1);
            return;
        }
        throw new IllegalArgumentException("inputChannel must not be null.");
    }

    public void unregisterInputChannel(InputChannel inputChannel) {
        if (inputChannel != null) {
            nativeUnregisterInputChannel(this.mPtr, inputChannel);
            return;
        }
        throw new IllegalArgumentException("inputChannel must not be null.");
    }

    public void setInputFilter(IInputFilter filter) {
        synchronized (this.mInputFilterLock) {
            IInputFilter oldFilter = this.mInputFilter;
            if (oldFilter != filter) {
                if (DEBUG_HWFLOW) {
                    Slog.i(TAG, "newFilter=" + filter + ",oldFilter=" + oldFilter);
                }
                if (oldFilter != null) {
                    this.mInputFilter = null;
                    this.mInputFilterHost.disconnectLocked();
                    this.mInputFilterHost = null;
                    try {
                        oldFilter.uninstall();
                    } catch (RemoteException e) {
                    }
                }
                if (filter != null) {
                    this.mInputFilter = filter;
                    this.mInputFilterHost = new InputFilterHost();
                    try {
                        filter.install(this.mInputFilterHost);
                    } catch (RemoteException e2) {
                    }
                }
                nativeSetInputFilterEnabled(this.mPtr, filter != null);
            }
        }
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        return injectInputEventInternal(event, mode);
    }

    /* access modifiers changed from: protected */
    public boolean injectInputEventInternal(InputEvent event, int mode) {
        return injectInputEventInternal(event, mode, 0);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean injectInputEventInternal(InputEvent event, int mode, int appendPolicyFlag) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (mode == 0 || mode == 2 || mode == 1) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                int result = nativeInjectInputEvent(this.mPtr, event, pid, uid, mode, INJECTION_TIMEOUT_MILLIS, appendPolicyFlag | DumpState.DUMP_HWFEATURES);
                Binder.restoreCallingIdentity(ident);
                if (DEBUG_HWFLOW && (event instanceof KeyEvent)) {
                    KeyEvent myEvent = (KeyEvent) event;
                    Slog.i(TAG, "inject C=" + myEvent.getKeyCode() + ",A=" + myEvent.getAction() + ",F=" + myEvent.getFlags() + ",P=" + pid + ",U=" + uid + ",R=" + result);
                }
                if (result == 0) {
                    return true;
                }
                if (result == 1) {
                    Slog.w(TAG, "Input event injection from pid " + pid + " permission denied.");
                    throw new SecurityException("Injecting to another application requires INJECT_EVENTS permission");
                } else if (result != 3) {
                    Slog.w(TAG, "Input event injection from pid " + pid + " failed.");
                    return false;
                } else {
                    Slog.w(TAG, "Input event injection from pid " + pid + " timed out.");
                    return false;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("mode is invalid");
        }
    }

    public InputDevice getInputDevice(int deviceId) {
        synchronized (this.mInputDevicesLock) {
            int count = this.mInputDevices.length;
            for (int i = 0; i < count; i++) {
                InputDevice inputDevice = this.mInputDevices[i];
                if (inputDevice.getId() == deviceId) {
                    return inputDevice;
                }
            }
            return null;
        }
    }

    public boolean isInputDeviceEnabled(int deviceId) {
        return nativeIsInputDeviceEnabled(this.mPtr, deviceId);
    }

    public void enableInputDevice(int deviceId) {
        if (checkCallingPermission("android.permission.DISABLE_INPUT_DEVICE", "enableInputDevice()")) {
            if (DEBUG_HWFLOW) {
                Slog.i(TAG, "enableInputDevice deviceId=" + deviceId);
            }
            nativeEnableInputDevice(this.mPtr, deviceId);
            return;
        }
        throw new SecurityException("Requires DISABLE_INPUT_DEVICE permission");
    }

    public void disableInputDevice(int deviceId) {
        if (checkCallingPermission("android.permission.DISABLE_INPUT_DEVICE", "disableInputDevice()")) {
            if (DEBUG_HWFLOW) {
                Slog.i(TAG, "disableInputDevice deviceId=" + deviceId);
            }
            nativeDisableInputDevice(this.mPtr, deviceId);
            return;
        }
        throw new SecurityException("Requires DISABLE_INPUT_DEVICE permission");
    }

    public int[] getInputDeviceIds() {
        int[] ids;
        synchronized (this.mInputDevicesLock) {
            int count = this.mInputDevices.length;
            ids = new int[count];
            for (int i = 0; i < count; i++) {
                ids[i] = this.mInputDevices[i].getId();
            }
        }
        return ids;
    }

    public InputDevice[] getInputDevices() {
        InputDevice[] inputDeviceArr;
        synchronized (this.mInputDevicesLock) {
            inputDeviceArr = this.mInputDevices;
        }
        return inputDeviceArr;
    }

    public void registerInputDevicesChangedListener(IInputDevicesChangedListener listener) {
        if (listener != null) {
            synchronized (this.mInputDevicesLock) {
                int callingPid = Binder.getCallingPid();
                if (this.mInputDevicesChangedListeners.get(callingPid) == null) {
                    InputDevicesChangedListenerRecord record = new InputDevicesChangedListenerRecord(callingPid, listener);
                    try {
                        listener.asBinder().linkToDeath(record, 0);
                        this.mInputDevicesChangedListeners.put(callingPid, record);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    throw new SecurityException("The calling process has already registered an InputDevicesChangedListener.");
                }
            }
            return;
        }
        throw new IllegalArgumentException("listener must not be null");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInputDevicesChangedListenerDied(int pid) {
        synchronized (this.mInputDevicesLock) {
            this.mInputDevicesChangedListeners.remove(pid);
        }
    }

    /* access modifiers changed from: protected */
    public void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
        Throwable th;
        int numListeners;
        int[] deviceIdAndGeneration;
        int numFullKeyboardsAdded = 0;
        this.mTempInputDevicesChangedListenersToNotify.clear();
        this.mTempFullKeyboards.clear();
        synchronized (this.mInputDevicesLock) {
            try {
                if (this.mInputDevicesChangedPending) {
                    this.mInputDevicesChangedPending = false;
                    numListeners = this.mInputDevicesChangedListeners.size();
                    for (int i = 0; i < numListeners; i++) {
                        this.mTempInputDevicesChangedListenersToNotify.add(this.mInputDevicesChangedListeners.valueAt(i));
                    }
                    int numDevices = this.mInputDevices.length;
                    deviceIdAndGeneration = new int[(numDevices * 2)];
                    for (int i2 = 0; i2 < numDevices; i2++) {
                        InputDevice inputDevice = this.mInputDevices[i2];
                        deviceIdAndGeneration[i2 * 2] = inputDevice.getId();
                        deviceIdAndGeneration[(i2 * 2) + 1] = inputDevice.getGeneration();
                        if (!inputDevice.isVirtual() && inputDevice.isFullKeyboard()) {
                            if (!containsInputDeviceWithDescriptor(oldInputDevices, inputDevice.getDescriptor())) {
                                int numFullKeyboardsAdded2 = numFullKeyboardsAdded + 1;
                                try {
                                    this.mTempFullKeyboards.add(numFullKeyboardsAdded, inputDevice);
                                    numFullKeyboardsAdded = numFullKeyboardsAdded2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            } else {
                                this.mTempFullKeyboards.add(inputDevice);
                            }
                        }
                    }
                } else {
                    return;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        for (int i3 = 0; i3 < numListeners; i3++) {
            this.mTempInputDevicesChangedListenersToNotify.get(i3).notifyInputDevicesChanged(deviceIdAndGeneration);
        }
        this.mTempInputDevicesChangedListenersToNotify.clear();
        List<InputDevice> keyboardsMissingLayout = new ArrayList<>();
        int numFullKeyboards = this.mTempFullKeyboards.size();
        synchronized (this.mDataStore) {
            for (int i4 = 0; i4 < numFullKeyboards; i4++) {
                InputDevice inputDevice2 = this.mTempFullKeyboards.get(i4);
                String layout = getCurrentKeyboardLayoutForInputDevice(inputDevice2.getIdentifier());
                if (layout == null && (layout = getDefaultKeyboardLayout(inputDevice2, true)) != null) {
                    setCurrentKeyboardLayoutForInputDevice(inputDevice2.getIdentifier(), layout);
                }
                if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT) {
                    layout = updateLayoutWithAuto(inputDevice2, layout, this.mDataStore);
                }
                if (layout == null) {
                    keyboardsMissingLayout.add(inputDevice2);
                }
            }
        }
        if (this.mNotificationManager != null) {
            boolean isChinese = "zh".equals(this.mContext.getResources().getConfiguration().locale.getLanguage());
            if (keyboardsMissingLayout.isEmpty() || isChinese) {
                if (this.mKeyboardLayoutNotificationShown) {
                    hideMissingKeyboardLayoutNotification();
                }
            } else if (keyboardsMissingLayout.size() > 1) {
                showMissingKeyboardLayoutNotification(null);
            } else {
                showMissingKeyboardLayoutNotification(keyboardsMissingLayout.get(0));
            }
        }
        this.mTempFullKeyboards.clear();
    }

    private String updateLayoutWithAuto(InputDevice inputDevice, String layout, PersistentDataStore dataStore) {
        String autoLayout = getDefaultKeyboardLayout(inputDevice, false);
        InputDeviceIdentifier identifier = inputDevice.getIdentifier();
        String key = getLayoutDescriptor(identifier);
        if (!dataStore.hasAddedEnLayout(key) && autoLayout != null && !isEnLocale()) {
            addKeyboardLayoutForInputDevice(identifier, getDefaultEnLayout());
            dataStore.setHasAddedEnLayout(key, true);
        }
        if (layout == null || "keyboard_layout_auto".equals(layout)) {
            setCurrentKeyboardLayoutForInputDevice(identifier, "keyboard_layout_auto");
            setAutoKeyboardLayoutForInputDevice(identifier, autoLayout);
            return autoLayout;
        }
        addKeyboardLayoutForInputDevice(identifier, "keyboard_layout_auto");
        dataStore.setAutoKeyboardLayout(key, autoLayout);
        return layout;
    }

    private String getAutoKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier) {
        String layout;
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            layout = this.mDataStore.getAutoKeyboardLayout(key);
            if (layout == null && !key.equals(identifier.getDescriptor())) {
                layout = this.mDataStore.getAutoKeyboardLayout(identifier.getDescriptor());
            }
            Slog.d(TAG, "Loaded auto keyboard layout id for " + key + " and got " + layout);
        }
        return layout;
    }

    private void setAutoKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            try {
                if (this.mDataStore.setAutoKeyboardLayout(key, keyboardLayoutDescriptor)) {
                    Slog.d(TAG, "set auto keyboard layout using " + key);
                    this.mHandler.sendEmptyMessage(3);
                }
                this.mDataStore.saveIfNeeded();
            } catch (Throwable th) {
                this.mDataStore.saveIfNeeded();
                throw th;
            }
        }
    }

    private boolean isEnLocale() {
        return "en".equals(this.mContext.getResources().getConfiguration().locale.getLanguage());
    }

    private String getDefaultEnLayout() {
        Locale systemLocale = this.mContext.getResources().getConfiguration().locale;
        for (String country : this.mCountryUS) {
            if (systemLocale.getCountry().equals(country)) {
                return DEFAULT_EN_LAYOUT_US;
            }
        }
        for (String language : this.mLanguageUS) {
            if (systemLocale.getLanguage().equals(language)) {
                return DEFAULT_EN_LAYOUT_US;
            }
        }
        return DEFAULT_EN_LAYOUT_UK;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void rsetHasAddedEnLayout() {
        synchronized (this.mDataStore) {
            for (InputDevice inputDevice : getInputDevices()) {
                this.mDataStore.setHasAddedEnLayout(getLayoutDescriptor(inputDevice.getIdentifier()), false);
            }
        }
    }

    private String getDefaultKeyboardLayout(final InputDevice d, final boolean isAccurate) {
        final Locale systemLocale = this.mContext.getResources().getConfiguration().locale;
        if (TextUtils.isEmpty(systemLocale.getLanguage())) {
            return null;
        }
        final List<KeyboardLayout> layouts = new ArrayList<>();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            /* class com.android.server.input.InputManagerService.AnonymousClass5 */

            @Override // com.android.server.input.InputManagerService.KeyboardLayoutVisitor
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                if (!isAccurate || (layout.getVendorId() == d.getVendorId() && layout.getProductId() == d.getProductId())) {
                    LocaleList locales = layout.getLocales();
                    int numLocales = locales.size();
                    for (int localeIndex = 0; localeIndex < numLocales; localeIndex++) {
                        if (InputManagerService.isCompatibleLocale(systemLocale, locales.get(localeIndex))) {
                            layouts.add(layout);
                            return;
                        }
                    }
                }
            }
        });
        if (layouts.isEmpty()) {
            return null;
        }
        Collections.sort(layouts);
        int N = layouts.size();
        for (int i = 0; i < N; i++) {
            KeyboardLayout layout = layouts.get(i);
            LocaleList locales = layout.getLocales();
            int numLocales = locales.size();
            for (int localeIndex = 0; localeIndex < numLocales; localeIndex++) {
                Locale locale = locales.get(localeIndex);
                if (locale.getCountry().equals(systemLocale.getCountry()) && locale.getVariant().equals(systemLocale.getVariant())) {
                    return layout.getDescriptor();
                }
            }
        }
        for (int i2 = 0; i2 < N; i2++) {
            KeyboardLayout layout2 = layouts.get(i2);
            LocaleList locales2 = layout2.getLocales();
            int numLocales2 = locales2.size();
            for (int localeIndex2 = 0; localeIndex2 < numLocales2; localeIndex2++) {
                if (locales2.get(localeIndex2).getCountry().equals(systemLocale.getCountry())) {
                    return layout2.getDescriptor();
                }
            }
        }
        return layouts.get(0).getDescriptor();
    }

    /* access modifiers changed from: private */
    public static boolean isCompatibleLocale(Locale systemLocale, Locale keyboardLocale) {
        if (!systemLocale.getLanguage().equals(keyboardLocale.getLanguage())) {
            return false;
        }
        if (TextUtils.isEmpty(systemLocale.getCountry()) || TextUtils.isEmpty(keyboardLocale.getCountry()) || systemLocale.getCountry().equals(keyboardLocale.getCountry())) {
            return true;
        }
        return false;
    }

    public TouchCalibration getTouchCalibrationForInputDevice(String inputDeviceDescriptor, int surfaceRotation) {
        TouchCalibration touchCalibration;
        if (inputDeviceDescriptor != null) {
            synchronized (this.mDataStore) {
                touchCalibration = this.mDataStore.getTouchCalibration(inputDeviceDescriptor, surfaceRotation);
            }
            return touchCalibration;
        }
        throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
    }

    public void setTouchCalibrationForInputDevice(String inputDeviceDescriptor, int surfaceRotation, TouchCalibration calibration) {
        if (!checkCallingPermission("android.permission.SET_INPUT_CALIBRATION", "setTouchCalibrationForInputDevice()")) {
            throw new SecurityException("Requires SET_INPUT_CALIBRATION permission");
        } else if (inputDeviceDescriptor == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        } else if (calibration == null) {
            throw new IllegalArgumentException("calibration must not be null");
        } else if (surfaceRotation < 0 || surfaceRotation > 3) {
            throw new IllegalArgumentException("surfaceRotation value out of bounds");
        } else {
            synchronized (this.mDataStore) {
                try {
                    if (this.mDataStore.setTouchCalibration(inputDeviceDescriptor, surfaceRotation, calibration)) {
                        nativeReloadCalibration(this.mPtr);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                    throw th;
                }
            }
        }
    }

    public int isInTabletMode() {
        if (checkCallingPermission("android.permission.TABLET_MODE", "isInTabletMode()")) {
            return getSwitchState(-1, -256, 1);
        }
        throw new SecurityException("Requires TABLET_MODE permission");
    }

    public void registerTabletModeChangedListener(ITabletModeChangedListener listener) {
        if (!checkCallingPermission("android.permission.TABLET_MODE", "registerTabletModeChangedListener()")) {
            throw new SecurityException("Requires TABLET_MODE_LISTENER permission");
        } else if (listener != null) {
            synchronized (this.mTabletModeLock) {
                int callingPid = Binder.getCallingPid();
                if (this.mTabletModeChangedListeners.get(callingPid) == null) {
                    TabletModeChangedListenerRecord record = new TabletModeChangedListenerRecord(callingPid, listener);
                    try {
                        listener.asBinder().linkToDeath(record, 0);
                        this.mTabletModeChangedListeners.put(callingPid, record);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    throw new IllegalStateException("The calling process has already registered a TabletModeChangedListener.");
                }
            }
        } else {
            throw new IllegalArgumentException("listener must not be null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTabletModeChangedListenerDied(int pid) {
        synchronized (this.mTabletModeLock) {
            this.mTabletModeChangedListeners.remove(pid);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deliverTabletModeChanged(long whenNanos, boolean inTabletMode) {
        int numListeners;
        this.mTempTabletModeChangedListenersToNotify.clear();
        synchronized (this.mTabletModeLock) {
            numListeners = this.mTabletModeChangedListeners.size();
            for (int i = 0; i < numListeners; i++) {
                this.mTempTabletModeChangedListenersToNotify.add(this.mTabletModeChangedListeners.valueAt(i));
            }
        }
        for (int i2 = 0; i2 < numListeners; i2++) {
            this.mTempTabletModeChangedListenersToNotify.get(i2).notifyTabletModeChanged(whenNanos, inTabletMode);
        }
    }

    private void showMissingKeyboardLayoutNotification(InputDevice device) {
        if (!this.mKeyboardLayoutNotificationShown) {
            Intent intent = new Intent("android.settings.HARD_KEYBOARD_SETTINGS");
            if (device != null) {
                intent.putExtra("input_device_identifier", (Parcelable) device.getIdentifier());
            }
            intent.setFlags(337641472);
            PendingIntent keyboardLayoutIntent = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            Resources r = this.mContext.getResources();
            this.mNotificationManager.notifyAsUser(null, 19, new Notification.Builder(this.mContext, SystemNotificationChannels.PHYSICAL_KEYBOARD).setContentTitle(r.getString(17041187)).setContentText(r.getString(17041186)).setContentIntent(keyboardLayoutIntent).setSmallIcon(17302810).setColor(this.mContext.getColor(17170460)).build(), UserHandle.ALL);
            this.mKeyboardLayoutNotificationShown = true;
        }
    }

    private void hideMissingKeyboardLayoutNotification() {
        if (this.mKeyboardLayoutNotificationShown) {
            this.mKeyboardLayoutNotificationShown = false;
            this.mNotificationManager.cancelAsUser(null, 19, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateKeyboardLayouts() {
        final HashSet<String> availableKeyboardLayouts = new HashSet<>();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            /* class com.android.server.input.InputManagerService.AnonymousClass6 */

            @Override // com.android.server.input.InputManagerService.KeyboardLayoutVisitor
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                availableKeyboardLayouts.add(layout.getDescriptor());
            }
        });
        synchronized (this.mDataStore) {
            try {
                this.mDataStore.removeUninstalledKeyboardLayouts(availableKeyboardLayouts);
            } finally {
                this.mDataStore.saveIfNeeded();
            }
        }
        reloadKeyboardLayouts();
    }

    private static boolean containsInputDeviceWithDescriptor(InputDevice[] inputDevices, String descriptor) {
        for (InputDevice inputDevice : inputDevices) {
            if (inputDevice.getDescriptor().equals(descriptor)) {
                return true;
            }
        }
        return false;
    }

    public KeyboardLayout[] getKeyboardLayouts() {
        final ArrayList<KeyboardLayout> list = new ArrayList<>();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            /* class com.android.server.input.InputManagerService.AnonymousClass7 */

            @Override // com.android.server.input.InputManagerService.KeyboardLayoutVisitor
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                list.add(layout);
            }
        });
        return (KeyboardLayout[]) list.toArray(new KeyboardLayout[list.size()]);
    }

    public KeyboardLayout[] getKeyboardLayoutsForInputDevice(final InputDeviceIdentifier identifier) {
        final String[] enabledLayoutDescriptors = getEnabledKeyboardLayoutsForInputDevice(identifier);
        final ArrayList<KeyboardLayout> enabledLayouts = new ArrayList<>(enabledLayoutDescriptors.length);
        final ArrayList<KeyboardLayout> potentialLayouts = new ArrayList<>();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            /* class com.android.server.input.InputManagerService.AnonymousClass8 */
            boolean mHasSeenDeviceSpecificLayout;

            @Override // com.android.server.input.InputManagerService.KeyboardLayoutVisitor
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                String[] strArr = enabledLayoutDescriptors;
                for (String s : strArr) {
                    if (s != null && s.equals(layout.getDescriptor())) {
                        enabledLayouts.add(layout);
                        return;
                    }
                }
                if (layout.getVendorId() == identifier.getVendorId() && layout.getProductId() == identifier.getProductId()) {
                    if (!this.mHasSeenDeviceSpecificLayout) {
                        this.mHasSeenDeviceSpecificLayout = true;
                        potentialLayouts.clear();
                    }
                    potentialLayouts.add(layout);
                } else if (layout.getVendorId() == -1 && layout.getProductId() == -1 && !this.mHasSeenDeviceSpecificLayout) {
                    potentialLayouts.add(layout);
                }
            }
        });
        int enabledLayoutSize = enabledLayouts.size();
        int potentialLayoutSize = potentialLayouts.size();
        KeyboardLayout[] layouts = new KeyboardLayout[(enabledLayoutSize + potentialLayoutSize)];
        enabledLayouts.toArray(layouts);
        for (int i = 0; i < potentialLayoutSize; i++) {
            layouts[enabledLayoutSize + i] = potentialLayouts.get(i);
        }
        return layouts;
    }

    public KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor) {
        if (keyboardLayoutDescriptor != null) {
            final KeyboardLayout[] result = new KeyboardLayout[1];
            visitKeyboardLayout(keyboardLayoutDescriptor, new KeyboardLayoutVisitor() {
                /* class com.android.server.input.InputManagerService.AnonymousClass9 */

                @Override // com.android.server.input.InputManagerService.KeyboardLayoutVisitor
                public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                    result[0] = layout;
                }
            });
            if (result[0] == null) {
                Slog.w(TAG, "Could not get keyboard layout with descriptor '" + keyboardLayoutDescriptor + "'.");
            }
            return result[0];
        }
        throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
    }

    private void visitAllKeyboardLayouts(KeyboardLayoutVisitor visitor) {
        PackageManager pm = this.mContext.getPackageManager();
        for (ResolveInfo resolveInfo : pm.queryBroadcastReceivers(new Intent("android.hardware.input.action.QUERY_KEYBOARD_LAYOUTS"), 786560)) {
            visitKeyboardLayoutsInPackage(pm, resolveInfo.activityInfo, null, resolveInfo.priority, visitor);
        }
    }

    private void visitKeyboardLayout(String keyboardLayoutDescriptor, KeyboardLayoutVisitor visitor) {
        KeyboardLayoutDescriptor d = KeyboardLayoutDescriptor.parse(keyboardLayoutDescriptor);
        if (d != null) {
            PackageManager pm = this.mContext.getPackageManager();
            try {
                visitKeyboardLayoutsInPackage(pm, pm.getReceiverInfo(new ComponentName(d.packageName, d.receiverName), 786560), d.keyboardLayoutName, 0, visitor);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
    }

    private void visitKeyboardLayoutsInPackage(PackageManager pm, ActivityInfo receiver, String keyboardName, int requestedPriority, KeyboardLayoutVisitor visitor) {
        int priority;
        Exception ex;
        XmlResourceParser parser;
        Throwable th;
        int configResId;
        Bundle metaData;
        int i;
        Resources resources;
        TypedArray a;
        Throwable th2;
        String name;
        String languageTags;
        Object obj = keyboardName;
        Bundle metaData2 = receiver.metaData;
        if (metaData2 != null) {
            int configResId2 = metaData2.getInt("android.hardware.input.metadata.KEYBOARD_LAYOUTS");
            if (configResId2 == 0) {
                Slog.w(TAG, "Missing meta-data 'android.hardware.input.metadata.KEYBOARD_LAYOUTS' on receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name);
                return;
            }
            CharSequence receiverLabel = receiver.loadLabel(pm);
            String collection = receiverLabel != null ? receiverLabel.toString() : "";
            int i2 = 1;
            if ((receiver.applicationInfo.flags & 1) != 0) {
                priority = requestedPriority;
            } else {
                priority = 0;
            }
            try {
                Resources resources2 = pm.getResourcesForApplication(receiver.applicationInfo);
                XmlResourceParser parser2 = resources2.getXml(configResId2);
                try {
                    XmlUtils.beginDocument(parser2, "keyboard-layouts");
                    while (true) {
                        XmlUtils.nextElement(parser2);
                        String element = parser2.getName();
                        if (element == null) {
                            try {
                                parser2.close();
                                return;
                            } catch (Exception e) {
                                ex = e;
                                Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name, ex);
                            }
                        } else {
                            if (element.equals("keyboard-layout")) {
                                TypedArray a2 = resources2.obtainAttributes(parser2, R.styleable.KeyboardLayout);
                                try {
                                    name = a2.getString(i2);
                                    String label = a2.getString(0);
                                    int keyboardLayoutResId = a2.getResourceId(2, 0);
                                    String languageTags2 = a2.getString(3);
                                    if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT) {
                                        try {
                                            if (TextUtils.isEmpty(languageTags2)) {
                                                languageTags = XmlUtils.readStringAttribute(parser2, "hw_locale");
                                                LocaleList locales = getLocalesFromLanguageTags(languageTags);
                                                metaData = metaData2;
                                                int vid = a2.getInt(5, -1);
                                                a = a2;
                                                configResId = configResId2;
                                                int pid = a.getInt(4, -1);
                                                if (name != null || label == null) {
                                                    parser = parser2;
                                                    resources = resources2;
                                                    i = 1;
                                                } else if (keyboardLayoutResId == 0) {
                                                    parser = parser2;
                                                    resources = resources2;
                                                    i = 1;
                                                } else {
                                                    String descriptor = KeyboardLayoutDescriptor.format(receiver.packageName, receiver.name, name);
                                                    if (obj != null) {
                                                        try {
                                                            if (!name.equals(obj)) {
                                                                parser = parser2;
                                                                resources = resources2;
                                                                i = 1;
                                                                a.recycle();
                                                            }
                                                        } catch (Throwable th3) {
                                                            th2 = th3;
                                                            a.recycle();
                                                            throw th2;
                                                        }
                                                    }
                                                    try {
                                                        parser = parser2;
                                                        resources = resources2;
                                                        i = 1;
                                                    } catch (Throwable th4) {
                                                        th2 = th4;
                                                        a.recycle();
                                                        throw th2;
                                                    }
                                                    try {
                                                        visitor.visitKeyboardLayout(resources, keyboardLayoutResId, new KeyboardLayout(descriptor, label, collection, priority, locales, vid, pid));
                                                        a.recycle();
                                                    } catch (Throwable th5) {
                                                        th2 = th5;
                                                        a.recycle();
                                                        throw th2;
                                                    }
                                                }
                                                Slog.w(TAG, "Missing required 'name', 'label' or 'keyboardLayout' attributes in keyboard layout resource from receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name);
                                                a.recycle();
                                            }
                                        } catch (Throwable th6) {
                                            th2 = th6;
                                            a = a2;
                                            a.recycle();
                                            throw th2;
                                        }
                                    }
                                    languageTags = languageTags2;
                                    LocaleList locales2 = getLocalesFromLanguageTags(languageTags);
                                    metaData = metaData2;
                                    try {
                                        int vid2 = a2.getInt(5, -1);
                                        a = a2;
                                        configResId = configResId2;
                                    } catch (Throwable th7) {
                                        th2 = th7;
                                        a = a2;
                                        a.recycle();
                                        throw th2;
                                    }
                                } catch (Throwable th8) {
                                    th2 = th8;
                                    a = a2;
                                    a.recycle();
                                    throw th2;
                                }
                                try {
                                    int pid2 = a.getInt(4, -1);
                                    if (name != null) {
                                    }
                                    parser = parser2;
                                    resources = resources2;
                                    i = 1;
                                    Slog.w(TAG, "Missing required 'name', 'label' or 'keyboardLayout' attributes in keyboard layout resource from receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name);
                                    a.recycle();
                                } catch (Throwable th9) {
                                    th = th9;
                                    try {
                                        parser.close();
                                        throw th;
                                    } catch (Exception e2) {
                                        ex = e2;
                                        Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name, ex);
                                    }
                                }
                            } else {
                                metaData = metaData2;
                                configResId = configResId2;
                                parser = parser2;
                                resources = resources2;
                                i = i2;
                                Slog.w(TAG, "Skipping unrecognized element '" + element + "' in keyboard layout resource from receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name);
                            }
                            resources2 = resources;
                            i2 = i;
                            metaData2 = metaData;
                            configResId2 = configResId;
                            parser2 = parser;
                            obj = keyboardName;
                        }
                    }
                } catch (Throwable th10) {
                    th = th10;
                    parser = parser2;
                    parser.close();
                    throw th;
                }
            } catch (Exception e3) {
                ex = e3;
                Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + receiver.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + receiver.name, ex);
            }
        }
    }

    private static LocaleList getLocalesFromLanguageTags(String languageTags) {
        if (TextUtils.isEmpty(languageTags)) {
            return LocaleList.getEmptyLocaleList();
        }
        return LocaleList.forLanguageTags(languageTags.replace('|', ','));
    }

    private String getLayoutDescriptor(InputDeviceIdentifier identifier) {
        if (identifier == null || identifier.getDescriptor() == null) {
            throw new IllegalArgumentException("identifier and descriptor must not be null");
        } else if (identifier.getVendorId() == 0 && identifier.getProductId() == 0) {
            return identifier.getDescriptor();
        } else {
            return "vendor:" + identifier.getVendorId() + ",product:" + identifier.getProductId();
        }
    }

    public String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier) {
        String layout;
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            layout = this.mDataStore.getCurrentKeyboardLayout(key);
            if (layout == null && !key.equals(identifier.getDescriptor())) {
                layout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
            }
        }
        return layout;
    }

    public void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "setCurrentKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor != null) {
            String key = getLayoutDescriptor(identifier);
            synchronized (this.mDataStore) {
                try {
                    if (this.mDataStore.setCurrentKeyboardLayout(key, keyboardLayoutDescriptor)) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        String[] layouts;
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            layouts = this.mDataStore.getKeyboardLayouts(key);
            if ((layouts == null || layouts.length == 0) && !key.equals(identifier.getDescriptor())) {
                layouts = this.mDataStore.getKeyboardLayouts(identifier.getDescriptor());
            }
        }
        return layouts;
    }

    public void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "addKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor != null) {
            String key = getLayoutDescriptor(identifier);
            synchronized (this.mDataStore) {
                try {
                    String oldLayout = this.mDataStore.getCurrentKeyboardLayout(key);
                    if (oldLayout == null && !key.equals(identifier.getDescriptor())) {
                        oldLayout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
                    }
                    if (this.mDataStore.addKeyboardLayout(key, keyboardLayoutDescriptor) && !Objects.equals(oldLayout, this.mDataStore.getCurrentKeyboardLayout(key))) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "removeKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor != null) {
            String key = getLayoutDescriptor(identifier);
            synchronized (this.mDataStore) {
                try {
                    String oldLayout = this.mDataStore.getCurrentKeyboardLayout(key);
                    if (oldLayout == null && !key.equals(identifier.getDescriptor())) {
                        oldLayout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
                    }
                    boolean removed = this.mDataStore.removeKeyboardLayout(key, keyboardLayoutDescriptor);
                    if (!key.equals(identifier.getDescriptor())) {
                        removed |= this.mDataStore.removeKeyboardLayout(identifier.getDescriptor(), keyboardLayoutDescriptor);
                    }
                    if (removed && !Objects.equals(oldLayout, this.mDataStore.getCurrentKeyboardLayout(key))) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                    throw th;
                }
            }
        } else {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
    }

    public void switchKeyboardLayout(int deviceId, int direction) {
        this.mHandler.obtainMessage(2, deviceId, direction).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSwitchKeyboardLayout(int deviceId, int direction) {
        boolean changed;
        String keyboardLayoutDescriptor;
        KeyboardLayout keyboardLayout;
        InputDevice device = getInputDevice(deviceId);
        if (device != null) {
            boolean isCurrentAuto = false;
            String key = getLayoutDescriptor(device.getIdentifier());
            synchronized (this.mDataStore) {
                try {
                    changed = this.mDataStore.switchKeyboardLayout(key, direction);
                    keyboardLayoutDescriptor = this.mDataStore.getCurrentKeyboardLayout(key);
                    if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT && "keyboard_layout_auto".equals(this.mDataStore.getCurrentKeyboardLayout(key))) {
                        isCurrentAuto = true;
                        keyboardLayoutDescriptor = this.mDataStore.getAutoKeyboardLayout(key);
                    }
                } finally {
                    this.mDataStore.saveIfNeeded();
                }
            }
            if (changed) {
                String label = null;
                if (!(keyboardLayoutDescriptor == null || (keyboardLayout = getKeyboardLayout(keyboardLayoutDescriptor)) == null)) {
                    label = keyboardLayout.getLabel();
                }
                this.mHwIMSEx.showSwitchedKeyboardLayoutToast(label, isCurrentAuto);
                reloadKeyboardLayouts();
            }
        }
    }

    public void setFocusedApplication(int displayId, InputApplicationHandle application) {
        nativeSetFocusedApplication(this.mPtr, displayId, application);
    }

    public void setFocusedDisplay(int displayId) {
        nativeSetFocusedDisplay(this.mPtr, displayId);
    }

    public void onDisplayRemoved(int displayId) {
        nativeSetInputWindows(this.mPtr, null, displayId);
    }

    public void requestPointerCapture(IBinder windowToken, boolean enabled) {
        IWindow iWindow = this.mFocusedWindow;
        if (iWindow == null || iWindow.asBinder() != windowToken) {
            Slog.e(TAG, "requestPointerCapture called for a window that has no focus: " + windowToken);
        } else if (this.mFocusedWindowHasCapture == enabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("requestPointerCapture: already ");
            sb.append(enabled ? "enabled" : "disabled");
            Slog.i(TAG, sb.toString());
        } else {
            setPointerCapture(enabled);
        }
    }

    private void setPointerCapture(boolean enabled) {
        if (this.mFocusedWindowHasCapture != enabled) {
            this.mFocusedWindowHasCapture = enabled;
            try {
                this.mFocusedWindow.dispatchPointerCaptureChanged(enabled);
            } catch (RemoteException e) {
            }
            nativeSetPointerCapture(this.mPtr, enabled);
        }
    }

    public void setInputDispatchMode(boolean enabled, boolean frozen) {
        nativeSetInputDispatchMode(this.mPtr, enabled, frozen);
    }

    public void setSystemUiVisibility(int visibility) {
        nativeSetSystemUiVisibility(this.mPtr, visibility);
    }

    public void tryPointerSpeed(int speed) {
        if (!checkCallingPermission("android.permission.SET_POINTER_SPEED", "tryPointerSpeed()")) {
            throw new SecurityException("Requires SET_POINTER_SPEED permission");
        } else if (speed < -7 || speed > 7) {
            throw new IllegalArgumentException("speed out of range");
        } else {
            setPointerSpeedUnchecked(speed);
        }
    }

    public void updatePointerSpeedFromSettings() {
        setPointerSpeedUnchecked(getPointerSpeedSetting());
    }

    private void setPointerSpeedUnchecked(int speed) {
        nativeSetPointerSpeed(this.mPtr, Math.min(Math.max(speed, -7), 7));
    }

    private void registerPointerSpeedSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("pointer_speed"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.input.InputManagerService.AnonymousClass10 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                InputManagerService.this.updatePointerSpeedFromSettings();
            }
        }, -1);
    }

    private int getPointerSpeedSetting() {
        try {
            return Settings.System.getIntForUser(this.mContext.getContentResolver(), "pointer_speed", -2);
        } catch (Settings.SettingNotFoundException e) {
            return 0;
        }
    }

    public void updateShowTouchesFromSettings() {
        boolean z = false;
        int setting = getShowTouchesSetting(0);
        long j = this.mPtr;
        if (setting != 0) {
            z = true;
        }
        nativeSetShowTouches(j, z);
    }

    private void registerShowTouchesSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("show_touches"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.input.InputManagerService.AnonymousClass11 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                InputManagerService.this.updateShowTouchesFromSettings();
            }
        }, -1);
    }

    public void updateAccessibilityLargePointerFromSettings() {
        boolean z = false;
        int accessibilityConfig = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_large_pointer_icon", 0, -2);
        PointerIcon.setUseLargeIcons(accessibilityConfig == 1);
        if (HwPCUtils.isPcCastModeInServer()) {
            nativeReloadPointerIcons(this.mPtr, getExternalContext());
            StringBuilder sb = new StringBuilder();
            sb.append("update Pointer, isDefaultDisplay:");
            if (getExternalContext() == null) {
                z = true;
            }
            sb.append(z);
            sb.append(", accessibilityConfig:");
            sb.append(accessibilityConfig);
            Slog.i(TAG, sb.toString());
            return;
        }
        nativeReloadPointerIcons(this.mPtr, null);
    }

    private void registerAccessibilityLargePointerSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("accessibility_large_pointer_icon"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.input.InputManagerService.AnonymousClass12 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                InputManagerService.this.updateAccessibilityLargePointerFromSettings();
            }
        }, -1);
    }

    private int getShowTouchesSetting(int defaultValue) {
        try {
            return Settings.System.getIntForUser(this.mContext.getContentResolver(), "show_touches", -2);
        } catch (Settings.SettingNotFoundException e) {
            return defaultValue;
        }
    }

    public void vibrate(int deviceId, long[] pattern, int repeat, IBinder token) {
        VibratorToken v;
        if (repeat < pattern.length) {
            synchronized (this.mVibratorLock) {
                v = this.mVibratorTokens.get(token);
                if (v == null) {
                    int i = this.mNextVibratorTokenValue;
                    this.mNextVibratorTokenValue = i + 1;
                    v = new VibratorToken(deviceId, token, i);
                    try {
                        token.linkToDeath(v, 0);
                        this.mVibratorTokens.put(token, v);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            synchronized (v) {
                v.mVibrating = true;
                nativeVibrate(this.mPtr, deviceId, pattern, repeat, v.mTokenValue);
            }
            return;
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public void cancelVibrate(int deviceId, IBinder token) {
        synchronized (this.mVibratorLock) {
            VibratorToken v = this.mVibratorTokens.get(token);
            if (v != null) {
                if (v.mDeviceId == deviceId) {
                    cancelVibrateIfNeeded(v);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onVibratorTokenDied(VibratorToken v) {
        synchronized (this.mVibratorLock) {
            this.mVibratorTokens.remove(v.mToken);
        }
        cancelVibrateIfNeeded(v);
    }

    private void cancelVibrateIfNeeded(VibratorToken v) {
        synchronized (v) {
            if (v.mVibrating) {
                nativeCancelVibrate(this.mPtr, v.mDeviceId, v.mTokenValue);
                v.mVibrating = false;
            }
        }
    }

    public void setPointerIconType(int iconId) {
        nativeSetPointerIconType(this.mPtr, iconId);
    }

    public void setCustomPointerIcon(PointerIcon icon) {
        Preconditions.checkNotNull(icon);
        nativeSetCustomPointerIcon(this.mPtr, icon);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("INPUT MANAGER (dumpsys input)\n");
            String dumpStr = nativeDump(this.mPtr);
            if (dumpStr != null) {
                pw.println(dumpStr);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mInputFilterLock) {
        }
        nativeMonitor(this.mPtr);
    }

    private void notifyConfigurationChanged(long whenNanos) {
        this.mWindowManagerCallbacks.notifyConfigurationChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyInputDevicesChanged(InputDevice[] inputDevices) {
        synchronized (this.mInputDevicesLock) {
            if (!this.mInputDevicesChangedPending) {
                this.mInputDevicesChangedPending = true;
                this.mHandler.obtainMessage(1, this.mInputDevices).sendToTarget();
            }
            this.mInputDevices = inputDevices;
        }
    }

    private void notifySwitch(long whenNanos, int switchValues, int switchMask) {
        boolean z = false;
        if ((switchMask & 1) != 0) {
            this.mWindowManagerCallbacks.notifyLidSwitchChanged(whenNanos, (switchValues & 1) == 0);
        }
        if ((switchMask & 512) != 0) {
            this.mWindowManagerCallbacks.notifyCameraLensCoverSwitchChanged(whenNanos, (switchValues & 512) != 0);
        }
        if (this.mUseDevInputEventForAudioJack && (switchMask & SW_JACK_BITS) != 0) {
            this.mWiredAccessoryCallbacks.notifyWiredAccessoryChanged(whenNanos, switchValues, switchMask);
        }
        if ((switchMask & 2) != 0) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = (int) (-1 & whenNanos);
            args.argi2 = (int) (whenNanos >> 32);
            if ((switchValues & 2) != 0) {
                z = true;
            }
            args.arg1 = Boolean.valueOf(z);
            this.mHandler.obtainMessage(6, args).sendToTarget();
        }
    }

    private void notifyInputChannelBroken(IBinder token) {
        this.mWindowManagerCallbacks.notifyInputChannelBroken(token);
    }

    private void notifyFocusChanged(IBinder oldToken, IBinder newToken) {
        IWindow iWindow = this.mFocusedWindow;
        if (iWindow != null) {
            if (iWindow.asBinder() == newToken) {
                Slog.w(TAG, "notifyFocusChanged called with unchanged mFocusedWindow=" + this.mFocusedWindow);
                return;
            }
            setPointerCapture(false);
        }
        this.mFocusedWindow = IWindow.Stub.asInterface(newToken);
    }

    private long notifyANR(IBinder token, String reason) {
        return this.mWindowManagerCallbacks.notifyANR(token, reason);
    }

    /* access modifiers changed from: package-private */
    public boolean filterInputEvent(InputEvent event, int policyFlags) {
        synchronized (this.mInputFilterLock) {
            if (this.mInputFilter != null) {
                try {
                    this.mInputFilter.filterInputEvent(event, policyFlags);
                } catch (RemoteException e) {
                }
                return false;
            }
            event.recycle();
            return true;
        }
    }

    private int interceptKeyBeforeQueueing(KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptKeyBeforeQueueing(event, policyFlags);
    }

    private int interceptMotionBeforeQueueingNonInteractive(int displayId, long whenNanos, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptMotionBeforeQueueingNonInteractive(displayId, whenNanos, policyFlags);
    }

    /* access modifiers changed from: protected */
    public long interceptKeyBeforeDispatching(IBinder focus, KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptKeyBeforeDispatching(focus, event, policyFlags);
    }

    /* access modifiers changed from: package-private */
    public KeyEvent dispatchUnhandledKey(IBinder focus, KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.dispatchUnhandledKey(focus, event, policyFlags);
    }

    private boolean checkInjectEventsPermission(int injectorPid, int injectorUid) {
        return this.mContext.checkPermission("android.permission.INJECT_EVENTS", injectorPid, injectorUid) == 0 || this.mContext.checkPermission(PERMISSION_HW_INJECT_EVENTS, injectorPid, injectorUid) == 0;
    }

    private void onPointerDownOutsideFocus(IBinder touchedToken) {
        this.mWindowManagerCallbacks.onPointerDownOutsideFocus(touchedToken);
    }

    private int getVirtualKeyQuietTimeMillis() {
        return this.mContext.getResources().getInteger(17694908);
    }

    private static String[] getExcludedDeviceNames() {
        List<String> names = new ArrayList<>();
        for (File baseDir : new File[]{Environment.getRootDirectory(), Environment.getVendorDirectory()}) {
            File confFile = new File(baseDir, EXCLUDED_DEVICES_PATH);
            try {
                names.addAll(ConfigurationProcessor.processExcludedDeviceNames(new FileInputStream(confFile)));
            } catch (FileNotFoundException e) {
            } catch (Exception e2) {
                Slog.e(TAG, "Could not parse '" + confFile.getAbsolutePath() + "'", e2);
            }
        }
        return (String[]) names.toArray(new String[0]);
    }

    private static <T> List<T> flatten(List<Pair<T, T>> pairs) {
        ArrayList arrayList = new ArrayList(pairs.size() * 2);
        for (Pair<T, T> pair : pairs) {
            arrayList.add(pair.first);
            arrayList.add(pair.second);
        }
        return arrayList;
    }

    private static String[] getInputPortAssociations() {
        File confFile = new File(Environment.getVendorDirectory(), PORT_ASSOCIATIONS_PATH);
        try {
            return (String[]) flatten(ConfigurationProcessor.processInputPortAssociations(new FileInputStream(confFile))).toArray(new String[0]);
        } catch (FileNotFoundException e) {
            return new String[0];
        } catch (Exception e2) {
            Slog.e(TAG, "Could not parse '" + confFile.getAbsolutePath() + "'", e2);
            return new String[0];
        }
    }

    public boolean canDispatchToDisplay(int deviceId, int displayId) {
        return nativeCanDispatchToDisplay(this.mPtr, deviceId, displayId);
    }

    private int getKeyRepeatTimeout() {
        return ViewConfiguration.getKeyRepeatTimeout();
    }

    private int getKeyRepeatDelay() {
        return ViewConfiguration.getKeyRepeatDelay();
    }

    private int getHoverTapTimeout() {
        return ViewConfiguration.getHoverTapTimeout();
    }

    private int getHoverTapSlop() {
        return ViewConfiguration.getHoverTapSlop();
    }

    private int getDoubleTapTimeout() {
        return ViewConfiguration.getDoubleTapTimeout();
    }

    private int getLongPressTimeout() {
        return ViewConfiguration.getLongPressTimeout();
    }

    private int getPointerLayer() {
        return this.mWindowManagerCallbacks.getPointerLayer();
    }

    /* access modifiers changed from: protected */
    public Context getExternalContext() {
        return null;
    }

    private PointerIcon getPointerIcon(int displayId) {
        return PointerIcon.getDefaultIcon(getContextForDisplay(displayId));
    }

    private Context getContextForDisplay(int displayId) {
        Context context = this.mDisplayContext;
        if (context != null && context.getDisplay().getDisplayId() == displayId) {
            return this.mDisplayContext;
        }
        if (this.mContext.getDisplay().getDisplayId() == displayId) {
            this.mDisplayContext = this.mContext;
            return this.mDisplayContext;
        }
        Display display = ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(displayId);
        if (display == null) {
            Slog.w(TAG, "display can not be NULL, return. displayId:" + displayId);
            return this.mDisplayContext;
        }
        this.mDisplayContext = this.mContext.createDisplayContext(display);
        return this.mDisplayContext;
    }

    private int getPointerDisplayId() {
        return this.mWindowManagerCallbacks.getPointerDisplayId();
    }

    private String[] getKeyboardLayoutOverlay(InputDeviceIdentifier identifier) {
        if (!this.mSystemReady) {
            return null;
        }
        String keyboardLayoutDescriptor = getCurrentKeyboardLayoutForInputDevice(identifier);
        if (IS_SUPPORT_AUTO_KEYBOARD_LAYOUT && (keyboardLayoutDescriptor == null || "keyboard_layout_auto".equals(keyboardLayoutDescriptor))) {
            keyboardLayoutDescriptor = getAutoKeyboardLayoutForInputDevice(identifier);
        }
        if (keyboardLayoutDescriptor == null) {
            return null;
        }
        final String[] result = new String[2];
        visitKeyboardLayout(keyboardLayoutDescriptor, new KeyboardLayoutVisitor() {
            /* class com.android.server.input.InputManagerService.AnonymousClass13 */

            @Override // com.android.server.input.InputManagerService.KeyboardLayoutVisitor
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                try {
                    result[0] = layout.getDescriptor();
                    result[1] = Streams.readFully(new InputStreamReader(resources.openRawResource(keyboardLayoutResId)));
                } catch (Resources.NotFoundException | IOException e) {
                }
            }
        });
        if (result[0] != null) {
            return result;
        }
        Slog.w(TAG, "Could not get keyboard layout with descriptor '" + keyboardLayoutDescriptor + "'.");
        return null;
    }

    private String getDeviceAlias(String uniqueId) {
        return BluetoothAdapter.checkBluetoothAddress(uniqueId) ? null : null;
    }

    private void reportBbFilterPoint(String eventMsg, int eventID) {
        Flog.bdReport(this.mContext, eventID, eventMsg);
    }

    public void notifyNativeEvent(int eventType, int eventValue, int keyAction, int pid, int uid) {
        IHwInputManagerServiceEx iHwInputManagerServiceEx = this.mHwIMSEx;
        if (iHwInputManagerServiceEx != null) {
            iHwInputManagerServiceEx.notifyNativeEvent(eventType, eventValue, keyAction, pid, uid);
        }
    }

    /* access modifiers changed from: private */
    public final class InputManagerHandler extends Handler {
        public InputManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    InputManagerService.this.deliverInputDevicesChanged((InputDevice[]) msg.obj);
                    return;
                case 2:
                    InputManagerService.this.handleSwitchKeyboardLayout(msg.arg1, msg.arg2);
                    return;
                case 3:
                    InputManagerService.this.reloadKeyboardLayouts();
                    return;
                case 4:
                    InputManagerService.this.updateKeyboardLayouts();
                    return;
                case 5:
                    InputManagerService.this.reloadDeviceAliases();
                    return;
                case 6:
                    SomeArgs args = (SomeArgs) msg.obj;
                    InputManagerService.this.deliverTabletModeChanged((((long) args.argi1) & 4294967295L) | (((long) args.argi2) << 32), ((Boolean) args.arg1).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    private final class InputFilterHost extends IInputFilterHost.Stub {
        private boolean mDisconnected;

        private InputFilterHost() {
        }

        public void disconnectLocked() {
            this.mDisconnected = true;
        }

        public void sendInputEvent(InputEvent event, int policyFlags) {
            if (event != null) {
                synchronized (InputManagerService.this.mInputFilterLock) {
                    if (!this.mDisconnected) {
                        InputManagerService.nativeInjectInputEvent(InputManagerService.this.mPtr, event, 0, 0, 0, 0, policyFlags | DumpState.DUMP_HANDLE);
                    }
                }
                return;
            }
            throw new IllegalArgumentException("event must not be null");
        }
    }

    private final class InputMonitorHost extends IInputMonitorHost.Stub {
        private final InputChannel mInputChannel;

        InputMonitorHost(InputChannel channel) {
            this.mInputChannel = channel;
        }

        public void pilferPointers() {
            InputManagerService.nativePilferPointers(InputManagerService.this.mPtr, asBinder());
        }

        public void dispose() {
            InputManagerService.nativeUnregisterInputChannel(InputManagerService.this.mPtr, this.mInputChannel);
            this.mInputChannel.dispose();
            Log.i(InputManagerService.TAG, "dispose server inputChannel");
        }
    }

    /* access modifiers changed from: private */
    public static final class KeyboardLayoutDescriptor {
        public String keyboardLayoutName;
        public String packageName;
        public String receiverName;

        private KeyboardLayoutDescriptor() {
        }

        public static String format(String packageName2, String receiverName2, String keyboardName) {
            return packageName2 + SliceClientPermissions.SliceAuthority.DELIMITER + receiverName2 + SliceClientPermissions.SliceAuthority.DELIMITER + keyboardName;
        }

        public static KeyboardLayoutDescriptor parse(String descriptor) {
            int pos2;
            int pos = descriptor.indexOf(47);
            if (pos < 0 || pos + 1 == descriptor.length() || (pos2 = descriptor.indexOf(47, pos + 1)) < pos + 2 || pos2 + 1 == descriptor.length()) {
                return null;
            }
            KeyboardLayoutDescriptor result = new KeyboardLayoutDescriptor();
            result.packageName = descriptor.substring(0, pos);
            result.receiverName = descriptor.substring(pos + 1, pos2);
            result.keyboardLayoutName = descriptor.substring(pos2 + 1);
            return result;
        }
    }

    /* access modifiers changed from: private */
    public final class InputDevicesChangedListenerRecord implements IBinder.DeathRecipient {
        private final IInputDevicesChangedListener mListener;
        private final int mPid;

        public InputDevicesChangedListenerRecord(int pid, IInputDevicesChangedListener listener) {
            this.mPid = pid;
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            InputManagerService.this.onInputDevicesChangedListenerDied(this.mPid);
        }

        public void notifyInputDevicesChanged(int[] info) {
            try {
                this.mListener.onInputDevicesChanged(info);
            } catch (RemoteException ex) {
                Slog.w(InputManagerService.TAG, "Failed to notify process " + this.mPid + " that input devices changed, assuming it died.", ex);
                binderDied();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class TabletModeChangedListenerRecord implements IBinder.DeathRecipient {
        private final ITabletModeChangedListener mListener;
        private final int mPid;

        public TabletModeChangedListenerRecord(int pid, ITabletModeChangedListener listener) {
            this.mPid = pid;
            this.mListener = listener;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            InputManagerService.this.onTabletModeChangedListenerDied(this.mPid);
        }

        public void notifyTabletModeChanged(long whenNanos, boolean inTabletMode) {
            try {
                this.mListener.onTabletModeChanged(whenNanos, inTabletMode);
            } catch (RemoteException ex) {
                Slog.w(InputManagerService.TAG, "Failed to notify process " + this.mPid + " that tablet mode changed, assuming it died.", ex);
                binderDied();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class VibratorToken implements IBinder.DeathRecipient {
        public final int mDeviceId;
        public final IBinder mToken;
        public final int mTokenValue;
        public boolean mVibrating;

        public VibratorToken(int deviceId, IBinder token, int tokenValue) {
            this.mDeviceId = deviceId;
            this.mToken = token;
            this.mTokenValue = tokenValue;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            InputManagerService.this.onVibratorTokenDied(this);
        }
    }

    private final class LocalService extends InputManagerInternal {
        private LocalService() {
        }

        public void setDisplayViewports(List<DisplayViewport> viewports) {
            InputManagerService.this.setDisplayViewportsInternal(viewports);
        }

        public boolean injectInputEvent(InputEvent event, int mode) {
            return InputManagerService.this.injectInputEventInternal(event, mode);
        }

        public void setInteractive(boolean interactive) {
            InputManagerService.nativeSetInteractive(InputManagerService.this.mPtr, interactive);
        }

        public void toggleCapsLock(int deviceId) {
            InputManagerService.nativeToggleCapsLock(InputManagerService.this.mPtr, deviceId);
        }

        public void setPulseGestureEnabled(boolean enabled) {
            if (InputManagerService.this.mDoubleTouchGestureEnableFile != null) {
                FileWriter writer = null;
                try {
                    writer = new FileWriter(InputManagerService.this.mDoubleTouchGestureEnableFile);
                    writer.write(enabled ? "1" : "0");
                } catch (IOException e) {
                    Log.wtf(InputManagerService.TAG, "Unable to setPulseGestureEnabled", e);
                } catch (Throwable th) {
                    IoUtils.closeQuietly((AutoCloseable) null);
                    throw th;
                }
                IoUtils.closeQuietly(writer);
            }
        }

        public void setDisplayMode(int displayMode, int subWidth, int mainWidth, int fullHeight) {
            InputManagerService.nativeSetDisplayMode(InputManagerService.this.mPtr, displayMode, subWidth, mainWidth, fullHeight);
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.input.InputManagerService$HwInnerInputManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public class HwInnerInputManagerService extends IHwInputManager.Stub {
        InputManagerService mIms;

        HwInnerInputManagerService(InputManagerService ims) {
            this.mIms = ims;
        }

        public String runHwTHPCommand(String command, String parameter) {
            if (InputManagerService.this.mHwIMSEx != null) {
                return InputManagerService.this.mHwIMSEx.runHwTHPCommand(command, parameter);
            }
            Slog.i(InputManagerService.TAG, "mHwIMSEx is null");
            return null;
        }

        public void registerListener(IHwTHPEventListener listener, IBinder iBinder) {
            if (InputManagerService.this.mHwIMSEx == null) {
                Slog.i(InputManagerService.TAG, "mHwIMSEx is null");
            } else {
                InputManagerService.this.mHwIMSEx.registerListener(listener, iBinder);
            }
        }

        public void unregisterListener(IHwTHPEventListener listener, IBinder iBinder) {
            if (InputManagerService.this.mHwIMSEx == null) {
                Slog.i(InputManagerService.TAG, "mHwIMSEx is null");
            } else {
                InputManagerService.this.mHwIMSEx.unregisterListener(listener, iBinder);
            }
        }

        public void setInputEventStrategy(boolean isStartInputEventControl) {
            this.mIms.setInputEventStrategy(isStartInputEventControl);
        }

        public String runSideTouchCommand(String command, String parameter) {
            if (InputManagerService.this.mHwIMSEx != null) {
                return InputManagerService.this.mHwIMSEx.runSideTouchCommand(command, parameter);
            }
            Slog.i(InputManagerService.TAG, "mHwIMSEx is null");
            return null;
        }

        public int setTouchscreenFeatureConfig(int feature, String config) {
            if (InputManagerService.this.mHwIMSEx != null) {
                return InputManagerService.this.mHwIMSEx.setTouchscreenFeatureConfig(feature, config);
            }
            Slog.i(InputManagerService.TAG, "mHwIMSEx is null");
            return -2;
        }

        public int[] setTPCommand(int type, Bundle bundle) {
            if (InputManagerService.this.mHwIMSEx != null) {
                return InputManagerService.this.mHwIMSEx.setTPCommand(type, bundle);
            }
            Slog.i(InputManagerService.TAG, "mHwIMSEx is null");
            return null;
        }

        public boolean injectInputEventByDisplayId(InputEvent inputEvent, int mode, int displayId) {
            if (inputEvent == null) {
                Slog.w(InputManagerService.TAG, "warning, inputEvent is null.");
                return false;
            }
            inputEvent.setDisplayId(displayId);
            return InputManagerService.this.injectInputEvent(inputEvent, mode);
        }

        public void fadeMousePointer() {
            if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || InputManagerService.this.checkCallingPermission(InputManagerService.BASIC_MODE_KEYMOUSE, "fadeMousePointer")) {
                if (InputManagerService.DEBUG_HWFLOW) {
                    Log.i(InputManagerService.TAG, "fadeMousePointer");
                }
                InputManagerService.nativeFadeMousePointer(InputManagerService.this.mPtr);
                return;
            }
            Log.e(InputManagerService.TAG, "fadeMousePointer check permission error.");
        }

        public void setMousePosition(float xPosition, float yPosition) {
            if (UserHandle.getAppId(Binder.getCallingUid()) != 1000) {
                Log.e(InputManagerService.TAG, "setMousePosition check permission error.");
                return;
            }
            if (InputManagerService.DEBUG_HWFLOW) {
                Log.i(InputManagerService.TAG, "setMousePosition xPosition: " + xPosition + " ,yPosition: " + yPosition);
            }
            InputManagerService.nativeSetMousePosition(InputManagerService.this.mPtr, xPosition, yPosition);
        }
    }
}
