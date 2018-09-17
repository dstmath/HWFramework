package android.nfc.tech;

import android.nfc.Tag;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;

public final class MifareUltralight extends BasicTagTechnology {
    public static final String EXTRA_IS_UL_C = "isulc";
    private static final int MAX_PAGE_COUNT = 256;
    private static final int NXP_MANUFACTURER_ID = 4;
    public static final int PAGE_SIZE = 4;
    private static final String TAG = "NFC";
    public static final int TYPE_ULTRALIGHT = 1;
    public static final int TYPE_ULTRALIGHT_C = 2;
    public static final int TYPE_UNKNOWN = -1;
    private int mType = -1;

    public static MifareUltralight get(Tag tag) {
        if (!tag.hasTech(9)) {
            return null;
        }
        try {
            return new MifareUltralight(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public MifareUltralight(Tag tag) throws RemoteException {
        super(tag, 9);
        NfcA a = NfcA.get(tag);
        if (a.getSak() != (short) 0 || tag.getId()[0] != (byte) 4) {
            return;
        }
        if (tag.getTechExtras(9).getBoolean(EXTRA_IS_UL_C)) {
            this.mType = 2;
        } else {
            this.mType = 1;
        }
    }

    public int getType() {
        return this.mType;
    }

    public byte[] readPages(int pageOffset) throws IOException {
        validatePageIndex(pageOffset);
        checkConnected();
        return transceive(new byte[]{(byte) 48, (byte) pageOffset}, false);
    }

    public void writePage(int pageOffset, byte[] data) throws IOException {
        validatePageIndex(pageOffset);
        checkConnected();
        byte[] cmd = new byte[(data.length + 2)];
        cmd[0] = (byte) -94;
        cmd[1] = (byte) pageOffset;
        System.arraycopy(data, 0, cmd, 2, data.length);
        transceive(cmd, false);
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }

    public void setTimeout(int timeout) {
        try {
            if (this.mTag.getTagService().setTimeout(9, timeout) != 0) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    public int getTimeout() {
        try {
            return this.mTag.getTagService().getTimeout(9);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    private static void validatePageIndex(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= 256) {
            throw new IndexOutOfBoundsException("page out of bounds: " + pageIndex);
        }
    }
}
