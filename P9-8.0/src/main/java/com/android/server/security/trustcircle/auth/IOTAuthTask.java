package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.InitAuthInfo;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RespPkInfo;
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
    private static final String TAG = IOTAuthTask.class.getSimpleName();
    EventListener mCancelListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != IOTAuthTask.this.mInitAuthInfo.mAuthID && authID != -2) {
                return false;
            }
            IOTAuthTask.this.-wrap0(2);
            return true;
        }
    };
    InitAuthInfo mInitAuthInfo;
    OnAuthAckInfo mOnAuthAckInfo;
    RecAuthAckInfo mRecInfo;
    RetCallback mReqAckCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            LogHelper.d(IOTAuthTask.TAG, child.getClass().getSimpleName() + "-->onTaskCallback, ret: " + ret);
            if (ret != 0) {
                IOTAuthTask.this.-wrap0(ret);
                return;
            }
            RequestAuthAckTask result = (RequestAuthAckTask) child;
            IOTAuthTask.this.mRecInfo = result.getRecAuthAckInfo();
            if (IOTAuthTask.this.mRecInfo.mAuthID == IOTAuthTask.this.mInitAuthInfo.mAuthID) {
                IOTAuthTask.this.mOnAuthAckInfo = AuthUtils.processRecAuthSyncAck(IOTAuthTask.this.mRecInfo);
                if (IOTAuthTask.this.mOnAuthAckInfo.mResult == 1) {
                    HwSecurityTaskThread.staticPushTask(new ServiceReqPkTask(IOTAuthTask.this, IOTAuthTask.this.mReqPkCallback, IOTAuthTask.this.mOnAuthAckInfo.mAuthID, 1000), 1);
                } else {
                    IOTAuthTask.this.-wrap0(IOTAuthTask.this.callIOTWithAuthResult(IOTAuthTask.this.mOnAuthAckInfo));
                }
            }
        }
    };
    RetCallback mReqPkCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0) {
                IOTAuthTask.this.-wrap0(ret);
            }
            RespPkInfo pkInfo = ((ServiceReqPkTask) child).getRespPkInfo();
            if (pkInfo != null && pkInfo.mAuthID == IOTAuthTask.this.mRecInfo.mAuthID) {
                IOTAuthTask.this.mOnAuthAckInfo = AuthUtils.processRecPkMaster(pkInfo);
                IAuthCallback callback = IOTController.getInstance().getIOTCallback(pkInfo.mAuthID, 1000);
                try {
                    if (IOTAuthTask.this.mOnAuthAckInfo.mResult != 0 || callback == null) {
                        if (callback != null) {
                            callback.onAuthAckError(IOTAuthTask.this.mOnAuthAckInfo.mAuthID, IOTAuthTask.this.mOnAuthAckInfo.mResult);
                            IOTAuthTask.this.-wrap0(3);
                        }
                    }
                    callback.onAuthAck(IOTAuthTask.this.mOnAuthAckInfo.mAuthID, IOTAuthTask.this.mOnAuthAckInfo.mResult, IOTAuthTask.this.mOnAuthAckInfo.mSessionKeyIV, IOTAuthTask.this.mOnAuthAckInfo.mSessionKey, IOTAuthTask.this.mOnAuthAckInfo.mMAC);
                    IOTAuthTask.this.-wrap0(0);
                } catch (RemoteException e) {
                    LogHelper.e(IOTAuthTask.TAG, e.toString());
                    IOTAuthTask.this.-wrap0(3);
                } catch (Exception e2) {
                    LogHelper.e(IOTAuthTask.TAG, e2.toString());
                    IOTAuthTask.this.-wrap0(3);
                }
            }
        }
    };

    public IOTAuthTask(HwSecurityTaskBase parent, RetCallback callback, InitAuthInfo info) {
        super(parent, callback);
        this.mInitAuthInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public int doAction() {
        LogHelper.d(TAG, "doAction, authID: " + this.mInitAuthInfo.mAuthID);
        HwSecurityTaskThread.staticPushTask(new RequestAuthAckTask(this, this.mReqAckCallback, AuthUtils.processAuthSync(this.mInitAuthInfo)), 1);
        return -1;
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
    }

    public long getAuthID() {
        return this.mInitAuthInfo.mAuthID;
    }

    private int callIOTWithAuthResult(OnAuthAckInfo onAuthAckInfo) {
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(onAuthAckInfo.mAuthID, 1000);
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
