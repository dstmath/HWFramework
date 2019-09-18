package com.android.server.input;

import android.app.IInputForwarder;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.Display;
import android.view.IInputFilter;
import android.view.IInputFilterHost;
import android.view.IWindow;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.PointerIcon;
import android.view.ViewConfiguration;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.DisplayThread;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.Watchdog;
import com.android.server.slice.SliceClientPermissions;
import com.huawei.android.hardware.input.IHwInputManager;
import com.huawei.android.hardware.input.IHwTHPEventListener;
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
import java.util.Objects;
import libcore.io.IoUtils;
import libcore.io.Streams;
import org.xmlpull.v1.XmlPullParser;

public class InputManagerService extends AbsInputManagerService implements Watchdog.Monitor, IHwInputManagerInner {
    public static final int BTN_MOUSE = 272;
    static final boolean DEBUG = false;
    protected static final boolean DEBUG_HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
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
    private final PersistentDataStore mDataStore = new PersistentDataStore();
    /* access modifiers changed from: private */
    public final File mDoubleTouchGestureEnableFile;
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
    private int mNextVibratorTokenValue;
    private NotificationManager mNotificationManager;
    public final long mPtr;
    private Toast mSwitchedKeyboardLayoutToast;
    private boolean mSystemReady;
    private final SparseArray<TabletModeChangedListenerRecord> mTabletModeChangedListeners = new SparseArray<>();
    private final Object mTabletModeLock = new Object();
    private final ArrayList<InputDevice> mTempFullKeyboards = new ArrayList<>();
    private final ArrayList<InputDevicesChangedListenerRecord> mTempInputDevicesChangedListenersToNotify = new ArrayList<>();
    private final List<TabletModeChangedListenerRecord> mTempTabletModeChangedListenersToNotify = new ArrayList();
    final boolean mUseDevInputEventForAudioJack;
    private Object mVibratorLock = new Object();
    private HashMap<IBinder, VibratorToken> mVibratorTokens = new HashMap<>();
    protected WindowManagerCallbacks mWindowManagerCallbacks;
    private WiredAccessoryCallbacks mWiredAccessoryCallbacks;

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
    }

    private final class InputDevicesChangedListenerRecord implements IBinder.DeathRecipient {
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
                        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.enabledInPad()) {
                            int unused = InputManagerService.nativeInjectInputEvent(InputManagerService.this.mPtr, event, 0, 0, 0, 0, 0, policyFlags | 67108864);
                        } else {
                            InputEvent inputEvent = event;
                            int unused2 = InputManagerService.nativeInjectInputEvent(InputManagerService.this.mPtr, inputEvent, HwPCUtils.getPCDisplayID(), 0, 0, 0, 0, policyFlags | 67108864);
                        }
                    }
                }
                return;
            }
            throw new IllegalArgumentException("event must not be null");
        }
    }

    private final class InputManagerHandler extends Handler {
        public InputManagerHandler(Looper looper) {
            super(looper, null, true);
        }

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

    private static final class KeyboardLayoutDescriptor {
        public String keyboardLayoutName;
        public String packageName;
        public String receiverName;

        private KeyboardLayoutDescriptor() {
        }

        public static String format(String packageName2, String receiverName2, String keyboardName) {
            return packageName2 + SliceClientPermissions.SliceAuthority.DELIMITER + receiverName2 + SliceClientPermissions.SliceAuthority.DELIMITER + keyboardName;
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

    private interface KeyboardLayoutVisitor {
        void visitKeyboardLayout(Resources resources, int i, KeyboardLayout keyboardLayout);
    }

    private final class LocalService extends InputManagerInternal {
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
                    IoUtils.closeQuietly(null);
                    throw th;
                }
                IoUtils.closeQuietly(writer);
            }
        }

        public void setMirrorLinkInputStatus(boolean status) {
            InputManagerService.nativeSetMirrorLinkInputStatus(InputManagerService.this.mPtr, status);
            Slog.i(InputManagerService.TAG, "setMirrorLinkInputStatus server status= " + status);
        }

        public void onKeyguardStateChanged(boolean showing) {
            InputManagerService.nativeSetKeyguardState(InputManagerService.this.mPtr, showing);
        }

        public boolean injectInputEvent(InputEvent event, int displayId, int mode, int appendPolicyFlag) {
            return InputManagerService.this.injectInputEventInternal(event, displayId, mode, appendPolicyFlag);
        }

        public void setDisplayMode(int displayMode, int subWidth, int mianWidth, int fullHight) {
            InputManagerService.nativeSetDisplayMode(InputManagerService.this.mPtr, displayMode, subWidth, mianWidth, fullHight);
        }

        public void setInputViewOrientation(int orientation) {
            InputManagerService.nativeSetInputViewOrientation(InputManagerService.this.mPtr, orientation);
        }
    }

    private final class TabletModeChangedListenerRecord implements IBinder.DeathRecipient {
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

    private final class VibratorToken implements IBinder.DeathRecipient {
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

        boolean hasLighterViewInPCCastMode();

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

    public interface WiredAccessoryCallbacks {
        void notifyWiredAccessoryChanged(long j, int i, int i2);

        void systemReady();
    }

    private static native void nativeCancelVibrate(long j, int i, int i2);

    private static native void nativeDisableInputDevice(long j, int i);

    private static native String nativeDump(long j);

    private static native void nativeEnableInputDevice(long j, int i);

    private static native int nativeGetKeyCodeState(long j, int i, int i2, int i3);

    private static native int nativeGetScanCodeState(long j, int i, int i2, int i3);

    private static native int nativeGetSwitchState(long j, int i, int i2, int i3);

    private static native boolean nativeHasKeys(long j, int i, int i2, int[] iArr, boolean[] zArr);

    private static native long nativeInit(InputManagerService inputManagerService, Context context, MessageQueue messageQueue);

    /* access modifiers changed from: private */
    public static native int nativeInjectInputEvent(long j, InputEvent inputEvent, int i, int i2, int i3, int i4, int i5, int i6);

    private static native boolean nativeIsInputDeviceEnabled(long j, int i);

    private static native void nativeMonitor(long j);

    private static native void nativeRegisterInputChannel(long j, InputChannel inputChannel, InputWindowHandle inputWindowHandle, boolean z);

    private static native void nativeReloadCalibration(long j);

    private static native void nativeReloadDeviceAliases(long j);

    private static native void nativeReloadKeyboardLayouts(long j);

    public static native void nativeReloadPointerIcons(long j, Context context);

    public static native void nativeResponseTouchEvent(long j, boolean z);

    private static native void nativeSetCustomPointerIcon(long j, PointerIcon pointerIcon);

    /* access modifiers changed from: private */
    public static native void nativeSetDisplayMode(long j, int i, int i2, int i3, int i4);

    private static native void nativeSetDisplayViewport(long j, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, String str);

    private static native void nativeSetFocusedApplication(long j, InputApplicationHandle inputApplicationHandle);

    public static native void nativeSetIawareGameMode(long j, int i);

    private static native void nativeSetInputDispatchMode(long j, boolean z, boolean z2);

    protected static native void nativeSetInputFilterEnabled(long j, boolean z);

    /* access modifiers changed from: private */
    public static native void nativeSetInputViewOrientation(long j, int i);

    private static native void nativeSetInputWindows(long j, InputWindowHandle[] inputWindowHandleArr);

    /* access modifiers changed from: private */
    public static native void nativeSetInteractive(long j, boolean z);

    /* access modifiers changed from: private */
    public static native void nativeSetKeyguardState(long j, boolean z);

    /* access modifiers changed from: private */
    public static native void nativeSetMirrorLinkInputStatus(long j, boolean z);

    private static native void nativeSetPointerCapture(long j, boolean z);

    private static native void nativeSetPointerIconType(long j, int i);

    private static native void nativeSetPointerSpeed(long j, int i);

    private static native void nativeSetShowTouches(long j, boolean z);

    private static native void nativeSetSystemUiVisibility(long j, int i);

    private static native void nativeSetVirtualDisplayViewports(long j, DisplayViewport[] displayViewportArr);

    private static native void nativeStart(long j);

    /* access modifiers changed from: private */
    public static native void nativeToggleCapsLock(long j, int i);

    private static native boolean nativeTransferTouchFocus(long j, InputChannel inputChannel, InputChannel inputChannel2);

    private static native void nativeUnregisterInputChannel(long j, InputChannel inputChannel);

    private static native void nativeVibrate(long j, int i, long[] jArr, int i2, int i3);

    public InputManagerService(Context context) {
        this.mHwIMSEx = HwServiceExFactory.getHwInputManagerServiceEx(this, context);
        this.mContext = context;
        this.mHandler = new InputManagerHandler(DisplayThread.get().getLooper());
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17957059);
        Slog.i(TAG, "Initializing input manager, mUseDevInputEventForAudioJack=" + this.mUseDevInputEventForAudioJack);
        this.mPtr = nativeInit(this, this.mContext, this.mHandler.getLooper().getQueue());
        String doubleTouchGestureEnablePath = context.getResources().getString(17039797);
        this.mDoubleTouchGestureEnableFile = TextUtils.isEmpty(doubleTouchGestureEnablePath) ? null : new File(doubleTouchGestureEnablePath);
        LocalServices.addService(InputManagerInternal.class, new LocalService());
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
        filter.addDataScheme("package");
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

    /* access modifiers changed from: private */
    public void reloadKeyboardLayouts() {
        nativeReloadKeyboardLayouts(this.mPtr);
    }

    /* access modifiers changed from: private */
    public void reloadDeviceAliases() {
        nativeReloadDeviceAliases(this.mPtr);
    }

    /* access modifiers changed from: private */
    public void setDisplayViewportsInternal(DisplayViewport defaultViewport, DisplayViewport externalTouchViewport, List<DisplayViewport> virtualTouchViewports) {
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
        DisplayViewport displayViewport = viewport;
        nativeSetDisplayViewport(this.mPtr, viewportType, displayViewport.displayId, displayViewport.orientation, displayViewport.logicalFrame.left, displayViewport.logicalFrame.top, displayViewport.logicalFrame.right, displayViewport.logicalFrame.bottom, displayViewport.physicalFrame.left, displayViewport.physicalFrame.top, displayViewport.physicalFrame.right, displayViewport.physicalFrame.bottom, displayViewport.deviceWidth, displayViewport.deviceHeight, displayViewport.uniqueId);
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
        if (inputChannelName != null) {
            InputChannel[] inputChannels = InputChannel.openInputChannelPair(inputChannelName);
            nativeRegisterInputChannel(this.mPtr, inputChannels[0], null, true);
            inputChannels[0].dispose();
            return inputChannels[1];
        }
        throw new IllegalArgumentException("inputChannelName must not be null.");
    }

    public void registerInputChannel(InputChannel inputChannel, InputWindowHandle inputWindowHandle) {
        if (inputChannel != null) {
            nativeRegisterInputChannel(this.mPtr, inputChannel, inputWindowHandle, false);
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

    public void setInputViewOrientation(int orientation) {
        nativeSetInputViewOrientation(this.mPtr, orientation);
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        return injectInputEventInternal(event, 0, mode);
    }

    /* access modifiers changed from: protected */
    public boolean injectInputEventInternal(InputEvent event, int displayId, int mode) {
        return injectInputEventInternal(event, displayId, mode, 0);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public boolean injectInputEventInternal(InputEvent event, int displayId, int mode, int appendPolicyFlag) {
        InputEvent inputEvent = event;
        int i = mode;
        if (inputEvent == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (i == 0 || i == 2 || i == 1) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                int result = nativeInjectInputEvent(this.mPtr, inputEvent, displayId, pid, uid, i, INJECTION_TIMEOUT_MILLIS, 134217728 | appendPolicyFlag);
                Binder.restoreCallingIdentity(ident);
                if (DEBUG_HWFLOW && (inputEvent instanceof KeyEvent)) {
                    KeyEvent myEvent = (KeyEvent) inputEvent;
                    Slog.i(TAG, "inject C=" + myEvent.getKeyCode() + ",A=" + myEvent.getAction() + ",F=" + myEvent.getFlags() + ",P=" + pid + ",U=" + uid + ",R=" + result);
                }
                if (result != 3) {
                    switch (result) {
                        case 0:
                            return true;
                        case 1:
                            Slog.w(TAG, "Input event injection from pid " + pid + " permission denied.");
                            throw new SecurityException("Injecting to another application requires INJECT_EVENTS permission");
                        default:
                            Slog.w(TAG, "Input event injection from pid " + pid + " failed.");
                            return false;
                    }
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
            for (InputDevice inputDevice : this.mInputDevices) {
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
                Flog.i(1506, "enableInputDevice deviceId=" + deviceId);
            }
            nativeEnableInputDevice(this.mPtr, deviceId);
            return;
        }
        throw new SecurityException("Requires DISABLE_INPUT_DEVICE permission");
    }

    public void disableInputDevice(int deviceId) {
        if (checkCallingPermission("android.permission.DISABLE_INPUT_DEVICE", "disableInputDevice()")) {
            if (DEBUG_HWFLOW) {
                Flog.i(1506, "disableInputDevice deviceId=" + deviceId);
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
    public void onInputDevicesChangedListenerDied(int pid) {
        synchronized (this.mInputDevicesLock) {
            this.mInputDevicesChangedListeners.remove(pid);
        }
    }

    public void inputdevicechanged() {
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0082, code lost:
        r0 = r3;
        r3 = r5;
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0085, code lost:
        if (r1 >= r0) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0087, code lost:
        r12.mTempInputDevicesChangedListenersToNotify.get(r1).notifyInputDevicesChanged(r3);
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0095, code lost:
        r12.mTempInputDevicesChangedListenersToNotify.clear();
        r4 = new java.util.ArrayList<>();
        r5 = r12.mTempFullKeyboards.size();
        r8 = r12.mDataStore;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00a8, code lost:
        monitor-enter(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a9, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00aa, code lost:
        if (r1 >= r5) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:?, code lost:
        r9 = r12.mTempFullKeyboards.get(r1);
        r10 = getCurrentKeyboardLayoutForInputDevice(r9.getIdentifier());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00bd, code lost:
        if (r10 != null) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00bf, code lost:
        r10 = getDefaultKeyboardLayout(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c4, code lost:
        if (r10 == null) goto L_0x00ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c6, code lost:
        setCurrentKeyboardLayoutForInputDevice(r9.getIdentifier(), r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00ce, code lost:
        if (r10 != null) goto L_0x00d3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00d0, code lost:
        r4.add(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00d3, code lost:
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00d6, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d8, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00db, code lost:
        if (r12.mHwIMSEx == null) goto L_0x00e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00dd, code lost:
        r12.mHwIMSEx.checkHasShowDismissSoftInputAlertDialog(r4.isEmpty());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00e8, code lost:
        if (r12.mNotificationManager == null) goto L_0x010c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ee, code lost:
        if (r4.isEmpty() != false) goto L_0x0105;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f4, code lost:
        if (r4.size() <= 1) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00f6, code lost:
        showMissingKeyboardLayoutNotification(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00fb, code lost:
        showMissingKeyboardLayoutNotification(r4.get(0));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0107, code lost:
        if (r12.mKeyboardLayoutNotificationShown == false) goto L_0x010c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0109, code lost:
        hideMissingKeyboardLayoutNotification();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x010c, code lost:
        r12.mTempFullKeyboards.clear();
        inputdevicechanged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0114, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0116, code lost:
        throw r1;
     */
    public void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
        this.mTempInputDevicesChangedListenersToNotify.clear();
        this.mTempFullKeyboards.clear();
        synchronized (this.mInputDevicesLock) {
            try {
                if (this.mInputDevicesChangedPending) {
                    this.mInputDevicesChangedPending = false;
                    int numListeners = this.mInputDevicesChangedListeners.size();
                    for (int i = 0; i < numListeners; i++) {
                        this.mTempInputDevicesChangedListenersToNotify.add(this.mInputDevicesChangedListeners.valueAt(i));
                    }
                    int numDevices = this.mInputDevices.length;
                    int[] deviceIdAndGeneration = new int[(numDevices * 2)];
                    int numFullKeyboardsAdded = 0;
                    int i2 = 0;
                    while (i2 < numDevices) {
                        try {
                            InputDevice inputDevice = this.mInputDevices[i2];
                            deviceIdAndGeneration[i2 * 2] = inputDevice.getId();
                            deviceIdAndGeneration[(i2 * 2) + 1] = inputDevice.getGeneration();
                            if (!inputDevice.isVirtual() && inputDevice.isFullKeyboard()) {
                                if (!containsInputDeviceWithDescriptor(oldInputDevices, inputDevice.getDescriptor())) {
                                    int numFullKeyboardsAdded2 = numFullKeyboardsAdded + 1;
                                    try {
                                        this.mTempFullKeyboards.add(numFullKeyboardsAdded, inputDevice);
                                        numFullKeyboardsAdded = numFullKeyboardsAdded2;
                                    } catch (Throwable th) {
                                        th = th;
                                        int i3 = numFullKeyboardsAdded2;
                                        throw th;
                                    }
                                } else {
                                    this.mTempFullKeyboards.add(inputDevice);
                                }
                            }
                            i2++;
                        } catch (Throwable th2) {
                            th = th2;
                            int i4 = numFullKeyboardsAdded;
                            throw th;
                        }
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private String getDefaultKeyboardLayout(final InputDevice d) {
        final Locale systemLocale = this.mContext.getResources().getConfiguration().locale;
        if (TextUtils.isEmpty(systemLocale.getLanguage())) {
            return null;
        }
        final List<KeyboardLayout> layouts = new ArrayList<>();
        visitAllKeyboardLayouts(new KeyboardLayoutVisitor() {
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                if (layout.getVendorId() == d.getVendorId() && layout.getProductId() == d.getProductId()) {
                    LocaleList locales = layout.getLocales();
                    int numLocales = locales.size();
                    int localeIndex = 0;
                    while (true) {
                        if (localeIndex >= numLocales) {
                            break;
                        } else if (InputManagerService.isCompatibleLocale(systemLocale, locales.get(localeIndex))) {
                            layouts.add(layout);
                            break;
                        } else {
                            localeIndex++;
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
    public void onTabletModeChangedListenerDied(int pid) {
        synchronized (this.mTabletModeLock) {
            this.mTabletModeChangedListeners.remove(pid);
        }
    }

    /* access modifiers changed from: private */
    public void deliverTabletModeChanged(long whenNanos, boolean inTabletMode) {
        int numListeners;
        int i;
        this.mTempTabletModeChangedListenersToNotify.clear();
        synchronized (this.mTabletModeLock) {
            numListeners = this.mTabletModeChangedListeners.size();
            i = 0;
            for (int i2 = 0; i2 < numListeners; i2++) {
                this.mTempTabletModeChangedListenersToNotify.add(this.mTabletModeChangedListeners.valueAt(i2));
            }
        }
        int numListeners2 = numListeners;
        while (true) {
            int i3 = i;
            if (i3 < numListeners2) {
                this.mTempTabletModeChangedListenersToNotify.get(i3).notifyTabletModeChanged(whenNanos, inTabletMode);
                i = i3 + 1;
            } else {
                return;
            }
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
            this.mNotificationManager.notifyAsUser(null, 19, new Notification.Builder(this.mContext, SystemNotificationChannels.PHYSICAL_KEYBOARD).setContentTitle(r.getString(17041064)).setContentText(r.getString(17041063)).setContentIntent(keyboardLayoutIntent).setSmallIcon(17302751).setColor(this.mContext.getColor(17170784)).build(), UserHandle.ALL);
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
    public void updateKeyboardLayouts() {
        final HashSet<String> availableKeyboardLayouts = new HashSet<>();
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
                throw th;
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
            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                list.add(layout);
            }
        });
        return (KeyboardLayout[]) list.toArray(new KeyboardLayout[list.size()]);
    }

    public KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        String[] enabledLayoutDescriptors = getEnabledKeyboardLayoutsForInputDevice(identifier);
        ArrayList<KeyboardLayout> enabledLayouts = new ArrayList<>(enabledLayoutDescriptors.length);
        ArrayList<KeyboardLayout> potentialLayouts = new ArrayList<>();
        final String[] strArr = enabledLayoutDescriptors;
        final ArrayList<KeyboardLayout> arrayList = enabledLayouts;
        final InputDeviceIdentifier inputDeviceIdentifier = identifier;
        final ArrayList<KeyboardLayout> arrayList2 = potentialLayouts;
        AnonymousClass7 r0 = new KeyboardLayoutVisitor() {
            boolean mHasSeenDeviceSpecificLayout;

            public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
                String[] strArr = strArr;
                int length = strArr.length;
                int i = 0;
                while (i < length) {
                    String s = strArr[i];
                    if (s == null || !s.equals(layout.getDescriptor())) {
                        i++;
                    } else {
                        arrayList.add(layout);
                        return;
                    }
                }
                if (layout.getVendorId() == inputDeviceIdentifier.getVendorId() && layout.getProductId() == inputDeviceIdentifier.getProductId()) {
                    if (!this.mHasSeenDeviceSpecificLayout) {
                        this.mHasSeenDeviceSpecificLayout = true;
                        arrayList2.clear();
                    }
                    arrayList2.add(layout);
                } else if (layout.getVendorId() == -1 && layout.getProductId() == -1 && !this.mHasSeenDeviceSpecificLayout) {
                    arrayList2.add(layout);
                }
            }
        };
        visitAllKeyboardLayouts(r0);
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
        XmlResourceParser parser;
        XmlResourceParser parser2;
        CharSequence receiverLabel;
        int configResId;
        Bundle metaData;
        int i;
        XmlResourceParser parser3;
        Resources resources;
        XmlResourceParser parser4;
        TypedArray a;
        String name;
        String label;
        String languageTags;
        int keyboardLayoutResId;
        LocaleList locales;
        int vid;
        XmlResourceParser parser5;
        XmlResourceParser receiverLabel2;
        XmlResourceParser xmlResourceParser;
        String descriptor;
        KeyboardLayout keyboardLayout;
        PackageManager packageManager = pm;
        ActivityInfo activityInfo = receiver;
        String str = keyboardName;
        Bundle metaData2 = activityInfo.metaData;
        if (metaData2 != null) {
            int configResId2 = metaData2.getInt("android.hardware.input.metadata.KEYBOARD_LAYOUTS");
            if (configResId2 == 0) {
                Slog.w(TAG, "Missing meta-data 'android.hardware.input.metadata.KEYBOARD_LAYOUTS' on receiver " + activityInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityInfo.name);
                return;
            }
            CharSequence receiverLabel3 = activityInfo.loadLabel(packageManager);
            String collection = receiverLabel3 != null ? receiverLabel3.toString() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            int i2 = 1;
            int i3 = 0;
            int priority = (activityInfo.applicationInfo.flags & 1) != 0 ? requestedPriority : 0;
            try {
                Resources resources2 = packageManager.getResourcesForApplication(activityInfo.applicationInfo);
                XmlResourceParser parser6 = resources2.getXml(configResId2);
                try {
                    parser2 = receiverLabel3;
                    XmlUtils.beginDocument(parser6, "keyboard-layouts");
                    XmlResourceParser receiverLabel4 = receiverLabel3;
                    while (true) {
                        XmlUtils.nextElement(parser6);
                        String element = parser6.getName();
                        if (element == null) {
                            try {
                                break;
                            } catch (Exception e) {
                                ex = e;
                                KeyboardLayoutVisitor keyboardLayoutVisitor = visitor;
                                Bundle bundle = metaData2;
                                int i4 = configResId2;
                                CharSequence charSequence = receiverLabel4;
                                Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + activityInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityInfo.name, ex);
                            }
                        } else {
                            parser2 = receiverLabel4;
                            if (element.equals("keyboard-layout")) {
                                TypedArray a2 = resources2.obtainAttributes(parser6, R.styleable.KeyboardLayout);
                                try {
                                    parser4 = receiverLabel4;
                                    name = a2.getString(i2);
                                    label = a2.getString(i3);
                                    int keyboardLayoutResId2 = a2.getResourceId(2, i3);
                                    languageTags = a2.getString(3);
                                    keyboardLayoutResId = keyboardLayoutResId2;
                                    locales = getLocalesFromLanguageTags(languageTags);
                                    metaData = metaData2;
                                    try {
                                        parser4 = receiverLabel4;
                                        a = a2;
                                        vid = a2.getInt(5, -1);
                                        configResId = configResId2;
                                    } catch (Throwable th) {
                                        th = th;
                                        int i5 = configResId2;
                                        CharSequence charSequence2 = receiverLabel4;
                                        Resources resources3 = resources2;
                                        XmlResourceParser xmlResourceParser2 = parser6;
                                        a = a2;
                                        String str2 = element;
                                        KeyboardLayoutVisitor keyboardLayoutVisitor2 = visitor;
                                        a.recycle();
                                        throw th;
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    Bundle bundle2 = metaData2;
                                    int i6 = configResId2;
                                    CharSequence charSequence3 = receiverLabel4;
                                    Resources resources4 = resources2;
                                    XmlResourceParser xmlResourceParser3 = parser6;
                                    a = a2;
                                    String str3 = element;
                                    KeyboardLayoutVisitor keyboardLayoutVisitor3 = visitor;
                                    a.recycle();
                                    throw th;
                                }
                                try {
                                    parser4 = receiverLabel4;
                                    String str4 = element;
                                    int pid = a.getInt(4, -1);
                                    if (name == null || label == null) {
                                        receiverLabel = receiverLabel4;
                                        resources = resources2;
                                        receiverLabel2 = parser6;
                                        String str5 = languageTags;
                                        i = 0;
                                        KeyboardLayoutVisitor keyboardLayoutVisitor4 = visitor;
                                    } else if (keyboardLayoutResId == 0) {
                                        receiverLabel = receiverLabel4;
                                        resources = resources2;
                                        receiverLabel2 = parser6;
                                        String str6 = languageTags;
                                        i = 0;
                                        KeyboardLayoutVisitor keyboardLayoutVisitor5 = visitor;
                                    } else {
                                        try {
                                            receiverLabel = receiverLabel4;
                                            try {
                                                xmlResourceParser = parser6;
                                                descriptor = KeyboardLayoutDescriptor.format(activityInfo.packageName, activityInfo.name, name);
                                                if (str != null) {
                                                    try {
                                                        if (!name.equals(str)) {
                                                            resources = resources2;
                                                            i = 0;
                                                            KeyboardLayoutVisitor keyboardLayoutVisitor6 = visitor;
                                                            parser5 = xmlResourceParser;
                                                            parser4 = parser5;
                                                            a.recycle();
                                                            parser3 = parser5;
                                                        }
                                                    } catch (Throwable th3) {
                                                        th = th3;
                                                        Resources resources5 = resources2;
                                                        KeyboardLayoutVisitor keyboardLayoutVisitor7 = visitor;
                                                        a.recycle();
                                                        throw th;
                                                    }
                                                }
                                            } catch (Throwable th4) {
                                                th = th4;
                                                Resources resources6 = resources2;
                                                XmlResourceParser xmlResourceParser4 = parser6;
                                                KeyboardLayoutVisitor keyboardLayoutVisitor8 = visitor;
                                                a.recycle();
                                                throw th;
                                            }
                                            try {
                                                resources = resources2;
                                                keyboardLayout = keyboardLayout;
                                                String str7 = languageTags;
                                                i = 0;
                                            } catch (Throwable th5) {
                                                th = th5;
                                                Resources resources7 = resources2;
                                                KeyboardLayoutVisitor keyboardLayoutVisitor9 = visitor;
                                                a.recycle();
                                                throw th;
                                            }
                                        } catch (Throwable th6) {
                                            th = th6;
                                            CharSequence charSequence4 = receiverLabel4;
                                            Resources resources8 = resources2;
                                            XmlResourceParser xmlResourceParser5 = parser6;
                                            KeyboardLayoutVisitor keyboardLayoutVisitor10 = visitor;
                                            a.recycle();
                                            throw th;
                                        }
                                        try {
                                            keyboardLayout = new KeyboardLayout(descriptor, label, collection, priority, locales, vid, pid);
                                            parser4 = xmlResourceParser;
                                            visitor.visitKeyboardLayout(resources, keyboardLayoutResId, keyboardLayout);
                                            parser5 = xmlResourceParser;
                                            parser4 = parser5;
                                            a.recycle();
                                            parser3 = parser5;
                                        } catch (Throwable th7) {
                                            th = th7;
                                            a.recycle();
                                            throw th;
                                        }
                                    }
                                    Slog.w(TAG, "Missing required 'name', 'label' or 'keyboardLayout' attributes in keyboard layout resource from receiver " + activityInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityInfo.name);
                                    parser5 = receiverLabel2;
                                    parser4 = parser5;
                                    a.recycle();
                                    parser3 = parser5;
                                } catch (Throwable th8) {
                                    th = th8;
                                    parser = parser4;
                                    try {
                                        parser.close();
                                        throw th;
                                    } catch (Exception e2) {
                                        ex = e2;
                                        Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + activityInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityInfo.name, ex);
                                    }
                                }
                            } else {
                                metaData = metaData2;
                                configResId = configResId2;
                                receiverLabel = receiverLabel4;
                                resources = resources2;
                                parser3 = parser6;
                                i = i3;
                                KeyboardLayoutVisitor keyboardLayoutVisitor11 = visitor;
                                Slog.w(TAG, "Skipping unrecognized element '" + element + "' in keyboard layout resource from receiver " + activityInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityInfo.name);
                            }
                            resources2 = resources;
                            parser6 = parser3;
                            i3 = i;
                            metaData2 = metaData;
                            configResId2 = configResId;
                            receiverLabel4 = receiverLabel;
                            PackageManager packageManager2 = pm;
                            str = keyboardName;
                            i2 = 1;
                        }
                    }
                    parser6.close();
                    KeyboardLayoutVisitor keyboardLayoutVisitor12 = visitor;
                    Bundle bundle3 = metaData2;
                    int i7 = configResId2;
                    CharSequence charSequence5 = receiverLabel4;
                } catch (Throwable th9) {
                    th = th9;
                    Bundle bundle4 = metaData2;
                    int i8 = configResId2;
                    CharSequence charSequence6 = parser2;
                    Resources resources9 = resources2;
                    parser = parser6;
                    KeyboardLayoutVisitor keyboardLayoutVisitor13 = visitor;
                    parser.close();
                    throw th;
                }
            } catch (Exception e3) {
                ex = e3;
                KeyboardLayoutVisitor keyboardLayoutVisitor14 = visitor;
                Bundle bundle5 = metaData2;
                int i9 = configResId2;
                CharSequence charSequence7 = receiverLabel3;
                Slog.w(TAG, "Could not parse keyboard layout resource from receiver " + activityInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityInfo.name, ex);
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
    public void handleSwitchKeyboardLayout(int deviceId, int direction) {
        boolean changed;
        String keyboardLayoutDescriptor;
        InputDevice device = getInputDevice(deviceId);
        if (device != null) {
            String key = getLayoutDescriptor(device.getIdentifier());
            synchronized (this.mDataStore) {
                try {
                    changed = this.mDataStore.switchKeyboardLayout(key, direction);
                    keyboardLayoutDescriptor = this.mDataStore.getCurrentKeyboardLayout(key);
                    this.mDataStore.saveIfNeeded();
                } catch (Throwable th) {
                    this.mDataStore.saveIfNeeded();
                    throw th;
                }
            }
            if (changed) {
                if (this.mSwitchedKeyboardLayoutToast != null) {
                    this.mSwitchedKeyboardLayoutToast.cancel();
                    this.mSwitchedKeyboardLayoutToast = null;
                }
                if (keyboardLayoutDescriptor != null) {
                    KeyboardLayout keyboardLayout = getKeyboardLayout(keyboardLayoutDescriptor);
                    if (keyboardLayout != null) {
                        this.mSwitchedKeyboardLayoutToast = Toast.makeText(this.mContext, keyboardLayout.getLabel(), 0);
                        this.mSwitchedKeyboardLayoutToast.show();
                    }
                }
                reloadKeyboardLayouts();
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
            StringBuilder sb = new StringBuilder();
            sb.append("requestPointerCapture: already ");
            sb.append(enabled ? "enabled" : "disabled");
            Slog.i(TAG, sb.toString());
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

    public void setInputDispatchMode(boolean enabled, boolean frozen) {
        nativeSetInputDispatchMode(this.mPtr, enabled, frozen);
    }

    public void setSystemUiVisibility(int visibility) {
        nativeSetSystemUiVisibility(this.mPtr, visibility);
    }

    public void responseTouchEvent(boolean status) {
        nativeResponseTouchEvent(this.mPtr, status);
    }

    public void setIawareGameMode(int game_mode) {
        nativeSetIawareGameMode(this.mPtr, game_mode);
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
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("pointer_speed"), true, new ContentObserver(this.mHandler) {
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
            public void onChange(boolean selfChange) {
                InputManagerService.this.updateShowTouchesFromSettings();
            }
        }, -1);
    }

    public void updateAccessibilityLargePointerFromSettings() {
        int accessibilityConfig = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_large_pointer_icon", 0, -2);
        boolean z = true;
        PointerIcon.setUseLargeIcons(accessibilityConfig == 1);
        if (HwPCUtils.isPcCastModeInServer()) {
            nativeReloadPointerIcons(this.mPtr, getExternalContext());
            StringBuilder sb = new StringBuilder();
            sb.append("update Pointer, isDefaultDisplay:");
            if (getExternalContext() != null) {
                z = false;
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
        if (icon != null) {
            nativeSetCustomPointerIcon(this.mPtr, icon);
        }
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

    public IInputForwarder createInputForwarder(int displayId) throws RemoteException {
        if (checkCallingPermission("android.permission.INJECT_EVENTS", "createInputForwarder()")) {
            Display display = ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(displayId);
            if (display == null) {
                throw new IllegalArgumentException("Can't create input forwarder for non-existent displayId: " + displayId);
            } else if (Binder.getCallingUid() == display.getOwnerUid()) {
                return new InputForwarder(displayId);
            } else {
                throw new SecurityException("Only owner of the display can forward input events to it.");
            }
        } else {
            throw new SecurityException("Requires INJECT_EVENTS permission");
        }
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
        if (switchMask != false && true) {
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

    private void notifyInputChannelBroken(InputWindowHandle inputWindowHandle) {
        this.mWindowManagerCallbacks.notifyInputChannelBroken(inputWindowHandle);
    }

    private long notifyANR(InputApplicationHandle inputApplicationHandle, InputWindowHandle inputWindowHandle, String reason) {
        return this.mWindowManagerCallbacks.notifyANR(inputApplicationHandle, inputWindowHandle, reason);
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

    private int interceptMotionBeforeQueueingNonInteractive(long whenNanos, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptMotionBeforeQueueingNonInteractive(whenNanos, policyFlags);
    }

    /* access modifiers changed from: protected */
    public long interceptKeyBeforeDispatching(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.interceptKeyBeforeDispatching(focus, event, policyFlags);
    }

    /* access modifiers changed from: package-private */
    public KeyEvent dispatchUnhandledKey(InputWindowHandle focus, KeyEvent event, int policyFlags) {
        return this.mWindowManagerCallbacks.dispatchUnhandledKey(focus, event, policyFlags);
    }

    private boolean checkInjectEventsPermission(int injectorPid, int injectorUid) {
        return this.mContext.checkPermission("android.permission.INJECT_EVENTS", injectorPid, injectorUid) == 0;
    }

    private int getVirtualKeyQuietTimeMillis() {
        return this.mContext.getResources().getInteger(17694880);
    }

    private String[] getExcludedDeviceNames() {
        ArrayList<String> names = new ArrayList<>();
        FileReader confreader = null;
        try {
            confreader = new FileReader(new File(Environment.getRootDirectory(), EXCLUDED_DEVICES_PATH));
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(confreader);
            XmlUtils.beginDocument(parser, "devices");
            while (true) {
                XmlUtils.nextElement(parser);
                if (!"device".equals(parser.getName())) {
                    try {
                        break;
                    } catch (IOException e) {
                    }
                } else {
                    String name = parser.getAttributeValue(null, com.android.server.pm.Settings.ATTR_NAME);
                    if (name != null) {
                        names.add(name);
                    }
                }
            }
            confreader.close();
        } catch (FileNotFoundException e2) {
            if (confreader != null) {
                confreader.close();
            }
        } catch (Exception e3) {
            Slog.e(TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e3);
            if (confreader != null) {
                confreader.close();
            }
        } catch (Throwable th) {
            if (confreader != null) {
                try {
                    confreader.close();
                } catch (IOException e4) {
                }
            }
            throw th;
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

    /* access modifiers changed from: protected */
    public Context getExternalContext() {
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

    private void notifyANRWarning(int pid) {
        if (this.mWindowManagerCallbacks != null) {
            this.mWindowManagerCallbacks.notifyANRWarning(pid);
        }
    }

    public void reportBbFilterPoint(String eventMsg, int eventID) {
        Flog.bdReport(this.mContext, eventID, eventMsg);
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.input.InputManagerService$HwInnerInputManagerService, android.os.IBinder] */
    public IBinder getHwInnerService() {
        return this.mHwInnerService;
    }

    public void setInputEventStrategy(boolean isStartInputEventControl) {
    }
}
