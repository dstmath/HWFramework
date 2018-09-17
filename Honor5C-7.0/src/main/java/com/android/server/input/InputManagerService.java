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
import android.os.ShellCommand;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import android.view.IInputFilter;
import android.view.IInputFilterHost.Stub;
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
import com.android.internal.os.SomeArgs;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
    final Context mContext;
    private InputMethodSubtypeHandle mCurrentImeHandle;
    HwCustInputManagerService mCust;
    private final PersistentDataStore mDataStore;
    private final InputManagerHandler mHandler;
    private InputDevice[] mInputDevices;
    private final SparseArray<InputDevicesChangedListenerRecord> mInputDevicesChangedListeners;
    private boolean mInputDevicesChangedPending;
    private Object mInputDevicesLock;
    IInputFilter mInputFilter;
    InputFilterHost mInputFilterHost;
    final Object mInputFilterLock;
    private boolean mKeyboardLayoutNotificationShown;
    private int mNextVibratorTokenValue;
    private NotificationManager mNotificationManager;
    private final long mPtr;
    private boolean mSystemReady;
    private final SparseArray<TabletModeChangedListenerRecord> mTabletModeChangedListeners;
    private final Object mTabletModeLock;
    private final ArrayList<InputDevice> mTempFullKeyboards;
    private final ArrayList<InputDevicesChangedListenerRecord> mTempInputDevicesChangedListenersToNotify;
    private final List<TabletModeChangedListenerRecord> mTempTabletModeChangedListenersToNotify;
    final boolean mUseDevInputEventForAudioJack;
    private Object mVibratorLock;
    private HashMap<IBinder, VibratorToken> mVibratorTokens;
    private WindowManagerCallbacks mWindowManagerCallbacks;
    private WiredAccessoryCallbacks mWiredAccessoryCallbacks;

    public interface WiredAccessoryCallbacks {
        void notifyWiredAccessoryChanged(long j, int i, int i2);

        void systemReady();
    }

    /* renamed from: com.android.server.input.InputManagerService.10 */
    class AnonymousClass10 extends ContentObserver {
        AnonymousClass10(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            InputManagerService.this.updatePointerSpeedFromSettings();
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.11 */
    class AnonymousClass11 extends ContentObserver {
        AnonymousClass11(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            InputManagerService.this.updateShowTouchesFromSettings();
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.12 */
    class AnonymousClass12 extends ContentObserver {
        AnonymousClass12(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            InputManagerService.this.updateAccessibilityLargePointerFromSettings();
        }
    }

    private interface KeyboardLayoutVisitor {
        void visitKeyboardLayout(Resources resources, int i, KeyboardLayout keyboardLayout);
    }

    /* renamed from: com.android.server.input.InputManagerService.13 */
    class AnonymousClass13 implements KeyboardLayoutVisitor {
        final /* synthetic */ PrintWriter val$pw;

        AnonymousClass13(PrintWriter val$pw) {
            this.val$pw = val$pw;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            this.val$pw.println("    \"" + layout + "\": " + layout.getDescriptor());
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.14 */
    class AnonymousClass14 implements KeyboardLayoutVisitor {
        final /* synthetic */ String[] val$result;

        AnonymousClass14(String[] val$result) {
            this.val$result = val$result;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            try {
                this.val$result[InputManagerService.SW_LID] = layout.getDescriptor();
                this.val$result[InputManagerService.SW_TABLET_MODE] = Streams.readFully(new InputStreamReader(resources.openRawResource(keyboardLayoutResId)));
            } catch (IOException e) {
            } catch (NotFoundException e2) {
            }
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.4 */
    class AnonymousClass4 implements KeyboardLayoutVisitor {
        final /* synthetic */ InputDevice val$d;
        final /* synthetic */ List val$layouts;
        final /* synthetic */ Locale val$systemLocale;

        AnonymousClass4(InputDevice val$d, Locale val$systemLocale, List val$layouts) {
            this.val$d = val$d;
            this.val$systemLocale = val$systemLocale;
            this.val$layouts = val$layouts;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            if (layout.getVendorId() == this.val$d.getVendorId() && layout.getProductId() == this.val$d.getProductId()) {
                LocaleList locales = layout.getLocales();
                int numLocales = locales.size();
                for (int localeIndex = InputManagerService.SW_LID; localeIndex < numLocales; localeIndex += InputManagerService.SW_TABLET_MODE) {
                    if (InputManagerService.isCompatibleLocale(this.val$systemLocale, locales.get(localeIndex))) {
                        this.val$layouts.add(layout);
                        break;
                    }
                }
            }
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.5 */
    class AnonymousClass5 implements KeyboardLayoutVisitor {
        final /* synthetic */ HashSet val$availableKeyboardLayouts;

        AnonymousClass5(HashSet val$availableKeyboardLayouts) {
            this.val$availableKeyboardLayouts = val$availableKeyboardLayouts;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            this.val$availableKeyboardLayouts.add(layout.getDescriptor());
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.6 */
    class AnonymousClass6 implements KeyboardLayoutVisitor {
        final /* synthetic */ ArrayList val$list;

        AnonymousClass6(ArrayList val$list) {
            this.val$list = val$list;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            this.val$list.add(layout);
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.7 */
    class AnonymousClass7 implements KeyboardLayoutVisitor {
        boolean mHasSeenDeviceSpecificLayout;
        final /* synthetic */ String[] val$enabledLayoutDescriptors;
        final /* synthetic */ ArrayList val$enabledLayouts;
        final /* synthetic */ InputDeviceIdentifier val$identifier;
        final /* synthetic */ ArrayList val$potentialLayouts;

        AnonymousClass7(String[] val$enabledLayoutDescriptors, ArrayList val$enabledLayouts, InputDeviceIdentifier val$identifier, ArrayList val$potentialLayouts) {
            this.val$enabledLayoutDescriptors = val$enabledLayoutDescriptors;
            this.val$enabledLayouts = val$enabledLayouts;
            this.val$identifier = val$identifier;
            this.val$potentialLayouts = val$potentialLayouts;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            String[] strArr = this.val$enabledLayoutDescriptors;
            int i = InputManagerService.SW_LID;
            int length = strArr.length;
            while (i < length) {
                String s = strArr[i];
                if (s == null || !s.equals(layout.getDescriptor())) {
                    i += InputManagerService.SW_TABLET_MODE;
                } else {
                    this.val$enabledLayouts.add(layout);
                    return;
                }
            }
            if (layout.getVendorId() == this.val$identifier.getVendorId() && layout.getProductId() == this.val$identifier.getProductId()) {
                if (!this.mHasSeenDeviceSpecificLayout) {
                    this.mHasSeenDeviceSpecificLayout = true;
                    this.val$potentialLayouts.clear();
                }
                this.val$potentialLayouts.add(layout);
            } else if (layout.getVendorId() == InputManagerService.KEY_STATE_UNKNOWN && layout.getProductId() == InputManagerService.KEY_STATE_UNKNOWN && !this.mHasSeenDeviceSpecificLayout) {
                this.val$potentialLayouts.add(layout);
            }
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.8 */
    class AnonymousClass8 implements KeyboardLayoutVisitor {
        final /* synthetic */ KeyboardLayout[] val$result;

        AnonymousClass8(KeyboardLayout[] val$result) {
            this.val$result = val$result;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            this.val$result[InputManagerService.SW_LID] = layout;
        }
    }

    /* renamed from: com.android.server.input.InputManagerService.9 */
    class AnonymousClass9 implements KeyboardLayoutVisitor {
        final /* synthetic */ KeyboardLayout[] val$result;

        AnonymousClass9(KeyboardLayout[] val$result) {
            this.val$result = val$result;
        }

        public void visitKeyboardLayout(Resources resources, int keyboardLayoutResId, KeyboardLayout layout) {
            this.val$result[InputManagerService.SW_LID] = layout;
        }
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
                    InputManagerService.nativeInjectInputEvent(InputManagerService.this.mPtr, event, InputManagerService.SW_LID, InputManagerService.SW_LID, InputManagerService.SW_LID, InputManagerService.SW_LID, InputManagerService.SW_LID, policyFlags | 67108864);
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
                case InputManagerService.SW_TABLET_MODE /*1*/:
                    InputManagerService.this.deliverInputDevicesChanged((InputDevice[]) msg.obj);
                case InputManagerService.SW_TABLET_MODE_BIT /*2*/:
                    args = msg.obj;
                    InputManagerService.this.handleSwitchKeyboardLayout((InputDeviceIdentifier) args.arg1, (InputMethodSubtypeHandle) args.arg2);
                case InputManagerService.MSG_RELOAD_KEYBOARD_LAYOUTS /*3*/:
                    InputManagerService.this.reloadKeyboardLayouts();
                case InputManagerService.SW_MICROPHONE_INSERT /*4*/:
                    InputManagerService.this.updateKeyboardLayouts();
                case InputManagerService.MSG_RELOAD_DEVICE_ALIASES /*5*/:
                    InputManagerService.this.reloadDeviceAliases();
                case InputManagerService.SW_LINEOUT_INSERT /*6*/:
                    args = (SomeArgs) msg.obj;
                    InputManagerService.this.deliverTabletModeChanged((((long) args.argi1) & 4294967295L) | (((long) args.argi2) << 32), ((Boolean) args.arg1).booleanValue());
                case InputManagerService.SW_JACK_PHYSICAL_INSERT /*7*/:
                    int userId = msg.arg1;
                    args = (SomeArgs) msg.obj;
                    InputMethodInfo inputMethodInfo = args.arg1;
                    InputMethodSubtype subtype = args.arg2;
                    args.recycle();
                    InputManagerService.this.handleSwitchInputMethodSubtype(userId, inputMethodInfo, subtype);
                default:
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
            if (pos < 0 || pos + InputManagerService.SW_TABLET_MODE == descriptor.length()) {
                return null;
            }
            int pos2 = descriptor.indexOf(47, pos + InputManagerService.SW_TABLET_MODE);
            if (pos2 < pos + InputManagerService.SW_TABLET_MODE_BIT || pos2 + InputManagerService.SW_TABLET_MODE == descriptor.length()) {
                return null;
            }
            KeyboardLayoutDescriptor result = new KeyboardLayoutDescriptor();
            result.packageName = descriptor.substring(InputManagerService.SW_LID, pos);
            result.receiverName = descriptor.substring(pos + InputManagerService.SW_TABLET_MODE, pos2);
            result.keyboardLayoutName = descriptor.substring(pos2 + InputManagerService.SW_TABLET_MODE);
            return result;
        }
    }

    private final class LocalService extends InputManagerInternal {
        private LocalService() {
        }

        public void setDisplayViewports(DisplayViewport defaultViewport, DisplayViewport externalTouchViewport) {
            InputManagerService.this.setDisplayViewportsInternal(defaultViewport, externalTouchViewport);
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
            InputManagerService.this.mHandler.obtainMessage(InputManagerService.SW_JACK_PHYSICAL_INSERT, userId, InputManagerService.SW_LID, someArgs).sendToTarget();
        }

        public void toggleCapsLock(int deviceId) {
            InputManagerService.nativeToggleCapsLock(InputManagerService.this.mPtr, deviceId);
        }
    }

    private class Shell extends ShellCommand {
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

    private static native void nativeReloadPointerIcons(long j);

    private static native void nativeSetCustomPointerIcon(long j, PointerIcon pointerIcon);

    private static native void nativeSetDisplayViewport(long j, boolean z, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12);

    private static native void nativeSetFocusedApplication(long j, InputApplicationHandle inputApplicationHandle);

    private static native void nativeSetInputDispatchMode(long j, boolean z, boolean z2);

    private static native void nativeSetInputFilterEnabled(long j, boolean z);

    private static native void nativeSetInputWindows(long j, InputWindowHandle[] inputWindowHandleArr);

    private static native void nativeSetInteractive(long j, boolean z);

    private static native void nativeSetMirrorLinkInputStatus(long j, boolean z);

    private static native void nativeSetPointerIconType(long j, int i);

    private static native void nativeSetPointerSpeed(long j, int i);

    private static native void nativeSetShowTouches(long j, boolean z);

    private static native void nativeSetSystemUiVisibility(long j, int i);

    private static native void nativeStart(long j);

    private static native void nativeToggleCapsLock(long j, int i);

    private static native boolean nativeTransferTouchFocus(long j, InputChannel inputChannel, InputChannel inputChannel2);

    private static native void nativeUnregisterInputChannel(long j, InputChannel inputChannel);

    private static native void nativeVibrate(long j, int i, long[] jArr, int i2, int i3);

    public InputManagerService(Context context) {
        this.mTabletModeLock = new Object();
        this.mTabletModeChangedListeners = new SparseArray();
        this.mTempTabletModeChangedListenersToNotify = new ArrayList();
        this.mDataStore = new PersistentDataStore();
        this.mInputDevicesLock = new Object();
        this.mInputDevices = new InputDevice[SW_LID];
        this.mInputDevicesChangedListeners = new SparseArray();
        this.mTempInputDevicesChangedListenersToNotify = new ArrayList();
        this.mTempFullKeyboards = new ArrayList();
        this.mVibratorLock = new Object();
        this.mVibratorTokens = new HashMap();
        this.mInputFilterLock = new Object();
        Object[] objArr = new Object[SW_TABLET_MODE];
        objArr[SW_LID] = this;
        this.mCust = (HwCustInputManagerService) HwCustUtils.createObj(HwCustInputManagerService.class, objArr);
        this.mContext = context;
        this.mHandler = new InputManagerHandler(DisplayThread.get().getLooper());
        this.mUseDevInputEventForAudioJack = context.getResources().getBoolean(17956987);
        Slog.i(TAG, "Initializing input manager, mUseDevInputEventForAudioJack=" + this.mUseDevInputEventForAudioJack);
        this.mPtr = nativeInit(this, this.mContext, this.mHandler.getLooper().getQueue());
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
        this.mHandler.sendEmptyMessage(MSG_RELOAD_DEVICE_ALIASES);
        this.mHandler.sendEmptyMessage(SW_MICROPHONE_INSERT);
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

    private void setDisplayViewportsInternal(DisplayViewport defaultViewport, DisplayViewport externalTouchViewport) {
        if (defaultViewport.valid) {
            setDisplayViewport(DEBUG, defaultViewport);
        }
        if (externalTouchViewport.valid) {
            setDisplayViewport(true, externalTouchViewport);
        } else if (defaultViewport.valid) {
            setDisplayViewport(true, defaultViewport);
        }
    }

    private void setDisplayViewport(boolean external, DisplayViewport viewport) {
        boolean z = external;
        nativeSetDisplayViewport(this.mPtr, z, viewport.displayId, viewport.orientation, viewport.logicalFrame.left, viewport.logicalFrame.top, viewport.logicalFrame.right, viewport.logicalFrame.bottom, viewport.physicalFrame.left, viewport.physicalFrame.top, viewport.physicalFrame.right, viewport.physicalFrame.bottom, viewport.deviceWidth, viewport.deviceHeight);
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
        nativeRegisterInputChannel(this.mPtr, inputChannels[SW_LID], null, true);
        inputChannels[SW_LID].dispose();
        return inputChannels[SW_TABLET_MODE];
    }

    public void registerInputChannel(InputChannel inputChannel, InputWindowHandle inputWindowHandle) {
        if (inputChannel == null) {
            throw new IllegalArgumentException("inputChannel must not be null.");
        }
        nativeRegisterInputChannel(this.mPtr, inputChannel, inputWindowHandle, DEBUG);
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
            boolean z;
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
            long j = this.mPtr;
            if (filter != null) {
                z = true;
            } else {
                z = DEBUG;
            }
            nativeSetInputFilterEnabled(j, z);
        }
    }

    public boolean injectInputEvent(InputEvent event, int mode) {
        return injectInputEventInternal(event, SW_LID, mode);
    }

    private boolean injectInputEventInternal(InputEvent event, int displayId, int mode) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        } else if (mode == 0 || mode == SW_TABLET_MODE_BIT || mode == SW_TABLET_MODE) {
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long ident = Binder.clearCallingIdentity();
            try {
                int result = nativeInjectInputEvent(this.mPtr, event, displayId, pid, uid, mode, INJECTION_TIMEOUT_MILLIS, 134217728);
                switch (result) {
                    case SW_LID /*0*/:
                        return true;
                    case SW_TABLET_MODE /*1*/:
                        Slog.w(TAG, "Input event injection from pid " + pid + " permission denied.");
                        throw new SecurityException("Injecting to another application requires INJECT_EVENTS permission");
                    case MSG_RELOAD_KEYBOARD_LAYOUTS /*3*/:
                        Slog.w(TAG, "Input event injection from pid " + pid + " timed out.");
                        return DEBUG;
                    default:
                        Slog.w(TAG, "Input event injection from pid " + pid + " failed.");
                        return DEBUG;
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
            int count = this.mInputDevices.length;
            for (int i = SW_LID; i < count; i += SW_TABLET_MODE) {
                InputDevice inputDevice = this.mInputDevices[i];
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
            for (int i = SW_LID; i < count; i += SW_TABLET_MODE) {
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
                listener.asBinder().linkToDeath(record, SW_LID);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void deliverInputDevicesChanged(InputDevice[] oldInputDevices) {
        Throwable th;
        this.mTempInputDevicesChangedListenersToNotify.clear();
        this.mTempFullKeyboards.clear();
        synchronized (this.mInputDevicesLock) {
            try {
                if (this.mInputDevicesChangedPending) {
                    int i;
                    InputDevice inputDevice;
                    this.mInputDevicesChangedPending = DEBUG;
                    int numListeners = this.mInputDevicesChangedListeners.size();
                    for (i = SW_LID; i < numListeners; i += SW_TABLET_MODE) {
                        this.mTempInputDevicesChangedListenersToNotify.add((InputDevicesChangedListenerRecord) this.mInputDevicesChangedListeners.valueAt(i));
                    }
                    int numDevices = this.mInputDevices.length;
                    int[] deviceIdAndGeneration = new int[(numDevices * SW_TABLET_MODE_BIT)];
                    i = SW_LID;
                    int numFullKeyboardsAdded = SW_LID;
                    while (i < numDevices) {
                        int numFullKeyboardsAdded2;
                        inputDevice = this.mInputDevices[i];
                        deviceIdAndGeneration[i * SW_TABLET_MODE_BIT] = inputDevice.getId();
                        deviceIdAndGeneration[(i * SW_TABLET_MODE_BIT) + SW_TABLET_MODE] = inputDevice.getGeneration();
                        if (inputDevice.isVirtual()) {
                            numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                        } else if (!inputDevice.isFullKeyboard()) {
                            numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                        } else if (containsInputDeviceWithDescriptor(oldInputDevices, inputDevice.getDescriptor())) {
                            try {
                                this.mTempFullKeyboards.add(inputDevice);
                                numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                            } catch (Throwable th2) {
                                th = th2;
                                numFullKeyboardsAdded2 = numFullKeyboardsAdded;
                            }
                        } else {
                            numFullKeyboardsAdded2 = numFullKeyboardsAdded + SW_TABLET_MODE;
                            this.mTempFullKeyboards.add(numFullKeyboardsAdded, inputDevice);
                        }
                        i += SW_TABLET_MODE;
                        numFullKeyboardsAdded = numFullKeyboardsAdded2;
                    }
                    for (i = SW_LID; i < numListeners; i += SW_TABLET_MODE) {
                        ((InputDevicesChangedListenerRecord) this.mTempInputDevicesChangedListenersToNotify.get(i)).notifyInputDevicesChanged(deviceIdAndGeneration);
                    }
                    this.mTempInputDevicesChangedListenersToNotify.clear();
                    List<InputDevice> keyboardsMissingLayout = new ArrayList();
                    int numFullKeyboards = this.mTempFullKeyboards.size();
                    synchronized (this.mDataStore) {
                        for (i = SW_LID; i < numFullKeyboards; i += SW_TABLET_MODE) {
                            inputDevice = (InputDevice) this.mTempFullKeyboards.get(i);
                            String layout = getCurrentKeyboardLayoutForInputDevice(inputDevice.getIdentifier());
                            if (layout == null) {
                                layout = getDefaultKeyboardLayout(inputDevice);
                                if (layout != null) {
                                    setCurrentKeyboardLayoutForInputDevice(inputDevice.getIdentifier(), layout);
                                }
                            }
                            if (layout == null) {
                                keyboardsMissingLayout.add(inputDevice);
                            }
                        }
                    }
                    if (this.mNotificationManager != null) {
                        if (keyboardsMissingLayout.isEmpty()) {
                            if (this.mKeyboardLayoutNotificationShown) {
                                hideMissingKeyboardLayoutNotification();
                            }
                        } else if (keyboardsMissingLayout.size() > SW_TABLET_MODE) {
                            showMissingKeyboardLayoutNotification(null);
                        } else {
                            showMissingKeyboardLayoutNotification((InputDevice) keyboardsMissingLayout.get(SW_LID));
                        }
                    }
                    this.mTempFullKeyboards.clear();
                    return;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    private String getDefaultKeyboardLayout(InputDevice d) {
        Locale systemLocale = this.mContext.getResources().getConfiguration().locale;
        if (TextUtils.isEmpty(systemLocale.getLanguage())) {
            return null;
        }
        List<KeyboardLayout> layouts = new ArrayList();
        visitAllKeyboardLayouts(new AnonymousClass4(d, systemLocale, layouts));
        if (layouts.isEmpty()) {
            return null;
        }
        int i;
        Collections.sort(layouts);
        int N = layouts.size();
        for (i = SW_LID; i < N; i += SW_TABLET_MODE) {
            int localeIndex;
            KeyboardLayout layout = (KeyboardLayout) layouts.get(i);
            LocaleList locales = layout.getLocales();
            int numLocales = locales.size();
            for (localeIndex = SW_LID; localeIndex < numLocales; localeIndex += SW_TABLET_MODE) {
                Locale locale = locales.get(localeIndex);
                if (locale.getCountry().equals(systemLocale.getCountry()) && locale.getVariant().equals(systemLocale.getVariant())) {
                    return layout.getDescriptor();
                }
            }
        }
        for (i = SW_LID; i < N; i += SW_TABLET_MODE) {
            layout = (KeyboardLayout) layouts.get(i);
            locales = layout.getLocales();
            numLocales = locales.size();
            for (localeIndex = SW_LID; localeIndex < numLocales; localeIndex += SW_TABLET_MODE) {
                if (locales.get(localeIndex).getCountry().equals(systemLocale.getCountry())) {
                    return layout.getDescriptor();
                }
            }
        }
        return ((KeyboardLayout) layouts.get(SW_LID)).getDescriptor();
    }

    private static boolean isCompatibleLocale(Locale systemLocale, Locale keyboardLocale) {
        if (!systemLocale.getLanguage().equals(keyboardLocale.getLanguage())) {
            return DEBUG;
        }
        if (TextUtils.isEmpty(systemLocale.getCountry()) || TextUtils.isEmpty(keyboardLocale.getCountry()) || systemLocale.getCountry().equals(keyboardLocale.getCountry())) {
            return true;
        }
        return DEBUG;
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
        } else if (surfaceRotation < 0 || surfaceRotation > MSG_RELOAD_KEYBOARD_LAYOUTS) {
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
            return getSwitchState(KEY_STATE_UNKNOWN, -256, SW_TABLET_MODE);
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
                    listener.asBinder().linkToDeath(record, SW_LID);
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
        this.mTempTabletModeChangedListenersToNotify.clear();
        synchronized (this.mTabletModeLock) {
            int i;
            int numListeners = this.mTabletModeChangedListeners.size();
            for (i = SW_LID; i < numListeners; i += SW_TABLET_MODE) {
                this.mTempTabletModeChangedListenersToNotify.add((TabletModeChangedListenerRecord) this.mTabletModeChangedListeners.valueAt(i));
            }
        }
        for (i = SW_LID; i < numListeners; i += SW_TABLET_MODE) {
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
            PendingIntent keyboardLayoutIntent = PendingIntent.getActivityAsUser(this.mContext, SW_LID, intent, SW_LID, null, UserHandle.CURRENT);
            Resources r = this.mContext.getResources();
            this.mNotificationManager.notifyAsUser(null, 17040403, new Builder(this.mContext).setContentTitle(r.getString(17040403)).setContentText(r.getString(17040404)).setContentIntent(keyboardLayoutIntent).setSmallIcon(17302554).setPriority(KEY_STATE_UNKNOWN).setColor(this.mContext.getColor(17170519)).build(), UserHandle.ALL);
            this.mKeyboardLayoutNotificationShown = true;
        }
    }

    private void hideMissingKeyboardLayoutNotification() {
        if (this.mKeyboardLayoutNotificationShown) {
            this.mKeyboardLayoutNotificationShown = DEBUG;
            this.mNotificationManager.cancelAsUser(null, 17040403, UserHandle.ALL);
        }
    }

    private void updateKeyboardLayouts() {
        HashSet<String> availableKeyboardLayouts = new HashSet();
        visitAllKeyboardLayouts(new AnonymousClass5(availableKeyboardLayouts));
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
        int numDevices = inputDevices.length;
        for (int i = SW_LID; i < numDevices; i += SW_TABLET_MODE) {
            if (inputDevices[i].getDescriptor().equals(descriptor)) {
                return true;
            }
        }
        return DEBUG;
    }

    public KeyboardLayout[] getKeyboardLayouts() {
        ArrayList<KeyboardLayout> list = new ArrayList();
        visitAllKeyboardLayouts(new AnonymousClass6(list));
        return (KeyboardLayout[]) list.toArray(new KeyboardLayout[list.size()]);
    }

    public KeyboardLayout[] getKeyboardLayoutsForInputDevice(InputDeviceIdentifier identifier) {
        String[] enabledLayoutDescriptors = getEnabledKeyboardLayoutsForInputDevice(identifier);
        ArrayList<KeyboardLayout> enabledLayouts = new ArrayList(enabledLayoutDescriptors.length);
        ArrayList<KeyboardLayout> potentialLayouts = new ArrayList();
        visitAllKeyboardLayouts(new AnonymousClass7(enabledLayoutDescriptors, enabledLayouts, identifier, potentialLayouts));
        int enabledLayoutSize = enabledLayouts.size();
        int potentialLayoutSize = potentialLayouts.size();
        KeyboardLayout[] layouts = new KeyboardLayout[(enabledLayoutSize + potentialLayoutSize)];
        enabledLayouts.toArray(layouts);
        for (int i = SW_LID; i < potentialLayoutSize; i += SW_TABLET_MODE) {
            layouts[enabledLayoutSize + i] = (KeyboardLayout) potentialLayouts.get(i);
        }
        return layouts;
    }

    public KeyboardLayout getKeyboardLayout(String keyboardLayoutDescriptor) {
        if (keyboardLayoutDescriptor == null) {
            throw new IllegalArgumentException("keyboardLayoutDescriptor must not be null");
        }
        KeyboardLayout[] result = new KeyboardLayout[SW_TABLET_MODE];
        visitKeyboardLayout(keyboardLayoutDescriptor, new AnonymousClass8(result));
        if (result[SW_LID] == null) {
            Slog.w(TAG, "Could not get keyboard layout with descriptor '" + keyboardLayoutDescriptor + "'.");
        }
        return result[SW_LID];
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
                visitKeyboardLayoutsInPackage(pm, pm.getReceiverInfo(new ComponentName(d.packageName, d.receiverName), 786560), d.keyboardLayoutName, SW_LID, visitor);
            } catch (NameNotFoundException e) {
            }
        }
    }

    private void visitKeyboardLayoutsInPackage(PackageManager pm, ActivityInfo receiver, String keyboardName, int requestedPriority, KeyboardLayoutVisitor visitor) {
        TypedArray a;
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
            if ((receiver.applicationInfo.flags & SW_TABLET_MODE) != 0) {
                priority = requestedPriority;
            } else {
                priority = SW_LID;
            }
            XmlResourceParser parser;
            try {
                Resources resources = pm.getResourcesForApplication(receiver.applicationInfo);
                parser = resources.getXml(configResId);
                XmlUtils.beginDocument(parser, "keyboard-layouts");
                while (true) {
                    XmlUtils.nextElement(parser);
                    String element = parser.getName();
                    if (element == null) {
                        break;
                    }
                    if (element.equals("keyboard-layout")) {
                        a = resources.obtainAttributes(parser, R.styleable.KeyboardLayout);
                        String name = a.getString(SW_TABLET_MODE);
                        String label = a.getString(SW_LID);
                        int keyboardLayoutResId = a.getResourceId(SW_TABLET_MODE_BIT, SW_LID);
                        LocaleList locales = getLocalesFromLanguageTags(a.getString(MSG_RELOAD_KEYBOARD_LAYOUTS));
                        int vid = a.getInt(SW_MICROPHONE_INSERT, KEY_STATE_UNKNOWN);
                        int pid = a.getInt(MSG_RELOAD_DEVICE_ALIASES, KEY_STATE_UNKNOWN);
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
                        StringBuilder append = new StringBuilder().append("Skipping unrecognized element '");
                        Slog.w(TAG, r23.append(element).append("' in keyboard layout resource from receiver ").append(receiver.packageName).append("/").append(receiver.name).toString());
                    }
                }
                parser.close();
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
            if (layout == null && !key.equals(identifier.getDescriptor())) {
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
                        this.mHandler.sendEmptyMessage(MSG_RELOAD_KEYBOARD_LAYOUTS);
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
            if ((layouts == null || layouts.length == 0) && !key.equals(identifier.getDescriptor())) {
                layouts = this.mDataStore.getKeyboardLayouts(identifier.getDescriptor());
            }
        }
        return layouts;
    }

    public KeyboardLayout getKeyboardLayoutForInputDevice(InputDeviceIdentifier identifier, InputMethodInfo imeInfo, InputMethodSubtype imeSubtype) {
        InputMethodSubtypeHandle handle = new InputMethodSubtypeHandle(imeInfo, imeSubtype);
        String key = getLayoutDescriptor(identifier);
        synchronized (this.mDataStore) {
            String keyboardLayoutDescriptor = this.mDataStore.getKeyboardLayout(key, handle);
        }
        if (keyboardLayoutDescriptor == null) {
            return null;
        }
        KeyboardLayout[] result = new KeyboardLayout[SW_TABLET_MODE];
        visitKeyboardLayout(keyboardLayoutDescriptor, new AnonymousClass9(result));
        if (result[SW_LID] == null) {
            Slog.w(TAG, "Could not get keyboard layout with descriptor '" + keyboardLayoutDescriptor + "'.");
        }
        return result[SW_LID];
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
                        this.mHandler.obtainMessage(SW_TABLET_MODE_BIT, args).sendToTarget();
                    }
                    this.mHandler.sendEmptyMessage(MSG_RELOAD_KEYBOARD_LAYOUTS);
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
                    if (oldLayout == null && !key.equals(identifier.getDescriptor())) {
                        oldLayout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
                    }
                    if (this.mDataStore.addKeyboardLayout(key, keyboardLayoutDescriptor) && !Objects.equal(oldLayout, this.mDataStore.getCurrentKeyboardLayout(key))) {
                        this.mHandler.sendEmptyMessage(MSG_RELOAD_KEYBOARD_LAYOUTS);
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
                    if (oldLayout == null && !key.equals(identifier.getDescriptor())) {
                        oldLayout = this.mDataStore.getCurrentKeyboardLayout(identifier.getDescriptor());
                    }
                    boolean removed = this.mDataStore.removeKeyboardLayout(key, keyboardLayoutDescriptor);
                    if (!key.equals(identifier.getDescriptor())) {
                        removed |= this.mDataStore.removeKeyboardLayout(identifier.getDescriptor(), keyboardLayoutDescriptor);
                    }
                    if (removed && !Objects.equal(oldLayout, this.mDataStore.getCurrentKeyboardLayout(key))) {
                        this.mHandler.sendEmptyMessage(MSG_RELOAD_KEYBOARD_LAYOUTS);
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
        } else if (subtype == null || "keyboard".equals(subtype.getMode())) {
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
            InputDevice[] inputDeviceArr = this.mInputDevices;
            int length = inputDeviceArr.length;
            for (int i = SW_LID; i < length; i += SW_TABLET_MODE) {
                InputDevice device = inputDeviceArr[i];
                if ((identifier == null || device.getIdentifier().equals(identifier)) && device.isFullKeyboard()) {
                    String key = getLayoutDescriptor(device.getIdentifier());
                    boolean changed = DEBUG;
                    synchronized (this.mDataStore) {
                        try {
                            if (this.mDataStore.switchKeyboardLayout(key, handle)) {
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

    public void setInputWindows(InputWindowHandle[] windowHandles) {
        nativeSetInputWindows(this.mPtr, windowHandles);
    }

    public void setFocusedApplication(InputApplicationHandle application) {
        nativeSetFocusedApplication(this.mPtr, application);
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
        } else if (speed < -7 || speed > SW_JACK_PHYSICAL_INSERT) {
            throw new IllegalArgumentException("speed out of range");
        } else {
            setPointerSpeedUnchecked(speed);
        }
    }

    public void updatePointerSpeedFromSettings() {
        setPointerSpeedUnchecked(getPointerSpeedSetting());
    }

    private void setPointerSpeedUnchecked(int speed) {
        nativeSetPointerSpeed(this.mPtr, Math.min(Math.max(speed, -7), SW_JACK_PHYSICAL_INSERT));
    }

    private void registerPointerSpeedSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("pointer_speed"), true, new AnonymousClass10(this.mHandler), KEY_STATE_UNKNOWN);
    }

    private int getPointerSpeedSetting() {
        int speed = SW_LID;
        try {
            speed = System.getIntForUser(this.mContext.getContentResolver(), "pointer_speed", -2);
        } catch (SettingNotFoundException e) {
        }
        return speed;
    }

    public void updateShowTouchesFromSettings() {
        boolean z = DEBUG;
        int setting = getShowTouchesSetting(SW_LID);
        long j = this.mPtr;
        if (setting != 0) {
            z = true;
        }
        nativeSetShowTouches(j, z);
    }

    private void registerShowTouchesSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("show_touches"), true, new AnonymousClass11(this.mHandler), KEY_STATE_UNKNOWN);
    }

    public void updateAccessibilityLargePointerFromSettings() {
        boolean z = true;
        if (Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_large_pointer_icon", SW_LID, -2) != SW_TABLET_MODE) {
            z = DEBUG;
        }
        PointerIcon.setUseLargeIcons(z);
        nativeReloadPointerIcons(this.mPtr);
    }

    private void registerAccessibilityLargePointerSettingObserver() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("accessibility_large_pointer_icon"), true, new AnonymousClass12(this.mHandler), KEY_STATE_UNKNOWN);
    }

    private int getShowTouchesSetting(int defaultValue) {
        int result = defaultValue;
        try {
            result = System.getIntForUser(this.mContext.getContentResolver(), "show_touches", -2);
        } catch (SettingNotFoundException e) {
        }
        return result;
    }

    public void vibrate(int deviceId, long[] pattern, int repeat, IBinder token) {
        if (repeat >= pattern.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        synchronized (this.mVibratorLock) {
            VibratorToken v = (VibratorToken) this.mVibratorTokens.get(token);
            if (v == null) {
                int i = this.mNextVibratorTokenValue;
                this.mNextVibratorTokenValue = i + SW_TABLET_MODE;
                v = new VibratorToken(deviceId, token, i);
                try {
                    token.linkToDeath(v, SW_LID);
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

    public void cancelVibrate(int deviceId, IBinder token) {
        synchronized (this.mVibratorLock) {
            VibratorToken v = (VibratorToken) this.mVibratorTokens.get(token);
            if (v == null || v.mDeviceId != deviceId) {
                return;
            }
            cancelVibrateIfNeeded(v);
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
                v.mVibrating = DEBUG;
            }
        }
    }

    public void setPointerIconType(int iconId) {
        nativeSetPointerIconType(this.mPtr, iconId);
    }

    public void setCustomPointerIcon(PointerIcon icon) {
        if (icon != null) {
            nativeSetCustomPointerIcon(this.mPtr, icon);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump InputManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("INPUT MANAGER (dumpsys input)\n");
        String dumpStr = nativeDump(this.mPtr);
        if (dumpStr != null) {
            pw.println(dumpStr);
        }
        pw.println("  Keyboard Layouts:");
        visitAllKeyboardLayouts(new AnonymousClass13(pw));
        pw.println();
        synchronized (this.mDataStore) {
            this.mDataStore.dump(pw, "  ");
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ResultReceiver resultReceiver) {
        new Shell().exec(this, in, out, err, args, resultReceiver);
    }

    public int onShellCommand(Shell shell, String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            shell.onHelp();
            return SW_TABLET_MODE;
        }
        if (cmd.equals("setlayout")) {
            if (checkCallingPermission("android.permission.SET_KEYBOARD_LAYOUT", "onShellCommand()")) {
                setKeyboardLayoutForInputDeviceInner(new InputDeviceIdentifier(shell.getNextArgRequired(), Integer.decode(shell.getNextArgRequired()).intValue(), Integer.decode(shell.getNextArgRequired()).intValue()), new InputMethodSubtypeHandle(shell.getNextArgRequired(), Integer.parseInt(shell.getNextArgRequired())), shell.getNextArgRequired());
            } else {
                throw new SecurityException("Requires SET_KEYBOARD_LAYOUT permission");
            }
        }
        return SW_LID;
    }

    private boolean checkCallingPermission(String permission, String func) {
        if (Binder.getCallingPid() == Process.myPid() || this.mContext.checkCallingPermission(permission) == 0) {
            return true;
        }
        Slog.w(TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
        return DEBUG;
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
                this.mHandler.obtainMessage(SW_TABLET_MODE, this.mInputDevices).sendToTarget();
            }
            this.mInputDevices = inputDevices;
        }
    }

    private void notifySwitch(long whenNanos, int switchValues, int switchMask) {
        boolean z = DEBUG;
        if ((switchMask & SW_TABLET_MODE) != 0) {
            this.mWindowManagerCallbacks.notifyLidSwitchChanged(whenNanos, (switchValues & SW_TABLET_MODE) == 0 ? true : DEBUG);
        }
        if ((switchMask & SW_CAMERA_LENS_COVER_BIT) != 0) {
            this.mWindowManagerCallbacks.notifyCameraLensCoverSwitchChanged(whenNanos, (switchValues & SW_CAMERA_LENS_COVER_BIT) != 0 ? true : DEBUG);
        }
        if (this.mUseDevInputEventForAudioJack && (switchMask & SW_JACK_BITS) != 0) {
            this.mWiredAccessoryCallbacks.notifyWiredAccessoryChanged(whenNanos, switchValues, switchMask);
        }
        if ((switchMask & SW_TABLET_MODE_BIT) != 0) {
            SomeArgs args = SomeArgs.obtain();
            args.argi1 = (int) (-1 & whenNanos);
            args.argi2 = (int) (whenNanos >> 32);
            if ((switchValues & SW_TABLET_MODE_BIT) != 0) {
                z = true;
            }
            args.arg1 = Boolean.valueOf(z);
            this.mHandler.obtainMessage(SW_LINEOUT_INSERT, args).sendToTarget();
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
                return DEBUG;
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
        return this.mContext.checkPermission("android.permission.INJECT_EVENTS", injectorPid, injectorUid) == 0 ? true : DEBUG;
    }

    private int getVirtualKeyQuietTimeMillis() {
        return this.mContext.getResources().getInteger(17694830);
    }

    private String[] getExcludedDeviceNames() {
        Exception e;
        Throwable th;
        ArrayList<String> names = new ArrayList();
        File confFile = new File(Environment.getRootDirectory(), EXCLUDED_DEVICES_PATH);
        FileReader fileReader = null;
        try {
            FileReader confreader = new FileReader(confFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(confreader);
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
                if (confreader != null) {
                    try {
                        confreader.close();
                    } catch (IOException e2) {
                    }
                }
                fileReader = confreader;
            } catch (FileNotFoundException e3) {
                fileReader = confreader;
            } catch (Exception e4) {
                e = e4;
                fileReader = confreader;
            } catch (Throwable th2) {
                th = th2;
                fileReader = confreader;
            }
        } catch (FileNotFoundException e5) {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e6) {
                }
            }
            return (String[]) names.toArray(new String[names.size()]);
        } catch (Exception e7) {
            e = e7;
            try {
                Slog.e(TAG, "Exception while parsing '" + confFile.getAbsolutePath() + "'", e);
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e8) {
                    }
                }
                return (String[]) names.toArray(new String[names.size()]);
            } catch (Throwable th3) {
                th = th3;
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e9) {
                    }
                }
                throw th;
            }
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

    private PointerIcon getPointerIcon() {
        return PointerIcon.getDefaultIcon(this.mContext);
    }

    private String[] getKeyboardLayoutOverlay(InputDeviceIdentifier identifier) {
        if (!this.mSystemReady) {
            return null;
        }
        String keyboardLayoutDescriptor = getCurrentKeyboardLayoutForInputDevice(identifier);
        if (keyboardLayoutDescriptor == null) {
            return null;
        }
        String[] result = new String[SW_TABLET_MODE_BIT];
        visitKeyboardLayout(keyboardLayoutDescriptor, new AnonymousClass14(result));
        if (result[SW_LID] != null) {
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
