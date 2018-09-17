package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthSyncAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.TimerOutProc;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class RequestRecAckTask extends HwSecurityTaskBase {
    private static final String TAG = RequestRecAckTask.class.getSimpleName();
    private static final long TIME_OUT = 5000;
    EventListener mCancelListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID && authID != -2) {
                return false;
            }
            LogHelper.i(RequestRecAckTask.TAG, "onCancelAuthEvent, authID: " + authID);
            RequestRecAckTask.this.-wrap0(2);
            RequestRecAckTask.this.mTimer.cancel();
            return true;
        }
    };
    private OnAuthSyncAckInfo mOnAuthSyncAckInfo;
    private RecAckInfo mRecAckInfo;
    EventListener mRecAckListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            RequestRecAckTask.this.mRecAckInfo = ((ReceiveAckEv) ev).getRecAckInfo();
            LogHelper.i(RequestRecAckTask.TAG, "receiveAcEvent, authID: " + RequestRecAckTask.this.mRecAckInfo.mAuthID + ", mAuthID: " + RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID);
            if (RequestRecAckTask.this.mRecAckInfo.mAuthID != RequestRecAckTask.this.mOnAuthSyncAckInfo.mAuthID) {
                return false;
            }
            RequestRecAckTask.this.mTimer.cancel();
            RequestRecAckTask.this.-wrap0(0);
            return true;
        }
    };
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            RequestRecAckTask.this.-wrap0(1);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public RequestRecAckTask(HwSecurityTaskBase parent, RetCallback callback, OnAuthSyncAckInfo info) {
        super(parent, callback);
        this.mOnAuthSyncAckInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(103, this, this.mRecAckListener);
    }

    RecAckInfo getRecAckInfo() {
        return this.mRecAckInfo;
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mOnAuthSyncAckInfo.mAuthID, 1001);
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

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(103, this);
    }
}
