package com.android.org.conscrypt;

class PinManagerException extends Exception {
    PinManagerException() {
    }

    PinManagerException(String msg) {
        super(msg);
    }

    PinManagerException(String msg, Exception e) {
        super(msg, e);
    }
}
