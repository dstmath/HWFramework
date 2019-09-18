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
    private static final byte[][] impTabL_DEFAULT = {new byte[]{0, 1, 0, 2, 0, 0, 0, 0}, new byte[]{0, 1, 3, 3, 20, 20, 0, 1}, new byte[]{0, 1, 0, 2, 21, 21, 0, 2}, new byte[]{0, 1, 3, 3, 20, 20, 0, 2}, new byte[]{0, 33, 51, 51, 4, 4, 0, 0}, new byte[]{0, 33, 0, 50, 5, 5, 0, 0}};
    private static final byte[][] impTabL_GROUP_NUMBERS_WITH_R = {new byte[]{0, 3, 17, 17, 0, 0, 0, 0}, new byte[]{32, 3, 1, 1, 2, 32, 32, 2}, new byte[]{32, 3, 1, 1, 2, 32, 32, 1}, new byte[]{0, 3, 5, 5, 20, 0, 0, 1}, new byte[]{32, 3, 5, 5, 4, 32, 32, 1}, new byte[]{0, 3, 5, 5, 20, 0, 0, 2}};
    private static final byte[][] impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = {new byte[]{0, 98, 1, 1, 0, 0, 0, 0}, new byte[]{0, 98, 1, 1, 0, 48, 0, 4}, new byte[]{0, 98, 84, 84, 19, 48, 0, 3}, new byte[]{48, 66, 84, 84, 3, 48, 48, 3}, new byte[]{48, 66, 4, 4, 19, 48, 48, 4}};
    private static final byte[][] impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS = {new byte[]{0, 99, 0, 1, 0, 0, 0, 0}, new byte[]{0, 99, 0, 1, 18, 48, 0, 4}, new byte[]{32, 99, 32, 1, 2, 48, 32, 3}, new byte[]{0, 99, 85, 86, 20, 48, 0, 3}, new byte[]{48, 67, 85, 86, 4, 48, 48, 3}, new byte[]{48, 67, 5, 86, 20, 48, 48, 4}, new byte[]{48, 67, 85, 6, 20, 48, 48, 4}};
    private static final byte[][] impTabL_INVERSE_NUMBERS_AS_L = {new byte[]{0, 1, 0, 0, 0, 0, 0, 0}, new byte[]{0, 1, 0, 0, 20, 20, 0, 1}, new byte[]{0, 1, 0, 0, 21, 21, 0, 2}, new byte[]{0, 1, 0, 0, 20, 20, 0, 2}, new byte[]{32, 1, 32, 32, 4, 4, 32, 1}, new byte[]{32, 1, 32, 32, 5, 5, 32, 1}};
    private static final byte[][] impTabL_NUMBERS_SPECIAL = {new byte[]{0, 2, 17, 17, 0, 0, 0, 0}, new byte[]{0, 66, 1, 1, 0, 0, 0, 0}, new byte[]{0, 2, 4, 4, 19, 19, 0, 1}, new byte[]{0, 34, 52, 52, 3, 3, 0, 0}, new byte[]{0, 2, 4, 4, 19, 19, 0, 2}};
    private static final short[][] impTabProps = {new short[]{1, 2, 4, 5, 7, 15, 17, 7, 9, 7, 0, 7, 3, 18, 21, 4}, new short[]{1, 34, 36, 37, 39, 47, 49, 39, 41, 39, 1, 1, 35, 50, 53, 0}, new short[]{33, 2, 36, 37, 39, 47, 49, 39, 41, 39, 2, 2, 35, 50, 53, 1}, new short[]{33, 34, 38, 38, 40, 48, 49, 40, 40, 40, 3, 3, 3, 50, 53, 1}, new short[]{33, 34, 4, 37, 39, 47, 49, 74, 11, 74, 4, 4, 35, 18, 21, 2}, new short[]{33, 34, 36, 5, 39, 47, 49, 39, 41, 76, 5, 5, 35, 50, 53, 3}, new short[]{33, 34, 6, 6, 40, 48, 49, 40, 40, 77, 6, 6, 35, 18, 21, 3}, new short[]{33, 34, 36, 37, 7, 47, 49, 7, 78, 7, 7, 7, 35, 50, 53, 4}, new short[]{33, 34, 38, 38, 8, 48, 49, 8, 8, 8, 8, 8, 35, 50, 53, 4}, new short[]{33, 34, 4, 37, 7, 47, 49, 7, 9, 7, 9, 9, 35, 18, 21, 4}, new short[]{97, 98, 4, 101, 135, 111, 113, 135, 142, 135, 10, 135, 99, 18, 21, 2}, new short[]{33, 34, 4, 37, 39, 47, 49, 39, 11, 39, 11, 11, 35, 18, 21, 2}, new short[]{97, 98, 100, 5, 135, 111, 113, 135, 142, 135, 12, 135, 99, 114, 117, 3}, new short[]{97, 98, 6, 6, 136, 112, 113, 136, 136, 136, 13, 136, 99, 18, 21, 3}, new short[]{33, 34, 132, 37, 7, 47, 49, 7, 14, 7, 14, 14, 35, 146, 149, 4}, new short[]{33, 34, 36, 37, 39, 15, 49, 39, 41, 39, 15, 39, 35, 50, 53, 5}, new short[]{33, 34, 38, 38, 40, 16, 49, 40, 40, 40, 16, 40, 35, 50, 53, 5}, new short[]{33, 34, 36, 37, 39, 47, 17, 39, 41, 39, 17, 39, 35, 50, 53, 6}, new short[]{33, 34, 18, 37, 39, 47, 49, 83, 20, 83, 18, 18, 35, 18, 21, 0}, new short[]{97, 98, 18, 101, 135, 111, 113, 135, 142, 135, 19, 135, 99, 18, 21, 0}, new short[]{33, 34, 18, 37, 39, 47, 49, 39, 20, 39, 20, 20, 35, 18, 21, 0}, new short[]{33, 34, 21, 37, 39, 47, 49, 86, 23, 86, 21, 21, 35, 18, 21, 3}, new short[]{97, 98, 21, 101, 135, 111, 113, 135, 142, 135, 22, 135, 99, 18, 21, 3}, new short[]{33, 34, 21, 37, 39, 47, 49, 39, 23, 39, 23, 23, 35, 18, 21, 3}};
    private static final byte[][] impTabR_DEFAULT = {new byte[]{1, 0, 2, 2, 0, 0, 0, 0}, new byte[]{1, 0, 1, 3, 20, 20, 0, 1}, new byte[]{1, 0, 2, 2, 0, 0, 0, 1}, new byte[]{1, 0, 1, 3, 5, 5, 0, 1}, new byte[]{33, 0, 33, 3, 4, 4, 0, 0}, new byte[]{1, 0, 1, 3, 5, 5, 0, 0}};
    private static final byte[][] impTabR_GROUP_NUMBERS_WITH_R = {new byte[]{2, 0, 1, 1, 0, 0, 0, 0}, new byte[]{2, 0, 1, 1, 0, 0, 0, 1}, new byte[]{2, 0, 20, 20, 19, 0, 0, 1}, new byte[]{34, 0, 4, 4, 3, 0, 0, 0}, new byte[]{34, 0, 4, 4, 3, 0, 0, 1}};
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT = {new byte[]{1, 0, 2, 2, 0, 0, 0, 0}, new byte[]{1, 0, 1, 2, 19, 19, 0, 1}, new byte[]{1, 0, 2, 2, 0, 0, 0, 1}, new byte[]{33, 48, 6, 4, 3, 3, 48, 0}, new byte[]{33, 48, 6, 4, 5, 5, 48, 3}, new byte[]{33, 48, 6, 4, 5, 5, 48, 2}, new byte[]{33, 48, 6, 4, 3, 3, 48, 1}};
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS = {new byte[]{19, 0, 1, 1, 0, 0, 0, 0}, new byte[]{35, 0, 1, 1, 2, 64, 0, 1}, new byte[]{35, 0, 1, 1, 2, 64, 0, 0}, new byte[]{3, 0, 3, 54, 20, 64, 0, 1}, new byte[]{83, 64, 5, 54, 4, 64, 64, 0}, new byte[]{83, 64, 5, 54, 4, 64, 64, 1}, new byte[]{83, 64, 6, 6, 4, 64, 64, 3}};
    private static final byte[][] impTabR_INVERSE_NUMBERS_AS_L = {new byte[]{1, 0, 1, 1, 0, 0, 0, 0}, new byte[]{1, 0, 1, 1, 20, 20, 0, 1}, new byte[]{1, 0, 1, 1, 0, 0, 0, 1}, new byte[]{1, 0, 1, 1, 5, 5, 0, 1}, new byte[]{33, 0, 33, 33, 4, 4, 0, 0}, new byte[]{1, 0, 1, 1, 5, 5, 0, 0}};
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

    /* access modifiers changed from: package-private */
    public boolean testDirPropFlagAt(int flag, int index) {
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
        return strong == 0 ? (byte) 0 : 1;
    }

    static final byte NoOverride(byte level) {
        return (byte) (level & LEVEL_DEFAULT_RTL);
    }

    static byte GetLRFromLevel(byte level) {
        return (byte) (level & 1);
    }

    static boolean IsDefaultLevel(byte level) {
        return (level & LEVEL_DEFAULT_LTR) == 126;
    }

    static boolean IsBidiControlChar(int c) {
        return (c & -4) == 8204 || (c >= 8234 && c <= 8238) || (c >= 8294 && c <= 8297);
    }

    /* access modifiers changed from: package-private */
    public void verifyValidPara() {
        if (this != this.paraBidi) {
            throw new IllegalStateException();
        }
    }

    /* access modifiers changed from: package-private */
    public void verifyValidParaOrLine() {
        Bidi para = this.paraBidi;
        if (this != para) {
            if (para == null || para != para.paraBidi) {
                throw new IllegalStateException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void verifyRange(int index, int start, int limit) {
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

    /* access modifiers changed from: package-private */
    public void getDirPropsMemory(int len) {
        getDirPropsMemory(this.mayAllocateText, len);
    }

    private void getLevelsMemory(boolean mayAllocate, int len) {
        this.levelsMemory = (byte[]) getMemory("Levels", this.levelsMemory, Byte.TYPE, mayAllocate, len);
    }

    /* access modifiers changed from: package-private */
    public void getLevelsMemory(int len) {
        getLevelsMemory(this.mayAllocateText, len);
    }

    private void getRunsMemory(boolean mayAllocate, int len) {
        this.runsMemory = (BidiRun[]) getMemory("Runs", this.runsMemory, BidiRun.class, mayAllocate, len);
    }

    /* access modifiers changed from: package-private */
    public void getRunsMemory(int len) {
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

    public void setInverse(boolean isInverse2) {
        int i;
        this.isInverse = isInverse2;
        if (isInverse2) {
            i = 4;
        } else {
            i = 0;
        }
        this.reorderingMode = i;
    }

    public boolean isInverse() {
        return this.isInverse;
    }

    public void setReorderingMode(int reorderingMode2) {
        if (reorderingMode2 >= 0 && reorderingMode2 < 7) {
            this.reorderingMode = reorderingMode2;
            this.isInverse = reorderingMode2 == 4;
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
            return 3;
        }
        int length2 = paragraph.length();
        int i = 0;
        while (i < length2) {
            byte direction2 = UCharacter.getDirectionality(UCharacter.codePointAt(paragraph, i));
            if (direction2 == 0) {
                return 0;
            }
            if (direction2 == 1 || direction2 == 13) {
                return 1;
            }
            i = UCharacter.offsetByCodePoints(paragraph, i, 1);
        }
        return 3;
    }

    private byte firstL_R_AL() {
        byte result = 10;
        int i = 0;
        while (i < this.prologue.length()) {
            int uchar = this.prologue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (result == 10) {
                if (dirProp == 0 || dirProp == 1 || dirProp == 13) {
                    result = dirProp;
                }
            } else if (dirProp == 7) {
                result = 10;
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

    /* JADX WARNING: Removed duplicated region for block: B:112:0x01d0  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x00f2 A[SYNTHETIC] */
    private void getDirProps() {
        byte state;
        byte b;
        int isDefaultLevel;
        char c;
        int i1;
        byte state2;
        byte state3;
        byte state4;
        int i = 0;
        this.flags = 0;
        int i12 = IsDefaultLevel(this.paraLevel);
        boolean isDefaultLevelInverse = i12 != 0 && (this.reorderingMode == 5 || this.reorderingMode == 6);
        this.lastArabicPos = -1;
        int controlCount2 = 0;
        boolean removeBidiControls = (this.reorderingOptions & 2) != 0;
        byte lastStrong = 10;
        int[] isolateStartStack = new int[126];
        byte[] previousStateStack = new byte[126];
        int stackLast = -1;
        if ((this.reorderingOptions & 4) != 0) {
            this.length = 0;
        }
        byte defaultParaLevel2 = (byte) (this.paraLevel & 1);
        if (i12 != 0) {
            this.paras_level[0] = defaultParaLevel2;
            lastStrong = defaultParaLevel2;
            if (this.prologue != null) {
                byte firstL_R_AL = firstL_R_AL();
                byte dirProp = firstL_R_AL;
                if (firstL_R_AL != 10) {
                    if (dirProp == 0) {
                        this.paras_level[0] = 0;
                    } else {
                        this.paras_level[0] = 1;
                    }
                    state = 0;
                }
            }
            state = 1;
        } else {
            this.paras_level[0] = this.paraLevel;
            state = 0;
        }
        int i2 = 0;
        while (i2 < this.originalLength) {
            int i0 = i2;
            int uchar = UTF16.charAt(this.text, i, this.originalLength, i2);
            i2 += UTF16.getCharCount(uchar);
            int i13 = i2 - 1;
            byte dirProp2 = (byte) getCustomizedClass(uchar);
            byte defaultParaLevel3 = defaultParaLevel2;
            this.flags |= DirPropFlag(dirProp2);
            this.dirProps[i13] = dirProp2;
            if (i13 > i0) {
                int i14 = i13;
                this.flags |= DirPropFlag((byte) 18);
                while (true) {
                    isDefaultLevel = i12;
                    c = 65535;
                    i1 = i14 - 1;
                    this.dirProps[i1] = 18;
                    if (i1 <= i0) {
                        break;
                    }
                    i14 = i1;
                    i12 = isDefaultLevel;
                }
            } else {
                isDefaultLevel = i12;
                c = 65535;
                i1 = i13;
            }
            if (removeBidiControls && IsBidiControlChar(uchar)) {
                controlCount2++;
            }
            if (dirProp2 == 0) {
                if (state == 1) {
                    int i3 = i1;
                    this.paras_level[this.paraCount - 1] = 0;
                    state4 = 0;
                } else {
                    if (state == 2) {
                        if (stackLast <= 125) {
                            this.flags |= DirPropFlag((byte) 20);
                        }
                        state4 = 3;
                    }
                    lastStrong = 0;
                }
                state = state4;
                lastStrong = 0;
            } else {
                int i4 = i1;
                if (dirProp2 == 1 || dirProp2 == 13) {
                    if (state == 1) {
                        this.paras_level[this.paraCount - 1] = 1;
                        state2 = 0;
                    } else {
                        if (state == 2) {
                            if (stackLast <= 125) {
                                this.dirProps[isolateStartStack[stackLast]] = 21;
                                this.flags = DirPropFlag((byte) 21) | this.flags;
                            }
                            state2 = 3;
                        }
                        lastStrong = 1;
                        if (dirProp2 != 13) {
                            this.lastArabicPos = i2 - 1;
                        }
                    }
                    state = state2;
                    lastStrong = 1;
                    if (dirProp2 != 13) {
                    }
                } else if (dirProp2 >= 19 && dirProp2 <= 21) {
                    stackLast++;
                    if (stackLast <= 125) {
                        isolateStartStack[stackLast] = i2 - 1;
                        previousStateStack[stackLast] = state;
                    }
                    if (dirProp2 == 19) {
                        this.dirProps[i2 - 1] = 20;
                        state = 2;
                    } else {
                        state = 3;
                    }
                } else if (dirProp2 == 22) {
                    if (state == 2 && stackLast <= 125) {
                        this.flags |= DirPropFlag((byte) 20);
                    }
                    if (stackLast >= 0) {
                        if (stackLast <= 125) {
                            state = previousStateStack[stackLast];
                        }
                        stackLast--;
                    }
                } else if (dirProp2 == 7) {
                    if (i2 < this.originalLength && uchar == 13) {
                        if (this.text[i2] == 10) {
                        }
                    }
                    this.paras_limit[this.paraCount - 1] = i2;
                    if (isDefaultLevelInverse && lastStrong == 1) {
                        this.paras_level[this.paraCount - 1] = 1;
                    }
                    if ((this.reorderingOptions & 4) != 0) {
                        this.length = i2;
                        this.controlCount = controlCount2;
                    }
                    if (i2 < this.originalLength) {
                        this.paraCount++;
                        checkParaCount();
                        if (isDefaultLevel != 0) {
                            this.paras_level[this.paraCount - 1] = defaultParaLevel3;
                            state3 = 1;
                            lastStrong = defaultParaLevel3;
                        } else {
                            this.paras_level[this.paraCount - 1] = this.paraLevel;
                            state3 = 0;
                        }
                        stackLast = -1;
                    }
                }
            }
            char c2 = c;
            defaultParaLevel2 = defaultParaLevel3;
            i12 = isDefaultLevel;
            i = 0;
        }
        int isDefaultLevel2 = i12;
        if (stackLast > 125) {
            stackLast = 125;
            state = 2;
        }
        while (true) {
            if (stackLast < 0) {
                break;
            } else if (state == 2) {
                this.flags |= DirPropFlag((byte) 20);
                break;
            } else {
                state = previousStateStack[stackLast];
                stackLast--;
            }
        }
        if ((this.reorderingOptions & 4) == 0) {
            b = 1;
            this.paras_limit[this.paraCount - 1] = this.originalLength;
            this.controlCount = controlCount2;
        } else if (this.length < this.originalLength) {
            b = 1;
            this.paraCount--;
        } else {
            b = 1;
        }
        if (isDefaultLevelInverse && lastStrong == b) {
            this.paras_level[this.paraCount - b] = b;
        }
        if (isDefaultLevel2 != 0) {
            this.paraLevel = this.paras_level[0];
        }
        for (int i5 = 0; i5 < this.paraCount; i5++) {
            this.flags |= DirPropFlagLR(this.paras_level[i5]);
        }
        if (this.orderParagraphsLTR && (this.flags & DirPropFlag((byte) 7)) != 0) {
            this.flags |= DirPropFlag((byte) 0);
        }
    }

    /* access modifiers changed from: package-private */
    public byte GetParaLevelAt(int pindex) {
        if (this.defaultParaLevel == 0 || pindex < this.paras_limit[0]) {
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
        boolean z = false;
        bd.isoRunLast = 0;
        bd.isoRuns[0] = new IsoRun();
        bd.isoRuns[0].start = 0;
        bd.isoRuns[0].limit = 0;
        bd.isoRuns[0].level = GetParaLevelAt(0);
        IsoRun isoRun = bd.isoRuns[0];
        IsoRun isoRun2 = bd.isoRuns[0];
        IsoRun isoRun3 = bd.isoRuns[0];
        byte GetParaLevelAt = (byte) (GetParaLevelAt(0) & 1);
        isoRun3.contextDir = GetParaLevelAt;
        isoRun2.lastBase = GetParaLevelAt;
        isoRun.lastStrong = GetParaLevelAt;
        bd.isoRuns[0].contextPos = 0;
        bd.openings = new Opening[20];
        if (this.reorderingMode == 1 || this.reorderingMode == 6) {
            z = true;
        }
        bd.isNumbersSpecial = z;
    }

    private void bracketProcessB(BracketData bd, byte level) {
        bd.isoRunLast = 0;
        bd.isoRuns[0].limit = 0;
        bd.isoRuns[0].level = level;
        IsoRun isoRun = bd.isoRuns[0];
        IsoRun isoRun2 = bd.isoRuns[0];
        byte b = (byte) (level & 1);
        bd.isoRuns[0].contextDir = b;
        isoRun2.lastBase = b;
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
        pLastIsoRun.lastBase = 10;
        short lastLimit = pLastIsoRun.limit;
        bd.isoRunLast++;
        IsoRun pLastIsoRun2 = bd.isoRuns[bd.isoRunLast];
        if (pLastIsoRun2 == null) {
            IsoRun[] isoRunArr = bd.isoRuns;
            int i = bd.isoRunLast;
            IsoRun isoRun = new IsoRun();
            isoRunArr[i] = isoRun;
            pLastIsoRun2 = isoRun;
        }
        pLastIsoRun2.limit = lastLimit;
        pLastIsoRun2.start = lastLimit;
        pLastIsoRun2.level = level;
        byte b = (byte) (level & 1);
        pLastIsoRun2.contextDir = b;
        pLastIsoRun2.lastBase = b;
        pLastIsoRun2.lastStrong = b;
        pLastIsoRun2.contextPos = 0;
    }

    private void bracketProcessPDI(BracketData bd) {
        bd.isoRunLast--;
        bd.isoRuns[bd.isoRunLast].lastBase = 10;
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
            Opening[] openingArr = bd.openings;
            short s = pLastIsoRun.limit;
            Opening opening = new Opening();
            openingArr[s] = opening;
            pOpening = opening;
        }
        pOpening.position = position;
        pOpening.match = match;
        pOpening.contextDir = pLastIsoRun.contextDir;
        pOpening.contextPos = pLastIsoRun.contextPos;
        pOpening.flags = 0;
        pLastIsoRun.limit = (short) (pLastIsoRun.limit + 1);
    }

    private void fixN0c(BracketData bd, int openingIndex, int newPropPosition, byte newProp) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        for (int k = openingIndex + 1; k < pLastIsoRun.limit; k++) {
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
                } else {
                    return;
                }
            }
        }
    }

    private byte bracketProcessClosing(BracketData bd, int openIdx, int position) {
        byte newProp;
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        Opening pOpening = bd.openings[openIdx];
        byte direction2 = (byte) (pLastIsoRun.level & 1);
        boolean stable = true;
        if ((direction2 == 0 && (pOpening.flags & FOUND_L) > 0) || (direction2 == 1 && (pOpening.flags & FOUND_R) > 0)) {
            newProp = direction2;
        } else if ((pOpening.flags & (FOUND_L | FOUND_R)) != 0) {
            stable = openIdx == pLastIsoRun.start;
            if (direction2 != pOpening.contextDir) {
                newProp = pOpening.contextDir;
            } else {
                newProp = direction2;
            }
        } else {
            pLastIsoRun.limit = (short) openIdx;
            return 10;
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
            int k = openIdx - 1;
            while (k >= pLastIsoRun.start && bd.openings[k].position == pOpening.position) {
                bd.openings[k].match = 0;
                k--;
            }
            for (int k2 = openIdx + 1; k2 < pLastIsoRun.limit; k2++) {
                Opening qOpening = bd.openings[k2];
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

    private void bracketProcessChar(BracketData bd, int position) {
        byte newProp;
        char match;
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        byte dirProp = this.dirProps[position];
        if (dirProp == 10) {
            char c = this.text[position];
            int idx = pLastIsoRun.limit - 1;
            while (true) {
                if (idx < pLastIsoRun.start) {
                    break;
                } else if (bd.openings[idx].match != c) {
                    idx--;
                } else {
                    byte newProp2 = bracketProcessClosing(bd, idx, position);
                    if (newProp2 == 10) {
                        c = 0;
                    } else {
                        pLastIsoRun.lastBase = 10;
                        pLastIsoRun.contextDir = newProp2;
                        pLastIsoRun.contextPos = position;
                        byte level = this.levels[position];
                        if ((level & LEVEL_OVERRIDE) != 0) {
                            byte newProp3 = (byte) (level & 1);
                            pLastIsoRun.lastStrong = newProp3;
                            short flag = (short) DirPropFlag(newProp3);
                            for (int i = pLastIsoRun.start; i < idx; i++) {
                                Opening opening = bd.openings[i];
                                opening.flags = (short) (opening.flags | flag);
                            }
                            byte[] bArr = this.levels;
                            bArr[position] = (byte) (bArr[position] & LEVEL_DEFAULT_RTL);
                        }
                        byte[] bArr2 = this.levels;
                        int i2 = bd.openings[idx].position;
                        bArr2[i2] = (byte) (bArr2[i2] & LEVEL_DEFAULT_RTL);
                        return;
                    }
                }
            }
            if (c != 0) {
                match = (char) UCharacter.getBidiPairedBracket(c);
            } else {
                match = 0;
            }
            if (match != c && UCharacter.getIntPropertyValue(c, UProperty.BIDI_PAIRED_BRACKET_TYPE) == 1) {
                if (match == 9002) {
                    bracketAddOpening(bd, 12297, position);
                } else if (match == 12297) {
                    bracketAddOpening(bd, 9002, position);
                }
                bracketAddOpening(bd, match, position);
            }
        }
        byte level2 = this.levels[position];
        if ((level2 & LEVEL_OVERRIDE) != 0) {
            newProp = (byte) (level2 & 1);
            if (!(dirProp == 8 || dirProp == 9 || dirProp == 10)) {
                this.dirProps[position] = newProp;
            }
            pLastIsoRun.lastBase = newProp;
            pLastIsoRun.lastStrong = newProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        } else if (dirProp <= 1 || dirProp == 13) {
            newProp = DirFromStrong(dirProp);
            pLastIsoRun.lastBase = dirProp;
            pLastIsoRun.lastStrong = dirProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        } else if (dirProp == 2) {
            pLastIsoRun.lastBase = 2;
            if (pLastIsoRun.lastStrong == 0) {
                if (!bd.isNumbersSpecial) {
                    this.dirProps[position] = 23;
                }
                pLastIsoRun.contextDir = 0;
                pLastIsoRun.contextPos = position;
                newProp = 0;
            } else {
                newProp = 1;
                if (pLastIsoRun.lastStrong == 13) {
                    this.dirProps[position] = 5;
                } else {
                    this.dirProps[position] = 24;
                }
                pLastIsoRun.contextDir = 1;
                pLastIsoRun.contextPos = position;
            }
        } else if (dirProp == 5) {
            newProp = 1;
            pLastIsoRun.lastBase = 5;
            pLastIsoRun.contextDir = 1;
            pLastIsoRun.contextPos = position;
        } else if (dirProp == 17) {
            newProp = pLastIsoRun.lastBase;
            if (newProp == 10) {
                this.dirProps[position] = newProp;
            }
        } else {
            newProp = dirProp;
            pLastIsoRun.lastBase = dirProp;
        }
        if (newProp <= 1 || newProp == 13) {
            short flag2 = (short) DirPropFlag(DirFromStrong(newProp));
            for (int i3 = pLastIsoRun.start; i3 < pLastIsoRun.limit; i3++) {
                if (position > bd.openings[i3].position) {
                    Opening opening2 = bd.openings[i3];
                    opening2.flags = (short) (opening2.flags | flag2);
                }
            }
        }
    }

    private byte directionFromFlags() {
        if ((this.flags & MASK_RTL) == 0 && ((this.flags & DirPropFlag((byte) 5)) == 0 || (this.flags & MASK_POSSIBLE_N) == 0)) {
            return 0;
        }
        if ((this.flags & MASK_LTR) == 0) {
            return 1;
        }
        return 2;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private byte resolveExplicitLevels() {
        byte b;
        byte newLevel;
        byte newLevel2;
        int start;
        int start2;
        int paraIndex = 0;
        byte level = GetParaLevelAt(0);
        this.isolateCount = 0;
        byte dirct = directionFromFlags();
        if (dirct != 2) {
            return dirct;
        }
        byte newLevel3 = 1;
        if (this.reorderingMode > 1) {
            while (paraIndex < this.paraCount) {
                if (paraIndex == 0) {
                    start2 = 0;
                } else {
                    start2 = this.paras_limit[paraIndex - 1];
                }
                int limit = this.paras_limit[paraIndex];
                byte level2 = this.paras_level[paraIndex];
                for (int i = start2; i < limit; i++) {
                    this.levels[i] = level2;
                }
                paraIndex++;
            }
            return dirct;
        } else if ((this.flags & (MASK_EXPLICIT | MASK_ISO)) == 0) {
            BracketData bracketData = new BracketData();
            bracketInit(bracketData);
            while (paraIndex < this.paraCount) {
                if (paraIndex == 0) {
                    start = 0;
                } else {
                    start = this.paras_limit[paraIndex - 1];
                }
                int limit2 = this.paras_limit[paraIndex];
                byte level3 = this.paras_level[paraIndex];
                for (int i2 = start; i2 < limit2; i2++) {
                    this.levels[i2] = level3;
                    byte dirProp = this.dirProps[i2];
                    if (dirProp != 18) {
                        if (dirProp != 7) {
                            bracketProcessChar(bracketData, i2);
                        } else if (i2 + 1 < this.length && !(this.text[i2] == 13 && this.text[i2 + 1] == 10)) {
                            bracketProcessB(bracketData, level3);
                        }
                    }
                }
                paraIndex++;
            }
            return dirct;
        } else {
            byte previousLevel = level;
            byte previousLevel2 = level;
            int lastCcPos = 0;
            short[] stack = new short[127];
            int stackLast = 0;
            int overflowIsolateCount = 0;
            int overflowEmbeddingCount = 0;
            int validIsolateCount = 0;
            BracketData bracketData2 = new BracketData();
            bracketInit(bracketData2);
            stack[0] = (short) level;
            this.flags = 0;
            int i3 = 0;
            while (i3 < this.length) {
                byte dirProp2 = this.dirProps[i3];
                switch (dirProp2) {
                    case 7:
                        b = newLevel3;
                        this.flags |= DirPropFlag((byte) 7);
                        this.levels[i3] = GetParaLevelAt(i3);
                        if (i3 + 1 < this.length && !(this.text[i3] == 13 && this.text[i3 + 1] == 10)) {
                            stackLast = 0;
                            byte previousLevel3 = GetParaLevelAt(i3 + 1);
                            previousLevel = previousLevel3;
                            stack[0] = (short) previousLevel;
                            bracketProcessB(bracketData2, previousLevel);
                            validIsolateCount = 0;
                            previousLevel2 = previousLevel3;
                            overflowEmbeddingCount = 0;
                            overflowIsolateCount = 0;
                            break;
                        }
                    case 11:
                    case 12:
                    case 14:
                    case 15:
                        this.flags |= DirPropFlag((byte) 18);
                        this.levels[i3] = previousLevel2;
                        if (dirProp2 == 11 || dirProp2 == 12) {
                            b = 1;
                            newLevel = (byte) ((previousLevel + 2) & 126);
                        } else {
                            b = 1;
                            newLevel = (byte) ((NoOverride(previousLevel) + 1) | 1);
                        }
                        if (newLevel > 125 || overflowIsolateCount != 0 || overflowEmbeddingCount != 0) {
                            if (overflowIsolateCount != 0) {
                                break;
                            } else {
                                overflowEmbeddingCount++;
                                break;
                            }
                        } else {
                            int lastCcPos2 = i3;
                            previousLevel = newLevel;
                            if (dirProp2 == 12 || dirProp2 == 15) {
                                previousLevel = (byte) (previousLevel | LEVEL_OVERRIDE);
                            }
                            stackLast++;
                            stack[stackLast] = (short) previousLevel;
                            lastCcPos = lastCcPos2;
                            continue;
                        }
                    case 16:
                        this.flags |= DirPropFlag((byte) 18);
                        this.levels[i3] = previousLevel2;
                        if (overflowIsolateCount <= 0) {
                            if (overflowEmbeddingCount > 0) {
                                overflowEmbeddingCount--;
                            } else if (stackLast > 0 && stack[stackLast] < 256) {
                                stackLast--;
                                previousLevel = (byte) stack[stackLast];
                                lastCcPos = i3;
                            }
                        }
                    case 18:
                        this.levels[i3] = previousLevel2;
                        this.flags |= DirPropFlag((byte) 18);
                    case 20:
                    case 21:
                        this.flags |= DirPropFlag((byte) 10) | DirPropFlagLR(previousLevel);
                        this.levels[i3] = NoOverride(previousLevel);
                        if (NoOverride(previousLevel) != NoOverride(previousLevel2)) {
                            bracketProcessBoundary(bracketData2, lastCcPos, previousLevel2, previousLevel);
                            this.flags |= DirPropFlagMultiRuns;
                        }
                        byte previousLevel4 = previousLevel;
                        if (dirProp2 == 20) {
                            newLevel2 = (byte) ((previousLevel + 2) & 126);
                        } else {
                            newLevel2 = (byte) ((NoOverride(previousLevel) + 1) | 1);
                        }
                        if (newLevel2 <= 125 && overflowIsolateCount == 0 && overflowEmbeddingCount == 0) {
                            this.flags |= DirPropFlag(dirProp2);
                            int lastCcPos3 = i3;
                            int validIsolateCount2 = validIsolateCount + 1;
                            if (validIsolateCount2 > this.isolateCount) {
                                this.isolateCount = validIsolateCount2;
                            }
                            previousLevel = newLevel2;
                            stackLast++;
                            stack[stackLast] = (short) (previousLevel + 256);
                            bracketProcessLRI_RLI(bracketData2, previousLevel);
                            lastCcPos = lastCcPos3;
                            validIsolateCount = validIsolateCount2;
                            b = 1;
                            previousLevel2 = previousLevel4;
                            continue;
                        } else {
                            this.dirProps[i3] = 9;
                            overflowIsolateCount++;
                            previousLevel2 = previousLevel4;
                        }
                    case 22:
                        if (NoOverride(previousLevel) != NoOverride(previousLevel2)) {
                            bracketProcessBoundary(bracketData2, lastCcPos, previousLevel2, previousLevel);
                            this.flags |= DirPropFlagMultiRuns;
                        }
                        if (overflowIsolateCount > 0) {
                            overflowIsolateCount--;
                            this.dirProps[i3] = 9;
                        } else if (validIsolateCount > 0) {
                            this.flags |= DirPropFlag((byte) 22);
                            lastCcPos = i3;
                            overflowEmbeddingCount = 0;
                            while (stack[stackLast] < 256) {
                                stackLast--;
                            }
                            stackLast--;
                            validIsolateCount--;
                            bracketProcessPDI(bracketData2);
                        } else {
                            this.dirProps[i3] = 9;
                        }
                        byte embeddingLevel = (byte) (stack[stackLast] & -257);
                        this.flags |= DirPropFlag((byte) 10) | DirPropFlagLR(embeddingLevel);
                        previousLevel = embeddingLevel;
                        this.levels[i3] = NoOverride(embeddingLevel);
                        previousLevel2 = previousLevel;
                        b = 1;
                        break;
                    default:
                        b = newLevel3;
                        if (NoOverride(previousLevel) != NoOverride(previousLevel2)) {
                            bracketProcessBoundary(bracketData2, lastCcPos, previousLevel2, previousLevel);
                            this.flags |= DirPropFlagMultiRuns;
                            if ((previousLevel & LEVEL_OVERRIDE) != 0) {
                                this.flags |= DirPropFlagO(previousLevel);
                            } else {
                                this.flags |= DirPropFlagE(previousLevel);
                            }
                        }
                        this.levels[i3] = previousLevel;
                        bracketProcessChar(bracketData2, i3);
                        this.flags |= DirPropFlag(this.dirProps[i3]);
                        previousLevel2 = previousLevel;
                        continue;
                }
                b = 1;
                i3++;
                newLevel3 = b;
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
        int isolateCount2 = 0;
        this.flags = 0;
        this.isolateCount = 0;
        int currentParaIndex = 0;
        int currentParaLimit = this.paras_limit[0];
        byte currentParaLevel = this.paraLevel;
        for (int i = 0; i < this.length; i++) {
            byte level = this.levels[i];
            byte dirProp = this.dirProps[i];
            if (dirProp == 20 || dirProp == 21) {
                isolateCount2++;
                if (isolateCount2 > this.isolateCount) {
                    this.isolateCount = isolateCount2;
                }
            } else if (dirProp == 22) {
                isolateCount2--;
            } else if (dirProp == 7) {
                isolateCount2 = 0;
            }
            if (this.defaultParaLevel != 0 && i == currentParaLimit && currentParaIndex + 1 < this.paraCount) {
                currentParaIndex++;
                currentParaLevel = this.paras_level[currentParaIndex];
                currentParaLimit = this.paras_limit[currentParaIndex];
            }
            int overrideFlag = level & -128;
            byte level2 = (byte) (level & LEVEL_DEFAULT_RTL);
            if (level2 < currentParaLevel || 125 < level2) {
                if (level2 != 0) {
                    throw new IllegalArgumentException("level " + level2 + " out of bounds at " + i);
                } else if (dirProp != 7) {
                    level2 = currentParaLevel;
                    this.levels[i] = (byte) (level2 | overrideFlag);
                }
            }
            if (overrideFlag != 0) {
                this.flags |= DirPropFlagO(level2);
            } else {
                this.flags |= DirPropFlagE(level2) | DirPropFlag(dirProp);
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
        this.insertPoints.size++;
    }

    private void setLevelsOutsideIsolates(int start, int limit, byte level) {
        int isolateCount2 = 0;
        for (int k = start; k < limit; k++) {
            byte dirProp = this.dirProps[k];
            if (dirProp == 22) {
                isolateCount2--;
            }
            if (isolateCount2 == 0) {
                this.levels[k] = level;
            }
            if (dirProp == 20 || dirProp == 21) {
                isolateCount2++;
            }
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private void processPropertySeq(LevState levState, short _prop, int start, int limit) {
        int start2;
        short[] impAct;
        byte[] bArr;
        LevState levState2 = levState;
        short s = _prop;
        int i = limit;
        byte[][] impTab = levState2.impTab;
        short[] impAct4 = levState2.impAct;
        int start0 = start;
        short oldStateSeq = levState2.state;
        byte cell = impTab[oldStateSeq][s];
        levState2.state = GetState(cell);
        short actionSeq = impAct4[GetAction(cell)];
        byte addLevel = impTab[levState2.state][7];
        if (actionSeq != 0) {
            switch (actionSeq) {
                case 1:
                    levState2.startON = start0;
                case 2:
                    start2 = levState2.startON;
                    break;
                case 3:
                    setLevelsOutsideIsolates(levState2.startON, start0, (byte) (levState2.runLevel + 1));
                case 4:
                    setLevelsOutsideIsolates(levState2.startON, start0, (byte) (levState2.runLevel + 2));
                case 5:
                    if (levState2.startL2EN >= 0) {
                        addPoint(levState2.startL2EN, 1);
                    }
                    levState2.startL2EN = -1;
                    if (this.insertPoints.points.length == 0 || this.insertPoints.size <= this.insertPoints.confirmed) {
                        levState2.lastStrongRTL = -1;
                        if ((impTab[oldStateSeq][7] & 1) == 0 || levState2.startON <= 0) {
                            start2 = start;
                        } else {
                            start2 = levState2.startON;
                        }
                        if (s == 5) {
                            addPoint(start0, 1);
                            this.insertPoints.confirmed = this.insertPoints.size;
                            break;
                        }
                    } else {
                        for (int k = levState2.lastStrongRTL + 1; k < start0; k++) {
                            this.levels[k] = (byte) ((this.levels[k] - 2) & -2);
                        }
                        this.insertPoints.confirmed = this.insertPoints.size;
                        levState2.lastStrongRTL = -1;
                        if (s == 5) {
                            addPoint(start0, 1);
                            this.insertPoints.confirmed = this.insertPoints.size;
                        }
                    }
                    break;
                case 6:
                    if (this.insertPoints.points.length > 0) {
                        this.insertPoints.size = this.insertPoints.confirmed;
                    }
                    levState2.startON = -1;
                    levState2.startL2EN = -1;
                    levState2.lastStrongRTL = i - 1;
                    start2 = start;
                    break;
                case 7:
                    if (s != 3 || this.dirProps[start0] != 5 || this.reorderingMode == 6) {
                        if (levState2.startL2EN == -1) {
                            levState2.startL2EN = start0;
                        }
                        start2 = start;
                        break;
                    } else {
                        if (levState2.startL2EN == -1) {
                            levState2.lastStrongRTL = i - 1;
                        } else {
                            if (levState2.startL2EN >= 0) {
                                addPoint(levState2.startL2EN, 1);
                                levState2.startL2EN = -2;
                            }
                            addPoint(start0, 1);
                        }
                        start2 = start;
                    }
                    break;
                case 8:
                    levState2.lastStrongRTL = i - 1;
                    levState2.startON = -1;
                    start2 = start;
                    break;
                case 9:
                    int k2 = start0 - 1;
                    while (k2 >= 0 && (this.levels[k2] & 1) == 0) {
                        k2--;
                    }
                    if (k2 >= 0) {
                        addPoint(k2, 4);
                        this.insertPoints.confirmed = this.insertPoints.size;
                    }
                    levState2.startON = start0;
                    start2 = start;
                    break;
                case 10:
                    addPoint(start0, 1);
                    addPoint(start0, 2);
                    start2 = start;
                    break;
                case 11:
                    this.insertPoints.size = this.insertPoints.confirmed;
                    if (s == 5) {
                        addPoint(start0, 4);
                        this.insertPoints.confirmed = this.insertPoints.size;
                    }
                    start2 = start;
                    break;
                case 12:
                    byte level = (byte) (levState2.runLevel + addLevel);
                    for (int k3 = levState2.startON; k3 < start0; k3++) {
                        if (this.levels[k3] < level) {
                            this.levels[k3] = level;
                        }
                    }
                    this.insertPoints.confirmed = this.insertPoints.size;
                    levState2.startON = start0;
                    start2 = start;
                    break;
                case 13:
                    byte level2 = levState2.runLevel;
                    int k4 = start0 - 1;
                    while (k4 >= levState2.startON) {
                        if (this.levels[k4] == level2 + 3) {
                            while (this.levels[k4] == level2 + 3) {
                                bArr[k4] = (byte) (this.levels[k4] - 2);
                                k4--;
                                impAct4 = impAct4;
                            }
                            impAct = impAct4;
                            while (this.levels[k4] == level2) {
                                k4--;
                            }
                        } else {
                            impAct = impAct4;
                        }
                        if (this.levels[k4] == level2 + 2) {
                            this.levels[k4] = level2;
                        } else {
                            this.levels[k4] = (byte) (level2 + 1);
                        }
                        k4--;
                        impAct4 = impAct;
                    }
                    break;
                case 14:
                    byte level3 = (byte) (levState2.runLevel + 1);
                    for (int k5 = start0 - 1; k5 >= levState2.startON; k5--) {
                        if (this.levels[k5] > level3) {
                            byte[] bArr2 = this.levels;
                            bArr2[k5] = (byte) (bArr2[k5] - 2);
                        }
                    }
                    short[] sArr = impAct4;
                    start2 = start;
                    break;
                default:
                    short[] sArr2 = impAct4;
                    throw new IllegalStateException("Internal ICU error in processPropertySeq");
            }
        }
        start2 = start;
        if (addLevel != 0 || start2 < start0) {
            byte level4 = (byte) (levState2.runLevel + addLevel);
            if (start2 >= levState2.runStart) {
                for (int k6 = start2; k6 < i; k6++) {
                    this.levels[k6] = level4;
                }
                return;
            }
            setLevelsOutsideIsolates(start2, i, level4);
        }
    }

    private byte lastL_R_AL() {
        int i = this.prologue.length();
        while (i > 0) {
            int uchar = this.prologue.codePointBefore(i);
            i -= Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (dirProp == 0) {
                return 0;
            }
            if (dirProp == 1 || dirProp == 13) {
                return 1;
            }
            if (dirProp == 7) {
                return 4;
            }
        }
        return 4;
    }

    private byte firstL_R_AL_EN_AN() {
        int i = 0;
        while (i < this.epilogue.length()) {
            int uchar = this.epilogue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (dirProp == 0) {
                return 0;
            }
            if (dirProp == 1 || dirProp == 13) {
                return 1;
            }
            if (dirProp == 2) {
                return 2;
            }
            if (dirProp == 5) {
                return 3;
            }
        }
        return 4;
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=short, code=int, for r8v13, types: [short] */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x01c1  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x017e A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0064  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x008e  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x018f  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0197  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01ad A[ADDED_TO_REGION] */
    private void resolveImplicitLevels(int start, int limit, short sor, short eor) {
        short sor2;
        short stateImp;
        int start1;
        int i;
        short eor2;
        int i2;
        byte dirProp;
        byte firstStrong;
        int k;
        boolean inverseRTL;
        int nextStrongPos;
        int start12;
        int k2;
        short stateImp2;
        int start2 = start;
        int i3 = limit;
        LevState levState = new LevState();
        int nextStrongPos2 = -1;
        boolean inverseRTL2 = start2 < this.lastArabicPos && (GetParaLevelAt(start) & 1) > 0 && (this.reorderingMode == 5 || this.reorderingMode == 6);
        levState.startL2EN = -1;
        levState.lastStrongRTL = -1;
        levState.runStart = start2;
        levState.runLevel = this.levels[start2];
        levState.impTab = this.impTabPair.imptab[levState.runLevel & 1];
        levState.impAct = this.impTabPair.impact[levState.runLevel & 1];
        if (start2 == 0 && this.prologue != null) {
            byte lastStrong = lastL_R_AL();
            if (lastStrong != 4) {
                sor2 = (short) lastStrong;
                if (this.dirProps[start2] != 22) {
                    levState.startON = this.isolates[this.isolateCount].startON;
                    start1 = this.isolates[this.isolateCount].start1;
                    stateImp = this.isolates[this.isolateCount].stateImp;
                    levState.state = this.isolates[this.isolateCount].state;
                    this.isolateCount--;
                } else {
                    levState.startON = -1;
                    int start13 = start2;
                    if (this.dirProps[start2] == 17) {
                        stateImp2 = (short) (1 + sor2);
                    } else {
                        stateImp2 = 0;
                    }
                    levState.state = 0;
                    processPropertySeq(levState, sor2, start2, start2);
                    start1 = start13;
                    stateImp = stateImp2;
                }
                int start22 = start2;
                int start14 = start1;
                short nextStrongProp = 1;
                i = start2;
                while (true) {
                    if (i > i3) {
                        if (i >= i3) {
                            int k3 = i3 - 1;
                            while (true) {
                                k2 = k3;
                                if (k2 <= start2 || (DirPropFlag(this.dirProps[k2]) & MASK_BN_EXPLICIT) == 0) {
                                    byte dirProp2 = this.dirProps[k2];
                                } else {
                                    k3 = k2 - 1;
                                }
                            }
                            byte dirProp22 = this.dirProps[k2];
                            if (dirProp22 == 20 || dirProp22 == 21) {
                            } else {
                                k = eor;
                            }
                        } else {
                            byte prop = this.dirProps[i];
                            if (prop == 7) {
                                this.isolateCount = -1;
                            }
                            if (inverseRTL2) {
                                if (prop == 13) {
                                    prop = 1;
                                } else if (prop == 2) {
                                    if (nextStrongPos2 <= i) {
                                        nextStrongProp = 1;
                                        nextStrongPos2 = i3;
                                        int j = i + 1;
                                        while (true) {
                                            if (j < i3) {
                                                byte prop1 = this.dirProps[j];
                                                if (prop1 == 0 || prop1 == 1 || prop1 == 13) {
                                                    nextStrongProp = (short) prop1;
                                                    nextStrongPos2 = j;
                                                } else {
                                                    j++;
                                                }
                                            }
                                        }
                                    }
                                    if (nextStrongProp == 13) {
                                        prop = 5;
                                    }
                                }
                            }
                            k = groupProp[prop];
                        }
                        short oldStateImp = stateImp;
                        short cell = impTabProps[oldStateImp][k];
                        stateImp = GetStateProps(cell);
                        short actionImp = GetActionProps(cell);
                        if (i == i3 && actionImp == 0) {
                            actionImp = 1;
                        }
                        if (actionImp != 0) {
                            nextStrongPos = nextStrongPos2;
                            short resProp = impTabProps[oldStateImp][15];
                            switch (actionImp) {
                                case 1:
                                    inverseRTL = inverseRTL2;
                                    processPropertySeq(levState, resProp, start14, i);
                                    start12 = i;
                                    break;
                                case 2:
                                    inverseRTL = inverseRTL2;
                                    start22 = i;
                                    continue;
                                case 3:
                                    processPropertySeq(levState, resProp, start14, start22);
                                    inverseRTL = inverseRTL2;
                                    processPropertySeq(levState, 4, start22, i);
                                    start12 = i;
                                    break;
                                case 4:
                                    processPropertySeq(levState, resProp, start14, start22);
                                    start14 = start22;
                                    start22 = i;
                                    inverseRTL = inverseRTL2;
                                    continue;
                                default:
                                    boolean z = inverseRTL2;
                                    short s = resProp;
                                    throw new IllegalStateException("Internal ICU error in resolveImplicitLevels");
                            }
                            start14 = start12;
                        } else {
                            nextStrongPos = nextStrongPos2;
                            inverseRTL = inverseRTL2;
                        }
                        i++;
                        nextStrongPos2 = nextStrongPos;
                        inverseRTL2 = inverseRTL;
                    }
                }
                if (i3 == this.length && this.epilogue != null) {
                    firstStrong = firstL_R_AL_EN_AN();
                    if (firstStrong != 4) {
                        eor2 = (short) firstStrong;
                        i2 = i3 - 1;
                        while (i2 > start2 && (DirPropFlag(this.dirProps[i2]) & MASK_BN_EXPLICIT) != 0) {
                            i2--;
                        }
                        dirProp = this.dirProps[i2];
                        if ((dirProp != 20 || dirProp == 21) && i3 < this.length) {
                            this.isolateCount++;
                            if (this.isolates[this.isolateCount] == null) {
                                this.isolates[this.isolateCount] = new Isolate();
                            }
                            this.isolates[this.isolateCount].stateImp = stateImp;
                            this.isolates[this.isolateCount].state = levState.state;
                            this.isolates[this.isolateCount].start1 = start14;
                            this.isolates[this.isolateCount].startON = levState.startON;
                        }
                        processPropertySeq(levState, eor2, i3, i3);
                        return;
                    }
                }
                eor2 = eor;
                i2 = i3 - 1;
                while (i2 > start2) {
                    i2--;
                }
                dirProp = this.dirProps[i2];
                if (dirProp != 20) {
                }
                this.isolateCount++;
                if (this.isolates[this.isolateCount] == null) {
                }
                this.isolates[this.isolateCount].stateImp = stateImp;
                this.isolates[this.isolateCount].state = levState.state;
                this.isolates[this.isolateCount].start1 = start14;
                this.isolates[this.isolateCount].startON = levState.startON;
            }
        }
        sor2 = sor;
        if (this.dirProps[start2] != 22) {
        }
        int start222 = start2;
        int start142 = start1;
        short nextStrongProp2 = 1;
        i = start2;
        while (true) {
            if (i > i3) {
            }
            i++;
            nextStrongPos2 = nextStrongPos;
            inverseRTL2 = inverseRTL;
        }
        firstStrong = firstL_R_AL_EN_AN();
        if (firstStrong != 4) {
        }
        eor2 = eor;
        i2 = i3 - 1;
        while (i2 > start2) {
        }
        dirProp = this.dirProps[i2];
        if (dirProp != 20) {
        }
        this.isolateCount++;
        if (this.isolates[this.isolateCount] == null) {
        }
        this.isolates[this.isolateCount].stateImp = stateImp;
        this.isolates[this.isolateCount].state = levState.state;
        this.isolates[this.isolateCount].start1 = start142;
        this.isolates[this.isolateCount].startON = levState.startON;
    }

    private void adjustWSLevels() {
        if ((this.flags & MASK_WS) != 0) {
            int i = this.trailingWSStart;
            while (i > 0) {
                while (i > 0) {
                    i--;
                    int DirPropFlag = DirPropFlag(this.dirProps[i]);
                    int flag = DirPropFlag;
                    if ((DirPropFlag & MASK_WS) == 0) {
                        break;
                    } else if (!this.orderParagraphsLTR || (DirPropFlag((byte) 7) & flag) == 0) {
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
                    int flag2 = DirPropFlag(this.dirProps[i]);
                    if ((MASK_BN_EXPLICIT & flag2) == 0) {
                        if (this.orderParagraphsLTR && (DirPropFlag((byte) 7) & flag2) != 0) {
                            this.levels[i] = 0;
                            break;
                        } else if ((MASK_B_S & flag2) != 0) {
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

    public void setContext(String prologue2, String epilogue2) {
        String str = null;
        this.prologue = (prologue2 == null || prologue2.length() <= 0) ? null : prologue2;
        if (epilogue2 != null && epilogue2.length() > 0) {
            str = epilogue2;
        }
        this.epilogue = str;
    }

    private void setParaSuccess() {
        this.prologue = null;
        this.epilogue = null;
        this.paraBidi = this;
    }

    /* access modifiers changed from: package-private */
    public int Bidi_Min(int x, int y) {
        return x < y ? x : y;
    }

    /* access modifiers changed from: package-private */
    public int Bidi_Abs(int x) {
        return x >= 0 ? x : -x;
    }

    /* access modifiers changed from: package-private */
    public void setParaRunsOnly(char[] parmText, byte parmParaLevel) {
        int runLength;
        int saveTrailingWSStart;
        int oldRunCount;
        String visualText;
        int step;
        int limit;
        int start;
        int saveTrailingWSStart2;
        byte parmParaLevel2;
        char[] cArr = parmText;
        byte b = parmParaLevel;
        this.reorderingMode = 0;
        int parmLength = cArr.length;
        if (parmLength == 0) {
            setPara(cArr, b, (byte[]) null);
            this.reorderingMode = 3;
            return;
        }
        int saveOptions = this.reorderingOptions;
        int i = 2;
        if ((saveOptions & 1) > 0) {
            this.reorderingOptions &= -2;
            this.reorderingOptions |= 2;
        }
        byte parmParaLevel3 = (byte) (b & 1);
        setPara(cArr, parmParaLevel3, (byte[]) null);
        byte[] saveLevels = new byte[this.length];
        System.arraycopy(getLevels(), 0, saveLevels, 0, this.length);
        int saveTrailingWSStart3 = this.trailingWSStart;
        String visualText2 = writeReordered(2);
        int[] visualMap = getVisualMap();
        this.reorderingOptions = saveOptions;
        int saveLength = this.length;
        byte saveDirection = this.direction;
        this.reorderingMode = 5;
        byte parmParaLevel4 = (byte) (parmParaLevel3 ^ 1);
        setPara(visualText2, parmParaLevel4, (byte[]) null);
        BidiLine.getRuns(this);
        int oldRunCount2 = this.runCount;
        int visualStart = 0;
        int addedRuns = 0;
        int i2 = 0;
        while (i2 < oldRunCount2) {
            int runLength2 = this.runs[i2].limit - visualStart;
            if (runLength2 >= i) {
                int logicalStart = this.runs[i2].start;
                int index1 = logicalStart + 1;
                while (true) {
                    parmParaLevel2 = parmParaLevel4;
                    int j = index1;
                    if (j >= logicalStart + runLength2) {
                        break;
                    }
                    int index = visualMap[j];
                    int index12 = visualMap[j - 1];
                    int saveOptions2 = saveOptions;
                    int logicalStart2 = logicalStart;
                    if (Bidi_Abs(index - index12) != 1 || saveLevels[index] != saveLevels[index12]) {
                        addedRuns++;
                    }
                    index1 = j + 1;
                    parmParaLevel4 = parmParaLevel2;
                    saveOptions = saveOptions2;
                    logicalStart = logicalStart2;
                }
            } else {
                parmParaLevel2 = parmParaLevel4;
            }
            i2++;
            visualStart += runLength2;
            parmParaLevel4 = parmParaLevel2;
            saveOptions = saveOptions;
            i = 2;
        }
        int i3 = saveOptions;
        if (addedRuns > 0) {
            getRunsMemory(oldRunCount2 + addedRuns);
            if (this.runCount == 1) {
                this.runsMemory[0] = this.runs[0];
            } else {
                System.arraycopy(this.runs, 0, this.runsMemory, 0, this.runCount);
            }
            this.runs = this.runsMemory;
            this.runCount += addedRuns;
            for (int i4 = oldRunCount2; i4 < this.runCount; i4++) {
                if (this.runs[i4] == null) {
                    this.runs[i4] = new BidiRun(0, 0, (byte) 0);
                }
            }
        }
        int i5 = oldRunCount2 - 1;
        while (i5 >= 0) {
            int newI = i5 + addedRuns;
            if (i5 == 0) {
                runLength = this.runs[0].limit;
            } else {
                runLength = this.runs[i5].limit - this.runs[i5 - 1].limit;
            }
            int logicalStart3 = this.runs[i5].start;
            int indexOddBit = this.runs[i5].level & 1;
            if (runLength < 2) {
                if (addedRuns > 0) {
                    visualText = visualText2;
                    this.runs[newI].copyFrom(this.runs[i5]);
                } else {
                    visualText = visualText2;
                }
                int logicalPos = visualMap[logicalStart3];
                this.runs[newI].start = logicalPos;
                int i6 = newI;
                this.runs[newI].level = (byte) (saveLevels[logicalPos] ^ indexOddBit);
                int i7 = runLength;
                int runLength3 = logicalPos;
                int i8 = logicalStart3;
                saveTrailingWSStart = saveTrailingWSStart3;
                oldRunCount = oldRunCount2;
            } else {
                int newI2 = newI;
                visualText = visualText2;
                if (indexOddBit > 0) {
                    start = logicalStart3;
                    limit = (logicalStart3 + runLength) - 1;
                    step = 1;
                } else {
                    start = (logicalStart3 + runLength) - 1;
                    limit = logicalStart3;
                    step = -1;
                }
                int step2 = step;
                int start2 = start;
                while (start != limit) {
                    int runLength4 = runLength;
                    int runLength5 = visualMap[start];
                    int index13 = visualMap[start + step2];
                    int logicalStart4 = logicalStart3;
                    int oldRunCount3 = oldRunCount2;
                    if (Bidi_Abs(runLength5 - index13) == 1 && saveLevels[runLength5] == saveLevels[index13]) {
                        int i9 = runLength5;
                        saveTrailingWSStart2 = saveTrailingWSStart3;
                    } else {
                        int logicalPos2 = Bidi_Min(visualMap[start2], runLength5);
                        this.runs[newI2].start = logicalPos2;
                        int i10 = runLength5;
                        this.runs[newI2].level = (byte) (saveLevels[logicalPos2] ^ indexOddBit);
                        this.runs[newI2].limit = this.runs[i5].limit;
                        int i11 = logicalPos2;
                        this.runs[i5].limit -= Bidi_Abs(start - start2) + 1;
                        int insertRemove = this.runs[i5].insertRemove & 10;
                        this.runs[newI2].insertRemove = insertRemove;
                        BidiRun bidiRun = this.runs[i5];
                        saveTrailingWSStart2 = saveTrailingWSStart3;
                        bidiRun.insertRemove = (~insertRemove) & bidiRun.insertRemove;
                        addedRuns--;
                        newI2--;
                        start2 = start + step2;
                    }
                    start += step2;
                    runLength = runLength4;
                    logicalStart3 = logicalStart4;
                    oldRunCount2 = oldRunCount3;
                    saveTrailingWSStart3 = saveTrailingWSStart2;
                }
                int i12 = logicalStart3;
                saveTrailingWSStart = saveTrailingWSStart3;
                oldRunCount = oldRunCount2;
                if (addedRuns > 0) {
                    this.runs[newI2].copyFrom(this.runs[i5]);
                }
                int logicalPos3 = Bidi_Min(visualMap[start2], visualMap[limit]);
                this.runs[newI2].start = logicalPos3;
                this.runs[newI2].level = (byte) (saveLevels[logicalPos3] ^ indexOddBit);
            }
            i5--;
            visualText2 = visualText;
            oldRunCount2 = oldRunCount;
            saveTrailingWSStart3 = saveTrailingWSStart;
        }
        String str = visualText2;
        int i13 = oldRunCount2;
        this.paraLevel = (byte) (this.paraLevel ^ 1);
        this.text = cArr;
        this.length = saveLength;
        this.originalLength = parmLength;
        this.direction = saveDirection;
        this.levels = saveLevels;
        this.trailingWSStart = saveTrailingWSStart3;
        if (this.runCount > 1) {
            this.direction = 2;
        }
        this.reorderingMode = 3;
    }

    public void setPara(String text2, byte paraLevel2, byte[] embeddingLevels) {
        if (text2 == null) {
            setPara(new char[0], paraLevel2, embeddingLevels);
        } else {
            setPara(text2.toCharArray(), paraLevel2, embeddingLevels);
        }
    }

    public void setPara(char[] chars, byte paraLevel2, byte[] embeddingLevels) {
        short eor;
        short sor;
        short eor2;
        if (paraLevel2 < 126) {
            verifyRange(paraLevel2, 0, 126);
        }
        if (chars == null) {
            chars = new char[0];
        }
        if (this.reorderingMode == 3) {
            setParaRunsOnly(chars, paraLevel2);
            return;
        }
        this.paraBidi = null;
        this.text = chars;
        int length2 = this.text.length;
        this.resultLength = length2;
        this.originalLength = length2;
        this.length = length2;
        this.paraLevel = paraLevel2;
        this.direction = (byte) (paraLevel2 & 1);
        this.paraCount = 1;
        this.dirProps = new byte[0];
        this.levels = new byte[0];
        this.runs = new BidiRun[0];
        this.isGoodLogicalToVisualRunsMap = false;
        this.insertPoints.size = 0;
        this.insertPoints.confirmed = 0;
        this.defaultParaLevel = IsDefaultLevel(paraLevel2) ? paraLevel2 : 0;
        if (this.length == 0) {
            if (IsDefaultLevel(paraLevel2)) {
                this.paraLevel = (byte) (1 & this.paraLevel);
                this.defaultParaLevel = 0;
            }
            this.flags = DirPropFlagLR(paraLevel2);
            this.runCount = 0;
            this.paraCount = 0;
            setParaSuccess();
            return;
        }
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
            case 0:
                this.trailingWSStart = 0;
                break;
            case 1:
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
                    int limit = 0;
                    byte level = GetParaLevelAt(0);
                    byte nextLevel = this.levels[0];
                    if (level < nextLevel) {
                        eor = (short) GetLRFromLevel(nextLevel);
                    } else {
                        eor = (short) GetLRFromLevel(level);
                    }
                    do {
                        int start = limit;
                        byte level2 = nextLevel;
                        if (start <= 0 || this.dirProps[start - 1] != 7) {
                            sor = eor;
                        } else {
                            sor = (short) GetLRFromLevel(GetParaLevelAt(start));
                        }
                        while (true) {
                            limit++;
                            if (limit >= this.length || (this.levels[limit] != level2 && (DirPropFlag(this.dirProps[limit]) & MASK_BN_EXPLICIT) == 0)) {
                            }
                        }
                        if (limit < this.length) {
                            nextLevel = this.levels[limit];
                        } else {
                            nextLevel = GetParaLevelAt(this.length - 1);
                        }
                        if (NoOverride(level2) < NoOverride(nextLevel)) {
                            eor2 = (short) GetLRFromLevel(nextLevel);
                        } else {
                            eor2 = (short) GetLRFromLevel(level2);
                        }
                        if ((level2 & LEVEL_OVERRIDE) == 0) {
                            resolveImplicitLevels(start, limit, sor, eor);
                            int i = start;
                        } else {
                            while (true) {
                                byte[] bArr = this.levels;
                                int start2 = start + 1;
                                bArr[start] = (byte) (bArr[start] & LEVEL_DEFAULT_RTL);
                                if (start2 < limit) {
                                    start = start2;
                                }
                            }
                        }
                    } while (limit < this.length);
                }
                adjustWSLevels();
                break;
        }
        if (this.defaultParaLevel > 0 && (this.reorderingOptions & 1) != 0 && (this.reorderingMode == 5 || this.reorderingMode == 6)) {
            int i2 = 0;
            while (i2 < this.paraCount) {
                int last = this.paras_limit[i2] - 1;
                if (this.paras_level[i2] != 0) {
                    int start3 = i2 == 0 ? 0 : this.paras_limit[i2 - 1];
                    int j = last;
                    while (true) {
                        if (j >= start3) {
                            byte dirProp = this.dirProps[j];
                            if (dirProp == 0) {
                                if (j < last) {
                                    while (this.dirProps[last] == 7) {
                                        last--;
                                    }
                                }
                                addPoint(last, 4);
                            } else if ((DirPropFlag(dirProp) & MASK_R_AL) == 0) {
                                j--;
                            }
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
        byte paraLvl;
        Boolean runDirection = (Boolean) paragraph.getAttribute(TextAttribute.RUN_DIRECTION);
        if (runDirection == null) {
            paraLvl = LEVEL_DEFAULT_LTR;
        } else {
            paraLvl = runDirection.equals(TextAttribute.RUN_DIRECTION_LTR) ? (byte) 0 : 1;
        }
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
                if (level != 0) {
                    if (level < 0) {
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
        int paraIndex = 0;
        verifyRange(charIndex, 0, bidi.length);
        while (true) {
            int paraIndex2 = paraIndex;
            if (charIndex < bidi.paras_limit[paraIndex2]) {
                return getParagraphByIndex(paraIndex2);
            }
            paraIndex = paraIndex2 + 1;
        }
    }

    public int getParagraphIndex(int charIndex) {
        verifyValidParaOrLine();
        Bidi bidi = this.paraBidi;
        int paraIndex = 0;
        verifyRange(charIndex, 0, bidi.length);
        while (true) {
            int paraIndex2 = paraIndex;
            if (charIndex < bidi.paras_limit[paraIndex2]) {
                return paraIndex2;
            }
            paraIndex = paraIndex2 + 1;
        }
    }

    public void setCustomClassifier(BidiClassifier classifier) {
        this.customClassifier = classifier;
    }

    public BidiClassifier getCustomClassifier() {
        return this.customClassifier;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000d, code lost:
        if (r0 == 23) goto L_0x000f;
     */
    public int getCustomizedClass(int c) {
        int dir;
        if (this.customClassifier != null) {
            int classify = this.customClassifier.classify(c);
            dir = classify;
        }
        dir = this.bdp.getClass(c);
        int dir2 = dir;
        if (dir2 >= 23) {
            return 10;
        }
        return dir2;
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
            if (this.direction == 0) {
                return visualIndex;
            }
            if (this.direction == 1) {
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

    public static int[] reorderLogical(byte[] levels2) {
        return BidiLine.reorderLogical(levels2);
    }

    public static int[] reorderVisual(byte[] levels2) {
        return BidiLine.reorderVisual(levels2);
    }

    public static int[] invertMap(int[] srcMap) {
        if (srcMap == null) {
            return null;
        }
        return BidiLine.invertMap(srcMap);
    }

    public Bidi(String paragraph, int flags2) {
        this(paragraph.toCharArray(), 0, null, 0, paragraph.length(), flags2);
    }

    public Bidi(AttributedCharacterIterator paragraph) {
        this();
        setPara(paragraph);
    }

    public Bidi(char[] text2, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags2) {
        this();
        byte paraLvl;
        byte[] paraEmbeddings;
        if (flags2 != 1) {
            switch (flags2) {
                case 126:
                    paraLvl = LEVEL_DEFAULT_LTR;
                    break;
                case 127:
                    paraLvl = LEVEL_DEFAULT_RTL;
                    break;
                default:
                    paraLvl = 0;
                    break;
            }
        } else {
            paraLvl = 1;
        }
        if (embeddings == null) {
            paraEmbeddings = null;
        } else {
            paraEmbeddings = new byte[paragraphLength];
            for (int i = 0; i < paragraphLength; i++) {
                byte lev = embeddings[i + embStart];
                if (lev < 0) {
                    lev = (byte) ((-lev) | -128);
                }
                paraEmbeddings[i] = lev;
            }
        }
        if (textStart == 0 && paragraphLength == text2.length) {
            setPara(text2, paraLvl, paraEmbeddings);
            return;
        }
        char[] paraText = new char[paragraphLength];
        System.arraycopy(text2, textStart, paraText, 0, paragraphLength);
        setPara(paraText, paraLvl, paraEmbeddings);
    }

    public Bidi createLineBidi(int lineStart, int lineLimit) {
        return setLine(lineStart, lineLimit);
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
            int count = countRuns();
            if (this.logicalToVisualRunsMap == null || this.logicalToVisualRunsMap.length < count) {
                this.logicalToVisualRunsMap = new int[count];
            }
            long[] keys = new long[count];
            for (int i = 0; i < count; i++) {
                keys[i] = (((long) this.runs[i].start) << 32) + ((long) i);
            }
            Arrays.sort(keys);
            for (int i2 = 0; i2 < count; i2++) {
                this.logicalToVisualRunsMap[i2] = (int) (keys[i2] & -1);
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

    public static boolean requiresBidi(char[] text2, int start, int limit) {
        for (int i = start; i < limit; i++) {
            if (((1 << UCharacter.getDirection(text2[i])) & 57378) != 0) {
                return true;
            }
        }
        return false;
    }

    public static void reorderVisually(byte[] levels2, int levelStart, Object[] objects, int objectStart, int count) {
        byte[] reorderLevels = new byte[count];
        System.arraycopy(levels2, levelStart, reorderLevels, 0, count);
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
