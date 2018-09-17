package com.android.server.job.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.UserHandle;
import com.android.internal.util.ArrayUtils;
import com.android.server.DeviceIdleController.LocalService;
import com.android.server.LocalServices;
import com.android.server.job.JobSchedulerService;
import com.android.server.job.JobStore.JobStatusFunctor;
import java.io.PrintWriter;

public class DeviceIdleJobsController extends StateController {
    private static final boolean LOG_DEBUG = false;
    private static final String LOG_TAG = "DeviceIdleJobsController";
    private static DeviceIdleJobsController sController;
    private static Object sCreationLock;
    private final BroadcastReceiver mBroadcastReceiver;
    private boolean mDeviceIdleMode;
    private int[] mDeviceIdleWhitelistAppIds;
    private final JobSchedulerService mJobSchedulerService;
    private final LocalService mLocalDeviceIdleController;
    private final PowerManager mPowerManager;
    final JobStatusFunctor mUpdateFunctor;

    /* renamed from: com.android.server.job.controllers.DeviceIdleJobsController.3 */
    class AnonymousClass3 implements JobStatusFunctor {
        final /* synthetic */ int val$filterUid;
        final /* synthetic */ PrintWriter val$pw;

        AnonymousClass3(int val$filterUid, PrintWriter val$pw) {
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
                this.val$pw.print((jobStatus.satisfiedConstraints & DumpState.DUMP_SHARED_USERS) != 0 ? " RUNNABLE" : " WAITING");
                if (jobStatus.dozeWhitelisted) {
                    this.val$pw.print(" WHITELISTED");
                }
                this.val$pw.println();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.job.controllers.DeviceIdleJobsController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.job.controllers.DeviceIdleJobsController.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.job.controllers.DeviceIdleJobsController.<clinit>():void");
    }

    public static DeviceIdleJobsController get(JobSchedulerService service) {
        DeviceIdleJobsController deviceIdleJobsController;
        synchronized (sCreationLock) {
            if (sController == null) {
                sController = new DeviceIdleJobsController(service, service.getContext(), service.getLock());
            }
            deviceIdleJobsController = sController;
        }
        return deviceIdleJobsController;
    }

    private DeviceIdleJobsController(JobSchedulerService jobSchedulerService, Context context, Object lock) {
        super(jobSchedulerService, context, lock);
        this.mUpdateFunctor = new JobStatusFunctor() {
            public void process(JobStatus jobStatus) {
                DeviceIdleJobsController.this.updateTaskStateLocked(jobStatus);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)) {
                    boolean z;
                    DeviceIdleJobsController deviceIdleJobsController = DeviceIdleJobsController.this;
                    if (DeviceIdleJobsController.this.mPowerManager == null) {
                        z = DeviceIdleJobsController.LOG_DEBUG;
                    } else if (DeviceIdleJobsController.this.mPowerManager.isDeviceIdleMode()) {
                        z = true;
                    } else {
                        z = DeviceIdleJobsController.this.mPowerManager.isLightDeviceIdleMode();
                    }
                    deviceIdleJobsController.updateIdleMode(z);
                } else if ("android.os.action.POWER_SAVE_WHITELIST_CHANGED".equals(action)) {
                    DeviceIdleJobsController.this.updateWhitelist();
                }
            }
        };
        this.mJobSchedulerService = jobSchedulerService;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mLocalDeviceIdleController = (LocalService) LocalServices.getService(LocalService.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        filter.addAction("android.os.action.POWER_SAVE_WHITELIST_CHANGED");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
    }

    void updateIdleMode(boolean enabled) {
        boolean changed = LOG_DEBUG;
        if (this.mDeviceIdleWhitelistAppIds == null) {
            updateWhitelist();
        }
        synchronized (this.mLock) {
            if (this.mDeviceIdleMode != enabled) {
                changed = true;
            }
            this.mDeviceIdleMode = enabled;
            this.mJobSchedulerService.getJobStore().forEachJob(this.mUpdateFunctor);
        }
        if (changed) {
            this.mStateChangedListener.onDeviceIdleStateChanged(enabled);
        }
    }

    void updateWhitelist() {
        synchronized (this.mLock) {
            if (this.mLocalDeviceIdleController != null) {
                this.mDeviceIdleWhitelistAppIds = this.mLocalDeviceIdleController.getPowerSaveWhitelistUserAppIds();
            }
        }
    }

    boolean isWhitelistedLocked(JobStatus job) {
        if (this.mDeviceIdleWhitelistAppIds == null || !ArrayUtils.contains(this.mDeviceIdleWhitelistAppIds, UserHandle.getAppId(job.getSourceUid()))) {
            return LOG_DEBUG;
        }
        return true;
    }

    private void updateTaskStateLocked(JobStatus task) {
        boolean whitelisted = isWhitelistedLocked(task);
        task.setDeviceNotDozingConstraintSatisfied(this.mDeviceIdleMode ? whitelisted : true, whitelisted);
    }

    public void maybeStartTrackingJobLocked(JobStatus jobStatus, JobStatus lastJob) {
        synchronized (this.mLock) {
            updateTaskStateLocked(jobStatus);
        }
    }

    public void maybeStopTrackingJobLocked(JobStatus jobStatus, JobStatus incomingJob, boolean forUpdate) {
    }

    public void dumpControllerStateLocked(PrintWriter pw, int filterUid) {
        pw.println(LOG_TAG);
        this.mJobSchedulerService.getJobStore().forEachJob(new AnonymousClass3(filterUid, pw));
    }
}
