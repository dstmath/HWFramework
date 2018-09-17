package android.icu.impl;

import android.icu.lang.UCharacter;
import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSet.SpanCondition;
import android.icu.util.OutputInt;
import java.util.ArrayList;

public class UnicodeSetStringSpan {
    public static final int ALL = 127;
    static final short ALL_CP_CONTAINED = (short) 255;
    public static final int BACK = 16;
    public static final int BACK_UTF16_CONTAINED = 18;
    public static final int BACK_UTF16_NOT_CONTAINED = 17;
    public static final int CONTAINED = 2;
    public static final int FWD = 32;
    public static final int FWD_UTF16_CONTAINED = 34;
    public static final int FWD_UTF16_NOT_CONTAINED = 33;
    static final short LONG_SPAN = (short) 254;
    public static final int NOT_CONTAINED = 1;
    public static final int WITH_COUNT = 64;
    private boolean all;
    private int maxLength16;
    private OffsetList offsets;
    private boolean someRelevant;
    private short[] spanLengths;
    private UnicodeSet spanNotSet;
    private UnicodeSet spanSet;
    private ArrayList<String> strings;

    private static final class OffsetList {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private int length;
        private int[] list;
        private int start;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.UnicodeSetStringSpan.OffsetList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.UnicodeSetStringSpan.OffsetList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UnicodeSetStringSpan.OffsetList.<clinit>():void");
        }

        public OffsetList() {
            this.list = new int[UnicodeSetStringSpan.BACK];
        }

        public void setMaxLength(int maxLength) {
            if (maxLength > this.list.length) {
                this.list = new int[maxLength];
            }
            clear();
        }

        public void clear() {
            int i = this.list.length;
            while (true) {
                int i2 = i - 1;
                if (i > 0) {
                    this.list[i2] = 0;
                    i = i2;
                } else {
                    this.length = 0;
                    this.start = 0;
                    return;
                }
            }
        }

        public boolean isEmpty() {
            return this.length == 0;
        }

        public void shift(int delta) {
            int i = this.start + delta;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            if (this.list[i] != 0) {
                this.list[i] = 0;
                this.length--;
            }
            this.start = i;
        }

        public void addOffset(int offset) {
            Object obj = null;
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            if (!-assertionsDisabled) {
                if (this.list[i] == 0) {
                    obj = UnicodeSetStringSpan.NOT_CONTAINED;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            this.list[i] = UnicodeSetStringSpan.NOT_CONTAINED;
            this.length += UnicodeSetStringSpan.NOT_CONTAINED;
        }

        public void addOffsetAndCount(int offset, int count) {
            Object obj = null;
            if (!-assertionsDisabled) {
                if (count > 0) {
                    obj = UnicodeSetStringSpan.NOT_CONTAINED;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            if (this.list[i] == 0) {
                this.list[i] = count;
                this.length += UnicodeSetStringSpan.NOT_CONTAINED;
            } else if (count < this.list[i]) {
                this.list[i] = count;
            }
        }

        public boolean containsOffset(int offset) {
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            if (this.list[i] != 0) {
                return true;
            }
            return false;
        }

        public boolean hasCountAtOffset(int offset, int count) {
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            int oldCount = this.list[i];
            if (oldCount == 0 || oldCount > count) {
                return false;
            }
            return true;
        }

        public int popMinimum(OutputInt outCount) {
            int result;
            int i = this.start;
            int count;
            do {
                i += UnicodeSetStringSpan.NOT_CONTAINED;
                if (i < this.list.length) {
                    count = this.list[i];
                } else {
                    result = this.list.length - this.start;
                    i = 0;
                    while (true) {
                        count = this.list[i];
                        if (count != 0) {
                            break;
                        }
                        i += UnicodeSetStringSpan.NOT_CONTAINED;
                    }
                    this.list[i] = 0;
                    this.length--;
                    this.start = i;
                    if (outCount != null) {
                        outCount.value = count;
                    }
                    return result + i;
                }
            } while (count == 0);
            this.list[i] = 0;
            this.length--;
            result = i - this.start;
            this.start = i;
            if (outCount != null) {
                outCount.value = count;
            }
            return result;
        }
    }

    public UnicodeSetStringSpan(UnicodeSet set, ArrayList<String> setStrings, int which) {
        int i;
        this.spanSet = new UnicodeSet(0, (int) UnicodeSet.MAX_VALUE);
        this.strings = setStrings;
        this.all = which == ALL;
        this.spanSet.retainAll(set);
        if ((which & NOT_CONTAINED) != 0) {
            this.spanNotSet = this.spanSet;
        }
        this.offsets = new OffsetList();
        int stringsLength = this.strings.size();
        this.someRelevant = false;
        for (i = 0; i < stringsLength; i += NOT_CONTAINED) {
            String string = (String) this.strings.get(i);
            int length16 = string.length();
            if (this.spanSet.span(string, SpanCondition.CONTAINED) < length16) {
                this.someRelevant = true;
            }
            if (length16 > this.maxLength16) {
                this.maxLength16 = length16;
            }
        }
        if (this.someRelevant || (which & WITH_COUNT) != 0) {
            int allocSize;
            int spanBackLengthsOffset;
            if (this.all) {
                this.spanSet.freeze();
            }
            if (this.all) {
                allocSize = stringsLength * CONTAINED;
            } else {
                allocSize = stringsLength;
            }
            this.spanLengths = new short[allocSize];
            if (this.all) {
                spanBackLengthsOffset = stringsLength;
            } else {
                spanBackLengthsOffset = 0;
            }
            for (i = 0; i < stringsLength; i += NOT_CONTAINED) {
                string = (String) this.strings.get(i);
                length16 = string.length();
                int spanLength = this.spanSet.span(string, SpanCondition.CONTAINED);
                short[] sArr;
                if (spanLength < length16) {
                    if ((which & CONTAINED) != 0) {
                        if ((which & FWD) != 0) {
                            this.spanLengths[i] = makeSpanLengthByte(spanLength);
                        }
                        if ((which & BACK) != 0) {
                            this.spanLengths[spanBackLengthsOffset + i] = makeSpanLengthByte(length16 - this.spanSet.spanBack(string, length16, SpanCondition.CONTAINED));
                        }
                    } else {
                        sArr = this.spanLengths;
                        this.spanLengths[spanBackLengthsOffset + i] = (short) 0;
                        sArr[i] = (short) 0;
                    }
                    if ((which & NOT_CONTAINED) != 0) {
                        if ((which & FWD) != 0) {
                            addToSpanNotSet(string.codePointAt(0));
                        }
                        if ((which & BACK) != 0) {
                            addToSpanNotSet(string.codePointBefore(length16));
                        }
                    }
                } else if (this.all) {
                    sArr = this.spanLengths;
                    this.spanLengths[spanBackLengthsOffset + i] = ALL_CP_CONTAINED;
                    sArr[i] = ALL_CP_CONTAINED;
                } else {
                    this.spanLengths[i] = ALL_CP_CONTAINED;
                }
            }
            if (this.all) {
                this.spanNotSet.freeze();
            }
        }
    }

    public UnicodeSetStringSpan(UnicodeSetStringSpan otherStringSpan, ArrayList<String> newParentSetStrings) {
        this.spanSet = otherStringSpan.spanSet;
        this.strings = newParentSetStrings;
        this.maxLength16 = otherStringSpan.maxLength16;
        this.someRelevant = otherStringSpan.someRelevant;
        this.all = true;
        if (otherStringSpan.spanNotSet == otherStringSpan.spanSet) {
            this.spanNotSet = this.spanSet;
        } else {
            this.spanNotSet = (UnicodeSet) otherStringSpan.spanNotSet.clone();
        }
        this.offsets = new OffsetList();
        this.spanLengths = (short[]) otherStringSpan.spanLengths.clone();
    }

    public boolean needsStringSpanUTF16() {
        return this.someRelevant;
    }

    public boolean contains(int c) {
        return this.spanSet.contains(c);
    }

    private void addToSpanNotSet(int c) {
        if (this.spanNotSet == null || this.spanNotSet == this.spanSet) {
            if (!this.spanSet.contains(c)) {
                this.spanNotSet = this.spanSet.cloneAsThawed();
            } else {
                return;
            }
        }
        this.spanNotSet.add(c);
    }

    public int span(CharSequence s, int start, SpanCondition spanCondition) {
        if (spanCondition == SpanCondition.NOT_CONTAINED) {
            return spanNot(s, start, null);
        }
        int spanLimit = this.spanSet.span(s, start, SpanCondition.CONTAINED);
        if (spanLimit == s.length()) {
            return spanLimit;
        }
        return spanWithStrings(s, start, spanLimit, spanCondition);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized int spanWithStrings(CharSequence s, int start, int spanLimit, SpanCondition spanCondition) {
        int initSize = 0;
        if (spanCondition == SpanCondition.CONTAINED) {
            initSize = this.maxLength16;
        }
        this.offsets.setMaxLength(initSize);
        int length = s.length();
        int pos = spanLimit;
        int rest = length - spanLimit;
        int spanLength = spanLimit - start;
        int stringsLength = this.strings.size();
        loop0:
        while (true) {
            int i;
            int overlap;
            String string;
            int length16;
            int inc;
            if (spanCondition == SpanCondition.CONTAINED) {
                for (i = 0; i < stringsLength; i += NOT_CONTAINED) {
                    overlap = this.spanLengths[i];
                    if (overlap != 255) {
                        string = (String) this.strings.get(i);
                        length16 = string.length();
                        if (overlap >= 254) {
                            overlap = length16;
                            overlap = string.offsetByCodePoints(length16, -1);
                        }
                        if (overlap > spanLength) {
                            overlap = spanLength;
                        }
                        for (inc = length16 - overlap; inc <= rest; inc += NOT_CONTAINED) {
                            if (!this.offsets.containsOffset(inc)) {
                                if (matches16CPB(s, pos - overlap, length, string, length16)) {
                                    if (inc == rest) {
                                        return length;
                                    }
                                    this.offsets.addOffset(inc);
                                }
                            }
                            if (overlap == 0) {
                                break;
                            }
                            overlap--;
                        }
                        continue;
                    }
                }
            } else {
                int maxInc = 0;
                int maxOverlap = 0;
                for (i = 0; i < stringsLength; i += NOT_CONTAINED) {
                    overlap = this.spanLengths[i];
                    string = (String) this.strings.get(i);
                    length16 = string.length();
                    if (overlap >= 254) {
                        overlap = length16;
                    }
                    if (overlap > spanLength) {
                        overlap = spanLength;
                    }
                    inc = length16 - overlap;
                    while (inc <= rest && overlap >= maxOverlap) {
                        if (overlap > maxOverlap || inc > maxInc) {
                            if (matches16CPB(s, pos - overlap, length, string, length16)) {
                                maxInc = inc;
                                maxOverlap = overlap;
                                break;
                            }
                        }
                        overlap--;
                        inc += NOT_CONTAINED;
                    }
                }
                if (!(maxInc == 0 && maxOverlap == 0)) {
                    pos += maxInc;
                    rest -= maxInc;
                    if (rest == 0) {
                        return length;
                    }
                    spanLength = 0;
                }
            }
            if (spanLength != 0 || pos == 0) {
                if (this.offsets.isEmpty()) {
                    return pos;
                }
            }
            if (this.offsets.isEmpty()) {
                spanLimit = this.spanSet.span(s, pos, SpanCondition.CONTAINED);
                spanLength = spanLimit - pos;
                if (spanLength != rest && spanLength != 0) {
                    pos += spanLength;
                    rest -= spanLength;
                }
            } else {
                spanLength = spanOne(this.spanSet, s, pos, rest);
                if (spanLength > 0) {
                    if (spanLength == rest) {
                        return length;
                    }
                    pos += spanLength;
                    rest -= spanLength;
                    this.offsets.shift(spanLength);
                    spanLength = 0;
                }
            }
            int minOffset = this.offsets.popMinimum(null);
            pos += minOffset;
            rest -= minOffset;
            spanLength = 0;
        }
    }

    public int spanAndCount(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        if (spanCondition == SpanCondition.NOT_CONTAINED) {
            return spanNot(s, start, outCount);
        }
        if (spanCondition == SpanCondition.CONTAINED) {
            return spanContainedAndCount(s, start, outCount);
        }
        int stringsLength = this.strings.size();
        int length = s.length();
        int pos = start;
        int rest = length - start;
        int count = 0;
        while (rest != 0) {
            int cpLength = spanOne(this.spanSet, s, pos, rest);
            int maxInc = cpLength > 0 ? cpLength : 0;
            for (int i = 0; i < stringsLength; i += NOT_CONTAINED) {
                String string = (String) this.strings.get(i);
                int length16 = string.length();
                if (maxInc < length16 && length16 <= rest && matches16CPB(s, pos, length, string, length16)) {
                    maxInc = length16;
                }
            }
            if (maxInc == 0) {
                outCount.value = count;
                return pos;
            }
            count += NOT_CONTAINED;
            pos += maxInc;
            rest -= maxInc;
        }
        outCount.value = count;
        return pos;
    }

    private synchronized int spanContainedAndCount(CharSequence s, int start, OutputInt outCount) {
        this.offsets.setMaxLength(this.maxLength16);
        int stringsLength = this.strings.size();
        int length = s.length();
        int pos = start;
        int rest = length - start;
        int count = 0;
        while (rest != 0) {
            int cpLength = spanOne(this.spanSet, s, pos, rest);
            if (cpLength > 0) {
                this.offsets.addOffsetAndCount(cpLength, count + NOT_CONTAINED);
            }
            for (int i = 0; i < stringsLength; i += NOT_CONTAINED) {
                String string = (String) this.strings.get(i);
                int length16 = string.length();
                if (length16 <= rest && !this.offsets.hasCountAtOffset(length16, count + NOT_CONTAINED) && matches16CPB(s, pos, length, string, length16)) {
                    this.offsets.addOffsetAndCount(length16, count + NOT_CONTAINED);
                }
            }
            if (this.offsets.isEmpty()) {
                outCount.value = count;
                return pos;
            }
            int minOffset = this.offsets.popMinimum(outCount);
            count = outCount.value;
            pos += minOffset;
            rest -= minOffset;
        }
        outCount.value = count;
        return pos;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int spanBack(CharSequence s, int length, SpanCondition spanCondition) {
        if (spanCondition == SpanCondition.NOT_CONTAINED) {
            return spanNotBack(s, length);
        }
        int pos = this.spanSet.spanBack(s, length, SpanCondition.CONTAINED);
        if (pos == 0) {
            return 0;
        }
        int spanLength = length - pos;
        int initSize = 0;
        if (spanCondition == SpanCondition.CONTAINED) {
            initSize = this.maxLength16;
        }
        this.offsets.setMaxLength(initSize);
        int stringsLength = this.strings.size();
        int spanBackLengthsOffset = 0;
        if (this.all) {
            spanBackLengthsOffset = stringsLength;
        }
        loop0:
        while (true) {
            int i;
            int overlap;
            String string;
            int length16;
            int dec;
            if (spanCondition == SpanCondition.CONTAINED) {
                for (i = 0; i < stringsLength; i += NOT_CONTAINED) {
                    overlap = this.spanLengths[spanBackLengthsOffset + i];
                    if (overlap != 255) {
                        string = (String) this.strings.get(i);
                        length16 = string.length();
                        if (overlap >= 254) {
                            overlap = length16;
                            overlap = length16 - string.offsetByCodePoints(0, NOT_CONTAINED);
                        }
                        if (overlap > spanLength) {
                            overlap = spanLength;
                        }
                        for (dec = length16 - overlap; dec <= pos; dec += NOT_CONTAINED) {
                            if (!this.offsets.containsOffset(dec)) {
                                if (matches16CPB(s, pos - dec, length, string, length16)) {
                                    if (dec == pos) {
                                        return 0;
                                    }
                                    this.offsets.addOffset(dec);
                                }
                            }
                            if (overlap == 0) {
                                break;
                            }
                            overlap--;
                        }
                        continue;
                    }
                }
            } else {
                int maxDec = 0;
                int maxOverlap = 0;
                for (i = 0; i < stringsLength; i += NOT_CONTAINED) {
                    overlap = this.spanLengths[spanBackLengthsOffset + i];
                    string = (String) this.strings.get(i);
                    length16 = string.length();
                    if (overlap >= 254) {
                        overlap = length16;
                    }
                    if (overlap > spanLength) {
                        overlap = spanLength;
                    }
                    dec = length16 - overlap;
                    while (dec <= pos && overlap >= maxOverlap) {
                        if (overlap > maxOverlap || dec > maxDec) {
                            if (matches16CPB(s, pos - dec, length, string, length16)) {
                                maxDec = dec;
                                maxOverlap = overlap;
                                break;
                            }
                        }
                        overlap--;
                        dec += NOT_CONTAINED;
                    }
                }
                if (!(maxDec == 0 && maxOverlap == 0)) {
                    pos -= maxDec;
                    if (pos == 0) {
                        return 0;
                    }
                    spanLength = 0;
                }
            }
            if (spanLength != 0 || pos == length) {
                if (this.offsets.isEmpty()) {
                    return pos;
                }
            }
            if (this.offsets.isEmpty()) {
                int oldPos = pos;
                pos = this.spanSet.spanBack(s, oldPos, SpanCondition.CONTAINED);
                spanLength = oldPos - pos;
                if (pos == 0 || spanLength == 0) {
                }
            } else {
                spanLength = spanOneBack(this.spanSet, s, pos);
                if (spanLength > 0) {
                    if (spanLength == pos) {
                        return 0;
                    }
                    pos -= spanLength;
                    this.offsets.shift(spanLength);
                    spanLength = 0;
                }
            }
            pos -= this.offsets.popMinimum(null);
            spanLength = 0;
        }
    }

    private int spanNot(CharSequence s, int start, OutputInt outCount) {
        int length = s.length();
        int pos = start;
        int rest = length - start;
        int stringsLength = this.strings.size();
        int count = 0;
        int cpLength;
        do {
            int spanLimit;
            if (outCount == null) {
                spanLimit = this.spanNotSet.span(s, pos, SpanCondition.NOT_CONTAINED);
            } else {
                spanLimit = this.spanNotSet.spanAndCount(s, pos, SpanCondition.NOT_CONTAINED, outCount);
                count += outCount.value;
                outCount.value = count;
            }
            if (spanLimit == length) {
                return length;
            }
            pos = spanLimit;
            rest = length - spanLimit;
            cpLength = spanOne(this.spanSet, s, pos, rest);
            if (cpLength > 0) {
                return pos;
            }
            for (int i = 0; i < stringsLength; i += NOT_CONTAINED) {
                if (this.spanLengths[i] != ALL_CP_CONTAINED) {
                    String string = (String) this.strings.get(i);
                    int length16 = string.length();
                    if (length16 <= rest && matches16CPB(s, pos, length, string, length16)) {
                        return pos;
                    }
                }
            }
            pos -= cpLength;
            count += NOT_CONTAINED;
        } while (rest + cpLength != 0);
        if (outCount != null) {
            outCount.value = count;
        }
        return length;
    }

    private int spanNotBack(CharSequence s, int length) {
        int pos = length;
        int stringsLength = this.strings.size();
        do {
            pos = this.spanNotSet.spanBack(s, pos, SpanCondition.NOT_CONTAINED);
            if (pos == 0) {
                return 0;
            }
            int cpLength = spanOneBack(this.spanSet, s, pos);
            if (cpLength > 0) {
                return pos;
            }
            for (int i = 0; i < stringsLength; i += NOT_CONTAINED) {
                if (this.spanLengths[i] != ALL_CP_CONTAINED) {
                    String string = (String) this.strings.get(i);
                    int length16 = string.length();
                    if (length16 <= pos && matches16CPB(s, pos - length16, length, string, length16)) {
                        return pos;
                    }
                }
            }
            pos += cpLength;
        } while (pos != 0);
        return 0;
    }

    static short makeSpanLengthByte(int spanLength) {
        return spanLength < SCSU.KATAKANAINDEX ? (short) spanLength : LONG_SPAN;
    }

    private static boolean matches16(CharSequence s, int start, String t, int length) {
        int end = start + length;
        int length2 = length;
        while (true) {
            length = length2 - 1;
            if (length2 <= 0) {
                return true;
            }
            end--;
            if (s.charAt(end) != t.charAt(length)) {
                return false;
            }
            length2 = length;
        }
    }

    static boolean matches16CPB(CharSequence s, int start, int limit, String t, int tlength) {
        if (!matches16(s, start, t, tlength)) {
            return false;
        }
        if (start > 0 && Character.isHighSurrogate(s.charAt(start - 1)) && Character.isLowSurrogate(s.charAt(start))) {
            return false;
        }
        if (start + tlength < limit && Character.isHighSurrogate(s.charAt((start + tlength) - 1)) && Character.isLowSurrogate(s.charAt(start + tlength))) {
            return false;
        }
        return true;
    }

    static int spanOne(UnicodeSet set, CharSequence s, int start, int length) {
        int i = CONTAINED;
        int c = s.charAt(start);
        if (c >= UCharacter.MIN_SURROGATE && c <= UCharacter.MAX_HIGH_SURROGATE && length >= CONTAINED) {
            char c2 = s.charAt(start + NOT_CONTAINED);
            if (UTF16.isTrailSurrogate(c2)) {
                if (!set.contains(Character.toCodePoint(c, c2))) {
                    i = -2;
                }
                return i;
            }
        }
        return set.contains(c) ? NOT_CONTAINED : -1;
    }

    static int spanOneBack(UnicodeSet set, CharSequence s, int length) {
        int i = CONTAINED;
        int c = s.charAt(length - 1);
        if (c >= UCharacter.MIN_LOW_SURROGATE && c <= UCharacter.MAX_SURROGATE && length >= CONTAINED) {
            char c2 = s.charAt(length - 2);
            if (UTF16.isLeadSurrogate(c2)) {
                if (!set.contains(Character.toCodePoint(c2, c))) {
                    i = -2;
                }
                return i;
            }
        }
        return set.contains(c) ? NOT_CONTAINED : -1;
    }
}
