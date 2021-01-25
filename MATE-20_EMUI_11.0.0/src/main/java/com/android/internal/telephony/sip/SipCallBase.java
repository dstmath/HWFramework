package com.android.internal.telephony.sip;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import java.util.List;

/* access modifiers changed from: package-private */
public abstract class SipCallBase extends Call {
    SipCallBase() {
    }

    @Override // com.android.internal.telephony.Call
    public List<Connection> getConnections() {
        return this.mConnections;
    }

    @Override // com.android.internal.telephony.Call
    public boolean isMultiparty() {
        return this.mConnections.size() > 1;
    }

    public String toString() {
        return this.mState.toString() + ":" + super.toString();
    }
}
