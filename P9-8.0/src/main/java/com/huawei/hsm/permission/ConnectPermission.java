package com.huawei.hsm.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import com.huawei.hsm.permission.minimms.PduParser;

public class ConnectPermission {
    public static final boolean DEBUG = false;
    public static final String HOTALK_CLASS = "com.hotalk.ui.chat.singleChat.SingleChatActivity";
    public static final String MMS_CLASS = "com.android.mms.ui.ComposeMessageActivity";
    public static final String MMS_PACKAGE = "com.huawei.message";
    private static final int ONE_MMS = 1;
    public static final int PERMISSION_MMS = 8192;
    private static final String SEND_MUTIL_MMS_STATUS = "true";
    public static final String TAG = ConnectPermission.class.getSimpleName();
    public static final boolean isControl = SystemProperties.getBoolean("ro.config.hw_wirenetcontrol", true);
    private Context mContext;

    public ConnectPermission(Context context) {
        this.mContext = context;
    }

    public static boolean isBlocked(int type, int uid, int pid) {
        if (!isControl || !StubController.checkPrecondition(uid)) {
            return false;
        }
        int remindResult = StubController.holdForGetPermissionSelection(type, uid, pid, null);
        return (remindResult == 0 || 1 == remindResult || 2 != remindResult) ? false : true;
    }

    public boolean isBlocked(byte[] pduDataStream) {
        if (!isControl) {
            return false;
        }
        if (StubController.checkPreBlock(Binder.getCallingUid(), 8192)) {
            return true;
        }
        if (!StubController.checkPrecondition(Binder.getCallingUid())) {
            return false;
        }
        String desAddr = null;
        if (1 < new PduParser(pduDataStream).getTargetCount()) {
            desAddr = SEND_MUTIL_MMS_STATUS;
        }
        int remindResult = StubController.holdForGetPermissionSelection(8192, Binder.getCallingUid(), Binder.getCallingPid(), desAddr);
        return (remindResult == 0 || 1 == remindResult || 2 != remindResult) ? false : true;
    }

    public static boolean blockStartActivity(Context context, Intent intent) {
        if (!isControl || !intentToMms(intent)) {
            return isControl && intentToSms(intent) && StubController.checkPreBlock(Binder.getCallingUid(), 32);
        } else {
            if (StubController.checkPreBlock(Binder.getCallingUid(), 8192)) {
                return true;
            }
            ConnectPermission wnp = new ConnectPermission(context);
            if (isBlocked(8192, Binder.getCallingUid(), Binder.getCallingPid())) {
                return true;
            }
            PackageManager pm = context.getPackageManager();
            if (pm.queryIntentActivities(intent, StubController.PERMISSION_SMSLOG_WRITE).size() <= 0) {
                ComponentName componentName = new ComponentName(MMS_PACKAGE, HOTALK_CLASS);
                try {
                    if (pm.getActivityInfo(componentName, 0) != null) {
                        intent.setComponent(componentName);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "can not found com.hotalk.ui.chat.singleChat.SingleChatActivity");
                }
            }
            return false;
        }
    }

    private static boolean intentToMms(Intent intent) {
        if (!"android.intent.action.SEND".equals(intent.getAction())) {
            return false;
        }
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return false;
        }
        return MMS_CLASS.equals(componentName.getClassName());
    }

    private static boolean intentToSms(Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.SENDTO".equals(action) || "android.intent.action.VIEW".equals(action)) {
            String scheme;
            if (intent.getData() != null) {
                scheme = intent.getData().getScheme();
            } else {
                scheme = null;
            }
            if (scheme != null && (scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase("smsto"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean allowOpenBt(Context cxt) {
        return cxt == null || Global.getInt(cxt.getContentResolver(), "bluetooth_on", 0) == 1 || !isBlocked(StubController.PERMISSION_BLUETOOTH, Binder.getCallingUid(), Binder.getCallingPid());
    }

    public static boolean allowOpenMobile(Context cxt) {
        return cxt == null || Global.getInt(cxt.getContentResolver(), "mobile_data", 0) == 1 || !isBlocked(StubController.PERMISSION_MOBILEDATE, Binder.getCallingUid(), Binder.getCallingPid());
    }

    public static boolean allowOpenWifi(Context cxt) {
        if (!(cxt == null || Global.getInt(cxt.getContentResolver(), "wifi_on", 0) == 1)) {
            boolean blocked = isBlocked(StubController.PERMISSION_WIFI, Binder.getCallingUid(), Binder.getCallingPid());
            Log.i(TAG, "allowOpenWifi blocked:" + blocked);
            if (blocked) {
                return false;
            }
        }
        return true;
    }
}
