package android.media;

public class HwMediaMonitorUtils {
    public static final int BAD_VALUE = -2;
    public static final int COUNT_MBD_MONITORSERVICE = 1;
    public static final int DEAD_OBJECT = -6;
    public static final int ERROR = -1;
    public static final int INVALID_OPERATION = -3;
    public static final int MEDIA_LOG_ERROR = 3;
    public static final int MEDIA_LOG_FATAL = 4;
    public static final int MEDIA_LOG_INFO = 1;
    public static final int MEDIA_LOG_WARN = 2;
    public static final int NO_INIT = -5;
    public static final int PERMISSION_DENIED = -4;
    public static final int SUCCESS = 0;
    private static final String TAG = "HwMediaMonitorUtils";
    private static final int[] TYPES_MBD_MONITORSERVICE = null;
    public static final int TYPE_AUDIOFLINGER_PLAYTRACK = 7;
    public static final int TYPE_AUDIOFLINGER_RECORDTRACK = 8;
    public static final int TYPE_AUDIO_BT_SCO_STATE = 6;
    public static final int TYPE_AUDIO_MODE = 1;
    public static final int TYPE_AUDIO_VOLUME = 2;
    public static final int TYPE_AUIDO_FORCE_COMMUNICATION_PATH = 3;
    public static final int TYPE_AUIDO_FORCE_MEDIA_PATH = 4;
    public static final int TYPE_AUIDO_FORCE_RECORD_PATH = 5;
    public static final int TYPE_MEDIA_BIGDATA_MAX = 20409;
    public static final int TYPE_MEDIA_BIGDATA_MIN = 10000;
    public static final int TYPE_MEDIA_RECORD_ALARM_COUNT = 20405;
    public static final int TYPE_MEDIA_RECORD_ANLG_DOCK_HEADSET_COUNT = 20214;
    public static final int TYPE_MEDIA_RECORD_AUDIOPLAYER_COUNT = 20103;
    public static final int TYPE_MEDIA_RECORD_AUX_DIGITAL_COUNT = 20211;
    public static final int TYPE_MEDIA_RECORD_AUX_LINE_COUNT = 20224;
    public static final int TYPE_MEDIA_RECORD_AWESOMEPLAYER_COUNT = 20105;
    public static final int TYPE_MEDIA_RECORD_BLUETOOTH_A2DP_COUNT = 20208;
    public static final int TYPE_MEDIA_RECORD_BLUETOOTH_A2DP_HEADPHONES_COUNT = 20209;
    public static final int TYPE_MEDIA_RECORD_BLUETOOTH_A2DP_SPEAKER_COUNT = 20210;
    public static final int TYPE_MEDIA_RECORD_BLUETOOTH_SCO_CARKIT_COUNT = 20207;
    public static final int TYPE_MEDIA_RECORD_BLUETOOTH_SCO_COUNT = 20205;
    public static final int TYPE_MEDIA_RECORD_BLUETOOTH_SCO_HEADSET_COUNT = 20206;
    public static final int TYPE_MEDIA_RECORD_CODEC_COUNT = 20300;
    public static final int TYPE_MEDIA_RECORD_DEV_COUNT = 20200;
    public static final int TYPE_MEDIA_RECORD_DGTL_DOCK_HEADSET_COUNT = 20215;
    public static final int TYPE_MEDIA_RECORD_DTMF_COUNT = 20409;
    public static final int TYPE_MEDIA_RECORD_DTS_COUNT = 20000;
    public static final int TYPE_MEDIA_RECORD_EARPIECE_COUNT = 20201;
    public static final int TYPE_MEDIA_RECORD_FM_COUNT = 20223;
    public static final int TYPE_MEDIA_RECORD_FOUNCTION_CALL = 10000;
    public static final int TYPE_MEDIA_RECORD_HDMI_ARC_COUNT = 20221;
    public static final int TYPE_MEDIA_RECORD_HDMI_COUNT = 20213;
    public static final int TYPE_MEDIA_RECORD_INTERNET_HEADSET_COUNT = 20001;
    public static final int TYPE_MEDIA_RECORD_IP_COUNT = 20226;
    public static final int TYPE_MEDIA_RECORD_LINE_COUNT = 20220;
    public static final int TYPE_MEDIA_RECORD_LOWPOWERPLAYER_COUNT = 20106;
    public static final int TYPE_MEDIA_RECORD_MEDIAPLAYER_COUNT = 20101;
    public static final int TYPE_MEDIA_RECORD_MUSIC_COUNT = 20404;
    public static final int TYPE_MEDIA_RECORD_NOTIFICATION_COUNT = 20406;
    public static final int TYPE_MEDIA_RECORD_NUPLAYER_COUNT = 20102;
    public static final int TYPE_MEDIA_RECORD_OUTPUT_COUNT = 20002;
    public static final int TYPE_MEDIA_RECORD_PLAYER_COUNT = 20100;
    public static final int TYPE_MEDIA_RECORD_PROXY_COUNT = 20227;
    public static final int TYPE_MEDIA_RECORD_REMOTE_SUBMIX_COUNT = 20218;
    public static final int TYPE_MEDIA_RECORD_RING_COUNT = 20403;
    public static final int TYPE_MEDIA_RECORD_SPDIF_COUNT = 20222;
    public static final int TYPE_MEDIA_RECORD_SPEAKER_COUNT = 20202;
    public static final int TYPE_MEDIA_RECORD_SPEAKER_SAFE_COUNT = 20225;
    public static final int TYPE_MEDIA_RECORD_STRAIGHTFRIGHTPLAYER_COUNT = 20104;
    public static final int TYPE_MEDIA_RECORD_STREAM_BLUETOOTH_SCO_COUNT = 20407;
    public static final int TYPE_MEDIA_RECORD_STREAM_COUNT = 20400;
    public static final int TYPE_MEDIA_RECORD_SYSTEM_COUNT = 20402;
    public static final int TYPE_MEDIA_RECORD_SYSTEM_ENFORCED_COUNT = 20408;
    public static final int TYPE_MEDIA_RECORD_TELEPHONY_TX_COUNT = 20219;
    public static final int TYPE_MEDIA_RECORD_USB_ACCESSORY_COUNT = 20216;
    public static final int TYPE_MEDIA_RECORD_USB_AUDIO_PLAYBACK_COUNT = 20003;
    public static final int TYPE_MEDIA_RECORD_USB_DEVICE_COUNT = 20217;
    public static final int TYPE_MEDIA_RECORD_USB_MIDI_COUNT = 20004;
    public static final int TYPE_MEDIA_RECORD_VOICE_CALL_COUNT = 20401;
    public static final int TYPE_MEDIA_RECORD_WIRED_HEADPHONE_COUNT = 20204;
    public static final int TYPE_MEDIA_RECORD_WIRED_HEADSET_COUNT = 20203;
    public static final int WOULD_BLOCK = -7;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.HwMediaMonitorUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.HwMediaMonitorUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.HwMediaMonitorUtils.<clinit>():void");
    }

    public static boolean isMediaBigDataWritedNative(int type) {
        for (int i = SUCCESS; i < TYPES_MBD_MONITORSERVICE.length; i += TYPE_AUDIO_MODE) {
            if (type == TYPES_MBD_MONITORSERVICE[i]) {
                return true;
            }
        }
        return false;
    }
}
