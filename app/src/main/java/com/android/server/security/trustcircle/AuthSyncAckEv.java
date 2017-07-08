package com.android.server.security.trustcircle;

import com.android.server.security.trustcircle.AuthPara.RecAuthAckInfo;
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
