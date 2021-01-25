package com.huawei.server.security.permissionmanager.util;

import com.huawei.android.util.SlogEx;

public class PermissionType {
    public static final long ACCESS_BROWSER_RECORDS = 1073741824;
    public static final long ADDVIEW_SWITCH = 17179869184L;
    public static final long CALL_FORWARD = 1048576;
    public static final long CALL_PHONE = 64;
    public static final long EDIT_SHORTCUT = 16777216;
    public static final long GET_ACCOUNTS = 512;
    public static final long PHONE_OTHERS = 65536;
    public static final long PROCESS_OUTGOING_CALLS = 8589934592L;
    public static final long READ_CALL_LOG = 2;
    public static final long READ_CONTACTS = 1;
    public static final long READ_PHONE_STATE = 16;
    public static final long READ_SMS = 4;
    public static final long RECEIVE_SMS = 4096;
    public static final long REQUEST_INSTALL_PACKAGES = 4294967296L;
    public static final long SEND_MMS = 8192;
    public static final long SEND_SMS = 32;
    public static final long SMS_OTHERS = 131072;
    public static final long SYSTEM_ALERT_WINDOW = 536870912;
    private static final String TAG = "PermissionType";
    public static final long WRITE_CALL_LOG = 32768;
    public static final long WRITE_CONTACTS = 16384;

    private PermissionType() {
        SlogEx.d(TAG, "create PermissionType");
    }
}
