package com.android.server.security.trustcircle;

import com.android.server.security.trustcircle.AuthPara.RecAckInfo;
import com.android.server.security.trustcircle.task.HwSecurityEvent;

public class ReceiveAckEv extends HwSecurityEvent {
    RecAckInfo mRecAckInfo;

    public ReceiveAckEv(int evID, RecAckInfo info) {
        super(evID);
        this.mRecAckInfo = info;
    }

    RecAckInfo getRecAckInfo() {
        return this.mRecAckInfo;
    }
}
