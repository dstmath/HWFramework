package com.android.server.mtm.iaware.brjob.controller;

import android.content.Context;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class AwareStateController {
    protected boolean DEBUG = false;
    protected final Context mContext;
    protected final Object mLock;
    protected final AwareStateChangedListener mStateChangedListener;

    public abstract void dump(PrintWriter printWriter);

    public abstract void maybeStartTrackingJobLocked(AwareJobStatus awareJobStatus);

    public abstract void maybeStopTrackingJobLocked(AwareJobStatus awareJobStatus);

    public AwareStateController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        this.mStateChangedListener = stateChangedListener;
        this.mContext = context;
        this.mLock = lock;
    }

    public void setDebug(boolean debug) {
        this.DEBUG = debug;
    }

    protected void addJobLocked(ArrayList<AwareJobStatus> jobList, AwareJobStatus jobStatus) {
        if (jobList != null) {
            Iterator<AwareJobStatus> it = jobList.iterator();
            while (it.hasNext()) {
                if (((AwareJobStatus) it.next()).equalJob(jobStatus)) {
                    it.remove();
                    break;
                }
            }
            jobList.add(jobStatus);
        }
    }
}
