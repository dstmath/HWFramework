package com.android.server.wifi;

import android.app.backup.BackupManager;

public class BackupManagerProxy {
    public void notifyDataChanged() {
        BackupManager.dataChanged("com.android.providers.settings");
    }
}
