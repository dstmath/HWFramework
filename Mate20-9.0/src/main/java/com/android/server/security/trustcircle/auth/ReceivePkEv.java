package com.android.server.security.trustcircle.auth;

import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class ReceivePkEv extends HwSecurityEvent {
    private AuthPara.RespPkInfo mRespPkInfo;

    public ReceivePkEv(int evID, AuthPara.RespPkInfo info) {
        super(evID);
        this.mRespPkInfo = info;
    }

    /* access modifiers changed from: package-private */
    public AuthPara.RespPkInfo getPkInfo() {
        return this.mRespPkInfo;
    }
}
