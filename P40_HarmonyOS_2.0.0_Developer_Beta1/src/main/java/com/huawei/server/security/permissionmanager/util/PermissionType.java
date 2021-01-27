package com.huawei.server.security.permissionmanager.util;

import com.huawei.android.util.SlogEx;

public class PermissionType {
    public static final long ACCESS_BROWSER_RECORDS = 1073741824;
    public static final long ACTIVITY_BACKGROUND = 68719476736L;
    public static final long ACTIVITY_LOCKSCREEN = 137438953472L;
    public static final long ACTIVITY_RECOGNITION = 274877906944L;
    public static final long ADDVIEW_SWITCH = 17179869184L;
    public static final long ADD_VOICEMAIL = 65536;
    public static final long ANSWER_PHONE_CALLS = 65536;
    public static final long BLUETOOTH = 8388608;
    public static final long BODY_SENSORS = 134217728;
    public static final long CALL_FORWARD = 1048576;
    public static final long CALL_PHONE = 64;
    public static final long CALL_RECORD = 128;
    public static final long CAMERA = 1024;
    public static final long CHANGE_WIFI_STATE = 2097152;
    public static final long EDIT_SHORTCUT = 16777216;
    public static final long GET_ACCOUNTS = 512;
    public static final long GET_PACKAGE_LIST = 33554432;
    public static final long INVALID = -1;
    public static final long LOCATION = 8;
    public static final long LOCATION_BACKGROUND = 2199023255552L;
    public static final long LOCATION_FOREGROUND = 8;
    public static final long MEDIA_AURAL = 549755813888L;
    public static final long MEDIA_VISUAL = 1099511627776L;
    public static final long MODIFY_PHONE_STATE = 4194304;
    public static final long NO_PERMISSION_RECOMMEND = 524288;
    public static final long PHONE_OTHERS = 65536;
    public static final long PROCESS_OUTGOING_CALLS = 8589934592L;
    public static final long READ_CALENDAR = 2048;
    public static final long READ_CALL_LOG = 2;
    public static final long READ_CONTACTS = 1;
    public static final long READ_MOTION_DATA = 67108864;
    public static final long READ_PHONE_NUMBERS = 65536;
    public static final long READ_PHONE_STATE = 16;
    public static final long READ_SMS = 4;
    public static final long RECEIVE_MMS = 131072;
    public static final long RECEIVE_SMS = 4096;
    public static final long RECEIVE_WAP_PUSH = 131072;
    public static final long RECORD_AUDIO = 128;
    public static final long REQUEST_INSTALL_PACKAGES = 4294967296L;
    public static final long SEND_MMS = 8192;
    public static final long SEND_SMS = 32;
    public static final long SMS_OTHERS = 131072;
    public static final long STORAGE = 256;
    public static final long SYSTEM_ALERT_WINDOW = 536870912;
    private static final String TAG = "PermissionType";
    public static final long TOAST_BACKGROUND = 34359738368L;
    public static final long USE_SIP = 65536;
    public static final long WRITE_CALENDAR = 268435456;
    public static final long WRITE_CALL_LOG = 32768;
    public static final long WRITE_CONTACTS = 16384;

    private PermissionType() {
        SlogEx.d(TAG, "create PermissionType");
    }
}
