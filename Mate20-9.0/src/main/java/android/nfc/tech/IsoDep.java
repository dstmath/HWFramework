package android.nfc.tech;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class IsoDep extends BasicTagTechnology {
    public static final String EXTRA_HIST_BYTES = "histbytes";
    public static final String EXTRA_HI_LAYER_RESP = "hiresp";
    private static final String TAG = "NFC";
    private byte[] mHiLayerResponse = null;
    private byte[] mHistBytes = null;

    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    public /* bridge */ /* synthetic */ void connect() throws IOException {
        super.connect();
    }

    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    public /* bridge */ /* synthetic */ void reconnect() throws IOException {
        super.reconnect();
    }

    public static IsoDep get(Tag tag) {
        if (!tag.hasTech(3)) {
            return null;
        }
        try {
            return new IsoDep(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public IsoDep(Tag tag) throws RemoteException {
        super(tag, 3);
        Bundle extras = tag.getTechExtras(3);
        if (extras != null) {
            this.mHiLayerResponse = extras.getByteArray(EXTRA_HI_LAYER_RESP);
            this.mHistBytes = extras.getByteArray(EXTRA_HIST_BYTES);
        }
    }

    public void setTimeout(int timeout) {
        try {
            if (this.mTag.getTagService().setTimeout(3, timeout) != 0) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    public int getTimeout() {
        try {
            return this.mTag.getTagService().getTimeout(3);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    public byte[] getHistoricalBytes() {
        return this.mHistBytes;
    }

    public byte[] getHiLayerResponse() {
        return this.mHiLayerResponse;
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }

    public boolean isExtendedLengthApduSupported() {
        try {
            return this.mTag.getTagService().getExtendedLengthApdusSupported();
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return false;
        }
    }
}
