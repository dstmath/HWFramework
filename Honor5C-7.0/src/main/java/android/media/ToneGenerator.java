package android.media;

public class ToneGenerator {
    public static final int MAX_VOLUME = 100;
    public static final int MIN_VOLUME = 0;
    public static final int TONE_CDMA_ABBR_ALERT = 97;
    public static final int TONE_CDMA_ABBR_INTERCEPT = 37;
    public static final int TONE_CDMA_ABBR_REORDER = 39;
    public static final int TONE_CDMA_ALERT_AUTOREDIAL_LITE = 87;
    public static final int TONE_CDMA_ALERT_CALL_GUARD = 93;
    public static final int TONE_CDMA_ALERT_INCALL_LITE = 91;
    public static final int TONE_CDMA_ALERT_NETWORK_LITE = 86;
    public static final int TONE_CDMA_ANSWER = 42;
    public static final int TONE_CDMA_CALLDROP_LITE = 95;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_INTERGROUP = 46;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL = 45;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_PAT3 = 48;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_PAT5 = 50;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_PAT6 = 51;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_PAT7 = 52;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_PING_RING = 49;
    public static final int TONE_CDMA_CALL_SIGNAL_ISDN_SP_PRI = 47;
    public static final int TONE_CDMA_CONFIRM = 41;
    public static final int TONE_CDMA_DIAL_TONE_LITE = 34;
    public static final int TONE_CDMA_EMERGENCY_RINGBACK = 92;
    public static final int TONE_CDMA_HIGH_L = 53;
    public static final int TONE_CDMA_HIGH_PBX_L = 71;
    public static final int TONE_CDMA_HIGH_PBX_SLS = 80;
    public static final int TONE_CDMA_HIGH_PBX_SS = 74;
    public static final int TONE_CDMA_HIGH_PBX_SSL = 77;
    public static final int TONE_CDMA_HIGH_PBX_S_X4 = 83;
    public static final int TONE_CDMA_HIGH_SLS = 65;
    public static final int TONE_CDMA_HIGH_SS = 56;
    public static final int TONE_CDMA_HIGH_SSL = 59;
    public static final int TONE_CDMA_HIGH_SS_2 = 62;
    public static final int TONE_CDMA_HIGH_S_X4 = 68;
    public static final int TONE_CDMA_INTERCEPT = 36;
    public static final int TONE_CDMA_KEYPAD_VOLUME_KEY_LITE = 89;
    public static final int TONE_CDMA_LOW_L = 55;
    public static final int TONE_CDMA_LOW_PBX_L = 73;
    public static final int TONE_CDMA_LOW_PBX_SLS = 82;
    public static final int TONE_CDMA_LOW_PBX_SS = 76;
    public static final int TONE_CDMA_LOW_PBX_SSL = 79;
    public static final int TONE_CDMA_LOW_PBX_S_X4 = 85;
    public static final int TONE_CDMA_LOW_SLS = 67;
    public static final int TONE_CDMA_LOW_SS = 58;
    public static final int TONE_CDMA_LOW_SSL = 61;
    public static final int TONE_CDMA_LOW_SS_2 = 64;
    public static final int TONE_CDMA_LOW_S_X4 = 70;
    public static final int TONE_CDMA_MED_L = 54;
    public static final int TONE_CDMA_MED_PBX_L = 72;
    public static final int TONE_CDMA_MED_PBX_SLS = 81;
    public static final int TONE_CDMA_MED_PBX_SS = 75;
    public static final int TONE_CDMA_MED_PBX_SSL = 78;
    public static final int TONE_CDMA_MED_PBX_S_X4 = 84;
    public static final int TONE_CDMA_MED_SLS = 66;
    public static final int TONE_CDMA_MED_SS = 57;
    public static final int TONE_CDMA_MED_SSL = 60;
    public static final int TONE_CDMA_MED_SS_2 = 63;
    public static final int TONE_CDMA_MED_S_X4 = 69;
    public static final int TONE_CDMA_NETWORK_BUSY = 40;
    public static final int TONE_CDMA_NETWORK_BUSY_ONE_SHOT = 96;
    public static final int TONE_CDMA_NETWORK_CALLWAITING = 43;
    public static final int TONE_CDMA_NETWORK_USA_RINGBACK = 35;
    public static final int TONE_CDMA_ONE_MIN_BEEP = 88;
    public static final int TONE_CDMA_PIP = 44;
    public static final int TONE_CDMA_PRESSHOLDKEY_LITE = 90;
    public static final int TONE_CDMA_REORDER = 38;
    public static final int TONE_CDMA_SIGNAL_OFF = 98;
    public static final int TONE_CDMA_SOFT_ERROR_LITE = 94;
    public static final int TONE_DTMF_0 = 0;
    public static final int TONE_DTMF_1 = 1;
    public static final int TONE_DTMF_2 = 2;
    public static final int TONE_DTMF_3 = 3;
    public static final int TONE_DTMF_4 = 4;
    public static final int TONE_DTMF_5 = 5;
    public static final int TONE_DTMF_6 = 6;
    public static final int TONE_DTMF_7 = 7;
    public static final int TONE_DTMF_8 = 8;
    public static final int TONE_DTMF_9 = 9;
    public static final int TONE_DTMF_A = 12;
    public static final int TONE_DTMF_B = 13;
    public static final int TONE_DTMF_C = 14;
    public static final int TONE_DTMF_D = 15;
    public static final int TONE_DTMF_P = 11;
    public static final int TONE_DTMF_S = 10;
    public static final int TONE_PROP_ACK = 25;
    public static final int TONE_PROP_BEEP = 24;
    public static final int TONE_PROP_BEEP2 = 28;
    public static final int TONE_PROP_NACK = 26;
    public static final int TONE_PROP_PROMPT = 27;
    public static final int TONE_SUP_BUSY = 17;
    public static final int TONE_SUP_CALL_WAITING = 22;
    public static final int TONE_SUP_CONFIRM = 32;
    public static final int TONE_SUP_CONGESTION = 18;
    public static final int TONE_SUP_CONGESTION_ABBREV = 31;
    public static final int TONE_SUP_DIAL = 16;
    public static final int TONE_SUP_ERROR = 21;
    public static final int TONE_SUP_INTERCEPT = 29;
    public static final int TONE_SUP_INTERCEPT_ABBREV = 30;
    public static final int TONE_SUP_PIP = 33;
    public static final int TONE_SUP_RADIO_ACK = 19;
    public static final int TONE_SUP_RADIO_NOTAVAIL = 20;
    public static final int TONE_SUP_RINGTONE = 23;
    public static final int TONE_UNKNOWN = -1;
    private long mNativeContext;

    private final native void native_finalize();

    private final native void native_setup(int i, int i2);

    public final native int getAudioSessionId();

    public native void release();

    public native boolean startTone(int i, int i2);

    public native void stopTone();

    public ToneGenerator(int streamType, int volume) {
        native_setup(streamType, volume);
    }

    public boolean startTone(int toneType) {
        return startTone(toneType, TONE_UNKNOWN);
    }

    protected void finalize() {
        native_finalize();
    }
}
