package com.android.server.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hdm.HwDeviceManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.server.LocalServices;
import com.android.server.policy.WindowManagerPolicy;
import java.util.HashSet;
import java.util.Set;

public class HwTvPowerManager implements HwTvPowerManagerPolicy {
    private static final String ACTION_TV_POWER_STATE_CHANGED = "com.huawei.homevision.action.TV_POWER_STATE_CHANGED";
    private static final long CHECK_SETPROP_TIMEOUT = 10000;
    private static final long CHECK_USER_OPERATION_TIMEOUT = 300000;
    private static final boolean DEBUG = Log.HWINFO;
    private static final int MSG_CHECK_USER_OPERATION_AFTER_BOOT = 1;
    private static final int MSG_SETPROP_TIMEOUT = 2;
    private static final String PROP_HISUSPEND_MODE = "sys.hw_mc.tvpower.suspend_mode";
    private static final String PROP_INPUT_BLOCK = "sys.hw_mc.tvpower.input_block";
    private static final String PROP_REBOOT_SCENE = "persist.sys.reboot.scene";
    private static final String PROP_RUNMODE = "ro.runmode";
    private static final String REBOOT_SCENE_NORMAL = "normal";
    private static final String RUNMODE_FACTORY = "factory";
    private static final String RUNMODE_NORMAL = "normal";
    private static final String SUSPEND_MODE_LIGHTSLEEP = "lightsleep";
    private static final String SUSPEND_MODE_NONE = "";
    private static final String SUSPEND_MODE_SHUTDOWN = "str";
    private static final String SUSPEND_MODE_WAKINGUP = "wakingup";
    private static final String TAG = "HwTvPowerManager";
    private static final Set<String> WAKELOCK_WHITE_LIST = new HashSet<String>(4) {
        /* class com.android.server.tv.HwTvPowerManager.AnonymousClass1 */

        {
            add("com.huawei.homevision.poweronoffsvr1000");
            add("com.huawei.homevision.tvservice1000");
            add("com.hisilicon.android.hiRMService1000");
            add("com.android.bluetooth1002");
        }
    };
    private Context mContext;
    private Handler mHandler;
    private boolean mIsBootCompleted;
    private boolean mIsSetpropTimeout = true;
    private boolean mIsUserOperationChecking;
    private WindowManagerPolicy mPolicy;
    private PowerManager mPowerManager;
    private BroadcastReceiver mUserActivityReceiver = null;
    private long mUserOperationTimeOut = 300000;

    public HwTvPowerManager(Context context) {
        this.mContext = context;
        this.mHandler = new TvHandler(Looper.getMainLooper());
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void systemReady() {
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mPolicy = (WindowManagerPolicy) LocalServices.getService(WindowManagerPolicy.class);
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void bootCompleted() {
        Log.i(TAG, "bootCompleted");
        this.mIsBootCompleted = true;
        if (isRunningInTvMode() && isUserSetuped() && !isFactoryMode()) {
            String rebootScene = SystemProperties.get(PROP_REBOOT_SCENE);
            boolean isNormalReboot = "normal".equalsIgnoreCase(rebootScene);
            Log.i(TAG, "rebootScene:" + rebootScene);
            if (!isNormalReboot) {
                scheduleCheckUserOperation();
            }
            SystemProperties.set(PROP_REBOOT_SCENE, "");
        }
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void onEarlyShutdownBegin(boolean isReboot) {
        if (isReboot && isRunningInTvMode()) {
            Log.i(TAG, "normal reboot begin");
            SystemProperties.set(PROP_REBOOT_SCENE, "normal");
        }
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void onKeyOperation() {
        if (this.mIsUserOperationChecking) {
            if (DEBUG) {
                Log.i(TAG, "onKeyOperation, cancel user operation check");
            }
            cancelUserOperationCheck();
        }
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void onEnterGoToSleep(int flags, boolean isAsleep) {
        if (isAsleep && (65536 & flags) != 0 && SUSPEND_MODE_LIGHTSLEEP.equals(SystemProperties.get(PROP_HISUSPEND_MODE))) {
            Log.i(TAG, "transition lightsleep to shutdown");
            SystemProperties.set(PROP_HISUSPEND_MODE, SUSPEND_MODE_SHUTDOWN);
            setInputBlockProperty(SUSPEND_MODE_SHUTDOWN);
            this.mHandler.post(new Runnable() {
                /* class com.android.server.tv.$$Lambda$HwTvPowerManager$Pll_wm5b0aAaTrbbuxsEPCfR4A */

                @Override // java.lang.Runnable
                public final void run() {
                    HwTvPowerManager.this.lambda$onEnterGoToSleep$0$HwTvPowerManager();
                }
            });
        }
    }

    public /* synthetic */ void lambda$onEnterGoToSleep$0$HwTvPowerManager() {
        sendTvPowerStateChangedBroadcast(SUSPEND_MODE_LIGHTSLEEP, SUSPEND_MODE_SHUTDOWN);
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void onEarlyGoToSleep(int flags) {
        if (isRunningInTvMode()) {
            Log.i(TAG, "goToSleep flags:0x" + Integer.toHexString(flags));
            if ((65536 & flags) != 0) {
                SystemProperties.set(PROP_HISUSPEND_MODE, SUSPEND_MODE_SHUTDOWN);
                setInputBlockProperty(SUSPEND_MODE_SHUTDOWN);
                return;
            }
            SystemProperties.set(PROP_HISUSPEND_MODE, SUSPEND_MODE_LIGHTSLEEP);
            setInputBlockProperty(SUSPEND_MODE_LIGHTSLEEP);
        }
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public void onStartedWakingUp(int why) {
        if (isRunningInTvMode() && this.mIsBootCompleted) {
            Log.i(TAG, "onStartedWakingUp why:" + why);
            if (why != 3) {
                setInputBlockProperty(SUSPEND_MODE_WAKINGUP);
            }
        }
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public boolean isHiRmsProcessing() {
        if (this.mIsSetpropTimeout) {
            return false;
        }
        String suspendMode = SystemProperties.get(PROP_INPUT_BLOCK, "");
        if (SUSPEND_MODE_WAKINGUP.equals(suspendMode) || SUSPEND_MODE_SHUTDOWN.equals(suspendMode) || SUSPEND_MODE_LIGHTSLEEP.equals(suspendMode)) {
            return true;
        }
        return false;
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public boolean isWakeLockDisabled(String packageName, int pid, int uid) {
        Set<String> set = WAKELOCK_WHITE_LIST;
        if (set.contains(packageName + uid)) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.tv.HwTvPowerManagerPolicy
    public boolean isWakelockCauseWakeUpDisabled() {
        return SUSPEND_MODE_SHUTDOWN.equals(SystemProperties.get(PROP_HISUSPEND_MODE));
    }

    final class TvHandler extends Handler {
        TvHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwTvPowerManager.this.handleCheckUserOperationTimeout();
            } else if (i == 2) {
                HwTvPowerManager.this.handleSetpropTimeout();
            }
        }
    }

    private void setInputBlockProperty(String value) {
        this.mHandler.removeMessages(2);
        SystemProperties.set(PROP_INPUT_BLOCK, value);
        this.mHandler.sendEmptyMessageDelayed(2, 10000);
        this.mIsSetpropTimeout = false;
    }

    private void scheduleCheckUserOperation() {
        this.mIsUserOperationChecking = true;
        if (!this.mHandler.hasMessages(1)) {
            try {
                this.mUserOperationTimeOut = Long.parseLong(HwDeviceManager.getString(73));
            } catch (NumberFormatException e) {
                Log.e(TAG, "get sleep time interval error");
            }
            long j = this.mUserOperationTimeOut;
            if (j != 0) {
                this.mHandler.sendEmptyMessageDelayed(1, j);
            }
        }
        registerUserActivityReceiver();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCheckUserOperationTimeout() {
        if (this.mIsUserOperationChecking) {
            Log.i(TAG, "No operation after boot in " + this.mUserOperationTimeOut + "ms, then goToSleep");
            cancelUserOperationCheck();
            PowerManager powerManager = this.mPowerManager;
            if (powerManager != null) {
                powerManager.goToSleep(SystemClock.uptimeMillis());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetpropTimeout() {
        Log.i(TAG, "Setprop timeout, treat as HiRms finished and stop intputBlock");
        this.mIsSetpropTimeout = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelUserOperationCheck() {
        this.mIsUserOperationChecking = false;
        this.mHandler.removeMessages(1);
        unregisterUserActivityReceiver();
    }

    private boolean isRunningInTvMode() {
        WindowManagerPolicy windowManagerPolicy = this.mPolicy;
        return windowManagerPolicy != null && HwTvPowerManagerPolicy.isTvMode(windowManagerPolicy.getUiMode());
    }

    private boolean isFactoryMode() {
        return RUNMODE_FACTORY.equals(SystemProperties.get(PROP_RUNMODE, "normal"));
    }

    private boolean isUserSetuped() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0;
    }

    private synchronized void registerUserActivityReceiver() {
        if (this.mUserActivityReceiver == null) {
            this.mUserActivityReceiver = new BroadcastReceiver() {
                /* class com.android.server.tv.HwTvPowerManager.AnonymousClass2 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (intent == null) {
                        Log.i(HwTvPowerManager.TAG, "intent error");
                    } else if ("android.intent.action.USER_ACTIVITY_NOTIFICATION".equals(intent.getAction())) {
                        Log.i(HwTvPowerManager.TAG, "receive OP_USER_ACTIVITY");
                        HwTvPowerManager.this.cancelUserOperationCheck();
                    }
                }
            };
            this.mContext.registerReceiver(this.mUserActivityReceiver, new IntentFilter("android.intent.action.USER_ACTIVITY_NOTIFICATION"));
            try {
                IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
                if (wm != null) {
                    wm.requestUserActivityNotification();
                    Log.i(TAG, "registerUserActivityReceiver and requested");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failed to requestUserActivityNotification:" + e.getMessage());
            }
        }
    }

    private synchronized void unregisterUserActivityReceiver() {
        if (this.mUserActivityReceiver != null) {
            try {
                this.mContext.unregisterReceiver(this.mUserActivityReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Fail to unregisterReceiver:" + e.getMessage());
            }
            this.mUserActivityReceiver = null;
        }
    }

    private void sendTvPowerStateChangedBroadcast(String fromState, String toState) {
        Log.i(TAG, "send power state changed broadcast, from:" + fromState + ", to:" + toState);
        Intent intent = new Intent(ACTION_TV_POWER_STATE_CHANGED);
        intent.addFlags(1073741824);
        intent.putExtra("fromState", fromState);
        intent.putExtra("toState", toState);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }
}
