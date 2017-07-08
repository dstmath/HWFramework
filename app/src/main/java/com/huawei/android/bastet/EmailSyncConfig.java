package com.huawei.android.bastet;

public class EmailSyncConfig {
    private static String mFolderName;
    private static String mLatestUid;

    public EmailSyncConfig(String folder, String uid) {
        mFolderName = folder;
        mLatestUid = uid;
    }

    public String getFolderName() {
        return mFolderName;
    }

    public String getLatestUid() {
        return mLatestUid;
    }
}
