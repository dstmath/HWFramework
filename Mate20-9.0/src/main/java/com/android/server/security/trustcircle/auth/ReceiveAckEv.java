package com.android.server.security.trustcircle.auth;

import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class ReceiveAckEv extends HwSecurityEvent {
    AuthPara.RecAckInfo mRecAckInfo;

    public ReceiveAckEv(int evID, AuthPara.RecAckInfo info) {
        super(evID);
        this.mRecAckInfo = info;
    }

    /* access modifiers changed from: package-private */
    public AuthPara.RecAckInfo getRecAckInfo() {
        return this.mRecAckInfo;
    }
}
