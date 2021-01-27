package ohos.nfc.oma;

import java.util.Arrays;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.nfc.NfcKitsUtils;
import ohos.rpc.RemoteException;

public final class SEService {
    private static final HiLogLabel LABEL = new HiLogLabel(3, NfcKitsUtils.NFC_DOMAIN_ID, "SEService");
    private OnCallback mCallback = null;
    private SecureElementCallbackProxy mCallbackProxy = null;
    private Context mContext = null;
    private boolean mIsServiceConnected = false;
    private Reader[] mReaders = null;
    private SecureElementProxy mSecureElementProxy = null;
    private String version = "1.0";

    public interface OnCallback {
        void serviceConnected();
    }

    public SEService(Context context, OnCallback onCallback) throws IllegalArgumentException {
        if (context != null) {
            this.mSecureElementProxy = new SecureElementProxy(this);
            this.mContext = context.getApplicationContext();
            this.mCallback = onCallback;
            this.mCallbackProxy = new SecureElementCallbackProxy(this, onCallback, NfcKitsUtils.CALLBACK_DESCRIPTOR);
            try {
                this.mIsServiceConnected = this.mSecureElementProxy.isSeServiceConnected();
            } catch (RemoteException unused) {
                HiLog.warn(LABEL, "SEService() RemoteException", new Object[0]);
            }
            if (this.mIsServiceConnected) {
                OnCallback onCallback2 = this.mCallback;
                if (onCallback2 != null) {
                    onCallback2.serviceConnected();
                    return;
                }
                return;
            }
            try {
                if (this.mContext == null) {
                    HiLog.warn(LABEL, "SEService() context is null.", new Object[0]);
                }
                this.mSecureElementProxy.bindSeService(this.mCallbackProxy);
            } catch (RemoteException unused2) {
                HiLog.warn(LABEL, "SEService RemoteException", new Object[0]);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void notifyServiceConnected(boolean z) {
        this.mIsServiceConnected = z;
    }

    public boolean isConnected() {
        return this.mIsServiceConnected;
    }

    public void shutdown() {
        if (this.mReaders != null && isConnected()) {
            for (Reader reader : this.mReaders) {
                reader.closeSessions();
            }
        }
        this.mReaders = null;
        this.mIsServiceConnected = false;
    }

    public OnCallback getCallback() {
        return this.mCallback;
    }

    public SecureElementCallbackProxy getCallbackProxy() {
        return this.mCallbackProxy;
    }

    public Reader[] getReaders() {
        try {
            if (isConnected()) {
                this.mReaders = this.mSecureElementProxy.getReaders(this);
            }
        } catch (RemoteException unused) {
            HiLog.warn(LABEL, "getReaders RemoteException", new Object[0]);
        }
        Reader[] readerArr = this.mReaders;
        return readerArr != null ? (Reader[]) Arrays.copyOf(readerArr, readerArr.length) : new Reader[0];
    }

    public String getVersion() {
        return this.version;
    }
}
