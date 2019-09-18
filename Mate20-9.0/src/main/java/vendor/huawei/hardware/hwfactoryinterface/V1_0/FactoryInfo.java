package vendor.huawei.hardware.hwfactoryinterface.V1_0;

import java.util.ArrayList;

public final class FactoryInfo {
    public static final int BT = 2;
    public static final int BTL = 13;
    public static final int CBC = 12;
    public static final int CBG = 10;
    public static final int CBL = 8;
    public static final int CBS = 9;
    public static final int CBU = 11;
    public static final int CT = 1;
    public static final int CTL = 14;
    public static final int CW = 6;
    public static final int DBC = 0;
    public static final int LT = 5;
    public static final int MC = 7;
    public static final int MM2S = 16;
    public static final int MMAS = 18;
    public static final int MMI1 = 22;
    public static final int MMI2 = 19;
    public static final int MMIA = 17;
    public static final int MMIBB = 28;
    public static final int MMIE = 24;
    public static final int MMIS = 21;
    public static final int MMIT = 20;
    public static final int MMIV = 27;
    public static final int MT = 4;
    public static final int PT = 3;
    public static final int RT = 15;
    public static final int RT2 = 26;
    public static final int SE = 25;
    public static final int UT = 23;

    public static final String toString(int o) {
        if (o == 0) {
            return "DBC";
        }
        if (o == 1) {
            return "CT";
        }
        if (o == 2) {
            return "BT";
        }
        if (o == 3) {
            return "PT";
        }
        if (o == 4) {
            return "MT";
        }
        if (o == 5) {
            return "LT";
        }
        if (o == 6) {
            return "CW";
        }
        if (o == 7) {
            return "MC";
        }
        if (o == 8) {
            return "CBL";
        }
        if (o == 9) {
            return "CBS";
        }
        if (o == 10) {
            return "CBG";
        }
        if (o == 11) {
            return "CBU";
        }
        if (o == 12) {
            return "CBC";
        }
        if (o == 13) {
            return "BTL";
        }
        if (o == 14) {
            return "CTL";
        }
        if (o == 15) {
            return "RT";
        }
        if (o == 16) {
            return "MM2S";
        }
        if (o == 17) {
            return "MMIA";
        }
        if (o == 18) {
            return "MMAS";
        }
        if (o == 19) {
            return "MMI2";
        }
        if (o == 20) {
            return "MMIT";
        }
        if (o == 21) {
            return "MMIS";
        }
        if (o == 22) {
            return "MMI1";
        }
        if (o == 23) {
            return "UT";
        }
        if (o == 24) {
            return "MMIE";
        }
        if (o == 25) {
            return "SE";
        }
        if (o == 26) {
            return "RT2";
        }
        if (o == 27) {
            return "MMIV";
        }
        if (o == 28) {
            return "MMIBB";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("DBC");
        if ((o & 1) == 1) {
            list.add("CT");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BT");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("PT");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("MT");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("LT");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("CW");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("MC");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("CBL");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("CBS");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("CBG");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("CBU");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("CBC");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("BTL");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("CTL");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("RT");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("MM2S");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("MMIA");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("MMAS");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("MMI2");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("MMIT");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("MMIS");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("MMI1");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("UT");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("MMIE");
            flipped |= 24;
        }
        if ((o & 25) == 25) {
            list.add("SE");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("RT2");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("MMIV");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("MMIBB");
            flipped |= 28;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
