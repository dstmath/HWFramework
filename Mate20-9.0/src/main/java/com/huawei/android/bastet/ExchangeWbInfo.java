package com.huawei.android.bastet;

public class ExchangeWbInfo {
    private String mCollectionId;
    private String mSyncKey;
    private int mSyncType;

    public ExchangeWbInfo(String collectionId, String syncKey, int syncType) {
        this.mCollectionId = collectionId;
        this.mSyncKey = syncKey;
        this.mSyncType = syncType;
    }

    public String getCollectionId() {
        return this.mCollectionId;
    }

    public String getSyncKey() {
        return this.mSyncKey;
    }

    public int getSyncType() {
        return this.mSyncType;
    }
}
