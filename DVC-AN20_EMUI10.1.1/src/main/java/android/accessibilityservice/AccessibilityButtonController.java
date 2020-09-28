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

    AccessibilityButtonController(IAccessibilityServiceConnection serviceConnection) {
        this.mServiceConnection = serviceConnection;
    }

    public boolean isAccessibilityButtonAvailable() {
        IAccessibilityServiceConnection iAccessibilityServiceConnection = this.mServiceConnection;
        if (iAccessibilityServiceConnection == null) {
            return false;
        }
        try {
            return iAccessibilityServiceConnection.isAccessibilityButtonAvailable();
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
                this.mCallbacks = new ArrayMap<>();
            }
            this.mCallbacks.put(callback, handler);
        }
    }

    public void unregisterAccessibilityButtonCallback(AccessibilityButtonCallback callback) {
        Preconditions.checkNotNull(callback);
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                int keyIndex = this.mCallbacks.indexOfKey(callback);
                if (keyIndex >= 0) {
                    this.mCallbacks.removeAt(keyIndex);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0018, code lost:
        r0 = 0;
        r2 = r1.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        if (r0 >= r2) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        r1.valueAt(r0).post(new android.accessibilityservice.$$Lambda$AccessibilityButtonController$b_UAM9QJWcH4KQOC_odiN0t_boU(r1.keyAt(r0)));
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0036, code lost:
        return;
     */
    public void dispatchAccessibilityButtonClicked() {
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                if (!this.mCallbacks.isEmpty()) {
                    ArrayMap<AccessibilityButtonCallback, Handler> entries = new ArrayMap<>(this.mCallbacks);
                }
            }
            Slog.w(LOG_TAG, "Received accessibility button click with no callbacks!");
        }
    }

    public /* synthetic */ void lambda$dispatchAccessibilityButtonClicked$0$AccessibilityButtonController(AccessibilityButtonCallback callback) {
        callback.onClicked(this);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0018, code lost:
        r0 = 0;
        r2 = r1.size();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        if (r0 >= r2) goto L_0x0036;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001f, code lost:
        r1.valueAt(r0).post(new android.accessibilityservice.$$Lambda$AccessibilityButtonController$RskKrfcSyUz7I9Sqaziy1P990ZM(r1.keyAt(r0), r7));
        r0 = r0 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0036, code lost:
        return;
     */
    public void dispatchAccessibilityButtonAvailabilityChanged(boolean available) {
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                if (!this.mCallbacks.isEmpty()) {
                    ArrayMap<AccessibilityButtonCallback, Handler> entries = new ArrayMap<>(this.mCallbacks);
                }
            }
            Slog.w(LOG_TAG, "Received accessibility button availability change with no callbacks!");
        }
    }

    public /* synthetic */ void lambda$dispatchAccessibilityButtonAvailabilityChanged$1$AccessibilityButtonController(AccessibilityButtonCallback callback, boolean available) {
        callback.onAvailabilityChanged(this, available);
    }

    public static abstract class AccessibilityButtonCallback {
        public void onClicked(AccessibilityButtonController controller) {
        }

        public void onAvailabilityChanged(AccessibilityButtonController controller, boolean available) {
        }
    }
}
