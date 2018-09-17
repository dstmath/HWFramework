package com.android.server.input;

import android.app.Notification.Builder;
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.ContentObserver;
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
import android.os.IBinder.DeathRecipient;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IInputFilter;
import android.view.IInputFilterHost.Stub;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.PointerIcon;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.R;
import com.android.internal.inputmethod.InputMethodSubtypeHandle;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DisplayThread;
import com.android.server.LocalServices;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.am.HwBroadcastRadarUtil;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import libcore.io.IoUtils;
import libcore.io.Streams;
import libcore.util.Objects;
import org.xmlpull.v1.XmlPullParser;

public class InputManagerService extends AbsInputManagerService implements Monitor {
    public static final int BTN_MOUSE = 272;
    static final boolean DEBUG = false;
    private static final String EXCLUDED_DEVICES_PATH = "etc/excluded-input-devices.xml";
    private static final int INJECTION_TIMEOUT_MILLIS = 30000;
    private static final int INPUT_EVENT_INJECTION_FAILED = 2;
    private static final int INPUT_EVENT_INJECTION_PERMISSION_DENIED = 1;
    private static final int INPUT_EVENT_INJECTION_SUCCEEDED = 0;
    private static final int INPUT_EVENT_INJECTION_TIMED_OUT = 3;
    public static final int KEY_STATE_DOWN = 1;
    public static final int KEY_STATE_UNKNOWN = -1;
    public static final int KEY_STATE_UP = 0;
    public static final int KEY_STATE_VIRTUAL = 2;
    private static final int MSG_DELIVER_INPUT_DEVICES_CHANGED = 1;
    private static final int MSG_DELIVER_TABLET_MODE_CHANGED = 6;
    private static final int MSG_INPUT_METHOD_SUBTYPE_CHANGED = 7;
    private static final int MSG_RELOAD_DEVICE_ALIASES = 5;
    private static final int MSG_RELOAD_KEYBOARD_LAYOUTS = 3;
    private static final int MSG_SWITCH_KEYBOARD_LAYOUT = 2;
    private static final int MSG_UPDATE_KEYBOARD_LAYOUTS = 4;
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
    public static final int VIEWPORT_DEFAULT = 1;
    public static final int VIEWPORT_EXTERNAL = 2;
    public static final int VIEWPORT_VIRTUAL = 3;
    final Context mContext;
    private InputMethodSubtypeHandle mCurrentImeHandle;
    HwCustInputManagerService mCust = ((HwCustInputManagerService) HwCustUtils.createObj(HwCustInputManagerService.class, new Object[]{this}));
    private final PersistentDataStore mDataStore = new PersistentDataStore();
    private final File mDoubleTouchGestureEnableFile;
    private IWindow mFocusedWindow;
    private boolean mFocusedWindowHasCapture;
    private final InputManagerHandler mHandler;
    private InputDevice[] mInputDevices = new InputDevice[0];
    private final SparseArray<InputDevicesChangedListenerRecord> mInputDevicesChangedListeners = new SparseArray();
    private boolean mInputDevicesChangedPending;
    private Object mInputDevicesLock = new Object();
    IInputFilter mInputFilter;
    InputFilterHost mInputFilterHost;
    final Object mInputFilterLock = new Object();
    private boolean mKeyboardLayoutNotificationShown;
    private int mNextVibratorTokenValue;
    private NotificationManager mNotificationManager;
    public final long mPtr;
    private boolean mSystemReady;
    private final SparseArray<TabletModeChangedListenerRecord> mTabletModeChangedListeners = new SparseArray();
    private final Object mTabletModeLock = new Object();
    private final ArrayList<InputDevice> mTempFullKeyboards = new ArrayList();
    private final ArrayList<InputDevicesChangedListenerRecord> mTempInputDevicesChangedListenersToNotify = new ArrayList();
    private final List<TabletModeChangedListenerRecord> mTempTabletModeChangedListenersToNotify = new ArrayList();
    final boolean mUseDevInputEventForAudioJack;
    private Object mVibratorLock = new Object();
    private HashMap<IBinder, VibratorToken> mVibratorTokens = new HashMap();
    private WindowManagerCallbacks mWindowManagerCallbacks;
    private WiredAccessoryCallbacks mWiredAccessoryCallbacks;

    public interface WiredAccessoryCallbacks {
        void notifyWiredAccessoryChanged(long j, int i, int i2);

        void systemReady();
    }

    private interface KeyboardLayoutVisitor {
        void visitKeyboardLayout(Resources resources, int i, KeyboardLayout keyboardLayout);
    }

    private final class InputDevicesChangedListenerRecord implements DeathRecipient {
        private final IInputDevicesChangedListener mListener;
        private final int mPid;

        public InputDevicesChangedListenerRecord(int pid, IInputDevicesChangedListener listener) {
            this.mPid = pid;
            this.mListener = listener;
        }

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

    private final class InputFilterHost extends Stub {
        private boolean mDisconnected;

        /* synthetic */ InputFilterHost(InputManagerService this$0, InputFilterHost -this1) {
            this();
        }

        private InputFilterHost() {
        }

        public void disconnectLocked() {
            this.mDisconnected = true;
        }

        public void sendInputEvent(InputEvent event, int policyFlags) {
            if (event == null) {
                throw new IllegalArgumentException("event must not be null");
            }
            synchronized (InputManagerService.this.mInputFilterLock) {
                if (!this.mDisconnected) {
                    if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad()) {
                        InputEvent inputEvent = event;
                        InputManagerService.nativeInjectInputEvent(InputManagerService.this.mPtr, inputEvent, HwPCUtils.getPCDisplayID(), 0, 0, 0, 0, policyFlags | 67108864);
                    } else {
                        InputManagerService.nativeInjectInputEvent(InputManagerService.this.mPtr, event, 0, 0, 0, 0, 0, policyFlags | 67108864);
                    }
                }
            }
        }
    }

    private final class InputManagerHandler extends Handler {
        public InputManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            SomeArgs args;
            switch (msg.what) {
                case 1:
                    InputManagerService.this.deliverInputDevicesChanged((InputDevice[]) msg.obj);
                    return;
                case 2:
                    args = msg.obj;
                    InputManagerService.this.handleSwitchKeyboardLayout((InputDeviceIdentifier) args.arg1, (InputMethodSubtypeHandle) args.arg2);
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
                    args = (SomeArgs) msg.obj;
                    InputManagerService.this.deliverTabletModeChanged((((long) args.argi1) & 4294967295L) | (((long) args.argi2) << 32), ((Boolean) args.arg1).booleanValue());
                    return;
                case 7:
                    int userId = msg.arg1;
                    args = (SomeArgs) msg.obj;
                    InputMethodInfo inputMethodInfo = args.arg1;
                    InputMethodSubtype subtype = args.arg2;
                    args.recycle();
                    InputManagerService.this.handleSwitchInputMethodSubtype(userId, inputMethodInfo, subtype);
                    return;
                default:
                    return;
            }
        }
    }

    private static final class KeyboardLayoutDescriptor {
        public String keyboardLayoutName;
        public String packageName;
        public String receiverName;

        private KeyboardLayoutDescriptor() {
        }

        public static String format(String packageName, String receiverName, String keyboardName) {
            return packageName + "/" + receiverName + "/" + keyboardName;
        }

        public static KeyboardLayoutDescriptor parse(String descriptor) {
            int pos = descriptor.indexOf(47);
            if (pos < 0 || pos + 1 == descriptor.length()) {
                return null;
            }
            int pos2 = descriptor.indexOf(47, pos + 1);
            if (pos2 < pos + 2 || pos2 + 1 == descriptor.length()) {
                return null;
            }
            KeyboardLayoutDescriptor result = new KeyboardLayoutDescriptor();
            result.packageName = descriptor.substring(0, pos);
            result.receiverName = descriptor.substring(pos + 1, pos2);
            result.keyboardLayoutName = descriptor.substring(pos2 + 1);
            return result;
        }
    }

    private final class LocalService extends InputManagerInternal {
        /* synthetic */ LocalService(InputManagerService this$0, LocalService -this1) {
            this();
        }

        private LocalService() {
        }

        public void setDisplayViewports(DisplayViewport defaultViewport, DisplayViewport externalTouchViewport, List<DisplayViewport> virtualTouchViewports) {
            InputManagerService.this.setDisplayViewportsInternal(defaultViewport, externalTouchViewport, virtualTouchViewports);
        }

        public boolean injectInputEvent(InputEvent event, int displayId, int mode) {
            return InputManagerService.this.injectInputEventInternal(event, displayId, mode);
        }

        public void setInteractive(boolean interactive) {
            InputManagerService.nativeSetInteractive(InputManagerService.this.mPtr, interactive);
        }

        public void onInputMethodSubtypeChanged(int userId, InputMethodInfo inputMethodInfo, InputMethodSubtype subtype) {
            SomeArgs someArgs = SomeArgs.obtain();
            someArgs.arg1 = inputMethodInfo;
            someArgs.arg2 = subtype;
            InputManagerService.this.mHandler.obtainMessage(7, userId, 0, someArgs).sendToTarget();
        }

        public void toggleCapsLock(int deviceId) {
            InputManagerService.nativeToggleCapsLock(InputManagerService.this.mPtr, deviceId);
        }

        public void setPulseGestureEnabled(boolean enabled) {
            IOException e;
            Object writer;
            Throwable th;
            if (InputManagerService.this.mDoubleTouchGestureEnableFile != null) {
                AutoCloseable writer2 = null;
                try {
                    String str;
                    FileWriter writer3 = new FileWriter(InputManagerService.this.mDoubleTouchGestureEnableFile);
                    if (enabled) {
                        try {
                            str = "1";
                        } catch (IOException e2) {
                            e = e2;
                            writer2 = writer3;
                        } catch (Throwable th2) {
                            th = th2;
                            writer2 = writer3;
                            IoUtils.closeQuietly(writer2);
                            throw th;
                        }
                    }
                    str = "0";
                    writer3.write(str);
                    IoUtils.closeQuietly(writer3);
                } catch (IOException e3) {
                    e = e3;
                    try {
                        Log.wtf(InputManagerService.TAG, "Unable to setPulseGestureEnabled", e);
                        IoUtils.closeQuietly(writer2);
                    } catch (Throwable th3) {
                        th = th3;
                        IoUtils.closeQuietly(writer2);
                        throw th;
                    }
                }
            }
        }
    }

    private class Shell extends ShellCommand {
        /* synthetic */ Shell(InputManagerService this$0, Shell -this1) {
            this();
        }

        private Shell() {
        }

        public int onCommand(String cmd) {
            return InputManagerService.this.onShellCommand(this, cmd);
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Input manager commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("");
            pw.println("  setlayout IME_ID IME_SUPTYPE_HASH_CODE DEVICE_DESCRIPTOR VENDOR_ID PRODUCT_ID KEYBOARD_DESCRIPTOR");
            pw.println("    Sets a keyboard layout for a given IME subtype and input device pair");
        }
    }

    private final class TabletModeChangedListenerRecord implements DeathRecipient {
        private final ITabletModeChangedListener mListener;
        private final int mPid;

        public TabletModeChangedListenerRecord(int pid, ITabletModeChangedListener listener) {
            this.mPid = pid;
            this.mListener = listener;
        }

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

    private final class VibratorToken implements DeathRecipient {
        public final int mDeviceId;
        public final IBinder mToken;
        public final int mTokenValue;
        public boolean mVibrating;

        public VibratorToken(int deviceId, IBinder token, int tokenValue) {
            this.mDeviceId = deviceId;
            this.mToken = token;
            this.mTokenValue = tokenValue;
        }

        public void binderDied() {
            InputManagerService.this.onVibratorTokenDied(this);
        }
    }

    public interface WindowManagerCallbacks {
        KeyEvent dispatchUnhandledKey(InputWindowHandle inputWindowHandle, KeyEvent keyEvent, int i);

        int getPointerLayer();

        long interceptKeyBeforeDispatching(InputWindowHandle inputWindowHandle, KeyEvent keyEvent, int i);

        int interceptKeyBeforeQueueing(KeyEvent keyEvent, int i);

        int interceptMotionBeforeQueueingNonInteractive(long j, int i);

        long notifyANR(InputApplicationHandle inputApplicationHandle, InputWindowHandle inputWindowHandle, String str);

        void notifyANRWarning(int i);

        void notifyCameraLensCoverSwitchChanged(long j, boolean z);

        void notifyConfigurationChanged();

        void notifyInputChannelBroken(InputWindowHandle inputWindowHandle);

        void notifyLidSwitchChanged(long j, boolean z);
    }

    private static native void nativeCancelVibrate(long j, int i, int i2);

    private static native String nativeDump(long j);

    private static native int nativeGetKeyCodeState(long j, int i, int i2, int i3);

    private static native int nativeGetScanCodeState(long j, int i, int i2, int i3);

    private static native int nativeGetSwitchState(long j, int i, int i2, int i3);

    private static native boolean nativeHasKeys(long j, int i, int i2, int[] iArr, boolean[] zArr);

    private static native long nativeInit(InputManagerService inputManagerService, Context context, MessageQueue messageQueue);

    private static native int nativeInjectInputEvent(long j, InputEvent inputEvent, int i, int i2, int i3, int i4, int i5, int i6);

    private static native void nativeMonitor(long j);

    private static native void nativeRegisterInputChannel(long j, InputChannel inputChannel, InputWindowHandle inputWindowHandle, boolean z);

    private static native void nativeReloadCalibration(long j);

    private static native void nativeReloadDeviceAliases(long j);

    private static native void nativeReloadKeyboardLayouts(long j);

    public static native void nativeReloadPointerIcons(long j, Context context);

    public static native void nativeResponseTouchEvent(long j, boolean z);

    private static native void nativeSetCustomPointerIcon(long j, PointerIcon pointerIcon);

    private static native void nativeSetDisplayViewport(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, String str);

    private static native void nativeSetFocusedApplication(long j, InputApplicationHandle inputApplicationHandle);

    private static native void nativeSetInputDispatchMode(long j, boolean z, boolean z2);

    protected static native void nativeSetInputFilterEnabled(long j, boolean z);

    private static native void nativeSetInputWindows(long j, InputWindowHandle[] inputWindowHandleArr);

    private static native void nativeSetInteractive(long j, boolean z);

    private static native void nativeSetMirrorLinkInputStatus(long j, boolean z);

    private static native void nativeSetPointerCapture(long j, boolean z);

    private static native void nativeSetPointerIconType(long j, int i);

    private static native void nativeSetPointerSpeed(long j, int i);

    private static native void nativeSetShowTouches(long j, boolean z);

    private static native void nativeSetSystemUiVisibility(long j, int i);

    private static native void nativeSetVirtualDisplayViewports(long j, DisplayViewport[] displayViewportArr);

    private static native void nativeStart(long j);

    private static native void nativeToggleCapsLock(long j, int i);

    private static native boolean nativeTransferTouchFocus(long j, InputChannel inputChannel, InputChannel inputChannel2);

    private static native void nativeUnregisterInputChannel(long j, InputChannel inputChannel);

    private static native void nativeVibrate(long j, int i, long[] jArr, int i2, int i3);

    public InputManagerService(Context context) {
        File file;
        this.mContext = context;
        this.mHandler = new InputManagerHandler(DisplayThread.get().getLooper());
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17957038);
        Slog.i(TAG, "Initializing input manager, mUseDevInputEventForAudioJack=" + this.mUseDevInputEventForAudioJack);
        this.mPtr = nativeInit(this, this.mContext, this.mHandler.getLooper().getQueue());
        String doubleTouchGestureEnablePath = context.getResources().getString(17039776);
        if (TextUtils.isEmpty(doubleTouchGestureEnablePath)) {
            file = null;
        } else {
            file = new File(doubleTouchGestureEnablePath);
        }
        this.mDoubleTouchGestureEnableFile = file;
        LocalServices.addService(InputManagerInternal.class, new LocalService(this, null));
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
        if (this.mCust != null) {
            this.mCust.registerContentObserverForSetGloveMode(this.mContext);
        }
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                InputManagerService.this.updatePointerSpeedFromSettings();
                InputManagerService.this.updateShowTouchesFromSettings();
                InputManagerService.this.updateAccessibilityLargePointerFromSettings();
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mHandler);
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
        filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                InputManagerService.this.updateKeyboardLayouts();
            }
        }, filter, null, this.mHandler);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                InputManagerService.this.reloadDeviceAliases();
            }
        }, new IntentFilter("android.bluetooth.device.action.ALIAS_CHANGED"), null, this.mHandler);
        this.mHandler.sendEmptyMessage(5);
        this.mHandler.sendEmptyMessage(4);
        if (this.mWiredAccessoryCallbacks != null) {
            this.mWiredAccessoryCallbacks.systemReady();
        }
    }

    private void reloadKeyboardLayouts() {
        nativeReloadKeyboardLayouts(this.mPtr);
    }

    private void reloadDeviceAliases() {
        nativeReloadDeviceAliases(this.mPtr);
    }

    private void setDisplayViewportsInternal(DisplayViewport defaultViewport, DisplayViewport externalTouchViewport, List<DisplayViewport> virtualTouchViewports) {
        if (defaultViewport.valid) {
            setDisplayViewport(1, defaultViewport);
        }
        if (externalTouchViewport.valid) {
            setDisplayViewport(2, externalTouchViewport);
        } else if (defaultViewport.valid) {
            setDisplayViewport(2, defaultViewport);
        }
        nativeSetVirtualDisplayViewports(this.mPtr, (DisplayViewport[]) virtualTouchViewports.toArray(new DisplayViewport[0]));
    }

    private void setDisplayViewport(int viewportType, DisplayViewport viewport) {
        nativeSetDisplayViewport(this.mPtr, viewportType, viewport.displayId, viewport.orientation, viewport.logicalFrame.left, viewport.logicalFrame.top, viewport.logicalFrame.right, viewport.logicalFrame.bottom, viewport.physicalFrame.left, viewport.physicalFrame.top, viewport.physicalFrame.right, viewport.physicalFrame.bottom, viewport.deviceWidth, viewport.deviceHeight, viewport.uniqueId);
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

    public InputChannel monitorInput(String inputChannelName) {
        if (inputChannelName == null) {
            throw new IllegalArgumentException("inputChannelName must not be null.");
        }
        InputChannel[] inputChannels = InputChannel.openInputChannelPair(inputChannelName);
        nativeRegisterInputChannel(this.mPtr, inputChannels[0], null, true);
        inputChannels[0].dispose();
        return inputChannels[1];
    }

    public void registerInputChannel(InputChannel inputChannel, InputWindowHandle inputWindowHandle) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null.");
        }
        nativeRegisterInputChannel(this.mPtr, inputChannel, inputWindowHandle, false);
    }

    public void unregisterInputChannel(InputChannel inputChannel) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null.");
        }
        nativeUnregisterInputChannel(this.mPtr, inputChannel);
    }

    public void setInputFilter(IInputFilter filter) {
        synchronized (this.mInputFilterLock) {
            IInputFilter oldFilter = this.mInputFilter;
            if (oldFilter == filter) {
                return;
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
                this.mInputFilterHost = new InputFilterHost(this, null);
                try {
                    filter.install(this.mInputFilterHost);
                } catch (RemoteException e2) {
                }
            }
            nativeSetInputFilterEnabled(this.mPtr, filter != null);
        }
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        return injectInputEventInternal(event, 0, mode);
    }

    protected boolean injectInputEventInternal(InputEvent event, int displayId, int mode) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (mode == 0 || mode == 2 || mode == 1) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                int result = nativeInjectInputEvent(this.mPtr, event, displayId, pid, uid, mode, INJECTION_TIMEOUT_MILLIS, 134217728);
                switch (result) {
                    case 0:
                        return true;
                    case 1:
                        Slog.w(TAG, "Input event injection from pid " + pid + " permission denied.");
                        throw new SecurityException("Injecting to another application requires INJECT_EVENTS permission");
                    case 3:
                        Slog.w(TAG, "Input event injection from pid " + pid + " timed out.");
                        return false;
                    default:
                        Slog.w(TAG, "Input event injection from pid " + pid + " failed.");
                        return false;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            throw new IllegalArgumentException("mode is invalid");
        }
    }

    public InputDevice getInputDevice(int deviceId) {
        synchronized (this.mInputDevicesLock) {
            for (InputDevice inputDevice : this.mInputDevices) {
                if (inputDevice.getId() == deviceId) {
                    return inputDevice;
                }
            }
            return null;
        }
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
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        synchronized (this.mInputDevicesLock) {
            int callingPid = Binder.getCallingPid();
            if (this.mInputDevicesChangedListeners.get(callingPid) != null) {
                throw new SecurityException("The calling process has already registered an InputDevicesChangedListener.");
            }
            InputDevicesChangedListenerRecord record = new InputDevicesChangedListenerRecord(callingPid, listener);
            try {
                listener.asBinder().linkToDeath(record, 0);
                this.mInputDevicesChangedListeners.put(callingPid, record);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void onInputDevicesChangedListenerDied(int pid) {
        synchronized (this.mInputDevicesLock) {
            this.mInputDevicesChangedListeners.remove(pid);
        }
    }

    /* JADX WARNING: Missing block: B:32:0x007c, code:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:33:0x007d, code:
            if (r1 >= r9) goto L_0x0090;
     */
    /* JADX WARNING: Missing block: B:34:0x007f, code:
            ((com.android.server.input.InputManagerService.InputDevicesChangedListenerRecord) r13.mTempInputDevicesChangedListenersToNotify.get(r1)).notifyInputDevicesChanged(r0);
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:38:0x0090, code:
            r13.mTempInputDevicesChangedListenersToNotify.clear();
            r3 = new java.util.ArrayList();
            r6 = r13.mTempFullKeyboards.size();
            r11 = r13.mDataStore;
     */
    /* JADX WARNING: Missing block: B:39:0x00a2, code:
            monitor-enter(r11);
     */
    /* JADX WARNING: Missing block: B:40:0x00a3, code:
            r1 = 0;
     */
    /* JADX WARNING: Missing block: B:41:0x00a4, code:
            if (r1 >= r6) goto L_0x00cd;
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r2 = (android.view.InputDevice) r13.mTempFullKeyboards.get(r1);
            r4 = getCurrentKeyboardLayoutForInputDevice(r2.getIdentifier());
     */
    /* JADX WARNING: Missing block: B:44:0x00b6, code:
            if (r4 != null) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:45:0x00b8, code:
            r4 = getDefaultKeyboardLayout(r2);
     */
    /* JADX WARNING: Missing block: B:46:0x00bc, code:
            if (r4 == null) goto L_0x00c5;
     */
    /* JADX WARNING: Missing block: B:47:0x00be, code:
            setCurrentKeyboardLayoutForInputDevice(r2.getIdentifier(), r4);
     */
    /* JADX WARNING: Missing block: B:48:0x00c5, code:
            if (r4 != null) goto L_0x00ca;
     */
    /* JADX WARNING: Missing block: B:49:0x00c7, code:
            r3.add(r2);
     */
    /* JADX WARNING: Missing block: B:50:0x00ca, code:
            r1 = r1 + 1;
     */
    /* JADX WARNING: Missing block: B:51:0x00cd, code:
            monitor-exit(r11);
     */
    /* JADX WARNING: Missing block: B:53:0x00d0, code:
            if (r13.mNotificationManager == null) goto L_0x00e3;
     */
    /* JADX WARNING: Missing block: B:55:0x00d6, code:
            if (r3.isEmpty() != false) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:57:0x00dd, code:
            if (r3.size() <= 1) goto L_0x00ec;
     */
    /* JADX WARNING: Missing block: B:58:0x00df, code:
            showMissingKeyboardLayoutNotification(null);
     */
    /* JADX WARNING: Missing block: B:59:0x00e3, code:
            r13.mTempFullKeyboards.clear();
     */
    /* JADX WARNING: Missing block: B:60:0x00e8, code:
            return;
     */
    /* JADX WARNING: Missing block: B:64:0x00ec, code:
            showMissingKeyboardLayoutNotification((android.view.InputDevice) r3.get(0));
     */
    /* JADX WARNING: Missing block: B:66:0x00f9, code:
            if (r13.mKeyboardLayoutNotificationShown == false) goto L_0x00e3;
     */
    /* JADX WARNING: Missing block: B:67:0x00fb, code:
            hideMissingKeyboardLayoutNotification();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
        Throwable th;
        this.mTempInputDevicesChangedListenersToNotify.clear();
        this.mTempFullKeyboards.clear();
        synchronized (this.mInputDevicesLock) {
            try {
                if (this.mInputDevicesChangedPending) {
                    int i;
                    this.mInputDevicesChangedPending = false;
                    int numListeners = this.mInputDevicesChangedListeners.size();
                    for (i = 0; i < numListeners; i++) {
                        this.mTempInputDevicesChangedListenersToNotify.add((InputDevicesChangedListenerRecord) this.mInputDevicesChangedListeners.valueAt(i));
                    }
                    int numDevices = this.mInputDevices.length;
                    int[] deviceIdAndGeneration = new int[(numDevices * 2)];
                    i = 0;
                    int numFullKeyboardsAdded = 0;
                    while (i < numDevices) {
                        int numFullKeyboardsAdded2;
                        try {
                            InputDevice inputDevice = this.mInputDevices[i];
                            deviceIdAndGeneration[i * 2] = inputDevice.getId();
                            deviceIdAndGeneration[(i * 2) + 1] = inputDevice.getGeneration();
                            if (inputDevice.isVirtual()) {
                                numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                            } else if (!inputDevice.isFullKeyboard()) {
                                numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                            } else if (containsInputDeviceWithDescriptor(oldInputDevices, inputDevice.getDescriptor())) {
                                this.mTempFullKeyboards.add(inputDevice);
                                numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                            } else {
                                numFullKeyboardsAdded2 = numFullKeyboardsAdded + 1;
                                this.mTempFullKeyboards.add(numFullKeyboardsAdded, inputDevice);
                            }
                            i++;
                            numFullKeyboardsAdded = numFullKeyboardsAdded2;
                        } catch (Throwable th2) {
                            th = th2;
                            numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                        }
                    }
                } else {
                    return;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
        throw th;
    }

    private String getDefaultKeyboardLayout(final InputDevice d) {
        final Locale systemLocale = this.mContext.getResources().getConfiguration().locale;
        if (TextUtils.isEmpty(systemLocale.getLanguage())) {
            return null;
        }
        final List<KeyboardLayout> layouts = new ArrayList();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                if (layout.getVendorId() == d.getVendorId() && layout.getProductId() == d.getProductId()) {
                    LocaleList locales = layout.getLocales();
                    int numLocales = locales.size();
                    for (int localeIndex = 0; localeIndex < numLocales; localeIndex++) {
                        if (InputManagerService.isCompatibleLocale(systemLocale, locales.get(localeIndex))) {
                            layouts.add(layout);
                            break;
                        }
                    }
                }
            }
        });
        if (layouts.isEmpty()) {
            return null;
        }
        int i;
        KeyboardLayout layout;
        LocaleList locales;
        int numLocales;
        int localeIndex;
        Collections.sort(layouts);
        int N = layouts.size();
        for (i = 0; i < N; i++) {
            layout = (KeyboardLayout) layouts.get(i);
            locales = layout.getLocales();
            numLocales = locales.size();
            for (localeIndex = 0; localeIndex < numLocales; localeIndex++) {
                Locale locale = locales.get(localeIndex);
                if (locale.getCountry().equals(systemLocale.getCountry()) && locale.getVariant().equals(systemLocale.getVariant())) {
                    return layout.getDescriptor();
                }
            }
        }
        for (i = 0; i < N; i++) {
            layout = (KeyboardLayout) layouts.get(i);
            locales = layout.getLocales();
            numLocales = locales.size();
            for (localeIndex = 0; localeIndex < numLocales; localeIndex++) {
                if (locales.get(localeIndex).getCountry().equals(systemLocale.getCountry())) {
                    return layout.getDescriptor();
                }
            }
        }
        return ((KeyboardLayout) layouts.get(0)).getDescriptor();
    }

    private static boolean isCompatibleLocale(Locale systemLocale, Locale keyboardLocale) {
        if (!systemLocale.getLanguage().equals(keyboardLocale.getLanguage())) {
            return false;
        }
        if (TextUtils.isEmpty(systemLocale.getCountry()) || (TextUtils.isEmpty(keyboardLocale.getCountry()) ^ 1) == 0 || (systemLocale.getCountry().equals(keyboardLocale.getCountry()) ^ 1) == 0) {
            return true;
        }
        return false;
    }

    public TouchCalibration getTouchCalibrationForInputDevice(String inputDeviceDescriptor, int surfaceRotation) {
        if (inputDeviceDescriptor == null) {
            throw new IllegalArgumentException("inputDeviceDescriptor must not be null");
        }
        TouchCalibration touchCalibration;
        synchronized (this.mDataStore) {
            touchCalibration = this.mDataStore.getTouchCalibration(inputDeviceDescriptor, surfaceRotation);
        }
        return touchCalibration;
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
        } else if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        } else {
            synchronized (this.mTabletModeLock) {
                int callingPid = Binder.getCallingPid();
                if (this.mTabletModeChangedListeners.get(callingPid) != null) {
                    throw new IllegalStateException("The calling process has already registered a TabletModeChangedListener.");
                }
                TabletModeChangedListenerRecord record = new TabletModeChangedListenerRecord(callingPid, listener);
                try {
                    listener.asBinder().linkToDeath(record, 0);
                    this.mTabletModeChangedListeners.put(callingPid, record);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private void onTabletModeChangedListenerDied(int pid) {
        synchronized (this.mTabletModeLock) {
            this.mTabletModeChangedListeners.remove(pid);
        }
    }

    private void deliverTabletModeChanged(long whenNanos, boolean inTabletMode) {
        int numListeners;
        int i;
        this.mTempTabletModeChangedListenersToNotify.clear();
        synchronized (this.mTabletModeLock) {
            numListeners = this.mTabletModeChangedListeners.size();
            for (i = 0; i < numListeners; i++) {
                this.mTempTabletModeChangedListenersToNotify.add((TabletModeChangedListenerRecord) this.mTabletModeChangedListeners.valueAt(i));
            }
        }
        for (i = 0; i < numListeners; i++) {
            ((TabletModeChangedListenerRecord) this.mTempTabletModeChangedListenersToNotify.get(i)).notifyTabletModeChanged(whenNanos, inTabletMode);
        }
    }

    private void showMissingKeyboardLayoutNotification(InputDevice device) {
        if (!this.mKeyboardLayoutNotificationShown) {
            Intent intent = new Intent("android.settings.HARD_KEYBOARD_SETTINGS");
            if (device != null) {
                intent.putExtra("input_device_identifier", device.getIdentifier());
            }
            intent.setFlags(337641472);
            PendingIntent keyboardLayoutIntent = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            Resources r = this.mContext.getResources();
            this.mNotificationManager.notifyAsUser(null, 19, new Builder(this.mContext, SystemNotificationChannels.PHYSICAL_KEYBOARD).setContentTitle(r.getString(17040947)).setContentText(r.getString(17040946)).setContentIntent(keyboardLayoutIntent).setSmallIcon(17302609).setColor(this.mContext.getColor(17170769)).build(), UserHandle.ALL);
            this.mKeyboardLayoutNotificationShown = true;
        }
    }

    private void hideMissingKeyboardLayoutNotification() {
        if (this.mKeyboardLayoutNotificationShown) {
            this.mKeyboardLayoutNotificationShown = false;
            this.mNotificationManager.cancelAsUser(null, 19, UserHandle.ALL);
        }
    }

    private void updateKeyboardLayouts() {
        final HashSet<String> availableKeyboardLayouts = new HashSet();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                availableKeyboardLayouts.add(layout.getDescriptor());
            }
        });
        synchronized (this.mDataStore) {
            try {
                this.mDataStore.removeUninstalledKeyboardLayouts(availableKeyboardLayouts);
                this.mDataStore.saveIfNeeded();
            } catch (Throwable th) {
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
        final ArrayList<KeyboardLayout> list = new ArrayList();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                list.add(layout);
            }
        });
        return (KeyboardLayout[]) list.toArray(new KeyboardLayout[list.size()]);
    }

    public KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        final String[] enabledLayoutDescriptors = getEnabledKeyboardLayoutsForInputDevice(identifier);
        final ArrayList<KeyboardLayout> enabledLayouts = new ArrayList(enabledLayoutDescriptors.length);
        final ArrayList<KeyboardLayout> potentialLayouts = new ArrayList();
        final InputDeviceIdentifier inputDeviceIdentifier = identifier;
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            boolean mHasSeenDeviceSpecificLayout;

            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                String[] strArr = enabledLayoutDescriptors;
                int i = 0;
                int length = strArr.length;
                while (i < length) {
                    String s = strArr[i];
                    if (s == null || !s.equals(layout.getDescriptor())) {
                        i++;
                    } else {
                        enabledLayouts.add(layout);
                        return;
                    }
                }
                if (layout.getVendorId() == inputDeviceIdentifier.getVendorId() && layout.getProductId() == inputDeviceIdentifier.getProductId()) {
                    if (!this.mHasSeenDeviceSpecificLayout) {
                        this.mHasSeenDeviceSpecificLayout = true;
                        potentialLayouts.clear();
                    }
                    potentialLayouts.add(layout);
                } else if (layout.getVendorId() == -1 && layout.getProductId() == -1 && (this.mHasSeenDeviceSpecificLayout ^ 1) != 0) {
                    potentialLayouts.add(layout);
                }
            }
        });
        int enabledLayoutSize = enabledLayouts.size();
        int potentialLayoutSize = potentialLayouts.size();
        KeyboardLayout[] layouts = new KeyboardLayout[(enabledLayoutSize + potentialLayoutSize)];
        enabledLayouts.toArray(layouts);
        for (int i = 0; i < potentialLayoutSize; i++) {
            layouts[enabledLayoutSize + i] = (KeyboardLayout) potentialLayouts.get(i);
        }
        return layouts;
    }

    public KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor) {
        if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
        final KeyboardLayout[] result = new KeyboardLayout[1];
        visitKeyboardLayout(keyboardLayoutDescriptor, new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                result[0] = layout;
            }
        });
        if (result[0] == null) {
            Slog.w(TAG, "Could not get keyboard layout with descriptor '" + keyboardLayoutDescriptor + "'.");
        }
        return result[0];
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
            } catch (NameNotFoundException e) {
            }
        }
    }

    /* JADX WARNING: Missing block: B:20:?, code:
            r19.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void visitKeyboardLayoutsInPackage(PackageManager pm, ActivityInfo receiver, String keyboardName, int requestedPriority, KeyboardLayoutVisitor visitor) {
        Bundle metaData = receiver.metaData;
        if (metaData != null) {
            int configResId = metaData.getInt("android.hardware.input.metadata.KEYBOARD_LAYOUTS");
            if (configResId == 0) {
                Slog.w(TAG, "Missing meta-data 'android.hardware.input.metadata.KEYBOARD_LAYOUTS' on receiver " + receiver.packageName + "/" + receiver.name);
                return;
            }
            int priority;
            CharSequence receiverLabel = receiver.loadLabel(pm);
            String collection = receiverLabel != null ? receiverLabel.toString() : "";
            if ((receiver.applicationInfo.flags & 1) != 0) {
                priority = requestedPriority;
            } else {
                priority = 0;
            }
            XmlResourceParser parser;
            TypedArray a;
            try {
                Resources resources = pm.getResourcesForApplication(receiver.applicationInfo);
                parser = resources.getXml(configResId);
                XmlUtils.beginDocument(parser, "keyboard-layouts");
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        break;
                    } else if (element.equals("keyboard-layout")) {
                        a = resources.obtainAttributes(parser, R.styleable.KeyboardLayout);
                        String name = a.getString(1);
                        String label = a.getString(0);
                        int keyboardLayoutResId = a.getResourceId(2, 0);
                        LocaleList locales = getLocalesFromLanguageTags(a.getString(3));
                        int vid = a.getInt(5, -1);
                        int pid = a.getInt(4, -1);
                        if (name == null || label == null || keyboardLayoutResId == 0) {
                            Slog.w(TAG, "Missing required 'name', 'label' or 'keyboardLayout' attributes in keyboard layout resource from receiver " + receiver.packageName + "/" + receiver.name);
                        } else {
                            String descriptor = KeyboardLayoutDescriptor.format(receiver.packageName, receiver.name, name);
                            if (keyboardName == null || name.equals(keyboardName)) {
                                visitor.visitKeyboardLayout(resources, keyboardLayoutResId, new KeyboardLayout(descriptor, label, collection, priority, locales, vid, pid));
                            }
                        }
                        a.recycle();
                    } else {
                        Slog.w(TAG, "Skipping unrecognized element '" + element + "' in keyboard layout resource from receiver " + receiver.packageName + "/" + receiver.name);
                    }
                }
            } catch (Exception ex) {
                Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + receiver.packageName + "/" + receiver.name, ex);
            } catch (Throwable th) {
                parser.close();
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
            StringBuilder bob = new StringBuilder();
            bob.append("vendor:").append(identifier.getVendorId());
            bob.append(",product:").append(identifier.getProductId());
            return bob.toString();
        }
    }

    public String getCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier) {
        String layout;
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            layout = this.mDataStore.getCurrentKeyboardLayout(key);
            if (layout == null && (key.equals(identifier.getDescriptor()) ^ 1) != 0) {
                layout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
            }
        }
        return layout;
    }

    public void setCurrentKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "setCurrentKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else {
            String key = getLayoutDescriptor(identifier);
            synchronized (this.mDataStore) {
                try {
                    if (this.mDataStore.setCurrentKeyboardLayout(key, keyboardLayoutDescriptor)) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                }
            }
        }
    }

    public String[] getEnabledKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        String[] layouts;
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            layouts = this.mDataStore.getKeyboardLayouts(key);
            if ((layouts == null || layouts.length == 0) && (key.equals(identifier.getDescriptor()) ^ 1) != 0) {
                layouts = this.mDataStore.getKeyboardLayouts(identifier.getDescriptor());
            }
        }
        return layouts;
    }

    public KeyboardLayout getKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo imeInfo, InputMethodSubtype imeSubtype) {
        String keyboardLayoutDescriptor;
        InputMethodSubtypeHandle handle = new InputMethodSubtypeHandle(imeInfo, imeSubtype);
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            keyboardLayoutDescriptor = this.mDataStore.getKeyboardLayout(key, handle);
        }
        if (keyboardLayoutDescriptor == null) {
            return null;
        }
        final KeyboardLayout[] result = new KeyboardLayout[1];
        visitKeyboardLayout(keyboardLayoutDescriptor, new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                result[0] = layout;
            }
        });
        if (result[0] == null) {
            Slog.w(TAG, "Could not get keyboard layout with descriptor '" + keyboardLayoutDescriptor + "'.");
        }
        return result[0];
    }

    public void setKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo imeInfo, InputMethodSubtype imeSubtype, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "setKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else if (imeInfo == null) {
            throw new IllegalArgumentException("imeInfo must not be null");
        } else {
            setKeyboardLayoutForInputDeviceInner(identifier, new InputMethodSubtypeHandle(imeInfo, imeSubtype), keyboardLayoutDescriptor);
        }
    }

    private void setKeyboardLayoutForInputDeviceInner(InputDeviceIdentifier identifier, InputMethodSubtypeHandle imeHandle, String keyboardLayoutDescriptor) {
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            try {
                if (this.mDataStore.setKeyboardLayout(key, imeHandle, keyboardLayoutDescriptor)) {
                    if (imeHandle.equals(this.mCurrentImeHandle)) {
                        SomeArgs args = SomeArgs.obtain();
                        args.arg1 = identifier;
                        args.arg2 = imeHandle;
                        this.mHandler.obtainMessage(2, args).sendToTarget();
                    }
                    this.mHandler.sendEmptyMessage(3);
                }
                this.mDataStore.saveIfNeeded();
            } catch (Throwable th) {
                this.mDataStore.saveIfNeeded();
            }
        }
    }

    public void addKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "addKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else {
            String key = getLayoutDescriptor(identifier);
            synchronized (this.mDataStore) {
                try {
                    String oldLayout = this.mDataStore.getCurrentKeyboardLayout(key);
                    if (oldLayout == null && (key.equals(identifier.getDescriptor()) ^ 1) != 0) {
                        oldLayout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
                    }
                    if (this.mDataStore.addKeyboardLayout(key, keyboardLayoutDescriptor) && (Objects.equal(oldLayout, this.mDataStore.getCurrentKeyboardLayout(key)) ^ 1) != 0) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                }
            }
        }
    }

    public void removeKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, String keyboardLayoutDescriptor) {
        if (!checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "removeKeyboardLayoutForInputDevice()")) {
            throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
        } else if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        } else {
            String key = getLayoutDescriptor(identifier);
            synchronized (this.mDataStore) {
                try {
                    String oldLayout = this.mDataStore.getCurrentKeyboardLayout(key);
                    if (oldLayout == null && (key.equals(identifier.getDescriptor()) ^ 1) != 0) {
                        oldLayout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
                    }
                    boolean removed = this.mDataStore.removeKeyboardLayout(key, keyboardLayoutDescriptor);
                    if (!key.equals(identifier.getDescriptor())) {
                        removed |= this.mDataStore.removeKeyboardLayout(identifier.getDescriptor(), keyboardLayoutDescriptor);
                    }
                    if (removed && (Objects.equal(oldLayout, this.mDataStore.getCurrentKeyboardLayout(key)) ^ 1) != 0) {
                        this.mHandler.sendEmptyMessage(3);
                    }
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                }
            }
        }
    }

    private void handleSwitchInputMethodSubtype(int userId, InputMethodInfo inputMethodInfo, InputMethodSubtype subtype) {
        if (inputMethodInfo == null) {
            Slog.d(TAG, "No InputMethod is running, ignoring change");
        } else if (subtype == null || ("keyboard".equals(subtype.getMode()) ^ 1) == 0) {
            InputMethodSubtypeHandle handle = new InputMethodSubtypeHandle(inputMethodInfo, subtype);
            if (!handle.equals(this.mCurrentImeHandle)) {
                this.mCurrentImeHandle = handle;
                handleSwitchKeyboardLayout(null, handle);
            }
        } else {
            Slog.d(TAG, "InputMethodSubtype changed to non-keyboard subtype, ignoring change");
        }
    }

    private void handleSwitchKeyboardLayout(InputDeviceIdentifier identifier, InputMethodSubtypeHandle handle) {
        synchronized (this.mInputDevicesLock) {
            for (InputDevice device : this.mInputDevices) {
                if ((identifier == null || (device.getIdentifier().equals(identifier) ^ 1) == 0) && (device.isFullKeyboard() ^ 1) == 0) {
                    boolean changed = false;
                    synchronized (this.mDataStore) {
                        try {
                            if (this.mDataStore.switchKeyboardLayout(getLayoutDescriptor(device.getIdentifier()), handle)) {
                                changed = true;
                            }
                            this.mDataStore.saveIfNeeded();
                        } catch (Throwable th) {
                            this.mDataStore.saveIfNeeded();
                        }
                    }
                    if (changed) {
                        reloadKeyboardLayouts();
                    }
                }
            }
        }
    }

    public void setInputWindows(InputWindowHandle[] windowHandles, InputWindowHandle focusedWindowHandle) {
        IWindow newFocusedWindow = focusedWindowHandle != null ? focusedWindowHandle.clientWindow : null;
        if (this.mFocusedWindow != newFocusedWindow) {
            this.mFocusedWindow = newFocusedWindow;
            if (this.mFocusedWindowHasCapture) {
                setPointerCapture(false);
            }
        }
        nativeSetInputWindows(this.mPtr, windowHandles);
    }

    public void setFocusedApplication(InputApplicationHandle application) {
        nativeSetFocusedApplication(this.mPtr, application);
    }

    public void requestPointerCapture(IBinder windowToken, boolean enabled) {
        if (this.mFocusedWindow == null || this.mFocusedWindow.asBinder() != windowToken) {
            Slog.e(TAG, "requestPointerCapture called for a window that has no focus: " + windowToken);
        } else if (this.mFocusedWindowHasCapture == enabled) {
            Slog.i(TAG, "requestPointerCapture: already " + (enabled ? "enabled" : "disabled"));
        } else {
            setPointerCapture(enabled);
            try {
                this.mFocusedWindow.dispatchPointerCaptureChanged(enabled);
            } catch (RemoteException e) {
            }
        }
    }

    private void setPointerCapture(boolean enabled) {
        this.mFocusedWindowHasCapture = enabled;
        nativeSetPointerCapture(this.mPtr, enabled);
    }

    public void setMirrorLinkInputStatus(boolean status) {
        nativeSetMirrorLinkInputStatus(this.mPtr, status);
        Slog.i(TAG, "setMirrorLinkInputStatus server status = " + status);
    }

    public void setInputDispatchMode(boolean enabled, boolean frozen) {
        nativeSetInputDispatchMode(this.mPtr, enabled, frozen);
    }

    public void setSystemUiVisibility(int visibility) {
        nativeSetSystemUiVisibility(this.mPtr, visibility);
    }

    public void responseTouchEvent(boolean status) {
        nativeResponseTouchEvent(this.mPtr, status);
    }

    public boolean transferTouchFocus(InputChannel fromChannel, InputChannel toChannel) {
        if (fromChannel == null) {
            throw new IllegalArgumentException("fromChannel must not be null.");
        } else if (toChannel != null) {
            return nativeTransferTouchFocus(this.mPtr, fromChannel, toChannel);
        } else {
            throw new IllegalArgumentException("toChannel must not be null.");
        }
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
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("pointer_speed"), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                InputManagerService.this.updatePointerSpeedFromSettings();
            }
        }, -1);
    }

    private int getPointerSpeedSetting() {
        int speed = 0;
        try {
            return System.getIntForUser(this.mContext.getContentResolver(), "pointer_speed", -2);
        } catch (SettingNotFoundException e) {
            return speed;
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
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("show_touches"), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                InputManagerService.this.updateShowTouchesFromSettings();
            }
        }, -1);
    }

    public void updateAccessibilityLargePointerFromSettings() {
        boolean z = true;
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_large_pointer_icon", 0, -2) != 1) {
            z = false;
        }
        PointerIcon.setUseLargeIcons(z);
        nativeReloadPointerIcons(this.mPtr, null);
    }

    private void registerAccessibilityLargePointerSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_large_pointer_icon"), true, new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange) {
                InputManagerService.this.updateAccessibilityLargePointerFromSettings();
            }
        }, -1);
    }

    private int getShowTouchesSetting(int defaultValue) {
        int result = defaultValue;
        try {
            return System.getIntForUser(this.mContext.getContentResolver(), "show_touches", -2);
        } catch (SettingNotFoundException e) {
            return result;
        }
    }

    public void vibrate(int deviceId, long[] pattern, int repeat, IBinder token) {
        if (repeat >= pattern.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        VibratorToken v;
        synchronized (this.mVibratorLock) {
            v = (VibratorToken) this.mVibratorTokens.get(token);
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
    }

    /* JADX WARNING: Missing block: B:8:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancelVibrate(int deviceId, IBinder token) {
        synchronized (this.mVibratorLock) {
            VibratorToken v = (VibratorToken) this.mVibratorTokens.get(token);
            if (v == null || v.mDeviceId != deviceId) {
            } else {
                cancelVibrateIfNeeded(v);
            }
        }
    }

    void onVibratorTokenDied(VibratorToken v) {
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
        if (icon != null) {
            nativeSetCustomPointerIcon(this.mPtr, icon);
        }
    }

    public void dump(FileDescriptor fd, final PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("INPUT MANAGER (dumpsys input)\n");
            String dumpStr = nativeDump(this.mPtr);
            if (dumpStr != null) {
                pw.println(dumpStr);
            }
            pw.println("  Keyboard Layouts:");
            visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
                public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                    pw.println("    \"" + layout + "\": " + layout.getDescriptor());
                }
            });
            pw.println();
            synchronized (this.mDataStore) {
                this.mDataStore.dump(pw, "  ");
            }
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        new Shell(this, null).exec(this, in, out, err, args, callback, resultReceiver);
    }

    public int onShellCommand(Shell shell, String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            shell.onHelp();
            return 1;
        }
        if (cmd.equals("setlayout")) {
            if (checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "onShellCommand()")) {
                setKeyboardLayoutForInputDeviceInner(new InputDeviceIdentifier(shell.getNextArgRequired(), Integer.decode(shell.getNextArgRequired()).intValue(), Integer.decode(shell.getNextArgRequired()).intValue()), new InputMethodSubtypeHandle(shell.getNextArgRequired(), Integer.parseInt(shell.getNextArgRequired())), shell.getNextArgRequired());
            } else {
                throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
            }
        }
        return 0;
    }

    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return false;
    }

    public void monitor() {
        synchronized (this.mInputFilterLock) {
        }
        nativeMonitor(this.mPtr);
    }

    private void notifyConfigurationChanged(long whenNanos) {
        this.mWindowManagerCallbacks.notifyConfigurationChanged();
    }

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
        if (this.mUseDevInputEventForAudioJack && (switchMask & 212) != 0) {
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

    private void notifyInputChannelBroken(InputWindowHandle inputWindowHandle) {
        this.mWindowManagerCallbacks.notifyInputChannelBroken(inputWindowHandle);
    }

    private long notifyANR(InputApplicationHandle inputApplicationHandle, InputWindowHandle inputWindowHandle, String reason) {
        return this.mWindowManagerCallbacks.notifyANR(inputApplicationHandle, inputWindowHandle, reason);
    }

    boolean filterInputEvent(InputEvent event, int policyFlags) {
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

    private int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
    }

    protected long interceptKeyBeforeDispatching(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptKeyBeforeDispatching(focus, event, policyFlags);
    }

    KeyEvent dispatchUnhandledKey(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.dispatchUnhandledKey(focus, event, policyFlags);
    }

    private boolean checkInjectEventsPermission(int injectorPid, int injectorUid) {
        return this.mContext.checkPermission("android.permission.INJECT_EVENTS", injectorPid, injectorUid) == 0;
    }

    private int getVirtualKeyQuietTimeMillis() {
        return this.mContext.getResources().getInteger(17694860);
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x005a A:{SYNTHETIC, Splitter: B:21:0x005a} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x008a A:{SYNTHETIC, Splitter: B:29:0x008a} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0093 A:{SYNTHETIC, Splitter: B:34:0x0093} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String[] getExcludedDeviceNames() {
        Exception e;
        Throwable th;
        ArrayList<String> names = new ArrayList();
        File confFile = new File(Environment.getRootDirectory(), EXCLUDED_DEVICES_PATH);
        FileReader confreader = null;
        try {
            FileReader confreader2 = new FileReader(confFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(confreader2);
                XmlUtils.beginDocument(parser, "devices");
                while (true) {
                    XmlUtils.nextElement(parser);
                    if (!"device".equals(parser.getName())) {
                        break;
                    }
                    String name = parser.getAttributeValue(null, "name");
                    if (name != null) {
                        names.add(name);
                    }
                }
                if (confreader2 != null) {
                    try {
                        confreader2.close();
                    } catch (IOException e2) {
                    }
                }
                confreader = confreader2;
            } catch (FileNotFoundException e3) {
                confreader = confreader2;
                if (confreader != null) {
                }
                return (String[]) names.toArray(new String[names.size()]);
            } catch (Exception e4) {
                e = e4;
                confreader = confreader2;
                try {
                    Slog.e(TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
                    if (confreader != null) {
                    }
                    return (String[]) names.toArray(new String[names.size()]);
                } catch (Throwable th2) {
                    th = th2;
                    if (confreader != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                confreader = confreader2;
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e7) {
                }
            }
            return (String[]) names.toArray(new String[names.size()]);
        } catch (Exception e8) {
            e = e8;
            Slog.e(TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e9) {
                }
            }
            return (String[]) names.toArray(new String[names.size()]);
        }
        return (String[]) names.toArray(new String[names.size()]);
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

    protected Context getExternalContext() {
        return null;
    }

    private PointerIcon getPointerIcon() {
        return PointerIcon.getDefaultIcon(getExternalContext() != null ? getExternalContext() : this.mContext);
    }

    private String[] getKeyboardLayoutOverlay(InputDeviceIdentifier identifier) {
        if (!this.mSystemReady) {
            return null;
        }
        String keyboardLayoutDescriptor = getCurrentKeyboardLayoutForInputDevice(identifier);
        if (keyboardLayoutDescriptor == null) {
            return null;
        }
        final String[] result = new String[2];
        visitKeyboardLayout(keyboardLayoutDescriptor, new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                try {
                    result[0] = layout.getDescriptor();
                    result[1] = Streams.readFully(new InputStreamReader(resources.openRawResource(keyboardLayoutResId)));
                } catch (IOException e) {
                } catch (NotFoundException e2) {
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

    private void notifyANRWarning(int pid) {
        if (this.mWindowManagerCallbacks != null) {
            this.mWindowManagerCallbacks.notifyANRWarning(pid);
        }
    }
}
