package com.android.server.pm;

import android.app.job.JobParameters;

public interface IHwBackgroundDexOptServiceEx {
    void cancelInterruptCompensateOpt();

    int getReason(int i, int i2, int i3, String str);

    void interruptCompensateOpt();

    boolean runBootUpdateDelayOpt(JobParameters jobParameters);

    boolean runCompensateOpt(JobParameters jobParameters);

    boolean stopBootUpdateDelayOpt(JobParameters jobParameters);

    boolean stopCompenstateOpt(JobParameters jobParameters);
}
