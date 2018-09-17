package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthSyncInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.TimerOutProc;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class RequestAuthAckTask extends HwSecurityTaskBase {
    private static final String TAG = RequestAuthAckTask.class.getSimpleName();
    private static final long TIME_OUT = 5000;
    EventListener mCancelListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID && authID != -2) {
                return false;
            }
            LogHelper.i(RequestAuthAckTask.TAG, "onCancelAuthEvent, authID: " + authID);
            RequestAuthAckTask.this.mTimer.cancel();
            RequestAuthAckTask.this.-wrap0(2);
            return true;
        }
    };
    EventListener mIOTResListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            RequestAuthAckTask.this.mRecAuthAckInfo = ((AuthSyncAckEv) ev).getRecAuthAckInfo();
            LogHelper.i(RequestAuthAckTask.TAG, "onAuthSyncAckEvent, authID: " + RequestAuthAckTask.this.mRecAuthAckInfo.mAuthID + ", mAuthID: " + RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID);
            if (RequestAuthAckTask.this.mRecAuthAckInfo.mAuthID != RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID) {
                return false;
            }
            RequestAuthAckTask.this.mTimer.cancel();
            RequestAuthAckTask.this.-wrap0(0);
            return true;
        }
    };
    private OnAuthSyncInfo mOnAuthSyncInfo;
    private RecAuthAckInfo mRecAuthAckInfo;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            RequestAuthAckTask.this.-wrap0(1);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public RequestAuthAckTask(HwSecurityTaskBase parent, RetCallback callback, OnAuthSyncInfo info) {
        super(parent, callback);
        this.mOnAuthSyncInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(101, this, this.mIOTResListener);
    }

    RecAuthAckInfo getRecAuthAckInfo() {
        return this.mRecAuthAckInfo;
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mOnAuthSyncInfo.mAuthID, 1000);
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

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(101, this);
    }
}
