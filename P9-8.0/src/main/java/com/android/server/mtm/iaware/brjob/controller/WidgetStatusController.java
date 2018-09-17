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
    private static final String TAG = "WidgetStatusController";
    private static WidgetStatusController mSingleton;
    private static Object sCreationLock = new Object();
    private AwareAppAssociate mAwareAppAssociate = AwareAppAssociate.getInstance();
    @GuardedBy("mLock")
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList();

    public static WidgetStatusController get(AwareJobSchedulerService jms) {
        WidgetStatusController widgetStatusController;
        synchronized (sCreationLock) {
            if (mSingleton == null) {
                mSingleton = new WidgetStatusController(jms, jms.getContext(), jms.getLock());
            }
            widgetStatusController = mSingleton;
        }
        return widgetStatusController;
    }

    private WidgetStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
    }

    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("WidgetStatus")) {
            Set<String> widgetsPkg = this.mAwareAppAssociate.getWidgetsPkg();
            if (this.DEBUG) {
                Object obj;
                String str = TAG;
                StringBuilder append = new StringBuilder().append("iaware_brjob, widgetsPkg: ");
                if (widgetsPkg == null) {
                    obj = "null";
                } else {
                    Set<String> obj2 = widgetsPkg;
                }
                AwareLog.i(str, append.append(obj2).toString());
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

    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("WidgetStatus")) {
            if (this.DEBUG) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    WidgetStatusController tracked job num: " + this.mTrackedJobs.size());
            pw.println("        now widget package: " + this.mAwareAppAssociate.getWidgetsPkg());
        }
    }
}
