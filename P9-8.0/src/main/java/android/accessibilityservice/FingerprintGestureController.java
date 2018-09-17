package android.accessibilityservice;

import android.accessibilityservice.-$Lambda$ktCbXYLeLcj2fWU6KTqcB7Ybd9k.AnonymousClass1;
import android.os.Handler;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;

public final class FingerprintGestureController {
    public static final int FINGERPRINT_GESTURE_SWIPE_DOWN = 8;
    public static final int FINGERPRINT_GESTURE_SWIPE_LEFT = 2;
    public static final int FINGERPRINT_GESTURE_SWIPE_RIGHT = 1;
    public static final int FINGERPRINT_GESTURE_SWIPE_UP = 4;
    private static final String LOG_TAG = "FingerprintGestureController";
    private final IAccessibilityServiceConnection mAccessibilityServiceConnection;
    private final ArrayMap<FingerprintGestureCallback, Handler> mCallbackHandlerMap = new ArrayMap(1);
    private final Object mLock = new Object();

    public static abstract class FingerprintGestureCallback {
        /* renamed from: onGestureDetectionAvailabilityChanged */
        public void lambda$-android_accessibilityservice_FingerprintGestureController_5924(boolean available) {
        }

        /* renamed from: onGestureDetected */
        public void lambda$-android_accessibilityservice_FingerprintGestureController_6679(int gesture) {
        }
    }

    public FingerprintGestureController(IAccessibilityServiceConnection connection) {
        this.mAccessibilityServiceConnection = connection;
    }

    public boolean isGestureDetectionAvailable() {
        try {
            return this.mAccessibilityServiceConnection.isFingerprintGestureDetectionAvailable();
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Failed to check if fingerprint gestures are active", re);
            re.rethrowFromSystemServer();
            return false;
        }
    }

    public void registerFingerprintGestureCallback(FingerprintGestureCallback callback, Handler handler) {
        synchronized (this.mLock) {
            this.mCallbackHandlerMap.put(callback, handler);
        }
    }

    public void unregisterFingerprintGestureCallback(FingerprintGestureCallback callback) {
        synchronized (this.mLock) {
            this.mCallbackHandlerMap.remove(callback);
        }
    }

    public void onGestureDetectionActiveChanged(boolean active) {
        ArrayMap<FingerprintGestureCallback, Handler> handlerMap;
        synchronized (this.mLock) {
            handlerMap = new ArrayMap(this.mCallbackHandlerMap);
        }
        int numListeners = handlerMap.size();
        for (int i = 0; i < numListeners; i++) {
            FingerprintGestureCallback callback = (FingerprintGestureCallback) handlerMap.keyAt(i);
            Handler handler = (Handler) handlerMap.valueAt(i);
            if (handler != null) {
                handler.post(new AnonymousClass1(active, callback));
            } else {
                callback.lambda$-android_accessibilityservice_FingerprintGestureController_5924(active);
            }
        }
    }

    public void onGesture(int gesture) {
        ArrayMap<FingerprintGestureCallback, Handler> handlerMap;
        synchronized (this.mLock) {
            handlerMap = new ArrayMap(this.mCallbackHandlerMap);
        }
        int numListeners = handlerMap.size();
        for (int i = 0; i < numListeners; i++) {
            FingerprintGestureCallback callback = (FingerprintGestureCallback) handlerMap.keyAt(i);
            Handler handler = (Handler) handlerMap.valueAt(i);
            if (handler != null) {
                handler.post(new -$Lambda$ktCbXYLeLcj2fWU6KTqcB7Ybd9k(gesture, callback));
            } else {
                callback.lambda$-android_accessibilityservice_FingerprintGestureController_6679(gesture);
            }
        }
    }
}
