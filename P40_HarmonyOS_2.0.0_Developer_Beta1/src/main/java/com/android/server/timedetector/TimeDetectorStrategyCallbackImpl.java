package com.android.server.timedetector;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.timedetector.TimeDetectorStrategy;
import java.util.Objects;

public final class TimeDetectorStrategyCallbackImpl implements TimeDetectorStrategy.Callback {
    private static final int SYSTEM_CLOCK_UPDATE_THRESHOLD_MILLIS_DEFAULT = 2000;
    private static final String TAG = "timedetector.TimeDetectorStrategyCallbackImpl";
    private final AlarmManager mAlarmManager;
    private final ContentResolver mContentResolver;
    private final Context mContext;
    private final int mSystemClockUpdateThresholdMillis = SystemProperties.getInt("ro.sys.time_detector_update_diff", 2000);
    private final PowerManager.WakeLock mWakeLock;

    public TimeDetectorStrategyCallbackImpl(Context context) {
        this.mContext = (Context) Objects.requireNonNull(context);
        this.mContentResolver = (ContentResolver) Objects.requireNonNull(context.getContentResolver());
        this.mWakeLock = (PowerManager.WakeLock) Objects.requireNonNull(((PowerManager) context.getSystemService(PowerManager.class)).newWakeLock(1, TAG));
        this.mAlarmManager = (AlarmManager) Objects.requireNonNull((AlarmManager) context.getSystemService(AlarmManager.class));
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public int systemClockUpdateThresholdMillis() {
        return this.mSystemClockUpdateThresholdMillis;
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public boolean isTimeDetectionEnabled() {
        try {
            return Settings.Global.getInt(this.mContentResolver, "auto_time") != 0;
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public void acquireWakeLock() {
        if (this.mWakeLock.isHeld()) {
            Slog.wtf(TAG, "WakeLock " + this.mWakeLock + " already held");
        }
        this.mWakeLock.acquire();
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public long elapsedRealtimeMillis() {
        checkWakeLockHeld();
        return SystemClock.elapsedRealtime();
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public long systemClockMillis() {
        checkWakeLockHeld();
        return System.currentTimeMillis();
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public void setSystemClock(long newTimeMillis) {
        checkWakeLockHeld();
        this.mAlarmManager.setTime(newTimeMillis);
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public void releaseWakeLock() {
        checkWakeLockHeld();
        this.mWakeLock.release();
    }

    @Override // com.android.server.timedetector.TimeDetectorStrategy.Callback
    public void sendStickyBroadcast(Intent intent) {
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void checkWakeLockHeld() {
        if (!this.mWakeLock.isHeld()) {
            Slog.wtf(TAG, "WakeLock " + this.mWakeLock + " not held");
        }
    }
}
