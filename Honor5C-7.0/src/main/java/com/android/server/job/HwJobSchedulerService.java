package com.android.server.job;

import android.content.Context;
import android.content.Intent;
import com.android.server.pfw.HwPFWService;

public class HwJobSchedulerService extends JobSchedulerService {
    public HwJobSchedulerService(Context context) {
        super(context);
    }

    public boolean checkShouldFilterIntent(Intent intent, int userId) {
        HwPFWService pfwService = HwPFWService.self();
        if (pfwService != null) {
            return pfwService.shouldPreventJobService(intent, userId);
        }
        return false;
    }
}
