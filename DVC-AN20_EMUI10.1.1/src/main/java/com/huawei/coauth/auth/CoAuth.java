package com.huawei.coauth.auth;

import android.content.Context;
import android.util.Log;
import com.huawei.coauth.auth.authentity.CoAuthHeaderEntity;
import com.huawei.coauth.auth.authentity.CoAuthPairGroupEntity;
import com.huawei.coauth.auth.authmsg.CoAuthMsgEncodeMgr;
import com.huawei.coauth.msg.CoMessengerClient;
import com.huawei.coauth.msg.Modules;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CoAuth {
    private static final String TAG = CoAuth.class.getName();
    private static Map<String, CoAuthPairGroupEntity> coAuthPairGroupMap = new ConcurrentHashMap();
    private static CoAuth instance;
    private static CoMessengerClient messengerClient;
    private Optional<String> consumerPackageName;

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
        if (CoAuthCheckUtil.connectServiceCheck(context, callback)) {
            this.consumerPackageName = Optional.ofNullable(context.getApplicationContext().getPackageName());
            coAuthServiceConnection(context, callback);
        } else if (callback == null) {
            Log.e(TAG, "connectService request failed, empty callback");
        } else {
            callback.onConnectFailed();
        }
    }

    private static void coAuthServiceConnection(Context context, IConnectServiceCallback callback) {
        messengerClient = CoMessengerClient.getInstance();
        messengerClient.connectServer(context, new ConnectCoAuthServiceCallback(callback));
    }

    public void disconnectService(Context context) {
        Log.i(TAG, "receive client's disconnectService request");
        if (!CoAuthCheckUtil.disconnectServiceCheck(context)) {
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient != null) {
                coMessengerClient.disConnectServer(context);
            }
            Log.i(TAG, "disconnectService from server");
        }
    }

    public void createCoAuthPairGroup(String moduleName, CoAuthDevice peerDevice, ICreateCallback callback) {
        Log.i(TAG, "receive client's createCoAuthPairGroup request");
        if (CoAuthCheckUtil.createCoAuthPairGroupCheck(moduleName, peerDevice, callback)) {
            long sessionId = CoAuthUtil.getNewSessionId();
            CoAuthHeaderEntity coAuthHeaderEntity = CoAuthUtil.getCoAuthHeader(CoAuthUtil.getSelfDeviceUdid(), Modules.DEFAULT_OTHER, peerDevice, 0);
            if (!this.consumerPackageName.isPresent()) {
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                Log.e(TAG, "createCoAuthPairGroup request failed, empty consumer packageName");
                return;
            }
            String str = TAG;
            Log.i(str, "createCoAuthPairGroup request consumer packageName = " + this.consumerPackageName.get());
            byte[] coAuthPairGroupMsg = new CoAuthMsgEncodeMgr().createCoAuthPairGroupMsg(sessionId, this.consumerPackageName.get(), moduleName, coAuthHeaderEntity);
            if (coAuthPairGroupMsg.length == 0) {
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                return;
            }
            CoMessengerClient coMessengerClient = messengerClient;
            if (coMessengerClient == null) {
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                Log.e(TAG, "createCoAuthPairGroup request failed, empty messengerClient");
                return;
            }
            int ret = coMessengerClient.sendMsgToServer(coAuthPairGroupMsg, new CreateCallback(coAuthHeaderEntity, callback, coAuthPairGroupMap));
            Log.i(TAG, "send createCoAuthPairGroup message to server");
            if (ret != 0) {
                String str2 = TAG;
                Log.e(str2, "sendMsgToServer error, callback with response message, CreateCallback.onFailed = " + ret);
                callback.onFailed(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue());
            }
        } else if (callback == null) {
            Log.e(TAG, "createCoAuthPairGroup request failed, empty callback");
        } else {
            callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
        }
    }

    public void destroyCoAuthPairGroup(CoAuthGroup coAuthGroup, IDestroyCallback callback) {
        Log.i(TAG, "receive client's destroyCoAuthPairGroup request");
        if (CoAuthCheckUtil.destroyCoAuthPairGroupCheck(coAuthGroup, callback)) {
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
        } else if (callback == null) {
            Log.e(TAG, "destroyCoAuthPairGroup request failed, empty callback");
        } else {
            callback.onFailed(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
        }
    }

    public void coAuth(CoAuthContext coAuthContext, ICoAuthCallback callback) {
        Log.i(TAG, "receive client's coAuth request");
        if (CoAuthCheckUtil.coAuthCheck(coAuthContext, callback)) {
            coAuthContext.setCoAuthBegin(true);
            long sessionId = coAuthContext.getSessionId();
            CoAuthPairGroupEntity coAuthPairGroupEntity = coAuthPairGroupMap.get(coAuthContext.getCoAuthGroup().getGroupId());
            if (coAuthPairGroupEntity == null) {
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), coAuthContext);
                String str = TAG;
                Log.i(str, "return coAuth result to client = " + CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue());
                return;
            }
            if (!this.consumerPackageName.isPresent()) {
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue(), coAuthContext);
                Log.e(TAG, "coAuth request failed, empty consumer packageName");
            }
            String str2 = TAG;
            Log.i(str2, "coAuth request consumer packageName = " + this.consumerPackageName.get());
            byte[] coAuthMsg = new CoAuthMsgEncodeMgr().coAuthMsg(sessionId, coAuthContext, coAuthPairGroupEntity, this.consumerPackageName.get());
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
                String str3 = TAG;
                Log.e(str3, "sendMsgToServer error, return coAuth result to client = " + ret);
                callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_FAIL.getValue(), coAuthContext);
            }
        } else if (callback == null) {
            Log.e(TAG, "coAuth request failed, empty callback");
        } else {
            callback.onCoAuthFinish(CoAuthRetCode.CO_AUTH_RET_BAD_ACCESS.getValue(), coAuthContext);
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
