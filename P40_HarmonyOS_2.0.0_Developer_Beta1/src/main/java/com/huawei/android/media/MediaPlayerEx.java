package com.huawei.android.media;

import android.media.MediaPlayer;

public class MediaPlayerEx {
    public static void startWithVibrate(MediaPlayer mp, String effectType) {
        if (mp != null) {
            mp.startWithVibrate(effectType);
        }
    }

    public static void startWithVibrate(MediaPlayer mp, String effectType, String packageName, int uid) {
        if (mp != null) {
            mp.startWithVibrate(effectType, packageName, uid);
        }
    }
}
