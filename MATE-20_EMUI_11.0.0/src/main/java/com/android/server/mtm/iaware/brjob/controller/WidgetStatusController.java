package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.android.server.rms.iaware.appmng.AwareAppAssociate;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;

public class WidgetStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "WidgetStatus";
    private static final Object CREATE_LOCK = new Object();
    private static final String TAG = "WidgetStatusController";
    private static WidgetStatusController sSingleton;
    private AwareAppAssociate mAwareAppAssociate = AwareAppAssociate.getInstance();
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private WidgetStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public static WidgetStatusController get(AwareJobSchedulerService jms) {
        WidgetStatusController widgetStatusController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new WidgetStatusController(jms, jms.getContext(), jms.getLock());
            }
            widgetStatusController = sSingleton;
        }
        return widgetStatusController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("WidgetStatus")) {
            Set<String> widgetsPkg = this.mAwareAppAssociate.getWidgetsPkg();
            if (this.debug) {
                StringBuilder sb = new StringBuilder();
                sb.append("iaware_brjob, widgetsPkg: ");
                sb.append(widgetsPkg == null ? "null" : widgetsPkg);
                AwareLog.i(TAG, sb.toString());
            }
            String receiverPkg = job.getReceiverPkg();
            if (widgetsPkg == null || !widgetsPkg.contains(receiverPkg)) {
                job.setSatisfied("WidgetStatus", false);
            } else {
                job.setSatisfied("WidgetStatus", true);
            }
            addJobLocked(this.mTrackedJobs, job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("WidgetStatus")) {
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    WidgetStatusController tracked job num: " + this.mTrackedJobs.size());
            Set<String> widgetsPkg = this.mAwareAppAssociate.getWidgetsPkg();
            pw.println("        now widget package: " + widgetsPkg);
        }
    }
}
