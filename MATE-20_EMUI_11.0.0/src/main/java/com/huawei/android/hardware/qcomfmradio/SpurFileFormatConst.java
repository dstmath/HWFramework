package com.huawei.android.hardware.qcomfmradio;

import java.util.regex.Pattern;

/* access modifiers changed from: package-private */
public final class SpurFileFormatConst {
    public static final char COMMENT = '#';
    public static final char DELIMETER = '=';
    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
    public static int SPUR_DETAILS_FOR_EACH_FREQ_CNT = 5;
    public static final String SPUR_FILTER_COEFF = "FilterCoefficeint";
    public static final String SPUR_FREQ = "SpurFreq";
    public static final String SPUR_IS_ENABLE = "IsEnableSpur";
    public static final String SPUR_LEVEL = "SpurLevel";
    public static final String SPUR_LSB_LENGTH = "LsbOfIntegrationLength";
    public static final String SPUR_MODE = "Mode";
    public static final String SPUR_NO_OF = "NoOfSpursToTrack";
    public static final String SPUR_NUM_ENTRY = "SpurNumEntry";
    public static final String SPUR_ROTATION_VALUE = "RotationValue";

    public enum LineType {
        EMPTY_LINE,
        SPUR_MODE_LINE,
        SPUR_N_ENTRY_LINE,
        SPUR_FR_LINE,
        SPUR_NO_OF_LINE,
        SPUR_ROT0_LINE,
        SPUR_LSB0_LINE,
        SPUR_FILTER0_LINE,
        SPUR_ENABLE0_LINE,
        SPUR_LEVEL0_LINE
    }

    SpurFileFormatConst() {
    }
}
