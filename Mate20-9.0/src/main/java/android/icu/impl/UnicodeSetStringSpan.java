package android.icu.impl;

import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;
import android.icu.util.OutputInt;
import java.util.ArrayList;

public class UnicodeSetStringSpan {
    public static final int ALL = 127;
    static final short ALL_CP_CONTAINED = 255;
    public static final int BACK = 16;
    public static final int BACK_UTF16_CONTAINED = 18;
    public static final int BACK_UTF16_NOT_CONTAINED = 17;
    public static final int CONTAINED = 2;
    public static final int FWD = 32;
    public static final int FWD_UTF16_CONTAINED = 34;
    public static final int FWD_UTF16_NOT_CONTAINED = 33;
    static final short LONG_SPAN = 254;
    public static final int NOT_CONTAINED = 1;
    public static final int WITH_COUNT = 64;
    private boolean all;
    private final int maxLength16;
    private OffsetList offsets;
    private boolean someRelevant;
    private short[] spanLengths;
    private UnicodeSet spanNotSet;
    private UnicodeSet spanSet;
    private ArrayList<String> strings;

    private static final class OffsetList {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private int length;
        private int[] list = new int[16];
        private int start;

        static {
            Class<UnicodeSetStringSpan> cls = UnicodeSetStringSpan.class;
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
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            this.list[i] = 1;
            this.length++;
        }

        public void addOffsetAndCount(int offset, int count) {
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            if (this.list[i] == 0) {
                this.list[i] = count;
                this.length++;
            } else if (count < this.list[i]) {
                this.list[i] = count;
            }
        }

        public boolean containsOffset(int offset) {
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            return this.list[i] != 0;
        }

        public boolean hasCountAtOffset(int offset, int count) {
            int i = this.start + offset;
            if (i >= this.list.length) {
                i -= this.list.length;
            }
            int oldCount = this.list[i];
            return oldCount != 0 && oldCount <= count;
        }

        public int popMinimum(OutputInt outCount) {
            int count;
            int count2;
            int i = this.start;
            do {
                i++;
                if (i < this.list.length) {
                    count2 = this.list[i];
                } else {
                    int result = this.list.length - this.start;
                    int i2 = 0;
                    while (true) {
                        int i3 = this.list[i2];
                        count = i3;
                        if (i3 != 0) {
                            break;
                        }
                        i2++;
                    }
                    this.list[i2] = 0;
                    this.length--;
                    this.start = i2;
                    if (outCount != null) {
                        outCount.value = count;
                    }
                    return result + i2;
                }
            } while (count2 == 0);
            this.list[i] = 0;
            this.length--;
            int result2 = i - this.start;
            this.start = i;
            if (outCount != null) {
                outCount.value = count2;
            }
            return result2;
        }
    }

    public UnicodeSetStringSpan(UnicodeSet set, ArrayList<String> setStrings, int which) {
        int allocSize;
        int spanBackLengthsOffset;
        int i = which;
        this.spanSet = new UnicodeSet(0, 1114111);
        this.strings = setStrings;
        this.all = i == 127;
        this.spanSet.retainAll(set);
        if ((i & 1) != 0) {
            this.spanNotSet = this.spanSet;
        }
        this.offsets = new OffsetList();
        int stringsLength = this.strings.size();
        this.someRelevant = false;
        int maxLength162 = 0;
        for (int i2 = 0; i2 < stringsLength; i2++) {
            String string = this.strings.get(i2);
            int length16 = string.length();
            if (this.spanSet.span(string, UnicodeSet.SpanCondition.CONTAINED) < length16) {
                this.someRelevant = true;
            }
            if (length16 > maxLength162) {
                maxLength162 = length16;
            }
        }
        this.maxLength16 = maxLength162;
        if (this.someRelevant || (i & 64) != 0) {
            if (this.all) {
                this.spanSet.freeze();
            }
            if (this.all) {
                allocSize = stringsLength * 2;
            } else {
                allocSize = stringsLength;
            }
            this.spanLengths = new short[allocSize];
            if (this.all) {
                spanBackLengthsOffset = stringsLength;
            } else {
                spanBackLengthsOffset = 0;
            }
            for (int i3 = 0; i3 < stringsLength; i3++) {
                String string2 = this.strings.get(i3);
                int length162 = string2.length();
                int spanLength = this.spanSet.span(string2, UnicodeSet.SpanCondition.CONTAINED);
                if (spanLength < length162) {
                    if ((i & 2) != 0) {
                        if ((i & 32) != 0) {
                            this.spanLengths[i3] = makeSpanLengthByte(spanLength);
                        }
                        if ((i & 16) != 0) {
                            this.spanLengths[spanBackLengthsOffset + i3] = makeSpanLengthByte(length162 - this.spanSet.spanBack(string2, length162, UnicodeSet.SpanCondition.CONTAINED));
                        }
                    } else {
                        short[] sArr = this.spanLengths;
                        this.spanLengths[spanBackLengthsOffset + i3] = 0;
                        sArr[i3] = 0;
                    }
                    if ((i & 1) != 0) {
                        if ((i & 32) != 0) {
                            addToSpanNotSet(string2.codePointAt(0));
                        }
                        if ((i & 16) != 0) {
                            addToSpanNotSet(string2.codePointBefore(length162));
                        }
                    }
                } else if (this.all) {
                    short[] sArr2 = this.spanLengths;
                    this.spanLengths[spanBackLengthsOffset + i3] = ALL_CP_CONTAINED;
                    sArr2[i3] = ALL_CP_CONTAINED;
                } else {
                    this.spanLengths[i3] = ALL_CP_CONTAINED;
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
        if (Utility.sameObjects(otherStringSpan.spanNotSet, otherStringSpan.spanSet)) {
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
        if (Utility.sameObjects(this.spanNotSet, null) || Utility.sameObjects(this.spanNotSet, this.spanSet)) {
            if (!this.spanSet.contains(c)) {
                this.spanNotSet = this.spanSet.cloneAsThawed();
            } else {
                return;
            }
        }
        this.spanNotSet.add(c);
    }

    public int span(CharSequence s, int start, UnicodeSet.SpanCondition spanCondition) {
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            return spanNot(s, start, null);
        }
        int spanLimit = this.spanSet.span(s, start, UnicodeSet.SpanCondition.CONTAINED);
        if (spanLimit == s.length()) {
            return spanLimit;
        }
        return spanWithStrings(s, start, spanLimit, spanCondition);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v22, resolved type: short} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v25, resolved type: short} */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x010b, code lost:
        return r2;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    private synchronized int spanWithStrings(CharSequence s, int start, int spanLimit, UnicodeSet.SpanCondition spanCondition) {
        int initSize;
        int spanLimit2;
        int i;
        int initSize2;
        int spanLimit3;
        CharSequence charSequence = s;
        UnicodeSet.SpanCondition spanCondition2 = spanCondition;
        synchronized (this) {
            int initSize3 = 0;
            if (spanCondition2 == UnicodeSet.SpanCondition.CONTAINED) {
                initSize3 = this.maxLength16;
            }
            this.offsets.setMaxLength(initSize3);
            int length = s.length();
            int pos = spanLimit;
            int rest = length - spanLimit;
            int spanLength = spanLimit - start;
            int stringsLength = this.strings.size();
            int spanLimit4 = spanLimit;
            while (true) {
                char c = 254;
                int overlap = 0;
                if (spanCondition2 == UnicodeSet.SpanCondition.CONTAINED) {
                    while (true) {
                        int i2 = overlap;
                        if (i2 >= stringsLength) {
                            spanLimit2 = spanLimit4;
                            initSize = initSize3;
                            break;
                        }
                        short overlap2 = this.spanLengths[i2];
                        if (overlap2 != 255) {
                            String string = this.strings.get(i2);
                            int length16 = string.length();
                            int overlap3 = overlap2;
                            if (overlap2 >= c) {
                                overlap3 = string.offsetByCodePoints(length16, -1);
                            }
                            if (overlap3 > spanLength) {
                                overlap3 = spanLength;
                            }
                            int inc = length16 - overlap3;
                            int overlap4 = overlap3;
                            while (true) {
                                if (inc > rest) {
                                    break;
                                }
                                spanLimit3 = spanLimit4;
                                if (this.offsets.containsOffset(inc) == 0 && matches16CPB(charSequence, pos - overlap4, length, string, length16)) {
                                    if (inc == rest) {
                                        return length;
                                    }
                                    this.offsets.addOffset(inc);
                                }
                                if (overlap4 == 0) {
                                    break;
                                }
                                inc++;
                                spanLimit4 = spanLimit3;
                                overlap4--;
                            }
                        }
                        spanLimit3 = spanLimit4;
                        overlap = i2 + 1;
                        spanLimit4 = spanLimit3;
                        c = 254;
                    }
                } else {
                    spanLimit2 = spanLimit4;
                    int maxInc = 0;
                    int maxOverlap = 0;
                    while (true) {
                        i = overlap;
                        if (i >= stringsLength) {
                            break;
                        }
                        short overlap5 = this.spanLengths[i];
                        String string2 = this.strings.get(i);
                        int length162 = string2.length();
                        int overlap6 = overlap5;
                        if (overlap5 >= 254) {
                            overlap6 = length162;
                        }
                        if (overlap6 > spanLength) {
                            overlap6 = spanLength;
                        }
                        int inc2 = length162 - overlap6;
                        int overlap7 = overlap6;
                        while (true) {
                            int inc3 = inc2;
                            if (inc3 > rest) {
                                initSize2 = initSize3;
                                break;
                            } else if (overlap7 < maxOverlap) {
                                initSize2 = initSize3;
                                break;
                            } else {
                                if (overlap7 <= maxOverlap) {
                                    if (inc3 <= maxInc) {
                                        initSize2 = initSize3;
                                        inc2 = inc3 + 1;
                                        initSize3 = initSize2;
                                        overlap7--;
                                    }
                                }
                                initSize2 = initSize3;
                                if (matches16CPB(charSequence, pos - overlap7, length, string2, length162) != 0) {
                                    maxInc = inc3;
                                    maxOverlap = overlap7;
                                    break;
                                }
                                inc2 = inc3 + 1;
                                initSize3 = initSize2;
                                overlap7--;
                            }
                        }
                        overlap = i + 1;
                        initSize3 = initSize2;
                        UnicodeSet.SpanCondition spanCondition3 = spanCondition;
                    }
                    initSize = initSize3;
                    if (maxInc == 0) {
                        if (maxOverlap == 0) {
                            int maxOverlap2 = i;
                        }
                    }
                    pos += maxInc;
                    rest -= maxInc;
                    if (rest == 0) {
                        return length;
                    }
                    spanLength = 0;
                    spanLimit4 = spanLimit2;
                    initSize3 = initSize;
                    spanCondition2 = spanCondition;
                }
                if (spanLength == 0) {
                    if (pos != 0) {
                        if (this.offsets.isEmpty()) {
                            spanLimit4 = this.spanSet.span(charSequence, pos, UnicodeSet.SpanCondition.CONTAINED);
                            spanLength = spanLimit4 - pos;
                            if (spanLength != rest && spanLength != 0) {
                                pos += spanLength;
                                rest -= spanLength;
                                initSize3 = initSize;
                                spanCondition2 = spanCondition;
                            }
                        } else {
                            int spanLength2 = spanOne(this.spanSet, charSequence, pos, rest);
                            if (spanLength2 > 0) {
                                if (spanLength2 == rest) {
                                    return length;
                                }
                                pos += spanLength2;
                                rest -= spanLength2;
                                this.offsets.shift(spanLength2);
                                spanLength = 0;
                                spanLimit4 = spanLimit2;
                                initSize3 = initSize;
                                spanCondition2 = spanCondition;
                            }
                            int minOffset = this.offsets.popMinimum(null);
                            pos += minOffset;
                            rest -= minOffset;
                            spanLength = 0;
                            spanLimit4 = spanLimit2;
                            initSize3 = initSize;
                            spanCondition2 = spanCondition;
                        }
                    }
                }
                if (this.offsets.isEmpty()) {
                    return pos;
                }
                int minOffset2 = this.offsets.popMinimum(null);
                pos += minOffset2;
                rest -= minOffset2;
                spanLength = 0;
                spanLimit4 = spanLimit2;
                initSize3 = initSize;
                spanCondition2 = spanCondition;
            }
        }
    }

    /*  JADX ERROR: JadxOverflowException in pass: LoopRegionVisitor
        jadx.core.utils.exceptions.JadxOverflowException: LoopRegionVisitor.assignOnlyInLoop endless recursion
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public int spanAndCount(java.lang.CharSequence r18, int r19, android.icu.text.UnicodeSet.SpanCondition r20, android.icu.util.OutputInt r21) {
        /*
            r17 = this;
            r0 = r17
            r1 = r18
            r2 = r19
            r3 = r20
            r4 = r21
            android.icu.text.UnicodeSet$SpanCondition r5 = android.icu.text.UnicodeSet.SpanCondition.NOT_CONTAINED
            if (r3 != r5) goto L_0x0013
            int r5 = r0.spanNot(r1, r2, r4)
            return r5
        L_0x0013:
            android.icu.text.UnicodeSet$SpanCondition r5 = android.icu.text.UnicodeSet.SpanCondition.CONTAINED
            if (r3 != r5) goto L_0x001c
            int r5 = r0.spanContainedAndCount(r1, r2, r4)
            return r5
        L_0x001c:
            java.util.ArrayList<java.lang.String> r5 = r0.strings
            int r5 = r5.size()
            int r6 = r18.length()
            r7 = r2
            int r8 = r6 - r2
            r9 = 0
            r10 = r7
            r7 = r9
        L_0x002c:
            if (r8 == 0) goto L_0x0061
            android.icu.text.UnicodeSet r11 = r0.spanSet
            int r11 = spanOne(r11, r1, r10, r8)
            if (r11 <= 0) goto L_0x0038
            r12 = r11
            goto L_0x0039
        L_0x0038:
            r12 = r9
        L_0x0039:
            r13 = r12
            r12 = r9
        L_0x003b:
            if (r12 >= r5) goto L_0x0057
            java.util.ArrayList<java.lang.String> r14 = r0.strings
            java.lang.Object r14 = r14.get(r12)
            java.lang.String r14 = (java.lang.String) r14
            int r15 = r14.length()
            if (r13 >= r15) goto L_0x0054
            if (r15 > r8) goto L_0x0054
            boolean r16 = matches16CPB(r1, r10, r6, r14, r15)
            if (r16 == 0) goto L_0x0054
            r13 = r15
        L_0x0054:
            int r12 = r12 + 1
            goto L_0x003b
        L_0x0057:
            if (r13 != 0) goto L_0x005c
            r4.value = r7
            return r10
        L_0x005c:
            int r7 = r7 + 1
            int r10 = r10 + r13
            int r8 = r8 - r13
            goto L_0x002c
        L_0x0061:
            r4.value = r7
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.UnicodeSetStringSpan.spanAndCount(java.lang.CharSequence, int, android.icu.text.UnicodeSet$SpanCondition, android.icu.util.OutputInt):int");
    }

    private synchronized int spanContainedAndCount(CharSequence s, int start, OutputInt outCount) {
        this.offsets.setMaxLength(this.maxLength16);
        int stringsLength = this.strings.size();
        int length = s.length();
        int rest = length - start;
        int pos = start;
        int count = 0;
        while (rest != 0) {
            int cpLength = spanOne(this.spanSet, s, pos, rest);
            if (cpLength > 0) {
                this.offsets.addOffsetAndCount(cpLength, count + 1);
            }
            for (int i = 0; i < stringsLength; i++) {
                String string = this.strings.get(i);
                int length16 = string.length();
                if (length16 <= rest && !this.offsets.hasCountAtOffset(length16, count + 1) && matches16CPB(s, pos, length, string, length16)) {
                    this.offsets.addOffsetAndCount(length16, count + 1);
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

    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0103, code lost:
        return r4;
     */
    /* JADX WARNING: Failed to insert additional move for type inference */
    public synchronized int spanBack(CharSequence s, int length, UnicodeSet.SpanCondition spanCondition) {
        CharSequence charSequence = s;
        int i = length;
        UnicodeSet.SpanCondition spanCondition2 = spanCondition;
        synchronized (this) {
            if (spanCondition2 == UnicodeSet.SpanCondition.NOT_CONTAINED) {
                int spanNotBack = spanNotBack(s, length);
                return spanNotBack;
            }
            int pos = this.spanSet.spanBack(charSequence, i, UnicodeSet.SpanCondition.CONTAINED);
            int i2 = 0;
            if (pos == 0) {
                return 0;
            }
            int spanLength = i - pos;
            int initSize = 0;
            if (spanCondition2 == UnicodeSet.SpanCondition.CONTAINED) {
                initSize = this.maxLength16;
            }
            this.offsets.setMaxLength(initSize);
            int stringsLength = this.strings.size();
            int spanBackLengthsOffset = 0;
            if (this.all) {
                spanBackLengthsOffset = stringsLength;
            }
            while (true) {
                char c = 254;
                if (spanCondition2 == UnicodeSet.SpanCondition.CONTAINED) {
                    int i3 = i2;
                    while (i3 < stringsLength) {
                        short overlap = this.spanLengths[spanBackLengthsOffset + i3];
                        if (overlap != 255) {
                            String string = this.strings.get(i3);
                            int length16 = string.length();
                            int overlap2 = overlap;
                            if (overlap >= c) {
                                overlap2 = length16 - string.offsetByCodePoints(i2, 1);
                            }
                            if (overlap2 > spanLength) {
                                overlap2 = spanLength;
                            }
                            int dec = length16 - overlap2;
                            int overlap3 = overlap2;
                            while (true) {
                                if (dec > pos) {
                                    break;
                                }
                                if (!this.offsets.containsOffset(dec) && matches16CPB(charSequence, pos - dec, i, string, length16)) {
                                    if (dec == pos) {
                                        return i2;
                                    }
                                    this.offsets.addOffset(dec);
                                }
                                if (overlap3 == 0) {
                                    break;
                                }
                                dec++;
                                overlap3--;
                            }
                        }
                        i3++;
                        c = 254;
                    }
                } else {
                    int maxOverlap = 0;
                    int maxOverlap2 = 0;
                    int i4 = i2;
                    while (i4 < stringsLength) {
                        short overlap4 = this.spanLengths[spanBackLengthsOffset + i4];
                        String string2 = this.strings.get(i4);
                        int length162 = string2.length();
                        int overlap5 = overlap4;
                        if (overlap4 >= 254) {
                            overlap5 = length162;
                        }
                        if (overlap5 > spanLength) {
                            overlap5 = spanLength;
                        }
                        int dec2 = length162 - overlap5;
                        int overlap6 = overlap5;
                        while (true) {
                            int dec3 = dec2;
                            if (dec3 > pos) {
                                break;
                            } else if (overlap6 < maxOverlap) {
                                break;
                            } else if ((overlap6 > maxOverlap || dec3 > maxOverlap2) && matches16CPB(charSequence, pos - dec3, i, string2, length162)) {
                                maxOverlap = overlap6;
                                maxOverlap2 = dec3;
                                break;
                            } else {
                                dec2 = dec3 + 1;
                                UnicodeSet.SpanCondition spanCondition3 = spanCondition;
                                overlap6--;
                            }
                        }
                        i4++;
                        UnicodeSet.SpanCondition spanCondition4 = spanCondition;
                    }
                    if (maxOverlap2 == 0) {
                        if (maxOverlap != 0) {
                        }
                    }
                    pos -= maxOverlap2;
                    if (pos == 0) {
                        return 0;
                    }
                    spanLength = 0;
                    i2 = 0;
                    spanCondition2 = spanCondition;
                }
                if (spanLength == 0) {
                    if (pos != i) {
                        if (this.offsets.isEmpty()) {
                            int oldPos = pos;
                            pos = this.spanSet.spanBack(charSequence, oldPos, UnicodeSet.SpanCondition.CONTAINED);
                            spanLength = oldPos - pos;
                            if (pos == 0 || spanLength == 0) {
                            }
                        } else {
                            int spanLength2 = spanOneBack(this.spanSet, charSequence, pos);
                            if (spanLength2 > 0) {
                                if (spanLength2 == pos) {
                                    return 0;
                                }
                                pos -= spanLength2;
                                this.offsets.shift(spanLength2);
                                spanLength = 0;
                            }
                            pos -= this.offsets.popMinimum(null);
                            spanLength = 0;
                        }
                        spanCondition2 = spanCondition;
                        i2 = 0;
                    }
                }
                if (this.offsets.isEmpty()) {
                    return pos;
                }
                pos -= this.offsets.popMinimum(null);
                spanLength = 0;
                spanCondition2 = spanCondition;
                i2 = 0;
            }
        }
    }

    private int spanNot(CharSequence s, int start, OutputInt outCount) {
        int spanLimit;
        int rest;
        int cpLength;
        int length = s.length();
        int stringsLength = this.strings.size();
        int i = length - start;
        int pos = start;
        int count = 0;
        do {
            if (outCount == null) {
                spanLimit = this.spanNotSet.span(s, pos, UnicodeSet.SpanCondition.NOT_CONTAINED);
            } else {
                spanLimit = this.spanNotSet.spanAndCount(s, pos, UnicodeSet.SpanCondition.NOT_CONTAINED, outCount);
                int i2 = outCount.value + count;
                count = i2;
                outCount.value = i2;
            }
            if (spanLimit == length) {
                return length;
            }
            int pos2 = spanLimit;
            rest = length - spanLimit;
            cpLength = spanOne(this.spanSet, s, pos2, rest);
            if (cpLength > 0) {
                return pos2;
            }
            for (int i3 = 0; i3 < stringsLength; i3++) {
                if (this.spanLengths[i3] != 255) {
                    String string = this.strings.get(i3);
                    int length16 = string.length();
                    if (length16 <= rest && matches16CPB(s, pos2, length, string, length16)) {
                        return pos2;
                    }
                }
            }
            pos = pos2 - cpLength;
            count++;
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
            int pos2 = this.spanNotSet.spanBack(s, pos, UnicodeSet.SpanCondition.NOT_CONTAINED);
            if (pos2 == 0) {
                return 0;
            }
            int cpLength = spanOneBack(this.spanSet, s, pos2);
            if (cpLength > 0) {
                return pos2;
            }
            for (int i = 0; i < stringsLength; i++) {
                if (this.spanLengths[i] != 255) {
                    String string = this.strings.get(i);
                    int length16 = string.length();
                    if (length16 <= pos2 && matches16CPB(s, pos2 - length16, length, string, length16)) {
                        return pos2;
                    }
                }
            }
            pos = pos2 + cpLength;
        } while (pos != 0);
        return 0;
    }

    static short makeSpanLengthByte(int spanLength) {
        return spanLength < 254 ? (short) spanLength : LONG_SPAN;
    }

    private static boolean matches16(CharSequence s, int start, String t, int length) {
        int end = start + length;
        while (true) {
            int length2 = length - 1;
            if (length <= 0) {
                return true;
            }
            end--;
            if (s.charAt(end) != t.charAt(length2)) {
                return false;
            }
            length = length2;
        }
    }

    static boolean matches16CPB(CharSequence s, int start, int limit, String t, int tlength) {
        if (!matches16(s, start, t, tlength) || ((start > 0 && Character.isHighSurrogate(s.charAt(start - 1)) && Character.isLowSurrogate(s.charAt(start))) || (start + tlength < limit && Character.isHighSurrogate(s.charAt((start + tlength) - 1)) && Character.isLowSurrogate(s.charAt(start + tlength))))) {
            return false;
        }
        return true;
    }

    static int spanOne(UnicodeSet set, CharSequence s, int start, int length) {
        char c = s.charAt(start);
        if (c >= 55296 && c <= 56319) {
            int i = 2;
            if (length >= 2) {
                char c2 = s.charAt(start + 1);
                if (UTF16.isTrailSurrogate(c2)) {
                    if (!set.contains(Character.toCodePoint(c, c2))) {
                        i = -2;
                    }
                    return i;
                }
            }
        }
        return set.contains((int) c) ? 1 : -1;
    }

    static int spanOneBack(UnicodeSet set, CharSequence s, int length) {
        char c = s.charAt(length - 1);
        if (c >= 56320 && c <= 57343) {
            int i = 2;
            if (length >= 2) {
                char c2 = s.charAt(length - 2);
                if (UTF16.isLeadSurrogate(c2)) {
                    if (!set.contains(Character.toCodePoint(c2, c))) {
                        i = -2;
                    }
                    return i;
                }
            }
        }
        return set.contains((int) c) ? 1 : -1;
    }
}
