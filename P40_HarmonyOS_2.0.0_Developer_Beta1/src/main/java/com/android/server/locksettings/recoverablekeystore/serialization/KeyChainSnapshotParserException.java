package com.android.server.locksettings.recoverablekeystore.serialization;

public class KeyChainSnapshotParserException extends Exception {
    public KeyChainSnapshotParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyChainSnapshotParserException(String message) {
        super(message);
    }
}
