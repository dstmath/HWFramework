package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.InitAuthInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthInfo;
import com.android.server.security.trustcircle.auth.AuthPara.ReqPkInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEventTask;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskThread;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IOTController {
    public static final int AUTH_ID_ERROR = -1;
    public static final int EV_CANCEL_AUTH = 100;
    public static final int EV_CANCEL_AUTH_ALL = 104;
    public static final int EV_IOT_AUTH_ACK = 101;
    public static final int EV_IOT_REC_ACK = 103;
    public static final int EV_IOT_REC_PK = 102;
    public static final int ID_CANCEL_ALL = -2;
    public static final int RESULT_OK = 0;
    public static final int RESULT_REQ_PK = 1;
    private static final String TAG = IOTController.class.getSimpleName();
    public static final int TYPE_MASTER = 1000;
    public static final int TYPE_SLAVE = 1001;
    private static Object mLock = new Object();
    private static long sGenAuthID;
    private static volatile IOTController sInstance;
    Map<Long, InitAuthInfo> mClientsMaster = new ConcurrentHashMap();
    Map<Long, RecAuthInfo> mClientsSlave = new ConcurrentHashMap();
    RetCallback mInitCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase base, int ret) {
            LogHelper.d(IOTController.TAG, "auth onTaskCallback, ret: " + ret);
            long authID = ((IOTAuthTask) base).getAuthID();
            if (ret == 2 || ret == 1) {
                AuthUtils.processCancelAuth(authID);
                IOTController.this.notifyAuthExited(authID, ret, 1000);
            }
            IOTController.this.mClientsMaster.remove(Long.valueOf(authID));
        }
    };
    RetCallback mRecCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase base, int ret) {
            LogHelper.d(IOTController.TAG, "receive auth onTaskCallback, ret: " + ret);
            long authID = ((RecAuthTask) base).getAuthID();
            if (ret == 2 || ret == 1) {
                AuthUtils.processCancelAuth(authID);
                IOTController.this.notifyAuthExited(authID, ret, 1001);
            }
            IOTController.this.mClientsSlave.remove(Long.valueOf(authID));
        }
    };

    private IOTController() {
    }

    public static IOTController getInstance() {
        if (sInstance == null) {
            synchronized (IOTController.class) {
                if (sInstance == null) {
                    sInstance = new IOTController();
                }
            }
        }
        return sInstance;
    }

    public long initAuth(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
        long authID = generateAuthID();
        LogHelper.d(TAG, "start auth, authId: " + authID);
        InitAuthInfo info = new InitAuthInfo(callback, authID, authType, authVersion, policy, userID, AESTmpKey);
        this.mClientsMaster.put(Long.valueOf(authID), info);
        HwSecurityTaskThread.staticPushTask(new IOTAuthTask(null, this.mInitCallback, info), 1);
        return authID;
    }

    public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
        long authID = generateAuthID();
        LogHelper.d(TAG, "receive auth, authID: " + authID);
        RecAuthInfo info = new RecAuthInfo(authID, callback, authType, authVersion, (short) taVersion, policy, userID, AESTmpKey, tcisId, pkVersion, nonce, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign);
        this.mClientsSlave.put(Long.valueOf(authID), info);
        HwSecurityTaskThread.staticPushTask(new RecAuthTask(null, this.mRecCallback, info), 1);
        return authID;
    }

    public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) {
        LogHelper.d(TAG, "receive authAck, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new AuthSyncAckEv(101, new RecAuthAckInfo(authID, tcisIdSlave, pkVersionSlave, nonceSlave, mac, (short) authKeyAlgoTypeSlave, authKeyInfoSlave, authKeyInfoSignSlave))), 1);
        return true;
    }

    public boolean requestPK(long authID, long userID) {
        int type;
        LogHelper.d(TAG, "IOT requestPK, authID: " + authID);
        ReqPkInfo reqPkInfo = new ReqPkInfo(userID, authID);
        if (this.mClientsMaster.containsKey(Long.valueOf(authID))) {
            type = 1000;
        } else if (!this.mClientsSlave.containsKey(Long.valueOf(authID))) {
            return false;
        } else {
            type = 1001;
        }
        HwSecurityTaskThread.staticPushTask(new IOTReqPkTask(null, null, reqPkInfo, type), 1);
        return true;
    }

    public boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
        LogHelper.d(TAG, "receivePK, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new ReceivePkEv(102, new RespPkInfo(authID, (short) authKeyAlgoType, authKeyData, authKeyDataSign, 0))), 1);
        return true;
    }

    public boolean receiveAck(long authID, byte[] mac) {
        LogHelper.d(TAG, "receiveAck, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new ReceiveAckEv(103, new RecAckInfo(authID, mac))), 1);
        return true;
    }

    public boolean cancelAuth(long authID) {
        LogHelper.i(TAG, "cancelAuth, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new CancelAuthEv(100, authID)), 0);
        return true;
    }

    private long generateAuthID() {
        long j;
        synchronized (mLock) {
            j = sGenAuthID + 1;
            sGenAuthID = j;
        }
        return j;
    }

    public IAuthCallback getIOTCallback(long authID, int type) {
        if (type == 1000 && this.mClientsMaster != null) {
            InitAuthInfo info = (InitAuthInfo) this.mClientsMaster.get(Long.valueOf(authID));
            if (info != null) {
                return info.mCallback;
            }
            return null;
        } else if (type != 1001 || this.mClientsSlave == null) {
            return null;
        } else {
            RecAuthInfo info2 = (RecAuthInfo) this.mClientsSlave.get(Long.valueOf(authID));
            if (info2 != null) {
                return info2.mCallback;
            }
            return null;
        }
    }

    private void notifyAuthExited(long authID, int ret, int type) {
        IAuthCallback callback = getIOTCallback(authID, type);
        if (callback == null) {
            return;
        }
        if (ret == 2) {
            try {
                callback.onAuthExited(authID, 2046820389);
            } catch (RemoteException e) {
                LogHelper.e(TAG, "notifyAuthExited failed, " + e.toString());
            } catch (Exception e2) {
                LogHelper.e(TAG, "notifyAuthExited failed, " + e2.toString());
            }
        } else if (ret == 1) {
            callback.onAuthExited(authID, 2046820388);
        }
    }
}
