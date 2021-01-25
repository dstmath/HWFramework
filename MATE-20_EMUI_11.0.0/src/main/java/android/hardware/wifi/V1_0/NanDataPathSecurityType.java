package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class NanDataPathSecurityType {
    public static final int OPEN = 0;
    public static final int PASSPHRASE = 2;
    public static final int PMK = 1;

    public static final String toString(int o) {
        if (o == 0) {
            return "OPEN";
        }
        if (o == 1) {
            return "PMK";
        }
        if (o == 2) {
            return "PASSPHRASE";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("OPEN");
        if ((o & 1) == 1) {
            list.add("PMK");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("PASSPHRASE");
            flipped |= 2;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
