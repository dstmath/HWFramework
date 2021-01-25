package ohos.nfc;

import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

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
    private NfcControllerProxy mNfcControllerProxy = NfcControllerProxy.getInstance();

    private NfcController(Context context) {
        this.mContext = context;
    }

    public static synchronized NfcController getInstance(Context context) {
        NfcController nfcController;
        synchronized (NfcController.class) {
            if (sNfcController == null) {
                sNfcController = new NfcController(context);
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
}
