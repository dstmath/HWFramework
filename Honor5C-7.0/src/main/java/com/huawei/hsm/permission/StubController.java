package com.huawei.hsm.permission;

import android.content.Context;
import android.hsm.HwSystemManager;
import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeInfo;
import android.util.Jlog;
import android.util.Log;
import android.util.LruCache;
import com.huawei.permission.IHoldService;
import com.huawei.permission.IHoldService.Stub;

public class StubController {
    public static final int AGGRESSIVE_DEFENSE_OFF = 1;
    public static final int AGGRESSIVE_DEFENSE_ON = 0;
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
    public static final int PERMISSION_MOBILEDATE = 4194304;
    public static final int PERMISSION_MODIFY_CALENDAR = 268435456;
    public static final int PERMISSION_NONE = 0;
    public static final int PERMISSION_NOTIFICATION = 4096;
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
    public static final int RHD_PERMISSION_CODE = 134217728;
    public static final int RMD_PERMISSION_CODE = 67108864;
    private static final int SEND_GROUP_MMS = 1001;
    private static final int SEND_GROUP_SMS = 1000;
    public static final int SEND_INTENT_VALUE = 0;
    public static final String SYSTEM_APP = "/system/app";
    private static final int SYSTEM_SERVER_UID = 1000;
    public static final String TABLE_COLUM_KEY = "key";
    public static final String TABLE_COLUM_NOTIFICATION_CFG = "sound_vibrate";
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
    private static int mLocationRequestCount;
    private static int mPhoneIDRequestCount;
    private static int mRequestCount;
    private static Object mRequestCountSync;
    private static LruCache<Integer, Boolean> permissionCache;
    private static Object syncObj;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.StubController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.StubController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.StubController.<clinit>():void");
    }

    private static void addRequestCount(int permissionType) {
        synchronized (mRequestCountSync) {
            mRequestCount += USER_ALLOWED;
            if (PERMISSION_GET_DEVICEID == permissionType) {
                mPhoneIDRequestCount += USER_ALLOWED;
            }
            if (PERMISSION_LOCATION == permissionType) {
                mLocationRequestCount += USER_ALLOWED;
            }
        }
    }

    private static void minusRequestCount(int permissionType) {
        synchronized (mRequestCountSync) {
            mRequestCount += REMIND_FAIL;
            if (PERMISSION_GET_DEVICEID == permissionType) {
                mPhoneIDRequestCount += REMIND_FAIL;
            }
            if (PERMISSION_LOCATION == permissionType) {
                mLocationRequestCount += REMIND_FAIL;
            }
        }
    }

    private static boolean letCurrentRequestGoOrNot(int permissionType) {
        synchronized (mRequestCountSync) {
            if (MOST_BINDER_COUNT_FOR_PHONE < mRequestCount && PERMISSION_GET_DEVICEID == permissionType) {
                return true;
            } else if (MOST_BINDER_COUNT_FOR_BOTH_LOCATION_AND_PHONE >= mPhoneIDRequestCount || PERMISSION_GET_DEVICEID != permissionType) {
                return DBG;
            } else {
                return true;
            }
        }
    }

    private static boolean blockCurrentRequestOrNot(int permissionType) {
        synchronized (mRequestCountSync) {
            if (MOST_BINDER_COUNT_FOR_BOTH_LOCATION_AND_PHONE >= mLocationRequestCount || PERMISSION_LOCATION != permissionType) {
                return DBG;
            }
            return true;
        }
    }

    public static boolean checkPreBlock(int callUid, int permissionType, boolean showToast) {
        int validUid = handleIncomingUser(callUid);
        IHoldService hService = getHoldServiceByUid(callUid);
        if (hService == null) {
            Log.e(TAG, "checkPreBlock, service = null");
            return DBG;
        }
        try {
            return hService.checkPreBlock(validUid, permissionType, showToast);
        } catch (Exception e) {
            e.printStackTrace();
            return DBG;
        }
    }

    public static boolean checkPreBlock(int callUid, int permissionType) {
        return checkPreBlock(callUid, permissionType, true);
    }

    public static boolean checkPrecondition(int uid) {
        if (HwSystemManager.mPermissionEnabled == 0 || checkSystemAppInternal(uid, DBG)) {
            return DBG;
        }
        return true;
    }

    private static boolean checkSystemAppInternal(int callUid, boolean notUsed) {
        if (SYSTEM_SERVER_UID == callUid) {
            return true;
        }
        Boolean cachedValue = (Boolean) permissionCache.get(Integer.valueOf(callUid));
        if (cachedValue != null) {
            return cachedValue.booleanValue();
        }
        int validUid = handleIncomingUser(callUid);
        IHoldService hService = getHoldServiceByUid(callUid);
        if (hService == null) {
            Log.e(TAG, "service = null");
            return true;
        }
        try {
            Boolean isFriend = Boolean.valueOf(hService.checkSystemAppInternal(validUid, notUsed));
            permissionCache.put(Integer.valueOf(callUid), isFriend);
            return isFriend.booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static IHoldService getHoldService() {
        return getHoldServiceByUid(Process.myUid());
    }

    private static IHoldService getHoldServiceByUid(int uid) {
        int myUid = UserHandle.getUserId(uid);
        String servicekey = "com.huawei.permissionmanager.service.holdservice";
        if (myUid != 0) {
            servicekey = servicekey + myUid;
        }
        try {
            IBinder b = ServiceManager.getService(servicekey);
            if (b == null) {
                return null;
            }
            return Stub.asInterface(b);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean callerIsProxy(int callerUid) {
        int currentUid = Process.myUid();
        if (callerUid == currentUid || currentUid < MIN_APPLICATION_UID) {
            return DBG;
        }
        return true;
    }

    private static int handleIncomingUser(int uid) {
        return callerIsProxy(uid) ? Process.myUid() : uid;
    }

    private static int handleIncomingPid(int uid, int pid) {
        return callerIsProxy(uid) ? Process.myPid() : pid;
    }

    public static int holdForGetPermissionSelection(int permissionType, int uid, int pid, String desAddr) {
        int i;
        int validUid = handleIncomingUser(uid);
        int validPid = handleIncomingPid(uid, pid);
        IHoldService hService = getHoldServiceByUid(validUid);
        if (hService == null) {
            Log.e(TAG, "service = null");
            return USER_IGNORED;
        }
        addRequestCount(permissionType);
        try {
            handleANRFilterFIFO(validUid, USER_IGNORED);
            int preCheckCode = hService.checkBeforeShowDialogWithPid(validUid, validPid, permissionType, desAddr);
            if (Log.HWLog) {
                i = mRequestCountSync;
                synchronized (i) {
                    Log.d(TAG, "preCheckCode:" + preCheckCode + ", validUid " + validUid + ", validPid " + validPid + ", permissionType " + permissionType);
                }
            }
            if (USER_ALLOWED == preCheckCode || USER_REFUSED == preCheckCode) {
                minusRequestCount(permissionType);
                try {
                    handleANRFilterFIFO(validUid, USER_ALLOWED);
                    return preCheckCode;
                } catch (Exception e) {
                    e.printStackTrace();
                    return USER_IGNORED;
                }
            }
            int newPermissionType = permissionType;
            if (SYSTEM_SERVER_UID == preCheckCode || SEND_GROUP_MMS == preCheckCode) {
                newPermissionType = preCheckCode;
            }
            if (letCurrentRequestGoOrNot(permissionType)) {
                minusRequestCount(permissionType);
                try {
                    handleANRFilterFIFO(validUid, USER_ALLOWED);
                    return USER_ALLOWED;
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return USER_IGNORED;
                }
            } else if (blockCurrentRequestOrNot(permissionType)) {
                minusRequestCount(permissionType);
                try {
                    handleANRFilterFIFO(validUid, USER_ALLOWED);
                    return USER_REFUSED;
                } catch (Exception e22) {
                    e22.printStackTrace();
                    return USER_IGNORED;
                }
            } else {
                int holdResult;
                long showDialogTime = SystemClock.uptimeMillis();
                i = syncObj;
                synchronized (i) {
                    holdResult = hService.holdServiceByRequestPermission(validUid, validPid, newPermissionType, desAddr);
                    if (Log.HWLog) {
                        Log.d(TAG, "holdResult:" + holdResult);
                    }
                }
                if (SystemClock.uptimeMillis() - showDialogTime >= 200) {
                    Jlog.d(AppTypeInfo.PG_APP_TYPE_SCRLOCK, "holdServiceByRequestPermission");
                }
                minusRequestCount(permissionType);
                try {
                    handleANRFilterFIFO(validUid, USER_ALLOWED);
                    return holdResult;
                } catch (Exception e222) {
                    e222.printStackTrace();
                    return USER_IGNORED;
                }
            }
        } catch (NullPointerException e3) {
            try {
                e3.printStackTrace();
                try {
                    handleANRFilterFIFO(validUid, i);
                    return USER_IGNORED;
                } catch (Exception e2222) {
                    e2222.printStackTrace();
                    return USER_IGNORED;
                }
            } finally {
                minusRequestCount(permissionType);
                i = USER_ALLOWED;
                try {
                    handleANRFilterFIFO(validUid, USER_ALLOWED);
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                    return USER_IGNORED;
                }
            }
        } catch (Exception e222222) {
            e222222.printStackTrace();
            minusRequestCount(permissionType);
            try {
                handleANRFilterFIFO(validUid, USER_ALLOWED);
                return USER_IGNORED;
            } catch (Exception e2222222) {
                e2222222.printStackTrace();
                return USER_IGNORED;
            }
        }
    }

    private static void handleANRFilterFIFO(int uid, int cmd) throws RemoteException {
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            IBinder ams = ServiceManager.getService(FreezeScreenScene.ACTIVITY_PARAM);
            if (ams == null) {
                Log.e(TAG, "invalid AMS binder");
                return;
            }
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInterfaceToken("android.app.IActivityManager");
            parcel.writeInt(uid);
            parcel.writeInt(cmd);
            ams.transact(502, parcel, parcel2, USER_IGNORED);
            parcel2.readException();
        } finally {
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        }
    }

    public static synchronized boolean isGlobalSwitchOn(Context context, int permissionType) {
        synchronized (StubController.class) {
        }
        return true;
    }
}
