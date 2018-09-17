package com.android.server.job;

import android.content.Context;
import android.content.Intent;
import com.android.server.SystemService;

public abstract class AbsJobSchedulerService extends SystemService {
    public AbsJobSchedulerService(Context context) {
        super(context);
    }

    public boolean checkShouldFilterIntent(Intent intent, int userId) {
        return false;
    }
}
