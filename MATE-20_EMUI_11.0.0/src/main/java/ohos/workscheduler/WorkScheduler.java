package ohos.workscheduler;

import java.lang.ref.WeakReference;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

public abstract class WorkScheduler extends Ability {
    private static final int COMMON_EVENT_ID = 2;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkScheduler");
    private static final int START_EVENT_ID = 1;
    private static final int STOP_EVENT_ID = 0;
    private static volatile IRemoteObject proxyConnect;
    private static IWorkSchedulerService workSchedulerService;
    private EventHandler handler;
    private EventRunner runner;
    private volatile IRemoteObject stubConnect;

    public void onCommonEventTriggered(Intent intent) {
    }

    public abstract void onWorkStart(WorkInfo workInfo);

    public abstract void onWorkStop(WorkInfo workInfo);

    /* access modifiers changed from: protected */
    public IRemoteObject onConnect(Intent intent) {
        WorkScheduler.super.onConnect(intent);
        HiLog.debug(LOG_LABEL, "Receive WorkSchedulerService Connect Request!", new Object[0]);
        if (this.stubConnect == null) {
            this.stubConnect = new StubInterface(this);
            this.runner = EventRunner.create();
            this.handler = new WorkConnectHandler(this.runner);
        }
        return getSubIRemoteObject();
    }

    /* access modifiers changed from: protected */
    public void onDisconnect(Intent intent) {
        WorkScheduler.super.onDisconnect(intent);
        HiLog.debug(LOG_LABEL, "Receive WorkSchedulerService Disconnect Request new!", new Object[0]);
        this.stubConnect = null;
    }

    static class StubInterface extends WorkSchedulerSkeleton {
        final WeakReference<WorkScheduler> mSchedulerService;

        StubInterface(WorkScheduler workScheduler) {
            super("ohos.workscheduler.IWorkScheduler");
            this.mSchedulerService = new WeakReference<>(workScheduler);
        }

        @Override // ohos.workscheduler.IWorkScheduler
        public void onWorkStart(WorkInfo workInfo) throws RemoteException {
            WorkScheduler workScheduler = this.mSchedulerService.get();
            if (workScheduler != null) {
                try {
                    workScheduler.handler.sendEvent(InnerEvent.get(1, workInfo));
                } catch (IllegalArgumentException unused) {
                    HiLog.error(WorkScheduler.LOG_LABEL, "[StubInterface] Failed to send Start event!", new Object[0]);
                }
            } else {
                HiLog.error(WorkScheduler.LOG_LABEL, "onWorkStop can not get scheduler!", new Object[0]);
            }
        }

        @Override // ohos.workscheduler.IWorkScheduler
        public void onWorkStop(WorkInfo workInfo) throws RemoteException {
            WorkScheduler workScheduler = this.mSchedulerService.get();
            if (workScheduler != null) {
                try {
                    workScheduler.handler.sendEvent(InnerEvent.get(0, workInfo));
                } catch (IllegalArgumentException unused) {
                    HiLog.error(WorkScheduler.LOG_LABEL, "[StubInterface] Failed to send Stop event!", new Object[0]);
                }
            } else {
                HiLog.error(WorkScheduler.LOG_LABEL, "onWorkStop can not get scheduler!", new Object[0]);
            }
        }

        @Override // ohos.workscheduler.IWorkScheduler
        public void onCommonEventTriggered(Intent intent) throws RemoteException {
            WorkScheduler workScheduler = this.mSchedulerService.get();
            if (workScheduler == null) {
                HiLog.error(WorkScheduler.LOG_LABEL, "onCommonEventTriggered can not get scheduler!", new Object[0]);
            } else if (intent == null) {
                HiLog.error(WorkScheduler.LOG_LABEL, "intent is null, can not trigger!", new Object[0]);
            } else {
                try {
                    workScheduler.handler.sendEvent(InnerEvent.get(2, intent));
                } catch (IllegalArgumentException unused) {
                    HiLog.error(WorkScheduler.LOG_LABEL, "[StubInterface] Failed to send Common event!", new Object[0]);
                }
            }
        }

        @Override // ohos.workscheduler.IWorkScheduler
        public boolean sendRemote(IRemoteObject iRemoteObject) throws RemoteException {
            if (this.mSchedulerService.get() == null) {
                return false;
            }
            if (iRemoteObject == null) {
                HiLog.error(WorkScheduler.LOG_LABEL, "[StubInterface] Failed to sendRemote, remote is null!", new Object[0]);
                return false;
            }
            IRemoteObject unused = WorkScheduler.proxyConnect = iRemoteObject;
            return true;
        }
    }

    private final class WorkConnectHandler extends EventHandler {
        private WorkConnectHandler(EventRunner eventRunner) {
            super(eventRunner);
        }

        public void processEvent(InnerEvent innerEvent) {
            WorkScheduler.super.processEvent(innerEvent);
            if (innerEvent != null) {
                if (innerEvent.eventId == 1) {
                    if (innerEvent.object instanceof WorkInfo) {
                        try {
                            WorkScheduler.this.onWorkStart((WorkInfo) innerEvent.object);
                        } catch (Exception unused) {
                            HiLog.error(WorkScheduler.LOG_LABEL, "WorkConnectHandler: AtomicAbility unable to handle onStartWork.", new Object[0]);
                        }
                    }
                } else if (innerEvent.eventId == 0) {
                    if (innerEvent.object instanceof WorkInfo) {
                        try {
                            WorkScheduler.this.onWorkStop((WorkInfo) innerEvent.object);
                        } catch (Exception unused2) {
                            HiLog.error(WorkScheduler.LOG_LABEL, "WorkConnectHandler: AtomicAbility unable to handle onStopWork.", new Object[0]);
                        }
                    }
                } else if (innerEvent.eventId != 2) {
                    HiLog.error(WorkScheduler.LOG_LABEL, "WorkConnectHandler: unknown event ID, event process failed!!", new Object[0]);
                } else if (innerEvent.object instanceof Intent) {
                    Intent intent = (Intent) innerEvent.object;
                    HiLog.debug(WorkScheduler.LOG_LABEL, "WorkConnectHandler: transfering Intent for onCommonEvent.", new Object[0]);
                    try {
                        WorkScheduler.this.onCommonEventTriggered(intent);
                    } catch (Exception unused3) {
                        HiLog.error(WorkScheduler.LOG_LABEL, "WorkConnectHandler: AtomicAbility unable to handle onCommonEvent.", new Object[0]);
                    }
                }
            }
        }
    }

    private IRemoteObject getSubIRemoteObject() {
        return this.stubConnect;
    }

    private static boolean prepareServiceConnect(WorkInfo workInfo) {
        if (workInfo == null || !workInfo.isWorkInfoValid()) {
            HiLog.error(LOG_LABEL, "WorkInfo is invalid, prepare failed!!", new Object[0]);
            return false;
        }
        if (proxyConnect == null) {
            proxyConnect = SysAbilityManager.getSysAbility(SystemAbilityDefinition.WORK_SCHEDULE_SERVICE_ID);
            if (proxyConnect == null) {
                HiLog.error(LOG_LABEL, "Get WorkScheduler Ability failed!!", new Object[0]);
                return false;
            }
        }
        workSchedulerService = WorkSchedulerServiceSkeleton.asInterface(proxyConnect);
        if (workSchedulerService != null) {
            return true;
        }
        HiLog.error(LOG_LABEL, "Connect WorkSchedulerSerice failed!!", new Object[0]);
        return false;
    }

    public static final boolean startWork(WorkInfo workInfo, boolean z) throws RemoteException {
        if (!prepareServiceConnect(workInfo)) {
            HiLog.error(LOG_LABEL, "Start work failed!!", new Object[0]);
            return false;
        }
        HiLog.debug(LOG_LABEL, "Start work now, work id:%{public}d, %{public}b", Integer.valueOf(workInfo.getCurrentWorkID()), Boolean.valueOf(z));
        return workSchedulerService.startWorkNow(workInfo, z);
    }

    public static final boolean stopWork(WorkInfo workInfo) throws RemoteException {
        if (!prepareServiceConnect(workInfo)) {
            HiLog.error(LOG_LABEL, "Stop work failed!!", new Object[0]);
            return false;
        }
        HiLog.debug(LOG_LABEL, "Stop work now, work id:%{public}d", Integer.valueOf(workInfo.getCurrentWorkID()));
        return workSchedulerService.stopWork(workInfo, false, false);
    }

    public static final boolean stopAndCancelWork(WorkInfo workInfo) throws RemoteException {
        if (!prepareServiceConnect(workInfo)) {
            HiLog.error(LOG_LABEL, "Stop work failed!!", new Object[0]);
            return false;
        }
        HiLog.debug(LOG_LABEL, "Stop and cancel work now, work id:%{public}d", Integer.valueOf(workInfo.getCurrentWorkID()));
        return workSchedulerService.stopWork(workInfo, true, false);
    }

    public static final WorkInfo getWorkStatus(int i) throws RemoteException {
        if (i <= 0) {
            HiLog.error(LOG_LABEL, "WorkInfo is invalid, start Work failed!!", new Object[0]);
            return null;
        }
        HiLog.debug(LOG_LABEL, "Get work status, work id:%{public}d", Integer.valueOf(i));
        return workSchedulerService.getWorkStatus(i);
    }
}
