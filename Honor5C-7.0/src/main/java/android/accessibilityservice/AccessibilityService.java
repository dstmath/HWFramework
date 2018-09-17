package android.accessibilityservice;

import android.accessibilityservice.GestureDescription.GestureStep;
import android.accessibilityservice.GestureDescription.MotionEventGenerator;
import android.accessibilityservice.IAccessibilityServiceClient.Stub;
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
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.rms.AppAssociate;
import android.speech.tts.TextToSpeech.Engine;
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
import com.android.internal.os.HandlerCaller.Callback;
import com.android.internal.os.SomeArgs;
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
    public static final int GLOBAL_ACTION_NOTIFICATIONS = 4;
    public static final int GLOBAL_ACTION_POWER_DIALOG = 6;
    public static final int GLOBAL_ACTION_QUICK_SETTINGS = 5;
    public static final int GLOBAL_ACTION_RECENTS = 3;
    public static final int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = 7;
    private static final String LOG_TAG = "AccessibilityService";
    public static final String SERVICE_INTERFACE = "android.accessibilityservice.AccessibilityService";
    public static final String SERVICE_META_DATA = "android.accessibilityservice";
    public static final int SHOW_MODE_AUTO = 0;
    public static final int SHOW_MODE_HIDDEN = 1;
    private int mConnectionId;
    private SparseArray<GestureResultCallbackInfo> mGestureStatusCallbackInfos;
    private int mGestureStatusCallbackSequence;
    private AccessibilityServiceInfo mInfo;
    private final Object mLock;
    private MagnificationController mMagnificationController;
    private SoftKeyboardController mSoftKeyboardController;
    private WindowManager mWindowManager;
    private IBinder mWindowToken;

    /* renamed from: android.accessibilityservice.AccessibilityService.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ boolean val$completedSuccessfully;
        final /* synthetic */ GestureResultCallbackInfo val$finalCallbackInfo;

        AnonymousClass1(boolean val$completedSuccessfully, GestureResultCallbackInfo val$finalCallbackInfo) {
            this.val$completedSuccessfully = val$completedSuccessfully;
            this.val$finalCallbackInfo = val$finalCallbackInfo;
        }

        public void run() {
            if (this.val$completedSuccessfully) {
                this.val$finalCallbackInfo.callback.onCompleted(this.val$finalCallbackInfo.gestureDescription);
            } else {
                this.val$finalCallbackInfo.callback.onCancelled(this.val$finalCallbackInfo.gestureDescription);
            }
        }
    }

    public interface Callbacks {
        void init(int i, IBinder iBinder);

        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);

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

        GestureResultCallbackInfo(GestureDescription gestureDescription, GestureResultCallback callback, Handler handler) {
            this.gestureDescription = gestureDescription;
            this.callback = callback;
            this.handler = handler;
        }
    }

    public static class IAccessibilityServiceClientWrapper extends Stub implements Callback {
        private static final int DO_CLEAR_ACCESSIBILITY_CACHE = 5;
        private static final int DO_GESTURE_COMPLETE = 9;
        private static final int DO_INIT = 1;
        private static final int DO_ON_ACCESSIBILITY_EVENT = 3;
        private static final int DO_ON_GESTURE = 4;
        private static final int DO_ON_INTERRUPT = 2;
        private static final int DO_ON_KEY_EVENT = 6;
        private static final int DO_ON_MAGNIFICATION_CHANGED = 7;
        private static final int DO_ON_SOFT_KEYBOARD_SHOW_MODE_CHANGED = 8;
        private final Callbacks mCallback;
        private final HandlerCaller mCaller;
        private int mConnectionId;

        public IAccessibilityServiceClientWrapper(Context context, Looper looper, Callbacks callback) {
            this.mCallback = callback;
            this.mCaller = new HandlerCaller(context, looper, this, true);
        }

        public void init(IAccessibilityServiceConnection connection, int connectionId, IBinder windowToken) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIOO(DO_INIT, connectionId, connection, windowToken));
        }

        public void onInterrupt() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(DO_ON_INTERRUPT));
        }

        public void onAccessibilityEvent(AccessibilityEvent event) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageO(DO_ON_ACCESSIBILITY_EVENT, event));
        }

        public void onGesture(int gestureId) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(DO_ON_GESTURE, gestureId));
        }

        public void clearAccessibilityCache() {
            this.mCaller.sendMessage(this.mCaller.obtainMessage(DO_CLEAR_ACCESSIBILITY_CACHE));
        }

        public void onKeyEvent(KeyEvent event, int sequence) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageIO(DO_ON_KEY_EVENT, sequence, event));
        }

        public void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = region;
            args.arg2 = Float.valueOf(scale);
            args.arg3 = Float.valueOf(centerX);
            args.arg4 = Float.valueOf(centerY);
            this.mCaller.sendMessage(this.mCaller.obtainMessageO(DO_ON_MAGNIFICATION_CHANGED, args));
        }

        public void onSoftKeyboardShowModeChanged(int showMode) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageI(DO_ON_SOFT_KEYBOARD_SHOW_MODE_CHANGED, showMode));
        }

        public void onPerformGestureResult(int sequence, boolean successfully) {
            this.mCaller.sendMessage(this.mCaller.obtainMessageII(DO_GESTURE_COMPLETE, sequence, successfully ? DO_INIT : AccessibilityService.SHOW_MODE_AUTO));
        }

        public void executeMessage(Message message) {
            SomeArgs args;
            IAccessibilityServiceConnection connection;
            switch (message.what) {
                case DO_INIT /*1*/:
                    this.mConnectionId = message.arg1;
                    args = message.obj;
                    connection = args.arg1;
                    IBinder windowToken = args.arg2;
                    args.recycle();
                    if (connection != null) {
                        AccessibilityInteractionClient.getInstance().addConnection(this.mConnectionId, connection);
                        this.mCallback.init(this.mConnectionId, windowToken);
                        this.mCallback.onServiceConnected();
                    } else {
                        AccessibilityInteractionClient.getInstance().removeConnection(this.mConnectionId);
                        this.mConnectionId = -1;
                        AccessibilityInteractionClient.getInstance().clearCache();
                        this.mCallback.init(-1, null);
                    }
                case DO_ON_INTERRUPT /*2*/:
                    this.mCallback.onInterrupt();
                case DO_ON_ACCESSIBILITY_EVENT /*3*/:
                    AccessibilityEvent event = message.obj;
                    if (event != null) {
                        AccessibilityInteractionClient.getInstance().onAccessibilityEvent(event);
                        this.mCallback.onAccessibilityEvent(event);
                        try {
                            event.recycle();
                        } catch (IllegalStateException e) {
                        }
                    }
                case DO_ON_GESTURE /*4*/:
                    this.mCallback.onGesture(message.arg1);
                case DO_CLEAR_ACCESSIBILITY_CACHE /*5*/:
                    AccessibilityInteractionClient.getInstance().clearCache();
                case DO_ON_KEY_EVENT /*6*/:
                    KeyEvent event2 = message.obj;
                    try {
                        connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
                        if (connection != null) {
                            try {
                                connection.setOnKeyEventResult(this.mCallback.onKeyEvent(event2), message.arg1);
                            } catch (RemoteException e2) {
                            }
                        }
                    } finally {
                        try {
                            event2.recycle();
                        } catch (IllegalStateException e3) {
                        }
                    }
                case DO_ON_MAGNIFICATION_CHANGED /*7*/:
                    args = (SomeArgs) message.obj;
                    this.mCallback.onMagnificationChanged(args.arg1, ((Float) args.arg2).floatValue(), ((Float) args.arg3).floatValue(), ((Float) args.arg4).floatValue());
                case DO_ON_SOFT_KEYBOARD_SHOW_MODE_CHANGED /*8*/:
                    this.mCallback.onSoftKeyboardShowModeChanged(message.arg1);
                case DO_GESTURE_COMPLETE /*9*/:
                    int i = message.arg2;
                    this.mCallback.onPerformGestureResult(message.arg1, r0 == DO_INIT);
                default:
                    Log.w(AccessibilityService.LOG_TAG, "Unknown message type " + message.what);
            }
        }
    }

    public static final class MagnificationController {
        private ArrayMap<OnMagnificationChangedListener, Handler> mListeners;
        private final Object mLock;
        private final AccessibilityService mService;

        /* renamed from: android.accessibilityservice.AccessibilityService.MagnificationController.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ float val$centerX;
            final /* synthetic */ float val$centerY;
            final /* synthetic */ OnMagnificationChangedListener val$listener;
            final /* synthetic */ Region val$region;
            final /* synthetic */ float val$scale;

            AnonymousClass1(OnMagnificationChangedListener val$listener, Region val$region, float val$scale, float val$centerX, float val$centerY) {
                this.val$listener = val$listener;
                this.val$region = val$region;
                this.val$scale = val$scale;
                this.val$centerX = val$centerX;
                this.val$centerY = val$centerY;
            }

            public void run() {
                this.val$listener.onMagnificationChanged(MagnificationController.this, this.val$region, this.val$scale, this.val$centerX, this.val$centerY);
            }
        }

        public interface OnMagnificationChangedListener {
            void onMagnificationChanged(MagnificationController magnificationController, Region region, float f, float f2, float f3);
        }

        MagnificationController(AccessibilityService service, Object lock) {
            this.mService = service;
            this.mLock = lock;
        }

        void onServiceConnected() {
            synchronized (this.mLock) {
                if (!(this.mListeners == null || this.mListeners.isEmpty())) {
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
                    this.mListeners = new ArrayMap();
                }
                boolean shouldEnableCallback = this.mListeners.isEmpty();
                this.mListeners.put(listener, handler);
                if (shouldEnableCallback) {
                    setMagnificationCallbackEnabled(true);
                }
            }
        }

        public boolean removeListener(OnMagnificationChangedListener listener) {
            if (this.mListeners == null) {
                return false;
            }
            boolean hasKey;
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    connection.setMagnificationCallbackEnabled(enabled);
                } catch (RemoteException re) {
                    throw new RuntimeException(re);
                }
            }
        }

        void dispatchMagnificationChanged(Region region, float scale, float centerX, float centerY) {
            synchronized (this.mLock) {
                if (this.mListeners == null || this.mListeners.isEmpty()) {
                    Slog.d(AccessibilityService.LOG_TAG, "Received magnification changed callback with no listeners registered!");
                    setMagnificationCallbackEnabled(false);
                    return;
                }
                ArrayMap<OnMagnificationChangedListener, Handler> entries = new ArrayMap(this.mListeners);
                int count = entries.size();
                for (int i = AccessibilityService.SHOW_MODE_AUTO; i < count; i += AccessibilityService.SHOW_MODE_HIDDEN) {
                    OnMagnificationChangedListener listener = (OnMagnificationChangedListener) entries.keyAt(i);
                    Handler handler = (Handler) entries.valueAt(i);
                    if (handler != null) {
                        handler.post(new AnonymousClass1(listener, region, scale, centerX, centerY));
                    } else {
                        listener.onMagnificationChanged(this, region, scale, centerX, centerY);
                    }
                }
            }
        }

        public float getScale() {
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    return connection.getMagnificationScale();
                } catch (RemoteException re) {
                    Log.w(AccessibilityService.LOG_TAG, "Failed to obtain scale", re);
                    re.rethrowFromSystemServer();
                }
            }
            return Engine.DEFAULT_VOLUME;
        }

        public float getCenterX() {
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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

        /* renamed from: android.accessibilityservice.AccessibilityService.SoftKeyboardController.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ OnShowModeChangedListener val$listener;
            final /* synthetic */ int val$showMode;

            AnonymousClass1(OnShowModeChangedListener val$listener, int val$showMode) {
                this.val$listener = val$listener;
                this.val$showMode = val$showMode;
            }

            public void run() {
                this.val$listener.onShowModeChanged(SoftKeyboardController.this, this.val$showMode);
            }
        }

        public interface OnShowModeChangedListener {
            void onShowModeChanged(SoftKeyboardController softKeyboardController, int i);
        }

        SoftKeyboardController(AccessibilityService service, Object lock) {
            this.mService = service;
            this.mLock = lock;
        }

        void onServiceConnected() {
            synchronized (this.mLock) {
                if (!(this.mListeners == null || this.mListeners.isEmpty())) {
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
                    this.mListeners = new ArrayMap();
                }
                boolean shouldEnableCallback = this.mListeners.isEmpty();
                this.mListeners.put(listener, handler);
                if (shouldEnableCallback) {
                    setSoftKeyboardCallbackEnabled(true);
                }
            }
        }

        public boolean removeOnShowModeChangedListener(OnShowModeChangedListener listener) {
            if (this.mListeners == null) {
                return false;
            }
            boolean hasKey;
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
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
            if (connection != null) {
                try {
                    connection.setSoftKeyboardCallbackEnabled(enabled);
                } catch (RemoteException re) {
                    throw new RuntimeException(re);
                }
            }
        }

        void dispatchSoftKeyboardShowModeChanged(int showMode) {
            synchronized (this.mLock) {
                if (this.mListeners == null || this.mListeners.isEmpty()) {
                    Slog.d(AccessibilityService.LOG_TAG, "Received soft keyboard show mode changed callback with no listeners registered!");
                    setSoftKeyboardCallbackEnabled(false);
                    return;
                }
                ArrayMap<OnShowModeChangedListener, Handler> entries = new ArrayMap(this.mListeners);
                int count = entries.size();
                for (int i = AccessibilityService.SHOW_MODE_AUTO; i < count; i += AccessibilityService.SHOW_MODE_HIDDEN) {
                    OnShowModeChangedListener listener = (OnShowModeChangedListener) entries.keyAt(i);
                    Handler handler = (Handler) entries.valueAt(i);
                    if (handler != null) {
                        handler.post(new AnonymousClass1(listener, showMode));
                    } else {
                        listener.onShowModeChanged(this, showMode);
                    }
                }
            }
        }

        public int getShowMode() {
            try {
                return Secure.getInt(this.mService.getContentResolver(), Secure.ACCESSIBILITY_SOFT_KEYBOARD_MODE);
            } catch (SettingNotFoundException e) {
                Log.v(AccessibilityService.LOG_TAG, "Failed to obtain the soft keyboard mode", e);
                return AccessibilityService.SHOW_MODE_AUTO;
            }
        }

        public boolean setShowMode(int showMode) {
            IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mService.mConnectionId);
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

    public abstract void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);

    public abstract void onInterrupt();

    public AccessibilityService() {
        this.mLock = new Object();
    }

    private void dispatchServiceConnected() {
        if (this.mMagnificationController != null) {
            this.mMagnificationController.onServiceConnected();
        }
        onServiceConnected();
    }

    protected void onServiceConnected() {
    }

    protected boolean onGesture(int gestureId) {
        return false;
    }

    protected boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    public List<AccessibilityWindowInfo> getWindows() {
        return AccessibilityInteractionClient.getInstance().getWindows(this.mConnectionId);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        return AccessibilityInteractionClient.getInstance().getRootInActiveWindow(this.mConnectionId);
    }

    public final void disableSelf() {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
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

    public final boolean dispatchGesture(GestureDescription gesture, GestureResultCallback callback, Handler handler) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
        if (connection == null) {
            return false;
        }
        List<GestureStep> steps = MotionEventGenerator.getGestureStepsFromGestureDescription(gesture, 100);
        try {
            synchronized (this.mLock) {
                this.mGestureStatusCallbackSequence += SHOW_MODE_HIDDEN;
                if (callback != null) {
                    if (this.mGestureStatusCallbackInfos == null) {
                        this.mGestureStatusCallbackInfos = new SparseArray();
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

    void onPerformGestureResult(int sequence, boolean completedSuccessfully) {
        if (this.mGestureStatusCallbackInfos != null) {
            GestureResultCallbackInfo callbackInfo;
            synchronized (this.mLock) {
                callbackInfo = (GestureResultCallbackInfo) this.mGestureStatusCallbackInfos.get(sequence);
            }
            GestureResultCallbackInfo finalCallbackInfo = callbackInfo;
            if (!(callbackInfo == null || callbackInfo.gestureDescription == null || callbackInfo.callback == null)) {
                if (callbackInfo.handler != null) {
                    callbackInfo.handler.post(new AnonymousClass1(completedSuccessfully, callbackInfo));
                } else if (completedSuccessfully) {
                    callbackInfo.callback.onCompleted(callbackInfo.gestureDescription);
                } else {
                    callbackInfo.callback.onCancelled(callbackInfo.gestureDescription);
                }
            }
        }
    }

    private void onMagnificationChanged(Region region, float scale, float centerX, float centerY) {
        if (this.mMagnificationController != null) {
            this.mMagnificationController.dispatchMagnificationChanged(region, scale, centerX, centerY);
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

    private void onSoftKeyboardShowModeChanged(int showMode) {
        if (this.mSoftKeyboardController != null) {
            this.mSoftKeyboardController.dispatchSoftKeyboardShowModeChanged(showMode);
        }
    }

    public final boolean performGlobalAction(int action) {
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
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
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
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
        IAccessibilityServiceConnection connection = AccessibilityInteractionClient.getInstance().getConnection(this.mConnectionId);
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
        } else if (!AppAssociate.ASSOC_WINDOW.equals(name)) {
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
                AccessibilityService.this.mConnectionId = connectionId;
                AccessibilityService.this.mWindowToken = windowToken;
                ((WindowManagerImpl) AccessibilityService.this.getSystemService(AppAssociate.ASSOC_WINDOW)).setDefaultToken(windowToken);
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
        });
    }
}
