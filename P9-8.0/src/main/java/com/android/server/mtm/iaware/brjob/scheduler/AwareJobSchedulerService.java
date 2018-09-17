package com.android.server.mtm.iaware.brjob.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
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
import com.android.server.mtm.iaware.brjob.controller.SIMStatusController;
import com.android.server.mtm.iaware.brjob.controller.ServicesStatusController;
import com.android.server.mtm.iaware.brjob.controller.WidgetStatusController;
import com.android.server.pfw.autostartup.comm.XmlConst.PreciseIgnore;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AwareJobSchedulerService implements AwareStateChangedListener {
    private static final int MSG_BEGIN = 400;
    public static final int MSG_CHECK_JOB = 402;
    public static final int MSG_CONTROLLER_CHANGED = 404;
    public static final int MSG_JOB_EXPIRED = 401;
    public static final int MSG_REMOVE_JOB = 403;
    public static final int MSG_SCHEDULE_JOB = 400;
    private static final String TAG = "AwareJobSchedulerService";
    private boolean DEBUG = false;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
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

    private class AwareJobHandler extends Handler {
        public AwareJobHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            Object -get0;
            switch (message.what) {
                case 400:
                    AwareJobSchedulerService.this.scheduleJob(message.obj);
                    return;
                case AwareJobSchedulerService.MSG_JOB_EXPIRED /*401*/:
                    -get0 = AwareJobSchedulerService.this.mLock;
                    synchronized (-get0) {
                        AwareJobStatus runNow = message.obj;
                        if (runNow != null) {
                            HwMtmBroadcastResourceManager.insertIawareOrderedBroadcast(runNow.getHwBroadcastRecord());
                            AwareJobSchedulerService.this.stopTrackingJobLocked(runNow);
                            break;
                        }
                    }
                    break;
                case AwareJobSchedulerService.MSG_CHECK_JOB /*402*/:
                    AwareJobSchedulerService.this.maybeQueueReadyJobsForExecution();
                    return;
                case AwareJobSchedulerService.MSG_REMOVE_JOB /*403*/:
                    -get0 = AwareJobSchedulerService.this.mLock;
                    synchronized (-get0) {
                        AwareJobSchedulerService.this.stopTrackingJobLocked(message.obj);
                        break;
                    }
                case AwareJobSchedulerService.MSG_CONTROLLER_CHANGED /*404*/:
                    List<AwareJobStatus> jobList = message.obj;
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

    public AwareJobSchedulerService(Context context, HandlerThread handlerThread) {
        this.mContext = context;
        this.mHandler = new AwareJobHandler(handlerThread.getLooper());
        this.mJobs = new ArrayList();
        this.mControllers = new ArrayList();
        this.mControllers.add(LimitNumController.get(this));
        this.mControllers.add(KeyWordController.get(this));
        this.mControllers.add(SIMStatusController.get(this));
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
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, filter, null, null);
    }

    public void onControllerStateChanged(List<AwareJobStatus> jobList) {
        if (jobList != null) {
            Message msg = this.mHandler.obtainMessage(MSG_CONTROLLER_CHANGED);
            msg.obj = jobList;
            msg.sendToTarget();
        }
    }

    public void onRunJobNow(AwareJobStatus jobStatus) {
        this.mHandler.obtainMessage(MSG_JOB_EXPIRED, jobStatus).sendToTarget();
    }

    public void onRemoveJobNow(AwareJobStatus jobStatus) {
        this.mHandler.obtainMessage(MSG_REMOVE_JOB, jobStatus).sendToTarget();
    }

    public Object getLock() {
        return this.mLock;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean schedule(HwBroadcastRecord hwBr) {
        if (hwBr == null) {
            return false;
        }
        AwareJobStatus jobStatus = AwareJobStatus.createFromBroadcastRecord(hwBr);
        if (this.DEBUG) {
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

    private void scheduleJob(AwareJobStatus jobStatus) {
        synchronized (this.mLock) {
            Iterator<AwareJobStatus> it = this.mJobs.iterator();
            while (it.hasNext()) {
                if (((AwareJobStatus) it.next()).equalJob(jobStatus)) {
                    it.remove();
                    break;
                }
            }
            this.mJobs.add(jobStatus);
            int listSize = this.mControllers.size();
            for (int i = 0; i < listSize; i++) {
                ((AwareStateController) this.mControllers.get(i)).maybeStartTrackingJobLocked(jobStatus);
            }
        }
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    private void controllerStateChanged(List<AwareJobStatus> jobList) {
        if (this.DEBUG) {
            AwareLog.i(TAG, "iaware_brjob controllerStateChanged begin");
        }
        synchronized (this.mLock) {
            int listSize = jobList.size();
            for (int i = 0; i < listSize; i++) {
                AwareJobStatus job = (AwareJobStatus) jobList.get(i);
                int controllerSize = this.mControllers.size();
                for (int index = 0; index < controllerSize; index++) {
                    ((AwareStateController) this.mControllers.get(index)).maybeStartTrackingJobLocked(job);
                }
            }
        }
        this.mHandler.obtainMessage(MSG_CHECK_JOB).sendToTarget();
    }

    private void cancelJobsForPkgLocked(String packageName) {
        synchronized (this.mLock) {
            int length = this.mJobs.size();
            if (length < 1) {
                AwareLog.w(TAG, "iaware_brjob no job need to cancel.");
                return;
            }
            for (int i = length; i > 0; i--) {
                AwareJobStatus jobStatus = (AwareJobStatus) this.mJobs.get(i - 1);
                if (packageName.equals(jobStatus.getReceiverPkg())) {
                    stopTrackingJobLocked(jobStatus);
                }
            }
        }
    }

    private void stopTrackingJobLocked(AwareJobStatus jobStatus) {
        if (this.DEBUG) {
            AwareLog.i(TAG, "iaware_brjob stopTrackingJobLocked begin");
        }
        this.mJobs.remove(jobStatus);
        int controllerSize = this.mControllers.size();
        for (int i = 0; i < controllerSize; i++) {
            ((AwareStateController) this.mControllers.get(i)).maybeStopTrackingJobLocked(jobStatus);
        }
    }

    private void maybeQueueReadyJobsForExecution() {
        if (this.DEBUG) {
            AwareLog.i(TAG, "iaware_brjob maybeQueueReadyJobsForExecution begin");
        }
        synchronized (this.mLock) {
            for (int i = this.mJobs.size() - 1; i >= 0; i--) {
                AwareJobStatus job = (AwareJobStatus) this.mJobs.get(i);
                if (job.isReady()) {
                    HwMtmBroadcastResourceManager.insertIawareOrderedBroadcast(job.getHwBroadcastRecord());
                    stopTrackingJobLocked(job);
                } else if (job.shouldCancelled()) {
                    stopTrackingJobLocked(job);
                }
            }
        }
    }

    public void dump(PrintWriter pw, String[] args) {
        if (pw != null && args != null) {
            String filterAction = "";
            String filterReceiver = "";
            if (args.length < 3 || args[2] == null) {
                dumpAllJobs(pw);
            } else if (PreciseIgnore.RECEIVER_ACTION_ELEMENT_KEY.equals(args[2])) {
                if (args.length < 4 || TextUtils.isEmpty(args[3])) {
                    pw.println("  no action option found, please input action name.");
                } else {
                    dumpJobsForAction(pw, args[3]);
                }
            } else if (PreciseIgnore.RECEIVER_ACTION_RECEIVER_ELEMENT_KEY.equals(args[2])) {
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
            for (AwareJobStatus job : this.mJobs) {
                job.dump(pw);
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:0x0050, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void dumpJobsForAction(PrintWriter pw, String action) {
        synchronized (this.mLock) {
            if (this.mJobs.size() == 0) {
                pw.println("    There is no job now.");
                return;
            }
            boolean hasAction = false;
            int listSize = this.mJobs.size();
            for (int i = 0; i < listSize; i++) {
                AwareJobStatus job = (AwareJobStatus) this.mJobs.get(i);
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

    /* JADX WARNING: Missing block: B:19:0x0050, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void dumpJobsForReceiver(PrintWriter pw, String receiver) {
        synchronized (this.mLock) {
            if (this.mJobs.size() == 0) {
                pw.println("    There is no job now.");
                return;
            }
            boolean hasReceiver = false;
            int listSize = this.mJobs.size();
            for (int i = 0; i < listSize; i++) {
                AwareJobStatus job = (AwareJobStatus) this.mJobs.get(i);
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
            ((AwareStateController) this.mControllers.get(i)).dump(pw);
        }
    }

    public void disableDebug() {
        this.DEBUG = false;
        int controllerSize = this.mControllers.size();
        for (int i = 0; i < controllerSize; i++) {
            ((AwareStateController) this.mControllers.get(i)).setDebug(false);
        }
        AwareJobStatus.setDebug(false);
    }

    public void enableDebug() {
        this.DEBUG = true;
        int controllerSize = this.mControllers.size();
        for (int i = 0; i < controllerSize; i++) {
            ((AwareStateController) this.mControllers.get(i)).setDebug(true);
        }
        AwareJobStatus.setDebug(true);
    }
}
