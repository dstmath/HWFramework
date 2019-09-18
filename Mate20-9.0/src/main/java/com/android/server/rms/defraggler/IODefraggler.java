package com.android.server.rms.defraggler;

import android.os.Bundle;
import android.util.Log;
import com.android.server.rms.IDefraggler;
import com.android.server.rms.io.IOStatsService;

public class IODefraggler implements IDefraggler {
    private static final String TAG = "RMS.IODefraggler";
    private IOStatsService mIOStatsService = null;

    public int compact(String reason, Bundle extras) {
        Log.w(TAG, reason);
        if (this.mIOStatsService == null) {
            this.mIOStatsService = IOStatsService.getInstance(null, null);
        }
        if (this.mIOStatsService != null) {
            this.mIOStatsService.periodMonitorTask();
        }
        return 0;
    }

    public void interrupt() {
        if (this.mIOStatsService != null) {
            this.mIOStatsService.interruptMonitorTask();
        }
    }
}
