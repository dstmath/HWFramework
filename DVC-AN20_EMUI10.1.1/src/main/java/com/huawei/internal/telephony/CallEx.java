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
        Call call = this.mCall;
        if (call == null) {
            return null;
        }
        return new PhoneEx(call.getPhone());
    }

    public boolean isActive() {
        Call call = this.mCall;
        if (call != null && call.getState() == Call.State.ACTIVE) {
            return true;
        }
        return false;
    }

    public boolean isMultiparty() {
        Call call = this.mCall;
        if (call == null) {
            return false;
        }
        return call.isMultiparty();
    }

    public List<ConnectionEx> getConnections() {
        Call call = this.mCall;
        if (call == null || call.getConnections() == null) {
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
