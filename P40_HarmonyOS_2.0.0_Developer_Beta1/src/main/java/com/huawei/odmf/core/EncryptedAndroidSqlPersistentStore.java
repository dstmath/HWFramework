package com.huawei.odmf.core;

import android.content.Context;

/* access modifiers changed from: package-private */
public class EncryptedAndroidSqlPersistentStore extends AndroidSqlPersistentStore {
    EncryptedAndroidSqlPersistentStore(Context context, String str, String str2, Configuration configuration, byte[] bArr) {
        super(context, str, str2, configuration, bArr);
    }

    @Override // com.huawei.odmf.core.PersistentStore
    public void resetDatabaseEncryptKey(byte[] bArr, byte[] bArr2) {
        try {
            this.db.resetDatabaseEncryptKey(bArr, bArr2);
        } finally {
            clearKey(bArr);
            clearKey(bArr2);
        }
    }
}
