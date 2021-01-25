package com.huawei.coauthservice.identitymgr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import com.huawei.coauthservice.identitymgr.callback.IConnectServiceCallback;
import com.huawei.coauthservice.identitymgr.callback.ICreateGroupCallback;
import com.huawei.coauthservice.identitymgr.callback.IDeleteGroupCallback;
import com.huawei.coauthservice.identitymgr.callback.IGetGroupCallback;
import com.huawei.coauthservice.identitymgr.callback.IPurgeGroupCallback;
import com.huawei.coauthservice.identitymgr.constants.ServicePackage;
import com.huawei.coauthservice.identitymgr.feature.ICreateIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IDeleteIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IGetIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IInitIdmServiceCallback;
import com.huawei.coauthservice.identitymgr.feature.IPurgeIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager;
import com.huawei.coauthservice.identitymgr.model.CreateGroupInfo;
import com.huawei.coauthservice.identitymgr.model.DeleteGroupInfo;
import com.huawei.coauthservice.identitymgr.model.DeviceInfo;
import com.huawei.coauthservice.identitymgr.model.GroupInfo;
import com.huawei.coauthservice.identitymgr.model.LinkType;
import com.huawei.coauthservice.identitymgr.model.PurgeGroupInfo;
import com.huawei.coauthservice.identitymgr.model.UserType;
import com.huawei.coauthservice.identitymgr.utils.ArgsValidationUtils;
import com.huawei.coauthservice.identitymgr.utils.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HwIdentityManager {
    private static final int INITIAL_CAPACITY = 16;
    private static final Object LOCK = new Object();
    private static final String TAG = HwIdentityManager.class.getSimpleName();
    private ServiceConnection mCoAuthServiceConnection = new ServiceConnection() {
        /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.error(HwIdentityManager.TAG, "onServiceDisconnected: coauthService disconnected");
            HwIdentityManager.this.mConnectServiceCallback.onDisconnect();
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.info(HwIdentityManager.TAG, "onServiceConnected: coauthService connected");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(ServicePackage.COAUTH_PACKAGE, ServicePackage.IDENTITY_SERVICE_NAME));
            try {
                HwIdentityManager.this.mContext.bindService(intent, HwIdentityManager.this.mIdmServiceConnection, 1);
            } catch (SecurityException e) {
                LogUtils.error(HwIdentityManager.TAG, "connect idm service: illegal access");
                HwIdentityManager.this.mConnectServiceCallback.onConnectFailed();
            }
        }
    };
    private IConnectServiceCallback mConnectServiceCallback;
    private Context mContext;
    private IUserIdentityManager mIdentityService;
    private ServiceConnection mIdmServiceConnection = new ServiceConnection() {
        /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.error(HwIdentityManager.TAG, "onServiceDisconnected: identityService disconnected");
            HwIdentityManager.this.mConnectServiceCallback.onDisconnect();
            HwIdentityManager.this.mIdentityService = null;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.info(HwIdentityManager.TAG, "onServiceConnected: identityService connected");
            HwIdentityManager.this.mIdentityService = IUserIdentityManager.Stub.asInterface(service);
            try {
                HwIdentityManager.this.mIdentityService.initService(UserType.valueOf(HwIdentityManager.this.mUserType.name()), new IInitIdmServiceCallback.Stub() {
                    /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass2.AnonymousClass1 */

                    @Override // com.huawei.coauthservice.identitymgr.feature.IInitIdmServiceCallback
                    public void onSuccess() {
                        HwIdentityManager.this.mConnectServiceCallback.onConnected();
                    }

                    @Override // com.huawei.coauthservice.identitymgr.feature.IInitIdmServiceCallback
                    public void onFailed(int reason) {
                        String str = HwIdentityManager.TAG;
                        LogUtils.error(str, "onServiceConnected: init failed: " + reason);
                        HwIdentityManager.this.mIdentityService = null;
                        HwIdentityManager.this.mConnectServiceCallback.onConnectFailed();
                    }
                });
                service.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass2.AnonymousClass2 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        HwIdentityManager.this.mIdentityService = null;
                    }
                }, 0);
            } catch (RemoteException e) {
                LogUtils.error(HwIdentityManager.TAG, "onServiceConnected: occur remoteException");
                HwIdentityManager.this.mIdentityService = null;
                HwIdentityManager.this.mConnectServiceCallback.onConnectFailed();
            }
        }
    };
    private IdmUserType mUserType;

    private HwIdentityManager(Context context, IdmUserType userType) {
        this.mContext = context;
        this.mUserType = userType;
    }

    public static synchronized HwIdentityManager getInstance(@NonNull Context context, @NonNull IdmUserType userType) {
        synchronized (HwIdentityManager.class) {
            if (ArgsValidationUtils.isNull(context, userType)) {
                return null;
            }
            return new HwIdentityManager(context, userType);
        }
    }

    public void connectService(@NonNull IConnectServiceCallback callback) {
        synchronized (LOCK) {
            LogUtils.info(TAG, "connectService: start connect service");
            if (ArgsValidationUtils.isNull(callback)) {
                LogUtils.error(TAG, "connectService: illegal parameters");
                return;
            }
            this.mConnectServiceCallback = callback;
            if (this.mContext != null) {
                if (isCoAuthPackageExist(this.mContext)) {
                    if (Objects.isNull(this.mIdentityService)) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(ServicePackage.COAUTH_PACKAGE, ServicePackage.COAUTH_SERVICE_NAME));
                        LogUtils.info(TAG, "Start bindService.");
                        try {
                            this.mContext.bindService(intent, this.mCoAuthServiceConnection, 1);
                        } catch (SecurityException e) {
                            LogUtils.error(TAG, "connectService: illegal access");
                            this.mConnectServiceCallback.onConnectFailed();
                        }
                    } else {
                        LogUtils.info(TAG, "connectService: identityService is not null");
                        this.mConnectServiceCallback.onConnected();
                    }
                    return;
                }
            }
            LogUtils.info(TAG, "package not exist?");
            this.mConnectServiceCallback.onConnectFailed();
        }
    }

    private boolean isCoAuthPackageExist(Context context) {
        boolean isExist = false;
        try {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                return false;
            }
            boolean z = false;
            ApplicationInfo info = packageManager.getApplicationInfo(ServicePackage.COAUTH_PACKAGE, 0);
            if (info != null) {
                if (isSystemApp(info) && isSignedWithPlatformKey(context, ServicePackage.COAUTH_PACKAGE, packageManager)) {
                    z = true;
                }
                isExist = z;
            }
            String str = TAG;
            LogUtils.debug(str, "CoAuth package exist info=" + isExist);
            return isExist;
        } catch (PackageManager.NameNotFoundException e) {
            isExist = false;
        }
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }

    private boolean isSignedWithPlatformKey(Context context, String packageName, PackageManager packageManager) {
        return packageManager.checkSignatures(packageName, ServicePackage.CHECK_PACKAGE) == 0;
    }

    public void disConnectService() {
        synchronized (LOCK) {
            LogUtils.info(TAG, "disConnectService: start disconnect service");
            if (Objects.nonNull(this.mIdentityService)) {
                try {
                    this.mContext.unbindService(this.mIdmServiceConnection);
                    this.mContext.unbindService(this.mCoAuthServiceConnection);
                } catch (IllegalArgumentException e) {
                    LogUtils.warn(TAG, "service has not been connected");
                }
            }
            this.mIdentityService = null;
            if (Objects.nonNull(this.mConnectServiceCallback)) {
                this.mConnectServiceCallback.onDisconnect();
            }
        }
    }

    public int createGroup(@NonNull IdmCreateGroupInfo idmCreateGroupInfo, @NonNull final ICreateGroupCallback callback) {
        LogUtils.info(TAG, "createGroup: start create group");
        if (ArgsValidationUtils.isNull(idmCreateGroupInfo, callback)) {
            LogUtils.error(TAG, "createGroup: illegal parameters");
            return 2;
        }
        ICreateIdmGroupCallback.Stub callbackStub = new ICreateIdmGroupCallback.Stub() {
            /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass3 */

            @Override // com.huawei.coauthservice.identitymgr.feature.ICreateIdmGroupCallback
            public void onSuccess(GroupInfo info) {
                LogUtils.info(HwIdentityManager.TAG, "createGroup: group created successfully");
                callback.onSuccess(HwIdentityManager.this.transGroupInfo(info));
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.ICreateIdmGroupCallback
            public void onFailed(int reason) {
                String str = HwIdentityManager.TAG;
                LogUtils.error(str, "createGroup: failed to create group, reason: " + reason);
                callback.onFailed(reason);
            }
        };
        if (this.mIdentityService == null) {
            return 1;
        }
        try {
            this.mIdentityService.createGroup(transCreateModel(idmCreateGroupInfo), callbackStub);
            LogUtils.info(TAG, "createGroup: create group completed");
            return 0;
        } catch (RemoteException e) {
            LogUtils.error(TAG, "createGroup: identityService failed to create group");
            return 1;
        }
    }

    public int deleteGroup(@NonNull IdmDeleteGroupInfo idmDeleteGroupInfo, @NonNull final IDeleteGroupCallback callback) {
        LogUtils.info(TAG, "deleteGroup: start delete group");
        if (ArgsValidationUtils.isNull(idmDeleteGroupInfo, callback)) {
            LogUtils.error(TAG, "deleteGroup: illegal parameters");
            return 2;
        }
        IDeleteIdmGroupCallback.Stub callbackStub = new IDeleteIdmGroupCallback.Stub() {
            /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass4 */

            @Override // com.huawei.coauthservice.identitymgr.feature.IDeleteIdmGroupCallback
            public void onSuccess(int result) {
                String str = HwIdentityManager.TAG;
                LogUtils.info(str, "deleteGroup: group deleted successfully, result: " + result);
                callback.onSuccess(result);
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.IDeleteIdmGroupCallback
            public void onFailed(int reason) {
                String str = HwIdentityManager.TAG;
                LogUtils.error(str, "deleteGroup: failed to delete group, reason: " + reason);
                callback.onFailed(reason);
            }
        };
        if (this.mIdentityService == null) {
            return 1;
        }
        try {
            this.mIdentityService.deleteGroup(transDeleteModel(idmDeleteGroupInfo), callbackStub);
            LogUtils.info(TAG, "deleteGroup: delete group completed");
            return 0;
        } catch (RemoteException e) {
            LogUtils.error(TAG, "deleteGroup: identityService failed to delete group");
            return 1;
        }
    }

    public int getGroups(String moduleName, @NonNull final IGetGroupCallback callback) {
        LogUtils.info(TAG, "getGroups: start get all groups");
        if (ArgsValidationUtils.isEmpty(moduleName)) {
            LogUtils.error(TAG, "getGroups: illegal parameters moduleName");
            return 2;
        } else if (ArgsValidationUtils.isNull(callback)) {
            LogUtils.error(TAG, "getGroups: illegal parameters callback");
            return 2;
        } else {
            IGetIdmGroupCallback.Stub callbackStub = new IGetIdmGroupCallback.Stub() {
                /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass5 */

                @Override // com.huawei.coauthservice.identitymgr.feature.IGetIdmGroupCallback
                public void onSuccess(List<GroupInfo> infos) {
                    List<IdmGroupInfo> idmGroupInfoLst = HwIdentityManager.this.transGroupInfoLst(infos);
                    LogUtils.info(HwIdentityManager.TAG, "getGroups: group get successfully");
                    callback.onSuccess(idmGroupInfoLst);
                }

                @Override // com.huawei.coauthservice.identitymgr.feature.IGetIdmGroupCallback
                public void onFailed(int reason) {
                    String str = HwIdentityManager.TAG;
                    LogUtils.error(str, "getGroups: failed to get group, reason: " + reason);
                    callback.onFailed(reason);
                }
            };
            IUserIdentityManager iUserIdentityManager = this.mIdentityService;
            if (iUserIdentityManager != null) {
                try {
                    iUserIdentityManager.getGroups(UserType.valueOf(this.mUserType.name()), moduleName, callbackStub);
                    LogUtils.info(TAG, "getGroups: get group completed");
                    return 0;
                } catch (RemoteException e) {
                    LogUtils.error(TAG, "getGroups: identityService get all groups failed");
                    return 1;
                }
            } else {
                LogUtils.error(TAG, "getGroups: identityService get all groups failed");
                return 1;
            }
        }
    }

    public int purgeGroup(String moduleName, final IPurgeGroupCallback callback) {
        LogUtils.info(TAG, "purgeGroup: start purge group");
        if (ArgsValidationUtils.isNull(callback) || ArgsValidationUtils.isEmpty(moduleName)) {
            LogUtils.error(TAG, "purgeGroup: illegal parameters");
            return 2;
        }
        IPurgeIdmGroupCallback.Stub callbackStub = new IPurgeIdmGroupCallback.Stub() {
            /* class com.huawei.coauthservice.identitymgr.HwIdentityManager.AnonymousClass6 */

            @Override // com.huawei.coauthservice.identitymgr.feature.IPurgeIdmGroupCallback
            public void onSuccess(int result) {
                String str = HwIdentityManager.TAG;
                LogUtils.info(str, "purgeGroup: group purged successfully, result: " + result);
                callback.onSuccess(result);
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.IPurgeIdmGroupCallback
            public void onFailed(int reason) {
                String str = HwIdentityManager.TAG;
                LogUtils.error(str, "purgeGroup: failed to purge group, reason: " + reason);
                callback.onFailed(reason);
            }
        };
        if (this.mIdentityService == null) {
            return 1;
        }
        try {
            PurgeGroupInfo purgeGroupInfo = transPurgeModel(moduleName);
            String str = TAG;
            LogUtils.info(str, "purgeGroup: group purged purgeGroupInfo: " + purgeGroupInfo);
            this.mIdentityService.purgeGroup(purgeGroupInfo, callbackStub);
            LogUtils.info(TAG, "purgeGroup: purge group completed");
            return 0;
        } catch (RemoteException e) {
            LogUtils.error(TAG, "purgeGroup: identityService failed to purge group");
            return 1;
        }
    }

    private CreateGroupInfo transCreateModel(IdmCreateGroupInfo idmCreateGroupInfo) {
        CreateGroupInfo createGroupInfo = new CreateGroupInfo();
        createGroupInfo.setOverwrite(idmCreateGroupInfo.isOverwrite());
        createGroupInfo.setModuleName(idmCreateGroupInfo.getModuleName());
        createGroupInfo.setUserType(UserType.valueOf(this.mUserType.name()));
        createGroupInfo.setPeerDeviceInfoList(transDeviceInfo(idmCreateGroupInfo.getPeerDeviceInfoList()));
        return createGroupInfo;
    }

    private List<DeviceInfo> transDeviceInfo(List<IdmDeviceInfo> idmDeviceInfoLst) {
        List<DeviceInfo> deviceInfoLst = new ArrayList<>(16);
        if (!ArgsValidationUtils.isNull(idmDeviceInfoLst) && !idmDeviceInfoLst.isEmpty()) {
            for (IdmDeviceInfo idmDeviceInfo : idmDeviceInfoLst) {
                if (!ArgsValidationUtils.isNull(idmDeviceInfo)) {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setDeviceId(idmDeviceInfo.getDeviceId());
                    deviceInfo.setLinkType(LinkType.valueOf(idmDeviceInfo.getIdmLinkType().name()));
                    if (!ArgsValidationUtils.isEmpty(idmDeviceInfo.getIp())) {
                        deviceInfo.setIp(idmDeviceInfo.getIp());
                    }
                    deviceInfoLst.add(deviceInfo);
                }
            }
        }
        return deviceInfoLst;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IdmGroupInfo transGroupInfo(GroupInfo info) {
        IdmGroupInfo idmGroupInfo = new IdmGroupInfo();
        idmGroupInfo.setAdminId(info.getAdminId());
        idmGroupInfo.setGroupId(info.getGroupId());
        return idmGroupInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private List<IdmGroupInfo> transGroupInfoLst(List<GroupInfo> infos) {
        List<IdmGroupInfo> idmGroupInfoLst = new ArrayList<>(16);
        if (!ArgsValidationUtils.isNull(infos) && !infos.isEmpty()) {
            for (GroupInfo groupInfo : infos) {
                if (!ArgsValidationUtils.isNull(groupInfo)) {
                    idmGroupInfoLst.add(transGroupInfo(groupInfo));
                }
            }
        }
        return idmGroupInfoLst;
    }

    private DeleteGroupInfo transDeleteModel(IdmDeleteGroupInfo idmDeleteGroupInfo) {
        DeleteGroupInfo deleteGroupInfo = new DeleteGroupInfo();
        deleteGroupInfo.setUserType(UserType.valueOf(this.mUserType.name()));
        deleteGroupInfo.setGroupId(idmDeleteGroupInfo.getGroupId());
        deleteGroupInfo.setPeerDeviceInfoList(transDeviceInfo(idmDeleteGroupInfo.getPeerDeviceInfoList()));
        return deleteGroupInfo;
    }

    private PurgeGroupInfo transPurgeModel(String moduleName) {
        PurgeGroupInfo purgeGroupInfo = new PurgeGroupInfo();
        purgeGroupInfo.setModuleName(moduleName);
        purgeGroupInfo.setUserType(UserType.valueOf(this.mUserType.name()));
        return purgeGroupInfo;
    }
}
