package android.accessibilityservice;

import android.accessibilityservice.AccessibilityButtonController;
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
    public void dispatchAccessibilityButtonClicked() {
        ArrayMap<AccessibilityButtonCallback, Handler> entries;
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                if (!this.mCallbacks.isEmpty()) {
                    entries = new ArrayMap<>(this.mCallbacks);
                }
            }
            Slog.w(LOG_TAG, "Received accessibility button click with no callbacks!");
            return;
        }
        int count = entries.size();
        for (int i = 0; i < count; i++) {
            entries.valueAt(i).post(new Runnable(entries.keyAt(i)) {
                /* class android.accessibilityservice.$$Lambda$AccessibilityButtonController$b_UAM9QJWcH4KQOC_odiN0t_boU */
                private final /* synthetic */ AccessibilityButtonController.AccessibilityButtonCallback f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AccessibilityButtonController.this.lambda$dispatchAccessibilityButtonClicked$0$AccessibilityButtonController(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$dispatchAccessibilityButtonClicked$0$AccessibilityButtonController(AccessibilityButtonCallback callback) {
        callback.onClicked(this);
    }

    /* access modifiers changed from: package-private */
    public void dispatchAccessibilityButtonAvailabilityChanged(boolean available) {
        ArrayMap<AccessibilityButtonCallback, Handler> entries;
        synchronized (this.mLock) {
            if (this.mCallbacks != null) {
                if (!this.mCallbacks.isEmpty()) {
                    entries = new ArrayMap<>(this.mCallbacks);
                }
            }
            Slog.w(LOG_TAG, "Received accessibility button availability change with no callbacks!");
            return;
        }
        int count = entries.size();
        for (int i = 0; i < count; i++) {
            entries.valueAt(i).post(new Runnable(entries.keyAt(i), available) {
                /* class android.accessibilityservice.$$Lambda$AccessibilityButtonController$RskKrfcSyUz7I9Sqaziy1P990ZM */
                private final /* synthetic */ AccessibilityButtonController.AccessibilityButtonCallback f$1;
                private final /* synthetic */ boolean f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    AccessibilityButtonController.this.lambda$dispatchAccessibilityButtonAvailabilityChanged$1$AccessibilityButtonController(this.f$1, this.f$2);
                }
            });
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
