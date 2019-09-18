package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class FeatureID {
    public static final int FEATURE_ALL = 16;
    public static final int FEATURE_BLC = 13;
    public static final int FEATURE_CONTRAST = 2;
    public static final int FEATURE_DDIC_CABC = 12;
    public static final int FEATURE_DDIC_COLOR = 10;
    public static final int FEATURE_DDIC_GAMM = 9;
    public static final int FEATURE_DDIC_RGBW = 11;
    public static final int FEATURE_EYE_PROTECT = 15;
    public static final int FEATURE_GAMMA = 7;
    public static final int FEATURE_GMP = 3;
    public static final int FEATURE_HUE = 5;
    public static final int FEATURE_IGAMMA = 8;
    public static final int FEATURE_MAX = 17;
    public static final int FEATURE_PANEL_INFO = 14;
    public static final int FEATURE_SAT = 6;
    public static final int FEATURE_SHARP = 0;
    public static final int FEATURE_SHARP2P = 1;
    public static final int FEATURE_XCC = 4;

    public static final String toString(int o) {
        if (o == 0) {
            return "FEATURE_SHARP";
        }
        if (o == 1) {
            return "FEATURE_SHARP2P";
        }
        if (o == 2) {
            return "FEATURE_CONTRAST";
        }
        if (o == 3) {
            return "FEATURE_GMP";
        }
        if (o == 4) {
            return "FEATURE_XCC";
        }
        if (o == 5) {
            return "FEATURE_HUE";
        }
        if (o == 6) {
            return "FEATURE_SAT";
        }
        if (o == 7) {
            return "FEATURE_GAMMA";
        }
        if (o == 8) {
            return "FEATURE_IGAMMA";
        }
        if (o == 9) {
            return "FEATURE_DDIC_GAMM";
        }
        if (o == 10) {
            return "FEATURE_DDIC_COLOR";
        }
        if (o == 11) {
            return "FEATURE_DDIC_RGBW";
        }
        if (o == 12) {
            return "FEATURE_DDIC_CABC";
        }
        if (o == 13) {
            return "FEATURE_BLC";
        }
        if (o == 14) {
            return "FEATURE_PANEL_INFO";
        }
        if (o == 15) {
            return "FEATURE_EYE_PROTECT";
        }
        if (o == 16) {
            return "FEATURE_ALL";
        }
        if (o == 17) {
            return "FEATURE_MAX";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("FEATURE_SHARP");
        if ((o & 1) == 1) {
            list.add("FEATURE_SHARP2P");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("FEATURE_CONTRAST");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("FEATURE_GMP");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("FEATURE_XCC");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("FEATURE_HUE");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("FEATURE_SAT");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("FEATURE_GAMMA");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("FEATURE_IGAMMA");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("FEATURE_DDIC_GAMM");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("FEATURE_DDIC_COLOR");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("FEATURE_DDIC_RGBW");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("FEATURE_DDIC_CABC");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("FEATURE_BLC");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("FEATURE_PANEL_INFO");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("FEATURE_EYE_PROTECT");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("FEATURE_ALL");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("FEATURE_MAX");
            flipped |= 17;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
