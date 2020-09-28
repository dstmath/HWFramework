package android.hardware.radio.V1_1;

import java.util.ArrayList;

public final class EutranBands {
    public static final int BAND_1 = 1;
    public static final int BAND_10 = 10;
    public static final int BAND_11 = 11;
    public static final int BAND_12 = 12;
    public static final int BAND_13 = 13;
    public static final int BAND_14 = 14;
    public static final int BAND_17 = 17;
    public static final int BAND_18 = 18;
    public static final int BAND_19 = 19;
    public static final int BAND_2 = 2;
    public static final int BAND_20 = 20;
    public static final int BAND_21 = 21;
    public static final int BAND_22 = 22;
    public static final int BAND_23 = 23;
    public static final int BAND_24 = 24;
    public static final int BAND_25 = 25;
    public static final int BAND_26 = 26;
    public static final int BAND_27 = 27;
    public static final int BAND_28 = 28;
    public static final int BAND_3 = 3;
    public static final int BAND_30 = 30;
    public static final int BAND_31 = 31;
    public static final int BAND_33 = 33;
    public static final int BAND_34 = 34;
    public static final int BAND_35 = 35;
    public static final int BAND_36 = 36;
    public static final int BAND_37 = 37;
    public static final int BAND_38 = 38;
    public static final int BAND_39 = 39;
    public static final int BAND_4 = 4;
    public static final int BAND_40 = 40;
    public static final int BAND_41 = 41;
    public static final int BAND_42 = 42;
    public static final int BAND_43 = 43;
    public static final int BAND_44 = 44;
    public static final int BAND_45 = 45;
    public static final int BAND_46 = 46;
    public static final int BAND_47 = 47;
    public static final int BAND_48 = 48;
    public static final int BAND_5 = 5;
    public static final int BAND_6 = 6;
    public static final int BAND_65 = 65;
    public static final int BAND_66 = 66;
    public static final int BAND_68 = 68;
    public static final int BAND_7 = 7;
    public static final int BAND_70 = 70;
    public static final int BAND_8 = 8;
    public static final int BAND_9 = 9;

    public static final String toString(int o) {
        if (o == 1) {
            return "BAND_1";
        }
        if (o == 2) {
            return "BAND_2";
        }
        if (o == 3) {
            return "BAND_3";
        }
        if (o == 4) {
            return "BAND_4";
        }
        if (o == 5) {
            return "BAND_5";
        }
        if (o == 6) {
            return "BAND_6";
        }
        if (o == 7) {
            return "BAND_7";
        }
        if (o == 8) {
            return "BAND_8";
        }
        if (o == 9) {
            return "BAND_9";
        }
        if (o == 10) {
            return "BAND_10";
        }
        if (o == 11) {
            return "BAND_11";
        }
        if (o == 12) {
            return "BAND_12";
        }
        if (o == 13) {
            return "BAND_13";
        }
        if (o == 14) {
            return "BAND_14";
        }
        if (o == 17) {
            return "BAND_17";
        }
        if (o == 18) {
            return "BAND_18";
        }
        if (o == 19) {
            return "BAND_19";
        }
        if (o == 20) {
            return "BAND_20";
        }
        if (o == 21) {
            return "BAND_21";
        }
        if (o == 22) {
            return "BAND_22";
        }
        if (o == 23) {
            return "BAND_23";
        }
        if (o == 24) {
            return "BAND_24";
        }
        if (o == 25) {
            return "BAND_25";
        }
        if (o == 26) {
            return "BAND_26";
        }
        if (o == 27) {
            return "BAND_27";
        }
        if (o == 28) {
            return "BAND_28";
        }
        if (o == 30) {
            return "BAND_30";
        }
        if (o == 31) {
            return "BAND_31";
        }
        if (o == 33) {
            return "BAND_33";
        }
        if (o == 34) {
            return "BAND_34";
        }
        if (o == 35) {
            return "BAND_35";
        }
        if (o == 36) {
            return "BAND_36";
        }
        if (o == 37) {
            return "BAND_37";
        }
        if (o == 38) {
            return "BAND_38";
        }
        if (o == 39) {
            return "BAND_39";
        }
        if (o == 40) {
            return "BAND_40";
        }
        if (o == 41) {
            return "BAND_41";
        }
        if (o == 42) {
            return "BAND_42";
        }
        if (o == 43) {
            return "BAND_43";
        }
        if (o == 44) {
            return "BAND_44";
        }
        if (o == 45) {
            return "BAND_45";
        }
        if (o == 46) {
            return "BAND_46";
        }
        if (o == 47) {
            return "BAND_47";
        }
        if (o == 48) {
            return "BAND_48";
        }
        if (o == 65) {
            return "BAND_65";
        }
        if (o == 66) {
            return "BAND_66";
        }
        if (o == 68) {
            return "BAND_68";
        }
        if (o == 70) {
            return "BAND_70";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        if ((o & 1) == 1) {
            list.add("BAND_1");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("BAND_2");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("BAND_3");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("BAND_4");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("BAND_5");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("BAND_6");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("BAND_7");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("BAND_8");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("BAND_9");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("BAND_10");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("BAND_11");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("BAND_12");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("BAND_13");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("BAND_14");
            flipped |= 14;
        }
        if ((o & 17) == 17) {
            list.add("BAND_17");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("BAND_18");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("BAND_19");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("BAND_20");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("BAND_21");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("BAND_22");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("BAND_23");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("BAND_24");
            flipped |= 24;
        }
        if ((o & 25) == 25) {
            list.add("BAND_25");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("BAND_26");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("BAND_27");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("BAND_28");
            flipped |= 28;
        }
        if ((o & 30) == 30) {
            list.add("BAND_30");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("BAND_31");
            flipped |= 31;
        }
        if ((o & 33) == 33) {
            list.add("BAND_33");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("BAND_34");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("BAND_35");
            flipped |= 35;
        }
        if ((o & 36) == 36) {
            list.add("BAND_36");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("BAND_37");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("BAND_38");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("BAND_39");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("BAND_40");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("BAND_41");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("BAND_42");
            flipped |= 42;
        }
        if ((o & 43) == 43) {
            list.add("BAND_43");
            flipped |= 43;
        }
        if ((o & 44) == 44) {
            list.add("BAND_44");
            flipped |= 44;
        }
        if ((o & 45) == 45) {
            list.add("BAND_45");
            flipped |= 45;
        }
        if ((o & 46) == 46) {
            list.add("BAND_46");
            flipped |= 46;
        }
        if ((o & 47) == 47) {
            list.add("BAND_47");
            flipped |= 47;
        }
        if ((o & 48) == 48) {
            list.add("BAND_48");
            flipped |= 48;
        }
        if ((o & 65) == 65) {
            list.add("BAND_65");
            flipped |= 65;
        }
        if ((o & 66) == 66) {
            list.add("BAND_66");
            flipped |= 66;
        }
        if ((o & 68) == 68) {
            list.add("BAND_68");
            flipped |= 68;
        }
        if ((o & 70) == 70) {
            list.add("BAND_70");
            flipped |= 70;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
