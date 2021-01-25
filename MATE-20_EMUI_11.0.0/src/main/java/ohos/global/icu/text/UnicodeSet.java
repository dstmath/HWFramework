package ohos.global.icu.text;

import java.io.IOException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import ohos.com.sun.org.apache.xml.internal.utils.LocaleUtility;
import ohos.global.icu.impl.BMPSet;
import ohos.global.icu.impl.CharacterPropertiesImpl;
import ohos.global.icu.impl.PatternProps;
import ohos.global.icu.impl.PatternTokenizer;
import ohos.global.icu.impl.RuleCharacterIterator;
import ohos.global.icu.impl.SortedSetRelation;
import ohos.global.icu.impl.StringRange;
import ohos.global.icu.impl.UCaseProps;
import ohos.global.icu.impl.UCharacterProperty;
import ohos.global.icu.impl.UPropertyAliases;
import ohos.global.icu.impl.UnicodeSetStringSpan;
import ohos.global.icu.impl.Utility;
import ohos.global.icu.lang.CharSequences;
import ohos.global.icu.lang.CharacterProperties;
import ohos.global.icu.lang.UCharacter;
import ohos.global.icu.lang.UProperty;
import ohos.global.icu.lang.UScript;
import ohos.global.icu.util.Freezable;
import ohos.global.icu.util.ICUUncheckedIOException;
import ohos.global.icu.util.OutputInt;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.VersionInfo;

public class UnicodeSet extends UnicodeFilter implements Iterable<String>, Comparable<UnicodeSet>, Freezable<UnicodeSet> {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final int ADD_CASE_MAPPINGS = 4;
    public static final UnicodeSet ALL_CODE_POINTS = new UnicodeSet(0, 1114111).freeze();
    private static final String ANY_ID = "ANY";
    private static final String ASCII_ID = "ASCII";
    private static final String ASSIGNED = "Assigned";
    public static final int CASE = 2;
    public static final int CASE_INSENSITIVE = 2;
    public static final UnicodeSet EMPTY = new UnicodeSet().freeze();
    private static final SortedSet<String> EMPTY_STRINGS = Collections.unmodifiableSortedSet(new TreeSet());
    private static final int HIGH = 1114112;
    public static final int IGNORE_SPACE = 1;
    private static final int INITIAL_CAPACITY = 25;
    private static final int LAST0_START = 0;
    private static final int LAST1_RANGE = 1;
    private static final int LAST2_SET = 2;
    private static final int LOW = 0;
    private static final int MAX_DEPTH = 100;
    private static final int MAX_LENGTH = 1114113;
    public static final int MAX_VALUE = 1114111;
    public static final int MIN_VALUE = 0;
    private static final int MODE0_NONE = 0;
    private static final int MODE1_INBRACKET = 1;
    private static final int MODE2_OUTBRACKET = 2;
    private static final VersionInfo NO_VERSION = VersionInfo.getInstance(0, 0, 0, 0);
    private static final int SETMODE0_NONE = 0;
    private static final int SETMODE1_UNICODESET = 1;
    private static final int SETMODE2_PROPERTYPAT = 2;
    private static final int SETMODE3_PREPARSED = 3;
    private static XSymbolTable XSYMBOL_TABLE = null;
    private volatile BMPSet bmpSet;
    private int[] buffer;
    private int len;
    private int[] list;
    private String pat;
    private int[] rangeList;
    private volatile UnicodeSetStringSpan stringSpan;
    SortedSet<String> strings;

    public enum ComparisonStyle {
        SHORTER_FIRST,
        LEXICOGRAPHIC,
        LONGER_FIRST
    }

    /* access modifiers changed from: private */
    public interface Filter {
        boolean contains(int i);
    }

    public enum SpanCondition {
        NOT_CONTAINED,
        CONTAINED,
        SIMPLE,
        CONDITION_COUNT
    }

    public static abstract class XSymbolTable implements SymbolTable {
        public boolean applyPropertyAlias(String str, String str2, UnicodeSet unicodeSet) {
            return false;
        }

        @Override // ohos.global.icu.text.SymbolTable
        public char[] lookup(String str) {
            return null;
        }

        @Override // ohos.global.icu.text.SymbolTable
        public UnicodeMatcher lookupMatcher(int i) {
            return null;
        }

        @Override // ohos.global.icu.text.SymbolTable
        public String parseReference(String str, ParsePosition parsePosition, int i) {
            return null;
        }
    }

    private static final int max(int i, int i2) {
        return i > i2 ? i : i2;
    }

    private int nextCapacity(int i) {
        if (i < 25) {
            return i + 25;
        }
        if (i <= 2500) {
            return i * 5;
        }
        int i2 = i * 2;
        if (i2 > 1114113) {
            return 1114113;
        }
        return i2;
    }

    public UnicodeSet() {
        this.strings = EMPTY_STRINGS;
        this.pat = null;
        this.list = new int[25];
        this.list[0] = 1114112;
        this.len = 1;
    }

    public UnicodeSet(UnicodeSet unicodeSet) {
        this.strings = EMPTY_STRINGS;
        this.pat = null;
        set(unicodeSet);
    }

    public UnicodeSet(int i, int i2) {
        this();
        add(i, i2);
    }

    public UnicodeSet(int... iArr) {
        this.strings = EMPTY_STRINGS;
        this.pat = null;
        if ((iArr.length & 1) == 0) {
            this.list = new int[(iArr.length + 1)];
            this.len = this.list.length;
            int i = -1;
            int i2 = 0;
            while (i2 < iArr.length) {
                int i3 = iArr[i2];
                if (i < i3) {
                    int[] iArr2 = this.list;
                    int i4 = i2 + 1;
                    iArr2[i2] = i3;
                    int i5 = iArr[i4] + 1;
                    if (i3 < i5) {
                        iArr2[i4] = i5;
                        i = i5;
                        i2 = i4 + 1;
                    } else {
                        throw new IllegalArgumentException("Must be monotonically increasing.");
                    }
                } else {
                    throw new IllegalArgumentException("Must be monotonically increasing.");
                }
            }
            this.list[i2] = 1114112;
            return;
        }
        throw new IllegalArgumentException("Must have even number of integers");
    }

    public UnicodeSet(String str) {
        this();
        applyPattern(str, null, null, 1);
    }

    public UnicodeSet(String str, boolean z) {
        this();
        applyPattern(str, null, null, z ? 1 : 0);
    }

    public UnicodeSet(String str, int i) {
        this();
        applyPattern(str, null, null, i);
    }

    public UnicodeSet(String str, ParsePosition parsePosition, SymbolTable symbolTable) {
        this();
        applyPattern(str, parsePosition, symbolTable, 1);
    }

    public UnicodeSet(String str, ParsePosition parsePosition, SymbolTable symbolTable, int i) {
        this();
        applyPattern(str, parsePosition, symbolTable, i);
    }

    @Override // java.lang.Object
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return new UnicodeSet(this);
    }

    public UnicodeSet set(int i, int i2) {
        checkFrozen();
        clear();
        complement(i, i2);
        return this;
    }

    public UnicodeSet set(UnicodeSet unicodeSet) {
        checkFrozen();
        this.list = Arrays.copyOf(unicodeSet.list, unicodeSet.len);
        this.len = unicodeSet.len;
        this.pat = unicodeSet.pat;
        if (unicodeSet.hasStrings()) {
            this.strings = new TreeSet((SortedSet) unicodeSet.strings);
        } else {
            this.strings = EMPTY_STRINGS;
        }
        return this;
    }

    public final UnicodeSet applyPattern(String str) {
        checkFrozen();
        return applyPattern(str, null, null, 1);
    }

    public UnicodeSet applyPattern(String str, boolean z) {
        checkFrozen();
        return applyPattern(str, null, null, z ? 1 : 0);
    }

    public UnicodeSet applyPattern(String str, int i) {
        checkFrozen();
        return applyPattern(str, null, null, i);
    }

    public static boolean resemblesPattern(String str, int i) {
        return (i + 1 < str.length() && str.charAt(i) == '[') || resemblesPropertyPattern(str, i);
    }

    private static void appendCodePoint(Appendable appendable, int i) {
        if (i <= 65535) {
            try {
                appendable.append((char) i);
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        } else {
            appendable.append(UTF16.getLeadSurrogate(i)).append(UTF16.getTrailSurrogate(i));
        }
    }

    private static void append(Appendable appendable, CharSequence charSequence) {
        try {
            appendable.append(charSequence);
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static <T extends Appendable> T _appendToPat(T t, String str, boolean z) {
        int i = 0;
        while (i < str.length()) {
            int codePointAt = str.codePointAt(i);
            _appendToPat(t, codePointAt, z);
            i += Character.charCount(codePointAt);
        }
        return t;
    }

    /* access modifiers changed from: private */
    public static <T extends Appendable> T _appendToPat(T t, int i, boolean z) {
        if (z) {
            try {
                if (Utility.isUnprintable(i) && Utility.escapeUnprintable(t, i)) {
                    return t;
                }
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        }
        if (!(i == 36 || i == 38 || i == 45 || i == 58 || i == 123 || i == 125)) {
            switch (i) {
                case 91:
                case 92:
                case 93:
                case 94:
                    break;
                default:
                    if (PatternProps.isWhiteSpace(i)) {
                        t.append(PatternTokenizer.BACK_SLASH);
                        break;
                    }
                    break;
            }
            appendCodePoint(t, i);
            return t;
        }
        t.append(PatternTokenizer.BACK_SLASH);
        appendCodePoint(t, i);
        return t;
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public String toPattern(boolean z) {
        String str = this.pat;
        if (str == null || z) {
            return ((StringBuilder) _toPattern(new StringBuilder(), z)).toString();
        }
        return str;
    }

    private <T extends Appendable> T _toPattern(T t, boolean z) {
        String str = this.pat;
        if (str == null) {
            return (T) appendNewPattern(t, z, true);
        }
        if (!z) {
            try {
                t.append(str);
                return t;
            } catch (IOException e) {
                throw new ICUUncheckedIOException(e);
            }
        } else {
            int i = 0;
            boolean z2 = false;
            while (i < this.pat.length()) {
                int codePointAt = this.pat.codePointAt(i);
                i += Character.charCount(codePointAt);
                if (Utility.isUnprintable(codePointAt)) {
                    Utility.escapeUnprintable(t, codePointAt);
                } else if (z2 || codePointAt != 92) {
                    if (z2) {
                        t.append(PatternTokenizer.BACK_SLASH);
                    }
                    appendCodePoint(t, codePointAt);
                } else {
                    z2 = true;
                }
                z2 = false;
            }
            if (z2) {
                t.append(PatternTokenizer.BACK_SLASH);
            }
            return t;
        }
    }

    public StringBuffer _generatePattern(StringBuffer stringBuffer, boolean z) {
        return _generatePattern(stringBuffer, z, true);
    }

    public StringBuffer _generatePattern(StringBuffer stringBuffer, boolean z, boolean z2) {
        return (StringBuffer) appendNewPattern(stringBuffer, z, z2);
    }

    private <T extends Appendable> T appendNewPattern(T t, boolean z, boolean z2) {
        try {
            t.append('[');
            int rangeCount = getRangeCount();
            if (rangeCount > 1 && getRangeStart(0) == 0 && getRangeEnd(rangeCount - 1) == 1114111) {
                t.append('^');
                for (int i = 1; i < rangeCount; i++) {
                    int rangeEnd = getRangeEnd(i - 1) + 1;
                    int rangeStart = getRangeStart(i) - 1;
                    _appendToPat(t, rangeEnd, z);
                    if (rangeEnd != rangeStart) {
                        if (rangeEnd + 1 != rangeStart) {
                            t.append(LocaleUtility.IETF_SEPARATOR);
                        }
                        _appendToPat(t, rangeStart, z);
                    }
                }
            } else {
                for (int i2 = 0; i2 < rangeCount; i2++) {
                    int rangeStart2 = getRangeStart(i2);
                    int rangeEnd2 = getRangeEnd(i2);
                    _appendToPat(t, rangeStart2, z);
                    if (rangeStart2 != rangeEnd2) {
                        if (rangeStart2 + 1 != rangeEnd2) {
                            t.append(LocaleUtility.IETF_SEPARATOR);
                        }
                        _appendToPat(t, rangeEnd2, z);
                    }
                }
            }
            if (z2 && hasStrings()) {
                for (String str : this.strings) {
                    t.append('{');
                    _appendToPat(t, str, z);
                    t.append('}');
                }
            }
            t.append(']');
            return t;
        } catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasStrings() {
        return !this.strings.isEmpty();
    }

    public int size() {
        int rangeCount = getRangeCount();
        int i = 0;
        for (int i2 = 0; i2 < rangeCount; i2++) {
            i += (getRangeEnd(i2) - getRangeStart(i2)) + 1;
        }
        return i + this.strings.size();
    }

    public boolean isEmpty() {
        return this.len == 1 && !hasStrings();
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public boolean matchesIndexValue(int i) {
        for (int i2 = 0; i2 < getRangeCount(); i2++) {
            int rangeStart = getRangeStart(i2);
            int rangeEnd = getRangeEnd(i2);
            if ((rangeStart & -256) == (rangeEnd & -256)) {
                if ((rangeStart & 255) <= i && i <= (rangeEnd & 255)) {
                    return true;
                }
            } else if ((rangeStart & 255) <= i || i <= (rangeEnd & 255)) {
                return true;
            }
        }
        if (hasStrings()) {
            for (String str : this.strings) {
                if ((UTF16.charAt(str, 0) & 255) == i) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // ohos.global.icu.text.UnicodeFilter, ohos.global.icu.text.UnicodeMatcher
    public int matches(Replaceable replaceable, int[] iArr, int i, boolean z) {
        if (iArr[0] != i) {
            if (hasStrings()) {
                boolean z2 = iArr[0] < i;
                char charAt = replaceable.charAt(iArr[0]);
                int i2 = 0;
                for (String str : this.strings) {
                    char charAt2 = str.charAt(z2 ? 0 : str.length() - 1);
                    if (z2 && charAt2 > charAt) {
                        break;
                    } else if (charAt2 == charAt) {
                        int matchRest = matchRest(replaceable, iArr[0], i, str);
                        if (z) {
                            if (matchRest == (z2 ? i - iArr[0] : iArr[0] - i)) {
                                return 1;
                            }
                        }
                        if (matchRest == str.length()) {
                            if (matchRest > i2) {
                                i2 = matchRest;
                            }
                            if (z2 && matchRest < i2) {
                                break;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                if (i2 != 0) {
                    int i3 = iArr[0];
                    if (!z2) {
                        i2 = -i2;
                    }
                    iArr[0] = i3 + i2;
                    return 2;
                }
            }
            return super.matches(replaceable, iArr, i, z);
        } else if (contains(65535)) {
            return z ? 1 : 2;
        } else {
            return 0;
        }
    }

    private static int matchRest(Replaceable replaceable, int i, int i2, String str) {
        int i3;
        int length = str.length();
        int i4 = 1;
        if (i < i2) {
            i3 = i2 - i;
            if (i3 > length) {
                i3 = length;
            }
            while (i4 < i3) {
                if (replaceable.charAt(i + i4) != str.charAt(i4)) {
                    return 0;
                }
                i4++;
            }
        } else {
            i3 = i - i2;
            if (i3 > length) {
                i3 = length;
            }
            int i5 = length - 1;
            while (i4 < i3) {
                if (replaceable.charAt(i - i4) != str.charAt(i5 - i4)) {
                    return 0;
                }
                i4++;
            }
        }
        return i3;
    }

    @Deprecated
    public int matchesAt(CharSequence charSequence, int i) {
        int i2 = -1;
        if (hasStrings()) {
            char charAt = charSequence.charAt(i);
            String str = null;
            Iterator<String> it = this.strings.iterator();
            while (true) {
                if (!it.hasNext()) {
                    while (true) {
                        int matchesAt = matchesAt(charSequence, i, str);
                        if (i2 > matchesAt) {
                            break;
                        } else if (!it.hasNext()) {
                            i2 = matchesAt;
                            break;
                        } else {
                            str = it.next();
                            i2 = matchesAt;
                        }
                    }
                } else {
                    str = it.next();
                    char charAt2 = str.charAt(0);
                    if (charAt2 >= charAt && charAt2 > charAt) {
                        break;
                    }
                }
            }
        }
        if (i2 < 2) {
            int charAt3 = UTF16.charAt(charSequence, i);
            if (contains(charAt3)) {
                i2 = UTF16.getCharCount(charAt3);
            }
        }
        return i + i2;
    }

    private static int matchesAt(CharSequence charSequence, int i, CharSequence charSequence2) {
        int length = charSequence2.length();
        if (charSequence.length() + i > length) {
            return -1;
        }
        int i2 = 0;
        while (i2 < length) {
            if (charSequence2.charAt(i2) != charSequence.charAt(i)) {
                return -1;
            }
            i2++;
            i++;
        }
        return i2;
    }

    @Override // ohos.global.icu.text.UnicodeMatcher
    public void addMatchSetTo(UnicodeSet unicodeSet) {
        unicodeSet.addAll(this);
    }

    public int indexOf(int i) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        }
        int i2 = 0;
        int i3 = 0;
        while (true) {
            int[] iArr = this.list;
            int i4 = i2 + 1;
            int i5 = iArr[i2];
            if (i < i5) {
                return -1;
            }
            int i6 = i4 + 1;
            int i7 = iArr[i4];
            if (i < i7) {
                return (i3 + i) - i5;
            }
            i3 += i7 - i5;
            i2 = i6;
        }
    }

    public int charAt(int i) {
        if (i < 0) {
            return -1;
        }
        int i2 = this.len & -2;
        int i3 = 0;
        while (i3 < i2) {
            int[] iArr = this.list;
            int i4 = i3 + 1;
            int i5 = iArr[i3];
            int i6 = i4 + 1;
            int i7 = iArr[i4] - i5;
            if (i < i7) {
                return i5 + i;
            }
            i -= i7;
            i3 = i6;
        }
        return -1;
    }

    public UnicodeSet add(int i, int i2) {
        checkFrozen();
        return add_unchecked(i, i2);
    }

    public UnicodeSet addAll(int i, int i2) {
        checkFrozen();
        return add_unchecked(i, i2);
    }

    private UnicodeSet add_unchecked(int i, int i2) {
        int i3;
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i2, 6));
        } else {
            if (i < i2) {
                int i4 = i2 + 1;
                int i5 = this.len;
                if ((i5 & 1) != 0) {
                    if (i5 == 1) {
                        i3 = -2;
                    } else {
                        i3 = this.list[i5 - 2];
                    }
                    if (i3 <= i) {
                        checkFrozen();
                        if (i3 == i) {
                            int[] iArr = this.list;
                            int i6 = this.len;
                            iArr[i6 - 2] = i4;
                            if (i4 == 1114112) {
                                this.len = i6 - 1;
                            }
                        } else {
                            int[] iArr2 = this.list;
                            int i7 = this.len;
                            iArr2[i7 - 1] = i;
                            if (i4 < 1114112) {
                                ensureCapacity(i7 + 2);
                                int[] iArr3 = this.list;
                                int i8 = this.len;
                                this.len = i8 + 1;
                                iArr3[i8] = i4;
                                int i9 = this.len;
                                this.len = i9 + 1;
                                iArr3[i9] = 1114112;
                            } else {
                                ensureCapacity(i7 + 1);
                                int[] iArr4 = this.list;
                                int i10 = this.len;
                                this.len = i10 + 1;
                                iArr4[i10] = 1114112;
                            }
                        }
                        this.pat = null;
                        return this;
                    }
                }
                add(range(i, i2), 2, 0);
            } else if (i == i2) {
                add(i);
            }
            return this;
        }
    }

    public final UnicodeSet add(int i) {
        checkFrozen();
        return add_unchecked(i);
    }

    private final UnicodeSet add_unchecked(int i) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        }
        int findCodePoint = findCodePoint(i);
        if ((findCodePoint & 1) != 0) {
            return this;
        }
        int[] iArr = this.list;
        if (i == iArr[findCodePoint] - 1) {
            iArr[findCodePoint] = i;
            if (i == 1114111) {
                ensureCapacity(this.len + 1);
                int[] iArr2 = this.list;
                int i2 = this.len;
                this.len = i2 + 1;
                iArr2[i2] = 1114112;
            }
            if (findCodePoint > 0) {
                int[] iArr3 = this.list;
                int i3 = findCodePoint - 1;
                if (i == iArr3[i3]) {
                    System.arraycopy(iArr3, findCodePoint + 1, iArr3, i3, (this.len - findCodePoint) - 1);
                    this.len -= 2;
                }
            }
        } else {
            if (findCodePoint > 0) {
                int i4 = findCodePoint - 1;
                if (i == iArr[i4]) {
                    iArr[i4] = iArr[i4] + 1;
                }
            }
            int i5 = this.len;
            int i6 = i5 + 2;
            int[] iArr4 = this.list;
            if (i6 > iArr4.length) {
                int[] iArr5 = new int[nextCapacity(i5 + 2)];
                if (findCodePoint != 0) {
                    System.arraycopy(this.list, 0, iArr5, 0, findCodePoint);
                }
                System.arraycopy(this.list, findCodePoint, iArr5, findCodePoint + 2, this.len - findCodePoint);
                this.list = iArr5;
            } else {
                System.arraycopy(iArr4, findCodePoint, iArr4, findCodePoint + 2, i5 - findCodePoint);
            }
            int[] iArr6 = this.list;
            iArr6[findCodePoint] = i;
            iArr6[findCodePoint + 1] = i + 1;
            this.len += 2;
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet add(CharSequence charSequence) {
        checkFrozen();
        int singleCP = getSingleCP(charSequence);
        if (singleCP < 0) {
            String charSequence2 = charSequence.toString();
            if (!this.strings.contains(charSequence2)) {
                addString(charSequence2);
                this.pat = null;
            }
        } else {
            add_unchecked(singleCP, singleCP);
        }
        return this;
    }

    private void addString(CharSequence charSequence) {
        if (this.strings == EMPTY_STRINGS) {
            this.strings = new TreeSet();
        }
        this.strings.add(charSequence.toString());
    }

    private static int getSingleCP(CharSequence charSequence) {
        if (charSequence.length() < 1) {
            throw new IllegalArgumentException("Can't use zero-length strings in UnicodeSet");
        } else if (charSequence.length() > 2) {
            return -1;
        } else {
            if (charSequence.length() == 1) {
                return charSequence.charAt(0);
            }
            int charAt = UTF16.charAt(charSequence, 0);
            if (charAt > 65535) {
                return charAt;
            }
            return -1;
        }
    }

    public final UnicodeSet addAll(CharSequence charSequence) {
        checkFrozen();
        int i = 0;
        while (i < charSequence.length()) {
            int charAt = UTF16.charAt(charSequence, i);
            add_unchecked(charAt, charAt);
            i += UTF16.getCharCount(charAt);
        }
        return this;
    }

    public final UnicodeSet retainAll(CharSequence charSequence) {
        return retainAll(fromAll(charSequence));
    }

    public final UnicodeSet complementAll(CharSequence charSequence) {
        return complementAll(fromAll(charSequence));
    }

    public final UnicodeSet removeAll(CharSequence charSequence) {
        return removeAll(fromAll(charSequence));
    }

    public final UnicodeSet removeAllStrings() {
        checkFrozen();
        if (hasStrings()) {
            this.strings.clear();
            this.pat = null;
        }
        return this;
    }

    public static UnicodeSet from(CharSequence charSequence) {
        return new UnicodeSet().add(charSequence);
    }

    public static UnicodeSet fromAll(CharSequence charSequence) {
        return new UnicodeSet().addAll(charSequence);
    }

    public UnicodeSet retain(int i, int i2) {
        checkFrozen();
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i2, 6));
        } else {
            if (i <= i2) {
                retain(range(i, i2), 2, 0);
            } else {
                clear();
            }
            return this;
        }
    }

    public final UnicodeSet retain(int i) {
        return retain(i, i);
    }

    public final UnicodeSet retain(CharSequence charSequence) {
        int singleCP = getSingleCP(charSequence);
        if (singleCP < 0) {
            checkFrozen();
            String charSequence2 = charSequence.toString();
            if (this.strings.contains(charSequence2) && size() == 1) {
                return this;
            }
            clear();
            addString(charSequence2);
            this.pat = null;
        } else {
            retain(singleCP, singleCP);
        }
        return this;
    }

    public UnicodeSet remove(int i, int i2) {
        checkFrozen();
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i2, 6));
        } else {
            if (i <= i2) {
                retain(range(i, i2), 2, 2);
            }
            return this;
        }
    }

    public final UnicodeSet remove(int i) {
        return remove(i, i);
    }

    public final UnicodeSet remove(CharSequence charSequence) {
        int singleCP = getSingleCP(charSequence);
        if (singleCP < 0) {
            checkFrozen();
            String charSequence2 = charSequence.toString();
            if (this.strings.contains(charSequence2)) {
                this.strings.remove(charSequence2);
                this.pat = null;
            }
        } else {
            remove(singleCP, singleCP);
        }
        return this;
    }

    public UnicodeSet complement(int i, int i2) {
        checkFrozen();
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i2, 6));
        } else {
            if (i <= i2) {
                xor(range(i, i2), 2, 0);
            }
            this.pat = null;
            return this;
        }
    }

    public final UnicodeSet complement(int i) {
        return complement(i, i);
    }

    public UnicodeSet complement() {
        checkFrozen();
        int[] iArr = this.list;
        if (iArr[0] == 0) {
            System.arraycopy(iArr, 1, iArr, 0, this.len - 1);
            this.len--;
        } else {
            ensureCapacity(this.len + 1);
            int[] iArr2 = this.list;
            System.arraycopy(iArr2, 0, iArr2, 1, this.len);
            this.list[0] = 0;
            this.len++;
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet complement(CharSequence charSequence) {
        checkFrozen();
        int singleCP = getSingleCP(charSequence);
        if (singleCP < 0) {
            String charSequence2 = charSequence.toString();
            if (this.strings.contains(charSequence2)) {
                this.strings.remove(charSequence2);
            } else {
                addString(charSequence2);
            }
            this.pat = null;
        } else {
            complement(singleCP, singleCP);
        }
        return this;
    }

    @Override // ohos.global.icu.text.UnicodeFilter
    public boolean contains(int i) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (this.bmpSet != null) {
            return this.bmpSet.contains(i);
        } else {
            if (this.stringSpan != null) {
                return this.stringSpan.contains(i);
            }
            return (findCodePoint(i) & 1) != 0;
        }
    }

    private final int findCodePoint(int i) {
        int[] iArr = this.list;
        int i2 = 0;
        if (i < iArr[0]) {
            return 0;
        }
        int i3 = this.len;
        if (i3 >= 2 && i >= iArr[i3 - 2]) {
            return i3 - 1;
        }
        int i4 = this.len - 1;
        while (true) {
            int i5 = (i2 + i4) >>> 1;
            if (i5 == i2) {
                return i4;
            }
            if (i < this.list[i5]) {
                i4 = i5;
            } else {
                i2 = i5;
            }
        }
    }

    public boolean contains(int i, int i2) {
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i2, 6));
        } else {
            int findCodePoint = findCodePoint(i);
            return (findCodePoint & 1) != 0 && i2 < this.list[findCodePoint];
        }
    }

    public final boolean contains(CharSequence charSequence) {
        int singleCP = getSingleCP(charSequence);
        if (singleCP < 0) {
            return this.strings.contains(charSequence.toString());
        }
        return contains(singleCP);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004f, code lost:
        return false;
     */
    public boolean containsAll(UnicodeSet unicodeSet) {
        int[] iArr = unicodeSet.list;
        int i = this.len - 1;
        int i2 = unicodeSet.len - 1;
        boolean z = true;
        boolean z2 = true;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (true) {
            if (z) {
                if (i3 < i) {
                    int[] iArr2 = this.list;
                    int i9 = i3 + 1;
                    int i10 = iArr2[i3];
                    int i11 = i9 + 1;
                    int i12 = iArr2[i9];
                    i5 = i10;
                    i3 = i11;
                    i7 = i12;
                } else if (!z2 || i4 < i2) {
                    return false;
                }
            }
            if (z2) {
                if (i4 >= i2) {
                    break;
                }
                int i13 = i4 + 1;
                int i14 = iArr[i4];
                i4 = i13 + 1;
                i8 = iArr[i13];
                i6 = i14;
            }
            if (i6 >= i7) {
                z = true;
                z2 = false;
            } else if (i6 < i5 || i8 > i7) {
                break;
            } else {
                z2 = true;
                z = false;
            }
        }
        if (!this.strings.containsAll(unicodeSet.strings)) {
            return false;
        }
        return true;
    }

    public boolean containsAll(String str) {
        int i = 0;
        while (i < str.length()) {
            int charAt = UTF16.charAt(str, i);
            if (contains(charAt)) {
                i += UTF16.getCharCount(charAt);
            } else if (!hasStrings()) {
                return false;
            } else {
                return containsAll(str, 0);
            }
        }
        return true;
    }

    private boolean containsAll(String str, int i) {
        if (i >= str.length()) {
            return true;
        }
        int charAt = UTF16.charAt(str, i);
        if (contains(charAt) && containsAll(str, UTF16.getCharCount(charAt) + i)) {
            return true;
        }
        for (String str2 : this.strings) {
            if (str.startsWith(str2, i) && containsAll(str, str2.length() + i)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public String getRegexEquivalent() {
        if (!hasStrings()) {
            return toString();
        }
        StringBuilder sb = new StringBuilder("(?:");
        appendNewPattern(sb, true, false);
        for (String str : this.strings) {
            sb.append('|');
            _appendToPat(sb, str, true);
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean containsNone(int i, int i2) {
        int[] iArr;
        if (i < 0 || i > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i, 6));
        } else if (i2 < 0 || i2 > 1114111) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex((long) i2, 6));
        } else {
            int i3 = -1;
            do {
                iArr = this.list;
                i3++;
            } while (i >= iArr[i3]);
            if ((i3 & 1) != 0 || i2 >= iArr[i3]) {
                return false;
            }
            return true;
        }
    }

    public boolean containsNone(UnicodeSet unicodeSet) {
        int[] iArr = unicodeSet.list;
        int i = this.len - 1;
        int i2 = unicodeSet.len - 1;
        boolean z = true;
        boolean z2 = true;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        while (true) {
            if (z) {
                if (i3 >= i) {
                    break;
                }
                int[] iArr2 = this.list;
                int i9 = i3 + 1;
                int i10 = iArr2[i3];
                int i11 = i9 + 1;
                int i12 = iArr2[i9];
                i5 = i10;
                i3 = i11;
                i7 = i12;
            }
            if (z2) {
                if (i4 >= i2) {
                    break;
                }
                int i13 = i4 + 1;
                int i14 = iArr[i4];
                i4 = i13 + 1;
                i8 = iArr[i13];
                i6 = i14;
            }
            if (i6 >= i7) {
                z = true;
                z2 = false;
            } else if (i5 < i8) {
                return false;
            } else {
                z2 = true;
                z = false;
            }
        }
        if (!SortedSetRelation.hasRelation(this.strings, 5, unicodeSet.strings)) {
            return false;
        }
        return true;
    }

    public boolean containsNone(CharSequence charSequence) {
        return span(charSequence, SpanCondition.NOT_CONTAINED) == charSequence.length();
    }

    public final boolean containsSome(int i, int i2) {
        return !containsNone(i, i2);
    }

    public final boolean containsSome(UnicodeSet unicodeSet) {
        return !containsNone(unicodeSet);
    }

    public final boolean containsSome(CharSequence charSequence) {
        return !containsNone(charSequence);
    }

    public UnicodeSet addAll(UnicodeSet unicodeSet) {
        checkFrozen();
        add(unicodeSet.list, unicodeSet.len, 0);
        if (unicodeSet.hasStrings()) {
            SortedSet<String> sortedSet = this.strings;
            if (sortedSet == EMPTY_STRINGS) {
                this.strings = new TreeSet((SortedSet) unicodeSet.strings);
            } else {
                sortedSet.addAll(unicodeSet.strings);
            }
        }
        return this;
    }

    public UnicodeSet retainAll(UnicodeSet unicodeSet) {
        checkFrozen();
        retain(unicodeSet.list, unicodeSet.len, 0);
        if (hasStrings()) {
            if (!unicodeSet.hasStrings()) {
                this.strings.clear();
            } else {
                this.strings.retainAll(unicodeSet.strings);
            }
        }
        return this;
    }

    public UnicodeSet removeAll(UnicodeSet unicodeSet) {
        checkFrozen();
        retain(unicodeSet.list, unicodeSet.len, 2);
        if (hasStrings() && unicodeSet.hasStrings()) {
            this.strings.removeAll(unicodeSet.strings);
        }
        return this;
    }

    public UnicodeSet complementAll(UnicodeSet unicodeSet) {
        checkFrozen();
        xor(unicodeSet.list, unicodeSet.len, 0);
        if (unicodeSet.hasStrings()) {
            SortedSet<String> sortedSet = this.strings;
            if (sortedSet == EMPTY_STRINGS) {
                this.strings = new TreeSet((SortedSet) unicodeSet.strings);
            } else {
                SortedSetRelation.doOperation(sortedSet, 5, unicodeSet.strings);
            }
        }
        return this;
    }

    public UnicodeSet clear() {
        checkFrozen();
        this.list[0] = 1114112;
        this.len = 1;
        this.pat = null;
        if (hasStrings()) {
            this.strings.clear();
        }
        return this;
    }

    public int getRangeCount() {
        return this.len / 2;
    }

    public int getRangeStart(int i) {
        return this.list[i * 2];
    }

    public int getRangeEnd(int i) {
        return this.list[(i * 2) + 1] - 1;
    }

    public UnicodeSet compact() {
        checkFrozen();
        int i = this.len;
        int i2 = i + 7;
        int[] iArr = this.list;
        if (i2 < iArr.length) {
            this.list = Arrays.copyOf(iArr, i);
        }
        this.rangeList = null;
        this.buffer = null;
        SortedSet<String> sortedSet = this.strings;
        if (sortedSet != EMPTY_STRINGS && sortedSet.isEmpty()) {
            this.strings = EMPTY_STRINGS;
        }
        return this;
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        try {
            UnicodeSet unicodeSet = (UnicodeSet) obj;
            if (this.len != unicodeSet.len) {
                return false;
            }
            for (int i = 0; i < this.len; i++) {
                if (this.list[i] != unicodeSet.list[i]) {
                    return false;
                }
            }
            return this.strings.equals(unicodeSet.strings);
        } catch (Exception unused) {
            return false;
        }
    }

    @Override // java.lang.Object
    public int hashCode() {
        int i = this.len;
        for (int i2 = 0; i2 < this.len; i2++) {
            i = (i * 1000003) + this.list[i2];
        }
        return i;
    }

    @Override // java.lang.Object
    public String toString() {
        return toPattern(true);
    }

    @Deprecated
    public UnicodeSet applyPattern(String str, ParsePosition parsePosition, SymbolTable symbolTable, int i) {
        boolean z = parsePosition == null;
        if (z) {
            parsePosition = new ParsePosition(0);
        }
        StringBuilder sb = new StringBuilder();
        RuleCharacterIterator ruleCharacterIterator = new RuleCharacterIterator(str, symbolTable, parsePosition);
        applyPattern(ruleCharacterIterator, symbolTable, sb, i, 0);
        if (ruleCharacterIterator.inVariable()) {
            syntaxError(ruleCharacterIterator, "Extra chars in variable value");
        }
        this.pat = sb.toString();
        if (z) {
            int index = parsePosition.getIndex();
            if ((i & 1) != 0) {
                index = PatternProps.skipWhiteSpace(str, index);
            }
            if (index != str.length()) {
                throw new IllegalArgumentException("Parse of \"" + str + "\" failed at " + index);
            }
        }
        return this;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:217:0x0034 */
    /* JADX WARNING: Code restructure failed: missing block: B:201:0x0392, code lost:
        r4 = r7;
        r10 = false;
        r23 = r20;
     */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x0336  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x0376  */
    private void applyPattern(RuleCharacterIterator ruleCharacterIterator, SymbolTable symbolTable, Appendable appendable, int i, int i2) {
        boolean z;
        boolean z2;
        int i3;
        boolean z3;
        int i4;
        Object obj;
        int i5;
        UnicodeSet unicodeSet;
        int i6;
        String str;
        StringBuilder sb;
        int i7;
        Object obj2;
        int i8;
        boolean z4;
        char c;
        UnicodeSet unicodeSet2;
        Object obj3;
        UnicodeSet unicodeSet3;
        int i9;
        char c2;
        UnicodeMatcher lookupMatcher;
        int i10 = i2;
        if (i10 > 100) {
            syntaxError(ruleCharacterIterator, "Pattern nested too deeply");
        }
        int i11 = (i & 1) != 0 ? 7 : 3;
        StringBuilder sb2 = new StringBuilder();
        clear();
        int i12 = 2;
        int i13 = true;
        int i14 = 0;
        Object obj4 = null;
        int i15 = 0;
        int i16 = 0;
        char c3 = 0;
        UnicodeSet unicodeSet4 = null;
        String str2 = null;
        StringBuilder sb3 = null;
        boolean z5 = false;
        boolean z6 = false;
        while (true) {
            if (i14 == i12 || ruleCharacterIterator.atEnd()) {
                break;
            }
            if (resemblesPropertyPattern(ruleCharacterIterator, i11)) {
                obj = obj4;
                z = z6;
                unicodeSet = null;
                i5 = 0;
                z3 = false;
                i4 = i14;
                i6 = 2;
            } else {
                Object pos = ruleCharacterIterator.getPos(obj4);
                i5 = ruleCharacterIterator.next(i11);
                z3 = ruleCharacterIterator.isEscaped();
                if (i5 != 91 || z3) {
                    if (!(symbolTable == null || (lookupMatcher = symbolTable.lookupMatcher(i5)) == null)) {
                        try {
                            UnicodeSet unicodeSet5 = (UnicodeSet) lookupMatcher;
                            z = z6;
                            i4 = i14;
                            i6 = 3;
                            obj = pos;
                            unicodeSet = unicodeSet5;
                        } catch (ClassCastException unused) {
                            syntaxError(ruleCharacterIterator, "Syntax error");
                        }
                    }
                    obj = pos;
                    z = z6;
                    unicodeSet = null;
                    i4 = i14;
                    i6 = 0;
                } else if (i14 == i13) {
                    ruleCharacterIterator.setPos(pos);
                    obj = pos;
                    z = z6;
                    unicodeSet = null;
                    i4 = i14;
                    i6 = i13;
                } else {
                    sb2.append('[');
                    Object pos2 = ruleCharacterIterator.getPos(pos);
                    int next = ruleCharacterIterator.next(i11);
                    boolean isEscaped = ruleCharacterIterator.isEscaped();
                    if (next != 94 || isEscaped) {
                        i5 = next;
                    } else {
                        sb2.append('^');
                        pos2 = ruleCharacterIterator.getPos(pos2);
                        int next2 = ruleCharacterIterator.next(i11);
                        ruleCharacterIterator.isEscaped();
                        i5 = next2;
                        z6 = i13;
                    }
                    obj4 = pos2;
                    if (i5 == 45) {
                        obj = obj4;
                        z3 = i13;
                        z = z6;
                        i6 = 0;
                        unicodeSet = null;
                        i4 = z3;
                    } else {
                        ruleCharacterIterator.setPos(obj4);
                        i14 = i13;
                        i12 = 2;
                    }
                }
            }
            if (i6 != 0) {
                if (i15 == i13) {
                    if (c3 != 0) {
                        syntaxError(ruleCharacterIterator, "Char expected after operator");
                    }
                    add_unchecked(i16, i16);
                    _appendToPat(sb2, i16, false);
                    c = LocaleUtility.IETF_SEPARATOR;
                    c3 = 0;
                } else {
                    c = LocaleUtility.IETF_SEPARATOR;
                }
                if (c3 == c || c3 == '&') {
                    sb2.append(c3);
                }
                if (unicodeSet == null) {
                    if (unicodeSet4 == null) {
                        unicodeSet4 = new UnicodeSet();
                    }
                    unicodeSet2 = unicodeSet4;
                } else {
                    unicodeSet2 = unicodeSet;
                }
                if (i6 != i13) {
                    if (i6 == 2) {
                        ruleCharacterIterator.skipIgnored(i11);
                        unicodeSet2.applyPropertyPattern(ruleCharacterIterator, sb2, symbolTable);
                    } else if (i6 != 3) {
                        unicodeSet3 = unicodeSet2;
                        i9 = i16;
                        c2 = c;
                        obj3 = obj;
                    } else {
                        unicodeSet2._toPattern(sb2, false);
                    }
                    unicodeSet3 = unicodeSet2;
                    i9 = i16;
                    obj3 = obj;
                    c2 = LocaleUtility.IETF_SEPARATOR;
                } else {
                    int i17 = i10 + 1;
                    unicodeSet3 = unicodeSet2;
                    i9 = i16;
                    obj3 = obj;
                    c2 = '-';
                    unicodeSet2.applyPattern(ruleCharacterIterator, symbolTable, sb2, i, i17);
                }
                if (i4 == 0) {
                    set(unicodeSet3);
                    i14 = 2;
                    i3 = 2;
                    z2 = false;
                    z5 = true;
                    break;
                }
                if (c3 == 0) {
                    addAll(unicodeSet3);
                } else if (c3 == '&') {
                    retainAll(unicodeSet3);
                } else if (c3 == c2) {
                    removeAll(unicodeSet3);
                }
                i16 = i9;
                i14 = i4;
                z6 = z;
                obj4 = obj3;
                i15 = 2;
                i13 = true;
                i12 = 2;
                c3 = 0;
                z5 = true;
            } else {
                if (i4 == 0) {
                    syntaxError(ruleCharacterIterator, "Missing '['");
                }
                if (!z3) {
                    if (i5 == 36) {
                        str = str2;
                        sb = sb3;
                        i7 = 2;
                        Object pos3 = ruleCharacterIterator.getPos(obj);
                        int next3 = ruleCharacterIterator.next(i11);
                        boolean z7 = next3 == 93 && !ruleCharacterIterator.isEscaped();
                        if (symbolTable == null && !z7) {
                            ruleCharacterIterator.setPos(pos3);
                            obj4 = pos3;
                            i5 = 36;
                            if (i15 == 0) {
                            }
                            i10 = i2;
                            i12 = i7;
                            i16 = i5;
                            sb3 = sb;
                            i14 = i4;
                            z6 = z;
                            i13 = true;
                        } else if (!z7 || c3 != 0) {
                            syntaxError(ruleCharacterIterator, "Unquoted '$'");
                            obj4 = pos3;
                            i5 = next3;
                            if (i15 == 0) {
                            }
                            i10 = i2;
                            i12 = i7;
                            i16 = i5;
                            sb3 = sb;
                            i14 = i4;
                            z6 = z;
                            i13 = true;
                        } else {
                            if (i15 == 1) {
                                add_unchecked(i16, i16);
                                _appendToPat(sb2, i16, false);
                            }
                            add_unchecked(65535);
                            sb2.append(SymbolTable.SYMBOL_REF);
                            sb2.append(']');
                            i14 = 2;
                            obj4 = pos3;
                            sb3 = sb;
                            str2 = str;
                            z6 = z;
                            i13 = true;
                            z5 = true;
                            i12 = 2;
                        }
                    } else if (i5 != 38) {
                        if (i5 == 45) {
                            str = str2;
                            sb = sb3;
                            if (c3 == 0) {
                                if (i15 == 0 && str == null) {
                                    add_unchecked(i5, i5);
                                    int next4 = ruleCharacterIterator.next(i11);
                                    boolean isEscaped2 = ruleCharacterIterator.isEscaped();
                                    if (next4 != 93 || isEscaped2) {
                                        i5 = next4;
                                    } else {
                                        sb2.append("-]");
                                        sb3 = sb;
                                        str2 = str;
                                    }
                                } else {
                                    c3 = (char) i5;
                                    sb3 = sb;
                                    str2 = str;
                                    i16 = i16;
                                    i14 = i4;
                                    z6 = z;
                                    obj4 = obj;
                                    i13 = true;
                                    i12 = 2;
                                }
                            }
                            syntaxError(ruleCharacterIterator, "'-' not after char, string, or set");
                            obj4 = obj;
                            i7 = 2;
                            if (i15 == 0) {
                                if (c3 == '-' && str != null) {
                                    syntaxError(ruleCharacterIterator, "Invalid range");
                                }
                                i15 = 1;
                                str2 = null;
                            } else if (i15 != 1) {
                                if (i15 != i7) {
                                    str2 = str;
                                    i5 = i16;
                                } else {
                                    if (c3 != 0) {
                                        syntaxError(ruleCharacterIterator, "Set expected after operator");
                                    }
                                    str2 = str;
                                    i15 = 1;
                                }
                            } else if (c3 == '-') {
                                if (str != null) {
                                    syntaxError(ruleCharacterIterator, "Invalid range");
                                }
                                if (i16 >= i5) {
                                    syntaxError(ruleCharacterIterator, "Invalid range");
                                }
                                add_unchecked(i16, i5);
                                _appendToPat(sb2, i16, false);
                                sb2.append(c3);
                                _appendToPat(sb2, i5, false);
                                str2 = str;
                                i15 = 0;
                                c3 = 0;
                                i5 = i16;
                            } else {
                                add_unchecked(i16, i16);
                                _appendToPat(sb2, i16, false);
                                str2 = str;
                            }
                            i10 = i2;
                            i12 = i7;
                            i16 = i5;
                            sb3 = sb;
                            i14 = i4;
                            z6 = z;
                            i13 = true;
                        } else if (i5 == 123) {
                            if (!(c3 == 0 || c3 == '-')) {
                                syntaxError(ruleCharacterIterator, "Missing operand after operator");
                            }
                            if (i15 == 1) {
                                add_unchecked(i16, i16);
                                i8 = 0;
                                _appendToPat(sb2, i16, false);
                            } else {
                                i8 = 0;
                            }
                            StringBuilder sb4 = sb3;
                            if (sb4 == null) {
                                sb4 = new StringBuilder();
                            } else {
                                sb4.setLength(i8);
                            }
                            while (true) {
                                if (ruleCharacterIterator.atEnd()) {
                                    z4 = false;
                                    break;
                                }
                                int next5 = ruleCharacterIterator.next(i11);
                                boolean isEscaped3 = ruleCharacterIterator.isEscaped();
                                if (next5 == 125 && !isEscaped3) {
                                    z4 = true;
                                    break;
                                }
                                appendCodePoint(sb4, next5);
                            }
                            if (sb4.length() < 1 || !z4) {
                                syntaxError(ruleCharacterIterator, "Invalid multicharacter string");
                            }
                            String sb5 = sb4.toString();
                            if (c3 == '-') {
                                int singleCodePoint = CharSequences.getSingleCodePoint(str2 == null ? "" : str2);
                                int singleCodePoint2 = CharSequences.getSingleCodePoint(sb5);
                                if (singleCodePoint == Integer.MAX_VALUE || singleCodePoint2 == Integer.MAX_VALUE) {
                                    if (this.strings == EMPTY_STRINGS) {
                                        this.strings = new TreeSet();
                                    }
                                    try {
                                        StringRange.expand(str2, sb5, true, this.strings);
                                    } catch (Exception e) {
                                        syntaxError(ruleCharacterIterator, e.getMessage());
                                    }
                                } else {
                                    add(singleCodePoint, singleCodePoint2);
                                }
                                c3 = 0;
                                str2 = null;
                            } else {
                                add(sb5);
                                str2 = sb5;
                            }
                            sb2.append('{');
                            _appendToPat(sb2, sb5, false);
                            sb2.append('}');
                            sb3 = sb4;
                            i16 = i16;
                            i14 = i4;
                            z6 = z;
                            obj4 = obj;
                            i15 = 0;
                            i13 = true;
                            i12 = 2;
                        } else if (i5 == 93) {
                            if (i15 == 1) {
                                add_unchecked(i16, i16);
                                _appendToPat(sb2, i16, false);
                            }
                            if (c3 == '-') {
                                add_unchecked(c3, c3);
                                sb2.append(c3);
                            } else if (c3 == '&') {
                                syntaxError(ruleCharacterIterator, "Trailing '&'");
                            }
                            sb2.append(']');
                        } else if (i5 == 94) {
                            syntaxError(ruleCharacterIterator, "'^' not after '['");
                        }
                        i16 = i16;
                        z6 = z;
                        obj4 = obj;
                        i14 = 2;
                        i13 = true;
                        i12 = 2;
                    } else {
                        str = str2;
                        sb = sb3;
                        i7 = 2;
                        if (i15 == 2 && c3 == 0) {
                            c3 = (char) i5;
                            sb3 = sb;
                            str2 = str;
                            i14 = i4;
                            z6 = z;
                            obj4 = obj;
                            i13 = true;
                            i12 = 2;
                        } else {
                            syntaxError(ruleCharacterIterator, "'&' not after set");
                            obj2 = obj;
                            obj4 = obj2;
                            if (i15 == 0) {
                            }
                            i10 = i2;
                            i12 = i7;
                            i16 = i5;
                            sb3 = sb;
                            i14 = i4;
                            z6 = z;
                            i13 = true;
                        }
                    }
                    i16 = i16;
                }
                str = str2;
                sb = sb3;
                obj2 = obj;
                i7 = 2;
                obj4 = obj2;
                if (i15 == 0) {
                }
                i10 = i2;
                i12 = i7;
                i16 = i5;
                sb3 = sb;
                i14 = i4;
                z6 = z;
                i13 = true;
            }
            i10 = i2;
        }
        if (i14 != i3) {
            syntaxError(ruleCharacterIterator, "Missing ']'");
        }
        ruleCharacterIterator.skipIgnored(i11);
        if ((i & 2) != 0) {
            closeOver(i3);
        }
        if (z) {
            complement();
        }
        if (z5) {
            append(appendable, sb2.toString());
        } else {
            appendNewPattern(appendable, z2, true);
        }
    }

    private static void syntaxError(RuleCharacterIterator ruleCharacterIterator, String str) {
        throw new IllegalArgumentException("Error: " + str + " at \"" + Utility.escape(ruleCharacterIterator.toString()) + '\"');
    }

    public <T extends Collection<String>> T addAllTo(T t) {
        return (T) addAllTo(this, t);
    }

    public String[] addAllTo(String[] strArr) {
        return (String[]) addAllTo(this, strArr);
    }

    public static String[] toArray(UnicodeSet unicodeSet) {
        return (String[]) addAllTo(unicodeSet, new String[unicodeSet.size()]);
    }

    public UnicodeSet add(Iterable<?> iterable) {
        return addAll(iterable);
    }

    public UnicodeSet addAll(Iterable<?> iterable) {
        checkFrozen();
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()) {
            add(it.next().toString());
        }
        return this;
    }

    private void ensureCapacity(int i) {
        if (i > 1114113) {
            i = 1114113;
        }
        if (i > this.list.length) {
            int[] iArr = new int[nextCapacity(i)];
            System.arraycopy(this.list, 0, iArr, 0, this.len);
            this.list = iArr;
        }
    }

    private void ensureBufferCapacity(int i) {
        if (i > 1114113) {
            i = 1114113;
        }
        int[] iArr = this.buffer;
        if (iArr == null || i > iArr.length) {
            this.buffer = new int[nextCapacity(i)];
        }
    }

    private int[] range(int i, int i2) {
        int[] iArr = this.rangeList;
        if (iArr == null) {
            this.rangeList = new int[]{i, i2 + 1, 1114112};
        } else {
            iArr[0] = i;
            iArr[1] = i2 + 1;
        }
        return this.rangeList;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0024  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    private UnicodeSet xor(int[] iArr, int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        ensureBufferCapacity(this.len + i);
        int i7 = 0;
        int i8 = this.list[0];
        int i9 = 1;
        if (i2 != 1 && i2 != 2) {
            i6 = iArr[0];
        } else if (iArr[0] == 0) {
            i6 = iArr[1];
        } else {
            i3 = 0;
            i4 = 0;
            while (true) {
                if (i8 >= i7) {
                    i5 = i3 + 1;
                    this.buffer[i3] = i8;
                    i8 = this.list[i9];
                    i9++;
                } else if (i7 < i8) {
                    i5 = i3 + 1;
                    this.buffer[i3] = i7;
                    i7 = iArr[i4];
                    i4++;
                } else if (i8 != 1114112) {
                    i8 = this.list[i9];
                    i9++;
                    i7 = iArr[i4];
                    i4++;
                } else {
                    int[] iArr2 = this.buffer;
                    iArr2[i3] = 1114112;
                    this.len = i3 + 1;
                    int[] iArr3 = this.list;
                    this.list = iArr2;
                    this.buffer = iArr3;
                    this.pat = null;
                    return this;
                }
                i3 = i5;
            }
        }
        i4 = 1;
        i7 = i6;
        i3 = 0;
        while (true) {
            if (i8 >= i7) {
            }
            i3 = i5;
        }
    }

    private UnicodeSet add(int[] iArr, int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        ensureBufferCapacity(this.len + i);
        int i11 = 0;
        int i12 = this.list[0];
        int i13 = iArr[0];
        int i14 = 1;
        int i15 = 1;
        while (true) {
            if (i2 != 0) {
                if (i2 != 1) {
                    if (i2 != 2) {
                        if (i2 == 3) {
                            if (i13 <= i12) {
                                if (i12 == 1114112) {
                                    break;
                                }
                                i10 = i11 + 1;
                                this.buffer[i11] = i12;
                            } else if (i13 == 1114112) {
                                break;
                            } else {
                                i10 = i11 + 1;
                                this.buffer[i11] = i13;
                            }
                            int i16 = i14 + 1;
                            i12 = this.list[i14];
                            int i17 = iArr[i15];
                            i2 = (i2 ^ 1) ^ 2;
                            i15++;
                            i13 = i17;
                            i14 = i16;
                            i11 = i10;
                        } else {
                            continue;
                        }
                    } else if (i13 < i12) {
                        i9 = i11 + 1;
                        this.buffer[i11] = i13;
                        i13 = iArr[i15];
                        i2 ^= 2;
                        i15++;
                    } else if (i12 < i13) {
                        i12 = this.list[i14];
                        i2 ^= 1;
                        i14++;
                    } else if (i12 == 1114112) {
                        break;
                    } else {
                        i8 = i14 + 1;
                        i12 = this.list[i14];
                        i5 = i2 ^ 1;
                        i7 = i15 + 1;
                        i6 = iArr[i15];
                        i2 = i5 ^ 2;
                        i14 = i8;
                        i13 = i6;
                        i15 = i7;
                    }
                } else if (i12 < i13) {
                    i9 = i11 + 1;
                    this.buffer[i11] = i12;
                    i12 = this.list[i14];
                    i2 ^= 1;
                    i14++;
                } else if (i13 < i12) {
                    i3 = i15 + 1;
                    i4 = iArr[i15];
                    i2 ^= 2;
                } else if (i12 == 1114112) {
                    break;
                } else {
                    i8 = i14 + 1;
                    i12 = this.list[i14];
                    i5 = i2 ^ 1;
                    i7 = i15 + 1;
                    i6 = iArr[i15];
                    i2 = i5 ^ 2;
                    i14 = i8;
                    i13 = i6;
                    i15 = i7;
                }
                i11 = i9;
            } else if (i12 < i13) {
                if (i11 > 0) {
                    int[] iArr2 = this.buffer;
                    if (i12 <= iArr2[i11 - 1]) {
                        i11--;
                        i12 = max(this.list[i14], iArr2[i11]);
                        i14++;
                        i2 ^= 1;
                    }
                }
                this.buffer[i11] = i12;
                i12 = this.list[i14];
                i11++;
                i14++;
                i2 ^= 1;
            } else if (i13 < i12) {
                if (i11 > 0) {
                    int[] iArr3 = this.buffer;
                    if (i13 <= iArr3[i11 - 1]) {
                        i11--;
                        i13 = max(iArr[i15], iArr3[i11]);
                        i15++;
                        i2 ^= 2;
                    }
                }
                this.buffer[i11] = i13;
                i13 = iArr[i15];
                i11++;
                i15++;
                i2 ^= 2;
            } else if (i12 == 1114112) {
                break;
            } else {
                if (i11 > 0) {
                    int[] iArr4 = this.buffer;
                    if (i12 <= iArr4[i11 - 1]) {
                        i11--;
                        i12 = max(this.list[i14], iArr4[i11]);
                        i14++;
                        i3 = i15 + 1;
                        i4 = iArr[i15];
                        i2 = (i2 ^ 1) ^ 2;
                    }
                }
                this.buffer[i11] = i12;
                i12 = this.list[i14];
                i11++;
                i14++;
                i3 = i15 + 1;
                i4 = iArr[i15];
                i2 = (i2 ^ 1) ^ 2;
            }
            i15 = i3;
            i13 = i4;
        }
        int[] iArr5 = this.buffer;
        iArr5[i11] = 1114112;
        this.len = i11 + 1;
        int[] iArr6 = this.list;
        this.list = iArr5;
        this.buffer = iArr6;
        this.pat = null;
        return this;
    }

    private UnicodeSet retain(int[] iArr, int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        int i10;
        int i11;
        int i12;
        int i13;
        int i14;
        int i15;
        ensureBufferCapacity(this.len + i);
        int i16 = this.list[0];
        int i17 = iArr[0];
        int i18 = 0;
        int i19 = 1;
        int i20 = 1;
        while (true) {
            if (i2 != 0) {
                if (i2 != 1) {
                    if (i2 != 2) {
                        if (i2 != 3) {
                            continue;
                        } else if (i16 < i17) {
                            i14 = i18 + 1;
                            this.buffer[i18] = i16;
                            i15 = i19 + 1;
                            i16 = this.list[i19];
                        } else if (i17 < i16) {
                            i13 = i18 + 1;
                            this.buffer[i18] = i17;
                            i10 = i20 + 1;
                            i11 = iArr[i20];
                        } else if (i16 == 1114112) {
                            break;
                        } else {
                            i8 = i18 + 1;
                            this.buffer[i18] = i16;
                            i4 = i19 + 1;
                            i16 = this.list[i19];
                            i7 = i2 ^ 1;
                            i5 = i20 + 1;
                            i3 = iArr[i20];
                            i2 = i7 ^ 2;
                            i18 = i8;
                            i6 = i3;
                            i20 = i5;
                            i19 = i4;
                            i17 = i6;
                        }
                    } else if (i17 < i16) {
                        i10 = i20 + 1;
                        i11 = iArr[i20];
                    } else if (i16 < i17) {
                        i14 = i18 + 1;
                        this.buffer[i18] = i16;
                        i15 = i19 + 1;
                        i16 = this.list[i19];
                    } else if (i16 == 1114112) {
                        break;
                    } else {
                        i4 = i19 + 1;
                        i16 = this.list[i19];
                        i12 = i2 ^ 1;
                        i5 = i20 + 1;
                        i3 = iArr[i20];
                        i2 = i12 ^ 2;
                        i6 = i3;
                        i20 = i5;
                        i19 = i4;
                        i17 = i6;
                    }
                    i2 ^= 1;
                    i19 = i15;
                    i18 = i14;
                } else if (i16 < i17) {
                    i9 = i19 + 1;
                    i16 = this.list[i19];
                    i2 ^= 1;
                    i19 = i9;
                } else if (i17 < i16) {
                    i13 = i18 + 1;
                    this.buffer[i18] = i17;
                    i10 = i20 + 1;
                    i11 = iArr[i20];
                } else if (i16 == 1114112) {
                    break;
                } else {
                    i4 = i19 + 1;
                    i16 = this.list[i19];
                    i12 = i2 ^ 1;
                    i5 = i20 + 1;
                    i3 = iArr[i20];
                    i2 = i12 ^ 2;
                    i6 = i3;
                    i20 = i5;
                    i19 = i4;
                    i17 = i6;
                }
                i2 ^= 2;
                i18 = i13;
                i6 = i11;
                i20 = i10;
                i17 = i6;
            } else if (i16 < i17) {
                i9 = i19 + 1;
                i16 = this.list[i19];
                i2 ^= 1;
                i19 = i9;
            } else if (i17 < i16) {
                i10 = i20 + 1;
                i11 = iArr[i20];
            } else if (i16 == 1114112) {
                break;
            } else {
                i8 = i18 + 1;
                this.buffer[i18] = i16;
                i4 = i19 + 1;
                i16 = this.list[i19];
                i7 = i2 ^ 1;
                i5 = i20 + 1;
                i3 = iArr[i20];
                i2 = i7 ^ 2;
                i18 = i8;
                i6 = i3;
                i20 = i5;
                i19 = i4;
                i17 = i6;
            }
            i2 ^= 2;
            i6 = i11;
            i20 = i10;
            i17 = i6;
        }
        int[] iArr2 = this.buffer;
        iArr2[i18] = 1114112;
        this.len = i18 + 1;
        int[] iArr3 = this.list;
        this.list = iArr2;
        this.buffer = iArr3;
        this.pat = null;
        return this;
    }

    /* access modifiers changed from: private */
    public static final class NumericValueFilter implements Filter {
        double value;

        NumericValueFilter(double d) {
            this.value = d;
        }

        @Override // ohos.global.icu.text.UnicodeSet.Filter
        public boolean contains(int i) {
            return UCharacter.getUnicodeNumericValue(i) == this.value;
        }
    }

    /* access modifiers changed from: private */
    public static final class GeneralCategoryMaskFilter implements Filter {
        int mask;

        GeneralCategoryMaskFilter(int i) {
            this.mask = i;
        }

        @Override // ohos.global.icu.text.UnicodeSet.Filter
        public boolean contains(int i) {
            return (this.mask & (1 << UCharacter.getType(i))) != 0;
        }
    }

    /* access modifiers changed from: private */
    public static final class IntPropertyFilter implements Filter {
        int prop;
        int value;

        IntPropertyFilter(int i, int i2) {
            this.prop = i;
            this.value = i2;
        }

        @Override // ohos.global.icu.text.UnicodeSet.Filter
        public boolean contains(int i) {
            return UCharacter.getIntPropertyValue(i, this.prop) == this.value;
        }
    }

    /* access modifiers changed from: private */
    public static final class ScriptExtensionsFilter implements Filter {
        int script;

        ScriptExtensionsFilter(int i) {
            this.script = i;
        }

        @Override // ohos.global.icu.text.UnicodeSet.Filter
        public boolean contains(int i) {
            return UScript.hasScript(i, this.script);
        }
    }

    /* access modifiers changed from: private */
    public static final class VersionFilter implements Filter {
        VersionInfo version;

        VersionFilter(VersionInfo versionInfo) {
            this.version = versionInfo;
        }

        @Override // ohos.global.icu.text.UnicodeSet.Filter
        public boolean contains(int i) {
            VersionInfo age = UCharacter.getAge(i);
            return !Utility.sameObjects(age, UnicodeSet.NO_VERSION) && age.compareTo(this.version) <= 0;
        }
    }

    private void applyFilter(Filter filter, UnicodeSet unicodeSet) {
        clear();
        int rangeCount = unicodeSet.getRangeCount();
        int i = -1;
        for (int i2 = 0; i2 < rangeCount; i2++) {
            int rangeEnd = unicodeSet.getRangeEnd(i2);
            for (int rangeStart = unicodeSet.getRangeStart(i2); rangeStart <= rangeEnd; rangeStart++) {
                if (filter.contains(rangeStart)) {
                    if (i < 0) {
                        i = rangeStart;
                    }
                } else if (i >= 0) {
                    add_unchecked(i, rangeStart - 1);
                    i = -1;
                }
            }
        }
        if (i >= 0) {
            add_unchecked(i, 1114111);
        }
    }

    private static String mungeCharName(String str) {
        String trimWhiteSpace = PatternProps.trimWhiteSpace(str);
        StringBuilder sb = null;
        for (int i = 0; i < trimWhiteSpace.length(); i++) {
            char charAt = trimWhiteSpace.charAt(i);
            if (PatternProps.isWhiteSpace(charAt)) {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append((CharSequence) trimWhiteSpace, 0, i);
                } else if (sb.charAt(sb.length() - 1) == ' ') {
                }
                charAt = ' ';
            }
            if (sb != null) {
                sb.append(charAt);
            }
        }
        return sb == null ? trimWhiteSpace : sb.toString();
    }

    public UnicodeSet applyIntPropertyValue(int i, int i2) {
        if (i == 8192) {
            applyFilter(new GeneralCategoryMaskFilter(i2), CharacterPropertiesImpl.getInclusionsForProperty(i));
        } else if (i == 28672) {
            applyFilter(new ScriptExtensionsFilter(i2), CharacterPropertiesImpl.getInclusionsForProperty(i));
        } else if (i < 0 || i >= 65) {
            if (4096 > i || i >= 4121) {
                throw new IllegalArgumentException("unsupported property " + i);
            }
            applyFilter(new IntPropertyFilter(i, i2), CharacterPropertiesImpl.getInclusionsForProperty(i));
        } else if (i2 == 0 || i2 == 1) {
            set(CharacterProperties.getBinaryPropertySet(i));
            if (i2 == 0) {
                complement();
            }
        } else {
            clear();
        }
        return this;
    }

    public UnicodeSet applyPropertyAlias(String str, String str2) {
        return applyPropertyAlias(str, str2, null);
    }

    public UnicodeSet applyPropertyAlias(String str, String str2, SymbolTable symbolTable) {
        int i;
        checkFrozen();
        if (symbolTable != null && (symbolTable instanceof XSymbolTable) && ((XSymbolTable) symbolTable).applyPropertyAlias(str, str2, this)) {
            return this;
        }
        XSymbolTable xSymbolTable = XSYMBOL_TABLE;
        if (xSymbolTable != null && xSymbolTable.applyPropertyAlias(str, str2, this)) {
            return this;
        }
        int i2 = 1;
        boolean z = false;
        if (str2.length() > 0) {
            i = UCharacter.getPropertyEnum(str);
            if (i == 4101) {
                i = 8192;
            }
            if ((i >= 0 && i < 65) || ((i >= 4096 && i < 4121) || (i >= 8192 && i < 8193))) {
                try {
                    i2 = UCharacter.getPropertyValueEnum(i, str2);
                } catch (IllegalArgumentException e) {
                    if (i == 4098 || i == 4112 || i == 4113) {
                        i2 = Integer.parseInt(PatternProps.trimWhiteSpace(str2));
                        if (i2 < 0 || i2 > 255) {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }
            } else if (i == 12288) {
                applyFilter(new NumericValueFilter(Double.parseDouble(PatternProps.trimWhiteSpace(str2))), CharacterPropertiesImpl.getInclusionsForProperty(i));
                return this;
            } else if (i == 16384) {
                applyFilter(new VersionFilter(VersionInfo.getInstance(mungeCharName(str2))), CharacterPropertiesImpl.getInclusionsForProperty(i));
                return this;
            } else if (i == 16389) {
                int charFromExtendedName = UCharacter.getCharFromExtendedName(mungeCharName(str2));
                if (charFromExtendedName != -1) {
                    clear();
                    add_unchecked(charFromExtendedName);
                    return this;
                }
                throw new IllegalArgumentException("Invalid character name");
            } else if (i == 16395) {
                throw new IllegalArgumentException("Unicode_1_Name (na1) not supported");
            } else if (i == 28672) {
                i2 = UCharacter.getPropertyValueEnum(UProperty.SCRIPT, str2);
            } else {
                throw new IllegalArgumentException("Unsupported property");
            }
        } else {
            UPropertyAliases uPropertyAliases = UPropertyAliases.INSTANCE;
            int propertyValueEnum = uPropertyAliases.getPropertyValueEnum(8192, str);
            if (propertyValueEnum == -1) {
                propertyValueEnum = uPropertyAliases.getPropertyValueEnum(UProperty.SCRIPT, str);
                if (propertyValueEnum == -1) {
                    int propertyEnum = uPropertyAliases.getPropertyEnum(str);
                    int i3 = propertyEnum == -1 ? -1 : propertyEnum;
                    if (i3 >= 0 && i3 < 65) {
                        i = i3;
                    } else if (i3 != -1) {
                        throw new IllegalArgumentException("Missing property value");
                    } else if (UPropertyAliases.compare(ANY_ID, str) == 0) {
                        set(0, 1114111);
                        return this;
                    } else if (UPropertyAliases.compare(ASCII_ID, str) == 0) {
                        set(0, 127);
                        return this;
                    } else if (UPropertyAliases.compare(ASSIGNED, str) == 0) {
                        z = true;
                        i = 8192;
                    } else {
                        throw new IllegalArgumentException("Invalid property alias: " + str + "=" + str2);
                    }
                } else {
                    i = 4106;
                }
            } else {
                i = 8192;
            }
            i2 = propertyValueEnum;
        }
        applyIntPropertyValue(i, i2);
        if (z) {
            complement();
        }
        return this;
    }

    private static boolean resemblesPropertyPattern(String str, int i) {
        if (i + 5 > str.length()) {
            return false;
        }
        if (str.regionMatches(i, "[:", 0, 2) || str.regionMatches(true, i, "\\p", 0, 2) || str.regionMatches(i, "\\N", 0, 2)) {
            return true;
        }
        return false;
    }

    private static boolean resemblesPropertyPattern(RuleCharacterIterator ruleCharacterIterator, int i) {
        int i2 = i & -3;
        Object pos = ruleCharacterIterator.getPos(null);
        int next = ruleCharacterIterator.next(i2);
        boolean z = false;
        if (next == 91 || next == 92) {
            int next2 = ruleCharacterIterator.next(i2 & -5);
            if (next != 91 ? next2 == 78 || next2 == 112 || next2 == 80 : next2 == 58) {
                z = true;
            }
        }
        ruleCharacterIterator.setPos(pos);
        return z;
    }

    private UnicodeSet applyPropertyPattern(String str, ParsePosition parsePosition, SymbolTable symbolTable) {
        boolean z;
        boolean z2;
        int i;
        String str2;
        String str3;
        int index = parsePosition.getIndex();
        if (index + 5 > str.length()) {
            return null;
        }
        int i2 = 2;
        boolean z3 = false;
        if (str.regionMatches(index, "[:", 0, 2)) {
            i = PatternProps.skipWhiteSpace(str, index + 2);
            if (i >= str.length() || str.charAt(i) != '^') {
                z2 = false;
                z = false;
                z3 = true;
            } else {
                i++;
                z2 = false;
                z = true;
                z3 = true;
            }
        } else if (!str.regionMatches(true, index, "\\p", 0, 2) && !str.regionMatches(index, "\\N", 0, 2)) {
            return null;
        } else {
            char charAt = str.charAt(index + 1);
            boolean z4 = charAt == 'P';
            boolean z5 = charAt == 'N';
            int skipWhiteSpace = PatternProps.skipWhiteSpace(str, index + 2);
            if (skipWhiteSpace != str.length()) {
                int i3 = skipWhiteSpace + 1;
                if (str.charAt(skipWhiteSpace) == '{') {
                    z = z4;
                    z2 = z5;
                    i = i3;
                }
            }
            return null;
        }
        int indexOf = str.indexOf(z3 ? ":]" : "}", i);
        if (indexOf < 0) {
            return null;
        }
        int indexOf2 = str.indexOf(61, i);
        if (indexOf2 < 0 || indexOf2 >= indexOf || z2) {
            str3 = str.substring(i, indexOf);
            if (z2) {
                str3 = "na";
                str2 = str3;
            } else {
                str2 = "";
            }
        } else {
            str3 = str.substring(i, indexOf2);
            str2 = str.substring(indexOf2 + 1, indexOf);
        }
        applyPropertyAlias(str3, str2, symbolTable);
        if (z) {
            complement();
        }
        if (!z3) {
            i2 = 1;
        }
        parsePosition.setIndex(indexOf + i2);
        return this;
    }

    private void applyPropertyPattern(RuleCharacterIterator ruleCharacterIterator, Appendable appendable, SymbolTable symbolTable) {
        String lookahead = ruleCharacterIterator.lookahead();
        ParsePosition parsePosition = new ParsePosition(0);
        applyPropertyPattern(lookahead, parsePosition, symbolTable);
        if (parsePosition.getIndex() == 0) {
            syntaxError(ruleCharacterIterator, "Invalid property pattern");
        }
        ruleCharacterIterator.jumpahead(parsePosition.getIndex());
        append(appendable, lookahead.substring(0, parsePosition.getIndex()));
    }

    private static final void addCaseMapping(UnicodeSet unicodeSet, int i, StringBuilder sb) {
        if (i < 0) {
            return;
        }
        if (i > 31) {
            unicodeSet.add(i);
            return;
        }
        unicodeSet.add(sb.toString());
        sb.setLength(0);
    }

    public UnicodeSet closeOver(int i) {
        checkFrozen();
        if ((i & 6) != 0) {
            UCaseProps uCaseProps = UCaseProps.INSTANCE;
            UnicodeSet unicodeSet = new UnicodeSet(this);
            ULocale uLocale = ULocale.ROOT;
            int i2 = i & 2;
            if (i2 != 0 && unicodeSet.hasStrings()) {
                unicodeSet.strings.clear();
            }
            int rangeCount = getRangeCount();
            StringBuilder sb = new StringBuilder();
            for (int i3 = 0; i3 < rangeCount; i3++) {
                int rangeStart = getRangeStart(i3);
                int rangeEnd = getRangeEnd(i3);
                if (i2 != 0) {
                    while (rangeStart <= rangeEnd) {
                        uCaseProps.addCaseClosure(rangeStart, unicodeSet);
                        rangeStart++;
                    }
                } else {
                    while (rangeStart <= rangeEnd) {
                        addCaseMapping(unicodeSet, uCaseProps.toFullLower(rangeStart, null, sb, 1), sb);
                        addCaseMapping(unicodeSet, uCaseProps.toFullTitle(rangeStart, null, sb, 1), sb);
                        addCaseMapping(unicodeSet, uCaseProps.toFullUpper(rangeStart, null, sb, 1), sb);
                        addCaseMapping(unicodeSet, uCaseProps.toFullFolding(rangeStart, sb, 0), sb);
                        rangeStart++;
                    }
                }
            }
            if (hasStrings()) {
                if (i2 != 0) {
                    for (String str : this.strings) {
                        String foldCase = UCharacter.foldCase(str, 0);
                        if (!uCaseProps.addStringCaseClosure(foldCase, unicodeSet)) {
                            unicodeSet.add(foldCase);
                        }
                    }
                } else {
                    BreakIterator wordInstance = BreakIterator.getWordInstance(uLocale);
                    for (String str2 : this.strings) {
                        unicodeSet.add(UCharacter.toLowerCase(uLocale, str2));
                        unicodeSet.add(UCharacter.toTitleCase(uLocale, str2, wordInstance));
                        unicodeSet.add(UCharacter.toUpperCase(uLocale, str2));
                        unicodeSet.add(UCharacter.foldCase(str2, 0));
                    }
                }
            }
            set(unicodeSet);
        }
        return this;
    }

    public boolean isFrozen() {
        return (this.bmpSet == null && this.stringSpan == null) ? false : true;
    }

    public UnicodeSet freeze() {
        if (!isFrozen()) {
            compact();
            if (hasStrings()) {
                this.stringSpan = new UnicodeSetStringSpan(this, new ArrayList(this.strings), 127);
            }
            if (this.stringSpan == null || !this.stringSpan.needsStringSpanUTF16()) {
                this.bmpSet = new BMPSet(this.list, this.len);
            }
        }
        return this;
    }

    public int span(CharSequence charSequence, SpanCondition spanCondition) {
        return span(charSequence, 0, spanCondition);
    }

    public int span(CharSequence charSequence, int i, SpanCondition spanCondition) {
        int length = charSequence.length();
        if (i < 0) {
            i = 0;
        } else if (i >= length) {
            return length;
        }
        if (this.bmpSet != null) {
            return this.bmpSet.span(charSequence, i, spanCondition, null);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.span(charSequence, i, spanCondition);
        }
        if (hasStrings()) {
            UnicodeSetStringSpan unicodeSetStringSpan = new UnicodeSetStringSpan(this, new ArrayList(this.strings), spanCondition == SpanCondition.NOT_CONTAINED ? 33 : 34);
            if (unicodeSetStringSpan.needsStringSpanUTF16()) {
                return unicodeSetStringSpan.span(charSequence, i, spanCondition);
            }
        }
        return spanCodePointsAndCount(charSequence, i, spanCondition, null);
    }

    @Deprecated
    public int spanAndCount(CharSequence charSequence, int i, SpanCondition spanCondition, OutputInt outputInt) {
        if (outputInt != null) {
            int length = charSequence.length();
            if (i < 0) {
                i = 0;
            } else if (i >= length) {
                return length;
            }
            if (this.stringSpan != null) {
                return this.stringSpan.spanAndCount(charSequence, i, spanCondition, outputInt);
            }
            if (this.bmpSet != null) {
                return this.bmpSet.span(charSequence, i, spanCondition, outputInt);
            }
            if (!hasStrings()) {
                return spanCodePointsAndCount(charSequence, i, spanCondition, outputInt);
            }
            return new UnicodeSetStringSpan(this, new ArrayList(this.strings), (spanCondition == SpanCondition.NOT_CONTAINED ? 33 : 34) | 64).spanAndCount(charSequence, i, spanCondition, outputInt);
        }
        throw new IllegalArgumentException("outCount must not be null");
    }

    private int spanCodePointsAndCount(CharSequence charSequence, int i, SpanCondition spanCondition, OutputInt outputInt) {
        int i2 = 0;
        boolean z = spanCondition != SpanCondition.NOT_CONTAINED;
        int length = charSequence.length();
        do {
            int codePointAt = Character.codePointAt(charSequence, i);
            if (z != contains(codePointAt)) {
                break;
            }
            i2++;
            i += Character.charCount(codePointAt);
        } while (i < length);
        if (outputInt != null) {
            outputInt.value = i2;
        }
        return i;
    }

    public int spanBack(CharSequence charSequence, SpanCondition spanCondition) {
        return spanBack(charSequence, charSequence.length(), spanCondition);
    }

    public int spanBack(CharSequence charSequence, int i, SpanCondition spanCondition) {
        boolean z = false;
        if (i <= 0) {
            return 0;
        }
        if (i > charSequence.length()) {
            i = charSequence.length();
        }
        if (this.bmpSet != null) {
            return this.bmpSet.spanBack(charSequence, i, spanCondition);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.spanBack(charSequence, i, spanCondition);
        }
        if (hasStrings()) {
            UnicodeSetStringSpan unicodeSetStringSpan = new UnicodeSetStringSpan(this, new ArrayList(this.strings), spanCondition == SpanCondition.NOT_CONTAINED ? 17 : 18);
            if (unicodeSetStringSpan.needsStringSpanUTF16()) {
                return unicodeSetStringSpan.spanBack(charSequence, i, spanCondition);
            }
        }
        if (spanCondition != SpanCondition.NOT_CONTAINED) {
            z = true;
        }
        do {
            int codePointBefore = Character.codePointBefore(charSequence, i);
            if (z != contains(codePointBefore)) {
                break;
            }
            i -= Character.charCount(codePointBefore);
        } while (i > 0);
        return i;
    }

    public UnicodeSet cloneAsThawed() {
        return new UnicodeSet(this);
    }

    private void checkFrozen() {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    public static class EntryRange {
        public int codepoint;
        public int codepointEnd;

        EntryRange() {
        }

        public String toString() {
            StringBuilder sb;
            StringBuilder sb2 = new StringBuilder();
            int i = this.codepoint;
            if (i == this.codepointEnd) {
                sb = (StringBuilder) UnicodeSet._appendToPat(sb2, i, false);
            } else {
                StringBuilder sb3 = (StringBuilder) UnicodeSet._appendToPat(sb2, i, false);
                sb3.append(LocaleUtility.IETF_SEPARATOR);
                sb = (StringBuilder) UnicodeSet._appendToPat(sb3, this.codepointEnd, false);
            }
            return sb.toString();
        }
    }

    public Iterable<EntryRange> ranges() {
        return new EntryRangeIterable();
    }

    private class EntryRangeIterable implements Iterable<EntryRange> {
        private EntryRangeIterable() {
        }

        @Override // java.lang.Iterable
        public Iterator<EntryRange> iterator() {
            return new EntryRangeIterator();
        }
    }

    private class EntryRangeIterator implements Iterator<EntryRange> {
        int pos;
        EntryRange result;

        private EntryRangeIterator() {
            this.result = new EntryRange();
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.pos < UnicodeSet.this.len - 1;
        }

        @Override // java.util.Iterator
        public EntryRange next() {
            if (this.pos < UnicodeSet.this.len - 1) {
                EntryRange entryRange = this.result;
                int[] iArr = UnicodeSet.this.list;
                int i = this.pos;
                this.pos = i + 1;
                entryRange.codepoint = iArr[i];
                EntryRange entryRange2 = this.result;
                int[] iArr2 = UnicodeSet.this.list;
                int i2 = this.pos;
                this.pos = i2 + 1;
                entryRange2.codepointEnd = iArr2[i2] - 1;
                return this.result;
            }
            throw new NoSuchElementException();
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override // java.lang.Iterable
    public Iterator<String> iterator() {
        return new UnicodeSetIterator2(this);
    }

    /* access modifiers changed from: private */
    public static class UnicodeSetIterator2 implements Iterator<String> {
        private char[] buffer;
        private int current;
        private int item;
        private int len;
        private int limit;
        private int[] sourceList;
        private SortedSet<String> sourceStrings;
        private Iterator<String> stringIterator;

        UnicodeSetIterator2(UnicodeSet unicodeSet) {
            this.len = unicodeSet.len - 1;
            if (this.len > 0) {
                this.sourceStrings = unicodeSet.strings;
                this.sourceList = unicodeSet.list;
                int[] iArr = this.sourceList;
                int i = this.item;
                this.item = i + 1;
                this.current = iArr[i];
                int i2 = this.item;
                this.item = i2 + 1;
                this.limit = iArr[i2];
                return;
            }
            this.stringIterator = unicodeSet.strings.iterator();
            this.sourceList = null;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.sourceList != null || this.stringIterator.hasNext();
        }

        @Override // java.util.Iterator
        public String next() {
            int[] iArr = this.sourceList;
            if (iArr == null) {
                return this.stringIterator.next();
            }
            int i = this.current;
            this.current = i + 1;
            if (this.current >= this.limit) {
                int i2 = this.item;
                if (i2 >= this.len) {
                    this.stringIterator = this.sourceStrings.iterator();
                    this.sourceList = null;
                } else {
                    this.item = i2 + 1;
                    this.current = iArr[i2];
                    int i3 = this.item;
                    this.item = i3 + 1;
                    this.limit = iArr[i3];
                }
            }
            if (i <= 65535) {
                return String.valueOf((char) i);
            }
            if (this.buffer == null) {
                this.buffer = new char[2];
            }
            int i4 = i - 65536;
            char[] cArr = this.buffer;
            cArr[0] = (char) ((i4 >>> 10) + 55296);
            cArr[1] = (char) ((i4 & UCharacterProperty.MAX_SCRIPT) + UTF16.TRAIL_SURROGATE_MIN_VALUE);
            return String.valueOf(cArr);
        }

        @Override // java.util.Iterator
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public <T extends CharSequence> boolean containsAll(Iterable<T> iterable) {
        for (T t : iterable) {
            if (!contains(t)) {
                return false;
            }
        }
        return true;
    }

    public <T extends CharSequence> boolean containsNone(Iterable<T> iterable) {
        for (T t : iterable) {
            if (contains(t)) {
                return false;
            }
        }
        return true;
    }

    public final <T extends CharSequence> boolean containsSome(Iterable<T> iterable) {
        return !containsNone(iterable);
    }

    public <T extends CharSequence> UnicodeSet addAll(T... tArr) {
        checkFrozen();
        for (T t : tArr) {
            add(t);
        }
        return this;
    }

    public <T extends CharSequence> UnicodeSet removeAll(Iterable<T> iterable) {
        checkFrozen();
        for (T t : iterable) {
            remove(t);
        }
        return this;
    }

    public <T extends CharSequence> UnicodeSet retainAll(Iterable<T> iterable) {
        checkFrozen();
        UnicodeSet unicodeSet = new UnicodeSet();
        unicodeSet.addAll((Iterable<?>) iterable);
        retainAll(unicodeSet);
        return this;
    }

    public int compareTo(UnicodeSet unicodeSet) {
        return compareTo(unicodeSet, ComparisonStyle.SHORTER_FIRST);
    }

    public int compareTo(UnicodeSet unicodeSet, ComparisonStyle comparisonStyle) {
        int compare;
        int size;
        boolean z = false;
        if (comparisonStyle == ComparisonStyle.LEXICOGRAPHIC || (size = size() - unicodeSet.size()) == 0) {
            int i = 0;
            while (true) {
                int[] iArr = this.list;
                int i2 = iArr[i];
                int[] iArr2 = unicodeSet.list;
                int i3 = i2 - iArr2[i];
                if (i3 != 0) {
                    if (iArr[i] == 1114112) {
                        if (!hasStrings()) {
                            return 1;
                        }
                        return compare(this.strings.first(), unicodeSet.list[i]);
                    } else if (iArr2[i] != 1114112) {
                        return (i & 1) == 0 ? i3 : -i3;
                    } else {
                        if (unicodeSet.hasStrings() && (compare = compare(unicodeSet.strings.first(), this.list[i])) <= 0) {
                            return compare < 0 ? 1 : 0;
                        }
                        return -1;
                    }
                } else if (iArr[i] == 1114112) {
                    return compare(this.strings, unicodeSet.strings);
                } else {
                    i++;
                }
            }
        } else {
            boolean z2 = size < 0;
            if (comparisonStyle == ComparisonStyle.SHORTER_FIRST) {
                z = true;
            }
            return z2 == z ? -1 : 1;
        }
    }

    public int compareTo(Iterable<String> iterable) {
        return compare(this, iterable);
    }

    public static int compare(CharSequence charSequence, int i) {
        return CharSequences.compare(charSequence, i);
    }

    public static int compare(int i, CharSequence charSequence) {
        return -CharSequences.compare(charSequence, i);
    }

    public static <T extends Comparable<T>> int compare(Iterable<T> iterable, Iterable<T> iterable2) {
        return compare(iterable.iterator(), iterable2.iterator());
    }

    @Deprecated
    public static <T extends Comparable<T>> int compare(Iterator<T> it, Iterator<T> it2) {
        while (it.hasNext()) {
            if (!it2.hasNext()) {
                return 1;
            }
            int compareTo = it.next().compareTo(it2.next());
            if (compareTo != 0) {
                return compareTo;
            }
        }
        return it2.hasNext() ? -1 : 0;
    }

    public static <T extends Comparable<T>> int compare(Collection<T> collection, Collection<T> collection2, ComparisonStyle comparisonStyle) {
        int size;
        if (comparisonStyle == ComparisonStyle.LEXICOGRAPHIC || (size = collection.size() - collection2.size()) == 0) {
            return compare(collection, collection2);
        }
        boolean z = false;
        boolean z2 = size < 0;
        if (comparisonStyle == ComparisonStyle.SHORTER_FIRST) {
            z = true;
        }
        if (z2 == z) {
            return -1;
        }
        return 1;
    }

    public static <T, U extends Collection<T>> U addAllTo(Iterable<T> iterable, U u) {
        for (T t : iterable) {
            u.add(t);
        }
        return u;
    }

    public static <T> T[] addAllTo(Iterable<T> iterable, T[] tArr) {
        int i = 0;
        for (T t : iterable) {
            tArr[i] = t;
            i++;
        }
        return tArr;
    }

    public Collection<String> strings() {
        if (hasStrings()) {
            return Collections.unmodifiableSortedSet(this.strings);
        }
        return EMPTY_STRINGS;
    }

    @Deprecated
    public static int getSingleCodePoint(CharSequence charSequence) {
        return CharSequences.getSingleCodePoint(charSequence);
    }

    @Deprecated
    public UnicodeSet addBridges(UnicodeSet unicodeSet) {
        UnicodeSetIterator unicodeSetIterator = new UnicodeSetIterator(new UnicodeSet(this).complement());
        while (unicodeSetIterator.nextRange()) {
            if (!(unicodeSetIterator.codepoint == 0 || unicodeSetIterator.codepoint == UnicodeSetIterator.IS_STRING || unicodeSetIterator.codepointEnd == 1114111 || !unicodeSet.contains(unicodeSetIterator.codepoint, unicodeSetIterator.codepointEnd))) {
                add(unicodeSetIterator.codepoint, unicodeSetIterator.codepointEnd);
            }
        }
        return this;
    }

    @Deprecated
    public int findIn(CharSequence charSequence, int i, boolean z) {
        while (i < charSequence.length()) {
            int charAt = UTF16.charAt(charSequence, i);
            if (contains(charAt) != z) {
                break;
            }
            i += UTF16.getCharCount(charAt);
        }
        return i;
    }

    @Deprecated
    public int findLastIn(CharSequence charSequence, int i, boolean z) {
        int i2 = i - 1;
        while (i2 >= 0) {
            int charAt = UTF16.charAt(charSequence, i2);
            if (contains(charAt) != z) {
                break;
            }
            i2 -= UTF16.getCharCount(charAt);
        }
        if (i2 < 0) {
            return -1;
        }
        return i2;
    }

    @Deprecated
    public String stripFrom(CharSequence charSequence, boolean z) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < charSequence.length()) {
            int findIn = findIn(charSequence, i, !z);
            sb.append(charSequence.subSequence(i, findIn));
            i = findIn(charSequence, findIn, z);
        }
        return sb.toString();
    }

    @Deprecated
    public static XSymbolTable getDefaultXSymbolTable() {
        return XSYMBOL_TABLE;
    }

    @Deprecated
    public static void setDefaultXSymbolTable(XSymbolTable xSymbolTable) {
        CharacterPropertiesImpl.clear();
        XSYMBOL_TABLE = xSymbolTable;
    }
}
