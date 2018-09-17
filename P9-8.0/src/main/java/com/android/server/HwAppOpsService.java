package com.android.server;

import android.os.Handler;
import java.io.File;

public class HwAppOpsService extends AppOpsService {
    static final long DELAY_POST_SYSTEM_ALERT_WINDOW = 500;
    static final String TAG = HwAppOpsService.class.getSimpleName();

    public HwAppOpsService(File storagePath) {
        super(storagePath, null);
    }

    public HwAppOpsService(File storagePath, Handler handler) {
        super(storagePath, handler);
    }

    protected void scheduleWriteLockedHook(int code) {
    }
}
