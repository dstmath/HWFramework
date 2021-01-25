package com.android.server.job;

import com.android.server.job.controllers.JobStatus;
import java.util.Comparator;

/* renamed from: com.android.server.job.-$$Lambda$JobSchedulerService$V6_ZmVmzJutg4w0s0LktDOsRAss  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$JobSchedulerService$V6_ZmVmzJutg4w0s0LktDOsRAss implements Comparator {
    public static final /* synthetic */ $$Lambda$JobSchedulerService$V6_ZmVmzJutg4w0s0LktDOsRAss INSTANCE = new $$Lambda$JobSchedulerService$V6_ZmVmzJutg4w0s0LktDOsRAss();

    private /* synthetic */ $$Lambda$JobSchedulerService$V6_ZmVmzJutg4w0s0LktDOsRAss() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return JobSchedulerService.lambda$static$0((JobStatus) obj, (JobStatus) obj2);
    }
}
