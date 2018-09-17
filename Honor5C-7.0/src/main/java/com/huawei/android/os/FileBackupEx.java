package com.huawei.android.os;

import huawei.android.os.HwFileBackupManager;

public class FileBackupEx {
    public static void startFileBackup() {
        HwFileBackupManager.getInstance().startFileBackup();
    }
}
