package ohos.nfc;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;

public class NfcCommProxy implements IRemoteBroker {
    private static final int ERR_OK = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NfcCommProxy");
    private int mAbilityId;
    private final Object mLock = new Object();
    private IRemoteObject mRemoteObject;

    static {
        System.loadLibrary("ipc_core.z");
    }

    protected NfcCommProxy(int i) {
        this.mAbilityId = i;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        synchronized (this.mLock) {
            if (this.mRemoteObject != null) {
                return this.mRemoteObject;
            }
            this.mRemoteObject = SysAbilityManager.getSysAbility(this.mAbilityId);
            if (this.mRemoteObject != null) {
                this.mRemoteObject.addDeathRecipient(new NfcDeathRecipient(), 0);
                HiLog.info(LABEL, "getSysAbility %{public}d completed.", Integer.valueOf(this.mAbilityId));
            } else {
                HiLog.error(LABEL, "getSysAbility %{public}d failed.", Integer.valueOf(this.mAbilityId));
            }
            return this.mRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public class NfcDeathRecipient implements IRemoteObject.DeathRecipient {
        private NfcDeathRecipient() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(NfcCommProxy.LABEL, "NfcDeathRecipient::onRemoteDied.", new Object[0]);
            synchronized (NfcCommProxy.this.mLock) {
                NfcCommProxy.this.mRemoteObject = null;
            }
        }
    }

    private IRemoteObject getRemote() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            return asObject;
        }
        HiLog.error(LABEL, "Failed to get remote object of nfc sa", new Object[0]);
        throw new RemoteException("get remote failed for nfc sa");
    }

    /* access modifiers changed from: protected */
    public MessageParcel request(int i, MessageParcel messageParcel) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        try {
            getRemote().sendRequest(i, messageParcel, obtain, new MessageOption(0));
            if (obtain.readInt() == 0) {
                if (messageParcel != null) {
                    messageParcel.reclaim();
                }
                return obtain;
            }
            throw new RemoteException();
        } catch (RemoteException unused) {
            throw new RemoteException("connect with nfc sa failed via request");
        } catch (Throwable th) {
            if (messageParcel != null) {
                messageParcel.reclaim();
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public MessageParcel requestWithoutCheck(int i, MessageParcel messageParcel) throws RemoteException {
        MessageParcel obtain = MessageParcel.obtain();
        try {
            getRemote().sendRequest(i, messageParcel, obtain, new MessageOption(0));
            if (messageParcel != null) {
                messageParcel.reclaim();
            }
            return obtain;
        } catch (RemoteException unused) {
            throw new RemoteException("connect with nfc sa failed via requestWithoutCheck");
        } catch (Throwable th) {
            if (messageParcel != null) {
                messageParcel.reclaim();
            }
            throw th;
        }
    }
}
