package ohos.hiviewdfx;

import java.util.Locale;
import ohos.media.camera.params.Metadata;

public final class HiTraceIdImpl {
    private static final int BITS_IN_ONE_BYTE = 8;
    private static final byte HITRACE_ID_VALID = 1;
    private static final byte HITRACE_VER_1 = 0;
    private static final int TRACEID_CHAINID_FROM_BIT = 0;
    private static final int TRACEID_CHAINID_TO_BIT = 59;
    private static final int TRACEID_FLAG_FROM_BIT = 116;
    private static final int TRACEID_FLAG_TO_BIT = 127;
    private static final int TRACEID_PARENTSPANID_FROM_BIT = 64;
    private static final int TRACEID_PARENTSPANID_TO_BIT = 89;
    private static final int TRACEID_SPANID_FROM_BIT = 90;
    private static final int TRACEID_SPANID_TO_BIT = 115;
    private static final int TRACEID_VALID_FROM_BIT = 63;
    private static final int TRACEID_VALID_TO_BIT = 63;
    private static final int TRACEID_VERSION_FROM_BIT = 60;
    private static final int TRACEID_VERSION_TO_BIT = 62;
    private byte[] idArray;

    private int getHighBitMask(int i) {
        int i2 = 128;
        byte b = 0;
        for (int i3 = 0; i3 < i; i3++) {
            b = (byte) (b | i2);
            i2 >>>= 1;
        }
        return b;
    }

    private int getLowBitMask(int i) {
        byte b = 0;
        int i2 = 1;
        for (int i3 = 0; i3 < i; i3++) {
            b = (byte) (b | i2);
            i2 <<= 1;
        }
        return b;
    }

    public HiTraceIdImpl(byte[] bArr) {
        initHiTraceId(bArr);
    }

    private void initHiTraceId(byte[] bArr) {
        this.idArray = new byte[16];
        if (bArr != null && bArr.length == 16) {
            System.arraycopy(bArr, 0, this.idArray, 0, 16);
        }
    }

    private boolean isValidIdArray() {
        byte[] bArr = this.idArray;
        return bArr != null && bArr.length == 16;
    }

    public boolean isValid() {
        if (isValidIdArray() && (getValueFromByte(63, 63) & 1) == 1) {
            return true;
        }
        return false;
    }

    private void setValid() {
        if (isValidIdArray()) {
            setValueToByte(1, 63, 63);
        }
    }

    private long getValueFromOneBit(int i) {
        return (long) (this.idArray[i >>> 3] & (1 << (8 - ((i % 8) + 1))));
    }

    private long getValueFromOneByte(int i, int i2) {
        int i3 = 8 - ((i2 % 8) + 1);
        return ((long) (this.idArray[i >>> 3] & (getLowBitMask((i2 - i) + 1) << i3))) >>> i3;
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0029  */
    private long getValueFromByte(int i, int i2) {
        long j;
        int i3 = i >>> 3;
        int i4 = i2 >>> 3;
        if (i == i2) {
            return getValueFromOneBit(i);
        }
        if (i3 == i4) {
            return getValueFromOneByte(i, i2);
        }
        int i5 = i % 8;
        int i6 = i2 % 8;
        if (i5 != 0) {
            j = (long) (getLowBitMask(8 - i5) & this.idArray[i3]);
            i3++;
        } else {
            j = 0;
        }
        if (i3 >= i4) {
            j = (j << 8) | ((long) (this.idArray[i3] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE));
            i3++;
            if (i3 >= i4) {
            }
        }
        if (i6 == 7) {
            return (j << 8) | ((long) (this.idArray[i4] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE));
        }
        int i7 = i6 + 1;
        return ((long) (((this.idArray[i4] & getHighBitMask(i7)) & 255) >>> (8 - i7))) | (j << i7);
    }

    private void setValueToOneBit(long j, int i) {
        int i2 = i >>> 3;
        byte[] bArr = this.idArray;
        bArr[i2] = (byte) ((((int) (j & 1)) << (8 - ((i % 8) + 1))) | bArr[i2]);
    }

    private void setValueToOneByte(long j, int i, int i2) {
        int i3 = i >>> 3;
        byte[] bArr = this.idArray;
        bArr[i3] = (byte) ((((int) (j & ((long) getLowBitMask((i2 - i) + 1)))) << (8 - ((i2 % 8) + 1))) | bArr[i3]);
    }

    private void setValueToByte(long j, int i, int i2) {
        int i3 = i >>> 3;
        int i4 = i2 >>> 3;
        if (i == i2) {
            setValueToOneBit(j, i);
        } else if (i3 == i4) {
            setValueToOneByte(j, i, i2);
        } else {
            int i5 = i2 % 8;
            if (i5 != 7) {
                int i6 = i5 + 1;
                byte[] bArr = this.idArray;
                bArr[i4] = (byte) (bArr[i4] & (~getHighBitMask(i6)));
                byte[] bArr2 = this.idArray;
                bArr2[i4] = (byte) (((byte) (((byte) ((int) (((long) getLowBitMask(i6)) & j))) << (8 - i6))) | bArr2[i4]);
                i4--;
                j >>>= i6;
            }
            while (i4 > i3) {
                j >>>= 8;
                this.idArray[i4] = (byte) ((int) (255 & j));
                i4--;
            }
            int i7 = i % 8;
            if (i7 == 0) {
                this.idArray[i3] = (byte) ((int) j);
                return;
            }
            int lowBitMask = getLowBitMask(8 - i7);
            byte[] bArr3 = this.idArray;
            bArr3[i3] = (byte) (bArr3[i3] & (~lowBitMask));
            bArr3[i3] = (byte) ((int) ((j & ((long) lowBitMask)) | ((long) bArr3[i3])));
        }
    }

    private void setVersion(byte b) {
        if (isValid()) {
            setValueToByte((long) b, 60, TRACEID_VERSION_TO_BIT);
        }
    }

    private byte getVersion() {
        return (byte) ((int) getValueFromByte(60, TRACEID_VERSION_TO_BIT));
    }

    public boolean isFlagEnabled(int i) {
        if (isValid() && i >= 0 && i < 64 && (getFlags() & i) != 0) {
            return true;
        }
        return false;
    }

    public void enableFlag(int i) {
        if (isValid() && i >= 0 && i < 64) {
            setFlags(i | getFlags());
        }
    }

    public int getFlags() {
        if (!isValid()) {
            return 0;
        }
        return (int) getValueFromByte(116, TRACEID_FLAG_TO_BIT);
    }

    public void setFlags(int i) {
        if (isValid() && i >= 0 && i < 64) {
            setValueToByte((long) i, 116, TRACEID_FLAG_TO_BIT);
        }
    }

    public long getChainId() {
        if (!isValid()) {
            return 0;
        }
        return getValueFromByte(0, TRACEID_CHAINID_TO_BIT);
    }

    public void setChainId(long j) {
        if (isValidIdArray()) {
            if (!isValid()) {
                setValid();
                setVersion((byte) 0);
                setFlags(0);
                setSpanId(0);
                setParentSpanId(0);
            }
            setValueToByte(j, 0, TRACEID_CHAINID_TO_BIT);
        }
    }

    public long getSpanId() {
        if (!isValid()) {
            return 0;
        }
        return getValueFromByte(90, 115);
    }

    public void setSpanId(long j) {
        if (isValid()) {
            setValueToByte(j, 90, 115);
        }
    }

    public long getParentSpanId() {
        if (!isValid()) {
            return 0;
        }
        return getValueFromByte(64, TRACEID_PARENTSPANID_TO_BIT);
    }

    public void setParentSpanId(long j) {
        if (isValid()) {
            setValueToByte(j, 64, TRACEID_PARENTSPANID_TO_BIT);
        }
    }

    public byte[] toBytes() {
        if (!isValidIdArray()) {
            return new byte[0];
        }
        byte[] bArr = new byte[16];
        System.arraycopy(this.idArray, 0, bArr, 0, 16);
        return bArr;
    }

    public String toString() {
        if (!isValidIdArray()) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(((int) getVersion()) + ":");
        byte[] bArr = this.idArray;
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            String format = String.format(Locale.ENGLISH, "%s", Integer.toHexString(bArr[i] & Metadata.BeautySkinToneType.BEAUTY_COLORS_NONE));
            if (format.length() == 1) {
                stringBuffer.append(0);
            }
            stringBuffer.append(format);
        }
        return stringBuffer.toString();
    }
}
