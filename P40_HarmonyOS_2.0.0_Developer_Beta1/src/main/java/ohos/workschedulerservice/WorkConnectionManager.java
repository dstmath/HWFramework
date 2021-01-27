package ohos.workschedulerservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.UserHandle;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.utils.IntentConverter;
import ohos.bundle.ShellInfo;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IPCAdapter;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.workscheduler.IWorkScheduler;
import ohos.workscheduler.WorkSchedulerProxy;
import ohos.workschedulerservice.controller.WorkStatus;

public class WorkConnectionManager {
    private static final int BIND_FAILED = 3;
    private static final int DESTORY_CONNECTION = 2;
    private static final int EXECUTE_TIMEOUT = 1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkConnectionManager");
    private static final int MAX_CONNECTION_SIZE = 5;
    private static final long MAX_EXECUTE_TIME = 180000;
    private Context context;
    private final Object lock;
    private volatile int maxConnectionSize;
    private WorkConnectionHandler workConnectionHandler;
    private final WorkEventTracker workEventTracker;
    private WorkQueueManager workQueueManager;
    private final HashMap<WorkStatus, WorkServiceConnection> workServiceConnections;

    /* access modifiers changed from: private */
    public static class Holder {
        static WorkConnectionManager workConnectionManager = new WorkConnectionManager();

        private Holder() {
        }
    }

    private WorkConnectionManager() {
        this.workEventTracker = new WorkEventTracker();
        this.workServiceConnections = new HashMap<>();
        this.lock = new Object();
        this.maxConnectionSize = 5;
    }

    public static WorkConnectionManager getInstance() {
        return Holder.workConnectionManager;
    }

    public void init(Context context2, WorkQueueManager workQueueManager2, EventRunner eventRunner) {
        if (context2 == null || workQueueManager2 == null || eventRunner == null) {
            HiLog.error(LOG_LABEL, "context or workQueueManager or runner is null, init failed", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            this.context = context2;
            this.workQueueManager = workQueueManager2;
            this.workConnectionHandler = new WorkConnectionHandler(eventRunner);
        }
    }

    public void updateMaxConnections(int i) {
        synchronized (this.lock) {
            this.maxConnectionSize = i;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasConnections() {
        boolean z;
        synchronized (this.lock) {
            z = this.workServiceConnections.size() < this.maxConnectionSize;
        }
        return z;
    }

    public void onWorkStartEvent(WorkStatus workStatus) {
        if (workStatus == null || workStatus.getCurrentState() != 0) {
            HiLog.error(LOG_LABEL, "onWorkStartEvent WorkStatus is null or not WORK_IS_PENDING", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            if (this.workServiceConnections.size() < this.maxConnectionSize) {
                if (this.workServiceConnections.get(workStatus) == null) {
                    WorkServiceConnection workServiceConnection = new WorkServiceConnection(workStatus);
                    if (workServiceConnection.onWorkStartEvent()) {
                        this.workEventTracker.noteOnWorkStartEvent(workStatus);
                        this.workServiceConnections.put(workStatus, workServiceConnection);
                        workStatus.markCurrentState(1);
                        if (!workStatus.onlyHasRepeatCondition()) {
                            HiLog.debug(LOG_LABEL, "update runtime in onWorkStartEvent!", new Object[0]);
                            workStatus.updateRunTimes(1);
                        }
                        if (this.workConnectionHandler != null) {
                            workStatus.setTimeOut(false);
                            this.workConnectionHandler.sendEvent(InnerEvent.get(1, workStatus), MAX_EXECUTE_TIME);
                        }
                    } else {
                        HiLog.error(LOG_LABEL, "WorkServiceConnection onWorkStart failed", new Object[0]);
                        if (this.workConnectionHandler != null) {
                            this.workConnectionHandler.sendEvent(InnerEvent.get(3, workStatus));
                        }
                    }
                }
            }
        }
    }

    public void onWorkStopEvent(WorkStatus workStatus) {
        if (workStatus == null || workStatus.getCurrentState() != 1) {
            HiLog.error(LOG_LABEL, "onWorkStopEvent WorkStatus is null or not WORK_IS_PENDING", new Object[0]);
            return;
        }
        synchronized (this.lock) {
            WorkServiceConnection workServiceConnection = this.workServiceConnections.get(workStatus);
            if (workServiceConnection != null) {
                if (!workServiceConnection.onWorkStopEvent()) {
                    HiLog.error(LOG_LABEL, "WorkServiceConnection onWorkStop failed", new Object[0]);
                }
            }
        }
    }

    public void cleanConnection(WorkStatus workStatus) {
        synchronized (this.lock) {
            WorkServiceConnection workServiceConnection = this.workServiceConnections.get(workStatus);
            if (workServiceConnection != null) {
                workStatus.markCurrentState(3);
                this.workServiceConnections.remove(workStatus);
                if (this.workConnectionHandler != null) {
                    this.workConnectionHandler.removeEvent(1, workStatus);
                }
                this.workEventTracker.noteWorkConnectionEndEvent(workStatus);
                if (this.context == null) {
                    HiLog.error(LOG_LABEL, "context is null", new Object[0]);
                    return;
                }
                try {
                    this.context.unbindService(workServiceConnection);
                } catch (SecurityException unused) {
                    HiLog.error(LOG_LABEL, "cleanConnection unbindService SecurityException!", new Object[0]);
                }
            }
        }
    }

    public void dumpConnectionsStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("Connections Status dump info:");
        synchronized (this.lock) {
            printWriter.println(str + "maxConnectionSize:" + this.maxConnectionSize);
            printWriter.println(str + "connection size:" + this.workServiceConnections.size());
        }
        this.workEventTracker.dumpHistory(printWriter, str);
    }

    /* access modifiers changed from: private */
    public class WorkServiceConnection implements ServiceConnection {
        private static final String BUNDLE_NAME_DOT = ".";
        private static final String SERVICE_SHELL_SUFFIX = "ShellService";
        private IWorkScheduler workSchedulerProxy;
        private WorkStatus workStatus;

        public WorkServiceConnection(WorkStatus workStatus2) {
            if (workStatus2 != null) {
                this.workStatus = workStatus2;
                return;
            }
            throw new IllegalArgumentException("workStatus is null");
        }

        public boolean onWorkStartEvent() {
            Intent intent = new Intent();
            intent.setElement(this.workStatus.getWork().getElementInfo());
            ShellInfo shellInfo = new ShellInfo();
            shellInfo.setPackageName(intent.getElement().getBundleName());
            shellInfo.setType(ShellInfo.ShellType.SERVICE);
            shellInfo.setName(intent.getElement().getBundleName() + BUNDLE_NAME_DOT + intent.getElement().getAbilityName() + SERVICE_SHELL_SUFFIX);
            Optional<android.content.Intent> checkIntentIsValid = checkIntentIsValid(intent, shellInfo);
            if (!checkIntentIsValid.isPresent()) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "convert android intent fail", new Object[0]);
                return false;
            } else if (WorkConnectionManager.this.context == null) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "context is null", new Object[0]);
                return false;
            } else {
                try {
                    return WorkConnectionManager.this.context.bindServiceAsUser(checkIntentIsValid.get(), this, 1, UserHandle.CURRENT);
                } catch (SecurityException unused) {
                    HiLog.error(WorkConnectionManager.LOG_LABEL, "bind Service SecurityException happens!", new Object[0]);
                    return false;
                }
            }
        }

        public boolean onWorkStopEvent() {
            IWorkScheduler iWorkScheduler = this.workSchedulerProxy;
            if (iWorkScheduler == null) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "onWorkStop workSchedulerProxy is null", new Object[0]);
                return false;
            }
            try {
                iWorkScheduler.onWorkStop(this.workStatus.getWork());
                HiLog.debug(WorkConnectionManager.LOG_LABEL, "workSchedulerProxy onWorkStop end", new Object[0]);
                return true;
            } catch (RemoteException unused) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "RemoteException", new Object[0]);
                return false;
            }
        }

        private Optional<android.content.Intent> checkIntentIsValid(Intent intent, ShellInfo shellInfo) {
            Optional<android.content.Intent> createAndroidIntent = IntentConverter.createAndroidIntent(intent, shellInfo);
            if (!createAndroidIntent.isPresent() || createAndroidIntent.get().getComponent() == null) {
                return Optional.empty();
            }
            HiLog.info(WorkConnectionManager.LOG_LABEL, "intent convert ok", new Object[0]);
            return createAndroidIntent;
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HiLog.info(WorkConnectionManager.LOG_LABEL, "onServiceConnected called", new Object[0]);
            if (componentName == null || iBinder == null) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "onServiceConnected ComponentName or IBinder service is null", new Object[0]);
                return;
            }
            HiLog.info(WorkConnectionManager.LOG_LABEL, "onServiceConnected ComponentName package:%{public}s, class name:%{public}s", componentName.getPackageName(), componentName.getClassName());
            Optional<IRemoteObject> translateToIRemoteObject = IPCAdapter.translateToIRemoteObject(iBinder);
            if (!translateToIRemoteObject.isPresent()) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "convert IRemoteObject fail", new Object[0]);
                return;
            }
            this.workSchedulerProxy = new WorkSchedulerProxy(translateToIRemoteObject.get());
            try {
                this.workSchedulerProxy.onWorkStart(this.workStatus.getWork());
                HiLog.debug(WorkConnectionManager.LOG_LABEL, "workSchedulerProxy onWorkStart end", new Object[0]);
            } catch (RemoteException unused) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "RemoteException", new Object[0]);
            }
            HiLog.info(WorkConnectionManager.LOG_LABEL, "onServiceConnected end", new Object[0]);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            if (componentName == null) {
                HiLog.error(WorkConnectionManager.LOG_LABEL, "onServiceDisconnected ComponentName is null", new Object[0]);
                return;
            }
            HiLog.debug(WorkConnectionManager.LOG_LABEL, "onServiceDisconnected ComponentName package:%{public}s, class name:%{public}s", componentName.getPackageName(), componentName.getClassName());
            synchronized (WorkConnectionManager.this.lock) {
                if (WorkConnectionManager.this.workConnectionHandler != null) {
                    WorkConnectionManager.this.workConnectionHandler.sendEvent(InnerEvent.get(2, this.workStatus));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class WorkConnectionHandler extends EventHandler {
        private WorkConnectionHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            WorkConnectionManager.super.processEvent(innerEvent);
            if (innerEvent != null) {
                int i = innerEvent.eventId;
                if (i != 1) {
                    if (i != 2) {
                        if (i != 3) {
                            HiLog.debug(WorkConnectionManager.LOG_LABEL, "processEvent default", new Object[0]);
                        } else if (innerEvent.object instanceof WorkStatus) {
                            WorkStatus workStatus = (WorkStatus) innerEvent.object;
                            HiLog.info(WorkConnectionManager.LOG_LABEL, "BIND_FAILED event called! workid:%{public}d", Integer.valueOf(workStatus.getWorkId()));
                            if (workStatus.getCurrentState() == 0 && WorkConnectionManager.this.workQueueManager != null) {
                                WorkConnectionManager.this.workQueueManager.removeWorkInPendingWorks(workStatus);
                            }
                        }
                    } else if (innerEvent.object instanceof WorkStatus) {
                        WorkStatus workStatus2 = (WorkStatus) innerEvent.object;
                        HiLog.info(WorkConnectionManager.LOG_LABEL, "DESTORY_CONNECTION event called! workid:%{public}d", Integer.valueOf(workStatus2.getWorkId()));
                        if (workStatus2.getCurrentState() == 1 && WorkConnectionManager.this.workQueueManager != null) {
                            WorkConnectionManager.this.workQueueManager.removeWorkInPendingWorks(workStatus2);
                        }
                    }
                } else if (innerEvent.object instanceof WorkStatus) {
                    WorkStatus workStatus3 = (WorkStatus) innerEvent.object;
                    HiLog.info(WorkConnectionManager.LOG_LABEL, "EXECUTE_TIMEOUT event called! workid:%{public}d", Integer.valueOf(workStatus3.getWorkId()));
                    if (workStatus3.getCurrentState() == 1 && checkRunningTime(workStatus3) && WorkConnectionManager.this.workQueueManager != null) {
                        WorkConnectionManager.this.workQueueManager.removeWorkInPendingWorks(workStatus3);
                        workStatus3.setTimeOut(true);
                    }
                }
            }
        }

        private boolean checkRunningTime(WorkStatus workStatus) {
            if (workStatus.getStartTrackingTime() != 0 && SystemClock.elapsedRealtime() - workStatus.getStartTrackingTime() >= WorkConnectionManager.MAX_EXECUTE_TIME) {
                return true;
            }
            return false;
        }
    }
}
