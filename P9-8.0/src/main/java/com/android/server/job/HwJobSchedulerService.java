package com.android.server.job;

import android.content.Context;
import android.content.Intent;

public class HwJobSchedulerService extends JobSchedulerService {
    public HwJobSchedulerService(Context context) {
        super(context);
    }

    public boolean checkShouldFilterIntent(Intent intent, int userId) {
        return false;
    }
}
