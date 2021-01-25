package ohos.bundle;

import ohos.appexecfwk.utils.AppLog;
import ohos.rpc.IRemoteObject;
import ohos.sysability.samgr.SysAbilityManager;

public class BundleManagerDeathRecipient implements IRemoteObject.DeathRecipient {
    private static final int SLEEP_TIME = 1000;
    private int timeout = 30;

    public void onRemoteDied() {
        IRemoteObject sysAbility = SysAbilityManager.getSysAbility(401);
        while (sysAbility == null) {
            if (this.timeout > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException unused) {
                    AppLog.d("onRemoteDied::InterruptedException exception", new Object[0]);
                }
                sysAbility = SysAbilityManager.getSysAbility(401);
                this.timeout--;
            } else {
                AppLog.d("onRemoteDied::getSysAbility failed", new Object[0]);
                return;
            }
        }
        BundleManager.resetFlag();
    }
}
