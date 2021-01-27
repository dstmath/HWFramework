package android.accessibilityservice;

import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.annotation.UnsupportedAppUsage;
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
    public static final int SHOW_MODE_HARD_KEYBOARD_ORIGINAL_VALUE = 536870912;
    public static final int SHOW_MODE_HARD_KEYBOARD_OVERRIDDEN = 1073741824;
    public static final int SHOW_MODE_HIDDEN = 1;
    public static final int SHOW_MODE_IGNORE_HARD_KEYBOARD = 2;
    public static final int SHOW_MODE_MASK = 3;
    private AccessibilityButtonController mAccessibilityButtonController;
    private int mConnectionId = -1;
    private FingerprintGestureController mFingerprintGestureController;
    private SparseArray<GestureResultCallbackInfo> mGestureStatusCallbackInfos;
    private int mGestureStatusCallbackSequence;
    @UnsupportedAppUsage
    private AccessibilityServiceInfo mInfo;
    private final Object mLock = new Object();
    private final SparseArray<MagnificationController> mMagnificationControllers = new SparseArray<>(0);
    private SoftKeyboardController mSoftKeyboardController;
    private WindowManager mWindowManager;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private IBinder mWindowToken;

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

        void onMagnificationChanged(int i, Region region, float f, float f2, float f3);

        void onPerformGestureResult(int i, boolean z);

        void onServiceConnected();

        void onSoftKeyboardShowModeChanged(int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SoftKeyboardShowMode {
    }

    public abstract void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    public abstract void onInterrupt();

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchServiceConnected() {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mMagnificationControllers.size(); i++) {
                this.mMagnificationControllers.valueAt(i).onServiceConnectedLocked();
            }
        }
        SoftKeyboardController softKeyboardController = this.mSoftKeyboardController;
        if (softKeyboardController != null) {
            softKeyboardController.onServiceConnected();
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
        return getMagnificationController(0);
    }

    public final MagnificationController getMagnificationController(int displayId) {
        MagnificationController controller;
        synchronized (this.mLock) {
            controller = this.mMagnificationControllers.get(displayId);
            if (controller == null) {
                controller = new MagnificationController(this, this.mLock, displayId);
                this.mMagnificationControllers.put(displayId, controller);
            }
        }
        return controller;
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
        final GestureResultCallbackInfo callbackInfo;
        if (this.mGestureStatusCallbackInfos != null) {
            synchronized (this.mLock) {
                callbackInfo = this.mGestureStatusCallbackInfos.get(sequence);
            }
            if (callbackInfo != null && callbackInfo.gestureDescription != null && callbackInfo.callback != null) {
                if (callbackInfo.handler != null) {
                    callbackInfo.handler.post(new Runnable() {
                        /* class android.accessibilityservice.AccessibilityService.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            if (completedSuccessfully) {
                                callbackInfo.callback.onCompleted(callbackInfo.gestureDescription);
                            } else {
                                callbackInfo.callback.onCancelled(callbackInfo.gestureDescription);
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
    /* access modifiers changed from: public */
    private void onMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) {
        MagnificationController controller;
        synchronized (this.mLock) {
            controller = this.mMagnificationControllers.get(displayId);
        }
        if (controller != null) {
            controller.dispatchMagnificationChanged(region, scale, centerX, centerY);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFingerprintCapturingGesturesChanged(boolean active) {
        getFingerprintGestureController().onGestureDetectionActiveChanged(active);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFingerprintGesture(int gesture) {
        getFingerprintGestureController().onGesture(gesture);
    }

    public static final class MagnificationController {
        private final int mDisplayId;
        private ArrayMap<OnMagnificationChangedListener, Handler> mListeners;
        private final Object mLock;
        private final AccessibilityService mService;

        public interface OnMagnificationChangedListener {
            void onMagnificationChanged(MagnificationController magnificationController, Region region, float f, float f2, float f3);
        }

        MagnificationController(AccessibilityService service, Object lock, int displayId) {
            this.mService = service;
            this.mLock = lock;
            this.mDisplayId = displayId;
        }

        /* access modifiers changed from: package-private */
        public void onServiceConnectedLocked() {
            ArrayMap<OnMagnificationChangedListener, Handler> arrayMap = this.mListeners;
            if (arrayMap != null && !arrayMap.isEmpty()) {
                setMagnificationCallbackEnabled(true);
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
                    connection.setMagnificationCallbackEnabled(this.mDisplayId, enabled);
                } catch (RemoteException re) {
                    throw new RuntimeException(re);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void dispatchMagnificationChanged(final Region region, final float scale, final float centerX, final float centerY) {
            ArrayMap<OnMagnificationChangedListener, Handler> entries;
            synchronized (this.mLock) {
                if (this.mListeners != null) {
                    if (!this.mListeners.isEmpty()) {
                        entries = new ArrayMap<>(this.mListeners);
                    }
                }
                Slog.d(AccessibilityService.LOG_TAG, "Received magnification changed callback with no listeners registered!");
                setMagnificationCallbackEnabled(false);
                return;
            }
            int count = entries.size();
            for (int i = 0; i < count; i++) {
                final OnMagnificationChangedListener listener = entries.keyAt(i);
                Handler handler = entries.valueAt(i);
                if (handler != null) {
                    handler.post(new Runnable() {
                        /* class android.accessibilityservice.AccessibilityService.MagnificationController.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            listener.onMagnificationChanged(MagnificationController.this, region, scale, centerX, centerY);
                        }
                    });
                } else {
                    listener.onMagnificationChanged(this, region, scale, centerX, centerY);
                }
            }
        }

        public float getScale() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return 1.0f;
            }
            try {
                return connection.getMagnificationScale(this.mDisplayId);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to obtain scale", re);
                re.rethrowFromSystemServer();
                return 1.0f;
            }
        }

        public float getCenterX() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return 0.0f;
            }
            try {
                return connection.getMagnificationCenterX(this.mDisplayId);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to obtain center X", re);
                re.rethrowFromSystemServer();
                return 0.0f;
            }
        }

        public float getCenterY() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return 0.0f;
            }
            try {
                return connection.getMagnificationCenterY(this.mDisplayId);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to obtain center Y", re);
                re.rethrowFromSystemServer();
                return 0.0f;
            }
        }

        public Region getMagnificationRegion() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.getMagnificationRegion(this.mDisplayId);
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
            if (connection == null) {
                return false;
            }
            try {
                return connection.resetMagnification(this.mDisplayId, animate);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to reset", re);
                re.rethrowFromSystemServer();
                return false;
            }
        }

        public boolean setScale(float scale, boolean animate) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return false;
            }
            try {
                return connection.setMagnificationScaleAndCenter(this.mDisplayId, scale, Float.NaN, Float.NaN, animate);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to set scale", re);
                re.rethrowFromSystemServer();
                return false;
            }
        }

        public boolean setCenter(float centerX, float centerY, boolean animate) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return false;
            }
            try {
                return connection.setMagnificationScaleAndCenter(this.mDisplayId, Float.NaN, centerX, centerY, animate);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to set center", re);
                re.rethrowFromSystemServer();
                return false;
            }
        }
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
    /* access modifiers changed from: public */
    private void onSoftKeyboardShowModeChanged(int showMode) {
        SoftKeyboardController softKeyboardController = this.mSoftKeyboardController;
        if (softKeyboardController != null) {
            softKeyboardController.dispatchSoftKeyboardShowModeChanged(showMode);
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
        public void dispatchSoftKeyboardShowModeChanged(final int showMode) {
            ArrayMap<OnShowModeChangedListener, Handler> entries;
            synchronized (this.mLock) {
                if (this.mListeners != null) {
                    if (!this.mListeners.isEmpty()) {
                        entries = new ArrayMap<>(this.mListeners);
                    }
                }
                Slog.w(AccessibilityService.LOG_TAG, "Received soft keyboard show mode changed callback with no listeners registered!");
                setSoftKeyboardCallbackEnabled(false);
                return;
            }
            int count = entries.size();
            for (int i = 0; i < count; i++) {
                final OnShowModeChangedListener listener = entries.keyAt(i);
                Handler handler = entries.valueAt(i);
                if (handler != null) {
                    handler.post(new Runnable() {
                        /* class android.accessibilityservice.AccessibilityService.SoftKeyboardController.AnonymousClass1 */

                        @Override // java.lang.Runnable
                        public void run() {
                            listener.onShowModeChanged(SoftKeyboardController.this, showMode);
                        }
                    });
                } else {
                    listener.onShowModeChanged(this, showMode);
                }
            }
        }

        public int getShowMode() {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return 0;
            }
            try {
                return connection.getSoftKeyboardShowMode();
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to set soft keyboard behavior", re);
                re.rethrowFromSystemServer();
                return 0;
            }
        }

        public boolean setShowMode(int showMode) {
            AccessibilityInteractionClient.getInstance();
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mService.mConnectionId);
            if (connection == null) {
                return false;
            }
            try {
                return connection.setSoftKeyboardShowMode(showMode);
            } catch (RemoteException re) {
                Log.w(AccessibilityService.LOG_TAG, "Failed to set soft keyboard behavior", re);
                re.rethrowFromSystemServer();
                return false;
            }
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
    /* access modifiers changed from: public */
    private void onAccessibilityButtonClicked() {
        getAccessibilityButtonController().dispatchAccessibilityButtonClicked();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onAccessibilityButtonAvailabilityChanged(boolean available) {
        getAccessibilityButtonController().dispatchAccessibilityButtonAvailabilityChanged(available);
    }

    public final boolean performGlobalAction(int action) {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (connection == null) {
            return false;
        }
        try {
            return connection.performGlobalAction(action);
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling performGlobalAction", re);
            re.rethrowFromSystemServer();
            return false;
        }
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, -2, AccessibilityNodeInfo.ROOT_NODE_ID, focus);
    }

    public final AccessibilityServiceInfo getServiceInfo() {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        if (connection == null) {
            return null;
        }
        try {
            return connection.getServiceInfo();
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while getting AccessibilityServiceInfo", re);
            re.rethrowFromSystemServer();
            return null;
        }
    }

    public final void setServiceInfo(AccessibilityServiceInfo info) {
        this.mInfo = info;
        sendServiceInfo();
    }

    private void sendServiceInfo() {
        AccessibilityInteractionClient.getInstance();
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        AccessibilityServiceInfo accessibilityServiceInfo = this.mInfo;
        if (accessibilityServiceInfo != null && connection != null) {
            try {
                connection.setServiceInfo(accessibilityServiceInfo);
                this.mInfo = null;
                AccessibilityInteractionClient.getInstance().clearCache();
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
                re.rethrowFromSystemServer();
            }
        }
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Object getSystemService(String name) {
        if (getBaseContext() == null) {
            throw new IllegalStateException("System services not available to Activities before onCreate()");
        } else if (!"window".equals(name)) {
            return super.getSystemService(name);
        } else {
            if (this.mWindowManager == null) {
                this.mWindowManager = (WindowManager) getBaseContext().getSystemService(name);
            }
            return this.mWindowManager;
        }
    }

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return new IAccessibilityServiceClientWrapper(this, getMainLooper(), new Callbacks() {
            /* class android.accessibilityservice.AccessibilityService.AnonymousClass2 */

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onServiceConnected() {
                AccessibilityService.this.dispatchServiceConnected();
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onInterrupt() {
                AccessibilityService.this.onInterrupt();
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onAccessibilityEvent(AccessibilityEvent event) {
                AccessibilityService.this.onAccessibilityEvent(event);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void init(int connectionId, IBinder windowToken) {
                AccessibilityService.this.mConnectionId = connectionId;
                AccessibilityService.this.mWindowToken = windowToken;
                ((WindowManagerImpl) AccessibilityService.this.getSystemService("window")).setDefaultToken(windowToken);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public boolean onGesture(int gestureId) {
                return AccessibilityService.this.onGesture(gestureId);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public boolean onKeyEvent(KeyEvent event) {
                return AccessibilityService.this.onKeyEvent(event);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) {
                AccessibilityService.this.onMagnificationChanged(displayId, region, scale, centerX, centerY);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onSoftKeyboardShowModeChanged(int showMode) {
                AccessibilityService.this.onSoftKeyboardShowModeChanged(showMode);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onPerformGestureResult(int sequence, boolean completedSuccessfully) {
                AccessibilityService.this.onPerformGestureResult(sequence, completedSuccessfully);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onFingerprintCapturingGesturesChanged(boolean active) {
                AccessibilityService.this.onFingerprintCapturingGesturesChanged(active);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onFingerprintGesture(int gesture) {
                AccessibilityService.this.onFingerprintGesture(gesture);
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onAccessibilityButtonClicked() {
                AccessibilityService.this.onAccessibilityButtonClicked();
            }

            @Override // android.accessibilityservice.AccessibilityService.Callbacks
            public void onAccessibilityButtonAvailabilityChanged(boolean available) {
                AccessibilityService.this.onAccessibilityButtonAvailabilityChanged(available);
            }
        });
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

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void init(IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIOO(1, connectionId, connection, windowToken));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onInterrupt() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(2));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onAccessibilityEvent(AccessibilityEvent event, boolean serviceWantsEvent) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageBO(3, serviceWantsEvent, event));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onGesture(int gestureId) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(4, gestureId));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void clearAccessibilityCache() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(5));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onKeyEvent(KeyEvent event, int sequence) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIO(6, sequence, event));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = region;
            args.arg2 = Float.valueOf(scale);
            args.arg3 = Float.valueOf(centerX);
            args.arg4 = Float.valueOf(centerY);
            args.argi1 = displayId;
            this.mCaller.sendMessage(this.mCaller.obtainMessageO(7, args));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onSoftKeyboardShowModeChanged(int showMode) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(8, showMode));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onPerformGestureResult(int sequence, boolean successfully) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageII(9, sequence, successfully ? 1 : 0));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onFingerprintCapturingGesturesChanged(boolean active) {
            HandlerCaller handlerCaller = this.mCaller;
            handlerCaller.sendMessage(handlerCaller.obtainMessageI(10, active ? 1 : 0));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onFingerprintGesture(int gesture) {
            HandlerCaller handlerCaller = this.mCaller;
            handlerCaller.sendMessage(handlerCaller.obtainMessageI(11, gesture));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onAccessibilityButtonClicked() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(12));
        }

        @Override // android.accessibilityservice.IAccessibilityServiceClient
        public void onAccessibilityButtonAvailabilityChanged(boolean available) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(13, available ? 1 : 0));
        }

        @Override // com.android.internal.os.HandlerCaller.Callback
        public void executeMessage(Message message) {
            boolean available = false;
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
                        return;
                    }
                    AccessibilityInteractionClient.getInstance();
                    AccessibilityInteractionClient.removeConnection(this.mConnectionId);
                    this.mConnectionId = -1;
                    AccessibilityInteractionClient.getInstance().clearCache();
                    this.mCallback.init(-1, null);
                    return;
                case 2:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onInterrupt();
                        return;
                    }
                    return;
                case 3:
                    AccessibilityEvent event = (AccessibilityEvent) message.obj;
                    if (message.arg1 != 0) {
                        available = true;
                    }
                    if (event != null) {
                        AccessibilityInteractionClient.getInstance().onAccessibilityEvent(event);
                        if (available && this.mConnectionId != -1) {
                            this.mCallback.onAccessibilityEvent(event);
                        }
                        try {
                            event.recycle();
                            return;
                        } catch (IllegalStateException e) {
                            return;
                        }
                    } else {
                        return;
                    }
                case 4:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onGesture(message.arg1);
                        return;
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
                        try {
                            return;
                        } catch (IllegalStateException e3) {
                            return;
                        }
                    } finally {
                        try {
                            event2.recycle();
                        } catch (IllegalStateException e4) {
                        }
                    }
                case 7:
                    if (this.mConnectionId != -1) {
                        SomeArgs args2 = (SomeArgs) message.obj;
                        float scale = ((Float) args2.arg2).floatValue();
                        float centerX = ((Float) args2.arg3).floatValue();
                        float centerY = ((Float) args2.arg4).floatValue();
                        int displayId = args2.argi1;
                        args2.recycle();
                        this.mCallback.onMagnificationChanged(displayId, (Region) args2.arg1, scale, centerX, centerY);
                        return;
                    }
                    return;
                case 8:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onSoftKeyboardShowModeChanged(message.arg1);
                        return;
                    }
                    return;
                case 9:
                    if (this.mConnectionId != -1) {
                        if (message.arg2 == 1) {
                            available = true;
                        }
                        this.mCallback.onPerformGestureResult(message.arg1, available);
                        return;
                    }
                    return;
                case 10:
                    if (this.mConnectionId != -1) {
                        Callbacks callbacks = this.mCallback;
                        if (message.arg1 == 1) {
                            available = true;
                        }
                        callbacks.onFingerprintCapturingGesturesChanged(available);
                        return;
                    }
                    return;
                case 11:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onFingerprintGesture(message.arg1);
                        return;
                    }
                    return;
                case 12:
                    if (this.mConnectionId != -1) {
                        this.mCallback.onAccessibilityButtonClicked();
                        return;
                    }
                    return;
                case 13:
                    if (this.mConnectionId != -1) {
                        if (message.arg1 != 0) {
                            available = true;
                        }
                        this.mCallback.onAccessibilityButtonAvailabilityChanged(available);
                        return;
                    }
                    return;
                default:
                    Log.w(AccessibilityService.LOG_TAG, "Unknown message type " + message.what);
                    return;
            }
        }
    }

    public static abstract class GestureResultCallback {
        public void onCompleted(GestureDescription gestureDescription) {
        }

        public void onCancelled(GestureDescription gestureDescription) {
        }
    }

    /* access modifiers changed from: private */
    public static class GestureResultCallbackInfo {
        GestureResultCallback callback;
        GestureDescription gestureDescription;
        Handler handler;

        GestureResultCallbackInfo(GestureDescription gestureDescription2, GestureResultCallback callback2, Handler handler2) {
            this.gestureDescription = gestureDescription2;
            this.callback = callback2;
            this.handler = handler2;
        }
    }
}
