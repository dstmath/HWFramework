package ohos.workschedulerservice;

import android.content.Context;
import com.huawei.ohos.workscheduleradapter.WorkSchedulerCommon;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCSkeleton;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.workscheduler.WorkInfo;
import ohos.workscheduler.WorkSchedulerServiceSkeleton;

public class WorkSchedulerService {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkSchedulerService");
    private static final int WORK_SCHEDULE_SERVICE_SA = 1904;
    private Context context = null;
    private WorkQueueManager workQueueMgr;
    private volatile StubInterface workSched;

    private boolean initServiceEnvironment() {
        HiLog.info(LOG_LABEL, "initServiceEnvironment start!", new Object[0]);
        this.workSched = new StubInterface(this);
        this.workQueueMgr = new WorkQueueManager(this.context);
        if (this.workQueueMgr.init()) {
            return true;
        }
        HiLog.error(LOG_LABEL, "WorkQueueManager init failed!!", new Object[0]);
        return false;
    }

    public WorkSchedulerService(Context context2) {
        HiLog.info(LOG_LABEL, "Instantiates WorkSchedulerService begin!", new Object[0]);
        if (context2 == null) {
            HiLog.error(LOG_LABEL, "contextInfo is null, instantiates failed!!", new Object[0]);
            return;
        }
        this.context = context2;
        HiLog.info(LOG_LABEL, "Instantiates WorkSchedulerService success!", new Object[0]);
    }

    public void start() {
        HiLog.info(LOG_LABEL, "Add SystemAbility begin!", new Object[0]);
        if (this.context == null) {
            HiLog.error(LOG_LABEL, "Sevice is not instantiated, start failed!!", new Object[0]);
        } else if (!initServiceEnvironment()) {
            HiLog.error(LOG_LABEL, "Sevice is not ready to add in SystemAbility, start failed!!", new Object[0]);
        } else {
            HiLog.info(LOG_LABEL, "Add SystemAbility result is %{public}d", Integer.valueOf(SysAbilityManager.addSysAbility(1904, this.workSched.asObject())));
        }
    }

    /* access modifiers changed from: private */
    public final class StubInterface extends WorkSchedulerServiceSkeleton {
        final WeakReference<WorkSchedulerService> mSchedulerService;

        StubInterface(WorkSchedulerService workSchedulerService) {
            super("ohos.workscheduler.IWorkSchedulerService");
            this.mSchedulerService = new WeakReference<>(workSchedulerService);
        }

        @Override // ohos.workscheduler.IWorkSchedulerService
        public boolean startWorkNow(WorkInfo workInfo, boolean z) throws RemoteException {
            if (this.mSchedulerService.get() == null || !workInfo.isWorkInfoValid()) {
                HiLog.error(WorkSchedulerService.LOG_LABEL, "startWorkNow failed, cannot get service or work is inValid!!", new Object[0]);
                return false;
            }
            HiLog.info(WorkSchedulerService.LOG_LABEL, "start work: %{public}s, %{public}d.", workInfo.getBundleName(), Integer.valueOf(workInfo.getCurrentWorkID()));
            int callingUid = IPCSkeleton.getCallingUid();
            int userIdFromUid = WorkSchedulerCommon.getUserIdFromUid(callingUid);
            if (WorkSchedulerCommon.checkClientPermission(workInfo, callingUid)) {
                return WorkSchedulerService.this.workQueueMgr.tryStartSignWork(workInfo, callingUid, userIdFromUid);
            }
            HiLog.error(WorkSchedulerService.LOG_LABEL, "checkClientPermission failed, not allow to start work", new Object[0]);
            return false;
        }

        @Override // ohos.workscheduler.IWorkSchedulerService
        public boolean stopWork(WorkInfo workInfo, boolean z, boolean z2) throws RemoteException {
            if (this.mSchedulerService.get() == null || !workInfo.isWorkInfoValid()) {
                HiLog.error(WorkSchedulerService.LOG_LABEL, "stopWork failed, cannot get service!!", new Object[0]);
                return false;
            }
            HiLog.info(WorkSchedulerService.LOG_LABEL, "stop work: %{public}s, %{public}d.", workInfo.getBundleName(), Integer.valueOf(workInfo.getCurrentWorkID()));
            int callingUid = IPCSkeleton.getCallingUid();
            int userIdFromUid = WorkSchedulerCommon.getUserIdFromUid(callingUid);
            if (WorkSchedulerCommon.checkClientPermission(workInfo, callingUid)) {
                return WorkSchedulerService.this.workQueueMgr.tryStopSignWork(workInfo, callingUid, userIdFromUid, z);
            }
            HiLog.error(WorkSchedulerService.LOG_LABEL, "checkClientPermission failed, not allow to stop work", new Object[0]);
            return false;
        }

        @Override // ohos.workscheduler.IWorkSchedulerService
        public WorkInfo getWorkStatus(int i) throws RemoteException {
            if (this.mSchedulerService.get() == null) {
                HiLog.error(WorkSchedulerService.LOG_LABEL, "get workstatus failed, cannot get service!!!", new Object[0]);
                return null;
            }
            int callingUid = IPCSkeleton.getCallingUid();
            HiLog.info(WorkSchedulerService.LOG_LABEL, "get work: %{public}d, %{public}d， %{public}d.", Integer.valueOf(callingUid), Integer.valueOf(i), Integer.valueOf(WorkSchedulerCommon.getUserIdFromUid(callingUid)));
            return WorkSchedulerService.this.workQueueMgr.tryGetWorkStatus(callingUid, i);
        }

        @Override // ohos.rpc.RemoteObject
        public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            int callingUid = IPCSkeleton.getCallingUid();
            if (callingUid != 0 && callingUid != 1000 && callingUid != 2000) {
                HiLog.warn(WorkSchedulerService.LOG_LABEL, "request caller is illegal uid: %{public}d", Integer.valueOf(callingUid));
            } else if (fileDescriptor != null && printWriter != null) {
                printWriter.println("WorkScheduler Dump info:");
                WorkSchedulerService.this.workQueueMgr.dump(printWriter, strArr);
            }
        }
    }
}
