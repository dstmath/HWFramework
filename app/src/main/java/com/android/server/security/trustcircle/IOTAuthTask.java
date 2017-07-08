package com.android.server.security.trustcircle;

import android.os.RemoteException;
import com.android.server.security.trustcircle.AuthPara.InitAuthInfo;
import com.android.server.security.trustcircle.AuthPara.OnAuthAckInfo;
import com.android.server.security.trustcircle.AuthPara.RecAuthAckInfo;
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

public class IOTAuthTask extends HwSecurityTaskBase {
    private static final String TAG = null;
    EventListener mCancelListener;
    InitAuthInfo mInitAuthInfo;
    OnAuthAckInfo mOnAuthAckInfo;
    RecAuthAckInfo mRecInfo;
    RetCallback mReqAckCallback;
    RetCallback mReqPkCallback;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.IOTAuthTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.IOTAuthTask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.IOTAuthTask.<clinit>():void");
    }

    public IOTAuthTask(HwSecurityTaskBase parent, RetCallback callback, InitAuthInfo info) {
        super(parent, callback);
        this.mCancelListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                long authID = ((CancelAuthEv) ev).getAuthID();
                if (authID != IOTAuthTask.this.mInitAuthInfo.mAuthID && authID != -2) {
                    return false;
                }
                IOTAuthTask.this.endWithResult(2);
                return true;
            }
        };
        this.mReqPkCallback = new RetCallback() {
            public void onTaskCallback(HwSecurityTaskBase child, int ret) {
                if (ret != 0) {
                    IOTAuthTask.this.endWithResult(ret);
                }
                RespPkInfo pkInfo = ((ServiceReqPkTask) child).getRespPkInfo();
                if (pkInfo != null && pkInfo.mAuthID == IOTAuthTask.this.mRecInfo.mAuthID) {
                    IOTAuthTask.this.mOnAuthAckInfo = AuthUtils.processRecPkMaster(pkInfo);
                    IAuthCallback callback = IOTController.getInstance().getIOTCallback(pkInfo.mAuthID, IOTController.TYPE_MASTER);
                    try {
                        if (IOTAuthTask.this.mOnAuthAckInfo.mResult != 0 || callback == null) {
                            if (callback != null) {
                                callback.onAuthAckError(IOTAuthTask.this.mOnAuthAckInfo.mAuthID, IOTAuthTask.this.mOnAuthAckInfo.mResult);
                                IOTAuthTask.this.endWithResult(3);
                            }
                        }
                        callback.onAuthAck(IOTAuthTask.this.mOnAuthAckInfo.mAuthID, IOTAuthTask.this.mOnAuthAckInfo.mResult, IOTAuthTask.this.mOnAuthAckInfo.mSessionKeyIV, IOTAuthTask.this.mOnAuthAckInfo.mSessionKey, IOTAuthTask.this.mOnAuthAckInfo.mMAC);
                        IOTAuthTask.this.endWithResult(0);
                    } catch (RemoteException e) {
                        LogHelper.e(IOTAuthTask.TAG, e.toString());
                        IOTAuthTask.this.endWithResult(3);
                    } catch (Exception e2) {
                        LogHelper.e(IOTAuthTask.TAG, e2.toString());
                        IOTAuthTask.this.endWithResult(3);
                    }
                }
            }
        };
        this.mReqAckCallback = new RetCallback() {
            public void onTaskCallback(HwSecurityTaskBase child, int ret) {
                LogHelper.d(IOTAuthTask.TAG, child.getClass().getSimpleName() + "-->onTaskCallback, ret: " + ret);
                if (ret != 0) {
                    IOTAuthTask.this.endWithResult(ret);
                    return;
                }
                RequestAuthAckTask result = (RequestAuthAckTask) child;
                IOTAuthTask.this.mRecInfo = result.getRecAuthAckInfo();
                if (IOTAuthTask.this.mRecInfo.mAuthID == IOTAuthTask.this.mInitAuthInfo.mAuthID) {
                    IOTAuthTask.this.mOnAuthAckInfo = AuthUtils.processRecAuthSyncAck(IOTAuthTask.this.mRecInfo);
                    if (IOTAuthTask.this.mOnAuthAckInfo.mResult == 1) {
                        HwSecurityTaskThread.staticPushTask(new ServiceReqPkTask(IOTAuthTask.this, IOTAuthTask.this.mReqPkCallback, IOTAuthTask.this.mOnAuthAckInfo.mAuthID, IOTController.TYPE_MASTER), 1);
                    } else {
                        IOTAuthTask.this.endWithResult(IOTAuthTask.this.callIOTWithAuthResult(IOTAuthTask.this.mOnAuthAckInfo));
                    }
                }
            }
        };
        this.mInitAuthInfo = info;
    }

    public int doAction() {
        LogHelper.d(TAG, "doAction, authID: " + this.mInitAuthInfo.mAuthID);
        HwSecurityTaskThread.staticPushTask(new RequestAuthAckTask(this, this.mReqAckCallback, AuthUtils.processAuthSync(this.mInitAuthInfo)), 1);
        return -1;
    }

    public void onStart() {
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
    }

    public long getAuthID() {
        return this.mInitAuthInfo.mAuthID;
    }

    private int callIOTWithAuthResult(OnAuthAckInfo onAuthAckInfo) {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(onAuthAckInfo.mAuthID, IOTController.TYPE_MASTER);
        if (callback == null) {
            return 3;
        }
        try {
            if (onAuthAckInfo.mResult == 0) {
                callback.onAuthAck(onAuthAckInfo.mAuthID, onAuthAckInfo.mResult, onAuthAckInfo.mSessionKeyIV, onAuthAckInfo.mSessionKey, onAuthAckInfo.mMAC);
                return 0;
            }
            callback.onAuthAckError(onAuthAckInfo.mAuthID, onAuthAckInfo.mResult);
            return 3;
        } catch (RemoteException e) {
            LogHelper.e(TAG, e.toString());
            return 3;
        } catch (Exception e2) {
            LogHelper.e(TAG, e2.toString());
            return 3;
        }
    }
}
