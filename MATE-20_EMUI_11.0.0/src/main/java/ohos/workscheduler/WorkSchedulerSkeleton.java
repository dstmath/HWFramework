package ohos.workscheduler;

import ohos.aafwk.content.Intent;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.workscheduler.WorkInfo;

public abstract class WorkSchedulerSkeleton extends RemoteObject implements IWorkScheduler {
    private static final int COMMAND_ON_COMMON_EVENT = 3;
    private static final int COMMAND_ON_SEND_REMOTE = 4;
    private static final int COMMAND_ON_WORK_START = 1;
    private static final int COMMAND_ON_WORK_STOP = 2;
    private static final String DESCRIPTOR = "ohos.workscheduler.IWorkScheduler";
    private static final int ERR_OK = 0;
    private static final int ERR_RUNTIME_EXCEPTION = -1;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public WorkSchedulerSkeleton(String str) {
        super(str);
    }

    public static IWorkScheduler asInterface(IRemoteObject iRemoteObject) {
        if (iRemoteObject == null) {
            return null;
        }
        IRemoteBroker queryLocalInterface = iRemoteObject.queryLocalInterface(DESCRIPTOR);
        if (queryLocalInterface == null) {
            return new WorkSchedulerProxy(iRemoteObject);
        }
        if (queryLocalInterface instanceof IWorkScheduler) {
            return (IWorkScheduler) queryLocalInterface;
        }
        return null;
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null || !DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            return false;
        }
        if (i == 1) {
            WorkInfo build = new WorkInfo.Builder().build();
            messageParcel.readSequenceable(build);
            try {
                onWorkStart(build);
                return messageParcel2.writeInt(0);
            } catch (RemoteException unused) {
                return messageParcel2.writeInt(-1);
            }
        } else if (i == 2) {
            WorkInfo build2 = new WorkInfo.Builder().build();
            messageParcel.readSequenceable(build2);
            try {
                onWorkStop(build2);
                return messageParcel2.writeInt(0);
            } catch (RemoteException unused2) {
                return messageParcel2.writeInt(-1);
            }
        } else if (i == 3) {
            Intent intent = null;
            if (messageParcel.readBoolean()) {
                intent = new Intent();
                messageParcel.readSequenceable(intent);
            }
            try {
                onCommonEventTriggered(intent);
                return messageParcel2.writeInt(0);
            } catch (RemoteException unused3) {
                return messageParcel2.writeInt(-1);
            }
        } else if (i != 4) {
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            try {
                if (!sendRemote(messageParcel.readRemoteObject())) {
                    return messageParcel2.writeInt(-1);
                }
                return messageParcel2.writeInt(0);
            } catch (RemoteException unused4) {
                return messageParcel2.writeInt(-1);
            }
        }
    }
}
