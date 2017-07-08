package com.android.server.security.trustcircle;

import com.android.server.security.trustcircle.AuthPara.InitAuthInfo;
import com.android.server.security.trustcircle.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.AuthPara.RecAuthInfo;
import com.android.server.security.trustcircle.AuthPara.ReqPkInfo;
import com.android.server.security.trustcircle.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.lifecycle.TcisLifeCycleDispatcher;
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
    private static final int AUTH_ID_ERROR = -1;
    public static final int EV_CANCEL_AUTH = 100;
    public static final int EV_CANCEL_AUTH_ALL = 104;
    public static final int EV_IOT_AUTH_ACK = 101;
    public static final int EV_IOT_REC_ACK = 103;
    public static final int EV_IOT_REC_PK = 102;
    public static final int ID_CANCEL_ALL = -2;
    public static final int RESULT_OK = 0;
    public static final int RESULT_REQ_PK = 1;
    private static final String TAG = null;
    public static final int TYPE_MASTER = 1000;
    public static final int TYPE_SLAVE = 1001;
    private static long sGenAuthID;
    private static IOTController sInstance;
    Map<Long, InitAuthInfo> mClientsMaster;
    Map<Long, RecAuthInfo> mClientsSlave;
    RetCallback mInitCallback;
    private Object mLock;
    RetCallback mRecCallback;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.IOTController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.IOTController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.IOTController.<clinit>():void");
    }

    private IOTController() {
        this.mLock = new Object();
        this.mClientsMaster = new ConcurrentHashMap();
        this.mClientsSlave = new ConcurrentHashMap();
        this.mInitCallback = new RetCallback() {
            public void onTaskCallback(HwSecurityTaskBase base, int ret) {
                LogHelper.d(IOTController.TAG, "auth onTaskCallback, ret: " + ret);
                long authID = ((IOTAuthTask) base).getAuthID();
                IOTController.this.mClientsMaster.remove(Long.valueOf(authID));
                if (ret == 2) {
                    AuthUtils.processCancelAuth(authID);
                }
            }
        };
        this.mRecCallback = new RetCallback() {
            public void onTaskCallback(HwSecurityTaskBase base, int ret) {
                LogHelper.d(IOTController.TAG, "receive auth onTaskCallback, ret: " + ret);
                long authID = ((RecAuthTask) base).getAuthID();
                IOTController.this.mClientsSlave.remove(Long.valueOf(authID));
                if (ret == 2) {
                    AuthUtils.processCancelAuth(authID);
                }
            }
        };
    }

    public static IOTController getInstance() {
        if (sInstance == null) {
            sInstance = new IOTController();
        }
        return sInstance;
    }

    long initAuth(IAuthCallback callback, int authType, int authVersion, int policy, long userID, byte[] AESTmpKey) {
        if (TcisLifeCycleDispatcher.checkUserIDLogined(userID)) {
            long authID = generateAuthID();
            LogHelper.d(TAG, "start auth, authId: " + authID);
            InitAuthInfo info = new InitAuthInfo(callback, authID, authType, authVersion, policy, userID, AESTmpKey);
            this.mClientsMaster.put(Long.valueOf(authID), info);
            HwSecurityTaskThread.staticPushTask(new IOTAuthTask(null, this.mInitCallback, info), RESULT_REQ_PK);
            return authID;
        }
        LogHelper.i(TAG, "userID: " + userID + " is not in login state.");
        return -1;
    }

    long receiveAuthSync(IAuthCallback callback, int authType, int authVersion, int taVersion, int policy, long userID, byte[] AESTmpKey, byte[] tcisId, int pkVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
        long authID = generateAuthID();
        LogHelper.d(TAG, "receive auth, authID: " + authID);
        IAuthCallback iAuthCallback = callback;
        int i = authType;
        int i2 = authVersion;
        int i3 = policy;
        long j = userID;
        byte[] bArr = AESTmpKey;
        byte[] bArr2 = tcisId;
        int i4 = pkVersion;
        long j2 = nonce;
        RecAuthInfo info = new RecAuthInfo(authID, iAuthCallback, i, i2, (short) taVersion, i3, j, bArr, bArr2, i4, j2, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign);
        this.mClientsSlave.put(Long.valueOf(authID), info);
        HwSecurityTaskThread.staticPushTask(new RecAuthTask(null, this.mRecCallback, info), RESULT_REQ_PK);
        return authID;
    }

    boolean receiveAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoTypeSlave, byte[] authKeyInfoSlave, byte[] authKeyInfoSignSlave) {
        LogHelper.d(TAG, "receive authAck, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new AuthSyncAckEv(EV_IOT_AUTH_ACK, new RecAuthAckInfo(authID, tcisIdSlave, pkVersionSlave, nonceSlave, mac, (short) authKeyAlgoTypeSlave, authKeyInfoSlave, authKeyInfoSignSlave))), RESULT_REQ_PK);
        return true;
    }

    boolean requestPK(long authID, long userID) {
        int type;
        LogHelper.d(TAG, "IOT requestPK, authID: " + authID);
        ReqPkInfo reqPkInfo = new ReqPkInfo(userID, authID);
        if (this.mClientsMaster.containsKey(Long.valueOf(authID))) {
            type = TYPE_MASTER;
        } else if (!this.mClientsSlave.containsKey(Long.valueOf(authID))) {
            return false;
        } else {
            type = TYPE_SLAVE;
        }
        HwSecurityTaskThread.staticPushTask(new IOTReqPkTask(null, null, reqPkInfo, type), RESULT_REQ_PK);
        return true;
    }

    boolean receivePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
        LogHelper.d(TAG, "receivePK, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new ReceivePkEv(EV_IOT_REC_PK, new RespPkInfo(authID, (short) authKeyAlgoType, authKeyData, authKeyDataSign, RESULT_OK))), RESULT_REQ_PK);
        return true;
    }

    boolean receiveAck(long authID, byte[] mac) {
        LogHelper.d(TAG, "receiveAck, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new ReceiveAckEv(EV_IOT_REC_ACK, new RecAckInfo(authID, mac))), RESULT_REQ_PK);
        return true;
    }

    public boolean cancelAuth(long authID) {
        LogHelper.i(TAG, "cancelAuth, authID: " + authID);
        HwSecurityTaskThread.staticPushTask(new HwSecurityEventTask(new CancelAuthEv(EV_CANCEL_AUTH, authID)), RESULT_OK);
        return true;
    }

    private long generateAuthID() {
        long j;
        synchronized (this.mLock) {
            j = sGenAuthID + 1;
            sGenAuthID = j;
        }
        return j;
    }

    IAuthCallback getIOTCallback(long authID, int type) {
        if (type == TYPE_MASTER && this.mClientsMaster != null) {
            InitAuthInfo info = (InitAuthInfo) this.mClientsMaster.get(Long.valueOf(authID));
            if (info != null) {
                return info.mCallback;
            }
            return null;
        } else if (type != TYPE_SLAVE || this.mClientsSlave == null) {
            return null;
        } else {
            RecAuthInfo info2 = (RecAuthInfo) this.mClientsSlave.get(Long.valueOf(authID));
            if (info2 != null) {
                return info2.mCallback;
            }
            return null;
        }
    }
}
