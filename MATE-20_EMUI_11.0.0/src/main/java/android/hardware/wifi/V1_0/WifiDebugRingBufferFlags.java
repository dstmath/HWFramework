package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class WifiDebugRingBufferFlags {
    public static final int HAS_ASCII_ENTRIES = 2;
    public static final int HAS_BINARY_ENTRIES = 1;
    public static final int HAS_PER_PACKET_ENTRIES = 4;

    public static final String toString(int o) {
        if (o == 1) {
            return "HAS_BINARY_ENTRIES";
        }
        if (o == 2) {
            return "HAS_ASCII_ENTRIES";
        }
        if (o == 4) {
            return "HAS_PER_PACKET_ENTRIES";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("HAS_BINARY_ENTRIES");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("HAS_ASCII_ENTRIES");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("HAS_PER_PACKET_ENTRIES");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
