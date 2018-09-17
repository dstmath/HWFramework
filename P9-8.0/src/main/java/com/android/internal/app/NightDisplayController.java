package com.android.internal.app;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.util.Slog;
import com.android.internal.R;
import java.util.Calendar;
import java.util.Locale;

public final class NightDisplayController {
    public static final int AUTO_MODE_CUSTOM = 1;
    public static final int AUTO_MODE_DISABLED = 0;
    public static final int AUTO_MODE_TWILIGHT = 2;
    private static final boolean DEBUG = false;
    private static final String TAG = "NightDisplayController";
    private Callback mCallback;
    private final ContentObserver mContentObserver;
    private final Context mContext;
    private final int mUserId;

    public interface Callback {
        void onActivated(boolean activated) {
        }

        void onAutoModeChanged(int autoMode) {
        }

        void onCustomStartTimeChanged(LocalTime startTime) {
        }

        void onCustomEndTimeChanged(LocalTime endTime) {
        }

        void onColorTemperatureChanged(int colorTemperature) {
        }
    }

    public static class LocalTime {
        public final int hourOfDay;
        public final int minute;

        public LocalTime(int hourOfDay, int minute) {
            if (hourOfDay < 0 || hourOfDay > 23) {
                throw new IllegalArgumentException("Invalid hourOfDay: " + hourOfDay);
            } else if (minute < 0 || minute > 59) {
                throw new IllegalArgumentException("Invalid minute: " + minute);
            } else {
                this.hourOfDay = hourOfDay;
                this.minute = minute;
            }
        }

        public Calendar getDateTimeBefore(Calendar time) {
            Calendar c = Calendar.getInstance();
            c.set(1, time.get(1));
            c.set(6, time.get(6));
            c.set(11, this.hourOfDay);
            c.set(12, this.minute);
            c.set(13, 0);
            c.set(14, 0);
            if (c.after(time)) {
                c.add(5, -1);
            }
            return c;
        }

        public Calendar getDateTimeAfter(Calendar time) {
            Calendar c = Calendar.getInstance();
            c.set(1, time.get(1));
            c.set(6, time.get(6));
            c.set(11, this.hourOfDay);
            c.set(12, this.minute);
            c.set(13, 0);
            c.set(14, 0);
            if (c.before(time)) {
                c.add(5, 1);
            }
            return c;
        }

        private static LocalTime valueOf(int millis) {
            return new LocalTime((millis / 3600000) % 24, (millis / 60000) % 60);
        }

        private int toMillis() {
            return (this.hourOfDay * 3600000) + (this.minute * 60000);
        }

        public String toString() {
            return String.format(Locale.US, "%02d:%02d", new Object[]{Integer.valueOf(this.hourOfDay), Integer.valueOf(this.minute)});
        }
    }

    public NightDisplayController(Context context) {
        this(context, ActivityManager.getCurrentUser());
    }

    public NightDisplayController(Context context, int userId) {
        this.mContext = context.getApplicationContext();
        this.mUserId = userId;
        this.mContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                String setting = uri == null ? null : uri.getLastPathSegment();
                if (setting != null) {
                    NightDisplayController.this.onSettingChanged(setting);
                }
            }
        };
    }

    public boolean isActivated() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_ACTIVATED, 0, this.mUserId) == 1;
    }

    public boolean setActivated(boolean activated) {
        if (isActivated() != activated) {
            Secure.putLongForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_LAST_ACTIVATED_TIME, System.currentTimeMillis(), this.mUserId);
        }
        return Secure.putIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_ACTIVATED, activated ? 1 : 0, this.mUserId);
    }

    public Calendar getLastActivatedTime() {
        long lastActivatedTimeMillis = Secure.getLongForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_LAST_ACTIVATED_TIME, -1, this.mUserId);
        if (lastActivatedTimeMillis < 0) {
            return null;
        }
        Calendar lastActivatedTime = Calendar.getInstance();
        lastActivatedTime.setTimeInMillis(lastActivatedTimeMillis);
        return lastActivatedTime;
    }

    public int getAutoMode() {
        int autoMode = Secure.getIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_AUTO_MODE, -1, this.mUserId);
        if (autoMode == -1) {
            autoMode = this.mContext.getResources().getInteger(R.integer.config_defaultNightDisplayAutoMode);
        }
        if (autoMode == 0 || autoMode == 1 || autoMode == 2) {
            return autoMode;
        }
        Slog.e(TAG, "Invalid autoMode: " + autoMode);
        return 0;
    }

    public boolean setAutoMode(int autoMode) {
        if (autoMode == 0 || autoMode == 1 || autoMode == 2) {
            return Secure.putIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_AUTO_MODE, autoMode, this.mUserId);
        }
        throw new IllegalArgumentException("Invalid autoMode: " + autoMode);
    }

    public LocalTime getCustomStartTime() {
        int startTimeValue = Secure.getIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_CUSTOM_START_TIME, -1, this.mUserId);
        if (startTimeValue == -1) {
            startTimeValue = this.mContext.getResources().getInteger(R.integer.config_defaultNightDisplayCustomStartTime);
        }
        return LocalTime.valueOf(startTimeValue);
    }

    public boolean setCustomStartTime(LocalTime startTime) {
        if (startTime != null) {
            return Secure.putIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_CUSTOM_START_TIME, startTime.toMillis(), this.mUserId);
        }
        throw new IllegalArgumentException("startTime cannot be null");
    }

    public LocalTime getCustomEndTime() {
        int endTimeValue = Secure.getIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_CUSTOM_END_TIME, -1, this.mUserId);
        if (endTimeValue == -1) {
            endTimeValue = this.mContext.getResources().getInteger(R.integer.config_defaultNightDisplayCustomEndTime);
        }
        return LocalTime.valueOf(endTimeValue);
    }

    public boolean setCustomEndTime(LocalTime endTime) {
        if (endTime != null) {
            return Secure.putIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_CUSTOM_END_TIME, endTime.toMillis(), this.mUserId);
        }
        throw new IllegalArgumentException("endTime cannot be null");
    }

    public int getColorTemperature() {
        int colorTemperature = Secure.getIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_COLOR_TEMPERATURE, -1, this.mUserId);
        if (colorTemperature == -1) {
            colorTemperature = getDefaultColorTemperature();
        }
        int minimumTemperature = getMinimumColorTemperature();
        int maximumTemperature = getMaximumColorTemperature();
        if (colorTemperature < minimumTemperature) {
            return minimumTemperature;
        }
        if (colorTemperature > maximumTemperature) {
            return maximumTemperature;
        }
        return colorTemperature;
    }

    public boolean setColorTemperature(int colorTemperature) {
        return Secure.putIntForUser(this.mContext.getContentResolver(), Secure.NIGHT_DISPLAY_COLOR_TEMPERATURE, colorTemperature, this.mUserId);
    }

    public int getMinimumColorTemperature() {
        return this.mContext.getResources().getInteger(R.integer.config_nightDisplayColorTemperatureMin);
    }

    public int getMaximumColorTemperature() {
        return this.mContext.getResources().getInteger(R.integer.config_nightDisplayColorTemperatureMax);
    }

    public int getDefaultColorTemperature() {
        return this.mContext.getResources().getInteger(R.integer.config_nightDisplayColorTemperatureDefault);
    }

    private void onSettingChanged(String setting) {
        if (this.mCallback == null) {
            return;
        }
        if (setting.equals(Secure.NIGHT_DISPLAY_ACTIVATED)) {
            this.mCallback.onActivated(isActivated());
        } else if (setting.equals(Secure.NIGHT_DISPLAY_AUTO_MODE)) {
            this.mCallback.onAutoModeChanged(getAutoMode());
        } else if (setting.equals(Secure.NIGHT_DISPLAY_CUSTOM_START_TIME)) {
            this.mCallback.onCustomStartTimeChanged(getCustomStartTime());
        } else if (setting.equals(Secure.NIGHT_DISPLAY_CUSTOM_END_TIME)) {
            this.mCallback.onCustomEndTimeChanged(getCustomEndTime());
        } else if (setting.equals(Secure.NIGHT_DISPLAY_COLOR_TEMPERATURE)) {
            this.mCallback.onColorTemperatureChanged(getColorTemperature());
        }
    }

    public void setListener(Callback callback) {
        Callback oldCallback = this.mCallback;
        if (oldCallback != callback) {
            this.mCallback = callback;
            if (callback == null) {
                this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
            } else if (oldCallback == null) {
                ContentResolver cr = this.mContext.getContentResolver();
                cr.registerContentObserver(Secure.getUriFor(Secure.NIGHT_DISPLAY_ACTIVATED), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Secure.getUriFor(Secure.NIGHT_DISPLAY_AUTO_MODE), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Secure.getUriFor(Secure.NIGHT_DISPLAY_CUSTOM_START_TIME), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Secure.getUriFor(Secure.NIGHT_DISPLAY_CUSTOM_END_TIME), false, this.mContentObserver, this.mUserId);
                cr.registerContentObserver(Secure.getUriFor(Secure.NIGHT_DISPLAY_COLOR_TEMPERATURE), false, this.mContentObserver, this.mUserId);
            }
        }
    }

    public static boolean isAvailable(Context context) {
        return context.getResources().getBoolean(R.bool.config_nightDisplayAvailable);
    }
}
