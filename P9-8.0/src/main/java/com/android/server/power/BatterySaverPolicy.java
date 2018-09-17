package com.android.server.power;

import android.annotation.IntDef;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerSaveState;
import android.os.PowerSaveState.Builder;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.util.KeyValueListParser;
import android.util.Slog;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BatterySaverPolicy extends ContentObserver {
    public static final int GPS_MODE_DISABLED_WHEN_SCREEN_OFF = 1;
    public static final int GPS_MODE_NO_CHANGE = 0;
    private static final String KEY_ADJUST_BRIGHTNESS_DISABLED = "adjust_brightness_disabled";
    private static final String KEY_ADJUST_BRIGHTNESS_FACTOR = "adjust_brightness_factor";
    private static final String KEY_ANIMATION_DISABLED = "animation_disabled";
    private static final String KEY_DATASAVER_DISABLED = "datasaver_disabled";
    private static final String KEY_FIREWALL_DISABLED = "firewall_disabled";
    private static final String KEY_FULLBACKUP_DEFERRED = "fullbackup_deferred";
    private static final String KEY_GPS_MODE = "gps_mode";
    private static final String KEY_KEYVALUE_DEFERRED = "keyvaluebackup_deferred";
    private static final String KEY_SOUNDTRIGGER_DISABLED = "soundtrigger_disabled";
    private static final String KEY_VIBRATION_DISABLED = "vibration_disabled";
    public static final String SECURE_KEY_GPS_MODE = "batterySaverGpsMode";
    private static final String TAG = "BatterySaverPolicy";
    private boolean mAdjustBrightnessDisabled;
    private float mAdjustBrightnessFactor;
    private boolean mAnimationDisabled;
    private ContentResolver mContentResolver;
    private boolean mDataSaverDisabled;
    private boolean mFireWallDisabled;
    private boolean mFullBackupDeferred;
    private int mGpsMode;
    private boolean mKeyValueBackupDeferred;
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    private boolean mSoundTriggerDisabled;
    private boolean mVibrationDisabled;

    @IntDef({1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ServiceType {
        public static final int ANIMATION = 3;
        public static final int BATTERY_STATS = 9;
        public static final int DATA_SAVER = 10;
        public static final int FULL_BACKUP = 4;
        public static final int GPS = 1;
        public static final int KEYVALUE_BACKUP = 5;
        public static final int NETWORK_FIREWALL = 6;
        public static final int NULL = 0;
        public static final int SCREEN_BRIGHTNESS = 7;
        public static final int SOUND = 8;
        public static final int VIBRATION = 2;
    }

    public BatterySaverPolicy(Handler handler) {
        super(handler);
    }

    public void start(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
        this.mContentResolver.registerContentObserver(Global.getUriFor("battery_saver_constants"), false, this);
        onChange(true, null);
    }

    public void onChange(boolean selfChange, Uri uri) {
        updateConstants(Global.getString(this.mContentResolver, "battery_saver_constants"));
    }

    void updateConstants(String value) {
        synchronized (this) {
            try {
                this.mParser.setString(value);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "Bad battery saver constants");
            }
            this.mVibrationDisabled = this.mParser.getBoolean(KEY_VIBRATION_DISABLED, true);
            this.mAnimationDisabled = this.mParser.getBoolean(KEY_ANIMATION_DISABLED, true);
            this.mSoundTriggerDisabled = this.mParser.getBoolean(KEY_SOUNDTRIGGER_DISABLED, true);
            this.mFullBackupDeferred = this.mParser.getBoolean(KEY_FULLBACKUP_DEFERRED, true);
            this.mKeyValueBackupDeferred = this.mParser.getBoolean(KEY_KEYVALUE_DEFERRED, true);
            this.mFireWallDisabled = this.mParser.getBoolean(KEY_FIREWALL_DISABLED, false);
            this.mAdjustBrightnessDisabled = this.mParser.getBoolean(KEY_ADJUST_BRIGHTNESS_DISABLED, false);
            this.mAdjustBrightnessFactor = this.mParser.getFloat(KEY_ADJUST_BRIGHTNESS_FACTOR, 0.5f);
            this.mDataSaverDisabled = this.mParser.getBoolean(KEY_DATASAVER_DISABLED, true);
            this.mGpsMode = this.mParser.getInt(KEY_GPS_MODE, Secure.getInt(this.mContentResolver, SECURE_KEY_GPS_MODE, 1));
        }
        return;
    }

    public PowerSaveState getBatterySaverPolicy(int type, boolean realMode) {
        synchronized (this) {
            Builder builder = new Builder().setGlobalBatterySaverEnabled(realMode);
            PowerSaveState build;
            if (realMode) {
                switch (type) {
                    case 1:
                        build = builder.setBatterySaverEnabled(realMode).setGpsMode(this.mGpsMode).build();
                        return build;
                    case 2:
                        build = builder.setBatterySaverEnabled(this.mVibrationDisabled).build();
                        return build;
                    case 3:
                        build = builder.setBatterySaverEnabled(this.mAnimationDisabled).build();
                        return build;
                    case 4:
                        build = builder.setBatterySaverEnabled(this.mFullBackupDeferred).build();
                        return build;
                    case 5:
                        build = builder.setBatterySaverEnabled(this.mKeyValueBackupDeferred).build();
                        return build;
                    case 6:
                        build = builder.setBatterySaverEnabled(this.mFireWallDisabled ^ 1).build();
                        return build;
                    case 7:
                        build = builder.setBatterySaverEnabled(this.mAdjustBrightnessDisabled ^ 1).setBrightnessFactor(this.mAdjustBrightnessFactor).build();
                        return build;
                    case 8:
                        build = builder.setBatterySaverEnabled(this.mSoundTriggerDisabled).build();
                        return build;
                    case 10:
                        build = builder.setBatterySaverEnabled(this.mDataSaverDisabled ^ 1).build();
                        return build;
                    default:
                        build = builder.setBatterySaverEnabled(realMode).build();
                        return build;
                }
            } else {
                build = builder.setBatterySaverEnabled(realMode).build();
                return build;
            }
        }
    }

    public void dump(PrintWriter pw) {
        pw.println();
        pw.println("Battery saver policy");
        pw.println("  Settings battery_saver_constants");
        pw.println("  value: " + Global.getString(this.mContentResolver, "battery_saver_constants"));
        pw.println();
        pw.println("  vibration_disabled=" + this.mVibrationDisabled);
        pw.println("  animation_disabled=" + this.mAnimationDisabled);
        pw.println("  fullbackup_deferred=" + this.mFullBackupDeferred);
        pw.println("  keyvaluebackup_deferred=" + this.mKeyValueBackupDeferred);
        pw.println("  firewall_disabled=" + this.mFireWallDisabled);
        pw.println("  datasaver_disabled=" + this.mDataSaverDisabled);
        pw.println("  adjust_brightness_disabled=" + this.mAdjustBrightnessDisabled);
        pw.println("  adjust_brightness_factor=" + this.mAdjustBrightnessFactor);
        pw.println("  gps_mode=" + this.mGpsMode);
    }
}
