package com.huawei.kvdb;

import com.huawei.android.util.NoExtAPIException;

public class KVConnection {
    private HwKVConnection hwKVConnection;

    /* access modifiers changed from: package-private */
    public void setHwKVConnection(HwKVDatabase hwKVDatabase) {
        this.hwKVConnection = hwKVDatabase.getKVConnection();
    }

    /* access modifiers changed from: package-private */
    public HwKVConnection getHwKVConnection() {
        return this.hwKVConnection;
    }

    @Deprecated
    public KVData get(long key) throws KVDatabaseDeleteException {
        throw new NoExtAPIException("method not support");
    }

    public byte[] getBytes(long key) throws KVDatabaseDeleteException {
        HwKVConnection hwKVConnection2 = this.hwKVConnection;
        if (hwKVConnection2 == null) {
            return null;
        }
        try {
            return hwKVConnection2.get(key);
        } catch (HwKVDatabaseDeleteException e) {
            throw new KVDatabaseDeleteException();
        }
    }

    public byte[] get(String key) throws KVDatabaseDeleteException {
        HwKVConnection hwKVConnection2 = this.hwKVConnection;
        if (hwKVConnection2 == null) {
            return null;
        }
        try {
            return hwKVConnection2.get(key);
        } catch (HwKVDatabaseDeleteException e) {
            throw new KVDatabaseDeleteException();
        }
    }
}
