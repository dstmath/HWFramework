package com.android.server.backup.encryption.storage;

import android.content.Context;

public class BackupEncryptionDb {
    private final BackupEncryptionDbHelper mHelper;

    public static BackupEncryptionDb newInstance(Context context) {
        BackupEncryptionDbHelper helper = new BackupEncryptionDbHelper(context);
        helper.setWriteAheadLoggingEnabled(true);
        return new BackupEncryptionDb(helper);
    }

    private BackupEncryptionDb(BackupEncryptionDbHelper helper) {
        this.mHelper = helper;
    }

    public TertiaryKeysTable getTertiaryKeysTable() {
        return new TertiaryKeysTable(this.mHelper);
    }

    public void clear() throws EncryptionDbException {
        this.mHelper.resetDatabase();
    }

    public void close() {
        this.mHelper.close();
    }
}
