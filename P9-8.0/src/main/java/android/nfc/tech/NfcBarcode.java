package android.nfc.tech;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;

public final class NfcBarcode extends BasicTagTechnology {
    public static final String EXTRA_BARCODE_TYPE = "barcodetype";
    public static final int TYPE_KOVIO = 1;
    public static final int TYPE_UNKNOWN = -1;
    private int mType;

    public static NfcBarcode get(Tag tag) {
        if (!tag.hasTech(10)) {
            return null;
        }
        try {
            return new NfcBarcode(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public NfcBarcode(Tag tag) throws RemoteException {
        super(tag, 10);
        Bundle extras = tag.getTechExtras(10);
        if (extras != null) {
            this.mType = extras.getInt(EXTRA_BARCODE_TYPE);
            return;
        }
        throw new NullPointerException("NfcBarcode tech extras are null.");
    }

    public int getType() {
        return this.mType;
    }

    public byte[] getBarcode() {
        switch (this.mType) {
            case 1:
                return this.mTag.getId();
            default:
                return null;
        }
    }
}
