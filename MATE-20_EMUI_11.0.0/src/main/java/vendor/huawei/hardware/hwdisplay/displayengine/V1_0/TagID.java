package vendor.huawei.hardware.hwdisplay.displayengine.V1_0;

import java.util.ArrayList;

public final class TagID {
    public static final int TAG_AL = 10;
    public static final int TAG_ALL = 0;
    public static final int TAG_BACKLIGHT = 21;
    public static final int TAG_BL = 11;
    public static final int TAG_COUNT = 53;
    public static final int TAG_DELTA = 5;
    public static final int TAG_DO_BLC = 17;
    public static final int TAG_DO_HDR10 = 20;
    public static final int TAG_DO_LCE = 16;
    public static final int TAG_DO_LRE = 19;
    public static final int TAG_DO_SRE = 18;
    public static final int TAG_ENABLE = 2;
    public static final int TAG_HBM_PARAMETER = 52;
    public static final int TAG_HIACE_HUE = 29;
    public static final int TAG_HIACE_SATURATION = 30;
    public static final int TAG_HIACE_VALUE = 31;
    public static final int TAG_HIST = 6;
    public static final int TAG_HIST_SIZE = 7;
    public static final int TAG_LEVEL = 4;
    public static final int TAG_LHIST_SFT = 28;
    public static final int TAG_LUT = 8;
    public static final int TAG_LUT_SIZE = 9;
    public static final int TAG_METADATA = 13;
    public static final int TAG_METADATA_LEN = 14;
    public static final int TAG_MODE = 3;
    public static final int TAG_NR_LEVEL = 24;
    public static final int TAG_PANEL_NAME = 27;
    public static final int TAG_PARAM = 1;
    public static final int TAG_RESULT = 22;
    public static final int TAG_RGB_WEIGHT = 25;
    public static final int TAG_S3_BLUE_SIGMA03 = 41;
    public static final int TAG_S3_BLUE_SIGMA45 = 42;
    public static final int TAG_S3_BYPASS_NR = 32;
    public static final int TAG_S3_FILTER_LEVEL = 45;
    public static final int TAG_S3_GREEN_SIGMA03 = 37;
    public static final int TAG_S3_GREEN_SIGMA45 = 38;
    public static final int TAG_S3_HUE = 48;
    public static final int TAG_S3_MIN_MAX_SIGMA = 36;
    public static final int TAG_S3_RED_SIGMA03 = 39;
    public static final int TAG_S3_RED_SIGMA45 = 40;
    public static final int TAG_S3_SATURATION = 49;
    public static final int TAG_S3_SIMILARIT_COEFF = 46;
    public static final int TAG_S3_SKIN_GAIN = 51;
    public static final int TAG_S3_SOME_BRIGHTNESS01 = 33;
    public static final int TAG_S3_SOME_BRIGHTNESS23 = 34;
    public static final int TAG_S3_SOME_BRIGHTNESS4 = 35;
    public static final int TAG_S3_VALUE = 50;
    public static final int TAG_S3_V_FILTER_WEIGHT_ADJ = 47;
    public static final int TAG_S3_WHITE_SIGMA03 = 43;
    public static final int TAG_S3_WHITE_SIGMA45 = 44;
    public static final int TAG_SKIN_GAIN = 23;
    public static final int TAG_SRE_ON_THREHOLD = 26;
    public static final int TAG_THMINV = 12;
    public static final int TAG_TIME = 15;

    public static final String toString(int o) {
        if (o == 0) {
            return "TAG_ALL";
        }
        if (o == 1) {
            return "TAG_PARAM";
        }
        if (o == 2) {
            return "TAG_ENABLE";
        }
        if (o == 3) {
            return "TAG_MODE";
        }
        if (o == 4) {
            return "TAG_LEVEL";
        }
        if (o == 5) {
            return "TAG_DELTA";
        }
        if (o == 6) {
            return "TAG_HIST";
        }
        if (o == 7) {
            return "TAG_HIST_SIZE";
        }
        if (o == 8) {
            return "TAG_LUT";
        }
        if (o == 9) {
            return "TAG_LUT_SIZE";
        }
        if (o == 10) {
            return "TAG_AL";
        }
        if (o == 11) {
            return "TAG_BL";
        }
        if (o == 12) {
            return "TAG_THMINV";
        }
        if (o == 13) {
            return "TAG_METADATA";
        }
        if (o == 14) {
            return "TAG_METADATA_LEN";
        }
        if (o == 15) {
            return "TAG_TIME";
        }
        if (o == 16) {
            return "TAG_DO_LCE";
        }
        if (o == 17) {
            return "TAG_DO_BLC";
        }
        if (o == 18) {
            return "TAG_DO_SRE";
        }
        if (o == 19) {
            return "TAG_DO_LRE";
        }
        if (o == 20) {
            return "TAG_DO_HDR10";
        }
        if (o == 21) {
            return "TAG_BACKLIGHT";
        }
        if (o == 22) {
            return "TAG_RESULT";
        }
        if (o == 23) {
            return "TAG_SKIN_GAIN";
        }
        if (o == 24) {
            return "TAG_NR_LEVEL";
        }
        if (o == 25) {
            return "TAG_RGB_WEIGHT";
        }
        if (o == 26) {
            return "TAG_SRE_ON_THREHOLD";
        }
        if (o == 27) {
            return "TAG_PANEL_NAME";
        }
        if (o == 28) {
            return "TAG_LHIST_SFT";
        }
        if (o == 29) {
            return "TAG_HIACE_HUE";
        }
        if (o == 30) {
            return "TAG_HIACE_SATURATION";
        }
        if (o == 31) {
            return "TAG_HIACE_VALUE";
        }
        if (o == 32) {
            return "TAG_S3_BYPASS_NR";
        }
        if (o == 33) {
            return "TAG_S3_SOME_BRIGHTNESS01";
        }
        if (o == 34) {
            return "TAG_S3_SOME_BRIGHTNESS23";
        }
        if (o == 35) {
            return "TAG_S3_SOME_BRIGHTNESS4";
        }
        if (o == 36) {
            return "TAG_S3_MIN_MAX_SIGMA";
        }
        if (o == 37) {
            return "TAG_S3_GREEN_SIGMA03";
        }
        if (o == 38) {
            return "TAG_S3_GREEN_SIGMA45";
        }
        if (o == 39) {
            return "TAG_S3_RED_SIGMA03";
        }
        if (o == 40) {
            return "TAG_S3_RED_SIGMA45";
        }
        if (o == 41) {
            return "TAG_S3_BLUE_SIGMA03";
        }
        if (o == 42) {
            return "TAG_S3_BLUE_SIGMA45";
        }
        if (o == 43) {
            return "TAG_S3_WHITE_SIGMA03";
        }
        if (o == 44) {
            return "TAG_S3_WHITE_SIGMA45";
        }
        if (o == 45) {
            return "TAG_S3_FILTER_LEVEL";
        }
        if (o == 46) {
            return "TAG_S3_SIMILARIT_COEFF";
        }
        if (o == 47) {
            return "TAG_S3_V_FILTER_WEIGHT_ADJ";
        }
        if (o == 48) {
            return "TAG_S3_HUE";
        }
        if (o == 49) {
            return "TAG_S3_SATURATION";
        }
        if (o == 50) {
            return "TAG_S3_VALUE";
        }
        if (o == 51) {
            return "TAG_S3_SKIN_GAIN";
        }
        if (o == 52) {
            return "TAG_HBM_PARAMETER";
        }
        if (o == 53) {
            return "TAG_COUNT";
        }
        return "0x" + Integer.toHexString(o);
    }

    public static final String dumpBitfield(int o) {
        ArrayList<String> list = new ArrayList<>();
        int flipped = 0;
        list.add("TAG_ALL");
        if ((o & 1) == 1) {
            list.add("TAG_PARAM");
            flipped = 0 | 1;
        }
        if ((o & 2) == 2) {
            list.add("TAG_ENABLE");
            flipped |= 2;
        }
        if ((o & 3) == 3) {
            list.add("TAG_MODE");
            flipped |= 3;
        }
        if ((o & 4) == 4) {
            list.add("TAG_LEVEL");
            flipped |= 4;
        }
        if ((o & 5) == 5) {
            list.add("TAG_DELTA");
            flipped |= 5;
        }
        if ((o & 6) == 6) {
            list.add("TAG_HIST");
            flipped |= 6;
        }
        if ((o & 7) == 7) {
            list.add("TAG_HIST_SIZE");
            flipped |= 7;
        }
        if ((o & 8) == 8) {
            list.add("TAG_LUT");
            flipped |= 8;
        }
        if ((o & 9) == 9) {
            list.add("TAG_LUT_SIZE");
            flipped |= 9;
        }
        if ((o & 10) == 10) {
            list.add("TAG_AL");
            flipped |= 10;
        }
        if ((o & 11) == 11) {
            list.add("TAG_BL");
            flipped |= 11;
        }
        if ((o & 12) == 12) {
            list.add("TAG_THMINV");
            flipped |= 12;
        }
        if ((o & 13) == 13) {
            list.add("TAG_METADATA");
            flipped |= 13;
        }
        if ((o & 14) == 14) {
            list.add("TAG_METADATA_LEN");
            flipped |= 14;
        }
        if ((o & 15) == 15) {
            list.add("TAG_TIME");
            flipped |= 15;
        }
        if ((o & 16) == 16) {
            list.add("TAG_DO_LCE");
            flipped |= 16;
        }
        if ((o & 17) == 17) {
            list.add("TAG_DO_BLC");
            flipped |= 17;
        }
        if ((o & 18) == 18) {
            list.add("TAG_DO_SRE");
            flipped |= 18;
        }
        if ((o & 19) == 19) {
            list.add("TAG_DO_LRE");
            flipped |= 19;
        }
        if ((o & 20) == 20) {
            list.add("TAG_DO_HDR10");
            flipped |= 20;
        }
        if ((o & 21) == 21) {
            list.add("TAG_BACKLIGHT");
            flipped |= 21;
        }
        if ((o & 22) == 22) {
            list.add("TAG_RESULT");
            flipped |= 22;
        }
        if ((o & 23) == 23) {
            list.add("TAG_SKIN_GAIN");
            flipped |= 23;
        }
        if ((o & 24) == 24) {
            list.add("TAG_NR_LEVEL");
            flipped |= 24;
        }
        if ((o & 25) == 25) {
            list.add("TAG_RGB_WEIGHT");
            flipped |= 25;
        }
        if ((o & 26) == 26) {
            list.add("TAG_SRE_ON_THREHOLD");
            flipped |= 26;
        }
        if ((o & 27) == 27) {
            list.add("TAG_PANEL_NAME");
            flipped |= 27;
        }
        if ((o & 28) == 28) {
            list.add("TAG_LHIST_SFT");
            flipped |= 28;
        }
        if ((o & 29) == 29) {
            list.add("TAG_HIACE_HUE");
            flipped |= 29;
        }
        if ((o & 30) == 30) {
            list.add("TAG_HIACE_SATURATION");
            flipped |= 30;
        }
        if ((o & 31) == 31) {
            list.add("TAG_HIACE_VALUE");
            flipped |= 31;
        }
        if ((o & 32) == 32) {
            list.add("TAG_S3_BYPASS_NR");
            flipped |= 32;
        }
        if ((o & 33) == 33) {
            list.add("TAG_S3_SOME_BRIGHTNESS01");
            flipped |= 33;
        }
        if ((o & 34) == 34) {
            list.add("TAG_S3_SOME_BRIGHTNESS23");
            flipped |= 34;
        }
        if ((o & 35) == 35) {
            list.add("TAG_S3_SOME_BRIGHTNESS4");
            flipped |= 35;
        }
        if ((o & 36) == 36) {
            list.add("TAG_S3_MIN_MAX_SIGMA");
            flipped |= 36;
        }
        if ((o & 37) == 37) {
            list.add("TAG_S3_GREEN_SIGMA03");
            flipped |= 37;
        }
        if ((o & 38) == 38) {
            list.add("TAG_S3_GREEN_SIGMA45");
            flipped |= 38;
        }
        if ((o & 39) == 39) {
            list.add("TAG_S3_RED_SIGMA03");
            flipped |= 39;
        }
        if ((o & 40) == 40) {
            list.add("TAG_S3_RED_SIGMA45");
            flipped |= 40;
        }
        if ((o & 41) == 41) {
            list.add("TAG_S3_BLUE_SIGMA03");
            flipped |= 41;
        }
        if ((o & 42) == 42) {
            list.add("TAG_S3_BLUE_SIGMA45");
            flipped |= 42;
        }
        if ((o & 43) == 43) {
            list.add("TAG_S3_WHITE_SIGMA03");
            flipped |= 43;
        }
        if ((o & 44) == 44) {
            list.add("TAG_S3_WHITE_SIGMA45");
            flipped |= 44;
        }
        if ((o & 45) == 45) {
            list.add("TAG_S3_FILTER_LEVEL");
            flipped |= 45;
        }
        if ((o & 46) == 46) {
            list.add("TAG_S3_SIMILARIT_COEFF");
            flipped |= 46;
        }
        if ((o & 47) == 47) {
            list.add("TAG_S3_V_FILTER_WEIGHT_ADJ");
            flipped |= 47;
        }
        if ((o & 48) == 48) {
            list.add("TAG_S3_HUE");
            flipped |= 48;
        }
        if ((o & 49) == 49) {
            list.add("TAG_S3_SATURATION");
            flipped |= 49;
        }
        if ((o & 50) == 50) {
            list.add("TAG_S3_VALUE");
            flipped |= 50;
        }
        if ((o & 51) == 51) {
            list.add("TAG_S3_SKIN_GAIN");
            flipped |= 51;
        }
        if ((o & 52) == 52) {
            list.add("TAG_HBM_PARAMETER");
            flipped |= 52;
        }
        if ((o & 53) == 53) {
            list.add("TAG_COUNT");
            flipped |= 53;
        }
        if (o != flipped) {
            list.add("0x" + Integer.toHexString((~flipped) & o));
        }
        return String.join(" | ", list);
    }
}
