package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskThread;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class IOTAuthTask extends HwSecurityTaskBase {
    /* access modifiers changed from: private */
    public static final String TAG = IOTAuthTask.class.getSimpleName();
    HwSecurityTaskBase.EventListener mCancelListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != IOTAuthTask.this.mInitAuthInfo.mAuthID && authID != -2) {
                return false;
            }
            IOTAuthTask.this.endWithResult(2);
            return true;
        }
    };
    AuthPara.InitAuthInfo mInitAuthInfo;
    AuthPara.OnAuthAckInfo mOnAuthAckInfo;
    AuthPara.RecAuthAckInfo mRecInfo;
    HwSecurityTaskBase.RetCallback mReqAckCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            String access$400 = IOTAuthTask.TAG;
            LogHelper.d(access$400, child.getClass().getSimpleName() + "-->onTaskCallback, ret: " + ret);
            if (ret != 0) {
                IOTAuthTask.this.endWithResult(ret);
                return;
            }
            IOTAuthTask.this.mRecInfo = ((RequestAuthAckTask) child).getRecAuthAckInfo();
            if (IOTAuthTask.this.mRecInfo.mAuthID == IOTAuthTask.this.mInitAuthInfo.mAuthID) {
                IOTAuthTask.this.mOnAuthAckInfo = AuthUtils.processRecAuthSyncAck(IOTAuthTask.this.mRecInfo);
                if (IOTAuthTask.this.mOnAuthAckInfo.mResult == 1) {
                    ServiceReqPkTask serviceReqPkTask = new ServiceReqPkTask(IOTAuthTask.this, IOTAuthTask.this.mReqPkCallback, IOTAuthTask.this.mOnAuthAckInfo.mAuthID, 1000);
                    HwSecurityTaskThread.staticPushTask(serviceReqPkTask, 1);
                } else {
                    IOTAuthTask.this.endWithResult(IOTAuthTask.this.callIOTWithAuthResult(IOTAuthTask.this.mOnAuthAckInfo));
                }
            }
        }
    };
    HwSecurityTaskBase.RetCallback mReqPkCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0) {
                IOTAuthTask.this.endWithResult(ret);
            }
            AuthPara.RespPkInfo pkInfo = ((ServiceReqPkTask) child).getRespPkInfo();
            if (pkInfo != null && pkInfo.mAuthID == IOTAuthTask.this.mRecInfo.mAuthID) {
                IOTAuthTask.this.mOnAuthAckInfo = AuthUtils.processRecPkMaster(pkInfo);
                IAuthCallback callback = IOTController.getInstance().getIOTCallback(pkInfo.mAuthID, 1000);
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

    public IOTAuthTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, AuthPara.InitAuthInfo info) {
        super(parent, callback);
        this.mInitAuthInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public int doAction() {
        String str = TAG;
        LogHelper.d(str, "doAction, authID: " + this.mInitAuthInfo.mAuthID);
        HwSecurityTaskThread.staticPushTask(new RequestAuthAckTask(this, this.mReqAckCallback, AuthUtils.processAuthSync(this.mInitAuthInfo)), 1);
        return -1;
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
    }

    public long getAuthID() {
        return this.mInitAuthInfo.mAuthID;
    }

    /* access modifiers changed from: private */
    public int callIOTWithAuthResult(AuthPara.OnAuthAckInfo onAuthAckInfo) {
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
