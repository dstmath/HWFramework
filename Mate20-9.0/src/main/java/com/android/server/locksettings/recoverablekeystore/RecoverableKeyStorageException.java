package com.android.server.locksettings.recoverablekeystore;

public class RecoverableKeyStorageException extends Exception {
    public RecoverableKeyStorageException(String message) {
        super(message);
    }

    public RecoverableKeyStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
