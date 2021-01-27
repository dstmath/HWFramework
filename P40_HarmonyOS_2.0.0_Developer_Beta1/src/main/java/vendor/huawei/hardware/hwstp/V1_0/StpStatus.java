package vendor.huawei.hardware.hwstp.V1_0;

import java.util.ArrayList;

public final class StpStatus {
    public static final byte STP_RISK = 1;
    public static final byte STP_SAFE = 0;

    public static final String toString(byte o) {
        if (o == 0) {
            return "STP_SAFE";
        }
        if (o == 1) {
            return "STP_RISK";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        list.add("STP_SAFE");
        if ((o & 1) == 1) {
            list.add("STP_RISK");
            flipped = (byte) (0 | 1);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
