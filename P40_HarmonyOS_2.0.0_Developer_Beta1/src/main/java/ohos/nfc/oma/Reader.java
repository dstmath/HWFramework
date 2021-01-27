package ohos.nfc.oma;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.RemoteException;

public final class Reader {
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "Reader");
    public static final int TYPE_BASIC_CHANNEL = 0;
    public static final int TYPE_LOGICAL_CHANNEL = 1;
    private String mName;
    private SEService mSEService = null;
    private SecureElementProxy mSecureElementProxy;

    public Reader(SecureElementProxy secureElementProxy, String str, SEService sEService) {
        this.mSecureElementProxy = secureElementProxy;
        this.mName = str;
        this.mSEService = sEService;
    }

    public SEService getSEService() {
        return this.mSEService;
    }

    public String getName() {
        return this.mName;
    }

    public boolean isSecureElementPresent() {
        try {
            if (this.mSEService.isConnected()) {
                return this.mSecureElementProxy.isSecureElementPresent(this.mName);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isSecureElementPresent RemoteException", new Object[0]);
        }
        return false;
    }

    public Optional<Session> openSession() {
        try {
            if (this.mSEService.isConnected()) {
                return this.mSecureElementProxy.openSession(this);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "openSession RemoteException", new Object[0]);
        }
        return Optional.empty();
    }

    public void closeSessions() {
        try {
            if (this.mSEService.isConnected()) {
                this.mSecureElementProxy.closeSeSessions(this);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "closeSeSessions RemoteException", new Object[0]);
        }
    }
}
