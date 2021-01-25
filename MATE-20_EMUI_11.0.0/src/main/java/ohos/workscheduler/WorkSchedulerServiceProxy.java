package ohos.workscheduler;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.workscheduler.WorkInfo;

public class WorkSchedulerServiceProxy implements IWorkSchedulerService {
    private static final int COMMAND_GET_WORK_STATUS = 3;
    private static final int COMMAND_START_WORK_NOW = 1;
    private static final int COMMAND_STOP_WORK = 2;
    private static final int ERR_NULL_EXCEPTION = -2;
    private static final int ERR_OK = 0;
    private static final int ERR_REMOTE_EXCEPTION = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkSchedulerServiceProxy");
    private static final String WORKSCHEDULER_INTERFACE_TOKEN = "ohos.workscheduler.IWorkSchedulerService";
    private final IRemoteObject remote;

    public WorkSchedulerServiceProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.workscheduler.IWorkSchedulerService
    public boolean startWorkNow(WorkInfo workInfo, boolean z) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        boolean z2 = false;
        MessageOption messageOption = new MessageOption(0);
        try {
            if (obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN)) {
                obtain.writeSequenceable(workInfo);
                boolean z3 = true;
                if (obtain.writeInt(z ? 1 : 0)) {
                    if (this.remote.sendRequest(1, obtain, obtain2, messageOption)) {
                        HiLog.debug(LOG_LABEL, "startWorkNow reply is %{public}d", Integer.valueOf(obtain2.readInt()));
                        if (obtain2.readInt() != 1) {
                            z3 = false;
                        }
                        z2 = z3;
                    } else {
                        HiLog.error(LOG_LABEL, "startWorkNow reply is false!!", new Object[0]);
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return z2;
                }
            }
            obtain.reclaim();
            obtain2.reclaim();
            return false;
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "startWorkNow RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.workscheduler.IWorkSchedulerService
    public boolean stopWork(WorkInfo workInfo, boolean z, boolean z2) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        boolean z3 = false;
        MessageOption messageOption = new MessageOption(0);
        try {
            if (obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN)) {
                obtain.writeSequenceable(workInfo);
                boolean z4 = true;
                if (obtain.writeInt(z ? 1 : 0)) {
                    if (obtain.writeInt(z2 ? 1 : 0)) {
                        if (this.remote.sendRequest(2, obtain, obtain2, messageOption)) {
                            HiLog.debug(LOG_LABEL, "stopWork reply is %{public}d", Integer.valueOf(obtain2.readInt()));
                            if (obtain2.readInt() != 1) {
                                z4 = false;
                            }
                            z3 = z4;
                        } else {
                            HiLog.error(LOG_LABEL, "stopWork reply is false!!", new Object[0]);
                        }
                        obtain.reclaim();
                        obtain2.reclaim();
                        return z3;
                    }
                }
            }
            obtain.reclaim();
            obtain2.reclaim();
            return false;
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "stopWork RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.workscheduler.IWorkSchedulerService
    public WorkInfo getWorkStatus(int i) {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption(0);
        WorkInfo workInfo = null;
        try {
            if (obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN)) {
                if (obtain.writeInt(i)) {
                    if (this.remote.sendRequest(3, obtain, obtain2, messageOption)) {
                        int readInt = obtain2.readInt();
                        if (!(readInt == -2 || readInt == -1)) {
                            workInfo = new WorkInfo.Builder().build();
                            obtain2.readSequenceable(workInfo);
                        }
                    } else {
                        HiLog.error(LOG_LABEL, "getWorkStatus reply is false!!", new Object[0]);
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    return workInfo;
                }
            }
            obtain.reclaim();
            obtain2.reclaim();
            return null;
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "getWorkStatus RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }
}
