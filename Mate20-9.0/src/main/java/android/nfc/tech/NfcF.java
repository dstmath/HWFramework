package android.nfc.tech;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class NfcF extends BasicTagTechnology {
    public static final String EXTRA_PMM = "pmm";
    public static final String EXTRA_SC = "systemcode";
    private static final String TAG = "NFC";
    private byte[] mManufacturer = null;
    private byte[] mSystemCode = null;

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

    public static NfcF get(Tag tag) {
        if (!tag.hasTech(4)) {
            return null;
        }
        try {
            return new NfcF(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NfcF(Tag tag) throws RemoteException {
        super(tag, 4);
        Bundle extras = tag.getTechExtras(4);
        if (extras != null) {
            this.mSystemCode = extras.getByteArray(EXTRA_SC);
            this.mManufacturer = extras.getByteArray(EXTRA_PMM);
        }
    }

    public byte[] getSystemCode() {
        return this.mSystemCode;
    }

    public byte[] getManufacturer() {
        return this.mManufacturer;
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }

    public void setTimeout(int timeout) {
        try {
            if (this.mTag.getTagService().setTimeout(4, timeout) != 0) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    public int getTimeout() {
        try {
            return this.mTag.getTagService().getTimeout(4);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }
}
