package com.huawei.internal.telephony;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Connection;

public class ConnectionEx {
    private Connection mConnection;

    public enum DisconnectCause {
        NOT_DISCONNECTED,
        INCOMING_MISSED,
        NORMAL,
        LOCAL,
        BUSY,
        CONGESTION,
        MMI,
        INVALID_NUMBER,
        NUMBER_UNREACHABLE,
        SERVER_UNREACHABLE,
        INVALID_CREDENTIALS,
        OUT_OF_NETWORK,
        SERVER_ERROR,
        TIMED_OUT,
        LOST_SIGNAL,
        LIMIT_EXCEEDED,
        INCOMING_REJECTED,
        POWER_OFF,
        OUT_OF_SERVICE,
        ICC_ERROR,
        CALL_BARRED,
        FDN_BLOCKED,
        CS_RESTRICTED,
        CS_RESTRICTED_NORMAL,
        CS_RESTRICTED_EMERGENCY,
        UNOBTAINABLE_NUMBER,
        DIAL_MODIFIED_TO_USSD,
        DIAL_MODIFIED_TO_SS,
        DIAL_MODIFIED_TO_DIAL,
        CDMA_LOCKED_UNTIL_POWER_CYCLE,
        CDMA_DROP,
        CDMA_INTERCEPT,
        CDMA_REORDER,
        CDMA_SO_REJECT,
        CDMA_RETRY_ORDER,
        CDMA_ACCESS_FAILURE,
        CDMA_PREEMPTED,
        CDMA_NOT_EMERGENCY,
        CDMA_ACCESS_BLOCKED,
        ERROR_UNSPECIFIED,
        EMERGENCY_TEMP_FAILURE,
        EMERGENCY_PERM_FAILURE,
        SRVCC_CALL_DROP,
        CALL_FAIL_MISC
    }

    public ConnectionEx(Connection connection) {
        this.mConnection = connection;
    }

    public static ConnectionEx from(Object obj) {
        if (obj == null || !(obj instanceof Connection)) {
            return null;
        }
        return new ConnectionEx((Connection) obj);
    }

    public long getCreateTime() {
        if (this.mConnection == null) {
            return 0;
        }
        return this.mConnection.getCreateTime();
    }

    public long getDisconnectTime() {
        if (this.mConnection == null) {
            return 0;
        }
        return this.mConnection.getDisconnectTime();
    }

    public boolean isActive() {
        boolean z = false;
        if (this.mConnection == null) {
            return false;
        }
        if (Call.State.ACTIVE == this.mConnection.getState()) {
            z = true;
        }
        return z;
    }

    public String getAddress() {
        if (this.mConnection == null) {
            return null;
        }
        return this.mConnection.getAddress();
    }

    public Object getUserData() {
        if (this.mConnection == null) {
            return null;
        }
        return this.mConnection.getUserData();
    }

    public void setUserData(Object userData) {
        if (this.mConnection != null) {
            this.mConnection.setUserData(userData);
        }
    }
}
