package com.huawei.odmf.core;

import android.content.Context;

class EncryptedAndroidSqlPersistentStore extends AndroidSqlPersistentStore {
    public EncryptedAndroidSqlPersistentStore(Context context, String modelPath, String uriString, Configuration configuration, byte[] key) {
        super(context, modelPath, uriString, configuration, key);
    }

    public void resetDatabaseEncryptKey(byte[] oldKey, byte[] newKey) {
        try {
            this.db.resetDatabaseEncryptKey(oldKey, newKey);
        } finally {
            clearKey(oldKey);
            clearKey(newKey);
        }
    }
}
