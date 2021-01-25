package com.huawei.security.deviceauth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.huawei.coauth.auth.CoAuthUtil;
import com.huawei.hwpartsecurity.BuildConfig;
import com.huawei.security.deviceauth.HwDeviceGroupManager;
import com.huawei.security.deviceauth.ICallbackMethods;
import com.huawei.security.deviceauth.IHichainService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

public final class HwDeviceAuthManager {
    public static final String BUNDLE_TAG_HOME_ID_EXPORT_ACCOUNT = "homeIdExportAccount";
    public static final String BUNDLE_TAG_HOME_ID_EXPORT_PIN = "homeIdExportPin";
    public static final String BUNDLE_TAG_HOME_ID_EXPORT_TYPE = "homeIdExportType";
    public static final int EXPORT_DATA_FULL_AUTH_INFO = 0;
    public static final int EXPORT_DATA_LITE_AUTH_INFO = 1;
    public static final int EXPORT_DATA_PIN_HOME_ID = 4;
    public static final int EXPORT_DATA_SIGNED_AUTH_INFO = 2;
    public static final int EXPORT_DATA_TRUST_CIRCLE_HOME_ID = 3;
    private static final char[] HEX_ARRAY = CoAuthUtil.HEX_CHAR.toCharArray();
    private static final int MAX_AUTH_ID_LEN = 64;
    private static final long REQ_ID = 1;
    private static final String SERVICE_NAME = "com.huawei.deviceauth.HwDeviceAuthService";
    private static final String SERVICE_PACKAGE_NAME = "com.huawei.deviceauth";
    private static final String TAG = "HichainDevAuthManager";
    private static volatile HwDeviceAuthManager sHwDeviceAuthManager;
    private final IBinder mBinderToken = new Binder();
    private final ExecutorService mCallbackJob = Executors.newSingleThreadExecutor();
    private final String mCallerPackageName;
    private final HwDevAuthConnectionCallback mConnectionCallback;
    private final Context mContext;
    private IHichainService mDeviceAuthService = null;
    private HwDeviceGroupManager.HichainGroupCallback mGroupManageCallback = new HwDeviceGroupManager.HichainGroupCallback() {
        /* class com.huawei.security.deviceauth.HwDeviceAuthManager.AnonymousClass1 */

        @Override // com.huawei.security.deviceauth.HwDeviceGroupManager.HichainGroupCallback
        public void onFinish(long requestId, GroupOperation operationCode, String returnData) {
            LogUtils.d(HwDeviceAuthManager.TAG, "call onFinish");
        }

        @Override // com.huawei.security.deviceauth.HwDeviceGroupManager.HichainGroupCallback
        public void onError(long requestId, GroupOperation operationCode, int errorCode, String errorReturn) {
            LogUtils.d(HwDeviceAuthManager.TAG, "call onError");
        }

        @Override // com.huawei.security.deviceauth.HwDeviceGroupManager.HichainGroupCallback
        public String onRequest(long requestId, GroupOperation operationCode, String reqParams) {
            LogUtils.d(HwDeviceAuthManager.TAG, "call onRequest");
            return BuildConfig.FLAVOR;
        }
    };
    private HwDeviceGroupManager mGroupManager;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.security.deviceauth.HwDeviceAuthManager.AnonymousClass2 */

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.i(HwDeviceAuthManager.TAG, "onServiceDisconnected started");
            HwDeviceAuthManager.this.mDeviceAuthService = null;
            try {
                LogUtils.i(HwDeviceAuthManager.TAG, "inform service disconnection");
                HwDeviceAuthManager.this.mConnectionCallback.onServiceDisconnected();
            } catch (Exception e) {
                LogUtils.e(HwDeviceAuthManager.TAG, "inform service disconnection fail");
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.i(HwDeviceAuthManager.TAG, "onServiceConnected started");
            HwDeviceAuthManager.this.mDeviceAuthService = IHichainService.Stub.asInterface(service);
            try {
                service.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.huawei.security.deviceauth.HwDeviceAuthManager.AnonymousClass2.AnonymousClass1 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        LogUtils.e(HwDeviceAuthManager.TAG, "binderDied");
                        HwDeviceAuthManager.this.mDeviceAuthService.asBinder().unlinkToDeath(this, 0);
                        LogUtils.i(HwDeviceAuthManager.TAG, "inform service disconnection");
                        HwDeviceAuthManager.this.mConnectionCallback.onServiceDisconnected();
                        HwDeviceAuthManager.this.mDeviceAuthService = null;
                    }
                }, 0);
                LogUtils.i(HwDeviceAuthManager.TAG, "inform service connection");
                HwDeviceAuthManager.this.mConnectionCallback.onServiceConnected();
            } catch (RemoteException e) {
                HwDeviceAuthManager.this.mDeviceAuthService = null;
                LogUtils.e(HwDeviceAuthManager.TAG, "occur remoteException when call binder linkToDeath");
            } catch (Exception e2) {
                LogUtils.e(HwDeviceAuthManager.TAG, "inform service connection fail");
            }
        }
    };

    private HwDeviceAuthManager(Context context, HwDevAuthConnectionCallback connectionCallback) {
        this.mContext = context;
        this.mCallerPackageName = context.getPackageName();
        this.mConnectionCallback = connectionCallback;
        connectDeviceAuthService();
    }

    public static HwDeviceAuthManager getInstance(Context context, HwDevAuthConnectionCallback connectionCallback) {
        HwDevAuthConnectionCallback connCallbacks;
        LogUtils.i(TAG, "start getInstance");
        if (sHwDeviceAuthManager == null) {
            synchronized (HwDeviceAuthManager.class) {
                if (context == null) {
                    try {
                        return null;
                    } catch (Throwable th) {
                        throw th;
                    }
                } else {
                    if (connectionCallback == null) {
                        connCallbacks = new HwDevAuthConnectionCallback() {
                            /* class com.huawei.security.deviceauth.HwDeviceAuthManager.AnonymousClass3 */

                            @Override // com.huawei.security.deviceauth.HwDevAuthConnectionCallback
                            public void onServiceDisconnected() {
                            }

                            @Override // com.huawei.security.deviceauth.HwDevAuthConnectionCallback
                            public void onServiceConnected() {
                            }
                        };
                    } else {
                        connCallbacks = connectionCallback;
                    }
                    LogUtils.d(TAG, "HwDeviceAuthManager instance created by " + context.getPackageName());
                    if (sHwDeviceAuthManager == null) {
                        sHwDeviceAuthManager = new HwDeviceAuthManager(context, connCallbacks);
                    }
                }
            }
        } else {
            sHwDeviceAuthManager.connectDeviceAuthService();
        }
        return sHwDeviceAuthManager;
    }

    private ICallbackMethods getManagerCallback(final HwDevAuthCallback devAuthCallbackMethods) {
        if (devAuthCallbackMethods == null) {
            return null;
        }
        return new ICallbackMethods.Stub() {
            /* class com.huawei.security.deviceauth.HwDeviceAuthManager.AnonymousClass4 */

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public void onOperationFinished(String sessionId, int operationCode, int result) {
                LogUtils.i(HwDeviceAuthManager.TAG, "start onOperationFinished");
                try {
                    devAuthCallbackMethods.onOperationFinished(sessionId, OperationCode.valueOf(operationCode), result, null);
                } catch (Exception e) {
                    LogUtils.e(HwDeviceAuthManager.TAG, "return operation result error");
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public void onOperationFinishedWithData(String sessionId, int operationCode, int result, byte[] returnData) {
                LogUtils.i(HwDeviceAuthManager.TAG, "start onOperationFinishedWithData");
                try {
                    devAuthCallbackMethods.onOperationFinished(sessionId, OperationCode.valueOf(operationCode), result, returnData);
                } catch (Exception e) {
                    LogUtils.e(HwDeviceAuthManager.TAG, "return operation result and data error");
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public boolean onPassthroughDataGenerated(String targetSessionId, byte[] passthroughData) {
                LogUtils.i(HwDeviceAuthManager.TAG, "start onPassthroughDataGenerated");
                try {
                    return devAuthCallbackMethods.onDataTransmit(targetSessionId, passthroughData);
                } catch (Exception e) {
                    LogUtils.e(HwDeviceAuthManager.TAG, "can't transmit passthrough data");
                    return false;
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public ConfirmParams onReceiveRequest(String sessionId, int operationCode) {
                LogUtils.i(HwDeviceAuthManager.TAG, "start onReceiveRequest");
                ConfirmParams confirmParams = new ConfirmParams(-2147483643, BuildConfig.FLAVOR, 0);
                try {
                    confirmParams = devAuthCallbackMethods.onReceiveRequest(sessionId, OperationCode.valueOf(operationCode));
                    LogUtils.i(HwDeviceAuthManager.TAG, "call onReceiveRequest success, aquire confirmParams");
                    return confirmParams;
                } catch (Exception e) {
                    LogUtils.e(HwDeviceAuthManager.TAG, "acquire upper permission error");
                    return confirmParams;
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public void onSessionKeyReturned(String sessionId, byte[] sessionKey) {
                LogUtils.i(HwDeviceAuthManager.TAG, "start onSessionKeyReturned");
                try {
                    devAuthCallbackMethods.onSessionKeyReturned(sessionId, sessionKey);
                } catch (Exception e) {
                    LogUtils.e(HwDeviceAuthManager.TAG, "return session key error");
                }
            }
        };
    }

    public void connectDeviceAuthService() {
        if (this.mDeviceAuthService == null) {
            LogUtils.i(TAG, this.mCallerPackageName + " connects HwDeviceAuthService...");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(SERVICE_PACKAGE_NAME, SERVICE_NAME));
            try {
                if (!this.mContext.bindService(intent, 9, this.mCallbackJob, this.mServiceConnection)) {
                    LogUtils.e(TAG, "fail to bind HwDeviceAuthService");
                } else {
                    LogUtils.i(TAG, "bind service ok");
                }
            } catch (SecurityException e) {
                LogUtils.e(TAG, "can't find HwDeviceAuthService or has no permission to access it");
            }
        } else {
            LogUtils.i(TAG, "service has already been connected");
        }
    }

    public void disconnectDeviceAuthService() {
        LogUtils.i(TAG, this.mCallerPackageName + " disconnects HwDeviceAuthService...");
        try {
            this.mContext.unbindService(this.mServiceConnection);
        } catch (IllegalArgumentException e) {
            LogUtils.w(TAG, "service has not been connected");
        }
        this.mDeviceAuthService = null;
        try {
            LogUtils.i(TAG, "inform service disconnection");
            this.mConnectionCallback.onServiceDisconnected();
        } catch (Exception e2) {
            LogUtils.e(TAG, "inform service disconnection fail");
        }
        HwDeviceGroupManager hwDeviceGroupManager = this.mGroupManager;
        if (hwDeviceGroupManager != null) {
            hwDeviceGroupManager.unbindHwGroupManageService();
        }
    }

    public int registerNewUser(UserInfo userInfo, int syncType, String accountId, HwDevAuthCallback callbackHandler) {
        if (!checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid userInfo when call registerNewUser");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (syncType != 0 && (accountId == null || callbackHandler == null)) {
            LogUtils.e(TAG, "invalid syncInfo when call registerNewUser");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (this.mDeviceAuthService == null) {
            LogUtils.e(TAG, "service disconnected when call registerNewUser");
            return ReturnCode.SERVICE_DISCONNECTED;
        } else if (callbackHandler == null && accountId == null) {
            try {
                LogUtils.d(TAG, "invoke local register");
                return this.mDeviceAuthService.register(this.mCallerPackageName, userInfo);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call registerNewUser");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call registerNewUser");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.d(TAG, "invoke register with uid");
            return this.mDeviceAuthService.registerWithCloud(this.mCallerPackageName, userInfo, syncType, accountId, getManagerCallback(callbackHandler));
        }
    }

    public int registerNewUser(UserInfo userInfo) {
        return registerNewUser(userInfo, 0, null, null);
    }

    public int getSessionKeyWithPin(OperationParameter opParams, String pin, int keyLength) {
        if (!checkParamsValidity(opParams, null) || pin == null || keyLength < 0) {
            LogUtils.e(TAG, "invalid parameters when call authKeyAgree");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.authKeyAgree(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), pin, keyLength);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call authKeyAgree");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call authKeyAgree");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call authKeyAgree");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int bindPeer(OperationParameter opParams, String pin, int keyLength) {
        if (!checkParamsValidity(opParams, null) || pin == null || keyLength < 0) {
            LogUtils.e(TAG, "invalid parameters when call bindPeer");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.bind(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), pin, keyLength);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call bindPeer");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call bindPeer");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call bindPeer");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int authenticatePeer(OperationParameter opParams, @Nullable String targetPackageName, int keyLength) {
        if (!checkParamsValidity(opParams, targetPackageName) || keyLength < 0) {
            LogUtils.e(TAG, "invaild parameters when call authenticate");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService == null) {
            LogUtils.e(TAG, "service disconnected when call authenticate");
            return ReturnCode.SERVICE_DISCONNECTED;
        } else if (targetPackageName != null) {
            return iHichainService.authenticateAcrossProcess(this.mCallerPackageName, targetPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), keyLength);
        } else {
            try {
                return iHichainService.authenticate(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), keyLength);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "RemoteException occurs when call authenticate");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call authenticate");
                return ReturnCode.UNKOWN;
            }
        }
    }

    public int authenticatePeer(OperationParameter operationParams, int keyLength) {
        return authenticatePeer(operationParams, null, keyLength);
    }

    public int addAuthInfo(OperationParameter opParams, int keyLength, byte[] addId, int addType) {
        if (!UserType.validUserType(addType) || !checkParamsValidity(opParams, null) || keyLength < 0) {
            LogUtils.e(TAG, "invalid parameters when call addAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (addId == null || addId.length > 64) {
            LogUtils.e(TAG, "invalid added authId when call addAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (this.mDeviceAuthService != null) {
            try {
                return this.mDeviceAuthService.addAuthInfo(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), keyLength, new UserInfo(opParams.getServiceType(), addId, addType));
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when callAddAuthInfo");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when callAddAuthInfo");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call addAuthInfo");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int removeAuthInfo(OperationParameter opParams, int keyLength, byte[] removeId, int removeType) {
        if (!UserType.validUserType(removeType) || !checkParamsValidity(opParams, null) || keyLength < 0) {
            LogUtils.e(TAG, "invalid parameters when call removeAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (removeId == null || removeId.length > 64) {
            LogUtils.e(TAG, "invalid removed authId when call removeAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (this.mDeviceAuthService != null) {
            try {
                return this.mDeviceAuthService.removeAuthInfo(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), keyLength, new UserInfo(opParams.getServiceType(), removeId, removeType));
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call removeAuthInfo");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call removeAuthInfo");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call removeAuthInfo");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int unBindPeer(OperationParameter opParams) {
        if (!checkParamsValidity(opParams, null)) {
            LogUtils.e(TAG, "invalid parameters when call unBind");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.unBind(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()));
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call unBind");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call unBind");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call unBind");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public void processReceivedData(OperationParameter opParams, byte[] receivedData) {
        if (!checkParamsValidity(opParams, null) || receivedData == null || receivedData.length == 0) {
            LogUtils.e(TAG, "invalid parameters when call processReceivedData");
            return;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                iHichainService.processReceivedData(this.mCallerPackageName, opParams.getSessionInfo(this.mBinderToken), getManagerCallback(opParams.getCallbackHandler()), receivedData);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call processReceivedData");
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call processReceivedData");
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call processReceivedData");
        }
    }

    public int deleteLocalAuthInfo(UserInfo userInfo) {
        if (!checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid parameters when call deleteLocalAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.deleteLocalAuthInfo(this.mCallerPackageName, userInfo);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call deleteLocalAuthInfo");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call deleteLocalAuthInfo");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call deleteLocalAuthInfo");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int deleteTrustPeer(UserInfo userInfo) {
        return deleteLocalAuthInfo(userInfo);
    }

    public int unregisterLocalUser(UserInfo userInfo) {
        if (!checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid parameters when call unregister");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.unregister(this.mCallerPackageName, userInfo);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call unregister");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call unregister");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call invoking unregister");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int unregisterUser(UserInfo userInfo) {
        return unregisterLocalUser(userInfo);
    }

    public boolean isRegistered(UserInfo userInfo) {
        if (!checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid parameters when call isRegistered");
            return false;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.isRegistered(this.mCallerPackageName, userInfo);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call isRegistered");
                return false;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call isRegistered");
                return false;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call isRegistered");
            return false;
        }
    }

    public boolean isTrustPeer(UserInfo userInfo, @Nullable String targetPackageName, boolean localOnly) {
        if (!checkUserInfoValidity(userInfo, targetPackageName)) {
            LogUtils.e(TAG, "invalid parameters when call isTrustPeer");
            return false;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService == null) {
            LogUtils.e(TAG, "service disconnected when call isTrustPeer");
            return false;
        } else if (targetPackageName != null) {
            return iHichainService.isTrustPeerAcrossProcess(this.mCallerPackageName, targetPackageName, userInfo, localOnly);
        } else {
            try {
                return iHichainService.isTrustPeer(this.mCallerPackageName, userInfo, localOnly);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call isTrustPeer");
                return false;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call isTrustPeer");
                return false;
            }
        }
    }

    public boolean isTrustPeer(UserInfo userInfo) {
        return isTrustPeer(userInfo, null, true);
    }

    public List<String> listTrustPeers(@Nullable String targetPackageName, String serviceType, int trustUserType, byte[] ownerAuthId, boolean localOnly) {
        if (ownerAuthId != null && ownerAuthId.length > 64) {
            LogUtils.e(TAG, "invalid owner's authId when call listTrustPeers");
            return null;
        } else if (!UserType.validUserType(trustUserType)) {
            LogUtils.e(TAG, "invalid trustUserType when call listTrustPeers");
            return null;
        } else {
            String inServiceType = serviceType;
            if (inServiceType == null) {
                if (targetPackageName == null) {
                    inServiceType = this.mCallerPackageName;
                } else {
                    inServiceType = targetPackageName;
                }
            }
            IHichainService iHichainService = this.mDeviceAuthService;
            if (iHichainService == null) {
                LogUtils.e(TAG, "service disconnected when call listTrustPeers");
                return null;
            } else if (targetPackageName == null) {
                try {
                    return iHichainService.listTrustPeers(this.mCallerPackageName, inServiceType, trustUserType, ownerAuthId, localOnly);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "remoteException occurs when call listTrustPeers");
                    return null;
                } catch (Exception e2) {
                    LogUtils.e(TAG, "unknown Exception occurs when call listTrustPeers");
                    return null;
                }
            } else {
                return this.mDeviceAuthService.listTrustPeersAcrossProcess(this.mCallerPackageName, targetPackageName, new UserInfo(inServiceType, ownerAuthId, trustUserType), localOnly);
            }
        }
    }

    public List<String> listTrustPeers(String serviceType, int trustUserType, byte[] ownerId) {
        return listTrustPeers(null, serviceType, trustUserType, ownerId, true);
    }

    public int cancelRequest(String sessionId) {
        if (sessionId == null) {
            LogUtils.e(TAG, "invalid parameters when call cancel");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.cancel(this.mCallerPackageName, sessionId, this.mBinderToken);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call cancel");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown exception occurs when call cancel");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected when call cancel");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public int importAuthInfo(String serviceType, byte[] selfAuthId, int authInfoType, byte[] authInfoBlob) {
        if (selfAuthId != null && selfAuthId.length > 64) {
            LogUtils.e(TAG, "invalid self authId when call importAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (authInfoBlob == null) {
            LogUtils.e(TAG, "invalid authInfoBlob when call importAuthInfo");
            return ReturnCode.INVALID_PARAMETERS;
        } else {
            String inServiceType = serviceType;
            if (inServiceType == null) {
                inServiceType = this.mCallerPackageName;
            }
            IHichainService iHichainService = this.mDeviceAuthService;
            if (iHichainService != null) {
                try {
                    return iHichainService.importAuthInfo(this.mCallerPackageName, inServiceType, selfAuthId, authInfoType, authInfoBlob);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "remoteException occurs when call importAuthInfo");
                    return ReturnCode.CALL_REMOTE_ERROR;
                } catch (Exception e2) {
                    LogUtils.e(TAG, "unknown Exception occurs when call importAuthInfo");
                    return ReturnCode.UNKOWN;
                }
            } else {
                LogUtils.e(TAG, "service disconnected when call importAuthInfo");
                return ReturnCode.SERVICE_DISCONNECTED;
            }
        }
    }

    public ExportResult exportAuthInfo(UserInfo exportUserInfo, byte[] selfAuthId, int authInfoType) {
        ExportResult expResult = new ExportResult();
        if (selfAuthId != null && selfAuthId.length > 64) {
            LogUtils.e(TAG, "invalid self authId when call exportAuthInfo");
            expResult.setResult(ReturnCode.INVALID_PARAMETERS);
            return expResult;
        } else if (!checkUserInfoValidity(exportUserInfo, null)) {
            LogUtils.e(TAG, "invalid exportUserInfo when call exportAuthInfo");
            expResult.setResult(ReturnCode.INVALID_PARAMETERS);
            return expResult;
        } else {
            if (exportUserInfo.getServiceType() == null) {
                exportUserInfo.setServiceType(this.mCallerPackageName);
            }
            IHichainService iHichainService = this.mDeviceAuthService;
            if (iHichainService != null) {
                try {
                    return iHichainService.exportAuthInfo(this.mCallerPackageName, exportUserInfo, selfAuthId, authInfoType);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "remoteException occurs when call exportAuthInfo");
                    expResult.setResult(ReturnCode.CALL_REMOTE_ERROR);
                    return expResult;
                } catch (Exception e2) {
                    LogUtils.e(TAG, "unknown Exception occurs when call exportAuthInfo");
                    expResult.setResult(ReturnCode.UNKOWN);
                    return expResult;
                }
            } else {
                LogUtils.e(TAG, "service disconnected");
                expResult.setResult(ReturnCode.SERVICE_DISCONNECTED);
                return expResult;
            }
        }
    }

    public int cloneHomeId(OperationParameter operationParams, int keyLength) {
        if (!checkParamsValidity(operationParams, null) || keyLength < 0) {
            LogUtils.e(TAG, "invalid parameters when call secCloneControllerID");
            return ReturnCode.INVALID_PARAMETERS;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.cloneHomeId(this.mCallerPackageName, operationParams.getSessionInfo(this.mBinderToken), getManagerCallback(operationParams.getCallbackHandler()), keyLength);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call secClone");
                return ReturnCode.CALL_REMOTE_ERROR;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call secClone");
                return ReturnCode.UNKOWN;
            }
        } else {
            LogUtils.e(TAG, "service disconnected");
            return ReturnCode.SERVICE_DISCONNECTED;
        }
    }

    public byte[] enableAuthInfoExport(UserInfo userInfo) {
        if (!checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid parameters when call enableAuthInfoExport");
            return null;
        }
        IHichainService iHichainService = this.mDeviceAuthService;
        if (iHichainService != null) {
            try {
                return iHichainService.enableExport(this.mCallerPackageName, userInfo);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call enableAuthInfoExport");
                return null;
            } catch (Exception e2) {
                LogUtils.e(TAG, "unknown Exception occurs when call enableAuthInfoExport");
                return null;
            }
        } else {
            LogUtils.e(TAG, "service disconnected");
            return null;
        }
    }

    public ExportResult exportHomeId(UserInfo userInfo, Bundle exportParams) {
        ExportResult expResult = new ExportResult();
        if (!checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid userInfo");
            expResult.setResult(ReturnCode.INVALID_PARAMETERS);
            return expResult;
        } else if (exportParams == null) {
            LogUtils.e(TAG, "empty export parameters");
            expResult.setResult(ReturnCode.INVALID_PARAMETERS);
            return expResult;
        } else {
            int exporType = exportParams.getInt(BUNDLE_TAG_HOME_ID_EXPORT_TYPE, -1);
            String encryptKeyBase = null;
            if (exporType == 4) {
                encryptKeyBase = exportParams.getString(BUNDLE_TAG_HOME_ID_EXPORT_PIN);
            }
            if (exporType == 3) {
                encryptKeyBase = exportParams.getString(BUNDLE_TAG_HOME_ID_EXPORT_ACCOUNT);
            }
            if (encryptKeyBase == null) {
                LogUtils.e(TAG, "invalid export parameters");
                expResult.setResult(ReturnCode.INVALID_PARAMETERS);
                return expResult;
            }
            IHichainService iHichainService = this.mDeviceAuthService;
            if (iHichainService != null) {
                try {
                    return iHichainService.exportHomeId(this.mCallerPackageName, userInfo, exporType, encryptKeyBase);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "remoteException occurs when call exportHomeId");
                    expResult.setResult(ReturnCode.CALL_REMOTE_ERROR);
                    return expResult;
                } catch (Exception e2) {
                    LogUtils.e(TAG, "unknown Exception occurs when call exportHomeId");
                    expResult.setResult(ReturnCode.UNKOWN);
                    return expResult;
                }
            } else {
                LogUtils.e(TAG, "service disconnected");
                expResult.setResult(ReturnCode.SERVICE_DISCONNECTED);
                return expResult;
            }
        }
    }

    public int importHomeId(String serviceType, byte[] homeIdBlob, Bundle importParams) {
        String encryptKeyBase;
        if (homeIdBlob == null) {
            LogUtils.e(TAG, "empty import data");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (importParams == null) {
            LogUtils.e(TAG, "empty import parameters");
            return ReturnCode.INVALID_PARAMETERS;
        } else {
            String inServiceType = serviceType;
            if (inServiceType == null) {
                inServiceType = this.mCallerPackageName;
            }
            int importType = importParams.getInt(BUNDLE_TAG_HOME_ID_EXPORT_TYPE, -1);
            String encryptKeyBase2 = null;
            if (importType == 4) {
                encryptKeyBase2 = importParams.getString(BUNDLE_TAG_HOME_ID_EXPORT_PIN);
            }
            if (importType == 3) {
                encryptKeyBase = importParams.getString(BUNDLE_TAG_HOME_ID_EXPORT_ACCOUNT);
            } else {
                encryptKeyBase = encryptKeyBase2;
            }
            if (encryptKeyBase == null) {
                LogUtils.e(TAG, "invalid import parameters");
                return ReturnCode.INVALID_PARAMETERS;
            }
            IHichainService iHichainService = this.mDeviceAuthService;
            if (iHichainService != null) {
                try {
                    return iHichainService.importHomeId(this.mCallerPackageName, inServiceType, importType, encryptKeyBase, homeIdBlob);
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "remoteException occurs when call importHomeId");
                    return ReturnCode.CALL_REMOTE_ERROR;
                } catch (Exception e2) {
                    LogUtils.e(TAG, "unknown Exception occurs when call importHomeId");
                    return ReturnCode.UNKOWN;
                }
            } else {
                LogUtils.e(TAG, "service disconnected");
                return ReturnCode.SERVICE_DISCONNECTED;
            }
        }
    }

    public int registerGroup(String groupName, UserInfo userInfo, int groupVisibility) {
        LogUtils.i(TAG, "invoke registerGroup");
        if (groupName == null || !checkUserInfoValidity(userInfo, null)) {
            LogUtils.e(TAG, "invalid parameter when call registerGroup");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (!isHwDeviceGroupManagerGot()) {
            return 1;
        } else {
            JSONObject groupInfo = new JSONObject();
            try {
                groupInfo.put("deviceId", toHexString(userInfo.getAuthId()));
                groupInfo.put("groupVisibility", groupVisibility);
                groupInfo.put(HichainAuthManager.AUTH_TAG_SERVICE_TYPE, userInfo.getServiceType());
                groupInfo.put("userType", userInfo.getUserType());
                return this.mGroupManager.createGroup(this.mCallerPackageName, groupName, 512, groupInfo.toString());
            } catch (JSONException e) {
                LogUtils.e(TAG, "pack groupInfo error");
                return 1;
            }
        }
    }

    public int unregisterGroup(String groupName) {
        LogUtils.i(TAG, "invoke registerGroup");
        if (groupName == null) {
            LogUtils.e(TAG, "invalid parameter when call unregisterGroup");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (!isHwDeviceGroupManagerGot()) {
            return 1;
        } else {
            return this.mGroupManager.deleteGroup(getGroupId(groupName));
        }
    }

    public int addTrustedDevice(String groupName, String addedConnDeviceId, String addedAuthId) {
        String groupId;
        LogUtils.i(TAG, "invoke addTrustedDevice");
        if (groupName == null || addedConnDeviceId == null || addedAuthId == null) {
            LogUtils.e(TAG, "invalid parameter when call addTrustedDevice");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (!isHwDeviceGroupManagerGot() || (groupId = getGroupId(groupName)) == null) {
            return 1;
        } else {
            JSONObject connParams = new JSONObject();
            JSONObject addParams = new JSONObject();
            try {
                connParams.put("DEVICE_ID", addedConnDeviceId);
                addParams.put("groupId", groupId);
                addParams.put(HwDeviceGroupManager.PARAMETER_TAG_GROUP_NAME, groupName);
                addParams.put(HwDeviceGroupManager.PARAMETER_TAG_ADD_ID, addedAuthId);
                addParams.put(HwDeviceGroupManager.PARAMETER_TAG_PIN, "add_member");
                addParams.put(HwDeviceGroupManager.PARAMETER_TAG_IS_ADMIN, true);
                return this.mGroupManager.addMemberToGroup(this.mCallerPackageName, REQ_ID, addParams.toString(), connParams.toString(), 512);
            } catch (JSONException e) {
                LogUtils.e(TAG, "pack parameters error");
                return 1;
            }
        }
    }

    public int deleteTrustedDevice(String groupName, String deletedConnDeviceId) {
        String groupId;
        LogUtils.i(TAG, "invoke deleteTrustedDevice");
        if (groupName == null || deletedConnDeviceId == null) {
            LogUtils.e(TAG, "invalid parameter when call deleteTrustedDevice");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (!isHwDeviceGroupManagerGot() || (groupId = getGroupId(groupName)) == null) {
            return 1;
        } else {
            JSONObject connParams = new JSONObject();
            JSONObject deleteParams = new JSONObject();
            try {
                connParams.put("DEVICE_ID", deletedConnDeviceId);
                deleteParams.put("groupId", groupId);
                return this.mGroupManager.deleteMemberFromGroup(this.mCallerPackageName, REQ_ID, deleteParams.toString(), connParams.toString());
            } catch (JSONException e) {
                LogUtils.e(TAG, "pack parameters error");
                return 1;
            }
        }
    }

    public int setFriendsList(String groupName, List<String> friendsList) {
        LogUtils.i(TAG, "invoke setFriendsList");
        if (groupName == null || friendsList == null) {
            LogUtils.e(TAG, "invalid parameter when call setFriendsList");
            return ReturnCode.INVALID_PARAMETERS;
        } else if (!isHwDeviceGroupManagerGot()) {
            return 1;
        } else {
            if (this.mGroupManager.setFriendsList(getGroupId(groupName), friendsList) != 0) {
                return 1;
            }
            return 0;
        }
    }

    public List<String> getFriendsList(String groupName) {
        LogUtils.i(TAG, "invoke getFriendsList");
        if (groupName == null) {
            LogUtils.e(TAG, "invalid parameter when call getFriendsList");
            return null;
        } else if (!isHwDeviceGroupManagerGot()) {
            return null;
        } else {
            return this.mGroupManager.getFriendsList(getGroupId(groupName));
        }
    }

    private boolean checkUserInfoValidity(UserInfo userInfo, String targetPackageName) {
        if (userInfo == null) {
            LogUtils.e(TAG, "invalid userInfov when call checkUserInfoValidity");
            return false;
        }
        if (userInfo.getServiceType() == null) {
            if (targetPackageName == null) {
                userInfo.setServiceType(this.mCallerPackageName);
            } else {
                userInfo.setServiceType(targetPackageName);
            }
        }
        if (userInfo.getAuthId() != null && userInfo.getAuthId().length > 64) {
            LogUtils.e(TAG, "authId is too long when call checkUserInfoValidity");
            return false;
        } else if (UserType.validUserType(userInfo.getUserType())) {
            return true;
        } else {
            LogUtils.e(TAG, "invalid user type when call checkUserInfoValidity");
            return false;
        }
    }

    private boolean checkParamsValidity(OperationParameter opParams, String targetPackageName) {
        if (opParams == null || opParams.getCallbackHandler() == null || opParams.getSessionId() == null) {
            return false;
        }
        if (opParams.getServiceType() == null) {
            if (targetPackageName == null) {
                opParams.setServiceType(this.mCallerPackageName);
            } else {
                opParams.setServiceType(targetPackageName);
            }
        }
        if (opParams.getSelfId() != null && opParams.getSelfId().length > 64) {
            LogUtils.e(TAG, "self authId is too long when call checkParamsValidity");
            return false;
        } else if (opParams.getPeerId() == null || opParams.getPeerId().length <= 64) {
            int peerType = opParams.getPeerType();
            if (!UserType.validUserType(peerType) && peerType != -1) {
                LogUtils.e(TAG, "invalid peer's user type when call checkParamsValidity");
                return false;
            } else if (UserType.validUserType(opParams.getSelfType())) {
                return true;
            } else {
                LogUtils.e(TAG, "invalid self user type when call checkParamsValidity");
                return false;
            }
        } else {
            LogUtils.e(TAG, "peer's authId is too long when call checkParamsValidity");
            return false;
        }
    }

    private String getGroupId(String groupName) {
        if (this.mGroupManager == null) {
            return null;
        }
        JSONObject queryParams = new JSONObject();
        try {
            queryParams.put(HwDeviceGroupManager.PARAMETER_TAG_GROUP_NAME, groupName);
            queryParams.put("groupOwner", this.mCallerPackageName);
            List<String> groupInfoList = this.mGroupManager.getGroupInfo(queryParams.toString());
            if (groupInfoList == null || groupInfoList.size() == 0) {
                LogUtils.e(TAG, "get groupInfo failed");
                return null;
            }
            JSONObject groupInfo = parseJsonString(groupInfoList.get(0));
            if (groupInfo == null) {
                return null;
            }
            return groupInfo.optString("groupId");
        } catch (JSONException e) {
            LogUtils.e(TAG, "pack query parameters error");
            return null;
        }
    }

    private JSONObject parseJsonString(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) {
            LogUtils.e(TAG, "Invalid parameter : input JSON string is null");
            return null;
        }
        try {
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            LogUtils.e(TAG, "bad JSON string format of input " + jsonStr);
            return null;
        }
    }

    private synchronized boolean isHwDeviceGroupManagerGot() {
        this.mGroupManager = HwDeviceGroupManager.getInstance(this.mContext, this.mCallerPackageName, this.mGroupManageCallback);
        return this.mGroupManager != null;
    }

    private String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length == 0) {
            return BuildConfig.FLAVOR;
        }
        char[] hexArray = new char[(bytes.length + bytes.length)];
        for (int i = 0; i < bytes.length; i++) {
            int temp = bytes[i] & 255;
            char[] cArr = HEX_ARRAY;
            hexArray[i * 2] = cArr[temp >>> 4];
            hexArray[(i * 2) + 1] = cArr[temp & 15];
        }
        return new String(hexArray);
    }
}
