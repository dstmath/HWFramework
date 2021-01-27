package ohos.nfc;

import java.util.List;
import ohos.event.intentagent.IntentAgent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.PacMapEx;
import ohos.nfc.NfcController;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.sysability.samgr.SysAbilityManager;
import ohos.utils.Sequenceable;
import ohos.utils.system.safwk.java.SystemAbilityDefinition;

/* access modifiers changed from: package-private */
public class NfcControllerProxy implements INfcController {
    private static final int FALSE = 0;
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NfcControllerProxy");
    private static final String NFC_CONTROLLER = "ohos.nfc.INfcController";
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
                obtain.writeString(NFC_CONTROLLER);
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
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeString(NFC_CONTROLLER);
                asObject.sendRequest(2, obtain, obtain2, messageOption);
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
            MessageOption messageOption = new MessageOption();
            boolean z = false;
            try {
                obtain.writeString(NFC_CONTROLLER);
                asObject.sendRequest(3, obtain, obtain2, messageOption);
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

    @Override // ohos.nfc.INfcController
    public void registerForegroundDispatch(IntentAgent intentAgent, List<String> list, ProfileParcel profileParcel) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain2.writeString(NFC_CONTROLLER);
                if (list != null) {
                    obtain2.writeInt(list.size() / 2);
                    for (String str : list) {
                        obtain2.writeString(str);
                    }
                }
                if (intentAgent instanceof Sequenceable) {
                    obtain.writeSequenceable(intentAgent);
                }
                if (profileParcel instanceof Sequenceable) {
                    obtain2.writeSequenceable(profileParcel);
                }
                asObject.sendRequest(43, obtain, obtain2, new MessageOption());
                obtain2.reclaim();
                obtain.reclaim();
            } catch (RemoteException e) {
                HiLog.warn(LABEL, "registerForegroundDispatch transcat failed.", new Object[0]);
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
    public void setReaderMode(IRemoteObject iRemoteObject, NfcController.ReaderModeCallback readerModeCallback, int i, PacMapEx pacMapEx) throws RemoteException {
        IRemoteObject asObject = asObject();
        if (asObject != null) {
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            try {
                obtain.writeString(NFC_CONTROLLER);
                obtain.writeRemoteObject(iRemoteObject);
                obtain.writeInt(i);
                if (pacMapEx instanceof Sequenceable) {
                    obtain.writeSequenceable(pacMapEx);
                }
                asObject.sendRequest(44, obtain, obtain2, new MessageOption());
                obtain2.reclaim();
                obtain.reclaim();
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
