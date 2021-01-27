package ohos.systemrestore.interfaces.impl;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.startup.utils.StartUpStringUtil;
import ohos.systemrestore.SystemRestoreException;
import ohos.systemrestore.adapter.SystemRestoreSystemAbility;
import ohos.systemrestore.interfaces.ISystemRestoreSystemAbility;

public class SystemRestoreSystemAbilityProxy implements ISystemRestoreSystemAbility {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218115072, SystemRestoreSystemAbilityProxy.class.getSimpleName());
    private final IRemoteObject remote;
    private final SystemRestoreSystemAbility systemAbility;

    public SystemRestoreSystemAbilityProxy() {
        this(null);
    }

    public SystemRestoreSystemAbilityProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
        this.systemAbility = SystemRestoreSystemAbility.getInstance();
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public boolean rebootRestoreCache(String str) throws SystemRestoreException {
        verifiSystemAbility();
        verifiCommands(str);
        return this.systemAbility.rebootRestoreCache(str);
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public boolean passedAuthenticationRestoreCache(Context context) throws SystemRestoreException {
        verifiSystemAbility();
        return this.systemAbility.passedAuthenticationRestoreCache(context);
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public boolean rebootRestoreUserData(String str) throws SystemRestoreException {
        verifiSystemAbility();
        verifiCommands(str);
        return this.systemAbility.rebootRestoreUserData(str);
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public boolean passedAuthenticationRestoreUserData(Context context) throws SystemRestoreException {
        verifiSystemAbility();
        return this.systemAbility.passedAuthenticationRestoreUserData(context);
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public void sendRestoreUserDataBroadcast(Context context) throws SystemRestoreException {
        verifiSystemAbility();
        this.systemAbility.sendRestoreUserDataBroadcast(context);
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public void passedAuthenticationInstallPackage(Context context) throws SystemRestoreException {
        verifiSystemAbility();
        this.systemAbility.passedAuthenticationInstallPackage(context);
    }

    @Override // ohos.systemrestore.interfaces.ISystemRestoreSystemAbility
    public void installUpdatePackageCommands(String str) throws SystemRestoreException {
        verifiSystemAbility();
        verifiCommands(str);
        this.systemAbility.installUpdatePackageCommands(str);
    }

    private void verifiSystemAbility() throws SystemRestoreException {
        if (this.systemAbility == null) {
            HiLog.error(TAG, "verifiSystemAbility get system ability is null.", new Object[0]);
            throw new SystemRestoreException("get system ability is null");
        }
    }

    private void verifiCommands(String str) throws SystemRestoreException {
        if (StartUpStringUtil.isEmpty(str)) {
            HiLog.error(TAG, "verifiCommands verification commands is null.", new Object[0]);
            throw new SystemRestoreException("commands is null");
        }
    }
}
