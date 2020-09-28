package com.android.internal.telephony;

import android.app.AlarmManager;
import android.app.timedetector.TimeDetector;
import android.app.timedetector.TimeSignal;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.TimestampedValue;

public class NewTimeServiceHelper {
    private static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    private final Context mContext;
    private final ContentResolver mCr;
    private Listener mListener;
    private final TimeDetector mTimeDetector;

    public interface Listener {
        void onTimeZoneDetectionChange(boolean z);
    }

    public NewTimeServiceHelper(Context context) {
        this.mContext = context;
        this.mCr = context.getContentResolver();
        this.mTimeDetector = (TimeDetector) context.getSystemService(TimeDetector.class);
    }

    public void setListener(final Listener listener) {
        if (listener == null) {
            throw new NullPointerException("listener==null");
        } else if (this.mListener == null) {
            this.mListener = listener;
            this.mCr.registerContentObserver(Settings.Global.getUriFor("auto_time_zone"), true, new ContentObserver(new Handler()) {
                /* class com.android.internal.telephony.NewTimeServiceHelper.AnonymousClass1 */

                public void onChange(boolean selfChange) {
                    listener.onTimeZoneDetectionChange(NewTimeServiceHelper.this.isTimeZoneDetectionEnabled());
                }
            });
        } else {
            throw new IllegalStateException("listener already set");
        }
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    public boolean isTimeZoneSettingInitialized() {
        return isTimeZoneSettingInitializedStatic();
    }

    public boolean isTimeZoneDetectionEnabled() {
        try {
            return Settings.Global.getInt(this.mCr, "auto_time_zone") > 0;
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
    }

    public void setDeviceTimeZone(String zoneId) {
        setDeviceTimeZoneStatic(this.mContext, zoneId);
    }

    public void suggestDeviceTime(TimestampedValue<Long> signalTimeMillis) {
        this.mTimeDetector.suggestTime(new TimeSignal("nitz", signalTimeMillis));
    }

    static boolean isTimeZoneSettingInitializedStatic() {
        String timeZoneId = SystemProperties.get(TIMEZONE_PROPERTY);
        return timeZoneId != null && timeZoneId.length() > 0 && !timeZoneId.equals("GMT");
    }

    static void setDeviceTimeZoneStatic(Context context, String zoneId) {
        ((AlarmManager) context.getSystemService("alarm")).setTimeZone(zoneId);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIMEZONE");
        intent.addFlags(536870912);
        intent.putExtra("time-zone", zoneId);
        context.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }
}
