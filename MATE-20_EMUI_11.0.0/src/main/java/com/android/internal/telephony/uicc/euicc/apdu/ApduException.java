package com.android.internal.telephony.uicc.euicc.apdu;

public class ApduException extends Exception {
    private final int mApduStatus;

    public ApduException(int apduStatus) {
        this.mApduStatus = apduStatus;
    }

    public ApduException(String message) {
        super(message);
        this.mApduStatus = 0;
    }

    public int getApduStatus() {
        return this.mApduStatus;
    }

    public String getStatusHex() {
        return Integer.toHexString(this.mApduStatus);
    }

    @Override // java.lang.Throwable
    public String getMessage() {
        return super.getMessage() + " (apduStatus=" + getStatusHex() + ")";
    }
}
