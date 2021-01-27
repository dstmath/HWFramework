package com.android.internal.telephony;

import android.app.AlarmManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;

public class TimeServiceHelper {
    private static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    private final Context mContext;
    private final ContentResolver mCr;
    private Listener mListener;

    public interface Listener {
        void onTimeDetectionChange(boolean z);

        void onTimeZoneDetectionChange(boolean z);
    }

    public TimeServiceHelper(Context context) {
        this.mContext = context;
        this.mCr = context.getContentResolver();
    }

    public void setListener(final Listener listener) {
        if (listener == null) {
            throw new NullPointerException("listener==null");
        } else if (this.mListener == null) {
            this.mListener = listener;
            this.mCr.registerContentObserver(Settings.Global.getUriFor("auto_time"), true, new ContentObserver(new Handler()) {
                /* class com.android.internal.telephony.TimeServiceHelper.AnonymousClass1 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    listener.onTimeDetectionChange(TimeServiceHelper.this.isTimeDetectionEnabled());
                }
            });
            this.mCr.registerContentObserver(Settings.Global.getUriFor("auto_time_zone"), true, new ContentObserver(new Handler()) {
                /* class com.android.internal.telephony.TimeServiceHelper.AnonymousClass2 */

                @Override // android.database.ContentObserver
                public void onChange(boolean selfChange) {
                    listener.onTimeZoneDetectionChange(TimeServiceHelper.this.isTimeZoneDetectionEnabled());
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

    public boolean isTimeDetectionEnabled() {
        try {
            return Settings.Global.getInt(this.mCr, "auto_time") > 0;
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
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

    public void setDeviceTime(long time) {
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIME");
        intent.addFlags(536870912);
        intent.putExtra("time", time);
        this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
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

    public static void setDeviceTimeZoneStaticHw(Context context, String zoneId) {
        setDeviceTimeZoneStatic(context, zoneId);
    }
}
