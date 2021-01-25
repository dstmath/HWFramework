package com.android.server.rms.iaware.cpu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.feature.SceneRecogFeature;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.concurrent.atomic.AtomicBoolean;

/* access modifiers changed from: package-private */
public class CpuPowerMode {
    private static final String ACTION_ENTER_SUPER_SAVE_MODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_POWER_MODE_CHANGE = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    private static final String ACTION_QUIT_SUPER_SAVE_MODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final int EXTRA_NONSAVE_STATE = 2;
    private static final int EXTRA_PERFORMANCE_STATE = 3;
    private static final int EXTRA_SAVE_STATE = 1;
    private static final Object LOCK = new Object();
    private static final int NON_SAVE_POWER_MODE = 1;
    private static final int PERFORMANCE_POWER_MODE = 3;
    private static final int SAVE_POWER_MODE = 2;
    private static final int SETTING_NONSAVE_POWER_MODE = 1;
    private static final int SETTING_SAVE_POWER_MODE = 4;
    private static final int SUPER_SAVE_POWER_MODE = 4;
    private static final String SYSTEM_MANAGER_PERMISSION = "com.huawei.systemmanager.permission.ACCESS_INTERFACE";
    private static final String TAG = "CpuPowerMode";
    private static CpuPowerMode sInstance;
    private static AtomicBoolean sIsFeatureEnable = new AtomicBoolean(false);
    private static AtomicBoolean sIsSaveMode = new AtomicBoolean(false);
    private static AtomicBoolean sIsSuperSaveMode = new AtomicBoolean(false);
    private Context mContext;
    private int mPowerMode = 1;
    private PowerStateChangeReceiver mPowerStateChangeReceiver;
    private int mTempPowerMode = 1;

    public void enable(Context context) {
        if (sIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CpuPowerMode has already enabled!");
            return;
        }
        sIsFeatureEnable.set(true);
        this.mContext = context;
        registerPowerStateReceiver();
        int powermode = Settings.System.getInt(this.mContext.getContentResolver(), "SmartModeStatus", 1);
        if (powermode == 1) {
            this.mPowerMode = 1;
            sIsSaveMode.set(false);
        } else if (powermode == 4) {
            this.mPowerMode = 2;
            sIsSaveMode.set(true);
        } else {
            AwareLog.d(TAG, "enable powermode not need to process");
        }
        if (!isSuperPowerSave()) {
            sIsSuperSaveMode.set(false);
        } else {
            sIsSuperSaveMode.set(true);
        }
        SystemPropertiesEx.set("persist.sys.performance", isPerformanceMode() ? "true" : "false");
    }

    public void disable() {
        if (!sIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CpuPowerMode has already disabled!");
            return;
        }
        sIsFeatureEnable.set(false);
        unregisterPowerStateReceiver();
    }

    private CpuPowerMode() {
    }

    public static CpuPowerMode getInstance() {
        CpuPowerMode cpuPowerMode;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new CpuPowerMode();
            }
            cpuPowerMode = sInstance;
        }
        return cpuPowerMode;
    }

    public boolean isSuperPowerSave() {
        return "true".equals(SystemPropertiesEx.get("sys.super_power_save", "false"));
    }

    private void registerPowerStateReceiver() {
        if (this.mContext != null && this.mPowerStateChangeReceiver == null) {
            this.mPowerStateChangeReceiver = new PowerStateChangeReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_POWER_MODE_CHANGE);
            filter.addAction(ACTION_ENTER_SUPER_SAVE_MODE);
            this.mContext.registerReceiver(this.mPowerStateChangeReceiver, filter);
            IntentFilter filterSuper = new IntentFilter();
            filterSuper.addAction(ACTION_QUIT_SUPER_SAVE_MODE);
            this.mContext.registerReceiver(this.mPowerStateChangeReceiver, filterSuper, SYSTEM_MANAGER_PERMISSION, null);
        }
    }

    private void unregisterPowerStateReceiver() {
        PowerStateChangeReceiver powerStateChangeReceiver;
        Context context = this.mContext;
        if (context != null && (powerStateChangeReceiver = this.mPowerStateChangeReceiver) != null) {
            context.unregisterReceiver(powerStateChangeReceiver);
            this.mPowerStateChangeReceiver = null;
        }
    }

    /* access modifiers changed from: private */
    public class PowerStateChangeReceiver extends BroadcastReceiver {
        private PowerStateChangeReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                AwareLog.e(CpuPowerMode.TAG, "PowerStateChangeReceiver onReceive intent null!");
            } else if (!CpuPowerMode.sIsFeatureEnable.get()) {
                AwareLog.e(CpuPowerMode.TAG, "PowerStateChangeReceiver disable, return!");
            } else {
                String action = intent.getAction();
                if (CpuPowerMode.ACTION_POWER_MODE_CHANGE.equals(action)) {
                    int powerMode = intent.getIntExtra(SceneRecogFeature.DATA_STATE, 0);
                    if (powerMode == 1) {
                        CpuPowerMode.this.mPowerMode = 2;
                        CpuPowerMode.sIsSaveMode.set(true);
                    } else if (powerMode == 2) {
                        CpuPowerMode.this.mPowerMode = 1;
                        CpuPowerMode.sIsSaveMode.set(false);
                    } else if (powerMode == 3) {
                        CpuPowerMode.this.mPowerMode = 3;
                    } else {
                        AwareLog.d(CpuPowerMode.TAG, "unknow powerMode " + powerMode);
                    }
                } else if (CpuPowerMode.ACTION_ENTER_SUPER_SAVE_MODE.equals(action)) {
                    CpuPowerMode cpuPowerMode = CpuPowerMode.this;
                    cpuPowerMode.mTempPowerMode = cpuPowerMode.mPowerMode;
                    CpuPowerMode.this.mPowerMode = 4;
                    CpuPowerMode.sIsSuperSaveMode.set(true);
                } else if (CpuPowerMode.ACTION_QUIT_SUPER_SAVE_MODE.equals(action)) {
                    CpuPowerMode cpuPowerMode2 = CpuPowerMode.this;
                    cpuPowerMode2.mPowerMode = cpuPowerMode2.mTempPowerMode;
                    CpuPowerMode.sIsSuperSaveMode.set(false);
                } else {
                    AwareLog.d(CpuPowerMode.TAG, "onReceive invaild action");
                }
                SystemPropertiesEx.set("persist.sys.performance", CpuPowerMode.isPerformanceMode() ? "true" : "false");
                CpuHighLoadManager.getInstance().notifyPowerStateChanged(CpuPowerMode.isPowerModePerformance(CpuPowerMode.this.mPowerMode));
            }
        }
    }

    public static boolean isPerformanceMode() {
        return !sIsSaveMode.get() && !sIsSuperSaveMode.get();
    }

    public static boolean isPowerModePerformance(int mode) {
        return mode == 3;
    }
}
