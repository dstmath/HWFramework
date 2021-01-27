package com.huawei.server.fsm;

import android.os.RemoteException;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import com.huawei.android.hidl.ITouchscreenHidlAdapter;
import com.huawei.android.os.HwBinderEx;
import com.huawei.android.server.LocalServicesExt;
import com.huawei.android.util.SlogEx;
import java.util.NoSuchElementException;

public final class TouchscreenNotify {
    private static final int FEATURE_SCREEN_STATUS_SWITCH_CTRL = 12;
    private static final String STATUS_EXPAND = "0";
    private static final String STATUS_FOLD = "1";
    private static final String STATUS_SCREEN_OFF_FOLD = "2";
    private static final String TAG = "TouchscreenNotify";
    private static final int TP_HAL_DEATH_COOKIE = 1000;
    private static TouchscreenNotify mInstance = new TouchscreenNotify();
    private HwFoldScreenManagerInternal mFsmInternal;
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
                    SlogEx.e(TAG, "mTouchscreenService is null, return");
                    return;
                }
            }
            String config = getTpConfig(isFolded);
            SlogEx.i(TAG, "setScreenStatusToTp config " + config);
            try {
                int result = this.mTouchscreenService.hwSetFeatureConfig((int) FEATURE_SCREEN_STATUS_SWITCH_CTRL, config);
                SlogEx.i(TAG, "setScreenStatusToTp result " + result);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "setScreenStatusToTp catch an RemoteException " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final class DeathRecipient extends HwBinderEx.DeathRecipientEx {
        DeathRecipient() {
        }

        public void serviceDied(long cookie) {
            if (cookie == 1000) {
                SlogEx.e(TouchscreenNotify.TAG, "tp hal service died cookie: " + cookie);
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
                SlogEx.i(TAG, "connectToService success.");
                this.mTouchscreenService.linkToDeath(new DeathRecipient(), (int) TP_HAL_DEATH_COOKIE);
                return;
            }
            SlogEx.e(TAG, "connectToService failed.");
        } catch (NoSuchElementException e) {
            SlogEx.e(TAG, "connectToService: tp hal service not found." + e.getMessage());
        } catch (RemoteException e2) {
            SlogEx.e(TAG, "connectToService: tp hal service not respond " + e2.getMessage());
        }
    }

    private String getTpConfig(boolean isFolded) {
        if (isScreenOffFold()) {
            return STATUS_SCREEN_OFF_FOLD;
        }
        return isFolded ? STATUS_FOLD : STATUS_EXPAND;
    }

    private boolean isScreenOffFold() {
        if (this.mFsmInternal == null) {
            this.mFsmInternal = (HwFoldScreenManagerInternal) LocalServicesExt.getService(HwFoldScreenManagerInternal.class);
            if (this.mTouchscreenService == null) {
                SlogEx.e(TAG, "mFsmInternal is null, return");
                return false;
            }
        }
        boolean isScreenOffFold = this.mFsmInternal.getInfoDrawWindow();
        SlogEx.i(TAG, "isScreenOffFold " + isScreenOffFold);
        return isScreenOffFold;
    }
}
