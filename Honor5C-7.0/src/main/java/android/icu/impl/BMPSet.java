package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.OutputInt;
import com.android.dex.DexFormat;
import dalvik.bytecode.Opcodes;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

public final class BMPSet {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    public static int U16_SURROGATE_OFFSET;
    private int[] bmpBlockBits;
    private boolean[] latin1Contains;
    private final int[] list;
    private int[] list4kStarts;
    private final int listLength;
    private int[] table7FF;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.BMPSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.BMPSet.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.BMPSet.<clinit>():void");
    }

    private static void set32x64Bits(int[] r1, int r2, int r3) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.BMPSet.set32x64Bits(int[], int, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.BMPSet.set32x64Bits(int[], int, int):void");
    }

    public BMPSet(int[] parentList, int parentListLength) {
        this.list = parentList;
        this.listLength = parentListLength;
        this.latin1Contains = new boolean[NodeFilter.SHOW_DOCUMENT];
        this.table7FF = new int[64];
        this.bmpBlockBits = new int[64];
        this.list4kStarts = new int[18];
        this.list4kStarts[0] = findCodePoint(NodeFilter.SHOW_NOTATION, 0, this.listLength - 1);
        for (int i = 1; i <= 16; i++) {
            this.list4kStarts[i] = findCodePoint(i << 12, this.list4kStarts[i - 1], this.listLength - 1);
        }
        this.list4kStarts[17] = this.listLength - 1;
        initBits();
    }

    public BMPSet(BMPSet otherBMPSet, int[] newParentList, int newParentListLength) {
        this.list = newParentList;
        this.listLength = newParentListLength;
        this.latin1Contains = (boolean[]) otherBMPSet.latin1Contains.clone();
        this.table7FF = (int[]) otherBMPSet.table7FF.clone();
        this.bmpBlockBits = (int[]) otherBMPSet.bmpBlockBits.clone();
        this.list4kStarts = (int[]) otherBMPSet.list4kStarts.clone();
    }

    public boolean contains(int c) {
        boolean z = true;
        if (c <= Opcodes.OP_CONST_CLASS_JUMBO) {
            return this.latin1Contains[c];
        }
        if (c <= Opcodes.OP_IGET_WIDE_JUMBO) {
            if ((this.table7FF[c & 63] & (1 << (c >> 6))) == 0) {
                z = false;
            }
            return z;
        } else if (c < UTF16.SURROGATE_MIN_VALUE || (c >= 57344 && c <= DexFormat.MAX_TYPE_IDX)) {
            int lead = c >> 12;
            int twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
            if (twoBits > 1) {
                return containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1]);
            }
            if (twoBits == 0) {
                z = false;
            }
            return z;
        } else if (c <= UnicodeSet.MAX_VALUE) {
            return containsSlow(c, this.list4kStarts[13], this.list4kStarts[17]);
        } else {
            return false;
        }
    }

    public final int span(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        int i = start;
        int limit = s.length();
        int numSupplementary = 0;
        char c;
        char c2;
        int lead;
        int twoBits;
        if (SpanCondition.NOT_CONTAINED != spanCondition) {
            while (i < limit) {
                c = s.charAt(i);
                if (c <= '\u00ff') {
                    if (!this.latin1Contains[c]) {
                        break;
                    }
                } else if (c <= '\u07ff') {
                    if ((this.table7FF[c & 63] & (1 << (c >> 6))) == 0) {
                        break;
                    }
                } else {
                    if (c >= UCharacter.MIN_SURROGATE && c < UCharacter.MIN_LOW_SURROGATE && i + 1 != limit) {
                        c2 = s.charAt(i + 1);
                        if (c2 >= UCharacter.MIN_LOW_SURROGATE && c2 < '\ue000') {
                            if (!containsSlow(Character.toCodePoint(c, c2), this.list4kStarts[16], this.list4kStarts[17])) {
                                break;
                            }
                            numSupplementary++;
                            i++;
                        }
                    }
                    lead = c >> 12;
                    twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                    if (twoBits <= 1) {
                        if (twoBits == 0) {
                            break;
                        }
                    } else if (!containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                        break;
                    }
                }
                i++;
            }
        } else {
            while (i < limit) {
                c = s.charAt(i);
                if (c <= '\u00ff') {
                    if (this.latin1Contains[c]) {
                        break;
                    }
                } else if (c > '\u07ff') {
                    if (c >= UCharacter.MIN_SURROGATE && c < UCharacter.MIN_LOW_SURROGATE && i + 1 != limit) {
                        c2 = s.charAt(i + 1);
                        if (c2 >= UCharacter.MIN_LOW_SURROGATE && c2 < '\ue000') {
                            if (containsSlow(Character.toCodePoint(c, c2), this.list4kStarts[16], this.list4kStarts[17])) {
                                break;
                            }
                            numSupplementary++;
                            i++;
                        }
                    }
                    lead = c >> 12;
                    twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                    if (twoBits <= 1) {
                        if (twoBits != 0) {
                            break;
                        }
                    } else if (containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                        break;
                    }
                } else if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
                    break;
                }
                i++;
            }
        }
        if (outCount != null) {
            outCount.value = (i - start) - numSupplementary;
        }
        return i;
    }

    public final int spanBack(CharSequence s, int limit, SpanCondition spanCondition) {
        char c;
        char c2;
        int lead;
        int twoBits;
        if (SpanCondition.NOT_CONTAINED != spanCondition) {
            do {
                limit--;
                c = s.charAt(limit);
                if (c <= '\u00ff') {
                    if (this.latin1Contains[c]) {
                    }
                } else if (c > '\u07ff') {
                    if (c >= UCharacter.MIN_SURROGATE && c >= UCharacter.MIN_LOW_SURROGATE && limit != 0) {
                        c2 = s.charAt(limit - 1);
                        if (c2 >= UCharacter.MIN_SURROGATE && c2 < UCharacter.MIN_LOW_SURROGATE) {
                            if (containsSlow(Character.toCodePoint(c2, c), this.list4kStarts[16], this.list4kStarts[17])) {
                                limit--;
                                continue;
                            }
                        }
                    }
                    lead = c >> 12;
                    twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                    if (twoBits <= 1) {
                        if (twoBits == 0) {
                        }
                    } else if (containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                    }
                } else if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
                }
            } while (limit != 0);
            return 0;
        }
        do {
            limit--;
            c = s.charAt(limit);
            if (c <= '\u00ff') {
                if (this.latin1Contains[c]) {
                }
            } else if (c > '\u07ff') {
                if (c >= UCharacter.MIN_SURROGATE && c >= UCharacter.MIN_LOW_SURROGATE && limit != 0) {
                    c2 = s.charAt(limit - 1);
                    if (c2 >= UCharacter.MIN_SURROGATE && c2 < UCharacter.MIN_LOW_SURROGATE) {
                        if (!containsSlow(Character.toCodePoint(c2, c), this.list4kStarts[16], this.list4kStarts[17])) {
                            limit--;
                            continue;
                        }
                    }
                }
                lead = c >> 12;
                twoBits = (this.bmpBlockBits[(c >> 6) & 63] >> lead) & 65537;
                if (twoBits <= 1) {
                    if (twoBits != 0) {
                    }
                } else if (containsSlow(c, this.list4kStarts[lead], this.list4kStarts[lead + 1])) {
                }
            } else if ((this.table7FF[c & 63] & (1 << (c >> 6))) != 0) {
            }
        } while (limit != 0);
        return 0;
        return limit + 1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initBits() {
        int limit;
        int listIndex;
        int start;
        int start2;
        int listIndex2 = 0;
        do {
            listIndex = listIndex2 + 1;
            start = this.list[listIndex2];
            if (listIndex < this.listLength) {
                listIndex2 = listIndex + 1;
                limit = this.list[listIndex];
            } else {
                limit = PropsVectors.INITIAL_VALUE_CP;
                listIndex2 = listIndex;
            }
            if (start >= NodeFilter.SHOW_DOCUMENT) {
                listIndex = listIndex2;
                break;
            }
            while (true) {
                start2 = start + 1;
                this.latin1Contains[start] = true;
                if (start2 < limit && start2 < NodeFilter.SHOW_DOCUMENT) {
                    start = start2;
                }
            }
        } while (limit <= NodeFilter.SHOW_DOCUMENT);
        listIndex = listIndex2;
        start = start2;
        while (start < NodeFilter.SHOW_NOTATION) {
            int i;
            int[] iArr = this.table7FF;
            if (limit <= NodeFilter.SHOW_NOTATION) {
                i = limit;
            } else {
                i = NodeFilter.SHOW_NOTATION;
            }
            set32x64Bits(iArr, start, i);
            if (limit > NodeFilter.SHOW_NOTATION) {
                start = NodeFilter.SHOW_NOTATION;
                break;
            }
            listIndex2 = listIndex + 1;
            start = this.list[listIndex];
            if (listIndex2 < this.listLength) {
                listIndex = listIndex2 + 1;
                limit = this.list[listIndex2];
                listIndex2 = listIndex;
            } else {
                limit = PropsVectors.INITIAL_VALUE_CP;
            }
            listIndex = listIndex2;
        }
        int minStart = NodeFilter.SHOW_NOTATION;
        while (start < DateUtilsBridge.FORMAT_ABBREV_MONTH) {
            if (limit > DateUtilsBridge.FORMAT_ABBREV_MONTH) {
                limit = DateUtilsBridge.FORMAT_ABBREV_MONTH;
            }
            if (start < minStart) {
                start = minStart;
            }
            if (start < limit) {
                int[] iArr2;
                int i2;
                if ((start & 63) != 0) {
                    start >>= 6;
                    iArr2 = this.bmpBlockBits;
                    i2 = start & 63;
                    iArr2[i2] = iArr2[i2] | (65537 << (start >> 6));
                    start = (start + 1) << 6;
                    minStart = start;
                }
                if (start < limit) {
                    if (start < (limit & -64)) {
                        set32x64Bits(this.bmpBlockBits, start >> 6, limit >> 6);
                    }
                    if ((limit & 63) != 0) {
                        limit >>= 6;
                        iArr2 = this.bmpBlockBits;
                        i2 = limit & 63;
                        iArr2[i2] = iArr2[i2] | (65537 << (limit >> 6));
                        limit = (limit + 1) << 6;
                        minStart = limit;
                    }
                }
            }
            if (limit != DateUtilsBridge.FORMAT_ABBREV_MONTH) {
                listIndex2 = listIndex + 1;
                start = this.list[listIndex];
                if (listIndex2 < this.listLength) {
                    listIndex = listIndex2 + 1;
                    limit = this.list[listIndex2];
                    listIndex2 = listIndex;
                } else {
                    limit = PropsVectors.INITIAL_VALUE_CP;
                }
                listIndex = listIndex2;
            } else {
                return;
            }
        }
    }

    private int findCodePoint(int c, int lo, int hi) {
        if (c < this.list[lo]) {
            return lo;
        }
        if (lo >= hi || c >= this.list[hi - 1]) {
            return hi;
        }
        while (true) {
            int i = (lo + hi) >>> 1;
            if (i == lo) {
                return hi;
            }
            if (c < this.list[i]) {
                hi = i;
            } else {
                lo = i;
            }
        }
    }

    private final boolean containsSlow(int c, int lo, int hi) {
        return (findCodePoint(c, lo, hi) & 1) != 0;
    }
}
