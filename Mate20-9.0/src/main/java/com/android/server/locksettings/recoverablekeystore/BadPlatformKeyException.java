package com.android.server.locksettings.recoverablekeystore;

public class BadPlatformKeyException extends Exception {
    public BadPlatformKeyException(String message) {
        super(message);
    }
}
