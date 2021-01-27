package com.android.internal.telephony;

import com.huawei.android.telephony.RlogEx;

public class HwGsmCdmaConnectionEx extends DefaultHwGsmCdmaConnectionEx {
    private static final String LOG_TAG = "HwGsmCdmaConnectionEx";
    private boolean hasRevFWIM = false;
    private boolean isEncryptCall = false;
    IGsmCdmaConnectionInner mGsmCdmaConnection = null;

    public HwGsmCdmaConnectionEx() {
    }

    public HwGsmCdmaConnectionEx(IGsmCdmaConnectionInner gsmCdmaConnection) {
        this.mGsmCdmaConnection = gsmCdmaConnection;
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
        RlogEx.i(LOG_TAG, " isMT:" + this.mGsmCdmaConnection.isIncoming() + " be set EncryptCall");
        this.isEncryptCall = isEncryptCall2;
    }

    public boolean compareToNumber(String number) {
        return number != null && number.equals(getRemoteNumber());
    }

    private String getRemoteNumber() {
        if (this.mGsmCdmaConnection.isIncoming()) {
            return this.mGsmCdmaConnection.getAddress();
        }
        return this.mGsmCdmaConnection.getOrigDialString();
    }
}
