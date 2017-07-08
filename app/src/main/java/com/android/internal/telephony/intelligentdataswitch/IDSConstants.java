package com.android.internal.telephony.intelligentdataswitch;

import android.telephony.Rlog;

public class IDSConstants {
    public static final String CALL_UI_NAME = "com.android.incallui";
    public static final int CDMA_QOS_CFG_INDEX = 4;
    public static final int CHECK_VOICE_CALL_UI_ON_TOP_TIMER = 5000;
    public static final String DATA_SWITCH_FORBIDDEN_INTERVAL_STRING = null;
    public static final String DATA_SWITCH_QOS_STRING = null;
    public static final int DATA_SWITCH_REASON_DATA_DISCONNECT_EXCEPTION = 2;
    public static final int DATA_SWITCH_REASON_DATA_RAT_CHANGE = 4;
    public static final int DATA_SWITCH_REASON_DATA_SETUP_FAILURE = 1;
    public static final int DATA_SWITCH_REASON_DATA_STALL_HAPPENED = 3;
    public static final int DATA_SWITCH_REASON_SIGNAL_STRENGTH_CHANGE = 5;
    public static final int DATA_SWITCH_REASON_VOICE_CALL_ON_DATA_SUB = 6;
    public static final boolean DBG = true;
    public static final String DEFAULT_DATA_SWITCH_FORBIDDEN_INTERVAL = "default_randomization=200,120000,240000,360000,480000,600000,720000";
    public static final int EVDO_QOS_CFG_INDEX = 3;
    public static final int EVENT_AIRPLANE_STATE_CHANGE = 12;
    public static final int EVENT_DATA_CELL_LOCATION_CHANGE = 8;
    public static final int EVENT_DATA_SERVICE_STATE_CHANGE = 6;
    public static final int EVENT_DATA_SIGNAL_STRENGTH_CHANGE = 7;
    public static final int EVENT_DATA_STALL_HAPPENED = 2;
    public static final int EVENT_DATA_STATE_CHANGE = 18;
    public static final int EVENT_DATA_STATE_CONNECTED = 0;
    public static final int EVENT_DATA_SWITCH_ALLOWED = 9;
    public static final int EVENT_DATA_VOICE_CALL_STATE_CHANGE = 17;
    public static final int EVENT_DEFAULT_DATA_DISCONNECTED_FAILURE = 3;
    public static final int EVENT_DEFAULT_DATA_SETUP_FAILURE = 4;
    public static final int EVENT_DEFAULT_DATA_SUB_CHANGED = 1;
    public static final int EVENT_IDS_STATE_CHANGE = 10;
    public static final int EVENT_NO_DATA_SWITCH_RESPONSE = 15;
    public static final int EVENT_ROAMING_STATE_CHANGE = 11;
    public static final int EVENT_TRIGGER_DATA_SWITCH = 5;
    public static final int EVENT_USER_CHANGE_DATA_SERVICE_STATE = 14;
    public static final int EVENT_VOICE_CALL_ON_DATA_SUB = 16;
    public static final int EVENT_WIFI_STATE_CHANGE = 13;
    public static final int FIRST_CHECK_VOICE_CALL_UI_ON_TOP_TIMER = 20000;
    public static final int GENERATION_FOUR = 4;
    public static final int GENERATION_THREE = 3;
    public static final int GENERATION_TWO = 2;
    public static final int GENERATION_UNKNOWN = 0;
    public static final int GOOD_CDMA_ECIO = -12;
    public static final int GOOD_EVDO_SNR = 7;
    public static final int GOOD_LTE_SIGNAL_QUALITY_VALUE = -10;
    public static final int GOOD_LTE_SIGNAL_STRENGTH_VALUE = -90;
    public static final int GOOD_SIGNAL_QUALITY_INDEX = 2;
    public static final int GOOD_SIGNAL_QUALITY_VALUE = -10;
    public static final int GOOD_SIGNAL_STRENGTH_INDEX = 0;
    public static final int GOOD_SIGNAL_STRENGTH_VALUE = -85;
    public static final int GSM_QOS_CFG_INDEX = 2;
    public static final int GSM_STRENGTH_UNKOUWN = 99;
    public static final int INVALID_VALUE = -1;
    public static final int LTE_QOS_CFG_INDEX = 0;
    public static final int LTE_STRENGTH_UNKOUWN = -44;
    public static final int MAX_DATA_SWITCH_INTERVAL_COUNT = 6;
    public static final int MAX_QOS_CONFIG_FOR_EACH_RAT = 4;
    public static final int MAX_RAT_QOS_CONFIG = 5;
    public static final int MAX_SLOT_ID = 2;
    public static final int MAX_TIME_STAY_IN_SWITCH_ONGOING_STATUS = 120000;
    public static final int MAX_WAIT_DS_RECOVER_TIMER = 2;
    public static final int NO_DELAY = 0;
    public static final int ONE_MINUTE_TO_WAIT = 60000;
    public static final int RAT_CDMA = 2;
    public static final int RAT_EVDO = 3;
    public static final int RAT_GSM = 1;
    public static final int RAT_LTE = 6;
    public static final int RAT_TDS = 4;
    public static final int RAT_UNKNOWN = 0;
    public static final int RAT_WCDMA = 5;
    public static final int SIGNAL_MAX_WEAK_COUNT = 5;
    public static final int SIGNAL_MAX_WEAK_TIMER = 20000;
    public static final int SLOT_0 = 0;
    public static final int SLOT_1 = 1;
    public static final int STRENGTH_IS_NONE = 0;
    public static final int TS_W_QOS_CFG_INDEX = 1;
    public static final int TWO_MINUTE_TO_WAIT = 120000;
    public static final String WAIT_DS_RECOVERY_TIMER = null;
    public static final int WEAK_CDMA_ECIO = -15;
    public static final int WEAK_EVDO_SNR = 1;
    public static final int WEAK_LTE_SIGNAL_QUALITY_VALUE = -15;
    public static final int WEAK_LTE_SIGNAL_STRENGTH_VALUE = -115;
    public static final int WEAK_SIGNAL_QUALITY_INDEX = 3;
    public static final int WEAK_SIGNAL_QUALITY_VALUE = -15;
    public static final int WEAK_SIGNAL_STRENGTH_INDEX = 1;
    public static final int WEAK_SIGNAL_STRENGTH_VALUE = -100;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.intelligentdataswitch.IDSConstants.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.intelligentdataswitch.IDSConstants.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.intelligentdataswitch.IDSConstants.<clinit>():void");
    }

    public static void logd(String tag, String msg) {
        Rlog.d("IDS module", "[" + tag + "]: " + msg);
    }

    public static void loge(String tag, String msg) {
        Rlog.e("IDS module", "[" + tag + "]: " + msg);
    }
}
