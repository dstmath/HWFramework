package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.OnAuthSyncAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RecAuthInfo;
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

public class RecAuthTask extends HwSecurityTaskBase {
    private static final String TAG = RecAuthTask.class.getSimpleName();
    EventListener mCancelListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != RecAuthTask.this.mRecAuthInfo.mAuthID && authID != -2) {
                return false;
            }
            RecAuthTask.this.-wrap0(2);
            return true;
        }
    };
    RecAuthInfo mRecAuthInfo;
    RetCallback mReqAckCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0) {
                RecAuthTask.this.-wrap0(ret);
                return;
            }
            RecAckInfo ackInfo = ((RequestRecAckTask) child).getRecAckInfo();
            if (ackInfo.mAuthID == RecAuthTask.this.mRecAuthInfo.mAuthID) {
                OnAuthAckInfo onAuthAckInfo = AuthUtils.processAckRec(ackInfo);
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
                RecAuthTask.this.-wrap0(ret);
            }
        }
    };
    RetCallback mReqPkCallback = new RetCallback() {
        public void onTaskCallback(HwSecurityTaskBase child, int ret) {
            if (ret != 0) {
                RecAuthTask.this.-wrap0(ret);
                return;
            }
            RespPkInfo pkInfo = ((ServiceReqPkTask) child).getRespPkInfo();
            if (pkInfo.mAuthID == RecAuthTask.this.mRecAuthInfo.mAuthID) {
                OnAuthSyncAckInfo info = AuthUtils.processRecPkSlave(pkInfo);
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
                RecAuthTask.this.-wrap0(ret);
            }
        }
    };

    public RecAuthTask(HwSecurityTaskBase parent, RetCallback callback, RecAuthInfo info) {
        super(parent, callback);
        this.mRecAuthInfo = info;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public int doAction() {
        LogHelper.d(TAG, "doAction, authID: " + this.mRecAuthInfo.mAuthID);
        OnAuthSyncAckInfo info = AuthUtils.processAuthSyncRec(this.mRecAuthInfo);
        if (info.mResult == 1) {
            HwSecurityTaskThread.staticPushTask(new ServiceReqPkTask(this, this.mReqPkCallback, info.mAuthID, 1001), 1);
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
