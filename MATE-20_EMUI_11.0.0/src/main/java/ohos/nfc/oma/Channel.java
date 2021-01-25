package ohos.nfc.oma;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public final class Channel {
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "SecureElementChannel");
    private SEService mSEService = null;
    private SecureElementProxy mSecureElementProxy;
    private IRemoteObject remoteObject = null;
    private int type = 0;

    public Channel(SEService sEService, SecureElementProxy secureElementProxy, IRemoteObject iRemoteObject, int i) {
        this.mSEService = sEService;
        this.mSecureElementProxy = secureElementProxy;
        this.remoteObject = iRemoteObject;
        this.type = i;
    }

    public boolean isClosed() {
        try {
            if (this.mSEService.isConnected() && this.remoteObject != null) {
                return this.mSecureElementProxy.isChannelClosed(this.remoteObject);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "isChannelClosed RemoteException", new Object[0]);
        }
        return false;
    }

    public boolean isBasicChannel() {
        return this.type == 0;
    }

    public byte[] transmit(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            HiLog.warn(LABEL, "method transmit input param is null or empty", new Object[0]);
            return new byte[0];
        }
        try {
            if (this.mSEService.isConnected() && this.remoteObject != null) {
                return this.mSecureElementProxy.transmit(this.remoteObject, bArr);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "transmit RemoteException", new Object[0]);
        }
        return new byte[0];
    }

    public byte[] getSelectResponse() {
        try {
            if (this.mSEService.isConnected() && this.remoteObject != null) {
                return this.mSecureElementProxy.getSelectResponse(this.remoteObject);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getSelectResponse RemoteException", new Object[0]);
        }
        return new byte[0];
    }

    public void closeChannel() {
        try {
            if (this.mSEService.isConnected() && this.remoteObject != null) {
                this.mSecureElementProxy.closeChannel(this.remoteObject);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "closeChannel RemoteException", new Object[0]);
        }
    }
}
