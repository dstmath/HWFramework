package ohos.wifi;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class WifiCommProxy implements IRemoteBroker {
    private static final int ERR_OK = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, InnerUtils.LOG_ID_WIFI, "WifiCommProxy");
    private int mAbilityId;
    private final Object mLock = new Object();
    private IRemoteObject mRemoteAbility;

    static {
        System.loadLibrary("ipc_core.z");
    }

    protected WifiCommProxy(int i) {
        this.mAbilityId = i;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        synchronized (this.mLock) {
            if (this.mRemoteAbility != null) {
                return this.mRemoteAbility;
            }
            this.mRemoteAbility = SysAbilityManager.getSysAbility(this.mAbilityId);
            if (this.mRemoteAbility != null) {
                this.mRemoteAbility.addDeathRecipient(new WifiDeathRecipient(), 0);
            } else {
                HiLog.error(LABEL, "getSysAbility %{public}d failed.", Integer.valueOf(this.mAbilityId));
            }
            return this.mRemoteAbility;
        }
    }

    /* access modifiers changed from: private */
    public class WifiDeathRecipient implements IRemoteObject.DeathRecipient {
        private WifiDeathRecipient() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(WifiCommProxy.LABEL, "WifiDeathRecipient::onRemoteDied.", new Object[0]);
            synchronized (WifiCommProxy.this.mLock) {
                WifiCommProxy.this.mRemoteAbility = null;
            }
        }
    }

    private IRemoteObject getRemote() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            return asObject;
        }
        HiLog.error(LABEL, "Failed to get remote object", new Object[0]);
        throw new RemoteException();
    }

    /* access modifiers changed from: protected */
    public MessageParcel request(int i, MessageParcel messageParcel) throws RemoteException {
        MessageParcel create = MessageParcel.create();
        try {
            getRemote().sendRequest(i, messageParcel, create, new MessageOption(0));
            int readInt = create.readInt();
            if (readInt == 73465857) {
                throw new SecurityException("Permission denied");
            } else if (readInt == 0) {
                if (messageParcel != null) {
                    messageParcel.reclaim();
                }
                return create;
            } else {
                throw new RemoteException();
            }
        } catch (RemoteException unused) {
            throw new RemoteException();
        } catch (Throwable th) {
            if (messageParcel != null) {
                messageParcel.reclaim();
            }
            throw th;
        }
    }
}
