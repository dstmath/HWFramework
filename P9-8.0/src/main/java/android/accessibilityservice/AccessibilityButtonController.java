package android.accessibilityservice;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.Preconditions;

public final class AccessibilityButtonController {
    private static final String LOG_TAG = "A11yButtonController";
    private ArrayMap<AccessibilityButtonCallback, Handler> mCallbacks;
    private final Object mLock = new Object();
    private final IAccessibilityServiceConnection mServiceConnection;

    public static abstract class AccessibilityButtonCallback {
        /* renamed from: onClicked */
        public void lambda$-android_accessibilityservice_AccessibilityButtonController_6699(AccessibilityButtonController controller) {
        }

        /* renamed from: onAvailabilityChanged */
        public void lambda$-android_accessibilityservice_AccessibilityButtonController_7728(AccessibilityButtonController controller, boolean available) {
        }
    }

    AccessibilityButtonController(IAccessibilityServiceConnection serviceConnection) {
        this.mServiceConnection = serviceConnection;
    }

    public boolean isAccessibilityButtonAvailable() {
        try {
            return this.mServiceConnection.isAccessibilityButtonAvailable();
        } catch (RemoteException re) {
            Slog.w(LOG_TAG, "Failed to get accessibility button availability.", re);
            re.rethrowFromSystemServer();
            return false;
        }
    }

    public void registerAccessibilityButtonCallback(AccessibilityButtonCallback callback) {
        registerAccessibilityButtonCallback(callback, new Handler(Looper.getMainLooper()));
    }

    public void registerAccessibilityButtonCallback(AccessibilityButtonCallback callback, Handler handler) {
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(handler);
        synchronized (this.mLock) {
            if (this.mCallbacks == null) {
                this.mCallbacks = new ArrayMap();
            }
            this.mCallbacks.put(callback, handler);
        }
    }

    /* JADX WARNING: Missing block: B:14:0x001d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void unregisterAccessibilityButtonCallback(AccessibilityButtonCallback callback) {
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            if (this.mCallbacks == null) {
                return;
            }
            int keyIndex = this.mCallbacks.indexOfKey(callback);
            if (keyIndex >= 0) {
                this.mCallbacks.removeAt(keyIndex);
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0022, code:
            r4 = 0;
            r1 = r2.size();
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            if (r4 >= r1) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:15:0x0029, code:
            ((android.os.Handler) r2.valueAt(r4)).post(new android.accessibilityservice.-$Lambda$kpEvk0Apj34UqdMMU09LT6cNwG4(r8, (android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback) r2.keyAt(r4)));
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:19:0x0043, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dispatchAccessibilityButtonClicked() {
        synchronized (this.mLock) {
            if (this.mCallbacks == null || this.mCallbacks.isEmpty()) {
                Slog.w(LOG_TAG, "Received accessibility button click with no callbacks!");
                return;
            }
            ArrayMap<AccessibilityButtonCallback, Handler> entries = new ArrayMap(this.mCallbacks);
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0022, code:
            r4 = 0;
            r1 = r2.size();
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            if (r4 >= r1) goto L_0x0043;
     */
    /* JADX WARNING: Missing block: B:15:0x0029, code:
            ((android.os.Handler) r2.valueAt(r4)).post(new android.accessibilityservice.-$Lambda$kpEvk0Apj34UqdMMU09LT6cNwG4.AnonymousClass1(r9, r8, (android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback) r2.keyAt(r4)));
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:19:0x0043, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void dispatchAccessibilityButtonAvailabilityChanged(boolean available) {
        synchronized (this.mLock) {
            if (this.mCallbacks == null || this.mCallbacks.isEmpty()) {
                Slog.w(LOG_TAG, "Received accessibility button availability change with no callbacks!");
                return;
            }
            ArrayMap<AccessibilityButtonCallback, Handler> entries = new ArrayMap(this.mCallbacks);
        }
    }
}
