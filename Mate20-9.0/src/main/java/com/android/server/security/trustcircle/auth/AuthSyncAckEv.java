package com.android.server.security.trustcircle.auth;

import com.android.server.security.trustcircle.auth.AuthPara;
import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class AuthSyncAckEv extends HwSecurityEvent {
    AuthPara.RecAuthAckInfo mRecAuthAckInfo;

    public AuthSyncAckEv(int evID, AuthPara.RecAuthAckInfo info) {
        super(evID);
        this.mRecAuthAckInfo = info;
    }

    /* access modifiers changed from: package-private */
    public AuthPara.RecAuthAckInfo getRecAuthAckInfo() {
        return this.mRecAuthAckInfo;
    }
}
