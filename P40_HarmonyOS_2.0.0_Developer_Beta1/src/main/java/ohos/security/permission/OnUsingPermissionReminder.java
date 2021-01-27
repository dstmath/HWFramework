package ohos.security.permission;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public abstract class OnUsingPermissionReminder extends RemoteObject implements IRemoteBroker {
    private static final String DESCRIPTOR = "ohos.security.permission.OnUsingPermissionReminder";
    private static final HiLogLabel LABEL = new HiLogLabel(3, SUB_DOMAIN_SECURITY_DPERMISSION, "OnUsingPermissionReminder");
    private static final int SUB_DOMAIN_SECURITY_DPERMISSION = 218115841;
    private static final int TRANS_START_USING_PERMISSION = 0;
    private static final int TRANS_STOP_USING_PERMISSION = 1;

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this;
    }

    public abstract void startUsingPermission(PermissionReminderInfo permissionReminderInfo) throws RemoteException;

    public abstract void stopUsingPermission(PermissionReminderInfo permissionReminderInfo) throws RemoteException;

    public OnUsingPermissionReminder() {
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
        }
        if (i == 0) {
            PermissionReminderInfo permissionReminderInfo = new PermissionReminderInfo();
            messageParcel.readSequenceable(permissionReminderInfo);
            startUsingPermission(permissionReminderInfo);
        } else if (i != 1) {
            HiLog.warn(LABEL, "onRemoteRequest unknown code", new Object[0]);
            return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        } else {
            PermissionReminderInfo permissionReminderInfo2 = new PermissionReminderInfo();
            messageParcel.readSequenceable(permissionReminderInfo2);
            stopUsingPermission(permissionReminderInfo2);
        }
        return true;
    }
}
