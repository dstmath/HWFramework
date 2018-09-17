package android.nfc.tech;

import android.bluetooth.BluetoothInputHost;
import android.net.wifi.WifiNetworkScoreCache;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.RemoteException;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MifareClassic extends BasicTagTechnology {
    public static final int BLOCK_SIZE = 16;
    public static final byte[] KEY_DEFAULT = new byte[]{(byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1};
    public static final byte[] KEY_MIFARE_APPLICATION_DIRECTORY = new byte[]{(byte) -96, (byte) -95, (byte) -94, (byte) -93, (byte) -92, (byte) -91};
    public static final byte[] KEY_NFC_FORUM = new byte[]{(byte) -45, (byte) -9, (byte) -45, (byte) -9, (byte) -45, (byte) -9};
    private static final int MAX_BLOCK_COUNT = 256;
    private static final int MAX_SECTOR_COUNT = 40;
    public static final int SIZE_1K = 1024;
    public static final int SIZE_2K = 2048;
    public static final int SIZE_4K = 4096;
    public static final int SIZE_MINI = 320;
    private static final String TAG = "NFC";
    public static final int TYPE_CLASSIC = 0;
    public static final int TYPE_PLUS = 1;
    public static final int TYPE_PRO = 2;
    public static final int TYPE_UNKNOWN = -1;
    private boolean mIsEmulated = false;
    private int mSize;
    private int mType;

    public static MifareClassic get(Tag tag) {
        if (!tag.hasTech(8)) {
            return null;
        }
        try {
            return new MifareClassic(tag);
        } catch (RemoteException e) {
            return null;
        }
    }

    public MifareClassic(Tag tag) throws RemoteException {
        super(tag, 8);
        NfcA a = NfcA.get(tag);
        switch (a.getSak()) {
            case (short) 1:
            case (short) 8:
                this.mType = 0;
                this.mSize = 1024;
                return;
            case (short) 9:
                this.mType = 0;
                this.mSize = 320;
                return;
            case (short) 16:
                this.mType = 1;
                this.mSize = 2048;
                return;
            case (short) 17:
                this.mType = 1;
                this.mSize = 4096;
                return;
            case (short) 24:
                this.mType = 0;
                this.mSize = 4096;
                return;
            case (short) 25:
                this.mType = 0;
                this.mSize = 2048;
                return;
            case (short) 40:
                this.mType = 0;
                this.mSize = 1024;
                this.mIsEmulated = true;
                return;
            case (short) 56:
                this.mType = 0;
                this.mSize = 4096;
                this.mIsEmulated = true;
                return;
            case (short) 136:
                this.mType = 0;
                this.mSize = 1024;
                return;
            case (short) 152:
            case (short) 184:
                this.mType = 2;
                this.mSize = 4096;
                return;
            default:
                throw new RuntimeException("Tag incorrectly enumerated as MIFARE Classic, SAK = " + a.getSak());
        }
    }

    public int getType() {
        return this.mType;
    }

    public int getSize() {
        return this.mSize;
    }

    public boolean isEmulated() {
        return this.mIsEmulated;
    }

    public int getSectorCount() {
        switch (this.mSize) {
            case 320:
                return 5;
            case 1024:
                return 16;
            case 2048:
                return 32;
            case 4096:
                return 40;
            default:
                return 0;
        }
    }

    public int getBlockCount() {
        return this.mSize / 16;
    }

    public int getBlockCountInSector(int sectorIndex) {
        validateSector(sectorIndex);
        if (sectorIndex < 32) {
            return 4;
        }
        return 16;
    }

    public int blockToSector(int blockIndex) {
        validateBlock(blockIndex);
        if (blockIndex < 128) {
            return blockIndex / 4;
        }
        return ((blockIndex + WifiNetworkScoreCache.INVALID_NETWORK_SCORE) / 16) + 32;
    }

    public int sectorToBlock(int sectorIndex) {
        if (sectorIndex < 32) {
            return sectorIndex * 4;
        }
        return ((sectorIndex - 32) * 16) + 128;
    }

    public boolean authenticateSectorWithKeyA(int sectorIndex, byte[] key) throws IOException {
        return authenticate(sectorIndex, key, true);
    }

    public boolean authenticateSectorWithKeyB(int sectorIndex, byte[] key) throws IOException {
        return authenticate(sectorIndex, key, false);
    }

    private boolean authenticate(int sector, byte[] key, boolean keyA) throws IOException {
        validateSector(sector);
        checkConnected();
        byte[] cmd = new byte[12];
        if (keyA) {
            cmd[0] = (byte) 96;
        } else {
            cmd[0] = (byte) 97;
        }
        cmd[1] = (byte) sectorToBlock(sector);
        byte[] uid = getTag().getId();
        System.arraycopy(uid, uid.length - 4, cmd, 2, 4);
        System.arraycopy(key, 0, cmd, 6, 6);
        try {
            return transceive(cmd, false) != null;
        } catch (TagLostException e) {
            throw e;
        } catch (IOException e2) {
        }
    }

    public byte[] readBlock(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        return transceive(new byte[]{(byte) 48, (byte) blockIndex}, false);
    }

    public void writeBlock(int blockIndex, byte[] data) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        if (data.length != 16) {
            throw new IllegalArgumentException("must write 16-bytes");
        }
        byte[] cmd = new byte[(data.length + 2)];
        cmd[0] = (byte) -96;
        cmd[1] = (byte) blockIndex;
        System.arraycopy(data, 0, cmd, 2, data.length);
        transceive(cmd, false);
    }

    public void increment(int blockIndex, int value) throws IOException {
        validateBlock(blockIndex);
        validateValueOperand(value);
        checkConnected();
        ByteBuffer cmd = ByteBuffer.allocate(6);
        cmd.order(ByteOrder.LITTLE_ENDIAN);
        cmd.put((byte) -63);
        cmd.put((byte) blockIndex);
        cmd.putInt(value);
        transceive(cmd.array(), false);
    }

    public void decrement(int blockIndex, int value) throws IOException {
        validateBlock(blockIndex);
        validateValueOperand(value);
        checkConnected();
        ByteBuffer cmd = ByteBuffer.allocate(6);
        cmd.order(ByteOrder.LITTLE_ENDIAN);
        cmd.put(BluetoothInputHost.SUBCLASS1_COMBO);
        cmd.put((byte) blockIndex);
        cmd.putInt(value);
        transceive(cmd.array(), false);
    }

    public void transfer(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        transceive(new byte[]{(byte) -80, (byte) blockIndex}, false);
    }

    public void restore(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        transceive(new byte[]{(byte) -62, (byte) blockIndex}, false);
    }

    public byte[] transceive(byte[] data) throws IOException {
        return transceive(data, true);
    }

    public int getMaxTransceiveLength() {
        return getMaxTransceiveLengthInternal();
    }

    public void setTimeout(int timeout) {
        try {
            if (this.mTag.getTagService().setTimeout(8, timeout) != 0) {
                throw new IllegalArgumentException("The supplied timeout is not valid");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
        }
    }

    public int getTimeout() {
        try {
            return this.mTag.getTagService().getTimeout(8);
        } catch (RemoteException e) {
            Log.e(TAG, "NFC service dead", e);
            return 0;
        }
    }

    private static void validateSector(int sector) {
        if (sector < 0 || sector >= 40) {
            throw new IndexOutOfBoundsException("sector out of bounds: " + sector);
        }
    }

    private static void validateBlock(int block) {
        if (block < 0 || block >= 256) {
            throw new IndexOutOfBoundsException("block out of bounds: " + block);
        }
    }

    private static void validateValueOperand(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value operand negative");
        }
    }
}
