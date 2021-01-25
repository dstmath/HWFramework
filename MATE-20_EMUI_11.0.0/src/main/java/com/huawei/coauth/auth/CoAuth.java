package com.huawei.coauth.auth;

import android.content.Context;
import android.util.Log;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgEncodeMgr;
import com.huawei.coauth.msg.CoMessengerClient;
import com.huawei.coauthservice.identitymgr.HwIdentityManager;
import com.huawei.coauthservice.identitymgr.IdmCreateGroupInfo;
import com.huawei.coauthservice.identitymgr.IdmDeleteGroupInfo;
import com.huawei.coauthservice.identitymgr.IdmDeviceInfo;
import com.huawei.coauthservice.identitymgr.IdmGroupInfo;
import com.huawei.coauthservice.identitymgr.IdmLinkType;
import com.huawei.coauthservice.identitymgr.IdmUserType;
import com.huawei.coauthservice.identitymgr.callback.ICreateGroupCallback;
import com.huawei.coauthservice.identitymgr.callback.IDeleteGroupCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CoAuth {
    private static final int AP = 0;
    private static final int DEV_LIST_NUMS = 1;
    private static final int P2P = 1;
    private static final String TAG = CoAuth.class.getName();
    private static Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap = new ConcurrentHashMap();
    private static CoAuth instance;
    private static CoMessengerClient messengerClient;
    private Optional<String> consumerPackageName;
    private HwIdentityManager identityManager;

    /* access modifiers changed from: protected */
    public interface ICancelCoAuthCallback {
        void onCancelCoAuthFinish(int i);
    }

    public interface ICoAuthCallback {
        void onCoAuthFinish(int i, CoAuthContext coAuthContext);

        void onCoAuthStart(CoAuthContext coAuthContext);
    }

    public interface IConnectServiceCallback {
        void onConnectFailed();

        void onConnected();

        void onDisconnect();
    }

    public interface ICreateCallback {
        void onFailed(int i);

        void onSuccess(CoAuthGroup coAuthGroup);
    }

    public interface IDestroyCallback {
        void onFailed(int i);

        void onSuccess();
    }

    public interface IGetPropCallback {
        void onGetResult(int i, byte[] bArr);
    }

    public interface IInitCallback {
        void onResult(int i, List<CoAuthDevice> list);
    }

    public interface IQueryAuthCallback {
        void onResult(List<CoAuthContext> list);
    }

    public interface ISetPropCallback {
        void onGetResult(int i);
    }

    private CoAuth() {
    }

    public static synchronized CoAuth getInstance() {
        CoAuth coAuth;
        synchronized (CoAuth.class) {
            if (instance == null) {
                instance = new CoAuth();
            }
            coAuth = instance;
        }
        return coAuth;
    }

    public void connectService(Context context, IConnectServiceCallback callback) {
        Log.i(TAG, "receive client's connectService request");
        if (callback == null) {
            Log.e(TAG, "connectService request failed, empty callback");
        } else if (!CoAuthCheckUtil.connectServiceCheck(context)) {
            callback.onConnectFailed();
        } else {
            this.consumerPackageName = Optional.ofNullable(context.getApplicationContext().getPackageName());
            this.identityManager = HwIdentityManager.getInstance(context, IdmUserType.SAME_USER_ID);
            HwIdentityManager hwIdentityManager = this.identityManager;
            if (hwIdentityManager == null) {
                Log.e(TAG, "identityMgrServiceSdk is null");
                callback.onConnectFailed();
                return;
            }
            coAuthServiceConnection(context, callback, hwIdentityManager);
        }
    }

    private static void coAuthServiceConnection(Context context, IConnectServiceCallback callback, HwIdentityManager identityManager2) {
        messengerClient = CoMessengerClient.getInstance();
        messengerClient.connectServer(context, new ConnectCoAuthServiceCallback(callback, identityManager2));
    }

    public void disconnectService(Context context) {
        Log.i(TAG, "receive client's disconnectService request");
        if (CoAuthCheckUtil.disconnectServiceCheck(context)) {
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient != null) {
                coMessengerClient.disConnectServer(context);
            }
            Log.i(TAG, "disconnectService from server");
        }
    }

    public void createCoAuthPairGroup(String moduleName, CoAuthDevice peerDevice, ICreateCallback callback) {
        Log.i(TAG, "receive client's createCoAuthPairGroup request");
        if (callback == null) {
            Log.e(TAG, "createCoAuthPairGroup request failed, empty callback");
        } else if (!CoAuthCheckUtil.createCoAuthPairGroupCheck(moduleName, peerDevice)) {
            callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
        } else {
            IdmDeviceInfo idmDeviceInfo = new IdmDeviceInfo();
            idmDeviceInfo.setDeviceId(peerDevice.getDeviceId());
            idmDeviceInfo.setIp(peerDevice.getIp());
            if (peerDevice.getPeerLinkType() == 0) {
                idmDeviceInfo.setIdmLinkType(IdmLinkType.AP);
            } else if (peerDevice.getPeerLinkType() == 1) {
                idmDeviceInfo.setIdmLinkType(IdmLinkType.P2P);
            } else {
                Log.e(TAG, "The value of peerLinkType is invalid.");
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                return;
            }
            List<IdmDeviceInfo> peerDeviceInfoList = new ArrayList<>(1);
            peerDeviceInfoList.add(idmDeviceInfo);
            IdmCreateGroupInfo idmCreateGroupInfo = new IdmCreateGroupInfo();
            idmCreateGroupInfo.setModuleName(moduleName);
            idmCreateGroupInfo.setOverwrite(true);
            idmCreateGroupInfo.setPeerDeviceInfoList(peerDeviceInfoList);
            createIdmGroup(callback, idmCreateGroupInfo, peerDevice);
        }
    }

    private void createIdmGroup(final ICreateCallback callback, IdmCreateGroupInfo groupInfo, final CoAuthDevice peerDevice) {
        HwIdentityManager hwIdentityManager = this.identityManager;
        if (hwIdentityManager == null) {
            Log.e(TAG, "identityMgrServiceSdk is null");
            callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
            return;
        }
        int result = hwIdentityManager.createGroup(groupInfo, new ICreateGroupCallback() {
            /* class com.huawei.coauth.auth.CoAuth.AnonymousClass1 */

            @Override // com.huawei.coauthservice.identitymgr.callback.ICreateGroupCallback
            public void onSuccess(IdmGroupInfo idmGroupInfo) {
                Log.i(CoAuth.TAG, "createGroupCallback onSuccess");
                if (Objects.nonNull(idmGroupInfo)) {
                    Log.i(CoAuth.TAG, "create group return success");
                    List<CoAuthDevice> devList = new ArrayList<>(1);
                    devList.add(peerDevice);
                    String idmGid = idmGroupInfo.getGroupId();
                    CoAuth.this.initCoAuthIdmGroup(idmGid, devList, CoAuth.this.getCoAuthInitCallback(callback, new CoAuthGroup(CoAuthUtil.hexStringToBytes(idmGid).get())));
                    return;
                }
                Log.e(CoAuth.TAG, "create group return error data");
            }

            @Override // com.huawei.coauthservice.identitymgr.callback.ICreateGroupCallback
            public void onFailed(int reason) {
                String str = CoAuth.TAG;
                Log.e(str, "createGroupCallback onFailed:" + reason);
            }
        });
        if (result != 0) {
            String str = TAG;
            Log.e(str, "create group return failed, code:" + result);
            callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
            return;
        }
        Log.d(TAG, "create group finish");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private IInitCallback getCoAuthInitCallback(final ICreateCallback createCallback, final CoAuthGroup coAuthGroup) {
        return new IInitCallback() {
            /* class com.huawei.coauth.auth.CoAuth.AnonymousClass2 */

            @Override // com.huawei.coauth.auth.CoAuth.IInitCallback
            public void onResult(int resultCode, List<CoAuthDevice> list) {
                if (resultCode == 0) {
                    Log.d(CoAuth.TAG, "CoAuth.IInitCallback onSuccess");
                    createCallback.onSuccess(coAuthGroup);
                    return;
                }
                String str = CoAuth.TAG;
                Log.e(str, "CoAuth.IInitCallback onFailed:" + resultCode);
                createCallback.onFailed(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue());
            }
        };
    }

    public void destroyCoAuthPairGroup(CoAuthGroup coAuthGroup, IDestroyCallback callback) {
        Log.i(TAG, "receive client's destroyCoAuthPairGroup request");
        if (callback == null) {
            Log.e(TAG, "destroyCoAuthPairGroup request failed, empty callback");
        } else if (!CoAuthCheckUtil.destroyCoAuthPairGroupCheck(coAuthGroup)) {
            callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
        } else {
            deleteIdmGroup(coAuthGroup.getGroupId());
            long sessionId = CoAuthUtil.getNewSessionId();
            String str = CoAuthUtil.TAG;
            Log.i(str, "receive client's destroyCoAuthPairGroup request groupId string = " + coAuthGroup.getGroupId());
            CoAuthPairGroupEntity coAuthPairGroupEntity = coAuthPairGroupMap.get(coAuthGroup.getGroupId());
            if (coAuthPairGroupEntity == null) {
                callback.onSuccess();
                String str2 = TAG;
                Log.i(str2, "return destroyCoAuthPairGroup success result to client = " + CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                return;
            }
            byte[] destroyGroupMsg = new CoAuthMsgEncodeMgr().destroyCoAuthPairGroupMsg(sessionId, CoAuthUtil.hexStringToBytes(coAuthGroup.getGroupId()).get(), coAuthPairGroupEntity);
            if (destroyGroupMsg.length == 0) {
                callback.onSuccess();
                String str3 = TAG;
                Log.i(str3, "return destroyCoAuthPairGroup success result to client = " + CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                Log.e(TAG, "destroyCoAuthPairGroup request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(destroyGroupMsg, new DestroyCallback(callback, coAuthPairGroupMap));
            Log.i(TAG, "send destroyCoAuthPairGroup message to server");
            if (ret != 0) {
                String str4 = TAG;
                Log.e(str4, "sendMsgToServer error, callback with response message, DestroyCallback.onFailed = " + ret);
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue());
            }
        }
    }

    private void deleteIdmGroup(String groupId) {
        Log.i(TAG, "start delete group");
        if (this.identityManager == null) {
            Log.e(TAG, "identityMgrServiceSdk is null");
            return;
        }
        IdmDeleteGroupInfo deleteGroupInfo = new IdmDeleteGroupInfo();
        deleteGroupInfo.setGroupId(groupId);
        int result = this.identityManager.deleteGroup(deleteGroupInfo, new IDeleteGroupCallback() {
            /* class com.huawei.coauth.auth.CoAuth.AnonymousClass3 */

            @Override // com.huawei.coauthservice.identitymgr.callback.IDeleteGroupCallback
            public void onSuccess(int result) {
                String str = CoAuth.TAG;
                Log.i(str, "deleteGroupCallback onSuccess: " + result);
            }

            @Override // com.huawei.coauthservice.identitymgr.callback.IDeleteGroupCallback
            public void onFailed(int reason) {
                String str = CoAuth.TAG;
                Log.e(str, "deleteGroupCallback onFailed: " + reason);
            }
        });
        if (result != 0) {
            String str = TAG;
            Log.e(str, "delete group return failed, code:" + result);
        }
        Log.d(TAG, "delete group finish");
    }

    public void queryCoAuthMethod(CoAuthContext context, IQueryAuthCallback callback) {
        Log.i(TAG, "receive client's queryCoAuthMethodCheck request");
        if (callback == null) {
            Log.e(TAG, "queryCoAuthMethodCheck request failed, empty callback");
        } else if (!CoAuthCheckUtil.queryCoAuthMethodCheck(context)) {
            callback.onResult(new ArrayList(0));
        } else {
            byte[] queryCoAuthMsg = new CoAuthMsgEncodeMgr().queryCoAuthMethodMsg(CoAuthUtil.getNewSessionId(), context);
            if (queryCoAuthMsg.length == 0) {
                callback.onResult(new ArrayList(0));
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onResult(new ArrayList(0));
                Log.e(TAG, "queryCoAuthMethod request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(queryCoAuthMsg, new QueryMethodCallback(callback));
            Log.i(TAG, "send queryCoAuthMethod message to server");
            if (ret != 0) {
                String str = TAG;
                Log.e(str, "sendMsgToServer error, callback with response message, QueryMethodCallback = " + ret);
                callback.onResult(new ArrayList(0));
            }
        }
    }

    public void getExecutorProp(CoAuthContext coAuthContext, byte[] key, IGetPropCallback callback) {
        Log.i(TAG, "receive client's getExecutorProp request");
        if (callback == null) {
            Log.e(TAG, "getPropertyCheck request failed, empty callback");
        } else if (!CoAuthCheckUtil.getPropertyCheck(coAuthContext, key)) {
            callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new byte[0]);
        } else {
            byte[] getPropertyMsg = new CoAuthMsgEncodeMgr().getPropertyMsg(CoAuthUtil.getNewSessionId(), key, coAuthContext);
            if (getPropertyMsg.length == 0) {
                callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new byte[0]);
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new byte[0]);
                Log.e(TAG, "getExecutorProp request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(getPropertyMsg, new GetPropertyCallback(callback));
            Log.i(TAG, "send getExecutorProp message to server");
            if (ret != 0) {
                String str = TAG;
                Log.e(str, "sendMsgToServer error, callback with response message, GetPropertyCallback = " + ret);
                callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new byte[0]);
            }
        }
    }

    public void setExecutorProp(CoAuthContext coAuthContext, byte[] key, byte[] value, ISetPropCallback callback) {
        Log.i(TAG, "receive client's setExecutorProp request");
        if (callback == null) {
            Log.e(TAG, "setPropertyCheck request failed, empty callback");
        } else if (!CoAuthCheckUtil.setPropertyCheck(coAuthContext, key, value)) {
            callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
        } else {
            byte[] setPropertyMsg = new CoAuthMsgEncodeMgr().setPropertyMsg(CoAuthUtil.getNewSessionId(), key, value, coAuthContext);
            if (setPropertyMsg.length == 0) {
                callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                Log.e(TAG, "setExecutorProp request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(setPropertyMsg, new SetPropertyCallback(callback));
            Log.i(TAG, "send setExecutorProp message to server");
            if (ret != 0) {
                String str = TAG;
                Log.e(str, "sendMsgToServer error, callback with response message, QueryMethodCallback = " + ret);
                callback.onGetResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
            }
        }
    }

    public void coAuth(CoAuthContext coAuthContext, ICoAuthCallback callback) {
        Log.i(TAG, "receive client's coAuth request");
        if (callback == null) {
            Log.e(TAG, "coAuth request failed, empty callback");
        } else if (!CoAuthCheckUtil.coAuthCheck(coAuthContext)) {
            callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), coAuthContext);
        } else {
            coAuthContext.setCoAuthBegin(true);
            long sessionId = coAuthContext.getSessionId();
            if (!this.consumerPackageName.isPresent()) {
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue(), coAuthContext);
                Log.e(TAG, "coAuth request failed, empty consumer packageName");
            }
            String str = TAG;
            Log.i(str, "coAuth request consumer packageName = " + this.consumerPackageName.get());
            byte[] coAuthMsg = new CoAuthMsgEncodeMgr().coAuthMsg(sessionId, coAuthContext, this.consumerPackageName.get());
            if (coAuthMsg.length == 0) {
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), coAuthContext);
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), coAuthContext);
                Log.e(TAG, "coAuth request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(coAuthMsg, new CoAuthCallback(coAuthContext, callback));
            Log.i(TAG, "send coAuth message to server");
            if (ret != 0) {
                String str2 = TAG;
                Log.e(str2, "sendMsgToServer error, return coAuth result to client = " + ret);
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue(), coAuthContext);
            }
        }
    }

    public void initCoAuthIdmGroup(String idmGid, List<CoAuthDevice> devList, IInitCallback callback) {
        Log.i(TAG, "receive client's initCoAuthIdmGroup request");
        if (callback == null) {
            Log.e(TAG, "initCoAuthIdmGroup request failed, empty callback");
        } else if (!CoAuthCheckUtil.initCoAuthIdmGroupCheck(idmGid, devList)) {
            callback.onResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new ArrayList(0));
        } else {
            byte[] initCoAuthIdmGroupMsg = new CoAuthMsgEncodeMgr().initCoAuthIdmGroupMsg(CoAuthUtil.getNewSessionId(), idmGid, devList);
            if (initCoAuthIdmGroupMsg.length == 0) {
                callback.onResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new ArrayList(0));
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onResult(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), new ArrayList(0));
                Log.e(TAG, "initCoAuthIdmGroup request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(initCoAuthIdmGroupMsg, new InitCoAuthIdmGroupCallback(callback));
            Log.i(TAG, "send initCoAuthIdmGroup message to server");
            if (ret != 0) {
                String str = TAG;
                Log.e(str, "sendMsgToServer error, return initCoAuthIdmGroup result to client = " + ret);
                callback.onResult(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue(), new ArrayList(0));
            }
        }
    }

    public int cancelCoAuth(CoAuthContext coAuthContext) {
        Log.i(TAG, "receive client's cancelCoAuth request");
        if (!CoAuthCheckUtil.cancelCoAuthCheck(coAuthContext)) {
            return CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue();
        }
        if (!coAuthContext.isCoAuthBegin()) {
            Log.i(TAG, "cancelCoAuth request success, coAuth not begin");
            return CoAuthRetCode.CO_AUTH_RET_SUCCESS.getValue();
        }
        long sessionId = coAuthContext.getSessionId();
        CoAuthPairGroupEntity coAuthPairGroupEntity = coAuthPairGroupMap.get(coAuthContext.getCoAuthGroup().getGroupId());
        if (coAuthPairGroupEntity == null) {
            String str = TAG;
            Log.i(str, "return cancelCoAuth success result to client = " + CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
            return CoAuthRetCode.CO_AUTH_RET_SUCCESS.getValue();
        }
        byte[] cancelCoAuthMsg = new CoAuthMsgEncodeMgr().cancelCoAuth(sessionId, coAuthContext, coAuthPairGroupEntity);
        if (cancelCoAuthMsg.length == 0) {
            String str2 = TAG;
            Log.i(str2, "return cancelCoAuth success result to client = " + CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
            return CoAuthRetCode.CO_AUTH_RET_SUCCESS.getValue();
        }
        CoMessengerClient coMessengerClient = messengerClient;
        if (coMessengerClient == null) {
            Log.i(TAG, "cancelCoAuth request error, empty messengerClient, return cancelCoAuth success result to client");
            return CoAuthRetCode.CO_AUTH_RET_SUCCESS.getValue();
        }
        int ret = coMessengerClient.sendMsgToServer(cancelCoAuthMsg, new CancelCoAuthCallback(coAuthContext, new CancelCoAuthCallbackProcesser()));
        Log.i(TAG, "send cancelCoAuth message to server");
        if (ret != 0) {
            String str3 = TAG;
            Log.i(str3, "sendMsgToServer error, return cancelCoAuth success result to client = " + ret);
        }
        return CoAuthRetCode.CO_AUTH_RET_SUCCESS.getValue();
    }

    private static class CancelCoAuthCallbackProcesser implements ICancelCoAuthCallback {
        CancelCoAuthCallbackProcesser() {
        }

        @Override // com.huawei.coauth.auth.CoAuth.ICancelCoAuthCallback
        public void onCancelCoAuthFinish(int resultCode) {
            String str = CoAuth.TAG;
            Log.i(str, "cancelCoAuth response message, onCoAuthFinish = " + resultCode);
        }
    }
}
