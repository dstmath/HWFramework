package ohos.nfc;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

class NfcControllerProxy implements INfcController {
    private static final int FALSE = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NfcControllerProxy");
    private static final int TRUE = 1;
    private static NfcControllerProxy sNfcControllerProxy;
    private final Object mRemoteLock = new Object();
    private IRemoteObject mRemoteObject;

    private NfcControllerProxy() {
    }

    public static synchronized NfcControllerProxy getInstance() {
        NfcControllerProxy nfcControllerProxy;
        synchronized (NfcControllerProxy.class) {
            if (sNfcControllerProxy == null) {
                sNfcControllerProxy = new NfcControllerProxy();
            }
            nfcControllerProxy = sNfcControllerProxy;
        }
        return nfcControllerProxy;
    }

    @Override // ohos.rpc.IRemoteBroker
    public IRemoteObject asObject() {
        synchronized (this.mRemoteLock) {
            if (this.mRemoteObject != null) {
                return this.mRemoteObject;
            }
            this.mRemoteObject = SysAbilityManager.getSysAbility(SystemAbilityDefinition.NFC_MANAGER_SYS_ABILITY_ID);
            if (this.mRemoteObject != null) {
                this.mRemoteObject.addDeathRecipient(new NfcControllerDeathRecipient(), 0);
                HiLog.info(LABEL, "Get NfcManagerService completed.", new Object[0]);
            } else {
                HiLog.error(LABEL, "getSysAbility(NfcManagerService) failed.", new Object[0]);
            }
            return this.mRemoteObject;
        }
    }

    @Override // ohos.nfc.INfcController
    public int setNfcEnabled(boolean z) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeInt(z ? 1 : 0);
                asObject.sendRequest(1, obtain, obtain2, messageOption);
                int readInt = obtain2.readInt();
                obtain2.reclaim();
                obtain.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.warn(LABEL, "setNfcEnabled transcat failed.", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
        } else {
            throw new RemoteException();
        }
    }

    @Override // ohos.nfc.INfcController
    public int getNfcState() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                asObject.sendRequest(2, obtain, obtain2, new MessageOption());
                int readInt = obtain2.readInt();
                obtain2.reclaim();
                obtain.reclaim();
                return readInt;
            } catch (RemoteException e) {
                HiLog.warn(LABEL, "getNfcState transcat failed.", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
        } else {
            throw new RemoteException();
        }
    }

    @Override // ohos.nfc.INfcController
    public boolean isNfcAvailable() throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            boolean z = false;
            try {
                asObject.sendRequest(3, obtain, obtain2, new MessageOption());
                if (obtain2.readInt() != 0) {
                    z = true;
                }
                obtain2.reclaim();
                obtain.reclaim();
                return z;
            } catch (RemoteException e) {
                HiLog.warn(LABEL, "isNfcAvailable transcat failed.", new Object[0]);
                throw e;
            } catch (Throwable th) {
                obtain2.reclaim();
                obtain.reclaim();
                throw th;
            }
        } else {
            throw new RemoteException();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRemoteObject(IRemoteObject iRemoteObject) {
        synchronized (this.mRemoteLock) {
            this.mRemoteObject = iRemoteObject;
        }
    }

    /* access modifiers changed from: private */
    public class NfcControllerDeathRecipient implements IRemoteObject.DeathRecipient {
        private NfcControllerDeathRecipient() {
        }

        @Override // ohos.rpc.IRemoteObject.DeathRecipient
        public void onRemoteDied() {
            HiLog.warn(NfcControllerProxy.LABEL, "NfcControllerDeathRecipient::onRemoteDied.", new Object[0]);
            NfcControllerProxy.this.setRemoteObject(null);
        }
    }
}
