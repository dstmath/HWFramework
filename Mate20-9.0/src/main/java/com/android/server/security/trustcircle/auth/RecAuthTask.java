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

public class RecAuthTask extends HwSecurityTaskBase {
    /* access modifiers changed from: private */
    public static final String TAG = RecAuthTask.class.getSimpleName();
    HwSecurityTaskBase.EventListener mCancelListener = new HwSecurityTaskBase.EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != RecAuthTask.this.mRecAuthInfo.mAuthID && authID != -2) {
                return false;
            }
            RecAuthTask.this.endWithResult(2);
            return true;
        }
    };
    AuthPara.RecAuthInfo mRecAuthInfo;
    HwSecurityTaskBase.RetCallback mReqAckCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            int i = ret;
            if (i != 0) {
                RecAuthTask.this.endWithResult(i);
                return;
            }
            AuthPara.RecAckInfo ackInfo = ((RequestRecAckTask) child).getRecAckInfo();
            if (ackInfo.mAuthID == RecAuthTask.this.mRecAuthInfo.mAuthID) {
                AuthPara.OnAuthAckInfo onAuthAckInfo = AuthUtils.processAckRec(ackInfo);
                IAuthCallback callback = IOTController.getInstance().getIOTCallback(ackInfo.mAuthID, 1001);
                if (callback != null) {
                    try {
                        if (onAuthAckInfo.mResult == 0) {
                            callback.onAuthAck(onAuthAckInfo.mAuthID, onAuthAckInfo.mResult, onAuthAckInfo.mSessionKeyIV, onAuthAckInfo.mSessionKey, new byte[0]);
                        } else {
                            callback.onAuthAckError(ackInfo.mAuthID, onAuthAckInfo.mResult);
                        }
                    } catch (RemoteException e) {
                        LogHelper.e(RecAuthTask.TAG, e.toString());
                    } catch (Exception e2) {
                        LogHelper.e(RecAuthTask.TAG, e2.toString());
                    }
                }
                RecAuthTask.this.endWithResult(i);
            }
        }
    };
    HwSecurityTaskBase.RetCallback mReqPkCallback = new HwSecurityTaskBase.RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0) {
                RecAuthTask.this.endWithResult(ret);
                return;
            }
            AuthPara.RespPkInfo pkInfo = ((ServiceReqPkTask) child).getRespPkInfo();
            if (pkInfo.mAuthID == RecAuthTask.this.mRecAuthInfo.mAuthID) {
                AuthPara.OnAuthSyncAckInfo info = AuthUtils.processRecPkSlave(pkInfo);
                if (info.mResult == 0) {
                    HwSecurityTaskThread.staticPushTask(new RequestRecAckTask(RecAuthTask.this, RecAuthTask.this.mReqAckCallback, info), 1);
                    return;
                }
                IAuthCallback callback = IOTController.getInstance().getIOTCallback(info.mAuthID, 1001);
                if (callback != null) {
                    try {
                        callback.onAuthSyncAckError(info.mAuthID, info.mResult);
                    } catch (RemoteException e) {
                        LogHelper.e(RecAuthTask.TAG, e.toString());
                    } catch (Exception e2) {
                        LogHelper.e(RecAuthTask.TAG, e2.toString());
                    }
                }
                RecAuthTask.this.endWithResult(ret);
            }
        }
    };

    public RecAuthTask(HwSecurityTaskBase parent, HwSecurityTaskBase.RetCallback callback, AuthPara.RecAuthInfo info) {
        super(parent, callback);
        this.mRecAuthInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public int doAction() {
        String str = TAG;
        LogHelper.d(str, "doAction, authID: " + this.mRecAuthInfo.mAuthID);
        AuthPara.OnAuthSyncAckInfo info = AuthUtils.processAuthSyncRec(this.mRecAuthInfo);
        if (info.mResult == 1) {
            ServiceReqPkTask serviceReqPkTask = new ServiceReqPkTask(this, this.mReqPkCallback, info.mAuthID, 1001);
            HwSecurityTaskThread.staticPushTask(serviceReqPkTask, 1);
            return -1;
        } else if (info.mResult == 0) {
            HwSecurityTaskThread.staticPushTask(new RequestRecAckTask(this, this.mReqAckCallback, info), 1);
            return -1;
        } else {
            IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mRecAuthInfo.mAuthID, 1001);
            if (callback != null) {
                try {
                    callback.onAuthSyncAckError(this.mRecAuthInfo.mAuthID, info.mResult);
                } catch (RemoteException e) {
                    LogHelper.e(TAG, e.toString());
                } catch (Exception e2) {
                    LogHelper.e(TAG, e2.toString());
                }
            }
            return 3;
        }
    }

    public void onStop() {
        HwSecurityMsgCenter.staticUnregisterEvent(100, this);
    }

    public long getAuthID() {
        return this.mRecAuthInfo.mAuthID;
    }
}
