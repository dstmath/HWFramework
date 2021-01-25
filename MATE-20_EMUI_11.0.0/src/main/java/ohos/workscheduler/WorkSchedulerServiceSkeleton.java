package ohos.workscheduler;

import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.workscheduler.WorkInfo;

public abstract class WorkSchedulerServiceSkeleton extends RemoteObject implements IWorkSchedulerService {
    private static final int COMMAND_GET_WORK_STATUS = 3;
    private static final int COMMAND_START_WORK_NOW = 1;
    private static final int COMMAND_STOP_WORK = 2;
    private static final String DESCRIPTOR = "ohos.workscheduler.IWorkSchedulerService";
    private static final int ERR_NULL_EXCEPTION = -2;
    private static final int ERR_OK = 0;
    private static final int ERR_REMOTE_EXCEPTION = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkSchedulerService");

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public WorkSchedulerServiceSkeleton(String str) {
        super(str);
    }

    public static IWorkSchedulerService asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new WorkSchedulerServiceProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IWorkSchedulerService) {
            return (IWorkSchedulerService) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        boolean writeInt;
        boolean writeInt2;
        if (messageParcel == null || messageParcel2 == null || !DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            return false;
        }
        if (i == 1) {
            WorkInfo build = new WorkInfo.Builder().build();
            messageParcel.readSequenceable(build);
            try {
                boolean startWorkNow = startWorkNow(build, messageParcel.readInt() == 1);
                writeInt = messageParcel2.writeInt(0) & true;
                writeInt2 = messageParcel2.writeInt(startWorkNow ? 1 : 0);
            } catch (RemoteException unused) {
                return messageParcel2.writeInt(-1);
            }
        } else if (i == 2) {
            WorkInfo build2 = new WorkInfo.Builder().build();
            messageParcel.readSequenceable(build2);
            try {
                boolean stopWork = stopWork(build2, messageParcel.readInt() == 1, messageParcel.readInt() == 1);
                writeInt = messageParcel2.writeInt(0) & true;
                writeInt2 = messageParcel2.writeInt(stopWork ? 1 : 0);
            } catch (RemoteException unused2) {
                return messageParcel2.writeInt(-1);
            }
        } else if (i != 3) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            int readInt = messageParcel.readInt();
            new WorkInfo.Builder().build();
            try {
                WorkInfo workStatus = getWorkStatus(readInt);
                if (workStatus == null) {
                    return messageParcel2.writeInt(-2) & true;
                }
                boolean writeInt3 = messageParcel2.writeInt(0) & true;
                messageParcel2.writeSequenceable(workStatus);
                return writeInt3;
            } catch (RemoteException unused3) {
                return messageParcel2.writeInt(-1);
            }
        }
        return writeInt2 & writeInt;
    }
}
