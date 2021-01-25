package ohos.aafwk.ability.delegation;

import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class AbilityToolsProxy {
    private static final LogLabel LABEL = LogLabel.create();
    public static final int MSG_LEN_LIMIT = 1000;
    public static final int OUTPUT_MSG = 0;
    private AbilityToolsDeathRecipient abilityToolsDeathRecipient;
    private final String deviceId;
    private IRemoteObject remote;
    private final Object remoteLock = new Object();
    private final int serviceId;

    public AbilityToolsProxy(int i, String str) {
        this.serviceId = i;
        this.deviceId = str;
    }

    /* access modifiers changed from: package-private */
    public void output(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Send message should not be null");
        } else if (str.length() <= 1000) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            obtain.writeString(str);
            synchronized (this.remoteLock) {
                if (this.remote != null) {
                    try {
                        this.remote.sendRequest(0, obtain, obtain2, messageOption);
                    } catch (RemoteException unused) {
                        Log.error(LABEL, "Remote object communicate exception", new Object[0]);
                    }
                } else {
                    obtain2.reclaim();
                    obtain.reclaim();
                    throw new IllegalStateException("You should get remote object first");
                }
            }
            obtain2.reclaim();
            obtain.reclaim();
        } else {
            throw new IllegalArgumentException("Message length limit is 1000");
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0043  */
    public void start() {
        synchronized (this.remoteLock) {
            if (this.remote == null) {
                if (this.deviceId != null) {
                    if (!this.deviceId.isEmpty()) {
                        this.remote = SysAbilityManager.getSysAbility(this.serviceId, this.deviceId);
                        if (this.remote == null) {
                            if (this.abilityToolsDeathRecipient == null) {
                                this.abilityToolsDeathRecipient = new AbilityToolsDeathRecipient();
                                this.remote.addDeathRecipient(this.abilityToolsDeathRecipient, 0);
                            }
                            return;
                        }
                        throw new IllegalStateException("Get remote service error, serviceId is:" + this.serviceId);
                    }
                }
                this.remote = SysAbilityManager.getSysAbility(this.serviceId);
                if (this.remote == null) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        synchronized (this.remoteLock) {
            if (!(this.abilityToolsDeathRecipient == null || this.remote == null)) {
                this.remote.removeDeathRecipient(this.abilityToolsDeathRecipient, 0);
            }
            this.remote = null;
            this.abilityToolsDeathRecipient = null;
        }
    }

    /* access modifiers changed from: private */
    public class AbilityToolsDeathRecipient implements IRemoteObject.DeathRecipient {
        private AbilityToolsDeathRecipient() {
        }

        public void onRemoteDied() {
            synchronized (AbilityToolsProxy.this.remoteLock) {
                Log.info(AbilityToolsProxy.LABEL, "Remote died", new Object[0]);
                AbilityToolsProxy.this.remote = null;
                AbilityToolsProxy.this.abilityToolsDeathRecipient = null;
            }
        }
    }
}
