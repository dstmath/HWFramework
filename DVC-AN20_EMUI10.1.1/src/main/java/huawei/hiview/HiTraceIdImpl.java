package huawei.hiview;

import com.huawei.uikit.effect.BuildConfig;
import java.util.Arrays;
import java.util.Locale;

public final class HiTraceIdImpl implements HiTraceId {
    private static final int BITS_IN_ONE_BYTE = 8;
    private static final byte HITRACE_ID_VALID = 1;
    private static final byte HITRACE_VER_1 = 0;
    public static final int ID_ARRAY_LEN = 16;
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

    public HiTraceIdImpl() {
        this.idArray = new byte[16];
    }

    public HiTraceIdImpl(byte[] idArray2) {
        initHiTraceId(idArray2);
    }

    private void initHiTraceId(byte[] array) {
        this.idArray = new byte[16];
        if (array != null && array.length >= 16) {
            System.arraycopy(array, 0, this.idArray, 0, 16);
            if (!isValid()) {
                Arrays.fill(this.idArray, (byte) HITRACE_VER_1);
            }
        }
    }

    private boolean isValidIdArray() {
        return this.idArray != null;
    }

    public boolean isValid() {
        boolean isValid = false;
        if (!isValidIdArray()) {
            return false;
        }
        if ((getValueFromByte(63, 63) & 1) == 1) {
            isValid = true;
        }
        return isValid;
    }

    private void setValid() {
        if (isValidIdArray()) {
            setValueToByte(1, 63, 63);
        }
    }

    private int getHighBitMask(int count) {
        int mask = 0;
        int flag = 128;
        for (int index = 0; index < count; index++) {
            mask = (byte) (mask | flag);
            flag >>>= 1;
        }
        return mask;
    }

    private int getLowBitMask(int count) {
        int mask = 0;
        int flag = 1;
        for (int index = 0; index < count; index++) {
            mask = (byte) (mask | flag);
            flag <<= 1;
        }
        return mask;
    }

    private long getValueFromOneBit(int bitIndex) {
        return (long) (this.idArray[bitIndex >>> 3] & (1 << (8 - ((bitIndex % 8) + 1))));
    }

    private long getValueFromOneByte(int fromBit, int toBit) {
        int count = 8 - ((toBit % 8) + 1);
        return ((long) (this.idArray[fromBit >>> 3] & (getLowBitMask((toBit - fromBit) + 1) << count))) >>> count;
    }

    private long getValueFromByte(int fromBit, int toBit) {
        long value = 0;
        int fromIndex = fromBit >>> 3;
        int toIndex = toBit >>> 3;
        if (fromBit == toBit) {
            return getValueFromOneBit(fromBit);
        }
        if (fromIndex == toIndex) {
            return getValueFromOneByte(fromBit, toBit);
        }
        int fromBitIndex = fromBit % 8;
        int toBitIndex = toBit % 8;
        if (fromBitIndex != 0) {
            value = (long) (this.idArray[fromIndex] & getLowBitMask(8 - fromBitIndex));
            fromIndex++;
        }
        for (int index = fromIndex; index < toIndex; index++) {
            value = (value << 8) | ((long) (this.idArray[index] & 255));
        }
        if (toBitIndex == 7) {
            return (value << 8) | ((long) (this.idArray[toIndex] & 255));
        }
        int count = toBitIndex + 1;
        return (value << count) | ((long) (((this.idArray[toIndex] & getHighBitMask(count)) & 255) >>> (8 - count)));
    }

    private void setValueToOneBit(long value, int bitIndex) {
        int index = bitIndex >>> 3;
        byte[] bArr = this.idArray;
        bArr[index] = (byte) (bArr[index] | (((int) (1 & value)) << (8 - ((bitIndex % 8) + 1))));
    }

    private void setValueToOneByte(long value, int fromBit, int toBit) {
        int index = fromBit >>> 3;
        int setValue = ((int) (((long) getLowBitMask((toBit - fromBit) + 1)) & value)) << (8 - ((toBit % 8) + 1));
        byte[] bArr = this.idArray;
        bArr[index] = (byte) (bArr[index] | setValue);
    }

    /* JADX INFO: Multiple debug info for r8v3 int: [D('index' int), D('fromBitIndex' int)] */
    private void setValueToByte(long value, int fromBit, int toBit) {
        int fromIndex = fromBit >>> 3;
        int toIndex = toBit >>> 3;
        long setValue = value;
        if (fromBit == toBit) {
            setValueToOneBit(setValue, fromBit);
        } else if (fromIndex == toIndex) {
            setValueToOneByte(setValue, fromBit, toBit);
        } else {
            int toBitIndex = toBit % 8;
            if (toBitIndex != 7) {
                int count = toBitIndex + 1;
                byte tmp = (byte) (((byte) ((int) (((long) getLowBitMask(count)) & setValue))) << (8 - count));
                byte[] bArr = this.idArray;
                bArr[toIndex] = (byte) (bArr[toIndex] & (~getHighBitMask(count)));
                byte[] bArr2 = this.idArray;
                bArr2[toIndex] = (byte) (bArr2[toIndex] | tmp);
                toIndex--;
                setValue >>>= count;
            }
            for (int index = toIndex; index > fromIndex; index--) {
                setValue >>>= 8;
                this.idArray[index] = (byte) ((int) (255 & setValue));
            }
            int fromBitIndex = fromBit % 8;
            if (fromBitIndex == 0) {
                this.idArray[fromIndex] = (byte) ((int) setValue);
                return;
            }
            int lowMask = getLowBitMask(8 - fromBitIndex);
            byte[] bArr3 = this.idArray;
            bArr3[fromIndex] = (byte) (bArr3[fromIndex] & (~lowMask));
            bArr3[fromIndex] = (byte) ((int) (((long) bArr3[fromIndex]) | (((long) lowMask) & setValue)));
        }
    }

    private void setVersion(byte version) {
        if (isValid()) {
            setValueToByte((long) version, TRACEID_VERSION_FROM_BIT, TRACEID_VERSION_TO_BIT);
        }
    }

    private byte getVersion() {
        return (byte) ((int) getValueFromByte(TRACEID_VERSION_FROM_BIT, TRACEID_VERSION_TO_BIT));
    }

    public boolean isFlagEnabled(int flag) {
        if (isValid() && flag >= 0 && flag < 64 && (getFlags() & flag) != 0) {
            return true;
        }
        return false;
    }

    public void enableFlag(int flag) {
        if (isValid() && flag >= 0 && flag < 64) {
            setFlags(getFlags() | flag);
        }
    }

    public int getFlags() {
        if (!isValid()) {
            return 0;
        }
        return (int) getValueFromByte(TRACEID_FLAG_FROM_BIT, 127);
    }

    public void setFlags(int flags) {
        if (isValid() && flags >= 0 && flags < 64) {
            setValueToByte((long) flags, TRACEID_FLAG_FROM_BIT, 127);
        }
    }

    public long getChainId() {
        if (!isValid()) {
            return 0;
        }
        return getValueFromByte(0, TRACEID_CHAINID_TO_BIT);
    }

    public void setChainId(long chainId) {
        if (isValidIdArray()) {
            if (!isValid()) {
                setValid();
                setVersion(HITRACE_VER_1);
                setFlags(0);
                setSpanId(0);
                setParentSpanId(0);
            }
            setValueToByte(chainId, 0, TRACEID_CHAINID_TO_BIT);
        }
    }

    public long getSpanId() {
        if (!isValid()) {
            return 0;
        }
        return getValueFromByte(TRACEID_SPANID_FROM_BIT, TRACEID_SPANID_TO_BIT);
    }

    public void setSpanId(long spanId) {
        if (isValid()) {
            setValueToByte(spanId, TRACEID_SPANID_FROM_BIT, TRACEID_SPANID_TO_BIT);
        }
    }

    public long getParentSpanId() {
        if (!isValid()) {
            return 0;
        }
        return getValueFromByte(64, TRACEID_PARENTSPANID_TO_BIT);
    }

    public void setParentSpanId(long parentSpanId) {
        if (isValid()) {
            setValueToByte(parentSpanId, 64, TRACEID_PARENTSPANID_TO_BIT);
        }
    }

    public byte[] toBytes() {
        if (!isValidIdArray()) {
            return new byte[0];
        }
        byte[] array = new byte[16];
        System.arraycopy(this.idArray, 0, array, 0, 16);
        return array;
    }

    public String toString() {
        if (!isValidIdArray()) {
            return BuildConfig.FLAVOR;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(((int) getVersion()) + ":");
        byte[] bArr = this.idArray;
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            byte element = bArr[i];
            String tmp = String.format(Locale.ENGLISH, "%s", Integer.toHexString(element & 255));
            if (tmp.length() == 1) {
                stringBuffer.append(0);
            }
            stringBuffer.append(tmp);
        }
        return stringBuffer.toString();
    }
}
