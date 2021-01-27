package com.android.nfc_extras;

import java.io.IOException;

public class EeIOException extends IOException {
    public EeIOException() {
    }

    public EeIOException(String message) {
        super(message);
    }
}
