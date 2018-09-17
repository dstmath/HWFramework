package android.media;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.audiopolicy.AudioMix;
import android.net.ProxyInfo;
import android.os.SystemProperties;
import android.util.Log;
import java.util.ArrayList;

public class AudioSystem {
    public static final int AUDIO_DEVICE_IN_USB_DEVICE_EXTENDED = -2013265920;
    public static final int AUDIO_DEVICE_OUT_USB_DEVICE_EXTENDED = 536870912;
    public static final int AUDIO_HW_SYNC_INVALID = 0;
    public static final int AUDIO_SESSION_ALLOCATE = 0;
    public static final int AUDIO_STATUS_ERROR = 1;
    public static final int AUDIO_STATUS_OK = 0;
    public static final int AUDIO_STATUS_SERVER_DIED = 100;
    public static final int BAD_VALUE = -2;
    public static final int DEAD_OBJECT = -6;
    public static final int DEFAULT_MUTE_STREAMS_AFFECTED = 46;
    public static int[] DEFAULT_STREAM_VOLUME = null;
    public static final int DEVICE_ALL_HDMI_SYSTEM_AUDIO_AND_SPEAKER = 2883586;
    public static final int DEVICE_BIT_DEFAULT = 1073741824;
    public static final int DEVICE_BIT_IN = Integer.MIN_VALUE;
    public static final int DEVICE_IN_ALL = -887095297;
    public static final int DEVICE_IN_ALL_SCO = -2147483640;
    public static final int DEVICE_IN_ALL_USB = -2013259776;
    public static final int DEVICE_IN_AMBIENT = -2147483646;
    public static final String DEVICE_IN_AMBIENT_NAME = "ambient";
    public static final int DEVICE_IN_ANLG_DOCK_HEADSET = -2147483136;
    public static final String DEVICE_IN_ANLG_DOCK_HEADSET_NAME = "analog_dock";
    public static final int DEVICE_IN_AUX_DIGITAL = -2147483616;
    public static final String DEVICE_IN_AUX_DIGITAL_NAME = "aux_digital";
    public static final int DEVICE_IN_BACK_MIC = -2147483520;
    public static final String DEVICE_IN_BACK_MIC_NAME = "back_mic";
    public static final int DEVICE_IN_BLUETOOTH_A2DP = -2147352576;
    public static final String DEVICE_IN_BLUETOOTH_A2DP_NAME = "bt_a2dp";
    public static final int DEVICE_IN_BLUETOOTH_SCO_HEADSET = -2147483640;
    public static final String DEVICE_IN_BLUETOOTH_SCO_HEADSET_NAME = "bt_sco_hs";
    public static final int DEVICE_IN_BUILTIN_MIC = -2147483644;
    public static final String DEVICE_IN_BUILTIN_MIC_NAME = "mic";
    public static final int DEVICE_IN_BUS = -2146435072;
    public static final String DEVICE_IN_BUS_NAME = "bus";
    public static final int DEVICE_IN_COMMUNICATION = -2147483647;
    public static final String DEVICE_IN_COMMUNICATION_NAME = "communication";
    public static final int DEVICE_IN_DEFAULT = -1073741824;
    public static final int DEVICE_IN_DGTL_DOCK_HEADSET = -2147482624;
    public static final String DEVICE_IN_DGTL_DOCK_HEADSET_NAME = "digital_dock";
    public static final int DEVICE_IN_FM_TUNER = -2147475456;
    public static final String DEVICE_IN_FM_TUNER_NAME = "fm_tuner";
    public static final int DEVICE_IN_HDMI = -2147483616;
    public static final int DEVICE_IN_IP = -2146959360;
    public static final String DEVICE_IN_IP_NAME = "ip";
    public static final int DEVICE_IN_LINE = -2147450880;
    public static final String DEVICE_IN_LINE_NAME = "line";
    public static final int DEVICE_IN_LOOPBACK = -2147221504;
    public static final String DEVICE_IN_LOOPBACK_NAME = "loopback";
    public static final int DEVICE_IN_PROXY = -2130706432;
    public static final String DEVICE_IN_PROXY_NAME = "proxy";
    public static final int DEVICE_IN_REMOTE_SUBMIX = -2147483392;
    public static final String DEVICE_IN_REMOTE_SUBMIX_NAME = "remote_submix";
    public static final int DEVICE_IN_SPDIF = -2147418112;
    public static final String DEVICE_IN_SPDIF_NAME = "spdif";
    public static final int DEVICE_IN_TELEPHONY_RX = -2147483584;
    public static final String DEVICE_IN_TELEPHONY_RX_NAME = "telephony_rx";
    public static final int DEVICE_IN_TV_TUNER = -2147467264;
    public static final String DEVICE_IN_TV_TUNER_NAME = "tv_tuner";
    public static final int DEVICE_IN_USB_ACCESSORY = -2147481600;
    public static final String DEVICE_IN_USB_ACCESSORY_NAME = "usb_accessory";
    public static final int DEVICE_IN_USB_DEVICE = -2147479552;
    public static final String DEVICE_IN_USB_DEVICE_NAME = "usb_device";
    public static final int DEVICE_IN_USB_HEADSET = -2113929216;
    public static final String DEVICE_IN_USB_HEADSET_NAME = "usb_headset";
    public static final int DEVICE_IN_VOICE_CALL = -2147483584;
    public static final int DEVICE_IN_WIRED_HEADSET = -2147483632;
    public static final String DEVICE_IN_WIRED_HEADSET_NAME = "headset";
    public static final int DEVICE_NONE = 0;
    public static final int DEVICE_OUT_ALL = 1744830463;
    public static final int DEVICE_OUT_ALL_A2DP = 896;
    public static final int DEVICE_OUT_ALL_HDMI_SYSTEM_AUDIO = 2883584;
    public static final int DEVICE_OUT_ALL_SCO = 112;
    public static final int DEVICE_OUT_ALL_USB = 536895488;
    public static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 2048;
    public static final String DEVICE_OUT_ANLG_DOCK_HEADSET_NAME = "analog_dock";
    public static final int DEVICE_OUT_AUX_DIGITAL = 1024;
    public static final String DEVICE_OUT_AUX_DIGITAL_NAME = "aux_digital";
    public static final int DEVICE_OUT_AUX_LINE = 2097152;
    public static final String DEVICE_OUT_AUX_LINE_NAME = "aux_line";
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 128;
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 256;
    public static final String DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES_NAME = "bt_a2dp_hp";
    public static final String DEVICE_OUT_BLUETOOTH_A2DP_NAME = "bt_a2dp";
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 512;
    public static final String DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER_NAME = "bt_a2dp_spk";
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 16;
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 64;
    public static final String DEVICE_OUT_BLUETOOTH_SCO_CARKIT_NAME = "bt_sco_carkit";
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 32;
    public static final String DEVICE_OUT_BLUETOOTH_SCO_HEADSET_NAME = "bt_sco_hs";
    public static final String DEVICE_OUT_BLUETOOTH_SCO_NAME = "bt_sco";
    public static final int DEVICE_OUT_BUS = 16777216;
    public static final String DEVICE_OUT_BUS_NAME = "bus";
    public static final int DEVICE_OUT_DEFAULT = 1073741824;
    public static final int DEVICE_OUT_DGTL_DOCK_HEADSET = 4096;
    public static final String DEVICE_OUT_DGTL_DOCK_HEADSET_NAME = "digital_dock";
    public static final int DEVICE_OUT_EARPIECE = 1;
    public static final String DEVICE_OUT_EARPIECE_NAME = "earpiece";
    public static final int DEVICE_OUT_FM = 1048576;
    public static final String DEVICE_OUT_FM_NAME = "fm_transmitter";
    public static final int DEVICE_OUT_HDMI = 1024;
    public static final int DEVICE_OUT_HDMI_ARC = 262144;
    public static final String DEVICE_OUT_HDMI_ARC_NAME = "hmdi_arc";
    public static final String DEVICE_OUT_HDMI_NAME = "hdmi";
    public static final int DEVICE_OUT_IP = 8388608;
    public static final String DEVICE_OUT_IP_NAME = "ip";
    public static final int DEVICE_OUT_LINE = 131072;
    public static final String DEVICE_OUT_LINE_NAME = "line";
    public static final int DEVICE_OUT_PROXY = 33554432;
    public static final String DEVICE_OUT_PROXY_NAME = "proxy";
    public static final int DEVICE_OUT_REMOTE_SUBMIX = 32768;
    public static final String DEVICE_OUT_REMOTE_SUBMIX_NAME = "remote_submix";
    public static final int DEVICE_OUT_SPDIF = 524288;
    public static final String DEVICE_OUT_SPDIF_NAME = "spdif";
    public static final int DEVICE_OUT_SPEAKER = 2;
    public static final String DEVICE_OUT_SPEAKER_NAME = "speaker";
    public static final int DEVICE_OUT_SPEAKER_SAFE = 4194304;
    public static final String DEVICE_OUT_SPEAKER_SAFE_NAME = "speaker_safe";
    public static final int DEVICE_OUT_TELEPHONY_TX = 65536;
    public static final String DEVICE_OUT_TELEPHONY_TX_NAME = "telephony_tx";
    public static final int DEVICE_OUT_USB_ACCESSORY = 8192;
    public static final String DEVICE_OUT_USB_ACCESSORY_NAME = "usb_accessory";
    public static final int DEVICE_OUT_USB_DEVICE = 16384;
    public static final String DEVICE_OUT_USB_DEVICE_NAME = "usb_device";
    public static final int DEVICE_OUT_USB_HEADSET = 67108864;
    public static final String DEVICE_OUT_USB_HEADSET_NAME = "usb_headset";
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 8;
    public static final String DEVICE_OUT_WIRED_HEADPHONE_NAME = "headphone";
    public static final int DEVICE_OUT_WIRED_HEADSET = 4;
    public static final String DEVICE_OUT_WIRED_HEADSET_NAME = "headset";
    public static final int DEVICE_STATE_AVAILABLE = 1;
    public static final int DEVICE_STATE_UNAVAILABLE = 0;
    private static final int DYNAMIC_POLICY_EVENT_MIX_STATE_UPDATE = 0;
    public static final int ERROR = -1;
    public static final int FORCE_ANALOG_DOCK = 8;
    public static final int FORCE_BT_A2DP = 4;
    public static final int FORCE_BT_CAR_DOCK = 6;
    public static final int FORCE_BT_DESK_DOCK = 7;
    public static final int FORCE_BT_SCO = 3;
    public static final int FORCE_DEFAULT = 0;
    public static final int FORCE_DIGITAL_DOCK = 9;
    public static final int FORCE_ENCODED_SURROUND_ALWAYS = 14;
    public static final int FORCE_ENCODED_SURROUND_NEVER = 13;
    public static final int FORCE_HDMI_SYSTEM_AUDIO_ENFORCED = 12;
    public static final int FORCE_HEADPHONES = 2;
    public static final int FORCE_NONE = 0;
    public static final int FORCE_NO_BT_A2DP = 10;
    public static final int FORCE_SPEAKER = 1;
    public static final int FORCE_SYSTEM_ENFORCED = 11;
    public static final int FORCE_WIRED_ACCESSORY = 5;
    public static final int FOR_COMMUNICATION = 0;
    public static final int FOR_DOCK = 3;
    public static final int FOR_ENCODED_SURROUND = 6;
    public static final int FOR_HDMI_SYSTEM_AUDIO = 5;
    public static final int FOR_MEDIA = 1;
    public static final int FOR_RECORD = 2;
    public static final int FOR_SYSTEM = 4;
    public static final int INVALID_OPERATION = -3;
    public static final String IN_VOICE_COMM_FOCUS_ID = "AudioFocus_For_Phone_Ring_And_Calls";
    private static final int MAX_VOLUME = SystemProperties.getInt("ro.config.vol_steps", -1);
    public static final int MODE_CURRENT = -1;
    public static final int MODE_INVALID = -2;
    public static final int MODE_IN_CALL = 2;
    public static final int MODE_IN_COMMUNICATION = 3;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RINGTONE = 1;
    static final int NATIVE_EVENT_ROUTING_CHANGE = 1000;
    public static final int NO_INIT = -5;
    private static final int NUM_DEVICE_STATES = 1;
    public static final int NUM_FORCE_CONFIG = 15;
    private static final int NUM_FORCE_USE = 7;
    public static final int NUM_MODES = 4;
    public static final int NUM_STREAMS = 5;
    private static final int NUM_STREAM_TYPES = 11;
    public static final int PERMISSION_DENIED = -4;
    public static final int PHONE_STATE_INCALL = 2;
    public static final int PHONE_STATE_OFFCALL = 0;
    public static final int PHONE_STATE_RINGING = 1;
    public static final int PLATFORM_DEFAULT = 0;
    public static final int PLATFORM_TELEVISION = 2;
    public static final int PLATFORM_VOICE = 1;
    public static final int PLAY_SOUND_DELAY = 300;
    @Deprecated
    public static final int ROUTE_ALL = -1;
    @Deprecated
    public static final int ROUTE_BLUETOOTH = 4;
    @Deprecated
    public static final int ROUTE_BLUETOOTH_A2DP = 16;
    @Deprecated
    public static final int ROUTE_BLUETOOTH_SCO = 4;
    @Deprecated
    public static final int ROUTE_EARPIECE = 1;
    @Deprecated
    public static final int ROUTE_HEADSET = 8;
    @Deprecated
    public static final int ROUTE_SPEAKER = 2;
    public static final int STREAM_ACCESSIBILITY = 10;
    public static final int STREAM_ALARM = 4;
    public static final int STREAM_BLUETOOTH_SCO = 6;
    public static final int STREAM_DEFAULT = -1;
    public static final int STREAM_DTMF = 8;
    public static final int STREAM_MUSIC = 3;
    public static final String[] STREAM_NAMES = new String[]{"STREAM_VOICE_CALL", "STREAM_SYSTEM", "STREAM_RING", "STREAM_MUSIC", "STREAM_ALARM", "STREAM_NOTIFICATION", "STREAM_BLUETOOTH_SCO", "STREAM_SYSTEM_ENFORCED", "STREAM_DTMF", "STREAM_TTS", "STREAM_ACCESSIBILITY"};
    public static final int STREAM_NOTIFICATION = 5;
    public static final int STREAM_RING = 2;
    public static final int STREAM_SYSTEM = 1;
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_TTS = 9;
    public static final int STREAM_VOICE_CALL = 0;
    public static final int SUCCESS = 0;
    public static final int SYNC_EVENT_NONE = 0;
    public static final int SYNC_EVENT_PRESENTATION_COMPLETE = 1;
    private static final String TAG = "AudioSystem";
    private static final int VOICE_CALL_MAX_VOLUME = SystemProperties.getInt("ro.config.voice_call_vol_steps", -1);
    public static final int WOULD_BLOCK = -7;
    private static ErrorCallback mErrorCallback;
    private static DynamicPolicyCallback sDynPolicyCallback;
    private static AudioRecordingCallback sRecordingCallback;

    public interface AudioRecordingCallback {
        void onRecordingConfigurationChanged(int i, int i2, int i3, int[] iArr);
    }

    public interface DynamicPolicyCallback {
        void onDynamicPolicyMixStateUpdate(String str, int i);
    }

    public interface ErrorCallback {
        void onError(int i);
    }

    public static native int checkAudioFlinger();

    public static native int createAudioPatch(AudioPatch[] audioPatchArr, AudioPortConfig[] audioPortConfigArr, AudioPortConfig[] audioPortConfigArr2);

    public static native int getAudioHwSyncForSession(int i);

    public static native int getDeviceConnectionState(int i, String str);

    public static native int getDevicesForStream(int i);

    public static native int getForceUse(int i);

    public static native boolean getMasterMono();

    public static native boolean getMasterMute();

    public static native float getMasterVolume();

    public static native int getOutputLatency(int i);

    public static native String getParameters(String str);

    public static native int getPrimaryOutputFrameCount();

    public static native int getPrimaryOutputSamplingRate();

    public static native int getStreamVolumeIndex(int i, int i2);

    public static native int handleDeviceConfigChange(int i, String str, String str2);

    public static native int initStreamVolume(int i, int i2, int i3);

    public static native boolean isMicrophoneMuted();

    public static native boolean isSourceActive(int i);

    public static native boolean isStreamActive(int i, int i2);

    public static native boolean isStreamActiveRemotely(int i, int i2);

    public static native int listAudioPatches(ArrayList<AudioPatch> arrayList, int[] iArr);

    public static native int listAudioPorts(ArrayList<AudioPort> arrayList, int[] iArr);

    public static native int muteMicrophone(boolean z);

    private static final native void native_register_dynamic_policy_callback();

    private static final native void native_register_recording_callback();

    public static native int newAudioPlayerId();

    public static native int newAudioSessionId();

    public static native int registerPolicyMixes(ArrayList<AudioMix> arrayList, boolean z);

    public static native int releaseAudioPatch(AudioPatch audioPatch);

    public static native int setAudioPortConfig(AudioPortConfig audioPortConfig);

    public static native int setDeviceConnectionState(int i, int i2, String str, String str2);

    public static native int setForceUse(int i, int i2);

    public static native int setLowRamDevice(boolean z);

    public static native int setMasterMono(boolean z);

    public static native int setMasterMute(boolean z);

    public static native int setMasterVolume(float f);

    public static native int setParameters(String str);

    public static native int setPhoneState(int i);

    public static native int setStreamVolumeIndex(int i, int i2, int i3);

    public static native int systemReady();

    public static final int getNumStreamTypes() {
        return 11;
    }

    static {
        int[] iArr;
        if (MAX_VOLUME > 0) {
            int i;
            int[] iArr2 = new int[11];
            if (VOICE_CALL_MAX_VOLUME > 0) {
                i = (VOICE_CALL_MAX_VOLUME * 4) / 5;
            } else {
                i = (MAX_VOLUME * 4) / 5;
            }
            iArr2[0] = i;
            iArr2[1] = (MAX_VOLUME * 7) / 7;
            iArr2[2] = (MAX_VOLUME * 5) / 7;
            iArr2[3] = (MAX_VOLUME * 11) / 15;
            iArr2[4] = (MAX_VOLUME * 6) / 7;
            iArr2[5] = (MAX_VOLUME * 5) / 7;
            iArr2[6] = (MAX_VOLUME * 7) / 15;
            iArr2[7] = (MAX_VOLUME * 7) / 7;
            iArr2[8] = (MAX_VOLUME * 11) / 15;
            iArr2[9] = (MAX_VOLUME * 11) / 15;
            iArr2[10] = (MAX_VOLUME * 11) / 15;
            iArr = iArr2;
        } else {
            iArr = new int[]{4, 7, 5, 11, 6, 5, 7, 7, 11, 11, 11};
        }
        DEFAULT_STREAM_VOLUME = iArr;
    }

    public static void setErrorCallback(ErrorCallback cb) {
        synchronized (AudioSystem.class) {
            mErrorCallback = cb;
            if (cb != null) {
                cb.onError(checkAudioFlinger());
            }
        }
    }

    private static void errorCallbackFromNative(int error) {
        ErrorCallback errorCallback = null;
        synchronized (AudioSystem.class) {
            if (mErrorCallback != null) {
                errorCallback = mErrorCallback;
            }
        }
        if (errorCallback != null) {
            errorCallback.onError(error);
        }
    }

    public static void setDynamicPolicyCallback(DynamicPolicyCallback cb) {
        synchronized (AudioSystem.class) {
            sDynPolicyCallback = cb;
            native_register_dynamic_policy_callback();
        }
    }

    private static void dynamicPolicyCallbackFromNative(int event, String regId, int val) {
        DynamicPolicyCallback cb = null;
        synchronized (AudioSystem.class) {
            if (sDynPolicyCallback != null) {
                cb = sDynPolicyCallback;
            }
        }
        if (cb != null) {
            switch (event) {
                case 0:
                    cb.onDynamicPolicyMixStateUpdate(regId, val);
                    return;
                default:
                    Log.e(TAG, "dynamicPolicyCallbackFromNative: unknown event " + event);
                    return;
            }
        }
    }

    public static void setRecordingCallback(AudioRecordingCallback cb) {
        synchronized (AudioSystem.class) {
            sRecordingCallback = cb;
            native_register_recording_callback();
        }
    }

    private static void recordingCallbackFromNative(int event, int session, int source, int[] recordingFormat) {
        AudioRecordingCallback cb;
        synchronized (AudioSystem.class) {
            cb = sRecordingCallback;
        }
        if (cb != null) {
            cb.onRecordingConfigurationChanged(event, session, source, recordingFormat);
        }
    }

    public static String getOutputDeviceName(int device) {
        switch (device) {
            case 1:
                return DEVICE_OUT_EARPIECE_NAME;
            case 2:
                return DEVICE_OUT_SPEAKER_NAME;
            case 4:
                return "headset";
            case 8:
                return DEVICE_OUT_WIRED_HEADPHONE_NAME;
            case 16:
                return DEVICE_OUT_BLUETOOTH_SCO_NAME;
            case 32:
                return "bt_sco_hs";
            case 64:
                return DEVICE_OUT_BLUETOOTH_SCO_CARKIT_NAME;
            case 128:
                return "bt_a2dp";
            case 256:
                return DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES_NAME;
            case 512:
                return DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER_NAME;
            case 1024:
                return DEVICE_OUT_HDMI_NAME;
            case 2048:
                return "analog_dock";
            case 4096:
                return "digital_dock";
            case 8192:
                return "usb_accessory";
            case 16384:
            case 536870912:
                return "usb_device";
            case 32768:
                return "remote_submix";
            case 65536:
                return DEVICE_OUT_TELEPHONY_TX_NAME;
            case 131072:
                return "line";
            case 262144:
                return DEVICE_OUT_HDMI_ARC_NAME;
            case 524288:
                return "spdif";
            case 1048576:
                return DEVICE_OUT_FM_NAME;
            case 2097152:
                return DEVICE_OUT_AUX_LINE_NAME;
            case 4194304:
                return DEVICE_OUT_SPEAKER_SAFE_NAME;
            case 8388608:
                return "ip";
            case 16777216:
                return "bus";
            case 33554432:
                return "proxy";
            case 67108864:
                return "usb_headset";
            default:
                return ProxyInfo.LOCAL_EXCL_LIST;
        }
    }

    public static String getInputDeviceName(int device) {
        switch (device) {
            case DEVICE_IN_COMMUNICATION /*-2147483647*/:
                return DEVICE_IN_COMMUNICATION_NAME;
            case DEVICE_IN_AMBIENT /*-2147483646*/:
                return DEVICE_IN_AMBIENT_NAME;
            case -2147483644:
                return DEVICE_IN_BUILTIN_MIC_NAME;
            case -2147483640:
                return "bt_sco_hs";
            case -2147483632:
                return "headset";
            case -2147483616:
                return "aux_digital";
            case -2147483584:
                return DEVICE_IN_TELEPHONY_RX_NAME;
            case -2147483520:
                return DEVICE_IN_BACK_MIC_NAME;
            case DEVICE_IN_REMOTE_SUBMIX /*-2147483392*/:
                return "remote_submix";
            case -2147483136:
                return "analog_dock";
            case -2147482624:
                return "digital_dock";
            case -2147481600:
                return "usb_accessory";
            case -2147479552:
            case -2013265920:
                return "usb_device";
            case -2147475456:
                return DEVICE_IN_FM_TUNER_NAME;
            case -2147467264:
                return DEVICE_IN_TV_TUNER_NAME;
            case -2147450880:
                return "line";
            case -2147418112:
                return "spdif";
            case DEVICE_IN_BLUETOOTH_A2DP /*-2147352576*/:
                return "bt_a2dp";
            case -2147221504:
                return DEVICE_IN_LOOPBACK_NAME;
            case DEVICE_IN_IP /*-2146959360*/:
                return "ip";
            case DEVICE_IN_BUS /*-2146435072*/:
                return "bus";
            case DEVICE_IN_PROXY /*-2130706432*/:
                return "proxy";
            case DEVICE_IN_USB_HEADSET /*-2113929216*/:
                return "usb_headset";
            default:
                return Integer.toString(device);
        }
    }

    public static int getValueForVibrateSetting(int existingValue, int vibrateType, int vibrateSetting) {
        return (existingValue & (~(3 << (vibrateType * 2)))) | ((vibrateSetting & 3) << (vibrateType * 2));
    }

    public static int getDefaultStreamVolume(int streamType) {
        return DEFAULT_STREAM_VOLUME[streamType];
    }

    public static String streamToString(int stream) {
        if (stream >= 0 && stream < STREAM_NAMES.length) {
            return STREAM_NAMES[stream];
        }
        if (stream == Integer.MIN_VALUE) {
            return "USE_DEFAULT_STREAM_TYPE";
        }
        return "UNKNOWN_STREAM_" + stream;
    }

    public static int getPlatformType(Context context) {
        if (context.getResources().getBoolean(17957047)) {
            return 1;
        }
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            return 2;
        }
        return 0;
    }

    public static boolean isSingleVolume(Context context) {
        return getPlatformType(context) != 2 ? context.getResources().getBoolean(17957006) : true;
    }
}
