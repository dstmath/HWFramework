package android.hardware.wifi.V1_0;

import java.util.ArrayList;

public final class RttPeerType {
    public static final int AP = 1;
    public static final int NAN = 5;
    public static final int P2P_CLIENT = 4;
    public static final int P2P_GO = 3;
    public static final int STA = 2;

    public static final String toString(int o) {
        if (o == 1) {
            return "AP";
        }
        if (o == 2) {
            return "STA";
        }
        if (o == 3) {
            return "P2P_GO";
        }
        if (o == 4) {
            return "P2P_CLIENT";
        }
        if (o == 5) {
            return "NAN";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("AP");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("STA");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("P2P_GO");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("P2P_CLIENT");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("NAN");
            flipped |= 5;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
