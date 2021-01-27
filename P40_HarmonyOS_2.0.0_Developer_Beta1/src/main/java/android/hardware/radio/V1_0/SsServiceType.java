package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class SsServiceType {
    public static final int ALL_BARRING = 16;
    public static final int BAIC = 14;
    public static final int BAIC_ROAMING = 15;
    public static final int BAOC = 11;
    public static final int BAOIC = 12;
    public static final int BAOIC_EXC_HOME = 13;
    public static final int CFU = 0;
    public static final int CF_ALL = 4;
    public static final int CF_ALL_CONDITIONAL = 5;
    public static final int CF_BUSY = 1;
    public static final int CF_NOT_REACHABLE = 3;
    public static final int CF_NO_REPLY = 2;
    public static final int CLIP = 6;
    public static final int CLIR = 7;
    public static final int COLP = 8;
    public static final int COLR = 9;
    public static final int INCOMING_BARRING = 18;
    public static final int OUTGOING_BARRING = 17;
    public static final int WAIT = 10;

    public static final String toString(int o) {
        if (o == 0) {
            return "CFU";
        }
        if (o == 1) {
            return "CF_BUSY";
        }
        if (o == 2) {
            return "CF_NO_REPLY";
        }
        if (o == 3) {
            return "CF_NOT_REACHABLE";
        }
        if (o == 4) {
            return "CF_ALL";
        }
        if (o == 5) {
            return "CF_ALL_CONDITIONAL";
        }
        if (o == 6) {
            return "CLIP";
        }
        if (o == 7) {
            return "CLIR";
        }
        if (o == 8) {
            return "COLP";
        }
        if (o == 9) {
            return "COLR";
        }
        if (o == 10) {
            return "WAIT";
        }
        if (o == 11) {
            return "BAOC";
        }
        if (o == 12) {
            return "BAOIC";
        }
        if (o == 13) {
            return "BAOIC_EXC_HOME";
        }
        if (o == 14) {
            return "BAIC";
        }
        if (o == 15) {
            return "BAIC_ROAMING";
        }
        if (o == 16) {
            return "ALL_BARRING";
        }
        if (o == 17) {
            return "OUTGOING_BARRING";
        }
        if (o == 18) {
            return "INCOMING_BARRING";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("CFU");
        if ((o & 1) == 1) {
            list.add("CF_BUSY");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("CF_NO_REPLY");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("CF_NOT_REACHABLE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("CF_ALL");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("CF_ALL_CONDITIONAL");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("CLIP");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("CLIR");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("COLP");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("COLR");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("WAIT");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("BAOC");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("BAOIC");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("BAOIC_EXC_HOME");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("BAIC");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("BAIC_ROAMING");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("ALL_BARRING");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("OUTGOING_BARRING");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("INCOMING_BARRING");
            flipped |= 18;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
