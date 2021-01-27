package com.huawei.android.media;

import android.media.Ringtone;

public class RingtoneEx {
    public static void playWithVibrate(Ringtone rt, String effectType) {
        if (rt != null) {
            rt.playWithVibrate(effectType);
        }
    }

    public static void playWithVibrate(Ringtone rt, String effectType, String packageName, int uid) {
        if (rt != null) {
            rt.playWithVibrate(effectType, packageName, uid);
        }
    }
}
