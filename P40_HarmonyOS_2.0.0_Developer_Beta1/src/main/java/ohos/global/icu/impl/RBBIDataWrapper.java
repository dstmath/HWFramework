package ohos.global.icu.impl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import ohos.global.icu.impl.ICUBinary;
import ohos.global.icu.impl.locale.LanguageTag;
import ohos.global.icu.lang.UCharacterEnums;
import ohos.global.icu.text.RuleBasedBreakIterator;

public final class RBBIDataWrapper {
    public static final int ACCEPTING = 0;
    public static final int DATA_FORMAT = 1114794784;
    public static final int DH_CATCOUNT = 3;
    public static final int DH_FORMATVERSION = 1;
    public static final int DH_FTABLE = 4;
    public static final int DH_FTABLELEN = 5;
    public static final int DH_LENGTH = 2;
    public static final int DH_MAGIC = 0;
    public static final int DH_RTABLE = 6;
    public static final int DH_RTABLELEN = 7;
    public static final int DH_RULESOURCE = 10;
    public static final int DH_RULESOURCELEN = 11;
    public static final int DH_SIZE = 20;
    public static final int DH_STATUSTABLE = 12;
    public static final int DH_STATUSTABLELEN = 13;
    public static final int DH_TRIE = 8;
    public static final int DH_TRIELEN = 9;
    public static final int FORMAT_VERSION = 83886080;
    private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();
    public static final int LOOKAHEAD = 1;
    public static final int NEXTSTATES = 4;
    public static final int RBBI_BOF_REQUIRED = 2;
    public static final int RBBI_LOOKAHEAD_HARD_BREAK = 1;
    public static final int RESERVED = 3;
    public static final int TAGIDX = 2;
    public RBBIStateTable fFTable;
    public RBBIDataHeader fHeader;
    public RBBIStateTable fRTable;
    public String fRuleSource;
    public int[] fStatusTable;
    public Trie2 fTrie;

    public static final class RBBIDataHeader {
        public int fCatCount;
        int fFTable;
        int fFTableLen;
        byte[] fFormatVersion = new byte[4];
        int fLength;
        int fMagic = 0;
        int fRTable;
        int fRTableLen;
        int fRuleSource;
        int fRuleSourceLen;
        int fStatusTable;
        int fStatusTableLen;
        int fTrie;
        int fTrieLen;
    }

    public static class RBBIStateTable {
        public int fFlags;
        public int fNumStates;
        public int fReserved;
        public int fRowLen;
        public short[] fTable;

        static RBBIStateTable get(ByteBuffer byteBuffer, int i) throws IOException {
            if (i == 0) {
                return null;
            }
            if (i >= 16) {
                RBBIStateTable rBBIStateTable = new RBBIStateTable();
                rBBIStateTable.fNumStates = byteBuffer.getInt();
                rBBIStateTable.fRowLen = byteBuffer.getInt();
                rBBIStateTable.fFlags = byteBuffer.getInt();
                rBBIStateTable.fReserved = byteBuffer.getInt();
                int i2 = i - 16;
                rBBIStateTable.fTable = ICUBinary.getShorts(byteBuffer, i2 / 2, i2 & 1);
                return rBBIStateTable;
            }
            throw new IOException("Invalid RBBI state table length.");
        }

        public int put(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeInt(this.fNumStates);
            dataOutputStream.writeInt(this.fRowLen);
            dataOutputStream.writeInt(this.fFlags);
            dataOutputStream.writeInt(this.fReserved);
            int i = (this.fRowLen * this.fNumStates) / 2;
            for (int i2 = 0; i2 < i; i2++) {
                dataOutputStream.writeShort(this.fTable[i2]);
            }
            int i3 = (this.fRowLen * this.fNumStates) + 16;
            while (i3 % 8 != 0) {
                dataOutputStream.writeByte(0);
                i3++;
            }
            return i3;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RBBIStateTable)) {
                return false;
            }
            RBBIStateTable rBBIStateTable = (RBBIStateTable) obj;
            if (this.fNumStates == rBBIStateTable.fNumStates && this.fRowLen == rBBIStateTable.fRowLen && this.fFlags == rBBIStateTable.fFlags && this.fReserved == rBBIStateTable.fReserved) {
                return Arrays.equals(this.fTable, rBBIStateTable.fTable);
            }
            return false;
        }
    }

    public static boolean equals(RBBIStateTable rBBIStateTable, RBBIStateTable rBBIStateTable2) {
        if (rBBIStateTable == rBBIStateTable2) {
            return true;
        }
        if (rBBIStateTable == null || rBBIStateTable2 == null) {
            return false;
        }
        return rBBIStateTable.equals(rBBIStateTable2);
    }

    private static final class IsAcceptable implements ICUBinary.Authenticate {
        private IsAcceptable() {
        }

        @Override // ohos.global.icu.impl.ICUBinary.Authenticate
        public boolean isDataVersionAcceptable(byte[] bArr) {
            return (((bArr[0] << UCharacterEnums.ECharacterCategory.MATH_SYMBOL) + (bArr[1] << 16)) + (bArr[2] << 8)) + bArr[3] == 83886080;
        }
    }

    public int getRowIndex(int i) {
        return i * (this.fHeader.fCatCount + 4);
    }

    RBBIDataWrapper() {
    }

    public static RBBIDataWrapper get(ByteBuffer byteBuffer) throws IOException {
        RBBIDataWrapper rBBIDataWrapper = new RBBIDataWrapper();
        ICUBinary.readHeader(byteBuffer, DATA_FORMAT, IS_ACCEPTABLE);
        rBBIDataWrapper.fHeader = new RBBIDataHeader();
        rBBIDataWrapper.fHeader.fMagic = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fFormatVersion[0] = byteBuffer.get();
        rBBIDataWrapper.fHeader.fFormatVersion[1] = byteBuffer.get();
        rBBIDataWrapper.fHeader.fFormatVersion[2] = byteBuffer.get();
        rBBIDataWrapper.fHeader.fFormatVersion[3] = byteBuffer.get();
        rBBIDataWrapper.fHeader.fLength = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fCatCount = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fFTable = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fFTableLen = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fRTable = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fRTableLen = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fTrie = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fTrieLen = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fRuleSource = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fRuleSourceLen = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fStatusTable = byteBuffer.getInt();
        rBBIDataWrapper.fHeader.fStatusTableLen = byteBuffer.getInt();
        ICUBinary.skipBytes(byteBuffer, 24);
        if (rBBIDataWrapper.fHeader.fMagic != 45472 || !IS_ACCEPTABLE.isDataVersionAcceptable(rBBIDataWrapper.fHeader.fFormatVersion)) {
            throw new IOException("Break Iterator Rule Data Magic Number Incorrect, or unsupported data version.");
        } else if (rBBIDataWrapper.fHeader.fFTable < 80 || rBBIDataWrapper.fHeader.fFTable > rBBIDataWrapper.fHeader.fLength) {
            throw new IOException("Break iterator Rule data corrupt");
        } else {
            ICUBinary.skipBytes(byteBuffer, rBBIDataWrapper.fHeader.fFTable - 80);
            int i = rBBIDataWrapper.fHeader.fFTable;
            rBBIDataWrapper.fFTable = RBBIStateTable.get(byteBuffer, rBBIDataWrapper.fHeader.fFTableLen);
            ICUBinary.skipBytes(byteBuffer, rBBIDataWrapper.fHeader.fRTable - (i + rBBIDataWrapper.fHeader.fFTableLen));
            int i2 = rBBIDataWrapper.fHeader.fRTable;
            rBBIDataWrapper.fRTable = RBBIStateTable.get(byteBuffer, rBBIDataWrapper.fHeader.fRTableLen);
            ICUBinary.skipBytes(byteBuffer, rBBIDataWrapper.fHeader.fTrie - (i2 + rBBIDataWrapper.fHeader.fRTableLen));
            int i3 = rBBIDataWrapper.fHeader.fTrie;
            byteBuffer.mark();
            rBBIDataWrapper.fTrie = Trie2.createFromSerialized(byteBuffer);
            byteBuffer.reset();
            if (i3 <= rBBIDataWrapper.fHeader.fStatusTable) {
                ICUBinary.skipBytes(byteBuffer, rBBIDataWrapper.fHeader.fStatusTable - i3);
                int i4 = rBBIDataWrapper.fHeader.fStatusTable;
                rBBIDataWrapper.fStatusTable = ICUBinary.getInts(byteBuffer, rBBIDataWrapper.fHeader.fStatusTableLen / 4, 3 & rBBIDataWrapper.fHeader.fStatusTableLen);
                int i5 = i4 + rBBIDataWrapper.fHeader.fStatusTableLen;
                if (i5 <= rBBIDataWrapper.fHeader.fRuleSource) {
                    ICUBinary.skipBytes(byteBuffer, rBBIDataWrapper.fHeader.fRuleSource - i5);
                    int i6 = rBBIDataWrapper.fHeader.fRuleSource;
                    rBBIDataWrapper.fRuleSource = ICUBinary.getString(byteBuffer, rBBIDataWrapper.fHeader.fRuleSourceLen / 2, rBBIDataWrapper.fHeader.fRuleSourceLen & 1);
                    if (RuleBasedBreakIterator.fDebugEnv != null && RuleBasedBreakIterator.fDebugEnv.indexOf("data") >= 0) {
                        rBBIDataWrapper.dump(System.out);
                    }
                    return rBBIDataWrapper;
                }
                throw new IOException("Break iterator Rule data corrupt");
            }
            throw new IOException("Break iterator Rule data corrupt");
        }
    }

    public void dump(PrintStream printStream) {
        if (this.fFTable != null) {
            printStream.println("RBBI Data Wrapper dump ...");
            printStream.println();
            printStream.println("Forward State Table");
            dumpTable(printStream, this.fFTable);
            printStream.println("Reverse State Table");
            dumpTable(printStream, this.fRTable);
            dumpCharCategories(printStream);
            printStream.println("Source Rules: " + this.fRuleSource);
            return;
        }
        throw new NullPointerException();
    }

    public static String intToString(int i, int i2) {
        StringBuilder sb = new StringBuilder(i2);
        sb.append(i);
        while (sb.length() < i2) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    public static String intToHexString(int i, int i2) {
        StringBuilder sb = new StringBuilder(i2);
        sb.append(Integer.toHexString(i));
        while (sb.length() < i2) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    private void dumpTable(PrintStream printStream, RBBIStateTable rBBIStateTable) {
        if (rBBIStateTable == null || rBBIStateTable.fTable.length == 0) {
            printStream.println("  -- null -- ");
            return;
        }
        StringBuilder sb = new StringBuilder(" Row  Acc Look  Tag");
        for (int i = 0; i < this.fHeader.fCatCount; i++) {
            sb.append(intToString(i, 5));
        }
        printStream.println(sb.toString());
        for (int i2 = 0; i2 < sb.length(); i2++) {
            printStream.print(LanguageTag.SEP);
        }
        printStream.println();
        for (int i3 = 0; i3 < rBBIStateTable.fNumStates; i3++) {
            dumpRow(printStream, rBBIStateTable, i3);
        }
        printStream.println();
    }

    private void dumpRow(PrintStream printStream, RBBIStateTable rBBIStateTable, int i) {
        StringBuilder sb = new StringBuilder((this.fHeader.fCatCount * 5) + 20);
        sb.append(intToString(i, 4));
        int rowIndex = getRowIndex(i);
        int i2 = rowIndex + 0;
        if (rBBIStateTable.fTable[i2] != 0) {
            sb.append(intToString(rBBIStateTable.fTable[i2], 5));
        } else {
            sb.append("     ");
        }
        int i3 = rowIndex + 1;
        if (rBBIStateTable.fTable[i3] != 0) {
            sb.append(intToString(rBBIStateTable.fTable[i3], 5));
        } else {
            sb.append("     ");
        }
        sb.append(intToString(rBBIStateTable.fTable[rowIndex + 2], 5));
        for (int i4 = 0; i4 < this.fHeader.fCatCount; i4++) {
            sb.append(intToString(rBBIStateTable.fTable[rowIndex + 4 + i4], 5));
        }
        printStream.println(sb);
    }

    private void dumpCharCategories(PrintStream printStream) {
        int i;
        int i2 = this.fHeader.fCatCount + 1;
        String[] strArr = new String[i2];
        int[] iArr = new int[i2];
        for (int i3 = 0; i3 <= this.fHeader.fCatCount; i3++) {
            strArr[i3] = "";
        }
        printStream.println("\nCharacter Categories");
        printStream.println("--------------------");
        int i4 = 0;
        int i5 = 0;
        int i6 = -1;
        int i7 = 0;
        while (true) {
            if (i7 > 1114111) {
                break;
            }
            i = this.fTrie.get(i7) & -16385;
            if (i < 0 || i > this.fHeader.fCatCount) {
                break;
            }
            if (i != i6) {
                if (i6 >= 0) {
                    if (strArr[i6].length() > iArr[i6] + 70) {
                        iArr[i6] = strArr[i6].length() + 10;
                        strArr[i6] = strArr[i6] + "\n       ";
                    }
                    strArr[i6] = strArr[i6] + " " + Integer.toHexString(i4);
                    if (i5 != i4) {
                        strArr[i6] = strArr[i6] + LanguageTag.SEP + Integer.toHexString(i5);
                    }
                }
                i4 = i7;
                i6 = i;
            }
            i5 = i7;
            i7++;
        }
        printStream.println("Error, bad category " + Integer.toHexString(i) + " for char " + Integer.toHexString(i7));
        strArr[i6] = strArr[i6] + " " + Integer.toHexString(i4);
        if (i5 != i4) {
            strArr[i6] = strArr[i6] + LanguageTag.SEP + Integer.toHexString(i5);
        }
        for (int i8 = 0; i8 <= this.fHeader.fCatCount; i8++) {
            printStream.println(intToString(i8, 5) + "  " + strArr[i8]);
        }
        printStream.println();
    }
}
