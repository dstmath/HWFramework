package com.huawei.security.deviceauth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.deviceauth.IGroupManageService;
import com.huawei.security.deviceauth.IHichainGroupCallback;
import com.huawei.security.deviceauth.IHichainGroupChangeListener;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

public class HwDeviceGroupManager {
    public static final int ALL_GROUP = 0;
    public static final int CANCELLED = -2147483646;
    public static final int COMPATIBLE_GROUP = 512;
    private static final String CONFIRM_REJECT = null;
    public static final int CONFLIT_REQUEST = -2147483647;
    private static final String DEFAULT_PACKAGE_NAME = "com.huawei.devicegroupmanage";
    private static final String DEFAULT_SERVICE_NAME = "com.huawei.devicegroupmanage.HwDeviceGroupManageService";
    public static final int FAILED = -1;
    public static final int FULL_MESH_GROUP = 257;
    public static final int GROUP_VISIBILITY_PRIVATE = 0;
    public static final int GROUP_VISIBILITY_PRIVILEGE = 8;
    public static final int GROUP_VISIBILITY_PUBLIC = -1;
    public static final int GROUP_VISIBILITY_SIGNATURE = 2;
    public static final int GROUP_VISIBILITY_SYSTEM = 4;
    public static final int GROUP_VISIBILITY_WHITELIST = 1;
    public static final int IDENTICAL_ACCOUNT_GROUP = 1;
    private static final int INITIAL_CAPACITY = 16;
    private static final HashMap<String, SoftReference<HwDeviceGroupManager>> MANAGER_MAP = new HashMap<>(16);
    public static final int NO_PERMISSION = -2147483644;
    public static final int P2P_GROUP = 256;
    public static final String PARAMETER_TAG_ADD_ID = "addId";
    public static final String PARAMETER_TAG_APP_ID = "appId";
    public static final String PARAMETER_TAG_CONFIRMATION = "confirmation";
    public static final String PARAMETER_TAG_CONNECT_DEVICE_ID = "connDeviceId";
    public static final String PARAMETER_TAG_DELETE_ID = "deleteId";
    public static final String PARAMETER_TAG_DEVICE_ID = "deviceId";
    public static final String PARAMETER_TAG_FORCE_DELETE = "isForceDelete";
    public static final String PARAMETER_TAG_GROUP_ID = "groupId";
    public static final String PARAMETER_TAG_GROUP_INFO = "groupInfo";
    public static final String PARAMETER_TAG_GROUP_NAME = "groupName";
    public static final String PARAMETER_TAG_GROUP_TYPE = "groupType";
    public static final String PARAMETER_TAG_IS_ADMIN = "isAdmin";
    public static final String PARAMETER_TAG_IS_PRIVATE = "isPrivate";
    public static final String PARAMETER_TAG_OWNER_PKG_NAME = "owner";
    public static final String PARAMETER_TAG_PEER_DEVICE_ID = "peerDeviceId";
    public static final String PARAMETER_TAG_PIN = "pinCode";
    public static final String PARAMETER_TAG_SESSION_KEY = "sessionKey";
    public static final int REQUEST_ACCEPTED = -2147483642;
    public static final int REQUEST_NOT_FOUND = -2147483645;
    public static final int REQUEST_REJECTED = -2147483643;
    public static final int STELLIFORM_GROUP = 2;
    public static final int SUCCESS = 0;
    private static final String TAG = "HwDeviceGroupManager";
    private static final int WAIT_LOCK_TIME = 2000;
    private static HashMap<String, IHichainGroupCallback> sCallbackMap = new HashMap<>(16);
    private String mCallerPkgName;
    private final ExecutorService mConnectServiceJob;
    private ServiceConnectionListener mConnectionListener;
    private Context mContext;
    private IHichainGroupChangeListener mGroupChangeCallbackBinder;
    private HichainGroupChangeListener mGroupChangeListener;
    private IGroupManageService mGroupManageService;
    private GroupManageServiceAccessMonitor mGroupManageServiceAccessMonitor;
    private CountDownLatch mLatch;
    private volatile boolean mRegisterFlag;
    private ServiceConnection mServiceConnection;
    private final Object serviceInstanceLock;

    public interface HichainGroupCallback {
        void onError(long j, GroupOperation groupOperation, int i, String str);

        void onFinish(long j, GroupOperation groupOperation, String str);

        String onRequest(long j, GroupOperation groupOperation, String str);
    }

    public interface HichainGroupChangeListener {
        void onGroupCreated(String str, int i);

        void onGroupDeleted(String str, int i);

        void onMemberAdded(String str, int i, List<String> list);

        void onMemberDeleted(String str, int i, List<String> list);
    }

    public interface ServiceConnectionListener {
        void onServiceConnected();

        void onServiceDisconnected();
    }

    static IHichainGroupCallback getCallback(final HichainGroupCallback callbackHandler) {
        return new IHichainGroupCallback.Stub() {
            /* class com.huawei.security.deviceauth.HwDeviceGroupManager.AnonymousClass1 */

            public void onFinish(long requestId, int operationCode, String returnData) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onFinish begin");
                try {
                    callbackHandler.onFinish(requestId, GroupOperation.valueOf(operationCode), returnData);
                } catch (Exception e) {
                    LogUtils.d(HwDeviceGroupManager.TAG, "call onFinish method failed");
                }
            }

            public void onError(long requestId, int operationCode, int errorCode, String errorReturn) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onError begin");
                try {
                    callbackHandler.onError(requestId, GroupOperation.valueOf(operationCode), errorCode, errorReturn);
                } catch (Exception e) {
                    LogUtils.d(HwDeviceGroupManager.TAG, "call onError method failed");
                }
            }

            public String onRequest(long requestId, int operationCode, String reqParams) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onRequest begin");
                String confirmation = HwDeviceGroupManager.CONFIRM_REJECT;
                try {
                    return callbackHandler.onRequest(requestId, GroupOperation.valueOf(operationCode), reqParams);
                } catch (Exception e) {
                    LogUtils.d(HwDeviceGroupManager.TAG, "call onRequest method failed");
                    return confirmation;
                }
            }
        };
    }

    private HwDeviceGroupManager(Context ctx, String appId, HichainGroupCallback callback) {
        this(ctx, appId, callback, null);
    }

    private HwDeviceGroupManager(Context ctx, String appId, HichainGroupCallback callback, ServiceConnectionListener listener) {
        this.mRegisterFlag = false;
        this.mConnectServiceJob = Executors.newSingleThreadExecutor();
        this.mGroupManageServiceAccessMonitor = null;
        this.serviceInstanceLock = new Object();
        this.mGroupChangeCallbackBinder = new IHichainGroupChangeListener.Stub() {
            /* class com.huawei.security.deviceauth.HwDeviceGroupManager.AnonymousClass2 */

            public void onGroupCreated(String groupId, int groupType) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onGroupCreated begin");
                HwDeviceGroupManager.this.mGroupChangeListener.onGroupCreated(groupId, groupType);
            }

            public void onGroupDeleted(String groupId, int groupType) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onGroupDeleted begin");
                HwDeviceGroupManager.this.mGroupChangeListener.onGroupDeleted(groupId, groupType);
            }

            public void onMemberAdded(String groupId, int groupType, List<String> memList) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onMemberAdded begin");
                HwDeviceGroupManager.this.mGroupChangeListener.onMemberAdded(groupId, groupType, memList);
            }

            public void onMemberDeleted(String groupId, int groupType, List<String> memList) {
                LogUtils.d(HwDeviceGroupManager.TAG, "onMemberDeleted begin");
                HwDeviceGroupManager.this.mGroupChangeListener.onMemberDeleted(groupId, groupType, memList);
            }
        };
        this.mContext = ctx;
        if (ctx != null) {
            this.mCallerPkgName = ctx.getPackageName();
        }
        this.mConnectionListener = listener;
        this.mServiceConnection = createServiceConnection();
        this.mGroupManageServiceAccessMonitor = new GroupManageServiceAccessMonitor(this, this.mCallerPkgName);
        this.mGroupManageServiceAccessMonitor.startDaemon();
    }

    private ServiceConnection createServiceConnection() {
        return new ServiceConnection() {
            /* class com.huawei.security.deviceauth.HwDeviceGroupManager.AnonymousClass3 */

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder binder) {
                LogUtils.i(HwDeviceGroupManager.TAG, "HwGroupManageService connected");
                synchronized (HwDeviceGroupManager.this.serviceInstanceLock) {
                    HwDeviceGroupManager.this.mGroupManageService = IGroupManageService.Stub.asInterface(binder);
                }
                HwDeviceGroupManager.this.binderLinkToDeath(binder);
                if (HwDeviceGroupManager.this.mConnectionListener != null) {
                    HwDeviceGroupManager.this.mConnectionListener.onServiceConnected();
                } else {
                    LogUtils.w(HwDeviceGroupManager.TAG, "Connection listener is null");
                }
                if (HwDeviceGroupManager.this.mLatch != null) {
                    HwDeviceGroupManager.this.mLatch.countDown();
                }
                HwDeviceGroupManager.this.mGroupManageServiceAccessMonitor.startDaemon();
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                LogUtils.i(HwDeviceGroupManager.TAG, "HwGroupManageService disconnected");
                synchronized (HwDeviceGroupManager.this.serviceInstanceLock) {
                    HwDeviceGroupManager.this.mGroupManageService = null;
                }
                if (HwDeviceGroupManager.this.mConnectionListener != null) {
                    HwDeviceGroupManager.this.mConnectionListener.onServiceDisconnected();
                } else {
                    LogUtils.w(HwDeviceGroupManager.TAG, "Connection listener is null");
                }
                HwDeviceGroupManager.this.mRegisterFlag = false;
                if (HwDeviceGroupManager.this.mLatch != null) {
                    HwDeviceGroupManager.this.mLatch.countDown();
                }
                synchronized (HwDeviceGroupManager.MANAGER_MAP) {
                    HwDeviceGroupManager.MANAGER_MAP.remove(HwDeviceGroupManager.this.mContext.getPackageName());
                }
                HwDeviceGroupManager.this.mGroupManageServiceAccessMonitor.stopDaemon();
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void binderLinkToDeath(IBinder binder) {
        try {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                /* class com.huawei.security.deviceauth.HwDeviceGroupManager.AnonymousClass4 */

                @Override // android.os.IBinder.DeathRecipient
                public void binderDied() {
                    HwDeviceGroupManager mgr;
                    LogUtils.e(HwDeviceGroupManager.TAG, "HwGroupManageService binderDied");
                    synchronized (HwDeviceGroupManager.this.serviceInstanceLock) {
                        HwDeviceGroupManager.this.mGroupManageService = null;
                    }
                    synchronized (HwDeviceGroupManager.MANAGER_MAP) {
                        SoftReference<HwDeviceGroupManager> manager = (SoftReference) HwDeviceGroupManager.MANAGER_MAP.get(HwDeviceGroupManager.this.mContext.getPackageName());
                        if (!(manager == null || (mgr = manager.get()) == null)) {
                            LogUtils.i(HwDeviceGroupManager.TAG, "shut down job since binder died");
                            mgr.mConnectServiceJob.shutdownNow();
                            mgr.blindlyUnbind();
                        }
                        HwDeviceGroupManager.MANAGER_MAP.remove(HwDeviceGroupManager.this.mContext.getPackageName());
                    }
                    if (HwDeviceGroupManager.this.mConnectionListener != null) {
                        HwDeviceGroupManager.this.mConnectionListener.onServiceDisconnected();
                    } else {
                        LogUtils.w(HwDeviceGroupManager.TAG, "Connection listener is null");
                    }
                }
            }, 0);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "RemoteException when call binder linkToDeath");
        }
    }

    public static HwDeviceGroupManager getInstance(Context context, String appId, HichainGroupCallback callbackHandler) {
        if (context == null) {
            LogUtils.i(TAG, "HwDeviceGroupManager getInstance failed for empty context");
            return null;
        }
        LogUtils.i(TAG, context.getPackageName() + " call new HwDeviceGroupManager getInstance.");
        return getInstance(context, appId, callbackHandler, null);
    }

    public static HwDeviceGroupManager getInstance(Context context, String appId, HichainGroupCallback callbackHandler, ServiceConnectionListener connListener) {
        HwDeviceGroupManager managerForThisPkg;
        if (context == null) {
            LogUtils.i(TAG, "HwDeviceGroupManager getInstance failed for empty context");
            return null;
        }
        LogUtils.i(TAG, context.getPackageName() + " call new HwDeviceGroupManager getInstance and register connection callback.");
        if (TextUtils.isEmpty(appId) || callbackHandler == null) {
            LogUtils.d(TAG, "Inputted parameter is invalid when getInstance.");
            return null;
        }
        synchronized (MANAGER_MAP) {
            String pkgName = context.getPackageName();
            SoftReference<HwDeviceGroupManager> managerRefered = MANAGER_MAP.get(pkgName);
            if (!(managerRefered == null || (managerForThisPkg = managerRefered.get()) == null)) {
                if (managerForThisPkg.registerCallback(appId, callbackHandler)) {
                    managerForThisPkg.mGroupManageServiceAccessMonitor.startDaemon();
                    return managerForThisPkg;
                }
                LogUtils.i(TAG, "we will create a new manager so we shut down job belongs to the old one");
                managerForThisPkg.mConnectServiceJob.shutdownNow();
                managerForThisPkg.blindlyUnbind();
            }
            LogUtils.i(TAG, "try to create new manager.");
            HwDeviceGroupManager hwDeviceGroupManager = new HwDeviceGroupManager(context, appId, callbackHandler, connListener);
            hwDeviceGroupManager.connectGroupManageService();
            if (!hwDeviceGroupManager.registerCallback(appId, callbackHandler)) {
                LogUtils.i(TAG, "register callback fail, we shut down the job before release the manager");
                hwDeviceGroupManager.mConnectServiceJob.shutdownNow();
                hwDeviceGroupManager.blindlyUnbind();
                return null;
            }
            MANAGER_MAP.put(pkgName, new SoftReference<>(hwDeviceGroupManager));
            return hwDeviceGroupManager;
        }
    }

    public void bindHwGroupManageService() {
        LogUtils.i(TAG, "Bind HwGroupManageService.");
        connectGroupManageService();
    }

    private void connectGroupManageService() {
        LogUtils.i(TAG, "try to bind HwGroupManageService.");
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService != null) {
                return;
            }
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DEFAULT_PACKAGE_NAME, DEFAULT_SERVICE_NAME));
        if (this.mConnectionListener == null) {
            this.mLatch = new CountDownLatch(1);
        }
        if (this.mContext.bindService(intent, 65, this.mConnectServiceJob, this.mServiceConnection)) {
            LogUtils.i(TAG, "bindHwGroupManageService ok");
        } else {
            LogUtils.e(TAG, "bindHwGroupManageService failed");
        }
    }

    public void unbindHwGroupManageService() {
        LogUtils.i(TAG, "Unbind HwGroupManageService.");
        synchronized (MANAGER_MAP) {
            try {
                this.mContext.unbindService(this.mServiceConnection);
                synchronized (this.serviceInstanceLock) {
                    sCallbackMap.clear();
                }
            } catch (IllegalArgumentException e) {
                LogUtils.i(TAG, "unbind exception");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void blindlyUnbind() {
        LogUtils.i(TAG, "blindlyUnbind");
        synchronized (MANAGER_MAP) {
            try {
                this.mContext.unbindService(this.mServiceConnection);
            } catch (IllegalArgumentException e) {
                LogUtils.i(TAG, "blindlyUnbind exception");
            }
        }
    }

    private boolean registerCallback(String appId, HichainGroupCallback callbackHandler) {
        LogUtils.i(TAG, "Register callback in HwDeviceGroupManager.");
        try {
            if (this.mLatch == null || this.mLatch.await(2000, TimeUnit.MILLISECONDS)) {
                synchronized (this.serviceInstanceLock) {
                    if (this.mGroupManageService == null) {
                        LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                        this.mRegisterFlag = false;
                        return this.mRegisterFlag;
                    }
                    try {
                        IHichainGroupCallback callBack = getCallback(callbackHandler);
                        String curKey = packCallerId(appId);
                        this.mRegisterFlag = this.mGroupManageService.registerCallback(curKey, callBack) == 0;
                        LogUtils.i(TAG, "register callback res " + this.mRegisterFlag);
                        for (Map.Entry<String, IHichainGroupCallback> entry : sCallbackMap.entrySet()) {
                            if (!curKey.equals(entry.getKey())) {
                                if (this.mRegisterFlag) {
                                    boolean registerSuc = this.mGroupManageService.registerCallback(entry.getKey(), entry.getValue()) == 0;
                                    LogUtils.i(TAG, "register callback res:" + registerSuc + "with key" + entry.getKey());
                                }
                            }
                        }
                        sCallbackMap.put(curKey, callBack);
                    } catch (RemoteException e) {
                        LogUtils.e(TAG, "Remote exception occurred when register callback.");
                        this.mRegisterFlag = false;
                    }
                    return this.mRegisterFlag;
                }
            }
            LogUtils.w(TAG, "wait HwDeviceGroupManage service connection timeout, register callback fail");
            this.mRegisterFlag = false;
            return this.mRegisterFlag;
        } catch (InterruptedException e2) {
            LogUtils.e(TAG, "Interrupted exception occurred when register callback.");
            this.mRegisterFlag = false;
            return this.mRegisterFlag;
        }
    }

    public int createGroup(String appId, String groupName, int groupType, String groupInfo) {
        LogUtils.i(TAG, "Create group in HwDeviceGroupManager.");
        if (TextUtils.isEmpty(appId) || groupName == null || groupInfo == null) {
            LogUtils.e(TAG, "invalid parameter when call createGroup");
            return -1;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                return this.mGroupManageService.createGroup(packCallerId(appId), groupName, groupType, groupInfo);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Remote exception occurred when create group.");
                return -1;
            }
        }
    }

    public int deleteGroup(String groupId) {
        LogUtils.i(TAG, "Delete group in HwDeviceGroupManager.");
        if (groupId == null) {
            LogUtils.e(TAG, "invalid parameter when call deleteGroup");
            return -1;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                return this.mGroupManageService.deleteDevGroup(this.mCallerPkgName, groupId);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Remote exception occurred when delete group.");
                return -1;
            }
        }
    }

    public String getLocalConnectInfo() {
        LogUtils.i(TAG, "Get local connect info group in HwDeviceGroupManager.");
        this.mGroupManageServiceAccessMonitor.startDaemon();
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return BuildConfig.FLAVOR;
            }
            try {
                return this.mGroupManageService.getLocalConnectInfo();
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Remote exception occurred when get local connect info.");
                return BuildConfig.FLAVOR;
            }
        }
    }

    public int addMemberToGroup(String appId, long requestId, String addParams, String connectParams, int groupType) {
        LogUtils.i(TAG, "Add member to group in HwDeviceGroupManager.");
        if (TextUtils.isEmpty(appId) || addParams == null || connectParams == null) {
            LogUtils.e(TAG, "invalid parameter when call addMemberToGroup");
            return -1;
        } else if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        } else {
            synchronized (this.serviceInstanceLock) {
                this.mGroupManageServiceAccessMonitor.startDaemon();
                if (this.mGroupManageService == null) {
                    LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                    return -1;
                }
                try {
                    return this.mGroupManageService.addMemberToGroup(packCallerId(appId), requestId, addParams, connectParams, groupType);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "Remote exception occurred when add member to group.");
                    return -1;
                }
            }
        }
    }

    public int deleteMemberFromGroup(String appId, long requestId, String deleteParams, String connectParams) {
        LogUtils.i(TAG, "Delete member from group in HwDeviceGroupManager.");
        if (TextUtils.isEmpty(appId) || deleteParams == null) {
            LogUtils.e(TAG, "invalid parameter when call deleteMemberFromGroup");
            return -1;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                return this.mGroupManageService.deleteMemberFromGroup(packCallerId(appId), requestId, deleteParams, connectParams);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "Remote exception occurred when delete member from group.");
                return -1;
            }
        }
    }

    public int cancelRequest(long requestId) {
        this.mGroupManageServiceAccessMonitor.startDaemon();
        LogUtils.i(TAG, "cancel the processing async request in HwDeviceGroupManager.");
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                return this.mGroupManageService.cancelGroupRequest(this.mCallerPkgName, requestId);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "cancel request fail : RemoteException");
                return -1;
            }
        }
    }

    public List<String> listJoinedGroups(int groupType) {
        LogUtils.i(TAG, "list all joined groups by groupType in HwDeviceGroupManager.");
        this.mGroupManageServiceAccessMonitor.startDaemon();
        List<String> groupIdList = null;
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return null;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService != null) {
                try {
                    groupIdList = this.mGroupManageService.listJoinedDevGroups(this.mCallerPkgName, groupType);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "list joined groups' IDs error : RemoteException");
                }
            } else {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
            }
        }
        return groupIdList;
    }

    public List<String> listTrustedDevices(String groupId) {
        LogUtils.i(TAG, "list all trust devices by groupId in HwDeviceGroupManager.");
        List<String> deviceIdList = null;
        if (groupId == null) {
            LogUtils.e(TAG, "invalid parameter when call listTrustedDevices");
            return null;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return null;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService != null) {
                try {
                    deviceIdList = this.mGroupManageService.listTrustedDevicesInGroup(this.mCallerPkgName, groupId);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "list trusted devices' IDs error : RemoteException");
                }
            } else {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
            }
        }
        return deviceIdList;
    }

    public boolean isDeviceInGroup(String groupId, String deviceId) {
        LogUtils.i(TAG, "query if device is in group");
        if (groupId == null || deviceId == null) {
            LogUtils.e(TAG, "invalid parameter when call isDeviceInGroup");
            return false;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        LogUtils.d(TAG, "To decide if the device is in the group in HwDeviceGroupManager.");
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return false;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return false;
            }
            try {
                return this.mGroupManageService.isDeviceInDevGroup(this.mCallerPkgName, groupId, deviceId);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "query if device is in group error : RemoteException");
                return false;
            }
        }
    }

    public List<String> getGroupInfo(String queryParams) {
        LogUtils.i(TAG, "get group information in HwDeviceGroupManager.");
        List<String> groupInfoList = null;
        if (queryParams == null) {
            LogUtils.e(TAG, "invalid parameter when call getGroupInfo");
            return null;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return null;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService != null) {
                try {
                    groupInfoList = this.mGroupManageService.getDevGroupInfo(this.mCallerPkgName, queryParams);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "get group information fail : RemoteException");
                }
            } else {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
            }
        }
        return groupInfoList;
    }

    public int registerGroupNotice(String groupId, HichainGroupChangeListener groupChangeListener) {
        LogUtils.i(TAG, "register group change notice in HwDeviceGroupManager.");
        if (groupId == null || groupChangeListener == null) {
            LogUtils.e(TAG, "invalid parameter when call registerGroupNotice");
            return -1;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                this.mGroupChangeListener = groupChangeListener;
                return this.mGroupManageService.registerDevGroupNotice(this.mCallerPkgName, groupId, this.mGroupChangeCallbackBinder);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "register group notice fail : RemoteException");
                return -1;
            }
        }
    }

    public int revokeGroupNotice(String groupId) {
        LogUtils.i(TAG, "revoke group change notice in HwDeviceGroupManager.");
        if (groupId == null) {
            LogUtils.e(TAG, "invalid parameter when call revokeGroupNotice");
            return -1;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                return this.mGroupManageService.revokeDevGroupNotice(this.mCallerPkgName, groupId);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "revoke group notice fail : RemoteException");
                return -1;
            }
        }
    }

    public int setFriendsList(String groupId, List<String> friendsList) {
        LogUtils.i(TAG, "set friends list");
        if (groupId == null || friendsList == null) {
            LogUtils.e(TAG, "invalid parameters");
            return -1;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return -1;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return -1;
            }
            try {
                return this.mGroupManageService.setFriendsList(this.mCallerPkgName, groupId, friendsList);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "register group notice fail : RemoteException");
                return -1;
            }
        }
    }

    public List<String> getFriendsList(String groupId) {
        LogUtils.i(TAG, "get friends list");
        List<String> friendsList = null;
        if (groupId == null) {
            LogUtils.e(TAG, "invalid parameters");
            return null;
        }
        this.mGroupManageServiceAccessMonitor.startDaemon();
        if (!this.mRegisterFlag) {
            LogUtils.e(TAG, "Callback has not been registered.");
            return null;
        }
        synchronized (this.serviceInstanceLock) {
            if (this.mGroupManageService == null) {
                LogUtils.e(TAG, "HwDeviceGroupManage service hasn't connected yet");
                return null;
            }
            try {
                friendsList = this.mGroupManageService.getFriendsList(this.mCallerPkgName, groupId);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "register group notice fail : RemoteException");
            }
            return friendsList;
        }
    }

    private String packCallerId(String appId) {
        JSONObject callerId = new JSONObject();
        try {
            callerId.put(PARAMETER_TAG_APP_ID, appId);
            callerId.put("pkgName", this.mCallerPkgName);
            return callerId.toString();
        } catch (JSONException e) {
            LogUtils.w(TAG, "pack caller ID error");
            return appId;
        }
    }

    /* access modifiers changed from: private */
    public static class GroupManageServiceAccessMonitor {
        private static final int END_DAEMON = 1;
        private static final long IDLE_TIME_OUT_INTERVAL = 30000;
        private static final int START_DAEMON = 0;
        private static final String TAG = "HwDeviceGroupManager";
        private static final int TIMEOUT = 2;
        private static final String[] TIMEOUTED_UNBIND_BLACKLIST = {"com.huawei.nearby"};
        private String callerPackageName = BuildConfig.FLAVOR;
        private Handler handler = new Handler(Looper.getMainLooper()) {
            /* class com.huawei.security.deviceauth.HwDeviceGroupManager.GroupManageServiceAccessMonitor.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 0) {
                    LogUtils.i(GroupManageServiceAccessMonitor.TAG, "HwDeviceGroupManage service monitor start");
                    if (GroupManageServiceAccessMonitor.this.handler != null) {
                        GroupManageServiceAccessMonitor.this.handler.removeMessages(2);
                        GroupManageServiceAccessMonitor.this.handler.sendMessageDelayed(Message.obtain(this, 2), GroupManageServiceAccessMonitor.IDLE_TIME_OUT_INTERVAL);
                    }
                } else if (i == 1) {
                    LogUtils.i(GroupManageServiceAccessMonitor.TAG, "HwDeviceGroupManage service monitor stop");
                    if (GroupManageServiceAccessMonitor.this.handler != null) {
                        GroupManageServiceAccessMonitor.this.handler.removeMessages(2);
                    }
                } else if (i != 2) {
                    LogUtils.w(GroupManageServiceAccessMonitor.TAG, "unhandled msg for daemon " + msg.what);
                } else {
                    LogUtils.i(GroupManageServiceAccessMonitor.TAG, "daemon timeout and unbind the service.");
                    if (GroupManageServiceAccessMonitor.this.mManager != null) {
                        HwDeviceGroupManager manager = (HwDeviceGroupManager) GroupManageServiceAccessMonitor.this.mManager.get();
                        synchronized (HwDeviceGroupManager.MANAGER_MAP) {
                            if (manager != null) {
                                manager.blindlyUnbind();
                            }
                        }
                    }
                }
            }
        };
        private WeakReference<HwDeviceGroupManager> mManager;

        public GroupManageServiceAccessMonitor(HwDeviceGroupManager manager, String callerPackageName2) {
            this.mManager = new WeakReference<>(manager);
            this.callerPackageName = callerPackageName2;
        }

        public void startDaemon() {
            boolean isInBlackList = false;
            String[] strArr = TIMEOUTED_UNBIND_BLACKLIST;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (strArr[i].equals(this.callerPackageName)) {
                    isInBlackList = true;
                    break;
                } else {
                    i++;
                }
            }
            if (isInBlackList) {
                Message.obtain(this.handler, 0).sendToTarget();
            }
        }

        public void stopDaemon() {
            Message.obtain(this.handler, 1).sendToTarget();
        }
    }
}
