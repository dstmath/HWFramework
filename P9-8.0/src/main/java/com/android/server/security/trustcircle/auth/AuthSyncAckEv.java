package com.android.server.security.trustcircle.auth;

import com.android.server.security.trustcircle.auth.AuthPara.RecAuthAckInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class AuthSyncAckEv extends HwSecurityEvent {
    RecAuthAckInfo mRecAuthAckInfo;

    public AuthSyncAckEv(int evID, RecAuthAckInfo info) {
        super(evID);
        this.mRecAuthAckInfo = info;
    }

    RecAuthAckInfo getRecAuthAckInfo() {
        return this.mRecAuthAckInfo;
    }
}
