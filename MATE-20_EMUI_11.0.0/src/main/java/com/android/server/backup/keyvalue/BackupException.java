package com.android.server.backup.keyvalue;

import android.util.AndroidException;

class BackupException extends AndroidException {
    BackupException() {
    }

    BackupException(Exception cause) {
        super(cause);
    }
}
