package ohos.security.permissionkitinner;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IPermissionKitInner extends IRemoteBroker {
    boolean canRequestPermission(String str, String str2, int i) throws RemoteException;
}
