package com.android.server.power.batterysaver;

import android.app.ActivityManagerInternal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatterySaverPolicyConfig;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.power.batterysaver.BatterySaverPolicy;
import java.util.ArrayList;

public class BatterySaverController implements BatterySaverPolicy.BatterySaverPolicyListener {
    static final boolean DEBUG = false;
    public static final int REASON_ADAPTIVE_DYNAMIC_POWER_SAVINGS_CHANGED = 11;
    public static final int REASON_DYNAMIC_POWER_SAVINGS_AUTOMATIC_OFF = 10;
    public static final int REASON_DYNAMIC_POWER_SAVINGS_AUTOMATIC_ON = 9;
    public static final int REASON_INTERACTIVE_CHANGED = 5;
    public static final int REASON_MANUAL_OFF = 3;
    public static final int REASON_MANUAL_ON = 2;
    public static final int REASON_PERCENTAGE_AUTOMATIC_OFF = 1;
    public static final int REASON_PERCENTAGE_AUTOMATIC_ON = 0;
    public static final int REASON_PLUGGED_IN = 7;
    public static final int REASON_POLICY_CHANGED = 6;
    public static final int REASON_SETTING_CHANGED = 8;
    public static final int REASON_STICKY_RESTORE = 4;
    public static final int REASON_TIMEOUT = 12;
    static final String TAG = "BatterySaverController";
    @GuardedBy({"mLock"})
    private boolean mAdaptiveEnabled;
    private boolean mAdaptivePreviouslyEnabled;
    private final BatterySaverPolicy mBatterySaverPolicy;
    private final BatterySavingStats mBatterySavingStats;
    private final Context mContext;
    private final FileUpdater mFileUpdater;
    @GuardedBy({"mLock"})
    private boolean mFullEnabled;
    private boolean mFullPreviouslyEnabled;
    private final MyHandler mHandler;
    @GuardedBy({"mLock"})
    private boolean mIsInteractive;
    @GuardedBy({"mLock"})
    private boolean mIsPluggedIn;
    @GuardedBy({"mLock"})
    private final ArrayList<PowerManagerInternal.LowPowerModeListener> mListeners = new ArrayList<>();
    private final Object mLock;
    private final Plugin[] mPlugins;
    private PowerManager mPowerManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.power.batterysaver.BatterySaverController.AnonymousClass1 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            boolean z = true;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1538406691:
                    if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case 498807504:
                    if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 870701415:
                    if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c != 0 && c != 1) {
                if (c == 2) {
                    synchronized (BatterySaverController.this.mLock) {
                        BatterySaverController batterySaverController = BatterySaverController.this;
                        if (intent.getIntExtra("plugged", 0) == 0) {
                            z = false;
                        }
                        batterySaverController.mIsPluggedIn = z;
                    }
                } else if (!(c == 3 || c == 4)) {
                    return;
                }
                BatterySaverController.this.updateBatterySavingStats();
            } else if (!BatterySaverController.this.isPolicyEnabled()) {
                BatterySaverController.this.updateBatterySavingStats();
            } else {
                BatterySaverController.this.mHandler.postStateChanged(false, 5);
            }
        }
    };

    public interface Plugin {
        void onBatterySaverChanged(BatterySaverController batterySaverController);

        void onSystemReady(BatterySaverController batterySaverController);
    }

    static String reasonToString(int reason) {
        switch (reason) {
            case 0:
                return "Percentage Auto ON";
            case 1:
                return "Percentage Auto OFF";
            case 2:
                return "Manual ON";
            case 3:
                return "Manual OFF";
            case 4:
                return "Sticky restore";
            case 5:
                return "Interactivity changed";
            case 6:
                return "Policy changed";
            case 7:
                return "Plugged in";
            case 8:
                return "Setting changed";
            case 9:
                return "Dynamic Warning Auto ON";
            case 10:
                return "Dynamic Warning Auto OFF";
            case 11:
                return "Adaptive Power Savings changed";
            case 12:
                return "timeout";
            default:
                return "Unknown reason: " + reason;
        }
    }

    public BatterySaverController(Object lock, Context context, Looper looper, BatterySaverPolicy policy, BatterySavingStats batterySavingStats) {
        this.mLock = lock;
        this.mContext = context;
        this.mHandler = new MyHandler(looper);
        this.mBatterySaverPolicy = policy;
        this.mBatterySaverPolicy.addListener(this);
        this.mFileUpdater = new FileUpdater(context);
        this.mBatterySavingStats = batterySavingStats;
        this.mPlugins = new Plugin[]{new BatterySaverLocationPlugin(this.mContext)};
    }

    public void addListener(PowerManagerInternal.LowPowerModeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.add(listener);
        }
    }

    public void systemReady() {
        IntentFilter filter = new IntentFilter("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        this.mContext.registerReceiver(this.mReceiver, filter);
        this.mFileUpdater.systemReady(((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).isRuntimeRestarted());
        this.mHandler.postSystemReady();
    }

    private PowerManager getPowerManager() {
        if (this.mPowerManager == null) {
            this.mPowerManager = (PowerManager) Preconditions.checkNotNull((PowerManager) this.mContext.getSystemService(PowerManager.class));
        }
        return this.mPowerManager;
    }

    @Override // com.android.server.power.batterysaver.BatterySaverPolicy.BatterySaverPolicyListener
    public void onBatterySaverPolicyChanged(BatterySaverPolicy policy) {
        if (isPolicyEnabled()) {
            this.mHandler.postStateChanged(true, 6);
        }
    }

    /* access modifiers changed from: private */
    public class MyHandler extends Handler {
        private static final int ARG_DONT_SEND_BROADCAST = 0;
        private static final int ARG_SEND_BROADCAST = 1;
        private static final int MSG_STATE_CHANGED = 1;
        private static final int MSG_SYSTEM_READY = 2;

        public MyHandler(Looper looper) {
            super(looper);
        }

        /* access modifiers changed from: package-private */
        public void postStateChanged(boolean sendBroadcast, int reason) {
            obtainMessage(1, sendBroadcast ? 1 : 0, reason).sendToTarget();
        }

        public void postSystemReady() {
            obtainMessage(2, 0, 0).sendToTarget();
        }

        @Override // android.os.Handler
        public void dispatchMessage(Message msg) {
            int i = msg.what;
            boolean z = false;
            if (i == 1) {
                BatterySaverController batterySaverController = BatterySaverController.this;
                if (msg.arg1 == 1) {
                    z = true;
                }
                batterySaverController.handleBatterySaverStateChanged(z, msg.arg2);
            } else if (i == 2) {
                for (Plugin p : BatterySaverController.this.mPlugins) {
                    p.onSystemReady(BatterySaverController.this);
                }
            }
        }
    }

    @VisibleForTesting
    public void enableBatterySaver(boolean enable, int reason) {
        synchronized (this.mLock) {
            if (this.mFullEnabled != enable) {
                this.mFullEnabled = enable;
                if (updatePolicyLevelLocked()) {
                    this.mHandler.postStateChanged(true, reason);
                }
            }
        }
    }

    private boolean updatePolicyLevelLocked() {
        if (this.mFullEnabled) {
            return this.mBatterySaverPolicy.setPolicyLevel(2);
        }
        if (this.mAdaptiveEnabled) {
            return this.mBatterySaverPolicy.setPolicyLevel(1);
        }
        return this.mBatterySaverPolicy.setPolicyLevel(0);
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            if (!this.mFullEnabled) {
                if (!this.mAdaptiveEnabled || !this.mBatterySaverPolicy.shouldAdvertiseIsEnabled()) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPolicyEnabled() {
        boolean z;
        synchronized (this.mLock) {
            if (!this.mFullEnabled) {
                if (!this.mAdaptiveEnabled) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isFullEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mFullEnabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isAdaptiveEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mAdaptiveEnabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean setAdaptivePolicyLocked(String settings, String deviceSpecificSettings, int reason) {
        return setAdaptivePolicyLocked(BatterySaverPolicy.Policy.fromSettings(settings, deviceSpecificSettings), reason);
    }

    /* access modifiers changed from: package-private */
    public boolean setAdaptivePolicyLocked(BatterySaverPolicyConfig config, int reason) {
        return setAdaptivePolicyLocked(BatterySaverPolicy.Policy.fromConfig(config), reason);
    }

    /* access modifiers changed from: package-private */
    public boolean setAdaptivePolicyLocked(BatterySaverPolicy.Policy policy, int reason) {
        if (!this.mBatterySaverPolicy.setAdaptivePolicyLocked(policy)) {
            return false;
        }
        this.mHandler.postStateChanged(true, reason);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean resetAdaptivePolicyLocked(int reason) {
        if (!this.mBatterySaverPolicy.resetAdaptivePolicyLocked()) {
            return false;
        }
        this.mHandler.postStateChanged(true, reason);
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setAdaptivePolicyEnabledLocked(boolean enabled, int reason) {
        if (this.mAdaptiveEnabled == enabled) {
            return false;
        }
        this.mAdaptiveEnabled = enabled;
        if (!updatePolicyLevelLocked()) {
            return false;
        }
        this.mHandler.postStateChanged(true, reason);
        return true;
    }

    public boolean isInteractive() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIsInteractive;
        }
        return z;
    }

    public BatterySaverPolicy getBatterySaverPolicy() {
        return this.mBatterySaverPolicy;
    }

    public boolean isLaunchBoostDisabled() {
        return isPolicyEnabled() && this.mBatterySaverPolicy.isLaunchBoostDisabled();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x001e  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0020  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0025  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0027  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x002c  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x002e  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0034  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0063  */
    public void handleBatterySaverStateChanged(boolean sendBroadcast, int reason) {
        int i;
        boolean enabled;
        PowerManagerInternal.LowPowerModeListener[] listeners;
        ArrayMap<String, String> fileValues;
        boolean isInteractive = getPowerManager().isInteractive();
        synchronized (this.mLock) {
            if (!this.mFullEnabled) {
                if (!this.mAdaptiveEnabled) {
                    enabled = false;
                    EventLogTags.writeBatterySaverMode(!this.mFullPreviouslyEnabled ? 1 : 0, !this.mAdaptivePreviouslyEnabled ? 1 : 0, !this.mFullEnabled ? 1 : 0, this.mAdaptiveEnabled ? 1 : 0, isInteractive ? 1 : 0, !enabled ? this.mBatterySaverPolicy.toEventLogString() : "", reason);
                    this.mFullPreviouslyEnabled = this.mFullEnabled;
                    this.mAdaptivePreviouslyEnabled = this.mAdaptiveEnabled;
                    listeners = (PowerManagerInternal.LowPowerModeListener[]) this.mListeners.toArray(new PowerManagerInternal.LowPowerModeListener[0]);
                    this.mIsInteractive = isInteractive;
                    if (!enabled) {
                        fileValues = this.mBatterySaverPolicy.getFileValues(isInteractive);
                    } else {
                        fileValues = null;
                    }
                }
            }
            enabled = true;
            if (!this.mFullPreviouslyEnabled) {
            }
            if (!this.mAdaptivePreviouslyEnabled) {
            }
            if (!this.mFullEnabled) {
            }
            EventLogTags.writeBatterySaverMode(!this.mFullPreviouslyEnabled ? 1 : 0, !this.mAdaptivePreviouslyEnabled ? 1 : 0, !this.mFullEnabled ? 1 : 0, this.mAdaptiveEnabled ? 1 : 0, isInteractive ? 1 : 0, !enabled ? this.mBatterySaverPolicy.toEventLogString() : "", reason);
            this.mFullPreviouslyEnabled = this.mFullEnabled;
            this.mAdaptivePreviouslyEnabled = this.mAdaptiveEnabled;
            listeners = (PowerManagerInternal.LowPowerModeListener[]) this.mListeners.toArray(new PowerManagerInternal.LowPowerModeListener[0]);
            this.mIsInteractive = isInteractive;
            if (!enabled) {
            }
        }
        PowerManagerInternal pmi = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        if (pmi != null) {
            pmi.powerHint(5, isEnabled() ? 1 : 0);
        }
        updateBatterySavingStats();
        if (ArrayUtils.isEmpty(fileValues)) {
            this.mFileUpdater.restoreDefault();
        } else {
            this.mFileUpdater.writeFiles(fileValues);
        }
        for (Plugin p : this.mPlugins) {
            p.onBatterySaverChanged(this);
        }
        if (sendBroadcast) {
            this.mContext.sendBroadcastAsUser(new Intent("android.os.action.POWER_SAVE_MODE_CHANGING").putExtra("mode", isEnabled()).addFlags(1073741824), UserHandle.ALL);
            Intent intent = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED");
            intent.addFlags(1073741824);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Intent intent2 = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL");
            intent2.addFlags(1073741824);
            this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL, "android.permission.DEVICE_POWER");
            for (PowerManagerInternal.LowPowerModeListener listener : listeners) {
                listener.onLowPowerModeChanged(this.mBatterySaverPolicy.getBatterySaverPolicy(listener.getServiceType()));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBatterySavingStats() {
        int dozeMode;
        PowerManager pm = getPowerManager();
        if (pm == null) {
            Slog.wtf(TAG, "PowerManager not initialized");
            return;
        }
        boolean isInteractive = pm.isInteractive();
        int i = 2;
        int i2 = 1;
        if (pm.isDeviceIdleMode()) {
            dozeMode = 2;
        } else if (pm.isLightDeviceIdleMode()) {
            dozeMode = 1;
        } else {
            dozeMode = 0;
        }
        synchronized (this.mLock) {
            if (this.mIsPluggedIn) {
                this.mBatterySavingStats.startCharging();
                return;
            }
            BatterySavingStats batterySavingStats = this.mBatterySavingStats;
            if (this.mFullEnabled) {
                i = 1;
            } else if (!this.mAdaptiveEnabled) {
                i = 0;
            }
            if (!isInteractive) {
                i2 = 0;
            }
            batterySavingStats.transitionState(i, i2, dozeMode);
        }
    }
}
