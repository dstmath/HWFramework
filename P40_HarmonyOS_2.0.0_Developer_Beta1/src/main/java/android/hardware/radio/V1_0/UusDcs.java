package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class UusDcs {
    public static final int IA5C = 4;
    public static final int OSIHLP = 1;
    public static final int RMCF = 3;
    public static final int USP = 0;
    public static final int X244 = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "USP";
        }
        if (o == 1) {
            return "OSIHLP";
        }
        if (o == 2) {
            return "X244";
        }
        if (o == 3) {
            return "RMCF";
        }
        if (o == 4) {
            return "IA5C";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("USP");
        if ((o & 1) == 1) {
            list.add("OSIHLP");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("X244");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RMCF");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IA5C");
            flipped |= 4;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
