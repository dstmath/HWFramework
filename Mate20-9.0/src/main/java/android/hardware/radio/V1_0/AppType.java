package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class AppType {
    public static final int CSIM = 4;
    public static final int ISIM = 5;
    public static final int RUIM = 3;
    public static final int SIM = 1;
    public static final int UNKNOWN = 0;
    public static final int USIM = 2;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "SIM";
        }
        if (o == 2) {
            return "USIM";
        }
        if (o == 3) {
            return "RUIM";
        }
        if (o == 4) {
            return "CSIM";
        }
        if (o == 5) {
            return "ISIM";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("SIM");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("USIM");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("RUIM");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CSIM");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("ISIM");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
