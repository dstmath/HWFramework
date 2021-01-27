package com.huawei.aod;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;

public class DisplayTypeConfig {
    private static final String AOD_DISPLAY_MODE = "aod_display_mode";
    private static final String AOD_DISPLAY_TYPE = "aod_display_type";
    private static final int AOD_GLOBAL_SWITCH_OPEN = 1;
    private static final String AOD_PACKAGE_NAME = "com.huawei.aod";
    private static final String AOD_SCHEDULED_SWITCH = "aod_scheduled_switch";
    private static final String AOD_SWITCH = "aod_switch";
    private static final int AOD_SWITCH_USER_DEPRECATE = -1;
    private static final int AOD_TIMER_SWITCH_OPEN = 1;
    private static final String AOD_TOUCH_TIME = "aod_touch_time";
    private static final int AOD_TOUCH_TIME_INDEX = 1;
    private static final int DISPLAY_MODE_TYPE_INDEX = 0;
    private static final String DISPLAY_STYLE_CONFIG = "hw_mc.aod.support_display_style";
    private static final String EYE_GLANCE_TIME = "eye_glance_time";
    private static final String FINGERPRINT_TOUCH_TIME = "fingerprint_touch_time";
    private static final int FINGER_PRINT_TOUCH_TIME_INDEX = 2;
    private static final int GLANCE_REMOVE_TIMEOUT_INDEX = 3;
    private static final int LMT_CONFIG_LENGTH = 3;
    private static final int LMT_MODE_MOVE = 1;
    public static final int LMT_MODE_SWING = 4;
    private static final int LMT_MODE_TOUCH = 2;
    private static final String TAG = DisplayTypeConfig.class.getSimpleName();
    private static final int TAP_TO_SHOW = 0;
    private static final int USER_UNSET = -2;
    private static int sAodTouchTime = 0;
    private static int sDisplayModeType = 0;
    private static int sFingerprintTouchTime = 0;
    private static int sGlanceProtectTime = 0;
    private static DisplayTypeConfig sInstance = null;
    private AwarenessClient mAodAwarenessClient = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.huawei.aod.DisplayTypeConfig.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.w(DisplayTypeConfig.TAG, "onReceive, the context or intent is null.");
                return;
            }
            String action = intent.getAction();
            String str = DisplayTypeConfig.TAG;
            Slog.w(str, "mBroadcastReceiver receive action : " + action);
            if (!"android.intent.action.BOOT_COMPLETED".equals(action)) {
                Slog.i(DisplayTypeConfig.TAG, " mBroadcastReceiver default branch");
            } else if ((DisplayTypeConfig.this.getDisplayType() & 4) == 4) {
                DisplayTypeConfig.this.mAodAwarenessClient.startAwarenessManager();
            }
        }
    };
    private Context mContext = null;
    private ContentObserver mDisplayTypeObserver = new ContentObserver(new Handler()) {
        /* class com.huawei.aod.DisplayTypeConfig.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange) {
            int aodDisplayType = DisplayTypeConfig.this.getDisplayType();
            String str = DisplayTypeConfig.TAG;
            Slog.i(str, "display type onChange, aodDisplayType:" + aodDisplayType);
            if ((aodDisplayType & 4) == 4) {
                DisplayTypeConfig.this.mAodAwarenessClient.startAwarenessManager();
            } else {
                DisplayTypeConfig.this.mAodAwarenessClient.stopAwarenessManager();
            }
        }
    };

    private DisplayTypeConfig(Context context) {
        this.mContext = context.getApplicationContext();
        if (this.mContext == null) {
            this.mContext = context;
        }
    }

    public static synchronized DisplayTypeConfig getInstance(Context context) {
        DisplayTypeConfig displayTypeConfig;
        synchronized (DisplayTypeConfig.class) {
            if (sInstance == null) {
                sInstance = new DisplayTypeConfig(context);
            }
            displayTypeConfig = sInstance;
        }
        return displayTypeConfig;
    }

    public void setLmtEnable() {
        parseDisplayStyleConfig();
        if (isConfigMatchMode(sDisplayModeType, 4)) {
            this.mAodAwarenessClient = new AwarenessClient(this.mContext);
            Slog.i(TAG, "setLmtEnable: registerContentObserver AOD_DISPLAY_TYPE");
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(AOD_DISPLAY_TYPE), false, this.mDisplayTypeObserver);
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BOOT_COMPLETED");
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        }
        if (supportIntelligentAod() && sAodTouchTime > 0 && sFingerprintTouchTime > 0) {
            setConfigValue();
        }
        updateSettingsByConfiguration();
    }

    private void parseDisplayStyleConfig() {
        String[] configs = SystemProperties.get(DISPLAY_STYLE_CONFIG, "").split(",");
        if (configs.length >= 3) {
            try {
                sDisplayModeType = Integer.parseInt(configs[0]);
                sAodTouchTime = Integer.parseInt(configs[1]);
                sFingerprintTouchTime = Integer.parseInt(configs[2]);
                if (configs.length > 3) {
                    sGlanceProtectTime = Integer.parseInt(configs[3]);
                }
            } catch (NumberFormatException e) {
                Slog.e(TAG, "hw_mc.aod.support_display_style contains invalid item.");
                sDisplayModeType = 0;
                sAodTouchTime = 0;
                sFingerprintTouchTime = 0;
                sGlanceProtectTime = 0;
            }
        }
    }

    private boolean supportIntelligentAod() {
        if (isConfigMatchMode(sDisplayModeType, 1) || isConfigMatchMode(sDisplayModeType, 2) || isConfigMatchMode(sDisplayModeType, 4)) {
            return true;
        }
        return false;
    }

    private void setConfigValue() {
        Context aodContext = getAodApkContext();
        if (aodContext == null) {
            Slog.w(TAG, "can not set config value as aodContext is null");
            return;
        }
        int aodDisplayMode = getAodApkSettings(aodContext, AOD_DISPLAY_MODE);
        int aodSwitch = getAodApkSettings(aodContext, AOD_SWITCH);
        int aodScheduledSwitch = getAodApkSettings(aodContext, AOD_SCHEDULED_SWITCH);
        if (aodDisplayMode == -2 && aodSwitch == -2 && aodScheduledSwitch == -2) {
            if (AodConst.DISPLAY_DEFAULT_CONFIG != 0) {
                setAodApkSettings(aodContext, AOD_SWITCH, 1);
                setAodApkSettings(aodContext, AOD_SCHEDULED_SWITCH, -1);
                setAodApkSettings(aodContext, AOD_DISPLAY_MODE, 0);
            }
            setAodApkSettings(aodContext, AOD_DISPLAY_TYPE, sDisplayModeType);
            setAodApkSettings(aodContext, AOD_TOUCH_TIME, sAodTouchTime);
            setAodApkSettings(aodContext, FINGERPRINT_TOUCH_TIME, sFingerprintTouchTime);
            setAodApkSettings(aodContext, EYE_GLANCE_TIME, sGlanceProtectTime);
            String str = TAG;
            Slog.i(str, "init change of global switch  = " + getAodApkSettings(aodContext, AOD_SWITCH));
        }
    }

    private void updateSettingsByConfiguration() {
        Context aodContext = getAodApkContext();
        if (aodContext == null) {
            Slog.w(TAG, "can not set config value as aodContext is null");
            return;
        }
        int displayTypeSettings = getAodApkSettings(aodContext, AOD_DISPLAY_TYPE);
        if (!(displayTypeSettings == -2 || displayTypeSettings == 0 || displayTypeSettings == sDisplayModeType)) {
            String str = TAG;
            Slog.i(str, "match display type settings to configuration:" + sDisplayModeType);
            setAodApkSettings(aodContext, AOD_DISPLAY_TYPE, sDisplayModeType);
        }
        if (!(displayTypeSettings == -2 || displayTypeSettings == 0 || sAodTouchTime == getAodApkSettings(aodContext, AOD_TOUCH_TIME))) {
            String str2 = TAG;
            Slog.i(str2, "match aod touch display time settings to configuration:" + sAodTouchTime);
            setAodApkSettings(aodContext, AOD_TOUCH_TIME, sAodTouchTime);
        }
        if (!(displayTypeSettings == -2 || displayTypeSettings == 0 || sFingerprintTouchTime == getAodApkSettings(aodContext, FINGERPRINT_TOUCH_TIME))) {
            String str3 = TAG;
            Slog.i(str3, "match finger print touch display time settings to configuration:" + sFingerprintTouchTime);
            setAodApkSettings(aodContext, FINGERPRINT_TOUCH_TIME, sFingerprintTouchTime);
        }
        if (displayTypeSettings != -2 && displayTypeSettings != 0 && sGlanceProtectTime != getAodApkSettings(aodContext, EYE_GLANCE_TIME)) {
            String str4 = TAG;
            Slog.i(str4, "match eye glance display protect time settings to configuration:" + sGlanceProtectTime);
            setAodApkSettings(aodContext, EYE_GLANCE_TIME, sGlanceProtectTime);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getDisplayType() {
        Context aodContext = getAodApkContext();
        if (aodContext != null) {
            return Settings.Secure.getIntForUser(aodContext.getContentResolver(), AOD_DISPLAY_TYPE, 0, ActivityManager.getCurrentUser());
        }
        Slog.w(TAG, "can not set config value as aodContext is null");
        return 0;
    }

    private int getAodApkSettings(Context aodContext, String settingsType) {
        return Settings.Secure.getIntForUser(aodContext.getContentResolver(), settingsType, -2, ActivityManager.getCurrentUser());
    }

    private void setAodApkSettings(Context aodContext, String settingsType, int value) {
        Settings.Secure.putIntForUser(aodContext.getContentResolver(), settingsType, value, ActivityManager.getCurrentUser());
    }

    private Context getAodApkContext() {
        try {
            return this.mContext.createPackageContext(AOD_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            String str = TAG;
            Slog.w(str, "package not exist " + e.toString());
            return null;
        }
    }

    private static boolean isConfigMatchMode(int displayModeType, int mode) {
        return (displayModeType & mode) == mode;
    }
}
