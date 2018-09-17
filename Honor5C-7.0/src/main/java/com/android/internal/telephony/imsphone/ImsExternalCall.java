package com.android.internal.telephony.imsphone;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Call.State;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import java.util.List;

public class ImsExternalCall extends Call {
    private Phone mPhone;

    public ImsExternalCall(Phone phone, ImsExternalConnection connection) {
        this.mPhone = phone;
        this.mConnections.add(connection);
    }

    public List<Connection> getConnections() {
        return this.mConnections;
    }

    public Phone getPhone() {
        return this.mPhone;
    }

    public boolean isMultiparty() {
        return false;
    }

    public void hangup() throws CallStateException {
    }

    public void setActive() {
        setState(State.ACTIVE);
    }

    public void setTerminated() {
        setState(State.DISCONNECTED);
    }
}
