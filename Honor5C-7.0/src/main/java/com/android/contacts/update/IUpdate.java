package com.android.contacts.update;

public interface IUpdate {
    DownloadRequest contructRequest();

    void handleComplete(DownloadResponse downloadResponse);

    void scheduleAutoUpdate();

    boolean tryUpdate();
}
