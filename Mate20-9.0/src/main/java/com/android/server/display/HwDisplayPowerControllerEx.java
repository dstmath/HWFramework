package com.android.server.display;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.display.IHwDisplayPowerControllerEx;
import com.android.server.policy.WindowManagerPolicy;

public final class HwDisplayPowerControllerEx implements IHwDisplayPowerControllerEx {
    private static final int DOZE_MODE = 1;
    private static final String HW_SCREEN_OFF_FOR_POSITIVE = "hw.intent.action.HW_SCREEN_OFF_FOR_POSITIVE";
    private static final String HW_SCREEN_OFF_FOR_POSITIVE_PERMISSION = "com.huawei.permission.HW_SCREEN_OFF_FOR_POSITIVE";
    private static final String KEY_POSITIVE = "key_positive";
    private static final int NORMAL_MODE = 2;
    private static final int SCENE = 22;
    private static final String TAG = "HwDisplayPowerControllerEx";
    /* access modifiers changed from: private */
    public final IHwDisplayPowerControllerEx.Callbacks mCallbacks;
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean mPendingTpKeep = false;
    private boolean mProxPendingByPhone = false;
    private boolean mProxPositive = false;
    private boolean mProximityTop = SystemProperties.getBoolean("ro.config.proximity_top", false);
    private final SensorManager mSensorManager;
    /* access modifiers changed from: private */
    public boolean mTpKeep = false;
    private TpKeepChange mTpKeepChange;
    private final WindowManagerPolicy mWindowManagerPolicy;

    private final class TpKeepChange implements WindowManagerPolicy.TpKeepListener {
        private TpKeepChange() {
        }

        public void setTpKeep(boolean keep) {
            boolean unused = HwDisplayPowerControllerEx.this.mTpKeep = keep;
            if (HwDisplayPowerControllerEx.this.mPendingTpKeep != keep) {
                HwDisplayPowerControllerEx.this.mCallbacks.handlerSendTpKeepMsgEx();
                boolean unused2 = HwDisplayPowerControllerEx.this.mPendingTpKeep = keep;
            }
        }
    }

    public HwDisplayPowerControllerEx(Context context, IHwDisplayPowerControllerEx.Callbacks callbacks, SensorManager sensorManager) {
        this.mCallbacks = callbacks;
        this.mContext = context;
        this.mSensorManager = sensorManager;
        this.mWindowManagerPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
    }

    public void handleTpKeep() {
        Slog.d(TAG, "TpKeepChange mTpKeep= " + this.mTpKeep + ",mPendingTpKeep=: " + this.mPendingTpKeep + ",mProxPrositive= " + this.mProxPositive);
        this.mCallbacks.handleProximitySensorEventEx(SystemClock.uptimeMillis(), this.mProxPositive || this.mTpKeep);
    }

    public void initTpKeepParamters() {
        if (this.mProximityTop) {
            this.mTpKeepChange = new TpKeepChange();
            this.mWindowManagerPolicy.setTpKeep(this.mTpKeepChange);
        }
    }

    public boolean getTpKeep() {
        return this.mTpKeep;
    }

    public void setProxPositive(boolean proxPositive) {
        this.mProxPositive = proxPositive;
    }

    public void setTPDozeMode(boolean useProximitySensor) {
        if (this.mProximityTop && useProximitySensor != this.mProxPendingByPhone) {
            if (useProximitySensor) {
                this.mWindowManagerPolicy.setTPDozeMode(22, 1);
            } else {
                this.mWindowManagerPolicy.setTPDozeMode(22, 2);
            }
            this.mProxPendingByPhone = useProximitySensor;
        }
    }

    public void sendProximityBroadcast(boolean positive) {
        if (this.mContext == null) {
            Slog.e(TAG, "mContext is null, can not sendProximityBroadcast.");
            return;
        }
        Intent intent = new Intent(HW_SCREEN_OFF_FOR_POSITIVE);
        intent.putExtra(KEY_POSITIVE, positive);
        Slog.d(TAG, "sendProximityBroadcast: hw.intent.action.HW_SCREEN_OFF_FOR_POSITIVE");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, HW_SCREEN_OFF_FOR_POSITIVE_PERMISSION);
    }
}
