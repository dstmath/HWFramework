package android.hardware.broadcastradio.V2_0;

import java.util.ArrayList;

public final class ConfigFlag {
    public static final int DAB_DAB_LINKING = 6;
    public static final int DAB_DAB_SOFT_LINKING = 8;
    public static final int DAB_FM_LINKING = 7;
    public static final int DAB_FM_SOFT_LINKING = 9;
    public static final int FORCE_ANALOG = 2;
    public static final int FORCE_DIGITAL = 3;
    public static final int FORCE_MONO = 1;
    public static final int RDS_AF = 4;
    public static final int RDS_REG = 5;

    public static final String toString(int o) {
        if (o == 1) {
            return "FORCE_MONO";
        }
        if (o == 2) {
            return "FORCE_ANALOG";
        }
        if (o == 3) {
            return "FORCE_DIGITAL";
        }
        if (o == 4) {
            return "RDS_AF";
        }
        if (o == 5) {
            return "RDS_REG";
        }
        if (o == 6) {
            return "DAB_DAB_LINKING";
        }
        if (o == 7) {
            return "DAB_FM_LINKING";
        }
        if (o == 8) {
            return "DAB_DAB_SOFT_LINKING";
        }
        if (o == 9) {
            return "DAB_FM_SOFT_LINKING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("FORCE_MONO");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FORCE_ANALOG");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("FORCE_DIGITAL");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("RDS_AF");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("RDS_REG");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("DAB_DAB_LINKING");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("DAB_FM_LINKING");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("DAB_DAB_SOFT_LINKING");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("DAB_FM_SOFT_LINKING");
            flipped |= 9;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
