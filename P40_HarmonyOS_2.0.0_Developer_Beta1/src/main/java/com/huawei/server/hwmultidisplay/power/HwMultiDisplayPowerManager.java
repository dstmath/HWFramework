package com.huawei.server.hwmultidisplay.power;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.HwPCUtils;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.hardware.input.HwInputManager;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UserHandleEx;
import com.huawei.screenrecorder.activities.SurfaceControlEx;

public class HwMultiDisplayPowerManager {
    private static final String ACTION_HWMULTIDISPLAY_SCREEN_OFF = "com.huawei.action.hwmultidisplay.SCREEN_OFF";
    private static final String ACTION_HWMULTIDISPLAY_SCREEN_ON = "com.huawei.action.hwmultidisplay.SCREEN_ON";
    private static final String AP_AOD_CONFIG_OFF = "0";
    private static final int AP_AOD_FEATURE = 11;
    private static final long DIM_TIMEOUT = 10000;
    private static final int GO_TO_SLEEP_FLAG_NO_DOZE = 1;
    private static final boolean IS_SUPPORT_AP_AOD = "2".equals(SystemPropertiesEx.get("ro.config.support_aod", (String) null));
    private static final Object LOCK = new Object();
    private static final int MSG_DIM_TIMEOUT = 1;
    private static final int MSG_UPDATE_SCREEN_STATE = 2;
    private static final int SCREEN_STATE_DIM = 2;
    private static final int SCREEN_STATE_OFF = 3;
    private static final int SCREEN_STATE_ON = 1;
    private static final String TAG = "HwMultiDisplayPowerManager";
    private static volatile HwMultiDisplayPowerManager sInstance = null;
    private Context mContext;
    private FoldDisplayListener mFoldDisplayListener;
    private boolean mIsDiming;
    private boolean mIsStateChangePending;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private PowerHandler mPowerHandler;
    private PowerManager mPowerManager;
    private int mScreenState = 1;
    private PowerManager.WakeLock mTriggerWakeLock;

    /* access modifiers changed from: private */
    public final class PowerHandler extends Handler {
        PowerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwMultiDisplayPowerManager.this.dimTimeout();
            } else if (i == 2) {
                HwMultiDisplayPowerManager.this.updateScreenState(msg.arg1);
            }
        }
    }

    private final class FoldDisplayListener implements HwFoldScreenManagerEx.FoldDisplayModeListener {
        private FoldDisplayListener() {
        }

        public void onScreenDisplayModeChange(int displayMode) {
            boolean isScreenOnFully = HwMultiDisplayPowerManager.this.isScreenOnFully();
            HwPCUtils.log(HwMultiDisplayPowerManager.TAG, "Fold display mode changed, mode:" + displayMode + ", isScreenOnFully:" + isScreenOnFully);
            if (!isScreenOnFully) {
                HwMultiDisplayPowerManager.this.setScreenPower(true);
            }
        }
    }

    private HwMultiDisplayPowerManager() {
        if (HwFoldScreenManagerEx.isInwardFoldDevice() && this.mFoldDisplayListener == null) {
            this.mFoldDisplayListener = new FoldDisplayListener();
            HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayListener);
        }
    }

    public static HwMultiDisplayPowerManager getDefault() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwMultiDisplayPowerManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context, Looper pcLooper) {
        this.mContext = context;
        this.mPowerHandler = new PowerHandler(pcLooper);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null) {
            this.mTriggerWakeLock = powerManager.newWakeLock(1, "MultiDisplayPower-trigger");
        }
    }

    public boolean isScreenOnAccurately() {
        boolean isScreenOnAccurately;
        synchronized (LOCK) {
            isScreenOnAccurately = isScreenOnAccurately(this.mScreenState);
        }
        return isScreenOnAccurately;
    }

    public boolean isScreenOnFully() {
        boolean z;
        synchronized (LOCK) {
            z = true;
            if (this.mScreenState != 1) {
                z = false;
            }
        }
        return z;
    }

    public void setScreenPower(boolean isTurnScreenOn) {
        synchronized (LOCK) {
            setScreenStateLocked(isTurnScreenOn);
        }
    }

    public void lockScreenWhenDisconnected() {
        if (!isScreenOnFully()) {
            HwPCUtils.log(TAG, "Lock phone screen when PC displayer is disconnected.");
            long eventTime = SystemClock.uptimeMillis();
            PowerManager powerManager = this.mPowerManager;
            if (powerManager != null) {
                PowerManagerEx.goToSleep(powerManager, eventTime, 5, 1);
            }
        }
    }

    private void setScreenStateLocked(boolean isTurnScreenOn) {
        if (this.mIsStateChangePending) {
            HwPCUtils.log(TAG, "Suppressed redundant change screen state while state is changing");
            return;
        }
        int lastState = this.mScreenState;
        this.mScreenState = transitionToNextState(isTurnScreenOn, lastState);
        HwPCUtils.log(TAG, "setScreenState turnOn:" + isTurnScreenOn + ", new:" + this.mScreenState + ", old:" + lastState);
        int i = this.mScreenState;
        if (i != lastState) {
            this.mIsStateChangePending = true;
            sendScreenStateBroadcastIfNeed(i, lastState);
            scheduleUpdateScreenState(this.mScreenState);
        }
    }

    private void sendScreenStateBroadcastIfNeed(int newState, int oldState) {
        boolean isNewStateScreenOn = isScreenOnAccurately(newState);
        if (isNewStateScreenOn != isScreenOnAccurately(oldState)) {
            this.mMainHandler.post(new Runnable(newState, oldState, isNewStateScreenOn) {
                /* class com.huawei.server.hwmultidisplay.power.$$Lambda$HwMultiDisplayPowerManager$lB1FN5CkvM0KdyiXNxyu2ip5Og */
                private final /* synthetic */ int f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwMultiDisplayPowerManager.this.lambda$sendScreenStateBroadcastIfNeed$0$HwMultiDisplayPowerManager(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    public /* synthetic */ void lambda$sendScreenStateBroadcastIfNeed$0$HwMultiDisplayPowerManager(int newState, int oldState, boolean isNewStateScreenOn) {
        Intent intent;
        HwPCUtils.log(TAG, "send multi display screen broadcast, new:" + newState + ", old:" + oldState);
        if (isNewStateScreenOn) {
            intent = new Intent(ACTION_HWMULTIDISPLAY_SCREEN_ON);
        } else {
            intent = new Intent(ACTION_HWMULTIDISPLAY_SCREEN_OFF);
        }
        intent.addFlags(1344274432);
        this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL);
    }

    private boolean setInternalDisplayPowerMode(int mode) {
        IBinder displayToken = SurfaceControlEx.getInternalDisplayToken();
        if (displayToken == null) {
            HwPCUtils.log(TAG, "Internal display may be disconnected when set mode:" + mode);
            return false;
        } else if (HwPCUtils.isInWindowsCastMode() && HwPCUtils.isShopDemo()) {
            return true;
        } else {
            SurfaceControlEx.setDisplayPowerMode(displayToken, mode);
            return true;
        }
    }

    private int transitionToNextState(boolean isTurnScreenOn, int lastState) {
        if (isTurnScreenOn) {
            return 1;
        }
        if (!HwPCUtils.isInWindowsCastMode()) {
            return 3;
        }
        if (lastState == 1) {
            return 2;
        }
        if (lastState != 2 || !this.mIsDiming) {
            return 3;
        }
        return 2;
    }

    private boolean isScreenOnAccurately(int state) {
        return state == 1 || state == 2;
    }

    private void scheduleUpdateScreenState(int state) {
        this.mPowerHandler.sendMessage(this.mPowerHandler.obtainMessage(2, state, 0));
    }

    private void stateChangeDoneLocked() {
        this.mIsStateChangePending = false;
    }

    private boolean startDimLocked() {
        if (this.mIsDiming) {
            return false;
        }
        this.mIsDiming = true;
        this.mPowerHandler.sendEmptyMessageDelayed(1, DIM_TIMEOUT);
        return true;
    }

    private void stopDimLocked() {
        this.mIsDiming = false;
        this.mPowerHandler.removeMessages(1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dimTimeout() {
        synchronized (LOCK) {
            this.mIsDiming = false;
            setScreenStateLocked(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScreenState(int state) {
        boolean isSuccess = false;
        try {
            synchronized (LOCK) {
                if (state == 1) {
                    stopDimLocked();
                    isSuccess = setInternalDisplayPowerMode(2);
                } else if (state == 2) {
                    isSuccess = startDimLocked();
                } else if (state == 3) {
                    stopDimLocked();
                    notifyThpBeforeOffIfNeed();
                    isSuccess = setInternalDisplayPowerMode(0);
                }
            }
            HwPCUtils.log(TAG, "updateScreenState state:" + state + ", success:" + isSuccess);
            if (isSuccess) {
                ensureScreenState(state);
            }
            synchronized (LOCK) {
                stateChangeDoneLocked();
            }
        } catch (Throwable th) {
            synchronized (LOCK) {
                stateChangeDoneLocked();
                throw th;
            }
        }
    }

    private void ensureScreenState(int state) {
        if (state == 1) {
            ensureScreenOn();
        } else if (state == 2) {
            triggerPowerUpdate();
        }
    }

    private void ensureScreenOn() {
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null) {
            PowerManagerEx.wakeUp(powerManager, SystemClock.uptimeMillis(), 0, "ensureMultiDisplayOn");
            PowerManagerEx.userActivity(this.mPowerManager, SystemClock.uptimeMillis(), PowerManagerEx.getUserActivityEventOther(), 0);
        }
    }

    private void triggerPowerUpdate() {
        PowerManager.WakeLock wakeLock = this.mTriggerWakeLock;
        if (wakeLock != null) {
            if (!wakeLock.isHeld()) {
                this.mTriggerWakeLock.acquire();
            }
            if (this.mTriggerWakeLock.isHeld()) {
                this.mTriggerWakeLock.release();
            }
        }
    }

    private void notifyThpBeforeOffIfNeed() {
        if (IS_SUPPORT_AP_AOD) {
            HwInputManager.setTouchscreenFeatureConfig((int) AP_AOD_FEATURE, "0");
        }
    }
}
