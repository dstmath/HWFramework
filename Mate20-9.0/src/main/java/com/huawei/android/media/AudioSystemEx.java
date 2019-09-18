package com.huawei.android.media;

import android.media.AudioSystem;
import android.os.Binder;

public class AudioSystemEx {
    private static final int BAD_VALUE = -2;
    public static final int FORCE_SPEAKER = 1;
    public static final int FOR_MEDIA = 1;
    public static final int STREAM_ALARM = 4;
    public static final int STREAM_FM = 10;
    public static final int STREAM_VOICE_HELPER = 11;
    private static final int SYSTEM_UID = 1000;

    public static int setForceUse(int usage, int config) {
        Binder.clearCallingIdentity();
        if (Binder.getCallingUid() == 1000) {
            return AudioSystem.setForceUse(usage, config);
        }
        return -2;
    }

    public static String getParameters(String keys) {
        return AudioSystem.getParameters(keys);
    }

    public static int setParameters(String keyValuePairs) {
        return AudioSystem.setParameters(keyValuePairs);
    }
}
