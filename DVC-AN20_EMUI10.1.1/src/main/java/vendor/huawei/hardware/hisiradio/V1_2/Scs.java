package vendor.huawei.hardware.hisiradio.V1_2;

import java.util.ArrayList;

public final class Scs {
    public static final int SCS_120KHZ = 3;
    public static final int SCS_15KHZ = 0;
    public static final int SCS_240KHZ = 4;
    public static final int SCS_30KHZ = 1;
    public static final int SCS_60KHZ = 2;
    public static final int SCS_UNKNOWN = 5;

    public static final String toString(int o) {
        if (o == 0) {
            return "SCS_15KHZ";
        }
        if (o == 1) {
            return "SCS_30KHZ";
        }
        if (o == 2) {
            return "SCS_60KHZ";
        }
        if (o == 3) {
            return "SCS_120KHZ";
        }
        if (o == 4) {
            return "SCS_240KHZ";
        }
        if (o == 5) {
            return "SCS_UNKNOWN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("SCS_15KHZ");
        if ((o & 1) == 1) {
            list.add("SCS_30KHZ");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SCS_60KHZ");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("SCS_120KHZ");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("SCS_240KHZ");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("SCS_UNKNOWN");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
