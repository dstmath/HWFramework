package com.android.server.security.trustcircle.auth;

import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.AuthPara.ReqPkInfo;
import com.android.server.security.trustcircle.auth.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;
import com.android.server.security.trustcircle.task.HwSecurityMsgCenter;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.EventListener;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.IAuthCallback;

public class IOTReqPkTask extends HwSecurityTaskBase {
    private static final String TAG = IOTReqPkTask.class.getSimpleName();
    EventListener mCancelListener = new EventListener() {
        public boolean onEvent(HwSecurityEvent ev) {
            long authID = ((CancelAuthEv) ev).getAuthID();
            if (authID != IOTReqPkTask.this.mReqPkInfo.mAuthID && authID != -2) {
                return false;
            }
            LogHelper.i(IOTReqPkTask.TAG, "onCancelAuthEvent, authID: " + authID);
            IOTReqPkTask.this.-wrap0(2);
            return true;
        }
    };
    private ReqPkInfo mReqPkInfo;
    private int mType;

    public IOTReqPkTask(HwSecurityTaskBase parent, RetCallback callback, ReqPkInfo reqPkInfo, int type) {
        super(parent, callback);
        this.mReqPkInfo = reqPkInfo;
        this.mType = type;
        HwSecurityMsgCenter.staticRegisterEvent(100, this, this.mCancelListener);
    }

    public int doAction() {
        RespPkInfo pkInfo = AuthUtils.processGetPk(this.mReqPkInfo);
        IAuthCallback callback = IOTController.getInstance().getIOTCallback(this.mReqPkInfo.mAuthID, this.mType);
        if (callback != null) {
            try {
                callback.responsePK(pkInfo.mAuthID, pkInfo.mAuthKeyAlgoType, pkInfo.mAuthKeyData, pkInfo.mAuthKeyDataSign);
                return 0;
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
    }
}
