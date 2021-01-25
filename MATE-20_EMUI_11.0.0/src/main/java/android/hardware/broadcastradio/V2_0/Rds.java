package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class Rds {
    public static final byte RBDS = 2;
    public static final byte RDS = 1;

    public static final String toString(byte o) {
        if (o == 1) {
            return "RDS";
        }
        if (o == 2) {
            return "RBDS";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        if ((o & 1) == 1) {
            list.add("RDS");
            flipped = (byte) (0 | 1);
        }
        if ((o & 2) == 2) {
            list.add("RBDS");
            flipped = (byte) (flipped | 2);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
