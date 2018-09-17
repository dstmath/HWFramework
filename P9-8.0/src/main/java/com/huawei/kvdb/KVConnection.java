package com.huawei.kvdb;

public class KVConnection {
    private HwKVConnection hwKVConnection;

    void setHwKVConnection(HwKVDatabase hwKVDatabase) {
        this.hwKVConnection = hwKVDatabase.getKVConnection();
    }

    HwKVConnection getHwKVConnection() {
        return this.hwKVConnection;
    }

    public KVData get(long key) throws KVDatabaseDeleteException {
        if (this.hwKVConnection == null) {
            return null;
        }
        try {
            HwKVData hwKVData = this.hwKVConnection.get(key);
            if (hwKVData == null) {
                return null;
            }
            KVData kvData = new KVData();
            kvData.value = hwKVData.value;
            kvData.size = hwKVData.size;
            return kvData;
        } catch (HwKVDatabaseDeleteException e) {
            throw new KVDatabaseDeleteException();
        }
    }
}
