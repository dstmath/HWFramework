package android.icu.impl.coll;

import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Trie2_32;
import android.icu.text.UnicodeSet;
import android.icu.util.ICUException;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import libcore.icu.ICU;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public final class CollationData {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int[] EMPTY_INT_ARRAY = null;
    static final int JAMO_CE32S_LENGTH = 67;
    static final int MAX_NUM_SPECIAL_REORDER_CODES = 8;
    static final int REORDER_RESERVED_AFTER_LATIN = 4111;
    static final int REORDER_RESERVED_BEFORE_LATIN = 4110;
    public CollationData base;
    int[] ce32s;
    long[] ces;
    public boolean[] compressibleBytes;
    String contexts;
    public char[] fastLatinTable;
    char[] fastLatinTableHeader;
    int[] jamoCE32s;
    public Normalizer2Impl nfcImpl;
    int numScripts;
    long numericPrimary;
    public long[] rootElements;
    char[] scriptStarts;
    char[] scriptsIndex;
    Trie2_32 trie;
    UnicodeSet unsafeBackwardSet;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.coll.CollationData.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.coll.CollationData.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.coll.CollationData.<clinit>():void");
    }

    CollationData(Normalizer2Impl nfc) {
        this.jamoCE32s = new int[JAMO_CE32S_LENGTH];
        this.numericPrimary = 301989888;
        this.nfcImpl = nfc;
    }

    public int getCE32(int c) {
        return this.trie.get(c);
    }

    int getCE32FromSupplementary(int c) {
        return this.trie.get(c);
    }

    boolean isDigit(int c) {
        if (c < 1632) {
            return (c > 57 || 48 > c) ? -assertionsDisabled : true;
        } else {
            return Collation.hasCE32Tag(getCE32(c), 10);
        }
    }

    public boolean isUnsafeBackward(int c, boolean numeric) {
        if (this.unsafeBackwardSet.contains(c)) {
            return true;
        }
        return numeric ? isDigit(c) : -assertionsDisabled;
    }

    public boolean isCompressibleLeadByte(int b) {
        return this.compressibleBytes[b];
    }

    public boolean isCompressiblePrimary(long p) {
        return isCompressibleLeadByte(((int) p) >>> 24);
    }

    int getCE32FromContexts(int index) {
        return (this.contexts.charAt(index) << 16) | this.contexts.charAt(index + 1);
    }

    int getIndirectCE32(int ce32) {
        if (-assertionsDisabled || Collation.isSpecialCE32(ce32)) {
            int tag = Collation.tagFromCE32(ce32);
            if (tag == 10) {
                return this.ce32s[Collation.indexFromCE32(ce32)];
            }
            if (tag == 13) {
                return -1;
            }
            if (tag == 11) {
                return this.ce32s[0];
            }
            return ce32;
        }
        throw new AssertionError();
    }

    int getFinalCE32(int ce32) {
        if (Collation.isSpecialCE32(ce32)) {
            return getIndirectCE32(ce32);
        }
        return ce32;
    }

    long getCEFromOffsetCE32(int c, int ce32) {
        return Collation.makeCE(Collation.getThreeBytePrimaryForOffsetData(c, this.ces[Collation.indexFromCE32(ce32)]));
    }

    long getSingleCE(int c) {
        CollationData d;
        int ce32 = getCE32(c);
        if (ce32 == Opcodes.OP_AND_LONG_2ADDR) {
            d = this.base;
            ce32 = this.base.getCE32(c);
        } else {
            d = this;
        }
        while (Collation.isSpecialCE32(ce32)) {
            switch (Collation.tagFromCE32(ce32)) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                case XmlPullParser.END_TAG /*3*/:
                    throw new AssertionError(String.format("unexpected CE32 tag for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case NodeFilter.SHOW_ELEMENT /*1*/:
                    return Collation.ceFromLongPrimaryCE32(ce32);
                case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                    return Collation.ceFromLongSecondaryCE32(ce32);
                case NodeFilter.SHOW_TEXT /*4*/:
                case XmlPullParser.IGNORABLE_WHITESPACE /*7*/:
                case MAX_NUM_SPECIAL_REORDER_CODES /*8*/:
                case XmlPullParser.COMMENT /*9*/:
                case ICU.U_ILLEGAL_CHAR_FOUND /*12*/:
                case Opcodes.OP_MOVE_EXCEPTION /*13*/:
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case XmlPullParser.CDSECT /*5*/:
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                        break;
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case XmlPullParser.ENTITY_REF /*6*/:
                    if (Collation.lengthFromCE32(ce32) == 1) {
                        return d.ces[Collation.indexFromCE32(ce32)];
                    }
                    throw new UnsupportedOperationException(String.format("there is not exactly one collation element for U+%04X (CE32 0x%08x)", new Object[]{Integer.valueOf(c), Integer.valueOf(ce32)}));
                case XmlPullParser.DOCDECL /*10*/:
                    ce32 = d.ce32s[Collation.indexFromCE32(ce32)];
                    break;
                case ICU.U_TRUNCATED_CHAR_FOUND /*11*/:
                    if (!-assertionsDisabled) {
                        if ((c == 0 ? 1 : 0) == 0) {
                            throw new AssertionError();
                        }
                    }
                    ce32 = d.ce32s[0];
                    break;
                case Opcodes.OP_RETURN_VOID /*14*/:
                    return d.getCEFromOffsetCE32(c, ce32);
                case ICU.U_BUFFER_OVERFLOW_ERROR /*15*/:
                    return Collation.unassignedCEFromCodePoint(c);
                default:
                    break;
            }
        }
        return Collation.ceFromSimpleCE32(ce32);
    }

    int getFCD16(int c) {
        return this.nfcImpl.getFCD16(c);
    }

    long getFirstPrimaryForGroup(int script) {
        int index = getScriptIndex(script);
        return index == 0 ? 0 : ((long) this.scriptStarts[index]) << 16;
    }

    public long getLastPrimaryForGroup(int script) {
        int index = getScriptIndex(script);
        if (index == 0) {
            return 0;
        }
        return (((long) this.scriptStarts[index + 1]) << 16) - 1;
    }

    public int getGroupForPrimary(long p) {
        p >>= 16;
        if (p < ((long) this.scriptStarts[1]) || ((long) this.scriptStarts[this.scriptStarts.length - 1]) <= p) {
            return -1;
        }
        int i;
        char index = '\u0001';
        while (p >= ((long) this.scriptStarts[index + 1])) {
            index++;
        }
        for (i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                return i;
            }
        }
        for (i = 0; i < MAX_NUM_SPECIAL_REORDER_CODES; i++) {
            if (this.scriptsIndex[this.numScripts + i] == index) {
                return i + VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS;
            }
        }
        return -1;
    }

    private int getScriptIndex(int script) {
        if (script < 0) {
            return 0;
        }
        if (script < this.numScripts) {
            return this.scriptsIndex[script];
        }
        if (script < VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS) {
            return 0;
        }
        script -= 4096;
        if (script < MAX_NUM_SPECIAL_REORDER_CODES) {
            return this.scriptsIndex[this.numScripts + script];
        }
        return 0;
    }

    public int[] getEquivalentScripts(int script) {
        char index = getScriptIndex(script);
        if (index == '\u0000') {
            return EMPTY_INT_ARRAY;
        }
        if (script >= VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS) {
            return new int[]{script};
        }
        int i;
        int length = 0;
        for (i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                length++;
            }
        }
        int[] dest = new int[length];
        if (length == 1) {
            dest[0] = script;
            return dest;
        }
        length = 0;
        for (i = 0; i < this.numScripts; i++) {
            if (this.scriptsIndex[i] == index) {
                int length2 = length + 1;
                dest[length] = i;
                length = length2;
            }
        }
        return dest;
    }

    void makeReorderRanges(int[] reorder, UVector32 ranges) {
        makeReorderRanges(reorder, -assertionsDisabled, ranges);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void makeReorderRanges(int[] reorder, boolean latinMustMove, UVector32 ranges) {
        ranges.removeAllElements();
        int length;
        if (length != 0 && (length != 1 || reorder[0] != 103)) {
            int start;
            int offset;
            int nextOffset;
            int newLeadByte;
            short[] table = new short[(this.scriptStarts.length - 1)];
            int index = this.scriptsIndex[(this.numScripts + REORDER_RESERVED_BEFORE_LATIN) - 4096];
            if (index != 0) {
                table[index] = (short) 255;
            }
            index = this.scriptsIndex[(this.numScripts + REORDER_RESERVED_AFTER_LATIN) - 4096];
            if (index != 0) {
                table[index] = (short) 255;
            }
            if (!-assertionsDisabled) {
                int length2 = this.scriptStarts.length;
                if ((r0 >= 2 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (!-assertionsDisabled) {
                if ((this.scriptStarts[0] == '\u0000' ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            int lowStart = this.scriptStarts[1];
            if (!-assertionsDisabled) {
                if ((lowStart == 768 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            int highLimit = this.scriptStarts[this.scriptStarts.length - 1];
            if (!-assertionsDisabled) {
                if ((highLimit == 65280 ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            int specials = 0;
            for (int i : reorder) {
                int reorderCode = i - 4096;
                if (reorderCode >= 0 && reorderCode < MAX_NUM_SPECIAL_REORDER_CODES) {
                    specials |= 1 << reorderCode;
                }
            }
            int i2 = 0;
            while (i2 < MAX_NUM_SPECIAL_REORDER_CODES) {
                index = this.scriptsIndex[this.numScripts + i2];
                if (index != 0 && ((1 << i2) & specials) == 0) {
                    lowStart = addLowScriptRange(table, index, lowStart);
                }
                i2++;
            }
            int skippedReserved = 0;
            if (specials == 0 && reorder[0] == 25 && !latinMustMove) {
                index = this.scriptsIndex[25];
                if (!-assertionsDisabled) {
                    if ((index != 0 ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                start = this.scriptStarts[index];
                if (!-assertionsDisabled) {
                    if ((lowStart <= start ? 1 : null) == null) {
                        throw new AssertionError();
                    }
                }
                skippedReserved = start - lowStart;
                lowStart = start;
            }
            boolean hasReorderToEnd = -assertionsDisabled;
            int i3 = 0;
            while (i3 < length) {
                i2 = i3 + 1;
                int script = reorder[i3];
                if (script == 103) {
                    hasReorderToEnd = true;
                    while (i2 < length) {
                        length--;
                        script = reorder[length];
                        if (script == 103) {
                            throw new IllegalArgumentException("setReorderCodes(): duplicate UScript.UNKNOWN");
                        } else if (script == -1) {
                            throw new IllegalArgumentException("setReorderCodes(): UScript.DEFAULT together with other scripts");
                        } else {
                            index = getScriptIndex(script);
                            if (index != 0) {
                                if (table[index] != (short) 0) {
                                    throw new IllegalArgumentException("setReorderCodes(): duplicate or equivalent script " + scriptCodeString(script));
                                }
                                highLimit = addHighScriptRange(table, index, highLimit);
                            }
                        }
                    }
                    i2 = 1;
                    while (true) {
                        if (i2 < this.scriptStarts.length - 1) {
                            break;
                        }
                        if (table[i2] != 0) {
                            start = this.scriptStarts[i2];
                            if (!hasReorderToEnd && start > lowStart) {
                                lowStart = start;
                            }
                            lowStart = addLowScriptRange(table, i2, lowStart);
                        }
                        i2++;
                    }
                    if (lowStart > highLimit) {
                        offset = 0;
                        i2 = 1;
                        while (true) {
                            nextOffset = offset;
                            while (true) {
                                if (i2 < this.scriptStarts.length - 1) {
                                    break;
                                }
                                newLeadByte = table[i2];
                                if (newLeadByte == 255) {
                                    nextOffset = newLeadByte - (this.scriptStarts[i2] >> MAX_NUM_SPECIAL_REORDER_CODES);
                                    if (nextOffset != offset) {
                                        break;
                                    }
                                }
                                i2++;
                            }
                            if (offset == 0) {
                            }
                            ranges.addElement((this.scriptStarts[i2] << 16) | (DexFormat.MAX_TYPE_IDX & offset));
                            if (i2 == this.scriptStarts.length - 1) {
                                offset = nextOffset;
                                i2++;
                            } else {
                                return;
                            }
                        }
                    } else if (lowStart - (Normalizer2Impl.JAMO_VT & skippedReserved) > highLimit) {
                        makeReorderRanges(reorder, true, ranges);
                        return;
                    } else {
                        throw new ICUException("setReorderCodes(): reordering too many partial-primary-lead-byte scripts");
                    }
                } else if (script == -1) {
                    throw new IllegalArgumentException("setReorderCodes(): UScript.DEFAULT together with other scripts");
                } else {
                    index = getScriptIndex(script);
                    if (index != 0) {
                        if (table[index] != (short) 0) {
                            throw new IllegalArgumentException("setReorderCodes(): duplicate or equivalent script " + scriptCodeString(script));
                        }
                        lowStart = addLowScriptRange(table, index, lowStart);
                    }
                    i3 = i2;
                }
            }
            i2 = 1;
            while (true) {
                if (i2 < this.scriptStarts.length - 1) {
                    break;
                    if (lowStart > highLimit) {
                        offset = 0;
                        i2 = 1;
                        while (true) {
                            nextOffset = offset;
                            while (true) {
                                if (i2 < this.scriptStarts.length - 1) {
                                    break;
                                }
                                newLeadByte = table[i2];
                                if (newLeadByte == 255) {
                                    nextOffset = newLeadByte - (this.scriptStarts[i2] >> MAX_NUM_SPECIAL_REORDER_CODES);
                                    if (nextOffset != offset) {
                                        break;
                                    }
                                }
                                i2++;
                                if (offset == 0) {
                                }
                                ranges.addElement((this.scriptStarts[i2] << 16) | (DexFormat.MAX_TYPE_IDX & offset));
                                if (i2 == this.scriptStarts.length - 1) {
                                    offset = nextOffset;
                                    i2++;
                                } else {
                                    return;
                                }
                            }
                        }
                    } else if (lowStart - (Normalizer2Impl.JAMO_VT & skippedReserved) > highLimit) {
                        throw new ICUException("setReorderCodes(): reordering too many partial-primary-lead-byte scripts");
                    } else {
                        makeReorderRanges(reorder, true, ranges);
                        return;
                    }
                }
                if (table[i2] != 0) {
                    start = this.scriptStarts[i2];
                    lowStart = start;
                    lowStart = addLowScriptRange(table, i2, lowStart);
                }
                i2++;
            }
        }
    }

    private int addLowScriptRange(short[] table, int index, int lowStart) {
        int start = this.scriptStarts[index];
        if ((start & Opcodes.OP_CONST_CLASS_JUMBO) < (lowStart & Opcodes.OP_CONST_CLASS_JUMBO)) {
            lowStart += NodeFilter.SHOW_DOCUMENT;
        }
        table[index] = (short) (lowStart >> MAX_NUM_SPECIAL_REORDER_CODES);
        int limit = this.scriptStarts[index + 1];
        return ((lowStart & Normalizer2Impl.JAMO_VT) + ((limit & Normalizer2Impl.JAMO_VT) - (Normalizer2Impl.JAMO_VT & start))) | (limit & Opcodes.OP_CONST_CLASS_JUMBO);
    }

    private int addHighScriptRange(short[] table, int index, int highLimit) {
        int limit = this.scriptStarts[index + 1];
        if ((limit & Opcodes.OP_CONST_CLASS_JUMBO) > (highLimit & Opcodes.OP_CONST_CLASS_JUMBO)) {
            highLimit -= 256;
        }
        int start = this.scriptStarts[index];
        highLimit = ((highLimit & Normalizer2Impl.JAMO_VT) - ((limit & Normalizer2Impl.JAMO_VT) - (Normalizer2Impl.JAMO_VT & start))) | (start & Opcodes.OP_CONST_CLASS_JUMBO);
        table[index] = (short) (highLimit >> MAX_NUM_SPECIAL_REORDER_CODES);
        return highLimit;
    }

    private static String scriptCodeString(int script) {
        return script < VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS ? Integer.toString(script) : "0x" + Integer.toHexString(script);
    }
}
