package android.hardware.radio.V1_0;

import java.util.ArrayList;

public final class RadioTechnology {
    public static final int EDGE = 2;
    public static final int EHRPD = 13;
    public static final int EVDO_0 = 7;
    public static final int EVDO_A = 8;
    public static final int EVDO_B = 12;
    public static final int GPRS = 1;
    public static final int GSM = 16;
    public static final int HSDPA = 9;
    public static final int HSPA = 11;
    public static final int HSPAP = 15;
    public static final int HSUPA = 10;
    public static final int IS95A = 4;
    public static final int IS95B = 5;
    public static final int IWLAN = 18;
    public static final int LTE = 14;
    public static final int LTE_CA = 19;
    public static final int ONE_X_RTT = 6;
    public static final int TD_SCDMA = 17;
    public static final int UMTS = 3;
    public static final int UNKNOWN = 0;

    public static final String toString(int o) {
        if (o == 0) {
            return "UNKNOWN";
        }
        if (o == 1) {
            return "GPRS";
        }
        if (o == 2) {
            return "EDGE";
        }
        if (o == 3) {
            return "UMTS";
        }
        if (o == 4) {
            return "IS95A";
        }
        if (o == 5) {
            return "IS95B";
        }
        if (o == 6) {
            return "ONE_X_RTT";
        }
        if (o == 7) {
            return "EVDO_0";
        }
        if (o == 8) {
            return "EVDO_A";
        }
        if (o == 9) {
            return "HSDPA";
        }
        if (o == 10) {
            return "HSUPA";
        }
        if (o == 11) {
            return "HSPA";
        }
        if (o == 12) {
            return "EVDO_B";
        }
        if (o == 13) {
            return "EHRPD";
        }
        if (o == 14) {
            return "LTE";
        }
        if (o == 15) {
            return "HSPAP";
        }
        if (o == 16) {
            return "GSM";
        }
        if (o == 17) {
            return "TD_SCDMA";
        }
        if (o == 18) {
            return "IWLAN";
        }
        if (o == 19) {
            return "LTE_CA";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("UNKNOWN");
        if ((o & 1) == 1) {
            list.add("GPRS");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("EDGE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("UMTS");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("IS95A");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("IS95B");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("ONE_X_RTT");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("EVDO_0");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("EVDO_A");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("HSDPA");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("HSUPA");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("HSPA");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("EVDO_B");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("EHRPD");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("LTE");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("HSPAP");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("GSM");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("TD_SCDMA");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("IWLAN");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("LTE_CA");
            flipped |= 19;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
