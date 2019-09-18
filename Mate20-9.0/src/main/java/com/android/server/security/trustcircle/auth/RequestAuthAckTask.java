package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class RequestAuthAckTask extends HwSecurityTaskBase {
    /* access modifiers changed from: private */
    public static final String TAG = RequestAuthAckTask.class.getSimpleName();
    private static final long TIME_OUT = 5000;
    HwSecurityTaskBase.EventListener mCancelListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID && authID != -2) {
                return false;
            }
            String access$100 = RequestAuthAckTask.TAG;
            LogHelper.i(access$100, "onCancelAuthEvent, authID: " + authID);
            RequestAuthAckTask.this.mTimer.cancel();
            RequestAuthAckTask.this.endWithResult(2);
            return true;
        }
    };
    HwSecurityTaskBase.EventListener mIOTResListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            AuthPara.RecAuthAckInfo unused = RequestAuthAckTask.this.mRecAuthAckInfo = ((AuthSyncAckEv) ev).getRecAuthAckInfo();
            String access$100 = RequestAuthAckTask.TAG;
            LogHelper.i(access$100, "onAuthSyncAckEvent, authID: " + RequestAuthAckTask.this.mRecAuthAckInfo.mAuthID + ", mAuthID: " + RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID);
            if (RequestAuthAckTask.this.mRecAuthAckInfo.mAuthID != RequestAuthAckTask.this.mOnAuthSyncInfo.mAuthID) {
                return false;
            }
            RequestAuthAckTask.this.mTimer.cancel();
            RequestAuthAckTask.this.endWithResult(0);
            return true;
        }
    };
    /* access modifiers changed from: private */
    public AuthPara.OnAuthSyncInfo mOnAuthSyncInfo;
    /* access modifiers changed from: private */
    public AuthPara.RecAuthAckInfo mRecAuthAckInfo;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            RequestAuthAckTask.this.endWithResult(1);
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTimerTask mTimer = new HwSecurityTimerTask();

    public RequestAuthAckTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, AuthPara.OnAuthSyncInfo info) {
        super(parent, callback);
        this.mOnAuthSyncInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(101, this, this.mIOTResListener);
    }

    /* access modifiers changed from: package-private */
    public AuthPara.RecAuthAckInfo getRecAuthAckInfo() {
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
                String str = TAG;
                LogHelper.i(str, "onAuthError, authID: " + this.mOnAuthSyncInfo.mAuthID + "errorcode: " + this.mOnAuthSyncInfo.mResult);
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
