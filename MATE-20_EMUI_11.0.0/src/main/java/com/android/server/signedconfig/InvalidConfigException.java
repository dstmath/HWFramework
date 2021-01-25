package com.android.server.signedconfig;

public class InvalidConfigException extends Exception {
    public InvalidConfigException(String message) {
        super(message);
    }

    public InvalidConfigException(String message, Exception cause) {
        super(message, cause);
    }
}
