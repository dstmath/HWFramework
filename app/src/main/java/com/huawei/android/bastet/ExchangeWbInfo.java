package com.huawei.android.bastet;

public class ExchangeWbInfo {
    private static String mCollectionId;
    private static String mSyncKey;
    private static int mSyncType;

    public ExchangeWbInfo(String collectionId, String syncKey, int syncType) {
        mCollectionId = collectionId;
        mSyncKey = syncKey;
        mSyncType = syncType;
    }

    public String getCollectionId() {
        return mCollectionId;
    }

    public String getSyncKey() {
        return mSyncKey;
    }

    public int getSyncType() {
        return mSyncType;
    }
}
