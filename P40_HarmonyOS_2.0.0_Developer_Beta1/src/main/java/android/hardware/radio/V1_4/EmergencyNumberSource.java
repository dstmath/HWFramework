package android.hardware.radio.V1_4;

import java.util.ArrayList;

public final class EmergencyNumberSource {
    public static final int DEFAULT = 8;
    public static final int MODEM_CONFIG = 4;
    public static final int NETWORK_SIGNALING = 1;
    public static final int SIM = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "NETWORK_SIGNALING";
        }
        if (o == 2) {
            return "SIM";
        }
        if (o == 4) {
            return "MODEM_CONFIG";
        }
        if (o == 8) {
            return "DEFAULT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("NETWORK_SIGNALING");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("SIM");
            flipped |= 2;
        }
        if ((o & 4) == 4) {
            list.add("MODEM_CONFIG");
            flipped |= 4;
        }
        if ((o & 8) == 8) {
            list.add("DEFAULT");
            flipped |= 8;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
