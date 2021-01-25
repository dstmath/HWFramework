package com.huawei.security.deviceauth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.security.deviceauth.IGroupAuthCallbacks;
import com.huawei.security.deviceauth.IGroupAuthService;
import com.huawei.security.deviceauth.ISignReqCallback;
import com.huawei.security.deviceauth.ITrustedDeviceChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class HichainAuthManager {
    public static final int ACROSS_ACCOUNT_AUTH = 2;
    public static final String AUTH_TAG_AUTH_FORM = "authForm";
    public static final String AUTH_TAG_CONFIRMATION = "confirmation";
    public static final String AUTH_TAG_DEVICE_ID = "deviceId";
    public static final String AUTH_TAG_GROUP_ID = "groupId";
    public static final String AUTH_TAG_IS_CLIENT = "isClient";
    public static final String AUTH_TAG_IS_DEVICE_LEVEL = "isDeviceLevel";
    public static final String AUTH_TAG_KEY_LENGTH = "keyLength";
    public static final String AUTH_TAG_PEER_AUTH_STATE = "peerAuthState";
    public static final String AUTH_TAG_PEER_CONN_ID = "peerConnDeviceId";
    public static final String AUTH_TAG_PEER_ID = "peerAuthId";
    public static final String AUTH_TAG_PEER_TYPE = "peerUserType";
    public static final String AUTH_TAG_PKG_NAME = "servicePkgName";
    public static final String AUTH_TAG_SELF_ID = "selfAuthId";
    public static final String AUTH_TAG_SELF_TYPE = "selfUserType";
    public static final String AUTH_TAG_SERVICE_TYPE = "serviceType";
    public static final String AUTH_TAG_SESSION_KEY = "sessionKey";
    public static final String AUTH_TAG_USER_ID = "userId";
    private static final String CONFIRM_REJECT = null;
    private static final int DATA_NOT_EXIST = -1;
    public static final int FAILED = -1;
    private static final String GAS_REGISTER_NAME = "group_authenticate_service";
    public static final int IDENTICAL_ACCOUNT_AUTH = 1;
    public static final int NON_ACCOUNT_AUTH = 0;
    public static final int REQUEST_ACCEPTED = -2147483642;
    public static final int REQUEST_REJECTED = -2147483643;
    private static final String SERVICE_NAME = "com.huawei.securityserver.HwDeviceGroupAuthService";
    private static final String SERVICE_PACKAGE_NAME = "com.huawei.securityserver";
    public static final int SUCCESS = 0;
    private static final String TAG = "HichainAuthManager";
    private static final int TRY_TIMES = 10;
    private static final int WAIT_SERVICE_START_TIME = 50;
    private static volatile HichainAuthManager sHichainAuthManager;
    private IBinder mBinderToken = new Binder();
    private IGroupAuthCallbacks mCallbackBinder;
    private String mCallerPackageName;
    private Context mContext;
    private IGroupAuthService mGroupAuthService;

    public interface HichainAuthCallback {
        void onError(long j, int i, int i2, String str);

        void onFinish(long j, int i, String str);

        String onRequest(long j, int i, String str);

        void onSessionKeyReturned(long j, byte[] bArr);

        boolean onTransmit(long j, byte[] bArr);
    }

    public interface HichainReqSignCallback {
        void onSignFinish(long j, int i, byte[] bArr);
    }

    public interface TrustedDeviceChangeListener {
        void onDeviceBound(String str, String str2);

        void onDeviceNotTrusted(String str);

        void onDeviceUnbound(String str, String str2);

        void onGroupCreated(String str);

        void onGroupDeleted(String str);

        void onLastGroupDeleted(String str, int i);

        void onTrustedDeviceNumChanged(int i);
    }

    private HichainAuthManager(Context context) {
        this.mCallerPackageName = context.getPackageName();
        this.mContext = context;
        connectGroupAuthService();
    }

    private HichainAuthManager(Context context, HichainAuthCallback callbacks) {
        this.mCallerPackageName = context.getPackageName();
        this.mCallbackBinder = getCallbackBinder(callbacks);
        this.mContext = context;
        connectGroupAuthService();
    }

    public static HichainAuthManager getInstance(Context context) {
        LogUtils.i(TAG, "get HichainAuthManager single instance");
        if (sHichainAuthManager == null) {
            synchronized (HichainAuthManager.class) {
                if (sHichainAuthManager == null) {
                    if (context == null) {
                        LogUtils.e(TAG, "Invalid parameters : context is null");
                        return sHichainAuthManager;
                    }
                    LogUtils.i(TAG, "HichainAuthManager single instance created by " + context.getPackageName());
                    sHichainAuthManager = new HichainAuthManager(context);
                }
            }
        }
        return sHichainAuthManager;
    }

    public static HichainAuthManager getInstance(Context context, HichainAuthCallback callbackHandler) {
        if (context == null) {
            LogUtils.e(TAG, "Invalid parameters when call getInstance: context is null");
            return null;
        }
        LogUtils.i(TAG, "get HichainAuthManager and register callbacks by " + context.getPackageName());
        if (callbackHandler != null) {
            return new HichainAuthManager(context, callbackHandler);
        }
        LogUtils.e(TAG, "Invalid parameters : callbackHandler is null");
        return null;
    }

    public int authDevice(HichainAuthCallback callbacks, long authReqId, String authParams) {
        LogUtils.i(TAG, "start authDevice with callback");
        int ret = -1;
        if (authParams == null || callbacks == null) {
            LogUtils.e(TAG, "Invalid parameters :authParams or callback is null");
            return -1;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.authDevice(this.mCallerPackageName, getCallbackBinder(callbacks), authReqId, authParams, this.mBinderToken);
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call authDevice");
            ret = ReturnCode.CALL_REMOTE_ERROR;
        }
        LogUtils.i(TAG, String.format(Locale.ENGLISH, "invoke authentication result  = 0x%08x", Integer.valueOf(ret)));
        return ret;
    }

    public int authDevice(long authReqId, String authParams) {
        LogUtils.i(TAG, "start authDevice");
        int ret = -1;
        if (this.mCallbackBinder == null) {
            LogUtils.e(TAG, "haven't register callbacks yet");
            return -1;
        } else if (authParams == null) {
            LogUtils.e(TAG, "Invalid parameters :authParams is null");
            return -1;
        } else {
            connectGroupAuthService();
            try {
                if (this.mGroupAuthService != null) {
                    ret = this.mGroupAuthService.authDevice(this.mCallerPackageName, this.mCallbackBinder, authReqId, authParams, this.mBinderToken);
                } else {
                    LogUtils.e(TAG, "cannot get HwGroupAuthService");
                }
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call authDevice");
                ret = ReturnCode.CALL_REMOTE_ERROR;
            }
            LogUtils.i(TAG, String.format(Locale.ENGLISH, "invoke authentication result  = 0x%08x", Integer.valueOf(ret)));
            return ret;
        }
    }

    public boolean processAuthData(HichainAuthCallback callbacks, long authReqId, byte[] data) {
        LogUtils.i(TAG, "start processAuthData with callback");
        boolean ret = false;
        if (callbacks == null) {
            LogUtils.e(TAG, "Invalid parameters : callback is null");
            return false;
        } else if (data == null) {
            LogUtils.e(TAG, "Invalid parameters : empty data");
            return false;
        } else {
            connectGroupAuthService();
            try {
                if (this.mGroupAuthService != null) {
                    ret = this.mGroupAuthService.processAuthData(this.mCallerPackageName, getCallbackBinder(callbacks), authReqId, data, this.mBinderToken);
                } else {
                    LogUtils.e(TAG, "cannot get HwGroupAuthService");
                }
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call processAuthData");
            }
            LogUtils.i(TAG, "processAuthData result = " + ret);
            return ret;
        }
    }

    public boolean processAuthData(long authReqId, byte[] data) {
        LogUtils.i(TAG, "start processAuthData");
        boolean ret = false;
        if (this.mCallbackBinder == null) {
            LogUtils.e(TAG, "haven't register callbacks yet");
            return false;
        } else if (data == null) {
            LogUtils.e(TAG, "Invalid parameters : empty data");
            return false;
        } else {
            connectGroupAuthService();
            try {
                if (this.mGroupAuthService != null) {
                    ret = this.mGroupAuthService.processAuthData(this.mCallerPackageName, this.mCallbackBinder, authReqId, data, this.mBinderToken);
                } else {
                    LogUtils.e(TAG, "cannot get HwGroupAuthService");
                }
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call processAuthData");
            }
            LogUtils.i(TAG, "processAuthData result = " + ret);
            return ret;
        }
    }

    public int cancelAuthRequest(long authReqId) {
        LogUtils.i(TAG, "start cancelAuthRequest");
        int ret = -1;
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.cancelAuthRequest(this.mCallerPackageName, authReqId, this.mBinderToken);
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call cancelAuthRequest");
            ret = ReturnCode.CALL_REMOTE_ERROR;
        }
        LogUtils.i(TAG, "cancelAuthRequest result = " + ret);
        return ret;
    }

    public int addDeviceChangeListener(TrustedDeviceChangeListener trustedDeviceChangeListener) {
        LogUtils.i(TAG, "start addDeviceChangeListener");
        int ret = -1;
        if (trustedDeviceChangeListener == null) {
            LogUtils.e(TAG, "invalid parameter : illegal listener");
            return -1;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.addDeviceChangeListener(this.mCallerPackageName, getListenerBinder(trustedDeviceChangeListener), this.mBinderToken);
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call addDeviceUnboundListener");
            ret = ReturnCode.CALL_REMOTE_ERROR;
        }
        LogUtils.i(TAG, String.format(Locale.ENGLISH, "addDeviceChangeListener result  = 0x%08x", Integer.valueOf(ret)));
        return ret;
    }

    public int revokeDeviceChangeListener() {
        LogUtils.i(TAG, "start revokeDeviceChangeListener");
        int ret = -1;
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.revokeDeviceChangeListener(this.mCallerPackageName, this.mBinderToken);
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call revokeDeviceUnboundListener");
            ret = ReturnCode.CALL_REMOTE_ERROR;
        }
        LogUtils.i(TAG, String.format(Locale.ENGLISH, "revokeDeviceChangeListener result  = 0x%08x", Integer.valueOf(ret)));
        return ret;
    }

    public String getDeviceIdHash(String deviceId, int hashLen) {
        LogUtils.i(TAG, "start getDeviceIdHash");
        String deviceHash = null;
        if (deviceId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal deviceId");
            return null;
        } else if (hashLen <= 0) {
            LogUtils.e(TAG, "invalid parameter : illegal hash length");
            return null;
        } else {
            connectGroupAuthService();
            try {
                if (this.mGroupAuthService != null) {
                    deviceHash = this.mGroupAuthService.getDeviceIdHash(deviceId, hashLen);
                } else {
                    LogUtils.e(TAG, "cannot get HwGroupAuthService");
                }
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call getDeviceIdHash");
            }
            LogUtils.i(TAG, "getDeviceIdHash = " + deviceHash);
            return deviceHash;
        }
    }

    public boolean isPotentialTrustedDevice(int idType, String deviceId, boolean isPrecise) {
        LogUtils.i(TAG, "start isPotentialTrustedDevice");
        boolean ret = false;
        if (deviceId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal deviceId");
            return false;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.isPotentialTrustedDevice(idType, deviceId, isPrecise);
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call isPotentialTrustedDevice");
        }
        LogUtils.i(TAG, "isPotentialTrustedDevice = " + ret);
        return ret;
    }

    public int queryTrustedDeviceNum() {
        LogUtils.i(TAG, "start queryTrustedDeviceNum");
        int ret = 0;
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.queryTrustedDeviceNum();
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call queryTrustedDeviceNum");
        }
        LogUtils.i(TAG, "trusted device number = " + ret);
        return ret;
    }

    public String getAuthState(long reqId, String groupId, String peerConnDeviceId) {
        LogUtils.i(TAG, "start getAuthState of request " + reqId);
        if (groupId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal groupId");
            return null;
        } else if (peerConnDeviceId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal peerDeviceId");
            return null;
        } else {
            connectGroupAuthService();
            try {
                if (this.mGroupAuthService != null) {
                    return this.mGroupAuthService.getAuthState(this.mCallerPackageName, reqId, groupId, peerConnDeviceId, this.mBinderToken);
                }
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
                return null;
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call getAuthState");
                return null;
            }
        }
    }

    public void informDeviceDisconnection(String connDeviceId) {
        LogUtils.i(TAG, "start informDeviceDisconnection");
        if (connDeviceId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal deviceId");
            return;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                this.mGroupAuthService.informDeviceDisconnection(this.mCallerPackageName, connDeviceId);
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call informDeviceDisconnection");
        }
    }

    public int startGroupManageService() {
        LogUtils.i(TAG, "start to bind HwDeviceGroupManage service");
        int ret = -1;
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                ret = this.mGroupAuthService.startGroupManageService();
            } else {
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when bind HwDeviceGroupManage service");
            ret = ReturnCode.CALL_REMOTE_ERROR;
        }
        LogUtils.i(TAG, "start HwDeviceGroupManage service result = " + ret);
        return ret;
    }

    public int requestSignature(HichainReqSignCallback callback, long signReqId, byte[] signReqParams) {
        LogUtils.i(TAG, "start requestSignature");
        if (callback == null || signReqParams == null) {
            LogUtils.e(TAG, "Invalid parameters: callback or signReqParams is null");
            return -1;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                return this.mGroupAuthService.requestSignature(getSignReqCallbackBinder(callback), signReqId, signReqParams);
            }
            LogUtils.e(TAG, "cannot get HwGroupAuthService");
            return -1;
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call requestSignature");
            return ReturnCode.CALL_REMOTE_ERROR;
        }
    }

    public List<String> getGroupInfo(String queryParams) {
        LogUtils.i(TAG, "invoke getGroupInfo");
        List<String> groupInfoList = new ArrayList<>(0);
        if (queryParams == null) {
            LogUtils.e(TAG, "invalid parameter : illegal queryParams");
            return groupInfoList;
        }
        try {
            JSONObject json = new JSONObject(queryParams);
            json.put("callerPkgName", this.mCallerPackageName);
            String inputParams = json.toString();
            connectGroupAuthService();
            try {
                if (this.mGroupAuthService != null) {
                    return this.mGroupAuthService.getGroupInfo(inputParams);
                }
                LogUtils.e(TAG, "cannot get HwGroupAuthService");
                return groupInfoList;
            } catch (RemoteException e) {
                LogUtils.e(TAG, "remoteException occurs when call getGroupInfo");
                return groupInfoList;
            }
        } catch (JSONException e2) {
            LogUtils.e(TAG, "jsonException occurs when call getGroupInfo");
            return groupInfoList;
        }
    }

    public List<String> getRelatedGroupInfo(String connDeviceId) {
        LogUtils.i(TAG, "invoke getRelatedGroupInfo");
        List<String> groupInfoList = new ArrayList<>(0);
        if (connDeviceId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal deviceId");
            return groupInfoList;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                return this.mGroupAuthService.getRelatedGroupInfo(connDeviceId);
            }
            LogUtils.e(TAG, "cannot get HwGroupAuthService");
            return groupInfoList;
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call getRelatedGroupInfo");
            return groupInfoList;
        }
    }

    public int checkAccessToGroup(String groupId, String pkgName) {
        LogUtils.i(TAG, "invoke checkAccessToGroup");
        if (groupId == null || pkgName == null) {
            LogUtils.e(TAG, "invalid parameter : illegal groupId or pkgName");
            return -1;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                return this.mGroupAuthService.checkAccessToGroup(groupId, pkgName);
            }
            LogUtils.e(TAG, "cannot get HwGroupAuthService");
            return -1;
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call checkAccessToGroup");
            return -1;
        }
    }

    public int checkAccessToDevice(String connDeviceId, String pkgName) {
        LogUtils.i(TAG, "invoke checkAccessToDevice");
        if (connDeviceId == null || pkgName == null) {
            LogUtils.e(TAG, "invalid parameter : illegal deviceId or pkgName");
            return -1;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                return this.mGroupAuthService.checkAccessToDevice(connDeviceId, pkgName);
            }
            LogUtils.e(TAG, "cannot get HwGroupAuthService");
            return -1;
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call checkAccessToDevice");
            return -1;
        }
    }

    public boolean isIdenticalAccountDevice(String connDeviceId) {
        LogUtils.i(TAG, "invoke isIdenticalAccountDevice");
        if (connDeviceId == null) {
            LogUtils.e(TAG, "invalid parameter : illegal deviceId");
            return false;
        }
        connectGroupAuthService();
        try {
            if (this.mGroupAuthService != null) {
                return this.mGroupAuthService.isIdenticalAccountDevice(connDeviceId);
            }
            LogUtils.e(TAG, "cannot get HwGroupAuthService");
            return false;
        } catch (RemoteException e) {
            LogUtils.e(TAG, "remoteException occurs when call isIdenticalAccountDevice");
            return false;
        }
    }

    private void connectGroupAuthService() {
        try {
            if (this.mGroupAuthService == null) {
                LogUtils.i(TAG, "get HwGroupAuthService");
                IBinder gasBinder = ServiceManagerEx.getService(GAS_REGISTER_NAME);
                if (gasBinder == null) {
                    LogUtils.e(TAG, "get GroupAuthService binder from service manager failed");
                    if (this.mContext != null) {
                        LogUtils.i(TAG, "try to start GroupAuthService again");
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(SERVICE_PACKAGE_NAME, SERVICE_NAME));
                        this.mContext.startService(intent);
                        gasBinder = ServiceManagerEx.getService(GAS_REGISTER_NAME);
                    }
                }
                int cnt = 0;
                while (gasBinder == null && cnt < 10) {
                    Thread.sleep(50);
                    gasBinder = ServiceManagerEx.getService(GAS_REGISTER_NAME);
                    cnt++;
                }
                if (gasBinder == null) {
                    LogUtils.e(TAG, "restart GroupAuthService failed");
                } else {
                    binderToServiceInterface(gasBinder);
                }
            }
        } catch (InterruptedException e) {
            LogUtils.e(TAG, "acquire lock to access to HwGroupAuthService error");
        } catch (SecurityException e2) {
            LogUtils.e(TAG, "has no permission to start HwGroupAuthService");
        }
    }

    private void binderToServiceInterface(IBinder gasBinder) {
        this.mGroupAuthService = IGroupAuthService.Stub.asInterface(gasBinder);
        try {
            gasBinder.linkToDeath(new IBinder.DeathRecipient() {
                /* class com.huawei.security.deviceauth.HichainAuthManager.AnonymousClass1 */

                @Override // android.os.IBinder.DeathRecipient
                public void binderDied() {
                    LogUtils.e(HichainAuthManager.TAG, "HwGroupAuthService binderDied");
                    HichainAuthManager.this.mGroupAuthService = null;
                }
            }, 0);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "RemoteException when call binder linkToDeath");
        }
        if (this.mGroupAuthService == null) {
            LogUtils.e(TAG, "connectGroupAuthService service failed");
        } else {
            LogUtils.i(TAG, "get HwGroupAuthService ok");
        }
    }

    private ITrustedDeviceChangeListener getListenerBinder(final TrustedDeviceChangeListener listener) {
        return new ITrustedDeviceChangeListener.Stub() {
            /* class com.huawei.security.deviceauth.HichainAuthManager.AnonymousClass2 */

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onDeviceNotTrusted(String deviceId) throws RemoteException {
                HichainAuthManager.this.deviceNotTrusted(listener, deviceId);
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onDeviceUnbound(String deviceId, String groupInfo) throws RemoteException {
                HichainAuthManager.this.deviceUnbound(listener, deviceId, groupInfo);
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onDeviceBound(String connDeviceId, String groupInfo) throws RemoteException {
                HichainAuthManager.this.deviceBound(listener, connDeviceId, groupInfo);
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onGroupCreated(String groupInfo) throws RemoteException {
                HichainAuthManager.this.groupCreated(listener, groupInfo);
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onGroupDeleted(String groupInfo) throws RemoteException {
                HichainAuthManager.this.groupDeleted(listener, groupInfo);
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onTrustedDeviceNumChanged(int deviceNum) throws RemoteException {
                HichainAuthManager.this.trustedDeviceNumChanged(listener, deviceNum);
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onLastGroupDeleted(String deviceId, int groupType) throws RemoteException {
                HichainAuthManager.this.lastGroupDeleted(listener, deviceId, groupType);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deviceNotTrusted(TrustedDeviceChangeListener listener, String deviceId) {
        LogUtils.i(TAG, "call onDeviceNotTrusted");
        try {
            listener.onDeviceNotTrusted(deviceId);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform of device not trusted error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform of device not trusted error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deviceUnbound(TrustedDeviceChangeListener listener, String deviceId, String groupInfo) {
        LogUtils.i(TAG, "call onDeviceUnbound");
        try {
            listener.onDeviceUnbound(deviceId, groupInfo);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform of device unbound error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform of device unbound error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void deviceBound(TrustedDeviceChangeListener listener, String connDeviceId, String groupInfo) {
        LogUtils.i(TAG, "call onDeviceBound");
        try {
            listener.onDeviceBound(connDeviceId, groupInfo);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform of device bound error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform of device bound error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void groupCreated(TrustedDeviceChangeListener listener, String groupInfo) {
        LogUtils.i(TAG, "call onGroupCreated");
        try {
            listener.onGroupCreated(groupInfo);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform of group created error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform of group created error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void groupDeleted(TrustedDeviceChangeListener listener, String groupInfo) {
        LogUtils.i(TAG, "call onGroupDeleted");
        try {
            listener.onGroupDeleted(groupInfo);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform of group deleted error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform of group deleted error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void trustedDeviceNumChanged(TrustedDeviceChangeListener listener, int deviceNum) {
        LogUtils.i(TAG, "call onTrustedDeviceNumChanged");
        try {
            listener.onTrustedDeviceNumChanged(deviceNum);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform of device number change error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform of device number change error");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lastGroupDeleted(TrustedDeviceChangeListener listener, String deviceId, int groupType) {
        LogUtils.i(TAG, "call onLastGroupDeleted");
        try {
            listener.onLastGroupDeleted(deviceId, groupType);
        } catch (Exception e) {
            LogUtils.e(TAG, "inform deletion of the last group of certain type error : UnknownException");
        } catch (AbstractMethodError e2) {
            LogUtils.e(TAG, "inform deletion of the last group of certain type error");
        }
    }

    private IGroupAuthCallbacks getCallbackBinder(HichainAuthCallback callbacks) {
        if (callbacks != null) {
            return new HichainAuthCallbackBinder(callbacks);
        }
        return null;
    }

    private ISignReqCallback getSignReqCallbackBinder(final HichainReqSignCallback callback) {
        LogUtils.i(TAG, "call getSignReqCallbackBinder");
        if (callback != null) {
            return new ISignReqCallback.Stub() {
                /* class com.huawei.security.deviceauth.HichainAuthManager.AnonymousClass3 */

                @Override // com.huawei.security.deviceauth.ISignReqCallback
                public void onSignFinish(long signReqId, int authForm, byte[] signReturn) throws RemoteException {
                    callback.onSignFinish(signReqId, authForm, signReturn);
                }
            };
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class HichainAuthCallbackBinder extends IGroupAuthCallbacks.Stub {
        private HichainAuthCallback mInputCallback;

        HichainAuthCallbackBinder(HichainAuthCallback callback) {
            this.mInputCallback = callback;
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
        public void onFinish(long reqId, int authForm, String authReturn) throws RemoteException {
            LogUtils.i(HichainAuthManager.TAG, "call onFinish of request " + reqId);
            HichainAuthCallback hichainAuthCallback = this.mInputCallback;
            if (hichainAuthCallback == null) {
                LogUtils.e(HichainAuthManager.TAG, "callback is empty");
                return;
            }
            try {
                hichainAuthCallback.onFinish(reqId, authForm, authReturn);
            } catch (Exception e) {
                LogUtils.e(HichainAuthManager.TAG, "return authentication result error : UnknownException");
            }
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
        public void onError(long reqId, int authForm, int errorCode, String errorReturn) throws RemoteException {
            LogUtils.i(HichainAuthManager.TAG, "call onError of request " + reqId);
            HichainAuthCallback hichainAuthCallback = this.mInputCallback;
            if (hichainAuthCallback == null) {
                LogUtils.e(HichainAuthManager.TAG, "callback is empty");
                return;
            }
            try {
                hichainAuthCallback.onError(reqId, authForm, errorCode, errorReturn);
            } catch (Exception e) {
                LogUtils.e(HichainAuthManager.TAG, "return authentication exception error : UnknownException");
            }
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
        public boolean onTransmit(long reqId, byte[] data) throws RemoteException {
            LogUtils.i(HichainAuthManager.TAG, "call onTransmit of request " + reqId);
            HichainAuthCallback hichainAuthCallback = this.mInputCallback;
            if (hichainAuthCallback == null) {
                LogUtils.e(HichainAuthManager.TAG, "callback is empty");
                return false;
            }
            try {
                return hichainAuthCallback.onTransmit(reqId, data);
            } catch (Exception e) {
                LogUtils.e(HichainAuthManager.TAG, "send message error : UnknownException");
                return false;
            }
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
        public String onRequest(long reqId, int authForm, String reqParams) throws RemoteException {
            LogUtils.i(HichainAuthManager.TAG, "call onRequest of request " + reqId);
            String confirmation = HichainAuthManager.CONFIRM_REJECT;
            HichainAuthCallback hichainAuthCallback = this.mInputCallback;
            if (hichainAuthCallback == null) {
                LogUtils.e(HichainAuthManager.TAG, "callback is empty");
                return HichainAuthManager.CONFIRM_REJECT;
            }
            try {
                return hichainAuthCallback.onRequest(reqId, authForm, reqParams);
            } catch (Exception e) {
                LogUtils.e(HichainAuthManager.TAG, "get confirmation error : UnknownException");
                return confirmation;
            }
        }

        @Override // com.huawei.security.deviceauth.IGroupAuthCallbacks
        public void onSessionKeyReturned(long reqId, byte[] sessionKey) throws RemoteException {
            LogUtils.i(HichainAuthManager.TAG, "call onSessionKeyReturned of request " + reqId);
            HichainAuthCallback hichainAuthCallback = this.mInputCallback;
            if (hichainAuthCallback == null) {
                LogUtils.e(HichainAuthManager.TAG, "callback is empty");
                return;
            }
            try {
                hichainAuthCallback.onSessionKeyReturned(reqId, sessionKey);
            } catch (AbstractMethodError e) {
                LogUtils.w(HichainAuthManager.TAG, "no onSessionKeyReturned method, pass");
            } catch (Exception e2) {
                LogUtils.e(HichainAuthManager.TAG, "return session key error : UnknownException");
            }
        }
    }
}
