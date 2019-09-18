package com.android.server.security.trustcircle.auth;

import android.content.Context;
import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEventTask;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskThread;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;
import huawei.android.security.IKaCallback;
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
    /* access modifiers changed from: private */
    public static final String TAG = IOTController.class.getSimpleName();
    public static final int TYPE_MASTER = 1000;
    public static final int TYPE_SLAVE = 1001;
    private static Object mLock = new Object();
    private static long sGenAuthID;
    private static volatile IOTController sInstance;
    Map<Long, AuthPara.InitAuthInfo> mClientsMaster = new ConcurrentHashMap();
    Map<Long, AuthPara.RecAuthInfo> mClientsSlave = new ConcurrentHashMap();
    HwSecurityTaskBase.RetCallback mInitCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase base, int ret) {
            String access$000 = IOTController.TAG;
            LogHelper.d(access$000, "auth onTaskCallback, ret: " + ret);
            long authID = ((IOTAuthTask) base).getAuthID();
            if (ret == 2 || ret == 1) {
                AuthUtils.processCancelAuth(authID);
                IOTController.this.notifyAuthExited(authID, ret, 1000);
            }
            IOTController.this.mClientsMaster.remove(Long.valueOf(authID));
        }
    };
    HwSecurityTaskBase.RetCallback mRecCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase base, int ret) {
            String access$000 = IOTController.TAG;
            LogHelper.d(access$000, "receive auth onTaskCallback, ret: " + ret);
            long authID = ((RecAuthTask) base).getAuthID();
            if (ret == 2 || ret == 1) {
                AuthUtils.processCancelAuth(authID);
                IOTController.this.notifyAuthExited(authID, ret, 1001);
            }
            IOTController.this.mClientsSlave.remove(Long.valueOf(authID));
        }
    };

    public static class KaInfoRequest {
        public byte[] aesTmpKey;
        public long authId;
        public String kaInfo;
        public int kaVersion;
        public long userId;

        public KaInfoRequest(long authId2, int kaVersion2, long userId2, byte[] aesTmpKey2, String kaInfo2) {
            this.authId = authId2;
            this.kaVersion = kaVersion2;
            this.userId = userId2;
            this.aesTmpKey = (byte[]) aesTmpKey2.clone();
            this.kaInfo = kaInfo2;
        }
    }

    public static class KaInfoResponse {
        public byte[] iv;
        public byte[] payload;
        public int result;

        public KaInfoResponse(int result2, byte[] iv2, byte[] payload2) {
            this.result = result2;
            this.iv = (byte[]) iv2.clone();
            this.payload = (byte[]) payload2.clone();
        }
    }

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

    public long initKeyAgreement(IKaCallback callback, int kaVersion, long userId, byte[] aesTmpKey, String kaInfo, Context context) {
        long authId = generateAuthID();
        KaInfoRequest authInfo = new KaInfoRequest(authId, kaVersion, userId, aesTmpKey, kaInfo);
        KaRequestTask kaRequestTask = new KaRequestTask(null, null, context, authInfo, callback);
        HwSecurityTaskThread.staticPushTask(kaRequestTask, 1);
        return authId;
    }

    public long initAuth(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
        long authID = generateAuthID();
        String str = TAG;
        LogHelper.d(str, "start auth, authId: " + authID);
        AuthPara.InitAuthInfo info = new AuthPara.InitAuthInfo(callback, authID, authType, authVersion, policy, userID, AESTmpKey);
        this.mClientsMaster.put(Long.valueOf(authID), info);
        HwSecurityTaskThread.staticPushTask(new IOTAuthTask(null, this.mInitCallback, info), 1);
        return authID;
    }

    public long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
        long authID = generateAuthID();
        String str = TAG;
        LogHelper.d(str, "receive auth, authID: " + authID);
        AuthPara.RecAuthInfo info = new AuthPara.RecAuthInfo(authID, callback, authType, authVersion, (short) taVersion, policy, userID, AESTmpKey, tcisId, pkVersion, nonce, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign);
        long authID2 = authID;
        this.mClientsSlave.put(Long.valueOf(authID2), info);
        HwSecurityTaskThread.staticPushTask(new RecAuthTask(null, this.mRecCallback, info), 1);
        return authID2;
    }

    public boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) {
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("receive authAck, authID: ");
        long j = authID;
        sb.append(j);
        LogHelper.d(str, sb.toString());
        AuthPara.RecAuthAckInfo recAuthAckInfo = new AuthPara.RecAuthAckInfo(j, tcisIdSlave, pkVersionSlave, nonceSlave, mac, (short) authKeyAlgoTypeSlave, authKeyInfoSlave, authKeyInfoSignSlave);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new AuthSyncAckEv(101, recAuthAckInfo)), 1);
        return true;
    }

    public boolean requestPK(long authID, long userID) {
        int type;
        String str = TAG;
        LogHelper.d(str, "IOT requestPK, authID: " + authID);
        AuthPara.ReqPkInfo reqPkInfo = new AuthPara.ReqPkInfo(userID, authID);
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
        String str = TAG;
        LogHelper.d(str, "receivePK, authID: " + authID);
        AuthPara.RespPkInfo respPkInfo = new AuthPara.RespPkInfo(authID, (short) authKeyAlgoType, authKeyData, authKeyDataSign, 0);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new ReceivePkEv(102, respPkInfo)), 1);
        return true;
    }

    public boolean receiveAck(long authID, byte[] mac) {
        String str = TAG;
        LogHelper.d(str, "receiveAck, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new ReceiveAckEv(103, new AuthPara.RecAckInfo(authID, mac))), 1);
        return true;
    }

    public boolean cancelAuth(long authID) {
        String str = TAG;
        LogHelper.i(str, "cancelAuth, authID: " + authID);
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
            AuthPara.InitAuthInfo info = this.mClientsMaster.get(Long.valueOf(authID));
            if (info != null) {
                return info.mCallback;
            }
            return null;
        } else if (type != 1001 || this.mClientsSlave == null) {
            return null;
        } else {
            AuthPara.RecAuthInfo info2 = this.mClientsSlave.get(Long.valueOf(authID));
            if (info2 != null) {
                return info2.mCallback;
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void notifyAuthExited(long authID, int ret, int type) {
        IAuthCallback callback = getIOTCallback(authID, type);
        if (callback == null) {
            return;
        }
        if (ret == 2) {
            try {
                callback.onAuthExited(authID, 2046820389);
            } catch (RemoteException e) {
                String str = TAG;
                LogHelper.e(str, "notifyAuthExited failed, " + e.toString());
            } catch (Exception e2) {
                String str2 = TAG;
                LogHelper.e(str2, "notifyAuthExited failed, " + e2.toString());
            }
        } else if (ret == 1) {
            callback.onAuthExited(authID, 2046820388);
        }
    }
}
