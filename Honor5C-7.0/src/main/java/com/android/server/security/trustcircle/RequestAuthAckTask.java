package com.android.server.security.trustcircle;

import android.os.RemoteException;
import com.android.server.security.trustcircle.AuthPara.OnAuthSyncInfo;
import com.android.server.security.trustcircle.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.TimerOutProc;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.android.security.IAuthCallback;

public class RequestAuthAckTask extends HwSecurityTaskBase {
    private static final String TAG = null;
    private static final long TIME_OUT = 5000;
    EventListener mCancelListener;
    EventListener mIOTResListener;
    private OnAuthSyncInfo mOnAuthSyncInfo;
    private RecAuthAckInfo mRecAuthAckInfo;
    TimerOutProc mTimeoutProc;
    private HwSecurityTimerTask mTimer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.trustcircle.RequestAuthAckTask.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.trustcircle.RequestAuthAckTask.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.trustcircle.RequestAuthAckTask.<clinit>():void");
    }

    public RequestAuthAckTask(HwSecurityTaskBase parent, RetCallback callback, OnAuthSyncInfo info) {
        super(parent, callback);
        this.mCancelListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                long authID = ((CancelAuthEv) ev).getAuthID();
                if (authID != RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID && authID != -2) {
                    return false;
                }
                LogHelper.i(RequestAuthAckTask.TAG, "onCancelAuthEvent, authID: " + authID);
                RequestAuthAckTask.this.mTimer.cancel();
                RequestAuthAckTask.this.endWithResult(2);
                return true;
            }
        };
        this.mIOTResListener = new EventListener() {
            public boolean onEvent(HwSecurityEvent ev) {
                RequestAuthAckTask.this.mRecAuthAckInfo = ((AuthSyncAckEv) ev).getRecAuthAckInfo();
                LogHelper.i(RequestAuthAckTask.TAG, "onAuthSyncAckEvent, authID: " + RequestAuthAckTask.this.mRecAuthAckInfo.mAuthID + ", mAuthID: " + RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID);
                if (RequestAuthAckTask.this.mRecAuthAckInfo.mAuthID != RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID) {
                    return false;
                }
                RequestAuthAckTask.this.mTimer.cancel();
                RequestAuthAckTask.this.endWithResult(0);
                return true;
            }
        };
        this.mTimeoutProc = new TimerOutProc() {
            public void onTimerOut() {
                RequestAuthAckTask.this.endWithResult(1);
            }
        };
        this.mTimer = new HwSecurityTimerTask();
        this.mOnAuthSyncInfo = info;
    }

    RecAuthAckInfo getRecAuthAckInfo() {
        return this.mRecAuthAckInfo;
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mOnAuthSyncInfo.mAuthID, IOTController.TYPE_MASTER);
        if (callback != null) {
            try {
                if (this.mOnAuthSyncInfo.mResult == 0) {
                    callback.onAuthSync(this.mOnAuthSyncInfo.mAuthID, this.mOnAuthSyncInfo.mTcisId, this.mOnAuthSyncInfo.mIndexVersion, this.mOnAuthSyncInfo.mTAVersion, this.mOnAuthSyncInfo.mNonce, this.mOnAuthSyncInfo.mAuthKeyAlgoType, this.mOnAuthSyncInfo.mAuthKeyInfo, this.mOnAuthSyncInfo.mAuthKeyInfoSign);
                    LogHelper.i(TAG, "waiting for receiveAuthSyncAck...");
                    this.mTimer.setTimeout(TIME_OUT, this.mTimeoutProc);
                    return -1;
                }
                LogHelper.i(TAG, "onAuthError, authID: " + this.mOnAuthSyncInfo.mAuthID + "errorcode: " + this.mOnAuthSyncInfo.mResult);
                callback.onAuthError(this.mOnAuthSyncInfo.mAuthID, this.mOnAuthSyncInfo.mResult);
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.toString());
            } catch (Exception e2) {
                LogHelper.e(TAG, e2.toString());
            }
        }
        return 3;
    }

    public void onStart() {
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(WifiProCommonDefs.TYEP_HAS_INTERNET, this, this.mIOTResListener);
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(WifiProCommonDefs.TYEP_HAS_INTERNET, this);
    }
}
