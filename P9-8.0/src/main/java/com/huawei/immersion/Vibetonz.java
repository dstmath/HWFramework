package com.huawei.immersion;

import android.content.Context;
import android.net.Uri;
import android.os.SystemProperties;
import android.provider.Settings.System;
import huawei.android.os.HwGeneralManager;

public class Vibetonz {
    public static final int HAPTIC_ALARM = 1;
    public static final int HAPTIC_EVENT_BULK_MOVE_ICONDROP = 900;
    public static final int HAPTIC_EVENT_BULK_MOVE_ICONPICKUP = 1100;
    public static final int HAPTIC_EVENT_BULK_MOVE_ICON_GATHER = 1000;
    public static final int HAPTIC_EVENT_CONTACT_ALPHA_SWITCH = 1600;
    public static final int HAPTIC_EVENT_COUNTDOWN_SWING = 100;
    public static final int HAPTIC_EVENT_FM_ADJUST = 500;
    public static final int HAPTIC_EVENT_FM_ADJUST_DONE = 700;
    public static final int HAPTIC_EVENT_FM_SPIN = 600;
    public static final int HAPTIC_EVENT_HOMESCREEN_ICON_FLY_WORKSPACE = 800;
    public static final int HAPTIC_EVENT_HOMESCREEN_SHAKE_ALIGN = 1200;
    public static final int HAPTIC_EVENT_LOCKSCREEN_UNLOCK = 2500;
    public static final int HAPTIC_EVENT_LONG_PRESS = 1300;
    public static final int HAPTIC_EVENT_LONG_PRESS_WORKSPACE = 1400;
    public static final int HAPTIC_EVENT_NUMBERPICKER_ITEMSCROLL = 300;
    public static final int HAPTIC_EVENT_NUMBERPICKER_TUNER = 400;
    public static final int HAPTIC_EVENT_SCROLL_INDICATOR_POP = 1500;
    public static final int HAPTIC_EVENT_TEXTVIEW_DOUBLE_TAP_SELECTWORD = 1800;
    public static final int HAPTIC_EVENT_TEXTVIEW_SELECTCHAR = 1700;
    public static final int HAPTIC_EVENT_TEXTVIEW_SETCURSOR = 2000;
    public static final int HAPTIC_EVENT_TEXTVIEW_TAPWORD = 1900;
    public static final int HAPTIC_EVENT_TIMING_ROTATE = 200;
    public static final int HAPTIC_EVENT_VIRTUAL_KEY = 2600;
    public static final int HAPTIC_EVENT_WEATHER_RAIN = 2100;
    public static final int HAPTIC_EVENT_WEATHER_SAND = 2200;
    public static final int HAPTIC_EVENT_WEATHER_THUNDERSTORM = 2300;
    public static final int HAPTIC_EVENT_WEATHER_WINDY = 2400;
    public static final int HAPTIC_NOTIFICATION = 3;
    public static final int HAPTIC_RINGTONE = 2;
    private static boolean mIsVibrateImplemented = SystemProperties.getBoolean("ro.config.touch_vibrate", false);
    private static Vibetonz mVibetonz;

    private Vibetonz() {
    }

    public static boolean isVibrateOn(Context mContext) {
        boolean z = true;
        if (mContext == null) {
            return false;
        }
        if (!mIsVibrateImplemented) {
            z = false;
        } else if (1 != System.getInt(mContext.getContentResolver(), "touch_vibrate_mode", 1)) {
            z = false;
        }
        return z;
    }

    public static Vibetonz getInstance() {
        if (mIsVibrateImplemented && mVibetonz == null) {
            mVibetonz = new Vibetonz();
        }
        return mVibetonz;
    }

    public void playIvtEffect(int effectNo) {
        HwGeneralManager.getInstance().playIvtEffect(effectNo);
    }

    public void stopPlayEffect() {
        HwGeneralManager.getInstance().stopPlayEffect();
    }

    public void pausePlayEffect(int effectNo) {
        HwGeneralManager.getInstance().pausePlayEffect(effectNo);
    }

    public void resumePausedEffect(int effectNo) {
        HwGeneralManager.getInstance().resumePausedEffect(effectNo);
    }

    public boolean isPlaying(int effectNo) {
        return HwGeneralManager.getInstance().isPlaying(effectNo);
    }

    public boolean startHaptic(Context mContext, int callerID, int ringtoneType, Uri uri) {
        return HwGeneralManager.getInstance().startHaptic(mContext, callerID, ringtoneType, uri);
    }

    public boolean hasHaptic(Context mContext, Uri uri) {
        return HwGeneralManager.getInstance().hasHaptic(mContext, uri);
    }

    public void stopHaptic() {
        HwGeneralManager.getInstance().stopHaptic();
    }
}
