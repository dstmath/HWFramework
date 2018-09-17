package com.android.server.storage;

import android.content.Context;
import com.android.server.SystemService;
import huawei.android.storage.HwCustDeviceStorageMonitorService;

public abstract class AbsDeviceStorageMonitorService extends SystemService {
    public AbsDeviceStorageMonitorService(Context context) {
        super(context);
    }

    public HwCustDeviceStorageMonitorService getCust() {
        return null;
    }
}
