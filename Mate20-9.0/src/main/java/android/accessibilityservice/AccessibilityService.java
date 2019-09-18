package android.accessibilityservice;

import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Region;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.android.internal.os.HandlerCaller;
import com.android.internal.os.SomeArgs;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public abstract class AccessibilityService extends Service {
    public static final int GESTURE_SWIPE_DOWN = 2;
    public static final int GESTURE_SWIPE_DOWN_AND_LEFT = 15;
    public static final int GESTURE_SWIPE_DOWN_AND_RIGHT = 16;
    public static final int GESTURE_SWIPE_DOWN_AND_UP = 8;
    public static final int GESTURE_SWIPE_LEFT = 3;
    public static final int GESTURE_SWIPE_LEFT_AND_DOWN = 10;
    public static final int GESTURE_SWIPE_LEFT_AND_RIGHT = 5;
    public static final int GESTURE_SWIPE_LEFT_AND_UP = 9;
    public static final int GESTURE_SWIPE_RIGHT = 4;
    public static final int GESTURE_SWIPE_RIGHT_AND_DOWN = 12;
    public static final int GESTURE_SWIPE_RIGHT_AND_LEFT = 6;
    public static final int GESTURE_SWIPE_RIGHT_AND_UP = 11;
    public static final int GESTURE_SWIPE_UP = 1;
    public static final int GESTURE_SWIPE_UP_AND_DOWN = 7;
    public static final int GESTURE_SWIPE_UP_AND_LEFT = 13;
    public static final int GESTURE_SWIPE_UP_AND_RIGHT = 14;
    public static final int GLOBAL_ACTION_BACK = 1;
    public static final int GLOBAL_ACTION_HOME = 2;
    public static final int GLOBAL_ACTION_LOCK_SCREEN = 8;
    public static final int GLOBAL_ACTION_NOTIFICATIONS = 4;
    public static final int GLOBAL_ACTION_POWER_DIALOG = 6;
    public static final int GLOBAL_ACTION_QUICK_SETTINGS = 5;
    public static final int GLOBAL_ACTION_RECENTS = 3;
    public static final int GLOBAL_ACTION_TAKE_SCREENSHOT = 9;
    public static final int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = 7;
    private static final String LOG_TAG = "AccessibilityService";
    public static final String SERVICE_INTERFACE = "android.accessibilityservice.AccessibilityService";
    public static final String SERVICE_META_DATA = "android.accessibilityservice";
    public static final int SHOW_MODE_AUTO = 0;
    public static final int SHOW_MODE_HIDDEN = 1;
    private AccessibilityButtonController mAccessibilityButtonController;
    /* access modifiers changed from: private */
    public int mConnectionId = -1;
    private FingerprintGestureController mFingerprintGestureController;
    private SparseArray<GestureResultCallbackInfo> mGestureStatusCallbackInfos;
    private int mGestureStatusCallbackSequence;
    private AccessibilityServiceInfo mInfo;
    private final Object mLock = new Object();
    private MagnificationController mMagnificationController;
    private SoftKeyboardController mSoftKeyboardController;
    private WindowManager mWindowManager;
    /* access modifiers changed from: private */
    public IBinder mWindowToken;

    public interface Callbacks {
        void init(int i, IBinder iBinder);

        void onAccessibilityButtonAvailabilityChanged(boolean z);

        void onAccessibilityButtonClicked();

        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onFingerprintCapturingGesturesChanged(boolean z);

        void onFingerprintGesture(int i);

        boolean onGesture(int i);

        void onInterrupt();

        boolean onKeyEvent(KeyEvent keyEvent);

        void onMagnificationChanged(Region region, float f, float f2, float f3);

        void onPerformGestureResult(int i, boolean z);

        void onServiceConnected();

        void onSoftKeyboardShowModeChanged(int i);
    }

    public static abstract class GestureResultCallback {
        public void onCompleted(GestureDescription gestureDescription) {
        }

        public void onCancelled(GestureDescription gestureDescription) {
        }
    }

    private static class GestureResultCallbackInfo {
        GestureResultCallback callback;
        GestureDescription gestureDescription;
        Handler handler;

        GestureResultCallbackInfo(GestureDescription gestureDescription2, GestureResultCallback callback2, Handler handler2) {
            this.gestureDescription = gestureDescription2;
            this.callback = callback2;
            this.handler = handler2;
        }
    }

    public static class IAccessibilityServiceClientWrapper extends IAccessibilityServiceClient.Stub implements HandlerCaller.Callback {
        private static final int DO_ACCESSIBILITY_BUTTON_AVAILABILITY_CHANGED = 13;
        private static final int DO_ACCESSIBILITY_BUTTON_CLICKED = 12;
        private static final int DO_CLEAR_ACCESSIBILITY_CACHE = 5;
        private static final int DO_GESTURE_COMPLETE = 9;
        private static final int DO_INIT = 1;
        private static final int DO_ON_ACCESSIBILITY_EVENT = 3;
        private static final int DO_ON_FINGERPRINT_ACTIVE_CHANGED = 10;
        private static final int DO_ON_FINGERPRINT_GESTURE = 11;
        private static final int DO_ON_GESTURE = 4;
        private static final int DO_ON_INTERRUPT = 2;
        private static final int DO_ON_KEY_EVENT = 6;
        private static final int DO_ON_MAGNIFICATION_CHANGED = 7;
        private static final int DO_ON_SOFT_KEYBOARD_SHOW_MODE_CHANGED = 8;
        private final Callbacks mCallback;
        private final HandlerCaller mCaller;
        private int mConnectionId = -1;

        public IAccessibilityServiceClientWrapper(Context context, Looper looper, Callbacks callback) {
            this.mCallback = callback;
            this.mCaller = new HandlerCaller(context, looper, this, true);
        }

        public void init(IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIOO(1, connectionId, connection, windowToken));
        }

        public void onInterrupt() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(2));
        }

        public void onAccessibilityEvent(AccessibilityEvent event, boolean serviceWantsEvent) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageBO(3, serviceWantsEvent, event));
        }

        public void onGesture(int gestureId) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(4, gestureId));
        }

        public void clearAccessibilityCache() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(5));
        }

        public void onKeyEvent(KeyEvent event, int sequence) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIO(6, sequence, event));
        }

        public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = region;
            args.arg2 = Float.valueOf(scale);
            args.arg3 = Float.valueOf(centerX);
            args.arg4 = Float.valueOf(centerY);
            this.mCaller.sendMessage(this.mCaller.obtainMessageO(7, args));
        }

        public void onSoftKeyboardShowModeChanged(int showMode) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(8, showMode));
        }

        public void onPerformGestureResult(int sequence, boolean successfully) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageII(9, sequence, successfully));
        }

        public void onFingerprintCapturingGesturesChanged(boolean active) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(10, active));
        }

        public void onFingerprintGesture(int gesture) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(11, gesture));
        }

        public void onAccessibilityButtonClicked() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(12));
        }

        public void onAccessibilityButtonAvailabilityChanged(boolean available) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(13, available));
        }

        public void executeMessage(Message message) {
            boolean serviceWantsEvent = false;
            switch (message.what) {
                case 1:
                    this.mConnectionId = message.arg1;
                    SomeArgs args = (SomeArgs) message.obj;
                    IAccessibilityServiceConnection connection = (IAccessibilityServiceConnection) args.arg1;
                    IBinder windowToken = (IBinder) args.arg2;
                    args.recycle();
                    if (connection != null) {
                        AccessibilityInteractionClient.getInstance();
                        AccessibilityInteractionClient.addConnection(this.mConnectionId, connection);
                        this.mCallback.init(this.mConnectionId, windowToken);
                        this.mCallback.onServiceConnected();
                    } else {
                        AccessibilityInteractionClient.getInstance();
                        AccessibilityInteractionClient.removeConnection(this.mConnectionId);
                        this.mConnectionId = -1;
                        AccessibilityInteractionClient.getInstance().clearCache();
                        this.mCallback.init(-1, null);
                    }
                    return;
                case 2:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onInterrupt();
                    }
                    return;
                case 3:
                    AccessibilityEvent event = (AccessibilityEvent) message.obj;
                    if (message.arg1 != 0) {
                        serviceWantsEvent = true;
                    }
                    if (event != null) {
                        AccessibilityInteractionClient.getInstance().onAccessibilityEvent(event);
                        if (serviceWantsEvent && this.mConnectionId != -1) {
                            this.mCallback.onAccessibilityEvent(event);
                        }
                        try {
                            event.recycle();
                        } catch (IllegalStateException e) {
                        }
                    }
                    return;
                case 4:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onGesture(message.arg1);
                    }
                    return;
                case 5:
                    AccessibilityInteractionClient.getInstance().clearCache();
                    return;
                case 6:
                    KeyEvent event2 = (KeyEvent) message.obj;
                    try {
                        AccessibilityInteractionClient.getInstance();
                        IAccessibilityServiceConnection connection2 = AccessibilityInteractionClient.getConnection(this.mConnectionId);
                        if (connection2 != null) {
                            try {
                                connection2.setOnKeyEventResult(this.mCallback.onKeyEvent(event2), message.arg1);
                            } catch (RemoteException e2) {
                            }
                        }
                        return;
                    } finally {
                        try {
                            event2.recycle();
                        } catch (IllegalStateException e3) {
                        }
                    }
                case 7:
                    if (this.mConnectionId != -1) {
                        SomeArgs args2 = (SomeArgs) message.obj;
                        this.mCallback.onMagnificationChanged((Region) args2.arg1, ((Float) args2.arg2).floatValue(), ((Float) args2.arg3).floatValue(), ((Float) args2.arg4).floatValue());
                    }
                    return;
                case 8:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onSoftKeyboardShowModeChanged(message.arg1);
                    }
                    return;
                case 9:
                    if (this.mConnectionId != -1) {
                        if (message.arg2 == 1) {
                            serviceWantsEvent = true;
                        }
                        this.mCallback.onPerformGestureResult(message.arg1, serviceWantsEvent);
                    }
                    return;
                case 10:
                    if (this.mConnectionId != -1) {
                        Callbacks callbacks = this.mCallback;
                        if (message.arg1 == 1) {
                            serviceWantsEvent = true;
                        }
                        callbacks.onFingerprintCapturingGesturesChanged(serviceWantsEvent);
                    }
                    return;
                case 11:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onFingerprintGesture(message.arg1);
                    }
                    return;
                case 12:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onAccessibilityButtonClicked();
                    }
                    return;
                case 13:
                    if (this.mConnectionId != -1) {
                        if (message.arg1 != 0) {
                            serviceWantsEvent = true;
                        }
                        this.mCallback.onAccessibilityButtonAvailabilityChanged(serviceWantsEvent);
                    }
                    return;
                default:
                    Log.w(AccessibilityService.LOG_TAG, "Unknown message type " + message.what);
                    return;
            }
        }
    }

    public static final class MagnificationController {
        private ArrayMap<OnMagnificationChangedListener, Handler> mListeners;
        private final Object mLock;
        private final AccessibilityService mService;

        public interface OnMagnificationChangedListener {
            void onMagnificationChanged(MagnificationController magnificationController, Region region, float f, float f2, float f3);
        }

        MagnificationController(AccessibilityService service, Object lock) {
            this.mService = service;
            this.mLock = lock;
        }

        /* access modifiers changed from: package-private */
        public void onServiceConnected() {
            synchronized (this.mLock) {
                if (this.mListeners != null && !this.mListeners.isEmpty()) {
                    setMagnificationCallbackEnabled(true);
                }
            }
        }

        public void addListener(OnMagnificationChangedListener listener) {
            addListener(listener, null);
        }

        public void addListener(OnMagnificationChangedListener listener, Handler handler) {
            synchronized (this.mLock) {
                if (this.mListeners == null) {
                    this.mListeners = new ArrayMap<>();
                }
                boolean shouldEnableCallback = this.mListeners.isEmpty();
                this.mListeners.put(listener, handler);
                if (shouldEnableCallback) {
                    setMagnificationCallbackEnabled(true);
                }
            }
        }

        public boolean removeListener(OnMagnificationChangedListener listener) {
            boolean hasKey;
            if (this.mListeners == null) {
                return false;
            }
            synchronized (this.mLock) {
                int keyIndex = this.mListeners.indexOfKey(listener);
                hasKey = keyIndex >= 0;
                if (hasKey) {
                    this.mListeners.removeAt(keyIndex);
                }
                if (hasKey && this.mListeners.isEmpty()) {
                    setMagnificationCallbackEnabled(false);
                }
            }
            return hasKey;
        }

        private void setMagnificationCallbackEnabled(boolean enabled) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    connection.setMagnificationCallbackEnabled(enabled);
                } catch (RemoteException re) {
                    throw new RuntimeException(re);
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
            r2 = r0.size();
            r9 = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
            r10 = r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
            if (r9 >= r10) goto L_0x0055;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
            r11 = r0.keyAt(r9);
            r12 = r0.valueAt(r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
            if (r12 == null) goto L_0x0045;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
            r3 = r11;
            r4 = r15;
            r5 = r16;
            r6 = r17;
            r7 = r18;
            r1 = new android.accessibilityservice.AccessibilityService.MagnificationController.AnonymousClass1(r8);
            r12.post(r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
            r11.onMagnificationChanged(r8, r15, r16, r17, r18);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0051, code lost:
            r9 = r9 + 1;
            r2 = r10;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0055, code lost:
            return;
         */
        public void dispatchMagnificationChanged(Region region, float scale, float centerX, float centerY) {
            synchronized (this.mLock) {
                if (this.mListeners != null) {
                    if (!this.mListeners.isEmpty()) {
                        ArrayMap<OnMagnificationChangedListener, Handler> entries = new ArrayMap<>(this.mListeners);
                    }
                }
                Slog.d(AccessibilityService.LOG_TAG, "Received magnification changed callback with no listeners registered!");
                setMagnificationCallbackEnabled(false);
            }
        }

        public float getScale() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.getMagnificationScale();
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to obtain scale", re);
                    re.rethrowFromSystemServer();
                }
            }
            return 1.0f;
        }

        public float getCenterX() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.getMagnificationCenterX();
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to obtain center X", re);
                    re.rethrowFromSystemServer();
                }
            }
            return 0.0f;
        }

        public float getCenterY() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.getMagnificationCenterY();
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to obtain center Y", re);
                    re.rethrowFromSystemServer();
                }
            }
            return 0.0f;
        }

        public Region getMagnificationRegion() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.getMagnificationRegion();
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to obtain magnified region", re);
                    re.rethrowFromSystemServer();
                }
            }
            return Region.obtain();
        }

        public boolean reset(boolean animate) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.resetMagnification(animate);
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to reset", re);
                    re.rethrowFromSystemServer();
                }
            }
            return false;
        }

        public boolean setScale(float scale, boolean animate) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.setMagnificationScaleAndCenter(scale, Float.NaN, Float.NaN, animate);
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to set scale", re);
                    re.rethrowFromSystemServer();
                }
            }
            return false;
        }

        public boolean setCenter(float centerX, float centerY, boolean animate) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.setMagnificationScaleAndCenter(Float.NaN, centerX, centerY, animate);
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to set center", re);
                    re.rethrowFromSystemServer();
                }
            }
            return false;
        }
    }

    public static final class SoftKeyboardController {
        private ArrayMap<OnShowModeChangedListener, Handler> mListeners;
        private final Object mLock;
        private final AccessibilityService mService;

        public interface OnShowModeChangedListener {
            void onShowModeChanged(SoftKeyboardController softKeyboardController, int i);
        }

        SoftKeyboardController(AccessibilityService service, Object lock) {
            this.mService = service;
            this.mLock = lock;
        }

        /* access modifiers changed from: package-private */
        public void onServiceConnected() {
            synchronized (this.mLock) {
                if (this.mListeners != null && !this.mListeners.isEmpty()) {
                    setSoftKeyboardCallbackEnabled(true);
                }
            }
        }

        public void addOnShowModeChangedListener(OnShowModeChangedListener listener) {
            addOnShowModeChangedListener(listener, null);
        }

        public void addOnShowModeChangedListener(OnShowModeChangedListener listener, Handler handler) {
            synchronized (this.mLock) {
                if (this.mListeners == null) {
                    this.mListeners = new ArrayMap<>();
                }
                boolean shouldEnableCallback = this.mListeners.isEmpty();
                this.mListeners.put(listener, handler);
                if (shouldEnableCallback) {
                    setSoftKeyboardCallbackEnabled(true);
                }
            }
        }

        public boolean removeOnShowModeChangedListener(OnShowModeChangedListener listener) {
            boolean hasKey;
            if (this.mListeners == null) {
                return false;
            }
            synchronized (this.mLock) {
                int keyIndex = this.mListeners.indexOfKey(listener);
                hasKey = keyIndex >= 0;
                if (hasKey) {
                    this.mListeners.removeAt(keyIndex);
                }
                if (hasKey && this.mListeners.isEmpty()) {
                    setSoftKeyboardCallbackEnabled(false);
                }
            }
            return hasKey;
        }

        private void setSoftKeyboardCallbackEnabled(boolean enabled) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    connection.setSoftKeyboardCallbackEnabled(enabled);
                } catch (RemoteException re) {
                    throw new RuntimeException(re);
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:10:0x0018, code lost:
            r0 = 0;
            r2 = r1.size();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
            if (r0 >= r2) goto L_0x003c;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
            r3 = r1.keyAt(r0);
            r4 = r1.valueAt(r0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
            if (r4 == null) goto L_0x0036;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002d, code lost:
            r4.post(new android.accessibilityservice.AccessibilityService.SoftKeyboardController.AnonymousClass1(r6));
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0036, code lost:
            r3.onShowModeChanged(r6, r7);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0039, code lost:
            r0 = r0 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x003c, code lost:
            return;
         */
        public void dispatchSoftKeyboardShowModeChanged(final int showMode) {
            synchronized (this.mLock) {
                if (this.mListeners != null) {
                    if (!this.mListeners.isEmpty()) {
                        ArrayMap<OnShowModeChangedListener, Handler> entries = new ArrayMap<>(this.mListeners);
                    }
                }
                Slog.w(AccessibilityService.LOG_TAG, "Received soft keyboard show mode changed callback with no listeners registered!");
                setSoftKeyboardCallbackEnabled(false);
            }
        }

        public int getShowMode() {
            try {
                return Settings.Secure.getInt(this.mService.getContentResolver(), "accessibility_soft_keyboard_mode");
            } catch (Settings.SettingNotFoundException e) {
                Log.v(AccessibilityService.LOG_TAG, "Failed to obtain the soft keyboard mode", e);
                return 0;
            }
        }

        public boolean setShowMode(int showMode) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.setSoftKeyboardShowMode(showMode);
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to set soft keyboard behavior", re);
                    re.rethrowFromSystemServer();
                }
            }
            return false;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SoftKeyboardShowMode {
    }

    public abstract void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    public abstract void onInterrupt();

    /* access modifiers changed from: private */
    public void dispatchServiceConnected() {
        if (this.mMagnificationController != null) {
            this.mMagnificationController.onServiceConnected();
        }
        if (this.mSoftKeyboardController != null) {
            this.mSoftKeyboardController.onServiceConnected();
        }
        onServiceConnected();
    }

    /* access modifiers changed from: protected */
    public void onServiceConnected() {
    }

    /* access modifiers changed from: protected */
    public boolean onGesture(int gestureId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    public List<AccessibilityWindowInfo> getWindows() {
        return AccessibilityInteractionClient.getInstance().getWindows(this.mConnectionId);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        return AccessibilityInteractionClient.getInstance().getRootInActiveWindow(this.mConnectionId);
    }

    public final void disableSelf() {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (connection != null) {
            try {
                connection.disableSelf();
            } catch (RemoteException re) {
                throw new RuntimeException(re);
            }
        }
    }

    public final MagnificationController getMagnificationController() {
        MagnificationController magnificationController;
        synchronized (this.mLock) {
            if (this.mMagnificationController == null) {
                this.mMagnificationController = new MagnificationController(this, this.mLock);
            }
            magnificationController = this.mMagnificationController;
        }
        return magnificationController;
    }

    public final FingerprintGestureController getFingerprintGestureController() {
        if (this.mFingerprintGestureController == null) {
            AccessibilityInteractionClient.getInstance();
            this.mFingerprintGestureController = new FingerprintGestureController(AccessibilityInteractionClient.getConnection(this.mConnectionId));
        }
        return this.mFingerprintGestureController;
    }

    public final boolean dispatchGesture(GestureDescription gesture, GestureResultCallback callback, Handler handler) {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (connection == null) {
            return false;
        }
        List<GestureDescription.GestureStep> steps = GestureDescription.MotionEventGenerator.getGestureStepsFromGestureDescription(gesture, 100);
        try {
            synchronized (this.mLock) {
                this.mGestureStatusCallbackSequence++;
                if (callback != null) {
                    if (this.mGestureStatusCallbackInfos == null) {
                        this.mGestureStatusCallbackInfos = new SparseArray<>();
                    }
                    this.mGestureStatusCallbackInfos.put(this.mGestureStatusCallbackSequence, new GestureResultCallbackInfo(gesture, callback, handler));
                }
                connection.sendGesture(this.mGestureStatusCallbackSequence, new ParceledListSlice(steps));
            }
            return true;
        } catch (RemoteException re) {
            throw new RuntimeException(re);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPerformGestureResult(int sequence, final boolean completedSuccessfully) {
        GestureResultCallbackInfo callbackInfo;
        if (this.mGestureStatusCallbackInfos != null) {
            synchronized (this.mLock) {
                callbackInfo = this.mGestureStatusCallbackInfos.get(sequence);
            }
            final GestureResultCallbackInfo finalCallbackInfo = callbackInfo;
            if (!(callbackInfo == null || callbackInfo.gestureDescription == null || callbackInfo.callback == null)) {
                if (callbackInfo.handler != null) {
                    callbackInfo.handler.post(new Runnable() {
                        public void run() {
                            if (completedSuccessfully) {
                                finalCallbackInfo.callback.onCompleted(finalCallbackInfo.gestureDescription);
                            } else {
                                finalCallbackInfo.callback.onCancelled(finalCallbackInfo.gestureDescription);
                            }
                        }
                    });
                } else if (completedSuccessfully) {
                    callbackInfo.callback.onCompleted(callbackInfo.gestureDescription);
                } else {
                    callbackInfo.callback.onCancelled(callbackInfo.gestureDescription);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
        if (this.mMagnificationController != null) {
            this.mMagnificationController.dispatchMagnificationChanged(region, scale, centerX, centerY);
        }
    }

    /* access modifiers changed from: private */
    public void onFingerprintCapturingGesturesChanged(boolean active) {
        getFingerprintGestureController().onGestureDetectionActiveChanged(active);
    }

    /* access modifiers changed from: private */
    public void onFingerprintGesture(int gesture) {
        getFingerprintGestureController().onGesture(gesture);
    }

    public final SoftKeyboardController getSoftKeyboardController() {
        SoftKeyboardController softKeyboardController;
        synchronized (this.mLock) {
            if (this.mSoftKeyboardController == null) {
                this.mSoftKeyboardController = new SoftKeyboardController(this, this.mLock);
            }
            softKeyboardController = this.mSoftKeyboardController;
        }
        return softKeyboardController;
    }

    /* access modifiers changed from: private */
    public void onSoftKeyboardShowModeChanged(int showMode) {
        if (this.mSoftKeyboardController != null) {
            this.mSoftKeyboardController.dispatchSoftKeyboardShowModeChanged(showMode);
        }
    }

    public final AccessibilityButtonController getAccessibilityButtonController() {
        AccessibilityButtonController accessibilityButtonController;
        synchronized (this.mLock) {
            if (this.mAccessibilityButtonController == null) {
                AccessibilityInteractionClient.getInstance();
                this.mAccessibilityButtonController = new AccessibilityButtonController(AccessibilityInteractionClient.getConnection(this.mConnectionId));
            }
            accessibilityButtonController = this.mAccessibilityButtonController;
        }
        return accessibilityButtonController;
    }

    /* access modifiers changed from: private */
    public void onAccessibilityButtonClicked() {
        getAccessibilityButtonController().dispatchAccessibilityButtonClicked();
    }

    /* access modifiers changed from: private */
    public void onAccessibilityButtonAvailabilityChanged(boolean available) {
        getAccessibilityButtonController().dispatchAccessibilityButtonAvailabilityChanged(available);
    }

    public final boolean performGlobalAction(int action) {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (connection != null) {
            try {
                return connection.performGlobalAction(action);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while calling performGlobalAction", re);
                re.rethrowFromSystemServer();
            }
        }
        return false;
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, -2, AccessibilityNodeInfo.ROOT_NODE_ID, focus);
    }

    public final AccessibilityServiceInfo getServiceInfo() {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (connection != null) {
            try {
                return connection.getServiceInfo();
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while getting AccessibilityServiceInfo", re);
                re.rethrowFromSystemServer();
            }
        }
        return null;
    }

    public final void setServiceInfo(AccessibilityServiceInfo info) {
        this.mInfo = info;
        sendServiceInfo();
    }

    private void sendServiceInfo() {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (this.mInfo != null && connection != null) {
            try {
                connection.setServiceInfo(this.mInfo);
                this.mInfo = null;
                AccessibilityInteractionClient.getInstance().clearCache();
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
                re.rethrowFromSystemServer();
            }
        }
    }

    public Object getSystemService(String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException("System services not available to Activities before onCreate()");
        } else if (!Context.WINDOW_SERVICE.equals(name)) {
            return super.getSystemService(name);
        } else {
            if (this.mWindowManager == null) {
                this.mWindowManager = (WindowManager) getBaseContext().getSystemService(name);
            }
            return this.mWindowManager;
        }
    }

    public final IBinder onBind(Intent intent) {
        return new IAccessibilityServiceClientWrapper(this, getMainLooper(), new Callbacks() {
            public void onServiceConnected() {
                AccessibilityService.this.dispatchServiceConnected();
            }

            public void onInterrupt() {
                AccessibilityService.this.onInterrupt();
            }

            public void onAccessibilityEvent(AccessibilityEvent event) {
                AccessibilityService.this.onAccessibilityEvent(event);
            }

            public void init(int connectionId, IBinder windowToken) {
                int unused = AccessibilityService.this.mConnectionId = connectionId;
                IBinder unused2 = AccessibilityService.this.mWindowToken = windowToken;
                ((WindowManagerImpl) AccessibilityService.this.getSystemService(Context.WINDOW_SERVICE)).setDefaultToken(windowToken);
            }

            public boolean onGesture(int gestureId) {
                return AccessibilityService.this.onGesture(gestureId);
            }

            public boolean onKeyEvent(KeyEvent event) {
                return AccessibilityService.this.onKeyEvent(event);
            }

            public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
                AccessibilityService.this.onMagnificationChanged(region, scale, centerX, centerY);
            }

            public void onSoftKeyboardShowModeChanged(int showMode) {
                AccessibilityService.this.onSoftKeyboardShowModeChanged(showMode);
            }

            public void onPerformGestureResult(int sequence, boolean completedSuccessfully) {
                AccessibilityService.this.onPerformGestureResult(sequence, completedSuccessfully);
            }

            public void onFingerprintCapturingGesturesChanged(boolean active) {
                AccessibilityService.this.onFingerprintCapturingGesturesChanged(active);
            }

            public void onFingerprintGesture(int gesture) {
                AccessibilityService.this.onFingerprintGesture(gesture);
            }

            public void onAccessibilityButtonClicked() {
                AccessibilityService.this.onAccessibilityButtonClicked();
            }

            public void onAccessibilityButtonAvailabilityChanged(boolean available) {
                AccessibilityService.this.onAccessibilityButtonAvailabilityChanged(available);
            }
        });
    }
}
