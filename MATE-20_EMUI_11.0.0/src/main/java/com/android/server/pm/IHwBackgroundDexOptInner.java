package com.android.server.pm;

import android.app.job.JobParameters;
import android.util.ArraySet;

public interface IHwBackgroundDexOptInner {
    boolean runPostBootUpdateEx(JobParameters jobParameters, PackageManagerService packageManagerService, ArraySet<String> arraySet);
}
