package com.android.internal.telephony.uicc.euicc;

public class EuiccCardException extends Exception {
    public EuiccCardException() {
    }

    public EuiccCardException(String message) {
        super(message);
    }

    public EuiccCardException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
