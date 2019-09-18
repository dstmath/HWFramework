package com.android.internal.telephony.sip;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import java.util.List;

abstract class SipCallBase extends Call {
    SipCallBase() {
    }

    public List<Connection> getConnections() {
        return this.mConnections;
    }

    public boolean isMultiparty() {
        return this.mConnections.size() > 1;
    }

    public String toString() {
        return this.mState.toString() + ":" + super.toString();
    }
}
