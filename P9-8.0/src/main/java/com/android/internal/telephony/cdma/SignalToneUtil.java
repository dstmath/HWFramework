package com.android.internal.telephony.cdma;

import java.util.HashMap;

public class SignalToneUtil {
    public static final int CDMA_INVALID_TONE = -1;
    public static final int IS95_CONST_IR_ALERT_HIGH = 1;
    public static final int IS95_CONST_IR_ALERT_LOW = 2;
    public static final int IS95_CONST_IR_ALERT_MED = 0;
    public static final int IS95_CONST_IR_SIGNAL_IS54B = 2;
    public static final int IS95_CONST_IR_SIGNAL_ISDN = 1;
    public static final int IS95_CONST_IR_SIGNAL_TONE = 0;
    public static final int IS95_CONST_IR_SIGNAL_USR_DEFD_ALERT = 4;
    public static final int IS95_CONST_IR_SIG_IS54B_L = 1;
    public static final int IS95_CONST_IR_SIG_IS54B_NO_TONE = 0;
    public static final int IS95_CONST_IR_SIG_IS54B_PBX_L = 7;
    public static final int IS95_CONST_IR_SIG_IS54B_PBX_SLS = 10;
    public static final int IS95_CONST_IR_SIG_IS54B_PBX_SS = 8;
    public static final int IS95_CONST_IR_SIG_IS54B_PBX_SSL = 9;
    public static final int IS95_CONST_IR_SIG_IS54B_PBX_S_X4 = 11;
    public static final int IS95_CONST_IR_SIG_IS54B_SLS = 5;
    public static final int IS95_CONST_IR_SIG_IS54B_SS = 2;
    public static final int IS95_CONST_IR_SIG_IS54B_SSL = 3;
    public static final int IS95_CONST_IR_SIG_IS54B_SS_2 = 4;
    public static final int IS95_CONST_IR_SIG_IS54B_S_X4 = 6;
    public static final int IS95_CONST_IR_SIG_ISDN_INTGRP = 1;
    public static final int IS95_CONST_IR_SIG_ISDN_NORMAL = 0;
    public static final int IS95_CONST_IR_SIG_ISDN_OFF = 15;
    public static final int IS95_CONST_IR_SIG_ISDN_PAT_3 = 3;
    public static final int IS95_CONST_IR_SIG_ISDN_PAT_5 = 5;
    public static final int IS95_CONST_IR_SIG_ISDN_PAT_6 = 6;
    public static final int IS95_CONST_IR_SIG_ISDN_PAT_7 = 7;
    public static final int IS95_CONST_IR_SIG_ISDN_PING = 4;
    public static final int IS95_CONST_IR_SIG_ISDN_SP_PRI = 2;
    public static final int IS95_CONST_IR_SIG_TONE_ABBR_ALRT = 0;
    public static final int IS95_CONST_IR_SIG_TONE_ABB_INT = 3;
    public static final int IS95_CONST_IR_SIG_TONE_ABB_RE = 5;
    public static final int IS95_CONST_IR_SIG_TONE_ANSWER = 8;
    public static final int IS95_CONST_IR_SIG_TONE_BUSY = 6;
    public static final int IS95_CONST_IR_SIG_TONE_CALL_W = 9;
    public static final int IS95_CONST_IR_SIG_TONE_CONFIRM = 7;
    public static final int IS95_CONST_IR_SIG_TONE_DIAL = 0;
    public static final int IS95_CONST_IR_SIG_TONE_INT = 2;
    public static final int IS95_CONST_IR_SIG_TONE_NO_TONE = 63;
    public static final int IS95_CONST_IR_SIG_TONE_PIP = 10;
    public static final int IS95_CONST_IR_SIG_TONE_REORDER = 4;
    public static final int IS95_CONST_IR_SIG_TONE_RING = 1;
    public static final int TAPIAMSSCDMA_SIGNAL_PITCH_UNKNOWN = 0;
    private static HashMap<Integer, Integer> mHm = new HashMap();

    static {
        mHm.put(signalParamHash(1, 0, 0), Integer.valueOf(45));
        mHm.put(signalParamHash(1, 0, 1), Integer.valueOf(46));
        mHm.put(signalParamHash(1, 0, 2), Integer.valueOf(47));
        mHm.put(signalParamHash(1, 0, 3), Integer.valueOf(48));
        mHm.put(signalParamHash(1, 0, 4), Integer.valueOf(49));
        mHm.put(signalParamHash(1, 0, 5), Integer.valueOf(50));
        mHm.put(signalParamHash(1, 0, 6), Integer.valueOf(51));
        mHm.put(signalParamHash(1, 0, 7), Integer.valueOf(52));
        mHm.put(signalParamHash(1, 0, 15), Integer.valueOf(98));
        mHm.put(signalParamHash(0, 0, 0), Integer.valueOf(34));
        mHm.put(signalParamHash(0, 0, 1), Integer.valueOf(35));
        mHm.put(signalParamHash(0, 0, 2), Integer.valueOf(29));
        mHm.put(signalParamHash(0, 0, 3), Integer.valueOf(30));
        mHm.put(signalParamHash(0, 0, 4), Integer.valueOf(38));
        mHm.put(signalParamHash(0, 0, 5), Integer.valueOf(39));
        mHm.put(signalParamHash(0, 0, 6), Integer.valueOf(40));
        mHm.put(signalParamHash(0, 0, 7), Integer.valueOf(32));
        mHm.put(signalParamHash(0, 0, 8), Integer.valueOf(42));
        mHm.put(signalParamHash(0, 0, 9), Integer.valueOf(43));
        mHm.put(signalParamHash(0, 0, 10), Integer.valueOf(44));
        mHm.put(signalParamHash(0, 0, 63), Integer.valueOf(98));
        mHm.put(signalParamHash(2, 1, 1), Integer.valueOf(53));
        mHm.put(signalParamHash(2, 0, 1), Integer.valueOf(54));
        mHm.put(signalParamHash(2, 2, 1), Integer.valueOf(55));
        mHm.put(signalParamHash(2, 1, 2), Integer.valueOf(56));
        mHm.put(signalParamHash(2, 0, 2), Integer.valueOf(57));
        mHm.put(signalParamHash(2, 2, 2), Integer.valueOf(58));
        mHm.put(signalParamHash(2, 1, 3), Integer.valueOf(59));
        mHm.put(signalParamHash(2, 0, 3), Integer.valueOf(60));
        mHm.put(signalParamHash(2, 2, 3), Integer.valueOf(61));
        mHm.put(signalParamHash(2, 1, 4), Integer.valueOf(62));
        mHm.put(signalParamHash(2, 0, 4), Integer.valueOf(63));
        mHm.put(signalParamHash(2, 2, 4), Integer.valueOf(64));
        mHm.put(signalParamHash(2, 1, 5), Integer.valueOf(65));
        mHm.put(signalParamHash(2, 0, 5), Integer.valueOf(66));
        mHm.put(signalParamHash(2, 2, 5), Integer.valueOf(67));
        mHm.put(signalParamHash(2, 1, 6), Integer.valueOf(68));
        mHm.put(signalParamHash(2, 0, 6), Integer.valueOf(69));
        mHm.put(signalParamHash(2, 2, 6), Integer.valueOf(70));
        mHm.put(signalParamHash(2, 1, 7), Integer.valueOf(71));
        mHm.put(signalParamHash(2, 0, 7), Integer.valueOf(72));
        mHm.put(signalParamHash(2, 2, 7), Integer.valueOf(73));
        mHm.put(signalParamHash(2, 1, 8), Integer.valueOf(74));
        mHm.put(signalParamHash(2, 0, 8), Integer.valueOf(75));
        mHm.put(signalParamHash(2, 2, 8), Integer.valueOf(76));
        mHm.put(signalParamHash(2, 1, 9), Integer.valueOf(77));
        mHm.put(signalParamHash(2, 0, 9), Integer.valueOf(78));
        mHm.put(signalParamHash(2, 2, 9), Integer.valueOf(79));
        mHm.put(signalParamHash(2, 1, 10), Integer.valueOf(80));
        mHm.put(signalParamHash(2, 0, 10), Integer.valueOf(81));
        mHm.put(signalParamHash(2, 2, 10), Integer.valueOf(82));
        mHm.put(signalParamHash(2, 1, 11), Integer.valueOf(83));
        mHm.put(signalParamHash(2, 0, 11), Integer.valueOf(84));
        mHm.put(signalParamHash(2, 2, 11), Integer.valueOf(85));
        mHm.put(signalParamHash(2, 0, 0), Integer.valueOf(98));
        mHm.put(signalParamHash(4, 0, 0), Integer.valueOf(97));
        mHm.put(signalParamHash(4, 0, 63), Integer.valueOf(97));
    }

    private static Integer signalParamHash(int signalType, int alertPitch, int signal) {
        if (signalType < 0 || signalType > 256 || alertPitch > 256 || alertPitch < 0 || signal > 256 || signal < 0) {
            return new Integer(-1);
        }
        if (signalType != 2) {
            alertPitch = 0;
        }
        return new Integer((((signalType * 256) * 256) + (alertPitch * 256)) + signal);
    }

    public static int getAudioToneFromSignalInfo(int signalType, int alertPitch, int signal) {
        Integer result = (Integer) mHm.get(signalParamHash(signalType, alertPitch, signal));
        if (result == null) {
            return -1;
        }
        return result.intValue();
    }

    private SignalToneUtil() {
    }
}
