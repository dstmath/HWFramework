package ohos.nfc;

import java.util.List;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.Lifecycle;
import ohos.app.Context;
import ohos.event.intentagent.IntentAgent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.PacMapEx;
import ohos.nfc.tag.TagInfo;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public final class NfcController {
    public static final String EXTRA_NFC_STATE = "extra_nfc_state";
    public static final String EXTRA_NFC_TRANSACTION = "extra_nfc_transaction";
    public static final String EXTRA_TAG_INFO = "extra_nfc_TAG_INFO";
    public static final String FIELD_OFF_DETECTED = "usual.event.nfc.action.RF_FIELD_OFF_DETECTED";
    public static final String FIELD_ON_DETECTED = "usual.event.nfc.action.RF_FIELD_ON_DETECTED";
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "NfcController");
    private static final int NO_ERROR = 1;
    private static final int NO_PERMISSION = -6;
    public static final String STATE_CHANGED = "usual.event.nfc.action.ADAPTER_STATE_CHANGED";
    public static final int STATE_OFF = 1;
    public static final int STATE_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_TURNING_ON = 2;
    private static NfcController sNfcController;
    private Context mContext;
    private NfcAbilityManager mNfcAbilityManager;
    private NfcControllerProxy mNfcControllerProxy = NfcControllerProxy.getInstance();

    public interface ReaderModeCallback extends IRemoteBroker {
        public static final int TRANSACTION_ON_CALLBACK = 1;

        void onTagDiscovered(TagInfo tagInfo);
    }

    public class ReaderModeCallbackStub extends RemoteObject implements ReaderModeCallback {
        private ReaderModeCallback callback;

        @Override // ohos.rpc.IRemoteBroker
        public IRemoteObject asObject() {
            return this;
        }

        public ReaderModeCallbackStub(ReaderModeCallback readerModeCallback) {
            super("");
            this.callback = readerModeCallback;
        }

        private void readInterfaceToken(Parcel parcel) {
            parcel.readInt();
            parcel.readInt();
            parcel.readString();
        }

        @Override // ohos.rpc.RemoteObject
        public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
            HiLog.debug(NfcController.LABEL, "call ReaderModeCallbackStub's onTransact", new Object[0]);
            if (i != 1) {
                HiLog.error(NfcController.LABEL, "ReaderModeCallbackStub unsupported transaction code.", new Object[0]);
                return super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            }
            readInterfaceToken(messageParcel);
            HiLog.debug(NfcController.LABEL, "ReaderModeCallbackStub onCallback was invoked.", new Object[0]);
            TagInfo tagInfo = new TagInfo(null, null, null, 0);
            if (tagInfo instanceof Sequenceable) {
                messageParcel2.readSequenceable(tagInfo);
            }
            onTagDiscovered(tagInfo);
            messageParcel2.writeInt(0);
            return true;
        }

        @Override // ohos.nfc.NfcController.ReaderModeCallback
        public void onTagDiscovered(TagInfo tagInfo) {
            ReaderModeCallback readerModeCallback = this.callback;
            if (readerModeCallback != null) {
                readerModeCallback.onTagDiscovered(tagInfo);
            }
        }
    }

    private NfcController(Context context) {
        this.mContext = context;
        this.mNfcAbilityManager = new NfcAbilityManager(this);
    }

    public static synchronized NfcController getInstance(Context context) throws IllegalArgumentException {
        NfcController nfcController;
        synchronized (NfcController.class) {
            if (sNfcController == null) {
                if (context != null) {
                    sNfcController = new NfcController(context.getApplicationContext());
                } else {
                    throw new IllegalArgumentException();
                }
            }
            nfcController = sNfcController;
        }
        return nfcController;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean openNfc() throws NfcPermissionException {
        try {
            int nfcEnabled = this.mNfcControllerProxy.setNfcEnabled(true);
            if (nfcEnabled != -6) {
                return nfcEnabled == 1;
            }
            throw new NfcPermissionException("No Nfc Permission");
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "open RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean closeNfc() throws NfcPermissionException {
        try {
            int nfcEnabled = this.mNfcControllerProxy.setNfcEnabled(false);
            if (nfcEnabled != -6) {
                return nfcEnabled == 1;
            }
            throw new NfcPermissionException("No Nfc Permission");
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "close RemoteException", new Object[0]);
            return false;
        }
    }

    public boolean isNfcOpen() {
        return getNfcState() == 3;
    }

    public int getNfcState() {
        try {
            return this.mNfcControllerProxy.getNfcState();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getNfcState RemoteException", new Object[0]);
            return 1;
        }
    }

    public boolean isNfcAvailable() {
        try {
            return this.mNfcControllerProxy.isNfcAvailable();
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isNfcAvailable RemoteException", new Object[0]);
            return false;
        }
    }

    public void setReaderMode(Ability ability, ReaderModeCallback readerModeCallback, int i, PacMapEx pacMapEx) {
        synchronized (NfcController.class) {
            if (!isNfcAvailable()) {
                throw new UnsupportedOperationException();
            }
        }
        this.mNfcAbilityManager.setReaderMode(ability, readerModeCallback, i, pacMapEx);
    }

    public void unsetReaderMode(Ability ability) {
        synchronized (NfcController.class) {
            if (!isNfcAvailable()) {
                throw new UnsupportedOperationException();
            }
        }
        this.mNfcAbilityManager.unsetReaderMode(ability);
    }

    public void registerForegroundDispatch(Ability ability, IntentAgent intentAgent, List<String> list, String[][] strArr) {
        synchronized (NfcController.class) {
            if (!isNfcAvailable()) {
                throw new UnsupportedOperationException();
            }
        }
        if (ability == null || intentAgent == null) {
            throw new NullPointerException("abilitySlice or intent is null");
        } else if (ability.getLifecycle().getLifecycleState() == Lifecycle.Event.valueOf("ON_ACTIVE")) {
            ProfileParcel profileParcel = null;
            if (strArr != null) {
                try {
                    if (strArr.length > 0) {
                        profileParcel = new ProfileParcel(strArr);
                    }
                } catch (RemoteException unused) {
                    HiLog.error(LABEL, "registerForegroundDispatch failed!", new Object[0]);
                    return;
                }
            }
            this.mNfcControllerProxy.registerForegroundDispatch(intentAgent, list, profileParcel);
        } else {
            throw new IllegalStateException("Foreground dispatch can not be enabled when ability slice is not active.");
        }
    }

    public void unregisterForegroundDispatch(Ability ability) {
        synchronized (NfcController.class) {
            if (!isNfcAvailable()) {
                throw new UnsupportedOperationException();
            }
        }
        unregisterForegroundDispatchInternal(ability, false);
    }

    private void unregisterForegroundDispatchInternal(Ability ability, boolean z) {
        try {
            this.mNfcControllerProxy.registerForegroundDispatch(null, null, null);
            if (z) {
                return;
            }
            if (ability.getLifecycle().getLifecycleState() != Lifecycle.Event.valueOf("ON_ACTIVE")) {
                throw new IllegalStateException("Can not unset foreground dispatching while ability is not resumed.");
            }
        } catch (RemoteException unused) {
            HiLog.error(LABEL, "unsetForegroundDispatchInternal failed!", new Object[0]);
        }
    }
}
