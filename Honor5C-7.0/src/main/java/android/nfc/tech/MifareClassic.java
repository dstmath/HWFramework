package android.nfc.tech;

import android.bluetooth.BluetoothAssignedNumbers;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.os.FileUtils;
import android.os.RemoteException;
import android.rms.HwSysResource;
import android.security.keymaster.KeymasterDefs;
import android.speech.SpeechRecognizer;
import android.telecom.AudioState;
import android.util.Log;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class MifareClassic extends BasicTagTechnology {
    public static final int BLOCK_SIZE = 16;
    public static final byte[] KEY_DEFAULT = null;
    public static final byte[] KEY_MIFARE_APPLICATION_DIRECTORY = null;
    public static final byte[] KEY_NFC_FORUM = null;
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
    private boolean mIsEmulated;
    private int mSize;
    private int mType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.nfc.tech.MifareClassic.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.nfc.tech.MifareClassic.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.nfc.tech.MifareClassic.<clinit>():void");
    }

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
        this.mIsEmulated = false;
        switch (a.getSak()) {
            case TYPE_PLUS /*1*/:
            case AudioState.ROUTE_SPEAKER /*8*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_1K;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS /*9*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_MINI;
            case BLOCK_SIZE /*16*/:
                this.mType = TYPE_PLUS;
                this.mSize = SIZE_2K;
            case HwSysResource.CURSOR /*17*/:
                this.mType = TYPE_PLUS;
                this.mSize = SIZE_4K;
            case HwSysResource.ANR /*24*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_4K;
            case HwSysResource.DELAY /*25*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_2K;
            case MAX_SECTOR_COUNT /*40*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_1K;
                this.mIsEmulated = true;
            case FileUtils.S_IRWXG /*56*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_4K;
                this.mIsEmulated = true;
            case Const.CODE_C3_SKIP5_RANGE_START /*136*/:
                this.mType = TYPE_CLASSIC;
                this.mSize = SIZE_1K;
            case Const.CODE_C1_DF0 /*152*/:
            case BluetoothAssignedNumbers.QUALCOMM_INNOVATION_CENTER /*184*/:
                this.mType = TYPE_PRO;
                this.mSize = SIZE_4K;
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
            case SIZE_MINI /*320*/:
                return 5;
            case SIZE_1K /*1024*/:
                return BLOCK_SIZE;
            case SIZE_2K /*2048*/:
                return 32;
            case SIZE_4K /*4096*/:
                return MAX_SECTOR_COUNT;
            default:
                return TYPE_CLASSIC;
        }
    }

    public int getBlockCount() {
        return this.mSize / BLOCK_SIZE;
    }

    public int getBlockCountInSector(int sectorIndex) {
        validateSector(sectorIndex);
        if (sectorIndex < 32) {
            return 4;
        }
        return BLOCK_SIZE;
    }

    public int blockToSector(int blockIndex) {
        validateBlock(blockIndex);
        if (blockIndex < KeymasterDefs.KM_ALGORITHM_HMAC) {
            return blockIndex / 4;
        }
        return ((blockIndex - 128) / BLOCK_SIZE) + 32;
    }

    public int sectorToBlock(int sectorIndex) {
        if (sectorIndex < 32) {
            return sectorIndex * 4;
        }
        return ((sectorIndex - 32) * BLOCK_SIZE) + KeymasterDefs.KM_ALGORITHM_HMAC;
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
            cmd[TYPE_CLASSIC] = (byte) 96;
        } else {
            cmd[TYPE_CLASSIC] = (byte) 97;
        }
        cmd[TYPE_PLUS] = (byte) sectorToBlock(sector);
        byte[] uid = getTag().getId();
        System.arraycopy(uid, uid.length - 4, cmd, TYPE_PRO, 4);
        System.arraycopy(key, TYPE_CLASSIC, cmd, 6, 6);
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
        byte[] cmd = new byte[TYPE_PRO];
        cmd[TYPE_CLASSIC] = (byte) 48;
        cmd[TYPE_PLUS] = (byte) blockIndex;
        return transceive(cmd, false);
    }

    public void writeBlock(int blockIndex, byte[] data) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        if (data.length != BLOCK_SIZE) {
            throw new IllegalArgumentException("must write 16-bytes");
        }
        byte[] cmd = new byte[(data.length + TYPE_PRO)];
        cmd[TYPE_CLASSIC] = (byte) -96;
        cmd[TYPE_PLUS] = (byte) blockIndex;
        System.arraycopy(data, TYPE_CLASSIC, cmd, TYPE_PRO, data.length);
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
        cmd.put((byte) -64);
        cmd.put((byte) blockIndex);
        cmd.putInt(value);
        transceive(cmd.array(), false);
    }

    public void transfer(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        byte[] cmd = new byte[TYPE_PRO];
        cmd[TYPE_CLASSIC] = (byte) -80;
        cmd[TYPE_PLUS] = (byte) blockIndex;
        transceive(cmd, false);
    }

    public void restore(int blockIndex) throws IOException {
        validateBlock(blockIndex);
        checkConnected();
        byte[] cmd = new byte[TYPE_PRO];
        cmd[TYPE_CLASSIC] = (byte) -62;
        cmd[TYPE_PLUS] = (byte) blockIndex;
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
            return TYPE_CLASSIC;
        }
    }

    private static void validateSector(int sector) {
        if (sector < 0 || sector >= MAX_SECTOR_COUNT) {
            throw new IndexOutOfBoundsException("sector out of bounds: " + sector);
        }
    }

    private static void validateBlock(int block) {
        if (block < 0 || block >= MAX_BLOCK_COUNT) {
            throw new IndexOutOfBoundsException("block out of bounds: " + block);
        }
    }

    private static void validateValueOperand(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value operand negative");
        }
    }
}
