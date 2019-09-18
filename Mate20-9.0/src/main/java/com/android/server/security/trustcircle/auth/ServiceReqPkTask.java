package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class ServiceReqPkTask extends HwSecurityTaskBase {
    /* access modifiers changed from: private */
    public static final String TAG = ServiceReqPkTask.class.getSimpleName();
    private static final long TIME_OUT = 5000;
    /* access modifiers changed from: private */
    public long mAuthID;
    HwSecurityTaskBase.EventListener mCancelListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != ServiceReqPkTask.this.mAuthID && authID != -2) {
                return false;
            }
            String access$100 = ServiceReqPkTask.TAG;
            LogHelper.i(access$100, "onCancelAuthEvent, authID: " + authID);
            ServiceReqPkTask.this.mTimer.cancel();
            ServiceReqPkTask.this.endWithResult(2);
            return true;
        }
    };
    HwSecurityTaskBase.EventListener mRecPkListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            AuthPara.RespPkInfo unused = ServiceReqPkTask.this.mRespPkInfo = ((ReceivePkEv) ev).getPkInfo();
            String access$100 = ServiceReqPkTask.TAG;
            LogHelper.i(access$100, "onReceivePkEvent, authID: " + ServiceReqPkTask.this.mRespPkInfo.mAuthID + ", mAuthID: " + ServiceReqPkTask.this.mAuthID);
            if (ServiceReqPkTask.this.mAuthID != ServiceReqPkTask.this.mRespPkInfo.mAuthID) {
                return false;
            }
            ServiceReqPkTask.this.mTimer.cancel();
            ServiceReqPkTask.this.endWithResult(0);
            return true;
        }
    };
    /* access modifiers changed from: private */
    public AuthPara.RespPkInfo mRespPkInfo;
    HwSecurityTaskBase.TimerOutProc mTimeoutProc = new HwSecurityTaskBase.TimerOutProc() {
        public void onTimerOut() {
            ServiceReqPkTask.this.endWithResult(1);
        }
    };
    /* access modifiers changed from: private */
    public HwSecurityTimerTask mTimer = new HwSecurityTimerTask();
    private int mType;

    public ServiceReqPkTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, long authID, int type) {
        super(parent, callback);
        this.mAuthID = authID;
        this.mType = type;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
        HwSecurityMsgCenter.staticRegisterEvent(102, this, this.mRecPkListener);
    }

    public int doAction() {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mAuthID, this.mType);
        if (callback != null) {
            try {
                callback.requestPK();
                LogHelper.i(TAG, "waiting for receivePk...");
                this.mTimer.setTimeout(TIME_OUT, this.mTimeoutProc);
                return -1;
            } catch (RemoteException e) {
                LogHelper.e(TAG, e.toString());
            } catch (Exception e2) {
                LogHelper.e(TAG, e2.toString());
            }
        }
        return 3;
    }

    public AuthPara.RespPkInfo getRespPkInfo() {
        return this.mRespPkInfo;
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(102, this);
    }
}
