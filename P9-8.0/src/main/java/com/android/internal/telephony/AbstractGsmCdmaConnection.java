package com.android.internal.telephony;

public abstract class AbstractGsmCdmaConnection extends Connection {
    private static final String LOG_TAG = "AbstractGsmCdmaConnection";
    private HwCdmaConnectionReference mCdmaReference = HwTelephonyFactory.getHwPhoneManager().createHwCdmaConnectionReference(this);

    public interface HwCdmaConnectionReference {
        boolean compareToNumber(String str);

        boolean hasRevFWIM();

        boolean isEncryptCall();

        void onLineControlInfo();

        void setEncryptCall(boolean z);
    }

    public AbstractGsmCdmaConnection(int phoneType) {
        super(phoneType);
    }

    public void onLineControlInfo() {
        this.mCdmaReference.onLineControlInfo();
    }

    public boolean hasRevFWIM() {
        return this.mCdmaReference.hasRevFWIM();
    }

    public boolean isEncryptCall() {
        return this.mCdmaReference.isEncryptCall();
    }

    public void setEncryptCall(boolean isEncryptCall) {
        this.mCdmaReference.setEncryptCall(isEncryptCall);
    }

    public boolean compareToNumber(String number) {
        return this.mCdmaReference.compareToNumber(number);
    }
}
