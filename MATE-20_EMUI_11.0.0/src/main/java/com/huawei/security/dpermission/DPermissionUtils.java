package com.huawei.security.dpermission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DPermissionUtils {
    private static final long ADDVIEW_SWITCH = 17179869184L;
    private static final long EDIT_SHORTCUT_TYPE = 16777216;
    private static final Set<String> NEED_SYNC_OP_SET = new HashSet();
    private static final Set<String> NOT_SYNC_PERMISSION_SET = new HashSet();
    private static final Set<Long> NOT_SYNC_PERMISSION_TYPE_SET = new HashSet();
    private static final String PERMISSION_ACCESS_NOTIFICATIONS = "android.permission.ACCESS_NOTIFICATIONS";
    private static final String PERMISSION_INSTALL_SHORTCUT = "com.android.launcher.permission.INSTALL_SHORTCUT";
    private static final String PERMISSION_INSTANT_APP_FOREGROUND_SERVICE = "android.permission.INSTANT_APP_FOREGROUND_SERVICE";
    private static final String PERMISSION_MANAGE_IPSEC_TUNNELS = "android.permission.MANAGE_IPSEC_TUNNELS";
    private static final Map<String, Long> PERMISSION_NAME_TO_TYPE_MAP = new HashMap();
    private static final String PERMISSION_PACKAGE_USAGE_STATS = "android.permission.PACKAGE_USAGE_STATS";
    private static final String PERMISSION_REQUEST_INSTALL_PACKAGES = "android.permission.REQUEST_INSTALL_PACKAGES";
    private static final String PERMISSION_SMS_FINANCIAL_TRANSACTIONS = "android.permission.SMS_FINANCIAL_TRANSACTIONS";
    private static final String PERMISSION_SYSTEM_ALERT_WINDOW = "android.permission.SYSTEM_ALERT_WINDOW";
    private static final String PERMISSION_UNINSTALL_SHORTCUT = "com.android.launcher.permission.UNINSTALL_SHORTCUT";
    private static final String PERMISSION_WRITE_SETTINGS = "android.permission.WRITE_SETTINGS";
    private static final long REQUEST_INSTALL_PACKAGES = 4294967296L;
    private static final long SYSTEM_ALERT_WINDOW = 536870912;

    static {
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_INSTALL_SHORTCUT);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_UNINSTALL_SHORTCUT);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_SYSTEM_ALERT_WINDOW);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_REQUEST_INSTALL_PACKAGES);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_WRITE_SETTINGS);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_INSTANT_APP_FOREGROUND_SERVICE);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_SMS_FINANCIAL_TRANSACTIONS);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_PACKAGE_USAGE_STATS);
        NOT_SYNC_PERMISSION_SET.add(PERMISSION_MANAGE_IPSEC_TUNNELS);
        NEED_SYNC_OP_SET.add(PERMISSION_ACCESS_NOTIFICATIONS);
        Set<Long> set = NOT_SYNC_PERMISSION_TYPE_SET;
        Long valueOf = Long.valueOf((long) EDIT_SHORTCUT_TYPE);
        set.add(valueOf);
        NOT_SYNC_PERMISSION_TYPE_SET.add(Long.valueOf((long) SYSTEM_ALERT_WINDOW));
        NOT_SYNC_PERMISSION_TYPE_SET.add(Long.valueOf((long) REQUEST_INSTALL_PACKAGES));
        NOT_SYNC_PERMISSION_TYPE_SET.add(Long.valueOf((long) ADDVIEW_SWITCH));
        PERMISSION_NAME_TO_TYPE_MAP.put(PERMISSION_INSTALL_SHORTCUT, valueOf);
        PERMISSION_NAME_TO_TYPE_MAP.put(PERMISSION_UNINSTALL_SHORTCUT, valueOf);
    }

    public static Set<String> getNotSyncPermissionSet() {
        return NOT_SYNC_PERMISSION_SET;
    }

    public static Set<String> getNeedSyncOpSet() {
        return NEED_SYNC_OP_SET;
    }

    public static Set<String> getHwPermissionSet() {
        return PERMISSION_NAME_TO_TYPE_MAP.keySet();
    }

    public static Set<Long> getNotSyncPermissionTypeSet() {
        return NOT_SYNC_PERMISSION_TYPE_SET;
    }

    public static Long getPermissionTypeByName(String str) {
        return PERMISSION_NAME_TO_TYPE_MAP.get(str);
    }
}
