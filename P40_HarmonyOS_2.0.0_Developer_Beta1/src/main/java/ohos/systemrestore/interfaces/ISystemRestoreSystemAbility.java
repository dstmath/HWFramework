package ohos.systemrestore.interfaces;

import ohos.app.Context;
import ohos.rpc.IRemoteBroker;
import ohos.systemrestore.SystemRestoreException;

public interface ISystemRestoreSystemAbility extends IRemoteBroker {
    void installUpdatePackageCommands(String str) throws SystemRestoreException;

    void passedAuthenticationInstallPackage(Context context) throws SystemRestoreException;

    boolean passedAuthenticationRestoreCache(Context context) throws SystemRestoreException;

    boolean passedAuthenticationRestoreUserData(Context context) throws SystemRestoreException;

    boolean rebootRestoreCache(String str) throws SystemRestoreException;

    boolean rebootRestoreUserData(String str) throws SystemRestoreException;

    void sendRestoreUserDataBroadcast(Context context) throws SystemRestoreException;
}
