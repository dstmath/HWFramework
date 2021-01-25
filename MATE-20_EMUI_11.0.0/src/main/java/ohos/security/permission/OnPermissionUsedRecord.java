package ohos.security.permission;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class OnPermissionUsedRecord extends RemoteObject implements IRemoteBroker {
    private static final String DESCRIPTOR = "ohos.security.permission.OnPermissionUsedRecord";
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_DPERMISSION, "OnUsingPermissionReminderCallback");
    private static final int ON_QUERIED = 0;
    private static final int SUB_DOMAIN_SECURITY_DPERMISSION = 218115841;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public abstract void onQueried(int i, QueryPermissionUsedResult queryPermissionUsedResult) throws RemoteException;

    public OnPermissionUsedRecord() {
        super(DESCRIPTOR);
    }

    @Override // ohos.rpc.RemoteObject
    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            return false;
        }
        HiLog.debug(LABEL, "onRemoteRequest code: %{public}d", Integer.valueOf(i));
        String readInterfaceToken = messageParcel.readInterfaceToken();
        if (!DESCRIPTOR.equals(readInterfaceToken)) {
            HiLog.error(LABEL, "receive unexpected descriptor: %{public}s", readInterfaceToken);
            return false;
        } else if (i == 0) {
            int readInt = messageParcel.readInt();
            QueryPermissionUsedResult queryPermissionUsedResult = new QueryPermissionUsedResult();
            messageParcel.readSequenceable(queryPermissionUsedResult);
            onQueried(readInt, queryPermissionUsedResult);
            return true;
        } else {
            HiLog.warn(LABEL, "onRemoteRequest unknown code", new Object[0]);
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
    }
}
