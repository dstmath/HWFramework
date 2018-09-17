package com.vzw.nfc.dos;

public class DoParserException extends Exception {
    public DoParserException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public DoParserException(String arg0) {
        super(arg0);
    }

    public DoParserException(Throwable arg0) {
        super(arg0);
    }
}
