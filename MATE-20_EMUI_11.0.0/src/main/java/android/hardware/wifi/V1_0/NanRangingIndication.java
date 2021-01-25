package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanRangingIndication {
    public static final int CONTINUOUS_INDICATION_MASK = 1;
    public static final int EGRESS_MET_MASK = 4;
    public static final int INGRESS_MET_MASK = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "CONTINUOUS_INDICATION_MASK";
        }
        if (o == 2) {
            return "INGRESS_MET_MASK";
        }
        if (o == 4) {
            return "EGRESS_MET_MASK";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("CONTINUOUS_INDICATION_MASK");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("INGRESS_MET_MASK");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("EGRESS_MET_MASK");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
