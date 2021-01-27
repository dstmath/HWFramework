package com.huawei.hsm.permission;

import android.content.Context;
import android.hsm.HwSystemManager;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.util.LruCache;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.permission.IHoldService;
import huawei.android.security.IHwPermissionManager;
import huawei.android.security.IHwSecurityService;

public class StubController {
    public static final int AGGRESSIVE_DEFENSE_OFF = 1;
    public static final int AGGRESSIVE_DEFENSE_ON = 0;
    public static final int ANR_FILTER_FIFO = 502;
    public static final String APP_GOOGLE = "com.android";
    public static final String APP_HUAWEI = "com.huawei";
    private static final int CACHE_SIZE = 5;
    public static final int COMMON_VALUE_INTENT_KEY = 20121109;
    public static final String CONTENT_COMMON_URI = "content://com.huawei.permissionmanager.provider.PermissionDataProvider/common";
    public static final String CONTENT_LOG_URI = "content://com.huawei.permissionmanager.provider.PermissionDataProvider/log";
    public static final String CONTENT_NOTIFICATIONMGR_URI = "content://com.huawei.systemmanager.NotificationDBProvider/notificationCfg";
    public static final String CONTENT_URI = "content://com.huawei.permissionmanager.provider.PermissionDataProvider/permission";
    public static final String CUST_URL = "/data/cust";
    private static final boolean DBG = false;
    private static final String HOLDSERVICE_METHOD = "record_permission_method";
    private static final int HW_CONTROL_PERMISSION = 1102061568;
    private static final int HW_PERM_PLUGIN_ID = 17;
    private static final Object LOCK = new Object();
    public static final int MIN_APPLICATION_UID = 10000;
    private static final int MOST_BINDER_COUNT_FOR_BOTH_LOCATION_AND_PHONE = 6;
    private static final int MOST_BINDER_COUNT_FOR_PHONE = 10;
    public static final int NOTIFICATION_FLOG_SOUND = 1;
    public static final int NOTIFICATION_FLOG_VIBRATE = 2;
    public static final String PATH = "/data/data/com.huawei.permissionmanager/databases/permission.db";
    public static final int PERMISSION_ACCESS_3G = 256;
    public static final int PERMISSION_ACCESS_BROWSER_RECORDS = 1073741824;
    public static final int PERMISSION_ACCESS_WIFI = 512;
    public static final int PERMISSION_ACTION_CALL = 64;
    public static final int PERMISSION_BLUETOOTH = 8388608;
    public static final int PERMISSION_CALENDAR = 2048;
    public static final int PERMISSION_CALLLOG = 2;
    public static final int PERMISSION_CALLLOG_DELETE = 262144;
    public static final int PERMISSION_CALLLOG_WRITE = 32768;
    public static final int PERMISSION_CALL_FORWARD = 1048576;
    public static final int PERMISSION_CALL_LISTENER = 128;
    public static final int PERMISSION_CAMERA = 1024;
    public static final int PERMISSION_CONTACTS = 1;
    public static final int PERMISSION_CONTACTS_DELETE = 131072;
    public static final int PERMISSION_CONTACTS_WRITE = 16384;
    public static final int PERMISSION_DELETE_CALENDAR = 536870912;
    public static final int PERMISSION_EDIT_SHORTCUT = 16777216;
    public static final int PERMISSION_GET_DEVICEID = 16;
    public static final int PERMISSION_GET_PACKAGE_LIST = 33554432;
    public static final int PERMISSION_LOCATION = 8;
    private static final long PERMISSION_MASK = 4294967295L;
    public static final int PERMISSION_MOBILEDATE = 4194304;
    public static final int PERMISSION_MODIFY_CALENDAR = 268435456;
    public static final int PERMISSION_NONE = 0;
    public static final int PERMISSION_RECEIVE_SMS = 4096;
    public static final int PERMISSION_SEND_MMS = 8192;
    public static final int PERMISSION_SEND_SMS = 32;
    public static final int PERMISSION_SMSLOG = 4;
    public static final int PERMISSION_SMSLOG_DELETE = 524288;
    public static final int PERMISSION_SMSLOG_WRITE = 65536;
    public static final int PERMISSION_TYPE_ALLOWED = 1;
    public static final int PERMISSION_TYPE_BLOCKED = 2;
    public static final int PERMISSION_TYPE_FAIL = 0;
    public static final int PERMISSION_TYPE_UNKNOWN = -1;
    public static final int PERMISSION_WIFI = 2097152;
    public static final int REMIND_FAIL = -1;
    public static final int REMIND_SKIP = 1;
    public static final int REMIND_SUCCESS = 0;
    public static final int RETURN_VALUE_FAILD = 1;
    public static final int RETURN_VALUE_SECCESS = 0;
    public static final int RHD_PERMISSION_CODE = 134217728;
    public static final int RMD_PERMISSION_CODE = 67108864;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final int SEND_GROUP_MMS = 1001;
    private static final int SEND_GROUP_SMS = 1000;
    public static final int SEND_INTENT_VALUE = 0;
    public static final String SYSTEM_APP = "/system/app";
    private static final int SYSTEM_SERVER_UID = 1000;
    public static final String TABLE_COLUM_CHANNEL_BYPASSDND = "channelbypassdnd";
    public static final String TABLE_COLUM_CHANNEL_ICONBADGE = "channeliconbadge";
    public static final String TABLE_COLUM_CHANNEL_ID = "channelid";
    public static final String TABLE_COLUM_CHANNEL_IMPORTANCE = "channelimportance";
    public static final String TABLE_COLUM_KEY = "key";
    public static final String TABLE_COLUM_NOTIFICATION_CFG = "sound_vibrate";
    public static final String TABLE_COLUM_NOTIFICATION_LOCKSCREEN_CFG = "lockscreencfg";
    public static final String TABLE_COLUM_PACKAGE_NAME = "packageName";
    public static final String TABLE_COLUM_PERMISSION_CFG = "permissionCfg";
    public static final String TABLE_COLUM_PERMISSION_CODE = "permissionCode";
    public static final String TABLE_COLUM_TRUST = "trust";
    public static final String TABLE_COLUM_UID = "uid";
    public static final String TABLE_COLUM_VALUE = "value";
    public static final String TABLE_COMMON_COLUM_KEY = "key";
    public static final String TABLE_COMMON_COLUM_VALUE = "value";
    public static final String TABLE_NAME_COMMON = "commonTable";
    public static final String TABLE_NAME_PERMSSION = "permissionCfg";
    static final String TAG = "StubController";
    public static final int USER_ALLOWED = 1;
    public static final int USER_IGNORED = 0;
    public static final int USER_REFUSED = 2;
    private static int mLocationRequestCount = 0;
    private static int mPhoneIDRequestCount = 0;
    private static int mRequestCount = 0;
    private static Object mRequestCountSync = new Object();
    private static LruCache<Integer, Boolean> permissionCache = new LruCache<>(5);
    private static volatile IHwPermissionManager sIHwPermissionManager;
    private static Object syncObj = new Object();

    public static boolean checkPreBlock(int callUid, int permissionType, boolean showToast) {
        return false;
    }

    public static boolean checkPreBlock(int callUid, int permissionType) {
        return checkPreBlock(callUid, permissionType, true);
    }

    public static boolean checkPrecondition(int uid) {
        if (HwSystemManager.mPermissionEnabled != 0 && !checkSystemAppInternal(uid, false)) {
            return true;
        }
        return false;
    }

    public static boolean checkPreconditionPermissionEnabled() {
        if (HwSystemManager.mPermissionEnabled == 0) {
            return true;
        }
        return false;
    }

    private static boolean checkSystemAppInternal(int callUid, boolean notUsed) {
        if (1000 == callUid) {
            return true;
        }
        Boolean cachedValue = permissionCache.get(Integer.valueOf(callUid));
        if (cachedValue != null) {
            return cachedValue.booleanValue();
        }
        int validUid = handleIncomingUser(callUid);
        IHwPermissionManager permissionManager = getHwPermissionService();
        if (permissionManager == null) {
            Log.e(TAG, "service = null");
            return true;
        }
        try {
            Boolean isFriend = Boolean.valueOf(!permissionManager.shouldMonitor(validUid));
            permissionCache.put(Integer.valueOf(callUid), isFriend);
            Log.i(TAG, "system app validUid:" + validUid + ", isFriend:" + isFriend);
            return isFriend.booleanValue();
        } catch (Exception e) {
            Log.e(TAG, "checkSystemAppInternal exception");
            return true;
        }
    }

    public static IHoldService getHoldService() {
        return getHoldServiceInner();
    }

    private static synchronized IHoldService getHoldServiceInner() {
        synchronized (StubController.class) {
            IHoldService holdService = null;
            try {
                IBinder binder = ServiceManagerEx.getService("com.huawei.permissionmanager.service.holdservice");
                if (binder == null) {
                    return null;
                }
                holdService = IHoldService.Stub.asInterface(binder);
                return holdService;
            } catch (Exception e) {
                Log.e(TAG, "getHoldServiceInner exception!");
            }
        }
    }

    private static boolean callerIsProxy(int callerUid) {
        int currentUid = Process.myUid();
        return callerUid != currentUid && currentUid >= 10000;
    }

    public static int handleIncomingUser(int uid) {
        return callerIsProxy(uid) ? Process.myUid() : uid;
    }

    public static int handleIncomingPid(int uid, int pid) {
        return callerIsProxy(uid) ? Process.myPid() : pid;
    }

    public static int holdForGetPermissionSelection(int permissionType, int uid, int pid) {
        Log.i(TAG, "holdForGetPermissionSelection permissionType:" + permissionType);
        int holdResult = 0;
        int validUid = handleIncomingUser(uid);
        int validPid = handleIncomingPid(uid, pid);
        if (checkSystemAppInternal(validUid, false)) {
            return 1;
        }
        if (getHwPermissionService() == null) {
            return 0;
        }
        Log.i(TAG, "holdForGetPermissionSelection hwPermissionManager is not null");
        try {
            handleANRFilterFIFO(validUid, 0);
            holdResult = sIHwPermissionManager.holdServiceByRequestPermission(validUid, validPid, ((long) permissionType) & PERMISSION_MASK);
        } catch (RemoteException e) {
            Log.e(TAG, "holdForGetPermissionSelection RemoteException e:" + e);
        } catch (Throwable th) {
            handleANRFilterFIFO(validUid, 1);
            throw th;
        }
        handleANRFilterFIFO(validUid, 1);
        return holdResult;
    }

    public static int holdForGetPermissionSelection(int permissionType, int uid, int pid, String desAddr) {
        if (isHwControlPermission(permissionType)) {
            try {
                IHwPermissionManager permissionManager = getHwPermissionService();
                if (permissionManager == null) {
                    Log.e(TAG, "service = null");
                    return 1;
                }
                int result = permissionManager.checkHwPermission(uid, pid, permissionType);
                Log.v(TAG, "holdForGetPermissionSelection permissionType: " + permissionType + ", result: " + result);
                return result;
            } catch (RemoteException e) {
                Log.e(TAG, "holdForGetPermissionSelection" + e.getMessage());
                return 1;
            }
        } else {
            Log.v(TAG, "hsm should not control this permission: " + permissionType);
            return 1;
        }
    }

    public static int authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) {
        if (sIHwPermissionManager == null) {
            sIHwPermissionManager = getHwPermissionService();
        }
        try {
            sIHwPermissionManager.authenticateSmsSend(notifyResult, uidOf3RdApk, smsId, smsBody, smsAddress);
            return 0;
        } catch (RemoteException e) {
            Log.e(TAG, "authenticateSmsSend remote exception");
            return 1;
        } catch (Exception e2) {
            Log.e(TAG, "authenticateSmsSend excepton");
            return 1;
        }
    }

    private static void handleANRFilterFIFO(int uid, int cmd) {
        HwActivityManager.handleANRFilterFIFO(uid, cmd);
    }

    public static synchronized boolean isGlobalSwitchOn(Context context, int permissionType) {
        synchronized (StubController.class) {
        }
        return true;
    }

    private static boolean isHwControlPermission(int permissionType) {
        return (HW_CONTROL_PERMISSION & permissionType) != 0;
    }

    private static IHwPermissionManager getHwPermissionService() {
        synchronized (LOCK) {
            if (sIHwPermissionManager != null) {
                return sIHwPermissionManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sIHwPermissionManager = IHwPermissionManager.Stub.asInterface(secService.querySecurityInterface(17));
                } catch (RemoteException e) {
                    Log.e(TAG, "Get HwPermissionService failed!");
                }
            }
            return sIHwPermissionManager;
        }
    }
}
