package com.android.internal.telephony.cdma;

import com.android.internal.telephony.AbstractGsmCdmaConnection;
import com.android.internal.telephony.GsmCdmaConnection;
import com.huawei.android.telephony.RlogEx;

public class HwCdmaConnectionReferenceImpl implements AbstractGsmCdmaConnection.HwCdmaConnectionReference {
    private static final String LOG_TAG = "HwCdmaConnectionReferenceImpl";
    boolean hasRevFWIM = false;
    boolean isEncryptCall = false;
    private GsmCdmaConnection mCdmaConnection;

    public HwCdmaConnectionReferenceImpl(GsmCdmaConnection connection) {
        this.mCdmaConnection = connection;
    }

    public void onLineControlInfo() {
        this.hasRevFWIM = true;
        RlogEx.i(LOG_TAG, "Do not reset call duration");
    }

    public boolean hasRevFWIM() {
        return this.hasRevFWIM;
    }

    public boolean isEncryptCall() {
        return this.isEncryptCall;
    }

    public void setEncryptCall(boolean isEncryptCall2) {
        RlogEx.i(LOG_TAG, " isMT:" + this.mCdmaConnection.isIncoming() + " be set EncryptCall");
        this.isEncryptCall = isEncryptCall2;
    }

    public boolean compareToNumber(String number) {
        return number != null && number.equals(getRemoteNumber());
    }

    private String getRemoteNumber() {
        if (this.mCdmaConnection.isIncoming()) {
            return this.mCdmaConnection.getAddress();
        }
        return this.mCdmaConnection.getOrigDialString();
    }
}
