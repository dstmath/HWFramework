package android.hardware.contexthub.V1_0;

import java.util.ArrayList;

public final class HostEndPoint {
    public static final short BROADCAST = -1;
    public static final short UNSPECIFIED = -2;

    public static final String toString(short o) {
        if (o == -1) {
            return "BROADCAST";
        }
        if (o == -2) {
            return "UNSPECIFIED";
        }
        return "0x" + Integer.toHexString(Short.toUnsignedInt(o));
    }

    public static final String dumpBitfield(short o) {
        ArrayList<String> list = new ArrayList<>();
        short flipped = 0;
        if ((o & -1) == -1) {
            list.add("BROADCAST");
            flipped = (short) (0 | -1);
        }
        if ((o & -2) == -2) {
            list.add("UNSPECIFIED");
            flipped = (short) (flipped | -2);
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString(Short.toUnsignedInt((short) ((~flipped) & o))));
        }
        return String.join(" | ", list);
    }
}
