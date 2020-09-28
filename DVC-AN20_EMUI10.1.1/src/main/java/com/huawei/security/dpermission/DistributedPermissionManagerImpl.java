package com.huawei.security.dpermission;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.security.HwKeystoreManager;
import com.huawei.security.dpermission.IDistributedPermissionManager;
import com.huawei.security.dpermission.IHwDPermission;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DistributedPermissionManagerImpl {
    private static final int GENERAL_ERROR = -1;
    private static final Object LOCK = new Object();
    private static final int MAX_NOTIFY_TIMEOUT = 200;
    private static final int PERMISSION_DENIED = -1;
    private static final int PERMISSION_GRANTED = 0;
    private static final String TAG = "DistributedPermissionManagerImpl";
    private static final int USER_ID_DISTRIBUTED_FROM_A = 126;
    private static final int USER_ID_DISTRIBUTED_FROM_Z = 125;
    private static IDPermissionManager sInnerServiceImpl;
    private static DistributedPermissionManagerImpl sInstance = new DistributedPermissionManagerImpl();
    private static IHwDPermission sProxiedService = null;
    private static IDistributedPermissionManager sService = null;

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
    public int allocateDuid(String deviceId, int rUid) {
        try {
            if (getService() != null) {
                return sService.allocateDuid(deviceId, rUid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "AllocateDuid error" + e.getMessage());
        }
        Log.e(TAG, "Failed to allocate duid!");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public int queryDuid(String deviceId, int rUid) {
        try {
            if (getService() != null) {
                return sService.queryDuid(deviceId, rUid);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "QueryDuid error" + e.getMessage());
        }
        Log.e(TAG, "Failed to query duid!");
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int checkDPermissionInner(int dUid, String permissionName) {
        try {
            int userId = dUid / HwKeystoreManager.AUTH_TYPE_UNSUPPORT;
            Log.i(TAG, "checkDPermission: userId: " + userId);
            if (userId == USER_ID_DISTRIBUTED_FROM_A) {
                if (getService() != null) {
                    int result = sService.checkDPermission(dUid, permissionName);
                    Log.i(TAG, "checkDPermission result: " + result);
                    return result;
                }
                Log.w(TAG, "No service for check permission!");
            }
            if (userId != USER_ID_DISTRIBUTED_FROM_Z) {
                Log.w(TAG, "Unknown userId for distributed permission: " + userId);
            } else if (getProxiedService() != null) {
                int result2 = sProxiedService.checkDPermission(dUid, permissionName);
                Log.i(TAG, "checkProxiedDPermission result: " + result2);
                return result2;
            } else {
                Log.w(TAG, "No Proxied for check permission!");
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
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass3 */

            public void run() {
                try {
                    if (DistributedPermissionManagerImpl.getService() != null) {
                        int result = DistributedPermissionManagerImpl.sService.notifyUidPermissionChanged(uid);
                        Log.i(DistributedPermissionManagerImpl.TAG, "Notify uid permission changed result: " + result);
                        return;
                    }
                    Log.w(DistributedPermissionManagerImpl.TAG, "No service when notify uid permission changed!");
                } catch (RemoteException e) {
                    Log.e(DistributedPermissionManagerImpl.TAG, "Failed to notify uid permission changed: " + e.getMessage());
                }
            }
        }, new Runnable() {
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass4 */

            public void run() {
                try {
                    if (DistributedPermissionManagerImpl.getProxiedService() != null) {
                        int result = DistributedPermissionManagerImpl.sProxiedService.notifyUidPermissionChanged(uid);
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
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass5 */

            public void run() {
                try {
                    if (DistributedPermissionManagerImpl.getService() != null) {
                        int result = DistributedPermissionManagerImpl.sService.notifyPermissionChanged(uid, permissionName, status);
                        Log.i(DistributedPermissionManagerImpl.TAG, "Notify permission changed result: " + result);
                        return;
                    }
                    Log.w(DistributedPermissionManagerImpl.TAG, "No service when notify permission changed!");
                } catch (RemoteException e) {
                    Log.e(DistributedPermissionManagerImpl.TAG, "Failed to notify permission changed: " + e.getMessage());
                }
            }
        }, new Runnable() {
            /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass6 */

            public void run() {
                try {
                    if (DistributedPermissionManagerImpl.getProxiedService() != null) {
                        int result = DistributedPermissionManagerImpl.sProxiedService.notifyPermissionChanged(uid, permissionName, status);
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
            AnonymousClass7 r15 = new Runnable() {
                /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass7 */

                public void run() {
                    notifyService.run();
                    Log.i(DistributedPermissionManagerImpl.TAG, threadPrefix + " done notify service done in " + (System.currentTimeMillis() - begin) + " ms.");
                    latch.countDown();
                }
            };
            new Thread(r15, threadPrefix + "_service").start();
            AnonymousClass8 r152 = new Runnable() {
                /* class com.huawei.security.dpermission.DistributedPermissionManagerImpl.AnonymousClass8 */

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

    private class InnerServiceImpl implements IDPermissionManager {
        private InnerServiceImpl() {
        }

        public int allocateDuid(String deviceId, int rUid) {
            return DistributedPermissionManagerImpl.this.allocateDuid(deviceId, rUid);
        }

        public int queryDuid(String deviceId, int rUid) {
            return DistributedPermissionManagerImpl.this.queryDuid(deviceId, rUid);
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
