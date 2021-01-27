package ohos.global.icu.text;

import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;
import ohos.global.icu.impl.UBiDiProps;
import ohos.global.icu.lang.UCharacter;
import ohos.media.camera.params.adapter.camera2ex.CameraMetadataEx;

public class Bidi {
    static final byte AL = 13;
    static final byte AN = 5;
    static final byte B = 7;
    static final byte BN = 18;
    @Deprecated
    public static final int CLASS_DEFAULT = 23;
    private static final char CR = '\r';
    static final byte CS = 6;
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = 126;
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = 127;
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;
    public static final short DO_MIRRORING = 2;
    static final int[] DirPropFlagE = {DirPropFlag((byte) 11), DirPropFlag((byte) 14)};
    static final int[] DirPropFlagLR = {DirPropFlag((byte) 0), DirPropFlag((byte) 1)};
    static final int DirPropFlagMultiRuns = DirPropFlag((byte) 31);
    static final int[] DirPropFlagO = {DirPropFlag((byte) 12), DirPropFlag((byte) 15)};
    static final byte EN = 2;
    static final byte ENL = 23;
    static final byte ENR = 24;
    static final byte ES = 3;
    static final byte ET = 4;
    static final int FIRSTALLOC = 10;
    static final byte FOUND_L = ((byte) DirPropFlag((byte) 0));
    static final byte FOUND_R = ((byte) DirPropFlag((byte) 1));
    static final byte FSI = 19;
    private static final int IMPTABLEVELS_COLUMNS = 8;
    private static final int IMPTABLEVELS_RES = 7;
    private static final int IMPTABPROPS_COLUMNS = 16;
    private static final int IMPTABPROPS_RES = 15;
    public static final short INSERT_LRM_FOR_NUMERIC = 4;
    static final int ISOLATE = 256;
    public static final short KEEP_BASE_COMBINING = 1;
    static final byte L = 0;
    public static final byte LEVEL_DEFAULT_LTR = 126;
    public static final byte LEVEL_DEFAULT_RTL = Byte.MAX_VALUE;
    public static final byte LEVEL_OVERRIDE = Byte.MIN_VALUE;
    private static final char LF = '\n';
    static final int LOOKING_FOR_PDI = 3;
    static final byte LRE = 11;
    static final byte LRI = 20;
    static final int LRM_AFTER = 2;
    static final int LRM_BEFORE = 1;
    static final byte LRO = 12;
    public static final byte LTR = 0;
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
    public static final byte MAX_EXPLICIT_LEVEL = 125;
    public static final byte MIXED = 2;
    public static final byte NEUTRAL = 3;
    static final int NOT_SEEKING_STRONG = 0;
    static final byte NSM = 17;
    static final byte ON = 10;
    public static final int OPTION_DEFAULT = 0;
    public static final int OPTION_INSERT_MARKS = 1;
    public static final int OPTION_REMOVE_CONTROLS = 2;
    public static final int OPTION_STREAMING = 4;
    public static final short OUTPUT_REVERSE = 16;
    static final byte PDF = 16;
    static final byte PDI = 22;
    static final byte R = 1;
    public static final short REMOVE_BIDI_CONTROLS = 8;
    static final short REORDER_COUNT = 7;
    public static final short REORDER_DEFAULT = 0;
    public static final short REORDER_GROUP_NUMBERS_WITH_R = 2;
    public static final short REORDER_INVERSE_FOR_NUMBERS_SPECIAL = 6;
    public static final short REORDER_INVERSE_LIKE_DIRECT = 5;
    public static final short REORDER_INVERSE_NUMBERS_AS_L = 4;
    static final short REORDER_LAST_LOGICAL_TO_VISUAL = 1;
    public static final short REORDER_NUMBERS_SPECIAL = 1;
    public static final short REORDER_RUNS_ONLY = 3;
    static final byte RLE = 14;
    static final byte RLI = 21;
    static final int RLM_AFTER = 8;
    static final int RLM_BEFORE = 4;
    static final byte RLO = 15;
    public static final byte RTL = 1;
    static final byte S = 8;
    static final int SEEKING_STRONG_FOR_FSI = 2;
    static final int SEEKING_STRONG_FOR_PARA = 1;
    static final int SIMPLE_OPENINGS_COUNT = 20;
    static final int SIMPLE_PARAS_COUNT = 10;
    static final byte WS = 9;
    private static final short _AN = 3;
    private static final short _B = 6;
    private static final short _EN = 2;
    private static final short _L = 0;
    private static final short _ON = 4;
    private static final short _R = 1;
    private static final short _S = 5;
    private static final short[] groupProp = {0, 1, 2, 7, 8, 3, 9, 6, 5, 4, 4, 10, 10, 12, 10, 10, 10, 11, 10, 4, 4, 4, 4, 13, 14};
    private static final short[] impAct0 = {0, 1, 2, 3, 4};
    private static final short[] impAct1 = {0, 1, 13, 14};
    private static final short[] impAct2 = {0, 1, 2, 5, 6, 7, 8};
    private static final short[] impAct3 = {0, 1, 9, 10, 11, 12};
    private static final byte[][] impTabL_DEFAULT = {new byte[]{0, 1, 0, 2, 0, 0, 0, 0}, new byte[]{0, 1, 3, 3, 20, 20, 0, 1}, new byte[]{0, 1, 0, 2, 21, 21, 0, 2}, new byte[]{0, 1, 3, 3, 20, 20, 0, 2}, new byte[]{0, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 51, 51, 4, 4, 0, 0}, new byte[]{0, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 0, 50, 5, 5, 0, 0}};
    private static final byte[][] impTabL_GROUP_NUMBERS_WITH_R = {new byte[]{0, 3, 17, 17, 0, 0, 0, 0}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 3, 1, 1, 2, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 2}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 3, 1, 1, 2, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1}, new byte[]{0, 3, 5, 5, 20, 0, 0, 1}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 3, 5, 5, 4, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1}, new byte[]{0, 3, 5, 5, 20, 0, 0, 2}};
    private static final byte[][] impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = {new byte[]{0, 98, 1, 1, 0, 0, 0, 0}, new byte[]{0, 98, 1, 1, 0, 48, 0, 4}, new byte[]{0, 98, 84, 84, 19, 48, 0, 3}, new byte[]{48, 66, 84, 84, 3, 48, 48, 3}, new byte[]{48, 66, 4, 4, 19, 48, 48, 4}};
    private static final byte[][] impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS = {new byte[]{0, 99, 0, 1, 0, 0, 0, 0}, new byte[]{0, 99, 0, 1, 18, 48, 0, 4}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 99, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1, 2, 48, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 3}, new byte[]{0, 99, 85, 86, 20, 48, 0, 3}, new byte[]{48, 67, 85, 86, 4, 48, 48, 3}, new byte[]{48, 67, 5, 86, 20, 48, 48, 4}, new byte[]{48, 67, 85, 6, 20, 48, 48, 4}};
    private static final byte[][] impTabL_INVERSE_NUMBERS_AS_L = {new byte[]{0, 1, 0, 0, 0, 0, 0, 0}, new byte[]{0, 1, 0, 0, 20, 20, 0, 1}, new byte[]{0, 1, 0, 0, 21, 21, 0, 2}, new byte[]{0, 1, 0, 0, 20, 20, 0, 2}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 4, 4, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 5, 5, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_BASE, 1}};
    private static final byte[][] impTabL_NUMBERS_SPECIAL = {new byte[]{0, 2, 17, 17, 0, 0, 0, 0}, new byte[]{0, 66, 1, 1, 0, 0, 0, 0}, new byte[]{0, 2, 4, 4, 19, 19, 0, 1}, new byte[]{0, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_HDR, 52, 52, 3, 3, 0, 0}, new byte[]{0, 2, 4, 4, 19, 19, 0, 2}};
    private static final short[][] impTabProps = {new short[]{1, 2, 4, 5, 7, 15, 17, 7, 9, 7, 0, 7, 3, 18, 21, 4}, new short[]{1, 34, 36, 37, 39, 47, 49, 39, 41, 39, 1, 1, 35, 50, 53, 0}, new short[]{33, 2, 36, 37, 39, 47, 49, 39, 41, 39, 2, 2, 35, 50, 53, 1}, new short[]{33, 34, 38, 38, 40, 48, 49, 40, 40, 40, 3, 3, 3, 50, 53, 1}, new short[]{33, 34, 4, 37, 39, 47, 49, 74, 11, 74, 4, 4, 35, 18, 21, 2}, new short[]{33, 34, 36, 5, 39, 47, 49, 39, 41, 76, 5, 5, 35, 50, 53, 3}, new short[]{33, 34, 6, 6, 40, 48, 49, 40, 40, 77, 6, 6, 35, 18, 21, 3}, new short[]{33, 34, 36, 37, 7, 47, 49, 7, 78, 7, 7, 7, 35, 50, 53, 4}, new short[]{33, 34, 38, 38, 8, 48, 49, 8, 8, 8, 8, 8, 35, 50, 53, 4}, new short[]{33, 34, 4, 37, 7, 47, 49, 7, 9, 7, 9, 9, 35, 18, 21, 4}, new short[]{97, 98, 4, 101, 135, 111, 113, 135, 142, 135, 10, 135, 99, 18, 21, 2}, new short[]{33, 34, 4, 37, 39, 47, 49, 39, 11, 39, 11, 11, 35, 18, 21, 2}, new short[]{97, 98, 100, 5, 135, 111, 113, 135, 142, 135, 12, 135, 99, 114, 117, 3}, new short[]{97, 98, 6, 6, 136, 112, 113, 136, 136, 136, 13, 136, 99, 18, 21, 3}, new short[]{33, 34, 132, 37, 7, 47, 49, 7, 14, 7, 14, 14, 35, 146, 149, 4}, new short[]{33, 34, 36, 37, 39, 15, 49, 39, 41, 39, 15, 39, 35, 50, 53, 5}, new short[]{33, 34, 38, 38, 40, 16, 49, 40, 40, 40, 16, 40, 35, 50, 53, 5}, new short[]{33, 34, 36, 37, 39, 47, 17, 39, 41, 39, 17, 39, 35, 50, 53, 6}, new short[]{33, 34, 18, 37, 39, 47, 49, 83, 20, 83, 18, 18, 35, 18, 21, 0}, new short[]{97, 98, 18, 101, 135, 111, 113, 135, 142, 135, 19, 135, 99, 18, 21, 0}, new short[]{33, 34, 18, 37, 39, 47, 49, 39, 20, 39, 20, 20, 35, 18, 21, 0}, new short[]{33, 34, 21, 37, 39, 47, 49, 86, 23, 86, 21, 21, 35, 18, 21, 3}, new short[]{97, 98, 21, 101, 135, 111, 113, 135, 142, 135, 22, 135, 99, 18, 21, 3}, new short[]{33, 34, 21, 37, 39, 47, 49, 39, 23, 39, 23, 23, 35, 18, 21, 3}};
    private static final byte[][] impTabR_DEFAULT = {new byte[]{1, 0, 2, 2, 0, 0, 0, 0}, new byte[]{1, 0, 1, 3, 20, 20, 0, 1}, new byte[]{1, 0, 2, 2, 0, 0, 0, 1}, new byte[]{1, 0, 1, 3, 5, 5, 0, 1}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 0, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 3, 4, 4, 0, 0}, new byte[]{1, 0, 1, 3, 5, 5, 0, 0}};
    private static final byte[][] impTabR_GROUP_NUMBERS_WITH_R = {new byte[]{2, 0, 1, 1, 0, 0, 0, 0}, new byte[]{2, 0, 1, 1, 0, 0, 0, 1}, new byte[]{2, 0, 20, 20, 19, 0, 0, 1}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_HDR, 0, 4, 4, 3, 0, 0, 0}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_HDR, 0, 4, 4, 3, 0, 0, 1}};
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT = {new byte[]{1, 0, 2, 2, 0, 0, 0, 0}, new byte[]{1, 0, 1, 2, 19, 19, 0, 1}, new byte[]{1, 0, 2, 2, 0, 0, 0, 1}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 48, 6, 4, 3, 3, 48, 0}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 48, 6, 4, 5, 5, 48, 3}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 48, 6, 4, 5, 5, 48, 2}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 48, 6, 4, 3, 3, 48, 1}};
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS = {new byte[]{19, 0, 1, 1, 0, 0, 0, 0}, new byte[]{35, 0, 1, 1, 2, 64, 0, 1}, new byte[]{35, 0, 1, 1, 2, 64, 0, 0}, new byte[]{3, 0, 3, 54, 20, 64, 0, 1}, new byte[]{83, 64, 5, 54, 4, 64, 64, 0}, new byte[]{83, 64, 5, 54, 4, 64, 64, 1}, new byte[]{83, 64, 6, 6, 4, 64, 64, 3}};
    private static final byte[][] impTabR_INVERSE_NUMBERS_AS_L = {new byte[]{1, 0, 1, 1, 0, 0, 0, 0}, new byte[]{1, 0, 1, 1, 20, 20, 0, 1}, new byte[]{1, 0, 1, 1, 0, 0, 0, 1}, new byte[]{1, 0, 1, 1, 5, 5, 0, 1}, new byte[]{CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 0, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, CameraMetadataEx.HUAWEI_EXT_SCENE_MODE_SMART, 4, 4, 0, 0}, new byte[]{1, 0, 1, 1, 5, 5, 0, 0}};
    private static final ImpTabPair impTab_DEFAULT;
    private static final ImpTabPair impTab_GROUP_NUMBERS_WITH_R;
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL = new ImpTabPair(impTabL_NUMBERS_SPECIAL, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = new ImpTabPair(impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS, impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct2, impAct3);
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT = new ImpTabPair(impTabL_DEFAULT, impTabR_INVERSE_LIKE_DIRECT, impAct0, impAct1);
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT_WITH_MARKS = new ImpTabPair(impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS, impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS, impAct2, impAct3);
    private static final ImpTabPair impTab_INVERSE_NUMBERS_AS_L;
    private static final ImpTabPair impTab_NUMBERS_SPECIAL;
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

    static final byte DirFromStrong(byte b) {
        return b == 0 ? (byte) 0 : 1;
    }

    static int DirPropFlag(byte b) {
        return 1 << b;
    }

    private static short GetAction(byte b) {
        return (short) (b >> 4);
    }

    private static short GetActionProps(short s) {
        return (short) (s >> 5);
    }

    static byte GetLRFromLevel(byte b) {
        return (byte) (b & 1);
    }

    private static short GetState(byte b) {
        return (short) (b & 15);
    }

    private static short GetStateProps(short s) {
        return (short) (s & 31);
    }

    static boolean IsBidiControlChar(int i) {
        return (i & -4) == 8204 || (i >= 8234 && i <= 8238) || (i >= 8294 && i <= 8297);
    }

    static boolean IsDefaultLevel(byte b) {
        return (b & LEVEL_DEFAULT_LTR) == 126;
    }

    static final byte NoOverride(byte b) {
        return (byte) (b & LEVEL_DEFAULT_RTL);
    }

    /* access modifiers changed from: package-private */
    public int Bidi_Abs(int i) {
        return i >= 0 ? i : -i;
    }

    /* access modifiers changed from: package-private */
    public int Bidi_Min(int i, int i2) {
        return i < i2 ? i : i2;
    }

    /* access modifiers changed from: package-private */
    public static class Point {
        int flag;
        int pos;

        Point() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class InsertPoints {
        int confirmed;
        Point[] points = new Point[0];
        int size;

        InsertPoints() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class Opening {
        byte contextDir;
        int contextPos;
        short flags;
        int match;
        int position;

        Opening() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class IsoRun {
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

    /* access modifiers changed from: package-private */
    public static class BracketData {
        boolean isNumbersSpecial;
        int isoRunLast;
        IsoRun[] isoRuns = new IsoRun[Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT];
        Opening[] openings = new Opening[20];

        BracketData() {
        }
    }

    /* access modifiers changed from: package-private */
    public static class Isolate {
        int start1;
        int startON;
        short state;
        short stateImp;

        Isolate() {
        }
    }

    static {
        byte[][] bArr = impTabL_DEFAULT;
        byte[][] bArr2 = impTabR_DEFAULT;
        short[] sArr = impAct0;
        impTab_DEFAULT = new ImpTabPair(bArr, bArr2, sArr, sArr);
        byte[][] bArr3 = impTabL_NUMBERS_SPECIAL;
        byte[][] bArr4 = impTabR_DEFAULT;
        short[] sArr2 = impAct0;
        impTab_NUMBERS_SPECIAL = new ImpTabPair(bArr3, bArr4, sArr2, sArr2);
        byte[][] bArr5 = impTabL_GROUP_NUMBERS_WITH_R;
        byte[][] bArr6 = impTabR_GROUP_NUMBERS_WITH_R;
        short[] sArr3 = impAct0;
        impTab_GROUP_NUMBERS_WITH_R = new ImpTabPair(bArr5, bArr6, sArr3, sArr3);
        byte[][] bArr7 = impTabL_INVERSE_NUMBERS_AS_L;
        byte[][] bArr8 = impTabR_INVERSE_NUMBERS_AS_L;
        short[] sArr4 = impAct0;
        impTab_INVERSE_NUMBERS_AS_L = new ImpTabPair(bArr7, bArr8, sArr4, sArr4);
    }

    /* access modifiers changed from: package-private */
    public boolean testDirPropFlagAt(int i, int i2) {
        return (DirPropFlag(this.dirProps[i2]) & i) != 0;
    }

    static final int DirPropFlagLR(byte b) {
        return DirPropFlagLR[b & 1];
    }

    static final int DirPropFlagE(byte b) {
        return DirPropFlagE[b & 1];
    }

    static final int DirPropFlagO(byte b) {
        return DirPropFlagO[b & 1];
    }

    /* access modifiers changed from: package-private */
    public void verifyValidPara() {
        if (this != this.paraBidi) {
            throw new IllegalStateException();
        }
    }

    /* access modifiers changed from: package-private */
    public void verifyValidParaOrLine() {
        Bidi bidi = this.paraBidi;
        if (this != bidi) {
            if (bidi == null || bidi != bidi.paraBidi) {
                throw new IllegalStateException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void verifyRange(int i, int i2, int i3) {
        if (i < i2 || i >= i3) {
            throw new IllegalArgumentException("Value " + i + " is out of range " + i2 + " to " + i3);
        }
    }

    public Bidi() {
        this(0, 0);
    }

    public Bidi(int i, int i2) {
        this.dirPropsMemory = new byte[1];
        this.levelsMemory = new byte[1];
        this.paras_limit = new int[10];
        this.paras_level = new byte[10];
        this.runsMemory = new BidiRun[0];
        this.simpleRuns = new BidiRun[]{new BidiRun()};
        this.customClassifier = null;
        this.insertPoints = new InsertPoints();
        if (i < 0 || i2 < 0) {
            throw new IllegalArgumentException();
        }
        this.bdp = UBiDiProps.INSTANCE;
        if (i > 0) {
            getInitialDirPropsMemory(i);
            getInitialLevelsMemory(i);
        } else {
            this.mayAllocateText = true;
        }
        if (i2 <= 0) {
            this.mayAllocateRuns = true;
        } else if (i2 > 1) {
            getInitialRunsMemory(i2);
        }
    }

    private Object getMemory(String str, Object obj, Class<?> cls, boolean z, int i) {
        int length2 = Array.getLength(obj);
        if (i == length2) {
            return obj;
        }
        if (z) {
            try {
                return Array.newInstance(cls, i);
            } catch (Exception unused) {
                throw new OutOfMemoryError("Failed to allocate memory for " + str);
            }
        } else if (i <= length2) {
            return obj;
        } else {
            throw new OutOfMemoryError("Failed to allocate memory for " + str);
        }
    }

    private void getDirPropsMemory(boolean z, int i) {
        this.dirPropsMemory = (byte[]) getMemory("DirProps", this.dirPropsMemory, Byte.TYPE, z, i);
    }

    /* access modifiers changed from: package-private */
    public void getDirPropsMemory(int i) {
        getDirPropsMemory(this.mayAllocateText, i);
    }

    private void getLevelsMemory(boolean z, int i) {
        this.levelsMemory = (byte[]) getMemory("Levels", this.levelsMemory, Byte.TYPE, z, i);
    }

    /* access modifiers changed from: package-private */
    public void getLevelsMemory(int i) {
        getLevelsMemory(this.mayAllocateText, i);
    }

    private void getRunsMemory(boolean z, int i) {
        this.runsMemory = (BidiRun[]) getMemory("Runs", this.runsMemory, BidiRun.class, z, i);
    }

    /* access modifiers changed from: package-private */
    public void getRunsMemory(int i) {
        getRunsMemory(this.mayAllocateRuns, i);
    }

    private void getInitialDirPropsMemory(int i) {
        getDirPropsMemory(true, i);
    }

    private void getInitialLevelsMemory(int i) {
        getLevelsMemory(true, i);
    }

    private void getInitialRunsMemory(int i) {
        getRunsMemory(true, i);
    }

    public void setInverse(boolean z) {
        this.isInverse = z;
        this.reorderingMode = z ? 4 : 0;
    }

    public boolean isInverse() {
        return this.isInverse;
    }

    public void setReorderingMode(int i) {
        if (i >= 0 && i < 7) {
            this.reorderingMode = i;
            this.isInverse = i == 4;
        }
    }

    public int getReorderingMode() {
        return this.reorderingMode;
    }

    public void setReorderingOptions(int i) {
        if ((i & 2) != 0) {
            this.reorderingOptions = i & -2;
        } else {
            this.reorderingOptions = i;
        }
    }

    public int getReorderingOptions() {
        return this.reorderingOptions;
    }

    public static byte getBaseDirection(CharSequence charSequence) {
        if (!(charSequence == null || charSequence.length() == 0)) {
            int length2 = charSequence.length();
            int i = 0;
            while (i < length2) {
                byte directionality = UCharacter.getDirectionality(UCharacter.codePointAt(charSequence, i));
                if (directionality == 0) {
                    return 0;
                }
                if (directionality == 1 || directionality == 13) {
                    return 1;
                }
                i = UCharacter.offsetByCodePoints(charSequence, i, 1);
            }
        }
        return 3;
    }

    private byte firstL_R_AL() {
        int i = 0;
        while (true) {
            byte b = 10;
            while (true) {
                if (i >= this.prologue.length()) {
                    return b;
                }
                int codePointAt = this.prologue.codePointAt(i);
                i += Character.charCount(codePointAt);
                byte customizedClass = (byte) getCustomizedClass(codePointAt);
                if (b == 10) {
                    if (customizedClass == 0 || customizedClass == 1 || customizedClass == 13) {
                        b = customizedClass;
                    }
                } else if (customizedClass == 7) {
                }
            }
        }
    }

    private void checkParaCount() {
        int i = this.paraCount;
        byte[] bArr = this.paras_level;
        if (i > bArr.length) {
            int length2 = bArr.length;
            int[] iArr = this.paras_limit;
            int i2 = i * 2;
            try {
                this.paras_limit = new int[i2];
                this.paras_level = new byte[i2];
                System.arraycopy(iArr, 0, this.paras_limit, 0, length2);
                System.arraycopy(bArr, 0, this.paras_level, 0, length2);
            } catch (Exception unused) {
                throw new OutOfMemoryError("Failed to allocate memory for paras");
            }
        }
    }

    private void getDirProps() {
        byte b;
        byte b2;
        byte b3;
        byte b4;
        int i;
        byte b5;
        int i2;
        byte firstL_R_AL;
        int i3;
        int i4 = 0;
        this.flags = 0;
        boolean IsDefaultLevel = IsDefaultLevel(this.paraLevel);
        boolean z = IsDefaultLevel && ((i3 = this.reorderingMode) == 5 || i3 == 6);
        this.lastArabicPos = -1;
        boolean z2 = (this.reorderingOptions & 2) != 0;
        int[] iArr = new int[DIRECTION_DEFAULT_LEFT_TO_RIGHT];
        byte[] bArr = new byte[DIRECTION_DEFAULT_LEFT_TO_RIGHT];
        if ((this.reorderingOptions & 4) != 0) {
            this.length = 0;
        }
        byte b6 = this.paraLevel;
        byte b7 = (byte) (b6 & 1);
        if (IsDefaultLevel) {
            this.paras_level[0] = b7;
            if (this.prologue == null || (firstL_R_AL = firstL_R_AL()) == 10) {
                b2 = 1;
            } else {
                if (firstL_R_AL == 0) {
                    this.paras_level[0] = 0;
                } else {
                    this.paras_level[0] = 1;
                }
                b2 = 0;
            }
            b = b7;
        } else {
            this.paras_level[0] = b6;
            b2 = 0;
            b = 10;
        }
        int i5 = 0;
        byte b8 = b2;
        byte b9 = b;
        int i6 = 0;
        int i7 = -1;
        while (true) {
            int i8 = this.originalLength;
            if (i6 >= i8) {
                break;
            }
            int charAt = UTF16.charAt(this.text, i4, i8, i6);
            int charCount = UTF16.getCharCount(charAt) + i6;
            int i9 = charCount - 1;
            byte customizedClass = (byte) getCustomizedClass(charAt);
            this.flags |= DirPropFlag(customizedClass);
            this.dirProps[i9] = customizedClass;
            if (i9 > i6) {
                this.flags |= DirPropFlag((byte) 18);
                int i10 = i9;
                b4 = b7;
                do {
                    i = -1;
                    i10--;
                    this.dirProps[i10] = 18;
                } while (i10 > i6);
            } else {
                b4 = b7;
                i = -1;
            }
            if (z2 && IsBidiControlChar(charAt)) {
                i5++;
            }
            if (customizedClass == 0) {
                if (b8 == 1) {
                    this.paras_level[this.paraCount - 1] = 0;
                    b8 = 0;
                } else if (b8 == 2) {
                    if (i7 <= 125) {
                        this.flags |= DirPropFlag((byte) 20);
                    }
                    b8 = 3;
                }
                i6 = charCount;
                b7 = b4;
                i4 = 0;
                b9 = 0;
            } else {
                byte b10 = 1;
                if (customizedClass != 1) {
                    if (customizedClass == 13) {
                        b10 = 1;
                    } else if (customizedClass < 19 || customizedClass > 21) {
                        if (customizedClass == 22) {
                            if (b8 == 2) {
                                i2 = 125;
                                if (i7 <= 125) {
                                    this.flags |= DirPropFlag((byte) 20);
                                }
                            } else {
                                i2 = 125;
                            }
                            if (i7 >= 0) {
                                if (i7 <= i2) {
                                    b8 = bArr[i7];
                                }
                                i7--;
                                i6 = charCount;
                                b7 = b4;
                                i4 = 0;
                            }
                        } else if (customizedClass == 7) {
                            if (charCount < this.originalLength && charAt == 13) {
                                if (this.text[charCount] == '\n') {
                                    b5 = b9;
                                    b9 = b5;
                                    i6 = charCount;
                                    b7 = b4;
                                    i4 = 0;
                                }
                            }
                            int[] iArr2 = this.paras_limit;
                            int i11 = this.paraCount;
                            iArr2[i11 - 1] = charCount;
                            b5 = b9;
                            if (z && b5 == 1) {
                                this.paras_level[i11 - 1] = 1;
                            }
                            if ((this.reorderingOptions & 4) != 0) {
                                this.length = charCount;
                                this.controlCount = i5;
                            }
                            if (charCount < this.originalLength) {
                                this.paraCount++;
                                checkParaCount();
                                if (IsDefaultLevel) {
                                    this.paras_level[this.paraCount - 1] = b4;
                                    b8 = 1;
                                    b9 = b4;
                                } else {
                                    this.paras_level[this.paraCount - 1] = this.paraLevel;
                                    b9 = b5;
                                    b8 = 0;
                                }
                                i6 = charCount;
                                i7 = i;
                                b7 = b4;
                                i4 = 0;
                            }
                            b9 = b5;
                            i6 = charCount;
                            b7 = b4;
                            i4 = 0;
                        }
                        b5 = b9;
                        b9 = b5;
                        i6 = charCount;
                        b7 = b4;
                        i4 = 0;
                    } else {
                        i7++;
                        if (i7 <= 125) {
                            iArr[i7] = i9;
                            bArr[i7] = b8;
                        }
                        if (customizedClass == 19) {
                            this.dirProps[i9] = 20;
                            i6 = charCount;
                            b7 = b4;
                            i4 = 0;
                            b8 = 2;
                        } else {
                            i6 = charCount;
                            b7 = b4;
                            i4 = 0;
                            b8 = 3;
                        }
                    }
                }
                if (b8 == b10) {
                    this.paras_level[this.paraCount - b10] = b10;
                    b8 = 0;
                } else if (b8 == 2) {
                    if (i7 <= 125) {
                        this.dirProps[iArr[i7]] = 21;
                        this.flags = DirPropFlag((byte) 21) | this.flags;
                    }
                    b8 = 3;
                }
                if (customizedClass == 13) {
                    this.lastArabicPos = i9;
                }
                i6 = charCount;
                b7 = b4;
                i4 = 0;
                b9 = 1;
            }
        }
        int i12 = 125;
        if (i7 > 125) {
            b8 = 2;
        } else {
            i12 = i7;
        }
        while (true) {
            if (i12 < 0) {
                break;
            } else if (b8 == 2) {
                this.flags |= DirPropFlag((byte) 20);
                break;
            } else {
                b8 = bArr[i12];
                i12--;
            }
        }
        if ((this.reorderingOptions & 4) == 0) {
            b3 = 1;
            this.paras_limit[this.paraCount - 1] = this.originalLength;
            this.controlCount = i5;
        } else if (this.length < this.originalLength) {
            b3 = 1;
            this.paraCount--;
        } else {
            b3 = 1;
        }
        if (z && b9 == b3) {
            this.paras_level[this.paraCount - b3] = b3;
        }
        if (IsDefaultLevel) {
            this.paraLevel = this.paras_level[0];
        }
        for (int i13 = 0; i13 < this.paraCount; i13++) {
            this.flags |= DirPropFlagLR(this.paras_level[i13]);
        }
        if (this.orderParagraphsLTR && (this.flags & DirPropFlag((byte) 7)) != 0) {
            this.flags |= DirPropFlag((byte) 0);
        }
    }

    /* access modifiers changed from: package-private */
    public byte GetParaLevelAt(int i) {
        if (this.defaultParaLevel == 0 || i < this.paras_limit[0]) {
            return this.paraLevel;
        }
        int i2 = 1;
        while (i2 < this.paraCount && i >= this.paras_limit[i2]) {
            i2++;
        }
        int i3 = this.paraCount;
        if (i2 >= i3) {
            i2 = i3 - 1;
        }
        return this.paras_level[i2];
    }

    private void bracketInit(BracketData bracketData) {
        boolean z = false;
        bracketData.isoRunLast = 0;
        bracketData.isoRuns[0] = new IsoRun();
        bracketData.isoRuns[0].start = 0;
        bracketData.isoRuns[0].limit = 0;
        bracketData.isoRuns[0].level = GetParaLevelAt(0);
        IsoRun isoRun = bracketData.isoRuns[0];
        IsoRun isoRun2 = bracketData.isoRuns[0];
        IsoRun isoRun3 = bracketData.isoRuns[0];
        byte GetParaLevelAt = (byte) (GetParaLevelAt(0) & 1);
        isoRun3.contextDir = GetParaLevelAt;
        isoRun2.lastBase = GetParaLevelAt;
        isoRun.lastStrong = GetParaLevelAt;
        bracketData.isoRuns[0].contextPos = 0;
        bracketData.openings = new Opening[20];
        int i = this.reorderingMode;
        if (i == 1 || i == 6) {
            z = true;
        }
        bracketData.isNumbersSpecial = z;
    }

    private void bracketProcessB(BracketData bracketData, byte b) {
        bracketData.isoRunLast = 0;
        bracketData.isoRuns[0].limit = 0;
        bracketData.isoRuns[0].level = b;
        IsoRun isoRun = bracketData.isoRuns[0];
        IsoRun isoRun2 = bracketData.isoRuns[0];
        byte b2 = (byte) (b & 1);
        bracketData.isoRuns[0].contextDir = b2;
        isoRun2.lastBase = b2;
        isoRun.lastStrong = b2;
        bracketData.isoRuns[0].contextPos = 0;
    }

    private void bracketProcessBoundary(BracketData bracketData, int i, byte b, byte b2) {
        IsoRun isoRun = bracketData.isoRuns[bracketData.isoRunLast];
        if ((DirPropFlag(this.dirProps[i]) & MASK_ISO) == 0) {
            if (NoOverride(b2) > NoOverride(b)) {
                b = b2;
            }
            isoRun.limit = isoRun.start;
            isoRun.level = b2;
            byte b3 = (byte) (b & 1);
            isoRun.contextDir = b3;
            isoRun.lastBase = b3;
            isoRun.lastStrong = b3;
            isoRun.contextPos = i;
        }
    }

    private void bracketProcessLRI_RLI(BracketData bracketData, byte b) {
        IsoRun isoRun = bracketData.isoRuns[bracketData.isoRunLast];
        isoRun.lastBase = 10;
        short s = isoRun.limit;
        bracketData.isoRunLast++;
        IsoRun isoRun2 = bracketData.isoRuns[bracketData.isoRunLast];
        if (isoRun2 == null) {
            IsoRun[] isoRunArr = bracketData.isoRuns;
            int i = bracketData.isoRunLast;
            IsoRun isoRun3 = new IsoRun();
            isoRunArr[i] = isoRun3;
            isoRun2 = isoRun3;
        }
        isoRun2.limit = s;
        isoRun2.start = s;
        isoRun2.level = b;
        byte b2 = (byte) (b & 1);
        isoRun2.contextDir = b2;
        isoRun2.lastBase = b2;
        isoRun2.lastStrong = b2;
        isoRun2.contextPos = 0;
    }

    private void bracketProcessPDI(BracketData bracketData) {
        bracketData.isoRunLast--;
        bracketData.isoRuns[bracketData.isoRunLast].lastBase = 10;
    }

    private void bracketAddOpening(BracketData bracketData, char c, int i) {
        IsoRun isoRun = bracketData.isoRuns[bracketData.isoRunLast];
        if (isoRun.limit >= bracketData.openings.length) {
            Opening[] openingArr = bracketData.openings;
            try {
                int length2 = bracketData.openings.length;
                bracketData.openings = new Opening[(length2 * 2)];
                System.arraycopy(openingArr, 0, bracketData.openings, 0, length2);
            } catch (Exception unused) {
                throw new OutOfMemoryError("Failed to allocate memory for openings");
            }
        }
        Opening opening = bracketData.openings[isoRun.limit];
        if (opening == null) {
            Opening[] openingArr2 = bracketData.openings;
            short s = isoRun.limit;
            Opening opening2 = new Opening();
            openingArr2[s] = opening2;
            opening = opening2;
        }
        opening.position = i;
        opening.match = c;
        opening.contextDir = isoRun.contextDir;
        opening.contextPos = isoRun.contextPos;
        opening.flags = 0;
        isoRun.limit = (short) (isoRun.limit + 1);
    }

    private void fixN0c(BracketData bracketData, int i, int i2, byte b) {
        IsoRun isoRun = bracketData.isoRuns[bracketData.isoRunLast];
        while (true) {
            i++;
            if (i < isoRun.limit) {
                Opening opening = bracketData.openings[i];
                if (opening.match < 0) {
                    if (i2 >= opening.contextPos) {
                        if (i2 >= opening.position) {
                            continue;
                        } else if (b != opening.contextDir) {
                            int i3 = opening.position;
                            this.dirProps[i3] = b;
                            int i4 = -opening.match;
                            this.dirProps[i4] = b;
                            opening.match = 0;
                            fixN0c(bracketData, i, i3, b);
                            fixN0c(bracketData, i, i4, b);
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }
        }
    }

    private byte bracketProcessClosing(BracketData bracketData, int i, int i2) {
        boolean z;
        IsoRun isoRun = bracketData.isoRuns[bracketData.isoRunLast];
        Opening opening = bracketData.openings[i];
        byte b = (byte) (isoRun.level & 1);
        if ((b == 0 && (opening.flags & FOUND_L) > 0) || (b == 1 && (opening.flags & FOUND_R) > 0)) {
            z = true;
        } else if ((opening.flags & (FOUND_L | FOUND_R)) != 0) {
            z = i == isoRun.start;
            if (b != opening.contextDir) {
                b = opening.contextDir;
            }
        } else {
            isoRun.limit = (short) i;
            return 10;
        }
        this.dirProps[opening.position] = b;
        this.dirProps[i2] = b;
        fixN0c(bracketData, i, opening.position, b);
        if (z) {
            isoRun.limit = (short) i;
            while (isoRun.limit > isoRun.start && bracketData.openings[isoRun.limit - 1].position == opening.position) {
                isoRun.limit = (short) (isoRun.limit - 1);
            }
        } else {
            opening.match = -i2;
            int i3 = i - 1;
            while (i3 >= isoRun.start && bracketData.openings[i3].position == opening.position) {
                bracketData.openings[i3].match = 0;
                i3--;
            }
            for (int i4 = i + 1; i4 < isoRun.limit; i4++) {
                Opening opening2 = bracketData.openings[i4];
                if (opening2.position >= i2) {
                    break;
                }
                if (opening2.match > 0) {
                    opening2.match = 0;
                }
            }
        }
        return b;
    }

    /* JADX WARNING: Removed duplicated region for block: B:67:0x0114 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0125  */
    private void bracketProcessChar(BracketData bracketData, int i) {
        int i2;
        IsoRun isoRun = bracketData.isoRuns[bracketData.isoRunLast];
        byte b = this.dirProps[i];
        byte b2 = 0;
        if (b == 10) {
            char c = this.text[i];
            int i3 = isoRun.limit - 1;
            while (true) {
                if (i3 < isoRun.start) {
                    break;
                } else if (bracketData.openings[i3].match != c) {
                    i3--;
                } else {
                    byte bracketProcessClosing = bracketProcessClosing(bracketData, i3, i);
                    if (bracketProcessClosing == 10) {
                        c = 0;
                    } else {
                        isoRun.lastBase = 10;
                        isoRun.contextDir = bracketProcessClosing;
                        isoRun.contextPos = i;
                        byte b3 = this.levels[i];
                        if ((b3 & LEVEL_OVERRIDE) != 0) {
                            byte b4 = (byte) (b3 & 1);
                            isoRun.lastStrong = b4;
                            short DirPropFlag = (short) DirPropFlag(b4);
                            for (int i4 = isoRun.start; i4 < i3; i4++) {
                                Opening opening = bracketData.openings[i4];
                                opening.flags = (short) (opening.flags | DirPropFlag);
                            }
                            byte[] bArr = this.levels;
                            bArr[i] = (byte) (bArr[i] & LEVEL_DEFAULT_RTL);
                        }
                        byte[] bArr2 = this.levels;
                        int i5 = bracketData.openings[i3].position;
                        bArr2[i5] = (byte) (bArr2[i5] & LEVEL_DEFAULT_RTL);
                        return;
                    }
                }
            }
            char bidiPairedBracket = c != 0 ? (char) UCharacter.getBidiPairedBracket(c) : 0;
            if (bidiPairedBracket != c && UCharacter.getIntPropertyValue(c, 4117) == 1) {
                if (bidiPairedBracket == 9002) {
                    bracketAddOpening(bracketData, 12297, i);
                } else if (bidiPairedBracket == 12297) {
                    bracketAddOpening(bracketData, 9002, i);
                }
                bracketAddOpening(bracketData, bidiPairedBracket, i);
            }
        }
        byte b5 = this.levels[i];
        if ((b5 & LEVEL_OVERRIDE) != 0) {
            b2 = (byte) (b5 & 1);
            if (!(b == 8 || b == 9 || b == 10)) {
                this.dirProps[i] = b2;
            }
            isoRun.lastBase = b2;
            isoRun.lastStrong = b2;
            isoRun.contextDir = b2;
            isoRun.contextPos = i;
        } else if (b <= 1 || b == 13) {
            byte DirFromStrong = DirFromStrong(b);
            isoRun.lastBase = b;
            isoRun.lastStrong = b;
            isoRun.contextDir = DirFromStrong;
            isoRun.contextPos = i;
            b = DirFromStrong;
            if (b > 1 || b == 13) {
                short DirPropFlag2 = (short) DirPropFlag(DirFromStrong(b));
                for (i2 = isoRun.start; i2 < isoRun.limit; i2++) {
                    if (i > bracketData.openings[i2].position) {
                        Opening opening2 = bracketData.openings[i2];
                        opening2.flags = (short) (opening2.flags | DirPropFlag2);
                    }
                }
            }
            return;
        } else {
            if (b == 2) {
                isoRun.lastBase = 2;
                if (isoRun.lastStrong == 0) {
                    if (!bracketData.isNumbersSpecial) {
                        this.dirProps[i] = 23;
                    }
                    isoRun.contextDir = 0;
                    isoRun.contextPos = i;
                } else {
                    if (isoRun.lastStrong == 13) {
                        this.dirProps[i] = 5;
                    } else {
                        this.dirProps[i] = 24;
                    }
                    isoRun.contextDir = 1;
                    isoRun.contextPos = i;
                }
            } else if (b == 5) {
                isoRun.lastBase = 5;
                isoRun.contextDir = 1;
                isoRun.contextPos = i;
            } else {
                if (b == 17) {
                    b = isoRun.lastBase;
                    if (b == 10) {
                        this.dirProps[i] = b;
                    }
                } else {
                    isoRun.lastBase = b;
                }
                if (b > 1) {
                }
                short DirPropFlag22 = (short) DirPropFlag(DirFromStrong(b));
                while (i2 < isoRun.limit) {
                }
            }
            b = 1;
            if (b > 1) {
            }
            short DirPropFlag222 = (short) DirPropFlag(DirFromStrong(b));
            while (i2 < isoRun.limit) {
            }
        }
        b = b2;
        if (b > 1) {
        }
        short DirPropFlag2222 = (short) DirPropFlag(DirFromStrong(b));
        while (i2 < isoRun.limit) {
        }
    }

    private byte directionFromFlags() {
        int i = this.flags;
        if ((MASK_RTL & i) == 0 && ((i & DirPropFlag((byte) 5)) == 0 || (this.flags & MASK_POSSIBLE_N) == 0)) {
            return 0;
        }
        return (this.flags & MASK_LTR) == 0 ? (byte) 1 : 2;
    }

    private byte resolveExplicitLevels() {
        char c;
        byte b;
        int i;
        byte GetParaLevelAt = GetParaLevelAt(0);
        this.isolateCount = 0;
        byte directionFromFlags = directionFromFlags();
        if (directionFromFlags != 2) {
            return directionFromFlags;
        }
        if (this.reorderingMode > 1) {
            for (int i2 = 0; i2 < this.paraCount; i2++) {
                int i3 = i2 == 0 ? 0 : this.paras_limit[i2 - 1];
                int i4 = this.paras_limit[i2];
                byte b2 = this.paras_level[i2];
                while (i3 < i4) {
                    this.levels[i3] = b2;
                    i3++;
                }
            }
            return directionFromFlags;
        }
        byte b3 = 10;
        if ((this.flags & (MASK_EXPLICIT | MASK_ISO)) == 0) {
            BracketData bracketData = new BracketData();
            bracketInit(bracketData);
            for (int i5 = 0; i5 < this.paraCount; i5++) {
                int i6 = i5 == 0 ? 0 : this.paras_limit[i5 - 1];
                int i7 = this.paras_limit[i5];
                byte b4 = this.paras_level[i5];
                while (i6 < i7) {
                    this.levels[i6] = b4;
                    byte b5 = this.dirProps[i6];
                    if (b5 != 18) {
                        if (b5 == 7) {
                            int i8 = i6 + 1;
                            if (i8 < this.length) {
                                char[] cArr = this.text;
                                if (cArr[i6] != '\r' || cArr[i8] != '\n') {
                                    bracketProcessB(bracketData, b4);
                                }
                            }
                        } else {
                            bracketProcessChar(bracketData, i6);
                        }
                    }
                    i6++;
                }
            }
            return directionFromFlags;
        }
        short[] sArr = new short[DIRECTION_DEFAULT_RIGHT_TO_LEFT];
        BracketData bracketData2 = new BracketData();
        bracketInit(bracketData2);
        sArr[0] = (short) GetParaLevelAt;
        this.flags = 0;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        int i13 = 0;
        byte b6 = GetParaLevelAt;
        byte b7 = b6;
        int i14 = 0;
        while (i14 < this.length) {
            byte b8 = this.dirProps[i14];
            switch (b8) {
                case 7:
                    this.flags |= DirPropFlag((byte) 7);
                    this.levels[i14] = GetParaLevelAt(i14);
                    int i15 = i14 + 1;
                    if (i15 < this.length) {
                        char[] cArr2 = this.text;
                        char c2 = cArr2[i14];
                        c = CR;
                        if (c2 == '\r') {
                            b = 10;
                            if (cArr2[i15] == '\n') {
                                i14++;
                                b3 = b;
                            }
                        } else {
                            b = 10;
                        }
                        byte GetParaLevelAt2 = GetParaLevelAt(i15);
                        sArr[0] = (short) GetParaLevelAt2;
                        bracketProcessB(bracketData2, GetParaLevelAt2);
                        b6 = GetParaLevelAt2;
                        b7 = b6;
                        i9 = 0;
                        i11 = 0;
                        i12 = 0;
                        i13 = 0;
                        continue;
                        i14++;
                        b3 = b;
                    }
                    b = 10;
                    break;
                case 8:
                case 9:
                case 10:
                case 13:
                case 17:
                case 19:
                default:
                    b = b3;
                    c = CR;
                    if (NoOverride(b6) != NoOverride(b7)) {
                        bracketProcessBoundary(bracketData2, i10, b7, b6);
                        this.flags |= DirPropFlagMultiRuns;
                        if ((b6 & LEVEL_OVERRIDE) != 0) {
                            this.flags |= DirPropFlagO(b6);
                        } else {
                            this.flags |= DirPropFlagE(b6);
                        }
                    }
                    this.levels[i14] = b6;
                    bracketProcessChar(bracketData2, i14);
                    this.flags |= DirPropFlag(this.dirProps[i14]);
                    b7 = b6;
                    continue;
                    i14++;
                    b3 = b;
                case 11:
                case 12:
                case 14:
                case 15:
                    this.flags |= DirPropFlag((byte) 18);
                    this.levels[i14] = b7;
                    byte NoOverride = (byte) ((b8 == 11 || b8 == 12) ? (b6 + 2) & DIRECTION_DEFAULT_LEFT_TO_RIGHT : (NoOverride(b6) + 1) | 1);
                    if (NoOverride > 125 || i9 != 0 || i11 != 0) {
                        if (i9 == 0) {
                            i11++;
                        }
                        b = 10;
                        break;
                    } else {
                        if (b8 == 12 || b8 == 15) {
                            NoOverride = (byte) (NoOverride | LEVEL_OVERRIDE);
                        }
                        i12++;
                        sArr[i12] = (short) NoOverride;
                        i10 = i14;
                        b6 = NoOverride;
                        b = 10;
                    }
                case 16:
                    this.flags |= DirPropFlag((byte) 18);
                    this.levels[i14] = b7;
                    if (i9 <= 0) {
                        if (i11 > 0) {
                            i11--;
                        } else if (i12 > 0 && sArr[i12] < 256) {
                            i12--;
                            b6 = (byte) sArr[i12];
                            i10 = i14;
                        }
                    }
                    b = 10;
                    break;
                case 18:
                    this.levels[i14] = b7;
                    this.flags |= DirPropFlag((byte) 18);
                    b = 10;
                    break;
                case 20:
                case 21:
                    this.flags |= DirPropFlag(b3) | DirPropFlagLR(b6);
                    this.levels[i14] = NoOverride(b6);
                    if (NoOverride(b6) != NoOverride(b7)) {
                        bracketProcessBoundary(bracketData2, i10, b7, b6);
                        this.flags |= DirPropFlagMultiRuns;
                    }
                    if (b8 == 20) {
                        i = (b6 + 2) & DIRECTION_DEFAULT_LEFT_TO_RIGHT;
                    } else {
                        i = (NoOverride(b6) + 1) | 1;
                    }
                    byte b9 = (byte) i;
                    if (b9 > 125 || i9 != 0 || i11 != 0) {
                        this.dirProps[i14] = 9;
                        i9++;
                        b7 = b6;
                        b = 10;
                        break;
                    } else {
                        this.flags = DirPropFlag(b8) | this.flags;
                        int i16 = i13 + 1;
                        if (i16 > this.isolateCount) {
                            this.isolateCount = i16;
                        }
                        i12++;
                        sArr[i12] = (short) (b9 + 256);
                        bracketProcessLRI_RLI(bracketData2, b9);
                        i13 = i16;
                        i10 = i14;
                        b7 = b6;
                        c = CR;
                        b6 = b9;
                        b = 10;
                        continue;
                        i14++;
                        b3 = b;
                    }
                case 22:
                    if (NoOverride(b6) != NoOverride(b7)) {
                        bracketProcessBoundary(bracketData2, i10, b7, b6);
                        this.flags |= DirPropFlagMultiRuns;
                    }
                    if (i9 > 0) {
                        i9--;
                        this.dirProps[i14] = 9;
                    } else if (i13 > 0) {
                        this.flags |= DirPropFlag((byte) 22);
                        while (sArr[i12] < 256) {
                            i12--;
                        }
                        i12--;
                        i13--;
                        bracketProcessPDI(bracketData2);
                        i10 = i14;
                        i11 = 0;
                    } else {
                        this.dirProps[i14] = 9;
                    }
                    byte b10 = (byte) (sArr[i12] & -257);
                    this.flags |= DirPropFlag(b3) | DirPropFlagLR(b10);
                    this.levels[i14] = NoOverride(b10);
                    b6 = b10;
                    b7 = b6;
                    b = b3;
                    break;
            }
            c = CR;
            i14++;
            b3 = b;
        }
        int i17 = this.flags;
        if ((MASK_EMBEDDING & i17) != 0) {
            this.flags = i17 | DirPropFlagLR(this.paraLevel);
        }
        if (this.orderParagraphsLTR && (this.flags & DirPropFlag((byte) 7)) != 0) {
            this.flags |= DirPropFlag((byte) 0);
        }
        return directionFromFlags();
    }

    private byte checkExplicitLevels() {
        int i;
        this.flags = 0;
        this.isolateCount = 0;
        int i2 = 0;
        int i3 = this.paras_limit[0];
        byte b = this.paraLevel;
        int i4 = 0;
        for (int i5 = 0; i5 < this.length; i5++) {
            byte b2 = this.levels[i5];
            byte b3 = this.dirProps[i5];
            if (b3 == 20 || b3 == 21) {
                i4++;
                if (i4 > this.isolateCount) {
                    this.isolateCount = i4;
                }
            } else if (b3 == 22) {
                i4--;
            } else if (b3 == 7) {
                i4 = 0;
            }
            if (this.defaultParaLevel != 0 && i5 == i3 && (i = i2 + 1) < this.paraCount) {
                byte b4 = this.paras_level[i];
                i3 = this.paras_limit[i];
                b = b4;
                i2 = i;
            }
            int i6 = b2 & LEVEL_OVERRIDE;
            byte b5 = (byte) (b2 & LEVEL_DEFAULT_RTL);
            if (b5 < b || 125 < b5) {
                if (b5 != 0) {
                    throw new IllegalArgumentException("level " + ((int) b5) + " out of bounds at " + i5);
                } else if (b3 != 7) {
                    this.levels[i5] = (byte) (b | i6);
                    b5 = b;
                }
            }
            if (i6 != 0) {
                this.flags = DirPropFlagO(b5) | this.flags;
            } else {
                this.flags = DirPropFlagE(b5) | DirPropFlag(b3) | this.flags;
            }
        }
        int i7 = this.flags;
        if ((MASK_EMBEDDING & i7) != 0) {
            this.flags = i7 | DirPropFlagLR(this.paraLevel);
        }
        return directionFromFlags();
    }

    /* access modifiers changed from: private */
    public static class ImpTabPair {
        short[][] impact;
        byte[][][] imptab;

        ImpTabPair(byte[][] bArr, byte[][] bArr2, short[] sArr, short[] sArr2) {
            this.imptab = new byte[][][]{bArr, bArr2};
            this.impact = new short[][]{sArr, sArr2};
        }
    }

    /* access modifiers changed from: private */
    public static class LevState {
        short[] impAct;
        byte[][] impTab;
        int lastStrongRTL;
        byte runLevel;
        int runStart;
        int startL2EN;
        int startON;
        short state;

        private LevState() {
        }
    }

    private void addPoint(int i, int i2) {
        Point point = new Point();
        int length2 = this.insertPoints.points.length;
        if (length2 == 0) {
            this.insertPoints.points = new Point[10];
            length2 = 10;
        }
        if (this.insertPoints.size >= length2) {
            Point[] pointArr = this.insertPoints.points;
            InsertPoints insertPoints2 = this.insertPoints;
            insertPoints2.points = new Point[(length2 * 2)];
            System.arraycopy(pointArr, 0, insertPoints2.points, 0, length2);
        }
        point.pos = i;
        point.flag = i2;
        this.insertPoints.points[this.insertPoints.size] = point;
        this.insertPoints.size++;
    }

    private void setLevelsOutsideIsolates(int i, int i2, byte b) {
        int i3 = 0;
        while (i < i2) {
            byte b2 = this.dirProps[i];
            if (b2 == 22) {
                i3--;
            }
            if (i3 == 0) {
                this.levels[i] = b;
            }
            if (b2 == 20 || b2 == 21) {
                i3++;
            }
            i++;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x01af A[LOOP:7: B:100:0x01af->B:101:0x01b1, LOOP_START, PHI: r13 
      PHI: (r13v2 int) = (r13v1 int), (r13v3 int) binds: [B:99:0x01ad, B:101:0x01b1] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARNING: Removed duplicated region for block: B:102:0x01b8  */
    private void processPropertySeq(LevState levState, short s, int i, int i2) {
        int i3;
        byte[][] bArr = levState.impTab;
        short[] sArr = levState.impAct;
        short s2 = levState.state;
        byte b = bArr[s2][s];
        levState.state = GetState(b);
        short s3 = sArr[GetAction(b)];
        byte b2 = bArr[levState.state][7];
        if (s3 != 0) {
            switch (s3) {
                case 1:
                    levState.startON = i;
                    break;
                case 2:
                    i3 = levState.startON;
                    break;
                case 3:
                    setLevelsOutsideIsolates(levState.startON, i, (byte) (levState.runLevel + 1));
                    break;
                case 4:
                    setLevelsOutsideIsolates(levState.startON, i, (byte) (levState.runLevel + 2));
                    break;
                case 5:
                    if (levState.startL2EN >= 0) {
                        addPoint(levState.startL2EN, 1);
                    }
                    levState.startL2EN = -1;
                    if (this.insertPoints.points.length == 0 || this.insertPoints.size <= this.insertPoints.confirmed) {
                        levState.lastStrongRTL = -1;
                        int i4 = ((bArr[s2][7] & 1) == 0 || levState.startON <= 0) ? i : levState.startON;
                        if (s == 5) {
                            addPoint(i, 1);
                            InsertPoints insertPoints2 = this.insertPoints;
                            insertPoints2.confirmed = insertPoints2.size;
                        }
                        i3 = i4;
                        break;
                    } else {
                        for (int i5 = levState.lastStrongRTL + 1; i5 < i; i5++) {
                            byte[] bArr2 = this.levels;
                            bArr2[i5] = (byte) ((bArr2[i5] - 2) & -2);
                        }
                        InsertPoints insertPoints3 = this.insertPoints;
                        insertPoints3.confirmed = insertPoints3.size;
                        levState.lastStrongRTL = -1;
                        if (s == 5) {
                            addPoint(i, 1);
                            InsertPoints insertPoints4 = this.insertPoints;
                            insertPoints4.confirmed = insertPoints4.size;
                            break;
                        }
                    }
                    break;
                case 6:
                    if (this.insertPoints.points.length > 0) {
                        InsertPoints insertPoints5 = this.insertPoints;
                        insertPoints5.size = insertPoints5.confirmed;
                    }
                    levState.startON = -1;
                    levState.startL2EN = -1;
                    levState.lastStrongRTL = i2 - 1;
                    break;
                case 7:
                    if (s != 3 || this.dirProps[i] != 5 || this.reorderingMode == 6) {
                        if (levState.startL2EN == -1) {
                            levState.startL2EN = i;
                            break;
                        }
                    } else if (levState.startL2EN == -1) {
                        levState.lastStrongRTL = i2 - 1;
                        break;
                    } else {
                        if (levState.startL2EN >= 0) {
                            addPoint(levState.startL2EN, 1);
                            levState.startL2EN = -2;
                        }
                        addPoint(i, 1);
                        break;
                    }
                    break;
                case 8:
                    levState.lastStrongRTL = i2 - 1;
                    levState.startON = -1;
                    break;
                case 9:
                    int i6 = i - 1;
                    while (i6 >= 0 && (this.levels[i6] & 1) == 0) {
                        i6--;
                    }
                    if (i6 >= 0) {
                        addPoint(i6, 4);
                        InsertPoints insertPoints6 = this.insertPoints;
                        insertPoints6.confirmed = insertPoints6.size;
                    }
                    levState.startON = i;
                    break;
                case 10:
                    addPoint(i, 1);
                    addPoint(i, 2);
                    break;
                case 11:
                    InsertPoints insertPoints7 = this.insertPoints;
                    insertPoints7.size = insertPoints7.confirmed;
                    if (s == 5) {
                        addPoint(i, 4);
                        InsertPoints insertPoints8 = this.insertPoints;
                        insertPoints8.confirmed = insertPoints8.size;
                        break;
                    }
                    break;
                case 12:
                    byte b3 = (byte) (levState.runLevel + b2);
                    for (int i7 = levState.startON; i7 < i; i7++) {
                        byte[] bArr3 = this.levels;
                        if (bArr3[i7] < b3) {
                            bArr3[i7] = b3;
                        }
                    }
                    InsertPoints insertPoints9 = this.insertPoints;
                    insertPoints9.confirmed = insertPoints9.size;
                    levState.startON = i;
                    break;
                case 13:
                    byte b4 = levState.runLevel;
                    int i8 = i - 1;
                    while (i8 >= levState.startON) {
                        int i9 = b4 + 3;
                        if (this.levels[i8] == i9) {
                            while (true) {
                                byte[] bArr4 = this.levels;
                                if (bArr4[i8] == i9) {
                                    bArr4[i8] = (byte) (bArr4[i8] - 2);
                                    i8--;
                                } else {
                                    while (this.levels[i8] == b4) {
                                        i8--;
                                    }
                                }
                            }
                        }
                        byte[] bArr5 = this.levels;
                        if (bArr5[i8] == b4 + 2) {
                            bArr5[i8] = b4;
                        } else {
                            bArr5[i8] = (byte) (b4 + 1);
                        }
                        i8--;
                    }
                    break;
                case 14:
                    byte b5 = (byte) (levState.runLevel + 1);
                    for (int i10 = i - 1; i10 >= levState.startON; i10--) {
                        byte[] bArr6 = this.levels;
                        if (bArr6[i10] > b5) {
                            bArr6[i10] = (byte) (bArr6[i10] - 2);
                        }
                    }
                    break;
                default:
                    throw new IllegalStateException("Internal ICU error in processPropertySeq");
            }
            if (b2 == 0 || i3 < i) {
                byte b6 = (byte) (levState.runLevel + b2);
                if (i3 < levState.runStart) {
                    while (i3 < i2) {
                        this.levels[i3] = b6;
                        i3++;
                    }
                    return;
                }
                setLevelsOutsideIsolates(i3, i2, b6);
                return;
            }
            return;
        }
        i3 = i;
        if (b2 == 0) {
        }
        byte b62 = (byte) (levState.runLevel + b2);
        if (i3 < levState.runStart) {
        }
    }

    private byte lastL_R_AL() {
        int length2 = this.prologue.length();
        while (length2 > 0) {
            int codePointBefore = this.prologue.codePointBefore(length2);
            length2 -= Character.charCount(codePointBefore);
            byte customizedClass = (byte) getCustomizedClass(codePointBefore);
            if (customizedClass == 0) {
                return 0;
            }
            if (customizedClass == 1 || customizedClass == 13) {
                return 1;
            }
            if (customizedClass == 7) {
                return 4;
            }
        }
        return 4;
    }

    private byte firstL_R_AL_EN_AN() {
        int i = 0;
        while (i < this.epilogue.length()) {
            int codePointAt = this.epilogue.codePointAt(i);
            i += Character.charCount(codePointAt);
            byte customizedClass = (byte) getCustomizedClass(codePointAt);
            if (customizedClass == 0) {
                return 0;
            }
            if (customizedClass == 1 || customizedClass == 13) {
                return 1;
            }
            if (customizedClass == 2) {
                return 2;
            }
            if (customizedClass == 5) {
                return 3;
            }
        }
        return 4;
    }

    private void resolveImplicitLevels(int i, int i2, short s, short s2) {
        short s3;
        int i3;
        byte firstL_R_AL_EN_AN;
        short s4;
        int i4;
        byte b;
        byte lastL_R_AL;
        int i5;
        LevState levState = new LevState();
        boolean z = i < this.lastArabicPos && (GetParaLevelAt(i) & 1) > 0 && ((i5 = this.reorderingMode) == 5 || i5 == 6);
        levState.startL2EN = -1;
        levState.lastStrongRTL = -1;
        levState.runStart = i;
        levState.runLevel = this.levels[i];
        levState.impTab = this.impTabPair.imptab[levState.runLevel & 1];
        levState.impAct = this.impTabPair.impact[levState.runLevel & 1];
        short s5 = (i != 0 || this.prologue == null || (lastL_R_AL = lastL_R_AL()) == 4) ? s : (short) lastL_R_AL;
        byte[] bArr = this.dirProps;
        if (bArr[i] == 22) {
            levState.startON = this.isolates[this.isolateCount].startON;
            i3 = this.isolates[this.isolateCount].start1;
            short s6 = this.isolates[this.isolateCount].stateImp;
            levState.state = this.isolates[this.isolateCount].state;
            this.isolateCount--;
            s3 = s6;
        } else {
            levState.startON = -1;
            s3 = bArr[i] == 17 ? (short) (s5 + 1) : 0;
            levState.state = 0;
            processPropertySeq(levState, s5, i, i);
            i3 = i;
        }
        int i6 = i;
        int i7 = i3;
        short s7 = 1;
        int i8 = -1;
        int i9 = i6;
        while (i9 <= i2) {
            if (i9 >= i2) {
                int i10 = i2 - 1;
                while (i10 > i && (DirPropFlag(this.dirProps[i10]) & MASK_BN_EXPLICIT) != 0) {
                    i10--;
                }
                byte b2 = this.dirProps[i10];
                if (b2 == 20 || b2 == 21) {
                    break;
                }
                s4 = s2;
            } else {
                byte b3 = this.dirProps[i9];
                if (b3 == 7) {
                    this.isolateCount = -1;
                }
                if (z) {
                    if (b3 == 13) {
                        b3 = 1;
                    } else if (b3 == 2) {
                        if (i8 <= i9) {
                            i4 = i9 + 1;
                            while (true) {
                                if (i4 >= i2) {
                                    i4 = i2;
                                    s7 = 1;
                                    break;
                                }
                                b = this.dirProps[i4];
                                if (b == 0 || b == 1 || b == 13) {
                                    break;
                                }
                                i4++;
                            }
                            s7 = (short) b;
                        } else {
                            i4 = i8;
                        }
                        i8 = i4;
                        if (s7 == 13) {
                            b3 = 5;
                        }
                    }
                }
                s4 = groupProp[b3];
            }
            short s8 = impTabProps[s3][s4];
            short GetStateProps = GetStateProps(s8);
            short GetActionProps = GetActionProps(s8);
            if (i9 == i2 && GetActionProps == 0) {
                GetActionProps = 1;
            }
            if (GetActionProps != 0) {
                short s9 = impTabProps[s3][15];
                if (GetActionProps != 1) {
                    if (GetActionProps != 2) {
                        if (GetActionProps == 3) {
                            processPropertySeq(levState, s9, i7, i6);
                            processPropertySeq(levState, 4, i6, i9);
                        } else if (GetActionProps == 4) {
                            processPropertySeq(levState, s9, i7, i6);
                            i7 = i6;
                        } else {
                            throw new IllegalStateException("Internal ICU error in resolveImplicitLevels");
                        }
                    }
                    i6 = i9;
                } else {
                    processPropertySeq(levState, s9, i7, i9);
                }
                i7 = i9;
            }
            i9++;
            s3 = GetStateProps;
        }
        short s10 = (i2 != this.length || this.epilogue == null || (firstL_R_AL_EN_AN = firstL_R_AL_EN_AN()) == 4) ? s2 : (short) firstL_R_AL_EN_AN;
        int i11 = i2 - 1;
        while (i11 > i && (DirPropFlag(this.dirProps[i11]) & MASK_BN_EXPLICIT) != 0) {
            i11--;
        }
        byte b4 = this.dirProps[i11];
        if ((b4 == 20 || b4 == 21) && i2 < this.length) {
            this.isolateCount++;
            Isolate[] isolateArr = this.isolates;
            int i12 = this.isolateCount;
            if (isolateArr[i12] == null) {
                isolateArr[i12] = new Isolate();
            }
            Isolate[] isolateArr2 = this.isolates;
            int i13 = this.isolateCount;
            isolateArr2[i13].stateImp = s3;
            isolateArr2[i13].state = levState.state;
            Isolate[] isolateArr3 = this.isolates;
            int i14 = this.isolateCount;
            isolateArr3[i14].start1 = i7;
            isolateArr3[i14].startON = levState.startON;
            return;
        }
        processPropertySeq(levState, s10, i2, i2);
    }

    private void adjustWSLevels() {
        if ((this.flags & MASK_WS) != 0) {
            int i = this.trailingWSStart;
            while (i > 0) {
                while (i > 0) {
                    i--;
                    int DirPropFlag = DirPropFlag(this.dirProps[i]);
                    if ((MASK_WS & DirPropFlag) == 0) {
                        break;
                    } else if (!this.orderParagraphsLTR || (DirPropFlag((byte) 7) & DirPropFlag) == 0) {
                        this.levels[i] = GetParaLevelAt(i);
                    } else {
                        this.levels[i] = 0;
                    }
                }
                while (true) {
                    if (i <= 0) {
                        break;
                    }
                    i--;
                    int DirPropFlag2 = DirPropFlag(this.dirProps[i]);
                    if ((MASK_BN_EXPLICIT & DirPropFlag2) == 0) {
                        if (this.orderParagraphsLTR && (DirPropFlag((byte) 7) & DirPropFlag2) != 0) {
                            this.levels[i] = 0;
                            break;
                        } else if ((DirPropFlag2 & MASK_B_S) != 0) {
                            this.levels[i] = GetParaLevelAt(i);
                            break;
                        }
                    } else {
                        byte[] bArr = this.levels;
                        bArr[i] = bArr[i + 1];
                    }
                }
            }
        }
    }

    public void setContext(String str, String str2) {
        if (str == null || str.length() <= 0) {
            str = null;
        }
        this.prologue = str;
        if (str2 == null || str2.length() <= 0) {
            str2 = null;
        }
        this.epilogue = str2;
    }

    private void setParaSuccess() {
        this.prologue = null;
        this.epilogue = null;
        this.paraBidi = this;
    }

    /* access modifiers changed from: package-private */
    public void setParaRunsOnly(char[] cArr, byte b) {
        int i;
        int i2;
        byte b2;
        int i3;
        this.reorderingMode = 0;
        int length2 = cArr.length;
        if (length2 == 0) {
            setPara(cArr, b, (byte[]) null);
            this.reorderingMode = 3;
            return;
        }
        int i4 = this.reorderingOptions;
        int i5 = 2;
        if ((i4 & 1) > 0) {
            this.reorderingOptions = i4 & -2;
            this.reorderingOptions |= 2;
        }
        byte b3 = 1;
        byte b4 = (byte) (b & 1);
        setPara(cArr, b4, (byte[]) null);
        byte[] bArr = new byte[this.length];
        System.arraycopy(getLevels(), 0, bArr, 0, this.length);
        int i6 = this.trailingWSStart;
        String writeReordered = writeReordered(2);
        int[] visualMap = getVisualMap();
        this.reorderingOptions = i4;
        int i7 = this.length;
        byte b5 = this.direction;
        this.reorderingMode = 5;
        setPara(writeReordered, (byte) (b4 ^ 1), (byte[]) null);
        BidiLine.getRuns(this);
        int i8 = this.runCount;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        while (i9 < i8) {
            int i12 = this.runs[i9].limit - i11;
            if (i12 >= i5) {
                int i13 = this.runs[i9].start;
                int i14 = i10;
                int i15 = i13 + 1;
                while (i15 < i13 + i12) {
                    int i16 = visualMap[i15];
                    int i17 = visualMap[i15 - 1];
                    if (Bidi_Abs(i16 - i17) != 1 || bArr[i16] != bArr[i17]) {
                        i14++;
                    }
                    i15++;
                    i13 = i13;
                }
                i10 = i14;
            }
            i9++;
            i11 += i12;
            i5 = 2;
        }
        if (i10 > 0) {
            getRunsMemory(i8 + i10);
            int i18 = this.runCount;
            if (i18 == 1) {
                this.runsMemory[0] = this.runs[0];
            } else {
                System.arraycopy(this.runs, 0, this.runsMemory, 0, i18);
            }
            this.runs = this.runsMemory;
            this.runCount += i10;
            for (int i19 = i8; i19 < this.runCount; i19++) {
                BidiRun[] bidiRunArr = this.runs;
                if (bidiRunArr[i19] == null) {
                    bidiRunArr[i19] = new BidiRun(0, 0, (byte) 0);
                }
            }
        }
        int i20 = i8 - 1;
        while (i20 >= 0) {
            int i21 = i20 + i10;
            if (i20 == 0) {
                i = this.runs[0].limit;
            } else {
                i = this.runs[i20].limit - this.runs[i20 - 1].limit;
            }
            int i22 = this.runs[i20].start;
            int i23 = this.runs[i20].level & b3;
            if (i < 2) {
                if (i10 > 0) {
                    BidiRun[] bidiRunArr2 = this.runs;
                    bidiRunArr2[i21].copyFrom(bidiRunArr2[i20]);
                }
                int i24 = visualMap[i22];
                BidiRun[] bidiRunArr3 = this.runs;
                bidiRunArr3[i21].start = i24;
                bidiRunArr3[i21].level = (byte) (bArr[i24] ^ i23);
                i2 = i6;
            } else {
                if (i23 > 0) {
                    b2 = b3;
                    i22 = (i + i22) - b3;
                    i3 = i22;
                } else {
                    i3 = (i + i22) - b3;
                    b2 = -1;
                }
                int i25 = i3;
                while (i3 != i22) {
                    int i26 = visualMap[i3];
                    int i27 = i3 + b2;
                    int i28 = visualMap[i27];
                    if (Bidi_Abs(i26 - i28) != 1 || bArr[i26] != bArr[i28]) {
                        int Bidi_Min = Bidi_Min(visualMap[i25], i26);
                        BidiRun[] bidiRunArr4 = this.runs;
                        bidiRunArr4[i21].start = Bidi_Min;
                        bidiRunArr4[i21].level = (byte) (bArr[Bidi_Min] ^ i23);
                        bidiRunArr4[i21].limit = bidiRunArr4[i20].limit;
                        this.runs[i20].limit -= Bidi_Abs(i3 - i25) + 1;
                        int i29 = this.runs[i20].insertRemove & 10;
                        BidiRun[] bidiRunArr5 = this.runs;
                        bidiRunArr5[i21].insertRemove = i29;
                        BidiRun bidiRun = bidiRunArr5[i20];
                        bidiRun.insertRemove = (~i29) & bidiRun.insertRemove;
                        i10--;
                        i21--;
                        i25 = i27;
                    }
                    b2 = b2;
                    i3 = i27;
                    i6 = i6;
                }
                i2 = i6;
                if (i10 > 0) {
                    BidiRun[] bidiRunArr6 = this.runs;
                    bidiRunArr6[i21].copyFrom(bidiRunArr6[i20]);
                }
                int Bidi_Min2 = Bidi_Min(visualMap[i25], visualMap[i22]);
                BidiRun[] bidiRunArr7 = this.runs;
                bidiRunArr7[i21].start = Bidi_Min2;
                bidiRunArr7[i21].level = (byte) (bArr[Bidi_Min2] ^ i23);
            }
            i20--;
            i6 = i2;
            b3 = 1;
        }
        this.paraLevel = (byte) (this.paraLevel ^ 1);
        this.text = cArr;
        this.length = i7;
        this.originalLength = length2;
        this.direction = b5;
        this.levels = bArr;
        this.trailingWSStart = i6;
        if (this.runCount > 1) {
            this.direction = 2;
        }
        this.reorderingMode = 3;
    }

    public void setPara(String str, byte b, byte[] bArr) {
        if (str == null) {
            setPara(new char[0], b, bArr);
        } else {
            setPara(str.toCharArray(), b, bArr);
        }
    }

    public void setPara(char[] cArr, byte b, byte[] bArr) {
        int i;
        byte b2;
        byte b3;
        byte b4;
        Isolate[] isolateArr;
        if (b < 126) {
            verifyRange(b, 0, DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        }
        if (cArr == null) {
            cArr = new char[0];
        }
        if (this.reorderingMode == 3) {
            setParaRunsOnly(cArr, b);
            return;
        }
        this.paraBidi = null;
        this.text = cArr;
        int length2 = this.text.length;
        this.resultLength = length2;
        this.originalLength = length2;
        this.length = length2;
        this.paraLevel = b;
        this.direction = (byte) (b & 1);
        this.paraCount = 1;
        this.dirProps = new byte[0];
        this.levels = new byte[0];
        this.runs = new BidiRun[0];
        this.isGoodLogicalToVisualRunsMap = false;
        InsertPoints insertPoints2 = this.insertPoints;
        insertPoints2.size = 0;
        insertPoints2.confirmed = 0;
        this.defaultParaLevel = IsDefaultLevel(b) ? b : 0;
        int i2 = this.length;
        if (i2 == 0) {
            if (IsDefaultLevel(b)) {
                this.paraLevel = (byte) (1 & this.paraLevel);
                this.defaultParaLevel = 0;
            }
            this.flags = DirPropFlagLR(b);
            this.runCount = 0;
            this.paraCount = 0;
            setParaSuccess();
            return;
        }
        this.runCount = -1;
        getDirPropsMemory(i2);
        this.dirProps = this.dirPropsMemory;
        getDirProps();
        int i3 = this.length;
        this.trailingWSStart = i3;
        if (bArr == null) {
            getLevelsMemory(i3);
            this.levels = this.levelsMemory;
            this.direction = resolveExplicitLevels();
        } else {
            this.levels = bArr;
            this.direction = checkExplicitLevels();
        }
        int i4 = this.isolateCount;
        if (i4 > 0 && ((isolateArr = this.isolates) == null || isolateArr.length < i4)) {
            this.isolates = new Isolate[(this.isolateCount + 3)];
        }
        this.isolateCount = -1;
        byte b5 = this.direction;
        if (b5 == 0) {
            this.trailingWSStart = 0;
        } else if (b5 != 1) {
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
                    if ((this.reorderingOptions & 1) != 0) {
                        this.impTabPair = impTab_INVERSE_LIKE_DIRECT_WITH_MARKS;
                        break;
                    } else {
                        this.impTabPair = impTab_INVERSE_LIKE_DIRECT;
                        break;
                    }
                case 6:
                    if ((this.reorderingOptions & 1) != 0) {
                        this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS;
                        break;
                    } else {
                        this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL;
                        break;
                    }
            }
            if (bArr == null && this.paraCount <= 1 && (this.flags & DirPropFlagMultiRuns) == 0) {
                resolveImplicitLevels(0, this.length, (short) GetLRFromLevel(GetParaLevelAt(0)), (short) GetLRFromLevel(GetParaLevelAt(this.length - 1)));
            } else {
                byte GetParaLevelAt = GetParaLevelAt(0);
                byte b6 = this.levels[0];
                if (GetParaLevelAt < b6) {
                    b2 = GetLRFromLevel(b6);
                } else {
                    b2 = GetLRFromLevel(GetParaLevelAt);
                }
                short s = (short) b2;
                int i5 = 0;
                while (true) {
                    if (i5 > 0 && this.dirProps[i5 - 1] == 7) {
                        s = (short) GetLRFromLevel(GetParaLevelAt(i5));
                    }
                    int i6 = i5;
                    while (true) {
                        i6++;
                        if (i6 >= this.length || (this.levels[i6] != b6 && (DirPropFlag(this.dirProps[i6]) & MASK_BN_EXPLICIT) == 0)) {
                        }
                    }
                    int i7 = this.length;
                    if (i6 < i7) {
                        b3 = this.levels[i6];
                    } else {
                        b3 = GetParaLevelAt(i7 - 1);
                    }
                    if (NoOverride(b6) < NoOverride(b3)) {
                        b4 = GetLRFromLevel(b3);
                    } else {
                        b4 = GetLRFromLevel(b6);
                    }
                    short s2 = (short) b4;
                    if ((b6 & LEVEL_OVERRIDE) == 0) {
                        resolveImplicitLevels(i5, i6, s, s2);
                    } else {
                        while (true) {
                            byte[] bArr2 = this.levels;
                            int i8 = i5 + 1;
                            bArr2[i5] = (byte) (bArr2[i5] & LEVEL_DEFAULT_RTL);
                            if (i8 < i6) {
                                i5 = i8;
                            }
                        }
                    }
                    if (i6 < this.length) {
                        i5 = i6;
                        b6 = b3;
                        s = s2;
                    }
                }
            }
            adjustWSLevels();
        } else {
            this.trailingWSStart = 0;
        }
        if (this.defaultParaLevel > 0 && (this.reorderingOptions & 1) != 0 && ((i = this.reorderingMode) == 5 || i == 6)) {
            for (int i9 = 0; i9 < this.paraCount; i9++) {
                int[] iArr = this.paras_limit;
                int i10 = iArr[i9] - 1;
                if (this.paras_level[i9] != 0) {
                    int i11 = i9 == 0 ? 0 : iArr[i9 - 1];
                    int i12 = i10;
                    while (true) {
                        if (i12 >= i11) {
                            byte b7 = this.dirProps[i12];
                            if (b7 == 0) {
                                if (i12 < i10) {
                                    while (this.dirProps[i10] == 7) {
                                        i10--;
                                    }
                                }
                                addPoint(i10, 4);
                            } else if ((DirPropFlag(b7) & MASK_R_AL) == 0) {
                                i12--;
                            }
                        }
                    }
                }
            }
        }
        if ((this.reorderingOptions & 2) != 0) {
            this.resultLength -= this.controlCount;
        } else {
            this.resultLength += this.insertPoints.size;
        }
        setParaSuccess();
    }

    public void setPara(AttributedCharacterIterator attributedCharacterIterator) {
        int i;
        byte byteValue;
        Boolean bool = (Boolean) attributedCharacterIterator.getAttribute(TextAttribute.RUN_DIRECTION);
        if (bool == null) {
            i = DIRECTION_DEFAULT_LEFT_TO_RIGHT;
        } else {
            i = !bool.equals(TextAttribute.RUN_DIRECTION_LTR);
        }
        int endIndex = attributedCharacterIterator.getEndIndex() - attributedCharacterIterator.getBeginIndex();
        byte[] bArr = new byte[endIndex];
        char[] cArr = new char[endIndex];
        char first = attributedCharacterIterator.first();
        byte[] bArr2 = null;
        int i2 = 0;
        while (first != 65535) {
            cArr[i2] = first;
            Integer num = (Integer) attributedCharacterIterator.getAttribute(TextAttribute.BIDI_EMBEDDING);
            if (!(num == null || (byteValue = num.byteValue()) == 0)) {
                if (byteValue < 0) {
                    bArr[i2] = (byte) ((0 - byteValue) | -128);
                } else {
                    bArr[i2] = byteValue;
                }
                bArr2 = bArr;
            }
            first = attributedCharacterIterator.next();
            i2++;
        }
        NumericShaper numericShaper = (NumericShaper) attributedCharacterIterator.getAttribute(TextAttribute.NUMERIC_SHAPING);
        if (numericShaper != null) {
            numericShaper.shape(cArr, 0, endIndex);
        }
        setPara(cArr, i == 1 ? (byte) 1 : 0, bArr2);
    }

    public void orderParagraphsLTR(boolean z) {
        this.orderParagraphsLTR = z;
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

    public BidiRun getParagraphByIndex(int i) {
        verifyValidParaOrLine();
        int i2 = 0;
        verifyRange(i, 0, this.paraCount);
        Bidi bidi = this.paraBidi;
        if (i != 0) {
            i2 = bidi.paras_limit[i - 1];
        }
        BidiRun bidiRun = new BidiRun();
        bidiRun.start = i2;
        bidiRun.limit = bidi.paras_limit[i];
        bidiRun.level = GetParaLevelAt(i2);
        return bidiRun;
    }

    public BidiRun getParagraph(int i) {
        verifyValidParaOrLine();
        Bidi bidi = this.paraBidi;
        int i2 = 0;
        verifyRange(i, 0, bidi.length);
        while (i >= bidi.paras_limit[i2]) {
            i2++;
        }
        return getParagraphByIndex(i2);
    }

    public int getParagraphIndex(int i) {
        verifyValidParaOrLine();
        Bidi bidi = this.paraBidi;
        int i2 = 0;
        verifyRange(i, 0, bidi.length);
        while (i >= bidi.paras_limit[i2]) {
            i2++;
        }
        return i2;
    }

    public void setCustomClassifier(BidiClassifier bidiClassifier) {
        this.customClassifier = bidiClassifier;
    }

    public BidiClassifier getCustomClassifier() {
        return this.customClassifier;
    }

    public int getCustomizedClass(int i) {
        int i2;
        BidiClassifier bidiClassifier = this.customClassifier;
        if (bidiClassifier == null || (i2 = bidiClassifier.classify(i)) == 23) {
            i2 = this.bdp.getClass(i);
        }
        if (i2 >= 23) {
            return 10;
        }
        return i2;
    }

    public Bidi setLine(int i, int i2) {
        verifyValidPara();
        verifyRange(i, 0, i2);
        verifyRange(i2, 0, this.length + 1);
        if (getParagraphIndex(i) == getParagraphIndex(i2 - 1)) {
            return BidiLine.setLine(this, i, i2);
        }
        throw new IllegalArgumentException();
    }

    public byte getLevelAt(int i) {
        verifyValidParaOrLine();
        verifyRange(i, 0, this.length);
        return BidiLine.getLevelAt(this, i);
    }

    public byte[] getLevels() {
        verifyValidParaOrLine();
        if (this.length <= 0) {
            return new byte[0];
        }
        return BidiLine.getLevels(this);
    }

    public BidiRun getLogicalRun(int i) {
        verifyValidParaOrLine();
        verifyRange(i, 0, this.length);
        return BidiLine.getLogicalRun(this, i);
    }

    public int countRuns() {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        return this.runCount;
    }

    public BidiRun getVisualRun(int i) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(i, 0, this.runCount);
        return BidiLine.getVisualRun(this, i);
    }

    public int getVisualIndex(int i) {
        verifyValidParaOrLine();
        verifyRange(i, 0, this.length);
        return BidiLine.getVisualIndex(this, i);
    }

    public int getLogicalIndex(int i) {
        verifyValidParaOrLine();
        verifyRange(i, 0, this.resultLength);
        if (this.insertPoints.size == 0 && this.controlCount == 0) {
            byte b = this.direction;
            if (b == 0) {
                return i;
            }
            if (b == 1) {
                return (this.length - i) - 1;
            }
        }
        BidiLine.getRuns(this);
        return BidiLine.getLogicalIndex(this, i);
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

    public static int[] reorderLogical(byte[] bArr) {
        return BidiLine.reorderLogical(bArr);
    }

    public static int[] reorderVisual(byte[] bArr) {
        return BidiLine.reorderVisual(bArr);
    }

    public static int[] invertMap(int[] iArr) {
        if (iArr == null) {
            return null;
        }
        return BidiLine.invertMap(iArr);
    }

    public Bidi(String str, int i) {
        this(str.toCharArray(), 0, null, 0, str.length(), i);
    }

    public Bidi(AttributedCharacterIterator attributedCharacterIterator) {
        this();
        setPara(attributedCharacterIterator);
    }

    public Bidi(char[] cArr, int i, byte[] bArr, int i2, int i3, int i4) {
        this();
        byte[] bArr2;
        byte b = LEVEL_DEFAULT_RTL;
        if (i4 == 1) {
            b = 1;
        } else if (i4 == 126) {
            b = 126;
        } else if (i4 != 127) {
            b = 0;
        }
        if (bArr == null) {
            bArr2 = null;
        } else {
            byte[] bArr3 = new byte[i3];
            for (int i5 = 0; i5 < i3; i5++) {
                byte b2 = bArr[i5 + i2];
                if (b2 < 0) {
                    b2 = (byte) ((-b2) | -128);
                }
                bArr3[i5] = b2;
            }
            bArr2 = bArr3;
        }
        if (i == 0 && i3 == cArr.length) {
            setPara(cArr, b, bArr2);
            return;
        }
        char[] cArr2 = new char[i3];
        System.arraycopy(cArr, i, cArr2, 0, i3);
        setPara(cArr2, b, bArr2);
    }

    public Bidi createLineBidi(int i, int i2) {
        return setLine(i, i2);
    }

    public boolean isMixed() {
        return !isLeftToRight() && !isRightToLeft();
    }

    public boolean isLeftToRight() {
        return getDirection() == 0 && (this.paraLevel & 1) == 0;
    }

    public boolean isRightToLeft() {
        return getDirection() == 1 && (this.paraLevel & 1) == 1;
    }

    public boolean baseIsLeftToRight() {
        return getParaLevel() == 0;
    }

    public int getBaseLevel() {
        return getParaLevel();
    }

    public int getRunCount() {
        return countRuns();
    }

    /* access modifiers changed from: package-private */
    public void getLogicalToVisualRunsMap() {
        if (!this.isGoodLogicalToVisualRunsMap) {
            int countRuns = countRuns();
            int[] iArr = this.logicalToVisualRunsMap;
            if (iArr == null || iArr.length < countRuns) {
                this.logicalToVisualRunsMap = new int[countRuns];
            }
            long[] jArr = new long[countRuns];
            for (int i = 0; i < countRuns; i++) {
                jArr[i] = (((long) this.runs[i].start) << 32) + ((long) i);
            }
            Arrays.sort(jArr);
            for (int i2 = 0; i2 < countRuns; i2++) {
                this.logicalToVisualRunsMap[i2] = (int) (jArr[i2] & -1);
            }
            this.isGoodLogicalToVisualRunsMap = true;
        }
    }

    public int getRunLevel(int i) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(i, 0, this.runCount);
        getLogicalToVisualRunsMap();
        return this.runs[this.logicalToVisualRunsMap[i]].level;
    }

    public int getRunStart(int i) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(i, 0, this.runCount);
        getLogicalToVisualRunsMap();
        return this.runs[this.logicalToVisualRunsMap[i]].start;
    }

    public int getRunLimit(int i) {
        int i2;
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(i, 0, this.runCount);
        getLogicalToVisualRunsMap();
        int i3 = this.logicalToVisualRunsMap[i];
        if (i3 == 0) {
            i2 = this.runs[i3].limit;
        } else {
            i2 = this.runs[i3].limit - this.runs[i3 - 1].limit;
        }
        return this.runs[i3].start + i2;
    }

    public static boolean requiresBidi(char[] cArr, int i, int i2) {
        while (i < i2) {
            if (((1 << UCharacter.getDirection(cArr[i])) & 57378) != 0) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static void reorderVisually(byte[] bArr, int i, Object[] objArr, int i2, int i3) {
        byte[] bArr2 = new byte[i3];
        System.arraycopy(bArr, i, bArr2, 0, i3);
        int[] reorderVisual = reorderVisual(bArr2);
        Object[] objArr2 = new Object[i3];
        System.arraycopy(objArr, i2, objArr2, 0, i3);
        for (int i4 = 0; i4 < i3; i4++) {
            objArr[i2 + i4] = objArr2[reorderVisual[i4]];
        }
    }

    public String writeReordered(int i) {
        verifyValidParaOrLine();
        if (this.length == 0) {
            return "";
        }
        return BidiWriter.writeReordered(this, i);
    }

    public static String writeReverse(String str, int i) {
        if (str != null) {
            return str.length() > 0 ? BidiWriter.writeReverse(str, i) : "";
        }
        throw new IllegalArgumentException();
    }
}
