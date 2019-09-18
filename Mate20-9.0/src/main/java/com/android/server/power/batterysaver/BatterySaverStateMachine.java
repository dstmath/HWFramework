package com.android.server.power.batterysaver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BackgroundThread;
import com.android.server.EventLogTags;
import com.android.server.power.IHwShutdownThread;
import java.io.PrintWriter;

public class BatterySaverStateMachine {
    private static final boolean DEBUG = false;
    private static final String TAG = "BatterySaverStateMachine";
    @GuardedBy("mLock")
    private int mBatteryLevel;
    private final BatterySaverController mBatterySaverController;
    @GuardedBy("mLock")
    private boolean mBatterySaverSnoozing;
    @GuardedBy("mLock")
    private boolean mBatteryStatusSet;
    @GuardedBy("mLock")
    private boolean mBootCompleted;
    private final Context mContext;
    @GuardedBy("mLock")
    private boolean mIsBatteryLevelLow;
    @GuardedBy("mLock")
    private boolean mIsPowered;
    @GuardedBy("mLock")
    private int mLastChangedIntReason;
    @GuardedBy("mLock")
    private String mLastChangedStrReason;
    /* access modifiers changed from: private */
    public final Object mLock;
    @GuardedBy("mLock")
    private boolean mSettingBatterySaverEnabled;
    @GuardedBy("mLock")
    private boolean mSettingBatterySaverEnabledSticky;
    @GuardedBy("mLock")
    private int mSettingBatterySaverTriggerThreshold;
    @GuardedBy("mLock")
    private boolean mSettingsLoaded;
    private final ContentObserver mSettingsObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange) {
            synchronized (BatterySaverStateMachine.this.mLock) {
                BatterySaverStateMachine.this.refreshSettingsLocked();
            }
        }
    };
    private final Runnable mThresholdChangeLogger = new Runnable() {
        public final void run() {
            EventLogTags.writeBatterySaverSetting(BatterySaverStateMachine.this.mSettingBatterySaverTriggerThreshold);
        }
    };

    public BatterySaverStateMachine(Object lock, Context context, BatterySaverController batterySaverController) {
        this.mLock = lock;
        this.mContext = context;
        this.mBatterySaverController = batterySaverController;
    }

    private boolean isBatterySaverEnabled() {
        return this.mBatterySaverController.isEnabled();
    }

    private boolean isAutoBatterySaverConfigured() {
        return this.mSettingBatterySaverTriggerThreshold > 0;
    }

    public void onBootCompleted() {
        putGlobalSetting("low_power", 0);
        runOnBgThread(new Runnable() {
            public final void run() {
                BatterySaverStateMachine.lambda$onBootCompleted$0(BatterySaverStateMachine.this);
            }
        });
    }

    public static /* synthetic */ void lambda$onBootCompleted$0(BatterySaverStateMachine batterySaverStateMachine) {
        ContentResolver cr = batterySaverStateMachine.mContext.getContentResolver();
        cr.registerContentObserver(Settings.Global.getUriFor("low_power"), false, batterySaverStateMachine.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("low_power_sticky"), false, batterySaverStateMachine.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, batterySaverStateMachine.mSettingsObserver, 0);
        synchronized (batterySaverStateMachine.mLock) {
            batterySaverStateMachine.mBootCompleted = true;
            batterySaverStateMachine.refreshSettingsLocked();
            batterySaverStateMachine.doAutoBatterySaverLocked();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void runOnBgThread(Runnable r) {
        BackgroundThread.getHandler().post(r);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void runOnBgThreadLazy(Runnable r, int delayMillis) {
        Handler h = BackgroundThread.getHandler();
        h.removeCallbacks(r);
        h.postDelayed(r, (long) delayMillis);
    }

    /* access modifiers changed from: package-private */
    public void refreshSettingsLocked() {
        boolean lowPowerModeEnabledSticky = true;
        boolean lowPowerModeEnabled = getGlobalSetting("low_power", 0) != 0;
        if (getGlobalSetting("low_power_sticky", 0) == 0) {
            lowPowerModeEnabledSticky = false;
        }
        setSettingsLocked(lowPowerModeEnabled, lowPowerModeEnabledSticky, getGlobalSetting("low_power_trigger_level", 0));
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setSettingsLocked(boolean batterySaverEnabled, boolean batterySaverEnabledSticky, int batterySaverTriggerThreshold) {
        this.mSettingsLoaded = true;
        boolean thresholdChanged = false;
        boolean enabledChanged = this.mSettingBatterySaverEnabled != batterySaverEnabled;
        boolean stickyChanged = this.mSettingBatterySaverEnabledSticky != batterySaverEnabledSticky;
        if (this.mSettingBatterySaverTriggerThreshold != batterySaverTriggerThreshold) {
            thresholdChanged = true;
        }
        if (enabledChanged || stickyChanged || thresholdChanged) {
            this.mSettingBatterySaverEnabled = batterySaverEnabled;
            this.mSettingBatterySaverEnabledSticky = batterySaverEnabledSticky;
            this.mSettingBatterySaverTriggerThreshold = batterySaverTriggerThreshold;
            if (thresholdChanged) {
                runOnBgThreadLazy(this.mThresholdChangeLogger, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            }
            if (enabledChanged) {
                enableBatterySaverLocked(batterySaverEnabled, true, 8, batterySaverEnabled ? "Global.low_power changed to 1" : "Global.low_power changed to 0");
            }
        }
    }

    public void setBatteryStatus(boolean newPowered, int newLevel, boolean newBatteryLevelLow) {
        synchronized (this.mLock) {
            boolean lowChanged = true;
            this.mBatteryStatusSet = true;
            boolean poweredChanged = this.mIsPowered != newPowered;
            boolean levelChanged = this.mBatteryLevel != newLevel;
            if (this.mIsBatteryLevelLow == newBatteryLevelLow) {
                lowChanged = false;
            }
            if (poweredChanged || levelChanged || lowChanged) {
                this.mIsPowered = newPowered;
                this.mBatteryLevel = newLevel;
                this.mIsBatteryLevelLow = newBatteryLevelLow;
                doAutoBatterySaverLocked();
            }
        }
    }

    private void doAutoBatterySaverLocked() {
        if (this.mBootCompleted && this.mSettingsLoaded && this.mBatteryStatusSet) {
            if (!this.mIsBatteryLevelLow) {
                updateSnoozingLocked(false, "Battery not low");
            }
            if (this.mIsPowered) {
                updateSnoozingLocked(false, "Plugged in");
                enableBatterySaverLocked(false, false, 7, "Plugged in");
            } else if (this.mSettingBatterySaverEnabledSticky) {
                enableBatterySaverLocked(true, true, 4, "Sticky restore");
            } else if (!this.mIsBatteryLevelLow) {
                enableBatterySaverLocked(false, false, 1, "Auto OFF");
            } else if (!this.mBatterySaverSnoozing && isAutoBatterySaverConfigured()) {
                enableBatterySaverLocked(true, false, 0, "Auto ON");
            }
        }
    }

    public void setBatterySaverEnabledManually(boolean enabled) {
        int i;
        synchronized (this.mLock) {
            if (enabled) {
                i = 2;
            } else {
                i = 3;
            }
            enableBatterySaverLocked(enabled, true, i, enabled ? "Manual ON" : "Manual OFF");
        }
    }

    private void enableBatterySaverLocked(boolean enable, boolean manual, int intReason, String strReason) {
        if (this.mBatterySaverController.isEnabled() != enable) {
            if (!enable || !this.mIsPowered) {
                this.mLastChangedIntReason = intReason;
                this.mLastChangedStrReason = strReason;
                if (manual) {
                    if (enable) {
                        updateSnoozingLocked(false, "Manual snooze OFF");
                    } else if (isBatterySaverEnabled() && this.mIsBatteryLevelLow) {
                        updateSnoozingLocked(true, "Manual snooze");
                    }
                }
                this.mSettingBatterySaverEnabled = enable;
                putGlobalSetting("low_power", enable);
                if (manual) {
                    this.mSettingBatterySaverEnabledSticky = enable;
                    putGlobalSetting("low_power_sticky", enable);
                }
                this.mBatterySaverController.enableBatterySaver(enable, intReason);
            }
        }
    }

    private void updateSnoozingLocked(boolean snoozing, String reason) {
        if (this.mBatterySaverSnoozing != snoozing) {
            this.mBatterySaverSnoozing = snoozing;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void putGlobalSetting(String key, int value) {
        Settings.Global.putInt(this.mContext.getContentResolver(), key, value);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getGlobalSetting(String key, int defValue) {
        return Settings.Global.getInt(this.mContext.getContentResolver(), key, defValue);
    }

    public void dump(PrintWriter pw) {
        synchronized (this.mLock) {
            pw.println();
            pw.println("Battery saver state machine:");
            pw.print("  Enabled=");
            pw.println(this.mBatterySaverController.isEnabled());
            pw.print("  mLastChangedIntReason=");
            pw.println(this.mLastChangedIntReason);
            pw.print("  mLastChangedStrReason=");
            pw.println(this.mLastChangedStrReason);
            pw.print("  mBootCompleted=");
            pw.println(this.mBootCompleted);
            pw.print("  mSettingsLoaded=");
            pw.println(this.mSettingsLoaded);
            pw.print("  mBatteryStatusSet=");
            pw.println(this.mBatteryStatusSet);
            pw.print("  mBatterySaverSnoozing=");
            pw.println(this.mBatterySaverSnoozing);
            pw.print("  mIsPowered=");
            pw.println(this.mIsPowered);
            pw.print("  mBatteryLevel=");
            pw.println(this.mBatteryLevel);
            pw.print("  mIsBatteryLevelLow=");
            pw.println(this.mIsBatteryLevelLow);
            pw.print("  mSettingBatterySaverEnabled=");
            pw.println(this.mSettingBatterySaverEnabled);
            pw.print("  mSettingBatterySaverEnabledSticky=");
            pw.println(this.mSettingBatterySaverEnabledSticky);
            pw.print("  mSettingBatterySaverTriggerThreshold=");
            pw.println(this.mSettingBatterySaverTriggerThreshold);
        }
    }

    public void dumpProto(ProtoOutputStream proto, long tag) {
        synchronized (this.mLock) {
            long token = proto.start(tag);
            proto.write(1133871366145L, this.mBatterySaverController.isEnabled());
            proto.write(1133871366146L, this.mBootCompleted);
            proto.write(1133871366147L, this.mSettingsLoaded);
            proto.write(1133871366148L, this.mBatteryStatusSet);
            proto.write(1133871366149L, this.mBatterySaverSnoozing);
            proto.write(1133871366150L, this.mIsPowered);
            proto.write(1120986464263L, this.mBatteryLevel);
            proto.write(1133871366152L, this.mIsBatteryLevelLow);
            proto.write(1133871366153L, this.mSettingBatterySaverEnabled);
            proto.write(1133871366154L, this.mSettingBatterySaverEnabledSticky);
            proto.write(1120986464267L, this.mSettingBatterySaverTriggerThreshold);
            proto.end(token);
        }
    }
}
