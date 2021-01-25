package com.android.server.backup.encryption.storage;

import android.provider.BaseColumns;

class BackupEncryptionDbContract {
    BackupEncryptionDbContract() {
    }

    static class TertiaryKeysEntry implements BaseColumns {
        static final String COLUMN_NAME_PACKAGE_NAME = "package_name";
        static final String COLUMN_NAME_SECONDARY_KEY_ALIAS = "secondary_key_alias";
        static final String COLUMN_NAME_WRAPPED_KEY_BYTES = "wrapped_key_bytes";
        static final String TABLE_NAME = "tertiary_keys";

        TertiaryKeysEntry() {
        }
    }
}
