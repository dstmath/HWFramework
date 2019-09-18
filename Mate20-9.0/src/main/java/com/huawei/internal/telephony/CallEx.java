package com.huawei.internal.telephony;

import android.os.Bundle;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;
import java.util.ArrayList;
import java.util.List;

public class CallEx {
    private Call mCall;

    public CallEx(Call call) {
        this.mCall = call;
    }

    public static final boolean updateRcsPreCallInfo(android.telecom.Call call, Bundle extras) {
        return call.updateRcsPreCallInfo(extras);
    }

    public static final boolean isActiveSub(android.telecom.Call call) {
        return call.mIsActiveSub;
    }

    public PhoneEx getPhone() {
        if (this.mCall == null) {
            return null;
        }
        return new PhoneEx(this.mCall.getPhone());
    }

    public boolean isActive() {
        boolean z = false;
        if (this.mCall == null) {
            return false;
        }
        if (this.mCall.getState() == Call.State.ACTIVE) {
            z = true;
        }
        return z;
    }

    public boolean isMultiparty() {
        if (this.mCall == null) {
            return false;
        }
        return this.mCall.isMultiparty();
    }

    public List<ConnectionEx> getConnections() {
        if (this.mCall == null || this.mCall.getConnections() == null) {
            return null;
        }
        List<ConnectionEx> connectionList = new ArrayList<>();
        int connectionSize = this.mCall.getConnections().size();
        for (int i = 0; i < connectionSize; i++) {
            connectionList.add(new ConnectionEx((Connection) this.mCall.getConnections().get(i)));
        }
        return connectionList;
    }
}
