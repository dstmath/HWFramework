package com.android.server.security.trustcircle;

import android.os.RemoteException;
import com.android.server.security.trustcircle.AuthPara.OnAuthSyncAckInfo;
import com.android.server.security.trustcircle.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.TimerOutProc;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.security.IAuthCallback;

public class RequestRecAckTask extends HwSecurityTaskBase {
    private static final String TAG = null;
    private static final long TIME_OUT = 5000;
    EventListener mCancelListener;
    private OnAuthSyncAckInfo mOnAuthSyncAckInfo;
    private RecAckInfo mRecAckInfo;
    EventListener mRecAckListener;
    TimerOutProc mTimeoutProc;
    private HwSecurityTimerTask mTimer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.RequestRecAckTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.RequestRecAckTask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.RequestRecAckTask.<clinit>():void");
    }

    public RequestRecAckTask(HwSecurityTaskBase parent, RetCallback callback, OnAuthSyncAckInfo info) {
        super(parent, callback);
        this.mCancelListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                long authID = ((CancelAuthEv) ev).getAuthID();
                if (authID != RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID && authID != -2) {
                    return false;
                }
                LogHelper.i(RequestRecAckTask.TAG, "onCancelAuthEvent, authID: " + authID);
                RequestRecAckTask.this.endWithResult(2);
                RequestRecAckTask.this.mTimer.cancel();
                return true;
            }
        };
        this.mRecAckListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                RequestRecAckTask.this.mRecAckInfo = ((ReceiveAckEv) ev).getRecAckInfo();
                LogHelper.i(RequestRecAckTask.TAG, "receiveAcEvent, authID: " + RequestRecAckTask.this.mRecAckInfo.mAuthID + ", mAuthID: " + RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID);
                if (RequestRecAckTask.this.mRecAckInfo.mAuthID != RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID) {
                    return false;
                }
                RequestRecAckTask.this.mTimer.cancel();
                RequestRecAckTask.this.endWithResult(0);
                return true;
            }
        };
        this.mTimeoutProc = new TimerOutProc() {
            public void onTimerOut() {
                RequestRecAckTask.this.endWithResult(1);
            }
        };
        this.mTimer = new HwSecurityTimerTask();
        this.mOnAuthSyncAckInfo = info;
    }

    RecAckInfo getRecAckInfo() {
        return this.mRecAckInfo;
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mOnAuthSyncAckInfo.mAuthID, IOTController.TYPE_SLAVE);
        if (callback != null) {
            try {
                LogHelper.i(TAG, "onAuthSyncAck, result: " + this.mOnAuthSyncAckInfo.mResult);
                if (this.mOnAuthSyncAckInfo.mResult == 0) {
                    callback.onAuthSyncAck(this.mOnAuthSyncAckInfo.mAuthID, this.mOnAuthSyncAckInfo.mTcisIdSlave, this.mOnAuthSyncAckInfo.mPkVersionSlave, this.mOnAuthSyncAckInfo.mNonceSlave, this.mOnAuthSyncAckInfo.mMAC, this.mOnAuthSyncAckInfo.mAuthKeyAlgoType, this.mOnAuthSyncAckInfo.mAuthKeyInfo, this.mOnAuthSyncAckInfo.mAuthKeyInfoSign);
                } else {
                    callback.onAuthSyncAckError(this.mOnAuthSyncAckInfo.mAuthID, this.mOnAuthSyncAckInfo.mResult);
                }
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.toString());
            } catch (Exception e2) {
                LogHelper.e(TAG, e2.toString());
            }
        }
        this.mTimer.setTimeout(TIME_OUT, this.mTimeoutProc);
        LogHelper.i(TAG, "authID: " + this.mOnAuthSyncAckInfo.mAuthID + ", waiting for receiveAck... ");
        return -1;
    }

    public void onStart() {
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(WifiProCommonUtils.HISTORY_TYPE_EMPTY, this, this.mRecAckListener);
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(WifiProCommonUtils.HISTORY_TYPE_EMPTY, this);
    }
}
