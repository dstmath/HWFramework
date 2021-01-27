package ohos.global.icu.impl;

import java.util.ArrayList;
import ohos.global.icu.text.UTF16;
import ohos.global.icu.text.UnicodeSet;
import ohos.global.icu.util.OutputInt;

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

    static short makeSpanLengthByte(int i) {
        if (i < 254) {
            return (short) i;
        }
        return 254;
    }

    public UnicodeSetStringSpan(UnicodeSet unicodeSet, ArrayList<String> arrayList, int i) {
        this.spanSet = new UnicodeSet(0, 1114111);
        this.strings = arrayList;
        this.all = i == 127;
        this.spanSet.retainAll(unicodeSet);
        int i2 = i & 1;
        if (i2 != 0) {
            this.spanNotSet = this.spanSet;
        }
        this.offsets = new OffsetList();
        int size = this.strings.size();
        this.someRelevant = false;
        int i3 = 0;
        for (int i4 = 0; i4 < size; i4++) {
            String str = this.strings.get(i4);
            int length = str.length();
            if (this.spanSet.span(str, UnicodeSet.SpanCondition.CONTAINED) < length) {
                this.someRelevant = true;
            }
            if (length > i3) {
                i3 = length;
            }
        }
        this.maxLength16 = i3;
        if (this.someRelevant || (i & 64) != 0) {
            if (this.all) {
                this.spanSet.freeze();
            }
            this.spanLengths = new short[(this.all ? size * 2 : size)];
            int i5 = this.all ? size : 0;
            for (int i6 = 0; i6 < size; i6++) {
                String str2 = this.strings.get(i6);
                int length2 = str2.length();
                int span = this.spanSet.span(str2, UnicodeSet.SpanCondition.CONTAINED);
                if (span < length2) {
                    if ((i & 2) != 0) {
                        if ((i & 32) != 0) {
                            this.spanLengths[i6] = makeSpanLengthByte(span);
                        }
                        if ((i & 16) != 0) {
                            this.spanLengths[i5 + i6] = makeSpanLengthByte(length2 - this.spanSet.spanBack(str2, length2, UnicodeSet.SpanCondition.CONTAINED));
                        }
                    } else {
                        short[] sArr = this.spanLengths;
                        sArr[i5 + i6] = 0;
                        sArr[i6] = 0;
                    }
                    if (i2 != 0) {
                        if ((i & 32) != 0) {
                            addToSpanNotSet(str2.codePointAt(0));
                        }
                        if ((i & 16) != 0) {
                            addToSpanNotSet(str2.codePointBefore(length2));
                        }
                    }
                } else if (this.all) {
                    short[] sArr2 = this.spanLengths;
                    sArr2[i5 + i6] = 255;
                    sArr2[i6] = 255;
                } else {
                    this.spanLengths[i6] = 255;
                }
            }
            if (this.all) {
                this.spanNotSet.freeze();
            }
        }
    }

    public UnicodeSetStringSpan(UnicodeSetStringSpan unicodeSetStringSpan, ArrayList<String> arrayList) {
        this.spanSet = unicodeSetStringSpan.spanSet;
        this.strings = arrayList;
        this.maxLength16 = unicodeSetStringSpan.maxLength16;
        this.someRelevant = unicodeSetStringSpan.someRelevant;
        this.all = true;
        if (Utility.sameObjects(unicodeSetStringSpan.spanNotSet, unicodeSetStringSpan.spanSet)) {
            this.spanNotSet = this.spanSet;
        } else {
            this.spanNotSet = (UnicodeSet) unicodeSetStringSpan.spanNotSet.clone();
        }
        this.offsets = new OffsetList();
        this.spanLengths = (short[]) unicodeSetStringSpan.spanLengths.clone();
    }

    public boolean needsStringSpanUTF16() {
        return this.someRelevant;
    }

    public boolean contains(int i) {
        return this.spanSet.contains(i);
    }

    private void addToSpanNotSet(int i) {
        if (Utility.sameObjects(this.spanNotSet, null) || Utility.sameObjects(this.spanNotSet, this.spanSet)) {
            if (!this.spanSet.contains(i)) {
                this.spanNotSet = this.spanSet.cloneAsThawed();
            } else {
                return;
            }
        }
        this.spanNotSet.add(i);
    }

    public int span(CharSequence charSequence, int i, UnicodeSet.SpanCondition spanCondition) {
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            return spanNot(charSequence, i, null);
        }
        int span = this.spanSet.span(charSequence, i, UnicodeSet.SpanCondition.CONTAINED);
        if (span == charSequence.length()) {
            return span;
        }
        return spanWithStrings(charSequence, i, span, spanCondition);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:18:0x004a */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:39:0x008d */
    private synchronized int spanWithStrings(CharSequence charSequence, int i, int i2, UnicodeSet.SpanCondition spanCondition) {
        int span;
        this.offsets.setMaxLength(spanCondition == UnicodeSet.SpanCondition.CONTAINED ? this.maxLength16 : 0);
        int length = charSequence.length();
        int i3 = i2 - i;
        int size = this.strings.size();
        int i4 = length - i2;
        int i5 = i2;
        while (true) {
            char c = 254;
            if (spanCondition == UnicodeSet.SpanCondition.CONTAINED) {
                for (int i6 = 0; i6 < size; i6++) {
                    short s = this.spanLengths[i6];
                    if (s != 255) {
                        String str = this.strings.get(i6);
                        int length2 = str.length();
                        short s2 = s;
                        if (s >= 254) {
                            s2 = str.offsetByCodePoints(length2, -1);
                        }
                        if (s2 > i3) {
                            s2 = i3;
                        }
                        int i7 = length2 - (s2 == 1 ? 1 : 0);
                        int i8 = s2;
                        while (true) {
                            if (i7 > i4) {
                                break;
                            }
                            if (!this.offsets.containsOffset(i7) && matches16CPB(charSequence, i5 - i8, length, str, length2)) {
                                if (i7 == i4) {
                                    return length;
                                }
                                this.offsets.addOffset(i7);
                            }
                            if (i8 == 0) {
                                break;
                            }
                            i7++;
                            i8 = (i8 == 1 ? 1 : 0) - 1;
                        }
                    }
                }
            } else {
                int i9 = 0;
                int i10 = 0;
                int i11 = 0;
                while (i9 < size) {
                    short s3 = this.spanLengths[i9];
                    String str2 = this.strings.get(i9);
                    int length3 = str2.length();
                    short s4 = s3;
                    if (s3 >= c) {
                        s4 = length3;
                    }
                    if (s4 > i3) {
                        s4 = i3;
                    }
                    int i12 = s4 == 1 ? 1 : 0;
                    int i13 = s4 == 1 ? 1 : 0;
                    int i14 = length3 - i12;
                    int i15 = s4 == 1 ? 1 : 0;
                    boolean z = s4 == 1 ? 1 : 0;
                    int i16 = i15;
                    int i17 = i14;
                    while (true) {
                        if (i17 > i4 || i16 < i11) {
                            break;
                        } else if ((i16 > i11 || i17 > i10) && matches16CPB(charSequence, i5 - i16, length, str2, length3)) {
                            i11 = i16;
                            i10 = i17;
                            break;
                        } else {
                            i16--;
                            i17++;
                        }
                    }
                    i9++;
                    c = 254;
                }
                if (!(i10 == 0 && i11 == 0)) {
                    i5 += i10;
                    i4 -= i10;
                    if (i4 == 0) {
                        return length;
                    }
                    i3 = 0;
                }
            }
            if (i3 != 0 || i5 == 0) {
                if (this.offsets.isEmpty()) {
                    return i5;
                }
            } else if (this.offsets.isEmpty()) {
                span = this.spanSet.span(charSequence, i5, UnicodeSet.SpanCondition.CONTAINED);
                i3 = span - i5;
                if (i3 == i4 || i3 == 0) {
                    break;
                }
                i5 += i3;
                i4 -= i3;
            } else {
                int spanOne = spanOne(this.spanSet, charSequence, i5, i4);
                if (spanOne > 0) {
                    if (spanOne == i4) {
                        return length;
                    }
                    i5 += spanOne;
                    i4 -= spanOne;
                    this.offsets.shift(spanOne);
                    i3 = 0;
                }
            }
            int popMinimum = this.offsets.popMinimum(null);
            i5 += popMinimum;
            i4 -= popMinimum;
            i3 = 0;
        }
        return span;
    }

    /*  JADX ERROR: JadxOverflowException in pass: LoopRegionVisitor
        jadx.core.utils.exceptions.JadxOverflowException: LoopRegionVisitor.assignOnlyInLoop endless recursion
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:57)
        	at jadx.core.utils.ErrorsCounter.error(ErrorsCounter.java:31)
        	at jadx.core.dex.attributes.nodes.NotificationAttrNode.addError(NotificationAttrNode.java:15)
        */
    public int spanAndCount(java.lang.CharSequence r9, int r10, ohos.global.icu.text.UnicodeSet.SpanCondition r11, ohos.global.icu.util.OutputInt r12) {
        /*
            r8 = this;
            ohos.global.icu.text.UnicodeSet$SpanCondition r0 = ohos.global.icu.text.UnicodeSet.SpanCondition.NOT_CONTAINED
            if (r11 != r0) goto L_0x0009
            int r8 = r8.spanNot(r9, r10, r12)
            return r8
        L_0x0009:
            ohos.global.icu.text.UnicodeSet$SpanCondition r0 = ohos.global.icu.text.UnicodeSet.SpanCondition.CONTAINED
            if (r11 != r0) goto L_0x0012
            int r8 = r8.spanContainedAndCount(r9, r10, r12)
            return r8
        L_0x0012:
            java.util.ArrayList<java.lang.String> r11 = r8.strings
            int r11 = r11.size()
            int r0 = r9.length()
            int r1 = r0 - r10
            r2 = 0
            r3 = r10
            r10 = r2
        L_0x0021:
            if (r1 == 0) goto L_0x0055
            ohos.global.icu.text.UnicodeSet r4 = r8.spanSet
            int r4 = spanOne(r4, r9, r3, r1)
            if (r4 <= 0) goto L_0x002c
            goto L_0x002d
        L_0x002c:
            r4 = r2
        L_0x002d:
            r5 = r4
            r4 = r2
        L_0x002f:
            if (r4 >= r11) goto L_0x004b
            java.util.ArrayList<java.lang.String> r6 = r8.strings
            java.lang.Object r6 = r6.get(r4)
            java.lang.String r6 = (java.lang.String) r6
            int r7 = r6.length()
            if (r5 >= r7) goto L_0x0048
            if (r7 > r1) goto L_0x0048
            boolean r6 = matches16CPB(r9, r3, r0, r6, r7)
            if (r6 == 0) goto L_0x0048
            r5 = r7
        L_0x0048:
            int r4 = r4 + 1
            goto L_0x002f
        L_0x004b:
            if (r5 != 0) goto L_0x0050
            r12.value = r10
            return r3
        L_0x0050:
            int r10 = r10 + 1
            int r3 = r3 + r5
            int r1 = r1 - r5
            goto L_0x0021
        L_0x0055:
            r12.value = r10
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: ohos.global.icu.impl.UnicodeSetStringSpan.spanAndCount(java.lang.CharSequence, int, ohos.global.icu.text.UnicodeSet$SpanCondition, ohos.global.icu.util.OutputInt):int");
    }

    private synchronized int spanContainedAndCount(CharSequence charSequence, int i, OutputInt outputInt) {
        this.offsets.setMaxLength(this.maxLength16);
        int size = this.strings.size();
        int length = charSequence.length();
        int i2 = length - i;
        int i3 = i;
        int i4 = 0;
        while (i2 != 0) {
            int spanOne = spanOne(this.spanSet, charSequence, i3, i2);
            if (spanOne > 0) {
                this.offsets.addOffsetAndCount(spanOne, i4 + 1);
            }
            for (int i5 = 0; i5 < size; i5++) {
                String str = this.strings.get(i5);
                int length2 = str.length();
                if (length2 <= i2) {
                    int i6 = i4 + 1;
                    if (!this.offsets.hasCountAtOffset(length2, i6) && matches16CPB(charSequence, i3, length, str, length2)) {
                        this.offsets.addOffsetAndCount(length2, i6);
                    }
                }
            }
            if (this.offsets.isEmpty()) {
                outputInt.value = i4;
                return i3;
            }
            int popMinimum = this.offsets.popMinimum(outputInt);
            i3 += popMinimum;
            i2 -= popMinimum;
            i4 = outputInt.value;
        }
        outputInt.value = i4;
        return i3;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:29:0x0065 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:49:0x00aa */
    public synchronized int spanBack(CharSequence charSequence, int i, UnicodeSet.SpanCondition spanCondition) {
        int spanBack;
        if (spanCondition == UnicodeSet.SpanCondition.NOT_CONTAINED) {
            return spanNotBack(charSequence, i);
        }
        int spanBack2 = this.spanSet.spanBack(charSequence, i, UnicodeSet.SpanCondition.CONTAINED);
        int i2 = 0;
        if (spanBack2 == 0) {
            return 0;
        }
        int i3 = i - spanBack2;
        this.offsets.setMaxLength(spanCondition == UnicodeSet.SpanCondition.CONTAINED ? this.maxLength16 : 0);
        int size = this.strings.size();
        int i4 = this.all ? size : 0;
        while (true) {
            char c = 254;
            if (spanCondition == UnicodeSet.SpanCondition.CONTAINED) {
                for (int i5 = i2; i5 < size; i5++) {
                    short s = this.spanLengths[i4 + i5];
                    if (s != 255) {
                        String str = this.strings.get(i5);
                        int length = str.length();
                        short s2 = s;
                        if (s >= 254) {
                            s2 = length - str.offsetByCodePoints(i2, 1);
                        }
                        if (s2 > i3) {
                            s2 = i3;
                        }
                        int i6 = length - (s2 == 1 ? 1 : 0);
                        int i7 = s2;
                        while (i6 <= spanBack2) {
                            if (!this.offsets.containsOffset(i6) && matches16CPB(charSequence, spanBack2 - i6, i, str, length)) {
                                if (i6 == spanBack2) {
                                    return i2;
                                }
                                this.offsets.addOffset(i6);
                            }
                            if (i7 == 0) {
                                break;
                            }
                            i6++;
                            i7 = (i7 == 1 ? 1 : 0) - 1;
                        }
                        continue;
                    }
                }
            } else {
                int i8 = i2;
                int i9 = i8;
                int i10 = i9;
                while (i8 < size) {
                    short s3 = this.spanLengths[i4 + i8];
                    String str2 = this.strings.get(i8);
                    int length2 = str2.length();
                    short s4 = s3;
                    if (s3 >= c) {
                        s4 = length2;
                    }
                    if (s4 > i3) {
                        s4 = i3;
                    }
                    int i11 = s4 == 1 ? 1 : 0;
                    int i12 = s4 == 1 ? 1 : 0;
                    int i13 = length2 - i11;
                    int i14 = s4 == 1 ? 1 : 0;
                    boolean z = s4 == 1 ? 1 : 0;
                    int i15 = i14;
                    int i16 = i13;
                    while (true) {
                        if (i16 > spanBack2 || i15 < i10) {
                            break;
                        } else if ((i15 > i10 || i16 > i9) && matches16CPB(charSequence, spanBack2 - i16, i, str2, length2)) {
                            i10 = i15;
                            i9 = i16;
                            break;
                        } else {
                            i15--;
                            i16++;
                        }
                    }
                    i8++;
                    c = 254;
                }
                if (!(i9 == 0 && i10 == 0)) {
                    spanBack2 -= i9;
                    if (spanBack2 == 0) {
                        return 0;
                    }
                    i2 = 0;
                    i3 = 0;
                }
            }
            if (i3 != 0 || spanBack2 == i) {
                if (this.offsets.isEmpty()) {
                    return spanBack2;
                }
            } else if (this.offsets.isEmpty()) {
                spanBack = this.spanSet.spanBack(charSequence, spanBack2, UnicodeSet.SpanCondition.CONTAINED);
                i3 = spanBack2 - spanBack;
                if (spanBack == 0 || i3 == 0) {
                    break;
                }
                spanBack2 = spanBack;
                i2 = 0;
            } else {
                int spanOneBack = spanOneBack(this.spanSet, charSequence, spanBack2);
                if (spanOneBack > 0) {
                    if (spanOneBack == spanBack2) {
                        return 0;
                    }
                    spanBack2 -= spanOneBack;
                    this.offsets.shift(spanOneBack);
                    i2 = 0;
                    i3 = 0;
                }
            }
            spanBack2 -= this.offsets.popMinimum(null);
            i2 = 0;
            i3 = 0;
        }
        return spanBack;
    }

    private int spanNot(CharSequence charSequence, int i, OutputInt outputInt) {
        int i2;
        int i3;
        int spanOne;
        String str;
        int length;
        int length2 = charSequence.length();
        int size = this.strings.size();
        int i4 = 0;
        do {
            if (outputInt == null) {
                i2 = this.spanNotSet.span(charSequence, i, UnicodeSet.SpanCondition.NOT_CONTAINED);
            } else {
                i2 = this.spanNotSet.spanAndCount(charSequence, i, UnicodeSet.SpanCondition.NOT_CONTAINED, outputInt);
                i4 += outputInt.value;
                outputInt.value = i4;
            }
            if (i2 == length2) {
                return length2;
            }
            i3 = length2 - i2;
            spanOne = spanOne(this.spanSet, charSequence, i2, i3);
            if (spanOne > 0) {
                return i2;
            }
            for (int i5 = 0; i5 < size; i5++) {
                if (this.spanLengths[i5] != 255 && (length = (str = this.strings.get(i5)).length()) <= i3 && matches16CPB(charSequence, i2, length2, str, length)) {
                    return i2;
                }
            }
            i = i2 - spanOne;
            i4++;
        } while (i3 + spanOne != 0);
        if (outputInt != null) {
            outputInt.value = i4;
        }
        return length2;
    }

    private int spanNotBack(CharSequence charSequence, int i) {
        String str;
        int length;
        int size = this.strings.size();
        int i2 = i;
        do {
            int spanBack = this.spanNotSet.spanBack(charSequence, i2, UnicodeSet.SpanCondition.NOT_CONTAINED);
            if (spanBack == 0) {
                return 0;
            }
            int spanOneBack = spanOneBack(this.spanSet, charSequence, spanBack);
            if (spanOneBack > 0) {
                return spanBack;
            }
            for (int i3 = 0; i3 < size; i3++) {
                if (this.spanLengths[i3] != 255 && (length = (str = this.strings.get(i3)).length()) <= spanBack && matches16CPB(charSequence, spanBack - length, i, str, length)) {
                    return spanBack;
                }
            }
            i2 = spanBack + spanOneBack;
        } while (i2 != 0);
        return 0;
    }

    private static boolean matches16(CharSequence charSequence, int i, String str, int i2) {
        int i3 = i + i2;
        while (true) {
            int i4 = i2 - 1;
            if (i2 <= 0) {
                return true;
            }
            i3--;
            if (charSequence.charAt(i3) != str.charAt(i4)) {
                return false;
            }
            i2 = i4;
        }
    }

    static boolean matches16CPB(CharSequence charSequence, int i, int i2, String str, int i3) {
        int i4;
        return matches16(charSequence, i, str, i3) && (i <= 0 || !Character.isHighSurrogate(charSequence.charAt(i + -1)) || !Character.isLowSurrogate(charSequence.charAt(i))) && ((i4 = i + i3) >= i2 || !Character.isHighSurrogate(charSequence.charAt(i4 + -1)) || !Character.isLowSurrogate(charSequence.charAt(i4)));
    }

    static int spanOne(UnicodeSet unicodeSet, CharSequence charSequence, int i, int i2) {
        char charAt = charSequence.charAt(i);
        if (charAt >= 55296 && charAt <= 56319 && i2 >= 2) {
            char charAt2 = charSequence.charAt(i + 1);
            if (UTF16.isTrailSurrogate(charAt2)) {
                if (unicodeSet.contains(Character.toCodePoint(charAt, charAt2))) {
                    return 2;
                }
                return -2;
            }
        }
        if (unicodeSet.contains(charAt)) {
            return 1;
        }
        return -1;
    }

    static int spanOneBack(UnicodeSet unicodeSet, CharSequence charSequence, int i) {
        char charAt = charSequence.charAt(i - 1);
        if (charAt >= 56320 && charAt <= 57343 && i >= 2) {
            char charAt2 = charSequence.charAt(i - 2);
            if (UTF16.isLeadSurrogate(charAt2)) {
                if (unicodeSet.contains(Character.toCodePoint(charAt2, charAt))) {
                    return 2;
                }
                return -2;
            }
        }
        return unicodeSet.contains(charAt) ? 1 : -1;
    }

    /* access modifiers changed from: private */
    public static final class OffsetList {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private int length;
        private int[] list = new int[16];
        private int start;

        public void setMaxLength(int i) {
            if (i > this.list.length) {
                this.list = new int[i];
            }
            clear();
        }

        public void clear() {
            int length2 = this.list.length;
            while (true) {
                int i = length2 - 1;
                if (length2 > 0) {
                    this.list[i] = 0;
                    length2 = i;
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

        public void shift(int i) {
            int i2 = this.start + i;
            int[] iArr = this.list;
            if (i2 >= iArr.length) {
                i2 -= iArr.length;
            }
            int[] iArr2 = this.list;
            if (iArr2[i2] != 0) {
                iArr2[i2] = 0;
                this.length--;
            }
            this.start = i2;
        }

        public void addOffset(int i) {
            int i2 = this.start + i;
            int[] iArr = this.list;
            if (i2 >= iArr.length) {
                i2 -= iArr.length;
            }
            this.list[i2] = 1;
            this.length++;
        }

        public void addOffsetAndCount(int i, int i2) {
            int i3 = this.start + i;
            int[] iArr = this.list;
            if (i3 >= iArr.length) {
                i3 -= iArr.length;
            }
            int[] iArr2 = this.list;
            if (iArr2[i3] == 0) {
                iArr2[i3] = i2;
                this.length++;
            } else if (i2 < iArr2[i3]) {
                iArr2[i3] = i2;
            }
        }

        public boolean containsOffset(int i) {
            int i2 = this.start + i;
            int[] iArr = this.list;
            if (i2 >= iArr.length) {
                i2 -= iArr.length;
            }
            return this.list[i2] != 0;
        }

        public boolean hasCountAtOffset(int i, int i2) {
            int i3 = this.start + i;
            int[] iArr = this.list;
            if (i3 >= iArr.length) {
                i3 -= iArr.length;
            }
            int i4 = this.list[i3];
            return i4 != 0 && i4 <= i2;
        }

        public int popMinimum(OutputInt outputInt) {
            int[] iArr;
            int[] iArr2;
            int i;
            int i2;
            int i3 = this.start;
            do {
                i3++;
                iArr = this.list;
                if (i3 < iArr.length) {
                    i2 = iArr[i3];
                } else {
                    int length2 = iArr.length - this.start;
                    int i4 = 0;
                    while (true) {
                        iArr2 = this.list;
                        i = iArr2[i4];
                        if (i != 0) {
                            break;
                        }
                        i4++;
                    }
                    iArr2[i4] = 0;
                    this.length--;
                    this.start = i4;
                    if (outputInt != null) {
                        outputInt.value = i;
                    }
                    return length2 + i4;
                }
            } while (i2 == 0);
            iArr[i3] = 0;
            this.length--;
            int i5 = i3 - this.start;
            this.start = i3;
            if (outputInt != null) {
                outputInt.value = i2;
            }
            return i5;
        }
    }
}
