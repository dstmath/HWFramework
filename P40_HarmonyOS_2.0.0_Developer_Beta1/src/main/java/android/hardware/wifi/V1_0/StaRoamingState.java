package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class StaRoamingState {
    public static final byte DISABLED = 0;
    public static final byte ENABLED = 1;

    public static final String toString(byte o) {
        if (o == 0) {
            return "DISABLED";
        }
        if (o == 1) {
            return "ENABLED";
        }
        return "0x" + Integer.toHexString(Byte.toUnsignedInt(o));
    }

    public static final String dumpBitfield(byte o) {
        ArrayList<String> list = new ArrayList<>();
        byte flipped = 0;
        list.add("DISABLED");
        if ((o & 1) == 1) {
            list.add("ENABLED");
            flipped = (byte) (0 | 1);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Byte.toUnsignedInt((byte) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
