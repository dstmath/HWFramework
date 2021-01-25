package com.android.server.backup.encryption.storage;

import java.io.IOException;

public class EncryptionDbException extends IOException {
    public EncryptionDbException(Throwable cause) {
        super(cause);
    }
}
