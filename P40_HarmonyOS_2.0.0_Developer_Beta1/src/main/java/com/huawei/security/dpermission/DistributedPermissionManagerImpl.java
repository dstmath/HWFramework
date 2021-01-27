package com.huawei.security.dpermission;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.dpermission.DistributedPermissionManager;
import com.huawei.security.dpermission.IDistributedPermissionManager;
import com.huawei.security.dpermission.IHwDPermission;
import com.huawei.security.dpermission.IRequestPermissionsResult;
import com.huawei.security.dpermission.permissionusingremind.IOnPermissionUsingReminder;
import com.huawei.security.dpermission.permissionusingremind.OnPermissionUsingReminder;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DistributedPermissionManagerImpl {
    private static final int DEFAULT_SIZE = 5;
    private static final int GENERAL_ERROR = -1;
    private static final Object LOCK = new Object();
    private static final int MAX_NOTIFY_TIMEOUT = 200;
    private static final int PERMISSION_DENIED = -1;
    private static final int PERMISSION_GRANTED = 0;
    private static final int PERMISSION_REQUEST_NOT_FINISH = -11;
    private static final String TAG = "DistributedPermissionManagerImpl";
    private static final int USER_ID_DISTRIBUTED_FROM_A = 126;
    private static final int USER_ID_DISTRIBUTED_FROM_Z = 125;
    private static IDPermissionManager sInnerServiceImpl;
    private static DistributedPermissionManagerImpl sInstance = new DistributedPermissionManagerImpl();
    private static IHwDPermission sProxiedService = null;
    private static IDistributedPermissionManager sService = null;
    private boolean isRequestingPermission = false;
    private ConcurrentHashMap<OnPermissionUsingReminder, OnPermissionUsingReminderDelegate> mUsingPermissionReminders = new ConcurrentHashMap<>(5);

    static {
        DistributedPermissionManagerImpl distributedPermissionManagerImpl = sInstance;
        distributedPermissionManagerImpl.getClass();
        sInnerServiceImpl = new InnerServiceImpl();
    }

    private DistributedPermissionManagerImpl() {
        Log.i(TAG, "DistributedPermissionManagerImpl called.");
    }

    /* access modifiers changed from: private */
    public static IDistributedPermissionManager getService() {
        synchronized (LOCK) {
            if (sService != null) {
                return sService;
            }
            try {
                IBinder binder = ServiceManagerEx.getService("distributedPermission");
                if (binder != null) {
                    sService = IDistributedPermissionManager.Stub.asInterface(binder);
                    Log.i(TAG, "Get service binder = " + binder);
                    binder.linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass1 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (DistributedPermissionManagerImpl.LOCK) {
                                IDistributedPermissionManager unused = DistributedPermissionManagerImpl.sService = null;
                                Log.w(DistributedPermissionManagerImpl.TAG, "distributedPermission is died.");
                            }
                        }
                    }, 0);
                }
                return sService;
            } catch (RemoteException e) {
                Log.e(TAG, "getService occurs RemoteException");
                return null;
            } catch (Exception e2) {
                Log.e(TAG, "getService occurs Exception");
                return null;
            } catch (Error e3) {
                Log.e(TAG, "getService occurs Error");
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    public static IHwDPermission getProxiedService() {
        synchronized (LOCK) {
            if (sProxiedService != null) {
                return sProxiedService;
            }
            try {
                IBinder binder = ServiceManagerEx.getService("com.huawei.security.dpermission.service.HwDPermissionService");
                if (binder != null) {
                    sProxiedService = IHwDPermission.Stub.asInterface(binder);
                    Log.i(TAG, "Get proxied binder = " + binder);
                    binder.linkToDeath(new IBinder.DeathRecipient() {
                        /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass2 */

                        @Override // android.os.IBinder.DeathRecipient
                        public void binderDied() {
                            synchronized (DistributedPermissionManagerImpl.LOCK) {
                                IHwDPermission unused = DistributedPermissionManagerImpl.sProxiedService = null;
                                Log.w(DistributedPermissionManagerImpl.TAG, "proxied service is died.");
                            }
                        }
                    }, 0);
                }
                return sProxiedService;
            } catch (RemoteException e) {
                Log.e(TAG, "getProxiedService occurs RemoteException");
                return null;
            } catch (Exception e2) {
                Log.e(TAG, "getProxiedService occurs Exception");
                return null;
            } catch (Error e3) {
                Log.e(TAG, "getProxiedService occurs Error");
                return null;
            }
        }
    }

    static DistributedPermissionManagerImpl getDefault() {
        return sInstance;
    }

    public static IDPermissionManager getInnerServiceImpl() {
        return sInnerServiceImpl;
    }

    /* access modifiers changed from: package-private */
    public int allocateDuid(String nodeId, int rUid) {
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.allocateDuid(nodeId, rUid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "AllocateDuid error" + e.getMessage());
        }
        Log.e(TAG, "Failed to allocate duid!");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int queryDuid(String nodeId, int rUid) {
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.queryDuid(nodeId, rUid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "QueryDuid error" + e.getMessage());
        }
        Log.e(TAG, "Failed to query duid!");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int notifyDeviceStatusChanged(String nodeId, int status) {
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.notifyDeviceStatusChanged(nodeId, status);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "notifyDeviceStatusChanged error" + e.getMessage());
        }
        Log.e(TAG, "Failed to notify device status changed!");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public boolean isTargetDevice(String nodeId, int uid) {
        Log.d(TAG, "isTargetDevice() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.isTargetDevice(nodeId, uid);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "isTargetDevice error " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void addTargetDevice(String nodeId, int uid) {
        Log.d(TAG, "addTargetDevice() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                service.addTargetDevice(nodeId, uid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "addTargetDevice error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public int notifySyncPermission(String nodeId, int uid, String packageName) {
        Log.d(TAG, "notifySyncPermission() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.notifySyncPermission(nodeId, uid, packageName);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "notifySyncPermission error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int waitDuidReady(String nodeId, int rUid, int timeout) {
        Log.d(TAG, "waitDuidReady() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.waitDuidReady(nodeId, rUid, timeout);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "waitDuidReady error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int verifyPermissionFromRemote(String permission, String nodeId, String appIdInfo) {
        Log.d(TAG, "verifyPermissionFromRemote() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.verifyPermissionFromRemote(permission, nodeId, appIdInfo);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "verifyPermissionFromRemote error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int verifySelfPermissionFromRemote(String permission, String nodeId) {
        Log.d(TAG, "verifySelfPermissionFromRemote() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.verifySelfPermissionFromRemote(permission, nodeId);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "verifySelfPermissionFromRemote error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean canRequestPermissionFromRemote(String permission, String nodeId) {
        Log.d(TAG, "canRequestPermissionFromRemote() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.canRequestPermissionFromRemote(permission, nodeId);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "canRequestPermissionFromRemote error " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearRequestingPermissionFlag() {
        synchronized (LOCK) {
            this.isRequestingPermission = false;
        }
    }

    private boolean isOtherRequestExecuting() {
        boolean haveOtherRequestExecuting = false;
        synchronized (LOCK) {
            if (this.isRequestingPermission) {
                haveOtherRequestExecuting = true;
            } else {
                this.isRequestingPermission = true;
            }
        }
        return haveOtherRequestExecuting;
    }

    /* access modifiers changed from: package-private */
    public void requestPermissionsFromRemote(String[] permissions, final DistributedPermissionManager.IRequestPermissionsResult callback, String nodeId, String bundleName, int reasonResId) {
        Log.d(TAG, "requestPermissionsFromRemote() called.");
        if (callback == null) {
            Log.e(TAG, "requestPermissionsFromRemote callback is null");
        } else if (permissions == null || permissions.length == 0) {
            Log.e(TAG, "requestPermissionsFromRemote permissions is empty");
        } else if (isOtherRequestExecuting()) {
            Log.e(TAG, "requestPermissionsFromRemote have other request is in processing now");
            int[] grantResults = new int[permissions.length];
            Arrays.fill(grantResults, -11);
            callback.onResult(nodeId, permissions, grantResults);
        } else {
            try {
                IDistributedPermissionManager service = getService();
                if (service != null) {
                    service.requestPermissionsFromRemote(permissions, new IRequestPermissionsResult.Stub() {
                        /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass3 */

                        @Override // com.huawei.security.dpermission.IRequestPermissionsResult
                        public void onCancel(String nodeId, String[] permissions) throws RemoteException {
                            DistributedPermissionManagerImpl.this.clearRequestingPermissionFlag();
                            callback.onCancel(nodeId, permissions);
                        }

                        @Override // com.huawei.security.dpermission.IRequestPermissionsResult
                        public void onResult(String nodeId, String[] permissions, int[] grantResults) throws RemoteException {
                            DistributedPermissionManagerImpl.this.clearRequestingPermissionFlag();
                            callback.onResult(nodeId, permissions, grantResults);
                        }

                        @Override // com.huawei.security.dpermission.IRequestPermissionsResult
                        public void onTimeOut(String nodeId, String[] permissions) throws RemoteException {
                            DistributedPermissionManagerImpl.this.clearRequestingPermissionFlag();
                            callback.onTimeOut(nodeId, permissions);
                        }
                    }, nodeId, bundleName, reasonResId);
                }
            } catch (RemoteException e) {
                clearRequestingPermissionFlag();
                Log.e(TAG, "requestPermissionsFromRemote error " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void grantSensitivePermissionToRemoteApp(String permission, String nodeId, int ruid) {
        Log.d(TAG, "grantSensitivePermissionToRemoteApp() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                service.grantSensitivePermissionToRemoteApp(permission, nodeId, ruid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "grantSensitivePermissionToRemoteApp error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String processZ2aMessage(String command, String payload) {
        Log.d(TAG, "processZ2aMessage() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.processZ2aMessage(command, payload);
            }
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            Log.e(TAG, "processZ2aMessage error " + e.getMessage());
            return BuildConfig.FLAVOR;
        }
    }

    /* access modifiers changed from: package-private */
    public void addPermissionRecord(String permissionName, String deviceId, int uid, int successCount, int failCount) {
        Log.d(TAG, "addPermissionRecord() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                service.addPermissionRecord(permissionName, deviceId, uid, successCount, failCount);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "addPermissionRecord error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public String getPermissionRecord(String data) {
        Log.d(TAG, "getPermissionRecord() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.getPermissionRecord(data);
            }
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            Log.e(TAG, "getPermissionRecord error " + e.getMessage());
            return BuildConfig.FLAVOR;
        }
    }

    /* access modifiers changed from: package-private */
    public void getPermissionRecordAsync(String data, IPermissionRecordQueryCallback callback) {
        Log.d(TAG, "getPermissionRecordAsync() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                service.getPermissionRecordAsync(data, callback);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getPermissionRecordAsync error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public int registerOnPermissionUsingReminder(OnPermissionUsingReminder reminder) {
        Log.d(TAG, "registerOnPermissionUsingReminder() called.");
        if (reminder == null) {
            Log.e(TAG, "registerOnPermissionUsingReminder: reminder is null");
            return -1;
        }
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.registerOnPermissionUsingReminder(this.mUsingPermissionReminders.computeIfAbsent(reminder, new Function<OnPermissionUsingReminder, OnPermissionUsingReminderDelegate>() {
                    /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass4 */

                    public OnPermissionUsingReminderDelegate apply(OnPermissionUsingReminder reminder) {
                        return new OnPermissionUsingReminderDelegate(reminder);
                    }
                }));
            }
        } catch (RemoteException e) {
            Log.e(TAG, "registerOnPermissionUsingReminder error " + e.getMessage());
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int unregisterOnPermissionUsingReminder(OnPermissionUsingReminder reminder) {
        Log.d(TAG, "unregisterOnPermissionUsingReminder() called.");
        if (reminder == null) {
            Log.e(TAG, "unregisterOnPermissionUsingReminder: reminder is null");
            return -1;
        }
        try {
            IDistributedPermissionManager service = getService();
            if (service == null) {
                Log.e(TAG, "unregisterOnPermissionUsingReminder: service is null!");
                return -1;
            }
            OnPermissionUsingReminderDelegate delegate = this.mUsingPermissionReminders.remove(reminder);
            if (delegate != null) {
                return service.unregisterOnPermissionUsingReminder(delegate);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterOnPermissionUsingReminder error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void startUsingPermission(String permissionName, String appIdInfo) {
        Log.d(TAG, "startUsingPermission() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                service.startUsingPermission(permissionName, appIdInfo);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "startUsingPermission error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public void stopUsingPermission(String permissionName, String appIdInfo) {
        Log.d(TAG, "stopUsingPermission() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                service.stopUsingPermission(permissionName, appIdInfo);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "stopUsingPermission error " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    public int verifyPermissionAndState(String permissionName, String appIdInfo) {
        Log.d(TAG, "verifyPermissionAndStatus() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.verifyPermissionAndState(permissionName, appIdInfo);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "verifyPermissionAndStatus error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int checkDPermissionAndStartUsing(String permissionName, String appIdInfo) {
        Log.d(TAG, "checkDPermissionAndStartUsing() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.checkDPermissionAndStartUsing(permissionName, appIdInfo);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "checkDPermissionAndStartUsing error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int checkDPermissionAndUse(String permissionName, String appIdInfo) {
        Log.d(TAG, "checkDPermissionAndUse() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.checkDPermissionAndUse(permissionName, appIdInfo);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "checkDPermissionAndUse error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int checkPermission(String permissionName, String nodeId, int pid, int uid) {
        Log.d(TAG, "checkPermission() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.checkPermission(permissionName, nodeId, pid, uid);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "checkPermission error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public int postPermissionEvent(String event) {
        Log.d(TAG, "postPermissionEvent() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.postPermissionEvent(event);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "postPermissionEvent error " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public String getPermissionUsagesInfo(String packageName, String[] permissions) {
        Log.d(TAG, "getPermissionUsagesInfo() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.getPermissionUsagesInfo(packageName, permissions);
            }
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            Log.e(TAG, "getPermissionUsagesInfo error " + e.getMessage());
            return BuildConfig.FLAVOR;
        }
    }

    /* access modifiers changed from: package-private */
    public String getBundleLabelInfo(int dUid) {
        Log.d(TAG, "getBundleLabelInfo() called.");
        try {
            IDistributedPermissionManager service = getService();
            if (service != null) {
                return service.getBundleLabelInfo(dUid);
            }
            return BuildConfig.FLAVOR;
        } catch (RemoteException e) {
            Log.e(TAG, "getBundleLabelInfo error " + e.getMessage());
            return BuildConfig.FLAVOR;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkDPermissionInner(int dUid, String permissionName) {
        try {
            int userId = dUid / HwKeystoreManager.AUTH_TYPE_UNSUPPORT;
            Log.i(TAG, "checkDPermission: userId: " + userId);
            if (userId == USER_ID_DISTRIBUTED_FROM_A) {
                IDistributedPermissionManager service = getService();
                if (service != null) {
                    int result = service.checkDPermission(dUid, permissionName);
                    Log.i(TAG, "checkDPermission result: " + result);
                    return result;
                }
                Log.w(TAG, "No service for check permission!");
            }
            if (userId == USER_ID_DISTRIBUTED_FROM_Z) {
                IHwDPermission proxiedService = getProxiedService();
                if (proxiedService != null) {
                    int result2 = proxiedService.checkDPermission(dUid, permissionName);
                    Log.i(TAG, "checkProxiedDPermission result: " + result2);
                    return result2;
                }
                Log.w(TAG, "No Proxied for check permission!");
            } else {
                Log.w(TAG, "Unknown userId for distributed permission: " + userId);
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "CheckDPermission error" + e.getMessage());
            Log.e(TAG, "Failed to check remote permission!");
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int notifyUidPermissionChangedInner(final int uid) {
        return notifyParallelLimited("Notifier_uid", new Runnable() {
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass5 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    IDistributedPermissionManager service = DistributedPermissionManagerImpl.getService();
                    if (service != null) {
                        int result = service.notifyUidPermissionChanged(uid);
                        Log.i(DistributedPermissionManagerImpl.TAG, "Notify uid permission changed result: " + result);
                        return;
                    }
                    Log.w(DistributedPermissionManagerImpl.TAG, "No service when notify uid permission changed!");
                } catch (RemoteException e) {
                    Log.e(DistributedPermissionManagerImpl.TAG, "Failed to notify uid permission changed: " + e.getMessage());
                }
            }
        }, new Runnable() {
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass6 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    IHwDPermission proxiedService = DistributedPermissionManagerImpl.getProxiedService();
                    if (proxiedService != null) {
                        int result = proxiedService.notifyUidPermissionChanged(uid);
                        Log.i(DistributedPermissionManagerImpl.TAG, "Notify proxied uid permission changed result: " + result);
                        return;
                    }
                    Log.w(DistributedPermissionManagerImpl.TAG, "No proxied when notify uid permission changed!");
                } catch (RemoteException e) {
                    Log.e(DistributedPermissionManagerImpl.TAG, "Failed to notify proxied uid permission changed: " + e.getMessage());
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int notifyPermissionChangedInner(final int uid, final String permissionName, final int status) {
        return notifyParallelLimited("Notifier_partly", new Runnable() {
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass7 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    IDistributedPermissionManager service = DistributedPermissionManagerImpl.getService();
                    if (service != null) {
                        int result = service.notifyPermissionChanged(uid, permissionName, status);
                        Log.i(DistributedPermissionManagerImpl.TAG, "Notify permission changed result: " + result);
                        return;
                    }
                    Log.w(DistributedPermissionManagerImpl.TAG, "No service when notify permission changed!");
                } catch (RemoteException e) {
                    Log.e(DistributedPermissionManagerImpl.TAG, "Failed to notify permission changed: " + e.getMessage());
                }
            }
        }, new Runnable() {
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass8 */

            @Override // java.lang.Runnable
            public void run() {
                try {
                    IHwDPermission proxiedService = DistributedPermissionManagerImpl.getProxiedService();
                    if (proxiedService != null) {
                        int result = proxiedService.notifyPermissionChanged(uid, permissionName, status);
                        Log.i(DistributedPermissionManagerImpl.TAG, "Notify proxied permission changed result: " + result);
                        return;
                    }
                    Log.w(DistributedPermissionManagerImpl.TAG, "No proxied when notify permission changed!");
                } catch (RemoteException e) {
                    Log.e(DistributedPermissionManagerImpl.TAG, "Failed to notify proxied permission changed: " + e.getMessage());
                }
            }
        });
    }

    private int notifyParallelLimited(final String threadPrefix, final Runnable notifyService, final Runnable notifyProxiedService) {
        try {
            final long begin = System.currentTimeMillis();
            final CountDownLatch latch = new CountDownLatch(2);
            AnonymousClass9 r15 = new Runnable() {
                /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass9 */

                @Override // java.lang.Runnable
                public void run() {
                    notifyService.run();
                    Log.i(DistributedPermissionManagerImpl.TAG, threadPrefix + " done notify service done in " + (System.currentTimeMillis() - begin) + " ms.");
                    latch.countDown();
                }
            };
            new Thread(r15, threadPrefix + "_service").start();
            AnonymousClass10 r152 = new Runnable() {
                /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass10 */

                @Override // java.lang.Runnable
                public void run() {
                    notifyProxiedService.run();
                    Log.i(DistributedPermissionManagerImpl.TAG, threadPrefix + " done notify proxied in " + (System.currentTimeMillis() - begin) + " ms.");
                    latch.countDown();
                }
            };
            new Thread(r152, threadPrefix + "_proxied").start();
            Log.d(TAG, "finish start threads, in " + (System.currentTimeMillis() - begin) + " ms.");
            boolean isDoneInTime = latch.await(200, TimeUnit.MILLISECONDS);
            Log.w(TAG, "isDoneInTime totally: " + isDoneInTime + ", in " + (System.currentTimeMillis() - begin) + " ms.");
            if (isDoneInTime) {
                return 0;
            }
            return -1;
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to latch, interrupted sooner: " + e.getMessage());
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public static class OnPermissionUsingReminderDelegate extends IOnPermissionUsingReminder.Stub {
        private OnPermissionUsingReminder mReminder;

        OnPermissionUsingReminderDelegate(OnPermissionUsingReminder reminder) {
            this.mReminder = reminder;
        }

        @Override // com.huawei.security.dpermission.permissionusingremind.IOnPermissionUsingReminder
        public void onPermissionStartUsing(Bundle info) {
            OnPermissionUsingReminder onPermissionUsingReminder = this.mReminder;
            if (onPermissionUsingReminder != null) {
                onPermissionUsingReminder.onPermissionStartUsing(info);
            }
        }

        @Override // com.huawei.security.dpermission.permissionusingremind.IOnPermissionUsingReminder
        public void onPermissionStopUsing(Bundle info) {
            OnPermissionUsingReminder onPermissionUsingReminder = this.mReminder;
            if (onPermissionUsingReminder != null) {
                onPermissionUsingReminder.onPermissionStopUsing(info);
            }
        }
    }

    private class InnerServiceImpl implements IDPermissionManager {
        private InnerServiceImpl() {
        }

        public int allocateDuid(String nodeId, int rUid) {
            return DistributedPermissionManagerImpl.this.allocateDuid(nodeId, rUid);
        }

        public int queryDuid(String nodeId, int rUid) {
            return DistributedPermissionManagerImpl.this.queryDuid(nodeId, rUid);
        }

        public int checkDPermission(int dUid, String permissionName) {
            return DistributedPermissionManagerImpl.this.checkDPermissionInner(dUid, permissionName);
        }

        public int notifyUidPermissionChanged(int uid) {
            return DistributedPermissionManagerImpl.this.notifyUidPermissionChangedInner(uid);
        }

        public int notifyPermissionChanged(int uid, String permissionName, int status) {
            return DistributedPermissionManagerImpl.this.notifyPermissionChangedInner(uid, permissionName, status);
        }
    }
}
