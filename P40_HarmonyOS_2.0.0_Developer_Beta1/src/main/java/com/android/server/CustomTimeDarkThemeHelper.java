package com.android.server;

import android.app.AlarmManager;
import android.app.IUiModeManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.display.Time;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class CustomTimeDarkThemeHelper {
    private static final int NOT_SET = -1;
    private static final String TAG = "CustomTimeDarkThemeHelper";
    private Context mContext;
    private int mCurrentDarkThemeMode;
    private CustomTimeDarkTheme mCustomTimeDarkTheme;
    private boolean mCustomTimeDarkThemeActivated;
    private final ContentObserver mDarkThemeObserver = new ContentObserver(this.mHandler) {
        /* class com.android.server.CustomTimeDarkThemeHelper.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri) {
            String setting = uri == null ? null : uri.getLastPathSegment();
            if (TextUtils.isEmpty(setting)) {
                Slog.i(CustomTimeDarkThemeHelper.TAG, "CustomTimeDarkThemeObserver#onChange: setting is emprty or null");
                return;
            }
            char c = 65535;
            switch (setting.hashCode()) {
                case -1106741696:
                    if (setting.equals("dark_theme_custom_end_time")) {
                        c = 2;
                        break;
                    }
                    break;
                case -397676711:
                    if (setting.equals("dark_theme_custom_start_time")) {
                        c = 1;
                        break;
                    }
                    break;
                case 53178102:
                    if (setting.equals("custom_time_dark_theme_activated")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1186889717:
                    if (setting.equals("ui_night_mode")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                int nightMode = CustomTimeDarkThemeHelper.this.getNightMode();
                if (nightMode == 0) {
                    Slog.i(CustomTimeDarkThemeHelper.TAG, "CustomTimeDarkThemeObserver#onChange: nightMode=" + nightMode + " SKIP!!!");
                    return;
                }
                boolean activated = CustomTimeDarkThemeHelper.this.isCustomTimeDarkThemeActivatedInternal();
                if (CustomTimeDarkThemeHelper.this.mCustomTimeDarkThemeActivated != activated) {
                    Settings.Secure.putStringForUser(CustomTimeDarkThemeHelper.this.mContext.getContentResolver(), "dark_theme_last_activated_time", null, CustomTimeDarkThemeHelper.this.mUserHandle);
                    Slog.i(CustomTimeDarkThemeHelper.TAG, "CustomTimeDarkThemeObserver#onChange: set dark_theme_last_activated_time to null");
                }
                CustomTimeDarkThemeHelper.this.mCustomTimeDarkThemeActivated = activated;
                StringBuilder sb = new StringBuilder();
                sb.append("CustomTimeDarkThemeObserver#onChange: ");
                sb.append(setting);
                sb.append(" is");
                sb.append(activated ? " " : " not ");
                sb.append("activated");
                Slog.i(CustomTimeDarkThemeHelper.TAG, sb.toString());
                CustomTimeDarkThemeHelper.this.onCustomTimeDarkThemeChanged(activated);
            } else if (c == 1) {
                CustomTimeDarkThemeHelper customTimeDarkThemeHelper = CustomTimeDarkThemeHelper.this;
                customTimeDarkThemeHelper.onDarkThemeCustomStartTimeChanged(customTimeDarkThemeHelper.getDarkThemeCustomStartTimeInternal().getLocalTime());
            } else if (c == 2) {
                CustomTimeDarkThemeHelper customTimeDarkThemeHelper2 = CustomTimeDarkThemeHelper.this;
                customTimeDarkThemeHelper2.onDarkThemeCustomEndTimeChanged(customTimeDarkThemeHelper2.getDarkThemeCustomEndTimeInternal().getLocalTime());
            } else if (c == 3) {
                int mode = CustomTimeDarkThemeHelper.this.getNightMode();
                if (CustomTimeDarkThemeHelper.this.mCurrentDarkThemeMode != mode) {
                    String now = LocalDateTime.now().toString();
                    Slog.i(CustomTimeDarkThemeHelper.TAG, "CustomTimeDarkThemeObserver#onChange: set dark_theme_last_activated_time to " + now);
                    Settings.Secure.putStringForUser(CustomTimeDarkThemeHelper.this.mContext.getContentResolver(), "dark_theme_last_activated_time", now, CustomTimeDarkThemeHelper.this.mUserHandle);
                }
                CustomTimeDarkThemeHelper.this.mCurrentDarkThemeMode = mode;
            }
        }
    };
    private Handler mHandler;
    private IUiModeManager.Stub mService;
    private int mUserHandle;

    protected CustomTimeDarkThemeHelper(IUiModeManager.Stub service, Handler handler) {
        this.mService = service;
        this.mHandler = handler;
    }

    /* access modifiers changed from: protected */
    public void onStarted(Context context, int userHandle) {
        Slog.i(TAG, "onStarted: userHandle=" + userHandle);
        this.mContext = context;
        this.mUserHandle = userHandle;
        this.mCurrentDarkThemeMode = getNightMode();
        this.mCustomTimeDarkThemeActivated = isCustomTimeDarkThemeActivatedInternal();
        registerCustomTimeDarkThemeObserver(this.mContext, this.mUserHandle);
    }

    /* access modifiers changed from: protected */
    public void onUserSwitched(Context context, int userHandle) {
        Slog.i(TAG, "onUserSwitched: userHandle=" + userHandle);
        this.mContext = context;
        this.mUserHandle = userHandle;
        unregisterCustomTimeDarkThemeObserver(this.mContext);
        registerCustomTimeDarkThemeObserver(this.mContext, this.mUserHandle);
        onCustomTimeDarkThemeChanged(isCustomTimeDarkThemeActivatedInternal());
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
        Slog.i(TAG, "onBootCompleted");
        onCustomTimeDarkThemeChanged(isCustomTimeDarkThemeActivatedInternal());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getNightMode() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "ui_night_mode", 1, this.mUserHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setNightMode(int mode) {
        try {
            Slog.i(TAG, "set night mode " + mode + " for user " + this.mUserHandle);
            this.mService.setNightModeForUser(mode, this.mUserHandle);
        } catch (RemoteException e) {
            Slog.e(TAG, "setNightMode", e);
        }
    }

    private void registerCustomTimeDarkThemeObserver(Context context, int userHandle) {
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("custom_time_dark_theme_activated"), false, this.mDarkThemeObserver, userHandle);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("dark_theme_custom_start_time"), false, this.mDarkThemeObserver, userHandle);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("dark_theme_custom_end_time"), false, this.mDarkThemeObserver, userHandle);
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("ui_night_mode"), false, this.mDarkThemeObserver, userHandle);
    }

    private void unregisterCustomTimeDarkThemeObserver(Context context) {
        context.getContentResolver().unregisterContentObserver(this.mDarkThemeObserver);
    }

    /* access modifiers changed from: private */
    public final class CustomTimeDarkTheme implements AlarmManager.OnAlarmListener {
        private static final int INVALID_NIGHT_MODE = -1;
        private final AlarmManager mAlarmManager;
        private LocalTime mEndTime;
        private final KeyguardManager mKeyguardManager;
        private LocalDateTime mLastActivatedTime;
        private int mNextNightMode = -1;
        private final PowerManager mPowerManager;
        private final BroadcastReceiver mScreenStateChangedReceiver;
        private boolean mShouldDelaySwitch;
        private LocalTime mStartTime;
        private final BroadcastReceiver mTimeChangedReceiver;

        CustomTimeDarkTheme() {
            this.mPowerManager = (PowerManager) CustomTimeDarkThemeHelper.this.mContext.getSystemService("power");
            this.mAlarmManager = (AlarmManager) CustomTimeDarkThemeHelper.this.mContext.getSystemService("alarm");
            this.mKeyguardManager = (KeyguardManager) CustomTimeDarkThemeHelper.this.mContext.getSystemService("keyguard");
            this.mTimeChangedReceiver = new BroadcastReceiver(CustomTimeDarkThemeHelper.this) {
                /* class com.android.server.CustomTimeDarkThemeHelper.CustomTimeDarkTheme.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    Slog.i(CustomTimeDarkThemeHelper.TAG, "Time changed: " + intent.getAction());
                    CustomTimeDarkTheme customTimeDarkTheme = CustomTimeDarkTheme.this;
                    customTimeDarkTheme.mLastActivatedTime = CustomTimeDarkThemeHelper.this.getDarkThemeLastActivatedTimeSetting();
                    CustomTimeDarkTheme.this.mShouldDelaySwitch = true;
                    CustomTimeDarkTheme.this.updateActivated();
                }
            };
            this.mScreenStateChangedReceiver = new BroadcastReceiver(CustomTimeDarkThemeHelper.this) {
                /* class com.android.server.CustomTimeDarkThemeHelper.CustomTimeDarkTheme.AnonymousClass2 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (CustomTimeDarkTheme.this.mShouldDelaySwitch) {
                        Slog.i(CustomTimeDarkThemeHelper.TAG, "screen state changed: " + intent.getAction());
                        CustomTimeDarkTheme.this.mShouldDelaySwitch = false;
                        CustomTimeDarkTheme.this.tryToSwitchThemeMode();
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateActivated() {
            PowerManager powerManager;
            KeyguardManager keyguardManager;
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = CustomTimeDarkThemeHelper.getDateTimeBefore(this.mStartTime, now);
            LocalDateTime end = CustomTimeDarkThemeHelper.getDateTimeAfter(this.mEndTime, start);
            boolean activated = now.isBefore(end);
            LocalDateTime localDateTime = this.mLastActivatedTime;
            int i = 2;
            if (localDateTime != null && localDateTime.isBefore(now) && this.mLastActivatedTime.isAfter(start) && (this.mLastActivatedTime.isAfter(end) || now.isBefore(end))) {
                activated = CustomTimeDarkThemeHelper.this.getNightMode() == 2;
            }
            updateNextAlarm(activated, now);
            if (CustomTimeDarkThemeHelper.this.getNightMode() == 0) {
                Slog.w(CustomTimeDarkThemeHelper.TAG, "updateActivated: current night mode is 0 for user " + CustomTimeDarkThemeHelper.this.mUserHandle);
                return;
            }
            if (!activated) {
                i = 1;
            }
            this.mNextNightMode = i;
            if (this.mShouldDelaySwitch && (keyguardManager = this.mKeyguardManager) != null) {
                this.mShouldDelaySwitch = !keyguardManager.isKeyguardLocked();
                if (!this.mShouldDelaySwitch) {
                    Slog.i(CustomTimeDarkThemeHelper.TAG, "updateActivated: keyguard is locked");
                }
            }
            if (this.mShouldDelaySwitch && (powerManager = this.mPowerManager) != null) {
                this.mShouldDelaySwitch = powerManager.isScreenOn();
                if (!this.mShouldDelaySwitch) {
                    Slog.i(CustomTimeDarkThemeHelper.TAG, "updateActivated: screen is off");
                }
            }
            Slog.i(CustomTimeDarkThemeHelper.TAG, "updateActivated: now=" + now + ", start=" + start + ", end=" + end + ", activated=" + activated + ", mLastActivatedTime=" + this.mLastActivatedTime + ", userHandle=" + CustomTimeDarkThemeHelper.this.mUserHandle + ", mShouldDelaySwitchMode=" + this.mShouldDelaySwitch + ", mNextNightMode=" + this.mNextNightMode);
            if (!this.mShouldDelaySwitch) {
                tryToSwitchThemeMode();
            }
        }

        private void updateNextAlarm(boolean activated, LocalDateTime now) {
            LocalDateTime next;
            if (activated) {
                next = CustomTimeDarkThemeHelper.getDateTimeAfter(this.mEndTime, now);
            } else {
                next = CustomTimeDarkThemeHelper.getDateTimeAfter(this.mStartTime, now);
            }
            this.mAlarmManager.setExact(1, next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), CustomTimeDarkThemeHelper.TAG, this, null);
            Slog.i(CustomTimeDarkThemeHelper.TAG, "updateNextAlarm: activated=" + activated + ", next=" + next);
        }

        public void onStart() {
            Slog.i(CustomTimeDarkThemeHelper.TAG, "onStart");
            IntentFilter intentFilter = new IntentFilter("android.intent.action.TIME_SET");
            intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
            CustomTimeDarkThemeHelper.this.mContext.registerReceiver(this.mTimeChangedReceiver, intentFilter);
            IntentFilter screenStateFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
            screenStateFilter.addAction("android.intent.action.SCREEN_ON");
            screenStateFilter.addAction("android.intent.action.USER_PRESENT");
            CustomTimeDarkThemeHelper.this.mContext.registerReceiver(this.mScreenStateChangedReceiver, screenStateFilter);
            this.mStartTime = CustomTimeDarkThemeHelper.this.getDarkThemeCustomStartTimeInternal().getLocalTime();
            this.mEndTime = CustomTimeDarkThemeHelper.this.getDarkThemeCustomEndTimeInternal().getLocalTime();
            this.mLastActivatedTime = CustomTimeDarkThemeHelper.this.getDarkThemeLastActivatedTimeSetting();
            this.mShouldDelaySwitch = false;
            updateActivated();
        }

        public void onStop() {
            try {
                CustomTimeDarkThemeHelper.this.mContext.unregisterReceiver(this.mTimeChangedReceiver);
                CustomTimeDarkThemeHelper.this.mContext.unregisterReceiver(this.mScreenStateChangedReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(CustomTimeDarkThemeHelper.TAG, "Failed to unregister reveivers.");
            }
            this.mAlarmManager.cancel(this);
            this.mLastActivatedTime = null;
            this.mShouldDelaySwitch = false;
        }

        public void onCustomStartTimeChanged(LocalTime startTime) {
            Slog.i(CustomTimeDarkThemeHelper.TAG, "onCustomStartTimeChanged=" + startTime);
            this.mStartTime = startTime;
            this.mLastActivatedTime = null;
            this.mShouldDelaySwitch = false;
            updateActivated();
        }

        public void onCustomEndTimeChanged(LocalTime endTime) {
            Slog.i(CustomTimeDarkThemeHelper.TAG, "onCustomEndTimeChanged=" + endTime);
            this.mEndTime = endTime;
            this.mLastActivatedTime = null;
            this.mShouldDelaySwitch = false;
            updateActivated();
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            Slog.i(CustomTimeDarkThemeHelper.TAG, "onAlarm");
            this.mLastActivatedTime = CustomTimeDarkThemeHelper.this.getDarkThemeLastActivatedTimeSetting();
            this.mShouldDelaySwitch = true;
            updateActivated();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void tryToSwitchThemeMode() {
            int i = this.mNextNightMode;
            if (i != -1) {
                CustomTimeDarkThemeHelper.this.setNightMode(i);
                this.mNextNightMode = -1;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCustomTimeDarkThemeActivatedInternal() {
        return Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "custom_time_dark_theme_activated", 0, this.mUserHandle) == 1;
    }

    private boolean setCustomTimeDarkThemeActivatedInternal(boolean activated) {
        return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "custom_time_dark_theme_activated", activated ? 1 : 0, this.mUserHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Time getDarkThemeCustomStartTimeInternal() {
        int startTimeValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "dark_theme_custom_start_time", -1, this.mUserHandle);
        if (startTimeValue == -1) {
            startTimeValue = 79200000;
        }
        return new Time(LocalTime.ofSecondOfDay((long) (startTimeValue / 1000)));
    }

    private boolean setDarkThemeCustomStartTimeInternal(Time startTime) {
        return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "dark_theme_custom_start_time", startTime.getLocalTime().toSecondOfDay() * 1000, this.mUserHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Time getDarkThemeCustomEndTimeInternal() {
        int endTimeValue = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "dark_theme_custom_end_time", -1, this.mUserHandle);
        if (endTimeValue == -1) {
            endTimeValue = 21600000;
        }
        return new Time(LocalTime.ofSecondOfDay((long) (endTimeValue / 1000)));
    }

    private boolean setDarkThemeCustomEndTimeInternal(Time endTime) {
        return Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "dark_theme_custom_end_time", endTime.getLocalTime().toSecondOfDay() * 1000, this.mUserHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onCustomTimeDarkThemeChanged(boolean activited) {
        Slog.i(TAG, "onCustomTimeDarkThemeChanged: activited=" + activited);
        CustomTimeDarkTheme customTimeDarkTheme = this.mCustomTimeDarkTheme;
        if (customTimeDarkTheme != null) {
            customTimeDarkTheme.onStop();
            this.mCustomTimeDarkTheme = null;
        }
        if (activited) {
            this.mCustomTimeDarkTheme = new CustomTimeDarkTheme();
            this.mCustomTimeDarkTheme.onStart();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDarkThemeCustomStartTimeChanged(LocalTime startTime) {
        Slog.i(TAG, "onDarkThemeCustomStartTimeChanged: startTime=" + startTime);
        CustomTimeDarkTheme customTimeDarkTheme = this.mCustomTimeDarkTheme;
        if (customTimeDarkTheme != null) {
            customTimeDarkTheme.onCustomStartTimeChanged(startTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDarkThemeCustomEndTimeChanged(LocalTime endTime) {
        Slog.i(TAG, "onDarkThemeCustomEndTimeChanged: endTime=" + endTime);
        CustomTimeDarkTheme customTimeDarkTheme = this.mCustomTimeDarkTheme;
        if (customTimeDarkTheme != null) {
            customTimeDarkTheme.onCustomEndTimeChanged(endTime);
        }
    }

    /* access modifiers changed from: private */
    public static LocalDateTime getDateTimeBefore(LocalTime localTime, LocalDateTime compareTime) {
        LocalDateTime ldt = LocalDateTime.of(compareTime.getYear(), compareTime.getMonth(), compareTime.getDayOfMonth(), localTime.getHour(), localTime.getMinute());
        return ldt.isAfter(compareTime) ? ldt.minusDays(1) : ldt;
    }

    /* access modifiers changed from: private */
    public static LocalDateTime getDateTimeAfter(LocalTime localTime, LocalDateTime compareTime) {
        LocalDateTime ldt = LocalDateTime.of(compareTime.getYear(), compareTime.getMonth(), compareTime.getDayOfMonth(), localTime.getHour(), localTime.getMinute());
        return ldt.isBefore(compareTime) ? ldt.plusDays(1) : ldt;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LocalDateTime getDarkThemeLastActivatedTimeSetting() {
        String lastActivatedTime = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "dark_theme_last_activated_time", this.mUserHandle);
        if (lastActivatedTime != null) {
            try {
                return LocalDateTime.parse(lastActivatedTime);
            } catch (DateTimeParseException e) {
                Slog.w(TAG, "date time parse exception, ignored.");
                try {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(lastActivatedTime)), ZoneId.systemDefault());
                } catch (NumberFormatException | DateTimeException e2) {
                    Slog.w(TAG, "date time parse or number format exception, ignored.");
                }
            }
        }
        return LocalDateTime.MIN;
    }
}
