package com.android.server.power.batterysaver;

import android.app.ActivityManagerInternal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;
import com.android.server.EventLogTags;
import com.android.server.LocalServices;
import com.android.server.power.BatterySaverPolicy;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class BatterySaverController implements BatterySaverPolicy.BatterySaverPolicyListener {
    static final boolean DEBUG = false;
    public static final int REASON_AUTOMATIC_OFF = 1;
    public static final int REASON_AUTOMATIC_ON = 0;
    public static final int REASON_INTERACTIVE_CHANGED = 5;
    public static final int REASON_MANUAL_OFF = 3;
    public static final int REASON_MANUAL_ON = 2;
    public static final int REASON_PLUGGED_IN = 7;
    public static final int REASON_POLICY_CHANGED = 6;
    public static final int REASON_SETTING_CHANGED = 8;
    public static final int REASON_STICKY_RESTORE = 4;
    static final String TAG = "BatterySaverController";
    private final BatterySaverPolicy mBatterySaverPolicy;
    private final BatterySavingStats mBatterySavingStats;
    private final Context mContext;
    private HwCustBatterySaverController mCustBatterySaverController;
    @GuardedBy("mLock")
    private boolean mEnabled;
    private final FileUpdater mFileUpdater;
    /* access modifiers changed from: private */
    public final MyHandler mHandler;
    @GuardedBy("mLock")
    private boolean mIsInteractive;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public boolean mIsPluggedIn;
    @GuardedBy("mLock")
    private final ArrayList<PowerManagerInternal.LowPowerModeListener> mListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Object mLock;
    /* access modifiers changed from: private */
    public final Plugin[] mPlugins;
    private PowerManager mPowerManager;
    private boolean mPreviouslyEnabled;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Can't fix incorrect switch cases order */
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
                case -1538406691:
                    if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 2;
                        break;
                    }
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 0;
                        break;
                    }
                case 498807504:
                    if (action.equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                        c = 4;
                        break;
                    }
                case 870701415:
                    if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                        c = 3;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                    if (BatterySaverController.this.isEnabled()) {
                        BatterySaverController.this.mHandler.postStateChanged(false, 5);
                        break;
                    } else {
                        BatterySaverController.this.updateBatterySavingStats();
                        return;
                    }
                case 2:
                    synchronized (BatterySaverController.this.mLock) {
                        BatterySaverController batterySaverController = BatterySaverController.this;
                        if (intent.getIntExtra("plugged", 0) == 0) {
                            z = false;
                        }
                        boolean unused = batterySaverController.mIsPluggedIn = z;
                        break;
                    }
                case 3:
                case 4:
                    break;
            }
            BatterySaverController.this.updateBatterySavingStats();
        }
    };

    private class MyHandler extends Handler {
        private static final int ARG_DONT_SEND_BROADCAST = 0;
        private static final int ARG_SEND_BROADCAST = 1;
        private static final int MSG_STATE_CHANGED = 1;
        private static final int MSG_SYSTEM_READY = 2;

        public MyHandler(Looper looper) {
            super(looper);
        }

        public void postStateChanged(boolean sendBroadcast, int reason) {
            int i;
            if (sendBroadcast) {
                i = 1;
            } else {
                i = 0;
            }
            obtainMessage(1, i, reason).sendToTarget();
        }

        public void postSystemReady() {
            obtainMessage(2, 0, 0).sendToTarget();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: boolean} */
        /* JADX WARNING: type inference failed for: r1v0 */
        /* JADX WARNING: type inference failed for: r1v2 */
        /* JADX WARNING: type inference failed for: r1v3, types: [int] */
        /* JADX WARNING: type inference failed for: r1v5 */
        /* JADX WARNING: Multi-variable type inference failed */
        public void dispatchMessage(Message msg) {
            ? r1 = 0;
            switch (msg.what) {
                case 1:
                    BatterySaverController batterySaverController = BatterySaverController.this;
                    if (msg.arg1 == 1) {
                        r1 = 1;
                    }
                    batterySaverController.handleBatterySaverStateChanged(r1, msg.arg2);
                    return;
                case 2:
                    Plugin[] access$400 = BatterySaverController.this.mPlugins;
                    int length = access$400.length;
                    while (r1 < length) {
                        access$400[r1].onSystemReady(BatterySaverController.this);
                        r1++;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public interface Plugin {
        void onBatterySaverChanged(BatterySaverController batterySaverController);

        void onSystemReady(BatterySaverController batterySaverController);
    }

    public BatterySaverController(Object lock, Context context, Looper looper, BatterySaverPolicy policy, BatterySavingStats batterySavingStats) {
        this.mLock = lock;
        this.mContext = context;
        this.mHandler = new MyHandler(looper);
        this.mBatterySaverPolicy = policy;
        this.mBatterySaverPolicy.addListener(this);
        this.mFileUpdater = new FileUpdater(context);
        this.mBatterySavingStats = batterySavingStats;
        this.mCustBatterySaverController = (HwCustBatterySaverController) HwCustUtils.createObj(HwCustBatterySaverController.class, new Object[]{context});
        ArrayList<Plugin> plugins = new ArrayList<>();
        plugins.add(new BatterySaverLocationPlugin(this.mContext));
        this.mPlugins = (Plugin[]) plugins.toArray(new Plugin[plugins.size()]);
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

    public void onBatterySaverPolicyChanged(BatterySaverPolicy policy) {
        if (isEnabled()) {
            this.mHandler.postStateChanged(true, 6);
        }
    }

    public void enableBatterySaver(boolean enable, int reason) {
        synchronized (this.mLock) {
            if (this.mEnabled != enable) {
                this.mEnabled = enable;
                this.mHandler.postStateChanged(true, reason);
            }
        }
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
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
        return isEnabled() && this.mBatterySaverPolicy.isLaunchBoostDisabled();
    }

    /* access modifiers changed from: package-private */
    public void handleBatterySaverStateChanged(boolean sendBroadcast, int reason) {
        PowerManagerInternal.LowPowerModeListener[] listeners;
        boolean enabled;
        ArrayMap<String, String> fileValues;
        boolean isInteractive = getPowerManager().isInteractive();
        synchronized (this.mLock) {
            EventLogTags.writeBatterySaverMode(this.mPreviouslyEnabled ? 1 : 0, this.mEnabled ? 1 : 0, isInteractive, this.mEnabled ? this.mBatterySaverPolicy.toEventLogString() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, reason);
            this.mPreviouslyEnabled = this.mEnabled;
            listeners = (PowerManagerInternal.LowPowerModeListener[]) this.mListeners.toArray(new PowerManagerInternal.LowPowerModeListener[this.mListeners.size()]);
            enabled = this.mEnabled;
            this.mIsInteractive = isInteractive;
            if (enabled) {
                fileValues = this.mBatterySaverPolicy.getFileValues(isInteractive);
            } else {
                fileValues = null;
            }
        }
        PowerManagerInternal pmi = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        if (pmi != null) {
            pmi.powerHint(5, enabled ? 1 : 0);
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
            this.mContext.sendBroadcastAsUser(new Intent("android.os.action.POWER_SAVE_MODE_CHANGING").putExtra("mode", enabled).addFlags(1073741824), UserHandle.ALL);
            Intent intent = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED");
            intent.addFlags(1073741824);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            Intent intent2 = new Intent("android.os.action.POWER_SAVE_MODE_CHANGED_INTERNAL");
            intent2.addFlags(1073741824);
            this.mContext.sendBroadcastAsUser(intent2, UserHandle.ALL, "android.permission.DEVICE_POWER");
            if (this.mCustBatterySaverController != null) {
                this.mCustBatterySaverController.setPowerSaverMode(enabled);
            }
            for (PowerManagerInternal.LowPowerModeListener listener : listeners) {
                listener.onLowPowerModeChanged(this.mBatterySaverPolicy.getBatterySaverPolicy(listener.getServiceType(), enabled));
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateBatterySavingStats() {
        int dozeMode;
        PowerManager pm = getPowerManager();
        if (pm == null) {
            Slog.wtf(TAG, "PowerManager not initialized");
            return;
        }
        boolean isInteractive = pm.isInteractive();
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
            boolean z = this.mEnabled;
            batterySavingStats.transitionState(z ? 1 : 0, isInteractive, dozeMode);
        }
    }
}
