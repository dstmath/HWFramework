package android.accessibilityservice;

import android.accessibilityservice.FingerprintGestureController;
import android.os.Handler;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;

public final class FingerprintGestureController {
    public static final int FINGERPRINT_GESTURE_SWIPE_DOWN = 8;
    public static final int FINGERPRINT_GESTURE_SWIPE_LEFT = 2;
    public static final int FINGERPRINT_GESTURE_SWIPE_RIGHT = 1;
    public static final int FINGERPRINT_GESTURE_SWIPE_UP = 4;
    private static final String LOG_TAG = "FingerprintGestureController";
    private final IAccessibilityServiceConnection mAccessibilityServiceConnection;
    private final ArrayMap<FingerprintGestureCallback, Handler> mCallbackHandlerMap = new ArrayMap<>(1);
    private final Object mLock = new Object();

    @VisibleForTesting
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
            handlerMap = new ArrayMap<>(this.mCallbackHandlerMap);
        }
        int numListeners = handlerMap.size();
        for (int i = 0; i < numListeners; i++) {
            FingerprintGestureCallback callback = handlerMap.keyAt(i);
            Handler handler = handlerMap.valueAt(i);
            if (handler != null) {
                handler.post(new Runnable(active) {
                    /* class android.accessibilityservice.$$Lambda$FingerprintGestureController$MZApqp96G6ZF2WdWrGDJ8Qsfck */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        FingerprintGestureController.FingerprintGestureCallback.this.onGestureDetectionAvailabilityChanged(this.f$1);
                    }
                });
            } else {
                callback.onGestureDetectionAvailabilityChanged(active);
            }
        }
    }

    public void onGesture(int gesture) {
        ArrayMap<FingerprintGestureCallback, Handler> handlerMap;
        synchronized (this.mLock) {
            handlerMap = new ArrayMap<>(this.mCallbackHandlerMap);
        }
        int numListeners = handlerMap.size();
        for (int i = 0; i < numListeners; i++) {
            FingerprintGestureCallback callback = handlerMap.keyAt(i);
            Handler handler = handlerMap.valueAt(i);
            if (handler != null) {
                handler.post(new Runnable(gesture) {
                    /* class android.accessibilityservice.$$Lambda$FingerprintGestureController$BQjrQQom4K3C98FNiI0fi7SvHfY */
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        FingerprintGestureController.FingerprintGestureCallback.this.onGestureDetected(this.f$1);
                    }
                });
            } else {
                callback.onGestureDetected(gesture);
            }
        }
    }

    public static abstract class FingerprintGestureCallback {
        public void onGestureDetectionAvailabilityChanged(boolean available) {
        }

        public void onGestureDetected(int gesture) {
        }
    }
}
