package com.android.server.job.controllers;

import android.app.usage.UsageStatsManagerInternal;
import android.content.Context;
import android.os.UserHandle;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.JobStore.JobStatusFunctor;
import java.io.PrintWriter;

public final class AppIdleController extends StateController {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AppIdleController";
    private static volatile AppIdleController sController;
    private static Object sCreationLock = new Object();
    boolean mAppIdleParoleOn = true;
    private boolean mInitializedParoleOn;
    private final JobSchedulerService mJobSchedulerService;
    private final UsageStatsManagerInternal mUsageStatsInternal = ((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));

    private final class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        /* synthetic */ AppIdleStateChangeListener(AppIdleController this$0, AppIdleStateChangeListener -this1) {
            this();
        }

        private AppIdleStateChangeListener() {
        }

        /* JADX WARNING: Missing block: B:12:0x0026, code:
            if (r0 == false) goto L_0x002f;
     */
        /* JADX WARNING: Missing block: B:13:0x0028, code:
            r4.this$0.mStateChangedListener.onControllerStateChanged();
     */
        /* JADX WARNING: Missing block: B:14:0x002f, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            boolean changed = false;
            synchronized (AppIdleController.this.mLock) {
                if (AppIdleController.this.mAppIdleParoleOn) {
                    return;
                }
                PackageUpdateFunc update = new PackageUpdateFunc(userId, packageName, idle);
                AppIdleController.this.mJobSchedulerService.getJobStore().forEachJob(update);
                if (update.mChanged) {
                    changed = true;
                }
            }
        }

        public void onParoleStateChanged(boolean isParoleOn) {
            AppIdleController.this.setAppIdleParoleOn(isParoleOn);
        }
    }

    final class GlobalUpdateFunc implements JobStatusFunctor {
        boolean mChanged;

        GlobalUpdateFunc() {
        }

        public void process(JobStatus jobStatus) {
            if (jobStatus.setAppNotIdleConstraintSatisfied((!AppIdleController.this.mAppIdleParoleOn ? AppIdleController.this.mUsageStatsInternal.isAppIdle(jobStatus.getSourcePackageName(), jobStatus.getSourceUid(), jobStatus.getSourceUserId()) : 0) ^ 1)) {
                this.mChanged = true;
            }
        }
    }

    static final class PackageUpdateFunc implements JobStatusFunctor {
        boolean mChanged;
        final boolean mIdle;
        final String mPackage;
        final int mUserId;

        PackageUpdateFunc(int userId, String pkg, boolean idle) {
            this.mUserId = userId;
            this.mPackage = pkg;
            this.mIdle = idle;
        }

        public void process(JobStatus jobStatus) {
            if (jobStatus.getSourcePackageName().equals(this.mPackage) && jobStatus.getSourceUserId() == this.mUserId && jobStatus.setAppNotIdleConstraintSatisfied(this.mIdle ^ 1)) {
                this.mChanged = true;
            }
        }
    }

    public static AppIdleController get(JobSchedulerService service) {
        AppIdleController appIdleController;
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new AppIdleController(service, service.getContext(), service.getLock());
            }
            appIdleController = sController;
        }
        return appIdleController;
    }

    private AppIdleController(JobSchedulerService service, Context context, Object lock) {
        super(service, context, lock);
        this.mJobSchedulerService = service;
        this.mUsageStatsInternal.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        if (!this.mInitializedParoleOn) {
            this.mInitializedParoleOn = true;
            this.mAppIdleParoleOn = this.mUsageStatsInternal.isAppIdleParoleOn();
        }
        jobStatus.setAppNotIdleConstraintSatisfied((!this.mAppIdleParoleOn ? this.mUsageStatsInternal.isAppIdle(jobStatus.getSourcePackageName(), jobStatus.getSourceUid(), jobStatus.getSourceUserId()) : 0) ^ 1);
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(final PrintWriter pw, final int filterUid) {
        pw.print("AppIdle: parole on = ");
        pw.println(this.mAppIdleParoleOn);
        this.mJobSchedulerService.getJobStore().forEachJob(new JobStatusFunctor() {
            public void process(JobStatus jobStatus) {
                if (jobStatus.shouldDump(filterUid)) {
                    pw.print("  #");
                    jobStatus.printUniqueId(pw);
                    pw.print(" from ");
                    UserHandle.formatUid(pw, jobStatus.getSourceUid());
                    pw.print(": ");
                    pw.print(jobStatus.getSourcePackageName());
                    if ((jobStatus.satisfiedConstraints & 134217728) != 0) {
                        pw.println(" RUNNABLE");
                    } else {
                        pw.println(" WAITING");
                    }
                }
            }
        });
    }

    /* JADX WARNING: Missing block: B:12:0x0020, code:
            if (r0 == false) goto L_0x0027;
     */
    /* JADX WARNING: Missing block: B:13:0x0022, code:
            r4.mStateChangedListener.onControllerStateChanged();
     */
    /* JADX WARNING: Missing block: B:14:0x0027, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void setAppIdleParoleOn(boolean isAppIdleParoleOn) {
        boolean changed = false;
        synchronized (this.mLock) {
            if (this.mAppIdleParoleOn == isAppIdleParoleOn) {
                return;
            }
            this.mAppIdleParoleOn = isAppIdleParoleOn;
            GlobalUpdateFunc update = new GlobalUpdateFunc();
            this.mJobSchedulerService.getJobStore().forEachJob(update);
            if (update.mChanged) {
                changed = true;
            }
        }
    }
}
