package android.nfc.tech;

import android.nfc.Tag;
import android.nfc.TransceiveResult;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

abstract class BasicTagTechnology implements TagTechnology {
    private static final String TAG = "NFC";
    boolean mIsConnected;
    int mSelectedTechnology;
    final Tag mTag;

    BasicTagTechnology(Tag tag, int tech) throws RemoteException {
        this.mTag = tag;
        this.mSelectedTechnology = tech;
    }

    public Tag getTag() {
        return this.mTag;
    }

    /* access modifiers changed from: package-private */
    public void checkConnected() {
        if (this.mTag.getConnectedTechnology() != this.mSelectedTechnology || this.mTag.getConnectedTechnology() == -1) {
            throw new IllegalStateException("Call connect() first!");
        }
    }

    public boolean isConnected() {
        if (!this.mIsConnected) {
            return false;
        }
        try {
            return this.mTag.getTagService().isPresent(this.mTag.getServiceHandle());
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return false;
        }
    }

    public void connect() throws IOException {
        try {
            int errorCode = this.mTag.getTagService().connect(this.mTag.getServiceHandle(), this.mSelectedTechnology);
            if (errorCode == 0) {
                this.mTag.setConnectedTechnology(this.mSelectedTechnology);
                this.mIsConnected = true;
            } else if (errorCode == -21) {
                throw new UnsupportedOperationException("Connecting to this technology is not supported by the NFC adapter.");
            } else {
                throw new IOException();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            throw new IOException("NFC service died");
        }
    }

    public void reconnect() throws IOException {
        if (this.mIsConnected) {
            try {
                if (this.mTag.getTagService().reconnect(this.mTag.getServiceHandle()) != 0) {
                    this.mIsConnected = false;
                    this.mTag.setTechnologyDisconnected();
                    throw new IOException();
                }
            } catch (RemoteException e) {
                this.mIsConnected = false;
                this.mTag.setTechnologyDisconnected();
                Log.e(TAG, "NFC service dead", e);
                throw new IOException("NFC service died");
            }
        } else {
            throw new IllegalStateException("Technology not connected yet");
        }
    }

    public void close() throws IOException {
        try {
            this.mTag.getTagService().resetTimeouts();
            this.mTag.getTagService().reconnect(this.mTag.getServiceHandle());
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        } catch (Throwable th) {
            this.mIsConnected = false;
            this.mTag.setTechnologyDisconnected();
            throw th;
        }
        this.mIsConnected = false;
        this.mTag.setTechnologyDisconnected();
    }

    /* access modifiers changed from: package-private */
    public int getMaxTransceiveLengthInternal() {
        try {
            return this.mTag.getTagService().getMaxTransceiveLength(this.mSelectedTechnology);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public byte[] transceive(byte[] data, boolean raw) throws IOException {
        checkConnected();
        try {
            TransceiveResult result = this.mTag.getTagService().transceive(this.mTag.getServiceHandle(), data, raw);
            if (result != null) {
                return result.getResponseOrThrow();
            }
            throw new IOException("transceive failed");
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            throw new IOException("NFC service died");
        }
    }
}
