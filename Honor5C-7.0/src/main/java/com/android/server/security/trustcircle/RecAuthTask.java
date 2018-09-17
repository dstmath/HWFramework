package com.android.server.security.trustcircle;

import android.os.RemoteException;
import com.android.server.security.trustcircle.AuthPara.OnAuthAckInfo;
import com.android.server.security.trustcircle.AuthPara.OnAuthSyncAckInfo;
import com.android.server.security.trustcircle.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.AuthPara.RecAuthInfo;
import com.android.server.security.trustcircle.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskThread;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class RecAuthTask extends HwSecurityTaskBase {
    private static final String TAG = null;
    EventListener mCancelListener;
    RecAuthInfo mRecAuthInfo;
    RetCallback mReqAckCallback;
    RetCallback mReqPkCallback;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.RecAuthTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.RecAuthTask.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.RecAuthTask.<clinit>():void");
    }

    public RecAuthTask(HwSecurityTaskBase parent, RetCallback callback, RecAuthInfo info) {
        super(parent, callback);
        this.mCancelListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                long authID = ((CancelAuthEv) ev).getAuthID();
                if (authID != RecAuthTask.this.mRecAuthInfo.mAuthID && authID != -2) {
                    return false;
                }
                RecAuthTask.this.endWithResult(2);
                return true;
            }
        };
        this.mReqPkCallback = new RetCallback() {
            public void onTaskCallback(HwSecurityTaskBase child, int ret) {
                if (ret != 0) {
                    RecAuthTask.this.endWithResult(ret);
                    return;
                }
                RespPkInfo pkInfo = ((ServiceReqPkTask) child).getRespPkInfo();
                if (pkInfo.mAuthID == RecAuthTask.this.mRecAuthInfo.mAuthID) {
                    OnAuthSyncAckInfo info = AuthUtils.processRecPkSlave(pkInfo);
                    if (info.mResult == 0) {
                        HwSecurityTaskThread.staticPushTask(new RequestRecAckTask(RecAuthTask.this, RecAuthTask.this.mReqAckCallback, info), 1);
                        return;
                    }
                    IAuthCallback callback = IOTController.getInstance().getIOTCallback(info.mAuthID, IOTController.TYPE_SLAVE);
                    if (callback != null) {
                        try {
                            callback.onAuthSyncAckError(info.mAuthID, info.mResult);
                        } catch (RemoteException e) {
                            LogHelper.e(RecAuthTask.TAG, e.toString());
                        } catch (Exception e2) {
                            LogHelper.e(RecAuthTask.TAG, e2.toString());
                        }
                    }
                    RecAuthTask.this.endWithResult(ret);
                }
            }
        };
        this.mReqAckCallback = new RetCallback() {
            public void onTaskCallback(HwSecurityTaskBase child, int ret) {
                if (ret != 0) {
                    RecAuthTask.this.endWithResult(ret);
                    return;
                }
                RecAckInfo ackInfo = ((RequestRecAckTask) child).getRecAckInfo();
                if (ackInfo.mAuthID == RecAuthTask.this.mRecAuthInfo.mAuthID) {
                    OnAuthAckInfo onAuthAckInfo = AuthUtils.processAckRec(ackInfo);
                    IAuthCallback callback = IOTController.getInstance().getIOTCallback(ackInfo.mAuthID, IOTController.TYPE_SLAVE);
                    if (callback != null) {
                        if (onAuthAckInfo != null) {
                            try {
                                if (onAuthAckInfo.mResult == 0) {
                                    callback.onAuthAck(onAuthAckInfo.mAuthID, onAuthAckInfo.mResult, onAuthAckInfo.mSessionKeyIV, onAuthAckInfo.mSessionKey, new byte[0]);
                                }
                            } catch (RemoteException e) {
                                LogHelper.e(RecAuthTask.TAG, e.toString());
                            } catch (Exception e2) {
                                LogHelper.e(RecAuthTask.TAG, e2.toString());
                            }
                        }
                        callback.onAuthAckError(ackInfo.mAuthID, onAuthAckInfo.mResult);
                    }
                    RecAuthTask.this.endWithResult(ret);
                }
            }
        };
        this.mRecAuthInfo = info;
    }

    public int doAction() {
        LogHelper.d(TAG, "doAction, authID: " + this.mRecAuthInfo.mAuthID);
        OnAuthSyncAckInfo info = AuthUtils.processAuthSyncRec(this.mRecAuthInfo);
        if (info.mResult == 1) {
            HwSecurityTaskThread.staticPushTask(new ServiceReqPkTask(this, this.mReqPkCallback, info.mAuthID, IOTController.TYPE_SLAVE), 1);
            return -1;
        } else if (info.mResult == 0) {
            HwSecurityTaskThread.staticPushTask(new RequestRecAckTask(this, this.mReqAckCallback, info), 1);
            return -1;
        } else {
            IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mRecAuthInfo.mAuthID, IOTController.TYPE_SLAVE);
            if (callback != null) {
                try {
                    callback.onAuthSyncAckError(this.mRecAuthInfo.mAuthID, info.mResult);
                } catch (RemoteException e) {
                    LogHelper.e(TAG, e.toString());
                } catch (Exception e2) {
                    LogHelper.e(TAG, e2.toString());
                }
            }
            return 3;
        }
    }

    public void onStart() {
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
    }

    public long getAuthID() {
        return this.mRecAuthInfo.mAuthID;
    }
}
