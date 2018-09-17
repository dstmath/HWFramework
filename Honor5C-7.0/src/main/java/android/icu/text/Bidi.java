package android.icu.text;

import android.icu.impl.UBiDiProps;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import dalvik.bytecode.Opcodes;
import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.util.Arrays;
import libcore.icu.ICU;
import libcore.io.IoBridge;
import org.xmlpull.v1.XmlPullParser;

public class Bidi {
    static final byte AL = (byte) 13;
    static final byte AN = (byte) 5;
    static final byte B = (byte) 7;
    static final byte BN = (byte) 18;
    public static final int CLASS_DEFAULT = 23;
    private static final char CR = '\r';
    static final byte CS = (byte) 6;
    public static final int DIRECTION_DEFAULT_LEFT_TO_RIGHT = 126;
    public static final int DIRECTION_DEFAULT_RIGHT_TO_LEFT = 127;
    public static final int DIRECTION_LEFT_TO_RIGHT = 0;
    public static final int DIRECTION_RIGHT_TO_LEFT = 1;
    public static final short DO_MIRRORING = (short) 2;
    static final int[] DirPropFlagE = null;
    static final int[] DirPropFlagLR = null;
    static final int DirPropFlagMultiRuns = 0;
    static final int[] DirPropFlagO = null;
    static final byte EN = (byte) 2;
    static final byte ENL = (byte) 23;
    static final byte ENR = (byte) 24;
    static final byte ES = (byte) 3;
    static final byte ET = (byte) 4;
    static final int FIRSTALLOC = 10;
    static final byte FOUND_L = (byte) 0;
    static final byte FOUND_R = (byte) 0;
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
    static final int MASK_BN_EXPLICIT = 0;
    static final int MASK_B_S = 0;
    static final int MASK_EMBEDDING = 0;
    static final int MASK_EXPLICIT = 0;
    static final int MASK_ISO = 0;
    static final int MASK_LTR = 0;
    static final int MASK_POSSIBLE_N = 0;
    static final int MASK_RTL = 0;
    static final int MASK_R_AL = 0;
    static final int MASK_STRONG_EN_AN = 0;
    static final int MASK_WS = 0;
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
    private static final short[] groupProp = null;
    private static final short[] impAct0 = null;
    private static final short[] impAct1 = null;
    private static final short[] impAct2 = null;
    private static final short[] impAct3 = null;
    private static final byte[][] impTabL_DEFAULT = null;
    private static final byte[][] impTabL_GROUP_NUMBERS_WITH_R = null;
    private static final byte[][] impTabL_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = null;
    private static final byte[][] impTabL_INVERSE_LIKE_DIRECT_WITH_MARKS = null;
    private static final byte[][] impTabL_INVERSE_NUMBERS_AS_L = null;
    private static final byte[][] impTabL_NUMBERS_SPECIAL = null;
    private static final short[][] impTabProps = null;
    private static final byte[][] impTabR_DEFAULT = null;
    private static final byte[][] impTabR_GROUP_NUMBERS_WITH_R = null;
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT = null;
    private static final byte[][] impTabR_INVERSE_LIKE_DIRECT_WITH_MARKS = null;
    private static final byte[][] impTabR_INVERSE_NUMBERS_AS_L = null;
    private static final ImpTabPair impTab_DEFAULT = null;
    private static final ImpTabPair impTab_GROUP_NUMBERS_WITH_R = null;
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL = null;
    private static final ImpTabPair impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS = null;
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT = null;
    private static final ImpTabPair impTab_INVERSE_LIKE_DIRECT_WITH_MARKS = null;
    private static final ImpTabPair impTab_INVERSE_NUMBERS_AS_L = null;
    private static final ImpTabPair impTab_NUMBERS_SPECIAL = null;
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
        IsoRun[] isoRuns;
        Opening[] openings;

        BracketData() {
            this.openings = new Opening[Bidi.SIMPLE_OPENINGS_COUNT];
            this.isoRuns = new IsoRun[Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT];
        }
    }

    private static class ImpTabPair {
        short[][] impact;
        byte[][][] imptab;

        ImpTabPair(byte[][] table1, byte[][] table2, short[] act1, short[] act2) {
            byte[][][] bArr = new byte[Bidi.SEEKING_STRONG_FOR_FSI][][];
            bArr[Bidi.OPTION_DEFAULT] = table1;
            bArr[Bidi.SEEKING_STRONG_FOR_PARA] = table2;
            this.imptab = bArr;
            short[][] sArr = new short[Bidi.SEEKING_STRONG_FOR_FSI][];
            sArr[Bidi.OPTION_DEFAULT] = act1;
            sArr[Bidi.SEEKING_STRONG_FOR_PARA] = act2;
            this.impact = sArr;
        }
    }

    static class InsertPoints {
        int confirmed;
        Point[] points;
        int size;

        InsertPoints() {
            this.points = new Point[Bidi.OPTION_DEFAULT];
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
        byte filler;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Bidi.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.Bidi.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Bidi.<clinit>():void");
    }

    void setParaRunsOnly(char[] r1, byte r2) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.Bidi.setParaRunsOnly(char[], byte):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 7 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.Bidi.setParaRunsOnly(char[], byte):void");
    }

    static int DirPropFlag(byte dir) {
        return SEEKING_STRONG_FOR_PARA << dir;
    }

    boolean testDirPropFlagAt(int flag, int index) {
        return (DirPropFlag(this.dirProps[index]) & flag) != 0;
    }

    static final int DirPropFlagLR(byte level) {
        return DirPropFlagLR[level & SEEKING_STRONG_FOR_PARA];
    }

    static final int DirPropFlagE(byte level) {
        return DirPropFlagE[level & SEEKING_STRONG_FOR_PARA];
    }

    static final int DirPropFlagO(byte level) {
        return DirPropFlagO[level & SEEKING_STRONG_FOR_PARA];
    }

    static final byte DirFromStrong(byte strong) {
        return strong == null ? LTR : RTL;
    }

    static final byte NoOverride(byte level) {
        return (byte) (level & DIRECTION_DEFAULT_RIGHT_TO_LEFT);
    }

    static byte GetLRFromLevel(byte level) {
        return (byte) (level & SEEKING_STRONG_FOR_PARA);
    }

    static boolean IsDefaultLevel(byte level) {
        return (level & DIRECTION_DEFAULT_LEFT_TO_RIGHT) == DIRECTION_DEFAULT_LEFT_TO_RIGHT;
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
        this((int) OPTION_DEFAULT, (int) OPTION_DEFAULT);
    }

    public Bidi(int maxLength, int maxRunCount) {
        this.dirPropsMemory = new byte[SEEKING_STRONG_FOR_PARA];
        this.levelsMemory = new byte[SEEKING_STRONG_FOR_PARA];
        this.paras_limit = new int[SIMPLE_PARAS_COUNT];
        this.paras_level = new byte[SIMPLE_PARAS_COUNT];
        this.runsMemory = new BidiRun[OPTION_DEFAULT];
        BidiRun[] bidiRunArr = new BidiRun[SEEKING_STRONG_FOR_PARA];
        bidiRunArr[OPTION_DEFAULT] = new BidiRun();
        this.simpleRuns = bidiRunArr;
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
        } else if (maxRunCount > SEEKING_STRONG_FOR_PARA) {
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
            i = RLM_BEFORE;
        } else {
            i = OPTION_DEFAULT;
        }
        this.reorderingMode = i;
    }

    public boolean isInverse() {
        return this.isInverse;
    }

    public void setReorderingMode(int reorderingMode) {
        boolean z = false;
        if (reorderingMode >= 0 && reorderingMode < IMPTABLEVELS_RES) {
            this.reorderingMode = reorderingMode;
            if (reorderingMode == RLM_BEFORE) {
                z = true;
            }
            this.isInverse = z;
        }
    }

    public int getReorderingMode() {
        return this.reorderingMode;
    }

    public void setReorderingOptions(int options) {
        if ((options & SEEKING_STRONG_FOR_FSI) != 0) {
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
            return NEUTRAL;
        }
        int length = paragraph.length();
        int i = OPTION_DEFAULT;
        while (i < length) {
            byte direction = UCharacter.getDirectionality(UCharacter.codePointAt(paragraph, i));
            if (direction == null) {
                return LTR;
            }
            if (direction == RTL || direction == 13) {
                return RTL;
            }
            i = UCharacter.offsetByCodePoints(paragraph, i, SEEKING_STRONG_FOR_PARA);
        }
        return NEUTRAL;
    }

    private byte firstL_R_AL() {
        byte result = ON;
        int i = OPTION_DEFAULT;
        while (i < this.prologue.length()) {
            int uchar = this.prologue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (result == SIMPLE_PARAS_COUNT) {
                if (!(dirProp == null || dirProp == SEEKING_STRONG_FOR_PARA)) {
                    if (dirProp == 13) {
                    }
                }
                result = dirProp;
            } else if (dirProp == IMPTABLEVELS_RES) {
                result = ON;
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
                this.paras_limit = new int[(count * SEEKING_STRONG_FOR_FSI)];
                this.paras_level = new byte[(count * SEEKING_STRONG_FOR_FSI)];
                System.arraycopy(saveLimits, OPTION_DEFAULT, this.paras_limit, OPTION_DEFAULT, oldLength);
                System.arraycopy(saveLevels, OPTION_DEFAULT, this.paras_level, OPTION_DEFAULT, oldLength);
            } catch (Exception e) {
                throw new OutOfMemoryError("Failed to allocate memory for paras");
            }
        }
    }

    private void getDirProps() {
        int i;
        boolean isDefaultLevelInverse;
        byte dirProp;
        byte state;
        this.flags = OPTION_DEFAULT;
        boolean isDefaultLevel = IsDefaultLevel(this.paraLevel);
        if (isDefaultLevel) {
            i = this.reorderingMode;
            if (r0 != 5) {
                i = this.reorderingMode;
                isDefaultLevelInverse = r0 == 6;
            } else {
                isDefaultLevelInverse = true;
            }
        } else {
            isDefaultLevelInverse = false;
        }
        this.lastArabicPos = MAP_NOWHERE;
        int controlCount = OPTION_DEFAULT;
        boolean removeBidiControls = (this.reorderingOptions & SEEKING_STRONG_FOR_FSI) != 0;
        byte b = ON;
        int[] isolateStartStack = new int[DIRECTION_DEFAULT_LEFT_TO_RIGHT];
        byte[] previousStateStack = new byte[DIRECTION_DEFAULT_LEFT_TO_RIGHT];
        int stackLast = MAP_NOWHERE;
        if ((this.reorderingOptions & RLM_BEFORE) != 0) {
            this.length = OPTION_DEFAULT;
        }
        byte defaultParaLevel = (byte) (this.paraLevel & SEEKING_STRONG_FOR_PARA);
        if (isDefaultLevel) {
            this.paras_level[OPTION_DEFAULT] = defaultParaLevel;
            b = defaultParaLevel;
            if (this.prologue != null) {
                dirProp = firstL_R_AL();
                if (dirProp != SIMPLE_PARAS_COUNT) {
                    if (dirProp == null) {
                        this.paras_level[OPTION_DEFAULT] = LTR;
                    } else {
                        this.paras_level[OPTION_DEFAULT] = RTL;
                    }
                    state = LTR;
                }
            }
            state = RTL;
        } else {
            this.paras_level[OPTION_DEFAULT] = this.paraLevel;
            state = LTR;
        }
        int i2 = OPTION_DEFAULT;
        while (true) {
            i = this.originalLength;
            if (i2 >= r0) {
                break;
            }
            int i0 = i2;
            int uchar = UTF16.charAt(this.text, OPTION_DEFAULT, this.originalLength, i2);
            i2 += UTF16.getCharCount(uchar);
            int i1 = i2 + MAP_NOWHERE;
            dirProp = (byte) getCustomizedClass(uchar);
            this.flags |= DirPropFlag(dirProp);
            this.dirProps[i1] = dirProp;
            if (i1 > i0) {
                this.flags |= DirPropFlag(BN);
                do {
                    i1 += MAP_NOWHERE;
                    this.dirProps[i1] = BN;
                } while (i1 > i0);
            }
            if (removeBidiControls && IsBidiControlChar(uchar)) {
                controlCount += SEEKING_STRONG_FOR_PARA;
            }
            if (dirProp == null) {
                if (state == SEEKING_STRONG_FOR_PARA) {
                    this.paras_level[this.paraCount + MAP_NOWHERE] = LTR;
                    state = LTR;
                } else if (state == SEEKING_STRONG_FOR_FSI) {
                    if (stackLast <= 125) {
                        this.flags |= DirPropFlag(LRI);
                    }
                    state = NEUTRAL;
                }
                b = LTR;
            } else if (dirProp == SEEKING_STRONG_FOR_PARA || dirProp == 13) {
                if (state == SEEKING_STRONG_FOR_PARA) {
                    this.paras_level[this.paraCount + MAP_NOWHERE] = RTL;
                    state = LTR;
                } else if (state == SEEKING_STRONG_FOR_FSI) {
                    if (stackLast <= 125) {
                        this.dirProps[isolateStartStack[stackLast]] = RLI;
                        this.flags |= DirPropFlag(RLI);
                    }
                    state = NEUTRAL;
                }
                b = RTL;
                if (dirProp == 13) {
                    this.lastArabicPos = i2 + MAP_NOWHERE;
                }
            } else if (dirProp >= 19 && dirProp <= 21) {
                stackLast += SEEKING_STRONG_FOR_PARA;
                if (stackLast <= 125) {
                    isolateStartStack[stackLast] = i2 + MAP_NOWHERE;
                    previousStateStack[stackLast] = state;
                }
                if (dirProp == 19) {
                    this.dirProps[i2 + MAP_NOWHERE] = LRI;
                    state = MIXED;
                } else {
                    state = NEUTRAL;
                }
            } else if (dirProp == 22) {
                if (state == SEEKING_STRONG_FOR_FSI && stackLast <= 125) {
                    this.flags |= DirPropFlag(LRI);
                }
                if (stackLast >= 0) {
                    if (stackLast <= 125) {
                        state = previousStateStack[stackLast];
                    }
                    stackLast += MAP_NOWHERE;
                }
            } else if (dirProp == IMPTABLEVELS_RES) {
                i = this.originalLength;
                if (i2 < r0 && uchar == 13) {
                    if (this.text[i2] == SIMPLE_PARAS_COUNT) {
                    }
                }
                this.paras_limit[this.paraCount + MAP_NOWHERE] = i2;
                if (isDefaultLevelInverse && r12 == SEEKING_STRONG_FOR_PARA) {
                    this.paras_level[this.paraCount + MAP_NOWHERE] = RTL;
                }
                if ((this.reorderingOptions & RLM_BEFORE) != 0) {
                    this.length = i2;
                    this.controlCount = controlCount;
                }
                i = this.originalLength;
                if (i2 < r0) {
                    this.paraCount += SEEKING_STRONG_FOR_PARA;
                    checkParaCount();
                    if (isDefaultLevel) {
                        this.paras_level[this.paraCount + MAP_NOWHERE] = defaultParaLevel;
                        state = RTL;
                        b = defaultParaLevel;
                    } else {
                        this.paras_level[this.paraCount + MAP_NOWHERE] = this.paraLevel;
                        state = LTR;
                    }
                    stackLast = MAP_NOWHERE;
                }
            }
        }
        if (stackLast > 125) {
            stackLast = Opcodes.OP_NEG_LONG;
            state = MIXED;
        }
        while (stackLast >= 0) {
            if (state == SEEKING_STRONG_FOR_FSI) {
                this.flags |= DirPropFlag(LRI);
                break;
            } else {
                state = previousStateStack[stackLast];
                stackLast += MAP_NOWHERE;
            }
        }
        if ((this.reorderingOptions & RLM_BEFORE) != 0) {
            if (this.length < this.originalLength) {
                this.paraCount += MAP_NOWHERE;
            }
        } else {
            this.paras_limit[this.paraCount + MAP_NOWHERE] = this.originalLength;
            this.controlCount = controlCount;
        }
        if (isDefaultLevelInverse && r12 == SEEKING_STRONG_FOR_PARA) {
            this.paras_level[this.paraCount + MAP_NOWHERE] = RTL;
        }
        if (isDefaultLevel) {
            this.paraLevel = this.paras_level[OPTION_DEFAULT];
        }
        i2 = OPTION_DEFAULT;
        while (true) {
            i = this.paraCount;
            if (i2 >= r0) {
                break;
            }
            this.flags |= DirPropFlagLR(this.paras_level[i2]);
            i2 += SEEKING_STRONG_FOR_PARA;
        }
        if (this.orderParagraphsLTR) {
            if ((this.flags & DirPropFlag(B)) != 0) {
                this.flags |= DirPropFlag(LTR);
            }
        }
    }

    byte GetParaLevelAt(int pindex) {
        if (this.defaultParaLevel == null || pindex < this.paras_limit[OPTION_DEFAULT]) {
            return this.paraLevel;
        }
        int i = SEEKING_STRONG_FOR_PARA;
        while (i < this.paraCount && pindex >= this.paras_limit[i]) {
            i += SEEKING_STRONG_FOR_PARA;
        }
        if (i >= this.paraCount) {
            i = this.paraCount + MAP_NOWHERE;
        }
        return this.paras_level[i];
    }

    private void bracketInit(BracketData bd) {
        boolean z = true;
        bd.isoRunLast = OPTION_DEFAULT;
        bd.isoRuns[OPTION_DEFAULT] = new IsoRun();
        bd.isoRuns[OPTION_DEFAULT].start = _L;
        bd.isoRuns[OPTION_DEFAULT].limit = _L;
        bd.isoRuns[OPTION_DEFAULT].level = GetParaLevelAt(OPTION_DEFAULT);
        IsoRun isoRun = bd.isoRuns[OPTION_DEFAULT];
        byte GetParaLevelAt = (byte) (GetParaLevelAt(OPTION_DEFAULT) & SEEKING_STRONG_FOR_PARA);
        bd.isoRuns[OPTION_DEFAULT].contextDir = GetParaLevelAt;
        bd.isoRuns[OPTION_DEFAULT].lastBase = GetParaLevelAt;
        isoRun.lastStrong = GetParaLevelAt;
        bd.isoRuns[OPTION_DEFAULT].contextPos = OPTION_DEFAULT;
        bd.openings = new Opening[SIMPLE_OPENINGS_COUNT];
        if (!(this.reorderingMode == SEEKING_STRONG_FOR_PARA || this.reorderingMode == 6)) {
            z = false;
        }
        bd.isNumbersSpecial = z;
    }

    private void bracketProcessB(BracketData bd, byte level) {
        bd.isoRunLast = OPTION_DEFAULT;
        bd.isoRuns[OPTION_DEFAULT].limit = _L;
        bd.isoRuns[OPTION_DEFAULT].level = level;
        IsoRun isoRun = bd.isoRuns[OPTION_DEFAULT];
        byte b = (byte) (level & SEEKING_STRONG_FOR_PARA);
        bd.isoRuns[OPTION_DEFAULT].contextDir = b;
        bd.isoRuns[OPTION_DEFAULT].lastBase = b;
        isoRun.lastStrong = b;
        bd.isoRuns[OPTION_DEFAULT].contextPos = OPTION_DEFAULT;
    }

    private void bracketProcessBoundary(BracketData bd, int lastCcPos, byte contextLevel, byte embeddingLevel) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if ((DirPropFlag(this.dirProps[lastCcPos]) & MASK_ISO) == 0) {
            if (NoOverride(embeddingLevel) > NoOverride(contextLevel)) {
                contextLevel = embeddingLevel;
            }
            pLastIsoRun.limit = pLastIsoRun.start;
            pLastIsoRun.level = embeddingLevel;
            byte b = (byte) (contextLevel & SEEKING_STRONG_FOR_PARA);
            pLastIsoRun.contextDir = b;
            pLastIsoRun.lastBase = b;
            pLastIsoRun.lastStrong = b;
            pLastIsoRun.contextPos = lastCcPos;
        }
    }

    private void bracketProcessLRI_RLI(BracketData bd, byte level) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        pLastIsoRun.lastBase = ON;
        short lastLimit = pLastIsoRun.limit;
        bd.isoRunLast += SEEKING_STRONG_FOR_PARA;
        pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if (pLastIsoRun == null) {
            pLastIsoRun = new IsoRun();
            bd.isoRuns[bd.isoRunLast] = pLastIsoRun;
        }
        pLastIsoRun.limit = lastLimit;
        pLastIsoRun.start = lastLimit;
        pLastIsoRun.level = level;
        byte b = (byte) (level & SEEKING_STRONG_FOR_PARA);
        pLastIsoRun.contextDir = b;
        pLastIsoRun.lastBase = b;
        pLastIsoRun.lastStrong = b;
        pLastIsoRun.contextPos = OPTION_DEFAULT;
    }

    private void bracketProcessPDI(BracketData bd) {
        bd.isoRunLast += MAP_NOWHERE;
        bd.isoRuns[bd.isoRunLast].lastBase = ON;
    }

    private void bracketAddOpening(BracketData bd, char match, int position) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        if (pLastIsoRun.limit >= bd.openings.length) {
            Opening[] saveOpenings = bd.openings;
            try {
                int count = bd.openings.length;
                bd.openings = new Opening[(count * SEEKING_STRONG_FOR_FSI)];
                System.arraycopy(saveOpenings, OPTION_DEFAULT, bd.openings, OPTION_DEFAULT, count);
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
        pOpening.flags = _L;
        pLastIsoRun.limit = (short) (pLastIsoRun.limit + SEEKING_STRONG_FOR_PARA);
    }

    private void fixN0c(BracketData bd, int openingIndex, int newPropPosition, byte newProp) {
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        for (short k = openingIndex + SEEKING_STRONG_FOR_PARA; k < pLastIsoRun.limit; k += SEEKING_STRONG_FOR_PARA) {
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
                        qOpening.match = OPTION_DEFAULT;
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
        byte direction = (byte) (pLastIsoRun.level & SEEKING_STRONG_FOR_PARA);
        boolean stable = true;
        if ((direction == null && (pOpening.flags & FOUND_L) > 0) || (direction == SEEKING_STRONG_FOR_PARA && (pOpening.flags & FOUND_R) > 0)) {
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
            return ON;
        }
        this.dirProps[pOpening.position] = newProp;
        this.dirProps[position] = newProp;
        fixN0c(bd, openIdx, pOpening.position, newProp);
        if (stable) {
            pLastIsoRun.limit = (short) openIdx;
            while (pLastIsoRun.limit > pLastIsoRun.start && bd.openings[pLastIsoRun.limit + MAP_NOWHERE].position == pOpening.position) {
                pLastIsoRun.limit = (short) (pLastIsoRun.limit + MAP_NOWHERE);
            }
        } else {
            pOpening.match = -position;
            short k = openIdx + MAP_NOWHERE;
            while (k >= pLastIsoRun.start && bd.openings[k].position == pOpening.position) {
                short k2 = k + MAP_NOWHERE;
                bd.openings[k].match = OPTION_DEFAULT;
                k = k2;
            }
            for (k = openIdx + SEEKING_STRONG_FOR_PARA; k < pLastIsoRun.limit; k += SEEKING_STRONG_FOR_PARA) {
                Opening qOpening = bd.openings[k];
                if (qOpening.position >= position) {
                    break;
                }
                if (qOpening.match > 0) {
                    qOpening.match = OPTION_DEFAULT;
                }
            }
        }
        return newProp;
    }

    private void bracketProcessChar(BracketData bd, int position) {
        byte newProp;
        byte level;
        short flag;
        short i;
        IsoRun pLastIsoRun = bd.isoRuns[bd.isoRunLast];
        byte dirProp = this.dirProps[position];
        if (dirProp == SIMPLE_PARAS_COUNT) {
            char bidiPairedBracket;
            char c = this.text[position];
            short idx = pLastIsoRun.limit + MAP_NOWHERE;
            while (idx >= pLastIsoRun.start) {
                if (bd.openings[idx].match != c) {
                    idx += MAP_NOWHERE;
                } else {
                    newProp = bracketProcessClosing(bd, idx, position);
                    if (newProp == SIMPLE_PARAS_COUNT) {
                        c = '\u0000';
                        if (c == '\u0000') {
                            bidiPairedBracket = (char) UCharacter.getBidiPairedBracket(c);
                        } else {
                            bidiPairedBracket = '\u0000';
                        }
                        if (bidiPairedBracket != c && UCharacter.getIntPropertyValue(c, UProperty.BIDI_PAIRED_BRACKET_TYPE) == SEEKING_STRONG_FOR_PARA) {
                            if (bidiPairedBracket == '\u232a') {
                                bracketAddOpening(bd, '\u3009', position);
                            } else if (bidiPairedBracket == '\u3009') {
                                bracketAddOpening(bd, '\u232a', position);
                            }
                            bracketAddOpening(bd, bidiPairedBracket, position);
                        }
                    } else {
                        byte[] bArr;
                        pLastIsoRun.lastBase = ON;
                        pLastIsoRun.contextDir = newProp;
                        pLastIsoRun.contextPos = position;
                        level = this.levels[position];
                        if ((level & -128) != 0) {
                            newProp = (byte) (level & SEEKING_STRONG_FOR_PARA);
                            pLastIsoRun.lastStrong = newProp;
                            flag = (short) DirPropFlag(newProp);
                            for (i = pLastIsoRun.start; i < idx; i += SEEKING_STRONG_FOR_PARA) {
                                Opening opening = bd.openings[i];
                                opening.flags = (short) (opening.flags | flag);
                            }
                            bArr = this.levels;
                            bArr[position] = (byte) (bArr[position] & DIRECTION_DEFAULT_RIGHT_TO_LEFT);
                        }
                        bArr = this.levels;
                        int i2 = bd.openings[idx].position;
                        bArr[i2] = (byte) (bArr[i2] & DIRECTION_DEFAULT_RIGHT_TO_LEFT);
                        return;
                    }
                }
            }
            if (c == '\u0000') {
                bidiPairedBracket = '\u0000';
            } else {
                bidiPairedBracket = (char) UCharacter.getBidiPairedBracket(c);
            }
            if (bidiPairedBracket == '\u232a') {
                bracketAddOpening(bd, '\u3009', position);
            } else if (bidiPairedBracket == '\u3009') {
                bracketAddOpening(bd, '\u232a', position);
            }
            bracketAddOpening(bd, bidiPairedBracket, position);
        }
        level = this.levels[position];
        if ((level & -128) != 0) {
            newProp = (byte) (level & SEEKING_STRONG_FOR_PARA);
            if (!(dirProp == RLM_AFTER || dirProp == 9 || dirProp == SIMPLE_PARAS_COUNT)) {
                this.dirProps[position] = newProp;
            }
            pLastIsoRun.lastBase = newProp;
            pLastIsoRun.lastStrong = newProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        } else if (dirProp <= SEEKING_STRONG_FOR_PARA || dirProp == 13) {
            newProp = DirFromStrong(dirProp);
            pLastIsoRun.lastBase = dirProp;
            pLastIsoRun.lastStrong = dirProp;
            pLastIsoRun.contextDir = newProp;
            pLastIsoRun.contextPos = position;
        } else if (dirProp == SEEKING_STRONG_FOR_FSI) {
            pLastIsoRun.lastBase = MIXED;
            if (pLastIsoRun.lastStrong == null) {
                newProp = LTR;
                if (!bd.isNumbersSpecial) {
                    this.dirProps[position] = ENL;
                }
                pLastIsoRun.contextDir = LTR;
                pLastIsoRun.contextPos = position;
            } else {
                newProp = RTL;
                if (pLastIsoRun.lastStrong == 13) {
                    this.dirProps[position] = AN;
                } else {
                    this.dirProps[position] = ENR;
                }
                pLastIsoRun.contextDir = RTL;
                pLastIsoRun.contextPos = position;
            }
        } else if (dirProp == 5) {
            newProp = RTL;
            pLastIsoRun.lastBase = AN;
            pLastIsoRun.contextDir = RTL;
            pLastIsoRun.contextPos = position;
        } else if (dirProp == 17) {
            newProp = pLastIsoRun.lastBase;
            if (newProp == SIMPLE_PARAS_COUNT) {
                this.dirProps[position] = newProp;
            }
        } else {
            newProp = dirProp;
            pLastIsoRun.lastBase = dirProp;
        }
        if (newProp <= SEEKING_STRONG_FOR_PARA || newProp == 13) {
            flag = (short) DirPropFlag(DirFromStrong(newProp));
            for (i = pLastIsoRun.start; i < pLastIsoRun.limit; i += SEEKING_STRONG_FOR_PARA) {
                if (position > bd.openings[i].position) {
                    opening = bd.openings[i];
                    opening.flags = (short) (opening.flags | flag);
                }
            }
        }
    }

    private byte directionFromFlags() {
        if ((this.flags & MASK_RTL) == 0 && ((this.flags & DirPropFlag(AN)) == 0 || (this.flags & MASK_POSSIBLE_N) == 0)) {
            return LTR;
        }
        if ((this.flags & MASK_LTR) == 0) {
            return RTL;
        }
        return MIXED;
    }

    private byte resolveExplicitLevels() {
        byte level = GetParaLevelAt(OPTION_DEFAULT);
        this.isolateCount = OPTION_DEFAULT;
        byte dirct = directionFromFlags();
        if (dirct != SEEKING_STRONG_FOR_FSI) {
            return dirct;
        }
        int i = this.reorderingMode;
        int paraIndex;
        int start;
        int limit;
        int i2;
        if (r0 > SEEKING_STRONG_FOR_PARA) {
            paraIndex = OPTION_DEFAULT;
            while (true) {
                i = this.paraCount;
                if (paraIndex >= r0) {
                    return dirct;
                }
                if (paraIndex == 0) {
                    start = OPTION_DEFAULT;
                } else {
                    start = this.paras_limit[paraIndex + MAP_NOWHERE];
                }
                limit = this.paras_limit[paraIndex];
                level = this.paras_level[paraIndex];
                for (i2 = start; i2 < limit; i2 += SEEKING_STRONG_FOR_PARA) {
                    this.levels[i2] = level;
                }
                paraIndex += SEEKING_STRONG_FOR_PARA;
            }
        } else {
            BracketData bracketData;
            byte dirProp;
            if ((this.flags & (MASK_EXPLICIT | MASK_ISO)) == 0) {
                bracketData = new BracketData();
                bracketInit(bracketData);
                paraIndex = OPTION_DEFAULT;
                while (true) {
                    i = this.paraCount;
                    if (paraIndex >= r0) {
                        return dirct;
                    }
                    if (paraIndex == 0) {
                        start = OPTION_DEFAULT;
                    } else {
                        start = this.paras_limit[paraIndex + MAP_NOWHERE];
                    }
                    limit = this.paras_limit[paraIndex];
                    level = this.paras_level[paraIndex];
                    for (i2 = start; i2 < limit; i2 += SEEKING_STRONG_FOR_PARA) {
                        this.levels[i2] = level;
                        dirProp = this.dirProps[i2];
                        if (dirProp != 18) {
                            if (dirProp == IMPTABLEVELS_RES) {
                                if (i2 + SEEKING_STRONG_FOR_PARA < this.length) {
                                    if (this.text[i2] == '\r') {
                                        if (this.text[i2 + SEEKING_STRONG_FOR_PARA] == SIMPLE_PARAS_COUNT) {
                                        }
                                    }
                                    bracketProcessB(bracketData, level);
                                }
                            } else {
                                bracketProcessChar(bracketData, i2);
                            }
                        }
                    }
                    paraIndex += SEEKING_STRONG_FOR_PARA;
                }
            } else {
                byte embeddingLevel = level;
                byte previousLevel = level;
                int lastCcPos = OPTION_DEFAULT;
                short[] stack = new short[DIRECTION_DEFAULT_RIGHT_TO_LEFT];
                int stackLast = OPTION_DEFAULT;
                int overflowIsolateCount = OPTION_DEFAULT;
                int overflowEmbeddingCount = OPTION_DEFAULT;
                int validIsolateCount = OPTION_DEFAULT;
                bracketData = new BracketData();
                bracketInit(bracketData);
                stack[OPTION_DEFAULT] = (short) level;
                this.flags = OPTION_DEFAULT;
                i2 = OPTION_DEFAULT;
                while (true) {
                    i = this.length;
                    if (i2 < r0) {
                        dirProp = this.dirProps[i2];
                        byte newLevel;
                        switch (dirProp) {
                            case IMPTABLEVELS_RES /*7*/:
                                this.flags |= DirPropFlag(B);
                                this.levels[i2] = GetParaLevelAt(i2);
                                if (i2 + SEEKING_STRONG_FOR_PARA >= this.length) {
                                    break;
                                }
                                if (this.text[i2] == '\r') {
                                    if (this.text[i2 + SEEKING_STRONG_FOR_PARA] == SIMPLE_PARAS_COUNT) {
                                        break;
                                    }
                                }
                                overflowIsolateCount = OPTION_DEFAULT;
                                overflowEmbeddingCount = OPTION_DEFAULT;
                                validIsolateCount = OPTION_DEFAULT;
                                stackLast = OPTION_DEFAULT;
                                embeddingLevel = GetParaLevelAt(i2 + SEEKING_STRONG_FOR_PARA);
                                previousLevel = embeddingLevel;
                                stack[OPTION_DEFAULT] = (short) embeddingLevel;
                                bracketProcessB(bracketData, embeddingLevel);
                                break;
                            case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                            case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                            case Opcodes.OP_RETURN_VOID /*14*/:
                            case IMPTABPROPS_RES /*15*/:
                                this.flags |= DirPropFlag(BN);
                                this.levels[i2] = previousLevel;
                                if (dirProp == 11 || dirProp == 12) {
                                    newLevel = (byte) ((embeddingLevel + SEEKING_STRONG_FOR_FSI) & DIRECTION_DEFAULT_LEFT_TO_RIGHT);
                                } else {
                                    newLevel = (byte) ((NoOverride(embeddingLevel) + SEEKING_STRONG_FOR_PARA) | SEEKING_STRONG_FOR_PARA);
                                }
                                if (newLevel > 125 || overflowIsolateCount != 0 || overflowEmbeddingCount != 0) {
                                    if (overflowIsolateCount != 0) {
                                        break;
                                    }
                                    overflowEmbeddingCount += SEEKING_STRONG_FOR_PARA;
                                    break;
                                }
                                lastCcPos = i2;
                                embeddingLevel = newLevel;
                                if (dirProp == 12 || dirProp == IMPTABPROPS_RES) {
                                    embeddingLevel = (byte) (embeddingLevel | -128);
                                }
                                stackLast += SEEKING_STRONG_FOR_PARA;
                                stack[stackLast] = (short) embeddingLevel;
                                break;
                                break;
                            case IMPTABPROPS_COLUMNS /*16*/:
                                this.flags |= DirPropFlag(BN);
                                this.levels[i2] = previousLevel;
                                if (overflowIsolateCount <= 0) {
                                    if (overflowEmbeddingCount <= 0) {
                                        if (stackLast > 0 && stack[stackLast] < ISOLATE) {
                                            lastCcPos = i2;
                                            stackLast += MAP_NOWHERE;
                                            embeddingLevel = (byte) stack[stackLast];
                                            break;
                                        }
                                    }
                                    overflowEmbeddingCount += MAP_NOWHERE;
                                    break;
                                }
                                break;
                            case Opcodes.OP_CONST_4 /*18*/:
                                this.levels[i2] = previousLevel;
                                this.flags |= DirPropFlag(BN);
                                break;
                            case SIMPLE_OPENINGS_COUNT /*20*/:
                            case IoBridge.JAVA_MCAST_JOIN_SOURCE_GROUP /*21*/:
                                this.flags |= DirPropFlag(ON) | DirPropFlagLR(embeddingLevel);
                                this.levels[i2] = NoOverride(embeddingLevel);
                                if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                                    bracketProcessBoundary(bracketData, lastCcPos, previousLevel, embeddingLevel);
                                    this.flags |= DirPropFlagMultiRuns;
                                }
                                previousLevel = embeddingLevel;
                                if (dirProp == SIMPLE_OPENINGS_COUNT) {
                                    newLevel = (byte) ((embeddingLevel + SEEKING_STRONG_FOR_FSI) & DIRECTION_DEFAULT_LEFT_TO_RIGHT);
                                } else {
                                    newLevel = (byte) ((NoOverride(embeddingLevel) + SEEKING_STRONG_FOR_PARA) | SEEKING_STRONG_FOR_PARA);
                                }
                                if (newLevel > 125 || overflowIsolateCount != 0 || overflowEmbeddingCount != 0) {
                                    this.dirProps[i2] = WS;
                                    overflowIsolateCount += SEEKING_STRONG_FOR_PARA;
                                    break;
                                }
                                this.flags |= DirPropFlag(dirProp);
                                lastCcPos = i2;
                                validIsolateCount += SEEKING_STRONG_FOR_PARA;
                                if (validIsolateCount > this.isolateCount) {
                                    this.isolateCount = validIsolateCount;
                                }
                                embeddingLevel = newLevel;
                                stackLast += SEEKING_STRONG_FOR_PARA;
                                stack[stackLast] = (short) (newLevel + ISOLATE);
                                bracketProcessLRI_RLI(bracketData, embeddingLevel);
                                break;
                                break;
                            case IoBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP /*22*/:
                                if (NoOverride(embeddingLevel) != NoOverride(previousLevel)) {
                                    bracketProcessBoundary(bracketData, lastCcPos, previousLevel, embeddingLevel);
                                    this.flags |= DirPropFlagMultiRuns;
                                }
                                if (overflowIsolateCount > 0) {
                                    overflowIsolateCount += MAP_NOWHERE;
                                    this.dirProps[i2] = WS;
                                } else if (validIsolateCount > 0) {
                                    this.flags |= DirPropFlag(PDI);
                                    lastCcPos = i2;
                                    overflowEmbeddingCount = OPTION_DEFAULT;
                                    while (stack[stackLast] < ISOLATE) {
                                        stackLast += MAP_NOWHERE;
                                    }
                                    stackLast += MAP_NOWHERE;
                                    validIsolateCount += MAP_NOWHERE;
                                    bracketProcessPDI(bracketData);
                                } else {
                                    this.dirProps[i2] = WS;
                                }
                                embeddingLevel = (byte) (stack[stackLast] & -257);
                                this.flags |= DirPropFlag(ON) | DirPropFlagLR(embeddingLevel);
                                previousLevel = embeddingLevel;
                                this.levels[i2] = NoOverride(embeddingLevel);
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
                                this.levels[i2] = embeddingLevel;
                                bracketProcessChar(bracketData, i2);
                                this.flags |= DirPropFlag(this.dirProps[i2]);
                                break;
                        }
                        i2 += SEEKING_STRONG_FOR_PARA;
                    } else {
                        if ((this.flags & MASK_EMBEDDING) != 0) {
                            this.flags |= DirPropFlagLR(this.paraLevel);
                        }
                        if (this.orderParagraphsLTR) {
                            if ((this.flags & DirPropFlag(B)) != 0) {
                                this.flags |= DirPropFlag(LTR);
                            }
                        }
                        return directionFromFlags();
                    }
                }
            }
        }
    }

    private byte checkExplicitLevels() {
        int isolateCount = OPTION_DEFAULT;
        this.flags = OPTION_DEFAULT;
        this.isolateCount = OPTION_DEFAULT;
        int i = OPTION_DEFAULT;
        while (i < this.length) {
            byte level = this.levels[i];
            byte dirProp = this.dirProps[i];
            if (dirProp == SIMPLE_OPENINGS_COUNT || dirProp == 21) {
                isolateCount += SEEKING_STRONG_FOR_PARA;
                if (isolateCount > this.isolateCount) {
                    this.isolateCount = isolateCount;
                }
            } else if (dirProp == 22) {
                isolateCount += MAP_NOWHERE;
            } else if (dirProp == B) {
                isolateCount = OPTION_DEFAULT;
            }
            if ((level & -128) != 0) {
                level = (byte) (level & DIRECTION_DEFAULT_RIGHT_TO_LEFT);
                this.flags |= DirPropFlagO(level);
            } else {
                this.flags |= DirPropFlagE(level) | DirPropFlag(dirProp);
            }
            if ((level >= GetParaLevelAt(i) || (level == null && dirProp == B)) && 125 >= level) {
                i += SEEKING_STRONG_FOR_PARA;
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
        return (short) (cell & IMPTABPROPS_RES);
    }

    private static short GetAction(byte cell) {
        return (short) (cell >> RLM_BEFORE);
    }

    private void addPoint(int pos, int flag) {
        Point point = new Point();
        int len = this.insertPoints.points.length;
        if (len == 0) {
            this.insertPoints.points = new Point[SIMPLE_PARAS_COUNT];
            len = SIMPLE_PARAS_COUNT;
        }
        if (this.insertPoints.size >= len) {
            Point[] savePoints = this.insertPoints.points;
            this.insertPoints.points = new Point[(len * SEEKING_STRONG_FOR_FSI)];
            System.arraycopy(savePoints, OPTION_DEFAULT, this.insertPoints.points, OPTION_DEFAULT, len);
        }
        point.pos = pos;
        point.flag = flag;
        this.insertPoints.points[this.insertPoints.size] = point;
        InsertPoints insertPoints = this.insertPoints;
        insertPoints.size += SEEKING_STRONG_FOR_PARA;
    }

    private void setLevelsOutsideIsolates(int start, int limit, byte level) {
        int isolateCount = OPTION_DEFAULT;
        for (int k = start; k < limit; k += SEEKING_STRONG_FOR_PARA) {
            byte dirProp = this.dirProps[k];
            if (dirProp == 22) {
                isolateCount += MAP_NOWHERE;
            }
            if (isolateCount == 0) {
                this.levels[k] = level;
            }
            if (dirProp == SIMPLE_OPENINGS_COUNT || dirProp == 21) {
                isolateCount += SEEKING_STRONG_FOR_PARA;
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
        byte addLevel = impTab[levState.state][IMPTABLEVELS_RES];
        if (actionSeq != (short) 0) {
            byte[] bArr;
            switch (actionSeq) {
                case SEEKING_STRONG_FOR_PARA /*1*/:
                    levState.startON = start;
                    break;
                case SEEKING_STRONG_FOR_FSI /*2*/:
                    start = levState.startON;
                    break;
                case LOOKING_FOR_PDI /*3*/:
                    setLevelsOutsideIsolates(levState.startON, start, (byte) (levState.runLevel + SEEKING_STRONG_FOR_PARA));
                    break;
                case RLM_BEFORE /*4*/:
                    setLevelsOutsideIsolates(levState.startON, start, (byte) (levState.runLevel + SEEKING_STRONG_FOR_FSI));
                    break;
                case XmlPullParser.CDSECT /*5*/:
                    if (levState.startL2EN >= 0) {
                        addPoint(levState.startL2EN, SEEKING_STRONG_FOR_PARA);
                    }
                    levState.startL2EN = MAP_NOWHERE;
                    if (this.insertPoints.points.length != 0 && this.insertPoints.size > this.insertPoints.confirmed) {
                        for (k = levState.lastStrongRTL + SEEKING_STRONG_FOR_PARA; k < start; k += SEEKING_STRONG_FOR_PARA) {
                            this.levels[k] = (byte) ((this.levels[k] - 2) & -2);
                        }
                        this.insertPoints.confirmed = this.insertPoints.size;
                        levState.lastStrongRTL = MAP_NOWHERE;
                        if (_prop == (short) 5) {
                            addPoint(start, SEEKING_STRONG_FOR_PARA);
                            this.insertPoints.confirmed = this.insertPoints.size;
                            break;
                        }
                    }
                    levState.lastStrongRTL = MAP_NOWHERE;
                    if ((impTab[oldStateSeq][IMPTABLEVELS_RES] & SEEKING_STRONG_FOR_PARA) != 0 && levState.startON > 0) {
                        start = levState.startON;
                    }
                    if (_prop == (short) 5) {
                        addPoint(start0, SEEKING_STRONG_FOR_PARA);
                        this.insertPoints.confirmed = this.insertPoints.size;
                        break;
                    }
                    break;
                case XmlPullParser.ENTITY_REF /*6*/:
                    if (this.insertPoints.points.length > 0) {
                        this.insertPoints.size = this.insertPoints.confirmed;
                    }
                    levState.startON = MAP_NOWHERE;
                    levState.startL2EN = MAP_NOWHERE;
                    levState.lastStrongRTL = limit + MAP_NOWHERE;
                    break;
                case IMPTABLEVELS_RES /*7*/:
                    if (_prop != LOOKING_FOR_PDI || this.dirProps[start] != 5 || this.reorderingMode == 6) {
                        if (levState.startL2EN == MAP_NOWHERE) {
                            levState.startL2EN = start;
                            break;
                        }
                    } else if (levState.startL2EN != MAP_NOWHERE) {
                        if (levState.startL2EN >= 0) {
                            addPoint(levState.startL2EN, SEEKING_STRONG_FOR_PARA);
                            levState.startL2EN = -2;
                        }
                        addPoint(start, SEEKING_STRONG_FOR_PARA);
                        break;
                    } else {
                        levState.lastStrongRTL = limit + MAP_NOWHERE;
                        break;
                    }
                    break;
                case RLM_AFTER /*8*/:
                    levState.lastStrongRTL = limit + MAP_NOWHERE;
                    levState.startON = MAP_NOWHERE;
                    break;
                case XmlPullParser.COMMENT /*9*/:
                    k = start + MAP_NOWHERE;
                    while (k >= 0 && (this.levels[k] & SEEKING_STRONG_FOR_PARA) == 0) {
                        k += MAP_NOWHERE;
                    }
                    if (k >= 0) {
                        addPoint(k, RLM_BEFORE);
                        this.insertPoints.confirmed = this.insertPoints.size;
                    }
                    levState.startON = start;
                    break;
                case SIMPLE_PARAS_COUNT /*10*/:
                    addPoint(start, SEEKING_STRONG_FOR_PARA);
                    addPoint(start, SEEKING_STRONG_FOR_FSI);
                    break;
                case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    this.insertPoints.size = this.insertPoints.confirmed;
                    if (_prop == (short) 5) {
                        addPoint(start, RLM_BEFORE);
                        this.insertPoints.confirmed = this.insertPoints.size;
                        break;
                    }
                    break;
                case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                    level = (byte) (levState.runLevel + addLevel);
                    for (k = levState.startON; k < start; k += SEEKING_STRONG_FOR_PARA) {
                        if (this.levels[k] < level) {
                            this.levels[k] = level;
                        }
                    }
                    this.insertPoints.confirmed = this.insertPoints.size;
                    levState.startON = start;
                    break;
                case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    level = levState.runLevel;
                    k = start + MAP_NOWHERE;
                    while (k >= levState.startON) {
                        if (this.levels[k] == level + LOOKING_FOR_PDI) {
                            while (this.levels[k] == level + LOOKING_FOR_PDI) {
                                bArr = this.levels;
                                int k2 = k + MAP_NOWHERE;
                                bArr[k] = (byte) (bArr[k] - 2);
                                k = k2;
                            }
                            while (this.levels[k] == level) {
                                k += MAP_NOWHERE;
                            }
                        }
                        if (this.levels[k] == level + SEEKING_STRONG_FOR_FSI) {
                            this.levels[k] = level;
                        } else {
                            this.levels[k] = (byte) (level + SEEKING_STRONG_FOR_PARA);
                        }
                        k += MAP_NOWHERE;
                    }
                    break;
                case Opcodes.OP_RETURN_VOID /*14*/:
                    level = (byte) (levState.runLevel + SEEKING_STRONG_FOR_PARA);
                    for (k = start + MAP_NOWHERE; k >= levState.startON; k += MAP_NOWHERE) {
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
        if (addLevel != null || start < start0) {
            level = (byte) (levState.runLevel + addLevel);
            if (start >= levState.runStart) {
                for (k = start; k < limit; k += SEEKING_STRONG_FOR_PARA) {
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
            if (dirProp == null) {
                return LTR;
            }
            if (dirProp == RTL || dirProp == 13) {
                return RTL;
            }
            if (dirProp == IMPTABLEVELS_RES) {
                return ET;
            }
        }
        return ET;
    }

    private byte firstL_R_AL_EN_AN() {
        int i = OPTION_DEFAULT;
        while (i < this.epilogue.length()) {
            int uchar = this.epilogue.codePointAt(i);
            i += Character.charCount(uchar);
            byte dirProp = (byte) getCustomizedClass(uchar);
            if (dirProp == null) {
                return LTR;
            }
            if (dirProp == RTL || dirProp == 13) {
                return RTL;
            }
            if (dirProp == MIXED) {
                return MIXED;
            }
            if (dirProp == 5) {
                return NEUTRAL;
            }
        }
        return ET;
    }

    private void resolveImplicitLevels(int start, int limit, short sor, short eor) {
        boolean inverseRTL;
        int start1;
        short stateImp;
        int i;
        byte dirProp;
        byte firstStrong;
        LevState levState = new LevState();
        short nextStrongProp = _R;
        int nextStrongPos = MAP_NOWHERE;
        if (start >= this.lastArabicPos || (GetParaLevelAt(start) & SEEKING_STRONG_FOR_PARA) <= 0) {
            inverseRTL = false;
        } else {
            int i2 = this.reorderingMode;
            if (r0 != 5) {
                i2 = this.reorderingMode;
                inverseRTL = r0 == 6;
            } else {
                inverseRTL = true;
            }
        }
        levState.startL2EN = MAP_NOWHERE;
        levState.lastStrongRTL = MAP_NOWHERE;
        levState.runStart = start;
        levState.runLevel = this.levels[start];
        levState.impTab = this.impTabPair.imptab[levState.runLevel & SEEKING_STRONG_FOR_PARA];
        levState.impAct = this.impTabPair.impact[levState.runLevel & SEEKING_STRONG_FOR_PARA];
        if (start == 0 && this.prologue != null) {
            byte lastStrong = lastL_R_AL();
            if (lastStrong != RLM_BEFORE) {
                sor = (short) lastStrong;
            }
        }
        if (this.dirProps[start] == 22) {
            levState.startON = this.isolates[this.isolateCount].startON;
            start1 = this.isolates[this.isolateCount].start1;
            stateImp = this.isolates[this.isolateCount].stateImp;
            levState.state = this.isolates[this.isolateCount].state;
            this.isolateCount += MAP_NOWHERE;
        } else {
            levState.startON = MAP_NOWHERE;
            start1 = start;
            if (this.dirProps[start] == 17) {
                stateImp = (short) (sor + SEEKING_STRONG_FOR_PARA);
            } else {
                stateImp = _L;
            }
            levState.state = _L;
            processPropertySeq(levState, sor, start, start);
        }
        int start2 = start;
        for (i = start; i <= limit; i += SEEKING_STRONG_FOR_PARA) {
            short gprop;
            if (i >= limit) {
                int k = limit + MAP_NOWHERE;
                while (k > start) {
                    if ((DirPropFlag(this.dirProps[k]) & MASK_BN_EXPLICIT) != 0) {
                        k += MAP_NOWHERE;
                    } else {
                        dirProp = this.dirProps[k];
                        if (!(dirProp == SIMPLE_OPENINGS_COUNT || dirProp == 21)) {
                            gprop = eor;
                        }
                        if (limit == this.length && this.epilogue != null) {
                            firstStrong = firstL_R_AL_EN_AN();
                            if (firstStrong != RLM_BEFORE) {
                                eor = (short) firstStrong;
                            }
                        }
                        while (i > start) {
                            if ((DirPropFlag(this.dirProps[i]) & MASK_BN_EXPLICIT) == 0) {
                            } else {
                                dirProp = this.dirProps[i];
                                if ((dirProp != SIMPLE_OPENINGS_COUNT || dirProp == 21) && limit < this.length) {
                                    this.isolateCount += SEEKING_STRONG_FOR_PARA;
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
                        }
                        dirProp = this.dirProps[i];
                        if (dirProp != SIMPLE_OPENINGS_COUNT) {
                        }
                        this.isolateCount += SEEKING_STRONG_FOR_PARA;
                        if (this.isolates[this.isolateCount] == null) {
                            this.isolates[this.isolateCount] = new Isolate();
                        }
                        this.isolates[this.isolateCount].stateImp = stateImp;
                        this.isolates[this.isolateCount].state = levState.state;
                        this.isolates[this.isolateCount].start1 = start1;
                        this.isolates[this.isolateCount].startON = levState.startON;
                    }
                }
                dirProp = this.dirProps[k];
                gprop = eor;
            } else {
                int prop = this.dirProps[i];
                if (prop == IMPTABLEVELS_RES) {
                    this.isolateCount = MAP_NOWHERE;
                }
                if (inverseRTL) {
                    if (prop == 13) {
                        prop = SEEKING_STRONG_FOR_PARA;
                    } else if (prop == SEEKING_STRONG_FOR_FSI) {
                        if (nextStrongPos <= i) {
                            nextStrongProp = _R;
                            nextStrongPos = limit;
                            int j = i + SEEKING_STRONG_FOR_PARA;
                            while (j < limit) {
                                byte prop1 = this.dirProps[j];
                                if (prop1 == null || prop1 == SEEKING_STRONG_FOR_PARA || prop1 == 13) {
                                    nextStrongProp = (short) prop1;
                                    nextStrongPos = j;
                                } else {
                                    j += SEEKING_STRONG_FOR_PARA;
                                }
                            }
                        }
                        if (nextStrongProp == (short) 13) {
                            prop = 5;
                        }
                    }
                }
                gprop = groupProp[prop];
            }
            short oldStateImp = stateImp;
            short cell = impTabProps[oldStateImp][gprop];
            stateImp = GetStateProps(cell);
            short actionImp = GetActionProps(cell);
            if (i == limit && actionImp == (short) 0) {
                actionImp = _R;
            }
            if (actionImp != (short) 0) {
                short resProp = impTabProps[oldStateImp][IMPTABPROPS_RES];
                switch (actionImp) {
                    case SEEKING_STRONG_FOR_PARA /*1*/:
                        processPropertySeq(levState, resProp, start1, i);
                        start1 = i;
                        break;
                    case SEEKING_STRONG_FOR_FSI /*2*/:
                        start2 = i;
                        break;
                    case LOOKING_FOR_PDI /*3*/:
                        processPropertySeq(levState, resProp, start1, start2);
                        processPropertySeq(levState, _ON, start2, i);
                        start1 = i;
                        break;
                    case RLM_BEFORE /*4*/:
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
        if (firstStrong != RLM_BEFORE) {
            eor = (short) firstStrong;
        }
        for (i = limit + MAP_NOWHERE; i > start; i += MAP_NOWHERE) {
            if ((DirPropFlag(this.dirProps[i]) & MASK_BN_EXPLICIT) == 0) {
                dirProp = this.dirProps[i];
                if (dirProp != SIMPLE_OPENINGS_COUNT) {
                }
                this.isolateCount += SEEKING_STRONG_FOR_PARA;
                if (this.isolates[this.isolateCount] == null) {
                    this.isolates[this.isolateCount] = new Isolate();
                }
                this.isolates[this.isolateCount].stateImp = stateImp;
                this.isolates[this.isolateCount].state = levState.state;
                this.isolates[this.isolateCount].start1 = start1;
                this.isolates[this.isolateCount].startON = levState.startON;
            }
        }
        dirProp = this.dirProps[i];
        if (dirProp != SIMPLE_OPENINGS_COUNT) {
        }
        this.isolateCount += SEEKING_STRONG_FOR_PARA;
        if (this.isolates[this.isolateCount] == null) {
            this.isolates[this.isolateCount] = new Isolate();
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
                    i += MAP_NOWHERE;
                    flag = DirPropFlag(this.dirProps[i]);
                    if ((MASK_WS & flag) == 0) {
                        break;
                    } else if (!this.orderParagraphsLTR || (DirPropFlag(B) & flag) == 0) {
                        this.levels[i] = GetParaLevelAt(i);
                    } else {
                        this.levels[i] = LTR;
                    }
                }
                while (i > 0) {
                    i += MAP_NOWHERE;
                    flag = DirPropFlag(this.dirProps[i]);
                    if ((MASK_BN_EXPLICIT & flag) == 0) {
                        if (this.orderParagraphsLTR && (DirPropFlag(B) & flag) != 0) {
                            this.levels[i] = LTR;
                            break;
                        } else if ((MASK_B_S & flag) != 0) {
                            this.levels[i] = GetParaLevelAt(i);
                            break;
                        }
                    } else {
                        this.levels[i] = this.levels[i + SEEKING_STRONG_FOR_PARA];
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

    public void setPara(String text, byte paraLevel, byte[] embeddingLevels) {
        if (text == null) {
            setPara(new char[OPTION_DEFAULT], paraLevel, embeddingLevels);
        } else {
            setPara(text.toCharArray(), paraLevel, embeddingLevels);
        }
    }

    public void setPara(char[] chars, byte paraLevel, byte[] embeddingLevels) {
        if (paraLevel < DIRECTION_DEFAULT_LEFT_TO_RIGHT) {
            verifyRange(paraLevel, OPTION_DEFAULT, DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        }
        if (chars == null) {
            chars = new char[OPTION_DEFAULT];
        }
        if (this.reorderingMode == LOOKING_FOR_PDI) {
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
        this.direction = (byte) (paraLevel & SEEKING_STRONG_FOR_PARA);
        this.paraCount = SEEKING_STRONG_FOR_PARA;
        this.dirProps = new byte[OPTION_DEFAULT];
        this.levels = new byte[OPTION_DEFAULT];
        this.runs = new BidiRun[OPTION_DEFAULT];
        this.isGoodLogicalToVisualRunsMap = false;
        this.insertPoints.size = OPTION_DEFAULT;
        this.insertPoints.confirmed = OPTION_DEFAULT;
        this.defaultParaLevel = IsDefaultLevel(paraLevel) ? paraLevel : LTR;
        if (this.length == 0) {
            if (IsDefaultLevel(paraLevel)) {
                this.paraLevel = (byte) (this.paraLevel & SEEKING_STRONG_FOR_PARA);
                this.defaultParaLevel = LTR;
            }
            this.flags = DirPropFlagLR(paraLevel);
            this.runCount = OPTION_DEFAULT;
            this.paraCount = OPTION_DEFAULT;
            setParaSuccess();
            return;
        }
        int i;
        this.runCount = MAP_NOWHERE;
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
            this.isolates = new Isolate[(this.isolateCount + LOOKING_FOR_PDI)];
        }
        this.isolateCount = MAP_NOWHERE;
        switch (this.direction) {
            case OPTION_DEFAULT /*0*/:
                this.trailingWSStart = OPTION_DEFAULT;
                break;
            case SEEKING_STRONG_FOR_PARA /*1*/:
                this.trailingWSStart = OPTION_DEFAULT;
                break;
            default:
                switch (this.reorderingMode) {
                    case OPTION_DEFAULT /*0*/:
                        this.impTabPair = impTab_DEFAULT;
                        break;
                    case SEEKING_STRONG_FOR_PARA /*1*/:
                        this.impTabPair = impTab_NUMBERS_SPECIAL;
                        break;
                    case SEEKING_STRONG_FOR_FSI /*2*/:
                        this.impTabPair = impTab_GROUP_NUMBERS_WITH_R;
                        break;
                    case LOOKING_FOR_PDI /*3*/:
                        throw new InternalError("Internal ICU error in setPara");
                    case RLM_BEFORE /*4*/:
                        this.impTabPair = impTab_INVERSE_NUMBERS_AS_L;
                        break;
                    case XmlPullParser.CDSECT /*5*/:
                        if ((this.reorderingOptions & SEEKING_STRONG_FOR_PARA) == 0) {
                            this.impTabPair = impTab_INVERSE_LIKE_DIRECT;
                            break;
                        } else {
                            this.impTabPair = impTab_INVERSE_LIKE_DIRECT_WITH_MARKS;
                            break;
                        }
                    case XmlPullParser.ENTITY_REF /*6*/:
                        if ((this.reorderingOptions & SEEKING_STRONG_FOR_PARA) == 0) {
                            this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL;
                            break;
                        } else {
                            this.impTabPair = impTab_INVERSE_FOR_NUMBERS_SPECIAL_WITH_MARKS;
                            break;
                        }
                }
                if (embeddingLevels == null && this.paraCount <= SEEKING_STRONG_FOR_PARA && (this.flags & DirPropFlagMultiRuns) == 0) {
                    resolveImplicitLevels(OPTION_DEFAULT, this.length, (short) GetLRFromLevel(GetParaLevelAt(OPTION_DEFAULT)), (short) GetLRFromLevel(GetParaLevelAt(this.length + MAP_NOWHERE)));
                } else {
                    short eor;
                    int limit = OPTION_DEFAULT;
                    byte level = GetParaLevelAt(OPTION_DEFAULT);
                    byte nextLevel = this.levels[OPTION_DEFAULT];
                    if (level < nextLevel) {
                        eor = (short) GetLRFromLevel(nextLevel);
                    } else {
                        eor = (short) GetLRFromLevel(level);
                    }
                    while (true) {
                        short sor;
                        i = limit;
                        level = nextLevel;
                        if (limit <= 0 || this.dirProps[i + MAP_NOWHERE] != IMPTABLEVELS_RES) {
                            sor = eor;
                        } else {
                            sor = (short) GetLRFromLevel(GetParaLevelAt(i));
                        }
                        while (true) {
                            limit += SEEKING_STRONG_FOR_PARA;
                            if (limit >= this.length || (this.levels[limit] != level && (DirPropFlag(this.dirProps[limit]) & MASK_BN_EXPLICIT) == 0)) {
                                if (limit < this.length) {
                                    nextLevel = this.levels[limit];
                                } else {
                                    nextLevel = GetParaLevelAt(this.length + MAP_NOWHERE);
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
                                        int start = i + SEEKING_STRONG_FOR_PARA;
                                        bArr[i] = (byte) (bArr[i] & DIRECTION_DEFAULT_RIGHT_TO_LEFT);
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
                    }
                }
                adjustWSLevels();
                break;
        }
        if (this.defaultParaLevel > null && (this.reorderingOptions & SEEKING_STRONG_FOR_PARA) != 0 && (this.reorderingMode == 5 || this.reorderingMode == 6)) {
            int i2 = OPTION_DEFAULT;
            while (i2 < this.paraCount) {
                int last = this.paras_limit[i2] + MAP_NOWHERE;
                if (this.paras_level[i2] != null) {
                    i = i2 == 0 ? OPTION_DEFAULT : this.paras_limit[i2 + MAP_NOWHERE];
                    int j = last;
                    while (j >= i) {
                        byte dirProp = this.dirProps[j];
                        if (dirProp == null) {
                            if (j < last) {
                                while (this.dirProps[last] == IMPTABLEVELS_RES) {
                                    last += MAP_NOWHERE;
                                }
                            }
                            addPoint(last, RLM_BEFORE);
                        } else if ((DirPropFlag(dirProp) & MASK_R_AL) == 0) {
                            j += MAP_NOWHERE;
                        }
                    }
                }
                i2 += SEEKING_STRONG_FOR_PARA;
            }
        }
        if ((this.reorderingOptions & SEEKING_STRONG_FOR_FSI) != 0) {
            this.resultLength -= this.controlCount;
        } else {
            this.resultLength += this.insertPoints.size;
        }
        setParaSuccess();
    }

    public void setPara(AttributedCharacterIterator paragraph) {
        Boolean runDirection = (Boolean) paragraph.getAttribute(TextAttribute.RUN_DIRECTION);
        byte paraLvl = runDirection == null ? LEVEL_DEFAULT_LTR : runDirection.equals(TextAttribute.RUN_DIRECTION_LTR) ? LTR : RTL;
        byte[] lvls = null;
        int len = paragraph.getEndIndex() - paragraph.getBeginIndex();
        byte[] embeddingLevels = new byte[len];
        char[] txt = new char[len];
        int i = OPTION_DEFAULT;
        char ch = paragraph.first();
        while (ch != UnicodeMatcher.ETHER) {
            txt[i] = ch;
            Integer embedding = (Integer) paragraph.getAttribute(TextAttribute.BIDI_EMBEDDING);
            if (embedding != null) {
                byte level = embedding.byteValue();
                if (level != null) {
                    if (level < null) {
                        lvls = embeddingLevels;
                        embeddingLevels[i] = (byte) ((0 - level) | -128);
                    } else {
                        lvls = embeddingLevels;
                        embeddingLevels[i] = level;
                    }
                }
            }
            ch = paragraph.next();
            i += SEEKING_STRONG_FOR_PARA;
        }
        NumericShaper shaper = (NumericShaper) paragraph.getAttribute(TextAttribute.NUMERIC_SHAPING);
        if (shaper != null) {
            shaper.shape(txt, OPTION_DEFAULT, len);
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
        verifyRange(paraIndex, OPTION_DEFAULT, this.paraCount);
        Bidi bidi = this.paraBidi;
        if (paraIndex == 0) {
            paraStart = OPTION_DEFAULT;
        } else {
            paraStart = bidi.paras_limit[paraIndex + MAP_NOWHERE];
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
        verifyRange(charIndex, OPTION_DEFAULT, bidi.length);
        int paraIndex = OPTION_DEFAULT;
        while (charIndex >= bidi.paras_limit[paraIndex]) {
            paraIndex += SEEKING_STRONG_FOR_PARA;
        }
        return getParagraphByIndex(paraIndex);
    }

    public int getParagraphIndex(int charIndex) {
        verifyValidParaOrLine();
        Bidi bidi = this.paraBidi;
        verifyRange(charIndex, OPTION_DEFAULT, bidi.length);
        int paraIndex = OPTION_DEFAULT;
        while (charIndex >= bidi.paras_limit[paraIndex]) {
            paraIndex += SEEKING_STRONG_FOR_PARA;
        }
        return paraIndex;
    }

    public void setCustomClassifier(BidiClassifier classifier) {
        this.customClassifier = classifier;
    }

    public BidiClassifier getCustomClassifier() {
        return this.customClassifier;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getCustomizedClass(int c) {
        int dir;
        if (this.customClassifier != null) {
            dir = this.customClassifier.classify(c);
        }
        dir = this.bdp.getClass(c);
        if (dir >= CLASS_DEFAULT) {
            return SIMPLE_PARAS_COUNT;
        }
        return dir;
    }

    public Bidi setLine(int start, int limit) {
        verifyValidPara();
        verifyRange(start, OPTION_DEFAULT, limit);
        verifyRange(limit, OPTION_DEFAULT, this.length + SEEKING_STRONG_FOR_PARA);
        if (getParagraphIndex(start) == getParagraphIndex(limit + MAP_NOWHERE)) {
            return BidiLine.setLine(this, start, limit);
        }
        throw new IllegalArgumentException();
    }

    public byte getLevelAt(int charIndex) {
        verifyValidParaOrLine();
        verifyRange(charIndex, OPTION_DEFAULT, this.length);
        return BidiLine.getLevelAt(this, charIndex);
    }

    public byte[] getLevels() {
        verifyValidParaOrLine();
        if (this.length <= 0) {
            return new byte[OPTION_DEFAULT];
        }
        return BidiLine.getLevels(this);
    }

    public BidiRun getLogicalRun(int logicalPosition) {
        verifyValidParaOrLine();
        verifyRange(logicalPosition, OPTION_DEFAULT, this.length);
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
        verifyRange(runIndex, OPTION_DEFAULT, this.runCount);
        return BidiLine.getVisualRun(this, runIndex);
    }

    public int getVisualIndex(int logicalIndex) {
        verifyValidParaOrLine();
        verifyRange(logicalIndex, OPTION_DEFAULT, this.length);
        return BidiLine.getVisualIndex(this, logicalIndex);
    }

    public int getLogicalIndex(int visualIndex) {
        verifyValidParaOrLine();
        verifyRange(visualIndex, OPTION_DEFAULT, this.resultLength);
        if (this.insertPoints.size == 0 && this.controlCount == 0) {
            if (this.direction == null) {
                return visualIndex;
            }
            if (this.direction == SEEKING_STRONG_FOR_PARA) {
                return (this.length - visualIndex) + MAP_NOWHERE;
            }
        }
        BidiLine.getRuns(this);
        return BidiLine.getLogicalIndex(this, visualIndex);
    }

    public int[] getLogicalMap() {
        countRuns();
        if (this.length <= 0) {
            return new int[OPTION_DEFAULT];
        }
        return BidiLine.getLogicalMap(this);
    }

    public int[] getVisualMap() {
        countRuns();
        if (this.resultLength <= 0) {
            return new int[OPTION_DEFAULT];
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
        this(paragraph.toCharArray(), OPTION_DEFAULT, null, OPTION_DEFAULT, paragraph.length(), flags);
    }

    public Bidi(AttributedCharacterIterator paragraph) {
        this();
        setPara(paragraph);
    }

    public Bidi(char[] text, int textStart, byte[] embeddings, int embStart, int paragraphLength, int flags) {
        byte paraLvl;
        byte[] bArr;
        this();
        switch (flags) {
            case SEEKING_STRONG_FOR_PARA /*1*/:
                paraLvl = RTL;
                break;
            case DIRECTION_DEFAULT_LEFT_TO_RIGHT /*126*/:
                paraLvl = LEVEL_DEFAULT_LTR;
                break;
            case DIRECTION_DEFAULT_RIGHT_TO_LEFT /*127*/:
                paraLvl = LEVEL_DEFAULT_RTL;
                break;
            default:
                paraLvl = LTR;
                break;
        }
        if (embeddings == null) {
            bArr = null;
        } else {
            bArr = new byte[paragraphLength];
            for (int i = OPTION_DEFAULT; i < paragraphLength; i += SEEKING_STRONG_FOR_PARA) {
                byte lev = embeddings[i + embStart];
                if (lev < null) {
                    lev = (byte) ((-lev) | -128);
                } else if (lev == null) {
                    lev = paraLvl;
                    if (paraLvl > 125) {
                        lev = (byte) (lev & SEEKING_STRONG_FOR_PARA);
                    }
                }
                bArr[i] = lev;
            }
        }
        if (textStart == 0 && embStart == 0 && paragraphLength == text.length) {
            setPara(text, paraLvl, bArr);
            return;
        }
        char[] paraText = new char[paragraphLength];
        System.arraycopy(text, textStart, paraText, OPTION_DEFAULT, paragraphLength);
        setPara(paraText, paraLvl, bArr);
    }

    public Bidi createLineBidi(int lineStart, int lineLimit) {
        return setLine(lineStart, lineLimit);
    }

    public boolean isMixed() {
        return (isLeftToRight() || isRightToLeft()) ? false : true;
    }

    public boolean isLeftToRight() {
        return getDirection() == null && (this.paraLevel & SEEKING_STRONG_FOR_PARA) == 0;
    }

    public boolean isRightToLeft() {
        return getDirection() == RTL && (this.paraLevel & SEEKING_STRONG_FOR_PARA) == SEEKING_STRONG_FOR_PARA;
    }

    public boolean baseIsLeftToRight() {
        return getParaLevel() == null;
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
            for (i = OPTION_DEFAULT; i < count; i += SEEKING_STRONG_FOR_PARA) {
                keys[i] = (((long) this.runs[i].start) << 32) + ((long) i);
            }
            Arrays.sort(keys);
            for (i = OPTION_DEFAULT; i < count; i += SEEKING_STRONG_FOR_PARA) {
                this.logicalToVisualRunsMap[i] = (int) (keys[i] & -1);
            }
            this.isGoodLogicalToVisualRunsMap = true;
        }
    }

    public int getRunLevel(int run) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, OPTION_DEFAULT, this.runCount);
        getLogicalToVisualRunsMap();
        return this.runs[this.logicalToVisualRunsMap[run]].level;
    }

    public int getRunStart(int run) {
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, OPTION_DEFAULT, this.runCount);
        getLogicalToVisualRunsMap();
        return this.runs[this.logicalToVisualRunsMap[run]].start;
    }

    public int getRunLimit(int run) {
        int len;
        verifyValidParaOrLine();
        BidiLine.getRuns(this);
        verifyRange(run, OPTION_DEFAULT, this.runCount);
        getLogicalToVisualRunsMap();
        int idx = this.logicalToVisualRunsMap[run];
        if (idx == 0) {
            len = this.runs[idx].limit;
        } else {
            len = this.runs[idx].limit - this.runs[idx + MAP_NOWHERE].limit;
        }
        return this.runs[idx].start + len;
    }

    public static boolean requiresBidi(char[] text, int start, int limit) {
        for (int i = start; i < limit; i += SEEKING_STRONG_FOR_PARA) {
            if (((SEEKING_STRONG_FOR_PARA << UCharacter.getDirection(text[i])) & 57378) != 0) {
                return true;
            }
        }
        return false;
    }

    public static void reorderVisually(byte[] levels, int levelStart, Object[] objects, int objectStart, int count) {
        byte[] reorderLevels = new byte[count];
        System.arraycopy(levels, levelStart, reorderLevels, OPTION_DEFAULT, count);
        int[] indexMap = reorderVisual(reorderLevels);
        Object[] temp = new Object[count];
        System.arraycopy(objects, objectStart, temp, OPTION_DEFAULT, count);
        for (int i = OPTION_DEFAULT; i < count; i += SEEKING_STRONG_FOR_PARA) {
            objects[objectStart + i] = temp[indexMap[i]];
        }
    }

    public String writeReordered(int options) {
        verifyValidParaOrLine();
        if (this.length == 0) {
            return XmlPullParser.NO_NAMESPACE;
        }
        return BidiWriter.writeReordered(this, options);
    }

    public static String writeReverse(String src, int options) {
        if (src == null) {
            throw new IllegalArgumentException();
        } else if (src.length() > 0) {
            return BidiWriter.writeReverse(src, options);
        } else {
            return XmlPullParser.NO_NAMESPACE;
        }
    }
}
