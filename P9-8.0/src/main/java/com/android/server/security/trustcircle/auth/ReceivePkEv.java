package com.android.server.security.trustcircle.auth;

import com.android.server.security.trustcircle.auth.AuthPara.RespPkInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class ReceivePkEv extends HwSecurityEvent {
    private RespPkInfo mRespPkInfo;

    public ReceivePkEv(int evID, RespPkInfo info) {
        super(evID);
        this.mRespPkInfo = info;
    }

    RespPkInfo getPkInfo() {
        return this.mRespPkInfo;
    }
}
