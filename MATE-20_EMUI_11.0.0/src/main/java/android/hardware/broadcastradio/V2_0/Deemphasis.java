package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class Deemphasis {
    public static final byte D50 = 1;
    public static final byte D75 = 2;

    public static final String toString(byte o) {
        if (o == 1) {
            return "D50";
        }
        if (o == 2) {
            return "D75";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        if ((o & 1) == 1) {
            list.add("D50");
            flipped = (byte) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("D75");
            flipped = (byte) (flipped | 2);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
