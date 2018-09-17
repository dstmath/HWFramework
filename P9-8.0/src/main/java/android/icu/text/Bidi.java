package android.icu.text;

import android.icu.impl.UBiDiProps;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;

public class Bidi {
    static final byte AL = (byte) 13;
    static final byte AN = (byte) 5;
    static final byte B = (byte) 7;
    static final byte BN = (byte) 18;
    @Deprecated
    public static final int CLASS_DEFAULT = 23;
    private static final char CR = '\r';
    static final byte CS = (byte) 6;
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = 126;
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = 127;
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;
    public static final short DO_MIRRORING = (short) 2;
    static final int[] DirPropFlagE = new int[]{DirPropFlag((byte) 11), DirPropFlag((byte) 14)};
    static final int[] DirPropFlagLR = new int[]{DirPropFlag((byte) 0), DirPropFlag((byte) 1)};
    static final int DirPropFlagMultiRuns = DirPropFlag((byte) 31);
    static final int[] DirPropFlagO = new int[]{DirPropFlag((byte) 12), DirPropFlag((byte) 15)};
    static final byte EN = (byte) 2;
    static final byte ENL = (byte) 23;
    static final byte ENR = (byte) 24;
    static final byte ES = (byte) 3;
    static final byte ET = (byte) 4;
    static final int FIRSTALLOC = 10;
    static final byte FOUND_L = ((byte) DirPropFlag((byte) 0));
    static final byte FOUND_R = ((byte) DirPropFlag((byte) 1));
    static final byte FSI = (byte) 19;
    private static final int IMPTABLEVELS_COLUMNS = 8;
    private static final int IMPTABLEVELS_RES = 7;
    private static final int IMPTABPROPS_COLUMNS = 16;
    private static final int IMPTABPROPS_RES = 15;
    public static final short INSERT_LRM_FOR_NUMERIC = (short) 4;
    static final int ISOLATE = 256;
    public static final short KEEP_BASE_COMBINING = (short) 1;
    static final byte L = (byte) 0;
    public static final byte LEVEL_DEFAULT_LTR = (byte) 126;
    public static final byte LEVEL_DEFAULT_RTL = Byte.MAX_VALUE;
    public static final byte LEVEL_OVERRIDE = Byte.MIN_VALUE;
    private static final char LF = '\n';
    static final int LOOKING_FOR_PDI = 3;
    static final byte LRE = (byte) 11;
    static final byte LRI = (byte) 20;
    static final int LRM_AFTER = 2;
    static final int LRM_BEFORE = 1;
    static final byte LRO = (byte) 12;
    public static final byte LTR = (byte) 0;
    public static final int MAP_NOWHERE = -1;
    static final int MASK_BN_EXPLICIT = (DirPropFlag((byte) 18) | MASK_EXPLICIT);
    static final int MASK_B_S = (DirPropFlag((byte) 7) | DirPropFlag((byte) 8));
    static final int MASK_EMBEDDING = (DirPropFlag((byte) 17) | MASK_POSSIBLE_N);
    static final int MASK_EXPLICIT = ((((DirPropFlag((byte) 11) | DirPropFlag((byte) 12)) | DirPropFlag((byte) 14)) | DirPropFlag((byte) 15)) | DirPropFlag((byte) 16));
    static final int MASK_ISO = (((DirPropFlag((byte) 20) | DirPropFlag((byte) 21)) | DirPropFlag((byte) 19)) | DirPropFlag((byte) 22));
    static final int MASK_LTR = (((((((DirPropFlag((byte) 0) | DirPropFlag((byte) 2)) | DirPropFlag((byte) 23)) | DirPropFlag((byte) 24)) | DirPropFlag((byte) 5)) | DirPropFlag((byte) 11)) | DirPropFlag((byte) 12)) | DirPropFlag((byte) 20));
    static final int MASK_POSSIBLE_N = ((((DirPropFlag((byte) 10) | DirPropFlag((byte) 6)) | DirPropFlag((byte) 3)) | DirPropFlag((byte) 4)) | MASK_WS);
    static final int MASK_RTL = ((((DirPropFlag((byte) 1) | DirPropFlag((byte) 13)) | DirPropFlag((byte) 14)) | DirPropFlag((byte) 15)) | DirPropFlag((byte) 21));
    static final int MASK_R_AL = (DirPropFlag((byte) 1) | DirPropFlag((byte) 13));
    static final int MASK_STRONG_EN_AN = ((((DirPropFlag((byte) 0) | DirPropFlag((byte) 1)) | DirPropFlag((byte) 13)) | DirPropFlag((byte) 2)) | DirPropFlag((byte) 5));
    static final int MASK_WS = (((MASK_B_S | DirPropFlag((byte) 9)) | MASK_BN_EXPLICIT) | MASK_ISO);
    public static final byte MAX_EXPLICIT_LEVEL = (byte) 125;
    public static final byte MIXED = (byte) 2;
    public static final byte NEUTRAL = (byte) 3;
    static final int NOT_SEEKING_STRONG = 0;
    static final byte NSM = (byte) 17;
    static final byte ON = (byte) 10;
    public static final int OPTION_DEFAULT = 0;
    public static final int OPTION_INSERT_MARKS = 1;
    public static final int OPTION_REMOVE_CONTROLS = 2;
    public static final int OPTION_STREAMING = 4;
    public static final short OUTPUT_REVERSE = (short) 16;
    static final byte PDF = (byte) 16;
    static final byte PDI = (byte) 22;
    static final byte R = (byte) 1;
    public static final short REMOVE_BIDI_CONTROLS = (short) 8;
    static final short REORDER_COUNT = (short) 7;
    public static final short REORDER_DEFAULT = (short) 0;
    public static final short REORDER_GROUP_NUMBERS_WITH_R = (short) 2;
    public static final short REORDER_INVERSE_FOR_NUMBERS_SPECIAL = (short) 6;
    public static final short REORDER_INVERSE_LIKE_DIRECT = (short) 5;
    public static final short REORDER_INVERSE_NUMBERS_AS_L = (short) 4;
    static final short REORDER_LAST_LOGICAL_TO_VISUAL = (short) 1;
    public static final short REORDER_NUMBERS_SPECIAL = (short) 1;
    public static final short REORDER_RUNS_ONLY = (short) 3;
    static final byte RLE = (byte) 14;
    static final byte RLI = (byte) 21;
    static final int RLM_AFTER = 8;
    static final int RLM_BEFORE = 4;
    static final byte RLO = (byte) 15;
    public static final byte RTL = (byte) 1;
    static final byte S = (byte) 8;
    static final int SEEKING_STRONG_FOR_FSI = 2;
    static final int SEEKING_STRONG_FOR_PARA = 1;
    static final int SIMPLE_OPENINGS_COUNT = 20;
    static final int SIMPLE_PARAS_COUNT = 10;
    static final byte WS = (byte) 9;
    private static final short _AN = (short) 3;
    private static final short _B = (short) 6;
    private static final short _EN = (short) 2;
    private static final short _L = (short) 0;
    private static final short _ON = (short) 4;
    private static final short _R = (short) 1;
    private static final short _S = (short) 5;
    private static final short[] groupProp = new short[]{(short) 0, (short) 1, (short) 2, (short) 7, (short) 8, (short) 3, (short) 9, (short) 6, (short) 5, (short) 4, (short) 4, (short) 10, (short) 10, (short) 12, (short) 10, (short) 10, (short) 10, (short) 11, (short) 10, (short) 4, (short) 4, (short) 4, (short) 4, (short) 13, (short) 14};
    private static final short[] impAct0 = new short[]{(short) 0, (short) 1, (short) 2, (short) 3, (short) 4};
    private static final short[] impAct1 = new short[]{(short) 0, (short) 1, (short) 13, (short) 14};
    private static final short[] impAct2 = new short[]{(short) 0, (short) 1, (short) 2, (short) 5, (short) 6, (short) 7, (short) 8};
    private static final short[] impAct3 = new short[]{(short) 0, (short) 1, (short) 9, (short) 10, (short) 11, (short) 12};
    private static final byte[][] impTabL_DEFAULT = new byte[][]{new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 1, (byte) 3, (byte) 3, (byte) 20, (byte) 20, (byte) 0, (byte) 1}, new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 2, (byte) 21, (byte) 21, (byte) 0, (byte) 2}, new byte[]{(byte) 0, (byte) 1, (byte) 3, (byte) 3, (byte) 20, (byte) 20, (byte) 0, (byte) 2}, new byte[]{(byte) 0, (byte) 33, (byte) 51, (byte) 51, (byte) 4, (byte) 4, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 33, (byte) 0, (byte) 50, (byte) 5, (byte) 5, (byte) 0, (byte) 0}};
    private static final byte[][] impTabL_GROUP_NUMBERS_WITH_R = new byte[][]{new byte[]{(byte) 0, (byte) 3, (byte) 17, (byte) 17, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 32, (byte) 3, (byte) 1, (byte) 1, (byte) 2, (byte) 32, (byte) 32, (byte) 2}, new byte[]{(byte) 32, (byte) 3, (byte) 1, (byte) 1, (byte) 2, (byte) 32, (byte) 32, (byte) 1}, new byte[]{(byte) 0, (byte) 3, (byte) 5, (byte) 5, (byte) 20, (byte) 0, (byte) 0, (byte) 1}, new byte[]{(byte) 32, (byte) 3, (byte) 5, (byte) 5, (byte) 4, (byte) 32, (byte) 32, (byte) 1}, new byte[]{(byte) 0, (byte) 3, (byte) 5, (byte) 5, (byte) 20, (byte) 0, (byte) 0, (byte) 2}};
    private static final byte[][] impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = new byte[][]{new byte[]{(byte) 0, (byte) 98, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 98, (byte) 1, (byte) 1, (byte) 0, (byte) 48, (byte) 0, (byte) 4}, new byte[]{(byte) 0, (byte) 98, (byte) 84, (byte) 84, (byte) 19, (byte) 48, (byte) 0, (byte) 3}, new byte[]{(byte) 48, (byte) 66, (byte) 84, (byte) 84, (byte) 3, (byte) 48, (byte) 48, (byte) 3}, new byte[]{(byte) 48, (byte) 66, (byte) 4, (byte) 4, (byte) 19, (byte) 48, (byte) 48, (byte) 4}};
    private static final byte[][] impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS = new byte[][]{new byte[]{(byte) 0, (byte) 99, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 99, (byte) 0, (byte) 1, (byte) 18, (byte) 48, (byte) 0, (byte) 4}, new byte[]{(byte) 32, (byte) 99, (byte) 32, (byte) 1, (byte) 2, (byte) 48, (byte) 32, (byte) 3}, new byte[]{(byte) 0, (byte) 99, (byte) 85, (byte) 86, (byte) 20, (byte) 48, (byte) 0, (byte) 3}, new byte[]{(byte) 48, (byte) 67, (byte) 85, (byte) 86, (byte) 4, (byte) 48, (byte) 48, (byte) 3}, new byte[]{(byte) 48, (byte) 67, (byte) 5, (byte) 86, (byte) 20, (byte) 48, (byte) 48, (byte) 4}, new byte[]{(byte) 48, (byte) 67, (byte) 85, (byte) 6, (byte) 20, (byte) 48, (byte) 48, (byte) 4}};
    private static final byte[][] impTabL_INVERSE_NUMBERS_AS_L = new byte[][]{new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 20, (byte) 20, (byte) 0, (byte) 1}, new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 21, (byte) 21, (byte) 0, (byte) 2}, new byte[]{(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 20, (byte) 20, (byte) 0, (byte) 2}, new byte[]{(byte) 32, (byte) 1, (byte) 32, (byte) 32, (byte) 4, (byte) 4, (byte) 32, (byte) 1}, new byte[]{(byte) 32, (byte) 1, (byte) 32, (byte) 32, (byte) 5, (byte) 5, (byte) 32, (byte) 1}};
    private static final byte[][] impTabL_NUMBERS_SPECIAL = new byte[][]{new byte[]{(byte) 0, (byte) 2, (byte) 17, (byte) 17, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 66, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 2, (byte) 4, (byte) 4, (byte) 19, (byte) 19, (byte) 0, (byte) 1}, new byte[]{(byte) 0, (byte) 34, (byte) 52, (byte) 52, (byte) 3, (byte) 3, (byte) 0, (byte) 0}, new byte[]{(byte) 0, (byte) 2, (byte) 4, (byte) 4, (byte) 19, (byte) 19, (byte) 0, (byte) 2}};
    private static final short[][] impTabProps = new short[][]{new short[]{(short) 1, (short) 2, (short) 4, (short) 5, (short) 7, (short) 15, (short) 17, (short) 7, (short) 9, (short) 7, (short) 0, (short) 7, (short) 3, (short) 18, (short) 21, (short) 4}, new short[]{(short) 1, (short) 34, (short) 36, (short) 37, (short) 39, (short) 47, (short) 49, (short) 39, (short) 41, (short) 39, (short) 1, (short) 1, (short) 35, (short) 50, (short) 53, (short) 0}, new short[]{(short) 33, (short) 2, (short) 36, (short) 37, (short) 39, (short) 47, (short) 49, (short) 39, (short) 41, (short) 39, (short) 2, (short) 2, (short) 35, (short) 50, (short) 53, (short) 1}, new short[]{(short) 33, (short) 34, (short) 38, (short) 38, (short) 40, (short) 48, (short) 49, (short) 40, (short) 40, (short) 40, (short) 3, (short) 3, (short) 3, (short) 50, (short) 53, (short) 1}, new short[]{(short) 33, (short) 34, (short) 4, (short) 37, (short) 39, (short) 47, (short) 49, (short) 74, (short) 11, (short) 74, (short) 4, (short) 4, (short) 35, (short) 18, (short) 21, (short) 2}, new short[]{(short) 33, (short) 34, (short) 36, (short) 5, (short) 39, (short) 47, (short) 49, (short) 39, (short) 41, (short) 76, (short) 5, (short) 5, (short) 35, (short) 50, (short) 53, (short) 3}, new short[]{(short) 33, (short) 34, (short) 6, (short) 6, (short) 40, (short) 48, (short) 49, (short) 40, (short) 40, (short) 77, (short) 6, (short) 6, (short) 35, (short) 18, (short) 21, (short) 3}, new short[]{(short) 33, (short) 34, (short) 36, (short) 37, (short) 7, (short) 47, (short) 49, (short) 7, (short) 78, (short) 7, (short) 7, (short) 7, (short) 35, (short) 50, (short) 53, (short) 4}, new short[]{(short) 33, (short) 34, (short) 38, (short) 38, (short) 8, (short) 48, (short) 49, (short) 8, (short) 8, (short) 8, (short) 8, (short) 8, (short) 35, (short) 50, (short) 53, (short) 4}, new short[]{(short) 33, (short) 34, (short) 4, (short) 37, (short) 7, (short) 47, (short) 49, (short) 7, (short) 9, (short) 7, (short) 9, (short) 9, (short) 35, (short) 18, (short) 21, (short) 4}, new short[]{(short) 97, (short) 98, (short) 4, (short) 101, (short) 135, (short) 111, (short) 113, (short) 135, (short) 142, (short) 135, (short) 10, (short) 135, (short) 99, (short) 18, (short) 21, (short) 2}, new short[]{(short) 33, (short) 34, (short) 4, (short) 37, (short) 39, (short) 47, (short) 49, (short) 39, (short) 11, (short) 39, (short) 11, (short) 11, (short) 35, (short) 18, (short) 21, (short) 2}, new short[]{(short) 97, (short) 98, (short) 100, (short) 5, (short) 135, (short) 111, (short) 113, (short) 135, (short) 142, (short) 135, (short) 12, (short) 135, (short) 99, (short) 114, (short) 117, (short) 3}, new short[]{(short) 97, (short) 98, (short) 6, (short) 6, (short) 136, (short) 112, (short) 113, (short) 136, (short) 136, (short) 136, (short) 13, (short) 136, (short) 99, (short) 18, (short) 21, (short) 3}, new short[]{(short) 33, (short) 34, (short) 132, (short) 37, (short) 7, (short) 47, (short) 49, (short) 7, (short) 14, (short) 7, (short) 14, (short) 14, (short) 35, (short) 146, (short) 149, (short) 4}, new short[]{(short) 33, (short) 34, (short) 36, (short) 37, (short) 39, (short) 15, (short) 49, (short) 39, (short) 41, (short) 39, (short) 15, (short) 39, (short) 35, (short) 50, (short) 53, (short) 5}, new short[]{(short) 33, (short) 34, (short) 38, (short) 38, (short) 40, (short) 16, (short) 49, (short) 40, (short) 40, (short) 40, (short) 16, (short) 40, (short) 35, (short) 50, (short) 53, (short) 5}, new short[]{(short) 33, (short) 34, (short) 36, (short) 37, (short) 39, (short) 47, (short) 17, (short) 39, (short) 41, (short) 39, (short) 17, (short) 39, (short) 35, (short) 50, (short) 53, (short) 6}, new short[]{(short) 33, (short) 34, (short) 18, (short) 37, (short) 39, (short) 47, (short) 49, (short) 83, (short) 20, (short) 83, (short) 18, (short) 18, (short) 35, (short) 18, (short) 21, (short) 0}, new short[]{(short) 97, (short) 98, (short) 18, (short) 101, (short) 135, (short) 111, (short) 113, (short) 135, (short) 142, (short) 135, (short) 19, (short) 135, (short) 99, (short) 18, (short) 21, (short) 0}, new short[]{(short) 33, (short) 34, (short) 18, (short) 37, (short) 39, (short) 47, (short) 49, (short) 39, (short) 20, (short) 39, (short) 20, (short) 20, (short) 35, (short) 18, (short) 21, (short) 0}, new short[]{(short) 33, (short) 34, (short) 21, (short) 37, (short) 39, (short) 47, (short) 49, (short) 86, (short) 23, (short) 86, (short) 21, (short) 21, (short) 35, (short) 18, (short) 21, (short) 3}, new short[]{(short) 97, (short) 98, (short) 21, (short) 101, (short) 135, (short) 111, (short) 113, (short) 135, (short) 142, (short) 135, (short) 22, (short) 135, (short) 99, (short) 18, (short) 21, (short) 3}, new short[]{(short) 33, (short) 34, (short) 21, (short) 37, (short) 39, (short) 47, (short) 49, (short) 39, (short) 23, (short) 39, (short) 23, (short) 23, (short) 35, (short) 18, (short) 21, (short) 3}};
    private static final byte[][] impTabR_DEFAULT = new byte[][]{new byte[]{(byte) 1, (byte) 0, (byte) 2, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 3, (byte) 20, (byte) 20, (byte) 0, (byte) 1}, new byte[]{(byte) 1, (byte) 0, (byte) 2, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 1}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 3, (byte) 5, (byte) 5, (byte) 0, (byte) 1}, new byte[]{(byte) 33, (byte) 0, (byte) 33, (byte) 3, (byte) 4, (byte) 4, (byte) 0, (byte) 0}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 3, (byte) 5, (byte) 5, (byte) 0, (byte) 0}};
    private static final byte[][] impTabR_GROUP_NUMBERS_WITH_R = new byte[][]{new byte[]{(byte) 2, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 2, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1}, new byte[]{(byte) 2, (byte) 0, (byte) 20, (byte) 20, (byte) 19, (byte) 0, (byte) 0, (byte) 1}, new byte[]{(byte) 34, (byte) 0, (byte) 4, (byte) 4, (byte) 3, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 34, (byte) 0, (byte) 4, (byte) 4, (byte) 3, (byte) 0, (byte) 0, (byte) 1}};
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT = new byte[][]{new byte[]{(byte) 1, (byte) 0, (byte) 2, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 2, (byte) 19, (byte) 19, (byte) 0, (byte) 1}, new byte[]{(byte) 1, (byte) 0, (byte) 2, (byte) 2, (byte) 0, (byte) 0, (byte) 0, (byte) 1}, new byte[]{(byte) 33, (byte) 48, (byte) 6, (byte) 4, (byte) 3, (byte) 3, (byte) 48, (byte) 0}, new byte[]{(byte) 33, (byte) 48, (byte) 6, (byte) 4, (byte) 5, (byte) 5, (byte) 48, (byte) 3}, new byte[]{(byte) 33, (byte) 48, (byte) 6, (byte) 4, (byte) 5, (byte) 5, (byte) 48, (byte) 2}, new byte[]{(byte) 33, (byte) 48, (byte) 6, (byte) 4, (byte) 3, (byte) 3, (byte) 48, (byte) 1}};
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS = new byte[][]{new byte[]{(byte) 19, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 35, (byte) 0, (byte) 1, (byte) 1, (byte) 2, (byte) 64, (byte) 0, (byte) 1}, new byte[]{(byte) 35, (byte) 0, (byte) 1, (byte) 1, (byte) 2, (byte) 64, (byte) 0, (byte) 0}, new byte[]{(byte) 3, (byte) 0, (byte) 3, (byte) 54, (byte) 20, (byte) 64, (byte) 0, (byte) 1}, new byte[]{(byte) 83, (byte) 64, (byte) 5, (byte) 54, (byte) 4, (byte) 64, (byte) 64, (byte) 0}, new byte[]{(byte) 83, (byte) 64, (byte) 5, (byte) 54, (byte) 4, (byte) 64, (byte) 64, (byte) 1}, new byte[]{(byte) 83, (byte) 64, (byte) 6, (byte) 6, (byte) 4, (byte) 64, (byte) 64, (byte) 3}};
    private static final byte[][] impTabR_INVERSE_NUMBERS_AS_L = new byte[][]{new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 20, (byte) 20, (byte) 0, (byte) 1}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 5, (byte) 5, (byte) 0, (byte) 1}, new byte[]{(byte) 33, (byte) 0, (byte) 33, (byte) 33, (byte) 4, (byte) 4, (byte) 0, (byte) 0}, new byte[]{(byte) 1, (byte) 0, (byte) 1, (byte) 1, (byte) 5, (byte) 5, (byte) 0, (byte) 0}};
    private static final ImpTabPair impTab_DEFAULT = new ImpTabPair(impTabL_DEFAULT, impTabR_DEFAULT, impAct0, impAct0);
    private static final ImpTabPair impTab_GROUP_NUMBERS_WITH_R = new ImpTabPair(impTabL_GROUP_NUMBERS_WITH_R, impTabR_GROUP_NUMBERS_WITH_R, impAct0, impAct0);
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL = new ImpTabPair(impTabL_NUMBERS_SPECIAL, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = new ImpTabPair(impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS, impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct2, impAct3);
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT = new ImpTabPair(impTabL_DEFAULT, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT_WITH_MARKS = new ImpTabPair(impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS, impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct2, impAct3);
    private static final ImpTabPair impTab_INVERSE_NUMBERS_AS_L = new ImpTabPair(impTabL_INVERSE_NUMBERS_AS_L, impTabR_INVERSE_NUMBERS_AS_L, impAct0, impAct0);
    private static final ImpTabPair impTab_NUMBERS_SPECIAL = new ImpTabPair(impTabL_NUMBERS_SPECIAL, impTabR_DEFAULT, impAct0, impAct0);
    final UBiDiProps bdp;
    int controlCount;
    BidiClassifier customClassifier;
    byte defaultParaLevel;
    byte[] dirProps;
    byte[] dirPropsMemory;
    byte direction;
    String epilogue;
    int flags;
    ImpTabPair impTabPair;
    InsertPoints insertPoints;
    boolean isGoodLogicalToVisualRunsMap;
    boolean isInverse;
    int isolateCount;
    Isolate[] isolates;
    int lastArabicPos;
    int length;
    byte[] levels;
    byte[] levelsMemory;
    int[] logicalToVisualRunsMap;
    boolean mayAllocateRuns;
    boolean mayAllocateText;
    boolean orderParagraphsLTR;
    int originalLength;
    Bidi paraBidi;
    int paraCount;
    byte paraLevel;
    byte[] paras_level;
    int[] paras_limit;
    String prologue;
    int reorderingMode;
    int reorderingOptions;
    int resultLength;
    int runCount;
    BidiRun[] runs;
    BidiRun[] runsMemory;
    BidiRun[] simpleRuns;
    char[] text;
    int trailingWSStart;

    static class BracketData {
        boolean isNumbersSpecial;
        int isoRunLast;
        IsoRun[] isoRuns = new IsoRun[127];
        Opening[] openings = new Opening[20];

        BracketData() {
        }
    }

    private static class ImpTabPair {
        short[][] impact;
        byte[][][] imptab;

        ImpTabPair(byte[][] table1, byte[][] table2, short[] act1, short[] act2) {
            this.imptab = new byte[][][]{table1, table2};
            this.impact = new short[][]{act1, act2};
        }
    }

    static class InsertPoints {
        int confirmed;
        Point[] points = new Point[0];
        int size;

        InsertPoints() {
        }
    }

    static class IsoRun {
        byte contextDir;
        int contextPos;
        byte lastBase;
        byte lastStrong;
        byte level;
        short limit;
        short start;

        IsoRun() {
        }
    }

    static class Isolate {
        int start1;
        int startON;
        short state;
        short stateImp;

        Isolate() {
        }
    }

    private static class LevState {
        short[] impAct;
        byte[][] impTab;
        int lastStrongRTL;
        byte runLevel;
        int runStart;
        int startL2EN;
        int startON;
        short state;

        /* synthetic */ LevState(LevState -this0) {
            this();
        }

        private LevState() {
        }
    }

    static class Opening {
        byte contextDir;
        int contextPos;
        short flags;
        int match;
        int position;

        Opening() {
        }
    }

    static class Point {
        int flag;
        int pos;

        Point() {
        }
    }

    static int DirPropFlag(byte dir) {
        return 1 << dir;
    }

    boolean testDirPropFlagAt(int flag, int index) {
        return (DirPropFlag(this.dirProps[index]) & flag) != 0;
    }

    static final int DirPropFlagLR(byte level) {
        return DirPropFlagLR[level & 1];
    }

    static final int DirPropFlagE(byte level) {
        return DirPropFlagE[level & 1];
    }

    static final int DirPropFlagO(byte level) {
        return DirPropFlagO[level & 1];
    }

    static final byte DirFromStrong(byte strong) {
        return strong == (byte) 0 ? (byte) 0 : (byte) 1;
    }

    static final byte NoOverride(byte level) {
        return (byte) (level & 127);
    }

    static byte GetLRFromLevel(byte level) {
        return (byte) (level & 1);
    }

    static boolean IsDefaultLevel(byte level) {
        return (level & 126) == 126;
    }

    static boolean IsBidiControlChar(int c) {
        if ((c & -4) == 8204) {
            return true;
        }
        if (c >= 8234 && c <= 8238) {
            return true;
        }
        if (c < 8294 || c > 8297) {
            return false;
        }
        return true;
    }

    void verifyValidPara() {
        if (this != this.paraBidi) {
            throw new IllegalStateException();
        }
    }

    void verifyValidParaOrLine() {
        Bidi para = this.paraBidi;
        if (this != para) {
            if (para == null || para != para.paraBidi) {
                throw new IllegalStateException();
            }
        }
    }

    void verifyRange(int index, int start, int limit) {
        if (index < start || index >= limit) {
            throw new IllegalArgumentException("Value " + index + " is out of range " + start + " to " + limit);
        }
    }

    public Bidi() {
        this(0, 0);
    }

    public Bidi(int maxLength, int maxRunCount) {
        this.dirPropsMemory = new byte[1];
        this.levelsMemory = new byte[1];
        this.paras_limit = new int[10];
        this.paras_level = new byte[10];
        this.runsMemory = new BidiRun[0];
        this.simpleRuns = new BidiRun[]{new BidiRun()};
        this.customClassifier = null;
        this.insertPoints = new InsertPoints();
        if (maxLength < 0 || maxRunCount < 0) {
            throw new IllegalArgumentException();
        }
        this.bdp = UBiDiProps.INSTANCE;
        if (maxLength > 0) {
            getInitialDirPropsMemory(maxLength);
            getInitialLevelsMemory(maxLength);
        } else {
            this.mayAllocateText = true;
        }
        if (maxRunCount <= 0) {
            this.mayAllocateRuns = true;
        } else if (maxRunCount > 1) {
            getInitialRunsMemory(maxRunCount);
        }
    }

    private Object getMemory(String label, Object array, Class<?> arrayClass, boolean mayAllocate, int sizeNeeded) {
        int len = Array.getLength(array);
        if (sizeNeeded == len) {
            return array;
        }
        if (mayAllocate) {
            try {
                return Array.newInstance(arrayClass, sizeNeeded);
            } catch (Exception e) {
                throw new OutOfMemoryError("Failed to allocate memory for " + label);
            }
        } else if (sizeNeeded <= len) {
            return array;
        } else {
            throw new OutOfMemoryError("Failed to allocate memory for " + label);
        }
    }

    private void getDirPropsMemory(boolean mayAllocate, int len) {
        this.dirPropsMemory = (byte[]) getMemory("DirProps", this.dirPropsMemory, Byte.TYPE, mayAllocate, len);
    }

    void getDirPropsMemory(int len) {
        getDirPropsMemory(this.mayAllocateText, len);
    }

    private void getLevelsMemory(boolean mayAllocate, int len) {
        this.levelsMemory = (byte[]) getMemory("Levels", this.levelsMemory, Byte.TYPE, mayAllocate, len);
    }

    void getLevelsMemory(int len) {
        getLevelsMemory(this.mayAllocateText, len);
    }

    private void getRunsMemory(boolean mayAllocate, int len) {
        this.runsMemory = (BidiRun[]) getMemory("Runs", this.runsMemory, BidiRun.class, mayAllocate, len);
    }

    void getRunsMemory(int len) {
        getRunsMemory(this.mayAllocateRuns, len);
    }

    private void getInitialDirPropsMemory(int len) {
        getDirPropsMemory(true, len);
    }

    private void getInitialLevelsMemory(int len) {
        getLevelsMemory(true, len);
    }

    private void getInitialRunsMemory(int len) {
        getRunsMemory(true, len);
    }

    public void setInverse(boolean isInverse) {
        int i;
        this.isInverse = isInverse;
        if (isInverse) {
            i = 4;
        } else {
            i = 0;
        }
        this.reorderingMode = i;
    }

    public boolean isInverse() {
        return this.isInverse;
    }

    public void setReorderingMode(int reorderingMode) {
        boolean z = false;
        if (reorderingMode >= 0 && reorderingMode < 7) {
            this.reorderingMode = reorderingMode;
            if (reorderingMode == 4) {
                z = true;
            }
            this.isInverse = z;
        }
    }

    public int getReorderingMode() {
        return this.reorderingMode;
    }

    public void setReorderingOptions(int options) {
        if ((options & 2) != 0) {
            this.reorderingOptions = options & -2;
        } else {
            this.reorderingOptions = options;
        }
    }

    public int getReorderingOptions() {
        return this.reorderingOptions;
    }

    public static byte getBaseDirection(CharSequence paragraph) {
        if (paragraph == null || paragraph.length() == 0) {
            return (byte) 3;
        }
        int length = paragraph.length();
        int i = 0;
        while (i < length) {
            byte direction = UCharacter.getDirectionality(UCharacter.codePointAt(paragraph, i));
            if (direction == (byte) 0) {
                return (byte) 0;
            }
            if (direction == (byte) 1 || direction == (byte) 13) {
                return (byte) 1;
            }
            i = UCharacter.offsetByCodePoints(paragraph, i, 1);
        }
        return (byte) 3;
    }

    private byte firstL_R_AL() {
        byte result = (byte) 10;
        int i = 0;
        while (i < this.prologue.length()) {
            int uchar = this.prologue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (result == (byte) 10) {
                if (dirProp == (byte) 0 || dirProp == (byte) 1 || dirProp == (byte) 13) {
                    result = dirProp;
                }
            } else if (dirProp == (byte) 7) {
                result = (byte) 10;
            }
        }
        return result;
    }

    private void checkParaCount() {
        int count = this.paraCount;
        if (count > this.paras_level.length) {
            int oldLength = this.paras_level.length;
            int[] saveLimits = this.paras_limit;
            byte[] saveLevels = this.paras_level;
            try {
                this.paras_limit = new int[(count * 2)];
                this.paras_level = new byte[(count * 2)];
                System.arraycopy(saveLimits, 0, this.paras_limit, 0, oldLength);
                System.arraycopy(saveLevels, 0, this.paras_level, 0, oldLength);
            } catch (Exception e) {
                throw new OutOfMemoryError("Failed to allocate memory for paras");
            }
        }
    }

    private void getDirProps() {
        byte dirProp;
        byte state;
        this.flags = 0;
        boolean isDefaultLevel = IsDefaultLevel(this.paraLevel);
        boolean isDefaultLevelInverse = isDefaultLevel ? this.reorderingMode != 5 ? this.reorderingMode == 6 : true : false;
        this.lastArabicPos = -1;
        int controlCount = 0;
        boolean removeBidiControls = (this.reorderingOptions & 2) != 0;
        byte lastStrong = (byte) 10;
        int[] isolateStartStack = new int[126];
        byte[] previousStateStack = new byte[126];
        int stackLast = -1;
        if ((this.reorderingOptions & 4) != 0) {
            this.length = 0;
        }
        byte defaultParaLevel = (byte) (this.paraLevel & 1);
        if (isDefaultLevel) {
            this.paras_level[0] = defaultParaLevel;
            lastStrong = defaultParaLevel;
            if (this.prologue != null) {
                dirProp = firstL_R_AL();
                if (dirProp != (byte) 10) {
                    if (dirProp == (byte) 0) {
                        this.paras_level[0] = (byte) 0;
                    } else {
                        this.paras_level[0] = (byte) 1;
                    }
                    state = (byte) 0;
                }
            }
            state = (byte) 1;
        } else {
            this.paras_level[0] = this.paraLevel;
            state = (byte) 0;
        }
        int i = 0;
        while (i < this.originalLength) {
            int i0 = i;
            int uchar = UTF16.charAt(this.text, 0, this.originalLength, i);
            i += UTF16.getCharCount(uchar);
            int i1 = i - 1;
            dirProp = (byte) getCustomizedClass(uchar);
            this.flags |= DirPropFlag(dirProp);
            this.dirProps[i1] = dirProp;
            if (i1 > i0) {
                this.flags |= DirPropFlag((byte) 18);
                do {
                    i1--;
                    this.dirProps[i1] = (byte) 18;
                } while (i1 > i0);
            }
            if (removeBidiControls && IsBidiControlChar(uchar)) {
                controlCount++;
            }
            if (dirProp == (byte) 0) {
                if (state == (byte) 1) {
                    this.paras_level[this.paraCount - 1] = (byte) 0;
                    state = (byte) 0;
                } else if (state == (byte) 2) {
                    if (stackLast <= 125) {
                        this.flags |= DirPropFlag((byte) 20);
                    }
                    state = (byte) 3;
                }
                lastStrong = (byte) 0;
            } else if (dirProp == (byte) 1 || dirProp == (byte) 13) {
                if (state == (byte) 1) {
                    this.paras_level[this.paraCount - 1] = (byte) 1;
                    state = (byte) 0;
                } else if (state == (byte) 2) {
                    if (stackLast <= 125) {
                        this.dirProps[isolateStartStack[stackLast]] = (byte) 21;
                        this.flags |= DirPropFlag((byte) 21);
                    }
                    state = (byte) 3;
                }
                lastStrong = (byte) 1;
                if (dirProp == (byte) 13) {
                    this.lastArabicPos = i - 1;
                }
            } else if (dirProp >= (byte) 19 && dirProp <= (byte) 21) {
                stackLast++;
                if (stackLast <= 125) {
                    isolateStartStack[stackLast] = i - 1;
                    previousStateStack[stackLast] = state;
                }
                if (dirProp == (byte) 19) {
                    this.dirProps[i - 1] = (byte) 20;
                    state = (byte) 2;
                } else {
                    state = (byte) 3;
                }
            } else if (dirProp == (byte) 22) {
                if (state == (byte) 2 && stackLast <= 125) {
                    this.flags |= DirPropFlag((byte) 20);
                }
                if (stackLast >= 0) {
                    if (stackLast <= 125) {
                        state = previousStateStack[stackLast];
                    }
                    stackLast--;
                }
            } else if (dirProp == (byte) 7 && !(i < this.originalLength && uchar == 13 && this.text[i] == 10)) {
                this.paras_limit[this.paraCount - 1] = i;
                if (isDefaultLevelInverse && lastStrong == (byte) 1) {
                    this.paras_level[this.paraCount - 1] = (byte) 1;
                }
                if ((this.reorderingOptions & 4) != 0) {
                    this.length = i;
                    this.controlCount = controlCount;
                }
                if (i < this.originalLength) {
                    this.paraCount++;
                    checkParaCount();
                    if (isDefaultLevel) {
                        this.paras_level[this.paraCount - 1] = defaultParaLevel;
                        state = (byte) 1;
                        lastStrong = defaultParaLevel;
                    } else {
                        this.paras_level[this.paraCount - 1] = this.paraLevel;
                        state = (byte) 0;
                    }
                    stackLast = -1;
                }
            }
        }
        if (stackLast > 125) {
            stackLast = 125;
            state = (byte) 2;
        }
        while (stackLast >= 0) {
            if (state == (byte) 2) {
                this.flags |= DirPropFlag((byte) 20);
                break;
            } else {
                state = previousStateStack[stackLast];
                stackLast--;
            }
        }
        if ((this.reorderingOptions & 4) != 0) {
            if (this.length < this.originalLength) {
                this.paraCount--;
            }
        } else {
            this.paras_limit[this.paraCount - 1] = this.originalLength;
            this.controlCount = controlCount;
        }
        if (isDefaultLevelInverse && lastStrong == (byte) 1) {
            this.paras_level[this.paraCount - 1] = (byte) 1;
        }
        if (isDefaultLevel) {
            this.paraLevel = this.paras_level[0];
        }
        for (i = 0; i < this.paraCount; i++) {
            this.flags |= DirPropFlagLR(this.paras_level[i]);
        }
        if (this.orderParagraphsLTR && (this.flags & DirPropFlag((byte) 7)) != 0) {
            this.flags |= DirPropFlag((byte) 0);
        }
    }

    byte GetParaLevelAt(int pindex) {
        if (this.defaultParaLevel == (byte) 0 || pindex < this.paras_limit[0]) {
            return this.paraLevel;
        }
        int i = 1;
        while (i < this.paraCount && pindex >= this.paras_limit[i]) {
            i++;
        }
        if (i >= this.paraCount) {
            i = this.paraCount - 1;
        }
        return this.paras_level[i];
    }

    private void bracketInit(BracketData bd) {
        boolean z = true;
        bd.isoRunLast = 0;
        bd.isoRuns[0] = new IsoRun();
        bd.isoRuns[0].start = (short) 0;
        bd.isoRuns[0].limit = (short) 0;
        bd.isoRuns[0].level = GetParaLevelAt(0);
        IsoRun isoRun = bd.isoRuns[0];
        byte GetParaLevelAt = (byte) (GetParaLevelAt(0) & 1);
        bd.isoRuns[0].contextDir = GetParaLevelAt;
        bd.isoRuns[0].lastBase = GetParaLevelAt;
        isoRun.lastStrong = GetParaLevelAt;
        bd.isoRuns[0].contextPos = 0;
        bd.openings = new Opening[20];
        if (!(this.reorderingMode == 1 || this.reorderingMode == 6)) {
            z = false;
        }
        bd.isNumbersSpecial = z;
    }

    private void bracketProcessB(BracketData bd, byte level) {
        bd.isoRunLast = 0;
        bd.isoRuns[0].limit = (short) 0;
        bd.isoRuns[0].level = level;
        IsoRun isoRun = bd.isoRuns[0];
        byte b = (byte) (level & 1);
        bd.isoRuns[0].contextDir = b;
        bd.isoRuns[0].lastBase = b;
        isoRun.lastStrong = b;
        bd.isoRuns[0].contextPos = 0;
    }

    private void bracketProcessBoundary(BracketData bd, int lastCcPos, byte contextLevel, byte embeddingLevel) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if ((DirPropFlag(this.dirProps[lastCcPos]) & MASK_ISO) == 0) {
            if (NoOverride(embeddingLevel) > NoOverride(contextLevel)) {
                contextLevel = embeddingLevel;
            }
            pLastIsoRun.limit = pLastIsoRun.start;
            pLastIsoRun.level = embeddingLevel;
            byte b = (byte) (contextLevel & 1);
            pLastIsoRun.contextDir = b;
            pLastIsoRun.lastBase = b;
            pLastIsoRun.lastStrong = b;
            pLastIsoRun.contextPos = lastCcPos;
        }
    }

    private void bracketProcessLRI_RLI(BracketData bd, byte level) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        pLastIsoRun.lastBase = (byte) 10;
        short lastLimit = pLastIsoRun.limit;
        bd.isoRunLast++;
        pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if (pLastIsoRun == null) {
            pLastIsoRun = new IsoRun();
            bd.isoRuns[bd.isoRunLast] = pLastIsoRun;
        }
        pLastIsoRun.limit = lastLimit;
        pLastIsoRun.start = lastLimit;
        pLastIsoRun.level = level;
        byte b = (byte) (level & 1);
        pLastIsoRun.contextDir = b;
        pLastIsoRun.lastBase = b;
        pLastIsoRun.lastStrong = b;
        pLastIsoRun.contextPos = 0;
    }

    private void bracketProcessPDI(BracketData bd) {
        bd.isoRunLast--;
        bd.isoRuns[bd.isoRunLast].lastBase = (byte) 10;
    }

    private void bracketAddOpening(BracketData bd, char match, int position) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if (pLastIsoRun.limit >= bd.openings.length) {
            Opening[] saveOpenings = bd.openings;
            try {
                int count = bd.openings.length;
                bd.openings = new Opening[(count * 2)];
                System.arraycopy(saveOpenings, 0, bd.openings, 0, count);
            } catch (Exception e) {
                throw new OutOfMemoryError("Failed to allocate memory for openings");
            }
        }
        Opening pOpening = bd.openings[pLastIsoRun.limit];
        if (pOpening == null) {
            pOpening = new Opening();
            bd.openings[pLastIsoRun.limit] = pOpening;
        }
        pOpening.position = position;
        pOpening.match = match;
        pOpening.contextDir = pLastIsoRun.contextDir;
        pOpening.contextPos = pLastIsoRun.contextPos;
        pOpening.flags = (short) 0;
        pLastIsoRun.limit = (short) (pLastIsoRun.limit + 1);
    }

    private void fixN0c(BracketData bd, int openingIndex, int newPropPosition, byte newProp) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        for (short k = openingIndex + 1; k < pLastIsoRun.limit; k++) {
            Opening qOpening = bd.openings[k];
            if (qOpening.match < 0) {
                if (newPropPosition >= qOpening.contextPos) {
                    if (newPropPosition >= qOpening.position) {
                        continue;
                    } else if (newProp != qOpening.contextDir) {
                        int openingPosition = qOpening.position;
                        this.dirProps[openingPosition] = newProp;
                        int closingPosition = -qOpening.match;
                        this.dirProps[closingPosition] = newProp;
                        qOpening.match = 0;
                        fixN0c(bd, k, openingPosition, newProp);
                        fixN0c(bd, k, closingPosition, newProp);
                    } else {
                        return;
                    }
                }
                return;
            }
        }
    }

    private byte bracketProcessClosing(BracketData bd, int openIdx, int position) {
        byte newProp;
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        Opening pOpening = bd.openings[openIdx];
        byte direction = (byte) (pLastIsoRun.level & 1);
        boolean stable = true;
        if ((direction == (byte) 0 && (pOpening.flags & FOUND_L) > 0) || (direction == (byte) 1 && (pOpening.flags & FOUND_R) > 0)) {
            newProp = direction;
        } else if ((pOpening.flags & (FOUND_L | FOUND_R)) != 0) {
            stable = openIdx == pLastIsoRun.start;
            if (direction != pOpening.contextDir) {
                newProp = pOpening.contextDir;
            } else {
                newProp = direction;
            }
        } else {
            pLastIsoRun.limit = (short) openIdx;
            return (byte) 10;
        }
        this.dirProps[pOpening.position] = newProp;
        this.dirProps[position] = newProp;
        fixN0c(bd, openIdx, pOpening.position, newProp);
        if (stable) {
            pLastIsoRun.limit = (short) openIdx;
            while (pLastIsoRun.limit > pLastIsoRun.start && bd.openings[pLastIsoRun.limit - 1].position == pOpening.position) {
                pLastIsoRun.limit = (short) (pLastIsoRun.limit - 1);
            }
        } else {
            pOpening.match = -position;
            short k = openIdx - 1;
            while (k >= pLastIsoRun.start && bd.openings[k].position == pOpening.position) {
                short k2 = k - 1;
                bd.openings[k].match = 0;
                k = k2;
            }
            for (k = openIdx + 1; k < pLastIsoRun.limit; k++) {
                Opening qOpening = bd.openings[k];
                if (qOpening.position >= position) {
                    break;
                }
                if (qOpening.match > 0) {
                    qOpening.match = 0;
                }
            }
        }
        return newProp;
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00de  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0044  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void bracketProcessChar(BracketData bd, int position) {
        byte newProp;
        byte level;
        short flag;
        short i;
        Opening opening;
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        byte dirProp = this.dirProps[position];
        if (dirProp == (byte) 10) {
            char match;
            char c = this.text[position];
            short idx = pLastIsoRun.limit - 1;
            while (idx >= pLastIsoRun.start) {
                if (bd.openings[idx].match != c) {
                    idx--;
                } else {
                    newProp = bracketProcessClosing(bd, idx, position);
                    if (newProp == (byte) 10) {
                        c = 0;
                        if (c == 0) {
                            match = (char) UCharacter.getBidiPairedBracket(c);
                        } else {
                            match = 0;
                        }
                        if (match != c && UCharacter.getIntPropertyValue(c, UProperty.BIDI_PAIRED_BRACKET_TYPE) == 1) {
                            if (match != 9002) {
                                bracketAddOpening(bd, 12297, position);
                            } else if (match == 12297) {
                                bracketAddOpening(bd, 9002, position);
                            }
                            bracketAddOpening(bd, match, position);
                        }
                    } else {
                        byte[] bArr;
                        pLastIsoRun.lastBase = (byte) 10;
                        pLastIsoRun.contextDir = newProp;
                        pLastIsoRun.contextPos = position;
                        level = this.levels[position];
                        if ((level & -128) != 0) {
                            newProp = (byte) (level & 1);
                            pLastIsoRun.lastStrong = newProp;
                            flag = (short) DirPropFlag(newProp);
                            for (i = pLastIsoRun.start; i < idx; i++) {
                                opening = bd.openings[i];
                                opening.flags = (short) (opening.flags | flag);
                            }
                            bArr = this.levels;
                            bArr[position] = (byte) (bArr[position] & 127);
                        }
                        bArr = this.levels;
                        int i2 = bd.openings[idx].position;
                        bArr[i2] = (byte) (bArr[i2] & 127);
                        return;
                    }
                }
            }
            if (c == 0) {
            }
            if (match != 9002) {
            }
            bracketAddOpening(bd, match, position);
        }
        level = this.levels[position];
        if ((level & -128) != 0) {
            newProp = (byte) (level & 1);
            if (!(dirProp == (byte) 8 || dirProp == (byte) 9 || dirProp == (byte) 10)) {
                this.dirProps[position] = newProp;
            }
            pLastIsoRun.lastBase = newProp;
            pLastIsoRun.lastStrong = newProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        } else if (dirProp <= (byte) 1 || dirProp == (byte) 13) {
            newProp = DirFromStrong(dirProp);
            pLastIsoRun.lastBase = dirProp;
            pLastIsoRun.lastStrong = dirProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        } else if (dirProp == (byte) 2) {
            pLastIsoRun.lastBase = (byte) 2;
            if (pLastIsoRun.lastStrong == (byte) 0) {
                newProp = (byte) 0;
                if (!bd.isNumbersSpecial) {
                    this.dirProps[position] = (byte) 23;
                }
                pLastIsoRun.contextDir = (byte) 0;
                pLastIsoRun.contextPos = position;
            } else {
                newProp = (byte) 1;
                if (pLastIsoRun.lastStrong == (byte) 13) {
                    this.dirProps[position] = (byte) 5;
                } else {
                    this.dirProps[position] = (byte) 24;
                }
                pLastIsoRun.contextDir = (byte) 1;
                pLastIsoRun.contextPos = position;
            }
        } else if (dirProp == (byte) 5) {
            newProp = (byte) 1;
            pLastIsoRun.lastBase = (byte) 5;
            pLastIsoRun.contextDir = (byte) 1;
            pLastIsoRun.contextPos = position;
        } else if (dirProp == (byte) 17) {
            newProp = pLastIsoRun.lastBase;
            if (newProp == (byte) 10) {
                this.dirProps[position] = newProp;
            }
        } else {
            newProp = dirProp;
            pLastIsoRun.lastBase = dirProp;
        }
        if (newProp <= (byte) 1 || newProp == (byte) 13) {
            flag = (short) DirPropFlag(DirFromStrong(newProp));
            for (i = pLastIsoRun.start; i < pLastIsoRun.limit; i++) {
                if (position > bd.openings[i].position) {
                    opening = bd.openings[i];
                    opening.flags = (short) (opening.flags | flag);
                }
            }
        }
    }

    private byte directionFromFlags() {
        if ((this.flags & MASK_RTL) == 0 && ((this.flags & DirPropFlag((byte) 5)) == 0 || (this.flags & MASK_POSSIBLE_N) == 0)) {
            return (byte) 0;
        }
        if ((this.flags & MASK_LTR) == 0) {
            return (byte) 1;
        }
        return (byte) 2;
    }

    private byte resolveExplicitLevels() {
        byte level = GetParaLevelAt(0);
        this.isolateCount = 0;
        byte dirct = directionFromFlags();
        if (dirct != (byte) 2) {
            return dirct;
        }
        int paraIndex;
        int start;
        int limit;
        int i;
        BracketData bracketData;
        byte dirProp;
        if (this.reorderingMode > 1) {
            for (paraIndex = 0; paraIndex < this.paraCount; paraIndex++) {
                if (paraIndex == 0) {
                    start = 0;
                } else {
                    start = this.paras_limit[paraIndex - 1];
                }
                limit = this.paras_limit[paraIndex];
                level = this.paras_level[paraIndex];
                for (i = start; i < limit; i++) {
                    this.levels[i] = level;
                }
            }
            return dirct;
        } else if ((this.flags & (MASK_EXPLICIT | MASK_ISO)) == 0) {
            bracketData = new BracketData();
            bracketInit(bracketData);
            for (paraIndex = 0; paraIndex < this.paraCount; paraIndex++) {
                if (paraIndex == 0) {
                    start = 0;
                } else {
                    start = this.paras_limit[paraIndex - 1];
                }
                limit = this.paras_limit[paraIndex];
                level = this.paras_level[paraIndex];
                i = start;
                while (i < limit) {
                    this.levels[i] = level;
                    dirProp = this.dirProps[i];
                    if (dirProp != (byte) 18) {
                        if (dirProp == (byte) 7) {
                            if (i + 1 < this.length && !(this.text[i] == 13 && this.text[i + 1] == 10)) {
                                bracketProcessB(bracketData, level);
                            }
                        } else {
                            bracketProcessChar(bracketData, i);
                        }
                    }
                    i++;
                }
            }
            return dirct;
        } else {
            byte embeddingLevel = level;
            byte previousLevel = level;
            int lastCcPos = 0;
            short[] stack = new short[127];
            int stackLast = 0;
            int overflowIsolateCount = 0;
            int overflowEmbeddingCount = 0;
            int validIsolateCount = 0;
            bracketData = new BracketData();
            bracketInit(bracketData);
            stack[0] = (short) level;
            this.flags = 0;
            i = 0;
            while (i < this.length) {
                dirProp = this.dirProps[i];
                byte newLevel;
                switch (dirProp) {
                    case (byte) 7:
                        this.flags |= DirPropFlag((byte) 7);
                        this.levels[i] = GetParaLevelAt(i);
                        if (i + 1 < this.length && !(this.text[i] == 13 && this.text[i + 1] == 10)) {
                            overflowIsolateCount = 0;
                            overflowEmbeddingCount = 0;
                            validIsolateCount = 0;
                            stackLast = 0;
                            embeddingLevel = GetParaLevelAt(i + 1);
                            previousLevel = embeddingLevel;
                            stack[0] = (short) embeddingLevel;
                            bracketProcessB(bracketData, embeddingLevel);
                            break;
                        }
                    case (byte) 11:
                    case (byte) 12:
                    case (byte) 14:
                    case (byte) 15:
                        this.flags |= DirPropFlag((byte) 18);
                        this.levels[i] = previousLevel;
                        if (dirProp == (byte) 11 || dirProp == (byte) 12) {
                            newLevel = (byte) ((embeddingLevel + 2) & 126);
                        } else {
                            newLevel = (byte) ((NoOverride(embeddingLevel) + 1) | 1);
                        }
                        if (newLevel > (byte) 125 || overflowIsolateCount != 0 || overflowEmbeddingCount != 0) {
                            if (overflowIsolateCount != 0) {
                                break;
                            }
                            overflowEmbeddingCount++;
                            break;
                        }
                        lastCcPos = i;
                        embeddingLevel = newLevel;
                        if (dirProp == (byte) 12 || dirProp == (byte) 15) {
                            embeddingLevel = (byte) (embeddingLevel | -128);
                        }
                        stackLast++;
                        stack[stackLast] = (short) embeddingLevel;
                        break;
                        break;
                    case (byte) 16:
                        this.flags |= DirPropFlag((byte) 18);
                        this.levels[i] = previousLevel;
                        if (overflowIsolateCount <= 0) {
                            if (overflowEmbeddingCount <= 0) {
                                if (stackLast > 0 && stack[stackLast] < (short) 256) {
                                    lastCcPos = i;
                                    stackLast--;
                                    embeddingLevel = (byte) stack[stackLast];
                                    break;
                                }
                            }
                            overflowEmbeddingCount--;
                            break;
                        }
                        break;
                    case (byte) 18:
                        this.levels[i] = previousLevel;
                        this.flags |= DirPropFlag((byte) 18);
                        break;
                    case (byte) 20:
                    case (byte) 21:
                        this.flags |= DirPropFlag((byte) 10) | DirPropFlagLR(embeddingLevel);
                        this.levels[i] = NoOverride(embeddingLevel);
                        if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                            bracketProcessBoundary(bracketData, lastCcPos, previousLevel, embeddingLevel);
                            this.flags |= DirPropFlagMultiRuns;
                        }
                        previousLevel = embeddingLevel;
                        if (dirProp == (byte) 20) {
                            newLevel = (byte) ((embeddingLevel + 2) & 126);
                        } else {
                            newLevel = (byte) ((NoOverride(embeddingLevel) + 1) | 1);
                        }
                        if (newLevel > (byte) 125 || overflowIsolateCount != 0 || overflowEmbeddingCount != 0) {
                            this.dirProps[i] = (byte) 9;
                            overflowIsolateCount++;
                            break;
                        }
                        this.flags |= DirPropFlag(dirProp);
                        lastCcPos = i;
                        validIsolateCount++;
                        if (validIsolateCount > this.isolateCount) {
                            this.isolateCount = validIsolateCount;
                        }
                        embeddingLevel = newLevel;
                        stackLast++;
                        stack[stackLast] = (short) (newLevel + 256);
                        bracketProcessLRI_RLI(bracketData, embeddingLevel);
                        break;
                        break;
                    case (byte) 22:
                        if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                            bracketProcessBoundary(bracketData, lastCcPos, previousLevel, embeddingLevel);
                            this.flags |= DirPropFlagMultiRuns;
                        }
                        if (overflowIsolateCount > 0) {
                            overflowIsolateCount--;
                            this.dirProps[i] = (byte) 9;
                        } else if (validIsolateCount > 0) {
                            this.flags |= DirPropFlag((byte) 22);
                            lastCcPos = i;
                            overflowEmbeddingCount = 0;
                            while (stack[stackLast] < (short) 256) {
                                stackLast--;
                            }
                            stackLast--;
                            validIsolateCount--;
                            bracketProcessPDI(bracketData);
                        } else {
                            this.dirProps[i] = (byte) 9;
                        }
                        embeddingLevel = (byte) (stack[stackLast] & -257);
                        this.flags |= DirPropFlag((byte) 10) | DirPropFlagLR(embeddingLevel);
                        previousLevel = embeddingLevel;
                        this.levels[i] = NoOverride(embeddingLevel);
                        break;
                    default:
                        if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                            bracketProcessBoundary(bracketData, lastCcPos, previousLevel, embeddingLevel);
                            this.flags |= DirPropFlagMultiRuns;
                            if ((embeddingLevel & -128) != 0) {
                                this.flags |= DirPropFlagO(embeddingLevel);
                            } else {
                                this.flags |= DirPropFlagE(embeddingLevel);
                            }
                        }
                        previousLevel = embeddingLevel;
                        this.levels[i] = embeddingLevel;
                        bracketProcessChar(bracketData, i);
                        this.flags |= DirPropFlag(this.dirProps[i]);
                        break;
                }
                i++;
            }
            if ((this.flags & MASK_EMBEDDING) != 0) {
                this.flags |= DirPropFlagLR(this.paraLevel);
            }
            if (this.orderParagraphsLTR && (this.flags & DirPropFlag((byte) 7)) != 0) {
                this.flags |= DirPropFlag((byte) 0);
            }
            return directionFromFlags();
        }
    }

    private byte checkExplicitLevels() {
        int isolateCount = 0;
        this.flags = 0;
        this.isolateCount = 0;
        int i = 0;
        while (i < this.length) {
            byte level = this.levels[i];
            byte dirProp = this.dirProps[i];
            if (dirProp == (byte) 20 || dirProp == (byte) 21) {
                isolateCount++;
                if (isolateCount > this.isolateCount) {
                    this.isolateCount = isolateCount;
                }
            } else if (dirProp == (byte) 22) {
                isolateCount--;
            } else if (dirProp == (byte) 7) {
                isolateCount = 0;
            }
            if ((level & -128) != 0) {
                level = (byte) (level & 127);
                this.flags |= DirPropFlagO(level);
            } else {
                this.flags |= DirPropFlagE(level) | DirPropFlag(dirProp);
            }
            if ((level >= GetParaLevelAt(i) || (level == (byte) 0 && dirProp == (byte) 7)) && MAX_EXPLICIT_LEVEL >= level) {
                i++;
            } else {
                throw new IllegalArgumentException("level " + level + " out of bounds at " + i);
            }
        }
        if ((this.flags & MASK_EMBEDDING) != 0) {
            this.flags |= DirPropFlagLR(this.paraLevel);
        }
        return directionFromFlags();
    }

    private static short GetStateProps(short cell) {
        return (short) (cell & 31);
    }

    private static short GetActionProps(short cell) {
        return (short) (cell >> 5);
    }

    private static short GetState(byte cell) {
        return (short) (cell & 15);
    }

    private static short GetAction(byte cell) {
        return (short) (cell >> 4);
    }

    private void addPoint(int pos, int flag) {
        Point point = new Point();
        int len = this.insertPoints.points.length;
        if (len == 0) {
            this.insertPoints.points = new Point[10];
            len = 10;
        }
        if (this.insertPoints.size >= len) {
            Point[] savePoints = this.insertPoints.points;
            this.insertPoints.points = new Point[(len * 2)];
            System.arraycopy(savePoints, 0, this.insertPoints.points, 0, len);
        }
        point.pos = pos;
        point.flag = flag;
        this.insertPoints.points[this.insertPoints.size] = point;
        InsertPoints insertPoints = this.insertPoints;
        insertPoints.size++;
    }

    private void setLevelsOutsideIsolates(int start, int limit, byte level) {
        int isolateCount = 0;
        for (int k = start; k < limit; k++) {
            byte dirProp = this.dirProps[k];
            if (dirProp == (byte) 22) {
                isolateCount--;
            }
            if (isolateCount == 0) {
                this.levels[k] = level;
            }
            if (dirProp == (byte) 20 || dirProp == (byte) 21) {
                isolateCount++;
            }
        }
    }

    private void processPropertySeq(LevState levState, short _prop, int start, int limit) {
        int k;
        byte level;
        byte[][] impTab = levState.impTab;
        short[] impAct = levState.impAct;
        int start0 = start;
        short oldStateSeq = levState.state;
        byte cell = impTab[oldStateSeq][_prop];
        levState.state = GetState(cell);
        short actionSeq = impAct[GetAction(cell)];
        byte addLevel = impTab[levState.state][7];
        if (actionSeq != (short) 0) {
            byte[] bArr;
            switch (actionSeq) {
                case (short) 1:
                    levState.startON = start;
                    break;
                case (short) 2:
                    start = levState.startON;
                    break;
                case (short) 3:
                    setLevelsOutsideIsolates(levState.startON, start, (byte) (levState.runLevel + 1));
                    break;
                case (short) 4:
                    setLevelsOutsideIsolates(levState.startON, start, (byte) (levState.runLevel + 2));
                    break;
                case (short) 5:
                    if (levState.startL2EN >= 0) {
                        addPoint(levState.startL2EN, 1);
                    }
                    levState.startL2EN = -1;
                    if (this.insertPoints.points.length != 0 && this.insertPoints.size > this.insertPoints.confirmed) {
                        for (k = levState.lastStrongRTL + 1; k < start; k++) {
                            this.levels[k] = (byte) ((this.levels[k] - 2) & -2);
                        }
                        this.insertPoints.confirmed = this.insertPoints.size;
                        levState.lastStrongRTL = -1;
                        if (_prop == (short) 5) {
                            addPoint(start, 1);
                            this.insertPoints.confirmed = this.insertPoints.size;
                            break;
                        }
                    }
                    levState.lastStrongRTL = -1;
                    if ((impTab[oldStateSeq][7] & 1) != 0 && levState.startON > 0) {
                        start = levState.startON;
                    }
                    if (_prop == (short) 5) {
                        addPoint(start0, 1);
                        this.insertPoints.confirmed = this.insertPoints.size;
                        break;
                    }
                    break;
                case (short) 6:
                    if (this.insertPoints.points.length > 0) {
                        this.insertPoints.size = this.insertPoints.confirmed;
                    }
                    levState.startON = -1;
                    levState.startL2EN = -1;
                    levState.lastStrongRTL = limit - 1;
                    break;
                case (short) 7:
                    if (_prop != (short) 3 || this.dirProps[start] != (byte) 5 || this.reorderingMode == 6) {
                        if (levState.startL2EN == -1) {
                            levState.startL2EN = start;
                            break;
                        }
                    } else if (levState.startL2EN != -1) {
                        if (levState.startL2EN >= 0) {
                            addPoint(levState.startL2EN, 1);
                            levState.startL2EN = -2;
                        }
                        addPoint(start, 1);
                        break;
                    } else {
                        levState.lastStrongRTL = limit - 1;
                        break;
                    }
                    break;
                case (short) 8:
                    levState.lastStrongRTL = limit - 1;
                    levState.startON = -1;
                    break;
                case (short) 9:
                    k = start - 1;
                    while (k >= 0 && (this.levels[k] & 1) == 0) {
                        k--;
                    }
                    if (k >= 0) {
                        addPoint(k, 4);
                        this.insertPoints.confirmed = this.insertPoints.size;
                    }
                    levState.startON = start;
                    break;
                case (short) 10:
                    addPoint(start, 1);
                    addPoint(start, 2);
                    break;
                case (short) 11:
                    this.insertPoints.size = this.insertPoints.confirmed;
                    if (_prop == (short) 5) {
                        addPoint(start, 4);
                        this.insertPoints.confirmed = this.insertPoints.size;
                        break;
                    }
                    break;
                case (short) 12:
                    level = (byte) (levState.runLevel + addLevel);
                    for (k = levState.startON; k < start; k++) {
                        if (this.levels[k] < level) {
                            this.levels[k] = level;
                        }
                    }
                    this.insertPoints.confirmed = this.insertPoints.size;
                    levState.startON = start;
                    break;
                case (short) 13:
                    level = levState.runLevel;
                    k = start - 1;
                    while (k >= levState.startON) {
                        if (this.levels[k] == level + 3) {
                            while (this.levels[k] == level + 3) {
                                bArr = this.levels;
                                int k2 = k - 1;
                                bArr[k] = (byte) (bArr[k] - 2);
                                k = k2;
                            }
                            while (this.levels[k] == level) {
                                k--;
                            }
                        }
                        if (this.levels[k] == level + 2) {
                            this.levels[k] = level;
                        } else {
                            this.levels[k] = (byte) (level + 1);
                        }
                        k--;
                    }
                    break;
                case (short) 14:
                    level = (byte) (levState.runLevel + 1);
                    for (k = start - 1; k >= levState.startON; k--) {
                        if (this.levels[k] > level) {
                            bArr = this.levels;
                            bArr[k] = (byte) (bArr[k] - 2);
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("Internal ICU error in processPropertySeq");
            }
        }
        if (addLevel != (byte) 0 || start < start0) {
            level = (byte) (levState.runLevel + addLevel);
            if (start >= levState.runStart) {
                for (k = start; k < limit; k++) {
                    this.levels[k] = level;
                }
                return;
            }
            setLevelsOutsideIsolates(start, limit, level);
        }
    }

    private byte lastL_R_AL() {
        int i = this.prologue.length();
        while (i > 0) {
            int uchar = this.prologue.codePointBefore(i);
            i -= Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (dirProp == (byte) 0) {
                return (byte) 0;
            }
            if (dirProp == (byte) 1 || dirProp == (byte) 13) {
                return (byte) 1;
            }
            if (dirProp == (byte) 7) {
                return (byte) 4;
            }
        }
        return (byte) 4;
    }

    private byte firstL_R_AL_EN_AN() {
        int i = 0;
        while (i < this.epilogue.length()) {
            int uchar = this.epilogue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (dirProp == (byte) 0) {
                return (byte) 0;
            }
            if (dirProp == (byte) 1 || dirProp == (byte) 13) {
                return (byte) 1;
            }
            if (dirProp == (byte) 2) {
                return (byte) 2;
            }
            if (dirProp == (byte) 5) {
                return (byte) 3;
            }
        }
        return (byte) 4;
    }

    /* JADX WARNING: Removed duplicated region for block: B:48:0x01ac  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x02dd  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void resolveImplicitLevels(int start, int limit, short sor, short eor) {
        int start1;
        short stateImp;
        int i;
        byte dirProp;
        byte firstStrong;
        LevState levState = new LevState(null);
        short nextStrongProp = (short) 1;
        int nextStrongPos = -1;
        boolean inverseRTL = (start >= this.lastArabicPos || (GetParaLevelAt(start) & 1) <= 0) ? false : this.reorderingMode != 5 ? this.reorderingMode == 6 : true;
        levState.startL2EN = -1;
        levState.lastStrongRTL = -1;
        levState.runStart = start;
        levState.runLevel = this.levels[start];
        levState.impTab = this.impTabPair.imptab[levState.runLevel & 1];
        levState.impAct = this.impTabPair.impact[levState.runLevel & 1];
        if (start == 0 && this.prologue != null) {
            byte lastStrong = lastL_R_AL();
            if (lastStrong != (byte) 4) {
                sor = (short) lastStrong;
            }
        }
        if (this.dirProps[start] == (byte) 22) {
            levState.startON = this.isolates[this.isolateCount].startON;
            start1 = this.isolates[this.isolateCount].start1;
            stateImp = this.isolates[this.isolateCount].stateImp;
            levState.state = this.isolates[this.isolateCount].state;
            this.isolateCount--;
        } else {
            levState.startON = -1;
            start1 = start;
            if (this.dirProps[start] == (byte) 17) {
                stateImp = (short) (sor + 1);
            } else {
                stateImp = (short) 0;
            }
            levState.state = (short) 0;
            processPropertySeq(levState, sor, start, start);
        }
        int start2 = start;
        for (i = start; i <= limit; i++) {
            short gprop;
            if (i >= limit) {
                int k = limit - 1;
                while (k > start && (DirPropFlag(this.dirProps[k]) & MASK_BN_EXPLICIT) != 0) {
                    k--;
                }
                dirProp = this.dirProps[k];
                if (!(dirProp == (byte) 20 || dirProp == (byte) 21)) {
                    gprop = eor;
                }
                if (limit == this.length && this.epilogue != null) {
                    firstStrong = firstL_R_AL_EN_AN();
                    if (firstStrong != (byte) 4) {
                        eor = (short) firstStrong;
                    }
                }
                i = limit - 1;
                while (i > start && (DirPropFlag(this.dirProps[i]) & MASK_BN_EXPLICIT) != 0) {
                    i--;
                }
                dirProp = this.dirProps[i];
                if ((dirProp != (byte) 20 || dirProp == (byte) 21) && limit < this.length) {
                    this.isolateCount++;
                    if (this.isolates[this.isolateCount] == null) {
                        this.isolates[this.isolateCount] = new Isolate();
                    }
                    this.isolates[this.isolateCount].stateImp = stateImp;
                    this.isolates[this.isolateCount].state = levState.state;
                    this.isolates[this.isolateCount].start1 = start1;
                    this.isolates[this.isolateCount].startON = levState.startON;
                }
                processPropertySeq(levState, eor, limit, limit);
                return;
            }
            int prop = this.dirProps[i];
            if (prop == 7) {
                this.isolateCount = -1;
            }
            if (inverseRTL) {
                if (prop == 13) {
                    prop = 1;
                } else if (prop == 2) {
                    if (nextStrongPos <= i) {
                        nextStrongProp = (short) 1;
                        nextStrongPos = limit;
                        int j = i + 1;
                        while (j < limit) {
                            byte prop1 = this.dirProps[j];
                            if (prop1 == (byte) 0 || prop1 == (byte) 1 || prop1 == (byte) 13) {
                                nextStrongProp = (short) prop1;
                                nextStrongPos = j;
                            } else {
                                j++;
                            }
                        }
                    }
                    if (nextStrongProp == (short) 13) {
                        prop = 5;
                    }
                }
            }
            gprop = groupProp[prop];
            short oldStateImp = stateImp;
            short cell = impTabProps[oldStateImp][gprop];
            stateImp = GetStateProps(cell);
            short actionImp = GetActionProps(cell);
            if (i == limit && actionImp == (short) 0) {
                actionImp = (short) 1;
            }
            if (actionImp != (short) 0) {
                short resProp = impTabProps[oldStateImp][15];
                switch (actionImp) {
                    case (short) 1:
                        processPropertySeq(levState, resProp, start1, i);
                        start1 = i;
                        break;
                    case (short) 2:
                        start2 = i;
                        break;
                    case (short) 3:
                        processPropertySeq(levState, resProp, start1, start2);
                        processPropertySeq(levState, (short) 4, start2, i);
                        start1 = i;
                        break;
                    case (short) 4:
                        processPropertySeq(levState, resProp, start1, start2);
                        start1 = start2;
                        start2 = i;
                        break;
                    default:
                        throw new IllegalStateException("Internal ICU error in resolveImplicitLevels");
                }
            }
        }
        firstStrong = firstL_R_AL_EN_AN();
        if (firstStrong != (byte) 4) {
        }
        i = limit - 1;
        while (i > start) {
            i--;
        }
        dirProp = this.dirProps[i];
        if (dirProp != (byte) 20) {
        }
        this.isolateCount++;
        if (this.isolates[this.isolateCount] == null) {
        }
        this.isolates[this.isolateCount].stateImp = stateImp;
        this.isolates[this.isolateCount].state = levState.state;
        this.isolates[this.isolateCount].start1 = start1;
        this.isolates[this.isolateCount].startON = levState.startON;
    }

    private void adjustWSLevels() {
        if ((this.flags & MASK_WS) != 0) {
            int i = this.trailingWSStart;
            while (i > 0) {
                int flag;
                while (i > 0) {
                    i--;
                    flag = DirPropFlag(this.dirProps[i]);
                    if ((MASK_WS & flag) == 0) {
                        break;
                    } else if (!this.orderParagraphsLTR || (DirPropFlag((byte) 7) & flag) == 0) {
                        this.levels[i] = GetParaLevelAt(i);
                    } else {
                        this.levels[i] = (byte) 0;
                    }
                }
                while (i > 0) {
                    i--;
                    flag = DirPropFlag(this.dirProps[i]);
                    if ((MASK_BN_EXPLICIT & flag) == 0) {
                        if (this.orderParagraphsLTR && (DirPropFlag((byte) 7) & flag) != 0) {
                            this.levels[i] = (byte) 0;
                            break;
                        } else if ((MASK_B_S & flag) != 0) {
                            this.levels[i] = GetParaLevelAt(i);
                            break;
                        }
                    } else {
                        this.levels[i] = this.levels[i + 1];
                    }
                }
            }
        }
    }

    public void setContext(String prologue, String epilogue) {
        if (prologue == null || prologue.length() <= 0) {
            prologue = null;
        }
        this.prologue = prologue;
        if (epilogue == null || epilogue.length() <= 0) {
            epilogue = null;
        }
        this.epilogue = epilogue;
    }

    private void setParaSuccess() {
        this.prologue = null;
        this.epilogue = null;
        this.paraBidi = this;
    }

    int Bidi_Min(int x, int y) {
        return x < y ? x : y;
    }

    int Bidi_Abs(int x) {
        return x >= 0 ? x : -x;
    }

    void setParaRunsOnly(char[] parmText, byte parmParaLevel) {
        this.reorderingMode = 0;
        int parmLength = parmText.length;
        if (parmLength == 0) {
            setPara(parmText, parmParaLevel, null);
            this.reorderingMode = 3;
            return;
        }
        int runLength;
        int logicalStart;
        int j;
        int index;
        int index1;
        int saveOptions = this.reorderingOptions;
        if ((saveOptions & 1) > 0) {
            this.reorderingOptions &= -2;
            this.reorderingOptions |= 2;
        }
        parmParaLevel = (byte) (parmParaLevel & 1);
        setPara(parmText, parmParaLevel, null);
        byte[] saveLevels = new byte[this.length];
        System.arraycopy(getLevels(), 0, saveLevels, 0, this.length);
        int saveTrailingWSStart = this.trailingWSStart;
        String visualText = writeReordered(2);
        int[] visualMap = getVisualMap();
        this.reorderingOptions = saveOptions;
        int saveLength = this.length;
        byte saveDirection = this.direction;
        this.reorderingMode = 5;
        setPara(visualText, (byte) (parmParaLevel ^ 1), null);
        BidiLine.getRuns(this);
        int addedRuns = 0;
        int oldRunCount = this.runCount;
        int visualStart = 0;
        int i = 0;
        while (i < oldRunCount) {
            runLength = this.runs[i].limit - visualStart;
            if (runLength >= 2) {
                logicalStart = this.runs[i].start;
                for (j = logicalStart + 1; j < logicalStart + runLength; j++) {
                    index = visualMap[j];
                    index1 = visualMap[j - 1];
                    if (Bidi_Abs(index - index1) != 1 || saveLevels[index] != saveLevels[index1]) {
                        addedRuns++;
                    }
                }
            }
            i++;
            visualStart += runLength;
        }
        if (addedRuns > 0) {
            getRunsMemory(oldRunCount + addedRuns);
            if (this.runCount == 1) {
                this.runsMemory[0] = this.runs[0];
            } else {
                System.arraycopy(this.runs, 0, this.runsMemory, 0, this.runCount);
            }
            this.runs = this.runsMemory;
            this.runCount += addedRuns;
            for (i = oldRunCount; i < this.runCount; i++) {
                if (this.runs[i] == null) {
                    this.runs[i] = new BidiRun(0, 0, (byte) 0);
                }
            }
        }
        for (i = oldRunCount - 1; i >= 0; i--) {
            int newI = i + addedRuns;
            if (i == 0) {
                runLength = this.runs[0].limit;
            } else {
                runLength = this.runs[i].limit - this.runs[i - 1].limit;
            }
            logicalStart = this.runs[i].start;
            int indexOddBit = this.runs[i].level & 1;
            int logicalPos;
            if (runLength < 2) {
                if (addedRuns > 0) {
                    this.runs[newI].copyFrom(this.runs[i]);
                }
                logicalPos = visualMap[logicalStart];
                this.runs[newI].start = logicalPos;
                this.runs[newI].level = (byte) (saveLevels[logicalPos] ^ indexOddBit);
            } else {
                int start;
                int limit;
                int step;
                if (indexOddBit > 0) {
                    start = logicalStart;
                    limit = (logicalStart + runLength) - 1;
                    step = 1;
                } else {
                    start = (logicalStart + runLength) - 1;
                    limit = logicalStart;
                    step = -1;
                }
                for (j = start; j != limit; j += step) {
                    index = visualMap[j];
                    index1 = visualMap[j + step];
                    if (Bidi_Abs(index - index1) != 1 || saveLevels[index] != saveLevels[index1]) {
                        logicalPos = Bidi_Min(visualMap[start], index);
                        this.runs[newI].start = logicalPos;
                        this.runs[newI].level = (byte) (saveLevels[logicalPos] ^ indexOddBit);
                        this.runs[newI].limit = this.runs[i].limit;
                        BidiRun bidiRun = this.runs[i];
                        bidiRun.limit -= Bidi_Abs(j - start) + 1;
                        int insertRemove = this.runs[i].insertRemove & 10;
                        this.runs[newI].insertRemove = insertRemove;
                        bidiRun = this.runs[i];
                        bidiRun.insertRemove &= ~insertRemove;
                        start = j + step;
                        addedRuns--;
                        newI--;
                    }
                }
                if (addedRuns > 0) {
                    this.runs[newI].copyFrom(this.runs[i]);
                }
                logicalPos = Bidi_Min(visualMap[start], visualMap[limit]);
                this.runs[newI].start = logicalPos;
                this.runs[newI].level = (byte) (saveLevels[logicalPos] ^ indexOddBit);
            }
        }
        this.paraLevel = (byte) (this.paraLevel ^ 1);
        this.text = parmText;
        this.length = saveLength;
        this.originalLength = parmLength;
        this.direction = saveDirection;
        this.levels = saveLevels;
        this.trailingWSStart = saveTrailingWSStart;
        if (this.runCount > 1) {
            this.direction = (byte) 2;
        }
        this.reorderingMode = 3;
    }

    public void setPara(String text, byte paraLevel, byte[] embeddingLevels) {
        if (text == null) {
            setPara(new char[0], paraLevel, embeddingLevels);
        } else {
            setPara(text.toCharArray(), paraLevel, embeddingLevels);
        }
    }

    public void setPara(char[] chars, byte paraLevel, byte[] embeddingLevels) {
        if (paraLevel < (byte) 126) {
            verifyRange(paraLevel, 0, 126);
        }
        if (chars == null) {
            chars = new char[0];
        }
        if (this.reorderingMode == 3) {
            setParaRunsOnly(chars, paraLevel);
            return;
        }
        this.paraBidi = null;
        this.text = chars;
        int length = this.text.length;
        this.resultLength = length;
        this.originalLength = length;
        this.length = length;
        this.paraLevel = paraLevel;
        this.direction = (byte) (paraLevel & 1);
        this.paraCount = 1;
        this.dirProps = new byte[0];
        this.levels = new byte[0];
        this.runs = new BidiRun[0];
        this.isGoodLogicalToVisualRunsMap = false;
        this.insertPoints.size = 0;
        this.insertPoints.confirmed = 0;
        this.defaultParaLevel = IsDefaultLevel(paraLevel) ? paraLevel : (byte) 0;
        if (this.length == 0) {
            if (IsDefaultLevel(paraLevel)) {
                this.paraLevel = (byte) (this.paraLevel & 1);
                this.defaultParaLevel = (byte) 0;
            }
            this.flags = DirPropFlagLR(paraLevel);
            this.runCount = 0;
            this.paraCount = 0;
            setParaSuccess();
            return;
        }
        int i;
        this.runCount = -1;
        getDirPropsMemory(this.length);
        this.dirProps = this.dirPropsMemory;
        getDirProps();
        this.trailingWSStart = this.length;
        if (embeddingLevels == null) {
            getLevelsMemory(this.length);
            this.levels = this.levelsMemory;
            this.direction = resolveExplicitLevels();
        } else {
            this.levels = embeddingLevels;
            this.direction = checkExplicitLevels();
        }
        if (this.isolateCount > 0 && (this.isolates == null || this.isolates.length < this.isolateCount)) {
            this.isolates = new Isolate[(this.isolateCount + 3)];
        }
        this.isolateCount = -1;
        switch (this.direction) {
            case (byte) 0:
                this.trailingWSStart = 0;
                break;
            case (byte) 1:
                this.trailingWSStart = 0;
                break;
            default:
                switch (this.reorderingMode) {
                    case 0:
                        this.impTabPair = impTab_DEFAULT;
                        break;
                    case 1:
                        this.impTabPair = impTab_NUMBERS_SPECIAL;
                        break;
                    case 2:
                        this.impTabPair = impTab_GROUP_NUMBERS_WITH_R;
                        break;
                    case 3:
                        throw new InternalError("Internal ICU error in setPara");
                    case 4:
                        this.impTabPair = impTab_INVERSE_NUMBERS_AS_L;
                        break;
                    case 5:
                        if ((this.reorderingOptions & 1) == 0) {
                            this.impTabPair = impTab_INVERSE_LIKE_DIRECT;
                            break;
                        } else {
                            this.impTabPair = impTab_INVERSE_LIKE_DIRECT_WITH_MARKS;
                            break;
                        }
                    case 6:
                        if ((this.reorderingOptions & 1) == 0) {
                            this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL;
                            break;
                        } else {
                            this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS;
                            break;
                        }
                }
                if (embeddingLevels == null && this.paraCount <= 1 && (this.flags & DirPropFlagMultiRuns) == 0) {
                    resolveImplicitLevels(0, this.length, (short) GetLRFromLevel(GetParaLevelAt(0)), (short) GetLRFromLevel(GetParaLevelAt(this.length - 1)));
                } else {
                    short eor;
                    int limit = 0;
                    byte level = GetParaLevelAt(0);
                    byte nextLevel = this.levels[0];
                    if (level < nextLevel) {
                        eor = (short) GetLRFromLevel(nextLevel);
                    } else {
                        eor = (short) GetLRFromLevel(level);
                    }
                    while (true) {
                        short sor;
                        i = limit;
                        level = nextLevel;
                        if (limit <= 0 || this.dirProps[i - 1] != (byte) 7) {
                            sor = eor;
                        } else {
                            sor = (short) GetLRFromLevel(GetParaLevelAt(i));
                        }
                        while (true) {
                            limit++;
                            if (limit >= this.length || (this.levels[limit] != level && (DirPropFlag(this.dirProps[limit]) & MASK_BN_EXPLICIT) == 0)) {
                            }
                        }
                        if (limit < this.length) {
                            nextLevel = this.levels[limit];
                        } else {
                            nextLevel = GetParaLevelAt(this.length - 1);
                        }
                        if (NoOverride(level) < NoOverride(nextLevel)) {
                            eor = (short) GetLRFromLevel(nextLevel);
                        } else {
                            eor = (short) GetLRFromLevel(level);
                        }
                        if ((level & -128) == 0) {
                            resolveImplicitLevels(i, limit, sor, eor);
                        } else {
                            while (true) {
                                byte[] bArr = this.levels;
                                int start = i + 1;
                                bArr[i] = (byte) (bArr[i] & 127);
                                if (start < limit) {
                                    i = start;
                                } else {
                                    i = start;
                                }
                            }
                        }
                        if (limit < this.length) {
                        }
                    }
                }
                adjustWSLevels();
                break;
        }
        if (this.defaultParaLevel > (byte) 0 && (this.reorderingOptions & 1) != 0 && (this.reorderingMode == 5 || this.reorderingMode == 6)) {
            int i2 = 0;
            while (i2 < this.paraCount) {
                int last = this.paras_limit[i2] - 1;
                if (this.paras_level[i2] != (byte) 0) {
                    i = i2 == 0 ? 0 : this.paras_limit[i2 - 1];
                    int j = last;
                    while (j >= i) {
                        byte dirProp = this.dirProps[j];
                        if (dirProp == (byte) 0) {
                            if (j < last) {
                                while (this.dirProps[last] == (byte) 7) {
                                    last--;
                                }
                            }
                            addPoint(last, 4);
                        } else if ((DirPropFlag(dirProp) & MASK_R_AL) == 0) {
                            j--;
                        }
                    }
                }
                i2++;
            }
        }
        if ((this.reorderingOptions & 2) != 0) {
            this.resultLength -= this.controlCount;
        } else {
            this.resultLength += this.insertPoints.size;
        }
        setParaSuccess();
    }

    public void setPara(AttributedCharacterIterator paragraph) {
        Boolean runDirection = (Boolean) paragraph.getAttribute(TextAttribute.RUN_DIRECTION);
        byte paraLvl = runDirection == null ? LEVEL_DEFAULT_LTR : runDirection.equals(TextAttribute.RUN_DIRECTION_LTR) ? (byte) 0 : (byte) 1;
        byte[] lvls = null;
        int len = paragraph.getEndIndex() - paragraph.getBeginIndex();
        byte[] embeddingLevels = new byte[len];
        char[] txt = new char[len];
        int i = 0;
        char ch = paragraph.first();
        while (ch != 65535) {
            txt[i] = ch;
            Integer embedding = (Integer) paragraph.getAttribute(TextAttribute.BIDI_EMBEDDING);
            if (embedding != null) {
                byte level = embedding.byteValue();
                if (level != (byte) 0) {
                    if (level < (byte) 0) {
                        lvls = embeddingLevels;
                        embeddingLevels[i] = (byte) ((0 - level) | -128);
                    } else {
                        lvls = embeddingLevels;
                        embeddingLevels[i] = level;
                    }
                }
            }
            ch = paragraph.next();
            i++;
        }
        NumericShaper shaper = (NumericShaper) paragraph.getAttribute(TextAttribute.NUMERIC_SHAPING);
        if (shaper != null) {
            shaper.shape(txt, 0, len);
        }
        setPara(txt, paraLvl, lvls);
    }

    public void orderParagraphsLTR(boolean ordarParaLTR) {
        this.orderParagraphsLTR = ordarParaLTR;
    }

    public boolean isOrderParagraphsLTR() {
        return this.orderParagraphsLTR;
    }

    public byte getDirection() {
        verifyValidParaOrLine();
        return this.direction;
    }

    public String getTextAsString() {
        verifyValidParaOrLine();
        return new String(this.text);
    }

    public char[] getText() {
        verifyValidParaOrLine();
        return this.text;
    }

    public int getLength() {
        verifyValidParaOrLine();
        return this.originalLength;
    }

    public int getProcessedLength() {
        verifyValidParaOrLine();
        return this.length;
    }

    public int getResultLength() {
        verifyValidParaOrLine();
        return this.resultLength;
    }

    public byte getParaLevel() {
        verifyValidParaOrLine();
        return this.paraLevel;
    }

    public int countParagraphs() {
        verifyValidParaOrLine();
        return this.paraCount;
    }

    public BidiRun getParagraphByIndex(int paraIndex) {
        int paraStart;
        verifyValidParaOrLine();
        verifyRange(paraIndex, 0, this.paraCount);
        Bidi bidi = this.paraBidi;
        if (paraIndex == 0) {
            paraStart = 0;
        } else {
            paraStart = bidi.paras_limit[paraIndex - 1];
        }
        BidiRun bidiRun = new BidiRun();
        bidiRun.start = paraStart;
        bidiRun.limit = bidi.paras_limit[paraIndex];
        bidiRun.level = GetParaLevelAt(paraStart);
        return bidiRun;
    }

    public BidiRun getParagraph(int charIndex) {
        verifyValidParaOrLine();
        Bidi bidi = this.paraBidi;
        verifyRange(charIndex, 0, bidi.length);
        int paraIndex = 0;
        while (charIndex >= bidi.paras_limit[paraIndex]) {
            paraIndex++;
        }
        return getParagraphByIndex(paraIndex);
    }

    public int getParagraphIndex(int charIndex) {
        verifyValidParaOrLine();
        Bidi bidi = this.paraBidi;
        verifyRange(charIndex, 0, bidi.length);
        int paraIndex = 0;
        while (charIndex >= bidi.paras_limit[paraIndex]) {
            paraIndex++;
        }
        return paraIndex;
    }

    public void setCustomClassifier(BidiClassifier classifier) {
        this.customClassifier = classifier;
    }

    public BidiClassifier getCustomClassifier() {
        return this.customClassifier;
    }

    /* JADX WARNING: Missing block: B:3:0x000c, code:
            if (r0 == 23) goto L_0x000e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCustomizedClass(int c) {
        int dir;
        if (this.customClassifier != null) {
            dir = this.customClassifier.classify(c);
        }
        dir = this.bdp.getClass(c);
        if (dir >= 23) {
            return 10;
        }
        return dir;
    }

    public Bidi setLine(int start, int limit) {
        verifyValidPara();
        verifyRange(start, 0, limit);
        verifyRange(limit, 0, this.length + 1);
        if (getParagraphIndex(start) == getParagraphIndex(limit - 1)) {
            return BidiLine.setLine(this, start, limit);
        }
        throw new IllegalArgumentException();
    }

    public byte getLevelAt(int charIndex) {
        verifyValidParaOrLine();
        verifyRange(charIndex, 0, this.length);
        return BidiLine.getLevelAt(this, charIndex);
    }

    public byte[] getLevels() {
        verifyValidParaOrLine();
        if (this.length <= 0) {
            return new byte[0];
        }
        return BidiLine.getLevels(this);
    }

    public BidiRun getLogicalRun(int logicalPosition) {
        verifyValidParaOrLine();
        verifyRange(logicalPosition, 0, this.length);
        return BidiLine.getLogicalRun(this, logicalPosition);
    }

    public int countRuns() {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        return this.runCount;
    }

    public BidiRun getVisualRun(int runIndex) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(runIndex, 0, this.runCount);
        return BidiLine.getVisualRun(this, runIndex);
    }

    public int getVisualIndex(int logicalIndex) {
        verifyValidParaOrLine();
        verifyRange(logicalIndex, 0, this.length);
        return BidiLine.getVisualIndex(this, logicalIndex);
    }

    public int getLogicalIndex(int visualIndex) {
        verifyValidParaOrLine();
        verifyRange(visualIndex, 0, this.resultLength);
        if (this.insertPoints.size == 0 && this.controlCount == 0) {
            if (this.direction == (byte) 0) {
                return visualIndex;
            }
            if (this.direction == (byte) 1) {
                return (this.length - visualIndex) - 1;
            }
        }
        BidiLine.getRuns(this);
        return BidiLine.getLogicalIndex(this, visualIndex);
    }

    public int[] getLogicalMap() {
        countRuns();
        if (this.length <= 0) {
            return new int[0];
        }
        return BidiLine.getLogicalMap(this);
    }

    public int[] getVisualMap() {
        countRuns();
        if (this.resultLength <= 0) {
            return new int[0];
        }
        return BidiLine.getVisualMap(this);
    }

    public static int[] reorderLogical(byte[] levels) {
        return BidiLine.reorderLogical(levels);
    }

    public static int[] reorderVisual(byte[] levels) {
        return BidiLine.reorderVisual(levels);
    }

    public static int[] invertMap(int[] srcMap) {
        if (srcMap == null) {
            return null;
        }
        return BidiLine.invertMap(srcMap);
    }

    public Bidi(String paragraph, int flags) {
        this(paragraph.toCharArray(), 0, null, 0, paragraph.length(), flags);
    }

    public Bidi(AttributedCharacterIterator paragraph) {
        this();
        setPara(paragraph);
    }

    public Bidi(char[] text, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags) {
        byte paraLvl;
        byte[] paraEmbeddings;
        this();
        switch (flags) {
            case 1:
                paraLvl = (byte) 1;
                break;
            case 126:
                paraLvl = LEVEL_DEFAULT_LTR;
                break;
            case 127:
                paraLvl = LEVEL_DEFAULT_RTL;
                break;
            default:
                paraLvl = (byte) 0;
                break;
        }
        if (embeddings == null) {
            paraEmbeddings = null;
        } else {
            paraEmbeddings = new byte[paragraphLength];
            for (int i = 0; i < paragraphLength; i++) {
                byte lev = embeddings[i + embStart];
                if (lev < (byte) 0) {
                    lev = (byte) ((-lev) | -128);
                } else if (lev == (byte) 0) {
                    lev = paraLvl;
                    if (paraLvl > MAX_EXPLICIT_LEVEL) {
                        lev = (byte) (lev & 1);
                    }
                }
                paraEmbeddings[i] = lev;
            }
        }
        if (textStart == 0 && embStart == 0 && paragraphLength == text.length) {
            setPara(text, paraLvl, paraEmbeddings);
            return;
        }
        char[] paraText = new char[paragraphLength];
        System.arraycopy(text, textStart, paraText, 0, paragraphLength);
        setPara(paraText, paraLvl, paraEmbeddings);
    }

    public Bidi createLineBidi(int lineStart, int lineLimit) {
        return setLine(lineStart, lineLimit);
    }

    public boolean isMixed() {
        return !isLeftToRight() ? isRightToLeft() ^ 1 : false;
    }

    public boolean isLeftToRight() {
        return getDirection() == (byte) 0 && (this.paraLevel & 1) == 0;
    }

    public boolean isRightToLeft() {
        return getDirection() == (byte) 1 && (this.paraLevel & 1) == 1;
    }

    public boolean baseIsLeftToRight() {
        return getParaLevel() == (byte) 0;
    }

    public int getBaseLevel() {
        return getParaLevel();
    }

    public int getRunCount() {
        return countRuns();
    }

    void getLogicalToVisualRunsMap() {
        if (!this.isGoodLogicalToVisualRunsMap) {
            int i;
            int count = countRuns();
            if (this.logicalToVisualRunsMap == null || this.logicalToVisualRunsMap.length < count) {
                this.logicalToVisualRunsMap = new int[count];
            }
            long[] keys = new long[count];
            for (i = 0; i < count; i++) {
                keys[i] = (((long) this.runs[i].start) << 32) + ((long) i);
            }
            Arrays.sort(keys);
            for (i = 0; i < count; i++) {
                this.logicalToVisualRunsMap[i] = (int) (keys[i] & -1);
            }
            this.isGoodLogicalToVisualRunsMap = true;
        }
    }

    public int getRunLevel(int run) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, 0, this.runCount);
        getLogicalToVisualRunsMap();
        return this.runs[this.logicalToVisualRunsMap[run]].level;
    }

    public int getRunStart(int run) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, 0, this.runCount);
        getLogicalToVisualRunsMap();
        return this.runs[this.logicalToVisualRunsMap[run]].start;
    }

    public int getRunLimit(int run) {
        int len;
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, 0, this.runCount);
        getLogicalToVisualRunsMap();
        int idx = this.logicalToVisualRunsMap[run];
        if (idx == 0) {
            len = this.runs[idx].limit;
        } else {
            len = this.runs[idx].limit - this.runs[idx - 1].limit;
        }
        return this.runs[idx].start + len;
    }

    public static boolean requiresBidi(char[] text, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (((1 << UCharacter.getDirection(text[i])) & 57378) != 0) {
                return true;
            }
        }
        return false;
    }

    public static void reorderVisually(byte[] levels, int levelStart, Object[] objects, int objectStart, int count) {
        byte[] reorderLevels = new byte[count];
        System.arraycopy(levels, levelStart, reorderLevels, 0, count);
        int[] indexMap = reorderVisual(reorderLevels);
        Object[] temp = new Object[count];
        System.arraycopy(objects, objectStart, temp, 0, count);
        for (int i = 0; i < count; i++) {
            objects[objectStart + i] = temp[indexMap[i]];
        }
    }

    public String writeReordered(int options) {
        verifyValidParaOrLine();
        if (this.length == 0) {
            return "";
        }
        return BidiWriter.writeReordered(this, options);
    }

    public static String writeReverse(String src, int options) {
        if (src == null) {
            throw new IllegalArgumentException();
        } else if (src.length() > 0) {
            return BidiWriter.writeReverse(src, options);
        } else {
            return "";
        }
    }
}
