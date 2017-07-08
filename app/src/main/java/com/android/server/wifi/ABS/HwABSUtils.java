package com.android.server.wifi.ABS;

import android.util.Log;

public class HwABSUtils {
    public static final String ABS_PROP = "ro.config.hw_abs_enable";
    public static final String ACTION_WIFI_ANTENNA_PREEMPTED = "com.huawei.action.ACTION_WIFI_ANTENNA_PREEMPTED";
    public static final int AUTO_HANDOVER_TIMER = 20000;
    public static final int CAPBILITY_MIMO = 2;
    public static final int CAPBILITY_SISO = 1;
    public static final int CMD_WIFI_HANDOVER_SISO = 103;
    public static final int CMD_WIFI_SWITCH_MIMO = 101;
    public static final int CMD_WIFI_SWITCH_SISO = 102;
    public static final boolean DBG = true;
    public static final String FLAG = "flag";
    public static final int HANDOVER_CAPBILITY_ATION_FRAME = 1;
    public static final int HANDOVER_CAPBILITY_RECONNECT = 2;
    public static final int HANDOVER_CAPBILITY_UNKONW = 0;
    public static final String HUAWEI_BUSSINESS_PERMISSION = "com.huawei.permission.HUAWEI_BUSSINESS_PERMISSION";
    public static final String MODEM0 = "modem0";
    public static final String MODEM1 = "modem1";
    public static final String MODEM2 = "modem2";
    public static final int MSG_CALL_STATE_IDLE = 8;
    public static final int MSG_CALL_STATE_OFFHOOK = 10;
    public static final int MSG_CALL_STATE_RINGING = 9;
    public static final int MSG_MODEM_ENTER_CONNECT_STATE = 11;
    public static final int MSG_MODEM_ENTER_SEARCHING_STATE = 13;
    public static final int MSG_MODEM_EXIT_CONNECT_STATE = 12;
    public static final int MSG_MODEM_EXIT_SEARCH = 20;
    public static final int MSG_MODEM_EXIT_SEARCHING_STATE = 14;
    public static final int MSG_MODEM_STATE_POWER_OFF = 21;
    public static final int MSG_MODEM_TRANSITION_TO_SEARCH = 19;
    public static final int MSG_OUTGOING_CALL = 7;
    public static final int MSG_SCREEN_OFF = 6;
    public static final int MSG_SCREEN_ON = 5;
    public static final int MSG_WIFI_ANTENNA_PREEMPTED = 15;
    public static final int MSG_WIFI_CHECK_LINK = 16;
    public static final int MSG_WIFI_CHECK_LINK_FAILED = 18;
    public static final int MSG_WIFI_CHECK_LINK_SUCCESS = 17;
    public static final int MSG_WIFI_CONNECTED = 1;
    public static final int MSG_WIFI_DISABLE = 4;
    public static final int MSG_WIFI_DISCONNECTED = 2;
    public static final int MSG_WIFI_ENABLED = 3;
    public static final String RES = "res";
    public static final int SEARCHING_STATE_INVALID = 4;
    public static final int SEARCHING_STATE_IN_SERVICE = 0;
    public static final int SEARCHING_STATE_LIMITED_SERVICE = 1;
    public static final int SEARCHING_STATE_NO_SERVICE = 2;
    public static final int SEARCHING_STATE_TERMINATE = 3;
    public static final String SUB_ID = "subId";
    public static final String TAG = "HwABSUtils";
    public static final boolean isABSEnable = false;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.ABS.HwABSUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.ABS.HwABSUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.ABS.HwABSUtils.<clinit>():void");
    }

    public static void logD(String info) {
        Log.d(TAG, info);
    }

    public static void logW(String info) {
        Log.w(TAG, info);
    }

    public static void logE(String info) {
        Log.e(TAG, info);
    }

    public static boolean getABSEnable() {
        return isABSEnable;
    }
}
