package com.android.internal.telephony.cdma;

import android.os.SystemClock;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractGsmCdmaConnection.HwCdmaConnectionReference;
import com.android.internal.telephony.ConnectionUtils;
import com.android.internal.telephony.GsmCdmaConnection;

public class HwCdmaConnectionReferenceImpl implements HwCdmaConnectionReference {
    private static final String LOG_TAG = "HwCdmaConnectionReferenceImpl";
    boolean hasRevFWIM = false;
    boolean isEncryptCall = false;
    private GsmCdmaConnection mCdmaConnection;

    public HwCdmaConnectionReferenceImpl(GsmCdmaConnection connection) {
        this.mCdmaConnection = connection;
    }

    public void onLineControlInfo() {
        ConnectionUtils.setConnectTime(this.mCdmaConnection, System.currentTimeMillis());
        ConnectionUtils.setConnectTimeReal(this.mCdmaConnection, SystemClock.elapsedRealtime());
        ConnectionUtils.setDuration(this.mCdmaConnection, 0);
        this.hasRevFWIM = true;
        Rlog.d(LOG_TAG, "Reset call duration");
    }

    public boolean hasRevFWIM() {
        return this.hasRevFWIM;
    }

    public boolean isEncryptCall() {
        return this.isEncryptCall;
    }

    public void setEncryptCall(boolean isEncryptCall) {
        Rlog.d(LOG_TAG, " isMT:" + this.mCdmaConnection.isIncoming() + " be set EncryptCall!!!");
        this.isEncryptCall = isEncryptCall;
    }

    public boolean compareToNumber(String number) {
        return number != null ? number.equals(getRemoteNumber()) : false;
    }

    private String getRemoteNumber() {
        if (this.mCdmaConnection.isIncoming()) {
            return this.mCdmaConnection.getAddress();
        }
        return this.mCdmaConnection.getOrigDialString();
    }
}
