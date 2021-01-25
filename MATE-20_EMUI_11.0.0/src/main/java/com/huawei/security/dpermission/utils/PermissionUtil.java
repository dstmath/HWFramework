package com.huawei.security.dpermission.utils;

import java.util.HashSet;
import java.util.Set;

public final class PermissionUtil {
    private static final int DEFAULT_PERMISSION_SET_SIZE = 16;
    private static final int DEFAULT_REPORT_PERMISSION_SET_SIZE = 30;
    private static final String DISTRIBUTED_VIRTUALDEVICE = "com.huawei.permission.DISTRIBUTED_VIRTUALDEVICE";
    private static final Set<String> D_PERMISSION_BLOCK_SET = new HashSet(16);
    private static final Set<String> D_PERMISSION_OP_TRUST_SET = new HashSet(16);
    private static final String PERMISSION_ACCESS_NOTIFICATIONS = "android.permission.ACCESS_NOTIFICATIONS";
    private static final String PERMISSION_MANAGE_IPSEC_TUNNELS = "android.permission.MANAGE_IPSEC_TUNNELS";
    private static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    private static final Set<String> REPORT_PERMISSION_NAMES = new HashSet(30);

    static {
        D_PERMISSION_BLOCK_SET.add("com.android.launcher.permission.INSTALL_SHORTCUT");
        D_PERMISSION_BLOCK_SET.add("com.android.launcher.permission.UNINSTALL_SHORTCUT");
        D_PERMISSION_BLOCK_SET.add("android.permission.SYSTEM_ALERT_WINDOW");
        D_PERMISSION_BLOCK_SET.add("android.permission.REQUEST_INSTALL_PACKAGES");
        D_PERMISSION_BLOCK_SET.add("android.permission.WRITE_SETTINGS");
        D_PERMISSION_BLOCK_SET.add("android.permission.INSTANT_APP_FOREGROUND_SERVICE");
        D_PERMISSION_BLOCK_SET.add("android.permission.SMS_FINANCIAL_TRANSACTIONS");
        D_PERMISSION_BLOCK_SET.add("android.permission.PACKAGE_USAGE_STATS");
        D_PERMISSION_BLOCK_SET.add(PERMISSION_MANAGE_IPSEC_TUNNELS);
        REPORT_PERMISSION_NAMES.add("android.permission.ACCESS_BACKGROUND_LOCATION");
        REPORT_PERMISSION_NAMES.add("android.permission.ACCESS_COARSE_LOCATION");
        REPORT_PERMISSION_NAMES.add("android.permission.ACCESS_FINE_LOCATION");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_CONTACTS");
        REPORT_PERMISSION_NAMES.add("android.permission.WRITE_CONTACTS");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_CALL_LOG");
        REPORT_PERMISSION_NAMES.add("android.permission.WRITE_CALL_LOG");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_CALENDAR");
        REPORT_PERMISSION_NAMES.add("android.permission.WRITE_CALENDAR");
        REPORT_PERMISSION_NAMES.add("android.permission.CALL_PHONE");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_SMS");
        REPORT_PERMISSION_NAMES.add("android.permission.RECEIVE_SMS");
        REPORT_PERMISSION_NAMES.add("android.permission.RECEIVE_MMS");
        REPORT_PERMISSION_NAMES.add("android.permission.RECEIVE_WAP_PUSH");
        REPORT_PERMISSION_NAMES.add("android.permission.SEND_SMS");
        REPORT_PERMISSION_NAMES.add("android.permission.CAMERA");
        REPORT_PERMISSION_NAMES.add("android.permission.RECORD_AUDIO");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_PHONE_STATE");
        REPORT_PERMISSION_NAMES.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        REPORT_PERMISSION_NAMES.add("android.permission.USE_SIP");
        REPORT_PERMISSION_NAMES.add("android.permission.BODY_SENSORS");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_EXTERNAL_STORAGE");
        REPORT_PERMISSION_NAMES.add("android.permission.WRITE_EXTERNAL_STORAGE");
        REPORT_PERMISSION_NAMES.add("android.permission.GET_ACCOUNTS");
        REPORT_PERMISSION_NAMES.add("android.permission.READ_PHONE_NUMBERS");
        REPORT_PERMISSION_NAMES.add("android.permission.ANSWER_PHONE_CALLS");
        REPORT_PERMISSION_NAMES.add("android.permission.ACCEPT_HANDOVER");
        REPORT_PERMISSION_NAMES.add("android.permission.ACTIVITY_RECOGNITION");
        REPORT_PERMISSION_NAMES.add(PROCESS_OUTGOING_CALLS);
        REPORT_PERMISSION_NAMES.add(DISTRIBUTED_VIRTUALDEVICE);
        D_PERMISSION_OP_TRUST_SET.add(PERMISSION_ACCESS_NOTIFICATIONS);
    }

    private PermissionUtil() {
    }

    public static boolean isBlockPermission(String str) {
        return D_PERMISSION_BLOCK_SET.contains(str);
    }

    public static boolean isTrustOp(String str) {
        return D_PERMISSION_OP_TRUST_SET.contains(str);
    }

    public static Set<String> getReportPermissionNames() {
        return REPORT_PERMISSION_NAMES;
    }
}
