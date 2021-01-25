package huawei.com.android.server.fsm;

import android.os.RemoteException;
import android.util.Slog;
import com.huawei.android.hidl.ITouchscreenHidlAdapter;
import com.huawei.android.os.HwBinderEx;
import java.util.NoSuchElementException;

public final class TouchscreenNotify {
    private static final int FEATURE_SCREEN_STATUS_SWITCH_CTRL = 12;
    private static final String STATUS_EXPAND = "0";
    private static final String STATUS_FOLD = "1";
    private static final String TAG = "TouchscreenNotify";
    private static final int TP_HAL_DEATH_COOKIE = 1000;
    private static TouchscreenNotify mInstance = new TouchscreenNotify();
    private final Object mLock = new Object();
    private ITouchscreenHidlAdapter mTouchscreenService = null;

    private TouchscreenNotify() {
    }

    public static TouchscreenNotify getInstance() {
        return mInstance;
    }

    public void setScreenStatusToTp(boolean isFolded) {
        synchronized (this.mLock) {
            if (this.mTouchscreenService == null) {
                connectToService();
                if (this.mTouchscreenService == null) {
                    Slog.e(TAG, "mTouchscreenService is null, return");
                    return;
                }
            }
            String config = isFolded ? "1" : "0";
            Slog.i(TAG, "setScreenStatusToTp config " + config);
            try {
                int result = this.mTouchscreenService.hwSetFeatureConfig(12, config);
                Slog.i(TAG, "setScreenStatusToTp result " + result);
            } catch (RemoteException e) {
                Slog.e(TAG, "setScreenStatusToTp catch an RemoteException " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient extends HwBinderEx.DeathRecipientEx {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                Slog.e(TouchscreenNotify.TAG, "tp hal service died cookie: " + cookie);
                synchronized (TouchscreenNotify.this.mLock) {
                    TouchscreenNotify.this.mTouchscreenService = null;
                }
            }
        }
    }

    private void connectToService() {
        try {
            this.mTouchscreenService = ITouchscreenHidlAdapter.getService();
            if (this.mTouchscreenService != null) {
                Slog.i(TAG, "connectToService success.");
                this.mTouchscreenService.linkToDeath(new DeathRecipient(), 1000);
                return;
            }
            Slog.e(TAG, "connectToService failed.");
        } catch (NoSuchElementException e) {
            Slog.e(TAG, "connectToService: tp hal service not found." + e.getMessage());
        } catch (RemoteException e2) {
            Slog.e(TAG, "connectToService: tp hal service not respond " + e2.getMessage());
        }
    }
}
