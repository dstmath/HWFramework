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
    private int mType;

    public /* bridge */ /* synthetic */ void close() {
        super.close();
    }

    public /* bridge */ /* synthetic */ void connect() {
        super.connect();
    }

    public /* bridge */ /* synthetic */ Tag getTag() {
        return super.getTag();
    }

    public /* bridge */ /* synthetic */ boolean isConnected() {
        return super.isConnected();
    }

    public /* bridge */ /* synthetic */ void reconnect() {
        super.reconnect();
    }

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
        this.mType = TYPE_UNKNOWN;
        if (a.getSak() != (short) 0 || tag.getId()[0] != PAGE_SIZE) {
            return;
        }
        if (tag.getTechExtras(9).getBoolean(EXTRA_IS_UL_C)) {
            this.mType = TYPE_ULTRALIGHT_C;
        } else {
            this.mType = TYPE_ULTRALIGHT;
        }
    }

    public int getType() {
        return this.mType;
    }

    public byte[] readPages(int pageOffset) throws IOException {
        validatePageIndex(pageOffset);
        checkConnected();
        byte[] cmd = new byte[TYPE_ULTRALIGHT_C];
        cmd[0] = (byte) 48;
        cmd[TYPE_ULTRALIGHT] = (byte) pageOffset;
        return transceive(cmd, false);
    }

    public void writePage(int pageOffset, byte[] data) throws IOException {
        validatePageIndex(pageOffset);
        checkConnected();
        byte[] cmd = new byte[(data.length + TYPE_ULTRALIGHT_C)];
        cmd[0] = (byte) -94;
        cmd[TYPE_ULTRALIGHT] = (byte) pageOffset;
        System.arraycopy(data, 0, cmd, TYPE_ULTRALIGHT_C, data.length);
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
        if (pageIndex < 0 || pageIndex >= MAX_PAGE_COUNT) {
            throw new IndexOutOfBoundsException("page out of bounds: " + pageIndex);
        }
    }
}
