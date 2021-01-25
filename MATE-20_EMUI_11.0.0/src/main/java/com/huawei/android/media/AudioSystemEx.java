package com.huawei.android.media;

import android.media.AudioSystem;
import android.os.Binder;
import com.huawei.annotation.HwSystemApi;

public class AudioSystemEx {
    @HwSystemApi
    public static final int AUDIO_FORMAT_DEFAULT = 0;
    private static final int BAD_VALUE = -2;
    @HwSystemApi
    public static final int DEVICE_OUT_FM = 1048576;
    @HwSystemApi
    public static final int DEVICE_STATE_AVAILABLE = 1;
    @HwSystemApi
    public static final int DEVICE_STATE_UNAVAILABLE = 0;
    public static final int FORCE_SPEAKER = 1;
    public static final int FOR_EXTEND_MEDIA = 9;
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

    @HwSystemApi
    public static int setDeviceConnectionState(int device, int state, String deviceAddress, String deviceName, int codecFormat) {
        return AudioSystem.setDeviceConnectionState(device, state, deviceAddress, deviceName, codecFormat);
    }
}
