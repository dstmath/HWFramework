package android.nfc.tech;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class NfcA extends BasicTagTechnology {
    public static final String EXTRA_ATQA = "atqa";
    public static final String EXTRA_SAK = "sak";
    private static final String TAG = "NFC";
    private byte[] mAtqa;
    private short mSak = (short) 0;

    public static NfcA get(Tag tag) {
        if (!tag.hasTech(1)) {
            return null;
        }
        try {
            return new NfcA(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NfcA(Tag tag) throws RemoteException {
        super(tag, 1);
        if (tag.hasTech(8)) {
            this.mSak = tag.getTechExtras(8).getShort(EXTRA_SAK);
        }
        Bundle extras = tag.getTechExtras(1);
        this.mSak = (short) (this.mSak | extras.getShort(EXTRA_SAK));
        this.mAtqa = extras.getByteArray(EXTRA_ATQA);
    }

    public byte[] getAtqa() {
        return this.mAtqa;
    }

    public short getSak() {
        return this.mSak;
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }

    public void setTimeout(int timeout) {
        try {
            if (this.mTag.getTagService().setTimeout(1, timeout) != 0) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    public int getTimeout() {
        try {
            return this.mTag.getTagService().getTimeout(1);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }
}
