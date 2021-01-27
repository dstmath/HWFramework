package com.huawei.android.media;

import android.media.AudioPlaybackConfiguration;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class AudioPlaybackConfigurationEx {
    public static boolean isActive(AudioPlaybackConfiguration config) {
        if (config == null) {
            return false;
        }
        return config.isActive();
    }
}
