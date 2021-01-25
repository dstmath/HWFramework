package ohos.workscheduler;

import ohos.aafwk.content.Intent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class WorkSchedulerProxy implements IWorkScheduler {
    private static final int COMMAND_ON_COMMON_EVENT = 3;
    private static final int COMMAND_ON_SEND_REMOTE = 4;
    private static final int COMMAND_ON_WORK_START = 1;
    private static final int COMMAND_ON_WORK_STOP = 2;
    private static final int ERR_OK = 0;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkSchedulerProxy");
    private static final String WORKSCHEDULER_INTERFACE_TOKEN = "ohos.workscheduler.IWorkScheduler";
    private final IRemoteObject remote;

    public WorkSchedulerProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.workscheduler.IWorkScheduler
    public void onWorkStart(WorkInfo workInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        int i = 0;
        MessageOption messageOption = new MessageOption(0);
        try {
            if (!obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN)) {
                obtain.reclaim();
                obtain2.reclaim();
                return;
            }
            obtain.writeSequenceable(workInfo);
            if (this.remote.sendRequest(1, obtain, obtain2, messageOption)) {
                i = obtain2.readInt();
            }
            obtain.reclaim();
            obtain2.reclaim();
            if (i != 0) {
                throw new RemoteException();
            }
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.workscheduler.IWorkScheduler
    public void onWorkStop(WorkInfo workInfo) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        int i = 0;
        MessageOption messageOption = new MessageOption(0);
        try {
            if (!obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN)) {
                obtain.reclaim();
                obtain2.reclaim();
                return;
            }
            obtain.writeSequenceable(workInfo);
            if (this.remote.sendRequest(2, obtain, obtain2, messageOption)) {
                i = obtain2.readInt();
            }
            obtain.reclaim();
            obtain2.reclaim();
            if (i != 0) {
                throw new RemoteException();
            }
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.workscheduler.IWorkScheduler
    public void onCommonEventTriggered(Intent intent) throws RemoteException {
        boolean z;
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        int i = 0;
        MessageOption messageOption = new MessageOption(0);
        try {
            if (obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN)) {
                if (intent == null) {
                    z = obtain.writeBoolean(false);
                } else {
                    boolean writeBoolean = obtain.writeBoolean(true);
                    obtain.writeSequenceable(intent);
                    z = writeBoolean;
                }
                if (!z) {
                    HiLog.error(LOG_LABEL, "onCommonEventTriggered write parcel error!!", new Object[0]);
                } else {
                    if (this.remote.sendRequest(3, obtain, obtain2, messageOption)) {
                        i = obtain2.readInt();
                    }
                    obtain.reclaim();
                    obtain2.reclaim();
                    if (i != 0) {
                        throw new RemoteException();
                    }
                    return;
                }
            }
            obtain.reclaim();
            obtain2.reclaim();
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }

    @Override // ohos.workscheduler.IWorkScheduler
    public boolean sendRemote(IRemoteObject iRemoteObject) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        int i = 0;
        MessageOption messageOption = new MessageOption(0);
        try {
            if (obtain.writeInterfaceToken(WORKSCHEDULER_INTERFACE_TOKEN) && obtain.writeRemoteObject(iRemoteObject)) {
                if (iRemoteObject.sendRequest(4, obtain, obtain2, messageOption)) {
                    i = obtain2.readInt();
                }
                obtain.reclaim();
                obtain2.reclaim();
                if (i == 0) {
                    return true;
                }
                throw new RemoteException();
            }
            obtain.reclaim();
            obtain2.reclaim();
            return false;
        } catch (RemoteException unused) {
            HiLog.error(LOG_LABEL, "RemoteException", new Object[0]);
        } catch (Throwable th) {
            obtain.reclaim();
            obtain2.reclaim();
            throw th;
        }
    }
}
