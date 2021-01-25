package com.android.server.power.batterysaver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.BatterySaverPolicyConfig;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.BackgroundThread;
import com.android.server.EventLogTags;
import com.android.server.pm.DumpState;
import com.android.server.power.IHwShutdownThread;
import java.io.PrintWriter;
import java.text.NumberFormat;

public class BatterySaverStateMachine {
    private static final int ADAPTIVE_AUTO_DISABLE_BATTERY_LEVEL = 80;
    private static final long ADAPTIVE_CHANGE_TIMEOUT_MS = 86400000;
    private static final String BATTERY_SAVER_NOTIF_CHANNEL_ID = "battery_saver_channel";
    private static final boolean DEBUG = false;
    private static final int DYNAMIC_MODE_NOTIFICATION_ID = 1992;
    private static final String DYNAMIC_MODE_NOTIF_CHANNEL_ID = "dynamic_mode_notification";
    private static final int STATE_AUTOMATIC_ON = 3;
    private static final int STATE_MANUAL_ON = 2;
    private static final int STATE_OFF = 1;
    private static final int STATE_OFF_AUTOMATIC_SNOOZED = 4;
    private static final int STATE_PENDING_STICKY_ON = 5;
    private static final int STICKY_AUTO_DISABLED_NOTIFICATION_ID = 1993;
    private static final String TAG = "BatterySaverStateMachine";
    @GuardedBy({"mLock"})
    private int mBatteryLevel;
    private final BatterySaverController mBatterySaverController;
    private final boolean mBatterySaverStickyBehaviourDisabled;
    @GuardedBy({"mLock"})
    private boolean mBatteryStatusSet;
    @GuardedBy({"mLock"})
    private boolean mBootCompleted;
    private final Context mContext;
    @GuardedBy({"mLock"})
    private boolean mDynamicPowerSavingsBatterySaver;
    @GuardedBy({"mLock"})
    private final int mDynamicPowerSavingsDefaultDisableThreshold;
    @GuardedBy({"mLock"})
    private int mDynamicPowerSavingsDisableThreshold;
    @GuardedBy({"mLock"})
    private boolean mIsBatteryLevelLow;
    @GuardedBy({"mLock"})
    private boolean mIsPowered;
    @GuardedBy({"mLock"})
    private long mLastAdaptiveBatterySaverChangedExternallyElapsed;
    @GuardedBy({"mLock"})
    private int mLastChangedIntReason;
    @GuardedBy({"mLock"})
    private String mLastChangedStrReason;
    private final Object mLock;
    @GuardedBy({"mLock"})
    private int mSettingAutomaticBatterySaver;
    @GuardedBy({"mLock"})
    private boolean mSettingBatterySaverEnabled;
    @GuardedBy({"mLock"})
    private boolean mSettingBatterySaverEnabledSticky;
    @GuardedBy({"mLock"})
    private boolean mSettingBatterySaverStickyAutoDisableEnabled;
    @GuardedBy({"mLock"})
    private int mSettingBatterySaverStickyAutoDisableThreshold;
    @GuardedBy({"mLock"})
    private int mSettingBatterySaverTriggerThreshold;
    @GuardedBy({"mLock"})
    private boolean mSettingsLoaded;
    private final ContentObserver mSettingsObserver = new ContentObserver(null) {
        /* class com.android.server.power.batterysaver.BatterySaverStateMachine.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            synchronized (BatterySaverStateMachine.this.mLock) {
                BatterySaverStateMachine.this.refreshSettingsLocked();
            }
        }
    };
    @GuardedBy({"mLock"})
    private int mState;
    private final Runnable mThresholdChangeLogger = new Runnable() {
        /* class com.android.server.power.batterysaver.$$Lambda$BatterySaverStateMachine$SSfmWJrD4RBoVg8A8loZrSjhAo */

        @Override // java.lang.Runnable
        public final void run() {
            BatterySaverStateMachine.this.lambda$new$1$BatterySaverStateMachine();
        }
    };

    public BatterySaverStateMachine(Object lock, Context context, BatterySaverController batterySaverController) {
        this.mLock = lock;
        this.mContext = context;
        this.mBatterySaverController = batterySaverController;
        this.mState = 1;
        this.mBatterySaverStickyBehaviourDisabled = this.mContext.getResources().getBoolean(17891370);
        this.mDynamicPowerSavingsDefaultDisableThreshold = this.mContext.getResources().getInteger(17694808);
    }

    private boolean isAutomaticModeActiveLocked() {
        return this.mSettingAutomaticBatterySaver == 0 && this.mSettingBatterySaverTriggerThreshold > 0;
    }

    private boolean isInAutomaticLowZoneLocked() {
        return this.mIsBatteryLevelLow;
    }

    private boolean isDynamicModeActiveLocked() {
        return this.mSettingAutomaticBatterySaver == 1 && this.mDynamicPowerSavingsBatterySaver;
    }

    private boolean isInDynamicLowZoneLocked() {
        return this.mBatteryLevel <= this.mDynamicPowerSavingsDisableThreshold;
    }

    public void onBootCompleted() {
        putGlobalSetting("low_power", 0);
        runOnBgThread(new Runnable() {
            /* class com.android.server.power.batterysaver.$$Lambda$BatterySaverStateMachine$fEidyt_9TXlXBpF6D2lhOOrfOC4 */

            @Override // java.lang.Runnable
            public final void run() {
                BatterySaverStateMachine.this.lambda$onBootCompleted$0$BatterySaverStateMachine();
            }
        });
    }

    public /* synthetic */ void lambda$onBootCompleted$0$BatterySaverStateMachine() {
        ContentResolver cr = this.mContext.getContentResolver();
        boolean lowPowerModeEnabledSticky = false;
        cr.registerContentObserver(Settings.Global.getUriFor("low_power"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("low_power_sticky"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("low_power_trigger_level"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("automatic_power_save_mode"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("dynamic_power_savings_enabled"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("dynamic_power_savings_disable_threshold"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("low_power_sticky_auto_disable_enabled"), false, this.mSettingsObserver, 0);
        cr.registerContentObserver(Settings.Global.getUriFor("low_power_sticky_auto_disable_level"), false, this.mSettingsObserver, 0);
        synchronized (this.mLock) {
            if (getGlobalSetting("low_power_sticky", 0) != 0) {
                lowPowerModeEnabledSticky = true;
            }
            if (lowPowerModeEnabledSticky) {
                this.mState = 5;
            }
            this.mBootCompleted = true;
            refreshSettingsLocked();
            doAutoBatterySaverLocked();
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mLock"})
    private void refreshSettingsLocked() {
        boolean lowPowerModeEnabled = getGlobalSetting("low_power", 0) != 0;
        boolean lowPowerModeEnabledSticky = getGlobalSetting("low_power_sticky", 0) != 0;
        boolean dynamicPowerSavingsBatterySaver = getGlobalSetting("dynamic_power_savings_enabled", 0) != 0;
        setSettingsLocked(lowPowerModeEnabled, lowPowerModeEnabledSticky, getGlobalSetting("low_power_trigger_level", 0), getGlobalSetting("low_power_sticky_auto_disable_enabled", 1) != 0, getGlobalSetting("low_power_sticky_auto_disable_level", 90), getGlobalSetting("automatic_power_save_mode", 0), dynamicPowerSavingsBatterySaver, getGlobalSetting("dynamic_power_savings_disable_threshold", this.mDynamicPowerSavingsDefaultDisableThreshold));
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mLock"})
    @VisibleForTesting
    public void setSettingsLocked(boolean batterySaverEnabled, boolean batterySaverEnabledSticky, int batterySaverTriggerThreshold, boolean isStickyAutoDisableEnabled, int stickyAutoDisableThreshold, int automaticBatterySaver, boolean dynamicPowerSavingsBatterySaver, int dynamicPowerSavingsDisableThreshold) {
        this.mSettingsLoaded = true;
        int stickyAutoDisableThreshold2 = Math.max(stickyAutoDisableThreshold, batterySaverTriggerThreshold);
        boolean enabledChanged = this.mSettingBatterySaverEnabled != batterySaverEnabled;
        boolean stickyChanged = this.mSettingBatterySaverEnabledSticky != batterySaverEnabledSticky;
        boolean thresholdChanged = this.mSettingBatterySaverTriggerThreshold != batterySaverTriggerThreshold;
        boolean stickyAutoDisableEnabledChanged = this.mSettingBatterySaverStickyAutoDisableEnabled != isStickyAutoDisableEnabled;
        boolean stickyAutoDisableThresholdChanged = this.mSettingBatterySaverStickyAutoDisableThreshold != stickyAutoDisableThreshold2;
        boolean automaticModeChanged = this.mSettingAutomaticBatterySaver != automaticBatterySaver;
        boolean dynamicPowerSavingsThresholdChanged = this.mDynamicPowerSavingsDisableThreshold != dynamicPowerSavingsDisableThreshold;
        boolean dynamicPowerSavingsBatterySaverChanged = this.mDynamicPowerSavingsBatterySaver != dynamicPowerSavingsBatterySaver;
        if (enabledChanged || stickyChanged || thresholdChanged || automaticModeChanged || stickyAutoDisableEnabledChanged || stickyAutoDisableThresholdChanged || dynamicPowerSavingsThresholdChanged || dynamicPowerSavingsBatterySaverChanged) {
            this.mSettingBatterySaverEnabled = batterySaverEnabled;
            this.mSettingBatterySaverEnabledSticky = batterySaverEnabledSticky;
            this.mSettingBatterySaverTriggerThreshold = batterySaverTriggerThreshold;
            this.mSettingBatterySaverStickyAutoDisableEnabled = isStickyAutoDisableEnabled;
            this.mSettingBatterySaverStickyAutoDisableThreshold = stickyAutoDisableThreshold2;
            this.mSettingAutomaticBatterySaver = automaticBatterySaver;
            this.mDynamicPowerSavingsDisableThreshold = dynamicPowerSavingsDisableThreshold;
            this.mDynamicPowerSavingsBatterySaver = dynamicPowerSavingsBatterySaver;
            if (thresholdChanged) {
                runOnBgThreadLazy(this.mThresholdChangeLogger, IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME);
            }
            if (!this.mSettingBatterySaverStickyAutoDisableEnabled) {
                hideStickyDisabledNotification();
            }
            if (enabledChanged) {
                enableBatterySaverLocked(batterySaverEnabled, true, 8, batterySaverEnabled ? "Global.low_power changed to 1" : "Global.low_power changed to 0");
            } else {
                doAutoBatterySaverLocked();
            }
        }
    }

    public /* synthetic */ void lambda$new$1$BatterySaverStateMachine() {
        EventLogTags.writeBatterySaverSetting(this.mSettingBatterySaverTriggerThreshold);
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

    public boolean setAdaptiveBatterySaverEnabled(boolean enabled) {
        boolean adaptivePolicyEnabledLocked;
        synchronized (this.mLock) {
            this.mLastAdaptiveBatterySaverChangedExternallyElapsed = SystemClock.elapsedRealtime();
            adaptivePolicyEnabledLocked = this.mBatterySaverController.setAdaptivePolicyEnabledLocked(enabled, 11);
        }
        return adaptivePolicyEnabledLocked;
    }

    public boolean setAdaptiveBatterySaverPolicy(BatterySaverPolicyConfig config) {
        boolean adaptivePolicyLocked;
        synchronized (this.mLock) {
            this.mLastAdaptiveBatterySaverChangedExternallyElapsed = SystemClock.elapsedRealtime();
            adaptivePolicyLocked = this.mBatterySaverController.setAdaptivePolicyLocked(config, 11);
        }
        return adaptivePolicyLocked;
    }

    @GuardedBy({"mLock"})
    private void doAutoBatterySaverLocked() {
        if (this.mBootCompleted && this.mSettingsLoaded && this.mBatteryStatusSet) {
            updateStateLocked(false, false);
            if (SystemClock.elapsedRealtime() - this.mLastAdaptiveBatterySaverChangedExternallyElapsed > 86400000) {
                this.mBatterySaverController.setAdaptivePolicyEnabledLocked(false, 12);
                this.mBatterySaverController.resetAdaptivePolicyLocked(12);
            } else if (this.mIsPowered && this.mBatteryLevel >= 80) {
                this.mBatterySaverController.setAdaptivePolicyEnabledLocked(false, 7);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void updateStateLocked(boolean manual, boolean enable) {
        if (manual || (this.mBootCompleted && this.mSettingsLoaded && this.mBatteryStatusSet)) {
            int i = this.mState;
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4) {
                            if (i != 5) {
                                Slog.wtf(TAG, "Unknown state: " + this.mState);
                            } else if (manual) {
                                Slog.e(TAG, "Tried to manually change BS state from PENDING_STICKY_ON");
                            } else {
                                boolean shouldTurnOffSticky = this.mSettingBatterySaverStickyAutoDisableEnabled && this.mBatteryLevel >= this.mSettingBatterySaverStickyAutoDisableThreshold;
                                if ((this.mBatterySaverStickyBehaviourDisabled || !this.mSettingBatterySaverEnabledSticky) || shouldTurnOffSticky) {
                                    this.mState = 1;
                                    setStickyActive(false);
                                    triggerStickyDisabledNotification();
                                } else if (!this.mIsPowered) {
                                    enableBatterySaverLocked(true, true, 4);
                                    this.mState = 2;
                                }
                            }
                        } else if (manual) {
                            if (!enable) {
                                Slog.e(TAG, "Tried to disable BS when it's already AUTO_SNOOZED");
                                return;
                            }
                            enableBatterySaverLocked(true, true, 2);
                            this.mState = 2;
                        } else if (this.mIsPowered || ((isAutomaticModeActiveLocked() && !isInAutomaticLowZoneLocked()) || ((isDynamicModeActiveLocked() && !isInDynamicLowZoneLocked()) || (!isAutomaticModeActiveLocked() && !isDynamicModeActiveLocked())))) {
                            this.mState = 1;
                        }
                    } else if (this.mIsPowered) {
                        enableBatterySaverLocked(false, false, 7);
                        this.mState = 1;
                    } else if (manual) {
                        if (enable) {
                            Slog.e(TAG, "Tried to enable BS when it's already AUTO_ON");
                            return;
                        }
                        enableBatterySaverLocked(false, true, 3);
                        this.mState = 4;
                    } else if (isAutomaticModeActiveLocked() && !isInAutomaticLowZoneLocked()) {
                        enableBatterySaverLocked(false, false, 1);
                        this.mState = 1;
                    } else if (isDynamicModeActiveLocked() && !isInDynamicLowZoneLocked()) {
                        enableBatterySaverLocked(false, false, 10);
                        this.mState = 1;
                    } else if (!isAutomaticModeActiveLocked() && !isDynamicModeActiveLocked()) {
                        enableBatterySaverLocked(false, false, 8);
                        this.mState = 1;
                    }
                } else if (manual) {
                    if (enable) {
                        Slog.e(TAG, "Tried to enable BS when it's already MANUAL_ON");
                        return;
                    }
                    enableBatterySaverLocked(false, true, 3);
                    this.mState = 1;
                } else if (this.mIsPowered) {
                    enableBatterySaverLocked(false, false, 7);
                    if (!this.mSettingBatterySaverEnabledSticky || this.mBatterySaverStickyBehaviourDisabled) {
                        this.mState = 1;
                    } else {
                        this.mState = 5;
                    }
                }
            } else if (this.mIsPowered) {
            } else {
                if (manual) {
                    if (!enable) {
                        Slog.e(TAG, "Tried to disable BS when it's already OFF");
                        return;
                    }
                    enableBatterySaverLocked(true, true, 2);
                    hideStickyDisabledNotification();
                    this.mState = 2;
                } else if (isAutomaticModeActiveLocked() && isInAutomaticLowZoneLocked()) {
                    enableBatterySaverLocked(true, false, 0);
                    hideStickyDisabledNotification();
                    this.mState = 3;
                } else if (isDynamicModeActiveLocked() && isInDynamicLowZoneLocked()) {
                    enableBatterySaverLocked(true, false, 9);
                    hideStickyDisabledNotification();
                    this.mState = 3;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getState() {
        int i;
        synchronized (this.mLock) {
            i = this.mState;
        }
        return i;
    }

    public void setBatterySaverEnabledManually(boolean enabled) {
        synchronized (this.mLock) {
            updateStateLocked(true, enabled);
        }
    }

    @GuardedBy({"mLock"})
    private void enableBatterySaverLocked(boolean enable, boolean manual, int intReason) {
        enableBatterySaverLocked(enable, manual, intReason, BatterySaverController.reasonToString(intReason));
    }

    @GuardedBy({"mLock"})
    private void enableBatterySaverLocked(boolean enable, boolean manual, int intReason, String strReason) {
        if (this.mBatterySaverController.isFullEnabled() != enable) {
            if (!enable || !this.mIsPowered) {
                this.mLastChangedIntReason = intReason;
                this.mLastChangedStrReason = strReason;
                this.mSettingBatterySaverEnabled = enable;
                putGlobalSetting("low_power", enable ? 1 : 0);
                if (manual) {
                    setStickyActive(!this.mBatterySaverStickyBehaviourDisabled && enable);
                }
                this.mBatterySaverController.enableBatterySaver(enable, intReason);
                if (intReason == 9) {
                    runOnBgThread(new Runnable() {
                        /* class com.android.server.power.batterysaver.$$Lambda$mQgroChNR1F7zC7uPirCwSx_zNg */

                        @Override // java.lang.Runnable
                        public final void run() {
                            BatterySaverStateMachine.this.triggerDynamicModeNotification();
                        }
                    });
                } else if (!enable) {
                    runOnBgThread(new Runnable() {
                        /* class com.android.server.power.batterysaver.$$Lambda$BatterySaverStateMachine$WGmfDqFGirqPfth6R7MtcSKGvs */

                        @Override // java.lang.Runnable
                        public final void run() {
                            BatterySaverStateMachine.this.hideDynamicModeNotification();
                        }
                    });
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void triggerDynamicModeNotification() {
        NotificationManager manager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        ensureNotificationChannelExists(manager, DYNAMIC_MODE_NOTIF_CHANNEL_ID, 17040015);
        manager.notifyAsUser(TAG, DYNAMIC_MODE_NOTIFICATION_ID, buildNotification(DYNAMIC_MODE_NOTIF_CHANNEL_ID, this.mContext.getResources().getString(17040017), 17040016, "android.intent.action.POWER_USAGE_SUMMARY"), UserHandle.ALL);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void triggerStickyDisabledNotification() {
        NotificationManager manager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        ensureNotificationChannelExists(manager, BATTERY_SAVER_NOTIF_CHANNEL_ID, 17039707);
        manager.notifyAsUser(TAG, STICKY_AUTO_DISABLED_NOTIFICATION_ID, buildNotification(BATTERY_SAVER_NOTIF_CHANNEL_ID, this.mContext.getResources().getString(17039704, NumberFormat.getPercentInstance().format(((double) this.mBatteryLevel) / 100.0d)), 17039709, "android.settings.BATTERY_SAVER_SETTINGS"), UserHandle.ALL);
    }

    private void ensureNotificationChannelExists(NotificationManager manager, String channelId, int nameId) {
        NotificationChannel channel = new NotificationChannel(channelId, this.mContext.getText(nameId), 3);
        channel.setSound(null, null);
        channel.setBlockableSystem(true);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification(String channelId, String title, int summaryId, String intentAction) {
        Resources res = this.mContext.getResources();
        Intent intent = new Intent(intentAction);
        intent.setFlags(268468224);
        PendingIntent batterySaverIntent = PendingIntent.getActivity(this.mContext, 0, intent, DumpState.DUMP_HWFEATURES);
        String summary = res.getString(summaryId);
        return new Notification.Builder(this.mContext, channelId).setSmallIcon(17302313).setContentTitle(title).setContentText(summary).setContentIntent(batterySaverIntent).setStyle(new Notification.BigTextStyle().bigText(summary)).setOnlyAlertOnce(true).setAutoCancel(true).build();
    }

    /* access modifiers changed from: private */
    public void hideDynamicModeNotification() {
        hideNotification(DYNAMIC_MODE_NOTIFICATION_ID);
    }

    private void hideStickyDisabledNotification() {
        hideNotification(STICKY_AUTO_DISABLED_NOTIFICATION_ID);
    }

    private void hideNotification(int notificationId) {
        ((NotificationManager) this.mContext.getSystemService(NotificationManager.class)).cancel(notificationId);
    }

    private void setStickyActive(boolean active) {
        this.mSettingBatterySaverEnabledSticky = active;
        putGlobalSetting("low_power_sticky", this.mSettingBatterySaverEnabledSticky ? 1 : 0);
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
            pw.print("    full=");
            pw.println(this.mBatterySaverController.isFullEnabled());
            pw.print("    adaptive=");
            pw.print(this.mBatterySaverController.isAdaptiveEnabled());
            if (this.mBatterySaverController.isAdaptiveEnabled()) {
                pw.print(" (advertise=");
                pw.print(this.mBatterySaverController.getBatterySaverPolicy().shouldAdvertiseIsEnabled());
                pw.print(")");
            }
            pw.println();
            pw.print("  mState=");
            pw.println(this.mState);
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
            pw.print("  mSettingBatterySaverStickyAutoDisableEnabled=");
            pw.println(this.mSettingBatterySaverStickyAutoDisableEnabled);
            pw.print("  mSettingBatterySaverStickyAutoDisableThreshold=");
            pw.println(this.mSettingBatterySaverStickyAutoDisableThreshold);
            pw.print("  mSettingBatterySaverTriggerThreshold=");
            pw.println(this.mSettingBatterySaverTriggerThreshold);
            pw.print("  mBatterySaverStickyBehaviourDisabled=");
            pw.println(this.mBatterySaverStickyBehaviourDisabled);
            pw.print("  mLastAdaptiveBatterySaverChangedExternallyElapsed=");
            pw.println(this.mLastAdaptiveBatterySaverChangedExternallyElapsed);
        }
    }

    public void dumpProto(ProtoOutputStream proto, long tag) {
        synchronized (this.mLock) {
            long token = proto.start(tag);
            proto.write(1133871366145L, this.mBatterySaverController.isEnabled());
            proto.write(1159641169938L, this.mState);
            proto.write(1133871366158L, this.mBatterySaverController.isFullEnabled());
            proto.write(1133871366159L, this.mBatterySaverController.isAdaptiveEnabled());
            proto.write(1133871366160L, this.mBatterySaverController.getBatterySaverPolicy().shouldAdvertiseIsEnabled());
            proto.write(1133871366146L, this.mBootCompleted);
            proto.write(1133871366147L, this.mSettingsLoaded);
            proto.write(1133871366148L, this.mBatteryStatusSet);
            proto.write(1133871366150L, this.mIsPowered);
            proto.write(1120986464263L, this.mBatteryLevel);
            proto.write(1133871366152L, this.mIsBatteryLevelLow);
            proto.write(1133871366153L, this.mSettingBatterySaverEnabled);
            proto.write(1133871366154L, this.mSettingBatterySaverEnabledSticky);
            proto.write(1120986464267L, this.mSettingBatterySaverTriggerThreshold);
            proto.write(1133871366156L, this.mSettingBatterySaverStickyAutoDisableEnabled);
            proto.write(1120986464269L, this.mSettingBatterySaverStickyAutoDisableThreshold);
            proto.write(1112396529681L, this.mLastAdaptiveBatterySaverChangedExternallyElapsed);
            proto.end(token);
        }
    }
}
