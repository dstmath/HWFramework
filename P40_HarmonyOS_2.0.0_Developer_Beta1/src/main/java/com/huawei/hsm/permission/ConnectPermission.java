package com.huawei.hsm.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.provider.Settings;
import com.huawei.android.os.SystemPropertiesEx;

public class ConnectPermission {
    public static final boolean DEBUG = false;
    public static final String HOTALK_CLASS = "com.hotalk.ui.chat.singleChat.SingleChatActivity";
    public static final String MMS_CLASS = "com.android.mms.ui.ComposeMessageActivity";
    public static final String MMS_PACKAGE = "com.huawei.message";
    private static final int ONE_MMS = 1;
    public static final int PERMISSION_MMS = 8192;
    private static final String SEND_MUTIL_MMS_STATUS = "true";
    public static final String TAG = ConnectPermission.class.getSimpleName();
    private static final int WIFI_STATUS_OFF = 0;
    private static final int WIFI_STATUS_ON = 1;
    public static final boolean isControl = SystemPropertiesEx.getBoolean("ro.config.hw_wirenetcontrol", true);
    private Context mContext;

    public ConnectPermission() {
    }

    public ConnectPermission(Context context) {
        this.mContext = context;
    }

    public static boolean isBlocked(int type, int uid, int pid) {
        int remindResult;
        if (isControl && (remindResult = StubController.holdForGetPermissionSelection(type, uid, pid, null)) != 0 && 1 != remindResult && 2 == remindResult) {
            return true;
        }
        return false;
    }

    public boolean isBlocked(byte[] pduDataStream) {
        return false;
    }

    public static boolean blockStartActivity(Context context, Intent intent) {
        return false;
    }

    private static boolean intentToMms(Intent intent) {
        ComponentName componentName;
        if ("android.intent.action.SEND".equals(intent.getAction()) && (componentName = intent.getComponent()) != null) {
            return MMS_CLASS.equals(componentName.getClassName());
        }
        return false;
    }

    private static boolean intentToSms(Intent intent) {
        String action = intent.getAction();
        if (!"android.intent.action.SENDTO".equals(action) && !"android.intent.action.VIEW".equals(action)) {
            return false;
        }
        String scheme = intent.getData() != null ? intent.getData().getScheme() : null;
        if (scheme == null) {
            return false;
        }
        if (scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase("smsto")) {
            return true;
        }
        return false;
    }

    public static boolean allowOpenBt(Context cxt) {
        if (cxt == null || Settings.Global.getInt(cxt.getContentResolver(), "bluetooth_on", 0) == 1 || !isBlocked(StubController.PERMISSION_BLUETOOTH, Binder.getCallingUid(), Binder.getCallingPid())) {
            return true;
        }
        return false;
    }

    public static boolean allowOpenMobile(Context cxt) {
        if (cxt == null || Settings.Global.getInt(cxt.getContentResolver(), "mobile_data", 0) == 1 || !isBlocked(StubController.PERMISSION_MOBILEDATE, Binder.getCallingUid(), Binder.getCallingPid())) {
            return true;
        }
        return false;
    }

    public static boolean allowOpenWifi(Context cxt) {
        if (cxt == null || Settings.Global.getInt(cxt.getContentResolver(), "wifi_on", 0) == 1 || 2 != StubController.holdForGetPermissionSelection(StubController.PERMISSION_WIFI, Binder.getCallingUid(), Binder.getCallingPid())) {
            return true;
        }
        return false;
    }
}
