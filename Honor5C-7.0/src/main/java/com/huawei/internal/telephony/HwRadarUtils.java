package com.huawei.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.BaseMmsColumns;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.util.LogException;

public class HwRadarUtils {
    private static final String APP_VERSION = "1.0";
    private static final String CAT_CSP = "csp";
    private static final String CAT_MMS = "mms";
    private static final String CHR_ACTION = "com.huawei.android.chr.action.ACTION_REPORT_CHR";
    private static final String CHR_RECEIVE_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final String DEFAULT_PACKAGE = "com.android.mms";
    public static final int ERROR_BASE_MMS = 1300;
    public static final int ERR_SMS_RECEIVE = 1312;
    public static final int ERR_SMS_SEND = 1311;
    public static final int ERR_SMS_SEND_BACKGROUND = 1317;
    private static final int MODULE_ID = 5000;
    private static final int RADAR_BUG_TYPE_FUNCTION_ERROR = 100;
    public static final int RADAR_LEVEL_A = 65;
    public static final int RADAR_LEVEL_B = 66;
    public static final int RADAR_LEVEL_C = 67;
    public static final int RADAR_LEVEL_D = 68;
    private static final String TAG = "HwRadarUtils";
    private static LogException mLogException;
    private static final String sAppInfo = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.internal.telephony.HwRadarUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.internal.telephony.HwRadarUtils.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.internal.telephony.HwRadarUtils.<clinit>():void");
    }

    private HwRadarUtils() {
    }

    public static void report(int errorType, String content) {
        report(null, errorType, content, 0);
    }

    public static void report(int errorType, String content, int subId) {
        report(null, errorType, content, subId);
    }

    public static void report(Context context, int errorType, String content) {
        report(context, errorType, content, SubscriptionManager.getDefaultSmsSubscriptionId());
    }

    public static void report(Context context, int errorType, String content, int subId) {
        String header = sAppInfo + "Bug type: " + String.valueOf(transalateErrorToBugType(errorType)) + "\n" + "Scene def: " + String.valueOf(errorType) + "\n";
        if (isNeedToTriggerAppRadar(errorType)) {
            reportApplicationRadarLog(header, content);
        }
        if (isNeedToTriggerCHR(errorType)) {
            reportChr(context, errorType, content, subId);
        }
    }

    private static final int transalateErrorToBugType(int error) {
        return RADAR_BUG_TYPE_FUNCTION_ERROR;
    }

    private static boolean isNeedToTriggerAppRadar(int errorType) {
        Rlog.d(TAG, "isNeedToTriggerAppRadar for error:" + errorType);
        switch (errorType) {
            case ERR_SMS_SEND /*1311*/:
                Rlog.d(TAG, "not need to trigger applicaton radar log");
                return false;
            case ERR_SMS_RECEIVE /*1312*/:
            case ERR_SMS_SEND_BACKGROUND /*1317*/:
                Rlog.d(TAG, "need to trigger applicaton radar log");
                return true;
            default:
                Rlog.d(TAG, "Not need to trigger APP Radar");
                return false;
        }
    }

    private static boolean isNeedToTriggerCHR(int errorType) {
        Rlog.d(TAG, "isNeedToTriggerCHR for error:" + errorType);
        switch (errorType) {
            case ERR_SMS_RECEIVE /*1312*/:
            case ERR_SMS_SEND_BACKGROUND /*1317*/:
                Rlog.d(TAG, "need To TriggerCHR");
                return true;
            default:
                Rlog.d(TAG, "not need to trigger CHR");
                return false;
        }
    }

    public static void reportApplicationRadarLog(String header, String msg) {
        reportApplicationRadarLog(CAT_MMS, RADAR_LEVEL_A, header, msg);
    }

    public static void reportApplicationRadarLog(String category, int level, String header, String msg) {
        if (mLogException != null) {
            try {
                Rlog.w(TAG, "radar report in FW:" + msg);
                mLogException.msg(category, level, header, msg);
                return;
            } catch (Throwable e) {
                Rlog.e(TAG, "call radar interface has exception" + e.getMessage());
                return;
            }
        }
        Rlog.e(TAG, "Radar interface is not support");
    }

    private static void reportChr(Context context, int errorType, String content, int subId) {
        if (context == null || -1 == subId) {
            Rlog.e(TAG, "para is error, not to trigger reportChr");
            return;
        }
        Intent intent = new Intent(CHR_ACTION);
        intent.putExtra("module_id", MODULE_ID);
        intent.putExtra("event_id", errorType);
        intent.putExtra(BaseMmsColumns.SUBJECT, subId);
        intent.putExtra("app_data", content);
        context.sendBroadcast(intent, CHR_RECEIVE_PERMISSION);
        Rlog.d(TAG, "reportChr in FW done");
    }
}
