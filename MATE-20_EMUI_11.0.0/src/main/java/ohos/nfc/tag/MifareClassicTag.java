package ohos.nfc.tag;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MifareClassicTag extends TagManager {
    public static final int BLOCK_BYTES = 16;
    private static final int MAX_BLOCK_COUNT = 256;
    private static final int MAX_SECTOR_COUNT = 40;
    public static final int MIFARE_CLASSIC = 0;
    public static final int MIFARE_PLUS = 1;
    public static final int MIFARE_PRO = 2;
    public static final int MIFARE_UNKNOWN = -1;
    private static final int SAK_01 = 1;
    private static final int SAK_08 = 8;
    private static final int SAK_09 = 9;
    private static final int SAK_10 = 16;
    private static final int SAK_11 = 17;
    private static final int SAK_18 = 24;
    private static final int SAK_19 = 25;
    private static final int SAK_28 = 40;
    private static final int SAK_38 = 56;
    private static final int SAK_88 = 136;
    private static final int SAK_98 = 152;
    private static final int SAK_B8 = 184;
    public static final int TAG_ROM_1K = 1024;
    public static final int TAG_ROM_2K = 2048;
    public static final int TAG_ROM_4K = 4096;
    public static final int TAG_ROM_MINI = 320;
    public static final byte USE_KEY_A = 96;
    public static final byte USE_KEY_B = 97;
    private int mMifareSize = 0;
    private int mMifareType = -1;

    public int getFirstBlockId(int i) {
        return i < 32 ? i * 4 : ((i - 32) * 16) + 128;
    }

    public static MifareClassicTag getInstance(TagInfo tagInfo) {
        if (tagInfo == null) {
            throw new NullPointerException("MifareClassicTag tagInfo is null");
        } else if (!tagInfo.isProfileSupported(1)) {
            return null;
        } else {
            return new MifareClassicTag(tagInfo);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0085 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0086  */
    private MifareClassicTag(TagInfo tagInfo) {
        super(tagInfo, 8);
        short s;
        NfcATag instance = NfcATag.getInstance(tagInfo);
        this.mMifareType = -1;
        if (instance != null) {
            s = instance.getSak();
        } else {
            s = -1;
        }
        if (s != 1) {
            if (s == 40) {
                this.mMifareType = 0;
                this.mMifareSize = 1024;
            } else if (s == SAK_38) {
                this.mMifareType = 0;
                this.mMifareSize = 4096;
            } else if (s == SAK_88) {
                this.mMifareType = 0;
                this.mMifareSize = 1024;
            } else if (s == SAK_98 || s == SAK_B8) {
                this.mMifareType = 2;
                this.mMifareSize = 4096;
            } else if (s != 8) {
                if (s == 9) {
                    this.mMifareType = 0;
                    this.mMifareSize = TAG_ROM_MINI;
                } else if (s == 16) {
                    this.mMifareType = 1;
                    this.mMifareSize = 2048;
                } else if (s == 17) {
                    this.mMifareType = 1;
                    this.mMifareSize = 4096;
                } else if (s == 24) {
                    this.mMifareType = 0;
                    this.mMifareSize = 4096;
                } else if (s == 25) {
                    this.mMifareType = 0;
                    this.mMifareSize = 2048;
                }
            }
            if (this.mMifareType != -1) {
                throw new IllegalArgumentException("Unknown Mifare type, received sak = " + ((int) s));
            }
            return;
        }
        this.mMifareType = 0;
        this.mMifareSize = 1024;
        if (this.mMifareType != -1) {
        }
    }

    public int getMifareType() {
        return this.mMifareType;
    }

    public int getTagSize() {
        return this.mMifareSize;
    }

    public int getSectorsNum() {
        int i = this.mMifareSize;
        if (i == 320) {
            return 5;
        }
        if (i == 1024) {
            return 16;
        }
        if (i != 2048) {
            return i != 4096 ? 0 : 40;
        }
        return 32;
    }

    public int getBlocksNum() {
        return this.mMifareSize / 16;
    }

    public int getBlocksNumForSector(int i) {
        checkSectorId(i);
        return i < 32 ? 4 : 16;
    }

    public int getSectorId(int i) {
        checkBlockId(i);
        if (i < 128) {
            return i / 4;
        }
        return ((i - 128) / 16) + 32;
    }

    public boolean authenSectorUseKey(int i, byte[] bArr, byte b) {
        checkSectorId(i);
        checkConnected();
        checkKeyType(b);
        byte[] bArr2 = new byte[12];
        bArr2[0] = b;
        bArr2[1] = (byte) getFirstBlockId(i);
        byte[] tagId = getTagInfo().getTagId();
        System.arraycopy(tagId, tagId.length - 4, bArr2, 2, 4);
        System.arraycopy(bArr, 0, bArr2, 6, 6);
        if (sendData(bArr2) != null) {
            return true;
        }
        return false;
    }

    public byte[] readBlock(int i) {
        checkBlockId(i);
        checkConnected();
        return sendData(new byte[]{48, (byte) i});
    }

    public void writeBlock(int i, byte[] bArr) {
        checkBlockId(i);
        checkConnected();
        if (bArr.length == 16) {
            byte[] bArr2 = new byte[(bArr.length + 2)];
            bArr2[0] = -96;
            bArr2[1] = (byte) i;
            System.arraycopy(bArr, 0, bArr2, 2, bArr.length);
            sendData(bArr2);
            return;
        }
        throw new IllegalArgumentException("should write 16-bytes");
    }

    public void incBlock(int i, int i2) {
        checkBlockId(i);
        checkValue(i2);
        checkConnected();
        ByteBuffer allocate = ByteBuffer.allocate(6);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        allocate.put((byte) -63);
        allocate.put((byte) i);
        allocate.putInt(i2);
        sendData(allocate.array());
    }

    public void decBlock(int i, int i2) {
        checkBlockId(i);
        checkValue(i2);
        checkConnected();
        ByteBuffer allocate = ByteBuffer.allocate(6);
        allocate.order(ByteOrder.LITTLE_ENDIAN);
        allocate.put((byte) -64);
        allocate.put((byte) i);
        allocate.putInt(i2);
        sendData(allocate.array());
    }

    public void restoreBlock(int i) {
        checkBlockId(i);
        checkConnected();
        sendData(new byte[]{-62, (byte) i});
    }

    private void checkSectorId(int i) {
        if (i < 0 || i >= 40) {
            throw new IndexOutOfBoundsException("sectorId out of bounds: " + i);
        }
    }

    private void checkBlockId(int i) {
        if (i < 0 || i >= 256) {
            throw new IndexOutOfBoundsException("blockId out of bounds: " + i);
        }
    }

    private void checkValue(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("input not valid, value = " + i);
        }
    }

    private void checkKeyType(byte b) {
        if (b != 96 && b != 97) {
            throw new IllegalArgumentException("input not valid, keyType = " + ((int) b));
        }
    }
}
