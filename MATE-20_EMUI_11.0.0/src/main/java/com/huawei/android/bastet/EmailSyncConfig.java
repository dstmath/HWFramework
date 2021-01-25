package com.huawei.android.bastet;

public class EmailSyncConfig {
    private String mFolderName;
    private String mLatestUid;

    public EmailSyncConfig(String folder, String uid) {
        this.mFolderName = folder;
        this.mLatestUid = uid;
    }

    public String getFolderName() {
        return this.mFolderName;
    }

    public String getLatestUid() {
        return this.mLatestUid;
    }
}
