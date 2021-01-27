package android.hardware.tetheroffload.control.V1_0;

import java.util.ArrayList;

public final class NetworkProtocol {
    public static final int TCP = 6;
    public static final int UDP = 17;

    public static final String toString(int o) {
        if (o == 6) {
            return "TCP";
        }
        if (o == 17) {
            return "UDP";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 6) == 6) {
            list.add("TCP");
            flipped = 0 | 6;
        }
        if ((o & 17) == 17) {
            list.add("UDP");
            flipped |= 17;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
