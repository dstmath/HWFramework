package com.android.server.mtm.iaware.brjob.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.am.HwMtmBroadcastResourceManager;
import com.android.server.mtm.iaware.brjob.controller.AppStatusController;
import com.android.server.mtm.iaware.brjob.controller.AwareStateController;
import com.android.server.mtm.iaware.brjob.controller.BarStatusController;
import com.android.server.mtm.iaware.brjob.controller.BluetoothStatusController;
import com.android.server.mtm.iaware.brjob.controller.ConnectivityController;
import com.android.server.mtm.iaware.brjob.controller.KeyWordController;
import com.android.server.mtm.iaware.brjob.controller.LimitNumController;
import com.android.server.mtm.iaware.brjob.controller.MinTimeController;
import com.android.server.mtm.iaware.brjob.controller.ServicesStatusController;
import com.android.server.mtm.iaware.brjob.controller.SimStatusController;
import com.android.server.mtm.iaware.brjob.controller.WidgetStatusController;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AwareJobSchedulerService implements AwareStateChangedListener {
    public static final int MSG_BEGIN = 400;
    public static final int MSG_CHECK_JOB = 402;
    public static final int MSG_CONTROLLER_CHANGED = 404;
    public static final int MSG_EXPIRE_JOB = 401;
    public static final int MSG_REMOVE_JOB = 403;
    public static final int MSG_SCHEDULE_JOB = 400;
    private static final String TAG = "AwareJobSchedulerService";
    private boolean debug = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.PACKAGE_REMOVED".equals(intent.getAction()) && intent.getData() != null) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (!TextUtils.isEmpty(packageName)) {
                    AwareJobSchedulerService.this.cancelJobsForPkgLocked(packageName);
                }
            }
        }
    };
    private final Context mContext;
    private ArrayList<AwareStateController> mControllers;
    private final AwareJobHandler mHandler;
    private ArrayList<AwareJobStatus> mJobs;
    private final Object mLock = new Object();

    public AwareJobSchedulerService(Context context, HandlerThread handlerThread) {
        this.mContext = context;
        this.mHandler = new AwareJobHandler(handlerThread.getLooper());
        this.mJobs = new ArrayList<>();
        this.mControllers = new ArrayList<>();
        this.mControllers.add(LimitNumController.get(this));
        this.mControllers.add(KeyWordController.get(this));
        this.mControllers.add(SimStatusController.get(this));
        this.mControllers.add(BluetoothStatusController.get(this));
        this.mControllers.add(WidgetStatusController.get(this));
        this.mControllers.add(BarStatusController.get(this));
        this.mControllers.add(AppStatusController.get(this));
        this.mControllers.add(MinTimeController.get(this));
        this.mControllers.add(ServicesStatusController.get(this));
        this.mControllers.add(ConnectivityController.get(this));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        ContextEx.registerReceiverAsUser(context, this.mBroadcastReceiver, UserHandleEx.ALL, filter, (String) null, (Handler) null);
    }

    @Override // com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener
    public void onControllerStateChanged(List<AwareJobStatus> jobList) {
        if (jobList != null) {
            Message msg = this.mHandler.obtainMessage(MSG_CONTROLLER_CHANGED);
            msg.obj = jobList;
            msg.sendToTarget();
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener
    public void onRunJobNow(AwareJobStatus jobStatus) {
        this.mHandler.obtainMessage(MSG_EXPIRE_JOB, jobStatus).sendToTarget();
    }

    @Override // com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener
    public void onRemoveJobNow(AwareJobStatus jobStatus) {
        this.mHandler.obtainMessage(MSG_REMOVE_JOB, jobStatus).sendToTarget();
    }

    public Object getLock() {
        return this.mLock;
    }

    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: private */
    public class AwareJobHandler extends Handler {
        public AwareJobHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 400:
                    AwareJobSchedulerService.this.scheduleJob((AwareJobStatus) message.obj);
                    return;
                case AwareJobSchedulerService.MSG_EXPIRE_JOB /* 401 */:
                    synchronized (AwareJobSchedulerService.this.mLock) {
                        AwareJobStatus runNow = (AwareJobStatus) message.obj;
                        if (runNow != null) {
                            HwMtmBroadcastResourceManager.insertAwareOrderedBroadcast(runNow.getHwBroadcastRecord());
                            AwareJobSchedulerService.this.stopTrackingJobLocked(runNow);
                        }
                    }
                    return;
                case AwareJobSchedulerService.MSG_CHECK_JOB /* 402 */:
                    AwareJobSchedulerService.this.maybeQueueReadyJobsForExecution();
                    return;
                case AwareJobSchedulerService.MSG_REMOVE_JOB /* 403 */:
                    synchronized (AwareJobSchedulerService.this.mLock) {
                        if (message.obj instanceof AwareJobStatus) {
                            AwareJobSchedulerService.this.stopTrackingJobLocked((AwareJobStatus) message.obj);
                        }
                    }
                    return;
                case AwareJobSchedulerService.MSG_CONTROLLER_CHANGED /* 404 */:
                    List<AwareJobStatus> jobList = (List) message.obj;
                    if (jobList != null) {
                        AwareJobSchedulerService.this.controllerStateChanged(jobList);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public boolean schedule(HwBroadcastRecord hwBr) {
        if (hwBr == null) {
            return false;
        }
        AwareJobStatus jobStatus = AwareJobStatus.createFromBroadcastRecord(hwBr);
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob schedule begin, receiver: " + jobStatus.getComponentName() + ", action: " + hwBr.getAction());
        }
        if (jobStatus.isParseError()) {
            AwareLog.e(TAG, "iaware_brjob implicit config error, app will not be started.");
            return true;
        } else if (jobStatus.isParamError() || jobStatus.getActionFilterSize() == 0) {
            AwareLog.e(TAG, "iaware_brjob intent error or no implicit config, don't schedule!");
            return false;
        } else {
            this.mHandler.obtainMessage(400, jobStatus).sendToTarget();
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scheduleJob(AwareJobStatus jobStatus) {
        synchronized (this.mLock) {
            Iterator<AwareJobStatus> it = this.mJobs.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                } else if (it.next().equalJob(jobStatus)) {
                    it.remove();
                    break;
                }
            }
            this.mJobs.add(jobStatus);
            int listSize = this.mControllers.size();
            for (int i = 0; i < listSize; i++) {
                this.mControllers.get(i).maybeStartTrackingJobLocked(jobStatus);
            }
        }
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void controllerStateChanged(List<AwareJobStatus> jobList) {
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob controllerStateChanged begin");
        }
        synchronized (this.mLock) {
            for (AwareJobStatus job : jobList) {
                int controllerSize = this.mControllers.size();
                for (int index = 0; index < controllerSize; index++) {
                    this.mControllers.get(index).maybeStartTrackingJobLocked(job);
                }
            }
        }
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelJobsForPkgLocked(String packageName) {
        synchronized (this.mLock) {
            int length = this.mJobs.size();
            if (length < 1) {
                AwareLog.w(TAG, "iaware_brjob no job need to cancel.");
                return;
            }
            for (int index = length; index > 0; index--) {
                AwareJobStatus jobStatus = this.mJobs.get(index - 1);
                if (packageName.equals(jobStatus.getReceiverPkg())) {
                    stopTrackingJobLocked(jobStatus);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopTrackingJobLocked(AwareJobStatus jobStatus) {
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob stopTrackingJobLocked begin");
        }
        this.mJobs.remove(jobStatus);
        int controllerSize = this.mControllers.size();
        for (int i = 0; i < controllerSize; i++) {
            this.mControllers.get(i).maybeStopTrackingJobLocked(jobStatus);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeQueueReadyJobsForExecution() {
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob maybeQueueReadyJobsForExecution begin");
        }
        synchronized (this.mLock) {
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                AwareJobStatus job = this.mJobs.get(i);
                if (job.isReady()) {
                    HwMtmBroadcastResourceManager.insertAwareOrderedBroadcast(job.getHwBroadcastRecord());
                }
                if (job.isReady() || job.shouldCancel()) {
                    stopTrackingJobLocked(job);
                }
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            if (args.length < 3 || args[2] == null) {
                dumpAllJobs(pw);
            } else if ("action".equals(args[2])) {
                if (args.length < 4 || TextUtils.isEmpty(args[3])) {
                    pw.println("  no action option found, please input action name.");
                } else {
                    dumpJobsForAction(pw, args[3]);
                }
            } else if ("receiver".equals(args[2])) {
                if (args.length < 4 || TextUtils.isEmpty(args[3])) {
                    pw.println("  no receiver option found, please input receiver name.");
                } else {
                    dumpJobsForReceiver(pw, args[3]);
                }
            } else if ("controller".equals(args[2])) {
                dumpControllers(pw);
            } else {
                pw.println("  bad command" + args[2]);
            }
        }
    }

    private void dumpAllJobs(PrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mJobs.size() == 0) {
                pw.println("    There is no job now.");
                return;
            }
            Iterator<AwareJobStatus> it = this.mJobs.iterator();
            while (it.hasNext()) {
                it.next().dump(pw);
            }
        }
    }

    private void dumpJobsForAction(PrintWriter pw, String action) {
        synchronized (this.mLock) {
            if (this.mJobs.size() == 0) {
                pw.println("    There is no job now.");
                return;
            }
            boolean hasAction = false;
            int listSize = this.mJobs.size();
            for (int i = 0; i < listSize; i++) {
                AwareJobStatus job = this.mJobs.get(i);
                if (action.equals(job.getAction())) {
                    job.dump(pw);
                    hasAction = true;
                }
            }
            if (!hasAction) {
                pw.println("    There is no job for action: " + action);
            }
        }
    }

    private void dumpJobsForReceiver(PrintWriter pw, String receiver) {
        synchronized (this.mLock) {
            if (this.mJobs.size() == 0) {
                pw.println("    There is no job now.");
                return;
            }
            boolean hasReceiver = false;
            int listSize = this.mJobs.size();
            for (int i = 0; i < listSize; i++) {
                AwareJobStatus job = this.mJobs.get(i);
                if (receiver.equals(job.getReceiverPkg())) {
                    job.dump(pw);
                    hasReceiver = true;
                }
            }
            if (!hasReceiver) {
                pw.println("    There is no job for receiver: " + receiver);
            }
        }
    }

    private void dumpControllers(PrintWriter pw) {
        int size = this.mControllers.size();
        if (size == 0) {
            pw.println("    There is no controller in manager.");
            return;
        }
        for (int i = 0; i < size; i++) {
            this.mControllers.get(i).dump(pw);
        }
    }

    public void disableDebug() {
        this.debug = false;
        int controllerSize = this.mControllers.size();
        for (int i = 0; i < controllerSize; i++) {
            this.mControllers.get(i).setDebug(false);
        }
        AwareJobStatus.setDebug(false);
    }

    public void enableDebug() {
        this.debug = true;
        int controllerSize = this.mControllers.size();
        for (int i = 0; i < controllerSize; i++) {
            this.mControllers.get(i).setDebug(true);
        }
        AwareJobStatus.setDebug(true);
    }
}
