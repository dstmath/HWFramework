package ohos.workscheduler;

import java.util.List;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.workscheduler.WorkInfo;

public abstract class WorkSchedulerServiceSkeleton extends RemoteObject implements IWorkSchedulerService {
    private static final int COMMAND_GET_ALL_WORK_STATUS = 4;
    private static final int COMMAND_GET_WORK_STATUS = 3;
    private static final int COMMAND_LAST_WORK_TIME_OUT = 6;
    private static final int COMMAND_START_WORK_NOW = 1;
    private static final int COMMAND_STOP_ALL_WORK_STATUS = 5;
    private static final int COMMAND_STOP_WORK = 2;
    private static final String DESCRIPTOR = "ohos.workscheduler.IWorkSchedulerService";
    private static final int ERR_NULL_EXCEPTION = -2;
    private static final int ERR_OK = 0;
    private static final int ERR_REMOTE_EXCEPTION = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, "WorkSchedulerService");
    private static final int MAX_WORK_INFO_LENGTH = 1024;

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
        switch (i) {
            case 1:
                WorkInfo build = new WorkInfo.Builder().build();
                messageParcel.readSequenceable(build);
                try {
                    boolean startWorkNow = startWorkNow(build, messageParcel.readInt() == 1);
                    writeInt = messageParcel2.writeInt(0) & true;
                    writeInt2 = messageParcel2.writeInt(startWorkNow ? 1 : 0);
                    break;
                } catch (RemoteException unused) {
                    return messageParcel2.writeInt(-1);
                }
            case 2:
                WorkInfo build2 = new WorkInfo.Builder().build();
                messageParcel.readSequenceable(build2);
                try {
                    boolean stopWork = stopWork(build2, messageParcel.readInt() == 1, messageParcel.readInt() == 1);
                    writeInt = messageParcel2.writeInt(0) & true;
                    writeInt2 = messageParcel2.writeInt(stopWork ? 1 : 0);
                    break;
                } catch (RemoteException unused2) {
                    return messageParcel2.writeInt(-1);
                }
            case 3:
                return handleGetWorkStatus(messageParcel, messageParcel2);
            case 4:
                return handleGetAllWorkStatus(messageParcel2);
            case 5:
                return handleStopAllWorkStatus(messageParcel2);
            case 6:
                return handleLastWorkTimeOut(messageParcel, messageParcel2);
            default:
                return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        return writeInt2 & writeInt;
    }

    private boolean handleGetWorkStatus(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            WorkInfo workStatus = getWorkStatus(messageParcel.readInt());
            if (workStatus == null) {
                return messageParcel2.writeInt(-2) & true;
            }
            boolean writeInt = messageParcel2.writeInt(0) & true;
            messageParcel2.writeSequenceable(workStatus);
            return writeInt;
        } catch (RemoteException unused) {
            return messageParcel2.writeInt(-1);
        }
    }

    private boolean handleGetAllWorkStatus(MessageParcel messageParcel) {
        HiLog.info(LOG_LABEL, "handleGetAllWorkStatus begin", new Object[0]);
        try {
            List<WorkInfo> obtainAllWorks = obtainAllWorks();
            if (obtainAllWorks == null) {
                return messageParcel.writeInt(-2) & true;
            }
            int size = obtainAllWorks.size();
            boolean writeInt = messageParcel.writeInt(0) & true & messageParcel.writeInt(size);
            if (size < 1024) {
                for (WorkInfo workInfo : obtainAllWorks) {
                    messageParcel.writeSequenceable(workInfo);
                }
            }
            return writeInt;
        } catch (RemoteException unused) {
            return messageParcel.writeInt(-1);
        }
    }

    private boolean handleStopAllWorkStatus(MessageParcel messageParcel) {
        try {
            boolean stopAndClearWorks = stopAndClearWorks();
            boolean writeInt = messageParcel.writeInt(0) & true;
            messageParcel.writeBoolean(stopAndClearWorks);
            return writeInt;
        } catch (RemoteException unused) {
            return messageParcel.writeInt(-1);
        }
    }

    private boolean handleLastWorkTimeOut(MessageParcel messageParcel, MessageParcel messageParcel2) {
        try {
            boolean isLastWorkTimeOut = isLastWorkTimeOut(messageParcel.readInt());
            HiLog.info(LOG_LABEL, "isLastWorkTimeOut result is %{public}b", Boolean.valueOf(isLastWorkTimeOut));
            boolean writeInt = messageParcel2.writeInt(0) & true;
            messageParcel2.writeBoolean(isLastWorkTimeOut);
            return writeInt;
        } catch (RemoteException unused) {
            return messageParcel2.writeInt(-1);
        }
    }
}
