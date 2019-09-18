package com.huawei.hsm.permission;

import android.content.Context;
import android.hsm.HwSystemManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AppTypeInfo;
import android.rms.iaware.AppTypeRecoManager;
import android.util.Jlog;
import android.util.Log;
import android.util.LruCache;
import com.huawei.android.app.HwActivityManager;
import com.huawei.permission.IHoldService;
import com.huawei.permission.IHsmMaliAppInfoListener;
import com.huawei.permission.MaliInfoBean;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
    /* access modifiers changed from: private */
    public static IHoldService mHoldService;
    private static int mLocationRequestCount = 0;
    private static int mPhoneIDRequestCount = 0;
    /* access modifiers changed from: private */
    public static int mRequestCount = 0;
    private static Object mRequestCountSync = new Object();
    private static HoldServiceDeathRecipient mServiceDeathRecipient = new HoldServiceDeathRecipient();
    private static ArrayList<HoldServiceDieListener> mServiceDieCallbacks = new ArrayList<>();
    private static int mUseridOfMoniterBinder;
    private static LruCache<Integer, Boolean> permissionCache = new LruCache<>(5);
    private static Object syncObj = new Object();

    private static final class HoldServiceDeathRecipient implements IBinder.DeathRecipient {
        private HoldServiceDeathRecipient() {
        }

        public void binderDied() {
            Log.e(StubController.TAG, "binderDied HoldService Die!");
            StubController.notifyBinderDie();
            IHoldService unused = StubController.mHoldService = null;
        }
    }

    public interface HoldServiceDieListener {
        void notifyServiceDie();
    }

    /* access modifiers changed from: private */
    public static void notifyBinderDie() {
        ArrayList<HoldServiceDieListener> listeners = new ArrayList<>();
        synchronized (mServiceDieCallbacks) {
            listeners.addAll(mServiceDieCallbacks);
        }
        int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).notifyServiceDie();
        }
    }

    /* access modifiers changed from: private */
    public static void addRequestCount(int permissionType) {
        synchronized (mRequestCountSync) {
            mRequestCount++;
            if (16 == permissionType) {
                mPhoneIDRequestCount++;
            }
            if (8 == permissionType) {
                mLocationRequestCount++;
            }
        }
    }

    /* access modifiers changed from: private */
    public static void minusRequestCount(int permissionType) {
        synchronized (mRequestCountSync) {
            mRequestCount--;
            if (16 == permissionType) {
                mPhoneIDRequestCount--;
            }
            if (8 == permissionType) {
                mLocationRequestCount--;
            }
        }
    }

    private static boolean letCurrentRequestGoOrNot(int permissionType) {
        synchronized (mRequestCountSync) {
            if (10 < mRequestCount && 16 == permissionType) {
                return true;
            }
            if (6 >= mPhoneIDRequestCount || 16 != permissionType) {
                return false;
            }
            return true;
        }
    }

    private static boolean blockCurrentRequestOrNot(int permissionType) {
        synchronized (mRequestCountSync) {
            if (6 >= mLocationRequestCount || 8 != permissionType) {
                return false;
            }
            return true;
        }
    }

    public static boolean checkPreBlock(int callUid, int permissionType, boolean showToast) {
        int validUid = handleIncomingUser(callUid);
        IHoldService hService = getHoldServiceByUid(callUid);
        if (hService == null) {
            Log.e(TAG, "checkPreBlock, service = null");
            return false;
        }
        try {
            return hService.checkPreBlock(validUid, permissionType, showToast);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public static void holdForInsertBroadcastRecord(String pkgName, final int permissionType, int uid) {
        final IHoldService hService = getHoldServiceByUid(handleIncomingUser(uid));
        if (hService == null) {
            Log.e(TAG, "holdForInsertBroadcastRecord service = null");
            return;
        }
        final Bundle bundle = new Bundle();
        bundle.putString("packageName", pkgName);
        bundle.putInt("permissionType", permissionType);
        bundle.putInt("appUid", uid);
        new Thread(new Runnable() {
            public void run() {
                try {
                    StubController.addRequestCount(permissionType);
                    if (StubController.mRequestCount > 16) {
                        StubController.minusRequestCount(permissionType);
                        Log.i(StubController.TAG, "holdForInsertBroadcastRecord mRequestCount = " + StubController.mRequestCount);
                        return;
                    }
                    hService.callHsmService(StubController.HOLDSERVICE_METHOD, bundle);
                    StubController.minusRequestCount(permissionType);
                } catch (RemoteException e) {
                } catch (Exception e2) {
                } finally {
                    StubController.minusRequestCount(permissionType);
                }
            }
        }).start();
    }

    public static void addServiceDieListener(HoldServiceDieListener listener) {
        synchronized (mServiceDieCallbacks) {
            if (listener != null) {
                try {
                    mServiceDieCallbacks.add(listener);
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
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

    public static void recordPermissionUsage(Bundle bundle) {
        IHoldService hService = getHoldService();
        if (hService == null) {
            Log.e(TAG, "service = null");
            return;
        }
        try {
            hService.callHsmService("call_hsm_permission_usage_moniter", bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "recordPermissionUsage remote_excepton : ", e);
        } catch (Exception e2) {
            Log.e(TAG, "recordPermissionUsage excepton : ", e2);
        }
    }

    public static IHoldService getHoldService() {
        return getHoldServiceByUid(Process.myUid());
    }

    private static synchronized IHoldService getHoldServiceByUid(int uid) {
        synchronized (StubController.class) {
            int callingUserId = UserHandle.getUserId(uid);
            if (mHoldService == null || callingUserId != mUseridOfMoniterBinder) {
                getHoldServiceByUidInner(callingUserId);
                IHoldService iHoldService = mHoldService;
                return iHoldService;
            }
            IHoldService iHoldService2 = mHoldService;
            return iHoldService2;
        }
    }

    private static void getHoldServiceByUidInner(int callingUserId) {
        try {
            IBinder b = ServiceManager.getService("com.huawei.permissionmanager.service.holdservice");
            if (b != null) {
                boolean hasLinkToDeath = false;
                try {
                    b.unlinkToDeath(mServiceDeathRecipient, 0);
                } catch (NoSuchElementException e) {
                    b.linkToDeath(mServiceDeathRecipient, 0);
                    hasLinkToDeath = true;
                }
                mHoldService = IHoldService.Stub.asInterface(b);
                mUseridOfMoniterBinder = callingUserId;
                if (!hasLinkToDeath) {
                    b.linkToDeath(mServiceDeathRecipient, 0);
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "getHoldServiceByUidInner : ", e2);
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

    public static int holdForGetPermissionSelection(int permissionType, int uid, int pid, String desAddr) {
        int holdResult;
        int i = permissionType;
        String str = desAddr;
        int validUid = handleIncomingUser(uid);
        int validPid = handleIncomingPid(uid, pid);
        IHoldService hService = getHoldServiceByUid(validUid);
        if (hService == null) {
            Log.e(TAG, "service = null");
            return 0;
        }
        addRequestCount(permissionType);
        try {
            handleANRFilterFIFO(validUid, 0);
            int preCheckCode = hService.checkBeforeShowDialogWithPid(validUid, validPid, i, str);
            if (Log.HWLog) {
                synchronized (mRequestCountSync) {
                    Log.d(TAG, "preCheckCode:" + preCheckCode + ", validUid " + validUid + ", validPid " + validPid + ", permissionType " + i);
                }
            }
            if (1 != preCheckCode) {
                if (2 != preCheckCode) {
                    int newPermissionType = i;
                    if (1000 == preCheckCode || 1001 == preCheckCode) {
                        newPermissionType = preCheckCode;
                    }
                    if (letCurrentRequestGoOrNot(permissionType)) {
                        minusRequestCount(permissionType);
                        try {
                            handleANRFilterFIFO(validUid, 1);
                            return 1;
                        } catch (Exception e) {
                            Exception exc = e;
                            e.printStackTrace();
                            return 0;
                        }
                    } else if (blockCurrentRequestOrNot(permissionType)) {
                        minusRequestCount(permissionType);
                        handleANRFilterFIFO(validUid, 1);
                        return 2;
                    } else {
                        long showDialogTime = SystemClock.uptimeMillis();
                        synchronized (syncObj) {
                            holdResult = hService.holdServiceByRequestPermission(validUid, validPid, newPermissionType, str);
                            if (Log.HWLog) {
                                Log.d(TAG, "holdResult:" + holdResult);
                            }
                        }
                        if (SystemClock.uptimeMillis() - showDialogTime >= 200) {
                            Jlog.d(AppTypeInfo.PG_APP_TYPE_SCRLOCK, "holdServiceByRequestPermission");
                        }
                        minusRequestCount(permissionType);
                        handleANRFilterFIFO(validUid, 1);
                        return holdResult;
                    }
                }
            }
            minusRequestCount(permissionType);
            handleANRFilterFIFO(validUid, 1);
            return preCheckCode;
        } catch (NullPointerException e2) {
            e2.printStackTrace();
            minusRequestCount(permissionType);
            handleANRFilterFIFO(validUid, 1);
            return 0;
        } catch (Exception e3) {
            try {
                e3.printStackTrace();
                return 0;
            } finally {
                minusRequestCount(permissionType);
                handleANRFilterFIFO(validUid, 1);
            }
        }
    }

    public static int authenticateSmsSend(IBinder notifyResult, int uidOf3RdApk, int smsId, String smsBody, String smsAddress) {
        int validUid = handleIncomingUser(uidOf3RdApk);
        int callingUserId = UserHandle.getUserId(validUid);
        getHoldServiceByUidInner(callingUserId);
        Log.d(TAG, "authenticateSmsSend appUid = " + validUid + " userId = " + callingUserId);
        int retVal = 0;
        if (mHoldService == null) {
            Log.e(TAG, "service = null");
            notifyBinderDie();
            return 1;
        }
        try {
            mHoldService.authenticateSmsSend(notifyResult, uidOf3RdApk, smsId, smsBody, smsAddress);
        } catch (RemoteException e) {
            retVal = 1;
            Log.e(TAG, "authenticateSmsSend remote_excepton : " + e);
        } catch (Exception e2) {
            retVal = 1;
            Log.e(TAG, "authenticateSmsSend excepton : " + e2);
        }
        return retVal;
    }

    public static void notifyBackgroundMgr(String pkgName, int pid, int uidOf3RdApk, int permType, int permCfg) {
        int validUid = handleIncomingUser(uidOf3RdApk);
        if (validUid >= 10000) {
            int validPid = handleIncomingPid(uidOf3RdApk, pid);
            IHoldService hService = getHoldServiceByUid(validUid);
            if (hService == null) {
                Log.e(TAG, "service = null");
                return;
            }
            try {
                Bundle bundle = new Bundle();
                bundle.putString(AppTypeRecoManager.APP_PKGNAME, pkgName);
                bundle.putInt("pid", validPid);
                bundle.putInt("uid", uidOf3RdApk);
                bundle.putInt("permType", permType);
                bundle.putInt("permCfg", permCfg);
                hService.callHsmService("call_hsm_background_moniter", bundle);
            } catch (RemoteException e) {
                Log.e(TAG, "notifyBackgroundMgr remote_excepton : ", e);
            } catch (Exception e2) {
                Log.e(TAG, "notifyBackgroundMgr excepton : ", e2);
            }
        }
    }

    public static List<String> queryAllMaliPkgs() {
        IHoldService hService = getHoldService();
        if (hService != null) {
            try {
                return hService.queryAllMaliPkgs();
            } catch (RemoteException e) {
                Log.e(TAG, "queryAllMaliPkgs : ", e);
            }
        }
        return new ArrayList();
    }

    public static List<MaliInfoBean> queryMaliAppInfoByPkg(String packageName, int flags) {
        IHoldService hService = getHoldService();
        if (hService != null) {
            try {
                return hService.queryMaliAppInfoByPkg(packageName, flags);
            } catch (RemoteException e) {
                Log.e(TAG, "queryMaliAppInfoByPkg : ", e);
            }
        }
        return new ArrayList();
    }

    public static List<MaliInfoBean> queryMaliAppInfoShort(int flags) {
        IHoldService hService = getHoldService();
        if (hService != null) {
            try {
                return hService.queryMaliAppInfoShort(flags);
            } catch (RemoteException e) {
                Log.e(TAG, "queryMaliAppInfoShort : ", e);
            }
        }
        return new ArrayList();
    }

    public static void registMaliAppInfoListener(IHsmMaliAppInfoListener listener, int flags, int priority) {
        IHoldService hService = getHoldService();
        if (hService == null) {
            Log.e(TAG, "registMaliAppInfoListener, service = null");
            return;
        }
        try {
            hService.registMaliAppInfoListener(listener, listener.hashCode(), flags, priority);
        } catch (Exception e) {
            Log.e(TAG, "registMaliAppInfoListener : ", e);
        }
    }

    public static void unregistMaliAppInfoListener(IHsmMaliAppInfoListener listener) {
        IHoldService hService = getHoldService();
        if (hService == null) {
            Log.e(TAG, "unregistMaliciousListener, service = null");
            return;
        }
        try {
            hService.unregistMaliAppInfoListener(listener.hashCode());
        } catch (Exception e) {
            Log.e(TAG, "unregistMaliAppInfoListener : ", e);
        }
    }

    public static void setRestrictStatus(String packageName, boolean isRestricted) {
        IHoldService hService = getHoldService();
        if (hService == null) {
            Log.e(TAG, "setRestrictStatus, service = null");
            return;
        }
        try {
            hService.setRestrictStatus(packageName, isRestricted);
        } catch (Exception e) {
            Log.e(TAG, "setRestrictStatus : ", e);
        }
    }

    public static String[] getAlwaysForbiddenPerms() {
        IHoldService hService = getHoldService();
        if (hService != null) {
            try {
                return hService.getAlwaysForbiddenPerms();
            } catch (Exception e) {
                Log.e(TAG, "getAlwaysForbiddenPerms : ", e);
            }
        }
        return new String[0];
    }

    private static void handleANRFilterFIFO(int uid, int cmd) {
        HwActivityManager.handleANRFilterFIFO(uid, cmd);
    }

    public static synchronized boolean isGlobalSwitchOn(Context context, int permissionType) {
        synchronized (StubController.class) {
        }
        return true;
    }
}
