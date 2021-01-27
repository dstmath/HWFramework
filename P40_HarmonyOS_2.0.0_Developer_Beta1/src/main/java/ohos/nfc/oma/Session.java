package ohos.nfc.oma;

import java.util.Optional;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public final class Session {
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "SecureElementSession");
    private IRemoteObject remoteSession = null;
    private SecureElementProxy seProxy = null;
    private SEService seService = null;

    public Session(SEService sEService, SecureElementProxy secureElementProxy, IRemoteObject iRemoteObject) {
        this.seService = sEService;
        this.seProxy = secureElementProxy;
        this.remoteSession = iRemoteObject;
    }

    public IRemoteObject getSessionObject() {
        return this.remoteSession;
    }

    public SEService getSEService() {
        return this.seService;
    }

    public Optional<Channel> openBasicChannel(Aid aid) {
        if (aid != null && aid.isAidValid() && this.seService.isConnected() && this.remoteSession != null) {
            try {
                return this.seProxy.openBasicChannel(this, aid.getAidBytes());
            } catch (RemoteException unused) {
                HiLog.warn(LABEL, "openBasicChannel RemoteException", new Object[0]);
            }
        }
        return Optional.empty();
    }

    public Optional<Channel> openLogicalChannel(Aid aid) {
        if (aid != null && aid.isAidValid() && this.seService.isConnected() && this.remoteSession != null) {
            try {
                return this.seProxy.openLogicalChannel(this, aid.getAidBytes());
            } catch (RemoteException unused) {
                HiLog.warn(LABEL, "openLogicalChannel RemoteException", new Object[0]);
            }
        }
        return Optional.empty();
    }

    public byte[] getATR() {
        try {
            if (this.seService.isConnected() && this.remoteSession != null) {
                return this.seProxy.getATR(this.remoteSession);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getATR RemoteException", new Object[0]);
        }
        return new byte[0];
    }

    public void close() {
        try {
            if (this.seService.isConnected() && this.remoteSession != null) {
                this.seProxy.close(this.remoteSession);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "close RemoteException", new Object[0]);
        }
    }
}
