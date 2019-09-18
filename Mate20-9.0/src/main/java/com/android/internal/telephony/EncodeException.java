package com.android.internal.telephony;

public class EncodeException extends Exception {
    public EncodeException() {
    }

    public EncodeException(String s) {
        super(s);
    }

    public EncodeException(char c) {
        super("Unencodable char: '" + c + "'");
    }
}
