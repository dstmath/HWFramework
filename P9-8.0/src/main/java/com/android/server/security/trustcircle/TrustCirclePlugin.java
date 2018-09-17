package com.android.server.security.trustcircle;

import android.app.ActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import com.android.server.security.trustcircle.auth.IOTController;
import com.android.server.security.trustcircle.jni.TcisJNI;
import com.android.server.security.trustcircle.lifecycle.LifeCycleProcessor;
import com.android.server.security.trustcircle.lifecycle.NetworkChangeReceiver;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskThread;
import com.android.server.security.trustcircle.task.HwSecurityTimer;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.LifeCycleStauts;
import com.android.server.security.trustcircle.utils.Status.TCIS_Result;
import com.android.server.security.trustcircle.utils.Utils;
import huawei.android.security.IAuthCallback;
import huawei.android.security.ILifeCycleCallback;
import huawei.android.security.ITrustCircleManager.Stub;

public class TrustCirclePlugin extends Stub implements IHwSecurityPlugin {
    public static final Object BINDLOCK = new Object();
    public static final Creator CREATOR = new Creator() {
        public IHwSecurityPlugin createPlugin(Context context) {
            LogHelper.d(TrustCirclePlugin.TAG, "createTrustCirclePlugin");
            return new TrustCirclePlugin(context);
        }

        public String getPluginPermission() {
            return TrustCirclePlugin.MANAGE_TRUSTCIRCLE;
        }
    };
    private static final String MANAGE_TRUSTCIRCLE = "com.huawei.permission.USE_TRUSTCIRCLE_MANAGER";
    private static final int MSG_PROCESS_CMD = 10;
    private static final String TAG = TrustCirclePlugin.class.getSimpleName();
    private Context mContext;
    private NetworkChangeReceiver mNetworkReceiver;

    public TrustCirclePlugin(Context context) {
        this.mContext = context;
    }

    public void onStart() {
        registerNetworkReceiver();
        listenForUserSwitches();
        HwSecurityMsgCenter.createInstance();
        HwSecurityTaskThread.createInstance();
        HwSecurityTaskThread.getInstance().startThread();
        TcisJNI.start();
        TcisLifeCycleDispatcher.getInstance().setContext(this.mContext);
        TcisLifeCycleDispatcher.getInstance().initTcisOfCurrentUser();
    }

    public void onStop() {
        unregisterNetworkReceiver();
        HwSecurityTaskThread.getInstance().stopThread();
        HwSecurityTaskThread.destroyInstance();
        HwSecurityMsgCenter.destroyInstance();
        HwSecurityTimer.destroyInstance();
        TcisJNI.stop();
    }

    public IBinder asBinder() {
        return this;
    }

    private void registerNetworkReceiver() {
        if (this.mContext != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mNetworkReceiver = new NetworkChangeReceiver();
            this.mContext.registerReceiver(this.mNetworkReceiver, filter);
            LogHelper.d(TAG, "registerNetworkReceiver successed");
            return;
        }
        LogHelper.e(TAG, "error:registerNetworkReceiver failed - Context is null");
    }

    private void unregisterNetworkReceiver() {
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mNetworkReceiver);
            this.mNetworkReceiver = null;
            LogHelper.d(TAG, "unregisterNetworkReceiver successed");
            return;
        }
        LogHelper.e(TAG, "error:unregisterNetworkReceiver failed - Context is null");
    }

    public boolean checkCallingAcrossUser() {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        int currentUserId = Utils.getCurrentUserId();
        if (callingUserId == currentUserId) {
            return false;
        }
        LogHelper.w(TAG, "calling from [user: " + callingUserId + ",pid: " + Binder.getCallingPid() + "] ,current user: " + currentUserId);
        return true;
    }

    public Bundle getTcisInfo() {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return null;
        }
        return TcisLifeCycleDispatcher.getInstance().getTcisInfo();
    }

    public int getCurrentState() {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return -1;
        }
        return TcisLifeCycleDispatcher.getInstance().getCurrentState();
    }

    public void loginServerRequest(ILifeCycleCallback callback, long userID, int serverRegisterStatus, String sessionID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!CallbackManager.getInstance().registerILifeCycleCallback(callback)) {
            return;
        }
        if (checkCallingAcrossUser()) {
            if (serverRegisterStatus == LifeCycleStauts.NOT_REGISTER.ordinal()) {
                LifeCycleProcessor.onRegisterResponse(TCIS_Result.CROSS_USER.value(), -1, -1, null, null, null);
            } else {
                LifeCycleProcessor.onLoginResponse(TCIS_Result.CROSS_USER.value(), -1, null);
            }
            return;
        }
        TcisLifeCycleDispatcher.getInstance().loginServerRequest(userID, serverRegisterStatus, sessionID);
    }

    public void updateServerRequest(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!CallbackManager.getInstance().registerILifeCycleCallback(callback)) {
            return;
        }
        if (checkCallingAcrossUser()) {
            LifeCycleProcessor.onUpdateResponse(TCIS_Result.CROSS_USER.value(), -1, null);
        } else {
            TcisLifeCycleDispatcher.getInstance().updateServerRequest(userID);
        }
    }

    public void finalRegister(ILifeCycleCallback callback, String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!CallbackManager.getInstance().isILifeCycleCallbackValid() && !CallbackManager.getInstance().registerILifeCycleCallback(callback)) {
            return;
        }
        if (checkCallingAcrossUser()) {
            LifeCycleProcessor.onFinalRegisterResult(TCIS_Result.CROSS_USER.value());
        } else {
            TcisLifeCycleDispatcher.getInstance().finalRegister(authPKData, authPKDataSign, updateIndexInfo, updateIndexSignature);
        }
    }

    public void finalLogin(ILifeCycleCallback callback, int updateResult, String updateIndexInfo, String updateIndexSignature) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!CallbackManager.getInstance().isILifeCycleCallbackValid() && !CallbackManager.getInstance().registerILifeCycleCallback(callback)) {
            return;
        }
        if (checkCallingAcrossUser()) {
            LifeCycleProcessor.onFinalLoginResult(TCIS_Result.CROSS_USER.value());
        } else {
            TcisLifeCycleDispatcher.getInstance().finalLogin(updateResult, updateIndexInfo, updateIndexSignature);
        }
    }

    public void logout(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!CallbackManager.getInstance().registerILifeCycleCallback(callback)) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in logout");
        } else if (checkCallingAcrossUser()) {
            LifeCycleProcessor.onLogoutResult(TCIS_Result.CROSS_USER.value());
        } else {
            IOTController.getInstance().cancelAuth(-2);
            TcisLifeCycleDispatcher.getInstance().logout(userID);
        }
    }

    public void cancelRegOrLogin(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if ((CallbackManager.getInstance().isILifeCycleCallbackValid() || CallbackManager.getInstance().registerILifeCycleCallback(callback)) && !checkCallingAcrossUser()) {
            TcisLifeCycleDispatcher.getInstance().cancelRegOrLogin(userID);
        }
    }

    public void unregister(ILifeCycleCallback callback, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!CallbackManager.getInstance().registerILifeCycleCallback(callback)) {
            LogHelper.e(TAG, "error:ILifeCycleCallback is null in unregister");
        } else if (checkCallingAcrossUser()) {
            LifeCycleProcessor.onUnregisterResult(TCIS_Result.CROSS_USER.value());
        } else {
            IOTController.getInstance().cancelAuth(-2);
            TcisLifeCycleDispatcher.getInstance().unregister(userID);
        }
    }

    private void listenForUserSwitches() {
        try {
            ActivityManager.getService().registerUserSwitchObserver(new SynchronousUserSwitchObserver() {
                public void onUserSwitching(int newUserId) throws RemoteException {
                    LogHelper.d(TrustCirclePlugin.TAG, "onUserSwitching- newUserId: " + newUserId);
                    TcisLifeCycleDispatcher.setIotEnable(false);
                    IOTController.getInstance().cancelAuth(-2);
                }

                public void onUserSwitchComplete(int newUserId) throws RemoteException {
                    LogHelper.d(TrustCirclePlugin.TAG, "onUserSwitchComplete- newUserId: " + newUserId);
                    TcisLifeCycleDispatcher.getInstance().initTcisByUserHandle(newUserId);
                }

                public void onForegroundProfileSwitch(int newProfileId) {
                }
            }, TAG);
        } catch (RemoteException e) {
            LogHelper.w(TAG, "Failed to listen for user switching event");
        }
    }

    public long initAuthenticate(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return -1;
        }
        TcisLifeCycleDispatcher.getInstance();
        if (TcisLifeCycleDispatcher.isReadyForIot(userID)) {
            return IOTController.getInstance().initAuth(callback, authType, authVersion, policy, userID, AESTmpKey);
        }
        NetworkChangeReceiver.requestApkServiceLogin(this.mContext);
        return -1;
    }

    public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return -1;
        }
        TcisLifeCycleDispatcher.getInstance();
        if (TcisLifeCycleDispatcher.isReadyForIot(userID)) {
            return IOTController.getInstance().receiveAuthSync(callback, authType, authVersion, taVersion, policy, userID, AESTmpKey, tcisId, pkVersion, nonce, authKeyAlgoType, authKeyInfo, authKeyInfoSign);
        }
        NetworkChangeReceiver.requestApkServiceLogin(this.mContext);
        return -1;
    }

    public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return false;
        }
        return IOTController.getInstance().receiveAuthSyncAck(authID, tcisIdSlave, pkVersionSlave, nonceSlave, mac, authKeyAlgoTypeSlave, authKeyInfoSlave, authKeyInfoSignSlave);
    }

    public boolean receiveAck(long authID, byte[] mac) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return false;
        }
        return IOTController.getInstance().receiveAck(authID, mac);
    }

    public boolean requestPK(long authID, long userID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return false;
        }
        return IOTController.getInstance().requestPK(authID, userID);
    }

    public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (checkCallingAcrossUser()) {
            return false;
        }
        return IOTController.getInstance().receivePK(authID, authKeyAlgoType, authKeyData, authKeyDataSign);
    }

    public void cancelAuthentication(long authID) {
        this.mContext.enforceCallingOrSelfPermission(MANAGE_TRUSTCIRCLE, null);
        if (!checkCallingAcrossUser()) {
            IOTController.getInstance().cancelAuth(authID);
        }
    }
}
