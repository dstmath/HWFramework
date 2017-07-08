package com.android.server.job.controllers;

import android.app.usage.UsageStatsManagerInternal;
import android.content.Context;
import android.os.UserHandle;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.JobStore.JobStatusFunctor;
import java.io.PrintWriter;

public class AppIdleController extends StateController {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AppIdleController";
    private static volatile AppIdleController sController;
    private static Object sCreationLock;
    boolean mAppIdleParoleOn;
    private boolean mInitializedParoleOn;
    private final JobSchedulerService mJobSchedulerService;
    private final UsageStatsManagerInternal mUsageStatsInternal;

    /* renamed from: com.android.server.job.controllers.AppIdleController.1 */
    class AnonymousClass1 implements JobStatusFunctor {
        final /* synthetic */ int val$filterUid;
        final /* synthetic */ PrintWriter val$pw;

        AnonymousClass1(int val$filterUid, PrintWriter val$pw) {
            this.val$filterUid = val$filterUid;
            this.val$pw = val$pw;
        }

        public void process(JobStatus jobStatus) {
            if (jobStatus.shouldDump(this.val$filterUid)) {
                this.val$pw.print("  #");
                jobStatus.printUniqueId(this.val$pw);
                this.val$pw.print(" from ");
                UserHandle.formatUid(this.val$pw, jobStatus.getSourceUid());
                this.val$pw.print(": ");
                this.val$pw.print(jobStatus.getSourcePackageName());
                if ((jobStatus.satisfiedConstraints & 64) != 0) {
                    this.val$pw.println(" RUNNABLE");
                } else {
                    this.val$pw.println(" WAITING");
                }
            }
        }
    }

    private class AppIdleStateChangeListener extends android.app.usage.UsageStatsManagerInternal.AppIdleStateChangeListener {
        private AppIdleStateChangeListener() {
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle) {
            boolean changed = AppIdleController.DEBUG;
            synchronized (AppIdleController.this.mLock) {
                if (AppIdleController.this.mAppIdleParoleOn) {
                    return;
                }
                PackageUpdateFunc update = new PackageUpdateFunc(userId, packageName, idle);
                AppIdleController.this.mJobSchedulerService.getJobStore().forEachJob(update);
                if (update.mChanged) {
                    changed = true;
                }
                if (changed) {
                    AppIdleController.this.mStateChangedListener.onControllerStateChanged();
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
            boolean appIdle;
            boolean z = AppIdleController.DEBUG;
            String packageName = jobStatus.getSourcePackageName();
            if (AppIdleController.this.mAppIdleParoleOn) {
                appIdle = AppIdleController.DEBUG;
            } else {
                appIdle = AppIdleController.this.mUsageStatsInternal.isAppIdle(packageName, jobStatus.getSourceUid(), jobStatus.getSourceUserId());
            }
            if (!appIdle) {
                z = true;
            }
            if (jobStatus.setAppNotIdleConstraintSatisfied(z)) {
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
            if (jobStatus.getSourcePackageName().equals(this.mPackage) && jobStatus.getSourceUserId() == this.mUserId) {
                boolean z;
                if (this.mIdle) {
                    z = AppIdleController.DEBUG;
                } else {
                    z = true;
                }
                if (jobStatus.setAppNotIdleConstraintSatisfied(z)) {
                    this.mChanged = true;
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.controllers.AppIdleController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.job.controllers.AppIdleController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.AppIdleController.<clinit>():void");
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
        this.mUsageStatsInternal = (UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class);
        this.mAppIdleParoleOn = true;
        this.mUsageStatsInternal.addAppIdleStateChangeListener(new AppIdleStateChangeListener());
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        boolean appIdle;
        boolean z = DEBUG;
        if (!this.mInitializedParoleOn) {
            this.mInitializedParoleOn = true;
            this.mAppIdleParoleOn = this.mUsageStatsInternal.isAppIdleParoleOn();
        }
        String packageName = jobStatus.getSourcePackageName();
        if (this.mAppIdleParoleOn) {
            appIdle = DEBUG;
        } else {
            appIdle = this.mUsageStatsInternal.isAppIdle(packageName, jobStatus.getSourceUid(), jobStatus.getSourceUserId());
        }
        if (!appIdle) {
            z = true;
        }
        jobStatus.setAppNotIdleConstraintSatisfied(z);
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.print("AppIdle: parole on = ");
        pw.println(this.mAppIdleParoleOn);
        this.mJobSchedulerService.getJobStore().forEachJob(new AnonymousClass1(filterUid, pw));
    }

    void setAppIdleParoleOn(boolean isAppIdleParoleOn) {
        boolean changed = DEBUG;
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
            if (changed) {
                this.mStateChangedListener.onControllerStateChanged();
            }
        }
    }
}
