package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.audiofx.AudioEffect;
import android.media.audiopolicy.AudioMix;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.Map;

public class AudioSystem {
    public static final int AUDIO_DEVICE_IN_USB_DEVICE_EXTENDED = -2145386496;
    public static final int AUDIO_DEVICE_OUT_USB_DEVICE_EXTENDED = 536870912;
    public static final int AUDIO_FORMAT_AAC = 67108864;
    public static final int AUDIO_FORMAT_APTX = 536870912;
    public static final int AUDIO_FORMAT_APTX_HD = 553648128;
    public static final int AUDIO_FORMAT_DEFAULT = 0;
    public static final int AUDIO_FORMAT_INVALID = -1;
    public static final int AUDIO_FORMAT_LDAC = 587202560;
    public static final int AUDIO_FORMAT_SBC = 520093696;
    public static final int AUDIO_HW_SYNC_INVALID = 0;
    public static final int AUDIO_SESSION_ALLOCATE = 0;
    public static final int AUDIO_STATUS_ERROR = 1;
    public static final int AUDIO_STATUS_OK = 0;
    public static final int AUDIO_STATUS_SERVER_DIED = 100;
    public static final int BAD_VALUE = -2;
    public static final int DEAD_OBJECT = -6;
    private static final boolean DEBUG_VOLUME = false;
    public static final int DEFAULT_MUTE_STREAMS_AFFECTED = 111;
    public static int[] DEFAULT_STREAM_VOLUME = null;
    public static final int DEVICE_ALL_HDMI_SYSTEM_AUDIO_AND_SPEAKER = 2883586;
    public static final int DEVICE_BIT_DEFAULT = 1073741824;
    public static final int DEVICE_BIT_IN = Integer.MIN_VALUE;
    public static final int DEVICE_IN_ALL = -549453825;
    public static final int DEVICE_IN_ALL_SCO = -2147483640;
    public static final int DEVICE_IN_ALL_USB = -2111825920;
    @UnsupportedAppUsage
    public static final int DEVICE_IN_AMBIENT = -2147483646;
    public static final String DEVICE_IN_AMBIENT_NAME = "ambient";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_ANLG_DOCK_HEADSET = -2147483136;
    public static final String DEVICE_IN_ANLG_DOCK_HEADSET_NAME = "analog_dock";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_AUX_DIGITAL = -2147483616;
    public static final String DEVICE_IN_AUX_DIGITAL_NAME = "aux_digital";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_BACK_MIC = -2147483520;
    public static final String DEVICE_IN_BACK_MIC_NAME = "back_mic";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_BLUETOOTH_A2DP = -2147352576;
    public static final String DEVICE_IN_BLUETOOTH_A2DP_NAME = "bt_a2dp";
    public static final int DEVICE_IN_BLUETOOTH_BLE = -2080374784;
    public static final String DEVICE_IN_BLUETOOTH_BLE_NAME = "bt_ble";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_BLUETOOTH_SCO_HEADSET = -2147483640;
    public static final String DEVICE_IN_BLUETOOTH_SCO_HEADSET_NAME = "bt_sco_hs";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_BUILTIN_MIC = -2147483644;
    public static final String DEVICE_IN_BUILTIN_MIC_NAME = "mic";
    public static final int DEVICE_IN_BUS = -2146435072;
    public static final String DEVICE_IN_BUS_NAME = "bus";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_COMMUNICATION = -2147483647;
    public static final String DEVICE_IN_COMMUNICATION_NAME = "communication";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_DEFAULT = -1073741824;
    @UnsupportedAppUsage
    public static final int DEVICE_IN_DGTL_DOCK_HEADSET = -2147482624;
    public static final String DEVICE_IN_DGTL_DOCK_HEADSET_NAME = "digital_dock";
    public static final int DEVICE_IN_ECHO_REFERENCE = -1879048192;
    public static final String DEVICE_IN_ECHO_REFERENCE_NAME = "echo_reference";
    public static final int DEVICE_IN_FM_TUNER = -2147475456;
    public static final String DEVICE_IN_FM_TUNER_NAME = "fm_tuner";
    public static final int DEVICE_IN_HDMI = -2147483616;
    public static final int DEVICE_IN_HDMI_ARC = -2013265920;
    public static final String DEVICE_IN_HDMI_ARC_NAME = "hdmi_arc";
    public static final int DEVICE_IN_IP = -2146959360;
    public static final String DEVICE_IN_IP_NAME = "ip";
    public static final int DEVICE_IN_LINE = -2147450880;
    public static final String DEVICE_IN_LINE_NAME = "line";
    public static final int DEVICE_IN_LOOPBACK = -2147221504;
    public static final String DEVICE_IN_LOOPBACK_NAME = "loopback";
    public static final int DEVICE_IN_PROXY = -2130706432;
    public static final String DEVICE_IN_PROXY_NAME = "proxy";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_REMOTE_SUBMIX = -2147483392;
    public static final String DEVICE_IN_REMOTE_SUBMIX_NAME = "remote_submix";
    public static final int DEVICE_IN_SPDIF = -2147418112;
    public static final String DEVICE_IN_SPDIF_NAME = "spdif";
    public static final int DEVICE_IN_TELEPHONY_RX = -2147483584;
    public static final String DEVICE_IN_TELEPHONY_RX_NAME = "telephony_rx";
    public static final int DEVICE_IN_TV_TUNER = -2147467264;
    public static final String DEVICE_IN_TV_TUNER_NAME = "tv_tuner";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_USB_ACCESSORY = -2147481600;
    public static final String DEVICE_IN_USB_ACCESSORY_NAME = "usb_accessory";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_USB_DEVICE = -2147479552;
    public static final String DEVICE_IN_USB_DEVICE_NAME = "usb_device";
    public static final int DEVICE_IN_USB_HEADSET = -2113929216;
    public static final String DEVICE_IN_USB_HEADSET_NAME = "usb_headset";
    @UnsupportedAppUsage
    public static final int DEVICE_IN_VOICE_CALL = -2147483584;
    @UnsupportedAppUsage
    public static final int DEVICE_IN_WIRED_HEADSET = -2147483632;
    public static final String DEVICE_IN_WIRED_HEADSET_NAME = "headset";
    public static final int DEVICE_NONE = 0;
    public static final int DEVICE_OUT_ALL = 1879048191;
    public static final int DEVICE_OUT_ALL_A2DP = 896;
    public static final int DEVICE_OUT_ALL_HDMI_SYSTEM_AUDIO = 2883584;
    public static final int DEVICE_OUT_ALL_SCO = 112;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_ALL_USB = 604004352;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_ANLG_DOCK_HEADSET = 2048;
    public static final String DEVICE_OUT_ANLG_DOCK_HEADSET_NAME = "analog_dock";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_AUX_DIGITAL = 1024;
    public static final String DEVICE_OUT_AUX_DIGITAL_NAME = "aux_digital";
    public static final int DEVICE_OUT_AUX_LINE = 2097152;
    public static final String DEVICE_OUT_AUX_LINE_NAME = "aux_line";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_A2DP = 128;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES = 256;
    public static final String DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES_NAME = "bt_a2dp_hp";
    public static final String DEVICE_OUT_BLUETOOTH_A2DP_NAME = "bt_a2dp";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER = 512;
    public static final String DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER_NAME = "bt_a2dp_spk";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_SCO = 16;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_SCO_CARKIT = 64;
    public static final String DEVICE_OUT_BLUETOOTH_SCO_CARKIT_NAME = "bt_sco_carkit";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_BLUETOOTH_SCO_HEADSET = 32;
    public static final String DEVICE_OUT_BLUETOOTH_SCO_HEADSET_NAME = "bt_sco_hs";
    public static final String DEVICE_OUT_BLUETOOTH_SCO_NAME = "bt_sco";
    public static final int DEVICE_OUT_BUS = 16777216;
    public static final String DEVICE_OUT_BUS_NAME = "bus";
    public static final int DEVICE_OUT_DEFAULT = 1073741824;
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_DGTL_DOCK_HEADSET = 4096;
    public static final String DEVICE_OUT_DGTL_DOCK_HEADSET_NAME = "digital_dock";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_EARPIECE = 1;
    public static final String DEVICE_OUT_EARPIECE_NAME = "earpiece";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_FM = 1048576;
    public static final String DEVICE_OUT_FM_NAME = "fm_transmitter";
    public static final int DEVICE_OUT_HDMI = 1024;
    public static final int DEVICE_OUT_HDMI_ARC = 262144;
    public static final String DEVICE_OUT_HDMI_ARC_NAME = "hmdi_arc";
    public static final String DEVICE_OUT_HDMI_NAME = "hdmi";
    public static final int DEVICE_OUT_HEARING_AID = 134217728;
    public static final String DEVICE_OUT_HEARING_AID_NAME = "hearing_aid_out";
    public static final int DEVICE_OUT_IP = 8388608;
    public static final String DEVICE_OUT_IP_NAME = "ip";
    public static final int DEVICE_OUT_LINE = 131072;
    public static final String DEVICE_OUT_LINE_NAME = "line";
    public static final int DEVICE_OUT_PROXY = 33554432;
    public static final String DEVICE_OUT_PROXY_NAME = "proxy";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_REMOTE_SUBMIX = 32768;
    public static final String DEVICE_OUT_REMOTE_SUBMIX_NAME = "remote_submix";
    public static final int DEVICE_OUT_SPDIF = 524288;
    public static final String DEVICE_OUT_SPDIF_NAME = "spdif";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_SPEAKER = 2;
    public static final String DEVICE_OUT_SPEAKER_NAME = "speaker";
    public static final int DEVICE_OUT_SPEAKER_SAFE = 4194304;
    public static final String DEVICE_OUT_SPEAKER_SAFE_NAME = "speaker_safe";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_TELEPHONY_TX = 65536;
    public static final String DEVICE_OUT_TELEPHONY_TX_NAME = "telephony_tx";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_USB_ACCESSORY = 8192;
    public static final String DEVICE_OUT_USB_ACCESSORY_NAME = "usb_accessory";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_USB_DEVICE = 16384;
    public static final String DEVICE_OUT_USB_DEVICE_NAME = "usb_device";
    public static final int DEVICE_OUT_USB_HEADSET = 67108864;
    public static final String DEVICE_OUT_USB_HEADSET_NAME = "usb_headset";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_WIRED_HEADPHONE = 8;
    public static final String DEVICE_OUT_WIRED_HEADPHONE_NAME = "headphone";
    @UnsupportedAppUsage
    public static final int DEVICE_OUT_WIRED_HEADSET = 4;
    public static final String DEVICE_OUT_WIRED_HEADSET_NAME = "headset";
    @UnsupportedAppUsage
    public static final int DEVICE_STATE_AVAILABLE = 1;
    @UnsupportedAppUsage
    public static final int DEVICE_STATE_UNAVAILABLE = 0;
    private static final int DYNAMIC_POLICY_EVENT_MIX_STATE_UPDATE = 0;
    public static final int ERROR = -1;
    @UnsupportedAppUsage
    public static final int FORCE_ANALOG_DOCK = 8;
    public static final int FORCE_BT_A2DP = 4;
    @UnsupportedAppUsage
    public static final int FORCE_BT_CAR_DOCK = 6;
    @UnsupportedAppUsage
    public static final int FORCE_BT_DESK_DOCK = 7;
    public static final int FORCE_BT_SCO = 3;
    public static final int FORCE_DEFAULT = 0;
    public static final int FORCE_DESKTOP_HDMI = 16;
    public static final int FORCE_DESKTOP_NO_HDMI = 17;
    @UnsupportedAppUsage
    public static final int FORCE_DIGITAL_DOCK = 9;
    public static final int FORCE_ENCODED_SURROUND_ALWAYS = 14;
    public static final int FORCE_ENCODED_SURROUND_MANUAL = 15;
    public static final int FORCE_ENCODED_SURROUND_NEVER = 13;
    public static final int FORCE_EXTEND_A2DP = 20;
    public static final int FORCE_EXTEND_EARPIECE = 18;
    public static final int FORCE_EXTEND_HEADSET = 22;
    public static final int FORCE_EXTEND_MSDP = 26;
    public static final int FORCE_EXTEND_OTHER = 25;
    public static final int FORCE_EXTEND_SCO = 21;
    public static final int FORCE_EXTEND_SPEAKER = 19;
    public static final int FORCE_EXTEND_TYPEC = 23;
    public static final int FORCE_EXTEND_USB = 24;
    public static final int FORCE_HDMI_SYSTEM_AUDIO_ENFORCED = 12;
    public static final int FORCE_HEADPHONES = 2;
    @UnsupportedAppUsage
    public static final int FORCE_NONE = 0;
    public static final int FORCE_NO_BT_A2DP = 10;
    public static final int FORCE_SPEAKER = 1;
    public static final int FORCE_SYSTEM_ENFORCED = 11;
    public static final int FORCE_WIRED_ACCESSORY = 5;
    public static final int FOR_COMMUNICATION = 0;
    public static final int FOR_DESKTOP_MODE = 8;
    public static final int FOR_DOCK = 3;
    public static final int FOR_ENCODED_SURROUND = 6;
    public static final int FOR_EXTEND_MEDIA = 9;
    public static final int FOR_HDMI_SYSTEM_AUDIO = 5;
    public static final int FOR_MEDIA = 1;
    public static final int FOR_RECORD = 2;
    public static final int FOR_SYSTEM = 4;
    public static final int FOR_VIBRATE_RINGING = 7;
    public static final int INVALID_OPERATION = -3;
    public static final String IN_VOICE_COMM_FOCUS_ID = "AudioFocus_For_Phone_Ring_And_Calls";
    private static final int MAX_VOLUME = SystemProperties.getInt("ro.config.vol_steps", -1);
    private static final int MEDIA_MAX_VOLUME = SystemProperties.getInt("ro.config.media_vol_steps", -1);
    public static final int MODE_CURRENT = -1;
    public static final int MODE_INVALID = -2;
    public static final int MODE_IN_CALL = 2;
    public static final int MODE_IN_COMMUNICATION = 3;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_RINGTONE = 1;
    static final int NATIVE_EVENT_ROUTING_CHANGE = 1000;
    public static final int NO_INIT = -5;
    private static final int NUM_DEVICE_STATES = 1;
    public static final int NUM_FORCE_CONFIG = 27;
    private static final int NUM_FORCE_USE = 10;
    public static final int NUM_MODES = 4;
    public static final int NUM_STREAMS = 5;
    private static final int NUM_STREAM_TYPES = 11;
    public static final int OUT_CHANNEL_COUNT_MAX = native_get_FCC_8();
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
    public static final String[] STREAM_NAMES = {"STREAM_VOICE_CALL", "STREAM_SYSTEM", "STREAM_RING", "STREAM_MUSIC", "STREAM_ALARM", "STREAM_NOTIFICATION", "STREAM_BLUETOOTH_SCO", "STREAM_SYSTEM_ENFORCED", "STREAM_DTMF", "STREAM_TTS", "STREAM_ACCESSIBILITY"};
    public static final int STREAM_NOTIFICATION = 5;
    public static final int STREAM_RING = 2;
    public static final int STREAM_SYSTEM = 1;
    @UnsupportedAppUsage
    public static final int STREAM_SYSTEM_ENFORCED = 7;
    public static final int STREAM_TTS = 9;
    public static final int STREAM_VOICE_CALL = 0;
    public static final int SUCCESS = 0;
    public static final int SYNC_EVENT_NONE = 0;
    public static final int SYNC_EVENT_PRESENTATION_COMPLETE = 1;
    private static final String TAG = "AudioSystem";
    private static final int VOICE_CALL_MAX_VOLUME = SystemProperties.getInt("ro.config.vc_call_vol_steps", -1);
    public static final int WOULD_BLOCK = -7;
    private static ErrorCallback mErrorCallback;
    private static PlaybackActivityMonitorCallback mPlaybackActivityMonitorCallback;
    private static DynamicPolicyCallback sDynPolicyCallback;
    private static AudioRecordingCallback sRecordingCallback;

    public interface AudioRecordingCallback {
        void onRecordingConfigurationChanged(int i, int i2, int i3, int i4, int i5, int i6, boolean z, int[] iArr, AudioEffect.Descriptor[] descriptorArr, AudioEffect.Descriptor[] descriptorArr2, int i7, String str);
    }

    public interface DynamicPolicyCallback {
        void onDynamicPolicyMixStateUpdate(String str, int i);
    }

    public interface ErrorCallback {
        void onError(int i);
    }

    public interface PlaybackActivityMonitorCallback {
        void OnDeviceChanged(int i, int i2, int i3);
    }

    @UnsupportedAppUsage
    public static native int checkAudioFlinger();

    public static native int createAudioPatch(AudioPatch[] audioPatchArr, AudioPortConfig[] audioPortConfigArr, AudioPortConfig[] audioPortConfigArr2);

    public static native int getAudioHwSyncForSession(int i);

    @UnsupportedAppUsage
    public static native int getDeviceConnectionState(int i, String str);

    @UnsupportedAppUsage
    public static native int getDevicesForStream(int i);

    @UnsupportedAppUsage
    public static native int getForceUse(int i);

    public static native int getHwOffloadEncodingFormatsSupportedForA2DP(ArrayList<Integer> arrayList);

    public static native float getMasterBalance();

    public static native boolean getMasterMono();

    @UnsupportedAppUsage
    public static native boolean getMasterMute();

    public static native float getMasterVolume();

    public static native int getMaxVolumeIndexForAttributes(AudioAttributes audioAttributes);

    public static native int getMicrophones(ArrayList<MicrophoneInfo> arrayList);

    public static native int getMinVolumeIndexForAttributes(AudioAttributes audioAttributes);

    @UnsupportedAppUsage
    public static native int getOutputLatency(int i);

    @UnsupportedAppUsage
    public static native String getParameters(String str);

    @UnsupportedAppUsage(trackingBug = 134049522)
    public static native int getPrimaryOutputFrameCount();

    @UnsupportedAppUsage(trackingBug = 134049522)
    public static native int getPrimaryOutputSamplingRate();

    public static native float getStreamVolumeDB(int i, int i2, int i3);

    public static native int getStreamVolumeIndex(int i, int i2);

    public static native int getSurroundFormats(Map<Integer, Boolean> map, boolean z);

    public static native int getVolumeIndexForAttributes(AudioAttributes audioAttributes, int i);

    public static native int handleDeviceConfigChange(int i, String str, String str2, int i2);

    @UnsupportedAppUsage
    public static native int initStreamVolume(int i, int i2, int i3);

    public static native boolean isHapticPlaybackSupported();

    @UnsupportedAppUsage
    public static native boolean isMicrophoneMuted();

    @UnsupportedAppUsage
    public static native boolean isSourceActive(int i);

    @UnsupportedAppUsage
    public static native boolean isStreamActive(int i, int i2);

    public static native boolean isStreamActiveRemotely(int i, int i2);

    public static native int listAudioPatches(ArrayList<AudioPatch> arrayList, int[] iArr);

    public static native int listAudioPorts(ArrayList<AudioPort> arrayList, int[] iArr);

    @UnsupportedAppUsage
    public static native int muteMicrophone(boolean z);

    private static final native void native_enableDeviceCallback();

    private static native int native_get_FCC_8();

    private static native boolean native_is_offload_supported(int i, int i2, int i3, int i4, int i5);

    private static final native void native_register_dynamic_policy_callback();

    private static final native void native_register_recording_callback();

    public static native int newAudioPlayerId();

    public static native int newAudioRecorderId();

    public static native int newAudioSessionId();

    public static native int registerPolicyMixes(ArrayList<AudioMix> arrayList, boolean z, int i);

    public static native int releaseAudioPatch(AudioPatch audioPatch);

    public static native int removeUidDeviceAffinities(int i);

    public static native int setA11yServicesUids(int[] iArr);

    public static native int setAllowedCapturePolicy(int i, int i2);

    public static native int setAssistantUid(int i);

    public static native int setAudioPortConfig(AudioPortConfig audioPortConfig);

    @UnsupportedAppUsage
    public static native int setDeviceConnectionState(int i, int i2, String str, String str2, int i3);

    @UnsupportedAppUsage
    public static native int setForceUse(int i, int i2);

    public static native int setLowRamDevice(boolean z, long j);

    public static native int setMasterBalance(float f);

    public static native int setMasterMono(boolean z);

    @UnsupportedAppUsage
    public static native int setMasterMute(boolean z);

    public static native int setMasterVolume(float f);

    @UnsupportedAppUsage
    public static native int setParameters(String str);

    @UnsupportedAppUsage
    public static native int setPhoneState(int i);

    public static native int setRttEnabled(boolean z);

    @UnsupportedAppUsage
    private static native int setStreamVolumeIndex(int i, int i2, int i3);

    public static native int setSurroundFormatEnabled(int i, boolean z);

    public static native int setUidDeviceAffinities(int i, int[] iArr, String[] strArr);

    public static native int setVolumeIndexForAttributes(AudioAttributes audioAttributes, int i, int i2);

    public static native int startAudioSource(AudioPortConfig audioPortConfig, AudioAttributes audioAttributes);

    public static native int stopAudioSource(int i);

    public static native int systemReady();

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0053: APUT  (r2v3 int[]), (0 ??[int, short, byte, char]), (r4v2 int) */
    static {
        int[] iArr;
        int i = MAX_VOLUME;
        if (i > 0) {
            iArr = new int[11];
            int i2 = VOICE_CALL_MAX_VOLUME;
            iArr[0] = i2 > 0 ? (i2 * 4) / 5 : (i * 4) / 5;
            int i3 = MAX_VOLUME;
            iArr[1] = (i3 * 7) / 7;
            iArr[2] = (i3 * 5) / 7;
            int i4 = MEDIA_MAX_VOLUME;
            iArr[3] = i4 > 0 ? (i4 * 25) / 100 : (i3 * 5) / 15;
            int i5 = MAX_VOLUME;
            iArr[4] = (i5 * 6) / 7;
            iArr[5] = (i5 * 5) / 7;
            iArr[6] = (i5 * 7) / 15;
            iArr[7] = (i5 * 7) / 7;
            iArr[8] = (i5 * 11) / 15;
            iArr[9] = (i5 * 11) / 15;
            iArr[10] = (i5 * 11) / 15;
        } else {
            iArr = new int[]{4, 7, 5, 5, 6, 5, 7, 7, 5, 5, 5};
        }
        DEFAULT_STREAM_VOLUME = iArr;
    }

    @UnsupportedAppUsage
    public static final int getNumStreamTypes() {
        return 11;
    }

    public static String modeToString(int mode) {
        if (mode == -2) {
            return "MODE_INVALID";
        }
        if (mode == -1) {
            return "MODE_CURRENT";
        }
        if (mode == 0) {
            return "MODE_NORMAL";
        }
        if (mode == 1) {
            return "MODE_RINGTONE";
        }
        if (mode == 2) {
            return "MODE_IN_CALL";
        }
        if (mode == 3) {
            return "MODE_IN_COMMUNICATION";
        }
        return "unknown mode (" + mode + ")";
    }

    public static int audioFormatToBluetoothSourceCodec(int audioFormat) {
        if (audioFormat == 67108864) {
            return 1;
        }
        if (audioFormat == 520093696) {
            return 0;
        }
        if (audioFormat == 536870912) {
            return 2;
        }
        if (audioFormat == 553648128) {
            return 3;
        }
        if (audioFormat != 587202560) {
            return 1000000;
        }
        return 4;
    }

    @UnsupportedAppUsage
    public static void setErrorCallback(ErrorCallback cb) {
        synchronized (AudioSystem.class) {
            mErrorCallback = cb;
            if (cb != null) {
                cb.onError(checkAudioFlinger());
            }
        }
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    private static void dynamicPolicyCallbackFromNative(int event, String regId, int val) {
        DynamicPolicyCallback cb = null;
        synchronized (AudioSystem.class) {
            if (sDynPolicyCallback != null) {
                cb = sDynPolicyCallback;
            }
        }
        if (cb == null) {
            return;
        }
        if (event != 0) {
            Log.e(TAG, "dynamicPolicyCallbackFromNative: unknown event " + event);
            return;
        }
        cb.onDynamicPolicyMixStateUpdate(regId, val);
    }

    public static void setRecordingCallback(AudioRecordingCallback cb) {
        synchronized (AudioSystem.class) {
            sRecordingCallback = cb;
            native_register_recording_callback();
        }
    }

    @UnsupportedAppUsage
    private static void recordingCallbackFromNative(int event, int riid, int uid, int session, int source, int portId, boolean silenced, int[] recordingFormat, AudioEffect.Descriptor[] clientEffects, AudioEffect.Descriptor[] effects, int activeSource) {
        Throwable th;
        AudioRecordingCallback cb;
        synchronized (AudioSystem.class) {
            try {
                cb = sRecordingCallback;
                try {
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        if (clientEffects.length != 0) {
            String str = clientEffects[0].name;
        }
        String str2 = effects.length == 0 ? "None" : effects[0].name;
        if (cb != null) {
            cb.onRecordingConfigurationChanged(event, riid, uid, session, source, portId, silenced, recordingFormat, clientEffects, effects, activeSource, "");
        }
    }

    public static String deviceStateToString(int state) {
        if (state == 0) {
            return "DEVICE_STATE_UNAVAILABLE";
        }
        if (state == 1) {
            return "DEVICE_STATE_AVAILABLE";
        }
        return "unknown state (" + state + ")";
    }

    @UnsupportedAppUsage
    public static String getOutputDeviceName(int device) {
        if (device == 1) {
            return DEVICE_OUT_EARPIECE_NAME;
        }
        if (device == 2) {
            return DEVICE_OUT_SPEAKER_NAME;
        }
        switch (device) {
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
            case 134217728:
                return DEVICE_OUT_HEARING_AID_NAME;
            default:
                return "";
        }
    }

    public static String getInputDeviceName(int device) {
        switch (device) {
            case DEVICE_IN_COMMUNICATION /* -2147483647 */:
                return DEVICE_IN_COMMUNICATION_NAME;
            case DEVICE_IN_AMBIENT /* -2147483646 */:
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
            case DEVICE_IN_REMOTE_SUBMIX /* -2147483392 */:
                return "remote_submix";
            case -2147483136:
                return "analog_dock";
            case -2147482624:
                return "digital_dock";
            case -2147481600:
                return "usb_accessory";
            case -2147479552:
            case -2145386496:
                return "usb_device";
            case -2147475456:
                return DEVICE_IN_FM_TUNER_NAME;
            case -2147467264:
                return DEVICE_IN_TV_TUNER_NAME;
            case -2147450880:
                return "line";
            case -2147418112:
                return "spdif";
            case DEVICE_IN_BLUETOOTH_A2DP /* -2147352576 */:
                return "bt_a2dp";
            case -2147221504:
                return DEVICE_IN_LOOPBACK_NAME;
            case DEVICE_IN_IP /* -2146959360 */:
                return "ip";
            case DEVICE_IN_BUS /* -2146435072 */:
                return "bus";
            case DEVICE_IN_PROXY /* -2130706432 */:
                return "proxy";
            case DEVICE_IN_USB_HEADSET /* -2113929216 */:
                return "usb_headset";
            case DEVICE_IN_BLUETOOTH_BLE /* -2080374784 */:
                return DEVICE_IN_BLUETOOTH_BLE_NAME;
            case -2013265920:
                return DEVICE_IN_HDMI_ARC_NAME;
            case -1879048192:
                return DEVICE_IN_ECHO_REFERENCE_NAME;
            default:
                return Integer.toString(device);
        }
    }

    public static String forceUseConfigToString(int config) {
        switch (config) {
            case 0:
                return "FORCE_NONE";
            case 1:
                return "FORCE_SPEAKER";
            case 2:
                return "FORCE_HEADPHONES";
            case 3:
                return "FORCE_BT_SCO";
            case 4:
                return "FORCE_BT_A2DP";
            case 5:
                return "FORCE_WIRED_ACCESSORY";
            case 6:
                return "FORCE_BT_CAR_DOCK";
            case 7:
                return "FORCE_BT_DESK_DOCK";
            case 8:
                return "FORCE_ANALOG_DOCK";
            case 9:
                return "FORCE_DIGITAL_DOCK";
            case 10:
                return "FORCE_NO_BT_A2DP";
            case 11:
                return "FORCE_SYSTEM_ENFORCED";
            case 12:
                return "FORCE_HDMI_SYSTEM_AUDIO_ENFORCED";
            case 13:
                return "FORCE_ENCODED_SURROUND_NEVER";
            case 14:
                return "FORCE_ENCODED_SURROUND_ALWAYS";
            case 15:
                return "FORCE_ENCODED_SURROUND_MANUAL";
            default:
                return "unknown config (" + config + ")";
        }
    }

    public static String forceUseUsageToString(int usage) {
        switch (usage) {
            case 0:
                return "FOR_COMMUNICATION";
            case 1:
                return "FOR_MEDIA";
            case 2:
                return "FOR_RECORD";
            case 3:
                return "FOR_DOCK";
            case 4:
                return "FOR_SYSTEM";
            case 5:
                return "FOR_HDMI_SYSTEM_AUDIO";
            case 6:
                return "FOR_ENCODED_SURROUND";
            case 7:
                return "FOR_VIBRATE_RINGING";
            default:
                return "unknown usage (" + usage + ")";
        }
    }

    public static int setStreamVolumeIndexAS(int stream, int index, int device) {
        Log.i(TAG, "setStreamVolumeIndexAS: stream=" + stream + " dev=" + Integer.toHexString(device) + " idx=" + index);
        return setStreamVolumeIndex(stream, index, device);
    }

    static boolean isOffloadSupported(AudioFormat format, AudioAttributes attr) {
        return native_is_offload_supported(format.getEncoding(), format.getSampleRate(), format.getChannelMask(), format.getChannelIndexMask(), attr.getVolumeControlStream());
    }

    public static int getValueForVibrateSetting(int existingValue, int vibrateType, int vibrateSetting) {
        return (existingValue & (~(3 << (vibrateType * 2)))) | ((vibrateSetting & 3) << (vibrateType * 2));
    }

    public static int getDefaultStreamVolume(int streamType) {
        return DEFAULT_STREAM_VOLUME[streamType];
    }

    public static String streamToString(int stream) {
        if (stream >= 0) {
            String[] strArr = STREAM_NAMES;
            if (stream < strArr.length) {
                return strArr[stream];
            }
        }
        if (stream == Integer.MIN_VALUE) {
            return "USE_DEFAULT_STREAM_TYPE";
        }
        return "UNKNOWN_STREAM_" + stream;
    }

    public static int getPlatformType(Context context) {
        if (context.getResources().getBoolean(R.bool.config_voice_capable)) {
            return 1;
        }
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            return 2;
        }
        return 0;
    }

    public static boolean isSingleVolume(Context context) {
        return getPlatformType(context) == 2 || context.getResources().getBoolean(R.bool.config_single_volume);
    }

    public static void setDeviceCallback() {
        synchronized (AudioSystem.class) {
            native_enableDeviceCallback();
        }
    }

    private static void postAudioEventFromNative(int arg1, int arg2, int arg3) {
        PlaybackActivityMonitorCallback playbackActivityMonitorCallback = mPlaybackActivityMonitorCallback;
        if (playbackActivityMonitorCallback != null) {
            playbackActivityMonitorCallback.OnDeviceChanged(arg1, arg2, arg3);
        }
    }

    public static void setPlaybackActivityMonitorCallback(PlaybackActivityMonitorCallback cb) {
        synchronized (AudioSystem.class) {
            mPlaybackActivityMonitorCallback = cb;
        }
    }
}
