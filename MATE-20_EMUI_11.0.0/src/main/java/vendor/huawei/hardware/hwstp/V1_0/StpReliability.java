package vendor.huawei.hardware.hwstp.V1_0;

import java.util.ArrayList;

public final class StpReliability {
    public static final byte STP_CREDIBLE = 1;
    public static final byte STP_REFERENCE = 0;

    public static final String toString(byte o) {
        if (o == 0) {
            return "STP_REFERENCE";
        }
        if (o == 1) {
            return "STP_CREDIBLE";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        list.add("STP_REFERENCE");
        if ((o & 1) == 1) {
            list.add("STP_CREDIBLE");
            flipped = (byte) (0 | 1);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
