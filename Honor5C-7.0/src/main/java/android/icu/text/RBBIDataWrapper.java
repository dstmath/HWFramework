package android.icu.text;

import android.icu.impl.CharTrie;
import android.icu.impl.ICUBinary;
import android.icu.impl.ICUBinary.Authenticate;
import android.icu.impl.Trie.DataManipulate;
import android.icu.impl.locale.LanguageTag;
import com.android.dex.DexFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import libcore.icu.DateUtilsBridge;
import org.xmlpull.v1.XmlPullParser;

final class RBBIDataWrapper {
    static final int ACCEPTING = 0;
    static final int DATA_FORMAT = 1114794784;
    static final int DH_CATCOUNT = 3;
    static final int DH_FORMATVERSION = 1;
    static final int DH_FTABLE = 4;
    static final int DH_FTABLELEN = 5;
    static final int DH_LENGTH = 2;
    static final int DH_MAGIC = 0;
    static final int DH_RTABLE = 6;
    static final int DH_RTABLELEN = 7;
    static final int DH_RULESOURCE = 14;
    static final int DH_RULESOURCELEN = 15;
    static final int DH_SFTABLE = 8;
    static final int DH_SFTABLELEN = 9;
    static final int DH_SIZE = 24;
    static final int DH_SRTABLE = 10;
    static final int DH_SRTABLELEN = 11;
    static final int DH_STATUSTABLE = 16;
    static final int DH_STATUSTABLELEN = 17;
    static final int DH_TRIE = 12;
    static final int DH_TRIELEN = 13;
    static final int FLAGS = 4;
    static final int FORMAT_VERSION = 50397184;
    private static final IsAcceptable IS_ACCEPTABLE = null;
    static final int LOOKAHEAD = 1;
    static final int NEXTSTATES = 4;
    static final int NUMSTATES = 0;
    static final int RBBI_BOF_REQUIRED = 2;
    static final int RBBI_LOOKAHEAD_HARD_BREAK = 1;
    static final int RESERVED = 3;
    static final int ROWLEN = 2;
    private static final int ROW_DATA = 8;
    static final int TAGIDX = 2;
    static TrieFoldingFunc fTrieFoldingFunc;
    short[] fFTable;
    RBBIDataHeader fHeader;
    short[] fRTable;
    String fRuleSource;
    short[] fSFTable;
    short[] fSRTable;
    int[] fStatusTable;
    CharTrie fTrie;
    private boolean isBigEndian;

    private static final class IsAcceptable implements Authenticate {
        private IsAcceptable() {
        }

        public boolean isDataVersionAcceptable(byte[] version) {
            return version[RBBIDataWrapper.NUMSTATES] == RBBIDataWrapper.RESERVED;
        }
    }

    static final class RBBIDataHeader {
        int fCatCount;
        int fFTable;
        int fFTableLen;
        byte[] fFormatVersion;
        int fLength;
        int fMagic;
        int fRTable;
        int fRTableLen;
        int fRuleSource;
        int fRuleSourceLen;
        int fSFTable;
        int fSFTableLen;
        int fSRTable;
        int fSRTableLen;
        int fStatusTable;
        int fStatusTableLen;
        int fTrie;
        int fTrieLen;
        int fVersion;

        public RBBIDataHeader() {
            this.fMagic = RBBIDataWrapper.NUMSTATES;
            this.fFormatVersion = new byte[RBBIDataWrapper.NEXTSTATES];
        }
    }

    static class TrieFoldingFunc implements DataManipulate {
        TrieFoldingFunc() {
        }

        public int getFoldingOffset(int data) {
            if ((DateUtilsBridge.FORMAT_ABBREV_WEEKDAY & data) != 0) {
                return data & 32767;
            }
            return RBBIDataWrapper.NUMSTATES;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.text.RBBIDataWrapper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.text.RBBIDataWrapper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBBIDataWrapper.<clinit>():void");
    }

    int getRowIndex(int state) {
        return ((this.fHeader.fCatCount + NEXTSTATES) * state) + ROW_DATA;
    }

    RBBIDataWrapper() {
    }

    static RBBIDataWrapper get(ByteBuffer bytes) throws IOException {
        RBBIDataWrapper This = new RBBIDataWrapper();
        ICUBinary.readHeader(bytes, DATA_FORMAT, IS_ACCEPTABLE);
        This.isBigEndian = bytes.order() == ByteOrder.BIG_ENDIAN;
        This.fHeader = new RBBIDataHeader();
        This.fHeader.fMagic = bytes.getInt();
        This.fHeader.fVersion = bytes.getInt(bytes.position());
        This.fHeader.fFormatVersion[NUMSTATES] = bytes.get();
        This.fHeader.fFormatVersion[RBBI_LOOKAHEAD_HARD_BREAK] = bytes.get();
        This.fHeader.fFormatVersion[TAGIDX] = bytes.get();
        This.fHeader.fFormatVersion[RESERVED] = bytes.get();
        This.fHeader.fLength = bytes.getInt();
        This.fHeader.fCatCount = bytes.getInt();
        This.fHeader.fFTable = bytes.getInt();
        This.fHeader.fFTableLen = bytes.getInt();
        This.fHeader.fRTable = bytes.getInt();
        This.fHeader.fRTableLen = bytes.getInt();
        This.fHeader.fSFTable = bytes.getInt();
        This.fHeader.fSFTableLen = bytes.getInt();
        This.fHeader.fSRTable = bytes.getInt();
        This.fHeader.fSRTableLen = bytes.getInt();
        This.fHeader.fTrie = bytes.getInt();
        This.fHeader.fTrieLen = bytes.getInt();
        This.fHeader.fRuleSource = bytes.getInt();
        This.fHeader.fRuleSourceLen = bytes.getInt();
        This.fHeader.fStatusTable = bytes.getInt();
        This.fHeader.fStatusTableLen = bytes.getInt();
        ICUBinary.skipBytes(bytes, DH_SIZE);
        if (This.fHeader.fMagic != 45472 || (This.fHeader.fVersion != RBBI_LOOKAHEAD_HARD_BREAK && This.fHeader.fFormatVersion[NUMSTATES] != (byte) 3)) {
            throw new IOException("Break Iterator Rule Data Magic Number Incorrect, or unsupported data version.");
        } else if (This.fHeader.fFTable < 96 || This.fHeader.fFTable > This.fHeader.fLength) {
            throw new IOException("Break iterator Rule data corrupt");
        } else {
            ICUBinary.skipBytes(bytes, This.fHeader.fFTable - 96);
            int pos = This.fHeader.fFTable;
            This.fFTable = ICUBinary.getShorts(bytes, This.fHeader.fFTableLen / TAGIDX, This.fHeader.fFTableLen & RBBI_LOOKAHEAD_HARD_BREAK);
            ICUBinary.skipBytes(bytes, This.fHeader.fRTable - (pos + This.fHeader.fFTableLen));
            pos = This.fHeader.fRTable;
            This.fRTable = ICUBinary.getShorts(bytes, This.fHeader.fRTableLen / TAGIDX, This.fHeader.fRTableLen & RBBI_LOOKAHEAD_HARD_BREAK);
            pos += This.fHeader.fRTableLen;
            if (This.fHeader.fSFTableLen > 0) {
                ICUBinary.skipBytes(bytes, This.fHeader.fSFTable - pos);
                pos = This.fHeader.fSFTable;
                This.fSFTable = ICUBinary.getShorts(bytes, This.fHeader.fSFTableLen / TAGIDX, This.fHeader.fSFTableLen & RBBI_LOOKAHEAD_HARD_BREAK);
                pos += This.fHeader.fSFTableLen;
            }
            if (This.fHeader.fSRTableLen > 0) {
                ICUBinary.skipBytes(bytes, This.fHeader.fSRTable - pos);
                pos = This.fHeader.fSRTable;
                This.fSRTable = ICUBinary.getShorts(bytes, This.fHeader.fSRTableLen / TAGIDX, This.fHeader.fSRTableLen & RBBI_LOOKAHEAD_HARD_BREAK);
                pos += This.fHeader.fSRTableLen;
            }
            ICUBinary.skipBytes(bytes, This.fHeader.fTrie - pos);
            pos = This.fHeader.fTrie;
            bytes.mark();
            This.fTrie = new CharTrie(bytes, fTrieFoldingFunc);
            bytes.reset();
            if (pos > This.fHeader.fStatusTable) {
                throw new IOException("Break iterator Rule data corrupt");
            }
            ICUBinary.skipBytes(bytes, This.fHeader.fStatusTable - pos);
            pos = This.fHeader.fStatusTable;
            This.fStatusTable = ICUBinary.getInts(bytes, This.fHeader.fStatusTableLen / NEXTSTATES, This.fHeader.fStatusTableLen & RESERVED);
            pos += This.fHeader.fStatusTableLen;
            if (pos > This.fHeader.fRuleSource) {
                throw new IOException("Break iterator Rule data corrupt");
            }
            ICUBinary.skipBytes(bytes, This.fHeader.fRuleSource - pos);
            pos = This.fHeader.fRuleSource;
            This.fRuleSource = ICUBinary.getString(bytes, This.fHeader.fRuleSourceLen / TAGIDX, This.fHeader.fRuleSourceLen & RBBI_LOOKAHEAD_HARD_BREAK);
            if (RuleBasedBreakIterator.fDebugEnv != null && RuleBasedBreakIterator.fDebugEnv.indexOf("data") >= 0) {
                This.dump();
            }
            return This;
        }
    }

    private int getStateTableNumStates(short[] table) {
        if (this.isBigEndian) {
            return (table[NUMSTATES] << DH_STATUSTABLE) | (table[RBBI_LOOKAHEAD_HARD_BREAK] & DexFormat.MAX_TYPE_IDX);
        }
        return (table[RBBI_LOOKAHEAD_HARD_BREAK] << DH_STATUSTABLE) | (table[NUMSTATES] & DexFormat.MAX_TYPE_IDX);
    }

    int getStateTableFlags(short[] table) {
        return table[this.isBigEndian ? DH_FTABLELEN : NEXTSTATES];
    }

    void dump() {
        if (this.fFTable.length == 0) {
            throw new NullPointerException();
        }
        System.out.println("RBBI Data Wrapper dump ...");
        System.out.println();
        System.out.println("Forward State Table");
        dumpTable(this.fFTable);
        System.out.println("Reverse State Table");
        dumpTable(this.fRTable);
        System.out.println("Forward Safe Points Table");
        dumpTable(this.fSFTable);
        System.out.println("Reverse Safe Points Table");
        dumpTable(this.fSRTable);
        dumpCharCategories();
        System.out.println("Source Rules: " + this.fRuleSource);
    }

    public static String intToString(int n, int width) {
        StringBuilder dest = new StringBuilder(width);
        dest.append(n);
        while (dest.length() < width) {
            dest.insert(NUMSTATES, ' ');
        }
        return dest.toString();
    }

    public static String intToHexString(int n, int width) {
        StringBuilder dest = new StringBuilder(width);
        dest.append(Integer.toHexString(n));
        while (dest.length() < width) {
            dest.insert(NUMSTATES, ' ');
        }
        return dest.toString();
    }

    private void dumpTable(short[] table) {
        if (table == null) {
            System.out.println("  -- null -- ");
            return;
        }
        int n;
        StringBuilder header = new StringBuilder(" Row  Acc Look  Tag");
        for (n = NUMSTATES; n < this.fHeader.fCatCount; n += RBBI_LOOKAHEAD_HARD_BREAK) {
            header.append(intToString(n, DH_FTABLELEN));
        }
        System.out.println(header.toString());
        for (n = NUMSTATES; n < header.length(); n += RBBI_LOOKAHEAD_HARD_BREAK) {
            System.out.print(LanguageTag.SEP);
        }
        System.out.println();
        for (int state = NUMSTATES; state < getStateTableNumStates(table); state += RBBI_LOOKAHEAD_HARD_BREAK) {
            dumpRow(table, state);
        }
        System.out.println();
    }

    private void dumpRow(short[] table, int state) {
        StringBuilder dest = new StringBuilder((this.fHeader.fCatCount * DH_FTABLELEN) + 20);
        dest.append(intToString(state, NEXTSTATES));
        int row = getRowIndex(state);
        if (table[row + NUMSTATES] != (short) 0) {
            dest.append(intToString(table[row + NUMSTATES], DH_FTABLELEN));
        } else {
            dest.append("     ");
        }
        if (table[row + RBBI_LOOKAHEAD_HARD_BREAK] != (short) 0) {
            dest.append(intToString(table[row + RBBI_LOOKAHEAD_HARD_BREAK], DH_FTABLELEN));
        } else {
            dest.append("     ");
        }
        dest.append(intToString(table[row + TAGIDX], DH_FTABLELEN));
        for (int col = NUMSTATES; col < this.fHeader.fCatCount; col += RBBI_LOOKAHEAD_HARD_BREAK) {
            dest.append(intToString(table[(row + NEXTSTATES) + col], DH_FTABLELEN));
        }
        System.out.println(dest);
    }

    private void dumpCharCategories() {
        int category;
        int n = this.fHeader.fCatCount;
        String[] catStrings = new String[(n + RBBI_LOOKAHEAD_HARD_BREAK)];
        int rangeStart = NUMSTATES;
        int rangeEnd = NUMSTATES;
        int lastCat = -1;
        int[] lastNewline = new int[(n + RBBI_LOOKAHEAD_HARD_BREAK)];
        for (category = NUMSTATES; category <= this.fHeader.fCatCount; category += RBBI_LOOKAHEAD_HARD_BREAK) {
            catStrings[category] = XmlPullParser.NO_NAMESPACE;
        }
        System.out.println("\nCharacter Categories");
        System.out.println("--------------------");
        for (int char32 = NUMSTATES; char32 <= UnicodeSet.MAX_VALUE; char32 += RBBI_LOOKAHEAD_HARD_BREAK) {
            category = this.fTrie.getCodePointValue(char32) & -16385;
            if (category < 0 || category > this.fHeader.fCatCount) {
                System.out.println("Error, bad category " + Integer.toHexString(category) + " for char " + Integer.toHexString(char32));
                break;
            }
            if (category == lastCat) {
                rangeEnd = char32;
            } else {
                if (lastCat >= 0) {
                    if (catStrings[lastCat].length() > lastNewline[lastCat] + 70) {
                        lastNewline[lastCat] = catStrings[lastCat].length() + DH_SRTABLE;
                        catStrings[lastCat] = catStrings[lastCat] + "\n       ";
                    }
                    catStrings[lastCat] = catStrings[lastCat] + " " + Integer.toHexString(rangeStart);
                    if (rangeEnd != rangeStart) {
                        catStrings[lastCat] = catStrings[lastCat] + LanguageTag.SEP + Integer.toHexString(rangeEnd);
                    }
                }
                lastCat = category;
                rangeEnd = char32;
                rangeStart = char32;
            }
        }
        catStrings[lastCat] = catStrings[lastCat] + " " + Integer.toHexString(rangeStart);
        if (rangeEnd != rangeStart) {
            catStrings[lastCat] = catStrings[lastCat] + LanguageTag.SEP + Integer.toHexString(rangeEnd);
        }
        for (category = NUMSTATES; category <= this.fHeader.fCatCount; category += RBBI_LOOKAHEAD_HARD_BREAK) {
            System.out.println(intToString(category, DH_FTABLELEN) + "  " + catStrings[category]);
        }
        System.out.println();
    }
}
