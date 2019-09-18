package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class RequestRecAckTask extends HwSecurityTaskBase {
    /* access modifiers changed from: private */
    public static final String TAG = RequestRecAckTask.class.getSimpleName();
    private static final long TIME_OUT = 5000;
    HwSecurityTaskBase.EventListener mCancelListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID && authID != -2) {
                return false;
            }
            String access$100 = RequestRecAckTask.TAG;
            LogHelper.i(access$100, "onCancelAuthEvent, authID: " + authID);
            RequestRecAckTask.this.endWithResult(2);
            RequestRecAckTask.this.mTimer.cancel();
            return true;
        }
    };
    /* access modifiers changed from: private */
    public AuthPara.OnAuthSyncAckInfo mOnAuthSyncAckInfo;
    /* access modifiers changed from: private */
    public AuthPara.RecAckInfo mRecAckInfo;
    HwSecurityTaskBase.EventListener mRecAckListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            AuthPara.RecAckInfo unused = RequestRecAckTask.this.mRecAckInfo = ((ReceiveAckEv) ev).getRecAckInfo();
            String access$100 = RequestRecAckTask.TAG;
            LogHelper.i(access$100, "receiveAcEvent, authID: " + RequestRecAckTask.this.mRecAckInfo.mAuthID + ", mAuthID: " + RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID);
            if (RequestRecAckTask.this.mRecAckInfo.mAuthID != RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID) {
                return false;
            }
            RequestRecAckTask.this.mTimer.cancel();
            RequestRecAckTask.this.endWithResult(0);
            return true;
        }
    };
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            RequestRecAckTask.this.endWithResult(1);
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public RequestRecAckTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, AuthPara.OnAuthSyncAckInfo info) {
        super(parent, callback);
        this.mOnAuthSyncAckInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(103, this, this.mRecAckListener);
    }

    /* access modifiers changed from: package-private */
    public AuthPara.RecAckInfo getRecAckInfo() {
        return this.mRecAckInfo;
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mOnAuthSyncAckInfo.mAuthID, 1001);
        if (callback != null) {
            try {
                String str = TAG;
                LogHelper.i(str, "onAuthSyncAck, result: " + this.mOnAuthSyncAckInfo.mResult);
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
        String str2 = TAG;
        LogHelper.i(str2, "authID: " + this.mOnAuthSyncAckInfo.mAuthID + ", waiting for receiveAck... ");
        return -1;
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(103, this);
    }
}
