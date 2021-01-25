package ohos.sysability.samgr;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;

public class SysAbilityManagerDeathRecipient implements IRemoteObject.DeathRecipient {
    private static final int DEATH_RECIPIENT_FLAG = 0;
    private static final int SLEEP_TIME = 1000;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218109952, "SysAbilityManagerDeathRecipient");
    private int timeout = 30;

    @Override // ohos.rpc.IRemoteObject.DeathRecipient
    public void onRemoteDied() {
        HiLog.info(TAG, "onRemoteDied", new Object[0]);
        SystemAbilityManagerClient.destroySystemAbilityManagerObject();
        IRemoteObject iRemoteObject = null;
        while (iRemoteObject == null) {
            HiLog.debug(TAG, "Waiting for samgr...", new Object[0]);
            if (this.timeout > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException unused) {
                    HiLog.warn(TAG, "setUp Thread.sleep exception", new Object[0]);
                }
                iRemoteObject = SystemAbilityManagerClient.getSystemAbilityManagerObject();
                this.timeout--;
            } else {
                HiLog.debug(TAG, "getSystemAbilityManagerObject failed", new Object[0]);
                return;
            }
        }
        if (!iRemoteObject.addDeathRecipient(new SysAbilityManagerDeathRecipient(), 0)) {
            HiLog.debug(TAG, "addDeathRecipient failed", new Object[0]);
        }
        if (!SysAbilityRegistry.getRegistry().reRegisterSysAbility()) {
            HiLog.debug(TAG, "sysAbilityRegistry reRegisterSysAbility failed", new Object[0]);
        }
    }
}
