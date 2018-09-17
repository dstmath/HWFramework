package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.TimerOutProc;
import com.android.server.security.trustcircle.task.HwSecurityTimerTask;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class ServiceReqPkTask extends HwSecurityTaskBase {
    private static final String TAG = ServiceReqPkTask.class.getSimpleName();
    private static final long TIME_OUT = 5000;
    private long mAuthID;
    EventListener mCancelListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != ServiceReqPkTask.this.mAuthID && authID != -2) {
                return false;
            }
            LogHelper.i(ServiceReqPkTask.TAG, "onCancelAuthEvent, authID: " + authID);
            ServiceReqPkTask.this.mTimer.cancel();
            ServiceReqPkTask.this.-wrap0(2);
            return true;
        }
    };
    EventListener mRecPkListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            ServiceReqPkTask.this.mRespPkInfo = ((ReceivePkEv) ev).getPkInfo();
            LogHelper.i(ServiceReqPkTask.TAG, "onReceivePkEvent, authID: " + ServiceReqPkTask.this.mRespPkInfo.mAuthID + ", mAuthID: " + ServiceReqPkTask.this.mAuthID);
            if (ServiceReqPkTask.this.mAuthID != ServiceReqPkTask.this.mRespPkInfo.mAuthID) {
                return false;
            }
            ServiceReqPkTask.this.mTimer.cancel();
            ServiceReqPkTask.this.-wrap0(0);
            return true;
        }
    };
    private RespPkInfo mRespPkInfo;
    TimerOutProc mTimeoutProc = new TimerOutProc() {
        public void onTimerOut() {
            ServiceReqPkTask.this.-wrap0(1);
        }
    };
    private HwSecurityTimerTask mTimer = new HwSecurityTimerTask();
    private int mType;

    public ServiceReqPkTask(HwSecurityTaskBase parent, RetCallback callback, long authID, int type) {
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

    public RespPkInfo getRespPkInfo() {
        return this.mRespPkInfo;
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
        HwSecurityMsgCenter.staticUnregisterEvent(102, this);
    }
}
